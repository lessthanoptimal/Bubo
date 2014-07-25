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

package bubo.gui.jme;

import bubo.gui.UtilDisplayBubo;
import bubo.gui.d3.PointCloudPanel;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class VisualCheckVectors {
	public static void main(String[] args) {

		JmeFactoryVisualization3D factory = new JmeFactoryVisualization3D();
		PointCloudPanel panel = factory.displayPointCloud();
		panel.setPreferredSize(new Dimension(640,480));

		UtilDisplayBubo.createWindow(panel, panel, "Vectors");

		List<Point3D_F64> points = new ArrayList<Point3D_F64>();
		List<Vector3D_F64> direction = new ArrayList<Vector3D_F64>();
		for (int i = 0; i < 100; i++) {
			points.add( new Point3D_F64(i*0.1,0,0) );
			direction.add( new Vector3D_F64(0,0.5,i*0.02) );
		}

		panel.addVectors(points,direction , 0xFFFF0000);
		panel.addPoints(points , 0xFF0000FF, 2 );
	}
}
