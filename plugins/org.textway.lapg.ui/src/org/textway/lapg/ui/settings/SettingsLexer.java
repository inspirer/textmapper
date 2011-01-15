package org.textway.lapg.ui.settings;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class SettingsLexer {

	public static class LapgSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int identifier = 1;
		public static final int scon = 2;
		public static final int _skip = 3;
		public static final int LSQUARE = 4;
		public static final int RSQUARE = 5;
		public static final int LPAREN = 6;
		public static final int RPAREN = 7;
		public static final int EQUAL = 8;
		public static final int COMMA = 9;
		public static final int Ldef = 10;
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
	    for (int i = start; i < end; i++) {
	        char c = s.charAt(i);
	        if (c == '\\') {
	            if (++i == end) {
	                break;
	            }
	            c = s.charAt(i);
	            if (c == 'u' || c == 'x') {
	                // FIXME process unicode
	            } else if (c == 'n') {
	                sb.append('\n');
	            } else if (c == 'r') {
	                sb.append('\r');
	            } else if (c == 't') {
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

	public SettingsLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 16, 4, 1, 1, 16, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		16, 1, 2, 1, 1, 1, 1, 1, 7, 8, 1, 1, 10, 14, 1, 1,
		15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 1, 1, 1, 9, 1, 1,
		1, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
		14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 5, 3, 6, 1, 14,
		1, 14, 14, 14, 11, 12, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14,
		14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 1, 1, 1, 1, 1
	};

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, -1, 1, -1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, -1, 2},
		{ -1, 1, 11, 12, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
		{ -5, -5, -5, -5, 2, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, 2},
		{ -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6},
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7},
		{ -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8},
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9},
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10},
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11},
		{ -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 10, 13, 10, 10, 10, -3},
		{ -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 10, 10, 10, 10, 10, -3},
		{ -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4},
		{ -1, 1, 1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
		{ -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 10, 10, 14, 10, 10, -3},
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, 10, 10, 10, 10, 10, -12}
	};

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
			tokenLine = currLine;
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
			case 1:
				 lapg_n.sym = current(); break; 
			case 2:
				 lapg_n.sym = unescape(current(), 1, token.length()-1); break; 
			case 3:
				 return false; 
		}
		return true;
	}
}
