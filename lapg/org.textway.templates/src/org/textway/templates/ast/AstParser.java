package org.textway.templates.ast;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.textway.templates.api.IBundleEntity;
import org.textway.templates.ast.AstLexer.ErrorReporter;
import org.textway.templates.ast.AstLexer.LapgSymbol;
import org.textway.templates.ast.AstLexer.Lexems;
import org.textway.templates.ast.AstTree.TextSource;

public class AstParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public AstParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	
	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	
	private ArrayList<IBundleEntity> entities;
	private String templatePackage;
	
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
	
	public boolean parse(TextSource source, String templatePackage) {
		this.templatePackage = templatePackage;
		this.entities = new ArrayList<IBundleEntity>();
		this.source = source; 
		try {
			AstLexer lexer = new AstLexer(source.getStream(), reporter);
			parseInput(lexer);
			return true;
		} catch( ParseException ex ) {
			return false;
		} catch( IOException ex ) {
			return false;
		}
	}
	
	public boolean parseBody(TextSource source, String templatePackage) {
		this.templatePackage = templatePackage;
		this.entities = new ArrayList<IBundleEntity>();
		this.source = source; 
		try {
			AstLexer lexer = new AstLexer(source.getStream(), reporter);
			parseBody(lexer);
			return true;
		} catch( ParseException ex ) {
			return false;
		} catch( IOException ex ) {
			return false;
		}
	}
	
	public IBundleEntity[] getResult() {
		return entities.toArray(new IBundleEntity[entities.size()]);
	}
	private static final int lapg_action[] = {
		-3, -1, 8, -11, -19, 4, 7, -1, 2, 37, 36, 34, 35, -1, -27, 28,
		33, 31, 32, -1, 16, -1, 10, -1, 3, -1, 6, -1, -41, 80, 82, -1,
		-1, 99, -1, -1, -1, -1, 84, -1, 98, 83, -1, -1, -1, -97, -1, -1,
		-1, -123, 93, 81, 103, -175, -221, -261, -297, -325, -351, -373, 127, 129, -393, 27,
		-1, 50, -401, -1, -1, 5, -413, -1, -439, -451, -505, -1, -513, -1, -521, -1,
		-529, 102, 101, -537, -1, 131, -583, -1, -1, 30, 29, 38, 68, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, 77, 56, 59, -591, -1, 12, -597, -605, 26, -1, 128, -615, 41, -641, -1,
		46, 47, -1, -1, -649, -1, 95, -1, -1, 94, 79, -1, -655, -1, -1, 104,
		105, 106, -709, -755, -801, -841, -881, -921, 117, -961, -991, -1019, -1047, -1073, -1, 130,
		-1, -1, -1095, -1105, -1, -1, 51, -1, 14, -1, -1111, 85, -1, -1, 43, 44,
		49, -1, -1117, 61, -1, 96, -1, 132, 92, -1127, -1, -1153, -1, 76, -1, -1,
		-1, 22, 20, -1179, 17, -1, 55, -1189, -1, -1, 70, 71, -1, -1, 64, -1,
		-1197, -1, -1, -1, 126, -1, -1, -1243, -1, -1, -1, -1253, -1, -1, 66, 65,
		62, 97, -1, 87, -1261, 90, -1, -1, 58, 24, -1, -1, -1, -1, -1, -1,
		-1, 15, -1287, 67, -1, 88, -1, 91, 57, 72, -1, -1, 63, 89, -1, -1,
		-2, -2,
	};

	private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 11, -1, 30, -1, 27, 9, -1, -2,
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1,
		5, -1, 0, 133, -1, -2, 45, -1, 52, -1, 24, 78, 29, 78, 34, 78,
		35, 78, 36, 78, 37, 78, 38, 78, 39, 78, 40, 78, 43, 78, 44, 78,
		46, 78, 47, 78, 48, 78, 49, 78, 50, 78, 51, 78, 53, 78, 54, 78,
		56, 78, 57, 78, 58, 78, 59, 78, 60, 78, 61, 78, -1, -2, 7, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1,
		43, -1, 45, -1, 44, 53, -1, -2, 43, -1, 47, -1, 54, -1, 24, 100,
		29, 100, 34, 100, 35, 100, 36, 100, 37, 100, 38, 100, 39, 100, 40, 100,
		44, 100, 46, 100, 48, 100, 49, 100, 50, 100, 51, 100, 53, 100, 56, 100,
		57, 100, 58, 100, 59, 100, 60, 100, 61, 100, -1, -2, 38, -1, 39, -1,
		40, -1, 24, 107, 29, 107, 34, 107, 35, 107, 36, 107, 37, 107, 44, 107,
		46, 107, 48, 107, 49, 107, 50, 107, 51, 107, 53, 107, 56, 107, 57, 107,
		58, 107, 59, 107, 60, 107, 61, 107, -1, -2, 36, -1, 37, -1, 24, 110,
		29, 110, 34, 110, 35, 110, 44, 110, 46, 110, 48, 110, 49, 110, 50, 110,
		51, 110, 53, 110, 56, 110, 57, 110, 58, 110, 59, 110, 60, 110, 61, 110,
		-1, -2, 56, -1, 57, -1, 58, -1, 59, -1, 24, 115, 29, 115, 34, 115,
		35, 115, 44, 115, 46, 115, 48, 115, 49, 115, 50, 115, 51, 115, 53, 115,
		60, 115, 61, 115, -1, -2, 24, -1, 29, 118, 34, 118, 35, 118, 44, 118,
		46, 118, 48, 118, 49, 118, 50, 118, 51, 118, 53, 118, 60, 118, 61, 118,
		-1, -2, 51, -1, 53, -1, 29, 121, 34, 121, 35, 121, 44, 121, 46, 121,
		48, 121, 49, 121, 50, 121, 60, 121, 61, 121, -1, -2, 49, -1, 29, 123,
		34, 123, 35, 123, 44, 123, 46, 123, 48, 123, 50, 123, 60, 123, 61, 123,
		-1, -2, 50, -1, 61, -1, 29, 125, 34, 125, 35, 125, 44, 125, 46, 125,
		48, 125, 60, 125, -1, -2, 48, -1, 34, 39, 35, 39, -1, -2, 45, -1,
		47, -1, 17, 11, 34, 11, 35, 11, -1, -2, 7, -1, 8, -1, 9, -1,
		16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1,
		46, 53, -1, -2, 45, -1, 47, -1, 17, 40, 34, 40, 35, 40, -1, -2,
		45, -1, 24, 78, 29, 78, 34, 78, 35, 78, 36, 78, 37, 78, 38, 78,
		39, 78, 40, 78, 43, 78, 44, 78, 46, 78, 47, 78, 48, 78, 49, 78,
		50, 78, 51, 78, 53, 78, 54, 78, 56, 78, 57, 78, 58, 78, 59, 78,
		60, 78, 61, 78, -1, -2, 48, -1, 34, 45, 35, 45, -1, -2, 48, -1,
		34, 74, 35, 74, -1, -2, 48, -1, 34, 73, 35, 73, -1, -2, 48, -1,
		34, 48, 35, 48, -1, -2, 45, -1, 60, -1, 24, 78, 36, 78, 37, 78,
		38, 78, 39, 78, 40, 78, 43, 78, 44, 78, 47, 78, 48, 78, 49, 78,
		50, 78, 51, 78, 53, 78, 54, 78, 56, 78, 57, 78, 58, 78, 59, 78,
		61, 78, -1, -2, 48, -1, 44, 54, 46, 54, -1, -2, 7, -1, 46, 18,
		-1, -2, 17, -1, 34, 13, 35, 13, -1, -2, 45, -1, 47, -1, 17, 11,
		52, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 31, -1,
		32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 53, -1, -2, 17, -1,
		34, 42, 35, 42, -1, -2, 1, -1, 5, 60, -1, -2, 45, -1, 24, 86,
		29, 86, 34, 86, 35, 86, 36, 86, 37, 86, 38, 86, 39, 86, 40, 86,
		43, 86, 44, 86, 46, 86, 47, 86, 48, 86, 49, 86, 50, 86, 51, 86,
		53, 86, 54, 86, 56, 86, 57, 86, 58, 86, 59, 86, 60, 86, 61, 86,
		-1, -2, 38, -1, 39, -1, 40, -1, 24, 108, 29, 108, 34, 108, 35, 108,
		36, 108, 37, 108, 44, 108, 46, 108, 48, 108, 49, 108, 50, 108, 51, 108,
		53, 108, 56, 108, 57, 108, 58, 108, 59, 108, 60, 108, 61, 108, -1, -2,
		38, -1, 39, -1, 40, -1, 24, 109, 29, 109, 34, 109, 35, 109, 36, 109,
		37, 109, 44, 109, 46, 109, 48, 109, 49, 109, 50, 109, 51, 109, 53, 109,
		56, 109, 57, 109, 58, 109, 59, 109, 60, 109, 61, 109, -1, -2, 36, -1,
		37, -1, 24, 113, 29, 113, 34, 113, 35, 113, 44, 113, 46, 113, 48, 113,
		49, 113, 50, 113, 51, 113, 53, 113, 56, 113, 57, 113, 58, 113, 59, 113,
		60, 113, 61, 113, -1, -2, 36, -1, 37, -1, 24, 114, 29, 114, 34, 114,
		35, 114, 44, 114, 46, 114, 48, 114, 49, 114, 50, 114, 51, 114, 53, 114,
		56, 114, 57, 114, 58, 114, 59, 114, 60, 114, 61, 114, -1, -2, 36, -1,
		37, -1, 24, 111, 29, 111, 34, 111, 35, 111, 44, 111, 46, 111, 48, 111,
		49, 111, 50, 111, 51, 111, 53, 111, 56, 111, 57, 111, 58, 111, 59, 111,
		60, 111, 61, 111, -1, -2, 36, -1, 37, -1, 24, 112, 29, 112, 34, 112,
		35, 112, 44, 112, 46, 112, 48, 112, 49, 112, 50, 112, 51, 112, 53, 112,
		56, 112, 57, 112, 58, 112, 59, 112, 60, 112, 61, 112, -1, -2, 47, -1,
		24, 116, 29, 116, 34, 116, 35, 116, 44, 116, 46, 116, 48, 116, 49, 116,
		50, 116, 51, 116, 53, 116, 60, 116, 61, 116, -1, -2, 24, -1, 29, 119,
		34, 119, 35, 119, 44, 119, 46, 119, 48, 119, 49, 119, 50, 119, 51, 119,
		53, 119, 60, 119, 61, 119, -1, -2, 24, -1, 29, 120, 34, 120, 35, 120,
		44, 120, 46, 120, 48, 120, 49, 120, 50, 120, 51, 120, 53, 120, 60, 120,
		61, 120, -1, -2, 51, -1, 53, -1, 29, 122, 34, 122, 35, 122, 44, 122,
		46, 122, 48, 122, 49, 122, 50, 122, 60, 122, 61, 122, -1, -2, 49, -1,
		29, 124, 34, 124, 35, 124, 44, 124, 46, 124, 48, 124, 50, 124, 60, 124,
		61, 124, -1, -2, 46, 21, 48, 21, 7, 50, 47, 50, -1, -2, 48, -1,
		46, 19, -1, -2, 17, -1, 52, 13, -1, -2, 29, -1, 48, -1, 34, 69,
		35, 69, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 31, -1,
		32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 53, -1, -2, 7, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1,
		43, -1, 45, -1, 46, 53, -1, -2, 47, -1, 34, 25, 35, 25, 52, 25,
		-1, -2, 48, -1, 34, 52, 35, 52, -1, -2, 42, -1, 45, -1, 24, 78,
		36, 78, 37, 78, 38, 78, 39, 78, 40, 78, 43, 78, 46, 78, 47, 78,
		48, 78, 49, 78, 50, 78, 51, 78, 53, 78, 54, 78, 56, 78, 57, 78,
		58, 78, 59, 78, 61, 78, -1, -2, 46, 23, 48, 23, 7, 50, 47, 50,
		-1, -2, 48, -1, 34, 75, 35, 75, -1, -2, 7, -1, 8, -1, 9, -1,
		16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1,
		46, 53, -1, -2, 29, -1, 34, 69, 35, 69, -1, -2,
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 18, 31, 44, 57, 73, 78, 145, 197, 250, 255, 256, 258, 262, 264,
		269, 321, 329, 334, 339, 339, 345, 347, 347, 350, 350, 402, 403, 408, 410, 411,
		463, 515, 520, 531, 540, 545, 602, 605, 608, 611, 663, 664, 718, 722, 785, 795,
		804, 825, 827, 828, 830, 832, 834, 835, 835, 836, 837, 838, 839, 843, 844, 844,
		845, 846, 848, 850, 851, 853, 855, 856, 858, 860, 866, 875, 888, 901, 906, 907,
		915, 916, 917, 930, 932, 945, 946, 948, 961, 966, 968, 973, 1025, 1077, 1078, 1130,
		1182, 1229, 1274, 1315, 1356, 1395, 1433, 1470, 1507, 1528, 1548, 1554, 1555, 1556, 1557, 1559,
		1561, 1562, 1563, 1564, 1565, 1571, 1572, 1574,
	};

	private static final short lapg_sym_from[] = {
		254, 255, 0, 1, 4, 7, 14, 19, 27, 64, 132, 191, 205, 214, 230, 240,
		244, 250, 1, 7, 14, 19, 27, 64, 191, 205, 214, 230, 240, 244, 250, 1,
		7, 14, 19, 27, 64, 191, 205, 214, 230, 240, 244, 250, 1, 7, 14, 19,
		27, 64, 191, 205, 214, 230, 240, 244, 250, 0, 1, 4, 7, 14, 19, 27,
		64, 180, 191, 205, 214, 230, 240, 244, 250, 64, 205, 214, 240, 250, 13, 21,
		25, 31, 32, 34, 35, 36, 37, 39, 42, 43, 44, 45, 46, 67, 70, 71,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108,
		109, 110, 111, 112, 116, 117, 124, 127, 131, 133, 135, 136, 141, 164, 167, 173,
		177, 185, 187, 188, 190, 192, 201, 207, 216, 217, 218, 220, 221, 226, 228, 231,
		246, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96,
		97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124,
		127, 131, 133, 136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220,
		221, 226, 228, 231, 246, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46,
		70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108,
		109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177, 185, 187, 188, 190,
		201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25, 112, 221, 231, 3,
		204, 221, 25, 112, 221, 231, 112, 221, 13, 25, 112, 221, 231, 13, 25, 32,
		35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136,
		141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231,
		246, 13, 25, 112, 119, 126, 170, 221, 231, 13, 25, 112, 221, 231, 13, 25,
		112, 221, 231, 13, 25, 112, 161, 221, 231, 75, 77, 56, 154, 155, 13, 25,
		32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99,
		100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133,
		136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228,
		231, 246, 23, 13, 25, 112, 221, 231, 178, 242, 3, 13, 25, 32, 35, 37,
		39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102,
		103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173,
		177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13,
		25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98,
		99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226,
		228, 231, 246, 13, 25, 112, 221, 231, 47, 48, 68, 79, 160, 161, 169, 213,
		234, 236, 237, 47, 48, 79, 160, 161, 169, 213, 236, 237, 54, 148, 149, 150,
		151, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 54, 70, 71, 93,
		96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112,
		124, 127, 131, 133, 136, 141, 148, 149, 150, 151, 173, 177, 185, 187, 188, 190,
		201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 53, 146, 147, 53, 146, 147,
		53, 146, 147, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71,
		93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111,
		112, 124, 127, 131, 133, 136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217,
		218, 220, 221, 226, 228, 231, 246, 208, 13, 25, 32, 35, 37, 39, 42, 43,
		44, 45, 46, 49, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104,
		106, 107, 108, 109, 110, 111, 112, 124, 127, 130, 131, 133, 136, 141, 173, 177,
		185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 84, 87,
		139, 235, 13, 25, 28, 32, 35, 37, 39, 42, 43, 44, 45, 46, 66, 70,
		71, 72, 73, 83, 93, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106,
		107, 108, 109, 110, 111, 112, 120, 124, 127, 131, 133, 136, 140, 141, 142, 173,
		177, 185, 187, 188, 190, 201, 207, 208, 210, 217, 218, 220, 221, 226, 228, 231,
		246, 88, 122, 165, 172, 186, 209, 211, 238, 239, 251, 49, 66, 72, 120, 142,
		153, 164, 195, 216, 62, 74, 76, 78, 79, 80, 84, 86, 88, 139, 163, 178,
		186, 199, 200, 213, 219, 234, 236, 238, 251, 58, 157, 59, 57, 156, 28, 197,
		57, 156, 49, 55, 55, 55, 55, 83, 158, 182, 238, 59, 0, 0, 0, 4,
		0, 4, 3, 0, 4, 66, 120, 116, 119, 170, 7, 27, 1, 7, 19, 191,
		230, 244, 47, 48, 79, 160, 161, 169, 213, 236, 237, 1, 7, 14, 19, 27,
		64, 191, 205, 214, 230, 240, 244, 250, 1, 7, 14, 19, 27, 64, 191, 205,
		214, 230, 240, 244, 250, 13, 25, 112, 221, 231, 74, 21, 31, 67, 95, 105,
		116, 167, 192, 126, 72, 1, 7, 14, 19, 27, 64, 191, 205, 214, 230, 240,
		244, 250, 64, 240, 1, 7, 14, 19, 27, 64, 191, 205, 214, 230, 240, 244,
		250, 180, 180, 205, 1, 7, 14, 19, 27, 64, 191, 205, 214, 230, 240, 244,
		250, 13, 25, 112, 221, 231, 178, 242, 64, 205, 214, 240, 250, 13, 25, 32,
		35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136,
		141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231,
		246, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96,
		97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124,
		127, 131, 133, 136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220,
		221, 226, 228, 231, 246, 45, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45,
		46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108,
		109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177, 185, 187, 188, 190,
		201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25, 32, 35, 37, 39,
		42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103,
		104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177,
		185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25,
		32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 99, 100, 101, 102, 103, 104,
		106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177, 185,
		187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25, 32,
		35, 37, 39, 42, 45, 46, 70, 71, 93, 101, 102, 103, 104, 106, 107, 108,
		109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177, 185, 187, 188, 190,
		201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25, 32, 35, 37, 39,
		42, 45, 46, 70, 71, 93, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226,
		228, 231, 246, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 106,
		107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177, 185, 187,
		188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25, 32, 35,
		37, 39, 42, 45, 46, 70, 71, 93, 108, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226,
		228, 231, 246, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 109,
		110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177, 185, 187, 188, 190, 201,
		207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25, 32, 35, 37, 39, 42,
		45, 46, 70, 71, 93, 110, 111, 112, 124, 127, 131, 133, 136, 141, 173, 177,
		185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226, 228, 231, 246, 13, 25,
		32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 173, 177, 185, 187, 188, 190, 201, 207, 217, 218, 220, 221, 226,
		228, 231, 246, 13, 25, 35, 37, 39, 42, 46, 93, 111, 112, 131, 141, 173,
		190, 201, 217, 220, 221, 226, 231, 246, 13, 25, 35, 37, 39, 42, 46, 93,
		112, 131, 141, 173, 190, 201, 217, 220, 221, 226, 231, 246, 45, 70, 124, 185,
		187, 228, 1, 0, 3, 66, 120, 119, 170, 116, 72, 126, 74, 45, 70, 124,
		185, 187, 228, 132, 178, 242,
	};

	private static final short lapg_sym_to[] = {
		256, 257, 2, 9, 2, 9, 9, 9, 9, 9, 179, 9, 9, 9, 9, 9,
		9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 13, 3, 25, 13, 13, 25,
		112, 204, 13, 221, 231, 13, 112, 13, 231, 113, 113, 113, 113, 113, 28, 65,
		28, 65, 73, 75, 28, 77, 28, 28, 28, 73, 73, 83, 28, 65, 73, 73,
		28, 140, 65, 73, 73, 73, 73, 73, 73, 73, 73, 73, 65, 73, 73, 73,
		73, 73, 28, 28, 162, 166, 73, 73, 28, 73, 182, 73, 28, 193, 65, 28,
		73, 208, 73, 73, 28, 215, 28, 73, 233, 28, 73, 28, 28, 28, 73, 28,
		28, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 152, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 20,
		220, 220, 68, 160, 160, 160, 161, 237, 32, 32, 32, 32, 32, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 34, 34, 34, 167, 173, 167, 34, 34, 35, 35, 35, 35, 35, 36, 36,
		36, 36, 36, 37, 37, 37, 190, 37, 37, 130, 131, 105, 105, 105, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 67, 39, 39, 39, 39, 39, 201, 201, 21, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 42, 42, 42, 42, 42, 89, 89, 121, 89, 89, 89, 89, 89,
		241, 89, 89, 90, 90, 90, 90, 90, 90, 90, 90, 90, 99, 99, 99, 99,
		99, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 100, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 100, 100, 100, 100, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 96, 96, 96, 97, 97, 97,
		98, 98, 98, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 226, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 93, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 177, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 134, 137,
		184, 242, 46, 46, 70, 46, 46, 46, 46, 46, 46, 46, 46, 46, 116, 46,
		46, 124, 70, 70, 46, 141, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 116, 46, 46, 46, 46, 46, 185, 46, 187, 46,
		46, 46, 46, 46, 46, 46, 46, 70, 228, 46, 46, 46, 46, 46, 46, 46,
		46, 138, 171, 194, 198, 210, 227, 229, 245, 247, 253, 94, 117, 117, 117, 117,
		117, 117, 117, 117, 111, 127, 111, 111, 111, 111, 135, 136, 111, 111, 192, 111,
		111, 111, 218, 111, 111, 111, 111, 111, 111, 108, 108, 109, 106, 106, 71, 217,
		107, 107, 95, 101, 102, 103, 104, 133, 188, 207, 246, 110, 254, 4, 5, 24,
		6, 6, 22, 7, 7, 118, 118, 163, 168, 168, 26, 69, 14, 27, 64, 214,
		240, 250, 91, 92, 132, 189, 191, 196, 230, 243, 244, 15, 15, 63, 15, 63,
		63, 15, 222, 63, 15, 63, 15, 63, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 47, 47, 47, 47, 47, 128, 66, 72, 120, 142, 153,
		164, 195, 216, 174, 125, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
		17, 17, 114, 248, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		18, 205, 206, 223, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
		19, 48, 48, 48, 48, 48, 202, 202, 115, 224, 232, 115, 252, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 84, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52, 52, 52, 52, 52,
		52, 81, 82, 52, 52, 52, 52, 52, 143, 144, 145, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 146, 147, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 148, 149, 150, 151, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 154,
		155, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 156, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 157,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 60, 60,
		74, 60, 60, 60, 60, 85, 60, 85, 123, 60, 158, 60, 60, 85, 176, 60,
		181, 183, 60, 60, 200, 85, 85, 212, 60, 60, 225, 60, 235, 60, 60, 60,
		85, 60, 60, 61, 61, 61, 61, 61, 61, 61, 61, 159, 61, 61, 61, 61,
		61, 61, 61, 61, 61, 61, 61, 61, 62, 62, 76, 78, 79, 80, 88, 139,
		62, 178, 186, 199, 213, 219, 234, 236, 62, 238, 62, 251, 86, 86, 86, 86,
		86, 86, 255, 8, 23, 119, 170, 169, 197, 165, 126, 175, 129, 87, 122, 172,
		209, 211, 239, 180, 203, 249,
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 2, 1, 3, 2, 1, 1, 0, 1, 0, 1, 0, 1, 9,
		1, 6, 0, 1, 3, 1, 2, 3, 4, 2, 3, 2, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 3, 1, 0, 1, 0, 1, 4, 0, 1, 3,
		2, 2, 1, 3, 2, 0, 1, 3, 3, 7, 5, 1, 0, 1, 7, 11,
		1, 2, 2, 4, 3, 0, 1, 5, 9, 2, 2, 2, 3, 1, 1, 3,
		1, 1, 1, 1, 1, 4, 3, 6, 8, 10, 6, 8, 4, 1, 3, 3,
		3, 5, 1, 1, 1, 2, 2, 1, 3, 3, 3, 1, 3, 3, 1, 3,
		3, 3, 3, 1, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1, 5, 1,
		3, 1, 3, 1, 3, 1,
	};

	private static final short lapg_rlex[] = {
		108, 108, 63, 64, 64, 65, 65, 65, 65, 109, 109, 110, 110, 111, 111, 66,
		67, 68, 112, 112, 69, 70, 70, 70, 70, 71, 72, 73, 73, 74, 74, 75,
		75, 75, 75, 75, 75, 75, 76, 77, 113, 113, 114, 114, 77, 115, 115, 77,
		77, 78, 79, 79, 80, 116, 116, 81, 82, 83, 83, 83, 117, 117, 84, 84,
		85, 85, 85, 86, 87, 118, 118, 88, 88, 88, 88, 89, 90, 90, 91, 91,
		91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 92, 92,
		93, 93, 94, 94, 95, 95, 95, 96, 96, 96, 96, 97, 97, 97, 98, 98,
		98, 98, 98, 99, 99, 99, 100, 100, 100, 101, 101, 102, 102, 103, 103, 104,
		104, 105, 105, 106, 106, 107,
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
		"templates",
		"template_declaration_or_space",
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
		"mult_expression",
		"additive_expression",
		"relational_expression",
		"instanceof_expression",
		"equality_expression",
		"conditional_and_expression",
		"conditional_or_expression",
		"conditional_expression",
		"assignment_expression",
		"expression",
		"expression_list",
		"body",
		"templatesopt",
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
		public static final int templates = 64;
		public static final int template_declaration_or_space = 65;
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
		public static final int mult_expression = 96;
		public static final int additive_expression = 97;
		public static final int relational_expression = 98;
		public static final int instanceof_expression = 99;
		public static final int equality_expression = 100;
		public static final int conditional_and_expression = 101;
		public static final int conditional_or_expression = 102;
		public static final int conditional_expression = 103;
		public static final int assignment_expression = 104;
		public static final int expression = 105;
		public static final int expression_list = 106;
		public static final int body = 107;
		public static final int templatesopt = 108;
		public static final int cached_flagopt = 109;
		public static final int parametersopt = 110;
		public static final int context_typeopt = 111;
		public static final int parameter_listopt = 112;
		public static final int template_argumentsopt = 113;
		public static final int template_for_expropt = 114;
		public static final int comma_expropt = 115;
		public static final int expression_listopt = 116;
		public static final int anyopt = 117;
		public static final int separator_expropt = 118;
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

	private Object parse(AstLexer lexer, int state) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = state;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 256+state) {
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

		if (lapg_m[lapg_head].state != 256+state) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
	}

	private void shift(AstLexer lexer) throws IOException {
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
			case 5:  // template_declaration_or_space ::= template_start instructions template_end
				 ((TemplateNode)lapg_m[lapg_head-2].sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); entities.add(((TemplateNode)lapg_m[lapg_head-2].sym)); 
				break;
			case 6:  // template_declaration_or_space ::= template_start template_end
				 entities.add(((TemplateNode)lapg_m[lapg_head-1].sym)); 
				break;
			case 7:  // template_declaration_or_space ::= query_def
				 entities.add(((QueryNode)lapg_m[lapg_head-0].sym)); 
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
			case 104:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 113:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // conditional_expression ::= conditional_or_expression '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 132:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 133:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, null, templatePackage, source, lapg_gg.offset, lapg_gg.endoffset);
							((TemplateNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-0].sym));
							entities.add(((TemplateNode)lapg_gg.sym));
						
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}

	public Object parseInput(AstLexer lexer) throws IOException, ParseException {
		return parse(lexer, 0);
	}

	public TemplateNode parseBody(AstLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1);
	}
}
