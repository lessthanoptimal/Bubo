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

import georegression.struct.point.Point3D_I32;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestOctree_I32 {

	@Test
	public void isSmallest() {
		Octree_I32 a = new Octree_I32();
		a.space.set(0,1,2,1,2,3);
		assertTrue(a.isSmallest());

		a.space.set(0,1,2,2,2,3);
		assertFalse(a.isSmallest());

		a.space.set(0,1,2,1,3,3);
		assertFalse(a.isSmallest());

		a.space.set(0,1,2,1,2,4);
		assertFalse(a.isSmallest());
	}

	@Test
	public void getChildIndex() {
		Octree_I32 tree = new Octree_I32();
		tree.divider = new Point3D_I32(20, 30, 40);

		// check easy cases
		assertEquals(0, tree.getChildIndex(new Point3D_I32(18, 28, 38)));
		assertEquals(1, tree.getChildIndex(new Point3D_I32(18, 32, 38)));
		assertEquals(2, tree.getChildIndex(new Point3D_I32(22, 28, 38)));
		assertEquals(3, tree.getChildIndex(new Point3D_I32(22, 32, 38)));
		assertEquals(4, tree.getChildIndex(new Point3D_I32(18, 28, 42)));
		assertEquals(5, tree.getChildIndex(new Point3D_I32(18, 32, 42)));
		assertEquals(6, tree.getChildIndex(new Point3D_I32(22, 28, 42)));
		assertEquals(7, tree.getChildIndex(new Point3D_I32(22, 32, 42)));

		// check edge cases
		assertEquals(7, tree.getChildIndex(new Point3D_I32(20, 30, 40)));

		assertEquals(2, tree.getChildIndex(new Point3D_I32(20, 28, 38)));
		assertEquals(1, tree.getChildIndex(new Point3D_I32(18, 30, 38)));
		assertEquals(4, tree.getChildIndex(new Point3D_I32(18, 28, 40)));
	}

}
