/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.tool.parser.action;

import java.io.IOException;
import java.text.MessageFormat;
import org.textmapper.tool.parser.action.SActionLexer.ErrorReporter;
import org.textmapper.tool.parser.action.SActionLexer.Span;
import org.textmapper.tool.parser.action.SActionLexer.Tokens;

public class SActionParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public SActionParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] tmAction = SActionLexer.unpack_int(11,
		"\uffff\uffff\ufffd\uffff\ufff7\uffff\ufff1\uffff\2\0\uffff\uffff\uffff\uffff\1\0" +
		"\0\0\3\0\ufffe\uffff");

	private static final short[] tmLalr = SActionLexer.unpack_short(18,
		"\1\uffff\3\5\uffff\ufffe\1\uffff\3\5\uffff\ufffe\1\uffff\3\4\uffff\ufffe");

	private static final short[] lapg_sym_goto = SActionLexer.unpack_short(9,
		"\0\0\4\4\6\7\11\14\16");

	private static final short[] lapg_sym_from = SActionLexer.unpack_short(14,
		"\0\1\2\3\5\6\0\1\2\1\2\3\1\2");

	private static final short[] lapg_sym_to = SActionLexer.unpack_short(14,
		"\1\2\2\2\10\11\12\3\3\4\4\7\5\6");

	private static final short[] tmRuleLen = SActionLexer.unpack_short(6,
		"\3\2\1\3\1\0");

	private static final short[] tmRuleSymbol = SActionLexer.unpack_short(6,
		"\4\5\5\6\7\7");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"'{'",
		"_skip",
		"'}'",
		"javaaction",
		"command_tokens",
		"command_token",
		"command_tokensopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int javaaction = 4;
		static final int command_tokens = 5;
		static final int command_token = 6;
		static final int command_tokensopt = 7;
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
			if (symbol == Tokens.Unavailable_) {
				return -3 - state;
			}
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
	protected SActionLexer tmLexer;

	public Object parse(SActionLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;

		tmStack[0] = new Span();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 10) {
			int action = tmAction(tmStack[tmHead].state, tmNext == null ? Tokens.Unavailable_ : tmNext.symbol);
			if (action <= -3 && tmNext == null) {
				tmNext = tmLexer.next();
				action = tmAction(tmStack[tmHead].state, tmNext.symbol);
			}

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				break;
			}
		}

		if (tmStack[tmHead].state != 10) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext == null ? tmLexer.getLine() : tmNext.line, tmNext == null ? tmLexer.getOffset() : tmNext.offset);
			throw new ParseException();
		}
		return tmStack[tmHead].value;
	}

	protected void shift() throws IOException {
		if (tmNext == null) {
			tmNext = tmLexer.next();
		}
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.tokenText()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = null;
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
		left.line = startsym == null ? tmLexer.getLine() : startsym.line;
		left.offset = startsym == null ? tmLexer.getOffset() : startsym.offset;
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
