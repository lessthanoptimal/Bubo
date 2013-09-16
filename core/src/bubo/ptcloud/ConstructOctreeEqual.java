package bubo.ptcloud;

import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;

import java.util.List;

/**
 * Constructs quad tree one point at a time.  The divider point is always the center of the cube and the same graph
 * will be produced independent of the order in which points are added.
 *
 * @author Peter Abeles
 */
public class ConstructOctreeEqual {

	// create a new node in the graph when the number of points exceeds
	private int divideThreshold;

	// the Octree that it modifies
	private Octree tree;

	public ConstructOctreeEqual(int divideThreshold) {
		this.divideThreshold = divideThreshold;
	}

	/**
	 * Sets the Octree data structure it should use
	 *
	 * @param tree data structure
	 */
	public void setTree(Octree tree) {
		this.tree = tree;
	}

	/**
	 * Adds all points to the Octree
	 *
	 * @param points List of points to add
	 */
	public void addPoints( List<Point3D_F64> points ) {
		int N = points.size();
		for( int i = 0; i < N; i++ ) {
			addPoint(points.get(i));
		}
	}

	/**
	 * Adds a point to the Octree.  If needed it will grow the tree
	 *
	 * @param point The point which is to be added
	 */
	public void addPoint( Point3D_F64 point ) {
		Octree node = tree;

		while( node != null ) {
			node.points.add( point );

			if( node.isLeaf() ) {
				// see if it needs to create a new node
				if( node.points.size() > divideThreshold ) {
					node.children = new Octree[8];
					computeDivider(node.space,node.divider);

					// create a new child for point to go into
					int index = node.getChildIndex(point);
					Octree child = checkAddChild(node,index);

					// Create new children where appropriate for all points in node, but 'point'
					for( int i = 0; i < node.points.size-1; i++ ) {
						Point3D_F64 p = node.points.get(i);
						int indexP = node.getChildIndex(p);

						// see if the node exists
						Octree childP = checkAddChild(node, indexP);
						// add the point
						childP.points.add(p);
					}
					node = child;
				} else {
					node = null;
				}
			} else {
				int index = node.getChildIndex(point);
				node = checkAddChild(node, index);
			}
		}
	}

	/**
	 * Checks to see if the child already exists.  If not it creates the child
	 */
	private Octree checkAddChild(Octree node, int index) {
		Octree child = node.children[index];
		if( child == null ) {
			child = node.children[index] = new Octree();
			setChildSpace(node.space,node.divider,index,child.space);
			// no points to add to child since none of the previous ones belong to it
		}
		return child;
	}

	/**
	 * Sets the divider to the center of space
	 */
	public static void computeDivider( Cube3D_F64 space , Point3D_F64 divider ) {
		divider.x = space.p.x + space.lengthX/2.0;
		divider.y = space.p.y + space.lengthY/2.0;
		divider.z = space.p.z + space.lengthZ/2.0;
	}

	public static void setChildSpace( Cube3D_F64 parentSpace , Point3D_F64 parentDivider , int index ,
									  Cube3D_F64 childSpace ) {

		childSpace.p.set( parentSpace.p );
		childSpace.lengthX = parentSpace.lengthX/2.0;
		childSpace.lengthY = parentSpace.lengthY/2.0;
		childSpace.lengthZ = parentSpace.lengthZ/2.0;

		// no change for index 0
		if( index == 1 ) {
			childSpace.p.y = parentDivider.y;
		} else if( index == 2 ) {
			childSpace.p.x = parentDivider.x;
		} else if( index == 3 ) {
			childSpace.p.x = parentDivider.x;
			childSpace.p.y = parentDivider.y;
		} else if( index == 4 ) {
			childSpace.p.z = parentDivider.z;
		} else if( index == 5 ) {
			childSpace.p.y = parentDivider.y;
			childSpace.p.z = parentDivider.z;
		} else if( index == 6 ) {
			childSpace.p.x = parentDivider.x;
			childSpace.p.z = parentDivider.z;
		} else if( index == 7 ) {
			childSpace.p.x = parentDivider.x;
			childSpace.p.y = parentDivider.y;
			childSpace.p.z = parentDivider.z;
		}
	}
}
