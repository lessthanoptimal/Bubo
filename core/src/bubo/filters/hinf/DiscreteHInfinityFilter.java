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

package bubo.filters.hinf;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.abst.KalmanFilterInterface;
import bubo.filters.kf.KalmanPredictor;
import bubo.filters.kf.KalmanProjector;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * An implementation of the discrete-time H Infinity filter.  This filter is designed to
 * be more robust than the Kalman filter and make less assumptions.  If everything is known
 * it tends to perform worse than a kalman filter.  If there is modeling errors or unknown
 * parameters it tends to perform better.
 * <p/>
 * "Optimal State Estimation" by Dan Simon. page 353
 * <p/>
 * This implementation is designed to be easily dropped into problem a Kalman filter
 * would be dropped into.  A more generic implementation would allow more of the
 * user specified variables to be user specified.  Essentially this makes stronger
 * assumptions about the system.
 * <p/>
 * System equations:
 * <p/>
 * x(k+1) = F(k)w(k) + w(k)
 * y(k) = H(k)x(k) + v(k)
 * z(k) = L(k)x(k)
 * <p/>
 * where w(k) and v(k) are noise terms.  Note the type of noise and other parameters are not
 * specified for w(k) and v(k) as they are in a Kalman filter.
 * <p/>
 * ------------------------------------------------
 */
public class DiscreteHInfinityFilter implements KalmanFilterInterface {
	// describes how the state changes as a function of time
	private KalmanPredictor predictor;

	// the state to meas matrix
	private KalmanProjector projector;

	/**
	 * \bar{s} = L^T S L
	 */
	private DenseMatrix64F S_bar;

	/**
	 * A user defined performance bound
	 */
	private double theta;

	private DenseMatrix64F I_z;
	private DenseMatrix64F I_x;
	private DenseMatrix64F R_inv;
	private DenseMatrix64F temp0_m_m;
	private DenseMatrix64F D;
	private DenseMatrix64F temp0_m_n;
	private DenseMatrix64F K;
	private DenseMatrix64F temp0_m_1;
	private DenseMatrix64F innovation;

	private DenseMatrix64F control;

	/**
	 * L is a user defined full rank matrix.  If we want to estimate the full state (like in a kalman
	 * filter) it is set to the identity matrix.
	 * <p/>
	 * S is a symmetric positive definte matrix (n by n) n = DOF of meas
	 * it is used to weight the importance of measurement parameters
	 * if an element in the measurement is more important then the elements
	 * corresponding to it should be larger
	 * <p/>
	 * theta is a user defined performance bound
	 * <p/>
	 * Both L and S are assumed to be constant and should not be changed.
	 *
	 * @param predictor describes how the state evolves as a function of time.  Saved internally.
	 * @param projector describes how measurements are taken of the system. Saved internally.
	 * @param control   An optional control input.  Can be set to null if there is none
	 * @param S         measurement estimation importance matrics.  Not saved internally.
	 * @param L         State estimation  matrix.  Not saved internally.
	 * @param theta     performance bound.
	 */
	public DiscreteHInfinityFilter(KalmanPredictor predictor, KalmanProjector projector,
								   DenseMatrix64F control,
								   DenseMatrix64F S, DenseMatrix64F L, double theta) {
		this.predictor = predictor;
		this.projector = projector;
		this.theta = theta;

		if (control != null) {
			this.control = control.copy();
		}

		int m = predictor.getNumStates();
		int n = projector.getNumStates();

		I_z = CommonOps.identity(n);
		I_x = CommonOps.identity(m);
		R_inv = new DenseMatrix64F(n, n);
		temp0_m_m = new DenseMatrix64F(m, m);
		D = new DenseMatrix64F(m, m);
		temp0_m_n = new DenseMatrix64F(m, n);
		K = new DenseMatrix64F(m, n);
		temp0_m_1 = new DenseMatrix64F(m, 1);
		innovation = new DenseMatrix64F(n, 1);

		S_bar = new DenseMatrix64F(m, m);
		CommonOps.multAddTransA(L, S, temp0_m_m);
		CommonOps.mult(temp0_m_m, L, S_bar);
	}

	@Override
	public void predict(MultivariateGaussianDM state) {
		DenseMatrix64F x = state.getMean();
		DenseMatrix64F P = state.getCovariance();

		DenseMatrix64F F = predictor.getStateTransition();
		DenseMatrix64F Q = predictor.getPlantNoise();

		CommonOps.mult(F, x, temp0_m_1);
		x.set(temp0_m_1);

		if (control != null) {
			DenseMatrix64F G = predictor.getControlTransition();

			CommonOps.mult(G, control, temp0_m_1);
			CommonOps.add(x, temp0_m_1, x);
		}

		CommonOps.mult(F, P, temp0_m_m);
		CommonOps.multTransB(temp0_m_m, F, P);
		CommonOps.add(P, Q, P);
	}

	@Override
	public void update(MultivariateGaussianDM state,
					   MultivariateGaussianDM meas) {
		DenseMatrix64F x = state.getMean();
		DenseMatrix64F P = state.getCovariance();
		DenseMatrix64F H = projector.getProjectionMatrix();
		DenseMatrix64F R = meas.getCovariance();
		DenseMatrix64F y = meas.getMean();

		// compute D
		CommonOps.mult(-theta, S_bar, P, D);

		CommonOps.invert(R, R_inv);

		CommonOps.multTransA(H, R_inv, temp0_m_n);
		CommonOps.mult(temp0_m_n, H, temp0_m_m);
		CommonOps.mult(temp0_m_m, P, D);

		for (int i = 0; i < D.getNumCols(); i++)
			D.set(i, i, D.get(i, i) + 1);

		CommonOps.invert(D, temp0_m_m);
		DenseMatrix64F PD_inv = D;
		CommonOps.mult(P, temp0_m_m, PD_inv);

		// compute K
		CommonOps.multTransB(PD_inv, H, temp0_m_n);
		CommonOps.mult(temp0_m_n, R_inv, K);

		// innovation
		innovation.set(y);
		CommonOps.multAdd(-1, H, x, innovation);

		// update the state
		CommonOps.mult(K, innovation, temp0_m_1);
		CommonOps.add(x, temp0_m_1, x);

		// update the covariance
		P.set(PD_inv);
	}

	public KalmanProjector getProjector() {
		return projector;
	}

	public KalmanPredictor getPredictor() {
		return predictor;
	}

	public DenseMatrix64F getInnovation() {
		return innovation;
	}

	public int getStateDOF() {
		return predictor.getNumStates();
	}
}
