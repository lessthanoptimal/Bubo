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

package bubo.io.rawlog.data;

import bubo.io.rawlog.RawlogSerializableStandard;

/**
 * <p>
 * Java implementation of  mrpt/slam/CObservationComment.h. Comment from original source:
 * </p>
 * <p/>
 * <p>
 * This "observation" is actually a placeholder for a text block with comments or additional parameters attached to a given rawlog file.
 * There should be only one of this observations in a rawlog file, and it's recommended to insert/modify them from the application RawlogViewer.
 * </p>
 *
 * @author Peter Abeles
 */
public class CObservationComment extends CObservation implements RawlogSerializableStandard {

	private String text;


	public String[] getVariableOrder(int version) {
		return new String[]{"text", "timestamp"};
	}

	public int getVersion() {
		return 0;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
