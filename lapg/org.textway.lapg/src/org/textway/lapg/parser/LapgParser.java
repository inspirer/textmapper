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
		-1, -1, 110, -3, -1, -1, 2, -11, -1, 27, 5, -17, 84, 85, -39, 86,
		87, 88, -1, -57, 95, -1, 39, -1, 3, -1, -1, 32, -1, 6, -1, -1,
		-63, 28, -71, 41, -1, -83, 79, 29, 96, -91, -1, -97, -1, 4, 40, 30,
		-103, 16, -1, 18, 19, 14, 15, -115, 12, 13, 17, 20, 22, 21, -1, 11,
		-143, -1, -157, 83, -1, -167, 42, 43, -173, 80, -1, 94, -1, -179, -1, 102,
		8, -185, -1, 9, 10, 26, -213, 7, 47, -1, -1, -229, -1, -1, 97, -1,
		-1, 91, 25, 35, -247, 46, 48, -1, -261, -1, -289, 71, -1, 49, -295, -311,
		-329, -1, -355, 57, -367, -375, 98, -1, 37, 38, 82, -1, 60, -393, -421, -1,
		-1, -1, -437, -443, 108, -1, -449, 44, -467, -485, -493, 63, -519, 74, 75, 73,
		-527, 58, 69, -555, 67, -1, -1, 52, 56, -1, -1, 70, -1, 72, -583, -1,
		107, 106, 50, -611, 55, 54, -1, 59, -1, -619, 65, 103, 45, 99, -647, 109,
		53, 68, 66, -1, 64, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 15, 6, 18, 6, -1, -2, 18, -1, 15, 31, -1, -2, 0, 7,
		1, 7, 2, 7, 14, 7, 16, 7, 17, 7, 19, 7, 32, 7, 13, 101,
		30, 101, -1, -2, 1, -1, 2, -1, 4, -1, 5, -1, 16, -1, 28, -1,
		29, -1, 17, 92, -1, -2, 13, -1, 30, 100, -1, -2, 18, -1, 9, 31,
		15, 31, -1, -2, 1, -1, 2, -1, 6, -1, 27, -1, 0, 1, -1, -2,
		27, -1, 2, 78, 15, 78, -1, -2, 14, -1, 17, 93, -1, -2, 2, -1,
		32, 89, -1, -2, 1, -1, 2, -1, 6, -1, 27, -1, 0, 0, -1, -2,
		2, -1, 13, -1, 14, -1, 16, -1, 17, -1, 18, -1, 21, -1, 22, -1,
		23, -1, 25, -1, 26, -1, 27, -1, 19, 23, -1, -2, 3, -1, 1, 33,
		2, 33, 6, 33, 16, 33, 27, 33, -1, -2, 18, -1, 2, 81, 15, 81,
		27, 81, -1, -2, 18, -1, 9, 31, -1, -2, 18, -1, 9, 31, -1, -2,
		14, -1, 32, 90, -1, -2, 2, -1, 13, -1, 14, -1, 16, -1, 17, -1,
		18, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1, 27, -1, 19, 24,
		-1, -2, 5, -1, 1, 34, 2, 34, 6, 34, 16, 34, 27, 34, 30, 34,
		-1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36,
		20, 36, -1, -2, 30, -1, 1, 36, 2, 36, 6, 36, 16, 36, 27, 36,
		-1, -2, 11, -1, 15, -1, 1, 7, 2, 7, 10, 7, 12, 7, 18, 7,
		20, 7, 23, 7, 24, 7, 25, 7, 27, 7, 30, 7, -1, -2, 31, -1,
		32, 104, -1, -2, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36,
		20, 36, -1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36,
		12, 36, 20, 36, -1, -2, 23, -1, 24, -1, 25, -1, 1, 61, 2, 61,
		10, 61, 12, 61, 18, 61, 19, 61, 20, 61, 27, 61, 30, 61, -1, -2,
		2, -1, 27, -1, 10, 37, 12, 37, 20, 37, -1, -2, 20, -1, 10, 51,
		12, 51, -1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36,
		12, 36, 20, 36, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 12, 7,
		18, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 27, 7, 30, 7,
		-1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 76, 19, 76,
		-1, -2, 31, -1, 32, 104, -1, -2, 31, -1, 32, 105, -1, -2, 1, -1,
		2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36, 20, 36, -1, -2,
		1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36, 20, 36,
		-1, -2, 20, -1, 10, 51, 12, 51, -1, -2, 23, -1, 24, -1, 25, -1,
		1, 62, 2, 62, 10, 62, 12, 62, 18, 62, 19, 62, 20, 62, 27, 62,
		30, 62, -1, -2, 20, -1, 10, 51, 12, 51, -1, -2, 11, -1, 15, -1,
		1, 7, 2, 7, 10, 7, 12, 7, 18, 7, 20, 7, 23, 7, 24, 7,
		25, 7, 27, 7, 30, 7, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7,
		12, 7, 18, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 27, 7,
		30, 7, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 12, 7, 18, 7,
		19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 27, 7, 30, 7, -1, -2,
		20, -1, 10, 51, 12, 51, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7,
		12, 7, 18, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 27, 7,
		30, 7, -1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 77,
		19, 77, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 23, 68, 69, 77, 87, 91, 91, 91, 94, 97, 104, 107, 112, 118,
		125, 140, 146, 165, 170, 174, 178, 182, 188, 190, 196, 200, 221, 228, 235, 247,
		250, 253, 254, 255, 257, 264, 293, 297, 299, 303, 304, 306, 310, 311, 313, 317,
		318, 320, 323, 326, 332, 342, 343, 359, 375, 392, 399, 400, 401, 408, 415, 419,
		430, 432, 435, 456, 457, 461, 462, 469, 473, 474, 475, 477
	};

	private static final short lapg_sym_from[] = {
		181, 182, 0, 1, 5, 8, 14, 21, 25, 31, 34, 48, 74, 90, 91, 95,
		111, 117, 126, 134, 136, 154, 174, 0, 1, 5, 8, 14, 21, 25, 26, 30,
		31, 34, 36, 43, 44, 48, 55, 62, 65, 74, 81, 89, 90, 91, 95, 96,
		105, 110, 111, 113, 114, 117, 123, 126, 128, 129, 134, 136, 149, 150, 154, 156,
		166, 168, 174, 179, 64, 1, 14, 21, 26, 74, 90, 95, 154, 1, 4, 14,
		21, 23, 74, 86, 90, 95, 154, 8, 25, 34, 48, 68, 92, 93, 108, 127,
		153, 3, 104, 125, 144, 147, 158, 169, 89, 108, 153, 19, 26, 55, 62, 81,
		26, 41, 55, 62, 77, 81, 28, 68, 76, 104, 113, 119, 144, 0, 1, 5,
		8, 14, 21, 25, 26, 55, 62, 74, 81, 90, 95, 154, 23, 26, 42, 55,
		62, 81, 7, 26, 32, 55, 62, 66, 69, 72, 81, 91, 105, 110, 111, 117,
		126, 134, 136, 156, 174, 50, 62, 82, 103, 127, 116, 137, 140, 163, 26, 55,
		62, 81, 26, 55, 62, 81, 26, 55, 62, 81, 112, 138, 112, 138, 26, 55,
		62, 81, 112, 138, 26, 55, 62, 81, 8, 25, 26, 34, 37, 48, 55, 62,
		81, 91, 105, 110, 111, 114, 117, 126, 129, 134, 136, 156, 174, 1, 14, 21,
		74, 90, 95, 154, 1, 14, 21, 74, 90, 95, 154, 18, 91, 100, 105, 110,
		111, 117, 126, 134, 136, 156, 174, 106, 130, 131, 78, 133, 159, 0, 0, 0,
		5, 0, 5, 8, 25, 34, 36, 48, 1, 14, 21, 65, 74, 89, 90, 91,
		95, 105, 110, 111, 113, 114, 117, 123, 126, 128, 129, 134, 136, 149, 150, 154,
		156, 166, 168, 174, 179, 7, 32, 69, 72, 26, 55, 26, 55, 62, 81, 64,
		0, 5, 0, 5, 8, 25, 4, 8, 25, 8, 25, 34, 48, 65, 91, 117,
		91, 117, 134, 91, 117, 134, 91, 105, 110, 117, 134, 156, 91, 105, 110, 111,
		117, 126, 134, 136, 156, 174, 105, 8, 25, 34, 48, 91, 105, 110, 111, 114,
		117, 126, 129, 134, 136, 156, 174, 8, 25, 34, 48, 91, 105, 110, 111, 114,
		117, 126, 129, 134, 136, 156, 174, 8, 25, 34, 37, 48, 91, 105, 110, 111,
		114, 117, 126, 129, 134, 136, 156, 174, 1, 14, 21, 74, 90, 95, 154, 14,
		43, 1, 14, 21, 74, 90, 95, 154, 1, 14, 21, 74, 90, 95, 154, 116,
		137, 140, 163, 91, 100, 105, 110, 111, 117, 126, 134, 136, 156, 174, 106, 130,
		106, 130, 131, 0, 1, 5, 8, 14, 21, 25, 31, 34, 48, 74, 90, 91,
		95, 111, 117, 126, 134, 136, 154, 174, 55, 7, 32, 69, 72, 86, 91, 100,
		110, 111, 117, 134, 136, 116, 137, 140, 163, 43, 14, 106, 130
	};

	private static final short lapg_sym_to[] = {
		183, 184, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 49, 65,
		66, 29, 29, 76, 79, 29, 49, 49, 87, 11, 49, 87, 11, 104, 11, 119,
		125, 125, 125, 144, 147, 104, 87, 125, 158, 147, 104, 125, 169, 87, 11, 125,
		87, 87, 125, 87, 85, 12, 12, 12, 50, 12, 12, 12, 12, 13, 22, 13,
		13, 46, 13, 99, 13, 13, 13, 30, 30, 30, 30, 91, 91, 117, 134, 156,
		134, 21, 123, 123, 166, 168, 166, 179, 101, 135, 172, 44, 51, 51, 51, 51,
		52, 74, 52, 52, 96, 52, 64, 64, 95, 124, 145, 154, 167, 4, 14, 4,
		4, 14, 14, 4, 53, 53, 53, 14, 53, 14, 14, 14, 47, 54, 75, 54,
		54, 54, 26, 55, 26, 55, 55, 90, 26, 26, 55, 105, 105, 105, 105, 105,
		105, 105, 105, 105, 105, 80, 83, 98, 122, 157, 150, 150, 150, 150, 56, 56,
		56, 56, 57, 57, 57, 57, 58, 58, 58, 58, 141, 141, 142, 142, 59, 59,
		59, 59, 143, 143, 60, 60, 60, 60, 31, 31, 61, 31, 31, 31, 61, 61,
		61, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 15, 15, 15,
		15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16, 43, 106, 106, 106, 106,
		106, 106, 106, 106, 106, 106, 106, 130, 130, 130, 97, 161, 175, 181, 5, 6,
		24, 7, 7, 32, 32, 69, 72, 69, 17, 17, 17, 88, 17, 102, 17, 107,
		17, 107, 107, 107, 146, 148, 107, 155, 107, 146, 148, 107, 107, 170, 171, 17,
		107, 177, 178, 107, 180, 27, 27, 27, 27, 62, 81, 63, 63, 84, 84, 86,
		8, 25, 9, 9, 33, 33, 23, 34, 48, 35, 35, 70, 70, 89, 108, 153,
		109, 109, 162, 110, 110, 110, 111, 126, 136, 111, 111, 174, 112, 112, 112, 138,
		112, 138, 112, 138, 112, 138, 127, 36, 36, 36, 36, 113, 128, 128, 128, 149,
		113, 128, 149, 113, 128, 128, 128, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 38, 38, 38, 73, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 182, 40, 45, 94, 103, 118, 173, 41,
		77, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 151,
		151, 151, 151, 114, 120, 129, 114, 114, 114, 129, 114, 114, 129, 129, 131, 131,
		132, 132, 160, 10, 20, 10, 39, 20, 20, 39, 67, 71, 71, 20, 20, 115,
		20, 139, 115, 139, 115, 139, 20, 139, 82, 28, 68, 92, 93, 100, 116, 121,
		137, 140, 116, 116, 163, 152, 164, 165, 176, 78, 42, 133, 159
	};

	private static final short lapg_rlen[] = {
		3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3, 0,
		1, 3, 0, 1, 0, 1, 6, 1, 2, 1, 2, 2, 5, 6, 4, 1,
		2, 1, 3, 0, 1, 4, 3, 3, 2, 1, 2, 3, 2, 1, 2, 2,
		5, 3, 4, 2, 4, 2, 3, 1, 3, 2, 2, 2, 1, 3, 1, 1,
		2, 2, 5, 2, 1, 1, 1, 1, 1, 0, 1, 4, 0, 1, 3, 1,
		1, 3, 3, 5, 1, 1, 3, 2, 0, 1, 3, 2, 1, 3, 1
	};

	private static final short lapg_rlex[] = {
		33, 33, 34, 34, 35, 35, 36, 37, 38, 38, 39, 39, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 67, 67, 40, 41, 42, 42, 42, 43, 68,
		68, 43, 69, 69, 70, 70, 43, 44, 44, 45, 45, 45, 46, 46, 46, 47,
		47, 48, 48, 71, 71, 49, 49, 49, 49, 49, 50, 50, 50, 51, 51, 51,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53, 53, 54, 55,
		55, 56, 56, 56, 57, 57, 57, 57, 57, 72, 72, 57, 73, 73, 57, 57,
		58, 58, 59, 59, 60, 61, 61, 62, 74, 74, 63, 64, 64, 65, 66
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
		"expression",
		"expression_list",
		"map_entries",
		"name",
		"qualified_id",
		"rule_priority",
		"command",
		"command_tokens",
		"command_token",
		"syntax_problem",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"commandopt",
		"rule_priorityopt",
		"map_entriesopt",
		"expression_listopt",
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
		public static final int expression = 57;
		public static final int expression_list = 58;
		public static final int map_entries = 59;
		public static final int name = 60;
		public static final int qualified_id = 61;
		public static final int rule_priority = 62;
		public static final int command = 63;
		public static final int command_tokens = 64;
		public static final int command_token = 65;
		public static final int syntax_problem = 66;
		public static final int type_part_listopt = 67;
		public static final int typeopt = 68;
		public static final int iconopt = 69;
		public static final int commandopt = 70;
		public static final int rule_priorityopt = 71;
		public static final int map_entriesopt = 72;
		public static final int expression_listopt = 73;
		public static final int command_tokensopt = 74;
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

	private Object parse(LapgLexer lexer, int state) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = state;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 183+state) {
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
					lapg_m[0].state = state;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != 183+state) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
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
			case 0:  // input ::= options lexer_parts grammar_parts
				  lapg_gg.sym = new AstRoot(((List<AstOptionPart>)lapg_m[lapg_head-2].sym), ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 1:  // input ::= lexer_parts grammar_parts
				  lapg_gg.sym = new AstRoot(null, ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 2:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOptionPart>(16); ((List<AstOptionPart>)lapg_gg.sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 3:  // options ::= options option
				 ((List<AstOptionPart>)lapg_m[lapg_head-1].sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 4:  // option ::= identifier '=' expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 9:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 26:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 27:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 30:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 38:  // lexer_part ::= symbol typeopt ':' pattern iconopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((AstRegexp)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 39:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 40:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 41:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 42:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 43:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 44:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 45:  // grammar_part ::= annotations_decl symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // grammar_part ::= '%' identifier references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 48:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 49:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 50:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 53:  // rule0 ::= ruleprefix rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstRulePrefix)lapg_m[lapg_head-3].sym).getAnnotations(), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // rule0 ::= rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // rule0 ::= ruleprefix commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstRulePrefix)lapg_m[lapg_head-2].sym).getAnnotations(), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 56:  // rule0 ::= commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 57:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 58:  // ruleprefix ::= annotations_decl ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 59:  // ruleprefix ::= annotations_decl identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 60:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 61:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 63:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 64:  // rulesym ::= command annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 65:  // rulesym ::= command annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // rulesym ::= command identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // rulesym ::= command reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-1].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // rulesym ::= annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // rulesym ::= annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // rulesym ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // rulesym ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // rulesym ::= '(' rulesyms_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 73:  // rulesym ::= rulesym '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 74:  // rulesym ::= rulesym '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 75:  // rulesym ::= rulesym '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 78:  // annotations_decl ::= annotations
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 80:  // annotations ::= annotations annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 81:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 84:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 91:  // expression ::= name '{' map_entriesopt '}'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 97:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 98:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 99:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 100:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 103:  // rule_priority ::= '<<' reference
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head-0].sym); 
				break;
			case 106:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 110:  // syntax_problem ::= error
				 lapg_gg.sym = new AstError(source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset); 
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}

	public AstRoot parseInput(LapgLexer lexer) throws IOException, ParseException {
		return (AstRoot) parse(lexer, 0);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1);
	}
}
