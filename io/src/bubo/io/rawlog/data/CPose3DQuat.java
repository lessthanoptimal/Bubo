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
import georegression.geometry.ConvertRotation3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.so.Quaternion_F64;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Stores 3D rigid body 3D as a translation and quaternion.
 *
 * @author Peter Abeles
 */
public class CPose3DQuat extends CPose implements RawlogSerializableCustom {
	Point3D_F64 position = new Point3D_F64();
	Quaternion_F64 rotation = new Quaternion_F64();

	public CPose3DQuat() {
	}

	public CPose3DQuat(CPose3D orig) {
		position.set(orig.getPosition());
		ConvertRotation3D_F64.matrixToQuaternion(orig.getRotation(), rotation);
	}

	public CPose3DQuat(Point3D_F64 position, Quaternion_F64 rotation) {
		this.position.set(position);
		this.rotation.set(rotation);
	}

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		if (version != 0)
			throw new RuntimeException("Unsupported version: " + version);

		try {
			position.x = LittleEndianIO.readDouble(decoder.getInput());
			position.y = LittleEndianIO.readDouble(decoder.getInput());
			position.z = LittleEndianIO.readDouble(decoder.getInput());
			rotation.x = LittleEndianIO.readDouble(decoder.getInput());
			rotation.y = LittleEndianIO.readDouble(decoder.getInput());
			rotation.z = LittleEndianIO.readDouble(decoder.getInput());
			rotation.w = LittleEndianIO.readDouble(decoder.getInput());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		throw new RuntimeException("Not supported yet");
	}

	@Override
	public int getVersion() {
		return 0;
	}

	public Point3D_F64 getPosition() {
		return position;
	}

	public void setPosition(Point3D_F64 position) {
		this.position.set(position);
	}

	public Quaternion_F64 getRotation() {
		return rotation;
	}

	public void setRotation(Quaternion_F64 rotation) {
		this.rotation.set(rotation);
	}
}
