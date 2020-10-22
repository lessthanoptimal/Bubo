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

package bubo.clouds.detect.alg;

import georegression.struct.point.Point3D_F64;
import org.ddogleg.nn.alg.KdTreeDistance;

/**
 * @author Peter Abeles
 */
public class KdDistancePointVectorNN implements KdTreeDistance<PointVectorNN> {
	@Override
	public double distance(PointVectorNN a, PointVectorNN b) {
		return a.p.distance2(b.p);
	}

	@Override
	public double valueAt(PointVectorNN point, int index) {
		switch( index ) {
			case 0: return point.p.x;
			case 1: return point.p.y;
			case 2: return point.p.z;
		}
		throw new IllegalArgumentException("Out of bounds. "+index);
	}

	@Override
	public int length() {
		return 3;
	}
}
