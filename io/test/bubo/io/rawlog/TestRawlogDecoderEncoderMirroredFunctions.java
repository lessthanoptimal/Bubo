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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Test functions that have direct parallels between encoding and decoding to see if they can invert each other
 *
 * @author Peter Abeles
 */
public class TestRawlogDecoderEncoderMirroredFunctions {

	@Test
	public void dataString() throws IOException {
		String expected = "philospher";

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		RawlogEncoder.writeString(out, expected);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		RawlogDecoder decoder = new RawlogDecoder(in);

		String found = decoder.readString();

		assertTrue(found.compareTo(expected) == 0);
	}

	@Test
	public void dataClassName() throws IOException {
		String expected = "philospher";

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		RawlogEncoder.writeClassName(out, expected);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		RawlogDecoder decoder = new RawlogDecoder(in);

		int length = in.read() & 0x7F;
		String found = decoder.readClassName(length);

		assertTrue(found.compareTo(expected) == 0);
	}
}
