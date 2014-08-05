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

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Add the same amount of odometry error but change the rate at which sensor data is processed.
 *
 * @author Peter Abeles
 */
public class DataRatesScanMotionValidation extends BaseNoiseScanMotionValidation {

	int rates[] = new int[]{1,2,5,10};

	public DataRatesScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		this.estimator = estimator;
		setOutputName("ScanMotionDataRate.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	@Override
	public void _performEvaluation() throws IOException {

		for( int i = 0; i < rates.length; i++ ) {
			skipSensor = rates[i];

			out.println("=========================================");
			out.println("Sensor skip = "+skipSensor);
			System.out.println("Sensor skip = "+skipSensor);
			configurePrintNoise(1);

			super.evaluateDataSets();
		}
	}

	public static void main(String[] args) throws IOException {
		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		DataRatesScanMotionValidation app = new DataRatesScanMotionValidation(alg);
		app.performEvaluation();
	}
}
