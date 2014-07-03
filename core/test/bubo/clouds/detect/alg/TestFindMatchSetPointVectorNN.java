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

import bubo.clouds.detect.shape.DistanceFromModel_P_to_PVNN;
import bubo.clouds.detect.shape.DistancePlaneToPoint3D;
import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestFindMatchSetPointVectorNN {

	/**
	 * Create a graph with cycles and 1/2 the nodes match the model and 1/2 don't
	 */
	public static List<PointVectorNN> createGraph(List<PointVectorNN> initialSample) {
		List<PointVectorNN> list = new ArrayList<PointVectorNN>();

		PointVectorNN prev = new PointVectorNN();
		prev.p = new Point3D_F64(0, 0, 0);

		list.add(prev);

		for (int i = 1; i < 10; i++) {
			PointVectorNN n = new PointVectorNN();
			n.p = new Point3D_F64(i, i, 0);
			n.neighbors.add(prev);
			prev.neighbors.add(n);

			PointVectorNN o = new PointVectorNN();
			o.p = new Point3D_F64(i, i + 1, 10);
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

	private static PointVectorNN pv(double x, double y, double z, double nx, double ny, double nz) {
		PointVectorNN p = new PointVectorNN();
		p.p = new Point3D_F64(x, y, z);
		p.normal.set(nx, ny, nz);
		return p;
	}

	@Test
	public void selectMatchSet() {
		DistancePlaneToPoint3D modelDistance = new DistancePlaneToPoint3D();

		PlaneNormal3D_F64 planeNorm = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);
		PlaneGeneral3D_F64 plane = UtilPlane3D_F64.convert(planeNorm, null);

		List<PointVectorNN> initialSample = new ArrayList<PointVectorNN>();
		createGraph(initialSample);

		List<PointVectorNN> found = new ArrayList<PointVectorNN>();

		FindMatchSetPointVectorNN matchFinder = new FindMatchSetPointVectorNN();
		matchFinder.reset();

		FindMatchSetPointVectorNN<PlaneGeneral3D_F64> alg = new FindMatchSetPointVectorNN<PlaneGeneral3D_F64>();
		alg.setModelDistance(new DistanceFromModel_P_to_PVNN(modelDistance));
		alg.selectMatchSet(initialSample, plane, 0.5, false, found);

		// see if there is the expected number
		assertEquals(10, found.size());

		// they should all be on the plane within tolerance
		modelDistance.setModel(plane);
		for (PointVectorNN pv : found) {
			assertTrue(modelDistance.computeDistance(pv.p) <= 1);
		}
	}

	/**
	 * Makes sure the flag to check the initial distance of the seed list is being obeyed.
	 */
	@Test
	public void checkInitialDistance() {
		DistancePlaneToPoint3D modelDistance = new DistancePlaneToPoint3D();

		PlaneNormal3D_F64 planeNorm = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);
		PlaneGeneral3D_F64 plane = UtilPlane3D_F64.convert(planeNorm, null);

		// create points far away from the plane
		List<PointVectorNN> initialSample = new ArrayList<PointVectorNN>();
		initialSample.add(pv(100, 200, 300, 0, 0, 1));
		initialSample.add(pv(0, 0, 400, 0, 0, 1));
		initialSample.add(pv(1, 2, 500, 0, 0, 1));

		List<PointVectorNN> found = new ArrayList<PointVectorNN>();

		FindMatchSetPointVectorNN matchFinder = new FindMatchSetPointVectorNN();
		matchFinder.reset();

		FindMatchSetPointVectorNN<PlaneGeneral3D_F64> alg = new FindMatchSetPointVectorNN<PlaneGeneral3D_F64>();
		alg.setModelDistance(new DistanceFromModel_P_to_PVNN(modelDistance));
		alg.selectMatchSet(initialSample, plane, 0.5, false, found);

		// the initial is never sanity checked and should include all the points
		assertEquals(3, found.size());

		// sanity check it now and the output should be empty
		found.clear();
		alg.selectMatchSet(initialSample, plane, 0.5, true, found);
		assertEquals(0, found.size());
	}

}
