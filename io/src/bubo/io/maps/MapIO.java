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

package bubo.io.maps;

import bubo.io.text.ReadCsv;
import bubo.io.text.ReadCsvObject;
import bubo.log.streams.LogLSe2_F64;
import bubo.log.streams.LogPoseRangeBearing;
import bubo.maps.d2.LandmarkMap2D;
import bubo.maps.d2.lines.LineSegmentMap;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.point.Point2D_F64;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Reading and writing maps.
 *
 * @author Peter Abeles
 */
public class MapIO {

	public static List<LogLSe2_F64> loadPath2D(String fileName ) {
		try {
			ReadCsvObject<LogLSe2_F64> reader = new ReadCsvObject<LogLSe2_F64>(new FileInputStream(fileName),
							LogLSe2_F64.class,"time","x","y","yaw");
			reader.setComment('#');
			return reader.readAll();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<LogPoseRangeBearing> loadPoseRangeBearing(String fileName ) {
		try {
			ReadCsvObject<LogPoseRangeBearing> reader =
					new ReadCsvObject<LogPoseRangeBearing>(new FileInputStream(fileName),
							LogPoseRangeBearing.class,"time","x","y","yaw","id","range","bearing");
			reader.setComment('#');
			return reader.readAll();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save( LandmarkMap2D map , String fileName ) {
		try {
			PrintStream out = new PrintStream(fileName);
			out.println("# LandmarkMap2D");
			out.println("# x y");
			for(Point2D_F64 l : map.getLocations()) {
				out.printf("%10f %10f\n",l.x,l.y);
			}
			out.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static LandmarkMap2D loadLandmarkMap(String fileName ) {
		try {
			ReadCsv reader = new ReadCsv(new FileInputStream(fileName));
			reader.setComment('#');
			LandmarkMap2D map = new LandmarkMap2D();
			while( true ) {
				List<String> words = reader.extractWords();
				if( words == null ) return map;
				Point2D_F64 location = new Point2D_F64();
				location.x = Double.parseDouble(words.get(0));
				location.y = Double.parseDouble(words.get(1));
				map.getLocations().add(location);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save( LineSegmentMap map , String fileName ) {
		try {
			PrintStream out = new PrintStream(fileName);
			out.println("# LineSegmentMap");
			out.println("# a.x a.y b.x b.y");
			for(LineSegment2D_F64 l : map.getLines()) {
				out.printf("%10f %10f %10f %10f\n",l.a.x,l.a.y,l.b.x,l.b.y);
			}
			out.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static LineSegmentMap loadLineSegmentMap(String fileName ) {
		try {
			ReadCsv reader = new ReadCsv(new FileInputStream(fileName));
			reader.setComment('#');
			LineSegmentMap map = new LineSegmentMap();
			while( true ) {
				List<String> words = reader.extractWords();
				if( words == null ) return map;
				LineSegment2D_F64 line = new LineSegment2D_F64();
				line.a.x = Double.parseDouble(words.get(0));
				line.a.y = Double.parseDouble(words.get(1));
				line.b.x = Double.parseDouble(words.get(2));
				line.b.y = Double.parseDouble(words.get(3));
				map.getLines().add(line);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
