/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.tool.importer;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

public class BisonLexer {

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
		int bracedCode = 1;
		int predicate = 2;
		int prologue = 3;
		int tag = 4;
		int epilogue = 5;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int ID_COLON = 1;
		int ID = 2;
		int skip = 3;
		int INT = 4;
		int CHAR = 5;
		int STRING = 6;
		int LtMultGt = 7;
		int LtGt = 8;
		int RemRem = 9;
		int Or = 10;
		int Semicolon = 11;
		int Lbrack = 12;
		int Rbrack = 13;
		int skip_comment = 14;
		int skip_ml_comment = 15;
		int Remtoken = 16;
		int Remnterm = 17;
		int Remtype = 18;
		int Remdestructor = 19;
		int Remprinter = 20;
		int Remleft = 21;
		int Remright = 22;
		int Remnonassoc = 23;
		int Remprecedence = 24;
		int Remprec = 25;
		int Remdprec = 26;
		int Remmerge = 27;
		int Remcode = 28;
		int RemdefaultMinusprec = 29;
		int Remdefine = 30;
		int Remdefines = 31;
		int Remempty = 32;
		int RemerrorMinusverbose = 33;
		int Remexpect = 34;
		int RemexpectMinusrr = 35;
		int RemLtflagGt = 36;
		int RemfileMinusprefix = 37;
		int RemglrMinusparser = 38;
		int ReminitialMinusaction = 39;
		int Remlanguage = 40;
		int RemnameMinusprefix = 41;
		int RemnoMinusdefaultMinusprec = 42;
		int RemnoMinuslines = 43;
		int RemnondeterministicMinusparser = 44;
		int Remoutput = 45;
		int Remparam = 46;
		int Remrequire = 47;
		int Remskeleton = 48;
		int Remstart = 49;
		int RemtokenMinustable = 50;
		int Remunion = 51;
		int Remverbose = 52;
		int Remyacc = 53;
		int LbraceDotDotDotRbrace = 54;
		int Rbrace = 55;
		int RemRbrace = 56;
		int tag_any = 57;
		int tag_inc_nesting = 58;
		int TAG = 59;
		int code_char = 60;
		int code_string = 61;
		int code_comment = 62;
		int code_ml_comment = 63;
		int code_any = 64;
		int code_inc_nesting = 65;
		int code_dec_nesting = 66;
		int code_lessless = 67;
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

	private int nesting = 0;
	private int lexemeStart = -1;
	private int foundColonOffset = -1;
	private int sectionCounter = 0;

	private boolean lookaheadColon() throws IOException {
		int offset = 0;
		// TODO handle "aa [ bb ] :"
		while (charAt(offset) == ' ') offset++;
		if (charAt(offset) == ':') {
			foundColonOffset = currOffset + offset;
			return true;
		}
		return false;
	}

	public BisonLexer(CharSequence input, ErrorReporter reporter) throws IOException {
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 51, 45, 51, 51, 44, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		51, 1, 9, 1, 1, 13, 1, 4, 1, 1, 11, 1, 1, 37, 46, 18,
		3, 50, 50, 50, 50, 50, 50, 50, 47, 47, 2, 15, 10, 1, 12, 42,
		1, 49, 49, 49, 49, 49, 49, 46, 46, 46, 46, 46, 46, 46, 46, 46,
		46, 46, 46, 46, 46, 8, 46, 46, 48, 46, 46, 16, 5, 17, 1, 46,
		1, 36, 39, 30, 28, 22, 33, 34, 35, 31, 46, 21, 32, 25, 23, 20,
		27, 40, 24, 29, 19, 7, 38, 46, 6, 26, 46, 41, 14, 43
	};

	private static final short tmStateMap[] = {
		0, 310, 365, 367, 370, 375
	};

	private static final int[] tmRuleSymbol = unpack_int(72,
		"\2\0\3\0\4\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\3\0\16\0\17\0\20\0\21" +
		"\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36\0\37\0\40\0\41" +
		"\0\42\0\43\0\44\0\45\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57\0\60\0\61" +
		"\0\62\0\63\0\64\0\65\0\3\0\3\0\3\0\3\0\66\0\67\0\70\0\71\0\72\0\73\0\74\0\75\0\76" +
		"\0\77\0\100\0\101\0\102\0\103\0");

	private static final int tmClassesCount = 52;

	private static final short[] tmGoto = unpack_vc_short(19552,
		"\1\ufffe\1\uffff\1\u0135\1\u0132\1\u0122\1\uffff\3\u0121\1\u0112\1\u010e\2\uffff" +
		"\1\15\1\14\1\13\1\12\1\11\1\4\22\u0121\1\uffff\3\u0121\1\3\2\uffff\2\2\1\u0121\1" +
		"\1\2\u0121\1\1\1\2\3\ufffb\1\1\53\ufffb\1\1\2\ufffb\1\1\1\ufffb\54\ufff0\2\2\5\ufff0" +
		"\1\2\64\uffc7\13\uffff\1\6\6\uffff\1\5\41\uffff\1\uffef\53\5\2\uffef\6\5\1\uffff" +
		"\12\6\1\7\50\6\1\uffff\12\6\1\7\6\6\1\10\41\6\64\uffee\64\ufff1\64\ufff2\64\ufff3" +
		"\64\ufff4\7\uffff\1\u0109\2\uffff\1\u0103\2\uffff\1\u0102\5\uffff\1\364\1\356\1\uffff" +
		"\1\325\1\231\1\216\1\211\1\205\1\162\1\126\1\112\1\106\1\70\1\55\1\42\1\30\3\uffff" +
		"\1\21\2\uffff\1\20\1\16\62\uffff\1\17\2\uffff\2\16\5\uffff\1\16\64\uffc6\64\uffc5" +
		"\26\uffff\1\22\65\uffff\1\23\102\uffff\1\24\40\uffff\1\25\74\uffff\1\26\54\uffff" +
		"\1\27\35\uffff\64\uffc9\40\uffff\1\31\53\uffff\1\32\100\uffff\1\33\51\uffff\1\34" +
		"\74\uffff\1\35\47\uffff\1\36\70\uffff\1\37\54\uffff\1\40\65\uffff\1\41\33\uffff\64" +
		"\uffd7\37\uffff\1\43\64\uffff\1\44\51\uffff\1\45\102\uffff\1\46\51\uffff\1\47\60" +
		"\uffff\1\50\61\uffff\1\51\76\uffff\1\52\61\uffff\1\53\32\uffff\1\54\55\uffff\64\uffd8" +
		"\26\uffff\1\65\15\uffff\1\56\46\uffff\1\57\76\uffff\1\60\30\uffff\1\61\120\uffff" +
		"\1\62\61\uffff\1\63\47\uffff\1\64\35\uffff\64\uffd5\41\uffff\1\66\45\uffff\1\67\40" +
		"\uffff\64\uffe8\27\uffff\1\71\73\uffff\1\72\47\uffff\1\73\77\uffff\1\74\70\uffff" +
		"\1\75\57\uffff\1\76\70\uffff\1\77\62\uffff\1\100\55\uffff\1\101\50\uffff\1\102\77" +
		"\uffff\1\103\50\uffff\1\104\66\uffff\1\105\34\uffff\64\uffd6\24\uffff\1\107\73\uffff" +
		"\1\110\55\uffff\1\111\35\uffff\64\uffe1\23\uffff\1\122\1\uffff\1\113\64\uffff\1\114" +
		"\75\uffff\1\115\51\uffff\1\116\60\uffff\1\117\64\uffff\1\120\66\uffff\1\121\34\uffff" +
		"\64\uffcd\44\uffff\1\123\47\uffff\1\124\56\uffff\1\125\40\uffff\64\uffcc\26\uffff" +
		"\1\133\4\uffff\1\127\60\uffff\1\130\61\uffff\1\131\73\uffff\1\132\25\uffff\64\uffe3" +
		"\35\uffff\1\152\3\uffff\1\134\61\uffff\1\146\4\uffff\1\135\26\uffff\1\136\114\uffff" +
		"\1\137\46\uffff\1\140\105\uffff\1\141\51\uffff\1\142\60\uffff\1\143\61\uffff\1\144" +
		"\73\uffff\1\145\25\uffff\64\uffe0\27\uffff\1\147\62\uffff\1\150\35\uffff\35\uffdf" +
		"\1\151\26\uffdf\64\uffde\23\uffff\1\153\70\uffff\1\154\42\uffff\1\155\112\uffff\1" +
		"\156\50\uffff\1\157\64\uffff\1\160\67\uffff\1\161\33\uffff\64\uffea\30\uffff\1\167" +
		"\13\uffff\1\163\47\uffff\1\164\77\uffff\1\165\50\uffff\1\166\32\uffff\64\uffcf\26" +
		"\uffff\1\175\10\uffff\1\170\53\uffff\1\171\57\uffff\1\172\66\uffff\1\173\65\uffff" +
		"\1\174\33\uffff\64\uffe9\36\uffff\1\176\25\uffff\26\uffe4\1\177\35\uffe4\34\uffff" +
		"\1\200\55\uffff\1\201\64\uffff\1\202\72\uffff\1\203\53\uffff\1\204\35\uffff\64\uffe5" +
		"\44\uffff\1\206\55\uffff\1\207\63\uffff\1\210\25\uffff\64\uffc8\26\uffff\1\212\65" +
		"\uffff\1\213\75\uffff\1\214\47\uffff\1\215\35\uffff\64\uffe2\26\uffff\1\223\10\uffff" +
		"\1\217\66\uffff\1\220\64\uffff\1\221\43\uffff\1\222\40\uffff\64\uffe7\50\uffff\1" +
		"\224\22\uffff\1\225\113\uffff\1\226\54\uffff\1\227\61\uffff\1\230\35\uffff\64\uffce" +
		"\23\uffff\1\321\1\244\17\uffff\1\232\50\uffff\1\233\60\uffff\1\234\102\uffff\1\235" +
		"\51\uffff\1\236\60\uffff\1\237\61\uffff\1\240\76\uffff\1\241\61\uffff\1\242\32\uffff" +
		"\1\243\55\uffff\64\uffd4\27\uffff\1\267\15\uffff\1\245\52\uffff\1\253\3\uffff\1\246" +
		"\62\uffff\1\247\53\uffff\1\250\62\uffff\1\251\72\uffff\1\252\26\uffff\64\uffd2\26" +
		"\uffff\1\254\76\uffff\1\255\66\uffff\1\256\26\uffff\1\257\114\uffff\1\260\46\uffff" +
		"\1\261\105\uffff\1\262\51\uffff\1\263\60\uffff\1\264\61\uffff\1\265\73\uffff\1\266" +
		"\25\uffff\64\uffd3\34\uffff\1\275\7\uffff\1\270\54\uffff\1\271\63\uffff\1\272\52" +
		"\uffff\1\273\75\uffff\1\274\25\uffff\64\uffe6\26\uffff\1\276\60\uffff\1\277\66\uffff" +
		"\1\300\65\uffff\1\301\64\uffff\1\302\71\uffff\1\303\53\uffff\1\304\73\uffff\1\305" +
		"\61\uffff\1\306\51\uffff\1\307\77\uffff\1\310\62\uffff\1\311\72\uffff\1\312\51\uffff" +
		"\1\313\74\uffff\1\314\47\uffff\1\315\70\uffff\1\316\54\uffff\1\317\65\uffff\1\320" +
		"\33\uffff\64\uffd1\26\uffff\1\322\65\uffff\1\323\64\uffff\1\324\32\uffff\64\uffec" +
		"\6\uffff\1\346\21\uffff\1\332\1\326\65\uffff\1\327\53\uffff\1\330\72\uffff\1\331" +
		"\31\uffff\64\uffdd\30\uffff\1\333\57\uffff\1\334\67\uffff\1\335\100\uffff\1\336\64" +
		"\uffff\1\337\43\uffff\1\340\65\uffff\1\341\102\uffff\1\342\40\uffff\1\343\74\uffff" +
		"\1\344\54\uffff\1\345\35\uffff\64\uffdc\33\uffff\1\347\56\uffff\1\350\73\uffff\1" +
		"\351\50\uffff\1\352\40\uffff\45\uffdb\1\353\16\uffdb\30\uffff\1\354\63\uffff\1\355" +
		"\33\uffff\64\uffda\7\uffff\1\357\77\uffff\1\360\73\uffff\1\361\37\uffff\1\362\77" +
		"\uffff\1\363\40\uffff\64\uffd0\24\uffff\1\370\5\uffff\1\365\64\uffff\1\366\56\uffff" +
		"\1\367\35\uffff\64\uffeb\25\uffff\1\371\64\uffff\1\372\64\uffff\1\373\34\uffff\45" +
		"\uffed\1\374\16\uffed\23\uffff\1\375\104\uffff\1\376\66\uffff\1\377\54\uffff\1\u0100" +
		"\51\uffff\1\u0101\35\uffff\64\uffcb\64\ufff5\41\uffff\1\u0104\62\uffff\1\u0105\67" +
		"\uffff\1\u0106\61\uffff\1\u0107\35\uffff\1\u0108\47\uffff\64\uffd9\27\uffff\1\u010a" +
		"\73\uffff\1\u010b\50\uffff\1\u010c\66\uffff\1\u010d\34\uffff\64\uffca\13\uffc4\1" +
		"\u0110\1\u010f\47\uffc4\64\ufff6\14\uffff\1\u0111\47\uffff\64\ufff7\1\uffff\4\u0112" +
		"\1\u0114\3\u0112\1\u0113\42\u0112\2\uffff\6\u0112\64\ufff8\3\uffff\1\u011f\2\u0112" +
		"\1\u011d\1\u0119\1\u0115\1\u0112\11\uffff\1\u0112\3\uffff\2\u0112\10\uffff\1\u0112" +
		"\2\uffff\1\u0112\1\uffff\2\u0112\2\uffff\1\u0112\7\uffff\1\u011f\4\uffff\1\u0116" +
		"\22\uffff\1\u0116\5\uffff\1\u0116\1\uffff\1\u0116\2\uffff\1\u0116\2\uffff\1\u0116" +
		"\2\uffff\1\u0116\7\uffff\1\u0116\1\uffff\2\u0116\4\uffff\1\u0117\22\uffff\1\u0117" +
		"\5\uffff\1\u0117\1\uffff\1\u0117\2\uffff\1\u0117\2\uffff\1\u0117\2\uffff\1\u0117" +
		"\7\uffff\1\u0117\1\uffff\2\u0117\4\uffff\1\u0118\22\uffff\1\u0118\5\uffff\1\u0118" +
		"\1\uffff\1\u0118\2\uffff\1\u0118\2\uffff\1\u0118\2\uffff\1\u0118\7\uffff\1\u0118" +
		"\1\uffff\2\u0118\4\uffff\1\u0119\22\uffff\1\u0119\5\uffff\1\u0119\1\uffff\1\u0119" +
		"\2\uffff\1\u0119\2\uffff\1\u0119\2\uffff\1\u0119\7\uffff\1\u0119\1\uffff\2\u0119" +
		"\4\uffff\1\u011a\22\uffff\1\u011a\5\uffff\1\u011a\1\uffff\1\u011a\2\uffff\1\u011a" +
		"\2\uffff\1\u011a\2\uffff\1\u011a\7\uffff\1\u011a\1\uffff\2\u011a\4\uffff\1\u011b" +
		"\22\uffff\1\u011b\5\uffff\1\u011b\1\uffff\1\u011b\2\uffff\1\u011b\2\uffff\1\u011b" +
		"\2\uffff\1\u011b\7\uffff\1\u011b\1\uffff\2\u011b\4\uffff\1\u011c\22\uffff\1\u011c" +
		"\5\uffff\1\u011c\1\uffff\1\u011c\2\uffff\1\u011c\2\uffff\1\u011c\2\uffff\1\u011c" +
		"\7\uffff\1\u011c\1\uffff\2\u011c\4\uffff\1\u0112\22\uffff\1\u0112\5\uffff\1\u0112" +
		"\1\uffff\1\u0112\2\uffff\1\u0112\2\uffff\1\u0112\2\uffff\1\u0112\7\uffff\1\u0112" +
		"\1\uffff\2\u0112\4\uffff\1\u011e\22\uffff\1\u011e\5\uffff\1\u011e\1\uffff\1\u011e" +
		"\2\uffff\1\u011e\2\uffff\1\u011e\2\uffff\1\u011e\7\uffff\1\u011e\1\uffff\2\u011e" +
		"\2\uffff\2\u0112\1\u011e\1\u0112\1\u0114\3\u0112\1\u0113\14\u0112\1\u011e\5\u0112" +
		"\1\u011e\1\u0112\1\u011e\2\u0112\1\u011e\2\u0112\1\u011e\2\u0112\1\u011e\4\u0112" +
		"\2\uffff\1\u0112\1\u011e\1\u0112\2\u011e\1\u0112\1\uffff\2\u0112\1\u0120\1\u0112" +
		"\1\u0114\3\u0112\1\u0113\42\u0112\2\uffff\4\u0112\1\u0120\1\u0112\1\uffff\4\u0112" +
		"\1\u0114\3\u0112\1\u0113\42\u0112\2\uffff\6\u0112\3\ufffd\1\u0121\2\ufffd\3\u0121" +
		"\12\ufffd\26\u0121\5\ufffd\5\u0121\1\ufffd\1\uffff\3\u0131\1\uffff\1\u0123\46\u0131" +
		"\2\uffff\6\u0131\3\uffff\1\u012f\2\u0131\1\u012c\1\u0128\1\u0124\1\u0131\11\uffff" +
		"\1\u0131\3\uffff\2\u0131\10\uffff\1\u0131\2\uffff\1\u0131\1\uffff\2\u0131\2\uffff" +
		"\1\u0131\7\uffff\1\u012f\4\uffff\1\u0125\22\uffff\1\u0125\5\uffff\1\u0125\1\uffff" +
		"\1\u0125\2\uffff\1\u0125\2\uffff\1\u0125\2\uffff\1\u0125\7\uffff\1\u0125\1\uffff" +
		"\2\u0125\4\uffff\1\u0126\22\uffff\1\u0126\5\uffff\1\u0126\1\uffff\1\u0126\2\uffff" +
		"\1\u0126\2\uffff\1\u0126\2\uffff\1\u0126\7\uffff\1\u0126\1\uffff\2\u0126\4\uffff" +
		"\1\u0127\22\uffff\1\u0127\5\uffff\1\u0127\1\uffff\1\u0127\2\uffff\1\u0127\2\uffff" +
		"\1\u0127\2\uffff\1\u0127\7\uffff\1\u0127\1\uffff\2\u0127\4\uffff\1\u0128\22\uffff" +
		"\1\u0128\5\uffff\1\u0128\1\uffff\1\u0128\2\uffff\1\u0128\2\uffff\1\u0128\2\uffff" +
		"\1\u0128\7\uffff\1\u0128\1\uffff\2\u0128\4\uffff\1\u0129\22\uffff\1\u0129\5\uffff" +
		"\1\u0129\1\uffff\1\u0129\2\uffff\1\u0129\2\uffff\1\u0129\2\uffff\1\u0129\7\uffff" +
		"\1\u0129\1\uffff\2\u0129\4\uffff\1\u012a\22\uffff\1\u012a\5\uffff\1\u012a\1\uffff" +
		"\1\u012a\2\uffff\1\u012a\2\uffff\1\u012a\2\uffff\1\u012a\7\uffff\1\u012a\1\uffff" +
		"\2\u012a\4\uffff\1\u012b\22\uffff\1\u012b\5\uffff\1\u012b\1\uffff\1\u012b\2\uffff" +
		"\1\u012b\2\uffff\1\u012b\2\uffff\1\u012b\7\uffff\1\u012b\1\uffff\2\u012b\4\uffff" +
		"\1\u0131\22\uffff\1\u0131\5\uffff\1\u0131\1\uffff\1\u0131\2\uffff\1\u0131\2\uffff" +
		"\1\u0131\2\uffff\1\u0131\7\uffff\1\u0131\1\uffff\2\u0131\4\uffff\1\u012d\22\uffff" +
		"\1\u012d\5\uffff\1\u012d\1\uffff\1\u012d\2\uffff\1\u012d\2\uffff\1\u012d\2\uffff" +
		"\1\u012d\7\uffff\1\u012d\1\uffff\2\u012d\4\uffff\1\u012d\1\u012e\21\uffff\1\u012d" +
		"\5\uffff\1\u012d\1\uffff\1\u012d\2\uffff\1\u012d\2\uffff\1\u012d\2\uffff\1\u012d" +
		"\7\uffff\1\u012d\1\uffff\2\u012d\1\uffff\64\ufff9\3\uffff\1\u0130\1\u012e\55\uffff" +
		"\1\u0130\4\uffff\1\u0131\1\u012e\55\uffff\1\u0131\5\uffff\1\u012e\57\uffff\3\ufffb" +
		"\1\1\2\ufffb\1\u0133\50\ufffb\1\1\1\u0133\1\ufffb\1\1\1\ufffb\3\uffff\1\u0134\22" +
		"\uffff\1\u0134\5\uffff\1\u0134\1\uffff\1\u0134\2\uffff\1\u0134\2\uffff\1\u0134\2" +
		"\uffff\1\u0134\7\uffff\1\u0134\1\uffff\2\u0134\1\uffff\3\ufffa\1\u0134\22\ufffa\1" +
		"\u0134\5\ufffa\1\u0134\1\ufffa\1\u0134\2\ufffa\1\u0134\2\ufffa\1\u0134\2\ufffa\1" +
		"\u0134\7\ufffa\1\u0134\1\ufffa\2\u0134\1\ufffa\64\ufffc\1\uffff\3\u016c\1\u015c\4" +
		"\u016c\1\u014c\1\u0146\2\u016c\1\u0141\4\u016c\1\u0139\26\u016c\1\u0138\1\u016c\1" +
		"\u0137\10\u016c\64\uffc3\64\uffb8\5\uffb9\1\u013e\5\uffb9\1\u013b\6\uffb9\1\u013a" +
		"\41\uffb9\1\uffbb\53\u013a\2\uffbb\6\u013a\1\uffff\12\u013b\1\u013c\50\u013b\1\uffff" +
		"\12\u013b\1\u013c\6\u013b\1\u013d\41\u013b\64\uffba\54\uffff\1\u0140\1\u013f\5\uffff" +
		"\1\u013e\5\uffff\1\u013e\5\uffff\1\u013b\6\uffff\1\u013a\116\uffff\1\u013f\6\uffff" +
		"\5\uffb9\1\u0143\6\uffb9\1\u0142\47\uffb9\64\uffb7\54\uffff\1\u0145\1\u0144\5\uffff" +
		"\1\u0143\5\uffff\1\u0143\6\uffff\1\u0142\124\uffff\1\u0144\6\uffff\5\uffb9\1\u0149" +
		"\4\uffb9\1\u0148\2\uffb9\1\u0147\46\uffb9\64\uffb8\64\uffb6\54\uffff\1\u014b\1\u014a" +
		"\5\uffff\1\u0149\5\uffff\1\u0149\4\uffff\1\u0148\2\uffff\1\u0147\123\uffff\1\u014a" +
		"\6\uffff\1\uffb9\4\u015b\1\u014e\3\u015b\1\u014d\43\u015b\1\uffb9\6\u015b\64\uffbc" +
		"\3\uffff\1\u0159\2\u015b\1\u0157\1\u0153\1\u014f\1\u015b\11\uffff\1\u015b\3\uffff" +
		"\2\u015b\10\uffff\1\u015b\2\uffff\1\u015b\1\uffff\2\u015b\2\uffff\1\u015b\7\uffff" +
		"\1\u0159\4\uffff\1\u0150\22\uffff\1\u0150\5\uffff\1\u0150\1\uffff\1\u0150\2\uffff" +
		"\1\u0150\2\uffff\1\u0150\2\uffff\1\u0150\7\uffff\1\u0150\1\uffff\2\u0150\4\uffff" +
		"\1\u0151\22\uffff\1\u0151\5\uffff\1\u0151\1\uffff\1\u0151\2\uffff\1\u0151\2\uffff" +
		"\1\u0151\2\uffff\1\u0151\7\uffff\1\u0151\1\uffff\2\u0151\4\uffff\1\u0152\22\uffff" +
		"\1\u0152\5\uffff\1\u0152\1\uffff\1\u0152\2\uffff\1\u0152\2\uffff\1\u0152\2\uffff" +
		"\1\u0152\7\uffff\1\u0152\1\uffff\2\u0152\4\uffff\1\u0153\22\uffff\1\u0153\5\uffff" +
		"\1\u0153\1\uffff\1\u0153\2\uffff\1\u0153\2\uffff\1\u0153\2\uffff\1\u0153\7\uffff" +
		"\1\u0153\1\uffff\2\u0153\4\uffff\1\u0154\22\uffff\1\u0154\5\uffff\1\u0154\1\uffff" +
		"\1\u0154\2\uffff\1\u0154\2\uffff\1\u0154\2\uffff\1\u0154\7\uffff\1\u0154\1\uffff" +
		"\2\u0154\4\uffff\1\u0155\22\uffff\1\u0155\5\uffff\1\u0155\1\uffff\1\u0155\2\uffff" +
		"\1\u0155\2\uffff\1\u0155\2\uffff\1\u0155\7\uffff\1\u0155\1\uffff\2\u0155\4\uffff" +
		"\1\u0156\22\uffff\1\u0156\5\uffff\1\u0156\1\uffff\1\u0156\2\uffff\1\u0156\2\uffff" +
		"\1\u0156\2\uffff\1\u0156\7\uffff\1\u0156\1\uffff\2\u0156\4\uffff\1\u015b\22\uffff" +
		"\1\u015b\5\uffff\1\u015b\1\uffff\1\u015b\2\uffff\1\u015b\2\uffff\1\u015b\2\uffff" +
		"\1\u015b\7\uffff\1\u015b\1\uffff\2\u015b\4\uffff\1\u0158\22\uffff\1\u0158\5\uffff" +
		"\1\u0158\1\uffff\1\u0158\2\uffff\1\u0158\2\uffff\1\u0158\2\uffff\1\u0158\7\uffff" +
		"\1\u0158\1\uffff\2\u0158\2\uffff\2\u015b\1\u0158\1\u015b\1\u014e\3\u015b\1\u014d" +
		"\14\u015b\1\u0158\5\u015b\1\u0158\1\u015b\1\u0158\2\u015b\1\u0158\2\u015b\1\u0158" +
		"\2\u015b\1\u0158\5\u015b\1\uffff\1\u015b\1\u0158\1\u015b\2\u0158\1\u015b\1\uffff" +
		"\2\u015b\1\u015a\1\u015b\1\u014e\3\u015b\1\u014d\43\u015b\1\uffff\4\u015b\1\u015a" +
		"\1\u015b\1\uffff\4\u015b\1\u014e\3\u015b\1\u014d\43\u015b\1\uffff\6\u015b\1\uffff" +
		"\4\u015b\1\u014e\3\u015b\1\u014d\43\u015b\1\uffff\6\u015b\1\uffb9\3\u016b\1\u016a" +
		"\1\u015d\47\u016b\1\uffb9\6\u016b\3\uffff\1\u0168\2\u016b\1\u0166\1\u0162\1\u015e" +
		"\1\u016b\11\uffff\1\u016b\3\uffff\2\u016b\10\uffff\1\u016b\2\uffff\1\u016b\1\uffff" +
		"\2\u016b\2\uffff\1\u016b\7\uffff\1\u0168\4\uffff\1\u015f\22\uffff\1\u015f\5\uffff" +
		"\1\u015f\1\uffff\1\u015f\2\uffff\1\u015f\2\uffff\1\u015f\2\uffff\1\u015f\7\uffff" +
		"\1\u015f\1\uffff\2\u015f\4\uffff\1\u0160\22\uffff\1\u0160\5\uffff\1\u0160\1\uffff" +
		"\1\u0160\2\uffff\1\u0160\2\uffff\1\u0160\2\uffff\1\u0160\7\uffff\1\u0160\1\uffff" +
		"\2\u0160\4\uffff\1\u0161\22\uffff\1\u0161\5\uffff\1\u0161\1\uffff\1\u0161\2\uffff" +
		"\1\u0161\2\uffff\1\u0161\2\uffff\1\u0161\7\uffff\1\u0161\1\uffff\2\u0161\4\uffff" +
		"\1\u0162\22\uffff\1\u0162\5\uffff\1\u0162\1\uffff\1\u0162\2\uffff\1\u0162\2\uffff" +
		"\1\u0162\2\uffff\1\u0162\7\uffff\1\u0162\1\uffff\2\u0162\4\uffff\1\u0163\22\uffff" +
		"\1\u0163\5\uffff\1\u0163\1\uffff\1\u0163\2\uffff\1\u0163\2\uffff\1\u0163\2\uffff" +
		"\1\u0163\7\uffff\1\u0163\1\uffff\2\u0163\4\uffff\1\u0164\22\uffff\1\u0164\5\uffff" +
		"\1\u0164\1\uffff\1\u0164\2\uffff\1\u0164\2\uffff\1\u0164\2\uffff\1\u0164\7\uffff" +
		"\1\u0164\1\uffff\2\u0164\4\uffff\1\u0165\22\uffff\1\u0165\5\uffff\1\u0165\1\uffff" +
		"\1\u0165\2\uffff\1\u0165\2\uffff\1\u0165\2\uffff\1\u0165\7\uffff\1\u0165\1\uffff" +
		"\2\u0165\4\uffff\1\u016b\22\uffff\1\u016b\5\uffff\1\u016b\1\uffff\1\u016b\2\uffff" +
		"\1\u016b\2\uffff\1\u016b\2\uffff\1\u016b\7\uffff\1\u016b\1\uffff\2\u016b\4\uffff" +
		"\1\u0167\22\uffff\1\u0167\5\uffff\1\u0167\1\uffff\1\u0167\2\uffff\1\u0167\2\uffff" +
		"\1\u0167\2\uffff\1\u0167\7\uffff\1\u0167\1\uffff\2\u0167\2\uffff\2\u016b\1\u0167" +
		"\1\u016a\1\u015d\20\u016b\1\u0167\5\u016b\1\u0167\1\u016b\1\u0167\2\u016b\1\u0167" +
		"\2\u016b\1\u0167\2\u016b\1\u0167\5\u016b\1\uffff\1\u016b\1\u0167\1\u016b\2\u0167" +
		"\1\u016b\1\uffff\2\u016b\1\u0169\1\u016a\1\u015d\47\u016b\1\uffff\4\u016b\1\u0169" +
		"\1\u016b\1\uffff\3\u016b\1\u016a\1\u015d\47\u016b\1\uffff\6\u016b\64\uffbd\1\uffff" +
		"\3\u016b\1\u016a\1\u015d\47\u016b\1\uffff\6\u016b\64\uffb9\1\uffff\3\u016c\1\u015c" +
		"\4\u016c\1\u014c\1\u0146\2\u016c\1\u0141\4\u016c\1\u0139\26\u016c\1\u0138\1\u016c" +
		"\1\u016e\10\u016c\64\uffc2\1\uffff\3\u016c\1\u015c\4\u016c\1\u014c\3\u016c\1\u0170" +
		"\4\u016c\1\u0139\41\u016c\53\uffb9\1\u0171\10\uffb9\64\uffc1\1\uffff\11\u0176\1\u0175" +
		"\1\u0176\1\u0174\30\u0176\1\u0173\16\u0176\1\uffc0\11\u0176\1\uffc0\32\u0176\1\u0173" +
		"\16\u0176\64\uffbe\64\uffbf\1\uffc0\11\u0176\1\uffc0\1\u0176\1\uffc0\30\u0176\1\u0173" +
		"\16\u0176\1\uffff\3\u016c\1\u015c\4\u016c\1\u014c\10\u016c\1\u0139\41\u016c");

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

			if (state == -1) {
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset, token.endoffset);
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
			case 0: // ID: /{letter}({letter}|[0-9\-])*/
				{ if (lookaheadColon()) token.symbol = Tokens.ID_COLON; }
				break;
			case 1: // skip: /:/
				spaceToken = true;
				{
		if (token.offset != foundColonOffset)
			reporter.error("Unexpected colon", token.line, token.offset, token.endoffset);
	}
				break;
			case 8: // '%%': /%%/
				{ if (++sectionCounter == 2) token.symbol = Tokens.eoi; }
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
				{ nesting = 0; lexemeStart = token.offset; }
				break;
			case 55: // skip: /%\?[ \f\r\n\t\v]*\{/
				spaceToken = true;
				state = States.predicate;
				{ nesting = 0; lexemeStart = token.offset; }
				break;
			case 56: // skip: /%\{/
				spaceToken = true;
				state = States.prologue;
				{ nesting = 0; lexemeStart = token.offset; }
				break;
			case 57: // skip: /</
				spaceToken = true;
				state = States.tag;
				{ nesting = 0; lexemeStart = token.offset; }
				break;
			case 58: // '{...}': /\}/
				{
		nesting--;
		if (nesting < 0) {
			setState(States.initial);
			token.offset = lexemeStart;
			token.value = ""; // TODO
		} else {
			spaceToken = true;
		}
	}
				break;
			case 59: // '%?{...}': /\}/
				{ nesting--; if (nesting < 0) { setState(States.initial); token.offset = lexemeStart; } else { spaceToken = true; } }
				break;
			case 60: // '%{...%}': /%\}/
				state = States.initial;
				{ token.offset = lexemeStart; }
				break;
			case 61: // tag_any: /([^<>]|\->)+/
				spaceToken = true;
				break;
			case 62: // tag_inc_nesting: /</
				spaceToken = true;
				{ nesting++; }
				break;
			case 63: // TAG: />/
				{ nesting--; if (nesting < 0) { setState(States.initial); token.offset = lexemeStart; } else { spaceToken = true; } }
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
				{ nesting++; }
				break;
			case 70: // code_dec_nesting: /%{splice}>/
				spaceToken = true;
				{ nesting--; }
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

}
