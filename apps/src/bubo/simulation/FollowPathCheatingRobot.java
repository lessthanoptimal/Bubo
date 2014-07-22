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

import bubo.desc.sensors.landmark.RangeBearingMeasurement;
import bubo.desc.sensors.landmark.RangeBearingParam;
import bubo.desc.sensors.lrf2d.Lrf2dMeasurement;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.simulation.d2.ControlListener2D;
import bubo.simulation.d2.RobotInterface2D;
import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;

import java.util.List;

/**
 * Robot which uses ground truth to mindlessly follow a set of waypoints.  How fast it travels and turns is set
 * by the user.  If it needs to turn it does a point turn.  If there is an obstacle in its way it will just
 * keep on crashing into it.
 *
 * @author Peter Abeles
 */
public class FollowPathCheatingRobot implements RobotInterface2D {

	List<Point2D_F64> waypoints;
	int target;

	ControlListener2D listener;
	double velocity;
	double angularVelocity;

	long timePrev;
	boolean turning;

	public FollowPathCheatingRobot(double velocity, double angularVelocity ,
								   List<Point2D_F64> waypoints) {
		this.velocity = velocity;
		this.angularVelocity = angularVelocity;
		this.waypoints = waypoints;
	}

	@Override
	public void doControl(long timeStamp) {
		if( timePrev == 0 || target >= waypoints.size()) {
			timePrev = timeStamp;
			turning = false;
			return;
		}

		double T = (timeStamp-timePrev)/1000.0;
		double travel = T*velocity;

		timePrev = timeStamp;

		Point2D_F64 wp = waypoints.get(target);

		Se2_F64 robotToWorld = listener._truthRobotToWorld();

		Point2D_F64 x = new Point2D_F64(robotToWorld.getX(),robotToWorld.getY());

		double d = x.distance(wp);

		// if it's very close just jump to the way point
		if( d < travel ) {
			target++;
			Se2_F64 pose = new Se2_F64(wp.x,wp.y,robotToWorld.getYaw());
			listener._setPose(pose);
		} else {
			Se2_F64 pose;
			double dx = (wp.x-x.x)/d;
			double dy = (wp.y-x.y)/d;

			double desired = Math.atan2( dy,dx);
			double actual = robotToWorld.getYaw();
			// see if it's facing the correct direction
			double angDist = UtilAngle.dist(desired,actual);
			if(angDist > 1e-4 ) {
				// turn towards the correct direction without moving
				double maxTurn = T*angularVelocity;
				if( maxTurn < angDist ) {
					if( UtilAngle.distanceCCW(actual,desired) <= Math.PI )
						desired = actual + maxTurn;
					else
						desired = actual - maxTurn;
				}
				pose = new Se2_F64(x.x,x.y,desired);
			} else {
				double xx = robotToWorld.getX() + dx*travel;
				double yy = robotToWorld.getY() + dy*travel;

				pose = new Se2_F64(xx,yy,desired);
			}

			listener._setPose(pose);
		}

	}

	@Override
	public void setControlListener(ControlListener2D listener) {
		this.listener = listener;
	}

	@Override
	public void setIntrinsic(Se2_F64 ladarToRobot, Lrf2dParam param, RangeBearingParam paramRb) {

	}

	@Override
	public void odometry(long timeStamp, Se2_F64 robotToWorld) {

	}

	@Override
	public void ladar(long timeStamp, Lrf2dMeasurement measurement) {

	}

	@Override
	public void rangeBearing(long timeStamp, RangeBearingMeasurement measurement) {

	}

	@Override
	public boolean isDone() {
		return target >= waypoints.size();
	}
}
