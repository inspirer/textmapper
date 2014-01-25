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

	public interface States {
		public static final int initial = 0;
		public static final int afterAt = 1;
		public static final int afterAtID = 2;
	}

	public interface Lexems {
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int regexp = 1;
		public static final int scon = 2;
		public static final int icon = 3;
		public static final int _skip = 4;
		public static final int _skip_comment = 5;
		public static final int PERCENT = 6;
		public static final int COLONCOLONEQUAL = 7;
		public static final int COLONCOLON = 8;
		public static final int OR = 9;
		public static final int EQUAL = 10;
		public static final int EQUALGREATER = 11;
		public static final int SEMICOLON = 12;
		public static final int DOT = 13;
		public static final int COMMA = 14;
		public static final int COLON = 15;
		public static final int LSQUARE = 16;
		public static final int RSQUARE = 17;
		public static final int LPAREN = 18;
		public static final int LPARENQUESTIONMARKEXCLAMATION = 19;
		public static final int RPAREN = 20;
		public static final int RCURLY = 21;
		public static final int LESS = 22;
		public static final int GREATER = 23;
		public static final int MULT = 24;
		public static final int PLUS = 25;
		public static final int PLUSEQUAL = 26;
		public static final int QUESTIONMARK = 27;
		public static final int AMPERSAND = 28;
		public static final int ATSIGN = 29;
		public static final int error = 30;
		public static final int ID = 31;
		public static final int Ltrue = 32;
		public static final int Lfalse = 33;
		public static final int Lnew = 34;
		public static final int Lseparator = 35;
		public static final int Las = 36;
		public static final int Limport = 37;
		public static final int Linline = 38;
		public static final int Lprio = 39;
		public static final int Lshift = 40;
		public static final int Lreturns = 41;
		public static final int Linput = 42;
		public static final int Lleft = 43;
		public static final int Lright = 44;
		public static final int Lnonassoc = 45;
		public static final int Lnoeoi = 46;
		public static final int Lsoft = 47;
		public static final int Lclass = 48;
		public static final int Linterface = 49;
		public static final int Lvoid = 50;
		public static final int Lspace = 51;
		public static final int Llayout = 52;
		public static final int Llanguage = 53;
		public static final int Llalr = 54;
		public static final int Llexer = 55;
		public static final int Lparser = 56;
		public static final int Lreduce = 57;
		public static final int code = 58;
		public static final int LCURLY = 59;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset, int endoffset);
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

	private static final short tmCharClass[] = {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 33, 4, 1, 1, 9, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		33, 22, 6, 10, 1, 8, 28, 2, 20, 23, 26, 27, 17, 7, 16, 5,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 11, 15, 25, 12, 14, 21,
		29, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 18, 3, 19, 1, 31,
		1, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 30, 13, 24, 1, 1
	};

	private static final short tmStateMap[] = {
		0, 0, 1
	};

	private static final short[] tmRuleSymbol = unpack_short(59,
		"\37\1\2\3\0\4\5\6\7\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34" +
		"\35\40\41\42\43\44\45\46\47\50\51\52\53\54\55\56\57\60\61\62\63\64\65\66\67\70\71" +
		"\72\73");

	private static final int tmClassesCount = 34;

	private static final short[] tmGoto = unpack_vc_short(1598,
		"\1\ufffe\1\uffff\1\2\1\uffff\1\3\1\4\1\5\1\6\1\7\1\3\1\10\1\11\1\12\1\13\1\14\1\15" +
		"\1\16\1\17\1\20\1\21\1\22\1\23\1\uffff\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1" +
		"\34\1\35\1\3\2\uffff\1\2\1\uffff\1\3\1\4\1\5\1\6\1\7\1\3\1\10\1\11\1\12\1\13\1\14" +
		"\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\uffff\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1" +
		"\36\1\34\1\35\1\3\1\uffff\1\2\1\37\1\40\1\uffff\35\2\4\ufff8\1\3\4\ufff8\1\3\27\ufff8" +
		"\1\3\1\uffff\2\4\1\41\1\uffff\1\42\34\4\1\uffff\2\5\1\43\1\uffff\1\5\1\44\33\5\40" +
		"\uffff\1\35\1\uffff\10\ufff6\1\45\31\ufff6\1\ufff7\3\10\1\46\35\10\13\uffed\1\47" +
		"\26\uffed\16\ufff2\1\50\23\ufff2\42\ufff3\42\uffe5\42\ufff0\42\uffef\42\uffee\42" +
		"\uffec\42\uffeb\25\uffea\1\51\14\uffea\42\uffe1\42\uffe8\42\uffe7\42\uffe6\42\uffe4" +
		"\14\uffe3\1\52\25\uffe3\42\uffe0\42\uffdf\42\uffc4\7\ufffd\1\53\27\ufffd\2\34\1\ufffd" +
		"\40\ufffa\1\35\1\ufffa\42\uffc3\42\ufffd\1\uffff\3\2\1\uffff\35\2\1\uffff\3\4\1\uffff" +
		"\35\4\42\ufffc\1\uffff\3\5\1\uffff\35\5\42\ufffb\1\ufff9\3\45\1\54\35\45\42\ufff7" +
		"\14\ufff4\1\55\25\ufff4\42\ufff1\26\uffff\1\56\13\uffff\42\uffe2\7\uffff\1\53\27" +
		"\uffff\2\34\1\uffff\42\ufff9\42\ufff5\42\uffe9");

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
			return tmCharClass[chr];
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

			for (state = tmStateMap[this.state]; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.symbol = 0;
					lapg_n.value = null;
					reporter.error("Unexpected end of input reached", lapg_n.line, lapg_n.offset, lapg_n.endoffset);
					lapg_n.offset = currOffset;
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
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, current()), lapg_n.line, lapg_n.offset, lapg_n.endoffset);
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

			lapg_n.symbol = tmRuleSymbol[-state - 3];
			lapg_n.value = null;

		} while (lapg_n.symbol == -1 || !createToken(lapg_n, -state - 3));
		tokenStart = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 0:
				return createIDToken(lapg_n, ruleIndex);
			case 1: // regexp: /\/([^\/\\\n]|\\.)*\//
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				 lapg_n.value = token.toString().substring(1, token.length()-1); 
				break;
			case 2: // scon: /"([^\n\\"]|\\.)*"/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				 lapg_n.value = unescape(current(), 1, token.length()-1); 
				break;
			case 3: // icon: /\-?[0-9]+/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				 lapg_n.value = Integer.parseInt(current()); 
				break;
			case 4: // eoi: /%%.*(\r?\n)?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				 templatesStart = lapg_n.endoffset; 
				break;
			case 5: // _skip: /[\n\r\t ]+/
				spaceToken = true;
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 6: // _skip_comment: /#.*(\r?\n)?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				 spaceToken = skipComments; 
				break;
			case 7: // '%': /%/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 8: // '::=': /::=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 9: // '::': /::/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 10: // '|': /\|/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 11: // '=': /=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 12: // '=>': /=>/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 13: // ';': /;/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 14: // '.': /\./
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 15: // ',': /,/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 16: // ':': /:/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 17: // '[': /\[/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 18: // ']': /\]/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 19: // '(': /\(/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 20: // '(?!': /\(\?!/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 21: // ')': /\)/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 22: // '}': /\}/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 23: // '<': /</
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 24: // '>': />/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 25: // '*': /\*/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 26: // '+': /\+/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 27: // '+=': /\+=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 28: // '?': /\?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 29: // '&': /&/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 30: // '@': /@/
				state = States.afterAt;
				break;
			case 57: // code: /\{/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
				}
				 skipAction(); lapg_n.endoffset = getOffset(); 
				break;
			case 58: // '{': /\{/
				state = States.initial;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfID = new HashMap<String,Integer>();
	static {
		subTokensOfID.put("true", 31);
		subTokensOfID.put("false", 32);
		subTokensOfID.put("new", 33);
		subTokensOfID.put("separator", 34);
		subTokensOfID.put("as", 35);
		subTokensOfID.put("import", 36);
		subTokensOfID.put("inline", 37);
		subTokensOfID.put("prio", 38);
		subTokensOfID.put("shift", 39);
		subTokensOfID.put("returns", 40);
		subTokensOfID.put("input", 41);
		subTokensOfID.put("left", 42);
		subTokensOfID.put("right", 43);
		subTokensOfID.put("nonassoc", 44);
		subTokensOfID.put("no-eoi", 45);
		subTokensOfID.put("soft", 46);
		subTokensOfID.put("class", 47);
		subTokensOfID.put("interface", 48);
		subTokensOfID.put("void", 49);
		subTokensOfID.put("space", 50);
		subTokensOfID.put("layout", 51);
		subTokensOfID.put("language", 52);
		subTokensOfID.put("lalr", 53);
		subTokensOfID.put("lexer", 54);
		subTokensOfID.put("parser", 55);
		subTokensOfID.put("reduce", 56);
	}

	protected boolean createIDToken(LapgSymbol lapg_n, int ruleIndex) {
		Integer replacement = subTokensOfID.get(current());
		if (replacement != null) {
			ruleIndex = replacement;
			lapg_n.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 31:	// true
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 32:	// false
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 33:	// new
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 34:	// separator
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 35:	// as
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 36:	// import
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 56:	// reduce
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 37:	// inline (soft)
			case 38:	// prio (soft)
			case 39:	// shift (soft)
			case 40:	// returns (soft)
			case 41:	// input (soft)
			case 42:	// left (soft)
			case 43:	// right (soft)
			case 44:	// nonassoc (soft)
			case 45:	// no-eoi (soft)
			case 46:	// soft (soft)
			case 47:	// class (soft)
			case 48:	// interface (soft)
			case 49:	// void (soft)
			case 50:	// space (soft)
			case 51:	// layout (soft)
			case 52:	// language (soft)
			case 53:	// lalr (soft)
			case 54:	// lexer (soft)
			case 55:	// parser (soft)
			case 0:	// <default>
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
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
