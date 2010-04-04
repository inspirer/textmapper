/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package net.sf.lapg.templates.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default class to use as helper in templates.
 */
public class DefaultStaticMethods {

	public String print(ArrayList<?> list, String separator, Integer maxwidth) {
		return print(list.toArray(), separator, maxwidth);
	}

	public String print(Object[] list, String separator, Integer maxwidth) {
		StringBuffer sb = new StringBuffer();
		int i = 0, lineStart = 0;
		for (Object a : list) {
			if (i > 0) {
				sb.append(separator);
			}
			String str = a.toString();
			if (sb.length() + str.length() - lineStart >= maxwidth) {
				sb.append('\n');
				lineStart = sb.length();
			}
			sb.append(str);
			i++;
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public String print(HashMap<?, ?> map) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		sb.append("[");
		ArrayList keys = new ArrayList(map.keySet());
		if(keys.size() > 0 && keys.get(0) instanceof Comparable<?>) {
			Collections.sort((List<Comparable>)keys);
		}
		for (Object key : keys) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(key);
			sb.append(" -> ");
			sb.append(map.get(key));
			i++;
		}
		sb.append("]");
		return sb.toString();
	}

	public String toFirstUpper(String s) {
		if(s.length() > 0) {
			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		}
		return s;
	}

	public String toCamelCase(String s, Boolean firstUpper) {
		char[] string = s.toCharArray();
		int len = 0;
		boolean nextUpper = firstUpper;
		for(int i = 0; i < string.length; i++) {
			char c = string[i];
			if(c == '_') {
				nextUpper = true;
			} else if(nextUpper){
				string[len] = len > 0 || firstUpper ? Character.toUpperCase(c) : c;
				len++;
				nextUpper = false;
			} else {
				string[len++] = c;
			}
		}
		return new String(string, 0, len);
	}

	private final Set<String> usedIdentifiers = new HashSet<String>();

	public String uniqueId(String s) {
		return uniqueId(s, null);
	}

	public String uniqueId(String s, String context) {
		String result = s;
		int i = 2;
		while(usedIdentifiers.contains(context != null ? context + "#" + result : result)) {
			result = s + i++;
		}
		usedIdentifiers.add(context != null ? context + "#" + result : result);
		return result;
	}
}
