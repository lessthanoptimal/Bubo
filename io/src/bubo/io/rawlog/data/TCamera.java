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
 * Rawlog class for camera intrinsic parameters.
 *
 * @author Peter Abeles
 */
public class TCamera implements RawlogSerializableCustom {

	// Number of pixels along the images width and height
	private int width, height;
	//!< 3 by 3 Matrix of intrinsic parameters (containing the focal length and principal point coordinates)
	private CMatrixD intrinsicParams;
	//!< [k1 k2 t1 t2 t3] -> k_i: parameters of radial distortion, t_i: parameters of tangential distortion (default=0)
	private double[] distortion = new double[5];
	//!< The focal length of the camera, in meters (can be used among 'intrinsicParams' to determine the pixel size).
	private double focalLengthMeters;

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		try {
			focalLengthMeters = LittleEndianIO.readDouble(decoder.getInput());
			for (int i = 0; i < 5; i++)
				distortion[i] = LittleEndianIO.readDouble(decoder.getInput());
			intrinsicParams = (CMatrixD) decoder.decodeObject();
			focalLengthMeters = LittleEndianIO.readDouble(decoder.getInput());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void setDistortion(CMatrix dist) {
		for (int i = 0; i < dist.getNumRows(); i++) {
			distortion[i] = dist.get(i, 0);
		}
	}

	public void setIntrinsic(CMatrix a) {
		intrinsicParams = new CMatrixD(a);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public CMatrixD getIntrinsicParams() {
		return intrinsicParams;
	}

	public void setIntrinsicParams(CMatrixD intrinsicParams) {
		this.intrinsicParams = intrinsicParams;
	}

	public double[] getDistortion() {
		return distortion;
	}

	public void setDistortion(double[] distortion) {
		this.distortion = distortion;
	}

	public double getFocalLengthMeters() {
		return focalLengthMeters;
	}

	public void setFocalLengthMeters(double focalLengthMeters) {
		this.focalLengthMeters = focalLengthMeters;
	}

	@Override
	public int getVersion() {
		return 2;
	}

	public String toReadableText() {
		String ret = "";

		ret += "Dimension " + width + " by " + height + "\n";
		ret += "Focal Length " + focalLengthMeters + " (m)\n";
		ret += "Intrinsic matrix:  implement\n";
		ret += "Distortion param: ";
		for (int i = 0; i < distortion.length; i++) {
			ret += distortion[i] + " ";
		}
		ret += "\n";

		return ret;
	}
}
