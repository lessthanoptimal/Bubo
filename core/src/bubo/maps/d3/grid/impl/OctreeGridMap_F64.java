package bubo.maps.d3.grid.impl;

import bubo.construct.ConstructOctreeLeaf_I32;
import bubo.construct.Octree_I32;
import bubo.maps.d3.grid.OccupancyGrid3D_F64;
import georegression.metric.Intersection3D_I32;
import georegression.struct.point.Point3D_I32;
import georegression.struct.shapes.Cube3D_I32;
import org.ddogleg.struct.FastQueue;

/**
 * Creates a 3D map using an Octree.  This is a sparse data structure which is in most situations much more
 * efficient at storing 3D maps than a raw 3D array.  The price paid is that reading and writing to the
 * octree is more expensive.
 *
 * @author Peter Abeles
 */
public class OctreeGridMap_F64 implements OccupancyGrid3D_F64 {

	// constructs and maintains the octree
	ConstructOctreeLeaf_I32 construct;

	// storage for map info which is placed in each leaf
	FastQueue<MapInfo> info = new FastQueue<MapInfo>(MapInfo.class,true);

	// used to temporarily store a point's value when looking things up
	Point3D_I32 temp = new Point3D_I32();

	// describes the area which the map is contained inside of
	Cube3D_I32 region;

	/**
	 * Creates a new map based on the users request.  The actual map size is adjusted to ensure
	 * that the leaf cells are the specified size

	 */
	public OctreeGridMap_F64( int lengthX , int lengthY , int lengthZ ) {
		this.region = new Cube3D_I32(0,0,0,lengthX,lengthY,lengthZ);

		construct = new ConstructOctreeLeaf_I32();
		construct.initialize(region);
	}

	// todo add blur function

	@Override
	public void set(int x, int y, int z, double value) {
		temp.set(x,y,z);

		Octree_I32 leaf = construct.addLeaf(temp);
		MapInfo info;
		if( leaf.userData == null ) {
			info = this.info.grow();
		} else {
			info = (MapInfo)leaf.userData;
		}
		info.value = value;
	}

	@Override
	public double get(int x, int y, int z) {
		temp.set(x,y,z);
		Octree_I32 node = construct.getTree().findDeepest(temp);
		if( node == null || !node.isLeaf() || !node.isSmallest())
			return 0.5;
		else
			return ((MapInfo)node.userData).value;
	}

	@Override
	public boolean isValid(double value) {
		return value >= 0 && value <= 1;
	}


	@Override
	public void clear() {
		construct.reset();
		info.reset();
	}

	@Override
	public boolean isInBounds(int x, int y, int z) {
		temp.set(x,y,z);
		return Intersection3D_I32.contained(region,temp);
	}

	@Override
	public boolean isKnown(int x, int y, int z) {
		temp.set(x,y,z);
		Octree_I32 node = construct.getTree().findDeepest(temp);
		return( node != null && node.isLeaf() && node.isSmallest() );
	}

	@Override
	public int getSizeX() {
		return region.getLengthX();
	}

	@Override
	public int getSizeY() {
		return region.getLengthY();
	}

	@Override
	public int getSizeZ() {
		return region.getLengthZ();
	}
}
