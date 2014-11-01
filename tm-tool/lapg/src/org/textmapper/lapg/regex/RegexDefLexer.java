/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

	public static class LapgSymbol {
		public Object value;
		public int symbol;
		public int state;
		public int offset;
		public int endoffset;
	}

	public interface States {
		public static final int initial = 0;
		public static final int afterChar = 1;
		public static final int inSet = 2;
	}

	public interface Tokens {
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int _char = 1;
		public static final int escaped = 2;
		public static final int charclass = 3;
		public static final int Dot = 4;
		public static final int Mult = 5;
		public static final int Plus = 6;
		public static final int Questionmark = 7;
		public static final int quantifier = 8;
		public static final int op_minus = 9;
		public static final int op_union = 10;
		public static final int op_intersect = 11;
		public static final int Lparen = 12;
		public static final int Or = 13;
		public static final int Rparen = 14;
		public static final int LparenQuestionmark = 15;
		public static final int Lsquare = 16;
		public static final int LsquareXor = 17;
		public static final int expand = 18;
		public static final int kw_eoi = 19;
		public static final int Rsquare = 20;
		public static final int Minus = 21;
	}

	public interface ErrorReporter {
		void error(String message, int offset, int endoffset);
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

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

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

	private int parseCodePoint(String s, LapgSymbol lapg_n) {
		int ch = RegexUtil.unescapeHex(s);
		if (Character.isValidCodePoint(ch)) return ch;
		reporter.error("unicode code point is out of range", lapg_n.offset, lapg_n.endoffset);
		return 0;
	}

	public RegexDefLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
				token.append(data, tokenOffset, l - tokenOffset);
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

	public String current() {
		return token.toString();
	}

	private static final short tmCharClass[] = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 33, 33, 1, 1, 33, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 22, 1, 23, 25, 17, 18, 20, 21, 16, 32,
		35, 35, 35, 35, 35, 35, 35, 35, 31, 31, 26, 1, 1, 1, 1, 19,
		1, 36, 36, 36, 37, 36, 36, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		34, 30, 30, 38, 30, 14, 30, 38, 34, 30, 30, 27, 4, 29, 28, 30,
		1, 5, 6, 36, 37, 36, 7, 30, 30, 39, 30, 30, 30, 30, 8, 30,
		15, 30, 9, 40, 10, 13, 11, 38, 12, 30, 30, 2, 24, 3, 1, 1
	};

	private static final short tmStateMap[] = {
		0, 1, 2
	};

	private static final short[] tmRuleSymbol = unpack_short(36,
		"\22\1\2\2\2\2\2\2\2\2\2\2\2\2\3\3\4\5\6\7\10\11\12\13\1\14\15\16\17\20\21\1\23\24" +
		"\25\1");

	private static final int tmClassesCount = 41;

	private static final short[] tmGoto = unpack_vc_short(2829,
		"\1\ufffe\1\3\1\4\1\3\1\5\13\3\1\6\3\7\1\3\1\10\1\3\1\11\1\12\1\13\1\3\1\14\1\3\1" +
		"\uffff\2\3\1\uffff\10\3\1\uffff\1\3\1\15\1\3\1\5\13\3\1\6\1\16\1\17\1\20\1\3\1\10" +
		"\1\3\1\11\1\12\1\13\1\3\1\14\1\3\1\uffff\2\3\1\uffff\10\3\1\uffff\3\3\1\5\13\3\1" +
		"\6\3\7\1\3\1\21\1\3\3\22\1\3\1\uffff\1\3\1\23\2\3\1\uffff\10\3\56\ufffc\13\24\16" +
		"\ufffc\1\24\3\ufffc\1\24\1\ufffc\5\24\1\uffff\4\25\1\26\1\27\1\30\1\31\1\32\1\33" +
		"\1\34\1\35\1\36\1\37\1\40\17\25\1\uffff\1\25\2\uffff\1\41\1\25\2\42\1\25\1\42\51" +
		"\uffed\51\uffe5\51\uffde\23\uffe4\1\43\25\uffe4\51\uffe3\51\uffe2\34\uffe0\1\44\14" +
		"\uffe0\5\ufffc\13\24\2\ufffc\1\45\2\ufffc\1\46\1\47\7\ufffc\1\24\1\50\2\ufffc\1\24" +
		"\1\50\5\24\51\uffec\51\uffeb\51\uffea\51\uffdb\51\uffda\51\uffdc\3\uffff\1\51\1\uffff" +
		"\13\24\5\uffff\1\24\10\uffff\2\24\2\uffff\7\24\51\ufffb\51\ufffa\51\ufff9\51\ufff8" +
		"\51\ufff7\51\ufff6\51\ufff5\51\ufff4\5\uffff\3\52\27\uffff\1\52\3\uffff\3\52\10\uffff" +
		"\3\53\27\uffff\1\53\3\uffff\3\53\10\uffff\3\54\27\uffff\1\54\3\uffff\3\54\5\uffff" +
		"\1\55\111\uffff\1\56\5\uffff\51\uffef\25\uffff\1\57\21\uffff\2\57\51\uffdf\3\uffff" +
		"\1\60\50\uffff\1\61\73\uffff\1\62\25\uffff\1\63\20\uffff\1\64\12\uffff\1\50\3\uffff" +
		"\1\50\5\uffff\51\ufffd\5\uffff\3\65\27\uffff\1\65\3\uffff\3\65\10\uffff\3\66\27\uffff" +
		"\1\66\3\uffff\3\66\10\uffff\3\67\27\uffff\1\67\3\uffff\3\67\10\uffff\13\70\16\uffff" +
		"\2\70\2\uffff\7\70\43\uffff\1\71\32\uffff\1\57\4\uffff\1\72\14\uffff\2\57\51\uffe7" +
		"\51\uffe8\3\uffff\1\73\45\uffff\51\uffe9\3\uffff\1\63\33\uffff\1\64\3\uffff\1\64" +
		"\5\uffff\51\ufff2\5\uffff\3\74\27\uffff\1\74\3\uffff\3\74\10\uffff\3\75\27\uffff" +
		"\1\75\3\uffff\3\75\6\uffff\1\76\1\uffff\13\70\16\uffff\2\70\2\uffff\7\70\51\ufff3" +
		"\51\uffe1\51\uffe6\5\uffff\3\77\27\uffff\1\77\3\uffff\3\77\10\uffff\3\100\27\uffff" +
		"\1\100\3\uffff\3\100\3\uffff\51\uffee\51\ufff1\5\uffff\3\101\27\uffff\1\101\3\uffff" +
		"\3\101\10\uffff\3\102\27\uffff\1\102\3\uffff\3\102\10\uffff\3\103\27\uffff\1\103" +
		"\3\uffff\3\103\10\uffff\3\104\27\uffff\1\104\3\uffff\3\104\3\uffff\51\ufff0");

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
			tokenOffset = charOffset;

			for (state = tmStateMap[this.state]; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == -1) {
					lapg_n.endoffset = currOffset;
					lapg_n.symbol = 0;
					lapg_n.value = null;
					reporter.error("Unexpected end of input reached", lapg_n.offset, lapg_n.endoffset);
					lapg_n.offset = currOffset;
					tokenOffset = -1;
					return lapg_n;
				}
				if (state >= -1 && chr != -1) {
					currOffset += l - charOffset;
					if (chr == '\n') {
						currLine++;
					}
					if (l + 1 >= datalen) {
						token.append(data, tokenOffset, l - tokenOffset);
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
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				if (charOffset > tokenOffset) {
					token.append(data, tokenOffset, charOffset - tokenOffset);
				}
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, current()), lapg_n.offset, lapg_n.endoffset);
				lapg_n.symbol = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.symbol = 0;
				lapg_n.value = null;
				tokenOffset = -1;
				return lapg_n;
			}

			if (charOffset > tokenOffset) {
				token.append(data, tokenOffset, charOffset - tokenOffset);
			}

			lapg_n.symbol = tmRuleSymbol[-state - 3];
			lapg_n.value = null;

		} while (lapg_n.symbol == -1 || !createToken(lapg_n, -state - 3));
		tokenOffset = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 0:
				return createExpandToken(lapg_n, ruleIndex);
			case 1: // char: /[^()\[\]\.|\\\/*?+\-]/
				 lapg_n.value = current().codePointAt(0); quantifierReady(); 
				break;
			case 2: // escaped: /\\[^\r\n\t0-9uUxXwWsSdDpPabfnrtv]/
				 lapg_n.value = (int) current().charAt(1); quantifierReady(); 
				break;
			case 3: // escaped: /\\a/
				 lapg_n.value = (int) 7; quantifierReady(); 
				break;
			case 4: // escaped: /\\b/
				 lapg_n.value = (int) '\b'; quantifierReady(); 
				break;
			case 5: // escaped: /\\f/
				 lapg_n.value = (int) '\f'; quantifierReady(); 
				break;
			case 6: // escaped: /\\n/
				 lapg_n.value = (int) '\n'; quantifierReady(); 
				break;
			case 7: // escaped: /\\r/
				 lapg_n.value = (int) '\r'; quantifierReady(); 
				break;
			case 8: // escaped: /\\t/
				 lapg_n.value = (int) '\t'; quantifierReady(); 
				break;
			case 9: // escaped: /\\v/
				 lapg_n.value = (int) 0xb; quantifierReady(); 
				break;
			case 10: // escaped: /\\[0-7][0-7][0-7]/
				 lapg_n.value = RegexUtil.unescapeOct(current().substring(1)); quantifierReady(); 
				break;
			case 11: // escaped: /\\x{hx}{2}/
				 lapg_n.value = parseCodePoint(current().substring(2), lapg_n); quantifierReady(); 
				break;
			case 12: // escaped: /\\u{hx}{4}/
				 lapg_n.value = parseCodePoint(current().substring(2), lapg_n); quantifierReady(); 
				break;
			case 13: // escaped: /\\U{hx}{8}/
				 lapg_n.value = parseCodePoint(current().substring(2), lapg_n); quantifierReady(); 
				break;
			case 14: // charclass: /\\[wWsSdD]/
				 lapg_n.value = current().substring(1); quantifierReady(); 
				break;
			case 15: // charclass: /\\p\{\w+\}/
				 lapg_n.value = current().substring(3, current().length() - 1); quantifierReady(); 
				break;
			case 16: // '.': /\./
				 quantifierReady(); 
				break;
			case 17: // '*': /\*/
				state = States.initial;
				break;
			case 18: // '+': /\+/
				state = States.initial;
				break;
			case 19: // '?': /\?/
				state = States.initial;
				break;
			case 20: // quantifier: /\{[0-9]+(,[0-9]*)?\}/
				state = States.initial;
				break;
			case 21: // op_minus: /\{\-\}/
				state = States.initial;
				break;
			case 22: // op_union: /\{\+\}/
				state = States.initial;
				break;
			case 23: // op_intersect: /\{&&\}/
				state = States.initial;
				break;
			case 24: // char: /[*+?]/
				 lapg_n.value = current().codePointAt(0); quantifierReady(); 
				break;
			case 25: // '(': /\(/
				 state = 0; 
				break;
			case 26: // '|': /\|/
				 state = 0; 
				break;
			case 27: // ')': /\)/
				 quantifierReady(); 
				break;
			case 28: // '(?': /\(\?[is\-]+:/
				 state = 0; 
				break;
			case 29: // '[': /\[/
				state = States.inSet;
				break;
			case 30: // '[^': /\[\^/
				state = States.inSet;
				break;
			case 31: // char: /\-/
				 lapg_n.value = current().codePointAt(0); quantifierReady(); 
				break;
			case 33: // ']': /\]/
				 state = 0; quantifierReady(); 
				break;
			case 35: // char: /[(|)]/
				 lapg_n.value = current().codePointAt(0); 
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfExpand = new HashMap<String,Integer>();
	static {
		subTokensOfExpand.put("{eoi}", 32);
	}

	protected boolean createExpandToken(LapgSymbol lapg_n, int ruleIndex) {
		Integer replacement = subTokensOfExpand.get(current());
		if (replacement != null) {
			ruleIndex = replacement;
			lapg_n.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 32:	// {eoi}
				 state = 0; 
				break;
			case 0:	// <default>
				 quantifierReady(); 
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
