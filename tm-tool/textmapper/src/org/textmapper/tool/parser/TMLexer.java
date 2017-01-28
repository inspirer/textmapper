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
		int afterID = 1;
		int afterColonOrEq = 2;
		int afterGT = 3;
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
		int ColonColon = 7;
		int Or = 8;
		int OrOr = 9;
		int Assign = 10;
		int AssignAssign = 11;
		int ExclAssign = 12;
		int Semicolon = 13;
		int Dot = 14;
		int Comma = 15;
		int Colon = 16;
		int Lbrack = 17;
		int Rbrack = 18;
		int Lparen = 19;
		int LparenQuestAssign = 20;
		int MinusGt = 21;
		int Rparen = 22;
		int Rbrace = 23;
		int Lt = 24;
		int Gt = 25;
		int Mult = 26;
		int Plus = 27;
		int PlusAssign = 28;
		int Quest = 29;
		int Excl = 30;
		int Tilde = 31;
		int And = 32;
		int AndAnd = 33;
		int Dollar = 34;
		int Atsign = 35;
		int error = 36;
		int ID = 37;
		int Ltrue = 38;
		int Lfalse = 39;
		int Lnew = 40;
		int Lseparator = 41;
		int Las = 42;
		int Limport = 43;
		int Lset = 44;
		int Limplements = 45;
		int Lbrackets = 46;
		int Ls = 47;
		int Lx = 48;
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
		35, 15, 5, 9, 30, 7, 29, 2, 21, 24, 11, 27, 18, 6, 17, 10,
		34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 12, 16, 26, 14, 23, 22,
		31, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 19, 3, 20, 1, 33,
		1, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 32, 13, 25, 28
	};

	private static final short tmStateMap[] = {
		0, 0, 52, 62
	};

	private static final short tmBacktracking[] = {
		2, 3, 22, 18, 81, 35
	};

	private static final int tmFirstRule = -4;

	private static final int[] tmRuleSymbol = unpack_int(82,
		"\uffff\uffff\0\0\45\0\1\0\2\0\0\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15" +
		"\0\16\0\17\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35" +
		"\0\36\0\37\0\40\0\41\0\42\0\43\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57" +
		"\0\60\0\61\0\62\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77" +
		"\0\100\0\101\0\102\0\103\0\104\0\105\0\106\0\107\0\110\0\111\0\112\0\113\0\114\0" +
		"\115\0\116\0\117\0\120\0");

	private static final int tmClassesCount = 36;

	private static final short[] tmGoto = unpack_vc_short(2304,
		"\1\ufffb\1\ufffc\1\61\1\ufffc\1\60\1\55\1\53\1\50\1\60\1\46\1\42\1\41\1\37\1\35\1" +
		"\33\1\31\1\30\1\27\1\26\1\25\1\24\1\21\1\20\1\17\1\16\1\15\1\14\1\12\1\11\1\7\1\6" +
		"\1\5\1\4\1\2\1\1\1\60\42\ufff8\1\1\1\ufff8\6\ufffa\1\uffff\32\ufffa\2\2\1\ufffa\6" +
		"\ufffc\1\3\32\ufffc\2\2\1\ufffc\44\uffae\44\uffd6\44\uffd7\35\uffd9\1\10\6\uffd9" +
		"\44\uffd8\44\uffda\16\uffde\1\13\25\uffde\44\uffdd\44\uffe1\44\uffe2\44\uffe3\44" +
		"\uffe0\44\uffdc\26\uffe6\1\ufffe\15\uffe6\16\ufffc\1\23\25\ufffc\44\uffe5\44\uffe7" +
		"\44\uffe8\44\uffea\44\uffeb\44\uffec\16\uffdb\1\32\25\uffdb\44\uffed\16\uffef\1\34" +
		"\25\uffef\44\uffee\15\ufff1\1\36\26\ufff1\44\ufff0\14\uffe9\1\40\27\uffe9\44\ufff2" +
		"\44\uffdf\13\uffab\1\ufffd\30\uffab\1\ufffc\12\43\1\44\30\43\1\ufffc\11\43\1\45\1" +
		"\44\30\43\44\ufff4\1\ufff5\3\46\1\47\37\46\44\ufff5\7\ufff3\1\51\34\ufff3\1\ufff7" +
		"\3\51\1\52\37\51\44\ufff7\27\ufffc\1\54\12\ufffc\1\1\1\ufffc\44\uffe4\1\ufffc\2\55" +
		"\1\57\1\ufffc\1\56\36\55\44\ufff9\1\ufffc\3\55\1\ufffc\37\55\4\ufff6\1\60\3\ufff6" +
		"\1\60\32\ufff6\1\60\1\ufffc\1\61\1\63\1\62\1\ufffc\37\61\1\ufffc\3\61\1\ufffc\37" +
		"\61\44\ufffa\2\ufffc\1\61\1\ufffc\1\60\1\55\1\53\1\50\1\60\1\46\1\65\1\41\1\37\1" +
		"\35\1\33\1\31\1\30\1\27\1\26\1\25\1\24\1\21\1\20\1\17\1\16\1\15\1\14\1\12\1\11\1" +
		"\7\1\6\1\5\1\4\1\2\1\1\1\60\1\ufffc\2\71\1\70\1\ufffc\3\71\1\ufffc\1\71\1\ufffc\1" +
		"\43\7\71\1\66\20\71\1\ufffc\2\66\1\67\1\ufffc\3\66\1\ufffc\13\66\1\71\17\66\1\ufffc" +
		"\3\66\1\ufffc\37\66\1\ufffc\3\71\1\ufffc\37\71\1\ufffc\2\71\1\75\1\ufffc\3\71\1\ufffc" +
		"\1\71\1\74\10\71\1\72\20\71\1\ufffc\2\72\1\73\1\ufffc\3\72\1\ufffc\13\72\1\71\17" +
		"\72\1\ufffc\3\72\1\ufffc\37\72\44\uffac\1\ufffc\3\71\1\ufffc\37\71\2\ufffc\1\61\1" +
		"\ufffc\1\60\1\55\1\53\1\50\1\60\1\46\1\42\1\41\1\37\1\35\1\33\1\31\1\30\1\27\1\26" +
		"\1\25\1\24\1\21\1\20\1\17\1\16\1\15\1\14\1\12\1\11\1\7\1\6\1\5\1\77\1\2\1\1\1\60" +
		"\44\uffad");

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
		switch (token.symbol) {
		case Tokens.Lt:
			inStatesSelector = this.state == States.initial || this.state == States.afterColonOrEq;
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
		case Tokens.ID:
		case Tokens.Lleft:
		case Tokens.Lright:
		case Tokens.Lnonassoc:
		case Tokens.Lgenerate:
		case Tokens.Lassert:
		case Tokens.Lempty:
		case Tokens.Lbrackets:
		case Tokens.Linline:
		case Tokens.Lprec:
		case Tokens.Lshift:
		case Tokens.Lreturns:
		case Tokens.Linput:
		case Tokens.Lnonempty:
		case Tokens.Lglobal:
		case Tokens.Lexplicit:
		case Tokens.Llookahead:
		case Tokens.Lparam:
		case Tokens.Lflag:
		case Tokens.Lnoeoi:
		case Tokens.Ls:
		case Tokens.Lx:
		case Tokens.Lsoft:
		case Tokens.Lclass:
		case Tokens.Linterface:
		case Tokens.Lvoid:
		case Tokens.Lspace:
		case Tokens.Llayout:
		case Tokens.Llanguage:
		case Tokens.Llalr:
		case Tokens.Llexer:
		case Tokens.Lparser:
		  this.state = States.afterID;
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
		subTokensOfID.put("true", 39);
		subTokensOfID.put("false", 40);
		subTokensOfID.put("new", 41);
		subTokensOfID.put("separator", 42);
		subTokensOfID.put("as", 43);
		subTokensOfID.put("import", 44);
		subTokensOfID.put("set", 45);
		subTokensOfID.put("implements", 46);
		subTokensOfID.put("brackets", 47);
		subTokensOfID.put("s", 48);
		subTokensOfID.put("x", 49);
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
			case 47:	// brackets (soft)
			case 48:	// s (soft)
			case 49:	// x (soft)
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
