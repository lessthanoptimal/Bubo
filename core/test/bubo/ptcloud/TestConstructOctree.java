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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestConstructOctree {

	Random rand = new Random(234);

	@Test
	public void reset() {
		// use a specific instrance for testing purposes
		ConstructOctree alg = new Dummy();
		alg.initialize(new Cube3D_F64(-100, -100, -100, 200, 200, 200));

		// randomly construct a tree with 100 points
		for( int i = 0; i < 100; i++ ) {
			alg.addPoint(new Point3D_F64(),null);
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
		ConstructOctree alg = new Dummy();
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

	@Test
	public void computeDivider() {
		Cube3D_F64 cube = new Cube3D_F64(10,20,30,60,80,100);
		Point3D_F64 p = new Point3D_F64();
		ConstructOctree.computeDivider(cube, p);

		assertEquals(10+25,p.x,1e-8);
		assertEquals(20+30,p.y,1e-8);
		assertEquals(30+35,p.z,1e-8);
	}

	@Test
	public void setChildSpace() {
		Cube3D_F64 cube = new Cube3D_F64(-50,-50,-50,50,50,50);
		Point3D_F64 divider = new Point3D_F64();

		Cube3D_F64 child = new Cube3D_F64();

		Point3D_F64 testPt = new Point3D_F64();

		for( int i = 0; i < 8; i++ ) {
			ConstructOctree.setChildSpace(cube, divider, i, child);
			assertEquals(50,child.getLengthX(),1e-8);
			assertEquals(50,child.getLengthY(),1e-8);
			assertEquals(50,child.getLengthZ(),1e-8);

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

	public static class Dummy extends ConstructOctree
	{
		Random rand = new Random(234);

		@Override
		public Octree addPoint(Point3D_F64 point, Object data) {

			Octree.Info info = storageInfo.grow();
			info.data = data;
			info.point = point;

			Octree node = tree;

			while( rand.nextDouble() > 0.7 ) {
				if( node.children == null ) {
					node.children = getChildrenArray();
				}
				node = checkAddChild(node,rand.nextInt(8),info);
			}

			return node;
		}
	}

}
