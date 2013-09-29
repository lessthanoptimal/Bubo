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

package bubo.ptcloud.wrapper;

import bubo.ptcloud.CloudShapeTypes;
import bubo.ptcloud.PointCloudShapeFinder;
import bubo.ptcloud.alg.ApproximateSurfaceNormals;
import bubo.ptcloud.alg.ConfigSchnabel2007;
import bubo.ptcloud.alg.PointCloudShapeDetectionSchnabel2007;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class TestSchnable2007_to_PointCloudShapeFinder extends GeneralChecksPointCloudShapeFinder {

	public TestSchnable2007_to_PointCloudShapeFinder() {
		super(10,1e-6);
	}

	@Override
	public PointCloudShapeFinder createAlgorithm() {

		ConfigSchnabel2007 config = ConfigSchnabel2007.createDefault(100,0.2,0.1,0.1);
		config.minModelAccept = 10;
		config.octreeSplit = 20;

		PointCloudShapeDetectionSchnabel2007 alg = new PointCloudShapeDetectionSchnabel2007(config);

		ApproximateSurfaceNormals surface = new ApproximateSurfaceNormals(6,20,3);

		List<CloudShapeTypes> shapeList = new ArrayList<CloudShapeTypes>();
		shapeList.add(CloudShapeTypes.SPHERE);
		shapeList.add(CloudShapeTypes.PLANE);
		shapeList.add(CloudShapeTypes.CYLINDER);

		return new Schnable2007_to_PointCloudShapeFinder(surface,alg,shapeList);
	}
}
