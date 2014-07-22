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

package bubo.simulation;

import boofcv.gui.image.ShowImages;
import bubo.gui.maps.MapDisplay;
import bubo.io.maps.MapIO;
import com.thoughtworks.xstream.XStream;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class SelectRobotWayPointsApp extends MapDisplay
		implements MouseListener, KeyListener {

	List<Point2D_F64> path = new ArrayList<Point2D_F64>();

	public SelectRobotWayPointsApp( String wallsName , String landmarksName ) {
		try {
			setMapWalls(MapIO.loadLineSegmentMap(wallsName));
		} catch( RuntimeException ignore ) {}
		try {
			setMapLandmarks(MapIO.loadLandmarkMap(landmarksName));
		} catch( RuntimeException ignore ) {}
		autoPreferredSize();
		addMouseListener(this);
		addKeyListener(this);
		grabFocus();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if( e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
			System.out.println("saving path to path.xml");
			XStream xstream = new XStream();
			try {
				xstream.toXML(path, new FileOutputStream("path.xml"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		final Point2D_F64 p = new Point2D_F64();
		imageToMap(e.getX(), e.getY(), p);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				path.add(p);
				repaint();
			}
		});
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;

		int r = 4;
		int w = r*2 + 1;

		Point2D_I32 imageP0 = new Point2D_I32();
		Point2D_I32 imageP1 = new Point2D_I32();

		g2.setColor(Color.BLUE);
		g2.setStroke(new BasicStroke(3));
		for (int i = 1; i < path.size(); i++) {
			Point2D_F64 p = path.get(i-1);
			mapToImage(p.x,p.y,imageP0);
			p = path.get(i);
			mapToImage(p.x,p.y,imageP1);

			g2.drawLine(imageP0.x, imageP0.y, imageP1.x, imageP1.y);
		}

		g2.setColor(Color.RED);
		for (int i = 0; i < path.size(); i++) {
			Point2D_F64 p = path.get(i);
			mapToImage(p.x,p.y,imageP0);

			g2.fillOval(imageP0.x-r,imageP0.y-r,w,w);
		}
	}

	public static void main(String[] args) {
//		SelectRobotWayPointsApp app = new SelectRobotWayPointsApp("walls.csv","landmarks.csv");
//		SelectRobotWayPointsApp app = new SelectRobotWayPointsApp("walls.csv",null);
		SelectRobotWayPointsApp app = new SelectRobotWayPointsApp(null,"landmarks.csv");
		JFrame frame = ShowImages.showWindow(app, "Way Points");
		frame.addKeyListener(app);
	}
}
