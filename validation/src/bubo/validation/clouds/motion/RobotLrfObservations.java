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

package bubo.validation.clouds.motion;

import georegression.struct.se.Se2_F64;

/**
 * Data structure for reading in 2D position data with range data from a LRF.
 *
 * @author Peter Abeles
 */
public class RobotLrfObservations {

	// when was the data collected
	private long timeStamp;

	// position of the robot when this observation was made
	private Se2_F64 scanToWorld;

	// list of range measurements
	private double[] range;

	public RobotLrfObservations( int numScans ) {
		range = new double[numScans];
	}

	public RobotLrfObservations() {
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Se2_F64 getScanToWorld() {
		return scanToWorld;
	}

	public void setScanToWorld(Se2_F64 scanToWorld) {
		this.scanToWorld = scanToWorld;
	}

	public double[] getRange() {
		return range;
	}

	public void setRange(double[] range) {
		this.range = range;
	}

	public void set( RobotLrfObservations a ) {
		timeStamp = a.timeStamp;
		scanToWorld.set(a.scanToWorld);
		System.arraycopy(a.range,0,range,0,a.range.length);
	}
}
