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

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.ekf.EkfProjector;
import bubo.filters.ekf.ExtendedKalmanFilter;
import bubo.filters.imm.CompareToKalmanTests;
import bubo.filters.kf.KalmanPredictor;
import bubo.filters.kf.KalmanProjector;
import bubo.filters.specific.ekf.KfToEkfPredictor;
import bubo.filters.specific.ekf.KfToEkfProjector;
import org.ejml.data.DMatrixRMaj;

import static bubo.filters.imm.TestInteractingMultipleModelFilter.createTransitionMatrix;

/**
 * See if a properly configured InhomoInteractingMultipleModel filter produces approximately
 * the same results as a kalman filter.
 */
public class CompareIhImmToKalman extends CompareToKalmanTests {

	InhomoInteractingMultipleModel imm;
	IhImmState stateImm;

	public CompareIhImmToKalman() {
		super(0.001, 0.1);
	}

	@Override
	protected void createOtherFilter() {
		int numModels = 3;

		KalmanProjector kfProj = createProjector();
		KalmanPredictor kfPred = createPredictor();

		KfToEkfPredictor pred = new KfToEkfPredictor(kfPred, null);
		EkfProjector proj = new KfToEkfProjector(kfProj);

		ExtendedKalmanFilter filters[] = new ExtendedKalmanFilter[numModels];
		for (int i = 0; i < numModels; i++) {
			filters[i] = new ExtendedKalmanFilter(pred, proj);
		}

		DMatrixRMaj pi = createTransitionMatrix(numModels);

		InternalStateConverter converter = new TrivialInternalStateConverter(3);

		imm = new InhomoInteractingMultipleModel(converter, filters, pi);
		stateImm = new IhImmState(imm);
	}

	@Override
	protected void predictOther() {
		imm.predict(stateImm,null,-1);
	}

	@Override
	protected void updateOther(MultivariateGaussianDM meas) {
		imm.update(stateImm, meas);
	}

	@Override
	protected void setOtherPriorState(MultivariateGaussianDM prior) {
		double initWeights[] = new double[]{0.4, 0.5, 0.1};

		int numModels = imm.getNumModels();

		for (int i = 0; i < numModels; i++) {
			stateImm.setState(i, prior, initWeights[i]);
		}
	}

	@Override
	protected MultivariateGaussianDM computeOutput() {
		return stateImm.computeMOG();
	}
}
