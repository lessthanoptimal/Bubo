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

package bubo.io.text;

import bubo.io.UtilReflections;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Saves an object in the CSV format.
 *
 * @author Peter Abeles
 */
public class WriteCsvObject {

	private BufferedWriter out;

	private String variableNames[];

	// Getters used to access variable values
	private Method[] getters;

	// the type of variable
	private Class<?>[] variableTypes;

	// is the variable an array or not
	private boolean[] arrayVariable;

	// what's used to separate words
	private String space = " ";

	// the comment character
	private char commentChar = '#';

	// if an object being written is null this is written instead.  if null then an exception is thrown
	private String nullString = null;

	public WriteCsvObject(OutputStream out, Class<?> objectType, String... variables) {
		this.out = new BufferedWriter(new OutputStreamWriter(out));

		this.variableNames = variables;

		getters = new Method[variables.length];
		variableTypes = new Class<?>[variables.length];
		arrayVariable = new boolean[variables.length];

		for (int i = 0; i < variables.length; i++) {
			String v = variables[i];

			getters[i] = UtilReflections.findGetter(objectType, v);

			if (getters[i] == null) {
				throw new RuntimeException("Getter not found! " + v);
			}

			// save the method and extract its input type
			variableTypes[i] = getters[i].getReturnType();

			arrayVariable[i] = ReadCsvObject.checkValidType(variableTypes[i], getters[i].getName());
		}
	}

	/**
	 * Writes the comment to the CSV file.  This function can correctly handle new lines inside
	 * of the comment.
	 *
	 * @param comment String which is to be written to the out put as a comment.
	 * @throws IOException
	 */
	public void writeComment(String comment) throws IOException {
		int index = 0;
		while (index < comment.length()) {
			out.write(commentChar);
			char c;
			do {
				c = comment.charAt(index++);
				out.write(c);
			} while (c != '\n' && index < comment.length());

			if (c != '\n')
				out.write('\n');
		}
		out.flush();
	}

	/**
	 * Writes the object to the OutputStream in a CSV format.
	 *
	 * @param o The object to be written.
	 * @throws IOException
	 */
	public void writeObject(Object o) throws IOException {
		for (int i = 0; i < variableNames.length; i++) {
			try {
				if (arrayVariable[i]) {
					writeArray(o, i);
				} else {
					writePrimitive(o, i);
				}
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		out.write('\n');

		out.flush();
	}

	private void writePrimitive(Object o, int index) throws InvocationTargetException, IllegalAccessException, IOException {
		Object p = getters[index].invoke(o);

		if (index > 0)
			out.write(space);
		if (p == null) {
			if (nullString == null) {
				throw new RuntimeException("Object is null '" + variableNames[index] + "'.  To force an output set the nullString");
			} else {
				out.write(nullString);
			}
		} else {
			String output = p.toString();
			if (0 == output.length()) {
				throw new RuntimeException("Attempting to write a zero length string!");
			}
			out.write(p.toString());
		}
	}

	private void writeArray(Object o, int index) throws InvocationTargetException, IllegalAccessException, IOException {
		Object p = getters[index].invoke(o);

		if (p == null) {
			if (nullString == null)
				throw new RuntimeException("Array has null value. " + variableNames[index] + ".  To force an output set the nullString");
			else
				out.write(nullString);
		} else {
			int length = Array.getLength(p);

			for (int i = 0; i < length; i++) {
				if (!(index == 0 && i == 0))
					out.write(space);
				out.write(Array.get(p, i).toString());
			}
		}
	}


	/**
	 * Closes the stream to writing.
	 */
	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public char getCommentChar() {
		return commentChar;
	}

	public void setCommentChar(char commentChar) {
		this.commentChar = commentChar;
	}

	public String getNullString() {
		return nullString;
	}

	public void setNullString(String nullString) {
		this.nullString = nullString;
	}
}
