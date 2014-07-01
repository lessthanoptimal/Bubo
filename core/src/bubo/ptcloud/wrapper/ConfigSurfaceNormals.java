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

package bubo.ptcloud.wrapper;

/**
 * Configuration for {@link bubo.ptcloud.alg.ApproximateSurfaceNormals}.
 *
 * @author Peter Abeles
 */
public class ConfigSurfaceNormals {
	/**
	 * Number of closest neighbors it will use to estimate the plane
	 */
	public int numPlane;

	/**
	 * The maximum distance a point can be when computing the plane.
	 */
	public double maxDistancePlane = Double.MAX_VALUE;
	/**
	 * Number of neighbors it will use to approximate normal.  Can be useful to set to a higher number if
	 * nearest-neighbor graph is used by other algorithms
	 */
	public int numNeighbors;
	/**
	 * The maximum distance apart two points can be for them to be neighbors.  By default this is set to
	 * {@link Double#MAX_VALUE}.
	 */
	public double maxDistanceNeighbor = Double.MAX_VALUE;

	public ConfigSurfaceNormals(int numPlane, int numNeighbors, double maxDistanceNeighbor) {
		this.numPlane = numPlane;
		this.numNeighbors = numNeighbors;
		this.maxDistanceNeighbor = maxDistanceNeighbor;
	}

	public void checkConfig() {
		if (numPlane <= 3)
			throw new IllegalArgumentException("A minimum of three points must be uesd to fit a plane");
		if (numNeighbors < numPlane)
			throw new IllegalArgumentException("The number of neighbors must be >= the number used by the plane");
		if (maxDistancePlane <= maxDistanceNeighbor)
			throw new IllegalArgumentException("maxDistancePlane must be less than or equal to maxDistanceNeighbor");
	}
}

