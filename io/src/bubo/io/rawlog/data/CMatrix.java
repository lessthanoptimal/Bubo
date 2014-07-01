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

import bubo.io.LittleEndianIO;
import bubo.io.rawlog.RawlogDecoder;
import bubo.io.rawlog.RawlogSerializableCustom;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Rawlogs class for storing floating point matrices in a row major format.
 *
 * @author Peter Abeles
 */
public class CMatrix implements RawlogSerializableCustom {

	int numRows;
	int numColumns;
	float[][] data;

	public CMatrix(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		data = new float[numRows][numColumns];
	}

	public CMatrix() {
	}

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		if (version != 0)
			throw new RuntimeException("Unexpected version: " + version);

		try {
			numRows = LittleEndianIO.readInt(decoder.getInput());
			numColumns = LittleEndianIO.readInt(decoder.getInput());

			data = new float[numRows][];

			for (int i = 0; i < numRows; i++) {
				data[i] = decoder.readFloatArray(numColumns);
			}

		} catch (IOException e) {
			throw new RuntimeException("Custom deserialization failed", e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public float get(int row, int col) {
		return data[row][col];
	}

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public float[][] getData() {
		return data;
	}

	public void setData(float[][] data) {
		this.data = data;
	}

	@Override
	public int getVersion() {
		return 0;
	}
}
