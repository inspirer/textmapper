/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.tool.bootstrap.b;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.tool.bootstrap.b.SampleBLexer.ErrorReporter;
import org.textmapper.tool.bootstrap.b.SampleBLexer.Span;
import org.textmapper.tool.bootstrap.b.SampleBLexer.Tokens;
import org.textmapper.tool.bootstrap.b.ast.AstClassdef;
import org.textmapper.tool.bootstrap.b.ast.AstClassdeflistItem;
import org.textmapper.tool.bootstrap.b.ast.IAstClassdefNoEoi;

public class SampleBParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public SampleBParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] tmAction = SampleBLexer.unpack_int(24,
		"\uffff\uffff\uffff\uffff\0\0\3\0\uffff\uffff\uffff\uffff\ufffd\uffff\uffff\uffff" +
		"\uffff\uffff\10\0\4\0\ufff3\uffff\uffff\uffff\uffeb\uffff\uffff\uffff\uffff\uffff" +
		"\5\0\1\0\uffff\uffff\6\0\uffff\uffff\2\0\7\0\ufffe\uffff");

	private static final int[] tmLalr = SampleBLexer.unpack_int(28,
		"\1\0\uffff\uffff\3\0\uffff\uffff\13\0\uffff\uffff\6\0\12\0\uffff\uffff\ufffe\uffff" +
		"\1\0\uffff\uffff\3\0\uffff\uffff\6\0\11\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff" +
		"\3\0\uffff\uffff\13\0\uffff\uffff\6\0\12\0\uffff\uffff\ufffe\uffff");

	private static final int[] tmGoto = SampleBLexer.unpack_int(22,
		"\0\0\0\0\12\0\12\0\22\0\24\0\30\0\34\0\40\0\44\0\44\0\44\0\50\0\50\0\50\0\50\0\50" +
		"\0\52\0\62\0\64\0\70\0\74\0");

	private static final int[] tmFromTo = SampleBLexer.unpack_int(60,
		"\1\0\3\0\5\0\7\0\6\0\10\0\13\0\17\0\15\0\10\0\0\0\1\0\6\0\1\0\13\0\1\0\15\0\1\0\4" +
		"\0\5\0\4\0\6\0\7\0\15\0\14\0\21\0\22\0\25\0\10\0\16\0\17\0\24\0\16\0\23\0\24\0\26" +
		"\0\6\0\11\0\15\0\11\0\0\0\27\0\0\0\2\0\6\0\12\0\13\0\20\0\15\0\12\0\1\0\4\0\6\0\13" +
		"\0\15\0\13\0\6\0\14\0\15\0\22\0");

	private static final int[] tmRuleLen = SampleBLexer.unpack_int(11,
		"\1\0\5\0\7\0\1\0\1\0\2\0\3\0\4\0\1\0\1\0\0\0");

	private static final int[] tmRuleSymbol = SampleBLexer.unpack_int(11,
		"\20\0\21\0\21\0\22\0\23\0\23\0\23\0\23\0\23\0\24\0\24\0");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"identifier",
		"_skip",
		"Lclass",
		"Lextends",
		"'{'",
		"'}'",
		"'('",
		"')'",
		"Linterface",
		"Lenum",
		"error",
		"numeric",
		"octal",
		"decimal",
		"eleven",
		"classdef_no_eoi",
		"classdef",
		"ID",
		"classdeflist",
		"classdeflistopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int classdef_no_eoi = 16;
		int classdef = 17;
		int ID = 18;
		int classdeflist = 19;
		int classdeflistopt = 20;
	}

	// set(follow error)
	private static int[] afterErr = {
		1, 3, 6
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

	protected static int gotoState(int state, int symbol) {
		int min = tmGoto[symbol], max = tmGoto[symbol + 1];
		int i, e;

		while (min < max) {
			e = (min + max) >> 2 << 1;
			i = tmFromTo[e];
			if (i == state) {
				return tmFromTo[e+1];
			} else if (i < state) {
				min = e + 2;
			} else {
				max = e;
			}
		}
		return -1;
	}

	protected int tmHead;
	protected Span[] tmStack;
	protected Span tmNext;
	protected SampleBLexer tmLexer;

	public IAstClassdefNoEoi parse(SampleBLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;
		int tmShiftsAfterError = 4;

		tmStack[0] = new Span();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 23) {
			int action = tmAction(tmStack[tmHead].state, tmNext == null ? Tokens.Unavailable_ : tmNext.symbol);
			if (action <= -3 && tmNext == null) {
				tmNext = tmLexer.next();
				action = tmAction(tmStack[tmHead].state, tmNext.symbol);
			}

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
				tmShiftsAfterError++;
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				if (restore()) {
					if (tmShiftsAfterError >= 4) {
						reporter.error(MessageFormat.format("syntax error before line {0}", tmLexer.getTokenLine()), tmNext.offset, tmNext.endoffset);
					}
					if (tmShiftsAfterError <= 1) {
						tmNext = tmLexer.next();
					}
					tmShiftsAfterError = 0;
					continue;
				}
				if (tmHead < 0) {
					tmHead = 0;
					tmStack[0] = new Span();
					tmStack[0].state = 0;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != 23) {
			if (tmShiftsAfterError >= 4) {
				reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext == null ? tmLexer.getOffset() : tmNext.offset, tmNext == null ? tmLexer.getOffset() : tmNext.endoffset);
			}
			throw new ParseException();
		}
		return (IAstClassdefNoEoi)tmStack[tmHead].value;
	}

	protected boolean restore() throws IOException {
		if (tmNext == null) {
			tmNext = tmLexer.next();
		}
		if (tmNext.symbol == 0) {
			return false;
		}
		while (tmHead >= 0 && gotoState(tmStack[tmHead].state, 11) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new Span();
			tmStack[tmHead].symbol = 11;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, 11);
			tmStack[tmHead].offset = tmNext.offset;
			tmStack[tmHead].endoffset = tmNext.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		if (tmNext == null) {
			tmNext = tmLexer.next();
		}
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, tmNext.symbol);
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
		left.offset = startsym == null ? tmLexer.getOffset() : startsym.offset;
		left.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext == null ? tmLexer.getOffset() : tmNext.offset;
		applyRule(left, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = left;
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
		switch (ruleIndex) {
			case 1:  // classdef : Lclass ID '{' classdeflistopt '}'
				tmLeft.value = new AstClassdef(
						true /* tc */,
						((String)tmStack[tmHead - 3].value) /* ID */,
						((List<AstClassdeflistItem>)tmStack[tmHead - 1].value) /* classdeflist */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 2:  // classdef : Lclass ID Lextends identifier '{' classdeflistopt '}'
				tmLeft.value = new AstClassdef(
						true /* tc */,
						((String)tmStack[tmHead - 5].value) /* ID */,
						((List<AstClassdeflistItem>)tmStack[tmHead - 1].value) /* classdeflist */,
						((String)tmStack[tmHead - 3].value) /* identifier */,
						null /* input */, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 4:  // classdeflist : classdef
				tmLeft.value = new ArrayList();
				((List<AstClassdeflistItem>)tmLeft.value).add(new AstClassdeflistItem(
						((AstClassdef)tmStack[tmHead].value) /* classdef */,
						null /* identifier */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 5:  // classdeflist : classdeflist classdef
				((List<AstClassdeflistItem>)tmLeft.value).add(new AstClassdeflistItem(
						((AstClassdef)tmStack[tmHead].value) /* classdef */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 6:  // classdeflist : identifier '(' ')'
				tmLeft.value = new ArrayList();
				((List<AstClassdeflistItem>)tmLeft.value).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)tmStack[tmHead - 2].value) /* identifier */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 7:  // classdeflist : classdeflist identifier '(' ')'
				((List<AstClassdeflistItem>)tmLeft.value).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)tmStack[tmHead - 2].value) /* identifier */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset));
				break;
			case 8:  // classdeflist : error
				tmLeft.value = new ArrayList();
				((List<AstClassdeflistItem>)tmLeft.value).add(new AstClassdeflistItem(
						null /* classdef */,
						null /* identifier */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(Span value) {
	}
}
