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
import bubo.clouds.detect.shape.GeneratePlanePointVector;
import georegression.fitting.plane.ModelManagerPlaneGeneral3D_F64;
import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.fitting.modelset.ransac.RansacMulti;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Only tests the functions which are overloaded.
 *
 * @author Peter Abeles
 */
public class TestRansacShapeDetection {

	/**
	 * Checks to see if it uses the {@link FindMatchSetPointVectorNN} correctly.
	 */
	@Test
	public void selectMatchSet() {
		RansacMulti.ObjectType o = new RansacMulti.ObjectType();
		o.modelManager = new ModelManagerPlaneGeneral3D_F64();
		o.modelDistance = new DistanceFromModel_P_to_PVNN(new DistancePlaneToPoint3D());
		o.modelGenerator = new GeneratePlanePointVector(0.1);
		o.thresholdFit = 0.5;

		List<RansacMulti.ObjectType> objects = new ArrayList<RansacMulti.ObjectType>();
		objects.add(o);

		DummyFindMatch dummy = new DummyFindMatch();
		RansacShapeDetection alg = new RansacShapeDetection(123213, 100, dummy, objects);

		// make it so the candidate list is not zero
		PlaneNormal3D_F64 planeNorm = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);
		PlaneGeneral3D_F64 plane = UtilPlane3D_F64.convert(planeNorm, null);

		alg.selectMatchSet(null,o.modelDistance, o.thresholdFit, plane);

		// ,make sure it called the functions
		assertTrue(dummy.modelDistance);
		assertTrue(dummy.modelSet);
	}

	protected class DummyFindMatch extends FindMatchSetPointVectorNN<PlaneGeneral3D_F64> {

		boolean modelDistance = false;
		boolean modelSet = false;

		@Override
		public void selectMatchSet(List<PointVectorNN> initialMatch,
								   PlaneGeneral3D_F64 param, double threshold,
								   boolean checkInitial,
								   List<PointVectorNN> outputMatch) {
			assertTrue(outputMatch.size() == 0);
			assertTrue(modelDistance);
			modelSet = true;
		}

		@Override
		public void setModelDistance(DistanceFromModel modelDistance) {
			this.modelDistance = true;
		}
	}

}
