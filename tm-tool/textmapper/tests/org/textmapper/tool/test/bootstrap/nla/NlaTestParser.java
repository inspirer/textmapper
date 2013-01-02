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
package org.textmapper.tool.test.bootstrap.nla;

import java.io.IOException;
import java.text.MessageFormat;
import org.textmapper.tool.test.bootstrap.nla.NlaTestLexer.ErrorReporter;
import org.textmapper.tool.test.bootstrap.nla.NlaTestLexer.LapgSymbol;
import org.textmapper.tool.test.bootstrap.nla.NlaTestLexer.Lexems;

public class NlaTestParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public NlaTestParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = true;
	private static final int[] lapg_action = NlaTestLexer.unpack_int(82,
		"\uffff\uffff\ufffd\uffff\13\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\14\0\uffff\uffff\uffff\uffff\uffff\uffff\uffdf\uffff\2\0\3\0\7\0\uffc3\uffff" +
		"\24\0\25\0\23\0\uffff\uffff\37\0\uffa7\uffff\46\0\50\0\uffff\uffff\uffff\uffff\uff91" +
		"\uffff\uff7b\uffff\36\0\26\0\uffff\uffff\uffff\uffff\35\0\uffff\uffff\5\0\uffff\uffff" +
		"\uff5d\uffff\1\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\4\0\uffff\uffff\47\0\52\0\uff53\uffff" +
		"\uffff\uffff\12\0\31\0\uffff\uffff\10\0\6\0\uffff\uffff\uff4d\uffff\33\0\32\0\uff2f" +
		"\uffff\uff19\uffff\uff03\uffff\ufeed\uffff\uffff\uffff\51\0\uffff\uffff\17\0\27\0" +
		"\30\0\22\0\ufed7\uffff\uffff\uffff\53\0\uffff\uffff\45\0\21\0\uffff\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = NlaTestLexer.unpack_short(316,
		"\4\uffff\12\uffff\5\11\6\11\7\11\10\11\11\11\13\11\16\11\17\11\20\11\21\11\23\11" +
		"\25\11\uffff\ufffe\1\uffff\27\uffff\2\uffff\7\uffff\10\uffff\12\uffff\14\uffff\22" +
		"\uffff\26\uffff\30\uffff\31\uffff\32\uffff\0\0\uffff\ufffe\16\uffff\21\uffff\25\uffff" +
		"\5\34\6\34\7\34\10\34\11\34\13\34\17\34\20\34\23\34\24\34\uffff\ufffe\6\uffff\7\uffff" +
		"\10\uffff\11\uffff\23\uffff\5\44\13\44\17\44\20\44\24\44\uffff\ufffe\1\uffff\27\uffff" +
		"\2\uffff\7\uffff\10\uffff\12\uffff\14\uffff\22\uffff\26\uffff\13\15\uffff\ufffe\12" +
		"\uffff\5\11\6\11\7\11\10\11\11\11\13\11\16\11\17\11\20\11\21\11\23\11\24\11\25\11" +
		"\uffff\ufffe\5\uffff\16\23\21\23\25\23\uffff\ufffe\20\uffff\13\16\uffff\ufffe\12" +
		"\uffff\5\20\6\20\7\20\10\20\11\20\13\20\16\20\17\20\20\20\21\20\23\20\24\20\25\20" +
		"\uffff\ufffe\6\42\7\42\10\uffff\11\uffff\5\42\13\42\17\42\20\42\23\42\24\42\uffff" +
		"\ufffe\6\43\7\43\10\uffff\11\uffff\5\43\13\43\17\43\20\43\23\43\24\43\uffff\ufffe" +
		"\6\40\7\40\10\40\11\40\5\40\13\40\17\40\20\40\23\40\24\40\uffff\ufffe\6\41\7\41\10" +
		"\41\11\41\5\41\13\41\17\41\20\41\23\41\24\41\uffff\ufffe\1\uffff\27\uffff\2\uffff" +
		"\7\uffff\10\uffff\12\uffff\14\uffff\22\uffff\26\uffff\13\15\uffff\ufffe");

	private static final short[] lapg_sym_goto = NlaTestLexer.unpack_short(46,
		"\0\1\30\54\54\55\61\66\116\147\154\203\207\232\233\235\236\242\244\267\270\271\274" +
		"\320\350\352\356\362\363\364\370\372\374\u0110\u0124\u0137\u0138\u014c\u0160\u0173" +
		"\u0184\u0191\u0198\u019e\u01a0\u01a2");

	private static final short[] lapg_sym_from = NlaTestLexer.unpack_short(418,
		"\120\0\3\4\5\7\11\13\14\32\33\40\47\50\53\54\55\56\57\61\70\105\112\113\0\3\5\7\13" +
		"\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113\1\12\31\42\45\26\77\100\101\102" +
		"\0\3\5\7\14\26\32\33\40\47\53\54\55\56\57\61\70\77\100\101\102\105\112\113\0\3\5" +
		"\7\13\14\26\32\33\40\47\53\54\55\56\57\61\70\77\100\101\102\105\112\113\26\77\100" +
		"\101\102\0\1\3\5\7\14\24\32\33\34\40\47\53\54\55\56\57\61\70\74\105\112\113\37\52" +
		"\65\115\0\3\5\7\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113\70\20\44\73\31\37" +
		"\64\73\20\44\0\3\5\7\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113\26\103\6\20" +
		"\44\0\3\5\7\13\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113\0\3\4\5\7\11\13\14" +
		"\32\33\40\47\50\51\53\54\55\56\57\61\70\105\112\113\0\14\0\14\40\70\0\14\40\70\0" +
		"\0\0\14\40\70\0\14\0\14\0\3\5\7\13\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113" +
		"\0\3\5\7\13\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113\0\3\5\7\14\32\33\40\47" +
		"\53\54\55\56\57\61\70\105\112\113\40\0\3\5\7\13\14\32\33\40\47\53\54\55\56\57\61" +
		"\70\105\112\113\0\3\5\7\13\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113\0\3\5" +
		"\7\14\32\33\40\47\53\54\55\56\57\61\70\105\112\113\0\5\14\32\33\40\47\53\54\55\56" +
		"\57\61\70\105\112\113\0\5\14\32\33\40\47\57\61\70\105\112\113\0\5\14\40\47\61\70" +
		"\0\5\14\40\47\70\33\112\33\112");

	private static final short[] lapg_sym_to = NlaTestLexer.unpack_short(418,
		"\121\1\34\36\1\34\42\34\1\34\34\1\1\74\34\34\34\34\34\1\1\34\34\34\2\2\2\2\2\2\2" +
		"\2\2\2\2\2\2\2\2\2\2\2\2\2\32\43\60\71\72\53\53\53\53\53\3\3\3\3\3\54\3\3\3\3\3\3" +
		"\3\3\3\3\3\54\54\54\54\3\3\3\4\4\4\4\4\4\55\4\4\4\4\4\4\4\4\4\4\4\55\55\55\55\4\4" +
		"\4\56\56\56\56\56\5\33\5\5\5\5\52\5\5\33\5\5\5\5\5\5\5\5\5\112\5\5\5\66\76\106\117" +
		"\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\107\47\47\111\61\61\105\61\50\50\7\7\7\7\7" +
		"\7\7\7\7\7\7\7\7\7\7\7\7\7\7\57\113\40\51\51\10\10\10\10\10\10\10\10\10\10\10\10" +
		"\10\10\10\10\10\10\10\10\1\34\36\1\34\42\34\1\34\34\1\1\74\75\34\34\34\34\34\1\1" +
		"\34\34\34\11\11\12\12\12\12\13\13\13\13\120\14\15\46\67\110\16\16\17\17\20\20\20" +
		"\20\44\20\20\20\20\20\20\20\20\20\20\20\20\20\20\20\21\21\21\21\21\21\21\21\21\21" +
		"\21\21\21\21\21\21\21\21\21\21\22\22\22\22\22\22\22\22\22\22\22\22\22\22\22\22\22" +
		"\22\22\70\23\23\23\23\45\23\23\23\23\23\23\23\23\23\23\23\23\23\23\23\24\24\24\24" +
		"\24\24\24\24\24\24\24\24\24\24\24\24\24\24\24\24\25\35\25\41\25\25\25\25\25\25\25" +
		"\25\25\25\25\25\25\25\25\26\26\26\26\26\26\26\77\100\101\102\26\26\26\26\26\26\27" +
		"\27\27\62\63\27\27\103\27\27\114\63\116\30\30\30\30\30\104\30\31\37\31\31\73\31\64" +
		"\64\65\115");

	private static final short[] lapg_rlen = NlaTestLexer.unpack_short(44,
		"\1\2\1\1\2\2\3\1\3\1\3\1\1\0\1\4\3\6\4\1\1\1\2\4\2\1\3\3\1\2\2\1\3\3\3\3\1\5\1\3" +
		"\1\3\1\3");

	private static final short[] lapg_rlex = NlaTestLexer.unpack_short(44,
		"\33\34\34\35\35\35\35\36\37\40\40\40\40\54\54\40\40\40\40\40\40\41\41\42\43\43\44" +
		"\45\46\46\46\47\47\47\47\47\50\50\51\51\52\52\53\53");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"icon",
		"_skip",
		"'='",
		"';'",
		"'+'",
		"'-'",
		"'*'",
		"'/'",
		"'('",
		"')'",
		"'{'",
		"'}'",
		"'['",
		"']'",
		"','",
		"'.'",
		"'!'",
		"'?'",
		"':'",
		"'->'",
		"Lnull",
		"Linvoke",
		"Lreset",
		"Lnop",
		"Lexotic",
		"input",
		"statements",
		"statement",
		"control_statement",
		"reset_statement",
		"primary_expression",
		"closure_rule",
		"closure",
		"statements_noreset",
		"exotic_call",
		"exotic_call_prefix",
		"unary_expression",
		"binary_op",
		"conditional_expression",
		"assignment_expression",
		"expression",
		"expression_list",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 27;
		public static final int statements = 28;
		public static final int statement = 29;
		public static final int control_statement = 30;
		public static final int reset_statement = 31;
		public static final int primary_expression = 32;
		public static final int closure_rule = 33;
		public static final int closure = 34;
		public static final int statements_noreset = 35;
		public static final int exotic_call = 36;
		public static final int exotic_call_prefix = 37;
		public static final int unary_expression = 38;
		public static final int binary_op = 39;
		public static final int conditional_expression = 40;
		public static final int assignment_expression = 41;
		public static final int expression = 42;
		public static final int expression_list = 43;
		public static final int expression_listopt = 44;
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.symbol) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final int lapg_state_sym(int state, int symbol) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol + 1] - 1;
		int i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if (i == state) {
				return lapg_sym_to[e];
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
			}
		}
		return -1;
	}

	protected int lapg_head;
	protected LapgSymbol[] lapg_m;
	protected LapgSymbol lapg_n;
	protected NlaTestLexer lapg_lexer;

	public Object parse(NlaTestLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 81) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != 81) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].value;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.symbol], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.symbol != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.value = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].value : null;
		lapg_gg.symbol = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
	}
}
