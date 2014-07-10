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

package bubo.simulation.d2;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.simulation.d2.features.LineSegmentWorld;
import bubo.simulation.d2.sensors.SimulateLadar2D;
import georegression.metric.ClosestPoint2D_F64;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Vector2D_F64;
import georegression.struct.se.Se2_F64;

/**
 * Simulation of a robot in 2D with a LIDAR sensor on it.  Primary intended to act as a simple way to generator
 * sensor observations.  Does not intend to accurately model physics, collisions, or anything remotely advanced.
 *
 * @author Peter Abeles
 */
public class Simulation2D implements ControlListener {

	// simulation and control models
	RobotInterface user;
	LineSegmentWorld world;
	CircularRobot2D robot;
	SimulateLadar2D sensor;

	// frequency for how often each item is updated, in seconds
	double periodControl;
	double periodOdometry;
	double periodLidar;
	double periodSimulation;

	// simulation time in seconds
	double time;

	// time that the following was last updated, in seconds
	double lastControl;
	double lastOdometry;
	double lastLidar;

	// transforms
	Se2_F64 sensorToWorld = new Se2_F64();
	Se2_F64 newToOld = new Se2_F64();
	Se2_F64 temp = new Se2_F64();

	public Simulation2D(RobotInterface user,
						LineSegmentWorld world,
						Lrf2dParam sensorParam,
						double robotRadius) {
		this.user = user;
		this.world = world;
		this.robot = new CircularRobot2D(robotRadius);
		sensor = new SimulateLadar2D(sensorParam);
	}

	public void setPeriods( double simulation , double control , double odometry , double lidar ) {
		this.periodControl = control;
		this.periodOdometry = odometry;
		this.periodLidar = lidar;
		this.periodSimulation = simulation;
	}

	public void setLocation( double x , double y , double theta ) {
		robot.robotToWorld.set(x,y,theta);
	}

	/**
	 * Runs the simulation for the specified amount of time
	 *
	 * @param lengthOfTime How long the simulation is run for
	 */
	public void run( double lengthOfTime ) {
		user.setIntrinsic(robot.sensorToRobot,sensor.getParam());
		this.time = 0;
		this.lastOdometry = this.lastLidar = this.lastControl = 0;
		while( time <= lengthOfTime ) {
			doStep();
		}
	}

	/**
	 * Performs a single simulation step
	 */
	public void doStep() {
		moveRobot(periodSimulation);
		handleCollisions();

		time += periodSimulation;
		long timeStamp = (long)(time*1000);
		if( time >= lastOdometry+periodOdometry ) {
			lastOdometry = time;
			user.odometry(timeStamp, robot.robotToWorld);
		}
		if( time >= lastLidar+periodLidar ) {
			lastLidar = time;
			robot.sensorToRobot.concat(robot.robotToWorld,sensorToWorld);
			sensor.update(sensorToWorld,world);

			user.ladar(timeStamp, sensor.getMeasurement());
		}
		if( time >= lastControl+periodControl ) {
			lastControl = time;
			user.doControl(timeStamp);
		}
	}

	/**
	 * Moves the robot based on its velocity
	 * @param T integration time
	 */
	protected void moveRobot( double T ) {
		// find its change in motion and update its state
		double theta = robot.angularVelocity*T;
		double dx = (robot.velocity*T)*Math.cos(theta);
		double dy = (robot.velocity*T)*Math.sin(theta);

		newToOld.set(dx, dy, theta);

		// add it to the previous pose and update
		newToOld.concat(robot.robotToWorld,temp);
		robot.robotToWorld.set(temp);
	}

	/**
	 * Sees if the robot is hitting any line.  If it does the robot is moved away from the line a little bit away
	 * from the wall.
	 */
	protected void handleCollisions() {
		Vector2D_F64 T = robot.robotToWorld.getTranslation();
		Point2D_F64 p = new Point2D_F64(T.x,T.y);
		Point2D_F64 c = new Point2D_F64();
		int numCycles;
		for( numCycles = 0; numCycles < 10; numCycles++ ) {
			boolean collision = false;
			for (int i = 0; i < world.lines.size(); i++) {
				LineSegment2D_F64 line = world.lines.get(i);

				ClosestPoint2D_F64.closestPoint(line,p,c);
				double d = p.distance2(c);
				if (d < robot.radius*robot.radius ) {
					// Collision!  Move the robot away from the wall
					collision = true;
					double slopeX = p.x - c.x;
					double slopeY = p.y - c.y;

					d = Math.sqrt(d);
					// move it a little bit extra so that it isn't a glancing blow
					p.x += slopeX*(d*1.0001)/d;
					p.y += slopeY*(d*1.0001)/d;
				}
			}
			if( !collision )
				break;
		}
		if( numCycles == 10 ) {
			throw new RuntimeException("Oh crap.  Cant move without hitting something");
		}
		robot.robotToWorld.T.set(p.x,p.y);
	}

	@Override
	public void sendControl(double velocity, double anglularVelocity) {
		robot.angularVelocity = anglularVelocity;
		robot.velocity = velocity;
	}

	@Override
	public Se2_F64 _truthRobotToWorld() {
		return robot.robotToWorld;
	}

	public Se2_F64 getSensorToRobot() {
		return robot.sensorToRobot;
	}

	public double getTime() {
		return time;
	}
}
