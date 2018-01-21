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

package bubo.mapping.models.sensor;

import georegression.metric.UtilAngle;
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestProjectorRangeBearing2D extends StandardProjectorTests {

	ProjectorRangeBearing2D alg = new ProjectorRangeBearing2D();

	public TestProjectorRangeBearing2D() {
		super(1e-5);
	}

	@Test
	public void checkProjection() {
		// easy cases first
		double sqrt2 = Math.sqrt(2);
		checkProjection(0, 0, 0, 1, 0, 1, 0);
		checkProjection(0, 0, 0, 0, 1, 1, Math.PI / 2.0);
		checkProjection(1, -1, 0, 0, 0, sqrt2, 3 * Math.PI / 4.0);

		// try a boundary condition
		checkProjection(2, -1, 0, 0, -1, 2, Math.PI);

		// try a non-zero angle
		checkProjection(2, -1, 0.1, 0, -1, 2, Math.PI - 0.1);
	}

	private void checkProjection(double robotX, double robotY, double theta, double x, double y,
								 double d, double bearings) {

		alg.setLandmarkLocation(x, y);
		alg.compute(new DMatrixRMaj(3, 1, true, robotX, robotY, theta));

		DMatrixRMaj z = alg.getProjected();

		assertEquals(d, z.get(0, 0), 1e-8);
		assertTrue(UtilAngle.dist(bearings, z.get(1, 0)) <= 1e-8);
	}

	/**
	 * Check the jacobian by numerically differentiating it
	 */
	@Test
	public void checkJacobian() {

		alg.setLandmarkLocation(5, -3.5);

		checkProjectorAtPoint(alg, false, 1, 2, -0.1);
		checkProjectorAtPoint(alg, false, 1, -3.5, -0.1);

		// below is a pathological situation and the numerical solution will be incorrect
		// manually inspect as a sanity check
//		checkProjectorAtPoint(alg,true,5,2,-0.1);
	}
}
