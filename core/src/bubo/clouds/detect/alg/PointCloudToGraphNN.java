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
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import java.util.List;
import java.util.Stack;

/**
 * Creates a nearest-neighbor graph out of a point cloud
 *
 * @author Peter Abeles
 */
public class PointCloudToGraphNN {

	NearestNeighbor<Point3D_F64> nn;
	NearestNeighbor.Search<Point3D_F64> searchNN;

	// the maximum distance a neighbor can be
	private double maxDistanceNeighbor;

	// number of nearest-neighbors it will search for
	private int numNeighbors;

	// point normal data which is stored in the graph
	private FastQueue<PointVectorNN> listPointVector = new FastQueue<>(PointVectorNN::new);

	// results of NN search
	private FastQueue<NnData<Point3D_F64>> resultsNN = new FastQueue<>(NnData::new);

	public PointCloudToGraphNN(NearestNeighbor<Point3D_F64> nn,
							   int numNeighbors ,
							   double maxDistanceNeighbor ) {
		this.nn = nn;
		this.numNeighbors = numNeighbors;
		this.maxDistanceNeighbor = maxDistanceNeighbor;

		this.searchNN = nn.createSearch();
	}

	/**
	 * Converts points into a format understood by the NN algorithm and initializes it
	 */
	public void process(List<Point3D_F64> cloud) {
		// declare the output data for creating the NN graph
		listPointVector.reset();
		for (int i = 0; i < cloud.size(); i++) {
			PointVectorNN p = listPointVector.grow();
			p.reset();
			p.p = cloud.get(i);
			p.index = i;
		}

		findNeighbors(cloud);
	}

	private void findNeighbors(List<Point3D_F64> cloud) {
		// find the nearest-neighbor for each point in the cloud
		nn.setPoints(cloud, true);

		for (int i = 0; i < listPointVector.size; i++) {
			// find the nearest-neighbors
			resultsNN.reset();

			Point3D_F64 targetPt = cloud.get(i);
			// numNeighbors+1 since the target node will also be returned and is removed
			searchNN.findNearest(targetPt, maxDistanceNeighbor, numNeighbors + 1, resultsNN);

			PointVectorNN p = listPointVector.get(i);

			// save the results
			p.neighbors.reset();
			for (int j = 0; j < resultsNN.size; j++) {
				NnData<Point3D_F64> n = resultsNN.get(j);

				// don't add the point to its own list of neighbors list
				if (n.index != i) {
					p.neighbors.add(listPointVector.get(n.index));
				}
			}
		}
	}

	public FastQueue<PointVectorNN> getListPointVector() {
		return listPointVector;
	}
}
