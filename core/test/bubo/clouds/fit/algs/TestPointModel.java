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

package bubo.clouds.fit.algs;

import georegression.geometry.UtilPoint3D_F64;
import georegression.struct.point.Point3D_F64;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestPointModel {

	Random rand = new Random(234234);

	/**
	 * Sees if it can find a point which perfectly matches the one in the model
	 */
	@Test
	public void testIdentical() {
		List<Point3D_F64> pts = UtilPoint3D_F64.random(-10, 10, 30, rand);

		PointModel<Point3D_F64> model = new PointModel<Point3D_F64>(pts);

		Point3D_F64 found = model.findClosestPoint(pts.get(2).copy());

		assertTrue(found == pts.get(2));
	}

	/**
	 * Add a bit of noise and see if it finds the best match.
	 */
	@Test
	public void testSlightError() {
		List<Point3D_F64> pts = UtilPoint3D_F64.random(-10, 10, 30, rand);
		List<Point3D_F64> noisy = UtilPoint3D_F64.copy(pts);
		UtilPoint3D_F64.noiseNormal(noisy, 0.1, rand);

		PointModel<Point3D_F64> model = new PointModel<Point3D_F64>(noisy);

		Point3D_F64 found = model.findClosestPoint(pts.get(2));

		int index = noisy.indexOf(found);

		assertEquals(index, 2);
	}
}
