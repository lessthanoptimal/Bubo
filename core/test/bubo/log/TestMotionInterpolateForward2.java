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

package bubo.log;

import georegression.struct.se.Se2_F64;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestMotionInterpolateForward2 {

	@Test
	public void lookup() {
		LogMotion2 log = new LogMotion2();

		log.add(2.2,new Se2_F64(1,2,0));
		log.add(4.2,new Se2_F64(4,6,0));
		log.add(5.2,new Se2_F64(5,2,0));
		log.add(6.1,new Se2_F64(6,6,0));
		log.add(7.3,new Se2_F64(7,2,0));
		log.add(8.4,new Se2_F64(8,6,0));

		MotionInterpolateForward2 alg = new MotionInterpolateForward2();
		alg.setLog(log);

		Se2_F64 found = new Se2_F64();
		assertFalse(alg.lookup(1.1, found));
		assertFalse(alg.lookup(9, found));

		assertTrue(alg.lookup(4.2,found));
		assertEquals(4,found.T.x,1e-8);

		assertTrue(alg.lookup(4.2,found));
		assertEquals(4,found.T.x,1e-8);

		assertTrue(alg.lookup(5.2,found));
		assertEquals(5,found.T.x,1e-8);

		assertFalse(alg.lookup(8.5,found));
	}

	@Test
	public void bound() {
		LogMotion2 log = new LogMotion2();

		MotionInterpolateForward2 alg = new MotionInterpolateForward2();
		alg.setLog(log);
		log.add(2, new Se2_F64());
		log.add(3,new Se2_F64());
		assertEquals(0, alg.bound(2));
		assertEquals(0,alg.bound(2.5));
		assertEquals(0,alg.bound(3));

		log.add(4.5,new Se2_F64());
		assertEquals(0,alg.bound(2.5));
		assertEquals(1,alg.bound(3.2));

		log.add(10, new Se2_F64());
		assertEquals(0,alg.bound(2.5));
		assertEquals(1,alg.bound(3.2));
		assertEquals(2,alg.bound(5));
	}

	@Test
	public void bound_outside() {
		LogMotion2 log = new LogMotion2();

		MotionInterpolateForward2 alg = new MotionInterpolateForward2();
		alg.setLog(log);

		assertEquals(-1, alg.bound(1));
		log.add(2, new Se2_F64());
		assertEquals(-1, alg.bound(1));
		assertEquals(-1, alg.bound(3));
		log.add(3, new Se2_F64());
		assertEquals(-1,alg.bound(1));
		assertEquals(-1,alg.bound(4));
	}
}
