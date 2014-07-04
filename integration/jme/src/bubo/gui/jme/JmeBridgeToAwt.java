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

package bubo.gui.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import java.awt.*;

/**
 * @author Peter Abeles
 */
// TODO add BUBO logo
public class JmeBridgeToAwt extends SimpleApplication {

	Canvas canvas;

	public JmeBridgeToAwt() {
		AppSettings settings = new AppSettings(true);
		settings.setWidth(640);
		settings.setHeight(480);
		settings.setFrameRate(30);

		setDisplayStatView(false);
		setDisplayFps(false);
		setPauseOnLostFocus(false);
		setSettings(settings);
		createCanvas();
		startCanvas();

		JmeCanvasContext context = (JmeCanvasContext)getContext();
		canvas = context.getCanvas();
		canvas.setSize(settings.getWidth(), settings.getHeight());
	}

	public Canvas getCanvas()
	{
		return canvas;
	}

	@Override
	public void simpleInitApp() {
		flyCam.setDragToRotate(true);
		flyCam.setMoveSpeed(5);
	}
}
