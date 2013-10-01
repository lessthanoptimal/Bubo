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

import bubo.ptcloud.alg.ApproximateSurfaceNormals;
import bubo.ptcloud.alg.ConfigSchnabel2007;
import bubo.ptcloud.alg.PointCloudShapeDetectionSchnabel2007;
import bubo.ptcloud.alg.ShapeDescription;
import bubo.ptcloud.wrapper.ConfigSurfaceNormals;
import bubo.ptcloud.wrapper.Schnable2007_to_PointCloudShapeFinder;
import georegression.fitting.cylinder.CodecCylinder3D_F64;
import georegression.fitting.plane.CodecPlaneGeneral3D_F64;
import georegression.fitting.sphere.CodecSphere3D_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating implementations of {@link PointCloudShapeFinder}.
 *
 * @author Peter Abeles
 */
public class FactoryPointCloudShape {

	/**
	 * Returns an implementation of {@link PointCloudShapeFinder} which detects points using an Octree and RANSAC.
	 * By performing RANSAC locally inside Octree nodes its able to take advantage of the local structure of shapes
	 * and detect shapes at different scales.  Based on [1] paper.  See JavaDoc and code comments for significant
	 * deviations from original paper.
	 *
	 * @see ApproximateSurfaceNormals
	 * @see PointCloudShapeDetectionSchnabel2007
	 *
	 * [1] Schnabel, Ruwen, Roland Wahl, and Reinhard Klein. "Efficient RANSAC for Point‐Cloud Shape Detection."
	 * Computer Graphics Forum. Vol. 26. No. 2. Blackwell Publishing Ltd, 2007.
	 *
	 * @param configNormal Configuration for approximation of surface normals.
	 * @param configRansac Configuration for {@link PointCloudShapeDetectionSchnabel2007}.
	 * @return
	 */
	public static PointCloudShapeFinder ransacOctree( ConfigSurfaceNormals configNormal ,
													  ConfigSchnabel2007 configRansac ) {
		configNormal.checkConfig();

		PointCloudShapeDetectionSchnabel2007 alg = new PointCloudShapeDetectionSchnabel2007(configRansac);

		ApproximateSurfaceNormals surface = new ApproximateSurfaceNormals(
				configNormal.numPlane,configNormal.numNeighbors, configNormal.maxDistanceNeighbor);

		List<CloudShapeTypes> shapeList = new ArrayList<CloudShapeTypes>();

		for(ShapeDescription d : configRansac.models ) {
			if( d.codec instanceof CodecSphere3D_F64 ) {
				shapeList.add(CloudShapeTypes.SPHERE);
			} else if( d.codec instanceof CodecCylinder3D_F64) {
				shapeList.add(CloudShapeTypes.CYLINDER);
			} else if( d.codec instanceof CodecPlaneGeneral3D_F64) {
				shapeList.add(CloudShapeTypes.PLANE);
			} else {
				throw new IllegalArgumentException("Unknown shape contained in configRansac.  Probably a bug.");
			}
		}

		return new Schnable2007_to_PointCloudShapeFinder(surface,alg,shapeList);
	}
}
