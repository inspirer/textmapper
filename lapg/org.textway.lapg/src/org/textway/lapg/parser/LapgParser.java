package org.textway.lapg.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ArrayList;
import org.textway.lapg.parser.LapgLexer.ErrorReporter;
import org.textway.lapg.parser.LapgLexer.Lexems;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.parser.ast.*;
import org.textway.lapg.parser.LapgLexer.LapgSymbol;

public class LapgParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public LapgParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	
	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	private static final int lapg_action[] = {
		-3, 104, -1, -13, 3, 6, -1, -1, 4, 7, -1, -23, -1, 28, 8, 87,
		88, -1, 89, 90, 91, 5, 94, 40, -1, -1, 33, -1, -1, -1, -29, 29,
		-37, 42, -1, -49, 80, 30, -57, -1, 95, -1, 41, 31, 17, -1, 19, 20,
		15, 16, -65, 13, 14, 18, 21, 23, 22, -1, 12, -93, -1, -107, 84, -1,
		-117, 43, 44, -123, 81, -1, -1, 92, -1, 93, 9, -129, -1, 10, 11, 27,
		-157, 48, -1, -1, -173, -1, -1, 85, -1, 96, 26, 36, -191, 47, 49, -1,
		-205, -1, -233, 72, -1, 50, -239, -255, -273, -1, -299, 58, -311, -319, -1, 38,
		39, 83, -1, 61, -337, -365, -1, -1, -1, -381, -387, 102, -1, -393, 45, -411,
		-429, -437, 64, -463, 75, 76, 74, -471, 59, 70, -499, 68, -1, -1, 53, 57,
		-1, 86, 71, -1, 73, -527, -1, 101, 100, 51, -555, 56, 55, -1, 60, -1,
		-563, 66, 97, 46, -591, 103, 54, 69, 67, -1, 65, -1, -2
	};

	private static final short lapg_lalr[] = {
		1, -1, 13, -1, 2, 0, 16, 0, -1, -2, 1, -1, 13, -1, 2, 1,
		16, 1, -1, -2, 18, -1, 15, 32, -1, -2, 18, -1, 9, 32, 15, 32,
		-1, -2, 1, -1, 2, -1, 6, -1, 27, -1, 0, 2, -1, -2, 27, -1,
		2, 79, 15, 79, -1, -2, 15, -1, 14, 8, 17, 8, -1, -2, 2, -1,
		13, -1, 14, -1, 16, -1, 17, -1, 18, -1, 21, -1, 22, -1, 23, -1,
		25, -1, 26, -1, 27, -1, 19, 24, -1, -2, 3, -1, 1, 34, 2, 34,
		6, 34, 16, 34, 27, 34, -1, -2, 18, -1, 2, 82, 15, 82, 27, 82,
		-1, -2, 18, -1, 9, 32, -1, -2, 18, -1, 9, 32, -1, -2, 2, -1,
		13, -1, 14, -1, 16, -1, 17, -1, 18, -1, 21, -1, 22, -1, 23, -1,
		25, -1, 26, -1, 27, -1, 19, 25, -1, -2, 5, -1, 1, 35, 2, 35,
		6, 35, 16, 35, 27, 35, 30, 35, -1, -2, 1, -1, 2, -1, 18, -1,
		27, -1, 30, -1, 10, 37, 12, 37, 20, 37, -1, -2, 30, -1, 1, 37,
		2, 37, 6, 37, 16, 37, 27, 37, -1, -2, 11, -1, 15, -1, 1, 8,
		2, 8, 10, 8, 12, 8, 18, 8, 20, 8, 23, 8, 24, 8, 25, 8,
		27, 8, 30, 8, -1, -2, 31, -1, 32, 98, -1, -2, 2, -1, 18, -1,
		27, -1, 30, -1, 10, 37, 12, 37, 20, 37, -1, -2, 1, -1, 2, -1,
		18, -1, 27, -1, 30, -1, 10, 37, 12, 37, 20, 37, -1, -2, 23, -1,
		24, -1, 25, -1, 1, 62, 2, 62, 10, 62, 12, 62, 18, 62, 19, 62,
		20, 62, 27, 62, 30, 62, -1, -2, 2, -1, 27, -1, 10, 38, 12, 38,
		20, 38, -1, -2, 20, -1, 10, 52, 12, 52, -1, -2, 1, -1, 2, -1,
		18, -1, 27, -1, 30, -1, 10, 37, 12, 37, 20, 37, -1, -2, 11, -1,
		1, 8, 2, 8, 10, 8, 12, 8, 18, 8, 19, 8, 20, 8, 23, 8,
		24, 8, 25, 8, 27, 8, 30, 8, -1, -2, 1, -1, 2, -1, 18, -1,
		27, -1, 30, -1, 10, 77, 19, 77, -1, -2, 31, -1, 32, 98, -1, -2,
		31, -1, 32, 99, -1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1,
		10, 37, 12, 37, 20, 37, -1, -2, 1, -1, 2, -1, 18, -1, 27, -1,
		30, -1, 10, 37, 12, 37, 20, 37, -1, -2, 20, -1, 10, 52, 12, 52,
		-1, -2, 23, -1, 24, -1, 25, -1, 1, 63, 2, 63, 10, 63, 12, 63,
		18, 63, 19, 63, 20, 63, 27, 63, 30, 63, -1, -2, 20, -1, 10, 52,
		12, 52, -1, -2, 11, -1, 15, -1, 1, 8, 2, 8, 10, 8, 12, 8,
		18, 8, 20, 8, 23, 8, 24, 8, 25, 8, 27, 8, 30, 8, -1, -2,
		11, -1, 1, 8, 2, 8, 10, 8, 12, 8, 18, 8, 19, 8, 20, 8,
		23, 8, 24, 8, 25, 8, 27, 8, 30, 8, -1, -2, 11, -1, 1, 8,
		2, 8, 10, 8, 12, 8, 18, 8, 19, 8, 20, 8, 23, 8, 24, 8,
		25, 8, 27, 8, 30, 8, -1, -2, 20, -1, 10, 52, 12, 52, -1, -2,
		11, -1, 1, 8, 2, 8, 10, 8, 12, 8, 18, 8, 19, 8, 20, 8,
		23, 8, 24, 8, 25, 8, 27, 8, 30, 8, -1, -2, 1, -1, 2, -1,
		18, -1, 27, -1, 30, -1, 10, 78, 19, 78, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 19, 59, 60, 67, 76, 78, 78, 78, 81, 84, 90, 93, 99, 105,
		112, 124, 131, 150, 155, 159, 163, 167, 173, 175, 181, 185, 204, 210, 216, 227,
		230, 232, 233, 234, 236, 240, 268, 272, 274, 278, 279, 280, 282, 283, 284, 286,
		287, 289, 292, 295, 301, 311, 312, 326, 340, 355, 356, 362, 363, 367, 378, 380,
		383, 401, 402, 403, 407, 408, 415, 419, 421
	};

	private static final short lapg_sym_from[] = {
		171, 0, 3, 7, 12, 17, 29, 32, 69, 72, 83, 84, 103, 109, 110, 117,
		125, 127, 164, 2, 6, 7, 12, 17, 25, 28, 29, 32, 34, 50, 57, 60,
		69, 70, 72, 75, 82, 83, 84, 97, 102, 103, 105, 106, 109, 110, 114, 117,
		119, 120, 125, 127, 140, 141, 147, 157, 159, 164, 169, 59, 7, 17, 25, 69,
		72, 83, 110, 7, 10, 17, 24, 69, 72, 80, 83, 110, 12, 32, 63, 85,
		86, 100, 118, 144, 96, 116, 135, 138, 149, 160, 82, 100, 144, 0, 3, 25,
		50, 57, 75, 25, 39, 41, 50, 57, 75, 27, 38, 63, 88, 96, 105, 135,
		6, 7, 12, 17, 25, 50, 57, 69, 72, 75, 83, 110, 24, 25, 39, 41,
		50, 57, 75, 11, 25, 30, 50, 57, 61, 64, 67, 75, 84, 97, 102, 103,
		109, 117, 125, 127, 147, 164, 45, 57, 76, 95, 118, 108, 128, 131, 154, 25,
		50, 57, 75, 25, 50, 57, 75, 25, 50, 57, 75, 104, 129, 104, 129, 25,
		50, 57, 75, 104, 129, 25, 50, 57, 75, 12, 25, 32, 35, 50, 57, 75,
		84, 97, 102, 103, 106, 109, 117, 120, 125, 127, 147, 164, 7, 17, 69, 72,
		83, 110, 7, 17, 69, 72, 83, 110, 84, 92, 97, 102, 103, 109, 117, 125,
		127, 147, 164, 98, 121, 122, 124, 150, 0, 0, 0, 3, 6, 12, 32, 34,
		7, 17, 60, 69, 72, 82, 83, 84, 97, 102, 103, 105, 106, 109, 110, 114,
		117, 119, 120, 125, 127, 140, 141, 147, 157, 159, 164, 169, 11, 30, 64, 67,
		25, 50, 25, 50, 57, 75, 59, 6, 6, 12, 10, 12, 12, 32, 60, 84,
		109, 84, 109, 125, 84, 109, 125, 84, 97, 102, 109, 125, 147, 84, 97, 102,
		103, 109, 117, 125, 127, 147, 164, 97, 12, 32, 84, 97, 102, 103, 106, 109,
		117, 120, 125, 127, 147, 164, 12, 32, 84, 97, 102, 103, 106, 109, 117, 120,
		125, 127, 147, 164, 12, 32, 35, 84, 97, 102, 103, 106, 109, 117, 120, 125,
		127, 147, 164, 17, 7, 17, 69, 72, 83, 110, 17, 108, 128, 131, 154, 84,
		92, 97, 102, 103, 109, 117, 125, 127, 147, 164, 98, 121, 98, 121, 122, 0,
		3, 7, 12, 17, 29, 32, 69, 72, 83, 84, 103, 109, 110, 117, 125, 127,
		164, 0, 50, 11, 30, 64, 67, 80, 84, 92, 102, 103, 109, 125, 127, 108,
		128, 131, 154, 98, 121
	};

	private static final short lapg_sym_to[] = {
		172, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 7, 9, 14, 9, 38, 44, 60, 61, 9, 9, 44, 44, 14,
		14, 88, 14, 44, 14, 14, 96, 116, 116, 116, 135, 138, 96, 14, 14, 116,
		149, 138, 96, 116, 160, 14, 116, 14, 14, 116, 14, 79, 15, 15, 45, 15,
		15, 15, 15, 16, 23, 16, 42, 16, 16, 91, 16, 16, 28, 28, 84, 84,
		109, 125, 147, 125, 114, 114, 157, 159, 157, 169, 93, 126, 163, 2, 2, 46,
		46, 46, 46, 47, 70, 72, 47, 47, 47, 59, 69, 59, 110, 115, 136, 158,
		10, 17, 10, 17, 48, 48, 48, 17, 17, 48, 17, 17, 43, 49, 71, 73,
		49, 49, 49, 25, 50, 25, 50, 50, 83, 25, 25, 50, 97, 97, 97, 97,
		97, 97, 97, 97, 97, 97, 74, 77, 90, 113, 148, 141, 141, 141, 141, 51,
		51, 51, 51, 52, 52, 52, 52, 53, 53, 53, 53, 132, 132, 133, 133, 54,
		54, 54, 54, 134, 134, 55, 55, 55, 55, 29, 56, 29, 29, 56, 56, 56,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 18, 18, 18, 18,
		18, 18, 19, 19, 19, 19, 19, 19, 98, 98, 98, 98, 98, 98, 98, 98,
		98, 98, 98, 121, 121, 121, 152, 165, 171, 3, 4, 8, 11, 30, 64, 67,
		20, 20, 81, 20, 20, 94, 20, 99, 99, 99, 99, 137, 139, 99, 20, 146,
		99, 137, 139, 99, 99, 161, 162, 99, 167, 168, 99, 170, 26, 26, 26, 26,
		57, 75, 58, 58, 78, 78, 80, 12, 13, 31, 24, 32, 33, 65, 82, 100,
		144, 101, 101, 153, 102, 102, 102, 103, 117, 127, 103, 103, 164, 104, 104, 104,
		129, 104, 129, 104, 129, 104, 129, 118, 34, 34, 105, 119, 119, 119, 140, 105,
		119, 140, 105, 119, 119, 119, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35,
		35, 35, 35, 35, 36, 36, 68, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 39, 21, 40, 87, 89, 95, 145, 41, 142, 142, 142, 142, 106,
		111, 120, 106, 106, 106, 120, 106, 106, 120, 120, 122, 122, 123, 123, 151, 5,
		5, 22, 37, 22, 62, 66, 22, 22, 22, 107, 130, 107, 22, 130, 107, 130,
		130, 6, 76, 27, 63, 85, 86, 92, 108, 112, 128, 131, 108, 108, 154, 143,
		155, 156, 166, 124, 150
	};

	private static final short lapg_rlen[] = {
		0, 1, 3, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3,
		0, 1, 3, 0, 1, 0, 1, 6, 1, 2, 1, 2, 2, 5, 6, 4,
		1, 2, 1, 3, 0, 1, 4, 3, 3, 2, 1, 2, 3, 2, 1, 2,
		2, 5, 3, 4, 2, 4, 2, 3, 1, 3, 2, 2, 2, 1, 3, 1,
		1, 2, 2, 5, 2, 3, 5, 1, 1, 1, 1, 1, 3, 3, 1, 1,
		3, 2, 0, 1, 3, 2, 1, 3, 1
	};

	private static final short lapg_rlex[] = {
		65, 65, 33, 34, 34, 35, 35, 36, 37, 38, 38, 39, 39, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 66, 66, 40, 41, 42, 42, 42, 43,
		67, 67, 43, 68, 68, 69, 69, 43, 44, 44, 45, 45, 45, 46, 46, 46,
		47, 47, 48, 48, 70, 70, 49, 49, 49, 49, 49, 50, 50, 50, 51, 51,
		51, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53, 53, 54,
		55, 55, 56, 56, 56, 57, 57, 58, 58, 58, 58, 58, 58, 58, 58, 59,
		59, 60, 71, 71, 61, 62, 62, 63, 64
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"error",
		"identifier",
		"regexp",
		"scon",
		"icon",
		"'%'",
		"_skip",
		"_skip_comment",
		"'::='",
		"'|'",
		"'='",
		"';'",
		"'.'",
		"','",
		"':'",
		"'['",
		"']'",
		"'('",
		"')'",
		"'<<'",
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'?'",
		"'&'",
		"'@'",
		"Ltrue",
		"Lfalse",
		"'{'",
		"'i{'",
		"'}'",
		"input",
		"options",
		"option",
		"symbol",
		"reference",
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"icon_list",
		"grammar_parts",
		"grammar_part",
		"references",
		"rules",
		"rule0",
		"ruleprefix",
		"rulesyms",
		"rulesym",
		"rulesyms_choice",
		"annotations_decl",
		"annotations",
		"annotation",
		"map_entries",
		"expression",
		"expression_list",
		"rule_priority",
		"command",
		"command_tokens",
		"command_token",
		"syntax_problem",
		"optionsopt",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"commandopt",
		"rule_priorityopt",
		"command_tokensopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 33;
		public static final int options = 34;
		public static final int option = 35;
		public static final int symbol = 36;
		public static final int reference = 37;
		public static final int type = 38;
		public static final int type_part_list = 39;
		public static final int type_part = 40;
		public static final int pattern = 41;
		public static final int lexer_parts = 42;
		public static final int lexer_part = 43;
		public static final int icon_list = 44;
		public static final int grammar_parts = 45;
		public static final int grammar_part = 46;
		public static final int references = 47;
		public static final int rules = 48;
		public static final int rule0 = 49;
		public static final int ruleprefix = 50;
		public static final int rulesyms = 51;
		public static final int rulesym = 52;
		public static final int rulesyms_choice = 53;
		public static final int annotations_decl = 54;
		public static final int annotations = 55;
		public static final int annotation = 56;
		public static final int map_entries = 57;
		public static final int expression = 58;
		public static final int expression_list = 59;
		public static final int rule_priority = 60;
		public static final int command = 61;
		public static final int command_tokens = 62;
		public static final int command_token = 63;
		public static final int syntax_problem = 64;
		public static final int optionsopt = 65;
		public static final int type_part_listopt = 66;
		public static final int typeopt = 67;
		public static final int iconopt = 68;
		public static final int commandopt = 69;
		public static final int rule_priorityopt = 70;
		public static final int command_tokensopt = 71;
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

	public AstRoot parse(LapgLexer lexer) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 172) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state, lapg_n.lexem);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift(lexer);
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				if (lapg_n.lexem == 0) {
					break;
				}
				while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 1) == -1) {
					lapg_m[lapg_head] = null; // TODO dispose?
					lapg_head--;
				}
				if (lapg_head >= 0) {
					lapg_m[++lapg_head] = new LapgSymbol();
					lapg_m[lapg_head].lexem = 1;
					lapg_m[lapg_head].sym = null;
					lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 1);
					lapg_m[lapg_head].line = lapg_n.line;
					lapg_m[lapg_head].offset = lapg_n.offset;
					lapg_m[lapg_head].endoffset = lapg_n.endoffset;
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
						lapg_n = lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				} else {
					lapg_head = 0;
					lapg_m[0] = new LapgSymbol();
					lapg_m[0].state = 0;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != 172) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return (AstRoot)lapg_m[lapg_head - 1].sym;
	}

	private void shift(LapgLexer lexer) throws IOException {
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
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		switch (rule) {
			case 2:  // input ::= optionsopt lexer_parts grammar_parts
				  lapg_gg.sym = new AstRoot(((List<AstOptionPart>)lapg_m[lapg_head-2].sym), ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOptionPart>(16); ((List<AstOptionPart>)lapg_gg.sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 4:  // options ::= options option
				 ((List<AstOptionPart>)lapg_m[lapg_head-1].sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 5:  // option ::= '.' identifier expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-1].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 10:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 27:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 28:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 30:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 31:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 34:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 39:  // lexer_part ::= symbol typeopt ':' pattern iconopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((AstRegexp)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 40:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 41:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 42:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 43:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 44:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 45:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // grammar_part ::= annotations_decl symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // grammar_part ::= '%' identifier references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 49:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 50:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 51:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 54:  // rule0 ::= ruleprefix rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstRulePrefix)lapg_m[lapg_head-3].sym).getAnnotations(), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // rule0 ::= rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 56:  // rule0 ::= ruleprefix commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstRulePrefix)lapg_m[lapg_head-2].sym).getAnnotations(), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 57:  // rule0 ::= commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 59:  // ruleprefix ::= annotations_decl ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 60:  // ruleprefix ::= annotations_decl identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 61:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 62:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 63:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 64:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 65:  // rulesym ::= command annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // rulesym ::= command annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // rulesym ::= command identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // rulesym ::= command reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-1].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // rulesym ::= annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // rulesym ::= annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // rulesym ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // rulesym ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // rulesym ::= '(' rulesyms_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 74:  // rulesym ::= rulesym '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 75:  // rulesym ::= rulesym '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 76:  // rulesym ::= rulesym '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 79:  // annotations_decl ::= annotations
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 81:  // annotations ::= annotations annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 82:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 85:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 86:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 87:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 90:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // expression ::= '[' map_entries ']'
				 lapg_gg.sym = new AstMap(((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // expression ::= '[' expression_list ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 96:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 97:  // rule_priority ::= '<<' reference
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head-0].sym); 
				break;
			case 100:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 104:  // syntax_problem ::= error
				 lapg_gg.sym = new AstError(source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset); 
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
