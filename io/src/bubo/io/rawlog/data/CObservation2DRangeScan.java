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

import bubo.io.LittleEndianIO;
import bubo.io.rawlog.RawlogDecoder;
import bubo.io.rawlog.RawlogSerializableCustom;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Rawlog class that stores scans from a 2D laser range finder type sensor.
 *
 * @author Peter Abeles
 */
public class CObservation2DRangeScan extends CObservation implements RawlogSerializableCustom {

	// The range values of the scan, in meters.
	private float[] scan;

	// It's false (=0) on no reflected rays, referenced to elements in "scan"
	private byte[] validRange;

	// the aperture of the device in radians
	private float aperture;

	// The scanning direction
	private boolean rightToLeft;

	// The maximum range allowed by the device, in meters (e.g. 80m, 50m,...)
	private float maxRange;

	/**
	 * The 6D pose of the sensor on the robot.
	 */
	private CPose3D sensorPose;

	/**
	 * The "sigma" error of the device in meters, used while inserting the scan in an occupancy grid.
	 */
	private float stdError;

	/**
	 * The aperture of each beam, in radians, used to insert "thick" rays in the occupancy grid.
	 * (Added in the streamming version #4 of the class)
	 */
	private float beamAperture;

	/**
	 * If the laser gathers data by sweeping in the pitch/elevation angle, this holds the increment in "pitch"
	 * (=-"elevation") between the beginning and the end of the scan (the sensorPose member stands for
	 * the pose at the beginning of the scan).
	 */
	private double deltaPitch;

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		if (version < 4 || version > 6)
			throw new RuntimeException("Version not supported: " + version);

		try {
			aperture = LittleEndianIO.readFloat(decoder.getInput());
			rightToLeft = LittleEndianIO.readBoolean(decoder.getInput());
			maxRange = LittleEndianIO.readFloat(decoder.getInput());
			sensorPose = (CPose3D) decoder.decodeObject();

			if (version < 6) {
				CMatrix cov = (CMatrix) decoder.decodeObject();
			}

			int N = LittleEndianIO.readInt(decoder.getInput());
			if (N > 0) {
				scan = decoder.readFloatArray(N);
				validRange = decoder.readByteArray(N);
			}
			stdError = LittleEndianIO.readFloat(decoder.getInput());
			setTimestamp(LittleEndianIO.readLong(decoder.getInput()));
			beamAperture = LittleEndianIO.readFloat(decoder.getInput());
			if (version >= 5) {
				setSensorLabel(decoder.readString());
				deltaPitch = LittleEndianIO.readDouble(decoder.getInput());
			} else {
				setSensorLabel(null);
				deltaPitch = 0;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public float[] getScan() {
		return scan;
	}

	public void setScan(float[] scan) {
		this.scan = scan;
	}

	public byte[] getValidRange() {
		return validRange;
	}

	public void setValidRange(byte[] validRange) {
		this.validRange = validRange;
	}

	public float getAperture() {
		return aperture;
	}

	public void setAperture(float aperture) {
		this.aperture = aperture;
	}

	public boolean isRightToLeft() {
		return rightToLeft;
	}

	public void setRightToLeft(boolean rightToLeft) {
		this.rightToLeft = rightToLeft;
	}

	public float getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(float maxRange) {
		this.maxRange = maxRange;
	}

	public CPose3D getSensorPose() {
		return sensorPose;
	}

	public void setSensorPose(CPose3D sensorPose) {
		this.sensorPose = sensorPose;
	}

	public float getStdError() {
		return stdError;
	}

	public void setStdError(float stdError) {
		this.stdError = stdError;
	}

	public float getBeamAperture() {
		return beamAperture;
	}

	public void setBeamAperture(float beamAperture) {
		this.beamAperture = beamAperture;
	}

	public double getDeltaPitch() {
		return deltaPitch;
	}

	public void setDeltaPitch(double deltaPitch) {
		this.deltaPitch = deltaPitch;
	}

	public int getVersion() {
		return 6;
	}
}
