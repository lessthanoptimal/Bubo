/*
 * Copyright (c) 2013-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Project BUBO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bubo.simulation.d3.sensors;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.desc.sensors.lrf3d.SpinningLrf2dMeasurement;
import bubo.desc.sensors.lrf3d.SpinningLrf2dParam;
import bubo.maps.d3.triangles.Triangle3dMap;
import georegression.struct.line.LineParametric3D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import georegression.struct.shapes.Triangle3D_F64;
import georegression.transform.se.SePointOps_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestRaytraceSpinningLrf2D {
	/**
	 * Checks to see if the return scans all hit a square block.  The sensor is not rotating and world coordinate
	 * is static at origin.
	 */
	@Test
	public void hitInsideBlock_NoRotation() {
		Triangle3dMap map = new Triangle3dMap();

		// create a square
		double z = 1;
		map.triangles.add( new Triangle3D_F64(0,2,z, 0,0,z, 2,0,z));
		map.triangles.add( new Triangle3D_F64(0,2,z, 2,2,z, 2,0,z));

		Lrf2dParam param2d = new Lrf2dParam();
		param2d.setStartAngle(-Math.PI / 2.0);
		param2d.setSweepAngle(Math.PI);
		param2d.setNumberOfScans(200);
		param2d.setMaxRange(5);

		SpinningLrf2dParam paramSpin = new SpinningLrf2dParam(param2d,0.1);

		RaytraceSpinningLrf2D alg = new RaytraceSpinningLrf2D(paramSpin);
		alg.setMap(map);

		SpinningLrf2dMeasurement meas = new SpinningLrf2dMeasurement(200,0,0);
		alg.process(null,null,meas);

		int numberValid = 0;
		for (int i = 0; i < param2d.getNumberOfScans(); i++) {

			if( paramSpin.param2d.isValidRange(meas.meas[i])) {
				numberValid++;

				Point3D_F64 expected = project(paramSpin, i, meas.meas[i]);

				assertEquals(z, expected.z, 1e-8);
				assertTrue(expected.x >= 0 && expected.x <= 2);
				assertTrue(expected.y >= 0 && expected.y <= 2);
			}
		}

		assertTrue(numberValid > param2d.getNumberOfScans()/8);
	}

	@Test
	public void findIntersection() {
		Triangle3dMap map = new Triangle3dMap();

		map.triangles.add( new Triangle3D_F64(-1,-1,0, 0,2,0, 4,-1,0));
		map.triangles.add( new Triangle3D_F64(-1,-1,-3, 0,2,-3, 4,-1,-3));

		Lrf2dParam param2d = new Lrf2dParam();
		param2d.setMaxRange(4);
		RaytraceSpinningLrf2D alg = new RaytraceSpinningLrf2D(new SpinningLrf2dParam(param2d,0.1));
		alg.setMap(map);

		assertEquals(1, alg.findIntersection(new LineParametric3D_F64(0, 0, -1, 0, 0, 1)), 1e-8);
		assertEquals(2, alg.findIntersection(new LineParametric3D_F64(0, 0, -1, 0, 0, -1)), 1e-8);
		assertTrue(Double.MAX_VALUE == alg.findIntersection(new LineParametric3D_F64(0, 0, -1, 0, 1, 0)));
	}

	private static Point3D_F64 project( SpinningLrf2dParam param , int index , double range ) {
		Se3_F64 lrfToArm = param.getLrfToArm();
		Se3_F64 armToBase = new Se3_F64();
		armToBase.getT().set(param.radius,0,0);

		Point2D_F64 p2 = new Point2D_F64();
		param.getParam2d().computeLocation(index,range,p2);

		Point3D_F64 p3 = new Point3D_F64();
		p3.set(-p2.y , 0 , p2.x );

		SePointOps_F64.transform(lrfToArm,p3,p3);
		SePointOps_F64.transform(armToBase,p3,p3);

		return p3;
	}
}