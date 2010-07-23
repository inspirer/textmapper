package net.sf.lapg.templates.ast;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;
import net.sf.lapg.templates.ast.AstLexer.LapgSymbol;
import net.sf.lapg.templates.ast.AstLexer.Lexems;
import net.sf.lapg.templates.ast.AstTree.TextSource;

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
		-3, -1, 8, -11, -19, 4, 7, -1, 2, 32, 31, 29, 30, -1, -27, 23,
		28, 26, 27, -1, 14, -1, 10, -1, 3, -1, 6, -1, -41, 75, 77, -1,
		-1, 94, -1, -1, -1, -1, 79, -1, 93, 78, -1, -1, -1, -97, -1, -1,
		-1, -123, 88, 76, 98, -175, -221, -261, -297, -325, -351, -373, 122, 124, -393, 22,
		-1, 45, -401, -1, -1, 5, -411, -1, -437, -449, -503, -1, -511, -1, -519, -1,
		-527, 97, 96, -535, -1, 126, -581, -1, -1, 25, 24, 33, 63, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, 72, 51, 54, -589, -1, 12, -1, -595, 21, -1, 123, -603, 36, -629, -1,
		41, 42, -1, -1, -637, -1, 90, -1, -1, 89, 74, -1, -643, -1, -1, 99,
		100, 101, -697, -743, -789, -829, -869, -909, 112, -949, -979, -1007, -1035, -1061, -1, 125,
		-1, -1, 19, -1083, -1, 46, 15, -1, 80, -1, -1, 38, 39, 44, -1, -1089,
		56, -1, 91, -1, 127, 87, -1099, -1, -1125, -1, 71, -1, -1, -1, 18, -1,
		50, -1151, -1, -1, 65, 66, -1, -1, 59, -1, -1159, -1, -1, -1, 121, -1,
		-1, 20, -1, -1, -1205, -1, -1, 61, 60, 57, 92, -1, 82, -1213, 85, -1,
		-1, 53, 13, -1, -1, -1, -1, -1, -1, -1239, 62, -1, 83, -1, 86, 52,
		67, -1, -1, 58, 84, -1, -1, -2, -2,
	};

    private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 11, -1, 30, -1, 27, 9, -1, -2,
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1,
		5, -1, 0, 128, -1, -2, 45, -1, 52, -1, 24, 73, 29, 73, 34, 73,
		35, 73, 36, 73, 37, 73, 38, 73, 39, 73, 40, 73, 43, 73, 44, 73,
		46, 73, 47, 73, 48, 73, 49, 73, 50, 73, 51, 73, 53, 73, 54, 73,
		56, 73, 57, 73, 58, 73, 59, 73, 60, 73, 61, 73, -1, -2, 7, -1,
		8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1,
		43, -1, 45, -1, 44, 48, -1, -2, 43, -1, 47, -1, 54, -1, 24, 95,
		29, 95, 34, 95, 35, 95, 36, 95, 37, 95, 38, 95, 39, 95, 40, 95,
		44, 95, 46, 95, 48, 95, 49, 95, 50, 95, 51, 95, 53, 95, 56, 95,
		57, 95, 58, 95, 59, 95, 60, 95, 61, 95, -1, -2, 38, -1, 39, -1,
		40, -1, 24, 102, 29, 102, 34, 102, 35, 102, 36, 102, 37, 102, 44, 102,
		46, 102, 48, 102, 49, 102, 50, 102, 51, 102, 53, 102, 56, 102, 57, 102,
		58, 102, 59, 102, 60, 102, 61, 102, -1, -2, 36, -1, 37, -1, 24, 105,
		29, 105, 34, 105, 35, 105, 44, 105, 46, 105, 48, 105, 49, 105, 50, 105,
		51, 105, 53, 105, 56, 105, 57, 105, 58, 105, 59, 105, 60, 105, 61, 105,
		-1, -2, 56, -1, 57, -1, 58, -1, 59, -1, 24, 110, 29, 110, 34, 110,
		35, 110, 44, 110, 46, 110, 48, 110, 49, 110, 50, 110, 51, 110, 53, 110,
		60, 110, 61, 110, -1, -2, 24, -1, 29, 113, 34, 113, 35, 113, 44, 113,
		46, 113, 48, 113, 49, 113, 50, 113, 51, 113, 53, 113, 60, 113, 61, 113,
		-1, -2, 51, -1, 53, -1, 29, 116, 34, 116, 35, 116, 44, 116, 46, 116,
		48, 116, 49, 116, 50, 116, 60, 116, 61, 116, -1, -2, 49, -1, 29, 118,
		34, 118, 35, 118, 44, 118, 46, 118, 48, 118, 50, 118, 60, 118, 61, 118,
		-1, -2, 50, -1, 61, -1, 29, 120, 34, 120, 35, 120, 44, 120, 46, 120,
		48, 120, 60, 120, -1, -2, 48, -1, 34, 34, 35, 34, -1, -2, 45, -1,
		47, -1, 34, 11, 35, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 48,
		-1, -2, 45, -1, 47, -1, 17, 35, 34, 35, 35, 35, -1, -2, 45, -1,
		24, 73, 29, 73, 34, 73, 35, 73, 36, 73, 37, 73, 38, 73, 39, 73,
		40, 73, 43, 73, 44, 73, 46, 73, 47, 73, 48, 73, 49, 73, 50, 73,
		51, 73, 53, 73, 54, 73, 56, 73, 57, 73, 58, 73, 59, 73, 60, 73,
		61, 73, -1, -2, 48, -1, 34, 40, 35, 40, -1, -2, 48, -1, 34, 69,
		35, 69, -1, -2, 48, -1, 34, 68, 35, 68, -1, -2, 48, -1, 34, 43,
		35, 43, -1, -2, 45, -1, 60, -1, 24, 73, 36, 73, 37, 73, 38, 73,
		39, 73, 40, 73, 43, 73, 44, 73, 47, 73, 48, 73, 49, 73, 50, 73,
		51, 73, 53, 73, 54, 73, 56, 73, 57, 73, 58, 73, 59, 73, 61, 73,
		-1, -2, 48, -1, 44, 49, 46, 49, -1, -2, 7, -1, 46, 16, -1, -2,
		45, -1, 47, -1, 52, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 48,
		-1, -2, 17, -1, 34, 37, 35, 37, -1, -2, 1, -1, 5, 55, -1, -2,
		45, -1, 24, 81, 29, 81, 34, 81, 35, 81, 36, 81, 37, 81, 38, 81,
		39, 81, 40, 81, 43, 81, 44, 81, 46, 81, 47, 81, 48, 81, 49, 81,
		50, 81, 51, 81, 53, 81, 54, 81, 56, 81, 57, 81, 58, 81, 59, 81,
		60, 81, 61, 81, -1, -2, 38, -1, 39, -1, 40, -1, 24, 103, 29, 103,
		34, 103, 35, 103, 36, 103, 37, 103, 44, 103, 46, 103, 48, 103, 49, 103,
		50, 103, 51, 103, 53, 103, 56, 103, 57, 103, 58, 103, 59, 103, 60, 103,
		61, 103, -1, -2, 38, -1, 39, -1, 40, -1, 24, 104, 29, 104, 34, 104,
		35, 104, 36, 104, 37, 104, 44, 104, 46, 104, 48, 104, 49, 104, 50, 104,
		51, 104, 53, 104, 56, 104, 57, 104, 58, 104, 59, 104, 60, 104, 61, 104,
		-1, -2, 36, -1, 37, -1, 24, 108, 29, 108, 34, 108, 35, 108, 44, 108,
		46, 108, 48, 108, 49, 108, 50, 108, 51, 108, 53, 108, 56, 108, 57, 108,
		58, 108, 59, 108, 60, 108, 61, 108, -1, -2, 36, -1, 37, -1, 24, 109,
		29, 109, 34, 109, 35, 109, 44, 109, 46, 109, 48, 109, 49, 109, 50, 109,
		51, 109, 53, 109, 56, 109, 57, 109, 58, 109, 59, 109, 60, 109, 61, 109,
		-1, -2, 36, -1, 37, -1, 24, 106, 29, 106, 34, 106, 35, 106, 44, 106,
		46, 106, 48, 106, 49, 106, 50, 106, 51, 106, 53, 106, 56, 106, 57, 106,
		58, 106, 59, 106, 60, 106, 61, 106, -1, -2, 36, -1, 37, -1, 24, 107,
		29, 107, 34, 107, 35, 107, 44, 107, 46, 107, 48, 107, 49, 107, 50, 107,
		51, 107, 53, 107, 56, 107, 57, 107, 58, 107, 59, 107, 60, 107, 61, 107,
		-1, -2, 47, -1, 24, 111, 29, 111, 34, 111, 35, 111, 44, 111, 46, 111,
		48, 111, 49, 111, 50, 111, 51, 111, 53, 111, 60, 111, 61, 111, -1, -2,
		24, -1, 29, 114, 34, 114, 35, 114, 44, 114, 46, 114, 48, 114, 49, 114,
		50, 114, 51, 114, 53, 114, 60, 114, 61, 114, -1, -2, 24, -1, 29, 115,
		34, 115, 35, 115, 44, 115, 46, 115, 48, 115, 49, 115, 50, 115, 51, 115,
		53, 115, 60, 115, 61, 115, -1, -2, 51, -1, 53, -1, 29, 117, 34, 117,
		35, 117, 44, 117, 46, 117, 48, 117, 49, 117, 50, 117, 60, 117, 61, 117,
		-1, -2, 49, -1, 29, 119, 34, 119, 35, 119, 44, 119, 46, 119, 48, 119,
		50, 119, 60, 119, 61, 119, -1, -2, 48, -1, 46, 17, -1, -2, 29, -1,
		48, -1, 34, 64, 35, 64, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1, 46, 48,
		-1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 31, -1, 32, -1,
		37, -1, 41, -1, 43, -1, 45, -1, 46, 48, -1, -2, 48, -1, 34, 47,
		35, 47, -1, -2, 42, -1, 45, -1, 24, 73, 36, 73, 37, 73, 38, 73,
		39, 73, 40, 73, 43, 73, 46, 73, 47, 73, 48, 73, 49, 73, 50, 73,
		51, 73, 53, 73, 54, 73, 56, 73, 57, 73, 58, 73, 59, 73, 61, 73,
		-1, -2, 48, -1, 34, 70, 35, 70, -1, -2, 7, -1, 8, -1, 9, -1,
		16, -1, 26, -1, 31, -1, 32, -1, 37, -1, 41, -1, 43, -1, 45, -1,
		46, 48, -1, -2, 29, -1, 34, 64, 35, 64, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 2, 18, 31, 44, 57, 73, 78, 142, 194, 247, 252, 253, 255, 259, 261,
		266, 318, 324, 329, 334, 334, 340, 342, 342, 345, 345, 397, 398, 403, 405, 406,
		458, 510, 515, 526, 535, 540, 597, 600, 603, 606, 658, 659, 713, 717, 780, 790,
		796, 817, 819, 820, 822, 824, 826, 827, 827, 828, 829, 830, 831, 835, 836, 836,
		837, 838, 840, 842, 843, 845, 847, 848, 850, 856, 865, 878, 891, 896, 897, 902,
		903, 904, 917, 919, 932, 933, 935, 948, 953, 955, 960, 1012, 1064, 1065, 1117, 1169,
		1216, 1261, 1302, 1343, 1382, 1420, 1457, 1494, 1515, 1535, 1541, 1542, 1543, 1544, 1546, 1547,
		1548, 1549, 1550, 1556, 1557, 1559,
	};

    private static final short lapg_sym_from[] = {
		245, 246, 0, 1, 4, 7, 14, 19, 27, 64, 132, 188, 199, 208, 223, 232,
		235, 241, 1, 7, 14, 19, 27, 64, 188, 199, 208, 223, 232, 235, 241, 1,
		7, 14, 19, 27, 64, 188, 199, 208, 223, 232, 235, 241, 1, 7, 14, 19,
		27, 64, 188, 199, 208, 223, 232, 235, 241, 0, 1, 4, 7, 14, 19, 27,
		64, 177, 188, 199, 208, 223, 232, 235, 241, 64, 199, 208, 232, 241, 13, 21,
		25, 31, 32, 34, 35, 36, 37, 39, 42, 43, 44, 45, 46, 67, 70, 71,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108,
		109, 110, 111, 112, 116, 117, 124, 127, 131, 133, 135, 136, 141, 170, 174, 182,
		184, 185, 187, 189, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13, 25,
		32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99,
		100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133,
		136, 141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221,
		224, 237, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93,
		96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
		112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 195, 201,
		211, 213, 214, 219, 221, 224, 237, 13, 25, 112, 214, 224, 3, 198, 214, 25,
		112, 214, 224, 112, 214, 13, 25, 112, 214, 224, 13, 25, 32, 35, 37, 39,
		42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103,
		104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174,
		182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13, 25,
		112, 126, 214, 224, 13, 25, 112, 214, 224, 13, 25, 112, 214, 224, 13, 25,
		112, 161, 214, 224, 75, 77, 56, 154, 155, 13, 25, 32, 35, 37, 39, 42,
		43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104,
		106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182,
		184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 23, 13, 25,
		112, 214, 224, 175, 233, 3, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45,
		46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108,
		109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187,
		191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13, 25, 32, 35, 37, 39,
		42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103,
		104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174,
		182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13, 25,
		112, 214, 224, 47, 48, 68, 79, 119, 160, 161, 207, 210, 228, 229, 47, 48,
		79, 119, 160, 161, 207, 228, 229, 54, 148, 149, 150, 151, 13, 25, 32, 35,
		37, 39, 42, 43, 44, 45, 46, 54, 70, 71, 93, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136,
		141, 148, 149, 150, 151, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211, 213,
		214, 219, 221, 224, 237, 53, 146, 147, 53, 146, 147, 53, 146, 147, 13, 25,
		32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99,
		100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133,
		136, 141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221,
		224, 237, 202, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 49, 70,
		71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110,
		111, 112, 124, 127, 130, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191,
		195, 201, 211, 213, 214, 219, 221, 224, 237, 84, 87, 139, 227, 13, 25, 28,
		32, 35, 37, 39, 42, 43, 44, 45, 46, 66, 70, 71, 72, 73, 83, 93,
		95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111,
		112, 120, 124, 127, 131, 133, 136, 140, 141, 142, 170, 174, 182, 184, 185, 187,
		191, 195, 201, 202, 204, 211, 213, 214, 219, 221, 224, 237, 88, 122, 164, 169,
		183, 203, 205, 230, 231, 242, 49, 66, 72, 120, 142, 153, 62, 74, 76, 78,
		79, 80, 84, 86, 88, 139, 163, 175, 183, 193, 194, 207, 210, 212, 228, 230,
		242, 58, 157, 59, 57, 156, 28, 167, 57, 156, 49, 55, 55, 55, 55, 83,
		158, 179, 230, 59, 0, 0, 0, 4, 0, 4, 3, 0, 4, 66, 120, 116,
		7, 27, 1, 7, 19, 188, 223, 235, 47, 48, 79, 119, 160, 161, 207, 228,
		229, 1, 7, 14, 19, 27, 64, 188, 199, 208, 223, 232, 235, 241, 1, 7,
		14, 19, 27, 64, 188, 199, 208, 223, 232, 235, 241, 13, 25, 112, 214, 224,
		74, 21, 31, 67, 95, 105, 126, 72, 1, 7, 14, 19, 27, 64, 188, 199,
		208, 223, 232, 235, 241, 64, 232, 1, 7, 14, 19, 27, 64, 188, 199, 208,
		223, 232, 235, 241, 177, 177, 199, 1, 7, 14, 19, 27, 64, 188, 199, 208,
		223, 232, 235, 241, 13, 25, 112, 214, 224, 175, 233, 64, 199, 208, 232, 241,
		13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127,
		131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211, 213, 214,
		219, 221, 224, 237, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70,
		71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110,
		111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 195,
		201, 211, 213, 214, 219, 221, 224, 237, 45, 13, 25, 32, 35, 37, 39, 42,
		43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104,
		106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182,
		184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13, 25, 32,
		35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136,
		141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224,
		237, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 99, 100, 101,
		102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141,
		170, 174, 182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237,
		13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 101, 102, 103, 104,
		106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182,
		184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13, 25, 32,
		35, 37, 39, 42, 45, 46, 70, 71, 93, 106, 107, 108, 109, 110, 111, 112,
		124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211,
		213, 214, 219, 221, 224, 237, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70,
		71, 93, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170,
		174, 182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13,
		25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 108, 109, 110, 111, 112,
		124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211,
		213, 214, 219, 221, 224, 237, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70,
		71, 93, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184,
		185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224, 237, 13, 25, 32, 35,
		37, 39, 42, 45, 46, 70, 71, 93, 110, 111, 112, 124, 127, 131, 133, 136,
		141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211, 213, 214, 219, 221, 224,
		237, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 110, 111, 112,
		124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 195, 201, 211,
		213, 214, 219, 221, 224, 237, 13, 25, 35, 37, 39, 42, 46, 93, 111, 112,
		131, 141, 170, 187, 191, 195, 213, 214, 219, 224, 237, 13, 25, 35, 37, 39,
		42, 46, 93, 112, 131, 141, 170, 187, 191, 195, 213, 214, 219, 224, 237, 45,
		70, 124, 182, 184, 221, 1, 0, 3, 66, 120, 116, 72, 126, 74, 45, 70,
		124, 182, 184, 221, 132, 175, 233,
	};

    private static final short lapg_sym_to[] = {
		247, 248, 2, 9, 2, 9, 9, 9, 9, 9, 176, 9, 9, 9, 9, 9,
		9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 13, 3, 25, 13, 13, 25,
		112, 198, 13, 214, 224, 13, 112, 13, 224, 113, 113, 113, 113, 113, 28, 65,
		28, 65, 73, 75, 28, 77, 28, 28, 28, 73, 73, 83, 28, 65, 73, 73,
		28, 140, 65, 73, 73, 73, 73, 73, 73, 73, 73, 73, 65, 73, 73, 73,
		73, 73, 28, 28, 162, 165, 73, 73, 28, 73, 179, 73, 28, 28, 73, 202,
		73, 73, 28, 209, 28, 28, 73, 73, 28, 28, 28, 73, 28, 28, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 152, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 20, 213, 213, 68,
		160, 160, 160, 161, 229, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 34, 34,
		34, 170, 34, 34, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 37, 37,
		37, 187, 37, 37, 130, 131, 105, 105, 105, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 67, 39, 39,
		39, 39, 39, 195, 195, 21, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 42, 42,
		42, 42, 42, 89, 89, 121, 89, 89, 89, 89, 89, 226, 89, 89, 90, 90,
		90, 90, 90, 90, 90, 90, 90, 99, 99, 99, 99, 99, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 100, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 100, 100, 100, 100, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 96, 96, 96, 97, 97, 97, 98, 98, 98, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 219, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 93, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 174, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 134, 137, 181, 233, 46, 46, 70,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 116, 46, 46, 124, 70, 70, 46,
		141, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 116, 46, 46, 46, 46, 46, 182, 46, 184, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 70, 221, 46, 46, 46, 46, 46, 46, 46, 138, 168, 190, 192,
		204, 220, 222, 236, 238, 244, 94, 117, 117, 117, 117, 117, 111, 127, 111, 111,
		111, 111, 135, 136, 111, 111, 189, 111, 111, 111, 211, 111, 111, 111, 111, 111,
		111, 108, 108, 109, 106, 106, 71, 191, 107, 107, 95, 101, 102, 103, 104, 133,
		185, 201, 237, 110, 245, 4, 5, 24, 6, 6, 22, 7, 7, 118, 118, 163,
		26, 69, 14, 27, 64, 208, 232, 241, 91, 92, 132, 166, 186, 188, 223, 234,
		235, 15, 15, 63, 15, 63, 63, 15, 215, 63, 15, 63, 15, 63, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 47, 47, 47, 47, 47,
		128, 66, 72, 120, 142, 153, 171, 125, 17, 17, 17, 17, 17, 17, 17, 17,
		17, 17, 17, 17, 17, 114, 239, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 18, 199, 200, 216, 19, 19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 48, 48, 48, 48, 48, 196, 196, 115, 217, 225, 115, 243,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 84, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52, 52,
		52, 52, 52, 52, 81, 82, 52, 52, 52, 52, 52, 143, 144, 145, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 146, 147, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 148, 149, 150, 151,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 154, 155, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 156, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 157, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 60, 60, 74, 60, 60, 60, 60, 85, 60, 85, 123, 60, 158, 60, 60,
		85, 173, 60, 178, 180, 60, 60, 194, 85, 85, 206, 60, 60, 60, 218, 227,
		60, 60, 60, 85, 60, 60, 61, 61, 61, 61, 61, 61, 61, 61, 159, 61,
		61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 62, 62, 76, 78, 79,
		80, 88, 139, 62, 175, 183, 193, 207, 210, 212, 228, 62, 230, 62, 242, 86,
		86, 86, 86, 86, 86, 246, 8, 23, 119, 167, 164, 126, 172, 129, 87, 122,
		169, 203, 205, 231, 177, 197, 240,
	};

    private static final short lapg_rlen[] = {
		0, 1, 1, 2, 1, 3, 2, 1, 1, 0, 1, 0, 1, 8, 1, 5,
		0, 1, 3, 1, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 3, 1, 0, 1, 0, 1, 4, 0, 1, 3, 2, 2, 1, 3, 2,
		0, 1, 3, 3, 7, 5, 1, 0, 1, 7, 11, 1, 2, 2, 4, 3,
		0, 1, 5, 9, 2, 2, 2, 3, 1, 1, 3, 1, 1, 1, 1, 1,
		4, 3, 6, 8, 10, 6, 8, 4, 1, 3, 3, 3, 5, 1, 1, 1,
		2, 2, 1, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 1, 3,
		3, 1, 3, 3, 1, 3, 1, 3, 1, 5, 1, 3, 1, 3, 1, 3,
		1,
	};

    private static final short lapg_rlex[] = {
		107, 107, 63, 64, 64, 65, 65, 65, 65, 108, 108, 109, 109, 66, 67, 68,
		110, 110, 69, 70, 70, 71, 72, 72, 73, 73, 74, 74, 74, 74, 74, 74,
		74, 75, 76, 111, 111, 112, 112, 76, 113, 113, 76, 76, 77, 78, 78, 79,
		114, 114, 80, 81, 82, 82, 82, 115, 115, 83, 83, 84, 84, 84, 85, 86,
		116, 116, 87, 87, 87, 87, 88, 89, 89, 90, 90, 90, 90, 90, 90, 90,
		90, 90, 90, 90, 90, 90, 90, 90, 90, 91, 91, 92, 92, 93, 93, 94,
		94, 94, 95, 95, 95, 95, 96, 96, 96, 97, 97, 97, 97, 97, 98, 98,
		98, 99, 99, 99, 100, 100, 101, 101, 102, 102, 103, 103, 104, 104, 105, 105,
		106,
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
		"identifier_list",
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
		"identifier_listopt",
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
		public static final int identifier_list = 70;
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
		public static final int identifier_listopt = 110;
		public static final int template_argumentsopt = 111;
		public static final int template_for_expropt = 112;
		public static final int comma_expropt = 113;
		public static final int expression_listopt = 114;
		public static final int anyopt = 115;
		public static final int separator_expropt = 116;
	}

	private static int lapg_next( int state, int symbol ) {
		int p;
		if( lapg_action[state] < -2 ) {
			for( p = - lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2 )
				if( lapg_lalr[p] == symbol ) break;
			return lapg_lalr[p+1];
		}
		return lapg_action[state];
	}

	private static int lapg_state_sym( int state, int symbol ) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol+1]-1;
		int i, e;

		while( min <= max ) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if( i == state )
				return lapg_sym_to[e];
			else if( i < state )
				min = e + 1;
			else
				max = e - 1;
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

		while( lapg_m[lapg_head].state != 247+state ) {
			int lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

			if( lapg_i >= 0 ) {
				reduce(lapg_i);
			} else if( lapg_i == -1 ) {
				shift(lexer);
			}

			if( lapg_i == -2 || lapg_m[lapg_head].state == -1 ) {
				break;
			}
		}

		if( lapg_m[lapg_head].state != 247+state ) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		};
		return lapg_m[lapg_head-1].sym;
	}

	private void shift(AstLexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
		if( DEBUG_SYNTAX ) {
			System.out.println(MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current()));
		}
		if( lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0 ) {
			lapg_n = lexer.next();
		}
	}

	@SuppressWarnings("unchecked")
	private void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]].sym:null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if( DEBUG_SYNTAX ) {
			System.out.println( "reduce to " + lapg_syms[lapg_rlex[rule]] );
		}
		LapgSymbol startsym = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]]:lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule]!=0)?lapg_m[lapg_head].endoffset:lapg_n.offset;
		switch( rule ) {
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
				 lapg_gg.sym = new QueryNode(((String)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-3].sym), templatePackage, ((ExpressionNode)lapg_m[lapg_head-1].sym), ((Boolean)lapg_m[lapg_head-6].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head-4].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-7].line); 
				break;
			case 14:  // cached_flag ::= Lcached
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 15:  // template_start ::= '${' Ltemplate qualified_id parametersopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head-2].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-4].line); 
				break;
			case 18:  // parameters ::= '(' identifier_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 19:  // identifier_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 20:  // identifier_list ::= identifier_list ',' identifier
				 ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 22:  // instructions ::= instructions instruction
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 23:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 24:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head-0].offset+1); 
				break;
			case 29:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 30:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 31:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 32:  // instruction ::= any
				 lapg_gg.sym = new TextNode(source, rawText(lapg_gg.offset, lapg_gg.endoffset), lapg_gg.endoffset); 
				break;
			case 33:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 39:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), templatePackage, true, source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 42:  // sentence ::= Leval conditional_expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 43:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 44:  // comma_expr ::= ',' conditional_expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 46:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 47:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 50:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 51:  // control_instruction ::= control_start instructions else_clause
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); applyElse(((CompoundNode)lapg_m[lapg_head-2].sym),((ElseIfNode)lapg_m[lapg_head-0].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 52:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				 lapg_gg.sym = new ElseIfNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ArrayList<Node>)lapg_m[lapg_head-1].sym), ((ElseIfNode)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-6].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 53:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new ElseIfNode(null, ((ArrayList<Node>)lapg_m[lapg_head-1].sym), null, source, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 54:  // else_clause ::= control_end
				 lapg_gg.sym = null; 
				break;
			case 57:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head-2].offset,lapg_m[lapg_head-2].endoffset, lapg_m[lapg_head-2].line); 
				break;
			case 58:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-8].sym), ((ArrayList)lapg_m[lapg_head-5].sym), ((ArrayList<Node>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head-6].offset,lapg_m[lapg_head-6].endoffset, lapg_m[lapg_head-6].line); 
				break;
			case 59:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 60:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 61:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 62:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 63:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 66:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), null, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-7].sym), ((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // separator_expr ::= Lseparator expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 73:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 84:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ':' expression ')'
				 lapg_gg.sym = createMapCollect(((ExpressionNode)lapg_m[lapg_head-9].sym), ((String)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 85:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-5].sym), templatePackage, false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 90:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 91:  // map_entries ::= identifier ':' conditional_expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 92:  // map_entries ::= map_entries ',' identifier ':' conditional_expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 93:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 94:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 96:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 107:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // conditional_expression ::= conditional_or_expression '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 127:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 128:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, templatePackage, source, lapg_gg.offset, lapg_gg.endoffset);
							((TemplateNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-0].sym));
							entities.add(((TemplateNode)lapg_gg.sym));
						
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
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
