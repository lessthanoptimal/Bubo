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

import georegression.fitting.plane.ModelManagerPlaneGeneral3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestShapeDescription {

	@Test
	public void modelStuff() {
		ShapeDescription<PlaneGeneral3D_F64> desc = new ShapeDescription<PlaneGeneral3D_F64>();
		desc.modelManager = new ModelManagerPlaneGeneral3D_F64();

		desc.createModel();
		desc.createModel();
		desc.createModel();

		assertEquals(0, desc.unused.size());
		assertEquals(3, desc.used.size());

		desc.reset();

		assertEquals(3, desc.unused.size());
		assertEquals(0, desc.used.size());

		desc.createModel();
		desc.createModel();
		desc.createModel();
		desc.createModel();

		assertEquals(0, desc.unused.size());
		assertEquals(4, desc.used.size());
	}

	@Test
	public void recycleTail() {
		ShapeDescription<PlaneGeneral3D_F64> desc = new ShapeDescription<PlaneGeneral3D_F64>();
		desc.modelManager = new ModelManagerPlaneGeneral3D_F64();

		desc.createModel();
		desc.createModel();
		desc.createModel();

		assertEquals(0, desc.unused.size());
		assertEquals(3, desc.used.size());

		desc.recycleTail();

		assertEquals(1, desc.unused.size());
		assertEquals(2, desc.used.size());
	}

}
