/*
 * Copyright (c) 2013, Peter Abeles. All Rights Reserved.
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

package bubo.ptcloud;

import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import georegression.struct.shapes.Sphere3D_F64;
import org.ddogleg.fitting.modelset.ransac.RansacMulti;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestPointCloudShapeDetectionSchnabel2007 {

	Random rand = new Random(234234);

	@Test
	public void perfectDataNoNoise_singleShape() {

		FastQueue<PointVectorNN> list = new FastQueue<PointVectorNN>(PointVectorNN.class,false);

		Sphere3D_F64 sphere = new Sphere3D_F64(1,2,3,4);
		addSpherePoints(list, sphere,400);

		PointCloudShapeDetectionSchnabel2007 alg = createAlgorithm();

		alg.process(list);

		FastQueue<FoundShape> found = alg.getFoundObjects();

		assertEquals(1,found.size());
		assertEquals(0,found.get(0).whichShape);
		assertEquals(400,found.get(0).points.size());

		Sphere3D_F64 foundShape = (Sphere3D_F64)found.get(0).modelParam;

		assertEquals(sphere.radius,foundShape.radius,1e-8);
		assertEquals(0,foundShape.center.distance(sphere.center),1e-8);
	}

	@Test
	public void perfectDataNoNoise_multipleShapes() {
		FastQueue<PointVectorNN> list = new FastQueue<PointVectorNN>(PointVectorNN.class,false);

		Sphere3D_F64 sphere = new Sphere3D_F64(1,2,3,4);
		addSpherePoints(list, sphere, 400);
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(-10,10,0,0,1,1);
		addPlanePoints(list, plane, 400);

		PointCloudShapeDetectionSchnabel2007 alg = createAlgorithm();

		alg.process(list);

		FastQueue<FoundShape> found = alg.getFoundObjects();

		assertEquals(2,found.size());

		int numSpheres = 0;
		int numPlanes = 0;

		for( int i = 0; i < found.size(); i++ ) {
			FoundShape shape = found.get(i);

			if( shape.whichShape == 0 ) {
				numSpheres++;

				assertEquals(400,found.get(i).points.size());

				Sphere3D_F64 foundShape = (Sphere3D_F64)found.get(i).modelParam;

				assertEquals(sphere.radius,foundShape.radius,1e-8);
				assertEquals(0,foundShape.center.distance(sphere.center),1e-8);

			} else if( shape.whichShape == 1 ) {
		        numPlanes++;

				assertEquals(400,found.get(i).points.size());

				PlaneGeneral3D_F64 foundShape = (PlaneGeneral3D_F64)found.get(i).modelParam;

				// TODO finish
			}
		}

		assertEquals(1,numSpheres);
		assertEquals(1,numPlanes);

	}


	@Test
	public void selectSampleNode() {
		// make sure its a random sampling of leafs

		// check the path it searches

		fail("implement");
	}

	@Test
	public void findLeafs() {
		PointCloudShapeDetectionSchnabel2007 alg = new PointCloudShapeDetectionSchnabel2007();

		ConstructOctreeEqual tree = alg.managerOctree = new ConstructOctreeEqual(10);

		Octree prev = tree.getTree();

		for( int i = 0; i < 10; i++ ) {
			Octree o1 = tree.storageNodes.grow();
			o1.parent = prev;
			Octree o2 = tree.storageNodes.grow();
			o2.parent = prev;

			prev.children = new Octree[8];
			prev.children[0] = o1;
			prev.children[1] = o2;

			prev = o1;
		}

		alg.findLeafs();

		FastQueue<Octree> leafs = alg.getLeafs();
		assertEquals(10+1,leafs.size);
	}

	@Test
	public void computingBoundingCube() {
		FastQueue<PointVectorNN> list = new FastQueue<PointVectorNN>(PointVectorNN.class,false);

		list.add(new PointVectorNN());
		list.add(new PointVectorNN());
		list.add(new PointVectorNN());

		list.get(0).p = new Point3D_F64(1,1,1);
		list.get(1).p = new Point3D_F64(2,3,1);
		list.get(2).p = new Point3D_F64(1.5,2,5);


		PointCloudShapeDetectionSchnabel2007 alg = new PointCloudShapeDetectionSchnabel2007();

		alg.computingBoundingCube(list);

		Cube3D_F64 cube = alg.getBounding();

		assertEquals(0,cube.getP().distance(list.get(0).p),1e-8);
		assertEquals(1,cube.lengthX,1e-8);
		assertEquals(2,cube.lengthY,1e-8);
		assertEquals(4,cube.lengthZ,1e-8);
	}

	private void addSpherePoints(FastQueue<PointVectorNN> list, Sphere3D_F64 sphere, int N ) {
		for( int i = 0; i < N; i++ ) {
			double phi = 2.0*rand.nextDouble()*Math.PI;
			double theta = 2.0*rand.nextDouble()*Math.PI;

			PointVectorNN p = TestGenerateSpherePointVector.createPt(sphere, phi, theta, 1);

			list.add(p);

			if( list.size() > 1 ) {
				PointVectorNN prev = list.get(list.size()-2);
				prev.neighbors.add(p);
				p.neighbors.add(prev);
			}
		}
	}

	private void addPlanePoints(FastQueue<PointVectorNN> list, PlaneNormal3D_F64 plane, int N ) {

		for( int i = 0; i < N; i++ ) {
			double x = 3.0*(rand.nextDouble()-0.5);
			double y = 3.0*(rand.nextDouble()-0.5);

			PointVectorNN p = TestGeneratePlanePointVector.createPt(plane, x, y, 1);

			list.add(p);

			if( list.size() > 1 ) {
				PointVectorNN prev = list.get(list.size()-2);
				prev.neighbors.add(p);
				p.neighbors.add(prev);
			}
		}
	}

	private PointCloudShapeDetectionSchnabel2007 createAlgorithm() {

		List<RansacMulti.ObjectType> objects = new ArrayList<RansacMulti.ObjectType>();

		RansacMulti.ObjectType sphere = new RansacMulti.ObjectType();
		sphere.modelDistance = new DistanceFromSpherePointVector();
		sphere.modelGenerator = new GenerateSpherePointVector(0.3,0.3);
		sphere.thresholdFit = 0.3;

		RansacMulti.ObjectType plane = new RansacMulti.ObjectType();
		plane.modelDistance = new DistanceFromPlanePointVector();
		plane.modelGenerator = new GeneratePlanePointVector(0.3);
		plane.thresholdFit = 0.3;

		RansacMulti.ObjectType cylinder = new RansacMulti.ObjectType();
		cylinder.modelDistance = new DistanceFromCylinderPointVector();
		cylinder.modelGenerator = new GenerateCylinderPointVector(0.3,0.3);
		cylinder.thresholdFit = 0.3;

		objects.add(sphere);
		objects.add(plane);
		objects.add(cylinder);

		return new PointCloudShapeDetectionSchnabel2007(objects,20,10,10,1000,0xBEEF);
	}
}
