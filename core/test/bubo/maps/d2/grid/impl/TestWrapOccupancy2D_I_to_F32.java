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
import bubo.maps.d2.grid.OccupancyGrid2D_I;
import org.junit.Test;

/**
 * Checks to see if a wrapper around integer grid maps have the expected behavior.  Care is taken to pass in
 * floating point values which are discretized nicely.
 *
 * @author Peter Abeles
 */
public class TestWrapOccupancy2D_I_to_F32 extends GenericMapTests {

	/**
	 * Checks to see if setAll() called on the original map has the same affect as calling it through the wrapper.
	 * Also checks to see if the wrapper set the floating point value correctly
	 */
	@Test
	public void clear() {
		super.clear_F32();
	}

	/**
	 * Sees if set and get produce the expected results
	 */
	@Test
	public void set_get() {
		super.set_get_F32(2f / (float) ArrayGrid2D_I8.MAX);
	}

	@Test
	public void isValid() {
		super.isValid_F32();
	}

	@Test
	public void isInBounds() {
		super.isInBounds();
	}

	@Test
	public void isKnown() {
		super.isKnown_F32();
	}

	@Test
	public void getWidth_getHeight() {
		super.getWidth_getHeight();
	}

	@Override
	public <T extends OccupancyGrid2D> T createMap(int width, int height) {
		OccupancyGrid2D_I orig = new ArrayGrid2D_I8(width, height);
		return (T) new WrapOccupancy2D_I_to_F32(orig);
	}
}
