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
import georegression.struct.shapes.Sphere3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestDistanceSphereToPointVectorNN {

	@Test
	public void computeDistance() {
		Sphere3D_F64 model = new Sphere3D_F64(1, 1, 1, 3);

		DistanceSphereToPointVectorNN alg = new DistanceSphereToPointVectorNN(0.2);
		alg.setModel(model);

		// test outside the sphere
		PointVectorNN point = new PointVectorNN(1, 1, 1 + 4, 0, 0, -1);

		assertEquals(1, alg.computeDistance(point), 1e-8);

		// test inside the sphere
		point = new PointVectorNN(1, 1, 1 + 2, 0, 0, 1);

		assertEquals(1, alg.computeDistance(point), 1e-8);
	}

	@Test
	public void computeDistance_angleCheck() {
		Sphere3D_F64 model = new Sphere3D_F64(1, 1, 1, 3);

		double angleTol = 0.2;

		DistanceSphereToPointVectorNN alg = new DistanceSphereToPointVectorNN(angleTol);
		alg.setModel(model);

		// have it just inside the tolerance
		double angle = angleTol - 0.01;
		double x = model.center.x;
		double y = model.center.y + Math.sin(angle) * 4;
		double z = model.center.z + Math.cos(angle) * 4;

		PointVectorNN point = new PointVectorNN(x, y, z, 0, 0, 1);

		assertEquals(1, alg.computeDistance(point), 1e-8);

		// now outside
		angle = angleTol + 0.01;
		x = model.center.x;
		y = model.center.y + Math.sin(angle) * 4;
		z = model.center.z + Math.cos(angle) * 4;

		point = new PointVectorNN(x, y, z, 0, 0, 1);

		assertTrue(Double.MAX_VALUE == alg.computeDistance(point));
	}

	@Test
	public void computeDistance_list() {
		Sphere3D_F64 model = new Sphere3D_F64(1, 1, 1, 3);

		DistanceSphereToPointVectorNN alg = new DistanceSphereToPointVectorNN(0.2);
		alg.setModel(model);

		PointVectorNN pointA = new PointVectorNN(1, 1, 1 + 4, 0, 0, 1);
		PointVectorNN pointB = new PointVectorNN(1, 1, 1 + 2, 0, 0, -1);

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
