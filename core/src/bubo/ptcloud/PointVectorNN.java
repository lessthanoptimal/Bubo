package bubo.ptcloud;

import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import org.ddogleg.struct.FastQueue;

/**
 * A point, normal vector tangent to the surface, and its neighbors.
 *
 * @author Peter Abeles
 */
public class PointVectorNN {
	/** Reference to the point in the point cloud */
	public Point3D_F64 p;
	/** Normal to the surface at p */
	public Vector3D_F64 normal = new Vector3D_F64();

	/** Points which are its neighbors */
	public FastQueue<PointVectorNN> neighbors = new FastQueue<PointVectorNN>(PointVectorNN.class,false);
}
