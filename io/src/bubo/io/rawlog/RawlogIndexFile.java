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
import bubo.io.text.ReadCsvObject;
import bubo.io.text.WriteCsvObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Reads and writes rawlog index files.  Index files are saved in a CSV format.
 *
 * @author Peter Abeles
 */
public class RawlogIndexFile {
	// string used to identify null in the index file
	public static final String NO_SOURCE = "null";

	// name of the file that is to be read or written to
	private String fileName;

	// list of all the data types it found
	private List<Class<?>> allDataTypes = new ArrayList<Class<?>>();

	// list of all the sources it found
	private List<String> allSources = new ArrayList<String>();

	/**
	 * @param fileName Name of the file that the index is to be read or written to.
	 */
	public RawlogIndexFile(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Constructor used for unit testing
	 */
	protected RawlogIndexFile() {
	}

	/**
	 * Saves the index for quick reloading later on
	 *
	 * @param index           List of objects and their file locations
	 * @param out             Output stream where the index is being written to.
	 * @param indexedFileName name of the input file
	 * @throws IOException
	 */
	protected static void saveIndex(List<LogFileObjectRef> index,
									OutputStream out,
									String indexedFileName)
			throws IOException {
		WriteCsvObject writer = new WriteCsvObject(out, Helper.class, "dataType", "fileLocation", "source");
		writer.setCommentChar('#');
		writer.setNullString(NO_SOURCE);

		writer.writeComment(" Indexed rawlog file: " + indexedFileName);

		Helper h = new Helper();
		for (LogFileObjectRef o : index) {
			h.dataType = o.dataType.getName();
			h.source = o.source;
			h.fileLocation = ((IndexedFileObjectRef) o).fileLocation;
			writer.writeObject(h);
		}

		out.close();
	}

	/**
	 * Save the  The index to a file.
	 *
	 * @param index           Index information that is to be saved.
	 * @param indexedFileName Name of the file that was index.  Added to comments.
	 * @throws IOException
	 */
	public void saveIndex(List<LogFileObjectRef> index, String indexedFileName)
			throws IOException {
		FileOutputStream out = new FileOutputStream(fileName);

		saveIndex(index, out, indexedFileName);
	}

	/**
	 * Loads the index file for the rawlog data file
	 *
	 * @return List of object locations.
	 */
	public List<LogFileObjectRef> loadIndex() {
		File fileIndex = new File(fileName);
		if (!fileIndex.exists())
			return null;

		try {
			FileInputStream in = new FileInputStream(fileName);
			return loadIndex(in);
		} catch (Exception e) {
			System.out.println("Read index failed!");
			// something went wrong with the index file.  Delete it and return null
			if (!fileIndex.delete())
				System.err.println("Deleting of index file failed!");
			return null;
		}
	}

	/**
	 * Loads the location of objects in a data file from an index file.
	 */
	public List<LogFileObjectRef> loadIndex(InputStream in) throws IOException {

		List<LogFileObjectRef> ret = new ArrayList<LogFileObjectRef>();


		ReadCsvObject reader = new ReadCsvObject(in, Helper.class, "dataType", "fileLocation", "source");
		reader.setComment('#');

		allDataTypes.clear();
		allSources.clear();

		Helper h = new Helper();
		IndexedFileObjectRef o;
		while ((reader.nextObject(h)) != null) {
			o = h.createRef();
			if (o.source.compareTo(NO_SOURCE) != 0) {
				UtilStrings.checkAddString(allSources, o.source);
			}
			if (!allDataTypes.contains(o.dataType))
				allDataTypes.add(o.dataType);

			ret.add(o);
		}

		in.close();


		return ret;
	}

	public List<Class<?>> getAllDataTypes() {
		return allDataTypes;
	}

	public List<String> getAllSources() {
		return allSources;
	}

	/**
	 * IndexedFileObjectRef uses Class<?> for its data type.  This cannot be written
	 * to a CSV file because it is not a primitive type.  To get around this problem
	 * it is converted into a string and read in using this class instead.
	 */
	public static class Helper {
		String dataType;
		long fileLocation;
		String source;

		public IndexedFileObjectRef createRef() {
			try {
				Class<?> type = Class.forName(dataType);
				return new IndexedFileObjectRef(type, source, fileLocation);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public long getFileLocation() {
			return fileLocation;
		}

		public void setFileLocation(long fileLocation) {
			this.fileLocation = fileLocation;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}
	}
}
