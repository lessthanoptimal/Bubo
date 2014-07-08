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

package bubo.maps.d3.grid;

import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Point3D_I32;
import georegression.struct.se.Se3_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestGridMapSpacialInfo3D {
	@Test
	public void gridToMap() {
		GridMapSpacialInfo3D alg = new GridMapSpacialInfo3D(0.1,new Se3_F64());
		Point3D_F64 map = new Point3D_F64();
		alg.gridToMap(1, 2, 3, map);

		assertEquals(0.1,map.getX(),1e-8);
		assertEquals(0.2,map.getY(),1e-8);
		assertEquals(0.3,map.getZ(),1e-8);
	}

	@Test
	public void mapToGrid() {
		GridMapSpacialInfo3D alg = new GridMapSpacialInfo3D(0.1,new Se3_F64());
		Point3D_I32 map = new Point3D_I32();
		alg.mapToGrid(0.1001, 0.2001, 0.30001, map);

		assertEquals(1,map.getX());
		assertEquals(2,map.getY());
		assertEquals(3,map.getZ());
	}

	@Test
	public void canonicalToMap() {
		Se3_F64 mapToCanon = new Se3_F64();
		mapToCanon.getT().set(-2,-3,-4);

		Point3D_F64 canon = new Point3D_F64();
		Point3D_F64 map = new Point3D_F64();

		GridMapSpacialInfo3D alg = new GridMapSpacialInfo3D(0.1,mapToCanon);
		alg.canonicalToMap(canon,map);

		assertEquals(2,map.getX(),1e-8);
		assertEquals(3,map.getY(),1e-8);
		assertEquals(4, map.getZ(), 1e-8);
	}

	@Test
	public void mapToCanonical() {
		Se3_F64 mapToCanon = new Se3_F64();
		mapToCanon.getT().set(-2,-3,-4);

		Point3D_F64 canon = new Point3D_F64();
		Point3D_F64 map = new Point3D_F64();

		GridMapSpacialInfo3D alg = new GridMapSpacialInfo3D(0.1,mapToCanon);
		alg.mapToCanonical(canon, map);

		assertEquals(-2,map.getX(),1e-8);
		assertEquals(-3,map.getY(),1e-8);
		assertEquals(-4, map.getZ(), 1e-8);
	}
}
