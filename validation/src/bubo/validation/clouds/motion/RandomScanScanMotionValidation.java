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
 * Every scan has a change of returning a completely random value
 *
 * @author Peter Abeles
 */
public class RandomScanScanMotionValidation extends BaseNoiseScanMotionValidation {

	double chanceRandomMeas;

	public RandomScanScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		this.estimator = estimator;
		setOutputName("ScanMotionRandomScan.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	@Override
	public void evaluate() throws IOException {
		// Noise on LRF
		lrfRangeSigma = 0.01;
		for( int i = 0; i <= 7; i++ ) {
			out.println("=========================================");
			chanceRandomMeas = 0.002 * Math.pow(2,i);
			out.println("badScanProb "+chanceRandomMeas);
			System.out.println("badScanProb "+chanceRandomMeas);
			printNoise();
			super.evaluateDataSets();
		}
	}

	@Override
	protected double[] adjustObservations(double[] ranges) {
		double []ret = super.adjustObservations(ranges);
		for( int i = 0; i < ret.length; i++ ) {
			if( rand.nextDouble() <= chanceRandomMeas ) {
				ret[i] = rand.nextDouble()*param.getMaxRange();
			}
		}
		return ret;
	}

	public static void main(String[] args) throws IOException {

		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		RandomScanScanMotionValidation app = new RandomScanScanMotionValidation(alg);
		app.evaluate();
	}
}
