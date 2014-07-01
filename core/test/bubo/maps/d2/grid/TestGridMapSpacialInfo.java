package bubo.maps.d2.grid;

import georegression.struct.point.Point2D_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestGridMapSpacialInfo {

	@Test
	public void globalToMap_mapToGlobal() {
		GridMapSpacialInfo info = new GridMapSpacialInfo(0.1,2,-3);

		Point2D_F64 world = new Point2D_F64(2.3,4.56);
		Point2D_F64 grid = new Point2D_F64();
		Point2D_F64 found = new Point2D_F64();

		info.globalToMap(world,grid);
		info.mapToGlobal(grid,found);

		assertEquals(world.x,found.x,1e-8);
		assertEquals(world.y,found.y,1e-8);
	}
}
