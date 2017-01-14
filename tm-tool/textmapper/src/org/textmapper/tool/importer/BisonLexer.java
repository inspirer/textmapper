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

	private static final short tmBacktracking[] = {
		27, 127, 36, 235, 18, 252, 59, 272, 4, 307, 70, 318, 70, 315, 70, 323,
		70, 329, 70, 347, 70, 334, 70, 363, 70, 349
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
		"\1\ufff1\1\ufff2\1\u0135\1\u0132\1\u0122\1\ufff2\3\u0121\1\u0112\1\u010e\2\ufff2" +
		"\1\15\1\14\1\13\1\12\1\11\1\4\22\u0121\1\ufff2\3\u0121\1\3\2\ufff2\2\2\1\u0121\1" +
		"\1\2\u0121\1\1\1\2\3\uffee\1\1\53\uffee\1\1\2\uffee\1\1\1\uffee\54\uffe3\2\2\5\uffe3" +
		"\1\2\64\uffba\13\ufff2\1\6\6\ufff2\1\5\41\ufff2\1\uffe2\53\5\2\uffe2\6\5\1\ufff2" +
		"\12\6\1\7\50\6\1\ufff2\12\6\1\7\6\6\1\10\41\6\64\uffe1\64\uffe4\64\uffe5\64\uffe6" +
		"\64\uffe7\7\ufff2\1\u0109\2\ufff2\1\u0103\2\ufff2\1\u0102\5\ufff2\1\364\1\356\1\ufff2" +
		"\1\325\1\231\1\216\1\211\1\205\1\162\1\126\1\112\1\106\1\70\1\55\1\42\1\30\3\ufff2" +
		"\1\21\2\ufff2\1\20\1\16\62\ufff2\1\17\2\ufff2\2\16\5\ufff2\1\16\64\uffb9\64\uffb8" +
		"\26\ufff2\1\22\65\ufff2\1\23\102\ufff2\1\24\40\ufff2\1\25\74\ufff2\1\26\54\ufff2" +
		"\1\27\35\ufff2\64\uffbc\40\ufff2\1\31\53\ufff2\1\32\100\ufff2\1\33\51\ufff2\1\34" +
		"\74\ufff2\1\35\47\ufff2\1\36\70\ufff2\1\37\54\ufff2\1\40\65\ufff2\1\41\33\ufff2\64" +
		"\uffca\37\ufff2\1\43\64\ufff2\1\44\51\ufff2\1\45\102\ufff2\1\46\51\ufff2\1\47\60" +
		"\ufff2\1\50\61\ufff2\1\51\76\ufff2\1\52\61\ufff2\1\53\32\ufff2\1\54\55\ufff2\64\uffcb" +
		"\26\ufff2\1\65\15\ufff2\1\56\46\ufff2\1\57\76\ufff2\1\60\30\ufff2\1\61\120\ufff2" +
		"\1\62\61\ufff2\1\63\47\ufff2\1\64\35\ufff2\64\uffc8\41\ufff2\1\66\45\ufff2\1\67\40" +
		"\ufff2\64\uffdb\27\ufff2\1\71\73\ufff2\1\72\47\ufff2\1\73\77\ufff2\1\74\70\ufff2" +
		"\1\75\57\ufff2\1\76\70\ufff2\1\77\62\ufff2\1\100\55\ufff2\1\101\50\ufff2\1\102\77" +
		"\ufff2\1\103\50\ufff2\1\104\66\ufff2\1\105\34\ufff2\64\uffc9\24\ufff2\1\107\73\ufff2" +
		"\1\110\55\ufff2\1\111\35\ufff2\64\uffd4\23\ufff2\1\122\1\ufff2\1\113\64\ufff2\1\114" +
		"\75\ufff2\1\115\51\ufff2\1\116\60\ufff2\1\117\64\ufff2\1\120\66\ufff2\1\121\34\ufff2" +
		"\64\uffc0\44\ufff2\1\123\47\ufff2\1\124\56\ufff2\1\125\40\ufff2\64\uffbf\26\ufff2" +
		"\1\133\4\ufff2\1\127\60\ufff2\1\130\61\ufff2\1\131\73\ufff2\1\132\25\ufff2\64\uffd6" +
		"\35\ufff2\1\152\3\ufff2\1\134\61\ufff2\1\146\4\ufff2\1\135\26\ufff2\1\136\114\ufff2" +
		"\1\137\46\ufff2\1\140\105\ufff2\1\141\51\ufff2\1\142\60\ufff2\1\143\61\ufff2\1\144" +
		"\73\ufff2\1\145\25\ufff2\64\uffd3\27\ufff2\1\147\62\ufff2\1\150\35\ufff2\35\uffd2" +
		"\1\151\26\uffd2\64\uffd1\23\ufff2\1\153\70\ufff2\1\154\42\ufff2\1\155\112\ufff2\1" +
		"\156\50\ufff2\1\157\64\ufff2\1\160\67\ufff2\1\161\33\ufff2\64\uffdd\30\ufff2\1\167" +
		"\13\ufff2\1\163\47\ufff2\1\164\77\ufff2\1\165\50\ufff2\1\166\32\ufff2\64\uffc2\26" +
		"\ufff2\1\175\10\ufff2\1\170\53\ufff2\1\171\57\ufff2\1\172\66\ufff2\1\173\65\ufff2" +
		"\1\174\33\ufff2\64\uffdc\36\ufff2\1\176\25\ufff2\26\uffd7\1\uffff\35\uffd7\34\ufff2" +
		"\1\200\55\ufff2\1\201\64\ufff2\1\202\72\ufff2\1\203\53\ufff2\1\204\35\ufff2\64\uffd8" +
		"\44\ufff2\1\206\55\ufff2\1\207\63\ufff2\1\210\25\ufff2\64\uffbb\26\ufff2\1\212\65" +
		"\ufff2\1\213\75\ufff2\1\214\47\ufff2\1\215\35\ufff2\64\uffd5\26\ufff2\1\223\10\ufff2" +
		"\1\217\66\ufff2\1\220\64\ufff2\1\221\43\ufff2\1\222\40\ufff2\64\uffda\50\ufff2\1" +
		"\224\22\ufff2\1\225\113\ufff2\1\226\54\ufff2\1\227\61\ufff2\1\230\35\ufff2\64\uffc1" +
		"\23\ufff2\1\321\1\244\17\ufff2\1\232\50\ufff2\1\233\60\ufff2\1\234\102\ufff2\1\235" +
		"\51\ufff2\1\236\60\ufff2\1\237\61\ufff2\1\240\76\ufff2\1\241\61\ufff2\1\242\32\ufff2" +
		"\1\243\55\ufff2\64\uffc7\27\ufff2\1\267\15\ufff2\1\245\52\ufff2\1\253\3\ufff2\1\246" +
		"\62\ufff2\1\247\53\ufff2\1\250\62\ufff2\1\251\72\ufff2\1\252\26\ufff2\64\uffc5\26" +
		"\ufff2\1\254\76\ufff2\1\255\66\ufff2\1\256\26\ufff2\1\257\114\ufff2\1\260\46\ufff2" +
		"\1\261\105\ufff2\1\262\51\ufff2\1\263\60\ufff2\1\264\61\ufff2\1\265\73\ufff2\1\266" +
		"\25\ufff2\64\uffc6\34\ufff2\1\275\7\ufff2\1\270\54\ufff2\1\271\63\ufff2\1\272\52" +
		"\ufff2\1\273\75\ufff2\1\274\25\ufff2\64\uffd9\26\ufff2\1\276\60\ufff2\1\277\66\ufff2" +
		"\1\300\65\ufff2\1\301\64\ufff2\1\302\71\ufff2\1\303\53\ufff2\1\304\73\ufff2\1\305" +
		"\61\ufff2\1\306\51\ufff2\1\307\77\ufff2\1\310\62\ufff2\1\311\72\ufff2\1\312\51\ufff2" +
		"\1\313\74\ufff2\1\314\47\ufff2\1\315\70\ufff2\1\316\54\ufff2\1\317\65\ufff2\1\320" +
		"\33\ufff2\64\uffc4\26\ufff2\1\322\65\ufff2\1\323\64\ufff2\1\324\32\ufff2\64\uffdf" +
		"\6\ufff2\1\346\21\ufff2\1\332\1\326\65\ufff2\1\327\53\ufff2\1\330\72\ufff2\1\331" +
		"\31\ufff2\64\uffd0\30\ufff2\1\333\57\ufff2\1\334\67\ufff2\1\335\100\ufff2\1\336\64" +
		"\ufff2\1\337\43\ufff2\1\340\65\ufff2\1\341\102\ufff2\1\342\40\ufff2\1\343\74\ufff2" +
		"\1\344\54\ufff2\1\345\35\ufff2\64\uffcf\33\ufff2\1\347\56\ufff2\1\350\73\ufff2\1" +
		"\351\50\ufff2\1\352\40\ufff2\45\uffce\1\ufffe\16\uffce\30\ufff2\1\354\63\ufff2\1" +
		"\355\33\ufff2\64\uffcd\7\ufff2\1\357\77\ufff2\1\360\73\ufff2\1\361\37\ufff2\1\362" +
		"\77\ufff2\1\363\40\ufff2\64\uffc3\24\ufff2\1\370\5\ufff2\1\365\64\ufff2\1\366\56" +
		"\ufff2\1\367\35\ufff2\64\uffde\25\ufff2\1\371\64\ufff2\1\372\64\ufff2\1\373\34\ufff2" +
		"\45\uffe0\1\ufffd\16\uffe0\23\ufff2\1\375\104\ufff2\1\376\66\ufff2\1\377\54\ufff2" +
		"\1\u0100\51\ufff2\1\u0101\35\ufff2\64\uffbe\64\uffe8\41\ufff2\1\u0104\62\ufff2\1" +
		"\u0105\67\ufff2\1\u0106\61\ufff2\1\u0107\35\ufff2\1\u0108\47\ufff2\64\uffcc\27\ufff2" +
		"\1\u010a\73\ufff2\1\u010b\50\ufff2\1\u010c\66\ufff2\1\u010d\34\ufff2\64\uffbd\13" +
		"\uffb7\1\ufffc\1\u010f\47\uffb7\64\uffe9\14\ufff2\1\u0111\47\ufff2\64\uffea\1\ufff2" +
		"\4\u0112\1\u0114\3\u0112\1\u0113\42\u0112\2\ufff2\6\u0112\64\uffeb\3\ufff2\1\u011f" +
		"\2\u0112\1\u011d\1\u0119\1\u0115\1\u0112\11\ufff2\1\u0112\3\ufff2\2\u0112\10\ufff2" +
		"\1\u0112\2\ufff2\1\u0112\1\ufff2\2\u0112\2\ufff2\1\u0112\7\ufff2\1\u011f\4\ufff2" +
		"\1\u0116\22\ufff2\1\u0116\5\ufff2\1\u0116\1\ufff2\1\u0116\2\ufff2\1\u0116\2\ufff2" +
		"\1\u0116\2\ufff2\1\u0116\7\ufff2\1\u0116\1\ufff2\2\u0116\4\ufff2\1\u0117\22\ufff2" +
		"\1\u0117\5\ufff2\1\u0117\1\ufff2\1\u0117\2\ufff2\1\u0117\2\ufff2\1\u0117\2\ufff2" +
		"\1\u0117\7\ufff2\1\u0117\1\ufff2\2\u0117\4\ufff2\1\u0118\22\ufff2\1\u0118\5\ufff2" +
		"\1\u0118\1\ufff2\1\u0118\2\ufff2\1\u0118\2\ufff2\1\u0118\2\ufff2\1\u0118\7\ufff2" +
		"\1\u0118\1\ufff2\2\u0118\4\ufff2\1\u0119\22\ufff2\1\u0119\5\ufff2\1\u0119\1\ufff2" +
		"\1\u0119\2\ufff2\1\u0119\2\ufff2\1\u0119\2\ufff2\1\u0119\7\ufff2\1\u0119\1\ufff2" +
		"\2\u0119\4\ufff2\1\u011a\22\ufff2\1\u011a\5\ufff2\1\u011a\1\ufff2\1\u011a\2\ufff2" +
		"\1\u011a\2\ufff2\1\u011a\2\ufff2\1\u011a\7\ufff2\1\u011a\1\ufff2\2\u011a\4\ufff2" +
		"\1\u011b\22\ufff2\1\u011b\5\ufff2\1\u011b\1\ufff2\1\u011b\2\ufff2\1\u011b\2\ufff2" +
		"\1\u011b\2\ufff2\1\u011b\7\ufff2\1\u011b\1\ufff2\2\u011b\4\ufff2\1\u011c\22\ufff2" +
		"\1\u011c\5\ufff2\1\u011c\1\ufff2\1\u011c\2\ufff2\1\u011c\2\ufff2\1\u011c\2\ufff2" +
		"\1\u011c\7\ufff2\1\u011c\1\ufff2\2\u011c\4\ufff2\1\u0112\22\ufff2\1\u0112\5\ufff2" +
		"\1\u0112\1\ufff2\1\u0112\2\ufff2\1\u0112\2\ufff2\1\u0112\2\ufff2\1\u0112\7\ufff2" +
		"\1\u0112\1\ufff2\2\u0112\4\ufff2\1\u011e\22\ufff2\1\u011e\5\ufff2\1\u011e\1\ufff2" +
		"\1\u011e\2\ufff2\1\u011e\2\ufff2\1\u011e\2\ufff2\1\u011e\7\ufff2\1\u011e\1\ufff2" +
		"\2\u011e\2\ufff2\2\u0112\1\u011e\1\u0112\1\u0114\3\u0112\1\u0113\14\u0112\1\u011e" +
		"\5\u0112\1\u011e\1\u0112\1\u011e\2\u0112\1\u011e\2\u0112\1\u011e\2\u0112\1\u011e" +
		"\4\u0112\2\ufff2\1\u0112\1\u011e\1\u0112\2\u011e\1\u0112\1\ufff2\2\u0112\1\u0120" +
		"\1\u0112\1\u0114\3\u0112\1\u0113\42\u0112\2\ufff2\4\u0112\1\u0120\1\u0112\1\ufff2" +
		"\4\u0112\1\u0114\3\u0112\1\u0113\42\u0112\2\ufff2\6\u0112\3\ufff0\1\u0121\2\ufff0" +
		"\3\u0121\12\ufff0\26\u0121\5\ufff0\5\u0121\1\ufff0\1\ufff2\3\u0131\1\ufff2\1\u0123" +
		"\46\u0131\2\ufff2\6\u0131\3\ufff2\1\u012f\2\u0131\1\u012c\1\u0128\1\u0124\1\u0131" +
		"\11\ufff2\1\u0131\3\ufff2\2\u0131\10\ufff2\1\u0131\2\ufff2\1\u0131\1\ufff2\2\u0131" +
		"\2\ufff2\1\u0131\7\ufff2\1\u012f\4\ufff2\1\u0125\22\ufff2\1\u0125\5\ufff2\1\u0125" +
		"\1\ufff2\1\u0125\2\ufff2\1\u0125\2\ufff2\1\u0125\2\ufff2\1\u0125\7\ufff2\1\u0125" +
		"\1\ufff2\2\u0125\4\ufff2\1\u0126\22\ufff2\1\u0126\5\ufff2\1\u0126\1\ufff2\1\u0126" +
		"\2\ufff2\1\u0126\2\ufff2\1\u0126\2\ufff2\1\u0126\7\ufff2\1\u0126\1\ufff2\2\u0126" +
		"\4\ufff2\1\u0127\22\ufff2\1\u0127\5\ufff2\1\u0127\1\ufff2\1\u0127\2\ufff2\1\u0127" +
		"\2\ufff2\1\u0127\2\ufff2\1\u0127\7\ufff2\1\u0127\1\ufff2\2\u0127\4\ufff2\1\u0128" +
		"\22\ufff2\1\u0128\5\ufff2\1\u0128\1\ufff2\1\u0128\2\ufff2\1\u0128\2\ufff2\1\u0128" +
		"\2\ufff2\1\u0128\7\ufff2\1\u0128\1\ufff2\2\u0128\4\ufff2\1\u0129\22\ufff2\1\u0129" +
		"\5\ufff2\1\u0129\1\ufff2\1\u0129\2\ufff2\1\u0129\2\ufff2\1\u0129\2\ufff2\1\u0129" +
		"\7\ufff2\1\u0129\1\ufff2\2\u0129\4\ufff2\1\u012a\22\ufff2\1\u012a\5\ufff2\1\u012a" +
		"\1\ufff2\1\u012a\2\ufff2\1\u012a\2\ufff2\1\u012a\2\ufff2\1\u012a\7\ufff2\1\u012a" +
		"\1\ufff2\2\u012a\4\ufff2\1\u012b\22\ufff2\1\u012b\5\ufff2\1\u012b\1\ufff2\1\u012b" +
		"\2\ufff2\1\u012b\2\ufff2\1\u012b\2\ufff2\1\u012b\7\ufff2\1\u012b\1\ufff2\2\u012b" +
		"\4\ufff2\1\u0131\22\ufff2\1\u0131\5\ufff2\1\u0131\1\ufff2\1\u0131\2\ufff2\1\u0131" +
		"\2\ufff2\1\u0131\2\ufff2\1\u0131\7\ufff2\1\u0131\1\ufff2\2\u0131\4\ufff2\1\u012d" +
		"\22\ufff2\1\u012d\5\ufff2\1\u012d\1\ufff2\1\u012d\2\ufff2\1\u012d\2\ufff2\1\u012d" +
		"\2\ufff2\1\u012d\7\ufff2\1\u012d\1\ufff2\2\u012d\4\ufff2\1\u012d\1\u012e\21\ufff2" +
		"\1\u012d\5\ufff2\1\u012d\1\ufff2\1\u012d\2\ufff2\1\u012d\2\ufff2\1\u012d\2\ufff2" +
		"\1\u012d\7\ufff2\1\u012d\1\ufff2\2\u012d\1\ufff2\64\uffec\3\ufff2\1\u0130\1\u012e" +
		"\55\ufff2\1\u0130\4\ufff2\1\u0131\1\u012e\55\ufff2\1\u0131\5\ufff2\1\u012e\57\ufff2" +
		"\3\uffee\1\1\2\uffee\1\ufffb\50\uffee\1\1\1\ufffb\1\uffee\1\1\1\uffee\3\ufff2\1\u0134" +
		"\22\ufff2\1\u0134\5\ufff2\1\u0134\1\ufff2\1\u0134\2\ufff2\1\u0134\2\ufff2\1\u0134" +
		"\2\ufff2\1\u0134\7\ufff2\1\u0134\1\ufff2\2\u0134\1\ufff2\3\uffed\1\u0134\22\uffed" +
		"\1\u0134\5\uffed\1\u0134\1\uffed\1\u0134\2\uffed\1\u0134\2\uffed\1\u0134\2\uffed" +
		"\1\u0134\7\uffed\1\u0134\1\uffed\2\u0134\1\uffed\64\uffef\1\ufff2\3\u016c\1\u015c" +
		"\4\u016c\1\u014c\1\u0146\2\u016c\1\u0141\4\u016c\1\u0139\26\u016c\1\u0138\1\u016c" +
		"\1\u0137\10\u016c\64\uffb6\64\uffab\5\uffac\1\ufffa\5\uffac\1\ufff9\6\uffac\1\u013a" +
		"\41\uffac\1\uffae\53\u013a\2\uffae\6\u013a\1\ufff2\12\u013b\1\u013c\50\u013b\1\ufff2" +
		"\12\u013b\1\u013c\6\u013b\1\u013d\41\u013b\64\uffad\54\ufff2\1\u0140\1\u013f\5\ufff2" +
		"\1\u013e\5\ufff2\1\u013e\5\ufff2\1\u013b\6\ufff2\1\u013a\116\ufff2\1\u013f\6\ufff2" +
		"\5\uffac\1\ufff8\6\uffac\1\u0142\47\uffac\64\uffaa\54\ufff2\1\u0145\1\u0144\5\ufff2" +
		"\1\u0143\5\ufff2\1\u0143\6\ufff2\1\u0142\124\ufff2\1\u0144\6\ufff2\5\uffac\1\ufff7" +
		"\4\uffac\1\u0148\2\uffac\1\u0147\46\uffac\64\uffab\64\uffa9\54\ufff2\1\u014b\1\u014a" +
		"\5\ufff2\1\u0149\5\ufff2\1\u0149\4\ufff2\1\u0148\2\ufff2\1\u0147\123\ufff2\1\u014a" +
		"\6\ufff2\1\uffac\4\ufff6\1\ufff5\3\ufff6\1\u014d\43\ufff6\1\uffac\6\ufff6\64\uffaf" +
		"\3\ufff2\1\u0159\2\u015b\1\u0157\1\u0153\1\u014f\1\u015b\11\ufff2\1\u015b\3\ufff2" +
		"\2\u015b\10\ufff2\1\u015b\2\ufff2\1\u015b\1\ufff2\2\u015b\2\ufff2\1\u015b\7\ufff2" +
		"\1\u0159\4\ufff2\1\u0150\22\ufff2\1\u0150\5\ufff2\1\u0150\1\ufff2\1\u0150\2\ufff2" +
		"\1\u0150\2\ufff2\1\u0150\2\ufff2\1\u0150\7\ufff2\1\u0150\1\ufff2\2\u0150\4\ufff2" +
		"\1\u0151\22\ufff2\1\u0151\5\ufff2\1\u0151\1\ufff2\1\u0151\2\ufff2\1\u0151\2\ufff2" +
		"\1\u0151\2\ufff2\1\u0151\7\ufff2\1\u0151\1\ufff2\2\u0151\4\ufff2\1\u0152\22\ufff2" +
		"\1\u0152\5\ufff2\1\u0152\1\ufff2\1\u0152\2\ufff2\1\u0152\2\ufff2\1\u0152\2\ufff2" +
		"\1\u0152\7\ufff2\1\u0152\1\ufff2\2\u0152\4\ufff2\1\u0153\22\ufff2\1\u0153\5\ufff2" +
		"\1\u0153\1\ufff2\1\u0153\2\ufff2\1\u0153\2\ufff2\1\u0153\2\ufff2\1\u0153\7\ufff2" +
		"\1\u0153\1\ufff2\2\u0153\4\ufff2\1\u0154\22\ufff2\1\u0154\5\ufff2\1\u0154\1\ufff2" +
		"\1\u0154\2\ufff2\1\u0154\2\ufff2\1\u0154\2\ufff2\1\u0154\7\ufff2\1\u0154\1\ufff2" +
		"\2\u0154\4\ufff2\1\u0155\22\ufff2\1\u0155\5\ufff2\1\u0155\1\ufff2\1\u0155\2\ufff2" +
		"\1\u0155\2\ufff2\1\u0155\2\ufff2\1\u0155\7\ufff2\1\u0155\1\ufff2\2\u0155\4\ufff2" +
		"\1\u0156\22\ufff2\1\u0156\5\ufff2\1\u0156\1\ufff2\1\u0156\2\ufff2\1\u0156\2\ufff2" +
		"\1\u0156\2\ufff2\1\u0156\7\ufff2\1\u0156\1\ufff2\2\u0156\4\ufff2\1\u015b\22\ufff2" +
		"\1\u015b\5\ufff2\1\u015b\1\ufff2\1\u015b\2\ufff2\1\u015b\2\ufff2\1\u015b\2\ufff2" +
		"\1\u015b\7\ufff2\1\u015b\1\ufff2\2\u015b\4\ufff2\1\u0158\22\ufff2\1\u0158\5\ufff2" +
		"\1\u0158\1\ufff2\1\u0158\2\ufff2\1\u0158\2\ufff2\1\u0158\2\ufff2\1\u0158\7\ufff2" +
		"\1\u0158\1\ufff2\2\u0158\2\ufff2\2\u015b\1\u0158\1\u015b\1\u014e\3\u015b\1\u014d" +
		"\14\u015b\1\u0158\5\u015b\1\u0158\1\u015b\1\u0158\2\u015b\1\u0158\2\u015b\1\u0158" +
		"\2\u015b\1\u0158\5\u015b\1\ufff2\1\u015b\1\u0158\1\u015b\2\u0158\1\u015b\1\ufff2" +
		"\2\u015b\1\u015a\1\u015b\1\u014e\3\u015b\1\u014d\43\u015b\1\ufff2\4\u015b\1\u015a" +
		"\1\u015b\1\ufff2\4\u015b\1\u014e\3\u015b\1\u014d\43\u015b\1\ufff2\6\u015b\1\ufff2" +
		"\4\u015b\1\u014e\3\u015b\1\u014d\43\u015b\1\ufff2\6\u015b\1\uffac\3\ufff4\1\u016a" +
		"\1\ufff3\47\ufff4\1\uffac\6\ufff4\3\ufff2\1\u0168\2\u016b\1\u0166\1\u0162\1\u015e" +
		"\1\u016b\11\ufff2\1\u016b\3\ufff2\2\u016b\10\ufff2\1\u016b\2\ufff2\1\u016b\1\ufff2" +
		"\2\u016b\2\ufff2\1\u016b\7\ufff2\1\u0168\4\ufff2\1\u015f\22\ufff2\1\u015f\5\ufff2" +
		"\1\u015f\1\ufff2\1\u015f\2\ufff2\1\u015f\2\ufff2\1\u015f\2\ufff2\1\u015f\7\ufff2" +
		"\1\u015f\1\ufff2\2\u015f\4\ufff2\1\u0160\22\ufff2\1\u0160\5\ufff2\1\u0160\1\ufff2" +
		"\1\u0160\2\ufff2\1\u0160\2\ufff2\1\u0160\2\ufff2\1\u0160\7\ufff2\1\u0160\1\ufff2" +
		"\2\u0160\4\ufff2\1\u0161\22\ufff2\1\u0161\5\ufff2\1\u0161\1\ufff2\1\u0161\2\ufff2" +
		"\1\u0161\2\ufff2\1\u0161\2\ufff2\1\u0161\7\ufff2\1\u0161\1\ufff2\2\u0161\4\ufff2" +
		"\1\u0162\22\ufff2\1\u0162\5\ufff2\1\u0162\1\ufff2\1\u0162\2\ufff2\1\u0162\2\ufff2" +
		"\1\u0162\2\ufff2\1\u0162\7\ufff2\1\u0162\1\ufff2\2\u0162\4\ufff2\1\u0163\22\ufff2" +
		"\1\u0163\5\ufff2\1\u0163\1\ufff2\1\u0163\2\ufff2\1\u0163\2\ufff2\1\u0163\2\ufff2" +
		"\1\u0163\7\ufff2\1\u0163\1\ufff2\2\u0163\4\ufff2\1\u0164\22\ufff2\1\u0164\5\ufff2" +
		"\1\u0164\1\ufff2\1\u0164\2\ufff2\1\u0164\2\ufff2\1\u0164\2\ufff2\1\u0164\7\ufff2" +
		"\1\u0164\1\ufff2\2\u0164\4\ufff2\1\u0165\22\ufff2\1\u0165\5\ufff2\1\u0165\1\ufff2" +
		"\1\u0165\2\ufff2\1\u0165\2\ufff2\1\u0165\2\ufff2\1\u0165\7\ufff2\1\u0165\1\ufff2" +
		"\2\u0165\4\ufff2\1\u016b\22\ufff2\1\u016b\5\ufff2\1\u016b\1\ufff2\1\u016b\2\ufff2" +
		"\1\u016b\2\ufff2\1\u016b\2\ufff2\1\u016b\7\ufff2\1\u016b\1\ufff2\2\u016b\4\ufff2" +
		"\1\u0167\22\ufff2\1\u0167\5\ufff2\1\u0167\1\ufff2\1\u0167\2\ufff2\1\u0167\2\ufff2" +
		"\1\u0167\2\ufff2\1\u0167\7\ufff2\1\u0167\1\ufff2\2\u0167\2\ufff2\2\u016b\1\u0167" +
		"\1\u016a\1\u015d\20\u016b\1\u0167\5\u016b\1\u0167\1\u016b\1\u0167\2\u016b\1\u0167" +
		"\2\u016b\1\u0167\2\u016b\1\u0167\5\u016b\1\ufff2\1\u016b\1\u0167\1\u016b\2\u0167" +
		"\1\u016b\1\ufff2\2\u016b\1\u0169\1\u016a\1\u015d\47\u016b\1\ufff2\4\u016b\1\u0169" +
		"\1\u016b\1\ufff2\3\u016b\1\u016a\1\u015d\47\u016b\1\ufff2\6\u016b\64\uffb0\1\ufff2" +
		"\3\u016b\1\u016a\1\u015d\47\u016b\1\ufff2\6\u016b\64\uffac\1\ufff2\3\u016c\1\u015c" +
		"\4\u016c\1\u014c\1\u0146\2\u016c\1\u0141\4\u016c\1\u0139\26\u016c\1\u0138\1\u016c" +
		"\1\u016e\10\u016c\64\uffb5\1\ufff2\3\u016c\1\u015c\4\u016c\1\u014c\3\u016c\1\u0170" +
		"\4\u016c\1\u0139\41\u016c\53\uffac\1\u0171\10\uffac\64\uffb4\1\ufff2\11\u0176\1\u0175" +
		"\1\u0176\1\u0174\30\u0176\1\u0173\16\u0176\1\uffb3\11\u0176\1\uffb3\32\u0176\1\u0173" +
		"\16\u0176\64\uffb1\64\uffb2\1\uffb3\11\u0176\1\uffb3\1\u0176\1\uffb3\30\u0176\1\u0173" +
		"\16\u0176\1\ufff2\3\u016c\1\u015c\4\u016c\1\u014c\10\u016c\1\u0139\41\u016c");

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
				state = States.bracedCode;
				{ nesting = 0; lexemeStart = token.offset; }
				break;
			case 57: // skip: /%\?[ \f\r\n\t\v]*\{/
				spaceToken = true;
				state = States.predicate;
				{ nesting = 0; lexemeStart = token.offset; }
				break;
			case 58: // skip: /%\{/
				spaceToken = true;
				state = States.prologue;
				{ nesting = 0; lexemeStart = token.offset; }
				break;
			case 59: // skip: /</
				spaceToken = true;
				state = States.tag;
				{ nesting = 0; lexemeStart = token.offset; }
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
				state = States.initial;
				{ token.offset = lexemeStart; }
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
