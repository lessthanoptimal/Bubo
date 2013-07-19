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

package bubo.fitting.motion;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.fitting.StoppingCondition;
import bubo.fitting.motion.general.GeneralizedScanToScan;
import bubo.fitting.motion.general.LocalAssociateInterpolate;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;

/**
 * <p>
 * Implementation of Iterative Dual Correspondence (IDC) algorithm for matching two scans from a 2D laser rangefinder
 * (LRF) using rigid body motion.  Works by using ICP {@link IterativeClosestPoint} and a range matching technique
 * to find the translation and rotation.
 * </p>
 *
 * <p>
 * Association is performed by considering a local search based on scan angle.  The search window decays with each
 * iteration.  Interpolation is performed between scan points based upon the angle.  Interpolation is performed
 * differently for the two types of association performed.  Association is done based on Cartesian distance of the
 * scan's end points and each scan's range.  The former is supposed to be better and translational estimates while
 * the latter at estimating angular changes.  Thus the outputted motion takes the translation from Cartesian association
 * and rotation from range association.
 * </p>
 *
 * <p>
 * Scan points are ignored if they are invalid measurements (e.g. at the max range) or can not be seen from the new
 * point of view being considered.  This is done by searching for scan angles that are out of sequence.  Unlike in the
 * original paper no change is done to see if a scan is blocked from view by another scan as a new view is considered.
 * </p>
 *
 * <p>
 * NOTE: Range based association is prone to ambiguous association.  For example, when viewing the corner of a room two equally
 * score hypotheses exist and one will be randomly selected.  This is some what mitigated by only searching a local
 * area, but when the robot is right up against the corner the local search will not be enough.  The end result is that
 * under some fairly common situations this produces a worse estimate than ICP.
 * </p>
 *
 * <p>
 * Based upon: Feng Lu and Evangelos Milios, "Robot Pose Estimation in Unknown Environments by Matching 2D Range Scans"
 * Journal of Intelligent and Robotics Systems, 18: 249-275, 1997.
 * </p>
 *
 * @author Peter Abeles
 */
public class Lrf2dScanToScan_IDC extends GeneralizedScanToScan {

    // association by Euclidean distance.  Used when estimating translation
    AssociateDistance assocCartesian = new AssociateDistance();
    // association by measured Range.  Used when estimating rotation.
    AssociateRange assocRange = new AssociateRange();

    // how many radians around the current angle will it search
    double searchNeighborhood;

    // sets the rate of decay for neighborhood size
    double windowDecayConstant = 0.1;

    public Lrf2dScanToScan_IDC(StoppingCondition stop, double searchNeighborhood, double maxSeparation,
                               double radiusDecayConstant) {
        super(stop);
        this.searchNeighborhood = searchNeighborhood;
        this.windowDecayConstant = radiusDecayConstant;

        assocCartesian.setMaxSeparation(maxSeparation);
        assocRange.setMaxSeparation(maxSeparation);
    }

    @Override
    public void setSensorParam(Lrf2dParam param ) {
        super.setSensorParam(param);

        assocCartesian.setParam(param);
        assocRange.setParam(param);

        double inc = Math.abs(param.getAngleIncrement());
        assocCartesian.setSamplePeriod(inc);
        assocRange.setSamplePeriod(inc);
    }

    /**
     * <p>
     * Estimates motion in two steps differentiated by the association technique.  First
     * points are associated by Euclidean distance and the motion estimated.  Next the motion is estimated
     * again by associating with measured range.  The final output found by using the estimated translation
     * from Euclidean distance association and angular change from range association.
     * </p>
     *
     * <p>
     * Local association is performed with interpolation.  The association search window decays with each iteration.
     * </p>
     *
     * @return Estimated motion.
     */
    @Override
    protected Se2_F64 estimateMotion() {
        // reduce the search window size for each iteration
        double windowRadius = searchNeighborhood*Math.exp(-windowDecayConstant *stop.getIteration());
        assocCartesian.setSearchNeighborhood(windowRadius);
        assocRange.setSearchNeighborhood(windowRadius);

        // find translational component
        Se2_F64 m = computeMotion(assocCartesian);
//        System.out.println(m);

        double dx = m.getX();
        double dy = m.getY();

        // find angular component
        m = computeMotion(assocRange);
//        System.out.println(m);

        m.set(dx,dy,m.getYaw());

        return m;
    }

    /**
     * Associates using Euclidean distance between two points.  This is supposed to be better at estimating the translation.
     */
    protected class AssociateDistance extends LocalAssociateInterpolate
    {
        Point2D_F64 from;

        @Override
        public boolean interpolate(InterpolatedPoint point) {
            final int N = param.getNumberOfScans();

            int index = (int)(N*(point.angle - param.getStartAngle())/param.getSweepAngle());

            if( index >= N-1 || index < 0 )
                return false;

            if( scanRef.vis[index] && scanRef.vis[index+1]) {
                double before = param.computeAngle(index);
                double after = param.computeAngle(index+1);

                Point2D_F64 b = scanRef.pts[index];
                Point2D_F64 a = scanRef.pts[index+1];

                double frac = (point.angle - before)/(after-before);
                point.point.x = frac*(a.x-b.x)+b.x;
                point.point.y = frac*(a.y-b.y)+b.y;
                point.range = point.point.normSq();

                return true;
            } else if( scanRef.vis[index] ) {
                point.point.set( scanRef.pts[index]);
                point.range = point.point.normSq();
                return true;
            } else if( scanRef.vis[index+1] ) {
                point.point.set( scanRef.pts[index+1]);
                point.range = point.point.normSq();
                return true;
            }

            return false;
        }

        @Override
        public void setTarget(int indexFrom) {
            from = scanMatch.pts[indexFrom];
        }

        @Override
        public double distToTarget(InterpolatedPoint point) {
            return from.distance(point.point);
        }
    }

    /**
     * Associate by range.  This is supposed to be better at estimating the angular change the by Euclidean distance.
     * However it is more prone to ambiguous associations.
     */
    protected class AssociateRange extends LocalAssociateInterpolate
    {
        double reference;

        @Override
        public boolean interpolate(InterpolatedPoint point) {
            final int N = param.getNumberOfScans();

            int index = (int)(N*(point.angle - param.getStartAngle())/param.getSweepAngle());

            if( index >= N || index < 0 )
                return false;
			else if( index == N-1 ) {
				point.range = scanRef.range[index];
			} else {
				if( scanRef.vis[index] && scanRef.vis[index+1]) {
					double o1 = param.computeAngle(index);
					double o2 = param.computeAngle(index+1);

					double r1 = scanRef.range[index];
					double r2 = scanRef.range[index+1];

					point.range = (r1*r2*(o2-o1))/(r1*(point.angle-o1) + r2*(o2-point.angle));
				} else if( scanRef.vis[index] ) {
					point.range = scanRef.range[index];
				} else if( scanRef.vis[index+1] ) {
					point.range = scanRef.range[index+1];
				} else {
					return false;
				}
			}

			point.point.x = Math.cos(point.angle)*point.range;
			point.point.y = Math.sin(point.angle)*point.range;

            return true;
        }

        @Override
        public void setTarget(int indexFrom) {
            reference = scanMatch.range[indexFrom];
        }

        @Override
        public double distToTarget(InterpolatedPoint point) {
            double d = point.range-reference;
            return d*d;
        }
    }
}
