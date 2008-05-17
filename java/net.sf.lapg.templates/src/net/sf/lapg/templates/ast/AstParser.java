// AstParser.java

package net.sf.lapg.templates.ast;

import java.io.IOException;
import net.sf.lapg.templates.ast.AstLexer.Lexems;
import net.sf.lapg.templates.ast.AstLexer.LapgSymbol;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.lapg.templates.api.ITemplate;


public class AstParser implements AstLexer.ErrorReporter {
	
	private ArrayList<ITemplate> templates;
	private String templatePackage;
	
	public AstParser() {
	}
	
	private static final boolean DEBUG_SYNTAX = false;
	
	private String inputName;
	private int killEnds = -1;
	private byte[] buff;
	
	private String rawText(int start, int end) {
		if( killEnds == start ) {
			while( start < end && (buff[start] == '\t' || buff[start] == ' ') )
				start++;
	
			if( start < end && buff[start] == '\r' )
				start++;
	
			if( start < end && buff[start] == '\n' )
				start++;
		}
		try {
			return new String(buff, start, end-start, "utf-8");
		} catch(UnsupportedEncodingException ex) {
			return "";
		}
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
			buff = s.getBytes("utf-8");
			AstLexer lexer = new AstLexer(new ByteArrayInputStream(buff), this, "utf-8");
			return parse(lexer);
		} catch( IOException ex ) {
			return false;
		}
	}
	
	public ITemplate[] getResult() {
		return templates.toArray(new ITemplate[templates.size()]);
	}

	private static final int[] lapg_action = new int[] {
		-3, 7, -1, 2, -11, 4, -1, -1, 3, 30, 29, 27, 28, -1, -1, 6,
		21, 24, 25, 26, -1, -19, -29, 70, 72, -1, -1, -1, 88, -1, -1, -1,
		-1, -81, -1, 74, -1, 87, 73, -1, -1, -1, -113, -1, -1, -1, 32, -1,
		-145, 71, 82, 95, -195, -241, -281, -317, -345, -369, 5, 20, -1, -1, -391, -397,
		8, -405, 43, -437, 19, -449, -1, 65, -1, -1, 62, -1, -1, -1, 41, 91,
		90, -457, -1, -465, -1, -1, -1, 23, 22, 31, 59, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 67, 49, -1, 17,
		-1, -473, -1, -1, 10, 116, -1, -479, -1, -511, 33, -1, 40, 38, -1, -1,
		-1, 64, -1, -519, -1, 83, -1, 84, -1, 69, -525, -1, -557, -1, 96, 97,
		98, -609, -655, -701, -741, -781, -821, -861, -897, -933, -961, -1, -1, -1, -1, 16,
		-1, -985, 12, 75, -1, 44, -1, 37, 35, 42, -1, 60, -1, -1, -1, 52,
		-1, 85, 117, -1, -1, 81, -993, -1, -1, -1, 66, 51, -1, 50, 18, 48,
		45, -1, 94, 92, -1, -1, -1, 55, -1, 76, -1, -1, -1025, 115, 61, -1,
		-1, -1, 57, 54, 56, 86, 78, -1, -1, 93, -1, -1057, 79, 58, -1, 80,
		-1, -2,
	};


	private static final short[] lapg_lalr = new short[] {
		1, -1, 5, -1, 0, 1, -1, -2, 1, -1, 5, -1, 0, 0, -1, -2,
		40, -1, 30, 9, 31, 9, 53, 9, -1, -2, 40, -1, 30, 68, 31, 68,
		32, 68, 33, 68, 34, 68, 35, 68, 36, 68, 38, 68, 39, 68, 41, 68,
		42, 68, 43, 68, 44, 68, 45, 68, 46, 68, 47, 68, 48, 68, 49, 68,
		50, 68, 51, 68, 52, 68, 53, 68, 54, 68, 55, 68, -1, -2, 23, -1,
		7, 63, 8, 63, 9, 63, 15, 63, 19, 63, 22, 63, 24, 63, 27, 63,
		28, 63, 33, 63, 37, 63, 38, 63, 40, 63, 56, 63, -1, -2, 7, -1,
		8, -1, 9, -1, 15, -1, 19, -1, 22, -1, 24, -1, 27, -1, 28, -1,
		33, -1, 37, -1, 38, -1, 40, -1, 56, -1, 39, 47, -1, -2, 38, -1,
		42, -1, 30, 89, 31, 89, 32, 89, 33, 89, 34, 89, 35, 89, 36, 89,
		39, 89, 41, 89, 43, 89, 44, 89, 45, 89, 46, 89, 47, 89, 48, 89,
		49, 89, 50, 89, 51, 89, 52, 89, 53, 89, 54, 89, 55, 89, -1, -2,
		34, -1, 35, -1, 36, -1, 30, 99, 31, 99, 32, 99, 33, 99, 39, 99,
		41, 99, 43, 99, 44, 99, 45, 99, 46, 99, 47, 99, 48, 99, 49, 99,
		50, 99, 51, 99, 52, 99, 53, 99, 54, 99, 55, 99, -1, -2, 32, -1,
		33, -1, 30, 102, 31, 102, 39, 102, 41, 102, 43, 102, 44, 102, 45, 102,
		46, 102, 47, 102, 48, 102, 49, 102, 50, 102, 51, 102, 52, 102, 53, 102,
		54, 102, 55, 102, -1, -2, 49, -1, 50, -1, 51, -1, 52, -1, 30, 107,
		31, 107, 39, 107, 41, 107, 43, 107, 44, 107, 45, 107, 46, 107, 47, 107,
		48, 107, 53, 107, 54, 107, 55, 107, -1, -2, 46, -1, 47, -1, 30, 110,
		31, 110, 39, 110, 41, 110, 43, 110, 44, 110, 45, 110, 48, 110, 53, 110,
		54, 110, 55, 110, -1, -2, 44, -1, 30, 112, 31, 112, 39, 112, 41, 112,
		43, 112, 45, 112, 48, 112, 53, 112, 54, 112, 55, 112, -1, -2, 45, -1,
		54, -1, 30, 114, 31, 114, 39, 114, 41, 114, 43, 114, 48, 114, 53, 114,
		55, 114, -1, -2, 7, -1, 41, 15, -1, -2, 53, -1, 30, 11, 31, 11,
		-1, -2, 7, -1, 8, -1, 9, -1, 15, -1, 19, -1, 22, -1, 24, -1,
		27, -1, 28, -1, 33, -1, 37, -1, 38, -1, 40, -1, 56, -1, 41, 47,
		-1, -2, 40, -1, 42, -1, 16, 34, 30, 34, 31, 34, -1, -2, 43, -1,
		30, 39, 31, 39, -1, -2, 48, -1, 39, 116, 43, 116, -1, -2, 43, -1,
		39, 46, 41, 46, -1, -2, 43, -1, 41, 14, -1, -2, 7, -1, 8, -1,
		9, -1, 15, -1, 19, -1, 22, -1, 24, -1, 27, -1, 28, -1, 33, -1,
		37, -1, 38, -1, 40, -1, 56, -1, 41, 47, -1, -2, 16, -1, 30, 36,
		31, 36, -1, -2, 1, -1, 5, 53, -1, -2, 7, -1, 8, -1, 9, -1,
		15, -1, 19, -1, 22, -1, 24, -1, 27, -1, 28, -1, 33, -1, 37, -1,
		38, -1, 40, -1, 56, -1, 41, 47, -1, -2, 40, -1, 30, 77, 31, 77,
		32, 77, 33, 77, 34, 77, 35, 77, 36, 77, 38, 77, 39, 77, 41, 77,
		42, 77, 43, 77, 44, 77, 45, 77, 46, 77, 47, 77, 48, 77, 49, 77,
		50, 77, 51, 77, 52, 77, 53, 77, 54, 77, 55, 77, -1, -2, 34, -1,
		35, -1, 36, -1, 30, 100, 31, 100, 32, 100, 33, 100, 39, 100, 41, 100,
		43, 100, 44, 100, 45, 100, 46, 100, 47, 100, 48, 100, 49, 100, 50, 100,
		51, 100, 52, 100, 53, 100, 54, 100, 55, 100, -1, -2, 34, -1, 35, -1,
		36, -1, 30, 101, 31, 101, 32, 101, 33, 101, 39, 101, 41, 101, 43, 101,
		44, 101, 45, 101, 46, 101, 47, 101, 48, 101, 49, 101, 50, 101, 51, 101,
		52, 101, 53, 101, 54, 101, 55, 101, -1, -2, 32, -1, 33, -1, 30, 105,
		31, 105, 39, 105, 41, 105, 43, 105, 44, 105, 45, 105, 46, 105, 47, 105,
		48, 105, 49, 105, 50, 105, 51, 105, 52, 105, 53, 105, 54, 105, 55, 105,
		-1, -2, 32, -1, 33, -1, 30, 106, 31, 106, 39, 106, 41, 106, 43, 106,
		44, 106, 45, 106, 46, 106, 47, 106, 48, 106, 49, 106, 50, 106, 51, 106,
		52, 106, 53, 106, 54, 106, 55, 106, -1, -2, 32, -1, 33, -1, 30, 103,
		31, 103, 39, 103, 41, 103, 43, 103, 44, 103, 45, 103, 46, 103, 47, 103,
		48, 103, 49, 103, 50, 103, 51, 103, 52, 103, 53, 103, 54, 103, 55, 103,
		-1, -2, 32, -1, 33, -1, 30, 104, 31, 104, 39, 104, 41, 104, 43, 104,
		44, 104, 45, 104, 46, 104, 47, 104, 48, 104, 49, 104, 50, 104, 51, 104,
		52, 104, 53, 104, 54, 104, 55, 104, -1, -2, 49, -1, 50, -1, 51, -1,
		52, -1, 30, 108, 31, 108, 39, 108, 41, 108, 43, 108, 44, 108, 45, 108,
		46, 108, 47, 108, 48, 108, 53, 108, 54, 108, 55, 108, -1, -2, 49, -1,
		50, -1, 51, -1, 52, -1, 30, 109, 31, 109, 39, 109, 41, 109, 43, 109,
		44, 109, 45, 109, 46, 109, 47, 109, 48, 109, 53, 109, 54, 109, 55, 109,
		-1, -2, 46, -1, 47, -1, 30, 111, 31, 111, 39, 111, 41, 111, 43, 111,
		44, 111, 45, 111, 48, 111, 53, 111, 54, 111, 55, 111, -1, -2, 44, -1,
		30, 113, 31, 113, 39, 113, 41, 113, 43, 113, 45, 113, 48, 113, 53, 113,
		54, 113, 55, 113, -1, -2, 42, -1, 30, 13, 31, 13, -1, -2, 7, -1,
		8, -1, 9, -1, 15, -1, 19, -1, 22, -1, 24, -1, 27, -1, 28, -1,
		33, -1, 37, -1, 38, -1, 40, -1, 56, -1, 41, 47, -1, -2, 7, -1,
		8, -1, 9, -1, 15, -1, 19, -1, 22, -1, 24, -1, 27, -1, 28, -1,
		33, -1, 37, -1, 38, -1, 40, -1, 56, -1, 41, 47, -1, -2, 7, -1,
		8, -1, 9, -1, 15, -1, 19, -1, 22, -1, 24, -1, 27, -1, 28, -1,
		33, -1, 37, -1, 38, -1, 40, -1, 56, -1, 41, 47, -1, -2,
	};


	private static final short[] lapg_sym_goto = new short[] {
		0, 1, 11, 18, 25, 32, 42, 45, 109, 162, 215, 220, 222, 226, 227, 232,
		285, 291, 296, 301, 354, 359, 360, 413, 414, 467, 472, 473, 526, 579, 584, 592,
		599, 604, 662, 665, 668, 671, 724, 778, 781, 843, 854, 859, 864, 866, 867, 869,
		871, 874, 877, 880, 883, 886, 889, 890, 892, 946, 946, 947, 948, 949, 951, 953,
		956, 958, 959, 960, 961, 962, 969, 973, 974, 975, 982, 989, 996, 1003, 1008, 1043,
		1044, 1045, 1046, 1047, 1048, 1049, 1056, 1063, 1070, 1073, 1074, 1075, 1076, 1078, 1083, 1084,
		1137, 1190, 1243, 1244, 1297, 1342, 1385, 1424, 1461, 1497, 1532,
	};

	private static final short[] lapg_sym_from = new short[] {
		224, 0, 4, 6, 14, 20, 61, 110, 131, 158, 198, 6, 14, 20, 61, 110,
		158, 198, 6, 14, 20, 61, 110, 158, 198, 6, 14, 20, 61, 110, 158, 198,
		0, 4, 6, 14, 20, 61, 110, 158, 176, 198, 61, 158, 198, 7, 13, 25,
		27, 29, 30, 31, 36, 39, 40, 41, 42, 43, 44, 60, 62, 65, 73, 75,
		76, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105,
		106, 107, 114, 119, 120, 123, 126, 127, 132, 134, 136, 138, 141, 160, 166, 172,
		173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208, 209, 219, 13, 27, 30,
		36, 39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132,
		134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208,
		209, 219, 13, 27, 30, 36, 39, 40, 41, 42, 43, 60, 65, 73, 75, 76,
		91, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107,
		119, 123, 126, 127, 132, 134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188,
		193, 200, 204, 207, 208, 209, 219, 13, 60, 107, 188, 209, 197, 209, 13, 107,
		188, 209, 107, 13, 60, 107, 188, 209, 13, 27, 30, 36, 39, 40, 41, 42,
		43, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96, 97, 98, 99, 100, 101,
		102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138, 166, 172,
		173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208, 209, 219, 13, 60, 107,
		121, 188, 209, 13, 60, 107, 188, 209, 13, 60, 107, 188, 209, 13, 27, 30,
		36, 39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132,
		134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208,
		209, 219, 13, 60, 107, 188, 209, 72, 13, 27, 30, 36, 39, 40, 41, 42,
		43, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96, 97, 98, 99, 100, 101,
		102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138, 166, 172,
		173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208, 209, 219, 33, 13, 27,
		30, 36, 39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 93, 94, 95,
		96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127,
		132, 134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207,
		208, 209, 219, 13, 60, 107, 188, 209, 2, 13, 27, 30, 36, 39, 40, 41,
		42, 43, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96, 97, 98, 99, 100,
		101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138, 166,
		172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208, 209, 219, 13, 27,
		30, 36, 39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 93, 94, 95,
		96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127,
		132, 134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207,
		208, 209, 219, 13, 60, 107, 188, 209, 26, 45, 47, 77, 115, 156, 157, 218,
		45, 47, 77, 115, 156, 157, 218, 53, 147, 148, 149, 150, 13, 27, 30, 36,
		39, 40, 41, 42, 43, 53, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132,
		134, 136, 138, 147, 148, 149, 150, 166, 172, 173, 174, 182, 183, 185, 188, 193,
		200, 204, 207, 208, 209, 219, 52, 145, 146, 52, 145, 146, 52, 145, 146, 13,
		27, 30, 36, 39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 93, 94,
		95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126,
		127, 132, 134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204,
		207, 208, 209, 219, 13, 27, 30, 36, 39, 40, 41, 42, 43, 48, 60, 65,
		73, 75, 76, 91, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104,
		105, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138, 166, 172, 173, 174, 182,
		183, 185, 188, 193, 200, 204, 207, 208, 209, 219, 82, 84, 139, 13, 21, 22,
		27, 30, 32, 34, 36, 39, 40, 41, 42, 43, 60, 65, 67, 73, 75, 76,
		86, 91, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106,
		107, 119, 123, 126, 127, 132, 134, 136, 138, 140, 166, 172, 173, 174, 182, 183,
		184, 185, 188, 193, 200, 204, 207, 208, 209, 215, 219, 85, 112, 118, 128, 130,
		164, 180, 196, 202, 216, 222, 48, 67, 86, 161, 184, 69, 83, 84, 113, 170,
		56, 154, 57, 55, 153, 55, 153, 81, 130, 179, 54, 151, 152, 54, 151, 152,
		54, 151, 152, 54, 151, 152, 63, 70, 155, 57, 141, 203, 13, 27, 30, 36,
		39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 92, 93, 94, 95, 96,
		97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132,
		134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208,
		209, 219, 0, 0, 0, 0, 4, 0, 4, 6, 20, 110, 6, 14, 21, 21,
		63, 63, 45, 47, 77, 115, 156, 157, 218, 25, 44, 114, 141, 62, 62, 6,
		14, 20, 61, 110, 158, 198, 6, 14, 20, 61, 110, 158, 198, 6, 14, 20,
		61, 110, 158, 198, 6, 14, 20, 61, 110, 158, 198, 13, 60, 107, 188, 209,
		13, 27, 30, 36, 39, 42, 43, 60, 65, 73, 75, 76, 91, 106, 107, 119,
		123, 126, 127, 132, 134, 136, 138, 166, 174, 182, 183, 185, 188, 193, 200, 204,
		208, 209, 219, 67, 67, 121, 121, 69, 69, 42, 65, 119, 138, 182, 204, 219,
		42, 65, 119, 138, 182, 204, 219, 6, 14, 20, 61, 110, 158, 198, 61, 158,
		198, 61, 131, 176, 176, 198, 13, 60, 107, 188, 209, 33, 13, 27, 30, 36,
		39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132, 134,
		136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208, 209,
		219, 13, 27, 30, 36, 39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91,
		93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119,
		123, 126, 127, 132, 134, 136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193,
		200, 204, 207, 208, 209, 219, 13, 27, 30, 36, 39, 40, 41, 42, 43, 60,
		65, 73, 75, 76, 91, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103,
		104, 105, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138, 166, 172, 173, 174,
		182, 183, 185, 188, 193, 200, 204, 207, 208, 209, 219, 42, 13, 27, 30, 36,
		39, 40, 41, 42, 43, 60, 65, 73, 75, 76, 91, 93, 94, 95, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132, 134,
		136, 138, 166, 172, 173, 174, 182, 183, 185, 188, 193, 200, 204, 207, 208, 209,
		219, 13, 27, 30, 36, 39, 42, 43, 60, 65, 73, 75, 76, 91, 96, 97,
		98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127, 132, 134,
		136, 138, 166, 174, 182, 183, 185, 188, 193, 200, 204, 208, 209, 219, 13, 27,
		30, 36, 39, 42, 43, 60, 65, 73, 75, 76, 91, 98, 99, 100, 101, 102,
		103, 104, 105, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138, 166, 174, 182,
		183, 185, 188, 193, 200, 204, 208, 209, 219, 13, 27, 30, 36, 39, 42, 43,
		60, 65, 73, 75, 76, 91, 102, 103, 104, 105, 106, 107, 119, 123, 126, 127,
		132, 134, 136, 138, 166, 174, 182, 183, 185, 188, 193, 200, 204, 208, 209, 219,
		13, 27, 30, 36, 39, 42, 43, 60, 65, 73, 75, 76, 91, 104, 105, 106,
		107, 119, 123, 126, 127, 132, 134, 136, 138, 166, 174, 182, 183, 185, 188, 193,
		200, 204, 208, 209, 219, 13, 27, 30, 36, 39, 42, 43, 60, 65, 73, 75,
		76, 91, 105, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138, 166, 174, 182,
		183, 185, 188, 193, 200, 204, 208, 209, 219, 13, 27, 30, 36, 39, 42, 43,
		60, 65, 73, 75, 76, 91, 106, 107, 119, 123, 126, 127, 132, 134, 136, 138,
		166, 174, 182, 183, 185, 188, 193, 200, 204, 208, 209, 219,
	};

	private static final short[] lapg_sym_to = new short[] {
		225, 1, 1, 9, 9, 9, 9, 9, 175, 9, 9, 10, 10, 10, 10, 10,
		10, 10, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12,
		2, 2, 13, 13, 60, 107, 60, 188, 197, 209, 108, 108, 108, 21, 22, 66,
		22, 70, 22, 72, 22, 22, 22, 22, 22, 22, 66, 22, 111, 22, 22, 22,
		22, 22, 140, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
		22, 22, 66, 22, 165, 22, 22, 22, 22, 22, 22, 22, 66, 190, 22, 22,
		22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23, 23, 23,
		23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
		23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
		23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
		23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
		24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
		24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
		24, 24, 24, 24, 24, 24, 24, 25, 25, 25, 25, 25, 208, 208, 26, 156,
		156, 156, 157, 27, 27, 27, 27, 27, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 29, 29, 29,
		166, 29, 29, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 33, 33, 33, 33, 33, 127, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 74, 35, 35,
		35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35,
		35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35,
		35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35,
		35, 35, 35, 36, 36, 36, 36, 36, 7, 37, 37, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 37, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
		38, 38, 38, 39, 39, 39, 39, 39, 68, 87, 87, 87, 87, 87, 87, 87,
		88, 88, 88, 88, 88, 88, 88, 96, 96, 96, 96, 96, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 97, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 97, 97, 97, 97, 40, 40, 40, 40, 40, 40, 40, 40, 40,
		40, 40, 40, 40, 40, 40, 93, 93, 93, 94, 94, 94, 95, 95, 95, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
		41, 41, 41, 41, 42, 42, 42, 42, 42, 42, 42, 42, 42, 91, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
		42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 133, 135, 181, 43, 62, 65,
		43, 43, 73, 76, 43, 43, 43, 43, 43, 43, 43, 43, 119, 43, 43, 43,
		138, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
		43, 43, 43, 43, 43, 43, 43, 43, 43, 182, 43, 43, 43, 43, 43, 43,
		204, 43, 43, 43, 43, 43, 43, 43, 43, 219, 43, 137, 159, 163, 172, 173,
		191, 201, 207, 214, 220, 223, 92, 120, 120, 120, 120, 123, 134, 136, 160, 193,
		104, 104, 105, 102, 102, 103, 103, 132, 174, 200, 98, 98, 98, 99, 99, 99,
		100, 100, 100, 101, 101, 101, 114, 126, 185, 106, 183, 215, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 141, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
		44, 44, 224, 3, 4, 5, 8, 6, 6, 14, 61, 158, 15, 58, 63, 64,
		115, 116, 89, 90, 131, 162, 186, 187, 221, 67, 86, 161, 184, 112, 113, 16,
		59, 16, 59, 16, 59, 210, 17, 17, 17, 17, 17, 17, 17, 18, 18, 18,
		18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 45, 45, 45, 45, 45,
		46, 69, 71, 77, 78, 81, 85, 46, 117, 128, 129, 130, 139, 155, 46, 117,
		169, 170, 171, 177, 178, 179, 117, 192, 196, 117, 203, 205, 46, 206, 213, 117,
		218, 46, 117, 121, 122, 167, 168, 124, 125, 82, 118, 164, 180, 202, 216, 222,
		83, 83, 83, 83, 83, 83, 83, 20, 20, 20, 20, 20, 20, 20, 109, 189,
		211, 110, 176, 198, 199, 212, 47, 47, 47, 47, 47, 75, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 84, 51, 51, 51, 51,
		51, 79, 80, 51, 51, 51, 51, 51, 51, 51, 51, 142, 143, 144, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51,
		51, 51, 51, 194, 195, 51, 51, 51, 51, 51, 51, 51, 51, 217, 51, 51,
		51, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 145, 146,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52,
		52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 147, 148, 149, 150, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53,
		53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 151, 152, 54, 54, 54, 54, 54, 54, 54, 54,
		54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 153, 55, 55,
		55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55,
		55, 55, 55, 55, 55, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 154, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
		56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
		57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57,
	};

	private static final short[] lapg_rlen = new short[] {
		1, 0, 1, 2, 1, 3, 2, 1, 1, 0, 1, 0, 6, 2, 1, 0,
		3, 1, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3,
		1, 1, 0, 1, 0, 4, 1, 0, 3, 2, 2, 1, 3, 2, 1, 0,
		3, 3, 5, 3, 1, 0, 7, 1, 2, 2, 4, 3, 4, 6, 1, 0,
		3, 2, 3, 1, 1, 3, 1, 1, 1, 1, 1, 4, 5, 3, 6, 7,
		9, 4, 1, 3, 3, 3, 5, 1, 1, 1, 2, 2, 5, 7, 5, 1,
		3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 1, 3, 3, 1, 3,
		1, 3, 1, 5, 1, 3,
	};

	private static final short[] lapg_rlex = new short[] {
		59, 59, 58, 60, 60, 61, 61, 61, 65, 65, 67, 67, 62, 68, 71, 71,
		66, 72, 72, 64, 63, 63, 69, 69, 73, 73, 73, 73, 73, 73, 73, 76,
		77, 79, 79, 81, 81, 77, 83, 83, 77, 77, 84, 70, 70, 82, 85, 85,
		80, 74, 74, 89, 90, 90, 75, 91, 91, 91, 92, 87, 93, 93, 94, 94,
		93, 93, 88, 88, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95,
		95, 95, 95, 97, 97, 98, 98, 96, 96, 99, 99, 99, 99, 99, 99, 100,
		100, 100, 100, 101, 101, 101, 102, 102, 102, 102, 102, 103, 103, 103, 104, 104,
		105, 105, 78, 78, 86, 86,
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
		"Lmap",
		"Lnot",
		"Lnull",
		"Lswitch",
		"Ltemplate",
		"Ltrue",
		"Lthis",
		"Lassert",
		"'}'",
		"'-}'",
		"'+'",
		"'-'",
		"'*'",
		"'/'",
		"'%'",
		"'!'",
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
		"Lnotopt",
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
		public static final int input = 58;
		public static final int templatesopt = 59;
		public static final int templates = 60;
		public static final int template_declaration_or_space = 61;
		public static final int template_start = 62;
		public static final int instructions = 63;
		public static final int template_end = 64;
		public static final int template_parametersopt = 65;
		public static final int template_parameters = 66;
		public static final int template_overridesopt = 67;
		public static final int template_overrides = 68;
		public static final int LBRACKETMINUSRBRACKETRBRACE = 69;
		public static final int template_id = 70;
		public static final int identifier_listopt = 71;
		public static final int identifier_list = 72;
		public static final int instruction = 73;
		public static final int control_instruction = 74;
		public static final int switch_instruction = 75;
		public static final int simple_instruction = 76;
		public static final int sentence = 77;
		public static final int expression = 78;
		public static final int template_argumentsopt = 79;
		public static final int template_arguments = 80;
		public static final int template_for_expropt = 81;
		public static final int template_for_expr = 82;
		public static final int comma_expropt = 83;
		public static final int comma_expr = 84;
		public static final int expression_listopt = 85;
		public static final int expression_list = 86;
		public static final int control_start = 87;
		public static final int control_end = 88;
		public static final int else_node = 89;
		public static final int anyopt = 90;
		public static final int case_list = 91;
		public static final int one_case = 92;
		public static final int control_sentence = 93;
		public static final int Lnotopt = 94;
		public static final int primary_expression = 95;
		public static final int bcon = 96;
		public static final int complex_data = 97;
		public static final int map_entries = 98;
		public static final int unary_expression = 99;
		public static final int mult_expression = 100;
		public static final int additive_expression = 101;
		public static final int relational_expression = 102;
		public static final int equality_expression = 103;
		public static final int conditional_and_expression = 104;
		public static final int conditional_or_expression = 105;
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

		while( lapg_m[lapg_head].state != 225 ) {
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

		if( lapg_m[lapg_head].state != 225 ) {
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
		if( lapg_m[lapg_head].state != -1 ) {
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
			case 5:
				 ((TemplateNode)lapg_m[lapg_head-2].sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); templates.add(((TemplateNode)lapg_m[lapg_head-2].sym)); 
				break;
			case 6:
				 templates.add(((TemplateNode)lapg_m[lapg_head-1].sym)); 
				break;
			case 12:
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-2].sym), templatePackage, ((String)lapg_m[lapg_head-1].sym), inputName, lapg_m[lapg_head-5].pos.line); 
				break;
			case 13:
				 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 16:
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 17:
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 18:
				 ((ArrayList)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 20:
				 ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 21:
				 lapg_gg.sym = new ArrayList<Node>(); ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 22:
				 skipSpaces(lapg_m[lapg_head-0].pos.offset+1); 
				break;
			case 27:
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-0].pos.line); 
				break;
			case 28:
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line), inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 29:
				 lapg_gg.sym = new DollarNode(inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 30:
				 lapg_gg.sym = new TextNode(rawText(lapg_m[lapg_head-0].pos.offset,lapg_m[lapg_head-0].endpos.offset),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 31:
				 lapg_gg.sym = ((Node)lapg_m[lapg_head-1].sym); 
				break;
			case 37:
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-2].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),templatePackage,inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 40:
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-2].pos.line); 
				break;
			case 41:
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName,lapg_m[lapg_head-1].pos.line); 
				break;
			case 42:
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 44:
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 45:
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-0].sym); 
				break;
			case 48:
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head-1].sym); 
				break;
			case 49:
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 50:
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head-3].sym)); applyElse(((CompoundNode)lapg_gg.sym),((ArrayList<Node>)lapg_m[lapg_head-1].sym)); 
				break;
			case 54:
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-6].pos.line); checkIsSpace(lapg_m[lapg_head-2].pos.offset,lapg_m[lapg_head-2].endpos.offset); 
				break;
			case 55:
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 56:
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head-0].sym)); 
				break;
			case 58:
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 59:
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head-1].sym); 
				break;
			case 60:
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 61:
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-5].pos.line); 
				break;
			case 64:
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 65:
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].pos.line); 
				break;
			case 68:
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 69:
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head-1].sym); 
				break;
			case 70:
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 71:
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 72:
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 73:
				 lapg_gg.sym = new ThisNode(inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 74:
				 lapg_gg.sym = new LiteralNode(null, inputName, lapg_m[lapg_head-0].pos.line); 
				break;
			case 75:
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 76:
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym),((ArrayList)lapg_m[lapg_head-1].sym),null,templatePackage,inputName, lapg_m[lapg_head-4].pos.line); 
				break;
			case 77:
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 78:
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-3].sym), ((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-5].pos.line); 
				break;
			case 79:
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head-3].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-6].sym),templatePackage,inputName, lapg_m[lapg_head-6].pos.line); 
				break;
			case 80:
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ArrayList)lapg_m[lapg_head-1].sym),((ExpressionNode)lapg_m[lapg_head-8].sym),templatePackage,inputName, lapg_m[lapg_head-8].pos.line); 
				break;
			case 81:
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head-3].sym), ((ExpressionNode)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-3].pos.line); 
				break;
			case 83:
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 84:
				 lapg_gg.sym = new ConcreteMapNode(((HashMap<ExpressionNode,ExpressionNode>)lapg_m[lapg_head-1].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 85:
				 lapg_gg.sym = new HashMap(); ((HashMap<ExpressionNode,ExpressionNode>)lapg_gg.sym).put(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 86:
				 ((HashMap<ExpressionNode,ExpressionNode>)lapg_gg.sym).put(((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 87:
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 88:
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 90:
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].pos.line); 
				break;
			case 91:
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-1].pos.line); 
				break;
			case 92:
				 lapg_gg.sym = new MapNode(null,((ExpressionNode)lapg_m[lapg_head-2].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-4].pos.line); 
				break;
			case 93:
				 lapg_gg.sym = new MapNode(((ExpressionNode)lapg_m[lapg_head-4].sym),((ExpressionNode)lapg_m[lapg_head-2].sym),((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-6].pos.line); 
				break;
			case 94:
				 error("TODO"); 
				break;
			case 96:
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 97:
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 98:
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 100:
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 101:
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 103:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 104:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 105:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 106:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 108:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 109:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 111:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 113:
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head-0].sym),inputName, lapg_m[lapg_head-2].pos.line); 
				break;
			case 115:
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head-4].sym), ((ExpressionNode)lapg_m[lapg_head-2].sym), ((ExpressionNode)lapg_m[lapg_head-0].sym), inputName, lapg_m[lapg_head-4].pos.line); 
				break;
			case 116:
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head-0].sym)); 
				break;
			case 117:
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
