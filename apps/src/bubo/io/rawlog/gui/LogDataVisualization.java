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
 * <p>
 * Interface for displaying an element in a log.  Each implementing class
 * can only display data for a specific type of data.  The data can be displayed in an arbitrary of
 * "displays" where each display is shown in its own tab in the GUI.  The displays are
 * rendered inside of a JComponent.
 * </p>
 *
 * @author Peter Abeles
 */
public interface LogDataVisualization {

	/**
	 * Specifies which data is to be viewed.
	 *
	 * @param data
	 */
	public void setData(Object data);

	/**
	 * The type of data object that this can visualize
	 *
	 * @return
	 */
	public Class<?> getType();


	public int numDisplay();

	public JComponent getDisplay(int index);

	public String getDisplayName(int index);

	// todo add config dialog
}
