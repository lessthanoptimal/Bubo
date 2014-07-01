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

import bubo.gui.sensors.laser2d.LadarComponent;
import bubo.gui.sensors.laser2d.LaserHistogram;
import bubo.io.rawlog.data.CObservation2DRangeScan;

import javax.swing.*;

/**
 * Prints out information on the scanner and displays the actual scan in an image.
 *
 * @author Peter Abeles
 */
public class VisualizeCObservation2DRangeScan implements LogDataVisualization {

	// display basic numerical information on the range data
	JTextArea textArea = new JTextArea();
	LadarComponent laserArea = new LadarComponent();
	LaserHistogram laserHist = new LaserHistogram();

	public VisualizeCObservation2DRangeScan() {
		laserArea.setAutoRescale(true);
	}

	@Override
	public void setData(Object data) {
		CObservation2DRangeScan info = (CObservation2DRangeScan) data;

		setUpTextArea(info);

		setupScanImage(info);

		laserArea.repaint();
	}

	private void setupScanImage(CObservation2DRangeScan info) {
		int N = info.getScan().length;

		double offsetAngle = info.getDeltaPitch();
		double angDelta = info.getAperture() / N;
		if (!info.isRightToLeft())
			angDelta = -angDelta;
		// I'm guessing that its symmetric
		double startAngle = offsetAngle - angDelta * N / 2.0;
		laserArea.configure(startAngle, angDelta, info.getMaxRange(), N);

		float[] foundRanges = info.getScan();
		double[] ranges = laserArea.getRanges();
		byte[] valid = info.getValidRange();

		// manually copy the range measurements over into the components window
		for (int i = 0; i < N; i++) {
			// see if the laser had a valid measurement at this angle
			if (valid[i] != 0 && foundRanges[i] < info.getMaxRange()) {
				ranges[i] = foundRanges[i];
			} else {
				ranges[i] = 0;
			}
		}
		laserHist.setData(N, ranges, info.getMaxRange());
		laserHist.repaint();
		laserArea.repaint();
	}

	private void setUpTextArea(CObservation2DRangeScan info) {
		String text = VisualizeCObservationImage.basicObservationText(info);
		text += "Sensor Position:\n";
		text += info.getSensorPose().toReadableText();
		text += "\n";
		text += "Aperture:       " + info.getAperture() + "\n";
		text += "Num Scans:      " + info.getScan().length + "\n";
		text += "Right to Left:  " + info.isRightToLeft() + "\n";
		text += "Beam Aperture:  " + info.getBeamAperture() + "\n";
		text += "Delta Pitch:    " + info.getDeltaPitch() + "\n";
		text += "Max Range (m):  " + info.getMaxRange() + "\n";
		text += "Std Error:      " + info.getStdError() + "\n";

		textArea.setText(text);
	}

	@Override
	public Class<?> getType() {
		return CObservation2DRangeScan.class;
	}

	@Override
	public int numDisplay() {
		return 3;
	}

	@Override
	public JComponent getDisplay(int index) {
		switch (index) {
			case 0:
				return textArea;

			case 1:
				return laserArea;

			case 2:
				return laserHist;

			default:
				throw new RuntimeException("Unknown");
		}
	}

	@Override
	public String getDisplayName(int index) {
		switch (index) {
			case 0:
				return "Info";

			case 1:
				return "Scan";

			case 2:
				return "Histogram";

			default:
				throw new RuntimeException("Unknown");
		}
	}
}
