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

package bubo.io.rawlog;

import bubo.io.logs.IndexedFileObjectRef;
import bubo.io.logs.LogFileObjectRef;
import bubo.io.logs.LogFileReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;


/**
 * Reads the location of objects in the file from an index file for quick lookup later on.
 * This only works on decompressed rawlog files.  Objects are only loaded into memory upon request.
 * Making it good in low memory situations.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class RawlogFileIndexedReader implements LogFileReader {

	// used to change the location it is reading from in the log file
	private FileChannel channel;
	// log file decoder
	private RawlogDecoder decoder;

	// list of references that contain the object's location in the log file
	private List<LogFileObjectRef> refs;

	@Override
	public boolean load(String fileName) {
		RawlogIndexFile reader = new RawlogIndexFile(fileName);

		refs = reader.loadIndex();

		// remove the .index from the end
		String origFileName = fileName.substring(0, fileName.length() - 6);

		try {
			FileInputStream fis = new FileInputStream(origFileName);
			channel = fis.getChannel();
			decoder = new RawlogDecoder(fis);
		} catch (FileNotFoundException e) {
			refs = null;
		} catch (IOException e) {
			refs = null;
		}

		return refs != null;
	}

	@Override
	public List<LogFileObjectRef> getReferences() {
		return refs;
	}

	@Override
	public <T> T getObject(LogFileObjectRef ref) {
		try {
			IndexedFileObjectRef loc = (IndexedFileObjectRef) ref;

			channel.position(loc.fileLocation);
			return (T) decoder.decode();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void cancelLoadRequest() {

	}

	@Override
	public void setListener(Listener listener) {
	}
}
