/*
 * Copyright (c) 2013, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
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
		DiscreteKalmanFilter filter = (DiscreteKalmanFilter) createFilter();

		MultivariateGaussianDM x = createState(9.0, 0, 1, 2);

		filter.predict(x);

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
		DenseMatrix64F F = CommonOps.identity(3);
		DenseMatrix64F Q = CommonOps.identity(3);
		DenseMatrix64F G = new DenseMatrix64F(3, 2);
		DenseMatrix64F u = new DenseMatrix64F(2, 1);

		CommonOps.fill(G, 1.0);
		CommonOps.fill(u, 2.0);

		FixedKalmanPredictor prop = new FixedKalmanPredictor(F, G, Q);

		DenseMatrix64F H = new DenseMatrix64F(new double[][]{{1, 1, 1}, {0, 1, 2}});

		KalmanProjector projector = new FixedKalmanProjector(H);

		// see how it works with the control
		DiscreteKalmanFilter kf = new DiscreteKalmanFilter(prop, projector);
		kf.setControlInput(u);

		MultivariateGaussianDM x = new MultivariateGaussianDM(3);
		kf.predict(x);

		for (int i = 0; i < 3; i++) {
			assertTrue(x.getMean().get(i, 0) > 0);
		}

		// now do it without the control
		prop.setControlTransition(null);

		x = new MultivariateGaussianDM(3);
		kf.predict(x);

		for (int i = 0; i < 3; i++) {
			assertTrue(x.getMean().get(i, 0) == 0);
		}
	}

	@Override
	protected KalmanFilterInterface createFilter() {
		ConstAccel1D constAccelProp = new ConstAccel1D(1.0, 1);

		DenseMatrix64F H = new DenseMatrix64F(new double[][]{{1, 1, 1}, {0, 1, 2}});

		FixedKalmanProjector projector = new FixedKalmanProjector(H);

		return new DiscreteKalmanFilter(constAccelProp, projector);
	}

	@Override
	protected MultivariateGaussianDM createInitialState() {
		return createState(9.0, 2, 3, 4);
	}

	@Override
	protected DenseMatrix64F createTargetState() {
		return createState(9.0, 1, 1, 1).getMean();
	}

	@Override
	protected MultivariateGaussianDM createPerfectMeas(KalmanFilterInterface f,
													   DenseMatrix64F state) {
		DiscreteKalmanFilter filter = (DiscreteKalmanFilter) f;
		DenseMatrix64F H = filter.getProjector().getProjectionMatrix();
		DenseMatrix64F X = state;

		DenseMatrix64F z = new DenseMatrix64F(2, 1);

		CommonOps.mult(H, X, z);

		return createState(2.0, z.get(0, 0), z.get(1, 0));
	}
}
