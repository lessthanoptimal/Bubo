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

import org.ejml.data.DenseMatrix64F;

/**
 * Computes matrices needed to propagate the estimated state forward in time.
 * <p/>
 * It is assumed that the dynamics does not change as a function of time.
 * <p/>
 * x(k+1) = F(k)x(k) + G(k)u(k) + v(k)
 * <p/>
 * v ~ N[0,R(k)]
 */
public interface KalmanPredictor {

	/**
	 * Returns a state transition matrix for the specified time step length.
	 * <p/>
	 * This is sometimes referred to as the F matrix.
	 */
	public DenseMatrix64F getStateTransition();

	/**
	 * This is an optional matrix.  The control input in a Kalman filter is assumed to be
	 * known and is not always used.  If this is not used then null should be returned.
	 * <p/>
	 * See G(k) above.
	 */
	public DenseMatrix64F getControlTransition();

	/**
	 * Amount of noise added to the system in the form of a covariance matrix.
	 *
	 * @return Covariance matrix
	 */
	public DenseMatrix64F getPlantNoise();

	/**
	 * Expected length of state vector
	 */
	public int getNumStates();

	/**
	 * The number of elements in the control input
	 */
	public int getNumControl();
}
