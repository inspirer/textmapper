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
		-3, -1, 8, -11, -19, 3, 7, -1, 2, 37, 36, 34, 35, -1, -27, 28,
		33, 31, 32, -1, 16, -1, 10, -1, 4, -1, 6, -1, -41, 80, 82, -1,
		-1, 107, -1, -1, -1, -1, -1, 84, -1, 106, 83, -1, -97, -1, -1, -105,
		-1, -1, -1, -135, 94, 93, 81, 111, -187, -233, -261, -287, 132, 134, -309, 27,
		-1, 50, -317, -1, -1, 5, -329, -1, -359, -371, -425, -1, -433, -1, -441, -1,
		-1, -449, -457, 110, 109, -463, -1, 136, -513, -1, -1, 30, 29, 38, 68, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, 77, 56, 59, -521, -1, 12, -527, -535, 26, -1, 133, -545, 41,
		-575, -1, 46, 47, -1, -1, -583, -589, -595, -607, -1, -1, 104, 105, 103, -1,
		97, -1, -1, 96, 79, -1, -615, -1, -1, -669, -715, -761, -807, -853, -899, -945,
		-991, -1037, 123, -1083, -1113, -1141, -1169, -1191, -1, 135, -1, -1, -1, 51, -1, 14,
		-1, -1213, 85, -1, -1, 43, 44, 49, -1, -1219, -1, -1229, -1, 61, -1, -1,
		22, -1, 101, -1, 137, 92, -1235, -1, -1265, -1, 76, -1, -1, 20, -1295, 17,
		-1, 55, -1305, -1, -1, 70, 71, 100, -1, -1, 64, -1313, -1, -1, -1, -1325,
		-1, -1, -1, 131, -1, -1, -1, -1, -1371, -1, -1, 66, 65, 62, 24, 95,
		102, -1, 87, -1379, 90, -1, -1, 58, -1, -1, -1, -1, -1, -1, -1, 15,
		-1409, 67, -1, 88, -1, 91, 57, 72, -1, -1, 63, 89, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		2, -1, 6, -1, 0, 0, -1, -2, 11, -1, 31, -1, 28, 9, -1, -2,
		2, -1, 6, -1, 0, 1, -1, -2, 2, -1, 3, -1, 4, -1, 5, -1,
		6, -1, 0, 138, -1, -2, 47, -1, 54, -1, 24, 78, 30, 78, 36, 78,
		37, 78, 38, 78, 39, 78, 40, 78, 41, 78, 42, 78, 45, 78, 46, 78,
		48, 78, 49, 78, 50, 78, 51, 78, 52, 78, 53, 78, 55, 78, 56, 78,
		58, 78, 59, 78, 60, 78, 61, 78, 62, 78, 63, 78, -1, -2, 11, -1,
		1, 9, 57, 9, -1, -2, 1, -1, 8, -1, 9, -1, 16, -1, 26, -1,
		27, -1, 32, -1, 33, -1, 35, -1, 39, -1, 43, -1, 45, -1, 47, -1,
		46, 53, -1, -2, 45, -1, 49, -1, 56, -1, 24, 108, 30, 108, 36, 108,
		37, 108, 38, 108, 39, 108, 40, 108, 41, 108, 42, 108, 46, 108, 48, 108,
		50, 108, 51, 108, 52, 108, 53, 108, 55, 108, 58, 108, 59, 108, 60, 108,
		61, 108, 62, 108, 63, 108, -1, -2, 38, -1, 39, -1, 40, -1, 41, -1,
		42, -1, 58, -1, 59, -1, 60, -1, 61, -1, 24, 121, 30, 121, 36, 121,
		37, 121, 46, 121, 48, 121, 50, 121, 51, 121, 52, 121, 53, 121, 55, 121,
		62, 121, 63, 121, -1, -2, 24, -1, 30, 124, 36, 124, 37, 124, 46, 124,
		48, 124, 50, 124, 51, 124, 52, 124, 53, 124, 55, 124, 62, 124, 63, 124,
		-1, -2, 53, -1, 55, -1, 30, 127, 36, 127, 37, 127, 46, 127, 48, 127,
		50, 127, 51, 127, 52, 127, 62, 127, 63, 127, -1, -2, 51, -1, 52, -1,
		63, -1, 30, 130, 36, 130, 37, 130, 46, 130, 48, 130, 50, 130, 62, 130,
		-1, -2, 50, -1, 36, 39, 37, 39, -1, -2, 47, -1, 49, -1, 17, 11,
		36, 11, 37, 11, -1, -2, 1, -1, 8, -1, 9, -1, 16, -1, 26, -1,
		27, -1, 32, -1, 33, -1, 35, -1, 39, -1, 43, -1, 45, -1, 47, -1,
		48, 53, -1, -2, 47, -1, 49, -1, 17, 40, 36, 40, 37, 40, -1, -2,
		47, -1, 24, 78, 30, 78, 36, 78, 37, 78, 38, 78, 39, 78, 40, 78,
		41, 78, 42, 78, 45, 78, 46, 78, 48, 78, 49, 78, 50, 78, 51, 78,
		52, 78, 53, 78, 55, 78, 56, 78, 58, 78, 59, 78, 60, 78, 61, 78,
		62, 78, 63, 78, -1, -2, 50, -1, 36, 45, 37, 45, -1, -2, 50, -1,
		36, 74, 37, 74, -1, -2, 50, -1, 36, 73, 37, 73, -1, -2, 50, -1,
		36, 48, 37, 48, -1, -2, 1, -1, 57, 18, -1, -2, 47, -1, 54, -1,
		57, -1, 62, -1, 24, 78, 38, 78, 39, 78, 40, 78, 41, 78, 42, 78,
		45, 78, 46, 78, 49, 78, 50, 78, 51, 78, 52, 78, 53, 78, 55, 78,
		56, 78, 58, 78, 59, 78, 60, 78, 61, 78, 63, 78, -1, -2, 50, -1,
		46, 54, 48, 54, -1, -2, 1, -1, 48, 18, -1, -2, 17, -1, 36, 13,
		37, 13, -1, -2, 47, -1, 49, -1, 17, 11, 54, 11, -1, -2, 1, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1, 35, -1,
		39, -1, 43, -1, 45, -1, 47, -1, 48, 53, -1, -2, 17, -1, 36, 42,
		37, 42, -1, -2, 1, -1, 48, 98, -1, -2, 2, -1, 6, 60, -1, -2,
		48, 21, 50, 21, 57, 21, 1, 50, 49, 50, -1, -2, 50, -1, 48, 19,
		57, 19, -1, -2, 47, -1, 24, 86, 30, 86, 36, 86, 37, 86, 38, 86,
		39, 86, 40, 86, 41, 86, 42, 86, 45, 86, 46, 86, 48, 86, 49, 86,
		50, 86, 51, 86, 52, 86, 53, 86, 55, 86, 56, 86, 58, 86, 59, 86,
		60, 86, 61, 86, 62, 86, 63, 86, -1, -2, 38, 115, 39, 115, 40, -1,
		41, -1, 42, -1, 58, 115, 59, 115, 60, 115, 61, 115, 24, 115, 30, 115,
		36, 115, 37, 115, 46, 115, 48, 115, 50, 115, 51, 115, 52, 115, 53, 115,
		55, 115, 62, 115, 63, 115, -1, -2, 38, 116, 39, 116, 40, -1, 41, -1,
		42, -1, 58, 116, 59, 116, 60, 116, 61, 116, 24, 116, 30, 116, 36, 116,
		37, 116, 46, 116, 48, 116, 50, 116, 51, 116, 52, 116, 53, 116, 55, 116,
		62, 116, 63, 116, -1, -2, 38, 112, 39, 112, 40, 112, 41, 112, 42, 112,
		58, 112, 59, 112, 60, 112, 61, 112, 24, 112, 30, 112, 36, 112, 37, 112,
		46, 112, 48, 112, 50, 112, 51, 112, 52, 112, 53, 112, 55, 112, 62, 112,
		63, 112, -1, -2, 38, 113, 39, 113, 40, 113, 41, 113, 42, 113, 58, 113,
		59, 113, 60, 113, 61, 113, 24, 113, 30, 113, 36, 113, 37, 113, 46, 113,
		48, 113, 50, 113, 51, 113, 52, 113, 53, 113, 55, 113, 62, 113, 63, 113,
		-1, -2, 38, 114, 39, 114, 40, 114, 41, 114, 42, 114, 58, 114, 59, 114,
		60, 114, 61, 114, 24, 114, 30, 114, 36, 114, 37, 114, 46, 114, 48, 114,
		50, 114, 51, 114, 52, 114, 53, 114, 55, 114, 62, 114, 63, 114, -1, -2,
		38, -1, 39, -1, 40, -1, 41, -1, 42, -1, 58, 119, 59, 119, 60, 119,
		61, 119, 24, 119, 30, 119, 36, 119, 37, 119, 46, 119, 48, 119, 50, 119,
		51, 119, 52, 119, 53, 119, 55, 119, 62, 119, 63, 119, -1, -2, 38, -1,
		39, -1, 40, -1, 41, -1, 42, -1, 58, 120, 59, 120, 60, 120, 61, 120,
		24, 120, 30, 120, 36, 120, 37, 120, 46, 120, 48, 120, 50, 120, 51, 120,
		52, 120, 53, 120, 55, 120, 62, 120, 63, 120, -1, -2, 38, -1, 39, -1,
		40, -1, 41, -1, 42, -1, 58, 117, 59, 117, 60, 117, 61, 117, 24, 117,
		30, 117, 36, 117, 37, 117, 46, 117, 48, 117, 50, 117, 51, 117, 52, 117,
		53, 117, 55, 117, 62, 117, 63, 117, -1, -2, 38, -1, 39, -1, 40, -1,
		41, -1, 42, -1, 58, 118, 59, 118, 60, 118, 61, 118, 24, 118, 30, 118,
		36, 118, 37, 118, 46, 118, 48, 118, 50, 118, 51, 118, 52, 118, 53, 118,
		55, 118, 62, 118, 63, 118, -1, -2, 49, -1, 24, 122, 30, 122, 36, 122,
		37, 122, 46, 122, 48, 122, 50, 122, 51, 122, 52, 122, 53, 122, 55, 122,
		62, 122, 63, 122, -1, -2, 24, -1, 30, 125, 36, 125, 37, 125, 46, 125,
		48, 125, 50, 125, 51, 125, 52, 125, 53, 125, 55, 125, 62, 125, 63, 125,
		-1, -2, 24, -1, 30, 126, 36, 126, 37, 126, 46, 126, 48, 126, 50, 126,
		51, 126, 52, 126, 53, 126, 55, 126, 62, 126, 63, 126, -1, -2, 51, 128,
		52, 128, 30, 128, 36, 128, 37, 128, 46, 128, 48, 128, 50, 128, 62, 128,
		63, 128, -1, -2, 51, -1, 52, 129, 30, 129, 36, 129, 37, 129, 46, 129,
		48, 129, 50, 129, 62, 129, 63, 129, -1, -2, 17, -1, 54, 13, -1, -2,
		30, -1, 50, -1, 36, 69, 37, 69, -1, -2, 50, -1, 48, 99, -1, -2,
		1, -1, 8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1,
		35, -1, 39, -1, 43, -1, 45, -1, 47, -1, 48, 53, -1, -2, 1, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1, 35, -1,
		39, -1, 43, -1, 45, -1, 47, -1, 48, 53, -1, -2, 49, -1, 36, 25,
		37, 25, 54, 25, -1, -2, 50, -1, 36, 52, 37, 52, -1, -2, 48, 23,
		50, 23, 57, 23, 1, 50, 49, 50, -1, -2, 44, -1, 47, -1, 24, 78,
		38, 78, 39, 78, 40, 78, 41, 78, 42, 78, 45, 78, 48, 78, 49, 78,
		50, 78, 51, 78, 52, 78, 53, 78, 55, 78, 56, 78, 58, 78, 59, 78,
		60, 78, 61, 78, 63, 78, -1, -2, 50, -1, 36, 75, 37, 75, -1, -2,
		1, -1, 8, -1, 9, -1, 16, -1, 26, -1, 27, -1, 32, -1, 33, -1,
		35, -1, 39, -1, 43, -1, 45, -1, 47, -1, 48, 53, -1, -2, 30, -1,
		36, 69, 37, 69, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 73, 89, 102, 115, 128, 144, 149, 202, 256, 261, 263, 265, 269, 271,
		276, 329, 337, 342, 347, 347, 353, 355, 355, 358, 358, 411, 464, 465, 470, 472,
		473, 526, 579, 584, 637, 649, 658, 668, 731, 741, 751, 761, 814, 815, 870, 874,
		939, 950, 960, 983, 986, 989, 990, 995, 996, 997, 1001, 1011, 1021, 1031, 1041, 1046,
		1047, 1047, 1048, 1049, 1051, 1053, 1055, 1057, 1059, 1061, 1063, 1065, 1071, 1080, 1093, 1106,
		1111, 1112, 1122, 1123, 1124, 1137, 1139, 1152, 1153, 1155, 1168, 1173, 1175, 1180, 1233, 1286,
		1339, 1341, 1344, 1397, 1450, 1501, 1543, 1583, 1623, 1661, 1683, 1704, 1710, 1711, 1712, 1714,
		1716, 1718, 1720, 1721, 1722, 1723, 1729, 1730, 1732, 1733
	};

	private static final short lapg_sym_from[] = {
		268, 269, 13, 21, 25, 31, 32, 34, 35, 36, 37, 38, 40, 43, 45, 46,
		47, 48, 67, 70, 71, 82, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104,
		105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 118, 119, 126, 129, 133, 134,
		138, 143, 145, 146, 151, 174, 180, 184, 191, 193, 198, 200, 201, 203, 212, 220,
		222, 230, 231, 233, 234, 241, 243, 246, 260, 0, 1, 4, 7, 14, 19, 27,
		64, 135, 204, 217, 229, 245, 254, 258, 264, 1, 7, 14, 19, 27, 64, 204,
		217, 229, 245, 254, 258, 264, 1, 7, 14, 19, 27, 64, 204, 217, 229, 245,
		254, 258, 264, 1, 7, 14, 19, 27, 64, 204, 217, 229, 245, 254, 258, 264,
		0, 1, 4, 7, 14, 19, 27, 64, 190, 204, 217, 229, 245, 254, 258, 264,
		64, 217, 229, 254, 264, 13, 25, 32, 35, 37, 40, 43, 45, 46, 47, 48,
		70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108, 109, 110, 111,
		112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203,
		212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 13, 25, 32, 35, 37, 40,
		43, 45, 46, 47, 48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105,
		106, 107, 108, 109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180,
		184, 193, 198, 200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260,
		13, 25, 114, 234, 246, 3, 44, 216, 234, 25, 114, 234, 246, 114, 234, 13,
		25, 114, 234, 246, 13, 25, 32, 35, 37, 40, 43, 45, 46, 47, 48, 70,
		71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108, 109, 110, 111, 112,
		113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203, 212,
		222, 230, 231, 233, 234, 241, 243, 246, 260, 13, 25, 114, 121, 128, 177, 234,
		246, 13, 25, 114, 234, 246, 13, 25, 114, 234, 246, 13, 25, 114, 171, 234,
		246, 75, 77, 57, 164, 165, 13, 25, 32, 35, 37, 40, 43, 45, 46, 47,
		48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108, 109, 110,
		111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201,
		203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 13, 25, 32, 35, 37,
		40, 43, 45, 46, 47, 48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104,
		105, 106, 108, 109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180,
		184, 193, 198, 200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260,
		23, 13, 25, 114, 234, 246, 185, 256, 3, 13, 25, 32, 35, 37, 40, 43,
		45, 46, 47, 48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106,
		108, 109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193,
		198, 200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 13, 25,
		32, 35, 37, 40, 43, 45, 46, 47, 48, 70, 71, 95, 98, 99, 100, 101,
		102, 103, 104, 105, 106, 108, 109, 110, 111, 112, 113, 114, 126, 129, 133, 143,
		146, 151, 180, 184, 193, 198, 200, 201, 203, 212, 222, 230, 231, 233, 234, 241,
		243, 246, 260, 13, 25, 114, 234, 246, 13, 25, 32, 35, 37, 40, 43, 45,
		46, 47, 48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108,
		109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198,
		200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 49, 50, 68,
		80, 170, 171, 176, 221, 228, 248, 250, 251, 49, 50, 80, 170, 171, 176, 228,
		250, 251, 56, 153, 154, 155, 156, 157, 158, 159, 160, 161, 13, 25, 32, 35,
		37, 40, 43, 45, 46, 47, 48, 56, 70, 71, 95, 98, 99, 100, 101, 102,
		103, 104, 105, 106, 108, 109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146,
		151, 153, 154, 155, 156, 157, 158, 159, 160, 161, 180, 184, 193, 198, 200, 201,
		203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 56, 153, 154, 155, 156,
		157, 158, 159, 160, 161, 56, 153, 154, 155, 156, 157, 158, 159, 160, 161, 56,
		153, 154, 155, 156, 157, 158, 159, 160, 161, 13, 25, 32, 35, 37, 40, 43,
		45, 46, 47, 48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106,
		108, 109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193,
		198, 200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 223, 13,
		25, 32, 35, 37, 40, 43, 45, 46, 47, 48, 51, 70, 71, 95, 98, 99,
		100, 101, 102, 103, 104, 105, 106, 108, 109, 110, 111, 112, 113, 114, 126, 129,
		132, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203, 212, 222, 230, 231,
		233, 234, 241, 243, 246, 260, 86, 89, 149, 249, 13, 25, 28, 32, 35, 37,
		40, 43, 45, 46, 47, 48, 66, 70, 71, 72, 73, 79, 85, 95, 97, 98,
		99, 100, 101, 102, 103, 104, 105, 106, 108, 109, 110, 111, 112, 113, 114, 122,
		126, 129, 133, 143, 146, 150, 151, 152, 180, 184, 193, 198, 200, 201, 203, 212,
		222, 223, 225, 230, 231, 233, 234, 241, 243, 246, 260, 90, 124, 172, 179, 188,
		199, 224, 226, 252, 253, 265, 51, 66, 72, 79, 122, 138, 152, 163, 206, 220,
		62, 74, 76, 78, 80, 81, 86, 88, 90, 137, 149, 185, 187, 199, 210, 211,
		221, 228, 232, 248, 250, 252, 265, 59, 166, 167, 59, 166, 167, 58, 28, 85,
		186, 195, 208, 58, 51, 85, 139, 186, 195, 56, 153, 154, 155, 156, 157, 158,
		159, 160, 161, 56, 153, 154, 155, 156, 157, 158, 159, 160, 161, 56, 153, 154,
		155, 156, 157, 158, 159, 160, 161, 56, 153, 154, 155, 156, 157, 158, 159, 160,
		161, 85, 168, 186, 195, 252, 59, 0, 0, 0, 4, 0, 4, 3, 44, 0,
		4, 66, 122, 82, 118, 121, 177, 7, 27, 1, 7, 19, 204, 245, 258, 49,
		50, 80, 170, 171, 176, 228, 250, 251, 1, 7, 14, 19, 27, 64, 204, 217,
		229, 245, 254, 258, 264, 1, 7, 14, 19, 27, 64, 204, 217, 229, 245, 254,
		258, 264, 13, 25, 114, 234, 246, 74, 21, 31, 38, 67, 82, 97, 107, 118,
		174, 191, 128, 72, 1, 7, 14, 19, 27, 64, 204, 217, 229, 245, 254, 258,
		264, 64, 254, 1, 7, 14, 19, 27, 64, 204, 217, 229, 245, 254, 258, 264,
		190, 190, 217, 1, 7, 14, 19, 27, 64, 204, 217, 229, 245, 254, 258, 264,
		13, 25, 114, 234, 246, 185, 256, 64, 217, 229, 254, 264, 13, 25, 32, 35,
		37, 40, 43, 45, 46, 47, 48, 70, 71, 95, 98, 99, 100, 101, 102, 103,
		104, 105, 106, 108, 109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151,
		180, 184, 193, 198, 200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246,
		260, 13, 25, 32, 35, 37, 40, 43, 45, 46, 47, 48, 70, 71, 95, 98,
		99, 100, 101, 102, 103, 104, 105, 106, 108, 109, 110, 111, 112, 113, 114, 126,
		129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203, 212, 222, 230, 231,
		233, 234, 241, 243, 246, 260, 13, 25, 32, 35, 37, 40, 43, 45, 46, 47,
		48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108, 109, 110,
		111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201,
		203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 47, 134, 85, 186, 195,
		13, 25, 32, 35, 37, 40, 43, 45, 46, 47, 48, 70, 71, 95, 98, 99,
		100, 101, 102, 103, 104, 105, 106, 108, 109, 110, 111, 112, 113, 114, 126, 129,
		133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203, 212, 222, 230, 231, 233,
		234, 241, 243, 246, 260, 13, 25, 32, 35, 37, 40, 43, 45, 46, 47, 48,
		70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108, 109, 110, 111,
		112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203,
		212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 13, 25, 32, 35, 37, 40,
		43, 47, 48, 70, 71, 95, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108,
		109, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198,
		200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 13, 25, 32,
		35, 37, 40, 43, 47, 48, 70, 71, 95, 108, 109, 110, 111, 112, 113, 114,
		126, 129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203, 212, 222, 230,
		231, 233, 234, 241, 243, 246, 260, 13, 25, 32, 35, 37, 40, 43, 47, 48,
		70, 71, 95, 110, 111, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184,
		193, 198, 200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 13,
		25, 32, 35, 37, 40, 43, 47, 48, 70, 71, 95, 110, 111, 112, 113, 114,
		126, 129, 133, 143, 146, 151, 180, 184, 193, 198, 200, 201, 203, 212, 222, 230,
		231, 233, 234, 241, 243, 246, 260, 13, 25, 32, 35, 37, 40, 43, 47, 48,
		70, 71, 95, 112, 113, 114, 126, 129, 133, 143, 146, 151, 180, 184, 193, 198,
		200, 201, 203, 212, 222, 230, 231, 233, 234, 241, 243, 246, 260, 13, 25, 35,
		37, 40, 43, 48, 95, 113, 114, 133, 151, 180, 193, 203, 212, 230, 233, 234,
		241, 246, 260, 13, 25, 35, 37, 40, 43, 48, 95, 114, 133, 151, 180, 193,
		203, 212, 230, 233, 234, 241, 246, 260, 47, 70, 126, 198, 200, 243, 1, 0,
		3, 44, 66, 122, 121, 177, 82, 118, 72, 128, 74, 47, 70, 126, 198, 200,
		243, 135, 185, 256, 134
	};

	private static final short lapg_sym_to[] = {
		270, 271, 28, 65, 28, 65, 73, 75, 28, 77, 28, 65, 28, 28, 73, 73,
		85, 28, 65, 73, 73, 136, 28, 150, 65, 73, 73, 73, 73, 73, 73, 73,
		73, 73, 65, 73, 73, 73, 73, 73, 28, 28, 136, 173, 73, 73, 28, 186,
		192, 73, 195, 73, 28, 65, 28, 73, 219, 28, 223, 73, 73, 28, 28, 238,
		73, 28, 73, 28, 28, 28, 73, 28, 28, 2, 9, 2, 9, 9, 9, 9,
		9, 189, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10,
		10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
		3, 13, 3, 25, 13, 13, 25, 114, 216, 13, 234, 246, 13, 114, 13, 246,
		115, 115, 115, 115, 115, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 162, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		31, 31, 31, 31, 31, 20, 20, 233, 233, 68, 170, 170, 170, 171, 251, 32,
		32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 34, 34, 34, 174, 180, 174, 34,
		34, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 37, 37, 37, 203, 37,
		37, 132, 133, 107, 107, 107, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		67, 40, 40, 40, 40, 40, 212, 212, 21, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 43, 43, 43, 43, 43, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 91, 91, 123,
		91, 91, 91, 91, 239, 91, 255, 91, 91, 92, 92, 92, 92, 92, 92, 92,
		92, 92, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 99, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 99, 99, 99, 99, 99, 99, 99, 99, 99, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 100, 100, 100, 100, 100,
		100, 100, 100, 100, 100, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 102,
		102, 102, 102, 102, 102, 102, 102, 102, 102, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 241, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 95, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		184, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 144, 147, 197, 256, 48, 48, 70, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 118, 48, 48, 126, 70, 134, 70, 48, 151, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 118,
		48, 48, 48, 48, 48, 198, 48, 200, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 70, 243, 48, 48, 48, 48, 48, 48, 48, 48, 148, 178, 205, 209, 215,
		225, 242, 244, 259, 261, 267, 96, 119, 119, 119, 119, 119, 119, 119, 119, 119,
		113, 129, 113, 113, 113, 113, 145, 146, 113, 191, 113, 113, 145, 113, 113, 231,
		113, 113, 113, 113, 113, 113, 113, 110, 110, 110, 111, 111, 111, 108, 71, 140,
		140, 140, 230, 109, 97, 141, 193, 141, 141, 103, 103, 103, 103, 103, 103, 103,
		103, 103, 103, 104, 104, 104, 104, 104, 104, 104, 104, 104, 104, 105, 105, 105,
		105, 105, 105, 105, 105, 105, 105, 106, 106, 106, 106, 106, 106, 106, 106, 106,
		106, 142, 201, 142, 142, 260, 112, 268, 4, 5, 24, 6, 6, 22, 22, 7,
		7, 120, 120, 137, 137, 175, 175, 26, 69, 14, 27, 64, 229, 254, 264, 93,
		94, 135, 202, 204, 207, 245, 257, 258, 15, 15, 63, 15, 63, 63, 15, 235,
		63, 15, 63, 15, 63, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 49, 49, 49, 49, 49, 130, 66, 72, 79, 122, 138, 152, 163, 138,
		206, 220, 181, 127, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
		17, 116, 262, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		217, 218, 236, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
		50, 50, 50, 50, 50, 213, 213, 117, 237, 247, 117, 266, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 86, 187, 143, 143, 222,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 55, 55, 55, 55, 55, 55, 55, 83, 84, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 153, 154, 155, 156, 157, 158, 159, 160, 161, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 164, 165, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 166, 167, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 60, 60, 74, 60, 60, 60, 60, 87, 60,
		87, 125, 60, 168, 60, 60, 87, 183, 60, 194, 196, 60, 60, 211, 60, 87,
		87, 227, 60, 60, 240, 60, 249, 60, 60, 60, 87, 60, 60, 61, 61, 61,
		61, 61, 61, 61, 61, 169, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61,
		61, 61, 61, 62, 62, 76, 78, 80, 81, 90, 149, 62, 185, 199, 210, 221,
		228, 232, 248, 250, 62, 252, 62, 265, 88, 88, 88, 88, 88, 88, 269, 8,
		23, 82, 121, 177, 176, 208, 139, 172, 128, 182, 131, 89, 124, 179, 224, 226,
		253, 190, 214, 263, 188
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 1, 2, 3, 2, 1, 1, 0, 1, 0, 1, 0, 1, 9,
		1, 6, 0, 1, 3, 1, 2, 3, 4, 2, 3, 2, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 3, 1, 0, 1, 0, 1, 4, 0, 1, 3,
		2, 2, 1, 3, 2, 0, 1, 3, 3, 7, 5, 1, 0, 1, 7, 11,
		1, 2, 2, 4, 3, 0, 1, 5, 9, 2, 2, 2, 3, 1, 1, 3,
		1, 1, 1, 1, 1, 4, 3, 6, 8, 10, 6, 8, 4, 1, 1, 6,
		3, 3, 0, 1, 5, 3, 5, 1, 1, 1, 1, 1, 1, 2, 2, 1,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 1,
		3, 3, 1, 5, 1, 3, 1, 3, 1, 3, 1
	};

	private static final short lapg_rlex[] = {
		109, 109, 65, 66, 66, 67, 67, 67, 67, 110, 110, 111, 111, 112, 112, 68,
		69, 70, 113, 113, 71, 72, 72, 72, 72, 73, 74, 75, 75, 76, 76, 77,
		77, 77, 77, 77, 77, 77, 78, 79, 114, 114, 115, 115, 79, 116, 116, 79,
		79, 80, 81, 81, 82, 117, 117, 83, 84, 85, 85, 85, 118, 118, 86, 86,
		87, 87, 87, 88, 89, 119, 119, 90, 90, 90, 90, 91, 92, 92, 93, 93,
		93, 93, 93, 93, 93, 93, 93, 93, 93, 93, 93, 93, 93, 93, 93, 94,
		95, 95, 120, 120, 95, 96, 96, 97, 97, 97, 98, 98, 99, 99, 99, 100,
		100, 100, 100, 100, 100, 100, 100, 100, 100, 101, 101, 101, 102, 102, 102, 103,
		103, 103, 104, 104, 105, 105, 106, 106, 107, 107, 108
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
		"input",
		"definitions",
		"definition",
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
		public static final int input = 65;
		public static final int definitions = 66;
		public static final int definition = 67;
		public static final int query_def = 68;
		public static final int cached_flag = 69;
		public static final int template_start = 70;
		public static final int parameters = 71;
		public static final int parameter_list = 72;
		public static final int context_type = 73;
		public static final int template_end = 74;
		public static final int instructions = 75;
		public static final int LSQUAREMINUSRSQUARERCURLY = 76;
		public static final int instruction = 77;
		public static final int simple_instruction = 78;
		public static final int sentence = 79;
		public static final int comma_expr = 80;
		public static final int qualified_id = 81;
		public static final int template_for_expr = 82;
		public static final int template_arguments = 83;
		public static final int control_instruction = 84;
		public static final int else_clause = 85;
		public static final int switch_instruction = 86;
		public static final int case_list = 87;
		public static final int one_case = 88;
		public static final int control_start = 89;
		public static final int control_sentence = 90;
		public static final int separator_expr = 91;
		public static final int control_end = 92;
		public static final int primary_expression = 93;
		public static final int closure = 94;
		public static final int complex_data = 95;
		public static final int map_entries = 96;
		public static final int map_separator = 97;
		public static final int bcon = 98;
		public static final int unary_expression = 99;
		public static final int binary_op = 100;
		public static final int instanceof_expression = 101;
		public static final int equality_expression = 102;
		public static final int conditional_op = 103;
		public static final int conditional_expression = 104;
		public static final int assignment_expression = 105;
		public static final int expression = 106;
		public static final int expression_list = 107;
		public static final int body = 108;
		public static final int definitionsopt = 109;
		public static final int cached_flagopt = 110;
		public static final int parametersopt = 111;
		public static final int context_typeopt = 112;
		public static final int parameter_listopt = 113;
		public static final int template_argumentsopt = 114;
		public static final int template_for_expropt = 115;
		public static final int comma_expropt = 116;
		public static final int expression_listopt = 117;
		public static final int anyopt = 118;
		public static final int separator_expropt = 119;
		public static final int map_entriesopt = 120;
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
	protected TemplatesLexer lapg_lexer;

	private Object parse(TemplatesLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = initialState;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != finalState) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, 
					MessageFormat.format("syntax error before line {0}",
					lapg_lexer.getTokenLine()));
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
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
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
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
			case 5:  // definition ::= template_start instructions template_end
				 ((TemplateNode)lapg_m[lapg_head-2].sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 8:  // definition ::= any
				 lapg_gg.sym = null; 
				break;
			case 15:  // query_def ::= '${' cached_flagopt Lquery qualified_id parametersopt context_typeopt '=' expression '}'
				 lapg_gg.sym = new QueryNode(((String)lapg_m[lapg_head-5].sym), ((List<ParameterNode>)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), templatePackage, ((ExpressionNode)lapg_m[lapg_head-1].sym), ((Boolean)lapg_m[lapg_head-7].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head-5].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-8].line); 
				break;
			case 16:  // cached_flag ::= Lcached
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 17:  // template_start ::= '${' Ltemplate qualified_id parametersopt context_typeopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-3].sym), ((List<ParameterNode>)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head-3].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-5].line); 
				break;
			case 20:  // parameters ::= '(' parameter_listopt ')'
				 lapg_gg.sym = ((List<ParameterNode>)lapg_m[lapg_head-1].sym); 
				break;
			case 21:  // parameter_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset)); 
				break;
			case 22:  // parameter_list ::= qualified_id identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head-1].offset, lapg_gg.endoffset)); 
				break;
			case 23:  // parameter_list ::= parameter_list ',' identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset)); 
				break;
			case 24:  // parameter_list ::= parameter_list ',' qualified_id identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head-1].offset, lapg_gg.endoffset)); 
				break;
			case 25:  // context_type ::= Lfor qualified_id
				 lapg_gg.sym = ((String)lapg_m[lapg_head].sym); 
				break;
			case 27:  // instructions ::= instructions instruction
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head].sym)); 
				break;
			case 28:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head].sym)); 
				break;
			case 29:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head].offset+1); 
				break;
			case 34:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head].sym), lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 35:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 37:  // instruction ::= any
				 lapg_gg.sym = new TextNode(source, rawText(lapg_gg.offset, lapg_gg.endoffset), lapg_gg.endoffset); 
				break;
			case 38:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 44:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head].sym), templatePackage, true, source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 47:  // sentence ::= Leval conditional_expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 48:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 49:  // comma_expr ::= ',' conditional_expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 51:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 52:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 55:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 56:  // control_instruction ::= control_start instructions else_clause
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); applyElse(((CompoundNode)lapg_m[lapg_head-2].sym),((ElseIfNode)lapg_m[lapg_head].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 57:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				 lapg_gg.sym = new ElseIfNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ArrayList<Node>)lapg_m[lapg_head-1].sym), ((ElseIfNode)lapg_m[lapg_head].sym), source, lapg_m[lapg_head-6].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 58:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new ElseIfNode(null, ((ArrayList<Node>)lapg_m[lapg_head-1].sym), null, source, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 59:  // else_clause ::= control_end
				 lapg_gg.sym = null; 
				break;
			case 62:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head-2].offset,lapg_m[lapg_head-2].endoffset, lapg_m[lapg_head-2].line); 
				break;
			case 63:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-8].sym), ((ArrayList)lapg_m[lapg_head-5].sym), ((ArrayList<Node>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head-6].offset,lapg_m[lapg_head-6].endoffset, lapg_m[lapg_head-6].line); 
				break;
			case 64:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head].sym)); 
				break;
			case 65:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head].sym)); 
				break;
			case 66:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head].sym)); 
				break;
			case 67:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 68:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 71:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), null, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-7].sym), ((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // separator_expr ::= Lseparator expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 78:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 89:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ':' expression ')'
				 lapg_gg.sym = createMapCollect(((ExpressionNode)lapg_m[lapg_head-9].sym), ((String)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 90:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-5].sym), templatePackage, false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 91:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // closure ::= '{' cached_flagopt parameter_listopt '=>' expression '}'
				 lapg_gg.sym = new ClosureNode(((Boolean)lapg_m[lapg_head-4].sym) != null, ((List<ParameterNode>)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((Map<String,ExpressionNode>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // complex_data ::= Lnew qualified_id '(' map_entriesopt ')'
				 lapg_gg.sym = new CreateClassNode(((String)lapg_m[lapg_head-3].sym), ((Map<String,ExpressionNode>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // map_entries ::= identifier map_separator conditional_expression
				 lapg_gg.sym = new LinkedHashMap(); ((Map<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 102:  // map_entries ::= map_entries ',' identifier map_separator conditional_expression
				 ((Map<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 106:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 107:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 109:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // binary_op ::= binary_op '*' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 113:  // binary_op ::= binary_op '/' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // binary_op ::= binary_op '%' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // binary_op ::= binary_op '+' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // binary_op ::= binary_op '-' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // binary_op ::= binary_op '<' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // binary_op ::= binary_op '>' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // binary_op ::= binary_op '<=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // binary_op ::= binary_op '>=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // conditional_op ::= conditional_op '&&' conditional_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // conditional_op ::= conditional_op '||' conditional_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // conditional_expression ::= conditional_op '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 133:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 135:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 136:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 137:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 138:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, null, templatePackage, source, lapg_gg.offset, lapg_gg.endoffset);
							((TemplateNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head].sym));
						
				break;
		}
	}

	public List<IBundleEntity> parseInput(TemplatesLexer lexer) throws IOException, ParseException {
		return (List<IBundleEntity>) parse(lexer, 0, 270);
	}

	public TemplateNode parseBody(TemplatesLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1, 271);
	}
}
