package org.textway.lapg.test.cases.bootstrap.nla;

import java.io.IOException;
import java.text.MessageFormat;
import org.textway.lapg.test.cases.bootstrap.nla.NlaTestLexer.ErrorReporter;
import org.textway.lapg.test.cases.bootstrap.nla.NlaTestLexer.LapgSymbol;
import org.textway.lapg.test.cases.bootstrap.nla.NlaTestLexer.Lexems;

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
		-1, -3, 8, -1, -1, -1, -1, 9, -1, -1, -33, 2, -57, 17, 16, -1,
		26, -85, 33, 35, -1, -1, -107, -127, 25, -1, -1, 24, -1, 5, 1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, 4, -1, 34, 37, -157, -1, 7, 20,
		-1, 3, -1, -163, 22, 21, -193, -215, -237, -259, -1, 36, -1, 12, 18, 19,
		15, -281, -1, 38, -1, 32, 14, -1, -2
	};

	private static final short lapg_lalr[] = {
		4, -1, 10, -1, 5, 6, 6, 6, 7, 6, 8, 6, 9, 6, 11, 6,
		14, 6, 15, 6, 16, 6, 17, 6, 19, 6, 21, 6, -1, -2, 1, -1,
		23, -1, 2, -1, 7, -1, 10, -1, 12, -1, 18, -1, 22, -1, 24, -1,
		25, -1, 0, 0, -1, -2, 14, -1, 17, -1, 21, -1, 5, 23, 6, 23,
		7, 23, 8, 23, 9, 23, 11, 23, 15, 23, 16, 23, 19, 23, 20, 23,
		-1, -2, 6, -1, 7, -1, 8, -1, 9, -1, 19, -1, 5, 31, 11, 31,
		15, 31, 16, 31, 20, 31, -1, -2, 1, -1, 23, -1, 2, -1, 7, -1,
		10, -1, 12, -1, 18, -1, 22, -1, 11, 10, -1, -2, 10, -1, 5, 6,
		6, 6, 7, 6, 8, 6, 9, 6, 11, 6, 14, 6, 15, 6, 16, 6,
		17, 6, 19, 6, 20, 6, 21, 6, -1, -2, 16, -1, 11, 11, -1, -2,
		10, -1, 5, 13, 6, 13, 7, 13, 8, 13, 9, 13, 11, 13, 14, 13,
		15, 13, 16, 13, 17, 13, 19, 13, 20, 13, 21, 13, -1, -2, 6, 29,
		7, 29, 8, -1, 9, -1, 5, 29, 11, 29, 15, 29, 16, 29, 19, 29,
		20, 29, -1, -2, 6, 30, 7, 30, 8, -1, 9, -1, 5, 30, 11, 30,
		15, 30, 16, 30, 19, 30, 20, 30, -1, -2, 6, 27, 7, 27, 8, 27,
		9, 27, 5, 27, 11, 27, 15, 27, 16, 27, 19, 27, 20, 27, -1, -2,
		6, 28, 7, 28, 8, 28, 9, 28, 5, 28, 11, 28, 15, 28, 16, 28,
		19, 28, 20, 28, -1, -2, 1, -1, 23, -1, 2, -1, 7, -1, 10, -1,
		12, -1, 18, -1, 22, -1, 11, 10, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 22, 41, 41, 42, 45, 50, 74, 79, 84, 107, 111, 130, 131, 132,
		133, 137, 138, 157, 158, 159, 161, 180, 202, 206, 210, 211, 212, 216, 235, 254,
		255, 274, 293, 312, 329, 342, 349, 355, 357, 359
	};

	private static final short lapg_sym_from[] = {
		71, 0, 3, 4, 6, 8, 10, 21, 22, 26, 31, 32, 35, 36, 37, 38,
		39, 41, 48, 60, 65, 66, 0, 3, 4, 6, 10, 21, 22, 26, 31, 35,
		36, 37, 38, 39, 41, 48, 60, 65, 66, 1, 9, 20, 28, 17, 54, 55,
		56, 57, 0, 3, 4, 6, 10, 17, 21, 22, 26, 31, 35, 36, 37, 38,
		39, 41, 48, 54, 55, 56, 57, 60, 65, 66, 17, 54, 55, 56, 57, 17,
		54, 55, 56, 57, 0, 1, 3, 4, 6, 10, 15, 21, 22, 23, 26, 31,
		35, 36, 37, 38, 39, 41, 48, 51, 60, 65, 66, 25, 34, 45, 68, 0,
		3, 4, 6, 10, 21, 22, 26, 31, 35, 36, 37, 38, 39, 41, 48, 60,
		65, 66, 48, 12, 50, 20, 25, 44, 50, 12, 0, 3, 4, 6, 10, 21,
		22, 26, 31, 35, 36, 37, 38, 39, 41, 48, 60, 65, 66, 17, 58, 5,
		12, 0, 3, 4, 6, 10, 21, 22, 26, 31, 35, 36, 37, 38, 39, 41,
		48, 60, 65, 66, 0, 3, 4, 6, 8, 10, 21, 22, 26, 31, 32, 33,
		35, 36, 37, 38, 39, 41, 48, 60, 65, 66, 0, 10, 26, 48, 0, 10,
		26, 48, 0, 0, 0, 10, 26, 48, 0, 3, 4, 6, 10, 21, 22, 26,
		31, 35, 36, 37, 38, 39, 41, 48, 60, 65, 66, 0, 3, 4, 6, 10,
		21, 22, 26, 31, 35, 36, 37, 38, 39, 41, 48, 60, 65, 66, 26, 0,
		3, 4, 6, 10, 21, 22, 26, 31, 35, 36, 37, 38, 39, 41, 48, 60,
		65, 66, 0, 3, 4, 6, 10, 21, 22, 26, 31, 35, 36, 37, 38, 39,
		41, 48, 60, 65, 66, 0, 3, 4, 6, 10, 21, 22, 26, 31, 35, 36,
		37, 38, 39, 41, 48, 60, 65, 66, 0, 4, 10, 21, 22, 26, 31, 35,
		36, 37, 38, 39, 41, 48, 60, 65, 66, 0, 4, 10, 21, 22, 26, 31,
		39, 41, 48, 60, 65, 66, 0, 4, 10, 26, 31, 41, 48, 0, 4, 10,
		26, 31, 48, 22, 65, 22, 65
	};

	private static final short lapg_sym_to[] = {
		72, 1, 23, 1, 23, 28, 1, 23, 23, 1, 1, 51, 23, 23, 23, 23,
		23, 1, 1, 23, 23, 23, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 21, 29, 40, 49, 35, 35, 35,
		35, 35, 3, 3, 3, 3, 3, 36, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 36, 36, 36, 36, 3, 3, 3, 37, 37, 37, 37, 37, 38,
		38, 38, 38, 38, 4, 22, 4, 4, 4, 4, 34, 4, 4, 22, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 65, 4, 4, 4, 46, 53, 61, 70, 5,
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
		5, 5, 62, 31, 64, 41, 41, 60, 41, 32, 6, 6, 6, 6, 6, 6,
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 39, 66, 26,
		33, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 1, 23, 1, 23, 28, 1, 23, 23, 1, 1, 51, 52,
		23, 23, 23, 23, 23, 1, 1, 23, 23, 23, 8, 8, 8, 8, 9, 9,
		9, 9, 71, 10, 11, 30, 47, 63, 12, 12, 12, 12, 12, 12, 12, 12,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13,
		13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 48, 14,
		14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
		14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
		15, 15, 15, 15, 15, 16, 24, 16, 27, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 54,
		55, 56, 57, 17, 17, 17, 17, 17, 17, 18, 18, 18, 42, 43, 18, 18,
		58, 18, 18, 67, 43, 69, 19, 19, 19, 19, 19, 59, 19, 20, 25, 20,
		20, 50, 20, 44, 44, 45, 68
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 3, 2, 2, 1, 3, 1, 1, 0, 1, 4, 3, 6, 4,
		1, 1, 4, 2, 1, 3, 3, 1, 2, 2, 1, 3, 3, 3, 3, 1,
		5, 1, 3, 1, 3, 1, 3
	};

	private static final short lapg_rlex[] = {
		26, 27, 27, 28, 28, 28, 29, 29, 29, 29, 40, 40, 29, 29, 29, 29,
		29, 29, 30, 31, 31, 32, 33, 34, 34, 34, 35, 35, 35, 35, 35, 36,
		36, 37, 37, 38, 38, 39, 39
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
		"input",
		"statements",
		"statement",
		"primary_expression",
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
		public static final int input = 26;
		public static final int statements = 27;
		public static final int statement = 28;
		public static final int primary_expression = 29;
		public static final int closure = 30;
		public static final int statements_noreset = 31;
		public static final int exotic_call = 32;
		public static final int exotic_call_prefix = 33;
		public static final int unary_expression = 34;
		public static final int binary_op = 35;
		public static final int conditional_expression = 36;
		public static final int assignment_expression = 37;
		public static final int expression = 38;
		public static final int expression_list = 39;
		public static final int expression_listopt = 40;
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

		while (lapg_m[lapg_head].state != 72) {
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

		if (lapg_m[lapg_head].state != 72) {
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
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
	}
}
