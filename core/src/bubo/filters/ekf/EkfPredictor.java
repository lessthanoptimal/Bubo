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

package bubo.filters.ekf;

import org.ejml.data.DenseMatrix64F;

/**
 * A state propagator for an extended Kalman filter.
 */
public interface EkfPredictor {

	/**
	 * Returns the jacobian of f(x) with respect to x.
	 *
	 * @return State transition Jacobian matrix. WARNING DO NOT MODIFY!  DATA IS MODIFIED EACH TIME COMPUTE IS CALLED.
	 */
	public DenseMatrix64F getJacobianF();

	/**
	 * Returns the propagation noise.
	 *
	 * @return Plant noise covariance matrix. WARNING DO NOT MODIFY!  DATA IS MODIFIED EACH TIME COMPUTE IS CALLED.
	 */
	public DenseMatrix64F getPlantNoise();

	/**
	 * Returns the predicted state at the next time step.
	 *
	 * @return Predicted state column vector. WARNING DO NOT MODIFY!  DATA IS MODIFIED EACH TIME COMPUTE IS CALLED.
	 */
	public DenseMatrix64F getPredictedState();

	/**
	 * The number of states in the state vector which is being manipulated
	 */
	public int getSystemSize();
}
