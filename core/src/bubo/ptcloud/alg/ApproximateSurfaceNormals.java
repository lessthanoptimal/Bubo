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

import georegression.fitting.plane.FitPlane3D_F64;
import georegression.struct.point.Point3D_F64;
import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Given a point cloud, estimate the tangent to the surface at each point and record the nearest neighbors.
 * At each point, there are two possible directions for the tangent and one is arbitrarily selected for each point.
 * No effort is made to make them consistent.
 * <p></p>
 * The implementation below is inspired by [1], in that it approximates the normal using the nearest neighbors.
 * Unlike in [1], the normalizes are no organized in any way so that their directions is consistent.  The data
 * structures have also been customized to provide support for {@link PointCloudShapeDetectionSchnabel2007}.
 * <p></p>
 * [1] Hoppe, H., DeRose, T., Duchamp, T., McDonald, J., & Stuetzle, W. "Surface reconstruction from unorganized
 * points" 1992, Vol. 26, No. 2, pp. 71-78. ACM.
 *
 * @author Peter Abeles
 */
public class ApproximateSurfaceNormals {

	// number of nearest-neighbors it will search for
	private int numNeighbors;
	// the maximum distance two points can be apart for them to be considered neighbors
	private double maxDistanceNeighbor;
	// The algorithm used to search for nearest neighbors
	private NearestNeighbor<PointVectorNN> nn;

	// stores and recycles Point3D converted to double[] for use in NN
	private Stack<double[]> unusedNnData = new Stack<double[]>();
	private Stack<double[]> usedNnData = new Stack<double[]>();

	// point normal data which is stored in the graph
	private FastQueue<PointVectorNN> listPointVector = new FastQueue<PointVectorNN>(PointVectorNN.class,true);

	// resuts of NN search
	private FastQueue<NnData<PointVectorNN>> resultsNN = new FastQueue<NnData<PointVectorNN>>((Class)NnData.class,true);

	// the local plane computed using neighbors
	private FitPlane3D_F64 fitPlane = new FitPlane3D_F64();
	// array to store points used to compute plane
	private List<Point3D_F64> fitList = new ArrayList<Point3D_F64>();

	/**
	 * Configures approximation algorithm
	 *
	 * @param nn Which nearest-neighbor algorithm to use
	 * @param numNeighbors Number of neighbors it will use to approximate normal
	 * @param maxDistanceNeighbor The maximum distance two points can be from each other to be considered a neighbor
	 */
	public ApproximateSurfaceNormals( NearestNeighbor<PointVectorNN> nn , int numNeighbors , double maxDistanceNeighbor) {
		this.numNeighbors = numNeighbors;
		this.maxDistanceNeighbor = maxDistanceNeighbor;
		this.nn = nn;

		nn.init(3);
	}

	/**
	 * Configures approximation algorithm and uses a K-D tree by default.
	 *
	 * @param numNeighbors Number of neighbors it will use to approximate normal
	 * @param maxDistanceNeighbor The maximum distance two points can be from each other to be considered a neighbor
	 */
	public ApproximateSurfaceNormals(int numNeighbors, double maxDistanceNeighbor) {
		this.numNeighbors = numNeighbors;
		this.maxDistanceNeighbor = maxDistanceNeighbor;

		nn = FactoryNearestNeighbor.kdtree();
	}

	/**
	 * Process point cloud and finds the shape's normals.  If a normal could not be estimated for the point
	 * its vector is set to (0,0,0).  A normal cannot be found for points with 1 or less neighbors.
	 *
	 * @param cloud Input: 3D point cloud
	 * @param output Output: Storage for the point cloud with normals. Must set declareInstances to false.
	 */
	public void process( List<Point3D_F64> cloud , FastQueue<PointVectorNN> output  ) {

		// convert the point cloud into a format that the NN algorithm can recognize
		setupNearestNeighbor(cloud);

		// declare the output data for creating the NN graph
		listPointVector.reset();
		for( int i = 0; i < cloud.size(); i++ ) {
			PointVectorNN p = listPointVector.grow();
			p.reset();
			p.p = cloud.get(i);
		}

		// find the nearest-neighbor for each point in the cloud
		nn.setPoints(usedNnData, listPointVector.toList());

		for( int i = 0; i < listPointVector.size; i++ ) {
			// find the nearest-neighbors
			resultsNN.reset();

			double[] targetPt = usedNnData.get(i);
			nn.findNearest(targetPt,maxDistanceNeighbor,numNeighbors,resultsNN);

			PointVectorNN p = listPointVector.get(i);

			// save the results
			p.neighbors.reset();
			for( int j = 0; j < resultsNN.size; j++ ) {
				NnData<PointVectorNN> n = resultsNN.get(j);

				// don't add the point to its own list of neighbors list
				if( n.point != targetPt)
					p.neighbors.add(n.data);
			}

			// try to compute the normal and add it to the output list if one could be fond
			computeSurfaceNormal(p);
			output.add(p);
		}

	}

	/**
	 * Fits a plane to the nearest neighbors around the point and sets point.normal.
	 */
	protected void computeSurfaceNormal(PointVectorNN point) {
		// need 3 points to compute a plane.  which means you need two neighbors and 'point'
		if( point.neighbors.size >= 2 ) {
			fitList.clear();
			fitList.add(point.p);
			for( int i = 0; i < point.neighbors.size; i++ ) {
				fitList.add( point.neighbors.data[i].p);
			}
			fitPlane.svdPoint(fitList,point.p,point.normal);
		} else {
			point.normal.set(0,0,0);
		}
	}

	/**
	 * Converts points into a format understood by the NN algorithm and initializes it
	 */
	private void setupNearestNeighbor(List<Point3D_F64> cloud) {
		nn.init(3);

		// swap the two lists to recycle old data and avoid creating new memory
		Stack<double[]> tmp = unusedNnData;
		unusedNnData = usedNnData;
		usedNnData = tmp;
		// add the smaller list to the larger one
		unusedNnData.addAll(usedNnData);
		usedNnData.clear();

		// convert the point cloud into the NN format
		for( int i = 0; i < cloud.size(); i++ ) {
			Point3D_F64 p = cloud.get(i);

			double[] d;
			if( unusedNnData.isEmpty() ) {
				d = new double[3];
			} else {
				d = unusedNnData.pop();
			}

			d[0] = p.x;
			d[1] = p.y;
			d[2] = p.z;

			usedNnData.add(d);
		}
	}
}
