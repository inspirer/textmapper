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
package org.textmapper.tool.test.bootstrap.unicode;

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
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int identifier = 1;
		public static final int icon = 2;
		public static final int string = 3;
		public static final int _skip = 4;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen;
	private int tokenOffset;
	private int l;
	private int charOffset;
	private int chr;

	private int state;

	final private StringBuilder tokenBuffer = new StringBuilder(TOKEN_SIZE);

	private int tokenLine;
	private int currLine;
	private int currOffset;

	public UnicodeTestLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.state = 0;
		tokenLine = currLine = 1;
		currOffset = 0;
		this.stream = stream;
		datalen = stream.read(data);
		l = 0;
		tokenOffset = -1;
		if (l + 1 >= datalen) {
			if (l < datalen) {
				data[0] = data[l];
				datalen = Math.max(stream.read(data, 1, data.length - 1) + 1, 1);
			} else {
				datalen = stream.read(data);
			}
			l = 0;
		}
		charOffset = l;
		chr = l < datalen ? data[l++] : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < datalen &&
				Character.isLowSurrogate(data[l])) {
			chr = Character.toCodePoint((char) chr, data[l++]);
		}
	}

	protected void advance() throws IOException {
		if (chr == -1) return;
		currOffset += l - charOffset;
		if (chr == '\n') {
			currLine++;
		}
		if (l + 1 >= datalen) {
			if (tokenOffset >= 0) {
				tokenBuffer.append(data, tokenOffset, l - tokenOffset);
				tokenOffset = 0;
			}
			if (l < datalen) {
				data[0] = data[l];
				datalen = Math.max(stream.read(data, 1, data.length - 1) + 1, 1);
			} else {
				datalen = stream.read(data);
			}
			l = 0;
		}
		charOffset = l;
		chr = l < datalen ? data[l++] : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < datalen &&
				Character.isLowSurrogate(data[l])) {
			chr = Character.toCodePoint((char) chr, data[l++]);
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
		return tokenBuffer.toString();
	}

	public int tokenSize() {
		return tokenBuffer.length();
	}

	private static final char[] tmCharClass = unpack_vc_char(131072,
		"\11\1\2\7\2\1\1\7\22\1\1\7\1\1\1\3\12\1\1\2\2\1\12\5\7\1\32\4\4\1\1\4\1\1\32\4\72" +
		"\1\1\6\51\1\30\6\1\1\10\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\2\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\2\1\1\6\1\1\1\6\1\1\3\6\2\1\1\6\1\1\1\6\2\1\1\6\3\1\2\6\4\1\1\6" +
		"\2\1\1\6\3\1\3\6\2\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\2\1\1\6\1\1\2\6\1\1\1\6\2\1\1\6" +
		"\3\1\1\6\1\1\1\6\2\1\2\6\2\1\3\6\6\1\1\6\2\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\2\6\2\1\1\6\1\1\1\6\3\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\7\6\2\1\1\6\2\1\2\6\1\1\1\6\4\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\105\6\1\1\33\6\301\1\1\6\1\1\1\6\3\1\1\6\3\1\3\6\22" +
		"\1\1\6\33\1\43\6\1\1\2\6\3\1\3\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6" +
		"\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\5\6\1\1\1\6\2\1\1\6\2\1\2\6\63\1\60" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\11\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1" +
		"\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\61\1\47" +
		"\6\u1778\1\54\6\77\1\15\6\1\1\42\6\146\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\11\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\11\6\10\1\6\6\12" +
		"\1\10\6\10\1\10\6\10\1\6\6\12\1\10\6\10\1\10\6\10\1\16\6\2\1\10\6\10\1\10\6\10\1" +
		"\10\6\10\1\5\6\1\1\2\6\6\1\1\6\3\1\3\6\1\1\2\6\10\1\4\6\2\1\2\6\10\1\10\6\12\1\3" +
		"\6\1\1\2\6\u0112\1\1\6\3\1\2\6\3\1\1\6\33\1\1\6\4\1\1\6\4\1\1\6\2\1\2\6\10\1\4\6" +
		"\4\1\1\6\65\1\1\6\u0aab\1\57\6\2\1\1\6\3\1\2\6\1\1\1\6\1\1\1\6\1\1\1\6\4\1\1\6\1" +
		"\1\2\6\1\1\6\6\5\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\2\6\7\1\1\6\1\1\1\6\4\1\1\6\14\1\46\6\1\1\1\6\5\1\1\6\u7913\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\23\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1" +
		"\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\207\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\3\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\10\6\1\1\1\6\1\1\1\6\2\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\4\1\1\6\1\1\1\6\2\1\1\6\1\1\3\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1" +
		"\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\1\1\1\6\120\1\1\6\u0335\1\53\6\11\1\2\6\u4f9a" +
		"\1\7\6\14\1\5\6\u0429\1\32\6\u04cd\1\50\6\u1470\1\40\6\ubb3a\1\32\6\32\1\7\6\1\1" +
		"\22\6\32\1\32\6\32\1\4\6\1\1\1\6\1\1\7\6\1\1\13\6\32\1\32\6\32\1\32\6\32\1\32\6\32" +
		"\1\32\6\32\1\32\6\32\1\32\6\32\1\32\6\32\1\32\6\32\1\34\6\34\1\31\6\1\1\6\6\32\1" +
		"\31\6\1\1\6\6\32\1\31\6\1\1\6\6\32\1\31\6\1\1\6\6\32\1\31\6\1\1\6\6\1\1\1\6\u2834" +
		"\1");

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

	private static final short[] tmRuleSymbol = unpack_short(4,
		"\1\2\3\4");

	private static final int tmClassesCount = 8;

	private static final short[] tmGoto = unpack_vc_short(64,
		"\1\ufffe\1\uffff\1\1\1\2\1\3\1\4\1\uffff\1\5\5\uffff\1\4\6\uffff\3\6\1\uffff\4\ufffd" +
		"\2\3\2\ufffd\5\ufffc\1\4\2\ufffc\7\ufffa\1\5\3\uffff\1\7\3\6\1\uffff\10\ufffb");

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
		if (chr >= 0 && chr < 131072) return tmCharClass[chr];
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
					if (l + 1 >= datalen) {
						tokenBuffer.append(data, tokenOffset, l - tokenOffset);
						tokenOffset = 0;
						if (l < datalen) {
							data[0] = data[l];
							datalen = Math.max(stream.read(data, 1, data.length - 1) + 1, 1);
						} else {
							datalen = stream.read(data);
						}
						l = 0;
					}
					charOffset = l;
					chr = l < datalen ? data[l++] : -1;
					if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < datalen &&
							Character.isLowSurrogate(data[l])) {
						chr = Character.toCodePoint((char) chr, data[l++]);
					}
				}
			}

			if (state == -1) {
				if (charOffset > tokenOffset) {
					tokenBuffer.append(data, tokenOffset, charOffset - tokenOffset);
				}
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset);
				token.symbol = -1;
				continue;
			}

			if (state == -2) {
				token.symbol = Tokens.eoi;
				token.value = null;
				break tokenloop;
			}

			if (charOffset > tokenOffset) {
				tokenBuffer.append(data, tokenOffset, charOffset - tokenOffset);
			}

			token.symbol = tmRuleSymbol[-state - 3];
			token.value = null;

		} while (token.symbol == -1 || !createToken(token, -state - 3));
		tokenOffset = -1;
		return token;
	}

	protected boolean createToken(Span token, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 0: // identifier: /[a-zA-Z_][a-zA-Z_0-9]*/
				 token.value = tokenText(); 
				break;
			case 1: // icon: /\-?[0-9]+/
				 token.value = Integer.parseInt(tokenText()); 
				break;
			case 2: // string: /"({schar})+"/
				 token.value = tokenText(); 
				break;
			case 3: // _skip: /[\n\t\r ]+/
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
