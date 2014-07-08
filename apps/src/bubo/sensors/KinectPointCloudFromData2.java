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
import boofcv.alg.geo.PerspectiveOps;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.gui.d3.PointCloudViewer;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.UtilImageIO;
import boofcv.openkinect.UtilOpenKinect;
import boofcv.struct.FastQueueArray_I32;
import boofcv.struct.calib.IntrinsicParameters;
import boofcv.struct.image.ImageUInt16;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;
import bubo.gui.FactoryVisualization3D;
import bubo.gui.UtilDisplayBubo;
import bubo.gui.d3.PointCloudPanel;
import georegression.struct.point.Point3D_F64;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;
import org.ejml.data.DenseMatrix64F;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class KinectPointCloudFromData2 {

	public static final double MAX_DISTANCE = 1.5;

	public static void main(String[] args) throws IOException {
		String baseDir = "data/kinect/trashcan/";

		String nameRgb = baseDir+"rgb01.jpg";
		String nameDepth = baseDir+"depth01.depth";
		String nameCalib = baseDir+"intrinsic.xml";

		IntrinsicParameters param = UtilIO.loadXML(nameCalib);

		ImageUInt16 depth = new ImageUInt16(1,1);

		BufferedImage input = UtilImageIO.loadImage(nameRgb);

		MultiSpectral<ImageUInt8> rgb =
				new MultiSpectral<ImageUInt8>(ImageUInt8.class,input.getWidth(),input.getHeight(),3);
		ConvertBufferedImage.convertFromMulti(input,rgb,true,ImageUInt8.class);
		UtilOpenKinect.parseDepth(nameDepth,depth,null);

		FastQueue<Point3D_F64> cloud = new FastQueue<Point3D_F64>(Point3D_F64.class,true);
		FastQueueArray_I32 cloudColor = new FastQueueArray_I32(3);

		VisualDepthOps.depthTo3D(param, rgb, depth, cloud, cloudColor);

		DenseMatrix64F K = PerspectiveOps.calibrationMatrix(param, null);

		PointCloudViewer viewer = new PointCloudViewer(K, 0.05);
		viewer.setPreferredSize(new Dimension(rgb.width,rgb.height));

		GrowQueue_I32 colors = new GrowQueue_I32();
		for( int i = 0; i < cloud.size; i++ ) {
			Point3D_F64 p = cloud.get(i);
			p.scale(-1.0/1000.0);
//			double x = -p.x; double y = -p.y; double z = p.z;
//			p.set(x,y,z);
			int[] color = cloudColor.get(i);
			colors.add((0xFF << 24) | (color[0] << 16) | (color[1] << 8) | color[2]);
		}

		FactoryVisualization3D factory = UtilDisplayBubo.createVisualize3D();

		PointCloudPanel gui =  factory.displayPointCloud();

		gui.addPoints(cloud.toList(),colors.data,2);

		ShowImages.showWindow(gui,"Two point clouds");
	}

}
