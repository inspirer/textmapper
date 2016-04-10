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
		int Percent = 7;
		int ColonColonEqual = 8;
		int ColonColon = 9;
		int Or = 10;
		int OrOr = 11;
		int Equal = 12;
		int EqualEqual = 13;
		int ExclamationEqual = 14;
		int EqualGreater = 15;
		int Semicolon = 16;
		int Dot = 17;
		int Comma = 18;
		int Colon = 19;
		int Lsquare = 20;
		int Rsquare = 21;
		int Lparen = 22;
		int Rparen = 23;
		int LcurlyTilde = 24;
		int Rcurly = 25;
		int Less = 26;
		int Greater = 27;
		int Mult = 28;
		int Plus = 29;
		int PlusEqual = 30;
		int Questionmark = 31;
		int Exclamation = 32;
		int Tilde = 33;
		int Ampersand = 34;
		int AmpersandAmpersand = 35;
		int Dollar = 36;
		int Atsign = 37;
		int error = 38;
		int ID = 39;
		int Ltrue = 40;
		int Lfalse = 41;
		int Lnew = 42;
		int Lseparator = 43;
		int Las = 44;
		int Limport = 45;
		int Lset = 46;
		int Lbrackets = 47;
		int Linline = 48;
		int Lprec = 49;
		int Lshift = 50;
		int Lreturns = 51;
		int Linput = 52;
		int Lleft = 53;
		int Lright = 54;
		int Lnonassoc = 55;
		int Lgenerate = 56;
		int Lassert = 57;
		int Lempty = 58;
		int Lnonempty = 59;
		int Lglobal = 60;
		int Lexplicit = 61;
		int Llookahead = 62;
		int Lparam = 63;
		int Lflag = 64;
		int Lnoeoi = 65;
		int Lsoft = 66;
		int Lclass = 67;
		int Linterface = 68;
		int Lvoid = 69;
		int Lspace = 70;
		int Llayout = 71;
		int Llanguage = 72;
		int Llalr = 73;
		int Llexer = 74;
		int Lparser = 75;
		int Lreduce = 76;
		int code = 77;
		int Lcurly = 78;
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
		35, 17, 9, 13, 31, 11, 30, 2, 22, 23, 8, 28, 21, 10, 20, 5,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 14, 19, 27, 15, 18, 29,
		32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 6, 3, 7, 1, 33,
		1, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 24, 16, 26, 25, 1
	};

	private static final short tmStateMap[] = {
		0, 0, 1
	};

	private static final int[] tmRuleSymbol = unpack_int(78,
		"\47\0\1\0\2\0\3\0\0\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\16\0\17\0\20" +
		"\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36\0\37\0\40" +
		"\0\41\0\42\0\43\0\44\0\45\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57\0\60\0\61\0\62" +
		"\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77\0\100\0\101\0\102" +
		"\0\103\0\104\0\105\0\106\0\107\0\110\0\111\0\112\0\113\0\114\0\115\0\116\0");

	private static final int tmClassesCount = 36;

	private static final short[] tmGoto = unpack_vc_short(2232,
		"\1\ufffe\1\uffff\1\2\1\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\3\1\13\1\14\1\15" +
		"\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35" +
		"\1\36\1\37\1\40\1\3\2\uffff\1\2\1\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\3\1" +
		"\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\41\1\27\1\30\1\31\1\32\1" +
		"\33\1\34\1\35\1\36\1\37\1\40\1\3\1\uffff\1\2\1\42\1\43\1\uffff\37\2\4\ufff8\1\3\7" +
		"\ufff8\1\3\26\ufff8\1\3\1\uffff\2\44\1\45\2\uffff\1\46\1\44\1\47\3\44\1\uffff\27" +
		"\44\44\uffe8\44\uffe7\44\uffe0\1\uffff\2\10\1\50\1\uffff\4\10\1\51\32\10\42\uffff" +
		"\1\40\1\uffff\13\ufff5\1\52\30\ufff5\1\ufff7\3\13\1\53\37\13\16\uffe9\1\54\25\uffe9" +
		"\17\ufff0\1\55\2\ufff0\1\56\21\ufff0\20\ufff2\1\57\23\ufff2\17\uffdc\1\60\24\uffdc" +
		"\44\uffe1\44\uffec\44\uffeb\44\uffea\44\uffe6\44\uffe5\31\uffb1\1\61\12\uffb1\44" +
		"\uffdb\44\uffe3\44\uffe2\17\uffdf\1\62\24\uffdf\44\uffdd\36\uffda\1\63\5\uffda\44" +
		"\uffd8\44\uffd7\12\ufffd\1\64\26\ufffd\2\37\1\ufffd\42\ufffa\1\40\1\ufffa\31\uffb0" +
		"\1\61\12\uffb0\44\ufffd\1\uffff\3\2\1\uffff\37\2\1\uffff\2\44\1\65\1\uffff\1\66\1" +
		"\67\5\44\1\uffff\27\44\1\uffff\3\44\1\uffff\37\44\1\uffff\2\46\1\70\1\uffff\2\46" +
		"\1\44\4\46\1\uffff\27\46\1\uffff\7\47\1\71\33\47\1\uffff\3\10\1\uffff\37\10\44\ufffb" +
		"\1\ufff9\3\52\1\72\37\52\44\ufff7\17\ufff3\1\73\24\ufff3\44\uffef\44\uffed\44\ufff1" +
		"\44\uffee\44\uffe4\44\uffde\44\uffd9\12\uffff\1\64\26\uffff\2\37\2\uffff\3\44\1\uffff" +
		"\37\44\44\ufffc\1\uffff\2\67\1\74\1\uffff\2\67\1\44\4\67\1\uffff\27\67\1\uffff\3" +
		"\46\1\uffff\37\46\1\uffff\4\47\1\75\2\47\1\71\33\47\44\ufff9\44\ufff4\1\uffff\3\67" +
		"\1\uffff\37\67\44\ufff6");

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

			token.symbol = tmRuleSymbol[-state - 3];
			token.value = null;

		} while (token.symbol == -1 || !createToken(token, -state - 3));
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
				return createIDToken(token, ruleIndex);
			case 1: // regexp: /\/{reFirst}{reChar}*\//
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
			case 2: // scon: /"([^\n\\"]|\\.)*"/
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
			case 3: // icon: /\-?[0-9]+/
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
			case 4: // eoi: /%%.*(\r?\n)?/
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
				{ spaceToken = skipComments; }
				break;
			case 7: // _skip_multiline: /\/\*{commentChars}\*\//
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
			case 8: // '%': /%/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 9: // '::=': /::=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 10: // '::': /::/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 11: // '|': /\|/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 12: // '||': /\|\|/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 13: // '=': /=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 14: // '==': /==/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 15: // '!=': /!=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 16: // '=>': /=>/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 17: // ';': /;/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 18: // '.': /\./
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 19: // ',': /,/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 20: // ':': /:/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 21: // '[': /\[/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 22: // ']': /\]/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 23: // '(': /\(/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 24: // ')': /\)/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 25: // '{~': /\{~/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 26: // '}': /\}/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 27: // '<': /</
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 28: // '>': />/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 29: // '*': /\*/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 30: // '+': /\+/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 31: // '+=': /\+=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 32: // '?': /\?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 33: // '!': /!/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 34: // '~': /~/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 35: // '&': /&/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 36: // '&&': /&&/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 37: // '$': /$/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 38: // '@': /@/
				state = States.afterAt;
				break;
			case 76: // code: /\{/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
				}
				{ skipAction(); token.endoffset = getOffset(); }
				break;
			case 77: // '{': /\{/
				state = States.initial;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfID = new HashMap<>();
	static {
		subTokensOfID.put("true", 39);
		subTokensOfID.put("false", 40);
		subTokensOfID.put("new", 41);
		subTokensOfID.put("separator", 42);
		subTokensOfID.put("as", 43);
		subTokensOfID.put("import", 44);
		subTokensOfID.put("set", 45);
		subTokensOfID.put("brackets", 46);
		subTokensOfID.put("inline", 47);
		subTokensOfID.put("prec", 48);
		subTokensOfID.put("shift", 49);
		subTokensOfID.put("returns", 50);
		subTokensOfID.put("input", 51);
		subTokensOfID.put("left", 52);
		subTokensOfID.put("right", 53);
		subTokensOfID.put("nonassoc", 54);
		subTokensOfID.put("generate", 55);
		subTokensOfID.put("assert", 56);
		subTokensOfID.put("empty", 57);
		subTokensOfID.put("nonempty", 58);
		subTokensOfID.put("global", 59);
		subTokensOfID.put("explicit", 60);
		subTokensOfID.put("lookahead", 61);
		subTokensOfID.put("param", 62);
		subTokensOfID.put("flag", 63);
		subTokensOfID.put("no-eoi", 64);
		subTokensOfID.put("soft", 65);
		subTokensOfID.put("class", 66);
		subTokensOfID.put("interface", 67);
		subTokensOfID.put("void", 68);
		subTokensOfID.put("space", 69);
		subTokensOfID.put("layout", 70);
		subTokensOfID.put("language", 71);
		subTokensOfID.put("lalr", 72);
		subTokensOfID.put("lexer", 73);
		subTokensOfID.put("parser", 74);
		subTokensOfID.put("reduce", 75);
	}

	protected boolean createIDToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfID.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 39:	// true
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 40:	// false
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 41:	// new
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 42:	// separator
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 43:	// as
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 44:	// import
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 45:	// set
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 75:	// reduce
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 46:	// brackets (soft)
			case 47:	// inline (soft)
			case 48:	// prec (soft)
			case 49:	// shift (soft)
			case 50:	// returns (soft)
			case 51:	// input (soft)
			case 52:	// left (soft)
			case 53:	// right (soft)
			case 54:	// nonassoc (soft)
			case 55:	// generate (soft)
			case 56:	// assert (soft)
			case 57:	// empty (soft)
			case 58:	// nonempty (soft)
			case 59:	// global (soft)
			case 60:	// explicit (soft)
			case 61:	// lookahead (soft)
			case 62:	// param (soft)
			case 63:	// flag (soft)
			case 64:	// no-eoi (soft)
			case 65:	// soft (soft)
			case 66:	// class (soft)
			case 67:	// interface (soft)
			case 68:	// void (soft)
			case 69:	// space (soft)
			case 70:	// layout (soft)
			case 71:	// language (soft)
			case 72:	// lalr (soft)
			case 73:	// lexer (soft)
			case 74:	// parser (soft)
			case 0:	// <default>
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
