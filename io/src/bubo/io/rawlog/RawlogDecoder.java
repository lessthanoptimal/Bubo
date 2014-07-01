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

import bubo.io.LittleEndianIO;
import bubo.io.UtilReflections;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reads a rawlog (http://www.mrpt.org/) data stream and generates objects from the serialized data.   Note that
 * rawlog uses little endian format, so special functions are provided for reading in all primitive variables.
 *
 * @author Peter Abeles
 */
public class RawlogDecoder {

	// print out statements that are usefull for debugging
	public static final boolean VERBOSE = false;

	// class path for where Rawlog data classes are stored
	public static final String RAWLOG_OBJECT_PATH = "bubo.io.rawlog.data.";

	// reference to input stream that is being parsed
	private InputStream in;

	/**
	 * Creates a new rawlog decoder
	 *
	 * @param in Input data stream that contains binary rawlog objects.
	 * @throws IOException
	 */
	public RawlogDecoder(InputStream in) throws IOException {
		this.in = in;
	}

	/**
	 * Extracts the next object from the data stream.
	 *
	 * @return Next object from the data stream.  null if no more objects are available.
	 * @throws IOException
	 */
	public <T extends RawlogSerializable> T decode() throws IOException {
		//noinspection unchecked
		return (T) decodeObject();
	}

	public RawlogSerializable decodeObject()
			throws IOException {

		// read in the length of the serialized class
		int nameLength = LittleEndianIO.readByte(in);

		// lengths of some data structures are different prior to rawlog 0.5.5 file format
		boolean oldFileFormat = (nameLength & 0x80) == 0;

		if (oldFileFormat) {
			nameLength |= LittleEndianIO.readByte(in) << 8;
			nameLength |= LittleEndianIO.readByte(in) << 16;
			nameLength |= LittleEndianIO.readByte(in) << 24;
		} else {
			// remove MSB
			nameLength = nameLength & 0x7F;
		}

		String className = readClassName(nameLength);

//        System.out.println("className "+className);

		// the version of the class
		int version = LittleEndianIO.readByte(in);

		if (oldFileFormat) {
			version |= LittleEndianIO.readByte(in) << 8;
			version |= LittleEndianIO.readByte(in) << 16;
			version |= LittleEndianIO.readByte(in) << 24;
		}

		if (VERBOSE) {
			System.out.println(" decoded: class name = " + className + "  version " + version);
		}

		RawlogSerializable ret = decode(className, version);

		if (!oldFileFormat) {
			int a = LittleEndianIO.readByte(in);
			if (a != 0x88)
				throw new RuntimeException("Missing ending byte 88");
		}

		return ret;
	}

	public String readClassName(int nameLength) throws IOException {
		if (nameLength > 120)
			throw new RuntimeException("Name is more than 120 characters the stream is corrupt. length " + nameLength);

		// read in the class' name
		String className = "";

		for (int i = 0; i < nameLength; i++) {
			className += (char) LittleEndianIO.readByte(in);
		}
		return className;
	}

	private RawlogSerializable decode(String name, int version) throws IOException {
		Class targetClass;

		try {
			targetClass = Class.forName(RAWLOG_OBJECT_PATH + name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Can't find class " + name);
		}

		if (RawlogSerializableStandard.class.isAssignableFrom(targetClass))
			return standardDecoding(version, targetClass);
		else if (RawlogSerializableCustom.class.isAssignableFrom(targetClass))
			return customDecoding(version, targetClass);
		else
			throw new RuntimeException("Not a serializable class! " + targetClass.getName());

	}

	private RawlogSerializableStandard standardDecoding(int version, Class targetClass) throws IOException {
		RawlogSerializableStandard ret;

		try {
			ret = (RawlogSerializableStandard) targetClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		String[] rawlogVariables = ret.getVariableOrder(version);

		for (String varName : rawlogVariables) {
			if (VERBOSE)
				System.out.println("  decoding variables: " + varName);
			String methodName = "";
			try {
				Method mGet = UtilReflections.findGetter(targetClass, varName);
				Method mSet = UtilReflections.findSetter(targetClass, varName);

				if (mGet == null)
					throw new RuntimeException("A getter could not be found for " + varName);
				else if (mSet == null)
					throw new RuntimeException("A setter could not be found for " + varName);

				// get the variable type from the get return type
				Class varType = mGet.getReturnType();

				// pass in the parsed data to the class
				mSet.invoke(ret, parseData(varType));

			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to invoke " + methodName, e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Failed to invoke " + methodName, e);
			}
		}

		return ret;
	}

	private RawlogSerializableCustom customDecoding(int version, Class targetClass) throws IOException {
		RawlogSerializableCustom ret;

		try {
			ret = (RawlogSerializableCustom) targetClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		ret.customDecoding(version, this);

		return ret;
	}

	private Object parseData(Class<?> varType) throws IOException {
		if (varType == String.class) {
			return readString();
		} else if (varType == byte.class) {
			return (byte) LittleEndianIO.readByte(in);
		} else if (varType == int.class) {
			return LittleEndianIO.readInt(in);
		} else if (varType == long.class) {
			return LittleEndianIO.readLong(in);
		} else if (varType == float.class) {
			return LittleEndianIO.readFloat(in);
		} else if (varType == double.class) {
			return LittleEndianIO.readDouble(in);
		} else if (varType == boolean.class) {
			return LittleEndianIO.readBoolean(in);
		} else if (varType == byte[].class) {
			return readByteArray();
		} else if (varType == boolean[].class) {
			return readBooleanArray();
		} else if (varType == float[].class) {
			return readFloatArray();
		} else if (varType == double[].class) {
			return readDoubleArray();
		} else {
			// must be a more complex type
			RawlogSerializable found = decodeObject();

			if (found.getClass() != varType)
				throw new RuntimeException("Decoded unexpected type");

			return found;
		}
	}

	public byte[] readByteArray() throws IOException {

		int length = LittleEndianIO.readInt(in);

		return readByteArray(length);
	}

	public byte[] readByteArray(int length) throws IOException {
		byte[] ret = new byte[length];

		for (int i = 0; i < length; i++) {
			ret[i] = (byte) LittleEndianIO.readByte(in);
		}

		return ret;
	}

	public boolean[] readBooleanArray() throws IOException {

		int length = LittleEndianIO.readInt(in);

		return readBooleanArray(length);
	}

	public boolean[] readBooleanArray(int length) throws IOException {
		boolean[] ret = new boolean[length];

		for (int i = 0; i < length; i++) {
			ret[i] = LittleEndianIO.readBoolean(in);
		}

		return ret;
	}

	public float[] readFloatArray() throws IOException {

		int length = LittleEndianIO.readInt(in);

		return readFloatArray(length);
	}

	public float[] readFloatArray(int length) throws IOException {
		float[] ret = new float[length];

		byte[] buff = readBytes(length * 4);

		for (int i = 0; i < length; i++) {
			ret[i] = LittleEndianIO.readFloat(buff, i * 4);
		}

		return ret;
	}

	public double[] readDoubleArray() throws IOException {

		int length = LittleEndianIO.readInt(in);

		return readDoubleArray(length);
	}

	public double[] readDoubleArray(int length) throws IOException {
		double[] ret = new double[length];

		byte[] buff = readBytes(length * 8);

		for (int i = 0; i < length; i++) {
			ret[i] = LittleEndianIO.readDouble(buff, i * 8);
		}

		return ret;
	}

	public String readString() throws IOException {
		int length = LittleEndianIO.readInt(in);

		StringBuffer string = new StringBuffer(length);

		byte[] buff = readBytes(length);

		for (int i = 0; i < length; i++) {
			string.append((char) buff[i]);
		}

		return string.toString();
	}

	private byte[] readBytes(int length) throws IOException {
		byte[] buff = new byte[length];

		int ret = in.read(buff);
		while (ret < length) {
			int a = in.read(buff, ret, length - ret);
			if (a > 0)
				ret += a;
			else
				throw new RuntimeException("Expected EOF");
		}
		return buff;
	}

	public InputStream getInput() {
		return in;
	}
}
