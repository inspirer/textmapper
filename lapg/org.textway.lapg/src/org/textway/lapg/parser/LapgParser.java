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
		-1, -1, 124, -3, -1, -1, 2, -11, -1, 27, 5, -17, 94, 95, -49, 96,
		97, 98, -1, -79, 105, -1, 43, -1, 3, -1, -1, 33, -1, -85, -1, -1,
		-95, 28, -103, 45, 50, -1, -127, 89, 29, 106, -147, -1, -153, -1, 26, 31,
		4, 44, 30, -171, 16, -1, 18, 19, 14, 15, -195, 12, 13, 17, 20, 22,
		21, -1, 11, -235, -1, -1, 51, 52, 53, -1, -261, 93, -1, 6, -283, 46,
		47, -289, 90, -1, 104, -1, -295, -1, 115, 8, -301, -1, 9, 10, -341, 7,
		56, -1, -1, -1, -371, -1, -1, 107, 111, 112, 110, -1, -1, 101, 25, 36,
		-401, 55, 57, 54, -1, -429, -1, -471, 80, -1, 58, -477, -505, -535, -1, -575,
		66, -599, -607, 108, -1, -1, 38, -637, 92, -1, 69, -663, -705, -1, -1, -1,
		-733, -739, 122, -1, -745, 48, -775, -805, -813, 72, -853, 84, 85, 83, -1, -861,
		67, 78, -903, 76, -1, -1, 61, 65, -1, -1, -1, 40, 41, 79, -1, 81,
		-945, -1, 121, 120, 59, -987, 64, 63, -995, -1, 68, -1, -1035, 74, -1, 117,
		49, 109, 42, -1077, 123, 62, 77, 75, -1, 116, 73, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 6, 19, 6, -1, -2, 19, -1, 16, 32, -1, -2, 0, 7,
		1, 7, 2, 7, 15, 7, 17, 7, 18, 7, 20, 7, 30, 7, 31, 7,
		32, 7, 33, 7, 34, 7, 35, 7, 14, 114, 19, 114, -1, -2, 1, -1,
		2, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 4, -1,
		5, -1, 17, -1, 28, -1, 29, -1, 18, 102, -1, -2, 14, -1, 19, 113,
		-1, -2, 11, -1, 9, 6, 16, 6, 19, 6, -1, -2, 19, -1, 9, 32,
		16, 32, -1, -2, 1, -1, 2, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 6, -1, 27, -1, 0, 1, -1, -2, 27, -1, 2, 88,
		16, 88, 30, 88, 31, 88, 32, 88, 33, 88, 34, 88, 35, 88, -1, -2,
		15, -1, 18, 103, -1, -2, 2, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 20, 99, -1, -2, 1, -1, 2, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 30, -1, 6, -1, 27, -1, 0, 0, -1, -2,
		2, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 14, -1,
		15, -1, 17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1,
		26, -1, 27, -1, 20, 23, -1, -2, 3, -1, 1, 34, 2, 34, 6, 34,
		17, 34, 27, 34, 30, 34, 31, 34, 32, 34, 33, 34, 34, 34, 35, 34,
		-1, -2, 19, -1, 2, 91, 16, 91, 27, 91, 30, 91, 31, 91, 32, 91,
		33, 91, 34, 91, 35, 91, -1, -2, 19, -1, 9, 32, -1, -2, 19, -1,
		9, 32, -1, -2, 15, -1, 20, 100, -1, -2, 2, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 30, -1, 14, -1, 15, -1, 17, -1, 18, -1,
		19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1, 27, -1, 20, 24,
		-1, -2, 5, -1, 1, 35, 2, 35, 6, 35, 17, 35, 19, 35, 27, 35,
		30, 35, 31, 35, 32, 35, 33, 35, 34, 35, 35, 35, 37, 35, -1, -2,
		1, -1, 2, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1,
		19, -1, 27, -1, 37, -1, 6, 39, 10, 39, 13, 39, -1, -2, 19, -1,
		1, 37, 2, 37, 6, 37, 17, 37, 27, 37, 30, 37, 31, 37, 32, 37,
		33, 37, 34, 37, 35, 37, 37, 37, -1, -2, 11, -1, 16, -1, 1, 7,
		2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 23, 7, 24, 7, 25, 7,
		26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7, 35, 7,
		37, 7, -1, -2, 38, -1, 39, 118, -1, -2, 2, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 37, -1, 6, 39,
		10, 39, 13, 39, -1, -2, 1, -1, 2, -1, 35, -1, 34, -1, 33, -1,
		32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 37, -1, 6, 39, 10, 39,
		13, 39, -1, -2, 23, -1, 24, -1, 25, -1, 26, -1, 1, 70, 2, 70,
		6, 70, 10, 70, 13, 70, 19, 70, 20, 70, 27, 70, 30, 70, 31, 70,
		32, 70, 33, 70, 34, 70, 35, 70, 37, 70, -1, -2, 2, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 27, -1, 6, 40, 10, 40,
		13, 40, -1, -2, 6, -1, 10, 60, 13, 60, -1, -2, 1, -1, 2, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1,
		37, -1, 6, 39, 10, 39, 13, 39, -1, -2, 37, -1, 1, 39, 2, 39,
		6, 39, 17, 39, 27, 39, 30, 39, 31, 39, 32, 39, 33, 39, 34, 39,
		35, 39, -1, -2, 11, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7,
		19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 30, 7,
		31, 7, 32, 7, 33, 7, 34, 7, 35, 7, 37, 7, -1, -2, 1, -1,
		2, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1,
		27, -1, 37, -1, 10, 86, 20, 86, -1, -2, 38, -1, 39, 118, -1, -2,
		38, -1, 39, 119, -1, -2, 1, -1, 2, -1, 35, -1, 34, -1, 33, -1,
		32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 37, -1, 6, 39, 10, 39,
		13, 39, -1, -2, 1, -1, 2, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 19, -1, 27, -1, 37, -1, 6, 39, 10, 39, 13, 39,
		-1, -2, 6, -1, 10, 60, 13, 60, -1, -2, 23, -1, 24, -1, 25, -1,
		26, -1, 1, 71, 2, 71, 6, 71, 10, 71, 13, 71, 19, 71, 20, 71,
		27, 71, 30, 71, 31, 71, 32, 71, 33, 71, 34, 71, 35, 71, 37, 71,
		-1, -2, 6, -1, 10, 60, 13, 60, -1, -2, 11, -1, 16, -1, 1, 7,
		2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 23, 7, 24, 7, 25, 7,
		26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7, 35, 7,
		37, 7, -1, -2, 11, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7,
		19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 30, 7,
		31, 7, 32, 7, 33, 7, 34, 7, 35, 7, 37, 7, -1, -2, 11, -1,
		1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 20, 7, 23, 7,
		24, 7, 25, 7, 26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7,
		34, 7, 35, 7, 37, 7, -1, -2, 6, -1, 10, 60, 13, 60, -1, -2,
		23, -1, 24, -1, 25, -1, 26, 82, 1, 82, 2, 82, 6, 82, 10, 82,
		13, 82, 19, 82, 20, 82, 27, 82, 30, 82, 31, 82, 32, 82, 33, 82,
		34, 82, 35, 82, 37, 82, -1, -2, 11, -1, 1, 7, 2, 7, 6, 7,
		10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 26, 7,
		27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7, 35, 7, 37, 7,
		-1, -2, 1, -1, 2, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		30, -1, 19, -1, 27, -1, 37, -1, 10, 87, 20, 87, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 23, 71, 74, 82, 92, 100, 100, 100, 103, 106, 116, 118, 122, 127,
		133, 140, 155, 161, 183, 190, 194, 198, 205, 208, 215, 222, 244, 251, 258, 307,
		356, 405, 454, 503, 552, 552, 564, 567, 569, 570, 571, 573, 580, 612, 616, 618,
		622, 625, 627, 631, 632, 633, 635, 639, 640, 644, 646, 648, 651, 654, 660, 671,
		672, 689, 706, 724, 731, 732, 733, 735, 742, 749, 753, 765, 767, 770, 791, 792,
		796, 797, 798, 805, 809, 810, 811, 813
	};

	private static final short lapg_sym_from[] = {
		203, 204, 0, 1, 5, 8, 14, 21, 25, 31, 34, 51, 83, 99, 100, 107,
		124, 130, 140, 148, 150, 169, 195, 0, 1, 5, 8, 14, 21, 25, 26, 31,
		34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 97, 98, 99, 100, 107,
		108, 118, 123, 124, 126, 127, 130, 133, 137, 140, 142, 143, 148, 150, 158, 164,
		169, 174, 185, 187, 190, 195, 200, 21, 67, 68, 1, 14, 21, 26, 83, 99,
		107, 169, 1, 4, 14, 21, 23, 83, 94, 99, 107, 169, 8, 25, 34, 51,
		129, 151, 154, 181, 76, 101, 102, 121, 141, 168, 3, 29, 85, 117, 132, 139,
		159, 162, 176, 188, 85, 132, 97, 98, 121, 168, 19, 26, 58, 65, 90, 26,
		42, 58, 65, 86, 90, 28, 76, 85, 117, 126, 132, 159, 0, 1, 5, 8,
		14, 21, 25, 26, 58, 65, 83, 90, 99, 107, 169, 23, 26, 43, 58, 65,
		90, 7, 18, 26, 32, 58, 65, 74, 78, 81, 90, 100, 112, 118, 123, 124,
		130, 140, 148, 150, 158, 174, 195, 53, 65, 87, 91, 116, 141, 170, 26, 58,
		65, 90, 26, 58, 65, 90, 26, 58, 65, 90, 125, 152, 184, 125, 152, 184,
		26, 58, 65, 90, 125, 152, 184, 26, 58, 65, 90, 125, 152, 184, 8, 25,
		26, 34, 38, 51, 58, 65, 90, 100, 118, 123, 124, 127, 130, 140, 143, 148,
		150, 158, 174, 195, 1, 14, 21, 83, 99, 107, 169, 1, 14, 21, 83, 99,
		107, 169, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 44, 45, 51,
		58, 65, 69, 73, 83, 90, 97, 98, 99, 100, 107, 108, 118, 123, 124, 126,
		127, 130, 133, 137, 140, 142, 143, 148, 150, 158, 164, 165, 169, 174, 185, 187,
		190, 195, 200, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 44, 45,
		51, 58, 65, 69, 73, 83, 90, 97, 98, 99, 100, 107, 108, 118, 123, 124,
		126, 127, 130, 133, 137, 140, 142, 143, 148, 150, 158, 164, 165, 169, 174, 185,
		187, 190, 195, 200, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37,
		44, 45, 51, 58, 65, 69, 73, 83, 90, 97, 98, 99, 100, 107, 108, 118,
		123, 124, 126, 127, 130, 133, 137, 140, 142, 143, 148, 150, 158, 164, 169, 174,
		185, 187, 190, 195, 200, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34,
		37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 97, 98, 99, 100, 107, 108,
		118, 123, 124, 126, 127, 130, 133, 137, 140, 142, 143, 148, 150, 158, 164, 169,
		174, 185, 187, 190, 195, 200, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31,
		34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 97, 98, 99, 100, 107,
		108, 118, 123, 124, 126, 127, 130, 133, 137, 140, 142, 143, 148, 150, 158, 164,
		169, 174, 185, 187, 190, 195, 200, 0, 1, 5, 8, 14, 21, 25, 26, 30,
		31, 34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 97, 98, 99, 100,
		107, 108, 118, 123, 124, 126, 127, 130, 133, 137, 140, 142, 143, 148, 150, 158,
		164, 169, 174, 185, 187, 190, 195, 200, 100, 118, 123, 124, 130, 135, 140, 148,
		150, 158, 174, 195, 119, 144, 145, 147, 177, 0, 0, 0, 5, 0, 5, 8,
		25, 34, 37, 51, 1, 14, 21, 69, 73, 83, 97, 98, 99, 100, 107, 118,
		123, 124, 126, 127, 130, 137, 140, 142, 143, 148, 150, 158, 164, 169, 174, 185,
		187, 190, 195, 200, 7, 32, 78, 81, 26, 58, 26, 58, 65, 90, 21, 67,
		68, 0, 5, 0, 5, 8, 25, 112, 4, 8, 25, 8, 25, 34, 51, 30,
		8, 25, 34, 51, 69, 73, 100, 130, 100, 130, 148, 100, 130, 148, 100, 118,
		123, 130, 148, 174, 100, 118, 123, 124, 130, 140, 148, 150, 158, 174, 195, 118,
		8, 25, 34, 51, 100, 118, 123, 124, 127, 130, 140, 143, 148, 150, 158, 174,
		195, 8, 25, 34, 51, 100, 118, 123, 124, 127, 130, 140, 143, 148, 150, 158,
		174, 195, 8, 25, 34, 38, 51, 100, 118, 123, 124, 127, 130, 140, 143, 148,
		150, 158, 174, 195, 1, 14, 21, 83, 99, 107, 169, 14, 44, 85, 132, 1,
		14, 21, 83, 99, 107, 169, 1, 14, 21, 83, 99, 107, 169, 129, 151, 154,
		181, 100, 118, 123, 124, 130, 135, 140, 148, 150, 158, 174, 195, 119, 144, 119,
		144, 145, 0, 1, 5, 8, 14, 21, 25, 31, 34, 51, 83, 99, 100, 107,
		124, 130, 140, 148, 150, 169, 195, 58, 7, 32, 78, 81, 94, 112, 100, 123,
		124, 130, 135, 148, 150, 129, 151, 154, 181, 44, 14, 119, 144
	};

	private static final short lapg_sym_to[] = {
		205, 206, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 52, 74,
		77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 95, 11, 117, 11,
		132, 139, 139, 139, 159, 162, 117, 170, 95, 139, 176, 162, 117, 139, 139, 188,
		11, 139, 95, 95, 95, 139, 95, 46, 46, 46, 12, 12, 12, 53, 12, 12,
		12, 12, 13, 22, 13, 13, 49, 13, 111, 13, 13, 13, 30, 30, 30, 30,
		165, 165, 165, 165, 100, 100, 130, 148, 174, 148, 21, 68, 104, 137, 104, 137,
		185, 187, 185, 200, 105, 105, 113, 115, 149, 192, 45, 54, 54, 54, 54, 55,
		83, 55, 55, 108, 55, 67, 67, 106, 138, 160, 106, 186, 4, 14, 4, 4,
		14, 14, 4, 56, 56, 56, 14, 56, 14, 14, 14, 50, 57, 84, 57, 57,
		57, 26, 44, 58, 26, 58, 58, 99, 26, 26, 58, 118, 133, 118, 118, 118,
		118, 118, 118, 118, 118, 118, 118, 89, 92, 109, 110, 136, 175, 194, 59, 59,
		59, 59, 60, 60, 60, 60, 61, 61, 61, 61, 155, 155, 155, 156, 156, 156,
		62, 62, 62, 62, 157, 157, 157, 63, 63, 63, 63, 158, 158, 158, 31, 31,
		64, 31, 31, 31, 64, 64, 64, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16,
		16, 16, 3, 11, 3, 29, 11, 11, 29, 52, 74, 77, 77, 85, 88, 77,
		52, 52, 95, 95, 11, 52, 95, 95, 11, 117, 11, 132, 139, 139, 139, 159,
		162, 117, 170, 95, 139, 176, 162, 117, 139, 139, 188, 190, 11, 139, 95, 95,
		95, 139, 95, 3, 11, 3, 29, 11, 11, 29, 52, 74, 77, 77, 85, 88,
		77, 52, 52, 95, 95, 11, 52, 95, 95, 11, 117, 11, 132, 139, 139, 139,
		159, 162, 117, 170, 95, 139, 176, 162, 117, 139, 139, 188, 191, 11, 139, 95,
		95, 95, 139, 95, 3, 11, 3, 29, 11, 11, 29, 52, 69, 74, 77, 77,
		85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 95, 11, 117, 11, 132, 139,
		139, 139, 159, 162, 117, 170, 95, 139, 176, 162, 117, 139, 139, 188, 11, 139,
		95, 95, 95, 139, 95, 3, 11, 3, 29, 11, 11, 29, 52, 70, 74, 77,
		77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 95, 11, 117, 11, 132,
		139, 139, 139, 159, 162, 117, 170, 95, 139, 176, 162, 117, 139, 139, 188, 11,
		139, 95, 95, 95, 139, 95, 3, 11, 3, 29, 11, 11, 29, 52, 71, 74,
		77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 95, 11, 117, 11,
		132, 139, 139, 139, 159, 162, 117, 170, 95, 139, 176, 162, 117, 139, 139, 188,
		11, 139, 95, 95, 95, 139, 95, 3, 11, 3, 29, 11, 11, 29, 52, 72,
		74, 77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 95, 11, 117,
		11, 132, 139, 139, 139, 159, 162, 117, 170, 95, 139, 176, 162, 117, 139, 139,
		188, 11, 139, 95, 95, 95, 139, 95, 119, 119, 119, 119, 119, 119, 119, 119,
		119, 119, 119, 119, 144, 144, 144, 179, 196, 203, 5, 6, 24, 7, 7, 32,
		32, 78, 81, 78, 17, 17, 17, 96, 96, 17, 114, 114, 17, 120, 17, 120,
		120, 120, 161, 163, 120, 173, 120, 161, 163, 120, 120, 120, 189, 17, 120, 198,
		199, 201, 120, 202, 27, 27, 27, 27, 65, 90, 66, 66, 93, 93, 47, 94,
		47, 8, 25, 9, 9, 33, 33, 134, 23, 34, 51, 35, 35, 79, 79, 73,
		36, 36, 36, 36, 97, 98, 121, 168, 122, 122, 180, 123, 123, 123, 124, 140,
		150, 124, 124, 195, 125, 125, 125, 152, 125, 152, 125, 152, 184, 125, 152, 141,
		37, 37, 37, 37, 126, 142, 142, 142, 164, 126, 142, 164, 126, 142, 142, 142,
		142, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 39, 39, 39, 82, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 204, 41, 48, 103, 116, 131, 193, 42, 86, 107, 169, 18,
		18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 166, 166, 166,
		166, 127, 143, 127, 127, 127, 171, 143, 127, 127, 143, 143, 143, 145, 145, 146,
		146, 178, 10, 20, 10, 40, 20, 20, 40, 75, 80, 80, 20, 20, 128, 20,
		153, 128, 153, 128, 153, 20, 153, 91, 28, 76, 101, 102, 112, 135, 129, 151,
		154, 129, 172, 129, 181, 167, 182, 183, 197, 87, 43, 147, 177
	};

	private static final short lapg_rlen[] = {
		3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3, 3,
		0, 1, 3, 0, 1, 0, 1, 0, 1, 7, 3, 1, 2, 1, 2, 2,
		5, 6, 1, 1, 1, 1, 4, 4, 1, 2, 1, 3, 0, 1, 4, 3,
		3, 2, 1, 2, 3, 2, 1, 2, 2, 5, 3, 4, 2, 4, 2, 3,
		1, 3, 3, 2, 2, 2, 1, 3, 1, 1, 2, 2, 5, 2, 1, 1,
		1, 1, 1, 0, 1, 4, 0, 1, 3, 1, 1, 3, 3, 5, 1, 1,
		1, 1, 1, 3, 3, 2, 0, 1, 3, 2, 1, 3, 1
	};

	private static final short lapg_rlex[] = {
		40, 40, 41, 41, 42, 42, 43, 44, 45, 45, 46, 46, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 78, 78, 47, 48, 49, 49, 49, 50, 50,
		79, 79, 50, 80, 80, 81, 81, 82, 82, 50, 51, 52, 52, 53, 53, 53,
		54, 54, 54, 55, 55, 55, 56, 56, 57, 57, 58, 58, 83, 83, 59, 59,
		59, 59, 59, 60, 60, 60, 61, 61, 61, 62, 62, 62, 62, 62, 62, 62,
		62, 62, 62, 62, 62, 62, 63, 63, 64, 65, 65, 66, 66, 66, 67, 67,
		67, 67, 67, 84, 84, 67, 85, 85, 67, 67, 68, 68, 69, 69, 70, 70,
		70, 71, 72, 72, 73, 73, 86, 86, 74, 75, 75, 76, 77
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
		"Linput",
		"Lleft",
		"Lright",
		"Lnonassoc",
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
		"priority_kw",
		"directive",
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
		public static final int input = 40;
		public static final int options = 41;
		public static final int option = 42;
		public static final int symbol = 43;
		public static final int reference = 44;
		public static final int type = 45;
		public static final int type_part_list = 46;
		public static final int type_part = 47;
		public static final int pattern = 48;
		public static final int lexer_parts = 49;
		public static final int lexer_part = 50;
		public static final int lexem_attr = 51;
		public static final int icon_list = 52;
		public static final int grammar_parts = 53;
		public static final int grammar_part = 54;
		public static final int priority_kw = 55;
		public static final int directive = 56;
		public static final int references = 57;
		public static final int rules = 58;
		public static final int rule0 = 59;
		public static final int ruleprefix = 60;
		public static final int rulesyms = 61;
		public static final int rulesym = 62;
		public static final int rulesyms_choice = 63;
		public static final int annotations_decl = 64;
		public static final int annotations = 65;
		public static final int annotation = 66;
		public static final int expression = 67;
		public static final int expression_list = 68;
		public static final int map_entries = 69;
		public static final int map_separator = 70;
		public static final int name = 71;
		public static final int qualified_id = 72;
		public static final int rule_attrs = 73;
		public static final int command = 74;
		public static final int command_tokens = 75;
		public static final int command_token = 76;
		public static final int syntax_problem = 77;
		public static final int type_part_listopt = 78;
		public static final int typeopt = 79;
		public static final int iconopt = 80;
		public static final int lexem_attropt = 81;
		public static final int commandopt = 82;
		public static final int rule_attrsopt = 83;
		public static final int map_entriesopt = 84;
		public static final int expression_listopt = 85;
		public static final int command_tokensopt = 86;
	}

	public interface Rules {
		public static final int lexer_part_group_selector = 30;  // lexer_part ::= '[' icon_list ']'
		public static final int lexer_part_alias = 31;  // lexer_part ::= identifier '=' pattern
		public static final int grammar_part_directive = 50;  // grammar_part ::= directive
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
			case 50:  // grammar_part ::= directive
				 lapg_gg.sym = lapg_m[lapg_head-0].sym; 
				break;
			case 54:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // directive ::= '%' Linput references ';'
				 lapg_gg.sym = new AstDirective("input", ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 56:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 58:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 59:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // rule0 ::= ruleprefix rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-3].sym), ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 63:  // rule0 ::= rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 64:  // rule0 ::= ruleprefix commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-2].sym), null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 65:  // rule0 ::= commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 67:  // ruleprefix ::= annotations_decl ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 68:  // ruleprefix ::= annotations_decl identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 69:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 70:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 71:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 72:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 73:  // rulesym ::= command annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // rulesym ::= command annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // rulesym ::= command identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // rulesym ::= command reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-1].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // rulesym ::= annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // rulesym ::= annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // rulesym ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // rulesym ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // rulesym ::= '(' rulesyms_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 82:  // rulesym ::= rulesym '&' rulesym
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 83:  // rulesym ::= rulesym '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 84:  // rulesym ::= rulesym '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 85:  // rulesym ::= rulesym '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 88:  // annotations_decl ::= annotations
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 90:  // annotations ::= annotations annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 91:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 94:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 107:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 108:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 109:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 113:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 116:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 124:  // syntax_problem ::= error
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
		return (AstRoot) parse(lexer, 0, 205);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 206);
	}
}
