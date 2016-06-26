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

	private static final int[] tmRuleSymbol = unpack_int(79,
		"\50\0\1\0\2\0\3\0\0\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\16\0\17\0\20" +
		"\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36\0\37\0\40" +
		"\0\41\0\42\0\43\0\44\0\45\0\46\0\51\0\52\0\53\0\54\0\55\0\56\0\57\0\60\0\61\0\62" +
		"\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77\0\100\0\101\0\102" +
		"\0\103\0\104\0\105\0\106\0\107\0\110\0\111\0\112\0\113\0\114\0\115\0\116\0\117\0");

	private static final int tmClassesCount = 36;

	private static final short[] tmGoto = unpack_vc_short(2304,
		"\1\ufffe\1\uffff\1\73\1\uffff\1\72\1\56\1\55\1\54\1\53\1\50\1\47\1\44\1\72\1\42\1" +
		"\37\1\34\1\32\1\30\1\27\1\26\1\25\1\24\1\21\1\20\1\17\1\15\1\14\1\13\1\12\1\10\1" +
		"\6\1\5\1\4\1\2\1\1\1\72\42\ufffa\1\1\1\ufffa\12\ufffd\1\3\26\ufffd\2\2\1\ufffd\12" +
		"\uffff\1\3\26\uffff\2\2\1\uffff\44\uffd6\44\uffd7\36\uffd9\1\7\5\uffd9\44\uffd8\17" +
		"\uffde\1\11\24\uffde\44\uffdd\44\uffe1\44\uffe2\44\uffda\32\uffb0\1\16\11\uffb0\44" +
		"\uffe3\44\uffe4\44\uffdc\27\uffe6\1\22\14\uffe6\17\uffff\1\23\24\uffff\44\uffe5\44" +
		"\uffea\44\uffeb\44\uffec\44\uffe0\17\uffdb\1\31\24\uffdb\44\uffee\20\ufff2\1\33\23" +
		"\ufff2\44\ufff1\17\ufff0\1\36\2\ufff0\1\35\21\ufff0\44\uffed\44\uffef\16\uffe9\1" +
		"\40\25\uffe9\17\ufff3\1\41\24\ufff3\44\ufff4\1\ufff7\3\42\1\43\37\42\44\ufff7\13" +
		"\ufff5\1\45\30\ufff5\1\ufff9\3\45\1\46\37\45\44\ufff9\42\uffff\1\1\2\uffff\2\50\1" +
		"\52\1\uffff\4\50\1\51\32\50\44\ufffb\1\uffff\3\50\1\uffff\37\50\44\uffdf\44\uffe7" +
		"\44\uffe8\1\uffff\2\65\1\64\2\uffff\1\62\1\65\1\57\3\65\1\uffff\27\65\1\uffff\7\57" +
		"\1\60\33\57\1\uffff\4\57\1\61\2\57\1\60\33\57\44\ufff6\1\uffff\2\62\1\63\1\uffff" +
		"\2\62\1\65\4\62\1\uffff\27\62\1\uffff\3\62\1\uffff\37\62\1\uffff\3\65\1\uffff\37" +
		"\65\1\uffff\2\65\1\71\1\uffff\1\70\1\66\5\65\1\uffff\27\65\1\uffff\2\66\1\67\1\uffff" +
		"\2\66\1\65\4\66\1\uffff\27\66\1\uffff\3\66\1\uffff\37\66\44\ufffc\1\uffff\3\65\1" +
		"\uffff\37\65\4\ufff8\1\72\7\ufff8\1\72\26\ufff8\1\72\1\uffff\1\73\1\75\1\74\1\uffff" +
		"\37\73\1\uffff\3\73\1\uffff\37\73\44\ufffd\2\uffff\1\73\1\uffff\1\72\1\56\1\55\1" +
		"\54\1\53\1\50\1\47\1\44\1\72\1\42\1\37\1\34\1\32\1\30\1\27\1\26\1\25\1\24\1\21\1" +
		"\20\1\17\1\77\1\14\1\13\1\12\1\10\1\6\1\5\1\4\1\2\1\1\1\72\32\uffaf\1\16\11\uffaf");

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
			case 24: // '(?=': /\(\?=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 25: // ')': /\)/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 26: // '{~': /\{~/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 27: // '}': /\}/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 28: // '<': /</
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 29: // '>': />/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 30: // '*': /\*/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 31: // '+': /\+/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 32: // '+=': /\+=/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 33: // '?': /\?/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 34: // '!': /!/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 35: // '~': /~/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 36: // '&': /&/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 37: // '&&': /&&/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 38: // '$': /$/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 39: // '@': /@/
				state = States.afterAt;
				break;
			case 77: // code: /\{/
				switch(state) {
					case States.afterAt:
						state = States.initial;
						break;
				}
				{ skipAction(); token.endoffset = getOffset(); }
				break;
			case 78: // '{': /\{/
				state = States.initial;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfID = new HashMap<>();
	static {
		subTokensOfID.put("true", 40);
		subTokensOfID.put("false", 41);
		subTokensOfID.put("new", 42);
		subTokensOfID.put("separator", 43);
		subTokensOfID.put("as", 44);
		subTokensOfID.put("import", 45);
		subTokensOfID.put("set", 46);
		subTokensOfID.put("brackets", 47);
		subTokensOfID.put("inline", 48);
		subTokensOfID.put("prec", 49);
		subTokensOfID.put("shift", 50);
		subTokensOfID.put("returns", 51);
		subTokensOfID.put("input", 52);
		subTokensOfID.put("left", 53);
		subTokensOfID.put("right", 54);
		subTokensOfID.put("nonassoc", 55);
		subTokensOfID.put("generate", 56);
		subTokensOfID.put("assert", 57);
		subTokensOfID.put("empty", 58);
		subTokensOfID.put("nonempty", 59);
		subTokensOfID.put("global", 60);
		subTokensOfID.put("explicit", 61);
		subTokensOfID.put("lookahead", 62);
		subTokensOfID.put("param", 63);
		subTokensOfID.put("flag", 64);
		subTokensOfID.put("no-eoi", 65);
		subTokensOfID.put("soft", 66);
		subTokensOfID.put("class", 67);
		subTokensOfID.put("interface", 68);
		subTokensOfID.put("void", 69);
		subTokensOfID.put("space", 70);
		subTokensOfID.put("layout", 71);
		subTokensOfID.put("language", 72);
		subTokensOfID.put("lalr", 73);
		subTokensOfID.put("lexer", 74);
		subTokensOfID.put("parser", 75);
		subTokensOfID.put("reduce", 76);
	}

	protected boolean createIDToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfID.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 40:	// true
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 41:	// false
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 42:	// new
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 43:	// separator
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 44:	// as
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 45:	// import
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 46:	// set
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 76:	// reduce
				switch(state) {
					case States.afterAt:
						state = States.afterAtID;
						break;
					case States.afterAtID:
						state = States.initial;
						break;
				}
				break;
			case 47:	// brackets (soft)
			case 48:	// inline (soft)
			case 49:	// prec (soft)
			case 50:	// shift (soft)
			case 51:	// returns (soft)
			case 52:	// input (soft)
			case 53:	// left (soft)
			case 54:	// right (soft)
			case 55:	// nonassoc (soft)
			case 56:	// generate (soft)
			case 57:	// assert (soft)
			case 58:	// empty (soft)
			case 59:	// nonempty (soft)
			case 60:	// global (soft)
			case 61:	// explicit (soft)
			case 62:	// lookahead (soft)
			case 63:	// param (soft)
			case 64:	// flag (soft)
			case 65:	// no-eoi (soft)
			case 66:	// soft (soft)
			case 67:	// class (soft)
			case 68:	// interface (soft)
			case 69:	// void (soft)
			case 70:	// space (soft)
			case 71:	// layout (soft)
			case 72:	// language (soft)
			case 73:	// lalr (soft)
			case 74:	// lexer (soft)
			case 75:	// parser (soft)
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
