/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.tool.parser.action;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public abstract class SActionLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int Lcurly = 1;
		int _skip = 2;
		int Rcurly = 3;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	private int chr;

	private int state;

	final private StringBuilder tokenBuffer = new StringBuilder(TOKEN_SIZE);

	private int tokenLine;
	private int currLine;
	private int currOffset;

	public SActionLexer(ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset();
	}

	public void reset() throws IOException {
		this.state = 0;
		tokenLine = currLine = 1;
		currOffset = 0;
		chr = nextChar();
	}
	protected abstract int nextChar() throws IOException;

	protected void advance() throws IOException {
		if (chr == -1) return;
		if (chr == '\n') {
			currLine++;
		}
		if (chr >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
			tokenBuffer.append(Character.toChars(chr));
		} else {
			tokenBuffer.append((char) chr);
		}
		chr = nextChar();
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

	public String tokenText() {
		return tokenBuffer.toString();
	}

	public int tokenSize() {
		return tokenBuffer.length();
	}

	private static final short tmCharClass[] = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 6, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 7, 1, 1
	};

	private static final int[] tmRuleSymbol = unpack_int(5,
		"\1\0\2\0\2\0\2\0\3\0");

	private static final int tmClassesCount = 8;

	private static final short[] tmGoto = unpack_vc_short(80,
		"\1\ufffe\1\1\1\2\1\3\2\1\1\4\1\5\1\ufffa\1\1\2\ufffa\2\1\2\ufffa\10\ufffd\1\uffff" +
		"\2\3\1\6\1\7\1\uffff\2\3\1\uffff\3\4\1\10\1\uffff\1\11\1\4\10\ufff9\10\ufffc\1\uffff" +
		"\4\3\1\uffff\2\3\1\uffff\4\4\1\uffff\2\4\10\ufffb");

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
		if (chr >= 0 && chr < 128) return tmCharClass[chr];
		return chr == -1 ? 0 : 1;
	}

	public Span next() throws IOException {
		Span token = new Span();
		int state;

		tokenloop:
		do {
			token.offset = currOffset;
			tokenLine = token.line = currLine;
			if (tokenBuffer.length() > TOKEN_SIZE) {
				tokenBuffer.setLength(TOKEN_SIZE);
				tokenBuffer.trimToSize();
			}
			tokenBuffer.setLength(0);

			for (state = this.state; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == -1) {
					token.symbol = 0;
					token.value = null;
					reporter.error("Unexpected end of input reached", token.line, token.offset);
					token.offset = currOffset;
					break tokenloop;
				}
				if (state >= -1 && chr != -1) {
					if (chr == '\n') {
						currLine++;
					}
					if (chr >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
						tokenBuffer.append(Character.toChars(chr));
					} else {
						tokenBuffer.append((char) chr);
					}
					chr = nextChar();
				}
			}

			if (state == -1) {
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset);
				token.symbol = -1;
				continue;
			}

			if (state == -2) {
				token.symbol = Tokens.eoi;
				token.value = null;
				break tokenloop;
			}

			token.symbol = tmRuleSymbol[-state - 3];
			token.value = null;

		} while (token.symbol == -1 || !createToken(token, -state - 3));
		return token;
	}

	protected boolean createToken(Span token, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 1: // _skip: /'([^\n\\']|\\.)*'/
				spaceToken = true;
				break;
			case 2: // _skip: /"([^\n\\"]|\\.)*"/
				spaceToken = true;
				break;
			case 3: // _skip: /[^'"{}]+/
				spaceToken = true;
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

}
