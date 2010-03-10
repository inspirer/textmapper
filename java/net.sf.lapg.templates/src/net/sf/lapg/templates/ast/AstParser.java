package net.sf.lapg.templates.ast;


import java.io.CharArrayReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;
import net.sf.lapg.templates.ast.AstLexer.LapgSymbol;
import net.sf.lapg.templates.ast.AstLexer.Lexems;

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
	private ArrayList<IBundleEntity> entities;
	private String templatePackage;
	
	protected String inputName;
	private int killEnds = -1;
	private char[] buff;
	
	private String rawText(int start, int end) {
		if( killEnds == start ) {
			while( start < end && (buff[start] == '\t' || buff[start] == ' ') )
				start++;
	
			if( start < end && buff[start] == '\r' )
				start++;
	
			if( start < end && buff[start] == '\n' )
				start++;
		}
		return new String(buff, start, end-start);
	}
	
	private void checkIsSpace(int start, int end, int line) {
		String val = rawText(start,end).trim();
		if( val.length() > 0 )
			reporter.error(start, end, line, "Unknown text ignored: `"+val+"`");
	}
	
	private void applyElse(CompoundNode node, ArrayList<Node> instructions, int offset, int endoffset, int line) {
		if( node instanceof IfNode ) {
			((IfNode)node).setElseInstructions(instructions);
		} else {
			reporter.error(offset, endoffset, line, "Unknown else node, instructions skipped");
		}
	}
	
	private ExpressionNode createCollectionProcessor(ExpressionNode forExpr, String instruction, String varName, ExpressionNode foreachExpr, String input, int line, int offset, int endoffset) {
		char first = instruction.charAt(0);
		int kind = 0;
		switch(first) {
		case 'c':
			if(instruction.equals("collect")) {
				kind = CollectionProcessorNode.COLLECT;
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
			return new ErrorNode(input,line);
		}
		return new CollectionProcessorNode(forExpr,kind,varName,foreachExpr,input,line);
	}
	
	private Node createEscapedId(String escid, int line) {
		int sharp = escid.indexOf('#');
		if( sharp >= 0 ) {
			Integer index = new Integer(escid.substring(sharp+1));
			escid = escid.substring(0, sharp);
			return new IndexNode(new SelectNode(null,escid,inputName,line), new LiteralNode(index,inputName,line),inputName,line);
		
		} else {
			return new SelectNode(null,escid,inputName,line);
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
	
	public boolean parse(String s, String templatePackage, String inputName) {
		this.templatePackage = templatePackage;
		this.inputName = inputName;
		entities = new ArrayList<IBundleEntity>();
		try {
			buff = s.toCharArray();
			AstLexer lexer = new AstLexer(new CharArrayReader(buff), reporter);
			parseInput(lexer);
			return true;
		} catch( ParseException ex ) {
			return false;
		} catch( IOException ex ) {
			return false;
		}
	}
	
	public boolean parseBody(String s, String templatePackage, String inputName) {
		this.templatePackage = templatePackage;
		this.inputName = inputName;
		entities = new ArrayList<IBundleEntity>();
		try {
			buff = s.toCharArray();
			AstLexer lexer = new AstLexer(new CharArrayReader(buff), reporter);
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
		-3, -1, 8, -1, -11, 4, 7, -1, 2, 29, 28, 26, 27, -1, -19, 20,
		25, 23, 24, -1, -1, -1, 3, -1, 6, -1, -33, 67, 69, -1, -1, 85,
		-1, -1, -1, -1, 71, -1, 84, 70, -1, -1, -1, -83, -1, -1, -1, -109,
		79, 68, 89, -157, -199, -235, -267, -291, -311, 31, 19, -1, 42, -329, -337, -1,
		5, -347, -373, -385, -1, 62, -1, 61, -1, 40, 88, 87, -393, -1, 110, -437,
		-1, -1, 22, 21, 30, 58, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, 64, -1, 48, -445, -1, 10, -1, -1,
		18, -1, -451, 33, -477, -1, 38, 39, -1, -1, -485, -1, 81, -1, -1, 80,
		66, -1, -491, -1, -1, 90, 91, 92, -541, -583, -625, -661, -697, -733, -769, -801,
		-833, -857, -1, -1, -1, -1, 16, -877, -1, 43, -1, 12, 72, -1, -1, 35,
		36, 41, -1, 59, 52, -1, 82, -1, 111, 78, -883, -1, -909, -1, 63, 50,
		-1, 49, -1, 15, -1, 47, 44, -1, -1, -1, 54, -1, -935, -1, -1, -1,
		109, 17, 11, 60, -1, -1, 56, 55, 53, 83, -1, 74, -979, 76, -1, -1,
		-1, 57, 75, 77, -1, -1, -2, -2,
	};

    private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 1, -1, 5, -1, 0, 1, -1, -2,
		1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 0, 112, -1, -2, 43, -1,
		32, 65, 33, 65, 34, 65, 35, 65, 36, 65, 37, 65, 38, 65, 41, 65,
		42, 65, 44, 65, 45, 65, 46, 65, 47, 65, 48, 65, 49, 65, 51, 65,
		52, 65, 53, 65, 54, 65, 55, 65, 56, 65, 57, 65, 58, 65, -1, -2,
		7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 29, -1, 30, -1, 35, -1,
		39, -1, 41, -1, 43, -1, 42, 45, -1, -2, 41, -1, 45, -1, 52, -1,
		32, 86, 33, 86, 34, 86, 35, 86, 36, 86, 37, 86, 38, 86, 42, 86,
		44, 86, 46, 86, 47, 86, 48, 86, 49, 86, 51, 86, 53, 86, 54, 86,
		55, 86, 56, 86, 57, 86, 58, 86, -1, -2, 36, -1, 37, -1, 38, -1,
		32, 93, 33, 93, 34, 93, 35, 93, 42, 93, 44, 93, 46, 93, 47, 93,
		48, 93, 49, 93, 51, 93, 53, 93, 54, 93, 55, 93, 56, 93, 57, 93,
		58, 93, -1, -2, 34, -1, 35, -1, 32, 96, 33, 96, 42, 96, 44, 96,
		46, 96, 47, 96, 48, 96, 49, 96, 51, 96, 53, 96, 54, 96, 55, 96,
		56, 96, 57, 96, 58, 96, -1, -2, 53, -1, 54, -1, 55, -1, 56, -1,
		32, 101, 33, 101, 42, 101, 44, 101, 46, 101, 47, 101, 48, 101, 49, 101,
		51, 101, 57, 101, 58, 101, -1, -2, 49, -1, 51, -1, 32, 104, 33, 104,
		42, 104, 44, 104, 46, 104, 47, 104, 48, 104, 57, 104, 58, 104, -1, -2,
		47, -1, 32, 106, 33, 106, 42, 106, 44, 106, 46, 106, 48, 106, 57, 106,
		58, 106, -1, -2, 48, -1, 58, -1, 32, 108, 33, 108, 42, 108, 44, 108,
		46, 108, 57, 108, -1, -2, 43, -1, 45, -1, 50, 9, -1, -2, 43, -1,
		45, -1, 32, 9, 33, 9, -1, -2, 7, -1, 8, -1, 9, -1, 15, -1,
		25, -1, 29, -1, 30, -1, 35, -1, 39, -1, 41, -1, 43, -1, 44, 45,
		-1, -2, 43, -1, 45, -1, 16, 32, 32, 32, 33, 32, -1, -2, 46, -1,
		32, 37, 33, 37, -1, -2, 43, -1, 57, -1, 34, 65, 35, 65, 36, 65,
		37, 65, 38, 65, 41, 65, 42, 65, 45, 65, 46, 65, 47, 65, 48, 65,
		49, 65, 51, 65, 52, 65, 53, 65, 54, 65, 55, 65, 56, 65, 58, 65,
		-1, -2, 46, -1, 42, 46, 44, 46, -1, -2, 7, -1, 44, 13, -1, -2,
		7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 29, -1, 30, -1, 35, -1,
		39, -1, 41, -1, 43, -1, 44, 45, -1, -2, 16, -1, 32, 34, 33, 34,
		-1, -2, 1, -1, 5, 51, -1, -2, 43, -1, 32, 73, 33, 73, 34, 73,
		35, 73, 36, 73, 37, 73, 38, 73, 41, 73, 42, 73, 44, 73, 45, 73,
		46, 73, 47, 73, 48, 73, 49, 73, 51, 73, 52, 73, 53, 73, 54, 73,
		55, 73, 56, 73, 57, 73, 58, 73, -1, -2, 36, -1, 37, -1, 38, -1,
		32, 94, 33, 94, 34, 94, 35, 94, 42, 94, 44, 94, 46, 94, 47, 94,
		48, 94, 49, 94, 51, 94, 53, 94, 54, 94, 55, 94, 56, 94, 57, 94,
		58, 94, -1, -2, 36, -1, 37, -1, 38, -1, 32, 95, 33, 95, 34, 95,
		35, 95, 42, 95, 44, 95, 46, 95, 47, 95, 48, 95, 49, 95, 51, 95,
		53, 95, 54, 95, 55, 95, 56, 95, 57, 95, 58, 95, -1, -2, 34, -1,
		35, -1, 32, 99, 33, 99, 42, 99, 44, 99, 46, 99, 47, 99, 48, 99,
		49, 99, 51, 99, 53, 99, 54, 99, 55, 99, 56, 99, 57, 99, 58, 99,
		-1, -2, 34, -1, 35, -1, 32, 100, 33, 100, 42, 100, 44, 100, 46, 100,
		47, 100, 48, 100, 49, 100, 51, 100, 53, 100, 54, 100, 55, 100, 56, 100,
		57, 100, 58, 100, -1, -2, 34, -1, 35, -1, 32, 97, 33, 97, 42, 97,
		44, 97, 46, 97, 47, 97, 48, 97, 49, 97, 51, 97, 53, 97, 54, 97,
		55, 97, 56, 97, 57, 97, 58, 97, -1, -2, 34, -1, 35, -1, 32, 98,
		33, 98, 42, 98, 44, 98, 46, 98, 47, 98, 48, 98, 49, 98, 51, 98,
		53, 98, 54, 98, 55, 98, 56, 98, 57, 98, 58, 98, -1, -2, 53, -1,
		54, -1, 55, -1, 56, -1, 32, 102, 33, 102, 42, 102, 44, 102, 46, 102,
		47, 102, 48, 102, 49, 102, 51, 102, 57, 102, 58, 102, -1, -2, 53, -1,
		54, -1, 55, -1, 56, -1, 32, 103, 33, 103, 42, 103, 44, 103, 46, 103,
		47, 103, 48, 103, 49, 103, 51, 103, 57, 103, 58, 103, -1, -2, 49, -1,
		51, -1, 32, 105, 33, 105, 42, 105, 44, 105, 46, 105, 47, 105, 48, 105,
		57, 105, 58, 105, -1, -2, 47, -1, 32, 107, 33, 107, 42, 107, 44, 107,
		46, 107, 48, 107, 57, 107, 58, 107, -1, -2, 46, -1, 44, 14, -1, -2,
		7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 29, -1, 30, -1, 35, -1,
		39, -1, 41, -1, 43, -1, 44, 45, -1, -2, 7, -1, 8, -1, 9, -1,
		15, -1, 25, -1, 29, -1, 30, -1, 35, -1, 39, -1, 41, -1, 43, -1,
		44, 45, -1, -2, 40, -1, 43, -1, 34, 65, 35, 65, 36, 65, 37, 65,
		38, 65, 41, 65, 44, 65, 45, 65, 46, 65, 47, 65, 48, 65, 49, 65,
		51, 65, 52, 65, 53, 65, 54, 65, 55, 65, 56, 65, 58, 65, -1, -2,
		7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 29, -1, 30, -1, 35, -1,
		39, -1, 41, -1, 43, -1, 44, 45, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 2, 14, 23, 32, 41, 53, 56, 114, 161, 208, 213, 215, 219, 220, 225,
		272, 278, 283, 288, 288, 293, 294, 294, 294, 294, 341, 342, 347, 348, 395, 442,
		447, 456, 463, 468, 520, 523, 526, 529, 576, 577, 625, 628, 685, 694, 699, 704,
		706, 707, 709, 710, 712, 713, 716, 719, 722, 725, 729, 730, 730, 730, 730, 731,
		732, 734, 736, 738, 740, 741, 743, 747, 754, 763, 772, 777, 778, 782, 783, 784,
		793, 794, 803, 804, 806, 815, 820, 823, 870, 917, 918, 965, 1012, 1054, 1094, 1130,
		1164, 1197, 1229, 1261, 1267, 1268, 1269, 1271, 1272, 1273, 1274, 1275, 1281, 1282,
	};

    private static final short lapg_sym_from[] = {
		212, 213, 0, 1, 4, 7, 14, 19, 25, 59, 105, 122, 149, 185, 1, 7,
		14, 19, 25, 59, 105, 149, 185, 1, 7, 14, 19, 25, 59, 105, 149, 185,
		1, 7, 14, 19, 25, 59, 105, 149, 185, 0, 1, 4, 7, 14, 19, 25,
		59, 105, 149, 165, 185, 59, 149, 185, 13, 20, 21, 23, 29, 30, 32, 33,
		34, 35, 37, 40, 41, 42, 43, 44, 65, 86, 87, 88, 89, 90, 91, 92,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 107, 108, 114, 117, 120,
		121, 123, 125, 126, 131, 154, 158, 170, 172, 173, 176, 178, 183, 187, 196, 197,
		202, 204, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43, 44, 65, 86, 89,
		90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 114, 117,
		120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202,
		204, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43, 44, 65, 86, 89, 90,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 114, 117, 120,
		121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204,
		13, 23, 103, 176, 197, 184, 197, 23, 103, 176, 197, 103, 13, 23, 103, 176,
		197, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43, 44, 65, 86, 89, 90,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 114, 117, 120,
		121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204,
		13, 23, 103, 116, 176, 197, 13, 23, 103, 176, 197, 13, 23, 103, 176, 197,
		13, 23, 103, 176, 197, 70, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43,
		44, 65, 86, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101,
		102, 103, 114, 117, 120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183,
		187, 196, 197, 202, 204, 3, 13, 23, 103, 176, 197, 3, 13, 23, 30, 33,
		35, 37, 40, 41, 42, 43, 44, 65, 86, 89, 90, 91, 92, 93, 94, 95,
		96, 97, 98, 99, 100, 101, 102, 103, 114, 117, 120, 121, 123, 126, 131, 154,
		158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 13, 23, 30, 33, 35,
		37, 40, 41, 42, 43, 44, 65, 86, 89, 90, 91, 92, 93, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 103, 114, 117, 120, 121, 123, 126, 131, 154, 158,
		170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 13, 23, 103, 176, 197, 45,
		46, 63, 72, 111, 147, 148, 180, 206, 45, 46, 72, 111, 147, 148, 206, 52,
		138, 139, 140, 141, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43, 44, 52,
		65, 86, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		103, 114, 117, 120, 121, 123, 126, 131, 138, 139, 140, 141, 154, 158, 170, 172,
		173, 176, 183, 187, 196, 197, 202, 204, 51, 136, 137, 51, 136, 137, 51, 136,
		137, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43, 44, 65, 86, 89, 90,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 114, 117, 120,
		121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204,
		188, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43, 44, 47, 65, 86, 89,
		90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 114, 117,
		120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202,
		204, 77, 80, 129, 13, 23, 26, 30, 33, 35, 37, 40, 41, 42, 43, 44,
		61, 62, 65, 66, 76, 86, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97,
		98, 99, 100, 101, 102, 103, 114, 117, 120, 121, 123, 126, 130, 131, 132, 154,
		158, 170, 172, 173, 176, 183, 187, 188, 190, 196, 197, 202, 204, 81, 113, 152,
		157, 171, 189, 191, 207, 208, 47, 61, 62, 66, 132, 67, 77, 79, 151, 162,
		55, 145, 56, 54, 144, 110, 54, 144, 47, 53, 142, 143, 53, 142, 143, 53,
		142, 143, 53, 142, 143, 68, 76, 146, 167, 56, 0, 0, 0, 4, 0, 4,
		0, 4, 61, 62, 107, 7, 25, 1, 7, 19, 105, 45, 46, 72, 111, 147,
		148, 206, 1, 7, 14, 19, 25, 59, 105, 149, 185, 1, 7, 14, 19, 25,
		59, 105, 149, 185, 13, 23, 103, 176, 197, 67, 20, 21, 29, 88, 116, 66,
		1, 7, 14, 19, 25, 59, 105, 149, 185, 59, 1, 7, 14, 19, 25, 59,
		105, 149, 185, 165, 165, 185, 1, 7, 14, 19, 25, 59, 105, 149, 185, 13,
		23, 103, 176, 197, 59, 149, 185, 13, 23, 30, 33, 35, 37, 40, 41, 42,
		43, 44, 65, 86, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
		101, 102, 103, 114, 117, 120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176,
		183, 187, 196, 197, 202, 204, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43,
		44, 65, 86, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101,
		102, 103, 114, 117, 120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183,
		187, 196, 197, 202, 204, 43, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43,
		44, 65, 86, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101,
		102, 103, 114, 117, 120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183,
		187, 196, 197, 202, 204, 13, 23, 30, 33, 35, 37, 40, 41, 42, 43, 44,
		65, 86, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		103, 114, 117, 120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176, 183, 187,
		196, 197, 202, 204, 13, 23, 30, 33, 35, 37, 40, 43, 44, 65, 86, 92,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 114, 117, 120, 121, 123,
		126, 131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 13, 23,
		30, 33, 35, 37, 40, 43, 44, 65, 86, 94, 95, 96, 97, 98, 99, 100,
		101, 102, 103, 114, 117, 120, 121, 123, 126, 131, 154, 158, 170, 172, 173, 176,
		183, 187, 196, 197, 202, 204, 13, 23, 30, 33, 35, 37, 40, 43, 44, 65,
		86, 98, 99, 100, 101, 102, 103, 114, 117, 120, 121, 123, 126, 131, 154, 158,
		170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 13, 23, 30, 33, 35, 37,
		40, 43, 44, 65, 86, 100, 101, 102, 103, 114, 117, 120, 121, 123, 126, 131,
		154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 13, 23, 30, 33,
		35, 37, 40, 43, 44, 65, 86, 101, 102, 103, 114, 117, 120, 121, 123, 126,
		131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 13, 23, 30,
		33, 35, 37, 40, 43, 44, 65, 86, 102, 103, 114, 117, 120, 121, 123, 126,
		131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 13, 23, 30,
		33, 35, 37, 40, 43, 44, 65, 86, 102, 103, 114, 117, 120, 121, 123, 126,
		131, 154, 158, 170, 172, 173, 176, 183, 187, 196, 197, 202, 204, 43, 65, 114,
		170, 172, 204, 1, 0, 61, 62, 107, 66, 116, 67, 43, 65, 114, 170, 172,
		204, 122,
	};

    private static final short lapg_sym_to[] = {
		214, 215, 2, 9, 2, 9, 9, 9, 9, 9, 9, 164, 9, 9, 10, 10,
		10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 13, 3, 23, 13, 13, 23,
		103, 13, 176, 184, 197, 104, 104, 104, 26, 60, 60, 26, 60, 26, 68, 26,
		70, 26, 26, 26, 26, 26, 76, 26, 26, 26, 130, 60, 26, 26, 26, 26,
		26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 150, 153, 26, 26, 26,
		26, 26, 167, 26, 26, 26, 26, 188, 26, 26, 26, 193, 26, 26, 26, 26,
		26, 26, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27,
		27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27,
		27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27,
		27, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		29, 29, 29, 29, 29, 196, 196, 63, 147, 147, 147, 148, 30, 30, 30, 30,
		30, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		32, 32, 32, 158, 32, 32, 33, 33, 33, 33, 33, 34, 34, 34, 34, 34,
		35, 35, 35, 35, 35, 121, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 20, 37, 37, 37, 37, 37, 21, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 40, 40, 40, 40, 40, 82,
		82, 112, 82, 82, 82, 82, 194, 82, 83, 83, 83, 83, 83, 83, 83, 92,
		92, 92, 92, 92, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 93,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 93, 93, 93, 93, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 89, 89, 89, 90, 90, 90, 91, 91,
		91, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		202, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 86, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 124, 127, 169, 44, 44, 65, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		107, 107, 44, 114, 65, 44, 131, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 170, 44, 172, 44,
		44, 44, 44, 44, 44, 44, 44, 65, 204, 44, 44, 44, 44, 128, 156, 179,
		181, 190, 203, 205, 210, 211, 87, 108, 108, 108, 108, 117, 125, 126, 178, 183,
		100, 100, 101, 98, 98, 154, 99, 99, 88, 94, 94, 94, 95, 95, 95, 96,
		96, 96, 97, 97, 97, 120, 123, 173, 187, 102, 212, 4, 5, 22, 6, 6,
		7, 7, 109, 109, 151, 24, 64, 14, 25, 59, 149, 84, 85, 122, 155, 174,
		175, 209, 15, 15, 58, 15, 58, 58, 15, 58, 198, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 45, 45, 45, 45, 45, 118, 61, 62, 66, 132, 159, 115,
		17, 17, 17, 17, 17, 17, 17, 17, 17, 105, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 185, 186, 199, 19, 19, 19, 19, 19, 19, 19, 19, 19, 46,
		46, 46, 46, 46, 106, 177, 200, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 77, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 74, 75, 50, 50,
		50, 50, 133, 134, 135, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 136,
		137, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 138, 139, 140, 141, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 142, 143, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 144, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 145, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57, 67,
		69, 71, 72, 73, 78, 81, 78, 129, 146, 57, 78, 161, 162, 163, 166, 168,
		171, 180, 182, 78, 78, 192, 57, 195, 201, 206, 57, 207, 78, 79, 79, 79,
		79, 79, 79, 213, 8, 110, 111, 152, 116, 160, 119, 80, 113, 157, 189, 191,
		208, 165,
	};

    private static final short lapg_rlen[] = {
		0, 1, 1, 2, 1, 3, 2, 1, 1, 0, 1, 7, 5, 0, 1, 3,
		1, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1,
		0, 1, 0, 1, 4, 0, 1, 3, 2, 2, 1, 3, 2, 0, 1, 3,
		3, 5, 3, 0, 1, 7, 1, 2, 2, 4, 3, 4, 6, 2, 2, 3,
		1, 1, 3, 1, 1, 1, 1, 1, 4, 3, 6, 8, 6, 8, 4, 1,
		3, 3, 3, 5, 1, 1, 1, 2, 2, 1, 3, 3, 3, 1, 3, 3,
		1, 3, 3, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1, 5, 1, 3,
		1,
	};

    private static final short lapg_rlex[] = {
		101, 101, 62, 63, 63, 64, 64, 64, 64, 102, 102, 65, 66, 103, 103, 67,
		68, 68, 69, 70, 70, 71, 71, 72, 72, 72, 72, 72, 72, 72, 73, 74,
		104, 104, 105, 105, 74, 106, 106, 74, 74, 75, 76, 76, 77, 107, 107, 78,
		79, 79, 80, 108, 108, 81, 82, 82, 82, 83, 84, 85, 85, 85, 85, 86,
		86, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87,
		88, 88, 89, 89, 90, 90, 91, 91, 91, 92, 92, 92, 92, 93, 93, 93,
		94, 94, 94, 94, 94, 95, 95, 95, 96, 96, 97, 97, 98, 98, 99, 99,
		100,
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
		"'<='",
		"'>='",
		"'<'",
		"'>'",
		"':'",
		"'?'",
		"'`'",
		"'$'",
		"_skip",
		"input",
		"templates",
		"template_declaration_or_space",
		"query_def",
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
		"else_node",
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
		"equality_expression",
		"conditional_and_expression",
		"conditional_or_expression",
		"expression",
		"expression_list",
		"body",
		"templatesopt",
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
		public static final int template_start = 66;
		public static final int parameters = 67;
		public static final int identifier_list = 68;
		public static final int template_end = 69;
		public static final int instructions = 70;
		public static final int LBRACKETMINUSRBRACKETRBRACE = 71;
		public static final int instruction = 72;
		public static final int simple_instruction = 73;
		public static final int sentence = 74;
		public static final int comma_expr = 75;
		public static final int qualified_id = 76;
		public static final int template_for_expr = 77;
		public static final int template_arguments = 78;
		public static final int control_instruction = 79;
		public static final int else_node = 80;
		public static final int switch_instruction = 81;
		public static final int case_list = 82;
		public static final int one_case = 83;
		public static final int control_start = 84;
		public static final int control_sentence = 85;
		public static final int control_end = 86;
		public static final int primary_expression = 87;
		public static final int complex_data = 88;
		public static final int map_entries = 89;
		public static final int bcon = 90;
		public static final int unary_expression = 91;
		public static final int mult_expression = 92;
		public static final int additive_expression = 93;
		public static final int relational_expression = 94;
		public static final int equality_expression = 95;
		public static final int conditional_and_expression = 96;
		public static final int conditional_or_expression = 97;
		public static final int expression = 98;
		public static final int expression_list = 99;
		public static final int body = 100;
		public static final int templatesopt = 101;
		public static final int parametersopt = 102;
		public static final int identifier_listopt = 103;
		public static final int template_argumentsopt = 104;
		public static final int template_for_expropt = 105;
		public static final int comma_expropt = 106;
		public static final int expression_listopt = 107;
		public static final int anyopt = 108;
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

		while( lapg_m[lapg_head].state != 214+state ) {
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

		if( lapg_m[lapg_head].state != 214+state ) {
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
			case 11:  // query_def ::= '${' Lquery qualified_id parametersopt '=' expression '}'
				 lapg_gg.sym = new QueryNode(((String)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-3].sym), templatePackage, ((ExpressionNode)lapg_m[lapg_head-1].sym), inputName, lapg_m[lapg_head-6].line); checkFqn(((String)lapg_m[lapg_head-4].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-6].line); 
				break;
			case 12:  // template_start ::= '${' Ltemplate qualified_id parametersopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), templatePackage, inputName, lapg_m[lapg_head-4].line); checkFqn(((String)lapg_m[lapg_head-2].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-4].line); 
				break;
			case 15:  // parameters ::= '(' identifier_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 16:  // identifier_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 17:  // identifier_list ::= identifier_list ',' identifier
				 ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 19:  // instructions ::= instructions instruction
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 20:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 21:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head-0].offset+1); 
				break;
			case 26:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-0].line); 
				break;
			case 27:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line), inputName, lapg_m[lapg_head-0].line); 
				break;
			case 28:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(inputName, lapg_m[lapg_head-0].line); 
				break;
			case 29:  // instruction ::= any
				 lapg_gg.sym = new TextNode(rawText(lapg_m[lapg_head-0].offset,lapg_m[lapg_head-0].endoffset),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 30:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 36:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),templatePackage,true,inputName, lapg_m[lapg_head-3].line); 
				break;
			case 39:  // sentence ::= Leval expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-2].line); 
				break;
			case 40:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-1].line); 
				break;
			case 41:  // comma_expr ::= ',' expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 43:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 44:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 47:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 48:  // control_instruction ::= control_start instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 49:  // control_instruction ::= control_start instructions else_node instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-3].sym)); applyElse(((CompoundNode)lapg_gg.sym),((ArrayList<Node>)lapg_m[lapg_head-1].sym),lapg_gg.offset,lapg_gg.endoffset,lapg_gg.line); 
				break;
			case 53:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-6].line); checkIsSpace(lapg_m[lapg_head-2].offset,lapg_m[lapg_head-2].endoffset, lapg_m[lapg_head-2].line); 
				break;
			case 54:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 55:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 56:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 58:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 59:  // control_sentence ::= Lforeach identifier Lin expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 60:  // control_sentence ::= Lfor identifier ':' expression ',' expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-5].line); 
				break;
			case 61:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 62:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 65:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 66:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName,lapg_m[lapg_head-2].line); 
				break;
			case 67:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 68:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 69:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 70:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(inputName, lapg_m[lapg_head-0].line); 
				break;
			case 71:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, inputName, lapg_m[lapg_head-0].line); 
				break;
			case 72:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 73:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 74:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-5].line); 
				break;
			case 75:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), inputName, lapg_m[lapg_head-7].line, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-5].sym),templatePackage,false,inputName, lapg_m[lapg_head-5].line); 
				break;
			case 77:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage,inputName, lapg_m[lapg_head-7].line); 
				break;
			case 78:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 80:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 81:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 82:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 83:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 84:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 85:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 87:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 88:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 90:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 91:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 92:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 94:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 95:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 97:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 98:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 99:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 100:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 102:  // equality_expression ::= equality_expression '==' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 103:  // equality_expression ::= equality_expression '!=' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 105:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 107:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 109:  // expression ::= conditional_or_expression '?' expression ':' expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), inputName, lapg_m[lapg_head-4].line); 
				break;
			case 110:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 111:  // expression_list ::= expression_list ',' expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 112:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, templatePackage, inputName, lapg_m[lapg_head-0].line);
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
