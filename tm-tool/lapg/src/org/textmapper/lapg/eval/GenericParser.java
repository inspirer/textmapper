/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.lapg.eval;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.ParserData;
import org.textmapper.lapg.eval.GenericLexer.ErrorReporter;
import org.textmapper.lapg.eval.GenericLexer.Span;
import org.textmapper.lapg.eval.GenericLexer.Tokens;
import org.textmapper.lapg.eval.GenericParseContext.TextSource;

public class GenericParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	protected TextSource source;
	protected final Grammar grammar;

	private final int[] tmAction;
	private final int[] tmLalr;
	private final int[] lapg_sym_goto;
	private final int[] lapg_sym_from;
	private final int[] lapg_sym_to;
	private final int[] tmRuleLen;
	private final int[] tmRuleSymbol;

	private final boolean debugSyntax;

	public GenericParser(ErrorReporter reporter, ParserData tables, Grammar grammar, boolean debugSyntax) {
		this.reporter = reporter;
		this.grammar = grammar;
		this.tmAction = tables.getAction();
		this.tmLalr = tables.getLalr();
		this.lapg_sym_goto = tables.getSymGoto();
		this.lapg_sym_to = tables.getSymTo();
		this.lapg_sym_from = tables.getSymFrom();
		this.tmRuleLen = tables.getRuleLength();
		this.tmRuleSymbol = tables.getLeft();
		this.debugSyntax = debugSyntax;
	}

	/**
	 * -3-n   Lookahead (state id)
	 * -2     Error
	 * -1     Shift
	 * 0..n   Reduce (rule index)
	 */
	protected final int tmAction(int state, int symbol) {
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

	protected final int tmGoto(int state, int symbol) {
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
	protected GenericLexer tmLexer;

	public Object parse(GenericLexer lexer, int initialState, int finalState, boolean noEoi) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;
		int tmShiftsAfterError = 4;

		tmStack[0] = new Span();
		tmStack[0].state = initialState;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != finalState) {
			int action = tmAction(tmStack[tmHead].state, tmNext == null ? Tokens.Unavailable_ : tmNext.symbol);
			if (action <= -3 && tmNext == null) {
				tmNext = tmLexer.next();
				action = tmAction(tmStack[tmHead].state, tmNext.symbol);
			}

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift(noEoi);
				tmShiftsAfterError++;
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				if (grammar.getError() == null) {
					break;
				}
				if (restore()) {
					if (tmShiftsAfterError >= 4) {
						reporter.error(MessageFormat.format("syntax error before line {0}", tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
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
					tmStack[0].state = initialState;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != finalState) {
			if (tmShiftsAfterError >= 4) {
				reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext == null ? tmLexer.getLine() : tmNext.line, tmNext == null ? tmLexer.getOffset() : tmNext.offset, tmNext == null ? tmLexer.getOffset() : tmNext.endoffset);
			}
			throw new ParseException();
		}
		return tmStack[noEoi ? tmHead : tmHead - 1].value;
	}

	protected boolean restore() throws IOException {
		if (tmNext == null) {
			tmNext = tmLexer.next();
		}
		if (tmNext.symbol == 0) {
			return false;
		}
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, grammar.getError().getIndex()) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new Span();
			tmStack[tmHead].symbol = grammar.getError().getIndex();
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, grammar.getError().getIndex());
			tmStack[tmHead].line = tmNext.line;
			tmStack[tmHead].offset = tmNext.offset;
			tmStack[tmHead].endoffset = tmNext.endoffset;
			return true;
		}
		return false;
	}

	protected void shift(boolean lazy) throws IOException {
		if (tmNext == null) {
			tmNext = tmLexer.next();
		}
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (debugSyntax) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", grammar.getSymbols()[tmNext.symbol].getName(), tmLexer.tokenText()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = lazy ? null : tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		Span left = new Span();
		left.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		left.symbol = tmRuleSymbol[rule];
		left.state = 0;
		if (debugSyntax) {
			System.out.println("reduce to " + grammar.getSymbols()[tmRuleSymbol[rule]].getName());
		}
		Span startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		left.line = startsym == null ? tmLexer.getLine() : startsym.line;
		left.offset = startsym == null ? tmLexer.getOffset() : startsym.offset;
		left.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext == null ? tmLexer.getOffset() : tmNext.offset;
		applyRule(left, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			cleanup(tmStack[tmHead]);
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = left;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, left.symbol);
	}

	protected void applyRule(Span tmLeft, int rule, int ruleLength) {
		if (ruleLength == 1) {
			Object right = tmStack[tmHead].value;
			if (right instanceof GenericNode) {
				tmLeft.value = right;
			} else {
				tmLeft.value = new GenericNode(source, tmLeft.offset, tmLeft.endoffset);
			}
		} else if (ruleLength > 1) {
			List<GenericNode> children = new ArrayList<>(ruleLength);
			for (int i = ruleLength - 1; i >= 0; i--) {
				if (tmStack[tmHead - i].value instanceof GenericNode) {
					children.add((GenericNode) tmStack[tmHead - i].value);
				}
			}
			tmLeft.value = new GenericNode(source, tmLeft.offset, tmLeft.endoffset, children.toArray(new GenericNode[children.size()]));
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(Span value) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(Span value) {
	}
}
