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

package bubo.clouds.fit;

import georegression.struct.GeoTuple_F64;
import georegression.struct.InvertibleTransform;

import java.util.List;

/**
 * Generalized interface for finding the transform which minimizes the difference between two point clouds.
 * A point cloud is a set of points.
 *
 * @author Peter Abeles
 */
public interface MatchCloudToCloud<SE extends InvertibleTransform, T extends GeoTuple_F64> {

	/**
	 * Provides the list of source points.
	 *
	 * <p>WARNING: Do not modify the passed in list until after processing has finished.</p>
	 *
	 * @param points Point cloud.  Not modified.
	 */
	public void setSource( List<T> points );

	/**
	 * Sets the destination point cloud. In algorithms which involve a costly processing step on one of the
	 * point clouds, this is the point cloud it should be done on.
	 *
	 * <p>WARNING: Do not modify the passed in list until after processing has finished.</p>
	 *
	 * @param points Point cloud.  Not modified.
	 */
	public void setDestination( List<T> points );

	/**
	 * Computes the transform between the two point clouds.  Returns if it was successful or not.
	 *
	 * @return True if match between the two point clouds could be found. False otherwise
	 */
	public boolean compute();

	/**
	 * Returns the found transform from source to destination.  Must call {@link #compute} first.
	 * @return transform
	 */
	public SE getSourceToDestination();

}
