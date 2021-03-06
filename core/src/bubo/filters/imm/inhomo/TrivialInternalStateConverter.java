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

package bubo.filters.imm.inhomo;

import org.ejml.data.DMatrixRMaj;

/**
 * Just returns the same state as the one that was passed in to it.
 */
public class TrivialInternalStateConverter implements InternalStateConverter {

	private int outputDOF;

	public TrivialInternalStateConverter(int outputDOF) {
		this.outputDOF = outputDOF;
	}

	@Override
	public DMatrixRMaj convertMergeFrom(boolean isMean, DMatrixRMaj fromState, int fromID, int target) {
		return fromState;
	}

	@Override
	public DMatrixRMaj convertOutput(boolean isMean, DMatrixRMaj input, int type) {
		return input;
	}

	@Override
	public int getOutputDimen() {
		return outputDOF;
	}
}
