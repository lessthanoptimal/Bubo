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

package bubo.maps;

import bubo.maps.d3.grid.impl.OctreeGridMap_F64;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilMaps {
	@Test
	public void sameShape_grid3d() {

		OctreeGridMap_F64 mapA = new OctreeGridMap_F64(2,3,4);

		assertTrue(UtilMaps.sameShape(mapA,new OctreeGridMap_F64(2,3,4)));
		assertFalse(UtilMaps.sameShape(mapA,new OctreeGridMap_F64(3,3,4)));
		assertFalse(UtilMaps.sameShape(mapA, new OctreeGridMap_F64(2, 4, 4)));
		assertFalse(UtilMaps.sameShape(mapA, new OctreeGridMap_F64(2, 3, 5)));
	}
}