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
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestRawlogIndexFile {
	/**
	 * Save a set of indexes then reload them and see if the two lists contain the same data.  Also se eif
	 * it correctly created a list of all the sources and types it read in
	 */
	@Test
	public void saveIndex_loadIndex_codec() throws IOException {
		List<LogFileObjectRef> orig = new ArrayList<LogFileObjectRef>();

		orig.add(new IndexedFileObjectRef(Long.class, "sourceA", 101));
		orig.add(new IndexedFileObjectRef(Integer.class, "sourceB", 1010));
		orig.add(new IndexedFileObjectRef(Byte.class, "sourceB", 1234));
		orig.add(new IndexedFileObjectRef(Long.class, "sourceC", 1010));

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		RawlogIndexFile.saveIndex(orig, out, "theFileName");

		String text = out.toString();

		ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes());

		RawlogIndexFile indexFile = new RawlogIndexFile();
		List<LogFileObjectRef> found = indexFile.loadIndex(in);

		// see if the read objects is the same as the ones it output
		assertEquals(orig.size(), found.size());
		for (int i = 0; i < orig.size(); i++) {
			IndexedFileObjectRef o = (IndexedFileObjectRef) orig.get(i);
			IndexedFileObjectRef f = (IndexedFileObjectRef) found.get(i);

			assertTrue(o.source.compareTo(f.source) == 0);
			assertTrue(o.dataType == f.dataType);
			assertTrue(o.fileLocation == f.fileLocation);
			// quick sanity check
			assertFalse(o == f);
		}

		// see if it created a list of all the types and sources
		assertEquals(3, indexFile.getAllDataTypes().size());
		assertEquals(3, indexFile.getAllSources().size());
	}
}
