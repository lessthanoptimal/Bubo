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

package bubo.io.rawlog;

/**
 * Custom serialization can be performed using {@link bubo.io.rawlog.RawlogSerializableCustom}.
 *
 * @author Peter Abeles
 */
public interface RawlogSerializableStandard extends RawlogSerializable {

	/**
	 * Returns a list of variables which are serializable and the order in which they are to be serialized.
	 *
	 * @param version Which serialization version is being read/written to.
	 * @return List of variable names and the order they are serialized.
	 */
	public String[] getVariableOrder(int version);

}
