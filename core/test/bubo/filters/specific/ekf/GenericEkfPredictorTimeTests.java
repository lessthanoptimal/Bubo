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

package bubo.filters.specific.ekf;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.ekf.EkfPredictor;
import bubo.filters.ekf.UtilEkfPropagator;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;

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
		DMatrixRMaj initX = createInitState().getMean();

		EkfPredictor prop = createPredictor();

		prop.predict(initX, null, 1);
		DMatrixRMaj a = new DMatrixRMaj(prop.getPredictedState());

		prop.predict(initX, null, 0.5);
		DMatrixRMaj b = new DMatrixRMaj(prop.getPredictedState());
		prop.predict(b, null, 0.5);
		b.set(prop.getPredictedState());

		assertTrue(MatrixFeatures_DDRM.isIdentical(a, b, 1e-5));
	}

	/**
	 * Compare a numerically computed F to the one returned by the propagator.
	 */
	public void checkNumericallyF() {
		EkfPredictor prop = createPredictor();

		double T = 1e-2;

		// numerically compute F
		DMatrixRMaj a = createInitState().getMean();

		DMatrixRMaj F_numerical = UtilEkfPropagator.numericalJacobian(a, prop, T, 1e-3);

		// get the one from the propagator
		prop.predict(a, null, T);
		DMatrixRMaj F = prop.getJacobianF();


		// compare results
		assertTrue(MatrixFeatures_DDRM.isIdentical(F_numerical, F, 1e-5));

	}

	/**
	 * The original state that it provided to the propagator should not be modified
	 */
	public void checkOriginalUnchanged() {
		EkfPredictor prop = createPredictor();

		DMatrixRMaj orig = createInitState().getMean();
		DMatrixRMaj origTest = createInitState().getMean();

		prop.predict(orig, null, 1);

		assertTrue(MatrixFeatures_DDRM.isIdentical(orig, origTest, 1e-8));

		assertFalse(MatrixFeatures_DDRM.isIdentical(orig, prop.getPredictedState(), 1e-8));
	}

	public abstract EkfPredictor createPredictor();

	public abstract MultivariateGaussianDM createInitState();
}