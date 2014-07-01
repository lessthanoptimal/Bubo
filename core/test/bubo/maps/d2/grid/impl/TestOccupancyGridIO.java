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

import bubo.maps.d2.grid.OccupancyGrid2D_F32;
import bubo.maps.d2.grid.OccupancyGrid2D_I;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestOccupancyGridIO {

	Random rand = new Random(24234);

	public static void checkIdentical(OccupancyGrid2D_I a, OccupancyGrid2D_I b) {
		assertEquals(a.getMaxValue(), b.getMaxValue());
		assertEquals(a.getWidth(), b.getWidth());
		assertEquals(a.getHeight(), b.getHeight());

		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				assertEquals(a.get(x, y), b.get(x, y));
			}
		}
	}

	public static void checkIdentical(OccupancyGrid2D_F32 a, OccupancyGrid2D_F32 b, float tol) {
		assertEquals(a.getWidth(), b.getWidth());
		assertEquals(a.getHeight(), b.getHeight());

		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				assertEquals(a.get(x, y), b.get(x, y), tol);
			}
		}
	}

	/**
	 * Checks to see if loading and saving from an image works for ArrayGrid2D_I8
	 */
	@Test
	public void testImage_Load_Save_I8() {
		OccupancyGrid2D_I map = new ArrayGrid2D_I8(10, 20);

		randomFill(map);

		BufferedImage image = OccupancyGridIO.render_I(map);
		OccupancyGrid2D_I found = OccupancyGridIO.load_I8(image);

		checkIdentical(map, found);
	}

	/**
	 * Checks to see if loading and saving from an image works for ArrayGrid2D_F32
	 */
	@Test
	public void testImage_Load_Save_F32() {
		OccupancyGrid2D_F32 map = new ArrayGrid2D_F32(10, 20);

		randomFill(map);

		BufferedImage image = OccupancyGridIO.render_F32(map, false);
		OccupancyGrid2D_F32 found = OccupancyGridIO.load_F32(image);

		checkIdentical(map, found, (1.0f / 255.0f));
	}

	private void randomFill(OccupancyGrid2D_I map) {

		int max = map.getMaxValue();

		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				int val = rand.nextInt(max);
				map.set(x, y, val);
			}
		}

	}

	private void randomFill(OccupancyGrid2D_F32 map) {

		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				map.set(x, y, rand.nextFloat());
			}
		}

	}
}
