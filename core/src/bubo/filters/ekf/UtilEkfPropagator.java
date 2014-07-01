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
import org.ejml.ops.CommonOps;

/**
 * Utility functions related to EkfPropagator
 *
 * @author Peter Abeles
 */
public class UtilEkfPropagator {

	/**
	 * Computes the jacobian numerically.
	 *
	 * @param initX The point the jacobian is computed around.
	 * @param prop  the proagator
	 * @param T     the time step that the state is propagated forward
	 * @param delta How much the state changes should be adjusted
	 * @return
	 */
	public static DenseMatrix64F numericalJacobian(DenseMatrix64F initX, EkfPredictorTime prop, double T, double delta) {
		int N = prop.getSystemSize();

		DenseMatrix64F a = new DenseMatrix64F(N, 1);
		DenseMatrix64F b = new DenseMatrix64F(initX);
		DenseMatrix64F F = new DenseMatrix64F(N, N);

		prop.compute(initX, T);
		a.set(prop.getPredictedState());

		for (int i = 0; i < N; i++) {
			b.set(i, 0, initX.get(i, 0) + delta);

			prop.compute(b, T);
			b.set(prop.getPredictedState());
			CommonOps.add(b, -1, a, b);


			for (int j = 0; j < N; j++) {
				F.set(j, i, b.get(j, 0) / delta);
			}
			b.set(initX);
		}

		return F;
	}
}
