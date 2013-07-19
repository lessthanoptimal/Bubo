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

package bubo.models.sensor;

import bubo.filters.ekf.EkfProjector;
import org.ejml.data.DenseMatrix64F;

/**
 * {@link EkfProjector} for a 2D range bearing sensor.  Input system state is 2D robot location and heading.
 *
 * @author Peter Abeles
 */
public class ProjectorRangeBearing2D implements LandmarkProjector {

	// location of the sensor
	double x,y;

	// predicted observation
	DenseMatrix64F predicted = new DenseMatrix64F(2,1);
	// observation Jacobian
	DenseMatrix64F H = new DenseMatrix64F(2,3);

	@Override
	public void setLandmarkLocation( double x , double y ) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getSystemSize() {
		return 3;
	}

	@Override
	public int getMeasurementSize() {
		return 2;
	}

	@Override
	public void compute(DenseMatrix64F systemState) {
		double rx = systemState.get(0);
		double ry = systemState.get(1);
		double rtheta = systemState.get(2);

		double dx = x - rx;
		double dy = y - ry;

		double d2 = dx*dx + dy*dy;
		double d = Math.sqrt(d2);

		// partial of predicted observation relative to robot state
		H.unsafe_set(0,0, -dx/d );
		H.unsafe_set(0,1, -dy/d );

		if( dx != 0 ) {
			H.unsafe_set(1,0,  dy/d2 );
			H.unsafe_set(1,1,  -dx/d2 );
		} else {
			H.unsafe_set(1,0, 0 );
			H.unsafe_set(1,1, 0 );
		}
		H.unsafe_set(1,2,-1);


		// predicted observation
		predicted.data[0] = d;
		predicted.data[1] = Math.atan2(dy,dx) - rtheta;

	}

	@Override
	public DenseMatrix64F getJacobianH() {
		return H;
	}

	@Override
	public DenseMatrix64F getProjected() {
		return predicted;
	}
}
