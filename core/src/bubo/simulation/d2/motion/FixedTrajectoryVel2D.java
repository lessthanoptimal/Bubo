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

import bubo.models.kinematics.PredictorRobotVelocity2D;

import java.util.Random;

/**
 * Simulates a robot trajectory with a fixed set of velocity controls.  The true value is noise free while
 * the "requested" value has noise added to it.  This way a test algorithm can have the robot move with
 * a fixed trajectory, but have noise added to its controls.
 *
 * @author Peter Abeles
 */
public class FixedTrajectoryVel2D extends SimulatedPredictorModel2D {

	// RNG for adding noise
	Random rand;

	// amount of control noise
	double sigmaVel;
	double sigmaAngVel;

	// requested motion control parameters
	double request[] = new double[2];
	// actual = requested + noise
	double actual[] = new double[2];

	public FixedTrajectoryVel2D(double vel, double angVel,
								double sigmaVel, double sigmaAngVel,
								double timePeriod, long randSeed) {
		super(new PredictorRobotVelocity2D(0.1, 0.1, 0.1, 0.1), timePeriod);

		this.rand = new Random(randSeed);

		this.sigmaVel = sigmaVel;
		this.sigmaAngVel = sigmaAngVel;

		actual[0] = vel;
		actual[1] = angVel;
	}

	@Override
	protected void drawControlNoise() {
		request[0] = actual[0] + rand.nextGaussian() * sigmaVel;
		request[1] = actual[1] + rand.nextGaussian() * sigmaAngVel;

		((PredictorRobotVelocity2D) model).setControl(actual[0], actual[1]);
	}

	@Override
	public double[] getControlRequested() {
		return request;
	}

	@Override
	public double[] getControlTruth() {
		return actual;
	}
}
