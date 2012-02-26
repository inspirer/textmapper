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
package org.textway.lapg.test.bootstrap.nla;

import java.io.IOException;
import java.text.MessageFormat;
import org.textway.lapg.test.bootstrap.nla.NlaTestLexer.ErrorReporter;
import org.textway.lapg.test.bootstrap.nla.NlaTestLexer.LapgSymbol;
import org.textway.lapg.test.bootstrap.nla.NlaTestLexer.Lexems;

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
	private static final int lapg_action[] = {
		-1, -3, 11, -1, -1, -1, -1, -1, 12, -1, -1, -1, -33, 2, 3, 7,
		-61, 20, 21, 19, -1, 31, -89, 38, 40, -1, -1, -111, -133, 30, 22, -1,
		-1, 29, -1, 5, -1, -163, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		4, -1, 39, 42, -173, -1, 10, 25, -1, 8, 6, -1, -179, 27, 26, -209,
		-231, -253, -275, -1, 41, -1, 15, 23, 24, 18, -297, -1, 43, -1, 37, 17,
		-1, -2
	};

	private static final short lapg_lalr[] = {
		4, -1, 10, -1, 5, 9, 6, 9, 7, 9, 8, 9, 9, 9, 11, 9,
		14, 9, 15, 9, 16, 9, 17, 9, 19, 9, 21, 9, -1, -2, 1, -1,
		23, -1, 2, -1, 7, -1, 8, -1, 10, -1, 12, -1, 18, -1, 22, -1,
		24, -1, 25, -1, 26, -1, 0, 0, -1, -2, 14, -1, 17, -1, 21, -1,
		5, 28, 6, 28, 7, 28, 8, 28, 9, 28, 11, 28, 15, 28, 16, 28,
		19, 28, 20, 28, -1, -2, 6, -1, 7, -1, 8, -1, 9, -1, 19, -1,
		5, 36, 11, 36, 15, 36, 16, 36, 20, 36, -1, -2, 1, -1, 23, -1,
		2, -1, 7, -1, 8, -1, 10, -1, 12, -1, 18, -1, 22, -1, 11, 13,
		-1, -2, 10, -1, 5, 9, 6, 9, 7, 9, 8, 9, 9, 9, 11, 9,
		14, 9, 15, 9, 16, 9, 17, 9, 19, 9, 20, 9, 21, 9, -1, -2,
		5, -1, 14, 19, 17, 19, 21, 19, -1, -2, 16, -1, 11, 14, -1, -2,
		10, -1, 5, 16, 6, 16, 7, 16, 8, 16, 9, 16, 11, 16, 14, 16,
		15, 16, 16, 16, 17, 16, 19, 16, 20, 16, 21, 16, -1, -2, 6, 34,
		7, 34, 8, -1, 9, -1, 5, 34, 11, 34, 15, 34, 16, 34, 19, 34,
		20, 34, -1, -2, 6, 35, 7, 35, 8, -1, 9, -1, 5, 35, 11, 35,
		15, 35, 16, 35, 19, 35, 20, 35, -1, -2, 6, 32, 7, 32, 8, 32,
		9, 32, 5, 32, 11, 32, 15, 32, 16, 32, 19, 32, 20, 32, -1, -2,
		6, 33, 7, 33, 8, 33, 9, 33, 5, 33, 11, 33, 15, 33, 16, 33,
		19, 33, 20, 33, -1, -2, 1, -1, 23, -1, 2, -1, 7, -1, 8, -1,
		10, -1, 12, -1, 18, -1, 22, -1, 11, 13, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 24, 44, 44, 45, 49, 54, 78, 103, 108, 131, 135, 154, 155, 157,
		158, 162, 164, 183, 184, 185, 188, 208, 232, 234, 238, 242, 243, 244, 248, 250,
		252, 272, 292, 311, 312, 332, 352, 371, 388, 401, 408, 414, 416, 418
	};

	private static final short lapg_sym_from[] = {
		80, 0, 3, 4, 5, 7, 9, 11, 12, 26, 27, 32, 39, 40, 43, 44,
		45, 46, 47, 49, 56, 69, 74, 75, 0, 3, 5, 7, 11, 12, 26, 27,
		32, 39, 43, 44, 45, 46, 47, 49, 56, 69, 74, 75, 1, 10, 25, 34,
		37, 22, 63, 64, 65, 66, 0, 3, 5, 7, 12, 22, 26, 27, 32, 39,
		43, 44, 45, 46, 47, 49, 56, 63, 64, 65, 66, 69, 74, 75, 0, 3,
		5, 7, 11, 12, 22, 26, 27, 32, 39, 43, 44, 45, 46, 47, 49, 56,
		63, 64, 65, 66, 69, 74, 75, 22, 63, 64, 65, 66, 0, 1, 3, 5,
		7, 12, 20, 26, 27, 28, 32, 39, 43, 44, 45, 46, 47, 49, 56, 60,
		69, 74, 75, 31, 42, 53, 77, 0, 3, 5, 7, 12, 26, 27, 32, 39,
		43, 44, 45, 46, 47, 49, 56, 69, 74, 75, 56, 16, 36, 59, 25, 31,
		52, 59, 16, 36, 0, 3, 5, 7, 12, 26, 27, 32, 39, 43, 44, 45,
		46, 47, 49, 56, 69, 74, 75, 22, 67, 6, 16, 36, 0, 3, 5, 7,
		11, 12, 26, 27, 32, 39, 43, 44, 45, 46, 47, 49, 56, 69, 74, 75,
		0, 3, 4, 5, 7, 9, 11, 12, 26, 27, 32, 39, 40, 41, 43, 44,
		45, 46, 47, 49, 56, 69, 74, 75, 0, 12, 0, 12, 32, 56, 0, 12,
		32, 56, 0, 0, 0, 12, 32, 56, 0, 12, 0, 12, 0, 3, 5, 7,
		11, 12, 26, 27, 32, 39, 43, 44, 45, 46, 47, 49, 56, 69, 74, 75,
		0, 3, 5, 7, 11, 12, 26, 27, 32, 39, 43, 44, 45, 46, 47, 49,
		56, 69, 74, 75, 0, 3, 5, 7, 12, 26, 27, 32, 39, 43, 44, 45,
		46, 47, 49, 56, 69, 74, 75, 32, 0, 3, 5, 7, 11, 12, 26, 27,
		32, 39, 43, 44, 45, 46, 47, 49, 56, 69, 74, 75, 0, 3, 5, 7,
		11, 12, 26, 27, 32, 39, 43, 44, 45, 46, 47, 49, 56, 69, 74, 75,
		0, 3, 5, 7, 12, 26, 27, 32, 39, 43, 44, 45, 46, 47, 49, 56,
		69, 74, 75, 0, 5, 12, 26, 27, 32, 39, 43, 44, 45, 46, 47, 49,
		56, 69, 74, 75, 0, 5, 12, 26, 27, 32, 39, 47, 49, 56, 69, 74,
		75, 0, 5, 12, 32, 39, 49, 56, 0, 5, 12, 32, 39, 56, 27, 74,
		27, 74
	};

	private static final short lapg_sym_to[] = {
		81, 1, 28, 30, 1, 28, 34, 28, 1, 28, 28, 1, 1, 60, 28, 28,
		28, 28, 28, 1, 1, 28, 28, 28, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 26, 35, 48, 57,
		58, 43, 43, 43, 43, 43, 3, 3, 3, 3, 3, 44, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 44, 44, 44, 44, 3, 3, 3, 4, 4,
		4, 4, 4, 4, 45, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
		45, 45, 45, 45, 4, 4, 4, 46, 46, 46, 46, 46, 5, 27, 5, 5,
		5, 5, 42, 5, 5, 27, 5, 5, 5, 5, 5, 5, 5, 5, 5, 74,
		5, 5, 5, 54, 62, 70, 79, 6, 6, 6, 6, 6, 6, 6, 6, 6,
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 71, 39, 39, 73, 49, 49,
		69, 49, 40, 40, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 47, 75, 32, 41, 41, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		1, 28, 30, 1, 28, 34, 28, 1, 28, 28, 1, 1, 60, 61, 28, 28,
		28, 28, 28, 1, 1, 28, 28, 28, 9, 9, 10, 10, 10, 10, 11, 11,
		11, 11, 80, 12, 13, 38, 55, 72, 14, 14, 15, 15, 16, 16, 16, 16,
		36, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
		17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
		17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 18, 18, 18, 18, 56, 19, 19, 19, 19, 37, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20,
		20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
		21, 29, 21, 33, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
		21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 63, 64, 65, 66, 22, 22,
		22, 22, 22, 22, 23, 23, 23, 50, 51, 23, 23, 67, 23, 23, 76, 51,
		78, 24, 24, 24, 24, 24, 68, 24, 25, 31, 25, 25, 59, 25, 52, 52,
		53, 77
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 1, 2, 2, 3, 1, 3, 1, 3, 1, 1, 0, 1, 4,
		3, 6, 4, 1, 1, 1, 2, 4, 2, 1, 3, 3, 1, 2, 2, 1,
		3, 3, 3, 3, 1, 5, 1, 3, 1, 3, 1, 3
	};

	private static final short lapg_rlex[] = {
		27, 28, 28, 29, 29, 29, 29, 30, 31, 32, 32, 32, 32, 44, 44, 32,
		32, 32, 32, 32, 32, 33, 33, 34, 35, 35, 36, 37, 38, 38, 38, 39,
		39, 39, 39, 39, 40, 40, 41, 41, 42, 42, 43, 43
	};

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
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected static final int lapg_state_sym(int state, int symbol) {
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
		return lapg_m[lapg_head - 1].sym;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
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
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
	}
}
