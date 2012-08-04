package org.textmapper.lapg.ui.settings;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.lapg.ui.settings.SettingsLexer.ErrorReporter;
import org.textmapper.lapg.ui.settings.SettingsLexer.LapgSymbol;
import org.textmapper.lapg.ui.settings.SettingsLexer.Lexems;

public class SettingsParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public SettingsParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int lapg_action[] = {
		-1, -1, -3, 1, -1, 2, -1, -9, -1, -21, 4, -1, -1, 5, 7, -1,
		-1, 10, -1, 9, 8, -1, 11, -1, -2
	};

	private static final short lapg_lalr[] = {
		4, -1, 0, 0, -1, -2, 8, -1, 0, 6, 1, 6, 4, 6, 10, 6,
		-1, -2, 1, -1, 10, -1, 0, 3, 4, 3, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 4, 9, 9, 11, 12, 13, 14, 16, 17, 19, 20, 21, 23, 24,
		26, 27
	};

	private static final short lapg_sym_from[] = {
		23, 6, 8, 9, 1, 11, 15, 16, 21, 0, 2, 4, 11, 18, 7, 12,
		18, 6, 9, 0, 0, 0, 2, 6, 6, 9, 15
	};

	private static final short lapg_sym_to[] = {
		24, 7, 12, 7, 4, 14, 17, 19, 22, 1, 1, 6, 15, 20, 11, 16,
		21, 8, 8, 23, 2, 3, 5, 9, 10, 13, 18
	};

	private static final short lapg_rlen[] = {
		1, 1, 2, 4, 1, 2, 1, 3, 5, 4, 1, 3
	};

	private static final short lapg_rlex[] = {
		11, 12, 12, 13, 14, 14, 15, 15, 15, 15, 16, 16
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"scon",
		"_skip",
		"'['",
		"']'",
		"'('",
		"')'",
		"'='",
		"','",
		"Ldef",
		"input",
		"settings_list",
		"settings",
		"options_list",
		"option",
		"string_list",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 11;
		public static final int settings_list = 12;
		public static final int settings = 13;
		public static final int options_list = 14;
		public static final int option = 15;
		public static final int string_list = 16;
	}

	private static int lapg_next(int state, int symbol) {
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

	private static int lapg_state_sym(int state, int symbol) {
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

	private int lapg_head;
	private LapgSymbol[] lapg_m;
	private LapgSymbol lapg_n;

	public AstInput parse(SettingsLexer lexer) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 24) {
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

		if (lapg_m[lapg_head].state != 24) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		}
		return (AstInput)lapg_m[lapg_head - 1].sym;
	}

	private void shift(SettingsLexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lexer.next();
		}
	}

	@SuppressWarnings("unchecked")
	private void reduce(int rule) {
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
		switch (rule) {
			case 0:  // input ::= settings_list
				lapg_gg.sym = new AstInput(
((List<AstSettings>)lapg_m[lapg_head-0].sym) /* settingsList */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 1:  // settings_list ::= settings
				lapg_gg.sym = new ArrayList();
((List<AstSettings>)lapg_gg.sym).add(((AstSettings)lapg_m[lapg_head-0].sym));
				break;
			case 2:  // settings_list ::= settings_list settings
				((List<AstSettings>)lapg_m[lapg_head-1].sym).add(((AstSettings)lapg_m[lapg_head-0].sym));
				break;
			case 3:  // settings ::= '[' scon ']' options_list
				lapg_gg.sym = new AstSettings(
((String)lapg_m[lapg_head-2].sym) /* scon */,
((List<AstOption>)lapg_m[lapg_head-0].sym) /* optionsList */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 4:  // options_list ::= option
				lapg_gg.sym = new ArrayList();
((List<AstOption>)lapg_gg.sym).add(((AstOption)lapg_m[lapg_head-0].sym));
				break;
			case 5:  // options_list ::= options_list option
				((List<AstOption>)lapg_m[lapg_head-1].sym).add(((AstOption)lapg_m[lapg_head-0].sym));
				break;
			case 6:  // option ::= identifier
				lapg_gg.sym = new AstOption(
false,
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* scon */,
null /* stringList */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 7:  // option ::= identifier '=' scon
				lapg_gg.sym = new AstOption(
false,
((String)lapg_m[lapg_head-2].sym) /* identifier */,
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* stringList */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 8:  // option ::= identifier '=' '(' string_list ')'
				lapg_gg.sym = new AstOption(
false,
((String)lapg_m[lapg_head-4].sym) /* identifier */,
null /* scon */,
((List<String>)lapg_m[lapg_head-1].sym) /* stringList */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 9:  // option ::= Ldef identifier '=' scon
				lapg_gg.sym = new AstOption(
true,
((String)lapg_m[lapg_head-2].sym) /* identifier */,
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* stringList */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 10:  // string_list ::= scon
				lapg_gg.sym = new ArrayList();
((List<String>)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym));
				break;
			case 11:  // string_list ::= string_list ',' scon
				((List<String>)lapg_m[lapg_head-2].sym).add(((String)lapg_m[lapg_head-0].sym));
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
