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
import georegression.struct.shapes.Cube3D_F64;
import org.ddogleg.struct.FastQueue;

import java.util.List;
import java.util.Stack;

/**
 * Constructs quad tree one point at a time.  The divider point is always the center of the cube and the same graph
 * will be produced independent of the order in which points are added.   Data structures associated with the
 * Octree are managed and saved by this class and recycled with {@link #reset} is called.
 *
 * @author Peter Abeles
 */
public class ConstructOctreeEqual {

	// create a new node in the graph when the number of points exceeds
	private int divideThreshold;

	// the Octree that it modifies
	private Octree tree;

	// save references to all data structures declared to create the tree
	protected FastQueue<Octree.Info> storageInfo = new FastQueue<Octree.Info>(Octree.Info.class,true);
	// Contains all nodes in the tree
	protected FastQueue<Octree> storageNodes = new FastQueue<Octree>(Octree.class,true);
	protected Stack<Octree[]> storageChildren = new Stack<Octree[]>();

	/**
	 * Specifies graph construction parameters
	 *
	 * @param divideThreshold Create a new node with the number of points exceeds this threshold
	 */
	public ConstructOctreeEqual(int divideThreshold) {
		this.divideThreshold = divideThreshold;
		this.tree = storageNodes.grow();
	}

	/**
	 * Initializes the Octree.  The space contained by the Octree is specified by the passed in cube.
	 * {@link #reset} is automatically called by this function
	 * @param cube  Space which is contained by the Octree.
	 */
	public void initialize( Cube3D_F64 cube ) {
		reset();
		tree.space.set(cube);
	}

	/**
	 * Discards the existing tree structure and recycles its data.  All reference to external data not owned
	 * by the graph is discarded
	 */
	public void reset() {
		// remove references to external data
		for( int i = 0; i < storageInfo.size; i++ ) {
			Octree.Info info = storageInfo.data[i];
			info.data = null;
			info.point = null;
		}
		storageInfo.reset();

		for( int i = 0; i < storageNodes.size; i++ ) {
			Octree o = storageNodes.data[i];
			if( o.children != null ) {
				for( int j = 0; j < 8; j++ ) {
					o.children[j] = null;
				}

				storageChildren.add(o.children);
			}
			o.parent = null;
			o.children = null;
			o.points.reset();
		}
		storageNodes.reset();

		// add the root tree again now that everything has been cleaned up
		this.tree = storageNodes.grow();
	}

	/**
	 * Adds all points to the Octree
	 *
	 * @param points List of points to add
	 */
	public void addPoints( List<Point3D_F64> points ) {
		int N = points.size();
		for( int i = 0; i < N; i++ ) {
			addPoint(points.get(i),null);
		}
	}

	/**
	 * Adds a point to the Octree.  If needed it will grow the tree
	 *
	 * @param point The point which is to be added
	 * @return The node which contains the point
	 */
	public Octree addPoint( Point3D_F64 point , Object data ) {
		// declare the structure which stores the point and data
		Octree.Info info = storageInfo.grow();
		info.point = point;
		info.data = data;

		Octree node = tree;
		tree.points.add( info );

		while( true ) {
			if( node.isLeaf() ) {
				// see if it needs to create a new node
				if( node.points.size() > divideThreshold ) {
					node.children = getChildrenArray();
					computeDivider(node.space,node.divider);

					// create a new child for point to go into
					int index = node.getChildIndex(point);
					Octree child = checkAddChild(node,index, info);

					// Create new children where appropriate for all points in node, but 'point'
					for( int i = 0; i < node.points.size-1; i++ ) {
						Octree.Info infoP = node.points.get(i);
						int indexP = node.getChildIndex(infoP.point);

						// see if the node exists
						checkAddChild(node, indexP, infoP );
					}

					// check for the pathological case where all the points are identical
					boolean pathological = checkPathological(node);

					if( pathological ) {
						// avoid infinite recursion by not splitting this node yet
						undoSplit(node);
						// search is done since it's at a leaf
						return node;
					} else {
						node = child;
					}
				} else {
					return node;
				}
			} else {
				int index = node.getChildIndex(point);
				node = checkAddChild(node, index, info );
			}
		}
	}

	/**
	 * If all the points are identical it will recurse forever since it can't split them.
	 */
	private boolean checkPathological(Octree node) {

		boolean pathological = true;
		Point3D_F64 first = node.points.data[0].point;
		for( int i = 1; i < node.points.size; i++ ) {
			Point3D_F64 p = node.points.data[i].point;
			if( first.x != p.x || first.y != p.y || first.z != p.z ) {
				pathological = false;
				break;
			}
		}

		return pathological;
	}

	/**
	 * A node was just split then it was realized that it should not have been split.  Undoes the split
	 * and recycles the data
	 * @param node Node which needs to become a leaf again.
	 */
	private void undoSplit(Octree node) {
		for( int i = 0; i < 8; i++ ) {
			Octree o = node.children[i];
			if( o != null ) {
				// the order might be different, but the N most recent will be recycled
				storageNodes.removeTail();

				o.parent = null;
				o.points.reset();
				node.children[i] = null;
			}
		}
		storageChildren.add(node.children);
		node.children = null;
	}

	/**
	 * Checks to see if the child already exists.  If not it creates the child
	 */
	private Octree checkAddChild(Octree node, int index  , Octree.Info info ) {
		Octree child = node.children[index];
		if( child == null ) {
			child = node.children[index] = storageNodes.grow();
			child.parent = node;
			setChildSpace(node.space,node.divider,index,child.space);
			// no points to add to child since none of the previous ones belong to it
		}
		child.points.add( info );
		return child;
	}

	/**
	 * Returns an array of Octree of length 8 with null elements.
	 */
	protected Octree[] getChildrenArray() {
		if( storageChildren.isEmpty() ) {
			return new Octree[8];
		} else {
			return storageChildren.pop();
		}
	}

	/**
	 * Sets the divider to the center of space
	 */
	public static void computeDivider( Cube3D_F64 space , Point3D_F64 divider ) {

		divider.x = (space.p0.x + space.p1.x)/2.0;
		divider.y = (space.p0.y + space.p1.y)/2.0;
		divider.z = (space.p0.z + space.p1.z)/2.0;
	}

	public static void setChildSpace( Cube3D_F64 parentSpace , Point3D_F64 parentDivider , int index ,
									  Cube3D_F64 childSpace ) {

		childSpace.p0.set( parentSpace.p0 );

		// no change for index 0
		if( index == 1 ) {
			childSpace.p0.y = parentDivider.y;
		} else if( index == 2 ) {
			childSpace.p0.x = parentDivider.x;
		} else if( index == 3 ) {
			childSpace.p0.x = parentDivider.x;
			childSpace.p0.y = parentDivider.y;
		} else if( index == 4 ) {
			childSpace.p0.z = parentDivider.z;
		} else if( index == 5 ) {
			childSpace.p0.y = parentDivider.y;
			childSpace.p0.z = parentDivider.z;
		} else if( index == 6 ) {
			childSpace.p0.x = parentDivider.x;
			childSpace.p0.z = parentDivider.z;
		} else if( index == 7 ) {
			childSpace.p0.x = parentDivider.x;
			childSpace.p0.y = parentDivider.y;
			childSpace.p0.z = parentDivider.z;
		}

		childSpace.p1.x = childSpace.p0.x + parentSpace.getLengthX()/2.0;
		childSpace.p1.y = childSpace.p0.y + parentSpace.getLengthY()/2.0;
		childSpace.p1.z = childSpace.p0.z + parentSpace.getLengthZ()/2.0;

	}

	/**
	 * Returns the Octree it has constructed
	 */
	public Octree getTree() {
		return tree;
	}

	/**
	 * List of all nodes in use
	 */
	public FastQueue<Octree> getAllNodes() {
		return storageNodes;
	}

	/**
	 * List of all points and associated data passed to the tree
	 */
	public FastQueue<Octree.Info> getAllPoints() {
		return storageInfo;
	}
}
