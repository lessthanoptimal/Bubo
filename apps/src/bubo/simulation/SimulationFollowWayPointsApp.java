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

package bubo.simulation;

import boofcv.gui.image.ShowImages;
import bubo.desc.sensors.landmark.RangeBearingParam;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.gui.Simulation2DPanel;
import bubo.io.maps.MapIO;
import bubo.maps.d2.LandmarkMap2D;
import bubo.maps.d2.lines.LineSegmentMap;
import bubo.simulation.d2.CircularRobot2D;
import bubo.simulation.d2.Simulation2D;
import com.thoughtworks.xstream.XStream;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.shapes.Rectangle2D_F64;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Peter Abeles
 */
// TODO provide controls for the user zooming and translating
// TODO Follow robot mode?
public class SimulationFollowWayPointsApp {

	boolean skipPause = true;

	Simulation2D sim;
	Simulation2DPanel gui;
	FollowPathCheatingRobot planner;

	public SimulationFollowWayPointsApp(String wallName, String landmarkName , String pathName) throws IOException {
		LineSegmentMap mapWall = null;
		LandmarkMap2D mapLandmarks = null;

		try {mapWall = MapIO.loadLineSegmentMap(wallName);
		} catch( RuntimeException ignore) {}
		try {mapLandmarks = MapIO.loadLandmarkMap(landmarkName);
		} catch( RuntimeException ignore) {}

		List<Point2D_F64> wayPoints = (List<Point2D_F64>)new XStream().fromXML(new FileInputStream(pathName));

//		planner = new FollowPathCheatingRobot(1,0.4,wayPoints);
		planner = new FollowPathLoggingRobot(1,0.4,wayPoints);

		// put the robot at the initial location facing the second way point
		Point2D_F64 p0 = wayPoints.get(0);
		Point2D_F64 p1 = wayPoints.get(1);
		double yaw = Math.atan2(p1.y-p0.y,p1.x-p0.x);

		CircularRobot2D robot = new CircularRobot2D(0.2);
		robot.getRobotToWorld().set(p0.x, p0.y, yaw);
		robot.getSensorToRobot().T.set(0.15,0);

		sim = new Simulation2D(planner,robot);
		gui = new Simulation2DPanel();

		Rectangle2D_F64 r = null;
		if( mapWall != null ) {
			// SICK like sensor
			Lrf2dParam param = new Lrf2dParam(null, Math.PI / 2.0, -Math.PI, 180, 5, 0, 0);
			r = mapWall.computeBoundingRectangle();
			sim.setWalls(mapWall);
			gui.setMapWalls(mapWall);
			sim.setLaserRangeFinder(param);
			gui.configureLrf(param);
		}

		if( mapLandmarks != null ) {
			RangeBearingParam param = new RangeBearingParam(5);
			r = mapLandmarks.computeBoundingRectangle();
			sim.setLandmarks(mapLandmarks,param);
			gui.setMapLandmarks(mapLandmarks);
		}

		sim.setPeriods(0.005,0.03,100,0.03);

		Se2_F64 centerToWorld = new Se2_F64();
		centerToWorld.T.set((r.p0.x+r.p1.x)/2.0,(r.p0.y+r.p1.y)/2.0);

		gui.setViewCenter(centerToWorld);
		gui.autoPreferredSize();
		gui.setMinimumSize(gui.getPreferredSize());

		ShowImages.showWindow(gui,"Simulation");
	}

	public void process() {
		sim.initialize();

		long sleepTime = Math.max(1,(int)(sim.getPeriodSimulation()*1000));
		while( !planner.isDone() ) {
			sim.doStep();
			gui.updateRobot(sim.getRobot());
			if( sim.getSimulatedLadar() != null )
				gui.updateLidar(sim.getSimulatedLadar().getMeasurement());
			if( sim.getSensorLandmarks() != null )
				gui.updateRangeBearing(sim.getSensorLandmarks().getMeasurements());
			gui.repaint();

			if( !skipPause ) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ignore) {
				}
			}
		}
		System.out.println("Done!");
	}

	public static void main(String[] args) throws IOException {
		String wallName = "walls.csv";
		String landmarkName = "landmarks.csv";
		String wayPointsName = "path.xml";

		wallName = null;
//		landmarkName = null;

		SimulationFollowWayPointsApp app = new SimulationFollowWayPointsApp(wallName,landmarkName,wayPointsName);
		app.process();
	}


}
