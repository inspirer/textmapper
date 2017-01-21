/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
package org.textmapper.tool.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ArrayList;
import org.textmapper.tool.parser.TMLexer.ErrorReporter;
import org.textmapper.tool.parser.TMLexer.Tokens;
import org.textmapper.tool.parser.TMTree.TextSource;
import org.textmapper.tool.parser.ast.*;
import org.textmapper.tool.parser.TMLexer.Span;

public class TMParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public TMParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	private static final int[] tmAction = TMLexer.unpack_int(414,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\317\0\320\0\uffb9\uffff\327\0\uff6b" +
		"\uffff\321\0\322\0\uffff\uffff\302\0\301\0\305\0\324\0\ufefb\uffff\ufef3\uffff\ufee7" +
		"\uffff\307\0\ufea3\uffff\uffff\uffff\ufe9d\uffff\20\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\330\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\304" +
		"\0\uffff\uffff\uffff\uffff\17\0\254\0\ufe59\uffff\ufe51\uffff\uffff\uffff\256\0\ufe4b" +
		"\uffff\uffff\uffff\uffff\uffff\7\0\325\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufe0b\uffff\4\0\16\0\306\0\263\0\264\0\uffff\uffff\uffff\uffff\261\0\uffff" +
		"\uffff\ufe05\uffff\uffff\uffff\313\0\ufdff\uffff\uffff\uffff\14\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\2\0\22\0\271\0\262\0\270\0\255\0\uffff\uffff\uffff" +
		"\uffff\303\0\uffff\uffff\12\0\13\0\uffff\uffff\uffff\uffff\ufdf9\uffff\ufdf1\uffff" +
		"\ufdeb\uffff\25\0\32\0\33\0\34\0\30\0\31\0\15\0\uffff\uffff\316\0\312\0\6\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\55\0\uffff\uffff\54\0\uffff\uffff\23\0\332\0\uffff" +
		"\uffff\26\0\27\0\uffff\uffff\uffff\uffff\uffff\uffff\ufda3\uffff\57\0\62\0\63\0\64" +
		"\0\ufd5d\uffff\uffff\uffff\241\0\uffff\uffff\56\0\uffff\uffff\47\0\uffff\uffff\uffff" +
		"\uffff\52\0\24\0\35\0\ufd15\uffff\uffff\uffff\104\0\105\0\106\0\uffff\uffff\uffff" +
		"\uffff\110\0\107\0\111\0\267\0\266\0\uffff\uffff\uffff\uffff\uffff\uffff\ufccb\uffff" +
		"\245\0\ufc81\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufc29\uffff\ufbe7\uffff\101" +
		"\0\102\0\uffff\uffff\uffff\uffff\60\0\61\0\240\0\uffff\uffff\uffff\uffff\50\0\uffff" +
		"\uffff\51\0\53\0\ufba5\uffff\ufb57\uffff\uffff\uffff\124\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\127\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\ufb4f\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufaf7\uffff\uffff" +
		"\uffff\326\0\uffff\uffff\220\0\ufa8b\uffff\uffff\uffff\134\0\ufa83\uffff\ufa2d\uffff" +
		"\346\0\uf9d7\uffff\uf9cd\uffff\uf975\uffff\174\0\173\0\170\0\203\0\205\0\uf919\uffff" +
		"\171\0\uf8bb\uffff\uf85b\uffff\227\0\uffff\uffff\172\0\156\0\155\0\uf7f7\uffff\uffff" +
		"\uffff\247\0\251\0\uf7b5\uffff\75\0\342\0\uf773\uffff\uf76d\uffff\uf767\uffff\uf70f" +
		"\uffff\uffff\uffff\uf6b7\uffff\uffff\uffff\uffff\uffff\45\0\46\0\334\0\uf65f\uffff" +
		"\125\0\117\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\116\0\130\0\uffff\uffff" +
		"\uffff\uffff\115\0\243\0\uffff\uffff\uffff\uffff\202\0\uffff\uffff\uf613\uffff\276" +
		"\0\uffff\uffff\uffff\uffff\uf607\uffff\uffff\uffff\201\0\uffff\uffff\176\0\uffff" +
		"\uffff\uf5af\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf557\uffff\74" +
		"\0\uf4fd\uffff\uf4a7\uffff\uf49d\uffff\145\0\uf445\uffff\uf43b\uffff\uffff\uffff" +
		"\151\0\154\0\uf3e3\uffff\uf3d9\uffff\167\0\153\0\uffff\uffff\211\0\uffff\uffff\224" +
		"\0\225\0\160\0\204\0\uf37d\uffff\uffff\uffff\250\0\uf375\uffff\uffff\uffff\344\0" +
		"\77\0\100\0\uffff\uffff\uffff\uffff\uf36f\uffff\uffff\uffff\uf317\uffff\uf2bf\uffff" +
		"\uffff\uffff\uffff\uffff\336\0\uf267\uffff\123\0\uffff\uffff\120\0\121\0\uffff\uffff" +
		"\113\0\uffff\uffff\161\0\162\0\272\0\uffff\uffff\uffff\uffff\uffff\uffff\157\0\uffff" +
		"\uffff\221\0\uffff\uffff\200\0\177\0\uffff\uffff\uffff\uffff\164\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uf21d\uffff\232\0\235\0\uffff\uffff\uffff\uffff\206\0\uf1d7\uffff" +
		"\207\0\133\0\uf173\uffff\uf169\uffff\141\0\144\0\uf111\uffff\143\0\150\0\uf107\uffff" +
		"\147\0\152\0\uf0fd\uffff\213\0\214\0\uffff\uffff\246\0\76\0\131\0\uf0a1\uffff\73" +
		"\0\72\0\uffff\uffff\70\0\uffff\uffff\uffff\uffff\uf09b\uffff\41\0\42\0\43\0\44\0" +
		"\uffff\uffff\340\0\36\0\122\0\uffff\uffff\114\0\274\0\275\0\uf043\uffff\uf03b\uffff" +
		"\uffff\uffff\175\0\163\0\226\0\uffff\uffff\234\0\231\0\uffff\uffff\230\0\uffff\uffff" +
		"\140\0\uf033\uffff\137\0\142\0\146\0\252\0\uffff\uffff\71\0\67\0\66\0\uffff\uffff" +
		"\40\0\112\0\uffff\uffff\233\0\uf029\uffff\uf021\uffff\136\0\132\0\65\0\223\0\222" +
		"\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4068,
		"\10\0\1\0\46\0\1\0\47\0\1\0\55\0\1\0\57\0\1\0\62\0\1\0\63\0\1\0\64\0\1\0\65\0\1\0" +
		"\66\0\1\0\67\0\1\0\70\0\1\0\71\0\1\0\72\0\1\0\73\0\1\0\74\0\1\0\75\0\1\0\76\0\1\0" +
		"\77\0\1\0\100\0\1\0\101\0\1\0\102\0\1\0\103\0\1\0\104\0\1\0\105\0\1\0\106\0\1\0\107" +
		"\0\1\0\110\0\1\0\111\0\1\0\112\0\1\0\113\0\1\0\114\0\1\0\115\0\1\0\uffff\uffff\ufffe" +
		"\uffff\1\0\uffff\uffff\2\0\uffff\uffff\22\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff" +
		"\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\57\0\uffff\uffff\50\0\uffff\uffff\51\0\uffff\uffff\52\0\uffff\uffff\23\0\311\0\uffff" +
		"\uffff\ufffe\uffff\32\0\uffff\uffff\0\0\21\0\6\0\21\0\10\0\21\0\11\0\21\0\16\0\21" +
		"\0\17\0\21\0\20\0\21\0\21\0\21\0\23\0\21\0\24\0\21\0\25\0\21\0\27\0\21\0\30\0\21" +
		"\0\34\0\21\0\35\0\21\0\37\0\21\0\42\0\21\0\44\0\21\0\45\0\21\0\46\0\21\0\47\0\21" +
		"\0\53\0\21\0\54\0\21\0\56\0\21\0\57\0\21\0\62\0\21\0\63\0\21\0\64\0\21\0\65\0\21" +
		"\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0\72\0\21\0\73\0\21\0\74\0\21\0\75\0\21" +
		"\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21\0\102\0\21\0\103\0\21\0\104\0\21\0\105" +
		"\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111\0\21\0\112\0\21\0\113\0\21\0\114\0\21" +
		"\0\115\0\21\0\116\0\21\0\uffff\uffff\ufffe\uffff\24\0\uffff\uffff\113\0\uffff\uffff" +
		"\16\0\331\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\16\0\323\0\24\0\323\0\27\0\323" +
		"\0\113\0\323\0\uffff\uffff\ufffe\uffff\55\0\uffff\uffff\10\0\5\0\46\0\5\0\47\0\5" +
		"\0\57\0\5\0\62\0\5\0\63\0\5\0\64\0\5\0\65\0\5\0\66\0\5\0\67\0\5\0\70\0\5\0\71\0\5" +
		"\0\72\0\5\0\73\0\5\0\74\0\5\0\75\0\5\0\76\0\5\0\77\0\5\0\100\0\5\0\101\0\5\0\102" +
		"\0\5\0\103\0\5\0\104\0\5\0\105\0\5\0\106\0\5\0\107\0\5\0\110\0\5\0\111\0\5\0\112" +
		"\0\5\0\113\0\5\0\114\0\5\0\115\0\5\0\uffff\uffff\ufffe\uffff\20\0\uffff\uffff\23" +
		"\0\310\0\uffff\uffff\ufffe\uffff\35\0\uffff\uffff\41\0\uffff\uffff\47\0\uffff\uffff" +
		"\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff" +
		"\uffff\33\0\260\0\uffff\uffff\ufffe\uffff\21\0\uffff\uffff\20\0\265\0\33\0\265\0" +
		"\uffff\uffff\ufffe\uffff\20\0\uffff\uffff\33\0\257\0\uffff\uffff\ufffe\uffff\47\0" +
		"\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff" +
		"\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\57\0\uffff\uffff\27\0\315\0\uffff\uffff\ufffe\uffff\10\0\uffff\uffff\0\0\3\0\uffff" +
		"\uffff\ufffe\uffff\20\0\uffff\uffff\27\0\314\0\uffff\uffff\ufffe\uffff\113\0\uffff" +
		"\uffff\16\0\331\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff\21\0\17\0\116\0\17\0\uffff" +
		"\uffff\ufffe\uffff\116\0\uffff\uffff\21\0\333\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\22\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0" +
		"\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff" +
		"\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\0\0\10\0\10" +
		"\0\10\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\57\0\uffff\uffff\0\0\11\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff" +
		"\21\0\242\0\24\0\242\0\44\0\242\0\47\0\242\0\56\0\242\0\57\0\242\0\62\0\242\0\63" +
		"\0\242\0\64\0\242\0\65\0\242\0\66\0\242\0\67\0\242\0\70\0\242\0\71\0\242\0\72\0\242" +
		"\0\73\0\242\0\74\0\242\0\75\0\242\0\76\0\242\0\77\0\242\0\100\0\242\0\101\0\242\0" +
		"\102\0\242\0\103\0\242\0\104\0\242\0\105\0\242\0\106\0\242\0\107\0\242\0\110\0\242" +
		"\0\111\0\242\0\112\0\242\0\113\0\242\0\114\0\242\0\115\0\242\0\uffff\uffff\ufffe" +
		"\uffff\120\0\uffff\uffff\0\0\37\0\6\0\37\0\10\0\37\0\22\0\37\0\46\0\37\0\47\0\37" +
		"\0\57\0\37\0\62\0\37\0\63\0\37\0\64\0\37\0\65\0\37\0\66\0\37\0\67\0\37\0\70\0\37" +
		"\0\71\0\37\0\72\0\37\0\73\0\37\0\74\0\37\0\75\0\37\0\76\0\37\0\77\0\37\0\100\0\37" +
		"\0\101\0\37\0\102\0\37\0\103\0\37\0\104\0\37\0\105\0\37\0\106\0\37\0\107\0\37\0\110" +
		"\0\37\0\111\0\37\0\112\0\37\0\113\0\37\0\114\0\37\0\115\0\37\0\uffff\uffff\ufffe" +
		"\uffff\13\0\uffff\uffff\21\0\244\0\24\0\244\0\44\0\244\0\45\0\244\0\47\0\244\0\56" +
		"\0\244\0\57\0\244\0\62\0\244\0\63\0\244\0\64\0\244\0\65\0\244\0\66\0\244\0\67\0\244" +
		"\0\70\0\244\0\71\0\244\0\72\0\244\0\73\0\244\0\74\0\244\0\75\0\244\0\76\0\244\0\77" +
		"\0\244\0\100\0\244\0\101\0\244\0\102\0\244\0\103\0\244\0\104\0\244\0\105\0\244\0" +
		"\106\0\244\0\107\0\244\0\110\0\244\0\111\0\244\0\112\0\244\0\113\0\244\0\114\0\244" +
		"\0\115\0\244\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff" +
		"\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\uffff\uffff\ufffe\uffff\47\0\uffff\uffff\115\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff" +
		"\7\0\343\0\21\0\343\0\uffff\uffff\ufffe\uffff\47\0\uffff\uffff\115\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\7\0\343\0\21" +
		"\0\343\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0\0\335\0\6\0\335\0\10\0\335\0\22" +
		"\0\335\0\24\0\335\0\46\0\335\0\47\0\335\0\57\0\335\0\62\0\335\0\63\0\335\0\64\0\335" +
		"\0\65\0\335\0\66\0\335\0\67\0\335\0\70\0\335\0\71\0\335\0\72\0\335\0\73\0\335\0\74" +
		"\0\335\0\75\0\335\0\76\0\335\0\77\0\335\0\100\0\335\0\101\0\335\0\102\0\335\0\103" +
		"\0\335\0\104\0\335\0\105\0\335\0\106\0\335\0\107\0\335\0\110\0\335\0\111\0\335\0" +
		"\112\0\335\0\113\0\335\0\114\0\335\0\115\0\335\0\116\0\335\0\uffff\uffff\ufffe\uffff" +
		"\103\0\uffff\uffff\16\0\126\0\20\0\126\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\116\0\uffff\uffff\11\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff" +
		"\32\0\uffff\uffff\13\0\17\0\21\0\17\0\36\0\17\0\6\0\21\0\11\0\21\0\16\0\21\0\17\0" +
		"\21\0\24\0\21\0\25\0\21\0\27\0\21\0\30\0\21\0\34\0\21\0\35\0\21\0\37\0\21\0\42\0" +
		"\21\0\44\0\21\0\45\0\21\0\46\0\21\0\47\0\21\0\53\0\21\0\54\0\21\0\56\0\21\0\57\0" +
		"\21\0\62\0\21\0\63\0\21\0\64\0\21\0\65\0\21\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0" +
		"\21\0\72\0\21\0\73\0\21\0\74\0\21\0\75\0\21\0\76\0\21\0\77\0\21\0\100\0\21\0\101" +
		"\0\21\0\102\0\21\0\103\0\21\0\104\0\21\0\105\0\21\0\106\0\21\0\107\0\21\0\110\0\21" +
		"\0\111\0\21\0\112\0\21\0\113\0\21\0\114\0\21\0\115\0\21\0\116\0\21\0\uffff\uffff" +
		"\ufffe\uffff\11\0\uffff\uffff\16\0\135\0\27\0\135\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\17\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17" +
		"\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\11" +
		"\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff" +
		"\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\42\0\uffff\uffff" +
		"\6\0\165\0\11\0\165\0\16\0\165\0\17\0\165\0\24\0\165\0\25\0\165\0\27\0\165\0\30\0" +
		"\165\0\44\0\165\0\45\0\165\0\46\0\165\0\47\0\165\0\53\0\165\0\56\0\165\0\57\0\165" +
		"\0\62\0\165\0\63\0\165\0\64\0\165\0\65\0\165\0\66\0\165\0\67\0\165\0\70\0\165\0\71" +
		"\0\165\0\72\0\165\0\73\0\165\0\74\0\165\0\75\0\165\0\76\0\165\0\77\0\165\0\100\0" +
		"\165\0\101\0\165\0\102\0\165\0\103\0\165\0\104\0\165\0\105\0\165\0\106\0\165\0\107" +
		"\0\165\0\110\0\165\0\111\0\165\0\112\0\165\0\113\0\165\0\114\0\165\0\115\0\165\0" +
		"\116\0\165\0\uffff\uffff\ufffe\uffff\37\0\uffff\uffff\6\0\210\0\11\0\210\0\16\0\210" +
		"\0\17\0\210\0\24\0\210\0\25\0\210\0\27\0\210\0\30\0\210\0\42\0\210\0\44\0\210\0\45" +
		"\0\210\0\46\0\210\0\47\0\210\0\53\0\210\0\56\0\210\0\57\0\210\0\62\0\210\0\63\0\210" +
		"\0\64\0\210\0\65\0\210\0\66\0\210\0\67\0\210\0\70\0\210\0\71\0\210\0\72\0\210\0\73" +
		"\0\210\0\74\0\210\0\75\0\210\0\76\0\210\0\77\0\210\0\100\0\210\0\101\0\210\0\102" +
		"\0\210\0\103\0\210\0\104\0\210\0\105\0\210\0\106\0\210\0\107\0\210\0\110\0\210\0" +
		"\111\0\210\0\112\0\210\0\113\0\210\0\114\0\210\0\115\0\210\0\116\0\210\0\uffff\uffff" +
		"\ufffe\uffff\54\0\uffff\uffff\6\0\212\0\11\0\212\0\16\0\212\0\17\0\212\0\24\0\212" +
		"\0\25\0\212\0\27\0\212\0\30\0\212\0\37\0\212\0\42\0\212\0\44\0\212\0\45\0\212\0\46" +
		"\0\212\0\47\0\212\0\53\0\212\0\56\0\212\0\57\0\212\0\62\0\212\0\63\0\212\0\64\0\212" +
		"\0\65\0\212\0\66\0\212\0\67\0\212\0\70\0\212\0\71\0\212\0\72\0\212\0\73\0\212\0\74" +
		"\0\212\0\75\0\212\0\76\0\212\0\77\0\212\0\100\0\212\0\101\0\212\0\102\0\212\0\103" +
		"\0\212\0\104\0\212\0\105\0\212\0\106\0\212\0\107\0\212\0\110\0\212\0\111\0\212\0" +
		"\112\0\212\0\113\0\212\0\114\0\212\0\115\0\212\0\116\0\212\0\uffff\uffff\ufffe\uffff" +
		"\34\0\uffff\uffff\35\0\uffff\uffff\6\0\216\0\11\0\216\0\16\0\216\0\17\0\216\0\24" +
		"\0\216\0\25\0\216\0\27\0\216\0\30\0\216\0\37\0\216\0\42\0\216\0\44\0\216\0\45\0\216" +
		"\0\46\0\216\0\47\0\216\0\53\0\216\0\54\0\216\0\56\0\216\0\57\0\216\0\62\0\216\0\63" +
		"\0\216\0\64\0\216\0\65\0\216\0\66\0\216\0\67\0\216\0\70\0\216\0\71\0\216\0\72\0\216" +
		"\0\73\0\216\0\74\0\216\0\75\0\216\0\76\0\216\0\77\0\216\0\100\0\216\0\101\0\216\0" +
		"\102\0\216\0\103\0\216\0\104\0\216\0\105\0\216\0\106\0\216\0\107\0\216\0\110\0\216" +
		"\0\111\0\216\0\112\0\216\0\113\0\216\0\114\0\216\0\115\0\216\0\116\0\216\0\uffff" +
		"\uffff\ufffe\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\20\0\17\0\33\0\17\0\uffff\uffff" +
		"\ufffe\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff" +
		"\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff" +
		"\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101" +
		"\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff" +
		"\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\57\0\uffff\uffff\7\0\343\0\21\0\343\0\uffff\uffff\ufffe\uffff\21" +
		"\0\uffff\uffff\7\0\345\0\uffff\uffff\ufffe\uffff\21\0\uffff\uffff\7\0\345\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff" +
		"\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff\uffff\11\0\347\0" +
		"\16\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff" +
		"\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff" +
		"\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\116\0\uffff\uffff\11\0\347\0\16\0\347\0\uffff\uffff\ufffe\uffff\24\0\uffff\uffff" +
		"\0\0\337\0\6\0\337\0\10\0\337\0\22\0\337\0\46\0\337\0\47\0\337\0\57\0\337\0\62\0" +
		"\337\0\63\0\337\0\64\0\337\0\65\0\337\0\66\0\337\0\67\0\337\0\70\0\337\0\71\0\337" +
		"\0\72\0\337\0\73\0\337\0\74\0\337\0\75\0\337\0\76\0\337\0\77\0\337\0\100\0\337\0" +
		"\101\0\337\0\102\0\337\0\103\0\337\0\104\0\337\0\105\0\337\0\106\0\337\0\107\0\337" +
		"\0\110\0\337\0\111\0\337\0\112\0\337\0\113\0\337\0\114\0\337\0\115\0\337\0\116\0" +
		"\337\0\uffff\uffff\ufffe\uffff\14\0\uffff\uffff\15\0\uffff\uffff\12\0\273\0\23\0" +
		"\273\0\43\0\273\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\24\0\uffff" +
		"\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff" +
		"\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112" +
		"\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff" +
		"\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101" +
		"\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff" +
		"\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\57\0\uffff\uffff\53\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff" +
		"\uffff\11\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff" +
		"\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\116\0\uffff\uffff\11\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\116\0\uffff\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30" +
		"\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\116\0\uffff\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17" +
		"\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\116\0\uffff\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17" +
		"\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\116\0\uffff\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\42\0\uffff\uffff" +
		"\6\0\166\0\11\0\166\0\16\0\166\0\17\0\166\0\24\0\166\0\25\0\166\0\27\0\166\0\30\0" +
		"\166\0\44\0\166\0\45\0\166\0\46\0\166\0\47\0\166\0\53\0\166\0\56\0\166\0\57\0\166" +
		"\0\62\0\166\0\63\0\166\0\64\0\166\0\65\0\166\0\66\0\166\0\67\0\166\0\70\0\166\0\71" +
		"\0\166\0\72\0\166\0\73\0\166\0\74\0\166\0\75\0\166\0\76\0\166\0\77\0\166\0\100\0" +
		"\166\0\101\0\166\0\102\0\166\0\103\0\166\0\104\0\166\0\105\0\166\0\106\0\166\0\107" +
		"\0\166\0\110\0\166\0\111\0\166\0\112\0\166\0\113\0\166\0\114\0\166\0\115\0\166\0" +
		"\116\0\166\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff\20\0\253\0\33\0\253\0\uffff" +
		"\uffff\ufffe\uffff\21\0\uffff\uffff\7\0\345\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\116\0\uffff\uffff\11\0\347\0\16\0\347\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff" +
		"\uffff\56\0\uffff\uffff\116\0\uffff\uffff\11\0\347\0\16\0\347\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff" +
		"\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff" +
		"\uffff\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\116\0\uffff\uffff\11\0\347\0\16\0\347\0\uffff" +
		"\uffff\ufffe\uffff\116\0\uffff\uffff\0\0\341\0\6\0\341\0\10\0\341\0\22\0\341\0\46" +
		"\0\341\0\47\0\341\0\57\0\341\0\62\0\341\0\63\0\341\0\64\0\341\0\65\0\341\0\66\0\341" +
		"\0\67\0\341\0\70\0\341\0\71\0\341\0\72\0\341\0\73\0\341\0\74\0\341\0\75\0\341\0\76" +
		"\0\341\0\77\0\341\0\100\0\341\0\101\0\341\0\102\0\341\0\103\0\341\0\104\0\341\0\105" +
		"\0\341\0\106\0\341\0\107\0\341\0\110\0\341\0\111\0\341\0\112\0\341\0\113\0\341\0" +
		"\114\0\341\0\115\0\341\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\47\0\uffff\uffff" +
		"\115\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff" +
		"\uffff\11\0\21\0\27\0\21\0\42\0\21\0\uffff\uffff\ufffe\uffff\34\0\uffff\uffff\35" +
		"\0\uffff\uffff\6\0\217\0\11\0\217\0\16\0\217\0\17\0\217\0\24\0\217\0\25\0\217\0\27" +
		"\0\217\0\30\0\217\0\37\0\217\0\42\0\217\0\44\0\217\0\45\0\217\0\46\0\217\0\47\0\217" +
		"\0\53\0\217\0\54\0\217\0\56\0\217\0\57\0\217\0\62\0\217\0\63\0\217\0\64\0\217\0\65" +
		"\0\217\0\66\0\217\0\67\0\217\0\70\0\217\0\71\0\217\0\72\0\217\0\73\0\217\0\74\0\217" +
		"\0\75\0\217\0\76\0\217\0\77\0\217\0\100\0\217\0\101\0\217\0\102\0\217\0\103\0\217" +
		"\0\104\0\217\0\105\0\217\0\106\0\217\0\107\0\217\0\110\0\217\0\111\0\217\0\112\0" +
		"\217\0\113\0\217\0\114\0\217\0\115\0\217\0\116\0\217\0\uffff\uffff\ufffe\uffff\6" +
		"\0\uffff\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\17\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff\uffff\114\0" +
		"\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff" +
		"\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\116\0\uffff\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\11" +
		"\0\347\0\16\0\347\0\27\0\347\0\uffff\uffff\ufffe\uffff\42\0\215\0\6\0\215\0\11\0" +
		"\215\0\16\0\215\0\17\0\215\0\24\0\215\0\25\0\215\0\27\0\215\0\30\0\215\0\44\0\215" +
		"\0\45\0\215\0\46\0\215\0\47\0\215\0\53\0\215\0\56\0\215\0\57\0\215\0\62\0\215\0\63" +
		"\0\215\0\64\0\215\0\65\0\215\0\66\0\215\0\67\0\215\0\70\0\215\0\71\0\215\0\72\0\215" +
		"\0\73\0\215\0\74\0\215\0\75\0\215\0\76\0\215\0\77\0\215\0\100\0\215\0\101\0\215\0" +
		"\102\0\215\0\103\0\215\0\104\0\215\0\105\0\215\0\106\0\215\0\107\0\215\0\110\0\215" +
		"\0\111\0\215\0\112\0\215\0\113\0\215\0\114\0\215\0\115\0\215\0\116\0\215\0\uffff" +
		"\uffff\ufffe\uffff\20\0\uffff\uffff\7\0\103\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\115\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\116\0\uffff\uffff\11\0\347\0\16\0\347\0\uffff\uffff\ufffe\uffff" +
		"\12\0\300\0\43\0\uffff\uffff\23\0\300\0\uffff\uffff\ufffe\uffff\12\0\277\0\43\0\277" +
		"\0\23\0\277\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\11\0\347\0\16\0\347\0\27\0" +
		"\347\0\uffff\uffff\ufffe\uffff\11\0\236\0\42\0\uffff\uffff\27\0\236\0\uffff\uffff" +
		"\ufffe\uffff\11\0\237\0\42\0\237\0\27\0\237\0\uffff\uffff\ufffe\uffff");

	private static final int[] lapg_sym_goto = TMLexer.unpack_int(180,
		"\0\0\2\0\22\0\41\0\41\0\41\0\41\0\100\0\110\0\112\0\117\0\122\0\132\0\133\0\134\0" +
		"\164\0\211\0\222\0\233\0\256\0\261\0\325\0\351\0\351\0\363\0\u0106\0\u0108\0\u010d" +
		"\0\u010f\0\u0112\0\u0117\0\u0118\0\u0119\0\u011e\0\u0125\0\u012d\0\u0130\0\u0149" +
		"\0\u0160\0\u017a\0\u01da\0\u01e7\0\u01f4\0\u01fa\0\u01fb\0\u01fc\0\u01fd\0\u0219" +
		"\0\u027a\0\u027b\0\u027c\0\u02e0\0\u0341\0\u03a2\0\u0406\0\u0467\0\u04c8\0\u0529" +
		"\0\u058a\0\u05eb\0\u064c\0\u06ad\0\u070e\0\u076f\0\u07d0\0\u0831\0\u0893\0\u08f5" +
		"\0\u0956\0\u09b7\0\u0a1d\0\u0a81\0\u0ae5\0\u0b46\0\u0ba7\0\u0c08\0\u0c6a\0\u0ccb" +
		"\0\u0d2c\0\u0d46\0\u0d46\0\u0d48\0\u0d48\0\u0d49\0\u0d4a\0\u0d4b\0\u0d4c\0\u0d4d" +
		"\0\u0d4e\0\u0d50\0\u0d51\0\u0d52\0\u0d83\0\u0da9\0\u0dbd\0\u0dc2\0\u0dc4\0\u0dc5" +
		"\0\u0dc7\0\u0dc9\0\u0dcb\0\u0dcc\0\u0dcd\0\u0dcf\0\u0dd1\0\u0dd3\0\u0dd5\0\u0dd6" +
		"\0\u0dd8\0\u0ddb\0\u0ddc\0\u0dde\0\u0de0\0\u0de4\0\u0de7\0\u0de8\0\u0de9\0\u0deb" +
		"\0\u0ded\0\u0dee\0\u0df0\0\u0df2\0\u0df3\0\u0dfd\0\u0e07\0\u0e12\0\u0e1d\0\u0e29" +
		"\0\u0e44\0\u0e57\0\u0e65\0\u0e79\0\u0e7a\0\u0e8e\0\u0e90\0\u0ea4\0\u0eb8\0\u0ece" +
		"\0\u0ee6\0\u0efe\0\u0f12\0\u0f2a\0\u0f43\0\u0f5f\0\u0f64\0\u0f68\0\u0f7e\0\u0f94" +
		"\0\u0fab\0\u0fac\0\u0fae\0\u0fb0\0\u0fba\0\u0fbb\0\u0fbc\0\u0fbf\0\u0fc1\0\u0fc3" +
		"\0\u0fc7\0\u0fca\0\u0fcd\0\u0fd3\0\u0fd4\0\u0fd5\0\u0fd6\0\u0fd7\0\u0fd9\0\u0fe6" +
		"\0\u0fe9\0\u0fec\0\u1001\0\u101b\0\u101d\0\u101e\0\u101f\0\u1020\0\u1021\0\u1024" +
		"\0\u1027\0\u1042\0");

	private static final int[] lapg_sym_from = TMLexer.unpack_int(4162,
		"\u019a\0\u019b\0\1\0\6\0\36\0\41\0\61\0\72\0\106\0\116\0\263\0\363\0\u0103\0\u011a" +
		"\0\u0135\0\u013b\0\u013c\0\u0160\0\1\0\6\0\41\0\55\0\72\0\106\0\116\0\250\0\263\0" +
		"\363\0\u011a\0\u0135\0\u013b\0\u013c\0\u0160\0\105\0\130\0\140\0\163\0\225\0\267" +
		"\0\303\0\304\0\306\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010c" +
		"\0\u010d\0\u010f\0\u0110\0\u0114\0\u0129\0\u012b\0\u012c\0\u0153\0\u0154\0\u0157" +
		"\0\u015a\0\u016b\0\u0185\0\162\0\235\0\236\0\242\0\342\0\344\0\345\0\u012d\0\37\0" +
		"\64\0\300\0\u014d\0\u017e\0\u0193\0\u0194\0\374\0\u0178\0\u0179\0\63\0\126\0\223" +
		"\0\254\0\262\0\276\0\362\0\u011f\0\372\0\372\0\34\0\60\0\104\0\121\0\175\0\177\0" +
		"\243\0\252\0\260\0\262\0\301\0\356\0\357\0\362\0\u0127\0\u0128\0\u012a\0\u0132\0" +
		"\u0137\0\u0167\0\u0169\0\u016a\0\u0174\0\u018e\0\21\0\225\0\267\0\303\0\304\0\307" +
		"\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110\0\u0118\0\u0129" +
		"\0\u012b\0\u012c\0\u0154\0\u016b\0\24\0\50\0\76\0\150\0\175\0\177\0\252\0\330\0\u0164" +
		"\0\47\0\75\0\155\0\276\0\323\0\336\0\337\0\u0122\0\u014e\0\1\0\6\0\41\0\105\0\106" +
		"\0\116\0\130\0\225\0\263\0\267\0\340\0\341\0\343\0\u0104\0\u0109\0\u0129\0\u012b" +
		"\0\u012c\0\u016b\0\25\0\150\0\374\0\20\0\30\0\32\0\225\0\267\0\272\0\274\0\303\0" +
		"\304\0\307\0\323\0\340\0\341\0\343\0\351\0\376\0\u0104\0\u0105\0\u0106\0\u0107\0" +
		"\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u0129\0\u012b\0\u012c\0" +
		"\u0148\0\u0149\0\u0154\0\u016b\0\u0181\0\u0183\0\225\0\267\0\303\0\304\0\307\0\340" +
		"\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110\0\u0118\0\u0129\0\u012b" +
		"\0\u012c\0\u0154\0\u016b\0\54\0\77\0\102\0\375\0\u0101\0\u0147\0\u014d\0\u0170\0" +
		"\u017a\0\u017e\0\225\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109" +
		"\0\u010b\0\u010d\0\u0110\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\u0103\0\u0145" +
		"\0\10\0\162\0\242\0\273\0\u014a\0\51\0\330\0\321\0\u0150\0\u0191\0\26\0\73\0\321" +
		"\0\u0150\0\u0191\0\276\0\316\0\266\0\270\0\u013d\0\u013f\0\u0144\0\26\0\73\0\u0105" +
		"\0\u0148\0\u0149\0\u0181\0\u0183\0\310\0\u0101\0\u0115\0\u014d\0\u015d\0\u017e\0" +
		"\u0193\0\u0194\0\374\0\u0178\0\u0179\0\225\0\267\0\303\0\304\0\307\0\323\0\340\0" +
		"\341\0\343\0\376\0\u0104\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110" +
		"\0\u0111\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\140\0\163\0\170\0\225" +
		"\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d" +
		"\0\u0110\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\1\0\6\0\37\0\41\0\106" +
		"\0\116\0\130\0\161\0\163\0\225\0\263\0\267\0\307\0\340\0\341\0\343\0\376\0\u0104" +
		"\0\u0109\0\u010d\0\u0110\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\1\0\2\0\6\0\13" +
		"\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0" +
		"\125\0\130\0\140\0\144\0\145\0\146\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220" +
		"\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0" +
		"\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366" +
		"\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b" +
		"\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c" +
		"\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160" +
		"\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\6\0\41\0\72\0\106\0\116\0\263\0\363" +
		"\0\u011a\0\u0135\0\u013b\0\u013c\0\u0160\0\1\0\6\0\41\0\72\0\106\0\116\0\263\0\363" +
		"\0\u011a\0\u0135\0\u013b\0\u013c\0\u0160\0\1\0\6\0\41\0\106\0\116\0\263\0\376\0\320" +
		"\0\22\0\225\0\255\0\256\0\267\0\303\0\304\0\307\0\323\0\340\0\341\0\343\0\355\0\376" +
		"\0\u0104\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118" +
		"\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\124\0\125\0\130\0\140\0" +
		"\144\0\145\0\146\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226" +
		"\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0" +
		"\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376" +
		"\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0" +
		"\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0" +
		"\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0" +
		"\u0181\0\u0183\0\u018a\0\124\0\124\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41" +
		"\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0" +
		"\146\0\161\0\162\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230" +
		"\0\231\0\232\0\236\0\242\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0" +
		"\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\345\0\354\0\363\0\366\0\367" +
		"\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d" +
		"\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135" +
		"\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b" +
		"\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0" +
		"\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146" +
		"\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0" +
		"\232\0\245\0\260\0\261\0\263\0\264\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307" +
		"\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0" +
		"\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0" +
		"\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0" +
		"\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0" +
		"\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72" +
		"\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161\0\163\0\171" +
		"\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0" +
		"\261\0\263\0\264\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333" +
		"\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106" +
		"\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120" +
		"\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148" +
		"\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161\0\162\0\163\0\171\0\173\0\200\0" +
		"\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\236\0\242\0\245\0\260\0\261" +
		"\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0" +
		"\341\0\343\0\345\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106" +
		"\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120" +
		"\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148" +
		"\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\160\0\161\0\163\0\171\0\173\0\200\0" +
		"\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265" +
		"\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0" +
		"\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108" +
		"\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129" +
		"\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a" +
		"\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\140\0\144\0\145\0\146\0\160\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220" +
		"\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0" +
		"\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366" +
		"\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b" +
		"\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c" +
		"\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160" +
		"\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0" +
		"\145\0\146\0\160\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226" +
		"\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0" +
		"\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376" +
		"\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0" +
		"\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0" +
		"\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0" +
		"\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0" +
		"\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\160\0" +
		"\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232" +
		"\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0" +
		"\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0" +
		"\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0" +
		"\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0" +
		"\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0" +
		"\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0" +
		"\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\160\0\161\0\163\0\171" +
		"\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0" +
		"\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340" +
		"\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0" +
		"\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0" +
		"\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0" +
		"\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0" +
		"\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\160\0\161\0\163\0\171\0\173\0\200\0" +
		"\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265" +
		"\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0" +
		"\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108" +
		"\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129" +
		"\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a" +
		"\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\140\0\144\0\145\0\146\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\212\0\220" +
		"\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0" +
		"\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366" +
		"\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b" +
		"\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c" +
		"\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160" +
		"\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0" +
		"\145\0\146\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\212\0\220\0\222\0\225\0\226" +
		"\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0" +
		"\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376" +
		"\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0" +
		"\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0" +
		"\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0" +
		"\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0" +
		"\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\160\0" +
		"\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232" +
		"\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0" +
		"\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0" +
		"\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0" +
		"\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0" +
		"\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0" +
		"\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0" +
		"\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\160\0\161\0\163\0\171" +
		"\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0" +
		"\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340" +
		"\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0" +
		"\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0" +
		"\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0" +
		"\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0" +
		"\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\160\0\161\0\163\0\171\0\173\0\200\0" +
		"\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265" +
		"\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0" +
		"\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108" +
		"\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129" +
		"\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a" +
		"\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\140\0\144\0\145\0\146\0\160\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220" +
		"\0\221\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0" +
		"\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363" +
		"\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0" +
		"\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0" +
		"\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0" +
		"\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0" +
		"\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140" +
		"\0\144\0\145\0\146\0\160\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\221\0" +
		"\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270" +
		"\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0" +
		"\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0" +
		"\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0" +
		"\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0" +
		"\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0" +
		"\145\0\146\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230" +
		"\0\231\0\232\0\245\0\251\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0" +
		"\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376" +
		"\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0" +
		"\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0" +
		"\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0" +
		"\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0" +
		"\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161\0" +
		"\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245" +
		"\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0" +
		"\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105" +
		"\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a" +
		"\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u012e\0\u0135\0\u013d\0\u013f\0\u0141" +
		"\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183" +
		"\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161\0\162\0\163\0" +
		"\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\227\0\230\0\231\0\232\0\236" +
		"\0\242\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0" +
		"\323\0\327\0\333\0\340\0\341\0\343\0\345\0\354\0\363\0\366\0\367\0\371\0\376\0\377" +
		"\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111" +
		"\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u012e\0\u0135\0\u013d" +
		"\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a" +
		"\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161" +
		"\0\162\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0" +
		"\232\0\236\0\242\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304" +
		"\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\345\0\354\0\363\0\366\0\367\0\371\0" +
		"\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110" +
		"\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d" +
		"\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a" +
		"\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161" +
		"\0\162\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0" +
		"\232\0\236\0\242\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304" +
		"\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\345\0\354\0\363\0\366\0\367\0\371\0" +
		"\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110" +
		"\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d" +
		"\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a" +
		"\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161" +
		"\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0" +
		"\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327" +
		"\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105" +
		"\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a" +
		"\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u012e\0\u0135\0\u013d\0\u013f\0\u0141" +
		"\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183" +
		"\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161\0\163\0\171\0" +
		"\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261" +
		"\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0" +
		"\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107" +
		"\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123" +
		"\0\u0129\0\u012b\0\u012c\0\u012e\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148" +
		"\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\0\0\1" +
		"\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106" +
		"\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161\0\163\0\171\0\173\0\200\0" +
		"\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265" +
		"\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0" +
		"\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108" +
		"\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129" +
		"\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a" +
		"\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\20\0" +
		"\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\101\0\105\0\106\0\116\0\117" +
		"\0\125\0\130\0\140\0\144\0\145\0\146\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0" +
		"\220\0\222\0\225\0\226\0\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267" +
		"\0\270\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0" +
		"\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b" +
		"\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c" +
		"\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160" +
		"\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\62\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\140\0\144" +
		"\0\145\0\146\0\161\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0" +
		"\230\0\231\0\232\0\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304" +
		"\0\307\0\323\0\327\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0" +
		"\377\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111" +
		"\0\u0118\0\u011a\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f" +
		"\0\u0141\0\u0144\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181" +
		"\0\u0183\0\u018a\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\107\0\116\0\117\0\125\0\130\0\140\0\144\0\145\0\146\0\161" +
		"\0\163\0\171\0\173\0\200\0\205\0\211\0\220\0\222\0\225\0\226\0\230\0\231\0\232\0" +
		"\245\0\260\0\261\0\263\0\265\0\266\0\267\0\270\0\271\0\303\0\304\0\307\0\323\0\327" +
		"\0\333\0\340\0\341\0\343\0\354\0\363\0\366\0\367\0\371\0\376\0\377\0\u0104\0\u0105" +
		"\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u011a" +
		"\0\u0120\0\u0123\0\u0129\0\u012b\0\u012c\0\u0135\0\u013d\0\u013f\0\u0141\0\u0144" +
		"\0\u0148\0\u0149\0\u014a\0\u0154\0\u0160\0\u016b\0\u017a\0\u0181\0\u0183\0\u018a" +
		"\0\127\0\162\0\225\0\236\0\242\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\345\0" +
		"\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110\0\u0118\0\u0129\0\u012b\0\u012c\0\u0130" +
		"\0\u0154\0\u016b\0\152\0\204\0\3\0\0\0\22\0\0\0\37\0\64\0\20\0\101\0\22\0\37\0\26" +
		"\0\43\0\44\0\73\0\105\0\130\0\140\0\145\0\146\0\163\0\171\0\222\0\225\0\226\0\231" +
		"\0\232\0\245\0\261\0\266\0\267\0\271\0\303\0\304\0\307\0\323\0\327\0\333\0\340\0" +
		"\341\0\343\0\371\0\376\0\u0104\0\u0106\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0" +
		"\u0111\0\u0118\0\u0120\0\u0129\0\u012b\0\u012c\0\u013d\0\u013f\0\u0154\0\u016b\0" +
		"\1\0\6\0\41\0\106\0\116\0\225\0\263\0\267\0\303\0\304\0\307\0\323\0\340\0\341\0\343" +
		"\0\376\0\u0104\0\u0105\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0" +
		"\u0111\0\u0118\0\u011a\0\u0129\0\u012b\0\u012c\0\u0148\0\u0149\0\u014a\0\u0154\0" +
		"\u016b\0\u0181\0\u0183\0\72\0\144\0\173\0\205\0\220\0\230\0\260\0\270\0\354\0\363" +
		"\0\366\0\367\0\377\0\u0123\0\u0135\0\u0141\0\u0144\0\u0160\0\u017a\0\u018a\0\127" +
		"\0\162\0\236\0\242\0\345\0\152\0\204\0\105\0\105\0\130\0\105\0\130\0\105\0\130\0" +
		"\351\0\u012e\0\105\0\130\0\145\0\146\0\105\0\130\0\105\0\130\0\125\0\125\0\200\0" +
		"\145\0\146\0\245\0\140\0\140\0\163\0\140\0\163\0\162\0\236\0\242\0\345\0\336\0\337" +
		"\0\u0122\0\160\0\160\0\140\0\163\0\140\0\163\0\205\0\205\0\354\0\220\0\u0141\0\u0123" +
		"\0\225\0\267\0\340\0\341\0\343\0\u0104\0\u0129\0\u012b\0\u012c\0\u016b\0\225\0\267" +
		"\0\340\0\341\0\343\0\u0104\0\u0129\0\u012b\0\u012c\0\u016b\0\225\0\267\0\340\0\341" +
		"\0\343\0\u0104\0\u0109\0\u0129\0\u012b\0\u012c\0\u016b\0\225\0\267\0\340\0\341\0" +
		"\343\0\u0104\0\u0109\0\u0129\0\u012b\0\u012c\0\u016b\0\225\0\267\0\303\0\340\0\341" +
		"\0\343\0\u0104\0\u0109\0\u0129\0\u012b\0\u012c\0\u016b\0\225\0\267\0\303\0\304\0" +
		"\306\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010c\0\u010d\0\u010f" +
		"\0\u0110\0\u0114\0\u0129\0\u012b\0\u012c\0\u0153\0\u0154\0\u0157\0\u015a\0\u016b" +
		"\0\u0185\0\225\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0" +
		"\u010b\0\u010d\0\u0110\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\225\0\267\0\303" +
		"\0\304\0\340\0\341\0\343\0\u0104\0\u0109\0\u010b\0\u0129\0\u012b\0\u012c\0\u016b" +
		"\0\225\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0" +
		"\u010d\0\u0110\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\270\0\225\0\267" +
		"\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110" +
		"\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\270\0\u0144\0\225\0\267\0\303" +
		"\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110\0\u0118" +
		"\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\225\0\267\0\303\0\304\0\307\0\340\0\341" +
		"\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110\0\u0118\0\u0129\0\u012b\0\u012c" +
		"\0\u0154\0\u016b\0\225\0\267\0\303\0\304\0\307\0\323\0\340\0\341\0\343\0\376\0\u0104" +
		"\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154" +
		"\0\u016b\0\225\0\267\0\303\0\304\0\307\0\323\0\340\0\341\0\343\0\376\0\u0104\0\u0106" +
		"\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u0129\0\u012b\0\u012c" +
		"\0\u0154\0\u016b\0\225\0\267\0\303\0\304\0\307\0\323\0\340\0\341\0\343\0\376\0\u0104" +
		"\0\u0106\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u0129\0\u012b" +
		"\0\u012c\0\u0154\0\u016b\0\225\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0" +
		"\u0104\0\u0109\0\u010b\0\u010d\0\u0110\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0" +
		"\u016b\0\225\0\267\0\303\0\304\0\307\0\323\0\340\0\341\0\343\0\376\0\u0104\0\u0106" +
		"\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u0129\0\u012b\0\u012c" +
		"\0\u0154\0\u016b\0\225\0\267\0\303\0\304\0\307\0\323\0\340\0\341\0\343\0\376\0\u0104" +
		"\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0\u010d\0\u0110\0\u0111\0\u0118\0\u0129" +
		"\0\u012b\0\u012c\0\u0154\0\u016b\0\225\0\255\0\256\0\267\0\303\0\304\0\307\0\323" +
		"\0\340\0\341\0\343\0\355\0\376\0\u0104\0\u0106\0\u0107\0\u0108\0\u0109\0\u010b\0" +
		"\u010d\0\u0110\0\u0111\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\u0105\0" +
		"\u0148\0\u0149\0\u0181\0\u0183\0\u0105\0\u0148\0\u0181\0\u0183\0\140\0\163\0\225" +
		"\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d" +
		"\0\u0110\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\140\0\163\0\225\0\267" +
		"\0\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110" +
		"\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\140\0\163\0\170\0\225\0\267\0" +
		"\303\0\304\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110" +
		"\0\u0118\0\u0129\0\u012b\0\u012c\0\u0154\0\u016b\0\226\0\162\0\242\0\226\0\u0120" +
		"\0\26\0\43\0\44\0\73\0\226\0\266\0\371\0\u0120\0\u013d\0\u013f\0\26\0\26\0\10\0\273" +
		"\0\u014a\0\26\0\73\0\160\0\221\0\72\0\363\0\u0135\0\u0160\0\266\0\u013d\0\u013f\0" +
		"\266\0\u013d\0\u013f\0\1\0\6\0\41\0\106\0\116\0\263\0\6\0\6\0\53\0\53\0\53\0\117" +
		"\0\1\0\6\0\41\0\72\0\106\0\116\0\263\0\363\0\u011a\0\u0135\0\u013b\0\u013c\0\u0160" +
		"\0\2\0\13\0\31\0\2\0\13\0\31\0\225\0\267\0\303\0\304\0\307\0\340\0\341\0\343\0\376" +
		"\0\u0104\0\u0109\0\u010b\0\u010d\0\u0110\0\u0118\0\u0129\0\u012b\0\u012c\0\u0130" +
		"\0\u0154\0\u016b\0\1\0\6\0\37\0\41\0\106\0\116\0\130\0\161\0\163\0\225\0\263\0\267" +
		"\0\307\0\340\0\341\0\343\0\376\0\u0104\0\u0109\0\u010d\0\u0110\0\u0129\0\u012b\0" +
		"\u012c\0\u0154\0\u016b\0\20\0\101\0\127\0\250\0\351\0\u0130\0\231\0\232\0\333\0\336" +
		"\0\337\0\u0122\0\225\0\267\0\303\0\304\0\306\0\307\0\340\0\341\0\343\0\376\0\u0104" +
		"\0\u0109\0\u010b\0\u010c\0\u010d\0\u010f\0\u0110\0\u0114\0\u0129\0\u012b\0\u012c" +
		"\0\u0153\0\u0154\0\u0157\0\u015a\0\u016b\0\u0185\0");

	private static final int[] lapg_sym_to = TMLexer.unpack_int(4162,
		"\u019c\0\u019d\0\4\0\4\0\60\0\4\0\104\0\4\0\4\0\4\0\4\0\4\0\u0145\0\4\0\4\0\4\0\4" +
		"\0\4\0\5\0\5\0\5\0\102\0\5\0\5\0\5\0\350\0\5\0\5\0\5\0\5\0\5\0\5\0\5\0\124\0\124" +
		"\0\160\0\160\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0" +
		"\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264\0\264" +
		"\0\264\0\264\0\225\0\340\0\341\0\343\0\u0129\0\u012b\0\u012c\0\u016b\0\62\0\107\0" +
		"\u0109\0\u0181\0\u0181\0\u0181\0\u0181\0\u013d\0\u013d\0\u013d\0\106\0\152\0\263" +
		"\0\355\0\363\0\u0106\0\u0135\0\u0160\0\u013b\0\u013c\0\56\0\103\0\123\0\143\0\244" +
		"\0\246\0\346\0\353\0\360\0\364\0\u010a\0\u0133\0\u0134\0\u0136\0\u0165\0\u0166\0" +
		"\u0168\0\u0173\0\u0175\0\u018b\0\u018c\0\u018d\0\u0190\0\u0197\0\35\0\265\0\265\0" +
		"\265\0\265\0\265\0\265\0\265\0\265\0\265\0\265\0\265\0\265\0\265\0\265\0\265\0\265" +
		"\0\265\0\265\0\265\0\265\0\41\0\73\0\117\0\200\0\245\0\245\0\354\0\u0120\0\u018a" +
		"\0\72\0\116\0\204\0\u0107\0\u011d\0\u0123\0\u0123\0\u0123\0\u0107\0\6\0\6\0\6\0\125" +
		"\0\6\0\6\0\125\0\266\0\6\0\266\0\266\0\266\0\266\0\266\0\266\0\266\0\266\0\266\0" +
		"\266\0\42\0\201\0\u013e\0\31\0\53\0\55\0\267\0\267\0\u0104\0\u0105\0\267\0\267\0" +
		"\267\0\267\0\267\0\267\0\267\0\u012e\0\267\0\267\0\u0148\0\267\0\267\0\267\0\267" +
		"\0\267\0\267\0\267\0\267\0\267\0\267\0\267\0\267\0\u0148\0\u0148\0\267\0\267\0\u0148" +
		"\0\u0148\0\270\0\270\0\270\0\270\0\270\0\270\0\270\0\270\0\270\0\270\0\270\0\270" +
		"\0\270\0\270\0\270\0\270\0\270\0\270\0\270\0\270\0\101\0\120\0\122\0\u0140\0\u0143" +
		"\0\u017d\0\u0182\0\u018f\0\u0191\0\u0192\0\271\0\271\0\271\0\271\0\271\0\271\0\271" +
		"\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\u0146" +
		"\0\u017c\0\26\0\226\0\226\0\26\0\26\0\74\0\u0121\0\u011b\0\u011b\0\u0198\0\43\0\43" +
		"\0\u011c\0\u011c\0\u0199\0\u0108\0\u0119\0\371\0\377\0\371\0\371\0\377\0\44\0\44" +
		"\0\u0149\0\u0149\0\u0149\0\u0149\0\u0149\0\u0118\0\u0144\0\u0118\0\u0183\0\u0118" +
		"\0\u0183\0\u0183\0\u0183\0\u013f\0\u013f\0\u013f\0\272\0\272\0\272\0\272\0\272\0" +
		"\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272" +
		"\0\272\0\272\0\272\0\272\0\272\0\272\0\161\0\161\0\161\0\161\0\161\0\161\0\161\0" +
		"\161\0\161\0\161\0\161\0\161\0\161\0\161\0\161\0\161\0\161\0\161\0\161\0\161\0\161" +
		"\0\161\0\161\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7" +
		"\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63" +
		"\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45" +
		"\0\45\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0" +
		"\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45" +
		"\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0" +
		"\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273" +
		"\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0" +
		"\u014a\0\u014a\0\111\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11" +
		"\0\11\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\13\0\13" +
		"\0\13\0\13\0\13\0\13\0\u0141\0\u011a\0\36\0\274\0\274\0\274\0\274\0\274\0\274\0\274" +
		"\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0" +
		"\274\0\274\0\274\0\274\0\274\0\274\0\274\0\274\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\144\0\147\0\126" +
		"\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0" +
		"\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273" +
		"\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111" +
		"\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0" +
		"\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111" +
		"\0\273\0\111\0\u014a\0\u014a\0\111\0\145\0\146\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0" +
		"\111\0\45\0\45\0\223\0\227\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327" +
		"\0\111\0\45\0\45\0\227\0\227\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273" +
		"\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\227\0\111\0\111\0\111\0\111\0\45" +
		"\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0" +
		"\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10" +
		"\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0" +
		"\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111" +
		"\0\45\0\45\0\45\0\111\0\45\0\10\0\366\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273" +
		"\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273" +
		"\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0" +
		"\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273" +
		"\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0" +
		"\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0" +
		"\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45" +
		"\0\111\0\45\0\10\0\367\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0" +
		"\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273" +
		"\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273" +
		"\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a" +
		"\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\230\0\45" +
		"\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\230\0\230" +
		"\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0" +
		"\45\0\273\0\273\0\273\0\230\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a" +
		"\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273" +
		"\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0" +
		"\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\205\0\223" +
		"\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0" +
		"\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273" +
		"\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0" +
		"\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111" +
		"\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\206\0\223\0\45\0\45\0\111" +
		"\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0" +
		"\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0" +
		"\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273" +
		"\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111" +
		"\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0" +
		"\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\207\0\223\0\45\0\45\0\111\0\147\0\111" +
		"\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273" +
		"\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111" +
		"\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0" +
		"\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a" +
		"\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147" +
		"\0\126\0\45\0\111\0\45\0\45\0\210\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0" +
		"\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45" +
		"\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45" +
		"\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0" +
		"\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10" +
		"\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0" +
		"\111\0\45\0\45\0\211\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327" +
		"\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273" +
		"\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273" +
		"\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0" +
		"\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273" +
		"\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0" +
		"\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0" +
		"\212\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0" +
		"\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45" +
		"\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0" +
		"\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273" +
		"\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0" +
		"\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\45" +
		"\0\45\0\111\0\147\0\111\0\254\0\255\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0" +
		"\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273" +
		"\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0" +
		"\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111" +
		"\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147" +
		"\0\111\0\254\0\256\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0" +
		"\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0" +
		"\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273" +
		"\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111" +
		"\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0" +
		"\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\213\0\223\0\45\0\45\0\111\0\147\0\111" +
		"\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273" +
		"\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111" +
		"\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0" +
		"\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a" +
		"\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147" +
		"\0\126\0\45\0\111\0\45\0\45\0\214\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0" +
		"\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45" +
		"\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45" +
		"\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0" +
		"\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10" +
		"\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0" +
		"\111\0\45\0\45\0\215\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327" +
		"\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273" +
		"\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273" +
		"\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0" +
		"\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273" +
		"\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0" +
		"\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0" +
		"\216\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\216\0\45\0\273\0\327\0\111\0" +
		"\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273" +
		"\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a" +
		"\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273" +
		"\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0" +
		"\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\217\0\223" +
		"\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\217\0\45\0\273\0\327\0\111\0\45\0\45\0" +
		"\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45" +
		"\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0" +
		"\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273" +
		"\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a" +
		"\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0" +
		"\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\352\0\111\0" +
		"\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273" +
		"\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0" +
		"\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45" +
		"\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0" +
		"\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147" +
		"\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0" +
		"\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0" +
		"\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273" +
		"\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\u016c\0\111\0\45\0\45\0\111" +
		"\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0" +
		"\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\231\0\45\0\45\0\111\0\147\0\111" +
		"\0\254\0\111\0\45\0\273\0\327\0\333\0\111\0\45\0\45\0\231\0\231\0\45\0\111\0\45\0" +
		"\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273" +
		"\0\231\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0" +
		"\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\u016d\0\111" +
		"\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\232\0\45\0\45\0\111" +
		"\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\232\0\232\0\45\0\111" +
		"\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0" +
		"\273\0\273\0\232\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10" +
		"\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111" +
		"\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\233\0\45\0\45\0\111" +
		"\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\233\0\233\0\45\0\111" +
		"\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0" +
		"\273\0\273\0\233\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10" +
		"\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111" +
		"\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147" +
		"\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0" +
		"\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0" +
		"\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273" +
		"\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\u016e\0\111\0\45\0\45\0\111" +
		"\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0" +
		"\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147\0\111\0\254" +
		"\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111" +
		"\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111" +
		"\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0" +
		"\273\0\10\0\327\0\111\0\273\0\273\0\273\0\u016f\0\111\0\45\0\45\0\111\0\111\0\u014a" +
		"\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\2\0\10\0\17\0\10\0" +
		"\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75" +
		"\0\147\0\126\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0" +
		"\45\0\273\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45" +
		"\0\273\0\273\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45" +
		"\0\273\0\111\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0" +
		"\10\0\327\0\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10" +
		"\0\273\0\111\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\32\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\32\0\126\0\10\0\10\0\75\0\147\0" +
		"\126\0\45\0\111\0\45\0\45\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273" +
		"\0\327\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273" +
		"\0\273\0\273\0\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111" +
		"\0\273\0\u014a\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0" +
		"\111\0\273\0\273\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111" +
		"\0\273\0\111\0\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0" +
		"\10\0\45\0\45\0\75\0\105\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\126\0\45\0\111\0" +
		"\45\0\45\0\223\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45" +
		"\0\45\0\45\0\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0" +
		"\45\0\45\0\273\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a" +
		"\0\273\0\10\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273" +
		"\0\273\0\111\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0" +
		"\u014a\0\u014a\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\140\0\10\0\75\0\147\0\126\0\45\0\111\0\45\0\45\0\223" +
		"\0\45\0\45\0\111\0\147\0\111\0\254\0\111\0\45\0\273\0\327\0\111\0\45\0\45\0\45\0" +
		"\111\0\45\0\10\0\370\0\45\0\273\0\111\0\45\0\273\0\273\0\273\0\273\0\45\0\45\0\273" +
		"\0\273\0\273\0\111\0\111\0\111\0\111\0\45\0\273\0\111\0\273\0\u014a\0\273\0\10\0" +
		"\273\0\273\0\273\0\273\0\273\0\273\0\273\0\10\0\327\0\111\0\273\0\273\0\273\0\111" +
		"\0\45\0\45\0\111\0\111\0\u014a\0\u014a\0\10\0\273\0\111\0\273\0\111\0\u014a\0\u014a" +
		"\0\111\0\153\0\153\0\275\0\153\0\153\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0" +
		"\153\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275" +
		"\0\202\0\202\0\22\0\u019a\0\37\0\3\0\64\0\110\0\33\0\33\0\40\0\65\0\46\0\46\0\46" +
		"\0\46\0\127\0\127\0\162\0\174\0\174\0\162\0\242\0\262\0\276\0\46\0\335\0\335\0\174" +
		"\0\362\0\46\0\276\0\u0103\0\276\0\276\0\276\0\276\0\u011f\0\335\0\276\0\276\0\276" +
		"\0\46\0\276\0\276\0\u014e\0\u014e\0\276\0\276\0\276\0\276\0\276\0\276\0\46\0\276" +
		"\0\276\0\276\0\46\0\46\0\276\0\276\0\14\0\14\0\14\0\14\0\14\0\277\0\14\0\277\0\277" +
		"\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\u014b\0\277\0\277\0\277\0\277" +
		"\0\277\0\277\0\277\0\277\0\277\0\u015e\0\277\0\277\0\277\0\u014b\0\u014b\0\u0180" +
		"\0\277\0\277\0\u014b\0\u014b\0\112\0\173\0\243\0\251\0\257\0\334\0\361\0\u0100\0" +
		"\251\0\112\0\u0138\0\u0139\0\u0142\0\u0163\0\112\0\257\0\u0100\0\112\0\361\0\u0196" +
		"\0\154\0\234\0\234\0\234\0\234\0\203\0\250\0\130\0\131\0\156\0\132\0\132\0\133\0" +
		"\133\0\u012f\0\u0170\0\134\0\134\0\175\0\177\0\135\0\135\0\136\0\136\0\150\0\151" +
		"\0\247\0\176\0\176\0\347\0\163\0\164\0\237\0\165\0\165\0\235\0\342\0\344\0\u012d" +
		"\0\u0124\0\u0124\0\u0124\0\220\0\221\0\166\0\166\0\167\0\167\0\252\0\253\0\u0131" +
		"\0\260\0\u017a\0\u0164\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300" +
		"\0\301\0\375\0\u0127\0\u0128\0\u012a\0\u0147\0\u0167\0\u0169\0\u016a\0\u018e\0\302" +
		"\0\302\0\302\0\302\0\302\0\302\0\u0152\0\302\0\302\0\302\0\302\0\303\0\303\0\303" +
		"\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\304\0\304\0\u010b\0\304\0\304" +
		"\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\305\0\305\0\305\0\305\0\305\0\305\0" +
		"\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305" +
		"\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\306\0\306\0\u010c\0\u010f\0\u0114\0" +
		"\306\0\306\0\306\0\u0114\0\306\0\306\0\u0153\0\u0157\0\u015a\0\306\0\306\0\306\0" +
		"\u0185\0\306\0\307\0\376\0\u010d\0\u0110\0\307\0\307\0\307\0\307\0\307\0\u0154\0" +
		"\307\0\307\0\307\0\307\0\310\0\310\0\310\0\310\0\u0115\0\310\0\310\0\310\0\u0115" +
		"\0\310\0\310\0\310\0\u0115\0\u0115\0\u015d\0\310\0\310\0\310\0\u0115\0\310\0\u0101" +
		"\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0" +
		"\311\0\311\0\311\0\311\0\311\0\311\0\311\0\u0102\0\u017b\0\312\0\312\0\312\0\312" +
		"\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0" +
		"\312\0\312\0\312\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313" +
		"\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\314\0\314\0\314\0\314\0" +
		"\314\0\u011e\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\u011e\0\314" +
		"\0\314\0\314\0\314\0\314\0\314\0\315\0\315\0\315\0\315\0\315\0\315\0\315\0\315\0" +
		"\315\0\315\0\315\0\u014f\0\u0151\0\315\0\315\0\315\0\315\0\315\0\315\0\315\0\315" +
		"\0\315\0\315\0\315\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0" +
		"\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316" +
		"\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0" +
		"\317\0\317\0\317\0\317\0\317\0\317\0\317\0\320\0\320\0\320\0\320\0\320\0\320\0\320" +
		"\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0" +
		"\320\0\320\0\320\0\320\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321" +
		"\0\321\0\321\0\u0150\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321" +
		"\0\321\0\321\0\322\0\356\0\357\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0" +
		"\u0132\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0" +
		"\322\0\322\0\322\0\322\0\u014c\0\u014c\0\u017f\0\u014c\0\u014c\0\u014d\0\u017e\0" +
		"\u0193\0\u0194\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170" +
		"\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\171\0\171\0" +
		"\323\0\323\0\323\0\u0111\0\u0111\0\323\0\323\0\323\0\u0111\0\323\0\323\0\u0111\0" +
		"\u0111\0\u0111\0\u0111\0\323\0\323\0\323\0\u0111\0\323\0\172\0\172\0\241\0\172\0" +
		"\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172" +
		"\0\172\0\172\0\172\0\172\0\172\0\330\0\236\0\345\0\331\0\u0161\0\47\0\70\0\71\0\47" +
		"\0\332\0\372\0\u013a\0\332\0\372\0\372\0\50\0\51\0\27\0\27\0\27\0\52\0\115\0\222" +
		"\0\261\0\113\0\u0137\0\u0174\0\u0189\0\373\0\373\0\373\0\374\0\u0178\0\u0179\0\u019b" +
		"\0\23\0\67\0\137\0\141\0\365\0\24\0\25\0\76\0\77\0\100\0\142\0\15\0\15\0\15\0\114" +
		"\0\15\0\15\0\15\0\114\0\u015f\0\114\0\u0176\0\u0177\0\114\0\20\0\30\0\54\0\21\0\21" +
		"\0\21\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324" +
		"\0\324\0\324\0\324\0\324\0\324\0\u0171\0\324\0\324\0\16\0\16\0\66\0\16\0\16\0\16" +
		"\0\157\0\224\0\240\0\325\0\16\0\325\0\u0116\0\325\0\325\0\325\0\u0116\0\325\0\325" +
		"\0\u0116\0\u0116\0\325\0\325\0\325\0\u0116\0\325\0\34\0\121\0\155\0\351\0\u0130\0" +
		"\u0172\0\336\0\337\0\u0122\0\u0125\0\u0126\0\u0162\0\326\0\326\0\u010e\0\u0112\0" +
		"\u0113\0\u0117\0\326\0\326\0\326\0\u0117\0\326\0\326\0\u0155\0\u0156\0\u0158\0\u0159" +
		"\0\u015b\0\u015c\0\326\0\326\0\326\0\u0184\0\u0186\0\u0187\0\u0188\0\326\0\u0195" +
		"\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(232,
		"\2\0\0\0\5\0\4\0\2\0\0\0\7\0\4\0\3\0\3\0\4\0\4\0\3\0\3\0\1\0\1\0\2\0\1\0\1\0\1\0" +
		"\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\7\0\3\0\3\0\1\0\1\0\1\0\1\0\5\0\3\0\1\0" +
		"\4\0\4\0\3\0\3\0\1\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0" +
		"\4\0\2\0\4\0\3\0\3\0\1\0\1\0\2\0\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0" +
		"\5\0\5\0\6\0\3\0\1\0\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\5\0\4\0\4\0\3\0\4\0\3\0" +
		"\3\0\2\0\4\0\3\0\3\0\2\0\3\0\2\0\2\0\1\0\1\0\3\0\2\0\3\0\3\0\4\0\3\0\1\0\2\0\2\0" +
		"\1\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\2\0\1\0\2\0\1\0\2\0\1\0\3\0\3\0\1\0\2\0\1\0\3\0" +
		"\3\0\3\0\1\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0\1\0\3\0\2\0\1\0\3\0\3\0" +
		"\2\0\1\0\1\0\4\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0\1\0\1\0\0\0\3\0\3\0\2\0" +
		"\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0\1\0\5\0\3\0\1\0\3\0\1\0" +
		"\1\0\0\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0\1\0\1\0\0\0\1\0\0\0" +
		"\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(232,
		"\122\0\122\0\123\0\123\0\124\0\124\0\125\0\125\0\126\0\127\0\130\0\131\0\131\0\132" +
		"\0\132\0\133\0\134\0\134\0\135\0\136\0\137\0\140\0\140\0\140\0\141\0\141\0\141\0" +
		"\141\0\141\0\142\0\143\0\143\0\144\0\145\0\145\0\145\0\145\0\146\0\147\0\147\0\150" +
		"\0\150\0\151\0\152\0\152\0\153\0\154\0\155\0\155\0\155\0\156\0\156\0\156\0\157\0" +
		"\157\0\157\0\157\0\157\0\157\0\157\0\157\0\160\0\160\0\160\0\160\0\160\0\160\0\161" +
		"\0\162\0\162\0\162\0\163\0\163\0\163\0\164\0\164\0\164\0\164\0\165\0\165\0\165\0" +
		"\165\0\165\0\166\0\166\0\167\0\167\0\170\0\170\0\171\0\171\0\172\0\172\0\173\0\174" +
		"\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0" +
		"\174\0\174\0\174\0\175\0\176\0\177\0\177\0\200\0\200\0\201\0\201\0\201\0\202\0\202" +
		"\0\202\0\202\0\202\0\203\0\203\0\204\0\205\0\205\0\206\0\207\0\207\0\210\0\210\0" +
		"\210\0\211\0\211\0\212\0\212\0\212\0\213\0\214\0\214\0\215\0\215\0\215\0\215\0\215" +
		"\0\215\0\215\0\215\0\216\0\217\0\217\0\217\0\217\0\220\0\220\0\220\0\221\0\221\0" +
		"\222\0\223\0\223\0\223\0\224\0\224\0\225\0\226\0\226\0\226\0\227\0\230\0\230\0\231" +
		"\0\231\0\232\0\233\0\233\0\233\0\233\0\234\0\234\0\235\0\235\0\236\0\236\0\236\0" +
		"\236\0\237\0\237\0\237\0\240\0\240\0\240\0\240\0\240\0\241\0\241\0\242\0\242\0\243" +
		"\0\243\0\244\0\244\0\245\0\246\0\246\0\246\0\246\0\247\0\250\0\250\0\251\0\252\0" +
		"\253\0\253\0\254\0\254\0\255\0\255\0\256\0\256\0\257\0\257\0\260\0\260\0\261\0\261" +
		"\0\262\0\262\0");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"scon",
		"icon",
		"_skip",
		"_skip_comment",
		"_skip_multiline",
		"'%'",
		"'::='",
		"'::'",
		"'|'",
		"'||'",
		"'='",
		"'=='",
		"'!='",
		"';'",
		"'.'",
		"','",
		"':'",
		"'['",
		"']'",
		"'('",
		"'(?='",
		"'->'",
		"')'",
		"'{~'",
		"'}'",
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'+='",
		"'?'",
		"'!'",
		"'~'",
		"'&'",
		"'&&'",
		"'$'",
		"'@'",
		"error",
		"ID",
		"Ltrue",
		"Lfalse",
		"Lnew",
		"Lseparator",
		"Las",
		"Limport",
		"Lset",
		"Lbrackets",
		"Ls",
		"Lx",
		"Linline",
		"Lprec",
		"Lshift",
		"Lreturns",
		"Linput",
		"Lleft",
		"Lright",
		"Lnonassoc",
		"Lgenerate",
		"Lassert",
		"Lempty",
		"Lnonempty",
		"Lglobal",
		"Lexplicit",
		"Llookahead",
		"Lparam",
		"Lflag",
		"Lnoeoi",
		"Lsoft",
		"Lclass",
		"Linterface",
		"Lvoid",
		"Lspace",
		"Llayout",
		"Llanguage",
		"Llalr",
		"Llexer",
		"Lparser",
		"code",
		"'{'",
		"regexp",
		"'/'",
		"import__optlist",
		"input",
		"option_optlist",
		"header",
		"lexer_section",
		"parser_section",
		"parsing_algorithm",
		"import_",
		"option",
		"identifier",
		"symref",
		"symref_noargs",
		"rawType",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"named_pattern",
		"lexeme",
		"lexeme_attrs",
		"lexeme_attribute",
		"brackets_directive",
		"lexer_state_list_Comma_separated",
		"states_clause",
		"state_selector",
		"stateref_list_Comma_separated",
		"stateref",
		"lexer_state",
		"grammar_parts",
		"grammar_part",
		"nonterm",
		"nonterm_type",
		"implements",
		"assoc",
		"param_modifier",
		"template_param",
		"directive",
		"inputref_list_Comma_separated",
		"inputref",
		"references",
		"references_cs",
		"rule0_list_Or_separated",
		"rules",
		"rule0",
		"predicate",
		"rhsPrefix",
		"rhsSuffix",
		"ruleAction",
		"rhsParts",
		"rhsPart",
		"lookahead_predicate_list_And_separated",
		"rhsLookahead",
		"lookahead_predicate",
		"rhsStateMarker",
		"rhsAnnotated",
		"rhsAssignment",
		"rhsOptional",
		"rhsCast",
		"rhsUnordered",
		"rhsClass",
		"rhsPrimary",
		"rhsSet",
		"setPrimary",
		"setExpression",
		"annotation_list",
		"annotations",
		"annotation",
		"nonterm_param_list_Comma_separated",
		"nonterm_params",
		"nonterm_param",
		"param_ref",
		"argument_list_Comma_separated",
		"argument_list_Comma_separated_opt",
		"symref_args",
		"argument",
		"param_type",
		"param_value",
		"predicate_primary",
		"predicate_expression",
		"expression",
		"expression_list_Comma_separated",
		"expression_list_Comma_separated_opt",
		"map_entry_list_Comma_separated",
		"map_entry_list_Comma_separated_opt",
		"map_entry",
		"literal",
		"name",
		"qualified_id",
		"command",
		"syntax_problem",
		"parsing_algorithmopt",
		"rawTypeopt",
		"iconopt",
		"lexeme_attrsopt",
		"commandopt",
		"identifieropt",
		"implementsopt",
		"rhsSuffixopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int import__optlist = 82;
		int input = 83;
		int option_optlist = 84;
		int header = 85;
		int lexer_section = 86;
		int parser_section = 87;
		int parsing_algorithm = 88;
		int import_ = 89;
		int option = 90;
		int identifier = 91;
		int symref = 92;
		int symref_noargs = 93;
		int rawType = 94;
		int pattern = 95;
		int lexer_parts = 96;
		int lexer_part = 97;
		int named_pattern = 98;
		int lexeme = 99;
		int lexeme_attrs = 100;
		int lexeme_attribute = 101;
		int brackets_directive = 102;
		int lexer_state_list_Comma_separated = 103;
		int states_clause = 104;
		int state_selector = 105;
		int stateref_list_Comma_separated = 106;
		int stateref = 107;
		int lexer_state = 108;
		int grammar_parts = 109;
		int grammar_part = 110;
		int nonterm = 111;
		int nonterm_type = 112;
		int _implements = 113;
		int assoc = 114;
		int param_modifier = 115;
		int template_param = 116;
		int directive = 117;
		int inputref_list_Comma_separated = 118;
		int inputref = 119;
		int references = 120;
		int references_cs = 121;
		int rule0_list_Or_separated = 122;
		int rules = 123;
		int rule0 = 124;
		int predicate = 125;
		int rhsPrefix = 126;
		int rhsSuffix = 127;
		int ruleAction = 128;
		int rhsParts = 129;
		int rhsPart = 130;
		int lookahead_predicate_list_And_separated = 131;
		int rhsLookahead = 132;
		int lookahead_predicate = 133;
		int rhsStateMarker = 134;
		int rhsAnnotated = 135;
		int rhsAssignment = 136;
		int rhsOptional = 137;
		int rhsCast = 138;
		int rhsUnordered = 139;
		int rhsClass = 140;
		int rhsPrimary = 141;
		int rhsSet = 142;
		int setPrimary = 143;
		int setExpression = 144;
		int annotation_list = 145;
		int annotations = 146;
		int annotation = 147;
		int nonterm_param_list_Comma_separated = 148;
		int nonterm_params = 149;
		int nonterm_param = 150;
		int param_ref = 151;
		int argument_list_Comma_separated = 152;
		int argument_list_Comma_separated_opt = 153;
		int symref_args = 154;
		int argument = 155;
		int param_type = 156;
		int param_value = 157;
		int predicate_primary = 158;
		int predicate_expression = 159;
		int expression = 160;
		int expression_list_Comma_separated = 161;
		int expression_list_Comma_separated_opt = 162;
		int map_entry_list_Comma_separated = 163;
		int map_entry_list_Comma_separated_opt = 164;
		int map_entry = 165;
		int literal = 166;
		int name = 167;
		int qualified_id = 168;
		int command = 169;
		int syntax_problem = 170;
		int parsing_algorithmopt = 171;
		int rawTypeopt = 172;
		int iconopt = 173;
		int lexeme_attrsopt = 174;
		int commandopt = 175;
		int identifieropt = 176;
		int implementsopt = 177;
		int rhsSuffixopt = 178;
	}

	public interface Rules {
		int nonterm_type_nontermTypeAST = 61;  // nonterm_type ::= Lreturns symref_noargs
		int nonterm_type_nontermTypeHint = 62;  // nonterm_type ::= Linline Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint2 = 63;  // nonterm_type ::= Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint3 = 64;  // nonterm_type ::= Linterface identifieropt implementsopt
		int nonterm_type_nontermTypeHint4 = 65;  // nonterm_type ::= Lvoid
		int directive_directivePrio = 78;  // directive ::= '%' assoc references ';'
		int directive_directiveInput = 79;  // directive ::= '%' Linput inputref_list_Comma_separated ';'
		int directive_directiveAssert = 80;  // directive ::= '%' Lassert Lempty rhsSet ';'
		int directive_directiveAssert2 = 81;  // directive ::= '%' Lassert Lnonempty rhsSet ';'
		int directive_directiveSet = 82;  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 137;  // rhsOptional ::= rhsCast '?'
		int rhsCast_rhsAsLiteral = 140;  // rhsCast ::= rhsClass Las literal
		int rhsPrimary_rhsSymbol = 144;  // rhsPrimary ::= symref
		int rhsPrimary_rhsNested = 145;  // rhsPrimary ::= '(' rules ')'
		int rhsPrimary_rhsList = 146;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		int rhsPrimary_rhsList2 = 147;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		int rhsPrimary_rhsQuantifier = 148;  // rhsPrimary ::= rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 149;  // rhsPrimary ::= rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 150;  // rhsPrimary ::= '$' '(' rules ')'
		int setPrimary_setSymbol = 153;  // setPrimary ::= ID symref
		int setPrimary_setSymbol2 = 154;  // setPrimary ::= symref
		int setPrimary_setCompound = 155;  // setPrimary ::= '(' setExpression ')'
		int setPrimary_setComplement = 156;  // setPrimary ::= '~' setPrimary
		int setExpression_setBinary = 158;  // setExpression ::= setExpression '|' setExpression
		int setExpression_setBinary2 = 159;  // setExpression ::= setExpression '&' setExpression
		int nonterm_param_inlineParameter = 170;  // nonterm_param ::= ID identifier '=' param_value
		int nonterm_param_inlineParameter2 = 171;  // nonterm_param ::= ID identifier
		int predicate_primary_boolPredicate = 186;  // predicate_primary ::= '!' param_ref
		int predicate_primary_boolPredicate2 = 187;  // predicate_primary ::= param_ref
		int predicate_primary_comparePredicate = 188;  // predicate_primary ::= param_ref '==' literal
		int predicate_primary_comparePredicate2 = 189;  // predicate_primary ::= param_ref '!=' literal
		int predicate_expression_predicateBinary = 191;  // predicate_expression ::= predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 192;  // predicate_expression ::= predicate_expression '||' predicate_expression
		int expression_instance = 195;  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 196;  // expression ::= '[' expression_list_Comma_separated_opt ']'
	}

	/**
	 * -3-n   Lookahead (state id)
	 * -2     Error
	 * -1     Shift
	 * 0..n   Reduce (rule index)
	 */
	protected static int tmAction(int state, int symbol) {
		int p;
		if (tmAction[state] < -2) {
			for (p = -tmAction[state] - 3; tmLalr[p] >= 0; p += 2) {
				if (tmLalr[p] == symbol) {
					break;
				}
			}
			return tmLalr[p + 1];
		}
		return tmAction[state];
	}

	protected static int tmGoto(int state, int symbol) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol + 1] - 1;
		int i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if (i == state) {
				return lapg_sym_to[e];
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
			}
		}
		return -1;
	}

	protected int tmHead;
	protected Span[] tmStack;
	protected Span tmNext;
	protected TMLexer tmLexer;

	private Object parse(TMLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;
		int tmShiftsAfterError = 4;

		tmStack[0] = new Span();
		tmStack[0].state = initialState;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != finalState) {
			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
				tmShiftsAfterError++;
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				if (restore()) {
					if (tmShiftsAfterError >= 4) {
						reporter.error(MessageFormat.format("syntax error before line {0}", tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
					}
					if (tmShiftsAfterError <= 1) {
						tmNext = tmLexer.next();
					}
					tmShiftsAfterError = 0;
					continue;
				}
				if (tmHead < 0) {
					tmHead = 0;
					tmStack[0] = new Span();
					tmStack[0].state = initialState;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != finalState) {
			if (tmShiftsAfterError >= 4) {
				reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
			}
			throw new ParseException();
		}
		return tmStack[tmHead - 1].value;
	}

	protected boolean restore() {
		if (tmNext.symbol == 0) {
			return false;
		}
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 38) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new Span();
			tmStack[tmHead].symbol = 38;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 38);
			tmStack[tmHead].line = tmNext.line;
			tmStack[tmHead].offset = tmNext.offset;
			tmStack[tmHead].endoffset = tmNext.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.tokenText()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		Span left = new Span();
		left.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		left.symbol = tmRuleSymbol[rule];
		left.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		Span startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		left.line = startsym.line;
		left.offset = startsym.offset;
		left.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(left, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			cleanup(tmStack[tmHead]);
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = left;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
		switch (ruleIndex) {
			case 0:  // import__optlist ::= import__optlist import_
				((List<TmaImport>)tmLeft.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 1:  // import__optlist ::=
				tmLeft.value = new ArrayList();
				break;
			case 2:  // input ::= header import__optlist option_optlist lexer_section parser_section
				tmLeft.value = new TmaInput(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexer */,
						((List<ITmaGrammarPart>)tmStack[tmHead].value) /* parser */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 3:  // input ::= header import__optlist option_optlist lexer_section
				tmLeft.value = new TmaInput(
						((TmaHeader)tmStack[tmHead - 3].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 2].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 1].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead].value) /* lexer */,
						null /* parser */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 4:  // option_optlist ::= option_optlist option
				((List<TmaOption>)tmLeft.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 5:  // option_optlist ::=
				tmLeft.value = new ArrayList();
				break;
			case 6:  // header ::= Llanguage name '(' name ')' parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 5].value) /* name */,
						((TmaName)tmStack[tmHead - 3].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 7:  // header ::= Llanguage name parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 2].value) /* name */,
						null /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // lexer_section ::= '::' Llexer lexer_parts
				tmLeft.value = ((List<ITmaLexerPart>)tmStack[tmHead].value);
				break;
			case 9:  // parser_section ::= '::' Lparser grammar_parts
				tmLeft.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value);
				break;
			case 10:  // parsing_algorithm ::= Llalr '(' icon ')'
				tmLeft.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // import_ ::= Limport ID scon ';'
				tmLeft.value = new TmaImport(
						((String)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 12:  // import_ ::= Limport scon ';'
				tmLeft.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 13:  // option ::= ID '=' expression
				tmLeft.value = new TmaOption(
						((String)tmStack[tmHead - 2].value) /* key */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 14:  // option ::= syntax_problem
				tmLeft.value = new TmaOption(
						null /* key */,
						null /* value */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 15:  // identifier ::= ID
				tmLeft.value = new TmaIdentifier(
						((String)tmStack[tmHead].value) /* ID */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 16:  // symref ::= ID symref_args
				tmLeft.value = new TmaSymref(
						((String)tmStack[tmHead - 1].value) /* name */,
						((TmaSymrefArgs)tmStack[tmHead].value) /* args */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 17:  // symref ::= ID
				tmLeft.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // symref_noargs ::= ID
				tmLeft.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // rawType ::= code
				tmLeft.value = new TmaRawType(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 20:  // pattern ::= regexp
				tmLeft.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 21:  // lexer_parts ::= lexer_part
				tmLeft.value = new ArrayList();
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 22:  // lexer_parts ::= lexer_parts lexer_part
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 23:  // lexer_parts ::= lexer_parts syntax_problem
				((List<ITmaLexerPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 29:  // named_pattern ::= ID '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // lexeme ::= identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaRawType)tmStack[tmHead - 5].value) /* rawType */,
						((TmaPattern)tmStack[tmHead - 3].value) /* pattern */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead - 1].value) /* attrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // lexeme ::= identifier rawTypeopt ':'
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaRawType)tmStack[tmHead - 1].value) /* rawType */,
						null /* pattern */,
						null /* priority */,
						null /* attrs */,
						null /* command */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // lexeme_attrs ::= '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 33:  // lexeme_attribute ::= Lsoft
				tmLeft.value = TmaLexemeAttribute.LSOFT;
				break;
			case 34:  // lexeme_attribute ::= Lclass
				tmLeft.value = TmaLexemeAttribute.LCLASS;
				break;
			case 35:  // lexeme_attribute ::= Lspace
				tmLeft.value = TmaLexemeAttribute.LSPACE;
				break;
			case 36:  // lexeme_attribute ::= Llayout
				tmLeft.value = TmaLexemeAttribute.LLAYOUT;
				break;
			case 37:  // brackets_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
				tmLeft.value = new TmaBracketsDirective(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 38:  // lexer_state_list_Comma_separated ::= lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 39:  // lexer_state_list_Comma_separated ::= lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 40:  // states_clause ::= '%' Ls lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						false /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 41:  // states_clause ::= '%' Lx lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						true /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 42:  // state_selector ::= '[' stateref_list_Comma_separated ']'
				tmLeft.value = new TmaStateSelector(
						((List<TmaStateref>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 43:  // stateref_list_Comma_separated ::= stateref_list_Comma_separated ',' stateref
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 44:  // stateref_list_Comma_separated ::= stateref
				tmLeft.value = new ArrayList();
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 45:  // stateref ::= ID
				tmLeft.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 46:  // lexer_state ::= identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 47:  // grammar_parts ::= grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 48:  // grammar_parts ::= grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 49:  // grammar_parts ::= grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 53:  // nonterm ::= annotations identifier nonterm_params nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // nonterm ::= annotations identifier nonterm_params '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 55:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 56:  // nonterm ::= annotations identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 57:  // nonterm ::= identifier nonterm_params nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // nonterm ::= identifier nonterm_params '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // nonterm ::= identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 61:  // nonterm_type ::= Lreturns symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 62:  // nonterm_type ::= Linline Lclass identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 63:  // nonterm_type ::= Lclass identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 64:  // nonterm_type ::= Linterface identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LINTERFACE /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 65:  // nonterm_type ::= Lvoid
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LVOID /* kind */,
						null /* name */,
						null /* _implements */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 67:  // implements ::= ':' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 68:  // assoc ::= Lleft
				tmLeft.value = TmaAssoc.LLEFT;
				break;
			case 69:  // assoc ::= Lright
				tmLeft.value = TmaAssoc.LRIGHT;
				break;
			case 70:  // assoc ::= Lnonassoc
				tmLeft.value = TmaAssoc.LNONASSOC;
				break;
			case 71:  // param_modifier ::= Lexplicit
				tmLeft.value = TmaParamModifier.LEXPLICIT;
				break;
			case 72:  // param_modifier ::= Lglobal
				tmLeft.value = TmaParamModifier.LGLOBAL;
				break;
			case 73:  // param_modifier ::= Llookahead
				tmLeft.value = TmaParamModifier.LLOOKAHEAD;
				break;
			case 74:  // template_param ::= '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 75:  // template_param ::= '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // template_param ::= '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // template_param ::= '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 78:  // directive ::= '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 79:  // directive ::= '%' Linput inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // directive ::= '%' Lassert Lempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 81:  // directive ::= '%' Lassert Lnonempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LNONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((String)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 83:  // inputref_list_Comma_separated ::= inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 84:  // inputref_list_Comma_separated ::= inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 85:  // inputref ::= symref_noargs Lnoeoi
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 86:  // inputref ::= symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 87:  // references ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 88:  // references ::= references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 89:  // references_cs ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 90:  // references_cs ::= references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 91:  // rule0_list_Or_separated ::= rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 92:  // rule0_list_Or_separated ::= rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 94:  // rule0 ::= predicate rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 4].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 95:  // rule0 ::= predicate rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 96:  // rule0 ::= predicate rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 97:  // rule0 ::= predicate rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 98:  // rule0 ::= predicate rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // rule0 ::= predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // rule0 ::= predicate ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // rule0 ::= predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // rule0 ::= rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // rule0 ::= rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // rule0 ::= rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // rule0 ::= rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // rule0 ::= rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rule0 ::= ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // rule0 ::= rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // rule0 ::= syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						null /* suffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // predicate ::= '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 112:  // rhsPrefix ::= annotations ':'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rhsSuffix ::= '%' Lprec symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // rhsSuffix ::= '%' Lshift symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // ruleAction ::= '{~' identifier scon '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((String)tmStack[tmHead - 1].value) /* parameter */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // ruleAction ::= '{~' identifier '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* action */,
						null /* parameter */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // rhsParts ::= rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 118:  // rhsParts ::= rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 119:  // rhsParts ::= rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 125:  // lookahead_predicate_list_And_separated ::= lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 126:  // lookahead_predicate_list_And_separated ::= lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 127:  // rhsLookahead ::= '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // lookahead_predicate ::= '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // lookahead_predicate ::= symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // rhsStateMarker ::= '.' ID
				tmLeft.value = new TmaRhsStateMarker(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // rhsAnnotated ::= annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // rhsAssignment ::= identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // rhsAssignment ::= identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 137:  // rhsOptional ::= rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // rhsCast ::= rhsClass Las symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 140:  // rhsCast ::= rhsClass Las literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rhsUnordered ::= rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // rhsClass ::= identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // rhsPrimary ::= symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // rhsPrimary ::= '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 146:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // rhsPrimary ::= rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // rhsPrimary ::= rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rhsPrimary ::= '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rhsSet ::= Lset '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // setPrimary ::= ID symref
				tmLeft.value = new TmaSetSymbol(
						((String)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // setPrimary ::= symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // setPrimary ::= '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // setPrimary ::= '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // setExpression ::= setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // setExpression ::= setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 161:  // annotation_list ::= annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 162:  // annotations ::= annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // annotation ::= '@' ID '=' expression
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // annotation ::= '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // annotation ::= '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 166:  // nonterm_param_list_Comma_separated ::= nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 167:  // nonterm_param_list_Comma_separated ::= nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 168:  // nonterm_params ::= '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 170:  // nonterm_param ::= ID identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 171:  // nonterm_param ::= ID identifier
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 172:  // param_ref ::= identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 173:  // argument_list_Comma_separated ::= argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 174:  // argument_list_Comma_separated ::= argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 177:  // symref_args ::= '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 178:  // argument ::= param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 179:  // argument ::= '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 180:  // argument ::= '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 181:  // argument ::= param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 182:  // param_type ::= Lflag
				tmLeft.value = TmaParamType.LFLAG;
				break;
			case 183:  // param_type ::= Lparam
				tmLeft.value = TmaParamType.LPARAM;
				break;
			case 186:  // predicate_primary ::= '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // predicate_primary ::= param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 188:  // predicate_primary ::= param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 189:  // predicate_primary ::= param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // predicate_expression ::= predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // predicate_expression ::= predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // expression ::= '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 198:  // expression_list_Comma_separated ::= expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 199:  // expression_list_Comma_separated ::= expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 202:  // map_entry_list_Comma_separated ::= map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 203:  // map_entry_list_Comma_separated ::= map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 206:  // map_entry ::= ID ':' expression
				tmLeft.value = new TmaMapEntry(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 207:  // literal ::= scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 208:  // literal ::= icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 209:  // literal ::= Ltrue
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 210:  // literal ::= Lfalse
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 211:  // name ::= qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 213:  // qualified_id ::= qualified_id '.' ID
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 214:  // command ::= code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 215:  // syntax_problem ::= error
				tmLeft.value = new TmaSyntaxProblem(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(Span value) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(Span value) {
	}

	public TmaInput parseInput(TMLexer lexer) throws IOException, ParseException {
		return (TmaInput) parse(lexer, 0, 412);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 413);
	}
}
