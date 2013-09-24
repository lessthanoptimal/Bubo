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

import bubo.ptcloud.CloudShapes;
import bubo.ptcloud.PointCloudShapeFinder;
import bubo.ptcloud.alg.ApproximateSurfaceNormals;
import bubo.ptcloud.alg.FoundShape;
import bubo.ptcloud.alg.PointCloudShapeDetectionSchnabel2007;
import bubo.ptcloud.alg.PointVectorNN;
import georegression.geometry.UtilPoint3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper which allows {@link bubo.ptcloud.alg.PointCloudShapeDetectionSchnabel2007} into
 * {@link PointCloudShapeFinder}.
 *
 * @author Peter Abeles
 */
public class Schnable2007_to_PointCloudShapeFinder implements PointCloudShapeFinder {

	ApproximateSurfaceNormals surfaceNormals;
	PointCloudShapeDetectionSchnabel2007 shapeDetector;
	List<CloudShapes> shapeList;

	FastQueue<PointVectorNN> pointNormList = new FastQueue<PointVectorNN>(PointVectorNN.class,true);

	FastQueue<Shape> output = new FastQueue<Shape>(Shape.class,true);

	Cube3D_F64 boundingBox = new Cube3D_F64();

	List<PointVectorNN> unmatchePV = new ArrayList<PointVectorNN>();

	public Schnable2007_to_PointCloudShapeFinder(ApproximateSurfaceNormals surfaceNormals,
												 PointCloudShapeDetectionSchnabel2007 shapeDetector,
												 List<CloudShapes> shapeList ) {
		this.surfaceNormals = surfaceNormals;
		this.shapeDetector = shapeDetector;
		this.shapeList = shapeList;
	}

	@Override
	public void process(List<Point3D_F64> cloud , Cube3D_F64 boundingBox ) {
		pointNormList.reset();
		surfaceNormals.process(cloud,pointNormList);

		if( boundingBox == null ) {
			UtilPoint3D_F64.boundingCube(cloud,this.boundingBox);
		} else {
			this.boundingBox.set(boundingBox);
		}

		shapeDetector.process(pointNormList,this.boundingBox);

		convertIntoOuput(shapeDetector.getFoundObjects());
	}

	/**
	 * Converts the list of shapes into the output format
	 */
	private void convertIntoOuput(FastQueue<FoundShape> schnabelShapes) {
		output.reset();
		for( int i = 0; i < schnabelShapes.size; i++ ) {
			FoundShape fs = schnabelShapes.get(i);
			Shape os = output.grow();
			os.parameters = fs.modelParam;
			os.type = shapeList.get( fs.whichShape );
			// make sure the list has been declared if it has not
			if( os.points == null ){
				os.points = new ArrayList<Point3D_F64>();
			} else {
				os.points.clear();
			}
			// add the points to it
			for( int j = 0; j < fs.points.size(); j++ ) {
				PointVectorNN pv = fs.points.get(j);
				os.points.add(pv.p);
			}
		}
	}

	@Override
	public List<Shape> getFound() {
		return output.toList();
	}

	@Override
	public void getUnmatched( List<Point3D_F64> unmatched ) {
		unmatchePV.clear();
		shapeDetector.findUnmatchedPoints(unmatchePV);

		for( int i = 0; i < unmatchePV.size(); i++ ) {
			unmatched.add( unmatchePV.get(i).p );
		}
	}

	@Override
	public List<CloudShapes> getShapesList() {
		return shapeList;
	}
}
