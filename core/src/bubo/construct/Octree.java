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

import georegression.metric.Intersection3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import org.ddogleg.struct.FastQueue;

import java.util.List;

/**
 * Octree data structure which uses a point to define sub-structure inside.  Each node in the tree contains 8 children.
 * Which child a point belongs to is defined by the 'space' of the node and the 'divider' point, see below. If
 * a node is a leaf then the 'children' data structure will be null.  Each 3D point can optionally have a Object
 * reference associated with it.  For example, color or other information.
 * <p/>
 * <p>
 * <ul>
 * <li>children[0] = p.x <  divider.x and p.y <  divider.y and p.z <  divider.z</li>
 * <li>children[1] = p.x <  divider.x and p.y >= divider.y and p.z <  divider.z</li>
 * <li>children[2] = p.x >= divider.x and p.y <  divider.y and p.z <  divider.z</li>
 * <li>children[3] = p.x >= divider.x and p.y >= divider.y and p.z <  divider.z</li>
 * <li>children[4] = p.x <  divider.x and p.y <  divider.y and p.z >= divider.z</li>
 * <li>children[5] = p.x <  divider.x and p.y >= divider.y and p.z >= divider.z</li>
 * <li>children[6] = p.x >= divider.x and p.y <  divider.y and p.z >= divider.z</li>
 * <li>children[7] = p.x >= divider.x and p.y >= divider.y and p.z >= divider.z</li>
 * </ul>
 * </p>
 *
 * @author Peter Abeles
 */
public class Octree {
	/**
	 * Defines the space in which this node is contained
	 */
	public Cube3D_F64 space = new Cube3D_F64();
	/**
	 * The control point used to segment the space out into 8 children.  This is commonly the center
	 * of the cube.
	 */
	public Point3D_F64 divider = new Point3D_F64();

	/**
	 * Children of the node.  If a leaf then the children will be null.
	 */
	public Octree children[];

	/**
	 * The parent of this node
	 */
	public Octree parent;

	/**
	 * Can be used as a reference to user provided data;
	 */
	public Object userData;

	/**
	 * Points contained inside this node.  Depending on how it was constructed, all the points might be
	 * contained in the leafs or not.  New points are not declared by the FastQueue, just the storage array
	 */
	public FastQueue<Info> points = new FastQueue<Info>(Info.class, false);

	/**
	 * Returns true if it is a leaf node or false if it is not
	 *
	 * @return true for leaf node and false if not.
	 */
	public boolean isLeaf() {
		return children == null;
	}

	/**
	 * Finds all the nodes in the Octree which contain the point.  The search stops when it hits a leaf.
	 *
	 * @param point (Input) Point which is being searched for
	 * @param path  (Output) All the nodes which contain point.  Order will be from general to specific.
	 */
	public void findPathToPoint(Point3D_F64 point, List<Octree> path) {
		Octree node = this;

		// see if it is inside this space
		if (!Intersection3D_F64.contained(space, point))
			return;

		while (node != null) {
			path.add(node);

			if (node.isLeaf())
				break;

			node = node.children[node.getChildIndex(point)];
		}
	}

	/**
	 * Traverses down the octree and searches for the deepest node which contains the point
	 *
	 * @param point Point in which the leaf is contained.
	 * @return The deepest node which contains the point.  null if it's not bounded by the Octree.
	 */
	public Octree findDeepest(Point3D_F64 point) {
		// see if it is inside this space
		if (!Intersection3D_F64.contained(space, point))
			return null;

		Octree node = this;

		while (true) {
			if (node.isLeaf()) {
				return node;
			} else {
				int index = node.getChildIndex(point);
				Octree next = node.children[index];
				if (next == null)
					return node;
				else
					node = next;
			}
		}
	}

	/**
	 * Given a point inside the cube, return which child it belongs in.
	 *
	 * @param point A Point in space
	 * @return index of the child which contains it.
	 */
	public int getChildIndex(Point3D_F64 point) {
		int quad;

		if (point.x < divider.x) {
			if (point.y < divider.y) {
				quad = 0;
			} else {
				quad = 1;
			}
		} else {
			if (point.y < divider.y) {
				quad = 2;
			} else {
				quad = 3;
			}
		}
		if (point.z >= divider.z) {
			quad += 4;
		}

		return quad;
	}

	public <T> T getUserData() {
		return (T) userData;
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	public static class Info {
		public Point3D_F64 point;
		public Object data;
	}
}
