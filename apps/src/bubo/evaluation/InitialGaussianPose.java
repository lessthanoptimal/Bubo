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

package bubo.evaluation;

import georegression.metric.UtilAngle;
import georegression.struct.se.Se2_F64;

import java.util.Random;

/**
 * Generates a noisy pose estimate from a normal distribution given the true position.
 *
 * @author Peter Abeles
 */
public class InitialGaussianPose implements InitialPose {

	Se2_F64 poseNoisy = new Se2_F64();

	Random rand;

	double sigmaXY;
	double sigmaYaw;

	@Override
	public void generatePose(Se2_F64 poseTruth) {

		double x = poseTruth.getX() + rand.nextGaussian()*sigmaXY;
		double y = poseTruth.getY() + rand.nextGaussian()*sigmaXY;
		double yaw = UtilAngle.bound(poseTruth.getY() + rand.nextGaussian()*sigmaYaw);

	}

	@Override
	public Se2_F64 getPoseNoisy() {
		return poseNoisy;
	}

	@Override
	public double getSigmaLocation() {
		return sigmaXY;
	}

	@Override
	public double getSigmaYaw() {
		return sigmaYaw;
	}
}
