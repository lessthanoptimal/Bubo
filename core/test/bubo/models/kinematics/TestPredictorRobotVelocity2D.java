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

package bubo.models.kinematics;

import georegression.geometry.UtilPoint2D_F64;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestPredictorRobotVelocity2D extends StandardPredictorTests {

	public TestPredictorRobotVelocity2D() {
		super(1e-4);
	}

	@Test
	public void stateJacobian() {
		PredictorRobotVelocity2D alg = new PredictorRobotVelocity2D(0.1, 0.1, 0.1, 0.1);

		// check translation only
		alg.setControl(1.5, 0);
		checkStateJacobianAtPoint(alg, false, 0.5, 1, 2, Math.PI / 2.0);

		// check rotation only
		alg.setControl(0, 0.5);
		checkStateJacobianAtPoint(alg, false, 0.5, 1, 2, Math.PI / 2.0);

		// check both
		alg.setControl(0.7, 0.5);
		checkStateJacobianAtPoint(alg, false, 0.5, 1, 2, Math.PI / 2.0);
	}

	@Test
	public void predictedState() {
		PredictorRobotVelocity2D alg = new PredictorRobotVelocity2D(0.1, 0.1, 0.1, 0.1);

		// test several simple cases

		// pure translation
		DenseMatrix64F x = new DenseMatrix64F(3, 1, true, 1, 2, Math.PI / 2.0);

		alg.setControl(1, 0);
		alg.compute(x, 0.5);
		DenseMatrix64F xp = alg.getPredictedState();

		assertEquals(1, xp.get(0), 1e-8);
		assertEquals(2.5, xp.get(1), 1e-8);
		assertEquals(Math.PI / 2.0, xp.get(2), 1e-8);

		// point turn
		x = new DenseMatrix64F(3, 1, true, 1, 2, Math.PI / 2.0);
		alg.setControl(0, Math.PI / 4);
		alg.compute(x, 1);

		assertEquals(1, xp.get(0), 1e-8);
		assertEquals(2, xp.get(1), 1e-8);
		assertEquals(3 * Math.PI / 4.0, xp.get(2), 1e-8);

		// translation and rotation
		x = new DenseMatrix64F(3, 1, true, 1, 2, Math.PI / 2.0);
		alg.setControl(2, Math.PI / 4);
		alg.compute(x, 1);

		// very crude test, see if it traveled about the expected distance
		double d = UtilPoint2D_F64.distance(1, 2, xp.data[0], xp.data[1]);
		assertEquals(d, 2, 0.1);
		assertEquals(3 * Math.PI / 4.0, xp.get(2), 1e-8);
	}

	@Test
	public void plantNoise_grow() {
		PredictorRobotVelocity2D alg = new PredictorRobotVelocity2D(2, 2, 2, 2);
		alg.setControl(0.5, 0);
		checkCovarianceIncreaseWithTime(alg, 0.5, 1, 2, 0.4);
		alg.setControl(0, 0.3);
		checkCovarianceIncreaseWithTime(alg, 0.5, 1, 2, 0.4);
		alg.setControl(0.75, 0.3);
		checkCovarianceIncreaseWithTime(alg, 0.5, 1, 2, 0.4);

	}

	@Test
	public void plantNoise_valid_covariance() {
		PredictorRobotVelocity2D alg = new PredictorRobotVelocity2D(2, 2, 2, 2);
		alg.setControl(0.5, 0);
		checkValidCovariance(alg, 0.5, 1, 2, 0.4);
		alg.setControl(0, 0.5);
		checkValidCovariance(alg, 0.5, 1, 2, 0.4);
		alg.setControl(0.75, 0.5);
		checkValidCovariance(alg, 0.5, 1, 2, 0.4);
	}
}
