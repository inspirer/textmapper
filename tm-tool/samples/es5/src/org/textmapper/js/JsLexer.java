package com.test;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class JsLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface States {
		public static final int initial = 0;
		public static final int div = 1;
	}

	public interface Tokens {
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int space = 1;
		public static final int LineTerminatorSequence = 2;
		public static final int MultiLineComment = 3;
		public static final int SingleLineComment = 4;
		public static final int Identifier = 5;
		public static final int _break = 6;
		public static final int _do = 7;
		public static final int _instanceof = 8;
		public static final int typeof = 9;
		public static final int _case = 10;
		public static final int _else = 11;
		public static final int _new = 12;
		public static final int var = 13;
		public static final int _catch = 14;
		public static final int _finally = 15;
		public static final int _return = 16;
		public static final int _void = 17;
		public static final int _continue = 18;
		public static final int _for = 19;
		public static final int _switch = 20;
		public static final int _while = 21;
		public static final int debugger = 22;
		public static final int function = 23;
		public static final int _this = 24;
		public static final int with = 25;
		public static final int _default = 26;
		public static final int _if = 27;
		public static final int _throw = 28;
		public static final int delete = 29;
		public static final int in = 30;
		public static final int _try = 31;
		public static final int _class = 32;
		public static final int _enum = 33;
		public static final int _extends = 34;
		public static final int _super = 35;
		public static final int _const = 36;
		public static final int export = 37;
		public static final int _import = 38;
		public static final int Lcurly = 39;
		public static final int Rcurly = 40;
		public static final int Lparen = 41;
		public static final int Rparen = 42;
		public static final int Lsquare = 43;
		public static final int Rsquare = 44;
		public static final int Dot = 45;
		public static final int Semicolon = 46;
		public static final int Comma = 47;
		public static final int Less = 48;
		public static final int Greater = 49;
		public static final int LessEqual = 50;
		public static final int GreaterEqual = 51;
		public static final int EqualEqual = 52;
		public static final int ExclamationEqual = 53;
		public static final int EqualEqualEqual = 54;
		public static final int ExclamationEqualEqual = 55;
		public static final int Plus = 56;
		public static final int Minus = 57;
		public static final int Mult = 58;
		public static final int Percent = 59;
		public static final int PlusPlus = 60;
		public static final int MinusMinus = 61;
		public static final int LessLess = 62;
		public static final int GreaterGreater = 63;
		public static final int GreaterGreaterGreater = 64;
		public static final int Ampersand = 65;
		public static final int Or = 66;
		public static final int Xor = 67;
		public static final int Exclamation = 68;
		public static final int Tilde = 69;
		public static final int AmpersandAmpersand = 70;
		public static final int OrOr = 71;
		public static final int Questionmark = 72;
		public static final int Colon = 73;
		public static final int Equal = 74;
		public static final int PlusEqual = 75;
		public static final int MinusEqual = 76;
		public static final int MultEqual = 77;
		public static final int PercentEqual = 78;
		public static final int LessLessEqual = 79;
		public static final int GreaterGreaterEqual = 80;
		public static final int GreaterGreaterGreaterEqual = 81;
		public static final int AmpersandEqual = 82;
		public static final int OrEqual = 83;
		public static final int XorEqual = 84;
		public static final int _null = 85;
		public static final int _true = 86;
		public static final int _false = 87;
		public static final int NumericLiteral = 88;
		public static final int StringLiteral = 89;
		public static final int RegularExpressionLiteral = 90;
		public static final int Slash = 91;
		public static final int SlashEqual = 92;
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

	final private StringBuilder tokenBuffer = new StringBuilder(TOKEN_SIZE);

	private int tokenLine;
	private int currLine;
	private int currOffset;

	public JsLexer(Reader stream, ErrorReporter reporter) throws IOException {
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
				tokenBuffer.append(data, tokenOffset, l - tokenOffset);
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

	public String tokenText() {
		return tokenBuffer.toString();
	}

	public int tokenSize() {
		return tokenBuffer.length();
	}

	private static final char[] tmCharClass = unpack_vc_char(1048576,
		"\11\1\1\63\1\11\2\63\1\10\22\1\1\63\1\30\1\43\1\1\1\2\1\33\1\34\1\45\1\16\1\17\1" +
		"\13\1\31\1\24\1\32\1\22\1\12\1\42\11\54\1\41\1\23\1\25\1\27\1\26\1\40\1\1\4\55\1" +
		"\65\1\55\21\46\1\67\2\46\1\20\1\4\1\21\1\36\1\3\1\1\4\56\1\66\1\56\16\47\1\5\2\47" +
		"\1\44\2\47\1\14\1\35\1\15\1\37\41\1\1\63\11\1\1\52\12\1\1\47\4\1\1\52\5\1\27\46\1" +
		"\1\7\46\30\47\1\1\10\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\2\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\2\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\2\46\1\47\1\46\1\47\1\46\3" +
		"\47\2\46\1\47\1\46\1\47\2\46\1\47\3\46\2\47\4\46\1\47\2\46\1\47\3\46\3\47\2\46\1" +
		"\47\2\46\1\47\1\46\1\47\1\46\1\47\2\46\1\47\1\46\2\47\1\46\1\47\2\46\1\47\3\46\1" +
		"\47\1\46\1\47\2\46\2\47\1\52\1\46\3\47\4\52\1\46\1\50\1\47\1\46\1\50\1\47\1\46\1" +
		"\50\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\2\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\2\47\1\46\1\50\1\47\1\46\1\47\3\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\7\47\2\46\1\47\2\46\2\47\1\46\1\47\4\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\105\47\1\52\33\47\22\51\4\1\14\51\16\1\5\51\7\1\1\51\1\1\1\51" +
		"\21\1\160\57\1\46\1\47\1\46\1\47\1\51\1\1\1\46\1\47\2\1\1\51\3\47\1\1\1\46\6\1\1" +
		"\46\1\1\3\46\1\1\1\46\1\1\2\46\1\47\21\46\1\1\11\46\43\47\1\46\2\47\3\46\3\47\1\46" +
		"\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46" +
		"\1\47\1\46\1\47\1\46\1\47\1\46\5\47\1\46\1\47\1\1\1\46\1\47\2\46\2\47\63\46\60\47" +
		"\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47" +
		"\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47" +
		"\1\46\1\47\1\1\5\57\2\1\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\2\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\2\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\1\46\46\2\1\1\51\7\1\47\47\11\1\55" +
		"\57\1\1\1\57\1\1\2\57\1\1\2\57\1\1\1\57\10\1\33\52\5\1\3\52\35\1\13\57\5\1\40\52" +
		"\1\51\12\52\25\57\12\61\4\1\2\52\1\57\143\52\1\1\1\52\7\57\2\1\6\57\2\51\2\57\1\1" +
		"\4\57\2\52\12\61\3\52\2\1\1\52\20\1\1\52\1\57\36\52\33\57\2\1\131\52\13\57\1\52\16" +
		"\1\12\61\41\52\11\57\2\51\4\1\1\51\5\1\26\52\4\57\1\51\11\57\1\51\3\57\1\51\5\57" +
		"\22\1\31\52\3\57\104\1\23\52\61\1\37\57\1\60\66\52\1\57\1\60\1\57\1\52\3\60\10\57" +
		"\4\60\1\57\2\60\1\52\7\57\12\52\2\57\2\1\12\61\1\1\1\51\17\52\1\57\2\60\1\1\10\52" +
		"\2\1\2\52\2\1\26\52\1\1\7\52\1\1\1\52\3\1\4\52\2\1\1\57\1\52\3\60\4\57\2\1\2\60\2" +
		"\1\2\60\1\57\1\52\10\1\1\60\4\1\2\52\1\1\3\52\2\57\2\1\12\61\2\52\17\1\2\57\1\60" +
		"\1\1\6\52\4\1\2\52\2\1\26\52\1\1\7\52\1\1\2\52\1\1\2\52\1\1\2\52\2\1\1\57\1\1\3\60" +
		"\2\57\4\1\2\57\2\1\3\57\3\1\1\57\7\1\4\52\1\1\1\52\7\1\12\61\2\57\3\52\1\57\13\1" +
		"\2\57\1\60\1\1\11\52\1\1\3\52\1\1\26\52\1\1\7\52\1\1\2\52\1\1\5\52\2\1\1\57\1\52" +
		"\3\60\5\57\1\1\2\57\1\60\1\1\2\60\1\57\2\1\1\52\17\1\2\52\2\57\2\1\12\61\21\1\1\57" +
		"\2\60\1\1\10\52\2\1\2\52\2\1\26\52\1\1\7\52\1\1\2\52\1\1\5\52\2\1\1\57\1\52\1\60" +
		"\1\57\1\60\4\57\2\1\2\60\2\1\2\60\1\57\10\1\1\57\1\60\4\1\2\52\1\1\3\52\2\57\2\1" +
		"\12\61\1\1\1\52\20\1\1\57\1\52\1\1\6\52\3\1\3\52\1\1\4\52\3\1\2\52\1\1\1\52\1\1\2" +
		"\52\3\1\2\52\3\1\3\52\3\1\14\52\4\1\2\60\1\57\2\60\3\1\3\60\1\1\3\60\1\57\2\1\1\52" +
		"\6\1\1\60\16\1\12\61\20\1\1\57\3\60\1\1\10\52\1\1\3\52\1\1\27\52\1\1\20\52\3\1\1" +
		"\52\3\57\4\60\1\1\3\57\1\1\4\57\7\1\2\57\1\1\2\52\6\1\2\52\2\57\2\1\12\61\21\1\1" +
		"\57\2\60\1\1\10\52\1\1\3\52\1\1\27\52\1\1\12\52\1\1\5\52\2\1\1\57\1\52\1\60\1\57" +
		"\5\60\1\1\1\57\2\60\1\1\2\60\2\57\7\1\2\60\7\1\1\52\1\1\2\52\2\57\2\1\12\61\1\1\2" +
		"\52\16\1\1\57\2\60\1\1\10\52\1\1\3\52\1\1\51\52\2\1\1\52\3\60\4\57\1\1\3\60\1\1\3" +
		"\60\1\57\1\52\10\1\1\60\10\1\2\52\2\57\2\1\12\61\12\1\6\52\2\1\2\60\1\1\22\52\3\1" +
		"\30\52\1\1\11\52\1\1\1\52\2\1\7\52\3\1\1\57\4\1\3\60\3\57\1\1\1\57\1\1\10\60\6\1" +
		"\12\61\2\1\2\60\15\1\60\52\1\57\2\52\7\57\5\1\6\52\1\51\10\57\1\1\12\61\47\1\2\52" +
		"\1\1\1\52\2\1\2\52\1\1\1\52\2\1\1\52\6\1\4\52\1\1\7\52\1\1\3\52\1\1\1\52\1\1\1\52" +
		"\2\1\2\52\1\1\4\52\1\57\2\52\6\57\1\1\2\57\1\52\2\1\5\52\1\1\1\51\1\1\6\57\2\1\12" +
		"\61\2\1\4\52\40\1\1\52\27\1\2\57\6\1\12\61\13\1\1\57\1\1\1\57\1\1\1\57\4\1\2\60\10" +
		"\52\1\1\44\52\4\1\16\57\1\60\5\57\1\1\2\57\5\52\13\57\1\1\44\57\11\1\1\57\71\1\53" +
		"\52\2\60\4\57\1\60\6\57\1\60\2\57\2\60\2\57\1\52\12\61\6\1\6\52\2\60\2\57\4\52\3" +
		"\57\1\52\3\60\2\52\7\60\3\52\4\57\15\52\1\57\2\60\2\57\6\60\1\57\1\52\1\60\12\61" +
		"\3\60\1\57\2\1\46\46\1\1\1\46\5\1\1\46\2\1\53\52\1\1\1\51\u014c\52\1\1\4\52\2\1\7" +
		"\52\1\1\1\52\1\1\4\52\2\1\51\52\1\1\4\52\2\1\41\52\1\1\4\52\2\1\7\52\1\1\1\52\1\1" +
		"\4\52\2\1\17\52\1\1\71\52\1\1\4\52\2\1\103\52\2\1\3\57\40\1\20\52\20\1\125\52\14" +
		"\1\u026c\52\2\1\21\52\1\63\32\52\5\1\113\52\3\1\3\53\10\52\7\1\15\52\1\1\4\52\3\57" +
		"\13\1\22\52\3\57\13\1\22\52\2\57\14\1\15\52\1\1\3\52\1\1\2\57\14\1\64\52\2\57\1\60" +
		"\7\57\10\60\1\57\2\60\13\57\3\1\1\51\4\1\1\52\1\57\2\1\12\61\41\1\3\57\2\1\12\61" +
		"\6\1\43\52\1\51\64\52\10\1\51\52\1\57\1\52\5\1\106\52\12\1\37\52\1\1\3\57\4\60\2" +
		"\57\3\60\4\1\2\60\1\57\6\60\3\57\12\1\12\61\36\52\2\1\5\52\13\1\54\52\4\1\21\60\7" +
		"\52\2\60\6\1\12\61\46\1\27\52\2\57\2\60\1\57\4\1\65\52\1\60\1\57\1\60\7\57\1\1\1" +
		"\57\1\60\1\57\2\60\10\57\6\60\12\57\2\1\1\57\12\61\6\1\12\61\15\1\1\51\10\1\16\57" +
		"\102\1\4\57\1\60\57\52\1\57\1\60\5\57\1\60\1\57\5\60\1\57\2\60\7\52\4\1\12\61\21" +
		"\1\11\57\14\1\2\57\1\60\36\52\1\60\4\57\2\60\2\57\1\60\3\57\2\52\12\61\54\52\1\57" +
		"\1\60\2\57\3\60\1\57\1\60\3\57\2\60\14\1\44\52\10\60\10\57\2\60\2\57\10\1\12\61\3" +
		"\1\3\52\12\61\36\52\6\51\122\1\3\57\1\1\15\57\1\60\7\57\4\52\1\57\4\52\2\60\1\57" +
		"\2\52\1\1\2\57\6\1\54\47\77\51\15\47\1\51\42\47\45\51\66\57\6\1\4\57\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\11\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\11\47\10\46\6\47\2\1\6\46\2\1\10\47\10\46\10\47\10\46\6\47" +
		"\2\1\6\46\2\1\10\47\1\1\1\46\1\1\1\46\1\1\1\46\1\1\1\46\10\47\10\46\16\47\2\1\10" +
		"\47\10\50\10\47\10\50\10\47\10\50\5\47\1\1\2\47\4\46\1\50\1\1\1\47\3\1\3\47\1\1\2" +
		"\47\4\46\1\50\3\1\4\47\2\1\2\47\4\46\4\1\10\47\5\46\5\1\3\47\1\1\2\47\4\46\1\50\3" +
		"\1\13\63\1\1\1\6\1\7\32\1\2\64\5\1\1\63\17\1\2\62\23\1\1\62\12\1\1\63\21\1\1\51\15" +
		"\1\1\51\20\1\15\51\63\1\15\57\4\1\1\57\3\1\14\57\21\1\1\46\4\1\1\46\2\1\1\47\3\46" +
		"\2\47\3\46\1\47\1\1\1\46\3\1\5\46\6\1\1\46\1\1\1\46\1\1\1\46\1\1\4\46\1\1\1\47\4" +
		"\46\1\47\4\52\1\47\2\1\2\47\2\46\5\1\1\46\4\47\4\1\1\47\21\1\43\53\1\46\1\47\4\53" +
		"\u0a77\1\57\46\1\1\57\47\1\1\1\46\1\47\3\46\2\47\1\46\1\47\1\46\1\47\1\46\1\47\4" +
		"\46\1\47\1\46\2\47\1\46\6\47\2\51\3\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\2\47\6\1\1\46\1\47\1\46\1\47\3\57" +
		"\1\46\1\47\14\1\46\47\1\1\1\47\5\1\1\47\2\1\70\52\7\1\1\51\17\1\1\57\27\52\11\1\7" +
		"\52\1\1\7\52\1\1\7\52\1\1\7\52\1\1\7\52\1\1\7\52\1\1\7\52\1\1\7\52\1\1\40\57\57\1" +
		"\1\51\u01d0\1\1\63\4\1\1\51\1\52\1\53\31\1\11\53\4\57\2\60\1\1\5\51\2\1\3\53\1\51" +
		"\1\52\4\1\126\52\2\1\2\57\2\1\2\51\1\52\1\1\132\52\1\1\3\51\1\52\5\1\51\52\3\1\136" +
		"\52\21\1\33\52\65\1\20\52\u0200\1\u19b6\52\112\1\u51cd\52\63\1\25\52\1\51\u0477\52" +
		"\103\1\50\52\6\51\2\1\u010c\52\1\51\3\1\20\52\12\61\2\52\24\1\1\46\1\47\1\46\1\47" +
		"\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47" +
		"\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47" +
		"\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\52\1\57\4\1\12\57\1\1\1\51\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1" +
		"\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\2\51\1\1\1\57\106\52\12" +
		"\53\2\57\45\1\11\51\2\1\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\3\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\51\10\47\1\46\1\47\1\46\1\47\2\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1" +
		"\47\1\51\2\1\1\46\1\47\1\46\1\47\1\1\1\46\1\47\1\46\3\47\1\46\1\47\1\46\1\47\1\46" +
		"\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\1\46\1\47\4\46" +
		"\2\1\2\46\105\1\1\52\2\51\1\47\7\52\1\57\3\52\1\57\4\52\1\57\27\52\2\60\2\57\1\60" +
		"\30\1\64\52\14\1\2\60\62\52\20\60\1\57\13\1\12\61\6\1\22\57\6\52\3\1\1\52\4\1\12" +
		"\61\34\52\10\57\2\1\27\52\13\57\2\60\14\1\35\52\3\1\3\57\1\60\57\52\1\57\2\60\4\57" +
		"\2\60\1\57\4\60\16\1\1\51\12\61\6\1\5\52\1\57\1\51\11\52\12\61\5\52\1\1\51\52\6\57" +
		"\2\60\2\57\2\60\2\57\11\1\3\52\1\57\10\52\1\57\1\60\2\1\12\61\6\1\20\52\1\51\6\52" +
		"\3\1\1\52\1\60\1\57\1\60\62\52\1\57\1\52\3\57\2\52\2\57\5\52\2\57\1\52\1\57\1\52" +
		"\30\1\2\52\1\51\2\1\13\52\1\60\2\57\2\60\2\1\1\52\2\51\1\60\1\57\12\1\6\52\2\1\6" +
		"\52\2\1\6\52\11\1\7\52\1\1\7\52\1\1\53\47\1\1\4\51\4\1\2\47\132\1\43\52\2\60\1\57" +
		"\2\60\1\57\2\60\1\1\1\60\1\57\2\1\12\61\6\1\u2ba4\52\14\1\27\52\4\1\61\52\u2104\1" +
		"\u016e\52\2\1\152\52\46\1\7\47\14\1\5\47\5\1\1\52\1\57\12\52\1\1\15\52\1\1\5\52\1" +
		"\1\1\52\1\1\2\52\1\1\2\52\1\1\154\52\41\1\u016b\52\22\1\100\52\2\1\66\52\50\1\14" +
		"\52\4\1\20\57\20\1\16\57\5\1\2\62\30\1\3\62\40\1\5\52\1\1\207\52\2\1\1\63\20\1\12" +
		"\61\7\1\32\46\4\1\1\62\1\1\32\47\13\1\12\52\1\51\55\52\2\51\37\52\3\1\6\52\2\1\6" +
		"\52\2\1\6\52\2\1\3\52\43\1\14\52\1\1\32\52\1\1\23\52\1\1\2\52\1\1\17\52\2\1\16\52" +
		"\42\1\173\52\105\1\65\53\210\1\1\57\202\1\35\52\3\1\61\52\17\1\1\57\37\1\40\52\20" +
		"\1\21\52\1\53\10\52\1\53\5\1\46\52\5\57\5\1\36\52\2\1\44\52\4\1\10\52\1\1\5\53\52" +
		"\1\50\46\50\47\116\52\2\1\12\61\126\1\50\52\10\1\64\52\234\1\u0137\52\11\1\26\52" +
		"\12\1\10\52\230\1\6\52\2\1\1\52\1\1\54\52\1\1\2\52\3\1\1\52\2\1\27\52\12\1\27\52" +
		"\11\1\37\52\141\1\26\52\12\1\32\52\106\1\70\52\6\1\2\52\100\1\1\52\3\57\1\1\2\57" +
		"\5\1\4\57\4\52\1\1\3\52\1\1\33\52\4\1\3\57\4\1\1\57\40\1\35\52\3\1\35\52\43\1\10" +
		"\52\1\1\34\52\2\57\31\1\66\52\12\1\26\52\12\1\23\52\15\1\22\52\156\1\111\52\u03b7" +
		"\1\1\60\1\57\1\60\65\52\17\57\37\1\12\61\17\1\3\57\1\60\55\52\3\60\4\57\2\60\2\57" +
		"\25\1\31\52\7\1\12\61\6\1\3\57\44\52\5\57\1\60\10\57\1\1\12\61\20\1\43\52\1\57\2" +
		"\1\1\52\11\1\2\57\1\60\60\52\3\60\11\57\2\60\4\52\13\1\12\61\1\52\45\1\22\52\1\1" +
		"\31\52\3\60\3\57\2\60\1\57\1\60\2\57\170\1\57\52\1\57\3\60\10\57\5\1\12\61\7\1\1" +
		"\57\2\60\1\1\10\52\2\1\2\52\2\1\26\52\1\1\7\52\1\1\2\52\1\1\5\52\2\1\1\57\1\52\2" +
		"\60\1\57\4\60\2\1\2\60\2\1\3\60\11\1\1\60\5\1\5\52\2\60\2\1\7\57\3\1\5\57\u010b\1" +
		"\60\52\3\60\6\57\1\60\1\57\4\60\2\57\1\60\2\57\2\52\1\1\1\52\10\1\12\61\246\1\57" +
		"\52\3\60\4\57\2\1\4\60\2\57\1\60\2\57\77\1\60\52\3\60\10\57\2\60\1\57\1\60\2\57\3" +
		"\1\1\52\13\1\12\61\46\1\53\52\1\57\1\60\1\57\2\60\6\57\1\60\1\57\10\1\12\61\u01d6" +
		"\1\40\46\40\47\12\61\25\1\1\52\u01c0\1\71\52\u0507\1\u0399\52\147\1\157\53\u0b91" +
		"\1\u042f\52\u33d1\1\u0239\52\7\1\37\52\1\1\12\61\146\1\36\52\2\1\5\57\13\1\60\52" +
		"\7\57\11\1\4\51\14\1\12\61\11\1\25\52\5\1\23\52\u0370\1\105\52\13\1\1\52\56\60\20" +
		"\1\4\57\15\51\u4060\1\2\52\u0bfe\1\153\52\5\1\15\52\3\1\11\52\7\1\12\52\3\1\2\57" +
		"\u14c6\1\2\60\3\57\3\1\6\60\10\1\10\57\2\1\7\57\36\1\4\57\224\1\3\57\u01bb\1\32\46" +
		"\32\47\32\46\7\47\1\1\22\47\32\46\32\47\1\46\1\1\2\46\2\1\1\46\2\1\2\46\2\1\4\46" +
		"\1\1\10\46\4\47\1\1\1\47\1\1\7\47\1\1\13\47\32\46\32\47\2\46\1\1\4\46\2\1\10\46\1" +
		"\1\7\46\1\1\32\47\2\46\1\1\4\46\1\1\5\46\1\1\1\46\3\1\7\46\1\1\32\47\32\46\32\47" +
		"\32\46\32\47\32\46\32\47\32\46\32\47\32\46\32\47\32\46\34\47\2\1\31\46\1\1\31\47" +
		"\1\1\6\47\31\46\1\1\31\47\1\1\6\47\31\46\1\1\31\47\1\1\6\47\31\46\1\1\31\47\1\1\6" +
		"\47\31\46\1\1\31\47\1\1\6\47\1\46\1\47\2\1\62\61\u1000\1\305\52\13\1\7\57\u0529\1" +
		"\4\52\1\1\33\52\1\1\2\52\1\1\1\52\2\1\1\52\1\1\12\52\1\1\4\52\1\1\1\52\1\1\1\52\6" +
		"\1\1\52\4\1\1\52\1\1\1\52\1\1\1\52\1\1\3\52\1\1\2\52\1\1\1\52\2\1\1\52\1\1\1\52\1" +
		"\1\1\52\1\1\1\52\1\1\1\52\1\1\2\52\1\1\1\52\2\1\4\52\1\1\7\52\1\1\4\52\1\1\4\52\1" +
		"\1\1\52\1\1\12\52\1\1\21\52\5\1\3\52\1\1\5\52\1\1\21\52\u1144\1\ua6d7\52\51\1\u1035" +
		"\52\13\1\336\52\u3fe2\1\u021e\52\uffff\1\uffff\1\uffff\1\uffff\1\uffff\1\uffff\1" +
		"\uffff\1\uffff\1\uffff\1\uffff\1\uffff\1\u06ed\1\360\57\uffff\1\ufe11\1");

	private static char[] unpack_vc_char(int size, String... st) {
		char[] res = new char[size];
		int t = 0;
		int count = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; ) {
				count = i > 0 || count == 0 ? s.charAt(i++) : count;
				if (i < slen) {
					char val = s.charAt(i++);
					while (count-- > 0) res[t++] = val;
				}
			}
		}
		assert res.length == t;
		return res;
	}

	private static final short tmStateMap[] = {
		0, 1
	};

	private static final short[] tmRuleSymbol = unpack_short(95,
		"\5\1\2\3\4\6\7\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35" +
		"\36\37\40\41\42\43\44\45\46\47\50\51\52\53\54\55\56\57\60\61\62\63\64\65\66\67\70" +
		"\71\72\73\74\75\76\77\100\101\102\103\104\105\106\107\110\111\112\113\114\115\116" +
		"\117\120\121\122\123\124\125\126\127\130\130\130\131\131\132\133\134");

	private static final int tmClassesCount = 56;

	private static final short[] tmGoto = unpack_vc_short(6384,
		"\1\ufffe\1\uffff\2\2\1\3\1\2\2\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15" +
		"\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35" +
		"\1\36\1\37\1\2\1\40\6\2\1\41\2\2\4\uffff\1\42\1\5\3\2\2\uffff\2\2\1\3\1\2\2\uffff" +
		"\1\4\1\5\1\43\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24" +
		"\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\2\1\40\6\2\1\41\2\2\4\uffff" +
		"\1\42\1\5\3\2\2\ufffd\2\2\1\44\3\2\32\ufffd\1\2\1\ufffd\1\2\1\ufffd\15\2\2\ufffd" +
		"\3\2\5\uffff\1\45\62\uffff\11\ufffb\1\5\146\ufffb\1\uffff\3\46\1\47\3\46\2\uffff" +
		"\1\50\1\51\4\46\1\52\43\46\1\uffff\3\46\27\uffc4\1\53\40\uffc4\70\uffd7\70\uffd6" +
		"\70\uffd5\70\uffd4\70\uffd3\70\uffd2\42\uffd1\1\54\11\uffd1\1\54\13\uffd1\70\uffd0" +
		"\70\uffcf\25\uffce\1\55\1\uffce\1\56\40\uffce\26\uffcd\1\57\1\60\40\uffcd\27\uffb4" +
		"\1\61\40\uffb4\27\uffba\1\62\40\uffba\27\uffc6\1\63\1\uffc6\1\64\36\uffc6\27\uffc5" +
		"\1\65\2\uffc5\1\66\35\uffc5\27\uffc3\1\67\40\uffc3\27\uffbd\1\70\4\uffbd\1\71\33" +
		"\uffbd\27\uffbc\1\72\5\uffbc\1\73\32\uffbc\27\uffbb\1\74\40\uffbb\70\uffb9\70\uffb6" +
		"\70\uffb5\22\uffa6\1\75\21\uffa6\1\76\20\uffa6\2\77\1\76\1\uffff\3\37\1\100\3\37" +
		"\2\uffff\31\37\1\101\20\37\1\uffff\3\37\1\uffff\3\40\1\102\3\40\2\uffff\33\40\1\103" +
		"\16\40\1\uffff\3\40\22\uffa6\1\75\17\uffa6\1\41\11\uffa6\1\41\10\uffa6\2\77\1\uffa6" +
		"\70\ufffc\12\uffa0\1\50\1\51\13\uffa0\1\104\40\uffa0\5\uffff\1\105\124\uffff\1\106" +
		"\11\uffff\3\106\6\uffff\2\106\2\uffff\3\46\1\107\3\46\2\uffff\1\110\5\46\1\111\43" +
		"\46\1\uffff\3\46\1\uffff\7\46\2\uffff\52\46\1\uffff\3\46\1\ufff9\7\50\2\ufff9\52" +
		"\50\1\ufff9\3\50\1\uffff\12\51\1\112\54\51\1\uffff\3\52\1\113\3\52\2\uffff\7\52\1" +
		"\46\42\52\1\uffff\3\52\70\uffb1\42\uffa5\1\54\11\uffa5\1\54\10\uffa5\2\114\1\uffa5" +
		"\27\uffc0\1\115\40\uffc0\70\uffcc\26\uffbf\1\116\1\117\40\uffbf\70\uffcb\27\uffca" +
		"\1\120\40\uffca\27\uffc9\1\121\40\uffc9\70\uffb3\70\uffc2\70\uffb2\70\uffc1\70\uffb0" +
		"\70\uffac\70\uffb8\70\uffab\70\uffb7\70\uffaa\42\uffa6\1\75\11\uffa6\1\75\10\uffa6" +
		"\2\77\1\uffa6\42\uffff\1\122\11\uffff\3\122\6\uffff\2\122\32\uffff\2\123\7\uffff" +
		"\1\124\11\uffff\1\124\14\uffff\4\37\1\125\2\37\1\126\33\37\1\127\7\37\1\uffff\13" +
		"\37\70\uffa3\1\uffff\4\40\1\130\2\40\1\131\33\40\1\132\7\40\1\uffff\13\40\70\uffa2" +
		"\70\uff9f\42\uffff\1\133\11\uffff\3\133\6\uffff\2\133\43\uffff\1\134\11\uffff\3\134" +
		"\6\uffff\2\134\2\uffff\7\46\2\uffff\52\46\1\uffff\3\46\2\uffa1\2\110\1\135\3\110" +
		"\32\uffa1\1\110\1\uffa1\1\110\1\uffa1\15\110\2\uffa1\3\110\1\uffff\3\111\1\136\3" +
		"\111\2\uffff\7\111\1\46\42\111\1\uffff\3\111\1\uffff\11\51\1\137\1\112\54\51\1\uffff" +
		"\7\52\2\uffff\52\52\1\uffff\3\52\31\uffff\2\140\7\uffff\1\141\11\uffff\1\141\13\uffff" +
		"\70\uffaf\27\uffbe\1\142\40\uffbe\70\uffae\70\uffc8\70\uffc7\42\uffa4\1\122\11\uffa4" +
		"\3\122\6\uffa4\2\122\1\uffa4\42\uffff\1\124\11\uffff\1\124\13\uffff\42\uffa6\1\124" +
		"\11\uffa6\1\124\13\uffa6\42\uffff\1\143\11\uffff\3\143\6\uffff\2\143\2\uffff\3\37" +
		"\1\100\3\37\1\uffff\32\37\1\101\20\37\1\uffff\3\37\42\uffff\1\144\11\uffff\3\144" +
		"\6\uffff\2\144\43\uffff\1\145\11\uffff\3\145\6\uffff\2\145\2\uffff\3\40\1\102\3\40" +
		"\1\uffff\34\40\1\103\16\40\1\uffff\3\40\42\uffff\1\146\11\uffff\3\146\6\uffff\2\146" +
		"\43\uffff\1\147\11\uffff\3\147\6\uffff\2\147\43\uffff\1\150\11\uffff\3\150\6\uffff" +
		"\2\150\6\uffff\1\151\63\uffff\7\111\2\uffff\52\111\1\uffff\3\111\70\ufffa\42\uffff" +
		"\1\141\11\uffff\1\141\13\uffff\42\uffa5\1\141\11\uffa5\1\141\13\uffa5\70\uffad\42" +
		"\uffff\1\152\11\uffff\3\152\6\uffff\2\152\43\uffff\1\37\11\uffff\3\37\6\uffff\2\37" +
		"\43\uffff\1\153\11\uffff\3\153\6\uffff\2\153\43\uffff\1\40\11\uffff\3\40\6\uffff" +
		"\2\40\43\uffff\1\154\11\uffff\3\154\6\uffff\2\154\43\uffff\1\2\11\uffff\3\2\6\uffff" +
		"\2\2\43\uffff\1\155\11\uffff\3\155\6\uffff\2\155\43\uffff\1\156\11\uffff\3\156\6" +
		"\uffff\2\156\43\uffff\1\157\11\uffff\3\157\6\uffff\2\157\43\uffff\1\2\11\uffff\3" +
		"\2\6\uffff\2\2\43\uffff\1\160\11\uffff\3\160\6\uffff\2\160\43\uffff\1\37\11\uffff" +
		"\3\37\6\uffff\2\37\43\uffff\1\40\11\uffff\3\40\6\uffff\2\40\43\uffff\1\161\11\uffff" +
		"\3\161\6\uffff\2\161\43\uffff\1\110\11\uffff\3\110\6\uffff\2\110\1\uffff");

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
		if (chr >= 0 && chr < 1048576) return tmCharClass[chr];
		return chr == -1 ? 0 : 1;
	}

	public Span next() throws IOException {
		Span token = new Span();
		int state;

		tokenloop:
		do {
			token.offset = currOffset;
			tokenLine = token.line = currLine;
			if (tokenBuffer.length() > TOKEN_SIZE) {
				tokenBuffer.setLength(TOKEN_SIZE);
				tokenBuffer.trimToSize();
			}
			tokenBuffer.setLength(0);
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
					if (l + 1 >= datalen) {
						tokenBuffer.append(data, tokenOffset, l - tokenOffset);
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
			token.endoffset = currOffset;

			if (state == -1) {
				if (charOffset > tokenOffset) {
					tokenBuffer.append(data, tokenOffset, charOffset - tokenOffset);
				}
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset, token.endoffset);
				token.symbol = -1;
				continue;
			}

			if (state == -2) {
				token.symbol = Tokens.eoi;
				token.value = null;
				break tokenloop;
			}

			if (charOffset > tokenOffset) {
				tokenBuffer.append(data, tokenOffset, charOffset - tokenOffset);
			}

			token.symbol = tmRuleSymbol[-state - 3];
			token.value = null;

		} while (token.symbol == -1 || !createToken(token, -state - 3));
		tokenOffset = -1;
		return token;
	}

	protected boolean createToken(Span token, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 0:
				return createIdentifierToken(token, ruleIndex);
			case 1: // space: /[\t\v\f \xa0\ufeff\p{Zs}]/
				spaceToken = true;
				break;
			case 2: // LineTerminatorSequence: /[\n\r\u2028\u2029]|\r\n/
				spaceToken = true;
				break;
			case 3: // MultiLineComment: /\/\*{commentChars}?\*\//
				spaceToken = true;
				break;
			case 4: // SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<String,Integer>();
	static {
		subTokensOfIdentifier.put("break", 5);
		subTokensOfIdentifier.put("do", 6);
		subTokensOfIdentifier.put("instanceof", 7);
		subTokensOfIdentifier.put("typeof", 8);
		subTokensOfIdentifier.put("case", 9);
		subTokensOfIdentifier.put("else", 10);
		subTokensOfIdentifier.put("new", 11);
		subTokensOfIdentifier.put("var", 12);
		subTokensOfIdentifier.put("catch", 13);
		subTokensOfIdentifier.put("finally", 14);
		subTokensOfIdentifier.put("return", 15);
		subTokensOfIdentifier.put("void", 16);
		subTokensOfIdentifier.put("continue", 17);
		subTokensOfIdentifier.put("for", 18);
		subTokensOfIdentifier.put("switch", 19);
		subTokensOfIdentifier.put("while", 20);
		subTokensOfIdentifier.put("debugger", 21);
		subTokensOfIdentifier.put("function", 22);
		subTokensOfIdentifier.put("this", 23);
		subTokensOfIdentifier.put("with", 24);
		subTokensOfIdentifier.put("default", 25);
		subTokensOfIdentifier.put("if", 26);
		subTokensOfIdentifier.put("throw", 27);
		subTokensOfIdentifier.put("delete", 28);
		subTokensOfIdentifier.put("in", 29);
		subTokensOfIdentifier.put("try", 30);
		subTokensOfIdentifier.put("class", 31);
		subTokensOfIdentifier.put("enum", 32);
		subTokensOfIdentifier.put("extends", 33);
		subTokensOfIdentifier.put("super", 34);
		subTokensOfIdentifier.put("const", 35);
		subTokensOfIdentifier.put("export", 36);
		subTokensOfIdentifier.put("import", 37);
		subTokensOfIdentifier.put("null", 84);
		subTokensOfIdentifier.put("true", 85);
		subTokensOfIdentifier.put("false", 86);
	}

	protected boolean createIdentifierToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfIdentifier.get(tokenText());
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
