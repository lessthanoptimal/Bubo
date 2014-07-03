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

package bubo.clouds.detect.tools;

import bubo.clouds.detect.shape.DistanceCylinderToPoint3D;
import bubo.clouds.detect.shape.DistancePlaneToPoint3D;
import bubo.clouds.detect.shape.DistanceSphereToPoint3D;
import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import georegression.struct.shapes.Sphere3D_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestPointCloudShapeTools {

	Random rand = new Random(234);

	@Test
	public void createPt_sphere() {
		Sphere3D_F64 sphere = new Sphere3D_F64(2, -3, 4, 4.5);

		DistanceSphereToPoint3D distance = new DistanceSphereToPoint3D();
		distance.setModel(sphere);

		for (int i = 0; i < 50; i++) {
			double phi = 2 * Math.PI * rand.nextDouble();
			double theta = 2 * Math.PI * rand.nextDouble();

			Point3D_F64 p = PointCloudShapeTools.createPt(sphere, phi, theta);

			double d = distance.computeDistance(p);
			assertEquals(0, d, 1e-8);
		}
	}

	@Test
	public void createPt_plane() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(2, -3, 4, 4.5, 2, 3);

		DistancePlaneToPoint3D distance = new DistancePlaneToPoint3D();
		distance.setModel(UtilPlane3D_F64.convert(plane, null));

		for (int i = 0; i < 50; i++) {
			double x = 2 * (rand.nextDouble() - 0.5);
			double y = 2 * (rand.nextDouble() - 0.5);

			Point3D_F64 p = PointCloudShapeTools.createPt(plane, x, y);

			double d = distance.computeDistance(p);
			assertEquals(0, d, 1e-8);
		}
	}

	@Test
	public void createPt_cylinder() {
		Cylinder3D_F64 cylinder = new Cylinder3D_F64(2, -3, 4, 4.5, 2, -1.3, 3);

		DistanceCylinderToPoint3D distance = new DistanceCylinderToPoint3D();
		distance.setModel(cylinder);

		for (int i = 0; i < 50; i++) {
			double z = 2 * (rand.nextDouble() - 0.5);
			double theta = 2 * Math.PI * rand.nextDouble();

			Point3D_F64 p = PointCloudShapeTools.createPt(cylinder, z, theta);

			double d = distance.computeDistance(p);
			assertEquals(0, d, 1e-8);
		}
	}

}
