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
import org.junit.Test;

import java.util.Random;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConstructOctree {

	Random rand = new Random(234);

	private static int countNodes(Octree node, int total) {

		total++;
		if (node.isLeaf())
			return total;

		for (int i = 0; i < node.children.length; i++) {
			if (node.children[i] == null)
				continue;
			total = countNodes(node.children[i], total);
		}
		return total;
	}

	private static int countLeafs(Octree node, int total) {

		if (node.isLeaf())
			return total + 1;

		for (int i = 0; i < node.children.length; i++) {
			if (node.children[i] == null)
				continue;
			total = countLeafs(node.children[i], total);
		}
		return total;
	}

	@Test
	public void reset() {
		// use a specific instrance for testing purposes
		ConstructOctree<Octree_F64,Point3D_F64> alg = new Dummy();

		// randomly construct a tree with 100 points
		for (int i = 0; i < 100; i++) {
			Octree_F64 o = alg.addPoint(new Point3D_F64(), null);
			o.userData = 1;
		}

		// see if it declared the expected amount of data
		int expectedNode = countNodes(alg.getTree(), 0);
		int foundNodes = alg.storageNodes.size;

		assertEquals(expectedNode, foundNodes);
		assertEquals(100, alg.storageInfo.size());

		int numLeafs = countLeafs(alg.getTree(), 0);

		// now reset
		alg.reset();

		// these should have a size of zero
		assertEquals(1, alg.storageNodes.size); // root is 1
		assertEquals(0, alg.storageInfo.size);
		// this should have all the children arrays added to it
		assertEquals(expectedNode - numLeafs, alg.storageChildren.size());

		// check the structures to see that they have been reset correctly
		for (Octree[] children : (Stack<Octree[]>)(Object)alg.storageChildren) {
			assertEquals(8, children.length);
			for (int i = 0; i < children.length; i++) {
				assertTrue(children[i] == null);
			}
		}
		for (Octree n : (Octree[])alg.storageNodes.data) {
			assertTrue(n.userData == null);
			assertTrue(n.parent == null);
			assertTrue(n.children == null);
			assertTrue(n.points.size() == 0);
		}
		for (Octree.Info n : (Octree.Info[])alg.storageInfo.data) {
			assertTrue(n.point == null);
			assertTrue(n.userData == null);
		}

	}

	/**
	 * Make sure the Octree.parent is correctly assigned
	 */
	@Test
	public void checkParent() {
		ConstructOctree alg = new Dummy();

		for (int i = 0; i < 100; i++) {
			Point3D_F64 a = new Point3D_F64();
			a.x = rand.nextGaussian() * 10;
			a.y = rand.nextGaussian() * 10;
			a.z = rand.nextGaussian() * 10;

			alg.addPoint(a, null);
		}

		int numNull = 0;
		for (int index = 0; index < alg.storageNodes.size; index++) {
			Octree n = (Octree)alg.storageNodes.data[index];
			Octree p = n.parent;
			if (p == null) {
				numNull++;
			} else {
				boolean match = false;
				for (int i = 0; i < p.children.length; i++) {
					if (p.children[i] == n) {
						match = true;
						break;
					}
				}
				assertTrue(match);
			}
		}
		// only the root should be null
		assertEquals(1, numNull);
	}

	public static class Dummy extends ConstructOctree<Octree_F64,Point3D_F64> {
		Random rand = new Random(234);

		public Dummy() {
			super(Octree_F64.class);
		}

		@Override
		public Octree_F64 addPoint(Point3D_F64 point, Object data) {

			Octree_F64.Info info = storageInfo.grow();
			info.userData = data;
			info.point = point;

			Octree_F64 node = tree;

			while (rand.nextDouble() > 0.7) {
				if (node.children == null) {
					node.children = getChildrenArray();
				}
				node = checkAddChild(node, rand.nextInt(8), info);
			}

			return node;
		}

		@Override
		public void setChildSpace(Octree_F64 parent, int index, Octree_F64 child) {

		}

		@Override
		public boolean isSpaceValid(Octree_F64 node) {return true;}
	}

}
