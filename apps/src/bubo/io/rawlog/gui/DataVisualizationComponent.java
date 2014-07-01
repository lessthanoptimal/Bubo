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
 * Panel that switches between different {@link LogDataVisualization} as the data object
 * being viewed changes.
 *
 * @author Peter Abeles
 */
public class DataVisualizationComponent extends JTabbedPane {


	LogDataVisualization vis;

	public DataVisualizationComponent() {
		setNoTarget();
	}

	/**
	 * If nothing is selected then it should switch to this view.
	 */
	public void setNoTarget() {
		vis = null;
		removeAll();

		JLabel textLabel = new JLabel("Nothing here yet");
		textLabel.setFont(textLabel.getFont().deriveFont(Font.ITALIC));
		textLabel.setHorizontalAlignment(JLabel.CENTER);

		addTab("Nothing", textLabel);
	}

	/**
	 * Changes the display to the specified visualization and view
	 */
	public void setTarget(LogDataVisualization vis, int view) {

		// clear the previous view
		if (this.vis != vis) {
			removeAll();

			for (int i = 0; i < vis.numDisplay(); i++) {
				addTab(vis.getDisplayName(i), vis.getDisplay(i));
			}
			this.vis = vis;
		}

		setSelectedIndex(view);

		repaint();
	}

	/**
	 * Returns which view is currently active
	 */
	public int getActiveView() {
		return getSelectedIndex();
	}
}
