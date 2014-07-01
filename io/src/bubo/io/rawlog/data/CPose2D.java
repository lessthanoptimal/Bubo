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
import georegression.struct.point.Point2D_F64;

import java.io.IOException;
import java.io.OutputStream;


/**
 * @author Peter Abeles
 */
public class CPose2D extends CPose implements RawlogSerializableCustom {
	public Point2D_F64 pose = new Point2D_F64();
	public double yaw;

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		try {
			if (version == 0) {
				pose.x = LittleEndianIO.readFloat(decoder.getInput());
				pose.y = LittleEndianIO.readFloat(decoder.getInput());
				yaw = LittleEndianIO.readFloat(decoder.getInput());
			} else if (version == 1) {
				pose.x = LittleEndianIO.readDouble(decoder.getInput());
				pose.y = LittleEndianIO.readDouble(decoder.getInput());
				yaw = LittleEndianIO.readDouble(decoder.getInput());
			} else {
				throw new RuntimeException("Unknown version: " + version);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getVersion() {
		return 1;
	}

	public Point2D_F64 getPose() {
		return pose;
	}

	public void setPose(Point2D_F64 pose) {
		this.pose = pose;
	}

	public double getYaw() {
		return yaw;
	}

	public void setYaw(double yaw) {
		this.yaw = yaw;
	}
}
