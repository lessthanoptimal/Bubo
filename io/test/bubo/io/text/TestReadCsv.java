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

package bubo.io.text;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestReadCsv {

	private String A = "this is a test 23490235 dooodad.asdf\n#   of the\tYYYY local broad cast system.";

	/**
	 * Parses a string with a known set of words.
	 *
	 * @throws IOException
	 */
	@Test
	public void extractWords() throws IOException {
		ReadCsv reader = new ReadCsv(new ByteArrayInputStream(A.getBytes()));

		List<String> line0 = reader.extractWords();
		List<String> line1 = reader.extractWords();
		List<String> line2 = reader.extractWords();

		assertTrue(line2 == null);
		assertEquals(6, line0.size());
		assertEquals(8, line1.size());

		assertTrue(line0.get(0).compareTo("this") == 0);
		assertTrue(line0.get(2).compareTo("a") == 0);
		assertTrue(line1.get(3).compareTo("YYYY") == 0);
	}

	/**
	 * Parse the same string but specify a comment character.
	 */
	@Test
	public void extractWords_comments() throws IOException {
		ReadCsv reader = new ReadCsv(new ByteArrayInputStream(A.getBytes()));

		reader.setComment('#');

		List<String> line0 = reader.extractWords();
		List<String> line1 = reader.extractWords();

		assertTrue(line1 == null);
		assertEquals(6, line0.size());

		assertTrue(line0.get(0).compareTo("this") == 0);
		assertTrue(line0.get(2).compareTo("a") == 0);
	}
}
