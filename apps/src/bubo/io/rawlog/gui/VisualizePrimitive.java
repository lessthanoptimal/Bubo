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

import javax.swing.*;


/**
 * Displays information on primitive java data structures (e.g. Long, Double, Byte).
 *
 * @author Peter Abeles
 */
public class VisualizePrimitive implements LogDataVisualization {

	JTextArea textArea = new JTextArea();
	Class<?> type;

	public VisualizePrimitive(Class<?> type) {
		this.type = type;
	}

	@Override
	public void setData(Object data) {
		if (data == null) {
			textArea.setText("NULL");
		} else {
			String text = "";

			if (data.getClass().isEnum()) {
				text = "Enum: " + data.getClass().getSimpleName() + "\n";
			} else {
				text = "Class: " + data.getClass().getSimpleName() + "\n";
			}

			text += "toString():\n";
			text += data.toString();
			textArea.setText(text);
		}

	}


	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public int numDisplay() {
		return 1;
	}

	@Override
	public JComponent getDisplay(int index) {
		return textArea;
	}

	@Override
	public String getDisplayName(int index) {
		return "Text Info";
	}
}
