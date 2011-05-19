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
package org.textway.lapg.gen;

import org.textway.lapg.common.JavaArrayArchiver;
import org.textway.templates.eval.DefaultStaticMethods;

public class TemplateStaticMethods extends DefaultStaticMethods {

	public String shiftRight(String s, Integer padding) {
		return shiftRightWithChar(s, padding, '\t');
	}

	public String shiftRightWithSpaces(String s, Integer padding) {
		return shiftRightWithChar(s, padding, ' ');
	}

	private static String shiftRightWithChar(String s, Integer padding, char paddingChar) {
		String[] sspl = s.split("\\r?\\n");
		StringBuilder sb = new StringBuilder(s.length() + (padding + 1) * sspl.length);
		for (String q : sspl) {
			if(q.trim().length() > 0) {
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
		String[] sspl = text.split("\\r?\\n");
		String prefix = null;
		for(int i = 0; i < sspl.length; i++) {
			if(sspl[i].trim().length() == 0) {
				sspl[i] = "";
				continue;
			}
			int spaces = 0;
			while(spaces < sspl[i].length()) {
				char c = sspl[i].charAt(spaces);
				if(c == ' ' || c == '\t') {
					spaces++;
				} else {
					break;
				}
			}
			if(prefix == null) {
				prefix = sspl[i].substring(0, spaces);
			} else {
				int len = 0;
				while(len < prefix.length() && len < spaces && prefix.charAt(len) == sspl[i].charAt(len)) len++;
				if(len < prefix.length()) {
					prefix = prefix.substring(0, len);
				}
			}
			if(prefix.length() == 0) {
				return text;
			}
		}
		if(prefix == null) {
			return text;
		}

		int padding = prefix.length();
		StringBuilder sb = new StringBuilder(text.length());
		for (String q : sspl) {
			if(q.length() > 0) {
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
				if(e > 0) {
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

	public static String packIntInt(int[][] table, Integer leftpadding) {
		return JavaArrayArchiver.packIntInt(table, leftpadding);
	}

	public static String packInt(int[] table, Integer leftpadding) {
		return JavaArrayArchiver.packInt(table, leftpadding);
	}

	public static String packShort(short[] table, Integer leftpadding) {
		return JavaArrayArchiver.packShort(table, leftpadding);
	}
}
