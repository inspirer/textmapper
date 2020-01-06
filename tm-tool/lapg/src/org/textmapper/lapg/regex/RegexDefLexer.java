/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
package org.textmapper.lapg.regex;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class RegexDefLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int offset;
		public int endoffset;
	}

	public interface States {
		int initial = 0;
		int afterChar = 1;
		int inSet = 2;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int _char = 1;
		int escaped = 2;
		int charclass = 3;
		int Dot = 4;
		int Mult = 5;
		int Plus = 6;
		int Quest = 7;
		int quantifier = 8;
		int op_minus = 9;
		int op_union = 10;
		int op_intersect = 11;
		int Lparen = 12;
		int Or = 13;
		int Rparen = 14;
		int LparenQuest = 15;
		int Lbrack = 16;
		int LbrackXor = 17;
		int expand = 18;
		int kw_eoi = 19;
		int Rbrack = 20;
		int Minus = 21;
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

	private void quantifierReady() {
		if (chr == -1) {
			if (state == 1) state = 0;
			return;
		}
		if (state == 0) state = 1;
	}

	private int parseCodePoint(String s, Span token) {
		int ch = RegexUtil.unescapeHex(s);
		if (Character.isValidCodePoint(ch)) return ch;
		reporter.error("unicode code point is out of range", token.offset, token.endoffset);
		return 0;
	}

	public RegexDefLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 3, 1, 4, 5, 6, 7, 8, 9, 10, 11,
		12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 14, 1, 1, 1, 1, 15,
		1, 16, 16, 16, 17, 16, 16, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		19, 18, 18, 20, 18, 21, 18, 20, 19, 18, 18, 22, 23, 24, 25, 18,
		1, 26, 27, 16, 17, 16, 28, 18, 18, 29, 18, 18, 18, 18, 30, 18,
		31, 18, 32, 33, 34, 35, 36, 20, 37, 18, 18, 38, 39, 40
	};

	private static final short tmStateMap[] = {
		0, 51, 66
	};

	private static final short tmBacktracking[] = {
		2, 3, 26, 47, 2, 60, 2, 58, 2, 56, 2, 53
	};

	private static final int tmFirstRule = -7;

	private static final int[] tmRuleSymbol = unpack_int(38,
		"\uffff\uffff\0\0\1\0\2\0\2\0\2\0\2\0\2\0\2\0\2\0\2\0\2\0\2\0\2\0\2\0\3\0\3\0\4\0" +
		"\5\0\6\0\7\0\10\0\11\0\12\0\13\0\1\0\14\0\15\0\16\0\17\0\20\0\21\0\1\0\22\0\23\0" +
		"\24\0\25\0\1\0");

	private static final int tmClassesCount = 41;

	private static final short[] tmGoto = unpack_vc_short(2870,
		"\1\ufff8\3\62\1\56\1\55\2\54\1\62\1\53\1\52\1\ufff9\3\62\1\54\6\62\1\50\1\6\1\ufff9" +
		"\15\62\1\2\1\1\1\62\51\uffde\20\ufff7\6\uffff\4\ufff7\14\uffff\3\ufff7\11\ufff9\1" +
		"\5\2\ufff9\2\3\2\ufff9\6\3\4\ufff9\14\3\2\ufff9\1\4\51\uffd8\11\ufff9\1\5\2\ufff9" +
		"\2\3\2\ufff9\6\3\4\ufff9\14\3\4\ufff9\1\47\1\ufff9\11\47\1\44\1\ufff9\3\47\1\43\1" +
		"\47\1\ufff9\1\43\1\32\4\47\1\31\1\30\1\27\1\47\1\26\1\22\1\21\1\43\1\20\1\13\1\12" +
		"\1\7\3\47\14\ufff9\2\10\2\ufff9\2\10\10\ufff9\3\10\30\ufff9\2\11\2\ufff9\2\11\10" +
		"\ufff9\3\11\14\ufff9\51\uffed\51\uffef\14\ufff9\2\14\2\ufff9\2\14\10\ufff9\3\14\30" +
		"\ufff9\2\15\2\ufff9\2\15\10\ufff9\3\15\30\ufff9\2\16\2\ufff9\2\16\10\ufff9\3\16\30" +
		"\ufff9\2\17\2\ufff9\2\17\10\ufff9\3\17\14\ufff9\51\uffec\51\ufff0\51\ufff1\46\ufff9" +
		"\1\23\16\ufff9\2\24\2\ufff9\6\24\4\ufff9\14\24\17\ufff9\2\24\2\ufff9\6\24\4\ufff9" +
		"\14\24\2\ufff9\1\25\51\uffe9\51\ufff2\51\ufff3\51\ufff4\51\ufff5\14\ufff9\2\33\2" +
		"\ufff9\2\33\10\ufff9\3\33\30\ufff9\2\34\2\ufff9\2\34\10\ufff9\3\34\30\ufff9\2\35" +
		"\2\ufff9\2\35\10\ufff9\3\35\30\ufff9\2\36\2\ufff9\2\36\10\ufff9\3\36\30\ufff9\2\37" +
		"\2\ufff9\2\37\10\ufff9\3\37\30\ufff9\2\40\2\ufff9\2\40\10\ufff9\3\40\30\ufff9\2\41" +
		"\2\ufff9\2\41\10\ufff9\3\41\30\ufff9\2\42\2\ufff9\2\42\10\ufff9\3\42\14\ufff9\51" +
		"\uffeb\51\uffea\14\ufff9\1\45\50\ufff9\1\46\34\ufff9\51\uffee\51\ufff6\31\uffdb\1" +
		"\51\17\uffdb\51\uffda\51\uffe8\51\uffd9\51\uffe0\51\uffdd\17\uffdf\1\ufffe\31\uffdf" +
		"\11\ufff9\1\60\23\ufff9\1\60\3\ufff9\1\60\20\ufff9\1\60\4\ufff9\1\61\16\ufff9\1\60" +
		"\3\ufff9\1\60\7\ufff9\51\uffdc\51\ufff7\1\ufff9\3\62\1\56\1\55\1\101\1\100\1\62\1" +
		"\53\1\52\1\ufff9\3\62\1\77\6\62\1\50\1\6\1\ufff9\15\62\1\64\1\1\1\62\3\ufff7\1\ufffd" +
		"\3\ufff7\1\ufffc\1\ufff7\1\ufffb\2\ufff7\2\ufffa\2\ufff7\6\uffff\4\ufff7\14\uffff" +
		"\3\ufff7\10\ufff9\1\67\3\ufff9\2\65\32\ufff9\1\66\51\uffe4\14\ufff9\2\67\32\ufff9" +
		"\1\66\50\ufff9\1\71\51\uffe3\50\ufff9\1\73\51\uffe2\3\ufff9\1\75\115\ufff9\1\76\51" +
		"\uffe1\51\uffe5\51\uffe6\51\uffe7\1\ufff9\3\62\2\105\2\54\1\62\1\104\1\52\1\ufff9" +
		"\3\62\1\54\6\62\1\ufff9\1\6\1\103\16\62\1\105\1\62\51\uffd6\51\uffd5\51\uffd4");

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

			// TODO use backupRule
			int backupRule = -1;
			for (state = tmStateMap[this.state]; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state > tmFirstRule && state < 0) {
					token.endoffset = currOffset;
					state = (-1 - state) * 2;
					backupRule = tmBacktracking[state++];
					state = tmBacktracking[state];
				}
				if (state == tmFirstRule && chr == -1) {
					token.endoffset = currOffset;
					token.symbol = 0;
					token.value = null;
					reporter.error("Unexpected end of input reached", token.offset, token.endoffset);
					token.offset = currOffset;
					break tokenloop;
				}
				if (state >= tmFirstRule && chr != -1) {
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

			token.symbol = tmRuleSymbol[tmFirstRule - state];
			token.value = null;

			if (token.symbol == -1) {
				reporter.error(MessageFormat.format("invalid token at line {0}: `{1}`, skipped", currLine, tokenText()), token.offset, token.endoffset);
			}

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
			case 2: // char: /[^()\[\]\.|\\\/*?+\-]/
				{ token.value = tokenText().codePointAt(0); quantifierReady(); }
				break;
			case 3: // escaped: /\\[^\r\n\t0-9uUxXwWsSdDpPabfnrtv]/
				{ token.value = (int) tokenText().charAt(1); quantifierReady(); }
				break;
			case 4: // escaped: /\\a/
				{ token.value = (int) 7; quantifierReady(); }
				break;
			case 5: // escaped: /\\b/
				{ token.value = (int) '\b'; quantifierReady(); }
				break;
			case 6: // escaped: /\\f/
				{ token.value = (int) '\f'; quantifierReady(); }
				break;
			case 7: // escaped: /\\n/
				{ token.value = (int) '\n'; quantifierReady(); }
				break;
			case 8: // escaped: /\\r/
				{ token.value = (int) '\r'; quantifierReady(); }
				break;
			case 9: // escaped: /\\t/
				{ token.value = (int) '\t'; quantifierReady(); }
				break;
			case 10: // escaped: /\\v/
				{ token.value = (int) 0xb; quantifierReady(); }
				break;
			case 11: // escaped: /\\[0-7][0-7][0-7]/
				{ token.value = RegexUtil.unescapeOct(tokenText().substring(1)); quantifierReady(); }
				break;
			case 12: // escaped: /\\x{hx}{2}/
				{ token.value = parseCodePoint(tokenText().substring(2), token); quantifierReady(); }
				break;
			case 13: // escaped: /\\u{hx}{4}/
				{ token.value = parseCodePoint(tokenText().substring(2), token); quantifierReady(); }
				break;
			case 14: // escaped: /\\U{hx}{8}/
				{ token.value = parseCodePoint(tokenText().substring(2), token); quantifierReady(); }
				break;
			case 15: // charclass: /\\[wWsSdD]/
				{ token.value = tokenText().substring(1); quantifierReady(); }
				break;
			case 16: // charclass: /\\p\{\w+\}/
				{ token.value = tokenText().substring(3, tokenSize() - 1); quantifierReady(); }
				break;
			case 17: // '.': /\./
				{ quantifierReady(); }
				break;
			case 18: // '*': /\*/
				{ state = States.initial; }
				break;
			case 19: // '+': /\+/
				{ state = States.initial; }
				break;
			case 20: // '?': /\?/
				{ state = States.initial; }
				break;
			case 21: // quantifier: /\{[0-9]+(,[0-9]*)?\}/
				{ state = States.initial; }
				break;
			case 22: // op_minus: /\{\-\}/
				{ state = States.initial; }
				break;
			case 23: // op_union: /\{\+\}/
				{ state = States.initial; }
				break;
			case 24: // op_intersect: /\{&&\}/
				{ state = States.initial; }
				break;
			case 25: // char: /[*+?]/
				{ token.value = tokenText().codePointAt(0); quantifierReady(); }
				break;
			case 26: // '(': /\(/
				{ state = 0; }
				break;
			case 27: // '|': /\|/
				{ state = 0; }
				break;
			case 28: // ')': /\)/
				{ quantifierReady(); }
				break;
			case 29: // '(?': /\(\?[is\-]+:/
				{ state = 0; }
				break;
			case 30: // '[': /\[/
				{ state = States.inSet; }
				break;
			case 31: // '[^': /\[\^/
				{ state = States.inSet; }
				break;
			case 32: // char: /\-/
				{ token.value = tokenText().codePointAt(0); quantifierReady(); }
				break;
			case 33:
				return createExpandToken(token, ruleIndex);
			case 35: // ']': /\]/
				{ state = 0; quantifierReady(); }
				break;
			case 37: // char: /[(|)]/
				{ token.value = tokenText().codePointAt(0); }
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfExpand = new HashMap<>();
	static {
		subTokensOfExpand.put("{eoi}", 34);
	}

	protected boolean createExpandToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfExpand.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 34:	// {eoi}
				{ state = 0; }
				break;
			case 33:	// <default>
				{ quantifierReady(); }
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
