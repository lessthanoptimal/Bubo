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

package bubo.filters.kf;

import bubo.filters.abst.InnovationInterface;
import org.ejml.data.DenseMatrix64F;

import static org.ejml.ops.CommonOps.*;

/**
 * The discrete Kalman Filter (KF) and Extended Kalman Filter (EKF) both share much of the same code.  The
 * only differences is how the state is predicted and updated.  The following is a standard
 * implementation of the KF and a first order EKF.
 */
// TODO add optional runtime matrix tests
public abstract class DKFCommon implements InnovationInterface {
	// these are predeclared for efficiency reasons
	protected DenseMatrix64F a, b;
	protected DenseMatrix64F y, S, S_inv, c, d;
	protected DenseMatrix64F K;

	public DKFCommon(int dimenX, int dimenZ) {
		a = new DenseMatrix64F(dimenX, 1);
		b = new DenseMatrix64F(dimenX, dimenX);
		y = new DenseMatrix64F(dimenZ, 1);
		S = new DenseMatrix64F(dimenZ, dimenZ);
		S_inv = new DenseMatrix64F(dimenZ, dimenZ);
		c = new DenseMatrix64F(dimenZ, dimenX);
		d = new DenseMatrix64F(dimenX, dimenZ);
		K = new DenseMatrix64F(dimenX, dimenZ);
	}

	/**
	 * Returns the number of DOF in the state
	 */
	public int getStateDOF() {
		return a.numRows;
	}

	/**
	 * Returns the number of DOF in the measurements
	 */
	public int getMeasDOF() {
		return y.numRows;
	}

	/**
	 * The innovation is defined as the observed measurement subtract the predicted measurement:
	 * <p/>
	 * y(k) = z(k) -  H(k)x(k|k-1)
	 */
	@Override
	public DenseMatrix64F getInnovation() {
		return y;
	}

	/**
	 * S(k) = H(k) P(k|k-1) H'(k)  + R(k)
	 */
	@Override
	public DenseMatrix64F getInnovationCov() {
		return S;
	}

	@Override
	public DenseMatrix64F getInnovationCovInverse() {
		return S_inv;
	}

	public DenseMatrix64F getGain() {
		return K;
	}

	protected final void _predictCovariance(DenseMatrix64F F, DenseMatrix64F Q, DenseMatrix64F P) {
		// update the covariance estimate
		// F P F' + Q
		mult(F, P, b);
		multTransB(b, F, P);
		addEquals(P, Q);
	}

	protected final void _updateCovariance(DenseMatrix64F H, DenseMatrix64F x, DenseMatrix64F P, DenseMatrix64F R) {
		// compute innovation covariance
		// H P H' + R
		mult(H, P, c);
		multTransB(c, H, S);
		addEquals(S, R);

		// compute the Kalman gain
		// K = PH'S^(-1)
		invert(S, S_inv);
		multTransA(H, S_inv, d);
		mult(P, d, K);

		// update the state estimate
		// x = x + Ky
		mult(K, y, a);
		addEquals(x, a);

		// update the covariance estimate
		// P = (I-kH)P = P - K(HP)
		mult(H, P, c);
		mult(K, c, b);
		subEquals(P, b);

		// the above is correct because matrix multiplication is associative
		// (KH)P=K(HP)
	}
}
