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
package org.textmapper.tool.test.bootstrap.a;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.tool.test.bootstrap.a.SampleALexer.ErrorReporter;
import org.textmapper.tool.test.bootstrap.a.SampleALexer.LapgSymbol;
import org.textmapper.tool.test.bootstrap.a.SampleALexer.Lexems;
import org.textmapper.tool.test.bootstrap.a.ast.AstClassdef;
import org.textmapper.tool.test.bootstrap.a.ast.IAstClassdefNoEoi;

public class SampleAParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public SampleAParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] lapg_action = SampleALexer.unpack_int(15,
		"\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\ufffd\uffff\6\0\4\0\ufff5\uffff" +
		"\uffff\uffff\5\0\3\0\ufffe\uffff\uffff\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = SampleALexer.unpack_short(14,
		"\3\uffff\6\uffff\5\1\uffff\ufffe\3\uffff\5\2\uffff\ufffe");

	private static final short[] lapg_sym_goto = SampleALexer.unpack_short(12,
		"\0\1\2\2\6\7\10\11\12\16\17\20");

	private static final short[] lapg_sym_from = SampleALexer.unpack_short(16,
		"\15\2\0\1\5\10\4\11\5\0\0\1\5\10\5\5");

	private static final short[] lapg_sym_to = SampleALexer.unpack_short(16,
		"\16\4\2\2\2\2\5\13\6\14\3\15\7\12\10\11");

	private static final short[] lapg_rlen = SampleALexer.unpack_short(7,
		"\1\0\1\5\1\2\1");

	private static final short[] lapg_rlex = SampleALexer.unpack_short(7,
		"\7\12\12\10\11\11\11");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"_skip",
		"Lclass",
		"'{'",
		"'}'",
		"error",
		"classdef_NoEoi",
		"classdef",
		"classdeflist",
		"classdeflistopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int classdef_NoEoi = 7;
		public static final int classdef = 8;
		public static final int classdeflist = 9;
		public static final int classdeflistopt = 10;
	}

	protected final int lapg_next(int state) throws IOException {
		int p;
		if (lapg_action[state] < -2) {
			if (lapg_n == null) {
				lapg_n = lapg_lexer.next();
			}
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.symbol) {
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
	protected LapgSymbol[] lapg_m;
	protected LapgSymbol lapg_n;
	protected SampleALexer lapg_lexer;

	private Object parse(SampleALexer lexer, int initialState, int finalState, boolean noEoi) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
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
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
								MessageFormat.format("syntax error before line {0}, column {1}",
								lapg_lexer.getTokenLine(), lapg_n.column));
					}
					if (lapg_symbols_ok <= 1) {
						lapg_n = lapg_lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (lapg_head < 0) {
					lapg_head = 0;
					lapg_m[0] = new LapgSymbol();
					lapg_m[0].state = initialState;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n == null ? lapg_lexer.getOffset() : lapg_n.offset, lapg_n == null ? lapg_lexer.getOffset() : lapg_n.endoffset, lapg_n == null ? lapg_lexer.getLine() : lapg_n.line,
						MessageFormat.format("syntax error before line {0}, column {1}",
								lapg_lexer.getTokenLine(), lapg_n == null ? lapg_lexer.getColumn() : lapg_n.column));
			}
			throw new ParseException();
		}
		return lapg_m[noEoi ? lapg_head : lapg_head - 1].value;
	}

	protected boolean restore() throws IOException {
		if (lapg_n == null) {
			lapg_n = lapg_lexer.next();
		}
		if (lapg_n.symbol == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 6) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].symbol = 6;
			lapg_m[lapg_head].value = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 6);
			lapg_m[lapg_head].line = lapg_n.line;
			lapg_m[lapg_head].column = lapg_n.column;
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endline = lapg_n.endline;
			lapg_m[lapg_head].endcolumn = lapg_n.endcolumn;
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
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.symbol], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.symbol != 0) {
			lapg_n = lazy ? null : lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.value = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].value : null;
		lapg_gg.symbol = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.line = startsym == null ? lapg_lexer.getLine() : startsym.line;
		lapg_gg.column = startsym == null ? lapg_lexer.getColumn() : startsym.column;
		lapg_gg.offset = startsym == null ? lapg_lexer.getOffset() : startsym.offset;
		lapg_gg.endline = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endline : lapg_n == null ? lapg_lexer.getLine() : lapg_n.line;
		lapg_gg.endcolumn = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endcolumn : lapg_n == null ? lapg_lexer.getColumn() : lapg_n.column;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n == null ? lapg_lexer.getOffset() : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 3:  // classdef ::= Lclass identifier '{' classdeflistopt '}'
				lapg_gg.value = new AstClassdef(
						((String)lapg_m[lapg_head - 3].value) /* identifier */,
						((List<AstClassdef>)lapg_m[lapg_head - 1].value) /* classdeflistopt */,
						null /* input */, lapg_m[lapg_head - 4].offset, lapg_m[lapg_head].endoffset);
				break;
			case 4:  // classdeflist ::= classdef
				lapg_gg.value = new ArrayList();
				((List<AstClassdef>)lapg_gg.value).add(((AstClassdef)lapg_m[lapg_head].value));
				break;
			case 5:  // classdeflist ::= classdeflist classdef
				((List<AstClassdef>)lapg_m[lapg_head - 1].value).add(((AstClassdef)lapg_m[lapg_head].value));
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol value) {
	}

	public IAstClassdefNoEoi parseClassdef_NoEoi(SampleALexer lexer) throws IOException, ParseException {
		return (IAstClassdefNoEoi) parse(lexer, 0, 12, true);
	}

	public AstClassdef parseClassdef(SampleALexer lexer) throws IOException, ParseException {
		return (AstClassdef) parse(lexer, 1, 14, false);
	}
}
