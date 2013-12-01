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

package bubo.filters.specific.ekf;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.ekf.EkfPredictorTime;
import bubo.filters.ekf.UtilEkfPropagator;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A series of tests that provide a basic sanity check on the correctness of an implementation
 * of the EkfPropagator interface
 */
public abstract class GenericEkfPredictorTimeTests {

	public void allGenericTests() {
		checkDoublePrediction();
		checkNumericallyF();
		checkOriginalUnchanged();
	}

	/**
	 * Propagate twice and then propagate once for the same length of time as the first time.
	 * See if the answers are the same.
	 */
	public void checkDoublePrediction() {
		DenseMatrix64F initX = createInitState().getMean();

		EkfPredictorTime prop = createPredictor();

		prop.compute(initX, 1);
		DenseMatrix64F a = new DenseMatrix64F(prop.getPredictedState());

		prop.compute(initX, 0.5);
		DenseMatrix64F b = new DenseMatrix64F(prop.getPredictedState());
		prop.compute(b, 0.5);
		b.set(prop.getPredictedState());

		assertTrue(MatrixFeatures.isIdentical(a, b, 1e-5));
	}

	/**
	 * Compare a numerically computed F to the one returned by the propagator.
	 */
	public void checkNumericallyF() {
		EkfPredictorTime prop = createPredictor();

		double T = 1e-2;

		// numerically compute F
		DenseMatrix64F a = createInitState().getMean();

		DenseMatrix64F F_numerical = UtilEkfPropagator.numericalJacobian(a, prop, T, 1e-3);

		// get the one from the propagator
		prop.compute(a, T);
		DenseMatrix64F F = prop.getJacobianF();


		// compare results
		assertTrue(MatrixFeatures.isIdentical(F_numerical, F, 1e-5));

	}

	/**
	 * The original state that it provided to the propagator should not be modified
	 */
	public void checkOriginalUnchanged() {
		EkfPredictorTime prop = createPredictor();

		DenseMatrix64F orig = createInitState().getMean();
		DenseMatrix64F origTest = createInitState().getMean();

		prop.compute(orig, 1);

		assertTrue(MatrixFeatures.isIdentical(orig, origTest, 1e-8));

		assertFalse(MatrixFeatures.isIdentical(orig, prop.getPredictedState(), 1e-8));
	}

	public abstract EkfPredictorTime createPredictor();

	public abstract MultivariateGaussianDM createInitState();
}