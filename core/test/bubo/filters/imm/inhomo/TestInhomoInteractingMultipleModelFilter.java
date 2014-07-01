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

package bubo.filters.imm.inhomo;

import bubo.filters.GenericKalmanFilterTests;
import bubo.filters.MultivariateGaussianDM;
import bubo.filters.UtilMultivariateGaussian;
import bubo.filters.ekf.EkfPredictorDiscrete;
import bubo.filters.ekf.EkfProjector;
import bubo.filters.ekf.ExtendedKalmanFilter;
import bubo.filters.kf.ConstAccel1D;
import bubo.filters.kf.FixedKalmanPredictor;
import bubo.filters.kf.FixedKalmanProjector;
import bubo.filters.kf.KalmanPredictor;
import bubo.filters.specific.ekf.KfToEkfPredictorDiscrete;
import bubo.filters.specific.ekf.KfToEkfProjector;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestInhomoInteractingMultipleModelFilter {

	/**
	 * Create a predictor where nothing changes
	 */
	public static EkfPredictorDiscrete createStaticPredictor() {
		DenseMatrix64F F = CommonOps.identity(3);
		DenseMatrix64F Q = CommonOps.identity(3);
		KalmanPredictor kalmanPred = new FixedKalmanPredictor(F, null, Q);

		return new KfToEkfPredictorDiscrete(kalmanPred, null);
	}

	public static EkfPredictorDiscrete createPredictorN(int dof) {
		DenseMatrix64F F = CommonOps.identity(dof);
		DenseMatrix64F Q = CommonOps.identity(dof);
		KalmanPredictor kalmanPred = new FixedKalmanPredictor(F, null, Q);

		return new KfToEkfPredictorDiscrete(kalmanPred, null);
	}

	public static EkfProjector createProjectorN(int dof) {
		DenseMatrix64F H = new DenseMatrix64F(1, dof);
		H.set(0, 0, 1.0);
		FixedKalmanProjector kfProj = new FixedKalmanProjector(H);

		return new KfToEkfProjector(kfProj);
	}

	/**
	 * Compare the IMM that has the exact same model for each state against a
	 * Kalman filter with the exact same model.  It also makes sure the
	 * model weights are infact changing as the filter predicts and updates.
	 */
	@Test
	public void compareToKalmanFilter() {
		CompareIhImmToKalman compare = new CompareIhImmToKalman();

		compare.compareToKalman();
	}

	/**
	 * Two internal models are provided with different state vector sizes.
	 * This sees if the filter blows up when that is done.
	 */
	@Test
	public void checkBlowUp() {
		InhomoInteractingMultipleModel imm = createDiffIMM();
		IhImmState state = new IhImmState(imm);

		MultivariateGaussianDM z = GenericKalmanFilterTests.createState(2.0, 1);

		state.setState(0, UtilMultivariateGaussian.createDummy(2, 1), 0.4);
		state.setState(1, UtilMultivariateGaussian.createDummy(3, 1), 0.6);

		imm.predict(state);
		imm.update(state, z);

		state.computeMOG();
	}

	/**
	 * Provide two models and then provide only measurements from one of the
	 * models.  The probabilistic weight should sift towards the correct model.
	 */
	@Test
	public void checkConvergence() {
		InhomoInteractingMultipleModel imm = createConvergeIMM();
		IhImmState state = new IhImmState(imm);

		MultivariateGaussianDM initState = createInitState();
		MultivariateGaussianDM z = GenericKalmanFilterTests.createState(2.0, 0, 2, 1.5);

		double initWeights[] = new double[]{0.5, 0.5};
		setHomoState(state, initState, initWeights);

		for (int i = 0; i < 20; i++) {
			imm.predict(state);
			imm.update(state, z);
		}

		assertTrue(state.getHypothesis(1).getProbability() > 0.9);
	}

	private void setHomoState(IhImmState stateImm,
							  MultivariateGaussianDM state,
							  double initWeights[]) {
		int numModels = stateImm.getNumHypotheses();

		for (int i = 0; i < numModels; i++) {
			stateImm.setState(i, state, initWeights[i]);
		}
	}

	private MultivariateGaussianDM createInitState() {
		MultivariateGaussianDM ret = new MultivariateGaussianDM(3);

		ret.getCovariance().set(CommonOps.identity(3));
		ret.getMean().set(1, 0, 2);
		ret.getMean().set(2, 0, 1.5);

		return ret;
	}

	/**
	 * Create an IMM with two models.  one is static and the other moves.
	 */
	private InhomoInteractingMultipleModel createConvergeIMM() {
		DenseMatrix64F H = CommonOps.identity(3);
		FixedKalmanProjector kfProj = new FixedKalmanProjector(H);

		EkfPredictorDiscrete predMove = new KfToEkfPredictorDiscrete(new ConstAccel1D(1, 1), null);
		EkfPredictorDiscrete predStatic = createStaticPredictor();

		ExtendedKalmanFilter filters[] = new ExtendedKalmanFilter[2];
		filters[0] = new ExtendedKalmanFilter(predMove, new KfToEkfProjector(kfProj));
		filters[1] = new ExtendedKalmanFilter(predStatic, new KfToEkfProjector(kfProj));

		DenseMatrix64F pi = new DenseMatrix64F(new double[][]{{0.95, 0.05}, {0.05, 0.95}});

		InternalStateConverter converter = new TrivialInternalStateConverter(3);

		return new InhomoInteractingMultipleModel(converter, filters, pi);
	}

	/**
	 * Create an IMM with two models with different state vector size.
	 * one is static and the other moves.
	 */
	private InhomoInteractingMultipleModel createDiffIMM() {

		ExtendedKalmanFilter filters[] = new ExtendedKalmanFilter[2];
		filters[0] = new ExtendedKalmanFilter(createPredictorN(2), createProjectorN(2));
		filters[1] = new ExtendedKalmanFilter(createPredictorN(3), createProjectorN(3));

		DenseMatrix64F pi = new DenseMatrix64F(new double[][]{{0.95, 0.05}, {0.05, 0.95}});

		TruncatelInternalStateConverter converter = new TruncatelInternalStateConverter(2, 3);
		converter.setDefault(UtilMultivariateGaussian.createDummy(3, 0));
		converter.setModelDimensions(2, 2, 3);

		return new InhomoInteractingMultipleModel(converter, filters, pi);
	}
}