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

package bubo.filters.imm.inhomo;

import bubo.filters.MultivariateGaussianDM;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * Assumes that the states can be converted from one to the other by simply truncating each other.
 * If asked to convert from a lower dimension to a higher dimension it copies elements from the
 * default state vector.
 */
public class TruncatelInternalStateConverter implements InternalStateConverter {

	// the default state vector.  This is used when a state of smaller dimension
	// is being converted to one of high dimension
	private MultivariateGaussianDM def;

	// the minimum number of dimension
	private int min;
	// predefined matrices for state and covariance
	private DenseMatrix64F states[];
	private DenseMatrix64F cov[];

	// number of DOF in the output state
	private int outputDimen;
	// the dimension of internal filters
	private int internalDimen[];

	/**
	 * @param min the minimum number of DOF the internal models will have (inclusive)
	 * @param max the maximum number of DOF the internal models will have (inclusive)
	 */
	public TruncatelInternalStateConverter(int min, int max) {
		states = new DenseMatrix64F[max - min + 1];
		cov = new DenseMatrix64F[max - min + 1];

		for (int i = min; i <= max; i++) {
			states[i - min] = new DenseMatrix64F(i, 1);
			cov[i - min] = new DenseMatrix64F(i, i);
		}
		this.min = min;
	}

	/**
	 * @param def The state which will be used as a default value.  A copy is made.
	 */
	public void setDefault(MultivariateGaussianDM def) {
		this.def = def.copy();
	}

	public void setModelDimensions(int outputDimen, int... internal) {
		this.outputDimen = outputDimen;
		this.internalDimen = new int[internal.length];

		System.arraycopy(internal, 0, internalDimen, 0, internal.length);
	}


	@Override
	public DenseMatrix64F convertMergeFrom(boolean isMean,
										   DenseMatrix64F fromState,
										   int fromID, int targetID) {

		int targetDimen = internalDimen[targetID];
		int fromDimen = internalDimen[fromID];

		return _convert(isMean, fromState, fromDimen, targetDimen);
	}

	@Override
	public DenseMatrix64F convertOutput(boolean isMean, DenseMatrix64F fromState, int fromID) {
		int fromDimen = internalDimen[fromID];

		return _convert(isMean, fromState, fromDimen, outputDimen);
	}

	@Override
	public int getOutputDimen() {
		return outputDimen;
	}

	/**
	 * Copies over the elements in the matrix from the from state.  If it needs more
	 * it uses the default state to fill in the holes.
	 */
	private DenseMatrix64F _convert(boolean isMean,
									DenseMatrix64F fromState,
									int fromDimen, int targetDimen) {
		DenseMatrix64F a = isMean ? states[targetDimen - min] : cov[targetDimen - min];

		if (targetDimen <= fromDimen) {
			int maxX = isMean ? 1 : targetDimen;
			CommonOps.insert(fromState, a, targetDimen, maxX);
//            UtilMtjMatrix.copySubMatrix(fromState,a,maxX,targetDimen);
		} else {
			int maxX = isMean ? 1 : fromDimen;
			DenseMatrix64F def = isMean ? this.def.getMean() : this.def.getCovariance();
			CommonOps.insert(fromState, a, fromDimen, maxX);
			throw new RuntimeException("Need to implement copyExcludeStill");
//            UtilMtjMatrix.copySubMatrix(fromState,a,maxX,fromDimen);
//            UtilMtjMatrix.copyExcludeSubMatrix(def,a,maxX,fromDimen);
		}

		return a;
	}
}