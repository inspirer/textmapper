/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.xml;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class XmlLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface States {
		int initial = 0;
		int inTag = 1;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int any = 1;
		int Lt = 2;
		int _skipcomment = 3;
		int identifier = 4;
		int ccon = 5;
		int Gt = 6;
		int Assign = 7;
		int Colon = 8;
		int Div = 9;
		int _skip = 10;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset, int endoffset);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	private CharSequence input;
	private int tokenOffset;
	private int l;
	private int charOffset;
	private int chr;

	private int state;

	private int tokenLine;
	private int currLine;
	private int currOffset;

	public XmlLexer(CharSequence input, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(input);
	}

	public void reset(CharSequence input) throws IOException {
		this.state = 0;
		tokenLine = currLine = 1;
		currOffset = 0;
		this.input = input;
		tokenOffset = l = 0;
		charOffset = l;
		chr = l < input.length() ? input.charAt(l++) : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < input.length() &&
				Character.isLowSurrogate(input.charAt(l))) {
			chr = Character.toCodePoint((char) chr, input.charAt(l++));
		}
	}

	protected void advance() {
		if (chr == -1) return;
		currOffset += l - charOffset;
		if (chr == '\n') {
			currLine++;
		}
		charOffset = l;
		chr = l < input.length() ? input.charAt(l++) : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < input.length() &&
				Character.isLowSurrogate(input.charAt(l))) {
			chr = Character.toCodePoint((char) chr, input.charAt(l++));
		}
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
		return input.subSequence(tokenOffset, charOffset).toString();
	}

	public int tokenSize() {
		return charOffset - tokenOffset;
	}

	private static final short tmCharClass[] = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 14, 13, 1, 1, 14, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		14, 3, 6, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 4, 1, 10,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 9, 1, 2, 8, 5, 1,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 1, 1, 1, 1, 11,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11
	};

	private static final short tmStateMap[] = {
		0, 9
	};

	private static final short tmBacktracking[] = {
		1, 2
	};

	private static final int tmFirstRule = -4;

	private static final int[] tmRuleSymbol = unpack_int(11,
		"\1\0\2\0\3\0\4\0\5\0\5\0\6\0\7\0\10\0\11\0\12\0");

	private static final int tmClassesCount = 15;

	private static final short[] tmGoto = unpack_vc_short(300,
		"\1\ufffe\1\10\1\1\14\10\3\ufffb\1\ufffd\13\ufffb\4\uffff\1\3\16\uffff\1\4\13\uffff" +
		"\3\4\1\5\12\4\1\uffff\3\4\1\6\12\4\1\uffff\4\4\1\7\11\4\17\ufffa\1\ufffc\1\10\1\ufffc" +
		"\14\10\5\uffff\1\23\1\21\1\17\1\16\1\15\1\14\1\13\1\uffff\2\12\15\ufff2\2\12\4\ufff9" +
		"\1\13\6\ufff9\2\13\2\ufff9\17\ufff3\17\ufff4\17\ufff5\1\uffff\6\17\1\20\5\17\1\uffff" +
		"\1\17\17\ufff7\1\uffff\5\21\1\22\6\21\1\uffff\1\21\17\ufff8\17\ufff6");

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
		if (chr >= 0 && chr < 123) return tmCharClass[chr];
		return chr == -1 ? 0 : 1;
	}

	public Span next() throws IOException {
		Span token = new Span();
		int state;

		tokenloop:
		do {
			token.offset = currOffset;
			tokenLine = token.line = currLine;
			tokenOffset = charOffset;

			// TODO use backupRule
			int backupRule = -1;
			for (state = tmStateMap[this.state]; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state > tmFirstRule && state < -2) {
					token.endoffset = currOffset;
					state = (-3 - state) * 2;
					backupRule = tmBacktracking[state++];
					state = tmBacktracking[state];
				}
				if (state == -1 && chr == -1) {
					token.endoffset = currOffset;
					token.symbol = 0;
					token.value = null;
					reporter.error("Unexpected end of input reached", token.line, token.offset, token.endoffset);
					token.offset = currOffset;
					break tokenloop;
				}
				if (state >= -1 && chr != -1) {
					currOffset += l - charOffset;
					if (chr == '\n') {
						currLine++;
					}
					charOffset = l;
					chr = l < input.length() ? input.charAt(l++) : -1;
					if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < input.length() &&
							Character.isLowSurrogate(input.charAt(l))) {
						chr = Character.toCodePoint((char) chr, input.charAt(l++));
					}
				}
			}
			token.endoffset = currOffset;

			if (state == -1) {
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset, token.endoffset);
				token.symbol = -1;
				continue;
			}

			if (state == -2) {
				token.symbol = Tokens.eoi;
				token.value = null;
				break tokenloop;
			}

			token.symbol = tmRuleSymbol[tmFirstRule - state];
			token.value = null;

		} while (token.symbol == -1 || !createToken(token, tmFirstRule - state));
		return token;
	}

	protected int charAt(int i) {
		if (i == 0) return chr;
		i += l - 1;
		int res = i < input.length() ? input.charAt(i++) : -1;
		if (res >= Character.MIN_HIGH_SURROGATE && res <= Character.MAX_HIGH_SURROGATE && i < input.length() &&
				Character.isLowSurrogate(input.charAt(i))) {
			res = Character.toCodePoint((char) res, input.charAt(i++));
		}
		return res;
	}

	protected boolean createToken(Span token, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 1: // '<': /</
				state = States.inTag;
				break;
			case 2: // _skipcomment: /<!\-\-([^\-]|\-[^\-]|\-\-[^>])*\-\->/
				spaceToken = true;
				break;
			case 3: // identifier: /[a-zA-Z_][A-Za-z_0-9\-]*/
				{ token.value = tokenText(); }
				break;
			case 4: // ccon: /"[^\n"]*"/
				{ token.value = tokenText().substring(1, tokenSize()-1); }
				break;
			case 5: // ccon: /'[^\n']*'/
				{ token.value = tokenText().substring(1, tokenSize()-1); }
				break;
			case 6: // '>': />/
				state = States.initial;
				break;
			case 10: // _skip: /[\t\r\n ]+/
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
