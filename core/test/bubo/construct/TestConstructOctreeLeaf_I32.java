package bubo.construct;

import georegression.struct.point.Point3D_I32;
import georegression.struct.shapes.Cube3D_I32;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConstructOctreeLeaf_I32 {

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

		alg.initialize(new Cube3D_I32(0,0,0,w,h,d));

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
}