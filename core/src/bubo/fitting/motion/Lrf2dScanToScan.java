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

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import georegression.struct.se.Se2_F64;

/**
 * Computes a rigid body motion {@link Se2_F64} which when applied to the 'match' scan will minimize the difference
 * between it and the 'reference' scan.
 *
 * @author Peter Abeles
 */
public interface Lrf2dScanToScan {

	/**
	 * Specifies the sensors intrinsic characteristics.
	 *
	 * @param param Sensor parameters.
	 */
	public void setSensorParam(Lrf2dParam param);

	/**
	 * If {@link #process(georegression.struct.se.Se2_F64)} returned true then this is the motion from the first to second scan.
	 *
	 * @return The found motion.
	 */
	public Se2_F64 getMotion();

	/**
	 * Specifies range measurements for the reference scan.
	 *
	 * @param scan range measurements.
	 */
	public void setReference(double[] scan);

	/**
	 * Specifies range measurements for the scan which is to be matched against the reference.
	 *
	 * @param scan range measurements
	 */
	public void setMatch(double[] scan);

	/**
	 * Takes the data associated with the match scan and sets it to be the reference scan.  Often times scan matching
	 * is done on a sequence of data and what was the second scan will become the first scan when new data arrives.
	 * This method is intended to avoid unnecessary recomputing or copying of data.
	 */
	public void setMatchToReference();

	/**
	 * Finds the motion which minimizes the error first and second scan.
	 *
	 * @param hint An initial estimate of the transform from the first to second scan.  Often from odometry.
	 * @return if registration was successful or not.
	 */
	public boolean process(Se2_F64 hint);

	/**
	 * A number which represents the error associated with the scan.  Primarily for debugging purposes,
	 * its meaning is implementation dependent.
	 *
	 * @return Error associated with the scan registration
	 */
	public double getError();
}
