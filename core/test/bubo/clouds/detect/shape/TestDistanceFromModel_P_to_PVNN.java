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

package bubo.clouds.detect.shape;

import bubo.clouds.detect.alg.PointVectorNN;
import georegression.struct.point.Point3D_F64;
import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestDistanceFromModel_P_to_PVNN {

	@Test
	public void single() {
		Dummy dummy = new Dummy();

		DistanceFromModel_P_to_PVNN<Object> alg = new DistanceFromModel_P_to_PVNN<Object>(dummy);

		alg.setModel(1);
		alg.computeDistance(new PointVectorNN());

		assertTrue(dummy.calledModel);
		assertTrue(dummy.calledSingle);
		assertFalse(dummy.calledMultiple);
	}

	@Test
	public void multiple() {
		Dummy dummy = new Dummy();

		DistanceFromModel_P_to_PVNN<Object> alg = new DistanceFromModel_P_to_PVNN<Object>(dummy);

		List<PointVectorNN> list = new ArrayList<PointVectorNN>();
		for (int i = 0; i < 10; i++) {
			PointVectorNN pv = new PointVectorNN();
			pv.p = new Point3D_F64();
			list.add(pv);
		}

		alg.setModel(1);
		alg.computeDistance(list, new double[10]);

		assertTrue(dummy.calledModel);
		assertFalse(dummy.calledSingle);
		assertTrue(dummy.calledMultiple);

		assertEquals(10, dummy.list.size());
		for (int i = 0; i < 10; i++) {
			assertTrue(list.get(i).p == dummy.list.get(i));
		}
	}

	protected class Dummy implements DistanceFromModel<Object, Point3D_F64> {

		boolean calledModel = false;
		boolean calledSingle = false;
		boolean calledMultiple = false;

		List<Point3D_F64> list;

		@Override
		public void setModel(Object o) {
			calledModel = true;
		}

		@Override
		public double computeDistance(Point3D_F64 pt) {
			calledSingle = true;
			return 0;
		}

		@Override
		public void computeDistance(List<Point3D_F64> point3D_f64s, double[] distance) {
			list = point3D_f64s;
			calledMultiple = true;
		}

		@Override
		public Class<Point3D_F64> getPointType() {
			return Point3D_F64.class;
		}

		@Override
		public Class<Object> getModelType() {
			return null;
		}
	}
}
