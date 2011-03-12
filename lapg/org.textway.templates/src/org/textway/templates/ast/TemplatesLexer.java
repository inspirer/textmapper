package org.textway.templates.ast;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class TemplatesLexer {

	public static class LapgSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int any = 1;
		public static final int escdollar = 2;
		public static final int escid = 3;
		public static final int escint = 4;
		public static final int DOLLARLCURLY = 5;
		public static final int DOLLARSLASH = 6;
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
		public static final int Lnew = 26;
		public static final int Lnull = 27;
		public static final int Lquery = 28;
		public static final int Lswitch = 29;
		public static final int Lseparator = 30;
		public static final int Ltemplate = 31;
		public static final int Ltrue = 32;
		public static final int Lself = 33;
		public static final int Lassert = 34;
		public static final int LCURLY = 35;
		public static final int RCURLY = 36;
		public static final int MINUSRCURLY = 37;
		public static final int PLUS = 38;
		public static final int MINUS = 39;
		public static final int MULT = 40;
		public static final int SLASH = 41;
		public static final int PERCENT = 42;
		public static final int EXCLAMATION = 43;
		public static final int OR = 44;
		public static final int LSQUARE = 45;
		public static final int RSQUARE = 46;
		public static final int LPAREN = 47;
		public static final int RPAREN = 48;
		public static final int DOT = 49;
		public static final int COMMA = 50;
		public static final int AMPERSANDAMPERSAND = 51;
		public static final int OROR = 52;
		public static final int EQUALEQUAL = 53;
		public static final int EQUAL = 54;
		public static final int EXCLAMATIONEQUAL = 55;
		public static final int MINUSGREATER = 56;
		public static final int EQUALGREATER = 57;
		public static final int LESSEQUAL = 58;
		public static final int GREATEREQUAL = 59;
		public static final int LESS = 60;
		public static final int GREATER = 61;
		public static final int COLON = 62;
		public static final int QUESTIONMARK = 63;
		public static final int _skip = 64;
	}
	
	public interface ErrorReporter {
		void error(int start, int end, int line, String s);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen, l;
	private char chr;

	private int group;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;
	
	private int deep = 0;
	
	private String unescape(String s, int start, int end) {
		StringBuilder sb = new StringBuilder();
		end = Math.min(end, s.length());
		for(int i = start; i < end; i++) {
			char c = s.charAt(i);
			if(c == '\\') {
				if(++i == end) {
					break;
				}
				c = s.charAt(i);
				if(c == 'u' || c == 'x') {
					// FIXME process unicode
				} else if(c == 'n') {
					sb.append('\n');
				} else if(c == 'r') {
					sb.append('\r');
				} else if(c == 't') {
					sb.append('\t');
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public TemplatesLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.stream = stream;
		this.datalen = stream.read(data);
		this.l = 0;
		this.group = 0;
		chr = l < datalen ? data[l++] : 0;
	}

	public int getState() {
		return group;
	}

	public void setState(int state) {
		this.group = state;
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 56, 51, 1, 1, 56, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		56, 35, 52, 3, 2, 34, 43, 6, 39, 40, 33, 32, 42, 31, 41, 5,
		55, 55, 55, 55, 55, 55, 55, 55, 50, 50, 47, 1, 46, 44, 45, 48,
		1, 54, 54, 54, 54, 54, 54, 49, 49, 49, 49, 49, 49, 49, 49, 49,
		49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 37, 7, 38, 1, 49,
		1, 10, 53, 9, 14, 13, 18, 22, 12, 21, 49, 49, 11, 24, 16, 19,
		23, 28, 20, 15, 25, 27, 17, 26, 8, 29, 49, 4, 36, 30, 1, 1
	};

	private static final short lapg_lexemnum[] = {
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
		17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
		33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
		49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64
	};

	private int lapg_lexem[][] = unpackFromString(144,57,
		"-2,2,3,2:54,-1:4,4,5,6,-1,7,8,9,7:2,10,7,11,12,7,13,7:2,14,15,7,16,17,7:2,1" +
		"8,7,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,7,38,39,-1,7:2" +
		",38,39,-3,2,-3,2:54,-1:2,40,-1,41,42,-1:2,43:22,-1:19,43,44,-1:2,43:2,44,-1" +
		",-37:57,-43:57,-1,6:5,45,46,6:43,-1,6:5,-9:8,7:22,-9:19,7:2,-9:2,7:3,-9:9,7" +
		":2,47,7:19,-9:19,7:2,-9:2,7:3,-9:9,7:7,48,7:14,-9:19,7:2,-9:2,7:3,-9:9,7:3," +
		"49,7:4,50,51,7:12,-9:19,7:2,-9:2,7:3,-9:9,7:5,52,7:12,53,7:3,-9:19,7:2,-9:2" +
		",7:3,-9:9,7:5,54,7:13,55,7:2,-9:19,7:2,-9:2,7:3,-9:9,7:2,56,7:8,57,7,58,7:8" +
		",-9:19,7:2,-9:2,7:3,-9:9,7:7,59,60,7,61,7:5,62,7:5,-9:19,7:2,-9:2,7:3,-9:9," +
		"7:12,63,7:9,-9:19,7:2,-9:2,7:3,-9:9,7:2,64,7:19,-9:19,7:2,-9:2,7:3,-9:9,7:5" +
		",65,7:6,66,7:9,-9:19,7:2,-9:2,7:3,-9:9,7:19,67,7:2,-9:19,7:2,-9:2,7:3,-9,-3" +
		"8:57,-41:30,68,-41:14,69,-41:11,-40:57,-42:57,-44:57,-45:44,70,-45:12,-46:3" +
		"6,71,-46:20,-47:57,-48:57,-49:57,-50:57,-51:57,-52:57,-1:43,72,-1:13,-56:44" +
		",73,74,-56:11,-63:44,75,-63:12,-62:44,76,-62:12,-64:57,-65:57,-10:50,38,-10" +
		":4,38,-10,-66:51,39,-66:4,39,-4:57,-7:57,-8:57,-5:3,77,-5:4,43:22,-5:19,43:" +
		"2,-5:2,43:3,-5,-6:50,44,-6:4,44,-6,-11:57,-1:6,6:2,78,-1,6,-1:5,6:3,-1,6,-1" +
		":4,6,-1:22,6,-1:3,6:2,-1,79,-1,-9:8,7,80,7,81,7:3,82,7:14,-9:19,7:2,-9:2,7:" +
		"3,-9:9,7:7,83,7:14,-9:19,7:2,-9:2,7:3,-9:9,7:7,84,7:14,-9:19,7:2,-9:2,7:3,-" +
		"9:9,7:6,85,7:15,-9:19,7:2,-9:2,7:3,-9:9,7:2,86,7:19,-9:19,7:2,-9:2,7:3,-9:9" +
		",7:3,87,7:11,88,7:6,-9:19,7:2,-9:2,7:3,-9:9,7:13,89,7:8,-9:19,7:2,-9:2,7:3," +
		"-9:9,7:18,90,7:3,-9:19,7:2,-9:2,7:3,-9:9,7:3,91,7:18,-9:19,7:2,-9:2,7:3,-9:" +
		"9,7:3,92,7:18,-9:19,7:2,-9:2,7:3,-9:9,7:12,93,7:9,-9:19,7:2,-9:2,7:3,-9:9,7" +
		":3,94,7:18,-9:19,7:2,-9:2,7:3,-9,-26:8,7:22,-26:19,7:2,-26:2,7:3,-26,-24:8," +
		"7:22,-24:19,7:2,-24:2,7:3,-24,-23:8,7:22,-23:19,7:2,-23:2,7:3,-23,-9:8,7:15" +
		",95,7:6,-9:19,7:2,-9:2,7:3,-9:9,7:5,96,7:16,-9:19,7:2,-9:2,7:3,-9:9,7:15,97" +
		",7:6,-9:19,7:2,-9:2,7:3,-9:9,7:16,98,7:5,-9:19,7:2,-9:2,7:3,-9:9,7:19,99,7:" +
		"2,-9:19,7:2,-9:2,7:3,-9:9,7:5,100,7:16,-9:19,7:2,-9:2,7:3,-9,-39:57,-58:57," +
		"-57:57,-54:57,-53:57,-55:57,-59:57,-61:57,-60:57,-1:50,101,-1:4,101,-1:10,1" +
		"02:2,-1:2,102:2,-1:3,102,-1:31,102,-1:2,102:3,-1:2,6:5,45,46,6:43,-1,6:3,10" +
		"3,6,-9:8,7:4,104,7:17,-9:19,7:2,-9:2,7:3,-9:9,7:3,105,7:18,-9:19,7:2,-9:2,7" +
		":3,-9:9,7:5,106,7:16,-9:19,7:2,-9:2,7:3,-9:9,7:5,107,7:16,-9:19,7:2,-9:2,7:" +
		"3,-9:9,7:5,108,7:16,-9:19,7:2,-9:2,7:3,-9,-15:8,7:22,-15:19,7:2,-15:2,7:3,-" +
		"15,-9:8,7:3,109,7:18,-9:19,7:2,-9:2,7:3,-9:9,7:10,110,7:11,-9:19,7:2,-9:2,7" +
		":3,-9:9,7:2,111,7:19,-9:19,7:2,-9:2,7:3,-9:9,7:17,112,7:4,-9:19,7:2,-9:2,7:" +
		"3,-9,-28:8,7:22,-28:19,7:2,-28:2,7:3,-28,-9:8,7:3,113,7:18,-9:19,7:2,-9:2,7" +
		":3,-9:9,7:7,114,7:14,-9:19,7:2,-9:2,7:3,-9,-19:8,7:5,115,7:16,-19:19,7:2,-1" +
		"9:2,7:3,-19,-9:8,7:5,116,7:16,-9:19,7:2,-9:2,7:3,-9:9,7:11,117,7:10,-9:19,7" +
		":2,-9:2,7:3,-9:9,7:15,118,7:6,-9:19,7:2,-9:2,7:3,-9,-27:8,7:22,-27:19,7:2,-" +
		"27:2,7:3,-27,-9:8,7:15,119,7:6,-9:19,7:2,-9:2,7:3,-9:9,7:5,120,7:16,-9:19,7" +
		":2,-9:2,7:3,-9:9,7:12,121,7:9,-9:19,7:2,-9:2,7:3,-9,-5:50,101,-5:4,101,-5,-" +
		"1,6:5,45,46,6,102:2,6:2,102:2,6:3,102,6:31,102,-1,6,102:3,6,-1,6:5,45,46,6:" +
		"43,-1,6:5,-9:8,7:5,122,7:16,-9:19,7:2,-9:2,7:3,-9,-12:8,7:22,-12:19,7:2,-12" +
		":2,7:3,-12,-14:8,7:22,-14:19,7:2,-14:2,7:3,-14,-9:8,7:12,123,7:9,-9:19,7:2," +
		"-9:2,7:3,-9,-16:8,7:22,-16:19,7:2,-16:2,7:3,-16,-17:8,7:22,-17:19,7:2,-17:2" +
		",7:3,-17,-35:8,7:22,-35:19,7:2,-35:2,7:3,-35,-9:8,7:12,124,7:9,-9:19,7:2,-9" +
		":2,7:3,-9:9,7,125,7:20,-9:19,7:2,-9:2,7:3,-9,-29:8,7:22,-29:19,7:2,-29:2,7:" +
		"3,-29,-9:8,7:5,126,7:16,-9:19,7:2,-9:2,7:3,-9:9,7:2,127,7:19,-9:19,7:2,-9:2" +
		",7:3,-9,-20:8,7:22,-20:19,7:2,-20:2,7:3,-20,-9:8,7:12,128,7:9,-9:19,7:2,-9:" +
		"2,7:3,-9,-22:8,7:22,-22:19,7:2,-22:2,7:3,-22,-9:8,7:3,129,7:18,-9:19,7:2,-9" +
		":2,7:3,-9,-34:8,7:22,-34:19,7:2,-34:2,7:3,-34,-9:8,7:21,130,-9:19,7:2,-9:2," +
		"7:3,-9:9,7:6,131,7:15,-9:19,7:2,-9:2,7:3,-9:9,7:17,132,7:4,-9:19,7:2,-9:2,7" +
		":3,-9:9,7:2,133,7:19,-9:19,7:2,-9:2,7:3,-9:9,7:4,134,7:17,-9:19,7:2,-9:2,7:" +
		"3,-9,-18:8,7:22,-18:19,7:2,-18:2,7:3,-18,-9:8,7,135,7:20,-9:19,7:2,-9:2,7:3" +
		",-9:9,7:17,136,7:4,-9:19,7:2,-9:2,7:3,-9:9,7:2,137,7:19,-9:19,7:2,-9:2,7:3," +
		"-9,-30:8,7:22,-30:19,7:2,-30:2,7:3,-30,-13:8,7:22,-13:19,7:2,-13:2,7:3,-13," +
		"-36:8,7:22,-36:19,7:2,-36:2,7:3,-36,-9:8,7:17,138,7:4,-9:19,7:2,-9:2,7:3,-9" +
		",-31:8,7:22,-31:19,7:2,-31:2,7:3,-31,-9:8,7:4,139,7:17,-9:19,7:2,-9:2,7:3,-" +
		"9,-25:8,7:22,-25:19,7:2,-25:2,7:3,-25,-9:8,7:17,140,7:4,-9:19,7:2,-9:2,7:3," +
		"-9:9,7:11,141,7:10,-9:19,7:2,-9:2,7:3,-9,-21:8,7:22,-21:19,7:2,-21:2,7:3,-2" +
		"1,-9:8,7:5,142,7:16,-9:19,7:2,-9:2,7:3,-9:9,7:12,143,7:9,-9:19,7:2,-9:2,7:3" +
		",-9,-33:8,7:22,-33:19,7:2,-33:2,7:3,-33,-32:8,7:22,-32:19,7:2,-32:2,7:3,-32");

	private static int[][] unpackFromString(int size1, int size2, String st) {
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
		if (chr >= 0 && chr < 128) {
			return lapg_char2no[chr];
		}
		return 1;
	}

	public LapgSymbol next() throws IOException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {
			lapg_n.offset = currOffset;
			tokenLine = lapg_n.line = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			int tokenStart = l - 1;

			for (state = group; state >= 0;) {
				state = lapg_lexem[state][mapCharacter(chr)];
				if (state >= -1 && chr != 0) {
					currOffset++;
					if (chr == '\n') {
						currLine++;
					}
					if (l >= datalen) {
						token.append(data, tokenStart, l - tokenStart);
						datalen = stream.read(data);
						tokenStart = l = 0;
					}
					chr = l < datalen ? data[l++] : 0;
				}
			}
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				if (chr == 0) {
					reporter.error(lapg_n.offset, lapg_n.endoffset, currLine, "Unexpected end of file reached");
					break;
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, currLine, MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.lexem = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.lexem = 0;
				lapg_n.sym = null;
				return lapg_n;
			}

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.lexem = lapg_lexemnum[-state - 3];
			lapg_n.sym = null;

		} while (lapg_n.lexem == -1 || !createToken(lapg_n, -state - 3));
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) {
		switch (lexemIndex) {
			case 2:
				 lapg_n.sym = token.toString().substring(1, token.length()); break; 
			case 3:
				 lapg_n.sym = Integer.parseInt(token.toString().substring(1, token.length())); break; 
			case 4:
				 deep = 1; group = 1; break; 
			case 6:
				 lapg_n.sym = current(); break; 
			case 7:
				 lapg_n.sym = Integer.parseInt(current()); break; 
			case 8:
				 lapg_n.sym = unescape(current(), 1, token.length()-1); break; 
			case 34:
				 deep++; break; 
			case 35:
				 if (--deep == 0) { group = 0; } break; 
			case 36:
				 group = 0; break; 
			case 63:
				 return false; 
		}
		return true;
	}
}
