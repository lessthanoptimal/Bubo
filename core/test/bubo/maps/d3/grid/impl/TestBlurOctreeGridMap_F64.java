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

import bubo.construct.Octree;
import bubo.construct.Octree_I32;
import georegression.struct.point.Point3D_I32;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestBlurOctreeGridMap_F64 {
	@Test
	public void apply() {
		Kernel3D_F64 kernel = randomKernel(1);

		BlurOctreeGridMap_F64 alg = new BlurOctreeGridMap_F64();
		OctreeGridMap_F64 input = new OctreeGridMap_F64(5,6,7);
		OctreeGridMap_F64 blurred = new OctreeGridMap_F64(5,6,7);

		input.set(2,3,1,0.5);
		input.set(1,3,1,0.6);

		// check multiple calls to see if it produces the same answer
		alg.apply(input,kernel,blurred);
		OctreeGridMap_F64 orig = (OctreeGridMap_F64)blurred.copy();
		alg.apply(input,kernel,blurred);

		for (int i = 0; i < blurred.getSizeX(); i++) {
			for (int j = 0; j < blurred.getSizeY(); j++) {
				for (int k = 0; k < blurred.getSizeZ(); k++) {
					double found = blurred.get(i,j,k);
					double expected = orig.get(i,j,k);
					assertTrue(expected==found);
				}
			}
		}
	}

	@Test
	public void createBlurredCells() {
		Kernel3D_F64 kernel = randomKernel(1);

		BlurOctreeGridMap_F64 alg = new BlurOctreeGridMap_F64();

		OctreeGridMap_F64 input = new OctreeGridMap_F64(5,6,7);
		OctreeGridMap_F64 blurred = new OctreeGridMap_F64(5,6,7);
		Octree_I32 o = input.getConstruct().addLeaf(new Point3D_I32(2,3,3));
		o.userData = new MapLeaf(0.8);
		o = input.getConstruct().addLeaf(new Point3D_I32(3,3,3));
		o.userData = new MapLeaf(0.4);
		o = input.getConstruct().addLeaf(new Point3D_I32(4,5,6));
		o.userData = new MapLeaf(0.6);

		alg.createBlurredCells(input,kernel,blurred);

		assertEquals(27+9+8,alg.convData.size);
		FastQueue<Octree_I32> nodes = blurred.getConstruct().getAllNodes();
		int count = 0;
		for (int i = 0; i < nodes.size(); i++) {
			Octree_I32 n = nodes.get(i);
			if( n.isSmallest() && n.userData != null )
				count++;
		}
		assertEquals(27+9+8,count);

	}

	@Test
	public void applyToNeighbors() {
		Kernel3D_F64 kernel = randomKernel(1);

		BlurOctreeGridMap_F64 alg = new BlurOctreeGridMap_F64();

		OctreeGridMap_F64 input = new OctreeGridMap_F64(5,6,7);
		Octree_I32 o = input.getConstruct().addLeaf(new Point3D_I32(2,3,3));
		o.userData = new MapLeaf(0.8);
		OctreeGridMap_F64 blurred = new OctreeGridMap_F64(5,6,7);

		alg.neighbors.add( blurred.getConstruct().addLeaf(new Point3D_I32( 2, 3, 3)));
		alg.neighbors.add( blurred.getConstruct().addLeaf(new Point3D_I32( 3, 3, 3)));
		alg.neighbors.add( blurred.getConstruct().addLeaf(new Point3D_I32( 3, 4, 3)));

		alg.applyToNeighbors(kernel,o);

		assertEquals(3,alg.convData.size);

		assertEquals(0.8*kernel.get(1,1,1),convTotal(blurred, 2, 3, 3),1e-8);
		assertEquals(kernel.get(1,1,1),convWeight(blurred,2,3,3),1e-8);

		assertEquals(0.8*kernel.get(2,1,1),convTotal(blurred,3,3,3),1e-8);
		assertEquals(kernel.get(2,1,1),convWeight(blurred,3,3,3),1e-8);

		assertEquals(0.8*kernel.get(2,2,1),convTotal(blurred,3,4,3),1e-8);
		assertEquals(kernel.get(2,2,1),convWeight(blurred,3,4,3),1e-8);
	}

	private static double convWeight( OctreeGridMap_F64 map , int x , int y , int z ) {
		Octree_I32 n = map.getConstruct().getTree().findDeepest(new Point3D_I32(x,y,z));
		return ((SumData)n.userData).weight;
	}
	private static double convTotal( OctreeGridMap_F64 map , int x , int y , int z ) {
		Octree_I32 n = map.getConstruct().getTree().findDeepest(new Point3D_I32(x,y,z));
		return ((SumData)n.userData).total;
	}

	@Test
	public void computeProbability() {
		OctreeGridMap_F64 map = new OctreeGridMap_F64(5,6,7);

		Octree node = map.getConstruct().addLeaf(new Point3D_I32(1,2,3));
		node.userData = new SumData(0.5625,0.75);
		node = map.getConstruct().addLeaf(new Point3D_I32(2,2,3));
		node.userData = new SumData(0.3,1);
		// node with no user data.  should be ignoredz
		map.getConstruct().addLeaf(new Point3D_I32(3,5,6));

		BlurOctreeGridMap_F64.computeProbability(map);

		// this will test to see if it swapped the user data and correctly computed
		// the probability while taking in account unknown neighbors being 0.5
		assertEquals(0.6875, map.get(1, 2, 3), 1e-8);
		assertEquals(0.3, map.get(2, 2, 3), 1e-8);
	}

	private static Kernel3D_F64 randomKernel( int radius ) {
		Random rand = new Random(234);
		Kernel3D_F64 kernel = new Kernel3D_F64(radius);
		double total = 0;
		for (int i = 0; i < kernel.data.length; i++) {
			total += kernel.data[i] = rand.nextDouble();
		}
		for (int i = 0; i < kernel.data.length; i++) {
			kernel.data[i] /= total;
		}
		return kernel;
	}
}