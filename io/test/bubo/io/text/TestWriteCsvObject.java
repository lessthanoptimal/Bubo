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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestWriteCsvObject {

	/**
	 * Sees if it can write a single line comment just fine.
	 */
	@Test
	public void comment_single_line() throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		WriteCsvObject alg = new WriteCsvObject(out, TestReadCsvObject.TestClass.class, "a", "b", "c", "d", "e", "f", "g", "h");

		alg.writeComment("Three Rings for the Elven-kings under the sky,");

		String found = out.toString();

		assertTrue(found.compareTo("#Three Rings for the Elven-kings under the sky,\n") == 0);
	}

	/**
	 * Sees if it can write multiple-line comments
	 */
	@Test
	public void comment_multiple_lines() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		WriteCsvObject alg = new WriteCsvObject(out, TestReadCsvObject.TestClass.class, "a", "b", "c", "d", "e", "f", "g", "h");

		alg.writeComment("Three Rings for the Elven-kings under the sky,\n" +
				"Seven for the dwarf-lords in their halls of stone,");

		String found = out.toString();

		String expected = "#Three Rings for the Elven-kings under the sky,\n" +
				"#Seven for the dwarf-lords in their halls of stone,\n";

		assertTrue(found.compareTo(expected) == 0);
	}

	/**
	 * Write out an object with just primitive types
	 */
	@Test
	public void justPrimitive() throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		WriteCsvObject alg = new WriteCsvObject(out, TestReadCsvObject.TestClass.class, "a", "b", "c", "d", "e", "f", "g", "h");

		TestReadCsvObject.TestClass c = new TestReadCsvObject.TestClass();

		c.a = 12;
		c.b = 'e';
		c.c = -345;
		c.d = 8345;
		c.e = -49358;
		c.f = 56.446f;
		c.g = 9342.4234;
		c.h = "foobar";

		alg.writeObject(c);

		String expected = "12 e -345 8345 -49358 56.446 9342.4234 foobar\n";
		String found = out.toString();

		assertTrue(found.compareTo(expected) == 0);
	}

	/**
	 * Write out an object with array and primitive types
	 */
	@Test
	public void primitiveAndArray() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		WriteCsvObject alg = new WriteCsvObject(out, TestReadCsvObject.TestClass.class, "a", "b", "i");

		TestReadCsvObject.TestClass c = new TestReadCsvObject.TestClass();

		c.a = 12;
		c.b = 'e';
		c.i[0] = -345;
		c.i[1] = 8345;
		c.i[2] = -49358;

		alg.writeObject(c);

		String expected = "12 e -345 8345 -49358\n";
		String found = out.toString();

		assertTrue(found.compareTo(expected) == 0);
	}
}
