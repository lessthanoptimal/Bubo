package bubo.ptcloud;

import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestOctree {

	@Test
	public void findPathToPoint() {

		Point3D_F64 target = new Point3D_F64(21,22,27);

		Octree tree = new Octree();
		tree.divider = new Point3D_F64(20,30,40);
		tree.space = new Cube3D_F64(-100,-100,-100,200,200,200);

		Octree node0 = new Octree();
		tree.divider = new Point3D_F64(5,-10,50);

		Octree node1 = new Octree();

		tree.children = new Octree[8];
		node0.children = new Octree[8];

		tree.children[tree.getChildIndex(target)] = node0;
		node0.children[node0.getChildIndex(target)] = node1;

		List<Octree> found = new ArrayList<Octree>();
		tree.findPathToPoint(target,found);

		assertEquals(3,found.size());
		assertTrue(tree == found.get(0));
		assertTrue(node0 == found.get(1));
		assertTrue(node1==found.get(2));
	}

	@Test
	public void getChildIndex() {
		Octree tree = new Octree();
		tree.divider = new Point3D_F64(20,30,40);

		// check easy cases
		assertEquals(0,tree.getChildIndex(new Point3D_F64(18,28,38)));
		assertEquals(1,tree.getChildIndex(new Point3D_F64(18,32,38)));
		assertEquals(2,tree.getChildIndex(new Point3D_F64(22,28,38)));
		assertEquals(3,tree.getChildIndex(new Point3D_F64(22,32,38)));
		assertEquals(4,tree.getChildIndex(new Point3D_F64(18,28,42)));
		assertEquals(5,tree.getChildIndex(new Point3D_F64(18,32,42)));
		assertEquals(6,tree.getChildIndex(new Point3D_F64(22,28,42)));
		assertEquals(7,tree.getChildIndex(new Point3D_F64(22,32,42)));

		// check edge cases
		assertEquals(7,tree.getChildIndex(new Point3D_F64(20,30,40)));

		assertEquals(2,tree.getChildIndex(new Point3D_F64(20,28,38)));
		assertEquals(1,tree.getChildIndex(new Point3D_F64(18,30,38)));
		assertEquals(4,tree.getChildIndex(new Point3D_F64(18,28,40)));
	}

}
