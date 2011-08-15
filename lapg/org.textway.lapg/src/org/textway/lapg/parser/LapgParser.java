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
	private static final int lapg_action[] = {
		-1, -1, 132, -3, -1, -1, 4, -11, -17, 29, 7, -51, 107, 108, -91, 109,
		110, 111, -1, -129, 118, -1, 48, -1, 5, -135, -1, 35, -1, -169, -1, -1,
		-179, 30, -187, 50, 55, -1, -219, 99, 31, 3, 119, -245, -1, -251, -1, 28,
		33, 6, 49, 32, 2, 18, -1, 20, 21, 16, 17, -277, 14, 15, 19, 22,
		24, 23, -1, 13, -325, -1, -1, 56, 57, 58, -1, -361, 103, -1, 8, -391,
		51, 52, -397, 100, -1, 117, -1, -403, -1, 128, 10, -409, -1, 11, 12, -457,
		9, -497, -1, 61, 66, -1, -1, -505, -1, -1, 120, 124, 125, 123, -1, -1,
		114, 27, 38, -543, 64, 65, 60, -1, 59, 67, -1, -581, -1, -1, 131, 86,
		-1, 68, -631, -667, -705, -1, -1, -753, -781, 71, 87, 76, 75, -807, 121, -1,
		-1, 40, -845, 62, 102, -1, 79, -1, 130, -881, -1, -931, -1, -1, -967, -993,
		53, -1031, 74, -1069, 82, 73, 91, 92, 90, -1, -1117, 84, 77, -1167, -1, -1,
		45, 46, 47, -1, 42, 43, 85, 129, 105, -1, -1, 88, -1193, 69, 72, -1243,
		-1, 78, 54, 122, 44, -1, 104, -1291, 83, 106, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 8, 19, 8, -1, -2, 19, -1, 16, 34, -1, -2, 1, -1,
		2, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 6, -1, 17, -1, 28, -1, 0, 0, -1, -2,
		0, 9, 1, 9, 2, 9, 15, 9, 17, 9, 18, 9, 20, 9, 31, 9,
		32, 9, 33, 9, 34, 9, 35, 9, 36, 9, 37, 9, 38, 9, 39, 9,
		40, 9, 14, 127, 19, 127, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1,
		38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		4, -1, 5, -1, 17, -1, 29, -1, 30, -1, 18, 115, -1, -2, 14, -1,
		19, 126, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1, 38, -1, 37, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 6, -1, 17, -1,
		28, -1, 0, 0, -1, -2, 11, -1, 9, 8, 16, 8, 19, 8, -1, -2,
		19, -1, 9, 34, 16, 34, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1,
		38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		6, -1, 28, -1, 0, 1, -1, -2, 28, -1, 2, 98, 31, 98, 32, 98,
		33, 98, 34, 98, 35, 98, 36, 98, 37, 98, 38, 98, 39, 98, 40, 98,
		-1, -2, 15, -1, 18, 116, -1, -2, 2, -1, 40, -1, 39, -1, 38, -1,
		37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 20, 112,
		-1, -2, 2, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 14, -1, 15, -1, 17, -1, 18, -1,
		19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 27, -1, 28, -1, 20, 25,
		-1, -2, 3, -1, 0, 36, 1, 36, 2, 36, 6, 36, 17, 36, 28, 36,
		31, 36, 32, 36, 33, 36, 34, 36, 35, 36, 36, 36, 37, 36, 38, 36,
		39, 36, 40, 36, -1, -2, 19, -1, 2, 101, 16, 101, 28, 101, 31, 101,
		32, 101, 33, 101, 34, 101, 35, 101, 36, 101, 37, 101, 38, 101, 39, 101,
		40, 101, -1, -2, 19, -1, 9, 34, -1, -2, 19, -1, 9, 34, -1, -2,
		15, -1, 20, 113, -1, -2, 2, -1, 40, -1, 39, -1, 38, -1, 37, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 14, -1, 15, -1,
		17, -1, 18, -1, 19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 27, -1,
		28, -1, 20, 26, -1, -2, 5, -1, 0, 37, 1, 37, 2, 37, 6, 37,
		17, 37, 19, 37, 28, 37, 31, 37, 32, 37, 33, 37, 34, 37, 35, 37,
		36, 37, 37, 37, 38, 37, 39, 37, 40, 37, 42, 37, -1, -2, 37, -1,
		13, 63, 15, 63, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1, 38, -1,
		37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 6, -1,
		19, -1, 28, -1, 42, -1, 10, 70, 13, 70, -1, -2, 19, -1, 0, 39,
		1, 39, 2, 39, 6, 39, 17, 39, 28, 39, 31, 39, 32, 39, 33, 39,
		34, 39, 35, 39, 36, 39, 37, 39, 38, 39, 39, 39, 40, 39, 42, 39,
		-1, -2, 11, -1, 16, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9,
		19, 9, 23, 9, 24, 9, 25, 9, 27, 9, 28, 9, 31, 9, 32, 9,
		33, 9, 34, 9, 35, 9, 36, 9, 37, 9, 38, 9, 39, 9, 40, 9,
		42, 9, -1, -2, 2, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 6, -1, 19, -1, 28, -1,
		42, -1, 10, 70, 13, 70, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1,
		38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		6, -1, 19, -1, 28, -1, 42, -1, 10, 70, 13, 70, -1, -2, 23, -1,
		24, -1, 25, -1, 27, -1, 1, 80, 2, 80, 6, 80, 10, 80, 13, 80,
		19, 80, 20, 80, 28, 80, 31, 80, 32, 80, 33, 80, 34, 80, 35, 80,
		36, 80, 37, 80, 38, 80, 39, 80, 40, 80, 42, 80, -1, -2, 28, -1,
		2, 95, 31, 95, 32, 95, 33, 95, 34, 95, 35, 95, 36, 95, 37, 95,
		38, 95, 39, 95, 40, 95, 16, 98, -1, -2, 28, -1, 2, 97, 31, 97,
		32, 97, 33, 97, 34, 97, 35, 97, 36, 97, 37, 97, 38, 97, 39, 97,
		40, 97, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1, 38, -1, 37, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 6, -1, 19, -1,
		28, -1, 42, -1, 10, 70, 13, 70, -1, -2, 42, -1, 0, 41, 1, 41,
		2, 41, 6, 41, 17, 41, 28, 41, 31, 41, 32, 41, 33, 41, 34, 41,
		35, 41, 36, 41, 37, 41, 38, 41, 39, 41, 40, 41, -1, -2, 11, -1,
		1, 9, 2, 9, 6, 9, 10, 9, 13, 9, 19, 9, 20, 9, 23, 9,
		24, 9, 25, 9, 27, 9, 28, 9, 31, 9, 32, 9, 33, 9, 34, 9,
		35, 9, 36, 9, 37, 9, 38, 9, 39, 9, 40, 9, 42, 9, -1, -2,
		1, -1, 2, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 19, -1, 28, -1, 42, -1, 10, 93,
		20, 93, -1, -2, 28, -1, 2, 95, 31, 95, 32, 95, 33, 95, 34, 95,
		35, 95, 36, 95, 37, 95, 38, 95, 39, 95, 40, 95, -1, -2, 1, -1,
		2, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 6, -1, 19, -1, 28, -1, 42, -1, 10, 70,
		13, 70, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1, 38, -1, 37, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 6, -1, 19, -1,
		28, -1, 42, -1, 10, 70, 13, 70, -1, -2, 23, -1, 24, -1, 25, -1,
		27, -1, 1, 81, 2, 81, 6, 81, 10, 81, 13, 81, 19, 81, 20, 81,
		28, 81, 31, 81, 32, 81, 33, 81, 34, 81, 35, 81, 36, 81, 37, 81,
		38, 81, 39, 81, 40, 81, 42, 81, -1, -2, 11, -1, 16, -1, 1, 9,
		2, 9, 6, 9, 10, 9, 13, 9, 19, 9, 23, 9, 24, 9, 25, 9,
		27, 9, 28, 9, 31, 9, 32, 9, 33, 9, 34, 9, 35, 9, 36, 9,
		37, 9, 38, 9, 39, 9, 40, 9, 42, 9, -1, -2, 28, -1, 2, 96,
		31, 96, 32, 96, 33, 96, 34, 96, 35, 96, 36, 96, 37, 96, 38, 96,
		39, 96, 40, 96, -1, -2, 11, -1, 1, 9, 2, 9, 6, 9, 10, 9,
		13, 9, 19, 9, 20, 9, 23, 9, 24, 9, 25, 9, 27, 9, 28, 9,
		31, 9, 32, 9, 33, 9, 34, 9, 35, 9, 36, 9, 37, 9, 38, 9,
		39, 9, 40, 9, 42, 9, -1, -2, 23, -1, 24, -1, 25, -1, 27, 89,
		1, 89, 2, 89, 6, 89, 10, 89, 13, 89, 19, 89, 20, 89, 28, 89,
		31, 89, 32, 89, 33, 89, 34, 89, 35, 89, 36, 89, 37, 89, 38, 89,
		39, 89, 40, 89, 42, 89, -1, -2, 1, -1, 2, -1, 40, -1, 39, -1,
		38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		19, -1, 28, -1, 42, -1, 10, 94, 20, 94, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 22, 65, 68, 76, 86, 95, 95, 95, 98, 102, 110, 112, 116, 121,
		128, 135, 150, 156, 178, 186, 190, 194, 201, 204, 211, 212, 219, 242, 249, 256,
		300, 344, 388, 432, 476, 520, 564, 608, 652, 696, 696, 708, 709, 710, 712, 718,
		747, 751, 753, 757, 760, 762, 766, 767, 768, 769, 771, 774, 775, 778, 779, 781,
		782, 784, 787, 790, 796, 807, 808, 819, 825, 840, 859, 870, 871, 878, 879, 880,
		882, 889, 896, 902, 914, 934, 936, 937, 941, 942, 943, 944, 945, 951, 952, 953
	};

	private static final short lapg_sym_from[] = {
		202, 203, 0, 1, 5, 8, 14, 21, 25, 31, 34, 84, 102, 103, 110, 131,
		141, 155, 159, 161, 175, 199, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34,
		37, 45, 46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125,
		130, 131, 133, 141, 149, 151, 154, 155, 157, 159, 161, 169, 175, 186, 192, 197,
		199, 21, 68, 69, 1, 14, 21, 26, 84, 102, 110, 175, 1, 4, 14, 21,
		23, 84, 95, 102, 110, 175, 8, 25, 34, 103, 130, 131, 141, 159, 161, 77,
		104, 105, 128, 156, 174, 185, 3, 29, 86, 123, 143, 153, 170, 188, 86, 143,
		98, 101, 128, 174, 19, 26, 59, 66, 91, 26, 43, 59, 66, 87, 91, 98,
		28, 77, 86, 123, 134, 143, 170, 0, 1, 5, 8, 14, 21, 25, 26, 59,
		66, 84, 91, 102, 110, 175, 23, 26, 44, 59, 66, 91, 7, 18, 26, 32,
		59, 66, 75, 79, 82, 91, 103, 115, 125, 130, 131, 141, 155, 159, 161, 169,
		186, 199, 54, 66, 88, 92, 122, 156, 179, 185, 26, 59, 66, 91, 26, 59,
		66, 91, 26, 59, 66, 91, 132, 163, 191, 132, 163, 191, 26, 59, 66, 91,
		132, 163, 191, 125, 26, 59, 66, 91, 132, 163, 191, 8, 25, 26, 34, 38,
		59, 66, 91, 103, 125, 130, 131, 135, 136, 141, 155, 158, 159, 161, 169, 173,
		186, 199, 1, 14, 21, 84, 102, 110, 175, 1, 14, 21, 84, 102, 110, 175,
		0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 124, 125, 130, 131, 133, 141, 149,
		151, 154, 155, 157, 159, 161, 169, 175, 186, 192, 197, 199, 0, 1, 5, 8,
		14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 101,
		102, 103, 110, 111, 119, 124, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157,
		159, 161, 169, 175, 186, 192, 197, 199, 0, 1, 5, 8, 14, 21, 25, 26,
		30, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110,
		111, 119, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157, 159, 161, 169, 175,
		186, 192, 197, 199, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37,
		45, 46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130,
		131, 133, 141, 149, 151, 154, 155, 157, 159, 161, 169, 175, 186, 192, 197, 199,
		0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66,
		70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 149,
		151, 154, 155, 157, 159, 161, 169, 175, 186, 192, 197, 199, 0, 1, 5, 8,
		14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91,
		101, 102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157,
		159, 161, 169, 175, 186, 192, 197, 199, 0, 1, 5, 8, 14, 21, 25, 26,
		31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 97, 101, 102, 103, 110,
		111, 119, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157, 159, 161, 169, 175,
		186, 192, 197, 199, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45,
		46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130, 131,
		133, 141, 144, 149, 151, 154, 155, 157, 159, 161, 169, 175, 186, 192, 197, 199,
		0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 144, 149,
		151, 154, 155, 157, 159, 161, 169, 175, 186, 192, 197, 199, 0, 1, 5, 8,
		14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 101,
		102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 144, 149, 151, 154, 155, 157,
		159, 161, 169, 175, 186, 192, 197, 199, 103, 125, 130, 131, 141, 146, 155, 159,
		161, 169, 186, 199, 0, 0, 0, 5, 0, 5, 8, 25, 34, 37, 1, 14,
		21, 70, 74, 84, 101, 102, 103, 110, 119, 125, 130, 131, 133, 141, 149, 151,
		154, 155, 157, 159, 161, 169, 175, 186, 192, 197, 199, 7, 32, 79, 82, 26,
		59, 26, 59, 66, 91, 21, 68, 69, 0, 5, 0, 5, 8, 25, 115, 144,
		4, 8, 25, 8, 25, 34, 30, 8, 25, 34, 70, 70, 119, 74, 103, 141,
		103, 141, 159, 103, 141, 159, 103, 125, 130, 141, 159, 186, 103, 125, 130, 131,
		141, 155, 159, 161, 169, 186, 199, 125, 103, 125, 130, 131, 141, 155, 159, 161,
		169, 186, 199, 8, 25, 34, 103, 141, 159, 8, 25, 34, 103, 125, 130, 131,
		136, 141, 155, 159, 161, 169, 186, 199, 8, 25, 34, 38, 103, 125, 130, 131,
		135, 136, 141, 155, 158, 159, 161, 169, 173, 186, 199, 103, 125, 130, 131, 141,
		155, 159, 161, 169, 186, 199, 154, 1, 14, 21, 84, 102, 110, 175, 14, 45,
		86, 143, 1, 14, 21, 84, 102, 110, 175, 1, 14, 21, 84, 102, 110, 175,
		103, 130, 131, 141, 159, 161, 103, 125, 130, 131, 141, 146, 155, 159, 161, 169,
		186, 199, 0, 1, 5, 8, 14, 21, 25, 31, 34, 84, 102, 103, 110, 131,
		141, 155, 159, 161, 175, 199, 8, 25, 59, 7, 32, 79, 82, 95, 115, 146,
		97, 103, 130, 131, 141, 159, 161, 45, 14
	};

	private static final short lapg_sym_to[] = {
		204, 205, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 3, 11, 3, 29, 11, 11, 29, 53, 75, 78,
		78, 86, 89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153,
		153, 153, 170, 123, 96, 96, 96, 153, 188, 123, 153, 153, 11, 153, 96, 96,
		153, 47, 47, 47, 12, 12, 12, 54, 12, 12, 12, 12, 13, 22, 13, 13,
		50, 13, 114, 13, 13, 13, 30, 30, 30, 124, 124, 124, 124, 124, 124, 103,
		103, 141, 159, 186, 159, 197, 21, 69, 107, 149, 107, 149, 192, 192, 108, 108,
		118, 120, 160, 194, 46, 55, 55, 55, 55, 56, 84, 56, 56, 111, 56, 119,
		68, 68, 109, 150, 172, 109, 193, 4, 14, 4, 4, 14, 14, 4, 57, 57,
		57, 14, 57, 14, 14, 14, 51, 58, 85, 58, 58, 58, 26, 45, 59, 26,
		59, 59, 102, 26, 26, 59, 125, 144, 125, 125, 125, 125, 125, 125, 125, 125,
		125, 125, 90, 93, 112, 113, 148, 187, 196, 198, 60, 60, 60, 60, 61, 61,
		61, 61, 62, 62, 62, 62, 166, 166, 166, 167, 167, 167, 63, 63, 63, 63,
		168, 168, 168, 154, 64, 64, 64, 64, 169, 169, 169, 31, 31, 65, 31, 31,
		65, 65, 65, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16,
		3, 11, 3, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 143, 96, 151, 153, 153, 153, 170, 123, 96,
		96, 96, 153, 188, 123, 153, 153, 11, 153, 96, 96, 153, 3, 11, 3, 29,
		11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 96,
		11, 123, 11, 143, 96, 152, 153, 153, 153, 170, 123, 96, 96, 96, 153, 188,
		123, 153, 153, 11, 153, 96, 96, 153, 3, 11, 3, 29, 11, 11, 29, 53,
		70, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11,
		143, 96, 153, 153, 153, 170, 123, 96, 96, 96, 153, 188, 123, 153, 153, 11,
		153, 96, 96, 153, 3, 11, 3, 29, 11, 11, 29, 53, 71, 75, 78, 78,
		86, 89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153,
		153, 170, 123, 96, 96, 96, 153, 188, 123, 153, 153, 11, 153, 96, 96, 153,
		3, 11, 3, 29, 11, 11, 29, 53, 72, 75, 78, 78, 86, 89, 53, 53,
		96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 96,
		96, 96, 153, 188, 123, 153, 153, 11, 153, 96, 96, 153, 3, 11, 3, 29,
		11, 11, 29, 53, 73, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53,
		96, 11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 96, 96, 96, 153, 188,
		123, 153, 153, 11, 153, 96, 96, 153, 3, 11, 3, 29, 11, 11, 29, 53,
		75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 116, 96, 11, 123, 11,
		143, 96, 153, 153, 153, 170, 123, 96, 96, 96, 153, 188, 123, 153, 153, 11,
		153, 96, 96, 153, 3, 11, 3, 29, 11, 11, 29, 53, 75, 78, 78, 86,
		89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153, 153,
		170, 123, 176, 96, 96, 96, 153, 188, 123, 153, 153, 11, 153, 96, 96, 153,
		3, 11, 3, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 177, 96,
		96, 96, 153, 188, 123, 153, 153, 11, 153, 96, 96, 153, 3, 11, 3, 29,
		11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 96,
		11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 178, 96, 96, 96, 153, 188,
		123, 153, 153, 11, 153, 96, 96, 153, 126, 126, 126, 126, 126, 126, 126, 126,
		126, 126, 126, 126, 202, 5, 6, 24, 7, 7, 32, 32, 79, 82, 17, 17,
		17, 97, 100, 17, 121, 17, 127, 17, 97, 127, 127, 127, 171, 127, 182, 183,
		184, 127, 171, 127, 127, 127, 17, 127, 200, 201, 127, 27, 27, 27, 27, 66,
		91, 67, 67, 94, 94, 48, 95, 48, 8, 25, 9, 9, 33, 33, 145, 179,
		23, 34, 34, 35, 35, 80, 74, 36, 36, 36, 98, 99, 147, 101, 128, 174,
		129, 129, 189, 130, 130, 130, 131, 155, 161, 131, 131, 199, 132, 132, 132, 163,
		132, 163, 132, 163, 191, 132, 163, 156, 133, 157, 157, 157, 133, 157, 133, 157,
		157, 157, 157, 37, 37, 37, 134, 134, 134, 38, 38, 38, 135, 158, 158, 158,
		173, 135, 158, 135, 158, 158, 158, 158, 39, 39, 39, 83, 39, 39, 39, 39,
		83, 39, 39, 39, 83, 39, 39, 39, 83, 39, 39, 136, 136, 136, 136, 136,
		136, 136, 136, 136, 136, 136, 185, 203, 42, 49, 106, 122, 142, 195, 43, 87,
		110, 175, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19,
		137, 137, 137, 137, 137, 137, 138, 138, 138, 138, 138, 180, 138, 138, 138, 138,
		138, 138, 10, 20, 10, 40, 20, 20, 40, 76, 81, 20, 20, 139, 20, 164,
		139, 164, 139, 164, 20, 164, 41, 52, 92, 28, 77, 104, 105, 115, 146, 181,
		117, 140, 162, 165, 140, 140, 190, 88, 44
	};

	private static final short lapg_rlen[] = {
		0, 1, 3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2,
		3, 3, 0, 1, 3, 0, 1, 0, 1, 0, 1, 7, 3, 1, 1, 1,
		1, 2, 1, 2, 2, 5, 6, 1, 1, 1, 1, 4, 4, 1, 3, 0,
		1, 2, 1, 2, 1, 3, 0, 1, 3, 2, 2, 1, 1, 2, 3, 2,
		1, 2, 2, 4, 2, 3, 1, 1, 3, 3, 2, 2, 2, 1, 3, 1,
		2, 1, 1, 1, 2, 2, 5, 2, 4, 1, 3, 1, 1, 1, 1, 1,
		0, 1, 4, 0, 1, 3, 1, 1, 3, 3, 5, 1, 1, 1, 1, 1,
		3, 3, 2, 1, 1
	};

	private static final short lapg_rlex[] = {
		85, 85, 43, 43, 44, 44, 45, 45, 46, 47, 48, 48, 49, 49, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 86, 86, 50, 51, 52, 52, 52,
		53, 53, 87, 87, 53, 88, 88, 89, 89, 90, 90, 53, 54, 55, 55, 55,
		56, 56, 57, 57, 57, 58, 58, 58, 59, 59, 59, 60, 60, 61, 61, 91,
		91, 62, 63, 63, 64, 64, 92, 92, 65, 65, 65, 65, 65, 66, 66, 66,
		67, 67, 67, 68, 68, 68, 68, 68, 68, 68, 68, 68, 68, 69, 69, 70,
		70, 70, 71, 72, 72, 73, 73, 73, 74, 75, 75, 76, 76, 76, 76, 76,
		93, 93, 76, 94, 94, 76, 76, 77, 77, 78, 78, 79, 79, 79, 80, 81,
		81, 82, 82, 83, 84
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
		"'?!'",
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
		"Lsoft",
		"Lclass",
		"Lspace",
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
		"lexem_attrs",
		"lexem_attribute",
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
		"ruleparts",
		"rulepart",
		"ruleparts_choice",
		"ruleannotations",
		"annotations",
		"annotation_list",
		"annotation",
		"negative_la",
		"negative_la_clause",
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
		"lexem_attrsopt",
		"commandopt",
		"Lnoeoiopt",
		"rule_attrsopt",
		"map_entriesopt",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 43;
		public static final int options = 44;
		public static final int option = 45;
		public static final int symbol = 46;
		public static final int reference = 47;
		public static final int type = 48;
		public static final int type_part_list = 49;
		public static final int type_part = 50;
		public static final int pattern = 51;
		public static final int lexer_parts = 52;
		public static final int lexer_part = 53;
		public static final int lexem_attrs = 54;
		public static final int lexem_attribute = 55;
		public static final int icon_list = 56;
		public static final int grammar_parts = 57;
		public static final int grammar_part = 58;
		public static final int priority_kw = 59;
		public static final int directive = 60;
		public static final int inputs = 61;
		public static final int inputref = 62;
		public static final int references = 63;
		public static final int rules = 64;
		public static final int rule0 = 65;
		public static final int ruleprefix = 66;
		public static final int ruleparts = 67;
		public static final int rulepart = 68;
		public static final int ruleparts_choice = 69;
		public static final int ruleannotations = 70;
		public static final int annotations = 71;
		public static final int annotation_list = 72;
		public static final int annotation = 73;
		public static final int negative_la = 74;
		public static final int negative_la_clause = 75;
		public static final int expression = 76;
		public static final int expression_list = 77;
		public static final int map_entries = 78;
		public static final int map_separator = 79;
		public static final int name = 80;
		public static final int qualified_id = 81;
		public static final int rule_attrs = 82;
		public static final int command = 83;
		public static final int syntax_problem = 84;
		public static final int grammar_partsopt = 85;
		public static final int type_part_listopt = 86;
		public static final int typeopt = 87;
		public static final int iconopt = 88;
		public static final int lexem_attrsopt = 89;
		public static final int commandopt = 90;
		public static final int Lnoeoiopt = 91;
		public static final int rule_attrsopt = 92;
		public static final int map_entriesopt = 93;
		public static final int expression_listopt = 94;
	}

	public interface Rules {
		public static final int lexer_part_group_selector = 32;  // lexer_part ::= '[' icon_list ']'
		public static final int lexer_part_alias = 33;  // lexer_part ::= identifier '=' pattern
		public static final int grammar_part_directive = 55;  // grammar_part ::= directive
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
			case 43:  // lexer_part ::= symbol typeopt ':' pattern iconopt lexem_attrsopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-6].sym), ((String)lapg_m[lapg_head-5].sym), ((AstRegexp)lapg_m[lapg_head-3].sym), ((Integer)lapg_m[lapg_head-2].sym), ((AstLexemAttrs)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.sym = ((AstLexemAttrs)lapg_m[lapg_head-1].sym); 
				break;
			case 45:  // lexem_attribute ::= Lsoft
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // lexem_attribute ::= Lclass
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // lexem_attribute ::= Lspace
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 49:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 50:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 51:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 52:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 53:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // grammar_part ::= directive
				 lapg_gg.sym = lapg_m[lapg_head-0].sym; 
				break;
			case 59:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 60:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head-2].sym).add(((AstInputRef)lapg_m[lapg_head-0].sym)); 
				break;
			case 65:  // inputref ::= reference Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 67:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 68:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 69:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 72:  // rule0 ::= ruleprefix ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-2].sym), ((List<AstRulePart>)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // rule0 ::= ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRulePart>)lapg_m[lapg_head-1].sym), ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // rule0 ::= ruleprefix rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head-1].sym), null, ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // rule0 ::= rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstRuleAttribute)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 77:  // ruleprefix ::= annotations ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head-1].sym), null); 
				break;
			case 78:  // ruleprefix ::= ruleannotations identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstRuleAnnotations)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 79:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head-1].sym)); 
				break;
			case 80:  // ruleparts ::= rulepart
				 lapg_gg.sym = new ArrayList<AstRulePart>(); ((List<AstRulePart>)lapg_gg.sym).add(((AstRulePart)lapg_m[lapg_head-0].sym)); 
				break;
			case 81:  // ruleparts ::= ruleparts rulepart
				 ((List<AstRulePart>)lapg_m[lapg_head-1].sym).add(((AstRulePart)lapg_m[lapg_head-0].sym)); 
				break;
			case 82:  // ruleparts ::= ruleparts syntax_problem
				 ((List<AstRulePart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 83:  // rulepart ::= ruleannotations identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstRuleAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // rulepart ::= ruleannotations reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((AstReference)lapg_m[lapg_head-0].sym), ((AstRuleAnnotations)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rulepart ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((String)lapg_m[lapg_head-2].sym), ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rulepart ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((AstReference)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // rulepart ::= '(' ruleparts_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 89:  // rulepart ::= rulepart '&' rulepart
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 90:  // rulepart ::= rulepart '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 91:  // rulepart ::= rulepart '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 92:  // rulepart ::= rulepart '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 95:  // ruleannotations ::= annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // ruleannotations ::= negative_la annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head-1].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // ruleannotations ::= negative_la
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // annotations ::= annotation_list
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // annotation_list ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 100:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 101:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 104:  // negative_la ::= '(' '?!' negative_la_clause ')'
				 lapg_gg.sym = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // negative_la_clause ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 106:  // negative_la_clause ::= negative_la_clause '|' reference
				 ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 107:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head-3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 120:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 121:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 122:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 126:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 129:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-0].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 132:  // syntax_problem ::= error
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
