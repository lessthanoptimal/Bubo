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

import boofcv.gui.image.ShowImages;
import bubo.gui.FactoryVisualization3D;
import bubo.gui.d3.JmePointCloudPanel;
import bubo.gui.d3.PointCloudPanel;
import georegression.struct.point.Point3D_F64;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class JmeFactoryVisualization3D implements FactoryVisualization3D {
	JmeBridgeToAwt bridge = new JmeBridgeToAwt();

	@Override
	public PointCloudPanel displayPointCloud() {
		JmeBridgeToAwt bridge = new JmeBridgeToAwt();
		return new JmePointCloudPanel(bridge);
	}

	public static void main(String[] args) {
		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		Random rand = new Random(234);
		int N = 1000;
		for (int i = 0; i < N; i++) {
			Point3D_F64 p = new Point3D_F64(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());

			cloud.add(p);
		}

		JPanel gui = new JPanel();
		gui.setLayout(new BorderLayout());
		gui.add(BorderLayout.WEST,new JLabel("Test"));

		JmeFactoryVisualization3D factory = new JmeFactoryVisualization3D();
		PointCloudPanel panel = factory.displayPointCloud();
		panel.setPreferredSize(new Dimension(640,480));

//		panel.addPoints(cloud,0x00FF00,1);

		int[] colors = new int[cloud.size()];
		for (int i = 0; i < N; i++) {
			colors[i] = rand.nextInt();
		}
		panel.addPoints(cloud,colors,50);


		gui.add(BorderLayout.CENTER,panel);

		ShowImages.showWindow(gui,"Cloud");

	}
}
