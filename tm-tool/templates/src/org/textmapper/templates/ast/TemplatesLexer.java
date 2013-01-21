/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

	public static class LapgSymbol {
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

	public interface Lexems {
		public static final int eoi = 0;
		public static final int any = 1;
		public static final int escdollar = 2;
		public static final int escid = 3;
		public static final int escint = 4;
		public static final int DOLLARLCURLY = 5;
		public static final int DOLLARSLASH = 6;
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
		public static final int LCURLY = 35;
		public static final int RCURLY = 36;
		public static final int MINUSRCURLY = 37;
		public static final int PLUS = 38;
		public static final int MINUS = 39;
		public static final int MULT = 40;
		public static final int SLASH = 41;
		public static final int PERCENT = 42;
		public static final int EXCLAMATION = 43;
		public static final int OR = 44;
		public static final int LSQUARE = 45;
		public static final int RSQUARE = 46;
		public static final int LPAREN = 47;
		public static final int RPAREN = 48;
		public static final int DOT = 49;
		public static final int COMMA = 50;
		public static final int AMPERSANDAMPERSAND = 51;
		public static final int OROR = 52;
		public static final int EQUALEQUAL = 53;
		public static final int EQUAL = 54;
		public static final int EXCLAMATIONEQUAL = 55;
		public static final int MINUSGREATER = 56;
		public static final int EQUALGREATER = 57;
		public static final int LESSEQUAL = 58;
		public static final int GREATEREQUAL = 59;
		public static final int LESS = 60;
		public static final int GREATER = 61;
		public static final int COLON = 62;
		public static final int QUESTIONMARK = 63;
		public static final int _skip = 64;
		public static final int error = 65;
	}

	public interface ErrorReporter {
		void error(int start, int end, int line, String s);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen, l, tokenStart;
	private char chr;

	private int state;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;

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
		this.stream = stream;
		this.state = 0;
		datalen = stream.read(data);
		l = 0;
		tokenStart = -1;
		chr = l < datalen ? data[l++] : 0;
	}

	protected void advance() throws IOException {
		if (chr == 0) return;
		currOffset++;
		if (chr == '\n') {
			currLine++;
		}
		if (l >= datalen) {
			if (tokenStart >= 0) {
				token.append(data, tokenStart, l - tokenStart);
				tokenStart = 0;
			}
			l = 0;
			datalen = stream.read(data);
		}
		chr = l < datalen ? data[l++] : 0;
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

	private static final short lapg_char2no[] = {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 36, 30, 1, 1, 36, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		36, 14, 31, 3, 2, 13, 22, 6, 18, 19, 12, 11, 21, 10, 20, 5,
		35, 35, 35, 35, 35, 35, 35, 35, 29, 29, 26, 1, 25, 23, 24, 27,
		1, 33, 33, 33, 33, 33, 33, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 16, 7, 17, 1, 28,
		1, 34, 34, 33, 33, 33, 34, 28, 28, 28, 28, 28, 28, 28, 32, 28,
		28, 28, 32, 28, 32, 28, 32, 28, 8, 28, 28, 4, 15, 9, 1, 1
	};

	private static final short[] lapg_lexemnum = unpack_short(64,
		"\7\1\2\3\4\5\6\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35" +
		"\36\37\40\41\42\43\44\45\46\47\50\51\52\53\54\55\56\57\60\61\62\63\64\65\66\67\70" +
		"\71\72\73\74\75\76\77\100");

	private static final short[] lapg_lexem = unpack_vc_short(1887,
		"\1\ufffe\1\2\1\3\42\2\4\uffff\1\4\1\5\1\6\1\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1" +
		"\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\7\1\33\1\34" +
		"\1\uffff\3\7\1\33\1\34\1\ufffc\1\2\1\ufffc\42\2\2\uffff\1\35\1\uffff\1\36\1\37\2" +
		"\uffff\1\40\23\uffff\1\40\1\41\2\uffff\3\40\1\41\1\uffff\45\uffdb\45\uffd5\1\uffff" +
		"\5\6\1\42\1\43\26\6\1\uffff\6\6\10\ufffd\1\7\23\ufffd\2\7\2\ufffd\4\7\1\ufffd\45" +
		"\uffda\11\uffd7\1\44\16\uffd7\1\45\14\uffd7\45\uffd8\45\uffd6\45\uffd4\27\uffd3\1" +
		"\46\15\uffd3\17\uffd2\1\47\25\uffd2\45\uffd1\45\uffd0\45\uffcf\45\uffce\45\uffcd" +
		"\45\uffcc\26\uffff\1\50\16\uffff\27\uffc8\1\51\1\52\14\uffc8\27\uffc1\1\53\15\uffc1" +
		"\27\uffc2\1\54\15\uffc2\45\uffc0\45\uffbf\35\ufff6\1\33\5\ufff6\1\33\1\ufff6\36\uffbe" +
		"\1\34\5\uffbe\1\34\45\ufffb\45\ufff8\45\ufff7\3\ufffa\1\55\4\ufffa\1\40\23\ufffa" +
		"\2\40\2\ufffa\4\40\1\ufffa\35\ufff9\1\41\5\ufff9\1\41\1\ufff9\45\ufff5\6\uffff\2" +
		"\6\1\56\22\uffff\1\6\3\uffff\2\6\1\uffff\1\6\1\57\1\uffff\45\uffd9\45\uffc6\45\uffc7" +
		"\45\uffca\45\uffcb\45\uffc9\45\uffc5\45\uffc3\45\uffc4\35\uffff\1\60\5\uffff\1\60" +
		"\36\uffff\1\61\3\uffff\3\61\2\uffff\5\6\1\42\1\43\26\6\1\uffff\4\6\1\62\1\6\35\ufffa" +
		"\1\60\5\ufffa\1\60\1\ufffa\1\uffff\5\6\1\42\1\43\25\6\1\61\1\uffff\2\6\3\61\1\6\1" +
		"\uffff\5\6\1\42\1\43\26\6\1\uffff\6\6");

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
		if (chr >= 0 && chr < 128) {
			return lapg_char2no[chr];
		}
		return 1;
	}

	public LapgSymbol next() throws IOException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {
			lapg_n.offset = currOffset;
			tokenLine = lapg_n.line = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			tokenStart = l - 1;

			for (state = this.state; state >= 0; ) {
				state = lapg_lexem[state * 37 + mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.symbol = 0;
					lapg_n.value = null;
					reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, "Unexpected end of input reached");
					tokenStart = -1;
					return lapg_n;
				}
				if (state >= -1 && chr != 0) {
					currOffset++;
					if (chr == '\n') {
						currLine++;
					}
					if (l >= datalen) {
						token.append(data, tokenStart, l - tokenStart);
						tokenStart = l = 0;
						datalen = stream.read(data);
					}
					chr = l < datalen ? data[l++] : 0;
				}
			}
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				if (l - 1 > tokenStart) {
					token.append(data, tokenStart, l - 1 - tokenStart);
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.symbol = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.symbol = 0;
				lapg_n.value = null;
				tokenStart = -1;
				return lapg_n;
			}

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.symbol = lapg_lexemnum[-state - 3];
			lapg_n.value = null;

		} while (lapg_n.symbol == -1 || !createToken(lapg_n, -state - 3));
		tokenStart = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) throws IOException {
		boolean spaceToken = false;
		switch (lexemIndex) {
			case 0:
				return createIdentifierToken(lapg_n, lexemIndex);
			case 3: // escid: /$[a-zA-Z_][A-Za-z_0-9]*(#[0-9]+)?/
				 lapg_n.value = token.toString().substring(1, token.length()); 
				break;
			case 4: // escint: /$[0-9]+/
				 lapg_n.value = Integer.parseInt(token.toString().substring(1, token.length())); 
				break;
			case 5: // '${': /$\{/
				state = States.query;
				 deep = 1;
				break;
			case 7: // icon: /[0-9]+/
				 lapg_n.value = Integer.parseInt(current()); 
				break;
			case 8: // ccon: /'([^\n\\']|\\(['"?\\abfnrtv]|x[0-9a-fA-F]+|[0-7]([0-7][0-7]?)?))*'/
				 lapg_n.value = unescape(current(), 1, token.length()-1); 
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

	protected boolean createIdentifierToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfIdentifier.get(current());
		if (replacement != null) {
			lexemIndex = replacement;
			lapg_n.symbol = lapg_lexemnum[lexemIndex];
		}
		boolean spaceToken = false;
		switch(lexemIndex) {
			case 0:	// <default>
				 lapg_n.value = current(); 
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
