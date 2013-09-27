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

import java.util.ArrayList;
import java.util.List;

/**
 * Give a set of shape and points which are members of the shape, identify shapes which are really the same
 * and merge them together.
 *
 * @author Peter Abeles
 */
public class MergePointVectorShapes {

	FastQueue<FoundShape> output;

	FastQueue<Helper> matches = new FastQueue<Helper>(Helper.class,true);

	public void merge( List<FoundShape> input , int cloudSize ) {
		// initialize data structures
		matches.reset();
		matches.growArray(cloudSize);
		for( int i = 0; i < cloudSize; i++ ) {
			matches.grow().matches.clear();
		}

		// find which shapes have which points as members
		for( int i = 0; i < input.size(); i++ ) {
			FoundShape shape = input.get(i);

			for( int j = 0; j < shape.points.size(); j++ ) {
				PointVectorNN pv = shape.points.get(j);
				matches.data[pv.index].matches.add(shape);
			}
		}


	}


	public FastQueue<FoundShape> getOutput() {
		return output;
	}

	private static class Helper {
		List<FoundShape> matches = new ArrayList<FoundShape>();
	}

	private static class Node {
		FoundShape shape;
		FastQueue<Edge> edges = new FastQueue<Edge>(Edge.class,true);
	}

	private static class Edge {
		int numHits;
		Node destination;
	}
}
