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

package bubo.simulation.d2.features;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import georegression.struct.line.LineSegment2D_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestModelLrf2DBasic {

	/**
	 * Line is in front of the sensor
	 */
	@Test
	public void lineInFront() {
		LineSegmentWorld2D world = new LineSegmentWorld2D();
		world.lines.add(new LineSegment2D_F64(-1, 2, 1, 2));


		SimStateLrf2D state = new SimStateLrf2D(createParam());

		ModelLrf2DBasic model = new ModelLrf2DBasic();
		model.setWorld(world);

		// point the sensor upwards
		state.getLocalToParent().setYaw(Math.PI / 2.0);

		// observer the environment
		model.updateSensor(state);

		// the laser pointed straight up should see 2
		assertEquals(2, state.ranges[50], 1e-8);

		// ones off to the side should be max range
		assertEquals(10, state.ranges[0], 1e-8);
		assertEquals(10, state.ranges[99], 1e-8);
	}

	/**
	 * Line is behind the sensor and should not be seen
	 */
	@Test
	public void lineBehind() {
		LineSegmentWorld2D world = new LineSegmentWorld2D();
		world.lines.add(new LineSegment2D_F64(-1, -2, 1, -2));


		SimStateLrf2D state = new SimStateLrf2D(createParam());

		ModelLrf2DBasic model = new ModelLrf2DBasic();
		model.setWorld(world);

		// point the sensor upwards
		state.getLocalToParent().setYaw(Math.PI / 2.0);

		// observer the environment
		model.updateSensor(state);

		// the laser pointed straight up should see 2
		for (int i = 0; i < state.ranges.length; i++) {
			assertEquals(10, state.ranges[i], 1e-8);
		}
	}

	/**
	 * A line is blocking the view of another and should not be seen
	 */
	@Test
	public void lineBlockView() {
		LineSegmentWorld2D world = new LineSegmentWorld2D();
		world.lines.add(new LineSegment2D_F64(-1, 2, 1, 2));
		world.lines.add(new LineSegment2D_F64(-1, 2.5, 1, 2.5));


		SimStateLrf2D state = new SimStateLrf2D(createParam());

		ModelLrf2DBasic model = new ModelLrf2DBasic();
		model.setWorld(world);

		// point the sensor upwards
		state.getLocalToParent().setYaw(Math.PI / 2.0);

		// observer the environment
		model.updateSensor(state);

		// the laser pointed straight up should see 2
		assertEquals(2, state.ranges[50], 1e-8);

	}

	/**
	 * See if it is adding noise.
	 * <p/>
	 * TODO This test could be improved by testing the statistical properties of the noise
	 * run it twice, once perfect and once with noise
	 */
	@Test
	public void lineInFront_noise() {
		LineSegmentWorld2D world = new LineSegmentWorld2D();
		world.lines.add(new LineSegment2D_F64(-1, 2, 1, 2));


		SimStateLrf2D state = new SimStateLrf2D(createParam());

		ModelLrf2DBasic model = new ModelLrf2DBasic();
		model.setNoiseSigma(1);
		model.setWorld(world);

		// point the sensor upwards
		state.getLocalToParent().setYaw(Math.PI / 2.0);

		// observer the environment
		model.updateSensor(state);

		//  with noise its unlikely to be 2
		assertTrue(Math.abs(2 - state.ranges[50]) > 1e-8);
	}

	public Lrf2dParam createParam() {
		return new Lrf2dParam("Dummy", -Math.PI / 2, Math.PI, 100, 10, 0, 0);
	}

}
