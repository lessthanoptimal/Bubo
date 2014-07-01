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

package bubo.simulation.d2.sensors;

import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.transform.se.SePointOps_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.Random;

/**
 * Simulates a range-bearing sensor in 2D
 *
 * @author Peter Abeles
 */
public class SimRangeBearingMeasurement {

	// random number generator for adding noise
	private Random rand;

	// maximum range that the sensor works
	private double maximumRange;

	// noise magnitude
	private double rangeSigma;
	private double bearingSigma;

	// true observation
	private double trueRange;
	private double trueBearing;

	// noisy
	private double noisyRange;
	private double noisyBearing;

	// transform from world frame into the robot frame
	private Se2_F64 worldToSensor = new Se2_F64();
	private Point2D_F64 landmarkInSensorFrame = new Point2D_F64();

	public SimRangeBearingMeasurement(double rangeSigma, double bearingSigma,
									  double maximumRange, long randSeed) {
		this.rand = new Random(randSeed);
		this.rangeSigma = rangeSigma;
		this.bearingSigma = bearingSigma;
		this.maximumRange = maximumRange;
	}

	/**
	 * Specify the sensor's pose.
	 *
	 * @param sensorToWorld Transform from, sensor reference frame to world reference frame.
	 */
	public void setSensorPose(Se2_F64 sensorToWorld) {
		sensorToWorld.invert(worldToSensor);
	}

	/**
	 * Computes a simulated observation to the specified landmark
	 *
	 * @param landmark Location of the landmark in global coordinates.
	 * @return true if the landmark was observed.
	 */
	public boolean process(Point2D_F64 landmark) {
		SePointOps_F64.transform(worldToSensor, landmark, landmarkInSensorFrame);

		trueRange = landmarkInSensorFrame.norm();
		trueBearing = Math.atan2(landmarkInSensorFrame.y, landmarkInSensorFrame.x);

		// add noise
		noisyRange = trueRange + rand.nextGaussian() * rangeSigma;
		noisyBearing = trueBearing + rand.nextGaussian() * bearingSigma;

		// apply very basic physical constraints and numerical bounds
		if (noisyRange < 0) noisyRange = 0;
		noisyBearing = UtilAngle.bound(noisyBearing);

		// use the noisy range to decide if it can be seen or not so that there isn't a hard threshold for visibility
		return noisyRange <= maximumRange;
	}

	public double getTrueBearing() {
		return trueBearing;
	}

	public double getTrueRange() {
		return trueRange;
	}

	public double getNoisyBearing() {
		return noisyBearing;
	}

	public double getNoisyRange() {
		return noisyRange;
	}

	public DenseMatrix64F getMeasurementCovariance() {
		return CommonOps.diag(rangeSigma * rangeSigma, bearingSigma * bearingSigma);
	}
}
