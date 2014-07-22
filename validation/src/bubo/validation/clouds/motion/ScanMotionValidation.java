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
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.io.serialization.SerializationDefinitionManager;
import bubo.io.text.ReadCsvObjectSmart;
import bubo.validation.ValidationBase;
import com.thoughtworks.xstream.XStream;
import georegression.struct.se.Se2_F64;
import org.ddogleg.struct.GrowQueue_F64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
// TODO make noise for sensors into common base class
// TODO initialize with no walls visible.
// TODO periodic bad scans with structure. look at floor or ceiling
// TODO Pathological tests.  No data.  One wall.  Long hallway
public abstract class ScanMotionValidation extends ValidationBase {
	// how frequently it scores in seconds
	double scorePeriod = 1.0;

	GrowQueue_F64 errorLocation = new GrowQueue_F64();
	GrowQueue_F64 errorAngle = new GrowQueue_F64();

	List<DataSet> dataSets = new ArrayList<DataSet>();

	Lrf2dParam param;
	ReadCsvObjectSmart<RobotLrfObservations> reader;

	Lrf2dMotionRollingKeyFrame estimator;

	Se2_F64 prevFound = new Se2_F64();
	Se2_F64 prevTruth = new Se2_F64();

	Random rand = new Random(234);

	// it will process every X sensor readings
	protected int skipSensor = 1;

	public ScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) {
		this.estimator = estimator;
	}

	protected ScanMotionValidation() {
	}

	protected void addDataSet( String fileObservations , String fileLidarParam ) {
		dataSets.add( new DataSet(fileObservations,fileLidarParam));
	}

	protected void evaluateDataSets() throws IOException {
		for ( DataSet s : dataSets ) {
			System.out.println("Processing: "+s.fileObservations);
			Results found = process(s);

			out.println("----------------------------------");
			out.println("File: "+s.fileObservations);
			out.println("score period = "+scorePeriod);
			out.println("    **** METRICS ****");
			out.println("failed     = "+found.failed);
			out.println("total      = "+found.total);
			out.println("location50 = "+found.location50);
			out.println("location95 = "+found.location95);
			out.println("angle50    = "+found.angle50);
			out.println("angle95    = "+found.angle95);
		}
	}

	protected Results process( DataSet dataSet ) throws IOException {
		initialize(dataSet);
		estimator.reset();

		boolean failed = false;

		RobotLrfObservations data = new RobotLrfObservations(param.getNumberOfScans());
		int count = 0;
		double nextScoreTime = 0;
		while (reader.nextObject(data) != null) {

			double time = data.getTimeStamp()/1000.0;

			if( count == 0 ) {
				nextScoreTime = time;
			}

			Se2_F64 truthSensorToWorld = data.getScanToWorld().copy();
			Se2_F64 noisySensorToWorld = adjustOdometry(data.getScanToWorld());
			double noisyObservations[] = adjustObservations(data.getRange());

			if( count % skipSensor == 0 ) {
				try {
					estimator.process(noisySensorToWorld, noisyObservations);
				} catch( RuntimeException ignore ) {
					failed = true;
					break;
				}

				if (nextScoreTime <= time) {
					while (nextScoreTime <= time) {
						nextScoreTime += scorePeriod;
					}
					if (count != 0) {
						Se2_F64 found = estimator.getSensorToWorld().concat(prevFound.invert(null), null);
						Se2_F64 truth = truthSensorToWorld.concat(prevTruth.invert(null), null);
						Se2_F64 difference = found.concat(truth.invert(null), null);

						errorLocation.add(difference.getTranslation().norm());
						errorAngle.add(Math.abs(difference.getYaw()));
					}
					prevFound.set(estimator.getSensorToWorld());
					prevTruth.set(truthSensorToWorld);
				}
			}
			count++;
		}

		Arrays.sort(errorLocation.data,0,errorLocation.size);
		Arrays.sort(errorAngle.data,0,errorAngle.size);

		int index50 = errorLocation.size/2;
		int index95 = (int)(errorLocation.size*0.95);

		Results result = new Results();
		result.failed = failed;
		result.total = errorAngle.size;
		result.location50 = errorLocation.get(index50);
		result.location95 = errorLocation.get(index95);
		result.angle50 = errorAngle.get(index50);
		result.angle95 = errorAngle.get(index95);

		return result;
	}

	protected abstract Se2_F64 adjustOdometry( Se2_F64 sensorToWorld );

	protected abstract double[] adjustObservations( double ranges[] );

	protected void initialize(DataSet dataSet) throws FileNotFoundException {
		param = (Lrf2dParam)new XStream().fromXML(new File(dataSet.fileLidarParam));

		SerializationDefinitionManager def = new SerializationDefinitionManager();
		def.loadDefinition(RobotLrfObservations.class, "timeStamp", "scanToWorld", "range");
		def.loadDefinition(Se2_F64.class, "x", "y", "yaw");

		reader = new ReadCsvObjectSmart<RobotLrfObservations>(new FileInputStream(dataSet.fileObservations), def,						RobotLrfObservations.class.getSimpleName());
		reader.setComment('#');
		reader.setIgnoreUnparsedData(true);

		errorLocation.reset();
		errorAngle.reset();

		estimator.init(param);
	}

	public static class DataSet {
		String fileObservations;
		String fileLidarParam;

		public DataSet(String fileObservations, String fileLidarParam) {
			this.fileObservations = fileObservations;
			this.fileLidarParam = fileLidarParam;
		}
	}

	public static class Results
	{
		public boolean failed;
		public int total;
		public double location50;
		public double location95;
		public double angle50;
		public double angle95;
	}

}
