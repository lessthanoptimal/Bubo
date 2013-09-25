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

import georegression.struct.point.Point3D_F64;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

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

		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		for( int i = 0; i < 50; i++ ) {
			for( int j = 0; j < 40; j++ ) {
				cloud.add( new Point3D_F64(j*0.1,i*0.1,-4));
			}
		}

		FastQueue<PointVectorNN> output = new FastQueue<PointVectorNN>(PointVectorNN.class,false);

		ApproximateSurfaceNormals alg = new ApproximateSurfaceNormals(5,1.0);

		alg.process(cloud,output);

		assertEquals(cloud.size(),output.size());

		for( int i = 0; i < cloud.size(); i++ ) {
			PointVectorNN pv = output.get(i);

			// see if the normal is valid
			assertEquals(0,pv.normal.x,1e-8);
			assertEquals(0,pv.normal.y,1e-8);
			assertEquals(1,Math.abs(pv.normal.z),1e-8);

			// see if it's neighbors are close by
			assertEquals(5,pv.neighbors.size);
			for( int j = 0; j < pv.neighbors.size; j++ ) {
				double d = pv.neighbors.get(j).p.distance(pv.p);
				assertTrue(d < 0.4);
			}

			// TODO the point itself should not be in the neighbor list
		}
	}

	/**
	 * Makes sure the output vectors are normalized to one
	 */
	@Test
	public void checkVectorNormalizedToOne() {
		fail("Implement");
	}

	@Test
	public void checkNoNormalIsZero() {
		fail("Implement");
	}

}
