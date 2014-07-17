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

package bubo.validation.clouds.motion;

import bubo.clouds.fit.Lrf2dScanToScan;
import bubo.clouds.fit.s2s.Lrf2dScanToScan_LocalICP;
import bubo.clouds.motion.Lrf2dMotionRollingKeyFrame;
import bubo.struct.StoppingCondition;

/**
 * @author Peter Abeles
 */
public class FactoryEvaluateScanMotion {

	public static Lrf2dMotionRollingKeyFrame createIcpLocal() {
		Lrf2dScanToScan scanMatching = new Lrf2dScanToScan_LocalICP(new StoppingCondition(20, 0.0001), 200, 0.20);

		return new Lrf2dMotionRollingKeyFrame(scanMatching,100);
	}
}
