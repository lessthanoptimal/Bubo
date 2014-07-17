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

package bubo.simulation.d2.sensors;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.desc.sensors.lrf3d.SpinningLrf2dMeasurement;
import bubo.maps.d3.triangles.Triangle3dMap;
import georegression.struct.se.Se3_F64;

/**
 * Simulates LRF scans in a simulated triangle world
 *
 * @author Peter Abeles
 */
public class SimulateSpinningLrf2D {
	Lrf2dParam param;
	SpinningLrf2dMeasurement measurement;

	public SimulateSpinningLrf2D(Lrf2dParam param) {
		this.param = param;
		measurement = new SpinningLrf2dMeasurement(param.getNumberOfScans());
	}

	public Lrf2dParam getParam() {
		return param;
	}

	public SpinningLrf2dMeasurement getMeasurement() {
		return measurement;
	}

	/**
	 * Given the world model, computes the range measurements
	 *
	 * @param sensorToWorld transform from sensor's base to world frame.  Sensor's base is assumed to be static in
	 *                      reference to the world during the scan.
	 * @param world Map of the world
	 */
	public void update( Se3_F64 sensorToWorld , Triangle3dMap world ) {

	}
}
