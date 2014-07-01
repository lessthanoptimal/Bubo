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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilReflections {

	@Test
	public void isPrimitiveType() {
		assertTrue(UtilReflections.isPrimitiveType(byte.class));
		assertTrue(UtilReflections.isPrimitiveType(char.class));
		assertTrue(UtilReflections.isPrimitiveType(short.class));
		assertTrue(UtilReflections.isPrimitiveType(int.class));
		assertTrue(UtilReflections.isPrimitiveType(long.class));
		assertTrue(UtilReflections.isPrimitiveType(float.class));
		assertTrue(UtilReflections.isPrimitiveType(double.class));

		assertFalse(UtilReflections.isPrimitiveType(byte[].class));
		assertFalse(UtilReflections.isPrimitiveType(UtilReflections.class));
	}

	@Test
	public void isPrimitiveArrayType() {
		assertTrue(UtilReflections.isPrimitiveArrayType(byte[].class));
		assertTrue(UtilReflections.isPrimitiveArrayType(char[].class));
		assertTrue(UtilReflections.isPrimitiveArrayType(short[].class));
		assertTrue(UtilReflections.isPrimitiveArrayType(int[].class));
		assertTrue(UtilReflections.isPrimitiveArrayType(long[].class));
		assertTrue(UtilReflections.isPrimitiveArrayType(float[].class));
		assertTrue(UtilReflections.isPrimitiveArrayType(double[].class));

		assertFalse(UtilReflections.isPrimitiveArrayType(byte.class));
		assertFalse(UtilReflections.isPrimitiveArrayType(double[][].class));
		assertFalse(UtilReflections.isPrimitiveArrayType(UtilReflections.class));
	}

	@Test
	public void findSetter() throws InvocationTargetException, IllegalAccessException {
		SimpleClass a = new SimpleClass();

		assertTrue(a.value == 0);

		Method m = UtilReflections.findSetter(SimpleClass.class, "value");
		m.invoke(a, 5);

		assertTrue(a.value == 5);
	}

	@Test
	public void findGetter() throws InvocationTargetException, IllegalAccessException {
		SimpleClass a = new SimpleClass();
		a.value = 5;

		Method m = UtilReflections.findGetter(SimpleClass.class, "value");
		int found = (Integer) m.invoke(a);

		assertTrue(a.value == found);
	}

	/**
	 * Test against various pre-made strings
	 */
	@Test
	public void createAccessorName() {
		String variable = "ponies";

		assertTrue(UtilReflections.createAccessorName(variable, true, false).compareTo("getPonies") == 0);
		assertTrue(UtilReflections.createAccessorName(variable, false, false).compareTo("setPonies") == 0);
		assertTrue(UtilReflections.createAccessorName(variable, true, true).compareTo("isPonies") == 0);
		assertTrue(UtilReflections.createAccessorName(variable, false, true).compareTo("setPonies") == 0);
	}

	private static class SimpleClass {
		int value;

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}
}
