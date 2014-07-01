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

package bubo.simulation.d2.motion;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestFixedTrajectoryVel2D {

	/**
	 * Makes sure the true control and noised control are different
	 */
	@Test
	public void checkControlNoise() {
		FixedTrajectoryVel2D alg = new FixedTrajectoryVel2D(1, 2, 0.1, 0.2, 1, 3453453);

		alg.drawControlNoise();

		double[] request = alg.getControlRequested();
		double[] actual = alg.getControlTruth();

		assertEquals(actual[0], 1, 1e-8);
		assertEquals(actual[1], 2, 1e-8);

		// they should not be the same
		assertTrue(actual[0] != request[0]);
		assertTrue(actual[1] != request[1]);

		// make sure only a bit of noise was added
		assertEquals(request[0], 1, 0.5);
		assertEquals(request[1], 2, 1);
	}
}
