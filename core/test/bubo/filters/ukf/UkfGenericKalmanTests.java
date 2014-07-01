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

import bubo.filters.GenericKalmanFilterTests;
import bubo.filters.MultivariateGaussianDM;
import bubo.filters.abst.KalmanFilterInterface;
import bubo.filters.ekf.EkfPredictorDiscrete;
import bubo.filters.ekf.EkfProjector;
import bubo.filters.kf.ConstAccel1D;
import bubo.filters.kf.FixedKalmanProjector;
import bubo.filters.specific.ekf.KfToEkfPredictorDiscrete;
import bubo.filters.specific.ekf.KfToEkfProjector;
import org.ejml.data.DenseMatrix64F;

/**
 * Configure an UKF as a Kalman filter and compare the results.
 */
public class UkfGenericKalmanTests extends GenericKalmanFilterTests {

	@Override
	protected KalmanFilterInterface createFilter() {
		ConstAccel1D constAccelProp = new ConstAccel1D(1.0, 1);

		EkfPredictorDiscrete predictor = new KfToEkfPredictorDiscrete(constAccelProp, null);

		DenseMatrix64F H = new DenseMatrix64F(new double[][]{{1, 1, 1}, {0, 1, 2}});

		EkfProjector projector = new KfToEkfProjector(new FixedKalmanProjector(H));

		return new UnscentedKalmanFilter(0.3, true, true, predictor, projector);
	}

	@Override
	protected MultivariateGaussianDM createInitialState() {
		return createState(9.0, 2, 3, 4);
	}

	@Override
	protected DenseMatrix64F createTargetState() {
		return createState(9.0, 1, 1, 1).getMean();
	}

	@Override
	protected MultivariateGaussianDM createPerfectMeas(KalmanFilterInterface f,
													   DenseMatrix64F state) {
		UnscentedKalmanFilter filter = (UnscentedKalmanFilter) f;
		filter.getProjector().compute(state);

		DenseMatrix64F z = filter.getProjector().getProjected();

		return createState(2.0, z.get(0, 0), z.get(1, 0));
	}
}
