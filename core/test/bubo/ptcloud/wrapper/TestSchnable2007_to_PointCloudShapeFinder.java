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
import bubo.ptcloud.alg.*;
import org.ddogleg.fitting.modelset.ransac.RansacMulti;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class TestSchnable2007_to_PointCloudShapeFinder extends GeneralChecksPointCloudShapeFinder {

	@Override
	public PointCloudShapeFinder createAlgorithm() {

		List<RansacMulti.ObjectType> objects = new ArrayList<RansacMulti.ObjectType>();

		RansacMulti.ObjectType sphere = new RansacMulti.ObjectType();
		sphere.modelDistance = new DistanceFromSpherePointVector();
		sphere.modelGenerator = new GenerateSpherePointVector(0.3,0.3);
		sphere.thresholdFit = 0.3;

		RansacMulti.ObjectType plane = new RansacMulti.ObjectType();
		plane.modelDistance = new DistanceFromPlanePointVector();
		plane.modelGenerator = new GeneratePlanePointVector(0.3);
		plane.thresholdFit = 0.3;

		RansacMulti.ObjectType cylinder = new RansacMulti.ObjectType();
		cylinder.modelDistance = new DistanceFromCylinderPointVector();
		cylinder.modelGenerator = new GenerateCylinderPointVector(0.3,0.3);
		cylinder.thresholdFit = 0.3;

		objects.add(sphere);
		objects.add(plane);
		objects.add(cylinder);

		PointCloudShapeDetectionSchnabel2007 alg =
				new PointCloudShapeDetectionSchnabel2007(objects,20,10,10,1000,0xBEEF);

		ApproximateSurfaceNormals surface = new ApproximateSurfaceNormals(6,1);

		List<CloudShapeTypes> shapeList = new ArrayList<CloudShapeTypes>();
		shapeList.add(CloudShapeTypes.SPHERE);
		shapeList.add(CloudShapeTypes.PLANE);
		shapeList.add(CloudShapeTypes.CYLINDER);

		return new Schnable2007_to_PointCloudShapeFinder(surface,alg,shapeList);
	}
}
