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

package bubo.filters.specific.ekf;

import bubo.filters.ekf.EkfProjector;
import bubo.filters.kf.KalmanProjector;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * A wrapper around a KalmanProjector that allows it to be used inside of an EKF.
 */
public class KfToEkfProjector implements EkfProjector {
	private KalmanProjector proj;
	private DenseMatrix64F H;
	private DenseMatrix64F z_hat;

	public KfToEkfProjector(KalmanProjector proj) {
		this.proj = proj;
		H = proj.getProjectionMatrix();
		z_hat = new DenseMatrix64F(proj.getNumStates(), 1);
	}

	@Override
	public int getSystemSize() {
		return proj.getNumStates();
	}

	@Override
	public int getMeasurementSize() {
		return z_hat.numRows;
	}

	@Override
	public void compute(DenseMatrix64F state) {
		CommonOps.mult(H, state, z_hat);
	}

	@Override
	public DenseMatrix64F getJacobianH() {
		return H;
	}

	@Override
	public DenseMatrix64F getProjected() {
		return z_hat;
	}
}
