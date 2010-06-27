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
		28, 26, 27, -1, 14, -1, 10, -1, 3, -1, 6, -1, -41, 72, 74, -1,
		-1, 91, -1, -1, -1, -1, 76, -1, 90, 75, -1, -1, -1, -95, -1, -1,
		-1, -121, 85, 73, 95, -171, -215, -253, -287, -313, -337, -357, 119, 121, -375, 22,
		-1, 45, -383, -1, -1, 5, -393, -1, -419, -431, -483, -1, -491, -1, -499, -1,
		-507, 94, 93, -515, -1, 123, -561, -1, -1, 25, 24, 33, 63, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, 69, 51, 54, -569, -1, 12, -1, -575, 21, -1, 120, -583, 36, -609, -1,
		41, 42, -1, -1, -617, -1, 87, -1, -1, 86, 71, -1, -623, -1, -1, 96,
		97, 98, -675, -719, -763, -801, -839, -877, 109, -915, -943, -969, -995, -1019, -1, 122,
		-1, -1, 19, -1039, -1, 46, 15, -1, 77, -1, -1, 38, 39, 44, -1, -1045,
		56, -1, 88, -1, 124, 84, -1053, -1, -1079, -1, 68, -1, -1, -1, 18, -1,
		50, -1105, -1, -1, -1, 59, -1, -1113, -1, -1, -1, 118, -1, -1, 20, -1,
		-1, -1, -1, 61, 60, 57, 89, -1, 79, -1159, 82, -1, -1, 53, 13, -1,
		-1, -1, -1, -1, -1, 65, 62, -1, 80, -1, 83, 52, -1, -1, 58, 81,
		-1, -1, -2, -2,
	};

    private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 11, -1, 29, -1, 27, 9, -1, -2,
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1,
		5, -1, 0, 125, -1, -2, 44, -1, 51, -1, 24, 70, 33, 70, 34, 70,
		35, 70, 36, 70, 37, 70, 38, 70, 39, 70, 42, 70, 43, 70, 45, 70,
		46, 70, 47, 70, 48, 70, 49, 70, 50, 70, 52, 70, 53, 70, 55, 70,
		56, 70, 57, 70, 58, 70, 59, 70, 60, 70, -1, -2, 7, -1, 8, -1,
		9, -1, 16, -1, 26, -1, 30, -1, 31, -1, 36, -1, 40, -1, 42, -1,
		44, -1, 43, 48, -1, -2, 42, -1, 46, -1, 53, -1, 24, 92, 33, 92,
		34, 92, 35, 92, 36, 92, 37, 92, 38, 92, 39, 92, 43, 92, 45, 92,
		47, 92, 48, 92, 49, 92, 50, 92, 52, 92, 55, 92, 56, 92, 57, 92,
		58, 92, 59, 92, 60, 92, -1, -2, 37, -1, 38, -1, 39, -1, 24, 99,
		33, 99, 34, 99, 35, 99, 36, 99, 43, 99, 45, 99, 47, 99, 48, 99,
		49, 99, 50, 99, 52, 99, 55, 99, 56, 99, 57, 99, 58, 99, 59, 99,
		60, 99, -1, -2, 35, -1, 36, -1, 24, 102, 33, 102, 34, 102, 43, 102,
		45, 102, 47, 102, 48, 102, 49, 102, 50, 102, 52, 102, 55, 102, 56, 102,
		57, 102, 58, 102, 59, 102, 60, 102, -1, -2, 55, -1, 56, -1, 57, -1,
		58, -1, 24, 107, 33, 107, 34, 107, 43, 107, 45, 107, 47, 107, 48, 107,
		49, 107, 50, 107, 52, 107, 59, 107, 60, 107, -1, -2, 24, -1, 33, 110,
		34, 110, 43, 110, 45, 110, 47, 110, 48, 110, 49, 110, 50, 110, 52, 110,
		59, 110, 60, 110, -1, -2, 50, -1, 52, -1, 33, 113, 34, 113, 43, 113,
		45, 113, 47, 113, 48, 113, 49, 113, 59, 113, 60, 113, -1, -2, 48, -1,
		33, 115, 34, 115, 43, 115, 45, 115, 47, 115, 49, 115, 59, 115, 60, 115,
		-1, -2, 49, -1, 60, -1, 33, 117, 34, 117, 43, 117, 45, 117, 47, 117,
		59, 117, -1, -2, 47, -1, 33, 34, 34, 34, -1, -2, 44, -1, 46, -1,
		33, 11, 34, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1,
		30, -1, 31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48, -1, -2,
		44, -1, 46, -1, 17, 35, 33, 35, 34, 35, -1, -2, 44, -1, 24, 70,
		33, 70, 34, 70, 35, 70, 36, 70, 37, 70, 38, 70, 39, 70, 42, 70,
		43, 70, 45, 70, 46, 70, 47, 70, 48, 70, 49, 70, 50, 70, 52, 70,
		53, 70, 55, 70, 56, 70, 57, 70, 58, 70, 59, 70, 60, 70, -1, -2,
		47, -1, 33, 40, 34, 40, -1, -2, 47, -1, 33, 67, 34, 67, -1, -2,
		47, -1, 33, 66, 34, 66, -1, -2, 47, -1, 33, 43, 34, 43, -1, -2,
		44, -1, 59, -1, 24, 70, 35, 70, 36, 70, 37, 70, 38, 70, 39, 70,
		42, 70, 43, 70, 46, 70, 47, 70, 48, 70, 49, 70, 50, 70, 52, 70,
		53, 70, 55, 70, 56, 70, 57, 70, 58, 70, 60, 70, -1, -2, 47, -1,
		43, 49, 45, 49, -1, -2, 7, -1, 45, 16, -1, -2, 44, -1, 46, -1,
		51, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 30, -1,
		31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48, -1, -2, 17, -1,
		33, 37, 34, 37, -1, -2, 1, -1, 5, 55, -1, -2, 44, -1, 24, 78,
		33, 78, 34, 78, 35, 78, 36, 78, 37, 78, 38, 78, 39, 78, 42, 78,
		43, 78, 45, 78, 46, 78, 47, 78, 48, 78, 49, 78, 50, 78, 52, 78,
		53, 78, 55, 78, 56, 78, 57, 78, 58, 78, 59, 78, 60, 78, -1, -2,
		37, -1, 38, -1, 39, -1, 24, 100, 33, 100, 34, 100, 35, 100, 36, 100,
		43, 100, 45, 100, 47, 100, 48, 100, 49, 100, 50, 100, 52, 100, 55, 100,
		56, 100, 57, 100, 58, 100, 59, 100, 60, 100, -1, -2, 37, -1, 38, -1,
		39, -1, 24, 101, 33, 101, 34, 101, 35, 101, 36, 101, 43, 101, 45, 101,
		47, 101, 48, 101, 49, 101, 50, 101, 52, 101, 55, 101, 56, 101, 57, 101,
		58, 101, 59, 101, 60, 101, -1, -2, 35, -1, 36, -1, 24, 105, 33, 105,
		34, 105, 43, 105, 45, 105, 47, 105, 48, 105, 49, 105, 50, 105, 52, 105,
		55, 105, 56, 105, 57, 105, 58, 105, 59, 105, 60, 105, -1, -2, 35, -1,
		36, -1, 24, 106, 33, 106, 34, 106, 43, 106, 45, 106, 47, 106, 48, 106,
		49, 106, 50, 106, 52, 106, 55, 106, 56, 106, 57, 106, 58, 106, 59, 106,
		60, 106, -1, -2, 35, -1, 36, -1, 24, 103, 33, 103, 34, 103, 43, 103,
		45, 103, 47, 103, 48, 103, 49, 103, 50, 103, 52, 103, 55, 103, 56, 103,
		57, 103, 58, 103, 59, 103, 60, 103, -1, -2, 35, -1, 36, -1, 24, 104,
		33, 104, 34, 104, 43, 104, 45, 104, 47, 104, 48, 104, 49, 104, 50, 104,
		52, 104, 55, 104, 56, 104, 57, 104, 58, 104, 59, 104, 60, 104, -1, -2,
		46, -1, 24, 108, 33, 108, 34, 108, 43, 108, 45, 108, 47, 108, 48, 108,
		49, 108, 50, 108, 52, 108, 59, 108, 60, 108, -1, -2, 24, -1, 33, 111,
		34, 111, 43, 111, 45, 111, 47, 111, 48, 111, 49, 111, 50, 111, 52, 111,
		59, 111, 60, 111, -1, -2, 24, -1, 33, 112, 34, 112, 43, 112, 45, 112,
		47, 112, 48, 112, 49, 112, 50, 112, 52, 112, 59, 112, 60, 112, -1, -2,
		50, -1, 52, -1, 33, 114, 34, 114, 43, 114, 45, 114, 47, 114, 48, 114,
		49, 114, 59, 114, 60, 114, -1, -2, 48, -1, 33, 116, 34, 116, 43, 116,
		45, 116, 47, 116, 49, 116, 59, 116, 60, 116, -1, -2, 47, -1, 45, 17,
		-1, -2, 47, -1, 33, 64, 34, 64, -1, -2, 7, -1, 8, -1, 9, -1,
		16, -1, 26, -1, 30, -1, 31, -1, 36, -1, 40, -1, 42, -1, 44, -1,
		45, 48, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 30, -1,
		31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48, -1, -2, 47, -1,
		33, 47, 34, 47, -1, -2, 41, -1, 44, -1, 24, 70, 35, 70, 36, 70,
		37, 70, 38, 70, 39, 70, 42, 70, 45, 70, 46, 70, 47, 70, 48, 70,
		49, 70, 50, 70, 52, 70, 53, 70, 55, 70, 56, 70, 57, 70, 58, 70,
		60, 70, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 30, -1,
		31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 2, 18, 31, 44, 57, 73, 78, 141, 192, 244, 249, 250, 252, 256, 258,
		263, 314, 320, 325, 330, 330, 336, 338, 338, 341, 341, 392, 393, 398, 399, 450,
		501, 506, 517, 526, 531, 587, 590, 593, 596, 647, 648, 701, 705, 767, 777, 783,
		803, 805, 806, 808, 810, 812, 813, 813, 814, 815, 816, 817, 821, 822, 822, 823,
		824, 826, 828, 829, 831, 833, 834, 836, 842, 851, 864, 877, 882, 883, 888, 889,
		890, 903, 905, 918, 919, 921, 934, 939, 944, 995, 1046, 1047, 1098, 1149, 1195, 1239,
		1279, 1319, 1357, 1394, 1430, 1466, 1486, 1505, 1511, 1512, 1513, 1514, 1516, 1517, 1518, 1519,
		1520, 1526, 1527,
	};

    private static final short lapg_sym_from[] = {
		240, 241, 0, 1, 4, 7, 14, 19, 27, 64, 132, 188, 196, 205, 219, 228,
		231, 236, 1, 7, 14, 19, 27, 64, 188, 196, 205, 219, 228, 231, 236, 1,
		7, 14, 19, 27, 64, 188, 196, 205, 219, 228, 231, 236, 1, 7, 14, 19,
		27, 64, 188, 196, 205, 219, 228, 231, 236, 0, 1, 4, 7, 14, 19, 27,
		64, 177, 188, 196, 205, 219, 228, 231, 236, 64, 196, 205, 228, 236, 13, 21,
		25, 31, 32, 34, 35, 36, 37, 39, 42, 43, 44, 45, 46, 67, 70, 71,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108,
		109, 110, 111, 112, 116, 117, 124, 127, 131, 133, 135, 136, 141, 170, 174, 182,
		184, 185, 187, 189, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13, 25, 32,
		35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136,
		141, 170, 174, 182, 184, 185, 187, 191, 198, 208, 209, 210, 215, 217, 220, 233,
		13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 124,
		127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198, 208, 209, 210,
		215, 217, 220, 233, 13, 25, 112, 210, 220, 3, 195, 210, 25, 112, 210, 220,
		112, 210, 13, 25, 112, 210, 220, 13, 25, 32, 35, 37, 39, 42, 43, 44,
		45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107,
		108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185,
		187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13, 25, 112, 126, 210, 220,
		13, 25, 112, 210, 220, 13, 25, 112, 210, 220, 13, 25, 112, 161, 210, 220,
		75, 77, 56, 154, 155, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46,
		70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109,
		110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191,
		198, 208, 209, 210, 215, 217, 220, 233, 23, 13, 25, 112, 210, 220, 3, 13,
		25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98,
		99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198, 208, 209, 210, 215, 217,
		220, 233, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93,
		96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112,
		124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198, 208, 209,
		210, 215, 217, 220, 233, 13, 25, 112, 210, 220, 47, 48, 68, 79, 119, 160,
		161, 204, 207, 224, 225, 47, 48, 79, 119, 160, 161, 204, 224, 225, 54, 148,
		149, 150, 151, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 54, 70,
		71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110,
		111, 112, 124, 127, 131, 133, 136, 141, 148, 149, 150, 151, 170, 174, 182, 184,
		185, 187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 53, 146, 147, 53, 146,
		147, 53, 146, 147, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70,
		71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110,
		111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198,
		208, 209, 210, 215, 217, 220, 233, 199, 13, 25, 32, 35, 37, 39, 42, 43,
		44, 45, 46, 49, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104,
		106, 107, 108, 109, 110, 111, 112, 124, 127, 130, 131, 133, 136, 141, 170, 174,
		182, 184, 185, 187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 84, 87, 139,
		223, 13, 25, 28, 32, 35, 37, 39, 42, 43, 44, 45, 46, 66, 70, 71,
		72, 73, 83, 93, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107,
		108, 109, 110, 111, 112, 120, 124, 127, 131, 133, 136, 140, 141, 142, 170, 174,
		182, 184, 185, 187, 191, 198, 199, 201, 208, 209, 210, 215, 217, 220, 233, 88,
		122, 164, 169, 183, 200, 202, 226, 227, 237, 49, 66, 72, 120, 142, 153, 62,
		74, 76, 78, 79, 80, 84, 86, 88, 139, 163, 175, 183, 193, 194, 204, 207,
		224, 226, 237, 58, 157, 59, 57, 156, 28, 167, 57, 156, 49, 55, 55, 55,
		55, 83, 158, 179, 226, 59, 0, 0, 0, 4, 0, 4, 3, 0, 4, 66,
		120, 116, 7, 27, 1, 7, 19, 188, 219, 231, 47, 48, 79, 119, 160, 161,
		204, 224, 225, 1, 7, 14, 19, 27, 64, 188, 196, 205, 219, 228, 231, 236,
		1, 7, 14, 19, 27, 64, 188, 196, 205, 219, 228, 231, 236, 13, 25, 112,
		210, 220, 74, 21, 31, 67, 95, 105, 126, 72, 1, 7, 14, 19, 27, 64,
		188, 196, 205, 219, 228, 231, 236, 64, 228, 1, 7, 14, 19, 27, 64, 188,
		196, 205, 219, 228, 231, 236, 177, 177, 196, 1, 7, 14, 19, 27, 64, 188,
		196, 205, 219, 228, 231, 236, 13, 25, 112, 210, 220, 64, 196, 205, 228, 236,
		13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71, 93, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111, 112, 124, 127,
		131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198, 208, 209, 210, 215,
		217, 220, 233, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 70, 71,
		93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107, 108, 109, 110, 111,
		112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198, 208,
		209, 210, 215, 217, 220, 233, 45, 13, 25, 32, 35, 37, 39, 42, 43, 44,
		45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103, 104, 106, 107,
		108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185,
		187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13, 25, 32, 35, 37, 39,
		42, 43, 44, 45, 46, 70, 71, 93, 96, 97, 98, 99, 100, 101, 102, 103,
		104, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174,
		182, 184, 185, 187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13, 25, 32,
		35, 37, 39, 42, 45, 46, 70, 71, 93, 99, 100, 101, 102, 103, 104, 106,
		107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184,
		185, 187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13, 25, 32, 35, 37,
		39, 42, 45, 46, 70, 71, 93, 101, 102, 103, 104, 106, 107, 108, 109, 110,
		111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198,
		208, 209, 210, 215, 217, 220, 233, 13, 25, 32, 35, 37, 39, 42, 45, 46,
		70, 71, 93, 106, 107, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141,
		170, 174, 182, 184, 185, 187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13,
		25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 106, 107, 108, 109, 110,
		111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198,
		208, 209, 210, 215, 217, 220, 233, 13, 25, 32, 35, 37, 39, 42, 45, 46,
		70, 71, 93, 108, 109, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174,
		182, 184, 185, 187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13, 25, 32,
		35, 37, 39, 42, 45, 46, 70, 71, 93, 109, 110, 111, 112, 124, 127, 131,
		133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198, 208, 209, 210, 215, 217,
		220, 233, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70, 71, 93, 110, 111,
		112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185, 187, 191, 198, 208,
		209, 210, 215, 217, 220, 233, 13, 25, 32, 35, 37, 39, 42, 45, 46, 70,
		71, 93, 110, 111, 112, 124, 127, 131, 133, 136, 141, 170, 174, 182, 184, 185,
		187, 191, 198, 208, 209, 210, 215, 217, 220, 233, 13, 25, 35, 37, 39, 42,
		46, 93, 111, 112, 131, 141, 170, 187, 191, 209, 210, 215, 220, 233, 13, 25,
		35, 37, 39, 42, 46, 93, 112, 131, 141, 170, 187, 191, 209, 210, 215, 220,
		233, 45, 70, 124, 182, 184, 217, 1, 0, 3, 66, 120, 116, 72, 126, 74,
		45, 70, 124, 182, 184, 217, 132,
	};

    private static final short lapg_sym_to[] = {
		242, 243, 2, 9, 2, 9, 9, 9, 9, 9, 176, 9, 9, 9, 9, 9,
		9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 13, 3, 25, 13, 13, 25,
		112, 195, 13, 210, 220, 13, 112, 13, 220, 113, 113, 113, 113, 113, 28, 65,
		28, 65, 73, 75, 28, 77, 28, 28, 28, 73, 73, 83, 28, 65, 73, 73,
		28, 140, 65, 73, 73, 73, 73, 73, 73, 73, 73, 73, 65, 73, 73, 73,
		73, 73, 28, 28, 162, 165, 73, 73, 28, 73, 179, 73, 28, 28, 73, 199,
		73, 73, 28, 206, 28, 73, 73, 28, 28, 28, 73, 28, 28, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 152, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 31, 31, 31, 31, 31, 20, 209, 209, 68, 160, 160, 160,
		161, 225, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 34, 34, 34, 170, 34, 34,
		35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 37, 37, 37, 187, 37, 37,
		130, 131, 105, 105, 105, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 67, 39, 39, 39, 39, 39, 21, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 42, 42, 42, 42, 42, 89, 89, 121, 89, 89, 89,
		89, 89, 222, 89, 89, 90, 90, 90, 90, 90, 90, 90, 90, 90, 99, 99,
		99, 99, 99, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 100, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 100, 100, 100, 100, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 96, 96, 96, 97, 97,
		97, 98, 98, 98, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 215, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 93, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 174, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 134, 137, 181,
		229, 46, 46, 70, 46, 46, 46, 46, 46, 46, 46, 46, 46, 116, 46, 46,
		124, 70, 70, 46, 141, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 116, 46, 46, 46, 46, 46, 182, 46, 184, 46, 46,
		46, 46, 46, 46, 46, 46, 70, 217, 46, 46, 46, 46, 46, 46, 46, 138,
		168, 190, 192, 201, 216, 218, 232, 234, 239, 94, 117, 117, 117, 117, 117, 111,
		127, 111, 111, 111, 111, 135, 136, 111, 111, 189, 111, 111, 111, 208, 111, 111,
		111, 111, 111, 108, 108, 109, 106, 106, 71, 191, 107, 107, 95, 101, 102, 103,
		104, 133, 185, 198, 233, 110, 240, 4, 5, 24, 6, 6, 22, 7, 7, 118,
		118, 163, 26, 69, 14, 27, 64, 205, 228, 236, 91, 92, 132, 166, 186, 188,
		219, 230, 231, 15, 15, 63, 15, 63, 63, 15, 211, 63, 15, 63, 15, 63,
		16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 47, 47, 47,
		47, 47, 128, 66, 72, 120, 142, 153, 171, 125, 17, 17, 17, 17, 17, 17,
		17, 17, 17, 17, 17, 17, 17, 114, 235, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 18, 18, 18, 196, 197, 212, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 48, 48, 48, 48, 48, 115, 213, 221, 115, 238,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 84, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52, 52, 52, 52, 52,
		52, 81, 82, 52, 52, 52, 52, 52, 143, 144, 145, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 146, 147, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 148, 149, 150, 151, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 154, 155, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 156, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 157, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59,
		59, 59, 59, 59, 59, 59, 60, 60, 74, 60, 60, 60, 60, 85, 60, 85,
		123, 60, 158, 60, 60, 85, 173, 60, 178, 180, 60, 60, 194, 85, 85, 203,
		60, 60, 214, 223, 60, 60, 60, 85, 60, 60, 61, 61, 61, 61, 61, 61,
		61, 61, 159, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 62, 62,
		76, 78, 79, 80, 88, 139, 62, 175, 183, 193, 204, 207, 224, 62, 226, 62,
		237, 86, 86, 86, 86, 86, 86, 241, 8, 23, 119, 167, 164, 126, 172, 129,
		87, 122, 169, 200, 202, 227, 177,
	};

    private static final short lapg_rlen[] = {
		0, 1, 1, 2, 1, 3, 2, 1, 1, 0, 1, 0, 1, 8, 1, 5,
		0, 1, 3, 1, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 3, 1, 0, 1, 0, 1, 4, 0, 1, 3, 2, 2, 1, 3, 2,
		0, 1, 3, 3, 7, 5, 1, 0, 1, 7, 11, 1, 2, 2, 4, 3,
		4, 8, 2, 2, 3, 1, 1, 3, 1, 1, 1, 1, 1, 4, 3, 6,
		8, 10, 6, 8, 4, 1, 3, 3, 3, 5, 1, 1, 1, 2, 2, 1,
		3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 1, 3, 3, 1, 3,
		3, 1, 3, 1, 3, 1, 5, 1, 3, 1, 3, 1, 3, 1,
	};

    private static final short lapg_rlex[] = {
		105, 105, 62, 63, 63, 64, 64, 64, 64, 106, 106, 107, 107, 65, 66, 67,
		108, 108, 68, 69, 69, 70, 71, 71, 72, 72, 73, 73, 73, 73, 73, 73,
		73, 74, 75, 109, 109, 110, 110, 75, 111, 111, 75, 75, 76, 77, 77, 78,
		112, 112, 79, 80, 81, 81, 81, 113, 113, 82, 82, 83, 83, 83, 84, 85,
		86, 86, 86, 86, 87, 87, 88, 88, 88, 88, 88, 88, 88, 88, 88, 88,
		88, 88, 88, 88, 88, 88, 89, 89, 90, 90, 91, 91, 92, 92, 92, 93,
		93, 93, 93, 94, 94, 94, 95, 95, 95, 95, 95, 96, 96, 96, 97, 97,
		97, 98, 98, 99, 99, 100, 100, 101, 101, 102, 102, 103, 103, 104,
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
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 62;
		public static final int templates = 63;
		public static final int template_declaration_or_space = 64;
		public static final int query_def = 65;
		public static final int cached_flag = 66;
		public static final int template_start = 67;
		public static final int parameters = 68;
		public static final int identifier_list = 69;
		public static final int template_end = 70;
		public static final int instructions = 71;
		public static final int LBRACKETMINUSRBRACKETRBRACE = 72;
		public static final int instruction = 73;
		public static final int simple_instruction = 74;
		public static final int sentence = 75;
		public static final int comma_expr = 76;
		public static final int qualified_id = 77;
		public static final int template_for_expr = 78;
		public static final int template_arguments = 79;
		public static final int control_instruction = 80;
		public static final int else_clause = 81;
		public static final int switch_instruction = 82;
		public static final int case_list = 83;
		public static final int one_case = 84;
		public static final int control_start = 85;
		public static final int control_sentence = 86;
		public static final int control_end = 87;
		public static final int primary_expression = 88;
		public static final int complex_data = 89;
		public static final int map_entries = 90;
		public static final int bcon = 91;
		public static final int unary_expression = 92;
		public static final int mult_expression = 93;
		public static final int additive_expression = 94;
		public static final int relational_expression = 95;
		public static final int instanceof_expression = 96;
		public static final int equality_expression = 97;
		public static final int conditional_and_expression = 98;
		public static final int conditional_or_expression = 99;
		public static final int conditional_expression = 100;
		public static final int assignment_expression = 101;
		public static final int expression = 102;
		public static final int expression_list = 103;
		public static final int body = 104;
		public static final int templatesopt = 105;
		public static final int cached_flagopt = 106;
		public static final int parametersopt = 107;
		public static final int identifier_listopt = 108;
		public static final int template_argumentsopt = 109;
		public static final int template_for_expropt = 110;
		public static final int comma_expropt = 111;
		public static final int expression_listopt = 112;
		public static final int anyopt = 113;
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

		while( lapg_m[lapg_head].state != 242+state ) {
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

		if( lapg_m[lapg_head].state != 242+state ) {
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
			case 64:  // control_sentence ::= Lforeach identifier Lin expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 65:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']'
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-6].sym), ((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 81:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ':' expression ')'
				 lapg_gg.sym = createMapCollect(((ExpressionNode)lapg_m[lapg_head-9].sym), ((String)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 82:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym), ((ExpressionNode)lapg_m[lapg_head-5].sym), templatePackage, false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // map_entries ::= identifier ':' conditional_expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 89:  // map_entries ::= map_entries ',' identifier ':' conditional_expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 90:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 91:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 93:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // conditional_expression ::= conditional_or_expression '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 124:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 125:  // body ::= instructions
				
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
