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

package bubo.simulation.d2.sensors;

import bubo.desc.sensors.lrf2d.Lrf2dMeasurement;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.maps.d2.lines.LineSegmentMap;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.se.Se2_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSimulateLrf2D {

	/**
	 * Put a single wall in front of the sensor and see if it measures it correctly
	 */
	@Test
	public void basic() {

		Lrf2dParam param = new Lrf2dParam(null,Math.PI/2.0,-Math.PI,180,10,0,0);

		LineSegmentMap world = new LineSegmentMap();
		world.lines.add( new LineSegment2D_F64(2,-1,2,1));

		SimulateLrf2D alg = new SimulateLrf2D(param);
		alg.update(new Se2_F64(),world);

		Lrf2dMeasurement meas = alg.getMeasurement();

		// compute the solid angle for the wall plus a fudge factor
		double theta = Math.atan2(1,2);
		double thetaIn = Math.atan2(1,2)+param.getAngleIncrement();

		for (int i = 0; i < meas.numMeas; i++) {
			double angle = param.computeAngle(i);
			if( angle <= -theta || angle >= theta) {
				assertTrue(meas.meas[i] >= param.getMaxRange()-0.1);
			} if( angle > -thetaIn && angle < thetaIn) {
				double y = 2*Math.tan(angle);
				double r = Math.sqrt(2*2 + y*y);
				assertEquals(r,meas.meas[i],1e-8);
			}
		}
	}

	/**
	 * Makes sure its apply the coordinate transform correctly
	 */
	@Test
	public void coordinateSystem() {
		Lrf2dParam param = new Lrf2dParam(null,Math.PI/2.0,-Math.PI,180,10,0,0);

		LineSegmentMap world = new LineSegmentMap();
		world.lines.add( new LineSegment2D_F64(-1,2,1,2));

		SimulateLrf2D alg = new SimulateLrf2D(param);
		Se2_F64 sensorToWorld = new Se2_F64(0,0,Math.PI/2.0);
		alg.update(sensorToWorld,world);

		Lrf2dMeasurement meas = alg.getMeasurement();
		assertEquals(2, meas.meas[meas.numMeas / 2], 0.001);// won't be perfectly infront
	}
}