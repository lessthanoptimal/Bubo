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

import bubo.io.rawlog.data.CPose2D;

import javax.swing.*;


/**
 * @author Peter Abeles
 */
public class VisualizeCPose2D implements LogDataVisualization {

	JTextArea textArea = new JTextArea();

	@Override
	public void setData(Object data) {
		setText((CPose2D) data);
	}

	public void setText(CPose2D pose) {


		String str = "CPose2D\n";
		str += "\n";
		str += "x   = " + pose.getPose().getX() + "\n";
		str += "y   = " + pose.getPose().getY() + "\n";
		str += "yaw = " + pose.getYaw();

		textArea.setText(str);
	}

	@Override
	public Class<?> getType() {
		return CPose2D.class;
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
