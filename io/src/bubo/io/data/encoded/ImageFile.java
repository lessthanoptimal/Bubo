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

package bubo.io.data.encoded;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import bubo.io.data.ImageEncoded;

import java.awt.image.BufferedImage;


/**
 * Contains a reference to a file that contains the image.
 *
 * @author Peter Abeles
 */
public class ImageFile implements ImageEncoded {

	Class imageType = GrayU8.class;
	String fileLocation;

	public ImageFile(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	@Override
	public byte[] getData() {
		return null;
	}

	@Override
	public int getDataSize() {
		return -1;
	}

	@Override
	public String getFormat() {
		return "file";
	}

	@Override
	public <T extends ImageBase> T convertToImage() {
		BufferedImage buffImg = convertToBuffered();
		return (T) ConvertBufferedImage.convertFromSingle(buffImg, null, imageType);
	}

	@Override
	public BufferedImage convertToBuffered() {
		return UtilImageIO.loadImage(fileLocation);
	}

	public String getFileLocation() {
		return fileLocation;
	}
}
