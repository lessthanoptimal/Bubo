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

package bubo.clouds.filter;

import georegression.struct.point.Point3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestUniformDensityCloudOctree {

	Random rand = new Random(234);

	@Test
	public void basicTest() {

		List<Point3D_F64> input = new ArrayList<Point3D_F64>();

		for (int i = 0; i < 1000; i++) {
			Point3D_F64 p = new Point3D_F64(10,12,-3);
			p.x += rand.nextDouble()*0.3;
			p.y += rand.nextDouble()*0.2;
			p.z += rand.nextDouble()*0.1;
			input.add(p);
		}

		List<Point3D_F64> output = new ArrayList<Point3D_F64>();

		UniformDensityCloudOctree alg = new UniformDensityCloudOctree(10,0.1,123213);

		alg.process(input,output);

		int expectedN = (int)Math.round(10*(0.3*0.2*0.1)/(0.1*0.1*0.1));

		assertEquals(expectedN,output.size(),5);
	}
}