/**
 * Copyright 2002-2015 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textmapper.tool.compiler;

import org.textmapper.lapg.common.FormatUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class UniqueNameHelper {

	private final Set<String> usedIdentifiers = new HashSet<String>();

	void markUsed(String name) {
		usedIdentifiers.add(name);
	}

	String generateSymbolId(String symbolName, int uniqueID) {
		if (usedIdentifiers.contains(symbolName)) {
			return symbolName;
		}
		symbolName = toIdentifier(symbolName, uniqueID);
		String result = symbolName;
		int i1 = 2;
		while (usedIdentifiers.contains(result)) {
			result = symbolName + i1++;
		}
		usedIdentifiers.add(result);
		return result;
	}

	private static String toIdentifier(String original, int uniqueID) {
		String s = original;

		// handle symbol names
		if (s.startsWith("\'") && s.endsWith("\'") && s.length() > 2) {
			s = s.substring(1, s.length() - 1);
			if (s.length() == 1 && FormatUtil.isIdentifier(s)) {
				s = "char_" + s;
			}
		} else if (s.equals("{}")) {
			s = "_sym" + uniqueID;
		}

		if (FormatUtil.isIdentifier(s)) {
			return s;
		}

		// convert
		return FormatUtil.toIdentifier(s);
	}
}
