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

package bubo.mapping.build.ladar2d;

import georegression.struct.point.Point2D_I32;
import org.ddogleg.sorting.QuickSort_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestLineGridGenericUpdate {

	/**
	 * Converts a list of doubles into an array.
	 *
	 * @param list A list of Double.
	 * @return equivalent double array.
	 */
	public static double[] toPrimitive(List<Double> list) {
		double[] ret = new double[list.size()];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i);
		}

		return ret;
	}

	/**
	 * Tests lines that are along the axes in both positive and negative direction.
	 */
	@Test
	public void axisLines() {
		// positive x-axis
		checkAxis(1.1, 2.6, 5.1, 1, 0);
		// negative x-axis
		checkAxis(10.1, 2.6, 5.1, -1, 0);
		// positive y-axis
		checkAxis(1.1, 2.6, 5.1, 0, 1);
		// negative y-axis
		checkAxis(1.1, 12.6, 5.1, 0, -1);
	}

	private void checkAxis(double x0, double y0, double length, int dx, int dy) {
		double x1 = x0 + dx * length;
		double y1 = y0 + dy * length;

		double step = length / (int) length;
		int N = (int) length + 1;

		DebugUpdate update = new DebugUpdate();

		update.update(x0, y0, x1, y1, 5.1);

		assertEquals(N, update.points.size());

		int x = (int) x0, y = (int) y0;
		// need to know the order because it might be traversed in reverse order
		int order[] = new int[N];
		double dist[] = toPrimitive(update.dist);

		QuickSort_F64 sort = new QuickSort_F64();
		sort.sort(dist, 0,N, order);

		for (int i = 0; i < N; i++, x += dx, y += dy) {
			int index = order[i];

			Point2D_I32 p = update.points.get(index);
			double d = update.dist.get(index);

			assertEquals(i * step, d, 1e-8);

			assertEquals(x, p.x);
			assertEquals(y, p.y);
		}
	}

	private static class DebugUpdate extends LineGridGenericUpdate {

		List<Point2D_I32> points = new ArrayList<Point2D_I32>();
		List<Double> dist = new ArrayList<Double>();

		@Override
		protected void update(int x, int y, double d) {
			points.add(new Point2D_I32(x, y));
			dist.add(d);
		}
	}
}

