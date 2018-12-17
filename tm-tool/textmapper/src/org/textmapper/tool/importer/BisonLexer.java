/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
		1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 2, 4, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		2, 1, 5, 1, 1, 6, 1, 7, 1, 1, 8, 1, 1, 9, 10, 11,
		12, 13, 13, 13, 13, 13, 13, 13, 14, 14, 15, 16, 17, 1, 18, 19,
		1, 20, 20, 20, 20, 20, 20, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		10, 10, 10, 10, 10, 21, 10, 10, 22, 10, 10, 23, 24, 25, 1, 10,
		1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 10, 35, 36, 37, 38, 39,
		40, 41, 42, 43, 44, 45, 46, 10, 47, 48, 10, 49, 50, 51
	};

	private static final short tmStateMap[] = {
		0, 310, 365, 367, 370, 375
	};

	private static final short tmBacktracking[] = {
		59, 7, 4, 13, 18, 63, 27, 101, 36, 234, 70, 314, 70, 324, 70, 320,
		70, 342, 70, 328, 70, 344, 70, 363, 70, 349
	};

	private static final int tmFirstRule = -14;

	private static final int[] tmRuleSymbol = unpack_int(74,
		"\uffff\uffff\0\0\2\0\3\0\4\0\4\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\3\0\16" +
		"\0\17\0\20\0\21\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36" +
		"\0\37\0\40\0\41\0\42\0\43\0\44\0\45\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56" +
		"\0\57\0\60\0\61\0\62\0\63\0\64\0\65\0\3\0\3\0\3\0\3\0\66\0\67\0\70\0\71\0\72\0\73" +
		"\0\74\0\75\0\76\0\77\0\100\0\101\0\102\0\103\0");

	private static final int tmClassesCount = 52;

	private static final short[] tmGoto = unpack_vc_short(19552,
		"\1\ufff1\1\ufff2\3\u0135\1\u0126\1\45\1\25\2\ufff2\1\24\1\17\1\14\2\13\1\12\1\11" +
		"\1\5\2\ufff2\3\24\1\4\1\ufff2\1\3\27\24\1\2\1\1\1\ufff2\64\uffe7\64\uffba\64\uffe4" +
		"\64\uffe5\10\uffb7\1\uffff\11\uffb7\1\6\41\uffb7\64\uffe9\22\ufff2\1\10\41\ufff2" +
		"\64\uffea\64\uffe6\64\uffef\14\uffee\3\13\61\uffee\3\13\7\uffee\1\ufffe\30\uffee" +
		"\1\ufffe\4\uffee\14\ufff2\3\16\5\ufff2\1\16\5\ufff2\6\16\24\ufff2\14\uffed\3\16\5" +
		"\uffed\1\16\5\uffed\6\16\24\uffed\10\ufff2\1\21\2\ufff2\1\20\50\ufff2\1\uffe2\2\20" +
		"\2\uffe2\57\20\1\ufff2\7\21\1\22\53\21\1\ufff2\7\21\1\22\2\21\1\23\50\21\64\uffe1" +
		"\11\ufff0\2\24\1\ufff0\3\24\5\ufff0\3\24\3\ufff0\27\24\3\ufff0\1\ufff2\2\44\2\ufff2" +
		"\2\44\1\ufff2\20\44\1\26\33\44\5\ufff2\1\44\1\ufff2\1\44\4\ufff2\2\42\5\ufff2\1\44" +
		"\1\ufff2\1\36\2\ufff2\1\44\1\ufff2\2\44\3\ufff2\1\44\6\ufff2\1\44\3\ufff2\1\44\1" +
		"\ufff2\1\44\1\32\1\44\1\27\20\ufff2\3\30\5\ufff2\1\30\5\ufff2\6\30\33\ufff2\1\31" +
		"\4\ufff2\3\30\5\ufff2\1\30\5\ufff2\6\30\24\ufff2\64\uffec\14\ufff2\3\33\5\ufff2\1" +
		"\33\5\ufff2\6\33\40\ufff2\3\34\5\ufff2\1\34\5\ufff2\6\34\40\ufff2\3\35\5\ufff2\1" +
		"\35\5\ufff2\6\35\40\ufff2\3\44\5\ufff2\1\44\5\ufff2\6\44\40\ufff2\3\37\5\ufff2\1" +
		"\37\5\ufff2\6\37\40\ufff2\3\40\5\ufff2\1\40\5\ufff2\6\40\40\ufff2\3\41\5\ufff2\1" +
		"\41\5\ufff2\6\41\40\ufff2\3\32\5\ufff2\1\32\5\ufff2\6\32\33\ufff2\1\31\4\ufff2\2" +
		"\43\55\ufff2\1\31\4\ufff2\2\44\55\ufff2\1\31\62\ufff2\1\u0125\12\ufff2\1\u011f\1" +
		"\ufff2\1\u011d\10\ufff2\1\u0119\1\375\1\344\1\331\1\317\1\ufff2\1\301\1\ufff2\1\266" +
		"\1\261\1\165\1\157\1\134\1\ufff2\1\121\1\105\1\67\1\62\1\53\1\ufff2\1\47\1\46\2\ufff2" +
		"\64\uffb8\32\ufff2\1\50\65\ufff2\1\51\63\ufff2\1\52\27\ufff2\64\uffbb\36\ufff2\1" +
		"\54\77\ufff2\1\55\44\ufff2\1\56\77\ufff2\1\57\67\ufff2\1\60\46\ufff2\1\61\25\ufff2" +
		"\64\uffbc\46\ufff2\1\63\57\ufff2\1\64\70\ufff2\1\65\62\ufff2\1\66\15\ufff2\64\uffbd" +
		"\47\ufff2\1\73\10\ufff2\1\70\53\ufff2\1\71\51\ufff2\1\72\25\ufff2\64\uffde\43\ufff2" +
		"\1\74\56\ufff2\1\75\73\ufff2\1\76\15\ufff2\11\uffe0\1\ufffd\52\uffe0\54\ufff2\1\100" +
		"\41\ufff2\1\101\64\ufff2\1\102\74\ufff2\1\103\55\ufff2\1\104\25\ufff2\64\uffbe\43" +
		"\ufff2\1\112\10\ufff2\1\106\41\ufff2\1\107\103\ufff2\1\110\65\ufff2\1\111\7\ufff2" +
		"\64\uffbf\36\ufff2\1\113\71\ufff2\1\114\55\ufff2\1\115\101\ufff2\1\116\56\ufff2\1" +
		"\117\62\ufff2\1\120\15\ufff2\64\uffc0\36\ufff2\1\126\3\ufff2\1\122\61\ufff2\1\123" +
		"\64\ufff2\1\124\76\ufff2\1\125\7\ufff2\64\uffda\51\ufff2\1\127\67\ufff2\1\130\50" +
		"\ufff2\1\131\73\ufff2\1\132\47\ufff2\1\133\25\ufff2\64\uffc1\32\ufff2\1\153\17\ufff2" +
		"\1\135\47\ufff2\1\143\3\ufff2\1\136\67\ufff2\1\137\71\ufff2\1\140\45\ufff2\1\141" +
		"\77\ufff2\1\142\11\ufff2\64\uffdc\34\ufff2\1\144\27\ufff2\36\uffd7\1\ufffc\25\uffd7" +
		"\35\ufff2\1\146\64\ufff2\1\147\73\ufff2\1\150\51\ufff2\1\151\65\ufff2\1\152\25\ufff2" +
		"\64\uffd8\52\ufff2\1\154\43\ufff2\1\155\76\ufff2\1\156\16\ufff2\64\uffc2\55\ufff2" +
		"\1\160\62\ufff2\1\161\57\ufff2\1\162\70\ufff2\1\163\62\ufff2\1\164\7\ufff2\64\uffc3" +
		"\32\ufff2\1\247\14\ufff2\1\172\4\ufff2\1\166\45\ufff2\1\167\77\ufff2\1\170\56\ufff2" +
		"\1\171\16\ufff2\64\uffdf\11\ufff2\1\225\34\ufff2\1\173\47\ufff2\1\220\2\ufff2\1\174" +
		"\64\ufff2\1\175\101\ufff2\1\176\45\ufff2\1\177\77\ufff2\1\200\56\ufff2\1\201\60\ufff2" +
		"\1\202\67\ufff2\1\203\57\ufff2\1\204\74\ufff2\1\205\64\ufff2\1\206\51\ufff2\1\207" +
		"\55\ufff2\1\210\40\ufff2\1\211\122\ufff2\1\212\45\ufff2\1\213\103\ufff2\1\214\64" +
		"\ufff2\1\215\46\ufff2\1\216\77\ufff2\1\217\11\ufff2\64\uffc4\53\ufff2\1\221\63\ufff2" +
		"\1\222\57\ufff2\1\223\50\ufff2\1\224\27\ufff2\64\uffd9\35\ufff2\1\233\6\ufff2\1\226" +
		"\61\ufff2\1\227\67\ufff2\1\230\53\ufff2\1\231\100\ufff2\1\232\10\ufff2\64\uffc5\36" +
		"\ufff2\1\234\64\ufff2\1\235\56\ufff2\1\236\106\ufff2\1\237\52\ufff2\1\240\73\ufff2" +
		"\1\241\20\ufff2\1\242\122\ufff2\1\243\65\ufff2\1\244\47\ufff2\1\245\61\ufff2\1\246" +
		"\27\ufff2\64\uffc6\45\ufff2\1\250\54\ufff2\1\251\36\ufff2\1\252\122\ufff2\1\253\65" +
		"\ufff2\1\254\47\ufff2\1\255\64\ufff2\1\256\66\ufff2\1\257\100\ufff2\1\260\4\ufff2" +
		"\64\uffc7\36\ufff2\1\262\77\ufff2\1\263\51\ufff2\1\264\61\ufff2\1\265\25\ufff2\64" +
		"\uffd5\32\ufff2\1\272\3\ufff2\1\267\64\ufff2\1\270\100\ufff2\1\271\7\ufff2\64\uffdb" +
		"\46\ufff2\1\273\55\ufff2\1\274\100\ufff2\1\275\40\ufff2\1\276\71\ufff2\1\277\61\ufff2" +
		"\1\300\25\ufff2\64\uffc8\46\ufff2\1\302\57\ufff2\1\303\75\ufff2\1\304\51\ufff2\1" +
		"\305\53\ufff2\1\306\75\ufff2\1\307\30\ufff2\1\310\104\ufff2\1\311\65\ufff2\1\312" +
		"\103\ufff2\1\313\51\ufff2\1\314\70\ufff2\1\315\62\ufff2\1\316\15\ufff2\64\uffc9\44" +
		"\ufff2\1\320\71\ufff2\1\321\22\ufff2\1\322\122\ufff2\1\323\45\ufff2\1\324\103\ufff2" +
		"\1\325\64\ufff2\1\326\46\ufff2\1\327\77\ufff2\1\330\11\ufff2\64\uffca\42\ufff2\1" +
		"\332\65\ufff2\1\333\55\ufff2\1\334\36\ufff2\1\335\122\ufff2\1\336\65\ufff2\1\337" +
		"\47\ufff2\1\340\64\ufff2\1\341\66\ufff2\1\342\100\ufff2\1\343\4\ufff2\64\uffcb\45" +
		"\ufff2\1\371\4\ufff2\1\355\4\ufff2\1\345\54\ufff2\1\346\51\ufff2\1\347\61\ufff2\1" +
		"\350\103\ufff2\1\351\7\ufff2\11\uffce\1\ufffb\52\uffce\52\ufff2\1\353\63\ufff2\1" +
		"\354\11\ufff2\64\uffcd\52\ufff2\1\356\60\ufff2\1\357\66\ufff2\1\360\22\ufff2\1\361" +
		"\130\ufff2\1\362\43\ufff2\1\363\77\ufff2\1\364\44\ufff2\1\365\77\ufff2\1\366\67\ufff2" +
		"\1\367\46\ufff2\1\370\25\ufff2\64\uffcf\50\ufff2\1\372\67\ufff2\1\373\67\ufff2\1" +
		"\374\3\ufff2\64\uffd0\36\ufff2\1\u0102\11\ufff2\1\376\65\ufff2\1\377\47\ufff2\1\u0100" +
		"\61\ufff2\1\u0101\27\ufff2\64\uffd6\37\ufff2\1\u010b\13\ufff2\1\u0103\64\ufff2\1" +
		"\u0104\61\ufff2\1\u0105\66\ufff2\1\u0106\42\ufff2\1\u0107\103\ufff2\1\u0108\56\ufff2" +
		"\1\u0109\66\ufff2\1\u010a\11\ufff2\64\uffdd\32\ufff2\1\u0110\7\ufff2\1\u010c\67\ufff2" +
		"\1\u010d\53\ufff2\1\u010e\25\ufff2\53\uffd2\1\u010f\10\uffd2\64\uffd1\55\ufff2\1" +
		"\u0111\52\ufff2\1\u0112\73\ufff2\1\u0113\20\ufff2\1\u0114\122\ufff2\1\u0115\65\ufff2" +
		"\1\u0116\47\ufff2\1\u0117\61\ufff2\1\u0118\27\ufff2\64\uffd3\47\ufff2\1\u011a\51" +
		"\ufff2\1\u011b\64\ufff2\1\u011c\25\ufff2\64\uffd4\2\ufff2\3\u011d\54\ufff2\1\u011e" +
		"\2\ufff2\64\uffb9\37\ufff2\1\u0120\70\ufff2\1\u0121\51\ufff2\1\u0122\71\ufff2\1\u0123" +
		"\45\ufff2\1\u0124\41\ufff2\64\uffcc\64\uffe8\1\ufff2\2\u0126\2\ufff2\1\u0134\22\u0126" +
		"\1\u0127\33\u0126\5\ufff2\1\u0126\1\ufff2\1\u0126\4\ufff2\2\u0132\5\ufff2\1\u0126" +
		"\1\ufff2\1\u012e\2\ufff2\1\u0126\1\ufff2\2\u0126\3\ufff2\1\u0126\6\ufff2\1\u0126" +
		"\3\ufff2\1\u0126\1\ufff2\1\u0126\1\u012a\1\u0126\1\u0128\20\ufff2\3\u0129\5\ufff2" +
		"\1\u0129\5\ufff2\6\u0129\25\ufff2\2\u0126\2\ufff2\1\u0134\6\u0126\3\u0129\5\u0126" +
		"\1\u0129\3\u0126\1\u0127\1\u0126\6\u0129\24\u0126\14\ufff2\3\u012b\5\ufff2\1\u012b" +
		"\5\ufff2\6\u012b\40\ufff2\3\u012c\5\ufff2\1\u012c\5\ufff2\6\u012c\40\ufff2\3\u012d" +
		"\5\ufff2\1\u012d\5\ufff2\6\u012d\40\ufff2\3\u0126\5\ufff2\1\u0126\5\ufff2\6\u0126" +
		"\40\ufff2\3\u012f\5\ufff2\1\u012f\5\ufff2\6\u012f\40\ufff2\3\u0130\5\ufff2\1\u0130" +
		"\5\ufff2\6\u0130\40\ufff2\3\u0131\5\ufff2\1\u0131\5\ufff2\6\u0131\40\ufff2\3\u012a" +
		"\5\ufff2\1\u012a\5\ufff2\6\u012a\25\ufff2\2\u0126\2\ufff2\1\u0134\6\u0126\2\u0133" +
		"\12\u0126\1\u0127\33\u0126\1\ufff2\2\u0126\2\ufff2\1\u0134\22\u0126\1\u0127\33\u0126" +
		"\64\uffeb\2\uffe3\3\u0135\57\uffe3\1\ufff2\4\u016c\1\u015c\1\u0157\1\u0147\3\u016c" +
		"\1\u013f\5\u016c\1\u0139\37\u016c\1\u0138\1\u016c\1\u0137\64\uffb6\64\uffab\6\uffac" +
		"\1\u013e\12\uffac\1\u013d\6\uffac\1\ufffa\33\uffac\2\ufff2\1\u013a\1\u013c\1\u013b" +
		"\62\ufff2\1\u013c\66\ufff2\1\u013e\12\ufff2\1\u013d\6\ufff2\1\u013a\33\ufff2\64\uffa9" +
		"\64\uffab\10\uffac\1\ufff9\2\uffac\1\u0143\14\uffac\1\ufff8\33\uffac\2\ufff2\1\u0140" +
		"\1\u0142\1\u0141\62\ufff2\1\u0142\70\ufff2\1\u0144\2\ufff2\1\u0143\14\ufff2\1\u0140" +
		"\33\ufff2\1\uffae\2\u0143\2\uffae\57\u0143\1\ufff2\7\u0144\1\u0145\53\u0144\1\ufff2" +
		"\7\u0144\1\u0145\2\u0144\1\u0146\50\u0144\64\uffad\1\uffac\2\ufff7\1\uffac\3\ufff7" +
		"\1\u0155\20\ufff7\1\ufff6\33\ufff7\5\ufff2\1\u0156\1\ufff2\1\u0156\4\ufff2\2\u0153" +
		"\5\ufff2\1\u0156\1\ufff2\1\u014f\2\ufff2\1\u0156\1\ufff2\2\u0156\3\ufff2\1\u0156" +
		"\6\ufff2\1\u0156\3\ufff2\1\u0156\1\ufff2\1\u0156\1\u014b\1\u0156\1\u0149\20\ufff2" +
		"\3\u014a\5\ufff2\1\u014a\5\ufff2\6\u014a\25\ufff2\2\u0156\1\ufff2\3\u0156\1\u0155" +
		"\4\u0156\3\u014a\5\u0156\1\u014a\3\u0156\1\u0148\1\u0156\6\u014a\24\u0156\14\ufff2" +
		"\3\u014c\5\ufff2\1\u014c\5\ufff2\6\u014c\40\ufff2\3\u014d\5\ufff2\1\u014d\5\ufff2" +
		"\6\u014d\40\ufff2\3\u014e\5\ufff2\1\u014e\5\ufff2\6\u014e\40\ufff2\3\u0156\5\ufff2" +
		"\1\u0156\5\ufff2\6\u0156\40\ufff2\3\u0150\5\ufff2\1\u0150\5\ufff2\6\u0150\40\ufff2" +
		"\3\u0151\5\ufff2\1\u0151\5\ufff2\6\u0151\40\ufff2\3\u0152\5\ufff2\1\u0152\5\ufff2" +
		"\6\u0152\40\ufff2\3\u014b\5\ufff2\1\u014b\5\ufff2\6\u014b\25\ufff2\2\u0156\1\ufff2" +
		"\3\u0156\1\u0155\4\u0156\2\u0154\12\u0156\1\u0148\33\u0156\1\ufff2\2\u0156\1\ufff2" +
		"\3\u0156\1\u0155\20\u0156\1\u0148\33\u0156\64\uffb0\1\ufff2\2\u0156\1\ufff2\3\u0156" +
		"\1\u0155\20\u0156\1\u0148\33\u0156\22\uffac\1\u015b\5\uffac\1\ufff5\33\uffac\2\ufff2" +
		"\1\u0158\1\u015a\1\u0159\62\ufff2\1\u015a\102\ufff2\1\u015b\5\ufff2\1\u0158\33\ufff2" +
		"\64\uffaa\1\uffac\2\ufff4\1\uffac\1\ufff4\1\u016a\22\ufff4\1\ufff3\33\ufff4\5\ufff2" +
		"\1\u016b\1\ufff2\1\u016b\4\ufff2\2\u0168\5\ufff2\1\u016b\1\ufff2\1\u0164\2\ufff2" +
		"\1\u016b\1\ufff2\2\u016b\3\ufff2\1\u016b\6\ufff2\1\u016b\3\ufff2\1\u016b\1\ufff2" +
		"\1\u016b\1\u0160\1\u016b\1\u015e\20\ufff2\3\u015f\5\ufff2\1\u015f\5\ufff2\6\u015f" +
		"\25\ufff2\2\u016b\1\ufff2\1\u016b\1\u016a\6\u016b\3\u015f\5\u016b\1\u015f\3\u016b" +
		"\1\u015d\1\u016b\6\u015f\24\u016b\14\ufff2\3\u0161\5\ufff2\1\u0161\5\ufff2\6\u0161" +
		"\40\ufff2\3\u0162\5\ufff2\1\u0162\5\ufff2\6\u0162\40\ufff2\3\u0163\5\ufff2\1\u0163" +
		"\5\ufff2\6\u0163\40\ufff2\3\u016b\5\ufff2\1\u016b\5\ufff2\6\u016b\40\ufff2\3\u0165" +
		"\5\ufff2\1\u0165\5\ufff2\6\u0165\40\ufff2\3\u0166\5\ufff2\1\u0166\5\ufff2\6\u0166" +
		"\40\ufff2\3\u0167\5\ufff2\1\u0167\5\ufff2\6\u0167\40\ufff2\3\u0160\5\ufff2\1\u0160" +
		"\5\ufff2\6\u0160\25\ufff2\2\u016b\1\ufff2\1\u016b\1\u016a\6\u016b\2\u0169\12\u016b" +
		"\1\u015d\33\u016b\1\ufff2\2\u016b\1\ufff2\1\u016b\1\u016a\22\u016b\1\u015d\33\u016b" +
		"\64\uffaf\1\ufff2\2\u016b\1\ufff2\1\u016b\1\u016a\22\u016b\1\u015d\33\u016b\64\uffac" +
		"\1\ufff2\4\u016c\1\u015c\1\u0157\1\u0147\3\u016c\1\u013f\5\u016c\1\u0139\37\u016c" +
		"\1\u0138\1\u016c\1\u016e\64\uffb5\1\ufff2\4\u016c\1\u015c\1\u0170\1\u0147\3\u016c" +
		"\1\u013f\50\u016c\63\uffac\1\u0171\64\uffb4\1\ufff2\10\u0176\1\u0175\7\u0176\1\u0174" +
		"\1\u0173\41\u0176\64\uffb1\64\uffb2\1\uffb3\10\u0176\1\u0175\7\u0176\1\uffb3\42\u0176" +
		"\1\uffb3\10\u0176\1\u0175\7\u0176\2\uffb3\41\u0176\1\ufff2\4\u016c\1\u015c\1\u016c" +
		"\1\u0147\3\u016c\1\u013f\50\u016c");

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
			case 2: // ID: /{letter}({letter}|[0-9\-])*/
				{ if (lookaheadColon()) token.symbol = Tokens.ID_COLON; }
				break;
			case 3: // skip: /:/
				spaceToken = true;
				{
		if (token.offset != foundColonOffset)
			reporter.error("Unexpected colon", token.line, token.offset, token.endoffset);
	}
				break;
			case 10: // '%%': /%%/
				{ if (++sectionCounter == 2) token.symbol = Tokens.eoi; }
				break;
			case 15: // skip: /[\r\n\t\f\v ]+/
				spaceToken = true;
				break;
			case 16: // skip_comment: /\/\/[^\r\n]*/
				spaceToken = true;
				break;
			case 17: // skip_ml_comment: /\/\*([^*]|\*+[^\/*])*\*+\//
				spaceToken = true;
				break;
			case 56: // skip: /\{/
				spaceToken = true;
				{ state = States.bracedCode; nesting = 0; lexemeStart = token.offset; }
				break;
			case 57: // skip: /%\?[ \f\r\n\t\v]*\{/
				spaceToken = true;
				{ state = States.predicate; nesting = 0; lexemeStart = token.offset; }
				break;
			case 58: // skip: /%\{/
				spaceToken = true;
				{ state = States.prologue; nesting = 0; lexemeStart = token.offset; }
				break;
			case 59: // skip: /</
				spaceToken = true;
				{ state = States.tag; nesting = 0; lexemeStart = token.offset; }
				break;
			case 60: // '{...}': /\}/
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
			case 61: // '%?{...}': /\}/
				{ nesting--; if (nesting < 0) { setState(States.initial); token.offset = lexemeStart; } else { spaceToken = true; } }
				break;
			case 62: // '%{...%}': /%\}/
				{ state = States.initial; token.offset = lexemeStart; }
				break;
			case 63: // tag_any: /([^<>]|\->)+/
				spaceToken = true;
				break;
			case 64: // tag_inc_nesting: /</
				spaceToken = true;
				{ nesting++; }
				break;
			case 65: // TAG: />/
				{ nesting--; if (nesting < 0) { setState(States.initial); token.offset = lexemeStart; } else { spaceToken = true; } }
				break;
			case 66: // code_char: /'([^'\n\\]|{escape})*'/
				spaceToken = true;
				break;
			case 67: // code_string: /"([^"\n\\]|{escape})*"/
				spaceToken = true;
				break;
			case 68: // code_comment: /\/{splice}\/[^\r\n]*/
				spaceToken = true;
				break;
			case 69: // code_ml_comment: /\/{splice}\*([^*]|\*+[^\/*])*\*+\//
				spaceToken = true;
				break;
			case 70: // code_any: /.|\n/
				spaceToken = true;
				break;
			case 71: // code_inc_nesting: /\{|<{splice}%/
				spaceToken = true;
				{ nesting++; }
				break;
			case 72: // code_dec_nesting: /%{splice}>/
				spaceToken = true;
				{ nesting--; }
				break;
			case 73: // code_lessless: /<{splice}</
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
