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

import java.io.IOException;

import static bubo.validation.clouds.motion.FactoryEvaluateScanMotion.createIcpLocal;

/**
 * Runs and saves the results for all validation tests
 *
 * @author Peter Abeles
 */
public class RunAllScanMotionValidations {

	String outputDirectory;

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void processAll() {
		process( createIcpLocal() , "ICP" );
	}

	public void process( Lrf2dMotionRollingKeyFrame estimator , String name ) {
		try {
			process( new PerfectScanMotionValidation(estimator),name);
			process( new DataRatesScanMotionValidation(estimator),name);
			process( new RandomScanScanMotionValidation(estimator),name);
			process( new ReverseScanMotionValidation(estimator),name);
			process( new VariousNoiseScanMotionValidation(estimator),name);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void process( ScanMotionValidation validator , String name ) throws IOException {
		validator.setOutputDirectory(outputDirectory);
		validator.setAlgorithmName(name);
		validator.performEvaluation();
	}
}
