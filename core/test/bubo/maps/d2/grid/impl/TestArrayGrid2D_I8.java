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

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestArrayGrid2D_I8 {

	Random rand = new Random(234);

	@Test
	public void clear() {
		ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5, 7);

		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				map.set(x, y, rand.nextInt(map.getMaxValue()));
			}
		}

		map.clear();

		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				assertEquals(map.getUnknown(), map.get(x, y), 1e-4);
			}
		}
	}

	@Test
	public void set_get() {
		ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5, 7);

		assertTrue(12 != map.get(2, 3));
		map.set(2, 3, 12);
		assertTrue(12 == map.get(2, 3));
	}

	@Test
	public void isKnown() {
		ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5, 7);

		map.set(2, 3, ArrayGrid2D_I8.UNKNOWN);
		assertFalse(map.isKnown(2, 3));
		map.set(2, 3, 10);
		assertTrue(map.isKnown(2, 3));
	}

	@Test
	public void isValid() {
		ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5, 7);

		assertTrue(map.isValid(0));
		assertTrue(map.isValid(256));
		assertTrue(map.isValid(70));
		assertFalse(map.isValid(-1));
		assertFalse(map.isValid(1000));
	}
}
