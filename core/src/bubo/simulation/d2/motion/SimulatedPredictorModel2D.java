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

package bubo.simulation.d2.motion;

import bubo.filters.ekf.EkfPredictorTime;
import bubo.simulation.d2.SimulatedRobotTrajectory;
import georegression.struct.se.Se2_F64;
import org.ejml.data.DenseMatrix64F;

/**
 * Simulates a 2D robot (x,y,yaw) using a {@link EkfPredictorTime} as its internal model.  While easy to use this
 * can be inefficient since the predictor has to compute much more than just the motion trajectory
 *
 * @author Peter Abeles
 */
public abstract class SimulatedPredictorModel2D implements SimulatedRobotTrajectory {

	// kinematics model
	protected EkfPredictorTime model;

	// true robot pose
	private Se2_F64 pose = new Se2_F64();

	// elapsed time since last update
	private double T;
	// temporary storage for robot state
	private DenseMatrix64F x = new DenseMatrix64F(3, 1);

	public SimulatedPredictorModel2D(EkfPredictorTime model, double timePeriod) {
		this.model = model;
		T = timePeriod;
	}

	@Override
	public Se2_F64 getPose() {
		return pose;
	}

	@Override
	public void setPose(Se2_F64 pose) {
		this.pose.set(pose);
	}

	public void setTimePeriod(double timePeriod) {
		T = timePeriod;
	}

	@Override
	public void update() {
		x.data[0] = pose.T.x;
		x.data[1] = pose.T.y;
		x.data[2] = pose.getYaw();

		drawControlNoise();

		// move the robot forward
		model.compute(x, T);

		// Update the robot's pose
		DenseMatrix64F x_pred = model.getPredictedState();
		pose.set(x_pred.data[0], x_pred.data[1], x_pred.data[2]);
	}

	/**
	 * Add noise to the requested control parameters.  The noisy control parameters are the true parameters.
	 */
	protected abstract void drawControlNoise();
}
