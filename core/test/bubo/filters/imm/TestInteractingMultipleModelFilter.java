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

import bubo.filters.GenericKalmanFilterTests;
import bubo.filters.MultivariateGaussianDM;
import bubo.filters.UtilMultivariateGaussian;
import bubo.filters.ekf.EkfPredictor;
import bubo.filters.ekf.EkfProjector;
import bubo.filters.ekf.ExtendedKalmanFilter;
import bubo.filters.kf.*;
import bubo.filters.specific.ekf.KfToEkfPredictor;
import bubo.filters.specific.ekf.KfToEkfProjector;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestInteractingMultipleModelFilter {

	private static KalmanFilter createKalman() {
		KalmanPredictor pred = createPred();

		DMatrixRMaj H = new DMatrixRMaj(new double[][]{{1, 0, 0}});
		FixedKalmanProjector projector = new FixedKalmanProjector(H);

		return new KalmanFilter(pred, projector);
	}

	private static KalmanPredictor createPred() {

		return new ConstAccel1D(1, 1);
	}

	/**
	 * Create an IMM that will produce the same output as a Kalman filter because it has
	 * three models which are identical.
	 */
	public static InteractingMultipleModelFilter createEquivIMM() {
		int numModels = 3;

		DMatrixRMaj H = new DMatrixRMaj(new double[][]{{1, 0, 0}});
		FixedKalmanProjector kfProj = new FixedKalmanProjector(H);

		KfToEkfPredictor pred = new KfToEkfPredictor(createPred(), null);
		EkfProjector proj = new KfToEkfProjector(kfProj);

		KfToEkfPredictor preds[] = new KfToEkfPredictor[numModels];
		for (int i = 0; i < numModels; i++) {
			preds[i] = pred;
		}

		ExtendedKalmanFilter filter = new ExtendedKalmanFilter(3, 1);
		filter.setProjector(proj);

		DMatrixRMaj pi = createTransitionMatrix(numModels);

		return new ImmCheckingFilter(filter, preds, pi);
	}

	/**
	 * Creates a Markov state transition matrix.  It creates an asymetric
	 * matrix for three models.  This is designed for the Kalman filter
	 * equivalence test where all the models are the same.
	 */
	public static DMatrixRMaj createTransitionMatrix(int numModels) {
		DMatrixRMaj pi = new DMatrixRMaj(numModels, numModels);
		double a = 0.2 / (numModels - 1);
		for (int i = 0; i < numModels; i++) {
			int b = 0;
			for (int j = 0; j < numModels; j++) {
				if (i == j) {
					pi.set(i, i, 0.8);
				} else {
					// give it different values so the matrix isn't symetric
					// which will hide some common errors
					if (b == 0) {
						pi.set(i, j, a * 1.5);
					} else if (b == 1) {
						pi.set(i, j, a * 0.5);
					} else {
						pi.set(i, j, a);
					}
					b++;
				}
			}
		}
		return pi;
	}

	/**
	 * Create a predictor where nothing changes
	 */
	public static EkfPredictor createStaticPredictor() {
		DMatrixRMaj F = CommonOps_DDRM.identity(3);
		DMatrixRMaj Q = CommonOps_DDRM.identity(3);
		KalmanPredictor kalmanPred = new FixedKalmanPredictor(F, null, Q);

		return new KfToEkfPredictor(kalmanPred, null);
	}

	/**
	 * Compare the IMM that has the exact same model for each state against a
	 * Kalman filter with the exact same model.  It also makes sure the
	 * model weights are infact changing as the filter predicts and updates.
	 */
	@Test
	public void compareToKalmanFilter() {
		KalmanFilter kf = createKalman();
		InteractingMultipleModelFilter imm = createEquivIMM();

		double initWeights[] = new double[]{0.4, 0.5, 0.1};
		ImmState immState = new ImmState(imm);
		immState.setState(createInitState(), initWeights);
		MultivariateGaussianDM kfState = createInitState();

		// perform the prediction step
		kf.predict(kfState,null,-1);
		imm.predict(immState,null,-1);

		assertTrue(UtilMultivariateGaussian.isSimilar(kfState, immState.computeMOG()));
		assertTrue(didModelWeightsChange(initWeights, immState.hypotheses));
		// save the changes
		for (int i = 0; i < initWeights.length; i++) {
			initWeights[i] = immState.hypotheses[i].getProbability();
		}

		// perform the update state
		MultivariateGaussianDM z = GenericKalmanFilterTests.createState(2.0, 2.4);
		kf.update(kfState, z);
		imm.update(immState, z);

		assertTrue(UtilMultivariateGaussian.isSimilar(kfState, immState.computeMOG()));
		// all the models performed just as well so their weights should not change
		assertFalse(didModelWeightsChange(initWeights, immState.hypotheses));
	}

	/**
	 * Provide two models and then provide only measurements from one of the
	 * models.  The probabilistic weight should sift towards the correct model.
	 */
	@Test
	public void checkConvergence() {
		InteractingMultipleModelFilter imm = createConvergeIMM();

		MultivariateGaussianDM initState = createInitState();
		MultivariateGaussianDM z = GenericKalmanFilterTests.createState(2.0, 0, 2, 1.5);

		double initWeights[] = new double[]{0.5, 0.5};
		ImmState state = new ImmState(imm);
		state.setState(initState, initWeights);

		for (int i = 0; i < 20; i++) {
			imm.predict(state,null,-1);
			imm.update(state, z);
		}

		assertTrue(state.hypotheses[1].getProbability() > 0.9);
	}

	/**
	 * Returns true if the weights are significantly different from each other
	 */
	private boolean didModelWeightsChange(double[] orig, ImmHypothesis models[]) {
		for (int i = 0; i < orig.length; i++) {
			if (Math.abs(orig[i] - models[i].getProbability()) > 1e-8) {
				return true;
			}
		}
		return false;
	}

	private MultivariateGaussianDM createInitState() {
		MultivariateGaussianDM ret = new MultivariateGaussianDM(3);

		ret.getCovariance().set(CommonOps_DDRM.identity(3));
		ret.getMean().set(1, 0, 2);
		ret.getMean().set(2, 0, 1.5);

		return ret;
	}

	/**
	 * Create an IMM with two models.  one is static and the other moves.
	 */
	private InteractingMultipleModelFilter createConvergeIMM() {
		DMatrixRMaj H = CommonOps_DDRM.identity(3);
		FixedKalmanProjector kfProj = new FixedKalmanProjector(H);

		EkfPredictor predMove = new KfToEkfPredictor(createPred(), null);
		EkfPredictor predStatic = createStaticPredictor();
		EkfProjector proj = new KfToEkfProjector(kfProj);

		EkfPredictor preds[] = new EkfPredictor[2];
		preds[0] = predMove;
		preds[1] = predStatic;

		ExtendedKalmanFilter filter = new ExtendedKalmanFilter(3, 3);
		filter.setProjector(proj);

		DMatrixRMaj pi = new DMatrixRMaj(new double[][]{{0.95, 0.05}, {0.05, 0.95}});

		return new ImmCheckingFilter(filter, preds, pi);
	}
}
