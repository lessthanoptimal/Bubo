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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUtilStrings {
	@SuppressWarnings({"StringEquality"})
	@Test
	public void checkAddString() {

		String a = "a";
		String b = "b";
		String c = "c";
		String aa = "a";

		List<String> list = new ArrayList<String>();

		assertTrue(a == UtilStrings.checkAddString(list, a));
		assertTrue(b == UtilStrings.checkAddString(list, b));
		assertTrue(c == UtilStrings.checkAddString(list, c));
		// pass in a string which has the same value as one already in the list
		// it should return an instance of the one in the list already
		assertTrue(a == UtilStrings.checkAddString(list, aa));
	}
}
