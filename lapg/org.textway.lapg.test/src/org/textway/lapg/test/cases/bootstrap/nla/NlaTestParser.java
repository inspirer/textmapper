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
		-1, -3, 10, -1, -1, -1, -1, -1, 11, -1, -1, -33, 2, 3, 6, -59,
		19, 20, 18, -1, 30, -87, 37, 39, -1, -1, -109, -131, 29, 21, -1, -1,
		28, -1, 5, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, -1, 38,
		41, -161, -1, 9, 24, -1, 7, -1, -167, 26, 25, -197, -219, -241, -263, -1,
		40, -1, 14, 22, 23, 17, -285, -1, 42, -1, 36, 16, -1, -2
	};

	private static final short lapg_lalr[] = {
		4, -1, 10, -1, 5, 8, 6, 8, 7, 8, 8, 8, 9, 8, 11, 8,
		14, 8, 15, 8, 16, 8, 17, 8, 19, 8, 21, 8, -1, -2, 1, -1,
		23, -1, 2, -1, 7, -1, 8, -1, 10, -1, 12, -1, 18, -1, 22, -1,
		24, -1, 25, -1, 0, 0, -1, -2, 14, -1, 17, -1, 21, -1, 5, 27,
		6, 27, 7, 27, 8, 27, 9, 27, 11, 27, 15, 27, 16, 27, 19, 27,
		20, 27, -1, -2, 6, -1, 7, -1, 8, -1, 9, -1, 19, -1, 5, 35,
		11, 35, 15, 35, 16, 35, 20, 35, -1, -2, 1, -1, 23, -1, 2, -1,
		7, -1, 8, -1, 10, -1, 12, -1, 18, -1, 22, -1, 11, 12, -1, -2,
		10, -1, 5, 8, 6, 8, 7, 8, 8, 8, 9, 8, 11, 8, 14, 8,
		15, 8, 16, 8, 17, 8, 19, 8, 20, 8, 21, 8, -1, -2, 16, -1,
		11, 13, -1, -2, 10, -1, 5, 15, 6, 15, 7, 15, 8, 15, 9, 15,
		11, 15, 14, 15, 15, 15, 16, 15, 17, 15, 19, 15, 20, 15, 21, 15,
		-1, -2, 6, 33, 7, 33, 8, -1, 9, -1, 5, 33, 11, 33, 15, 33,
		16, 33, 19, 33, 20, 33, -1, -2, 6, 34, 7, 34, 8, -1, 9, -1,
		5, 34, 11, 34, 15, 34, 16, 34, 19, 34, 20, 34, -1, -2, 6, 31,
		7, 31, 8, 31, 9, 31, 5, 31, 11, 31, 15, 31, 16, 31, 19, 31,
		20, 31, -1, -2, 6, 32, 7, 32, 8, 32, 9, 32, 5, 32, 11, 32,
		15, 32, 16, 32, 19, 32, 20, 32, -1, -2, 1, -1, 23, -1, 2, -1,
		7, -1, 8, -1, 10, -1, 12, -1, 18, -1, 22, -1, 11, 12, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 23, 42, 42, 43, 46, 51, 75, 99, 104, 127, 131, 150, 151, 152,
		153, 157, 158, 177, 178, 179, 181, 200, 223, 227, 231, 232, 233, 237, 241, 245,
		264, 283, 302, 303, 322, 341, 360, 377, 390, 397, 403, 405, 407
	};

	private static final short lapg_sym_from[] = {
		76, 0, 3, 4, 5, 7, 9, 11, 25, 26, 31, 36, 37, 40, 41, 42,
		43, 44, 46, 53, 65, 70, 71, 0, 3, 5, 7, 11, 25, 26, 31, 36,
		40, 41, 42, 43, 44, 46, 53, 65, 70, 71, 1, 10, 24, 33, 21, 59,
		60, 61, 62, 0, 3, 5, 7, 11, 21, 25, 26, 31, 36, 40, 41, 42,
		43, 44, 46, 53, 59, 60, 61, 62, 65, 70, 71, 0, 3, 5, 7, 11,
		21, 25, 26, 31, 36, 40, 41, 42, 43, 44, 46, 53, 59, 60, 61, 62,
		65, 70, 71, 21, 59, 60, 61, 62, 0, 1, 3, 5, 7, 11, 19, 25,
		26, 27, 31, 36, 40, 41, 42, 43, 44, 46, 53, 56, 65, 70, 71, 30,
		39, 50, 73, 0, 3, 5, 7, 11, 25, 26, 31, 36, 40, 41, 42, 43,
		44, 46, 53, 65, 70, 71, 53, 15, 55, 24, 30, 49, 55, 15, 0, 3,
		5, 7, 11, 25, 26, 31, 36, 40, 41, 42, 43, 44, 46, 53, 65, 70,
		71, 21, 63, 6, 15, 0, 3, 5, 7, 11, 25, 26, 31, 36, 40, 41,
		42, 43, 44, 46, 53, 65, 70, 71, 0, 3, 4, 5, 7, 9, 11, 25,
		26, 31, 36, 37, 38, 40, 41, 42, 43, 44, 46, 53, 65, 70, 71, 0,
		11, 31, 53, 0, 11, 31, 53, 0, 0, 0, 11, 31, 53, 0, 11, 31,
		53, 0, 11, 31, 53, 0, 3, 5, 7, 11, 25, 26, 31, 36, 40, 41,
		42, 43, 44, 46, 53, 65, 70, 71, 0, 3, 5, 7, 11, 25, 26, 31,
		36, 40, 41, 42, 43, 44, 46, 53, 65, 70, 71, 0, 3, 5, 7, 11,
		25, 26, 31, 36, 40, 41, 42, 43, 44, 46, 53, 65, 70, 71, 31, 0,
		3, 5, 7, 11, 25, 26, 31, 36, 40, 41, 42, 43, 44, 46, 53, 65,
		70, 71, 0, 3, 5, 7, 11, 25, 26, 31, 36, 40, 41, 42, 43, 44,
		46, 53, 65, 70, 71, 0, 3, 5, 7, 11, 25, 26, 31, 36, 40, 41,
		42, 43, 44, 46, 53, 65, 70, 71, 0, 5, 11, 25, 26, 31, 36, 40,
		41, 42, 43, 44, 46, 53, 65, 70, 71, 0, 5, 11, 25, 26, 31, 36,
		44, 46, 53, 65, 70, 71, 0, 5, 11, 31, 36, 46, 53, 0, 5, 11,
		31, 36, 53, 26, 70, 26, 70
	};

	private static final short lapg_sym_to[] = {
		77, 1, 27, 29, 1, 27, 33, 1, 27, 27, 1, 1, 56, 27, 27, 27,
		27, 27, 1, 1, 27, 27, 27, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 25, 34, 45, 54, 40, 40,
		40, 40, 40, 3, 3, 3, 3, 3, 41, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 41, 41, 41, 41, 3, 3, 3, 4, 4, 4, 4, 4,
		42, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 42, 42, 42, 42,
		4, 4, 4, 43, 43, 43, 43, 43, 5, 26, 5, 5, 5, 5, 39, 5,
		5, 26, 5, 5, 5, 5, 5, 5, 5, 5, 5, 70, 5, 5, 5, 51,
		58, 66, 75, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
		6, 6, 6, 6, 6, 6, 67, 36, 69, 46, 46, 65, 46, 37, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 44, 71, 31, 38, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 1, 27, 29, 1, 27, 33, 1, 27,
		27, 1, 1, 56, 57, 27, 27, 27, 27, 27, 1, 1, 27, 27, 27, 9,
		9, 9, 9, 10, 10, 10, 10, 76, 11, 12, 35, 52, 68, 13, 13, 13,
		13, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
		15, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17,
		17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 53, 18,
		18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 20, 28, 20, 32, 20, 20, 20, 20, 20, 20, 20,
		20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21, 59,
		60, 61, 62, 21, 21, 21, 21, 21, 21, 22, 22, 22, 47, 48, 22, 22,
		63, 22, 22, 72, 48, 74, 23, 23, 23, 23, 23, 64, 23, 24, 30, 24,
		24, 55, 24, 49, 49, 50, 73
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 1, 2, 2, 1, 3, 1, 3, 1, 1, 0, 1, 4, 3,
		6, 4, 1, 1, 1, 2, 4, 2, 1, 3, 3, 1, 2, 2, 1, 3,
		3, 3, 3, 1, 5, 1, 3, 1, 3, 1, 3
	};

	private static final short lapg_rlex[] = {
		26, 27, 27, 28, 28, 28, 29, 30, 31, 31, 31, 31, 43, 43, 31, 31,
		31, 31, 31, 31, 32, 32, 33, 34, 34, 35, 36, 37, 37, 37, 38, 38,
		38, 38, 38, 39, 39, 40, 40, 41, 41, 42, 42
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
		public static final int input = 26;
		public static final int statements = 27;
		public static final int statement = 28;
		public static final int control_statement = 29;
		public static final int reset_statement = 30;
		public static final int primary_expression = 31;
		public static final int closure_rule = 32;
		public static final int closure = 33;
		public static final int statements_noreset = 34;
		public static final int exotic_call = 35;
		public static final int exotic_call_prefix = 36;
		public static final int unary_expression = 37;
		public static final int binary_op = 38;
		public static final int conditional_expression = 39;
		public static final int assignment_expression = 40;
		public static final int expression = 41;
		public static final int expression_list = 42;
		public static final int expression_listopt = 43;
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

		while (lapg_m[lapg_head].state != 77) {
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

		if (lapg_m[lapg_head].state != 77) {
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
