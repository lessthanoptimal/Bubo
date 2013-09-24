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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * Output: List of found shapes and points which are inliers.  Unmatched points.
 *
 * @author Peter Abeles
 */
public class PointCloudShapeDetectionSchnabel2007 {

	// used to randomly sample regions in the octree
	private Random rand;

	// An object must have this many points before being accepted as valid
	private int minModelAccept;

	protected ConstructOctreeEqual managerOctree;
	// optional user specified bounding code
	private Cube3D_F64 aprioriBounding;
	// the initial bounding cube used by the point cloud
	private Cube3D_F64 bounding = new Cube3D_F64();

	// list of leafs in the Octree.  Allows quick finding of the path to the base node
	private FastQueue<Octree> leafs = new FastQueue<Octree>(Octree.class,false);

	// found path from leaf to base
	private FastQueue<Octree> path = new FastQueue<Octree>(Octree.class,false);

	// robust model estimation
	private RansacShapeDetection ransac;

	// list of found objects
	private FastQueue<FoundShape> foundObjects = new FastQueue<FoundShape>(FoundShape.class,true);

	int maximumAllowedIterations;

	/**
	 * TODO comment
	 * @param models
	 * @param octreeSplit
	 * @param minModelAccept
	 * @param ransacExtension
	 */
	public PointCloudShapeDetectionSchnabel2007(List<RansacMulti.ObjectType> models, int octreeSplit,
												int minModelAccept, int ransacExtension ,
												int maximumAllowedIterations ,long randomSeed  )
	{
		this.minModelAccept = minModelAccept;
		this.rand = new Random(randomSeed);
		this.maximumAllowedIterations = maximumAllowedIterations;

		if( octreeSplit < this.minModelAccept) {
			throw new IllegalArgumentException("octreeSplit should be at least 3 times the ransac sample size, which "+
			"is "+ this.minModelAccept);
		}

		managerOctree = new ConstructOctreeEqual(octreeSplit);

		ransac = new RansacShapeDetection(1234,ransacExtension,models);
	}

	protected PointCloudShapeDetectionSchnabel2007() {
	}

	/**
	 * TODO comment
	 * @param points
	 */
	public void process( FastQueue<PointVectorNN> points ) {

		ransac.reset();
		foundObjects.reset();

		constructOctree(points);
		findLeafs();

		List<PointVectorNN> sampleSet = new ArrayList<PointVectorNN>();

		int totalIterations = 0;
		while( totalIterations < maximumAllowedIterations  ) {
			// select region to search for a shape inside
			Octree sampleNode = selectSampleNode();

			// create list of points which are not a member of a shape yet
			sampleSet.clear();

			for( int i = 0; i < sampleNode.points.size; i++ ) {
				PointVectorNN pv = (PointVectorNN)sampleNode.points.get(i).data;

				if( !pv.used )
					sampleSet.add( pv );
			}

			// see if its possible to find a valid model with this data
			if( sampleSet.size() < minModelAccept ) {
				totalIterations += 10; // TODO REMOVE THIS HACK
				continue; // TODO exit if this happens too much, or make it impossible.
			}

			// use RANSAC to find a shape
			if( ransac.process(sampleSet)) {
				List<PointVectorNN> inliers = ransac.getMatchSet();

				// see if there are enough points to be a valid shape
				if( inliers.size() >= minModelAccept ) {

					// estimate shape parameters using all inlier points
					// TODO do it

					// construct the output shape
					FoundShape shape = foundObjects.grow();
					shape.points.clear();
					shape.modelParam = ransac.getModelParameters();
					shape.whichShape = ransac.getModelIndex();

					// mark points as being used and add to the output shape
					for( int i = 0; i < inliers.size(); i++ ) {
						PointVectorNN p = inliers.get(i);
						p.used = true;
						shape.points.add(p);
					}

					// If too many points are used then create a new octree with only the unused points
					// todo consider doing that
				}
			}

			// note how many iterations have been processed
			totalIterations += ransac.getIteration();

			// todo exit if X percent of points have been matched
		}
	}

	/**
	 * Instead of selecting a random point then searching for it in the tree,
	 * selects a random leaf on the tree and traces backwards to the root
	 *
	 * @return Node it should draw samples from
	 */
	protected Octree selectSampleNode() {
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
		return path.get( rand.nextInt(path.size));
	}

	/**
	 * Constructs an Octree for the set of points.  The initial bound box will be found from the
	 * input points or is provided by the user.
\	 */
	protected void constructOctree( FastQueue<PointVectorNN> points ) {

		// specify the Octree's bounding box
		if( aprioriBounding == null )
			computingBoundingCube(points);
		else
			bounding.set(aprioriBounding);

		managerOctree.initialize(bounding);

		// add points to the Octree
		for( int i = 0; i < points.size; i++ ) {
			PointVectorNN p = points.data[i];

			managerOctree.addPoint(p.p,p);
		}
	}

	/**
	 * Searches for all the leafs in the Octree
	 */
	protected void findLeafs() {
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

	/**
	 * Computes the minimal bounding box around the set of input points
	 */
	protected void computingBoundingCube(FastQueue<PointVectorNN> points) {
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
		bounding.lengthX = maxX-minX;
		bounding.lengthY = maxY-minY;
		bounding.lengthZ = maxZ-minZ;
	}

	/**
	 * Returns a list of all the objects that it found
	 */
	public FastQueue<FoundShape> getFoundObjects() {
		return foundObjects;
	}

	/**
	 * Used to specify a bounding cube.  If not specified a bounding cube will be manually computed
	 * each iteration.
	 *
	 * @param bounding
	 */
	public void setBoundingCube(Cube3D_F64 bounding ) {
		this.aprioriBounding = new Cube3D_F64(bounding);
	}

	public Cube3D_F64 getBounding() {
		return bounding;
	}

	public FastQueue<Octree> getLeafs() {
		return leafs;
	}

	public ConstructOctreeEqual getManagerOctree() {
		return managerOctree;
	}
}
