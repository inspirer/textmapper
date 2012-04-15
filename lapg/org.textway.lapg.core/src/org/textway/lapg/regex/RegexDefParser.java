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
import org.textway.lapg.common.CharacterSetImpl;
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
	CharacterSetImpl.Builder setbuilder = new CharacterSetImpl.Builder();
	private static final int lapg_action[] = {
		-3, 16, 9, 10, 11, 12, -25, -1, -1, -1, 26, -47, -79, 2, -1, 17,
		18, 19, 20, 21, -1, -1, -103, 5, 6, 7, 8, 27, 13, 14, -127, 22,
		15, 3, 24, 25, -2
	};

	private static final short lapg_lalr[] = {
		1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 13, -1, 17, -1, 18, -1,
		0, 0, 14, 0, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 5, -1,
		13, -1, 17, -1, 18, -1, 14, 0, 15, 0, -1, -2, 6, -1, 7, -1,
		8, -1, 9, -1, 0, 4, 1, 4, 2, 4, 3, 4, 4, 4, 5, 4,
		13, 4, 14, 4, 15, 4, 17, 4, 18, 4, -1, -2, 1, -1, 2, -1,
		3, -1, 4, -1, 5, -1, 13, -1, 17, -1, 18, -1, 0, 1, 14, 1,
		15, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 13, -1,
		17, -1, 18, -1, 0, 0, 14, 0, 15, 0, -1, -2, 2, -1, 3, -1,
		4, 23, 20, 23, 21, 23, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 5, 14, 23, 31, 35, 36, 37, 38, 39, 39, 39, 39, 43, 45,
		46, 46, 50, 54, 54, 56, 60, 62, 66, 70, 74, 76, 79, 82
	};

	private static final short lapg_sym_from[] = {
		9, 0, 6, 12, 22, 0, 6, 7, 8, 12, 20, 21, 22, 30, 0, 6,
		7, 8, 12, 20, 21, 22, 30, 0, 6, 7, 8, 12, 20, 21, 22, 0,
		6, 12, 22, 11, 11, 11, 11, 0, 6, 12, 22, 9, 14, 14, 0, 6,
		12, 22, 0, 6, 12, 22, 20, 21, 7, 8, 20, 21, 0, 6, 0, 6,
		12, 22, 0, 6, 12, 22, 7, 8, 20, 21, 7, 8, 0, 6, 22, 0,
		6, 22
	};

	private static final short lapg_sym_to[] = {
		36, 1, 1, 1, 1, 2, 2, 15, 15, 2, 15, 15, 2, 34, 3, 3,
		16, 16, 3, 16, 16, 3, 35, 4, 4, 17, 17, 4, 17, 17, 4, 5,
		5, 5, 5, 23, 24, 25, 26, 6, 6, 6, 6, 22, 22, 28, 7, 7,
		7, 7, 8, 8, 8, 8, 29, 32, 18, 18, 30, 30, 9, 14, 10, 10,
		27, 10, 11, 11, 11, 11, 19, 19, 31, 31, 20, 21, 12, 12, 12, 13,
		13, 33
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 3, 1, 2, 2, 2, 2, 1, 1, 1, 1, 3, 3, 3,
		1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 1, 2
	};

	private static final short lapg_rlex[] = {
		28, 28, 22, 22, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24,
		24, 25, 25, 25, 26, 26, 26, 26, 26, 26, 27, 27
	};

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"expand",
		"char",
		"escaped",
		"charclass",
		"'.'",
		"'*'",
		"'+'",
		"'?'",
		"quantifier",
		"op_minus",
		"op_union",
		"op_intersect",
		"'('",
		"'|'",
		"')'",
		"'(?'",
		"'['",
		"'[^'",
		"kw_eoi",
		"']'",
		"'-'",
		"pattern",
		"part",
		"primitive_part",
		"setsymbol",
		"charset",
		"parts",
		"partsopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int pattern = 22;
		public static final int part = 23;
		public static final int primitive_part = 24;
		public static final int setsymbol = 25;
		public static final int charset = 26;
		public static final int parts = 27;
		public static final int partsopt = 28;
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
	protected RegexDefLexer lapg_lexer;

	public RegexPart parse(RegexDefLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 36) {
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

		if (lapg_m[lapg_head].state != 36) {
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
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 2:  // pattern ::= partsopt
				 lapg_gg.sym = RegexUtil.emptyIfNull(((RegexPart)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset); 
				break;
			case 3:  // pattern ::= pattern '|' partsopt
				 lapg_gg.sym = RegexUtil.createOr(((RegexPart)lapg_m[lapg_head - 2].sym), ((RegexPart)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset); 
				break;
			case 5:  // part ::= primitive_part '*'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head - 1].sym), 0, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // part ::= primitive_part '+'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head - 1].sym), 1, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // part ::= primitive_part '?'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head - 1].sym), 0, 1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // part ::= primitive_part quantifier
				 lapg_gg.sym = RegexUtil.createQuantifier(((RegexPart)lapg_m[lapg_head - 1].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset, reporter); 
				break;
			case 9:  // primitive_part ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // primitive_part ::= escaped
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 11:  // primitive_part ::= charclass
				 lapg_gg.sym = new RegexCharClass(((String)lapg_m[lapg_head].sym), RegexUtil.getClassSet(((String)lapg_m[lapg_head].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 12:  // primitive_part ::= '.'
				 lapg_gg.sym = new RegexAny(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 13:  // primitive_part ::= '(' pattern ')'
				 lapg_gg.sym = RegexUtil.wrap(((RegexPart)lapg_m[lapg_head - 1].sym)); 
				break;
			case 14:  // primitive_part ::= '[' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head - 1].sym), reporter, setbuilder, false); 
				break;
			case 15:  // primitive_part ::= '[^' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head - 1].sym), reporter, setbuilder, true); 
				break;
			case 16:  // primitive_part ::= expand
				 lapg_gg.sym = new RegexExpand(source, lapg_gg.offset, lapg_gg.endoffset); RegexUtil.checkExpand((RegexExpand) lapg_gg.sym, reporter); 
				break;
			case 17:  // setsymbol ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 18:  // setsymbol ::= escaped
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 19:  // setsymbol ::= charclass
				 lapg_gg.sym = new RegexCharClass(((String)lapg_m[lapg_head].sym), RegexUtil.getClassSet(((String)lapg_m[lapg_head].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 20:  // charset ::= '-'
				 lapg_gg.sym = new ArrayList<RegexPart>(); ((List<RegexPart>)lapg_gg.sym).add(new RegexChar('-', source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 21:  // charset ::= setsymbol
				 lapg_gg.sym = new ArrayList<RegexPart>(); RegexUtil.addSetSymbol(((List<RegexPart>)lapg_gg.sym), ((RegexPart)lapg_m[lapg_head].sym), reporter); 
				break;
			case 22:  // charset ::= charset setsymbol
				 RegexUtil.addSetSymbol(((List<RegexPart>)lapg_m[lapg_head - 1].sym), ((RegexPart)lapg_m[lapg_head].sym), reporter); 
				break;
			case 23:  // charset ::= charset '-' %prio char
				 ((List<RegexPart>)lapg_m[lapg_head - 1].sym).add(new RegexChar('-', source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 24:  // charset ::= charset '-' char
				 RegexUtil.applyRange(((List<RegexPart>)lapg_m[lapg_head - 2].sym), new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset), reporter); 
				break;
			case 25:  // charset ::= charset '-' escaped
				 RegexUtil.applyRange(((List<RegexPart>)lapg_m[lapg_head - 2].sym), new RegexChar(((Character)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset), reporter); 
				break;
			case 27:  // parts ::= parts part
				 lapg_gg.sym = RegexUtil.createSequence(((RegexPart)lapg_m[lapg_head - 1].sym), ((RegexPart)lapg_m[lapg_head].sym)); 
				break;
		}
	}
}
