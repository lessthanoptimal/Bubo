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

import bubo.io.rawlog.data.CObservationGPS;

import javax.swing.*;

/**
 * Creates a GUI for displaying GPS information.
 *
 * @author Peter Abeles
 */
public class VisualizeCObservationGPS implements LogDataVisualization {

	JTextArea textArea = new JTextArea();

	/**
	 * Prints out basic GPS information about the estimated position and if other data structures are available.
	 */
	@Override
	public void setData(Object data) {
		CObservationGPS gps = (CObservationGPS) data;

		String text = VisualizeCObservationImage.basicObservationText(gps);
		text += "\n";
		text += "Sensor Position:\n";
		text += gps.getSensorPose().toReadableText();
		text += "\n";
		text += "GGA  = " + gps.isHas_GGA_datum() + "\n";
		text += "PZS  = " + gps.isHas_PZS_datum() + "\n";
		text += "RMC  = " + gps.isHas_RMC_datum() + "\n";
		text += "SATS = " + gps.isHas_SATS_datum() + "\n";
		text += "\n";
		if (gps.isHas_GGA_datum())
			text += printGGA(gps.GGA_datum);
		textArea.setText(text);
	}

	private String printGGA(CObservationGPS.TGPSDatum_GGA data) {
		String text = "";

		text += "--------- Datum GGA ---------\n";
		text += "Longitude:       " + data.longitude_degrees + "\n";
		text += "Latitude:        " + data.latitude_degrees + "\n";
		text += "Altitude (m):    " + data.altitude_meters + "\n";
		text += "Num. Satellites: " + data.satellitesUsed + "\n";
		text += "Fix Quality:     " + fixQuality(data.fix_quality) + "\n";
		if (data.thereis_HDOP)
			text += "HDOP:            " + data.HDOP + "\n";
		else
			text += "No HDOP\n";
		text += "\n";

		return text;
	}

	private String fixQuality(int value) {
		switch (value) {
			case 0:
				return "invalid";

			case 1:
				return "GPS fix (SPS)";

			case 2:
				return "DGPS fix";

			case 3:
				return "PPS fix";

			case 4:
				return "Real Time Kinematic";

			case 5:
				return "Float RTK";

			case 6:
				return "Estimated (dead reckoning)";

			case 7:
				return "Simulation Mode";

			default:
				return "Unknown quality " + value;
		}
	}

	@Override
	public Class<?> getType() {
		return CObservationGPS.class;
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
		return "GPS";
	}
}
