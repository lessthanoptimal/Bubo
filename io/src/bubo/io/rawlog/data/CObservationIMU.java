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

package bubo.io.rawlog.data;

import bubo.io.rawlog.RawlogSerializableStandard;

/**
 * Rawlog format for storing IMU information.
 *
 * @author Peter Abeles
 */
public class CObservationIMU extends CObservation implements RawlogSerializableStandard {
	/**
	 * The pose of the sensor on the robot.
	 */
	private CPose3D sensorPose;

	/**
	 * Each of the 15 entries of this vector is true if the corresponding data index contains valid data (the IMU unit supplies that kind of data).
	 * See the top of this page for the meaning of the indices.
	 */
	private boolean[] dataIsPresent;

	/**
	 * The accelerometer and/or gyroscope measurements taken by the IMU at the given timestamp.
	 * \sa dataIsPresent, CObservation::timestamp
	 */
	private double[] rawMeasurements;

	@Override
	public String[] getVariableOrder(int version) {
		return new String[]{"sensorPose", "dataIsPresent", "timestamp", "rawMeasurements", "sensorLabel"};
	}

	public CPose3D getSensorPose() {
		return sensorPose;
	}

	public void setSensorPose(CPose3D sensorPose) {
		this.sensorPose = sensorPose;
	}

	public boolean[] getDataIsPresent() {
		return dataIsPresent;
	}

	public void setDataIsPresent(boolean[] dataIsPresent) {
		this.dataIsPresent = dataIsPresent;
	}

	public double[] getRawMeasurements() {
		return rawMeasurements;
	}

	public void setRawMeasurements(double[] rawMeasurements) {
		this.rawMeasurements = rawMeasurements;
	}

	@Override
	public int getVersion() {
		return 1;
	}
}
