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
		-3, -1, 8, -11, -19, 4, 7, -1, 2, 34, 33, 31, 32, -1, -27, 25,
		30, 28, 29, -1, 14, -1, 10, -1, 3, -1, 6, -1, -41, 77, 79, -1,
		-1, 96, -1, -1, -1, -1, 81, -1, 95, 80, -1, -1, -1, -97, -1, -1,
		-1, -123, 90, 78, 100, -175, -221, -261, -297, -325, -351, -373, 124, 126, -393, 24,
		-1, 47, -401, -1, -1, 5, -411, -1, -437, -449, -503, -1, -511, -1, -519, -1,
		-527, 99, 98, -535, -1, 128, -581, -1, -1, 27, 26, 35, 65, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, 74, 53, 56, -589, -1, 12, -1, -595, 23, -1, 125, -603, 38, -629, -1,
		43, 44, -1, -1, -637, -1, 92, -1, -1, 91, 76, -1, -643, -1, -1, 101,
		102, 103, -697, -743, -789, -829, -869, -909, 114, -949, -979, -1007, -1035, -1061, -1, 127,
		-1, -1, -1083, -1093, -1, -1, 48, 15, -1, 82, -1, -1, 40, 41, 46, -1,
		-1099, 58, -1, 93, -1, 129, 89, -1109, -1, -1135, -1, 73, -1, -1, -1, 20,
		18, -1, 52, -1161, -1, -1, 67, 68, -1, -1, 61, -1, -1169, -1, -1, -1,
		123, -1, -1, -1215, -1, -1, -1, -1225, -1, -1, 63, 62, 59, 94, -1, 84,
		-1233, 87, -1, -1, 55, 22, 13, -1, -1, -1, -1, -1, -1, -1259, 64, -1,
		85, -1, 88, 54, 69, -1, -1, 60, 86, -1, -1, -2, -2,
	};

	private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 11, -1, 30, -1, 27, 9, -1, -2,
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1,
		5, -1, 0, 130, -1, -2, 45, -1, 52, -1, 24, 75, 29, 75, 34, 75,
		35, 75, 36, 75, 37, 75, 38, 75, 39, 75, 40, 75, 43, 75, 44, 75,
		46, 75, 47, 75, 48, 75, 49, 75, 50, 75, 51, 75, 53, 75, 54, 75,
		56, 75, 57, 75, 58, 75, 59, 75, 60, 75, 61, 75, -1, -2, 7, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1,
		43, -1, 45, -1, 44, 50, -1, -2, 43, -1, 47, -1, 54, -1, 24, 97,
		29, 97, 34, 97, 35, 97, 36, 97, 37, 97, 38, 97, 39, 97, 40, 97,
		44, 97, 46, 97, 48, 97, 49, 97, 50, 97, 51, 97, 53, 97, 56, 97,
		57, 97, 58, 97, 59, 97, 60, 97, 61, 97, -1, -2, 38, -1, 39, -1,
		40, -1, 24, 104, 29, 104, 34, 104, 35, 104, 36, 104, 37, 104, 44, 104,
		46, 104, 48, 104, 49, 104, 50, 104, 51, 104, 53, 104, 56, 104, 57, 104,
		58, 104, 59, 104, 60, 104, 61, 104, -1, -2, 36, -1, 37, -1, 24, 107,
		29, 107, 34, 107, 35, 107, 44, 107, 46, 107, 48, 107, 49, 107, 50, 107,
		51, 107, 53, 107, 56, 107, 57, 107, 58, 107, 59, 107, 60, 107, 61, 107,
		-1, -2, 56, -1, 57, -1, 58, -1, 59, -1, 24, 112, 29, 112, 34, 112,
		35, 112, 44, 112, 46, 112, 48, 112, 49, 112, 50, 112, 51, 112, 53, 112,
		60, 112, 61, 112, -1, -2, 24, -1, 29, 115, 34, 115, 35, 115, 44, 115,
		46, 115, 48, 115, 49, 115, 50, 115, 51, 115, 53, 115, 60, 115, 61, 115,
		-1, -2, 51, -1, 53, -1, 29, 118, 34, 118, 35, 118, 44, 118, 46, 118,
		48, 118, 49, 118, 50, 118, 60, 118, 61, 118, -1, -2, 49, -1, 29, 120,
		34, 120, 35, 120, 44, 120, 46, 120, 48, 120, 50, 120, 60, 120, 61, 120,
		-1, -2, 50, -1, 61, -1, 29, 122, 34, 122, 35, 122, 44, 122, 46, 122,
		48, 122, 60, 122, -1, -2, 48, -1, 34, 36, 35, 36, -1, -2, 45, -1,
		47, -1, 34, 11, 35, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 50,
		-1, -2, 45, -1, 47, -1, 17, 37, 34, 37, 35, 37, -1, -2, 45, -1,
		24, 75, 29, 75, 34, 75, 35, 75, 36, 75, 37, 75, 38, 75, 39, 75,
		40, 75, 43, 75, 44, 75, 46, 75, 47, 75, 48, 75, 49, 75, 50, 75,
		51, 75, 53, 75, 54, 75, 56, 75, 57, 75, 58, 75, 59, 75, 60, 75,
		61, 75, -1, -2, 48, -1, 34, 42, 35, 42, -1, -2, 48, -1, 34, 71,
		35, 71, -1, -2, 48, -1, 34, 70, 35, 70, -1, -2, 48, -1, 34, 45,
		35, 45, -1, -2, 45, -1, 60, -1, 24, 75, 36, 75, 37, 75, 38, 75,
		39, 75, 40, 75, 43, 75, 44, 75, 47, 75, 48, 75, 49, 75, 50, 75,
		51, 75, 53, 75, 54, 75, 56, 75, 57, 75, 58, 75, 59, 75, 61, 75,
		-1, -2, 48, -1, 44, 51, 46, 51, -1, -2, 7, -1, 46, 16, -1, -2,
		45, -1, 47, -1, 52, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 50,
		-1, -2, 17, -1, 34, 39, 35, 39, -1, -2, 1, -1, 5, 57, -1, -2,
		45, -1, 24, 83, 29, 83, 34, 83, 35, 83, 36, 83, 37, 83, 38, 83,
		39, 83, 40, 83, 43, 83, 44, 83, 46, 83, 47, 83, 48, 83, 49, 83,
		50, 83, 51, 83, 53, 83, 54, 83, 56, 83, 57, 83, 58, 83, 59, 83,
		60, 83, 61, 83, -1, -2, 38, -1, 39, -1, 40, -1, 24, 105, 29, 105,
		34, 105, 35, 105, 36, 105, 37, 105, 44, 105, 46, 105, 48, 105, 49, 105,
		50, 105, 51, 105, 53, 105, 56, 105, 57, 105, 58, 105, 59, 105, 60, 105,
		61, 105, -1, -2, 38, -1, 39, -1, 40, -1, 24, 106, 29, 106, 34, 106,
		35, 106, 36, 106, 37, 106, 44, 106, 46, 106, 48, 106, 49, 106, 50, 106,
		51, 106, 53, 106, 56, 106, 57, 106, 58, 106, 59, 106, 60, 106, 61, 106,
		-1, -2, 36, -1, 37, -1, 24, 110, 29, 110, 34, 110, 35, 110, 44, 110,
		46, 110, 48, 110, 49, 110, 50, 110, 51, 110, 53, 110, 56, 110, 57, 110,
		58, 110, 59, 110, 60, 110, 61, 110, -1, -2, 36, -1, 37, -1, 24, 111,
		29, 111, 34, 111, 35, 111, 44, 111, 46, 111, 48, 111, 49, 111, 50, 111,
		51, 111, 53, 111, 56, 111, 57, 111, 58, 111, 59, 111, 60, 111, 61, 111,
		-1, -2, 36, -1, 37, -1, 24, 108, 29, 108, 34, 108, 35, 108, 44, 108,
		46, 108, 48, 108, 49, 108, 50, 108, 51, 108, 53, 108, 56, 108, 57, 108,
		58, 108, 59, 108, 60, 108, 61, 108, -1, -2, 36, -1, 37, -1, 24, 109,
		29, 109, 34, 109, 35, 109, 44, 109, 46, 109, 48, 109, 49, 109, 50, 109,
		51, 109, 53, 109, 56, 109, 57, 109, 58, 109, 59, 109, 60, 109, 61, 109,
		-1, -2, 47, -1, 24, 113, 29, 113, 34, 113, 35, 113, 44, 113, 46, 113,
		48, 113, 49, 113, 50, 113, 51, 113, 53, 113, 60, 113, 61, 113, -1, -2,
		24, -1, 29, 116, 34, 116, 35, 116, 44, 116, 46, 116, 48, 116, 49, 116,
		50, 116, 51, 116, 53, 116, 60, 116, 61, 116, -1, -2, 24, -1, 29, 117,
		34, 117, 35, 117, 44, 117, 46, 117, 48, 117, 49, 117, 50, 117, 51, 117,
		53, 117, 60, 117, 61, 117, -1, -2, 51, -1, 53, -1, 29, 119, 34, 119,
		35, 119, 44, 119, 46, 119, 48, 119, 49, 119, 50, 119, 60, 119, 61, 119,
		-1, -2, 49, -1, 29, 121, 34, 121, 35, 121, 44, 121, 46, 121, 48, 121,
		50, 121, 60, 121, 61, 121, -1, -2, 46, 19, 48, 19, 7, 47, 47, 47,
		-1, -2, 48, -1, 46, 17, -1, -2, 29, -1, 48, -1, 34, 66, 35, 66,
		-1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1,
		37, -1, 41, -1, 43, -1, 45, -1, 46, 50, -1, -2, 7, -1, 8, -1,
		9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1,
		45, -1, 46, 50, -1, -2, 48, -1, 34, 49, 35, 49, -1, -2, 42, -1,
		45, -1, 24, 75, 36, 75, 37, 75, 38, 75, 39, 75, 40, 75, 43, 75,
		46, 75, 47, 75, 48, 75, 49, 75, 50, 75, 51, 75, 53, 75, 54, 75,
		56, 75, 57, 75, 58, 75, 59, 75, 61, 75, -1, -2, 46, 21, 48, 21,
		7, 47, 47, 47, -1, -2, 48, -1, 34, 72, 35, 72, -1, -2, 7, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1,
		43, -1, 45, -1, 46, 50, -1, -2, 29, -1, 34, 66, 35, 66, -1, -2,
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 18, 31, 44, 57, 73, 78, 144, 196, 249, 254, 255, 257, 261, 263,
		268, 320, 326, 331, 336, 336, 342, 344, 344, 347, 347, 399, 400, 405, 407, 408,
		460, 512, 517, 528, 537, 542, 599, 602, 605, 608, 660, 661, 715, 719, 782, 792,
		800, 821, 823, 824, 826, 828, 830, 831, 831, 832, 833, 834, 835, 839, 840, 840,
		841, 842, 844, 846, 847, 849, 851, 852, 854, 860, 869, 882, 895, 900, 901, 908,
		909, 910, 923, 925, 938, 939, 941, 954, 959, 961, 966, 1018, 1070, 1071, 1123, 1175,
		1222, 1267, 1308, 1349, 1388, 1426, 1463, 1500, 1521, 1541, 1547, 1548, 1549, 1550, 1552, 1553,
		1554, 1555, 1556, 1562, 1563, 1565,
	};

	private static final short lapg_sym_from[] = {
		249, 250, 0, 1, 4, 7, 14, 19, 27, 64, 132, 189, 201, 210, 226, 236,
		239, 245, 1, 7, 14, 19, 27, 64, 189, 201, 210, 226, 236, 239, 245, 1,
		7, 14, 19, 27, 64, 189, 201, 210, 226, 236, 239, 245, 1, 7, 14, 19,
		27, 64, 189, 201, 210, 226, 236, 239, 245, 0, 1, 4, 7, 14, 19, 27,
		64, 178, 189, 201, 210, 226, 236, 239, 245, 64, 201, 210, 236, 245, 13, 21,
		25, 31, 32, 34, 35, 36, 37, 39, 42, 43, 44, 45, 46, 67, 70, 71,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108,
		109, 110, 111, 112, 116, 117, 124, 127, 131, 133, 135, 136, 141, 164, 171, 175,
		183, 185, 186, 188, 190, 193, 197, 203, 212, 214, 216, 217, 222, 224, 227, 241,
		13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127,
		131, 133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217,
		222, 224, 227, 241, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70,
		71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
		110, 111, 112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185, 186, 188, 193,
		197, 203, 214, 216, 217, 222, 224, 227, 241, 13, 25, 112, 217, 227, 3, 200,
		217, 25, 112, 217, 227, 112, 217, 13, 25, 112, 217, 227, 13, 25, 32, 35,
		37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101,
		102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141,
		171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241,
		13, 25, 112, 126, 217, 227, 13, 25, 112, 217, 227, 13, 25, 112, 217, 227,
		13, 25, 112, 161, 217, 227, 75, 77, 56, 154, 155, 13, 25, 32, 35, 37,
		39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102,
		103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 171,
		175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 23,
		13, 25, 112, 217, 227, 176, 237, 3, 13, 25, 32, 35, 37, 39, 42, 43,
		44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106,
		107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185,
		186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 13, 25, 32, 35,
		37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101,
		102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141,
		171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241,
		13, 25, 112, 217, 227, 47, 48, 68, 79, 119, 160, 161, 209, 213, 232, 233,
		47, 48, 79, 119, 160, 161, 209, 232, 233, 54, 148, 149, 150, 151, 13, 25,
		32, 35, 37, 39, 42, 43, 44, 45, 46, 54, 70, 71, 93, 96, 97, 98,
		99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 148, 149, 150, 151, 171, 175, 183, 185, 186, 188, 193, 197, 203,
		214, 216, 217, 222, 224, 227, 241, 53, 146, 147, 53, 146, 147, 53, 146, 147,
		13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127,
		131, 133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217,
		222, 224, 227, 241, 204, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46,
		49, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108,
		109, 110, 111, 112, 124, 127, 130, 131, 133, 136, 141, 171, 175, 183, 185, 186,
		188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 84, 87, 139, 231, 13,
		25, 28, 32, 35, 37, 39, 42, 43, 44, 45, 46, 66, 70, 71, 72, 73,
		83, 93, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109,
		110, 111, 112, 120, 124, 127, 131, 133, 136, 140, 141, 142, 171, 175, 183, 185,
		186, 188, 193, 197, 203, 204, 206, 214, 216, 217, 222, 224, 227, 241, 88, 122,
		165, 170, 184, 205, 207, 234, 235, 246, 49, 66, 72, 120, 142, 153, 164, 212,
		62, 74, 76, 78, 79, 80, 84, 86, 88, 139, 163, 176, 184, 195, 196, 209,
		213, 215, 232, 234, 246, 58, 157, 59, 57, 156, 28, 168, 57, 156, 49, 55,
		55, 55, 55, 83, 158, 180, 234, 59, 0, 0, 0, 4, 0, 4, 3, 0,
		4, 66, 120, 116, 7, 27, 1, 7, 19, 189, 226, 239, 47, 48, 79, 119,
		160, 161, 209, 232, 233, 1, 7, 14, 19, 27, 64, 189, 201, 210, 226, 236,
		239, 245, 1, 7, 14, 19, 27, 64, 189, 201, 210, 226, 236, 239, 245, 13,
		25, 112, 217, 227, 74, 21, 31, 67, 95, 105, 116, 190, 126, 72, 1, 7,
		14, 19, 27, 64, 189, 201, 210, 226, 236, 239, 245, 64, 236, 1, 7, 14,
		19, 27, 64, 189, 201, 210, 226, 236, 239, 245, 178, 178, 201, 1, 7, 14,
		19, 27, 64, 189, 201, 210, 226, 236, 239, 245, 13, 25, 112, 217, 227, 176,
		237, 64, 201, 210, 236, 245, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45,
		46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108,
		109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185, 186, 188,
		193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 13, 25, 32, 35, 37, 39,
		42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103,
		104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 171, 175,
		183, 185, 186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 45, 13,
		25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98,
		99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217, 222,
		224, 227, 241, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71,
		93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111,
		112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203,
		214, 216, 217, 222, 224, 227, 241, 13, 25, 32, 35, 37, 39, 42, 45, 46,
		70, 71, 93, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112,
		124, 127, 131, 133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203, 214,
		216, 217, 222, 224, 227, 241, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70,
		71, 93, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217, 222,
		224, 227, 241, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 106,
		107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185,
		186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 13, 25, 32, 35,
		37, 39, 42, 45, 46, 70, 71, 93, 106, 107, 108, 109, 110, 111, 112, 124,
		127, 131, 133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216,
		217, 222, 224, 227, 241, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71,
		93, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185,
		186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 13, 25, 32, 35,
		37, 39, 42, 45, 46, 70, 71, 93, 109, 110, 111, 112, 124, 127, 131, 133,
		136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203, 214, 216, 217, 222, 224,
		227, 241, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 110, 111,
		112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185, 186, 188, 193, 197, 203,
		214, 216, 217, 222, 224, 227, 241, 13, 25, 32, 35, 37, 39, 42, 45, 46,
		70, 71, 93, 110, 111, 112, 124, 127, 131, 133, 136, 141, 171, 175, 183, 185,
		186, 188, 193, 197, 203, 214, 216, 217, 222, 224, 227, 241, 13, 25, 35, 37,
		39, 42, 46, 93, 111, 112, 131, 141, 171, 188, 193, 197, 216, 217, 222, 227,
		241, 13, 25, 35, 37, 39, 42, 46, 93, 112, 131, 141, 171, 188, 193, 197,
		216, 217, 222, 227, 241, 45, 70, 124, 183, 185, 224, 1, 0, 3, 66, 120,
		116, 72, 126, 74, 45, 70, 124, 183, 185, 224, 132, 176, 237,
	};

	private static final short lapg_sym_to[] = {
		251, 252, 2, 9, 2, 9, 9, 9, 9, 9, 177, 9, 9, 9, 9, 9,
		9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 13, 3, 25, 13, 13, 25,
		112, 200, 13, 217, 227, 13, 112, 13, 227, 113, 113, 113, 113, 113, 28, 65,
		28, 65, 73, 75, 28, 77, 28, 28, 28, 73, 73, 83, 28, 65, 73, 73,
		28, 140, 65, 73, 73, 73, 73, 73, 73, 73, 73, 73, 65, 73, 73, 73,
		73, 73, 28, 28, 162, 166, 73, 73, 28, 73, 180, 73, 28, 191, 28, 73,
		204, 73, 73, 28, 211, 28, 28, 73, 229, 73, 28, 28, 28, 73, 28, 28,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 152, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 20, 216,
		216, 68, 160, 160, 160, 161, 233, 32, 32, 32, 32, 32, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		34, 34, 34, 171, 34, 34, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36,
		37, 37, 37, 188, 37, 37, 130, 131, 105, 105, 105, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 67,
		39, 39, 39, 39, 39, 197, 197, 21, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		42, 42, 42, 42, 42, 89, 89, 121, 89, 89, 89, 89, 89, 230, 89, 89,
		90, 90, 90, 90, 90, 90, 90, 90, 90, 99, 99, 99, 99, 99, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 100, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 100, 100, 100, 100, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 96, 96, 96, 97, 97, 97, 98, 98, 98,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 222, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		93, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 175, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 134, 137, 182, 237, 46,
		46, 70, 46, 46, 46, 46, 46, 46, 46, 46, 46, 116, 46, 46, 124, 70,
		70, 46, 141, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 116, 46, 46, 46, 46, 46, 183, 46, 185, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 70, 224, 46, 46, 46, 46, 46, 46, 46, 138, 169,
		192, 194, 206, 223, 225, 240, 242, 248, 94, 117, 117, 117, 117, 117, 117, 117,
		111, 127, 111, 111, 111, 111, 135, 136, 111, 111, 190, 111, 111, 111, 214, 111,
		111, 111, 111, 111, 111, 108, 108, 109, 106, 106, 71, 193, 107, 107, 95, 101,
		102, 103, 104, 133, 186, 203, 241, 110, 249, 4, 5, 24, 6, 6, 22, 7,
		7, 118, 118, 163, 26, 69, 14, 27, 64, 210, 236, 245, 91, 92, 132, 167,
		187, 189, 226, 238, 239, 15, 15, 63, 15, 63, 63, 15, 218, 63, 15, 63,
		15, 63, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 47,
		47, 47, 47, 47, 128, 66, 72, 120, 142, 153, 164, 212, 172, 125, 17, 17,
		17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 114, 243, 18, 18, 18,
		18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 201, 202, 219, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 48, 48, 48, 48, 48, 198,
		198, 115, 220, 228, 115, 247, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 84, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 52, 52, 52, 52, 52, 52, 52, 81, 82, 52, 52, 52, 52,
		52, 143, 144, 145, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 146, 147, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 148, 149, 150, 151, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 154, 155, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 156, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 157, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 60, 60, 74, 60, 60, 60, 60, 85, 60,
		85, 123, 60, 158, 60, 60, 85, 174, 60, 179, 181, 60, 60, 196, 85, 85,
		208, 60, 60, 60, 221, 231, 60, 60, 60, 85, 60, 60, 61, 61, 61, 61,
		61, 61, 61, 61, 159, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61,
		61, 62, 62, 76, 78, 79, 80, 88, 139, 62, 176, 184, 195, 209, 213, 215,
		232, 62, 234, 62, 246, 86, 86, 86, 86, 86, 86, 250, 8, 23, 119, 168,
		165, 126, 173, 129, 87, 122, 170, 205, 207, 235, 178, 199, 244,
	};

	private static final short lapg_rlen[] = {
		0, 1, 1, 2, 1, 3, 2, 1, 1, 0, 1, 0, 1, 8, 1, 5,
		0, 1, 3, 1, 2, 3, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 3, 1, 0, 1, 0, 1, 4, 0, 1, 3, 2, 2, 1,
		3, 2, 0, 1, 3, 3, 7, 5, 1, 0, 1, 7, 11, 1, 2, 2,
		4, 3, 0, 1, 5, 9, 2, 2, 2, 3, 1, 1, 3, 1, 1, 1,
		1, 1, 4, 3, 6, 8, 10, 6, 8, 4, 1, 3, 3, 3, 5, 1,
		1, 1, 2, 2, 1, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3,
		1, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1, 5, 1, 3, 1, 3,
		1, 3, 1,
	};

	private static final short lapg_rlex[] = {
		107, 107, 63, 64, 64, 65, 65, 65, 65, 108, 108, 109, 109, 66, 67, 68,
		110, 110, 69, 70, 70, 70, 70, 71, 72, 72, 73, 73, 74, 74, 74, 74,
		74, 74, 74, 75, 76, 111, 111, 112, 112, 76, 113, 113, 76, 76, 77, 78,
		78, 79, 114, 114, 80, 81, 82, 82, 82, 115, 115, 83, 83, 84, 84, 84,
		85, 86, 116, 116, 87, 87, 87, 87, 88, 89, 89, 90, 90, 90, 90, 90,
		90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 91, 91, 92, 92, 93,
		93, 94, 94, 94, 95, 95, 95, 95, 96, 96, 96, 97, 97, 97, 97, 97,
		98, 98, 98, 99, 99, 99, 100, 100, 101, 101, 102, 102, 103, 103, 104, 104,
		105, 105, 106,
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
		public static final int template_end = 71;
		public static final int instructions = 72;
		public static final int LSQUAREMINUSRSQUARERCURLY = 73;
		public static final int instruction = 74;
		public static final int simple_instruction = 75;
		public static final int sentence = 76;
		public static final int comma_expr = 77;
		public static final int qualified_id = 78;
		public static final int template_for_expr = 79;
		public static final int template_arguments = 80;
		public static final int control_instruction = 81;
		public static final int else_clause = 82;
		public static final int switch_instruction = 83;
		public static final int case_list = 84;
		public static final int one_case = 85;
		public static final int control_start = 86;
		public static final int control_sentence = 87;
		public static final int separator_expr = 88;
		public static final int control_end = 89;
		public static final int primary_expression = 90;
		public static final int complex_data = 91;
		public static final int map_entries = 92;
		public static final int bcon = 93;
		public static final int unary_expression = 94;
		public static final int mult_expression = 95;
		public static final int additive_expression = 96;
		public static final int relational_expression = 97;
		public static final int instanceof_expression = 98;
		public static final int equality_expression = 99;
		public static final int conditional_and_expression = 100;
		public static final int conditional_or_expression = 101;
		public static final int conditional_expression = 102;
		public static final int assignment_expression = 103;
		public static final int expression = 104;
		public static final int expression_list = 105;
		public static final int body = 106;
		public static final int templatesopt = 107;
		public static final int cached_flagopt = 108;
		public static final int parametersopt = 109;
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

	private Object parse(AstLexer lexer, int state) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = state;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 251+state) {
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

		if (lapg_m[lapg_head].state != 251+state) {
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
			case 13:  // query_def ::= '${' cached_flagopt Lquery qualified_id parametersopt '=' expression '}'
				 lapg_gg.sym = new QueryNode(((String)lapg_m[lapg_head-4].sym), ((List<ParameterNode>)lapg_m[lapg_head-3].sym), templatePackage, ((ExpressionNode)lapg_m[lapg_head-1].sym), ((Boolean)lapg_m[lapg_head-6].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head-4].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-7].line); 
				break;
			case 14:  // cached_flag ::= Lcached
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 15:  // template_start ::= '${' Ltemplate qualified_id parametersopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-2].sym), ((List<ParameterNode>)lapg_m[lapg_head-1].sym), templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head-2].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-4].line); 
				break;
			case 18:  // parameters ::= '(' parameter_listopt ')'
				 lapg_gg.sym = ((List<ParameterNode>)lapg_m[lapg_head-1].sym); 
				break;
			case 19:  // parameter_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset, lapg_gg.endoffset)); 
				break;
			case 20:  // parameter_list ::= qualified_id identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].offset, lapg_gg.endoffset)); 
				break;
			case 21:  // parameter_list ::= parameter_list ',' identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-0].offset, lapg_gg.endoffset)); 
				break;
			case 22:  // parameter_list ::= parameter_list ',' qualified_id identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].offset, lapg_gg.endoffset)); 
				break;
			case 24:  // instructions ::= instructions instruction
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 25:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 26:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head-0].offset+1); 
				break;
			case 31:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 32:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 34:  // instruction ::= any
				 lapg_gg.sym = new TextNode(source, rawText(lapg_gg.offset, lapg_gg.endoffset), lapg_gg.endoffset); 
				break;
			case 35:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 41:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), templatePackage, true, source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 44:  // sentence ::= Leval conditional_expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 45:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 46:  // comma_expr ::= ',' conditional_expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 48:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 49:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 52:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 53:  // control_instruction ::= control_start instructions else_clause
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); applyElse(((CompoundNode)lapg_m[lapg_head-2].sym),((ElseIfNode)lapg_m[lapg_head-0].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 54:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				 lapg_gg.sym = new ElseIfNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ArrayList<Node>)lapg_m[lapg_head-1].sym), ((ElseIfNode)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-6].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 55:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new ElseIfNode(null, ((ArrayList<Node>)lapg_m[lapg_head-1].sym), null, source, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 56:  // else_clause ::= control_end
				 lapg_gg.sym = null; 
				break;
			case 59:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head-2].offset,lapg_m[lapg_head-2].endoffset, lapg_m[lapg_head-2].line); 
				break;
			case 60:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-8].sym), ((ArrayList)lapg_m[lapg_head-5].sym), ((ArrayList<Node>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head-6].offset,lapg_m[lapg_head-6].endoffset, lapg_m[lapg_head-6].line); 
				break;
			case 61:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 63:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 64:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 65:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 68:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), null, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-7].sym), ((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // separator_expr ::= Lseparator expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 75:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 86:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ':' expression ')'
				 lapg_gg.sym = createMapCollect(((ExpressionNode)lapg_m[lapg_head-9].sym), ((String)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 87:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-5].sym), templatePackage, false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 91:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // map_entries ::= identifier ':' conditional_expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 94:  // map_entries ::= map_entries ',' identifier ':' conditional_expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 95:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 96:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 98:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 113:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // conditional_expression ::= conditional_or_expression '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 129:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 130:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, templatePackage, source, lapg_gg.offset, lapg_gg.endoffset);
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
