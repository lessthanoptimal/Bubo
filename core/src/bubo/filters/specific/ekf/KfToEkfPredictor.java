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

package bubo.filters.specific.ekf;

import bubo.filters.ekf.EkfPredictor;
import bubo.filters.kf.KalmanPredictor;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * A simple wrapper that lets a KF propagator be used as a propagator for an EKF.
 * If the model is not linear this is highly likely
 */
public class KfToEkfPredictor<Control> implements EkfPredictor<Control> {

	private KalmanPredictor<Control> predictor;
	private DenseMatrix64F controlInput;
	private DenseMatrix64F a;

	public KfToEkfPredictor(KalmanPredictor<Control> predictor, DenseMatrix64F controlInput) {
		this.predictor = predictor;
		this.a = new DenseMatrix64F(predictor.getNumStates(), 1);
		this.controlInput = controlInput;
	}

	@Override
	public void predict(DenseMatrix64F state, Control control, double elapsedTime) {
		predictor.compute(control,elapsedTime);
		DenseMatrix64F F = predictor.getStateTransition();
		DenseMatrix64F G = predictor.getControlTransition();

		// predict the state
		CommonOps.mult(F, state, a);

		// handle the control, if there is one
		if (G != null) {
			CommonOps.multAdd(G, controlInput, a);
		}
	}

	public DenseMatrix64F getControlInput() {
		return controlInput;
	}

	@Override
	public DenseMatrix64F getJacobianF() {
		return predictor.getStateTransition();
	}

	@Override
	public DenseMatrix64F getPlantNoise() {
		return predictor.getPlantNoise();
	}

	@Override
	public DenseMatrix64F getPredictedState() {
		return a;
	}

	@Override
	public int getSystemSize() {
		return predictor.getNumStates();
	}
}