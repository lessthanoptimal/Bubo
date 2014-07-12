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
	PrintStream out;

	public FollowPathLoggingRobot(double velocity, double angularVelocity, List<Point2D_F64> wayPoints) {
		super(velocity, angularVelocity, wayPoints);

		try {
			out = new PrintStream("observations.txt");
			out.println("# time-stamp x y yaw [ranges ... N]");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void ladar(long timeStamp, Lrf2dMeasurement measurement) {
		Se2_F64 robotToWorld =  listener._truthRobotToWorld();

		ladarToRobot.concat(robotToWorld,ladarToWorld);

		out.printf("%d %f %f %f",timeStamp,ladarToWorld.getX(),ladarToWorld.getY(),ladarToWorld.getYaw());
		for (int i = 0; i < measurement.numMeas; i++) {
			out.printf(" %f",measurement.meas[i]);
		}
		out.println();
		out.flush();
	}

	@Override
	public void setIntrinsic(Se2_F64 ladarToRobot, Lrf2dParam param) {
		this.ladarToRobot = ladarToRobot;
		try {
			new XStream().toXML(param,new FileOutputStream("lrf.xml"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
