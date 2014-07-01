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

import org.ddogleg.rand.MultivariateGaussianDraw;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;

import java.util.Random;

/**
 * Utilities for dealing with multi-variate Gaussian distributions
 */
public class UtilMultivariateGaussian {
	/**
	 * Does a random draw from the provided distribution.
	 *
	 * @param ret If not null the results are stored here.  Otherwise it create a new matrix.
	 */
	public static DenseMatrix64F randomDraw(MultivariateGaussianDM dist, Random rand, DenseMatrix64F ret) {
		MultivariateGaussianDraw draw = new MultivariateGaussianDraw(rand, dist.getMean(), dist.getCovariance());

		if (ret == null)
			ret = new DenseMatrix64F(dist.getDimension(), 1);

		draw.next(ret);

		return ret;
	}

	/**
	 * Compute the likelihood, without the 1/(2pi)^(1/n) term.
	 *
	 * @param y the difference between the mean and the sample
	 * @param S the covariance of the distribution
	 */
	public static double likelihoodP(DenseMatrix64F y, DenseMatrix64F S, DenseMatrix64F S_inv) {
		DenseMatrix64F d = new DenseMatrix64F(1, y.numRows);

		return likelihoodP(y, S, S_inv, d);
	}

	/**
	 * Compute the likelihood, without the 1/(2pi)^(1/n) term.
	 *
	 * @param y  the difference between the mean and the sample
	 * @param S  the covariance of the distribution
	 * @param _d a variable used to store temporary results that is 1 by N where N is measurement DOF
	 */
	public static double likelihoodP(DenseMatrix64F y,
									 DenseMatrix64F S, DenseMatrix64F S_inv,
									 DenseMatrix64F _d) {
		double S_det = CommonOps.det(S);

		double chisq = chiSquare(y, S_inv, _d);

		return (1.0 / Math.sqrt(S_det)) * Math.exp(-0.5 * chisq);
	}

	/**
	 * Returns the -2 log likelihood with out the (N/2)*log( 2pi) term.
	 *
	 * @param y     the difference between the mean and the sample
	 * @param S     the covariance of the distribution
	 * @param S_inv the inverse of S
	 */
	public static double logLikelihoodP(DenseMatrix64F y, DenseMatrix64F S, DenseMatrix64F S_inv) {
		DenseMatrix64F d = new DenseMatrix64F(1, y.numRows);

		return logLikelihoodP(y, S, S_inv, d);
	}

	/**
	 * Returns the -2 log likelihood with out the (N/2)*log( 2pi) term.
	 *
	 * @param y     the difference between the mean and the sample
	 * @param S     the covariance of the distribution
	 * @param S_inv the inverse of S
	 * @param _d    a variable used to store temporary results that is 1 by N where N is measurement DOF
	 */
	public static double logLikelihoodP(DenseMatrix64F y,
										DenseMatrix64F S, DenseMatrix64F S_inv,
										DenseMatrix64F _d) {
		double S_det = CommonOps.det(S);

		double chisq = chiSquare(y, S_inv, _d);

		return Math.log(S_det) + chisq;
	}

	/**
	 * Computes the chi square for the specified Gaussian
	 *
	 * @param y     the difference between the mean and the sample
	 * @param S_inv the inverse of covariance of the distribution
	 */
	public static double chiSquare(DenseMatrix64F y, DenseMatrix64F S_inv) {
		DenseMatrix64F d = new DenseMatrix64F(1, y.numRows);

		return chiSquare(y, S_inv, d);
	}

	/**
	 * Computes the chi square for the specified Gaussian
	 *
	 * @param y     the difference between the mean and the sample
	 * @param S_inv the inverse of covariance of the distribution
	 * @param _d    a variable used to store temporary results that is 1 by N where N is measurement DOF
	 */
	public static double chiSquare(DenseMatrix64F y, DenseMatrix64F S_inv, DenseMatrix64F _d) {
		CommonOps.multTransA(y, S_inv, _d);

		double val = 0.0;
		for (int i = 0; i < y.numRows; i++) {
			val += y.get(i, 0) * _d.get(0, i);
		}

		return val;
	}

	/**
	 * Returns true if the two distributions are very similar to each other.  An element
	 * by element comparision is performed to test for similarity.
	 */
	public static boolean isSimilar(MultivariateGaussianDM a, MultivariateGaussianDM b) {
		boolean check;
		check = MatrixFeatures.isIdentical(a.getMean(), b.getMean(), 1e-8);
		if (!check) return false;

		check = MatrixFeatures.isIdentical(a.getCovariance(), b.getCovariance(), 1e-8);
		return check;
	}

	/**
	 * Returns true if the two distributions are very similar to each other.  An element
	 * by element comparision is performed to test for similarity.
	 */
	public static boolean isSimilar(MultivariateGaussianDM a, MultivariateGaussianDM b,
									double meanTol, double covTol) {
		boolean check;
		check = MatrixFeatures.isIdentical(a.getMean(), b.getMean(), meanTol);
		if (!check) return false;

		check = MatrixFeatures.isIdentical(a.getCovariance(), b.getCovariance(), covTol);
		return check;
	}

	/**
	 * Creates a new distribution that has elements from the original
	 */
	public static MultivariateGaussianDM subState(MultivariateGaussianDM orig, int a, int b) {
		MultivariateGaussianDM ret = new MultivariateGaussianDM(b - a);

		for (int i = a; i < b; i++) {
			ret.getMean().set(i - a, 0, orig.getMean().get(i, 0));
			for (int j = a; j < b; j++) {
				ret.getCovariance().set(i - a, j - a, orig.getCovariance().get(i, j));
			}
		}

		return ret;
	}

	public static void subState(MultivariateGaussianDM orig, int a, int b,
								MultivariateGaussianDM result) {
		for (int i = a; i < b; i++) {
			result.getMean().set(i - a, 0, orig.getMean().get(i, 0));
			for (int j = a; j < b; j++) {
				result.getCovariance().set(i - a, j - a, orig.getCovariance().get(i, j));
			}
		}
	}

	/**
	 * Copies all the states of the original into the desitnation.
	 */
	public static void copyInto(MultivariateGaussianDM orig, MultivariateGaussianDM dest) {
		DenseMatrix64F o_x = orig.getMean();
		DenseMatrix64F o_p = orig.getCovariance();

		DenseMatrix64F d_x = dest.getMean();
		DenseMatrix64F d_p = dest.getCovariance();

		final int N = orig.getDimension();

		for (int i = 0; i < N; i++) {
			d_x.set(i, 0, o_x.get(i, 0));

			for (int j = 0; j < N; j++) {
				d_p.set(i, j, o_p.get(i, j));
			}
		}
	}

	/**
	 * Creates a state with a covariance that has an identify matrix.
	 */
	public static MultivariateGaussianDM createDummy(int dof, double covMag) {
		MultivariateGaussianDM ret = new MultivariateGaussianDM(dof);

		DenseMatrix64F P = ret.getCovariance();

		for (int i = 0; i < dof; i++) {
			P.set(i, i, covMag);
		}

		return ret;
	}

	/**
	 * Sets all the elements in the state and covariance to zero
	 */
	public static void zero(MultivariateGaussianDM state) {
		final int N = state.getDimension();

		DenseMatrix64F d_x = state.getMean();
		DenseMatrix64F d_p = state.getCovariance();

		for (int i = 0; i < N; i++) {
			d_x.set(i, 0, 0);

			for (int j = 0; j < N; j++) {
				d_p.set(i, j, 0);
			}
		}
	}
}
