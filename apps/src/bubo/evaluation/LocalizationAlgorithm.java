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

package bubo.evaluation;

import bubo.simulation.d2.sensors.RangeBearing;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import org.ejml.data.DenseMatrix64F;

import java.util.List;

/**
 * @author Peter Abeles
 */
public interface LocalizationAlgorithm {

	public void configure( List<Point2D_F64> landmarks , DenseMatrix64F R );

	void setInitial(Se2_F64 pose , double sigmaXY , double sigmaYaw );

	public void predict( double vel , double angVel );

	public void update( List<RangeBearing> measNoisy , List<RangeBearing> measTrue);

	public Se2_F64 getPoseEstimate();

}
