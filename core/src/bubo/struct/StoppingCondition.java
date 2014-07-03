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

package bubo.struct;


/**
 * Specifies the stopping condition for an iterative algorithm.
 *
 * @author Peter Abeles
 */
public class StoppingCondition {

	int maxIterations;
	double errorThreshold;

	int iteration;
	double previousError;

	public StoppingCondition(int maxIterations, double errorThreshold) {
		this.maxIterations = maxIterations;
		this.errorThreshold = errorThreshold;
	}

	public void reset() {
		iteration = 0;
		previousError = 0;
	}

	public boolean isFinished(double foundError) {
		if (foundError < errorThreshold)
			return true;

		if (iteration++ > 0) {
			// see if its at a minimum
			if (Math.abs(previousError - foundError) <= 1e-10)
				return true;
		}
		previousError = foundError;

		return iteration >= maxIterations;
	}

	public StoppingCondition copy() {
		return new StoppingCondition(maxIterations, errorThreshold);
	}

	public int getIteration() {
		return iteration;
	}
}
