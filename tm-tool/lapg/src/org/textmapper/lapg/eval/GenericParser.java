/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.lapg.eval.GenericLexer.Lexems;
import org.textmapper.lapg.eval.GenericLexer.ParseSymbol;
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
	private final short[] tmLalr;
	private final short[] lapg_sym_goto;
	private final short[] lapg_sym_from;
	private final short[] lapg_sym_to;
	private final int[] lapg_rlen;
	private final int[] lapg_rlex;

	private final boolean debugSyntax;

	public GenericParser(ErrorReporter reporter, ParserData tables, Grammar grammar, boolean debugSyntax) {
		this.reporter = reporter;
		this.grammar = grammar;
		this.tmAction = tables.getAction();
		this.tmLalr = tables.getLalr();
		this.lapg_sym_goto = tables.getSymGoto();
		this.lapg_sym_to = tables.getSymTo();
		this.lapg_sym_from = tables.getSymFrom();
		this.lapg_rlen = tables.getRuleLength();
		this.lapg_rlex = tables.getLeft();
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
			if (symbol == Lexems.Unavailable_) {
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
	protected ParseSymbol[] tmStack;
	protected ParseSymbol tmNext;
	protected GenericLexer tmLexer;

	public Object parse(GenericLexer lexer, int initialState, int finalState, boolean noEoi) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new ParseSymbol[1024];
		tmHead = 0;
		int lapg_symbols_ok = 4;

		tmStack[0] = new ParseSymbol();
		tmStack[0].state = initialState;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != finalState) {
			int action = tmAction(tmStack[tmHead].state, tmNext == null ? Lexems.Unavailable_ : tmNext.symbol);
			if (action <= -3 && tmNext == null) {
				tmNext = tmLexer.next();
				action = tmAction(tmStack[tmHead].state, tmNext.symbol);
			}

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift(noEoi);
				lapg_symbols_ok++;
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				if (grammar.getError() == null) {
					break;
				}
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(tmNext.offset, tmNext.endoffset, tmNext.line,
								MessageFormat.format("syntax error before line {0}", tmLexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
						tmNext = tmLexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (tmHead < 0) {
					tmHead = 0;
					tmStack[0] = new ParseSymbol();
					tmStack[0].state = initialState;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(tmNext == null ? tmLexer.getOffset() : tmNext.offset, tmNext == null ? tmLexer.getOffset() : tmNext.endoffset, tmNext == null ? tmLexer.getLine() : tmNext.line,
						MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()));
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
			tmStack[++tmHead] = new ParseSymbol();
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
			System.out.println(MessageFormat.format("shift: {0} ({1})", grammar.getSymbols()[tmNext.symbol].getName(), tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = lazy ? null : tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		ParseSymbol lapg_gg = new ParseSymbol();
		lapg_gg.value = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]].value : null;
		lapg_gg.symbol = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (debugSyntax) {
			System.out.println("reduce to " + grammar.getSymbols()[lapg_rlex[rule]].getName());
		}
		ParseSymbol startsym = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]] : tmNext;
		lapg_gg.line = startsym == null ? tmLexer.getLine() : startsym.line;
		lapg_gg.offset = startsym == null ? tmLexer.getOffset() : startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext == null ? tmLexer.getOffset() : tmNext.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			cleanup(tmStack[tmHead]);
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = lapg_gg;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, lapg_gg.symbol);
	}

	protected void applyRule(ParseSymbol lapg_gg, int rule, int ruleLength) {
		if (ruleLength == 1) {
			Object right = tmStack[tmHead].value;
			if (right instanceof GenericNode) {
				lapg_gg.value = right;
			} else {
				lapg_gg.value = new GenericNode(source, lapg_gg.offset, lapg_gg.endoffset);
			}
		} else if (ruleLength > 1) {
			List<GenericNode> children = new ArrayList<GenericNode>(ruleLength);
			for (int i = ruleLength - 1; i >= 0; i--) {
				if (tmStack[tmHead - i].value instanceof GenericNode) {
					children.add((GenericNode) tmStack[tmHead - i].value);
				}
			}
			lapg_gg.value = new GenericNode(source, lapg_gg.offset, lapg_gg.endoffset, children.toArray(new GenericNode[children.size()]));
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(ParseSymbol value) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(ParseSymbol value) {
	}
}
