/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.lapg.common;

public class FormatUtil {

	public static void appendEscaped(StringBuilder sb, char c) {
		String sym = Integer.toString(c, 16);
		boolean isShort = sym.length() <= 2;
		sb.append(isShort ? "\\x" : "\\u");
		int len = isShort ? 2 : 4;
		if (sym.length() < len) {
			sb.append("0000".substring(sym.length() + (4 - len)));
		}
		sb.append(sym);
	}

	public static String asHex(int i, int width) {
		String s = Integer.toHexString(i);
		if (s.length() >= width) {
			return s;
		}
		StringBuilder sb = new StringBuilder();
		for (int chars = width - s.length(); chars > 0; chars--) {
			sb.append('0');
		}
		sb.append(s);
		return sb.toString();
	}

	public static String asDecimal(int i, int width, char padding) {
		String s = Integer.toString(i);
		if (s.length() >= width) {
			return s;
		}
		StringBuilder sb = new StringBuilder();
		for (int chars = width - s.length(); chars > 0; chars--) {
			sb.append(padding);
		}
		sb.append(s);
		return sb.toString();
	}

	public static boolean isIdentifier(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		char[] c = s.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (!(
					c[i] >= 'a' && c[i] <= 'z' ||
							c[i] >= 'A' && c[i] <= 'Z' ||
							c[i] == '_' ||
							i > 0 && c[i] >= '0' && c[i] <= '9')) {
				return false;
			}
		}
		return true;
	}

	public static String toCamelCase(String s, Boolean firstUpper) {
		char[] string = s.toCharArray();
		int len = 0;
		boolean nextUpper = firstUpper;
		for (int i = 0; i < string.length; i++) {
			char c = string[i];
			if (c == '_') {
				nextUpper = true;
			} else if (nextUpper) {
				string[len] = len > 0 || firstUpper ? Character.toUpperCase(c) : c;
				len++;
				nextUpper = false;
			} else {
				string[len++] = c;
			}
		}
		return new String(string, 0, len);
	}

	public static String toUpperWithUnderscores(String s) {
		StringBuilder sb = new StringBuilder();
		boolean newWord = true;
		boolean lastIsLower = false;
		for (char c : s.toCharArray()) {
			if (c == '_') {
				newWord = true;
				lastIsLower = false;
			} else {
				if (newWord || lastIsLower && Character.isUpperCase(c)) {
					final int len = sb.length();
					if (len > 0 && sb.charAt(len - 1) != '_') {
						sb.append('_');
					}
					newWord = false;
				}
				sb.append(Character.toUpperCase(c));
				lastIsLower = Character.isLowerCase(c);
			}
		}
		return sb.toString();
	}
}
