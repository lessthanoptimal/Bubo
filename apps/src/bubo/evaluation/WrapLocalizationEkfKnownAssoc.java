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

package bubo.evaluation;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.ekf.TimeToDiscrete;
import bubo.localization.d2.landmark.LocalizationEkfKnownAssoc;
import bubo.models.kinematics.PredictorRobotVelocity2D;
import bubo.models.sensor.LandmarkProjector;
import bubo.simulation.d2.sensors.RangeBearing;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import org.ejml.data.DenseMatrix64F;

import java.util.List;

/**
 * Wrapper around {@link PredictorRobotVelocity2D}
 *
 * @author Peter Abeles
 */
public class WrapLocalizationEkfKnownAssoc implements LocalizationAlgorithm {

	LocalizationEkfKnownAssoc alg;
	PredictorRobotVelocity2D predictor;

	// contains sensor measurement
	MultivariateGaussianDM r = new MultivariateGaussianDM(2);

	// the estimate robot pose
	Se2_F64 pose = new Se2_F64();

	public WrapLocalizationEkfKnownAssoc( PredictorRobotVelocity2D predictor , LandmarkProjector projector , double T ) {
		this.predictor = predictor;

		alg = new LocalizationEkfKnownAssoc(new TimeToDiscrete(predictor,T),projector);
	}

	@Override
	public void configure(List<Point2D_F64> landmarks, DenseMatrix64F R) {
		r.getCovariance().set(R);

		alg.setLandmarks(landmarks);
	}

	@Override
	public void setInitial(Se2_F64 pose, double sigmaXY, double sigmaYaw) {

		MultivariateGaussianDM state = new MultivariateGaussianDM(3);
		DenseMatrix64F X = state.getMean();
		DenseMatrix64F P = state.getCovariance();

		X.data[0] = pose.getX();
		X.data[1] = pose.getY();
		X.data[2] = pose.getYaw();

		P.set(0,0,sigmaXY*sigmaXY);
		P.set(1,1,sigmaXY*sigmaXY);
		P.set(2,2,sigmaYaw*sigmaYaw);

		alg.setInitialPose(state);
	}

	@Override
	public void predict(double vel, double angVel) {
		predictor.setControl(vel,angVel);
		alg.predict();
	}

	@Override
	public void update(List<RangeBearing> measNoisy, List<RangeBearing> measTrue) {
		for( RangeBearing m : measNoisy ) {

			r.x.set(0, m.r);
			r.x.set(1, m.bearing);

			alg.update((int)m.id,r);
		}
	}

	@Override
	public Se2_F64 getPoseEstimate() {
		DenseMatrix64F X = alg.getState().getMean();

		pose.set(X.data[0],X.data[1],X.data[2]);

		return pose;
	}
}
