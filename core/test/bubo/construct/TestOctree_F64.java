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

import georegression.struct.point.Point3D_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestOctree_F64 {

	@Test
	public void getChildIndex() {
		Octree_F64 tree = new Octree_F64();
		tree.divider = new Point3D_F64(20, 30, 40);

		// check easy cases
		assertEquals(0, tree.getChildIndex(new Point3D_F64(18, 28, 38)));
		assertEquals(1, tree.getChildIndex(new Point3D_F64(18, 32, 38)));
		assertEquals(2, tree.getChildIndex(new Point3D_F64(22, 28, 38)));
		assertEquals(3, tree.getChildIndex(new Point3D_F64(22, 32, 38)));
		assertEquals(4, tree.getChildIndex(new Point3D_F64(18, 28, 42)));
		assertEquals(5, tree.getChildIndex(new Point3D_F64(18, 32, 42)));
		assertEquals(6, tree.getChildIndex(new Point3D_F64(22, 28, 42)));
		assertEquals(7, tree.getChildIndex(new Point3D_F64(22, 32, 42)));

		// check edge cases
		assertEquals(7, tree.getChildIndex(new Point3D_F64(20, 30, 40)));

		assertEquals(2, tree.getChildIndex(new Point3D_F64(20, 28, 38)));
		assertEquals(1, tree.getChildIndex(new Point3D_F64(18, 30, 38)));
		assertEquals(4, tree.getChildIndex(new Point3D_F64(18, 28, 40)));
	}

}
