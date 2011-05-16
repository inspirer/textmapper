/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ArrayList;
import org.textway.lapg.api.Lexem;
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
	
	private AstLexemAttrs lexemAttrs(String attr, TextSource source, int offset, int endoffset, int line) {
		if("class".equals(attr)) {
			return new AstLexemAttrs(Lexem.KIND_CLASS, source, offset, endoffset);
		} else if("soft".equals(attr)) {
			return new AstLexemAttrs(Lexem.KIND_SOFT, source, offset, endoffset);
		}
	    reporter.error(offset, endoffset, line, "unknown lexem attribute: " + attr);
		return null;
	}
	private static final int lapg_action[] = {
		-1, -1, 119, -3, -1, -1, 2, -11, -1, 27, 5, -17, 89, 90, -37, 91,
		92, 93, -1, -55, 100, -1, 43, -1, 3, -1, -1, 33, -1, -61, -1, -1,
		-71, 28, -79, 45, -1, -91, 84, 29, 101, -99, -1, -105, -1, 26, 31, 4,
		44, 30, -111, 16, -1, 18, 19, 14, 15, -123, 12, 13, 17, 20, 22, 21,
		-1, 11, -151, -1, -1, -165, 88, -1, 6, -175, 46, 47, -181, 85, -1, 99,
		-1, -187, -1, 110, 8, -193, -1, 9, 10, -221, 7, 51, -1, -1, -239, -1,
		-1, 102, 106, 107, 105, -1, -1, 96, 25, 36, -257, 50, 52, -1, -273, -1,
		-303, 75, -1, 53, -309, -325, -343, -1, -371, 61, -383, -391, 103, -1, -1, 38,
		-409, 87, -1, 64, -423, -453, -1, -1, -1, -469, -475, 117, -1, -481, 48, -499,
		-517, -525, 67, -553, 79, 80, 78, -1, -561, 62, 73, -591, 71, -1, -1, 56,
		60, -1, -1, -1, 40, 41, 74, -1, 76, -621, -1, 116, 115, 54, -651, 59,
		58, -659, -1, 63, -1, -687, 69, -1, 112, 49, 104, 42, -717, 118, 57, 72,
		70, -1, 111, 68, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 6, 19, 6, -1, -2, 19, -1, 16, 32, -1, -2, 0, 7,
		1, 7, 2, 7, 15, 7, 17, 7, 18, 7, 20, 7, 14, 109, 19, 109,
		-1, -2, 1, -1, 2, -1, 4, -1, 5, -1, 17, -1, 28, -1, 29, -1,
		18, 97, -1, -2, 14, -1, 19, 108, -1, -2, 11, -1, 9, 6, 16, 6,
		19, 6, -1, -2, 19, -1, 9, 32, 16, 32, -1, -2, 1, -1, 2, -1,
		6, -1, 27, -1, 0, 1, -1, -2, 27, -1, 2, 83, 16, 83, -1, -2,
		15, -1, 18, 98, -1, -2, 2, -1, 20, 94, -1, -2, 1, -1, 2, -1,
		6, -1, 27, -1, 0, 0, -1, -2, 2, -1, 14, -1, 15, -1, 17, -1,
		18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1, 27, -1,
		20, 23, -1, -2, 3, -1, 1, 34, 2, 34, 6, 34, 17, 34, 27, 34,
		-1, -2, 19, -1, 2, 86, 16, 86, 27, 86, -1, -2, 19, -1, 9, 32,
		-1, -2, 19, -1, 9, 32, -1, -2, 15, -1, 20, 95, -1, -2, 2, -1,
		14, -1, 15, -1, 17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1,
		25, -1, 26, -1, 27, -1, 20, 24, -1, -2, 5, -1, 1, 35, 2, 35,
		6, 35, 17, 35, 19, 35, 27, 35, 33, 35, -1, -2, 1, -1, 2, -1,
		19, -1, 27, -1, 33, -1, 6, 39, 10, 39, 13, 39, -1, -2, 19, -1,
		1, 37, 2, 37, 6, 37, 17, 37, 27, 37, 33, 37, -1, -2, 11, -1,
		16, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 23, 7,
		24, 7, 25, 7, 26, 7, 27, 7, 33, 7, -1, -2, 34, -1, 35, 113,
		-1, -2, 2, -1, 19, -1, 27, -1, 33, -1, 6, 39, 10, 39, 13, 39,
		-1, -2, 1, -1, 2, -1, 19, -1, 27, -1, 33, -1, 6, 39, 10, 39,
		13, 39, -1, -2, 23, -1, 24, -1, 25, -1, 26, -1, 1, 65, 2, 65,
		6, 65, 10, 65, 13, 65, 19, 65, 20, 65, 27, 65, 33, 65, -1, -2,
		2, -1, 27, -1, 6, 40, 10, 40, 13, 40, -1, -2, 6, -1, 10, 55,
		13, 55, -1, -2, 1, -1, 2, -1, 19, -1, 27, -1, 33, -1, 6, 39,
		10, 39, 13, 39, -1, -2, 33, -1, 1, 39, 2, 39, 6, 39, 17, 39,
		27, 39, -1, -2, 11, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7,
		19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 33, 7,
		-1, -2, 1, -1, 2, -1, 19, -1, 27, -1, 33, -1, 10, 81, 20, 81,
		-1, -2, 34, -1, 35, 113, -1, -2, 34, -1, 35, 114, -1, -2, 1, -1,
		2, -1, 19, -1, 27, -1, 33, -1, 6, 39, 10, 39, 13, 39, -1, -2,
		1, -1, 2, -1, 19, -1, 27, -1, 33, -1, 6, 39, 10, 39, 13, 39,
		-1, -2, 6, -1, 10, 55, 13, 55, -1, -2, 23, -1, 24, -1, 25, -1,
		26, -1, 1, 66, 2, 66, 6, 66, 10, 66, 13, 66, 19, 66, 20, 66,
		27, 66, 33, 66, -1, -2, 6, -1, 10, 55, 13, 55, -1, -2, 11, -1,
		16, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 23, 7,
		24, 7, 25, 7, 26, 7, 27, 7, 33, 7, -1, -2, 11, -1, 1, 7,
		2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7,
		25, 7, 26, 7, 27, 7, 33, 7, -1, -2, 11, -1, 1, 7, 2, 7,
		6, 7, 10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7,
		26, 7, 27, 7, 33, 7, -1, -2, 6, -1, 10, 55, 13, 55, -1, -2,
		23, -1, 24, -1, 25, -1, 26, 77, 1, 77, 2, 77, 6, 77, 10, 77,
		13, 77, 19, 77, 20, 77, 27, 77, 33, 77, -1, -2, 11, -1, 1, 7,
		2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7,
		25, 7, 26, 7, 27, 7, 33, 7, -1, -2, 1, -1, 2, -1, 19, -1,
		27, -1, 33, -1, 10, 82, 20, 82, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 23, 70, 73, 81, 91, 99, 99, 99, 102, 105, 115, 117, 120, 125,
		131, 138, 153, 159, 181, 188, 192, 196, 203, 206, 213, 220, 242, 249, 256, 257,
		258, 258, 270, 273, 275, 276, 277, 279, 286, 316, 320, 322, 326, 329, 331, 335,
		336, 337, 339, 343, 344, 346, 349, 352, 358, 369, 370, 387, 404, 422, 429, 430,
		431, 433, 440, 447, 451, 463, 465, 468, 489, 490, 494, 495, 496, 503, 507, 508,
		509, 511
	};

	private static final short lapg_sym_from[] = {
		196, 197, 0, 1, 5, 8, 14, 21, 25, 31, 34, 50, 78, 93, 94, 101,
		117, 123, 133, 141, 143, 162, 188, 0, 1, 5, 8, 14, 21, 25, 26, 30,
		31, 34, 36, 43, 44, 50, 57, 64, 68, 78, 85, 92, 93, 94, 101, 102,
		111, 116, 117, 119, 120, 123, 126, 130, 133, 135, 136, 141, 143, 151, 157, 162,
		167, 178, 180, 183, 188, 193, 21, 66, 67, 1, 14, 21, 26, 78, 93, 101,
		162, 1, 4, 14, 21, 23, 78, 89, 93, 101, 162, 8, 25, 34, 50, 122,
		144, 147, 174, 71, 95, 96, 114, 134, 161, 3, 29, 80, 110, 125, 132, 152,
		155, 169, 181, 80, 125, 92, 114, 161, 19, 26, 57, 64, 85, 26, 41, 57,
		64, 81, 85, 28, 71, 80, 110, 119, 125, 152, 0, 1, 5, 8, 14, 21,
		25, 26, 57, 64, 78, 85, 93, 101, 162, 23, 26, 42, 57, 64, 85, 7,
		18, 26, 32, 57, 64, 69, 73, 76, 85, 94, 106, 111, 116, 117, 123, 133,
		141, 143, 151, 167, 188, 52, 64, 82, 86, 109, 134, 163, 26, 57, 64, 85,
		26, 57, 64, 85, 26, 57, 64, 85, 118, 145, 177, 118, 145, 177, 26, 57,
		64, 85, 118, 145, 177, 26, 57, 64, 85, 118, 145, 177, 8, 25, 26, 34,
		37, 50, 57, 64, 85, 94, 111, 116, 117, 120, 123, 133, 136, 141, 143, 151,
		167, 188, 1, 14, 21, 78, 93, 101, 162, 1, 14, 21, 78, 93, 101, 162,
		158, 158, 94, 111, 116, 117, 123, 128, 133, 141, 143, 151, 167, 188, 112, 137,
		138, 140, 170, 0, 0, 0, 5, 0, 5, 8, 25, 34, 36, 50, 1, 14,
		21, 68, 78, 92, 93, 94, 101, 111, 116, 117, 119, 120, 123, 130, 133, 135,
		136, 141, 143, 151, 157, 162, 167, 178, 180, 183, 188, 193, 7, 32, 73, 76,
		26, 57, 26, 57, 64, 85, 21, 66, 67, 0, 5, 0, 5, 8, 25, 106,
		4, 8, 25, 8, 25, 34, 50, 68, 94, 123, 94, 123, 141, 94, 123, 141,
		94, 111, 116, 123, 141, 167, 94, 111, 116, 117, 123, 133, 141, 143, 151, 167,
		188, 111, 8, 25, 34, 50, 94, 111, 116, 117, 120, 123, 133, 136, 141, 143,
		151, 167, 188, 8, 25, 34, 50, 94, 111, 116, 117, 120, 123, 133, 136, 141,
		143, 151, 167, 188, 8, 25, 34, 37, 50, 94, 111, 116, 117, 120, 123, 133,
		136, 141, 143, 151, 167, 188, 1, 14, 21, 78, 93, 101, 162, 14, 43, 80,
		125, 1, 14, 21, 78, 93, 101, 162, 1, 14, 21, 78, 93, 101, 162, 122,
		144, 147, 174, 94, 111, 116, 117, 123, 128, 133, 141, 143, 151, 167, 188, 112,
		137, 112, 137, 138, 0, 1, 5, 8, 14, 21, 25, 31, 34, 50, 78, 93,
		94, 101, 117, 123, 133, 141, 143, 162, 188, 57, 7, 32, 73, 76, 89, 106,
		94, 116, 117, 123, 128, 141, 143, 122, 144, 147, 174, 43, 14, 112, 137
	};

	private static final short lapg_sym_to[] = {
		198, 199, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 51, 68,
		69, 72, 72, 80, 83, 72, 51, 51, 90, 11, 51, 90, 11, 110, 11, 125,
		132, 132, 132, 152, 155, 110, 163, 90, 132, 169, 155, 110, 132, 132, 181, 11,
		132, 90, 90, 90, 132, 90, 45, 45, 45, 12, 12, 12, 52, 12, 12, 12,
		12, 13, 22, 13, 13, 48, 13, 105, 13, 13, 13, 30, 30, 30, 30, 158,
		158, 158, 158, 94, 94, 123, 141, 167, 141, 21, 67, 98, 130, 98, 130, 178,
		180, 178, 193, 99, 99, 107, 142, 185, 44, 53, 53, 53, 53, 54, 78, 54,
		54, 102, 54, 66, 66, 100, 131, 153, 100, 179, 4, 14, 4, 4, 14, 14,
		4, 55, 55, 55, 14, 55, 14, 14, 14, 49, 56, 79, 56, 56, 56, 26,
		43, 57, 26, 57, 57, 93, 26, 26, 57, 111, 126, 111, 111, 111, 111, 111,
		111, 111, 111, 111, 111, 84, 87, 103, 104, 129, 168, 187, 58, 58, 58, 58,
		59, 59, 59, 59, 60, 60, 60, 60, 148, 148, 148, 149, 149, 149, 61, 61,
		61, 61, 150, 150, 150, 62, 62, 62, 62, 151, 151, 151, 31, 31, 63, 31,
		31, 31, 63, 63, 63, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16,
		183, 184, 112, 112, 112, 112, 112, 112, 112, 112, 112, 112, 112, 112, 137, 137,
		137, 172, 189, 196, 5, 6, 24, 7, 7, 32, 32, 73, 76, 73, 17, 17,
		17, 91, 17, 108, 17, 113, 17, 113, 113, 113, 154, 156, 113, 166, 113, 154,
		156, 113, 113, 113, 182, 17, 113, 191, 192, 194, 113, 195, 27, 27, 27, 27,
		64, 85, 65, 65, 88, 88, 46, 89, 46, 8, 25, 9, 9, 33, 33, 127,
		23, 34, 50, 35, 35, 74, 74, 92, 114, 161, 115, 115, 173, 116, 116, 116,
		117, 133, 143, 117, 117, 188, 118, 118, 118, 145, 118, 145, 118, 145, 177, 118,
		145, 134, 36, 36, 36, 36, 119, 135, 135, 135, 157, 119, 135, 157, 119, 135,
		135, 135, 135, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 38, 38, 38, 77, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 197, 40, 47, 97, 109, 124, 186, 41, 81, 101,
		162, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 159,
		159, 159, 159, 120, 136, 120, 120, 120, 164, 136, 120, 120, 136, 136, 136, 138,
		138, 139, 139, 171, 10, 20, 10, 39, 20, 20, 39, 70, 75, 75, 20, 20,
		121, 20, 146, 121, 146, 121, 146, 20, 146, 86, 28, 71, 95, 96, 106, 128,
		122, 144, 147, 122, 165, 122, 174, 160, 175, 176, 190, 82, 42, 140, 170
	};

	private static final short lapg_rlen[] = {
		3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3, 3,
		0, 1, 3, 0, 1, 0, 1, 0, 1, 7, 3, 1, 2, 1, 2, 2,
		5, 6, 4, 1, 2, 1, 3, 0, 1, 4, 3, 3, 2, 1, 2, 3,
		2, 1, 2, 2, 5, 3, 4, 2, 4, 2, 3, 1, 3, 3, 2, 2,
		2, 1, 3, 1, 1, 2, 2, 5, 2, 1, 1, 1, 1, 1, 0, 1,
		4, 0, 1, 3, 1, 1, 3, 3, 5, 1, 1, 1, 1, 1, 3, 3,
		2, 0, 1, 3, 2, 1, 3, 1
	};

	private static final short lapg_rlex[] = {
		36, 36, 37, 37, 38, 38, 39, 40, 41, 41, 42, 42, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 72, 72, 43, 44, 45, 45, 45, 46, 46,
		73, 73, 46, 74, 74, 75, 75, 76, 76, 46, 47, 48, 48, 49, 49, 49,
		50, 50, 50, 51, 51, 52, 52, 77, 77, 53, 53, 53, 53, 53, 54, 54,
		54, 55, 55, 55, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 57, 57, 58, 59, 59, 60, 60, 60, 61, 61, 61, 61, 61, 78, 78,
		61, 79, 79, 61, 61, 62, 62, 63, 63, 64, 64, 64, 65, 66, 66, 67,
		67, 80, 80, 68, 69, 69, 70, 71
	};

	protected static final String[] lapg_syms = new String[] {
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
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'?'",
		"'&'",
		"'@'",
		"Ltrue",
		"Lfalse",
		"Lprio",
		"Lshift",
		"Lreduce",
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
		"lexem_attr",
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
		"rule_attrs",
		"command",
		"command_tokens",
		"command_token",
		"syntax_problem",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"lexem_attropt",
		"commandopt",
		"rule_attrsopt",
		"map_entriesopt",
		"expression_listopt",
		"command_tokensopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 36;
		public static final int options = 37;
		public static final int option = 38;
		public static final int symbol = 39;
		public static final int reference = 40;
		public static final int type = 41;
		public static final int type_part_list = 42;
		public static final int type_part = 43;
		public static final int pattern = 44;
		public static final int lexer_parts = 45;
		public static final int lexer_part = 46;
		public static final int lexem_attr = 47;
		public static final int icon_list = 48;
		public static final int grammar_parts = 49;
		public static final int grammar_part = 50;
		public static final int references = 51;
		public static final int rules = 52;
		public static final int rule0 = 53;
		public static final int ruleprefix = 54;
		public static final int rulesyms = 55;
		public static final int rulesym = 56;
		public static final int rulesyms_choice = 57;
		public static final int annotations_decl = 58;
		public static final int annotations = 59;
		public static final int annotation = 60;
		public static final int expression = 61;
		public static final int expression_list = 62;
		public static final int map_entries = 63;
		public static final int map_separator = 64;
		public static final int name = 65;
		public static final int qualified_id = 66;
		public static final int rule_attrs = 67;
		public static final int command = 68;
		public static final int command_tokens = 69;
		public static final int command_token = 70;
		public static final int syntax_problem = 71;
		public static final int type_part_listopt = 72;
		public static final int typeopt = 73;
		public static final int iconopt = 74;
		public static final int lexem_attropt = 75;
		public static final int commandopt = 76;
		public static final int rule_attrsopt = 77;
		public static final int map_entriesopt = 78;
		public static final int expression_listopt = 79;
		public static final int command_tokensopt = 80;
	}

	public interface Rules {
		public static final int lexer_part_group_selector = 30;  // lexer_part ::= '[' icon_list ']'
		public static final int lexer_part_alias = 31;  // lexer_part ::= identifier '=' pattern
		public static final int grammar_part_directive = 50;  // grammar_part ::= '%' identifier references ';'
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
	protected LapgLexer lapg_lexer;

	private Object parse(LapgLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = initialState;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != finalState) {
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
						reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, 
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
					lapg_m[0].state = initialState;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, 
					MessageFormat.format("syntax error before line {0}",
					lapg_lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
	}

	protected boolean restore() {
		if (lapg_n.lexem == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 1) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
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
			return true;
		}
		return false;
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
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			cleanup(lapg_m[lapg_head]);
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
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
			case 31:  // lexer_part ::= identifier '=' pattern
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 34:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 41:  // lexer_part ::= symbol typeopt ':' pattern iconopt lexem_attropt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-6].sym), ((String)lapg_m[lapg_head-5].sym), ((AstRegexp)lapg_m[lapg_head-3].sym), ((Integer)lapg_m[lapg_head-2].sym), ((AstLexemAttrs)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 42:  // lexem_attr ::= '(' identifier ')'
				 lapg_gg.sym = lexemAttrs(((String)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 43:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 44:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 45:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 46:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 47:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 48:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 49:  // grammar_part ::= annotations_decl symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 50:  // grammar_part ::= '%' identifier references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 51:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 52:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 53:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 54:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:  // rule0 ::= ruleprefix rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-3].sym), ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // rule0 ::= rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 59:  // rule0 ::= ruleprefix commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-2].sym), null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 60:  // rule0 ::= commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // ruleprefix ::= annotations_decl ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 63:  // ruleprefix ::= annotations_decl identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 64:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 65:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 66:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 67:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 68:  // rulesym ::= command annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // rulesym ::= command annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // rulesym ::= command identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // rulesym ::= command reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-1].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // rulesym ::= annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // rulesym ::= annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // rulesym ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // rulesym ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // rulesym ::= '(' rulesyms_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 77:  // rulesym ::= rulesym '&' rulesym
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 78:  // rulesym ::= rulesym '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 79:  // rulesym ::= rulesym '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 80:  // rulesym ::= rulesym '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 83:  // annotations_decl ::= annotations
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 85:  // annotations ::= annotations annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 86:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 89:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 90:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 91:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 102:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 103:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 104:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 108:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 111:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 119:  // syntax_problem ::= error
				 lapg_gg.sym = new AstError(source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset); 
				break;
		}
	}

	/**
	 *  disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol sym) {
	}

	/**
	 *  cleans node removed from the stack
	 */
	protected void cleanup(LapgSymbol sym) {
	}

	public AstRoot parseInput(LapgLexer lexer) throws IOException, ParseException {
		return (AstRoot) parse(lexer, 0, 198);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 199);
	}
}
