package org.textway.templates.ast;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		-1, 99, -1, -1, -1, -1, 84, -1, 98, 83, -1, -1, -1, -97, -1, -1,
		-1, -123, 93, 81, 103, -175, -221, -249, -275, -297, 125, 127, -317, 27, -1, 50,
		-325, -1, -1, 5, -337, -1, -363, -375, -429, -1, -437, -1, -445, -1, -453, 102,
		101, -461, -1, 129, -507, -1, -1, 30, 29, 38, 68, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 77,
		56, 59, -515, -1, 12, -521, -529, 26, -1, 126, -539, 41, -565, -1, 46, 47,
		-1, -1, -573, -1, 95, -1, -1, 94, 79, -1, -579, -1, -1, -633, -679, -725,
		-771, -817, -863, -909, -955, -1001, 115, -1047, -1077, -1105, -1133, -1159, -1, 128, -1, -1,
		-1181, -1191, -1, -1, 51, -1, 14, -1, -1197, 85, -1, -1, 43, 44, 49, -1,
		-1203, 61, -1, 96, -1, 130, 92, -1213, -1, -1239, -1, 76, -1, -1, -1, 22,
		20, -1265, 17, -1, 55, -1275, -1, -1, 70, 71, -1, -1, 64, -1, -1283, -1,
		-1, -1, 124, -1, -1, -1329, -1, -1, -1, -1339, -1, -1, 66, 65, 62, 97,
		-1, 87, -1347, 90, -1, -1, 58, 24, -1, -1, -1, -1, -1, -1, -1, 15,
		-1373, 67, -1, 88, -1, 91, 57, 72, -1, -1, 63, 89, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 11, -1, 30, -1, 27, 9, -1, -2,
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1,
		5, -1, 0, 131, -1, -2, 45, -1, 52, -1, 24, 78, 29, 78, 34, 78,
		35, 78, 36, 78, 37, 78, 38, 78, 39, 78, 40, 78, 43, 78, 44, 78,
		46, 78, 47, 78, 48, 78, 49, 78, 50, 78, 51, 78, 53, 78, 54, 78,
		56, 78, 57, 78, 58, 78, 59, 78, 60, 78, 61, 78, -1, -2, 7, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1,
		43, -1, 45, -1, 44, 53, -1, -2, 43, -1, 47, -1, 54, -1, 24, 100,
		29, 100, 34, 100, 35, 100, 36, 100, 37, 100, 38, 100, 39, 100, 40, 100,
		44, 100, 46, 100, 48, 100, 49, 100, 50, 100, 51, 100, 53, 100, 56, 100,
		57, 100, 58, 100, 59, 100, 60, 100, 61, 100, -1, -2, 36, -1, 37, -1,
		38, -1, 39, -1, 40, -1, 56, -1, 57, -1, 58, -1, 59, -1, 24, 113,
		29, 113, 34, 113, 35, 113, 44, 113, 46, 113, 48, 113, 49, 113, 50, 113,
		51, 113, 53, 113, 60, 113, 61, 113, -1, -2, 24, -1, 29, 116, 34, 116,
		35, 116, 44, 116, 46, 116, 48, 116, 49, 116, 50, 116, 51, 116, 53, 116,
		60, 116, 61, 116, -1, -2, 51, -1, 53, -1, 29, 119, 34, 119, 35, 119,
		44, 119, 46, 119, 48, 119, 49, 119, 50, 119, 60, 119, 61, 119, -1, -2,
		49, -1, 29, 121, 34, 121, 35, 121, 44, 121, 46, 121, 48, 121, 50, 121,
		60, 121, 61, 121, -1, -2, 50, -1, 61, -1, 29, 123, 34, 123, 35, 123,
		44, 123, 46, 123, 48, 123, 60, 123, -1, -2, 48, -1, 34, 39, 35, 39,
		-1, -2, 45, -1, 47, -1, 17, 11, 34, 11, 35, 11, -1, -2, 7, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1,
		43, -1, 45, -1, 46, 53, -1, -2, 45, -1, 47, -1, 17, 40, 34, 40,
		35, 40, -1, -2, 45, -1, 24, 78, 29, 78, 34, 78, 35, 78, 36, 78,
		37, 78, 38, 78, 39, 78, 40, 78, 43, 78, 44, 78, 46, 78, 47, 78,
		48, 78, 49, 78, 50, 78, 51, 78, 53, 78, 54, 78, 56, 78, 57, 78,
		58, 78, 59, 78, 60, 78, 61, 78, -1, -2, 48, -1, 34, 45, 35, 45,
		-1, -2, 48, -1, 34, 74, 35, 74, -1, -2, 48, -1, 34, 73, 35, 73,
		-1, -2, 48, -1, 34, 48, 35, 48, -1, -2, 45, -1, 60, -1, 24, 78,
		36, 78, 37, 78, 38, 78, 39, 78, 40, 78, 43, 78, 44, 78, 47, 78,
		48, 78, 49, 78, 50, 78, 51, 78, 53, 78, 54, 78, 56, 78, 57, 78,
		58, 78, 59, 78, 61, 78, -1, -2, 48, -1, 44, 54, 46, 54, -1, -2,
		7, -1, 46, 18, -1, -2, 17, -1, 34, 13, 35, 13, -1, -2, 45, -1,
		47, -1, 17, 11, 52, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 53,
		-1, -2, 17, -1, 34, 42, 35, 42, -1, -2, 1, -1, 5, 60, -1, -2,
		45, -1, 24, 86, 29, 86, 34, 86, 35, 86, 36, 86, 37, 86, 38, 86,
		39, 86, 40, 86, 43, 86, 44, 86, 46, 86, 47, 86, 48, 86, 49, 86,
		50, 86, 51, 86, 53, 86, 54, 86, 56, 86, 57, 86, 58, 86, 59, 86,
		60, 86, 61, 86, -1, -2, 36, 107, 37, 107, 38, -1, 39, -1, 40, -1,
		56, 107, 57, 107, 58, 107, 59, 107, 24, 107, 29, 107, 34, 107, 35, 107,
		44, 107, 46, 107, 48, 107, 49, 107, 50, 107, 51, 107, 53, 107, 60, 107,
		61, 107, -1, -2, 36, 108, 37, 108, 38, -1, 39, -1, 40, -1, 56, 108,
		57, 108, 58, 108, 59, 108, 24, 108, 29, 108, 34, 108, 35, 108, 44, 108,
		46, 108, 48, 108, 49, 108, 50, 108, 51, 108, 53, 108, 60, 108, 61, 108,
		-1, -2, 36, 104, 37, 104, 38, 104, 39, 104, 40, 104, 56, 104, 57, 104,
		58, 104, 59, 104, 24, 104, 29, 104, 34, 104, 35, 104, 44, 104, 46, 104,
		48, 104, 49, 104, 50, 104, 51, 104, 53, 104, 60, 104, 61, 104, -1, -2,
		36, 105, 37, 105, 38, 105, 39, 105, 40, 105, 56, 105, 57, 105, 58, 105,
		59, 105, 24, 105, 29, 105, 34, 105, 35, 105, 44, 105, 46, 105, 48, 105,
		49, 105, 50, 105, 51, 105, 53, 105, 60, 105, 61, 105, -1, -2, 36, 106,
		37, 106, 38, 106, 39, 106, 40, 106, 56, 106, 57, 106, 58, 106, 59, 106,
		24, 106, 29, 106, 34, 106, 35, 106, 44, 106, 46, 106, 48, 106, 49, 106,
		50, 106, 51, 106, 53, 106, 60, 106, 61, 106, -1, -2, 36, -1, 37, -1,
		38, -1, 39, -1, 40, -1, 56, 111, 57, 111, 58, 111, 59, 111, 24, 111,
		29, 111, 34, 111, 35, 111, 44, 111, 46, 111, 48, 111, 49, 111, 50, 111,
		51, 111, 53, 111, 60, 111, 61, 111, -1, -2, 36, -1, 37, -1, 38, -1,
		39, -1, 40, -1, 56, 112, 57, 112, 58, 112, 59, 112, 24, 112, 29, 112,
		34, 112, 35, 112, 44, 112, 46, 112, 48, 112, 49, 112, 50, 112, 51, 112,
		53, 112, 60, 112, 61, 112, -1, -2, 36, -1, 37, -1, 38, -1, 39, -1,
		40, -1, 56, 109, 57, 109, 58, 109, 59, 109, 24, 109, 29, 109, 34, 109,
		35, 109, 44, 109, 46, 109, 48, 109, 49, 109, 50, 109, 51, 109, 53, 109,
		60, 109, 61, 109, -1, -2, 36, -1, 37, -1, 38, -1, 39, -1, 40, -1,
		56, 110, 57, 110, 58, 110, 59, 110, 24, 110, 29, 110, 34, 110, 35, 110,
		44, 110, 46, 110, 48, 110, 49, 110, 50, 110, 51, 110, 53, 110, 60, 110,
		61, 110, -1, -2, 47, -1, 24, 114, 29, 114, 34, 114, 35, 114, 44, 114,
		46, 114, 48, 114, 49, 114, 50, 114, 51, 114, 53, 114, 60, 114, 61, 114,
		-1, -2, 24, -1, 29, 117, 34, 117, 35, 117, 44, 117, 46, 117, 48, 117,
		49, 117, 50, 117, 51, 117, 53, 117, 60, 117, 61, 117, -1, -2, 24, -1,
		29, 118, 34, 118, 35, 118, 44, 118, 46, 118, 48, 118, 49, 118, 50, 118,
		51, 118, 53, 118, 60, 118, 61, 118, -1, -2, 51, -1, 53, -1, 29, 120,
		34, 120, 35, 120, 44, 120, 46, 120, 48, 120, 49, 120, 50, 120, 60, 120,
		61, 120, -1, -2, 49, -1, 29, 122, 34, 122, 35, 122, 44, 122, 46, 122,
		48, 122, 50, 122, 60, 122, 61, 122, -1, -2, 46, 21, 48, 21, 7, 50,
		47, 50, -1, -2, 48, -1, 46, 19, -1, -2, 17, -1, 52, 13, -1, -2,
		29, -1, 48, -1, 34, 69, 35, 69, -1, -2, 7, -1, 8, -1, 9, -1,
		16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1,
		46, 53, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 31, -1,
		32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 53, -1, -2, 47, -1,
		34, 25, 35, 25, 52, 25, -1, -2, 48, -1, 34, 52, 35, 52, -1, -2,
		42, -1, 45, -1, 24, 78, 36, 78, 37, 78, 38, 78, 39, 78, 40, 78,
		43, 78, 46, 78, 47, 78, 48, 78, 49, 78, 50, 78, 51, 78, 53, 78,
		54, 78, 56, 78, 57, 78, 58, 78, 59, 78, 61, 78, -1, -2, 46, 23,
		48, 23, 7, 50, 47, 50, -1, -2, 48, -1, 34, 75, 35, 75, -1, -2,
		7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1,
		41, -1, 43, -1, 45, -1, 46, 53, -1, -2, 29, -1, 34, 69, 35, 69,
		-1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 18, 31, 44, 57, 73, 78, 145, 197, 250, 255, 256, 258, 262, 264,
		269, 321, 329, 334, 339, 339, 345, 347, 347, 350, 350, 402, 403, 408, 410, 411,
		463, 515, 520, 531, 540, 550, 612, 622, 632, 642, 694, 695, 749, 753, 816, 826,
		835, 856, 858, 859, 861, 863, 865, 866, 866, 876, 886, 896, 906, 910, 911, 911,
		912, 913, 915, 917, 918, 920, 922, 923, 925, 927, 933, 942, 955, 968, 973, 974,
		982, 983, 984, 997, 999, 1012, 1013, 1015, 1028, 1033, 1035, 1040, 1092, 1144, 1145, 1197,
		1249, 1299, 1340, 1379, 1417, 1454, 1491, 1512, 1532, 1538, 1539, 1540, 1541, 1543, 1545, 1546,
		1547, 1548, 1549, 1555, 1556, 1558
	};

	private static final short lapg_sym_from[] = {
		252, 253, 0, 1, 4, 7, 14, 19, 27, 62, 130, 189, 203, 212, 228, 238,
		242, 248, 1, 7, 14, 19, 27, 62, 189, 203, 212, 228, 238, 242, 248, 1,
		7, 14, 19, 27, 62, 189, 203, 212, 228, 238, 242, 248, 1, 7, 14, 19,
		27, 62, 189, 203, 212, 228, 238, 242, 248, 0, 1, 4, 7, 14, 19, 27,
		62, 178, 189, 203, 212, 228, 238, 242, 248, 62, 203, 212, 238, 248, 13, 21,
		25, 31, 32, 34, 35, 36, 37, 39, 42, 43, 44, 45, 46, 65, 68, 69,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106,
		107, 108, 109, 110, 114, 115, 122, 125, 129, 131, 133, 134, 139, 162, 165, 171,
		175, 183, 185, 186, 188, 190, 199, 205, 214, 215, 216, 218, 219, 224, 226, 229,
		244, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 68, 69, 91, 94,
		95, 96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 122,
		125, 129, 131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218,
		219, 224, 226, 229, 244, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46,
		68, 69, 91, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106,
		107, 108, 109, 110, 122, 125, 129, 131, 134, 139, 171, 175, 183, 185, 186, 188,
		199, 205, 215, 216, 218, 219, 224, 226, 229, 244, 13, 25, 110, 219, 229, 3,
		202, 219, 25, 110, 219, 229, 110, 219, 13, 25, 110, 219, 229, 13, 25, 32,
		35, 37, 39, 42, 43, 44, 45, 46, 68, 69, 91, 94, 95, 96, 97, 98,
		99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 122, 125, 129, 131, 134,
		139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224, 226, 229,
		244, 13, 25, 110, 117, 124, 168, 219, 229, 13, 25, 110, 219, 229, 13, 25,
		110, 219, 229, 13, 25, 110, 159, 219, 229, 73, 75, 54, 152, 153, 13, 25,
		32, 35, 37, 39, 42, 43, 44, 45, 46, 68, 69, 91, 94, 95, 96, 97,
		98, 99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 122, 125, 129, 131,
		134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224, 226,
		229, 244, 23, 13, 25, 110, 219, 229, 176, 240, 3, 13, 25, 32, 35, 37,
		39, 42, 43, 44, 45, 46, 68, 69, 91, 94, 95, 96, 97, 98, 99, 100,
		101, 102, 104, 105, 106, 107, 108, 109, 110, 122, 125, 129, 131, 134, 139, 171,
		175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224, 226, 229, 244, 13,
		25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 68, 69, 91, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 122, 125, 129,
		131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224,
		226, 229, 244, 13, 25, 110, 219, 229, 47, 48, 66, 77, 158, 159, 167, 211,
		232, 234, 235, 47, 48, 77, 158, 159, 167, 211, 234, 235, 53, 141, 142, 143,
		144, 145, 146, 147, 148, 149, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45,
		46, 53, 68, 69, 91, 94, 95, 96, 97, 98, 99, 100, 101, 102, 104, 105,
		106, 107, 108, 109, 110, 122, 125, 129, 131, 134, 139, 141, 142, 143, 144, 145,
		146, 147, 148, 149, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219,
		224, 226, 229, 244, 53, 141, 142, 143, 144, 145, 146, 147, 148, 149, 53, 141,
		142, 143, 144, 145, 146, 147, 148, 149, 53, 141, 142, 143, 144, 145, 146, 147,
		148, 149, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 68, 69, 91,
		94, 95, 96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110,
		122, 125, 129, 131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216,
		218, 219, 224, 226, 229, 244, 206, 13, 25, 32, 35, 37, 39, 42, 43, 44,
		45, 46, 49, 68, 69, 91, 94, 95, 96, 97, 98, 99, 100, 101, 102, 104,
		105, 106, 107, 108, 109, 110, 122, 125, 128, 129, 131, 134, 139, 171, 175, 183,
		185, 186, 188, 199, 205, 215, 216, 218, 219, 224, 226, 229, 244, 82, 85, 137,
		233, 13, 25, 28, 32, 35, 37, 39, 42, 43, 44, 45, 46, 64, 68, 69,
		70, 71, 81, 91, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 104, 105,
		106, 107, 108, 109, 110, 118, 122, 125, 129, 131, 134, 138, 139, 140, 171, 175,
		183, 185, 186, 188, 199, 205, 206, 208, 215, 216, 218, 219, 224, 226, 229, 244,
		86, 120, 163, 170, 184, 207, 209, 236, 237, 249, 49, 64, 70, 118, 140, 151,
		162, 193, 214, 60, 72, 74, 76, 77, 78, 82, 84, 86, 137, 161, 176, 184,
		197, 198, 211, 217, 232, 234, 236, 249, 56, 155, 57, 55, 154, 28, 195, 55,
		154, 49, 53, 141, 142, 143, 144, 145, 146, 147, 148, 149, 53, 141, 142, 143,
		144, 145, 146, 147, 148, 149, 53, 141, 142, 143, 144, 145, 146, 147, 148, 149,
		53, 141, 142, 143, 144, 145, 146, 147, 148, 149, 81, 156, 180, 236, 57, 0,
		0, 0, 4, 0, 4, 3, 0, 4, 64, 118, 114, 117, 168, 7, 27, 1,
		7, 19, 189, 228, 242, 47, 48, 77, 158, 159, 167, 211, 234, 235, 1, 7,
		14, 19, 27, 62, 189, 203, 212, 228, 238, 242, 248, 1, 7, 14, 19, 27,
		62, 189, 203, 212, 228, 238, 242, 248, 13, 25, 110, 219, 229, 72, 21, 31,
		65, 93, 103, 114, 165, 190, 124, 70, 1, 7, 14, 19, 27, 62, 189, 203,
		212, 228, 238, 242, 248, 62, 238, 1, 7, 14, 19, 27, 62, 189, 203, 212,
		228, 238, 242, 248, 178, 178, 203, 1, 7, 14, 19, 27, 62, 189, 203, 212,
		228, 238, 242, 248, 13, 25, 110, 219, 229, 176, 240, 62, 203, 212, 238, 248,
		13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 68, 69, 91, 94, 95,
		96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 122, 125,
		129, 131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219,
		224, 226, 229, 244, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 68,
		69, 91, 94, 95, 96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 108,
		109, 110, 122, 125, 129, 131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205,
		215, 216, 218, 219, 224, 226, 229, 244, 45, 13, 25, 32, 35, 37, 39, 42,
		43, 44, 45, 46, 68, 69, 91, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		104, 105, 106, 107, 108, 109, 110, 122, 125, 129, 131, 134, 139, 171, 175, 183,
		185, 186, 188, 199, 205, 215, 216, 218, 219, 224, 226, 229, 244, 13, 25, 32,
		35, 37, 39, 42, 43, 44, 45, 46, 68, 69, 91, 94, 95, 96, 97, 98,
		99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 122, 125, 129, 131, 134,
		139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224, 226, 229,
		244, 13, 25, 32, 35, 37, 39, 42, 45, 46, 68, 69, 91, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 122, 125, 129,
		131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224,
		226, 229, 244, 13, 25, 32, 35, 37, 39, 42, 45, 46, 68, 69, 91, 104,
		105, 106, 107, 108, 109, 110, 122, 125, 129, 131, 134, 139, 171, 175, 183, 185,
		186, 188, 199, 205, 215, 216, 218, 219, 224, 226, 229, 244, 13, 25, 32, 35,
		37, 39, 42, 45, 46, 68, 69, 91, 106, 107, 108, 109, 110, 122, 125, 129,
		131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224,
		226, 229, 244, 13, 25, 32, 35, 37, 39, 42, 45, 46, 68, 69, 91, 107,
		108, 109, 110, 122, 125, 129, 131, 134, 139, 171, 175, 183, 185, 186, 188, 199,
		205, 215, 216, 218, 219, 224, 226, 229, 244, 13, 25, 32, 35, 37, 39, 42,
		45, 46, 68, 69, 91, 108, 109, 110, 122, 125, 129, 131, 134, 139, 171, 175,
		183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224, 226, 229, 244, 13, 25,
		32, 35, 37, 39, 42, 45, 46, 68, 69, 91, 108, 109, 110, 122, 125, 129,
		131, 134, 139, 171, 175, 183, 185, 186, 188, 199, 205, 215, 216, 218, 219, 224,
		226, 229, 244, 13, 25, 35, 37, 39, 42, 46, 91, 109, 110, 129, 139, 171,
		188, 199, 215, 218, 219, 224, 229, 244, 13, 25, 35, 37, 39, 42, 46, 91,
		110, 129, 139, 171, 188, 199, 215, 218, 219, 224, 229, 244, 45, 68, 122, 183,
		185, 226, 1, 0, 3, 64, 118, 117, 168, 114, 70, 124, 72, 45, 68, 122,
		183, 185, 226, 130, 176, 240
	};

	private static final short lapg_sym_to[] = {
		254, 255, 2, 9, 2, 9, 9, 9, 9, 9, 177, 9, 9, 9, 9, 9,
		9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 13, 3, 25, 13, 13, 25,
		110, 202, 13, 219, 229, 13, 110, 13, 229, 111, 111, 111, 111, 111, 28, 63,
		28, 63, 71, 73, 28, 75, 28, 28, 28, 71, 71, 81, 28, 63, 71, 71,
		28, 138, 63, 71, 71, 71, 71, 71, 71, 71, 71, 71, 63, 71, 71, 71,
		71, 71, 28, 28, 160, 164, 71, 71, 28, 71, 180, 71, 28, 191, 63, 28,
		71, 206, 71, 71, 28, 213, 28, 71, 231, 28, 71, 28, 28, 28, 71, 28,
		28, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 150, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 20,
		218, 218, 66, 158, 158, 158, 159, 235, 32, 32, 32, 32, 32, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 34, 34, 34, 165, 171, 165, 34, 34, 35, 35, 35, 35, 35, 36, 36,
		36, 36, 36, 37, 37, 37, 188, 37, 37, 128, 129, 103, 103, 103, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 65, 39, 39, 39, 39, 39, 199, 199, 21, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 42, 42, 42, 42, 42, 87, 87, 119, 87, 87, 87, 87, 87,
		239, 87, 87, 88, 88, 88, 88, 88, 88, 88, 88, 88, 94, 94, 94, 94,
		94, 94, 94, 94, 94, 94, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 95, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 95, 95, 95, 95, 95,
		95, 95, 95, 95, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 96, 96, 96, 96, 96, 96, 96, 96, 96, 96, 97, 97,
		97, 97, 97, 97, 97, 97, 97, 97, 98, 98, 98, 98, 98, 98, 98, 98,
		98, 98, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 224, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 91, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 175, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 132, 135, 182,
		240, 46, 46, 68, 46, 46, 46, 46, 46, 46, 46, 46, 46, 114, 46, 46,
		122, 68, 68, 46, 139, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 114, 46, 46, 46, 46, 46, 183, 46, 185, 46, 46,
		46, 46, 46, 46, 46, 46, 68, 226, 46, 46, 46, 46, 46, 46, 46, 46,
		136, 169, 192, 196, 208, 225, 227, 243, 245, 251, 92, 115, 115, 115, 115, 115,
		115, 115, 115, 109, 125, 109, 109, 109, 109, 133, 134, 109, 109, 190, 109, 109,
		109, 216, 109, 109, 109, 109, 109, 109, 106, 106, 107, 104, 104, 69, 215, 105,
		105, 93, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 100, 100, 100, 100,
		100, 100, 100, 100, 100, 100, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101,
		102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 131, 186, 205, 244, 108, 252,
		4, 5, 24, 6, 6, 22, 7, 7, 116, 116, 161, 166, 166, 26, 67, 14,
		27, 62, 212, 238, 248, 89, 90, 130, 187, 189, 194, 228, 241, 242, 15, 15,
		61, 15, 61, 61, 15, 220, 61, 15, 61, 15, 61, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 47, 47, 47, 47, 47, 126, 64, 70,
		118, 140, 151, 162, 193, 214, 172, 123, 17, 17, 17, 17, 17, 17, 17, 17,
		17, 17, 17, 17, 17, 112, 246, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 18, 203, 204, 221, 19, 19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 48, 48, 48, 48, 48, 200, 200, 113, 222, 230, 113, 250,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 82, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52, 52,
		52, 52, 52, 52, 79, 80, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 141, 142, 143,
		144, 145, 146, 147, 148, 149, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 152,
		153, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 154, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 155,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 58, 58,
		72, 58, 58, 58, 58, 83, 58, 83, 121, 58, 156, 58, 58, 83, 174, 58,
		179, 181, 58, 58, 198, 83, 83, 210, 58, 58, 223, 58, 233, 58, 58, 58,
		83, 58, 58, 59, 59, 59, 59, 59, 59, 59, 59, 157, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 60, 60, 74, 76, 77, 78, 86, 137,
		60, 176, 184, 197, 211, 217, 232, 234, 60, 236, 60, 249, 84, 84, 84, 84,
		84, 84, 253, 8, 23, 117, 168, 167, 195, 163, 124, 173, 127, 85, 120, 170,
		207, 209, 237, 178, 201, 247
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 1, 2, 3, 2, 1, 1, 0, 1, 0, 1, 0, 1, 9,
		1, 6, 0, 1, 3, 1, 2, 3, 4, 2, 3, 2, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 3, 1, 0, 1, 0, 1, 4, 0, 1, 3,
		2, 2, 1, 3, 2, 0, 1, 3, 3, 7, 5, 1, 0, 1, 7, 11,
		1, 2, 2, 4, 3, 0, 1, 5, 9, 2, 2, 2, 3, 1, 1, 3,
		1, 1, 1, 1, 1, 4, 3, 6, 8, 10, 6, 8, 4, 1, 3, 3,
		3, 5, 1, 1, 1, 2, 2, 1, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 1, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1, 5, 1, 3, 1,
		3, 1, 3, 1
	};

	private static final short lapg_rlex[] = {
		106, 106, 63, 64, 64, 65, 65, 65, 65, 107, 107, 108, 108, 109, 109, 66,
		67, 68, 110, 110, 69, 70, 70, 70, 70, 71, 72, 73, 73, 74, 74, 75,
		75, 75, 75, 75, 75, 75, 76, 77, 111, 111, 112, 112, 77, 113, 113, 77,
		77, 78, 79, 79, 80, 114, 114, 81, 82, 83, 83, 83, 115, 115, 84, 84,
		85, 85, 85, 86, 87, 116, 116, 88, 88, 88, 88, 89, 90, 90, 91, 91,
		91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 92, 92,
		93, 93, 94, 94, 95, 95, 95, 96, 96, 96, 96, 96, 96, 96, 96, 96,
		96, 97, 97, 97, 98, 98, 98, 99, 99, 100, 100, 101, 101, 102, 102, 103,
		103, 104, 104, 105
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"any",
		"escdollar",
		"escid",
		"escint",
		"'${'",
		"'$/'",
		"identifier",
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
		"Lnull",
		"Lquery",
		"Lswitch",
		"Lseparator",
		"Ltemplate",
		"Ltrue",
		"Lself",
		"Lassert",
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
		"complex_data",
		"map_entries",
		"bcon",
		"unary_expression",
		"binary_op",
		"instanceof_expression",
		"equality_expression",
		"conditional_and_expression",
		"conditional_or_expression",
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
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 63;
		public static final int definitions = 64;
		public static final int definition = 65;
		public static final int query_def = 66;
		public static final int cached_flag = 67;
		public static final int template_start = 68;
		public static final int parameters = 69;
		public static final int parameter_list = 70;
		public static final int context_type = 71;
		public static final int template_end = 72;
		public static final int instructions = 73;
		public static final int LSQUAREMINUSRSQUARERCURLY = 74;
		public static final int instruction = 75;
		public static final int simple_instruction = 76;
		public static final int sentence = 77;
		public static final int comma_expr = 78;
		public static final int qualified_id = 79;
		public static final int template_for_expr = 80;
		public static final int template_arguments = 81;
		public static final int control_instruction = 82;
		public static final int else_clause = 83;
		public static final int switch_instruction = 84;
		public static final int case_list = 85;
		public static final int one_case = 86;
		public static final int control_start = 87;
		public static final int control_sentence = 88;
		public static final int separator_expr = 89;
		public static final int control_end = 90;
		public static final int primary_expression = 91;
		public static final int complex_data = 92;
		public static final int map_entries = 93;
		public static final int bcon = 94;
		public static final int unary_expression = 95;
		public static final int binary_op = 96;
		public static final int instanceof_expression = 97;
		public static final int equality_expression = 98;
		public static final int conditional_and_expression = 99;
		public static final int conditional_or_expression = 100;
		public static final int conditional_expression = 101;
		public static final int assignment_expression = 102;
		public static final int expression = 103;
		public static final int expression_list = 104;
		public static final int body = 105;
		public static final int definitionsopt = 106;
		public static final int cached_flagopt = 107;
		public static final int parametersopt = 108;
		public static final int context_typeopt = 109;
		public static final int parameter_listopt = 110;
		public static final int template_argumentsopt = 111;
		public static final int template_for_expropt = 112;
		public static final int comma_expropt = 113;
		public static final int expression_listopt = 114;
		public static final int anyopt = 115;
		public static final int separator_expropt = 116;
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

	private Object parse(TemplatesLexer lexer, int state) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = state;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 254+state) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state, lapg_n.lexem);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift(lexer);
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != 254+state) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
	}

	private void shift(TemplatesLexer lexer) throws IOException {
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
			case 3:  // definitions ::= definition
				 lapg_gg.sym = new ArrayList(); if (((IBundleEntity)lapg_m[lapg_head-0].sym) != null) ((List<IBundleEntity>)lapg_gg.sym).add(((IBundleEntity)lapg_m[lapg_head-0].sym)); 
				break;
			case 4:  // definitions ::= definitions definition
				 if (((IBundleEntity)lapg_m[lapg_head-0].sym) != null) ((List<IBundleEntity>)lapg_gg.sym).add(((IBundleEntity)lapg_m[lapg_head-0].sym)); 
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
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset, lapg_gg.endoffset)); 
				break;
			case 22:  // parameter_list ::= qualified_id identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].offset, lapg_gg.endoffset)); 
				break;
			case 23:  // parameter_list ::= parameter_list ',' identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset, lapg_gg.endoffset)); 
				break;
			case 24:  // parameter_list ::= parameter_list ',' qualified_id identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].offset, lapg_gg.endoffset)); 
				break;
			case 25:  // context_type ::= Lfor qualified_id
				 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 27:  // instructions ::= instructions instruction
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head-0].offset+1); 
				break;
			case 34:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 35:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset), source, lapg_gg.offset, lapg_gg.endoffset); 
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
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), templatePackage, true, source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 47:  // sentence ::= Leval conditional_expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 48:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 49:  // comma_expr ::= ',' conditional_expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 51:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 52:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 55:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 56:  // control_instruction ::= control_start instructions else_clause
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); applyElse(((CompoundNode)lapg_m[lapg_head-2].sym),((ElseIfNode)lapg_m[lapg_head-0].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 57:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				 lapg_gg.sym = new ElseIfNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ArrayList<Node>)lapg_m[lapg_head-1].sym), ((ElseIfNode)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-6].offset, lapg_m[lapg_head-1].endoffset); 
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
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 65:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 66:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 67:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 68:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 71:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), null, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-7].sym), ((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // separator_expr ::= Lseparator expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 78:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
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
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
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
			case 94:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // map_entries ::= identifier ':' conditional_expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 97:  // map_entries ::= map_entries ',' identifier ':' conditional_expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 98:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 99:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 101:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // binary_op ::= binary_op '*' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // binary_op ::= binary_op '/' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // binary_op ::= binary_op '%' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 107:  // binary_op ::= binary_op '+' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // binary_op ::= binary_op '-' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // binary_op ::= binary_op '<' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // binary_op ::= binary_op '>' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // binary_op ::= binary_op '<=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // binary_op ::= binary_op '>=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // conditional_expression ::= conditional_or_expression '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 130:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 131:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, null, templatePackage, source, lapg_gg.offset, lapg_gg.endoffset);
							((TemplateNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-0].sym));
						
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}

	public List<IBundleEntity> parseInput(TemplatesLexer lexer) throws IOException, ParseException {
		return (List<IBundleEntity>) parse(lexer, 0);
	}

	public TemplateNode parseBody(TemplatesLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1);
	}
}
