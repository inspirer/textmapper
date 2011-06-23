/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
		-3, 9, 10, 11, 12, 13, -31, -1, -1, 14, 15, 16, -1, 36, -59, -89,
		2, -1, 39, -1, 20, 21, 30, -1, 22, 23, 24, 25, 26, 27, 28, 29,
		31, -1, -117, -1, 5, 6, 7, 38, 17, 40, 41, 37, -1, -147, 33, 18,
		32, 3, -1, 19, 35, 8, -2
	};

	private static final short lapg_lalr[] = {
		1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 6, -1, 9, -1, 11, -1,
		13, -1, 14, -1, 15, -1, 0, 0, 7, 0, -1, -2, 1, -1, 2, -1,
		3, -1, 4, -1, 5, -1, 6, -1, 9, -1, 11, -1, 13, -1, 14, -1,
		15, -1, 7, 0, 8, 0, -1, -2, 9, -1, 13, -1, 14, -1, 15, -1,
		0, 4, 1, 4, 2, 4, 3, 4, 4, 4, 5, 4, 6, 4, 7, 4,
		8, 4, 11, 4, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 5, -1,
		6, -1, 11, -1, 13, -1, 14, -1, 15, -1, 0, 1, 7, 1, 8, 1,
		-1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 6, -1, 9, -1,
		11, -1, 13, -1, 14, -1, 15, -1, 0, 0, 7, 0, 8, 0, -1, -2,
		1, -1, 2, 34, 4, 34, 5, 34, 6, 34, 7, 34, 8, 34, 9, 34,
		10, 34, 12, 34, 13, 34, 14, 34, 15, 34, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 14, 22, 28, 36, 43, 51, 57, 62, 70, 76, 80, 82, 91, 100,
		109, 111, 115, 119, 123, 125, 128, 130, 133
	};

	private static final short lapg_sym_from[] = {
		12, 0, 6, 7, 8, 15, 19, 23, 33, 34, 35, 44, 45, 50, 0, 6,
		8, 15, 23, 33, 34, 44, 0, 6, 15, 19, 34, 50, 0, 6, 8, 15,
		23, 33, 34, 44, 0, 6, 8, 15, 33, 34, 44, 0, 6, 8, 15, 23,
		33, 34, 44, 8, 12, 17, 23, 33, 44, 8, 17, 23, 33, 44, 0, 6,
		8, 14, 23, 33, 34, 44, 8, 19, 23, 33, 44, 50, 0, 6, 15, 34,
		33, 44, 0, 6, 8, 14, 15, 23, 33, 34, 44, 0, 6, 8, 14, 15,
		23, 33, 34, 44, 0, 6, 8, 14, 15, 23, 33, 34, 44, 0, 6, 0,
		6, 15, 34, 0, 6, 15, 34, 8, 23, 33, 44, 8, 23, 0, 6, 34,
		7, 35, 0, 6, 34
	};

	private static final short lapg_sym_to[] = {
		54, 1, 1, 18, 20, 1, 41, 20, 20, 1, 18, 20, 52, 41, 2, 2,
		21, 2, 21, 21, 2, 21, 3, 3, 3, 42, 3, 42, 4, 4, 22, 4,
		22, 45, 4, 45, 5, 5, 23, 5, 46, 5, 46, 6, 6, 24, 6, 24,
		24, 6, 24, 25, 34, 34, 25, 25, 25, 26, 40, 26, 26, 26, 7, 7,
		27, 35, 27, 27, 7, 27, 28, 43, 28, 28, 28, 53, 8, 8, 8, 8,
		47, 51, 9, 9, 29, 36, 9, 29, 29, 9, 29, 10, 10, 30, 37, 10,
		30, 30, 10, 30, 11, 11, 31, 38, 11, 31, 31, 11, 31, 12, 17, 13,
		13, 39, 13, 14, 14, 14, 14, 32, 32, 48, 48, 33, 44, 15, 15, 15,
		19, 50, 16, 16, 49
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 3, 1, 2, 2, 2, 4, 1, 1, 1, 1, 1, 1, 1,
		1, 3, 3, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		2, 2, 2, 3, 1, 3, 2, 1, 2, 2
	};

	private static final short lapg_rlex[] = {
		23, 23, 16, 16, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20,
		20, 20, 20, 20, 21, 21, 21, 22, 22, 22
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
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int pattern = 16;
		public static final int part = 17;
		public static final int primitive_part = 18;
		public static final int setsymbol = 19;
		public static final int charset = 20;
		public static final int parts = 21;
		public static final int scon = 22;
		public static final int partsopt = 23;
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

		while (lapg_m[lapg_head].state != 54) {
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

		if (lapg_m[lapg_head].state != 54) {
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
				 lapg_gg.sym = RegexUtil.emptyIfNull(((RegexPart)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset); 
				break;
			case 3:  // pattern ::= pattern '|' partsopt
				 lapg_gg.sym = RegexUtil.createOr(((RegexPart)lapg_m[lapg_head-2].sym), ((RegexPart)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset); 
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
			case 8:  // part ::= primitive_part '{' scon '}'
				
												  lapg_gg.sym = RegexUtil.createQuantifierOrSequence(((RegexPart)lapg_m[lapg_head-3].sym), new RegexExpand(source, lapg_m[lapg_head-2].offset, lapg_gg.endoffset), reporter);
												  if(lapg_gg.sym instanceof RegexList) {
												  	if(lapg_n.lexem == Lexems.MULT || lapg_n.lexem == Lexems.PLUS || lapg_n.lexem == Lexems.QUESTIONMARK) {
												  	  RegexUtil.applyQuantifierToTheLastElement((RegexList) lapg_gg.sym, lapg_n.lexem);
												  	  try {
												  	  	lapg_n = lapg_lexer.next();
												  	  } catch(IOException e) {
												  	    // ignore
												  	  }
												  	}
												  }
												
				break;
			case 9:  // primitive_part ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // primitive_part ::= charclass
				 lapg_gg.sym = new RegexCharClass(((Character)lapg_m[lapg_head-0].sym), RegexUtil.getClassSet(((Character)lapg_m[lapg_head-0].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 11:  // primitive_part ::= '.'
				 lapg_gg.sym = new RegexAny(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 12:  // primitive_part ::= '-'
				 lapg_gg.sym = new RegexChar('-', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 13:  // primitive_part ::= '^'
				 lapg_gg.sym = new RegexChar('^', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 14:  // primitive_part ::= '*'
				 lapg_gg.sym = new RegexChar('*', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 15:  // primitive_part ::= '+'
				 lapg_gg.sym = new RegexChar('+', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 16:  // primitive_part ::= '?'
				 lapg_gg.sym = new RegexChar('?', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 17:  // primitive_part ::= '(' pattern ')'
				 lapg_gg.sym = RegexUtil.wrap(((RegexPart)lapg_m[lapg_head-1].sym)); 
				break;
			case 18:  // primitive_part ::= '[' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head-1].sym), reporter, setbuilder, false); 
				break;
			case 19:  // primitive_part ::= '[' '^' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head-1].sym), reporter, setbuilder, true); 
				break;
			case 20:  // setsymbol ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 21:  // setsymbol ::= charclass
				 lapg_gg.sym = new RegexCharClass(((Character)lapg_m[lapg_head-0].sym), RegexUtil.getClassSet(((Character)lapg_m[lapg_head-0].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 22:  // setsymbol ::= '('
				 lapg_gg.sym = new RegexChar('(', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 23:  // setsymbol ::= '|'
				 lapg_gg.sym = new RegexChar('|', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 24:  // setsymbol ::= ')'
				 lapg_gg.sym = new RegexChar(')', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 25:  // setsymbol ::= '{'
				 lapg_gg.sym = new RegexChar('{', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 26:  // setsymbol ::= '}'
				 lapg_gg.sym = new RegexChar('}', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 27:  // setsymbol ::= '*'
				 lapg_gg.sym = new RegexChar('*', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 28:  // setsymbol ::= '+'
				 lapg_gg.sym = new RegexChar('+', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 29:  // setsymbol ::= '?'
				 lapg_gg.sym = new RegexChar('?', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 30:  // charset ::= '-'
				 lapg_gg.sym = new ArrayList<RegexPart>(); ((List<RegexPart>)lapg_gg.sym).add(new RegexChar('-', source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 31:  // charset ::= setsymbol
				 lapg_gg.sym = new ArrayList<RegexPart>(); ((List<RegexPart>)lapg_gg.sym).add(((RegexPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 32:  // charset ::= charset setsymbol
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(((RegexPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 33:  // charset ::= charset '^'
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(new RegexChar('^', source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 34:  // charset ::= charset '-' %prio char
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(new RegexChar('-', source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 35:  // charset ::= charset '-' char
				 RegexUtil.applyRange(((List<RegexPart>)lapg_m[lapg_head-2].sym), new RegexChar(((Character)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset), reporter); 
				break;
			case 37:  // parts ::= '{' scon '}'
				 lapg_gg.sym = new RegexExpand(source, lapg_gg.offset, lapg_gg.endoffset); RegexUtil.checkExpand((RegexExpand) lapg_gg.sym, reporter, false); 
				break;
			case 38:  // parts ::= parts part
				 lapg_gg.sym = RegexUtil.createSequence(((RegexPart)lapg_m[lapg_head-1].sym), ((RegexPart)lapg_m[lapg_head-0].sym)); 
				break;
		}
	}
}
