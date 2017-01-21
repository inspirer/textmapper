/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
		int afterColonOrEq = 1;
		int afterGT = 2;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int scon = 1;
		int icon = 2;
		int _skip = 3;
		int _skip_comment = 4;
		int _skip_multiline = 5;
		int Rem = 6;
		int ColonColonAssign = 7;
		int ColonColon = 8;
		int Or = 9;
		int OrOr = 10;
		int Assign = 11;
		int AssignAssign = 12;
		int ExclAssign = 13;
		int Semicolon = 14;
		int Dot = 15;
		int Comma = 16;
		int Colon = 17;
		int Lbrack = 18;
		int Rbrack = 19;
		int Lparen = 20;
		int LparenQuestAssign = 21;
		int MinusGt = 22;
		int Rparen = 23;
		int LbraceTilde = 24;
		int Rbrace = 25;
		int Lt = 26;
		int Gt = 27;
		int Mult = 28;
		int Plus = 29;
		int PlusAssign = 30;
		int Quest = 31;
		int Excl = 32;
		int Tilde = 33;
		int And = 34;
		int AndAnd = 35;
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
		int Ls = 48;
		int Lx = 49;
		int Linline = 50;
		int Lprec = 51;
		int Lshift = 52;
		int Lreturns = 53;
		int Linput = 54;
		int Lleft = 55;
		int Lright = 56;
		int Lnonassoc = 57;
		int Lgenerate = 58;
		int Lassert = 59;
		int Lempty = 60;
		int Lnonempty = 61;
		int Lglobal = 62;
		int Lexplicit = 63;
		int Llookahead = 64;
		int Lparam = 65;
		int Lflag = 66;
		int Lnoeoi = 67;
		int Lsoft = 68;
		int Lclass = 69;
		int Linterface = 70;
		int Lvoid = 71;
		int Lspace = 72;
		int Llayout = 73;
		int Llanguage = 74;
		int Llalr = 75;
		int Llexer = 76;
		int Lparser = 77;
		int code = 78;
		int Lbrace = 79;
		int regexp = 80;
		int Div = 81;
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

	protected boolean inStatesSelector = false;
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
		inStatesSelector = false;
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 35, 4, 1, 1, 8, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		35, 15, 5, 9, 31, 7, 30, 2, 21, 24, 11, 29, 18, 6, 17, 10,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 12, 16, 28, 13, 23, 22,
		32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 19, 3, 20, 1, 33,
		1, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 25, 14, 27, 26
	};

	private static final short tmStateMap[] = {
		0, 54, 64
	};

	private static final short tmBacktracking[] = {
		2, 3, 23, 19, 82, 37
	};

	private static final int tmFirstRule = -4;

	private static final int[] tmRuleSymbol = unpack_int(83,
		"\uffff\uffff\0\0\47\0\1\0\2\0\0\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15" +
		"\0\16\0\17\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35" +
		"\0\36\0\37\0\40\0\41\0\42\0\43\0\44\0\45\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57" +
		"\0\60\0\61\0\62\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77" +
		"\0\100\0\101\0\102\0\103\0\104\0\105\0\106\0\107\0\110\0\111\0\112\0\113\0\114\0" +
		"\115\0\116\0\117\0\120\0\121\0");

	private static final int tmClassesCount = 36;

	private static final short[] tmGoto = unpack_vc_short(2376,
		"\1\ufffb\1\ufffc\1\63\1\ufffc\1\62\1\57\1\55\1\52\1\62\1\50\1\44\1\43\1\40\1\36\1" +
		"\34\1\32\1\31\1\30\1\27\1\26\1\25\1\22\1\21\1\20\1\17\1\15\1\14\1\13\1\12\1\10\1" +
		"\6\1\5\1\4\1\2\1\1\1\62\42\ufff8\1\1\1\ufff8\6\ufffa\1\uffff\32\ufffa\2\2\1\ufffa" +
		"\6\ufffc\1\3\32\ufffc\2\2\1\ufffc\44\uffd4\44\uffd5\36\uffd7\1\7\5\uffd7\44\uffd6" +
		"\15\uffdc\1\11\26\uffdc\44\uffdb\44\uffdf\44\uffe0\44\uffd8\32\uffad\1\16\11\uffad" +
		"\44\uffe1\44\uffe2\44\uffde\44\uffda\26\uffe5\1\ufffe\15\uffe5\15\ufffc\1\24\26\ufffc" +
		"\44\uffe4\44\uffe6\44\uffe7\44\uffe9\44\uffea\44\uffeb\15\uffd9\1\33\26\uffd9\44" +
		"\uffec\16\ufff0\1\35\25\ufff0\44\uffef\15\uffee\1\37\26\uffee\44\uffed\14\uffe8\1" +
		"\41\27\uffe8\15\ufff1\1\42\26\ufff1\44\ufff2\44\uffdd\13\uffaa\1\ufffd\30\uffaa\1" +
		"\ufffc\12\45\1\46\30\45\1\ufffc\11\45\1\47\1\46\30\45\44\ufff4\1\ufff5\3\50\1\51" +
		"\37\50\44\ufff5\7\ufff3\1\53\34\ufff3\1\ufff7\3\53\1\54\37\53\44\ufff7\27\ufffc\1" +
		"\56\12\ufffc\1\1\1\ufffc\44\uffe3\1\ufffc\2\57\1\61\1\ufffc\1\60\36\57\44\ufff9\1" +
		"\ufffc\3\57\1\ufffc\37\57\4\ufff6\1\62\3\ufff6\1\62\32\ufff6\1\62\1\ufffc\1\63\1" +
		"\65\1\64\1\ufffc\37\63\1\ufffc\3\63\1\ufffc\37\63\44\ufffa\2\ufffc\1\63\1\ufffc\1" +
		"\62\1\57\1\55\1\52\1\62\1\50\1\67\1\43\1\40\1\36\1\34\1\32\1\31\1\30\1\27\1\26\1" +
		"\25\1\22\1\21\1\20\1\17\1\15\1\14\1\13\1\12\1\10\1\6\1\5\1\4\1\2\1\1\1\62\1\ufffc" +
		"\2\73\1\72\1\ufffc\3\73\1\ufffc\1\73\1\ufffc\1\45\7\73\1\70\20\73\1\ufffc\2\70\1" +
		"\71\1\ufffc\3\70\1\ufffc\13\70\1\73\17\70\1\ufffc\3\70\1\ufffc\37\70\1\ufffc\3\73" +
		"\1\ufffc\37\73\1\ufffc\2\73\1\77\1\ufffc\3\73\1\ufffc\1\73\1\76\10\73\1\74\20\73" +
		"\1\ufffc\2\74\1\75\1\ufffc\3\74\1\ufffc\13\74\1\73\17\74\1\ufffc\3\74\1\ufffc\37" +
		"\74\44\uffab\1\ufffc\3\73\1\ufffc\37\73\2\ufffc\1\63\1\ufffc\1\62\1\57\1\55\1\52" +
		"\1\62\1\50\1\44\1\43\1\40\1\36\1\34\1\32\1\31\1\30\1\27\1\26\1\25\1\22\1\21\1\20" +
		"\1\17\1\101\1\14\1\13\1\12\1\10\1\6\1\5\1\4\1\2\1\1\1\62\32\uffac\1\16\11\uffac");

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
		int lastTokenLine = tokenLine;
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
		switch (token.symbol) {
		case Tokens.Lt:
			inStatesSelector = (lastTokenLine != tokenLine) || this.state == States.afterColonOrEq;
			this.state = States.initial;
			break;
		case Tokens.Gt:
			this.state = inStatesSelector ? States.afterGT : States.initial;
			inStatesSelector = false;
			break;
		case Tokens.Assign:
		case Tokens.Colon:
			this.state = States.afterColonOrEq;
			break;
		case Tokens._skip:
		case Tokens._skip_comment:
		case Tokens._skip_multiline:
			break;
		default:
			this.state = States.initial;
		}
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
			case 3: // scon: /"([^\n\\"]|\\.)*"/
				{ token.value = unescape(tokenText(), 1, tokenSize()-1); }
				break;
			case 4: // icon: /\-?[0-9]+/
				{ token.value = Integer.parseInt(tokenText()); }
				break;
			case 5: // eoi: /%%.*(\r?\n)?/
				{ templatesStart = token.endoffset; }
				break;
			case 6: // _skip: /[\n\r\t ]+/
				spaceToken = true;
				break;
			case 7: // _skip_comment: /#.*(\r?\n)?/
				{ spaceToken = skipComments; }
				break;
			case 8: // _skip_multiline: /\/\*{commentChars}\*\//
				spaceToken = true;
				break;
			case 79: // code: /\{/
				{ skipAction(); token.endoffset = getOffset(); }
				break;
			case 81: // regexp: /\/{reFirst}{reChar}*\//
				{ token.value = tokenText().substring(1, tokenSize()-1); }
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfID = new HashMap<>();
	static {
		subTokensOfID.put("true", 41);
		subTokensOfID.put("false", 42);
		subTokensOfID.put("new", 43);
		subTokensOfID.put("separator", 44);
		subTokensOfID.put("as", 45);
		subTokensOfID.put("import", 46);
		subTokensOfID.put("set", 47);
		subTokensOfID.put("brackets", 48);
		subTokensOfID.put("s", 49);
		subTokensOfID.put("x", 50);
		subTokensOfID.put("inline", 51);
		subTokensOfID.put("prec", 52);
		subTokensOfID.put("shift", 53);
		subTokensOfID.put("returns", 54);
		subTokensOfID.put("input", 55);
		subTokensOfID.put("left", 56);
		subTokensOfID.put("right", 57);
		subTokensOfID.put("nonassoc", 58);
		subTokensOfID.put("generate", 59);
		subTokensOfID.put("assert", 60);
		subTokensOfID.put("empty", 61);
		subTokensOfID.put("nonempty", 62);
		subTokensOfID.put("global", 63);
		subTokensOfID.put("explicit", 64);
		subTokensOfID.put("lookahead", 65);
		subTokensOfID.put("param", 66);
		subTokensOfID.put("flag", 67);
		subTokensOfID.put("no-eoi", 68);
		subTokensOfID.put("soft", 69);
		subTokensOfID.put("class", 70);
		subTokensOfID.put("interface", 71);
		subTokensOfID.put("void", 72);
		subTokensOfID.put("space", 73);
		subTokensOfID.put("layout", 74);
		subTokensOfID.put("language", 75);
		subTokensOfID.put("lalr", 76);
		subTokensOfID.put("lexer", 77);
		subTokensOfID.put("parser", 78);
	}

	protected boolean createIDToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfID.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
			case 48:	// brackets (soft)
			case 51:	// inline (soft)
			case 52:	// prec (soft)
			case 53:	// shift (soft)
			case 54:	// returns (soft)
			case 55:	// input (soft)
			case 56:	// left (soft)
			case 57:	// right (soft)
			case 58:	// nonassoc (soft)
			case 59:	// generate (soft)
			case 60:	// assert (soft)
			case 61:	// empty (soft)
			case 62:	// nonempty (soft)
			case 63:	// global (soft)
			case 64:	// explicit (soft)
			case 65:	// lookahead (soft)
			case 66:	// param (soft)
			case 67:	// flag (soft)
			case 68:	// no-eoi (soft)
			case 69:	// soft (soft)
			case 70:	// class (soft)
			case 71:	// interface (soft)
			case 72:	// void (soft)
			case 73:	// space (soft)
			case 74:	// layout (soft)
			case 75:	// language (soft)
			case 76:	// lalr (soft)
			case 77:	// lexer (soft)
			case 78:	// parser (soft)
			case 2:	// <default>
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
