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

package bubo.mapping.models.sensor;

import bubo.filters.ekf.EkfProjector;
import org.ddogleg.optimization.DerivativeChecker;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class StandardProjectorTests {

	EkfProjector projector;
	double tol;

	public StandardProjectorTests(double tolerance) {
		this.tol = tolerance;
	}

	/**
	 * Checks the projector jacobian against a numerical jacobian
	 *
	 * @param input
	 */
	public void checkProjectorAtPoint(EkfProjector projector, boolean printResults, double... input) {
		this.projector = projector;
		ProjectorJacobian j = new ProjectorJacobian();
		ProjectorFunction f = new ProjectorFunction();

		if (printResults) {
			DerivativeChecker.jacobianPrint(f, j, input, tol);
		}
		assertTrue(DerivativeChecker.jacobian(f, j, input, tol));
	}

	private class ProjectorFunction implements FunctionNtoM {

		@Override
		public int getNumOfInputsN() {
			return projector.getSystemSize();
		}

		@Override
		public int getNumOfOutputsM() {
			return projector.getMeasurementSize();
		}

		@Override
		public void process(double[] input, double[] output) {
			DMatrixRMaj X = new DMatrixRMaj(3, 1, true, input);

			projector.compute(X);

			double[] found = projector.getProjected().data;

			System.arraycopy(found, 0, output, 0, found.length);

		}
	}

	private class ProjectorJacobian implements FunctionNtoMxN {

		@Override
		public int getNumOfInputsN() {
			return projector.getSystemSize();
		}

		@Override
		public int getNumOfOutputsM() {
			return projector.getMeasurementSize();
		}

		@Override
		public DMatrix declareMatrixMxN() {
			return new DMatrixRMaj(projector.getMeasurementSize(),projector.getSystemSize());
		}

		@Override
		public void process(double[] input, DMatrix outputM) {
			double[] output = ((DMatrixRMaj)outputM).data;
			DMatrixRMaj X = new DMatrixRMaj(3, 1, true, input);

			projector.compute(X);

			double[] found = projector.getJacobianH().data;

			System.arraycopy(found, 0, output, 0, found.length);
		}
	}
}
