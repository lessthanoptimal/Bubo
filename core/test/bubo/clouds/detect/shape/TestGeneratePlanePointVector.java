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

package bubo.clouds.detect.shape;

import bubo.clouds.detect.alg.PointVectorNN;
import georegression.geometry.ConvertRotation3D_F64;
import georegression.geometry.GeometryMath_F64;
import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.so.Rodrigues_F64;
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestGeneratePlanePointVector {

	public static void checkPlanes(PlaneNormal3D_F64 expected, PlaneGeneral3D_F64 found, double tol) {
		PlaneGeneral3D_F64 expectedG = new PlaneGeneral3D_F64();
		UtilPlane3D_F64.convert(expected, expectedG);

		double normE = Math.sqrt(expectedG.A * expectedG.A + expectedG.B * expectedG.B +
				expectedG.C * expectedG.C + expectedG.D * expectedG.D);

		double normF = Math.sqrt(found.A * found.A + found.B * found.B +
				found.C * found.C + found.D * found.D);

		// should be sign invariant too
		double sumG = expectedG.A + expectedG.B + expectedG.C + expectedG.D;
		double sumF = found.A + found.B + found.C + found.D;

		if (Math.signum(sumG) != Math.signum(sumF))
			normF = -normF;

		assertEquals(expectedG.A / normE, found.A / normF, tol);
		assertEquals(expectedG.B / normE, found.B / normF, tol);
		assertEquals(expectedG.C / normE, found.C / normF, tol);
		assertEquals(expectedG.D / normE, found.D / normF, tol);
	}

	public static PointVectorNN createPt(PlaneNormal3D_F64 plane, double x, double y, double sign) {
		PointVectorNN pv = new PointVectorNN();
		pv.p = new Point3D_F64();
		pv.normal.set(0, 0, sign);
		pv.p.set(x, y, 0);

		Vector3D_F64 cross = pv.normal.cross(plane.n);
		if (Math.abs(cross.norm()) < 1e-8) {
			cross.set(0, 0, 1);
		} else {
			cross.normalize();
		}

		double angle = pv.normal.dot(plane.n);
		angle = Math.acos(angle / (plane.n.norm()));

		Rodrigues_F64 rod = new Rodrigues_F64(angle, cross);
		DMatrixRMaj R = ConvertRotation3D_F64.rodriguesToMatrix(rod, null);

		GeometryMath_F64.mult(R, pv.p, pv.p);
		pv.p.x += plane.p.x;
		pv.p.y += plane.p.y;
		pv.p.z += plane.p.z;

		GeometryMath_F64.mult(R, pv.normal, pv.normal);

		return pv;
	}

	@Test
	public void simpleCase0() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(plane, 1, 0, 1));
		pts.add(createPt(plane, 0, 1, 1));
		pts.add(createPt(plane, -1, -1, 1));

		GeneratePlanePointVector alg = new GeneratePlanePointVector(0.1);
		PlaneGeneral3D_F64 found = new PlaneGeneral3D_F64();
		assertTrue(alg.generate(pts, found));

		checkPlanes(plane, found, 1e-8);
	}

	@Test
	public void notSoSimpleCase() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(-1, 2, 0.5, 1, -0.6, -0.9);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(plane, 1, 0, 1));
		pts.add(createPt(plane, 0, 1, 1));
		pts.add(createPt(plane, -1, -1, 1));

		GeneratePlanePointVector alg = new GeneratePlanePointVector(0.1);
		PlaneGeneral3D_F64 found = new PlaneGeneral3D_F64();
		assertTrue(alg.generate(pts, found));

		checkPlanes(plane, found, 1e-8);
	}

	@Test
	public void checkInvarianceToVectorDirection() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(-1, 2, 0.5, 1, -0.6, -0.9);

		// same as above, but in the opposite direction
		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(plane, 1, 0, -1));
		pts.add(createPt(plane, 0, 1, -1));
		pts.add(createPt(plane, -1, -1, -1));

		GeneratePlanePointVector alg = new GeneratePlanePointVector(0.1);
		PlaneGeneral3D_F64 found = new PlaneGeneral3D_F64();
		assertTrue(alg.generate(pts, found));

		checkPlanes(plane, found, 1e-8);

		// check mixed directions
		pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(plane, 1, 0, 1));
		pts.add(createPt(plane, 0, 1, -1));
		pts.add(createPt(plane, -1, -1, 1));

		assertTrue(alg.generate(pts, found));

		checkPlanes(plane, found, 1e-8);
	}

	@Test
	public void checkAngleTolerance() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);

		GeneratePlanePointVector alg = new GeneratePlanePointVector(0.1);
		alg.n.set(plane.n);
		alg.n.normalize();

		// check perfect case
		PointVectorNN pv = createPt(plane, 1, 0, 1);
		assertTrue(alg.checkModel(pv, pv, pv));

		// check positive case close to threshold, positive rotation
		rotatePoint(plane, pv, 0.099);
		assertTrue(alg.checkModel(pv, pv, pv));
		// check positive case close to threshold, negative rotation
		pv = createPt(plane, 1, 0, 1);
		rotatePoint(plane, pv, -0.099);
		assertTrue(alg.checkModel(pv, pv, pv));

		// negative case, positive rotation
		PointVectorNN fail = createPt(plane, 1, 0, 1);
		rotatePoint(plane, fail, 0.1001);
		assertFalse(alg.checkModel(fail, pv, pv));
		assertFalse(alg.checkModel(pv, fail, pv));
		assertFalse(alg.checkModel(pv, pv, fail));

		// negative case, negative rotation
		fail = createPt(plane, 1, 0, 1);
		rotatePoint(plane, fail, -0.1001);
		assertFalse(alg.checkModel(fail, pv, pv));
		assertFalse(alg.checkModel(pv, fail, pv));
		assertFalse(alg.checkModel(pv, pv, fail));
	}

	@Test
	public void minimumPoints() {
		GeneratePlanePointVector alg = new GeneratePlanePointVector(0.1);

		assertEquals(3, alg.getMinimumPoints());
	}

	/**
	 * Does it correctly run the parameter check?
	 */
	@Test
	public void doesItPerformCheck() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(plane, 1, 0, 1));
		pts.add(createPt(plane, 0, 1, 1));
		pts.add(createPt(plane, -1, -1, 1));

		GeneratePlanePointVector alg = new GeneratePlanePointVector(0.1);
		PlaneGeneral3D_F64 found = new PlaneGeneral3D_F64();

		// give it a positive example and its return value should depend on the value of the CheckShape
		alg.setCheck(new CheckShapeDummy<PlaneGeneral3D_F64>(true));
		assertTrue(alg.generate(pts, found));
		alg.setCheck(new CheckShapeDummy<PlaneGeneral3D_F64>(false));
		assertFalse(alg.generate(pts, found));
	}

	private void rotatePoint(PlaneNormal3D_F64 plane, PointVectorNN pv, double angle) {
		// Find a vector perpendicular to the plane's normal
		Vector3D_F64 v = new Vector3D_F64();
		v.x = 20 - plane.p.x;
		v.y = -25 - plane.p.y;
		v.z = 16 - plane.p.z;

		Vector3D_F64 rotationAxis = plane.n.cross(v);
		rotationAxis.normalize();

		Rodrigues_F64 rod = new Rodrigues_F64(angle, rotationAxis);
		DMatrixRMaj R = ConvertRotation3D_F64.rodriguesToMatrix(rod, null);

		GeometryMath_F64.mult(R, pv.normal, pv.normal);
	}

}
