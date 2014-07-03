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

package bubo.clouds.fit.c2c;

import bubo.clouds.FactoryFitting;
import bubo.clouds.fit.MatchCloudToCloud;
import bubo.struct.StoppingCondition;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.se.Se3_F64;
import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestMatchCloudToCloudIcp {

	@Test
	public void check2D() {
		new GeneralCloudToCloudChecksSe2() {

			@Override
			public MatchCloudToCloud<Se2_F64, Point2D_F64> create() {
				return FactoryFitting.cloudIcp2D(0.1,new StoppingCondition(200,1e-6));
			}
		}.all();
	}

	@Test
	public void check3D() {
		new GeneralCloudToCloudChecksSe3() {

			@Override
			public MatchCloudToCloud<Se3_F64, Point3D_F64> create() {
				return FactoryFitting.cloudIcp3D(0.1, new StoppingCondition(200, 1e-6));
			}
		}.all();
	}

}
