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
public class TestSerializationDefinitionManager {

	@Test
	public void loadDefinition_string() {
		SerializationDefinitionManager manager = new SerializationDefinitionManager();
		manager.addPath("bubo.io.serialization");

		DataDefinition found = manager.loadDefinition(DummyDataTypeDescription.class.getSimpleName());

		checkLoad(manager, found, DummyDataTypeDescription.class);
	}

	@Test
	public void loadDefinition_string_class() {
		SerializationDefinitionManager manager = new SerializationDefinitionManager();

		DataDefinition found = manager.loadDefinition(DummyData.class, "value");

		checkLoad(manager, found, DummyData.class);
	}

	@Test
	public void addDefinition() {
		SerializationDefinitionManager manager = new SerializationDefinitionManager();

		DataDefinition found = manager.loadDefinition(DummyData.class, "value");

		SerializationDefinitionManager manager2 = new SerializationDefinitionManager();
		manager2.addDefinition(found);

		checkLoad(manager2, found, DummyData.class);
	}

	private void checkLoad(SerializationDefinitionManager manager, DataDefinition found, Class<?> type) {
		assertTrue(found.type == type);
		assertTrue(found.typeName.compareTo(type.getSimpleName()) == 0);
		assertEquals(1, found.setters.length);
		assertEquals(1, found.getters.length);
		assertEquals(1, found.variableNames.length);
		assertEquals(1, found.variableTypes.length);
		assertTrue(found.variableNames[0].compareTo("value") == 0);

		assertTrue(found == manager.lookup(type.getSimpleName()));
	}
}
