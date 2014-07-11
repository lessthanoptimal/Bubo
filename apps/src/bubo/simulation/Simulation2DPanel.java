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

import bubo.desc.sensors.lrf2d.Lrf2dMeasurement;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.desc.sensors.lrf2d.Lrf2dPrecomputedTrig;
import bubo.gui.maps.LineMapDisplay;
import bubo.simulation.d2.CircularRobot2D;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.transform.se.SePointOps_F64;

import java.awt.*;

/**
 * @author Peter Abeles
 */
public class Simulation2DPanel extends LineMapDisplay {

	final CircularRobot2D robot = new CircularRobot2D();

	final Lrf2dMeasurement measurement;
	final Lrf2dPrecomputedTrig trig;
	final double maxRange;


	Se2_F64 lrfToWorld = new Se2_F64();

	public Simulation2DPanel(Lrf2dParam param) {
		this.trig = new Lrf2dPrecomputedTrig(param);
		this.measurement = new Lrf2dMeasurement(param.getNumberOfScans());
		this.maxRange = param.getMaxRange();
	}

	public void updateRobot( CircularRobot2D robot ) {
		synchronized ( this.robot ) {
			this.robot.set(robot);
		}
	}

	public void updateLidar( Lrf2dMeasurement measurements ) {
		synchronized ( this.measurement ) {
			this.measurement.setMeasurements(measurements);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;

		int centerX = getWidth()/2;
		int centerY = getHeight()/2;

		synchronized ( robot ) {
			g2.setColor(Color.BLACK);
			Se2_F64 robotToWorld = robot.getRobotToWorld();

			int robotX = (int)(robotToWorld.T.x*metersToPixels+0.5) + centerX;
			int robotY = (int)(robotToWorld.T.y*metersToPixels+0.5) + centerY;
			int pradius = (int)(robot.getRadius()*metersToPixels+0.5);
			int pwidth = (int)(2*robot.getRadius()*metersToPixels+0.5);

			// create a line indicating where the robot is facing
			Point2D_F64 x = new Point2D_F64(robot.getRadius(),0);
			SePointOps_F64.transform(robotToWorld,x,x);

			int dirX = (int)(x.x*metersToPixels+0.5) + centerX;
			int dirY = (int)(x.y*metersToPixels+0.5) + centerY;

			g2.drawOval(robotX-pradius,robotY-pradius,pwidth,pwidth);
			g2.drawLine(robotX,robotY,dirX,dirY);

			robot.getSensorToRobot().concat(robotToWorld,lrfToWorld);
		}

		synchronized ( measurement ) {

			g2.setColor(Color.BLUE);
			int lrfX = (int)(lrfToWorld.T.x*metersToPixels+0.5) + centerX;
			int lrfY = (int)(lrfToWorld.T.y*metersToPixels+0.5) + centerY;

			Point2D_F64 x = new Point2D_F64();
			for (int i = 0; i < measurement.numMeas; i++) {
				double r = measurement.meas[i];
				if( r == maxRange )
					continue;
				trig.computeEndPoint(i, measurement.meas[i], x);
				SePointOps_F64.transform(lrfToWorld,x,x);
				int scanX = (int)(x.x*metersToPixels+0.5) + centerX;
				int scanY = (int)(x.y*metersToPixels+0.5) + centerY;

				g2.drawLine(lrfX,lrfY,scanX,scanY);
			}

		}

	}

}
