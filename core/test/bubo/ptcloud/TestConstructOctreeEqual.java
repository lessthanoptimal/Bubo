package bubo.ptcloud;

import georegression.metric.Intersection3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestConstructOctreeEqual {

	@Test
	public void addPoint_singleNode() {
		Octree tree = new Octree();
		tree.space = new Cube3D_F64(-50,-50,-50,100,100,100);
		tree.divider = new Point3D_F64();

		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);
		alg.setTree(tree);

		alg.addPoint(new Point3D_F64(1,1,1));

		assertEquals(1, tree.points.size());
	}

	@Test
	public void addPoint_multipleNodes() {
		Point3D_F64 p = new Point3D_F64(1,1,1);

		Octree tree = new Octree();
		tree.space = new Cube3D_F64(-50,-50,-50,100,100,100);
		tree.divider = new Point3D_F64();
		tree.children = new Octree[8];

		Octree node = new Octree();
		tree.children[ tree.getChildIndex(p)] = node;

		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);
		alg.setTree(tree);

		alg.addPoint(p);

		assertEquals(1, tree.points.size());
		assertEquals(1, node.points.size());
	}

	@Test
	public void addPoint_createNode() {
		Octree tree = new Octree();
		tree.space = new Cube3D_F64(-50,-50,-50,100,100,100);
		tree.divider = new Point3D_F64();

		ConstructOctreeEqual alg = new ConstructOctreeEqual(10);
		alg.setTree(tree);

		for( int i = 0; i < 9; i++ ) {
			alg.addPoint(new Point3D_F64(1,1,1));
			alg.addPoint(new Point3D_F64(-1,-1,-1));
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
