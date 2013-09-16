package bubo.ptcloud;

import georegression.metric.Intersection3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import georegression.struct.shapes.RectangleCorner2D_F64;
import org.ddogleg.struct.FastQueue;

import java.util.List;

/**
 * Octree data structure which uses a point to define sub-structure inside.  Each node in the tree contains 8 children.
 * Which child a point belongs to is defined by the 'space' of the node and the 'divider' point, see below. If
 * a node is a leaf then the 'children' data structure will be null.
 *
 * <p>
 * <ul>
 *   <li>children[0] = p.x <  divider.x and p.y <  divider.y and p.z <  divider.z</li>
 *   <li>children[1] = p.x <  divider.x and p.y >= divider.y and p.z <  divider.z</li>
 *   <li>children[2] = p.x >= divider.x and p.y <  divider.y and p.z <  divider.z</li>
 *   <li>children[3] = p.x >= divider.x and p.y >= divider.y and p.z <  divider.z</li>
 *   <li>children[4] = p.x <  divider.x and p.y <  divider.y and p.z >= divider.z</li>
 *   <li>children[5] = p.x <  divider.x and p.y >= divider.y and p.z >= divider.z</li>
 *   <li>children[6] = p.x >= divider.x and p.y <  divider.y and p.z >= divider.z</li>
 *   <li>children[7] = p.x >= divider.x and p.y >= divider.y and p.z >= divider.z</li>
 * </ul>
 * </p>
 *
 * @author Peter Abeles
 */
public class Octree {
	/**
	 * Defines the space in which this node is contained
	 */
	public Cube3D_F64 space = new Cube3D_F64();
	/**
	 * The control point used to segment the space out into 8 children.  This is commonly the center
	 * of the cube.
	 */
	public Point3D_F64 divider = new Point3D_F64();

	/**
	 * Children of the node.  If a leaf then the children will be null.
	 */
	public Octree children[];

	/**
	 * Points contained inside this node.  Depending on how it was constructed, all the points might be
	 * contained in the leafs or not.  New points are not declared by the FastQueue, just the storage array
	 */
	public FastQueue<Point3D_F64> points = new FastQueue<Point3D_F64>(Point3D_F64.class,false);

	/**
	 * Returns true if it is a leaf node or false if it is not
	 * @return true for leaf node and false if not.
	 */
	public boolean isLeaf() {
		return children == null;
	}

	/**
	 * Finds all the nodes in the Octree which contain the point.  The search stops when it hits a leaf.
	 *
	 * @param point (Input) Point which is being searched for
	 * @param path (Output) All the nodes which contain point.  Order will be from general to specific.
	 */
	public void findPathToPoint( Point3D_F64 point , List<Octree> path ) {
		Octree node = this;

		// see if it is inside this space
		if( !Intersection3D_F64.contained(space,point) )
			return;

		while( node != null ) {
			path.add(node);

			if( node.isLeaf() )
				break;

			node = node.children[ node.getChildIndex(point) ];
		}
	}

	/**
	 * Given a point inside the cube, return which child it belongs in.
	 *
	 * @param point A Point in space
	 * @return index of the child which contains it.
	 */
	public int getChildIndex( Point3D_F64 point ) {
		int quad;

		if( point.x < divider.x ) {
			if( point.y < divider.y ) {
				quad = 0;
			} else {
				quad = 1;
			}
		} else {
			if( point.y < divider.y ) {
				quad = 2;
			} else {
				quad = 3;
			}
		}
		if( point.z >= divider.z ) {
			quad += 4;
		}

		return quad;
	}
}
