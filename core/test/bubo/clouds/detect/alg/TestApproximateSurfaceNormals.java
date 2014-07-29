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

package bubo.clouds.detect.alg;

import georegression.struct.point.Point3D_F64;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestApproximateSurfaceNormals {

	Random rand = new Random(234234);

	/**
	 * Give it a simple plane and see if it produces the expected results
	 */
	@Test
	public void checkUsingAPlane() {

		double maxDistance = 0.4;
		int numNeighbors = 5;

		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 40; j++) {
				cloud.add(new Point3D_F64(j * 0.1, i * 0.1, -4));
			}
		}

		FastQueue<PointVectorNN> output = new FastQueue<PointVectorNN>(PointVectorNN.class, false);

		ApproximateSurfaceNormals alg = new ApproximateSurfaceNormals(numNeighbors, maxDistance);

		alg.process(cloud, output);

		assertEquals(cloud.size(), output.size());

		for (int i = 0; i < cloud.size(); i++) {
			PointVectorNN pv = output.get(i);

			// see if the normal is valid
			assertEquals(0, pv.normal.x, 1e-8);
			assertEquals(0, pv.normal.y, 1e-8);
			assertEquals(1, Math.abs(pv.normal.z), 1e-8);

			// see if it's neighbors are close by
			assertEquals(numNeighbors, pv.neighbors.size);
			for (int j = 0; j < pv.neighbors.size; j++) {
				double d = pv.neighbors.get(j).p.distance(pv.p);
				assertTrue(d <= maxDistance);

				// the point should not be a neighbor to itself
				assertTrue(Math.abs(d) > 1e-8);
			}
		}
	}

	/**
	 * Makes sure the output vectors are normalized to one.  If no normal was found then it should be set to all zeros.
	 * <p/>
	 * Also checks to see that the index is correctly setup
	 */
	@Test
	public void checkVectorNormalizedToOne_index() {
		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		for (int i = 0; i < 200; i++) {
			double x = 3 * (rand.nextDouble() - 0.5);
			double y = 3 * (rand.nextDouble() - 0.5);
			double z = 3 * (rand.nextDouble() - 0.5);

			cloud.add(new Point3D_F64(x, y, z));
		}

		FastQueue<PointVectorNN> output = new FastQueue<PointVectorNN>(PointVectorNN.class, false);

		ApproximateSurfaceNormals alg = new ApproximateSurfaceNormals( 8, 0.4);

		alg.process(cloud, output);

		assertEquals(cloud.size(), output.size());

		int numNorm = 0;
		int numZero = 0;
		for (int i = 0; i < cloud.size(); i++) {
			PointVectorNN pv = output.get(i);

			// check index
			assertEquals(i, pv.index);

			// check normal
			double n = pv.normal.norm();

			if (n == 0) {
				numZero++;
			} else {
				assertEquals(1, pv.normal.norm(), 1e-8);
				numNorm++;
			}
		}

		assertTrue(numZero > 0);
		assertTrue(numNorm > 0);
	}

	/**
	 * Make sure everything works when it is called multiple times
	 */
	@Test
	public void multipleCalls() {
		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		for (int i = 0; i < 200; i++) {
			double x = 3 * (rand.nextDouble() - 0.5);
			double y = 3 * (rand.nextDouble() - 0.5);
			double z = 3 * (rand.nextDouble() - 0.5);

			cloud.add(new Point3D_F64(x, y, z));
		}

		FastQueue<PointVectorNN> output = new FastQueue<PointVectorNN>(PointVectorNN.class, false);

		ApproximateSurfaceNormals alg = new ApproximateSurfaceNormals( 8, 0.4);

		alg.process(cloud, output);

		assertEquals(cloud.size(), output.size());

		int numNorm = 0;
		int numZero = 0;
		for (int i = 0; i < cloud.size(); i++) {
			PointVectorNN pv = output.get(i);

			double n = pv.normal.norm();

			if (n == 0) {
				numZero++;
			} else {
				numNorm++;
			}
		}

		assertTrue(numZero > 0);
		assertTrue(numNorm > 0);

		output.reset();
		alg.process(cloud, output);

		assertEquals(cloud.size(), output.size());

		int numNorm2 = 0;
		int numZero2 = 0;
		for (int i = 0; i < cloud.size(); i++) {
			PointVectorNN pv = output.get(i);

			double n = pv.normal.norm();

			if (n == 0) {
				numZero2++;
			} else {
				numNorm2++;
			}
		}

		assertTrue(numZero == numZero2);
		assertTrue(numNorm == numNorm2);
	}

}
