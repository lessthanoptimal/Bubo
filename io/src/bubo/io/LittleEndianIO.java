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

package bubo.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Java uses big endian byte order.  To output little endian the byte order needs to be changed.  This class
 * contains functions for writing and reading little endian primitive data.
 *
 * @author Peter Abeles
 */
public class LittleEndianIO {

	public static boolean readBoolean(InputStream in) throws IOException {
		return readByte(in) != 0;
	}

	public static short readShort(InputStream in) throws IOException {
		short val = 0;

		val |= readByte(in);
		val |= readByte(in) << 8;

		return val;
	}

	public static void writeShort(OutputStream out, short val) throws IOException {
		out.write(val & 0xff);
		out.write((val >> 8) & 0xff);
	}

	public static int readInt(InputStream in) throws IOException {
		int val = 0;

		val |= readByte(in);
		val |= readByte(in) << 8;
		val |= readByte(in) << 16;
		val |= readByte(in) << 24;

		return val;
	}

	public static int readInt(byte[] data, int offset) throws IOException {
		int val = 0;

		val |= (data[offset++] & 0xFF);
		val |= (data[offset++] & 0xFF) << 8;
		val |= (data[offset++] & 0xFF) << 16;
		val |= (data[offset] & 0xFF) << 24;

		return val;
	}

	public static void writeInt(OutputStream out, int val) throws IOException {
		out.write(val & 0xff);
		out.write((val >> 8) & 0xff);
		out.write((val >> 16) & 0xff);
		out.write((val >> 24) & 0xff);
	}

	public static float readFloat(InputStream in) throws IOException {
		int rawValue = readInt(in);

		return Float.intBitsToFloat(rawValue);
	}

	public static float readFloat(byte[] data, int offset) throws IOException {
		int rawValue = readInt(data, offset);

		return Float.intBitsToFloat(rawValue);
	}

	public static void writeFloat(OutputStream out, float val) throws IOException {
		int valInt = Float.floatToRawIntBits(val);
		writeInt(out, valInt);
	}

	public static double readDouble(InputStream in) throws IOException {
		long rawValue = readLong(in);

		return Double.longBitsToDouble(rawValue);
	}

	public static double readDouble(byte[] data, int offset) throws IOException {
		long rawValue = readLong(data, offset);

		return Double.longBitsToDouble(rawValue);
	}

	public static void writeDouble(OutputStream out, double val) throws IOException {
		long valInt = Double.doubleToRawLongBits(val);
		writeLong(out, valInt);
	}

	public static long readLong(InputStream in) throws IOException {
		long val = 0;

		val |= (long) readByte(in);
		val |= (long) readByte(in) << 8;
		val |= (long) readByte(in) << 16;
		val |= (long) readByte(in) << 24;
		val |= (long) readByte(in) << 32;
		val |= (long) readByte(in) << 40;
		val |= (long) readByte(in) << 48;
		val |= (long) readByte(in) << 56;

		return val;
	}

	public static long readLong(byte[] data, int offset) throws IOException {
		long val = 0;

		val |= (long) (data[offset++] & 0xFF);
		val |= (long) (data[offset++] & 0xFF) << 8;
		val |= (long) (data[offset++] & 0xFF) << 16;
		val |= (long) (data[offset++] & 0xFF) << 24;
		val |= (long) (data[offset++] & 0xFF) << 32;
		val |= (long) (data[offset++] & 0xFF) << 40;
		val |= (long) (data[offset++] & 0xFF) << 48;
		val |= (long) (data[offset] & 0xFF) << 56;

		return val;
	}

	public static void writeLong(OutputStream out, long val) throws IOException {
		out.write((int) (val & 0xff));
		out.write((int) ((val >> 8) & 0xff));
		out.write((int) ((val >> 16) & 0xff));
		out.write((int) ((val >> 24) & 0xff));
		out.write((int) ((val >> 32) & 0xff));
		out.write((int) ((val >> 40) & 0xff));
		out.write((int) ((val >> 48) & 0xff));
		out.write((int) ((val >> 56) & 0xff));
	}

	public static int readByte(InputStream in) throws IOException {
		int val = in.read();

		if (val < 0)
			throw new EOFException();

		return val;
	}
}
