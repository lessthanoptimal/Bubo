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

package bubo.io.data;

import boofcv.struct.image.ImageBase;

import java.awt.image.BufferedImage;


/**
 * An image encoded in a format which is not fully supported.  For example it could be raw JPEG data.
 * This class provides a convenient way to convert it into a standard format as needed, leaving the
 * option for it to be stored in its compressed format.
 *
 * @author Peter Abeles
 */
public interface ImageEncoded {

	/**
	 * Returns the raw encoded data.  If not supported null is returned.
	 *
	 * @return raw encoded image data.
	 */
	byte[] getData();

	/**
	 * The size of the encoded data.  If not supported -1 is returned.
	 *
	 * @return size of encoded data.
	 */
	int getDataSize();

	/**
	 * Name of the format the image is encoded in.
	 */
	public String getFormat();

	/**
	 * Convert the image into a standard image format.  If this decoding can't be done
	 * null is returned.  If not supported null is returned.
	 *
	 * @return Equivalent image in standard format.
	 */
	<T extends ImageBase> T convertToImage();

	/**
	 * Converts the image into a buffered image for ease of display.
	 * If not supported null is returned.
	 *
	 * @return Buffered image.
	 */
	BufferedImage convertToBuffered();
}
