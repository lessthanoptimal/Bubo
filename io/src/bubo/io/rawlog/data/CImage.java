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

package bubo.io.rawlog.data;

import boofcv.struct.image.GrayU8;
import bubo.io.LittleEndianIO;
import bubo.io.data.ImageEncoded;
import bubo.io.data.encoded.ImageFile;
import bubo.io.data.encoded.ImageJpeg;
import bubo.io.data.encoded.ImageStandard;
import bubo.io.rawlog.RawlogDecoder;
import bubo.io.rawlog.RawlogSerializableCustom;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Rawlog class for storing images.
 *
 * @author Peter Abeles
 */
public class CImage implements RawlogSerializableCustom {

	// if stored internally
	private ImageEncoded image;

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		if (version <= 1)
			throw new RuntimeException("Version not supported");

		try {
			boolean externalStorage;

			if (version >= 6)
				externalStorage = LittleEndianIO.readBoolean(decoder.getInput());
			else
				externalStorage = false;

			if (externalStorage) {
				String externalFileName = decoder.readString();
				image = new ImageFile(externalFileName);
			} else {
				boolean hasColor = LittleEndianIO.readBoolean(decoder.getInput());

				if (hasColor) {
					boolean loadJPEG = true;

					if (version >= 7) {
						throw new RuntimeException("not supported");
					}

					if (loadJPEG) {
						int size = LittleEndianIO.readInt(decoder.getInput());
						byte jpegData[] = decoder.readByteArray(size);

						image = new ImageJpeg(jpegData, size);
					}

				} else {
					int width = LittleEndianIO.readInt(decoder.getInput());
					int height = LittleEndianIO.readInt(decoder.getInput());
					int origin = LittleEndianIO.readInt(decoder.getInput());
					int imageSize = LittleEndianIO.readInt(decoder.getInput());

					if (version == 2) {
						// read in raw bytes
						GrayU8 img = new GrayU8();
						img.data = decoder.readByteArray(imageSize);
						img.width = width;
						img.height = height;
						this.image = new ImageStandard(img);
					} else {
						boolean storedAsZip = LittleEndianIO.readBoolean(decoder.getInput());

						if (storedAsZip) {
							throw new RuntimeException("Zip compression not supported yet");
						} else {
							throw new RuntimeException("Normal images not supported yet");
						}
					}
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public ImageEncoded getImage() {
		return image;
	}

	public void setImage(ImageEncoded image) {
		this.image = image;
	}

	@Override
	public int getVersion() {
		return 7;
	}
}
