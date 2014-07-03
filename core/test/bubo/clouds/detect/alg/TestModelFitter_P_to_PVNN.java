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
import org.ddogleg.fitting.modelset.ModelFitter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestModelFitter_P_to_PVNN {

	@Test
	public void fitModel() {
		Dummy dummy = new Dummy();

		ModelFitter_P_to_PVNN<Object> alg = new ModelFitter_P_to_PVNN<Object>(dummy);

		List<PointVectorNN> list = new ArrayList<PointVectorNN>();
		for (int i = 0; i < 10; i++) {
			PointVectorNN pv = new PointVectorNN();
			pv.p = new Point3D_F64();
			list.add(pv);
		}

		alg.fitModel(list, 1, 1);

		assertTrue(dummy.calledFit);

		assertEquals(10, dummy.list.size());
		for (int i = 0; i < 10; i++) {
			assertTrue(list.get(i).p == dummy.list.get(i));
		}
	}

	protected class Dummy implements ModelFitter<Object, Point3D_F64> {

		boolean calledFit = false;
		List<Point3D_F64> list;

		@Override
		public boolean fitModel(List<Point3D_F64> dataSet, Object initial, Object found) {
			list = dataSet;
			calledFit = true;
			return true;
		}
	}

}
