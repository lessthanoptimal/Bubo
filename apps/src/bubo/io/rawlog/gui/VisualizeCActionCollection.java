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

import bubo.io.rawlog.data.CAction;
import bubo.io.rawlog.data.CActionCollection;

import javax.swing.*;


/**
 * @author Peter Abeles
 */
public class VisualizeCActionCollection implements LogDataVisualization {

	JTextArea textArea = new JTextArea();
	CActionCollection collection;

	@Override
	public void setData(Object data) {
		collection = (CActionCollection) data;

		setUpTextArea(collection);
	}

	private void setUpTextArea(CActionCollection collection) {
		String text = "Action Collection:\n";
		text += "N = " + collection.actions.length + "\n";
		text += "\n";
		for (int i = 0; i < collection.actions.length; i++) {
			CAction c = collection.actions[i];
			text += c.getClass().getSimpleName() + "\n";
		}

		textArea.setText(text);
	}

	@Override
	public Class<?> getType() {
		return CActionCollection.class;
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
		return "Action List";
	}
}
