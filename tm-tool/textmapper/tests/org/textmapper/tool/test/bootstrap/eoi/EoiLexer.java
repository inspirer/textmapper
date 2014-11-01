package org.textmapper.tool.test.bootstrap.eoi;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class EoiLexer {

	public static class LapgSymbol {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface States {
		public static final int initial = 0;
		public static final int a = 1;
		public static final int b = 2;
	}

	public interface Tokens {
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int id = 1;
		public static final int Colon = 2;
		public static final int Semicolon = 3;
		public static final int Comma = 4;
		public static final int _skip = 5;
		public static final int Lparen = 6;
		public static final int Rparen = 7;
		public static final int _customEOI = 8;
		public static final int _retfromA = 9;
		public static final int _retfromB = 10;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset, int endoffset);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen;
	private int tokenOffset;
	private int l;
	private int charOffset;
	private int chr;

	private int state;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine;
	private int currLine;
	private int currOffset;

	private int eoiToGo = 5;

	public EoiLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.state = 0;
		tokenLine = currLine = 1;
		currOffset = 0;
		this.stream = stream;
		datalen = stream.read(data);
		l = 0;
		tokenOffset = -1;
		if (l + 1 >= datalen) {
			if (l < datalen) {
				data[0] = data[l];
				datalen = Math.max(stream.read(data, 1, data.length - 1) + 1, 1);
			} else {
				datalen = stream.read(data);
			}
			l = 0;
		}
		charOffset = l;
		chr = l < datalen ? data[l++] : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < datalen &&
				Character.isLowSurrogate(data[l])) {
			chr = Character.toCodePoint((char) chr, data[l++]);
		}
	}

	protected void advance() throws IOException {
		if (chr == -1) return;
		currOffset += l - charOffset;
		if (chr == '\n') {
			currLine++;
		}
		if (l + 1 >= datalen) {
			if (tokenOffset >= 0) {
				token.append(data, tokenOffset, l - tokenOffset);
				tokenOffset = 0;
			}
			if (l < datalen) {
				data[0] = data[l];
				datalen = Math.max(stream.read(data, 1, data.length - 1) + 1, 1);
			} else {
				datalen = stream.read(data);
			}
			l = 0;
		}
		charOffset = l;
		chr = l < datalen ? data[l++] : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < datalen &&
				Character.isLowSurrogate(data[l])) {
			chr = Character.toCodePoint((char) chr, data[l++]);
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

	public String current() {
		return token.toString();
	}

	private static final short tmCharClass[] = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 8, 1, 1, 8, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		8, 1, 1, 1, 1, 1, 1, 1, 5, 6, 1, 1, 4, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 1, 1, 1, 1,
		1, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 1, 1, 1, 7,
		1, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 1, 1, 1, 1
	};

	private static final short tmStateMap[] = {
		0, 1, 2
	};

	private static final short[] tmRuleSymbol = unpack_short(14,
		"\1\2\3\4\5\6\7\10\6\7\11\6\7\12");

	private static final int tmClassesCount = 9;

	private static final short[] tmGoto = unpack_vc_short(153,
		"\1\ufffe\1\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\uffff\1\4\1\5\1\6\1\14\1\15" +
		"\1\11\1\12\1\16\1\uffff\1\4\1\5\1\6\1\17\1\20\1\11\1\12\11\ufff6\11\ufffc\11\ufffb" +
		"\11\ufffa\11\ufff8\11\ufff7\7\ufffd\1\11\1\ufffd\10\ufff9\1\12\11\ufff3\11\ufff5" +
		"\11\ufff4\11\ufff0\11\ufff2\11\ufff1");

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

	public LapgSymbol next() throws IOException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {
			lapg_n.offset = currOffset;
			tokenLine = lapg_n.line = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			tokenOffset = charOffset;

			for (state = tmStateMap[this.state]; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == -1) {
					lapg_n.endoffset = currOffset;
					lapg_n.symbol = 0;
					lapg_n.value = null;
					reporter.error("Unexpected end of input reached", lapg_n.line, lapg_n.offset, lapg_n.endoffset);
					lapg_n.offset = currOffset;
					tokenOffset = -1;
					return lapg_n;
				}
				if (state >= -1 && chr != -1) {
					currOffset += l - charOffset;
					if (chr == '\n') {
						currLine++;
					}
					if (l + 1 >= datalen) {
						token.append(data, tokenOffset, l - tokenOffset);
						tokenOffset = 0;
						if (l < datalen) {
							data[0] = data[l];
							datalen = Math.max(stream.read(data, 1, data.length - 1) + 1, 1);
						} else {
							datalen = stream.read(data);
						}
						l = 0;
					}
					charOffset = l;
					chr = l < datalen ? data[l++] : -1;
					if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < datalen &&
							Character.isLowSurrogate(data[l])) {
						chr = Character.toCodePoint((char) chr, data[l++]);
					}
				}
			}
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				if (charOffset > tokenOffset) {
					token.append(data, tokenOffset, charOffset - tokenOffset);
				}
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, current()), lapg_n.line, lapg_n.offset, lapg_n.endoffset);
				lapg_n.symbol = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.symbol = Tokens.eoi;
				lapg_n.value = null;
				tokenOffset = -1;
				return lapg_n;
			}

			if (charOffset > tokenOffset) {
				token.append(data, tokenOffset, charOffset - tokenOffset);
			}

			lapg_n.symbol = tmRuleSymbol[-state - 3];
			lapg_n.value = null;

		} while (lapg_n.symbol == -1 || !createToken(lapg_n, -state - 3));
		tokenOffset = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 4: // _skip: /[\n\t\r ]+/
				spaceToken = true;
				break;
			case 5: // '(': /\(/
				state = States.a;
				break;
			case 7: // _customEOI: /{eoi}/
				spaceToken = true;
				 if (--eoiToGo == 0) { lapg_n.symbol = Tokens.eoi; } 
				break;
			case 8: // '(': /\(/
				state = States.b;
				break;
			case 9: // ')': /\)/
				state = States.initial;
				break;
			case 10: // _retfromA: /{eoi}/
				spaceToken = true;
				state = States.initial;
				break;
			case 12: // ')': /\)/
				state = States.a;
				break;
			case 13: // _retfromB: /{eoi}/
				spaceToken = true;
				state = States.a;
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

	/* package */ static short[] unpack_short(int size, String... st) {
		short[] res = new short[size];
		int t = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; i++) {
				res[t++] = (short) s.charAt(i);
			}
		}
		assert res.length == t;
		return res;
	}
}
