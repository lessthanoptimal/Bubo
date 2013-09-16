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
 *
 * TODO describe how the tangent is found.  Inspired by [1] and customized for this specific application.
 *
 * [1] TODO
 *
 * @author Peter Abeles
 */
public class ApproximateSurfaceNormals {

	private int numNeighbors;
	private double maxDistanceNeighbor;
	private NearestNeighbor<PointVectorNN> nn;
	private List<double[]> nnPoints = new ArrayList<double[]>();

	private Stack<double[]> storage = new Stack<double[]>();
	private FastQueue<NnData<PointVectorNN>> resultsNN = new FastQueue<NnData<PointVectorNN>>((Class)NnData.class,true);

	private FitPlane3D_F64 fitPlane = new FitPlane3D_F64();
	private List<Point3D_F64> fitList = new ArrayList<Point3D_F64>();

	public ApproximateSurfaceNormals( NearestNeighbor<PointVectorNN> nn , int numNeighbors , double maxDistanceNeighbor) {
		this.numNeighbors = numNeighbors;
		this.maxDistanceNeighbor = maxDistanceNeighbor;
		this.nn = nn;

		nn.init(3);
	}

	public ApproximateSurfaceNormals(int numNeighbors, double maxDistanceNeighbor) {
		this.numNeighbors = numNeighbors;
		this.maxDistanceNeighbor = maxDistanceNeighbor;

		nn = FactoryNearestNeighbor.kdtree();
	}

	public void process( List<Point3D_F64> cloud , FastQueue<PointVectorNN> output ) {

		// convert the point cloud into a format that the NN algorithm can recognize
		setupNearestNeighbor(cloud);

		// declare the output data for creating the NN graph
		for( int i = 0; i < cloud.size(); i++ ) {
			PointVectorNN p = output.grow();
			p.p = cloud.get(i);
			p.neighbors.reset();
		}

		// find the nearest-neighbor for each point in the cloud
		nn.setPoints(nnPoints,output.toList());

		for( int i = 0; i < output.size; i++ ) {
			// find the nearest-neighbors
			resultsNN.reset();
			nn.findNearest(nnPoints.get(i),maxDistanceNeighbor,numNeighbors,resultsNN);

			PointVectorNN p = output.get(i);

			// save the results
			p.neighbors.reset();
			for( int j = 0; j < resultsNN.size; j++ ) {
				NnData<PointVectorNN> n = resultsNN.get(j);
				p.neighbors.add(n.data);
			}

			computeSurfaceNormal(p);
		}

	}

	/**
	 * Fits a plane to the nearest neighbors around the point and sets point.normal.
	 */
	protected void computeSurfaceNormal(PointVectorNN point) {
		if( point.neighbors.size < 2 ) {
			point.normal.set(0,0,0);
		} else {
			fitList.clear();
			for( int i = 0; i < point.neighbors.size; i++ ) {
				fitList.add( point.neighbors.data[i].p);
			}
			fitPlane.svdPoint(fitList,point.p,point.normal);
		}
	}

	/**
	 * Converts points into a format understood by the NN algorithm and initializes it
	 */
	private void setupNearestNeighbor(List<Point3D_F64> cloud) {
		nn.init(3);

		for( int i = 0; i < cloud.size(); i++ ) {
			Point3D_F64 p = cloud.get(i);

			double[] d;
			if( storage.isEmpty() ) {
				d = new double[3];
			} else {
				d = storage.pop();
			}

			d[0] = p.x;
			d[1] = p.y;
			d[2] = p.z;

			nnPoints.add(d);
		}
	}
}
