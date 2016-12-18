/**
 * Copyright 2002-2016 Evgeny Gryaznov
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

import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.common.FormatUtil;

import java.util.*;
import java.util.Map.Entry;

class UniqueNameHelper {

	private final Map<String, Integer> desiredNameUsage = new HashMap<>();
	private final Map<Symbol, String> desiredNames = new LinkedHashMap<>();
	private final Set<String> usedIdentifiers = new HashSet<>();
	private final boolean tokensAllCaps;

	UniqueNameHelper(boolean tokensAllCaps) {
		this.tokensAllCaps = tokensAllCaps;
	}

	void add(Symbol s) {
		String desiredName = desiredName(s);
		desiredNames.put(s, desiredName);

		Integer count = desiredNameUsage.get(desiredName);
		desiredNameUsage.put(desiredName, count == null ? 1 : count + 1);
	}

	void apply() {
		for (Entry<Symbol, String> entry : desiredNames.entrySet()) {
			Integer count = desiredNameUsage.get(entry.getValue());
			if (count == 1) {
				TMDataUtil.putId(entry.getKey(), entry.getValue());
				usedIdentifiers.add(entry.getValue());
			}
		}
		for (Entry<Symbol, String> entry : desiredNames.entrySet()) {
			Integer count = desiredNameUsage.get(entry.getValue());
			if (count != 1) {
				TMDataUtil.putId(entry.getKey(), generateSymbolId(entry.getValue()));
			}
		}
	}

	private String generateSymbolId(String desiredName) {
		String result = desiredName;
		int i1 = 1;
		while (usedIdentifiers.contains(result)) {
			result = desiredName + i1++;
		}
		usedIdentifiers.add(result);
		return result;
	}

	private String desiredName(Symbol s) {
		String name = s.getNameText();
		String desiredName;
		if (FormatUtil.isIdentifier(name)) {
			desiredName = name;
		} else if (s.isTerm() && ((Terminal)s).isConstant()) {
			desiredName = toIdentifier(((Terminal) s).getConstantValue(), s.getIndex());
			if (desiredName.length() == 1 && FormatUtil.isIdentifier(desiredName)) {
				desiredName = "char_" + desiredName;
			}
		} else {
			desiredName = toIdentifier(name, s.getIndex());
		}
		if (this.tokensAllCaps && s.isTerm()) {
			desiredName = desiredName.toUpperCase();
		}
		return desiredName;
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
