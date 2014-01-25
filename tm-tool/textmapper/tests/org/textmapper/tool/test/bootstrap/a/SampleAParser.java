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
import org.textmapper.tool.test.bootstrap.a.ast.AstClassdeflistItem;
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
	private static final int[] tmAction = SampleALexer.unpack_int(15,
		"\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\ufffd\uffff\6\0\4\0\ufff5\uffff" +
		"\uffff\uffff\5\0\3\0\ufffe\uffff\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = SampleALexer.unpack_short(14,
		"\3\uffff\6\uffff\5\2\uffff\ufffe\3\uffff\5\1\uffff\ufffe");

	private static final short[] lapg_sym_goto = SampleALexer.unpack_short(12,
		"\0\1\2\2\6\7\10\11\12\16\17\20");

	private static final short[] lapg_sym_from = SampleALexer.unpack_short(16,
		"\15\2\0\1\5\10\4\11\5\0\0\1\5\10\5\5");

	private static final short[] lapg_sym_to = SampleALexer.unpack_short(16,
		"\16\4\2\2\2\2\5\13\6\14\3\15\7\12\10\11");

	private static final short[] lapg_rlen = SampleALexer.unpack_short(7,
		"\1\1\0\5\1\2\1");

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

	/**
	 * -3-n   Lookahead (state id)
	 * -2     Error
	 * -1     Shift
	 * 0..n   Reduce (rule index)
	 */
	protected static int tmAction(int state, int symbol) {
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
	protected SampleALexer tmLexer;

	private Object parse(SampleALexer lexer, int initialState, int finalState, boolean noEoi) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new LapgSymbol[1024];
		tmHead = 0;
		int lapg_symbols_ok = 4;

		tmStack[0] = new LapgSymbol();
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
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(MessageFormat.format("syntax error before line {0}, column {1}",
								tmLexer.getTokenLine(), tmNext.column), tmNext.line, tmNext.offset, tmNext.column, tmNext.endline, tmNext.endoffset, tmNext.endcolumn);
					}
					if (lapg_symbols_ok <= 1) {
						tmNext = tmLexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (tmHead < 0) {
					tmHead = 0;
					tmStack[0] = new LapgSymbol();
					tmStack[0].state = initialState;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(MessageFormat.format("syntax error before line {0}, column {1}",
								tmLexer.getTokenLine(), tmNext == null ? tmLexer.getColumn() : tmNext.column), tmNext == null ? tmLexer.getLine() : tmNext.line, tmNext == null ? tmLexer.getOffset() : tmNext.offset, tmNext == null ? tmLexer.getColumn() : tmNext.column, tmNext == null ? tmLexer.getLine() : tmNext.endline, tmNext == null ? tmLexer.getOffset() : tmNext.endoffset, tmNext == null ? tmLexer.getColumn() : tmNext.endcolumn);
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
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 6) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new LapgSymbol();
			tmStack[tmHead].symbol = 6;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 6);
			tmStack[tmHead].line = tmNext.line;
			tmStack[tmHead].offset = tmNext.offset;
			tmStack[tmHead].column = tmNext.column;
			tmStack[tmHead].endline = tmNext.endline;
			tmStack[tmHead].endoffset = tmNext.endoffset;
			tmStack[tmHead].endcolumn = tmNext.endcolumn;
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
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[tmNext.symbol], tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = lazy ? null : tmLexer.next();
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
		lapg_gg.line = startsym == null ? tmLexer.getLine() : startsym.line;
		lapg_gg.column = startsym == null ? tmLexer.getColumn() : startsym.column;
		lapg_gg.offset = startsym == null ? tmLexer.getOffset() : startsym.offset;
		lapg_gg.endline = (lapg_rlen[rule] != 0) ? tmStack[tmHead].endline : tmNext == null ? tmLexer.getLine() : tmNext.line;
		lapg_gg.endcolumn = (lapg_rlen[rule] != 0) ? tmStack[tmHead].endcolumn : tmNext == null ? tmLexer.getColumn() : tmNext.column;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext == null ? tmLexer.getOffset() : tmNext.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = lapg_gg;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, lapg_gg.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 3:  // classdef ::= Lclass identifier '{' classdeflistopt '}'
				lapg_gg.value = new AstClassdef(
						((String)tmStack[tmHead - 3].value) /* identifier */,
						((List<AstClassdeflistItem>)tmStack[tmHead - 1].value) /* classdeflist */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead - 4].column, tmStack[tmHead].endline, tmStack[tmHead].endoffset, tmStack[tmHead].endcolumn);
				break;
			case 4:  // classdeflist ::= classdef
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						((AstClassdef)tmStack[tmHead].value) /* classdef */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].column, tmStack[tmHead].endline, tmStack[tmHead].endoffset, tmStack[tmHead].endcolumn));
				break;
			case 5:  // classdeflist ::= classdeflist classdef
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						((AstClassdef)tmStack[tmHead].value) /* classdef */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead - 1].column, tmStack[tmHead].endline, tmStack[tmHead].endoffset, tmStack[tmHead].endcolumn));
				break;
			case 6:  // classdeflist ::= error
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						null /* classdef */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].column, tmStack[tmHead].endline, tmStack[tmHead].endoffset, tmStack[tmHead].endcolumn));
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
