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

package bubo.desc.sensors.lrf2d;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestLrf2dParam {
	@Test
	public void computeAngle() {
		Lrf2dParam param = new Lrf2dParam(null,-Math.PI/2,Math.PI,180,5,0,0);

		assertEquals(-Math.PI / 2, param.computeAngle(0), 1e-8);
		assertEquals(Math.PI/2,param.computeAngle(179),1e-8);
	}
}