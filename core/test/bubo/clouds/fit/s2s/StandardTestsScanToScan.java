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

package bubo.clouds.fit.s2s;

import bubo.clouds.fit.Lrf2dScanToScan;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.maps.d2.lines.LineSegmentMap;
import bubo.simulation.d2.sensors.SimulateLrf2D;
import georegression.misc.test.GeometryUnitTest;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.se.Se2_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Standard tests for Lrf2dScanToScan
 *
 * @author Peter Abeles
 */
public abstract class StandardTestsScanToScan {

	protected double angTol = -1;
	protected double tranTol = -1;
	Lrf2dParam param;
	private SimulateLrf2D model;
	private Se2_F64 sensorToWorld = new Se2_F64();
	private LineSegmentMap world;

	public abstract Lrf2dScanToScan createAlg();

	@Test
	public void standardTestsCCW() {
		param = new Lrf2dParam("Dummy", Math.PI / 2, -Math.PI, 100, 10, 0, 0);
		allTests();
	}

	@Test
	public void standardTestsCW() {
		param = new Lrf2dParam("Dummy", -Math.PI / 2, Math.PI, 100, 10, 0, 0);
		allTests();
	}

	private void allTests() {
		setupSimulation();
		checkPerfectNoHint();
		setupSimulation();
		checkPrefectHint();
		setupSimulation();
		setSecondToFirst();
	}

	public void setupSimulation() {
		world = new LineSegmentMap();
		// two perpendicular lines so that the position can be uniquely localized
		world.lines.add(new LineSegment2D_F64(-2, 4, 2, 0));
		world.lines.add(new LineSegment2D_F64(-2, -4, 2, 0));

		model = new SimulateLrf2D(param);

		if (angTol < 0 || tranTol < 0) {
			throw new RuntimeException("angTol and tranTol must be set");
		}

		sensorToWorld.reset();
	}

	/**
	 * Perfect observations in an easy scenario
	 */
	public void checkPerfectNoHint() {
		Lrf2dScanToScan alg = createAlg();
		alg.setSensorParam(param);

		// observe before any transform is applied
		model.update(new Se2_F64(),world);

		alg.setDestination(model.getMeasurement().meas.clone());

		// rotate and translate the sensor
		sensorToWorld.set(0.12, -0.12, 0.05);

		// make an observation in the new position
		model.update(sensorToWorld, world);

		alg.setSource(model.getMeasurement().meas.clone());

		// find the motion
		assertTrue(alg.process(null));

		// see if it is close enough to the expected value
		Se2_F64 found = alg.getSourceToDestination();
		Se2_F64 expected = sensorToWorld;

		GeometryUnitTest.assertEquals(expected.getTranslation(), found.getTranslation(), tranTol);
		assertEquals(expected.getYaw(), found.getYaw(), angTol);
		assertTrue(alg.getError() < 0.01);
	}

	/**
	 * Apply a large transform that should cause it to fail, check that it failed,
	 * provide a hint, and see if it works.
	 */
	public void checkPrefectHint() {
		Lrf2dScanToScan alg = createAlg();
		alg.setSensorParam(param);

		// observe before any transform is applied
		model.update(new Se2_F64(),world);

		alg.setDestination(model.getMeasurement().meas.clone());

		// rotate and translate the sensor, but make the magnitude so great it should
		//  not be able to recover the motion
		sensorToWorld.set(-0.5, -0.3, 0.6);

		// make an observation in the new position
		model.update(sensorToWorld, world);

		alg.setSource(model.getMeasurement().meas.clone());

		// It should not be able to find an answer
		if (alg.process(null)) {
			Se2_F64 found = alg.getSourceToDestination();
			Se2_F64 expected = sensorToWorld;
			GeometryUnitTest.assertNotEquals(expected, found, tranTol, angTol);
		}

		// now give it the hint
		assertTrue(alg.process(sensorToWorld));

		Se2_F64 found = alg.getSourceToDestination();
		Se2_F64 expected = sensorToWorld;
		GeometryUnitTest.assertEquals(expected, found, tranTol, angTol);
		assertTrue(alg.getError() < 0.01);
	}

	/**
	 * Sees if setSecondToFirst() works by only making calls to setSecondScan()
	 */
	public void setSecondToFirst() {
		Lrf2dScanToScan alg = createAlg();
		alg.setSensorParam(param);

		// observe before any transform is applied
		model.update(new Se2_F64(), world);

		alg.setSource(model.getMeasurement().meas.clone());
		// tell it to swap the first and second scan
		alg.assignSourceToDestination();

		// rotate and translate the sensor
		sensorToWorld.set(0.12, -0.12, 0.05);

		// make an observation in the new position
		model.update(sensorToWorld,world);

		alg.setSource(model.getMeasurement().meas.clone());

		// find the motion
		assertTrue(alg.process(null));

		// see if it is close enough to the expected value
		Se2_F64 found = alg.getSourceToDestination();
		Se2_F64 expected = sensorToWorld;

		GeometryUnitTest.assertEquals(expected.getTranslation(), found.getTranslation(), tranTol);
		assertEquals(expected.getYaw(), found.getYaw(), angTol);
		assertTrue(alg.getError() < 0.01);
	}
}
