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
 * Evaluate different amounts of noise on the LRF's range measurement.
 *
 * @author Peter Abeles
 */
public class NoiseSensorScanMotionValidation extends ScanMotionValidation {

	double sigmaRange;

	public NoiseSensorScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		super(estimator);
		setOutputName("ScanMotionSensorNoise.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	public void evaluate() throws IOException {
		for( int i = 0; i <= 5; i++ ) {
			sigmaRange = 0.01*Math.pow(2,i);
			out.println("=========================================");
			out.println("Range SIGMA = "+sigmaRange);
			System.out.println("Range SIGMA = "+sigmaRange);
			super.evaluateDataSets();
		}
	}

	@Override
	protected Se2_F64 adjustOdometry(Se2_F64 sensorToWorld) {
		return sensorToWorld;
	}

	@Override
	protected double[] adjustObservations(double[] ranges) {
		double[] ret = ranges.clone();
		for( int i = 0; i < ret.length; i++ ) {
			ret[i] += rand.nextGaussian()*sigmaRange;
			if( ret[i] < 0 )
				ret[i] = 0;
		}
		return ret;
	}

	public static void main(String[] args) throws IOException {

		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		NoiseSensorScanMotionValidation app = new NoiseSensorScanMotionValidation(alg);
		app.evaluate();
	}
}
