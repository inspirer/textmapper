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
		int _skipSoftKW = 16;
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 12, 1, 1, 12, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		12, 1, 1, 1, 1, 1, 1, 1, 6, 7, 1, 1, 1, 1, 1, 1,
		2, 11, 11, 11, 11, 11, 11, 11, 9, 9, 1, 1, 1, 1, 1, 1,
		1, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1, 1, 1, 1, 8,
		1, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 3, 8, 8, 4, 1, 5, 1, 1
	};

	private static final int[] tmRuleSymbol = unpack_int(15,
		"\1\0\14\0\15\0\16\0\2\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\17\0\20\0");

	private static final int tmClassesCount = 13;

	private static final short[] tmGoto = unpack_vc_short(169,
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
		if (chr >= 0 && chr < 128) return tmCharClass[chr];
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

			if (state == -1) {
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.offset, token.endoffset);
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
			case 0:
				return createIdentifierToken(token, ruleIndex);
			case 1:
				return createNumericToken(token, ruleIndex);
			case 2:
				return createOctalToken(token, ruleIndex);
			case 3:
				return createDecimalToken(token, ruleIndex);
			case 4: // _skip: /[\n\t\r ]+/
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<>();
	static {
		subTokensOfIdentifier.put("class", 5);
		subTokensOfIdentifier.put("extends", 6);
		subTokensOfIdentifier.put("interface", 11);
		subTokensOfIdentifier.put("enum", 12);
		subTokensOfIdentifier.put("xyzzz", 14);
	}

	protected boolean createIdentifierToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfIdentifier.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 5:	// class
				{ token.value = "class"; }
				break;
			case 11:	// interface
				{ token.value = "interface"; }
				break;
			case 12:	// enum
				{ token.value = new Object(); }
				break;
			case 6:	// extends (soft)
			case 14:	// xyzzz (soft)
			case 0:	// <default>
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
			case 2:	// <default>
				{ token.value = Integer.parseInt(tokenText(), 8); }
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfDecimal = new HashMap<>();
	static {
		subTokensOfDecimal.put("11", 13);
	}

	protected boolean createDecimalToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfDecimal.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 13:	// 11
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
