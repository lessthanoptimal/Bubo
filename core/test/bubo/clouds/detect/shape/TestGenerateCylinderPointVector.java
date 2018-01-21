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
import georegression.metric.Distance3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import georegression.struct.so.Rodrigues_F64;
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestGenerateCylinderPointVector {

	public static void checkEquivalent(Cylinder3D_F64 a, Cylinder3D_F64 b, double tol) {
		assertEquals(a.radius, b.radius, 1e-8);

		// the points should be on the other line
		assertEquals(0, Distance3D_F64.distance(a.line, b.line.p), tol);
		assertEquals(0, Distance3D_F64.distance(b.line, a.line.p), tol);

		// slopes should be the same
		double dot = a.line.slope.dot(b.line.slope);
		double tmp = dot / (a.line.slope.norm() * b.line.slope.norm());
		double angle;
		if (tmp > 1.0)
			angle = 0;
		else if (tmp < -1.0)
			angle = Math.PI;
		else
			angle = Math.acos(tmp);
		assertTrue(Math.abs(angle) < tol || Math.abs(angle - Math.PI) < tol);
	}

	public static PointVectorNN createPt(Cylinder3D_F64 cylinder, double h, double theta, double signNorm) {
		PointVectorNN pv = new PointVectorNN();
		pv.p = new Point3D_F64();
		pv.p.x = cylinder.radius * Math.cos(theta);
		pv.p.y = cylinder.radius * Math.sin(theta);
		pv.p.z = h;

		pv.normal.set(pv.p.x * signNorm, pv.p.y * signNorm, 0);
		pv.normal.normalize();

		Vector3D_F64 axisZ = new Vector3D_F64(0, 0, 1);
		Vector3D_F64 cross = axisZ.cross(cylinder.line.slope);
		if (Math.abs(cross.norm()) < 1e-8) {
			cross.set(0, 0, 1);
		} else {
			cross.normalize();
		}

		double angle = axisZ.dot(cylinder.line.slope);
		angle = Math.acos(angle / (cylinder.line.slope.norm()));

		Rodrigues_F64 rod = new Rodrigues_F64(angle, cross);
		DMatrixRMaj R = ConvertRotation3D_F64.rodriguesToMatrix(rod, null);

		GeometryMath_F64.mult(R, pv.p, pv.p);
		pv.p.x += cylinder.line.p.x;
		pv.p.y += cylinder.line.p.y;
		pv.p.z += cylinder.line.p.z;

		GeometryMath_F64.mult(R, pv.normal, pv.normal);

		return pv;
	}

	@Test
	public void simpleCase0() {
		Cylinder3D_F64 c = new Cylinder3D_F64(0, 0, 0, 0, 0, 1, 2);

		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.1);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(c, 0, 0, 1));
		pts.add(createPt(c, 0.5, Math.PI / 2.0, 1));
		pts.add(createPt(c, 1, 0.5, 1));

		Cylinder3D_F64 found = new Cylinder3D_F64();

		assertTrue(alg.generate(pts, found));

		assertEquals(1, found.line.slope.norm(), 1e-8);
		checkEquivalent(c, found, 1e-8);
	}

	/**
	 * Similar to simpleCase0, but the cylinder is along a different axis
	 */
	@Test
	public void simpleCase1() {
		Cylinder3D_F64 c = new Cylinder3D_F64(0, 0, 0, 0, 1, 0, 2);

		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.1);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(c, 0, 0, 1));
		pts.add(createPt(c, 0.5, Math.PI / 2.0, 1));
		pts.add(createPt(c, 1, 0.5, 1));

		Cylinder3D_F64 found = new Cylinder3D_F64();

		assertTrue(alg.generate(pts, found));

		assertEquals(1, found.line.slope.norm(), 1e-8);
		checkEquivalent(c, found, 1e-8);
	}

	/**
	 * Cylinder is at some funky orientation and location
	 */
	@Test
	public void notATrivialCaseAnyMore() {
		Cylinder3D_F64 c = new Cylinder3D_F64(1, -0.5, 3, 2, 1, -0.5, 1.5);

		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.1);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(c, 0, 0, 1));
		pts.add(createPt(c, 0.5, Math.PI / 2.0, 1));
		pts.add(createPt(c, 1, 0.5, 1));

		Cylinder3D_F64 found = new Cylinder3D_F64();

		alg.generate(pts, found);

		assertEquals(1, found.line.slope.norm(), 1e-8);
		checkEquivalent(c, found, 1e-8);
	}

	@Test
	public void checkInvarianceToVectorDirection() {
		Cylinder3D_F64 c = new Cylinder3D_F64(0, 0, 0, 0, 0, 1, 2);

		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.1);

		// same as simple case, but norms are in the opposite direction
		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(c, 0, 0, -1));
		pts.add(createPt(c, 0.5, Math.PI / 2.0, -1));
		pts.add(createPt(c, 1, 0.5, -1));

		Cylinder3D_F64 found = new Cylinder3D_F64();

		assertTrue(alg.generate(pts, found));

		assertEquals(1, found.line.slope.norm(), 1e-8);
		checkEquivalent(c, found, 1e-8);

		// mixed direction norms
		pts.add(createPt(c, 0, 0, 1));
		pts.add(createPt(c, 0.5, Math.PI / 2.0, -1));
		pts.add(createPt(c, 1, 0.5, -1));

		assertTrue(alg.generate(pts, found));

		assertEquals(1, found.line.slope.norm(), 1e-8);
		checkEquivalent(c, found, 1e-8);
	}

	@Test
	public void checkAngleTolerance() {
		Cylinder3D_F64 c = new Cylinder3D_F64(0, 0, 0, 0, 0, 1, 2);
		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.2);

		// far away
		PointVectorNN a = createPt(c, 0, 0, 1);
		rotatePoint(c, a, 0.5);
		assertFalse(alg.checkModel(c, a, 2, 2, 2));

		// close to threshold
		a = createPt(c, 0, 0, 1);
		rotatePoint(c, a, 0.10001);
		assertFalse(alg.checkModel(c, a, 2, 2, 2));


		// point is perfect
		a = createPt(c, 0, 0, 1);
		rotatePoint(c, a, 0);
		assertTrue(alg.checkModel(c, a, 2, 2, 2));

		// close to threshold
		a = createPt(c, 0, 0, 1);
		rotatePoint(c, a, 0.09);
		assertTrue(alg.checkModel(c, a, 2, 2, 2));
	}

	@Test
	public void checkDistanceTolerance() {
		Cylinder3D_F64 c = new Cylinder3D_F64(0, 0, 0, 0, 0, 1, 2);
		PointVectorNN a = createPt(c, 0, 0, 1);

		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.2);

		// perfect positive case
		assertTrue(alg.checkModel(c, a, 2, 2, 2));
		// positive close to border
		assertTrue(alg.checkModel(c, a, 2.19, 2.19, 2.19));
		assertTrue(alg.checkModel(c, a, 1.81, 1.81, 1.81));

		// negative close to border.  check each point individually
		assertFalse(alg.checkModel(c, a, 2.21, 2, 2));
		assertFalse(alg.checkModel(c, a, 2, 2.21, 2));
		assertFalse(alg.checkModel(c, a, 2, 2, 2.21));
		assertFalse(alg.checkModel(c, a, 1.79, 2, 2));
		assertFalse(alg.checkModel(c, a, 2, 1.79, 2));
		assertFalse(alg.checkModel(c, a, 2, 2, 1.79));
	}

	@Test
	public void minimumPoints() {
		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.1);

		assertEquals(3, alg.getMinimumPoints());
	}

	/**
	 * Does it correctly run the parameter check?
	 */
	@Test
	public void doesItPerformCheck() {
		Cylinder3D_F64 c = new Cylinder3D_F64(0, 0, 0, 0, 0, 1, 2);

		GenerateCylinderPointVector alg = new GenerateCylinderPointVector(0.1, 0.1);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		pts.add(createPt(c, 0, 0, 1));
		pts.add(createPt(c, 0.5, Math.PI / 2.0, 1));
		pts.add(createPt(c, 1, 0.5, 1));

		Cylinder3D_F64 found = new Cylinder3D_F64();

		// give it a positive example and its return value should depend on the value of the CheckShape
		alg.setCheck(new CheckShapeDummy<Cylinder3D_F64>(true));
		assertTrue(alg.generate(pts, found));
		alg.setCheck(new CheckShapeDummy<Cylinder3D_F64>(false));
		assertFalse(alg.generate(pts, found));
	}

	/**
	 * Rotates the point's normal up or down away from the cylinder's tangent
	 */
	private void rotatePoint(Cylinder3D_F64 cylinder, PointVectorNN pv, double angle) {
		Vector3D_F64 v = new Vector3D_F64();
		v.x = pv.p.x - cylinder.line.p.x;
		v.y = pv.p.y - cylinder.line.p.y;
		v.z = pv.p.z - cylinder.line.p.z;

		Vector3D_F64 rotationAxis = v.cross(cylinder.line.slope);
		rotationAxis.normalize();

		Rodrigues_F64 rod = new Rodrigues_F64(angle, rotationAxis);
		DMatrixRMaj R = ConvertRotation3D_F64.rodriguesToMatrix(rod, null);

		GeometryMath_F64.mult(R, pv.normal, pv.normal);
	}
}
