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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A series of tests that provide a basic sanity check on the correctness of an implementation
 * of the EkfPropagator interface
 */
public abstract class GenericEkfPredictorDiscreteTests {

	public void allGenericTests() {
		checkOriginalUnchanged();
	}


	/**
	 * The original state that it provided to the propagator should not be modified
	 */
	public void checkOriginalUnchanged() {
		EkfPredictor prop = createPredictor();

		DMatrixRMaj orig = createInitState().getMean();
		DMatrixRMaj origTest = createInitState().getMean();

		prop.predict(orig, null, -1);

		assertTrue(MatrixFeatures_DDRM.isIdentical(orig, origTest, 1e-8));

		assertFalse(MatrixFeatures_DDRM.isIdentical(orig, prop.getPredictedState(), 1e-8));
	}

	public abstract EkfPredictor createPredictor();

	public abstract MultivariateGaussianDM createInitState();
}
