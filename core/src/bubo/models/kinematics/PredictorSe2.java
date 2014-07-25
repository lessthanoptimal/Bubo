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
import georegression.struct.se.Se2_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * Kinematics for a 2D robot which moves according to rigid body motion.  Control inputs is
 * a rigid body motion (translation and rotation), e.g. [tx,ty,&Delta;&theta;].  This is
 * primarily used for simulation purposes where the robot magically moves
 * </p>
 * <p>
 * Motion Model:
 * <pre>
 * [ x' ]   [ tx + x*c - y*s ]
 * [ y' ] = [ ty + x*s + y*c  ]
 * [ &theta;' ]   [ &theta; + &Delta;&theta; ]
 * </pre>
 * where c = cos(&Delta;&theta;) and s = sin(&Delta;&theta;).
 * </p>
 *
 * <p>
 * Plant noise is a function of motion magnitude.  More it moves the more noise is added.  If it is
 * stationary then no noise is added to the system.
 * </p>
 *
 * @author Peter Abeles
 */
public class PredictorSe2 implements EkfPredictor<Se2_F64> {

	// estimated state
	DenseMatrix64F x_est = new DenseMatrix64F(3, 1);
	// state transition matrix
	DenseMatrix64F G = CommonOps.identity(3);
	// plant noise covariance matrix
	DenseMatrix64F Q = new DenseMatrix64F(3, 3);

	// Control noise model
	DenseMatrix64F M = new DenseMatrix64F(3, 3);
	// Control Jacobian.  Used to map noise in control space into state space
	DenseMatrix64F V = new DenseMatrix64F(3, 3);
	// holds intermediate results
	DenseMatrix64F tempVM = new DenseMatrix64F(3, 3);

	// location error per distance traveled
	double odomTravelSigma;
	// angle error per distance traveled
	double odomTravelAngleSigma;
	// rotation error per radian turned
	double odomAngleSigma;

	public PredictorSe2(double odomTravelSigma,
						double odomTravelAngleSigma,
						double odomAngleSigma) {
		this.odomTravelSigma = odomTravelSigma;
		this.odomTravelAngleSigma = odomTravelAngleSigma;
		this.odomAngleSigma = odomAngleSigma;
	}

	@Override
	public int getSystemSize() {
		return 3;
	}

	@Override
	public void predict(DenseMatrix64F state, Se2_F64 control, double elapsedTime) {

		double x = state.get(0);
		double y = state.get(1);
		double theta = state.get(2);
		double ctheta = control.getYaw();

		x_est.data[0] = control.T.x + control.c*x - control.s*y;
		x_est.data[1] = control.T.y + control.s*x + control.c*y;
		x_est.data[2] = theta + ctheta;

		G.data[0] = control.c; G.data[1] = -control.s;
		G.data[3] = control.s; G.data[4] = control.c;
		G.data[8] = 1;

		V.data[0] = 1; V.data[2] = -x*control.s - y*control.c;
		V.data[4] = 1; V.data[5] =  x*control.c - y*control.s;
		V.data[8] = 1;

		// plant noise is dependent magnitude of motion
		double dx = x_est.data[0]-x;
		double dy = x_est.data[1]-y;
		double d = Math.sqrt(dx*dx + dy*dy);

		M.unsafe_set(0, 0, d*odomTravelSigma );
		M.unsafe_set(1, 1, d*odomTravelSigma );
		M.unsafe_set(2, 2, d*odomTravelAngleSigma + Math.abs(ctheta)*odomAngleSigma );

		CommonOps.mult(V, M, tempVM);
		CommonOps.multTransB(tempVM, V, Q);
	}

	@Override
	public DenseMatrix64F getJacobianF() {
		return G;
	}

	@Override
	public DenseMatrix64F getPlantNoise() {
		return Q;
	}

	@Override
	public DenseMatrix64F getPredictedState() {
		return x_est;
	}

	public DenseMatrix64F getM() {
		return M;
	}

	public DenseMatrix64F getV() {
		return V;
	}
}
