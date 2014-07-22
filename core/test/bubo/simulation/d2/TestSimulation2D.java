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

import bubo.desc.sensors.landmark.RangeBearingMeasurement;
import bubo.desc.sensors.landmark.RangeBearingParam;
import bubo.desc.sensors.lrf2d.Lrf2dMeasurement;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.maps.d2.lines.LineSegmentMap;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.se.Se2_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestSimulation2D {

	Lrf2dParam param = new Lrf2dParam(null,Math.PI/2.0,-Math.PI,180,10,0,0);

	@Test
	public void doStep() {
		User user = new User();
		LineSegmentMap world = new LineSegmentMap();
		world.lines.add(new LineSegment2D_F64(2, -1, 2, 1));

		Simulation2D alg = new Simulation2D(user,new CircularRobot2D(0.5));
		alg.setWalls(world);
		alg.setLaserRangeFinder(param);
		alg.setPeriods(0.01,0.1,0.2,0.3);
		for( int i = 0; i < 200; i++ ) {
			alg.doStep();
		}

		assertEquals(19,user.countControl);
		assertEquals(10,user.countOdometry);
		assertEquals(6,user.countLadar);
	}

	@Test
	public void moveRobot() {
		LineSegmentMap world = new LineSegmentMap();
		world.lines.add(new LineSegment2D_F64(2, -1, 2, 1));

		Simulation2D alg = new Simulation2D(null,new CircularRobot2D(0.5));
		alg.setWalls(world);
		alg.setLaserRangeFinder(param);
		alg.setLocation(1.75, 0, 0);
		alg.sendControl(0,Math.PI/2);
		alg.moveRobot(1);
		alg.sendControl(1,0);
		alg.moveRobot(1);

		Se2_F64 found = alg._truthRobotToWorld();

		assertEquals(1.75,found.getX(),1e-8);
		assertEquals(1,found.getY(),1e-8);
		assertEquals(Math.PI/2,found.getYaw(),1e-8);
	}

	@Test
	public void handleCollisions() {
		LineSegmentMap world = new LineSegmentMap();
		world.lines.add(new LineSegment2D_F64(2, -1, 2, 1));

		Simulation2D alg = new Simulation2D(null,new CircularRobot2D(0.5));
		alg.setWalls(world);
		alg.setLaserRangeFinder(param);
		alg.setLocation(1.75,0,0);

		alg.handleCollisions();

		Se2_F64 found = alg._truthRobotToWorld();
		assertEquals(1.5,found.getX(),0.01);
		assertEquals(0,found.getY(),1e-8);
		assertEquals(0,found.getYaw(),1e-8);
	}

	@Test
	public void rangeBearingSensor() {
		fail("Implement");
	}

	private static class User implements RobotInterface2D {

		int countControl, countOdometry, countLadar;

		@Override
		public void doControl(long timeStamp) {
			this.countControl++;
		}

		@Override
		public void setControlListener(ControlListener2D listener) {}

		@Override
		public void setIntrinsic(Se2_F64 ladarToRobot, Lrf2dParam paramLrf, RangeBearingParam paramRb) {}

		@Override
		public void odometry(long timeStamp, Se2_F64 robotToWorld) {
			countOdometry++;
		}

		@Override
		public void ladar(long timeStamp, Lrf2dMeasurement measurement) {
			countLadar++;
		}

		@Override
		public void rangeBearing(long timeStamp, RangeBearingMeasurement measurement) {

		}

		@Override
		public boolean isDone() {return false;}
	}
}