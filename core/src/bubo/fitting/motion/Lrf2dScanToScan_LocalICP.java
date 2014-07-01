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
import bubo.fitting.StoppingCondition;
import bubo.fitting.motion.general.GeneralizedScanToScan;
import bubo.fitting.motion.general.LocalAssociateDiscrete;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;


/**
 * <p>
 * Implementation of {@link IterativeClosestPoint ICP} which has been specialized for scans from a 2D
 * laser rangefinder (LRF).  Potential associations are only considered in a window around a
 * scan based on its index.  No interpolation is performed.
 * </p>
 *
 * @author Peter Abeles
 */
public class Lrf2dScanToScan_LocalICP extends GeneralizedScanToScan {

	// code for associating points
	private DistanceAssociate assoc;

	/**
	 * Constructor.
	 *
	 * @param stop              Optimization stopping condition.
	 * @param associationRadius How many indexes around the current
	 * @param maxSeparation     Maximum distance two points can be for them to be associated.
	 */
	public Lrf2dScanToScan_LocalICP(StoppingCondition stop, int associationRadius, double maxSeparation) {
		super(stop);

		assoc = new DistanceAssociate(associationRadius, maxSeparation * maxSeparation);

	}

	@Override
	public void setSensorParam(Lrf2dParam param) {
		super.setSensorParam(param);
		assoc.setParam(param);
	}

	@Override
	protected Se2_F64 estimateMotion() {
		return computeMotion(assoc);
	}

	/**
	 * Associates points based on cartesian distance.
	 */
	protected class DistanceAssociate extends LocalAssociateDiscrete {
		Point2D_F64 fromPt;

		protected DistanceAssociate(int searchNeighborhood, double maxSeparation) {
			super(searchNeighborhood, maxSeparation);
		}

		@Override
		public void setTarget(int indexFrom) {
			fromPt = scanMatch.pts[indexFrom];
		}

		@Override
		public double distToTarget(int indexTo) {
			return scanRef.pts[indexTo].distance2(fromPt);
		}

	}
}
