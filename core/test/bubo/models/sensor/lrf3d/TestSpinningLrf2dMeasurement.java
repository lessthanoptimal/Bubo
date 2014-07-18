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

package bubo.models.sensor.lrf3d;

import bubo.desc.sensors.lrf3d.SpinningLrf2dMeasurement;
import georegression.struct.se.Se3_F64;
import georegression.struct.se.SpecialEuclideanOps_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestSpinningLrf2dMeasurement {
	@Test
	public void interpolate() {

		// really simple case to interpolate.  Not testing interpolation, but ability compute 't'
		Se3_F64 start = SpecialEuclideanOps_F64.setEulerXYZ(0,0,0,  4,0,0,null);
		Se3_F64 end = SpecialEuclideanOps_F64.setEulerXYZ(0,0,0,  8,0,0,null);

		SpinningLrf2dMeasurement meas = new SpinningLrf2dMeasurement(125);
		meas.setTransforms(start,end);

		Se3_F64 found = new Se3_F64();
		for (int i = 0; i < 125; i++) {
			double t = i/(double)124;

			meas.interpolate(i,found);
			assertEquals(4 + t * 4, found.getX(), 1e-8);
			assertEquals(0,found.getY(),1e-8);
			assertEquals(0,found.getZ(),1e-8);
		}
	}
}