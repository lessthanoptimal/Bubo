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

package bubo.ptcloud;

import georegression.geometry.GeometryMath_F64;
import georegression.metric.ClosestPoint3D_F64;
import georegression.metric.Distance3D_F64;
import georegression.metric.UtilAngle;
import georegression.struct.line.LineParametric3D_F64;
import georegression.struct.shapes.Cylinder3D_F64;
import org.ddogleg.fitting.modelset.ModelGenerator;

import java.util.List;

/**
 * Cylinder estimation for use in {@link RansacMultiObjectPointCloud}.  The sphere is estimated
 * using two points and their normal vectors.
 *
 * @author Peter Abeles
 */
public class GenerateCylinderPointVector implements ModelGenerator<Cylinder3D_F64,PointVectorNN> {

	// tolerance angle for vector normals
	private double tolAngle;
	// tolerance for each point from the sphere
	private double tolDistance;

	// line defined by two lines.  used to find sphere center
	private LineParametric3D_F64 lineA = new LineParametric3D_F64(false);
	private LineParametric3D_F64 lineB = new LineParametric3D_F64(false);

	public GenerateCylinderPointVector(double tolAngle, double tolDistance) {
		this.tolAngle = tolAngle;
		this.tolDistance = tolDistance;
	}

	@Override
	public Cylinder3D_F64 createModelInstance() {
		return new Cylinder3D_F64();
	}

	@Override
	public boolean generate(List<PointVectorNN> dataSet, Cylinder3D_F64 output) {

		PointVectorNN pa = dataSet.get(0);
		PointVectorNN pb = dataSet.get(1);
		PointVectorNN pc = dataSet.get(2);

		lineA.p = pa.p;
		lineA.slope = pa.normal;

		lineB.p = pb.p;
		lineB.slope = pb.normal;

		ClosestPoint3D_F64.closestPoint(lineA,lineB,output.line.p);
		GeometryMath_F64.cross(pa.normal,pb.normal,output.line.slope);
		// slope should be normalized to 1 already since pa.normal and pb.normal already are

		double ra = Distance3D_F64.distance(output.line,pa.p);
		double rb = Distance3D_F64.distance(output.line,pb.p);
		double rc = Distance3D_F64.distance(output.line,pc.p);

		output.radius = (ra+rb)/2.0;

		return checkModel(output, pc, ra, rb, rc);
	}

	protected final boolean checkModel(Cylinder3D_F64 output, PointVectorNN pc, double ra, double rb, double rc) {
		// check the solution
		if( Math.abs(ra-output.radius) > tolDistance )
			return false;
		if( Math.abs(rb-output.radius) > tolDistance )
			return false;
		if( Math.abs(rc-output.radius) > tolDistance )
			return false;

		// only need to check to see if one angle is off since the other two are within tolerance by definition
		double acute = Math.acos(output.line.slope.dot(pc.normal));
		if(UtilAngle.dist(acute, Math.PI / 2.0) > tolAngle)
			return false;

		return true;
	}

	@Override
	public int getMinimumPoints() {
		return 3;
	}
}
