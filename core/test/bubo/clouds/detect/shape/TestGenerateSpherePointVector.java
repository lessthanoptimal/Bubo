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
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.shapes.Sphere3D_F64;
import georegression.struct.so.Rodrigues_F64;
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestGenerateSpherePointVector {

	public static void checkSpheres(Sphere3D_F64 expected, Sphere3D_F64 found, double tol) {

		assertEquals(0, expected.center.distance(found.center), tol);
		assertEquals(expected.radius, found.radius, tol);
	}

	public static PointVectorNN createPt(Sphere3D_F64 sphere, double phi, double theta, double sign) {
		PointVectorNN pv = new PointVectorNN();
		pv.p = new Point3D_F64();
		pv.normal.set(0, 0, sign);
		pv.p.set(0, 0, sphere.radius);


		Rodrigues_F64 rodX = new Rodrigues_F64(phi, new Vector3D_F64(1, 0, 0));
		DMatrixRMaj rotX = ConvertRotation3D_F64.rodriguesToMatrix(rodX, null);
		Rodrigues_F64 rodZ = new Rodrigues_F64(theta, new Vector3D_F64(0, 0, 1));
		DMatrixRMaj rotZ = ConvertRotation3D_F64.rodriguesToMatrix(rodZ, null);

		GeometryMath_F64.mult(rotX, pv.p, pv.p);
		GeometryMath_F64.mult(rotZ, pv.p, pv.p);
		pv.p.x += sphere.center.x;
		pv.p.y += sphere.center.y;
		pv.p.z += sphere.center.z;

		GeometryMath_F64.mult(rotX, pv.normal, pv.normal);
		GeometryMath_F64.mult(rotZ, pv.normal, pv.normal);

		return pv;
	}

	@Test
	public void simpleCase0() {
		Sphere3D_F64 sphere = new Sphere3D_F64(0, 0, 0, 2);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(sphere, 0, 0, 1));
		pts.add(createPt(sphere, 0.5, 0, 1));
		pts.add(createPt(sphere, 1, 1, 1));

		GenerateSpherePointVector alg = new GenerateSpherePointVector(0.1, 0.2);
		Sphere3D_F64 found = new Sphere3D_F64();
		assertTrue(alg.generate(pts, found));

		checkSpheres(sphere, found, 1e-8);
	}

	@Test
	public void notSoSimpleCase() {
		Sphere3D_F64 sphere = new Sphere3D_F64(-0.56, 1, 2.93, 6.4);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(sphere, 0, 0, 1));
		pts.add(createPt(sphere, 0.5, 0, 1));
		pts.add(createPt(sphere, 1, 1, 1));

		GenerateSpherePointVector alg = new GenerateSpherePointVector(0.1, 0.2);
		Sphere3D_F64 found = new Sphere3D_F64();
		assertTrue(alg.generate(pts, found));

		checkSpheres(sphere, found, 1e-8);
	}

	@Test
	public void checkInvarianceToVectorDirection() {
		Sphere3D_F64 sphere = new Sphere3D_F64(-0.56, 1, 2.93, 6.4);

		// opposite direction of preceding test
		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(sphere, 0, 0, -1));
		pts.add(createPt(sphere, 0.5, 0, -1));
		pts.add(createPt(sphere, 1, 1, -1));

		GenerateSpherePointVector alg = new GenerateSpherePointVector(0.1, 0.2);
		Sphere3D_F64 found = new Sphere3D_F64();
		assertTrue(alg.generate(pts, found));

		checkSpheres(sphere, found, 1e-8);

		// mixed directions
		pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(sphere, 0, 0, -1));
		pts.add(createPt(sphere, 0.5, 0, 1));
		pts.add(createPt(sphere, 1, 1, 1));

		assertTrue(alg.generate(pts, found));

		checkSpheres(sphere, found, 1e-8);
	}

	@Test
	public void checkAngleTolerance() {
		Sphere3D_F64 sphere = new Sphere3D_F64(-0.56, 1, 2.93, 6.4);
		GenerateSpherePointVector alg = new GenerateSpherePointVector(0.1, 0.2);

		// check perfect case
		PointVectorNN pv = createPt(sphere, 1, 0.5, 1);
		assertTrue(alg.checkAngles(sphere, pv, pv, pv));

		// check positive case close to threshold, positive rotation
		pv = createPt(sphere, 1, 0.5, 1);
		rotatePoint(sphere, pv, 0.099);
		assertTrue(alg.checkAngles(sphere, pv, pv, pv));
		// check positive case close to threshold, negative rotation
		pv = createPt(sphere, 1, 0.5, 1);
		pv = createPt(sphere, 1, 0, 1);
		rotatePoint(sphere, pv, -0.099);
		assertTrue(alg.checkAngles(sphere, pv, pv, pv));

		// check negative case close to threshold, positive rotation
		PointVectorNN fail = createPt(sphere, 1, 0.5, 1);
		rotatePoint(sphere, fail, 0.1001);
		assertFalse(alg.checkAngles(sphere, fail, pv, pv));
		assertFalse(alg.checkAngles(sphere, pv, fail, pv));
		assertFalse(alg.checkAngles(sphere, pv, pv, fail));
		// check negative case close to threshold, negative rotation
		fail = createPt(sphere, 1, 0.5, 1);
		rotatePoint(sphere, fail, -0.1001);
		assertFalse(alg.checkAngles(sphere, fail, pv, pv));
		assertFalse(alg.checkAngles(sphere, pv, fail, pv));
		assertFalse(alg.checkAngles(sphere, pv, pv, fail));
	}

	/**
	 * Should fail due to radius not being consistent
	 */
	@Test
	public void checkDistanceTolerance() {
		Sphere3D_F64 sphere = new Sphere3D_F64(-0.56, 1, 2.93, 6.4);

		// opposite direction of preceding test
		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(sphere, 0, 0, 1));
		pts.add(createPt(sphere, 0.5, 0, 1));
		pts.add(createPt(sphere, 1, 1, 1));

		pushPoint(pts.get(0), 0.5);
		pushPoint(pts.get(1), -0.5);
		pushPoint(pts.get(2), 0);

		GenerateSpherePointVector alg = new GenerateSpherePointVector(0.1, 0.2);
		Sphere3D_F64 found = new Sphere3D_F64();
		assertFalse(alg.generate(pts, found));
	}

	/**
	 * Does it correctly run the parameter check?
	 */
	@Test
	public void doesItPerformCheck() {
		Sphere3D_F64 sphere = new Sphere3D_F64(0, 0, 0, 2);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(sphere, 0, 0, 1));
		pts.add(createPt(sphere, 0.5, 0, 1));
		pts.add(createPt(sphere, 1, 1, 1));

		GenerateSpherePointVector alg = new GenerateSpherePointVector(0.1, 0.2);
		Sphere3D_F64 found = new Sphere3D_F64();

		// give it a positive example and its return value should depend on the value of the CheckShape
		alg.setCheck(new CheckShapeDummy<Sphere3D_F64>(true));
		assertTrue(alg.generate(pts, found));
		alg.setCheck(new CheckShapeDummy<Sphere3D_F64>(false));
		assertFalse(alg.generate(pts, found));
	}

	private void pushPoint(PointVectorNN pv, double distance) {
		pv.p.x += pv.normal.x * distance;
		pv.p.y += pv.normal.y * distance;
		pv.p.z += pv.normal.z * distance;
	}

	private void rotatePoint(Sphere3D_F64 sphere, PointVectorNN pv, double angle) {
		// Find a vector perpendicular to the sphere surface normal
		Vector3D_F64 v = new Vector3D_F64();
		v.x = 20 - pv.p.x;
		v.y = -25 - pv.p.y;
		v.z = 16 - pv.p.z;

		Vector3D_F64 rotationAxis = pv.normal.cross(v);
		rotationAxis.normalize();

		Rodrigues_F64 rod = new Rodrigues_F64(angle, rotationAxis);
		DMatrixRMaj R = ConvertRotation3D_F64.rodriguesToMatrix(rod, null);

		GeometryMath_F64.mult(R, pv.normal, pv.normal);
	}
}
