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

package bubo.filters.imm.inhomo;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.UtilMultivariateGaussian;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTruncatelInternalStateConverter {

	/**
	 * Convert a state from a large size into a smaller size
	 */
	@Test
	public void largeToSmall() {
		TruncatelInternalStateConverter converter = new TruncatelInternalStateConverter(2, 6);

		converter.setModelDimensions(2, 5, 3);

		MultivariateGaussianDM large = UtilMultivariateGaussian.createDummy(5, 2);

		CommonOps_DDRM.fill(large.getMean(), 5);

		DMatrixRMaj x = converter.convertMergeFrom(true, large.getMean(), 0, 1);
		assertEquals(15, CommonOps_DDRM.elementSum(x), 1e-6);

		DMatrixRMaj P = converter.convertMergeFrom(false, large.getCovariance(), 0, 1);
		assertEquals(6, CommonOps_DDRM.elementSum(P), 1e-6);
	}

	/**
	 * Convert a state from a small size to a larger one.  This will require use of
	 * the default state
	 */
	@Test
	public void smallToLarge() {
		TruncatelInternalStateConverter converter = new TruncatelInternalStateConverter(2, 6);
		MultivariateGaussianDM def = new MultivariateGaussianDM(6);
		CommonOps_DDRM.fill(def.getMean(), 4);
		CommonOps_DDRM.fill(def.getCovariance(), 4);

		converter.setModelDimensions(2, 5, 3);
		converter.setDefault(def);

		MultivariateGaussianDM small = UtilMultivariateGaussian.createDummy(3, 0);

		DMatrixRMaj x = converter.convertMergeFrom(true, small.getMean(), 1, 0);
		assertEquals(8, CommonOps_DDRM.elementSum(x), 1e-6);

		DMatrixRMaj P = converter.convertMergeFrom(false, small.getCovariance(), 1, 0);
		assertEquals(64, CommonOps_DDRM.elementSum(P), 1e-6);
	}

}
