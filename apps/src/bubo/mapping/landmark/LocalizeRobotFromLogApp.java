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
import bubo.desc.sensors.landmark.RangeBearingParam;
import bubo.filters.MultivariateGaussianDM;
import bubo.gui.Simulation2DPanel;
import bubo.gui.UtilDisplayBubo;
import bubo.io.maps.MapIO;
import bubo.localization.d2.landmark.LocalizationKnownRangeBearingEkf;
import bubo.log.streams.LogPoseRangeBearing;
import bubo.log.streams.LogSe2_F64;
import bubo.maps.d2.LandmarkMap2D;
import bubo.models.kinematics.PredictorSe2;
import com.thoughtworks.xstream.XStream;
import georegression.struct.se.Se2_F64;
import org.ejml.ops.CommonOps;

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
	List<LogSe2_F64> path;
	List<LogPoseRangeBearing> measurements;

	Simulation2DPanel gui;
	Se2_F64 sensorToRobot;


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

	}

	public void process() {
		ShowImages.showWindow(gui,"Landmark Localization");

		LogSe2_F64 initial = path.get(0);

		LocalizationKnownRangeBearingEkf<Se2_F64> estimator =
				new LocalizationKnownRangeBearingEkf<Se2_F64>(new PredictorSe2(0.05,0.001,0.05),paramRb);

		MultivariateGaussianDM initState = new MultivariateGaussianDM(3);
		initState.x.data[0] = initial.getX();
		initState.x.data[1] = initial.getY();
		initState.x.data[2] = initial.getYaw();
		initState.P.set(CommonOps.diag(4, 4, 0.2));

		estimator.setInitialState(initState);

		Se2_F64 sensor0ToWorld = sensorToRobot.concat(initial,null);
		Se2_F64 sensor0ToSensor1 = new Se2_F64();

		for (int i = 0; i < path.size(); i++) {

			Se2_F64 robotToWorld = path.get(i);
			Se2_F64 sensor1ToWorld = sensorToRobot.concat(robotToWorld,null);
			sensor0ToWorld.concat(sensor1ToWorld.invert(null),sensor0ToSensor1);

			estimator.predict(sensor0ToSensor1);

			gui.updateGhost(0,estimator.getPose());
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

		// TODO create initialization algorithm

		// draw covariance ellipse
	}

	private Se2_F64 convertToSensor( Se2_F64 robotToWorld ) {
		return sensorToRobot.concat(robotToWorld,null);
	}

	public static void main(String[] args) throws FileNotFoundException {
		LocalizeRobotFromLogApp app = new LocalizeRobotFromLogApp("data/mapping2d/landmark01/");

		app.process();
	}
}
