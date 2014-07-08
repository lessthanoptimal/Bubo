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

import bubo.construct.ConstructOctreeLeaf_I32;
import bubo.construct.OctreeOps;
import bubo.construct.Octree_I32;
import bubo.maps.d3.grid.CellProbability_F64;
import bubo.maps.d3.grid.OccupancyGrid3D_F64;
import georegression.metric.Intersection3D_I32;
import georegression.struct.point.Point3D_I32;
import georegression.struct.shapes.Box3D_I32;
import org.ddogleg.struct.FastQueue;

import java.util.Iterator;
import java.util.List;

/**
 * Creates a 3D map using an Octree.  This is a sparse data structure which is in most situations much more
 * efficient at storing 3D maps than a raw 3D array.  The price paid is that reading and writing to the
 * octree is more expensive.
 *
 * @author Peter Abeles
 */
public class OctreeGridMap_F64 implements OccupancyGrid3D_F64 {

	// value of cells with no information
	double unknownValue = 0.5;

	// constructs and maintains the octree
	ConstructOctreeLeaf_I32 construct;

	// storage for map info which is placed in each leaf
	FastQueue<MapInfo> info = new FastQueue<MapInfo>(MapInfo.class,true);

	// used to temporarily store a point's value when looking things up
	Point3D_I32 temp = new Point3D_I32();

	// describes the area which the map is contained inside of
	Box3D_I32 region;

	/**
	 * Creates a new map based on the users request.  The actual map size is adjusted to ensure
	 * that the leaf cells are the specified size
	 *
	 * @param lengthX Number of map cells along x-axis
	 * @param lengthY Number of map cells along y-axis
	 * @param lengthZ Number of map cells along z-axis
	 */
	public OctreeGridMap_F64( int lengthX , int lengthY , int lengthZ ) {
		this.region = new Box3D_I32(0,0,0,lengthX,lengthY,lengthZ);

		construct = new ConstructOctreeLeaf_I32();
		construct.initialize(region);
	}

	/**
	 * Applies the kernel to the map while taking advantage of its sparsity.
	 * @param kernel
	 * @param blurred
	 */
	public void blur( Kernel3D_F64 kernel , OctreeGridMap_F64 blurred ) {
		// TODO make sure it's the same size
		blurred.clear();

		// find all cells in this map

		// for each leaf find all the surrounding leafs in the blurred map

		// add the blurred value and weight to each cell

		// go through all leafs in the blurred map and normalize cell values

	}

	@Override
	public void set(int x, int y, int z, double value) {
		temp.set(x,y,z);

		Octree_I32 leaf = construct.addLeaf(temp);
		MapInfo info;
		if( leaf.userData == null ) {
			info = this.info.grow();
			leaf.userData = info;
		} else {
			info = (MapInfo)leaf.userData;
		}
		info.value = value;
	}

	@Override
	public double get(int x, int y, int z) {
		temp.set(x,y,z);
		Octree_I32 node = construct.getTree().findDeepest(temp);
		if( node == null || !node.isLeaf() || !node.isSmallest())
			return unknownValue;
		else
			return ((MapInfo)node.userData).value;
	}

	@Override
	public boolean isValid(double value) {
		return value >= 0 && value <= 1;
	}

	@Override
	public Iterator<CellProbability_F64> iteratorKnown() {
		return new OctIterator();
	}

	@Override
	public void clear() {
		construct.reset();
		info.reset();
	}

	@Override
	public boolean isInBounds(int x, int y, int z) {
		temp.set(x,y,z);
		return Intersection3D_I32.contained(region,temp);
	}

	@Override
	public boolean isKnown(int x, int y, int z) {
		temp.set(x,y,z);
		Octree_I32 node = construct.getTree().findDeepest(temp);
		return( node != null && node.isLeaf() && node.isSmallest() );
	}

	@Override
	public int getSizeX() {
		return region.getLengthX();
	}

	@Override
	public int getSizeY() {
		return region.getLengthY();
	}

	@Override
	public int getSizeZ() {
		return region.getLengthZ();
	}

	public ConstructOctreeLeaf_I32 getConstruct() {
		return construct;
	}

	/**
	 * Returns all grid cells as {@link bubo.construct.Octree_I32} nodes.
	 * @return List of all occupied cells
	 */
	public List<Octree_I32> getGridCells() {
		return OctreeOps.findAllSmallest(construct.getAllNodes().toList(),null);
	}

	private class OctIterator implements Iterator<CellProbability_F64> {

		FastQueue<Octree_I32> nodes = construct.getAllNodes();
		int index;

		Octree_I32 next;
		CellProbability_F64 storage = new CellProbability_F64();

		public OctIterator() {
			searchNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public CellProbability_F64 next() {
			Octree_I32 prev = next;
			searchNext();
			MapInfo info = prev.getUserData();
			storage.set( prev.space.p0 );
			storage.probability = info.value;

			return storage;
		}

		protected void searchNext() {
			next = null;
			while( index < nodes.size() ) {
				Octree_I32 o = nodes.get(index++);
				if( o.isSmallest() ) {
					MapInfo info = o.getUserData();
					if (info != null && info.value != 0.5f) {
						next = o;
						break;
					}
				}
			}
		}

		@Override
		public void remove() {
			throw new RuntimeException("Remove is not supported");
		}
	}
}
