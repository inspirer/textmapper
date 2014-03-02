/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
import org.textmapper.lapg.common.CharacterSetImpl.Builder;

import java.util.*;

/**
 * Gryaznov Evgeny, 4/22/12
 */
public class UnicodeData {

	private static UnicodeData INSTANCE = new UnicodeData();

	public static UnicodeData getInstance() {
		return INSTANCE;
	}

	private Map<String, String> rawData;
	private Map<String, String> aliases;
	private Map<String, Collection<String>> composites;
	private Map<String, CharacterSet> set = new HashMap<String, CharacterSet>();

	private UnicodeData() {
	}

	public String getVersion() {
		return UnicodeDataTables.VERSION;
	}

	public Collection<String> getAvailableProperties() {
		initRawData();
		initAliases();
		initComposites();
		List<String> list = new ArrayList<String>(rawData.size() + aliases.size() + composites.size());
		list.addAll(rawData.keySet());
		list.addAll(aliases.keySet());
		list.addAll(composites.keySet());
		return list;
	}

	public CharacterSet getCharacterSet(String propertyName) {
		initRawData();
		String canonicalName = resolveAlias(toCanonicalName(propertyName));
		CharacterSet result = set.get(canonicalName);
		if (result != null) {
			return result;
		}
		if (!(rawData.containsKey(canonicalName))) {
			initComposites();
			if (composites.containsKey(canonicalName)) {
				Builder b = new Builder();
				for (String part : composites.get(canonicalName)) {
					b.addSet(getCharacterSet(part));
				}
				result = b.create();
				set.put(canonicalName, result);
			}
			return result;
		}

		String data = rawData.get(canonicalName);
		result = decode(data);
		set.put(canonicalName, result);
		return result;
	}

	private String resolveAlias(String propertyName) {
		initAliases();
		String val = aliases.get(propertyName);
		return val != null ? val : propertyName;
	}

	private void initAliases() {
		if (aliases != null) {
			return;
		}
		aliases = new HashMap<String, String>();
		for (int i = 0; i < UnicodeDataTables.ALIASES.length; ) {
			aliases.put(UnicodeDataTables.ALIASES[i++], UnicodeDataTables.ALIASES[i++]);
		}
	}

	private void initComposites() {
		if (composites != null) {
			return;
		}
		composites = new HashMap<String, Collection<String>>();
		for (int i = 0; i < UnicodeDataTables.COMPOSITES.length; i++) {
			String name = UnicodeDataTables.COMPOSITES[i++];
			Collection<String> properties = new ArrayList<String>();
			while (UnicodeDataTables.COMPOSITES[i] != null) {
				properties.add(UnicodeDataTables.COMPOSITES[i++]);
			}
			composites.put(name, properties);
		}
	}

	private void initRawData() {
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
