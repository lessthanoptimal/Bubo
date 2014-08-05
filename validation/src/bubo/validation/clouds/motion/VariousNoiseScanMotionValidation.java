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
 * Evaluate different amounts of additive noise on odometry and LRF
 *
 * @author Peter Abeles
 */
public class VariousNoiseScanMotionValidation extends BaseNoiseScanMotionValidation {

	public VariousNoiseScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		this.estimator = estimator;
		setOutputName("ScanMotionVariousNoise.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	@Override
	public void _performEvaluation() throws IOException {
		// Noise on LRF
		baseOdomAngle = baseOdomTravelAngle = baseOdomTravel = 0;
		baseLrfRange = 0.01;
		for( int i = 0; i <= 5; i++ ) {
			out.println("=========================================");
			configurePrintNoise(Math.pow(2,i));
			super.evaluateDataSets();
		}

		// Odometry Noise
		baseOdomTravel = 0.02;
		baseOdomTravelAngle = 0.002;
		baseOdomAngle = 0.05;
		baseLrfRange = 0;
		for( int i = 0; i <= 5; i++ ) {
			out.println("=========================================");
			configurePrintNoise(Math.pow(2,i));
			super.evaluateDataSets();
		}
	}

	public static void main(String[] args) throws IOException {

		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		VariousNoiseScanMotionValidation app = new VariousNoiseScanMotionValidation(alg);
//		app.activateVisualization();
		app.performEvaluation();
	}
}
