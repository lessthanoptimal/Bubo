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

package bubo.filters.kf;

import org.ejml.data.DenseMatrix64F;

/**
 * This KalmanPropagator always returns the same matrices no matter what the time step is.
 * Primarily used for debugging.
 */
public class FixedKalmanPredictor implements KalmanPredictor {
	private DenseMatrix64F F;
	private DenseMatrix64F G;
	private DenseMatrix64F Q;

	public FixedKalmanPredictor(DenseMatrix64F F, DenseMatrix64F G, DenseMatrix64F Q) {
		this.F = F;
		this.Q = Q;
		this.G = G;
	}

	@Override
	public void compute(Object o, double elapsedTime) {}

	@Override
	public DenseMatrix64F getStateTransition() {
		return F;
	}

	@Override
	public DenseMatrix64F getControlTransition() {
		return G;
	}

	public void setControlTransition(DenseMatrix64F G) {
		this.G = G;
	}

	@Override
	public DenseMatrix64F getPlantNoise() {
		return Q;
	}

	@Override
	public int getNumStates() {
		return Q.numCols;
	}

	@Override
	public int getNumControl() {
		if (G == null)
			return 0;
		else
			return G.numCols;
	}
}
