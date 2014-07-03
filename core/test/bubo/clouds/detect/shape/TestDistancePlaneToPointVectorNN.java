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

import bubo.clouds.detect.alg.PointVectorNN;
import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestDistancePlaneToPointVectorNN {

	@Test
	public void computeDistance() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 1, 1, 0, 0, 1);
		PlaneGeneral3D_F64 model = UtilPlane3D_F64.convert(plane, null);
		PointVectorNN point;

		DistancePlaneToPointVectorNN alg = new DistancePlaneToPointVectorNN(0.2);
		alg.setModel(model);

		// test above the plane
		point = new PointVectorNN(1, 1, 2, 0, 0, 1);

		assertEquals(1, alg.computeDistance(point), 1e-8);

		// test below the plane
		point = new PointVectorNN(1, 1, 0, 0, 0, -1);

		assertEquals(1, alg.computeDistance(point), 1e-8);
	}

	@Test
	public void computeDistance_angleCheck() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 1, 1, 0, 0, 1);
		PlaneGeneral3D_F64 model = UtilPlane3D_F64.convert(plane, null);
		PointVectorNN point;

		double angleTol = 0.2;

		DistancePlaneToPointVectorNN alg = new DistancePlaneToPointVectorNN(angleTol);
		alg.setModel(model);

		// have it just inside the tolerance
		double angle = angleTol - 0.01;

		point = new PointVectorNN(1, 1, 2, 0, Math.sin(angle), Math.cos(angle));
		assertEquals(1, alg.computeDistance(point), 1e-8);

		// have it outside the tolerance
		angle = angleTol + 0.01;

		point = new PointVectorNN(1, 1, 2, 0, Math.sin(angle), Math.cos(angle));
		assertTrue(Double.MAX_VALUE == alg.computeDistance(point));
	}

	@Test
	public void computeDistance_list() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 1, 1, 0, 0, 1);
		PlaneGeneral3D_F64 model = UtilPlane3D_F64.convert(plane, null);

		DistancePlaneToPointVectorNN alg = new DistancePlaneToPointVectorNN(0.2);
		alg.setModel(model);

		PointVectorNN pointA = new PointVectorNN(1, 1, 2, 0, 0, 1);
		PointVectorNN pointB = new PointVectorNN(1, 1, 0, 0, 0, -1);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
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