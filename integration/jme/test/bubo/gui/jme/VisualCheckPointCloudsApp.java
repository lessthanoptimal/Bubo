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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tests the point cloud visualization.
 *
 * 1) Adds a mono color point cloud
 * 2) Adds a random color point cloud
 * 3) Closes the window
 * 4) Opens a new one with a new point cloud
 * 5) Changes the camera parameters.
 *
 * @author Peter Abeles
 */
public class VisualCheckPointCloudsApp {

	public static int PAUSE = 3000;

	public static void main(String[] args) {
		int N = 1000;
		Random rand = new Random(234);
		JPanel gui = new JPanel();
		gui.setLayout(new BorderLayout());
		gui.add(BorderLayout.WEST,new JLabel("Test"));

		JmeFactoryVisualization3D factory = new JmeFactoryVisualization3D();
		PointCloudPanel panel = factory.displayPointCloud();
		panel.setPreferredSize(new Dimension(640,480));
		gui.add(BorderLayout.CENTER,panel);

		JFrame frame = UtilDisplayBubo.createWindow(gui, panel, "Cloud 1");

		System.out.println("Mono color point cloud");
		panel.addPoints(createRandomCloud(0, N, rand),0x00FF00,1);
		sleep(PAUSE);
		System.out.println("Colorized point cloud");
		int[] colors = createRandomColors(N, rand);
		panel.addPoints(createRandomCloud(1.5, N, rand), colors, 3);
		sleep(PAUSE);
		System.out.println("Closing the window");
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		sleep(1000);

		System.out.println("Creating a new GUI");
		gui = new JPanel();
		gui.setLayout(new BorderLayout());
		gui.add(BorderLayout.WEST,new JLabel("Test"));
		panel = factory.displayPointCloud();
		panel.setPreferredSize(new Dimension(640,480));
		gui.add(BorderLayout.CENTER,panel);
		UtilDisplayBubo.createWindow(gui, panel, "Cloud 1");

		System.out.println("New point cloud");
		panel.addPoints(createRandomCloud(0, N, rand),0xFF0000,1);
		sleep(PAUSE);

		System.out.println("Changing camera");

		System.out.println("Finished");
		System.exit(0);
	}

	private static void sleep(long time) {
		long target = System.currentTimeMillis()+time;
		while( target > System.currentTimeMillis()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
			}
		}
	}

	private static int[] createRandomColors(int N, Random rand) {
		int[] colors = new int[N];
		for (int i = 0; i < N; i++) {
			colors[i] = rand.nextInt();
		}
		return colors;
	}

	private static List<Point3D_F64> createRandomCloud( double offset , int N, Random rand) {
		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		for (int i = 0; i < N; i++) {
			Point3D_F64 p = new Point3D_F64(offset+rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			cloud.add(p);
		}
		return cloud;
	}
}
