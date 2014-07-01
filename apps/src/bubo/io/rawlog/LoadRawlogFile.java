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

import bubo.io.UtilCompression;
import bubo.io.UtilStrings;
import bubo.io.logs.LogFileObjectRef;
import bubo.io.logs.LogFileReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Handles the process of loading a rawlog file in a swing friendly way.  Checks to see if the file is an index or not or if it
 * is compressed or not.  After loading the file is also creates a list of all the different types and sources
 * contained in the file.
 *
 * @author Peter Abeles
 */
public class LoadRawlogFile extends SwingWorker implements LogFileReader.Listener {

	// object that's reading the log file
	LogFileReader reader;
	// list of all the different data types
	private List<Class<?>> allTypes = new ArrayList<Class<?>>();
	// list of all the different data sources
	private List<String> allSources = new ArrayList<String>();
	// show the reading status
	private ProgressMonitor processMonitor;

	// a way to tell another process that it is done
	private Listener listener;

	// The component that spawned this process
	private Component mainWindow;

	// the file being read
	private File file;

	public LoadRawlogFile(Component mainWindow, Listener listener) {
		this.mainWindow = mainWindow;
		this.listener = listener;
	}

	public void startRead(String fileName) {
		allTypes.clear();
		allSources.clear();

		// first try reading it as a gzip file

		file = new File(fileName);

		execute();
	}

	public LogFileReader getReader() {
		return reader;
	}

	@Override
	protected Object doInBackground() throws Exception {

		if (file.getName().endsWith(".index")) {
			loadMemory(new RawlogFileIndexedReader());
		} else if (UtilCompression.isGzipFile(file)) {
			loadMemory(new RawlogFileMemoryReader(true));
		} else {
			loadMemory(new RawlogFileMemoryReader(false));
		}

		return null;
	}

	private void loadMemory(LogFileReader reader) {
		this.reader = reader;
		reader.setListener(this);

		processMonitor = new ProgressMonitor(mainWindow, "Opening Log File", "", 0, 1000);
		processMonitor.setProgress(0);

		if (reader.load(file.getAbsolutePath())) {
			List<LogFileObjectRef> refs = reader.getReferences();

			processMonitor.setNote("Extracting source and type information...");
			processMonitor.setProgress(0);
			for (LogFileObjectRef r : refs) {
				// reduces the amount of memory used to store equivalent strings
				// by saving the reference to the string in the list
				if (!allTypes.contains(r.getDataType())) {
					// unique instances of Class are provided for each object by java
					allTypes.add(r.getDataType());
				}
				r.source = UtilStrings.checkAddString(allSources, r.getSource());
			}
			processMonitor.setProgress(processMonitor.getMaximum());

		} else {
			// failed to read the file
			this.reader = null;
		}
		processMonitor.close();
		if (listener != null)
			listener.loadFileComplete();
	}

	public boolean isSuccessful() {
		return reader != null;
	}

	public List<Class<?>> getAllTypes() {
		return allTypes;
	}

	public List<String> getAllSources() {
		return allSources;
	}

	@Override
	public void logLoadProcess(String message, double fractionRead) {
		if (processMonitor.isCanceled()) {
			reader.cancelLoadRequest();
		} else {

//        System.out.println("message = "+message+"  "+fractionRead);
			int max = processMonitor.getMaximum();

			int value = (int) (fractionRead * max);

			if (message != null)
				processMonitor.setNote(message);
			processMonitor.setProgress(value);
		}
	}

	public File getFile() {
		return file;
	}

	public static interface Listener {
		public void loadFileComplete();
	}
}
