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

package bubo.maps.d3.triangles;

import georegression.struct.shapes.Box3D_F64;
import georegression.struct.shapes.Triangle3D_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTriangle3dMap {

	Random rand = new Random(234);

	@Test
	public void computeBoundingBox() {
		Triangle3dMap map = new Triangle3dMap();

		for (int i = 0; i < 20; i++) {
			Triangle3D_F64 t = new Triangle3D_F64();
			t.v0.x = rand.nextDouble()*3-1.5;
			t.v0.y = rand.nextDouble()*3-1.5;
			t.v0.z = rand.nextDouble()*3-1.5;

			t.v1.x = rand.nextDouble()*3-1.5;
			t.v1.y = rand.nextDouble()*3-1.5;
			t.v1.z = rand.nextDouble()*3-1.5;

			t.v2.x = rand.nextDouble()*3-1.5;
			t.v2.y = rand.nextDouble()*3-1.5;
			t.v2.z = rand.nextDouble()*3-1.5;

			map.addTriangle(t);
		}

		Box3D_F64 box = map.computeBoundingBox();

		// check box properties
		assertTrue(box.p0.x >= -1.5);
		assertTrue(box.p0.y >= -1.5);
		assertTrue(box.p0.z >= -1.5);

		assertTrue(box.p1.x <= 1.5);
		assertTrue(box.p1.y <= 1.5);
		assertTrue(box.p1.z <= 1.5);

		for( Triangle3D_F64 t : map.triangles ) {
			assertTrue(t.v0.x >= box.p0.x && t.v0.x <= box.p1.x);
			assertTrue(t.v0.y >= box.p0.y && t.v0.y <= box.p1.y);
			assertTrue(t.v0.z >= box.p0.z && t.v0.z <= box.p1.z);

			assertTrue(t.v1.x >= box.p0.x && t.v1.x <= box.p1.x);
			assertTrue(t.v1.y >= box.p0.y && t.v1.y <= box.p1.y);
			assertTrue(t.v1.z >= box.p0.z && t.v1.z <= box.p1.z);

			assertTrue(t.v2.x >= box.p0.x && t.v2.x <= box.p1.x);
			assertTrue(t.v2.y >= box.p0.y && t.v2.y <= box.p1.y);
			assertTrue(t.v2.z >= box.p0.z && t.v2.z <= box.p1.z);
		}

	}

}
