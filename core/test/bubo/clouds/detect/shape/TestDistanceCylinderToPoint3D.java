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

import georegression.metric.Distance3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestDistanceCylinderToPoint3D {

	@Test
	public void computeDistance() {

		DistanceCylinderToPoint3D alg = new DistanceCylinderToPoint3D();

		Cylinder3D_F64 model = new Cylinder3D_F64(1, -2, 3, -0.5, 0.2, 2, 3);
		Point3D_F64 point = new Point3D_F64(3.4, 5.6, 7.8);

		alg.setModel(model);

		double expected = Math.abs(Distance3D_F64.distance(model, point));
		double found = alg.computeDistance(point);

		assertTrue(found > 0);
		assertEquals(expected, found, 1e-8);

		// now try a point inside the cylinder
		point.set(1, -2, 3.5);
		alg.setModel(model);

		expected = Math.abs(Distance3D_F64.distance(model, point));
		found = alg.computeDistance(point);

		assertTrue(found > 0);
		assertEquals(expected, found, 1e-8);
	}

	@Test
	public void computeDistance_array() {
		DistanceCylinderToPoint3D alg = new DistanceCylinderToPoint3D();

		Cylinder3D_F64 model = new Cylinder3D_F64(1, -2, 3, -0.5, 0.2, 2, 3);

		Point3D_F64 pointA = new Point3D_F64(3.4, 5.6, 7.8);
		Point3D_F64 pointB = new Point3D_F64(1, -2, 3.5);

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
