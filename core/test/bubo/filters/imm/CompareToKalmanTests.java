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

package bubo.filters.imm;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.UtilMultivariateGaussian;
import bubo.filters.kf.ConstAccel1D;
import bubo.filters.kf.KalmanFilter;
import bubo.filters.kf.KalmanPredictor;
import bubo.filters.kf.KalmanProjector;
import bubo.filters.specific.kf.FirstFewProjector;
import org.ejml.data.DMatrixRMaj;

import static org.junit.Assert.assertTrue;

/**
 * A series of tests where a filter is compared to a Kalman filter.  It is assumed
 * that the other filter can be configured so that it is equivalent to a Kalman filter
 * given gaussian measurements and prior state estimates.
 */
public abstract class CompareToKalmanTests {
	double T = 0.5;

	double stateTol;
	double covTol;

	public CompareToKalmanTests(double stateTol, double covTol) {
		this.stateTol = stateTol;
		this.covTol = covTol;
	}

	/**
	 * See if the Kalman filter and particle filter produce similar results
	 */
	public void compareToKalman() {
		KalmanFilter kf = createKalman();
		createOtherFilter();

		MultivariateGaussianDM kalState = createPrior();
		setOtherPriorState(createPrior());

		DMatrixRMaj target = createTrueInitial();

		MultivariateGaussianDM otherOutput = computeOutput();
		assertTrue(UtilMultivariateGaussian.isSimilar(kalState, otherOutput, stateTol, covTol));

		for (int i = 0; i < 100; i++) {
//            System.out.println("----------------------------------");
			predict(target);
			kf.predict(kalState,null,-1);
			predictOther();

			otherOutput = computeOutput();
			assertTrue(UtilMultivariateGaussian.isSimilar(kalState, otherOutput, stateTol, covTol));

			kf.update(kalState, createMeas(target));
			updateOther(createMeas(target));

			otherOutput = computeOutput();
			assertTrue(UtilMultivariateGaussian.isSimilar(kalState, otherOutput, stateTol, covTol));
		}
	}

	protected abstract void createOtherFilter();

	protected abstract void predictOther();

	protected abstract void updateOther(MultivariateGaussianDM meas);

	protected abstract void setOtherPriorState(MultivariateGaussianDM prior);

	protected abstract MultivariateGaussianDM computeOutput();


	private MultivariateGaussianDM createPrior() {
		MultivariateGaussianDM ret = UtilMultivariateGaussian.createDummy(3, 3);

		ret.getMean().set(1, 0, 2);
		ret.getMean().set(2, 0, 2);

		return ret;
	}

	private DMatrixRMaj createTrueInitial() {
		DMatrixRMaj ret = new DMatrixRMaj(new double[][]{{0}, {2}, {2}});

		return ret;
	}

	private MultivariateGaussianDM createMeas(DMatrixRMaj state) {
		MultivariateGaussianDM ret = UtilMultivariateGaussian.createDummy(1, 1);

		ret.getMean().set(0, 0, state.get(0, 0));

		return ret;
	}

	private void predict(DMatrixRMaj x) {
		double p = x.get(0, 0);
		double v = x.get(1, 0);
		double a = x.get(2, 0);

		x.set(0, 0, p + v * T + 0.5 * a * T * T);
		x.set(1, 0, v + a * T);
	}

	private KalmanFilter createKalman() {
		KalmanPredictor pred = createPredictor();
		KalmanProjector proj = createProjector();

		return new KalmanFilter(pred, proj);
	}

	protected KalmanPredictor createPredictor() {
		ConstAccel1D pred = new ConstAccel1D(.5, 1);

		return pred;
	}

	protected KalmanProjector createProjector() {
		FirstFewProjector proj = new FirstFewProjector(1, 3);

		return proj;
	}
}
