package com.test;

import com.test.JsonLexer.ErrorReporter;
import com.test.JsonLexer.Span;
import com.test.JsonLexer.Tokens;
import java.io.IOException;
import java.text.MessageFormat;

public class JsonParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public JsonParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] tmAction = JsonLexer.unpack_int(28,
		"\uffff\uffff\uffff\uffff\uffff\uffff\6\0\7\0\1\0\2\0\3\0\0\0\4\0\5\0\11\0\uffff\uffff" +
		"\13\0\uffff\uffff\16\0\17\0\uffff\uffff\uffff\uffff\10\0\uffff\uffff\15\0\uffff\uffff" +
		"\12\0\14\0\20\0\uffff\uffff\ufffe\uffff");

	private static final short[] lapg_sym_goto = JsonLexer.unpack_short(21,
		"\0\1\5\7\13\15\16\20\20\26\32\36\42\46\47\53\57\61\62\66\67");

	private static final short[] lapg_sym_from = JsonLexer.unpack_short(55,
		"\32\0\2\22\26\1\16\0\2\22\26\2\21\14\16\21\0\1\2\22\24\26\0\2\22\26\0\2\22\26\0\2" +
		"\22\26\0\2\22\26\0\0\2\22\26\0\2\22\26\1\24\1\0\2\22\26\2");

	private static final short[] lapg_sym_to = JsonLexer.unpack_short(55,
		"\33\1\1\1\1\13\23\2\2\2\2\17\25\22\24\26\3\14\3\3\14\3\4\4\4\4\5\5\5\5\6\6\6\6\7" +
		"\7\7\7\32\10\20\27\31\11\11\11\11\15\30\16\12\12\12\12\21");

	private static final short[] tmRuleLen = JsonLexer.unpack_short(17,
		"\1\1\1\1\1\1\1\1\3\2\3\1\3\3\2\1\3");

	private static final short[] tmRuleSymbol = JsonLexer.unpack_short(17,
		"\15\16\16\16\16\16\16\16\17\17\20\21\21\22\22\23\23");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"'{'",
		"'}'",
		"'['",
		"']'",
		"':'",
		"','",
		"space",
		"JSONString",
		"JSONNumber",
		"'null'",
		"'true'",
		"'false'",
		"JSONText",
		"JSONValue",
		"JSONObject",
		"JSONMember",
		"JSONMemberList",
		"JSONArray",
		"JSONElementList",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int JSONText = 13;
		int JSONValue = 14;
		int JSONObject = 15;
		int JSONMember = 16;
		int JSONMemberList = 17;
		int JSONArray = 18;
		int JSONElementList = 19;
	}

	/**
	 * -3-n   Lookahead (state id)
	 * -2     Error
	 * -1     Shift
	 * 0..n   Reduce (rule index)
	 */
	protected static int tmAction(int state, int symbol) {
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
	protected JsonLexer tmLexer;

	public Object parse(JsonLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;

		tmStack[0] = new Span();
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
