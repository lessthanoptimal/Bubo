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

import bubo.io.logs.LogFileObjectRef;
import bubo.io.logs.LogFileReader;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Display for log files.  A user selectable list of log file data is shown in the left and a visual
 * display of the selected item is shown in the right.  For each type of data to be shown correctly
 * a {@link LogDataVisualization} must be provided.
 * </p>
 *
 * @author Peter Abeles
 */
public class DataLogIndexSplitPane extends JSplitPane implements DataListComponent.SelectionListener {

	VisualizationState stateUnknown;
	Map<String, VisualizationState> visualizations = new HashMap<String, VisualizationState>();
	// what is currently being displayed
	VisualizationState active;
	// what was previously displayed
	VisualizationState prev;
	// where the data is visualized
	private DataVisualizationComponent dataVis;
	// where data can be selected from a list
	private DataListComponent dataList;

	public DataLogIndexSplitPane() {
		dataList = new DataListComponent(this);

		dataVis = new DataVisualizationComponent();

		JScrollPane pictureScrollPane = new JScrollPane(dataVis);

		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setLeftComponent(dataList);
		setRightComponent(pictureScrollPane);
		setDividerLocation(250);

		//Provide minimum sizes for the two components in the split pane.
		Dimension minimumSize = new Dimension(150, 150);
		dataList.setMinimumSize(minimumSize);
		pictureScrollPane.setMinimumSize(minimumSize);

		//Provide a preferred size for the split pane.
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(getPreferredSize());
	}

	/**
	 * Select the next item in the list.  Useful when playing a lot file
	 *
	 * @return If it is NOT at the last element.
	 */
	public boolean selectNextItem() {
		return dataList.selectNextItem();
	}

	/**
	 * Specifies the visualizer that is used when the data type is known.
	 *
	 * @param vis
	 */
	public void setUnknownVisualizer(LogDataVisualization vis) {
		stateUnknown = new VisualizationState();
		stateUnknown.activeWindow = 0;
		stateUnknown.vis = vis;
	}

	/**
	 * Adds a new general purpose visualizer
	 *
	 * @param visualizer
	 */
	public void addVisualizaton(LogDataVisualization visualizer) {
		VisualizationState state = new VisualizationState();
		state.type = visualizer.getType();
		state.activeWindow = 0;
		state.vis = visualizer;

		visualizations.put(visualizer.getType().getName(), state);
	}

	/**
	 * Provides a list of objects read in the from the log file and the reader for
	 * accessing that information
	 *
	 * @param listData
	 * @param reader
	 */
	public void setObjectList(List<LogFileObjectRef> listData,
							  LogFileReader reader) {
		prev = null;
		dataList.setObjectList(listData, reader);
	}

	/**
	 * Called when the user has selected a new object for viewing.
	 */
	@Override
	public void selectionChanged(Object d) {

		// save the previous view
		if (active != null)
			active.activeWindow = dataVis.getActiveView();

		VisualizationState v = d == null ? null : visualizations.get(d.getClass().getName());

		boolean changed = prev != v;

		if (v == null) {
			if (stateUnknown == null)
				throw new RuntimeException("Must set the unknown state visualizer!");
			active = stateUnknown;
		} else {
			active = v;
		}

		// only repaint or change the view when needed
		active.vis.setData(d);
		if (changed)
			dataVis.setTarget(active.vis, active.activeWindow);
		else
			dataVis.repaint();

		prev = v;
	}

	/**
	 * Saves the current state of a visualization object.  When a user changes to another data type
	 * and comes back the same tab will be visible.  Making it easier to stream through
	 * data in a movie format.
	 */
	private static class VisualizationState {
		Class<?> type;
		int activeWindow;
		LogDataVisualization vis;
	}
}
