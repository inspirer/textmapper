package net.sf.lapg.gen;

import java.io.PrintStream;

import net.sf.lapg.LexerTables;
import net.sf.lapg.ParserTables;
import net.sf.lapg.common.FormatUtil;

public class OutputUtils {

	public static void print(PrintStream out, int[] table, int maxwidth, int leftpadding ) {
		for( int i = 0; i < table.length; i++ ) {
			if( i > 0 ) {
				if( (i%maxwidth) == 0 ) {
					out.print("\n");
					for( int e = 0; e < leftpadding; e++) {
						out.print("\t");
					}
				} else {
					out.print(" ");
				}
			}
			out.print(table[i]);
			out.print(",");
		}
	}

	public static void print(PrintStream out, short[] table, int maxwidth, int leftpadding ) {
		for( int i = 0; i < table.length; i++ ) {
			if( i > 0 ) {
				if( (i%maxwidth) == 0 ) {
					out.print("\n");
					for( int e = 0; e < leftpadding; e++) {
						out.print("\t");
					}
				} else {
					out.print(" ");
				}
			}
			out.print(table[i]);
			out.print(",");
		}
	}

	public static void print(PrintStream out, int[][] table, int leftpadding, char startrow, char endrow ) {
		for( int i = 0; i < table.length; i++ ) {
			if( i > 0 ) {
				for( int e = 0; e < leftpadding; e++) {
					out.print("\t");
				}
			}
			out.print(startrow);
			out.print(" ");
			int[] row = table[i];
			for( int e = 0; e < row.length; e++ ) {
				out.print(row[e]);
				out.print(", ");
			}
			out.print(endrow);
			out.print(",\n");
		}
	}

	public static void printTables(PrintStream out, LexerTables lt) {

		out.print("// tables.txt\n\n");

		out.print("lapg_char2no = {\n\t");
		print(out, lt.char2no, 16, 1);
		out.print("\n}\n\n");

		out.print("lapg_lexem ["+lt.nstates+","+lt.nchars+"] = {\n\t");
		print(out, lt.change, 1, '{', '}');
		out.print("}\n\n");
	}

	public static String toIdentifier(String s, int number) {

		if( s.startsWith("\'") && s.endsWith("\'")) {
			StringBuffer res = new StringBuffer();
			String inner = s.substring(1, s.length()-1);
			for( int i = 0; i < inner.length(); i++ ) {
				int c = inner.charAt(i);
				if( c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' ) {
					res.append(c);
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

	public static void printTables(PrintStream out, ParserTables pt) {
		out.print("lapg_action ["+pt.nstates+"] = {\n\t");
		print(out, pt.action_index, 16, 1);
		out.print("\n}\n\n");

		if( pt.nactions > 0 ) {
			out.print("lapg_lalr ["+pt.nactions+"] = {\n\t");
			print(out, pt.action_table, 16, 1);
			out.print("\n}\n\n");
		}

		out.print("lapg_sym_goto ["+pt.nsyms+"+1] = {\n\t");
		print(out, pt.sym_goto, 16, 1);
		out.print("\n}\n\n");

		out.print("lapg_sym_from ["+pt.sym_goto[pt.nsyms]+"] = {\n\t");
		print(out, pt.sym_from, 16, 1);
		out.print("\n}\n\n");

		out.print("lapg_sym_to ["+pt.sym_goto[pt.nsyms]+"] = {\n\t");
		print(out, pt.sym_to, 16, 1);
		out.print("\n}\n\n");

		out.print("lapg_rlen ["+pt.rules+"] = {\n\t");
		for( int i = 0; i < pt.rules; i++ ) {
			if( i > 0 ) {
				if( (i%16) == 0 ) {
					out.print("\n\t");
				} else {
					out.print(" ");
				}
			}
			int e = 0;
			for(; pt.rright[ pt.rindex[i]+e ] >= 0; e++) {
				;
			}
			out.print(e);
			out.print(",");
		}
		out.print("\n}\n\n");

		out.print("lapg_rlex ["+pt.rules+"] = {\n\t");
		print(out, pt.rleft, 16, 1);
		out.print("\n}\n\n");

		out.print("lapg_syms = {\n");
		for( int i = 0; i < pt.nsyms; i++ ) {
			out.print("\t\"");
			out.print(pt.sym[i].getName());
			out.print("\",\n");
		}
		out.print("}\n\n");

		out.print("Tokens = {\n");
		for( int i = 0; i < pt.nsyms; i++ ) {
			out.print("\t");
			out.print(toIdentifier(pt.sym[i].getName(), i));
			out.print(",\n");
		}
		out.print("}\n\n");

	}
}
