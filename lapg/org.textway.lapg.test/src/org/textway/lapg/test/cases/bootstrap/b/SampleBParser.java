package org.textway.lapg.test.cases.bootstrap.b;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.lapg.test.cases.bootstrap.b.SampleBLexer.ErrorReporter;
import org.textway.lapg.test.cases.bootstrap.b.SampleBLexer.LapgSymbol;
import org.textway.lapg.test.cases.bootstrap.b.SampleBLexer.Lexems;
import org.textway.lapg.test.cases.bootstrap.b.ast.AstClassdef;
import org.textway.lapg.test.cases.bootstrap.b.ast.AstClassdeflistItem;
import org.textway.lapg.test.cases.bootstrap.b.ast.AstID;
import org.textway.lapg.test.cases.bootstrap.b.ast.IAstClassdefNoEoi;

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
	private static final int lapg_action[] = {
		-1, -1, 0, 5, -1, -1, -3, -1, -1, 10, 6, -17, -1, -29, -1, -1,
		7, 3, -1, 8, -1, 4, 9, -2
	};

	private static final short lapg_lalr[] = {
		1, -1, 16, -1, 4, -1, 3, -1, 11, -1, 6, 1, -1, -2, 1, -1,
		16, -1, 4, -1, 3, -1, 6, 2, -1, -2, 1, -1, 16, -1, 4, -1,
		3, -1, 11, -1, 6, 1, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 0, 5, 5, 9, 15, 17, 19, 21, 23, 23, 23, 25, 25, 25, 25,
		25, 30, 31, 35, 36, 38, 40
	};

	private static final short lapg_sym_from[] = {
		1, 5, 6, 11, 13, 0, 6, 11, 13, 1, 4, 5, 6, 11, 13, 4,
		7, 12, 18, 8, 15, 14, 20, 6, 13, 1, 5, 6, 11, 13, 0, 0,
		6, 11, 13, 1, 6, 13, 6, 13
	};

	private static final short lapg_sym_to[] = {
		3, 7, 8, 15, 8, 1, 1, 1, 1, 3, 5, 7, 8, 15, 8, 6,
		13, 17, 21, 14, 20, 19, 22, 9, 9, 3, 7, 8, 15, 8, 23, 2,
		10, 16, 10, 4, 11, 11, 12, 18
	};

	private static final short lapg_rlen[] = {
		1, 0, 1, 5, 7, 1, 1, 2, 3, 4, 1
	};

	private static final short lapg_rlex[] = {
		17, 21, 21, 18, 18, 19, 20, 20, 20, 20, 20
	};

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
			if(lapg_n == null) {
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
	protected SampleBLexer lapg_lexer;

	public IAstClassdefNoEoi parse(SampleBLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 23) {
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

		if (lapg_m[lapg_head].state != 23) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n == null ? lapg_lexer.getOffset() : lapg_n.offset, lapg_n == null ? lapg_lexer.getOffset() : lapg_n.endoffset, lapg_n == null ? lapg_lexer.getLine() : lapg_lexer.getTokenLine(), 
					MessageFormat.format("syntax error before line {0}",
					lapg_lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return (IAstClassdefNoEoi)lapg_m[lapg_head].sym;
	}

	protected boolean restore() throws IOException {
		if(lapg_n == null) {
			lapg_n = lapg_lexer.next();
		}
		if (lapg_n.lexem == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 11) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].lexem = 11;
			lapg_m[lapg_head].sym = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 11);
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endoffset = lapg_n.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		if(lapg_n == null) {
			lapg_n = lapg_lexer.next();
		}
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = null;
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
		lapg_gg.offset = startsym == null ? lapg_lexer.getOffset() : startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n == null ? lapg_lexer.getOffset() : lapg_n.offset;
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
			case 3:  // classdef ::= Lclass ID '{' classdeflistopt '}'
				lapg_gg.sym = new AstClassdef(
						((AstID)lapg_m[lapg_head-3].sym) /* ID */,
						((List<AstClassdeflistItem>)lapg_m[lapg_head-1].sym) /* classdeflistopt */,
						null /* Lextends */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 4:  // classdef ::= Lclass ID Lextends identifier '{' classdeflistopt '}'
				lapg_gg.sym = new AstClassdef(
						((AstID)lapg_m[lapg_head-5].sym) /* ID */,
						((List<AstClassdeflistItem>)lapg_m[lapg_head-1].sym) /* classdeflistopt */,
						((String)lapg_m[lapg_head-4].sym) /* Lextends */,
						((String)lapg_m[lapg_head-3].sym) /* identifier */,
						null /* input */, lapg_m[lapg_head-6].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 5:  // ID ::= identifier
				lapg_gg.sym = new AstID(
						((String)lapg_m[lapg_head-0].sym) /* identifier */,
						null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 6:  // classdeflist ::= classdef
				lapg_gg.sym = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.sym).add(new AstClassdeflistItem(
						((AstClassdef)lapg_m[lapg_head-0].sym) /* classdef */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 7:  // classdeflist ::= classdeflist classdef
				((List<AstClassdeflistItem>)lapg_m[lapg_head-1].sym).add(new AstClassdeflistItem(
						((AstClassdef)lapg_m[lapg_head-0].sym) /* classdef */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 8:  // classdeflist ::= identifier '(' ')'
				lapg_gg.sym = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.sym).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)lapg_m[lapg_head-2].sym) /* identifier */,
						null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 9:  // classdeflist ::= classdeflist identifier '(' ')'
				((List<AstClassdeflistItem>)lapg_m[lapg_head-3].sym).add(new AstClassdeflistItem(
						null /* classdef */,
						((String)lapg_m[lapg_head-2].sym) /* identifier */,
						null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 10:  // classdeflist ::= error
				lapg_gg.sym = new ArrayList();
				((List<AstClassdeflistItem>)lapg_gg.sym).add(new AstClassdeflistItem(
						null /* classdef */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset));
				break;
		}
	}

	/**
	 *  disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol sym) {
	}
}
