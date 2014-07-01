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

package bubo.io.logs;

import java.util.List;


/**
 * Interface for reading log files.  This is designed to seamlessly support indexed log files and
 * log files that are read entirely into memory.
 *
 * @author Peter Abeles
 */
public interface LogFileReader {

	/**
	 * Reads the specified log file.  Blocks until it is finished.  Its process
	 * can be monitored by adding a listener.
	 *
	 * @param fileName The log file being read.
	 * @return If the log file was successfully read or not.
	 */
	public boolean load(String fileName);

	/**
	 * Returns the object references found in the log file.
	 *
	 * @return Object references.
	 */
	public List<LogFileObjectRef> getReferences();

	/**
	 * Loads an instance of an object from the log file given a reference to the object.
	 *
	 * @param ref Reference that describes how to find the object,
	 * @return An instance of the object.
	 */
	public <T> T getObject(LogFileObjectRef ref);

	/**
	 * Sets a progress listener.
	 *
	 * @param listener Listener.
	 */
	public void setListener(Listener listener);

	/**
	 * Tell the file reader that it should stop reading the input file and load() should exit.
	 */
	public void cancelLoadRequest();

	/**
	 * Used for the display of process information.
	 */
	public static interface Listener {
		/**
		 * @param message      Message describing the latest progress.
		 * @param fractionRead number from 0 to 1 that describes how far along it is, where 1 is finished.
		 */
		public void logLoadProcess(String message, double fractionRead);
	}

}
