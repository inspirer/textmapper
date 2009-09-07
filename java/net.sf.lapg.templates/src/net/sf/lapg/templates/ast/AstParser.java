package net.sf.lapg.templates.ast;

import java.io.CharArrayReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.ast.AstLexer.LapgSymbol;
import net.sf.lapg.templates.ast.AstLexer.Lexems;


public class AstParser implements AstLexer.ErrorReporter {
	
	private ArrayList<ITemplate> templates;
	private String templatePackage;
	
	public AstParser() {
	}
	
	private static final boolean DEBUG_SYNTAX = false;
	
	private String inputName;
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
	
	private void checkIsSpace(int start, int end) {
		String val = rawText(start,end).trim();
		if( val.length() > 0 )
			error("Unknown text ignored: `"+val+"`");
	}
	
	private void applyElse(CompoundNode node, ArrayList<Node> instructions) {
		if( node instanceof IfNode ) {
			((IfNode)node).setElseInstructions(instructions);
		} else {
			error("Unknown else node, instructions skipped");
		}
	}
	
	private ExpressionNode createCollectionProcessor(ExpressionNode forExpr, String instruction, String varName, ExpressionNode foreachExpr, String input, int line) {
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
			error("unknown collection processing instruction: " + instruction);
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
	
	public void error( String s ) {
		System.err.println(inputName + ":" + s);
	}
	
	public boolean parse(String s, String templatePackage, String inputName) {
		this.templatePackage = templatePackage;
		this.inputName = inputName;
		templates = new ArrayList<ITemplate>();
		try {
			buff = s.toCharArray();
			AstLexer lexer = new AstLexer(new CharArrayReader(buff), this);
			return parse(lexer);
		} catch( IOException ex ) {
			return false;
		}
	}
	
	public ITemplate[] getResult() {
		return templates.toArray(new ITemplate[templates.size()]);
	}
    private static final int lapg_action[] = {
		-3, 7, -1, 2, -11, 4, -1, -1, 3, 30, 29, 27, 28, -1, -1, 6,
		21, 24, 25, 26, -1, -19, -29, 68, 70, -1, -1, -1, 86, -1, -1, -1,
		-1, 72, -1, 85, 71, -1, -1, -1, -79, -1, -1, 32, -1, -105, 69, 80,
		90, -153, -195, -231, -263, -287, -307, 5, 20, -1, -1, -325, -331, 8, -339, 43,
		-365, 19, -377, -1, 63, -1, 62, -1, 41, 89, 88, -385, 111, -1, -429, -1,
		-1, 23, 22, 31, 59, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, 65, 49, -1, 17, -1, -437, -1, -1, 10,
		-1, -443, -1, -469, 33, -1, 40, 38, -1, -1, -477, -1, 81, -1, 82, -1,
		67, -1, -483, -1, -1, 91, 92, 93, -533, -575, -617, -653, -689, -725, -761, -793,
		-825, -849, -1, -1, -1, -1, 16, -1, -869, 12, 73, -1, 44, -1, 37, 35,
		42, -1, 60, 52, -1, 83, 112, -1, 79, -877, -1, -903, -1, 64, 51, -1,
		50, 18, 48, 45, -1, -1, -1, 55, -1, -929, -1, -1, -1, 110, 61, -1,
		-1, 57, 54, 56, 84, -1, 75, -973, 77, -1, -1, -1, 58, 76, 78, -1,
		-2,
	};

    private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 5, -1, 0, 0, -1, -2,
		42, -1, 31, 9, 32, 9, 55, 9, -1, -2, 42, -1, 31, 66, 32, 66,
		33, 66, 34, 66, 35, 66, 36, 66, 37, 66, 40, 66, 41, 66, 43, 66,
		44, 66, 45, 66, 46, 66, 47, 66, 48, 66, 49, 66, 50, 66, 51, 66,
		52, 66, 53, 66, 54, 66, 55, 66, 56, 66, -1, -2, 7, -1, 8, -1,
		9, -1, 15, -1, 25, -1, 28, -1, 29, -1, 34, -1, 38, -1, 40, -1,
		42, -1, 41, 47, -1, -2, 40, -1, 44, -1, 50, -1, 31, 87, 32, 87,
		33, 87, 34, 87, 35, 87, 36, 87, 37, 87, 41, 87, 43, 87, 45, 87,
		46, 87, 47, 87, 48, 87, 49, 87, 51, 87, 52, 87, 53, 87, 54, 87,
		55, 87, 56, 87, -1, -2, 35, -1, 36, -1, 37, -1, 31, 94, 32, 94,
		33, 94, 34, 94, 41, 94, 43, 94, 45, 94, 46, 94, 47, 94, 48, 94,
		49, 94, 51, 94, 52, 94, 53, 94, 54, 94, 55, 94, 56, 94, -1, -2,
		33, -1, 34, -1, 31, 97, 32, 97, 41, 97, 43, 97, 45, 97, 46, 97,
		47, 97, 48, 97, 49, 97, 51, 97, 52, 97, 53, 97, 54, 97, 55, 97,
		56, 97, -1, -2, 51, -1, 52, -1, 53, -1, 54, -1, 31, 102, 32, 102,
		41, 102, 43, 102, 45, 102, 46, 102, 47, 102, 48, 102, 49, 102, 55, 102,
		56, 102, -1, -2, 48, -1, 49, -1, 31, 105, 32, 105, 41, 105, 43, 105,
		45, 105, 46, 105, 47, 105, 55, 105, 56, 105, -1, -2, 46, -1, 31, 107,
		32, 107, 41, 107, 43, 107, 45, 107, 47, 107, 55, 107, 56, 107, -1, -2,
		47, -1, 56, -1, 31, 109, 32, 109, 41, 109, 43, 109, 45, 109, 55, 109,
		-1, -2, 7, -1, 43, 15, -1, -2, 55, -1, 31, 11, 32, 11, -1, -2,
		7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 28, -1, 29, -1, 34, -1,
		38, -1, 40, -1, 42, -1, 43, 47, -1, -2, 42, -1, 44, -1, 16, 34,
		31, 34, 32, 34, -1, -2, 45, -1, 31, 39, 32, 39, -1, -2, 42, -1,
		55, -1, 33, 66, 34, 66, 35, 66, 36, 66, 37, 66, 40, 66, 41, 66,
		44, 66, 45, 66, 46, 66, 47, 66, 48, 66, 49, 66, 50, 66, 51, 66,
		52, 66, 53, 66, 54, 66, 56, 66, -1, -2, 45, -1, 41, 46, 43, 46,
		-1, -2, 45, -1, 43, 14, -1, -2, 7, -1, 8, -1, 9, -1, 15, -1,
		25, -1, 28, -1, 29, -1, 34, -1, 38, -1, 40, -1, 42, -1, 43, 47,
		-1, -2, 16, -1, 31, 36, 32, 36, -1, -2, 1, -1, 5, 53, -1, -2,
		42, -1, 31, 74, 32, 74, 33, 74, 34, 74, 35, 74, 36, 74, 37, 74,
		40, 74, 41, 74, 43, 74, 44, 74, 45, 74, 46, 74, 47, 74, 48, 74,
		49, 74, 50, 74, 51, 74, 52, 74, 53, 74, 54, 74, 55, 74, 56, 74,
		-1, -2, 35, -1, 36, -1, 37, -1, 31, 95, 32, 95, 33, 95, 34, 95,
		41, 95, 43, 95, 45, 95, 46, 95, 47, 95, 48, 95, 49, 95, 51, 95,
		52, 95, 53, 95, 54, 95, 55, 95, 56, 95, -1, -2, 35, -1, 36, -1,
		37, -1, 31, 96, 32, 96, 33, 96, 34, 96, 41, 96, 43, 96, 45, 96,
		46, 96, 47, 96, 48, 96, 49, 96, 51, 96, 52, 96, 53, 96, 54, 96,
		55, 96, 56, 96, -1, -2, 33, -1, 34, -1, 31, 100, 32, 100, 41, 100,
		43, 100, 45, 100, 46, 100, 47, 100, 48, 100, 49, 100, 51, 100, 52, 100,
		53, 100, 54, 100, 55, 100, 56, 100, -1, -2, 33, -1, 34, -1, 31, 101,
		32, 101, 41, 101, 43, 101, 45, 101, 46, 101, 47, 101, 48, 101, 49, 101,
		51, 101, 52, 101, 53, 101, 54, 101, 55, 101, 56, 101, -1, -2, 33, -1,
		34, -1, 31, 98, 32, 98, 41, 98, 43, 98, 45, 98, 46, 98, 47, 98,
		48, 98, 49, 98, 51, 98, 52, 98, 53, 98, 54, 98, 55, 98, 56, 98,
		-1, -2, 33, -1, 34, -1, 31, 99, 32, 99, 41, 99, 43, 99, 45, 99,
		46, 99, 47, 99, 48, 99, 49, 99, 51, 99, 52, 99, 53, 99, 54, 99,
		55, 99, 56, 99, -1, -2, 51, -1, 52, -1, 53, -1, 54, -1, 31, 103,
		32, 103, 41, 103, 43, 103, 45, 103, 46, 103, 47, 103, 48, 103, 49, 103,
		55, 103, 56, 103, -1, -2, 51, -1, 52, -1, 53, -1, 54, -1, 31, 104,
		32, 104, 41, 104, 43, 104, 45, 104, 46, 104, 47, 104, 48, 104, 49, 104,
		55, 104, 56, 104, -1, -2, 48, -1, 49, -1, 31, 106, 32, 106, 41, 106,
		43, 106, 45, 106, 46, 106, 47, 106, 55, 106, 56, 106, -1, -2, 46, -1,
		31, 108, 32, 108, 41, 108, 43, 108, 45, 108, 47, 108, 55, 108, 56, 108,
		-1, -2, 44, -1, 31, 13, 32, 13, -1, -2, 7, -1, 8, -1, 9, -1,
		15, -1, 25, -1, 28, -1, 29, -1, 34, -1, 38, -1, 40, -1, 42, -1,
		43, 47, -1, -2, 7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 28, -1,
		29, -1, 34, -1, 38, -1, 40, -1, 42, -1, 43, 47, -1, -2, 39, -1,
		42, -1, 33, 66, 34, 66, 35, 66, 36, 66, 37, 66, 40, 66, 43, 66,
		44, 66, 45, 66, 46, 66, 47, 66, 48, 66, 49, 66, 50, 66, 51, 66,
		52, 66, 53, 66, 54, 66, 56, 66, -1, -2, 7, -1, 8, -1, 9, -1,
		15, -1, 25, -1, 28, -1, 29, -1, 34, -1, 38, -1, 40, -1, 42, -1,
		43, 47, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 11, 18, 25, 32, 42, 45, 102, 148, 194, 199, 201, 205, 206, 211,
		257, 263, 268, 273, 273, 278, 279, 279, 279, 279, 325, 330, 331, 377, 423, 428,
		436, 443, 448, 499, 502, 505, 508, 554, 555, 602, 605, 660, 669, 673, 678, 680,
		681, 683, 685, 686, 689, 692, 695, 698, 703, 704, 704, 704, 704, 705, 706, 707,
		709, 711, 714, 716, 717, 718, 719, 720, 727, 730, 731, 732, 739, 746, 753, 760,
		765, 796, 797, 798, 799, 800, 801, 802, 808, 814, 821, 824, 825, 826, 827, 829,
		834, 880, 926, 972, 973, 1019, 1060, 1099, 1134, 1167, 1199, 1230,
	};

    private static final short lapg_sym_from[] = {
		207, 0, 4, 6, 14, 20, 58, 105, 122, 149, 182, 6, 14, 20, 58, 105,
		149, 182, 6, 14, 20, 58, 105, 149, 182, 6, 14, 20, 58, 105, 149, 182,
		0, 4, 6, 14, 20, 58, 105, 149, 164, 182, 58, 149, 182, 7, 13, 25,
		27, 29, 30, 31, 32, 34, 37, 38, 39, 40, 41, 57, 59, 62, 85, 86,
		87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		109, 113, 114, 117, 120, 121, 123, 125, 127, 131, 151, 157, 169, 171, 172, 175,
		180, 184, 191, 192, 197, 199, 13, 27, 30, 32, 34, 37, 38, 39, 40, 41,
		57, 62, 85, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
		101, 102, 113, 117, 120, 121, 123, 125, 131, 157, 169, 171, 172, 175, 180, 184,
		191, 192, 197, 199, 13, 27, 30, 32, 34, 37, 38, 39, 40, 41, 57, 62,
		85, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		113, 117, 120, 121, 123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192,
		197, 199, 13, 57, 102, 175, 192, 181, 192, 13, 102, 175, 192, 102, 13, 57,
		102, 175, 192, 13, 27, 30, 32, 34, 37, 38, 39, 40, 41, 57, 62, 85,
		88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 113,
		117, 120, 121, 123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197,
		199, 13, 57, 102, 115, 175, 192, 13, 57, 102, 175, 192, 13, 57, 102, 175,
		192, 13, 57, 102, 175, 192, 69, 13, 27, 30, 32, 34, 37, 38, 39, 40,
		41, 57, 62, 85, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99,
		100, 101, 102, 113, 117, 120, 121, 123, 125, 131, 157, 169, 171, 172, 175, 180,
		184, 191, 192, 197, 199, 13, 57, 102, 175, 192, 2, 13, 27, 30, 32, 34,
		37, 38, 39, 40, 41, 57, 62, 85, 88, 89, 90, 91, 92, 93, 94, 95,
		96, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121, 123, 125, 131, 157, 169,
		171, 172, 175, 180, 184, 191, 192, 197, 199, 13, 27, 30, 32, 34, 37, 38,
		39, 40, 41, 57, 62, 85, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97,
		98, 99, 100, 101, 102, 113, 117, 120, 121, 123, 125, 131, 157, 169, 171, 172,
		175, 180, 184, 191, 192, 197, 199, 13, 57, 102, 175, 192, 26, 42, 44, 71,
		110, 147, 148, 201, 42, 44, 71, 110, 147, 148, 201, 50, 138, 139, 140, 141,
		13, 27, 30, 32, 34, 37, 38, 39, 40, 41, 50, 57, 62, 85, 88, 89,
		90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 113, 117, 120,
		121, 123, 125, 131, 138, 139, 140, 141, 157, 169, 171, 172, 175, 180, 184, 191,
		192, 197, 199, 49, 136, 137, 49, 136, 137, 49, 136, 137, 13, 27, 30, 32,
		34, 37, 38, 39, 40, 41, 57, 62, 85, 88, 89, 90, 91, 92, 93, 94,
		95, 96, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121, 123, 125, 131, 157,
		169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 185, 13, 27, 30, 32, 34,
		37, 38, 39, 40, 41, 45, 57, 62, 85, 88, 89, 90, 91, 92, 93, 94,
		95, 96, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121, 123, 125, 131, 157,
		169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 77, 79, 129, 13, 21, 22,
		27, 30, 32, 34, 37, 38, 39, 40, 41, 57, 62, 64, 75, 85, 87, 88,
		89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 113, 117,
		120, 121, 123, 125, 130, 131, 132, 157, 169, 171, 172, 175, 180, 184, 185, 187,
		191, 192, 197, 199, 80, 107, 112, 155, 170, 186, 188, 202, 203, 45, 64, 132,
		152, 66, 78, 79, 108, 161, 53, 145, 54, 52, 144, 52, 144, 45, 51, 142,
		143, 51, 142, 143, 51, 142, 143, 51, 142, 143, 60, 67, 75, 146, 167, 54,
		0, 0, 0, 0, 4, 0, 4, 6, 20, 105, 6, 14, 21, 21, 60, 60,
		42, 44, 71, 110, 147, 148, 201, 25, 87, 109, 59, 59, 6, 14, 20, 58,
		105, 149, 182, 6, 14, 20, 58, 105, 149, 182, 6, 14, 20, 58, 105, 149,
		182, 6, 14, 20, 58, 105, 149, 182, 13, 57, 102, 175, 192, 13, 27, 30,
		32, 34, 37, 40, 41, 57, 62, 85, 101, 102, 113, 117, 120, 121, 123, 125,
		131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 64, 64, 115, 115,
		66, 66, 40, 62, 113, 169, 171, 199, 40, 62, 113, 169, 171, 199, 6, 14,
		20, 58, 105, 149, 182, 58, 149, 182, 58, 122, 164, 164, 182, 13, 57, 102,
		175, 192, 13, 27, 30, 32, 34, 37, 38, 39, 40, 41, 57, 62, 85, 88,
		89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 113, 117,
		120, 121, 123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199,
		13, 27, 30, 32, 34, 37, 38, 39, 40, 41, 57, 62, 85, 88, 89, 90,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121,
		123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 13, 27,
		30, 32, 34, 37, 38, 39, 40, 41, 57, 62, 85, 88, 89, 90, 91, 92,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121, 123, 125,
		131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 40, 13, 27, 30,
		32, 34, 37, 38, 39, 40, 41, 57, 62, 85, 88, 89, 90, 91, 92, 93,
		94, 95, 96, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121, 123, 125, 131,
		157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 13, 27, 30, 32, 34,
		37, 40, 41, 57, 62, 85, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
		101, 102, 113, 117, 120, 121, 123, 125, 131, 157, 169, 171, 172, 175, 180, 184,
		191, 192, 197, 199, 13, 27, 30, 32, 34, 37, 40, 41, 57, 62, 85, 93,
		94, 95, 96, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121, 123, 125, 131,
		157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 13, 27, 30, 32, 34,
		37, 40, 41, 57, 62, 85, 97, 98, 99, 100, 101, 102, 113, 117, 120, 121,
		123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 13, 27,
		30, 32, 34, 37, 40, 41, 57, 62, 85, 99, 100, 101, 102, 113, 117, 120,
		121, 123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 13,
		27, 30, 32, 34, 37, 40, 41, 57, 62, 85, 100, 101, 102, 113, 117, 120,
		121, 123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199, 13,
		27, 30, 32, 34, 37, 40, 41, 57, 62, 85, 101, 102, 113, 117, 120, 121,
		123, 125, 131, 157, 169, 171, 172, 175, 180, 184, 191, 192, 197, 199,
	};

    private static final short lapg_sym_to[] = {
		208, 1, 1, 9, 9, 9, 9, 9, 163, 9, 9, 10, 10, 10, 10, 10,
		10, 10, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12,
		2, 2, 13, 13, 57, 102, 57, 175, 181, 192, 103, 103, 103, 21, 22, 63,
		22, 67, 22, 69, 22, 22, 22, 22, 22, 75, 22, 22, 106, 22, 22, 130,
		63, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
		63, 22, 156, 22, 22, 22, 22, 22, 167, 22, 177, 22, 185, 22, 22, 22,
		22, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
		23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
		23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
		23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
		24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
		24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
		24, 24, 25, 25, 25, 25, 25, 191, 191, 26, 147, 147, 147, 148, 27, 27,
		27, 27, 27, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 29, 29, 29, 157, 29, 29, 30, 30, 30, 30, 30, 31, 31, 31, 31,
		31, 32, 32, 32, 32, 32, 121, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 34, 34, 34, 34, 34, 7, 35, 35, 35, 35, 35,
		35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35,
		35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35,
		35, 35, 35, 35, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 37, 37, 37, 37, 37, 65, 81, 81, 81,
		81, 81, 81, 81, 82, 82, 82, 82, 82, 82, 82, 91, 91, 91, 91, 91,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 92, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 92, 92, 92, 92, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 88, 88, 88, 89, 89, 89, 90, 90, 90, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 197, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 85, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 124, 126, 168, 41, 59, 62,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 113, 62, 41, 131, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 169, 41, 171, 41, 41, 41, 41, 41, 41, 41, 62, 199,
		41, 41, 41, 41, 128, 150, 154, 178, 187, 198, 200, 205, 206, 86, 114, 114,
		114, 117, 125, 127, 151, 180, 99, 99, 100, 97, 97, 98, 98, 87, 93, 93,
		93, 94, 94, 94, 95, 95, 95, 96, 96, 96, 109, 120, 123, 172, 184, 101,
		207, 3, 4, 5, 8, 6, 6, 14, 58, 149, 15, 55, 60, 61, 110, 111,
		83, 84, 122, 153, 173, 174, 204, 64, 132, 152, 107, 108, 16, 56, 16, 56,
		16, 56, 193, 17, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18,
		18, 19, 19, 19, 19, 19, 19, 19, 42, 42, 42, 42, 42, 43, 66, 68,
		70, 71, 72, 76, 80, 43, 76, 129, 146, 43, 76, 160, 161, 162, 165, 166,
		170, 179, 76, 76, 189, 43, 190, 196, 201, 43, 202, 76, 115, 116, 158, 159,
		118, 119, 77, 112, 155, 186, 188, 203, 78, 78, 78, 78, 78, 78, 20, 20,
		20, 20, 20, 20, 20, 104, 176, 194, 105, 164, 182, 183, 195, 44, 44, 44,
		44, 44, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 79, 48, 48, 48,
		48, 48, 48, 73, 74, 48, 48, 48, 48, 48, 133, 134, 135, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 136, 137, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 138,
		139, 140, 141, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 142, 143, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 144, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 145, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
	};

    private static final short lapg_rlen[] = {
		1, 0, 1, 2, 1, 3, 2, 1, 1, 0, 1, 0, 6, 2, 1, 0,
		3, 1, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3,
		1, 1, 0, 1, 0, 4, 1, 0, 3, 2, 2, 1, 3, 2, 1, 0,
		3, 3, 5, 3, 1, 0, 7, 1, 2, 2, 4, 3, 4, 6, 2, 2,
		3, 1, 1, 3, 1, 1, 1, 1, 1, 4, 3, 6, 8, 6, 8, 4,
		1, 3, 3, 3, 5, 1, 1, 1, 2, 2, 1, 3, 3, 3, 1, 3,
		3, 1, 3, 3, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1, 5, 1,
		3,
	};

    private static final short lapg_rlex[] = {
		61, 61, 60, 62, 62, 63, 63, 63, 67, 67, 69, 69, 64, 70, 73, 73,
		68, 74, 74, 66, 65, 65, 71, 71, 75, 75, 75, 75, 75, 75, 75, 78,
		79, 81, 81, 83, 83, 79, 85, 85, 79, 79, 86, 72, 72, 84, 87, 87,
		82, 76, 76, 91, 92, 92, 77, 93, 93, 93, 94, 89, 95, 95, 95, 95,
		90, 90, 96, 96, 96, 96, 96, 96, 96, 96, 96, 96, 96, 96, 96, 96,
		96, 98, 98, 99, 99, 97, 97, 100, 100, 100, 101, 101, 101, 101, 102, 102,
		102, 103, 103, 103, 103, 103, 104, 104, 104, 105, 105, 106, 106, 80, 80, 88,
		88,
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
		"templatesopt",
		"templates",
		"template_declaration_or_space",
		"template_start",
		"instructions",
		"template_end",
		"template_parametersopt",
		"template_parameters",
		"template_overridesopt",
		"template_overrides",
		"'[-]}'",
		"template_id",
		"identifier_listopt",
		"identifier_list",
		"instruction",
		"control_instruction",
		"switch_instruction",
		"simple_instruction",
		"sentence",
		"expression",
		"template_argumentsopt",
		"template_arguments",
		"template_for_expropt",
		"template_for_expr",
		"comma_expropt",
		"comma_expr",
		"expression_listopt",
		"expression_list",
		"control_start",
		"control_end",
		"else_node",
		"anyopt",
		"case_list",
		"one_case",
		"control_sentence",
		"primary_expression",
		"bcon",
		"complex_data",
		"map_entries",
		"unary_expression",
		"mult_expression",
		"additive_expression",
		"relational_expression",
		"equality_expression",
		"conditional_and_expression",
		"conditional_or_expression",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 60;
		public static final int templatesopt = 61;
		public static final int templates = 62;
		public static final int template_declaration_or_space = 63;
		public static final int template_start = 64;
		public static final int instructions = 65;
		public static final int template_end = 66;
		public static final int template_parametersopt = 67;
		public static final int template_parameters = 68;
		public static final int template_overridesopt = 69;
		public static final int template_overrides = 70;
		public static final int LBRACKETMINUSRBRACKETRBRACE = 71;
		public static final int template_id = 72;
		public static final int identifier_listopt = 73;
		public static final int identifier_list = 74;
		public static final int instruction = 75;
		public static final int control_instruction = 76;
		public static final int switch_instruction = 77;
		public static final int simple_instruction = 78;
		public static final int sentence = 79;
		public static final int expression = 80;
		public static final int template_argumentsopt = 81;
		public static final int template_arguments = 82;
		public static final int template_for_expropt = 83;
		public static final int template_for_expr = 84;
		public static final int comma_expropt = 85;
		public static final int comma_expr = 86;
		public static final int expression_listopt = 87;
		public static final int expression_list = 88;
		public static final int control_start = 89;
		public static final int control_end = 90;
		public static final int else_node = 91;
		public static final int anyopt = 92;
		public static final int case_list = 93;
		public static final int one_case = 94;
		public static final int control_sentence = 95;
		public static final int primary_expression = 96;
		public static final int bcon = 97;
		public static final int complex_data = 98;
		public static final int map_entries = 99;
		public static final int unary_expression = 100;
		public static final int mult_expression = 101;
		public static final int additive_expression = 102;
		public static final int relational_expression = 103;
		public static final int equality_expression = 104;
		public static final int conditional_and_expression = 105;
		public static final int conditional_or_expression = 106;
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

	public boolean parse(AstLexer lexer) throws IOException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next(); 

		while( lapg_m[lapg_head].state != 208 ) {
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

		if( lapg_m[lapg_head].state != 208 ) {
			error( MessageFormat.format( "syntax error before line {0}", lapg_n.pos.line ) );
			return false;
		};
		return true;
	}

	private void shift(AstLexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
		if( DEBUG_SYNTAX ) {
			System.out.println( MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current() ) );
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
		lapg_gg.pos = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]].pos:lapg_n.pos;
		lapg_gg.endpos = (lapg_rlen[rule]!=0)?lapg_m[lapg_head].endpos:lapg_n.pos;
		switch( rule ) {
			case 5:  // template_declaration_or_space ::= template_start instructions template_end
				 ((TemplateNode)lapg_m[lapg_head-2].sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); templates.add(((TemplateNode)lapg_m[lapg_head-2].sym)); 
				break;
			case 6:  // template_declaration_or_space ::= template_start template_end
				 templates.add(((TemplateNode)lapg_m[lapg_head-1].sym)); 
				break;
			case 12:  // template_start ::= '${' Ltemplate identifier template_parametersopt template_overridesopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-2].sym), templatePackage, ((String)lapg_m[lapg_head-1].sym), inputName, lapg_m[lapg_head-5].pos.line); 
				break;
			case 13:  // template_overrides ::= ':' template_id
				 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 16:  // template_parameters ::= '(' identifier_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 17:  // identifier_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 18:  // identifier_list ::= identifier_list ',' identifier
				 ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 20:  // instructions ::= instructions instruction
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 21:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 22:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head-0].pos.offset+1); 
				break;
			case 27:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-0].pos.line); 
				break;
			case 28:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line), inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 29:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 30:  // instruction ::= any
				 lapg_gg.sym = new TextNode(rawText(lapg_m[lapg_head-0].pos.offset,lapg_m[lapg_head-0].endpos.offset),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 31:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 37:  // sentence ::= Lcall template_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),templatePackage,true,inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 40:  // sentence ::= Leval expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-2].pos.line); 
				break;
			case 41:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-1].pos.line); 
				break;
			case 42:  // comma_expr ::= ',' expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 44:  // template_id ::= template_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 45:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 48:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 49:  // control_instruction ::= control_start instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 50:  // control_instruction ::= control_start instructions else_node instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-3].sym)); applyElse(((CompoundNode)lapg_gg.sym),((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 54:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-6].pos.line); checkIsSpace(lapg_m[lapg_head-2].pos.offset,lapg_m[lapg_head-2].endpos.offset); 
				break;
			case 55:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 56:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 58:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 59:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 60:  // control_sentence ::= Lforeach identifier Lin expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 61:  // control_sentence ::= Lfor identifier ':' expression ',' expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-5].pos.line); 
				break;
			case 62:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].pos.line); 
				break;
			case 63:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].pos.line); 
				break;
			case 66:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 67:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-1].sym); 
				break;
			case 68:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 69:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 70:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 71:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 72:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 73:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 74:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 75:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-5].pos.line); 
				break;
			case 76:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), inputName, lapg_m[lapg_head-7].pos.line); 
				break;
			case 77:  // primary_expression ::= primary_expression '->' template_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-5].sym),templatePackage,false,inputName, lapg_m[lapg_head-5].pos.line); 
				break;
			case 78:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage,inputName, lapg_m[lapg_head-7].pos.line); 
				break;
			case 79:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 81:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 82:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 83:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 84:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 85:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 86:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 88:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].pos.line); 
				break;
			case 89:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].pos.line); 
				break;
			case 91:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 92:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 93:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 95:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 96:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 98:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 99:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 100:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 101:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 103:  // equality_expression ::= equality_expression '==' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 104:  // equality_expression ::= equality_expression '!=' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 106:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 108:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 110:  // expression ::= conditional_or_expression '?' expression ':' expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), inputName, lapg_m[lapg_head-4].pos.line); 
				break;
			case 111:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 112:  // expression_list ::= expression_list ',' expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
