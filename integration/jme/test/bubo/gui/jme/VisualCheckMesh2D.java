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
import georegression.geometry.ConvertRotation3D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se3_F64;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class VisualCheckMesh2D {
	public static void main(String[] args) {

		JmeFactoryVisualization3D factory = new JmeFactoryVisualization3D();
		PointCloudPanel panel = factory.displayPointCloud();
		panel.setPreferredSize(new Dimension(640,480));

		UtilDisplayBubo.createWindow(panel, panel, "Mesh2D");

		// tilt it towards the camera
		Se3_F64 meshToWorld = new Se3_F64();
		meshToWorld.getT().set(0,0,0);

		List<Point2D_F64> points = new ArrayList<Point2D_F64>();
		points.add(new Point2D_F64(0,0));
		points.add(new Point2D_F64(0,2));
		points.add(new Point2D_F64(1,2));
		points.add(new Point2D_F64(1,0));

		// has CW ordering.  Convex
		panel.addMesh2D(meshToWorld, points , 0xFFFF0000);

		meshToWorld = new Se3_F64();
		meshToWorld.getT().set(0.5,0,1);

		points = new ArrayList<Point2D_F64>();
		points.add(new Point2D_F64(0,0));
		points.add(new Point2D_F64(2,0));
		points.add(new Point2D_F64(1.2,1));
		points.add(new Point2D_F64(2,2));
		points.add(new Point2D_F64(0,2));

		// has CCW ordering.  Concave
		panel.addMesh2D(meshToWorld, points , 0xA000FF00 );

		meshToWorld = new Se3_F64();
		meshToWorld.getT().set(0.5,0,1);
		ConvertRotation3D_F64.eulerToMatrix(EulerType.XYZ,-3,1,0,meshToWorld.R);

		points = new ArrayList<Point2D_F64>();
		points.add(new Point2D_F64(0,0));
		points.add(new Point2D_F64(2,0));
		points.add(new Point2D_F64(1.2,1));
		points.add(new Point2D_F64(2,2));
		points.add(new Point2D_F64(0,2));
		points.add(new Point2D_F64(0.5,1));

		// has CCW ordering.  Concave.  Rotated
		panel.addMesh2D(meshToWorld, points , 0xA00000FF );
	}
}
