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

package bubo.filters.imm;

import bubo.filters.ekf.EkfPredictor;
import bubo.filters.kf.ConstAccel1D;
import bubo.filters.specific.ekf.KfToEkfPredictor;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestImmCheckingFilter {

	/**
	 * See if it will catch a bad transition matrix
	 */
	@Test
	public void checkBadTransitionMatrix() {
		DMatrixRMaj pi = CommonOps_DDRM.identity(3);

		assertTrue(ImmCheckingFilter.isValidMarkovMatrix(pi));

		pi.set(2, 1, 0.1);

		assertFalse(ImmCheckingFilter.isValidMarkovMatrix(pi));
	}

	/**
	 * Run the sanity check when nothing is wrong.
	 */
	@Test
	public void checkSanityCheckNothingBad() {
		ImmCheckingFilter.sanityCheckFilter(createModels(-1));
	}

	/**
	 * See if the state sanity check on probability works
	 */
	@Test(expected = ImmCheckingFilter.SanityCheck.class)
	public void checkSanityCheckProb() {
		ImmCheckingFilter.sanityCheckFilter(createModels(0));
	}

	@Test(expected = ImmCheckingFilter.SanityCheck.class)
	public void checkSanityCheckState() {
		ImmCheckingFilter.sanityCheckFilter(createModels(1));
	}

	@Test(expected = ImmCheckingFilter.SanityCheck.class)
	public void checkSanityCheckCovar() {
		ImmCheckingFilter.sanityCheckFilter(createModels(2));
	}

	/**
	 * Creates a set of models with various things right or wrong about them.
	 * <p/>
	 * 0 = bad probability
	 * 1 = bad state
	 * 2 = bad covariance
	 * <p/>
	 * Anything else is all good.
	 */
	private ImmHypothesis[] createModels(int whatIsBad) {
		EkfPredictor pred = createPred();

		ImmHypothesis[] ret = new ImmHypothesis[2];

		double prob = whatIsBad == 0 ? 1.2 / ret.length : 1.0 / ret.length;

		for (int i = 0; i < ret.length; i++) {
			ret[i] = new ImmHypothesis(pred);
			ret[i].setProbability(prob);

			ret[i].getState().getCovariance().set(CommonOps_DDRM.identity(3));

			if (whatIsBad == 1) {
				ret[i].getState().getMean().set(1, 0, Double.NaN);
			} else if (whatIsBad == 2) {
				ret[i].getState().getCovariance().set(1, 1, Double.NaN);
			}
		}

		return ret;
	}

	private EkfPredictor createPred() {
		return new KfToEkfPredictor(new ConstAccel1D(1, 1), null);
	}
}
