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
package org.textway.lapg.regex;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class RegexDefLexer {

	public static class LapgSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int expand = 1;
		public static final int _char = 2;
		public static final int escaped = 3;
		public static final int charclass = 4;
		public static final int DOT = 5;
		public static final int MULT = 6;
		public static final int PLUS = 7;
		public static final int QUESTIONMARK = 8;
		public static final int quantifier = 9;
		public static final int op_minus = 10;
		public static final int op_union = 11;
		public static final int op_intersect = 12;
		public static final int LPAREN = 13;
		public static final int OR = 14;
		public static final int RPAREN = 15;
		public static final int LPARENQUESTIONMARK = 16;
		public static final int LSQUARE = 17;
		public static final int LSQUAREXOR = 18;
		public static final int kw_eoi = 19;
		public static final int RSQUARE = 20;
		public static final int MINUS = 21;
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

	private void quantifierReady() {
		if (chr == 0) {
			if (group == 1) group = 0;
			return;
		}
		if (group == 0) group = 1;
	}

	public RegexDefLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
		0, 1, 1, 1, 1, 1, 1, 1, 1, 30, 30, 1, 1, 30, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 19, 1, 20, 22, 14, 15, 17, 18, 13, 29,
		32, 32, 32, 32, 32, 32, 32, 32, 28, 28, 23, 1, 1, 1, 1, 16,
		1, 34, 34, 34, 35, 34, 34, 27, 27, 27, 27, 27, 27, 27, 27, 27,
		31, 27, 27, 37, 27, 36, 27, 37, 33, 27, 27, 24, 4, 26, 25, 27,
		1, 5, 6, 34, 35, 34, 7, 27, 27, 39, 27, 27, 27, 27, 8, 27,
		12, 27, 9, 38, 10, 36, 11, 37, 33, 27, 27, 2, 21, 3, 1, 1
	};

	private static final short[] lapg_lexemnum = unpack_short(35,
		"\1\2\3\3\3\3\3\3\3\3\3\3\3\4\4\5\6\7\10\11\12\13\14\2\15\16\17\20\21\22\2\23\24\25" +
		"\2");

	private static final short[] lapg_lexem = unpack_vc_short(2400,
		"\1\ufffe\1\3\1\4\1\3\1\5\10\3\1\6\3\7\1\3\1\10\1\3\1\11\1\12\1\13\1\3\1\14\1\3\1" +
		"\uffff\2\3\1\uffff\12\3\1\uffff\1\3\1\15\1\3\1\5\10\3\1\6\1\16\1\17\1\20\1\3\1\10" +
		"\1\3\1\11\1\12\1\13\1\3\1\14\1\3\1\uffff\2\3\1\uffff\12\3\1\uffff\3\3\1\5\10\3\1" +
		"\6\3\7\1\3\1\21\1\3\3\22\1\3\1\uffff\1\3\1\23\2\3\1\uffff\12\3\55\ufffc\10\24\16" +
		"\ufffc\1\24\3\ufffc\1\24\1\ufffc\7\24\1\uffff\4\25\1\26\1\27\1\30\1\31\1\32\1\33" +
		"\1\34\1\35\17\25\1\uffff\1\25\2\uffff\1\36\1\37\1\25\1\40\1\41\2\40\1\25\50\uffee" +
		"\50\uffe6\50\uffdf\20\uffe5\1\42\27\uffe5\50\uffe4\50\uffe3\31\uffe1\1\43\16\uffe1" +
		"\2\ufffc\1\44\1\45\1\ufffc\10\24\5\ufffc\1\46\1\47\7\ufffc\1\24\1\50\2\ufffc\1\24" +
		"\1\50\7\24\50\uffed\50\uffec\50\uffeb\50\uffdc\50\uffdb\50\uffdd\3\uffff\1\51\1\uffff" +
		"\10\24\5\uffff\1\24\10\uffff\2\24\2\uffff\11\24\50\ufffb\50\ufffa\50\ufff9\50\ufff8" +
		"\50\ufff7\50\ufff6\50\ufff5\50\ufff4\2\uffff\1\52\105\uffff\1\53\14\uffff\3\54\24" +
		"\uffff\1\54\3\uffff\1\54\1\uffff\2\54\4\uffff\50\ufff0\5\uffff\3\55\24\uffff\1\55" +
		"\3\uffff\1\55\1\uffff\2\55\26\uffff\1\56\23\uffff\2\56\50\uffe0\2\uffff\1\44\1\45" +
		"\44\uffff\50\uffe8\3\uffff\1\57\67\uffff\1\60\27\uffff\1\61\15\uffff\1\62\12\uffff" +
		"\1\50\3\uffff\1\50\7\uffff\50\ufffd\5\uffff\10\63\16\uffff\2\63\2\uffff\11\63\40" +
		"\uffff\1\64\14\uffff\3\65\24\uffff\1\65\3\uffff\1\65\1\uffff\2\65\11\uffff\3\66\24" +
		"\uffff\1\66\3\uffff\1\66\1\uffff\2\66\26\uffff\1\56\4\uffff\1\67\16\uffff\2\56\50" +
		"\uffe9\3\uffff\1\70\44\uffff\50\uffea\3\uffff\1\61\30\uffff\1\62\3\uffff\1\62\12" +
		"\uffff\1\71\1\uffff\10\63\16\uffff\2\63\2\uffff\11\63\50\ufff3\50\ufff2\5\uffff\3" +
		"\72\24\uffff\1\72\3\uffff\1\72\1\uffff\2\72\4\uffff\50\uffe2\50\uffe7\50\uffef\5" +
		"\uffff\3\73\24\uffff\1\73\3\uffff\1\73\1\uffff\2\73\4\uffff\50\ufff1");

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
			tokenLine = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			tokenStart = l - 1;

			for (state = group; state >= 0; ) {
				state = lapg_lexem[state * 40 + mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.lexem = 0;
					lapg_n.sym = null;
					reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), "Unexpected end of input reached");
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
				reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
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
				return createExpandToken(lapg_n, lexemIndex);
			case 1:
				 lapg_n.sym = current().charAt(0); quantifierReady(); break; 
			case 2:
				 lapg_n.sym = current().charAt(1); quantifierReady(); break; 
			case 3:
				 lapg_n.sym = (char) 7; quantifierReady(); break; 
			case 4:
				 lapg_n.sym = '\b'; quantifierReady(); break; 
			case 5:
				 lapg_n.sym = '\f'; quantifierReady(); break; 
			case 6:
				 lapg_n.sym = '\n'; quantifierReady(); break; 
			case 7:
				 lapg_n.sym = '\r'; quantifierReady(); break; 
			case 8:
				 lapg_n.sym = '\t'; quantifierReady(); break; 
			case 9:
				 lapg_n.sym = (char) 0xb; quantifierReady(); break; 
			case 10:
				 lapg_n.sym = RegexUtil.unescapeOct(current().substring(1)); quantifierReady(); break; 
			case 11:
				 lapg_n.sym = RegexUtil.unescapeHex(current().substring(2)); quantifierReady(); break; 
			case 12:
				 lapg_n.sym = RegexUtil.unescapeHex(current().substring(2)); quantifierReady(); break; 
			case 13:
				 lapg_n.sym = current().substring(1); quantifierReady(); break; 
			case 14:
				 lapg_n.sym = current().substring(3, current().length() - 1); quantifierReady(); break; 
			case 15:
				 quantifierReady(); break; 
			case 16:
				 group = 0; break; 
			case 17:
				 group = 0; break; 
			case 18:
				 group = 0; break; 
			case 19:
				 group = 0; break; 
			case 20:
				 group = 0; break; 
			case 21:
				 group = 0; break; 
			case 22:
				 group = 0; break; 
			case 23:
				 lapg_n.sym = current().charAt(0); quantifierReady(); break; 
			case 24:
				 group = 0; break; 
			case 25:
				 group = 0; break; 
			case 26:
				 quantifierReady(); break; 
			case 27:
				 group = 0; break; 
			case 28:
				 group = 2; break; 
			case 29:
				 group = 2; break; 
			case 30:
				 lapg_n.sym = current().charAt(0); quantifierReady(); break; 
			case 32:
				 group = 0; quantifierReady(); break; 
			case 34:
				 lapg_n.sym = current().charAt(0); break; 
		}
		return true;
	}

	private static Map<String,Integer> subTokensOfExpand = new HashMap<String,Integer>();
	static {
		subTokensOfExpand.put("{eoi}", 31);
	}

	protected boolean createExpandToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfExpand.get(current());
		if (replacement != null) {
			lexemIndex = replacement;
			lapg_n.lexem = lapg_lexemnum[lexemIndex];
		}
		switch(lexemIndex) {
			case 31:	// {eoi}
				 group = 0; break; 
			case 0:	// <default>
				 quantifierReady(); break; 
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
