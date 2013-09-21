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

import georegression.struct.shapes.Cube3D_F64;
import org.ddogleg.fitting.modelset.ransac.RansacMulti;
import org.ddogleg.struct.FastQueue;

import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class RansacMultiObjectPointCloud {

	Random rand = new Random(234);

	int numCloudSamples;
	int maxRansacIterations;

	// The minimum number of points it needs before running RANSAC
	int ransacMinimumPoints;

	FastQueue<PointVectorNN> points;

	ConstructOctreeEqual managerOctree;
	Cube3D_F64 bounding = new Cube3D_F64();

//	List<CloudModels> models = new ArrayList<CloudModels>();

	FastQueue<Octree> leafs = new FastQueue<Octree>(Octree.class,false);

	FastQueue<Octree> path = new FastQueue<Octree>(Octree.class,false);

	RansacMulti<PointVectorNN> ransac;

	public RansacMultiObjectPointCloud( List<RansacMulti.ObjectType> models , int octreeSplit , int minModelAccept )
	{
		ransacMinimumPoints = minModelAccept;

		if( octreeSplit < ransacMinimumPoints ) {
			throw new IllegalArgumentException("octreeSplit should be at least 3 times the ransac sample size, which "+
			"is "+ransacMinimumPoints);
		}

		managerOctree = new ConstructOctreeEqual(octreeSplit);

		ransac = new RansacMulti<PointVectorNN>(1234,maxRansacIterations,models,PointVectorNN.class);
	}

	public void process( FastQueue<PointVectorNN> points ) {

		constructOctree(points);

		for( int i = 0; i < numCloudSamples; i++ ) {
			// Instead of selecting a random point then searching for it in the tree,
			// it selects a random leaf on the tree and traces backwards to the root
			Octree node = leafs.get(rand.nextInt(leafs.size));

			path.reset();
			path.add(node);

			node = node.parent;
			while( node != null ) {
				path.add(node);
				node = node.parent;
			}

			// randomly select one of the nodes in the path
			// TODO use a PDF
			node = path.get( rand.nextInt(path.size));

		}
	}

	protected void constructOctree( FastQueue<PointVectorNN> points ) {

		computingBoundingCube(points);

		// add points to the Octree
		managerOctree.initialize(bounding);

		for( int i = 0; i < points.size; i++ ) {
			PointVectorNN p = points.data[i];

			managerOctree.addPoint(p.p,p);
		}

		// create a list of leafs
		leafs.reset();

		FastQueue<Octree> nodes = managerOctree.getAllNodes();
		for( int i = 0; i < nodes.size; i++ ) {
			Octree n = nodes.get(i);
			if( n.isLeaf() ) {
				leafs.add(n);
			}
		}
	}

	private void computingBoundingCube(FastQueue<PointVectorNN> points) {
		double minX=Double.MAX_VALUE,maxX=-Double.MAX_VALUE;
		double minY=Double.MAX_VALUE,maxY=-Double.MAX_VALUE;
		double minZ=Double.MAX_VALUE,maxZ=-Double.MAX_VALUE;

		for( int i = 0; i < points.size; i++ ) {
			PointVectorNN p = points.data[i];
			if( p.p.x < minX )
				minX = p.p.x;
			if( p.p.x > maxX )
				maxX = p.p.x;
			if( p.p.y < minY )
				minY = p.p.y;
			if( p.p.y > maxY )
				maxY = p.p.y;
			if( p.p.z < minZ )
				minZ = p.p.z;
			if( p.p.z > maxZ )
				maxZ = p.p.z;
		}

		bounding.p.set(minX,minY,minZ);
		bounding.lengthX = minX;
		bounding.lengthY = minY;
		bounding.lengthZ = minZ;
	}


}
