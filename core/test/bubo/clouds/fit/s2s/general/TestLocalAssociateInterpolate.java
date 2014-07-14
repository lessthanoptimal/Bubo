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
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestLocalAssociateInterpolate {

	@Test
	public void updateCode() {
		fail("Once there is some new data update this algorithm");
	}

	/**
	 * Very basis test that only tests basic functionality
	 */
	@Test
	public void simpleTest() {
		Lrf2dParam param = TestGeneralizedScanToScan.createParam();
		int N = param.getNumberOfScans();

		ScanInfo scanMatch = new ScanInfo(N);
		ScanInfo scanRef = new ScanInfo(N);

		for (int i = 0; i < N; i++) {
			scanMatch.pts[i] = new Point2D_F64(i, i);
			scanRef.pts[i] = new Point2D_F64(i, i);
			scanMatch.vis[i] = true;
			scanRef.vis[i] = true;
			scanMatch.theta[i] = param.getStartAngle() + i * param.getSweepAngle() / N;
			scanRef.theta[i] = param.getStartAngle() + i * param.getSweepAngle() / N;
		}

		DummyAssociate alg = new DummyAssociate(param, Math.PI / 20, 10, Math.PI / 100);
		alg.setParam(param);

		alg.associate(scanMatch, scanRef);

		// should associate to the point with the same index
		List<Point2D_F64> match = alg.getListSource();
		List<Point2D_F64> ref = alg.getListDestination();

		for (int i = 0; i < N; i++) {
			assertTrue(match.get(i) == scanMatch.pts[i]);
			assertTrue(ref.get(i).distance(scanRef.pts[i]) < 0.01);
		}
	}

	public static class DummyAssociate extends LocalAssociateInterpolate {

		double angle;

		public DummyAssociate(Lrf2dParam param, double searchNeighborhood, double maxSeparation, double samplePeriod) {
			super(param, searchNeighborhood, maxSeparation, samplePeriod);
		}

		@Override
		public boolean interpolate(InterpolatedPoint point) {
			// need to do some real interpolation here or else the sanity check will fail
			int index = (int) (param.getNumberOfScans() * (point.angle - param.getStartAngle()) / param.getSweepAngle());
			if (index < 0 || index >= param.getNumberOfScans())
				return false;
			else if (index == param.getNumberOfScans() - 1) {
				point.point.set(scanRef.pts[index]);
				return true;
			}

			double frac = (point.angle - scanRef.theta[index]) / (scanRef.theta[index + 1] - scanRef.theta[index]);
			double x = (1.0 - frac) * scanRef.pts[index].x + frac * scanRef.pts[index + 1].x;
			double y = (1.0 - frac) * scanRef.pts[index].y + frac * scanRef.pts[index + 1].y;

			point.point.x = x;
			point.point.y = y;

			return true;
		}

		@Override
		public void setTarget(int indexFrom) {
			angle = scanMatch.theta[indexFrom];
		}

		@Override
		public double distToTarget(InterpolatedPoint point) {
			return Math.abs(point.angle - angle);
		}
	}
}
