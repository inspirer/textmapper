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
		-1, -1, 126, -3, -1, -1, 4, -11, -17, 29, 7, -45, 101, 102, -79, 103,
		104, 105, -1, -111, 112, -1, 45, -1, 5, -117, -1, 35, -1, -145, -1, -1,
		-155, 30, -163, 47, 52, -1, -189, 96, 31, 3, 113, -211, -1, -217, -1, 28,
		33, 6, 46, 32, 2, 18, -1, 20, 21, 16, 17, -237, 14, 15, 19, 22,
		24, 23, -1, 13, -279, -1, -1, 53, 54, 55, -1, -309, 100, -1, 8, -333,
		48, 49, -339, 97, -1, 111, -1, -345, -1, 122, 10, -351, -1, 11, 12, -393,
		9, -427, -1, 58, 63, -1, -1, -435, -1, -1, 114, 118, 119, 117, -1, -1,
		108, 27, 38, -467, 61, 62, 57, -1, 56, 64, -1, -499, -1, 125, 87, -1,
		65, -543, -573, -605, -1, -647, 73, -673, -681, 115, -1, -1, 40, -713, 59, 99,
		-1, 76, -743, -787, -1, -1, -1, -817, 50, -849, -881, -889, 79, -931, 91, 92,
		90, -1, -939, 74, 85, -983, 83, -1, -1, 68, 72, -1, -1, -1, 42, 43,
		86, -1, 88, -1027, 66, -1071, 71, 70, -1079, -1, 75, -1, -1121, 81, -1, 124,
		51, 116, 44, -1165, 69, 84, 82, -1, 123, 80, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 8, 19, 8, -1, -2, 19, -1, 16, 34, -1, -2, 1, -1,
		2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1,
		6, -1, 17, -1, 27, -1, 0, 0, -1, -2, 0, 9, 1, 9, 2, 9,
		15, 9, 17, 9, 18, 9, 20, 9, 30, 9, 31, 9, 32, 9, 33, 9,
		34, 9, 35, 9, 36, 9, 14, 121, 19, 121, -1, -2, 1, -1, 2, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 4, -1,
		5, -1, 17, -1, 28, -1, 29, -1, 18, 109, -1, -2, 14, -1, 19, 120,
		-1, -2, 1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 6, -1, 17, -1, 27, -1, 0, 0, -1, -2, 11, -1,
		9, 8, 16, 8, 19, 8, -1, -2, 19, -1, 9, 34, 16, 34, -1, -2,
		1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		30, -1, 6, -1, 27, -1, 0, 1, -1, -2, 27, -1, 2, 95, 16, 95,
		30, 95, 31, 95, 32, 95, 33, 95, 34, 95, 35, 95, 36, 95, -1, -2,
		15, -1, 18, 110, -1, -2, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1,
		32, -1, 31, -1, 30, -1, 20, 106, -1, -2, 2, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 14, -1, 15, -1, 17, -1,
		18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1, 27, -1,
		20, 25, -1, -2, 3, -1, 0, 36, 1, 36, 2, 36, 6, 36, 17, 36,
		27, 36, 30, 36, 31, 36, 32, 36, 33, 36, 34, 36, 35, 36, 36, 36,
		-1, -2, 19, -1, 2, 98, 16, 98, 27, 98, 30, 98, 31, 98, 32, 98,
		33, 98, 34, 98, 35, 98, 36, 98, -1, -2, 19, -1, 9, 34, -1, -2,
		19, -1, 9, 34, -1, -2, 15, -1, 20, 107, -1, -2, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 14, -1, 15, -1,
		17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1,
		27, -1, 20, 26, -1, -2, 5, -1, 0, 37, 1, 37, 2, 37, 6, 37,
		17, 37, 19, 37, 27, 37, 30, 37, 31, 37, 32, 37, 33, 37, 34, 37,
		35, 37, 36, 37, 38, 37, -1, -2, 36, -1, 13, 60, 15, 60, -1, -2,
		1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		30, -1, 19, -1, 27, -1, 38, -1, 6, 41, 10, 41, 13, 41, -1, -2,
		19, -1, 0, 39, 1, 39, 2, 39, 6, 39, 17, 39, 27, 39, 30, 39,
		31, 39, 32, 39, 33, 39, 34, 39, 35, 39, 36, 39, 38, 39, -1, -2,
		11, -1, 16, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9, 19, 9,
		23, 9, 24, 9, 25, 9, 26, 9, 27, 9, 30, 9, 31, 9, 32, 9,
		33, 9, 34, 9, 35, 9, 36, 9, 38, 9, -1, -2, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1,
		38, -1, 6, 41, 10, 41, 13, 41, -1, -2, 1, -1, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1,
		38, -1, 6, 41, 10, 41, 13, 41, -1, -2, 23, -1, 24, -1, 25, -1,
		26, -1, 1, 77, 2, 77, 6, 77, 10, 77, 13, 77, 19, 77, 20, 77,
		27, 77, 30, 77, 31, 77, 32, 77, 33, 77, 34, 77, 35, 77, 36, 77,
		38, 77, -1, -2, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 27, -1, 6, 42, 10, 42, 13, 42, -1, -2, 6, -1,
		10, 67, 13, 67, -1, -2, 1, -1, 2, -1, 36, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 38, -1, 6, 41,
		10, 41, 13, 41, -1, -2, 38, -1, 0, 41, 1, 41, 2, 41, 6, 41,
		17, 41, 27, 41, 30, 41, 31, 41, 32, 41, 33, 41, 34, 41, 35, 41,
		36, 41, -1, -2, 11, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9,
		19, 9, 20, 9, 23, 9, 24, 9, 25, 9, 26, 9, 27, 9, 30, 9,
		31, 9, 32, 9, 33, 9, 34, 9, 35, 9, 36, 9, 38, 9, -1, -2,
		1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		30, -1, 19, -1, 27, -1, 38, -1, 10, 93, 20, 93, -1, -2, 1, -1,
		2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1,
		19, -1, 27, -1, 38, -1, 6, 41, 10, 41, 13, 41, -1, -2, 1, -1,
		2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1,
		19, -1, 27, -1, 38, -1, 6, 41, 10, 41, 13, 41, -1, -2, 6, -1,
		10, 67, 13, 67, -1, -2, 23, -1, 24, -1, 25, -1, 26, -1, 1, 78,
		2, 78, 6, 78, 10, 78, 13, 78, 19, 78, 20, 78, 27, 78, 30, 78,
		31, 78, 32, 78, 33, 78, 34, 78, 35, 78, 36, 78, 38, 78, -1, -2,
		6, -1, 10, 67, 13, 67, -1, -2, 11, -1, 16, -1, 1, 9, 2, 9,
		6, 9, 10, 9, 13, 9, 19, 9, 23, 9, 24, 9, 25, 9, 26, 9,
		27, 9, 30, 9, 31, 9, 32, 9, 33, 9, 34, 9, 35, 9, 36, 9,
		38, 9, -1, -2, 11, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9,
		19, 9, 20, 9, 23, 9, 24, 9, 25, 9, 26, 9, 27, 9, 30, 9,
		31, 9, 32, 9, 33, 9, 34, 9, 35, 9, 36, 9, 38, 9, -1, -2,
		11, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9, 19, 9, 20, 9,
		23, 9, 24, 9, 25, 9, 26, 9, 27, 9, 30, 9, 31, 9, 32, 9,
		33, 9, 34, 9, 35, 9, 36, 9, 38, 9, -1, -2, 6, -1, 10, 67,
		13, 67, -1, -2, 23, -1, 24, -1, 25, -1, 26, 89, 1, 89, 2, 89,
		6, 89, 10, 89, 13, 89, 19, 89, 20, 89, 27, 89, 30, 89, 31, 89,
		32, 89, 33, 89, 34, 89, 35, 89, 36, 89, 38, 89, -1, -2, 11, -1,
		1, 9, 2, 9, 6, 9, 10, 9, 13, 9, 19, 9, 20, 9, 23, 9,
		24, 9, 25, 9, 26, 9, 27, 9, 30, 9, 31, 9, 32, 9, 33, 9,
		34, 9, 35, 9, 36, 9, 38, 9, -1, -2, 1, -1, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1,
		38, -1, 10, 94, 20, 94, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 22, 69, 72, 80, 90, 97, 97, 97, 100, 103, 113, 115, 119, 124,
		131, 138, 153, 159, 181, 188, 192, 196, 203, 206, 213, 220, 241, 248, 255, 303,
		351, 399, 447, 495, 543, 591, 591, 603, 604, 605, 607, 613, 645, 649, 651, 655,
		658, 660, 664, 665, 666, 668, 671, 672, 675, 676, 678, 679, 681, 684, 687, 693,
		704, 705, 721, 737, 754, 761, 762, 763, 765, 772, 779, 783, 795, 815, 817, 818,
		822, 823, 824, 831, 832, 836, 837, 838
	};

	private static final short lapg_sym_from[] = {
		202, 203, 0, 1, 5, 8, 14, 21, 25, 31, 34, 84, 102, 103, 110, 130,
		136, 147, 151, 153, 172, 195, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34,
		37, 45, 46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 124,
		129, 130, 132, 133, 136, 139, 144, 147, 149, 150, 151, 153, 161, 167, 172, 177,
		185, 187, 190, 195, 199, 21, 68, 69, 1, 14, 21, 26, 84, 102, 110, 172,
		1, 4, 14, 21, 23, 84, 95, 102, 110, 172, 8, 25, 34, 135, 154, 157,
		181, 77, 104, 105, 127, 148, 171, 3, 29, 86, 123, 138, 146, 162, 165, 179,
		188, 86, 138, 98, 101, 127, 171, 19, 26, 59, 66, 91, 26, 43, 59, 66,
		87, 91, 98, 28, 77, 86, 123, 132, 138, 162, 0, 1, 5, 8, 14, 21,
		25, 26, 59, 66, 84, 91, 102, 110, 172, 23, 26, 44, 59, 66, 91, 7,
		18, 26, 32, 59, 66, 75, 79, 82, 91, 103, 115, 124, 129, 130, 136, 147,
		151, 153, 161, 177, 195, 54, 66, 88, 92, 122, 148, 173, 26, 59, 66, 91,
		26, 59, 66, 91, 26, 59, 66, 91, 131, 155, 184, 131, 155, 184, 26, 59,
		66, 91, 131, 155, 184, 26, 59, 66, 91, 131, 155, 184, 8, 25, 26, 34,
		38, 59, 66, 91, 103, 124, 129, 130, 133, 136, 147, 150, 151, 153, 161, 177,
		195, 1, 14, 21, 84, 102, 110, 172, 1, 14, 21, 84, 102, 110, 172, 0,
		1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70, 74,
		84, 91, 101, 102, 103, 110, 111, 119, 124, 129, 130, 132, 133, 136, 139, 144,
		147, 149, 150, 151, 153, 161, 167, 168, 172, 177, 185, 187, 190, 195, 199, 0,
		1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70, 74,
		84, 91, 101, 102, 103, 110, 111, 119, 124, 129, 130, 132, 133, 136, 139, 144,
		147, 149, 150, 151, 153, 161, 167, 168, 172, 177, 185, 187, 190, 195, 199, 0,
		1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 124, 129, 130, 132, 133, 136, 139,
		144, 147, 149, 150, 151, 153, 161, 167, 172, 177, 185, 187, 190, 195, 199, 0,
		1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 124, 129, 130, 132, 133, 136, 139,
		144, 147, 149, 150, 151, 153, 161, 167, 172, 177, 185, 187, 190, 195, 199, 0,
		1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 124, 129, 130, 132, 133, 136, 139,
		144, 147, 149, 150, 151, 153, 161, 167, 172, 177, 185, 187, 190, 195, 199, 0,
		1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 124, 129, 130, 132, 133, 136, 139,
		144, 147, 149, 150, 151, 153, 161, 167, 172, 177, 185, 187, 190, 195, 199, 0,
		1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70, 74,
		84, 91, 97, 101, 102, 103, 110, 111, 119, 124, 129, 130, 132, 133, 136, 139,
		144, 147, 149, 150, 151, 153, 161, 167, 172, 177, 185, 187, 190, 195, 199, 103,
		124, 129, 130, 136, 141, 147, 151, 153, 161, 177, 195, 0, 0, 0, 5, 0,
		5, 8, 25, 34, 37, 1, 14, 21, 70, 74, 84, 101, 102, 103, 110, 119,
		124, 129, 130, 132, 133, 136, 144, 147, 149, 150, 151, 153, 161, 167, 172, 177,
		185, 187, 190, 195, 199, 7, 32, 79, 82, 26, 59, 26, 59, 66, 91, 21,
		68, 69, 0, 5, 0, 5, 8, 25, 115, 4, 8, 25, 8, 25, 34, 30,
		8, 25, 34, 70, 70, 119, 74, 103, 136, 103, 136, 151, 103, 136, 151, 103,
		124, 129, 136, 151, 177, 103, 124, 129, 130, 136, 147, 151, 153, 161, 177, 195,
		124, 8, 25, 34, 103, 124, 129, 130, 133, 136, 147, 150, 151, 153, 161, 177,
		195, 8, 25, 34, 103, 124, 129, 130, 133, 136, 147, 150, 151, 153, 161, 177,
		195, 8, 25, 34, 38, 103, 124, 129, 130, 133, 136, 147, 150, 151, 153, 161,
		177, 195, 1, 14, 21, 84, 102, 110, 172, 14, 45, 86, 138, 1, 14, 21,
		84, 102, 110, 172, 1, 14, 21, 84, 102, 110, 172, 135, 154, 157, 181, 103,
		124, 129, 130, 136, 141, 147, 151, 153, 161, 177, 195, 0, 1, 5, 8, 14,
		21, 25, 31, 34, 84, 102, 103, 110, 130, 136, 147, 151, 153, 172, 195, 8,
		25, 59, 7, 32, 79, 82, 95, 115, 103, 129, 130, 136, 141, 151, 153, 97,
		135, 154, 157, 181, 45, 14
	};

	private static final short lapg_sym_to[] = {
		204, 205, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 53, 75, 78,
		78, 86, 89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11, 138, 96, 146,
		146, 146, 162, 165, 123, 173, 96, 146, 179, 165, 123, 146, 146, 188, 11, 146,
		96, 96, 96, 146, 96, 47, 47, 47, 12, 12, 12, 54, 12, 12, 12, 12,
		13, 22, 13, 13, 50, 13, 114, 13, 13, 13, 30, 30, 30, 168, 168, 168,
		168, 103, 103, 136, 151, 177, 151, 21, 69, 107, 144, 107, 144, 185, 187, 185,
		199, 108, 108, 118, 120, 152, 192, 46, 55, 55, 55, 55, 56, 84, 56, 56,
		111, 56, 119, 68, 68, 109, 145, 163, 109, 186, 4, 14, 4, 4, 14, 14,
		4, 57, 57, 57, 14, 57, 14, 14, 14, 51, 58, 85, 58, 58, 58, 26,
		45, 59, 26, 59, 59, 102, 26, 26, 59, 124, 139, 124, 124, 124, 124, 124,
		124, 124, 124, 124, 124, 90, 93, 112, 113, 143, 178, 194, 60, 60, 60, 60,
		61, 61, 61, 61, 62, 62, 62, 62, 158, 158, 158, 159, 159, 159, 63, 63,
		63, 63, 160, 160, 160, 64, 64, 64, 64, 161, 161, 161, 31, 31, 65, 31,
		31, 65, 65, 65, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16, 3,
		11, 3, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96, 96,
		11, 53, 96, 11, 123, 11, 138, 96, 146, 146, 146, 162, 165, 123, 173, 96,
		146, 179, 165, 123, 146, 146, 188, 190, 11, 146, 96, 96, 96, 146, 96, 3,
		11, 3, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96, 96,
		11, 53, 96, 11, 123, 11, 138, 96, 146, 146, 146, 162, 165, 123, 173, 96,
		146, 179, 165, 123, 146, 146, 188, 191, 11, 146, 96, 96, 96, 146, 96, 3,
		11, 3, 29, 11, 11, 29, 53, 70, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 138, 96, 146, 146, 146, 162, 165, 123, 173,
		96, 146, 179, 165, 123, 146, 146, 188, 11, 146, 96, 96, 96, 146, 96, 3,
		11, 3, 29, 11, 11, 29, 53, 71, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 138, 96, 146, 146, 146, 162, 165, 123, 173,
		96, 146, 179, 165, 123, 146, 146, 188, 11, 146, 96, 96, 96, 146, 96, 3,
		11, 3, 29, 11, 11, 29, 53, 72, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 138, 96, 146, 146, 146, 162, 165, 123, 173,
		96, 146, 179, 165, 123, 146, 146, 188, 11, 146, 96, 96, 96, 146, 96, 3,
		11, 3, 29, 11, 11, 29, 53, 73, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 138, 96, 146, 146, 146, 162, 165, 123, 173,
		96, 146, 179, 165, 123, 146, 146, 188, 11, 146, 96, 96, 96, 146, 96, 3,
		11, 3, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96, 96,
		11, 53, 116, 96, 11, 123, 11, 138, 96, 146, 146, 146, 162, 165, 123, 173,
		96, 146, 179, 165, 123, 146, 146, 188, 11, 146, 96, 96, 96, 146, 96, 125,
		125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 202, 5, 6, 24, 7,
		7, 32, 32, 79, 82, 17, 17, 17, 97, 100, 17, 121, 17, 126, 17, 97,
		126, 126, 126, 164, 166, 126, 176, 126, 164, 166, 126, 126, 126, 189, 17, 126,
		197, 198, 200, 126, 201, 27, 27, 27, 27, 66, 91, 67, 67, 94, 94, 48,
		95, 48, 8, 25, 9, 9, 33, 33, 140, 23, 34, 34, 35, 35, 80, 74,
		36, 36, 36, 98, 99, 142, 101, 127, 171, 128, 128, 180, 129, 129, 129, 130,
		147, 153, 130, 130, 195, 131, 131, 131, 155, 131, 155, 131, 155, 184, 131, 155,
		148, 37, 37, 37, 132, 149, 149, 149, 167, 132, 149, 167, 132, 149, 149, 149,
		149, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 39, 39, 39, 83, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 203, 42, 49, 106, 122, 137, 193, 43, 87, 110, 172, 18, 18, 18,
		18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 169, 169, 169, 169, 133,
		150, 133, 133, 133, 174, 150, 133, 133, 150, 150, 150, 10, 20, 10, 40, 20,
		20, 40, 76, 81, 20, 20, 134, 20, 156, 134, 156, 134, 156, 20, 156, 41,
		52, 92, 28, 77, 104, 105, 115, 141, 135, 154, 157, 135, 175, 135, 181, 117,
		170, 182, 183, 196, 88, 44
	};

	private static final short lapg_rlen[] = {
		0, 1, 3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2,
		3, 3, 0, 1, 3, 0, 1, 0, 1, 0, 1, 7, 3, 1, 2, 1,
		2, 2, 5, 6, 1, 1, 1, 1, 4, 4, 1, 3, 0, 1, 2, 1,
		2, 1, 3, 0, 1, 4, 3, 3, 2, 1, 2, 3, 2, 1, 2, 2,
		5, 3, 4, 2, 4, 2, 3, 1, 3, 3, 2, 2, 2, 1, 3, 1,
		1, 2, 2, 5, 2, 1, 1, 1, 1, 1, 0, 1, 4, 0, 1, 3,
		1, 1, 3, 3, 5, 1, 1, 1, 1, 1, 3, 3, 2, 1, 1
	};

	private static final short lapg_rlex[] = {
		77, 77, 39, 39, 40, 40, 41, 41, 42, 43, 44, 44, 45, 45, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 78, 78, 46, 47, 48, 48, 48,
		49, 49, 79, 79, 49, 80, 80, 81, 81, 82, 82, 49, 50, 51, 51, 52,
		52, 52, 53, 53, 53, 54, 54, 54, 55, 55, 56, 56, 83, 83, 57, 58,
		58, 59, 59, 84, 84, 60, 60, 60, 60, 60, 61, 61, 61, 62, 62, 62,
		63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 64, 64, 65,
		66, 66, 67, 67, 67, 68, 68, 68, 68, 68, 85, 85, 68, 86, 86, 68,
		68, 69, 69, 70, 70, 71, 71, 71, 72, 73, 73, 74, 74, 75, 76
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
		"Lnoeoi",
		"Lreduce",
		"code",
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
		"inputs",
		"inputref",
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
		"syntax_problem",
		"grammar_partsopt",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"lexem_attropt",
		"commandopt",
		"Lnoeoiopt",
		"rule_attrsopt",
		"map_entriesopt",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 39;
		public static final int options = 40;
		public static final int option = 41;
		public static final int symbol = 42;
		public static final int reference = 43;
		public static final int type = 44;
		public static final int type_part_list = 45;
		public static final int type_part = 46;
		public static final int pattern = 47;
		public static final int lexer_parts = 48;
		public static final int lexer_part = 49;
		public static final int lexem_attr = 50;
		public static final int icon_list = 51;
		public static final int grammar_parts = 52;
		public static final int grammar_part = 53;
		public static final int priority_kw = 54;
		public static final int directive = 55;
		public static final int inputs = 56;
		public static final int inputref = 57;
		public static final int references = 58;
		public static final int rules = 59;
		public static final int rule0 = 60;
		public static final int ruleprefix = 61;
		public static final int rulesyms = 62;
		public static final int rulesym = 63;
		public static final int rulesyms_choice = 64;
		public static final int annotations_decl = 65;
		public static final int annotations = 66;
		public static final int annotation = 67;
		public static final int expression = 68;
		public static final int expression_list = 69;
		public static final int map_entries = 70;
		public static final int map_separator = 71;
		public static final int name = 72;
		public static final int qualified_id = 73;
		public static final int rule_attrs = 74;
		public static final int command = 75;
		public static final int syntax_problem = 76;
		public static final int grammar_partsopt = 77;
		public static final int type_part_listopt = 78;
		public static final int typeopt = 79;
		public static final int iconopt = 80;
		public static final int lexem_attropt = 81;
		public static final int commandopt = 82;
		public static final int Lnoeoiopt = 83;
		public static final int rule_attrsopt = 84;
		public static final int map_entriesopt = 85;
		public static final int expression_listopt = 86;
	}

	public interface Rules {
		public static final int lexer_part_group_selector = 32;  // lexer_part ::= '[' icon_list ']'
		public static final int lexer_part_alias = 33;  // lexer_part ::= identifier '=' pattern
		public static final int grammar_part_directive = 52;  // grammar_part ::= directive
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
			case 2:  // input ::= options lexer_parts grammar_partsopt
				  lapg_gg.sym = new AstRoot(((List<AstOptionPart>)lapg_m[lapg_head-2].sym), ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // input ::= lexer_parts grammar_partsopt
				  lapg_gg.sym = new AstRoot(null, ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 4:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOptionPart>(16); ((List<AstOptionPart>)lapg_gg.sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 5:  // options ::= options option
				 ((List<AstOptionPart>)lapg_m[lapg_head-1].sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 6:  // option ::= identifier '=' expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 11:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 28:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 29:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 30:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 31:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 32:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // lexer_part ::= identifier '=' pattern
				 lapg_gg.sym = new AstNamedPattern(((String)lapg_m[lapg_head-2].sym), ((AstRegexp)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 43:  // lexer_part ::= symbol typeopt ':' pattern iconopt lexem_attropt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-6].sym), ((String)lapg_m[lapg_head-5].sym), ((AstRegexp)lapg_m[lapg_head-3].sym), ((Integer)lapg_m[lapg_head-2].sym), ((AstLexemAttrs)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // lexem_attr ::= '(' identifier ')'
				 lapg_gg.sym = lexemAttrs(((String)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 45:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 46:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 47:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 48:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 49:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 50:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 51:  // grammar_part ::= annotations_decl symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 52:  // grammar_part ::= directive
				 lapg_gg.sym = lapg_m[lapg_head-0].sym; 
				break;
			case 56:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 57:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head-0].sym)); 
				break;
			case 59:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head-2].sym).add(((AstInputRef)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // inputref ::= reference Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 63:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 64:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 65:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 66:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 69:  // rule0 ::= ruleprefix rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-3].sym), ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // rule0 ::= rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // rule0 ::= ruleprefix commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-2].sym), null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // rule0 ::= commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 74:  // ruleprefix ::= annotations_decl ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 75:  // ruleprefix ::= annotations_decl identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 76:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 77:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 78:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 79:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 80:  // rulesym ::= command annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // rulesym ::= command annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // rulesym ::= command identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // rulesym ::= command reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-1].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // rulesym ::= annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rulesym ::= annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rulesym ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // rulesym ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // rulesym ::= '(' rulesyms_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 89:  // rulesym ::= rulesym '&' rulesym
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 90:  // rulesym ::= rulesym '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 91:  // rulesym ::= rulesym '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 92:  // rulesym ::= rulesym '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 95:  // annotations_decl ::= annotations
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 97:  // annotations ::= annotations annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 98:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 101:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 113:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 114:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 115:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 116:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 120:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 123:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-0].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 126:  // syntax_problem ::= error
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
		return (AstRoot) parse(lexer, 0, 204);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 205);
	}
}
