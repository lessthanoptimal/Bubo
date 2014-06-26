/*
 * Copyright (c) 2013, Peter Abeles. All Rights Reserved.
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

package bubo.ptcloud;

import georegression.struct.point.Point3D_F64;

/**
 * Constructs an octree by adding a point and creating new cells until a cell which is smaller than the maximum cell
 * size is created.  Points are only added to leaf cells.
 *
 * @author Peter Abeles
 */
public class ConstructOctreeCellSize extends ConstructOctree {

	// The maximum size a cell can be which a point is placed inside of
	private double maximumCellSize;

	/**
	 * Specifies graph construction parameters
	 *
	 * @param maximumCellSize Create a new node with the number of points exceeds this threshold
	 */
	public ConstructOctreeCellSize(double maximumCellSize) {
		this.maximumCellSize = maximumCellSize;
	}

	/**
	 * Adds a point to the Octree.  If needed it will grow the tree
	 *
	 * @param point The point which is to be added
	 * @return The node which contains the point
	 */
	@Override
	public Octree addPoint( Point3D_F64 point , Object data ) {
		// declare the structure which stores the point and data
		Octree.Info info = storageInfo.grow();
		info.point = point;
		info.data = data;

		Octree node = tree;
		tree.points.add( info );

		while( true ) {
			if( node.isLeaf() ) {
				if( node.space.getLengthX() > maximumCellSize ) {
					// add data for the children
					node.children = getChildrenArray();
					// compute a divider in the center
					computeDivider(node.space,node.divider);

					// create a new child for point to go into
					int index = node.getChildIndex(point);
					node = checkAddChild(node,index);
				} else {
					// it is small enough
					node.points.add(info);
					return node;

				}
			} else {
				int index = node.getChildIndex(point);
				node = checkAddChild(node, index );
			}
		}
	}

}
