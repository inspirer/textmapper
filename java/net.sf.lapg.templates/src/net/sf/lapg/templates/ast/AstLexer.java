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
		public static final int Lcase = 11;
		public static final int Lend = 12;
		public static final int Lelse = 13;
		public static final int Leval = 14;
		public static final int Lfalse = 15;
		public static final int Lfor = 16;
		public static final int Lfile = 17;
		public static final int Lforeach = 18;
		public static final int Lgrep = 19;
		public static final int Lif = 20;
		public static final int Lin = 21;
		public static final int Limport = 22;
		public static final int Lis = 23;
		public static final int Lmap = 24;
		public static final int Lnull = 25;
		public static final int Lquery = 26;
		public static final int Lswitch = 27;
		public static final int Ltemplate = 28;
		public static final int Ltrue = 29;
		public static final int Lself = 30;
		public static final int Lassert = 31;
		public static final int RBRACE = 32;
		public static final int MINUSRBRACE = 33;
		public static final int PLUS = 34;
		public static final int MINUS = 35;
		public static final int MULT = 36;
		public static final int DIV = 37;
		public static final int PERC = 38;
		public static final int EXCL = 39;
		public static final int OR = 40;
		public static final int LBRACKET = 41;
		public static final int RBRACKET = 42;
		public static final int LROUNDBRACKET = 43;
		public static final int RROUNDBRACKET = 44;
		public static final int DOT = 45;
		public static final int COMMA = 46;
		public static final int AMPAMP = 47;
		public static final int OROR = 48;
		public static final int EQEQ = 49;
		public static final int EQ = 50;
		public static final int EXCLEQ = 51;
		public static final int MINUSGREATER = 52;
		public static final int LESSEQ = 53;
		public static final int GREATEREQ = 54;
		public static final int LESS = 55;
		public static final int GREATER = 56;
		public static final int COLON = 57;
		public static final int QUESTMARK = 58;
		public static final int N60 = 59;
		public static final int N24 = 60;
		public static final int _skip = 61;
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
		49, 10, 54, 9, 15, 13, 17, 22, 21, 20, 50, 50, 11, 24, 14, 18,
		23, 27, 19, 12, 25, 26, 16, 29, 8, 28, 50, 4, 36, 30, 1, 1,
	};

	private int lapg_lexem[][] = unpackFromString(131,58,
		"-2,2,3,2:55,-1:2,4,-1:2,5,6,-1,7,8,9,7,10,11,12,7:2,13,7:2,14,7,15,7,16,17," +
		"7,18,7:2,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,7,39,4" +
		"0,-1,7:2,39,40,-3,2,-3,2:55,-1:2,41,-1,42,43,-1:2,44:22,-1:20,44,45,-1:2,44" +
		":2,45,-1,-62:58,-39:58,-1,6:5,46,47,6:44,-1,6:5,-9:8,7:22,-9:20,7:2,-9:2,7:" +
		"3,-9:9,7:2,48,7:19,-9:20,7:2,-9:2,7:3,-9:9,7:4,49,7:17,-9:20,7:2,-9:2,7:3,-" +
		"9:9,7:5,50,7:15,51,-9:20,7:2,-9:2,7:3,-9:9,7:3,52,7:2,53,7,54,7:13,-9:20,7:" +
		"2,-9:2,7:3,-9:9,7:18,55,7:3,-9:20,7:2,-9:2,7:3,-9:9,7:2,56,7:7,57,7,58,7:9," +
		"-9:20,7:2,-9:2,7:3,-9:9,7:4,59,7,60,7:2,61,7:6,62,7:5,-9:20,7:2,-9:2,7:3,-9" +
		":9,7:11,63,7:10,-9:20,7:2,-9:2,7:3,-9:9,7:2,64,7:19,-9:20,7:2,-9:2,7:3,-9:9" +
		",7:5,65,7:5,66,7:10,-9:20,7:2,-9:2,7:3,-9:9,7:18,67,7:3,-9:20,7:2,-9:2,7:3," +
		"-9,-34:58,-37:30,68,-37:14,69,-37:12,-36:58,-38:58,-40:58,-41:44,70,-41:13," +
		"-42:36,71,-42:21,-43:58,-44:58,-45:58,-46:58,-47:58,-48:58,-1:43,72,-1:14,-" +
		"52:44,73,-52:13,-58:44,74,-58:13,-57:44,75,-57:13,-59:58,-60:58,-61:58,-10:" +
		"51,39,-10:4,39,-10,-63:52,40,-63:4,40,-4:58,-7:58,-8:58,-5:3,76,-5:4,44:22," +
		"-5:20,44:2,-5:2,44:3,-5,-6:51,45,-6:4,45,-6,-11:58,-1:6,6:2,77,-1,6,-1:3,6," +
		"-1,6:2,-1,6,-1:5,6,-1:22,6,-1:4,6:2,-1,78,-1,-9:8,7:3,79,80,7:17,-9:20,7:2," +
		"-9:2,7:3,-9:9,7:4,81,7:17,-9:20,7:2,-9:2,7:3,-9:9,7:3,82,7:18,-9:20,7:2,-9:" +
		"2,7:3,-9:9,7:12,83,7:9,-9:20,7:2,-9:2,7:3,-9:9,7:4,84,7:17,-9:20,7:2,-9:2,7" +
		":3,-9:9,7:7,85,7:14,-9:20,7:2,-9:2,7:3,-9:9,7:2,86,7:19,-9:20,7:2,-9:2,7:3," +
		"-9:9,7:3,87,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:3,88,7:18,-9:20,7:2,-9:2,7:3,-9:" +
		"9,7:11,89,7:10,-9:20,7:2,-9:2,7:3,-9:9,7:3,90,7:18,-9:20,7:2,-9:2,7:3,-9,-2" +
		"5:8,7:22,-25:20,7:2,-25:2,7:3,-25,-23:8,7:22,-23:20,7:2,-23:2,7:3,-23,-22:8" +
		",7:22,-22:20,7:2,-22:2,7:3,-22,-9:8,7:15,91,7:6,-9:20,7:2,-9:2,7:3,-9:9,7:5" +
		",92,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:15,93,7:6,-9:20,7:2,-9:2,7:3,-9:9,7:16,9" +
		"4,7:5,-9:20,7:2,-9:2,7:3,-9:9,7:18,95,7:3,-9:20,7:2,-9:2,7:3,-9:9,7:5,96,7:" +
		"16,-9:20,7:2,-9:2,7:3,-9,-35:58,-54:58,-53:58,-50:58,-49:58,-51:58,-56:58,-" +
		"55:58,-1:51,97,-1:4,97,-1:10,98:2,-1:2,98,-1,98,-1,98,-1:33,98,-1:2,98:3,-1" +
		":2,6:5,46,47,6:44,-1,6:3,99,6,-9:8,7:3,100,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:5" +
		",101,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:5,102,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:9," +
		"103,7:12,-9:20,7:2,-9:2,7:3,-9:9,7:17,104,7:4,-9:20,7:2,-9:2,7:3,-9:9,7:5,1" +
		"05,7:16,-9:20,7:2,-9:2,7:3,-9,-14:8,7:22,-14:20,7:2,-14:2,7:3,-14,-9:8,7:3," +
		"106,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:3,107,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:4,1" +
		"08,7:17,-9:20,7:2,-9:2,7:3,-9,-18:8,7:5,109,7:16,-18:20,7:2,-18:2,7:3,-18,-" +
		"9:8,7:5,110,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:10,111,7:11,-9:20,7:2,-9:2,7:3,-" +
		"9:9,7:15,112,7:6,-9:20,7:2,-9:2,7:3,-9,-26:8,7:22,-26:20,7:2,-26:2,7:3,-26," +
		"-9:8,7:15,113,7:6,-9:20,7:2,-9:2,7:3,-9:9,7:5,114,7:16,-9:20,7:2,-9:2,7:3,-" +
		"9:9,7:11,115,7:10,-9:20,7:2,-9:2,7:3,-9,-5:51,97,-5:4,97,-5,-1,6:5,46,47,6," +
		"98:2,6:2,98,6,98,6,98,6:33,98,-1,6,98:3,6,-1,6:5,46,47,6:44,-1,6:5,-12:8,7:" +
		"22,-12:20,7:2,-12:2,7:3,-12,-13:8,7:22,-13:20,7:2,-13:2,7:3,-13,-9:8,7:11,1" +
		"16,7:10,-9:20,7:2,-9:2,7:3,-9,-32:8,7:22,-32:20,7:2,-32:2,7:3,-32,-9:8,7,11" +
		"7,7:20,-9:20,7:2,-9:2,7:3,-9,-15:8,7:22,-15:20,7:2,-15:2,7:3,-15,-16:8,7:22" +
		",-16:20,7:2,-16:2,7:3,-16,-27:8,7:22,-27:20,7:2,-27:2,7:3,-27,-9:8,7:5,118," +
		"7:16,-9:20,7:2,-9:2,7:3,-9:9,7:2,119,7:19,-9:20,7:2,-9:2,7:3,-9,-19:8,7:22," +
		"-19:20,7:2,-19:2,7:3,-19,-9:8,7:11,120,7:10,-9:20,7:2,-9:2,7:3,-9,-21:8,7:2" +
		"2,-21:20,7:2,-21:2,7:3,-21,-9:8,7:3,121,7:18,-9:20,7:2,-9:2,7:3,-9,-31:8,7:" +
		"22,-31:20,7:2,-31:2,7:3,-31,-9:8,7:20,122,7,-9:20,7:2,-9:2,7:3,-9:9,7:17,12" +
		"3,7:4,-9:20,7:2,-9:2,7:3,-9:9,7:13,124,7:8,-9:20,7:2,-9:2,7:3,-9,-17:8,7:22" +
		",-17:20,7:2,-17:2,7:3,-17,-9:8,7,125,7:20,-9:20,7:2,-9:2,7:3,-9:9,7:17,126," +
		"7:4,-9:20,7:2,-9:2,7:3,-9:9,7:2,127,7:19,-9:20,7:2,-9:2,7:3,-9,-28:8,7:22,-" +
		"28:20,7:2,-28:2,7:3,-28,-33:8,7:22,-33:20,7:2,-33:2,7:3,-33,-29:8,7:22,-29:" +
		"20,7:2,-29:2,7:3,-29,-9:8,7:13,128,7:8,-9:20,7:2,-9:2,7:3,-9,-24:8,7:22,-24" +
		":20,7:2,-24:2,7:3,-24,-9:8,7:17,129,7:4,-9:20,7:2,-9:2,7:3,-9,-20:8,7:22,-2" +
		"0:20,7:2,-20:2,7:3,-20,-9:8,7:5,130,7:16,-9:20,7:2,-9:2,7:3,-9,-30:8,7:22,-" +
		"30:20,7:2,-30:2,7:3,-30");
		
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
			case 32:
				 group = 0; break; 
			case 33:
				 group = 0; break; 
			case 61:
				 return false; 
		}
		return true;
	}
}
