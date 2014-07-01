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

package bubo.io.logs;

/**
 * <p>
 * Specifies how to get an object in a log file as well as the type of data it is and
 * where the data came from.  If the type or source values are not applicable then they
 * are set to null.
 * </p>
 *
 * @author Peter Abeles
 */
public class LogFileObjectRef {
	// the type of data object
	public Class<?> dataType;

	// which sensor it came from
	public String source;

	public LogFileObjectRef() {
	}

	public LogFileObjectRef(Class<?> dataType, String source) {
		this.dataType = dataType;
		this.source = source;
	}

	@Override
	public String toString() {
		return dataType.getSimpleName();
	}

	public Class<?> getDataType() {
		return dataType;
	}

	public void setDataType(Class<?> dataType) {
		this.dataType = dataType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
