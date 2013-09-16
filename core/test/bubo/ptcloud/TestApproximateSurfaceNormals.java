package bubo.ptcloud;

import georegression.struct.point.Point3D_F64;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestApproximateSurfaceNormals {

	/**
	 * Give it a simple plane and see if it produces the expected results
	 */
	@Test
	public void checkUsingAPlane() {

		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		for( int i = 0; i < 50; i++ ) {
			for( int j = 0; j < 40; j++ ) {
				cloud.add( new Point3D_F64(j*0.1,i*0.1,-4));
			}
		}

		FastQueue<PointVectorNN> output = new FastQueue<PointVectorNN>(PointVectorNN.class,true);

		ApproximateSurfaceNormals alg = new ApproximateSurfaceNormals(5,1.0);

		alg.process(cloud,output);

		assertEquals(cloud.size(),output.size());

		for( int i = 0; i < cloud.size(); i++ ) {
			PointVectorNN pv = output.get(i);

			// see if the normal is valid
			assertEquals(0,pv.normal.x,1e-8);
			assertEquals(0,pv.normal.y,1e-8);
			assertEquals(1,Math.abs(pv.normal.z),1e-8);

			// see if it's neighbors are close by
			assertEquals(5,pv.neighbors.size);
			for( int j = 0; j < pv.neighbors.size; j++ ) {
				double d = pv.neighbors.get(j).p.distance(pv.p);
				assertTrue(d < 0.4);
			}
		}
	}

}
