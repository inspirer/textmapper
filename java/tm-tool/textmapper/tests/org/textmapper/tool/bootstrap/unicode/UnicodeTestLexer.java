/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
package org.textmapper.tool.bootstrap.unicode;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class UnicodeTestLexer {

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
		int identifier = 1;
		int icon = 2;
		int string = 3;
		int _skip = 4;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset);
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

	public UnicodeTestLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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

	private static final char[] tmCharClass = unpack_vc_char(125253,
		"\11\1\2\2\2\1\1\2\22\1\1\2\1\1\1\3\12\1\1\4\2\1\12\5\7\1\32\6\4\1\1\6\1\1\32\6\72" +
		"\1\1\7\51\1\30\7\1\1\10\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\2\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\2\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\2\1\1\7\1\1\1\7\1\1\3\7\2\1\1\7\1\1\1\7\2\1\1\7\3\1\2\7\4\1\1\7" +
		"\2\1\1\7\3\1\3\7\2\1\1\7\2\1\1\7\1\1\1\7\1\1\1\7\2\1\1\7\1\1\2\7\1\1\1\7\2\1\1\7" +
		"\3\1\1\7\1\1\1\7\2\1\2\7\2\1\3\7\6\1\1\7\2\1\1\7\2\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\2\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\2\7\2\1\1\7\1\1\1\7\3\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\7\7\2\1\1\7\2\1\2\7\1\1\1\7\4\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\105\7\1\1\33\7\301\1\1\7\1\1\1\7\3\1\1\7\3\1\3\7\22" +
		"\1\1\7\33\1\43\7\1\1\2\7\3\1\3\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\5\7\1\1\1\7\2\1\1\7\2\1\2\7\63\1\60" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\11\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\2\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\2\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\60\1\51" +
		"\7\u0b47\1\53\7\2\1\3\7\u02f8\1\6\7\u0882\1\11\7\167\1\54\7\77\1\15\7\1\1\42\7\146" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\11\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1" +
		"\1\1\7\1\1\1\7\1\1\1\7\1\1\11\7\10\1\6\7\12\1\10\7\10\1\10\7\10\1\6\7\12\1\10\7\10" +
		"\1\10\7\10\1\16\7\2\1\10\7\10\1\10\7\10\1\10\7\10\1\5\7\1\1\2\7\6\1\1\7\3\1\3\7\1" +
		"\1\2\7\10\1\4\7\2\1\2\7\10\1\10\7\12\1\3\7\1\1\2\7\u0112\1\1\7\3\1\2\7\3\1\1\7\33" +
		"\1\1\7\4\1\1\7\4\1\1\7\2\1\2\7\10\1\4\7\4\1\1\7\65\1\1\7\u0aab\1\60\7\1\1\1\7\3\1" +
		"\2\7\1\1\1\7\1\1\1\7\1\1\1\7\4\1\1\7\1\1\2\7\1\1\6\7\5\1\1\7\1\1\1\7\1\1\1\7\1\1" +
		"\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1" +
		"\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1" +
		"\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1" +
		"\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1" +
		"\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\2\7\7\1\1\7\1\1\1\7\4\1\1\7\14\1" +
		"\46\7\1\1\1\7\5\1\1\7\u7913\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\23\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7" +
		"\207\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\3\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\10\7\1\1\1" +
		"\7\1\1\1\7\2\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\4\1\1\7\1\1\1\7\2\1\1\7\1\1\3" +
		"\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1" +
		"\7\5\1\1\7\5\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\4\1\1" +
		"\7\1\1\1\7\6\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\1\1\1\7\34\1\1\7\3\1\1\7\u0335\1\53\7" +
		"\5\1\11\7\7\1\120\7\u4f40\1\7\7\14\1\5\7\u0429\1\32\7\u04cd\1\50\7\210\1\44\7\233" +
		"\1\13\7\1\1\17\7\1\1\7\7\1\1\2\7\u0703\1\63\7\u0bcd\1\40\7\u5580\1\40\7\u659a\1\32" +
		"\7\32\1\7\7\1\1\22\7\32\1\32\7\32\1\4\7\1\1\1\7\1\1\7\7\1\1\13\7\32\1\32\7\32\1\32" +
		"\7\32\1\32\7\32\1\32\7\32\1\32\7\32\1\32\7\32\1\32\7\32\1\32\7\32\1\34\7\34\1\31" +
		"\7\1\1\6\7\32\1\31\7\1\1\6\7\32\1\31\7\1\1\6\7\32\1\31\7\1\1\6\7\32\1\31\7\1\1\6" +
		"\7\1\1\1\7\u0734\1\12\7\1\1\24\7\6\1\6\7\u09f7\1\42\7\1\1");

	private static char[] unpack_vc_char(int size, String... st) {
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

	private static final int[] tmRuleSymbol = unpack_int(6,
		"\uffff\uffff\0\0\1\0\2\0\3\0\4\0");

	private static final int tmClassesCount = 8;

	private static final short[] tmGoto = unpack_vc_short(64,
		"\1\ufffe\1\uffff\1\7\1\4\1\3\1\2\1\1\1\uffff\5\ufffd\2\1\1\ufffd\5\ufffc\1\2\2\ufffc" +
		"\5\uffff\1\2\7\uffff\3\5\3\uffff\1\6\1\uffff\3\5\10\ufffb\2\ufffa\1\7\5\ufffa");

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
		if (chr >= 0 && chr < 125253) return tmCharClass[chr];
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

			token.symbol = tmRuleSymbol[-1 - state];
			token.value = null;

			if (token.symbol == -1) {
				reporter.error(MessageFormat.format("invalid token at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset);
			}

		} while (token.symbol == -1 || !createToken(token, -1 - state));
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
			case 2: // identifier: /[a-zA-Z_][a-zA-Z_0-9]*/
				{ token.value = tokenText(); }
				break;
			case 3: // icon: /\-?[0-9]+/
				{ token.value = Integer.parseInt(tokenText()); }
				break;
			case 4: // string: /"({schar})+"/
				{ token.value = tokenText(); }
				break;
			case 5: // _skip: /[\n\t\r ]+/
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
