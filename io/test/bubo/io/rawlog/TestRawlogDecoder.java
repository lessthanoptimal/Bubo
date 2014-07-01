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
import bubo.io.rawlog.data.Trivialdata;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestRawlogDecoder {

	/**
	 * See if it can decode a very simple object multiple times
	 *
	 * @throws IOException
	 */
	@Test
	public void decodeSimpleObject() throws IOException {
		// manually encode a simple data structure
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		writeTrivialObject(out, 57);
		writeTrivialObject(out, 34);

		// now read it back in
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		RawlogDecoder decoder = new RawlogDecoder(in);

		Trivialdata found = decoder.decode();
		assertEquals(found.getData(), 57);

		found = decoder.decode();
		assertEquals(found.getData(), 34);
	}

	/**
	 * Writes a trivial object that just has an int to output using the rawlog format
	 */
	private void writeTrivialObject(ByteArrayOutputStream out, int value) throws IOException {
		// class name
		RawlogEncoder.writeClassName(out, Trivialdata.class.getSimpleName());
		// class version
		out.write(0);
		// data
		LittleEndianIO.writeInt(out, value);
		// end bit
		out.write(0x88);
	}

}
