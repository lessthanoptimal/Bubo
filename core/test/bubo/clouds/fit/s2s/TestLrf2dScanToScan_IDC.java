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

package bubo.clouds.fit.s2s;

import bubo.clouds.fit.Lrf2dScanToScan;
import bubo.struct.StoppingCondition;
import georegression.metric.UtilAngle;
import org.junit.Test;

import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestLrf2dScanToScan_IDC extends StandardTestsScanToScan {
	public TestLrf2dScanToScan_IDC() {
		angTol = 0.02;
		tranTol = 0.02;
	}

	@Override
	public Lrf2dScanToScan createAlg() {
		StoppingCondition stop = new StoppingCondition(20, 0.0001);

		double searchRadius = UtilAngle.degreeToRadian(20);
		return new Lrf2dScanToScan_IDC(stop, searchRadius, 0.2, 0.1);
	}

	// todo implement generic interpolation test where it sees if it performs perfectly on original points
	@Test
	public void interpolateDistance() {
		fail("implement");
	}

	@Test
	public void interpolateRange() {
		fail("implement");
	}
}
