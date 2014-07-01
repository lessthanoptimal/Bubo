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

package bubo;

import boofcv.io.image.UtilImageIO;
import bubo.gui.LogoComponent;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains parameters that can turns functionality on and off globally.
 *
 * @author Peter Abeles
 */
public class BuboIOConfig {

	public final static boolean SHOW_BUBO_LOGO = true;

	public final static boolean SHOW_DEBUG_GUI = true;

	public final static List<LogoComponent> DEFAULT_LOGOS = new ArrayList<LogoComponent>();

	static {
		if (SHOW_BUBO_LOGO) {
			try {
				URL url = LogoComponent.class.getResource("bubo/gui/bubo_logo.png");
				if (url != null) {
					BufferedImage image = UtilImageIO.loadImage(url);
					DEFAULT_LOGOS.add(new LogoComponent(image));
				}
			} catch (Exception ignore) {
			}
		}
	}

}
