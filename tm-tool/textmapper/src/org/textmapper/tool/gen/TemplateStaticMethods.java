/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.tool.gen;

import org.textmapper.lapg.common.FormatUtil;
import org.textmapper.lapg.util.ArrayIterable;
import org.textmapper.templates.eval.DefaultStaticMethods;
import org.textmapper.tool.common.JavaArrayEncoder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateStaticMethods extends DefaultStaticMethods {

	public String shiftRight(String s, Integer padding) {
		return shiftRightWithChar(s, padding, '\t');
	}

	public String shiftRightWithSpaces(String s, Integer padding) {
		return shiftRightWithChar(s, padding, ' ');
	}

	public String spaces(Integer num) {
		char[] c = new char[num];
		Arrays.fill(c, ' ');
		return new String(c);
	}

	private static String shiftRightWithChar(String s, Integer padding, char paddingChar) {
		if (s.trim().isEmpty()) return s;

		String[] sspl = s.split("\\r?\\n");
		StringBuilder sb = new StringBuilder(s.length() + (padding + 1) * sspl.length);
		for (String q : sspl) {
			if (q.trim().length() > 0) {
				for (int i = 0; i < padding; i++) {
					sb.append(paddingChar);
				}
				sb.append(q);
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public String shiftLeft(String text) {
		if (text.trim().isEmpty()) return text;

		String[] sspl = text.split("\\r?\\n");
		String prefix = null;
		for (int i = 0; i < sspl.length; i++) {
			if (sspl[i].trim().length() == 0) {
				sspl[i] = "";
				continue;
			}
			int spaces = 0;
			while (spaces < sspl[i].length()) {
				char c = sspl[i].charAt(spaces);
				if (c == ' ' || c == '\t') {
					spaces++;
				} else {
					break;
				}
			}
			if (prefix == null) {
				prefix = sspl[i].substring(0, spaces);
			} else {
				int len = 0;
				while (len < prefix.length() && len < spaces
						&& prefix.charAt(len) == sspl[i].charAt(len)) {
					len++;
				}
				if (len < prefix.length()) {
					prefix = prefix.substring(0, len);
				}
			}
			if (prefix.length() == 0) {
				return text;
			}
		}
		if (prefix == null) {
			return text;
		}

		int padding = prefix.length();
		StringBuilder sb = new StringBuilder(text.length());
		for (String q : sspl) {
			if (q.length() > 0) {
				sb.append(q.substring(padding));
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public String format(int[] table, Integer maxwidth, Integer leftpadding) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < table.length; i++) {
			if (i > 0) {
				if ((i % maxwidth) == 0) {
					sb.append("\n");
					for (int e = 0; e < leftpadding; e++) {
						sb.append("\t");
					}
				} else {
					sb.append(" ");
				}
			}
			sb.append(table[i]);
			if (i + 1 < table.length) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public String formatEx(int[] table, String padding, Integer maxwidth) {
		if (table.length == 0) {
			return "";
		}
		int col = maxwidth;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < table.length; i++) {
			int size = table[i] <= 0 ? 1 : 0;
			for (int val = Math.abs(table[i]); val > 0; val /= 10) {
				size++;
			}
			col += size + 2 /* space and , */;
			if (col < maxwidth) {
				sb.append(' ');
			} else {
				sb.append('\n');
				sb.append(padding);
				col = padding.length() + size + 1;
			}
			sb.append(table[i]);
			sb.append(',');
		}
		sb.append('\n');
		return sb.toString().substring(1);
	}

	public static String format(int[][] table, Integer leftpadding, String
			startrow, String endrow) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < table.length; i++) {
			if (i > 0) {
				for (int e = 0; e < leftpadding; e++) {
					sb.append("\t");
				}
			}
			sb.append(startrow);
			sb.append(" ");
			int[] row = table[i];
			for (int e = 0; e < row.length; e++) {
				if (e > 0) {
					sb.append(", ");
				}
				sb.append(row[e]);
			}
			sb.append(endrow);
			if (i + 1 < table.length) {
				sb.append(",\n");
			}
		}
		return sb.toString();
	}

	public static List<List<String>> packShortCountValue(int[] arr, Boolean positiveOnly) {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		int count = 0;
		int value = 0;
		for (int i = 0; i < arr.length; i++) {
			if (value == arr[i] && count < 0xffff) {
				count++;
			} else {
				if (count > 0) {
					enc.appendChar(count);
					if (positiveOnly) {
						enc.appendChar(value);
					} else {
						enc.appendShort(value);
					}
				}
				count = 1;
				value = arr[i];

				if (!positiveOnly && (value < Short.MIN_VALUE || value > Short.MAX_VALUE)
						|| positiveOnly && (value < 0 || value > Character.MAX_VALUE)) {
					throw new IllegalArgumentException("cannot convert int[] into " +
							(positiveOnly ? "char" : "short") + "[], contains `" + value + "'");
				}
			}
		}
		if (count > 0) {
			enc.appendChar(count);
			if (positiveOnly) {
				enc.appendChar(value);
			} else {
				enc.appendShort(value);
			}
		}
		return enc.getResult();
	}

	public static int[] head(int[] arr, int headLen) {
		return Arrays.copyOf(arr, headLen);
	}

	public static int[] tail(int[] arr, int headLen) {
		return Arrays.copyOfRange(arr, headLen, arr.length);
	}

	public static class MapRange {
		public int lo, hi, defaultVal;
		public int[] val;

		public int getLo() {
			return lo;
		}

		public int getHi() {
			return hi;
		}

		public int getDefaultVal() {
			return defaultVal;
		}

		public int[] getVal() {
			return val;
		}
	}

	public static List<MapRange> packAsMapRanges(int[] arr, Integer rangeOffset) {
		List<MapRange> result = new ArrayList<>();
		int i = 0;
		while (i < arr.length) {
			while (i < arr.length && arr[i] == 1) i++;
			if (i == arr.length) break;

			MapRange r = new MapRange();
			r.lo = i;
			r.hi = i;
			int lastChar = -1;
			int numOthers = 0;
			int numOnes = 0;

			for (; i < arr.length; i++) {
				if (arr[i] == 1) {
					numOnes++;
				} else if (arr[i] == lastChar && numOnes == 0) {
					numOthers++;
				} else if (numOnes + numOthers > 8) {
					break;
				} else {
					numOnes = 0;
					numOthers = 1;
					lastChar = arr[i];
					r.hi = i;
				}
			}
			r.defaultVal = lastChar;
			if (r.lo < r.hi) {
				r.val = Arrays.copyOfRange(arr, r.lo, r.hi);
			}
			r.hi = i - numOnes + rangeOffset;
			r.lo += rangeOffset;
			result.add(r);
		}
		return result;
	}

	public static List<List<String>> packInt(int[] arr) {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		for (int i : arr) {
			enc.appendInt(i);
		}
		return enc.getResult();
	}

	public String escape(String s) {
		return FormatUtil.escape(s);
	}

	public String toUpperWithUnderscores(String s) {
		return FormatUtil.toUpperWithUnderscores(s);
	}

	public Iterable reverse(Object[] array) {
		return new ArrayIterable(array, true);
	}

	private static Pattern STMT = Pattern.compile("\\{\\{(([^}]|\\}[^}])*)\\}\\}");

	public String extractStatements(String code) {
		StringBuilder prefix = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		Set<String> seen = new HashSet<>();
		int lastStart = 0;
		Matcher m = STMT.matcher(code);
		while (m.find()) {
			sb.append(code, lastStart, m.start());
			lastStart = m.end();
			String stmt = m.group(1).trim();
			if (seen.add(stmt)) {
				prefix.append(stmt).append("\n");
			}
		}
		sb.append(code.substring(lastStart));
		prefix.append(sb);
		return prefix.toString();
	}

	public Integer rangeSwitchSize(Integer len) {
		int range = 8;
		while (range < len) {
			range *= 2;
		}
		return range;
	}

	public String hashHex(String s) {
		int i = stringHash(s);
		return Integer.toHexString(i);
	}

	public int stringHash(String s) {
		int hash = 0;
		for (char c : s.toCharArray()) {
			hash = hash * 31 + c;
		}
		return hash;
	}

	public int rangedHash(String s, int range) {
		int i = stringHash(s) % range;
		if (i < 0) i += range;
		return i;
	}

	public int bitsForElement(int[] arr) {
		int result = 8;
		for (int i : arr) {
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) continue;
			if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) return 32;
			result = 16;
		}
		return result;
	}
}
