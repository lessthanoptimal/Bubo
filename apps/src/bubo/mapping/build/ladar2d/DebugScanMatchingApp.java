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

package bubo.mapping.build.ladar2d;

import boofcv.gui.image.ShowImages;
import bubo.clouds.fit.Lrf2dScanToScan;
import bubo.clouds.fit.s2s.Lrf2dScanToScan_LocalICP;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.desc.sensors.lrf2d.Lrf2dPrecomputedTrig;
import bubo.gui.sensors.laser2d.ComparePoints2DPanel;
import bubo.io.serialization.SerializationDefinitionManager;
import bubo.io.text.ReadCsvObjectSmart;
import bubo.log.streams.PositionRangeArrayData;
import bubo.struct.StoppingCondition;
import com.thoughtworks.xstream.XStream;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class DebugScanMatchingApp {

	public static final int FRAME = 663;
	public static final String directory = "data/mapping2d/sim02/";
	public static final String fileName = directory+"observations.txt";

	public static void main(String[] args) throws IOException
	{
		Lrf2dParam param = (Lrf2dParam)new XStream().fromXML(new FileInputStream(directory+"lrf.xml"));
		PositionRangeArrayData data = new PositionRangeArrayData(param.getNumberOfScans());

		SerializationDefinitionManager def = new SerializationDefinitionManager();
		def.loadDefinition(PositionRangeArrayData.class, "timeStamp", "scanToWorld", "range");
		def.loadDefinition(Se2_F64.class, "x", "y", "yaw");

		ReadCsvObjectSmart<PositionRangeArrayData> reader = new ReadCsvObjectSmart<PositionRangeArrayData>(
				new FileInputStream(fileName), def, PositionRangeArrayData.class.getSimpleName());
		reader.setComment('#');
		reader.setIgnoreUnparsedData(true);  // TODO read hokuyo config and make sure this is needed

		Lrf2dScanToScan scanMatching = new Lrf2dScanToScan_LocalICP(new StoppingCondition(20, 0.0001), 200, 0.20);
		Lrf2dPrecomputedTrig trig = new Lrf2dPrecomputedTrig(param);

		ComparePoints2DPanel gui = new ComparePoints2DPanel();
		gui.setPixelsPerMeter(40);
		gui.setGridWidth(0.10);

		double prev[] = new double[ param.getNumberOfScans() ];
		Se2_F64 pose1to0 = new Se2_F64();

		int frame = 0;
		double mr = param.getMaxRange();
		while( reader.nextObject(data) != null ) {

			if( frame++ == FRAME ) {
				Se2_F64 pose2to0 = data.getScanToWorld();
				Se2_F64 pose2to1 = pose2to0.concat(pose1to0.invert(null),null);
				System.out.println(pose2to1);
				gui.addPoints(compute(prev,trig,mr),new Color(170,170,255));
				gui.addPoints(compute(data.getRange(),trig,mr),new Color(255,0,0));
				break;
			}

//			if( frame == FRAME-10 ) {
				pose1to0.set(data.getScanToWorld());
				System.arraycopy(data.getRange(), 0, prev, 0, param.getNumberOfScans());
//			}
		}

		gui.setPreferredSize(new Dimension(500,500));
		ShowImages.showWindow(gui, "Points");
	}

	public static List<Point2D_F64> compute( double []ranges , Lrf2dPrecomputedTrig trig , double maxRange ) {
		List<Point2D_F64> ret = new ArrayList<Point2D_F64>();

		for (int i = 0; i < ranges.length; i++) {
			Point2D_F64 p = new Point2D_F64();
			if( ranges[i] < maxRange ) {
				trig.computeEndPoint(i, ranges[i], p);
				ret.add(p);
			}
		}

		return ret;
	}
}
