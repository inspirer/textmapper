package org.textmapper.tool.bootstrap.set;

import java.io.IOException;
import java.text.MessageFormat;
import org.textmapper.tool.bootstrap.set.SetLexer.ErrorReporter;
import org.textmapper.tool.bootstrap.set.SetLexer.Span;
import org.textmapper.tool.bootstrap.set.SetLexer.Tokens;

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
	private static final int[] tmAction = SetLexer.unpack_int(38,
		"\uffff\uffff\5\0\6\0\7\0\uffff\uffff\1\0\uffff\uffff\ufffd\uffff\0\0\2\0\10\0\11" +
		"\0\12\0\13\0\14\0\15\0\uffff\uffff\ufff7\uffff\uffff\uffff\4\0\uffff\uffff\uffef" +
		"\uffff\37\0\35\0\uffff\uffff\21\0\22\0\uffff\uffff\3\0\34\0\33\0\23\0\24\0\25\0\26" +
		"\0\27\0\uffff\uffff\ufffe\uffff");

	private static final int[] tmLalr = SetLexer.unpack_int(20,
		"\2\0\uffff\uffff\13\0\36\0\uffff\uffff\ufffe\uffff\3\0\uffff\uffff\4\0\uffff\uffff" +
		"\13\0\40\0\uffff\uffff\ufffe\uffff\4\0\uffff\uffff\13\0\40\0\uffff\uffff\ufffe\uffff");

	private static final int[] lapg_sym_goto = SetLexer.unpack_int(30,
		"\0\0\1\0\5\0\11\0\15\0\20\0\23\0\24\0\24\0\26\0\30\0\31\0\32\0\33\0\33\0\34\0\35" +
		"\0\37\0\40\0\42\0\43\0\43\0\44\0\45\0\46\0\46\0\46\0\47\0\50\0\52\0");

	private static final int[] lapg_sym_from = SetLexer.unpack_int(42,
		"\44\0\0\0\4\0\6\0\33\0\6\0\7\0\24\0\33\0\0\0\4\0\6\0\21\0\6\0\21\0\25\0\0\0\4\0\6" +
		"\0\6\0\20\0\33\0\24\0\33\0\4\0\22\0\30\0\0\0\0\0\0\0\4\0\20\0\0\0\4\0\6\0\24\0\33" +
		"\0\24\0\4\0\7\0\21\0\25\0");

	private static final int[] lapg_sym_to = SetLexer.unpack_int(42,
		"\45\0\1\0\1\0\12\0\37\0\13\0\21\0\31\0\40\0\2\0\2\0\14\0\25\0\15\0\26\0\26\0\3\0" +
		"\3\0\16\0\17\0\23\0\41\0\32\0\42\0\7\0\30\0\36\0\4\0\44\0\5\0\10\0\24\0\6\0\6\0\20" +
		"\0\33\0\43\0\34\0\11\0\22\0\27\0\35\0");

	private static final int[] tmRuleLen = SetLexer.unpack_int(33,
		"\2\0\1\0\2\0\4\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\2\0\2\0\2\0\1\0\1\0\1\0" +
		"\1\0\1\0\1\0\2\0\2\0\1\0\4\0\4\0\3\0\2\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = SetLexer.unpack_int(33,
		"\16\0\16\0\17\0\20\0\21\0\22\0\22\0\22\0\23\0\23\0\23\0\23\0\23\0\23\0\24\0\24\0" +
		"\24\0\25\0\25\0\26\0\26\0\26\0\26\0\27\0\30\0\30\0\31\0\32\0\33\0\33\0\33\0\34\0" +
		"\34\0");

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
		"'3'",
		"'4'",
		"'5'",
		"'6'",
		"abcdef_list",
		"input",
		"abcdef",
		"setof_'h'",
		"setof_first_pair",
		"setof_pair",
		"pair",
		"setof_first_recursive",
		"setof_recursive",
		"test2",
		"recursive",
		"helper",
		"test_3",
		"test_3_helper",
		"maybeD",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int abcdef_list = 14;
		int input = 15;
		int abcdef = 16;
		int setof_ApostrophehApostrophe = 17;
		int setof_first_pair = 18;
		int setof_pair = 19;
		int pair = 20;
		int setof_first_recursive = 21;
		int setof_recursive = 22;
		int test2 = 23;
		int recursive = 24;
		int helper = 25;
		int test_3 = 26;
		int test_3_helper = 27;
		int maybeD = 28;
	}

	// set(precede '4')
	private static int[] precede4 = {
		2, 3, 4, 10
	};

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
	protected Span[] tmStack;
	protected Span tmNext;
	protected SetLexer tmLexer;

	public Object parse(SetLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;

		tmStack[0] = new Span();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 37) {
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

		if (tmStack[tmHead].state != 37) {
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
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.tokenText()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		Span left = new Span();
		left.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		left.symbol = tmRuleSymbol[rule];
		left.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		Span startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		left.line = startsym.line;
		left.offset = startsym.offset;
		left.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(left, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = left;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
	}
}
