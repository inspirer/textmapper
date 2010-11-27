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
		-1, 110, -3, -1, -1, 2, -11, -1, 27, 5, -1, 39, -1, 3, -1, -1,
		32, -1, 6, -1, -1, -17, 28, -25, 41, -1, -37, 79, 29, -45, 84, 85,
		-65, 86, 87, 88, 4, -1, -83, 95, 40, 30, -89, 16, -1, 18, 19, 14,
		15, -101, 12, 13, 17, 20, 22, 21, -1, 11, -129, -1, -143, 83, -1, -153,
		42, 43, -159, 80, 96, -165, -1, -171, -1, 8, -177, -1, 9, 10, 26, -205,
		7, 47, -1, -1, -221, -1, -1, -1, 94, -1, -239, -1, 102, 25, 35, -245,
		46, 48, -1, -259, -1, -287, 71, -1, 49, -293, -309, -327, -1, -353, 57, -365,
		-373, 97, -1, -1, 91, 37, 38, 82, -1, 60, -391, -419, -1, -1, -1, -435,
		-441, 108, -1, -447, 44, -465, -483, -491, 63, -517, 74, 75, 73, -525, 58, 69,
		-553, 67, -1, -1, 52, 56, -1, 98, -1, 70, -1, 72, -581, -1, 107, 106,
		50, -609, 55, 54, -1, 59, -1, -617, 65, 103, 45, -1, -645, 109, 53, 68,
		66, -1, 99, 64, -1, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 15, 6, 18, 6, -1, -2, 18, -1, 15, 31, -1, -2, 18, -1,
		9, 31, 15, 31, -1, -2, 1, -1, 2, -1, 6, -1, 27, -1, 0, 1,
		-1, -2, 27, -1, 2, 78, 15, 78, -1, -2, 1, 7, 2, 7, 14, 7,
		16, 7, 17, 7, 19, 7, 32, 7, 13, 101, 30, 101, -1, -2, 1, -1,
		2, -1, 4, -1, 5, -1, 16, -1, 28, -1, 29, -1, 17, 92, -1, -2,
		13, -1, 30, 100, -1, -2, 1, -1, 2, -1, 6, -1, 27, -1, 0, 0,
		-1, -2, 2, -1, 13, -1, 14, -1, 16, -1, 17, -1, 18, -1, 21, -1,
		22, -1, 23, -1, 25, -1, 26, -1, 27, -1, 19, 23, -1, -2, 3, -1,
		1, 33, 2, 33, 6, 33, 16, 33, 27, 33, -1, -2, 18, -1, 2, 81,
		15, 81, 27, 81, -1, -2, 18, -1, 9, 31, -1, -2, 18, -1, 9, 31,
		-1, -2, 14, -1, 17, 93, -1, -2, 2, -1, 32, 89, -1, -2, 2, -1,
		13, -1, 14, -1, 16, -1, 17, -1, 18, -1, 21, -1, 22, -1, 23, -1,
		25, -1, 26, -1, 27, -1, 19, 24, -1, -2, 5, -1, 1, 34, 2, 34,
		6, 34, 16, 34, 27, 34, 30, 34, -1, -2, 1, -1, 2, -1, 18, -1,
		27, -1, 30, -1, 10, 36, 12, 36, 20, 36, -1, -2, 14, -1, 32, 90,
		-1, -2, 30, -1, 1, 36, 2, 36, 6, 36, 16, 36, 27, 36, -1, -2,
		11, -1, 15, -1, 1, 7, 2, 7, 10, 7, 12, 7, 18, 7, 20, 7,
		23, 7, 24, 7, 25, 7, 27, 7, 30, 7, -1, -2, 31, -1, 32, 104,
		-1, -2, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36, 20, 36,
		-1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36,
		20, 36, -1, -2, 23, -1, 24, -1, 25, -1, 1, 61, 2, 61, 10, 61,
		12, 61, 18, 61, 19, 61, 20, 61, 27, 61, 30, 61, -1, -2, 2, -1,
		27, -1, 10, 37, 12, 37, 20, 37, -1, -2, 20, -1, 10, 51, 12, 51,
		-1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36,
		20, 36, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 12, 7, 18, 7,
		19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 27, 7, 30, 7, -1, -2,
		1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 76, 19, 76, -1, -2,
		31, -1, 32, 104, -1, -2, 31, -1, 32, 105, -1, -2, 1, -1, 2, -1,
		18, -1, 27, -1, 30, -1, 10, 36, 12, 36, 20, 36, -1, -2, 1, -1,
		2, -1, 18, -1, 27, -1, 30, -1, 10, 36, 12, 36, 20, 36, -1, -2,
		20, -1, 10, 51, 12, 51, -1, -2, 23, -1, 24, -1, 25, -1, 1, 62,
		2, 62, 10, 62, 12, 62, 18, 62, 19, 62, 20, 62, 27, 62, 30, 62,
		-1, -2, 20, -1, 10, 51, 12, 51, -1, -2, 11, -1, 15, -1, 1, 7,
		2, 7, 10, 7, 12, 7, 18, 7, 20, 7, 23, 7, 24, 7, 25, 7,
		27, 7, 30, 7, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 12, 7,
		18, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 27, 7, 30, 7,
		-1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 12, 7, 18, 7, 19, 7,
		20, 7, 23, 7, 24, 7, 25, 7, 27, 7, 30, 7, -1, -2, 20, -1,
		10, 51, 12, 51, -1, -2, 11, -1, 1, 7, 2, 7, 10, 7, 12, 7,
		18, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 27, 7, 30, 7,
		-1, -2, 1, -1, 2, -1, 18, -1, 27, -1, 30, -1, 10, 77, 19, 77,
		-1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 21, 65, 66, 73, 82, 86, 86, 86, 89, 92, 99, 102, 107, 113,
		120, 134, 140, 159, 164, 168, 172, 176, 182, 184, 190, 194, 215, 221, 227, 239,
		242, 245, 246, 247, 249, 256, 284, 288, 290, 294, 295, 297, 301, 302, 304, 308,
		309, 311, 314, 317, 323, 333, 334, 350, 366, 383, 389, 390, 391, 397, 403, 407,
		418, 420, 423, 443, 444, 448, 449, 456, 460, 461, 462, 464
	};

	private static final short lapg_sym_from[] = {
		180, 0, 4, 7, 10, 14, 20, 23, 32, 42, 83, 84, 87, 106, 112, 114,
		123, 131, 133, 171, 172, 0, 4, 7, 10, 14, 15, 19, 20, 23, 25, 32,
		42, 49, 56, 59, 71, 72, 74, 82, 83, 84, 87, 100, 105, 106, 108, 109,
		112, 114, 115, 120, 123, 125, 126, 131, 133, 146, 147, 154, 164, 166, 171, 172,
		177, 58, 10, 15, 32, 83, 87, 114, 171, 3, 10, 12, 32, 79, 83, 87,
		114, 171, 7, 14, 23, 42, 62, 85, 86, 103, 124, 150, 2, 99, 122, 141,
		144, 156, 167, 82, 103, 150, 15, 38, 49, 56, 74, 15, 49, 56, 69, 74,
		90, 17, 62, 89, 99, 108, 141, 152, 0, 4, 7, 10, 14, 15, 32, 49,
		56, 74, 83, 87, 114, 171, 12, 15, 49, 56, 70, 74, 6, 15, 21, 49,
		56, 60, 63, 66, 74, 84, 100, 105, 106, 112, 123, 131, 133, 154, 172, 44,
		56, 75, 98, 124, 111, 134, 137, 161, 15, 49, 56, 74, 15, 49, 56, 74,
		15, 49, 56, 74, 107, 135, 107, 135, 15, 49, 56, 74, 107, 135, 15, 49,
		56, 74, 7, 14, 15, 23, 26, 42, 49, 56, 74, 84, 100, 105, 106, 109,
		112, 123, 126, 131, 133, 154, 172, 10, 32, 83, 87, 114, 171, 10, 32, 83,
		87, 114, 171, 37, 84, 95, 100, 105, 106, 112, 123, 131, 133, 154, 172, 101,
		127, 128, 91, 130, 157, 0, 0, 0, 4, 0, 4, 7, 14, 23, 25, 42,
		10, 32, 59, 82, 83, 84, 87, 100, 105, 106, 108, 109, 112, 114, 120, 123,
		125, 126, 131, 133, 146, 147, 154, 164, 166, 171, 172, 177, 6, 21, 63, 66,
		15, 49, 15, 49, 56, 74, 58, 0, 4, 0, 4, 7, 14, 3, 7, 14,
		7, 14, 23, 42, 59, 84, 112, 84, 112, 131, 84, 112, 131, 84, 100, 105,
		112, 131, 154, 84, 100, 105, 106, 112, 123, 131, 133, 154, 172, 100, 7, 14,
		23, 42, 84, 100, 105, 106, 109, 112, 123, 126, 131, 133, 154, 172, 7, 14,
		23, 42, 84, 100, 105, 106, 109, 112, 123, 126, 131, 133, 154, 172, 7, 14,
		23, 26, 42, 84, 100, 105, 106, 109, 112, 123, 126, 131, 133, 154, 172, 10,
		32, 83, 87, 114, 171, 32, 71, 10, 32, 83, 87, 114, 171, 10, 32, 83,
		87, 114, 171, 111, 134, 137, 161, 84, 95, 100, 105, 106, 112, 123, 131, 133,
		154, 172, 101, 127, 101, 127, 128, 0, 4, 7, 10, 14, 20, 23, 32, 42,
		83, 84, 87, 106, 112, 114, 123, 131, 133, 171, 172, 49, 6, 21, 63, 66,
		79, 84, 95, 105, 106, 112, 131, 133, 111, 134, 137, 161, 71, 32, 101, 127
	};

	private static final short lapg_sym_to[] = {
		181, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 2, 2, 18, 29, 18, 43, 59, 60, 18, 18, 29,
		18, 43, 43, 80, 89, 92, 43, 80, 29, 99, 29, 122, 122, 122, 141, 144,
		99, 29, 152, 80, 122, 156, 144, 99, 122, 167, 80, 122, 80, 80, 29, 122,
		80, 78, 30, 44, 30, 30, 30, 30, 30, 11, 31, 40, 31, 94, 31, 31,
		31, 31, 19, 19, 19, 19, 84, 84, 112, 131, 154, 131, 10, 120, 120, 164,
		166, 164, 177, 96, 132, 170, 45, 72, 45, 45, 45, 46, 46, 46, 87, 46,
		115, 58, 58, 114, 121, 142, 165, 171, 3, 3, 3, 32, 3, 47, 32, 47,
		47, 47, 32, 32, 32, 32, 41, 48, 48, 48, 88, 48, 15, 49, 15, 49,
		49, 83, 15, 15, 49, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 73,
		76, 93, 119, 155, 147, 147, 147, 147, 50, 50, 50, 50, 51, 51, 51, 51,
		52, 52, 52, 52, 138, 138, 139, 139, 53, 53, 53, 53, 140, 140, 54, 54,
		54, 54, 20, 20, 55, 20, 20, 20, 55, 55, 55, 20, 20, 20, 20, 20,
		20, 20, 20, 20, 20, 20, 20, 33, 33, 33, 33, 33, 33, 34, 34, 34,
		34, 34, 34, 71, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 127,
		127, 127, 116, 159, 173, 180, 4, 5, 13, 6, 6, 21, 21, 63, 66, 63,
		35, 35, 81, 97, 35, 102, 35, 102, 102, 102, 143, 145, 102, 35, 153, 102,
		143, 145, 102, 102, 168, 169, 102, 175, 176, 35, 102, 179, 16, 16, 16, 16,
		56, 74, 57, 57, 77, 77, 79, 7, 14, 8, 8, 22, 22, 12, 23, 42,
		24, 24, 64, 64, 82, 103, 150, 104, 104, 160, 105, 105, 105, 106, 123, 133,
		106, 106, 172, 107, 107, 107, 135, 107, 135, 107, 135, 107, 135, 124, 25, 25,
		25, 25, 108, 125, 125, 125, 146, 108, 125, 146, 108, 125, 125, 125, 26, 26,
		26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 27, 27,
		27, 67, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 36,
		68, 98, 113, 151, 178, 69, 90, 37, 37, 37, 37, 37, 37, 38, 38, 38,
		38, 38, 38, 148, 148, 148, 148, 109, 117, 126, 109, 109, 109, 126, 109, 109,
		126, 126, 128, 128, 129, 129, 158, 9, 9, 28, 39, 28, 61, 65, 39, 65,
		39, 110, 39, 136, 110, 39, 136, 110, 136, 39, 136, 75, 17, 62, 85, 86,
		95, 111, 118, 134, 137, 111, 111, 161, 149, 162, 163, 174, 91, 70, 130, 157
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

	public AstRoot parse(LapgLexer lexer) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 181) {
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

		if (lapg_m[lapg_head].state != 181) {
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
}
