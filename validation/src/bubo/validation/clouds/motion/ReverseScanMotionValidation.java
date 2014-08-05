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
import bubo.io.serialization.SerializationDefinitionManager;
import bubo.io.text.ReadCsvObjectSmart;
import bubo.io.text.ReverseLineInputStream;
import georegression.struct.se.Se2_F64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Plays back a datalog in reverse and sees if it still produces reasonable state estimates.  No noise is added.
 *
 * @author Peter Abeles
 */
public class ReverseScanMotionValidation extends ScanMotionValidation {

	boolean first;
	long firstTime;

	public ReverseScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		super(estimator);
		setOutputName("ScanMotionReversePerfect.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02","sim03"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	@Override
	public void _performEvaluation() throws IOException {
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

	@Override
	protected Results process( DataSet dataSet ) throws IOException {
		first = true;
		return super.process(dataSet);
	}

	/**
	 * Need to adjust the time stamp so that time flows forward
	 */
	protected void mangleData( RobotLrfObservations data ) {
		if( first ) {
			first = false;
			firstTime = data.getTimeStamp();
		}

		data.setTimeStamp( firstTime-data.getTimeStamp());
	}

	/**
	 * This is where the magic happens.  Input file gets reversed here
	 */
	protected void initialize(DataSet dataSet) throws FileNotFoundException {
		super.initialize(dataSet);

		SerializationDefinitionManager def = new SerializationDefinitionManager();
		def.loadDefinition(RobotLrfObservations.class, "timeStamp", "scanToWorld", "range");
		def.loadDefinition(Se2_F64.class, "x", "y", "yaw");

		InputStream in = new ReverseLineInputStream(dataSet.fileObservations);

		reader = new ReadCsvObjectSmart<RobotLrfObservations>(in, def,
				RobotLrfObservations.class.getSimpleName());
		reader.setComment('#');
		reader.setIgnoreUnparsedData(true);
	}

	public static void main(String[] args) throws IOException {

		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		ReverseScanMotionValidation app = new ReverseScanMotionValidation(alg);
		app.performEvaluation();
	}
}
