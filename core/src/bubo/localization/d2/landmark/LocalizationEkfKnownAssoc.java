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

package bubo.localization.d2.landmark;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.ekf.EkfPredictorDiscrete;
import bubo.filters.ekf.ExtendedKalmanFilter;
import bubo.models.sensor.LandmarkProjector;
import georegression.struct.point.Point2D_F64;

import java.util.List;

/**
 * Straight forward implementation of localization using an EKF which assumes perfect association and a known set
 * landmarks with known perfect locations.
 *
 * @author Peter Abeles
 */
public class LocalizationEkfKnownAssoc {

	// processes landmark observations
	private LandmarkProjector projector;
	// State estimation filter
	private ExtendedKalmanFilter ekf;
	// list of landmarks
	private List<Point2D_F64> landmarks;

	// Robot's pose estimate
	private MultivariateGaussianDM state = new MultivariateGaussianDM(3);

	public LocalizationEkfKnownAssoc(EkfPredictorDiscrete predictor,
									 LandmarkProjector projector ) {
		if( predictor.getSystemSize() != 3 )
			throw new IllegalArgumentException("Predictor: Expecting a state size of 3, (x,y,yaw)");
		if( projector.getSystemSize() != 3 )
			throw new IllegalArgumentException("Projector: Expecting a state size of 3, (x,y,yaw)");

		this.projector = projector;

		ekf = new ExtendedKalmanFilter(predictor,projector);
	}

	public void setLandmarks(List<Point2D_F64> landmarks) {
		this.landmarks = landmarks;
	}

	/**
	 * Specifies the initial state of the robot and the confidence
	 * @param state Initial filter state
	 */
	public void setInitialPose( MultivariateGaussianDM state ) {
		this.state.set(state);
	}

	/**
	 * Predicts the robot's next pose and update its uncertainty.  Changes to the roboto's control
	 * must be done to the predictor directly before invoking this function.
	 */
	public void predict() {
		ekf.predict(state);
	}

	/**
	 * Updates the filter given an observation of the specified landmark
	 * @param landmarkID index of which landmark has been observed
	 * @param measurement The observation
	 */
	public void update( int landmarkID , MultivariateGaussianDM measurement  ) {
		Point2D_F64 p = landmarks.get(landmarkID);
		projector.setLandmarkLocation(p.x,p.y);

		ekf.update(state,measurement);
	}

	/**
	 * The estimated robot pose
	 * @return Robot pose estimate
	 */
	public MultivariateGaussianDM getState() {
		return state;
	}
}
