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

import bubo.io.logs.LogFileObjectRef;
import bubo.io.logs.LogFileReader;
import bubo.io.logs.MemoryFileObjectRef;
import bubo.io.rawlog.data.CObservation;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;


/**
 * Reads the entire rawlog file into memory.  Rawlog files can be gziped or not.
 * Creates references of type {@link MemoryFileObjectRef}.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class RawlogFileMemoryReader implements LogFileReader {

	// status listener
	private Listener listener;

	// is the input file gzipped?
	private boolean gziped;
	// list of all the references
	private List<LogFileObjectRef> refs = new ArrayList<LogFileObjectRef>();

	// the exception that caused it to fail
	private Exception e;

	// has a request to cancel been made?
	private boolean canceled;

	/**
	 * Creates a reader for a rawlog file.
	 *
	 * @param gziped True if the input file is gziped or false if it is not.
	 */
	public RawlogFileMemoryReader(boolean gziped) {
		this.gziped = gziped;
		canceled = false;
	}

	@Override
	public boolean load(String fileName) {
		refs.clear();
		e = null;

		try {
			FileInputStream fis = new FileInputStream(fileName);
			FileChannel channel = fis.getChannel();
			InputStream in = fis;

			if (gziped) {
				in = new GZIPInputStream(in);
			}
			long size = new File(fileName).length();

			RawlogDecoder decoder = new RawlogDecoder(in);

			RawlogSerializable o = decoder.decode();

			while (o != null && !canceled) {
//                System.out.println("pos = "+channel.position());
				Class<?> type = o.getClass();
				String source = CObservation.class.isAssignableFrom(type) ?
						((CObservation) o).getSensorLabel() : null;

				refs.add(new MemoryFileObjectRef(type, source, o));

				updateProcess(type.getSimpleName(), channel.position() / (double) size);
				o = decoder.decode();
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
			this.e = e;
			return false;
		} catch (IOException e) {
			if (!(e instanceof EOFException)) {
				System.out.println(e);
				this.e = e;
				return false;
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.out.println(e);
			this.e = e;
			return false;
		}
		return !canceled;
	}

	public Exception getException() {
		return e;
	}

	private void updateProcess(String message, double fractionRead) {
		if (listener != null) {
			listener.logLoadProcess(message, fractionRead);
		}
	}

	@Override
	public void cancelLoadRequest() {
		this.canceled = true;
	}

	@Override
	public List<LogFileObjectRef> getReferences() {
		return refs;
	}

	@Override
	public <T> T getObject(LogFileObjectRef ref) {
		return (T) ((MemoryFileObjectRef) ref).getData();
	}

	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
	}
}
