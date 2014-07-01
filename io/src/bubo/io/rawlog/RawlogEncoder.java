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

package bubo.io.rawlog;

import bubo.io.LittleEndianIO;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Saves rawlog (http://www.mrpt.org/) data structures in the rawlog binary format.
 *
 * @author Peter Abeles
 */
public class RawlogEncoder {

	DataOutputStream out;

	public RawlogEncoder(OutputStream out, boolean gzip) {
		this.out = new DataOutputStream(out);
	}

	public static void writeClassName(OutputStream out, String string) throws IOException {
		int length = string.length();
		out.write(length | 0x80);

		byte[] data = string.getBytes();
		for (int i = 0; i < length; i++) {
			out.write(data[i]);
		}
	}

	public static void writeString(OutputStream out, String string) throws IOException {

		int length = string.length();
		LittleEndianIO.writeInt(out, length);

		byte[] data = string.getBytes();
		for (int i = 0; i < length; i++) {
			out.write(data[i]);
		}
	}

	public void encode(RawlogSerializable data) throws IOException {
		if (data instanceof RawlogSerializableStandard) {
			encodeStandard((RawlogSerializableStandard) data);
		} else {

		}
	}

	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void encodeStandard(RawlogSerializableStandard data) throws IOException {
		// class name
		RawlogEncoder.writeClassName(out, data.getClass().getSimpleName());
		// class version
		out.write(data.getVersion());

		// output each data with reflections
		String vars[] = data.getVariableOrder(data.getVersion());
		for (int i = 0; i < vars.length; i++) {

		}

		// end byte
		out.write(0x88);

	}
}
