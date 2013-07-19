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

import org.ejml.data.DenseMatrix64F;

/**
 * A Gaussian (or normal) distribution in N dimensions is stored here.  The distributions is described by a mean
 * and a covariance.
 */
public final class MultivariateGaussianDM {
	// the mean
	public DenseMatrix64F x;
	// the covariance
	public DenseMatrix64F P;

	/**
	 * Creates a new structure with null values for its mean and covariance.
	 * This should be used in conjunction with assignRef()
	 */
	public MultivariateGaussianDM() {
	}

	/**
	 * Creates a distribution with the specified dimentions.  All values will
	 * initially be set to zero.
	 */
	public MultivariateGaussianDM(int dimen) {
		x = new DenseMatrix64F(dimen, 1);
		P = new DenseMatrix64F(dimen, dimen);
	}

	/**
	 * Creates a new distribution that uses the specified matrices.  A copy of the
	 * matrices is made.
	 */
	public MultivariateGaussianDM(DenseMatrix64F x, DenseMatrix64F P) {
		this.x = new DenseMatrix64F(x);
		this.P = new DenseMatrix64F(P);
	}

	/**
	 * Creates a copy of the provided distribution.
	 */
	public MultivariateGaussianDM(MultivariateGaussianDM d) {
		this.x = new DenseMatrix64F(d.getMean());
		this.P = new DenseMatrix64F(d.getCovariance());
	}

	/**
	 * If the parameter is not null, then the reference provided is used internally.
	 */
	public void assignRef(DenseMatrix64F x, DenseMatrix64F P) {
		if (x != null) {
			this.x = x;
		}

		if (P != null) {
			this.P = P;
		}
	}

	public int getDimension() {
		return x.numRows;
	}

	/**
	 * Returns the mean of the Gaussian distribution
	 */
	public DenseMatrix64F getMean() {
		return x;
	}

	/**
	 * Returns the covariance of the Gaussian distribution
	 */
	public DenseMatrix64F getCovariance() {
		return P;
	}

	public MultivariateGaussianDM copy() {
		return new MultivariateGaussianDM(x, P);
	}

	public void set(MultivariateGaussianDM distribution) {
		x.set(distribution.getMean());
		P.set(distribution.getCovariance());
	}
}
