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

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestReadCsvObject {

	String testString = "12 e -345 8345 -49358 56.446 9342.4234 foobar";
	String testString2 = "12 e -345 8345 -49358";

	/**
	 * Read in the object, but with no arrays.
	 */
	@Test
	public void testPositive_noarray() throws IOException {
		ReadCsvObject<TestClass> reader = new ReadCsvObject<TestClass>(new ByteArrayInputStream(testString.getBytes()), TestClass.class, "a", "b", "c", "d", "e", "f", "g", "h");

		TestClass t = reader.nextObject(null);

		assertEquals((byte) 12, t.a);
		assertEquals('e', t.b);
		assertEquals((short) -345, t.c);
		assertEquals((int) 8345, t.d);
		assertEquals((long) -49358, t.e);
		assertEquals((float) 56.446, t.f, 1e-5);
		assertEquals((double) 9342.4234, t.g, 1e-5);
		assertTrue("foobar".compareTo(t.h) == 0);
	}

	/**
	 * Read in the object and its array
	 */
	@Test
	public void testPositive_array() throws IOException {
		ReadCsvObject<TestClass> reader = new ReadCsvObject<TestClass>(new ByteArrayInputStream(testString2.getBytes()), TestClass.class, "a", "b", "i");

		TestClass t = reader.nextObject(null);

		assertEquals((byte) 12, t.a);
		assertEquals('e', t.b);
		assertEquals(-345, t.i[0]);
		assertEquals(8345, t.i[1]);
		assertEquals(-49358, t.i[2]);
	}

	/**
	 * It is told to parse a class that has a variable of a bad type.
	 */
	@Test(expected = IOException.class)
	public void testTooMannyWords() throws IOException {
		ReadCsvObject<TestClass> reader = new ReadCsvObject<TestClass>(new ByteArrayInputStream(testString.getBytes()), TestClass.class, "a", "b", "i");

		TestClass t = reader.nextObject(null);
	}

	/**
	 * Give it a file with too few words on a line.
	 */
	@Test(expected = IOException.class)
	public void testTooFewWords() throws IOException {
		ReadCsvObject<TestClass> reader = new ReadCsvObject<TestClass>(new ByteArrayInputStream(testString2.getBytes()), TestClass.class, "a", "b", "c", "i");

		TestClass t = reader.nextObject(null);
	}

	@Test
	public void readAll() {
		fail("Implement");
	}

	public static class TestClass {
		byte a;
		char b;
		short c;
		int d;
		long e;
		float f;
		double g;
		String h;

		int i[] = new int[3];

		public TestClass() {
		}

		public byte getA() {
			return a;
		}

		public void setA(byte a) {
			this.a = a;
		}

		public char getB() {
			return b;
		}

		public void setB(char b) {
			this.b = b;
		}

		public short getC() {
			return c;
		}

		public void setC(short c) {
			this.c = c;
		}

		public int getD() {
			return d;
		}

		public void setD(int d) {
			this.d = d;
		}

		public long getE() {
			return e;
		}

		public void setE(long e) {
			this.e = e;
		}

		public float getF() {
			return f;
		}

		public void setF(float f) {
			this.f = f;
		}

		public double getG() {
			return g;
		}

		public void setG(double g) {
			this.g = g;
		}

		public String getH() {
			return h;
		}

		public void setH(String h) {
			this.h = h;
		}

		public int[] getI() {
			return i;
		}

		public void setI(int[] i) {
			this.i = i;
		}
	}
}
