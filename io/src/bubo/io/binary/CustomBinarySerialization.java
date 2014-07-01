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

package bubo.io.binary;

import bubo.io.serialization.SerializationDescription;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * More complex objects with parameters whose values are dependent upon other parameter's values can't be serialized using
 * the standard algorithm.  Instead they require a custom serialization, which this class provides an interface
 * for.
 *
 * @author Peter Abeles
 */
public interface CustomBinarySerialization extends SerializationDescription {

	public void decode(DataInput input);

	public void encode(DataOutput output);
}
