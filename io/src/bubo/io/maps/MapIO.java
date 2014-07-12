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
import bubo.maps.d2.lines.LineSegmentMap;
import georegression.struct.line.LineSegment2D_F64;

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
