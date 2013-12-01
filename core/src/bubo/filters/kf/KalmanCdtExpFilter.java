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

import bubo.filters.MultivariateGaussianDM;

/**
 * An implementation of the continuous-discrete Kalman filter where there is an
 * explicit solution to the prediction equations.
 * <p/>
 * In a continuous discrete system the state is a continuous time process and the measurements
 * are discrete.  Practically this means that the prediction state has a time component
 * that was not available in the discrete time version.  If there is no explicit solution
 * to the prediction equations then they need to be numerically integrated, which this
 * filter can't do.
 *
 * @See DiscreteKalmanFilter
 */
public class KalmanCdtExpFilter
		extends DiscreteKalmanFilter
		implements KalmanCdtFilterInterface {
	KalmanCdtExpPredictor pred;

	public KalmanCdtExpFilter(KalmanCdtExpPredictor predictor,
							  KalmanProjector projector) {
		super(predictor, projector);
		this.pred = predictor;
	}

	@Override
	public void predict(MultivariateGaussianDM state, double T) {
		pred.compute(T);
		super.predict(state);
	}
}
