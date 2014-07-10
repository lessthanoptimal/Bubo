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

package bubo.maps.d3.grid.impl;

import bubo.maps.d3.grid.CellProbability_F64;
import bubo.maps.d3.grid.OccupancyGrid3D_F64;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public abstract class GenericOccupancyGrid3DChecks {

	public abstract OccupancyGrid3D_F64 createOccupancyGrid( int sizeX , int sizeY , int sizeZ );

	@Test
	public void initialUnknown() {
		OccupancyGrid3D_F64 map = createOccupancyGrid(2,3,4);

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 4; k++) {
					assertEquals(0.5,map.get(i,j,k),1e-8);
				}
			}
		}
	}

	@Test
	public void set_and_get() {
		OccupancyGrid3D_F64 map = createOccupancyGrid(3,4,5);

		map.set(1,2,3,0.9);
		assertEquals(0.9,map.get(1,2,3),1e-8);

		assertEquals(map.getDefaultValue(),map.get(2,2,3),1e-8);
		assertEquals(map.getDefaultValue(),map.get(0,2,3),1e-8);
		assertEquals(map.getDefaultValue(),map.get(2,3,4),1e-8);
	}

	@Test
	public void iteratorKnown() {

		Iterator<CellProbability_F64> iter = null;
		OccupancyGrid3D_F64 map = createOccupancyGrid(20,30,40);

		assertFalse(map.iteratorKnown().hasNext());

		map.set(10,11,2,0.9);
		iter = map.iteratorKnown();
		assertTrue(iter.hasNext());
		assertEquals(0.9,iter.next().getProbability(),1e-8);
		assertFalse(iter.hasNext());

		map.set(0,0,0,0.8);
		iter = map.iteratorKnown();
		assertTrue(iter.hasNext());
		iter.next();
		assertTrue(iter.hasNext());
		iter.next();
		assertFalse(iter.hasNext());
	}

	@Test
	public void copy() {
		Random rand = new Random(234);
		OccupancyGrid3D_F64 map = createOccupancyGrid(10,12,14);
		for (int i = 0; i < 100; i++) {
			int x = rand.nextInt(map.getSizeX());
			int y = rand.nextInt(map.getSizeY());
			int z = rand.nextInt(map.getSizeZ());
			double p = rand.nextDouble();
			map.set(x,y,z,p);
		}

		OccupancyGrid3D_F64 copy = map.copy();
		for (int i = 0; i < map.getSizeX(); i++) {
			for (int j = 0; j < map.getSizeY(); j++) {
				for (int k = 0; k < map.getSizeZ(); k++) {
					double found = copy.get(i,j,k);
					double expected = map.get(i,j,k);
					assertTrue(expected==found);
				}
			}
		}
	}
}
