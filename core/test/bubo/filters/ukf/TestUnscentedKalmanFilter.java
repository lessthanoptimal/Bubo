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

package bubo.filters.ukf;

import org.junit.Test;

public class TestUnscentedKalmanFilter {
	/**
	 * Configure the filter so that it performs the transform and is equivalent to a kalman filter
	 */
	@Test
	public void standardKalman() {
		UkfGenericKalmanTests generic = new UkfGenericKalmanTests();
		generic.performAllGenericTests();
	}

	/**
	 * Given a simple linear system, see if it produces the same output as a Kalman filter.
	 * <p/>
	 * Also tests to see if both postive and negative kappas are handled correctly
	 */
	@Test
	public void compareToKalman() {
		UkfCompareToKalmanTests generic = new UkfCompareToKalmanTests(1e-4, 1e-2, 0.1);

		generic.compareToKalman();

		generic = new UkfCompareToKalmanTests(1e-4, 1e-2, -0.1);

		generic.compareToKalman();
	}
}
