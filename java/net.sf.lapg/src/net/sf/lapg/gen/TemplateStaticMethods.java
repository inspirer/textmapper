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
package net.sf.lapg.gen;

import net.sf.lapg.common.JavaArrayArchiver;
import net.sf.lapg.templates.api.impl.DefaultStaticMethods;

public class TemplateStaticMethods extends DefaultStaticMethods {

	public String shiftRight(String s, Integer padding) {
		String[] sspl = s.split("\\n");
		StringBuffer sb = new StringBuffer(s.length() + (padding + 1) * sspl.length);
		for (String q : sspl) {

			for (int i = 0; i < padding; i++) {
				sb.append('\t');
			}
			sb.append(q);
			sb.append('\n');
		}
		return sb.toString();
	}

	public String format(short[] table, Integer maxwidth, Integer leftpadding) {
		StringBuffer sb = new StringBuffer();
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
			sb.append(",");
		}
		return sb.toString();
	}

	public String format(int[] table, Integer maxwidth, Integer leftpadding) {
		StringBuffer sb = new StringBuffer();
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
			sb.append(",");
		}
		return sb.toString();
	}

	public static String format(int[][] table, Integer leftpadding, String startrow, String endrow) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < table.length; i++) {
			if (i > 0) {
				for (int e = 0; e < leftpadding; e++) {
					sb.append("\t");
				}
			}
			sb.append(startrow);
			sb.append(" ");
			int[] row = table[i];
			for (int element : row) {
				sb.append(element);
				sb.append(", ");
			}
			sb.append(endrow);
			sb.append(",\n");
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
