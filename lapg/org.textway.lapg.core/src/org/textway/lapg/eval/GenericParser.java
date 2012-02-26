/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.eval;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.lapg.api.Grammar;
import org.textway.lapg.eval.GenericLexer.ErrorReporter;
import org.textway.lapg.eval.GenericLexer.ParseSymbol;
import org.textway.lapg.eval.GenericParseContext.TextSource;
import org.textway.lapg.lalr.ParserTables;

public class GenericParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	protected TextSource source;
	protected final Grammar grammar;

	private final int[] lapg_action;
	private final short[] lapg_lalr;
	private final short[] lapg_sym_goto;
	private final short[] lapg_sym_from;
	private final short[] lapg_sym_to;
	private final int[] lapg_rlen;
	private final int[] lapg_rlex;

	private final boolean debugSyntax;

	public GenericParser(ErrorReporter reporter, ParserTables tables, Grammar grammar, boolean debugSyntax) {
		this.reporter = reporter;
		this.grammar = grammar;
		this.lapg_action = tables.action_index;
		this.lapg_lalr = tables.action_table;
		this.lapg_sym_goto = tables.sym_goto;
		this.lapg_sym_to = tables.sym_to;
		this.lapg_sym_from = tables.sym_from;
		this.lapg_rlen = tables.getRuleLength();
		this.lapg_rlex = tables.rleft;
		this.debugSyntax = debugSyntax;
	}

	protected final int lapg_next(int state) throws IOException {
		int p;
		if (lapg_action[state] < -2) {
			if (lapg_n == null) {
				lapg_n = lapg_lexer.next();
			}
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final int lapg_state_sym(int state, int symbol) {
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
	protected ParseSymbol[] lapg_m;
	protected ParseSymbol lapg_n;
	protected GenericLexer lapg_lexer;

	public Object parse(GenericLexer lexer, int initialState, int finalState, boolean noEoi) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new ParseSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new ParseSymbol();
		lapg_m[0].state = initialState;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != finalState) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift(noEoi);
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				if (grammar.getError() == null) {
					break;
				}
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
								MessageFormat.format("syntax error before line {0}", lapg_lexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
						lapg_n = lapg_lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (lapg_head < 0) {
					lapg_head = 0;
					lapg_m[0] = new ParseSymbol();
					lapg_m[0].state = initialState;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n == null ? lapg_lexer.getOffset() : lapg_n.offset, lapg_n == null ? lapg_lexer.getOffset() : lapg_n.endoffset, lapg_n == null ? lapg_lexer.getLine() : lapg_n.line,
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return lapg_m[noEoi ? lapg_head : lapg_head - 1].sym;
	}

	protected boolean restore() throws IOException {
		if (lapg_n == null) {
			lapg_n = lapg_lexer.next();
		}
		if (lapg_n.lexem == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, grammar.getError().getIndex()) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new ParseSymbol();
			lapg_m[lapg_head].lexem = grammar.getError().getIndex();
			lapg_m[lapg_head].sym = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, grammar.getError().getIndex());
			lapg_m[lapg_head].line = lapg_n.line;
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endoffset = lapg_n.endoffset;
			return true;
		}
		return false;
	}

	protected void shift(boolean lazy) throws IOException {
		if (lapg_n == null) {
			lapg_n = lapg_lexer.next();
		}
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (debugSyntax) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", grammar.getSymbols()[lapg_n.lexem].getName(), lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lazy ? null : lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		ParseSymbol lapg_gg = new ParseSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (debugSyntax) {
			System.out.println("reduce to " + grammar.getSymbols()[lapg_rlex[rule]].getName());
		}
		ParseSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.line = startsym == null ? lapg_lexer.getLine() : startsym.line;
		lapg_gg.offset = startsym == null ? lapg_lexer.getOffset() : startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n == null ? lapg_lexer.getOffset() : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			cleanup(lapg_m[lapg_head]);
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	protected void applyRule(ParseSymbol lapg_gg, int rule, int ruleLength) {
		if (ruleLength == 1) {
			Object right = lapg_m[lapg_head].sym;
			if (right instanceof GenericNode) {
				lapg_gg.sym = right;
			} else {
				lapg_gg.sym = new GenericNode(source, lapg_gg.offset, lapg_gg.endoffset);
			}
		} else if (ruleLength > 1) {
			List<GenericNode> children = new ArrayList<GenericNode>(ruleLength);
			for (int i = ruleLength - 1; i >= 0; i--) {
				if (lapg_m[lapg_head - i].sym instanceof GenericNode) {
					children.add((GenericNode) lapg_m[lapg_head - i].sym);
				}
			}
			lapg_gg.sym = new GenericNode(source, lapg_gg.offset, lapg_gg.endoffset, children.toArray(new GenericNode[children.size()]));
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(ParseSymbol sym) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(ParseSymbol sym) {
	}
}
