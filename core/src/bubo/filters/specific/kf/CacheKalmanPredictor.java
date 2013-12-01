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

package bubo.filters.specific.kf;

import bubo.filters.kf.KalmanCdtExpPredictor;
import org.ejml.data.DenseMatrix64F;

/**
 * Checks to see if the delta time has changed, if so it recomputes everything, otherwise it
 * uses the previous value.
 */
public abstract class CacheKalmanPredictor implements KalmanCdtExpPredictor {

	private double prevDeltaTime = -1;
	protected DenseMatrix64F tran;
	protected DenseMatrix64F control;
	protected DenseMatrix64F plant;

	public CacheKalmanPredictor(int stateDimen, int controlDimen) {
		tran = new DenseMatrix64F(stateDimen, stateDimen);
		plant = new DenseMatrix64F(stateDimen, stateDimen);
		control = new DenseMatrix64F(stateDimen, controlDimen);
	}

	public CacheKalmanPredictor(int stateDimen) {
		tran = new DenseMatrix64F(stateDimen, stateDimen);
		plant = new DenseMatrix64F(stateDimen, stateDimen);
	}

	public void compute(double deltaTime) {
		if (this.prevDeltaTime != deltaTime) {
			_compute(deltaTime);
			prevDeltaTime = deltaTime;
		}
	}

	/**
	 * If the delta time is different this is called and the matrices should be recomputed
	 */
	protected abstract void _compute(double deltaTime);

	public DenseMatrix64F getStateTransition() {
		return tran;
	}

	public DenseMatrix64F getControlTransition() {
		return control;
	}

	public DenseMatrix64F getPlantNoise() {
		return plant;
	}

	public int getNumStates() {
		return tran.getNumCols();
	}

	@Override
	public int getNumControl() {
		if (control == null)
			return 0;
		else
			return control.getNumCols();
	}
}
