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

package bubo.gui;

import bubo.desc.sensors.landmark.RangeBearingMeasurement;
import bubo.desc.sensors.lrf2d.Lrf2dMeasurement;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.desc.sensors.lrf2d.Lrf2dPrecomputedTrig;
import bubo.gui.maps.MapDisplay;
import bubo.simulation.d2.CircularRobot2D;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.shapes.EllipseRotated_F64;
import georegression.transform.se.SePointOps_F64;
import org.ddogleg.struct.FastQueue;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class Simulation2DPanel extends MapDisplay {

	final CircularRobot2D robot = new CircularRobot2D();
	final List<Ghost> ghosts = new ArrayList<Ghost>();

	// LRF stuff
	Lrf2dMeasurement measurementLrf;
	Lrf2dPrecomputedTrig trig;
	double maxRange;

	// Landmark Stuff
	final FastQueue<RangeBearingMeasurement> rangeBearing =
			new FastQueue<RangeBearingMeasurement>(RangeBearingMeasurement.class,true);

	Se2_F64 lrfToCenter = new Se2_F64();

	// work space
	Se2_F64 robotToCenter = new Se2_F64();
	Se2_F64 worldToCenter = new Se2_F64();

	public void setTotalGhosts( int total ) {
		ghosts.clear();
		for (int i = 0; i < total; i++) {
			ghosts.add( new Ghost() );
		}
	}

	public void configureLrf( Lrf2dParam param ) {
		this.trig = new Lrf2dPrecomputedTrig(param);
		this.measurementLrf = new Lrf2dMeasurement(param.getNumberOfScans());
		this.maxRange = param.getMaxRange();
	}

	public void updateGhost( int which , Se2_F64 robotToWorld ) {
		synchronized (ghosts){
			ghosts.get(which).getRobotToWorld().set(robotToWorld);
		}
	}

	public void updateGhostEllipse( int which , double minor , double major , double theta  ) {
		synchronized (ghosts){
			ghosts.get(which).ellipse.set(0,0,major,minor,theta);
		}
	}

	public void updateRobot( CircularRobot2D robot ) {
		synchronized ( this.robot ) {
			this.robot.set(robot);
		}
	}

	public void updateRobot( Se2_F64 robotToWorld ) {
		synchronized ( this.robot ) {
			this.robot.getRobotToWorld().set(robotToWorld);
		}
	}

	public void updateLidar( Lrf2dMeasurement measurements ) {
		synchronized ( this.measurementLrf) {
			this.measurementLrf.setMeasurements(measurements);
		}
	}

	public void updateLidar( double[] measurements ) {
		synchronized ( this.measurementLrf) {
			this.measurementLrf.setMeasurements(measurements);
		}
	}

	public void updateRangeBearing( FastQueue<RangeBearingMeasurement> measurements ) {
		synchronized ( this.rangeBearing) {
			this.rangeBearing.reset();
			for (int i = 0; i < measurements.size; i++) {
				this.rangeBearing.grow().set(measurements.get(i));
			}
		}
	}

	public void updateRangeBearing( int which , FastQueue<RangeBearingMeasurement> measurements ) {
		FastQueue<RangeBearingMeasurement> rangeBearing = ghosts.get(which).rangeBearing;
		synchronized ( rangeBearing) {
			rangeBearing.reset();
			for (int i = 0; i < measurements.size; i++) {
				rangeBearing.grow().set(measurements.get(i));
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;

		synchronized ( robot ) {
			updateTransforms();
		}

		if( measurementLrf != null ) {
			synchronized (measurementLrf) {
				g2.setColor(Color.BLUE);
				int lrfX = (int) Math.round(lrfToCenter.T.x * metersToPixels);
				int lrfY = (int) Math.round(lrfToCenter.T.y * metersToPixels);

				Point2D_F64 x = new Point2D_F64();
				for (int i = 0; i < measurementLrf.numMeas; i++) {
					double r = measurementLrf.meas[i];
					if (r == maxRange)
						continue;
					trig.computeEndPoint(i, measurementLrf.meas[i], x);
					SePointOps_F64.transform(lrfToCenter, x, x);
					int scanX = (int) Math.round(x.x * metersToPixels);
					int scanY = (int) Math.round(x.y * metersToPixels);

					drawLine(g2, lrfX, lrfY, scanX, scanY);
				}
			}
		}

		synchronized ( rangeBearing ) {
			renderRangeBearing(g2,lrfToCenter,rangeBearing);
		}

		synchronized ( robot ) {
			drawRobot(g2);
		}

		synchronized ( ghosts ) {
			for (int i = 0; i < ghosts.size(); i++) {
				drawGhost(g2,ghosts.get(i));
			}
		}
	}

	private void renderRangeBearing(Graphics2D g2 , Se2_F64 lrfToCenter , FastQueue<RangeBearingMeasurement> rangeBearing ) {
		g2.setColor(Color.CYAN);
		int sensorX = (int) Math.round(lrfToCenter.T.x * metersToPixels);
		int sensorY = (int) Math.round(lrfToCenter.T.y * metersToPixels);

		Point2D_F64 x = new Point2D_F64();
		for (int i = 0; i < rangeBearing.size; i++) {
			double r = rangeBearing.get(i).range;
			double bearing = rangeBearing.get(i).bearing;

			x.x = Math.cos(bearing)*r;
			x.y = Math.sin(bearing)*r;

			SePointOps_F64.transform(lrfToCenter, x, x);
			int scanX = (int) Math.round(x.x * metersToPixels);
			int scanY = (int) Math.round(x.y * metersToPixels);

			drawLine(g2, sensorX, sensorY, scanX, scanY);
		}
	}

	private void updateTransforms() {
		Se2_F64 robotToWorld = robot.getRobotToWorld();
		centerToWorld.invert(worldToCenter);
		robotToWorld.concat(worldToCenter,robotToCenter);
		robot.getSensorToRobot().concat(robotToCenter, lrfToCenter);
	}

	private void drawRobot(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		int robotX = (int)Math.round(robotToCenter.T.x*metersToPixels);
		int robotY = (int)Math.round(robotToCenter.T.y*metersToPixels);
		int pradius = (int)Math.round(robot.getRadius()*metersToPixels);
		int pwidth = (int)Math.round(2*robot.getRadius()*metersToPixels);

		// create a line indicating where the robot is facing
		Point2D_F64 x = new Point2D_F64(robot.getRadius(),0);
		SePointOps_F64.transform(robotToCenter, x, x);

		int dirX = (int)Math.round(x.x*metersToPixels);
		int dirY = (int)Math.round(x.y*metersToPixels);

		drawOval(g2, robotX - pradius, robotY - pradius, pwidth, pwidth);
		drawLine(g2, robotX, robotY, dirX, dirY);
	}

	private void drawGhost(Graphics2D g2, Ghost ghost ) {
		g2.setColor(ghost.color);

		ghost.getRobotToWorld().concat(worldToCenter, robotToCenter);

		int robotX = (int)Math.round(robotToCenter.T.x*metersToPixels);
		int robotY = (int)Math.round(robotToCenter.T.y*metersToPixels);
		int pradius = (int)Math.round(ghost.getRadius()*metersToPixels);
		int pwidth = (int)Math.round(2*ghost.getRadius()*metersToPixels);

		// create a line indicating where the robot is facing
		Point2D_F64 x = new Point2D_F64(ghost.getRadius(),0);
		SePointOps_F64.transform(robotToCenter, x, x);

		int dirX = (int)Math.round(x.x*metersToPixels);
		int dirY = (int)Math.round(x.y*metersToPixels);

		drawOval(g2, robotX - pradius, robotY - pradius, pwidth, pwidth);
		drawLine(g2, robotX, robotY, dirX, dirY);


		synchronized ( rangeBearing ) {
			FastQueue<RangeBearingMeasurement> rangeBearing = ghost.rangeBearing;
			renderRangeBearing(g2,robotToCenter,rangeBearing);
		}

		if( ghost.ellipse.a != 0 ) {
			drawEllipseRotated(g2,robotX,robotY,ghost.ellipse.b,ghost.ellipse.a,ghost.ellipse.phi);
		}

	}

	/**
	 * Returns all the ghosts.  It's unsafe to use this function after the GUI has started
	 */
	public List<Ghost> getGhosts() {
		return ghosts;
	}

	public static class Ghost extends CircularRobot2D {
		public Color color;
		public EllipseRotated_F64 ellipse = new EllipseRotated_F64();
		public FastQueue<RangeBearingMeasurement> rangeBearing =
				new FastQueue<RangeBearingMeasurement>(RangeBearingMeasurement.class,true);
	}
}
