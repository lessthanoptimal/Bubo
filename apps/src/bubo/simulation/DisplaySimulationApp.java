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
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.maps.d2.lines.LineSegmentMap;
import bubo.simulation.d2.CircularRobot2D;
import bubo.simulation.d2.Simulation2D;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.shapes.Rectangle2D_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
// TODO provide controls for the user zooming and translating
// TODO Follow robot mode?
public class DisplaySimulationApp {

	Simulation2D sim;
	Simulation2DPanel gui;

	public DisplaySimulationApp() {
		LineSegmentMap map = new LineSegmentMap();

		map.add(-10,4,10,4);
		map.add(-10,4,-10,-4);
		map.add(-10,-4,10,-4);
		map.add(10,-4,10,4);
		map.add(-2,-1.5,-2,0.3);
		map.add(-3,1.6,0.5,0.5);

		List<Point2D_F64> waypoints = new ArrayList<Point2D_F64>();
		waypoints.add( new Point2D_F64(8,2));
		waypoints.add( new Point2D_F64(-8,2));
		waypoints.add( new Point2D_F64(-8,-2));
		waypoints.add( new Point2D_F64(8,-2));
		waypoints.add( new Point2D_F64(8,2));

		FollowPathCheatingRobot planner = new FollowPathCheatingRobot(1,0.4,waypoints);

		Lrf2dParam param = new Lrf2dParam(null,Math.PI/2.0,-Math.PI,180,5,0,0);

		CircularRobot2D robot = new CircularRobot2D(0.2);
		robot.getSensorToRobot().T.set(0.15,0);

		sim = new Simulation2D(planner,map,param,robot);
		sim.setPeriods(0.005,0.01,100,0.01);

		Rectangle2D_F64 r = map.computeBoundingRectangle();
		Se2_F64 centerToWorld = new Se2_F64();
		centerToWorld.T.set(r.getX()+r.width/2,r.getY()+r.height/2);

		gui = new Simulation2DPanel(param);
		gui.setMap(map);
		gui.setViewCenter(centerToWorld);
		gui.autoPreferredSize();
		gui.setMinimumSize(gui.getPreferredSize());

		ShowImages.showWindow(gui,"Simulation");
	}

	public void process() {
		sim.initialize();

		long sleepTime = Math.max(1,(int)(sim.getPeriodSimulation()*1000));
		while( sim.getTime() < 100 ) {
			sim.doStep();
			gui.updateRobot(sim.getRobot());
			gui.updateLidar(sim.getSimulatedLadar().getMeasurement());
			gui.repaint();

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ignore) {}
		}
	}

	public static void main(String[] args) {
		DisplaySimulationApp app = new DisplaySimulationApp();
		app.process();
	}


}
