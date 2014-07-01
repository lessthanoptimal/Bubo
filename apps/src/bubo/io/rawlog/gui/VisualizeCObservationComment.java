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

package bubo.io.rawlog.gui;

import bubo.io.rawlog.data.CObservationComment;

import javax.swing.*;

/**
 * Displays text contained in a comment.
 *
 * @author Peter Abeles
 */
public class VisualizeCObservationComment implements LogDataVisualization {
	CObservationComment comment;

	JTextArea text;

	public VisualizeCObservationComment() {
		text = new JTextArea();
	}

	@Override
	public void setData(Object data) {
		comment = (CObservationComment) data;

		text.setText(comment.getText());
	}

	@Override
	public Class<?> getType() {
		return CObservationComment.class;
	}

	@Override
	public int numDisplay() {
		return 1;
	}

	@Override
	public JComponent getDisplay(int index) {
		return text;
	}

	@Override
	public String getDisplayName(int index) {
		return "Text";
	}
}
