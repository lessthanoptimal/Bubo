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

import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.fitting.modelset.ModelCodec;
import org.ddogleg.fitting.modelset.ModelFitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds a locally optimal best fit set of points and model parameters to a shape in a point cloud with nearest-neighbor
 * information.  Initially the algorithm is given a set of points and parameters which belong to
 * and describe the shape, respectively.  The points then are used to refine the model parameters.  A new set
 * of points which match the new parameters is found.  This process repeats until the model parameters converge or
 * it runs out of iterations.
 *
 * @author Peter Abeles
 */
public class LocalFitShapeNN <Model> {

	// used to estimate the shape's parameters
	private ModelFitter<Model,PointVectorNN> fitter;
	// converts the model parameters to and from double[].  Used to test for convergence
	private ModelCodec<Model> codec;

	// finds points in the local neighbor hood which match the model
	private FindMatchSetPointVectorNN<Model> findMatchSet;

	// distance which defines an inlier
	private double distanceThreshold;

	// maximum number of iterations
	private int maxIterations;
	// minimum amount of change between model parameters before convergence is declared
	private double minimumChangeThreshold;

	// storage for shape parameters converted into double[]
	private double paramPrev[] = new double[1];
	private double paramCurr[] = new double[1];

	// stores points which fit the model
	private List<PointVectorNN> listTempA = new ArrayList<PointVectorNN>();
	private List<PointVectorNN> listTempB = new ArrayList<PointVectorNN>();

	/**
	 * Configures the search and fit algorithm
	 *
	 * @param maxIterations Maximum number of inlier select and model estimate iterations it will perform.
	 * @param minimumChangeThreshold When the average change in model parameters is less than this value
	 *                                  iteration will stop.
	 * @param findMatchSet The code which searches for neighbors that match the provided model
	 */
	public LocalFitShapeNN(int maxIterations,
						   double minimumChangeThreshold,
						   FindMatchSetPointVectorNN<Model> findMatchSet) {
		this.maxIterations = maxIterations;
		this.minimumChangeThreshold = minimumChangeThreshold;
		this.findMatchSet = findMatchSet;
	}

	/**
	 * Specifies algorithms for computing he distance between the shape and a point, refining the shape parameters,
	 * and converting the model parameters into an array.
	 *
	 * @param fitter Uses a set of points and an initial guess to estimate the shape's parameters
	 * @param distance Computes the distance between a shape and a point
	 * @param codec Converts the shape parameter to and from double array
	 * @param threshold Threshold which defined an inlier.  inlier <= threshold
	 */
	public void configure( ModelFitter<Model,PointVectorNN> fitter,
						   DistanceFromModel<Model,PointVectorNN> distance ,
						   ModelCodec<Model> codec ,
						   double threshold ) {
		this.fitter = fitter;
		this.codec = codec;
		this.distanceThreshold = threshold;

		findMatchSet.setModelDistance(distance);
		
		if( paramPrev.length < codec.getParamLength() ) {
			paramPrev = new double[ codec.getParamLength() ];
			paramCurr = new double[ codec.getParamLength() ];
		}
	}

	/**
	 * Refines the set of points which belong to the shape and the model parameters which define the shape
	 *
	 * NOTE: The initialParam is used inside this function to store intermediate results
	 *
	 * @param initialMatch (input) Initial set of points which belong to the shape
	 * @param initialParam (input) Initial description of the shape. THIS IS MODIFIED.
	 * @param initialFitToPoints (input) If true it will fit the model parameters to the initial points.  If false
	 *                           it will start by selecting points which match the initial model.
	 * @param outputMatch (Output) Refined set of points which belong to the shape.
	 * @param outputParam (Output) Refined description of the shape.
	 */
	public void refine( List<PointVectorNN> initialMatch , Model initialParam , boolean initialFitToPoints,
						List<PointVectorNN> outputMatch , Model outputParam )
	{
		if( initialFitToPoints )
			fitter.fitModel(initialMatch, initialParam, outputParam);

		codec.encode(initialParam, paramPrev);
		listTempA.clear();
		listTempA.addAll(initialMatch);

		for( int iter = 0; iter < maxIterations; iter++ ) {
			// find list of points which match the model
			listTempB.clear();
			// find the points which match the model
			findMatchSet.selectMatchSet(listTempA,outputParam,distanceThreshold,true,listTempB);
			// use the points which match the model to estimate the parameters.
			fitter.fitModel(listTempB,outputParam,initialParam);

			// Compute the change in parameters
			codec.encode(initialParam, paramCurr);
		
			double change = 0;
			for( int i = 0; i < codec.getParamLength(); i++ ) {
				double d = paramCurr[i] - paramPrev[i];
				change += Math.abs(d);
			}
			change /= codec.getParamLength();

			// Set the output parameters to the most recently computed parameters
			codec.decode(paramCurr,outputParam);

			// check to see if it has converged
			if( change <= minimumChangeThreshold ) {
				break;
			}

			// swap current and previous
			List<PointVectorNN> tempL = listTempB;
			listTempB = listTempA;
			listTempA = tempL;

			double tempD[] = paramCurr;
			paramCurr = paramPrev;
			paramPrev = tempD;
		}

		outputMatch.addAll(listTempB);
	}

}
