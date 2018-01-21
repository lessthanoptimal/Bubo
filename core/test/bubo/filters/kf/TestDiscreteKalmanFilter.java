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

package bubo.filters.kf;

import bubo.filters.GenericKalmanFilterTests;
import bubo.filters.MultivariateGaussianDM;
import bubo.filters.abst.KalmanFilterInterface;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestDiscreteKalmanFilter extends GenericKalmanFilterTests {

	@Test
	public void doAllStandardTests() {
		performAllGenericTests();
	}

	/**
	 * Checks to see if the forward propagation computes the expected values in the state
	 */
	@Test
	public void checkPropagationValues() {
		KalmanFilter filter = (KalmanFilter) createFilter();

		MultivariateGaussianDM x = createState(9.0, 0, 1, 2);

		filter.predict(x,null,-1);

		// see if the mean is correct
		assertEquals(2.0, x.getMean().get(0, 0), 1e-6);
		assertEquals(3.0, x.getMean().get(1, 0), 1e-6);
		assertEquals(2.0, x.getMean().get(2, 0), 1e-6);
	}

	/**
	 * Checks to see if the control input is used and not used when appropriate.
	 */
	@Test
	public void checkControlUse() {
		DMatrixRMaj F = CommonOps_DDRM.identity(3);
		DMatrixRMaj Q = CommonOps_DDRM.identity(3);
		DMatrixRMaj G = new DMatrixRMaj(3, 2);
		DMatrixRMaj u = new DMatrixRMaj(2, 1);

		CommonOps_DDRM.fill(G, 1.0);
		CommonOps_DDRM.fill(u, 2.0);

		FixedKalmanPredictor prop = new FixedKalmanPredictor(F, G, Q);

		DMatrixRMaj H = new DMatrixRMaj(new double[][]{{1, 1, 1}, {0, 1, 2}});

		KalmanProjector projector = new FixedKalmanProjector(H);

		// see how it works with the control
		KalmanFilter kf = new KalmanFilter(prop, projector);
		kf.setControlInput(u);

		MultivariateGaussianDM x = new MultivariateGaussianDM(3);
		kf.predict(x,null,-1);

		for (int i = 0; i < 3; i++) {
			assertTrue(x.getMean().get(i, 0) > 0);
		}

		// now do it without the control
		prop.setControlTransition(null);

		x = new MultivariateGaussianDM(3);
		kf.predict(x,null,-1);

		for (int i = 0; i < 3; i++) {
			assertTrue(x.getMean().get(i, 0) == 0);
		}
	}

	@Override
	protected KalmanFilterInterface createFilter() {
		ConstAccel1D constAccelProp = new ConstAccel1D(1.0, 1);

		DMatrixRMaj H = new DMatrixRMaj(new double[][]{{1, 1, 1}, {0, 1, 2}});

		FixedKalmanProjector projector = new FixedKalmanProjector(H);

		return new KalmanFilter(constAccelProp, projector);
	}

	@Override
	protected MultivariateGaussianDM createInitialState() {
		return createState(9.0, 2, 3, 4);
	}

	@Override
	protected DMatrixRMaj createTargetState() {
		return createState(9.0, 1, 1, 1).getMean();
	}

	@Override
	protected MultivariateGaussianDM createPerfectMeas(KalmanFilterInterface f,
													   DMatrixRMaj state) {
		KalmanFilter filter = (KalmanFilter) f;
		DMatrixRMaj H = filter.getProjector().getProjectionMatrix();
		DMatrixRMaj X = state;

		DMatrixRMaj z = new DMatrixRMaj(2, 1);

		CommonOps_DDRM.mult(H, X, z);

		return createState(2.0, z.get(0, 0), z.get(1, 0));
	}
}
