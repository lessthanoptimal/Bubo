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

import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestUtilMultivariateGaussian {

	/**
	 * Except in the 1D case standard functions to test it against are not readily available.
	 * Instead I see if it acts like a gaussian.  I was lazy and did the 1D case.
	 */
	@Test
	public void testLikelihoodPBehavior() {
		DMatrixRMaj y = new DMatrixRMaj(1, 1);
		DMatrixRMaj S = new DMatrixRMaj(1, 1);
		DMatrixRMaj S_inv = new DMatrixRMaj(1, 1);

		S.set(0, 0, 10);
		S_inv.set(0, 0, 1.0 / 10.0);

		double prev = UtilMultivariateGaussian.likelihoodP(y, S, S_inv);
		for (int i = 1; i < 10; i++) {
			y.set(0, 0, i);
			double curr = UtilMultivariateGaussian.likelihoodP(y, S, S_inv);

			assertTrue(prev > curr);

			prev = curr;
		}
	}

	/**
	 * Makes sure the log likelihood is the same as the log of the likelihood times -2
	 */
	@Test
	public void testLogLikelihood() {
		DMatrixRMaj y = new DMatrixRMaj(1, 1);
		DMatrixRMaj S = new DMatrixRMaj(1, 1);
		DMatrixRMaj S_inv = new DMatrixRMaj(1, 1);

		S.set(0, 0, 10);
		S_inv.set(0, 0, 1.0 / 10.0);
		y.set(0, 0, 6);

		double likelihood = UtilMultivariateGaussian.likelihoodP(y, S, S_inv);

		double logLike = UtilMultivariateGaussian.logLikelihoodP(y, S, S_inv);

		assertEquals(-2 * Math.log(likelihood), logLike, 1e-5);
	}

	@Test
	public void randomDraw() {
		MultivariateGaussianDM d = new MultivariateGaussianDM(2);
		d.getCovariance().set(0, 0, 5);
		d.getCovariance().set(1, 1, 10);

		Random rand = new Random(0xff);

		// see if it actually assigned a value to it
		DMatrixRMaj x = UtilMultivariateGaussian.randomDraw(d, rand, null);
		assertTrue(x.get(0, 0) != 0);
		assertTrue(x.get(1, 0) != 0);

		// see if anything works correctly if a matrix is provided to it
		DMatrixRMaj y = new DMatrixRMaj(2, 1);
		UtilMultivariateGaussian.randomDraw(d, rand, y);

		assertTrue(x.get(0, 0) != y.get(0, 0));
		assertTrue(x.get(1, 0) != y.get(1, 0));
	}
}
