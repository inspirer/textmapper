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
package org.textway.lapg.gen;

import org.textway.lapg.common.FormatUtil;
import org.textway.lapg.common.JavaArrayEncoder;
import org.textway.templates.eval.DefaultStaticMethods;

import java.util.List;

public class TemplateStaticMethods extends DefaultStaticMethods {

	public String shiftRight(String s, Integer padding) {
		return shiftRightWithChar(s, padding, '\t');
	}

	public String shiftRightWithSpaces(String s, Integer padding) {
		return shiftRightWithChar(s, padding, ' ');
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
				while (len < prefix.length() && len < spaces && prefix.charAt(len) == sspl[i].charAt(len)) len++;
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

	public String format(short[] table, Integer maxwidth, Integer leftpadding) {
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

	public static String format(int[][] table, Integer leftpadding, String startrow, String endrow) {
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

	public static List<List<String>> packValueCount(int[] arr, Boolean positiveOnly) {
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

	public static List<List<String>> packShort(short[] arr) {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		for (short s : arr) {
			enc.appendChar((char) s);
		}
		return enc.getResult();
	}

	public static List<List<String>> packShort(int[] arr) {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		for (int i : arr) {
			if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
				throw new IllegalArgumentException("cannot convert int[] into short[], contains `" + i + "'");
			}
			enc.appendShort(i);
		}
		return enc.getResult();
	}

	public static List<List<String>> packInt(int[] arr) {
		JavaArrayEncoder enc = new JavaArrayEncoder(80);
		for (int i : arr) {
			enc.appendInt(i);
		}
		return enc.getResult();
	}

	public String escape(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			switch (c) {
				case '"':
				case '\'':
				case '\\':
					sb.append('\\');
					sb.append(c);
					continue;
				case '\f':
					sb.append("\\f");
					continue;
				case '\n':
					sb.append("\\n");
					continue;
				case '\r':
					sb.append("\\r");
					continue;
				case '\t':
					sb.append("\\t");
					continue;
			}
			if (c >= 0x20 && c < 0x80) {
				sb.append(c);
				continue;
			}
			FormatUtil.appendEscaped(sb, c);
		}
		return sb.toString();
	}
}
