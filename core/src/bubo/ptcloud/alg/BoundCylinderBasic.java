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

package bubo.ptcloud.alg;

import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;

import java.util.List;

/**
 * Given an equation for a cylinder and a set of points which belong to the cylinder, bound the length of the
 * cylinder.  The bound is found by projecting points into the cylinder's center line then finding
 * the minimum and maximum value of the points in the direction of the line.
 *
 * @author Peter Abeles
 */
public class BoundCylinderBasic {

	// the upper and lower bounds on the cylinder
	double upper;
	double lower;

	public void bound( Cylinder3D_F64 cylinder , List<Point3D_F64> points ) {

		upper = -Double.MAX_VALUE;
		lower = Double.MAX_VALUE;

		// origin of cylinder coordinate system
		Point3D_F64 origin = cylinder.line.getPoint();

		double n = cylinder.line.slope.norm();
		double nx = cylinder.line.slope.x/n;
		double ny = cylinder.line.slope.y/n;
		double nz = cylinder.line.slope.z/n;

		for( int i = 0; i < points.size(); i++ ) {
			Point3D_F64 pt = points.get(i);

			// compute the distance which the point is along the line from the cylinder's origin
			double dx = pt.x - origin.x;
			double dy = pt.y - origin.y;
			double dz = pt.z - origin.z;

			double d = nx*dx + ny*dy + nz*dz;

			upper = Math.max(d,upper);
			lower = Math.min(d,lower);
		}
	}

	public double getUpper() {
		return upper;
	}

	public double getLower() {
		return lower;
	}
}
