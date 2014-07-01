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

import java.util.List;


/**
 * @author Peter Abeles
 */
public class UtilStrings {

	/**
	 * <p>
	 * Checks to see if the list already contains the string.  if not it is added.  It
	 * either returns the equivalent string in the list or the passed in string if there
	 * is no equivalent already in the list.
	 * </p>
	 *
	 * @param list List of strings.
	 * @param text string which will be added to the if an equivalent string is not already in it
	 * @return The string which is in the list and is equivalent to 'text'.
	 */
	public static String checkAddString(List<String> list, String text) {
		if (text == null)
			return null;
		for (String s : list) {
			if (s.compareTo(text) == 0)
				return s;
		}

		list.add(text);
		return text;
	}
}
