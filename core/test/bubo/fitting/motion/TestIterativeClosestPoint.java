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

package bubo.fitting.motion;

import bubo.fitting.StoppingCondition;
import georegression.fitting.se.MotionSe2PointSVD_F64;
import georegression.fitting.se.MotionSe3PointCrossCovariance_F64;
import georegression.geometry.RotationMatrixGenerator;
import georegression.geometry.UtilPoint2D_F64;
import georegression.geometry.UtilPoint3D_F64;
import georegression.misc.test.GeometryUnitTest;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.se.Se3_F64;
import georegression.transform.se.SePointOps_F64;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestIterativeClosestPoint {

	Random rand = new Random(234234);

	public static void checkTransform(List<Point3D_F64> from, List<Point3D_F64> to, Se3_F64 tranFound, double tol) {
		Point3D_F64 foundPt = new Point3D_F64();
		for (int i = 0; i < from.size(); i++) {

			Point3D_F64 p = from.get(i);

			SePointOps_F64.transform(tranFound, p, foundPt);

			GeometryUnitTest.assertEquals(to.get(i), foundPt, tol);
		}
	}

	public static void checkTransform(List<Point2D_F64> from, List<Point2D_F64> to, Se2_F64 tranFound, double tol) {
		Point2D_F64 foundPt = new Point2D_F64();
		for (int i = 0; i < from.size(); i++) {

			Point2D_F64 p = from.get(i);

			SePointOps_F64.transform(tranFound, p, foundPt);

			GeometryUnitTest.assertEquals(to.get(i), foundPt, tol);
		}
	}

	/**
	 * The two sets of points match perfectly and with a little bit of noise
	 */
	@Test
	public void test3D() {
		for (double noise = 0; noise <= 0.01; noise += 0.01) {
			// can only correct small changes
			DenseMatrix64F R = RotationMatrixGenerator.eulerXYZ(0.01, -0.002, 0.03, null);
			Vector3D_F64 T = new Vector3D_F64(0.02, 0.03, 0.01);

			Se3_F64 tran = new Se3_F64(R, T);

			List<Point3D_F64> fromPts = UtilPoint3D_F64.random(-10, 10, 30, rand);
			List<Point3D_F64> fromOrig = UtilPoint3D_F64.copy(fromPts);
			List<Point3D_F64> toPts = new ArrayList<Point3D_F64>();
			for (Point3D_F64 p : fromPts) {
				toPts.add(SePointOps_F64.transform(tran, p, null));
			}

			// add noise
			UtilPoint3D_F64.noiseNormal(toPts, noise, rand);

			PointModel<Point3D_F64> model = new PointModel<Point3D_F64>(toPts);
			StoppingCondition stop = new StoppingCondition(10, 0.1 * noise / fromPts.size() + 1e-8);
			IterativeClosestPoint<Se3_F64, Point3D_F64>
					alg = new IterativeClosestPoint<Se3_F64, Point3D_F64>(stop, new MotionSe3PointCrossCovariance_F64());
			alg.setModel(model);

			alg.process(fromPts);

			Se3_F64 foundTran = alg.getMotion();

//            tran.getT().print();
//            foundTran.getT().print();

			checkTransform(fromOrig, toPts, foundTran, noise * 10 + 1e-8);
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

			List<Point2D_F64> fromPts = UtilPoint2D_F64.random(-10, 10, 30, rand);
			List<Point2D_F64> fromOrig = UtilPoint2D_F64.copy(fromPts);
			List<Point2D_F64> toPts = new ArrayList<Point2D_F64>();
			for (Point2D_F64 p : fromPts) {
				toPts.add(SePointOps_F64.transform(tran, p, null));
			}

			// add noise
			UtilPoint2D_F64.noiseNormal(toPts, noise, rand);

			PointModel<Point2D_F64> model = new PointModel<Point2D_F64>(toPts);
			StoppingCondition stop = new StoppingCondition(10, 0.1 * noise / fromPts.size() + 1e-8);
			IterativeClosestPoint<Se2_F64, Point2D_F64>
					alg = new IterativeClosestPoint<Se2_F64, Point2D_F64>(stop, new MotionSe2PointSVD_F64());
			alg.setModel(model);

			alg.process(fromPts);

			Se2_F64 foundTran = alg.getMotion();

//        tran.getT().print();
//        foundTran.getT().print();

			checkTransform(fromOrig, toPts, foundTran, noise * 10 + 1e-8);
		}
	}

}
