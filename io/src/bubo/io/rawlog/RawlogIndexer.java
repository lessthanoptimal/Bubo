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

import bubo.io.UtilStrings;
import bubo.io.logs.IndexedFileObjectRef;
import bubo.io.logs.LogFileObjectRef;
import bubo.io.rawlog.data.CObservation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an index of a rawlog binary log file.  The index is the location of each object in the log file.
 * Only uncompressed files can be indexed because there is no good way to randomly access a compressed file.
 * Its process while parsing the file can be following by using a {@link Listener listener}.
 *
 * @author Peter Abeles
 */
public class RawlogIndexer {

	// input stream
	private FileInputStream fis;

	// channel used to lookup data structures
	private FileChannel channel;

	// parses the rawlog file
	private RawlogDecoder decoder;

	// listener for progress while reading lg files
	private Listener listener;

	// list of all the data types it found
	private List<Class<?>> allDataTypes = new ArrayList<Class<?>>();

	// list of all the sources it found
	private List<String> allSources = new ArrayList<String>();

	public RawlogIndexer(String fileName) throws FileNotFoundException {
		fis = new FileInputStream(fileName);
		channel = fis.getChannel();
		try {
			// gzip file's can't be indexed since the index will refer to the location in
			// the compressed file, which does not have a 1 to 1 ratio with the decompressed file
			decoder = new RawlogDecoder(fis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected RawlogIndexer() {
	}

	/**
	 * Add a listener that reports the indexing status and can cancel the indexing process.
	 */
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public List<Class<?>> getAllDataTypes() {
		return allDataTypes;
	}

	public List<String> getAllSources() {
		return allSources;
	}

	/**
	 * Reads in a rawlog file and indexes the location of each data element inside the file for quick lookup later on
	 *
	 * @return List of object types and locations in the file
	 * @throws FileNotFoundException
	 */
	public List<LogFileObjectRef> computeIndexes() {
		List<LogFileObjectRef> ret = new ArrayList<LogFileObjectRef>();
		allSources.clear();
		allDataTypes.clear();

		try {
			long location = channel.position();

			while (true) {
				RawlogSerializable o = decoder.decode();

				// set up the object locaiton description and make a list of all the unique data source/type locations
				IndexedFileObjectRef a = new IndexedFileObjectRef();
				a.dataType = o.getClass();
				a.setFileLocation(location);
				if (o instanceof CObservation) {
					a.source = ((CObservation) o).getSensorLabel();

					if (a.source != null)
						UtilStrings.checkAddString(allSources, a.source);
					else
						a.source = RawlogIndexFile.NO_SOURCE;
				}

				if (!allDataTypes.contains(a.dataType))
					allDataTypes.add(a.dataType);

				ret.add(a);

				location = channel.position();

				if (listener != null) {
					double fractionProcessed = (double) location / (double) channel.size();
					listener.update(a.dataType.getSimpleName(), fractionProcessed);

					if (listener.cancelRequested()) {
						return null;
					}
				}
			}

		} catch (IOException e) {
			System.out.println(e);
		}

		try {
			fis.close();
		} catch (IOException e) {
		}

		return ret;
	}

	public static interface Listener {
		public void update(String name, double fraction);

		public boolean cancelRequested();
	}
}
