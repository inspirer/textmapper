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
package org.textmapper.tool.parser;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.textmapper.tool.parser.action.SActionLexer;
import org.textmapper.tool.parser.action.SActionParser;

public class TMLexer {

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
		int afterAt = 1;
		int afterAtID = 2;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int regexp = 1;
		int scon = 2;
		int icon = 3;
		int _skip = 4;
		int _skip_comment = 5;
		int _skip_multiline = 6;
		int Rem = 7;
		int ColonColonAssign = 8;
		int ColonColon = 9;
		int Or = 10;
		int OrOr = 11;
		int Assign = 12;
		int AssignAssign = 13;
		int ExclAssign = 14;
		int AssignGt = 15;
		int Semicolon = 16;
		int Dot = 17;
		int Comma = 18;
		int Colon = 19;
		int Lbrack = 20;
		int Rbrack = 21;
		int Lparen = 22;
		int LparenQuestAssign = 23;
		int Rparen = 24;
		int LbraceTilde = 25;
		int Rbrace = 26;
		int Lt = 27;
		int Gt = 28;
		int Mult = 29;
		int Plus = 30;
		int PlusAssign = 31;
		int Quest = 32;
		int Excl = 33;
		int Tilde = 34;
		int And = 35;
		int AndAnd = 36;
		int Dollar = 37;
		int Atsign = 38;
		int error = 39;
		int ID = 40;
		int Ltrue = 41;
		int Lfalse = 42;
		int Lnew = 43;
		int Lseparator = 44;
		int Las = 45;
		int Limport = 46;
		int Lset = 47;
		int Lbrackets = 48;
		int Linline = 49;
		int Lprec = 50;
		int Lshift = 51;
		int Lreturns = 52;
		int Linput = 53;
		int Lleft = 54;
		int Lright = 55;
		int Lnonassoc = 56;
		int Lgenerate = 57;
		int Lassert = 58;
		int Lempty = 59;
		int Lnonempty = 60;
		int Lglobal = 61;
		int Lexplicit = 62;
		int Llookahead = 63;
		int Lparam = 64;
		int Lflag = 65;
		int Lnoeoi = 66;
		int Lsoft = 67;
		int Lclass = 68;
		int Linterface = 69;
		int Lvoid = 70;
		int Lspace = 71;
		int Llayout = 72;
		int Llanguage = 73;
		int Llalr = 74;
		int Llexer = 75;
		int Lparser = 76;
		int Lreduce = 77;
		int code = 78;
		int Lbrace = 79;
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
		SActionLexer.ErrorReporter innerreporter = (String message, int line, int offset) ->
				reporter.error(message, line, offset, offset + 1);
		SActionLexer l = new SActionLexer(innerreporter) {
			@Override
			protected int nextChar() throws IOException {
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
			reporter.error("syntax error in action", getLine(), getOffset(), getOffset() + 1);
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

	public TMLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 35, 4, 1, 1, 12, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		35, 17, 9, 13, 31, 11, 30, 2, 22, 24, 8, 29, 21, 10, 20, 5,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 14, 19, 28, 15, 18, 23,
		32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 6, 3, 7, 1, 33,
		1, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 25, 16, 27, 26
	};

	private static final short tmStateMap[] = {
		0, 0, 62
	};

	private static final short tmBacktracking[] = {
		0, 3, 23, 18
	};

	private static final int tmFirstRule = -3;

	private static final int[] tmRuleSymbol = unpack_int(81,
		"\uffff\uffff\0\0\50\0\1\0\2\0\3\0\0\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15" +
		"\0\16\0\17\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35" +
		"\0\36\0\37\0\40\0\41\0\42\0\43\0\44\0\45\0\46\0\51\0\52\0\53\0\54\0\55\0\56\0\57" +
		"\0\60\0\61\0\62\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77" +
		"\0\100\0\101\0\102\0\103\0\104\0\105\0\106\0\107\0\110\0\111\0\112\0\113\0\114\0" +
		"\115\0\116\0\117\0");

	private static final int tmClassesCount = 36;

	private static final short[] tmGoto = unpack_vc_short(2304,
		"\1\ufffc\1\ufffd\1\73\1\ufffd\1\72\1\56\1\55\1\54\1\53\1\50\1\47\1\44\1\72\1\42\1" +
		"\37\1\34\1\32\1\30\1\27\1\26\1\25\1\24\1\21\1\20\1\17\1\15\1\14\1\13\1\12\1\10\1" +
		"\6\1\5\1\4\1\2\1\1\1\72\42\ufff8\1\1\1\ufff8\12\ufffb\1\uffff\26\ufffb\2\2\1\ufffb" +
		"\12\ufffd\1\3\26\ufffd\2\2\1\ufffd\44\uffd4\44\uffd5\36\uffd7\1\7\5\uffd7\44\uffd6" +
		"\17\uffdc\1\11\24\uffdc\44\uffdb\44\uffdf\44\uffe0\44\uffd8\32\uffae\1\16\11\uffae" +
		"\44\uffe1\44\uffe2\44\uffda\27\uffe4\1\ufffe\14\uffe4\17\ufffd\1\23\24\ufffd\44\uffe3" +
		"\44\uffe8\44\uffe9\44\uffea\44\uffde\17\uffd9\1\31\24\uffd9\44\uffec\20\ufff0\1\33" +
		"\23\ufff0\44\uffef\17\uffee\1\36\2\uffee\1\35\21\uffee\44\uffeb\44\uffed\16\uffe7" +
		"\1\40\25\uffe7\17\ufff1\1\41\24\ufff1\44\ufff2\1\ufff5\3\42\1\43\37\42\44\ufff5\13" +
		"\ufff3\1\45\30\ufff3\1\ufff7\3\45\1\46\37\45\44\ufff7\42\ufffd\1\1\2\ufffd\2\50\1" +
		"\52\1\ufffd\4\50\1\51\32\50\44\ufff9\1\ufffd\3\50\1\ufffd\37\50\44\uffdd\44\uffe5" +
		"\44\uffe6\1\ufffd\2\65\1\64\2\ufffd\1\62\1\65\1\57\3\65\1\ufffd\27\65\1\ufffd\7\57" +
		"\1\60\33\57\1\ufffd\4\57\1\61\2\57\1\60\33\57\44\ufff4\1\ufffd\2\62\1\63\1\ufffd" +
		"\2\62\1\65\4\62\1\ufffd\27\62\1\ufffd\3\62\1\ufffd\37\62\1\ufffd\3\65\1\ufffd\37" +
		"\65\1\ufffd\2\65\1\71\1\ufffd\1\70\1\66\5\65\1\ufffd\27\65\1\ufffd\2\66\1\67\1\ufffd" +
		"\2\66\1\65\4\66\1\ufffd\27\66\1\ufffd\3\66\1\ufffd\37\66\44\ufffa\1\ufffd\3\65\1" +
		"\ufffd\37\65\4\ufff6\1\72\7\ufff6\1\72\26\ufff6\1\72\1\ufffd\1\73\1\75\1\74\1\ufffd" +
		"\37\73\1\ufffd\3\73\1\ufffd\37\73\44\ufffb\2\ufffd\1\73\1\ufffd\1\72\1\56\1\55\1" +
		"\54\1\53\1\50\1\47\1\44\1\72\1\42\1\37\1\34\1\32\1\30\1\27\1\26\1\25\1\24\1\21\1" +
		"\20\1\17\1\77\1\14\1\13\1\12\1\10\1\6\1\5\1\4\1\2\1\1\1\72\32\uffad\1\16\11\uffad");

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
			case 2:
				return createIDToken(token, ruleIndex);
			case 3: // regexp: /\/{reFirst}{reChar}*\//
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				{ token.value = tokenText().substring(1, tokenSize()-1); }
				break;
			case 4: // scon: /"([^\n\\"]|\\.)*"/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				{ token.value = unescape(tokenText(), 1, tokenSize()-1); }
				break;
			case 5: // icon: /\-?[0-9]+/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				{ token.value = Integer.parseInt(tokenText()); }
				break;
			case 6: // eoi: /%%.*(\r?\n)?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				{ templatesStart = token.endoffset; }
				break;
			case 7: // _skip: /[\n\r\t ]+/
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
			case 8: // _skip_comment: /#.*(\r?\n)?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				{ spaceToken = skipComments; }
				break;
			case 9: // _skip_multiline: /\/\*{commentChars}\*\//
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
			case 10: // '%': /%/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 11: // '::=': /::=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 12: // '::': /::/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 13: // '|': /\|/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 14: // '||': /\|\|/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 15: // '=': /=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 16: // '==': /==/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 17: // '!=': /!=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 18: // '=>': /=>/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 19: // ';': /;/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 20: // '.': /\./
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 21: // ',': /,/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 22: // ':': /:/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 23: // '[': /\[/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 24: // ']': /\]/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 25: // '(': /\(/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 26: // '(?=': /\(\?=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 27: // ')': /\)/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 28: // '{~': /\{~/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 29: // '}': /\}/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 30: // '<': /</
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 31: // '>': />/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 32: // '*': /\*/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 33: // '+': /\+/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 34: // '+=': /\+=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 35: // '?': /\?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 36: // '!': /!/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 37: // '~': /~/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 38: // '&': /&/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 39: // '&&': /&&/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 40: // '$': /$/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 41: // '@': /@/
				state = States.afterAt;
				break;
			case 79: // code: /\{/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
				}
				{ skipAction(); token.endoffset = getOffset(); }
				break;
			case 80: // '{': /\{/
				state = States.initial;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfID = new HashMap<>();
	static {
		subTokensOfID.put("true", 42);
		subTokensOfID.put("false", 43);
		subTokensOfID.put("new", 44);
		subTokensOfID.put("separator", 45);
		subTokensOfID.put("as", 46);
		subTokensOfID.put("import", 47);
		subTokensOfID.put("set", 48);
		subTokensOfID.put("brackets", 49);
		subTokensOfID.put("inline", 50);
		subTokensOfID.put("prec", 51);
		subTokensOfID.put("shift", 52);
		subTokensOfID.put("returns", 53);
		subTokensOfID.put("input", 54);
		subTokensOfID.put("left", 55);
		subTokensOfID.put("right", 56);
		subTokensOfID.put("nonassoc", 57);
		subTokensOfID.put("generate", 58);
		subTokensOfID.put("assert", 59);
		subTokensOfID.put("empty", 60);
		subTokensOfID.put("nonempty", 61);
		subTokensOfID.put("global", 62);
		subTokensOfID.put("explicit", 63);
		subTokensOfID.put("lookahead", 64);
		subTokensOfID.put("param", 65);
		subTokensOfID.put("flag", 66);
		subTokensOfID.put("no-eoi", 67);
		subTokensOfID.put("soft", 68);
		subTokensOfID.put("class", 69);
		subTokensOfID.put("interface", 70);
		subTokensOfID.put("void", 71);
		subTokensOfID.put("space", 72);
		subTokensOfID.put("layout", 73);
		subTokensOfID.put("language", 74);
		subTokensOfID.put("lalr", 75);
		subTokensOfID.put("lexer", 76);
		subTokensOfID.put("parser", 77);
		subTokensOfID.put("reduce", 78);
	}

	protected boolean createIDToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfID.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 42:	// true
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 43:	// false
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 44:	// new
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 45:	// separator
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 46:	// as
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 47:	// import
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 48:	// set
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 78:	// reduce
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 49:	// brackets (soft)
			case 50:	// inline (soft)
			case 51:	// prec (soft)
			case 52:	// shift (soft)
			case 53:	// returns (soft)
			case 54:	// input (soft)
			case 55:	// left (soft)
			case 56:	// right (soft)
			case 57:	// nonassoc (soft)
			case 58:	// generate (soft)
			case 59:	// assert (soft)
			case 60:	// empty (soft)
			case 61:	// nonempty (soft)
			case 62:	// global (soft)
			case 63:	// explicit (soft)
			case 64:	// lookahead (soft)
			case 65:	// param (soft)
			case 66:	// flag (soft)
			case 67:	// no-eoi (soft)
			case 68:	// soft (soft)
			case 69:	// class (soft)
			case 70:	// interface (soft)
			case 71:	// void (soft)
			case 72:	// space (soft)
			case 73:	// layout (soft)
			case 74:	// language (soft)
			case 75:	// lalr (soft)
			case 76:	// lexer (soft)
			case 77:	// parser (soft)
			case 2:	// <default>
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
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
