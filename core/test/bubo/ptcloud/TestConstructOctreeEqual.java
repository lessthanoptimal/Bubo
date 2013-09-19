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

import georegression.metric.Intersection3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestConstructOctreeEqual {

	Random rand = new Random(234);

	@Test
	public void reset() {
		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);
		alg.initialize(new Cube3D_F64(-100,-100,-100,200,200,200));

		for( int i = 0; i < 100; i++ ) {
			Point3D_F64 a = new Point3D_F64();
			a.x = rand.nextGaussian()*10;
			a.y = rand.nextGaussian()*10;
			a.z = rand.nextGaussian()*10;

			alg.addPoint(a,null);
		}

		// see if it declared the expected amount of data
		int expectedNode = countNodes(alg.getTree(),0);
		int foundNodes = alg.storageNodes.size;

		assertEquals(expectedNode,foundNodes);
		assertEquals(100,alg.storageInfo.size());

		int numLeafs = countLeafs(alg.getTree(),0);

		// now reset
		alg.reset();

		// these should have a size of zero
		assertEquals(1,alg.storageNodes.size); // root is 1
		assertEquals(0,alg.storageInfo.size);
		// this should have all the children arrays added to it
		assertEquals(expectedNode-numLeafs,alg.storageChildren.size());

		// check the structures to see that they have been reset correctly
		for( Octree[] children : alg.storageChildren ) {
			assertEquals(8,children.length);
			for( int i = 0; i < children.length; i++ ) {
				assertTrue(children[i]==null);
			}
		}
		for( Octree n : alg.storageNodes.data ) {
			assertTrue(n.parent==null);
			assertTrue(n.children==null);
			assertTrue(n.points.size()==0);
		}
		for( Octree.Info n : alg.storageInfo.data ) {
			assertTrue(n.point==null);
			assertTrue(n.data==null);
		}

	}

	private static int countNodes( Octree node , int total ) {

		total++;
		if( node.isLeaf() )
			return total;

		for( int i = 0; i < node.children.length; i++ ) {
			if( node.children[i] == null )
				continue;
			total = countNodes(node.children[i],total);
		}
		return total;
	}

	private static int countLeafs( Octree node , int total ) {

		if( node.isLeaf() )
			return total+1;

		for( int i = 0; i < node.children.length; i++ ) {
			if( node.children[i] == null )
				continue;
			total = countLeafs(node.children[i],total);
		}
		return total;
	}

	/**
	 * Make sure the Octree.parent is correctly assigned
	 */
	@Test
	public void checkParent() {
		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);
		alg.initialize(new Cube3D_F64(-100,-100,-100,200,200,200));

		for( int i = 0; i < 100; i++ ) {
			Point3D_F64 a = new Point3D_F64();
			a.x = rand.nextGaussian()*10;
			a.y = rand.nextGaussian()*10;
			a.z = rand.nextGaussian()*10;

			alg.addPoint(a,null);
		}

		int numNull = 0;
		for( int index = 0; index <  alg.storageNodes.size; index++ ) {
			Octree n = alg.storageNodes.data[index];
			Octree p = n.parent;
			if( p == null ) {
				numNull++;
			} else {
				boolean match = false;
				for( int i = 0; i < p.children.length; i++ ) {
					if( p.children[i] == n ) {
						match = true;
						break;
					}
				}
				assertTrue(match);
			}
		}
		// only the root should be null
		assertEquals(1,numNull);
	}

	/**
	 * Makes sure the point and data are correctly associated to each other
	 */
	@Test
	public void addPoint_data() {
		Point3D_F64 p = new Point3D_F64(1,2,3);
		Integer d = 1;

		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);
		alg.addPoint(p,d);

		Octree.Info a = alg.getTree().points.get(0);
		assertTrue(a.point==p);
		assertTrue(a.data == d );
	}

	@Test
	public void addPoint_singleNode() {
		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);
		Octree tree = alg.getTree();
		tree.space = new Cube3D_F64(-50,-50,-50,100,100,100);
		tree.divider = new Point3D_F64();


		alg.addPoint(new Point3D_F64(1,1,1),null);

		assertEquals(1, tree.points.size());
	}

	@Test
	public void addPoint_multipleNodes() {
		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);

		Point3D_F64 p = new Point3D_F64(1,1,1);

		Octree tree = alg.getTree();
		tree.space = new Cube3D_F64(-50,-50,-50,100,100,100);
		tree.divider = new Point3D_F64();
		tree.children = new Octree[8];

		Octree node = new Octree();
		tree.children[ tree.getChildIndex(p)] = node;

		alg.addPoint(p,null);

		assertEquals(1, tree.points.size());
		assertEquals(1, node.points.size());
	}

	@Test
	public void addPoint_createNode() {
		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);

		Octree tree = alg.getTree();
		tree.space = new Cube3D_F64(-50,-50,-50,100,100,100);
		tree.divider = new Point3D_F64();

		for( int i = 0; i < 9; i++ ) {
			alg.addPoint(new Point3D_F64(1,1,1),null);
			alg.addPoint(new Point3D_F64(-1,-1,-1),null);
		}

		assertEquals(18, tree.points.size());
		assertFalse(tree.isLeaf());

		Octree node0 = tree.children[ tree.getChildIndex(new Point3D_F64(1,1,1))];
		Octree node1 = tree.children[ tree.getChildIndex(new Point3D_F64(-1,-1,-1))];

		assertTrue(node0 != node1 );

		assertEquals(9, node0.points.size());
		assertFalse(tree.isLeaf());
		assertEquals(9, node1.points.size());
		assertFalse(tree.isLeaf());

	}

	@Test
	public void computeDivider() {
		Cube3D_F64 cube = new Cube3D_F64(10,20,30,50,60,70);
		Point3D_F64 p = new Point3D_F64();
		ConstructOctreeEqual.computeDivider(cube,p);

		assertEquals(10+25,p.x,1e-8);
		assertEquals(20+30,p.y,1e-8);
		assertEquals(30+35,p.z,1e-8);
	}

	@Test
	public void setChildSpace() {
		Cube3D_F64 cube = new Cube3D_F64(-50,-50,-50,100,100,100);
		Point3D_F64 divider = new Point3D_F64();

		Cube3D_F64 child = new Cube3D_F64();

		Point3D_F64 testPt = new Point3D_F64();

		for( int i = 0; i < 8; i++ ) {
			ConstructOctreeEqual.setChildSpace(cube,divider,i,child);
			assertEquals(50,child.lengthX,1e-8);
			assertEquals(50,child.lengthY,1e-8);
			assertEquals(50,child.lengthZ,1e-8);

			if( i == 0 ) {
				testPt.set(-1,-1,-1);
			} else if( i == 1 ) {
				testPt.set(-1, 1,-1);
			} else if( i == 2 ) {
				testPt.set( 1,-1,-1);
			} else if( i == 3 ) {
				testPt.set( 1, 1,-1);
			} else if( i == 4 ) {
				testPt.set(-1,-1, 1);
			} else if( i == 5 ) {
				testPt.set(-1, 1, 1);
			} else if( i == 6 ) {
				testPt.set( 1,-1, 1);
			} else if( i == 7 ) {
				testPt.set( 1, 1, 1);
			}

			assertTrue(Intersection3D_F64.contained(child,testPt));
		}
	}

}
