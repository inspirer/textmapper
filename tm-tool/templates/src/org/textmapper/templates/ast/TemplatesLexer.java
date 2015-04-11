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
		public static final int initial = 0;
		public static final int query = 1;
	}

	public interface Tokens {
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int any = 1;
		public static final int escdollar = 2;
		public static final int escid = 3;
		public static final int escint = 4;
		public static final int DollarLcurly = 5;
		public static final int DollarSlash = 6;
		public static final int identifier = 7;
		public static final int icon = 8;
		public static final int ccon = 9;
		public static final int Lcall = 10;
		public static final int Lcached = 11;
		public static final int Lcase = 12;
		public static final int Lend = 13;
		public static final int Lelse = 14;
		public static final int Leval = 15;
		public static final int Lfalse = 16;
		public static final int Lfor = 17;
		public static final int Lfile = 18;
		public static final int Lforeach = 19;
		public static final int Lgrep = 20;
		public static final int Lif = 21;
		public static final int Lin = 22;
		public static final int Limport = 23;
		public static final int Lis = 24;
		public static final int Lmap = 25;
		public static final int Lnew = 26;
		public static final int Lnull = 27;
		public static final int Lquery = 28;
		public static final int Lswitch = 29;
		public static final int Lseparator = 30;
		public static final int Ltemplate = 31;
		public static final int Ltrue = 32;
		public static final int Lself = 33;
		public static final int Lassert = 34;
		public static final int Lcurly = 35;
		public static final int Rcurly = 36;
		public static final int MinusRcurly = 37;
		public static final int Plus = 38;
		public static final int Minus = 39;
		public static final int Mult = 40;
		public static final int Slash = 41;
		public static final int Percent = 42;
		public static final int Exclamation = 43;
		public static final int Or = 44;
		public static final int Lsquare = 45;
		public static final int Rsquare = 46;
		public static final int Lparen = 47;
		public static final int Rparen = 48;
		public static final int Dot = 49;
		public static final int Comma = 50;
		public static final int AmpersandAmpersand = 51;
		public static final int OrOr = 52;
		public static final int EqualEqual = 53;
		public static final int Equal = 54;
		public static final int ExclamationEqual = 55;
		public static final int MinusGreater = 56;
		public static final int EqualGreater = 57;
		public static final int LessEqual = 58;
		public static final int GreaterEqual = 59;
		public static final int Less = 60;
		public static final int Greater = 61;
		public static final int Colon = 62;
		public static final int Questionmark = 63;
		public static final int _skip = 64;
		public static final int error = 65;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset, int endoffset);
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

	public TemplatesLexer(Reader stream, ErrorReporter reporter) throws IOException {
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

	private static final short tmCharClass[] = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 36, 30, 1, 1, 36, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		36, 14, 31, 3, 2, 13, 22, 6, 18, 19, 12, 11, 21, 10, 20, 5,
		35, 35, 35, 35, 35, 35, 35, 35, 29, 29, 26, 1, 25, 23, 24, 27,
		1, 33, 33, 33, 33, 33, 33, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 16, 7, 17, 1, 28,
		1, 34, 34, 33, 33, 33, 34, 28, 28, 28, 28, 28, 28, 28, 32, 28,
		28, 28, 32, 28, 32, 28, 32, 28, 8, 28, 28, 4, 15, 9, 1, 1
	};

	private static final short tmStateMap[] = {
		0, 1
	};

	private static final short[] tmRuleSymbol = unpack_short(64,
		"\7\1\2\3\4\5\6\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35" +
		"\36\37\40\41\42\43\44\45\46\47\50\51\52\53\54\55\56\57\60\61\62\63\64\65\66\67\70" +
		"\71\72\73\74\75\76\77\100");

	private static final int tmClassesCount = 37;

	private static final short[] tmGoto = unpack_vc_short(1887,
		"\1\ufffe\1\2\1\3\42\2\2\uffff\1\4\1\uffff\1\5\1\6\1\7\1\uffff\1\4\1\10\1\11\1\12" +
		"\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32" +
		"\1\4\1\33\1\34\1\uffff\3\4\1\33\1\34\1\ufffc\1\2\1\ufffc\42\2\2\uffff\1\35\1\uffff" +
		"\1\36\1\37\2\uffff\1\40\23\uffff\1\40\1\41\2\uffff\3\40\1\41\1\uffff\2\ufffd\1\4" +
		"\5\ufffd\1\4\23\ufffd\2\4\2\ufffd\4\4\1\ufffd\45\uffdb\45\uffd5\1\uffff\5\7\1\42" +
		"\1\43\26\7\1\uffff\6\7\45\uffda\11\uffd7\1\44\16\uffd7\1\45\14\uffd7\45\uffd8\45" +
		"\uffd6\45\uffd4\27\uffd3\1\46\15\uffd3\17\uffd2\1\47\25\uffd2\45\uffd1\45\uffd0\45" +
		"\uffcf\45\uffce\45\uffcd\45\uffcc\26\uffff\1\50\16\uffff\27\uffc8\1\51\1\52\14\uffc8" +
		"\27\uffc1\1\53\15\uffc1\27\uffc2\1\54\15\uffc2\45\uffc0\45\uffbf\35\ufff6\1\33\5" +
		"\ufff6\1\33\1\ufff6\36\uffbe\1\34\5\uffbe\1\34\45\ufffb\45\ufff8\45\ufff7\3\ufffa" +
		"\1\55\4\ufffa\1\40\23\ufffa\2\40\2\ufffa\4\40\1\ufffa\35\ufff9\1\41\5\ufff9\1\41" +
		"\1\ufff9\45\ufff5\6\uffff\2\7\1\56\22\uffff\1\7\3\uffff\2\7\1\uffff\1\7\1\57\1\uffff" +
		"\45\uffd9\45\uffc6\45\uffc7\45\uffca\45\uffcb\45\uffc9\45\uffc5\45\uffc3\45\uffc4" +
		"\35\uffff\1\60\5\uffff\1\60\36\uffff\1\61\3\uffff\3\61\2\uffff\5\7\1\42\1\43\26\7" +
		"\1\uffff\4\7\1\62\1\7\35\ufffa\1\60\5\ufffa\1\60\1\ufffa\1\uffff\5\7\1\42\1\43\25" +
		"\7\1\61\1\uffff\2\7\3\61\1\7\1\uffff\5\7\1\42\1\43\26\7\1\uffff\6\7");

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
			tokenOffset = charOffset;

			for (state = tmStateMap[this.state]; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
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
			token.endoffset = currOffset;

			if (state == -1) {
				if (charOffset > tokenOffset) {
					tokenBuffer.append(data, tokenOffset, charOffset - tokenOffset);
				}
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset, token.endoffset);
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
			case 0:
				return createIdentifierToken(token, ruleIndex);
			case 3: // escid: /$[a-zA-Z_][A-Za-z_0-9]*(#[0-9]+)?/
				 token.value = tokenText().substring(1, tokenSize()); 
				break;
			case 4: // escint: /$[0-9]+/
				 token.value = Integer.parseInt(tokenText().substring(1, tokenSize())); 
				break;
			case 5: // '${': /$\{/
				state = States.query;
				 deep = 1;
				break;
			case 7: // icon: /[0-9]+/
				 token.value = Integer.parseInt(tokenText()); 
				break;
			case 8: // ccon: /'([^\n\\']|\\(['"?\\abfnrtv]|x[0-9a-fA-F]+|[0-7]([0-7][0-7]?)?))*'/
				 token.value = unescape(tokenText(), 1, tokenSize()-1); 
				break;
			case 34: // '{': /\{/
				 deep++; 
				break;
			case 35: // '}': /\}/
				 if (--deep == 0) { state = 0; } 
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

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<String,Integer>();
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
				 token.value = tokenText(); 
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
