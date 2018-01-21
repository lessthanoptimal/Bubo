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

package bubo.clouds.detect.alg;

import georegression.geometry.ConvertRotation3D_F64;
import georegression.geometry.GeometryMath_F64;
import georegression.geometry.UtilPlane3D_F64;
import georegression.metric.Distance3D_F64;
import georegression.struct.EulerType;
import georegression.struct.line.LineParametric3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import georegression.transform.se.SePointOps_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestBoundPlaneRectangle {

	@Test
	public void basicTest() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);


		// add points which form a nice rectangle
		List<Point3D_F64> pts = new ArrayList<Point3D_F64>();
		pts.add(new Point3D_F64(-1, 2, 0));
		pts.add(new Point3D_F64(0, 2, 0));
		pts.add(new Point3D_F64(1, 2, 0));
		pts.add(new Point3D_F64(-1, -3, 0));
		pts.add(new Point3D_F64(0, -3, 0));
		pts.add(new Point3D_F64(1, -3, 0));

		// put the points in some arbitrary orientation to make it harder to process
		Se3_F64 se = new Se3_F64();
		ConvertRotation3D_F64.eulerToMatrix(EulerType.XYZ,0.1, 0.8, -0.5, se.getR());
		se.T.set(-0.5, 1, 2);

		for (Point3D_F64 p : pts) {
			SePointOps_F64.transform(se, p, p);
		}

		// apply the same transform to the plane
		SePointOps_F64.transform(se, plane.p, plane.p);
		GeometryMath_F64.mult(se.getR(), plane.n, plane.n);
		PlaneGeneral3D_F64 general = UtilPlane3D_F64.convert(plane, null);

		BoundPlaneRectangle alg = new BoundPlaneRectangle();
		alg.process(general, pts);

		Point3D_F64 rect[] = alg.getRect();

		// check angles between the points
		checkAngle(rect[0], rect[1], rect[2]);
		checkAngle(rect[1], rect[2], rect[3]);
		checkAngle(rect[2], rect[3], rect[0]);
		checkAngle(rect[3], rect[0], rect[1]);

		// make sure the points are on the plane
		for (Point3D_F64 p : rect) {
			assertEquals(0, Distance3D_F64.distance(general, p), 1e-8);
		}

		// check to see if the rectangle is the expected size
		double length0 = rect[0].distance(rect[1]);
		double length1 = rect[0].distance(rect[3]);

		double width = Math.min(length0, length1);
		double height = Math.max(length0, length1);

		assertEquals(2, width, 1e-8);
		assertEquals(5, height, 1e-8);

		// see if the points are inside the rectangle.  the sum of the distance between the two lines should
		// add up to the width or heights
		LineParametric3D_F64 line0 = createLine(rect[0], rect[1]);
		LineParametric3D_F64 line1 = createLine(rect[1], rect[2]);
		LineParametric3D_F64 line2 = createLine(rect[2], rect[3]);
		LineParametric3D_F64 line3 = createLine(rect[3], rect[0]);

		for (Point3D_F64 p : pts) {
			double dist0 = Distance3D_F64.distance(line0, p);
			double dist1 = Distance3D_F64.distance(line1, p);
			double dist2 = Distance3D_F64.distance(line2, p);
			double dist3 = Distance3D_F64.distance(line3, p);

			double w = dist0 + dist2;
			double h = dist1 + dist3;

			assertTrue(Math.abs(w - width) < 1e-6 || Math.abs(w - height) < 1e-6);
			assertTrue(Math.abs(h - width) < 1e-6 || Math.abs(h - height) < 1e-6);
		}

	}

	private LineParametric3D_F64 createLine(Point3D_F64 a, Point3D_F64 b) {
		LineParametric3D_F64 ret = new LineParametric3D_F64();
		ret.p.set(a);
		ret.slope.set(b.x - a.x, b.y - a.y, b.z - a.z);

		return ret;
	}

	private void checkAngle(Point3D_F64 a, Point3D_F64 b, Point3D_F64 c) {

		double x0 = b.x - a.x;
		double y0 = b.y - a.y;
		double z0 = b.z - a.z;

		double x1 = c.x - b.x;
		double y1 = c.y - b.y;
		double z1 = c.z - b.z;

		double r0 = Math.sqrt(x0 * x0 + y0 * y0 + z0 * z0);
		double r1 = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);

		double dot = x0 * x1 + y0 * y1 + z0 * z1;
		dot /= r0 * r1;
		double angle = Math.acos(dot);
		assertEquals(Math.PI / 2.0, angle, 1e-8);
	}

}
