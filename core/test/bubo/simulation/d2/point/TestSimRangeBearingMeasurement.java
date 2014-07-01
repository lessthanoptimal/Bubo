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

package bubo.simulation.d2.point;

import bubo.simulation.d2.sensors.SimRangeBearingMeasurement;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestSimRangeBearingMeasurement {

	@Test
	public void simpleTest() {
		SimRangeBearingMeasurement alg = new SimRangeBearingMeasurement(0.5, 0.1, 20, 234234);

		alg.setSensorPose(new Se2_F64(1, 2, Math.PI / 2.0));
		assertTrue(alg.process(new Point2D_F64(1, 5)));

		assertEquals(0, alg.getTrueBearing(), 1e-8);
		assertEquals(3, alg.getTrueRange(), 1e-8);

		assertTrue(alg.getTrueBearing() != alg.getNoisyBearing());
		assertTrue(alg.getTrueRange() != alg.getNoisyRange());

		assertEquals(0, alg.getNoisyBearing(), 2);
		assertEquals(3, alg.getNoisyRange(), 1);
	}

	@Test
	public void checkMaxRange() {
		SimRangeBearingMeasurement alg = new SimRangeBearingMeasurement(0.5, 0.1, 20, 234234);

		alg.setSensorPose(new Se2_F64(1, 2, Math.PI / 2.0));

		// test within the range
		assertTrue(alg.process(new Point2D_F64(1, 5)));

		// test out of range
		assertFalse(alg.process(new Point2D_F64(1, 50)));
	}
}
