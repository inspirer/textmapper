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
		-1, -1, 128, -3, -1, -1, 2, -11, -1, 27, 5, -17, 98, 99, -51, 100,
		101, 102, -1, -83, 109, -1, 43, -1, 3, -1, -1, 33, -1, -89, -1, -1,
		-99, 28, -107, 45, 50, -1, -133, 93, 29, 110, -155, -1, -161, -1, 26, 31,
		4, 44, 30, -181, 16, -1, 18, 19, 14, 15, -207, 12, 13, 17, 20, 22,
		21, -1, 11, -249, -1, -1, 51, 52, 53, -1, -277, 97, -1, 6, -301, 46,
		47, -307, 94, -1, 108, -1, -313, -1, 119, 8, -319, -1, 9, 10, -361, 7,
		-393, -1, 60, -1, -1, -401, -1, -1, 111, 115, 116, 114, -1, -1, 105, 25,
		36, -433, 57, 58, 55, -1, 54, 61, -1, -463, -1, -507, 84, -1, 62, -513,
		-543, -575, -1, -617, 70, -643, -651, 112, -1, -1, 38, -683, -711, 96, -1, 73,
		-719, -763, -1, -1, -1, -793, -799, 126, -1, -805, 48, -837, -869, -877, 76, -919,
		88, 89, 87, -1, -927, 71, 82, -971, 80, -1, -1, 65, 69, -1, -1, -1,
		40, 41, 59, 83, -1, 85, -1015, -1, 125, 124, 63, -1059, 68, 67, -1067, -1,
		72, -1, -1109, 78, -1, 121, 49, 113, 42, -1153, 127, 66, 81, 79, -1, 120,
		77, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 6, 19, 6, -1, -2, 19, -1, 16, 32, -1, -2, 0, 7,
		1, 7, 2, 7, 15, 7, 17, 7, 18, 7, 20, 7, 30, 7, 31, 7,
		32, 7, 33, 7, 34, 7, 35, 7, 36, 7, 14, 118, 19, 118, -1, -2,
		1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		30, -1, 4, -1, 5, -1, 17, -1, 28, -1, 29, -1, 18, 106, -1, -2,
		14, -1, 19, 117, -1, -2, 11, -1, 9, 6, 16, 6, 19, 6, -1, -2,
		19, -1, 9, 32, 16, 32, -1, -2, 1, -1, 2, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 6, -1, 27, -1, 0, 1,
		-1, -2, 27, -1, 2, 92, 16, 92, 30, 92, 31, 92, 32, 92, 33, 92,
		34, 92, 35, 92, 36, 92, -1, -2, 15, -1, 18, 107, -1, -2, 2, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 20, 103,
		-1, -2, 1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 6, -1, 27, -1, 0, 0, -1, -2, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 14, -1, 15, -1,
		17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1,
		27, -1, 20, 23, -1, -2, 3, -1, 1, 34, 2, 34, 6, 34, 17, 34,
		27, 34, 30, 34, 31, 34, 32, 34, 33, 34, 34, 34, 35, 34, 36, 34,
		-1, -2, 19, -1, 2, 95, 16, 95, 27, 95, 30, 95, 31, 95, 32, 95,
		33, 95, 34, 95, 35, 95, 36, 95, -1, -2, 19, -1, 9, 32, -1, -2,
		19, -1, 9, 32, -1, -2, 15, -1, 20, 104, -1, -2, 2, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 14, -1, 15, -1,
		17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 26, -1,
		27, -1, 20, 24, -1, -2, 5, -1, 1, 35, 2, 35, 6, 35, 17, 35,
		19, 35, 27, 35, 30, 35, 31, 35, 32, 35, 33, 35, 34, 35, 35, 35,
		36, 35, 38, 35, -1, -2, 36, -1, 13, 56, 15, 56, -1, -2, 1, -1,
		2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1,
		19, -1, 27, -1, 38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 19, -1,
		1, 37, 2, 37, 6, 37, 17, 37, 27, 37, 30, 37, 31, 37, 32, 37,
		33, 37, 34, 37, 35, 37, 36, 37, 38, 37, -1, -2, 11, -1, 16, -1,
		1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 23, 7, 24, 7,
		25, 7, 26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7,
		35, 7, 36, 7, 38, 7, -1, -2, 39, -1, 40, 122, -1, -2, 2, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1,
		27, -1, 38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 1, -1, 2, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1,
		27, -1, 38, -1, 6, 39, 10, 39, 13, 39, -1, -2, 23, -1, 24, -1,
		25, -1, 26, -1, 1, 74, 2, 74, 6, 74, 10, 74, 13, 74, 19, 74,
		20, 74, 27, 74, 30, 74, 31, 74, 32, 74, 33, 74, 34, 74, 35, 74,
		36, 74, 38, 74, -1, -2, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1,
		32, -1, 31, -1, 30, -1, 27, -1, 6, 40, 10, 40, 13, 40, -1, -2,
		6, -1, 10, 64, 13, 64, -1, -2, 1, -1, 2, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 38, -1,
		6, 39, 10, 39, 13, 39, -1, -2, 38, -1, 1, 39, 2, 39, 6, 39,
		17, 39, 27, 39, 30, 39, 31, 39, 32, 39, 33, 39, 34, 39, 35, 39,
		36, 39, -1, -2, 36, -1, 13, 56, 15, 56, -1, -2, 11, -1, 1, 7,
		2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7,
		25, 7, 26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7,
		35, 7, 36, 7, 38, 7, -1, -2, 1, -1, 2, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 30, -1, 19, -1, 27, -1, 38, -1,
		10, 90, 20, 90, -1, -2, 39, -1, 40, 122, -1, -2, 39, -1, 40, 123,
		-1, -2, 1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 19, -1, 27, -1, 38, -1, 6, 39, 10, 39, 13, 39,
		-1, -2, 1, -1, 2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 30, -1, 19, -1, 27, -1, 38, -1, 6, 39, 10, 39, 13, 39,
		-1, -2, 6, -1, 10, 64, 13, 64, -1, -2, 23, -1, 24, -1, 25, -1,
		26, -1, 1, 75, 2, 75, 6, 75, 10, 75, 13, 75, 19, 75, 20, 75,
		27, 75, 30, 75, 31, 75, 32, 75, 33, 75, 34, 75, 35, 75, 36, 75,
		38, 75, -1, -2, 6, -1, 10, 64, 13, 64, -1, -2, 11, -1, 16, -1,
		1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7, 23, 7, 24, 7,
		25, 7, 26, 7, 27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7,
		35, 7, 36, 7, 38, 7, -1, -2, 11, -1, 1, 7, 2, 7, 6, 7,
		10, 7, 13, 7, 19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 26, 7,
		27, 7, 30, 7, 31, 7, 32, 7, 33, 7, 34, 7, 35, 7, 36, 7,
		38, 7, -1, -2, 11, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7,
		19, 7, 20, 7, 23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 30, 7,
		31, 7, 32, 7, 33, 7, 34, 7, 35, 7, 36, 7, 38, 7, -1, -2,
		6, -1, 10, 64, 13, 64, -1, -2, 23, -1, 24, -1, 25, -1, 26, 86,
		1, 86, 2, 86, 6, 86, 10, 86, 13, 86, 19, 86, 20, 86, 27, 86,
		30, 86, 31, 86, 32, 86, 33, 86, 34, 86, 35, 86, 36, 86, 38, 86,
		-1, -2, 11, -1, 1, 7, 2, 7, 6, 7, 10, 7, 13, 7, 19, 7,
		20, 7, 23, 7, 24, 7, 25, 7, 26, 7, 27, 7, 30, 7, 31, 7,
		32, 7, 33, 7, 34, 7, 35, 7, 36, 7, 38, 7, -1, -2, 1, -1,
		2, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 30, -1,
		19, -1, 27, -1, 38, -1, 10, 91, 20, 91, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 23, 71, 74, 82, 92, 100, 100, 100, 103, 106, 116, 118, 122, 127,
		134, 141, 156, 162, 184, 191, 195, 199, 206, 209, 216, 223, 245, 252, 259, 308,
		357, 406, 455, 504, 553, 603, 603, 615, 618, 620, 621, 622, 624, 631, 663, 667,
		669, 673, 676, 678, 682, 683, 684, 686, 690, 691, 695, 696, 697, 699, 702, 705,
		711, 722, 723, 740, 757, 775, 782, 783, 784, 786, 793, 800, 804, 816, 818, 821,
		842, 843, 847, 848, 849, 856, 858, 862, 863, 864, 866
	};

	private static final short lapg_sym_from[] = {
		209, 210, 0, 1, 5, 8, 14, 21, 25, 31, 34, 51, 83, 100, 101, 108,
		128, 134, 145, 153, 155, 174, 201, 0, 1, 5, 8, 14, 21, 25, 26, 31,
		34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 99, 100, 101, 108, 109,
		117, 122, 127, 128, 130, 131, 134, 137, 142, 145, 147, 148, 153, 155, 163, 169,
		174, 180, 191, 193, 196, 201, 206, 21, 67, 68, 1, 14, 21, 26, 83, 100,
		108, 174, 1, 4, 14, 21, 23, 83, 94, 100, 108, 174, 8, 25, 34, 51,
		133, 156, 159, 187, 76, 102, 103, 125, 146, 173, 3, 29, 85, 121, 136, 144,
		164, 167, 182, 194, 85, 136, 97, 99, 125, 173, 19, 26, 58, 65, 90, 26,
		42, 58, 65, 86, 90, 97, 28, 76, 85, 121, 130, 136, 164, 0, 1, 5,
		8, 14, 21, 25, 26, 58, 65, 83, 90, 100, 108, 174, 23, 26, 43, 58,
		65, 90, 7, 18, 26, 32, 58, 65, 74, 78, 81, 90, 101, 113, 122, 127,
		128, 134, 145, 153, 155, 163, 180, 201, 53, 65, 87, 91, 120, 146, 175, 26,
		58, 65, 90, 26, 58, 65, 90, 26, 58, 65, 90, 129, 157, 190, 129, 157,
		190, 26, 58, 65, 90, 129, 157, 190, 26, 58, 65, 90, 129, 157, 190, 8,
		25, 26, 34, 38, 51, 58, 65, 90, 101, 122, 127, 128, 131, 134, 145, 148,
		153, 155, 163, 180, 201, 1, 14, 21, 83, 100, 108, 174, 1, 14, 21, 83,
		100, 108, 174, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 44, 45,
		51, 58, 65, 69, 73, 83, 90, 99, 100, 101, 108, 109, 117, 122, 127, 128,
		130, 131, 134, 137, 142, 145, 147, 148, 153, 155, 163, 169, 170, 174, 180, 191,
		193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 44,
		45, 51, 58, 65, 69, 73, 83, 90, 99, 100, 101, 108, 109, 117, 122, 127,
		128, 130, 131, 134, 137, 142, 145, 147, 148, 153, 155, 163, 169, 170, 174, 180,
		191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34,
		37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 99, 100, 101, 108, 109, 117,
		122, 127, 128, 130, 131, 134, 137, 142, 145, 147, 148, 153, 155, 163, 169, 174,
		180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31,
		34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 99, 100, 101, 108, 109,
		117, 122, 127, 128, 130, 131, 134, 137, 142, 145, 147, 148, 153, 155, 163, 169,
		174, 180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26, 30,
		31, 34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 99, 100, 101, 108,
		109, 117, 122, 127, 128, 130, 131, 134, 137, 142, 145, 147, 148, 153, 155, 163,
		169, 174, 180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25, 26,
		30, 31, 34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 99, 100, 101,
		108, 109, 117, 122, 127, 128, 130, 131, 134, 137, 142, 145, 147, 148, 153, 155,
		163, 169, 174, 180, 191, 193, 196, 201, 206, 0, 1, 5, 8, 14, 21, 25,
		26, 31, 34, 37, 44, 45, 51, 58, 65, 69, 73, 83, 90, 96, 99, 100,
		101, 108, 109, 117, 122, 127, 128, 130, 131, 134, 137, 140, 142, 145, 147, 148,
		153, 155, 163, 169, 174, 180, 191, 193, 196, 201, 206, 101, 122, 127, 128, 134,
		139, 145, 153, 155, 163, 180, 201, 123, 149, 150, 152, 183, 0, 0, 0, 5,
		0, 5, 8, 25, 34, 37, 51, 1, 14, 21, 69, 73, 83, 99, 100, 101,
		108, 117, 122, 127, 128, 130, 131, 134, 142, 145, 147, 148, 153, 155, 163, 169,
		174, 180, 191, 193, 196, 201, 206, 7, 32, 78, 81, 26, 58, 26, 58, 65,
		90, 21, 67, 68, 0, 5, 0, 5, 8, 25, 113, 4, 8, 25, 8, 25,
		34, 51, 30, 8, 25, 34, 51, 69, 73, 101, 134, 101, 134, 153, 101, 134,
		153, 101, 122, 127, 134, 153, 180, 101, 122, 127, 128, 134, 145, 153, 155, 163,
		180, 201, 122, 8, 25, 34, 51, 101, 122, 127, 128, 131, 134, 145, 148, 153,
		155, 163, 180, 201, 8, 25, 34, 51, 101, 122, 127, 128, 131, 134, 145, 148,
		153, 155, 163, 180, 201, 8, 25, 34, 38, 51, 101, 122, 127, 128, 131, 134,
		145, 148, 153, 155, 163, 180, 201, 1, 14, 21, 83, 100, 108, 174, 14, 44,
		85, 136, 1, 14, 21, 83, 100, 108, 174, 1, 14, 21, 83, 100, 108, 174,
		133, 156, 159, 187, 101, 122, 127, 128, 134, 139, 145, 153, 155, 163, 180, 201,
		123, 149, 123, 149, 150, 0, 1, 5, 8, 14, 21, 25, 31, 34, 51, 83,
		100, 101, 108, 128, 134, 145, 153, 155, 174, 201, 58, 7, 32, 78, 81, 94,
		113, 101, 127, 128, 134, 139, 153, 155, 96, 140, 133, 156, 159, 187, 44, 14,
		123, 149
	};

	private static final short lapg_sym_to[] = {
		211, 212, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 52, 74,
		77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 121, 11, 136,
		95, 144, 144, 144, 164, 167, 121, 175, 95, 144, 182, 167, 121, 144, 144, 194,
		11, 144, 95, 95, 95, 144, 95, 46, 46, 46, 12, 12, 12, 53, 12, 12,
		12, 12, 13, 22, 13, 13, 49, 13, 112, 13, 13, 13, 30, 30, 30, 30,
		170, 170, 170, 170, 101, 101, 134, 153, 180, 153, 21, 68, 105, 142, 105, 142,
		191, 193, 191, 206, 106, 106, 116, 118, 154, 198, 45, 54, 54, 54, 54, 55,
		83, 55, 55, 109, 55, 117, 67, 67, 107, 143, 165, 107, 192, 4, 14, 4,
		4, 14, 14, 4, 56, 56, 56, 14, 56, 14, 14, 14, 50, 57, 84, 57,
		57, 57, 26, 44, 58, 26, 58, 58, 100, 26, 26, 58, 122, 137, 122, 122,
		122, 122, 122, 122, 122, 122, 122, 122, 89, 92, 110, 111, 141, 181, 200, 59,
		59, 59, 59, 60, 60, 60, 60, 61, 61, 61, 61, 160, 160, 160, 161, 161,
		161, 62, 62, 62, 62, 162, 162, 162, 63, 63, 63, 63, 163, 163, 163, 31,
		31, 64, 31, 31, 31, 64, 64, 64, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16,
		16, 16, 16, 3, 11, 3, 29, 11, 11, 29, 52, 74, 77, 77, 85, 88,
		77, 52, 52, 95, 95, 11, 52, 95, 11, 121, 11, 136, 95, 144, 144, 144,
		164, 167, 121, 175, 95, 144, 182, 167, 121, 144, 144, 194, 196, 11, 144, 95,
		95, 95, 144, 95, 3, 11, 3, 29, 11, 11, 29, 52, 74, 77, 77, 85,
		88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 121, 11, 136, 95, 144, 144,
		144, 164, 167, 121, 175, 95, 144, 182, 167, 121, 144, 144, 194, 197, 11, 144,
		95, 95, 95, 144, 95, 3, 11, 3, 29, 11, 11, 29, 52, 69, 74, 77,
		77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 121, 11, 136, 95,
		144, 144, 144, 164, 167, 121, 175, 95, 144, 182, 167, 121, 144, 144, 194, 11,
		144, 95, 95, 95, 144, 95, 3, 11, 3, 29, 11, 11, 29, 52, 70, 74,
		77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 121, 11, 136,
		95, 144, 144, 144, 164, 167, 121, 175, 95, 144, 182, 167, 121, 144, 144, 194,
		11, 144, 95, 95, 95, 144, 95, 3, 11, 3, 29, 11, 11, 29, 52, 71,
		74, 77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 121, 11,
		136, 95, 144, 144, 144, 164, 167, 121, 175, 95, 144, 182, 167, 121, 144, 144,
		194, 11, 144, 95, 95, 95, 144, 95, 3, 11, 3, 29, 11, 11, 29, 52,
		72, 74, 77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 95, 11, 121,
		11, 136, 95, 144, 144, 144, 164, 167, 121, 175, 95, 144, 182, 167, 121, 144,
		144, 194, 11, 144, 95, 95, 95, 144, 95, 3, 11, 3, 29, 11, 11, 29,
		52, 74, 77, 77, 85, 88, 77, 52, 52, 95, 95, 11, 52, 114, 95, 11,
		121, 11, 136, 95, 144, 144, 144, 164, 167, 121, 175, 114, 95, 144, 182, 167,
		121, 144, 144, 194, 11, 144, 95, 95, 95, 144, 95, 123, 123, 123, 123, 123,
		123, 123, 123, 123, 123, 123, 123, 149, 149, 149, 185, 202, 209, 5, 6, 24,
		7, 7, 32, 32, 78, 81, 78, 17, 17, 17, 96, 98, 17, 119, 17, 124,
		17, 140, 124, 124, 124, 166, 168, 124, 179, 124, 166, 168, 124, 124, 124, 195,
		17, 124, 204, 205, 207, 124, 208, 27, 27, 27, 27, 65, 90, 66, 66, 93,
		93, 47, 94, 47, 8, 25, 9, 9, 33, 33, 138, 23, 34, 51, 35, 35,
		79, 79, 73, 36, 36, 36, 36, 97, 99, 125, 173, 126, 126, 186, 127, 127,
		127, 128, 145, 155, 128, 128, 201, 129, 129, 129, 157, 129, 157, 129, 157, 190,
		129, 157, 146, 37, 37, 37, 37, 130, 147, 147, 147, 169, 130, 147, 169, 130,
		147, 147, 147, 147, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 39, 39, 39, 82, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 210, 41, 48, 104, 120, 135, 199, 42, 86,
		108, 174, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19,
		171, 171, 171, 171, 131, 148, 131, 131, 131, 176, 148, 131, 131, 148, 148, 148,
		150, 150, 151, 151, 184, 10, 20, 10, 40, 20, 20, 40, 75, 80, 80, 20,
		20, 132, 20, 158, 132, 158, 132, 158, 20, 158, 91, 28, 76, 102, 103, 113,
		139, 133, 156, 159, 133, 177, 133, 187, 115, 178, 172, 188, 189, 203, 87, 43,
		152, 183
	};

	private static final short lapg_rlen[] = {
		3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3, 3,
		0, 1, 3, 0, 1, 0, 1, 0, 1, 7, 3, 1, 2, 1, 2, 2,
		5, 6, 1, 1, 1, 1, 4, 4, 0, 1, 2, 4, 1, 2, 1, 3,
		0, 1, 4, 3, 3, 2, 1, 2, 3, 2, 1, 2, 2, 5, 3, 4,
		2, 4, 2, 3, 1, 3, 3, 2, 2, 2, 1, 3, 1, 1, 2, 2,
		5, 2, 1, 1, 1, 1, 1, 0, 1, 4, 0, 1, 3, 1, 1, 3,
		3, 5, 1, 1, 1, 1, 1, 3, 3, 2, 0, 1, 3, 2, 1, 3,
		1
	};

	private static final short lapg_rlex[] = {
		41, 41, 42, 42, 43, 43, 44, 45, 46, 46, 47, 47, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 80, 80, 48, 49, 50, 50, 50, 51, 51,
		81, 81, 51, 82, 82, 83, 83, 84, 84, 51, 52, 53, 53, 54, 54, 54,
		55, 55, 55, 56, 56, 56, 57, 57, 85, 85, 58, 58, 59, 59, 60, 60,
		86, 86, 61, 61, 61, 61, 61, 62, 62, 62, 63, 63, 63, 64, 64, 64,
		64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 65, 65, 66, 67, 67, 68,
		68, 68, 69, 69, 69, 69, 69, 87, 87, 69, 88, 88, 69, 69, 70, 70,
		71, 71, 72, 72, 72, 73, 74, 74, 75, 75, 89, 89, 76, 77, 77, 78,
		79
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
		public static final int references = 59;
		public static final int rules = 60;
		public static final int rule0 = 61;
		public static final int ruleprefix = 62;
		public static final int rulesyms = 63;
		public static final int rulesym = 64;
		public static final int rulesyms_choice = 65;
		public static final int annotations_decl = 66;
		public static final int annotations = 67;
		public static final int annotation = 68;
		public static final int expression = 69;
		public static final int expression_list = 70;
		public static final int map_entries = 71;
		public static final int map_separator = 72;
		public static final int name = 73;
		public static final int qualified_id = 74;
		public static final int rule_attrs = 75;
		public static final int command = 76;
		public static final int command_tokens = 77;
		public static final int command_token = 78;
		public static final int syntax_problem = 79;
		public static final int type_part_listopt = 80;
		public static final int typeopt = 81;
		public static final int iconopt = 82;
		public static final int lexem_attropt = 83;
		public static final int commandopt = 84;
		public static final int Lnoeoiopt = 85;
		public static final int rule_attrsopt = 86;
		public static final int map_entriesopt = 87;
		public static final int expression_listopt = 88;
		public static final int command_tokensopt = 89;
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
			case 55:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstDirective("input", ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // inputs ::= reference Lnoeoiopt
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-1].sym)); 
				break;
			case 59:  // inputs ::= inputs ',' reference Lnoeoiopt
				 ((List<AstReference>)lapg_m[lapg_head-3].sym).add(((AstReference)lapg_m[lapg_head-1].sym)); 
				break;
			case 60:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 61:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 63:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 66:  // rule0 ::= ruleprefix rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-3].sym), ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // rule0 ::= rulesyms commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // rule0 ::= ruleprefix commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-2].sym), null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // rule0 ::= commandopt rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 71:  // ruleprefix ::= annotations_decl ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 72:  // ruleprefix ::= annotations_decl identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 73:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 74:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 75:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 76:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 77:  // rulesym ::= command annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // rulesym ::= command annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // rulesym ::= command identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // rulesym ::= command reference
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-1].sym), null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // rulesym ::= annotations_decl identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // rulesym ::= annotations_decl reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // rulesym ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // rulesym ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rulesym ::= '(' rulesyms_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 86:  // rulesym ::= rulesym '&' rulesym
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 87:  // rulesym ::= rulesym '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 88:  // rulesym ::= rulesym '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 89:  // rulesym ::= rulesym '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 92:  // annotations_decl ::= annotations
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 94:  // annotations ::= annotations annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 95:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 98:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 111:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 112:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 113:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 117:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 120:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 128:  // syntax_problem ::= error
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
