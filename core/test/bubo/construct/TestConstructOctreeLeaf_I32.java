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

import georegression.metric.Intersection3D_I32;
import georegression.struct.point.Point3D_I32;
import georegression.struct.shapes.Box3D_I32;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConstructOctreeLeaf_I32 {

	/**
	 * Add a point outside and see if it blows up
	 */
	@Test
	public void addPoint_outside() {
		ConstructOctree_I32 alg = new ConstructOctreeLeaf_I32();
		alg.initialize(new Box3D_I32(0,0,0,10,10,10));
		assertTrue(null == alg.addPoint(new Point3D_I32(100, 200, 300), null));
	}

	@Test
	public void addPoint_inside() {
		ConstructOctree_I32 alg = new ConstructOctreeLeaf_I32();
		alg.initialize(new Box3D_I32(0,0,0,10,10,10));
		Octree_I32 n = alg.addPoint(new Point3D_I32(1, 2, 3), null);
		assertEquals(n.space.p0.x,1);
		assertEquals(n.space.p0.y,2);
		assertEquals(n.space.p0.z,3);
	}

	/**
	 * Add points to a cube where each axis is a power of two.  That means it will always
	 * be evenly divisible
	 */
	@Test
	public void easy() {
		exhaustiveCheck(4,8,16);
	}

	/**
	 * The sides are no longer powers of 2
	 */
	@Test
	public void odd() {
		exhaustiveCheck(5,7,11);
		exhaustiveCheck(8,1,13);
		exhaustiveCheck(1,1,1);
	}

	private void exhaustiveCheck( int w , int h , int d) {
		ConstructOctreeLeaf_I32 alg = new ConstructOctreeLeaf_I32();

		alg.initialize(new Box3D_I32(0,0,0,w,h,d));

		// fill the whole space
		int total = 0;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				for (int z = 0; z < d; z++ ) {
					total++;
					Point3D_I32 p = new Point3D_I32(x,y,z);
					Octree_I32 found = alg.addLeaf(p);
					assertTrue(found.isSmallest());
					assertTrue(found.space.p0.isIdentical(p));
					assertEquals(total,countSmallest(alg));
				}
			}
		}

		// same point again
		Octree_I32 found = alg.addLeaf(new Point3D_I32(w/2,h/2,d/2));
		assertTrue(found.isSmallest());
		assertTrue(found.space.p0.isIdentical(new Point3D_I32(w/2,h/2,d/2)));
		assertEquals(total,countSmallest(alg));

		sanityCheck(alg);
	}

	private void sanityCheck( ConstructOctreeLeaf_I32 alg ) {
		List<Octree_I32> all = alg.getAllNodes().toList();
		for( Octree_I32 t : all ) {
			if( t.isSmallest() )
				assertTrue(t.isLeaf());
		}
	}

	private int countSmallest( ConstructOctreeLeaf_I32 alg ) {
		List<Octree_I32> all = alg.getAllNodes().toList();

		int total = 0;

		for( Octree_I32 t : all ) {
			if(t.isSmallest()) {
				total++;
			}
		}

		return total;
	}

	@Test
	public void addLeafsIntersect() {
		ConstructOctreeLeaf_I32 alg = new ConstructOctreeLeaf_I32();

		alg.initialize(new Box3D_I32(0, 0, 0, 10, 10, 10));

		List<Octree_I32> output = new ArrayList<Octree_I32>();
		Box3D_I32 target = new Box3D_I32(2,3,4,5,5,5);
		alg.addLeafsIntersect(target,output,null);

		assertEquals(3*2*1,output.size());

		for( Octree_I32 o : output ) {
			assertTrue(o.isSmallest());
			assertTrue(o.isLeaf());
			assertTrue(Intersection3D_I32.contained(target,o.space.p0));
		}
	}

	@Test
	public void findLeafsIntersect() {
		ConstructOctreeLeaf_I32 alg = new ConstructOctreeLeaf_I32();

		alg.initialize(new Box3D_I32(0, 0, 0, 10, 10, 10));

		List<Octree_I32> output = new ArrayList<Octree_I32>();
		Box3D_I32 target = new Box3D_I32(2,3,4,5,5,5);
		alg.findLeafsIntersect(target, output, null);

		assertEquals(0,output.size());
		assertEquals(1,alg.getAllNodes().size());

		alg.addLeaf(new Point3D_I32(3,3,4));
		alg.findLeafsIntersect(target, output, null);

		assertEquals(1,output.size());

		for( Octree_I32 o : output ) {
			assertTrue(o.isSmallest());
			assertTrue(o.isLeaf());
			assertTrue(Intersection3D_I32.contained(target,o.space.p0));
		}
	}
}