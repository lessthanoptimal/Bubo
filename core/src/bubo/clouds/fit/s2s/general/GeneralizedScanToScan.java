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

package bubo.clouds.fit.s2s.general;

import bubo.clouds.fit.Lrf2dScanToScan;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.desc.sensors.lrf2d.Lrf2dPrecomputedTrig;
import bubo.struct.StoppingCondition;
import georegression.fitting.se.MotionSe2PointSVD_F64;
import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.transform.se.SePointOps_F64;

import java.util.List;


/**
 * <p>
 * Base class for scan-to-scan matching which automatically precomputes information which most 2D-LRF scan-to-scan
 * matching algorithms would need.  Simplifies the creating of algorithms at the cost of some performance.  Allows
 * for global or local association as well as interpolation.
 * </p>
 *
 * @author Peter Abeles
 */
public abstract class GeneralizedScanToScan implements Lrf2dScanToScan {

	// description of the sensor
	protected Lrf2dParam param;
	// decides when to stop iterating
	protected StoppingCondition stop;
	// original scan measurements
	protected double rangesFrom[];
	// various bits of information related to each scan
	protected ScanInfo scanTo; // the reference scan being registered to
	protected ScanInfo scanFrom; // the scan which is being registered
	// speeds up calculations
	private Lrf2dPrecomputedTrig lrf2pt;
	// the found total motion
	private Se2_F64 motion = new Se2_F64();
	// the final error
	private double foundError;

	// given associated points computes rigid body motion
	private MotionSe2PointSVD_F64 motionAlg = new MotionSe2PointSVD_F64();

	private List<Point2D_F64> fromPts;
	private List<Point2D_F64> toPts;

	public GeneralizedScanToScan(StoppingCondition stop) {

		this.stop = stop;
	}

	@Override
	public void setSensorParam(Lrf2dParam param) {
		this.param = param;
		this.lrf2pt = new Lrf2dPrecomputedTrig(param);

		scanTo = new ScanInfo(param.getNumberOfScans());
		scanFrom = new ScanInfo(param.getNumberOfScans());
		rangesFrom = new double[param.getNumberOfScans()];
	}

	@Override
	public Se2_F64 getMotion() {
		return motion;
	}

	@Override
	public void setReference(double[] scan) {
		System.arraycopy(scan, 0, scanTo.range, 0, param.getNumberOfScans());
	}

	@Override
	public void setMatch(double[] scan) {
		System.arraycopy(scan, 0, scanFrom.range, 0, param.getNumberOfScans());
	}

	public void computeScanEndPoint(double scan[], Point2D_F64 pts[]) {
		final int N = param.getNumberOfScans();
		final double maxRange = param.getMaxRange();
		for (int i = 0; i < N; i++) {
			double r = scan[i];

			if (r <= maxRange) { // todo less than range?
				lrf2pt.computeEndPoint(i, r, pts[i]);
			}
		}
	}

	@Override
	public void setMatchToReference() {
		ScanInfo temp = scanTo;
		scanTo = scanFrom;
		scanFrom = temp;
	}

	@Override
	public boolean process(Se2_F64 hint) {
		// save the original ranges
		System.arraycopy(scanFrom.range, 0, rangesFrom, 0, param.getNumberOfScans());
		// find the obstacle location
		computeScanEndPoint(scanFrom.range, scanFrom.pts);
		computeScanEndPoint(scanTo.range, scanTo.pts);

		// apply the hit if any
		if (hint != null) {
			transform(hint, scanFrom);
			motion.set(hint);
		} else
			motion.set(0, 0, 0);

		setVisibleByRange(scanTo); // todo could just do this when end point is computed

		stop.reset();
		while (true) {
			// compute the angle of each point in the current view
			projectScan(rangesFrom, scanFrom);

			// angle based visibility test
			checkVisibleByDeltaAngle(scanFrom);

			// find the motion which minimizes the error between the two scans
			Se2_F64 foundMotion = estimateMotion();

			// apply the transform to the points in the scan being matched
			transform(foundMotion, scanFrom);
			foundError = computeMeanSquaredError();

			// increment
			motion = motion.concat(foundMotion, null);

			if (stop.isFinished(foundError))
				break;
		}

		// undo the recomputed ranges
		System.arraycopy(rangesFrom, 0, scanFrom.range, 0, param.getNumberOfScans());

		return true;
	}

	/**
	 * Function which estimates the
	 *
	 * @return
	 */
	protected abstract Se2_F64 estimateMotion();

	/**
	 * Sets visibility depending on the measured range being less than the max range.
	 */
	private void setVisibleByRange(ScanInfo info) {
		final int N = param.getNumberOfScans();
		final double maxRange = param.getMaxRange();
		for (int i = 0; i < N; i++) {
			scanTo.vis[i] = info.range[i] <= maxRange;
		}
	}

	/**
	 * Computes the angle of each scan and flags visible based on measured range
	 */
	protected void projectScan(double measuredRange[], ScanInfo info) {
		final int N = param.getNumberOfScans();
		final double maxRange = param.getMaxRange();

		for (int i = 0; i < N; i++) {
			double r = measuredRange[i];

			if (r <= maxRange) {
				Point2D_F64 p = info.pts[i];
				info.theta[i] = Math.atan2(p.y, p.x);
				info.range[i] = p.norm();
				info.vis[i] = true;
			} else {
				info.range[i] = maxRange;
				info.vis[i] = false;
			}
		}
	}

	/**
	 * <p>
	 * Each angle should always be increasing or decreasing.  If this order is broken
	 * at any point then the point is not visible from the current point of view.
	 * </p>
	 * <p/>
	 * <p>
	 * See section 3.2 of: Feng Lu and Evangelos Milios, "Robot Pose Estimation in Unknown Environments by Matching 2D Range Scans"
	 * Journal of Intelligent and Robotics Systems, 18: 249-275, 1997.
	 * </p>
	 */
	protected void checkVisibleByDeltaAngle(ScanInfo info) {
		final int N = param.getNumberOfScans();
		boolean increasing = param.getAngleIncrement() > 0;
		double ang[] = info.theta;

		for (int i = 1; i < N; i++) {
			if (!info.vis[i])
				continue;

			double deltaAng = UtilAngle.minus(ang[i], ang[i - 1]);
			if (increasing) {
				if (deltaAng < 0)
					info.vis[i] = false;
			} else {
				if (deltaAng > 0)
					info.vis[i] = false;
			}
		}
	}

	/**
	 * Computes motion by associating points using the provided distance function then
	 * computing the Se2_F64 transform.
	 *
	 * @return found motion
	 */
	protected Se2_F64 computeMotion(AssociateLrfMeas assoc) {
		assoc.associate(scanFrom, scanTo);

		fromPts = assoc.getListMatch();
		toPts = assoc.getListReference();

		motionAlg.process(fromPts, toPts);

		return motionAlg.getTransformSrcToDst();
	}


	private void transform(Se2_F64 m, ScanInfo scan) {
		for (Point2D_F64 p : scan.pts) {
			SePointOps_F64.transform(m, p, p);
		}
	}

	/**
	 * Returns the error between associated scan points as the mean squared error.
	 * In other words the sum of the Euclidean distance squared divided by the number of points.
	 *
	 * @return the error
	 */
	private double computeMeanSquaredError() {
		double error = 0;

		for (int i = 0; i < fromPts.size(); i++) {
			Point2D_F64 f = fromPts.get(i);
			Point2D_F64 t = toPts.get(i);

			error += f.distance2(t);
		}

		return error / fromPts.size();
	}

	@Override
	public double getError() {
		return foundError;
	}

}
