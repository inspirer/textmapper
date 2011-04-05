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
		-1, 6, 7, 8, 9, -1, -1, -1, 10, 11, 12, -1, 32, -3, -31, -1,
		34, -1, 17, 26, -1, 18, 19, 20, 21, 22, 23, 24, 25, 27, -1, -1,
		3, 4, 5, 33, 13, 35, 36, 16, -1, -59, 29, 14, 28, -85, 15, 31,
		-2
	};

	private static final short lapg_lalr[] = {
		12, -1, 13, -1, 14, -1, 0, 2, 1, 2, 2, 2, 3, 2, 4, 2,
		5, 2, 6, 2, 7, 2, 8, 2, 10, 2, -1, -2, 1, -1, 2, -1,
		3, -1, 4, -1, 5, -1, 8, -1, 10, -1, 12, -1, 13, -1, 14, -1,
		0, 0, 6, 0, 7, 0, -1, -2, 1, -1, 3, 30, 4, 30, 5, 30,
		6, 30, 7, 30, 8, 30, 9, 30, 11, 30, 12, 30, 13, 30, 14, 30,
		-1, -2, 1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 8, -1, 10, -1,
		12, -1, 13, -1, 14, -1, 0, 1, 6, 1, 7, 1, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 13, 19, 28, 36, 45, 51, 56, 65, 70, 75, 77, 87, 97, 107,
		109, 114, 119, 123, 125, 128, 129
	};

	private static final short lapg_sym_from[] = {
		11, 0, 5, 6, 7, 14, 17, 20, 30, 31, 40, 41, 45, 0, 5, 14,
		17, 31, 45, 0, 5, 7, 14, 20, 30, 31, 40, 45, 0, 5, 7, 14,
		30, 31, 40, 45, 0, 5, 7, 14, 20, 30, 31, 40, 45, 7, 11, 15,
		20, 30, 40, 7, 15, 20, 30, 40, 0, 5, 7, 14, 20, 30, 31, 40,
		45, 7, 17, 20, 30, 40, 0, 5, 14, 31, 45, 30, 40, 0, 5, 7,
		13, 14, 20, 30, 31, 40, 45, 0, 5, 7, 13, 14, 20, 30, 31, 40,
		45, 0, 5, 7, 13, 14, 20, 30, 31, 40, 45, 0, 5, 0, 5, 14,
		31, 45, 0, 5, 14, 31, 45, 7, 20, 30, 40, 7, 20, 0, 5, 31,
		6
	};

	private static final short lapg_sym_to[] = {
		48, 1, 1, 16, 18, 1, 37, 18, 18, 1, 18, 47, 1, 2, 2, 2,
		38, 2, 2, 3, 3, 19, 3, 19, 41, 3, 41, 3, 4, 4, 20, 4,
		42, 4, 42, 4, 5, 5, 21, 5, 21, 21, 5, 21, 5, 22, 31, 31,
		22, 22, 22, 23, 36, 23, 23, 23, 6, 6, 24, 6, 24, 24, 6, 24,
		6, 25, 39, 25, 25, 25, 7, 7, 7, 7, 7, 43, 46, 8, 8, 26,
		32, 8, 26, 26, 8, 26, 8, 9, 9, 27, 33, 9, 27, 27, 9, 27,
		9, 10, 10, 28, 34, 10, 28, 28, 10, 28, 10, 11, 15, 12, 12, 35,
		12, 35, 13, 13, 13, 13, 13, 29, 29, 44, 44, 30, 40, 14, 14, 45,
		17
	};

	private static final short lapg_rlen[] = {
		1, 3, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 3, 3, 4,
		3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3,
		1, 2, 1, 2, 2
	};

	private static final short lapg_rlex[] = {
		15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
		17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19,
		20, 20, 21, 21, 21
	};

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"char",
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
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int pattern = 15;
		public static final int part = 16;
		public static final int primitive_part = 17;
		public static final int setsymbol = 18;
		public static final int charset = 19;
		public static final int parts = 20;
		public static final int scon = 21;
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

		while (lapg_m[lapg_head].state != 48) {
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

		if (lapg_m[lapg_head].state != 48) {
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
			case 1:  // pattern ::= pattern '|' parts
				 lapg_gg.sym = RegexUtil.createOr(((RegexPart)lapg_m[lapg_head-2].sym), ((RegexPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 3:  // part ::= primitive_part '*'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head-1].sym), 0, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 4:  // part ::= primitive_part '+'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head-1].sym), 1, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 5:  // part ::= primitive_part '?'
				 lapg_gg.sym = new RegexQuantifier(((RegexPart)lapg_m[lapg_head-1].sym), 0, 1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // primitive_part ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // primitive_part ::= '.'
				 lapg_gg.sym = new RegexAny(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // primitive_part ::= '-'
				 lapg_gg.sym = new RegexChar('-', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // primitive_part ::= '^'
				 lapg_gg.sym = new RegexChar('^', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // primitive_part ::= '*'
				 lapg_gg.sym = new RegexChar('*', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 11:  // primitive_part ::= '+'
				 lapg_gg.sym = new RegexChar('+', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 12:  // primitive_part ::= '?'
				 lapg_gg.sym = new RegexChar('?', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 13:  // primitive_part ::= '(' pattern ')'
				 lapg_gg.sym = RegexUtil.wrap(((RegexPart)lapg_m[lapg_head-1].sym)); 
				break;
			case 14:  // primitive_part ::= '[' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head-1].sym), reporter, setbuilder, false); 
				break;
			case 15:  // primitive_part ::= '[' '^' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexPart>)lapg_m[lapg_head-1].sym), reporter, setbuilder, true); 
				break;
			case 16:  // primitive_part ::= '{' scon '}'
				 lapg_gg.sym = new RegexExpand(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 17:  // setsymbol ::= char
				 lapg_gg.sym = new RegexChar(((Character)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 18:  // setsymbol ::= '('
				 lapg_gg.sym = new RegexChar('(', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 19:  // setsymbol ::= '|'
				 lapg_gg.sym = new RegexChar('|', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 20:  // setsymbol ::= ')'
				 lapg_gg.sym = new RegexChar(')', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 21:  // setsymbol ::= '{'
				 lapg_gg.sym = new RegexChar('{', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 22:  // setsymbol ::= '}'
				 lapg_gg.sym = new RegexChar('}', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 23:  // setsymbol ::= '*'
				 lapg_gg.sym = new RegexChar('*', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 24:  // setsymbol ::= '+'
				 lapg_gg.sym = new RegexChar('+', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 25:  // setsymbol ::= '?'
				 lapg_gg.sym = new RegexChar('?', source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 26:  // charset ::= '-'
				 lapg_gg.sym = new ArrayList<RegexPart>(); ((List<RegexPart>)lapg_gg.sym).add(new RegexChar('-', source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 27:  // charset ::= setsymbol
				 lapg_gg.sym = new ArrayList<RegexPart>(); ((List<RegexPart>)lapg_gg.sym).add(((RegexChar)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // charset ::= charset setsymbol
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(((RegexChar)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // charset ::= charset '^'
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(new RegexChar('^', source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 30:  // charset ::= charset '-' %prio char
				 ((List<RegexPart>)lapg_m[lapg_head-1].sym).add(new RegexChar('-', source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 31:  // charset ::= charset '-' char
				 RegexUtil.applyRange(((List<RegexPart>)lapg_m[lapg_head-2].sym), new RegexChar(((Character)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset), reporter); 
				break;
			case 33:  // parts ::= parts part
				 lapg_gg.sym = RegexUtil.createSequence(((RegexPart)lapg_m[lapg_head-1].sym), ((RegexPart)lapg_m[lapg_head-0].sym)); 
				break;
		}
	}
}
