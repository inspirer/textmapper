package org.textmapper.tool.bootstrap.states;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class StatesLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
	}

	public interface States {
		int initial = 0;
		int a = 1;
		int b = 2;
		int c = 3;
		int d = 4;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int x = 1;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset);
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

	public StatesLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 13, 1, 1, 1, 1, 11, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 2, 3, 4, 5, 1, 12, 1, 1, 7, 1, 1, 10, 1, 8, 1,
		1, 1, 1, 1, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
	};

	private static final short tmStateMap[] = {
		0, 1, 2, 3, 4
	};

	private static final int[] tmRuleSymbol = unpack_int(16,
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0");

	private static final int tmClassesCount = 14;

	private static final short[] tmGoto = unpack_vc_short(420,
		"\1\ufffe\1\uffff\1\5\16\uffff\1\6\1\7\1\10\1\11\1\12\5\uffff\1\13\2\uffff\1\14\1" +
		"\uffff\1\15\1\16\1\11\1\12\5\uffff\1\13\2\uffff\1\17\1\20\1\uffff\1\21\1\11\1\12" +
		"\5\uffff\1\13\2\uffff\1\22\1\23\1\24\2\uffff\1\12\6\uffff\16\ufffd\16\ufffc\16\ufffb" +
		"\16\ufffa\16\ufff0\10\uffff\1\25\5\uffff\16\uffee\16\ufff9\16\ufff8\16\ufff7\16\ufff6" +
		"\16\ufff5\16\ufff4\16\ufff3\16\ufff2\16\ufff1\7\uffff\1\26\17\uffff\1\27\13\uffff" +
		"\1\30\10\uffff\1\31\25\uffff\1\32\16\uffff\1\33\16\uffff\1\34\16\uffff\1\35\16\uffef");

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
					token.symbol = 0;
					token.value = null;
					reporter.error("Unexpected end of input reached", token.line, token.offset);
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

			if (state == -1) {
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset);
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
			case 0: // x: /a/
				state = States.a;
				break;
			case 1: // x: /b/
				state = States.b;
				break;
			case 2: // x: /c/
				state = States.c;
				break;
			case 3: // x: /d/
				state = States.d;
				break;
			case 4: // x: /a/
				state = States.a;
				break;
			case 5: // x: /c/
				state = States.c;
				break;
			case 6: // x: /d/
				state = States.d;
				break;
			case 7: // x: /a/
				state = States.a;
				break;
			case 8: // x: /b/
				state = States.b;
				break;
			case 9: // x: /d/
				state = States.d;
				break;
			case 10: // x: /a/
				state = States.a;
				break;
			case 11: // x: /b/
				state = States.b;
				break;
			case 12: // x: /c/
				state = States.c;
				break;
			case 13: // x: /!/
				switch(state) {
					case States.b:
						state = States.c;
						break;
					case States.c:
						state = States.d;
						break;
					default:
						state = States.b;
						break;
				}
				break;
			case 14: // x: /initialIfD/
				switch(state) {
					case States.d:
						state = States.initial;
						break;
				}
				break;
			case 15: // x: /D/
				state = States.d;
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
