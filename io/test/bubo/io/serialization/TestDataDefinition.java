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

package bubo.io.serialization;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestDataDefinition {

	@Test
	public void constructor() {
		DataDefinition def = new DataDefinition("Foo", DummyData.class, "value");

		assertTrue(def.typeName.compareTo("Foo") == 0);
		assertTrue(def.type == DummyData.class);
		assertEquals(1, def.setters.length);
		assertEquals(1, def.getters.length);
		assertEquals(1, def.variableNames.length);
		assertEquals(1, def.variableTypes.length);
		assertTrue(def.variableNames[0].compareTo("value") == 0);
	}

	@Test
	public void createInstance() {
		DataDefinition def = new DataDefinition("Foo", DummyData.class, "value");

		DummyData found = def.createInstance(45);

		assertEquals(45, found.value);
	}
}
