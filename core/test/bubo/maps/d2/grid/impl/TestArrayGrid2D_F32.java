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
import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestArrayGrid2D_F32 extends GenericMapTests {

	@Test
	public void clear() {
		super.clear_F32();
	}

	@Test
	public void set_get() {
		super.set_get_F32(5.6f);
	}

	@Test
	public void isKnown() {
		super.isKnown_F32();
	}

	@Test
	public void isValid() {
		super.isValid_F32();
	}

	@Override
	public <T extends OccupancyGrid2D> T createMap(int width, int height) {
		return (T) new ArrayGrid2D_F32(width, height);
	}
}
