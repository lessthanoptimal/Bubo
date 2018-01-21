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

package bubo.filters.specific.ekf;

import bubo.filters.GenericKalmanFilterTests;
import bubo.filters.MultivariateGaussianDM;
import bubo.filters.ekf.EkfPredictor;
import bubo.filters.kf.ConstAccel1D;
import bubo.filters.kf.FixedKalmanProjector;
import bubo.filters.kf.KalmanFilter;
import bubo.filters.kf.KalmanPredictor;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestKfToEkfPredictorWrapper {

	/**
	 * Creates the a KalmanFilter and a wrapper.  sees if they produce the same results.
	 */
	@Test
	public void compareToKfPredict() {
		compareToKfPredict(false);
		compareToKfPredict(true);
	}

	/**
	 * Performs the actual comparision between KF and the wrapper.  Allows the use of
	 * control input to be toggled.
	 */
	private void compareToKfPredict(boolean hasInput) {
		DMatrixRMaj u = null;

		if (hasInput) {
			u = createControl();
		}

		EkfPredictor pred = new KfToEkfPredictor(createPredictor(hasInput), u);
		KalmanFilter filter = createFilter(hasInput);

		MultivariateGaussianDM x = GenericKalmanFilterTests.createState(9.0, 0, 1, 2);

		pred.predict(x.getMean(), null, -1);
		filter.predict(x,null,-1);

		assertTrue(MatrixFeatures_DDRM.isIdentical(pred.getPredictedState(), x.getMean(), 1e-5));
	}

	/**
	 * Makes sure the original state is not modified durring the prediction process
	 */
	@Test
	public void checkStateNoModify() {
		EkfPredictor pred = new KfToEkfPredictor(createPredictor(true), createControl());

		MultivariateGaussianDM x = GenericKalmanFilterTests.createState(9.0, 0, 1, 2);

		pred.predict(x.getMean(), null, -1);

		// make sure the original hasn't changed
		assertEquals(0, x.getMean().get(0, 0), 1e-5);
		assertEquals(1, x.getMean().get(1, 0), 1e-5);
		assertEquals(2, x.getMean().get(2, 0), 1e-5);

		// make sure the prediction is different
		for (int i = 0; i < 3; i++) {
			assertNotSame(pred.getPredictedState().get(i, 0), x.getMean().get(i, 0));
		}
	}

	private KalmanFilter createFilter(boolean withControl) {
		KalmanPredictor prop = createPredictor(withControl);

		DMatrixRMaj H = new DMatrixRMaj(new double[][]{{1, 1, 1}, {0, 1, 2}});

		FixedKalmanProjector projector = new FixedKalmanProjector(H);

		KalmanFilter ret = new KalmanFilter(prop, projector);

		if (withControl) {
			ret.setControlInputRef(createControl());
		}
		return ret;
	}

	private KalmanPredictor createPredictor(boolean withControl) {
		if (!withControl)
			return new ConstAccel1D(1.0, 1);
		else {
			DMatrixRMaj G = new DMatrixRMaj(3, 2);
			G.set(0, 0, 0.3);
			G.set(2, 1, 0.8);
			return new ConstAccel1D(G, 1.0);
		}
	}

	private DMatrixRMaj createControl() {
		DMatrixRMaj ret = new DMatrixRMaj(2, 1);
		ret.set(0, 0, 2);
		ret.set(1, 0, 0.1);

		return ret;
	}
}
