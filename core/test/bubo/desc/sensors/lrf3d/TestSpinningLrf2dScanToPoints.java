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

package bubo.desc.sensors.lrf3d;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import georegression.struct.point.Point3D_F64;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestSpinningLrf2dScanToPoints {

	/**
	 * Rotation angle of 0.  lrfToArm is identity.
	 */
	@Test
	public void rotationAngleOf0() {

		double r = 2;
		Lrf2dParam param2d = new Lrf2dParam(null,Math.PI/2.0,-Math.PI,181,5,0,0);
		SpinningLrf2dParam param = new SpinningLrf2dParam(param2d,r);

		SpinningLrf2dMeasurement meas = new SpinningLrf2dMeasurement(181);
		Arrays.fill(meas.meas,10);
		meas.meas[0] = 1;
		meas.meas[90] = 1;
		meas.meas[180] = 1;

		SpinningLrf2dScanToPoints alg = new SpinningLrf2dScanToPoints(param);

		alg.process(meas,null,null);

		assertEquals(3,alg.getPoints().size());
		assertEquals(0,alg.getPoints().get(0).distance(new Point3D_F64(r-1,0,0)),1e-8);
		assertEquals(0,alg.getPoints().get(1).distance(new Point3D_F64(r,0,1)),1e-8);
		assertEquals(0,alg.getPoints().get(2).distance(new Point3D_F64(r+1,0,0)),1e-8);
	}

	/**
	 * Rotation angle of 0.  lrfToArm is T = (0,0.5,0).
	 */
	@Test
	public void checkLrfToArm() {

		double r = 2;
		Lrf2dParam param2d = new Lrf2dParam(null,Math.PI/2.0,-Math.PI,181,5,0,0);
		SpinningLrf2dParam param = new SpinningLrf2dParam(param2d,r);
		param.lrfToArm.getT().set(0,0.5,0);

		SpinningLrf2dMeasurement meas = new SpinningLrf2dMeasurement(181);
		Arrays.fill(meas.meas,10);
		meas.meas[0] = 1;
		meas.meas[90] = 1;
		meas.meas[180] = 1;

		SpinningLrf2dScanToPoints alg = new SpinningLrf2dScanToPoints(param);

		alg.process(meas,null,null);

		assertEquals(3,alg.getPoints().size());
		assertEquals(0,alg.getPoints().get(0).distance(new Point3D_F64(r-1,0.5,0)),1e-8);
		assertEquals(0,alg.getPoints().get(1).distance(new Point3D_F64(r,0.5,1)),1e-8);
		assertEquals(0,alg.getPoints().get(2).distance(new Point3D_F64(r+1,0.5,0)),1e-8);
	}

	/**
	 * Rotate the sensor by 90 degrees in a single scan.  See if the 3 sample points are at the expected location
	 */
	@Test
	public void checkInterpolation() {

		double r = 2;
		Lrf2dParam param2d = new Lrf2dParam(null,Math.PI/2.0,-Math.PI,181,5,0,0);
		SpinningLrf2dParam param = new SpinningLrf2dParam(param2d,r);

		SpinningLrf2dMeasurement meas = new SpinningLrf2dMeasurement(181);
		meas.angle1 = Math.PI/2.0;
		Arrays.fill(meas.meas,10);
		meas.meas[0] = 1;
		meas.meas[90] = 1;
		meas.meas[180] = 1;

		SpinningLrf2dScanToPoints alg = new SpinningLrf2dScanToPoints(param);

		alg.process(meas,null,null);

		assertEquals(3, alg.getPoints().size());
		assertEquals(0,alg.getPoints().get(0).distance(new Point3D_F64(r-1,0,0)),1e-8);
		assertEquals(0,alg.getPoints().get(1).distance(new Point3D_F64(r*0.5,r*0.5,1)),1e-8); // linear interpolation...
		assertEquals(0,alg.getPoints().get(2).distance(new Point3D_F64(0,r+1,0)),1e-8);
	}
}
