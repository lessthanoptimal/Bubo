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

package bubo.cloud;

import boofcv.gui.image.ShowImages;
import bubo.gui.FactoryVisualization3D;
import bubo.gui.UtilDisplayBubo;
import bubo.gui.d3.PointCloudPanel;
import bubo.io.text.ReadCsvObject;
import georegression.struct.point.Point3D_F64;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays an XYZ formatted point cloud
 *
 * @author Peter Abeles
 */
public class DisplayPointCloudApp {
	public static void main(String[] args) throws FileNotFoundException {
		InputStream in = new FileInputStream("data/pointcloud.txt");
		ReadCsvObject<Point3D_F64> reader = new ReadCsvObject<Point3D_F64>(in, Point3D_F64.class,"x","y","z");

		List<Point3D_F64> cloud = new ArrayList<Point3D_F64>();

		while( true ) {
			try {
				Point3D_F64 p = reader.nextObject(null);
				if (p == null)
					break;
				else {
//					if( p.normSq() > 25 )
//						continue;
					p.y = -p.y;
					p.x = -p.x;
					p.z = -p.z;
					cloud.add(p);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FactoryVisualization3D factory = UtilDisplayBubo.createVisualize3D();
		PointCloudPanel gui =  factory.displayPointCloud();

		gui.addPoints(cloud,0xFF0000,1);

		ShowImages.showWindow(gui, "Two point clouds");
	}
}
