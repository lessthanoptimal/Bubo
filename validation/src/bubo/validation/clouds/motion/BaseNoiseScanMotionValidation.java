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

package bubo.validation.clouds.motion;

import georegression.struct.se.Se2_F64;

import java.io.IOException;

/**
 * Base class for adding noise to robot motion and LRF
 *
 * @author Peter Abeles
 */
public abstract class BaseNoiseScanMotionValidation extends ScanMotionValidation {

	// base error at a scale of one
	protected double baseOdomTravel = 0.02;
	protected double baseOdomTravelAngle = 0.002;
	protected double baseOdomAngle = 0.02;

	protected double baseLrfRange = 0.01;

	// location error per distance traveled
	double odomTravelSigma;
	// angle error per distance traveled
	double odomTravelAngleSigma;
	// rotation error per radian turned
	double odomAngleSigma;

	// Gaussian noise added to range measurements
	double lrfRangeSigma;

	// book keeping for odometry error
	Se2_F64 previousTruthInvert = new Se2_F64();
	Se2_F64 previousOdometry = new Se2_F64();
	Se2_F64 odometry = new Se2_F64();
	Se2_F64 change = new Se2_F64();
	boolean first;

	@Override
	public void evaluate() throws IOException {
		for( int i = 0; i <= 5; i++ ) {
			lrfRangeSigma = 0.01*Math.pow(2,i);
			out.println("=========================================");
			out.println("Range SIGMA = "+ lrfRangeSigma);
			System.out.println("Range SIGMA = "+ lrfRangeSigma);
			super.evaluateDataSets();
		}
	}

	protected void configurePrintNoise( double scale ) {
		odomTravelSigma = baseOdomTravel*scale;
		odomTravelAngleSigma = baseOdomTravelAngle*scale;
		odomAngleSigma = baseOdomAngle*scale;

		lrfRangeSigma = baseLrfRange*scale;

		out.println("LRF range = "+ lrfRangeSigma);
		out.println("Odometry Travel = "+ odomTravelSigma +" TravelAngle "+ odomTravelAngleSigma +" Angle "+ odomAngleSigma);

		System.out.println("LRF range = "+ lrfRangeSigma);
		System.out.println("Odometry Travel = "+ odomTravelSigma +" TravelAngle "+ odomTravelAngleSigma +" Angle "+ odomAngleSigma);
	}

	@Override
	protected Se2_F64 adjustOdometry(Se2_F64 sensorToWorld) {
		if( first ) {
			first = false;
			previousOdometry.set(sensorToWorld);
			sensorToWorld.invert(previousTruthInvert);
			return sensorToWorld;
		} else {
			// find the true change in location
			previousTruthInvert.concat(sensorToWorld,change);
			sensorToWorld.invert(previousTruthInvert);

			double T = change.T.norm();
			double deltaAngle = change.getYaw();

			// add noise to change
			change.T.x += rand.nextGaussian()* odomTravelSigma *T;
			change.T.y += rand.nextGaussian()* odomTravelSigma *T;

			double noiseYaw = rand.nextGaussian()*(odomTravelAngleSigma *T + odomAngleSigma *deltaAngle);
			change.setYaw( change.getYaw() + noiseYaw );

			previousOdometry.concat(change,odometry);
			previousOdometry.set(odometry);

			return odometry;
		}
	}

	@Override
	protected double[] adjustObservations(double[] ranges) {
		double[] ret = ranges.clone();
		for( int i = 0; i < ret.length; i++ ) {
			ret[i] += rand.nextGaussian()* lrfRangeSigma;
			if( ret[i] < 0 )
				ret[i] = 0;
		}
		return ret;
	}
}
