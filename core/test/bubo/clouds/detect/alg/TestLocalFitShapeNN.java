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

import bubo.clouds.detect.shape.*;
import bubo.clouds.detect.wrapper.PlaneGeneralSvd_to_ModelFitter;
import georegression.fitting.plane.CodecPlaneGeneral3D_F64;
import georegression.geometry.UtilPlane3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import georegression.struct.plane.PlaneNormal3D_F64;
import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.fitting.modelset.ModelCodec;
import org.ddogleg.fitting.modelset.ModelFitter;
import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestLocalFitShapeNN {

	Random rand = new Random(234);

	FindMatchSetPointVectorNN<PlaneGeneral3D_F64> findMatch = new FindMatchSetPointVectorNN<PlaneGeneral3D_F64>();

	ModelFitter<PlaneGeneral3D_F64, PointVectorNN> modelFitter =
			new ModelFitter_P_to_PVNN(new PlaneGeneralSvd_to_ModelFitter());
	DistanceFromModel<PlaneGeneral3D_F64, PointVectorNN> modelDistance =
			new DistanceFromModel_P_to_PVNN(new DistancePlaneToPoint3D());
	ModelCodec<PlaneGeneral3D_F64> modelCodec = new CodecPlaneGeneral3D_F64();
	CheckShapeParameters<PlaneGeneral3D_F64> modelCheck = new CheckShapeAcceptAll<PlaneGeneral3D_F64>();

	/**
	 * Its already in the optimal location and model parameters.  Then see if it fails when the model
	 * check failed
	 */
	@Test
	public void perfectInitial_modelCheck() {

		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 2, 3, -0.5, 0.25, 1);
		PlaneGeneral3D_F64 inputPlane = UtilPlane3D_F64.convert(plane, null);

		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		for (int i = 0; i < 100; i++) {
			double x = (rand.nextDouble() - 0.5) * 5;
			double y = (rand.nextDouble() - 0.5) * 5;
			pts.add(TestGeneratePlanePointVector.createPt(plane, x, y, 1));
		}

		createGraph(pts);

		findMatch.reset();
		LocalFitShapeNN<PlaneGeneral3D_F64> alg = new LocalFitShapeNN<PlaneGeneral3D_F64>(100, 1e-8, findMatch);

		alg.configure(modelFitter, modelDistance, modelCheck, modelCodec, 0.3);

		assertTrue(alg.refine(pts, inputPlane, true));

		assertEquals(100, pts.size());
		TestGeneratePlanePointVector.checkPlanes(plane, inputPlane, 1e-8);

		// now give it a model check which will always fail and see if it fails
		alg.configure(modelFitter, modelDistance, new CheckShapeDummy(false), modelCodec, 0.3);

		assertFalse(alg.refine(pts, inputPlane, true));

	}

	/**
	 * The initial estimate includes random points that aren't members. Also make sure that the
	 * input can be passed in as storage for the output without changing the results
	 */
	@Test
	public void withRandomPoints_sameInstance() {
		PlaneNormal3D_F64 plane = new PlaneNormal3D_F64(1, 2, 3, -0.5, 0.25, 1);
		PlaneGeneral3D_F64 planeFound = UtilPlane3D_F64.convert(plane, null);
		// make the initial estimate slightly off
		planeFound.A = 1.01;

		// create a mixture of perfect and noisy points
		List<PointVectorNN> pts = new ArrayList<PointVectorNN>();
		for (int i = 0; i < 100; i++) {
			double x = (rand.nextDouble() - 0.5) * 5;
			double y = (rand.nextDouble() - 0.5) * 5;
			pts.add(TestGeneratePlanePointVector.createPt(plane, x, y, 1));
		}
		for (int i = 0; i < 15; i++) {
			double x = (rand.nextDouble() - 0.5) * 5;
			double y = (rand.nextDouble() - 0.5) * 5;
			PointVectorNN pv = TestGeneratePlanePointVector.createPt(plane, x, y, 1);

			// make the points really far away from the plane
			pv.p.x += rand.nextGaussian() * 3 + 5;
			pv.p.y += rand.nextGaussian() * 3 + 5;
			pv.p.z += rand.nextGaussian() * 3 + 5;
			pts.add(pv);
		}

		// this is the set of initial points which is passed in
		List<PointVectorNN> guessPts = new ArrayList<PointVectorNN>();
		for (int i = 0; i < 30; i++) {
			guessPts.add(pts.get(i));
		}

		createGraph(pts);

		findMatch.reset();
		LocalFitShapeNN<PlaneGeneral3D_F64> alg = new LocalFitShapeNN<PlaneGeneral3D_F64>(100, 1e-8, findMatch);

		alg.configure(modelFitter, modelDistance, modelCheck, modelCodec, 0.3);

		assertTrue(alg.refine(guessPts, planeFound, true));

		// should be an easy enough case that it filters out all the bad points
		assertEquals(100, guessPts.size());
		TestGeneratePlanePointVector.checkPlanes(plane, planeFound, 1e-8);
	}

	/**
	 * Create a NN graph from the points
	 */
	private void createGraph(List<PointVectorNN> cloud) {
		NearestNeighbor<PointVectorNN> nn = FactoryNearestNeighbor.kdtree();

		List<double[]> pointsD = new ArrayList<double[]>();

		// convert the point cloud into the NN format
		for (int i = 0; i < cloud.size(); i++) {
			PointVectorNN p = cloud.get(i);

			double[] d = new double[3];

			d[0] = p.p.x;
			d[1] = p.p.y;
			d[2] = p.p.z;

			pointsD.add(d);
		}

		nn.init(3);
		nn.setPoints(pointsD, cloud);

		FastQueue<NnData<PointVectorNN>> neighbors = new FastQueue<NnData<PointVectorNN>>((Class) NnData.class, true);

		for (int i = 0; i < cloud.size(); i++) {
			PointVectorNN p = cloud.get(i);
			double[] d = pointsD.get(i);

			neighbors.reset();
			nn.findNearest(d, 2, 8, neighbors);

			for (int j = 0; j < neighbors.size; j++) {
				PointVectorNN pv = neighbors.get(j).data;
				if (pv == p)
					continue;
				p.neighbors.add(pv);
			}
		}
	}

}
