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

import boofcv.gui.image.ShowImages;
import bubo.clouds.motion.Lrf2dMotionRollingKeyFrame;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.gui.Simulation2DPanel;
import bubo.io.serialization.SerializationDefinitionManager;
import bubo.io.text.ReadCsvObjectSmart;
import bubo.validation.ValidationBase;
import com.thoughtworks.xstream.XStream;
import georegression.struct.se.Se2_F64;
import org.ddogleg.struct.GrowQueue_F64;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Base class which plays back recorded LRF scan data plus ground truth.  Noise is added to the data and motion
 * estimation performance measured.
 *
 * @author Peter Abeles
 */
// TODO periodic bad scans with structure. look at floor or ceiling
public abstract class ScanMotionValidation extends ValidationBase {
	// how frequently it scores in seconds
	double scorePeriod = 1.0;

	GrowQueue_F64 errorLocation = new GrowQueue_F64();
	GrowQueue_F64 errorAngle = new GrowQueue_F64();
	GrowQueue_F64 totalDistance = new GrowQueue_F64();
	GrowQueue_F64 totalRotate = new GrowQueue_F64();

	List<DataSet> dataSets = new ArrayList<DataSet>();

	protected Lrf2dParam param;
	ReadCsvObjectSmart<RobotLrfObservations> reader;

	Lrf2dMotionRollingKeyFrame estimator;

	Se2_F64 prevFound = new Se2_F64();
	Se2_F64 prevTruth = new Se2_F64();

	Random rand = new Random(234);

	// it will process every X sensor readings
	protected int skipSensor = 1;

	protected boolean showGui = false;
	protected Simulation2DPanel gui;

	public ScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) {
		this.estimator = estimator;
	}

	protected ScanMotionValidation() {
	}

	public void activateVisualization() {
		showGui = true;
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
			out.println("motion travel50 = "+found.travel50);
			out.println("motion travel95 = "+found.travel95);
			out.println("motion rotate50 = "+found.rotate50);
			out.println("motion rotate95 = "+found.rotate95);
			out.println("error location50 = "+found.location50);
			out.println("error location95 = "+found.location95);
			out.println("error angle50    = "+found.angle50);
			out.println("error angle95    = "+found.angle95);
		}
	}

	protected Results process( DataSet dataSet ) throws IOException {
		initialize(dataSet);
		estimator.reset();

		JFrame window = null;
		if( showGui ) {
			gui = new Simulation2DPanel();
			gui.setPreferredSize(new Dimension(640,640));
			gui.configureLrf(param);
			gui.setTotalGhosts(2);
			// ground truth
			gui.getGhosts().get(0).color = Color.RED;
			gui.getGhosts().get(0).radius = 0.1;
			// odometry
			gui.getGhosts().get(1).color = Color.DARK_GRAY;
			gui.getGhosts().get(1).radius = 0.1;

			window = ShowImages.showWindow(gui,"Visualization");
		}

		boolean failed = false;

		RobotLrfObservations data = new RobotLrfObservations(param.getNumberOfScans());
		int count = 0;
		double nextScoreTime = 0;
		while (reader.nextObject(data) != null) {

			mangleData(data);

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

				if( showGui ) {
					gui.updateLidar(noisyObservations);
					gui.updateRobot(estimator.getSensorToWorld());
					gui.updateGhost(0,truthSensorToWorld);
					gui.updateGhost(1,noisySensorToWorld);
					gui.repaint();
					try {Thread.sleep(5);} catch (InterruptedException ignore) {}
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

						totalDistance.add(truth.getTranslation().norm());
						totalRotate.add(Math.abs(truth.getYaw()));
					}
					prevFound.set(estimator.getSensorToWorld());
					prevTruth.set(truthSensorToWorld);
				}
			}
			count++;
		}

		if( showGui ) {
			window.dispose();
			gui = null;
		}

		Arrays.sort(errorLocation.data,0,errorLocation.size);
		Arrays.sort(errorAngle.data,0,errorAngle.size);
		Arrays.sort(totalDistance.data,0,totalDistance.size);
		Arrays.sort(totalRotate.data,0,totalRotate.size);

		int index50 = errorLocation.size/2;
		int index95 = (int)(errorLocation.size*0.95);

		Results result = new Results();
		result.failed = failed;
		result.total = errorAngle.size;
		result.travel50 = totalDistance.get(index50);
		result.travel95 = totalDistance.get(index95);
		result.rotate50 = totalRotate.get(index50);
		result.rotate95 = totalRotate.get(index95);
		result.location50 = errorLocation.get(index50);
		result.location95 = errorLocation.get(index95);
		result.angle50 = errorAngle.get(index50);
		result.angle95 = errorAngle.get(index95);

		return result;
	}

	protected void mangleData( RobotLrfObservations data ){}

	protected abstract Se2_F64 adjustOdometry( Se2_F64 sensorToWorld );

	protected abstract double[] adjustObservations( double ranges[] );

	protected void initialize(DataSet dataSet) throws FileNotFoundException {
		param = (Lrf2dParam)new XStream().fromXML(new FileReader(dataSet.fileLidarParam));

		SerializationDefinitionManager def = new SerializationDefinitionManager();
		def.loadDefinition(RobotLrfObservations.class, "timeStamp", "scanToWorld", "range");
		def.loadDefinition(Se2_F64.class, "x", "y", "yaw");

		reader = new ReadCsvObjectSmart<RobotLrfObservations>(new FileInputStream(dataSet.fileObservations), def,
				RobotLrfObservations.class.getSimpleName());
		reader.setComment('#');
		reader.setIgnoreUnparsedData(true);

		errorLocation.reset();
		errorAngle.reset();
		totalDistance.reset();
		totalRotate.reset();

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

		public double travel50;
		public double travel95;
		public double rotate50;
		public double rotate95;

		public double location50;
		public double location95;
		public double angle50;
		public double angle95;
	}

}
