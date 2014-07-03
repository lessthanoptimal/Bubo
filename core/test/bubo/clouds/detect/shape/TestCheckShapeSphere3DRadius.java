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

import georegression.struct.shapes.Sphere3D_F64;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCheckShapeSphere3DRadius {

	@Test
	public void basic() {
		double threshold = 12.5;
		CheckShapeSphere3DRadius alg = new CheckShapeSphere3DRadius(threshold);

		Sphere3D_F64 param = new Sphere3D_F64(0, 0, 0, threshold);

		assertTrue(alg.valid(param));
		param.radius = threshold + 1e-5;
		assertFalse(alg.valid(param));
		param.radius = threshold - 1e-5;
		assertTrue(alg.valid(param));
	}
}
