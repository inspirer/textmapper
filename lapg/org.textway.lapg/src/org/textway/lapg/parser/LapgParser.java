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
		-1, -1, 114, -3, -1, -1, 2, -11, -1, 27, 5, -17, 85, 86, -37, 87,
		88, 89, -1, -55, 96, -1, 40, -1, 3, -1, -1, 32, -1, -61, -1, -1,
		-71, 28, -79, 42, -1, -91, 80, 29, 97, -99, -1, -105, -1, 26, 34, 4,
		41, 30, -111, 16, -1, 18, 19, 14, 15, -123, 12, 13, 17, 20, 22, 21,
		-1, 11, -151, -1, -1, -165, 84, -1, 6, -175, 43, 44, -181, 81, -1, 95,
		-1, -187, -1, 106, 8, -193, -1, 9, 10, -221, 7, 48, -1, -1, -237, -1,
		-1, 98, 102, 103, 101, -1, -1, 92, 25, 36, -255, 47, 49, -1, -269, -1,
		-297, 72, -1, 50, -303, -319, -337, -1, -363, 58, -375, -383, 99, -1, 38, 39,
		83, -1, 61, -401, -429, -1, -1, -1, -445, -451, 112, -1, -457, 45, -475, -493,
		-501, 64, -527, 75, 76, 74, -535, 59, 70, -563, 68, -1, -1, 53, 57, -1,
		-1, 71, -1, 73, -591, -1, 111, 110, 51, -619, 56, 55, -1, 60, -1, -627,
		66, 107, 46, 100, -655, 113, 54, 69, 67, -1, 65, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 6, 19, 6, -1, -2, 19, -1, 16, 31, -1, -2, 0, 7,
		1, 7, 2, 7, 15, 7, 17, 7, 18, 7, 20, 7, 14, 105, 19, 105,
		-1, -2, 1, -1, 2, -1, 4, -1, 5, -1, 17, -1, 29, -1, 30, -1,
		18, 93, -1, -2, 14, -1, 19, 104, -1, -2, 11, -1, 9, 6, 16, 6,
		19, 6, -1, -2, 19, -1, 9, 31, 16, 31, -1, -2, 1, -1, 2, -1,
		6, -1, 28, -1, 0, 1, -1, -2, 28, -1, 2, 79, 16, 79, -1, -2,
		15, -1, 18, 94, -1, -2, 2, -1, 20, 90, -1, -2, 1, -1, 2, -1,
		6, -1, 28, -1, 0, 0, -1, -2, 2, -1, 14, -1, 15, -1, 17, -1,
		18, -1, 19, -1, 22, -1, 23, -1, 24, -1, 26, -1, 27, -1, 28, -1,
		20, 23, -1, -2, 3, -1, 1, 33, 2, 33, 6, 33, 17, 33, 28, 33,
		-1, -2, 19, -1, 2, 82, 16, 82, 28, 82, -1, -2, 19, -1, 9, 31,
		-1, -2, 19, -1, 9, 31, -1, -2, 15, -1, 20, 91, -1, -2, 2, -1,
		14, -1, 15, -1, 17, -1, 18, -1, 19, -1, 22, -1, 23, -1, 24, -1,
		26, -1, 27, -1, 28, -1, 20, 24, -1, -2, 5, -1, 1, 35, 2, 35,
		6, 35, 17, 35, 28, 35, 31, 35, -1, -2, 1, -1, 2, -1, 19, -1,
		28, -1, 31, -1, 10, 37, 13, 37, 21, 37, -1, -2, 31, -1, 1, 37,
		2, 37, 6, 37, 17, 37, 28, 37, -1, -2, 11, -1, 16, -1, 1, 7,
		2, 7, 10, 7, 13, 7, 19, 7, 21, 7, 24, 7, 25, 7, 26, 7,
		28, 7, 31, 7, -1, -2, 32, -1, 33, 108, -1, -2, 2, -1, 19, -1,
		28, -1, 31, -1, 10, 37, 13, 37, 21, 37, -1, -2, 1, -1, 2, -1,
		19, -1, 28, -1, 31, -1, 10, 37, 13, 37, 21, 37, -1, -2, 24, -1,
		25, -1, 26, -1, 1, 62, 2, 62, 10, 62, 13, 62, 19, 62, 20, 62,
		21, 62, 28, 62, 31, 62, -1, -2, 2, -1, 28, -1, 10, 38, 13, 38,
		21, 38, -1, -2, 21, -1, 10, 52, 13, 52, -1, -2, 1, -1, 2, -1,
		19, -1, 28, -1, 31, -1, 10, 37, 13, 37, 21, 37, -1, -2, 11, -1,
		1, 7, 2, 7, 10, 7, 13, 7, 19, 7, 20, 7, 21, 7, 24, 7,
		25, 7, 26, 7, 28, 7, 31, 7, -1, -2, 1, -1, 2, -1, 19, -1,
		28, -1, 31, -1, 10, 77, 20, 77, -1, -2, 32, -1, 33, 108, -1, -2,
		32, -1, 33, 109, -1, -2, 1, -1, 2, -1, 19, -1, 28, -1, 31, -1,
		10, 37, 13, 37, 21, 37, -1, -2, 1, -1, 2, -1, 19, -1, 28, -1,
		31, -1, 10, 37, 13, 37, 21, 37, -1, -2, 21, -1, 10, 52, 13, 52,
		-1, -2, 24, -1, 25, -1, 26, -1, 1, 63, 2, 63, 10, 63, 13, 63,
		19, 63, 20, 63, 21, 63, 28, 63, 31, 63, -1, -2, 21, -1, 10, 52,
		13, 52, -1, -2, 11, -1, 16, -1, 1, 7, 2, 7, 10, 7, 13, 7,
		19, 7, 21, 7, 24, 7, 25, 7, 26, 7, 28, 7, 31, 7, -1, -2,
		11, -1, 1, 7, 2, 7, 10, 7, 13, 7, 19, 7, 20, 7, 21, 7,
		24, 7, 25, 7, 26, 7, 28, 7, 31, 7, -1, -2, 11, -1, 1, 7,
		2, 7, 10, 7, 13, 7, 19, 7, 20, 7, 21, 7, 24, 7, 25, 7,
		26, 7, 28, 7, 31, 7, -1, -2, 21, -1, 10, 52, 13, 52, -1, -2,
		11, -1, 1, 7, 2, 7, 10, 7, 13, 7, 19, 7, 20, 7, 21, 7,
		24, 7, 25, 7, 26, 7, 28, 7, 31, 7, -1, -2, 1, -1, 2, -1,
		19, -1, 28, -1, 31, -1, 10, 78, 20, 78, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 23, 68, 71, 79, 89, 93, 93, 93, 96, 99, 109, 111, 114, 119,
		125, 132, 147, 153, 173, 179, 183, 187, 191, 197, 199, 205, 209, 230, 237, 244,
		255, 258, 260, 261, 262, 264, 271, 300, 304, 306, 310, 313, 315, 319, 320, 322,
		326, 327, 329, 332, 335, 341, 351, 352, 368, 384, 401, 408, 409, 410, 412, 419,
		426, 430, 441, 443, 446, 467, 468, 472, 473, 480, 484, 485, 486, 488
	};

	private static final short lapg_sym_from[] = {
		187, 188, 0, 1, 5, 8, 14, 21, 25, 31, 34, 50, 78, 93, 94, 101,
		117, 123, 132, 140, 142, 160, 180, 0, 1, 5, 8, 14, 21, 25, 26, 30,
		31, 34, 36, 43, 44, 50, 57, 64, 68, 78, 85, 92, 93, 94, 101, 102,
		111, 116, 117, 119, 120, 123, 129, 132, 134, 135, 140, 142, 155, 156, 160, 162,
		172, 174, 180, 185, 21, 66, 67, 1, 14, 21, 26, 78, 93, 101, 160, 1,
		4, 14, 21, 23, 78, 89, 93, 101, 160, 8, 25, 34, 50, 71, 95, 96,
		114, 133, 159, 3, 29, 80, 110, 125, 131, 150, 153, 164, 175, 80, 125, 92,
		114, 159, 19, 26, 57, 64, 85, 26, 41, 57, 64, 81, 85, 28, 71, 80,
		110, 119, 125, 150, 0, 1, 5, 8, 14, 21, 25, 26, 57, 64, 78, 85,
		93, 101, 160, 23, 26, 42, 57, 64, 85, 7, 18, 26, 32, 57, 64, 69,
		73, 76, 85, 94, 111, 116, 117, 123, 132, 140, 142, 162, 180, 52, 64, 82,
		86, 109, 133, 122, 143, 146, 169, 26, 57, 64, 85, 26, 57, 64, 85, 26,
		57, 64, 85, 118, 144, 118, 144, 26, 57, 64, 85, 118, 144, 26, 57, 64,
		85, 8, 25, 26, 34, 37, 50, 57, 64, 85, 94, 111, 116, 117, 120, 123,
		132, 135, 140, 142, 162, 180, 1, 14, 21, 78, 93, 101, 160, 1, 14, 21,
		78, 93, 101, 160, 94, 106, 111, 116, 117, 123, 132, 140, 142, 162, 180, 112,
		136, 137, 139, 165, 0, 0, 0, 5, 0, 5, 8, 25, 34, 36, 50, 1,
		14, 21, 68, 78, 92, 93, 94, 101, 111, 116, 117, 119, 120, 123, 129, 132,
		134, 135, 140, 142, 155, 156, 160, 162, 172, 174, 180, 185, 7, 32, 73, 76,
		26, 57, 26, 57, 64, 85, 21, 66, 67, 0, 5, 0, 5, 8, 25, 4,
		8, 25, 8, 25, 34, 50, 68, 94, 123, 94, 123, 140, 94, 123, 140, 94,
		111, 116, 123, 140, 162, 94, 111, 116, 117, 123, 132, 140, 142, 162, 180, 111,
		8, 25, 34, 50, 94, 111, 116, 117, 120, 123, 132, 135, 140, 142, 162, 180,
		8, 25, 34, 50, 94, 111, 116, 117, 120, 123, 132, 135, 140, 142, 162, 180,
		8, 25, 34, 37, 50, 94, 111, 116, 117, 120, 123, 132, 135, 140, 142, 162,
		180, 1, 14, 21, 78, 93, 101, 160, 14, 43, 80, 125, 1, 14, 21, 78,
		93, 101, 160, 1, 14, 21, 78, 93, 101, 160, 122, 143, 146, 169, 94, 106,
		111, 116, 117, 123, 132, 140, 142, 162, 180, 112, 136, 112, 136, 137, 0, 1,
		5, 8, 14, 21, 25, 31, 34, 50, 78, 93, 94, 101, 117, 123, 132, 140,
		142, 160, 180, 57, 7, 32, 73, 76, 89, 94, 106, 116, 117, 123, 140, 142,
		122, 143, 146, 169, 43, 14, 112, 136
	};

	private static final short lapg_sym_to[] = {
		189, 190, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 51, 68,
		69, 72, 72, 80, 83, 72, 51, 51, 90, 11, 51, 90, 11, 110, 11, 125,
		131, 131, 131, 150, 153, 110, 90, 131, 164, 153, 110, 131, 175, 90, 11, 131,
		90, 90, 131, 90, 45, 45, 45, 12, 12, 12, 52, 12, 12, 12, 12, 13,
		22, 13, 13, 48, 13, 105, 13, 13, 13, 30, 30, 30, 30, 94, 94, 123,
		140, 162, 140, 21, 67, 98, 129, 98, 129, 172, 174, 172, 185, 99, 99, 107,
		141, 178, 44, 53, 53, 53, 53, 54, 78, 54, 54, 102, 54, 66, 66, 100,
		130, 151, 100, 173, 4, 14, 4, 4, 14, 14, 4, 55, 55, 55, 14, 55,
		14, 14, 14, 49, 56, 79, 56, 56, 56, 26, 43, 57, 26, 57, 57, 93,
		26, 26, 57, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 84, 87, 103,
		104, 128, 163, 156, 156, 156, 156, 58, 58, 58, 58, 59, 59, 59, 59, 60,
		60, 60, 60, 147, 147, 148, 148, 61, 61, 61, 61, 149, 149, 62, 62, 62,
		62, 31, 31, 63, 31, 31, 31, 63, 63, 63, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16,
		16, 16, 16, 16, 112, 112, 112, 112, 112, 112, 112, 112, 112, 112, 112, 136,
		136, 136, 167, 181, 187, 5, 6, 24, 7, 7, 32, 32, 73, 76, 73, 17,
		17, 17, 91, 17, 108, 17, 113, 17, 113, 113, 113, 152, 154, 113, 161, 113,
		152, 154, 113, 113, 176, 177, 17, 113, 183, 184, 113, 186, 27, 27, 27, 27,
		64, 85, 65, 65, 88, 88, 46, 89, 46, 8, 25, 9, 9, 33, 33, 23,
		34, 50, 35, 35, 74, 74, 92, 114, 159, 115, 115, 168, 116, 116, 116, 117,
		132, 142, 117, 117, 180, 118, 118, 118, 144, 118, 144, 118, 144, 118, 144, 133,
		36, 36, 36, 36, 119, 134, 134, 134, 155, 119, 134, 155, 119, 134, 134, 134,
		37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		38, 38, 38, 77, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 188, 40, 47, 97, 109, 124, 179, 41, 81, 101, 160, 18, 18, 18, 18,
		18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 157, 157, 157, 157, 120, 126,
		135, 120, 120, 120, 135, 120, 120, 135, 135, 137, 137, 138, 138, 166, 10, 20,
		10, 39, 20, 20, 39, 70, 75, 75, 20, 20, 121, 20, 145, 121, 145, 121,
		145, 20, 145, 86, 28, 71, 95, 96, 106, 122, 127, 143, 146, 122, 122, 169,
		158, 170, 171, 182, 82, 42, 139, 165
	};

	private static final short lapg_rlen[] = {
		3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3, 0,
		1, 3, 3, 0, 1, 0, 1, 6, 1, 2, 1, 2, 2, 5, 6, 4,
		1, 2, 1, 3, 0, 1, 4, 3, 3, 2, 1, 2, 3, 2, 1, 2,
		2, 5, 3, 4, 2, 4, 2, 3, 1, 3, 2, 2, 2, 1, 3, 1,
		1, 2, 2, 5, 2, 1, 1, 1, 1, 1, 0, 1, 4, 0, 1, 3,
		1, 1, 3, 3, 5, 1, 1, 1, 1, 1, 3, 2, 0, 1, 3, 2,
		1, 3, 1
	};

	private static final short lapg_rlex[] = {
		34, 34, 35, 35, 36, 36, 37, 38, 39, 39, 40, 40, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 69, 69, 41, 42, 43, 43, 43, 44, 70,
		70, 44, 44, 71, 71, 72, 72, 44, 45, 45, 46, 46, 46, 47, 47, 47,
		48, 48, 49, 49, 73, 73, 50, 50, 50, 50, 50, 51, 51, 51, 52, 52,
		52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 55,
		56, 56, 57, 57, 57, 58, 58, 58, 58, 58, 74, 74, 58, 75, 75, 58,
		58, 59, 59, 60, 60, 61, 61, 61, 62, 63, 63, 64, 76, 76, 65, 66,
		66, 67, 68
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
		"'=>'",
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
		"map_separator",
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
		public static final int input = 34;
		public static final int options = 35;
		public static final int option = 36;
		public static final int symbol = 37;
		public static final int reference = 38;
		public static final int type = 39;
		public static final int type_part_list = 40;
		public static final int type_part = 41;
		public static final int pattern = 42;
		public static final int lexer_parts = 43;
		public static final int lexer_part = 44;
		public static final int icon_list = 45;
		public static final int grammar_parts = 46;
		public static final int grammar_part = 47;
		public static final int references = 48;
		public static final int rules = 49;
		public static final int rule0 = 50;
		public static final int ruleprefix = 51;
		public static final int rulesyms = 52;
		public static final int rulesym = 53;
		public static final int rulesyms_choice = 54;
		public static final int annotations_decl = 55;
		public static final int annotations = 56;
		public static final int annotation = 57;
		public static final int expression = 58;
		public static final int expression_list = 59;
		public static final int map_entries = 60;
		public static final int map_separator = 61;
		public static final int name = 62;
		public static final int qualified_id = 63;
		public static final int rule_priority = 64;
		public static final int command = 65;
		public static final int command_tokens = 66;
		public static final int command_token = 67;
		public static final int syntax_problem = 68;
		public static final int type_part_listopt = 69;
		public static final int typeopt = 70;
		public static final int iconopt = 71;
		public static final int commandopt = 72;
		public static final int rule_priorityopt = 73;
		public static final int map_entriesopt = 74;
		public static final int expression_listopt = 75;
		public static final int command_tokensopt = 76;
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

		while (lapg_m[lapg_head].state != 189+state) {
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

		if (lapg_m[lapg_head].state != 189+state) {
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
			case 34:  // lexer_part ::= identifier '=' pattern
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
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
			case 85:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 98:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 99:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 100:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 104:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 107:  // rule_priority ::= '<<' reference
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head-0].sym); 
				break;
			case 110:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 114:  // syntax_problem ::= error
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
