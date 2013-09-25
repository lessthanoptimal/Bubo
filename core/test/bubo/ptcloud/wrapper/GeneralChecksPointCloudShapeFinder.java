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

package bubo.ptcloud.wrapper;

import bubo.ptcloud.CloudShapeTypes;
import bubo.ptcloud.PointCloudShapeFinder;
import bubo.ptcloud.PointCloudShapeTools;
import bubo.ptcloud.alg.TestGenerateCylinderPointVector;
import bubo.ptcloud.alg.TestGeneratePlanePointVector;
import bubo.ptcloud.alg.TestGenerateSpherePointVector;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import georegression.struct.shapes.Sphere3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * General tests for implementations of {@link bubo.ptcloud.PointCloudShapeFinder}
 *
 * @author Peter Abeles
 */
public abstract class GeneralChecksPointCloudShapeFinder {

	Random rand = new Random(234);

	public abstract PointCloudShapeFinder createAlgorithm();


	/**
	 * Goes through each supported shape and sees if it can detect it by itsself with perfect data
	 * and no noise
	 */
	@Test
	public void detectShapesSingle() {

		PointCloudShapeFinder alg = createAlgorithm();

		List<CloudShapeTypes> shapes = alg.getShapesList();

		assertTrue(shapes.size()>1);

		int N = 200;

		Cylinder3D_F64 cylinder = new Cylinder3D_F64(1,2,3,0.5,-0.25,0.1,3);
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1,2,3,-0.5,0.25,1);
		Sphere3D_F64 sphere = new Sphere3D_F64(-1,-2,-3,2.5);

		for( CloudShapeTypes shape : shapes ) {
			List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

			switch( shape ) {
				case CYLINDER:
					for( int i = 0; i < N; i++ )
						cloud.add(PointCloudShapeTools.createPt(cylinder, rand.nextDouble() * 2, rand.nextDouble() * 2 * Math.PI));
					break;

				case PLANE:
					for( int i = 0; i < N; i++ )
						cloud.add(PointCloudShapeTools.createPt(plane, 3 * (rand.nextDouble() - 0.5), 3 * (rand.nextDouble() - 0.5)));
					break;

				case SPHERE:
					for( int i = 0; i < N; i++ )
						cloud.add(PointCloudShapeTools.createPt(sphere, rand.nextDouble() * 2 * Math.PI, rand.nextDouble() * 2 * Math.PI));
					break;

				default:
					throw new RuntimeException("Unknown type");
			}

			alg.process(cloud,null);

			List<Point3D_F64> unmatched = new ArrayList<Point3D_F64>();
			alg.getUnmatched(unmatched);

			List<PointCloudShapeFinder.Shape> found = alg.getFound();
//			assertEquals(1,found.size());
			PointCloudShapeFinder.Shape s = found.get(0);


			assertEquals(200,unmatched.size()+findUsedCount(cloud,found));
			assertEquals(0,unmatched.size());
			assertEquals(200,s.points.size());

			switch( shape ) {
				case CYLINDER:
					TestGenerateCylinderPointVector.checkEquivalent(cylinder,(Cylinder3D_F64)s.parameters);
					break;

				case PLANE:
					TestGeneratePlanePointVector.checkPlanes(plane, (PlaneGeneral3D_F64) s.parameters);
					break;

				case SPHERE:
					TestGenerateSpherePointVector.checkSpheres(sphere, (Sphere3D_F64) s.parameters);
					break;

			}
		}
	}

	/**
	 * Have two shapes overlap each other and see if everything is handled correctly
	 */
	@Test
	public void planeThroughSphere() {
		// first see if the detector supports plane and spheres

//		fail("implement");
	}

//	@Test
//	public void detectShapesMultiple() {
//		fail("Implement");
//	}
//
//	@Test
//	public void detectShapesNoise() {
//		fail("Implement");
//	}
//
//	/**
//	 * Make sure it handle the case with and without the bounding cube being specified
//	 */
//	@Test
//	public void checkBoundingCube() {
//		fail("Implement");
//	}

	/**
	 * Find the total number of points in the cloud that are used.  The same point can be matched to multiple shapes
	 */
	private int findUsedCount( List<Point3D_F64> cloud , List<PointCloudShapeFinder.Shape> found) {
		int total = 0;

		for( Point3D_F64 p : cloud ) {
			boolean matched = false;
			for( int i = 0; i < found.size() && !matched; i++ ) {
				for( Point3D_F64 c : found.get(i).points ) {
					if( c == p ) {
						matched = true;
						break;
					}
				}
			}
			if( matched )
				total++;
		}

		return total;
	}

}
