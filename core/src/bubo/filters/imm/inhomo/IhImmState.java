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

import bubo.filters.MultivariateGaussianDM;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * There state of an InhomoInteractingMultipleModel filter.
 */
public class IhImmState {

	protected IhImmHypothesis[] hypotheses;
	private MultivariateGaussianDM stateMOG;

	private InternalStateConverter converter;

	// to cut down on memory each hypothesis could share this memory
	private DenseMatrix64F d;
	private DenseMatrix64F outer;

	public IhImmState(InhomoInteractingMultipleModel filter) {
		converter = filter.getConverter();
		hypotheses = new IhImmHypothesis[filter.getNumModels()];

		for (int i = 0; i < filter.getNumModels(); i++) {
			hypotheses[i] = new IhImmHypothesis(filter.getModels()[i]);
		}


		int dimen = converter.getOutputDimen();

		stateMOG = new MultivariateGaussianDM(dimen);

		d = new DenseMatrix64F(dimen, 1);
		outer = new DenseMatrix64F(dimen, dimen);
	}

	/**
	 * Returns the information on a specific internal hypothesis for the IMM
	 */
	public IhImmHypothesis getHypothesis(int index) {
		return hypotheses[index];
	}

	/**
	 * Sets the state of the filter to the specified values.  Each model will have the
	 * specified state and the coresponding probability.
	 *
	 * @param state The value of this state is copied into the individual model
	 */
	public void setState(int modelID, MultivariateGaussianDM state, double prob) {
		hypotheses[modelID].setStateValue(state, prob);
	}

	/**
	 * Computes a mixture of Gaussians (MOG) from the models.
	 */
	public MultivariateGaussianDM computeMOG() {
		DenseMatrix64F x_ret = stateMOG.getMean();
		DenseMatrix64F P_ret = stateMOG.getCovariance();

		x_ret.zero();
		P_ret.zero();

//        System.out.println("compute MOG");
		// compute the mean
		for (int i = 0; i < hypotheses.length; i++) {
			IhImmHypothesis h = hypotheses[i];
//            System.out.println("  model prob = "+h.getProbability());
			DenseMatrix64F x_j_orig = h.getState().getMean();
			DenseMatrix64F x_j = converter.convertOutput(true, x_j_orig, i);
			CommonOps.add(x_ret, h.getProbability(), x_j, x_ret);
		}

		// compute the covariance
		for (int i = 0; i < hypotheses.length; i++) {
			IhImmHypothesis h = hypotheses[i];

			DenseMatrix64F x_j_orig = h.getState().getMean();
			DenseMatrix64F x_j = converter.convertOutput(true, x_j_orig, i);

			d.set(x_j);
			CommonOps.add(d, -1, x_ret, d);

			CommonOps.multTransB(d, d, outer);

			DenseMatrix64F P_j_orig = h.getState().getCovariance();
			DenseMatrix64F P_j = converter.convertOutput(false, P_j_orig, i);
			CommonOps.add(outer, P_j, outer);

			CommonOps.add(P_ret, h.getProbability(), outer, P_ret);
		}

		return stateMOG;
	}

	/**
	 * Returns the model specific estimated state.
	 */
	public MultivariateGaussianDM getModelState(int modelIndex) {
		return hypotheses[modelIndex].getState();
	}

	public int getNumHypotheses() {
		return hypotheses.length;
	}
}
