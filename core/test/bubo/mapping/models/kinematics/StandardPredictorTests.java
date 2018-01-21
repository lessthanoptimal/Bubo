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

package bubo.mapping.models.kinematics;

import bubo.filters.ekf.EkfPredictor;
import org.ddogleg.optimization.DerivativeChecker;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.NormOps_DDRM;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class StandardPredictorTests {
	EkfPredictor predictor;
	double tol;
	double T;

	public StandardPredictorTests(double tolerance) {
		this.tol = tolerance;
	}

	/**
	 * Computes two covariance matrices for different amounts of elapsed time.  The larger elapsed time should
	 * have more uncertainty and thus have a larger covariance
	 *
	 * @param predictor
	 * @param T_init    the smaller time.  the larger time will be 50% larger
	 */
	public void checkCovarianceIncreaseWithTime(EkfPredictor predictor, Object control, double T_init, double... state) {
		DMatrixRMaj x = new DMatrixRMaj(state.length, 1, true, state);
		predictor.predict(x, control,T_init);

		// Using P2 norm instead of determinant since the determinant seems to produce very small numbers
		double Q1 = NormOps_DDRM.normP2(predictor.getPlantNoise());
		predictor.predict(x, control, T_init * 1.5);
		double Q2 = NormOps_DDRM.normP2(predictor.getPlantNoise());

		assertTrue(Q1 < Q2);
	}

	/**
	 * Checks the projector jacobian against a numerical jacobian
	 *
	 * @param input
	 */
	public void checkStateJacobianAtPoint(EkfPredictor predictor, Object control, boolean printResults, double T, double... input) {
		this.predictor = predictor;
		this.T = T;
		PredictorStateJacobian j = new PredictorStateJacobian(control);
		PredictorFunction f = new PredictorFunction(control);

		if (printResults) {
			DerivativeChecker.jacobianPrint(f, j, input, tol);
		}
		assertTrue(DerivativeChecker.jacobian(f, j, input, tol));
	}

	/**
	 * Checks to see if the covariance matrix is valid
	 */
	public void checkValidCovariance(EkfPredictor predictor, Object control , double T, double... state) {
		DMatrixRMaj x = new DMatrixRMaj(state.length, 1, true, state);
		predictor.predict(x, control, T);
		DMatrixRMaj Q = predictor.getPlantNoise();

		// has all valid numbers
		assertTrue(!MatrixFeatures_DDRM.hasUncountable(Q));
		// test positive definite
		assertTrue(CommonOps_DDRM.det(Q) >= 0);
		// test symmetric
		assertTrue(MatrixFeatures_DDRM.isSymmetric(Q, tol));
	}

	private class PredictorFunction implements FunctionNtoM {

		Object control;

		private PredictorFunction(Object control) {
			this.control = control;
		}

		@Override
		public int getNumOfInputsN() {
			return predictor.getSystemSize();
		}

		@Override
		public int getNumOfOutputsM() {
			return predictor.getSystemSize();
		}

		@Override
		public void process(double[] input, double[] output) {
			DMatrixRMaj X = new DMatrixRMaj(3, 1, true, input);

			predictor.predict(X, control, T);

			double[] found = predictor.getPredictedState().data;

			System.arraycopy(found, 0, output, 0, found.length);

		}
	}

	private class PredictorStateJacobian implements FunctionNtoMxN {

		Object control;

		private PredictorStateJacobian(Object control) {
			this.control = control;
		}

		@Override
		public int getNumOfInputsN() {
			return predictor.getSystemSize();
		}

		@Override
		public int getNumOfOutputsM() {
			return predictor.getSystemSize();
		}

		@Override
		public void process(double[] input, DMatrix outputM) {
			double output[] = ((DMatrixRMaj)outputM).data;
			DMatrixRMaj X = new DMatrixRMaj(3, 1, true, input);

			predictor.predict(X, control, T);

			double[] found = predictor.getJacobianF().data;

			System.arraycopy(found, 0, output, 0, found.length);
		}

		@Override
		public DMatrix declareMatrixMxN() {
			return new DMatrixRMaj(getNumOfOutputsM(),getNumOfInputsN());
		}

	}
}
