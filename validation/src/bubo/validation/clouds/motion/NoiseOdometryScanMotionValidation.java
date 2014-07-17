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

import bubo.clouds.motion.Lrf2dMotionRollingKeyFrame;
import georegression.struct.se.Se2_F64;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class NoiseOdometryScanMotionValidation extends ScanMotionValidation  {

	// location error per distance traveled
	double sigmaTravel;
	// angle error per distance traveled
	double sigmaTravelAngle = 0.001;
	// rotation error per radian turned
	double sigmaAngle = 0.01;

	Se2_F64 previousTruthInvert = new Se2_F64();
	Se2_F64 previousOdometry = new Se2_F64();
	Se2_F64 odometry = new Se2_F64();
	Se2_F64 change = new Se2_F64();
	boolean first;

	public NoiseOdometryScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		super(estimator);
		setOutputName("ScanMotionOdometryNoise.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	@Override
	protected void initialize(DataSet dataSet) throws FileNotFoundException {
		super.initialize(dataSet);
		first = true;
	}

	public void evaluate() throws IOException {
		for( int i = 0; i <= 5; i++ ) {
			sigmaTravel = 0.01*Math.pow(2,i);
			sigmaTravelAngle = 0.001*Math.pow(2,i);
			sigmaAngle = 0.01*Math.pow(2,i);
			out.println("=========================================");
			out.println("SIGMA Travel = "+sigmaTravel+" TravelAngle "+sigmaAngle+" Angle "+sigmaAngle);
			System.out.println("SIGMA Travel = "+sigmaTravel+" TravelAngle "+sigmaAngle+" Angle "+sigmaAngle);
			super.evaluateDataSets();
		}
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

			double T = change.T.norm();
			double deltaAngle = change.getYaw();

			// add noise to change
			change.T.x += rand.nextGaussian()*sigmaTravel*T;
			change.T.y += rand.nextGaussian()*sigmaTravel*T;

			double noiseYaw = rand.nextGaussian()*(sigmaTravelAngle*T + sigmaAngle*deltaAngle);
			change.setYaw( change.getYaw() + noiseYaw );

			previousOdometry.concat(change,odometry);
			previousOdometry.set(odometry);

			return odometry;
		}
	}

	@Override
	protected double[] adjustObservations(double[] ranges) {
		return ranges;
	}

	public static void main(String[] args) throws IOException {
		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		NoiseOdometryScanMotionValidation app = new NoiseOdometryScanMotionValidation(alg);
		app.evaluate();
	}
}
