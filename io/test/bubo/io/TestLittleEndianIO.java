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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLittleEndianIO {
	@Test
	public void readBoolean() throws IOException {
		byte[] data = new byte[2];
		data[0] = 1;
		data[1] = 0;
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		assertTrue(LittleEndianIO.readBoolean(in));
		assertFalse(LittleEndianIO.readBoolean(in));
	}

	@Test
	public void readShort() throws IOException {

		short value = -1235;

		byte[] data = new byte[2];
		data[0] = (byte) (0xFF & value);
		data[1] = (byte) (0xFF & (value >> 8));
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		assertTrue(LittleEndianIO.readShort(in) == value);
	}

	@Test
	public void writeShort() throws IOException {
		short value = -1235;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		LittleEndianIO.writeShort(out, value);

		byte data[] = out.toByteArray();

		int found = data[0] & 0xFF;
		found |= (data[1] & 0xFF) << 8;

		assertTrue((short) found == value);
	}

	@Test
	public void readInt() throws IOException {
		int value = -14552345;

		byte[] data = new byte[8];
		data[0] = (byte) (0xFF & value);
		data[1] = (byte) (0xFF & (value >> 8));
		data[2] = (byte) (0xFF & (value >> 16));
		data[3] = (byte) (0xFF & (value >> 24));
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		assertTrue(LittleEndianIO.readInt(in) == value);
	}

	@Test
	public void writeInt() throws IOException {
		int value = -14552345;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		LittleEndianIO.writeInt(out, value);

		byte data[] = out.toByteArray();

		int found = data[0] & 0xFF;
		found |= (data[1] & 0xFF) << 8;
		found |= (data[2] & 0xFF) << 16;
		found |= (data[3] & 0xFF) << 24;

		assertTrue(found == value);
	}

	@Test
	public void readLong() throws IOException {
		long value = -14552778345L;

		byte[] data = new byte[8];
		data[0] = (byte) (0xFF & value);
		data[1] = (byte) (0xFF & (value >> 8));
		data[2] = (byte) (0xFF & (value >> 16));
		data[3] = (byte) (0xFF & (value >> 24));
		data[4] = (byte) (0xFF & (value >> 32));
		data[5] = (byte) (0xFF & (value >> 40));
		data[6] = (byte) (0xFF & (value >> 48));
		data[7] = (byte) (0xFF & (value >> 56));
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		assertTrue(LittleEndianIO.readLong(in) == value);
	}

	@Test
	public void writeLong() throws IOException {
		long value = -14552778345L;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		LittleEndianIO.writeLong(out, value);

		byte data[] = out.toByteArray();

		long found = data[0] & 0xFF;
		found |= ((long) (data[1] & 0xFF) << 8);
		found |= ((long) (data[2] & 0xFF) << 16);
		found |= ((long) (data[3] & 0xFF) << 24);
		found |= ((long) (data[4] & 0xFF) << 32);
		found |= ((long) (data[5] & 0xFF) << 40);
		found |= ((long) (data[6] & 0xFF) << 48);
		found |= ((long) (data[7] & 0xFF) << 56);

		assertTrue(found == value);
	}

	@Test
	public void readFloat() throws IOException {
		float value = 234.423f;

		int valueInt = Float.floatToRawIntBits(value);

		byte[] data = new byte[8];
		data[0] = (byte) (0xFF & valueInt);
		data[1] = (byte) (0xFF & (valueInt >> 8));
		data[2] = (byte) (0xFF & (valueInt >> 16));
		data[3] = (byte) (0xFF & (valueInt >> 24));
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		assertTrue(LittleEndianIO.readFloat(in) == value);
	}

	@Test
	public void writeFloat() throws IOException {
		float value = 234.423f;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		LittleEndianIO.writeFloat(out, value);

		byte data[] = out.toByteArray();

		int found = data[0] & 0xFF;
		found |= (data[1] & 0xFF) << 8;
		found |= (data[2] & 0xFF) << 16;
		found |= (data[3] & 0xFF) << 24;

		float foundFloat = Float.intBitsToFloat(found);
		assertTrue(value == foundFloat);
	}

	@Test
	public void readDouble() throws IOException {
		double value = 23424.59384877;
		long valueInt = Double.doubleToRawLongBits(value);

		byte[] data = new byte[8];
		data[0] = (byte) (0xFF & valueInt);
		data[1] = (byte) (0xFF & (valueInt >> 8));
		data[2] = (byte) (0xFF & (valueInt >> 16));
		data[3] = (byte) (0xFF & (valueInt >> 24));
		data[4] = (byte) (0xFF & (valueInt >> 32));
		data[5] = (byte) (0xFF & (valueInt >> 40));
		data[6] = (byte) (0xFF & (valueInt >> 48));
		data[7] = (byte) (0xFF & (valueInt >> 56));
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		assertTrue(LittleEndianIO.readDouble(in) == value);
	}

	@Test
	public void writeDouble() throws IOException {
		double value = 23424.59384877;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		LittleEndianIO.writeDouble(out, value);

		byte data[] = out.toByteArray();

		long found = data[0] & 0xFF;
		found |= ((long) (data[1] & 0xFF) << 8);
		found |= ((long) (data[2] & 0xFF) << 16);
		found |= ((long) (data[3] & 0xFF) << 24);
		found |= ((long) (data[4] & 0xFF) << 32);
		found |= ((long) (data[5] & 0xFF) << 40);
		found |= ((long) (data[6] & 0xFF) << 48);
		found |= ((long) (data[7] & 0xFF) << 56);

		double foundFloat = Double.longBitsToDouble(found);
		assertTrue(value == foundFloat);
	}


}
