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

package bubo.construct;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestOctreeOps {

	@Test
	public void findLeafsWithData() {
		List<Octree_I32> input = new ArrayList<Octree_I32>();

		input.add( new Octree_I32());
		input.add( new Octree_I32());
		input.add( new Octree_I32());
		input.add( new Octree_I32());

		input.get(1).space.set(2,2,2,3,3,3);
		input.get(1).userData = 1;
		input.get(2).space.set(2,2,2,3,3,3);
		input.get(2).userData = 1;
		input.get(3).space.set(2,2,2,3,3,3);

		List<Octree_I32> filtered = OctreeOps.findLeafsWithData(input, null);

		assertEquals(2,filtered.size());
		assertTrue(filtered.get(0) == input.get(1));
		assertTrue(filtered.get(1) == input.get(2));
	}

	@Test
	public void findLeafsWithPoints() {
		List<Octree_I32> input = new ArrayList<Octree_I32>();

		input.add( new Octree_I32());
		input.add( new Octree_I32());
		input.add( new Octree_I32());
		input.add( new Octree_I32());

		input.get(1).space.set(2,2,2,3,3,3);
		input.get(1).points.grow();
		input.get(2).space.set(2,2,2,3,3,3);
		input.get(2).points.grow();
		input.get(3).space.set(2,2,2,3,3,3);

		List<Octree_I32> filtered = OctreeOps.findLeafsWithPoints(input, null);

		assertEquals(2,filtered.size());
		assertTrue(filtered.get(0) == input.get(1));
		assertTrue(filtered.get(1) == input.get(2));
	}
}
