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

package jtarget.tracking.specific.filter.imm;

import bubo.filters.imm.InteractionMatrixInterface;
import org.ejml.data.DMatrixRMaj;

/**
 * This transition matrix is independent of the observation sojourn time.
 */
public class FixedTransitionMatrix
		implements InteractionMatrixInterface {
	private DMatrixRMaj pi;

	public FixedTransitionMatrix(DMatrixRMaj pi) {
		this.pi = pi;

		// make sure its a valid matrix
		for (int i = 0; i < pi.numRows; i++) {
			double total = 0;
			for (int j = 0; j < pi.numCols; j++) {
				total += pi.get(i, j);
			}
			if (Math.abs(total - 1) > 1e-5)
				throw new IllegalArgumentException("Bad matrix");
		}
	}

	@Override
	public DMatrixRMaj computeMatrix(double deltaTime) {
		return pi;
	}
}
