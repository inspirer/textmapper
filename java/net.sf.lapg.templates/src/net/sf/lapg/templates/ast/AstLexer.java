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
		public static final int Lswitch = 26;
		public static final int Ltemplate = 27;
		public static final int Ltrue = 28;
		public static final int Lself = 29;
		public static final int Lassert = 30;
		public static final int RBRACE = 31;
		public static final int MINUSRBRACE = 32;
		public static final int PLUS = 33;
		public static final int MINUS = 34;
		public static final int MULT = 35;
		public static final int DIV = 36;
		public static final int PERC = 37;
		public static final int EXCL = 38;
		public static final int OR = 39;
		public static final int LBRACKET = 40;
		public static final int RBRACKET = 41;
		public static final int LROUNDBRACKET = 42;
		public static final int RROUNDBRACKET = 43;
		public static final int DOT = 44;
		public static final int COMMA = 45;
		public static final int AMPAMP = 46;
		public static final int OROR = 47;
		public static final int EQEQ = 48;
		public static final int EXCLEQ = 49;
		public static final int MINUSGREATER = 50;
		public static final int LESSEQ = 51;
		public static final int GREATEREQ = 52;
		public static final int LESS = 53;
		public static final int GREATER = 54;
		public static final int COLON = 55;
		public static final int QUESTMARK = 56;
		public static final int N60 = 57;
		public static final int N24 = 58;
		public static final int _skip = 59;
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 55, 50, 1, 1, 55, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		55, 33, 51, 3, 2, 32, 41, 6, 37, 38, 31, 30, 40, 29, 39, 5,
		54, 54, 54, 54, 54, 54, 54, 54, 49, 49, 45, 1, 44, 42, 43, 46,
		1, 53, 53, 53, 53, 53, 53, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 35, 7, 36, 1, 48,
		47, 10, 52, 9, 15, 13, 17, 22, 21, 20, 48, 48, 11, 24, 14, 18,
		23, 48, 19, 12, 25, 26, 16, 27, 8, 48, 48, 4, 34, 28, 1, 1,
	};

	private int lapg_lexem[][] = unpackFromString(126,56,
		"-2,2,3,2:53,-1:2,4,-1:2,5,6,-1,7,8,9,7,10,11,12,7:2,13,7:2,14,7,15,7,16,17," +
		"7:2,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,7,38,39,-1," +
		"7:2,38,39,-3,2,-3,2:53,-1:2,40,-1,41,42,-1:2,43:20,-1:20,43,44,-1:2,43:2,44" +
		",-1,-60:56,-38:56,-1,6:5,45,46,6:42,-1,6:5,-9:8,7:20,-9:20,7:2,-9:2,7:3,-9:" +
		"9,7:2,47,7:17,-9:20,7:2,-9:2,7:3,-9:9,7:4,48,7:15,-9:20,7:2,-9:2,7:3,-9:9,7" +
		":5,49,7:13,50,-9:20,7:2,-9:2,7:3,-9:9,7:3,51,7:2,52,7,53,7:11,-9:20,7:2,-9:" +
		"2,7:3,-9:9,7:18,54,7,-9:20,7:2,-9:2,7:3,-9:9,7:2,55,7:7,56,7,57,7:7,-9:20,7" +
		":2,-9:2,7:3,-9:9,7:4,58,7,59,7:2,60,7:6,61,7:3,-9:20,7:2,-9:2,7:3,-9:9,7:11" +
		",62,7:8,-9:20,7:2,-9:2,7:3,-9:9,7:2,63,7:17,-9:20,7:2,-9:2,7:3,-9:9,7:5,64," +
		"7:5,65,7:8,-9:20,7:2,-9:2,7:3,-9,-33:56,-36:28,66,-36:14,67,-36:12,-35:56,-" +
		"37:56,-39:56,-40:42,68,-40:13,-41:34,69,-41:21,-42:56,-43:56,-44:56,-45:56," +
		"-46:56,-47:56,-1:41,70,-1:56,71,-1:13,-56:42,72,-56:13,-55:42,73,-55:13,-57" +
		":56,-58:56,-59:56,-10:49,38,-10:4,38,-10,-61:50,39,-61:4,39,-4:56,-7:56,-8:" +
		"56,-5:3,74,-5:4,43:20,-5:20,43:2,-5:2,43:3,-5,-6:49,44,-6:4,44,-6,-11:56,-1" +
		":6,6:2,75,-1,6,-1:3,6,-1,6:2,-1,6,-1:5,6,-1:20,6,-1:4,6:2,-1,76,-1,-9:8,7:3" +
		",77,78,7:15,-9:20,7:2,-9:2,7:3,-9:9,7:4,79,7:15,-9:20,7:2,-9:2,7:3,-9:9,7:3" +
		",80,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:12,81,7:7,-9:20,7:2,-9:2,7:3,-9:9,7:4,82" +
		",7:15,-9:20,7:2,-9:2,7:3,-9:9,7:7,83,7:12,-9:20,7:2,-9:2,7:3,-9:9,7:2,84,7:" +
		"17,-9:20,7:2,-9:2,7:3,-9:9,7:3,85,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:3,86,7:16," +
		"-9:20,7:2,-9:2,7:3,-9:9,7:11,87,7:8,-9:20,7:2,-9:2,7:3,-9:9,7:3,88,7:16,-9:" +
		"20,7:2,-9:2,7:3,-9,-25:8,7:20,-25:20,7:2,-25:2,7:3,-25,-23:8,7:20,-23:20,7:" +
		"2,-23:2,7:3,-23,-22:8,7:20,-22:20,7:2,-22:2,7:3,-22,-9:8,7:15,89,7:4,-9:20," +
		"7:2,-9:2,7:3,-9:9,7:5,90,7:14,-9:20,7:2,-9:2,7:3,-9:9,7:15,91,7:4,-9:20,7:2" +
		",-9:2,7:3,-9:9,7:16,92,7:3,-9:20,7:2,-9:2,7:3,-9:9,7:18,93,7,-9:20,7:2,-9:2" +
		",7:3,-9,-34:56,-52:56,-51:56,-49:56,-48:56,-50:56,-54:56,-53:56,-1:49,94,-1" +
		":4,94,-1:10,95:2,-1:2,95,-1,95,-1,95,-1:31,95,-1:2,95:3,-1:2,6:5,45,46,6:42" +
		",-1,6:3,96,6,-9:8,7:3,97,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:5,98,7:14,-9:20,7:2" +
		",-9:2,7:3,-9:9,7:5,99,7:14,-9:20,7:2,-9:2,7:3,-9:9,7:9,100,7:10,-9:20,7:2,-" +
		"9:2,7:3,-9:9,7:17,101,7:2,-9:20,7:2,-9:2,7:3,-9:9,7:5,102,7:14,-9:20,7:2,-9" +
		":2,7:3,-9,-14:8,7:20,-14:20,7:2,-14:2,7:3,-14,-9:8,7:3,103,7:16,-9:20,7:2,-" +
		"9:2,7:3,-9:9,7:3,104,7:16,-9:20,7:2,-9:2,7:3,-9:9,7:4,105,7:15,-9:20,7:2,-9" +
		":2,7:3,-9,-18:8,7:5,106,7:14,-18:20,7:2,-18:2,7:3,-18,-9:8,7:5,107,7:14,-9:" +
		"20,7:2,-9:2,7:3,-9:9,7:10,108,7:9,-9:20,7:2,-9:2,7:3,-9:9,7:15,109,7:4,-9:2" +
		"0,7:2,-9:2,7:3,-9,-26:8,7:20,-26:20,7:2,-26:2,7:3,-26,-9:8,7:15,110,7:4,-9:" +
		"20,7:2,-9:2,7:3,-9:9,7:5,111,7:14,-9:20,7:2,-9:2,7:3,-9,-5:49,94,-5:4,94,-5" +
		",-1,6:5,45,46,6,95:2,6:2,95,6,95,6,95,6:31,95,-1,6,95:3,6,-1,6:5,45,46,6:42" +
		",-1,6:5,-12:8,7:20,-12:20,7:2,-12:2,7:3,-12,-13:8,7:20,-13:20,7:2,-13:2,7:3" +
		",-13,-9:8,7:11,112,7:8,-9:20,7:2,-9:2,7:3,-9,-31:8,7:20,-31:20,7:2,-31:2,7:" +
		"3,-31,-9:8,7,113,7:18,-9:20,7:2,-9:2,7:3,-9,-15:8,7:20,-15:20,7:2,-15:2,7:3" +
		",-15,-16:8,7:20,-16:20,7:2,-16:2,7:3,-16,-27:8,7:20,-27:20,7:2,-27:2,7:3,-2" +
		"7,-9:8,7:5,114,7:14,-9:20,7:2,-9:2,7:3,-9:9,7:2,115,7:17,-9:20,7:2,-9:2,7:3" +
		",-9,-19:8,7:20,-19:20,7:2,-19:2,7:3,-19,-9:8,7:11,116,7:8,-9:20,7:2,-9:2,7:" +
		"3,-9,-21:8,7:20,-21:20,7:2,-21:2,7:3,-21,-9:8,7:3,117,7:16,-9:20,7:2,-9:2,7" +
		":3,-9,-30:8,7:20,-30:20,7:2,-30:2,7:3,-30,-9:8,7:17,118,7:2,-9:20,7:2,-9:2," +
		"7:3,-9:9,7:13,119,7:6,-9:20,7:2,-9:2,7:3,-9,-17:8,7:20,-17:20,7:2,-17:2,7:3" +
		",-17,-9:8,7,120,7:18,-9:20,7:2,-9:2,7:3,-9:9,7:17,121,7:2,-9:20,7:2,-9:2,7:" +
		"3,-9:9,7:2,122,7:17,-9:20,7:2,-9:2,7:3,-9,-32:8,7:20,-32:20,7:2,-32:2,7:3,-" +
		"32,-28:8,7:20,-28:20,7:2,-28:2,7:3,-28,-9:8,7:13,123,7:6,-9:20,7:2,-9:2,7:3" +
		",-9,-24:8,7:20,-24:20,7:2,-24:2,7:3,-24,-9:8,7:17,124,7:2,-9:20,7:2,-9:2,7:" +
		"3,-9,-20:8,7:20,-20:20,7:2,-20:2,7:3,-20,-9:8,7:5,125,7:14,-9:20,7:2,-9:2,7" +
		":3,-9,-29:8,7:20,-29:20,7:2,-29:2,7:3,-29");
		
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
			case 31:
				 group = 0; break; 
			case 32:
				 group = 0; break; 
			case 59:
				 return false; 
		}
		return true;
	}
}
