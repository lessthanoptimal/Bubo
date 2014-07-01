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
import georegression.struct.point.Point3D_F64;
import georegression.struct.so.Quaternion_F64;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Observation class for either a pair of left+right or left+disparity images from a stereo camera.
 *
 * @author Peter Abeles
 */
public class CObservationStereoImages extends CObservation implements RawlogSerializableCustom {

	/**
	 * Image from the left camera (this image will be ALWAYS present)
	 */
	CImage imageLeft;

	/**
	 * Image from the right camera, only contains a valid image if hasImageRight == true.
	 */
	CImage imageRight;

	/**
	 * Disparity image, only contains a valid image if hasImageDisparity == true.
	 * The relation between the actual disparity and pixels and each value in this image is... ???????????
	 */
	CImage imageDisparity;

	/**
	 * Parameters for the left/right cameras: individual intrinsic and distortion parameters of the cameras.
	 * See the <a href="http://www.mrpt.org/Camera_Parameters">tutorial</a> for a discussion of these parameters.
	 */
	TCamera leftCamera, rightCamera;

	CPose3DQuat cameraPose;        //!< The pose of the LEFT camera, relative to the robot.

	/**
	 * The pose of the right camera, relative to the left one:
	 * Note that using the conventional reference coordinates for the left
	 * camera (x points to the right, y down), the "right" camera is situated
	 * at position (BL, 0, 0) with yaw=pitch=roll=0, where BL is the BASELINE.
	 */
	CPose3DQuat rightCameraPose;

	long timeStamp;

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		if (version >= 6)
			throw new RuntimeException("Version not supported: " + version);

		try {
			if (version < 5) {
				CPose3D tmp = (CPose3D) decoder.decodeObject();
				cameraPose = new CPose3DQuat(tmp);
			}

			if (version >= 5) {
				cameraPose = (CPose3DQuat) decoder.decodeObject();
				leftCamera = (TCamera) decoder.decodeObject();
				rightCamera = (TCamera) decoder.decodeObject();
			} else {
				leftCamera = new TCamera();
				rightCamera = new TCamera();
				CMatrix intrinsicParam = (CMatrix) decoder.decodeObject();
				leftCamera.setIntrinsicParams(new CMatrixD(intrinsicParam));
				rightCamera.setIntrinsicParams(new CMatrixD(intrinsicParam));
			}

			imageLeft = (CImage) decoder.decodeObject();
			imageRight = (CImage) decoder.decodeObject();

			if (version >= 1) {
				timeStamp = LittleEndianIO.readLong(decoder.getInput());
			} else {
				timeStamp = Long.MAX_VALUE;
			}

			if (version >= 2) {
				if (version < 5) {
					CPose3D tmp = (CPose3D) decoder.decodeObject();
					rightCameraPose = new CPose3DQuat(tmp);
				} else {
					rightCameraPose = (CPose3DQuat) decoder.decodeObject();
				}
			} else {
				rightCameraPose = new CPose3DQuat(new Point3D_F64(0.1, 0, 0), new Quaternion_F64());
			}

			if (version >= 3 && version < 5) {
				double fc = LittleEndianIO.readDouble(decoder.getInput());
				leftCamera.setFocalLengthMeters(fc);
				rightCamera.setFocalLengthMeters(fc);
			} else if (version < 3) {
				// For version 0, 1 & 2 (from version 5, this parameter is included in the TCamera objects)
				leftCamera.setFocalLengthMeters(0.002);
				rightCamera.setFocalLengthMeters(0.002);
			}

			if (version >= 4) {
				setSensorLabel(decoder.readString());
			} else {
				setSensorLabel(null);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public CImage getImageLeft() {
		return imageLeft;
	}

	public void setImageLeft(CImage imageLeft) {
		this.imageLeft = imageLeft;
	}

	public CImage getImageRight() {
		return imageRight;
	}

	public void setImageRight(CImage imageRight) {
		this.imageRight = imageRight;
	}

	public TCamera getLeftCamera() {
		return leftCamera;
	}

	public void setLeftCamera(TCamera leftCamera) {
		this.leftCamera = leftCamera;
	}

	public TCamera getRightCamera() {
		return rightCamera;
	}

	public void setRightCamera(TCamera rightCamera) {
		this.rightCamera = rightCamera;
	}

	public CPose3DQuat getCameraPose() {
		return cameraPose;
	}

	public void setCameraPose(CPose3DQuat cameraPose) {
		this.cameraPose = cameraPose;
	}

	public CPose3DQuat getRightCameraPose() {
		return rightCameraPose;
	}

	public void setRightCameraPose(CPose3DQuat rightCameraPose) {
		this.rightCameraPose = rightCameraPose;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public CImage getImageDisparity() {
		return imageDisparity;
	}

	public void setImageDisparity(CImage imageDisparity) {
		this.imageDisparity = imageDisparity;
	}

	@Override
	public void customEncoding(OutputStream output) {
		throw new RuntimeException("Encoding not supported yet");
	}

	@Override
	public int getVersion() {
		return 6;
	}
}
