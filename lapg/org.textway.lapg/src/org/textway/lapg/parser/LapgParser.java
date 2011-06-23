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
		-1, -1, 129, -3, -1, -1, 2, -11, -1, 27, 5, -17, 99, 100, -51, 101,
		102, 103, -1, -83, 110, -1, 43, -1, 3, -1, -1, 33, -1, -89, -1, -1,
		-99, 28, -107, 45, 50, -1, -133, 94, 29, 111, -155, -1, -161, -1, 26, 31,
		4, 44, 30, -181, 16, -1, 18, 19, 14, 15, -207, 12, 13, 17, 20, 22,
		21, -1, 11, -249, -1, -1, 51, 52, 53, -1, -277, 98, -1, 6, -301, 46,
		47, -307, 95, -1, 109, -1, -313, -1, 120, 8, -319, -1, 9, 10, -361, 7,
		-393, -1, 56, 61, -1, -1, -401, -1, -1, 112, 116, 117, 115, -1, -1, 106,
		25, 36, -433, 59, 60, 55, -1, 54, 62, -1, -463, -1, -507, 85, -1, 63,
		-513, -543, -575, -1, -617, 71, -643, -651, 113, -1, -1, 38, -683, 57, 97, -1,
		74, -711, -755, -1, -1, -1, -785, -791, 127, -1, -797, 48, -829, -861, -869, 77,
		-911, 89, 90, 88, -1, -919, 72, 83, -963, 81, -1, -1, 66, 70, -1, -1,
		-1, 40, 41, 84, -1, 86, -1007, -1, 126, 125, 64, -1051, 69, 68, -1059, -1,
		73, -1, -1101, 79, -1, 122, 49, 114, 42, -1145, 128, 67, 82, 80, -1, 121,
		78, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 6, 19, 6, -1, -2, 19, -1, 16, 32, -1, -2, 0, 7,
		1, 7, 2, 7, 15, 7, 17, 7, 18, 7, 20, 7, 30, 7, 31, 7,
		32, 7, 33, 7, 34, 7, 35, 7, 36, 7, 14, 119, 19, 119, -1, -2,
		1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		30, -1, 4, -1, 5, -1, 17, -1, 28, -1, 29, -1, 18, 107, -1, -2,
		14, -1, 19, 118, -1, -2, 11, -1, 9, 6, 16, 6, 19, 6, -1, -2,
		19, -1, 9, 32, 16, 32, -1, -2, 1, -1, 2, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 6, -1, 27, -1, 0, 1,
		-1, -2, 27, -1, 2, 93, 16, 93, 30, 93, 31, 93, 32, 93, 33, 93,
		34, 93, 35, 93, 36, 93, -1, -2, 15, -1, 18, 108, -1, -2, 2, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 20, 104,
		-1, -2, 1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 6, -1, 27, -1, 0, 0, -1, -2, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 14, -1, 15, -1,
		17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1,
		27, -1, 20, 23, -1, -2, 3, -1, 1, 34, 2, 34, 6, 34, 17, 34,
		27, 34, 30, 34, 31, 34, 32, 34, 33, 34, 34, 34, 35, 34, 36, 34,
		-1, -2, 19, -1, 2, 96, 16, 96, 27, 96, 30, 96, 31, 96, 32, 96,
		33, 96, 34, 96, 35, 96, 36, 96, -1, -2, 19, -1, 9, 32, -1, -2,
		19, -1, 9, 32, -1, -2, 15, -1, 20, 105, -1, -2, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 14, -1, 15, -1,
		17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1,
		27, -1, 20, 24, -1, -2, 5, -1, 1, 35, 2, 35, 6, 35, 17, 35,
		19, 35, 27, 35, 30, 35, 31, 35, 32, 35, 33, 35, 34, 35, 35, 35,
		36, 35, 38, 35, -1, -2, 36, -1, 13, 58, 15, 58, -1, -2, 1, -1,
		2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1,
		19, -1, 27, -1, 38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 19, -1,
		1, 37, 2, 37, 6, 37, 17, 37, 27, 37, 30, 37, 31, 37, 32, 37,
		33, 37, 34, 37, 35, 37, 36, 37, 38, 37, -1, -2, 11, -1, 16, -1,
		1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 23, 7, 24, 7,
		25, 7, 26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7,
		35, 7, 36, 7, 38, 7, -1, -2, 39, -1, 40, 123, -1, -2, 2, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1,
		27, -1, 38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 1, -1, 2, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1,
		27, -1, 38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 23, -1, 24, -1,
		25, -1, 26, -1, 1, 75, 2, 75, 6, 75, 10, 75, 13, 75, 19, 75,
		20, 75, 27, 75, 30, 75, 31, 75, 32, 75, 33, 75, 34, 75, 35, 75,
		36, 75, 38, 75, -1, -2, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1,
		32, -1, 31, -1, 30, -1, 27, -1, 6, 40, 10, 40, 13, 40, -1, -2,
		6, -1, 10, 65, 13, 65, -1, -2, 1, -1, 2, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 38, -1,
		6, 39, 10, 39, 13, 39, -1, -2, 38, -1, 1, 39, 2, 39, 6, 39,
		17, 39, 27, 39, 30, 39, 31, 39, 32, 39, 33, 39, 34, 39, 35, 39,
		36, 39, -1, -2, 11, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7,
		19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 30, 7,
		31, 7, 32, 7, 33, 7, 34, 7, 35, 7, 36, 7, 38, 7, -1, -2,
		1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		30, -1, 19, -1, 27, -1, 38, -1, 10, 91, 20, 91, -1, -2, 39, -1,
		40, 123, -1, -2, 39, -1, 40, 124, -1, -2, 1, -1, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1,
		38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 1, -1, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1,
		38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 6, -1, 10, 65, 13, 65,
		-1, -2, 23, -1, 24, -1, 25, -1, 26, -1, 1, 76, 2, 76, 6, 76,
		10, 76, 13, 76, 19, 76, 20, 76, 27, 76, 30, 76, 31, 76, 32, 76,
		33, 76, 34, 76, 35, 76, 36, 76, 38, 76, -1, -2, 6, -1, 10, 65,
		13, 65, -1, -2, 11, -1, 16, -1, 1, 7, 2, 7, 6, 7, 10, 7,
		13, 7, 19, 7, 23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 30, 7,
		31, 7, 32, 7, 33, 7, 34, 7, 35, 7, 36, 7, 38, 7, -1, -2,
		11, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 20, 7,
		23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 30, 7, 31, 7, 32, 7,
		33, 7, 34, 7, 35, 7, 36, 7, 38, 7, -1, -2, 11, -1, 1, 7,
		2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7,
		25, 7, 26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7,
		35, 7, 36, 7, 38, 7, -1, -2, 6, -1, 10, 65, 13, 65, -1, -2,
		23, -1, 24, -1, 25, -1, 26, 87, 1, 87, 2, 87, 6, 87, 10, 87,
		13, 87, 19, 87, 20, 87, 27, 87, 30, 87, 31, 87, 32, 87, 33, 87,
		34, 87, 35, 87, 36, 87, 38, 87, -1, -2, 11, -1, 1, 7, 2, 7,
		6, 7, 10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7,
		26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7, 35, 7,
		36, 7, 38, 7, -1, -2, 1, -1, 2, -1, 36, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 38, -1, 10, 92,
		20, 92, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 23, 71, 74, 82, 92, 100, 100, 100, 103, 106, 116, 118, 122, 127,
		134, 141, 156, 162, 184, 191, 195, 199, 206, 209, 216, 223, 245, 252, 259, 308,
		357, 406, 455, 504, 553, 602, 602, 614, 617, 619, 620, 621, 623, 630, 662, 666,
		668, 672, 675, 677, 681, 682, 683, 685, 689, 690, 694, 695, 697, 698, 700, 703,
		706, 712, 723, 724, 741, 758, 776, 783, 784, 785, 787, 794, 801, 805, 817, 819,
		822, 843, 844, 848, 849, 850, 857, 858, 862, 863, 864, 866
	};

	private static final short lapg_sym_from[] = {
		209, 210, 0, 1, 5, 8, 14, 21, 25, 31, 34, 51, 83, 101, 102, 109,
		129, 135, 146, 154, 156, 175, 201, 0, 1, 5, 8, 14, 21, 25, 26, 31,
		34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 100, 101, 102, 109, 110,
		118, 123, 128, 129, 131, 132, 135, 138, 143, 146, 148, 149, 154, 156, 164, 170,
		175, 180, 191, 193, 196, 201, 206, 21, 67, 68, 1, 14, 21, 26, 83, 101,
		109, 175, 1, 4, 14, 21, 23, 83, 94, 101, 109, 175, 8, 25, 34, 51,
		134, 157, 160, 187, 76, 103, 104, 126, 147, 174, 3, 29, 85, 122, 137, 145,
		165, 168, 182, 194, 85, 137, 97, 100, 126, 174, 19, 26, 58, 65, 90, 26,
		42, 58, 65, 86, 90, 97, 28, 76, 85, 122, 131, 137, 165, 0, 1, 5,
		8, 14, 21, 25, 26, 58, 65, 83, 90, 101, 109, 175, 23, 26, 43, 58,
		65, 90, 7, 18, 26, 32, 58, 65, 74, 78, 81, 90, 102, 114, 123, 128,
		129, 135, 146, 154, 156, 164, 180, 201, 53, 65, 87, 91, 121, 147, 176, 26,
		58, 65, 90, 26, 58, 65, 90, 26, 58, 65, 90, 130, 158, 190, 130, 158,
		190, 26, 58, 65, 90, 130, 158, 190, 26, 58, 65, 90, 130, 158, 190, 8,
		25, 26, 34, 38, 51, 58, 65, 90, 102, 123, 128, 129, 132, 135, 146, 149,
		154, 156, 164, 180, 201, 1, 14, 21, 83, 101, 109, 175, 1, 14, 21, 83,
		101, 109, 175, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 44, 45,
		51, 58, 65, 69, 73, 83, 90, 100, 101, 102, 109, 110, 118, 123, 128, 129,
		131, 132, 135, 138, 143, 146, 148, 149, 154, 156, 164, 170, 171, 175, 180, 191,
		193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 44,
		45, 51, 58, 65, 69, 73, 83, 90, 100, 101, 102, 109, 110, 118, 123, 128,
		129, 131, 132, 135, 138, 143, 146, 148, 149, 154, 156, 164, 170, 171, 175, 180,
		191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34,
		37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 100, 101, 102, 109, 110, 118,
		123, 128, 129, 131, 132, 135, 138, 143, 146, 148, 149, 154, 156, 164, 170, 175,
		180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31,
		34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 100, 101, 102, 109, 110,
		118, 123, 128, 129, 131, 132, 135, 138, 143, 146, 148, 149, 154, 156, 164, 170,
		175, 180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 30,
		31, 34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 100, 101, 102, 109,
		110, 118, 123, 128, 129, 131, 132, 135, 138, 143, 146, 148, 149, 154, 156, 164,
		170, 175, 180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26,
		30, 31, 34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 100, 101, 102,
		109, 110, 118, 123, 128, 129, 131, 132, 135, 138, 143, 146, 148, 149, 154, 156,
		164, 170, 175, 180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25,
		26, 31, 34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 96, 100, 101,
		102, 109, 110, 118, 123, 128, 129, 131, 132, 135, 138, 143, 146, 148, 149, 154,
		156, 164, 170, 175, 180, 191, 193, 196, 201, 206, 102, 123, 128, 129, 135, 140,
		146, 154, 156, 164, 180, 201, 124, 150, 151, 153, 183, 0, 0, 0, 5, 0,
		5, 8, 25, 34, 37, 51, 1, 14, 21, 69, 73, 83, 100, 101, 102, 109,
		118, 123, 128, 129, 131, 132, 135, 143, 146, 148, 149, 154, 156, 164, 170, 175,
		180, 191, 193, 196, 201, 206, 7, 32, 78, 81, 26, 58, 26, 58, 65, 90,
		21, 67, 68, 0, 5, 0, 5, 8, 25, 114, 4, 8, 25, 8, 25, 34,
		51, 30, 8, 25, 34, 51, 69, 69, 118, 73, 102, 135, 102, 135, 154, 102,
		135, 154, 102, 123, 128, 135, 154, 180, 102, 123, 128, 129, 135, 146, 154, 156,
		164, 180, 201, 123, 8, 25, 34, 51, 102, 123, 128, 129, 132, 135, 146, 149,
		154, 156, 164, 180, 201, 8, 25, 34, 51, 102, 123, 128, 129, 132, 135, 146,
		149, 154, 156, 164, 180, 201, 8, 25, 34, 38, 51, 102, 123, 128, 129, 132,
		135, 146, 149, 154, 156, 164, 180, 201, 1, 14, 21, 83, 101, 109, 175, 14,
		44, 85, 137, 1, 14, 21, 83, 101, 109, 175, 1, 14, 21, 83, 101, 109,
		175, 134, 157, 160, 187, 102, 123, 128, 129, 135, 140, 146, 154, 156, 164, 180,
		201, 124, 150, 124, 150, 151, 0, 1, 5, 8, 14, 21, 25, 31, 34, 51,
		83, 101, 102, 109, 129, 135, 146, 154, 156, 175, 201, 58, 7, 32, 78, 81,
		94, 114, 102, 128, 129, 135, 140, 154, 156, 96, 134, 157, 160, 187, 44, 14,
		124, 150
	};

	private static final short lapg_sym_to[] = {
		211, 212, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 52, 74,
		77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 122, 11, 137,
		95, 145, 145, 145, 165, 168, 122, 176, 95, 145, 182, 168, 122, 145, 145, 194,
		11, 145, 95, 95, 95, 145, 95, 46, 46, 46, 12, 12, 12, 53, 12, 12,
		12, 12, 13, 22, 13, 13, 49, 13, 113, 13, 13, 13, 30, 30, 30, 30,
		171, 171, 171, 171, 102, 102, 135, 154, 180, 154, 21, 68, 106, 143, 106, 143,
		191, 193, 191, 206, 107, 107, 117, 119, 155, 198, 45, 54, 54, 54, 54, 55,
		83, 55, 55, 110, 55, 118, 67, 67, 108, 144, 166, 108, 192, 4, 14, 4,
		4, 14, 14, 4, 56, 56, 56, 14, 56, 14, 14, 14, 50, 57, 84, 57,
		57, 57, 26, 44, 58, 26, 58, 58, 101, 26, 26, 58, 123, 138, 123, 123,
		123, 123, 123, 123, 123, 123, 123, 123, 89, 92, 111, 112, 142, 181, 200, 59,
		59, 59, 59, 60, 60, 60, 60, 61, 61, 61, 61, 161, 161, 161, 162, 162,
		162, 62, 62, 62, 62, 163, 163, 163, 63, 63, 63, 63, 164, 164, 164, 31,
		31, 64, 31, 31, 31, 64, 64, 64, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16,
		16, 16, 16, 3, 11, 3, 29, 11, 11, 29, 52, 74, 77, 77, 85, 88,
		77, 52, 52, 95, 95, 11, 52, 95, 11, 122, 11, 137, 95, 145, 145, 145,
		165, 168, 122, 176, 95, 145, 182, 168, 122, 145, 145, 194, 196, 11, 145, 95,
		95, 95, 145, 95, 3, 11, 3, 29, 11, 11, 29, 52, 74, 77, 77, 85,
		88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 122, 11, 137, 95, 145, 145,
		145, 165, 168, 122, 176, 95, 145, 182, 168, 122, 145, 145, 194, 197, 11, 145,
		95, 95, 95, 145, 95, 3, 11, 3, 29, 11, 11, 29, 52, 69, 74, 77,
		77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 122, 11, 137, 95,
		145, 145, 145, 165, 168, 122, 176, 95, 145, 182, 168, 122, 145, 145, 194, 11,
		145, 95, 95, 95, 145, 95, 3, 11, 3, 29, 11, 11, 29, 52, 70, 74,
		77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 122, 11, 137,
		95, 145, 145, 145, 165, 168, 122, 176, 95, 145, 182, 168, 122, 145, 145, 194,
		11, 145, 95, 95, 95, 145, 95, 3, 11, 3, 29, 11, 11, 29, 52, 71,
		74, 77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 122, 11,
		137, 95, 145, 145, 145, 165, 168, 122, 176, 95, 145, 182, 168, 122, 145, 145,
		194, 11, 145, 95, 95, 95, 145, 95, 3, 11, 3, 29, 11, 11, 29, 52,
		72, 74, 77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 122,
		11, 137, 95, 145, 145, 145, 165, 168, 122, 176, 95, 145, 182, 168, 122, 145,
		145, 194, 11, 145, 95, 95, 95, 145, 95, 3, 11, 3, 29, 11, 11, 29,
		52, 74, 77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 115, 95, 11,
		122, 11, 137, 95, 145, 145, 145, 165, 168, 122, 176, 95, 145, 182, 168, 122,
		145, 145, 194, 11, 145, 95, 95, 95, 145, 95, 124, 124, 124, 124, 124, 124,
		124, 124, 124, 124, 124, 124, 150, 150, 150, 185, 202, 209, 5, 6, 24, 7,
		7, 32, 32, 78, 81, 78, 17, 17, 17, 96, 99, 17, 120, 17, 125, 17,
		96, 125, 125, 125, 167, 169, 125, 179, 125, 167, 169, 125, 125, 125, 195, 17,
		125, 204, 205, 207, 125, 208, 27, 27, 27, 27, 65, 90, 66, 66, 93, 93,
		47, 94, 47, 8, 25, 9, 9, 33, 33, 139, 23, 34, 51, 35, 35, 79,
		79, 73, 36, 36, 36, 36, 97, 98, 141, 100, 126, 174, 127, 127, 186, 128,
		128, 128, 129, 146, 156, 129, 129, 201, 130, 130, 130, 158, 130, 158, 130, 158,
		190, 130, 158, 147, 37, 37, 37, 37, 131, 148, 148, 148, 170, 131, 148, 170,
		131, 148, 148, 148, 148, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 39, 39, 39, 82, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 210, 41, 48, 105, 121, 136, 199, 42,
		86, 109, 175, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19,
		19, 172, 172, 172, 172, 132, 149, 132, 132, 132, 177, 149, 132, 132, 149, 149,
		149, 151, 151, 152, 152, 184, 10, 20, 10, 40, 20, 20, 40, 75, 80, 80,
		20, 20, 133, 20, 159, 133, 159, 133, 159, 20, 159, 91, 28, 76, 103, 104,
		114, 140, 134, 157, 160, 134, 178, 134, 187, 116, 173, 188, 189, 203, 87, 43,
		153, 183
	};

	private static final short lapg_rlen[] = {
		3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3, 3,
		0, 1, 3, 0, 1, 0, 1, 0, 1, 7, 3, 1, 2, 1, 2, 2,
		5, 6, 1, 1, 1, 1, 4, 4, 1, 3, 0, 1, 2, 1, 2, 1,
		3, 0, 1, 4, 3, 3, 2, 1, 2, 3, 2, 1, 2, 2, 5, 3,
		4, 2, 4, 2, 3, 1, 3, 3, 2, 2, 2, 1, 3, 1, 1, 2,
		2, 5, 2, 1, 1, 1, 1, 1, 0, 1, 4, 0, 1, 3, 1, 1,
		3, 3, 5, 1, 1, 1, 1, 1, 3, 3, 2, 0, 1, 3, 2, 1,
		3, 1
	};

	private static final short lapg_rlex[] = {
		41, 41, 42, 42, 43, 43, 44, 45, 46, 46, 47, 47, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 81, 81, 48, 49, 50, 50, 50, 51, 51,
		82, 82, 51, 83, 83, 84, 84, 85, 85, 51, 52, 53, 53, 54, 54, 54,
		55, 55, 55, 56, 56, 56, 57, 57, 58, 58, 86, 86, 59, 60, 60, 61,
		61, 87, 87, 62, 62, 62, 62, 62, 63, 63, 63, 64, 64, 64, 65, 65,
		65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 66, 66, 67, 68, 68,
		69, 69, 69, 70, 70, 70, 70, 70, 88, 88, 70, 89, 89, 70, 70, 71,
		71, 72, 72, 73, 73, 73, 74, 75, 75, 76, 76, 90, 90, 77, 78, 78,
		79, 80
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
		"command_tokens",
		"command_token",
		"syntax_problem",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"lexem_attropt",
		"commandopt",
		"Lnoeoiopt",
		"rule_attrsopt",
		"map_entriesopt",
		"expression_listopt",
		"command_tokensopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 41;
		public static final int options = 42;
		public static final int option = 43;
		public static final int symbol = 44;
		public static final int reference = 45;
		public static final int type = 46;
		public static final int type_part_list = 47;
		public static final int type_part = 48;
		public static final int pattern = 49;
		public static final int lexer_parts = 50;
		public static final int lexer_part = 51;
		public static final int lexem_attr = 52;
		public static final int icon_list = 53;
		public static final int grammar_parts = 54;
		public static final int grammar_part = 55;
		public static final int priority_kw = 56;
		public static final int directive = 57;
		public static final int inputs = 58;
		public static final int inputref = 59;
		public static final int references = 60;
		public static final int rules = 61;
		public static final int rule0 = 62;
		public static final int ruleprefix = 63;
		public static final int rulesyms = 64;
		public static final int rulesym = 65;
		public static final int rulesyms_choice = 66;
		public static final int annotations_decl = 67;
		public static final int annotations = 68;
		public static final int annotation = 69;
		public static final int expression = 70;
		public static final int expression_list = 71;
		public static final int map_entries = 72;
		public static final int map_separator = 73;
		public static final int name = 74;
		public static final int qualified_id = 75;
		public static final int rule_attrs = 76;
		public static final int command = 77;
		public static final int command_tokens = 78;
		public static final int command_token = 79;
		public static final int syntax_problem = 80;
		public static final int type_part_listopt = 81;
		public static final int typeopt = 82;
		public static final int iconopt = 83;
		public static final int lexem_attropt = 84;
		public static final int commandopt = 85;
		public static final int Lnoeoiopt = 86;
		public static final int rule_attrsopt = 87;
		public static final int map_entriesopt = 88;
		public static final int expression_listopt = 89;
		public static final int command_tokensopt = 90;
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
				 lapg_gg.sym = new AstNamedPattern(((String)lapg_m[lapg_head-2].sym), ((AstRegexp)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
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
			case 55:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 56:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head-2].sym).add(((AstInputRef)lapg_m[lapg_head-0].sym)); 
				break;
			case 60:  // inputref ::= reference Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 63:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 64:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 67:  // rule0 ::= ruleprefix rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-3].sym), ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // rule0 ::= rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // rule0 ::= ruleprefix commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-2].sym), null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // rule0 ::= commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 72:  // ruleprefix ::= annotations_decl ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 73:  // ruleprefix ::= annotations_decl identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 74:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 75:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 76:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 77:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 78:  // rulesym ::= command annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // rulesym ::= command annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // rulesym ::= command identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // rulesym ::= command reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-1].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // rulesym ::= annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // rulesym ::= annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // rulesym ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rulesym ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rulesym ::= '(' rulesyms_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 87:  // rulesym ::= rulesym '&' rulesym
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 88:  // rulesym ::= rulesym '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 89:  // rulesym ::= rulesym '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 90:  // rulesym ::= rulesym '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 93:  // annotations_decl ::= annotations
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 95:  // annotations ::= annotations annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 96:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 99:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 112:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 113:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 114:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 118:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 121:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 129:  // syntax_problem ::= error
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
		return (AstRoot) parse(lexer, 0, 211);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 212);
	}
}
