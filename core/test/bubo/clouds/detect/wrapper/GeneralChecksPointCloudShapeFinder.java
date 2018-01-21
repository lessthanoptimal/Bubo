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

package bubo.clouds.detect.wrapper;

import bubo.clouds.detect.CloudShapeTypes;
import bubo.clouds.detect.PointCloudShapeFinder;
import bubo.clouds.detect.shape.*;
import bubo.clouds.detect.tools.PointCloudShapeTools;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Box3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import georegression.struct.shapes.Sphere3D_F64;
import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.struct.GrowQueue_I32;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * General tests for implementations of {@link bubo.clouds.detect.PointCloudShapeFinder}
 *
 * NOTE: Until the update in 2018 all these unit tests passed
 *
 * @author Peter Abeles
 */
public abstract class GeneralChecksPointCloudShapeFinder {

	Random rand = new Random(234);
	// how close to the expected set size does it need to be
	int tolSetSize = 2;
	double tolModelparam = 1e-6;

	protected GeneralChecksPointCloudShapeFinder(int tolSetSize, double tolModelparam) {
		this.tolSetSize = tolSetSize;
		this.tolModelparam = tolModelparam;
	}

	public abstract PointCloudShapeFinder createAlgorithm();

	/**
	 * Goes through each supported shape and sees if it can detect it by itsself with perfect data
	 * and no noise
	 */
	@Test
	public void detectShapesSingle() {

		PointCloudShapeFinder alg = createAlgorithm();

		List<CloudShapeTypes> shapes = alg.getShapesList();

		assertTrue(shapes.size() > 1);

		int N = 200;

		Cylinder3D_F64 cylinder = new Cylinder3D_F64(1, 2, 3, 0.5, -0.25, 0.1, 3);
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 2, 3, -0.5, 0.25, 1);
		Sphere3D_F64 sphere = new Sphere3D_F64(-1, -2, -3, 2.5);

		for (CloudShapeTypes shape : shapes) {
			List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

			switch (shape) {
				case CYLINDER:
					addShapeToCloud(cylinder, N, cloud);
					break;

				case PLANE:
					addShapeToCloud(plane, N, cloud);
					break;

				case SPHERE:
					addShapeToCloud(sphere, N, cloud);
					break;

				default:
					throw new RuntimeException("Unknown type " + shape);
			}

			alg.process(cloud, null);

			List<Point3D_F64> unmatched = new ArrayList<Point3D_F64>();
			alg.getUnmatched(unmatched);

			List<PointCloudShapeFinder.Shape> found = alg.getFound();

			// see if the error of the matched shape is reasonable and that the
			// point indexes are correctly set
			for (PointCloudShapeFinder.Shape s : found) {
				checkIndexes(s, cloud);
				double error = averageError(s);
				assertEquals(0, error, 0.1);
			}

			assertEquals(1, found.size());
			PointCloudShapeFinder.Shape s = found.get(0);

			assertEquals(N, unmatched.size() + findUsedCount(cloud, found));
			assertTrue(unmatched.size() <= tolSetSize);
			assertTrue(Math.abs(N - s.points.size()) <= tolSetSize);

			switch (shape) {
				case CYLINDER:
					TestGenerateCylinderPointVector.checkEquivalent(cylinder, (Cylinder3D_F64) s.parameters, tolModelparam);
					break;

				case PLANE:
					TestGeneratePlanePointVector.checkPlanes(plane, (PlaneGeneral3D_F64) s.parameters, tolModelparam);
					break;

				case SPHERE:
					TestGenerateSpherePointVector.checkSpheres(sphere, (Sphere3D_F64) s.parameters, tolModelparam);
					break;

				default:
					throw new RuntimeException("Unknown type " + shape);
			}
		}
	}

	@Test
	public void detectShapesMultiple() {
		PointCloudShapeFinder alg = createAlgorithm();

		// see if the algorithm can find multiple shapes
		if (!alg.isSupportMultipleObjects())
			return;

		List<CloudShapeTypes> shapes = alg.getShapesList();
		List<CloudShapeTypes> shapesSelect = new ArrayList<CloudShapeTypes>();
		shapesSelect.addAll(shapes);

		assertTrue(shapes.size() > 2);

		int N = 400;

		// shapes should be far away from each other
		Cylinder3D_F64 cylinder = new Cylinder3D_F64(1, 2, 3, 0.5, -0.25, 0.1, 3);
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 2, 10, 0, 0, 1);
		Sphere3D_F64 sphere = new Sphere3D_F64(-5, -6, -3, 2.5);

		for (int i = 0; i < 20; i++) {
			// construct the cloud from two random shapes
			List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

			Collections.shuffle(shapesSelect, rand);

			for (int j = 0; j < 2; j++) {
				switch (shapesSelect.get(j)) {
					case CYLINDER:
						addShapeToCloud(cylinder, N, cloud);
						break;

					case PLANE:
						addShapeToCloud(plane, N, cloud);
						break;

					case SPHERE:
						addShapeToCloud(sphere, N, cloud);
						break;
				}
			}
			alg.process(cloud, null);

			List<Point3D_F64> unmatched = new ArrayList<Point3D_F64>();
			alg.getUnmatched(unmatched);

			List<PointCloudShapeFinder.Shape> found = alg.getFound();

			// see if the error of the matched shape is reasonable and that the
			// point indexes are correctly set
			for (PointCloudShapeFinder.Shape s : found) {
				checkIndexes(s, cloud);
				double error = averageError(s);
				assertEquals(0, error, 0.1);
			}

			assertEquals(2, found.size());

			assertEquals(N * 2, unmatched.size() + findUsedCount(cloud, found));
			assertTrue(unmatched.size() <= tolSetSize * N);


			for (int j = 0; j < found.size(); j++) {
				PointCloudShapeFinder.Shape s = found.get(j);

				assertTrue(Math.abs(N - s.points.size()) <= tolSetSize);

				switch (s.type) {
					case CYLINDER:
						TestGenerateCylinderPointVector.checkEquivalent(cylinder, (Cylinder3D_F64) s.parameters, tolModelparam);
						break;

					case PLANE:
						TestGeneratePlanePointVector.checkPlanes(plane, (PlaneGeneral3D_F64) s.parameters, tolModelparam);
						break;

					case SPHERE:
						TestGenerateSpherePointVector.checkSpheres(sphere, (Sphere3D_F64) s.parameters, tolModelparam);
						break;
				}
			}
		}

	}

	/**
	 * Very basic test for the bounding cube.  basically sees if it blows up or not
	 */
	@Test
	public void checkBoundingCube() {
		PointCloudShapeFinder alg = createAlgorithm();

		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		int N = 200;

		Cylinder3D_F64 cylinder = new Cylinder3D_F64(1, 2, 3, 0.5, -0.25, 0.1, 3);
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 2, 3, -0.5, 0.25, 1);
		Sphere3D_F64 sphere = new Sphere3D_F64(-1, -2, -3, 2.5);
		switch (alg.getShapesList().get(0)) {
			case CYLINDER:
				addShapeToCloud(cylinder, N, cloud);
				break;

			case PLANE:
				addShapeToCloud(plane, N, cloud);
				break;

			case SPHERE:
				addShapeToCloud(sphere, N, cloud);
				break;
		}

		// check with a bounding box
		alg.process(cloud, new Box3D_F64(-10, -10, -10, 10, 10, 10));
		assertEquals(1, alg.getFound().size());
		assertTrue(alg.getFound().get(0).type == alg.getShapesList().get(0));

		// let it select the bounding box
		alg.process(cloud, null);
		assertEquals(1, alg.getFound().size());
		assertTrue(alg.getFound().get(0).type == alg.getShapesList().get(0));
	}

	private void checkIndexes(PointCloudShapeFinder.Shape shape, List<Point3D_F64> cloud) {
		assertEquals(shape.indexes.size, shape.points.size());
		GrowQueue_I32 indexes = shape.indexes;

		for (int i = 0; i < indexes.size; i++) {
			int found = indexes.get(i);
			int expected = cloud.indexOf(shape.points.get(i));
			assertEquals(found, expected);
		}
	}

	private void addShapeToCloud(Object shapeParam, int N, List<Point3D_F64> cloud) {
		if (shapeParam instanceof Cylinder3D_F64) {
			for (int i = 0; i < N; i++)
				cloud.add(PointCloudShapeTools.createPt((Cylinder3D_F64) shapeParam, rand.nextDouble() * 2, rand.nextDouble() * 2 * Math.PI));
		} else if (shapeParam instanceof PlaneNormal3D_F64) {
			for (int i = 0; i < N; i++)
				cloud.add(PointCloudShapeTools.createPt((PlaneNormal3D_F64) shapeParam, 3 * (rand.nextDouble() - 0.5), 3 * (rand.nextDouble() - 0.5)));
		} else if (shapeParam instanceof Sphere3D_F64) {
			for (int i = 0; i < N; i++)
				cloud.add(PointCloudShapeTools.createPt((Sphere3D_F64) shapeParam, rand.nextDouble() * 2 * Math.PI, rand.nextDouble() * 2 * Math.PI));
		} else {
			throw new IllegalArgumentException("Unknown shape");
		}
	}

	/**
	 * Find the total number of points in the cloud that are used.  The same point can be matched to multiple shapes
	 */
	private int findUsedCount(List<Point3D_F64> cloud, List<PointCloudShapeFinder.Shape> found) {
		int total = 0;

		for (Point3D_F64 p : cloud) {
			boolean matched = false;
			for (int i = 0; i < found.size() && !matched; i++) {
				for (Point3D_F64 c : found.get(i).points) {
					if (c == p) {
						matched = true;
						break;
					}
				}
			}
			if (matched)
				total++;
		}

		return total;
	}

	private double averageError(PointCloudShapeFinder.Shape shape) {
		DistanceFromModel function;
		switch (shape.type) {
			case CYLINDER:
				function = new DistanceCylinderToPoint3D();
				break;

			case PLANE:
				function = new DistancePlaneToPoint3D();
				break;

			case SPHERE:
				function = new DistanceSphereToPoint3D();
				break;
			default:
				throw new RuntimeException("Unknown shape");
		}

		function.setModel(shape.parameters);
		double total = 0;
		for (int i = 0; i < shape.points.size(); i++) {
			total += function.computeDistance(shape.points.get(i));
		}
		return total / shape.points.size();
	}

}
