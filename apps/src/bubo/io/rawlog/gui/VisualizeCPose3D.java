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

import bubo.io.rawlog.data.CPose3D;
import georegression.geometry.ConvertRotation3D_F64;
import georegression.struct.EulerType;
import org.ejml.data.DMatrixRMaj;

import javax.swing.*;


/**
 * @author Peter Abeles
 */
public class VisualizeCPose3D implements LogDataVisualization {

	JTextArea textArea = new JTextArea();

	@Override
	public void setData(Object data) {
		setText((CPose3D) data);
	}

	public void setText(CPose3D pose) {

		DMatrixRMaj R = pose.getRotation();
		double euler[] = ConvertRotation3D_F64.matrixToEuler(R, EulerType.XYZ, (double[])null);

		String str = "CPose3D\n";
		str += "\n";
		str += "Position " + pose.getPosition() + "\n";
		str += "RotX " + euler[0] + "\n";
		str += "RotY " + euler[1] + "\n";
		str += "RotZ " + euler[2] + "\n";

		textArea.setText(str);
	}

	@Override
	public Class<?> getType() {
		return CPose3D.class;
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
