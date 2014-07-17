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

package bubo.desc.sensors.lrf3d;

import bubo.desc.sensors.lrf2d.Lrf2dMeasurement;
import georegression.struct.se.Se3_F64;

/**
 * <p>
 * Observation from a 2D LRF while is being rotated.  The sensor's motion is specified through two transforms,
 * which represent the sensor's location at the beginning and end of the scan. The transformations specify
 * the location in reference to base.  Base is the location of the sensor at some canonical pose.
 * </p>
 *
 * <p>
 * To find the end point of a scan in the base's reference frame:<br>
 * 1) Set the coordinate of an individual measurement to be [0,0,r], assuming the +z axis is point out of the
 * sensor.<br>
 * 2) Apply the interpolated transform for that particular measurement to that point.
 * </p>
 *
 * @author Peter Abeles
 */
public class SpinningLrf2dMeasurement extends Lrf2dMeasurement {
	/**
	 * Location of the sensor at the start of the scan
	 */
	public Se3_F64 startToBase = new Se3_F64();
	/**
	 * Location of the sensor at the end of the scan
	 */
	public Se3_F64 endToBase = new Se3_F64();

	public SpinningLrf2dMeasurement(int numMeas) {
		super(numMeas);
	}

	public SpinningLrf2dMeasurement() {
	}

	public void interpolate( int measIndex , Se3_F64 currToBase ) {
		// todo put code for interpolating between two Se3's into georegression?
	}
}
