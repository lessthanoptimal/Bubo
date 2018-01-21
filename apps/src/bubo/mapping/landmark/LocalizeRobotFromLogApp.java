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

package bubo.mapping.landmark;

import boofcv.gui.image.ShowImages;
import bubo.desc.sensors.landmark.RangeBearingMeasurement;
import bubo.desc.sensors.landmark.RangeBearingParam;
import bubo.filters.MultivariateGaussianDM;
import bubo.gui.Simulation2DPanel;
import bubo.gui.UtilDisplayBubo;
import bubo.io.maps.MapIO;
import bubo.log.streams.LogLSe2_F64;
import bubo.log.streams.LogPoseRangeBearing;
import bubo.mapping.localization.d2.landmark.LocalizationKnownRangeBearingEkf;
import bubo.mapping.models.kinematics.LocalMotion2D;
import bubo.mapping.models.kinematics.PredictorLocalMotion2D;
import bubo.maps.d2.LandmarkMap2D;
import com.thoughtworks.xstream.XStream;
import georegression.fitting.ellipse.CovarianceToEllipse_F64;
import georegression.struct.se.Se2_F64;
import org.ddogleg.struct.FastQueue;
import org.ejml.dense.row.CommonOps_DDRM;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class LocalizeRobotFromLogApp {

	private static final long pauseTimeMilli = 10;

	LandmarkMap2D map;
	RangeBearingParam paramRb;
	List<LogLSe2_F64> path;
	List<LogPoseRangeBearing> measurements;

	Simulation2DPanel gui;
	Se2_F64 sensorToRobot;

	CovarianceToEllipse_F64 covToEllipse = new CovarianceToEllipse_F64();

	boolean paused = false;
	boolean takeStep = false;

	public LocalizeRobotFromLogApp(  String directory ) throws FileNotFoundException {
		paramRb = (RangeBearingParam)new XStream().fromXML(new FileInputStream(directory+"sensorRB.xml"));
		map = MapIO.loadLandmarkMap(directory+"landmarks.csv");
		path = MapIO.loadPath2D(directory+"poseTruth.txt");
		measurements = MapIO.loadPoseRangeBearing(directory+"rangeBearing.txt");
		sensorToRobot = (Se2_F64)new XStream().fromXML(new FileInputStream(directory+"sensorToRobot.xml"));

		gui = new Simulation2DPanel();
		gui.setMapLandmarks(map);
		gui.autoPreferredSize();
		gui.setTotalGhosts(2);
		gui.getGhosts().get(0).radius = 0.3;
		gui.getGhosts().get(0).color = Color.RED;
		gui.getGhosts().get(1).radius = 0.3;
		gui.getGhosts().get(1).color = Color.GRAY;

		covToEllipse.setNumStdev(2.5);

		paramRb.rangeSigma = 100;
		paramRb.bearingSigma = .5;
	}

	public void process() {
		ShowImages.showWindow(gui,"Landmark Localization");

		LogLSe2_F64 initial = path.get(0);

		LocalizationKnownRangeBearingEkf<LocalMotion2D> estimator =
				new LocalizationKnownRangeBearingEkf<LocalMotion2D>(new PredictorLocalMotion2D(0.4,0.01,0.1),paramRb);

		estimator.setLandmarks(map);

		MultivariateGaussianDM initState = new MultivariateGaussianDM(3);
		initState.x.data[0] = initial.getX();
		initState.x.data[1] = initial.getY();
		initState.x.data[2] = initial.getYaw();
		initState.P.set(CommonOps_DDRM.diag(0.5, 0.5, 0.05));

		estimator.setInitialState(initState);

		Se2_F64 sensor0ToWorld = sensorToRobot.concat(initial,null);
		LocalMotion2D motion = new LocalMotion2D();

		int indexMeas = 0;

		long time = path.get(0).time;

		// synchronize measurements and path
		while( measurements.get(indexMeas).getTime() < time ) {
			indexMeas++;
		}

		FastQueue<RangeBearingMeasurement> measurementsRB = new FastQueue<RangeBearingMeasurement>(RangeBearingMeasurement.class,true);
		for (int i = 0; i < path.size(); i++) {

			LogLSe2_F64 robotToWorld = path.get(i);
			Se2_F64 sensor1ToWorld = sensorToRobot.concat(robotToWorld,null);

			motion.setFrom(sensor0ToWorld,sensor1ToWorld);
			motion.x += 0.01;
			estimator.predict(motion);

			// process measurements
			time = robotToWorld.getTime();
			measurementsRB.reset();
			while( indexMeas < measurements.size()  ) {
				LogPoseRangeBearing meas = measurements.get(indexMeas);

				if( meas.getTime() <= time ) {
					indexMeas++;
//					System.out.println("   measurement "+meas.getId());
					RangeBearingMeasurement rb = measurementsRB.grow();
					rb.bearing = meas.getBearing();
					rb.range = meas.getRange();
					rb.id = meas.getId();
					estimator.update(rb);
				} else {
					break;
				}
			}

			// draw covariance ellipse
			MultivariateGaussianDM stateDM = estimator.getState();
			if( covToEllipse.setCovariance(stateDM.P.get(0, 0), stateDM.P.get(0, 1), stateDM.P.get(1, 1)) ) {
				gui.updateGhostEllipse(0, covToEllipse.getMinorAxis(), covToEllipse.getMajorAxis(), covToEllipse.getAngle());
			} else {
				gui.updateGhostEllipse(0, 0,0,0);
			}

//			System.out.println("ellipse "+covToEllipse.getMinorAxis()+"  "+covToEllipse.getMajorAxis());

			gui.updateRangeBearing(0,measurementsRB);
			gui.updateGhost(0, estimator.getPose());
			gui.updateGhost(1,robotToWorld);
			gui.repaint();

			sensor0ToWorld.set(sensor1ToWorld);

			UtilDisplayBubo.pause(pauseTimeMilli);
			while (paused) {
				UtilDisplayBubo.pause(5);
				if (takeStep) {
					takeStep = false;
					break;
				}
			}
		}
	}

	private Se2_F64 convertToSensor( Se2_F64 robotToWorld ) {
		return sensorToRobot.concat(robotToWorld,null);
	}

	public static void main(String[] args) throws FileNotFoundException {
		LocalizeRobotFromLogApp app = new LocalizeRobotFromLogApp("data/mapping2d/landmark01/");

		app.process();
	}
}
