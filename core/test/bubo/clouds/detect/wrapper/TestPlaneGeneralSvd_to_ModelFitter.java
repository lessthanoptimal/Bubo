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

package bubo.clouds.detect.wrapper;

import bubo.clouds.detect.shape.TestGeneratePlanePointVector;
import bubo.clouds.detect.tools.PointCloudShapeTools;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import georegression.struct.point.Point3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestPlaneGeneralSvd_to_ModelFitter {

	@Test
	public void fitModel() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(0, 0, 0, 0, 0, 1);

		List<Point3D_F64> pts = new ArrayList<Point3D_F64>();
		pts.add(PointCloudShapeTools.createPt(plane, 1, 0));
		pts.add(PointCloudShapeTools.createPt(plane, 0, 1));
		pts.add(PointCloudShapeTools.createPt(plane, -1, -1));

		PlaneGeneralSvd_to_ModelFitter alg = new PlaneGeneralSvd_to_ModelFitter();

		PlaneGeneral3D_F64 found = new PlaneGeneral3D_F64();
		assertTrue(alg.fitModel(pts, null, found));

		TestGeneratePlanePointVector.checkPlanes(plane, found, 1e-8);
	}

}
