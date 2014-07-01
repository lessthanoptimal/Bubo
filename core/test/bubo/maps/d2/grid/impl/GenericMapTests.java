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

package bubo.maps.d2.grid.impl;

import bubo.maps.d2.grid.OccupancyGrid2D;
import bubo.maps.d2.grid.OccupancyGrid2D_F32;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Provides some generic map tests.  For some floating point tests a value can be passed in.  This is to allow
 * the test to set floating point values that it can store exactly.
 *
 * @author Peter Abeles
 */
public abstract class GenericMapTests {

	Random rand = new Random(24);

	public abstract <T extends OccupancyGrid2D> T createMap(int width, int height);

	public void clear_F32() {
		ArrayGrid2D_F32 map = new ArrayGrid2D_F32(5, 7);

		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				map.set(x, y, rand.nextFloat());
			}
		}

		map.clear();

		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				assertEquals(0.5f, map.get(x, y), 1e-4);
			}
		}
	}

	public void set_get_F32(float value) {
		OccupancyGrid2D_F32 map = new ArrayGrid2D_F32(5, 7);

		assertTrue(5.6f != map.get(2, 3));
		map.set(2, 3, value);
		assertTrue(value == map.get(2, 3));
	}

	public void isKnown_F32() {
		OccupancyGrid2D_F32 map = createMap(5, 7);

		map.set(2, 3, 0.5f);
		assertFalse(map.isKnown(2, 3));
		map.set(2, 3, 0.25f);
		assertTrue(map.isKnown(2, 3));
	}

	public void isValid_F32() {
		OccupancyGrid2D_F32 map = createMap(5, 7);

		assertTrue(map.isValid(0f));
		assertTrue(map.isValid(1.0f));
		assertTrue(map.isValid(0.6f));
		assertFalse(map.isValid(-0.01f));
		assertFalse(map.isValid(1.1f));
	}

	public void isInBounds() {
		int width = 6;
		int height = 12;
		OccupancyGrid2D map = createMap(width, height);

		assertTrue(map.isInBounds(0, 0));
		assertTrue(map.isInBounds(3, 3));
		assertTrue(map.isInBounds(5, 11));
		assertFalse(map.isInBounds(11, 5));
		assertFalse(map.isInBounds(12, 6));
		assertFalse(map.isInBounds(-1, -1));
	}

	public void getWidth_getHeight() {
		int width = 6;
		int height = 12;
		OccupancyGrid2D map = createMap(width, height);

		assertEquals(width, map.getWidth());
		assertEquals(height, map.getHeight());
	}
}
