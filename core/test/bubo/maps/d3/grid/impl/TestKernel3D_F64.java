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

package bubo.maps.d3.grid.impl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestKernel3D_F64 {
	@Test
	public void get() {
		Kernel3D_F64 kernel = new Kernel3D_F64(3);
		for (int i = 0; i < kernel.data.length; i++) {
			kernel.data[i] = i;
		}

		int index = 0;
		for (int i = 0; i < kernel.width; i++) {
			for (int j = 0; j < kernel.width; j++) {
				for (int k = 0; k < kernel.width; k++, index++) {
					double found = kernel.get(i,j,k);
					double expected = kernel.data[index];
					assertTrue(found==expected);
				}
			}
		}
	}

	@Test
	public void getRadius() {
		Kernel3D_F64 k = new Kernel3D_F64(2);
		assertEquals(2,k.getRadius());
	}

	@Test
	public void getWidth() {
		Kernel3D_F64 k = new Kernel3D_F64(2);
		assertEquals(5, k.getWidth());
	}

	/**
	 * Tests the property of a gaussian kernel that the value goes down as it goes farther away
	 */
	@Test
	public void gaussian_decreasing() {
		Kernel3D_F64 k = Kernel3D_F64.gaussian(3,4);

		List<DistanceWeight> weights = new ArrayList<DistanceWeight>();

		for (int i = 0; i < k.getWidth(); i++) {
			int di = i-k.getRadius();
			for (int j = 0; j < k.getWidth(); j++) {
				int dj = j-k.getRadius();
				for (int l = 0; l < k.getWidth(); l++) {
					int dl = l-k.getRadius();

					DistanceWeight dw = new DistanceWeight();
					dw.weight = k.get(i,j,l);
					dw.distance = Math.sqrt(di*di + dj*dj + dl*dl);
					weights.add(dw);
				}
			}
		}

		Collections.sort(weights);

		DistanceWeight prev = null;
		for( DistanceWeight w : weights ) {
			if( prev != null ) {
				assertTrue( prev.distance <= w.distance );
				assertTrue( prev.weight >= w.weight );
			}
			prev = w;
		}
	}

	@Test
	public void gaussian_sumsToOne() {
		for( int i = 1; i < 5; i++ ) {
			Kernel3D_F64 k = Kernel3D_F64.gaussian(1.5*i,i);
			double total = 0;
			for (int j = 0; j < k.data.length; j++) {
				total += k.data[j];
			}
			assertEquals(1,total,1e-8);
		}
	}

	private static class DistanceWeight implements Comparable<DistanceWeight>
	{
		double weight;
		double distance;

		@Override
		public int compareTo(DistanceWeight o) {
			if( distance < o.distance )
				return -1;
			else if( distance > o.distance )
				return 1;
			else
				return 0;
		}
	}
}