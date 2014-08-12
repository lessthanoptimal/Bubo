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
import bubo.maps.d2.LandmarkMap2D;
import bubo.maps.d2.lines.LineSegmentMap;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.struct.se.Se2_F64;
import georegression.struct.shapes.Rectangle2D_F64;
import georegression.transform.se.SePointOps_F64;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Base class for displaying 2D maps
 *
 * @author Peter Abeles
 */
public class MapDisplay extends SpacialDisplay {
	// transform from map center to world
	protected Se2_F64 centerToWorld = new Se2_F64();

	protected boolean drawGrid = true;
	protected double gridWidth = 2.0;
	protected Color gridColor =  new Color(220, 220, 220);

	LineSegmentMap mapWalls;
	LandmarkMap2D mapLandmarks;

	public MapDisplay() {
		showCoordinateAxis("Y", true, "X", true);
		showScale(false,4.0);
	}

	/**
	 * Specifies the center of the view in map coordinates
	 *
	 * @param centerToWorld transform from view center to map world coordinates
	 */
	public void setViewCenter( final Se2_F64 centerToWorld ) {
		this.centerToWorld.set(centerToWorld);
	}

	public void setMapWalls(final LineSegmentMap mapWalls) {
		this.mapWalls = mapWalls;
	}

	public LineSegmentMap getMapWalls() {
		return mapWalls;
	}

	public void setMapLandmarks(final LandmarkMap2D mapLandmarks) {
		this.mapLandmarks = mapLandmarks;
	}

	public LandmarkMap2D getMapLandmarks() {
		return mapLandmarks;
	}

	public void autoPreferredSize() {
		Rectangle2D_F64 bounds;

		if( mapWalls != null ) {
			bounds = mapWalls.computeBoundingRectangle();
		} else if( mapLandmarks != null ) {
			bounds = mapLandmarks.computeBoundingRectangle();
		} else {
			throw new RuntimeException("No map set");
		}

		double buffer = Math.max(bounds.getWidth(),bounds.getHeight())*0.2;
		double width = bounds.getWidth()+buffer;
		double height = bounds.getHeight()+buffer;

		setPreferredSize(new Dimension((int) (width * metersToPixels), (int) (height * metersToPixels)));

	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;

		if( drawGrid ) {
			drawGrid(g2);
		}

		if( mapWalls != null )
			drawWalls(g2);

		if( mapLandmarks != null )
			drawLandmarks(g2);
	}

	private void drawWalls(Graphics2D g2) {
		Point2D_F64 a = new Point2D_F64();
		Point2D_F64 b = new Point2D_F64();
		g2.setColor(Color.BLACK);
		for (int i = 0; i < mapWalls.getLines().size(); i++) {
			LineSegment2D_F64 l = mapWalls.getLines().get(i);

			SePointOps_F64.transformReverse(centerToWorld, l.a, a);
			SePointOps_F64.transformReverse(centerToWorld, l.b, b);

			int x0 = (int)Math.round(a.x*metersToPixels);
			int y0 = (int)Math.round(a.y*metersToPixels);
			int x1 = (int)Math.round(b.x*metersToPixels);
			int y1 = (int)Math.round(b.y*metersToPixels);

			drawLine(g2, x0, y0, x1, y1);
		}
	}

	private void drawLandmarks(Graphics2D g2) {
		int r = 4;
		int w = r*2+1;
		g2.setColor(Color.BLUE);
		Point2D_F64 a = new Point2D_F64();
		for (int i = 0; i < mapLandmarks.getTotal(); i++) {
			Point2D_F64 l = mapLandmarks.getLocation(i);

			SePointOps_F64.transformReverse(centerToWorld, l, a);

			int x = (int)Math.round(a.x*metersToPixels);
			int y = (int)Math.round(a.y*metersToPixels);

			drawOval(g2, x-r,y-r,w,w);
		}
	}

	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
	}

	public double getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(double gridWidth) {
		this.gridWidth = gridWidth;
	}

	/**
	 * Draws a grid in the background
	 */
	protected void drawGrid(Graphics2D g2) {
		Point2D_F64 a = new Point2D_F64();

		int width = getWidth();
		int height = getHeight();

		g2.setColor(gridColor);

		double viewWidth = width/metersToPixels;
		double viewHeight = height/metersToPixels;

		double startX = Math.floor((centerToWorld.T.x - viewWidth/2)/gridWidth)*gridWidth;
		double startY = Math.floor((centerToWorld.T.y - viewHeight/2)/gridWidth)*gridWidth;

		int numCols = (int)(viewWidth/gridWidth)+2;
		int numRows = (int)(viewHeight/gridWidth)+2;

		for (int i = 0; i < numRows; i++) {
			a.set(0,startY+i*gridWidth);
			SePointOps_F64.transformReverse(centerToWorld, a, a);
			int y = (int)Math.round(a.y*metersToPixels);
			g2.drawLine(0, height / 2 - 1 - y, width, height / 2 - 1 - y);
		}

		for (int i = 0; i < numCols; i++) {
			a.set(startX+i*gridWidth,0);
			SePointOps_F64.transformReverse(centerToWorld, a, a);
			int x = (int)Math.round(a.x*metersToPixels);
			g2.drawLine(x+width/2,0,x+width/2,height);
		}
	}

	public void mapToImage( double x , double y , Point2D_I32 imagePt ) {

		Point2D_F64 a = new Point2D_F64(x,y);
		SePointOps_F64.transformReverse(centerToWorld, a, a);

		int centerX = getWidth()/2;
		int centerY = getHeight()/2;

		int h = getHeight()-1;

		imagePt.x = (int)Math.round(x*metersToPixels)+centerX;
		imagePt.y = h - ((int)Math.round(y*metersToPixels)+centerY);
	}

	public void imageToMap( int x , int y , Point2D_F64 mapPt ) {

		int h = getHeight();

		x -= getWidth()/2;
		y = (h-1) - (h/2+y);

		// convert the point in the center of the pixel.  smaller expected error
		mapPt.set((x+0.5) / metersToPixels, (y+0.5) / metersToPixels);
		SePointOps_F64.transform(centerToWorld,mapPt,mapPt);
	}

	protected void drawLine( Graphics2D g2 , int x0 , int y0 , int x1 , int y1 ) {
		int centerX = getWidth()/2;
		int centerY = getHeight()/2;

		int h = getHeight()-1;
		g2.drawLine(x0+centerX,h-y0-centerY,x1+centerX,h-y1-centerY);
	}

	protected void drawOval( Graphics2D g2 , int x, int y, int width, int height ) {
		int centerX = getWidth()/2;
		int centerY = getHeight()/2;

		int w = getWidth()-1;
		int h = getHeight()-1;
		int y1 = y+height-1;
		g2.drawOval(x+centerX,h-y1-centerY,width,height);
	}

	protected void drawEllipseRotated( Graphics2D g2 , int x, int y, double minor, double major , double phi ) {
		int centerX = getWidth()/2;
		int centerY = getHeight()/2;

		int h = getHeight()-1;

		AffineTransform rotate = new AffineTransform();
		rotate.rotate(-phi);

		// TODO make sure this is correct
		double ww = major*metersToPixels * 2;
		double hh = minor*metersToPixels * 2;

		Shape shape = rotate.createTransformedShape(new Ellipse2D.Double(-ww/2,-hh/2,ww,hh));
		shape = AffineTransform.getTranslateInstance(x+centerX,h-y-centerY).createTransformedShape(shape);

		g2.draw(shape);
	}
}
