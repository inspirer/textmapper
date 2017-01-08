package org.textmapper.json;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class JsonLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int Lbrace = 1;
		int Rbrace = 2;
		int Lbrack = 3;
		int Rbrack = 4;
		int Colon = 5;
		int Comma = 6;
		int space = 7;
		int JSONString = 8;
		int JSONNumber = 9;
		int _null = 10;
		int _true = 11;
		int _false = 12;
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

	public JsonLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 22, 22, 1, 1, 22, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		22, 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 28, 7, 11, 13, 23,
		12, 26, 26, 26, 26, 26, 26, 26, 26, 26, 6, 1, 1, 1, 1, 1,
		1, 24, 24, 24, 24, 27, 24, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 9, 5, 1, 1,
		1, 20, 25, 24, 24, 18, 19, 1, 1, 1, 1, 1, 15, 1, 14, 1,
		1, 1, 17, 21, 16, 10, 1, 1, 1, 1, 1, 2, 1, 3
	};

	private static final short tmBacktracking[] = {
		8, 5, 8, 2
	};

	private static final int tmFirstRule = -3;

	private static final int[] tmRuleSymbol = unpack_int(14,
		"\uffff\uffff\0\0\1\0\2\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0");

	private static final int tmClassesCount = 29;

	private static final short[] tmGoto = unpack_vc_short(1044,
		"\1\ufffc\1\ufffd\1\43\1\42\1\41\1\40\1\37\1\36\1\27\2\ufffd\1\26\1\25\1\ufffd\1\21" +
		"\1\ufffd\1\15\2\ufffd\1\10\2\ufffd\1\7\3\ufffd\1\1\2\ufffd\14\ufff3\1\1\1\uffff\4" +
		"\ufff3\1\ufffe\7\ufff3\1\1\1\ufffe\1\ufff3\13\ufffd\1\4\1\3\15\ufffd\1\3\1\ufffd" +
		"\1\4\14\ufff3\1\3\15\ufff3\1\3\2\ufff3\14\ufffd\1\3\15\ufffd\1\3\16\ufffd\1\6\15" +
		"\ufffd\1\6\2\ufffd\14\ufff3\1\6\5\ufff3\1\ufffe\7\ufff3\1\6\1\ufffe\1\ufff3\26\ufff5" +
		"\1\7\6\ufff5\24\ufffd\1\11\27\ufffd\1\12\42\ufffd\1\13\31\ufffd\1\14\12\ufffd\35" +
		"\ufff0\21\ufffd\1\16\25\ufffd\1\17\44\ufffd\1\20\12\ufffd\35\ufff1\12\ufffd\1\22" +
		"\41\ufffd\1\23\34\ufffd\1\24\15\ufffd\35\ufff2\15\ufff3\1\uffff\4\ufff3\1\ufffe\10" +
		"\ufff3\1\ufffe\1\ufff3\14\ufffd\1\25\15\ufffd\1\1\3\ufffd\7\27\1\35\1\30\23\27\10" +
		"\ufffd\2\27\1\31\3\ufffd\1\27\1\ufffd\2\27\1\ufffd\1\27\3\ufffd\1\27\1\ufffd\1\27" +
		"\17\ufffd\1\32\5\ufffd\3\32\3\ufffd\4\32\15\ufffd\1\33\5\ufffd\3\33\3\ufffd\4\33" +
		"\15\ufffd\1\34\5\ufffd\3\34\3\ufffd\4\34\15\ufffd\1\27\5\ufffd\3\27\3\ufffd\4\27" +
		"\1\ufffd\35\ufff4\35\ufff6\35\ufff7\35\ufff8\35\ufff9\35\ufffa\35\ufffb");

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
		if (chr >= 0 && chr < 126) return tmCharClass[chr];
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
			for (state = this.state; state >= 0; ) {
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
			case 8: // space: /[\t\r\n ]+/
				spaceToken = true;
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
