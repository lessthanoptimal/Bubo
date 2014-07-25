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

package bubo.filters.specific.kf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCacheKalmanPredictor {

	/**
	 * Tells it to compute three times.  Two of which are the same interval.  Make sure
	 * compute is only called when necisary.
	 */
	@Test
	public void checkNumCompute() {
		Helper h = new Helper();

		h.compute(null,0.1);
		h.compute(null,0.1);

		assertEquals(1, h.getNumCompute());

		h.compute(null,0.22);
		assertEquals(2, h.getNumCompute());

	}

	/**
	 * A simple implementation that is just counts the number of times compute is called.
	 */
	private static class Helper extends CacheKalmanPredictor<Object> {
		int numCompute = 0;

		public Helper() {
			super(1);
		}

		protected void _compute(double deltaTime) {
			numCompute++;
		}

		public int getNumCompute() {
			return numCompute;
		}
	}
}
