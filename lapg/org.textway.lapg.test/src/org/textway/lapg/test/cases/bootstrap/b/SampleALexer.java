package org.textway.lapg.test.cases.bootstrap.b;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class SampleALexer {

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
		public static final int _skip = 2;
		public static final int Lclass = 3;
		public static final int LCURLY = 4;
		public static final int RCURLY = 5;
		public static final int error = 6;
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
	

	public SampleALexer(Reader stream, ErrorReporter reporter) throws IOException {
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

	public int getLine() {
		return currLine;
	}

	public void setLine(int currLine) {
		this.currLine = currLine;
	}

	public int getOffset() {
		return currOffset;
	}

	public void setOffset(int currOffset) {
		this.currOffset = currOffset;
	}

	public String current() {
		return token.toString();
	}

	private static final short lapg_char2no[] = {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 10, 10, 1, 1, 10, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 1, 1, 1, 1, 1, 1,
		1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1, 1, 1, 1, 8,
		1, 4, 8, 2, 8, 8, 8, 8, 8, 8, 8, 8, 3, 8, 8, 8,
		8, 8, 8, 5, 8, 8, 8, 8, 8, 8, 8, 6, 1, 7, 1, 1
	};

	private static final short lapg_lexemnum[] = {
		1, 2, 3, 4, 5
	};

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, -1, 1, 2, 2, 2, 3, 4, 2, -1, 5},
		{ -3, -3, 2, 6, 2, 2, -3, -3, 2, 2, -3},
		{ -3, -3, 2, 2, 2, 2, -3, -3, 2, 2, -3},
		{ -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6},
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7},
		{ -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, 5},
		{ -3, -3, 2, 2, 7, 2, -3, -3, 2, 2, -3},
		{ -3, -3, 2, 2, 2, 8, -3, -3, 2, 2, -3},
		{ -3, -3, 2, 2, 2, 9, -3, -3, 2, 2, -3},
		{ -5, -5, 2, 2, 2, 2, -5, -5, 2, 2, -5}
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
					reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), "Unexpected end of file reached");
					break;
				}
				if (l - 1 > tokenStart) {
					token.append(data, tokenStart, l - 1 - tokenStart);
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
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
			case 0:
				 lapg_n.sym = current(); break; 
			case 1:
				 return false; 
		}
		return true;
	}
}
