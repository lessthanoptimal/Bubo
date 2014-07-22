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
import bubo.maps.d2.LandmarkMap2D;
import georegression.struct.point.Point2D_F64;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Creates a map with landmarks.
 *
 * @author Peter Abeles
 */
public class CreateLandmarkMapApp extends MapDisplay
		implements MouseListener , KeyListener {


	public CreateLandmarkMapApp() {
		setMapLandmarks(new LandmarkMap2D());
		addMouseListener(this);
		addKeyListener(this);
		grabFocus();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if( e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
			System.out.println("saving map to landmarks.csv");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MapIO.save(getMapLandmarks(), "landmarks.csv");
				}
			});
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		final Point2D_F64 where = new Point2D_F64();
		imageToMap(e.getX(),e.getY(),where);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getMapLandmarks().add(where.x, where.y);
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

	public static void main(String[] args) {
		CreateLandmarkMapApp app = new CreateLandmarkMapApp();
		app.setPreferredSize(new Dimension(500,500));
		JFrame frame = ShowImages.showWindow(app,"Map Maker");
		frame.addKeyListener(app);
	}
}
