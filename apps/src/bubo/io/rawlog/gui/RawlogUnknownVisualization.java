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
import java.awt.*;

/**
 * Displays information about unknown data types from rawlog.
 *
 * @author Peter Abeles
 */
public class RawlogUnknownVisualization implements LogDataVisualization {

	JLabel label;

	public RawlogUnknownVisualization() {
		label = new JLabel("Unknown data type");
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
		label.setHorizontalAlignment(JLabel.CENTER);
	}


	@Override
	public void setData(Object data) {
		String text;
		if (data == null) {
			text = "Null Data";
		} else {
			text = "Unknown data type:\n";
			text += data.getClass().getName();
		}

		label.setText(text);
	}

	@Override
	public Class<?> getType() {
		return null;
	}

	@Override
	public int numDisplay() {
		return 1;
	}

	@Override
	public JComponent getDisplay(int index) {
		return label;
	}

	@Override
	public String getDisplayName(int index) {
		return "Info";
	}
}
