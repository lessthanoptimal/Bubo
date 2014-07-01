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

import bubo.io.serialization.SerializationDefinitionManager;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestReadCsvObjectSmart {

	String testA = "12 -34245 12.0 578.234 74.3";
	String testB = "12 578.234";

	SerializationDefinitionManager defs;


	void setupDefinitions(String... paramNames) {
		defs = new SerializationDefinitionManager();
		defs.loadDefinition("TestClass", TestClass.class, paramNames);
		defs.loadDefinition("TestClassB", TestClassB.class, "a");
	}

	/**
	 * Positive case where all the variables are primitive variables.
	 */
	@Test
	public void testPositive_primitive() throws IOException {
		setupDefinitions("a", "b");
		ReadCsvObjectSmart<TestClass> reader = new ReadCsvObjectSmart<TestClass>(new ByteArrayInputStream(testA.substring(0, 9).getBytes()), defs, "TestClass");

		TestClass o = new TestClass();

		reader.nextObject(o);

		assertEquals((byte) 12, o.a);
		assertEquals(-34245, o.b);

		assertTrue(null == reader.nextObject(null));
	}

	/**
	 * Positive case where one of the variables if an array object.
	 */
	@Test
	public void testPositive_array() throws IOException {
		setupDefinitions("a", "b", "c");
		ReadCsvObjectSmart<TestClass> reader = new ReadCsvObjectSmart<TestClass>(new ByteArrayInputStream(testA.getBytes()), defs, "TestClass");

		TestClass o = new TestClass();

		reader.nextObject(o);

		assertEquals((byte) 12, o.a);
		assertEquals(-34245, o.b);
		assertEquals(12.0f, o.c[0], 1e-5);
		assertEquals(578.234f, o.c[1], 1e-5);
		assertEquals(74.3f, o.c[2], 1e-5);

		assertTrue(null == reader.nextObject(null));
	}

	/**
	 * Positive case where one of the variables if a Bubo serializable object.
	 */
	@Test
	public void testPositive_bubo() throws IOException {
		setupDefinitions("b", "d");
		ReadCsvObjectSmart<TestClass> reader = new ReadCsvObjectSmart<TestClass>(new ByteArrayInputStream(testB.getBytes()), defs, "TestClass");

		TestClass o = new TestClass();

		reader.nextObject(o);

		assertEquals(12, o.b);
		assertEquals(578.234f, o.d.a, 1e-5);

		assertTrue(null == reader.nextObject(null));
	}

	/**
	 * Negative case where there are too few words to read in.
	 */
	@Test(expected = IOException.class)
	public void testNegative_short() throws IOException {
		setupDefinitions("a", "b", "c", "c");
		ReadCsvObjectSmart<TestClass> reader = new ReadCsvObjectSmart<TestClass>(new ByteArrayInputStream(testA.getBytes()), defs, "TestClass");

		TestClass o = new TestClass();

		reader.nextObject(o);
	}

	/**
	 * Negative case where there are too many words on the line.
	 */
	@Test(expected = IOException.class)
	public void testNegative_long() throws IOException {
		setupDefinitions("a", "b");
		ReadCsvObjectSmart<TestClass> reader = new ReadCsvObjectSmart<TestClass>(new ByteArrayInputStream(testA.getBytes()), defs, "TestClass");

		TestClass o = new TestClass();

		reader.nextObject(o);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static class TestClassB {
		float a;

		public float getA() {
			return a;
		}

		public void setA(float a) {
			this.a = a;
		}
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static class TestClass {

		byte a;
		int b;
		float[] c = new float[3];
		TestClassB d = new TestClassB();

		public TestClass() {
		}

		public byte getA() {
			return a;
		}

		public void setA(byte a) {
			this.a = a;
		}

		public int getB() {
			return b;
		}

		public void setB(int b) {
			this.b = b;
		}

		public float[] getC() {
			return c;
		}

		public void setC(float[] c) {
			this.c = c;
		}

		public TestClassB getD() {
			return d;
		}

		public void setD(TestClassB d) {
			this.d = d;
		}
	}
}
