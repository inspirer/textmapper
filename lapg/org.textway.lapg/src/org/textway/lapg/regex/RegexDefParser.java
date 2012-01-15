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
package org.textway.lapg.regex;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.lapg.lex.CharacterSet;
import org.textway.lapg.regex.RegexDefLexer.ErrorReporter;
import org.textway.lapg.regex.RegexDefLexer.LapgSymbol;
import org.textway.lapg.regex.RegexDefLexer.Lexems;
import org.textway.lapg.regex.RegexDefTree.TextSource;

public class RegexDefParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public RegexDefParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	CharacterSet.Builder setbuilder = new CharacterSet.Builder();
	private static final int lapg_action[] = {
		-3, 11, 12, 13, 14, 15, -31, -59, -1, 16, 17, 18, -1, 41, -67, -99,
		2, -1, 43, 44, -129, -1, 23, 24, 35, -1, 25, 26, 27, 28, 29, 30,
		31, 32, 33, 34, 36, -1, -137, -167, 5, 6, 7, 42, 19, 45, 46, 22,
		-1, -175, 38, 20, 37, 3, -1, 21, 40, 10, -2
	};

	private static final short lapg_lalr[] = {
		1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 6, -1, 11, -1, 13, -1,
		15, -1, 16, -1, 17, -1, 0, 0, 7, 0, -1, -2, 1, -1, 2, -1,
		3, -1, 4, -1, 5, -1, 6, -1, 11, -1, 13, -1, 15, -1, 16, -1,
		17, -1, 7, 0, 8, 0, -1, -2, 1, -1, 4, -1, 12, 8, -1, -2,
		10, -1, 15, -1, 16, -1, 17, -1, 0, 4, 1, 4, 2, 4, 3, 4,
		4, 4, 5, 4, 6, 4, 7, 4, 8, 4, 11, 4, 13, 4, -1, -2,
		1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 6, -1, 11, -1, 13, -1,
		15, -1, 16, -1, 17, -1, 0, 1, 7, 1, 8, 1, -1, -2, 1, -1,
		4, -1, 12, 9, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 5, -1,
		6, -1, 11, -1, 13, -1, 15, -1, 16, -1, 17, -1, 0, 0, 7, 0,
		8, 0, -1, -2, 1, -1, 4, -1, 12, 8, -1, -2, 1, -1, 2, 39,
		4, 39, 5, 39, 6, 39, 7, 39, 8, 39, 9, 39, 10, 39, 11, 39,
		12, 39, 14, 39, 15, 39, 16, 39, 17, 39, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 13, 21, 25, 36, 43, 51, 57, 62, 66, 71, 79, 85, 89, 91,
		100, 109, 118, 120, 124, 128, 132, 134, 137, 139, 142, 144
	};

	private static final short lapg_sym_from[] = {
		12, 0, 6, 7, 8, 15, 20, 25, 37, 38, 39, 48, 49, 0, 6, 8,
		15, 25, 37, 38, 48, 0, 6, 15, 38, 0, 6, 7, 8, 15, 20, 25,
		37, 38, 39, 48, 0, 6, 8, 15, 37, 38, 48, 0, 6, 8, 15, 25,
		37, 38, 48, 8, 12, 17, 25, 37, 48, 8, 17, 25, 37, 48, 8, 25,
		37, 48, 8, 14, 25, 37, 48, 0, 6, 8, 15, 25, 37, 38, 48, 8,
		21, 25, 37, 48, 54, 0, 6, 15, 38, 37, 48, 0, 6, 8, 14, 15,
		25, 37, 38, 48, 0, 6, 8, 14, 15, 25, 37, 38, 48, 0, 6, 8,
		14, 15, 25, 37, 38, 48, 0, 6, 0, 6, 15, 38, 0, 6, 15, 38,
		8, 25, 37, 48, 8, 25, 0, 6, 38, 7, 39, 0, 6, 38, 7, 39
	};

	private static final short lapg_sym_to[] = {
		58, 1, 1, 18, 22, 1, 45, 22, 22, 1, 18, 22, 56, 2, 2, 23,
		2, 23, 23, 2, 23, 3, 3, 3, 3, 4, 4, 19, 24, 4, 46, 24,
		49, 4, 19, 49, 5, 5, 25, 5, 50, 5, 50, 6, 6, 26, 6, 26,
		26, 6, 26, 27, 38, 38, 27, 27, 27, 28, 44, 28, 28, 28, 29, 29,
		29, 29, 30, 39, 30, 30, 30, 7, 7, 31, 7, 31, 31, 7, 31, 32,
		47, 32, 32, 32, 57, 8, 8, 8, 8, 51, 55, 9, 9, 33, 40, 9,
		33, 33, 9, 33, 10, 10, 34, 41, 10, 34, 34, 10, 34, 11, 11, 35,
		42, 11, 35, 35, 11, 35, 12, 17, 13, 13, 43, 13, 14, 14, 14, 14,
		36, 36, 52, 52, 37, 48, 15, 15, 15, 20, 20, 16, 16, 53, 21, 54
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 3, 1, 2, 2, 2, 0, 1, 4, 1, 1, 1, 1, 1,
		1, 1, 1, 3, 3, 4, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 2, 2, 2, 3, 1, 2, 1, 1, 2, 2
	};

	private static final short lapg_rlex[] = {
		25, 25, 18, 18, 19, 19, 19, 19, 26, 26, 19, 20, 20, 20, 20, 20,
		20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21, 21, 21,
		21, 21, 21, 22, 22, 22, 22, 22, 22, 23, 23, 24, 24, 24, 24
	};

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"char",
		"charclass",
		"'.'",
		"'-'",
		"'^'",
		"'('",
		"'|'",
		"')'",
		"'{'",
		"'{digit'",
		"'{letter'",
		"'}'",
		"'['",
		"']'",
		"'*'",
		"'+'",
		"'?'",
		"pattern",
		"part",
		"primitive_part",
		"setsymbol",
		"charset",
		"parts",
		"scon",
		"partsopt",
		"sconopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int pattern = 18;
		public static final int part = 19;
		public static final int primitive_part = 20;
		public static final int setsymbol = 21;
		public static final int charset = 22;
		public static final int parts = 23;
		public static final int scon = 24;
		public static final int partsopt = 25;
		public static final int sconopt = 26;
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected static final int lapg_state_sym(int state, int symbol) {
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
	protected RegexDefLexer lapg_lexer;

	public RegexPart parse(RegexDefLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 58) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != 58) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_lexer.getTokenLine(), 
					MessageFormat.format("syntax error before line {0}",
					lapg_lexer.getTokenLine()));
			throw new ParseException();
		}
		return (RegexPart)lapg_m[lapg_head - 1].sym;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 2:  // pattern ::= partsopt
				 lapg_gg.sym = RegexUtil.emptyIfNull(((RegexPart)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset); 
				break;
			case 3:  // pattern ::= pattern '|' partsopt
				 lapg_gg.sym = RegexUtil.createOr(((RegexPart)lapg_m[lapg_head-2].sym), ((RegexPart)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset); 
				break;
			case 5:  // part ::= primitive_part '*'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head-1].sym), 0, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // part ::= primitive_part '+'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head-1].sym), 1, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // part ::= primitive_part '?'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head-1].sym), 0, 1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // part ::= primitive_part '{digit' sconopt '}'
				 lapg_gg.sym = RegexUtil.createQuantifier(((RegexPart)lapg_m[lapg_head-3].sym), source, lapg_m[lapg_head-2].offset, lapg_gg.endoffset, reporter); 
				break;
			case 11:  // primitive_part ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 12:  // primitive_part ::= charclass
				 lapg_gg.sym = new RegexCharClass(((String)lapg_m[lapg_head].sym), RegexUtil.getClassSet(((String)lapg_m[lapg_head].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 13:  // primitive_part ::= '.'
				 lapg_gg.sym = new RegexAny(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 14:  // primitive_part ::= '-'
				 lapg_gg.sym = new RegexChar('-', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 15:  // primitive_part ::= '^'
				 lapg_gg.sym = new RegexChar('^', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 16:  // primitive_part ::= '*'
				 lapg_gg.sym = new RegexChar('*', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 17:  // primitive_part ::= '+'
				 lapg_gg.sym = new RegexChar('+', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 18:  // primitive_part ::= '?'
				 lapg_gg.sym = new RegexChar('?', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 19:  // primitive_part ::= '(' pattern ')'
				 lapg_gg.sym = RegexUtil.wrap(((RegexPart)lapg_m[lapg_head-1].sym)); 
				break;
			case 20:  // primitive_part ::= '[' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head-1].sym), reporter, setbuilder, false); 
				break;
			case 21:  // primitive_part ::= '[' '^' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head-1].sym), reporter, setbuilder, true); 
				break;
			case 22:  // primitive_part ::= '{letter' sconopt '}'
				 lapg_gg.sym = new RegexExpand(source, lapg_gg.offset, lapg_gg.endoffset); RegexUtil.checkExpand((RegexExpand) lapg_gg.sym, reporter); 
				break;
			case 23:  // setsymbol ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 24:  // setsymbol ::= charclass
				 lapg_gg.sym = new RegexCharClass(((String)lapg_m[lapg_head].sym), RegexUtil.getClassSet(((String)lapg_m[lapg_head].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 25:  // setsymbol ::= '('
				 lapg_gg.sym = new RegexChar('(', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 26:  // setsymbol ::= '|'
				 lapg_gg.sym = new RegexChar('|', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 27:  // setsymbol ::= ')'
				 lapg_gg.sym = new RegexChar(')', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 28:  // setsymbol ::= '{'
				 lapg_gg.sym = new RegexChar('{', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 29:  // setsymbol ::= '{digit'
				 lapg_gg.sym = RegexUtil.createOr(new RegexChar('{', source, lapg_gg.offset, lapg_gg.offset+1),
																		  new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset+1, lapg_gg.endoffset), null, 0); 
				break;
			case 30:  // setsymbol ::= '{letter'
				 lapg_gg.sym = RegexUtil.createOr(new RegexChar('{', source, lapg_gg.offset, lapg_gg.offset+1),
																		  new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset+1, lapg_gg.endoffset), null, 0); 
				break;
			case 31:  // setsymbol ::= '}'
				 lapg_gg.sym = new RegexChar('}', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 32:  // setsymbol ::= '*'
				 lapg_gg.sym = new RegexChar('*', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // setsymbol ::= '+'
				 lapg_gg.sym = new RegexChar('+', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 34:  // setsymbol ::= '?'
				 lapg_gg.sym = new RegexChar('?', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 35:  // charset ::= '-'
				 lapg_gg.sym = new ArrayList<RegexPart>(); ((List<RegexPart>)lapg_gg.sym).add(new RegexChar('-', source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 36:  // charset ::= setsymbol
				 lapg_gg.sym = new ArrayList<RegexPart>(); RegexUtil.addSetSymbol(((List<RegexPart>)lapg_gg.sym), ((RegexPart)lapg_m[lapg_head].sym), reporter); 
				break;
			case 37:  // charset ::= charset setsymbol
				 RegexUtil.addSetSymbol(((List<RegexPart>)lapg_m[lapg_head-1].sym), ((RegexPart)lapg_m[lapg_head].sym), reporter); 
				break;
			case 38:  // charset ::= charset '^'
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(new RegexChar('^', source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 39:  // charset ::= charset '-' %prio char
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(new RegexChar('-', source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 40:  // charset ::= charset '-' char
				 RegexUtil.applyRange(((List<RegexPart>)lapg_m[lapg_head-2].sym), new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset), reporter); 
				break;
			case 42:  // parts ::= parts part
				 lapg_gg.sym = RegexUtil.createSequence(((RegexPart)lapg_m[lapg_head-1].sym), ((RegexPart)lapg_m[lapg_head].sym)); 
				break;
		}
	}
}
