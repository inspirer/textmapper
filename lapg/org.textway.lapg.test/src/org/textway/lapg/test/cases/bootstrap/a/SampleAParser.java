package org.textway.lapg.test.cases.bootstrap.a;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.lapg.test.cases.bootstrap.a.SampleALexer.ErrorReporter;
import org.textway.lapg.test.cases.bootstrap.a.SampleALexer.LapgSymbol;
import org.textway.lapg.test.cases.bootstrap.a.SampleALexer.Lexems;
import org.textway.lapg.test.cases.bootstrap.a.ast.AstClassdef;
import org.textway.lapg.test.cases.bootstrap.a.ast.IAstClassdefNoEoi;

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
		-1, -1, -1, 0, -1, -3, 4, -9, -1, 5, 3, -2, -1, -2
	};

	private static final short lapg_lalr[] = {
		3, -1, 5, 1, -1, -2, 3, -1, 5, 2, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 2, 2, 6, 7, 8, 9, 13, 14, 15
	};

	private static final short lapg_sym_from[] = {
		12, 2, 0, 1, 5, 7, 4, 8, 0, 0, 1, 5, 7, 5, 5
	};

	private static final short lapg_sym_to[] = {
		13, 4, 2, 2, 2, 2, 5, 10, 11, 3, 12, 6, 9, 7, 8
	};

	private static final short lapg_rlen[] = {
		1, 0, 1, 5, 1, 2
	};

	private static final short lapg_rlex[] = {
		6, 9, 9, 7, 8, 8
	};

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"_skip",
		"Lclass",
		"'{'",
		"'}'",
		"classdef_NoEoi",
		"classdef",
		"classdeflist",
		"classdeflistopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int classdef_NoEoi = 6;
		public static final int classdef = 7;
		public static final int classdeflist = 8;
		public static final int classdeflistopt = 9;
	}

	protected final static int lapg_next(int state, int symbol) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == symbol) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final static int lapg_state_sym(int state, int symbol) {
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

	private Object parse(SampleALexer lexer, int initialState, int finalState) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = initialState;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != finalState) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state, lapg_n.lexem);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift(lexer);
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}, column {1}", lexer.getTokenLine(), lapg_n.column));
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
	}

	protected void shift(SampleALexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lexer.next();
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
		lapg_gg.line = startsym.line;
		lapg_gg.column = startsym.column;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endline = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endline : lapg_n.line;
		lapg_gg.endcolumn = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endcolumn : lapg_n.column;
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

	public IAstClassdefNoEoi parseClassdef_NoEoi(SampleALexer lexer) throws IOException, ParseException {
		return (IAstClassdefNoEoi) parse(lexer, 0, 11);
	}

	public AstClassdef parseClassdef(SampleALexer lexer) throws IOException, ParseException {
		return (AstClassdef) parse(lexer, 1, 13);
	}
}
