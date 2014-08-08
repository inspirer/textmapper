package org.textmapper.tool.test.bootstrap.set;

import java.io.IOException;
import java.text.MessageFormat;
import org.textmapper.tool.test.bootstrap.set.SetLexer.ErrorReporter;
import org.textmapper.tool.test.bootstrap.set.SetLexer.LapgSymbol;
import org.textmapper.tool.test.bootstrap.set.SetLexer.Tokens;

public class SetParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public SetParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] tmAction = SetLexer.unpack_int(28,
		"\uffff\uffff\13\0\14\0\15\0\12\0\ufffd\uffff\uffff\uffff\11\0\16\0\17\0\20\0\21\0" +
		"\22\0\23\0\uffff\uffff\24\0\uffff\uffff\25\0\26\0\1\0\uffff\uffff\27\0\30\0\31\0" +
		"\32\0\5\0\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = SetLexer.unpack_short(10,
		"\1\uffff\3\uffff\5\uffff\0\0\uffff\ufffe");

	private static final short[] lapg_sym_goto = SetLexer.unpack_short(23,
		"\0\1\5\10\13\14\17\20\20\22\24\25\27\27\30\30\30\31\33\34\35\36\37");

	private static final short[] lapg_sym_from = SetLexer.unpack_short(31,
		"\32\0\5\6\24\6\20\24\0\5\6\6\0\5\6\6\16\24\20\24\0\0\5\20\0\0\5\6\16\20\24");

	private static final short[] lapg_sym_to = SetLexer.unpack_short(31,
		"\33\1\1\10\25\11\21\26\2\2\12\13\3\3\14\15\17\27\22\30\32\4\7\23\5\6\6\16\20\24\31");

	private static final short[] tmRuleLen = SetLexer.unpack_short(27,
		"\1\4\2\2\2\2\2\1\4\2\1\1\1\1\1\1\1\1\1\1\1\1\1\1\1\1\1");

	private static final short[] tmRuleSymbol = SetLexer.unpack_short(27,
		"\12\13\14\14\14\15\16\16\17\20\20\21\21\21\22\22\22\22\22\22\23\24\24\25\25\25\25");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"'a'",
		"'b'",
		"'c'",
		"'d'",
		"'e'",
		"'f'",
		"'g'",
		"'h'",
		"'i'",
		"input",
		"abcdef",
		"pair",
		"test2",
		"recursive",
		"helper",
		"abcdef_list",
		"setof_first_pair",
		"setof_pair",
		"setof_'h'",
		"setof_first_recursive",
		"setof_recursive",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int input = 10;
		static final int abcdef = 11;
		static final int pair = 12;
		static final int test2 = 13;
		static final int recursive = 14;
		static final int helper = 15;
		static final int abcdef_list = 16;
		static final int setof_first_pair = 17;
		static final int setof_pair = 18;
		static final int setof_ApostrophehApostrophe = 19;
		static final int setof_first_recursive = 20;
		static final int setof_recursive = 21;
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
	protected SetLexer tmLexer;

	public Object parse(SetLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new LapgSymbol[1024];
		tmHead = 0;

		tmStack[0] = new LapgSymbol();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 27) {
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

		if (tmStack[tmHead].state != 27) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
			throw new ParseException();
		}
		return tmStack[tmHead - 1].value;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol tmLeft = new LapgSymbol();
		tmLeft.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		tmLeft.symbol = tmRuleSymbol[rule];
		tmLeft.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		LapgSymbol startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		tmLeft.line = startsym.line;
		tmLeft.offset = startsym.offset;
		tmLeft.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(tmLeft, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = tmLeft;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmLeft.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol tmLeft, int tmRule, int tmLength) {
	}
}
