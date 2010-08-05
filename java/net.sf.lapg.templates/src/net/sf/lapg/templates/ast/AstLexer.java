package net.sf.lapg.templates.ast;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class AstLexer {

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
		public static final int Lnull = 26;
		public static final int Lquery = 27;
		public static final int Lswitch = 28;
		public static final int Lseparator = 29;
		public static final int Ltemplate = 30;
		public static final int Ltrue = 31;
		public static final int Lself = 32;
		public static final int Lassert = 33;
		public static final int RCURLY = 34;
		public static final int MINUSRCURLY = 35;
		public static final int PLUS = 36;
		public static final int MINUS = 37;
		public static final int MULT = 38;
		public static final int SLASH = 39;
		public static final int PERCENT = 40;
		public static final int EXCLAMATION = 41;
		public static final int OR = 42;
		public static final int LSQUARE = 43;
		public static final int RSQUARE = 44;
		public static final int LPAREN = 45;
		public static final int RPAREN = 46;
		public static final int DOT = 47;
		public static final int COMMA = 48;
		public static final int AMPERSANDAMPERSAND = 49;
		public static final int OROR = 50;
		public static final int EQUALEQUAL = 51;
		public static final int EQUAL = 52;
		public static final int EXCLAMATIONEQUAL = 53;
		public static final int MINUSGREATER = 54;
		public static final int EQUALGREATER = 55;
		public static final int LESSEQUAL = 56;
		public static final int GREATEREQUAL = 57;
		public static final int LESS = 58;
		public static final int GREATER = 59;
		public static final int COLON = 60;
		public static final int QUESTIONMARK = 61;
		public static final int _skip = 62;
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

	public AstLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
		23, 27, 20, 15, 25, 26, 17, 29, 8, 28, 49, 4, 36, 30, 1, 1,
	};

	private int lapg_lexem[][] = unpackFromString(141,57,
		"-2,2,3,2:54,-1:5,4,5,-1,6,7,8,6:2,9,6,10,11,6,12,6:2,13,14,6,15,16,6,17,6:2" +
		",18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,6,37,38,-1,6:2,37" +
		",38,-3,2,-3,2:54,-1:2,39,-1,40,41,-1:2,42:22,-1:19,42,43,-1:2,42:2,43,-1,-4" +
		"1:57,-1,5:5,44,45,5:43,-1,5:5,-9:8,6:22,-9:19,6:2,-9:2,6:3,-9:9,6:2,46,6:19" +
		",-9:19,6:2,-9:2,6:3,-9:9,6:7,47,6:14,-9:19,6:2,-9:2,6:3,-9:9,6:3,48,6:4,49," +
		"50,6:12,-9:19,6:2,-9:2,6:3,-9:9,6:5,51,6:15,52,-9:19,6:2,-9:2,6:3,-9:9,6:18" +
		",53,6:3,-9:19,6:2,-9:2,6:3,-9:9,6:2,54,6:8,55,6,56,6:8,-9:19,6:2,-9:2,6:3,-" +
		"9:9,6:7,57,58,6,59,6:5,60,6:5,-9:19,6:2,-9:2,6:3,-9:9,6:12,61,6:9,-9:19,6:2" +
		",-9:2,6:3,-9:9,6:2,62,6:19,-9:19,6:2,-9:2,6:3,-9:9,6:5,63,6:6,64,6:9,-9:19," +
		"6:2,-9:2,6:3,-9:9,6:18,65,6:3,-9:19,6:2,-9:2,6:3,-9,-36:57,-39:30,66,-39:14" +
		",67,-39:11,-38:57,-40:57,-42:57,-43:44,68,-43:12,-44:36,69,-44:20,-45:57,-4" +
		"6:57,-47:57,-48:57,-49:57,-50:57,-1:43,70,-1:13,-54:44,71,72,-54:11,-61:44," +
		"73,-61:12,-60:44,74,-60:12,-62:57,-63:57,-10:50,37,-10:4,37,-10,-64:51,38,-" +
		"64:4,38,-4:57,-7:57,-8:57,-5:3,75,-5:4,42:22,-5:19,42:2,-5:2,42:3,-5,-6:50," +
		"43,-6:4,43,-6,-11:57,-1:6,5:2,76,-1,5,-1:5,5:3,-1,5,-1:4,5,-1:22,5,-1:3,5:2" +
		",-1,77,-1,-9:8,6,78,6,79,6:3,80,6:14,-9:19,6:2,-9:2,6:3,-9:9,6:7,81,6:14,-9" +
		":19,6:2,-9:2,6:3,-9:9,6:7,82,6:14,-9:19,6:2,-9:2,6:3,-9:9,6:6,83,6:15,-9:19" +
		",6:2,-9:2,6:3,-9:9,6:2,84,6:19,-9:19,6:2,-9:2,6:3,-9:9,6:3,85,6:11,86,6:6,-" +
		"9:19,6:2,-9:2,6:3,-9:9,6:13,87,6:8,-9:19,6:2,-9:2,6:3,-9:9,6:3,88,6:18,-9:1" +
		"9,6:2,-9:2,6:3,-9:9,6:3,89,6:18,-9:19,6:2,-9:2,6:3,-9:9,6:12,90,6:9,-9:19,6" +
		":2,-9:2,6:3,-9:9,6:3,91,6:18,-9:19,6:2,-9:2,6:3,-9,-26:8,6:22,-26:19,6:2,-2" +
		"6:2,6:3,-26,-24:8,6:22,-24:19,6:2,-24:2,6:3,-24,-23:8,6:22,-23:19,6:2,-23:2" +
		",6:3,-23,-9:8,6:15,92,6:6,-9:19,6:2,-9:2,6:3,-9:9,6:5,93,6:16,-9:19,6:2,-9:" +
		"2,6:3,-9:9,6:15,94,6:6,-9:19,6:2,-9:2,6:3,-9:9,6:16,95,6:5,-9:19,6:2,-9:2,6" +
		":3,-9:9,6:18,96,6:3,-9:19,6:2,-9:2,6:3,-9:9,6:5,97,6:16,-9:19,6:2,-9:2,6:3," +
		"-9,-37:57,-56:57,-55:57,-52:57,-51:57,-53:57,-57:57,-59:57,-58:57,-1:50,98," +
		"-1:4,98,-1:10,99:2,-1:2,99:2,-1:3,99,-1:31,99,-1:2,99:3,-1:2,5:5,44,45,5:43" +
		",-1,5:3,100,5,-9:8,6:4,101,6:17,-9:19,6:2,-9:2,6:3,-9:9,6:3,102,6:18,-9:19," +
		"6:2,-9:2,6:3,-9:9,6:5,103,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:5,104,6:16,-9:19,6" +
		":2,-9:2,6:3,-9:9,6:5,105,6:16,-9:19,6:2,-9:2,6:3,-9,-15:8,6:22,-15:19,6:2,-" +
		"15:2,6:3,-15,-9:8,6:3,106,6:18,-9:19,6:2,-9:2,6:3,-9:9,6:10,107,6:11,-9:19," +
		"6:2,-9:2,6:3,-9:9,6:2,108,6:19,-9:19,6:2,-9:2,6:3,-9:9,6:17,109,6:4,-9:19,6" +
		":2,-9:2,6:3,-9:9,6:3,110,6:18,-9:19,6:2,-9:2,6:3,-9:9,6:7,111,6:14,-9:19,6:" +
		"2,-9:2,6:3,-9,-19:8,6:5,112,6:16,-19:19,6:2,-19:2,6:3,-19,-9:8,6:5,113,6:16" +
		",-9:19,6:2,-9:2,6:3,-9:9,6:11,114,6:10,-9:19,6:2,-9:2,6:3,-9:9,6:15,115,6:6" +
		",-9:19,6:2,-9:2,6:3,-9,-27:8,6:22,-27:19,6:2,-27:2,6:3,-27,-9:8,6:15,116,6:" +
		"6,-9:19,6:2,-9:2,6:3,-9:9,6:5,117,6:16,-9:19,6:2,-9:2,6:3,-9:9,6:12,118,6:9" +
		",-9:19,6:2,-9:2,6:3,-9,-5:50,98,-5:4,98,-5,-1,5:5,44,45,5,99:2,5:2,99:2,5:3" +
		",99,5:31,99,-1,5,99:3,5,-1,5:5,44,45,5:43,-1,5:5,-9:8,6:5,119,6:16,-9:19,6:" +
		"2,-9:2,6:3,-9,-12:8,6:22,-12:19,6:2,-12:2,6:3,-12,-14:8,6:22,-14:19,6:2,-14" +
		":2,6:3,-14,-9:8,6:12,120,6:9,-9:19,6:2,-9:2,6:3,-9,-16:8,6:22,-16:19,6:2,-1" +
		"6:2,6:3,-16,-17:8,6:22,-17:19,6:2,-17:2,6:3,-17,-34:8,6:22,-34:19,6:2,-34:2" +
		",6:3,-34,-9:8,6:12,121,6:9,-9:19,6:2,-9:2,6:3,-9:9,6,122,6:20,-9:19,6:2,-9:" +
		"2,6:3,-9,-28:8,6:22,-28:19,6:2,-28:2,6:3,-28,-9:8,6:5,123,6:16,-9:19,6:2,-9" +
		":2,6:3,-9:9,6:2,124,6:19,-9:19,6:2,-9:2,6:3,-9,-20:8,6:22,-20:19,6:2,-20:2," +
		"6:3,-20,-9:8,6:12,125,6:9,-9:19,6:2,-9:2,6:3,-9,-22:8,6:22,-22:19,6:2,-22:2" +
		",6:3,-22,-9:8,6:3,126,6:18,-9:19,6:2,-9:2,6:3,-9,-33:8,6:22,-33:19,6:2,-33:" +
		"2,6:3,-33,-9:8,6:20,127,6,-9:19,6:2,-9:2,6:3,-9:9,6:6,128,6:15,-9:19,6:2,-9" +
		":2,6:3,-9:9,6:17,129,6:4,-9:19,6:2,-9:2,6:3,-9:9,6:2,130,6:19,-9:19,6:2,-9:" +
		"2,6:3,-9:9,6:4,131,6:17,-9:19,6:2,-9:2,6:3,-9,-18:8,6:22,-18:19,6:2,-18:2,6" +
		":3,-18,-9:8,6,132,6:20,-9:19,6:2,-9:2,6:3,-9:9,6:17,133,6:4,-9:19,6:2,-9:2," +
		"6:3,-9:9,6:2,134,6:19,-9:19,6:2,-9:2,6:3,-9,-29:8,6:22,-29:19,6:2,-29:2,6:3" +
		",-29,-13:8,6:22,-13:19,6:2,-13:2,6:3,-13,-35:8,6:22,-35:19,6:2,-35:2,6:3,-3" +
		"5,-9:8,6:17,135,6:4,-9:19,6:2,-9:2,6:3,-9,-30:8,6:22,-30:19,6:2,-30:2,6:3,-" +
		"30,-9:8,6:4,136,6:17,-9:19,6:2,-9:2,6:3,-9,-25:8,6:22,-25:19,6:2,-25:2,6:3," +
		"-25,-9:8,6:17,137,6:4,-9:19,6:2,-9:2,6:3,-9:9,6:11,138,6:10,-9:19,6:2,-9:2," +
		"6:3,-9,-21:8,6:22,-21:19,6:2,-21:2,6:3,-21,-9:8,6:5,139,6:16,-9:19,6:2,-9:2" +
		",6:3,-9:9,6:12,140,6:9,-9:19,6:2,-9:2,6:3,-9,-32:8,6:22,-32:19,6:2,-32:2,6:" +
		"3,-32,-31:8,6:22,-31:19,6:2,-31:2,6:3,-31");

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
			int tokenStart = l-1;

			for( state = group; state >= 0; ) {
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

			lapg_n.lexem = -state-2;
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
			case 34:
				 group = 0; break; 
			case 35:
				 group = 0; break; 
			case 62:
				 return false; 
		}
		return true;
	}
}
