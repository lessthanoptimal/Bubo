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

package bubo.desc.sensors.laser2d;

import bubo.desc.IntrinsicParameters;

/**
 * Description of basic parameters that describe a typical planar LADAR, such as a SICK or Hokuyo.
 *
 * @author Peter Abeles
 */
// TODO should units be specified?
public class PlanarLadarParam implements IntrinsicParameters {
	/**
	 * Type of scanner
	 */
	private String sensorType;
	/**
	 * The angle in radians that the scanner starts at
	 */
	private double startAngle;
	/**
	 * Number of radians and the direction between each consecutive scan line
	 */
	private double angleIncrement;
	/**
	 * How many scans/range measurements are there in a single sweep
	 */
	private int numberOfScans;
	/**
	 * How far can the LADAR see objects
	 */
	private double maxRange;
	/**
	 * The laser's aperture.  How "wide" the beam is.
	 */
	private double beamAperture;
	/**
	 * Accuracy of the range measurement
	 */
	private double rangeAccuracy;

	/**
	 * @param sensorType     Type of scanner
	 * @param startAngle     Angle in radians that the scan starts at
	 * @param angleIncrement Angle and direction between consecutive scans
	 * @param numberOfScans  Number of scans performed in a single sweep
	 * @param maxRange       The maximum range of the sensor.
	 * @param beamAperture   How wide the beam is.  Angular units.
	 */
	public PlanarLadarParam(String sensorType,
							double startAngle, double angleIncrement, int numberOfScans,
							double maxRange, double beamAperture,
							double rangeAccuracy) {
		this.sensorType = sensorType;
		this.startAngle = startAngle;
		this.angleIncrement = angleIncrement;
		this.numberOfScans = numberOfScans;
		this.maxRange = maxRange;
		this.beamAperture = beamAperture;
		this.rangeAccuracy = rangeAccuracy;
	}

	public PlanarLadarParam() {
	}

	/**
	 * Checks to see if the provided range measurement is a valid range.
	 *
	 * @param range range measurement
	 * @return true if valid.
	 */
	public boolean isValidRange(double range) {
		if (Double.isNaN(range))
			return false;

		if (range > maxRange)
			return false;

		return true;
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}

	public double getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(double startAngle) {
		this.startAngle = startAngle;
	}

	public double getAngleIncrement() {
		return angleIncrement;
	}

	public void setAngleIncrement(double angleIncrement) {
		this.angleIncrement = angleIncrement;
	}

	public int getNumberOfScans() {
		return numberOfScans;
	}

	public void setNumberOfScans(int numberOfScans) {
		this.numberOfScans = numberOfScans;
	}

	public double getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(double maxRange) {
		this.maxRange = maxRange;
	}

	public double getBeamAperture() {
		return beamAperture;
	}

	public void setBeamAperture(double beamAperture) {
		this.beamAperture = beamAperture;
	}

	public double getRangeAccuracy() {
		return rangeAccuracy;
	}

	public void setRangeAccuracy(double rangeAccuracy) {
		this.rangeAccuracy = rangeAccuracy;
	}
}
