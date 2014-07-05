package bubo.construct;

import georegression.struct.point.Point3D_I32;

/**
 * Constructs an octree by adding points to the smallest possible leaf which contains
 * the point.  The smallest possible leaf is 1x1x1 wide and will thus have the same coordinate
 * as the point which is added to it.
 *
 * When a new point is added it is only added to the leaf node and not any of the nodes on the way.
 *
 * @author Peter Abeles
 */
public class ConstructOctreeLeaf_I32 extends ConstructOctree_I32 {

	/**
	 * Adds the point to the smallest leaf at this point.
	 *
	 * NOTE: This function is of dubious value since all points at the leaf will have
	 * the same coordinate so in many situations there is no point in keeping a list.
	 * Instead the leaf itself could be used.  See {@link #addLeaf(georegression.struct.point.Point3D_I32)}.
	 */
	@Override
	public Octree_I32 addPoint(Point3D_I32 point, Object data) {

		// declare the structure which stores the point and data
		Octree.Info<Point3D_I32> info = storageInfo.grow();
		info.point = point;
		info.data = data;

		Octree_I32 leaf = addLeaf(point);
		leaf.points.add(info);

		return leaf;
	}

	/**
	 * Finds the leaf at this point.  If none exist then a new one is created.  New nodes in
	 * the graph are created as it searches.  A reference to the leaf is returned.
	 * @param point Point being searched for.
	 * @return Leaf at that point.
	 */
	public Octree_I32 addLeaf( Point3D_I32 point ) {
		if( !tree.contained(point) )
			return null;

		Octree_I32 node = tree;

		while( true ) {
			if( node.isSmallest() ) {
				return node;
			} else {
				if( node.isLeaf() ) {
					node.children = getChildrenArray();
					computeDivider(node.space, node.divider);
				}
				// Traverse down to the next child
				int index = node.getChildIndex(point);
				node = checkAddChild(node, index);
			}
		}
	}
}
