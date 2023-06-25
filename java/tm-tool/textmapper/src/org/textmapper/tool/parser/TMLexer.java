/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
		int as = 38;
		int extend = 39;
		int _false = 40;
		int _implements = 41;
		int _import = 42;
		int separator = 43;
		int set = 44;
		int _true = 45;
		int _assert = 46;
		int brackets = 47;
		int _class = 48;
		int empty = 49;
		int expect = 50;
		int expectMinusrr = 51;
		int explicit = 52;
		int flag = 53;
		int generate = 54;
		int global = 55;
		int inline = 56;
		int input = 57;
		int _interface = 58;
		int lalr = 59;
		int language = 60;
		int layout = 61;
		int left = 62;
		int lexer = 63;
		int lookahead = 64;
		int noMinuseoi = 65;
		int nonassoc = 66;
		int nonempty = 67;
		int param = 68;
		int parser = 69;
		int prec = 70;
		int returns = 71;
		int right = 72;
		int char_s = 73;
		int shift = 74;
		int space = 75;
		int _void = 76;
		int char_x = 77;
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
	protected boolean afterColonColon = false;
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
		afterColonColon = false;
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 1, 1, 4, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
		20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 22, 23, 24, 25, 26,
		27, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 29, 30, 31, 1, 28,
		1, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 32, 33, 34, 35
	};

	private static final short tmStateMap[] = {
		0, 0, 52, 62
	};

	private static final short tmBacktracking[] = {
		38, 9, 82, 21, 21, 33
	};

	private static final int tmFirstRule = -4;

	private static final int[] tmRuleSymbol = unpack_int(83,
		"\uffff\uffff\0\0\1\0\2\0\0\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\16" +
		"\0\17\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36" +
		"\0\37\0\40\0\41\0\42\0\43\0\45\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57" +
		"\0\60\0\61\0\62\0\63\0\64\0\65\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76\0\77" +
		"\0\100\0\101\0\102\0\103\0\104\0\105\0\106\0\107\0\110\0\111\0\112\0\113\0\114\0" +
		"\115\0\116\0\117\0\120\0\121\0");

	private static final int tmClassesCount = 36;

	private static final short[] tmGoto = unpack_vc_short(2304,
		"\1\ufffb\1\ufffc\3\63\1\61\1\56\1\54\1\53\1\50\1\46\1\43\1\40\1\37\1\36\1\34\1\33" +
		"\1\31\1\30\1\24\1\23\1\21\1\20\1\17\1\15\1\14\1\13\1\12\1\10\1\7\1\ufffc\1\6\1\5" +
		"\1\3\1\2\1\1\44\uffdb\44\uffe3\41\ufff2\1\4\2\ufff2\44\ufff1\44\uffad\44\uffe8\44" +
		"\uffe9\21\uffd6\1\uffff\2\uffd6\1\10\7\uffd6\1\10\7\uffd6\21\ufffc\1\11\2\ufffc\1" +
		"\10\7\ufffc\1\10\7\ufffc\44\uffd7\44\uffdd\44\uffe1\30\ufff0\1\16\13\ufff0\44\uffef" +
		"\44\uffe2\44\uffed\25\uffea\1\22\16\uffea\44\ufff3\24\ufff9\1\23\17\ufff9\16\uffaa" +
		"\1\ufffe\25\uffaa\1\ufffc\15\25\1\26\25\25\1\ufffc\15\25\1\26\4\25\1\27\20\25\44" +
		"\ufff5\44\uffec\24\ufffc\1\23\4\ufffc\1\32\12\ufffc\44\uffe5\44\uffeb\30\uffdf\1" +
		"\35\13\uffdf\44\uffde\44\uffe0\44\uffe4\32\uffe7\1\ufffd\11\uffe7\30\ufffc\1\42\13" +
		"\ufffc\44\uffe6\1\ufffc\2\43\1\ufffc\7\43\1\45\22\43\1\44\5\43\1\ufffc\2\43\1\ufffc" +
		"\40\43\44\uffd6\12\uffda\1\47\31\uffda\44\uffd9\11\ufff4\1\51\32\ufff4\1\ufff8\2" +
		"\51\1\52\40\51\44\ufff8\44\uffd8\1\ufff6\2\54\1\55\40\54\44\ufff6\1\ufffc\2\56\1" +
		"\ufffc\2\56\1\60\27\56\1\57\5\56\1\ufffc\2\56\1\ufffc\40\56\44\ufffa\30\uffdc\1\62" +
		"\13\uffdc\44\uffee\2\ufff7\3\63\37\ufff7\2\ufffc\3\63\1\61\1\56\1\54\1\53\1\50\1" +
		"\46\1\43\1\40\1\37\1\36\1\34\1\33\1\31\1\30\1\65\1\23\1\21\1\20\1\17\1\15\1\14\1" +
		"\13\1\12\1\10\1\7\1\ufffc\1\6\1\5\1\3\1\2\1\1\1\ufffc\2\71\2\ufffc\11\71\1\25\4\71" +
		"\1\ufffc\11\71\1\67\1\66\5\71\1\ufffc\2\71\1\ufffc\40\71\1\ufffc\2\67\2\ufffc\31" +
		"\67\1\70\1\71\4\67\1\ufffc\2\67\1\ufffc\40\67\1\ufffc\2\71\2\ufffc\16\71\1\75\11" +
		"\71\1\73\1\72\5\71\1\ufffc\2\71\1\ufffc\40\71\1\ufffc\2\73\2\ufffc\31\73\1\74\1\71" +
		"\4\73\1\ufffc\2\73\1\ufffc\40\73\44\uffab\2\ufffc\3\63\1\61\1\56\1\54\1\53\1\50\1" +
		"\46\1\43\1\40\1\37\1\36\1\34\1\33\1\31\1\30\1\24\1\23\1\21\1\20\1\17\1\15\1\14\1" +
		"\13\1\12\1\10\1\7\1\ufffc\1\6\1\77\1\3\1\2\1\1\44\uffac");

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
		case Tokens.left:
		case Tokens.right:
		case Tokens.nonassoc:
		case Tokens.generate:
		case Tokens._assert:
		case Tokens.empty:
		case Tokens.brackets:
		case Tokens.inline:
		case Tokens.prec:
		case Tokens.shift:
		case Tokens.returns:
		case Tokens.input:
		case Tokens.nonempty:
		case Tokens.global:
		case Tokens.explicit:
		case Tokens.lookahead:
		case Tokens.param:
		case Tokens.flag:
		case Tokens.noMinuseoi:
		case Tokens.char_s:
		case Tokens.char_x:
		case Tokens._class:
		case Tokens._interface:
		case Tokens._void:
		case Tokens.space:
		case Tokens.layout:
		case Tokens.language:
		case Tokens.lalr:
		  this.state = States.afterID;
		  break;
		case Tokens.lexer:
		case Tokens.parser:
		  this.state = afterColonColon ? States.initial : States.afterID;
		  break;
		case Tokens._skip:
		case Tokens._skip_comment:
		case Tokens._skip_multiline:
		  // Note: these do not affect the '::' tracking.
			return token;
		default:
			this.state = States.initial;
		}
		this.afterColonColon = (token.symbol == Tokens.ColonColon);
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
			case 2: // scon: /"([^\n\\"]|\\.)*"/
				{ token.value = unescape(tokenText(), 1, tokenSize()-1); }
				break;
			case 3: // icon: /\-?[0-9]+/
				{ token.value = Integer.parseInt(tokenText()); }
				break;
			case 4: // eoi: /%%.*(\r?\n)?/
				{ templatesStart = token.endoffset; }
				break;
			case 5: // _skip: /[\n\r\t ]+/
				spaceToken = true;
				break;
			case 6: // _skip_comment: /#.*(\r?\n)?/
				{ spaceToken = skipComments; }
				break;
			case 7: // _skip_multiline: /\/\*{commentChars}\*\//
				spaceToken = true;
				break;
			case 38:
				return createIDToken(token, ruleIndex);
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
		subTokensOfID.put("as", 39);
		subTokensOfID.put("extend", 40);
		subTokensOfID.put("false", 41);
		subTokensOfID.put("implements", 42);
		subTokensOfID.put("import", 43);
		subTokensOfID.put("separator", 44);
		subTokensOfID.put("set", 45);
		subTokensOfID.put("true", 46);
		subTokensOfID.put("assert", 47);
		subTokensOfID.put("brackets", 48);
		subTokensOfID.put("class", 49);
		subTokensOfID.put("empty", 50);
		subTokensOfID.put("expect", 51);
		subTokensOfID.put("expect-rr", 52);
		subTokensOfID.put("explicit", 53);
		subTokensOfID.put("flag", 54);
		subTokensOfID.put("generate", 55);
		subTokensOfID.put("global", 56);
		subTokensOfID.put("inline", 57);
		subTokensOfID.put("input", 58);
		subTokensOfID.put("interface", 59);
		subTokensOfID.put("lalr", 60);
		subTokensOfID.put("language", 61);
		subTokensOfID.put("layout", 62);
		subTokensOfID.put("left", 63);
		subTokensOfID.put("lexer", 64);
		subTokensOfID.put("lookahead", 65);
		subTokensOfID.put("no-eoi", 66);
		subTokensOfID.put("nonassoc", 67);
		subTokensOfID.put("nonempty", 68);
		subTokensOfID.put("param", 69);
		subTokensOfID.put("parser", 70);
		subTokensOfID.put("prec", 71);
		subTokensOfID.put("returns", 72);
		subTokensOfID.put("right", 73);
		subTokensOfID.put("s", 74);
		subTokensOfID.put("shift", 75);
		subTokensOfID.put("space", 76);
		subTokensOfID.put("void", 77);
		subTokensOfID.put("x", 78);
	}

	protected boolean createIDToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfID.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
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

}
