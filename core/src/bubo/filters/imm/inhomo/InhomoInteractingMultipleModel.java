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
import bubo.filters.UtilMultivariateGaussian;
import bubo.filters.ekf.ExtendedKalmanFilter;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * This is a generalized implementation of the IMM filter.  It allows is internal filters to have
 * states with different dimension and meaning.  It doesn't have a homogeneous set of internal states.
 * This is accomplished by specifying a set of converters that convert between each type of filter.
 */
public class InhomoInteractingMultipleModel {

	// converts between different state vector types
	private InternalStateConverter converter;

	// creates the interaction matrix based upon the sensors measurement sojourn time
	private DenseMatrix64F markov;

	// an array of the internal models
	private ExtendedKalmanFilter models[];

	private DenseMatrix64F c;
	private DenseMatrix64F d[];
	private DenseMatrix64F outer[];

	// The DOF of the state for each one of the internal models.
	// note the all the models' state will have the same dimension/DOF.
	private int dimen;

	// the measurement DOF
	private int measDOF;

	/**
	 * Constructor used to create a new IMM filter.
	 *
	 * @param converter    Specifies how state vectors are converted between the two
	 * @param modelFilters The filters used by each internal model
	 * @param markov       Used to compute the interaction matrix at each time step
	 */
	public InhomoInteractingMultipleModel(InternalStateConverter converter,
										  ExtendedKalmanFilter modelFilters[],
										  DenseMatrix64F markov) {
		this.converter = converter;
		dimen = converter.getOutputDimen();
		measDOF = modelFilters[0].getMeasDOF();
		final int N = modelFilters.length;

		// create one model for each dynamics model provided
		models = new ExtendedKalmanFilter[N];
		System.arraycopy(modelFilters, 0, models, 0, N);
		d = new DenseMatrix64F[N];
		outer = new DenseMatrix64F[N];
		for (int i = 0; i < N; i++) {
			if (measDOF != modelFilters[i].getMeasDOF())
				throw new RuntimeException("All filters must have the same measurement DOF");

			int dof = modelFilters[i].getStateDOF();
			// this could be made more memory efficient by only creating new structures for
			// each DOF once
			d[i] = new DenseMatrix64F(dof, 1);
			outer[i] = new DenseMatrix64F(dof, dof);
		}

		this.markov = markov;

		// predeclare matrices that will be used later on
		c = new DenseMatrix64F(N, N);


	}

	protected InhomoInteractingMultipleModel() {
	}

	/**
	 * Returns the DOF in the state vector
	 */
	public int getStateDOF() {
		return dimen;
	}

	/**
	 * The DOF of the sensor measurements its expecting
	 */
	public int getMeasDOF() {
		return measDOF;
	}

	/**
	 * Returns the number of models the filter internally uses.
	 */
	public int getNumModels() {
		return models.length;
	}

	/**
	 * Predict the state of the system deltaTime in the future.
	 * <p/>
	 * Before the state are propagated in the future the filter mixes
	 * the model estimates together.
	 */
	public void predict(IhImmState state) {
		// mix the states together
		mixing(state);

		// predict the state of each hypothesis and set the mixed state
		// to be the true state
		IhImmHypothesis hypotheses[] = state.hypotheses;
		for (int i = 0; i < hypotheses.length; i++) {
			IhImmHypothesis h = hypotheses[i];
			ExtendedKalmanFilter filter = models[i];
			filter.predict(h.getMix());
			h.swapMix();
		}
	}

	/**
	 * Updates the state of each model with the measurement as well as the model probabilities
	 */
	public void update(IhImmState state, MultivariateGaussianDM meas) {
		IhImmHypothesis hypotheses[] = state.hypotheses;
//        System.out.println("IMM Update");
		double total = 0.0;

//        System.out.println("  measurement = "+meas.getMean().get(0,0));

		for (int i = 0; i < hypotheses.length; i++) {
			// update it with the measurements
			IhImmHypothesis h = hypotheses[i];
			ExtendedKalmanFilter filter = models[i];
			filter.update(h.getState(), meas);

			// compute the likelihood
			DenseMatrix64F y = filter.getInnovation();
			DenseMatrix64F S = filter.getInnovationCov();
			DenseMatrix64F S_inv = filter.getInnovationCovInverse();

			double likelihood = UtilMultivariateGaussian.likelihoodP(y, S, S_inv);
			double prob = h.getProbability() * likelihood;
			total += prob;

//            System.out.printf("  pred pos = %8.2f like = %8.2f prob = %8.2f\n",predPos,likelihood,prob);

			h.setProbability(prob);
		}

		// now normalize so that its a probability
		for (IhImmHypothesis h : hypotheses) {
			h.setProbability(h.getProbability() / total);
//            System.out.println("normalized prob = "+h.getProbability());
		}
	}

	/**
	 * Performs the mixing step in the IMM
	 */
	protected void mixing(IhImmState state) {
		computeMixedProbability(state);
		computeMixedMean(state);
		computeMixedCovariance(state);
	}

	protected void computeMixedProbability(IhImmState state) {
		IhImmHypothesis hypotheses[] = state.hypotheses;

		for (int i = 0; i < hypotheses.length; i++) {
			double p = 0.0;

			IhImmHypothesis m = hypotheses[i];

			for (int j = 0; j < hypotheses.length; j++) {
				p += markov.get(j, i) * hypotheses[j].getProbability();
			}
			m.setMixedProb(p);

			for (int j = 0; j < hypotheses.length; j++) {
				double c_element = markov.get(j, i) * hypotheses[j].getProbability() / m.getMixedProb();
				c.set(i, j, c_element);
			}
		}
	}

	protected void computeMixedMean(IhImmState state) {
		IhImmHypothesis hypotheses[] = state.hypotheses;

		for (int i = 0; i < hypotheses.length; i++) {
			DenseMatrix64F mix = hypotheses[i].getMix().getMean();
			mix.zero();

			for (int j = 0; j < hypotheses.length; j++) {
				DenseMatrix64F x_j_orig = hypotheses[j].getState().getMean();
				DenseMatrix64F x_j = converter.convertMergeFrom(true, x_j_orig, j, i);

				CommonOps.add(mix, c.get(i, j), x_j, mix);
			}
		}
	}

	protected void computeMixedCovariance(IhImmState state) {
		IhImmHypothesis hypotheses[] = state.hypotheses;

		for (int i = 0; i < hypotheses.length; i++) {
			IhImmHypothesis m = hypotheses[i];
			DenseMatrix64F mixMean = m.getMix().getMean();
			DenseMatrix64F mixCov = m.getMix().getCovariance();
			mixCov.zero();

			DenseMatrix64F d = this.d[i];
			DenseMatrix64F outer = this.outer[i];

			for (int j = 0; j < hypotheses.length; j++) {
				IhImmHypothesis m_j = hypotheses[j];

				DenseMatrix64F x_j_orig = hypotheses[j].getState().getMean();
				DenseMatrix64F x_j = converter.convertMergeFrom(true, x_j_orig, j, i);

				d.set(mixMean);
				CommonOps.add(d, -1.0, x_j, d);
				CommonOps.multTransB(d, d, outer);

				DenseMatrix64F P_j_orig = m_j.getState().getCovariance();
				DenseMatrix64F P_j = converter.convertMergeFrom(false, P_j_orig, j, i);

				CommonOps.add(outer, P_j, outer);

				CommonOps.add(mixCov, c.get(i, j), outer, mixCov);
			}
		}
	}

	protected ExtendedKalmanFilter[] getModels() {
		return models;
	}

	public InternalStateConverter getConverter() {
		return converter;
	}
}