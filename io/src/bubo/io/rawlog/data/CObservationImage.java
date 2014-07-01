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
 * Rawlog class that stores images and information on the camera's extrinsic and intrinsic parameters.
 *
 * @author Peter Abeles
 */
public class CObservationImage extends CObservation implements RawlogSerializableCustom {

	/**
	 * The pose of the camera on the robot
	 */
	private CPose3D cameraPose;

	/**
	 * Intrinsic and distortion parameters of the camera.
	 * See the <a href="http://www.mrpt.org/Camera_Parameters">tutorial</a> for a discussion of these parameters.
	 */
	private TCamera cameraParams;

	private CImage image; //!< The image captured by the camera, that is, the main piece of information of this observation.

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {

		try {
			cameraPose = (CPose3D) decoder.decodeObject();
			if (version >= 4) {
				cameraParams = (TCamera) decoder.decodeObject();
			} else {
				CMatrix distortion = (CMatrix) decoder.decodeObject();
				CMatrix intrinsic = (CMatrix) decoder.decodeObject();

				cameraParams = new TCamera();
				cameraParams.setDistortion(distortion);
				cameraParams.setIntrinsic(intrinsic);
			}
			image = (CImage) decoder.decodeObject();
			if (version >= 1)
				setTimestamp(LittleEndianIO.readLong(decoder.getInput()));

			if (version >= 2) {
				if (version < 4) {
					cameraParams.setFocalLengthMeters(LittleEndianIO.readDouble(decoder.getInput()));
				}
			} else {
				cameraParams.setFocalLengthMeters(0.002);
			}

			if (version >= 3)
				setSensorLabel(decoder.readString());
			else
				setSensorLabel("");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public CPose3D getCameraPose() {
		return cameraPose;
	}

	public void setCameraPose(CPose3D cameraPose) {
		this.cameraPose = cameraPose;
	}

	public TCamera getCameraParams() {
		return cameraParams;
	}

	public void setCameraParams(TCamera cameraParams) {
		this.cameraParams = cameraParams;
	}

	public CImage getImage() {
		return image;
	}

	public void setImage(CImage image) {
		this.image = image;
	}

	@Override
	public int getVersion() {
		return 4;
	}
}
