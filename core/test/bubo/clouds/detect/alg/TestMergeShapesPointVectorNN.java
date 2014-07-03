/*
 * Copyright (c) 2013-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Project BUBO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bubo.clouds.detect.alg;

import bubo.clouds.detect.shape.*;
import georegression.fitting.cylinder.CodecCylinder3D_F64;
import georegression.fitting.cylinder.FitCylinderToPoints_F64;
import georegression.fitting.sphere.CodecSphere3D_F64;
import georegression.fitting.sphere.FitSphereToPoints_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import georegression.struct.shapes.Sphere3D_F64;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestMergeShapesPointVectorNN {

	Random rand = new Random(234);

	List<ShapeDescription> objects = new ArrayList<ShapeDescription>();
	ShapeDescription descSphere;
	ShapeDescription descCylinder;

	int pointIndex;

	public TestMergeShapesPointVectorNN() {

		double angleTolerance = 0.1;
		double distanceTolerance = 0.1;
		int fitIterations = 150;
		double ransacDistanceThreshold = 0.2;

		descSphere = new ShapeDescription();
		descSphere.modelDistance = new DistanceFromModel_P_to_PVNN(new DistanceSphereToPoint3D());
		descSphere.modelGenerator = new GenerateSpherePointVector(angleTolerance, distanceTolerance);
		descSphere.modelFitter = new ModelFitter_P_to_PVNN(new FitSphereToPoints_F64(fitIterations));
		descSphere.modelCheck = new CheckShapeAcceptAll();
		descSphere.codec = new CodecSphere3D_F64();
		descSphere.thresholdFit = ransacDistanceThreshold;

		descCylinder = new ShapeDescription();
		descCylinder.modelDistance = new DistanceFromModel_P_to_PVNN(new DistanceCylinderToPoint3D());
		descCylinder.modelGenerator = new GenerateCylinderPointVector(angleTolerance, distanceTolerance);
		descCylinder.modelFitter = new ModelFitter_P_to_PVNN(new FitCylinderToPoints_F64(fitIterations));
		descCylinder.modelCheck = new CheckShapeAcceptAll();
		descCylinder.codec = new CodecCylinder3D_F64();
		descCylinder.thresholdFit = ransacDistanceThreshold;

		objects.add(descSphere);
		objects.add(descCylinder);
	}

	@Before
	public void setup() {
		pointIndex = 0;
	}

	/**
	 * Give it a single model.  It should do nothing and return it unmolested.
	 */
	@Test
	public void merge_one() {

		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.6, 0.9);
		alg.setup(objects, null);

		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 200, cloud);

		List<FoundShape> shapes = new ArrayList<FoundShape>();
		FoundShape s = new FoundShape();
		s.whichShape = 0;
		s.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 50; i++) {
			s.points.add(cloud.get(i + 10));
		}
		shapes.add(s);

		alg.process(shapes, cloud.size());

		List<FoundShape> found = alg.getOutput();

		assertEquals(1, found.size());
		FoundShape foundShape = found.get(0);
		assertEquals(50, foundShape.points.size());
	}

	/**
	 * Give it N models of the same shape with different points.  See if they are merged together into
	 * a single shape with all the points.
	 */
	@Test
	public void merge_N_identical() {

		// set the commonPointsFraction really slow so that it will consider matches to all the points
		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.01, 0.9);
		alg.setup(objects, null);

		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 300, cloud);

		for (int numShapes = 2; numShapes <= 5; numShapes++) {
			for (int numShuffles = 0; numShuffles < 5; numShuffles++) {
				List<FoundShape> shapes = new ArrayList<FoundShape>();

				// create the shapes which are the same object really but with mostly independent points
				for (int i = 0; i < numShapes; i++) {
					FoundShape s = new FoundShape();
					s.whichShape = 0;
					s.modelParam = new Sphere3D_F64(1, 2, 3, 4);
					int start = i * cloud.size() / numShapes;
					int stop = (i + 1) * cloud.size() / numShapes;

					if (i != numShapes - 1) {
						stop += 10;// give it some overlap in points
					}

					for (int j = start; j < stop; j++) {
						s.points.add(cloud.get(j));
					}
					shapes.add(s);
				}

				// shuffle to avoid bias caused by the order its processed in
				Collections.shuffle(shapes, rand);

				// run the algoritm
				alg.process(shapes, cloud.size());

				List<FoundShape> found = alg.getOutput();

				assertEquals(1, found.size());
				FoundShape foundShape = found.get(0);
				// it should have all the points in it, just once
				assertEquals(cloud.size(), foundShape.points.size());
			}
		}
	}

	/**
	 * Two models will be provided.  One will be a perfect for all the data and the other will be a good
	 * fit for some of the data.  It should select the perfect one for output and merge it with the less
	 * than perfect.
	 */
	@Test
	public void merge_better_model() {
		// set the commonPointsFraction really slow so that it will consider matches to all the points
		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.01, 0.9);
		alg.setup(objects, null);

		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 300, cloud);

		for (int numShapes = 2; numShapes <= 5; numShapes++) {
			for (int numShuffles = 0; numShuffles < 5; numShuffles++) {
				List<FoundShape> shapes = new ArrayList<FoundShape>();

				// create the shapes which are the same object really but with mostly independent points
				for (int i = 0; i < numShapes; i++) {
					FoundShape s = new FoundShape();
					s.whichShape = 0;
					if (i == 0)
						s.modelParam = new Sphere3D_F64(1, 2, 3, 4);
					else
						// bad model which will still fit some of the points
						s.modelParam = new Sphere3D_F64(1.5, 2, 3, 4.5);
					int start = i * cloud.size() / numShapes;
					int stop = (i + 1) * cloud.size() / numShapes;

					if (i != numShapes - 1) {
						stop += 10;// give it some overlap in points
					}

					for (int j = start; j < stop; j++) {
						s.points.add(cloud.get(j));
					}
					shapes.add(s);
				}

				// shuffle to avoid bias caused by the order its processed in
				Collections.shuffle(shapes, rand);

				// run the algorithm
				alg.process(shapes, cloud.size());

				List<FoundShape> found = alg.getOutput();

				assertEquals(1, found.size());
				FoundShape foundShape = found.get(0);
				// it should have all the points in it, just once
				assertEquals(cloud.size(), foundShape.points.size());
				Sphere3D_F64 foundModel = (Sphere3D_F64) foundShape.modelParam;
				// make sure the correct model is selected
				assertEquals(1, foundModel.center.x, 1e-8);
				assertEquals(2, foundModel.center.y, 1e-8);
				assertEquals(3, foundModel.center.z, 1e-8);
				assertEquals(4, foundModel.radius, 1e-8);
			}
		}
	}

	/**
	 * See if it is correctly using the commonPointsFraction parameter
	 */
	@Test
	public void merge_threshold_commonPointsFraction() {
		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 200, cloud);

		// create two shapes which will have a 59% overlap of points
		List<FoundShape> shapes = new ArrayList<FoundShape>();
		FoundShape s0 = new FoundShape();
		s0.whichShape = 0;
		s0.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 100; i++) {
			s0.points.add(cloud.get(i));
		}
		FoundShape s1 = new FoundShape();
		s1.whichShape = 0;
		s1.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 100; i++) {
			s1.points.add(cloud.get(i + 41));
		}

		shapes.add(s0);
		shapes.add(s1);

		// no merging should happen since they don't pass the first test
		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.6, 0.9);
		alg.setup(objects, null);
		alg.process(shapes, cloud.size());
		assertEquals(2, alg.getOutput().size());

		// now they should path both tests
		alg = new MergeShapesPointVectorNN(0.58, 0.9);
		alg.setup(objects, null);
		alg.process(shapes, cloud.size());
		assertEquals(1, alg.getOutput().size());
	}

	/**
	 * See if it is correctly using the commonMembershipFraction parameter
	 */
	@Test
	public void merge_threshold_distance() {
		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 200, cloud);
		addPoints(new Sphere3D_F64(1, 2.3, 3, 4), 200, cloud);

		// create two shapes which will have a 59% overlap of points
		List<FoundShape> shapes = new ArrayList<FoundShape>();
		FoundShape s0 = new FoundShape();
		s0.whichShape = 0;
		s0.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 200; i++) {
			s0.points.add(cloud.get(i));
		}
		FoundShape s1 = new FoundShape();
		s1.whichShape = 0;
		s1.modelParam = new Sphere3D_F64(1, 2.3, 3, 4);
		for (int i = 0; i < 200; i++) {
			s1.points.add(cloud.get(i + 200));
		}

		shapes.add(s0);
		shapes.add(s1);

		// no merging should happen since they don't pass the second test
		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.0, 0.9);
		alg.setup(objects, null);
		alg.process(shapes, cloud.size());
		assertEquals(2, alg.getOutput().size());

		// now they should path both tests
		alg = new MergeShapesPointVectorNN(0.0, 0.1);
		alg.setup(objects, null);
		alg.process(shapes, cloud.size());
		assertEquals(1, alg.getOutput().size());
	}

	/**
	 * There is some intersection, but they shouldn't be merged
	 */
	@Test
	public void merge_two_notSimilarAtAll() {
		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 200, cloud);
		addPoints(new Sphere3D_F64(1, 2, 3, 8), 200, cloud);

		// create two shapes which will have a 59% overlap of points
		List<FoundShape> shapes = new ArrayList<FoundShape>();
		FoundShape s0 = new FoundShape();
		s0.whichShape = 0;
		s0.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 200; i++) {
			s0.points.add(cloud.get(i));
		}
		FoundShape s1 = new FoundShape();
		s1.whichShape = 0;
		s1.modelParam = new Sphere3D_F64(1, 2, 3, 8);
		for (int i = 0; i < 200; i++) {
			s1.points.add(cloud.get(i + 200));
		}

		shapes.add(s0);
		shapes.add(s1);

		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.6, 0.9);
		alg.setup(objects, null);
		alg.process(shapes, cloud.size());
		assertEquals(2, alg.getOutput().size());
	}

	@Test
	public void findMembersRigorous() {
		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.01, 0.9);
		alg.setup(objects, null);

		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 300, cloud);
		addPointsRandom(200, 20, cloud);

		FoundShape s = new FoundShape();
		s.whichShape = 0;
		s.modelParam = new Sphere3D_F64(1, 2, 3, 4);

		List<PointVectorNN> points = new ArrayList<PointVectorNN>();
		for (int i = 0; i < 50; i++) {
			points.add(cloud.get(i + 40));
		}
		// add a few points far away
		points.add(cloud.get(300));
		points.add(cloud.get(301));
		points.add(cloud.get(302));


		List<PointVectorNN> members = new ArrayList<PointVectorNN>();
		alg.member.resize(cloud.size());
		alg.findMembersRigorous(s, points, members);

		// should have 80 points total and contains each of the points exactly once
		assertEquals(50, members.size());
		for (int i = 40; i < 90; i++) {
			assertTrue(members.contains(cloud.get(i)));
		}
	}

	@Test
	public void mergeShape() {
		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.01, 0.9);
		alg.setup(objects, null);

		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 300, cloud);

		FoundShape s = new FoundShape();
		s.whichShape = 0;
		s.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 50; i++) {
			s.points.add(cloud.get(i + 10));
		}

		List<PointVectorNN> points = new ArrayList<PointVectorNN>();
		for (int i = 0; i < 50; i++) {
			points.add(cloud.get(i + 40));
		}

		alg.member.resize(cloud.size());
		assertTrue(alg.mergeShape(s, points));

		// should have 80 points total and contains each of the points exactly once
		assertEquals(80, s.points.size());
		for (int i = 10; i < 90; i++) {
			assertTrue(s.points.contains(cloud.get(i)));
		}

		// all should be false in member
		for (int i = 0; i < cloud.size(); i++) {
			assertFalse(alg.member.data[i]);
		}
	}

	/**
	 * See if it is refining the points after merging
	 */
	@Test
	public void refine() {
		FindMatchSetPointVectorNN findMatchSet = new FindMatchSetPointVectorNN();
		LocalFitShapeNN refine = new LocalFitShapeNN(100, 1e-8, findMatchSet);
		MergeShapesPointVectorNN alg = new MergeShapesPointVectorNN(0.01, 0.9);
		alg.setup(objects, refine);

		// create points from two slightly different shapes
		List<PointVectorNN> cloud = new ArrayList<PointVectorNN>();
		addPoints(new Sphere3D_F64(1, 2, 3, 4), 100, cloud);
		addPoints(new Sphere3D_F64(1.05, 2, 3, 4), 100, cloud);

		FoundShape s0 = new FoundShape();
		s0.whichShape = 0;
		s0.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 100; i++) {
			s0.points.add(cloud.get(i));
		}

		FoundShape s1 = new FoundShape();
		s1.whichShape = 0;
		s1.modelParam = new Sphere3D_F64(1, 2, 3, 4);
		for (int i = 0; i < 120; i++) {
			s1.points.add(cloud.get(i + 80));
		}

		List<FoundShape> shapes = new ArrayList<FoundShape>();
		shapes.add(s0);
		shapes.add(s1);

		// after merging there should be one shape and its parameters should be a bit off
		alg.process(shapes, cloud.size());

		List<FoundShape> found = alg.getOutput();

		assertEquals(1, found.size());
		Sphere3D_F64 foundModel = (Sphere3D_F64) found.get(0).modelParam;

		// shouldn't match either model
		assertTrue(Math.abs(foundModel.center.x - 1) > 1e-8);
		assertTrue(Math.abs(foundModel.center.x - 1.05) > 1e-8);
	}

	public void addPointsRandom(double scale, int total, List<PointVectorNN> cloud) {
		for (int i = 0; i < total; i++) {
			PointVectorNN pv = new PointVectorNN();

			pv.p = new Point3D_F64(rand.nextGaussian() * scale, rand.nextGaussian() * scale, rand.nextGaussian() * scale);
			pv.normal = new Vector3D_F64(rand.nextGaussian() * scale, rand.nextGaussian() * scale, rand.nextGaussian() * scale);

			pv.index = pointIndex++;

			cloud.add(pv);
		}
	}

	public void addPoints(Sphere3D_F64 sphere, int total, List<PointVectorNN> cloud) {
		for (int i = 0; i < total; i++) {

			double phi = 2 * Math.PI * rand.nextDouble();
			double theta = 2 * Math.PI * rand.nextDouble();

			PointVectorNN pv = TestGenerateSpherePointVector.createPt(sphere, phi, theta, 1);

			pv.index = pointIndex++;

			cloud.add(pv);
		}
	}

	public void addPoints(Cylinder3D_F64 cylinder, int total, List<PointVectorNN> cloud) {
		for (int i = 0; i < total; i++) {

			double h = 2 * (rand.nextDouble() - 0.5);
			double theta = 2 * Math.PI * rand.nextDouble();

			PointVectorNN pv = TestGenerateCylinderPointVector.createPt(cylinder, h, theta, 1);

			pv.index = pointIndex++;

			cloud.add(pv);
		}
	}

}
