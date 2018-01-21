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
import boofcv.gui.image.ShowImages;
import boofcv.io.calibration.CalibrationIO;
import boofcv.openkinect.UtilOpenKinect;
import boofcv.struct.calib.CameraPinholeRadial;
import boofcv.struct.image.GrayU16;
import bubo.clouds.FactoryFitting;
import bubo.clouds.fit.MatchCloudToCloud;
import bubo.gui.FactoryVisualization3D;
import bubo.gui.UtilDisplayBubo;
import bubo.gui.d3.PointCloudPanel;
import bubo.struct.StoppingCondition;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import georegression.transform.se.SePointOps_F64;
import org.ddogleg.struct.FastQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class KinectPointCloudFromData {

	public static final double MAX_DISTANCE = 1.5;

	public static void main(String[] args) throws IOException {
		List<Point3D_F64> cloudA = loadPointCloud("01");
		List<Point3D_F64> cloudB = loadPointCloud("02");

		MatchCloudToCloud<Se3_F64,Point3D_F64> fit = FactoryFitting.cloudIcp3D(1,new StoppingCondition(100,1e-4,1e-6));

		List<Point3D_F64> originA = new ArrayList<Point3D_F64>();
		for (int i = 0; i < cloudA.size(); i++) {
			originA.add(cloudA.get(i).copy());
		}

		long time0 = System.currentTimeMillis();
		fit.setSource(cloudA);
		fit.setDestination(cloudB);
		long time1 = System.currentTimeMillis();
		System.out.println("Set cloud time: "+(time1-time0)/1000.0);

		System.out.println("Start fitting");
		if( !fit.compute() )
			throw new RuntimeException("Matching failed!");

		Se3_F64 srcToDst = fit.getSourceToDestination();
		long time2 = System.currentTimeMillis();
		System.out.println("Register time: "+(time2-time1)/1000.0);

		System.out.println(srcToDst);


		for (int i = 0; i < originA.size(); i++) {
			Point3D_F64 a = originA.get(i);
			SePointOps_F64.transform(srcToDst, a, cloudA.get(i));
		}
		System.out.println("Done fitting");

		FactoryVisualization3D factory = UtilDisplayBubo.createVisualize3D();

		PointCloudPanel gui =  factory.displayPointCloud();

		gui.addPoints(cloudA,0xFF0000,1);
		gui.addPoints(cloudB,0x00FF00,1);
		gui.addPoints(originA,0x0000FF,1);

		ShowImages.showWindow(gui,"Two point clouds");
	}

	public static List<Point3D_F64> loadPointCloud( String which ) throws IOException {
		CameraPinholeRadial intrinsic = CalibrationIO.load("data/kinect/trashcan/intrinsic.xml");

		String nameDepth = "data/kinect/trashcan/depth"+which+".depth";

		GrayU16 depth = new GrayU16(1,1);

		UtilOpenKinect.parseDepth(nameDepth, depth, null);

		FastQueue<Point3D_F64> cloud = new FastQueue<Point3D_F64>(Point3D_F64.class,true);

		VisualDepthOps.depthTo3D(intrinsic, depth, cloud);

		List<Point3D_F64> out = new ArrayList<Point3D_F64>();

		for (int i = 0; i < cloud.size(); i++) {
			Point3D_F64 p = cloud.get(i);
			p.scale(1.0/1000.0);

			double r = p.normSq();

			double x = -p.x;
			double y = -p.y;
			double z = p.z;
			p.set(x,y,z);

			if( r <= MAX_DISTANCE*MAX_DISTANCE ) {
				out.add(p);
			}
		}

		System.out.println("Total points "+cloud.size()+"  after pruning "+out.size());

		return out;
	}
}
