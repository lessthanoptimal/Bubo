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
import boofcv.io.UtilIO;
import boofcv.openkinect.UtilOpenKinect;
import boofcv.struct.calib.VisualDepthParameters;
import boofcv.struct.image.ImageUInt16;
import georegression.struct.point.Point3D_F64;
import org.ddogleg.struct.FastQueue;

import java.io.IOException;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class KinectPointCloudFromData {
	public static void main(String[] args) throws IOException {
		VisualDepthParameters intrinsic = UtilIO.loadXML("data/kinect/intrinsic.xml");

		List<Point3D_F64> cloudA = loadPointCloud("01");
		List<Point3D_F64> cloudB = loadPointCloud("02");



	}

	public static List<Point3D_F64> loadPointCloud( String which ) throws IOException {
		VisualDepthParameters intrinsic = UtilIO.loadXML("data/kinect/intrinsic.xml");

		String nameDepth = "data/kinect/trashcan/depth"+which+".depth";

		ImageUInt16 depth = new ImageUInt16(1,1);

		UtilOpenKinect.parseDepth(nameDepth, depth, null);

		FastQueue<Point3D_F64> cloud = new FastQueue<Point3D_F64>(Point3D_F64.class,true);

		VisualDepthOps.depthTo3D(intrinsic.visualParam, depth, cloud);

		return cloud.toList();
	}
}
