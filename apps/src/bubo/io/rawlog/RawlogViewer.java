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
import bubo.io.rawlog.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and visualizes data contained in a rawlog file.
 *
 * @author Peter Abeles
 */
// TODO if an exception is thrown when using the index, display a dialog saying bad index, delete old index and reload the index.
public class RawlogViewer implements LoadRawlogFile.Listener, ActionListener {

	final JFileChooser fileChooser = new JFileChooser();
	// where all the information is displayed
	DataLogIndexSplitPane mainPanel;
	// the window containing mainPanel
	JFrame window;
	// used to read the raw log file
	LoadRawlogFile openHelper;
	// list of data locations in the file
	List<LogFileObjectRef> indexes;
	// system information and user preferences
	Config config = new Config();
	// dialog for selecting a filter for the data
	FilterDialog filterDialog;
	// thread that controls playback.
	// is null if not playing back
	PlaybackThread playback;
	// menu bar items
	JMenuBar menuBar;
	JMenuItem mQuit;
	JMenuItem mOpen;
	JMenuItem mSave;
	JMenuItem mCreateIndex;
	JMenuItem mSelectAll;
	JMenuItem mDelete;
	JMenuItem mConfig;
	JMenuItem mFilter;

	JButton bFilter;
	JButton bPlay;

	public RawlogViewer() {
		mainPanel = new DataLogIndexSplitPane();
		mainPanel.setUnknownVisualizer(new VisualizePrimitive(null));
		mainPanel.addVisualizaton(new VisualizeCObservationComment());
		mainPanel.addVisualizaton(new VisualizeCObservationImage(config));
		mainPanel.addVisualizaton(new VisualizeCObservationIMU());
		mainPanel.addVisualizaton(new VisualizeCObservationGPS());
		mainPanel.addVisualizaton(new VisualizeCObservation2DRangeScan());
		mainPanel.addVisualizaton(new VisualizeCActionCollection());
		mainPanel.addVisualizaton(new VisualizeCSensorialFrame());
		mainPanel.addVisualizaton(new VisualizeCPose2D());
		mainPanel.addVisualizaton(new VisualizeCPose3D());
		mainPanel.addVisualizaton(new VisualizeCMRPTImage(config));

		window = new JFrame("Log File Viewer");
		window.add(mainPanel, BorderLayout.CENTER);
		addMenuBar(window);
		addToolbar(window);

		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		filterDialog = new FilterDialog(window);
		filterDialog.pack();
		filterDialog.setVisible(false);
	}

	public static void main(String args[]) {
		final String fileName = "/home/pja/data/dataset_corridor2.2_20070308/2007-03MAR-08_Stereo_20fps_pasillo.rawlog.index";

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				RawlogViewer viewer = new RawlogViewer();

				viewer.loadFile(fileName);
				// todo if that fails add select file
			}
		});

	}

	/**
	 * Let the user select a file to open.
	 *
	 * @return if a file was selected and successfully read.
	 */
	public boolean selectFile() {
		// open in the current directory, if one is set
		if (config.directory != null) {
			fileChooser.setCurrentDirectory(new File(config.directory));
		}
		int ret = fileChooser.showOpenDialog(mainPanel);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			loadFile(file.getAbsolutePath());
			return true;
		}
		return false;
	}

	/**
	 * Discard any previously loaded data and load the specified log file.
	 *
	 * @param fileName Name of the file which is to be loaded.
	 * @return if the file was loaded or not.
	 */
	public void loadFile(String fileName) {
		// prevent the user from doing two actions at the same time
		setMenuBarEnabled(false);
		indexes = null;
		openHelper = null;

		openHelper = new LoadRawlogFile(mainPanel, this);
		openHelper.startRead(fileName);
	}

	private void setMenuBarEnabled(boolean state) {
		menuBar.setEnabled(state);
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			menuBar.getMenu(i).setEnabled(state);
		}
	}

	/**
	 * Turns off components to prevent a conflict from happening when playing back
	 * data in a log.
	 *
	 * @param isPlaying is it playing back data or not
	 */
	private void adjustGuiForPlayBack(boolean isPlaying) {
		mOpen.setEnabled(!isPlaying);
		mSave.setEnabled(!isPlaying);
		mCreateIndex.setEnabled(!isPlaying);
		mSelectAll.setEnabled(!isPlaying);
		mDelete.setEnabled(!isPlaying);
		mConfig.setEnabled(!isPlaying);
		mFilter.setEnabled(!isPlaying);
		bFilter.setEnabled(!isPlaying);
	}

	/**
	 * Sets up a menu bar for the applications window.
	 *
	 * @param window window where the menu bar goes.
	 */
	private void addMenuBar(JFrame window) {
		menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		mOpen = new JMenuItem("Open", KeyEvent.VK_O);
		mOpen.addActionListener(this);
		mOpen.setMnemonic(KeyEvent.VK_O);
		mOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		mSave = new JMenuItem("Save");
		mSave.addActionListener(this);
		mCreateIndex = new JMenuItem("Create Index");
		mCreateIndex.addActionListener(this);

		mQuit = new JMenuItem("Quit", KeyEvent.VK_Q);
		mQuit.addActionListener(this);
		mQuit.setMnemonic(KeyEvent.VK_Q);
		mQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		fileMenu.add(mOpen);
		fileMenu.add(mSave);
		fileMenu.add(mCreateIndex);
		fileMenu.add(mQuit);

		JMenu editMenu = new JMenu("Edit");
		mSelectAll = new JMenuItem("Select All");
		mSelectAll.addActionListener(this);
		mDelete = new JMenuItem("Delete");
		mDelete.addActionListener(this);
		editMenu.add(mSelectAll);
		editMenu.add(mDelete);


		JMenu optionMenu = new JMenu("Options");
		mConfig = new JMenuItem("Configure");
		mConfig.addActionListener(this);
		mFilter = new JMenuItem("Filter", KeyEvent.VK_F);
		mFilter.addActionListener(this);
		mFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		mFilter.setMnemonic(KeyEvent.VK_F);
		optionMenu.add(mConfig);
		optionMenu.add(mFilter);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(optionMenu);

		window.setJMenuBar(menuBar);
	}

	private void addToolbar(JFrame window) {
		JToolBar toolbar = new JToolBar("Tools");

		JButton bSort = new JButton("Sort");
		bSort.addActionListener(this);
		bFilter = new JButton("Filter");
		bFilter.addActionListener(this);
		bPlay = new JButton("Play");
		bPlay.addActionListener(this);

		toolbar.add(bPlay);
		toolbar.add(bSort);
		toolbar.add(bFilter);

		window.add(toolbar, BorderLayout.PAGE_START);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mQuit) {
			System.exit(0);
		} else if (e.getSource() == mOpen) {
			selectFile();
		} else if (e.getSource() == mConfig) {

		} else if (e.getSource() == mCreateIndex) {
			setMenuBarEnabled(false);
			CreateLogIndex createIndex = new CreateLogIndex(mainPanel, this);
			createIndex.start(openHelper.getFile().getAbsolutePath());
		} else if (e.getSource() == mFilter || e.getSource() == bFilter) {
			if (filterDialog.performSelection(mainPanel)) {
				filterList();
			}
		} else if (e.getSource() == bPlay) {
			if (playback == null) {
				bPlay.setText("Stop");
				playback = new PlaybackThread(mainPanel, this, 20);
				playback.start();
				adjustGuiForPlayBack(true);
			} else {
				playbackIsFinished();
			}
		}
	}

	/**
	 * Call when the playback has finished or needs to stop.
	 */
	public void playbackIsFinished() {
		bPlay.setText("Play");
		playback.stopPlaying();
		playback = null;
		adjustGuiForPlayBack(false);
	}

	/**
	 * Filters data based on what was selected inside of the filter dialog.
	 */
	private void filterList() {
		switch (filterDialog.getFilterType()) {
			case NO_FILTERING:
				mainPanel.setObjectList(indexes, openHelper.getReader());
				break;

			case SOURCE:
				filterBySource(filterDialog.getSelectedSources());
				break;

			case DATA:
				filterByType(filterDialog.getSelectedTypes());
				break;
		}
	}

	/**
	 * Filters indexed data by source
	 *
	 * @param allowed List containing the types of data it is allowed to display.
	 */
	private void filterBySource(List<String> allowed) {
		List<LogFileObjectRef> list = new ArrayList<LogFileObjectRef>();

		for (LogFileObjectRef o : indexes) {
			if (o.getSource() == null)
				continue;

			for (String s : allowed) {

				if (o.getSource().compareTo(s) == 0) {
					list.add(o);
					break;
				}
			}
		}

		mainPanel.setObjectList(list, openHelper.getReader());
	}

	/**
	 * Filters indexed data by data type
	 *
	 * @param allowed List containing the types of data it is allowed to display.
	 */
	private void filterByType(List<Class<?>> allowed) {
		List<LogFileObjectRef> list = new ArrayList<LogFileObjectRef>();

		for (LogFileObjectRef o : indexes) {

			for (Class<?> s : allowed) {
				if (o.getDataType() == s) {
					list.add(o);
					break;
				}
			}
		}

		mainPanel.setObjectList(list, openHelper.getReader());
	}

	@Override
	public void loadFileComplete() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!openHelper.isSuccessful()) {
					System.out.println("Load file failed!");
					setMenuBarEnabled(true);
					return;
				}

				indexes = openHelper.getReader().getReferences();

				File f = openHelper.getFile();
				config.directory = f.getParent();
				setMenuBarEnabled(true);

				filterDialog.setData(openHelper.getAllTypes(), openHelper.getAllSources());
				mainPanel.setObjectList(indexes, openHelper.getReader());
				String[] a = f.getName().split("[.]");
				if (a.length > 0)
					window.setTitle(a[0]);
				else
					window.setTitle(f.getName());
				setMenuBarEnabled(true);
			}
		});
	}

	public void finishedCreateIndex() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setMenuBarEnabled(true);
			}
		});
	}

	public static class Config {
		public String directory;
	}
}
