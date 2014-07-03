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

import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestOctree {

	@Test
	public void findPathToPoint() {

		Point3D_F64 target = new Point3D_F64(21, 22, 27);

		Octree tree = new Octree();
		tree.divider = new Point3D_F64(20, 30, 40);
		tree.space = new Cube3D_F64(-100, -100, -100, 200, 200, 200);

		Octree node0 = new Octree();
		tree.divider = new Point3D_F64(5, -10, 50);

		Octree node1 = new Octree();

		tree.children = new Octree[8];
		node0.children = new Octree[8];

		tree.children[tree.getChildIndex(target)] = node0;
		node0.children[node0.getChildIndex(target)] = node1;

		List<Octree> found = new ArrayList<Octree>();
		tree.findPathToPoint(target, found);

		assertEquals(3, found.size());
		assertTrue(tree == found.get(0));
		assertTrue(node0 == found.get(1));
		assertTrue(node1 == found.get(2));
	}

	@Test
	public void findDeepest() {
		Point3D_F64 target = new Point3D_F64(21, 22, 27);

		Octree tree = new Octree();
		tree.divider = new Point3D_F64(20, 30, 40);
		tree.space = new Cube3D_F64(-100, -100, -100, 200, 200, 200);

		Octree node0 = new Octree();
		tree.divider = new Point3D_F64(5, -10, 50);

		Octree node1 = new Octree();

		tree.children = new Octree[8];
		node0.children = new Octree[8];

		tree.children[tree.getChildIndex(target)] = node0;
		node0.children[node0.getChildIndex(target)] = node1;

		assertTrue(null == tree.findDeepest(new Point3D_F64(10000, 0, 0)));
		assertTrue(node1 == tree.findDeepest(target));
		assertTrue(tree == tree.findDeepest(new Point3D_F64(-99, -99, -99)));
	}

	@Test
	public void getChildIndex() {
		Octree tree = new Octree();
		tree.divider = new Point3D_F64(20, 30, 40);

		// check easy cases
		assertEquals(0, tree.getChildIndex(new Point3D_F64(18, 28, 38)));
		assertEquals(1, tree.getChildIndex(new Point3D_F64(18, 32, 38)));
		assertEquals(2, tree.getChildIndex(new Point3D_F64(22, 28, 38)));
		assertEquals(3, tree.getChildIndex(new Point3D_F64(22, 32, 38)));
		assertEquals(4, tree.getChildIndex(new Point3D_F64(18, 28, 42)));
		assertEquals(5, tree.getChildIndex(new Point3D_F64(18, 32, 42)));
		assertEquals(6, tree.getChildIndex(new Point3D_F64(22, 28, 42)));
		assertEquals(7, tree.getChildIndex(new Point3D_F64(22, 32, 42)));

		// check edge cases
		assertEquals(7, tree.getChildIndex(new Point3D_F64(20, 30, 40)));

		assertEquals(2, tree.getChildIndex(new Point3D_F64(20, 28, 38)));
		assertEquals(1, tree.getChildIndex(new Point3D_F64(18, 30, 38)));
		assertEquals(4, tree.getChildIndex(new Point3D_F64(18, 28, 40)));
	}

}
