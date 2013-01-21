/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.tool.test.bootstrap.b;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class SampleBLexer {

	public static class LapgSymbol {
		public Object value;
		public int symbol;
		public int state;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int identifier = 1;
		public static final int _skip = 2;
		public static final int Lclass = 3;
		public static final int Lextends = 4;
		public static final int LCURLY = 5;
		public static final int RCURLY = 6;
		public static final int LPAREN = 7;
		public static final int RPAREN = 8;
		public static final int Linterface = 9;
		public static final int Lenum = 10;
		public static final int error = 11;
		public static final int numeric = 12;
		public static final int octal = 13;
		public static final int decimal = 14;
		public static final int eleven = 15;
		public static final int _skipSoftKW = 16;
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

	private int state;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;

	public SampleBLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.stream = stream;
		this.state = 0;
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
		return state;
	}

	public void setState(int state) {
		this.state = state;
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 12, 12, 1, 1, 12, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		12, 1, 1, 1, 1, 1, 1, 1, 6, 7, 1, 1, 1, 1, 1, 1,
		2, 11, 11, 11, 11, 11, 11, 11, 9, 9, 1, 1, 1, 1, 1, 1,
		1, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1, 1, 1, 1, 8,
		1, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 3, 8, 8, 4, 1, 5, 1, 1
	};

	private static final short[] lapg_lexemnum = unpack_short(15,
		"\1\14\15\16\2\3\4\5\6\7\10\11\12\17\20");

	private static final short[] lapg_lexem = unpack_vc_short(169,
		"\1\ufffe\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\2\1\7\1\2\1\7\1\10\2\uffff\1\11\1\12\7" +
		"\uffff\1\11\1\uffff\2\ufffd\2\2\4\ufffd\4\2\1\ufffd\15\ufff6\15\ufff5\15\ufff4\15" +
		"\ufff3\2\uffff\1\13\6\uffff\1\13\1\uffff\1\13\1\uffff\14\ufff9\1\10\2\ufffb\1\11" +
		"\10\ufffb\1\11\1\ufffb\2\uffff\1\14\6\uffff\3\14\1\uffff\2\ufffa\1\13\6\ufffa\1\13" +
		"\1\ufffa\1\13\1\ufffa\2\ufffc\1\14\6\ufffc\3\14\1\ufffc");

	private static short[] unpack_vc_short(int size, String... st) {
		short[] res = new short[size];
		int t = 0;
		int count = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; ) {
				count = i > 0 || count == 0 ? s.charAt(i++) : count;
				if (i < slen) {
					short val = (short) s.charAt(i++);
					while (count-- > 0) res[t++] = val;
				}
			}
		}
		assert res.length == t;
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
			tokenLine = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			tokenStart = l - 1;

			for (state = this.state; state >= 0; ) {
				state = lapg_lexem[state * 13 + mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.symbol = 0;
					lapg_n.value = null;
					reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), "Unexpected end of input reached");
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
				reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.symbol = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.symbol = 0;
				lapg_n.value = null;
				tokenStart = -1;
				return lapg_n;
			}

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.symbol = lapg_lexemnum[-state - 3];
			lapg_n.value = null;

		} while (lapg_n.symbol == -1 || !createToken(lapg_n, -state - 3));
		tokenStart = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) throws IOException {
		boolean spaceToken = false;
		switch (lexemIndex) {
			case 0:
				return createIdentifierToken(lapg_n, lexemIndex);
			case 1:
				return createNumericToken(lapg_n, lexemIndex);
			case 2:
				return createOctalToken(lapg_n, lexemIndex);
			case 3:
				return createDecimalToken(lapg_n, lexemIndex);
			case 4: // _skip: /[\n\t\r ]+/
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<String,Integer>();
	static {
		subTokensOfIdentifier.put("class", 5);
		subTokensOfIdentifier.put("extends", 6);
		subTokensOfIdentifier.put("interface", 11);
		subTokensOfIdentifier.put("enum", 12);
		subTokensOfIdentifier.put("xyzzz", 14);
	}

	protected boolean createIdentifierToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfIdentifier.get(current());
		if (replacement != null) {
			lexemIndex = replacement;
			lapg_n.symbol = lapg_lexemnum[lexemIndex];
		}
		boolean spaceToken = false;
		switch(lexemIndex) {
			case 5:	// class
				 lapg_n.value = "class"; 
				break;
			case 11:	// interface
				 lapg_n.value = "interface"; 
				break;
			case 12:	// enum
				 lapg_n.value = new Object(); 
				break;
			case 6:	// extends (soft)
			case 14:	// xyzzz (soft)
			case 0:	// <default>
				 lapg_n.value = current(); 
				break;
		}
		return !(spaceToken);
	}

	protected boolean createNumericToken(LapgSymbol lapg_n, int lexemIndex) {
		return true;
	}

	protected boolean createOctalToken(LapgSymbol lapg_n, int lexemIndex) {
		boolean spaceToken = false;
		switch(lexemIndex) {
			case 2:	// <default>
				 lapg_n.value = Integer.parseInt(current(), 8); 
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfDecimal = new HashMap<String,Integer>();
	static {
		subTokensOfDecimal.put("11", 13);
	}

	protected boolean createDecimalToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfDecimal.get(current());
		if (replacement != null) {
			lexemIndex = replacement;
			lapg_n.symbol = lapg_lexemnum[lexemIndex];
		}
		boolean spaceToken = false;
		switch(lexemIndex) {
			case 13:	// 11
				 lapg_n.value = 11; 
				break;
		}
		return !(spaceToken);
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
