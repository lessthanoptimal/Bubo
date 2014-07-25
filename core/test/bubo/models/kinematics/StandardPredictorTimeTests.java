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

package bubo.models.kinematics;

import bubo.filters.ekf.EkfPredictor;
import org.ddogleg.optimization.JacobianChecker;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.NormOps;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class StandardPredictorTimeTests {
	EkfPredictor predictor;
	double tol;
	double T;

	public StandardPredictorTimeTests(double tolerance) {
		this.tol = tolerance;
	}

	/**
	 * Computes two covariance matrices for different amounts of elapsed time.  The larger elapsed time should
	 * have more uncertainty and thus have a larger covariance
	 *
	 * @param predictor
	 * @param T_init    the smaller time.  the larger time will be 50% larger
	 */
	public void checkCovarianceIncreaseWithTime(EkfPredictor predictor, double T_init, double... state) {
		DenseMatrix64F x = new DenseMatrix64F(state.length, 1, true, state);
		predictor.predict(x, null,T_init);

		// Using P2 norm instead of determinant since the determinant seems to produce very small numbers
		double Q1 = NormOps.normP2(predictor.getPlantNoise());
		predictor.predict(x, null, T_init * 1.5);
		double Q2 = NormOps.normP2(predictor.getPlantNoise());

		assertTrue(Q1 < Q2);
	}

	/**
	 * Checks the projector jacobian against a numerical jacobian
	 *
	 * @param input
	 */
	public void checkStateJacobianAtPoint(EkfPredictor predictor, boolean printResults, double T, double... input) {
		this.predictor = predictor;
		this.T = T;
		PredictorStateJacobian j = new PredictorStateJacobian();
		PredictorFunction f = new PredictorFunction();

		if (printResults) {
			JacobianChecker.jacobianPrint(f, j, input, tol);
		}
		assertTrue(JacobianChecker.jacobian(f, j, input, tol));
	}

	/**
	 * Checks to see if the covariance matrix is valid
	 */
	public void checkValidCovariance(EkfPredictor predictor, double T, double... state) {
		DenseMatrix64F x = new DenseMatrix64F(state.length, 1, true, state);
		predictor.predict(x, null, T);
		DenseMatrix64F Q = predictor.getPlantNoise();

		// has all valid numbers
		assertTrue(!MatrixFeatures.hasUncountable(Q));
		// test positive definite
		assertTrue(CommonOps.det(Q) >= 0);
		// test symmetric
		assertTrue(MatrixFeatures.isSymmetric(Q, tol));
	}

	private class PredictorFunction implements FunctionNtoM {

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
			DenseMatrix64F X = new DenseMatrix64F(3, 1, true, input);

			predictor.predict(X, null, T);

			double[] found = predictor.getPredictedState().data;

			System.arraycopy(found, 0, output, 0, found.length);

		}
	}

	private class PredictorStateJacobian implements FunctionNtoMxN {

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
			DenseMatrix64F X = new DenseMatrix64F(3, 1, true, input);

			predictor.predict(X, null, T);

			double[] found = predictor.getJacobianF().data;

			System.arraycopy(found, 0, output, 0, found.length);
		}
	}
}
