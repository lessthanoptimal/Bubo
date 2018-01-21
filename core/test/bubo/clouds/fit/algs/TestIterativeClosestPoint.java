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

package bubo.clouds.fit.algs;

import bubo.struct.StoppingCondition;
import georegression.fitting.se.MotionSe2PointSVD_F64;
import georegression.fitting.se.MotionSe3PointSVD_F64;
import georegression.geometry.ConvertRotation3D_F64;
import georegression.geometry.UtilPoint2D_F64;
import georegression.geometry.UtilPoint3D_F64;
import georegression.misc.test.GeometryUnitTest;
import georegression.struct.EulerType;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.se.Se3_F64;
import georegression.transform.se.SePointOps_F64;
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestIterativeClosestPoint {

	Random rand = new Random(234234);

	public static void checkTransform(List<Point3D_F64> pts, List<Point3D_F64> model, Se3_F64 modelToPts, double tol) {
		Point3D_F64 foundPt = new Point3D_F64();
		for (int i = 0; i < pts.size(); i++) {

			Point3D_F64 p = pts.get(i);
			SePointOps_F64.transform(modelToPts, p, foundPt);
			GeometryUnitTest.assertEquals(model.get(i), foundPt, tol);
		}
	}

	public static void checkTransform(List<Point2D_F64> pts, List<Point2D_F64> model, Se2_F64 modelToPts, double tol) {
		Point2D_F64 foundPt = new Point2D_F64();
		for (int i = 0; i < pts.size(); i++) {

			Point2D_F64 p = pts.get(i);
			SePointOps_F64.transform(modelToPts, p, foundPt);
			GeometryUnitTest.assertEquals(model.get(i), foundPt, tol);
		}
	}

	/**
	 * The two sets of points match perfectly and with a little bit of noise
	 */
	@Test
	public void test3D() {
		for (double noise = 0; noise <= 0.01; noise += 0.01) {
			// can only correct small changes
			DMatrixRMaj R = ConvertRotation3D_F64.eulerToMatrix(EulerType.XYZ,0.01, -0.002, 0.03, null);
			Vector3D_F64 T = new Vector3D_F64(0.02, 0.03, 0.01);

			Se3_F64 tran = new Se3_F64(R, T);

			List<Point3D_F64> srcPts = UtilPoint3D_F64.random(-10, 10, 30, rand);
			List<Point3D_F64> srcOrig = UtilPoint3D_F64.copy(srcPts);
			List<Point3D_F64> modelPts = new ArrayList<Point3D_F64>();
			for (Point3D_F64 p : srcPts) {
				modelPts.add(SePointOps_F64.transform(tran, p, null));
			}

			// add noise
			UtilPoint3D_F64.noiseNormal(modelPts, noise, rand);

			PointModel<Point3D_F64> model = new PointModel<Point3D_F64>(modelPts);
			StoppingCondition stop = new StoppingCondition(10, 0.1 * noise / srcPts.size() + 1e-8);
			IterativeClosestPoint<Se3_F64, Point3D_F64>
					alg = new IterativeClosestPoint<Se3_F64, Point3D_F64>(stop, new MotionSe3PointSVD_F64());
			alg.setModel(model);

			alg.process(srcPts);

			Se3_F64 foundTran = alg.getPointsToModel();

			checkTransform(srcOrig, modelPts, foundTran, noise * 10 + 1e-8);
		}
	}

	/**
	 * Test against perfectly matched and a little bit of noise
	 */
	@Test
	public void test2D() {
		for (double noise = 0; noise <= 0.01; noise += 0.01) {
			// can only correct small changes
			Se2_F64 tran = new Se2_F64(0.01, -0.02, 0.01);

			List<Point2D_F64> srcPts = UtilPoint2D_F64.random(-10, 10, 30, rand);
			List<Point2D_F64> srcOrig = UtilPoint2D_F64.copy(srcPts);
			List<Point2D_F64> modelPts = new ArrayList<Point2D_F64>();
			for (Point2D_F64 p : srcPts) {
				modelPts.add(SePointOps_F64.transform(tran, p, null));
			}

			// add noise
			UtilPoint2D_F64.noiseNormal(modelPts, noise, rand);

			PointModel<Point2D_F64> model = new PointModel<Point2D_F64>(modelPts);
			StoppingCondition stop = new StoppingCondition(10, 0.1 * noise / srcPts.size() + 1e-8);
			IterativeClosestPoint<Se2_F64, Point2D_F64>
					alg = new IterativeClosestPoint<Se2_F64, Point2D_F64>(stop, new MotionSe2PointSVD_F64());
			alg.setModel(model);

			alg.process(srcPts);

			Se2_F64 foundTran = alg.getPointsToModel();

			checkTransform(srcOrig, modelPts, foundTran, noise * 10 + 1e-8);
		}
	}

}
