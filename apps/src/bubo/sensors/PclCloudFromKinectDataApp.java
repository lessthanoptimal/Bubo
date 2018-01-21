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

package bubo.sensors;

import boofcv.alg.depth.VisualDepthOps;
import boofcv.io.calibration.CalibrationIO;
import boofcv.openkinect.UtilOpenKinect;
import boofcv.struct.calib.CameraPinholeRadial;
import boofcv.struct.image.GrayU16;
import bubo.io.pcl.PointCloudLibraryPcdWriter;
import georegression.struct.point.Point3D_F64;
import org.ddogleg.struct.FastQueue;

import java.io.IOException;

/**
 * Reads in kinect data and outputs a PCL file
 *
 * @author Peter Abeles
 */
public class PclCloudFromKinectDataApp {
	public static void main(String[] args) throws IOException {
		CameraPinholeRadial intrinsic = CalibrationIO.load("data/kinect/trashcan/intrinsic.xml");

		String inputDirectory = "data/kinect/trashcan";
		String[] inputNames = new String[]{"01","02"};

		String[] fileNames = new String[inputNames.length];
		for (int i = 0; i < inputNames.length; i++) {
			fileNames[i] = inputDirectory+"/depth"+inputNames[i]+".depth";
		}

		GrayU16 depth = new GrayU16(1,1);
		for (int i = 0; i < inputNames.length; i++) {
			System.out.println("Processing "+fileNames[i]);
			UtilOpenKinect.parseDepth(fileNames[i], depth, null);
			FastQueue<Point3D_F64> cloud = new FastQueue<Point3D_F64>(Point3D_F64.class,true);
			VisualDepthOps.depthTo3D(intrinsic, depth, cloud);

			// convert to meters
			for( Point3D_F64 p : cloud.toList() ) {
				p.scale(1.0/1000.0);
			}

			PointCloudLibraryPcdWriter.save(cloud.toList(),"cloud"+i+".pcd");
		}
	}
}
