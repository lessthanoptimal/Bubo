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

import bubo.construct.Octree_I32;
import bubo.maps.UtilMaps;
import georegression.struct.point.Point3D_I32;
import georegression.struct.shapes.Box3D_I32;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for applying 3D kernels to {@link bubo.maps.d3.grid.impl.OctreeGridMap_F64} which
 * takes advantage of the maps sparse data structure.  Any {@link bubo.maps.d3.grid.impl.Kernel3D_F64}
 * can be used.
 *
 * NOTE: If a cell is along the map's border cells outside the map will be treated as having a value
 * of unknown.  A better way would be to reweigh it based on the cells which are inside the image.
 *
 * @author Peter Abeles
 */
public class BlurOctreeGridMap_F64 {

	// internal work space
	List<Octree_I32> neighbors = new ArrayList<Octree_I32>();
	List<Octree_I32> ignore = new ArrayList<Octree_I32>();
	Box3D_I32 box = new Box3D_I32();

	/**
	 * Applies the kernel to the map while taking advantage of its sparsity.
	 *
	 * @param input (Input) Input map.  Not modified.
	 * @param kernel (Input) Blur kernel. Not modified.
	 * @param blurred (Output) Storage for blurred map
	 */
	public void apply( OctreeGridMap_F64 input , Kernel3D_F64 kernel , OctreeGridMap_F64 blurred ) {
		if( !UtilMaps.sameShape(input, blurred))
			throw new IllegalArgumentException("Maps must be the same shape!");

		blurred.clear();

		// Construct the blurred map
		createBlurredCells(input, kernel.radius, blurred);

		// for each grid cell in the blurred image perform a sparse convolve in the input image
		applyConvolution(input, kernel, blurred);
	}

	/**
	 * For each grid cell in the blurred map apply a convolution kernel across the input map at the
	 * same coordinate.  Write the results to the blurred image
	 */
	private void applyConvolution(OctreeGridMap_F64 input, Kernel3D_F64 kernel, OctreeGridMap_F64 blurred) {
		FastQueue<Octree_I32> list = blurred.getConstruct().getAllNodes();
		int kernelWidth = kernel.getWidth()-1;
		for (int i = 0; i < list.size; i++) {
			Octree_I32 o = list.get(i);
			if ( !o.isLeaf() || !o.isSmallest())
				continue;

			if( o.userData != null )
				throw new RuntimeException("BUG!  Not properly reset");

			// find all the leafs which intersect the box
			blurred.getConstruct().findLeafsIntersect(box, neighbors, ignore);

			// perform the convolution.  Assume the kernel is symmetric
			double total = 0, totalWeight = 0;
			for (int j = 0; j < neighbors.size(); j++) {
				Octree_I32 neighbor = neighbors.get(j);
				Point3D_I32 p = neighbor.space.p0;
				double weight = kernel.get(
						kernelWidth-(p.x - o.space.p0.x),
						kernelWidth-(p.y - o.space.p0.y),
						kernelWidth-(p.z - o.space.p0.z));
				double probability = ((MapLeaf)neighbor.getUserData()).probability;

				total += probability*weight;
				totalWeight += weight;
			}

			// take in account all the unknown cells surrounding it
			// NOTE: This will effectively treat cells outside the map as having a value of unknown
			int numUnknown = kernel.getTotalElements() - neighbors.size();
			total += input.getUnknownValue()*numUnknown*(1.0-totalWeight);

			MapLeaf data = blurred.info.grow();
			data.probability = total;
			o.userData = data;
		}
	}

	/**
	 * Creates grid map cells in the blurred image around map cells in the input image which
	 * have been assigned a value other than unknown
	 */
	private void createBlurredCells(OctreeGridMap_F64 input,  int radius, OctreeGridMap_F64 blurred) {
		FastQueue<Octree_I32> list = input.getConstruct().getAllNodes();

		for (int i = 0; i < list.size; i++) {
			Octree_I32 o = list.get(i);
			if (o.userData == null || !o.isLeaf() || !o.isSmallest())
				continue;

			// set the box around it
			box.p0.set(o.space.p0);
			box.p0.x -= radius;
			box.p0.y -= radius;
			box.p0.z -= radius;

			box.p1.set(o.space.p0);
			box.p1.x += radius + 1;
			box.p1.y += radius + 1;
			box.p1.z += radius + 1;

			// create grid cells in blurred image around the location of 'o'
			blurred.getConstruct().addLeafsIntersect(box, neighbors, ignore);
		}
	}
}
