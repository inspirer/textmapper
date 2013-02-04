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
	private static final int[] lapg_action = SampleBLexer.unpack_int(26,
		"\uffff\uffff\uffff\uffff\0\0\5\0\uffff\uffff\uffff\uffff\ufffd\uffff\uffff\uffff" +
		"\uffff\uffff\13\0\6\0\uffef\uffff\uffff\uffff\uffe3\uffff\uffff\uffff\uffff\uffff" +
		"\7\0\3\0\uffff\uffff\uffff\uffff\10\0\uffff\uffff\4\0\11\0\12\0\ufffe\uffff");

	private static final short[] lapg_lalr = SampleBLexer.unpack_short(40,
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
	protected SampleBLexer lapg_lexer;

	public IAstClassdefNoEoi parse(SampleBLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 25) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_lexer.getTokenLine(),
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
					lapg_m[0] = new LapgSymbol();
					lapg_m[0].state = 0;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != 25) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n == null ? lapg_lexer.getOffset() : lapg_n.offset, lapg_n == null ? lapg_lexer.getOffset() : lapg_n.endoffset, lapg_n == null ? lapg_lexer.getLine() : lapg_lexer.getTokenLine(),
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return (IAstClassdefNoEoi)lapg_m[lapg_head].value;
	}

	protected boolean restore() throws IOException {
		if (lapg_n == null) {
			lapg_n = lapg_lexer.next();
		}
		if (lapg_n.symbol == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 11) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].symbol = 11;
			lapg_m[lapg_head].value = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 11);
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endoffset = lapg_n.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		if (lapg_n == null) {
			lapg_n = lapg_lexer.next();
		}
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.symbol], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.symbol != 0) {
			lapg_n = null;
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
		lapg_gg.offset = startsym == null ? lapg_lexer.getOffset() : startsym.offset;
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
			case 3:  // classdef ::= Lclass ID '{' classdeflistopt '}'
				lapg_gg.value = new AstClassdef(
						true,
						false,
						((String)lapg_m[lapg_head - 3].value) /* ID */,
						((List<AstClassdeflistItem>)lapg_m[lapg_head - 1].value) /* classdeflistopt */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head - 4].offset, lapg_m[lapg_head].endoffset);
				break;
			case 4:  // classdef ::= Lclass ID Lextends identifier '{' classdeflistopt '}'
				lapg_gg.value = new AstClassdef(
						true,
						true,
						((String)lapg_m[lapg_head - 5].value) /* ID */,
						((List<AstClassdeflistItem>)lapg_m[lapg_head - 1].value) /* classdeflistopt */,
						((String)lapg_m[lapg_head - 3].value) /* identifier */,
						null /* input */, lapg_m[lapg_head - 6].offset, lapg_m[lapg_head].endoffset);
				break;
			case 6:  // classdeflist ::= classdef
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						((AstClassdef)lapg_m[lapg_head].value) /* classdef */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset));
				break;
			case 7:  // classdeflist ::= classdeflist classdef
				((List<AstClassdeflistItem>)lapg_m[lapg_head - 1].value).add(new AstClassdeflistItem(
						((AstClassdef)lapg_m[lapg_head].value) /* classdef */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head - 1].offset, lapg_m[lapg_head].endoffset));
				break;
			case 8:  // classdeflist ::= identifier '(' ')'
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)lapg_m[lapg_head - 2].value) /* identifier */,
						null /* input */, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset));
				break;
			case 9:  // classdeflist ::= identifier '(' Lextends ')'
				 String s = /* should be string */ ((String)lapg_m[lapg_head - 1].value); 
				break;
			case 10:  // classdeflist ::= classdeflist identifier '(' ')'
				((List<AstClassdeflistItem>)lapg_m[lapg_head - 3].value).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)lapg_m[lapg_head - 2].value) /* identifier */,
						null /* input */, lapg_m[lapg_head - 3].offset, lapg_m[lapg_head].endoffset));
				break;
			case 11:  // classdeflist ::= error
				lapg_gg.value = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.value).add(new AstClassdeflistItem(
						null /* classdef */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset));
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol value) {
	}
}
