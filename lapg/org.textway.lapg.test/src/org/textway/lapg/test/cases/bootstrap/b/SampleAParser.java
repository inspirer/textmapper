package org.textway.lapg.test.cases.bootstrap.b;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.lapg.test.cases.bootstrap.b.SampleALexer.ErrorReporter;
import org.textway.lapg.test.cases.bootstrap.b.SampleALexer.LapgSymbol;
import org.textway.lapg.test.cases.bootstrap.b.SampleALexer.Lexems;
import org.textway.lapg.test.cases.bootstrap.b.ast.AstClassdef;
import org.textway.lapg.test.cases.bootstrap.b.ast.IAstClassdefNoEoi;

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
	private static final int lapg_action[] = {
		-1, -1, 0, -1, -3, 6, 4, -11, -1, 5, 3, -2
	};

	private static final short lapg_lalr[] = {
		3, -1, 8, -1, 5, 1, -1, -2, 3, -1, 5, 2, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 0, 1, 1, 4, 5, 6, 6, 6, 7, 8, 11, 12, 13
	};

	private static final short lapg_sym_from[] = {
		1, 0, 4, 7, 3, 8, 4, 0, 0, 4, 7, 4, 4
	};

	private static final short lapg_sym_to[] = {
		3, 1, 1, 1, 4, 10, 5, 11, 2, 6, 9, 7, 8
	};

	private static final short lapg_rlen[] = {
		1, 0, 1, 5, 1, 2, 1
	};

	private static final short lapg_rlex[] = {
		9, 12, 12, 10, 11, 11, 11
	};

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"_skip",
		"Lclass",
		"'{'",
		"'}'",
		"Linterface",
		"Lenum",
		"error",
		"classdef_NoEoi",
		"classdef",
		"classdeflist",
		"classdeflistopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int classdef_NoEoi = 9;
		public static final int classdef = 10;
		public static final int classdeflist = 11;
		public static final int classdeflistopt = 12;
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
	protected SampleALexer lapg_lexer;

	public IAstClassdefNoEoi parse(SampleALexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 11) {
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

		if (lapg_m[lapg_head].state != 11) {
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
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 8) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].lexem = 8;
			lapg_m[lapg_head].sym = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 8);
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
			case 3:  // classdef ::= Lclass identifier '{' classdeflistopt '}'
				lapg_gg.sym = new AstClassdef(
						((String)lapg_m[lapg_head-3].sym) /* identifier */,
						((List<AstClassdef>)lapg_m[lapg_head-1].sym) /* classdeflistopt */,
						null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 4:  // classdeflist ::= classdef
				lapg_gg.sym = new ArrayList();
				((List<AstClassdef>)lapg_gg.sym).add(((AstClassdef)lapg_m[lapg_head-0].sym));
				break;
			case 5:  // classdeflist ::= classdeflist classdef
				((List<AstClassdef>)lapg_m[lapg_head-1].sym).add(((AstClassdef)lapg_m[lapg_head-0].sym));
				break;
		}
	}

	/**
	 *  disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol sym) {
	}
}
