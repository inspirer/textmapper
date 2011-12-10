/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.xml;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class XmlLexer {

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
		public static final int LESS = 2;
		public static final int _skipcomment = 3;
		public static final int identifier = 4;
		public static final int ccon = 5;
		public static final int GREATER = 6;
		public static final int EQUAL = 7;
		public static final int COLON = 8;
		public static final int SLASH = 9;
		public static final int _skip = 10;
	}

	public interface ErrorReporter {
		void error(int start, int end, int line, String s);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen, l, tokenStart;
	private char chr;

	private int group;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;


	public XmlLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.stream = stream;
		this.group = 0;
		datalen = stream.read(data);
		l = 0;
		tokenStart = -1;
		chr = l < datalen ? data[l++] : 0;
	}

	protected void advance() throws IOException {
		if (chr == 0) return;
		currOffset++;
		if (chr == '\n') {
			currLine++;
		}
		if (l >= datalen) {
			if (tokenStart >= 0) {
				token.append(data, tokenStart, l - tokenStart);
				tokenStart = 0;
			}
			l = 0;
			datalen = stream.read(data);
		}
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 14, 13, 1, 1, 14, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		14, 3, 6, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 4, 1, 10,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 9, 1, 2, 8, 5, 1,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 1, 1, 1, 1, 11,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 1, 1, 1, 1, 1
	};

	private static final short lapg_lexemnum[] = {
		1, 2, 3, 4, 5, 5, 6, 7, 8, 9, 10
	};

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
		{ -1, -1, -1, -1, -1, 4, 5, 6, 7, 8, 9, 10, -1, 11, 11},
		{ -3, 2, -3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
		{ -4, -4, -4, 12, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4},
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9},
		{ -1, 5, 5, 5, 5, 5, 13, 5, 5, 5, 5, 5, 5, -1, 5},
		{ -1, 6, 6, 6, 6, 6, 6, 14, 6, 6, 6, 6, 6, -1, 6},
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10},
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11},
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12},
		{ -6, -6, -6, -6, 10, -6, -6, -6, -6, -6, -6, 10, 10, -6, -6},
		{ -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, 11, 11},
		{ -1, -1, -1, -1, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7},
		{ -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8},
		{ -1, -1, -1, -1, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, 16, 16, 16, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16},
		{ -1, 16, 16, 16, 18, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16},
		{ -1, 16, 16, 16, 16, 19, 16, 16, 16, 16, 16, 16, 16, 16, 16},
		{ -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5}
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
			tokenLine = lapg_n.line = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			tokenStart = l - 1;

			for (state = group; state >= 0; ) {
				state = lapg_lexem[state][mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.lexem = 0;
					lapg_n.sym = null;
					reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, "Unexpected end of input reached");
					tokenStart = -1;
					return lapg_n;
				}
				if (state >= -1 && chr != 0) {
					currOffset++;
					if (chr == '\n') {
						currLine++;
					}
					if (l >= datalen) {
						token.append(data, tokenStart, l - tokenStart);
						tokenStart = l = 0;
						datalen = stream.read(data);
					}
					chr = l < datalen ? data[l++] : 0;
				}
			}
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				if (l - 1 > tokenStart) {
					token.append(data, tokenStart, l - 1 - tokenStart);
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.lexem = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.lexem = 0;
				lapg_n.sym = null;
				tokenStart = -1;
				return lapg_n;
			}

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.lexem = lapg_lexemnum[-state - 3];
			lapg_n.sym = null;

		} while (lapg_n.lexem == -1 || !createToken(lapg_n, -state - 3));
		tokenStart = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) throws IOException {
		switch (lexemIndex) {
			case 1:
				 group = 1; break; 
			case 2:
				 return false; 
			case 3:
				 lapg_n.sym = current(); break; 
			case 4:
				 lapg_n.sym = token.toString().substring(1, token.length()-1); break; 
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
