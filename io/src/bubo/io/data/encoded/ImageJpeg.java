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
import boofcv.struct.image.GrayU8;
import bubo.io.data.ImageEncoded;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * Image stored as a compressed JPEG image.
 *
 * @author Peter Abeles
 */
public class ImageJpeg implements ImageEncoded {

	Class imageType = GrayU8.class;
	byte[] data;
	int length;

	public ImageJpeg(byte[] data, int length) {
		this.data = data;
		this.length = length;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public int getDataSize() {
		return length;
	}

	@Override
	public String getFormat() {
		return "JPEG";
	}

	@Override
	public <T extends boofcv.struct.image.ImageBase> T convertToImage() {
		BufferedImage buffImg = convertToBuffered();
		return (T) ConvertBufferedImage.convertFromSingle(buffImg, null, imageType);
	}

	@Override
	public BufferedImage convertToBuffered() {
		try {
			return ImageIO.read(new ByteArrayInputStream(data));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
