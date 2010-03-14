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
		-3, -1, 8, -11, -19, 4, 7, -1, 2, 32, 31, 29, 30, -1, -27, 23,
		28, 26, 27, -1, 14, -1, 10, -1, 3, -1, 6, -1, -41, 70, 72, -1,
		-1, 88, -1, -1, -1, -1, 74, -1, 87, 73, -1, -1, -1, -91, -1, -1,
		-1, -117, 82, 71, 92, -165, -207, -243, -275, -299, -319, 34, 22, -1, 45, -337,
		-1, -1, 5, -347, -373, -385, -1, 65, -1, 64, -1, 43, 91, 90, -393, -1,
		113, -437, -1, -1, 25, 24, 33, 61, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 67, -1, 51, -445, -1, 12,
		-1, -451, 21, -1, -459, 36, -485, -1, 41, 42, -1, -1, -493, -1, 84, -1,
		-1, 83, 69, -1, -499, -1, -1, 93, 94, 95, -549, -591, -633, -669, -705, -741,
		-777, -809, -841, -865, -1, -1, -1, -1, 19, -885, -1, 46, 15, -1, 75, -1,
		-1, 38, 39, 44, -1, 62, 55, -1, 85, -1, 114, 81, -891, -1, -917, -1,
		66, 53, -1, 52, -1, 18, -1, 50, 47, -1, -1, -1, 57, -1, -943, -1,
		-1, -1, 112, 20, -1, 63, -1, -1, 59, 58, 56, 86, -1, 77, -987, 79,
		13, -1, -1, -1, 60, 78, 80, -1, -1, -2, -2,
	};

    private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 11, -1, 29, -1, 27, 9, -1, -2,
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1,
		5, -1, 0, 115, -1, -2, 44, -1, 33, 68, 34, 68, 35, 68, 36, 68,
		37, 68, 38, 68, 39, 68, 42, 68, 43, 68, 45, 68, 46, 68, 47, 68,
		48, 68, 49, 68, 50, 68, 52, 68, 53, 68, 54, 68, 55, 68, 56, 68,
		57, 68, 58, 68, 59, 68, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 30, -1, 31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 43, 48,
		-1, -2, 42, -1, 46, -1, 53, -1, 33, 89, 34, 89, 35, 89, 36, 89,
		37, 89, 38, 89, 39, 89, 43, 89, 45, 89, 47, 89, 48, 89, 49, 89,
		50, 89, 52, 89, 54, 89, 55, 89, 56, 89, 57, 89, 58, 89, 59, 89,
		-1, -2, 37, -1, 38, -1, 39, -1, 33, 96, 34, 96, 35, 96, 36, 96,
		43, 96, 45, 96, 47, 96, 48, 96, 49, 96, 50, 96, 52, 96, 54, 96,
		55, 96, 56, 96, 57, 96, 58, 96, 59, 96, -1, -2, 35, -1, 36, -1,
		33, 99, 34, 99, 43, 99, 45, 99, 47, 99, 48, 99, 49, 99, 50, 99,
		52, 99, 54, 99, 55, 99, 56, 99, 57, 99, 58, 99, 59, 99, -1, -2,
		54, -1, 55, -1, 56, -1, 57, -1, 33, 104, 34, 104, 43, 104, 45, 104,
		47, 104, 48, 104, 49, 104, 50, 104, 52, 104, 58, 104, 59, 104, -1, -2,
		50, -1, 52, -1, 33, 107, 34, 107, 43, 107, 45, 107, 47, 107, 48, 107,
		49, 107, 58, 107, 59, 107, -1, -2, 48, -1, 33, 109, 34, 109, 43, 109,
		45, 109, 47, 109, 49, 109, 58, 109, 59, 109, -1, -2, 49, -1, 59, -1,
		33, 111, 34, 111, 43, 111, 45, 111, 47, 111, 58, 111, -1, -2, 44, -1,
		46, -1, 33, 11, 34, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 30, -1, 31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48,
		-1, -2, 44, -1, 46, -1, 17, 35, 33, 35, 34, 35, -1, -2, 47, -1,
		33, 40, 34, 40, -1, -2, 44, -1, 58, -1, 35, 68, 36, 68, 37, 68,
		38, 68, 39, 68, 42, 68, 43, 68, 46, 68, 47, 68, 48, 68, 49, 68,
		50, 68, 52, 68, 53, 68, 54, 68, 55, 68, 56, 68, 57, 68, 59, 68,
		-1, -2, 47, -1, 43, 49, 45, 49, -1, -2, 7, -1, 45, 16, -1, -2,
		44, -1, 46, -1, 51, 11, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 30, -1, 31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48,
		-1, -2, 17, -1, 33, 37, 34, 37, -1, -2, 1, -1, 5, 54, -1, -2,
		44, -1, 33, 76, 34, 76, 35, 76, 36, 76, 37, 76, 38, 76, 39, 76,
		42, 76, 43, 76, 45, 76, 46, 76, 47, 76, 48, 76, 49, 76, 50, 76,
		52, 76, 53, 76, 54, 76, 55, 76, 56, 76, 57, 76, 58, 76, 59, 76,
		-1, -2, 37, -1, 38, -1, 39, -1, 33, 97, 34, 97, 35, 97, 36, 97,
		43, 97, 45, 97, 47, 97, 48, 97, 49, 97, 50, 97, 52, 97, 54, 97,
		55, 97, 56, 97, 57, 97, 58, 97, 59, 97, -1, -2, 37, -1, 38, -1,
		39, -1, 33, 98, 34, 98, 35, 98, 36, 98, 43, 98, 45, 98, 47, 98,
		48, 98, 49, 98, 50, 98, 52, 98, 54, 98, 55, 98, 56, 98, 57, 98,
		58, 98, 59, 98, -1, -2, 35, -1, 36, -1, 33, 102, 34, 102, 43, 102,
		45, 102, 47, 102, 48, 102, 49, 102, 50, 102, 52, 102, 54, 102, 55, 102,
		56, 102, 57, 102, 58, 102, 59, 102, -1, -2, 35, -1, 36, -1, 33, 103,
		34, 103, 43, 103, 45, 103, 47, 103, 48, 103, 49, 103, 50, 103, 52, 103,
		54, 103, 55, 103, 56, 103, 57, 103, 58, 103, 59, 103, -1, -2, 35, -1,
		36, -1, 33, 100, 34, 100, 43, 100, 45, 100, 47, 100, 48, 100, 49, 100,
		50, 100, 52, 100, 54, 100, 55, 100, 56, 100, 57, 100, 58, 100, 59, 100,
		-1, -2, 35, -1, 36, -1, 33, 101, 34, 101, 43, 101, 45, 101, 47, 101,
		48, 101, 49, 101, 50, 101, 52, 101, 54, 101, 55, 101, 56, 101, 57, 101,
		58, 101, 59, 101, -1, -2, 54, -1, 55, -1, 56, -1, 57, -1, 33, 105,
		34, 105, 43, 105, 45, 105, 47, 105, 48, 105, 49, 105, 50, 105, 52, 105,
		58, 105, 59, 105, -1, -2, 54, -1, 55, -1, 56, -1, 57, -1, 33, 106,
		34, 106, 43, 106, 45, 106, 47, 106, 48, 106, 49, 106, 50, 106, 52, 106,
		58, 106, 59, 106, -1, -2, 50, -1, 52, -1, 33, 108, 34, 108, 43, 108,
		45, 108, 47, 108, 48, 108, 49, 108, 58, 108, 59, 108, -1, -2, 48, -1,
		33, 110, 34, 110, 43, 110, 45, 110, 47, 110, 49, 110, 58, 110, 59, 110,
		-1, -2, 47, -1, 45, 17, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 30, -1, 31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48,
		-1, -2, 7, -1, 8, -1, 9, -1, 16, -1, 26, -1, 30, -1, 31, -1,
		36, -1, 40, -1, 42, -1, 44, -1, 45, 48, -1, -2, 41, -1, 44, -1,
		35, 68, 36, 68, 37, 68, 38, 68, 39, 68, 42, 68, 45, 68, 46, 68,
		47, 68, 48, 68, 49, 68, 50, 68, 52, 68, 53, 68, 54, 68, 55, 68,
		56, 68, 57, 68, 59, 68, -1, -2, 7, -1, 8, -1, 9, -1, 16, -1,
		26, -1, 30, -1, 31, -1, 36, -1, 40, -1, 42, -1, 44, -1, 45, 48,
		-1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 2, 14, 23, 32, 41, 53, 56, 114, 161, 208, 213, 214, 216, 220, 221,
		226, 273, 279, 284, 289, 289, 294, 295, 295, 295, 295, 342, 343, 348, 349, 396,
		443, 448, 457, 464, 469, 521, 524, 527, 530, 577, 578, 626, 629, 686, 695, 700,
		705, 707, 708, 710, 711, 713, 714, 717, 720, 723, 726, 730, 731, 731, 731, 731,
		732, 733, 735, 737, 738, 740, 742, 743, 745, 749, 756, 765, 774, 779, 780, 784,
		785, 786, 795, 796, 805, 806, 808, 817, 822, 825, 872, 919, 920, 967, 1014, 1056,
		1096, 1132, 1166, 1199, 1231, 1263, 1269, 1270, 1271, 1272, 1274, 1275, 1276, 1277, 1278, 1284,
		1285,
	};

    private static final short lapg_sym_from[] = {
		215, 216, 0, 1, 4, 7, 14, 19, 27, 61, 107, 124, 151, 187, 1, 7,
		14, 19, 27, 61, 107, 151, 187, 1, 7, 14, 19, 27, 61, 107, 151, 187,
		1, 7, 14, 19, 27, 61, 107, 151, 187, 0, 1, 4, 7, 14, 19, 27,
		61, 107, 151, 167, 187, 61, 151, 187, 13, 21, 25, 31, 32, 34, 35, 36,
		37, 39, 42, 43, 44, 45, 46, 64, 67, 88, 89, 90, 91, 92, 93, 94,
		95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 109, 110, 116, 119, 122,
		123, 125, 127, 128, 133, 160, 172, 174, 175, 178, 180, 182, 185, 189, 198, 199,
		204, 206, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 67, 88, 91,
		92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 116, 119,
		122, 123, 125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204,
		206, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 67, 88, 91, 92,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 116, 119, 122,
		123, 125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206,
		13, 25, 105, 178, 199, 3, 186, 199, 25, 105, 178, 199, 105, 13, 25, 105,
		178, 199, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 67, 88, 91,
		92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 116, 119,
		122, 123, 125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204,
		206, 13, 25, 105, 118, 178, 199, 13, 25, 105, 178, 199, 13, 25, 105, 178,
		199, 13, 25, 105, 178, 199, 72, 13, 25, 32, 35, 37, 39, 42, 43, 44,
		45, 46, 67, 88, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		103, 104, 105, 116, 119, 122, 123, 125, 128, 133, 160, 172, 174, 175, 178, 182,
		185, 189, 198, 199, 204, 206, 23, 13, 25, 105, 178, 199, 3, 13, 25, 32,
		35, 37, 39, 42, 43, 44, 45, 46, 67, 88, 91, 92, 93, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 103, 104, 105, 116, 119, 122, 123, 125, 128, 133,
		160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206, 13, 25, 32, 35,
		37, 39, 42, 43, 44, 45, 46, 67, 88, 91, 92, 93, 94, 95, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 105, 116, 119, 122, 123, 125, 128, 133, 160,
		172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206, 13, 25, 105, 178, 199,
		47, 48, 65, 74, 112, 149, 150, 196, 209, 47, 48, 74, 112, 149, 150, 209,
		54, 140, 141, 142, 143, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46,
		54, 67, 88, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103,
		104, 105, 116, 119, 122, 123, 125, 128, 133, 140, 141, 142, 143, 160, 172, 174,
		175, 178, 182, 185, 189, 198, 199, 204, 206, 53, 138, 139, 53, 138, 139, 53,
		138, 139, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 67, 88, 91,
		92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 116, 119,
		122, 123, 125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204,
		206, 190, 13, 25, 32, 35, 37, 39, 42, 43, 44, 45, 46, 49, 67, 88,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 116,
		119, 122, 123, 125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199,
		204, 206, 79, 82, 131, 13, 25, 28, 32, 35, 37, 39, 42, 43, 44, 45,
		46, 63, 67, 68, 78, 88, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99,
		100, 101, 102, 103, 104, 105, 113, 116, 119, 122, 123, 125, 128, 132, 133, 134,
		160, 172, 174, 175, 178, 182, 185, 189, 190, 192, 198, 199, 204, 206, 83, 115,
		154, 159, 173, 191, 193, 210, 211, 49, 63, 68, 113, 134, 69, 79, 81, 153,
		164, 57, 147, 58, 56, 146, 157, 56, 146, 49, 55, 144, 145, 55, 144, 145,
		55, 144, 145, 55, 144, 145, 70, 78, 148, 169, 58, 0, 0, 0, 4, 0,
		4, 3, 0, 4, 63, 113, 109, 7, 27, 1, 7, 19, 107, 47, 48, 74,
		112, 149, 150, 209, 1, 7, 14, 19, 27, 61, 107, 151, 187, 1, 7, 14,
		19, 27, 61, 107, 151, 187, 13, 25, 105, 178, 199, 69, 21, 31, 64, 90,
		118, 68, 1, 7, 14, 19, 27, 61, 107, 151, 187, 61, 1, 7, 14, 19,
		27, 61, 107, 151, 187, 167, 167, 187, 1, 7, 14, 19, 27, 61, 107, 151,
		187, 13, 25, 105, 178, 199, 61, 151, 187, 13, 25, 32, 35, 37, 39, 42,
		43, 44, 45, 46, 67, 88, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 105, 116, 119, 122, 123, 125, 128, 133, 160, 172, 174, 175,
		178, 182, 185, 189, 198, 199, 204, 206, 13, 25, 32, 35, 37, 39, 42, 43,
		44, 45, 46, 67, 88, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101,
		102, 103, 104, 105, 116, 119, 122, 123, 125, 128, 133, 160, 172, 174, 175, 178,
		182, 185, 189, 198, 199, 204, 206, 45, 13, 25, 32, 35, 37, 39, 42, 43,
		44, 45, 46, 67, 88, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101,
		102, 103, 104, 105, 116, 119, 122, 123, 125, 128, 133, 160, 172, 174, 175, 178,
		182, 185, 189, 198, 199, 204, 206, 13, 25, 32, 35, 37, 39, 42, 43, 44,
		45, 46, 67, 88, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		103, 104, 105, 116, 119, 122, 123, 125, 128, 133, 160, 172, 174, 175, 178, 182,
		185, 189, 198, 199, 204, 206, 13, 25, 32, 35, 37, 39, 42, 45, 46, 67,
		88, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 116, 119, 122,
		123, 125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206,
		13, 25, 32, 35, 37, 39, 42, 45, 46, 67, 88, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 105, 116, 119, 122, 123, 125, 128, 133, 160, 172, 174, 175,
		178, 182, 185, 189, 198, 199, 204, 206, 13, 25, 32, 35, 37, 39, 42, 45,
		46, 67, 88, 100, 101, 102, 103, 104, 105, 116, 119, 122, 123, 125, 128, 133,
		160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206, 13, 25, 32, 35,
		37, 39, 42, 45, 46, 67, 88, 102, 103, 104, 105, 116, 119, 122, 123, 125,
		128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206, 13, 25,
		32, 35, 37, 39, 42, 45, 46, 67, 88, 103, 104, 105, 116, 119, 122, 123,
		125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206, 13,
		25, 32, 35, 37, 39, 42, 45, 46, 67, 88, 104, 105, 116, 119, 122, 123,
		125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206, 13,
		25, 32, 35, 37, 39, 42, 45, 46, 67, 88, 104, 105, 116, 119, 122, 123,
		125, 128, 133, 160, 172, 174, 175, 178, 182, 185, 189, 198, 199, 204, 206, 45,
		67, 116, 172, 174, 206, 1, 0, 3, 63, 113, 109, 68, 118, 69, 45, 67,
		116, 172, 174, 206, 124,
	};

    private static final short lapg_sym_to[] = {
		217, 218, 2, 9, 2, 9, 9, 9, 9, 9, 9, 166, 9, 9, 10, 10,
		10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 13, 3, 25, 13, 13, 25,
		105, 13, 178, 186, 199, 106, 106, 106, 28, 62, 28, 62, 28, 70, 28, 72,
		28, 28, 28, 28, 28, 78, 28, 62, 28, 28, 132, 62, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 152, 155, 28, 28, 28,
		28, 28, 169, 28, 28, 28, 190, 28, 28, 28, 195, 28, 28, 28, 28, 28,
		28, 28, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		31, 31, 31, 31, 31, 20, 198, 198, 65, 149, 149, 149, 150, 32, 32, 32,
		32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 34, 34, 34, 160, 34, 34, 35, 35, 35, 35, 35, 36, 36, 36, 36,
		36, 37, 37, 37, 37, 37, 123, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 64, 39, 39, 39, 39, 39, 21, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 42, 42, 42, 42, 42,
		84, 84, 114, 84, 84, 84, 84, 208, 84, 85, 85, 85, 85, 85, 85, 85,
		94, 94, 94, 94, 94, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		95, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 95, 95, 95, 95, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 91, 91, 91, 92, 92, 92, 93,
		93, 93, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 204, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 88, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 126, 129, 171, 46, 46, 67, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 109, 46, 116, 67, 46, 133, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 109, 46, 46, 46, 46, 46, 46, 172, 46, 174,
		46, 46, 46, 46, 46, 46, 46, 46, 67, 206, 46, 46, 46, 46, 130, 158,
		181, 183, 192, 205, 207, 213, 214, 89, 110, 110, 110, 110, 119, 127, 128, 180,
		185, 102, 102, 103, 100, 100, 182, 101, 101, 90, 96, 96, 96, 97, 97, 97,
		98, 98, 98, 99, 99, 99, 122, 125, 175, 189, 104, 215, 4, 5, 24, 6,
		6, 22, 7, 7, 111, 111, 153, 26, 66, 14, 27, 61, 151, 86, 87, 124,
		156, 176, 177, 212, 15, 15, 60, 15, 60, 60, 15, 60, 200, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 47, 47, 47, 47, 47, 120, 63, 68, 113, 134,
		161, 117, 17, 17, 17, 17, 17, 17, 17, 17, 17, 107, 18, 18, 18, 18,
		18, 18, 18, 18, 18, 187, 188, 201, 19, 19, 19, 19, 19, 19, 19, 19,
		19, 48, 48, 48, 48, 48, 108, 179, 202, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 79, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 52, 52, 52, 52, 52, 52, 52, 76, 77,
		52, 52, 52, 52, 135, 136, 137, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 138, 139, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 140, 141, 142, 143, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 144, 145, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 146, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 147, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
		58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 59,
		59, 69, 71, 73, 74, 75, 80, 83, 80, 131, 148, 59, 80, 163, 164, 165,
		168, 170, 173, 184, 80, 80, 194, 59, 196, 197, 203, 209, 59, 210, 80, 81,
		81, 81, 81, 81, 81, 216, 8, 23, 112, 157, 154, 118, 162, 121, 82, 115,
		159, 191, 193, 211, 167,
	};

    private static final short lapg_rlen[] = {
		0, 1, 1, 2, 1, 3, 2, 1, 1, 0, 1, 0, 1, 8, 1, 5,
		0, 1, 3, 1, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 3, 1, 0, 1, 0, 1, 4, 0, 1, 3, 2, 2, 1, 3, 2,
		0, 1, 3, 3, 5, 3, 0, 1, 7, 1, 2, 2, 4, 3, 4, 6,
		2, 2, 3, 1, 1, 3, 1, 1, 1, 1, 1, 4, 3, 6, 8, 6,
		8, 4, 1, 3, 3, 3, 5, 1, 1, 1, 2, 2, 1, 3, 3, 3,
		1, 3, 3, 1, 3, 3, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1,
		5, 1, 3, 1,
	};

    private static final short lapg_rlex[] = {
		103, 103, 63, 64, 64, 65, 65, 65, 65, 104, 104, 105, 105, 66, 67, 68,
		106, 106, 69, 70, 70, 71, 72, 72, 73, 73, 74, 74, 74, 74, 74, 74,
		74, 75, 76, 107, 107, 108, 108, 76, 109, 109, 76, 76, 77, 78, 78, 79,
		110, 110, 80, 81, 81, 82, 111, 111, 83, 84, 84, 84, 85, 86, 87, 87,
		87, 87, 88, 88, 89, 89, 89, 89, 89, 89, 89, 89, 89, 89, 89, 89,
		89, 89, 89, 90, 90, 91, 91, 92, 92, 93, 93, 93, 94, 94, 94, 94,
		95, 95, 95, 96, 96, 96, 96, 96, 97, 97, 97, 98, 98, 99, 99, 100,
		100, 101, 101, 102,
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
		public static final int LBRACKETMINUSRBRACKETRBRACE = 73;
		public static final int instruction = 74;
		public static final int simple_instruction = 75;
		public static final int sentence = 76;
		public static final int comma_expr = 77;
		public static final int qualified_id = 78;
		public static final int template_for_expr = 79;
		public static final int template_arguments = 80;
		public static final int control_instruction = 81;
		public static final int else_node = 82;
		public static final int switch_instruction = 83;
		public static final int case_list = 84;
		public static final int one_case = 85;
		public static final int control_start = 86;
		public static final int control_sentence = 87;
		public static final int control_end = 88;
		public static final int primary_expression = 89;
		public static final int complex_data = 90;
		public static final int map_entries = 91;
		public static final int bcon = 92;
		public static final int unary_expression = 93;
		public static final int mult_expression = 94;
		public static final int additive_expression = 95;
		public static final int relational_expression = 96;
		public static final int equality_expression = 97;
		public static final int conditional_and_expression = 98;
		public static final int conditional_or_expression = 99;
		public static final int expression = 100;
		public static final int expression_list = 101;
		public static final int body = 102;
		public static final int templatesopt = 103;
		public static final int cached_flagopt = 104;
		public static final int parametersopt = 105;
		public static final int identifier_listopt = 106;
		public static final int template_argumentsopt = 107;
		public static final int template_for_expropt = 108;
		public static final int comma_expropt = 109;
		public static final int expression_listopt = 110;
		public static final int anyopt = 111;
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

		while( lapg_m[lapg_head].state != 217+state ) {
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

		if( lapg_m[lapg_head].state != 217+state ) {
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
				 lapg_gg.sym = new QueryNode(((String)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-3].sym), templatePackage, ((ExpressionNode)lapg_m[lapg_head-1].sym), ((Boolean)lapg_m[lapg_head-6].sym) != null, inputName, lapg_m[lapg_head-7].line); checkFqn(((String)lapg_m[lapg_head-4].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-7].line); 
				break;
			case 14:  // cached_flag ::= Lcached
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 15:  // template_start ::= '${' Ltemplate qualified_id parametersopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), templatePackage, inputName, lapg_m[lapg_head-4].line); checkFqn(((String)lapg_m[lapg_head-2].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-4].line); 
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
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-0].line); 
				break;
			case 30:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line), inputName, lapg_m[lapg_head-0].line); 
				break;
			case 31:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(inputName, lapg_m[lapg_head-0].line); 
				break;
			case 32:  // instruction ::= any
				 lapg_gg.sym = new TextNode(rawText(lapg_m[lapg_head-0].offset,lapg_m[lapg_head-0].endoffset),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 33:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 39:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),templatePackage,true,inputName, lapg_m[lapg_head-3].line); 
				break;
			case 42:  // sentence ::= Leval expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-2].line); 
				break;
			case 43:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-1].line); 
				break;
			case 44:  // comma_expr ::= ',' expression
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
			case 51:  // control_instruction ::= control_start instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 52:  // control_instruction ::= control_start instructions else_node instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-3].sym)); applyElse(((CompoundNode)lapg_gg.sym),((ArrayList<Node>)lapg_m[lapg_head-1].sym),lapg_gg.offset,lapg_gg.endoffset,lapg_gg.line); 
				break;
			case 56:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-6].line); checkIsSpace(lapg_m[lapg_head-2].offset,lapg_m[lapg_head-2].endoffset, lapg_m[lapg_head-2].line); 
				break;
			case 57:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 58:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 59:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 60:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 61:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 62:  // control_sentence ::= Lforeach identifier Lin expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 63:  // control_sentence ::= Lfor identifier ':' expression ',' expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-5].line); 
				break;
			case 64:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 65:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 68:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 69:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName,lapg_m[lapg_head-2].line); 
				break;
			case 70:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 71:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 72:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 73:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(inputName, lapg_m[lapg_head-0].line); 
				break;
			case 74:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, inputName, lapg_m[lapg_head-0].line); 
				break;
			case 75:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 76:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 77:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-5].line); 
				break;
			case 78:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), inputName, lapg_m[lapg_head-7].line, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-5].sym),templatePackage,false,inputName, lapg_m[lapg_head-5].line); 
				break;
			case 80:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage,inputName, lapg_m[lapg_head-7].line); 
				break;
			case 81:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 83:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 84:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 85:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 86:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 87:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 88:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 90:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 91:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 93:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 94:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 95:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 97:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 98:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 100:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 101:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 102:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 103:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 105:  // equality_expression ::= equality_expression '==' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 106:  // equality_expression ::= equality_expression '!=' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 108:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 110:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 112:  // expression ::= conditional_or_expression '?' expression ':' expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), inputName, lapg_m[lapg_head-4].line); 
				break;
			case 113:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 114:  // expression_list ::= expression_list ',' expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 115:  // body ::= instructions
				
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
