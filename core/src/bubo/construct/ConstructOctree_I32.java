package bubo.construct;

import georegression.struct.point.Point3D_I32;
import georegression.struct.shapes.Cube3D_I32;

/**
 * Base class for constructing {@link Octree_I32}.
 *
 * @author Peter Abeles
 */
public abstract class ConstructOctree_I32 extends ConstructOctree<Octree_I32,Point3D_I32>{

	public ConstructOctree_I32() {
		super(Octree_I32.class);
	}

	/**
	 * Initializes the Octree.  The space contained by the Octree is specified by the passed in cube.
	 * {@link #reset} is automatically called by this function
	 *
	 * @param cube Space which is contained by the Octree.
	 */
	public void initialize(Cube3D_I32 cube) {
		reset();
		tree.space.set(cube);
	}

	@Override
	public void setChildSpace(Octree_I32 parent, int index, Octree_I32 child) {
		setChildSpace(parent.space,parent.divider,index,child.space);
	}

	/**
	 * Sets the divider to the center of space
	 */
	public static void computeDivider(Cube3D_I32 space, Point3D_I32 divider) {

		divider.x = (space.p0.x + space.p1.x) / 2;
		divider.y = (space.p0.y + space.p1.y) / 2;
		divider.z = (space.p0.z + space.p1.z) / 2;
	}

	public static void setChildSpace(Cube3D_I32 parentSpace, Point3D_I32 parentDivider, int index,
									 Cube3D_I32 childSpace) {

		childSpace.p0.set(parentSpace.p0);
		childSpace.p1.set(parentSpace.p1);

		if( index == 0 ) {
			childSpace.p1.set(parentDivider);
		} else if (index == 1) {
			childSpace.p0.y = parentDivider.y;
			childSpace.p1.x = parentDivider.x;
			childSpace.p1.z = parentDivider.z;
		} else if (index == 2) {
			childSpace.p0.x = parentDivider.x;
			childSpace.p1.y = parentDivider.y;
			childSpace.p1.z = parentDivider.z;
		} else if (index == 3) {
			childSpace.p0.x = parentDivider.x;
			childSpace.p0.y = parentDivider.y;
			childSpace.p1.z = parentDivider.z;
		} else if (index == 4) {
			childSpace.p0.z = parentDivider.z;
			childSpace.p1.x = parentDivider.x;
			childSpace.p1.y = parentDivider.y;
		} else if (index == 5) {
			childSpace.p0.y = parentDivider.y;
			childSpace.p0.z = parentDivider.z;
			childSpace.p1.x = parentDivider.x;
		} else if (index == 6) {
			childSpace.p0.x = parentDivider.x;
			childSpace.p0.z = parentDivider.z;
			childSpace.p1.y = parentDivider.y;
		} else if (index == 7) {
			childSpace.p0.x = parentDivider.x;
			childSpace.p0.y = parentDivider.y;
			childSpace.p0.z = parentDivider.z;
		}
	}
}
