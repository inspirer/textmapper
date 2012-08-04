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
package org.textmapper.lapg.parser;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.textmapper.lapg.parser.action.SActionLexer;
import org.textmapper.lapg.parser.action.SActionParser;

public class LapgLexer {

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
		public static final int error = 2;
		public static final int regexp = 3;
		public static final int scon = 4;
		public static final int icon = 5;
		public static final int PERCENT = 6;
		public static final int _skip = 7;
		public static final int _skip_comment = 8;
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
		public static final int Lprio = 32;
		public static final int Lshift = 33;
		public static final int Linput = 34;
		public static final int Lleft = 35;
		public static final int Lright = 36;
		public static final int Lnonassoc = 37;
		public static final int Lnoeoi = 38;
		public static final int Lsoft = 39;
		public static final int Lclass = 40;
		public static final int Lspace = 41;
		public static final int Llayout = 42;
		public static final int Lreduce = 43;
		public static final int code = 44;
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

	private int group;

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
				LapgLexer.this.advance();
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

	public LapgLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.stream = stream;
		this.group = 0;
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
		return group;
	}

	public void setState(int state) {
		this.group = state;
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
		31, 21, 6, 9, 1, 8, 26, 2, 19, 22, 24, 25, 16, 7, 15, 5,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 10, 14, 23, 11, 13, 20,
		27, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 17, 3, 18, 1, 29,
		1, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29,
		29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 28, 12, 1, 1, 1
	};

	private static final short[] lapg_lexemnum = unpack_short(44,
		"\1\3\4\5\0\6\7\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35" +
		"\36\37\40\41\42\43\44\45\46\47\50\51\52\53\54");

	private static final short[] lapg_lexem = unpack_vc_short(1376,
		"\1\ufffe\1\uffff\1\1\1\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15" +
		"\1\16\1\17\1\20\1\21\1\22\1\uffff\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1" +
		"\34\1\uffff\1\1\1\35\1\36\1\uffff\33\1\10\ufff7\1\37\27\ufff7\1\uffff\2\3\1\40\1" +
		"\uffff\1\41\32\3\1\uffff\2\4\1\42\1\uffff\1\4\1\43\31\4\36\uffff\1\33\1\uffff\40" +
		"\ufff8\1\ufff6\3\7\1\ufff6\33\7\12\uffee\1\44\25\uffee\15\ufff3\1\45\22\ufff3\40" +
		"\ufff4\40\uffe7\40\ufff1\40\ufff0\40\uffef\40\uffed\40\uffec\24\uffeb\1\46\13\uffeb" +
		"\40\uffe4\40\uffe9\40\uffe8\40\uffe6\40\uffe5\40\uffe3\40\uffe2\40\uffd2\7\ufffd" +
		"\1\47\25\ufffd\2\32\1\ufffd\36\ufffa\1\33\1\ufffa\37\ufff7\1\34\40\ufffd\1\uffff" +
		"\3\1\1\uffff\33\1\10\ufff8\1\50\27\ufff8\1\uffff\3\3\1\uffff\33\3\40\ufffc\1\uffff" +
		"\3\4\1\uffff\33\4\40\ufffb\13\uffff\1\51\24\uffff\40\ufff2\25\uffff\1\52\21\uffff" +
		"\1\47\25\uffff\2\32\1\uffff\1\ufff9\3\50\1\ufff9\33\50\40\ufff5\40\uffea");

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

			for (state = group; state >= 0; ) {
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
		switch (lexemIndex) {
			case 0:
				return createIdentifierToken(lapg_n, lexemIndex);
			case 1:
				 lapg_n.sym = token.toString().substring(1, token.length()-1); break; 
			case 2:
				 lapg_n.sym = unescape(current(), 1, token.length()-1); break; 
			case 3:
				 lapg_n.sym = Integer.parseInt(current()); break; 
			case 4:
				 templatesStart = lapg_n.endoffset; break; 
			case 6:
				 return false; 
			case 7:
				 return !skipComments; 
			case 43:
				 skipAction(); lapg_n.endoffset = getOffset(); break; 
		}
		return true;
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<String,Integer>();
	static {
		subTokensOfIdentifier.put("true", 28);
		subTokensOfIdentifier.put("false", 29);
		subTokensOfIdentifier.put("new", 30);
		subTokensOfIdentifier.put("prio", 31);
		subTokensOfIdentifier.put("shift", 32);
		subTokensOfIdentifier.put("input", 33);
		subTokensOfIdentifier.put("left", 34);
		subTokensOfIdentifier.put("right", 35);
		subTokensOfIdentifier.put("nonassoc", 36);
		subTokensOfIdentifier.put("no-eoi", 37);
		subTokensOfIdentifier.put("soft", 38);
		subTokensOfIdentifier.put("class", 39);
		subTokensOfIdentifier.put("space", 40);
		subTokensOfIdentifier.put("layout", 41);
		subTokensOfIdentifier.put("reduce", 42);
	}

	protected boolean createIdentifierToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfIdentifier.get(current());
		if (replacement != null) {
			lexemIndex = replacement;
			lapg_n.lexem = lapg_lexemnum[lexemIndex];
		}
		switch(lexemIndex) {
			case 31:	// prio (soft)
			case 32:	// shift (soft)
			case 33:	// input (soft)
			case 34:	// left (soft)
			case 35:	// right (soft)
			case 36:	// nonassoc (soft)
			case 37:	// no-eoi (soft)
			case 38:	// soft (soft)
			case 39:	// class (soft)
			case 40:	// space (soft)
			case 41:	// layout (soft)
			case 0:	// <default>
				 lapg_n.sym = current(); break; 
		}
		return true;
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
