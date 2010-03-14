package net.sf.lapg.templates.ast;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class AstLexer {

	public static class LapgSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	};

	public interface Lexems {
		public static final int eoi = 0;
		public static final int any = 1;
		public static final int escdollar = 2;
		public static final int escid = 3;
		public static final int escint = 4;
		public static final int N24LBRACE = 5;
		public static final int N24DIV = 6;
		public static final int identifier = 7;
		public static final int icon = 8;
		public static final int ccon = 9;
		public static final int Lcall = 10;
		public static final int Lcached = 11;
		public static final int Lcase = 12;
		public static final int Lend = 13;
		public static final int Lelse = 14;
		public static final int Leval = 15;
		public static final int Lfalse = 16;
		public static final int Lfor = 17;
		public static final int Lfile = 18;
		public static final int Lforeach = 19;
		public static final int Lgrep = 20;
		public static final int Lif = 21;
		public static final int Lin = 22;
		public static final int Limport = 23;
		public static final int Lis = 24;
		public static final int Lmap = 25;
		public static final int Lnull = 26;
		public static final int Lquery = 27;
		public static final int Lswitch = 28;
		public static final int Ltemplate = 29;
		public static final int Ltrue = 30;
		public static final int Lself = 31;
		public static final int Lassert = 32;
		public static final int RBRACE = 33;
		public static final int MINUSRBRACE = 34;
		public static final int PLUS = 35;
		public static final int MINUS = 36;
		public static final int MULT = 37;
		public static final int DIV = 38;
		public static final int PERC = 39;
		public static final int EXCL = 40;
		public static final int OR = 41;
		public static final int LBRACKET = 42;
		public static final int RBRACKET = 43;
		public static final int LROUNDBRACKET = 44;
		public static final int RROUNDBRACKET = 45;
		public static final int DOT = 46;
		public static final int COMMA = 47;
		public static final int AMPAMP = 48;
		public static final int OROR = 49;
		public static final int EQEQ = 50;
		public static final int EQ = 51;
		public static final int EXCLEQ = 52;
		public static final int MINUSGREATER = 53;
		public static final int LESSEQ = 54;
		public static final int GREATEREQ = 55;
		public static final int LESS = 56;
		public static final int GREATER = 57;
		public static final int COLON = 58;
		public static final int QUESTMARK = 59;
		public static final int N60 = 60;
		public static final int N24 = 61;
		public static final int _skip = 62;
	}
	
	public interface ErrorReporter {
		void error(int start, int end, int line, String s);
	};

	public static final int TOKEN_SIZE = 2048;

	final private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen, l;
	private char chr;

	private int group = 0;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;
	

	public AstLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.stream = stream;
		this.reporter = reporter;
		this.datalen = stream.read(data);
		this.l = 0;
		chr = l < datalen ? data[l++] : 0;
	}

	public int getTokenLine() {
		return tokenLine;
	} 

	public void setLine(int currLine) {
		this.currLine = currLine;
	}

	public void setOffset(int currOffset) {
		this.currOffset = currOffset;
	}

	public String current() {
		return token.toString();
	}

    private static final short lapg_char2no[] = {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 57, 52, 1, 1, 57, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		57, 35, 53, 3, 2, 34, 43, 6, 39, 40, 33, 32, 42, 31, 41, 5,
		56, 56, 56, 56, 56, 56, 56, 56, 51, 51, 47, 1, 46, 44, 45, 48,
		1, 55, 55, 55, 55, 55, 55, 50, 50, 50, 50, 50, 50, 50, 50, 50,
		50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 37, 7, 38, 1, 50,
		49, 10, 54, 9, 14, 13, 18, 22, 12, 21, 50, 50, 11, 24, 16, 19,
		23, 27, 20, 15, 25, 26, 17, 29, 8, 28, 50, 4, 36, 30, 1, 1,
	};

	private int lapg_lexem[][] = unpackFromString(135,58,
		"-2,2,3,2:55,-1:2,4,-1:2,5,6,-1,7,8,9,7:2,10,7,11,12,7,13,7:2,14,15,7,16,17," +
		"7,18,7:2,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,7,39,4" +
		"0,-1,7:2,39,40,-3,2,-3,2:55,-1:2,41,-1,42,43,-1:2,44:22,-1:20,44,45,-1:2,44" +
		":2,45,-1,-63:58,-40:58,-1,6:5,46,47,6:44,-1,6:5,-9:8,7:22,-9:20,7:2,-9:2,7:" +
		"3,-9:9,7:2,48,7:19,-9:20,7:2,-9:2,7:3,-9:9,7:7,49,7:14,-9:20,7:2,-9:2,7:3,-" +
		"9:9,7:3,50,7:4,51,52,7:12,-9:20,7:2,-9:2,7:3,-9:9,7:5,53,7:15,54,-9:20,7:2," +
		"-9:2,7:3,-9:9,7:18,55,7:3,-9:20,7:2,-9:2,7:3,-9:9,7:2,56,7:8,57,7,58,7:8,-9" +
		":20,7:2,-9:2,7:3,-9:9,7:7,59,60,7,61,7:5,62,7:5,-9:20,7:2,-9:2,7:3,-9:9,7:1" +
		"2,63,7:9,-9:20,7:2,-9:2,7:3,-9:9,7:2,64,7:19,-9:20,7:2,-9:2,7:3,-9:9,7:5,65" +
		",7:6,66,7:9,-9:20,7:2,-9:2,7:3,-9:9,7:18,67,7:3,-9:20,7:2,-9:2,7:3,-9,-35:5" +
		"8,-38:30,68,-38:14,69,-38:12,-37:58,-39:58,-41:58,-42:44,70,-42:13,-43:36,7" +
		"1,-43:21,-44:58,-45:58,-46:58,-47:58,-48:58,-49:58,-1:43,72,-1:14,-53:44,73" +
		",-53:13,-59:44,74,-59:13,-58:44,75,-58:13,-60:58,-61:58,-62:58,-10:51,39,-1" +
		"0:4,39,-10,-64:52,40,-64:4,40,-4:58,-7:58,-8:58,-5:3,76,-5:4,44:22,-5:20,44" +
		":2,-5:2,44:3,-5,-6:51,45,-6:4,45,-6,-11:58,-1:6,6:2,77,-1,6,-1:5,6:3,-1,6,-" +
		"1:4,6,-1:22,6,-1:4,6:2,-1,78,-1,-9:8,7,79,7,80,7:3,81,7:14,-9:20,7:2,-9:2,7" +
		":3,-9:9,7:7,82,7:14,-9:20,7:2,-9:2,7:3,-9:9,7:7,83,7:14,-9:20,7:2,-9:2,7:3," +
		"-9:9,7:6,84,7:15,-9:20,7:2,-9:2,7:3,-9:9,7:2,85,7:19,-9:20,7:2,-9:2,7:3,-9:" +
		"9,7:3,86,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:13,87,7:8,-9:20,7:2,-9:2,7:3,-9:9,7" +
		":3,88,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:3,89,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:12" +
		",90,7:9,-9:20,7:2,-9:2,7:3,-9:9,7:3,91,7:18,-9:20,7:2,-9:2,7:3,-9,-26:8,7:2" +
		"2,-26:20,7:2,-26:2,7:3,-26,-24:8,7:22,-24:20,7:2,-24:2,7:3,-24,-23:8,7:22,-" +
		"23:20,7:2,-23:2,7:3,-23,-9:8,7:15,92,7:6,-9:20,7:2,-9:2,7:3,-9:9,7:5,93,7:1" +
		"6,-9:20,7:2,-9:2,7:3,-9:9,7:15,94,7:6,-9:20,7:2,-9:2,7:3,-9:9,7:16,95,7:5,-" +
		"9:20,7:2,-9:2,7:3,-9:9,7:18,96,7:3,-9:20,7:2,-9:2,7:3,-9:9,7:5,97,7:16,-9:2" +
		"0,7:2,-9:2,7:3,-9,-36:58,-55:58,-54:58,-51:58,-50:58,-52:58,-57:58,-56:58,-" +
		"1:51,98,-1:4,98,-1:10,99:2,-1:2,99:2,-1:3,99,-1:32,99,-1:2,99:3,-1:2,6:5,46" +
		",47,6:44,-1,6:3,100,6,-9:8,7:4,101,7:17,-9:20,7:2,-9:2,7:3,-9:9,7:3,102,7:1" +
		"8,-9:20,7:2,-9:2,7:3,-9:9,7:5,103,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:5,104,7:16" +
		",-9:20,7:2,-9:2,7:3,-9:9,7:5,105,7:16,-9:20,7:2,-9:2,7:3,-9,-15:8,7:22,-15:" +
		"20,7:2,-15:2,7:3,-15,-9:8,7:3,106,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:10,107,7:1" +
		"1,-9:20,7:2,-9:2,7:3,-9:9,7:17,108,7:4,-9:20,7:2,-9:2,7:3,-9:9,7:3,109,7:18" +
		",-9:20,7:2,-9:2,7:3,-9:9,7:7,110,7:14,-9:20,7:2,-9:2,7:3,-9,-19:8,7:5,111,7" +
		":16,-19:20,7:2,-19:2,7:3,-19,-9:8,7:5,112,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:11" +
		",113,7:10,-9:20,7:2,-9:2,7:3,-9:9,7:15,114,7:6,-9:20,7:2,-9:2,7:3,-9,-27:8," +
		"7:22,-27:20,7:2,-27:2,7:3,-27,-9:8,7:15,115,7:6,-9:20,7:2,-9:2,7:3,-9:9,7:5" +
		",116,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:12,117,7:9,-9:20,7:2,-9:2,7:3,-9,-5:51," +
		"98,-5:4,98,-5,-1,6:5,46,47,6,99:2,6:2,99:2,6:3,99,6:32,99,-1,6,99:3,6,-1,6:" +
		"5,46,47,6:44,-1,6:5,-9:8,7:5,118,7:16,-9:20,7:2,-9:2,7:3,-9,-12:8,7:22,-12:" +
		"20,7:2,-12:2,7:3,-12,-14:8,7:22,-14:20,7:2,-14:2,7:3,-14,-9:8,7:12,119,7:9," +
		"-9:20,7:2,-9:2,7:3,-9,-16:8,7:22,-16:20,7:2,-16:2,7:3,-16,-17:8,7:22,-17:20" +
		",7:2,-17:2,7:3,-17,-33:8,7:22,-33:20,7:2,-33:2,7:3,-33,-9:8,7,120,7:20,-9:2" +
		"0,7:2,-9:2,7:3,-9,-28:8,7:22,-28:20,7:2,-28:2,7:3,-28,-9:8,7:5,121,7:16,-9:" +
		"20,7:2,-9:2,7:3,-9:9,7:2,122,7:19,-9:20,7:2,-9:2,7:3,-9,-20:8,7:22,-20:20,7" +
		":2,-20:2,7:3,-20,-9:8,7:12,123,7:9,-9:20,7:2,-9:2,7:3,-9,-22:8,7:22,-22:20," +
		"7:2,-22:2,7:3,-22,-9:8,7:3,124,7:18,-9:20,7:2,-9:2,7:3,-9,-32:8,7:22,-32:20" +
		",7:2,-32:2,7:3,-32,-9:8,7:20,125,7,-9:20,7:2,-9:2,7:3,-9:9,7:6,126,7:15,-9:" +
		"20,7:2,-9:2,7:3,-9:9,7:17,127,7:4,-9:20,7:2,-9:2,7:3,-9:9,7:4,128,7:17,-9:2" +
		"0,7:2,-9:2,7:3,-9,-18:8,7:22,-18:20,7:2,-18:2,7:3,-18,-9:8,7,129,7:20,-9:20" +
		",7:2,-9:2,7:3,-9:9,7:17,130,7:4,-9:20,7:2,-9:2,7:3,-9:9,7:2,131,7:19,-9:20," +
		"7:2,-9:2,7:3,-9,-29:8,7:22,-29:20,7:2,-29:2,7:3,-29,-13:8,7:22,-13:20,7:2,-" +
		"13:2,7:3,-13,-34:8,7:22,-34:20,7:2,-34:2,7:3,-34,-30:8,7:22,-30:20,7:2,-30:" +
		"2,7:3,-30,-9:8,7:4,132,7:17,-9:20,7:2,-9:2,7:3,-9,-25:8,7:22,-25:20,7:2,-25" +
		":2,7:3,-25,-9:8,7:17,133,7:4,-9:20,7:2,-9:2,7:3,-9,-21:8,7:22,-21:20,7:2,-2" +
		"1:2,7:3,-21,-9:8,7:5,134,7:16,-9:20,7:2,-9:2,7:3,-9,-31:8,7:22,-31:20,7:2,-" +
		"31:2,7:3,-31");
		
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;
		
		int commaIndex;
		String workString;
		
		int res[][] = new int[size1][size2];
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex == -1) ? st : st.substring(0, commaIndex);
				st = st.substring(commaIndex + 1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j] = Integer.parseInt(workString);
					continue;
				}
				lengthString = workString.substring(colonIndex + 1);
				sequenceLength = Integer.parseInt(lengthString);
				workString = workString.substring(0, colonIndex);
				sequenceInteger = Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}

	private static int mapCharacter(int chr) {
		if(chr >= 0 && chr < 128) {
			return lapg_char2no[chr];
		}
		return 1;
	}

	public LapgSymbol next() throws IOException, UnsupportedEncodingException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {
			lapg_n.offset = currOffset;
			tokenLine = lapg_n.line = currLine;
			if(token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			int tokenStart = l-1;

			for( state = group; state >= 0; ) {
				state = lapg_lexem[state][mapCharacter(chr)];
				if( state >= -1 && chr != 0 ) { 
					currOffset++;
					if( chr == '\n' ) {
						currLine++;
					}
					if( l >= datalen ) {
						token.append(data, tokenStart, l - tokenStart);
						datalen = stream.read(data);
						tokenStart = l = 0;
					}
					chr = l < datalen ? data[l++] : 0;
				}
			}
			lapg_n.endoffset = currOffset;

			if( state == -1 ) {
				if( chr == 0 ) {
					reporter.error(lapg_n.offset, lapg_n.endoffset, currLine, "Unexpected end of file reached");
					break;
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, currLine, MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.lexem = -1;
				continue;
			}

			if(l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.lexem = -state-2;
			lapg_n.sym = null;

		} while(lapg_n.lexem == -1 || !createToken(lapg_n));
		return lapg_n;
	}

	private boolean createToken(LapgSymbol lapg_n) throws UnsupportedEncodingException {
		switch( lapg_n.lexem ) {
			case 3:
				 lapg_n.sym = token.toString().substring(1, token.length()); break; 
			case 4:
				 lapg_n.sym = Integer.parseInt(token.toString().substring(1, token.length())); break; 
			case 5:
				 group = 1; break; 
			case 7:
				 lapg_n.sym = current(); break; 
			case 8:
				 lapg_n.sym = Integer.parseInt(current()); break; 
			case 9:
				 lapg_n.sym = token.toString().substring(1, token.length()-1); break; 
			case 33:
				 group = 0; break; 
			case 34:
				 group = 0; break; 
			case 62:
				 return false; 
		}
		return true;
	}
}
