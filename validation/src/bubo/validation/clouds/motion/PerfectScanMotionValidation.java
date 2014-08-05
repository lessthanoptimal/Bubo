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
public class PerfectScanMotionValidation extends ScanMotionValidation {

	public PerfectScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		super(estimator);
		setOutputName("ScanMotionPerfect.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02","sim03"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	@Override
	protected void _performEvaluation() throws IOException {
		evaluateDataSets();
	}

	@Override
	protected Se2_F64 adjustOdometry(Se2_F64 sensorToWorld) {
		return sensorToWorld;
	}

	@Override
	protected double[] adjustObservations(double[] ranges) {
		return ranges;
	}

	public static void main(String[] args) throws IOException {

		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		PerfectScanMotionValidation app = new PerfectScanMotionValidation(alg);
//		app.activateVisualization();
		app.performEvaluation();
	}
}
