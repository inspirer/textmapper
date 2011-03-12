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
package org.textway.lapg.test.cases;

import org.textway.lapg.common.FormatUtil;
import org.textway.lapg.lalr.ParserTables;
import org.textway.lapg.lex.LexerTables;

public class OutputUtils {

	public static void print(StringBuffer sb, int[] table, int maxwidth, int leftpadding ) {
		for( int i = 0; i < table.length; i++ ) {
			if( i > 0 ) {
				if( (i%maxwidth) == 0 ) {
					sb.append("\n");
					for( int e = 0; e < leftpadding; e++) {
						sb.append("\t");
					}
				} else {
					sb.append(" ");
				}
			}
			sb.append(table[i]);
			sb.append(",");
		}
	}

	public static void print(StringBuffer sb, short[] table, int maxwidth, int leftpadding ) {
		for( int i = 0; i < table.length; i++ ) {
			if( i > 0 ) {
				if( (i%maxwidth) == 0 ) {
					sb.append("\n");
					for( int e = 0; e < leftpadding; e++) {
						sb.append("\t");
					}
				} else {
					sb.append(" ");
				}
			}
			sb.append(table[i]);
			sb.append(",");
		}
	}

	public static void printTables(StringBuffer sb, LexerTables lt, boolean convertLNum) {

		sb.append("// tables.txt\n\n");

		sb.append("lapg_char2no = {\n\t");
		print(sb, lt.char2no, 16, 1);
		sb.append("\n}\n\n");

		sb.append("lapg_lexem ["+lt.nstates+","+lt.nchars+"] = {\n\t");
		for( int i = 0; i < lt.change.length; i++ ) {
			if( i > 0 ) {
				for( int e = 0; e < 1; e++) {
					sb.append("\t");
				}
			}
			sb.append('{');
			sb.append(" ");
			int[] row = lt.change[i];
			for (int element : row) {
				if(convertLNum && element < -2) {
					sb.append(-2 - lt.lnum[-3 - element]);
				} else {
					sb.append(element);
				}
				sb.append(", ");
			}
			sb.append('}');
			sb.append(",\n");
		}
		sb.append("}\n\n");
	}

	public static String toIdentifier(String s, int number) {

		if( s.startsWith("\'") && s.endsWith("\'")) {
			StringBuffer res = new StringBuffer();
			String inner = s.substring(1, s.length()-1);
			for( int i = 0; i < inner.length(); i++ ) {
				int c = inner.charAt(i);
				if( c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' ) {
					res.append((char)c);
				} else {
					String name;
					switch( c ) {
					case '{': name = "LBRACE"; break;
					case '}': name = "RBRACE"; break;
					case '[': name = "LBRACKET"; break;
					case ']': name = "RBRACKET"; break;
					case '(': name = "LROUNDBRACKET"; break;
					case ')': name = "RROUNDBRACKET"; break;
					case '.': name = "DOT"; break;
					case ',': name = "COMMA"; break;
					case ':': name = "COLON"; break;
					case ';': name = "SEMICOLON"; break;
					case '+': name = "PLUS"; break;
					case '-': name = "MINUS"; break;
					case '*': name = "MULT"; break;
					case '/': name = "DIV"; break;
					case '%': name = "PERC"; break;
					case '&': name = "AMP"; break;
					case '|': name = "OR"; break;
					case '^': name = "XOR"; break;
					case '!': name = "EXCL"; break;
					case '~': name = "TILDE"; break;
					case '=': name = "EQ"; break;
					case '<': name = "LESS"; break;
					case '>': name = "GREATER"; break;
					case '?': name = "QUESTMARK"; break;
					default: name = "N" + FormatUtil.asHex(c, 2);break;
					}
					res.append(name);
				}
			}

			return res.toString();
		} else if( s.equals("{}") ) {
			return "_sym" + number;
		} else {
			return s;
		}
	}

	public static void printTables(StringBuffer sb, ParserTables pt) {
		sb.append("lapg_action ["+pt.nstates+"] = {\n\t");
		print(sb, pt.action_index, 16, 1);
		sb.append("\n}\n\n");

		if( pt.nactions > 0 ) {
			sb.append("lapg_lalr ["+pt.nactions+"] = {\n\t");
			print(sb, pt.action_table, 16, 1);
			sb.append("\n}\n\n");
		}

		sb.append("lapg_sym_goto ["+pt.nsyms+"+1] = {\n\t");
		print(sb, pt.sym_goto, 16, 1);
		sb.append("\n}\n\n");

		sb.append("lapg_sym_from ["+pt.sym_goto[pt.nsyms]+"] = {\n\t");
		print(sb, pt.sym_from, 16, 1);
		sb.append("\n}\n\n");

		sb.append("lapg_sym_to ["+pt.sym_goto[pt.nsyms]+"] = {\n\t");
		print(sb, pt.sym_to, 16, 1);
		sb.append("\n}\n\n");

		sb.append("lapg_rlen ["+pt.rules+"] = {\n\t");
		for( int i = 0; i < pt.rules; i++ ) {
			if( i > 0 ) {
				if( (i%16) == 0 ) {
					sb.append("\n\t");
				} else {
					sb.append(" ");
				}
			}
			int e = 0;
			for(; pt.rright[ pt.rindex[i]+e ] >= 0; e++) {
				;
			}
			sb.append(e);
			sb.append(",");
		}
		sb.append("\n}\n\n");

		sb.append("lapg_rlex ["+pt.rules+"] = {\n\t");
		print(sb, pt.rleft, 16, 1);
		sb.append("\n}\n\n");

		sb.append("lapg_syms = {\n");
		for( int i = 0; i < pt.nsyms; i++ ) {
			sb.append("\t\"");
			sb.append(pt.sym[i].getName());
			sb.append("\",\n");
		}
		sb.append("}\n\n");

		sb.append("Tokens = {\n");
		for( int i = 0; i < pt.nsyms; i++ ) {
			sb.append("\t");
			sb.append(toIdentifier(pt.sym[i].getName(), i));
			sb.append(",\n");
		}
		sb.append("}\n\n");

	}
}
