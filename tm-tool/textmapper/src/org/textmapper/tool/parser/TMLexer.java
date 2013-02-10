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
package org.textmapper.tool.parser;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.textmapper.tool.parser.action.SActionLexer;
import org.textmapper.tool.parser.action.SActionParser;

public class TMLexer {

	public static class LapgSymbol {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int error = 1;
		public static final int ID = 2;
		public static final int regexp = 3;
		public static final int scon = 4;
		public static final int icon = 5;
		public static final int _skip = 6;
		public static final int _skip_comment = 7;
		public static final int PERCENT = 8;
		public static final int COLONCOLONEQUAL = 9;
		public static final int OR = 10;
		public static final int EQUAL = 11;
		public static final int EQUALGREATER = 12;
		public static final int SEMICOLON = 13;
		public static final int DOT = 14;
		public static final int COMMA = 15;
		public static final int COLON = 16;
		public static final int LSQUARE = 17;
		public static final int RSQUARE = 18;
		public static final int LPAREN = 19;
		public static final int LPARENQUESTIONMARKEXCLAMATION = 20;
		public static final int RPAREN = 21;
		public static final int LESS = 22;
		public static final int GREATER = 23;
		public static final int MULT = 24;
		public static final int PLUS = 25;
		public static final int QUESTIONMARK = 26;
		public static final int AMPERSAND = 27;
		public static final int ATSIGN = 28;
		public static final int Ltrue = 29;
		public static final int Lfalse = 30;
		public static final int Lnew = 31;
		public static final int Lseparator = 32;
		public static final int Las = 33;
		public static final int Lextends = 34;
		public static final int Linline = 35;
		public static final int Lprio = 36;
		public static final int Lshift = 37;
		public static final int Lreturns = 38;
		public static final int Linput = 39;
		public static final int Lleft = 40;
		public static final int Lright = 41;
		public static final int Lnonassoc = 42;
		public static final int Lnoeoi = 43;
		public static final int Lsoft = 44;
		public static final int Lclass = 45;
		public static final int Lspace = 46;
		public static final int Llayout = 47;
		public static final int Lreduce = 48;
		public static final int code = 49;
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
	private int templatesStart = -1;
	private boolean skipComments = true;

	int getTemplatesStart() {
		return templatesStart;
	}

	public void setSkipComments(boolean skip) {
		this.skipComments = skip;
	}

	private boolean skipAction() throws IOException {
		final int[] ind = new int[] { 0 };
		SActionLexer.ErrorReporter innerreporter = new SActionLexer.ErrorReporter() {
			public void error(int start, int line, String s) {
				reporter.error(start, start + 1, line, s);
			}
		};
		SActionLexer l = new SActionLexer(innerreporter) {
			@Override
			protected char nextChar() throws IOException {
				if (ind[0] < 2) {
					return ind[0]++ == 0 ? '{' : chr;
				}
				TMLexer.this.advance();
				return chr;
			}
		};
		SActionParser p = new SActionParser(innerreporter);
		try {
			p.parse(l);
		} catch (SActionParser.ParseException e) {
			reporter.error(getOffset(), getOffset() + 1, getLine(), "syntax error in action");
			return false;
		}
		return true;
	}

	private String unescape(String s, int start, int end) {
		StringBuilder sb = new StringBuilder();
		end = Math.min(end, s.length());
		for (int i = start; i < end; i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				if (++i == end) {
					break;
				}
				c = s.charAt(i);
				if (c == 'u' || c == 'x') {
					// FIXME process unicode
				} else if (c == 'n') {
					sb.append('\n');
				} else if (c == 'r') {
					sb.append('\r');
				} else if (c == 't') {
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

	public TMLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 32, 4, 1, 1, 9, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		32, 22, 6, 10, 1, 8, 27, 2, 20, 23, 25, 26, 17, 7, 16, 5,
		31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 11, 15, 24, 12, 14, 21,
		28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 18, 3, 19, 1, 30,
		1, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 29, 13, 1, 1, 1
	};

	private static final short[] lapg_lexemnum = unpack_short(49,
		"\2\3\4\5\0\6\7\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35" +
		"\36\37\40\41\42\43\44\45\46\47\50\51\52\53\54\55\56\57\60\61");

	private static final short[] lapg_lexem = unpack_vc_short(1419,
		"\1\ufffe\1\uffff\1\1\1\uffff\1\2\1\3\1\4\1\5\1\6\1\2\1\7\1\10\1\11\1\12\1\13\1\14" +
		"\1\15\1\16\1\17\1\20\1\21\1\22\1\uffff\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1" +
		"\33\1\2\1\uffff\1\1\1\34\1\35\1\uffff\34\1\4\ufff8\1\2\4\ufff8\1\2\26\ufff8\1\2\1" +
		"\uffff\2\3\1\36\1\uffff\1\37\33\3\1\uffff\2\4\1\40\1\uffff\1\4\1\41\32\4\37\uffff" +
		"\1\33\1\uffff\10\ufff6\1\42\30\ufff6\1\ufff7\3\7\1\43\34\7\13\uffee\1\44\25\uffee" +
		"\16\ufff3\1\45\22\ufff3\41\ufff4\41\uffe7\41\ufff1\41\ufff0\41\uffef\41\uffed\41" +
		"\uffec\25\uffeb\1\46\13\uffeb\41\uffe4\41\uffe9\41\uffe8\41\uffe6\41\uffe5\41\uffe3" +
		"\41\uffe2\41\uffcd\7\ufffd\1\47\26\ufffd\2\32\1\ufffd\37\ufffa\1\33\1\ufffa\41\ufffd" +
		"\1\uffff\3\1\1\uffff\34\1\1\uffff\3\3\1\uffff\34\3\41\ufffc\1\uffff\3\4\1\uffff\34" +
		"\4\41\ufffb\1\ufff9\3\42\1\50\34\42\41\ufff7\14\uffff\1\51\24\uffff\41\ufff2\26\uffff" +
		"\1\52\21\uffff\1\47\26\uffff\2\32\1\uffff\41\ufff9\41\ufff5\41\uffea");

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
				state = lapg_lexem[state * 33 + mapCharacter(chr)];
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
				return createIDToken(lapg_n, lexemIndex);
			case 1: // regexp: /\/([^\/\\\n]|\\.)*\//
				 lapg_n.value = token.toString().substring(1, token.length()-1); 
				break;
			case 2: // scon: /"([^\n\\"]|\\.)*"/
				 lapg_n.value = unescape(current(), 1, token.length()-1); 
				break;
			case 3: // icon: /\-?[0-9]+/
				 lapg_n.value = Integer.parseInt(current()); 
				break;
			case 4: // eoi: /%%.*(\r?\n)?/
				 templatesStart = lapg_n.endoffset; 
				break;
			case 5: // _skip: /[\n\r\t ]+/
				spaceToken = true;
				break;
			case 6: // _skip_comment: /#.*(\r?\n)?/
				 spaceToken = skipComments; 
				break;
			case 48: // code: /\{/
				 skipAction(); lapg_n.endoffset = getOffset(); 
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfID = new HashMap<String,Integer>();
	static {
		subTokensOfID.put("true", 28);
		subTokensOfID.put("false", 29);
		subTokensOfID.put("new", 30);
		subTokensOfID.put("separator", 31);
		subTokensOfID.put("as", 32);
		subTokensOfID.put("extends", 33);
		subTokensOfID.put("inline", 34);
		subTokensOfID.put("prio", 35);
		subTokensOfID.put("shift", 36);
		subTokensOfID.put("returns", 37);
		subTokensOfID.put("input", 38);
		subTokensOfID.put("left", 39);
		subTokensOfID.put("right", 40);
		subTokensOfID.put("nonassoc", 41);
		subTokensOfID.put("no-eoi", 42);
		subTokensOfID.put("soft", 43);
		subTokensOfID.put("class", 44);
		subTokensOfID.put("space", 45);
		subTokensOfID.put("layout", 46);
		subTokensOfID.put("reduce", 47);
	}

	protected boolean createIDToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfID.get(current());
		if (replacement != null) {
			lexemIndex = replacement;
			lapg_n.symbol = lapg_lexemnum[lexemIndex];
		}
		boolean spaceToken = false;
		switch(lexemIndex) {
			case 35:	// prio (soft)
			case 36:	// shift (soft)
			case 37:	// returns (soft)
			case 38:	// input (soft)
			case 39:	// left (soft)
			case 40:	// right (soft)
			case 41:	// nonassoc (soft)
			case 42:	// no-eoi (soft)
			case 43:	// soft (soft)
			case 44:	// class (soft)
			case 45:	// space (soft)
			case 46:	// layout (soft)
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
