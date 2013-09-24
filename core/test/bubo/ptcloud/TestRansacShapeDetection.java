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

package bubo.ptcloud;

import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import org.ddogleg.fitting.modelset.ransac.RansacMulti;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Only tests the functions which are overloaded.
 *
 * @author Peter Abeles
 */
public class TestRansacShapeDetection {

	@Test
	public void selectMatchSet() {
		RansacMulti.ObjectType o = new RansacMulti.ObjectType();
		o.modelDistance = new DistanceFromPlanePointVector();
		o.modelGenerator = new GeneratePlanePointVector(0.1);
		o.thresholdFit = 0.5;

		List<RansacMulti.ObjectType> objects = new ArrayList<RansacMulti.ObjectType>();
		objects.add(o);

		RansacShapeDetection alg = new RansacShapeDetection(123213,100,objects);

		PlaneNormal3D_F64 planeNorm = new PlaneNormal3D_F64(0,0,0,0,0,1);
		PlaneGeneral3D_F64 plane = UtilPlane3D_F64.convert(planeNorm,null);

		createGraph(alg.getInitialSample());
		alg.selectMatchSet(o.modelDistance, o.thresholdFit, plane);

		List<PointVectorNN> candidates = alg.getCandidatePoints();

		// see if there is the expected number
		assertEquals(10,candidates.size());

		// they should all be on the plane within tolerance
		for( PointVectorNN pv : candidates ) {
			assertTrue(o.modelDistance.computeDistance(pv) <= 1 );
		}
	}

	/**
	 * Create a graph with cycles and 1/2 the nodes match the model and 1/2 don't
	 */
	private List<PointVectorNN> createGraph( FastQueue<PointVectorNN> initialSample ) {
		List<PointVectorNN> list = new ArrayList<PointVectorNN>();

		PointVectorNN prev = new PointVectorNN();
		prev.p = new Point3D_F64(0,0,0);

		list.add(prev);

		for( int i = 1; i < 10; i++ ) {
			PointVectorNN n = new PointVectorNN();
			n.p = new Point3D_F64(i,i,0);
			n.neighbors.add(prev);
			prev.neighbors.add(n);

			PointVectorNN o = new PointVectorNN();
			o.p = new Point3D_F64(i,i+1,10);
			o.neighbors.add(prev);
			prev.neighbors.add(o);

			o.neighbors.add(n);
			n.neighbors.add(o);

			list.add(n);
			list.add(o);

			prev = n;
		}

		// give it a set of points on the feature
		initialSample.add(list.get(0));
		initialSample.add(list.get(1));
		initialSample.add(list.get(3));

		return list;
	}



}
