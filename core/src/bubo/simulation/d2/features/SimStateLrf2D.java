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
import bubo.simulation.d2.SimulatedObjectState;

/**
 * State of a simulated 2D laser rangefinder (LRF).
 *
 * @author Peter Abeles
 */
public class SimStateLrf2D extends SimulatedObjectState {

	// description of the sensor
	Lrf2dParam sensorParam;

	// current observations
	double ranges[];

	public SimStateLrf2D(Lrf2dParam sensorParam) {
		this.sensorParam = sensorParam;

		ranges = new double[sensorParam.getNumberOfScans()];
	}

	public Lrf2dParam getSensorParam() {
		return sensorParam;
	}

	public double[] getRanges() {
		return ranges;
	}
}
