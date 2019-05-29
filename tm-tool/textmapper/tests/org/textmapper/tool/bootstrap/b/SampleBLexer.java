/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
package org.textmapper.tool.bootstrap.b;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class SampleBLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int offset;
		public int endoffset;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int identifier = 1;
		int _skip = 2;
		int Lclass = 3;
		int Lextends = 4;
		int Lbrace = 5;
		int Rbrace = 6;
		int Lparen = 7;
		int Rparen = 8;
		int Linterface = 9;
		int Lenum = 10;
		int error = 11;
		int numeric = 12;
		int octal = 13;
		int decimal = 14;
		int eleven = 15;
	}

	public interface ErrorReporter {
		void error(String message, int offset, int endoffset);
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

	public SampleBLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		2, 1, 1, 1, 1, 1, 1, 1, 3, 4, 1, 1, 1, 1, 1, 1,
		5, 6, 6, 6, 6, 6, 6, 6, 7, 7, 1, 1, 1, 1, 1, 1,
		1, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 1, 1, 1, 1, 9,
		1, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 10, 9, 9, 11, 1, 12
	};

	private static final int[] tmRuleSymbol = unpack_int(16,
		"\uffff\uffff\0\0\1\0\2\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\14\0\15\0\16\0\17\0");

	private static final int tmClassesCount = 13;

	private static final short[] tmGoto = unpack_vc_short(169,
		"\1\ufffe\1\uffff\1\14\1\13\1\12\1\6\2\4\3\3\1\2\1\1\15\ufff8\15\ufff9\5\ufffd\6\3" +
		"\2\ufffd\5\uffff\3\5\5\uffff\5\ufff1\3\5\5\ufff1\5\uffff\2\11\3\uffff\1\7\7\uffff" +
		"\4\10\4\uffff\5\ufff3\4\10\4\ufff3\5\ufff2\2\11\6\ufff2\15\ufff6\15\ufff7\2\ufffc" +
		"\1\14\12\ufffc");

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
		if (chr >= 0 && chr < 126) return tmCharClass[chr];
		return chr == -1 ? 0 : 1;
	}

	public Span next() throws IOException {
		Span token = new Span();
		int state;

		tokenloop:
		do {
			token.offset = currOffset;
			tokenLine = currLine;
			tokenOffset = charOffset;

			for (state = this.state; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == -1) {
					token.endoffset = currOffset;
					token.symbol = 0;
					token.value = null;
					reporter.error("Unexpected end of input reached", token.offset, token.endoffset);
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

			token.symbol = tmRuleSymbol[-1 - state];
			token.value = null;

			if (token.symbol == -1) {
				reporter.error(MessageFormat.format("invalid token at line {0}: `{1}`, skipped", currLine, tokenText()), token.offset, token.endoffset);
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
			case 2:
				return createIdentifierToken(token, ruleIndex);
			case 3: // _skip: /[\n\t\r ]+/
				spaceToken = true;
				break;
			case 12:
				return createNumericToken(token, ruleIndex);
			case 13:
				return createOctalToken(token, ruleIndex);
			case 14:
				return createDecimalToken(token, ruleIndex);
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<>();
	static {
		subTokensOfIdentifier.put("class", 4);
		subTokensOfIdentifier.put("extends", 5);
		subTokensOfIdentifier.put("interface", 10);
		subTokensOfIdentifier.put("enum", 11);
	}

	protected boolean createIdentifierToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfIdentifier.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 4:	// class
				{ token.value = "class"; }
				break;
			case 10:	// interface
				{ token.value = "interface"; }
				break;
			case 11:	// enum
				{ token.value = new Object(); }
				break;
			case 2:	// <default>
				{ token.value = tokenText(); }
				break;
		}
		return !(spaceToken);
	}

	protected boolean createNumericToken(Span token, int ruleIndex) {
		return true;
	}

	protected boolean createOctalToken(Span token, int ruleIndex) {
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 13:	// <default>
				{ token.value = Integer.parseInt(tokenText(), 8); }
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfDecimal = new HashMap<>();
	static {
		subTokensOfDecimal.put("11", 15);
	}

	protected boolean createDecimalToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfDecimal.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 15:	// 11
				{ token.value = 11; }
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
