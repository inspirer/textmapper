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
		public static final int RCURLY = 35;
		public static final int MINUSRCURLY = 36;
		public static final int PLUS = 37;
		public static final int MINUS = 38;
		public static final int MULT = 39;
		public static final int SLASH = 40;
		public static final int PERCENT = 41;
		public static final int EXCLAMATION = 42;
		public static final int OR = 43;
		public static final int LSQUARE = 44;
		public static final int RSQUARE = 45;
		public static final int LPAREN = 46;
		public static final int RPAREN = 47;
		public static final int DOT = 48;
		public static final int COMMA = 49;
		public static final int AMPERSANDAMPERSAND = 50;
		public static final int OROR = 51;
		public static final int EQUALEQUAL = 52;
		public static final int EQUAL = 53;
		public static final int EXCLAMATIONEQUAL = 54;
		public static final int MINUSGREATER = 55;
		public static final int EQUALGREATER = 56;
		public static final int LESSEQUAL = 57;
		public static final int GREATEREQUAL = 58;
		public static final int LESS = 59;
		public static final int GREATER = 60;
		public static final int COLON = 61;
		public static final int QUESTIONMARK = 62;
		public static final int _skip = 63;
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

	private int lapg_lexem[][] = unpackFromString(143,57,
		"-2,2,3,2:54,-1:5,4,5,-1,6,7,8,6:2,9,6,10,11,6,12,6:2,13,14,6,15,16,6:2,17,6" +
		",18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,6,37,38,-1,6:2,37" +
		",38,-3,2,-3,2:54,-1:2,39,-1,40,41,-1:2,42:22,-1:19,42,43,-1:2,42:2,43,-1,-4" +
		"2:57,-1,5:5,44,45,5:43,-1,5:5,-9:8,6:22,-9:19,6:2,-9:2,6:3,-9:9,6:2,46,6:19" +
		",-9:19,6:2,-9:2,6:3,-9:9,6:7,47,6:14,-9:19,6:2,-9:2,6:3,-9:9,6:3,48,6:4,49," +
		"50,6:12,-9:19,6:2,-9:2,6:3,-9:9,6:5,51,6:12,52,6:3,-9:19,6:2,-9:2,6:3,-9:9," +
		"6:5,53,6:13,54,6:2,-9:19,6:2,-9:2,6:3,-9:9,6:2,55,6:8,56,6,57,6:8,-9:19,6:2" +
		",-9:2,6:3,-9:9,6:7,58,59,6,60,6:5,61,6:5,-9:19,6:2,-9:2,6:3,-9:9,6:12,62,6:" +
		"9,-9:19,6:2,-9:2,6:3,-9:9,6:2,63,6:19,-9:19,6:2,-9:2,6:3,-9:9,6:5,64,6:6,65" +
		",6:9,-9:19,6:2,-9:2,6:3,-9:9,6:19,66,6:2,-9:19,6:2,-9:2,6:3,-9,-37:57,-40:3" +
		"0,67,-40:14,68,-40:11,-39:57,-41:57,-43:57,-44:44,69,-44:12,-45:36,70,-45:2" +
		"0,-46:57,-47:57,-48:57,-49:57,-50:57,-51:57,-1:43,71,-1:13,-55:44,72,73,-55" +
		":11,-62:44,74,-62:12,-61:44,75,-61:12,-63:57,-64:57,-10:50,37,-10:4,37,-10," +
		"-65:51,38,-65:4,38,-4:57,-7:57,-8:57,-5:3,76,-5:4,42:22,-5:19,42:2,-5:2,42:" +
		"3,-5,-6:50,43,-6:4,43,-6,-11:57,-1:6,5:2,77,-1,5,-1:5,5:3,-1,5,-1:4,5,-1:22" +
		",5,-1:3,5:2,-1,78,-1,-9:8,6,79,6,80,6:3,81,6:14,-9:19,6:2,-9:2,6:3,-9:9,6:7" +
		",82,6:14,-9:19,6:2,-9:2,6:3,-9:9,6:7,83,6:14,-9:19,6:2,-9:2,6:3,-9:9,6:6,84" +
		",6:15,-9:19,6:2,-9:2,6:3,-9:9,6:2,85,6:19,-9:19,6:2,-9:2,6:3,-9:9,6:3,86,6:" +
		"11,87,6:6,-9:19,6:2,-9:2,6:3,-9:9,6:13,88,6:8,-9:19,6:2,-9:2,6:3,-9:9,6:18," +
		"89,6:3,-9:19,6:2,-9:2,6:3,-9:9,6:3,90,6:18,-9:19,6:2,-9:2,6:3,-9:9,6:3,91,6" +
		":18,-9:19,6:2,-9:2,6:3,-9:9,6:12,92,6:9,-9:19,6:2,-9:2,6:3,-9:9,6:3,93,6:18" +
		",-9:19,6:2,-9:2,6:3,-9,-26:8,6:22,-26:19,6:2,-26:2,6:3,-26,-24:8,6:22,-24:1" +
		"9,6:2,-24:2,6:3,-24,-23:8,6:22,-23:19,6:2,-23:2,6:3,-23,-9:8,6:15,94,6:6,-9" +
		":19,6:2,-9:2,6:3,-9:9,6:5,95,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:15,96,6:6,-9:19" +
		",6:2,-9:2,6:3,-9:9,6:16,97,6:5,-9:19,6:2,-9:2,6:3,-9:9,6:19,98,6:2,-9:19,6:" +
		"2,-9:2,6:3,-9:9,6:5,99,6:16,-9:19,6:2,-9:2,6:3,-9,-38:57,-57:57,-56:57,-53:" +
		"57,-52:57,-54:57,-58:57,-60:57,-59:57,-1:50,100,-1:4,100,-1:10,101:2,-1:2,1" +
		"01:2,-1:3,101,-1:31,101,-1:2,101:3,-1:2,5:5,44,45,5:43,-1,5:3,102,5,-9:8,6:" +
		"4,103,6:17,-9:19,6:2,-9:2,6:3,-9:9,6:3,104,6:18,-9:19,6:2,-9:2,6:3,-9:9,6:5" +
		",105,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:5,106,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:5," +
		"107,6:16,-9:19,6:2,-9:2,6:3,-9,-15:8,6:22,-15:19,6:2,-15:2,6:3,-15,-9:8,6:3" +
		",108,6:18,-9:19,6:2,-9:2,6:3,-9:9,6:10,109,6:11,-9:19,6:2,-9:2,6:3,-9:9,6:2" +
		",110,6:19,-9:19,6:2,-9:2,6:3,-9:9,6:17,111,6:4,-9:19,6:2,-9:2,6:3,-9,-28:8," +
		"6:22,-28:19,6:2,-28:2,6:3,-28,-9:8,6:3,112,6:18,-9:19,6:2,-9:2,6:3,-9:9,6:7" +
		",113,6:14,-9:19,6:2,-9:2,6:3,-9,-19:8,6:5,114,6:16,-19:19,6:2,-19:2,6:3,-19" +
		",-9:8,6:5,115,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:11,116,6:10,-9:19,6:2,-9:2,6:3" +
		",-9:9,6:15,117,6:6,-9:19,6:2,-9:2,6:3,-9,-27:8,6:22,-27:19,6:2,-27:2,6:3,-2" +
		"7,-9:8,6:15,118,6:6,-9:19,6:2,-9:2,6:3,-9:9,6:5,119,6:16,-9:19,6:2,-9:2,6:3" +
		",-9:9,6:12,120,6:9,-9:19,6:2,-9:2,6:3,-9,-5:50,100,-5:4,100,-5,-1,5:5,44,45" +
		",5,101:2,5:2,101:2,5:3,101,5:31,101,-1,5,101:3,5,-1,5:5,44,45,5:43,-1,5:5,-" +
		"9:8,6:5,121,6:16,-9:19,6:2,-9:2,6:3,-9,-12:8,6:22,-12:19,6:2,-12:2,6:3,-12," +
		"-14:8,6:22,-14:19,6:2,-14:2,6:3,-14,-9:8,6:12,122,6:9,-9:19,6:2,-9:2,6:3,-9" +
		",-16:8,6:22,-16:19,6:2,-16:2,6:3,-16,-17:8,6:22,-17:19,6:2,-17:2,6:3,-17,-3" +
		"5:8,6:22,-35:19,6:2,-35:2,6:3,-35,-9:8,6:12,123,6:9,-9:19,6:2,-9:2,6:3,-9:9" +
		",6,124,6:20,-9:19,6:2,-9:2,6:3,-9,-29:8,6:22,-29:19,6:2,-29:2,6:3,-29,-9:8," +
		"6:5,125,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:2,126,6:19,-9:19,6:2,-9:2,6:3,-9,-20" +
		":8,6:22,-20:19,6:2,-20:2,6:3,-20,-9:8,6:12,127,6:9,-9:19,6:2,-9:2,6:3,-9,-2" +
		"2:8,6:22,-22:19,6:2,-22:2,6:3,-22,-9:8,6:3,128,6:18,-9:19,6:2,-9:2,6:3,-9,-" +
		"34:8,6:22,-34:19,6:2,-34:2,6:3,-34,-9:8,6:21,129,-9:19,6:2,-9:2,6:3,-9:9,6:" +
		"6,130,6:15,-9:19,6:2,-9:2,6:3,-9:9,6:17,131,6:4,-9:19,6:2,-9:2,6:3,-9:9,6:2" +
		",132,6:19,-9:19,6:2,-9:2,6:3,-9:9,6:4,133,6:17,-9:19,6:2,-9:2,6:3,-9,-18:8," +
		"6:22,-18:19,6:2,-18:2,6:3,-18,-9:8,6,134,6:20,-9:19,6:2,-9:2,6:3,-9:9,6:17," +
		"135,6:4,-9:19,6:2,-9:2,6:3,-9:9,6:2,136,6:19,-9:19,6:2,-9:2,6:3,-9,-30:8,6:" +
		"22,-30:19,6:2,-30:2,6:3,-30,-13:8,6:22,-13:19,6:2,-13:2,6:3,-13,-36:8,6:22," +
		"-36:19,6:2,-36:2,6:3,-36,-9:8,6:17,137,6:4,-9:19,6:2,-9:2,6:3,-9,-31:8,6:22" +
		",-31:19,6:2,-31:2,6:3,-31,-9:8,6:4,138,6:17,-9:19,6:2,-9:2,6:3,-9,-25:8,6:2" +
		"2,-25:19,6:2,-25:2,6:3,-25,-9:8,6:17,139,6:4,-9:19,6:2,-9:2,6:3,-9:9,6:11,1" +
		"40,6:10,-9:19,6:2,-9:2,6:3,-9,-21:8,6:22,-21:19,6:2,-21:2,6:3,-21,-9:8,6:5," +
		"141,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:12,142,6:9,-9:19,6:2,-9:2,6:3,-9,-33:8,6" +
		":22,-33:19,6:2,-33:2,6:3,-33,-32:8,6:22,-32:19,6:2,-32:2,6:3,-32");

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

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.lexem = - state - 2;
			lapg_n.sym = null;

		} while (lapg_n.lexem == -1 || !createToken(lapg_n));
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n) {
		switch (lapg_n.lexem) {
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
				 lapg_n.sym = unescape(current(), 1, token.length()-1); break; 
			case 35:
				 group = 0; break; 
			case 36:
				 group = 0; break; 
			case 63:
				 return false; 
		}
		return true;
	}
}
