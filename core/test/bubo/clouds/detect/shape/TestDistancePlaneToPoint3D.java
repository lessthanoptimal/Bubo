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

package bubo.clouds.detect.shape;

import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestDistancePlaneToPoint3D {

	@Test
	public void computeDistance() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 1, 1, 0, 0, 1);
		PlaneGeneral3D_F64 model = UtilPlane3D_F64.convert(plane, null);
		Point3D_F64 point;

		DistancePlaneToPoint3D alg = new DistancePlaneToPoint3D();
		alg.setModel(model);

		// test above the plane
		point = new Point3D_F64(1, 1, 2);

		assertEquals(1, alg.computeDistance(point), 1e-8);

		// test below the plane
		point = new Point3D_F64(1, 1, 0);

		assertEquals(1, alg.computeDistance(point), 1e-8);
	}

	@Test
	public void computeDistance_list() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 1, 1, 0, 0, 1);
		PlaneGeneral3D_F64 model = UtilPlane3D_F64.convert(plane, null);

		DistancePlaneToPoint3D alg = new DistancePlaneToPoint3D();
		alg.setModel(model);

		Point3D_F64 pointA = new Point3D_F64(1, 1, 2);
		Point3D_F64 pointB = new Point3D_F64(1, 1, 0);

		List<Point3D_F64> pts = new ArrayList<Point3D_F64>();
		pts.add(pointA);
		pts.add(pointB);

		double scores[] = new double[2];

		alg.setModel(model);
		alg.computeDistance(pts, scores);
		for (int i = 0; i < 2; i++) {
			double expected = alg.computeDistance(pts.get(i));
			assertEquals(expected, scores[i], 1e-8);
		}
	}

}
