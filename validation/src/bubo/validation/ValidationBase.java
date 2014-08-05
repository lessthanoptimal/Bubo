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

package bubo.validation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Baseclass for all validation classes
 *
 * @author Peter Abeles
 */
public abstract class ValidationBase {

	protected String outputDirectory = "./";
	protected String algorithmName = "";
	protected String outputName;

	protected PrintStream out;

	public void setOutputName( String dataName ) throws FileNotFoundException {
		this.outputName = dataName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void performEvaluation() {
		try {
			out = new PrintStream(outputDirectory+algorithmName+"_"+outputName);
		} catch (FileNotFoundException e) {
			throw new RuntimeException();
		}

		try {
			_performEvaluation();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract void _performEvaluation() throws IOException;
}
