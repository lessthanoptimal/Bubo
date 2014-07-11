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

package bubo.gui.maps;

import bubo.gui.SpacialDisplay;
import bubo.maps.d2.lines.LineSegmentMap;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;
import georegression.struct.shapes.Rectangle2D_F64;
import georegression.transform.se.SePointOps_F64;

import java.awt.*;

/**
 *
 *
 * @author Peter Abeles
 */
public class LineMapDisplay extends SpacialDisplay {

	LineSegmentMap map;

	// transform from map center to world
	Se2_F64 centerToWorld = new Se2_F64();

	public LineMapDisplay() {
		showCoordinateAxis("Y", true, "X", true);
	}

	public void setMap( final LineSegmentMap map ) {
		this.map = map;
	}

	/**
	 * Specifies the center of the view in map coordinates
	 *
	 * @param centerToWorld transform from view center to map world coordinates
	 */
	public void setViewCenter( final Se2_F64 centerToWorld ) {
		this.centerToWorld.set(centerToWorld);
	}

	public void autoPreferredSize() {
		Rectangle2D_F64 bounds = map.computeBoundingRectangle();

		double width = bounds.getWidth()*1.1;
		double height = bounds.getHeight()*1.1;

		System.out.println("width "+width+" h "+height);

		setPreferredSize(new Dimension((int) (width * metersToPixels), (int) (height * metersToPixels)));
	}

	@Override
	public void paintComponent(Graphics g) {
		if( map == null )
			return;

		Graphics2D g2 = (Graphics2D)g;

		int centerX = getWidth()/2;
		int centerY = getHeight()/2;

		Point2D_F64 a = new Point2D_F64();
		Point2D_F64 b = new Point2D_F64();

		for (int i = 0; i < map.getLines().size(); i++) {
			LineSegment2D_F64 l = map.getLines().get(i);

			SePointOps_F64.transformReverse(centerToWorld,l.a,a);
			SePointOps_F64.transformReverse(centerToWorld, l.b, b);

			int x0 = (int)(a.x*metersToPixels) + centerX;
			int y0 = (int)(a.y*metersToPixels) + centerY;
			int x1 = (int)(b.x*metersToPixels) + centerX;
			int y1 = (int)(b.y*metersToPixels) + centerY;

			g2.drawLine(x0,y0,x1,y1);
		}
	}
}
