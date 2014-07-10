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

package bubo.io.pcl;

import georegression.struct.point.Point3D_F64;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

/**
 * Outputs data in PCL compatible file formats
 *
 * @author Peter Abeles
 */
public class PointCloudLibraryPcdWriter {

	/**
	 * Saves a point cloud with no color information in PCD format
	 */
	public static void save( List<Point3D_F64> cloud ,  String fileName ) throws FileNotFoundException {

		PrintStream out = new PrintStream(fileName);

		out.println(
				"# .PCD v.7 - Point Cloud Data file format\n" +
				"VERSION .7\n" +
				"FIELDS x y z\n" +
				"SIZE 4 4 4\n" +
				"TYPE F F F\n" +
				"COUNT 1 1 1\n" +
				"WIDTH "+cloud.size()+"\n" +
				"HEIGHT 1\n" +
				"VIEWPOINT 0 0 0 1 0 0 0\n" +
				"POINTS "+cloud.size()+"\n" +
				"DATA ascii");

		for (int i = 0; i < cloud.size(); i++) {
			Point3D_F64 p = cloud.get(i);

			out.printf("%f %f %f\n",p.x,p.y,p.z);
		}
		out.close();
	}

	/**
	 * Saves a point cloud with color information in PCD format
	 */
	public static void save( List<Point3D_F64> cloud ,  int[] rgb ,  String fileName ) throws FileNotFoundException {

		PrintStream out = new PrintStream(fileName);

		out.println(
				"# .PCD v.7 - Point Cloud Data file format\n" +
						"VERSION .7\n" +
						"FIELDS x y z rgb\n" +
						"SIZE 4 4 4 4\n" +
						"TYPE F F F F\n" +
						"COUNT 1 1 1 1\n" +
						"WIDTH "+cloud.size()+"\n" +
						"HEIGHT 1\n" +
						"VIEWPOINT 0 0 0 1 0 0 0\n" +
						"POINTS "+cloud.size()+"\n" +
						"DATA ascii");

		for (int i = 0; i < cloud.size(); i++) {
			Point3D_F64 p = cloud.get(i);

			out.printf("%f %f %f %f\n",p.x,p.y,p.z,(float)rgb[i]);
		}
		out.close();
	}
}
