package org.textmapper.tool.bootstrap.eoi;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class EoiLexer {

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
		int a = 1;
		int b = 2;
		int c = 3;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int id = 1;
		int Colon = 2;
		int Semicolon = 3;
		int Comma = 4;
		int gotoc = 5;
		int _skip = 6;
		int Lparen = 7;
		int Rparen = 8;
		int _customEOI = 9;
		int _retfromA = 10;
		int _retfromB = 11;
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

	private int eoiToGo = 5;

	public EoiLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 11, 11, 1, 1, 11, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		11, 1, 1, 1, 1, 1, 1, 1, 8, 9, 1, 1, 4, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 5, 1, 7, 1,
		1, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 1, 1, 1, 1, 10,
		1, 10, 10, 6, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
	};

	private static final short tmStateMap[] = {
		0, 12, 16, 20
	};

	private static final int[] tmRuleSymbol = unpack_int(18,
		"\uffff\uffff\0\0\1\0\2\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\7\0\10\0\12\0\7\0\10\0\13" +
		"\0\0\0");

	private static final int tmClassesCount = 12;

	private static final short[] tmGoto = unpack_vc_short(264,
		"\1\13\1\uffff\1\12\1\11\1\10\1\5\1\4\1\uffff\1\3\1\2\1\4\1\1\13\ufff8\1\1\14\ufff6" +
		"\14\ufff7\6\ufffd\1\4\3\ufffd\1\4\1\ufffd\6\uffff\1\6\14\uffff\1\7\4\uffff\14\ufff9" +
		"\14\ufffa\14\ufffb\14\ufffc\14\ufff5\1\17\1\uffff\1\12\1\11\1\10\1\5\1\4\1\uffff" +
		"\1\16\1\15\1\4\1\1\14\ufff3\14\ufff4\14\ufff2\1\23\1\uffff\1\12\1\11\1\10\1\5\1\4" +
		"\1\uffff\1\22\1\21\1\4\1\1\14\ufff0\14\ufff1\14\uffef\1\25\1\uffff\1\12\1\11\1\10" +
		"\1\5\1\4\3\uffff\1\4\1\1\14\uffee");

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
		if (chr >= 0 && chr < 123) return tmCharClass[chr];
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

			token.symbol = tmRuleSymbol[-1 - state];
			token.value = null;

			if (token.symbol == -1) {
				reporter.error(MessageFormat.format("invalid token at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset, token.endoffset);
			}

		} while (token.symbol == -1 || !createToken(token, -1 - state));
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
			case 6: // gotoc: /<c>/
				{ state = States.c; }
				break;
			case 7: // _skip: /[\n\t\r ]+/
				spaceToken = true;
				break;
			case 8: // '(': /\(/
				{ state = States.a; }
				break;
			case 10: // _customEOI: /{eoi}/
				spaceToken = true;
				{ if (--eoiToGo < 0) { token.symbol = Tokens.eoi; spaceToken = false; } }
				break;
			case 11: // '(': /\(/
				{ state = States.b; }
				break;
			case 12: // ')': /\)/
				{ state = States.initial; }
				break;
			case 13: // _retfromA: /{eoi}/
				spaceToken = true;
				{ state = States.initial; }
				break;
			case 15: // ')': /\)/
				{ state = States.a; }
				break;
			case 16: // _retfromB: /{eoi}/
				spaceToken = true;
				{ state = States.a; }
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
