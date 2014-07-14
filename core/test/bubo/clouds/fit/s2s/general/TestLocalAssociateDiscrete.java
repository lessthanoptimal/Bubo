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

package bubo.clouds.fit.s2s.general;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import georegression.struct.point.Point2D_F64;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLocalAssociateDiscrete {

	/**
	 * Test basic association functionality by having it associate a trivial example
	 */
	@Test
	public void trivialCase() {
		Lrf2dParam param = TestGeneralizedScanToScan.createParam();
		int N = param.getNumberOfScans();

		ScanInfo scanMatch = new ScanInfo(N);
		ScanInfo scanRef = new ScanInfo(N);

		for (int i = 0; i < N; i++) {
			scanMatch.pts[i] = new Point2D_F64(i, i);
			scanRef.pts[i] = new Point2D_F64(i, i);
			scanMatch.vis[i] = true;
			scanRef.vis[i] = true;
		}

		DummyAssociate alg = new DummyAssociate(10, 5);
		alg.setParam(param);

		alg.associate(scanMatch, scanRef);

		// should associate to the point with the same index
		List<Point2D_F64> match = alg.getListSource();
		List<Point2D_F64> ref = alg.getListDestination();

		for (int i = 0; i < N; i++) {
			assertTrue(match.get(i) == scanMatch.pts[i]);
			assertTrue(ref.get(i) == scanRef.pts[i]);
		}
	}

	/**
	 * Very simple implementation of associate.  Does it based upon the index alone.
	 */
	private static class DummyAssociate extends LocalAssociateDiscrete {
		int indexFrom;

		protected DummyAssociate(int searchNeighborhood, double maxSeparation) {
			super(searchNeighborhood, maxSeparation);
		}

		@Override
		public void setTarget(int indexFrom) {
			this.indexFrom = indexFrom;
		}

		@Override
		public double distToTarget(int indexTo) {
			return Math.abs(indexTo - indexFrom);
		}
	}
}
