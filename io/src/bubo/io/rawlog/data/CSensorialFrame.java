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
 * @author Peter Abeles
 */
public class CSensorialFrame extends CObservation implements RawlogSerializableCustom {


	CObservation obs[];

	@Override
	public int getVersion() {
		return 2;
	}

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		if (version > 2)
			throw new RuntimeException("Version not supported");

		try {
			if (version < 2) {
				LittleEndianIO.readInt(decoder.getInput());
			}

			if (version == 0) {
				throw new RuntimeException("It should read in a timestamp here");
			}

			int N = LittleEndianIO.readInt(decoder.getInput());
			obs = new CObservation[N];

			for (int i = 0; i < N; i++) {
				obs[i] = (CObservation) decoder.decodeObject();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void customEncoding(OutputStream output) {
		throw new RuntimeException("Implement");
	}

	public CObservation[] getObs() {
		return obs;
	}

	public void setObs(CObservation[] obs) {
		this.obs = obs;
	}
}
