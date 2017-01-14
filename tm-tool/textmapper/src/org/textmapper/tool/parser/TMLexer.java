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
		int AssignGt = 14;
		int Semicolon = 15;
		int Dot = 16;
		int Comma = 17;
		int Colon = 18;
		int Lbrack = 19;
		int Rbrack = 20;
		int Lparen = 21;
		int LparenQuestAssign = 22;
		int MinusGt = 23;
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
		int code = 77;
		int Lbrace = 78;
		int regexp = 79;
		int Div = 80;
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
		35, 15, 5, 9, 31, 7, 30, 2, 22, 24, 11, 29, 19, 6, 18, 10,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 12, 17, 28, 13, 16, 23,
		32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 20, 3, 21, 1, 33,
		1, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 25, 14, 27, 26
	};

	private static final short tmStateMap[] = {
		0, 55, 65
	};

	private static final short tmBacktracking[] = {
		2, 3, 24, 18, 81, 38
	};

	private static final int tmFirstRule = -4;

	private static final int[] tmRuleSymbol = unpack_int(82,
		"\uffff\uffff\0\0\50\0\1\0\2\0\0\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15" +
		"\0\16\0\17\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35" +
		"\0\36\0\37\0\40\0\41\0\42\0\43\0\44\0\45\0\46\0\51\0\52\0\53\0\54\0\55\0\56\0\57" +
		"\0\60\0\61\0\62\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77" +
		"\0\100\0\101\0\102\0\103\0\104\0\105\0\106\0\107\0\110\0\111\0\112\0\113\0\114\0" +
		"\115\0\116\0\117\0\120\0");

	private static final int tmClassesCount = 36;

	private static final short[] tmGoto = unpack_vc_short(2412,
		"\1\ufffb\1\ufffc\1\64\1\ufffc\1\63\1\60\1\56\1\53\1\63\1\51\1\45\1\44\1\41\1\36\1" +
		"\34\1\32\1\31\1\30\1\27\1\26\1\25\1\24\1\21\1\20\1\17\1\15\1\14\1\13\1\12\1\10\1" +
		"\6\1\5\1\4\1\2\1\1\1\63\42\ufff8\1\1\1\ufff8\6\ufffa\1\uffff\32\ufffa\2\2\1\ufffa" +
		"\6\ufffc\1\3\32\ufffc\2\2\1\ufffc\44\uffd3\44\uffd4\36\uffd6\1\7\5\uffd6\44\uffd5" +
		"\15\uffdb\1\11\26\uffdb\44\uffda\44\uffde\44\uffdf\44\uffd7\32\uffae\1\16\11\uffae" +
		"\44\uffe0\44\uffe1\44\uffd9\27\uffe4\1\ufffe\14\uffe4\15\ufffc\1\23\26\ufffc\44\uffe3" +
		"\44\uffe5\44\uffe6\44\uffe8\44\uffe9\44\uffea\44\uffdd\15\uffd8\1\33\26\uffd8\44" +
		"\uffec\16\ufff0\1\35\25\ufff0\44\uffef\15\uffee\1\40\2\uffee\1\37\23\uffee\44\uffeb" +
		"\44\uffed\14\uffe7\1\42\27\uffe7\15\ufff1\1\43\26\ufff1\44\ufff2\44\uffdc\13\uffab" +
		"\1\ufffd\30\uffab\1\ufffc\12\46\1\47\30\46\1\ufffc\11\46\1\50\1\47\30\46\44\ufff4" +
		"\1\ufff5\3\51\1\52\37\51\44\ufff5\7\ufff3\1\54\34\ufff3\1\ufff7\3\54\1\55\37\54\44" +
		"\ufff7\20\ufffc\1\57\21\ufffc\1\1\1\ufffc\44\uffe2\1\ufffc\2\60\1\62\1\ufffc\1\61" +
		"\36\60\44\ufff9\1\ufffc\3\60\1\ufffc\37\60\4\ufff6\1\63\3\ufff6\1\63\32\ufff6\1\63" +
		"\1\ufffc\1\64\1\66\1\65\1\ufffc\37\64\1\ufffc\3\64\1\ufffc\37\64\44\ufffa\2\ufffc" +
		"\1\64\1\ufffc\1\63\1\60\1\56\1\53\1\63\1\51\1\70\1\44\1\41\1\36\1\34\1\32\1\31\1" +
		"\30\1\27\1\26\1\25\1\24\1\21\1\20\1\17\1\15\1\14\1\13\1\12\1\10\1\6\1\5\1\4\1\2\1" +
		"\1\1\63\1\ufffc\2\74\1\73\1\ufffc\3\74\1\ufffc\1\74\1\ufffc\1\46\10\74\1\71\17\74" +
		"\1\ufffc\2\71\1\72\1\ufffc\3\71\1\ufffc\14\71\1\74\16\71\1\ufffc\3\71\1\ufffc\37" +
		"\71\1\ufffc\3\74\1\ufffc\37\74\1\ufffc\2\74\1\100\1\ufffc\3\74\1\ufffc\1\74\1\77" +
		"\11\74\1\75\17\74\1\ufffc\2\75\1\76\1\ufffc\3\75\1\ufffc\14\75\1\74\16\75\1\ufffc" +
		"\3\75\1\ufffc\37\75\44\uffac\1\ufffc\3\74\1\ufffc\37\74\2\ufffc\1\64\1\ufffc\1\63" +
		"\1\60\1\56\1\53\1\63\1\51\1\45\1\44\1\41\1\36\1\34\1\32\1\31\1\30\1\27\1\26\1\25" +
		"\1\24\1\21\1\20\1\17\1\102\1\14\1\13\1\12\1\10\1\6\1\5\1\4\1\2\1\1\1\63\32\uffad" +
		"\1\16\11\uffad");

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
			case 78: // code: /\{/
				{ skipAction(); token.endoffset = getOffset(); }
				break;
			case 80: // regexp: /\/{reFirst}{reChar}*\//
				{ token.value = tokenText().substring(1, tokenSize()-1); }
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
	}

	protected boolean createIDToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfID.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		boolean spaceToken = false;
		switch(ruleIndex) {
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
