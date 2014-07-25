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
import bubo.filters.ekf.EkfPredictor;
import bubo.filters.ekf.EkfProjector;
import bubo.filters.imm.CompareToKalmanTests;
import bubo.filters.kf.KalmanPredictor;
import bubo.filters.kf.KalmanProjector;
import bubo.filters.specific.ekf.KfToEkfPredictor;
import bubo.filters.specific.ekf.KfToEkfProjector;

/**
 * Used to compare the results from a UKF to an equivalent KF
 */
public class UkfCompareToKalmanTests extends CompareToKalmanTests {
	UnscentedKalmanFilter ukf;
	MultivariateGaussianDM state;
	double kappa;

	public UkfCompareToKalmanTests(double stateTol, double covTol, double kappa) {
		super(stateTol, covTol);
		this.kappa = kappa;
	}

	@Override
	protected void createOtherFilter() {
		KalmanProjector kfProj = createProjector();
		KalmanPredictor kfPred = createPredictor();

		EkfPredictor pred = new KfToEkfPredictor(kfPred, null);
		EkfProjector proj = new KfToEkfProjector(kfProj);

		ukf = new UnscentedKalmanFilter(kappa, true, true, pred, proj);
	}

	@Override
	protected void predictOther() {
		ukf.predict(state,null,-1);
	}

	@Override
	protected void updateOther(MultivariateGaussianDM meas) {
		ukf.update(state, meas);
	}

	@Override
	protected void setOtherPriorState(MultivariateGaussianDM prior) {
		this.state = prior.copy();
	}

	@Override
	protected MultivariateGaussianDM computeOutput() {
		return state;
	}
}
