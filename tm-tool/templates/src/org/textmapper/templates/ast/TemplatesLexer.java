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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 36, 30, 1, 1, 36, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		36, 14, 31, 3, 2, 13, 22, 6, 18, 19, 12, 11, 21, 10, 20, 5,
		35, 35, 35, 35, 35, 35, 35, 35, 29, 29, 26, 1, 25, 23, 24, 27,
		1, 33, 33, 33, 33, 33, 33, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 16, 7, 17, 1, 28,
		1, 34, 34, 33, 33, 33, 34, 28, 28, 28, 28, 28, 28, 28, 32, 28,
		28, 28, 32, 28, 32, 28, 32, 28, 8, 28, 28, 4, 15, 9
	};

	private static final short tmStateMap[] = {
		0, 10
	};

	private static final short tmBacktracking[] = {
		3, 4
	};

	private static final int tmFirstRule = -4;

	private static final int[] tmRuleSymbol = unpack_int(64,
		"\7\0\1\0\2\0\3\0\4\0\5\0\6\0\10\0\11\0\12\0\13\0\14\0\15\0\16\0\17\0\20\0\21\0\22" +
		"\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36\0\37\0\40\0\41\0\42" +
		"\0\43\0\44\0\45\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57\0\60\0\61\0\62" +
		"\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77\0\100\0");

	private static final int tmClassesCount = 37;

	private static final short[] tmGoto = unpack_vc_short(1887,
		"\1\ufffe\1\11\1\1\42\11\2\uffff\1\10\1\uffff\1\7\1\6\2\uffff\1\3\23\uffff\1\3\1\2" +
		"\2\uffff\3\3\1\2\1\uffff\35\ufff8\1\2\5\ufff8\1\2\1\ufff8\3\ufff9\1\ufffd\4\ufff9" +
		"\1\3\23\ufff9\2\3\2\ufff9\4\3\1\ufff9\35\uffff\1\5\5\uffff\1\5\1\uffff\35\ufff9\1" +
		"\5\5\ufff9\1\5\1\ufff9\45\ufff6\45\ufff7\45\ufffa\1\ufffb\1\11\1\ufffb\42\11\2\uffff" +
		"\1\62\1\uffff\1\61\1\60\1\51\1\uffff\1\62\1\50\1\45\1\44\1\43\1\42\1\40\1\36\1\35" +
		"\1\34\1\33\1\32\1\31\1\30\1\26\1\23\1\21\1\17\1\16\1\15\1\62\1\14\1\13\1\uffff\3" +
		"\62\1\14\1\13\36\uffbd\1\13\5\uffbd\1\13\35\ufff5\1\14\5\ufff5\1\14\1\ufff5\45\uffbe" +
		"\45\uffbf\27\uffc1\1\20\15\uffc1\45\uffc3\27\uffc0\1\22\15\uffc0\45\uffc2\27\uffc7" +
		"\1\25\1\24\14\uffc7\45\uffc4\45\uffc8\26\uffff\1\27\16\uffff\45\uffca\45\uffcb\45" +
		"\uffcc\45\uffcd\45\uffce\45\uffcf\45\uffd0\17\uffd1\1\37\25\uffd1\45\uffc9\27\uffd2" +
		"\1\41\15\uffd2\45\uffc6\45\uffd3\45\uffd5\45\uffd7\11\uffd6\1\47\16\uffd6\1\46\14" +
		"\uffd6\45\uffc5\45\uffd8\45\uffd9\1\uffff\5\51\1\57\1\52\26\51\1\uffff\6\51\6\uffff" +
		"\2\51\1\55\22\uffff\1\51\3\uffff\2\51\1\uffff\1\51\1\53\2\uffff\5\51\1\57\1\52\26" +
		"\51\1\uffff\4\51\1\54\1\51\1\uffff\5\51\1\57\1\52\26\51\1\uffff\6\51\35\uffff\1\56" +
		"\3\uffff\3\56\2\uffff\5\51\1\57\1\52\25\51\1\56\1\uffff\2\51\3\56\1\51\45\ufff4\45" +
		"\uffd4\45\uffda\2\ufffc\1\62\5\ufffc\1\62\23\ufffc\2\62\2\ufffc\4\62\1\ufffc");

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
			case 0:
				return createIdentifierToken(token, ruleIndex);
			case 3: // escid: /$[a-zA-Z_][A-Za-z_0-9]*(#[0-9]+)?/
				{ token.value = tokenText().substring(1, tokenSize()); }
				break;
			case 4: // escint: /$[0-9]+/
				{ token.value = Integer.parseInt(tokenText().substring(1, tokenSize())); }
				break;
			case 5: // '${': /$\{/
				state = States.query;
				{ deep = 1;}
				break;
			case 7: // icon: /[0-9]+/
				{ token.value = Integer.parseInt(tokenText()); }
				break;
			case 8: // ccon: /'([^\n\\']|\\(['"?\\abfnrtv]|x[0-9a-fA-F]+|[0-7]([0-7][0-7]?)?))*'/
				{ token.value = unescape(tokenText(), 1, tokenSize()-1); }
				break;
			case 34: // '{': /\{/
				{ deep++; }
				break;
			case 35: // '}': /\}/
				{ if (--deep == 0) { state = 0; } }
				break;
			case 36: // '-}': /\-\}/
				state = States.initial;
				break;
			case 63: // _skip: /[\t\r\n ]+/
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<>();
	static {
		subTokensOfIdentifier.put("call", 9);
		subTokensOfIdentifier.put("cached", 10);
		subTokensOfIdentifier.put("case", 11);
		subTokensOfIdentifier.put("end", 12);
		subTokensOfIdentifier.put("else", 13);
		subTokensOfIdentifier.put("eval", 14);
		subTokensOfIdentifier.put("false", 15);
		subTokensOfIdentifier.put("for", 16);
		subTokensOfIdentifier.put("file", 17);
		subTokensOfIdentifier.put("foreach", 18);
		subTokensOfIdentifier.put("grep", 19);
		subTokensOfIdentifier.put("if", 20);
		subTokensOfIdentifier.put("in", 21);
		subTokensOfIdentifier.put("import", 22);
		subTokensOfIdentifier.put("is", 23);
		subTokensOfIdentifier.put("map", 24);
		subTokensOfIdentifier.put("new", 25);
		subTokensOfIdentifier.put("null", 26);
		subTokensOfIdentifier.put("query", 27);
		subTokensOfIdentifier.put("switch", 28);
		subTokensOfIdentifier.put("separator", 29);
		subTokensOfIdentifier.put("template", 30);
		subTokensOfIdentifier.put("true", 31);
		subTokensOfIdentifier.put("self", 32);
		subTokensOfIdentifier.put("assert", 33);
	}

	protected boolean createIdentifierToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfIdentifier.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 0:	// <default>
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
