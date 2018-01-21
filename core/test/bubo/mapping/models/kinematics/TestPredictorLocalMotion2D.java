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

package bubo.mapping.models.kinematics;

import georegression.struct.se.Se2_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.NormOps_DDRM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestPredictorLocalMotion2D extends StandardPredictorTests {

	LocalMotion2D control = new LocalMotion2D();

	public TestPredictorLocalMotion2D() {
		super(1e-6);
	}

	@Test
	public void stateJacobian() {
		PredictorLocalMotion2D alg = new PredictorLocalMotion2D(0.1,0.01,0.1);

		// check translation only
		control.set(1.5, 0.5, 0);
		checkStateJacobianAtPoint(alg, control, false, -1, 1, 2, Math.PI / 2.0);
		control.set(1.5, 0.5, Math.PI/2);
		checkStateJacobianAtPoint(alg, control, false, -1, 1, 2, 0);

		// check rotation only
		control.set(0, 0, 1);
		checkStateJacobianAtPoint(alg, control, false, -1, 1, 2, Math.PI / 2.0);

		// check both
		control.set(0.7, 0.5, 1);
		checkStateJacobianAtPoint(alg, control, false, -1, 1, 2, Math.PI / 2.0);
	}

	@Test
	public void predictedState() {
		PredictorLocalMotion2D alg = new PredictorLocalMotion2D(0.1,0.01,0.1);

		Se2_F64 init = new Se2_F64(1,2, Math.PI/2.0);
		DMatrixRMaj x = new DMatrixRMaj(3, 1, true, init.T.x, init.T.y, init.getYaw());

		control.set(1,0.5,0.1);


		Se2_F64 expected = init.copy();
		control.addTo(expected);

		alg.predict(x, control, -1);
		DMatrixRMaj xp = alg.getPredictedState();

		assertEquals(expected.T.x, xp.get(0), 1e-8);
		assertEquals(expected.T.y, xp.get(1), 1e-8);
		assertEquals(expected.getYaw(), xp.get(2), 1e-8);
	}

	/**
	 * Plant noise should get larger as it moves more
	 */
	@Test
	public void plantNoise_motion() {
		PredictorLocalMotion2D alg = new PredictorLocalMotion2D(0.1,0.01,0.1);
		DMatrixRMaj x = new DMatrixRMaj(3, 1);

		// plant noise should be zero since there is no motion
		alg.predict(x, new LocalMotion2D(0,0,0), -1);
		assertEquals(0, NormOps_DDRM.normF(alg.getPlantNoise()),1e-8);

		alg.predict(x, new LocalMotion2D(1,0,0), -1);
		assertTrue( NormOps_DDRM.normF(alg.getPlantNoise()) > 0 );
		alg.predict(x, new LocalMotion2D(0,1,0), -1);
		assertTrue( NormOps_DDRM.normF(alg.getPlantNoise()) > 0 );
		alg.predict(x, new LocalMotion2D(0,0,1), -1);
		assertTrue( NormOps_DDRM.normF(alg.getPlantNoise()) > 0 );
	}

	@Test
	public void plantNoise_valid_covariance() {
		PredictorLocalMotion2D alg = new PredictorLocalMotion2D(0.1,0.01,0.1);
		control.set(1,0.5,0);
		checkValidCovariance(alg, control, -1, 1, 2, 0.4);
		control.set(0, 0, 0.5);
		checkValidCovariance(alg, control, -1, 1, 2, 0.4);
	}
}