/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textmapper.lapg.unicode;

import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.common.CharacterSetImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Gryaznov Evgeny, 4/22/12
 */
public class UnicodeData {

	private static UnicodeData INSTANCE = new UnicodeData();

	public static UnicodeData getInstance() {
		return INSTANCE;
	}

	private Map<String, String> rawData;
	private Map<String, CharacterSet> set = new HashMap<String, CharacterSet>();

	private UnicodeData() {
	}

	public String getVersion() {
		return UnicodeDataTables.VERSION;
	}

	public Collection<String> getAvailableProperties() {
		if (rawData == null) {
			initData();
		}
		return rawData.keySet();
	}

	public CharacterSet getCharacterSet(String propertyName) {
		if (rawData == null) {
			initData();
		}
		String canonicalName = toCanonicalName(propertyName);
		CharacterSet result = set.get(canonicalName);
		if (result != null || !(rawData.containsKey(canonicalName))) {
			return result;
		}

		String data = rawData.get(canonicalName);
		result = decode(data);
		set.put(canonicalName, result);
		return result;
	}

	private void initData() {
		if (rawData != null) {
			return;
		}
		rawData = new HashMap<String, String>();
		String[] properties = UnicodeDataTables.PROPERTIES;
		for (int i = 0; i < properties.length; ) {
			rawData.put(properties[i++], properties[i++]);
		}
	}

	public static String toCanonicalName(String propertyName) {
		// see http://www.unicode.org/reports/tr44/#UAX44-LM3
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < propertyName.length(); i++) {
			char c = propertyName.charAt(i);
			if (c == '-' || c == '_' || Character.isWhitespace(c)) {
				continue;
			}
			sb.append(Character.toLowerCase(c));
		}
		if (sb.indexOf("is") == 0) {
			sb.delete(0, 2);
		}
		return sb.toString();
	}

	private static CharacterSet decode(String data) {
		int len = data.charAt(0);
		boolean containsSurrogate = len == 0;
		int index = 1;
		if (containsSurrogate) {
			len = data.charAt(index++);
		}
		int[] set = new int[len];
		for (int i = 0; i < len; i++) {
			int val;
			if (containsSurrogate) {
				val = data.charAt(index++) + (data.charAt(index++) << 16);
			} else {
				val = data.codePointAt(index++);
				if (val > 0xffff) {
					index++;
				}
			}
			set[i] = val;
		}
		return new CharacterSetImpl(set);
	}
}
