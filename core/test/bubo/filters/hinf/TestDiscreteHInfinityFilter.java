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

package bubo.filters.hinf;

import bubo.filters.GenericKalmanFilterTests;
import bubo.filters.MultivariateGaussianDM;
import bubo.filters.abst.KalmanFilterInterface;
import bubo.filters.kf.ConstAccel1D;
import bubo.filters.kf.FixedKalmanProjector;
import bubo.filters.kf.KalmanFilter;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

// TODO add a test that verifies the update some how
public class TestDiscreteHInfinityFilter extends GenericKalmanFilterTests {
	double theta = Double.NaN;

	/**
	 * Run a battery of tests that are designed to test specific behaviors in a Kalman
	 * filter.
	 */
	@Test
	public void performanKalmanTests() {
		// this better pass, its a kalman filter
		theta = 0;
		performAllGenericTests();

		theta = 0.5;
		testPropagationCovarianceIncrease();
	}

	/**
	 * If theta is zero and L and S are the indenity matrix,
	 * then it reduces to the Kalman filter.
	 */
	@Test
	public void checkKalmanEquivelence() {
		theta = 0;
		KalmanFilterInterface kalman = createSimilarKalman();
		KalmanFilterInterface h_inf = createFilter();

		DenseMatrix64F x_tgt = createTargetState();

		MultivariateGaussianDM x_kal = createInitialState();
		MultivariateGaussianDM x_inf = createInitialState();

		MultivariateGaussianDM z = createPerfectMeas(h_inf, x_tgt);

		for (int i = 0; i < 5; i++) {
			kalman.update(x_kal, z);
			h_inf.update(x_inf, z);

			assertTrue(MatrixFeatures.isIdentical(x_kal.getMean(), x_inf.getMean(), 1e-5));
			assertTrue(MatrixFeatures.isIdentical(x_kal.getCovariance(), x_inf.getCovariance(), 1e-5));

			kalman.predict(x_kal,null,-1);
			h_inf.predict(x_inf,null,-1);

			assertTrue(MatrixFeatures.isIdentical(x_kal.getMean(), x_inf.getMean(), 1e-5));
			assertTrue(MatrixFeatures.isIdentical(x_kal.getCovariance(), x_inf.getCovariance(), 1e-5));
		}
	}

	@Override
	protected KalmanFilterInterface createFilter() {
		ConstAccel1D constAccelProp = createProp();

		DenseMatrix64F H = new DenseMatrix64F(new double[][]{{1, 1, 1}, {0, 1, 2}});

		FixedKalmanProjector projector = new FixedKalmanProjector(H);

		DenseMatrix64F S = CommonOps.identity(3);
		DenseMatrix64F L = CommonOps.identity(3);

		return new DiscreteHInfinityFilter(constAccelProp, projector, null, S, L, theta);
	}

	private ConstAccel1D createProp() {
		return new ConstAccel1D(1.0, 1.1);
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
		DiscreteHInfinityFilter filter = (DiscreteHInfinityFilter) f;
		DenseMatrix64F H = filter.getProjector().getProjectionMatrix();
		DenseMatrix64F X = state;

		DenseMatrix64F z = new DenseMatrix64F(2, 1);

		CommonOps.mult(H, X, z);

		return createState(2.0, z.get(0, 0), z.get(1, 0));
	}


	/**
	 * Create a kalman filter that has the same kinematics as the other filter.
	 */
	protected KalmanFilterInterface createSimilarKalman() {
		ConstAccel1D constAccelProp = createProp();

		DenseMatrix64F H = new DenseMatrix64F(new double[][]{{1, 1, 1}, {0, 1, 2}});

		FixedKalmanProjector projector = new FixedKalmanProjector(H);

		return new KalmanFilter(constAccelProp, projector);
	}
}
