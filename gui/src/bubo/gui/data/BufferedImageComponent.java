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

package bubo.gui.data;

import bubo.gui.InfoDisplay;

import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Displays a buffered image.
 *
 * @author Peter Abeles
 */
public class BufferedImageComponent extends InfoDisplay {
	// the image that is being displayed
	BufferedImage img;

	public BufferedImageComponent(BufferedImage img) {
		setImage(img);
	}

	private void changeSize(BufferedImage img) {
		setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());
	}

	/**
	 * Changes the image being displayed.
	 *
	 * @param img
	 */
	public void setImage(BufferedImage img) {
		this.img = img;
		if (img != null)
			changeSize(img);
	}

	@Override
	public void paintComponent(Graphics g) {
		// save the reference locally to avoid the obscure chance that the image is changed while
		// this is being drawn
		BufferedImage img = this.img;

		//draw the image
		if (img != null)
			g.drawImage(img, 0, 0, this);
	}
}
