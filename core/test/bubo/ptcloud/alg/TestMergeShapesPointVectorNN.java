/*
 * Copyright (c) 2013, Peter Abeles. All Rights Reserved.
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

package bubo.ptcloud.alg;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestMergeShapesPointVectorNN {


	@Test
	public void merge_one() {
		fail("Implement");
	}

	@Test
	public void merge_N_identical() {
		for( int numShapes = 2; numShapes <= 5; numShapes++ ) {
			fail("Implement");
		}
	}

	/**
	 * There are shapes which represent the same shape, but contain a different subset of points
	 */
	@Test
	public void merge_sameObject_differentPoints() {
		fail("Implement");
	}

	@Test
	public void merge_threshold_commonPoints() {
		fail("Implement");
	}

	@Test
	public void merge_threshold_distance() {
		fail("Implement");
	}

	@Test
	public void merge_two_notSimilarAtAll() {
		fail("Implement");
	}

	@Test
	public void merge_differentShapes() {
		fail("Implement");
	}

	@Test
	public void findMembersRigorous() {
		fail("Implement");
	}

	@Test
	public void mergeShape() {
		fail("Implement");
	}

}
