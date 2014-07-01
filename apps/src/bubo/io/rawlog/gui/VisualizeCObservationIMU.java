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

import bubo.io.rawlog.data.CObservationIMU;

import javax.swing.*;

/**
 * Displays information from a {@link CObservationIMU} in a JComponent.
 *
 * @author Peter Abeles
 */
public class VisualizeCObservationIMU implements LogDataVisualization {

	JTextArea textArea = new JTextArea();

	/**
	 * Creates a text area for displaying position information and raw data.
	 */
	@Override
	public void setData(Object data) {
		CObservationIMU o = (CObservationIMU) data;

		String text = VisualizeCObservationImage.basicObservationText(o);

		text += "Sensor Position:\n";
		text += o.getSensorPose().toReadableText();
		text += "\n";

		text += "Data Present: [ ";
		for (boolean b : o.getDataIsPresent()) {
			text += b + " ";
		}
		text += "]\n";
		text += "\n";
		text += "Raw Data: [ ";
		for (double b : o.getRawMeasurements()) {
			text += String.format("%1.3e ", b);
		}
		text += "]\n";

		textArea.setText(text);
	}

	@Override
	public Class<?> getType() {
		return CObservationIMU.class;
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
		return "Odometry";
	}
}
