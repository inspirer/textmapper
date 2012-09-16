/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textmapper.templates.types;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class TypesLexer {

	public static class LapgSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int identifier = 1;
		public static final int scon = 2;
		public static final int icon = 3;
		public static final int bcon = 4;
		public static final int _skip = 5;
		public static final int DOTDOT = 6;
		public static final int DOT = 7;
		public static final int MULT = 8;
		public static final int SEMICOLON = 9;
		public static final int COMMA = 10;
		public static final int COLON = 11;
		public static final int EQUAL = 12;
		public static final int EQUALGREATER = 13;
		public static final int LCURLY = 14;
		public static final int RCURLY = 15;
		public static final int LPAREN = 16;
		public static final int RPAREN = 17;
		public static final int LSQUARE = 18;
		public static final int RSQUARE = 19;
		public static final int Lclass = 20;
		public static final int Lextends = 21;
		public static final int Lint = 22;
		public static final int Lbool = 23;
		public static final int Lstring = 24;
		public static final int Lset = 25;
		public static final int Lchoice = 26;
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

	public TypesLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 31, 4, 1, 1, 31, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		31, 1, 5, 15, 1, 1, 1, 2, 25, 26, 17, 1, 19, 6, 16, 1,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 20, 18, 1, 21, 22, 1,
		1, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 27, 3, 28, 1, 29,
		1, 12, 29, 29, 29, 10, 11, 29, 29, 29, 29, 29, 13, 29, 29, 29,
		29, 29, 8, 14, 7, 9, 29, 29, 29, 29, 29, 23, 1, 24, 1, 1
	};

	private static final short[] lapg_lexemnum = unpack_short(27,
		"\1\2\3\4\5\5\6\7\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32");

	private static final short[] lapg_lexem = unpack_vc_short(1088,
		"\1\ufffe\1\uffff\1\1\1\uffff\1\2\1\3\1\4\1\5\3\6\1\7\3\6\1\10\1\11\1\12\1\13\1\14" +
		"\1\15\1\16\1\uffff\1\17\1\20\1\21\1\22\1\23\1\24\1\6\1\25\1\2\1\uffff\1\1\1\26\1" +
		"\27\1\uffff\33\1\4\ufff9\1\2\32\ufff9\1\2\1\uffff\2\3\1\30\1\uffff\1\31\32\3\36\uffff" +
		"\1\25\1\uffff\7\ufffd\1\6\1\32\6\6\16\ufffd\2\6\10\ufffd\10\6\16\ufffd\2\6\10\ufffd" +
		"\5\6\1\33\2\6\16\ufffd\2\6\1\ufffd\1\ufff8\3\10\1\ufff8\33\10\20\ufff6\1\34\17\ufff6" +
		"\40\ufff5\40\ufff4\40\ufff3\40\ufff2\26\ufff1\1\35\11\ufff1\40\uffef\40\uffee\40" +
		"\uffed\40\uffec\40\uffeb\40\uffea\36\ufffb\1\25\1\ufffb\40\ufffd\1\uffff\3\1\1\uffff" +
		"\33\1\1\uffff\3\3\1\uffff\33\3\40\ufffc\7\ufffd\2\6\1\36\5\6\16\ufffd\2\6\10\ufffd" +
		"\6\6\1\37\1\6\16\ufffd\2\6\1\ufffd\40\ufff7\40\ufff0\7\ufffd\3\6\1\40\4\6\16\ufffd" +
		"\2\6\10\ufffd\7\6\1\41\16\ufffd\2\6\1\ufffd\7\ufffa\10\6\16\ufffa\2\6\1\ufffa\7\ufffd" +
		"\3\6\1\40\4\6\16\ufffd\2\6\1\ufffd");

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
				state = lapg_lexem[state * 32 + mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.lexem = 0;
					lapg_n.sym = null;
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
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.lexem = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.lexem = 0;
				lapg_n.sym = null;
				tokenStart = -1;
				return lapg_n;
			}

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.lexem = lapg_lexemnum[-state - 3];
			lapg_n.sym = null;

		} while (lapg_n.lexem == -1 || !createToken(lapg_n, -state - 3));
		tokenStart = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) throws IOException {
		boolean spaceToken = false;
		switch (lexemIndex) {
			case 0:
				return createIdentifierToken(lapg_n, lexemIndex);
			case 1: // scon: /"([^\n\\"]|\\.)*"/
				 lapg_n.sym = unescape(current(), 1, token.length()-1); 
				break;
			case 2: // icon: /\-?[0-9]+/
				 lapg_n.sym = Integer.parseInt(current()); 
				break;
			case 3: // bcon: /true|false/
				 lapg_n.sym = current().equals("true"); 
				break;
			case 4: // _skip: /[\n\t\r ]+/
				spaceToken = true;
				break;
			case 5: // _skip: /#.*/
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<String,Integer>();
	static {
		subTokensOfIdentifier.put("class", 20);
		subTokensOfIdentifier.put("extends", 21);
		subTokensOfIdentifier.put("int", 22);
		subTokensOfIdentifier.put("bool", 23);
		subTokensOfIdentifier.put("string", 24);
		subTokensOfIdentifier.put("set", 25);
		subTokensOfIdentifier.put("choice", 26);
	}

	protected boolean createIdentifierToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfIdentifier.get(current());
		if (replacement != null) {
			lexemIndex = replacement;
			lapg_n.lexem = lapg_lexemnum[lexemIndex];
		}
		boolean spaceToken = false;
		switch(lexemIndex) {
			case 0:	// <default>
				 lapg_n.sym = current(); 
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
