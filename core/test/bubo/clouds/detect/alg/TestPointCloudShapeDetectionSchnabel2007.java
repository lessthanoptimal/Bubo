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

import bubo.clouds.detect.CloudShapeTypes;
import bubo.clouds.detect.shape.TestGeneratePlanePointVector;
import bubo.clouds.detect.shape.TestGenerateSpherePointVector;
import bubo.construct.ConstructOctreeNumPoints_F64;
import bubo.construct.Octree_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.shapes.Box3D_F64;
import georegression.struct.shapes.Sphere3D_F64;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestPointCloudShapeDetectionSchnabel2007 {

	Random rand = new Random(234234);

	// make sure the order is known
	CloudShapeTypes shapeDetect[] =
			new CloudShapeTypes[]{CloudShapeTypes.SPHERE, CloudShapeTypes.PLANE, CloudShapeTypes.CYLINDER};

	@Test
	public void perfectDataNoNoise_singleShape() {

		FastQueue<PointVectorNN> list = new FastQueue<PointVectorNN>(PointVectorNN.class, false);

		Sphere3D_F64 sphere = new Sphere3D_F64(1, 2, 3, 4);
		addSpherePoints(list, sphere, 400);

		PointCloudShapeDetectionSchnabel2007 alg = createAlgorithm();

		alg.process(list, new Box3D_F64(-100, -100, -100, 200, 200, 200));

		FastQueue<FoundShape> found = alg.getFoundObjects();

		assertEquals(1, found.size());
		assertEquals(0, found.get(0).whichShape);
		assertEquals(400, found.get(0).points.size());

		Sphere3D_F64 foundShape = (Sphere3D_F64) found.get(0).modelParam;

		assertEquals(sphere.radius, foundShape.radius, 1e-8);
		assertEquals(0, foundShape.center.distance(sphere.center), 1e-8);
	}

	@Test
	public void perfectDataNoNoise_multipleShapes() {
		FastQueue<PointVectorNN> list = new FastQueue<PointVectorNN>(PointVectorNN.class, false);

		Sphere3D_F64 sphere = new Sphere3D_F64(1, 2, 3, 4);
		addSpherePoints(list, sphere, 400);
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(-10, 10, 0, 0, 1, 1);
		addPlanePoints(list, plane, 400);

		PointCloudShapeDetectionSchnabel2007 alg = createAlgorithm();

		alg.process(list, new Box3D_F64(-100, -100, -100, 200, 200, 200));

		FastQueue<FoundShape> found = alg.getFoundObjects();

		assertEquals(2, found.size());

		int numSpheres = 0;
		int numPlanes = 0;

		for (int i = 0; i < found.size(); i++) {
			FoundShape shape = found.get(i);

			if (shape.whichShape == 0) {
				numSpheres++;

				assertEquals(400, found.get(i).points.size());

				Sphere3D_F64 foundShape = (Sphere3D_F64) found.get(i).modelParam;

				assertEquals(sphere.radius, foundShape.radius, 1e-8);
				assertEquals(0, foundShape.center.distance(sphere.center), 1e-8);

			} else if (shape.whichShape == 1) {
				numPlanes++;

				assertEquals(400, found.get(i).points.size());

				PlaneGeneral3D_F64 foundShape = (PlaneGeneral3D_F64) found.get(i).modelParam;

				TestGeneratePlanePointVector.checkPlanes(plane, foundShape, 1e-8);
			}
		}

		assertEquals(1, numSpheres);
		assertEquals(1, numPlanes);
	}

	@Test
	public void findLeafs() {
		PointCloudShapeDetectionSchnabel2007 alg = new PointCloudShapeDetectionSchnabel2007();

		ConstructOctreeNumPoints_F64 tree = alg.managerOctree = new ConstructOctreeNumPoints_F64(10);

		Octree_F64 prev = tree.getTree();

		for (int i = 0; i < 10; i++) {
			Octree_F64 o1 = tree.getAllNodes().grow();
			o1.parent = prev;
			Octree_F64 o2 = tree.getAllNodes().grow();
			o2.parent = prev;

			prev.children = new Octree_F64[8];
			prev.children[0] = o1;
			prev.children[1] = o2;

			prev = o1;
		}

		alg.findLeafs();

		FastQueue<Octree_F64> leafs = alg.getLeafs();
		assertEquals(10 + 1, leafs.size);
	}

	private void addSpherePoints(FastQueue<PointVectorNN> list, Sphere3D_F64 sphere, int N) {
		for (int i = 0; i < N; i++) {
			double phi = 2.0 * rand.nextDouble() * Math.PI;
			double theta = 2.0 * rand.nextDouble() * Math.PI;

			PointVectorNN p = TestGenerateSpherePointVector.createPt(sphere, phi, theta, 1);

			list.add(p);

			if (list.size() > 1) {
				PointVectorNN prev = list.get(list.size() - 2);
				prev.neighbors.add(p);
				p.neighbors.add(prev);
			}
		}
	}

	private void addPlanePoints(FastQueue<PointVectorNN> list, PlaneNormal3D_F64 plane, int N) {

		for (int i = 0; i < N; i++) {
			double x = 3.0 * (rand.nextDouble() - 0.5);
			double y = 3.0 * (rand.nextDouble() - 0.5);

			PointVectorNN p = TestGeneratePlanePointVector.createPt(plane, x, y, 1);

			list.add(p);

			if (list.size() > 1) {
				PointVectorNN prev = list.get(list.size() - 2);
				prev.neighbors.add(p);
				p.neighbors.add(prev);
			}
		}
	}

	private PointCloudShapeDetectionSchnabel2007 createAlgorithm() {

		ConfigSchnabel2007 config = ConfigSchnabel2007.createDefault(100, 0.2, 0.2, shapeDetect);

		config.minModelAccept = 10;
		config.octreeSplit = 20;

		return new PointCloudShapeDetectionSchnabel2007(config);
	}
}
