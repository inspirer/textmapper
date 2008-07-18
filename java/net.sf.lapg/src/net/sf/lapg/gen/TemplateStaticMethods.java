/*************************************************************
 * Copyright (c) 2002-2008 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 *************************************************************/
package net.sf.lapg.gen;

import net.sf.lapg.common.FormatUtil;
import net.sf.lapg.lex.JavaArrayArchiver;
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
			for (int e = 0; e < row.length; e++) {
				sb.append(row[e]);
				sb.append(", ");
			}
			sb.append(endrow);
			sb.append(",\n");
		}
		return sb.toString();
	}

	public static String formatForJava(int[][] table, Integer leftpadding) {
		return JavaArrayArchiver.emit_table_as_string(table, leftpadding);
	}

	public String toIdentifier(String s, Integer number) {

		if (s.startsWith("\'") && s.endsWith("\'")) {
			StringBuffer res = new StringBuffer();
			String inner = s.substring(1, s.length() - 1);
			for (int i = 0; i < inner.length(); i++) {
				int c = inner.charAt(i);
				if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_') {
					res.append(c);
				} else {
					String name;
					switch (c) {
					case '{':
						name = "LBRACE";
						break;
					case '}':
						name = "RBRACE";
						break;
					case '[':
						name = "LBRACKET";
						break;
					case ']':
						name = "RBRACKET";
						break;
					case '(':
						name = "LROUNDBRACKET";
						break;
					case ')':
						name = "RROUNDBRACKET";
						break;
					case '.':
						name = "DOT";
						break;
					case ',':
						name = "COMMA";
						break;
					case ':':
						name = "COLON";
						break;
					case ';':
						name = "SEMICOLON";
						break;
					case '+':
						name = "PLUS";
						break;
					case '-':
						name = "MINUS";
						break;
					case '*':
						name = "MULT";
						break;
					case '/':
						name = "DIV";
						break;
					case '%':
						name = "PERC";
						break;
					case '&':
						name = "AMP";
						break;
					case '|':
						name = "OR";
						break;
					case '^':
						name = "XOR";
						break;
					case '!':
						name = "EXCL";
						break;
					case '~':
						name = "TILDE";
						break;
					case '=':
						name = "EQ";
						break;
					case '<':
						name = "LESS";
						break;
					case '>':
						name = "GREATER";
						break;
					case '?':
						name = "QUESTMARK";
						break;
					default:
						name = "N" + FormatUtil.asHex(c, 2);
						break;
					}
					res.append(name);
				}
			}

			return res.toString();
		} else if (s.equals("{}")) {
			return "_sym" + number;
		} else {
			return s;
		}
	}
}
