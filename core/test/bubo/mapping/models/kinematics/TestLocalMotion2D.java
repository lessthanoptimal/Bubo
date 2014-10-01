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

package bubo.mapping.models.kinematics;

import georegression.struct.se.Se2_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestLocalMotion2D {

	@Test
	public void testHelperFunctions() {
		Se2_F64 a = new Se2_F64(3,4,0.2);
		Se2_F64 b = new Se2_F64(7,3,-0.3);

		LocalMotion2D motion = new LocalMotion2D();

		motion.setFrom(a,b);
		motion.addTo(a);

		assertEquals(b.getX(),a.getX(),1e-8);
		assertEquals(b.getY(),a.getY(),1e-8);
		assertEquals(b.getYaw(),a.getYaw(),1e-8);
	}
}
