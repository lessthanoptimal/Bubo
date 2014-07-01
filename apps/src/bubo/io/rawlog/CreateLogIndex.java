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
import bubo.io.logs.LogFileObjectRef;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;


/**
 * Creates a new index of the currently opened log file.  If an index already exists it will be written over.
 * If the file is compressed a dialog box will open asking if it should be decompressed before proceeding.
 *
 * @author Peter Abeles
 */
public class CreateLogIndex extends SwingWorker implements RawlogIndexer.Listener {

	// the component which dialog boxes are opened relative to
	private Component parent;
	// the viewer which called this class
	private RawlogViewer owner;

	// process monitor dialog
	private ProgressMonitor monitor;

	// the log file that is being indexed
	private File file;

	public CreateLogIndex(Component parent, RawlogViewer owner) {
		this.parent = parent;
		this.owner = owner;
	}

	/**
	 * Spawns a new SwingWorker thread that will create an index of the provided log file.
	 *
	 * @param originalFile The log file which is to be indexed.
	 */
	public void start(String originalFile) {
		file = new File(originalFile);
		execute();
	}

	@Override
	protected Object doInBackground() throws Exception {
		// see if the input was an index.  in which case it is rebuilding the index
		if (file.getName().endsWith(".index")) {
			String p = file.getAbsolutePath();
			file = new File(p.substring(0, p.length() - 6));
		}

		if (UtilCompression.isGzipFile(file)) {
			if (checkDecompress()) {
				// decompress the file and show the status
				ProgressMonitor monitor = new ProgressMonitor(parent, "Decompressing Log File", "", 0, 1000);
				File output = new File(file.getAbsolutePath() + ".decomp");
				UtilCompression.gzipDecompressFile(file.getAbsolutePath(), output.getAbsolutePath(),
						10000, monitor);
				monitor.close();

				//the output is now the input
				file = output;
			} else {
				return null;
			}
		}

		readCreateIndex();

		return null;
	}

	/**
	 * Reads in the indexes and creates the index file
	 */
	private void readCreateIndex() {
		try {
			// read in index files
			monitor = new ProgressMonitor(parent, "Reading in log file indexes...", "", 0, 1000);
			monitor.setProgress(0);
			RawlogIndexer indexer = new RawlogIndexer(file.getAbsolutePath());
			indexer.setListener(this);

			List<LogFileObjectRef> refs = indexer.computeIndexes();
			RawlogIndexFile saveIndex = new RawlogIndexFile(file.getAbsolutePath() + ".index");

			monitor.setNote("Saving index file.");
			monitor.setProgress(0);
			saveIndex.saveIndex(refs, file.getAbsolutePath());
			monitor.setProgress(monitor.getMaximum());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent,
					"Failed to create index file.",
					file.getName(),
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}

	@Override
	protected void done() {
		super.done();
		owner.finishedCreateIndex();

		if (monitor != null) {
			monitor.close();
			monitor = null;
		}
	}

	private boolean checkDecompress() {
		Object[] options = {"Yes", "No"};

		int n = JOptionPane.showOptionDialog(parent,
				"Log file is compressed.",
				"Decompress?",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);

		return n == 0;
	}

	@Override
	public void update(String name, double fraction) {
		int value = (int) (monitor.getMaximum() * fraction);

		monitor.setNote(name);
		monitor.setProgress(value);
	}

	@Override
	public boolean cancelRequested() {
		return monitor.isCanceled();
	}
}
