package net.sf.lapg.templates.ast;

import java.io.IOException;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;
import net.sf.lapg.templates.ast.AstLexer.LapgSymbol;

import java.io.CharArrayReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.lapg.templates.api.ITemplate;
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
	private ArrayList<ITemplate> templates;
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
		templates = new ArrayList<ITemplate>();
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
		templates = new ArrayList<ITemplate>();
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
	
	public ITemplate[] getResult() {
		return templates.toArray(new ITemplate[templates.size()]);
	}
    private static final int lapg_action[] = {
		-3, -1, 7, -1, -11, 4, -1, 2, 27, 26, 24, 25, -1, -19, 18, 23,
		21, 22, -1, -1, 3, -1, 6, -1, -33, 65, 67, -1, -1, 83, -1, -1,
		-1, -1, 69, -1, 82, 68, -1, -1, -1, -83, -1, -1, -1, -109, 77, 66,
		87, -157, -199, -235, -267, -291, -311, 29, 17, -1, 40, -329, -1, 5, -339, -365,
		-377, -1, 60, -1, 59, -1, 38, 86, 85, -385, -1, 108, -429, -1, -1, 20,
		19, 28, 56, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, 62, -1, 46, -437, -1, 9, -1, 16, -1, -443, 31,
		-469, -1, 36, 37, -1, -1, -477, -1, 79, -1, -1, 78, 64, -1, -483, -1,
		-1, 88, 89, 90, -533, -575, -617, -653, -689, -725, -761, -793, -825, -849, -1, -1,
		-1, -1, 14, -869, -1, 41, 10, 70, -1, -1, 33, 34, 39, -1, 57, 50,
		-1, 80, -1, 109, 76, -875, -1, -901, -1, 61, 48, -1, 47, -1, 13, 45,
		42, -1, -1, -1, 52, -1, -927, -1, -1, -1, 107, 15, 58, -1, -1, 54,
		53, 51, 81, -1, 72, -971, 74, -1, -1, -1, 55, 73, 75, -1, -1, -2,
		-2,
	};

    private static final short lapg_lalr[] = {
		1, -1, 5, -1, 0, 0, -1, -2, 1, -1, 5, -1, 0, 1, -1, -2,
		1, -1, 2, -1, 3, -1, 4, -1, 5, -1, 0, 110, -1, -2, 42, -1,
		31, 63, 32, 63, 33, 63, 34, 63, 35, 63, 36, 63, 37, 63, 40, 63,
		41, 63, 43, 63, 44, 63, 45, 63, 46, 63, 47, 63, 48, 63, 49, 63,
		50, 63, 51, 63, 52, 63, 53, 63, 54, 63, 55, 63, 56, 63, -1, -2,
		7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 28, -1, 29, -1, 34, -1,
		38, -1, 40, -1, 42, -1, 41, 43, -1, -2, 40, -1, 44, -1, 50, -1,
		31, 84, 32, 84, 33, 84, 34, 84, 35, 84, 36, 84, 37, 84, 41, 84,
		43, 84, 45, 84, 46, 84, 47, 84, 48, 84, 49, 84, 51, 84, 52, 84,
		53, 84, 54, 84, 55, 84, 56, 84, -1, -2, 35, -1, 36, -1, 37, -1,
		31, 91, 32, 91, 33, 91, 34, 91, 41, 91, 43, 91, 45, 91, 46, 91,
		47, 91, 48, 91, 49, 91, 51, 91, 52, 91, 53, 91, 54, 91, 55, 91,
		56, 91, -1, -2, 33, -1, 34, -1, 31, 94, 32, 94, 41, 94, 43, 94,
		45, 94, 46, 94, 47, 94, 48, 94, 49, 94, 51, 94, 52, 94, 53, 94,
		54, 94, 55, 94, 56, 94, -1, -2, 51, -1, 52, -1, 53, -1, 54, -1,
		31, 99, 32, 99, 41, 99, 43, 99, 45, 99, 46, 99, 47, 99, 48, 99,
		49, 99, 55, 99, 56, 99, -1, -2, 48, -1, 49, -1, 31, 102, 32, 102,
		41, 102, 43, 102, 45, 102, 46, 102, 47, 102, 55, 102, 56, 102, -1, -2,
		46, -1, 31, 104, 32, 104, 41, 104, 43, 104, 45, 104, 47, 104, 55, 104,
		56, 104, -1, -2, 47, -1, 56, -1, 31, 106, 32, 106, 41, 106, 43, 106,
		45, 106, 55, 106, -1, -2, 42, -1, 44, -1, 31, 8, 32, 8, -1, -2,
		7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 28, -1, 29, -1, 34, -1,
		38, -1, 40, -1, 42, -1, 43, 43, -1, -2, 42, -1, 44, -1, 16, 30,
		31, 30, 32, 30, -1, -2, 45, -1, 31, 35, 32, 35, -1, -2, 42, -1,
		55, -1, 33, 63, 34, 63, 35, 63, 36, 63, 37, 63, 40, 63, 41, 63,
		44, 63, 45, 63, 46, 63, 47, 63, 48, 63, 49, 63, 50, 63, 51, 63,
		52, 63, 53, 63, 54, 63, 56, 63, -1, -2, 45, -1, 41, 44, 43, 44,
		-1, -2, 7, -1, 43, 11, -1, -2, 7, -1, 8, -1, 9, -1, 15, -1,
		25, -1, 28, -1, 29, -1, 34, -1, 38, -1, 40, -1, 42, -1, 43, 43,
		-1, -2, 16, -1, 31, 32, 32, 32, -1, -2, 1, -1, 5, 49, -1, -2,
		42, -1, 31, 71, 32, 71, 33, 71, 34, 71, 35, 71, 36, 71, 37, 71,
		40, 71, 41, 71, 43, 71, 44, 71, 45, 71, 46, 71, 47, 71, 48, 71,
		49, 71, 50, 71, 51, 71, 52, 71, 53, 71, 54, 71, 55, 71, 56, 71,
		-1, -2, 35, -1, 36, -1, 37, -1, 31, 92, 32, 92, 33, 92, 34, 92,
		41, 92, 43, 92, 45, 92, 46, 92, 47, 92, 48, 92, 49, 92, 51, 92,
		52, 92, 53, 92, 54, 92, 55, 92, 56, 92, -1, -2, 35, -1, 36, -1,
		37, -1, 31, 93, 32, 93, 33, 93, 34, 93, 41, 93, 43, 93, 45, 93,
		46, 93, 47, 93, 48, 93, 49, 93, 51, 93, 52, 93, 53, 93, 54, 93,
		55, 93, 56, 93, -1, -2, 33, -1, 34, -1, 31, 97, 32, 97, 41, 97,
		43, 97, 45, 97, 46, 97, 47, 97, 48, 97, 49, 97, 51, 97, 52, 97,
		53, 97, 54, 97, 55, 97, 56, 97, -1, -2, 33, -1, 34, -1, 31, 98,
		32, 98, 41, 98, 43, 98, 45, 98, 46, 98, 47, 98, 48, 98, 49, 98,
		51, 98, 52, 98, 53, 98, 54, 98, 55, 98, 56, 98, -1, -2, 33, -1,
		34, -1, 31, 95, 32, 95, 41, 95, 43, 95, 45, 95, 46, 95, 47, 95,
		48, 95, 49, 95, 51, 95, 52, 95, 53, 95, 54, 95, 55, 95, 56, 95,
		-1, -2, 33, -1, 34, -1, 31, 96, 32, 96, 41, 96, 43, 96, 45, 96,
		46, 96, 47, 96, 48, 96, 49, 96, 51, 96, 52, 96, 53, 96, 54, 96,
		55, 96, 56, 96, -1, -2, 51, -1, 52, -1, 53, -1, 54, -1, 31, 100,
		32, 100, 41, 100, 43, 100, 45, 100, 46, 100, 47, 100, 48, 100, 49, 100,
		55, 100, 56, 100, -1, -2, 51, -1, 52, -1, 53, -1, 54, -1, 31, 101,
		32, 101, 41, 101, 43, 101, 45, 101, 46, 101, 47, 101, 48, 101, 49, 101,
		55, 101, 56, 101, -1, -2, 48, -1, 49, -1, 31, 103, 32, 103, 41, 103,
		43, 103, 45, 103, 46, 103, 47, 103, 55, 103, 56, 103, -1, -2, 46, -1,
		31, 105, 32, 105, 41, 105, 43, 105, 45, 105, 47, 105, 55, 105, 56, 105,
		-1, -2, 45, -1, 43, 12, -1, -2, 7, -1, 8, -1, 9, -1, 15, -1,
		25, -1, 28, -1, 29, -1, 34, -1, 38, -1, 40, -1, 42, -1, 43, 43,
		-1, -2, 7, -1, 8, -1, 9, -1, 15, -1, 25, -1, 28, -1, 29, -1,
		34, -1, 38, -1, 40, -1, 42, -1, 43, 43, -1, -2, 39, -1, 42, -1,
		33, 63, 34, 63, 35, 63, 36, 63, 37, 63, 40, 63, 43, 63, 44, 63,
		45, 63, 46, 63, 47, 63, 48, 63, 49, 63, 50, 63, 51, 63, 52, 63,
		53, 63, 54, 63, 56, 63, -1, -2, 7, -1, 8, -1, 9, -1, 15, -1,
		25, -1, 28, -1, 29, -1, 34, -1, 38, -1, 40, -1, 42, -1, 43, 43,
		-1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 2, 14, 23, 32, 41, 53, 56, 112, 158, 204, 209, 211, 215, 216, 221,
		267, 273, 278, 283, 283, 288, 289, 289, 289, 289, 335, 340, 341, 387, 433, 438,
		446, 453, 458, 509, 512, 515, 518, 564, 565, 612, 615, 670, 679, 683, 688, 690,
		691, 693, 695, 696, 699, 702, 705, 708, 712, 713, 713, 713, 713, 714, 715, 717,
		719, 720, 721, 723, 727, 734, 743, 752, 757, 758, 761, 762, 763, 772, 773, 782,
		783, 785, 794, 799, 802, 848, 894, 895, 941, 987, 1028, 1067, 1102, 1135, 1167, 1198,
		1229, 1235, 1236, 1237, 1238, 1239, 1240, 1241, 1242, 1248, 1249,
	};

    private static final short lapg_sym_from[] = {
		205, 206, 0, 1, 4, 6, 13, 18, 23, 57, 102, 118, 145, 179, 1, 6,
		13, 18, 23, 57, 102, 145, 179, 1, 6, 13, 18, 23, 57, 102, 145, 179,
		1, 6, 13, 18, 23, 57, 102, 145, 179, 0, 1, 4, 6, 13, 18, 23,
		57, 102, 145, 160, 179, 57, 145, 179, 12, 19, 21, 27, 28, 30, 31, 32,
		33, 35, 38, 39, 40, 41, 42, 62, 83, 84, 85, 86, 87, 88, 89, 90,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 104, 105, 110, 113, 116, 117,
		119, 121, 122, 127, 153, 165, 167, 168, 171, 173, 177, 181, 189, 190, 195, 197,
		12, 21, 28, 31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86, 87, 88,
		89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117,
		119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21,
		28, 31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86, 87, 88, 89, 90,
		91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117, 119, 122,
		127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21, 100, 171,
		190, 178, 190, 21, 100, 171, 190, 100, 12, 21, 100, 171, 190, 12, 21, 28,
		31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86, 87, 88, 89, 90, 91,
		92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117, 119, 122, 127,
		153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21, 100, 112, 171,
		190, 12, 21, 100, 171, 190, 12, 21, 100, 171, 190, 12, 21, 100, 171, 190,
		67, 12, 21, 28, 31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86, 87,
		88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116,
		117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12,
		21, 100, 171, 190, 3, 12, 21, 28, 31, 33, 35, 38, 39, 40, 41, 42,
		62, 83, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99,
		100, 110, 113, 116, 117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189,
		190, 195, 197, 12, 21, 28, 31, 33, 35, 38, 39, 40, 41, 42, 62, 83,
		86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 110,
		113, 116, 117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195,
		197, 12, 21, 100, 171, 190, 43, 44, 60, 69, 107, 143, 144, 199, 43, 44,
		69, 107, 143, 144, 199, 50, 134, 135, 136, 137, 12, 21, 28, 31, 33, 35,
		38, 39, 40, 41, 42, 50, 62, 83, 86, 87, 88, 89, 90, 91, 92, 93,
		94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117, 119, 122, 127, 134, 135,
		136, 137, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 49, 132, 133,
		49, 132, 133, 49, 132, 133, 12, 21, 28, 31, 33, 35, 38, 39, 40, 41,
		42, 62, 83, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98,
		99, 100, 110, 113, 116, 117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181,
		189, 190, 195, 197, 182, 12, 21, 28, 31, 33, 35, 38, 39, 40, 41, 42,
		45, 62, 83, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98,
		99, 100, 110, 113, 116, 117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181,
		189, 190, 195, 197, 74, 77, 125, 12, 21, 24, 28, 31, 33, 35, 38, 39,
		40, 41, 42, 59, 62, 63, 73, 83, 85, 86, 87, 88, 89, 90, 91, 92,
		93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117, 119, 122, 126, 127,
		128, 153, 165, 167, 168, 171, 177, 181, 182, 184, 189, 190, 195, 197, 78, 109,
		148, 152, 166, 183, 185, 200, 201, 45, 59, 63, 128, 64, 74, 76, 147, 157,
		53, 141, 54, 52, 140, 52, 140, 45, 51, 138, 139, 51, 138, 139, 51, 138,
		139, 51, 138, 139, 65, 73, 142, 162, 54, 0, 0, 0, 4, 0, 4, 59,
		104, 6, 23, 1, 6, 18, 102, 43, 44, 69, 107, 143, 144, 199, 1, 6,
		13, 18, 23, 57, 102, 145, 179, 1, 6, 13, 18, 23, 57, 102, 145, 179,
		12, 21, 100, 171, 190, 64, 19, 27, 85, 112, 63, 1, 6, 13, 18, 23,
		57, 102, 145, 179, 57, 1, 6, 13, 18, 23, 57, 102, 145, 179, 160, 160,
		179, 1, 6, 13, 18, 23, 57, 102, 145, 179, 12, 21, 100, 171, 190, 57,
		145, 179, 12, 21, 28, 31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86,
		87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113,
		116, 117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197,
		12, 21, 28, 31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86, 87, 88,
		89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117,
		119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 41, 12,
		21, 28, 31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86, 87, 88, 89,
		90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117, 119,
		122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21, 28,
		31, 33, 35, 38, 39, 40, 41, 42, 62, 83, 86, 87, 88, 89, 90, 91,
		92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117, 119, 122, 127,
		153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21, 28, 31, 33,
		35, 38, 41, 42, 62, 83, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98,
		99, 100, 110, 113, 116, 117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181,
		189, 190, 195, 197, 12, 21, 28, 31, 33, 35, 38, 41, 42, 62, 83, 91,
		92, 93, 94, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117, 119, 122, 127,
		153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21, 28, 31, 33,
		35, 38, 41, 42, 62, 83, 95, 96, 97, 98, 99, 100, 110, 113, 116, 117,
		119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21,
		28, 31, 33, 35, 38, 41, 42, 62, 83, 97, 98, 99, 100, 110, 113, 116,
		117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12,
		21, 28, 31, 33, 35, 38, 41, 42, 62, 83, 98, 99, 100, 110, 113, 116,
		117, 119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12,
		21, 28, 31, 33, 35, 38, 41, 42, 62, 83, 99, 100, 110, 113, 116, 117,
		119, 122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 12, 21,
		28, 31, 33, 35, 38, 41, 42, 62, 83, 99, 100, 110, 113, 116, 117, 119,
		122, 127, 153, 165, 167, 168, 171, 177, 181, 189, 190, 195, 197, 41, 62, 110,
		165, 167, 197, 1, 0, 59, 104, 63, 112, 64, 41, 62, 110, 165, 167, 197,
		118,
	};

    private static final short lapg_sym_to[] = {
		207, 208, 2, 8, 2, 8, 8, 8, 8, 8, 8, 159, 8, 8, 9, 9,
		9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 3, 12, 3, 21, 12, 12, 21,
		100, 12, 171, 178, 190, 101, 101, 101, 24, 58, 24, 58, 24, 65, 24, 67,
		24, 24, 24, 24, 24, 73, 24, 24, 24, 126, 58, 24, 24, 24, 24, 24,
		24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 146, 149, 24, 24, 24, 24,
		24, 162, 24, 24, 24, 182, 24, 24, 24, 187, 24, 24, 24, 24, 24, 24,
		25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25,
		25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25,
		25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 26, 26,
		26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
		26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
		26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 27, 27, 27, 27,
		27, 189, 189, 60, 143, 143, 143, 144, 28, 28, 28, 28, 28, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 30, 30, 30, 153, 30,
		30, 31, 31, 31, 31, 31, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33,
		117, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 35,
		35, 35, 35, 35, 19, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
		36, 36, 36, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		37, 38, 38, 38, 38, 38, 79, 79, 108, 79, 79, 79, 79, 79, 80, 80,
		80, 80, 80, 80, 80, 89, 89, 89, 89, 89, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 90, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 90, 90,
		90, 90, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 86, 86, 86,
		87, 87, 87, 88, 88, 88, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 195, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		83, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 120, 123, 164, 42, 42, 62, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 104, 42, 110, 62, 42, 127, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 165, 42,
		167, 42, 42, 42, 42, 42, 42, 42, 62, 197, 42, 42, 42, 42, 124, 151,
		174, 175, 184, 196, 198, 203, 204, 84, 105, 105, 105, 113, 121, 122, 173, 177,
		97, 97, 98, 95, 95, 96, 96, 85, 91, 91, 91, 92, 92, 92, 93, 93,
		93, 94, 94, 94, 116, 119, 168, 181, 99, 205, 4, 5, 20, 6, 6, 106,
		147, 22, 61, 13, 23, 57, 145, 81, 82, 118, 150, 169, 170, 202, 14, 14,
		56, 14, 56, 56, 14, 56, 191, 15, 15, 15, 15, 15, 15, 15, 15, 15,
		43, 43, 43, 43, 43, 114, 59, 63, 128, 154, 111, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 102, 17, 17, 17, 17, 17, 17, 17, 17, 17, 179, 180,
		192, 18, 18, 18, 18, 18, 18, 18, 18, 18, 44, 44, 44, 44, 44, 103,
		172, 193, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 74, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47,
		47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 47, 48, 48, 48,
		48, 48, 48, 48, 71, 72, 48, 48, 48, 48, 129, 130, 131, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 132, 133, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 134,
		135, 136, 137, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 138, 139, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 140, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 141, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 55, 55,
		64, 66, 68, 69, 70, 75, 78, 75, 125, 142, 55, 75, 156, 157, 158, 161,
		163, 166, 176, 75, 75, 186, 55, 188, 194, 199, 55, 200, 75, 76, 76, 76,
		76, 76, 76, 206, 7, 107, 148, 112, 155, 115, 77, 109, 152, 183, 185, 201,
		160,
	};

    private static final short lapg_rlen[] = {
		0, 1, 1, 2, 1, 3, 2, 1, 0, 1, 5, 0, 1, 3, 1, 3,
		3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 0, 1,
		0, 1, 4, 0, 1, 3, 2, 2, 1, 3, 2, 0, 1, 3, 3, 5,
		3, 0, 1, 7, 1, 2, 2, 4, 3, 4, 6, 2, 2, 3, 1, 1,
		3, 1, 1, 1, 1, 1, 4, 3, 6, 8, 6, 8, 4, 1, 3, 3,
		3, 5, 1, 1, 1, 2, 2, 1, 3, 3, 3, 1, 3, 3, 1, 3,
		3, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1, 5, 1, 3, 1,
	};

    private static final short lapg_rlex[] = {
		98, 98, 60, 61, 61, 62, 62, 62, 99, 99, 63, 100, 100, 64, 65, 65,
		66, 67, 67, 68, 68, 69, 69, 69, 69, 69, 69, 69, 70, 71, 101, 101,
		102, 102, 71, 103, 103, 71, 71, 72, 73, 73, 74, 104, 104, 75, 76, 76,
		77, 105, 105, 78, 79, 79, 79, 80, 81, 82, 82, 82, 82, 83, 83, 84,
		84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 85, 85,
		86, 86, 87, 87, 88, 88, 88, 89, 89, 89, 89, 90, 90, 90, 91, 91,
		91, 91, 91, 92, 92, 92, 93, 93, 94, 94, 95, 95, 96, 96, 97,
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
		"templates",
		"template_declaration_or_space",
		"template_start",
		"template_parameters",
		"identifier_list",
		"template_end",
		"instructions",
		"'[-]}'",
		"instruction",
		"simple_instruction",
		"sentence",
		"comma_expr",
		"template_id",
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
		"template_parametersopt",
		"identifier_listopt",
		"template_argumentsopt",
		"template_for_expropt",
		"comma_expropt",
		"expression_listopt",
		"anyopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 60;
		public static final int templates = 61;
		public static final int template_declaration_or_space = 62;
		public static final int template_start = 63;
		public static final int template_parameters = 64;
		public static final int identifier_list = 65;
		public static final int template_end = 66;
		public static final int instructions = 67;
		public static final int LBRACKETMINUSRBRACKETRBRACE = 68;
		public static final int instruction = 69;
		public static final int simple_instruction = 70;
		public static final int sentence = 71;
		public static final int comma_expr = 72;
		public static final int template_id = 73;
		public static final int template_for_expr = 74;
		public static final int template_arguments = 75;
		public static final int control_instruction = 76;
		public static final int else_node = 77;
		public static final int switch_instruction = 78;
		public static final int case_list = 79;
		public static final int one_case = 80;
		public static final int control_start = 81;
		public static final int control_sentence = 82;
		public static final int control_end = 83;
		public static final int primary_expression = 84;
		public static final int complex_data = 85;
		public static final int map_entries = 86;
		public static final int bcon = 87;
		public static final int unary_expression = 88;
		public static final int mult_expression = 89;
		public static final int additive_expression = 90;
		public static final int relational_expression = 91;
		public static final int equality_expression = 92;
		public static final int conditional_and_expression = 93;
		public static final int conditional_or_expression = 94;
		public static final int expression = 95;
		public static final int expression_list = 96;
		public static final int body = 97;
		public static final int templatesopt = 98;
		public static final int template_parametersopt = 99;
		public static final int identifier_listopt = 100;
		public static final int template_argumentsopt = 101;
		public static final int template_for_expropt = 102;
		public static final int comma_expropt = 103;
		public static final int expression_listopt = 104;
		public static final int anyopt = 105;
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

		while( lapg_m[lapg_head].state != 207+state ) {
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

		if( lapg_m[lapg_head].state != 207+state ) {
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
				 ((TemplateNode)lapg_m[lapg_head-2].sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); templates.add(((TemplateNode)lapg_m[lapg_head-2].sym)); 
				break;
			case 6:  // template_declaration_or_space ::= template_start template_end
				 templates.add(((TemplateNode)lapg_m[lapg_head-1].sym)); 
				break;
			case 10:  // template_start ::= '${' Ltemplate template_id template_parametersopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList)lapg_m[lapg_head-1].sym), templatePackage, inputName, lapg_m[lapg_head-4].line); checkFqn(((String)lapg_m[lapg_head-2].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head-4].line); 
				break;
			case 13:  // template_parameters ::= '(' identifier_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 14:  // identifier_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 15:  // identifier_list ::= identifier_list ',' identifier
				 ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 17:  // instructions ::= instructions instruction
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 18:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 19:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head-0].offset+1); 
				break;
			case 24:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-0].line); 
				break;
			case 25:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line), inputName, lapg_m[lapg_head-0].line); 
				break;
			case 26:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(inputName, lapg_m[lapg_head-0].line); 
				break;
			case 27:  // instruction ::= any
				 lapg_gg.sym = new TextNode(rawText(lapg_m[lapg_head-0].offset,lapg_m[lapg_head-0].endoffset),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 28:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 34:  // sentence ::= Lcall template_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),templatePackage,true,inputName, lapg_m[lapg_head-3].line); 
				break;
			case 37:  // sentence ::= Leval expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-2].line); 
				break;
			case 38:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-1].line); 
				break;
			case 39:  // comma_expr ::= ',' expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 41:  // template_id ::= template_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 42:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 45:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 46:  // control_instruction ::= control_start instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 47:  // control_instruction ::= control_start instructions else_node instructions control_end
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-3].sym)); applyElse(((CompoundNode)lapg_gg.sym),((ArrayList<Node>)lapg_m[lapg_head-1].sym),lapg_gg.offset,lapg_gg.endoffset,lapg_gg.line); 
				break;
			case 51:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-6].line); checkIsSpace(lapg_m[lapg_head-2].offset,lapg_m[lapg_head-2].endoffset, lapg_m[lapg_head-2].line); 
				break;
			case 52:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 53:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 54:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 55:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 56:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 57:  // control_sentence ::= Lforeach identifier Lin expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 58:  // control_sentence ::= Lfor identifier ':' expression ',' expression
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-5].line); 
				break;
			case 59:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 60:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 63:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 64:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName,lapg_m[lapg_head-2].line); 
				break;
			case 65:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 66:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 67:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].line); 
				break;
			case 68:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(inputName, lapg_m[lapg_head-0].line); 
				break;
			case 69:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, inputName, lapg_m[lapg_head-0].line); 
				break;
			case 70:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 71:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 72:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-5].line); 
				break;
			case 73:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head-7].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym), inputName, lapg_m[lapg_head-7].line, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // primary_expression ::= primary_expression '->' template_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-5].sym),templatePackage,false,inputName, lapg_m[lapg_head-5].line); 
				break;
			case 75:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-7].sym),templatePackage,inputName, lapg_m[lapg_head-7].line); 
				break;
			case 76:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].line); 
				break;
			case 78:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 79:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<String,ExpressionNode>)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 80:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new HashMap(); ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 81:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((HashMap<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 82:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 83:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 85:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 86:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].line); 
				break;
			case 88:  // mult_expression ::= mult_expression '*' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 89:  // mult_expression ::= mult_expression '/' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 90:  // mult_expression ::= mult_expression '%' unary_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 92:  // additive_expression ::= additive_expression '+' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 93:  // additive_expression ::= additive_expression '-' mult_expression
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 95:  // relational_expression ::= relational_expression '<' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 96:  // relational_expression ::= relational_expression '>' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 97:  // relational_expression ::= relational_expression '<=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 98:  // relational_expression ::= relational_expression '>=' additive_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 100:  // equality_expression ::= equality_expression '==' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 101:  // equality_expression ::= equality_expression '!=' relational_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 103:  // conditional_and_expression ::= conditional_and_expression '&&' equality_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 105:  // conditional_or_expression ::= conditional_or_expression '||' conditional_and_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].line); 
				break;
			case 107:  // expression ::= conditional_or_expression '?' expression ':' expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), inputName, lapg_m[lapg_head-4].line); 
				break;
			case 108:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 109:  // expression_list ::= expression_list ',' expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 110:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, templatePackage, inputName, lapg_m[lapg_head-0].line);
							((TemplateNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-0].sym));
							templates.add(((TemplateNode)lapg_gg.sym));
						
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
