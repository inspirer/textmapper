/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.templates.ast;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.textway.templates.ast.TemplatesLexer.ErrorReporter;
import org.textway.templates.ast.TemplatesLexer.LapgSymbol;
import org.textway.templates.ast.TemplatesLexer.Lexems;
import org.textway.templates.ast.TemplatesTree.TextSource;
import org.textway.templates.bundle.IBundleEntity;

public class TemplatesParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public TemplatesParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	String templatePackage;

	private int killEnds = -1;

	private int rawText(int start, final int end) {
		char[] buff = source.getContents();
		if( killEnds == start ) {
			while( start < end && (buff[start] == '\t' || buff[start] == ' ') )
				start++;

			if( start < end && buff[start] == '\r' )
				start++;

			if( start < end && buff[start] == '\n' )
				start++;
		}
		return start;
	}

	private void checkIsSpace(int start, int end, int line) {
		String val = source.getText(rawText(start,end),end).trim();
		if( val.length() > 0 )
			reporter.error(start, end, line, "Unknown text ignored: `"+val+"`");
	}

	private void applyElse(CompoundNode node, ElseIfNode elseNode, int offset, int endoffset, int line) {
		if (elseNode == null ) {
			return;
		}
		if (node instanceof IfNode) {
			((IfNode)node).applyElse(elseNode);
		} else {
			reporter.error(offset, endoffset, line, "Unknown else node, instructions skipped");
		}
	}

	private ExpressionNode createMapCollect(ExpressionNode context, String instruction, String varName, ExpressionNode key, ExpressionNode value, TextSource source, int offset, int endoffset, int line) {
		if(!instruction.equals("collect")) {
			reporter.error(offset, endoffset, line, "unknown collection processing instruction: " + instruction);
			return new ErrorNode(source, offset, endoffset);
		}
		return new CollectMapNode(context, varName, key, value, source, offset, endoffset);
	}

	private ExpressionNode createCollectionProcessor(ExpressionNode context, String instruction, String varName, ExpressionNode foreachExpr, TextSource source, int offset, int endoffset, int line) {
		char first = instruction.charAt(0);
		int kind = 0;
		switch(first) {
		case 'c':
			if(instruction.equals("collect")) {
				kind = CollectionProcessorNode.COLLECT;
			} else if(instruction.equals("collectUnique")) {
				kind = CollectionProcessorNode.COLLECTUNIQUE;
			}
			break;
		case 'r':
			if(instruction.equals("reject")) {
				kind = CollectionProcessorNode.REJECT;
			}
			break;
		case 's':
			if(instruction.equals("select")) {
				kind = CollectionProcessorNode.SELECT;
			} else if(instruction.equals("sort")) {
				kind = CollectionProcessorNode.SORT;
			}
			break;
		case 'f':
			if(instruction.equals("forAll")) {
				kind = CollectionProcessorNode.FORALL;
			}
			break;
		case 'e':
			if(instruction.equals("exists")) {
				kind = CollectionProcessorNode.EXISTS;
			}
			break;
		case 'g':
			if(instruction.equals("groupBy")) {
				kind = CollectionProcessorNode.GROUPBY;
			}
			break;
		}
		if(kind == 0) {
			reporter.error(offset, endoffset, line, "unknown collection processing instruction: " + instruction);
			return new ErrorNode(source, offset, endoffset);
		}
		return new CollectionProcessorNode(context, kind, varName, foreachExpr, source, offset, endoffset);
	}

	private Node createEscapedId(String escid, int offset, int endoffset) {
		int sharp = escid.indexOf('#');
		if( sharp >= 0 ) {
			Integer index = new Integer(escid.substring(sharp+1));
			escid = escid.substring(0, sharp);
			return new IndexNode(new SelectNode(null,escid,source,offset,endoffset), new LiteralNode(index,source,offset,endoffset),source,offset,endoffset);

		} else {
			return new SelectNode(null,escid,source,offset,endoffset);
		}
	}

	private void skipSpaces(int offset) {
		killEnds = offset+1;
	}

	private void checkFqn(String templateName, int offset, int endoffset, int line) {
		if( templateName.indexOf('.') >= 0 && templatePackage != null) {
			reporter.error(offset, endoffset, line, "template name should be simple identifier");
		}
	}
	private static final int lapg_action[] = {
		-3, -1, 7, -11, -19, 3, 5, 6, -1, 2, 38, 37, 35, 36, -1, -27,
		29, 34, 32, 33, -1, 17, -1, 11, -1, 4, -1, 9, -1, -41, 82, 84,
		-1, -1, 109, -1, -1, -1, -1, -1, 86, -1, 108, 85, -1, -97, -1, -1,
		-105, -1, 141, -1, -1, -135, 96, 95, 83, 113, -187, -233, -261, -287, 134, 136,
		-309, 50, 28, -1, 52, -317, -1, -1, 8, -329, -1, -359, -371, -425, -1, -433,
		-1, -441, -1, -1, -449, -457, 112, 111, -463, -1, 138, -513, -1, -1, 31, 30,
		39, 70, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, 79, 58, 61, -521, -1, 13, -527, -535, 27, -1,
		135, -545, 42, -575, -1, 47, 48, -1, -1, -583, -589, -595, -607, -1, -1, 106,
		107, 105, -1, 99, -1, -1, 98, 81, -1, -615, -1, -1, -669, -715, -761, -807,
		-853, -899, -945, -991, -1037, 125, -1083, -1113, -1141, -1169, -1191, -1, 137, -1, -1, -1,
		53, -1, 15, -1, -1213, 87, -1, -1, 44, 45, 51, -1, -1219, -1, -1229, -1,
		63, -1, -1, 23, -1, 103, -1, 139, 94, -1235, -1, -1265, -1, 78, -1, -1,
		21, -1295, 18, -1, 57, -1305, -1, -1, 72, 73, 102, -1, -1, 66, -1313, -1,
		-1, -1, -1325, -1, -1, -1, 133, -1, -1, -1, -1, -1371, -1, -1, 68, 67,
		64, 25, 97, 104, -1, 89, -1379, 92, -1, -1, 60, -1, -1, -1, -1, -1,
		-1, -1, 16, -1409, 69, -1, 90, -1, 93, 59, 74, -1, -1, 65, 91, -1,
		-1, -2, -2
	};

	private static final short lapg_lalr[] = {
		2, -1, 6, -1, 0, 0, -1, -2, 11, -1, 31, -1, 28, 10, -1, -2,
		2, -1, 6, -1, 0, 1, -1, -2, 2, -1, 3, -1, 4, -1, 5, -1,
		6, -1, 0, 140, -1, -2, 47, -1, 54, -1, 24, 80, 30, 80, 36, 80,
		37, 80, 38, 80, 39, 80, 40, 80, 41, 80, 42, 80, 45, 80, 46, 80,
		48, 80, 49, 80, 50, 80, 51, 80, 52, 80, 53, 80, 55, 80, 56, 80,
		58, 80, 59, 80, 60, 80, 61, 80, 62, 80, 63, 80, -1, -2, 11, -1,
		1, 10, 57, 10, -1, -2, 1, -1, 8, -1, 9, -1, 16, -1, 26, -1,
		27, -1, 32, -1, 33, -1, 35, -1, 39, -1, 43, -1, 45, -1, 47, -1,
		46, 55, -1, -2, 45, -1, 49, -1, 56, -1, 24, 110, 30, 110, 36, 110,
		37, 110, 38, 110, 39, 110, 40, 110, 41, 110, 42, 110, 46, 110, 48, 110,
		50, 110, 51, 110, 52, 110, 53, 110, 55, 110, 58, 110, 59, 110, 60, 110,
		61, 110, 62, 110, 63, 110, -1, -2, 38, -1, 39, -1, 40, -1, 41, -1,
		42, -1, 58, -1, 59, -1, 60, -1, 61, -1, 24, 123, 30, 123, 36, 123,
		37, 123, 46, 123, 48, 123, 50, 123, 51, 123, 52, 123, 53, 123, 55, 123,
		62, 123, 63, 123, -1, -2, 24, -1, 30, 126, 36, 126, 37, 126, 46, 126,
		48, 126, 50, 126, 51, 126, 52, 126, 53, 126, 55, 126, 62, 126, 63, 126,
		-1, -2, 53, -1, 55, -1, 30, 129, 36, 129, 37, 129, 46, 129, 48, 129,
		50, 129, 51, 129, 52, 129, 62, 129, 63, 129, -1, -2, 51, -1, 52, -1,
		63, -1, 30, 132, 36, 132, 37, 132, 46, 132, 48, 132, 50, 132, 62, 132,
		-1, -2, 50, -1, 36, 40, 37, 40, -1, -2, 47, -1, 49, -1, 17, 12,
		36, 12, 37, 12, -1, -2, 1, -1, 8, -1, 9, -1, 16, -1, 26, -1,
		27, -1, 32, -1, 33, -1, 35, -1, 39, -1, 43, -1, 45, -1, 47, -1,
		48, 55, -1, -2, 47, -1, 49, -1, 17, 41, 36, 41, 37, 41, -1, -2,
		47, -1, 24, 80, 30, 80, 36, 80, 37, 80, 38, 80, 39, 80, 40, 80,
		41, 80, 42, 80, 45, 80, 46, 80, 48, 80, 49, 80, 50, 80, 51, 80,
		52, 80, 53, 80, 55, 80, 56, 80, 58, 80, 59, 80, 60, 80, 61, 80,
		62, 80, 63, 80, -1, -2, 50, -1, 36, 46, 37, 46, -1, -2, 50, -1,
		36, 76, 37, 76, -1, -2, 50, -1, 36, 75, 37, 75, -1, -2, 50, -1,
		36, 49, 37, 49, -1, -2, 1, -1, 57, 19, -1, -2, 47, -1, 54, -1,
		57, -1, 62, -1, 24, 80, 38, 80, 39, 80, 40, 80, 41, 80, 42, 80,
		45, 80, 46, 80, 49, 80, 50, 80, 51, 80, 52, 80, 53, 80, 55, 80,
		56, 80, 58, 80, 59, 80, 60, 80, 61, 80, 63, 80, -1, -2, 50, -1,
		46, 56, 48, 56, -1, -2, 1, -1, 48, 19, -1, -2, 17, -1, 36, 14,
		37, 14, -1, -2, 47, -1, 49, -1, 17, 12, 54, 12, -1, -2, 1, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1, 35, -1,
		39, -1, 43, -1, 45, -1, 47, -1, 48, 55, -1, -2, 17, -1, 36, 43,
		37, 43, -1, -2, 1, -1, 48, 100, -1, -2, 2, -1, 6, 62, -1, -2,
		48, 22, 50, 22, 57, 22, 1, 52, 49, 52, -1, -2, 50, -1, 48, 20,
		57, 20, -1, -2, 47, -1, 24, 88, 30, 88, 36, 88, 37, 88, 38, 88,
		39, 88, 40, 88, 41, 88, 42, 88, 45, 88, 46, 88, 48, 88, 49, 88,
		50, 88, 51, 88, 52, 88, 53, 88, 55, 88, 56, 88, 58, 88, 59, 88,
		60, 88, 61, 88, 62, 88, 63, 88, -1, -2, 38, 117, 39, 117, 40, -1,
		41, -1, 42, -1, 58, 117, 59, 117, 60, 117, 61, 117, 24, 117, 30, 117,
		36, 117, 37, 117, 46, 117, 48, 117, 50, 117, 51, 117, 52, 117, 53, 117,
		55, 117, 62, 117, 63, 117, -1, -2, 38, 118, 39, 118, 40, -1, 41, -1,
		42, -1, 58, 118, 59, 118, 60, 118, 61, 118, 24, 118, 30, 118, 36, 118,
		37, 118, 46, 118, 48, 118, 50, 118, 51, 118, 52, 118, 53, 118, 55, 118,
		62, 118, 63, 118, -1, -2, 38, 114, 39, 114, 40, 114, 41, 114, 42, 114,
		58, 114, 59, 114, 60, 114, 61, 114, 24, 114, 30, 114, 36, 114, 37, 114,
		46, 114, 48, 114, 50, 114, 51, 114, 52, 114, 53, 114, 55, 114, 62, 114,
		63, 114, -1, -2, 38, 115, 39, 115, 40, 115, 41, 115, 42, 115, 58, 115,
		59, 115, 60, 115, 61, 115, 24, 115, 30, 115, 36, 115, 37, 115, 46, 115,
		48, 115, 50, 115, 51, 115, 52, 115, 53, 115, 55, 115, 62, 115, 63, 115,
		-1, -2, 38, 116, 39, 116, 40, 116, 41, 116, 42, 116, 58, 116, 59, 116,
		60, 116, 61, 116, 24, 116, 30, 116, 36, 116, 37, 116, 46, 116, 48, 116,
		50, 116, 51, 116, 52, 116, 53, 116, 55, 116, 62, 116, 63, 116, -1, -2,
		38, -1, 39, -1, 40, -1, 41, -1, 42, -1, 58, 121, 59, 121, 60, 121,
		61, 121, 24, 121, 30, 121, 36, 121, 37, 121, 46, 121, 48, 121, 50, 121,
		51, 121, 52, 121, 53, 121, 55, 121, 62, 121, 63, 121, -1, -2, 38, -1,
		39, -1, 40, -1, 41, -1, 42, -1, 58, 122, 59, 122, 60, 122, 61, 122,
		24, 122, 30, 122, 36, 122, 37, 122, 46, 122, 48, 122, 50, 122, 51, 122,
		52, 122, 53, 122, 55, 122, 62, 122, 63, 122, -1, -2, 38, -1, 39, -1,
		40, -1, 41, -1, 42, -1, 58, 119, 59, 119, 60, 119, 61, 119, 24, 119,
		30, 119, 36, 119, 37, 119, 46, 119, 48, 119, 50, 119, 51, 119, 52, 119,
		53, 119, 55, 119, 62, 119, 63, 119, -1, -2, 38, -1, 39, -1, 40, -1,
		41, -1, 42, -1, 58, 120, 59, 120, 60, 120, 61, 120, 24, 120, 30, 120,
		36, 120, 37, 120, 46, 120, 48, 120, 50, 120, 51, 120, 52, 120, 53, 120,
		55, 120, 62, 120, 63, 120, -1, -2, 49, -1, 24, 124, 30, 124, 36, 124,
		37, 124, 46, 124, 48, 124, 50, 124, 51, 124, 52, 124, 53, 124, 55, 124,
		62, 124, 63, 124, -1, -2, 24, -1, 30, 127, 36, 127, 37, 127, 46, 127,
		48, 127, 50, 127, 51, 127, 52, 127, 53, 127, 55, 127, 62, 127, 63, 127,
		-1, -2, 24, -1, 30, 128, 36, 128, 37, 128, 46, 128, 48, 128, 50, 128,
		51, 128, 52, 128, 53, 128, 55, 128, 62, 128, 63, 128, -1, -2, 51, 130,
		52, 130, 30, 130, 36, 130, 37, 130, 46, 130, 48, 130, 50, 130, 62, 130,
		63, 130, -1, -2, 51, -1, 52, 131, 30, 131, 36, 131, 37, 131, 46, 131,
		48, 131, 50, 131, 62, 131, 63, 131, -1, -2, 17, -1, 54, 14, -1, -2,
		30, -1, 50, -1, 36, 71, 37, 71, -1, -2, 50, -1, 48, 101, -1, -2,
		1, -1, 8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1,
		35, -1, 39, -1, 43, -1, 45, -1, 47, -1, 48, 55, -1, -2, 1, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1, 35, -1,
		39, -1, 43, -1, 45, -1, 47, -1, 48, 55, -1, -2, 49, -1, 36, 26,
		37, 26, 54, 26, -1, -2, 50, -1, 36, 54, 37, 54, -1, -2, 48, 24,
		50, 24, 57, 24, 1, 52, 49, 52, -1, -2, 44, -1, 47, -1, 24, 80,
		38, 80, 39, 80, 40, 80, 41, 80, 42, 80, 45, 80, 48, 80, 49, 80,
		50, 80, 51, 80, 52, 80, 53, 80, 55, 80, 56, 80, 58, 80, 59, 80,
		60, 80, 61, 80, 63, 80, -1, -2, 50, -1, 36, 77, 37, 77, -1, -2,
		1, -1, 8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1,
		35, -1, 39, -1, 43, -1, 45, -1, 47, -1, 48, 55, -1, -2, 30, -1,
		36, 71, 37, 71, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 73, 89, 102, 115, 128, 144, 149, 202, 256, 261, 263, 265, 269, 271,
		276, 329, 337, 342, 347, 347, 353, 355, 355, 358, 358, 411, 464, 465, 470, 472,
		473, 526, 579, 584, 637, 649, 658, 668, 731, 741, 751, 761, 814, 815, 870, 874,
		939, 950, 960, 983, 986, 989, 990, 995, 996, 997, 1001, 1011, 1021, 1031, 1041, 1046,
		1047, 1047, 1052, 1053, 1054, 1056, 1058, 1060, 1062, 1064, 1066, 1068, 1070, 1072, 1078, 1087,
		1100, 1113, 1118, 1119, 1129, 1130, 1131, 1144, 1146, 1159, 1160, 1162, 1175, 1180, 1182, 1187,
		1240, 1293, 1346, 1348, 1351, 1404, 1457, 1508, 1550, 1590, 1630, 1668, 1690, 1711, 1717, 1718,
		1723, 1724, 1726, 1728, 1730, 1732, 1733, 1734, 1735, 1741, 1742, 1744, 1745
	};

	private static final short lapg_sym_from[] = {
		271, 272, 14, 22, 26, 32, 33, 35, 36, 37, 38, 39, 41, 44, 46, 47,
		48, 49, 70, 73, 74, 85, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107,
		108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 121, 122, 129, 132, 136, 137,
		141, 146, 148, 149, 154, 177, 183, 187, 194, 196, 201, 203, 204, 206, 215, 223,
		225, 233, 234, 236, 237, 244, 246, 249, 263, 0, 1, 4, 8, 15, 20, 28,
		67, 138, 207, 220, 232, 248, 257, 261, 267, 1, 8, 15, 20, 28, 67, 207,
		220, 232, 248, 257, 261, 267, 1, 8, 15, 20, 28, 67, 207, 220, 232, 248,
		257, 261, 267, 1, 8, 15, 20, 28, 67, 207, 220, 232, 248, 257, 261, 267,
		0, 1, 4, 8, 15, 20, 28, 67, 193, 207, 220, 232, 248, 257, 261, 267,
		67, 220, 232, 257, 267, 14, 26, 33, 36, 38, 41, 44, 46, 47, 48, 49,
		73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 112, 113, 114,
		115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196, 201, 203, 204, 206,
		215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26, 33, 36, 38, 41,
		44, 46, 47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108,
		109, 110, 111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183,
		187, 196, 201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263,
		14, 26, 117, 237, 249, 3, 45, 219, 237, 26, 117, 237, 249, 117, 237, 14,
		26, 117, 237, 249, 14, 26, 33, 36, 38, 41, 44, 46, 47, 48, 49, 73,
		74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 112, 113, 114, 115,
		116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196, 201, 203, 204, 206, 215,
		225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26, 117, 124, 131, 180, 237,
		249, 14, 26, 117, 237, 249, 14, 26, 117, 237, 249, 14, 26, 117, 174, 237,
		249, 78, 80, 59, 167, 168, 14, 26, 33, 36, 38, 41, 44, 46, 47, 48,
		49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 112, 113,
		114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196, 201, 203, 204,
		206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26, 33, 36, 38,
		41, 44, 46, 47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107,
		108, 109, 111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183,
		187, 196, 201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263,
		24, 14, 26, 117, 237, 249, 188, 259, 3, 14, 26, 33, 36, 38, 41, 44,
		46, 47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109,
		111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196,
		201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26,
		33, 36, 38, 41, 44, 46, 47, 48, 49, 73, 74, 98, 101, 102, 103, 104,
		105, 106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146,
		149, 154, 183, 187, 196, 201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244,
		246, 249, 263, 14, 26, 117, 237, 249, 14, 26, 33, 36, 38, 41, 44, 46,
		47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111,
		112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196, 201,
		203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 51, 52, 71,
		83, 173, 174, 179, 224, 231, 251, 253, 254, 51, 52, 83, 173, 174, 179, 231,
		253, 254, 58, 156, 157, 158, 159, 160, 161, 162, 163, 164, 14, 26, 33, 36,
		38, 41, 44, 46, 47, 48, 49, 58, 73, 74, 98, 101, 102, 103, 104, 105,
		106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149,
		154, 156, 157, 158, 159, 160, 161, 162, 163, 164, 183, 187, 196, 201, 203, 204,
		206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 58, 156, 157, 158, 159,
		160, 161, 162, 163, 164, 58, 156, 157, 158, 159, 160, 161, 162, 163, 164, 58,
		156, 157, 158, 159, 160, 161, 162, 163, 164, 14, 26, 33, 36, 38, 41, 44,
		46, 47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109,
		111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196,
		201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 226, 14,
		26, 33, 36, 38, 41, 44, 46, 47, 48, 49, 53, 73, 74, 98, 101, 102,
		103, 104, 105, 106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 129, 132,
		135, 136, 146, 149, 154, 183, 187, 196, 201, 203, 204, 206, 215, 225, 233, 234,
		236, 237, 244, 246, 249, 263, 89, 92, 152, 252, 14, 26, 29, 33, 36, 38,
		41, 44, 46, 47, 48, 49, 69, 73, 74, 75, 76, 82, 88, 98, 100, 101,
		102, 103, 104, 105, 106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 125,
		129, 132, 136, 146, 149, 153, 154, 155, 183, 187, 196, 201, 203, 204, 206, 215,
		225, 226, 228, 233, 234, 236, 237, 244, 246, 249, 263, 93, 127, 175, 182, 191,
		202, 227, 229, 255, 256, 268, 53, 69, 75, 82, 125, 141, 155, 166, 209, 223,
		64, 77, 79, 81, 83, 84, 89, 91, 93, 140, 152, 188, 190, 202, 213, 214,
		224, 231, 235, 251, 253, 255, 268, 61, 169, 170, 61, 169, 170, 60, 29, 88,
		189, 198, 211, 60, 53, 88, 142, 189, 198, 58, 156, 157, 158, 159, 160, 161,
		162, 163, 164, 58, 156, 157, 158, 159, 160, 161, 162, 163, 164, 58, 156, 157,
		158, 159, 160, 161, 162, 163, 164, 58, 156, 157, 158, 159, 160, 161, 162, 163,
		164, 88, 171, 189, 198, 255, 61, 14, 26, 117, 237, 249, 0, 0, 0, 4,
		0, 4, 0, 4, 3, 45, 0, 4, 69, 125, 85, 121, 124, 180, 8, 28,
		1, 8, 20, 207, 248, 261, 51, 52, 83, 173, 174, 179, 231, 253, 254, 1,
		8, 15, 20, 28, 67, 207, 220, 232, 248, 257, 261, 267, 1, 8, 15, 20,
		28, 67, 207, 220, 232, 248, 257, 261, 267, 14, 26, 117, 237, 249, 77, 22,
		32, 39, 70, 85, 100, 110, 121, 177, 194, 131, 75, 1, 8, 15, 20, 28,
		67, 207, 220, 232, 248, 257, 261, 267, 67, 257, 1, 8, 15, 20, 28, 67,
		207, 220, 232, 248, 257, 261, 267, 193, 193, 220, 1, 8, 15, 20, 28, 67,
		207, 220, 232, 248, 257, 261, 267, 14, 26, 117, 237, 249, 188, 259, 67, 220,
		232, 257, 267, 14, 26, 33, 36, 38, 41, 44, 46, 47, 48, 49, 73, 74,
		98, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 112, 113, 114, 115, 116,
		117, 129, 132, 136, 146, 149, 154, 183, 187, 196, 201, 203, 204, 206, 215, 225,
		233, 234, 236, 237, 244, 246, 249, 263, 14, 26, 33, 36, 38, 41, 44, 46,
		47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111,
		112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196, 201,
		203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26, 33,
		36, 38, 41, 44, 46, 47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105,
		106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149,
		154, 183, 187, 196, 201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246,
		249, 263, 48, 137, 88, 189, 198, 14, 26, 33, 36, 38, 41, 44, 46, 47,
		48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 112,
		113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196, 201, 203,
		204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26, 33, 36,
		38, 41, 44, 46, 47, 48, 49, 73, 74, 98, 101, 102, 103, 104, 105, 106,
		107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154,
		183, 187, 196, 201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249,
		263, 14, 26, 33, 36, 38, 41, 44, 48, 49, 73, 74, 98, 101, 102, 103,
		104, 105, 106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 129, 132, 136,
		146, 149, 154, 183, 187, 196, 201, 203, 204, 206, 215, 225, 233, 234, 236, 237,
		244, 246, 249, 263, 14, 26, 33, 36, 38, 41, 44, 48, 49, 73, 74, 98,
		111, 112, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196,
		201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26,
		33, 36, 38, 41, 44, 48, 49, 73, 74, 98, 113, 114, 115, 116, 117, 129,
		132, 136, 146, 149, 154, 183, 187, 196, 201, 203, 204, 206, 215, 225, 233, 234,
		236, 237, 244, 246, 249, 263, 14, 26, 33, 36, 38, 41, 44, 48, 49, 73,
		74, 98, 113, 114, 115, 116, 117, 129, 132, 136, 146, 149, 154, 183, 187, 196,
		201, 203, 204, 206, 215, 225, 233, 234, 236, 237, 244, 246, 249, 263, 14, 26,
		33, 36, 38, 41, 44, 48, 49, 73, 74, 98, 115, 116, 117, 129, 132, 136,
		146, 149, 154, 183, 187, 196, 201, 203, 204, 206, 215, 225, 233, 234, 236, 237,
		244, 246, 249, 263, 14, 26, 36, 38, 41, 44, 49, 98, 116, 117, 136, 154,
		183, 196, 206, 215, 233, 236, 237, 244, 249, 263, 14, 26, 36, 38, 41, 44,
		49, 98, 117, 136, 154, 183, 196, 206, 215, 233, 236, 237, 244, 249, 263, 48,
		73, 129, 201, 203, 246, 1, 14, 26, 117, 237, 249, 0, 3, 45, 69, 125,
		124, 180, 85, 121, 75, 131, 77, 48, 73, 129, 201, 203, 246, 138, 188, 259,
		137
	};

	private static final short lapg_sym_to[] = {
		273, 274, 29, 68, 29, 68, 76, 78, 29, 80, 29, 68, 29, 29, 76, 76,
		88, 29, 68, 76, 76, 139, 29, 153, 68, 76, 76, 76, 76, 76, 76, 76,
		76, 76, 68, 76, 76, 76, 76, 76, 29, 29, 139, 176, 76, 76, 29, 189,
		195, 76, 198, 76, 29, 68, 29, 76, 222, 29, 226, 76, 76, 29, 29, 241,
		76, 29, 76, 29, 29, 29, 76, 29, 29, 2, 10, 2, 10, 10, 10, 10,
		10, 192, 10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
		12, 12, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13,
		3, 14, 3, 26, 14, 14, 26, 117, 219, 14, 237, 249, 14, 117, 14, 249,
		118, 118, 118, 118, 118, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 165, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		32, 32, 32, 32, 32, 21, 21, 236, 236, 71, 173, 173, 173, 174, 254, 33,
		33, 33, 33, 33, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 35, 35, 35, 177, 183, 177, 35,
		35, 36, 36, 36, 36, 36, 37, 37, 37, 37, 37, 38, 38, 38, 206, 38,
		38, 135, 136, 110, 110, 110, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		70, 41, 41, 41, 41, 41, 215, 215, 22, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 44, 44, 44, 44, 44, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 94, 94, 126,
		94, 94, 94, 94, 242, 94, 258, 94, 94, 95, 95, 95, 95, 95, 95, 95,
		95, 95, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 102, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 102, 102, 102, 102, 102, 102, 102, 102, 102, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 103, 103, 103, 103, 103,
		103, 103, 103, 103, 103, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 105,
		105, 105, 105, 105, 105, 105, 105, 105, 105, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 244, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 98, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		187, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 147, 150, 200, 259, 49, 49, 73, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 121, 49, 49, 129, 73, 137, 73, 49, 154, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 121,
		49, 49, 49, 49, 49, 201, 49, 203, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 73, 246, 49, 49, 49, 49, 49, 49, 49, 49, 151, 181, 208, 212, 218,
		228, 245, 247, 262, 264, 270, 99, 122, 122, 122, 122, 122, 122, 122, 122, 122,
		116, 132, 116, 116, 116, 116, 148, 149, 116, 194, 116, 116, 148, 116, 116, 234,
		116, 116, 116, 116, 116, 116, 116, 113, 113, 113, 114, 114, 114, 111, 74, 143,
		143, 143, 233, 112, 100, 144, 196, 144, 144, 106, 106, 106, 106, 106, 106, 106,
		106, 106, 106, 107, 107, 107, 107, 107, 107, 107, 107, 107, 107, 108, 108, 108,
		108, 108, 108, 108, 108, 108, 108, 109, 109, 109, 109, 109, 109, 109, 109, 109,
		109, 145, 204, 145, 145, 263, 115, 50, 50, 50, 50, 50, 271, 4, 5, 25,
		6, 6, 7, 7, 23, 23, 8, 8, 123, 123, 140, 140, 178, 178, 27, 72,
		15, 28, 67, 232, 257, 267, 96, 97, 138, 205, 207, 210, 248, 260, 261, 16,
		16, 66, 16, 66, 66, 16, 238, 66, 16, 66, 16, 66, 17, 17, 17, 17,
		17, 17, 17, 17, 17, 17, 17, 17, 17, 51, 51, 51, 51, 51, 133, 69,
		75, 82, 125, 141, 155, 166, 141, 209, 223, 184, 130, 18, 18, 18, 18, 18,
		18, 18, 18, 18, 18, 18, 18, 18, 119, 265, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 220, 221, 239, 20, 20, 20, 20, 20, 20,
		20, 20, 20, 20, 20, 20, 20, 52, 52, 52, 52, 52, 216, 216, 120, 240,
		250, 120, 269, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 89, 190, 146, 146, 225, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57, 57, 57,
		57, 57, 57, 86, 87, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 156, 157, 158,
		159, 160, 161, 162, 163, 164, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		167, 168, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 60, 60,
		60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60,
		60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60,
		60, 60, 60, 60, 60, 60, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61,
		61, 61, 169, 170, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61,
		61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 62, 62,
		77, 62, 62, 62, 62, 90, 62, 90, 128, 62, 171, 62, 62, 90, 186, 62,
		197, 199, 62, 62, 214, 62, 90, 90, 230, 62, 62, 243, 62, 252, 62, 62,
		62, 90, 62, 62, 63, 63, 63, 63, 63, 63, 63, 63, 172, 63, 63, 63,
		63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 64, 64, 79, 81, 83, 84,
		93, 152, 64, 188, 202, 213, 224, 231, 235, 251, 253, 64, 255, 64, 268, 91,
		91, 91, 91, 91, 91, 272, 65, 65, 65, 65, 65, 9, 24, 85, 124, 180,
		179, 211, 142, 175, 131, 185, 134, 92, 127, 182, 227, 229, 256, 193, 217, 266,
		191
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 1, 2, 1, 1, 1, 3, 2, 0, 1, 0, 1, 0, 1,
		9, 1, 6, 0, 1, 3, 1, 2, 3, 4, 2, 3, 2, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 3, 1, 0, 1, 0, 1, 4, 0, 1,
		3, 2, 1, 2, 1, 3, 2, 0, 1, 3, 3, 7, 5, 1, 0, 1,
		7, 11, 1, 2, 2, 4, 3, 0, 1, 5, 9, 2, 2, 2, 3, 1,
		1, 3, 1, 1, 1, 1, 1, 4, 3, 6, 8, 10, 6, 8, 4, 1,
		1, 6, 3, 3, 0, 1, 5, 3, 5, 1, 1, 1, 1, 1, 1, 2,
		2, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3,
		3, 1, 3, 3, 1, 5, 1, 3, 1, 3, 1, 3, 1, 1
	};

	private static final short lapg_rlex[] = {
		112, 112, 66, 67, 67, 68, 68, 68, 69, 69, 113, 113, 114, 114, 115, 115,
		70, 71, 72, 116, 116, 73, 74, 74, 74, 74, 75, 76, 77, 77, 78, 78,
		79, 79, 79, 79, 79, 79, 79, 80, 81, 117, 117, 118, 118, 81, 119, 119,
		81, 81, 81, 82, 83, 83, 84, 120, 120, 85, 86, 87, 87, 87, 121, 121,
		88, 88, 89, 89, 89, 90, 91, 122, 122, 92, 92, 92, 92, 93, 94, 94,
		95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95,
		95, 96, 97, 97, 123, 123, 97, 98, 98, 99, 99, 99, 100, 100, 101, 101,
		101, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 103, 103, 103, 104, 104,
		104, 105, 105, 105, 106, 106, 107, 107, 108, 108, 109, 109, 110, 111
	};

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"any",
		"escdollar",
		"escid",
		"escint",
		"'${'",
		"'$/'",
		"icon",
		"ccon",
		"Lcall",
		"Lcached",
		"Lcase",
		"Lend",
		"Lelse",
		"Leval",
		"Lfalse",
		"Lfor",
		"Lfile",
		"Lforeach",
		"Lgrep",
		"Lif",
		"Lin",
		"Limport",
		"Lis",
		"Lmap",
		"Lnew",
		"Lnull",
		"Lquery",
		"Lswitch",
		"Lseparator",
		"Ltemplate",
		"Ltrue",
		"Lself",
		"Lassert",
		"'{'",
		"'}'",
		"'-}'",
		"'+'",
		"'-'",
		"'*'",
		"'/'",
		"'%'",
		"'!'",
		"'|'",
		"'['",
		"']'",
		"'('",
		"')'",
		"'.'",
		"','",
		"'&&'",
		"'||'",
		"'=='",
		"'='",
		"'!='",
		"'->'",
		"'=>'",
		"'<='",
		"'>='",
		"'<'",
		"'>'",
		"':'",
		"'?'",
		"_skip",
		"error",
		"input",
		"definitions",
		"definition",
		"template_def",
		"query_def",
		"cached_flag",
		"template_start",
		"parameters",
		"parameter_list",
		"context_type",
		"template_end",
		"instructions",
		"'[-]}'",
		"instruction",
		"simple_instruction",
		"sentence",
		"comma_expr",
		"qualified_id",
		"template_for_expr",
		"template_arguments",
		"control_instruction",
		"else_clause",
		"switch_instruction",
		"case_list",
		"one_case",
		"control_start",
		"control_sentence",
		"separator_expr",
		"control_end",
		"primary_expression",
		"closure",
		"complex_data",
		"map_entries",
		"map_separator",
		"bcon",
		"unary_expression",
		"binary_op",
		"instanceof_expression",
		"equality_expression",
		"conditional_op",
		"conditional_expression",
		"assignment_expression",
		"expression",
		"expression_list",
		"body",
		"syntax_problem",
		"definitionsopt",
		"cached_flagopt",
		"parametersopt",
		"context_typeopt",
		"parameter_listopt",
		"template_argumentsopt",
		"template_for_expropt",
		"comma_expropt",
		"expression_listopt",
		"anyopt",
		"separator_expropt",
		"map_entriesopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 66;
		public static final int definitions = 67;
		public static final int definition = 68;
		public static final int template_def = 69;
		public static final int query_def = 70;
		public static final int cached_flag = 71;
		public static final int template_start = 72;
		public static final int parameters = 73;
		public static final int parameter_list = 74;
		public static final int context_type = 75;
		public static final int template_end = 76;
		public static final int instructions = 77;
		public static final int LSQUAREMINUSRSQUARERCURLY = 78;
		public static final int instruction = 79;
		public static final int simple_instruction = 80;
		public static final int sentence = 81;
		public static final int comma_expr = 82;
		public static final int qualified_id = 83;
		public static final int template_for_expr = 84;
		public static final int template_arguments = 85;
		public static final int control_instruction = 86;
		public static final int else_clause = 87;
		public static final int switch_instruction = 88;
		public static final int case_list = 89;
		public static final int one_case = 90;
		public static final int control_start = 91;
		public static final int control_sentence = 92;
		public static final int separator_expr = 93;
		public static final int control_end = 94;
		public static final int primary_expression = 95;
		public static final int closure = 96;
		public static final int complex_data = 97;
		public static final int map_entries = 98;
		public static final int map_separator = 99;
		public static final int bcon = 100;
		public static final int unary_expression = 101;
		public static final int binary_op = 102;
		public static final int instanceof_expression = 103;
		public static final int equality_expression = 104;
		public static final int conditional_op = 105;
		public static final int conditional_expression = 106;
		public static final int assignment_expression = 107;
		public static final int expression = 108;
		public static final int expression_list = 109;
		public static final int body = 110;
		public static final int syntax_problem = 111;
		public static final int definitionsopt = 112;
		public static final int cached_flagopt = 113;
		public static final int parametersopt = 114;
		public static final int context_typeopt = 115;
		public static final int parameter_listopt = 116;
		public static final int template_argumentsopt = 117;
		public static final int template_for_expropt = 118;
		public static final int comma_expropt = 119;
		public static final int expression_listopt = 120;
		public static final int anyopt = 121;
		public static final int separator_expropt = 122;
		public static final int map_entriesopt = 123;
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

	protected final int lapg_state_sym(int state, int symbol) {
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
	protected TemplatesLexer lapg_lexer;

	private Object parse(TemplatesLexer lexer, int initialState, int finalState) throws IOException, ParseException {

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
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 65) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].lexem = 65;
			lapg_m[lapg_head].sym = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 65);
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
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 3:  // definitions ::= definition
				 lapg_gg.sym = new ArrayList(); if (((IBundleEntity)lapg_m[lapg_head].sym) != null) ((List<IBundleEntity>)lapg_gg.sym).add(((IBundleEntity)lapg_m[lapg_head].sym)); 
				break;
			case 4:  // definitions ::= definitions definition
				 if (((IBundleEntity)lapg_m[lapg_head].sym) != null) ((List<IBundleEntity>)lapg_gg.sym).add(((IBundleEntity)lapg_m[lapg_head].sym)); 
				break;
			case 7:  // definition ::= any
				 lapg_gg.sym = null; 
				break;
			case 8:  // template_def ::= template_start instructions template_end
				 ((TemplateNode)lapg_m[lapg_head - 2].sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head - 1].sym)); 
				break;
			case 16:  // query_def ::= '${' cached_flagopt Lquery qualified_id parametersopt context_typeopt '=' expression '}'
				 lapg_gg.sym = new QueryNode(((String)lapg_m[lapg_head - 5].sym), ((List<ParameterNode>)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), templatePackage, ((ExpressionNode)lapg_m[lapg_head - 1].sym), ((Boolean)lapg_m[lapg_head - 7].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head - 5].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head - 8].line); 
				break;
			case 17:  // cached_flag ::= Lcached
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 18:  // template_start ::= '${' Ltemplate qualified_id parametersopt context_typeopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head - 3].sym), ((List<ParameterNode>)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym), templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head - 3].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head - 5].line); 
				break;
			case 21:  // parameters ::= '(' parameter_listopt ')'
				 lapg_gg.sym = ((List<ParameterNode>)lapg_m[lapg_head - 1].sym); 
				break;
			case 22:  // parameter_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset)); 
				break;
			case 23:  // parameter_list ::= qualified_id identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 1].offset, lapg_gg.endoffset)); 
				break;
			case 24:  // parameter_list ::= parameter_list ',' identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset)); 
				break;
			case 25:  // parameter_list ::= parameter_list ',' qualified_id identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 1].offset, lapg_gg.endoffset)); 
				break;
			case 26:  // context_type ::= Lfor qualified_id
				 lapg_gg.sym = ((String)lapg_m[lapg_head].sym); 
				break;
			case 28:  // instructions ::= instructions instruction
				 if (((Node)lapg_m[lapg_head].sym) != null) ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head].sym)); 
				break;
			case 29:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); if (((Node)lapg_m[lapg_head].sym)!=null) ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head].sym)); 
				break;
			case 30:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head].offset+1); 
				break;
			case 35:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head].sym), lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 37:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 38:  // instruction ::= any
				 lapg_gg.sym = new TextNode(source, rawText(lapg_gg.offset, lapg_gg.endoffset), lapg_gg.endoffset); 
				break;
			case 39:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head - 1].sym); 
				break;
			case 45:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head - 2].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), ((ExpressionNode)lapg_m[lapg_head].sym), templatePackage, true, source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 48:  // sentence ::= Leval conditional_expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head - 1].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 49:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 50:  // sentence ::= syntax_problem
				 lapg_gg.sym = null; 
				break;
			case 51:  // comma_expr ::= ',' conditional_expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 53:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 54:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 57:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head - 1].sym); 
				break;
			case 58:  // control_instruction ::= control_start instructions else_clause
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head - 1].sym)); applyElse(((CompoundNode)lapg_m[lapg_head - 2].sym),((ElseIfNode)lapg_m[lapg_head].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 59:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				 lapg_gg.sym = new ElseIfNode(((ExpressionNode)lapg_m[lapg_head - 3].sym), ((ArrayList<Node>)lapg_m[lapg_head - 1].sym), ((ElseIfNode)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 6].offset, lapg_m[lapg_head - 1].endoffset); 
				break;
			case 60:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new ElseIfNode(null, ((ArrayList<Node>)lapg_m[lapg_head - 1].sym), null, source, lapg_m[lapg_head - 4].offset, lapg_m[lapg_head - 1].endoffset); 
				break;
			case 61:  // else_clause ::= control_end
				 lapg_gg.sym = null; 
				break;
			case 64:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head - 4].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), null, source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head - 2].offset,lapg_m[lapg_head - 2].endoffset, lapg_m[lapg_head - 2].line); 
				break;
			case 65:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head - 8].sym), ((ArrayList)lapg_m[lapg_head - 5].sym), ((ArrayList<Node>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head - 6].offset,lapg_m[lapg_head - 6].endoffset, lapg_m[lapg_head - 6].line); 
				break;
			case 66:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head].sym)); 
				break;
			case 67:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head].sym)); 
				break;
			case 68:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head].sym)); 
				break;
			case 69:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 70:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head - 1].sym); 
				break;
			case 73:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), null, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head - 7].sym), ((ExpressionNode)lapg_m[lapg_head - 4].sym), ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // separator_expr ::= Lseparator expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 80:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head - 3].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head - 5].sym), ((String)lapg_m[lapg_head - 3].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 90:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head - 7].sym), ((String)lapg_m[lapg_head - 5].sym), ((String)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 91:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ':' expression ')'
				 lapg_gg.sym = createMapCollect(((ExpressionNode)lapg_m[lapg_head - 9].sym), ((String)lapg_m[lapg_head - 7].sym), ((String)lapg_m[lapg_head - 5].sym), ((ExpressionNode)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 92:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head - 3].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), ((ExpressionNode)lapg_m[lapg_head - 5].sym), templatePackage, false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head - 4].sym),((ArrayList)lapg_m[lapg_head - 1].sym),((ExpressionNode)lapg_m[lapg_head - 7].sym),templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // closure ::= '{' cached_flagopt parameter_listopt '=>' expression '}'
				 lapg_gg.sym = new ClosureNode(((Boolean)lapg_m[lapg_head - 4].sym) != null, ((List<ParameterNode>)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((Map<String,ExpressionNode>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // complex_data ::= Lnew qualified_id '(' map_entriesopt ')'
				 lapg_gg.sym = new CreateClassNode(((String)lapg_m[lapg_head - 3].sym), ((Map<String,ExpressionNode>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // map_entries ::= identifier map_separator conditional_expression
				 lapg_gg.sym = new LinkedHashMap(); ((Map<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 104:  // map_entries ::= map_entries ',' identifier map_separator conditional_expression
				 ((Map<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 108:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 109:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 111:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // binary_op ::= binary_op '*' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // binary_op ::= binary_op '/' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // binary_op ::= binary_op '%' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // binary_op ::= binary_op '+' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // binary_op ::= binary_op '-' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // binary_op ::= binary_op '<' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // binary_op ::= binary_op '>' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // binary_op ::= binary_op '<=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // binary_op ::= binary_op '>=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // conditional_op ::= conditional_op '&&' conditional_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // conditional_op ::= conditional_op '||' conditional_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 133:  // conditional_expression ::= conditional_op '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head - 4].sym), ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 135:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 137:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 138:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 139:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 140:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, null, templatePackage, source, lapg_gg.offset, lapg_gg.endoffset);
							((TemplateNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head].sym));
						
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol sym) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(LapgSymbol sym) {
	}

	public List<IBundleEntity> parseInput(TemplatesLexer lexer) throws IOException, ParseException {
		return (List<IBundleEntity>) parse(lexer, 0, 273);
	}

	public TemplateNode parseBody(TemplatesLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1, 274);
	}
}
