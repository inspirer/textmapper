package org.textmapper.templates.java;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class JavaLexer {

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
		int WhiteSpace = 1;
		int EndOfLineComment = 2;
		int TraditionalComment = 3;
		int Identifier = 4;
		int kw_abstract = 5;
		int kw_assert = 6;
		int kw_boolean = 7;
		int kw_break = 8;
		int kw_byte = 9;
		int kw_case = 10;
		int kw_catch = 11;
		int kw_char = 12;
		int kw_class = 13;
		int kw_const = 14;
		int kw_continue = 15;
		int kw_default = 16;
		int kw_do = 17;
		int kw_double = 18;
		int kw_else = 19;
		int kw_enum = 20;
		int kw_extends = 21;
		int kw_final = 22;
		int kw_finally = 23;
		int kw_float = 24;
		int kw_for = 25;
		int kw_goto = 26;
		int kw_if = 27;
		int kw_implements = 28;
		int kw_import = 29;
		int kw_instanceof = 30;
		int kw_int = 31;
		int kw_interface = 32;
		int kw_long = 33;
		int kw_native = 34;
		int kw_new = 35;
		int kw_package = 36;
		int kw_private = 37;
		int kw_protected = 38;
		int kw_public = 39;
		int kw_return = 40;
		int kw_short = 41;
		int kw_static = 42;
		int kw_strictfp = 43;
		int kw_super = 44;
		int kw_switch = 45;
		int kw_synchronized = 46;
		int kw_this = 47;
		int kw_throw = 48;
		int kw_throws = 49;
		int kw_transient = 50;
		int kw_try = 51;
		int kw_void = 52;
		int kw_volatile = 53;
		int kw_while = 54;
		int IntegerLiteral = 55;
		int FloatingPointLiteral = 56;
		int BooleanLiteral = 57;
		int CharacterLiteral = 58;
		int StringLiteral = 59;
		int NullLiteral = 60;
		int Lparen = 61;
		int Rparen = 62;
		int Lbrace = 63;
		int Rbrace = 64;
		int Lbrack = 65;
		int Rbrack = 66;
		int Semicolon = 67;
		int Comma = 68;
		int Dot = 69;
		int DotDotDot = 70;
		int Assign = 71;
		int Gt = 72;
		int Lt = 73;
		int Excl = 74;
		int Tilde = 75;
		int Quest = 76;
		int Colon = 77;
		int AssignAssign = 78;
		int LtAssign = 79;
		int GtAssign = 80;
		int ExclAssign = 81;
		int AndAnd = 82;
		int OrOr = 83;
		int PlusPlus = 84;
		int MinusMinus = 85;
		int Plus = 86;
		int Minus = 87;
		int Mult = 88;
		int Div = 89;
		int And = 90;
		int Or = 91;
		int Xor = 92;
		int Rem = 93;
		int LtLt = 94;
		int GtGt = 95;
		int GtGtGt = 96;
		int PlusAssign = 97;
		int MinusAssign = 98;
		int MultAssign = 99;
		int DivAssign = 100;
		int AndAssign = 101;
		int OrAssign = 102;
		int XorAssign = 103;
		int RemAssign = 104;
		int LtLtAssign = 105;
		int GtGtAssign = 106;
		int GtGtGtAssign = 107;
		int Atsign = 108;
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

	public JavaLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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

	private static final char[] tmCharClass = unpack_vc_char(205745,
		"\11\1\1\2\1\3\1\1\1\2\1\4\14\1\1\5\5\1\1\2\1\6\1\7\1\1\1\10\1\11\1\12\1\13\1\14\1" +
		"\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\2\26\4\27\2\30\1\31\1\32\1\33\1\34\1" +
		"\35\1\36\1\37\1\40\1\41\1\40\1\42\1\43\1\42\5\10\1\44\3\10\1\45\7\10\1\46\2\10\1" +
		"\47\1\50\1\51\1\52\1\53\1\1\1\40\1\54\1\40\1\42\1\43\1\55\5\10\1\44\1\10\1\56\1\10" +
		"\1\45\1\10\1\56\1\10\1\56\1\57\2\10\1\46\2\10\1\60\1\61\1\62\1\63\53\1\1\10\12\1" +
		"\1\10\4\1\1\10\5\1\27\10\1\1\37\10\1\1\u01ca\10\4\1\14\10\16\1\5\10\7\1\1\10\1\1" +
		"\1\10\201\1\5\10\1\1\2\10\2\1\4\10\1\1\1\10\6\1\1\10\1\1\3\10\1\1\1\10\1\1\24\10" +
		"\1\1\123\10\1\1\213\10\10\1\246\10\1\1\46\10\2\1\1\10\6\1\51\10\107\1\33\10\4\1\4" +
		"\10\55\1\53\10\25\1\12\64\4\1\2\10\1\1\143\10\1\1\1\10\17\1\2\10\7\1\2\10\12\64\3" +
		"\10\2\1\1\10\20\1\1\10\1\1\36\10\35\1\131\10\13\1\1\10\16\1\12\64\41\10\11\1\2\10" +
		"\4\1\1\10\5\1\26\10\4\1\1\10\11\1\1\10\3\1\1\10\27\1\31\10\7\1\13\10\5\1\30\10\1" +
		"\1\6\10\21\1\52\10\72\1\66\10\3\1\1\10\22\1\1\10\7\1\12\10\4\1\12\64\1\1\20\10\4" +
		"\1\10\10\2\1\2\10\2\1\26\10\1\1\7\10\1\1\1\10\3\1\4\10\3\1\1\10\20\1\1\10\15\1\2" +
		"\10\1\1\3\10\4\1\12\64\2\10\12\1\1\10\10\1\6\10\4\1\2\10\2\1\26\10\1\1\7\10\1\1\2" +
		"\10\1\1\2\10\1\1\2\10\37\1\4\10\1\1\1\10\7\1\12\64\2\1\3\10\20\1\11\10\1\1\3\10\1" +
		"\1\26\10\1\1\7\10\1\1\2\10\1\1\5\10\3\1\1\10\22\1\1\10\17\1\2\10\4\1\12\64\11\1\1" +
		"\10\13\1\10\10\2\1\2\10\2\1\26\10\1\1\7\10\1\1\2\10\1\1\5\10\3\1\1\10\36\1\2\10\1" +
		"\1\3\10\4\1\12\64\1\1\1\10\21\1\1\10\1\1\6\10\3\1\3\10\1\1\4\10\3\1\2\10\1\1\1\10" +
		"\1\1\2\10\3\1\2\10\3\1\3\10\3\1\14\10\26\1\1\10\25\1\12\64\25\1\10\10\1\1\3\10\1" +
		"\1\27\10\1\1\20\10\3\1\1\10\32\1\3\10\2\1\1\10\2\1\2\10\4\1\12\64\20\1\1\10\4\1\10" +
		"\10\1\1\3\10\1\1\27\10\1\1\12\10\1\1\5\10\3\1\1\10\37\1\2\10\1\1\2\10\4\1\12\64\1" +
		"\1\2\10\21\1\11\10\1\1\3\10\1\1\51\10\2\1\1\10\20\1\1\10\5\1\3\10\10\1\3\10\4\1\12" +
		"\64\12\1\6\10\5\1\22\10\3\1\30\10\1\1\11\10\1\1\1\10\2\1\7\10\37\1\12\64\21\1\60" +
		"\10\1\1\2\10\14\1\7\10\11\1\12\64\47\1\2\10\1\1\1\10\1\1\5\10\1\1\30\10\1\1\1\10" +
		"\1\1\12\10\1\1\2\10\11\1\1\10\2\1\5\10\1\1\1\10\11\1\12\64\2\1\4\10\40\1\1\10\37" +
		"\1\12\64\26\1\10\10\1\1\44\10\33\1\5\10\163\1\53\10\24\1\1\10\12\64\6\1\6\10\4\1" +
		"\4\10\3\1\1\10\3\1\2\10\7\1\3\10\4\1\15\10\14\1\1\10\1\1\12\64\6\1\46\10\1\1\1\10" +
		"\5\1\1\10\2\1\53\10\1\1\u014d\10\1\1\4\10\2\1\7\10\1\1\1\10\1\1\4\10\2\1\51\10\1" +
		"\1\4\10\2\1\41\10\1\1\4\10\2\1\7\10\1\1\1\10\1\1\4\10\2\1\17\10\1\1\71\10\1\1\4\10" +
		"\2\1\103\10\45\1\20\10\20\1\126\10\2\1\6\10\3\1\u026c\10\2\1\21\10\1\1\32\10\5\1" +
		"\113\10\3\1\13\10\7\1\22\10\15\1\23\10\16\1\22\10\16\1\15\10\1\1\3\10\17\1\64\10" +
		"\43\1\1\10\4\1\1\10\3\1\12\64\46\1\12\64\6\1\131\10\7\1\5\10\2\1\42\10\1\1\1\10\5" +
		"\1\106\10\12\1\37\10\47\1\12\64\36\10\2\1\5\10\13\1\54\10\4\1\32\10\6\1\12\64\46" +
		"\1\27\10\11\1\65\10\53\1\12\64\6\1\12\64\15\1\1\10\135\1\57\10\21\1\10\10\3\1\12" +
		"\64\51\1\36\10\15\1\2\10\12\64\54\10\32\1\44\10\34\1\12\64\3\1\3\10\12\64\44\10\2" +
		"\1\11\10\7\1\53\10\2\1\3\10\51\1\4\10\1\1\6\10\1\1\2\10\3\1\1\10\5\1\300\10\100\1" +
		"\u0116\10\2\1\6\10\2\1\46\10\2\1\6\10\2\1\10\10\1\1\1\10\1\1\1\10\1\1\1\10\1\1\37" +
		"\10\2\1\65\10\1\1\7\10\1\1\1\10\3\1\3\10\1\1\7\10\3\1\4\10\2\1\6\10\4\1\15\10\5\1" +
		"\3\10\1\1\7\10\164\1\1\10\15\1\1\10\20\1\15\10\145\1\1\10\4\1\1\10\2\1\12\10\1\1" +
		"\1\10\3\1\5\10\6\1\1\10\1\1\1\10\1\1\1\10\1\1\4\10\1\1\13\10\2\1\4\10\5\1\5\10\4" +
		"\1\1\10\21\1\51\10\u0a77\1\345\10\6\1\4\10\3\1\2\10\14\1\46\10\1\1\1\10\5\1\1\10" +
		"\2\1\70\10\7\1\1\10\20\1\27\10\11\1\7\10\1\1\7\10\1\1\7\10\1\1\7\10\1\1\7\10\1\1" +
		"\7\10\1\1\7\10\1\1\7\10\120\1\1\10\u01d5\1\3\10\31\1\11\10\7\1\5\10\2\1\5\10\4\1" +
		"\126\10\6\1\3\10\1\1\132\10\1\1\4\10\5\1\53\10\1\1\136\10\21\1\40\10\60\1\20\10\u0200" +
		"\1\u19c0\10\100\1\u568d\10\103\1\56\10\2\1\u010d\10\3\1\20\10\12\64\2\10\24\1\57" +
		"\10\20\1\37\10\2\1\120\10\47\1\11\10\2\1\147\10\2\1\100\10\5\1\2\10\1\1\1\10\1\1" +
		"\5\10\30\1\20\10\1\1\3\10\1\1\4\10\1\1\27\10\35\1\64\10\16\1\62\10\34\1\12\64\30" +
		"\1\6\10\3\1\1\10\1\1\2\10\1\1\12\64\34\10\12\1\27\10\31\1\35\10\7\1\57\10\34\1\1" +
		"\10\12\64\6\1\5\10\1\1\12\10\12\64\5\10\1\1\51\10\27\1\3\10\1\1\10\10\4\1\12\64\6" +
		"\1\27\10\3\1\1\10\3\1\62\10\1\1\1\10\3\1\2\10\2\1\5\10\2\1\1\10\1\1\1\10\30\1\3\10" +
		"\2\1\13\10\7\1\3\10\14\1\6\10\2\1\6\10\2\1\6\10\11\1\7\10\1\1\7\10\1\1\53\10\1\1" +
		"\16\10\6\1\163\10\15\1\12\64\6\1\u2ba4\10\14\1\27\10\4\1\61\10\u2104\1\u016e\10\2" +
		"\1\152\10\46\1\7\10\14\1\5\10\5\1\1\10\1\1\12\10\1\1\15\10\1\1\5\10\1\1\1\10\1\1" +
		"\2\10\1\1\2\10\1\1\154\10\41\1\u016b\10\22\1\100\10\2\1\66\10\50\1\14\10\164\1\5" +
		"\10\1\1\207\10\23\1\12\64\7\1\32\10\6\1\32\10\13\1\131\10\3\1\6\10\2\1\6\10\2\1\6" +
		"\10\2\1\3\10\43\1\14\10\1\1\32\10\1\1\23\10\1\1\2\10\1\1\17\10\2\1\16\10\42\1\173" +
		"\10\105\1\65\10\u010b\1\35\10\3\1\61\10\57\1\40\10\15\1\36\10\5\1\46\10\12\1\36\10" +
		"\2\1\44\10\4\1\10\10\1\1\5\10\52\1\236\10\2\1\12\64\6\1\44\10\4\1\44\10\4\1\50\10" +
		"\10\1\64\10\14\1\13\10\1\1\17\10\1\1\7\10\1\1\2\10\1\1\13\10\1\1\17\10\1\1\7\10\1" +
		"\1\2\10\103\1\u0137\10\11\1\26\10\12\1\10\10\30\1\6\10\1\1\52\10\1\1\11\10\105\1" +
		"\6\10\2\1\1\10\1\1\54\10\1\1\2\10\3\1\1\10\2\1\27\10\12\1\27\10\11\1\37\10\101\1" +
		"\23\10\1\1\2\10\12\1\26\10\12\1\32\10\106\1\70\10\6\1\2\10\100\1\1\10\17\1\4\10\1" +
		"\1\3\10\1\1\35\10\52\1\35\10\3\1\35\10\43\1\10\10\1\1\34\10\33\1\66\10\12\1\26\10" +
		"\12\1\23\10\15\1\22\10\156\1\111\10\67\1\63\10\15\1\63\10\15\1\44\10\14\1\12\64\u0146" +
		"\1\52\10\6\1\2\10\116\1\35\10\12\1\1\10\10\1\26\10\52\1\22\10\56\1\25\10\33\1\27" +
		"\10\14\1\65\10\56\1\12\64\1\1\2\10\2\1\1\10\15\1\55\10\40\1\31\10\7\1\12\64\11\1" +
		"\44\10\17\1\12\64\4\1\1\10\2\1\1\10\10\1\43\10\3\1\1\10\14\1\60\10\16\1\4\10\13\1" +
		"\12\64\1\10\1\1\1\10\43\1\22\10\1\1\31\10\23\1\2\10\77\1\7\10\1\1\1\10\1\1\4\10\1" +
		"\1\17\10\1\1\12\10\7\1\57\10\21\1\12\64\13\1\10\10\2\1\2\10\2\1\26\10\1\1\7\10\1" +
		"\1\2\10\1\1\5\10\3\1\1\10\22\1\1\10\14\1\5\10\236\1\65\10\22\1\4\10\5\1\12\64\5\1" +
		"\3\10\36\1\60\10\24\1\2\10\1\1\1\10\10\1\12\64\246\1\57\10\51\1\4\10\44\1\60\10\24" +
		"\1\1\10\13\1\12\64\46\1\53\10\15\1\1\10\7\1\12\64\66\1\33\10\25\1\12\64\6\1\7\10" +
		"\271\1\54\10\164\1\100\10\12\64\25\1\10\10\2\1\1\10\2\1\10\10\1\1\2\10\1\1\30\10" +
		"\17\1\1\10\1\1\1\10\16\1\12\64\106\1\10\10\2\1\47\10\20\1\1\10\1\1\1\10\34\1\1\10" +
		"\12\1\50\10\7\1\1\10\25\1\1\10\13\1\56\10\23\1\1\10\22\1\111\10\u0107\1\11\10\1\1" +
		"\45\10\21\1\1\10\17\1\12\64\30\1\36\10\160\1\7\10\1\1\2\10\1\1\46\10\25\1\1\10\11" +
		"\1\12\64\6\1\6\10\1\1\2\10\1\1\40\10\16\1\1\10\7\1\12\64\u0136\1\23\10\17\1\1\10" +
		"\1\1\15\10\1\1\42\10\34\1\12\64\126\1\1\10\117\1\u039a\10\146\1\157\10\21\1\304\10" +
		"\u0a4c\1\141\10\17\1\u0430\10\21\1\6\10\u0fb9\1\u0247\10\u21b9\1\u0239\10\7\1\37" +
		"\10\1\1\12\64\6\1\117\10\1\1\12\64\6\1\36\10\22\1\60\10\20\1\4\10\14\1\12\64\11\1" +
		"\25\10\5\1\23\10\u02b0\1\100\10\200\1\113\10\5\1\1\10\102\1\15\10\100\1\2\10\1\1" +
		"\1\10\34\1\u17f8\10\10\1\u04d6\10\52\1\11\10\u22e7\1\4\10\1\1\7\10\1\1\2\10\1\1\u0123" +
		"\10\17\1\1\10\35\1\3\10\2\1\1\10\16\1\4\10\10\1\u018c\10\u0904\1\153\10\5\1\15\10" +
		"\3\1\11\10\7\1\12\10\u1766\1\125\10\1\1\107\10\1\1\2\10\2\1\1\10\2\1\2\10\2\1\4\10" +
		"\1\1\14\10\1\1\1\10\1\1\7\10\1\1\101\10\1\1\4\10\2\1\10\10\1\1\7\10\1\1\34\10\1\1" +
		"\4\10\1\1\5\10\1\1\1\10\3\1\7\10\1\1\u0154\10\2\1\31\10\1\1\31\10\1\1\37\10\1\1\31" +
		"\10\1\1\37\10\1\1\31\10\1\1\37\10\1\1\31\10\1\1\37\10\1\1\31\10\1\1\10\10\2\1\62" +
		"\64\u0700\1\37\10\6\1\6\10\u0105\1\76\10\222\1\55\10\12\1\7\10\2\1\12\64\4\1\1\10" +
		"\u0141\1\36\10\22\1\54\10\4\1\12\64\u01d6\1\34\10\4\1\12\64\u02e6\1\7\10\1\1\4\10" +
		"\1\1\2\10\1\1\17\10\1\1\305\10\73\1\104\10\7\1\1\10\4\1\12\64\u04a6\1\4\10\1\1\33" +
		"\10\1\1\2\10\1\1\1\10\2\1\1\10\1\1\12\10\1\1\4\10\1\1\1\10\1\1\1\10\6\1\1\10\4\1" +
		"\1\10\1\1\1\10\1\1\1\10\1\1\3\10\1\1\2\10\1\1\1\10\2\1\1\10\1\1\1\10\1\1\1\10\1\1" +
		"\1\10\1\1\1\10\1\1\2\10\1\1\1\10\2\1\4\10\1\1\7\10\1\1\4\10\1\1\4\10\1\1\1\10\1\1" +
		"\12\10\1\1\21\10\5\1\3\10\1\1\5\10\1\1\21\10\u0d34\1\12\64\u0406\1\ua6e0\10\40\1" +
		"\u103a\10\6\1\336\10\2\1\u1682\10\16\1\u1d31\10\u0c1f\1\u021e\10\u05e2\1\u134b\10" +
		"\5\1\u1060\10\1\1");

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

	private static final short tmBacktracking[] = {
		57, 35, 57, 33, 62, 37, 57, 42, 61, 44, 61, 46, 61, 50, 57, 70,
		57, 66, 57, 53, 57, 52, 58, 62, 58, 56, 58, 55, 64, 58, 60, 68,
		59, 70, 59, 35, 59, 73, 98, 78, 78, 84, 61, 83, 6, 113
	};

	private static final int tmFirstRule = -24;

	private static final int[] tmRuleSymbol = unpack_int(118,
		"\uffff\uffff\0\0\0\0\1\0\2\0\3\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\16" +
		"\0\17\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36" +
		"\0\37\0\40\0\41\0\42\0\43\0\44\0\45\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56" +
		"\0\57\0\60\0\61\0\62\0\63\0\64\0\65\0\66\0\67\0\67\0\67\0\67\0\70\0\70\0\70\0\70" +
		"\0\71\0\71\0\72\0\73\0\74\0\75\0\76\0\77\0\100\0\101\0\102\0\103\0\104\0\105\0\106" +
		"\0\107\0\110\0\111\0\112\0\113\0\114\0\115\0\116\0\117\0\120\0\121\0\122\0\123\0" +
		"\124\0\125\0\126\0\127\0\130\0\131\0\132\0\133\0\134\0\135\0\136\0\137\0\140\0\141" +
		"\0\142\0\143\0\144\0\145\0\146\0\147\0\150\0\151\0\152\0\153\0\154\0");

	private static final int tmClassesCount = 53;

	private static final short[] tmGoto = unpack_vc_short(6996,
		"\1\uffe7\1\uffe8\2\203\1\202\1\201\1\177\1\166\1\160\1\156\1\153\1\141\1\140\1\137" +
		"\1\135\1\132\1\131\1\126\1\121\1\113\1\63\4\40\1\37\1\36\1\32\1\30\1\22\1\21\1\20" +
		"\7\160\1\17\1\12\1\11\1\7\5\160\1\6\1\3\1\2\1\1\1\uffe8\65\uff94\65\uff9f\34\uff84" +
		"\1\5\24\uff84\1\4\3\uff84\65\uff8c\65\uff79\65\uffa0\34\uff83\1\10\30\uff83\65\uff78" +
		"\65\uff9d\57\uffe8\1\13\31\uffe8\5\14\7\uffe8\4\14\10\uffe8\2\14\1\uffe8\1\13\31" +
		"\uffe8\5\15\7\uffe8\4\15\10\uffe8\2\15\33\uffe8\5\16\7\uffe8\4\16\10\uffe8\2\16\33" +
		"\uffe8\5\160\7\uffe8\4\160\10\uffe8\2\160\7\uffe8\65\uff9e\65\uff73\65\uff93\34\uff97" +
		"\1\27\1\23\27\uff97\34\uff80\1\26\1\24\27\uff80\34\uff7f\1\25\30\uff7f\65\uff74\65" +
		"\uff75\65\uff8f\34\uff98\1\31\30\uff98\65\uff91\33\uff96\1\34\1\33\30\uff96\65\uff90" +
		"\34\uff81\1\35\30\uff81\65\uff76\65\uff9c\65\uff92\22\uffaf\1\53\1\uffaf\5\51\11" +
		"\uffaf\1\50\1\uffff\1\42\6\uffaf\1\ufffe\1\uffaf\1\50\7\uffaf\24\uffe8\5\51\22\uffe8" +
		"\1\41\11\uffe8\65\uffaf\17\uffe8\1\47\1\uffe8\1\47\2\uffe8\5\44\34\uffe8\24\uffaa" +
		"\5\44\11\uffaa\1\46\10\uffaa\1\ufffd\1\uffaa\1\46\7\uffaa\24\uffe8\5\44\22\uffe8" +
		"\1\45\11\uffe8\65\uffaa\24\uffe8\5\44\34\uffe8\65\uffa9\22\uffaf\1\53\1\uffaf\5\51" +
		"\11\uffaf\1\50\1\uffff\1\42\6\uffaf\1\ufffc\1\uffaf\1\50\7\uffaf\24\uffe8\5\51\22" +
		"\uffe8\1\52\11\uffe8\24\uffab\5\61\11\uffab\1\60\1\ufffb\11\uffab\1\60\7\uffab\17" +
		"\uffe8\1\57\1\uffe8\1\57\2\uffe8\5\55\34\uffe8\24\uffab\5\55\11\uffab\1\60\10\uffab" +
		"\1\ufffa\1\uffab\1\60\7\uffab\24\uffe8\5\55\22\uffe8\1\56\35\uffe8\5\55\34\uffe8" +
		"\111\uffab\5\61\11\uffab\1\60\1\ufffb\7\uffab\1\ufff9\1\uffab\1\60\7\uffab\24\uffe8" +
		"\5\61\22\uffe8\1\62\11\uffe8\22\uffaf\1\53\1\uffaf\4\110\1\ufff8\10\uffaf\1\ufff7" +
		"\1\50\1\uffff\1\42\1\uffaf\1\ufff6\4\uffaf\1\ufff5\1\ufff7\1\50\7\uffaf\24\uffe8" +
		"\4\110\1\106\22\uffe8\1\64\33\uffe8\1\101\1\uffe8\5\66\7\uffe8\4\66\10\uffe8\2\66" +
		"\7\uffe8\22\uffae\1\ufff4\1\uffae\5\66\7\uffae\4\66\1\75\1\ufff3\5\uffae\1\ufff2" +
		"\2\66\7\uffae\24\uffe8\5\66\7\uffe8\4\66\7\uffe8\1\67\2\66\26\uffe8\1\74\1\uffe8" +
		"\1\74\2\uffe8\5\71\34\uffe8\24\uffa8\5\71\11\uffa8\1\73\10\uffa8\1\ufff1\1\uffa8" +
		"\1\73\7\uffa8\24\uffe8\5\71\22\uffe8\1\72\11\uffe8\65\uffa8\24\uffe8\5\71\34\uffe8" +
		"\65\uffae\24\uffe8\5\77\7\uffe8\4\77\1\uffe8\1\70\6\uffe8\2\77\33\uffe8\5\77\7\uffe8" +
		"\4\77\1\uffe8\1\70\5\uffe8\1\100\2\77\33\uffe8\5\77\7\uffe8\4\77\7\uffe8\1\100\2" +
		"\77\33\uffe8\5\77\7\uffe8\4\77\10\uffe8\2\77\33\uffe8\2\103\37\uffe8\24\uffac\2\103" +
		"\16\uffac\1\105\6\uffac\1\ufff0\11\uffac\24\uffe8\2\103\25\uffe8\1\104\11\uffe8\65" +
		"\uffac\22\uffe8\1\53\1\uffe8\5\106\11\uffe8\1\50\1\43\7\uffe8\1\107\1\uffe8\1\50" +
		"\33\uffe8\5\106\22\uffe8\1\107\11\uffe8\22\uffad\1\53\1\uffad\4\110\1\uffef\11\uffad" +
		"\1\50\1\uffee\1\112\6\uffad\1\uffed\1\uffad\1\50\7\uffad\24\uffe8\4\110\1\106\22" +
		"\uffe8\1\111\11\uffe8\65\uffad\16\uff86\1\uffec\4\uff86\1\115\10\uff86\1\114\30\uff86" +
		"\65\uff7b\1\uffe4\2\115\2\uffe4\60\115\1\uffe8\15\116\1\117\46\116\1\uffe8\15\116" +
		"\1\117\4\116\1\120\41\116\65\uffe3\22\uff9a\1\uffeb\1\uff9a\5\122\34\uff9a\24\uffab" +
		"\5\122\11\uffab\1\60\1\ufffb\7\uffab\1\uffea\1\uffab\1\60\7\uffab\24\uffe8\5\122" +
		"\22\uffe8\1\123\33\uffe8\1\125\42\uffe8\65\uff99\21\uff88\1\130\12\uff88\1\127\30" +
		"\uff88\65\uff7d\65\uff8a\65\uff9b\17\uff89\1\134\14\uff89\1\133\30\uff89\65\uff7e" +
		"\65\uff8b\34\uff87\1\136\30\uff87\65\uff7c\65\uffa1\65\uffa2\1\uffe8\2\152\2\uffe8" +
		"\6\152\1\uffe8\34\152\1\142\14\152\7\uffe8\1\152\3\uffe8\1\152\10\uffe8\3\151\1\147" +
		"\20\uffe8\1\152\3\uffe8\3\152\1\143\31\uffe8\5\144\7\uffe8\4\144\10\uffe8\2\144\1" +
		"\uffe8\1\143\31\uffe8\5\145\7\uffe8\4\145\10\uffe8\2\145\33\uffe8\5\146\7\uffe8\4" +
		"\146\10\uffe8\2\146\33\uffe8\5\152\7\uffe8\4\152\10\uffe8\2\152\22\uffe8\1\150\10" +
		"\uffe8\4\152\35\uffe8\65\uffa5\13\uffe8\1\150\10\uffe8\4\147\50\uffe8\1\150\51\uffe8" +
		"\12\uff85\1\155\21\uff85\1\154\30\uff85\65\uff7a\65\uff8d\34\uff82\1\157\30\uff82" +
		"\65\uff77\10\uffe2\1\160\13\uffe2\5\160\7\uffe2\7\160\1\uffe2\1\uffe9\2\uffe2\5\160" +
		"\4\uffe2\1\160\57\uffe8\1\162\31\uffe8\5\163\7\uffe8\4\163\10\uffe8\2\163\1\uffe8" +
		"\1\162\31\uffe8\5\164\7\uffe8\4\164\10\uffe8\2\164\33\uffe8\5\165\7\uffe8\4\165\10" +
		"\uffe8\2\165\33\uffe8\5\160\7\uffe8\4\160\10\uffe8\2\160\10\uffe8\2\166\2\uffe8\2" +
		"\166\1\176\40\166\1\167\14\166\7\uffe8\1\166\3\uffe8\1\166\10\uffe8\3\175\1\174\20" +
		"\uffe8\1\166\3\uffe8\3\166\1\170\31\uffe8\5\171\7\uffe8\4\171\10\uffe8\2\171\1\uffe8" +
		"\1\170\31\uffe8\5\172\7\uffe8\4\172\10\uffe8\2\172\33\uffe8\5\173\7\uffe8\4\173\10" +
		"\uffe8\2\173\33\uffe8\5\166\7\uffe8\4\166\10\uffe8\2\166\10\uffe8\2\166\2\uffe8\2" +
		"\166\1\176\40\166\1\167\14\166\1\uffe8\2\166\2\uffe8\2\166\1\176\14\166\4\174\20" +
		"\166\1\167\14\166\65\uffa4\34\uff95\1\200\30\uff95\65\uff8e\65\uffe6\3\uffe5\1\203" +
		"\146\uffe5");

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
		if (chr >= 0 && chr < 205745) return tmCharClass[chr];
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
			case 3: // WhiteSpace: /[\r\n\t\f ]|\r\n/
				spaceToken = true;
				break;
			case 4: // EndOfLineComment: /\/\/[^\r\n]*/
				spaceToken = true;
				break;
			case 5: // TraditionalComment: /\/\*([^*]|\*+[^\/*])*\*+\//
				spaceToken = true;
				break;
			case 6:
				return createIdentifierToken(token, ruleIndex);
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<>();
	static {
		subTokensOfIdentifier.put("abstract", 7);
		subTokensOfIdentifier.put("assert", 8);
		subTokensOfIdentifier.put("boolean", 9);
		subTokensOfIdentifier.put("break", 10);
		subTokensOfIdentifier.put("byte", 11);
		subTokensOfIdentifier.put("case", 12);
		subTokensOfIdentifier.put("catch", 13);
		subTokensOfIdentifier.put("char", 14);
		subTokensOfIdentifier.put("class", 15);
		subTokensOfIdentifier.put("const", 16);
		subTokensOfIdentifier.put("continue", 17);
		subTokensOfIdentifier.put("default", 18);
		subTokensOfIdentifier.put("do", 19);
		subTokensOfIdentifier.put("double", 20);
		subTokensOfIdentifier.put("else", 21);
		subTokensOfIdentifier.put("enum", 22);
		subTokensOfIdentifier.put("extends", 23);
		subTokensOfIdentifier.put("final", 24);
		subTokensOfIdentifier.put("finally", 25);
		subTokensOfIdentifier.put("float", 26);
		subTokensOfIdentifier.put("for", 27);
		subTokensOfIdentifier.put("goto", 28);
		subTokensOfIdentifier.put("if", 29);
		subTokensOfIdentifier.put("implements", 30);
		subTokensOfIdentifier.put("import", 31);
		subTokensOfIdentifier.put("instanceof", 32);
		subTokensOfIdentifier.put("int", 33);
		subTokensOfIdentifier.put("interface", 34);
		subTokensOfIdentifier.put("long", 35);
		subTokensOfIdentifier.put("native", 36);
		subTokensOfIdentifier.put("new", 37);
		subTokensOfIdentifier.put("package", 38);
		subTokensOfIdentifier.put("private", 39);
		subTokensOfIdentifier.put("protected", 40);
		subTokensOfIdentifier.put("public", 41);
		subTokensOfIdentifier.put("return", 42);
		subTokensOfIdentifier.put("short", 43);
		subTokensOfIdentifier.put("static", 44);
		subTokensOfIdentifier.put("strictfp", 45);
		subTokensOfIdentifier.put("super", 46);
		subTokensOfIdentifier.put("switch", 47);
		subTokensOfIdentifier.put("synchronized", 48);
		subTokensOfIdentifier.put("this", 49);
		subTokensOfIdentifier.put("throw", 50);
		subTokensOfIdentifier.put("throws", 51);
		subTokensOfIdentifier.put("transient", 52);
		subTokensOfIdentifier.put("try", 53);
		subTokensOfIdentifier.put("void", 54);
		subTokensOfIdentifier.put("volatile", 55);
		subTokensOfIdentifier.put("while", 56);
		subTokensOfIdentifier.put("true", 65);
		subTokensOfIdentifier.put("false", 66);
		subTokensOfIdentifier.put("null", 69);
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

}
