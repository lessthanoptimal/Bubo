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
import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestSplitIntoClustersNN {

	Random rand = new Random(234);

	@Test
	public void easyTest() {
		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		for (int i = 0; i < 100; i++) {
			cloud.add(createRandomPoint(0,0,0,0.1));
			cloud.add(createRandomPoint(0,2,0,0.1));
			cloud.add(createRandomPoint(0,0,3,0.1));
		}

		PointCloudToGraphNN cloudToNN = new PointCloudToGraphNN((NearestNeighbor)FactoryNearestNeighbor.kdtree(),10,0.025);
		cloudToNN.process(cloud);

		SplitIntoClustersNN alg = new SplitIntoClustersNN();
		alg.process(cloudToNN.getListPointVector().toList());

		assertEquals(3,alg.getClusters().size());

		int total = 0;
		for(List<PointVectorNN> c : alg.getClusters() ) {
			total += c.size();
		}
		assertEquals(300,total);
	}

	@Test
	public void ignorePointsNotInInputList() {
		fail("Implement");
	}

	public Point3D_F64 createRandomPoint( double x , double y , double z , double r ) {
		Point3D_F64 p = new Point3D_F64(x,y,z);

		p.x += (rand.nextDouble()-0.5)*2*r;
		p.y += (rand.nextDouble()-0.5)*2*r;
		p.z += (rand.nextDouble()-0.5)*2*r;

		return p;
	}
}
