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

import bubo.io.rawlog.data.CObservation;
import bubo.io.rawlog.data.CSensorialFrame;

import javax.swing.*;


/**
 * @author Peter Abeles
 */
public class VisualizeCSensorialFrame implements LogDataVisualization {

	CSensorialFrame frame;
	JTextArea textArea = new JTextArea();

	@Override
	public void setData(Object data) {
		frame = (CSensorialFrame) data;

		setText(frame);
	}

	public void setText(CSensorialFrame frame) {

		CObservation[] obs = frame.getObs();

		String str = "CSensorialFrame\n";
		str += " N = " + obs.length + "\n";
		str += "\n";
		for (int i = 0; i < obs.length; i++) {
			str += obs[i].getClass().getSimpleName() + "\n";
		}

		textArea.setText(str);
	}

	@Override
	public Class<?> getType() {
		return CSensorialFrame.class;
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
		return "Observation List";
	}
}
