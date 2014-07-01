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
 * Image stored in an known format.
 *
 * @author Peter Abeles
 */
public class ImageUnknown implements ImageEncoded {

	byte[] data;
	int length;

	public ImageUnknown(byte[] data, int length) {
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
		return "unknown";
	}

	@Override
	public ImageBase convertToImage() {
		return null;
	}

	@Override
	public BufferedImage convertToBuffered() {
		return null;
	}
}
