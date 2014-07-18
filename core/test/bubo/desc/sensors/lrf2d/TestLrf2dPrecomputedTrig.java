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

package bubo.desc.sensors.lrf2d;

import georegression.struct.point.Point2D_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestLrf2dPrecomputedTrig {

	@Test
	public void computeEndPoint_internal() {
		Lrf2dParam param = createParam();

		Lrf2dPrecomputedTrig alg = new Lrf2dPrecomputedTrig(param);

		double r = 25.0;
		alg.computeEndPoint(5, r);

		double foundR = Math.sqrt(alg.x * alg.x + alg.y * alg.y);
		double foundYaw = Math.atan2(alg.y, alg.x);

		assertEquals(r, foundR, 1e-8);
		assertEquals(foundYaw, -Math.PI + 5.0 / 99.0 * Math.PI, 1e-8);
	}

	@Test
	public void computeEndPoint_point() {
		Lrf2dParam param = createParam();

		Lrf2dPrecomputedTrig alg = new Lrf2dPrecomputedTrig(param);

		Point2D_F64 pt = new Point2D_F64();

		double r = 25.0;
		alg.computeEndPoint(5, r, pt);

		double foundR = Math.sqrt(pt.x * pt.x + pt.y * pt.y);
		double foundYaw = Math.atan2(pt.y, pt.x);

		assertEquals(r, foundR, 1e-8);
		assertEquals(foundYaw, -Math.PI + 5.0 / 99.0 * Math.PI, 1e-8);
	}

	private Lrf2dParam createParam() {
		Lrf2dParam param = new Lrf2dParam();

		param.setNumberOfScans(100);
		param.setSweepAngle(Math.PI);
		param.setStartAngle(-Math.PI);

		return param;
	}
}
