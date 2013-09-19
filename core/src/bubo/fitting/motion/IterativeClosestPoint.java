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

import bubo.fitting.StoppingCondition;
import georegression.fitting.MotionTransformPoint;
import georegression.struct.GeoTuple_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.se.Se3_F64;
import georegression.struct.se.SpecialEuclidean;
import georegression.transform.se.SePointOps_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A straight forward implementation of the Iterative Closest Point (ICP) algorithm for 2D or 3D objects.  ICP
 * works by finding a locally optimal rigid body transform that minimizes the error between a set of points
 * and a model.  The model can be described in several different formats and is implement as an interface
 * {@link ClosestPointToModel}.
 * </p>
 *
 * <p>
 * While this implementation is primarily designed for simplicity, generic, and correctness, its performance will
 * be primarily determined by the efficiency of the ClosestPointToModel provided to it.  This is especially
 * true for complex models with large number of points.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class IterativeClosestPoint<SE extends SpecialEuclidean , T extends GeoTuple_F64> {

	// stopping condition
	private StoppingCondition stop;

	// the mean squared error
	private double foundError;

	private ClosestPointToModel<T> model;
	private MotionTransformPoint<SE,T> motion;

	// transform from the original point location to their current one
	private SE foundMotion;

	public IterativeClosestPoint( StoppingCondition stop,
								  MotionTransformPoint<SE, T> motion) {
		this.stop = stop.copy();
		this.motion = motion;
	}

	/**
	 * Mean square error between the model and the set of points after the optimal transformation has been found
	 */
	public double getFoundError() {
		return foundError;
	}

	/**
	 * Rigid body transformation between the provided points and the model.
	 */
	public SE getMotion() {
		return foundMotion;
	}

	/**
	 * The model that the input points is being fitted against.
	 */
	public void setModel( ClosestPointToModel model ) {
		this.model = model;
	}

	/**
	 * Computes the best fit transform
	 * @param points Points which are to matched to a model.  Their state is modified to the optimal fit location.
	 */
	public void process( List<T> points ) {
		foundMotion = null;
		if( points.isEmpty() ) {
			return;
		}

		int dof = points.get(0).getDimension();

		List<T> fromPts = new ArrayList<T>();
		List<T> toPts = new ArrayList<T>();

		stop.reset();
		while( true) {
			// find correspondences
			fromPts.clear();
			toPts.clear();
			for( T p : points ) {
				T match = model.findClosestPoint(p);
				if( match != null ) {
					fromPts.add(p);
					toPts.add(match);
				}
			}

			// from the optimal transform
			motion.process(fromPts,toPts);

			if( dof == 2 ) {
				transform2D( (List<Point2D_F64>)points );
			} else if( dof == 3 ) {
				transform3D( (List<Point3D_F64>)points );
			} else {
				throw new RuntimeException("Unknown dimension");
			}

			// sum up all the transforms up to this point
			if( foundMotion == null ) {
				foundMotion = (SE)motion.getMotion().createInstance();
				foundMotion.set(motion.getMotion());
			} else {
				// the returned transform is the result of the sequence of transforms.
				foundMotion = (SE)motion.getMotion().concat(foundMotion,null);
			}

			// compute mean squared error
			foundError = computeMeanSquaredError(fromPts, toPts);

			if( stop.isFinished(foundError))
				break;
		}
	}

	private double computeMeanSquaredError(List<T> fromPts, List<T> toPts) {
		double error = 0;
		for( int i = 0; i < fromPts.size(); i++ ) {
			T a = fromPts.get(i);
			T b = toPts.get(i);

			error += a.distance2(b);
		}
		error /= fromPts.size();
		return error;
	}

	private void transform3D( List<Point3D_F64> points ) {
		Se3_F64 m = (Se3_F64)motion.getMotion();

		for( Point3D_F64 p : points ) {
			SePointOps_F64.transform(m,p,p);
		}
	}

	private void transform2D( List<Point2D_F64> points ) {
		Se2_F64 m = (Se2_F64)motion.getMotion();

		for( Point2D_F64 p : points ) {
			SePointOps_F64.transform(m,p,p);
		}
	}
}
