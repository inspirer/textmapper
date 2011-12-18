/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.parser;

import org.textway.lapg.common.FormatUtil;

import java.util.HashSet;
import java.util.Set;

public class UniqueNameHelper {

	private final Set<String> usedIdentifiers = new HashSet<String>();

	void markUsed(String name) {
		usedIdentifiers.add(name);
	}

	String generateId(String name, int i) {
		if (usedIdentifiers.contains(name)) {
			return name;
		}
		name = FormatUtil.toIdentifier(name, i);
		String result = name;
		int i1 = 2;
		while (usedIdentifiers.contains(result)) {
			result = name + i1++;
		}
		usedIdentifiers.add(result);
		return result;
	}

	static boolean safeEquals(Object o1, Object o2) {
		return o1 == null || o2 == null ? o1 == o2 : o1.equals(o2);
	}
}
