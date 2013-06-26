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
package org.textmapper.tool.test.bootstrap.b;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.tool.test.bootstrap.b.SampleBLexer.ErrorReporter;
import org.textmapper.tool.test.bootstrap.b.SampleBLexer.LapgSymbol;
import org.textmapper.tool.test.bootstrap.b.SampleBLexer.Lexems;
import org.textmapper.tool.test.bootstrap.b.ast.AstClassdef;
import org.textmapper.tool.test.bootstrap.b.ast.AstClassdeflistItem;
import org.textmapper.tool.test.bootstrap.b.ast.IAstClassdefNoEoi;

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
	private static final int[] tmAction = SampleBLexer.unpack_int(26,
		"\uffff\uffff\uffff\uffff\0\0\5\0\uffff\uffff\uffff\uffff\ufffd\uffff\uffff\uffff" +
		"\uffff\uffff\13\0\6\0\uffef\uffff\uffff\uffff\uffe3\uffff\uffff\uffff\uffff\uffff" +
		"\7\0\3\0\uffff\uffff\uffff\uffff\10\0\uffff\uffff\4\0\11\0\12\0\ufffe\uffff");

	private static final short[] tmLalr = SampleBLexer.unpack_short(40,
		"\1\uffff\20\uffff\4\uffff\3\uffff\13\uffff\6\2\uffff\ufffe\1\uffff\20\uffff\4\uffff" +
		"\3\uffff\6\1\uffff\ufffe\1\uffff\20\uffff\4\uffff\3\uffff\13\uffff\6\2\uffff\ufffe");

	private static final short[] lapg_sym_goto = SampleBLexer.unpack_short(23,
		"\0\0\5\5\11\20\22\24\26\31\31\31\33\33\33\33\33\40\41\45\46\50\52");

	private static final short[] lapg_sym_from = SampleBLexer.unpack_short(42,
		"\1\5\6\13\15\0\6\13\15\1\4\5\6\13\15\16\4\7\14\22\10\17\16\23\25\6\15\1\5\6\13\15" +
		"\0\0\6\13\15\1\6\15\6\15");

	private static final short[] lapg_sym_to = SampleBLexer.unpack_short(42,
		"\3\7\10\17\10\1\1\1\1\3\5\7\10\17\10\23\6\15\21\26\16\25\24\27\30\11\11\3\7\10\17" +
		"\10\31\2\12\20\12\4\13\13\14\22");

	private static final short[] lapg_rlen = SampleBLexer.unpack_short(12,
		"\1\1\0\5\7\1\1\2\3\4\4\1");

	private static final short[] lapg_rlex = SampleBLexer.unpack_short(12,
		"\21\25\25\22\22\23\24\24\24\24\24\24");

	protected static final String[] lapg_syms = new String[] {
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
		"_skipSoftKW",
		"classdef_NoEoi",
		"classdef",
		"ID",
		"classdeflist",
		"classdeflistopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int classdef_NoEoi = 17;
		public static final int classdef = 18;
		public static final int ID = 19;
		public static final int classdeflist = 20;
		public static final int classdeflistopt = 21;
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
	protected SampleBLexer tmLexer;

	public IAstClassdefNoEoi parse(SampleBLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new LapgSymbol[1024];
		tmHead = 0;
		int lapg_symbols_ok = 4;

		tmStack[0] = new LapgSymbol();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 25) {
			int action = tmAction(tmStack[tmHead].state, tmNext == null ? Lexems.Unavailable_ : tmNext.symbol);
			if (action <= -3 && tmNext == null) {
				tmNext = tmLexer.next();
				action = tmAction(tmStack[tmHead].state, tmNext.symbol);
			}

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
				lapg_symbols_ok++;
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(tmNext.offset, tmNext.endoffset, tmLexer.getTokenLine(),
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
					tmStack[0] = new LapgSymbol();
					tmStack[0].state = 0;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != 25) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(tmNext == null ? tmLexer.getOffset() : tmNext.offset, tmNext == null ? tmLexer.getOffset() : tmNext.endoffset, tmNext == null ? tmLexer.getLine() : tmLexer.getTokenLine(),
						MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()));
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
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 11) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new LapgSymbol();
			tmStack[tmHead].symbol = 11;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 11);
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
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[tmNext.symbol], tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = null;
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
		lapg_gg.offset = startsym == null ? tmLexer.getOffset() : startsym.offset;
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
			case 3:  // classdef ::= Lclass ID '{' classdeflistopt '}'
				lapg_gg.value = new AstClassdef(
						true /* tc */,
						false /* te */,
						((String)tmStack[tmHead - 3].value) /* ID */,
						((List<AstClassdeflistItem>)tmStack[tmHead - 1].value) /* classdeflist */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 4:  // classdef ::= Lclass ID Lextends identifier '{' classdeflistopt '}'
				lapg_gg.value = new AstClassdef(
						true /* tc */,
						true /* te */,
						((String)tmStack[tmHead - 5].value) /* ID */,
						((List<AstClassdeflistItem>)tmStack[tmHead - 1].value) /* classdeflist */,
						((String)tmStack[tmHead - 3].value) /* identifier */,
						null /* input */, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 6:  // classdeflist ::= classdef
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						((AstClassdef)tmStack[tmHead].value) /* classdef */,
						null /* identifier */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 7:  // classdeflist ::= classdeflist classdef
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						((AstClassdef)tmStack[tmHead].value) /* classdef */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 8:  // classdeflist ::= identifier '(' ')'
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)tmStack[tmHead - 2].value) /* identifier */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 9:  // classdeflist ::= identifier '(' Lextends ')'
				 String s = /* should be string */ ((String)tmStack[tmHead - 1].value); 
				break;
			case 10:  // classdeflist ::= classdeflist identifier '(' ')'
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)tmStack[tmHead - 2].value) /* identifier */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset));
				break;
			case 11:  // classdeflist ::= error
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						null /* classdef */,
						null /* identifier */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol value) {
	}
}
