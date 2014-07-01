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

package bubo.io;

import javax.swing.*;
import java.io.*;
import java.util.zip.GZIPInputStream;


/**
 * @author Peter Abeles
 */
public class UtilCompression {

	public static boolean isGzipFile(File f) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			GZIPInputStream gs = new GZIPInputStream(fis);
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;

	}

	/**
	 * Decompresses a GZIP file and saves the output in another file.
	 *
	 * @param inputName
	 * @param outputName
	 * @return true if it worked and false if it didn't work.
	 */
	public static boolean gzipDecompressFile(String inputName,
											 String outputName,
											 int bufferSize,
											 ProgressMonitor monitor) {
		byte buffer[] = new byte[bufferSize];

		int max = monitor.getMaximum();

		boolean ret = false;

		try {
			File f = new File(inputName);
			FileInputStream fis = new FileInputStream(f);
			GZIPInputStream gs = new GZIPInputStream(fis);

			long size = f.length();
			long total = 0;

			FileOutputStream fos = new FileOutputStream(outputName);

			int read;
			while ((read = gs.read(buffer)) > 0 && !monitor.isCanceled()) {
				total += read;
				fos.write(buffer, 0, read);
				monitor.setProgress((int) ((total * max) / size));
			}

//            System.out.println("Done decompressing file");
			fos.close();
			fis.close();

			ret = !monitor.isCanceled();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		monitor.close();

		return ret;
	}
}
