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

import georegression.metric.UtilAngle;
import georegression.struct.se.Se2_F64;

import java.io.FileNotFoundException;

/**
 * Base class for adding noise to robot motion and LRF
 *
 * @author Peter Abeles
 */
public abstract class BaseNoiseScanMotionValidation extends ScanMotionValidation {

	// base error at a scale of one
	protected double baseOdomTravel = 0.02;
	protected double baseOdomTravelAngle = 0.002;
	protected double baseOdomAngle = 0.05;

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
	Se2_F64 prevTruth = new Se2_F64();
	Se2_F64 odometry = new Se2_F64();
	boolean first;

	protected void configurePrintNoise( double scale ) {
		odomTravelSigma = baseOdomTravel*scale;
		odomTravelAngleSigma = baseOdomTravelAngle*scale;
		odomAngleSigma = baseOdomAngle*scale;

		lrfRangeSigma = baseLrfRange*scale;

		printNoise();
	}

	@Override
	protected void initialize(DataSet dataSet) throws FileNotFoundException {
		super.initialize(dataSet);
		first = true;
	}

	protected void printNoise() {
		out.println("LRF range = "+ lrfRangeSigma);
		out.println("Odometry Travel = "+ odomTravelSigma +" TravelAngle "+ odomTravelAngleSigma +" Angle "+ odomAngleSigma);

		System.out.println("LRF range = "+ lrfRangeSigma);
		System.out.println("Odometry Travel = "+ odomTravelSigma +" TravelAngle "+ odomTravelAngleSigma +" Angle "+ odomAngleSigma);
	}


	@Override
	protected Se2_F64 adjustOdometry(Se2_F64 sensorToWorld) {
		if( first ) {
			first = false;
			prevTruth.set(sensorToWorld);
			odometry.set(sensorToWorld);
			return sensorToWorld;
		} else {
			// compute change in translation and orientation from previous frame
			double Tx = sensorToWorld.T.x - prevTruth.T.x;
			double Ty = sensorToWorld.T.y - prevTruth.T.y;
			double T = sensorToWorld.T.distance(prevTruth.T);
			double deltaAngle = UtilAngle.minus( sensorToWorld.getYaw() , prevTruth.getYaw() );
			double adjustAngle = UtilAngle.minus(odometry.getYaw(),prevTruth.getYaw());
			prevTruth.set(sensorToWorld);

			// add noise to change
			double noiseAngle = rand.nextGaussian() * (odomTravelAngleSigma * T + odomAngleSigma * deltaAngle);

//			System.out.println("adjustAngle "+adjustAngle);

			double c = Math.cos(adjustAngle);
			double s = Math.sin(adjustAngle);

			odometry.T.x += Tx*c - Ty*s + rand.nextGaussian()* odomTravelSigma*T;
			odometry.T.y += Tx*s + Ty*c + rand.nextGaussian()* odomTravelSigma*T;
			odometry.setYaw( odometry.getYaw() + deltaAngle + noiseAngle );

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
