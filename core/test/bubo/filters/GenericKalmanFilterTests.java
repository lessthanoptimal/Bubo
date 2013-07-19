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

package bubo.filters;

import bubo.filters.abst.KalmanFilterInterface;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.NormOps;
import org.ejml.ops.SpecializedOps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * This tests behaviors that types Kalman filter and implementations should have.  See
 * comments in each test for the details
 */
public abstract class GenericKalmanFilterTests {

	public void performAllGenericTests() {
		testPropagationCovarianceIncrease();
		testUpdateCovariance();
	}

	/**
	 * Propagate the createInitialState() forward in time and compare it against
	 * an expected value with the specified tolerance.
	 */
	public void checkPropagation( DenseMatrix64F expected , double tol )
	{
		KalmanFilterInterface filter = createFilter();

		MultivariateGaussianDM x = createInitialState();

		filter.predict(x);

		// see if the mean is correct
		for( int i = 0; i < x.getDimension(); i++ ) {
			assertEquals(expected.get(i,0),x.getMean().get(i,0),tol);
		}
	}

	/**
	 * Makes sure the covariance increases as it propagates forward with time.
	 */
	public void testPropagationCovarianceIncrease() {
		KalmanFilterInterface filter = createFilter();

		MultivariateGaussianDM x = createInitialState();

		double prev = NormOps.normF(x.getCovariance());

		for( int i = 0; i < 5; i++ ) {
			filter.predict(x);

			assertTrue(!MatrixFeatures.hasUncountable(x.getMean()) );
			assertTrue(!MatrixFeatures.hasUncountable(x.getCovariance()) );

			double after = NormOps.normF(x.getCovariance());

			assertTrue( after > prev);
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
		DenseMatrix64F x_tgt = createTargetState();

		double prevCov = NormOps.normF(x.getCovariance());
		double prevState = SpecializedOps.diffNormF(x.getMean(),x_tgt);

		MultivariateGaussianDM z = createPerfectMeas(filter,x_tgt);

		for( int i = 0; i < 5; i++ ) {
			filter.update(x,z);

			assertTrue(!MatrixFeatures.hasUncountable(x.getMean()) );
			assertTrue(!MatrixFeatures.hasUncountable(x.getCovariance()) );

			// error after update
			double afterCov = NormOps.normF(x.getCovariance());
			double afterState = SpecializedOps.diffNormF(x.getMean(),x_tgt);

			assertTrue( afterCov < prevCov);
			assertTrue( afterState < prevState);
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
	protected abstract DenseMatrix64F createTargetState();

	/**
	 * Given the target's state, create a perfect measurement.
	 *
	 * @param filter Used to extract a projector for the measurement
	 * @param state The target's state
	 */
	protected abstract MultivariateGaussianDM createPerfectMeas( KalmanFilterInterface filter,
																 DenseMatrix64F state );

	/**
	 * Creates a Gaussian distribution with a covariance matrix and the specified state.
	 */
	public static MultivariateGaussianDM createState( double cov , double ...x )
	{
		DenseMatrix64F X = new DenseMatrix64F(x.length,1);
		DenseMatrix64F P = new DenseMatrix64F(x.length,x.length);

		for( int i = 0; i < x.length; i++ ) {
			X.set(i,0,x[i]);
			P.set(i,i,cov);
		}
		MultivariateGaussianDM ret = new MultivariateGaussianDM();
		ret.assignRef(X,P);

		return ret;
	}
}
