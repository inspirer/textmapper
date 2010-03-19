package net.sf.lapg.templates.model.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class XmlLexer {

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
		public static final int LESS = 2;
		public static final int _skipcomment = 3;
		public static final int identifier = 4;
		public static final int ccon = 5;
		public static final int GREATER = 6;
		public static final int EQ = 7;
		public static final int COLON = 8;
		public static final int DIV = 9;
		public static final int _skip = 10;
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
	

	public XmlLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 14, 13, 1, 1, 14, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		14, 3, 6, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 4, 1, 10,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 9, 1, 2, 8, 5, 1,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 1, 1, 1, 1, 11,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 1, 1, 1, 1, 1,
	};

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, },
		{ -1, -1, -1, -1, -1, 4, 5, 6, 7, 8, 9, 10, -1, 11, 11, },
		{ -3, 2, -3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, },
		{ -4, -4, -4, 12, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, },
		{ -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, },
		{ -1, 5, 5, 5, 5, 5, 13, 5, 5, 5, 5, 5, 5, -1, 5, },
		{ -1, 6, 6, 6, 6, 6, 6, 14, 6, 6, 6, 6, 6, -1, 6, },
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, },
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, },
		{ -6, -6, -6, -6, 10, -6, -6, -6, -6, -6, -6, 10, 10, -6, -6, },
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, 11, 11, },
		{ -1, -1, -1, -1, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, },
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, },
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, },
		{ -1, -1, -1, -1, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, },
		{ -1, 16, 16, 16, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, },
		{ -1, 16, 16, 16, 18, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, },
		{ -1, 16, 16, 16, 16, 19, 16, 16, 16, 16, 16, 16, 16, 16, 16, },
		{ -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, },

	};

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
			case 2:
				 group = 1; break; 
			case 3:
				 return false; 
			case 4:
				 lapg_n.sym = current(); break; 
			case 5:
				 lapg_n.sym = token.toString().substring(1, token.length()-1); break; 
			case 6:
				 group = 0; break; 
			case 10:
				 return false; 
		}
		return true;
	}
}
