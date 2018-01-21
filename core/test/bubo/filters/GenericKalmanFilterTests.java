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

package bubo.filters;

import bubo.filters.abst.KalmanFilterInterface;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * This tests behaviors that types Kalman filter and implementations should have.  See
 * comments in each test for the details
 */
public abstract class GenericKalmanFilterTests {

	/**
	 * Creates a Gaussian distribution with a covariance matrix and the specified state.
	 */
	public static MultivariateGaussianDM createState(double cov, double... x) {
		DMatrixRMaj X = new DMatrixRMaj(x.length, 1);
		DMatrixRMaj P = new DMatrixRMaj(x.length, x.length);

		for (int i = 0; i < x.length; i++) {
			X.set(i, 0, x[i]);
			P.set(i, i, cov);
		}
		MultivariateGaussianDM ret = new MultivariateGaussianDM();
		ret.assignRef(X, P);

		return ret;
	}

	public void performAllGenericTests() {
		testPropagationCovarianceIncrease();
		testUpdateCovariance();
	}

	/**
	 * Propagate the createInitialState() forward in time and compare it against
	 * an expected value with the specified tolerance.
	 */
	public void checkPropagation(DMatrixRMaj expected, double tol) {
		KalmanFilterInterface filter = createFilter();

		MultivariateGaussianDM x = createInitialState();

		filter.predict(x,null,-1);

		// see if the mean is correct
		for (int i = 0; i < x.getDimension(); i++) {
			assertEquals(expected.get(i, 0), x.getMean().get(i, 0), tol);
		}
	}

	/**
	 * Makes sure the covariance increases as it propagates forward with time.
	 */
	public void testPropagationCovarianceIncrease() {
		KalmanFilterInterface filter = createFilter();

		MultivariateGaussianDM x = createInitialState();

		double prev = NormOps_DDRM.normF(x.getCovariance());

		for (int i = 0; i < 5; i++) {
			filter.predict(x,null,-1);

			assertTrue(!MatrixFeatures_DDRM.hasUncountable(x.getMean()));
			assertTrue(!MatrixFeatures_DDRM.hasUncountable(x.getCovariance()));

			double after = NormOps_DDRM.normF(x.getCovariance());

			assertTrue(after > prev);
			prev = after;
		}
	}

	/**
	 * Makes sure the covariance decreases and that it converges towards the target
	 * state after an update.
	 */
	public void testUpdateCovariance() {
		KalmanFilterInterface filter = createFilter();

		MultivariateGaussianDM x = createInitialState();
		DMatrixRMaj x_tgt = createTargetState();

		double prevCov = NormOps_DDRM.normF(x.getCovariance());
		double prevState = SpecializedOps_DDRM.diffNormF(x.getMean(), x_tgt);

		MultivariateGaussianDM z = createPerfectMeas(filter, x_tgt);

		for (int i = 0; i < 5; i++) {
			filter.update(x, z);

			assertTrue(!MatrixFeatures_DDRM.hasUncountable(x.getMean()));
			assertTrue(!MatrixFeatures_DDRM.hasUncountable(x.getCovariance()));

			// error after update
			double afterCov = NormOps_DDRM.normF(x.getCovariance());
			double afterState = SpecializedOps_DDRM.diffNormF(x.getMean(), x_tgt);

			assertTrue(afterCov < prevCov);
			assertTrue(afterState < prevState);
			prevCov = afterCov;
			prevState = afterState;
		}
	}

	protected abstract KalmanFilterInterface createFilter();

	/**
	 * Creates the inital state estimate that will be given to both filters
	 */
	protected abstract MultivariateGaussianDM createInitialState();

	/**
	 * Creates the initial state of the target
	 */
	protected abstract DMatrixRMaj createTargetState();

	/**
	 * Given the target's state, create a perfect measurement.
	 *
	 * @param filter Used to extract a projector for the measurement
	 * @param state  The target's state
	 */
	protected abstract MultivariateGaussianDM createPerfectMeas(KalmanFilterInterface filter,
																DMatrixRMaj state);
}
