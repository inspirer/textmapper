package org.textmapper.tool.importer;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class BisonLexer {

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
		public static final int bracedCode = 1;
		public static final int predicate = 2;
		public static final int prologue = 3;
		public static final int tag = 4;
		public static final int epilogue = 5;
	}

	public interface Tokens {
		public static final int Unavailable_ = -1;
		public static final int eoi = 0;
		public static final int ID_COLON = 1;
		public static final int ID = 2;
		public static final int skip = 3;
		public static final int INT = 4;
		public static final int CHAR = 5;
		public static final int STRING = 6;
		public static final int LessMultGreater = 7;
		public static final int LessGreater = 8;
		public static final int PercentPercent = 9;
		public static final int Or = 10;
		public static final int Semicolon = 11;
		public static final int Lsquare = 12;
		public static final int Rsquare = 13;
		public static final int skip_comment = 14;
		public static final int skip_ml_comment = 15;
		public static final int Percenttoken = 16;
		public static final int Percentnterm = 17;
		public static final int Percenttype = 18;
		public static final int Percentdestructor = 19;
		public static final int Percentprinter = 20;
		public static final int Percentleft = 21;
		public static final int Percentright = 22;
		public static final int Percentnonassoc = 23;
		public static final int Percentprecedence = 24;
		public static final int Percentprec = 25;
		public static final int Percentdprec = 26;
		public static final int Percentmerge = 27;
		public static final int Percentcode = 28;
		public static final int PercentdefaultMinusprec = 29;
		public static final int Percentdefine = 30;
		public static final int Percentdefines = 31;
		public static final int Percentempty = 32;
		public static final int PercenterrorMinusverbose = 33;
		public static final int Percentexpect = 34;
		public static final int PercentexpectMinusrr = 35;
		public static final int PercentLessflagGreater = 36;
		public static final int PercentfileMinusprefix = 37;
		public static final int PercentglrMinusparser = 38;
		public static final int PercentinitialMinusaction = 39;
		public static final int Percentlanguage = 40;
		public static final int PercentnameMinusprefix = 41;
		public static final int PercentnoMinusdefaultMinusprec = 42;
		public static final int PercentnoMinuslines = 43;
		public static final int PercentnondeterministicMinusparser = 44;
		public static final int Percentoutput = 45;
		public static final int Percentparam = 46;
		public static final int Percentrequire = 47;
		public static final int Percentskeleton = 48;
		public static final int Percentstart = 49;
		public static final int PercenttokenMinustable = 50;
		public static final int Percentunion = 51;
		public static final int Percentverbose = 52;
		public static final int Percentyacc = 53;
		public static final int LcurlyDotDotDotRcurly = 54;
		public static final int PercentQuestionmarkLcurlyDotDotDotRcurly = 55;
		public static final int PercentLcurlyDotDotDotPercentRcurly = 56;
		public static final int tag_any = 57;
		public static final int tag_inc_nesting = 58;
		public static final int TAG = 59;
		public static final int code_char = 60;
		public static final int code_string = 61;
		public static final int code_comment = 62;
		public static final int code_ml_comment = 63;
		public static final int code_any = 64;
		public static final int code_inc_nesting = 65;
		public static final int code_dec_nesting = 66;
		public static final int code_lessless = 67;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset, int endoffset);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	private char[] data;
	private int tokenOffset;
	private int l;
	private int charOffset;
	private int chr;

	private int state;

	private int tokenLine;
	private int currLine;
	private int currOffset;

	private int nesting = 0;
	private int lexemeStart = -1;
	private int foundColonOffset = -1;
	private int sectionCounter = 0;

	private boolean lookaheadColon() throws IOException {
		int offset = 0;
		// TODO handle "aa [ bb ] :"
		while (lookahead(offset) == ' ') offset++;
		if (lookahead(offset) == ':') {
			foundColonOffset = currOffset + offset;
			return true;
		}
		return false;
	}

	protected int lookahead(int i) throws IOException {
		if (i == 0) return chr;
		return l + i - 1 < data.length ? data[l] : 0;
	}

	public BisonLexer(char[] input, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(input);
	}

	public void reset(char[] input) throws IOException {
		this.state = 0;
		tokenLine = currLine = 1;
		currOffset = 0;
		this.data = input;
		tokenOffset = l = 0;
		charOffset = l;
		chr = l < data.length ? data[l++] : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < data.length &&
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
		charOffset = l;
		chr = l < data.length ? data[l++] : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < data.length &&
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
		return new String(data, tokenOffset, charOffset - tokenOffset);
	}

	private static final short tmCharClass[] = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 51, 45, 51, 51, 44, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		51, 1, 9, 1, 1, 13, 1, 4, 1, 1, 11, 1, 1, 37, 46, 18,
		3, 50, 50, 50, 50, 50, 50, 50, 47, 47, 2, 15, 10, 1, 12, 42,
		1, 49, 49, 49, 49, 49, 49, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 8, 46, 46, 48, 46, 46, 16, 5, 17, 1, 46,
		1, 36, 39, 30, 28, 22, 33, 34, 35, 31, 46, 21, 32, 25, 23, 20,
		27, 40, 24, 29, 19, 7, 38, 46, 6, 26, 46, 41, 14, 43, 1, 1
	};

	private static final short tmStateMap[] = {
		0, 1, 2, 3, 4, 5
	};

	private static final short[] tmRuleSymbol = unpack_short(72,
		"\2\3\4\4\5\6\7\10\11\12\13\14\15\3\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35" +
		"\36\37\40\41\42\43\44\45\46\47\50\51\52\53\54\55\56\57\60\61\62\63\64\65\3\3\3\3" +
		"\66\67\70\71\72\73\74\75\76\77\100\101\102\103");

	private static final int tmClassesCount = 52;

	private static final short[] tmGoto = unpack_vc_short(19552,
		"\1\ufffe\1\uffff\1\6\1\7\1\10\1\uffff\3\11\1\12\1\13\2\uffff\1\14\1\15\1\16\1\17" +
		"\1\20\1\21\22\11\1\uffff\3\11\1\22\2\uffff\2\23\1\11\1\24\2\11\1\24\1\23\1\uffff" +
		"\3\25\1\26\4\25\1\27\1\30\2\25\1\31\4\25\1\32\26\25\1\33\1\25\1\34\10\25\1\uffff" +
		"\3\25\1\26\4\25\1\27\1\30\2\25\1\31\4\25\1\32\26\25\1\33\1\25\1\35\10\25\1\uffff" +
		"\3\25\1\26\4\25\1\27\3\25\1\36\4\25\1\32\41\25\1\uffff\11\37\1\40\1\37\1\41\30\37" +
		"\1\42\16\37\1\uffff\3\25\1\26\4\25\1\27\10\25\1\32\41\25\64\ufffc\3\ufffb\1\24\2" +
		"\ufffb\1\43\50\ufffb\1\24\1\43\1\ufffb\1\24\1\ufffb\1\uffff\3\44\1\uffff\1\45\46" +
		"\44\2\uffff\6\44\3\ufffd\1\11\2\ufffd\3\11\12\ufffd\26\11\5\ufffd\5\11\1\ufffd\1" +
		"\uffff\4\12\1\46\3\12\1\47\42\12\2\uffff\6\12\13\uffc4\1\50\1\51\47\uffc4\7\uffff" +
		"\1\52\2\uffff\1\53\2\uffff\1\54\5\uffff\1\55\1\56\1\uffff\1\57\1\60\1\61\1\62\1\63" +
		"\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1\73\3\uffff\1\74\2\uffff\1\75\1\76\11\uffff" +
		"\64\ufff4\64\ufff3\64\ufff2\64\ufff1\13\uffff\1\77\6\uffff\1\100\41\uffff\64\uffc7" +
		"\54\ufff0\2\23\5\ufff0\1\23\3\ufffb\1\24\53\ufffb\1\24\2\ufffb\1\24\1\ufffb\65\uffb9" +
		"\3\101\1\102\1\103\47\101\1\uffb9\6\101\1\uffb9\4\104\1\105\3\104\1\106\43\104\1" +
		"\uffb9\6\104\5\uffb9\1\107\4\uffb9\1\110\2\uffb9\1\111\53\uffb9\1\112\6\uffb9\1\113" +
		"\54\uffb9\1\114\5\uffb9\1\115\6\uffb9\1\116\41\uffb9\64\uffb8\64\uffc3\64\uffc2\53" +
		"\uffb9\1\117\10\uffb9\1\uffc0\11\37\1\uffc0\1\37\1\uffc0\30\37\1\42\16\37\64\uffbf" +
		"\64\uffbe\1\uffc0\11\37\1\uffc0\32\37\1\42\16\37\3\uffff\1\120\22\uffff\1\120\5\uffff" +
		"\1\120\1\uffff\1\120\2\uffff\1\120\2\uffff\1\120\2\uffff\1\120\7\uffff\1\120\1\uffff" +
		"\2\120\5\uffff\1\121\62\uffff\1\122\2\44\1\123\1\124\1\125\1\44\11\uffff\1\44\3\uffff" +
		"\2\44\10\uffff\1\44\2\uffff\1\44\1\uffff\2\44\2\uffff\1\44\7\uffff\1\122\4\uffff" +
		"\1\126\2\12\1\127\1\130\1\131\1\12\11\uffff\1\12\3\uffff\2\12\10\uffff\1\12\2\uffff" +
		"\1\12\1\uffff\2\12\2\uffff\1\12\7\uffff\1\126\1\uffff\64\ufff8\14\uffff\1\132\47" +
		"\uffff\64\ufff6\27\uffff\1\133\75\uffff\1\134\22\uffff\64\ufff5\24\uffff\1\135\5" +
		"\uffff\1\136\40\uffff\1\137\62\uffff\1\140\21\uffff\1\141\1\142\55\uffff\1\143\1" +
		"\144\17\uffff\1\145\45\uffff\1\146\10\uffff\1\147\52\uffff\1\150\101\uffff\1\151" +
		"\47\uffff\1\152\13\uffff\1\153\45\uffff\1\154\4\uffff\1\155\53\uffff\1\156\1\uffff" +
		"\1\157\62\uffff\1\160\66\uffff\1\161\62\uffff\1\162\15\uffff\1\163\56\uffff\1\164" +
		"\64\uffff\1\165\51\uffff\1\166\35\uffff\64\uffc5\51\uffff\1\167\2\uffff\2\76\5\uffff" +
		"\1\76\1\uffff\12\77\1\170\50\77\1\uffef\53\100\2\uffef\6\100\1\uffff\3\101\1\102" +
		"\1\103\47\101\1\uffff\6\101\64\uffbd\3\uffff\1\171\2\101\1\172\1\173\1\174\1\101" +
		"\11\uffff\1\101\3\uffff\2\101\10\uffff\1\101\2\uffff\1\101\1\uffff\2\101\2\uffff" +
		"\1\101\7\uffff\1\171\2\uffff\4\104\1\105\3\104\1\106\43\104\1\uffff\6\104\3\uffff" +
		"\1\175\2\104\1\176\1\177\1\200\1\104\11\uffff\1\104\3\uffff\2\104\10\uffff\1\104" +
		"\2\uffff\1\104\1\uffff\2\104\2\uffff\1\104\7\uffff\1\175\1\uffff\64\uffbc\54\uffff" +
		"\1\201\1\202\5\uffff\1\107\64\uffb6\64\uffb8\54\uffff\1\203\1\204\5\uffff\1\112\64" +
		"\uffb7\54\uffff\1\205\1\206\5\uffff\1\114\1\uffff\12\115\1\207\50\115\1\uffbb\53" +
		"\116\2\uffbb\6\116\64\uffc1\3\ufffa\1\120\22\ufffa\1\120\5\ufffa\1\120\1\ufffa\1" +
		"\120\2\ufffa\1\120\2\ufffa\1\120\2\ufffa\1\120\7\ufffa\1\120\1\ufffa\2\120\1\ufffa" +
		"\64\ufff9\3\uffff\1\210\1\121\55\uffff\1\210\4\uffff\1\211\22\uffff\1\211\5\uffff" +
		"\1\211\1\uffff\1\211\2\uffff\1\211\2\uffff\1\211\2\uffff\1\211\7\uffff\1\211\1\uffff" +
		"\2\211\4\uffff\1\212\22\uffff\1\212\5\uffff\1\212\1\uffff\1\212\2\uffff\1\212\2\uffff" +
		"\1\212\2\uffff\1\212\7\uffff\1\212\1\uffff\2\212\4\uffff\1\213\22\uffff\1\213\5\uffff" +
		"\1\213\1\uffff\1\213\2\uffff\1\213\2\uffff\1\213\2\uffff\1\213\7\uffff\1\213\1\uffff" +
		"\2\213\2\uffff\2\12\1\214\1\12\1\46\3\12\1\47\42\12\2\uffff\4\12\1\214\1\12\3\uffff" +
		"\1\215\22\uffff\1\215\5\uffff\1\215\1\uffff\1\215\2\uffff\1\215\2\uffff\1\215\2\uffff" +
		"\1\215\7\uffff\1\215\1\uffff\2\215\4\uffff\1\216\22\uffff\1\216\5\uffff\1\216\1\uffff" +
		"\1\216\2\uffff\1\216\2\uffff\1\216\2\uffff\1\216\7\uffff\1\216\1\uffff\2\216\4\uffff" +
		"\1\217\22\uffff\1\217\5\uffff\1\217\1\uffff\1\217\2\uffff\1\217\2\uffff\1\217\2\uffff" +
		"\1\217\7\uffff\1\217\1\uffff\2\217\1\uffff\64\ufff7\37\uffff\1\220\64\uffff\1\221" +
		"\50\uffff\1\222\71\uffff\1\223\53\uffff\1\224\73\uffff\1\225\60\uffff\1\226\66\uffff" +
		"\1\227\56\uffff\1\230\64\uffff\1\231\15\uffff\1\232\47\uffff\1\233\102\uffff\1\234" +
		"\55\uffff\1\235\51\uffff\1\236\71\uffff\1\237\53\uffff\1\240\10\uffff\1\241\54\uffff" +
		"\1\242\70\uffff\1\243\3\uffff\1\244\52\uffff\1\245\77\uffff\1\246\45\uffff\1\247" +
		"\71\uffff\1\250\66\uffff\1\251\65\uffff\1\252\51\uffff\1\253\74\uffff\1\254\53\uffff" +
		"\1\255\63\uffff\1\256\33\uffff\64\uffc6\1\uffff\12\77\1\170\6\77\1\257\41\77\1\uffff" +
		"\2\101\1\260\1\102\1\103\47\101\1\uffff\4\101\1\260\1\101\3\uffff\1\261\22\uffff" +
		"\1\261\5\uffff\1\261\1\uffff\1\261\2\uffff\1\261\2\uffff\1\261\2\uffff\1\261\7\uffff" +
		"\1\261\1\uffff\2\261\4\uffff\1\262\22\uffff\1\262\5\uffff\1\262\1\uffff\1\262\2\uffff" +
		"\1\262\2\uffff\1\262\2\uffff\1\262\7\uffff\1\262\1\uffff\2\262\4\uffff\1\263\22\uffff" +
		"\1\263\5\uffff\1\263\1\uffff\1\263\2\uffff\1\263\2\uffff\1\263\2\uffff\1\263\7\uffff" +
		"\1\263\1\uffff\2\263\2\uffff\2\104\1\264\1\104\1\105\3\104\1\106\43\104\1\uffff\4" +
		"\104\1\264\1\104\3\uffff\1\265\22\uffff\1\265\5\uffff\1\265\1\uffff\1\265\2\uffff" +
		"\1\265\2\uffff\1\265\2\uffff\1\265\7\uffff\1\265\1\uffff\2\265\4\uffff\1\266\22\uffff" +
		"\1\266\5\uffff\1\266\1\uffff\1\266\2\uffff\1\266\2\uffff\1\266\2\uffff\1\266\7\uffff" +
		"\1\266\1\uffff\2\266\4\uffff\1\267\22\uffff\1\267\5\uffff\1\267\1\uffff\1\267\2\uffff" +
		"\1\267\2\uffff\1\267\2\uffff\1\267\7\uffff\1\267\1\uffff\2\267\56\uffff\1\202\13" +
		"\uffff\1\107\4\uffff\1\110\2\uffff\1\111\123\uffff\1\204\13\uffff\1\112\6\uffff\1" +
		"\113\124\uffff\1\206\13\uffff\1\114\5\uffff\1\115\6\uffff\1\116\42\uffff\12\115\1" +
		"\207\6\115\1\270\41\115\3\uffff\1\44\1\121\55\uffff\1\44\4\uffff\1\211\1\121\21\uffff" +
		"\1\211\5\uffff\1\211\1\uffff\1\211\2\uffff\1\211\2\uffff\1\211\2\uffff\1\211\7\uffff" +
		"\1\211\1\uffff\2\211\4\uffff\1\271\22\uffff\1\271\5\uffff\1\271\1\uffff\1\271\2\uffff" +
		"\1\271\2\uffff\1\271\2\uffff\1\271\7\uffff\1\271\1\uffff\2\271\4\uffff\1\272\22\uffff" +
		"\1\272\5\uffff\1\272\1\uffff\1\272\2\uffff\1\272\2\uffff\1\272\2\uffff\1\272\7\uffff" +
		"\1\272\1\uffff\2\272\2\uffff\4\12\1\46\3\12\1\47\42\12\2\uffff\6\12\1\uffff\2\12" +
		"\1\215\1\12\1\46\3\12\1\47\14\12\1\215\5\12\1\215\1\12\1\215\2\12\1\215\2\12\1\215" +
		"\2\12\1\215\4\12\2\uffff\1\12\1\215\1\12\2\215\1\12\3\uffff\1\273\22\uffff\1\273" +
		"\5\uffff\1\273\1\uffff\1\273\2\uffff\1\273\2\uffff\1\273\2\uffff\1\273\7\uffff\1" +
		"\273\1\uffff\2\273\4\uffff\1\274\22\uffff\1\274\5\uffff\1\274\1\uffff\1\274\2\uffff" +
		"\1\274\2\uffff\1\274\2\uffff\1\274\7\uffff\1\274\1\uffff\2\274\25\uffff\1\275\103" +
		"\uffff\1\276\45\uffff\1\277\63\uffff\1\300\70\uffff\1\301\56\uffff\1\302\61\uffff" +
		"\1\303\62\uffff\1\304\70\uffff\1\305\67\uffff\1\306\7\uffff\1\307\53\uffff\1\310" +
		"\3\uffff\1\311\51\uffff\1\312\44\uffff\1\313\117\uffff\1\314\62\uffff\1\315\57\uffff" +
		"\1\316\63\uffff\1\317\54\uffff\1\320\100\uffff\1\321\42\uffff\1\322\77\uffff\1\323" +
		"\4\uffff\1\324\45\uffff\1\325\65\uffff\1\326\73\uffff\1\327\51\uffff\1\330\60\uffff" +
		"\1\331\63\uffff\1\332\102\uffff\1\333\47\uffff\1\334\102\uffff\1\335\65\uffff\1\336" +
		"\14\uffff\64\uffee\1\uffff\3\101\1\102\1\103\47\101\1\uffff\6\101\1\uffff\2\101\1" +
		"\261\1\102\1\103\20\101\1\261\5\101\1\261\1\101\1\261\2\101\1\261\2\101\1\261\2\101" +
		"\1\261\5\101\1\uffff\1\101\1\261\1\101\2\261\1\101\3\uffff\1\337\22\uffff\1\337\5" +
		"\uffff\1\337\1\uffff\1\337\2\uffff\1\337\2\uffff\1\337\2\uffff\1\337\7\uffff\1\337" +
		"\1\uffff\2\337\4\uffff\1\340\22\uffff\1\340\5\uffff\1\340\1\uffff\1\340\2\uffff\1" +
		"\340\2\uffff\1\340\2\uffff\1\340\7\uffff\1\340\1\uffff\2\340\2\uffff\4\104\1\105" +
		"\3\104\1\106\43\104\1\uffff\6\104\1\uffff\2\104\1\265\1\104\1\105\3\104\1\106\14" +
		"\104\1\265\5\104\1\265\1\104\1\265\2\104\1\265\2\104\1\265\2\104\1\265\5\104\1\uffff" +
		"\1\104\1\265\1\104\2\265\1\104\3\uffff\1\341\22\uffff\1\341\5\uffff\1\341\1\uffff" +
		"\1\341\2\uffff\1\341\2\uffff\1\341\2\uffff\1\341\7\uffff\1\341\1\uffff\2\341\4\uffff" +
		"\1\342\22\uffff\1\342\5\uffff\1\342\1\uffff\1\342\2\uffff\1\342\2\uffff\1\342\2\uffff" +
		"\1\342\7\uffff\1\342\1\uffff\2\342\1\uffff\64\uffba\3\uffff\1\343\22\uffff\1\343" +
		"\5\uffff\1\343\1\uffff\1\343\2\uffff\1\343\2\uffff\1\343\2\uffff\1\343\7\uffff\1" +
		"\343\1\uffff\2\343\4\uffff\1\344\22\uffff\1\344\5\uffff\1\344\1\uffff\1\344\2\uffff" +
		"\1\344\2\uffff\1\344\2\uffff\1\344\7\uffff\1\344\1\uffff\2\344\4\uffff\1\345\22\uffff" +
		"\1\345\5\uffff\1\345\1\uffff\1\345\2\uffff\1\345\2\uffff\1\345\2\uffff\1\345\7\uffff" +
		"\1\345\1\uffff\2\345\4\uffff\1\346\22\uffff\1\346\5\uffff\1\346\1\uffff\1\346\2\uffff" +
		"\1\346\2\uffff\1\346\2\uffff\1\346\7\uffff\1\346\1\uffff\2\346\30\uffff\1\347\76" +
		"\uffff\1\350\50\uffff\1\351\34\uffff\64\uffeb\7\uffff\1\352\112\uffff\1\353\55\uffff" +
		"\1\354\65\uffff\1\355\62\uffff\1\356\60\uffff\1\357\72\uffff\1\360\54\uffff\1\361" +
		"\74\uffff\1\362\71\uffff\1\363\55\uffff\1\364\47\uffff\1\365\66\uffff\1\366\35\uffff" +
		"\64\uffc8\26\uffe4\1\367\35\uffe4\23\uffff\1\370\71\uffff\1\371\62\uffff\1\372\62" +
		"\uffff\1\373\43\uffff\1\374\112\uffff\1\375\50\uffff\1\376\66\uffff\1\377\35\uffff" +
		"\64\uffe1\37\uffff\1\u0100\24\uffff\64\uffe8\7\uffff\1\u0101\121\uffff\1\u0102\51" +
		"\uffff\1\u0103\54\uffff\1\u0104\42\uffff\1\u0105\22\uffff\1\u0105\5\uffff\1\u0105" +
		"\1\uffff\1\u0105\2\uffff\1\u0105\2\uffff\1\u0105\2\uffff\1\u0105\7\uffff\1\u0105" +
		"\1\uffff\2\u0105\4\uffff\1\u0106\22\uffff\1\u0106\5\uffff\1\u0106\1\uffff\1\u0106" +
		"\2\uffff\1\u0106\2\uffff\1\u0106\2\uffff\1\u0106\7\uffff\1\u0106\1\uffff\2\u0106" +
		"\4\uffff\1\u0107\22\uffff\1\u0107\5\uffff\1\u0107\1\uffff\1\u0107\2\uffff\1\u0107" +
		"\2\uffff\1\u0107\2\uffff\1\u0107\7\uffff\1\u0107\1\uffff\2\u0107\4\uffff\1\u0108" +
		"\22\uffff\1\u0108\5\uffff\1\u0108\1\uffff\1\u0108\2\uffff\1\u0108\2\uffff\1\u0108" +
		"\2\uffff\1\u0108\7\uffff\1\u0108\1\uffff\2\u0108\4\uffff\1\44\22\uffff\1\44\5\uffff" +
		"\1\44\1\uffff\1\44\2\uffff\1\44\2\uffff\1\44\2\uffff\1\44\7\uffff\1\44\1\uffff\2" +
		"\44\4\uffff\1\124\22\uffff\1\124\5\uffff\1\124\1\uffff\1\124\2\uffff\1\124\2\uffff" +
		"\1\124\2\uffff\1\124\7\uffff\1\124\1\uffff\2\124\4\uffff\1\12\22\uffff\1\12\5\uffff" +
		"\1\12\1\uffff\1\12\2\uffff\1\12\2\uffff\1\12\2\uffff\1\12\7\uffff\1\12\1\uffff\2" +
		"\12\4\uffff\1\130\22\uffff\1\130\5\uffff\1\130\1\uffff\1\130\2\uffff\1\130\2\uffff" +
		"\1\130\2\uffff\1\130\7\uffff\1\130\1\uffff\2\130\1\uffff\64\uffca\14\uffff\1\u0109" +
		"\47\uffff\45\uffed\1\u010a\16\uffed\23\uffff\1\u010b\63\uffff\1\u010c\105\uffff\1" +
		"\u010d\16\uffff\64\uffdd\64\uffec\23\uffff\1\u010e\75\uffff\1\u010f\67\uffff\1\u0110" +
		"\51\uffff\1\u0111\67\uffff\1\u0112\60\uffff\1\u0113\33\uffff\64\uffe7\64\uffe2\34" +
		"\uffff\1\u0114\55\uffff\1\u0115\35\uffff\64\uffcf\7\uffff\1\u0116\102\uffff\1\u0117" +
		"\75\uffff\1\u0118\23\uffff\64\uffe3\64\uffcc\23\uffff\1\u0119\104\uffff\1\u011a\63" +
		"\uffff\1\u011b\52\uffff\1\u011c\74\uffff\1\u011d\54\uffff\1\u011e\31\uffff\1\101" +
		"\22\uffff\1\101\5\uffff\1\101\1\uffff\1\101\2\uffff\1\101\2\uffff\1\101\2\uffff\1" +
		"\101\7\uffff\1\101\1\uffff\2\101\4\uffff\1\173\22\uffff\1\173\5\uffff\1\173\1\uffff" +
		"\1\173\2\uffff\1\173\2\uffff\1\173\2\uffff\1\173\7\uffff\1\173\1\uffff\2\173\4\uffff" +
		"\1\104\22\uffff\1\104\5\uffff\1\104\1\uffff\1\104\2\uffff\1\104\2\uffff\1\104\2\uffff" +
		"\1\104\7\uffff\1\104\1\uffff\2\104\4\uffff\1\177\22\uffff\1\177\5\uffff\1\177\1\uffff" +
		"\1\177\2\uffff\1\177\2\uffff\1\177\2\uffff\1\177\7\uffff\1\177\1\uffff\2\177\1\uffff" +
		"\64\uffd9\23\uffff\1\u011f\40\uffff\64\uffd0\45\uffdb\1\u0120\16\uffdb\46\uffff\1" +
		"\u0121\43\uffff\1\u0122\61\uffff\1\u0123\103\uffff\1\u0124\45\uffff\1\u0125\65\uffff" +
		"\1\u0126\61\uffff\1\u0127\63\uffff\1\u0128\65\uffff\1\u0129\71\uffff\1\u012a\25\uffff" +
		"\35\uffdf\1\u012b\26\uffdf\23\uffff\1\u012c\64\uffff\1\u012d\77\uffff\1\u012e\65" +
		"\uffff\1\u012f\51\uffff\1\u0130\63\uffff\1\u0131\61\uffff\1\u0132\101\uffff\1\u0133" +
		"\47\uffff\1\u0134\61\uffff\1\u0135\65\uffff\1\u0136\71\uffff\1\u0137\34\uffff\1\u0138" +
		"\111\uffff\1\u0139\54\uffff\1\u013a\35\uffff\64\uffce\27\uffff\1\u013b\34\uffff\64" +
		"\uffe9\23\uffff\1\u013c\40\uffff\64\uffde\45\uffff\1\u013d\45\uffff\1\u013e\101\uffff" +
		"\1\u013f\44\uffff\1\u0140\63\uffff\1\u0141\72\uffff\1\u0142\26\uffff\64\uffc9\47" +
		"\uffff\1\u0143\44\uffff\1\u0144\63\uffff\1\u0145\64\uffff\1\u0146\32\uffff\64\uffe6" +
		"\40\uffff\1\u0147\23\uffff\64\uffd2\41\uffff\1\u0148\60\uffff\1\u0149\51\uffff\1" +
		"\u014a\72\uffff\1\u014b\30\uffff\64\uffcd\44\uffff\1\u014c\17\uffff\64\uffd5\41\uffff" +
		"\1\u014d\50\uffff\1\u014e\75\uffff\1\u014f\23\uffff\64\uffda\47\uffff\1\u0150\53" +
		"\uffff\1\u0151\47\uffff\1\u0152\77\uffff\1\u0153\52\uffff\1\u0154\65\uffff\1\u0155" +
		"\63\uffff\1\u0156\71\uffff\1\u0157\64\uffff\1\u0158\54\uffff\1\u0159\61\uffff\1\u015a" +
		"\61\uffff\1\u015b\66\uffff\1\u015c\101\uffff\1\u015d\24\uffff\1\u015e\55\uffff\64" +
		"\uffe5\64\uffea\26\uffff\1\u015f\60\uffff\1\u0160\46\uffff\1\u0161\55\uffff\64\uffd7" +
		"\64\uffcb\35\uffff\1\u0162\65\uffff\1\u0163\57\uffff\1\u0164\30\uffff\64\uffd4\36" +
		"\uffff\1\u0165\64\uffff\1\u0166\24\uffff\64\uffd8\26\uffff\1\u0167\72\uffff\1\u0168" +
		"\56\uffff\1\u0169\33\uffff\64\uffe0\24\uffff\1\u016a\37\uffff\64\uffdc\23\uffff\1" +
		"\u016b\66\uffff\1\u016c\64\uffff\1\u016d\73\uffff\1\u016e\62\uffff\1\u016f\25\uffff" +
		"\64\uffd6\36\uffff\1\u0170\25\uffff\64\uffd3\45\uffff\1\u0171\51\uffff\1\u0172\74" +
		"\uffff\1\u0173\47\uffff\1\u0174\70\uffff\1\u0175\54\uffff\1\u0176\65\uffff\1\u0177" +
		"\33\uffff\64\uffd1");

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
			tokenOffset = charOffset;

			for (state = tmStateMap[this.state]; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == -1) {
					lapg_n.endoffset = currOffset;
					lapg_n.symbol = 0;
					lapg_n.value = null;
					reporter.error("Unexpected end of input reached", lapg_n.line, lapg_n.offset, lapg_n.endoffset);
					lapg_n.offset = currOffset;
					return lapg_n;
				}
				if (state >= -1 && chr != -1) {
					currOffset += l - charOffset;
					if (chr == '\n') {
						currLine++;
					}
					charOffset = l;
					chr = l < data.length ? data[l++] : -1;
					if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < data.length &&
							Character.isLowSurrogate(data[l])) {
						chr = Character.toCodePoint((char) chr, data[l++]);
					}
				}
			}
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, current()), lapg_n.line, lapg_n.offset, lapg_n.endoffset);
				lapg_n.symbol = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.symbol = Tokens.eoi;
				lapg_n.value = null;
				return lapg_n;
			}

			lapg_n.symbol = tmRuleSymbol[-state - 3];
			lapg_n.value = null;

		} while (lapg_n.symbol == -1 || !createToken(lapg_n, -state - 3));
		return lapg_n;
	}

	protected boolean createToken(LapgSymbol lapg_n, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 0: // ID: /{letter}({letter}|[0-9\-])*/
				 if (lookaheadColon()) lapg_n.symbol = Tokens.ID_COLON; 
				break;
			case 1: // skip: /:/
				spaceToken = true;
				
		if (lapg_n.offset != foundColonOffset)
			reporter.error("Unexpected colon", lapg_n.line, lapg_n.offset, lapg_n.endoffset);
	
				break;
			case 8: // '%%': /%%/
				 if (++sectionCounter == 2) lapg_n.symbol = Tokens.eoi; 
				break;
			case 13: // skip: /[\r\n\t\f\v ]+/
				spaceToken = true;
				break;
			case 14: // skip_comment: /\/\/[^\r\n]*/
				spaceToken = true;
				break;
			case 15: // skip_ml_comment: /\/\*([^*]|\*+[^\/*])*\*+\//
				spaceToken = true;
				break;
			case 54: // skip: /\{/
				spaceToken = true;
				state = States.bracedCode;
				 nesting = 0; lexemeStart = lapg_n.offset; 
				break;
			case 55: // skip: /%\?[ \f\r\n\t\v]*\{/
				spaceToken = true;
				state = States.predicate;
				 nesting = 0; lexemeStart = lapg_n.offset; 
				break;
			case 56: // skip: /%\{/
				spaceToken = true;
				state = States.prologue;
				 nesting = 0; lexemeStart = lapg_n.offset; 
				break;
			case 57: // skip: /</
				spaceToken = true;
				state = States.tag;
				 nesting = 0; lexemeStart = lapg_n.offset; 
				break;
			case 58: // '{...}': /\}/
				
		nesting--;
		if (nesting < 0) {
			setState(States.initial);
			lapg_n.offset = lexemeStart;
			lapg_n.value = ""; // TODO
		} else {
			spaceToken = true;
		}
	
				break;
			case 59: // '%?{...}': /\}/
				 nesting--; if (nesting < 0) { setState(States.initial); lapg_n.offset = lexemeStart; } else { spaceToken = true; } 
				break;
			case 60: // '%{...%}': /%\}/
				state = States.initial;
				 lapg_n.offset = lexemeStart; 
				break;
			case 61: // tag_any: /([^<>]|\->)+/
				spaceToken = true;
				break;
			case 62: // tag_inc_nesting: /</
				spaceToken = true;
				 nesting++; 
				break;
			case 63: // TAG: />/
				 nesting--; if (nesting < 0) { setState(States.initial); lapg_n.offset = lexemeStart; } else { spaceToken = true; } 
				break;
			case 64: // code_char: /'([^'\n\\]|{escape})*'/
				spaceToken = true;
				break;
			case 65: // code_string: /"([^"\n\\]|{escape})*"/
				spaceToken = true;
				break;
			case 66: // code_comment: /\/{splice}\/[^\r\n]*/
				spaceToken = true;
				break;
			case 67: // code_ml_comment: /\/{splice}\*([^*]|\*+[^\/*])*\*+\//
				spaceToken = true;
				break;
			case 68: // code_any: /.|\n/
				spaceToken = true;
				break;
			case 69: // code_inc_nesting: /\{|<{splice}%/
				spaceToken = true;
				 nesting++; 
				break;
			case 70: // code_dec_nesting: /%{splice}>/
				spaceToken = true;
				 nesting--; 
				break;
			case 71: // code_lessless: /<{splice}</
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
