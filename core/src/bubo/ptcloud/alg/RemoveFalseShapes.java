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

package bubo.ptcloud.alg;

import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class RemoveFalseShapes implements PostProcessShapes {

	List<ShapeDescription> models;

	GrowQueue_I32 cloudToShape = new GrowQueue_I32();
	FastQueue<PixelInfo> shapePixels = new FastQueue<PixelInfo>(PixelInfo.class,true);

	List<FoundShape> output = new ArrayList<FoundShape>();

	double thresholdDiscard;

	public RemoveFalseShapes(double thresholdDiscard) {
		this.thresholdDiscard = thresholdDiscard;
	}

	@Override
	public void setup(List<ShapeDescription> models, LocalFitShapeNN refine) {
		this.models = models;
	}

	@Override
	public void process(List<FoundShape> input, int cloudSize) {
		cloudToShape.resize(cloudSize);
		output.clear();

		// find the fraction of points for each shape in which another point has less error
		for( int i = 0; i < input.size(); i++ ) {
			FoundShape shape = input.get(i);

			setupShape(shape);

			for( int j = 0; j < input.size(); j++ ) {
				if( i == j )
					continue;

				compareToShape( input.get(j) );
			}

			int totalSuck = 0;
			for( int j = 0; j < shapePixels.size; j++ ) {
				PixelInfo info = shapePixels.get(j);

				if( info.internal > info.external )
					totalSuck++;
			}
			double suckRatio = totalSuck/(double)shapePixels.size;
			if( suckRatio < thresholdDiscard ) {
				output.add(shape);
			}
		}

	}

	private void compareToShape( FoundShape shape ) {
		ShapeDescription desc = models.get( shape.whichShape );

		for( int i = 0; i < shape.points.size(); i++ ) {
			PointVectorNN pv = shape.points.get(i);

			int which = cloudToShape.data[pv.index];
			if( which < 0 )
				continue;

			PixelInfo info = shapePixels.get(which);

			double d = desc.modelDistance.computeDistance(shape.points.get(i));
			info.external = Math.min(d,info.external);
		}
	}

	private void setupShape( FoundShape shape ) {
		ShapeDescription desc = models.get( shape.whichShape );

		shapePixels.reset();

		desc.modelDistance.setModel(shape.modelParam);

		for( int i = 0; i < cloudToShape.size; i++ ) {
			cloudToShape.data[i] = -1;
		}

		for( int i = 0; i < shape.points.size(); i++ ) {
			PointVectorNN pv = shape.points.get(i);

			cloudToShape.data[pv.index] = i;

			PixelInfo info = shapePixels.grow();
			info.internal = desc.modelDistance.computeDistance(shape.points.get(i));
			info.external = Double.MAX_VALUE;
		}
	}

	@Override
	public List<FoundShape> getOutput() {
		return output;
	}

	public static class PixelInfo {
		double external;
		double internal;
	}
}
