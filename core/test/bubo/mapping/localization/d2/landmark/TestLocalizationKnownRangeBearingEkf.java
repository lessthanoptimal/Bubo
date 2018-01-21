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

package bubo.mapping.localization.d2.landmark;

import bubo.desc.sensors.landmark.RangeBearingMeasurement;
import bubo.desc.sensors.landmark.RangeBearingParam;
import bubo.mapping.models.kinematics.LocalMotion2D;
import bubo.mapping.models.kinematics.PredictorLocalMotion2D;
import bubo.maps.d2.LandmarkMap2D;
import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLocalizationKnownRangeBearingEkf {

	RangeBearingParam param = new RangeBearingParam(20,0.75,0.15);

	Random rand = new Random(234);

	/**
	 * Provide 1 to N landmarks and see if the state estimate error decreases
	 */
	@Test
	public void landmarksImproveState() {

		LandmarkMap2D map = new LandmarkMap2D();

		// test it with various numbers of landmarks
		for (int i = 1; i <= 20; i++) {

			PredictorLocalMotion2D predictor = new PredictorLocalMotion2D(0.1, 0.01, 0.1);
			LocalizationKnownRangeBearingEkf alg = new LocalizationKnownRangeBearingEkf(predictor, param);

			CommonOps_DDRM.diag(alg.getState().getCovariance(), 3, 10000, 10000, 100);
			// due to linearization it can't handle a huge error in initial angle
			Se2_F64 truth = new Se2_F64(0.5, 1, 0.25);

			// add one more landmark to the map
			map.add( rand.nextDouble()*20-10, rand.nextDouble()*20-10);

			alg.setLandmarks(map);

			double errorL = alg.getPose().T.distance(truth.T);
			double errorO = UtilAngle.dist(alg.getPose().getYaw(), truth.getYaw());

			for (int id = 0; id < map.getTotal(); id++) {
				Point2D_F64 l = map.getLocation(id);

				double dx = l.x - truth.T.x;
				double dy = l.y - truth.T.y;
				double theta = Math.atan2(dy, dx);

				double r = Math.sqrt(dx * dx + dy * dy);
				double bearing = UtilAngle.minus(theta, truth.getYaw());

				// give it perfect measurements
				alg.update(new RangeBearingMeasurement(id, r, bearing));
			}

			// some times the location can get slightly worse.  so a combination
			// of both error metrics is used
			double foundL = alg.getPose().T.distance(truth.T);
			double foundO = UtilAngle.dist(alg.getPose().getYaw(), truth.getYaw());

			double score = (foundL/errorL)*(foundO/errorO);

			assertTrue(score<1.0);
		}
	}

	@Test
	public void predictionIncreasesUncertainty(){
		PredictorLocalMotion2D predictor = new PredictorLocalMotion2D(0.1, 0.01, 0.1);
		LocalizationKnownRangeBearingEkf<LocalMotion2D> alg = new LocalizationKnownRangeBearingEkf<LocalMotion2D>(predictor, param);

		CommonOps_DDRM.diag(alg.getState().getCovariance(), 3, 100, 100, 100);

		double before = NormOps_DDRM.normF(alg.getState().getCovariance());

		alg.predict(new LocalMotion2D(300,200,0.1));

		double after = NormOps_DDRM.normF(alg.getState().getCovariance());

		assertTrue(after>before);
	}
}