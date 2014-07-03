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
import georegression.metric.Distance3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestDistanceCylinderToPointVectorNN {

	@Test
	public void computeDistance() {

		DistanceCylinderToPointVectorNN alg = new DistanceCylinderToPointVectorNN(Math.PI / 2.0);

		Cylinder3D_F64 model = new Cylinder3D_F64(1, -2, 3, -0.5, 0.2, 2, 3);
		model.line.slope.normalize();
		PointVectorNN point = new PointVectorNN(3.4, 5.6, 7.8, 0, 0, 1);

		alg.setModel(model);

		double expected = Math.abs(Distance3D_F64.distance(model, point.p));
		double found = alg.computeDistance(point);

		assertTrue(found > 0);
		assertEquals(expected, found, 1e-8);

		// now try a point inside the cylinder
		point = new PointVectorNN(1, -2, 3.5, 0, 0, 1);
		alg.setModel(model);

		expected = Math.abs(Distance3D_F64.distance(model, point.p));
		found = alg.computeDistance(point);

		assertTrue(found > 0);
		assertEquals(expected, found, 1e-8);
	}

	@Test
	public void computeDistance_angleCheck() {

		double angleTol = 0.2;

		DistanceCylinderToPointVectorNN alg = new DistanceCylinderToPointVectorNN(angleTol);

		Cylinder3D_F64 model = new Cylinder3D_F64(0, 0, 0, 0, 0, 1, 2);
		alg.setModel(model);

		// have it just inside the tolerance
		double angle = angleTol - 0.01;
		PointVectorNN point = new PointVectorNN(0, 3, 0, 0, Math.cos(angle), Math.sin(angle));
		assertEquals(1, alg.computeDistance(point), 1e-8);

		// just outside
		angle = angleTol + 0.01;
		point = new PointVectorNN(0, 3, 0, 0, Math.cos(angle), Math.sin(angle));
		assertTrue(Double.MAX_VALUE == alg.computeDistance(point));
	}

	@Test
	public void computeDistance_array() {
		DistanceCylinderToPointVectorNN alg = new DistanceCylinderToPointVectorNN(Math.PI / 2.0);

		Cylinder3D_F64 model = new Cylinder3D_F64(1, -2, 3, -0.5, 0.2, 2, 3);
		model.line.slope.normalize();

		PointVectorNN pointA = new PointVectorNN(3.4, 5.6, 7.8, 0, 0, 1);
		PointVectorNN pointB = new PointVectorNN(1, -2, 3.5, 0, 0, 1);

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
