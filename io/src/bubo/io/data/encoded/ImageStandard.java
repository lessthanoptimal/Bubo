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


import boofcv.struct.image.ImageBase;
import bubo.io.data.ImageEncoded;

import java.awt.image.BufferedImage;


/**
 * An image in any of the standard formats.
 *
 * @author Peter Abeles
 */
public class ImageStandard implements ImageEncoded {

	ImageBase image;

	public ImageStandard(ImageBase image) {
		this.image = image;
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
		return "standard";
	}

	@Override
	public <T extends ImageBase> T convertToImage() {
		return (T) image;
	}

	@Override
	public BufferedImage convertToBuffered() {
		return null;
	}
}
