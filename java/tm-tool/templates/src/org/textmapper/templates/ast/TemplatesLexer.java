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
package org.textmapper.templates.ast;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class TemplatesLexer {

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
		int query = 1;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int any = 1;
		int escdollar = 2;
		int escid = 3;
		int escint = 4;
		int DollarLbrace = 5;
		int DollarDiv = 6;
		int identifier = 7;
		int icon = 8;
		int ccon = 9;
		int Lcall = 10;
		int Lcached = 11;
		int Lcase = 12;
		int Lend = 13;
		int Lelse = 14;
		int Leval = 15;
		int Lfalse = 16;
		int Lfor = 17;
		int Lfile = 18;
		int Lforeach = 19;
		int Lgrep = 20;
		int Lif = 21;
		int Lin = 22;
		int Limport = 23;
		int Lis = 24;
		int Lmap = 25;
		int Lnew = 26;
		int Lnull = 27;
		int Lquery = 28;
		int Lswitch = 29;
		int Lseparator = 30;
		int Ltemplate = 31;
		int Ltrue = 32;
		int Lself = 33;
		int Lassert = 34;
		int Lbrace = 35;
		int Rbrace = 36;
		int MinusRbrace = 37;
		int Plus = 38;
		int Minus = 39;
		int Mult = 40;
		int Div = 41;
		int Rem = 42;
		int Excl = 43;
		int Or = 44;
		int Lbrack = 45;
		int Rbrack = 46;
		int Lparen = 47;
		int Rparen = 48;
		int Dot = 49;
		int Comma = 50;
		int AndAnd = 51;
		int OrOr = 52;
		int AssignAssign = 53;
		int Assign = 54;
		int ExclAssign = 55;
		int MinusGt = 56;
		int AssignGt = 57;
		int LtAssign = 58;
		int GtAssign = 59;
		int Lt = 60;
		int Gt = 61;
		int Colon = 62;
		int Quest = 63;
		int _skip = 64;
		int error = 65;
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

	private int deep = 0;

	private String unescape(String s, int start, int end) {
		StringBuilder sb = new StringBuilder();
		end = Math.min(end, s.length());
		for(int i = start; i < end; i++) {
			char c = s.charAt(i);
			if(c == '\\') {
				if(++i == end) {
					break;
				}
				c = s.charAt(i);
				if(c == 'u' || c == 'x') {
					// FIXME process unicode
				} else if(c == 'n') {
					sb.append('\n');
				} else if(c == 'r') {
					sb.append('\r');
				} else if(c == 't') {
					sb.append('\t');
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public TemplatesLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 1, 1, 2, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
		19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 21, 1, 22, 23, 24, 25,
		1, 26, 26, 26, 26, 26, 26, 27, 27, 27, 27, 27, 27, 27, 27, 27,
		27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 28, 29, 30, 1, 27,
		1, 31, 31, 26, 26, 26, 31, 27, 27, 27, 27, 27, 27, 27, 32, 27,
		27, 27, 32, 27, 32, 27, 32, 27, 33, 27, 27, 34, 35, 36, 1
	};

	private static final short tmStateMap[] = {
		0, 10
	};

	private static final short tmBacktracking[] = {
		4, 4
	};

	private static final int tmFirstRule = -2;

	private static final int[] tmRuleSymbol = unpack_int(66,
		"\uffff\uffff\0\0\1\0\2\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\16\0\17" +
		"\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36\0\37" +
		"\0\40\0\41\0\42\0\43\0\44\0\45\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57" +
		"\0\60\0\61\0\62\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77" +
		"\0\100\0");

	private static final int tmClassesCount = 37;

	private static final short[] tmGoto = unpack_vc_short(1887,
		"\1\ufffd\6\11\1\1\35\11\7\ufffe\1\10\12\ufffe\1\7\2\6\5\ufffe\2\3\3\ufffe\3\3\1\2" +
		"\2\ufffe\45\ufff8\6\ufffa\1\uffff\14\ufffa\2\3\5\ufffa\2\3\3\ufffa\3\3\3\ufffa\23" +
		"\ufffe\2\5\20\ufffe\23\ufffa\2\5\20\ufffa\23\ufff9\2\6\20\ufff9\45\ufff7\45\ufffb" +
		"\1\ufffc\6\11\1\ufffc\35\11\2\ufffe\2\62\1\60\2\ufffe\1\57\1\56\1\54\1\45\1\44\1" +
		"\43\1\42\1\41\1\40\1\35\1\34\1\33\2\32\1\31\1\27\1\24\1\22\1\21\2\57\1\20\1\ufffe" +
		"\1\17\3\57\1\16\1\14\1\13\45\uffd9\43\uffd1\1\15\1\uffd1\45\uffc9\45\uffda\45\uffcf" +
		"\45\uffd0\45\uffbe\27\uffc0\1\23\15\uffc0\45\uffc2\27\uffc7\1\26\1\25\14\uffc7\45" +
		"\uffc4\45\uffc8\27\uffc1\1\30\15\uffc1\45\uffc3\45\uffbf\23\ufff5\2\32\20\ufff5\45" +
		"\uffd4\45\uffcc\30\uffd6\1\37\13\uffd6\1\36\45\uffd8\45\uffc5\45\uffcb\45\uffd7\45" +
		"\uffd5\45\uffcd\45\uffce\1\ufffe\2\45\1\ufffe\6\45\1\53\22\45\1\46\7\45\5\ufffe\1" +
		"\45\4\ufffe\1\45\10\ufffe\1\51\5\ufffe\1\45\3\ufffe\1\45\1\ufffe\2\45\1\47\26\ufffe" +
		"\2\50\5\ufffe\1\50\4\ufffe\1\50\6\ufffe\2\45\1\ufffe\6\45\1\53\10\45\2\50\5\45\1" +
		"\50\2\45\1\46\1\45\1\50\5\45\1\ufffe\2\45\1\ufffe\6\45\1\53\10\45\1\52\11\45\1\46" +
		"\7\45\1\ufffe\2\45\1\ufffe\6\45\1\53\22\45\1\46\7\45\45\ufff4\11\ufffe\1\55\33\ufffe" +
		"\45\uffca\45\uffd3\7\ufff6\1\57\13\ufff6\2\57\5\ufff6\2\57\3\ufff6\3\57\3\ufff6\27" +
		"\uffd2\1\61\15\uffd2\45\uffc6\2\uffbd\2\62\41\uffbd");

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
		if (chr >= 0 && chr < 127) return tmCharClass[chr];
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
					reporter.error("Unexpected end of input reached", token.line, token.offset, token.endoffset);
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
				reporter.error(MessageFormat.format("invalid token at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset, token.endoffset);
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
			case 4: // escid: /$[a-zA-Z_][A-Za-z_0-9]*(#[0-9]+)?/
				{ token.value = tokenText().substring(1, tokenSize()); }
				break;
			case 5: // escint: /$[0-9]+/
				{ token.value = Integer.parseInt(tokenText().substring(1, tokenSize())); }
				break;
			case 6: // '${': /$\{/
				{ state = States.query; deep = 1;}
				break;
			case 8:
				return createIdentifierToken(token, ruleIndex);
			case 9: // icon: /[0-9]+/
				{ token.value = Integer.parseInt(tokenText()); }
				break;
			case 10: // ccon: /'([^\n\\']|\\(['"?\\abfnrtv]|x[0-9a-fA-F]+|[0-7]([0-7][0-7]?)?))*'/
				{ token.value = unescape(tokenText(), 1, tokenSize()-1); }
				break;
			case 36: // '{': /\{/
				{ deep++; }
				break;
			case 37: // '}': /\}/
				{ if (--deep == 0) { state = 0; } }
				break;
			case 38: // '-}': /\-\}/
				{ state = States.initial; }
				break;
			case 65: // _skip: /[\t\r\n ]+/
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<>();
	static {
		subTokensOfIdentifier.put("call", 11);
		subTokensOfIdentifier.put("cached", 12);
		subTokensOfIdentifier.put("case", 13);
		subTokensOfIdentifier.put("end", 14);
		subTokensOfIdentifier.put("else", 15);
		subTokensOfIdentifier.put("eval", 16);
		subTokensOfIdentifier.put("false", 17);
		subTokensOfIdentifier.put("for", 18);
		subTokensOfIdentifier.put("file", 19);
		subTokensOfIdentifier.put("foreach", 20);
		subTokensOfIdentifier.put("grep", 21);
		subTokensOfIdentifier.put("if", 22);
		subTokensOfIdentifier.put("in", 23);
		subTokensOfIdentifier.put("import", 24);
		subTokensOfIdentifier.put("is", 25);
		subTokensOfIdentifier.put("map", 26);
		subTokensOfIdentifier.put("new", 27);
		subTokensOfIdentifier.put("null", 28);
		subTokensOfIdentifier.put("query", 29);
		subTokensOfIdentifier.put("switch", 30);
		subTokensOfIdentifier.put("separator", 31);
		subTokensOfIdentifier.put("template", 32);
		subTokensOfIdentifier.put("true", 33);
		subTokensOfIdentifier.put("self", 34);
		subTokensOfIdentifier.put("assert", 35);
	}

	protected boolean createIdentifierToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfIdentifier.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 8:	// <default>
				{ token.value = tokenText(); }
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
