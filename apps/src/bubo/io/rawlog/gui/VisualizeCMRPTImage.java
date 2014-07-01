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

package bubo.io.rawlog.gui;

import boofcv.io.image.UtilImageIO;
import bubo.gui.data.BufferedImageComponent;
import bubo.io.data.ImageEncoded;
import bubo.io.data.encoded.ImageFile;
import bubo.io.rawlog.RawlogViewer;
import bubo.io.rawlog.data.CMRPTImage;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * @author Peter Abeles
 */
public class VisualizeCMRPTImage implements LogDataVisualization {

	CMRPTImage obs;

	// display the image
	JPanel imageComponent = new JPanel();
	// displays text information about the image
	JTextArea infoComponent = new JTextArea();

	// components that go inside imageComponent
	JLabel imageText = new JLabel("Not Set!");
	BufferedImageComponent imageDisplay = new BufferedImageComponent(null);


	RawlogViewer.Config config;

	public VisualizeCMRPTImage(RawlogViewer.Config config) {
		this.config = config;
		imageComponent.add(new JLabel("Not set"));
	}

	@Override
	public void setData(Object data) {
		this.obs = (CMRPTImage) data;

		ImageEncoded encoded = obs.getImage();

		BufferedImage img;
		String imageInfo;

		if (encoded instanceof ImageFile) {
			imageInfo = config.directory + "/Images/" + ((ImageFile) encoded).getFileLocation();
			img = UtilImageIO.loadImage(imageInfo);
			imageInfo = "File Name: " + imageInfo;
		} else {
			img = encoded.convertToBuffered();
			imageInfo = "Format: " + encoded.getFormat();
		}

		imageComponent.removeAll();
		if (img == null) {
			imageText.setText("No Image Loaded: " + imageInfo);
			imageComponent.add(imageText);
		} else {
			imageDisplay.setImage(img);
			imageComponent.add(imageDisplay);
		}

		String text = "ImageType = " + encoded.getClass().getSimpleName() + "\n";
		text += imageInfo;

		infoComponent.setText(text);
	}

	@Override
	public Class<?> getType() {
		return CMRPTImage.class;
	}

	@Override
	public int numDisplay() {
		return 2;
	}

	@Override
	public JComponent getDisplay(int index) {
		if (index == 0)
			return infoComponent;
		else if (index == 1)
			return imageComponent;

		return null;
	}

	@Override
	public String getDisplayName(int index) {
		if (index == 0)
			return "Info";
		else if (index == 1)
			return "Image";
		return null;
	}
}
