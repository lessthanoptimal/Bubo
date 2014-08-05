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
import bubo.maps.d2.lines.LineSegmentMap;
import bubo.simulation.d2.sensors.SimulateLrf2D;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.se.Se2_F64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This test is designed to simulate the robot seeing the floor on rough terrain.  There is a chance of it seeing
 * the floor.  When that happens the scan is replaced by a random line scan for N+2 scans where N is a random
 * number.
 *
 * @author Peter Abeles
 */
public class RandomScanScanMotionValidation extends BaseNoiseScanMotionValidation {

	double changeBadScan;
	int length;

	List<double[]> badScans = new ArrayList<double[]>();

	public RandomScanScanMotionValidation(Lrf2dMotionRollingKeyFrame estimator) throws FileNotFoundException {
		this.estimator = estimator;
		setOutputName("ScanMotionFloorScans.txt");

		String dataDir = "data/mapping2d/";
		String sets[] = new String[]{"sim02"};

		for( String set : sets) {
			addDataSet(dataDir+set+"/observations.txt",dataDir+set+"/lrf.xml");
		}
	}

	@Override
	protected void initialize(DataSet dataSet) throws FileNotFoundException {
		super.initialize(dataSet);

		SimulateLrf2D sim = new SimulateLrf2D(param);

		LineSegmentMap map = new LineSegmentMap();

		LineSegment2D_F64 line = new LineSegment2D_F64();
		map.lines.add(line);

		badScans.clear();
		for (int i = 0; i < 10; i++) {
			double x = i*4.0/9.0+0.1;
			line.set(x,-10,x,10);
			sim.update(new Se2_F64(),map);

			badScans.add( sim.getMeasurement().meas.clone());
		}
	}

	@Override
	public void _performEvaluation() throws IOException {
		// Noise on LRF
		lrfRangeSigma = 0.01;
		length = 0;
		for( int i = 0; i <= 5; i++ ) {
			out.println("=========================================");
			changeBadScan = 0.001 * Math.pow(2,i);
			out.println("floorScanProb "+changeBadScan);
			System.out.println("floorScanProb "+changeBadScan);
			printNoise();
			super.evaluateDataSets();
		}
	}

	@Override
	protected double[] adjustObservations(double[] ranges) {
		if( length <= 0 && rand.nextDouble() <= changeBadScan ) {
			length = rand.nextInt(5)+2;
		}
		if( length> 0 ) {
			length--;
			return badScans.get( rand.nextInt(badScans.size()));
		}
		return ranges;
	}

	public static void main(String[] args) throws IOException {

		Lrf2dMotionRollingKeyFrame alg = FactoryEvaluateScanMotion.createIcpLocal();

		RandomScanScanMotionValidation app = new RandomScanScanMotionValidation(alg);
//		app.activateVisualization();
		app.performEvaluation();
	}
}
