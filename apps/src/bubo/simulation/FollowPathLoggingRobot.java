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
import com.thoughtworks.xstream.XStream;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Logs the robot's observations as it moves
 *
 * @author Peter Abeles
 */
public class FollowPathLoggingRobot extends FollowPathCheatingRobot {

	Se2_F64 ladarToRobot;
	Se2_F64 ladarToWorld = new Se2_F64();
	PrintStream outLrf;
	PrintStream outRB;
	PrintStream outPose;

	public FollowPathLoggingRobot(double velocity, double angularVelocity, List<Point2D_F64> wayPoints) {
		super(velocity, angularVelocity, wayPoints);

		try {
			outLrf = new PrintStream("scansLrf.txt");
			outLrf.println("# time-stamp x y yaw [ranges ... N]");

			outRB = new PrintStream("rangeBearing.txt");
			outRB.println("# time-stamp x y yaw id range bearing");

			outPose = new PrintStream("poseTruth.txt");
			outPose.println("# time-stamp x y yaw");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void ladar(long timeStamp, Lrf2dMeasurement measurement) {
		Se2_F64 robotToWorld =  listener._truthRobotToWorld();

		ladarToRobot.concat(robotToWorld,ladarToWorld);

		outLrf.printf("%d %f %f %f", timeStamp, ladarToWorld.getX(), ladarToWorld.getY(), ladarToWorld.getYaw());
		for (int i = 0; i < measurement.numMeas; i++) {
			outLrf.printf(" %f", measurement.meas[i]);
		}
		outLrf.println();
		outLrf.flush();
	}

	@Override
	public void rangeBearing(long timeStamp, RangeBearingMeasurement measurement) {
		Se2_F64 robotToWorld =  listener._truthRobotToWorld();

		ladarToRobot.concat(robotToWorld,ladarToWorld);

		outRB.printf("%d %f %f %f ", timeStamp,ladarToWorld.getX(), ladarToWorld.getY(), ladarToWorld.getYaw());
		outRB.printf("%d %.10f %.10f", measurement.id, measurement.range, measurement.bearing);
		outRB.println();
		outRB.flush();
	}

	@Override
	public void doControl(long timeStamp) {
		super.doControl(timeStamp);

		Se2_F64 robotToWorld =  listener._truthRobotToWorld();
		outPose.printf("%d %.10f %.10f %.10f\n",timeStamp,robotToWorld.getX(),robotToWorld.getY(),robotToWorld.getYaw());
		outPose.flush();
	}

	@Override
	public void setIntrinsic(Se2_F64 ladarToRobot, Lrf2dParam paramLrf, RangeBearingParam paramRb) {
		this.ladarToRobot = ladarToRobot;
		try {
			new XStream().toXML(ladarToRobot,new FileOutputStream("sensorToRobot.xml"));
			new XStream().toXML(paramLrf,new FileOutputStream("sensorLRF.xml"));
			new XStream().toXML(paramRb,new FileOutputStream("sensorRB.xml"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
