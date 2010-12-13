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
		-1, -1, 113, -3, -1, -1, 2, -11, -1, 27, 5, -17, 84, 85, -37, 86,
		87, 88, -1, -55, 95, -1, 39, -1, 3, -1, -1, 32, -1, 6, -1, -1,
		-61, 28, -69, 41, -1, -81, 79, 29, 96, -89, -1, -95, -1, 4, 40, 30,
		-101, 16, -1, 18, 19, 14, 15, -113, 12, 13, 17, 20, 22, 21, -1, 11,
		-141, -1, -155, 83, -1, -165, 42, 43, -171, 80, -1, 94, -1, -177, -1, 105,
		8, -183, -1, 9, 10, 26, -211, 7, 47, -1, -1, -227, -1, -1, 97, 101,
		102, 100, -1, -1, 91, 25, 35, -245, 46, 48, -1, -259, -1, -287, 71, -1,
		49, -293, -309, -327, -1, -353, 57, -365, -373, 98, -1, 37, 38, 82, -1, 60,
		-391, -419, -1, -1, -1, -435, -441, 111, -1, -447, 44, -465, -483, -491, 63, -517,
		74, 75, 73, -525, 58, 69, -553, 67, -1, -1, 52, 56, -1, -1, 70, -1,
		72, -581, -1, 110, 109, 50, -609, 55, 54, -1, 59, -1, -617, 65, 106, 45,
		99, -645, 112, 53, 68, 66, -1, 64, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 6, 19, 6, -1, -2, 19, -1, 16, 31, -1, -2, 0, 7,
		1, 7, 2, 7, 15, 7, 17, 7, 18, 7, 20, 7, 14, 104, 19, 104,
		-1, -2, 1, -1, 2, -1, 4, -1, 5, -1, 17, -1, 29, -1, 30, -1,
		18, 92, -1, -2, 14, -1, 19, 103, -1, -2, 19, -1, 9, 31, 16, 31,
		-1, -2, 1, -1, 2, -1, 6, -1, 28, -1, 0, 1, -1, -2, 28, -1,
		2, 78, 16, 78, -1, -2, 15, -1, 18, 93, -1, -2, 2, -1, 20, 89,
		-1, -2, 1, -1, 2, -1, 6, -1, 28, -1, 0, 0, -1, -2, 2, -1,
		14, -1, 15, -1, 17, -1, 18, -1, 19, -1, 22, -1, 23, -1, 24, -1,
		26, -1, 27, -1, 28, -1, 20, 23, -1, -2, 3, -1, 1, 33, 2, 33,
		6, 33, 17, 33, 28, 33, -1, -2, 19, -1, 2, 81, 16, 81, 28, 81,
		-1, -2, 19, -1, 9, 31, -1, -2, 19, -1, 9, 31, -1, -2, 15, -1,
		20, 90, -1, -2, 2, -1, 14, -1, 15, -1, 17, -1, 18, -1, 19, -1,
		22, -1, 23, -1, 24, -1, 26, -1, 27, -1, 28, -1, 20, 24, -1, -2,
		5, -1, 1, 34, 2, 34, 6, 34, 17, 34, 28, 34, 31, 34, -1, -2,
		1, -1, 2, -1, 19, -1, 28, -1, 31, -1, 10, 36, 13, 36, 21, 36,
		-1, -2, 31, -1, 1, 36, 2, 36, 6, 36, 17, 36, 28, 36, -1, -2,
		11, -1, 16, -1, 1, 7, 2, 7, 10, 7, 13, 7, 19, 7, 21, 7,
		24, 7, 25, 7, 26, 7, 28, 7, 31, 7, -1, -2, 32, -1, 33, 107,
		-1, -2, 2, -1, 19, -1, 28, -1, 31, -1, 10, 36, 13, 36, 21, 36,
		-1, -2, 1, -1, 2, -1, 19, -1, 28, -1, 31, -1, 10, 36, 13, 36,
		21, 36, -1, -2, 24, -1, 25, -1, 26, -1, 1, 61, 2, 61, 10, 61,
		13, 61, 19, 61, 20, 61, 21, 61, 28, 61, 31, 61, -1, -2, 2, -1,
		28, -1, 10, 37, 13, 37, 21, 37, -1, -2, 21, -1, 10, 51, 13, 51,
		-1, -2, 1, -1, 2, -1, 19, -1, 28, -1, 31, -1, 10, 36, 13, 36,
		21, 36, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 13, 7, 19, 7,
		20, 7, 21, 7, 24, 7, 25, 7, 26, 7, 28, 7, 31, 7, -1, -2,
		1, -1, 2, -1, 19, -1, 28, -1, 31, -1, 10, 76, 20, 76, -1, -2,
		32, -1, 33, 107, -1, -2, 32, -1, 33, 108, -1, -2, 1, -1, 2, -1,
		19, -1, 28, -1, 31, -1, 10, 36, 13, 36, 21, 36, -1, -2, 1, -1,
		2, -1, 19, -1, 28, -1, 31, -1, 10, 36, 13, 36, 21, 36, -1, -2,
		21, -1, 10, 51, 13, 51, -1, -2, 24, -1, 25, -1, 26, -1, 1, 62,
		2, 62, 10, 62, 13, 62, 19, 62, 20, 62, 21, 62, 28, 62, 31, 62,
		-1, -2, 21, -1, 10, 51, 13, 51, -1, -2, 11, -1, 16, -1, 1, 7,
		2, 7, 10, 7, 13, 7, 19, 7, 21, 7, 24, 7, 25, 7, 26, 7,
		28, 7, 31, 7, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 13, 7,
		19, 7, 20, 7, 21, 7, 24, 7, 25, 7, 26, 7, 28, 7, 31, 7,
		-1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 13, 7, 19, 7, 20, 7,
		21, 7, 24, 7, 25, 7, 26, 7, 28, 7, 31, 7, -1, -2, 21, -1,
		10, 51, 13, 51, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 13, 7,
		19, 7, 20, 7, 21, 7, 24, 7, 25, 7, 26, 7, 28, 7, 31, 7,
		-1, -2, 1, -1, 2, -1, 19, -1, 28, -1, 31, -1, 10, 77, 20, 77,
		-1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 23, 68, 69, 77, 87, 91, 91, 91, 94, 97, 106, 108, 111, 116,
		122, 129, 144, 150, 170, 176, 180, 184, 188, 194, 196, 202, 206, 227, 234, 241,
		252, 255, 257, 258, 259, 261, 268, 297, 301, 303, 307, 308, 310, 314, 315, 317,
		321, 322, 324, 327, 330, 336, 346, 347, 363, 379, 396, 403, 404, 405, 407, 414,
		421, 425, 436, 438, 441, 462, 463, 467, 468, 475, 479, 480, 481, 483
	};

	private static final short lapg_sym_from[] = {
		184, 185, 0, 1, 5, 8, 14, 21, 25, 31, 34, 48, 74, 90, 91, 98,
		114, 120, 129, 137, 139, 157, 177, 0, 1, 5, 8, 14, 21, 25, 26, 30,
		31, 34, 36, 43, 44, 48, 55, 62, 65, 74, 81, 89, 90, 91, 98, 99,
		108, 113, 114, 116, 117, 120, 126, 129, 131, 132, 137, 139, 152, 153, 157, 159,
		169, 171, 177, 182, 64, 1, 14, 21, 26, 74, 90, 98, 157, 1, 4, 14,
		21, 23, 74, 86, 90, 98, 157, 8, 25, 34, 48, 68, 92, 93, 111, 130,
		156, 3, 76, 107, 122, 128, 147, 150, 161, 172, 76, 122, 89, 111, 156, 19,
		26, 55, 62, 81, 26, 41, 55, 62, 77, 81, 28, 68, 76, 107, 116, 122,
		147, 0, 1, 5, 8, 14, 21, 25, 26, 55, 62, 74, 81, 90, 98, 157,
		23, 26, 42, 55, 62, 81, 7, 18, 26, 32, 55, 62, 66, 69, 72, 81,
		91, 108, 113, 114, 120, 129, 137, 139, 159, 177, 50, 62, 78, 82, 106, 130,
		119, 140, 143, 166, 26, 55, 62, 81, 26, 55, 62, 81, 26, 55, 62, 81,
		115, 141, 115, 141, 26, 55, 62, 81, 115, 141, 26, 55, 62, 81, 8, 25,
		26, 34, 37, 48, 55, 62, 81, 91, 108, 113, 114, 117, 120, 129, 132, 137,
		139, 159, 177, 1, 14, 21, 74, 90, 98, 157, 1, 14, 21, 74, 90, 98,
		157, 91, 103, 108, 113, 114, 120, 129, 137, 139, 159, 177, 109, 133, 134, 136,
		162, 0, 0, 0, 5, 0, 5, 8, 25, 34, 36, 48, 1, 14, 21, 65,
		74, 89, 90, 91, 98, 108, 113, 114, 116, 117, 120, 126, 129, 131, 132, 137,
		139, 152, 153, 157, 159, 169, 171, 177, 182, 7, 32, 69, 72, 26, 55, 26,
		55, 62, 81, 64, 0, 5, 0, 5, 8, 25, 4, 8, 25, 8, 25, 34,
		48, 65, 91, 120, 91, 120, 137, 91, 120, 137, 91, 108, 113, 120, 137, 159,
		91, 108, 113, 114, 120, 129, 137, 139, 159, 177, 108, 8, 25, 34, 48, 91,
		108, 113, 114, 117, 120, 129, 132, 137, 139, 159, 177, 8, 25, 34, 48, 91,
		108, 113, 114, 117, 120, 129, 132, 137, 139, 159, 177, 8, 25, 34, 37, 48,
		91, 108, 113, 114, 117, 120, 129, 132, 137, 139, 159, 177, 1, 14, 21, 74,
		90, 98, 157, 14, 43, 76, 122, 1, 14, 21, 74, 90, 98, 157, 1, 14,
		21, 74, 90, 98, 157, 119, 140, 143, 166, 91, 103, 108, 113, 114, 120, 129,
		137, 139, 159, 177, 109, 133, 109, 133, 134, 0, 1, 5, 8, 14, 21, 25,
		31, 34, 48, 74, 90, 91, 98, 114, 120, 129, 137, 139, 157, 177, 55, 7,
		32, 69, 72, 86, 91, 103, 113, 114, 120, 137, 139, 119, 140, 143, 166, 43,
		14, 109, 133
	};

	private static final short lapg_sym_to[] = {
		186, 187, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 49, 65,
		66, 29, 29, 76, 79, 29, 49, 49, 87, 11, 49, 87, 11, 107, 11, 122,
		128, 128, 128, 147, 150, 107, 87, 128, 161, 150, 107, 128, 172, 87, 11, 128,
		87, 87, 128, 87, 85, 12, 12, 12, 50, 12, 12, 12, 12, 13, 22, 13,
		13, 46, 13, 102, 13, 13, 13, 30, 30, 30, 30, 91, 91, 120, 137, 159,
		137, 21, 95, 126, 95, 126, 169, 171, 169, 182, 96, 96, 104, 138, 175, 44,
		51, 51, 51, 51, 52, 74, 52, 52, 99, 52, 64, 64, 97, 127, 148, 97,
		170, 4, 14, 4, 4, 14, 14, 4, 53, 53, 53, 14, 53, 14, 14, 14,
		47, 54, 75, 54, 54, 54, 26, 43, 55, 26, 55, 55, 90, 26, 26, 55,
		108, 108, 108, 108, 108, 108, 108, 108, 108, 108, 80, 83, 100, 101, 125, 160,
		153, 153, 153, 153, 56, 56, 56, 56, 57, 57, 57, 57, 58, 58, 58, 58,
		144, 144, 145, 145, 59, 59, 59, 59, 146, 146, 60, 60, 60, 60, 31, 31,
		61, 31, 31, 31, 61, 61, 61, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16,
		16, 109, 109, 109, 109, 109, 109, 109, 109, 109, 109, 109, 133, 133, 133, 164,
		178, 184, 5, 6, 24, 7, 7, 32, 32, 69, 72, 69, 17, 17, 17, 88,
		17, 105, 17, 110, 17, 110, 110, 110, 149, 151, 110, 158, 110, 149, 151, 110,
		110, 173, 174, 17, 110, 180, 181, 110, 183, 27, 27, 27, 27, 62, 81, 63,
		63, 84, 84, 86, 8, 25, 9, 9, 33, 33, 23, 34, 48, 35, 35, 70,
		70, 89, 111, 156, 112, 112, 165, 113, 113, 113, 114, 129, 139, 114, 114, 177,
		115, 115, 115, 141, 115, 141, 115, 141, 115, 141, 130, 36, 36, 36, 36, 116,
		131, 131, 131, 152, 116, 131, 152, 116, 131, 131, 131, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 38, 38, 38, 73, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 185, 40, 45, 94,
		106, 121, 176, 41, 77, 98, 157, 18, 18, 18, 18, 18, 18, 18, 19, 19,
		19, 19, 19, 19, 19, 154, 154, 154, 154, 117, 123, 132, 117, 117, 117, 132,
		117, 117, 132, 132, 134, 134, 135, 135, 163, 10, 20, 10, 39, 20, 20, 39,
		67, 71, 71, 20, 20, 118, 20, 142, 118, 142, 118, 142, 20, 142, 82, 28,
		68, 92, 93, 103, 119, 124, 140, 143, 119, 119, 166, 155, 167, 168, 179, 78,
		42, 136, 162
	};

	private static final short lapg_rlen[] = {
		3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3, 0,
		1, 3, 0, 1, 0, 1, 6, 1, 2, 1, 2, 2, 5, 6, 4, 1,
		2, 1, 3, 0, 1, 4, 3, 3, 2, 1, 2, 3, 2, 1, 2, 2,
		5, 3, 4, 2, 4, 2, 3, 1, 3, 2, 2, 2, 1, 3, 1, 1,
		2, 2, 5, 2, 1, 1, 1, 1, 1, 0, 1, 4, 0, 1, 3, 1,
		1, 3, 3, 5, 1, 1, 1, 1, 1, 3, 2, 0, 1, 3, 2, 1,
		3, 1
	};

	private static final short lapg_rlex[] = {
		34, 34, 35, 35, 36, 36, 37, 38, 39, 39, 40, 40, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 69, 69, 41, 42, 43, 43, 43, 44, 70,
		70, 44, 71, 71, 72, 72, 44, 45, 45, 46, 46, 46, 47, 47, 47, 48,
		48, 49, 49, 73, 73, 50, 50, 50, 50, 50, 51, 51, 51, 52, 52, 52,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 55, 56,
		56, 57, 57, 57, 58, 58, 58, 58, 58, 74, 74, 58, 75, 75, 58, 58,
		59, 59, 60, 60, 61, 61, 61, 62, 63, 63, 64, 76, 76, 65, 66, 66,
		67, 68
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

		while (lapg_m[lapg_head].state != 186+state) {
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

		if (lapg_m[lapg_head].state != 186+state) {
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
			case 91:  // expression ::= name '(' map_entriesopt ')'
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
			case 98:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 99:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 103:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 106:  // rule_priority ::= '<<' reference
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head-0].sym); 
				break;
			case 109:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 113:  // syntax_problem ::= error
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
