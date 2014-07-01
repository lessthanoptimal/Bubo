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

package bubo.filters.ekf;

import org.ejml.data.DenseMatrix64F;

/**
 * Converts a {@link EkfPredictorTime} into a {@link EkfPredictorDiscrete}.
 *
 * @author Peter Abeles
 */
public class TimeToDiscrete implements EkfPredictorDiscrete {
	// time period
	double T;

	EkfPredictorTime predictor;

	public TimeToDiscrete(EkfPredictorTime predictor, double t) {
		this.predictor = predictor;
		T = t;
	}

	@Override
	public void compute(DenseMatrix64F mean) {
		predictor.compute(mean, T);
	}

	@Override
	public DenseMatrix64F getJacobianF() {
		return predictor.getJacobianF();
	}

	@Override
	public DenseMatrix64F getPlantNoise() {
		return predictor.getPlantNoise();
	}

	@Override
	public DenseMatrix64F getPredictedState() {
		return predictor.getPredictedState();
	}

	@Override
	public int getSystemSize() {
		return predictor.getSystemSize();
	}
}
