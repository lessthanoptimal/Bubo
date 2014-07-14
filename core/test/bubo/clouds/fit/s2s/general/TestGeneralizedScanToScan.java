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
import bubo.struct.StoppingCondition;
import georegression.struct.point.Point2D_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestGeneralizedScanToScan {

	public static Lrf2dParam createParam() {
		return new Lrf2dParam(null, -Math.PI * 0.75, Math.PI * 0.75, 100, 5, 0.01, 0.01);
	}

	/**
	 * Sees if the location of the scan's end points are correctly computed.
	 */
	@Test
	public void computeScan() {
		Lrf2dParam param = createParam();
		Helper h = new Helper(null);
		h.setSensorParam(param);

		final int N = param.getNumberOfScans();
		final double range = param.getMaxRange() * 0.9;

		double ranges[] = new double[N];
		Point2D_F64 pts[] = new Point2D_F64[N];
		for (int i = 0; i < N; i++) {
			ranges[i] = range;
			pts[i] = new Point2D_F64();
		}

		// it should skip over points which exceed the max range
		ranges[10] = param.getMaxRange() + 10;

		h.computeScanEndPoint(ranges, pts);

		// validate by computing the range of each point
		for (int i = 0; i < N; i++) {
			double r = pts[i].norm();
			if (i != 10)
				assertEquals(range, r, 1e-8);
			else
				assertTrue(r < 1);
		}
	}

	/**
	 * Check to see if scans with theta in an unexpected direction are set
	 * as not visible
	 */
	@Test
	public void checkVisibleByDeltaAngle() {
		Lrf2dParam param = createParam();
		Helper h = new Helper(null);
		h.setSensorParam(param);

		final int N = param.getNumberOfScans();
		ScanInfo info = new ScanInfo(N);
		for (int i = 0; i < N; i++) {
			info.theta[i] = param.getStartAngle() + param.getAngleIncrement() * i;
			info.vis[i] = true;
		}
		// cause a bad point
		info.theta[10] = param.getStartAngle();

		h.checkVisibleByDeltaAngle(info);

		for (int i = 0; i < N; i++) {
			assertTrue(info.vis[i] == (i != 10));
		}
	}

	@Test
	public void projectScan() {
		Lrf2dParam param = createParam();
		Helper h = new Helper(null);
		h.setSensorParam(param);

		double r = param.getMaxRange() * 0.9;

		final int N = param.getNumberOfScans();
		ScanInfo info = new ScanInfo(N);
		double range[] = new double[N];
		for (int i = 0; i < N; i++) {
			range[i] = r;
			info.pts[i].set(1, 1);
			info.vis[i] = false;
		}
		// cause a bad point
		range[10] = param.getMaxRange() * 1.1;

		h.projectScan(range, info);

		// see if visible and angle has been correctly set
		for (int i = 0; i < N; i++) {
			if (i != 10) {
				assertEquals(info.theta[i], Math.PI / 4, 1e-8);
				assertTrue(info.vis[i]);
			} else {
				assertTrue(!info.vis[i]);
			}
		}
	}

	public static class Helper extends GeneralizedScanToScan {
		public Helper(StoppingCondition stop) {
			super(stop);
		}

		@Override
		protected void estimateAndApplyMotion(ScanInfo scanSrc, EstimationResults results) {
			}

		public void checkVisibleByDeltaAngle(ScanInfo info) {
			super.checkVisibleByDeltaAngle(info);
		}

		public void projectScan(double measuredRange[], ScanInfo info) {
			super.projectScan(measuredRange, info);
		}

		@Override
		public int totalScansMatched() {return 0;}
	}
}
