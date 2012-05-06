/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.test.bootstrap.unicode;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class UnicodeTestLexer {

	public static class LapgSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int line;
		public int offset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int identifier = 1;
		public static final int icon = 2;
		public static final int string = 3;
		public static final int _skip = 4;
	}

	public interface ErrorReporter {
		void error(int start, int line, String s);
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

	public UnicodeTestLexer(Reader stream, ErrorReporter reporter) throws IOException {
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

	private static final char lapg_char2no[] = unpack_char2no(131072,
		"\1\0\10\1\2\7\2\1\1\7\22\1\1\7\1\1\1\3\12\1\1\2\2\1\12\5\7\1\32\4\4\1\1\4\1\1\32" +
		"\4\72\1\1\6\51\1\30\6\1\1\10\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\2\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\2\1\1\6\1\1\1\6\1\1\3\6\2\1\1\6\1\1\1\6\2\1\1\6\3\1\2\6\4" +
		"\1\1\6\2\1\1\6\3\1\3\6\2\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\2\1\1\6\1\1\2\6\1\1\1\6\2" +
		"\1\1\6\3\1\1\6\1\1\1\6\2\1\2\6\2\1\3\6\6\1\1\6\2\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\2\6\2\1\1\6\1\1\1\6\3\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\7\6\2\1\1\6\2\1\2\6\1\1\1\6\4" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\105\6\1\1\33\6\301\1\1\6\1\1\1\6\3\1\1\6\3\1\3" +
		"\6\22\1\1\6\33\1\43\6\1\1\2\6\3\1\3\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\5\6\1\1\1\6\2\1\1\6\2\1\2\6\63\1" +
		"\60\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\11\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\71\1\47\6\u1778\1\54\6\77\1\15\6\1\1" +
		"\42\6\146\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\11\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\11\6\10\1\6\6\12\1\10\6\10\1\10\6\10\1\6\6\12" +
		"\1\10\6\10\1\10\6\10\1\16\6\2\1\10\6\10\1\10\6\10\1\10\6\10\1\5\6\1\1\2\6\6\1\1\6" +
		"\3\1\3\6\1\1\2\6\10\1\4\6\2\1\2\6\10\1\10\6\12\1\3\6\1\1\2\6\u0112\1\1\6\3\1\2\6" +
		"\3\1\1\6\33\1\1\6\4\1\1\6\4\1\1\6\2\1\2\6\10\1\4\6\4\1\1\6\65\1\1\6\u0aab\1\57\6" +
		"\2\1\1\6\3\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\4\1\1\6\1\1\2\6\1\1\6\6\5\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\2\6\7\1\1\6\1\1\1\6" +
		"\4\1\1\6\14\1\46\6\1\1\1\6\5\1\1\6\u7913\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\23\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\213" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\3\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\10\6\1\1\1\6\1" +
		"\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\4\1\1\6\1\1\1\6\2\1\1\6\1\1\1\6\15" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\120\1\1\6\u5305\1\7\6\14\1\5\6\u0429\1\32" +
		"\6\u04cd\1\50\6\ucfca\1\32\6\32\1\7\6\1\1\22\6\32\1\32\6\32\1\4\6\1\1\1\6\1\1\7\6" +
		"\1\1\13\6\32\1\32\6\32\1\32\6\32\1\32\6\32\1\32\6\32\1\32\6\32\1\32\6\32\1\32\6\32" +
		"\1\32\6\32\1\34\6\34\1\31\6\1\1\6\6\32\1\31\6\1\1\6\6\32\1\31\6\1\1\6\6\32\1\31\6" +
		"\1\1\6\6\32\1\31\6\1\1\6\6\1\1\1\6\u2834\1");

	private static char[] unpack_char2no(int size, String... st) {
		char[] res = new char[size];
		int t = 0;
		int count = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; ) {
				count = i > 0 || count == 0 ? s.charAt(i++) : count;
				if (i < slen) {
					char val = s.charAt(i++);
					while (count-- > 0) res[t++] = val;
				}
			}
		}
		assert res.length == t;
		return res;
	}

	private static final short[] lapg_lexemnum = unpack_short(4,
		"\1\2\3\4");

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, -1, 1, 2, 3, 4, -1, 5},
		{ -1, -1, -1, -1, -1, 4, -1, -1},
		{ -1, -1, -1, -1, 6, 6, 6, -1},
		{ -3, -3, -3, -3, 3, 3, -3, -3},
		{ -4, -4, -4, -4, -4, 4, -4, -4},
		{ -6, -6, -6, -6, -6, -6, -6, 5},
		{ -1, -1, -1, 7, 6, 6, 6, -1},
		{ -5, -5, -5, -5, -5, -5, -5, -5}
	};

	private static int mapCharacter(int chr) {
		if (chr >= 0 && chr < 131072) {
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
					lapg_n.lexem = 0;
					lapg_n.sym = null;
					reporter.error(lapg_n.offset, lapg_n.line, "Unexpected end of input reached");
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

			if (state == -1) {
				if (l - 1 > tokenStart) {
					token.append(data, tokenStart, l - 1 - tokenStart);
				}
				reporter.error(lapg_n.offset, lapg_n.line, MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
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
			case 0:
				 lapg_n.sym = current(); break; 
			case 1:
				 lapg_n.sym = Integer.parseInt(current()); break; 
			case 2:
				 lapg_n.sym = current(); break; 
			case 3:
				 return false; 
		}
		return true;
	}

	/* package */ static int[] unpack_int(int size, String... st) {
		int[] res = new int[size];
		boolean second = false;
		char first = 0;
		int t = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; i++) {
				if (second) {
					res[t++] = (s.charAt(i) << 16) + first;
				} else {
					first = s.charAt(i);
				}
				second = !second;
			}
		}
		assert !second;
		assert res.length == t;
		return res;
	}

	/* package */ static short[] unpack_short(int size, String... st) {
		short[] res = new short[size];
		int t = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; i++) {
				res[t++] = (short) s.charAt(i);
			}
		}
		assert res.length == t;
		return res;
	}
}
