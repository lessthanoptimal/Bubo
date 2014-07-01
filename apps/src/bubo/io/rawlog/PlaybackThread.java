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

import bubo.io.rawlog.gui.DataLogIndexSplitPane;

import javax.swing.*;

/**
 * A thread that will play through a log file until it hits the end or the user tells it to stop
 *
 * @author Peter Abeles
 */
public class PlaybackThread extends Thread {

	// the main program which called this
	RawlogViewer owner;

	// what's displaying the data
	DataLogIndexSplitPane view;

	// how long it will wait in milliseconds before moving onto the next item
	long waitTime;

	// is it playing through the log
	volatile boolean play;

	// used to tell if the thread has run or not
	volatile int counter;

	/**
	 * Creates a new playback thread.
	 *
	 * @param view     What's displaying the data.
	 * @param waitTime How long it waits in milliseconds between data frames.
	 */
	public PlaybackThread(DataLogIndexSplitPane view, RawlogViewer owner, long waitTime) {
		this.view = view;
		this.owner = owner;
		this.waitTime = waitTime;
	}

	public boolean isPlay() {
		return play;
	}

	/**
	 * Tell the thread to stop playing through the data
	 */
	public void stopPlaying() {
		play = false;
		owner = null;
	}

	@Override
	public void run() {
		play = true;

		counter = 0;
		while (play) {
			// need to jump through some hoops or else swing will throw random exceptions
			// and get all messed up
			int before = counter;
			Thread r = new Thread() {
				public void run() {
					if (!view.selectNextItem()) {
						play = false;
					}
					counter++;
				}
			};
			SwingUtilities.invokeLater(r);

			// wait until the elapsed time has passed and the thread has finished running
			synchronized (this) {
				try {
					wait(waitTime);
					while (before == counter && play)
						wait(2);
				} catch (InterruptedException e) {
				}
			}
		}

		play = false;
		if (owner != null)
			owner.playbackIsFinished();
	}
}
