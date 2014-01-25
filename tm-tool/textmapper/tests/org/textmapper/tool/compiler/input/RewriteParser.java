package org.textmapper.tool.compiler.input;

import java.io.IOException;
import java.text.MessageFormat;
import org.textmapper.tool.compiler.input.RewriteLexer.ErrorReporter;
import org.textmapper.tool.compiler.input.RewriteLexer.LapgSymbol;
import org.textmapper.tool.compiler.input.RewriteLexer.Lexems;

public class RewriteParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public RewriteParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] tmAction = RewriteLexer.unpack_int(3,
		"\ufffd\uffff\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = RewriteLexer.unpack_short(4,
		"\0\0\uffff\ufffe");

	private static final short[] lapg_sym_goto = RewriteLexer.unpack_short(29,
		"\0\1\1\1\1\1\1\1\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2");

	private static final short[] lapg_sym_from = RewriteLexer.unpack_short(2,
		"\1\0");

	private static final short[] lapg_sym_to = RewriteLexer.unpack_short(2,
		"\2\1");

	private static final short[] lapg_rlen = RewriteLexer.unpack_short(51,
		"\0\1\1\1\1\1\2\1\2\1\3\1\3\1\4\1\4\4\4\1\1\1\4\4\4\0\1\2\0\2\2\0\2\0\2\0\1\2\1\4" +
		"\0\4\0\1\1\0\2\0\1\2\1");

	private static final short[] lapg_rlex = RewriteLexer.unpack_short(51,
		"\7\10\10\11\12\13\13\14\14\15\15\16\16\17\17\20\20\20\20\21\21\21\21\21\21\22\22" +
		"\22\23\23\23\24\24\25\25\26\26\26\27\27\30\30\31\31\33\33\31\32\32\32\32");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"'{'",
		"'}'",
		"','",
		"'.'",
		"'a'",
		"'b'",
		"input",
		"Elem",
		"Elem2",
		"Elem3",
		"ElemPlus1",
		"ElemPlus1rr",
		"ElemPlus2",
		"ElemPlus2rr",
		"ElemPlus3",
		"ElemPlus4",
		"ElemPlus5",
		"ElemStar1",
		"ElemStar1ex",
		"ElemStar2",
		"ElemStar3rr",
		"ElemStar4rr",
		"ElemPlus5rr",
		"ElemSep6rr",
		"EStar1",
		"EStar2",
		"EStar1$1",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 7;
		public static final int Elem = 8;
		public static final int Elem2 = 9;
		public static final int Elem3 = 10;
		public static final int ElemPlus1 = 11;
		public static final int ElemPlus1rr = 12;
		public static final int ElemPlus2 = 13;
		public static final int ElemPlus2rr = 14;
		public static final int ElemPlus3 = 15;
		public static final int ElemPlus4 = 16;
		public static final int ElemPlus5 = 17;
		public static final int ElemStar1 = 18;
		public static final int ElemStar1ex = 19;
		public static final int ElemStar2 = 20;
		public static final int ElemStar3rr = 21;
		public static final int ElemStar4rr = 22;
		public static final int ElemPlus5rr = 23;
		public static final int ElemSep6rr = 24;
		public static final int EStar1 = 25;
		public static final int EStar2 = 26;
		public static final int EStar1DOLLAR1 = 27;
	}

	/**
	 * -3-n   Lookahead (state id)
	 * -2     Error
	 * -1     Shift
	 * 0..n   Reduce (rule index)
	 */
	protected static int tmAction(int state, int symbol) {
		int p;
		if (tmAction[state] < -2) {
			for (p = -tmAction[state] - 3; tmLalr[p] >= 0; p += 2) {
				if (tmLalr[p] == symbol) {
					break;
				}
			}
			return tmLalr[p + 1];
		}
		return tmAction[state];
	}

	protected static int tmGoto(int state, int symbol) {
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

	protected int tmHead;
	protected LapgSymbol[] tmStack;
	protected LapgSymbol tmNext;
	protected RewriteLexer tmLexer;

	public Object parse(RewriteLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new LapgSymbol[1024];
		tmHead = 0;

		tmStack[0] = new LapgSymbol();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 2) {
			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				break;
			}
		}

		if (tmStack[tmHead].state != 2) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset);
			throw new ParseException();
		}
		return tmStack[tmHead - 1].value;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[tmNext.symbol], tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.value = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]].value : null;
		lapg_gg.symbol = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]] : tmNext;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = lapg_gg;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, lapg_gg.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
	}
}
