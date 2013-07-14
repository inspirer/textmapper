package org.textmapper.templates.java;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class JavaLexer {

	public static class LapgSymbol {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int WhiteSpace = 1;
		public static final int EndOfLineComment = 2;
		public static final int TraditionalComment = 3;
		public static final int Identifier = 4;
		public static final int kw_abstract = 5;
		public static final int kw_assert = 6;
		public static final int kw_boolean = 7;
		public static final int kw_break = 8;
		public static final int kw_byte = 9;
		public static final int kw_case = 10;
		public static final int kw_catch = 11;
		public static final int kw_char = 12;
		public static final int kw_class = 13;
		public static final int kw_const = 14;
		public static final int kw_continue = 15;
		public static final int kw_default = 16;
		public static final int kw_do = 17;
		public static final int kw_double = 18;
		public static final int kw_else = 19;
		public static final int kw_enum = 20;
		public static final int kw_extends = 21;
		public static final int kw_final = 22;
		public static final int kw_finally = 23;
		public static final int kw_float = 24;
		public static final int kw_for = 25;
		public static final int kw_goto = 26;
		public static final int kw_if = 27;
		public static final int kw_implements = 28;
		public static final int kw_import = 29;
		public static final int kw_instanceof = 30;
		public static final int kw_int = 31;
		public static final int kw_interface = 32;
		public static final int kw_long = 33;
		public static final int kw_native = 34;
		public static final int kw_new = 35;
		public static final int kw_package = 36;
		public static final int kw_private = 37;
		public static final int kw_protected = 38;
		public static final int kw_public = 39;
		public static final int kw_return = 40;
		public static final int kw_short = 41;
		public static final int kw_static = 42;
		public static final int kw_strictfp = 43;
		public static final int kw_super = 44;
		public static final int kw_switch = 45;
		public static final int kw_synchronized = 46;
		public static final int kw_this = 47;
		public static final int kw_throw = 48;
		public static final int kw_throws = 49;
		public static final int kw_transient = 50;
		public static final int kw_try = 51;
		public static final int kw_void = 52;
		public static final int kw_volatile = 53;
		public static final int kw_while = 54;
		public static final int IntegerLiteral = 55;
		public static final int FloatingPointLiteral = 56;
		public static final int BooleanLiteral = 57;
		public static final int CharacterLiteral = 58;
		public static final int StringLiteral = 59;
		public static final int NullLiteral = 60;
		public static final int LPAREN = 61;
		public static final int RPAREN = 62;
		public static final int LCURLY = 63;
		public static final int RCURLY = 64;
		public static final int LSQUARE = 65;
		public static final int RSQUARE = 66;
		public static final int SEMICOLON = 67;
		public static final int COMMA = 68;
		public static final int DOT = 69;
		public static final int DOTDOTDOT = 70;
		public static final int EQUAL = 71;
		public static final int GREATER = 72;
		public static final int LESS = 73;
		public static final int EXCLAMATION = 74;
		public static final int TILDE = 75;
		public static final int QUESTIONMARK = 76;
		public static final int COLON = 77;
		public static final int EQUALEQUAL = 78;
		public static final int LESSEQUAL = 79;
		public static final int GREATEREQUAL = 80;
		public static final int EXCLAMATIONEQUAL = 81;
		public static final int AMPERSANDAMPERSAND = 82;
		public static final int OROR = 83;
		public static final int PLUSPLUS = 84;
		public static final int MINUSMINUS = 85;
		public static final int PLUS = 86;
		public static final int MINUS = 87;
		public static final int MULT = 88;
		public static final int SLASH = 89;
		public static final int AMPERSAND = 90;
		public static final int OR = 91;
		public static final int XOR = 92;
		public static final int PERCENT = 93;
		public static final int LESSLESS = 94;
		public static final int GREATERGREATER = 95;
		public static final int GREATERGREATERGREATER = 96;
		public static final int PLUSEQUAL = 97;
		public static final int MINUSEQUAL = 98;
		public static final int MULTEQUAL = 99;
		public static final int SLASHEQUAL = 100;
		public static final int AMPERSANDEQUAL = 101;
		public static final int OREQUAL = 102;
		public static final int XOREQUAL = 103;
		public static final int PERCENTEQUAL = 104;
		public static final int LESSLESSEQUAL = 105;
		public static final int GREATERGREATEREQUAL = 106;
		public static final int GREATERGREATERGREATEREQUAL = 107;
		public static final int ATSIGN = 108;
	}

	public interface ErrorReporter {
		void error(int start, int end, int line, String s);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen, l, tokenStart;
	private char chr;

	private int state;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;

	public JavaLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.stream = stream;
		this.state = 0;
		datalen = stream.read(data);
		l = 0;
		tokenStart = -1;
		chr = l < datalen ? data[l++] : 0;
	}

	protected void advance() throws IOException {
		if (chr == 0) return;
		currOffset++;
		if (chr == '\n') {
			currLine++;
		}
		if (l >= datalen) {
			if (tokenStart >= 0) {
				token.append(data, tokenStart, l - tokenStart);
				tokenStart = 0;
			}
			l = 0;
			datalen = stream.read(data);
		}
		chr = l < datalen ? data[l++] : 0;
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

	private static final char[] tmCharClass = unpack_vc_char(262144,
		"\1\0\10\1\1\50\1\6\1\1\1\50\1\5\14\1\1\4\5\1\1\50\1\31\1\15\1\1\1\44\1\42\1\35\1" +
		"\14\1\16\1\17\1\10\1\37\1\25\1\40\1\13\1\7\1\11\1\55\2\64\4\53\2\45\1\34\1\24\1\30" +
		"\1\26\1\27\1\33\1\43\1\46\1\54\1\46\1\57\1\56\1\57\5\44\1\51\3\44\1\60\7\44\1\52" +
		"\2\44\1\22\1\2\1\23\1\41\1\12\1\1\1\46\1\62\1\46\1\57\1\56\1\63\5\44\1\51\1\44\1" +
		"\61\1\44\1\60\1\44\1\61\1\44\1\61\1\3\2\44\1\52\2\44\1\20\1\36\1\21\1\32\53\1\1\44" +
		"\12\1\1\44\4\1\1\44\5\1\27\44\1\1\37\44\1\1\u01ca\44\4\1\14\44\16\1\5\44\7\1\1\44" +
		"\1\1\1\44\201\1\5\44\1\1\2\44\2\1\4\44\10\1\1\44\1\1\3\44\1\1\1\44\1\1\24\44\1\1" +
		"\123\44\1\1\213\44\10\1\236\44\11\1\46\44\2\1\1\44\7\1\47\44\110\1\33\44\5\1\3\44" +
		"\55\1\53\44\25\1\12\47\4\1\2\44\1\1\143\44\1\1\1\44\17\1\2\44\7\1\2\44\12\47\3\44" +
		"\2\1\1\44\20\1\1\44\1\1\36\44\35\1\131\44\13\1\1\44\16\1\12\47\41\44\11\1\2\44\4" +
		"\1\1\44\5\1\26\44\4\1\1\44\11\1\1\44\3\1\1\44\27\1\31\44\107\1\1\44\1\1\13\44\127" +
		"\1\66\44\3\1\1\44\22\1\1\44\7\1\12\44\4\1\12\47\1\1\7\44\1\1\7\44\5\1\10\44\2\1\2" +
		"\44\2\1\26\44\1\1\7\44\1\1\1\44\3\1\4\44\3\1\1\44\20\1\1\44\15\1\2\44\1\1\3\44\4" +
		"\1\12\47\2\44\23\1\6\44\4\1\2\44\2\1\26\44\1\1\7\44\1\1\2\44\1\1\2\44\1\1\2\44\37" +
		"\1\4\44\1\1\1\44\7\1\12\47\2\1\3\44\20\1\11\44\1\1\3\44\1\1\26\44\1\1\7\44\1\1\2" +
		"\44\1\1\5\44\3\1\1\44\22\1\1\44\17\1\2\44\4\1\12\47\25\1\10\44\2\1\2\44\2\1\26\44" +
		"\1\1\7\44\1\1\2\44\1\1\5\44\3\1\1\44\36\1\2\44\1\1\3\44\4\1\12\47\1\1\1\44\21\1\1" +
		"\44\1\1\6\44\3\1\3\44\1\1\4\44\3\1\2\44\1\1\1\44\1\1\2\44\3\1\2\44\3\1\3\44\3\1\14" +
		"\44\26\1\1\44\25\1\12\47\25\1\10\44\1\1\3\44\1\1\27\44\1\1\12\44\1\1\5\44\3\1\1\44" +
		"\32\1\2\44\6\1\2\44\4\1\12\47\25\1\10\44\1\1\3\44\1\1\27\44\1\1\12\44\1\1\5\44\3" +
		"\1\1\44\40\1\1\44\1\1\2\44\4\1\12\47\1\1\2\44\22\1\10\44\1\1\3\44\1\1\51\44\2\1\1" +
		"\44\20\1\1\44\21\1\2\44\4\1\12\47\12\1\6\44\5\1\22\44\3\1\30\44\1\1\11\44\1\1\1\44" +
		"\2\1\7\44\72\1\60\44\1\1\2\44\14\1\7\44\11\1\12\47\47\1\2\44\1\1\1\44\2\1\2\44\1" +
		"\1\1\44\2\1\1\44\6\1\4\44\1\1\7\44\1\1\3\44\1\1\1\44\1\1\1\44\2\1\2\44\1\1\4\44\1" +
		"\1\2\44\11\1\1\44\2\1\5\44\1\1\1\44\11\1\12\47\2\1\4\44\40\1\1\44\37\1\12\47\26\1" +
		"\10\44\1\1\44\44\33\1\5\44\163\1\53\44\24\1\1\44\12\47\6\1\6\44\4\1\4\44\3\1\1\44" +
		"\3\1\2\44\7\1\3\44\4\1\15\44\14\1\1\44\1\1\12\47\6\1\46\44\1\1\1\44\5\1\1\44\2\1" +
		"\53\44\1\1\u014d\44\1\1\4\44\2\1\7\44\1\1\1\44\1\1\4\44\2\1\51\44\1\1\4\44\2\1\41" +
		"\44\1\1\4\44\2\1\7\44\1\1\1\44\1\1\4\44\2\1\17\44\1\1\71\44\1\1\4\44\2\1\103\44\45" +
		"\1\20\44\20\1\125\44\14\1\u026c\44\2\1\21\44\1\1\32\44\5\1\113\44\3\1\3\44\17\1\15" +
		"\44\1\1\4\44\16\1\22\44\16\1\22\44\16\1\15\44\1\1\3\44\17\1\64\44\43\1\1\44\4\1\1" +
		"\44\3\1\12\47\46\1\12\47\6\1\130\44\10\1\51\44\1\1\1\44\5\1\106\44\12\1\35\44\51" +
		"\1\12\47\36\44\2\1\5\44\13\1\54\44\25\1\7\44\10\1\12\47\46\1\27\44\11\1\65\44\53" +
		"\1\12\47\6\1\12\47\15\1\1\44\135\1\57\44\21\1\7\44\4\1\12\47\51\1\36\44\15\1\2\44" +
		"\12\47\54\44\32\1\44\44\34\1\12\47\3\1\3\44\12\47\44\44\153\1\4\44\1\1\4\44\3\1\2" +
		"\44\11\1\300\44\100\1\u0116\44\2\1\6\44\2\1\46\44\2\1\6\44\2\1\10\44\1\1\1\44\1\1" +
		"\1\44\1\1\1\44\1\1\37\44\2\1\65\44\1\1\7\44\1\1\1\44\3\1\3\44\1\1\7\44\3\1\4\44\2" +
		"\1\6\44\4\1\15\44\5\1\3\44\1\1\7\44\164\1\1\44\15\1\1\44\20\1\15\44\145\1\1\44\4" +
		"\1\1\44\2\1\12\44\1\1\1\44\3\1\5\44\6\1\1\44\1\1\1\44\1\1\1\44\1\1\4\44\1\1\13\44" +
		"\2\1\4\44\5\1\5\44\4\1\1\44\21\1\51\44\u0a77\1\57\44\1\1\57\44\1\1\205\44\6\1\4\44" +
		"\3\1\2\44\14\1\46\44\1\1\1\44\5\1\1\44\2\1\70\44\7\1\1\44\20\1\27\44\11\1\7\44\1" +
		"\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\120\1\1\44\u01d5\1" +
		"\3\44\31\1\11\44\7\1\5\44\2\1\5\44\4\1\126\44\6\1\3\44\1\1\132\44\1\1\4\44\5\1\51" +
		"\44\3\1\136\44\21\1\33\44\65\1\20\44\u0200\1\u19b6\44\112\1\u51cd\44\63\1\u048d\44" +
		"\103\1\56\44\2\1\u010d\44\3\1\20\44\12\47\2\44\24\1\57\44\20\1\31\44\10\1\120\44" +
		"\47\1\11\44\2\1\147\44\2\1\4\44\1\1\4\44\14\1\13\44\115\1\12\44\1\1\3\44\1\1\4\44" +
		"\1\1\27\44\35\1\64\44\16\1\62\44\34\1\12\47\30\1\6\44\3\1\1\44\4\1\12\47\34\44\12" +
		"\1\27\44\31\1\35\44\7\1\57\44\34\1\1\44\12\47\46\1\51\44\27\1\3\44\1\1\10\44\4\1" +
		"\12\47\6\1\27\44\3\1\1\44\5\1\60\44\1\1\1\44\3\1\2\44\2\1\5\44\2\1\1\44\1\1\1\44" +
		"\30\1\3\44\2\1\13\44\7\1\3\44\14\1\6\44\2\1\6\44\2\1\6\44\11\1\7\44\1\1\7\44\221" +
		"\1\43\44\15\1\12\47\6\1\u2ba4\44\14\1\27\44\4\1\61\44\u2104\1\u016e\44\2\1\152\44" +
		"\46\1\7\44\14\1\5\44\5\1\1\44\1\1\12\44\1\1\15\44\1\1\5\44\1\1\1\44\1\1\2\44\1\1" +
		"\2\44\1\1\154\44\41\1\u016b\44\22\1\100\44\2\1\66\44\50\1\14\44\164\1\5\44\1\1\207" +
		"\44\23\1\12\47\7\1\32\44\6\1\32\44\13\1\131\44\3\1\6\44\2\1\6\44\2\1\6\44\2\1\3\44" +
		"\43\1\14\44\1\1\32\44\1\1\23\44\1\1\2\44\1\1\17\44\2\1\16\44\42\1\173\44\105\1\65" +
		"\44\u010b\1\35\44\3\1\61\44\57\1\37\44\21\1\33\44\65\1\36\44\2\1\44\44\4\1\10\44" +
		"\1\1\5\44\52\1\236\44\2\1\12\47\u0356\1\6\44\2\1\1\44\1\1\54\44\1\1\2\44\3\1\1\44" +
		"\2\1\27\44\252\1\26\44\12\1\32\44\106\1\70\44\6\1\2\44\100\1\1\44\17\1\4\44\1\1\3" +
		"\44\1\1\33\44\54\1\35\44\203\1\66\44\12\1\26\44\12\1\23\44\215\1\111\44\u03ba\1\65" +
		"\44\56\1\12\47\23\1\55\44\40\1\31\44\7\1\12\47\11\1\44\44\17\1\12\47\103\1\60\44" +
		"\16\1\4\44\13\1\12\47\u04a6\1\53\44\25\1\12\47\u0936\1\u036f\44\221\1\143\44\u0b9d" +
		"\1\u042f\44\u33d1\1\u0239\44\u04c7\1\105\44\13\1\1\44\102\1\15\44\u4060\1\2\44\u23fe" +
		"\1\125\44\1\1\107\44\1\1\2\44\2\1\1\44\2\1\2\44\2\1\4\44\1\1\14\44\1\1\1\44\1\1\7" +
		"\44\1\1\101\44\1\1\4\44\2\1\10\44\1\1\7\44\1\1\34\44\1\1\4\44\1\1\5\44\1\1\1\44\3" +
		"\1\7\44\1\1\u0154\44\2\1\31\44\1\1\31\44\1\1\37\44\1\1\31\44\1\1\37\44\1\1\31\44" +
		"\1\1\37\44\1\1\31\44\1\1\37\44\1\1\31\44\1\1\10\44\2\1\62\47\u1600\1\4\44\1\1\33" +
		"\44\1\1\2\44\1\1\1\44\2\1\1\44\1\1\12\44\1\1\4\44\1\1\1\44\1\1\1\44\6\1\1\44\4\1" +
		"\1\44\1\1\1\44\1\1\1\44\1\1\3\44\1\1\2\44\1\1\1\44\2\1\1\44\1\1\1\44\1\1\1\44\1\1" +
		"\1\44\1\1\1\44\1\1\2\44\1\1\1\44\2\1\4\44\1\1\7\44\1\1\4\44\1\1\4\44\1\1\1\44\1\1" +
		"\12\44\1\1\21\44\5\1\3\44\1\1\5\44\1\1\21\44\u1144\1\ua6d7\44\51\1\u1035\44\13\1" +
		"\336\44\u3fe2\1\u021e\44\uffff\1\u05e3\1");

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

	private static final short[] tmRuleSymbol = unpack_short(116,
		"\4\0\1\2\3\5\6\7\10\11\12\13\14\15\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35" +
		"\36\37\40\41\42\43\44\45\46\47\50\51\52\53\54\55\56\57\60\61\62\63\64\65\66\67\67" +
		"\67\67\70\70\70\70\71\71\72\73\74\75\76\77\100\101\102\103\104\105\106\107\110\111" +
		"\112\113\114\115\116\117\120\121\122\123\124\125\126\127\130\131\132\133\134\135" +
		"\136\137\140\141\142\143\144\145\146\147\150\151\152\153\154");

	private static final int tmClassesCount = 53;

	private static final short[] tmGoto = unpack_vc_short(6996,
		"\1\ufffe\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\2\1\11\1\12\1\13\1\14\1\15\1" +
		"\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1" +
		"\36\1\37\1\40\1\41\1\2\1\42\1\2\1\uffff\1\5\2\2\1\42\1\2\1\42\6\2\1\42\3\uffff\1" +
		"\43\61\uffff\2\ufffd\1\44\1\2\5\ufffd\2\2\31\ufffd\4\2\1\ufffd\14\2\65\ufffc\6\ufffb" +
		"\1\5\143\ufffb\7\uff9d\1\45\1\46\15\uff9d\1\47\36\uff9d\26\uff9e\1\50\36\uff9e\11" +
		"\uffc6\1\51\1\52\1\53\31\uffc6\1\54\3\uffc6\1\55\1\56\1\51\1\57\1\51\1\60\1\61\2" +
		"\uffc6\1\57\1\61\1\51\11\uffb1\1\62\1\uffb1\1\63\31\uffb1\1\62\5\uffb1\1\62\1\uffb1" +
		"\1\62\6\uffb1\1\62\1\uffff\1\64\1\65\2\64\2\uffff\5\64\1\uffff\50\64\1\uffff\1\13" +
		"\1\66\2\13\2\uffff\6\13\1\67\47\13\65\uffb9\65\uffb8\65\uffb7\65\uffb6\65\uffb5\65" +
		"\uffb4\65\uffb3\65\uffb2\26\uffaf\1\70\36\uffaf\26\uffae\1\71\1\72\35\uffae\26\uffad" +
		"\1\73\1\uffad\1\74\34\uffad\26\uffac\1\75\36\uffac\65\uffab\65\uffaa\65\uffa9\26" +
		"\uff9c\1\76\6\uff9c\1\77\27\uff9c\26\uff9b\1\100\7\uff9b\1\101\26\uff9b\26\uffa0" +
		"\1\102\10\uffa0\1\103\25\uffa0\26\uff9f\1\104\11\uff9f\1\105\24\uff9f\26\uff9a\1" +
		"\106\36\uff9a\26\uff99\1\107\36\uff99\65\uff8a\11\uffc6\1\110\1\111\1\53\31\uffc6" +
		"\1\110\3\uffc6\1\55\1\uffc6\1\110\1\uffc6\1\110\1\60\1\61\3\uffc6\1\61\1\110\3\uffff" +
		"\1\43\5\uffff\1\112\33\uffff\2\112\4\uffff\5\112\2\uffff\3\112\3\uffff\1\113\61\uffff" +
		"\1\ufffa\4\45\2\ufffa\56\45\1\uffff\7\46\1\114\54\46\65\uff92\65\uff93\11\uffc4\1" +
		"\51\1\115\1\53\31\uffc4\1\54\3\uffc4\1\116\1\uffc4\1\51\1\uffc4\1\51\1\60\1\61\3" +
		"\uffc4\1\61\1\51\11\uffff\1\51\1\52\32\uffff\1\54\5\uffff\1\51\1\uffff\1\51\6\uffff" +
		"\1\51\11\uffc2\1\117\33\uffc2\1\117\5\uffc2\1\117\1\uffc2\1\117\1\120\1\121\3\uffc2" +
		"\1\121\1\117\11\uffff\1\54\1\122\1\53\31\uffff\1\54\5\uffff\1\54\1\uffff\1\54\1\60" +
		"\1\61\3\uffff\1\61\1\54\65\uffc6\11\uffff\1\123\1\uffff\1\124\31\uffff\2\123\4\uffff" +
		"\5\123\2\uffff\3\123\11\uffff\1\125\43\uffff\1\125\20\uffff\1\126\25\uffff\2\127" +
		"\4\uffff\1\126\5\uffff\1\126\1\uffff\1\126\6\uffff\1\126\65\uffc0\11\uffc2\1\62\1" +
		"\130\32\uffc2\1\62\5\uffc2\1\62\1\uffc2\1\62\1\120\1\121\3\uffc2\1\121\1\62\13\uffff" +
		"\1\131\65\uffff\1\132\52\uffff\1\64\1\133\5\uffff\1\134\2\uffff\2\64\35\uffff\1\135" +
		"\1\uffff\1\134\3\uffff\3\64\1\134\2\uffff\1\13\1\136\5\uffff\1\137\2\uffff\2\13\35" +
		"\uffff\1\140\1\uffff\1\137\3\uffff\3\13\1\137\65\uffbb\65\uffa8\65\uffa6\26\uff97" +
		"\1\141\1\142\35\uff97\65\uffa7\26\uff98\1\143\36\uff98\65\uffa5\65\uff91\65\uffa4" +
		"\65\uff90\65\uffa3\65\uff95\65\uffa2\65\uff94\65\uffa1\65\uff8f\65\uff8e\11\uffc6" +
		"\1\110\1\144\1\53\31\uffc6\1\110\3\uffc6\1\55\1\uffc6\1\110\1\uffc6\1\110\1\60\1" +
		"\61\3\uffc6\1\61\1\110\11\uffff\1\110\1\111\32\uffff\1\110\5\uffff\1\110\1\uffff" +
		"\1\110\6\uffff\1\110\11\uffff\1\145\33\uffff\2\145\4\uffff\5\145\2\uffff\3\145\3" +
		"\uffff\1\113\5\uffff\1\146\33\uffff\2\146\4\uffff\5\146\2\uffff\3\146\1\uffff\6\46" +
		"\1\147\1\114\54\46\11\uffff\1\51\1\115\32\uffff\1\54\5\uffff\1\51\1\uffff\1\51\6" +
		"\uffff\1\51\65\uffc4\11\uffc2\1\117\1\150\32\uffc2\1\117\5\uffc2\1\117\1\uffc2\1" +
		"\117\1\120\1\121\3\uffc2\1\121\1\117\11\uffff\1\151\25\uffff\2\152\4\uffff\1\151" +
		"\5\uffff\1\151\1\uffff\1\151\6\uffff\1\151\65\uffc2\11\uffff\1\54\1\122\32\uffff" +
		"\1\54\5\uffff\1\54\1\uffff\1\54\6\uffff\1\54\11\uffc5\1\123\1\153\1\154\31\uffc5" +
		"\2\123\2\uffc5\1\155\1\uffc5\5\123\1\156\1\uffc5\3\123\11\uffff\1\157\33\uffff\2" +
		"\157\4\uffff\5\157\2\uffff\3\157\11\uffc3\1\125\1\160\36\uffc3\1\161\3\uffc3\1\125" +
		"\7\uffc3\11\uffc1\1\126\1\162\32\uffc1\1\126\5\uffc1\1\126\1\uffc1\1\126\1\uffc1" +
		"\1\163\3\uffc1\1\163\1\126\11\uffff\1\126\33\uffff\1\126\5\uffff\1\126\1\uffff\1" +
		"\126\6\uffff\1\126\11\uffff\1\62\1\130\32\uffff\1\62\5\uffff\1\62\1\uffff\1\62\6" +
		"\uffff\1\62\65\uffb0\65\uffbc\3\uffff\1\133\5\uffff\1\164\33\uffff\2\164\4\uffff" +
		"\5\164\2\uffff\3\164\11\uffff\1\135\2\uffff\1\132\36\uffff\1\135\1\uffff\1\135\6" +
		"\uffff\1\135\11\uffff\1\64\2\uffff\1\132\36\uffff\1\64\1\uffff\1\64\6\uffff\1\64" +
		"\3\uffff\1\136\5\uffff\1\165\33\uffff\2\165\4\uffff\5\165\2\uffff\3\165\1\uffff\1" +
		"\13\1\66\2\13\2\uffff\2\13\1\140\3\13\1\67\35\13\1\140\1\13\1\140\6\13\1\140\1\uffff" +
		"\1\13\1\66\2\13\2\uffff\6\13\1\67\47\13\65\uff8c\26\uff96\1\166\36\uff96\65\uff8d" +
		"\11\uffff\1\110\1\144\32\uffff\1\110\5\uffff\1\110\1\uffff\1\110\6\uffff\1\110\11" +
		"\uffff\1\167\33\uffff\2\167\4\uffff\5\167\2\uffff\3\167\11\uffff\1\170\33\uffff\2" +
		"\170\4\uffff\5\170\2\uffff\3\170\65\ufff9\11\uffff\1\117\1\150\32\uffff\1\117\5\uffff" +
		"\1\117\1\uffff\1\117\6\uffff\1\117\11\uffc2\1\151\1\171\32\uffc2\1\151\5\uffc2\1" +
		"\151\1\uffc2\1\151\1\uffc2\1\121\3\uffc2\1\121\1\151\11\uffff\1\151\33\uffff\1\151" +
		"\5\uffff\1\151\1\uffff\1\151\6\uffff\1\151\11\uffff\1\123\1\153\32\uffff\2\123\4" +
		"\uffff\5\123\2\uffff\3\123\11\uffff\1\157\33\uffff\2\157\4\uffff\5\157\1\156\1\uffff" +
		"\3\157\65\uffc5\11\uffff\1\172\25\uffff\2\173\4\uffff\1\172\5\uffff\1\172\1\uffff" +
		"\1\172\6\uffff\1\172\11\uffff\1\157\1\174\32\uffff\2\157\4\uffff\5\157\1\156\1\uffff" +
		"\3\157\11\uffff\1\125\1\160\42\uffff\1\125\7\uffff\65\uffc3\11\uffff\1\126\1\162" +
		"\32\uffff\1\126\5\uffff\1\126\1\uffff\1\126\6\uffff\1\126\65\uffc1\11\uffff\1\175" +
		"\33\uffff\2\175\4\uffff\5\175\2\uffff\3\175\11\uffff\1\176\33\uffff\2\176\4\uffff" +
		"\5\176\2\uffff\3\176\65\uff8b\11\uffff\1\2\33\uffff\2\2\4\uffff\5\2\2\uffff\3\2\11" +
		"\uffff\1\177\33\uffff\2\177\4\uffff\5\177\2\uffff\3\177\11\uffff\1\151\1\171\32\uffff" +
		"\1\151\5\uffff\1\151\1\uffff\1\151\6\uffff\1\151\11\uffbf\1\172\1\200\32\uffbf\1" +
		"\172\5\uffbf\1\172\1\uffbf\1\172\1\uffbf\1\201\3\uffbf\1\201\1\172\11\uffff\1\172" +
		"\33\uffff\1\172\5\uffff\1\172\1\uffff\1\172\6\uffff\1\172\11\uffff\1\157\1\174\32" +
		"\uffff\2\157\4\uffff\5\157\2\uffff\3\157\11\uffff\1\202\33\uffff\2\202\4\uffff\5" +
		"\202\2\uffff\3\202\11\uffff\1\203\33\uffff\2\203\4\uffff\5\203\2\uffff\3\203\11\uffff" +
		"\1\2\33\uffff\2\2\4\uffff\5\2\2\uffff\3\2\11\uffff\1\172\1\200\32\uffff\1\172\5\uffff" +
		"\1\172\1\uffff\1\172\6\uffff\1\172\65\uffbf\11\uffff\1\64\33\uffff\2\64\4\uffff\5" +
		"\64\2\uffff\3\64\11\uffff\1\13\33\uffff\2\13\4\uffff\5\13\2\uffff\3\13");

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
		if (chr >= 0 && chr < 262144) {
			return tmCharClass[chr];
		}
		return 1;
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
			tokenStart = l - 1;

			for (state = this.state; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.symbol = 0;
					lapg_n.value = null;
					reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, "Unexpected end of input reached");
					tokenStart = -1;
					return lapg_n;
				}
				if (state >= -1 && chr != 0) {
					currOffset++;
					if (chr == '\n') {
						currLine++;
					}
					if (l >= datalen) {
						token.append(data, tokenStart, l - tokenStart);
						tokenStart = l = 0;
						datalen = stream.read(data);
					}
					chr = l < datalen ? data[l++] : 0;
				}
			}
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				if (l - 1 > tokenStart) {
					token.append(data, tokenStart, l - 1 - tokenStart);
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line, MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.symbol = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.symbol = 0;
				lapg_n.value = null;
				tokenStart = -1;
				return lapg_n;
			}

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.symbol = tmRuleSymbol[-state - 3];
			lapg_n.value = null;

		} while (lapg_n.symbol == -1 || !createToken(lapg_n, -state - 3));
		tokenStart = -1;
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 0:
				return createIdentifierToken(lapg_n, ruleIndex);
			case 2: // WhiteSpace: /[\r\n\t\f ]|\r\n/
				spaceToken = true;
				break;
			case 3: // EndOfLineComment: /\/\/[^\r\n]*/
				spaceToken = true;
				break;
			case 4: // TraditionalComment: /\/\*([^*]|\*+[^\/*])*\*+\//
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<String,Integer>();
	static {
		subTokensOfIdentifier.put("abstract", 5);
		subTokensOfIdentifier.put("assert", 6);
		subTokensOfIdentifier.put("boolean", 7);
		subTokensOfIdentifier.put("break", 8);
		subTokensOfIdentifier.put("byte", 9);
		subTokensOfIdentifier.put("case", 10);
		subTokensOfIdentifier.put("catch", 11);
		subTokensOfIdentifier.put("char", 12);
		subTokensOfIdentifier.put("class", 13);
		subTokensOfIdentifier.put("const", 14);
		subTokensOfIdentifier.put("continue", 15);
		subTokensOfIdentifier.put("default", 16);
		subTokensOfIdentifier.put("do", 17);
		subTokensOfIdentifier.put("double", 18);
		subTokensOfIdentifier.put("else", 19);
		subTokensOfIdentifier.put("enum", 20);
		subTokensOfIdentifier.put("extends", 21);
		subTokensOfIdentifier.put("final", 22);
		subTokensOfIdentifier.put("finally", 23);
		subTokensOfIdentifier.put("float", 24);
		subTokensOfIdentifier.put("for", 25);
		subTokensOfIdentifier.put("goto", 26);
		subTokensOfIdentifier.put("if", 27);
		subTokensOfIdentifier.put("implements", 28);
		subTokensOfIdentifier.put("import", 29);
		subTokensOfIdentifier.put("instanceof", 30);
		subTokensOfIdentifier.put("int", 31);
		subTokensOfIdentifier.put("interface", 32);
		subTokensOfIdentifier.put("long", 33);
		subTokensOfIdentifier.put("native", 34);
		subTokensOfIdentifier.put("new", 35);
		subTokensOfIdentifier.put("package", 36);
		subTokensOfIdentifier.put("private", 37);
		subTokensOfIdentifier.put("protected", 38);
		subTokensOfIdentifier.put("public", 39);
		subTokensOfIdentifier.put("return", 40);
		subTokensOfIdentifier.put("short", 41);
		subTokensOfIdentifier.put("static", 42);
		subTokensOfIdentifier.put("strictfp", 43);
		subTokensOfIdentifier.put("super", 44);
		subTokensOfIdentifier.put("switch", 45);
		subTokensOfIdentifier.put("synchronized", 46);
		subTokensOfIdentifier.put("this", 47);
		subTokensOfIdentifier.put("throw", 48);
		subTokensOfIdentifier.put("throws", 49);
		subTokensOfIdentifier.put("transient", 50);
		subTokensOfIdentifier.put("try", 51);
		subTokensOfIdentifier.put("void", 52);
		subTokensOfIdentifier.put("volatile", 53);
		subTokensOfIdentifier.put("while", 54);
		subTokensOfIdentifier.put("true", 63);
		subTokensOfIdentifier.put("false", 64);
		subTokensOfIdentifier.put("null", 67);
	}

	protected boolean createIdentifierToken(LapgSymbol lapg_n, int ruleIndex) {
		Integer replacement = subTokensOfIdentifier.get(current());
		if (replacement != null) {
			ruleIndex = replacement;
			lapg_n.symbol = tmRuleSymbol[ruleIndex];
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
