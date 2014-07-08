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

package bubo.construct;

import georegression.metric.Intersection3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Box3D_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConstructOctree_F64 {

	@Test
	public void computeDivider() {
		Box3D_F64 cube = new Box3D_F64(10, 20, 30, 60, 80, 100);
		Point3D_F64 p = new Point3D_F64();
		ConstructOctree_F64.computeDivider(cube, p);

		assertEquals(10 + 25, p.x, 1e-8);
		assertEquals(20 + 30, p.y, 1e-8);
		assertEquals(30 + 35, p.z, 1e-8);
	}

	@Test
	public void setChildSpace() {
		Box3D_F64 cube = new Box3D_F64(-50, -50, -50, 50, 50, 50);
		Point3D_F64 divider = new Point3D_F64();

		Box3D_F64 child = new Box3D_F64();

		Point3D_F64 testPt = new Point3D_F64();

		for (int i = 0; i < 8; i++) {
			ConstructOctree_F64.setChildSpace(cube, divider, i, child);
			assertEquals(50, child.getLengthX(), 1e-8);
			assertEquals(50, child.getLengthY(), 1e-8);
			assertEquals(50, child.getLengthZ(), 1e-8);

			if (i == 0) {
				testPt.set(-1, -1, -1);
			} else if (i == 1) {
				testPt.set(-1, 1, -1);
			} else if (i == 2) {
				testPt.set(1, -1, -1);
			} else if (i == 3) {
				testPt.set(1, 1, -1);
			} else if (i == 4) {
				testPt.set(-1, -1, 1);
			} else if (i == 5) {
				testPt.set(-1, 1, 1);
			} else if (i == 6) {
				testPt.set(1, -1, 1);
			} else if (i == 7) {
				testPt.set(1, 1, 1);
			}

			assertTrue(Intersection3D_F64.contained(child, testPt));
		}
	}

}
