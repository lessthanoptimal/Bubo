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

import georegression.metric.Distance3D_F64;
import georegression.struct.plane.PlaneGeneral3D_F64;
import org.ddogleg.fitting.modelset.DistanceFromModel;

import java.util.List;

/**
 * Distance from a {@link PlaneGeneral3D_F64} for use with {@link PointCloudShapeDetectionSchnabel2007}.
 *
 * @author Peter Abeles
 */
public class DistanceFromPlanePointVector implements DistanceFromModel<PlaneGeneral3D_F64,PointVectorNN> {

	PlaneGeneral3D_F64 model;

	@Override
	public void setModel(PlaneGeneral3D_F64 model) {
		this.model = model;
	}

	@Override
	public double computeDistance(PointVectorNN pt) {
		return Math.abs(Distance3D_F64.distance(model, pt.p));
	}

	@Override
	public void computeDistance(List<PointVectorNN> points, double[] distance) {
		for( int i = 0; i < points.size(); i++ ) {
			distance[i] = Math.abs(Distance3D_F64.distance(model, points.get(i).p));
		}
	}
}
