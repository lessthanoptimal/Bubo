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

package bubo.filters.ukf;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.abst.KalmanFilterInterface;
import bubo.filters.ekf.EkfPredictor;
import bubo.filters.ekf.EkfProjector;
import bubo.filters.ekf.ExtendedKalmanFilter;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.ops.CommonOps;

/**
 * An implementation of the Unscented Kalman Filter based on the description in this paper:
 * <p/>
 * Simon Julier, Jeffrey Uhlmann, and Hugh F. Durrant-Whyte "A New Method for the Nonlinear Transformation
 * of Means and Covariances in Filters and Estimators"  IEEE AES Vol. 45, No 3, March 2000
 * <p/>
 * and their 1997 paper.
 * <p/>
 * ---------------------------
 * <p/>
 * The n-dimensional random variable x(k) with mean \hat{x}(k|k) and covariance P(k|k) is approximated
 * by 2n+1 weighted samples or sigma points selected by the algorithm.
 * <p/>
 * In this implementation the unscented transform can be applied to the prediction and update steps
 * independently.  If the unscented tranform is not used the extended Kalman filter equations are used
 * instead.
 * <p/>
 * The following system equations are assumed:
 * <p/>
 * x(k+1) = f( x(k) , u(k) ) + w(k)
 * z(k)   = h( x(k) ) + v(k)
 * <p/>
 * where w(k) and v(k) are independent white Gaussian noise processes.
 * <p/>
 * *note* This is a straight forward implementation.  There are ways to slightly improve performance (some times
 * at the cost of accuracy) that have not been used.
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class UnscentedKalmanFilter<Control> implements KalmanFilterInterface<Control> {

	// should it perform an unscented update on the prediction and/or update?
	boolean unscentedPred;
	boolean unscentedUpdate;
	// tuning parameters
	private double kappa;
	// the DOF of the state
	private int n;
	// target and sensor models
	private EkfPredictor<Control> predictor;
	private EkfProjector projector;

	// sample points
	private DenseMatrix64F points[];
	private DenseMatrix64F measPoints[];
	private double weights[];
	// used to handle kappa's less than 0
	// this is the first index in the points list thats used
	private int firstCovIndex;

	// used to update the state when the unscented transform has not been requested
	private ExtendedKalmanFilter<Control> ekf;

	// used during the unscented prediction and update steps
	private DenseMatrix64F tempMxM;
	private DenseMatrix64F temp0_Nx1;
	private DenseMatrix64F temp1_Nx1;
	private DenseMatrix64F temp0_NxN;
	private DenseMatrix64F temp1_NxN;
	private DenseMatrix64F temp0_MxN;
	private DenseMatrix64F temp1_MxN;

	// used to compute the matrix square root
	private CholeskyDecomposition<DenseMatrix64F> choleky;
	private DenseMatrix64F tempL;

	/**
	 * @param kappa           a tuning parameter the effects the distribution of sample points. kappa \in Re
	 * @param unscentedPred   If true an unscented transform will be applied to the prediction
	 * @param unscentedUpdate If true an unscented transform will be applied to the update
	 * @param predictor       Used to predict the state of the system at a future time
	 * @param projector       Creates a measurement from the given state.
	 */
	public UnscentedKalmanFilter(double kappa,
								 boolean unscentedPred,
								 boolean unscentedUpdate,
								 EkfPredictor<Control> predictor,
								 EkfProjector projector) {
		if (!unscentedPred && !unscentedUpdate)
			throw new IllegalArgumentException("No point in using the unscented filter if it " +
					"is turned off for both prediction and update");

		this.kappa = kappa;
		this.predictor = predictor;
		this.projector = projector;
		this.unscentedPred = unscentedPred;
		this.unscentedUpdate = unscentedUpdate;
		this.n = predictor.getSystemSize();

		if (kappa <= 0) {
			firstCovIndex = 1;
		} else {
			firstCovIndex = 0;
		}

		weights = new double[2 * n + 1];

		// get the DOF of the state and measurement
		int M = predictor.getSystemSize();
		int N = projector.getSystemSize();


		points = new DenseMatrix64F[2 * n + 1];
		for (int i = 0; i < points.length; i++) {
			points[i] = new DenseMatrix64F(M, 1);
		}

		if (unscentedUpdate) {
			measPoints = new DenseMatrix64F[2 * n + 1];
			for (int i = 0; i < measPoints.length; i++) {
				measPoints[i] = new DenseMatrix64F(N, 1);
			}
		}

		if (!unscentedPred || !unscentedUpdate) {
			ekf = new ExtendedKalmanFilter<Control>(predictor, projector);
		}

		weights[0] = kappa / (n + kappa);
		double w = 1.0 / (2.0 * (n + kappa));
		for (int i = 1; i <= n; i++) {
			weights[i] = w;
			weights[i + n] = w;
		}

		choleky = DecompositionFactory.chol(M, true);
		tempL = new DenseMatrix64F(M, M);

		declareTemporary(M, N);
	}

	private void declareTemporary(int M, int N) {
		tempMxM = new DenseMatrix64F(M, M);
		temp0_Nx1 = new DenseMatrix64F(N, 1);
		temp1_Nx1 = new DenseMatrix64F(N, 1);
		temp0_NxN = new DenseMatrix64F(N, N);
		temp1_NxN = new DenseMatrix64F(N, N);
		temp0_MxN = new DenseMatrix64F(M, N);
		temp1_MxN = new DenseMatrix64F(M, N);
	}

	public EkfProjector getProjector() {
		return projector;
	}

	public EkfPredictor getPredictor() {
		return predictor;
	}


	/**
	 * Intelligently sample the points of the distribution
	 */
	private void setPoints(MultivariateGaussianDM output) {
		DenseMatrix64F x = output.getMean();
		DenseMatrix64F P = output.getCovariance();

		tempMxM.set(P);
		CommonOps.scale(n + kappa, tempMxM);

		tempL.set(tempMxM);
		if (!choleky.decompose(tempL))
			throw new RuntimeException("Cholesky failed");

		DenseMatrix64F A = choleky.getT(null);
//        UtilMtjMatrix.print(A,null);

		points[0].set(x);

		for (int i = 1; i <= n; i++) {

			DenseMatrix64F pA = points[i];
			DenseMatrix64F pB = points[i + n];

			for (int j = 0; j < x.numRows; j++) {
				pA.set(j, 0, x.get(j, 0) + A.get(j, i - 1));
				pB.set(j, 0, x.get(j, 0) - A.get(j, i - 1));
			}
		}
	}

	/**
	 * Predicts the state of the system at the end of the next time step.
	 *
	 * @param state Initially its the current state of the system.  After the function exits,
	 *              it is the predicted state of the system
	 */
	@Override
	public void predict(MultivariateGaussianDM state, Control control, double elapsedTime) {
		if (unscentedPred) {
			predictUnscented(state,control,elapsedTime);
		} else {
			ekf.predict(state,control,elapsedTime);
		}
	}

	/**
	 * Updates the state using the specified measurement.
	 *
	 * @param state The current state of the system.  When the function exits, it is the updated state
	 *              of the system.
	 * @param meas  The measurement which will be used to update the system.
	 */
	public void update(MultivariateGaussianDM state, MultivariateGaussianDM meas) {
		if (unscentedUpdate) {
			updateUnscented(state, meas);
		} else {
			ekf.update(state, meas);
		}
	}

	/**
	 * Predicts the state unsing the unscented transforme
	 */
	private void predictUnscented(MultivariateGaussianDM state, Control control, double elapsedTime) {
		setPoints(state);

		DenseMatrix64F x = state.getMean();
		DenseMatrix64F P = state.getCovariance();

		x.zero();

		// compute the state
		for (int i = 0; i < points.length; i++) {
			DenseMatrix64F p = points[i];

			predictor.predict(p,control,elapsedTime);
			p.set(predictor.getPredictedState());

			CommonOps.add(x, weights[i], p, x);
		}

		// compute the covariance with plant noise
		predictor.predict(x, control, elapsedTime);
		P.set(predictor.getPlantNoise());

		for (int i = firstCovIndex; i < points.length; i++) {
			DenseMatrix64F p = points[i];

			CommonOps.add(p, -1, x, p);
			CommonOps.multAddTransB(weights[i], p, p, P);
		}

	}

	/**
	 * Updates the state with the measurement and using the unscented transform
	 */
	private void updateUnscented(MultivariateGaussianDM state, MultivariateGaussianDM meas) {
		setPoints(state);

		DenseMatrix64F x = state.getMean();
		DenseMatrix64F P = state.getCovariance();

		DenseMatrix64F z = meas.getMean();
		DenseMatrix64F R = meas.getCovariance();

		// compute predicted measurement for each point as well as the weighted
		// average of all the predited points
		DenseMatrix64F z_hat = computePredictedMeas();

		// estimate predicted measurement covariance
		DenseMatrix64F Pz = computePredMeasCov(R, z_hat);

		// estimate the cross covariance
		DenseMatrix64F Pxz = computeCrossCov(x);

		// perform the unscented update

		// K = Pxz Pz^{-1}
		DenseMatrix64F K = temp1_MxN;
		CommonOps.invert(Pz, temp1_NxN);
		CommonOps.mult(Pxz, temp1_NxN, K);

		// x = x + K*(z-\hat{z})
		temp1_Nx1.set(z);
		CommonOps.add(temp1_Nx1, -1, z_hat, temp1_Nx1);
		CommonOps.multAdd(K, temp1_Nx1, x);

		// P = P - K*PzK'
		CommonOps.mult(K, Pz, temp0_MxN);
		CommonOps.multAddTransB(-1, temp0_MxN, K, P);
	}

	private DenseMatrix64F computeCrossCov(DenseMatrix64F x) {
		// Pxz = sum(i=0:2n+1) ( w_i (x_i - x)(z_ihat - z_hat)' )
		DenseMatrix64F Pxz = temp0_MxN;
		Pxz.zero();
		for (int i = firstCovIndex; i < measPoints.length; i++) {
			//
			DenseMatrix64F pX = points[i];
			CommonOps.add(pX, -1, x, pX);
			DenseMatrix64F pZ = measPoints[i];

			CommonOps.multAddTransB(weights[i], pX, pZ, Pxz);
		}
		return Pxz;
	}

	private DenseMatrix64F computePredMeasCov(DenseMatrix64F r, DenseMatrix64F z_hat) {
		DenseMatrix64F Pz = temp0_NxN;
		Pz.set(r);

		// Pz = sum(i=0:2n+1) ( w_i (z_ihat - z_hat)(...)' )
		for (int i = firstCovIndex; i < measPoints.length; i++) {
			DenseMatrix64F z_ihat = measPoints[i];
			CommonOps.add(z_ihat, -1, z_hat, z_ihat);

			CommonOps.multAddTransB(weights[i], z_ihat, z_ihat, Pz);
		}
		return Pz;
	}

	private DenseMatrix64F computePredictedMeas() {
		DenseMatrix64F z_hat = temp0_Nx1;
		z_hat.zero();
		for (int i = 0; i < points.length; i++) {
			DenseMatrix64F p = points[i];

			projector.compute(p);
			DenseMatrix64F z_ihat = projector.getProjected();
			measPoints[i].set(z_ihat);
			CommonOps.add(z_hat, weights[i], z_ihat, z_hat);
		}
		return z_hat;
	}
}
