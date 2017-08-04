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
	private static final int[] tmAction = TMLexer.unpack_int(456,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\336\0\337\0\uffb5\uffff\346\0\uff63" +
		"\uffff\340\0\341\0\uffff\uffff\321\0\320\0\324\0\343\0\ufeef\uffff\ufee7\uffff\ufedb" +
		"\uffff\326\0\ufe93\uffff\uffff\uffff\ufe8d\uffff\20\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\347\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\323" +
		"\0\uffff\uffff\uffff\uffff\17\0\273\0\ufe45\uffff\ufe3d\uffff\uffff\uffff\275\0\ufe37" +
		"\uffff\uffff\uffff\uffff\uffff\7\0\344\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufdf3\uffff\4\0\16\0\325\0\302\0\303\0\uffff\uffff\uffff\uffff\300\0\uffff" +
		"\uffff\ufded\uffff\uffff\uffff\332\0\ufde7\uffff\uffff\uffff\14\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\2\0\22\0\310\0\301\0\307\0\274\0\uffff\uffff\uffff" +
		"\uffff\322\0\uffff\uffff\12\0\13\0\uffff\uffff\uffff\uffff\ufde1\uffff\ufdd9\uffff" +
		"\ufdd3\uffff\25\0\31\0\34\0\uffff\uffff\32\0\33\0\30\0\15\0\uffff\uffff\335\0\331" +
		"\0\6\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\61\0\uffff\uffff\42\0\uffff" +
		"\uffff\23\0\351\0\uffff\uffff\26\0\27\0\uffff\uffff\ufd87\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\ufd81\uffff\63\0\66\0\67\0\70\0\ufd37\uffff\uffff\uffff\260\0" +
		"\uffff\uffff\62\0\uffff\uffff\56\0\uffff\uffff\37\0\uffff\uffff\40\0\24\0\35\0\ufceb" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\120\0\121\0\122\0\uffff\uffff\uffff\uffff" +
		"\124\0\123\0\125\0\306\0\305\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufc9b" +
		"\uffff\264\0\ufc4d\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufbf1\uffff" +
		"\ufba9\uffff\115\0\116\0\uffff\uffff\uffff\uffff\uffff\uffff\64\0\65\0\257\0\uffff" +
		"\uffff\uffff\uffff\57\0\uffff\uffff\60\0\41\0\ufb61\uffff\36\0\ufb0d\uffff\ufabd" +
		"\uffff\uffff\uffff\143\0\uffff\uffff\uffff\uffff\uffff\uffff\141\0\uffff\uffff\146" +
		"\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufab5\uffff\uffff\uffff\uffff\uffff\ufa59\uffff\uffff\uffff\345\0\uffff\uffff" +
		"\237\0\uf9e9\uffff\uffff\uffff\153\0\uf9e1\uffff\uf987\uffff\365\0\uf92d\uffff\uf8d1" +
		"\uffff\213\0\212\0\207\0\222\0\224\0\uf871\uffff\210\0\uf80f\uffff\uf7ab\uffff\246" +
		"\0\uffff\uffff\211\0\175\0\uf743\uffff\uf739\uffff\uf72d\uffff\uffff\uffff\266\0" +
		"\270\0\uf6e7\uffff\111\0\361\0\uf69f\uffff\uf697\uffff\uf68f\uffff\uffff\uffff\uf633" +
		"\uffff\uf5d7\uffff\uffff\uffff\uffff\uffff\uf57b\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\54\0\55\0\353\0\uf51f\uffff\uf4cd\uffff\144\0\133\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\134\0\uffff\uffff\132\0\147\0\uffff\uffff\uffff\uffff\131" +
		"\0\262\0\uffff\uffff\uffff\uffff\221\0\uffff\uffff\uf479\uffff\315\0\uffff\uffff" +
		"\uffff\uffff\uf46d\uffff\uffff\uffff\220\0\uffff\uffff\215\0\uf411\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uf3b5\uffff\110\0\uf357\uffff\uf2fd\uffff\uf2a1" +
		"\uffff\uf297\uffff\uffff\uffff\uf23b\uffff\uf231\uffff\206\0\uf1d1\uffff\uffff\uffff" +
		"\230\0\uffff\uffff\243\0\244\0\177\0\223\0\173\0\uffff\uffff\uf1c7\uffff\uffff\uffff" +
		"\267\0\uf1bf\uffff\uffff\uffff\363\0\113\0\114\0\uffff\uffff\uf1b7\uffff\uffff\uffff" +
		"\uffff\uffff\uf15b\uffff\uffff\uffff\uf0ff\uffff\uffff\uffff\uf0a3\uffff\uffff\uffff" +
		"\uf047\uffff\uefeb\uffff\uffff\uffff\uffff\uffff\uffff\uffff\355\0\uef8f\uffff\uef3f" +
		"\uffff\142\0\uffff\uffff\135\0\136\0\140\0\uffff\uffff\127\0\uffff\uffff\200\0\201" +
		"\0\311\0\uffff\uffff\uffff\uffff\uffff\uffff\176\0\uffff\uffff\240\0\uffff\uffff" +
		"\217\0\216\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ueeed\uffff\251\0\254" +
		"\0\uffff\uffff\uffff\uffff\225\0\ueea3\uffff\226\0\152\0\uee3b\uffff\ueddf\uffff" +
		"\uedd5\uffff\163\0\uedcb\uffff\167\0\171\0\uedc1\uffff\232\0\233\0\202\0\uffff\uffff" +
		"\265\0\112\0\150\0\ued61\uffff\106\0\uffff\uffff\107\0\104\0\uffff\uffff\ued59\uffff" +
		"\uffff\uffff\100\0\uffff\uffff\uecfd\uffff\uffff\uffff\uffff\uffff\ueca1\uffff\uffff" +
		"\uffff\uec45\uffff\50\0\51\0\52\0\53\0\uffff\uffff\357\0\45\0\uebe9\uffff\137\0\uffff" +
		"\uffff\130\0\313\0\314\0\ueb99\uffff\ueb91\uffff\uffff\uffff\214\0\245\0\uffff\uffff" +
		"\253\0\250\0\uffff\uffff\247\0\uffff\uffff\ueb89\uffff\157\0\161\0\165\0\271\0\uffff" +
		"\uffff\105\0\102\0\uffff\uffff\103\0\76\0\uffff\uffff\77\0\74\0\uffff\uffff\ueb7f" +
		"\uffff\uffff\uffff\47\0\43\0\126\0\uffff\uffff\252\0\ueb23\uffff\ueb1b\uffff\155" +
		"\0\151\0\101\0\75\0\72\0\uffff\uffff\73\0\242\0\241\0\71\0\uffff\uffff\uffff\uffff" +
		"\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(5354,
		"\7\0\1\0\44\0\1\0\45\0\1\0\53\0\1\0\56\0\1\0\57\0\1\0\60\0\1\0\61\0\1\0\62\0\1\0" +
		"\63\0\1\0\64\0\1\0\65\0\1\0\66\0\1\0\67\0\1\0\70\0\1\0\71\0\1\0\72\0\1\0\73\0\1\0" +
		"\74\0\1\0\75\0\1\0\76\0\1\0\77\0\1\0\100\0\1\0\101\0\1\0\102\0\1\0\103\0\1\0\104" +
		"\0\1\0\105\0\1\0\106\0\1\0\107\0\1\0\110\0\1\0\111\0\1\0\112\0\1\0\113\0\1\0\114" +
		"\0\1\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\2\0\uffff\uffff\21\0\uffff\uffff\44" +
		"\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff" +
		"\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\50\0\uffff\uffff\22\0\330\0\uffff\uffff\ufffe\uffff\30\0\uffff" +
		"\uffff\0\0\21\0\6\0\21\0\7\0\21\0\10\0\21\0\15\0\21\0\16\0\21\0\17\0\21\0\20\0\21" +
		"\0\22\0\21\0\23\0\21\0\24\0\21\0\25\0\21\0\26\0\21\0\32\0\21\0\33\0\21\0\35\0\21" +
		"\0\40\0\21\0\42\0\21\0\43\0\21\0\44\0\21\0\45\0\21\0\51\0\21\0\52\0\21\0\54\0\21" +
		"\0\56\0\21\0\57\0\21\0\60\0\21\0\61\0\21\0\62\0\21\0\63\0\21\0\64\0\21\0\65\0\21" +
		"\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0\72\0\21\0\73\0\21\0\74\0\21\0\75\0\21" +
		"\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21\0\102\0\21\0\103\0\21\0\104\0\21\0\105" +
		"\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111\0\21\0\112\0\21\0\113\0\21\0\114\0\21" +
		"\0\115\0\21\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\112\0\uffff\uffff\15\0\350" +
		"\0\uffff\uffff\ufffe\uffff\16\0\uffff\uffff\15\0\342\0\23\0\342\0\26\0\342\0\112" +
		"\0\342\0\uffff\uffff\ufffe\uffff\53\0\uffff\uffff\7\0\5\0\44\0\5\0\45\0\5\0\56\0" +
		"\5\0\57\0\5\0\60\0\5\0\61\0\5\0\62\0\5\0\63\0\5\0\64\0\5\0\65\0\5\0\66\0\5\0\67\0" +
		"\5\0\70\0\5\0\71\0\5\0\72\0\5\0\73\0\5\0\74\0\5\0\75\0\5\0\76\0\5\0\77\0\5\0\100" +
		"\0\5\0\101\0\5\0\102\0\5\0\103\0\5\0\104\0\5\0\105\0\5\0\106\0\5\0\107\0\5\0\110" +
		"\0\5\0\111\0\5\0\112\0\5\0\113\0\5\0\114\0\5\0\uffff\uffff\ufffe\uffff\17\0\uffff" +
		"\uffff\22\0\327\0\uffff\uffff\ufffe\uffff\33\0\uffff\uffff\37\0\uffff\uffff\45\0" +
		"\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff" +
		"\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\31\0\277\0\uffff\uffff\ufffe" +
		"\uffff\20\0\uffff\uffff\17\0\304\0\31\0\304\0\uffff\uffff\ufffe\uffff\17\0\uffff" +
		"\uffff\31\0\276\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\26\0\334\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\0\0\3\0\uffff" +
		"\uffff\ufffe\uffff\17\0\uffff\uffff\26\0\333\0\uffff\uffff\ufffe\uffff\112\0\uffff" +
		"\uffff\15\0\350\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\20\0\17\0\115\0\17\0\uffff" +
		"\uffff\ufffe\uffff\115\0\uffff\uffff\20\0\352\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0" +
		"\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff" +
		"\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\0\0\10\0\7\0\10\0\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\20" +
		"\0\352\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\0\0\11\0\uffff\uffff\ufffe" +
		"\uffff\43\0\uffff\uffff\20\0\261\0\23\0\261\0\42\0\261\0\45\0\261\0\54\0\261\0\56" +
		"\0\261\0\57\0\261\0\60\0\261\0\61\0\261\0\62\0\261\0\63\0\261\0\64\0\261\0\65\0\261" +
		"\0\66\0\261\0\67\0\261\0\70\0\261\0\71\0\261\0\72\0\261\0\73\0\261\0\74\0\261\0\75" +
		"\0\261\0\76\0\261\0\77\0\261\0\100\0\261\0\101\0\261\0\102\0\261\0\103\0\261\0\104" +
		"\0\261\0\105\0\261\0\106\0\261\0\107\0\261\0\110\0\261\0\111\0\261\0\112\0\261\0" +
		"\113\0\261\0\114\0\261\0\uffff\uffff\ufffe\uffff\117\0\uffff\uffff\0\0\46\0\6\0\46" +
		"\0\7\0\46\0\27\0\46\0\30\0\46\0\44\0\46\0\45\0\46\0\56\0\46\0\57\0\46\0\60\0\46\0" +
		"\61\0\46\0\62\0\46\0\63\0\46\0\64\0\46\0\65\0\46\0\66\0\46\0\67\0\46\0\70\0\46\0" +
		"\71\0\46\0\72\0\46\0\73\0\46\0\74\0\46\0\75\0\46\0\76\0\46\0\77\0\46\0\100\0\46\0" +
		"\101\0\46\0\102\0\46\0\103\0\46\0\104\0\46\0\105\0\46\0\106\0\46\0\107\0\46\0\110" +
		"\0\46\0\111\0\46\0\112\0\46\0\113\0\46\0\114\0\46\0\uffff\uffff\ufffe\uffff\12\0" +
		"\uffff\uffff\20\0\263\0\23\0\263\0\42\0\263\0\43\0\263\0\45\0\263\0\54\0\263\0\56" +
		"\0\263\0\57\0\263\0\60\0\263\0\61\0\263\0\62\0\263\0\63\0\263\0\64\0\263\0\65\0\263" +
		"\0\66\0\263\0\67\0\263\0\70\0\263\0\71\0\263\0\72\0\263\0\73\0\263\0\74\0\263\0\75" +
		"\0\263\0\76\0\263\0\77\0\263\0\100\0\263\0\101\0\263\0\102\0\263\0\103\0\263\0\104" +
		"\0\263\0\105\0\263\0\106\0\263\0\107\0\263\0\110\0\263\0\111\0\263\0\112\0\263\0" +
		"\113\0\263\0\114\0\263\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff" +
		"\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\20\0\362\0\25" +
		"\0\362\0\55\0\362\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\20\0\362\0\25\0\362\0\55\0\362\0\uffff\uffff\ufffe\uffff\2\0\uffff" +
		"\uffff\0\0\354\0\6\0\354\0\7\0\354\0\23\0\354\0\27\0\354\0\30\0\354\0\44\0\354\0" +
		"\45\0\354\0\56\0\354\0\57\0\354\0\60\0\354\0\61\0\354\0\62\0\354\0\63\0\354\0\64" +
		"\0\354\0\65\0\354\0\66\0\354\0\67\0\354\0\70\0\354\0\71\0\354\0\72\0\354\0\73\0\354" +
		"\0\74\0\354\0\75\0\354\0\76\0\354\0\77\0\354\0\100\0\354\0\101\0\354\0\102\0\354" +
		"\0\103\0\354\0\104\0\354\0\105\0\354\0\106\0\354\0\107\0\354\0\110\0\354\0\111\0" +
		"\354\0\112\0\354\0\113\0\354\0\114\0\354\0\115\0\354\0\uffff\uffff\ufffe\uffff\117" +
		"\0\uffff\uffff\0\0\44\0\6\0\44\0\7\0\44\0\27\0\44\0\30\0\44\0\44\0\44\0\45\0\44\0" +
		"\56\0\44\0\57\0\44\0\60\0\44\0\61\0\44\0\62\0\44\0\63\0\44\0\64\0\44\0\65\0\44\0" +
		"\66\0\44\0\67\0\44\0\70\0\44\0\71\0\44\0\72\0\44\0\73\0\44\0\74\0\44\0\75\0\44\0" +
		"\76\0\44\0\77\0\44\0\100\0\44\0\101\0\44\0\102\0\44\0\103\0\44\0\104\0\44\0\105\0" +
		"\44\0\106\0\44\0\107\0\44\0\110\0\44\0\111\0\44\0\112\0\44\0\113\0\44\0\114\0\44" +
		"\0\uffff\uffff\ufffe\uffff\102\0\uffff\uffff\15\0\145\0\17\0\145\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0" +
		"\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff" +
		"\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\25\0\366\0\26\0\366\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff\12\0\17\0\20\0\17" +
		"\0\34\0\17\0\6\0\21\0\10\0\21\0\15\0\21\0\16\0\21\0\23\0\21\0\24\0\21\0\25\0\21\0" +
		"\26\0\21\0\32\0\21\0\33\0\21\0\35\0\21\0\40\0\21\0\42\0\21\0\43\0\21\0\44\0\21\0" +
		"\45\0\21\0\51\0\21\0\52\0\21\0\54\0\21\0\56\0\21\0\57\0\21\0\60\0\21\0\61\0\21\0" +
		"\62\0\21\0\63\0\21\0\64\0\21\0\65\0\21\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0" +
		"\72\0\21\0\73\0\21\0\74\0\21\0\75\0\21\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21" +
		"\0\102\0\21\0\103\0\21\0\104\0\21\0\105\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111" +
		"\0\21\0\112\0\21\0\113\0\21\0\114\0\21\0\115\0\21\0\uffff\uffff\ufffe\uffff\10\0" +
		"\uffff\uffff\15\0\154\0\26\0\154\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\45\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\366\0\15\0\366\0\25\0\366\0\26\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff" +
		"\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff" +
		"\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\26\0\366\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff" +
		"\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0" +
		"\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff" +
		"\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366" +
		"\0\26\0\366\0\uffff\uffff\ufffe\uffff\40\0\uffff\uffff\6\0\204\0\10\0\204\0\15\0" +
		"\204\0\16\0\204\0\23\0\204\0\24\0\204\0\25\0\204\0\26\0\204\0\42\0\204\0\43\0\204" +
		"\0\44\0\204\0\45\0\204\0\51\0\204\0\54\0\204\0\56\0\204\0\57\0\204\0\60\0\204\0\61" +
		"\0\204\0\62\0\204\0\63\0\204\0\64\0\204\0\65\0\204\0\66\0\204\0\67\0\204\0\70\0\204" +
		"\0\71\0\204\0\72\0\204\0\73\0\204\0\74\0\204\0\75\0\204\0\76\0\204\0\77\0\204\0\100" +
		"\0\204\0\101\0\204\0\102\0\204\0\103\0\204\0\104\0\204\0\105\0\204\0\106\0\204\0" +
		"\107\0\204\0\110\0\204\0\111\0\204\0\112\0\204\0\113\0\204\0\114\0\204\0\115\0\204" +
		"\0\uffff\uffff\ufffe\uffff\35\0\uffff\uffff\6\0\227\0\10\0\227\0\15\0\227\0\16\0" +
		"\227\0\23\0\227\0\24\0\227\0\25\0\227\0\26\0\227\0\40\0\227\0\42\0\227\0\43\0\227" +
		"\0\44\0\227\0\45\0\227\0\51\0\227\0\54\0\227\0\56\0\227\0\57\0\227\0\60\0\227\0\61" +
		"\0\227\0\62\0\227\0\63\0\227\0\64\0\227\0\65\0\227\0\66\0\227\0\67\0\227\0\70\0\227" +
		"\0\71\0\227\0\72\0\227\0\73\0\227\0\74\0\227\0\75\0\227\0\76\0\227\0\77\0\227\0\100" +
		"\0\227\0\101\0\227\0\102\0\227\0\103\0\227\0\104\0\227\0\105\0\227\0\106\0\227\0" +
		"\107\0\227\0\110\0\227\0\111\0\227\0\112\0\227\0\113\0\227\0\114\0\227\0\115\0\227" +
		"\0\uffff\uffff\ufffe\uffff\52\0\uffff\uffff\6\0\231\0\10\0\231\0\15\0\231\0\16\0" +
		"\231\0\23\0\231\0\24\0\231\0\25\0\231\0\26\0\231\0\35\0\231\0\40\0\231\0\42\0\231" +
		"\0\43\0\231\0\44\0\231\0\45\0\231\0\51\0\231\0\54\0\231\0\56\0\231\0\57\0\231\0\60" +
		"\0\231\0\61\0\231\0\62\0\231\0\63\0\231\0\64\0\231\0\65\0\231\0\66\0\231\0\67\0\231" +
		"\0\70\0\231\0\71\0\231\0\72\0\231\0\73\0\231\0\74\0\231\0\75\0\231\0\76\0\231\0\77" +
		"\0\231\0\100\0\231\0\101\0\231\0\102\0\231\0\103\0\231\0\104\0\231\0\105\0\231\0" +
		"\106\0\231\0\107\0\231\0\110\0\231\0\111\0\231\0\112\0\231\0\113\0\231\0\114\0\231" +
		"\0\115\0\231\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\33\0\uffff\uffff\6\0\235" +
		"\0\10\0\235\0\15\0\235\0\16\0\235\0\23\0\235\0\24\0\235\0\25\0\235\0\26\0\235\0\35" +
		"\0\235\0\40\0\235\0\42\0\235\0\43\0\235\0\44\0\235\0\45\0\235\0\51\0\235\0\52\0\235" +
		"\0\54\0\235\0\56\0\235\0\57\0\235\0\60\0\235\0\61\0\235\0\62\0\235\0\63\0\235\0\64" +
		"\0\235\0\65\0\235\0\66\0\235\0\67\0\235\0\70\0\235\0\71\0\235\0\72\0\235\0\73\0\235" +
		"\0\74\0\235\0\75\0\235\0\76\0\235\0\77\0\235\0\100\0\235\0\101\0\235\0\102\0\235" +
		"\0\103\0\235\0\104\0\235\0\105\0\235\0\106\0\235\0\107\0\235\0\110\0\235\0\111\0" +
		"\235\0\112\0\235\0\113\0\235\0\114\0\235\0\115\0\235\0\uffff\uffff\ufffe\uffff\25" +
		"\0\uffff\uffff\10\0\174\0\15\0\174\0\26\0\174\0\uffff\uffff\ufffe\uffff\120\0\uffff" +
		"\uffff\10\0\203\0\15\0\203\0\20\0\203\0\26\0\203\0\uffff\uffff\ufffe\uffff\45\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\17\0\17\0\31\0\17\0\uffff\uffff" +
		"\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff" +
		"\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\20\0\362\0\25" +
		"\0\362\0\55\0\362\0\uffff\uffff\ufffe\uffff\55\0\uffff\uffff\20\0\364\0\25\0\364" +
		"\0\uffff\uffff\ufffe\uffff\55\0\uffff\uffff\20\0\364\0\25\0\364\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0" +
		"\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff" +
		"\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\23\0\uffff\uffff\0\0\356\0\6\0\356\0\7\0\356\0\27\0\356\0\30\0\356\0" +
		"\44\0\356\0\45\0\356\0\56\0\356\0\57\0\356\0\60\0\356\0\61\0\356\0\62\0\356\0\63" +
		"\0\356\0\64\0\356\0\65\0\356\0\66\0\356\0\67\0\356\0\70\0\356\0\71\0\356\0\72\0\356" +
		"\0\73\0\356\0\74\0\356\0\75\0\356\0\76\0\356\0\77\0\356\0\100\0\356\0\101\0\356\0" +
		"\102\0\356\0\103\0\356\0\104\0\356\0\105\0\356\0\106\0\356\0\107\0\356\0\110\0\356" +
		"\0\111\0\356\0\112\0\356\0\113\0\356\0\114\0\356\0\115\0\356\0\uffff\uffff\ufffe" +
		"\uffff\2\0\uffff\uffff\0\0\354\0\6\0\354\0\7\0\354\0\23\0\354\0\27\0\354\0\30\0\354" +
		"\0\44\0\354\0\45\0\354\0\56\0\354\0\57\0\354\0\60\0\354\0\61\0\354\0\62\0\354\0\63" +
		"\0\354\0\64\0\354\0\65\0\354\0\66\0\354\0\67\0\354\0\70\0\354\0\71\0\354\0\72\0\354" +
		"\0\73\0\354\0\74\0\354\0\75\0\354\0\76\0\354\0\77\0\354\0\100\0\354\0\101\0\354\0" +
		"\102\0\354\0\103\0\354\0\104\0\354\0\105\0\354\0\106\0\354\0\107\0\354\0\110\0\354" +
		"\0\111\0\354\0\112\0\354\0\113\0\354\0\114\0\354\0\115\0\354\0\uffff\uffff\ufffe" +
		"\uffff\13\0\uffff\uffff\14\0\uffff\uffff\11\0\312\0\22\0\312\0\41\0\312\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42" +
		"\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\51\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\25\0\366\0\26\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\25\0\366\0\26\0\366\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\26\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\45\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\366\0\15\0\366\0\25\0\366\0\26\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112" +
		"\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff" +
		"\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101" +
		"\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff" +
		"\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\26\0\366\0\uffff" +
		"\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\164\0\15\0\164\0\26\0\164\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42" +
		"\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366" +
		"\0\26\0\366\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\170\0\15\0\170\0\26\0" +
		"\170\0\uffff\uffff\ufffe\uffff\40\0\uffff\uffff\6\0\205\0\10\0\205\0\15\0\205\0\16" +
		"\0\205\0\23\0\205\0\24\0\205\0\25\0\205\0\26\0\205\0\42\0\205\0\43\0\205\0\44\0\205" +
		"\0\45\0\205\0\51\0\205\0\54\0\205\0\56\0\205\0\57\0\205\0\60\0\205\0\61\0\205\0\62" +
		"\0\205\0\63\0\205\0\64\0\205\0\65\0\205\0\66\0\205\0\67\0\205\0\70\0\205\0\71\0\205" +
		"\0\72\0\205\0\73\0\205\0\74\0\205\0\75\0\205\0\76\0\205\0\77\0\205\0\100\0\205\0" +
		"\101\0\205\0\102\0\205\0\103\0\205\0\104\0\205\0\105\0\205\0\106\0\205\0\107\0\205" +
		"\0\110\0\205\0\111\0\205\0\112\0\205\0\113\0\205\0\114\0\205\0\115\0\205\0\uffff" +
		"\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\172\0\15\0\172\0\26\0\172\0\uffff\uffff" +
		"\ufffe\uffff\12\0\uffff\uffff\17\0\272\0\31\0\272\0\uffff\uffff\ufffe\uffff\55\0" +
		"\uffff\uffff\20\0\364\0\25\0\364\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff" +
		"\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112" +
		"\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff" +
		"\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101" +
		"\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff" +
		"\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\0\0\360\0\6\0\360" +
		"\0\7\0\360\0\27\0\360\0\30\0\360\0\44\0\360\0\45\0\360\0\56\0\360\0\57\0\360\0\60" +
		"\0\360\0\61\0\360\0\62\0\360\0\63\0\360\0\64\0\360\0\65\0\360\0\66\0\360\0\67\0\360" +
		"\0\70\0\360\0\71\0\360\0\72\0\360\0\73\0\360\0\74\0\360\0\75\0\360\0\76\0\360\0\77" +
		"\0\360\0\100\0\360\0\101\0\360\0\102\0\360\0\103\0\360\0\104\0\360\0\105\0\360\0" +
		"\106\0\360\0\107\0\360\0\110\0\360\0\111\0\360\0\112\0\360\0\113\0\360\0\114\0\360" +
		"\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0\356\0\6\0\356\0\7\0\356\0\27\0\356" +
		"\0\30\0\356\0\44\0\356\0\45\0\356\0\56\0\356\0\57\0\356\0\60\0\356\0\61\0\356\0\62" +
		"\0\356\0\63\0\356\0\64\0\356\0\65\0\356\0\66\0\356\0\67\0\356\0\70\0\356\0\71\0\356" +
		"\0\72\0\356\0\73\0\356\0\74\0\356\0\75\0\356\0\76\0\356\0\77\0\356\0\100\0\356\0" +
		"\101\0\356\0\102\0\356\0\103\0\356\0\104\0\356\0\105\0\356\0\106\0\356\0\107\0\356" +
		"\0\110\0\356\0\111\0\356\0\112\0\356\0\113\0\356\0\114\0\356\0\115\0\356\0\uffff" +
		"\uffff\ufffe\uffff\30\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\10\0\21\0\26\0\21\0\40\0\21\0\uffff\uffff\ufffe\uffff\32\0\uffff" +
		"\uffff\33\0\uffff\uffff\6\0\236\0\10\0\236\0\15\0\236\0\16\0\236\0\23\0\236\0\24" +
		"\0\236\0\25\0\236\0\26\0\236\0\35\0\236\0\40\0\236\0\42\0\236\0\43\0\236\0\44\0\236" +
		"\0\45\0\236\0\51\0\236\0\52\0\236\0\54\0\236\0\56\0\236\0\57\0\236\0\60\0\236\0\61" +
		"\0\236\0\62\0\236\0\63\0\236\0\64\0\236\0\65\0\236\0\66\0\236\0\67\0\236\0\70\0\236" +
		"\0\71\0\236\0\72\0\236\0\73\0\236\0\74\0\236\0\75\0\236\0\76\0\236\0\77\0\236\0\100" +
		"\0\236\0\101\0\236\0\102\0\236\0\103\0\236\0\104\0\236\0\105\0\236\0\106\0\236\0" +
		"\107\0\236\0\110\0\236\0\111\0\236\0\112\0\236\0\113\0\236\0\114\0\236\0\115\0\236" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0" +
		"\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\26\0\366\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\160" +
		"\0\15\0\160\0\26\0\160\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\162\0\15\0" +
		"\162\0\26\0\162\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\166\0\15\0\166\0" +
		"\26\0\166\0\uffff\uffff\ufffe\uffff\40\0\234\0\6\0\234\0\10\0\234\0\15\0\234\0\16" +
		"\0\234\0\23\0\234\0\24\0\234\0\25\0\234\0\26\0\234\0\42\0\234\0\43\0\234\0\44\0\234" +
		"\0\45\0\234\0\51\0\234\0\54\0\234\0\56\0\234\0\57\0\234\0\60\0\234\0\61\0\234\0\62" +
		"\0\234\0\63\0\234\0\64\0\234\0\65\0\234\0\66\0\234\0\67\0\234\0\70\0\234\0\71\0\234" +
		"\0\72\0\234\0\73\0\234\0\74\0\234\0\75\0\234\0\76\0\234\0\77\0\234\0\100\0\234\0" +
		"\101\0\234\0\102\0\234\0\103\0\234\0\104\0\234\0\105\0\234\0\106\0\234\0\107\0\234" +
		"\0\110\0\234\0\111\0\234\0\112\0\234\0\113\0\234\0\114\0\234\0\115\0\234\0\uffff" +
		"\uffff\ufffe\uffff\17\0\uffff\uffff\20\0\117\0\25\0\117\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0" +
		"\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff" +
		"\ufffe\uffff\115\0\uffff\uffff\0\0\360\0\6\0\360\0\7\0\360\0\27\0\360\0\30\0\360" +
		"\0\44\0\360\0\45\0\360\0\56\0\360\0\57\0\360\0\60\0\360\0\61\0\360\0\62\0\360\0\63" +
		"\0\360\0\64\0\360\0\65\0\360\0\66\0\360\0\67\0\360\0\70\0\360\0\71\0\360\0\72\0\360" +
		"\0\73\0\360\0\74\0\360\0\75\0\360\0\76\0\360\0\77\0\360\0\100\0\360\0\101\0\360\0" +
		"\102\0\360\0\103\0\360\0\104\0\360\0\105\0\360\0\106\0\360\0\107\0\360\0\110\0\360" +
		"\0\111\0\360\0\112\0\360\0\113\0\360\0\114\0\360\0\uffff\uffff\ufffe\uffff\11\0\317" +
		"\0\41\0\uffff\uffff\22\0\317\0\uffff\uffff\ufffe\uffff\11\0\316\0\41\0\316\0\22\0" +
		"\316\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\156\0\15\0\156\0\26\0\156\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\366\0\15\0\366\0\25\0\366\0\uffff\uffff\ufffe\uffff\10\0\255\0\40\0\uffff" +
		"\uffff\26\0\255\0\uffff\uffff\ufffe\uffff\10\0\256\0\40\0\256\0\26\0\256\0\uffff" +
		"\uffff\ufffe\uffff");

	private static final int[] tmGoto = TMLexer.unpack_int(181,
		"\0\0\4\0\42\0\102\0\102\0\102\0\102\0\204\0\210\0\222\0\230\0\250\0\252\0\254\0\356" +
		"\0\u0128\0\u013c\0\u016a\0\u019c\0\u01a0\0\u01fa\0\u0232\0\u0252\0\u0266\0\u0268" +
		"\0\u027a\0\u0282\0\u028a\0\u0294\0\u0296\0\u0298\0\u02a2\0\u02b0\0\u02c0\0\u02c6" +
		"\0\u0308\0\u0346\0\u038c\0\u0468\0\u0482\0\u049c\0\u04a8\0\u04aa\0\u04ac\0\u04ae" +
		"\0\u04f6\0\u04fc\0\u05da\0\u06b8\0\u0796\0\u087a\0\u0958\0\u0a36\0\u0b1a\0\u0bf8" +
		"\0\u0cd6\0\u0db4\0\u0e92\0\u0f70\0\u104e\0\u112c\0\u120a\0\u12e8\0\u13c6\0\u14a4" +
		"\0\u1584\0\u1664\0\u1742\0\u1820\0\u1908\0\u19ee\0\u1ad2\0\u1bb0\0\u1c8e\0\u1d6c" +
		"\0\u1e4c\0\u1f2a\0\u2008\0\u2050\0\u2052\0\u2058\0\u205a\0\u205c\0\u205e\0\u2060" +
		"\0\u2062\0\u2064\0\u2066\0\u206a\0\u206c\0\u206e\0\u20ec\0\u2148\0\u2170\0\u217c" +
		"\0\u2182\0\u2186\0\u218e\0\u2196\0\u219e\0\u21a6\0\u21a8\0\u21b0\0\u21b4\0\u21b6" +
		"\0\u21be\0\u21c2\0\u21ca\0\u21ce\0\u21d4\0\u21d6\0\u21da\0\u21de\0\u21e6\0\u21ec" +
		"\0\u21ee\0\u21f0\0\u21f4\0\u21f8\0\u21fa\0\u21fc\0\u2200\0\u2204\0\u2206\0\u222a" +
		"\0\u224e\0\u2274\0\u229a\0\u22c2\0\u22f8\0\u2318\0\u2344\0\u237c\0\u237e\0\u23b6" +
		"\0\u23ba\0\u23f2\0\u242a\0\u2466\0\u24a6\0\u24e6\0\u251e\0\u255e\0\u25a0\0\u25e8" +
		"\0\u25f2\0\u25fa\0\u2636\0\u2672\0\u26b0\0\u26b2\0\u26b6\0\u26ba\0\u26ce\0\u26d0" +
		"\0\u26d2\0\u26d8\0\u26dc\0\u26e0\0\u26e8\0\u26ee\0\u26f4\0\u2700\0\u2702\0\u2704" +
		"\0\u2706\0\u2708\0\u270c\0\u2726\0\u272c\0\u2732\0\u276e\0\u27b4\0\u27b8\0\u27bc" +
		"\0\u27c0\0\u27c4\0\u27c8\0\u27ce\0\u27d4\0\u280a\0");

	private static final int[] tmFromTo = TMLexer.unpack_int(10250,
		"\u01c4\0\u01c6\0\u01c5\0\u01c7\0\1\0\4\0\6\0\4\0\36\0\60\0\41\0\4\0\61\0\104\0\72" +
		"\0\4\0\106\0\4\0\116\0\4\0\301\0\4\0\u0107\0\4\0\u0129\0\4\0\u014f\0\4\0\u0155\0" +
		"\4\0\u0156\0\4\0\u0176\0\4\0\1\0\5\0\6\0\5\0\41\0\5\0\55\0\102\0\72\0\5\0\106\0\5" +
		"\0\116\0\5\0\262\0\371\0\301\0\5\0\373\0\371\0\u0107\0\5\0\u0129\0\5\0\u014f\0\5" +
		"\0\u0155\0\5\0\u0156\0\5\0\u0176\0\5\0\105\0\124\0\130\0\124\0\141\0\164\0\162\0" +
		"\124\0\167\0\164\0\212\0\124\0\235\0\302\0\305\0\302\0\320\0\302\0\321\0\302\0\323" +
		"\0\302\0\355\0\302\0\357\0\302\0\360\0\302\0\363\0\302\0\u0112\0\302\0\u0117\0\302" +
		"\0\u011c\0\302\0\u011e\0\302\0\u011f\0\302\0\u0121\0\302\0\u0139\0\302\0\u013c\0" +
		"\302\0\u013e\0\302\0\u0140\0\302\0\u0142\0\302\0\u0143\0\302\0\u016b\0\302\0\u0180" +
		"\0\302\0\u0184\0\302\0\u0187\0\302\0\u0189\0\302\0\u01b1\0\302\0\37\0\62\0\64\0\107" +
		"\0\315\0\u011c\0\u0165\0\u019f\0\u019c\0\u019f\0\u01b8\0\u019f\0\u01b9\0\u019f\0" +
		"\u0110\0\u0157\0\u0197\0\u0157\0\u0198\0\u0157\0\63\0\106\0\126\0\154\0\233\0\301" +
		"\0\270\0\377\0\300\0\u0107\0\313\0\u0119\0\u0106\0\u014f\0\u0130\0\u0176\0\u010e" +
		"\0\u0155\0\u010e\0\u0156\0\34\0\56\0\60\0\103\0\104\0\123\0\121\0\144\0\201\0\256" +
		"\0\203\0\260\0\255\0\367\0\266\0\375\0\274\0\u0102\0\276\0\u0104\0\300\0\u0108\0" +
		"\316\0\u011d\0\u0100\0\u014c\0\u0101\0\u014d\0\u0106\0\u0150\0\u0138\0\u017b\0\u013a" +
		"\0\u017d\0\u013b\0\u017e\0\u013f\0\u0182\0\u014b\0\u0192\0\u0151\0\u0194\0\u017c" +
		"\0\u01a8\0\u017f\0\u01a9\0\u0181\0\u01ab\0\u0183\0\u01ac\0\u0185\0\u01ae\0\u0186" +
		"\0\u01af\0\u0193\0\u01b5\0\u01aa\0\u01bc\0\u01ad\0\u01bd\0\u01b0\0\u01be\0\u01b2" +
		"\0\u01c0\0\u01bf\0\u01c3\0\21\0\35\0\235\0\303\0\305\0\303\0\320\0\303\0\321\0\303" +
		"\0\323\0\303\0\355\0\303\0\357\0\303\0\360\0\303\0\363\0\303\0\u0112\0\303\0\u0117" +
		"\0\303\0\u011c\0\303\0\u011e\0\303\0\u011f\0\303\0\u0121\0\303\0\u0127\0\303\0\u0139" +
		"\0\303\0\u013c\0\303\0\u013e\0\303\0\u0140\0\303\0\u0142\0\303\0\u0143\0\303\0\u016b" +
		"\0\303\0\u0180\0\303\0\u0184\0\303\0\u0187\0\303\0\u0189\0\303\0\u01b1\0\303\0\24" +
		"\0\41\0\50\0\73\0\76\0\117\0\152\0\205\0\201\0\257\0\203\0\257\0\266\0\376\0\274" +
		"\0\u0103\0\345\0\u0131\0\u017a\0\u01a7\0\47\0\72\0\75\0\116\0\157\0\211\0\166\0\235" +
		"\0\213\0\264\0\246\0\355\0\247\0\357\0\250\0\360\0\254\0\363\0\313\0\u011a\0\337" +
		"\0\u012c\0\356\0\u0139\0\361\0\u013c\0\362\0\u013e\0\364\0\u0140\0\365\0\u0142\0" +
		"\366\0\u0143\0\u013d\0\u0180\0\u0141\0\u0184\0\u0144\0\u0187\0\u0145\0\u0189\0\u0166" +
		"\0\u011a\0\u0188\0\u01b1\0\1\0\6\0\6\0\6\0\41\0\6\0\106\0\6\0\116\0\6\0\235\0\304" +
		"\0\301\0\6\0\305\0\304\0\355\0\304\0\357\0\304\0\360\0\304\0\363\0\304\0\u0117\0" +
		"\304\0\u011c\0\304\0\u0139\0\304\0\u013c\0\304\0\u013e\0\304\0\u0140\0\304\0\u0142" +
		"\0\304\0\u0143\0\304\0\u0180\0\304\0\u0184\0\304\0\u0187\0\304\0\u0189\0\304\0\u01b1" +
		"\0\304\0\25\0\42\0\u0110\0\u0158\0\20\0\31\0\30\0\53\0\32\0\55\0\235\0\305\0\305" +
		"\0\305\0\307\0\u0117\0\311\0\u0118\0\320\0\305\0\321\0\305\0\323\0\305\0\337\0\305" +
		"\0\355\0\305\0\357\0\305\0\360\0\305\0\363\0\305\0\372\0\u0146\0\u0112\0\305\0\u0117" +
		"\0\305\0\u0118\0\u0160\0\u0119\0\305\0\u011a\0\305\0\u011b\0\305\0\u011c\0\305\0" +
		"\u011e\0\305\0\u011f\0\305\0\u0121\0\305\0\u0122\0\305\0\u0127\0\305\0\u0139\0\305" +
		"\0\u013c\0\305\0\u013e\0\305\0\u0140\0\305\0\u0142\0\305\0\u0143\0\305\0\u0149\0" +
		"\u0146\0\u0160\0\u0160\0\u0161\0\u0160\0\u016b\0\305\0\u0180\0\305\0\u0184\0\305" +
		"\0\u0187\0\305\0\u0189\0\305\0\u019f\0\u0160\0\u01a1\0\u0160\0\u01b1\0\305\0\235" +
		"\0\306\0\305\0\306\0\320\0\306\0\321\0\306\0\323\0\306\0\355\0\306\0\357\0\306\0" +
		"\360\0\306\0\363\0\306\0\u0112\0\306\0\u0117\0\306\0\u011c\0\306\0\u011e\0\306\0" +
		"\u011f\0\306\0\u0121\0\306\0\u0127\0\306\0\u0139\0\306\0\u013c\0\306\0\u013e\0\306" +
		"\0\u0140\0\306\0\u0142\0\306\0\u0143\0\306\0\u016b\0\306\0\u0180\0\306\0\u0184\0" +
		"\306\0\u0187\0\306\0\u0189\0\306\0\u01b1\0\306\0\166\0\236\0\246\0\236\0\250\0\236" +
		"\0\254\0\236\0\342\0\236\0\361\0\236\0\364\0\236\0\366\0\236\0\u0120\0\236\0\u0123" +
		"\0\236\0\u0126\0\236\0\u0144\0\236\0\u016c\0\236\0\u016d\0\236\0\u016f\0\236\0\u01a2" +
		"\0\236\0\54\0\101\0\77\0\120\0\102\0\122\0\u0111\0\u015a\0\u0115\0\u015d\0\u015f" +
		"\0\u019b\0\u0165\0\u01a0\0\u018e\0\u01b3\0\u0199\0\u01b6\0\u019c\0\u01b7\0\212\0" +
		"\263\0\10\0\26\0\105\0\125\0\130\0\125\0\162\0\125\0\166\0\237\0\212\0\125\0\254" +
		"\0\237\0\310\0\26\0\u0162\0\26\0\51\0\74\0\150\0\204\0\152\0\206\0\345\0\u0132\0" +
		"\125\0\150\0\335\0\u012a\0\u0168\0\u012a\0\u01b6\0\u01c1\0\26\0\43\0\73\0\43\0\335" +
		"\0\u012b\0\u0168\0\u012b\0\u01b6\0\u01c2\0\313\0\u011b\0\332\0\u0128\0\304\0\u010d" +
		"\0\306\0\u0113\0\u0157\0\u010d\0\u0159\0\u010d\0\u015e\0\u0113\0\26\0\44\0\73\0\44" +
		"\0\u0118\0\u0161\0\u0160\0\u0161\0\u0161\0\u0161\0\u019f\0\u0161\0\u01a1\0\u0161" +
		"\0\324\0\u0127\0\u0115\0\u015e\0\u0124\0\u0127\0\u0165\0\u01a1\0\u0172\0\u0127\0" +
		"\u019c\0\u01a1\0\u01b8\0\u01a1\0\u01b9\0\u01a1\0\u0110\0\u0159\0\u0197\0\u0159\0" +
		"\u0198\0\u0159\0\235\0\307\0\305\0\307\0\320\0\307\0\321\0\307\0\323\0\307\0\337" +
		"\0\307\0\355\0\307\0\357\0\307\0\360\0\307\0\363\0\307\0\u0112\0\307\0\u0117\0\307" +
		"\0\u0119\0\307\0\u011a\0\307\0\u011b\0\307\0\u011c\0\307\0\u011e\0\307\0\u011f\0" +
		"\307\0\u0121\0\307\0\u0122\0\307\0\u0127\0\307\0\u0139\0\307\0\u013c\0\307\0\u013e" +
		"\0\307\0\u0140\0\307\0\u0142\0\307\0\u0143\0\307\0\u016b\0\307\0\u0180\0\307\0\u0184" +
		"\0\307\0\u0187\0\307\0\u0189\0\307\0\u01b1\0\307\0\141\0\165\0\167\0\165\0\174\0" +
		"\165\0\235\0\165\0\305\0\165\0\320\0\165\0\321\0\165\0\323\0\165\0\355\0\165\0\357" +
		"\0\165\0\360\0\165\0\363\0\165\0\u0112\0\165\0\u0117\0\165\0\u011c\0\165\0\u011e" +
		"\0\165\0\u011f\0\165\0\u0121\0\165\0\u0127\0\165\0\u0139\0\165\0\u013c\0\165\0\u013e" +
		"\0\165\0\u0140\0\165\0\u0142\0\165\0\u0143\0\165\0\u016b\0\165\0\u0180\0\165\0\u0184" +
		"\0\165\0\u0187\0\165\0\u0189\0\165\0\u01b1\0\165\0\1\0\7\0\6\0\7\0\37\0\7\0\41\0" +
		"\7\0\106\0\7\0\116\0\7\0\130\0\7\0\165\0\7\0\167\0\7\0\212\0\7\0\235\0\7\0\301\0" +
		"\7\0\305\0\7\0\323\0\7\0\355\0\7\0\357\0\7\0\360\0\7\0\363\0\7\0\u0112\0\7\0\u0117" +
		"\0\7\0\u011c\0\7\0\u011f\0\7\0\u0121\0\7\0\u0139\0\7\0\u013c\0\7\0\u013e\0\7\0\u0140" +
		"\0\7\0\u0142\0\7\0\u0143\0\7\0\u016b\0\7\0\u0180\0\7\0\u0184\0\7\0\u0187\0\7\0\u0189" +
		"\0\7\0\u01b1\0\7\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0" +
		"\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0" +
		"\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0" +
		"\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233\0\167\0" +
		"\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270\0\227" +
		"\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111\0\242" +
		"\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304" +
		"\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310\0\344" +
		"\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\376\0\111\0\u0103" +
		"\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113" +
		"\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c" +
		"\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129" +
		"\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e" +
		"\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159" +
		"\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0" +
		"\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310\0\u0189\0\310" +
		"\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0" +
		"\11\0\6\0\11\0\41\0\11\0\72\0\11\0\106\0\11\0\116\0\11\0\301\0\11\0\u0107\0\11\0" +
		"\u0129\0\11\0\u014f\0\11\0\u0155\0\11\0\u0156\0\11\0\u0176\0\11\0\1\0\12\0\6\0\12" +
		"\0\41\0\12\0\72\0\12\0\106\0\12\0\116\0\12\0\301\0\12\0\u0107\0\12\0\u0129\0\12\0" +
		"\u014f\0\12\0\u0155\0\12\0\u0156\0\12\0\u0176\0\12\0\1\0\13\0\6\0\13\0\41\0\13\0" +
		"\106\0\13\0\116\0\13\0\301\0\13\0\u0112\0\u015b\0\334\0\u0129\0\22\0\36\0\235\0\311" +
		"\0\271\0\311\0\272\0\311\0\305\0\311\0\320\0\311\0\321\0\311\0\323\0\311\0\337\0" +
		"\311\0\355\0\311\0\357\0\311\0\360\0\311\0\363\0\311\0\377\0\311\0\u0112\0\311\0" +
		"\u0117\0\311\0\u0119\0\311\0\u011a\0\311\0\u011b\0\311\0\u011c\0\311\0\u011e\0\311" +
		"\0\u011f\0\311\0\u0121\0\311\0\u0122\0\311\0\u0127\0\311\0\u0139\0\311\0\u013c\0" +
		"\311\0\u013e\0\311\0\u0140\0\311\0\u0142\0\311\0\u0143\0\311\0\u016b\0\311\0\u0180" +
		"\0\311\0\u0184\0\311\0\u0187\0\311\0\u0189\0\311\0\u01b1\0\311\0\353\0\u0134\0\354" +
		"\0\u0134\0\u0133\0\u0134\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17" +
		"\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111" +
		"\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\124\0\145\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111" +
		"\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344" +
		"\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0" +
		"\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310" +
		"\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310" +
		"\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45" +
		"\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a" +
		"\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122" +
		"\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139" +
		"\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f" +
		"\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161" +
		"\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187" +
		"\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111" +
		"\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57" +
		"\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45" +
		"\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\124\0\146\0\125\0\151\0\130\0\126" +
		"\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233" +
		"\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270" +
		"\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111" +
		"\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\303\0\u010c" +
		"\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310" +
		"\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\376\0\111" +
		"\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310" +
		"\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0" +
		"\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127" +
		"\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c" +
		"\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f\0\111\0\u0157" +
		"\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0" +
		"\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310" +
		"\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1" +
		"\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0" +
		"\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105" +
		"\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\124\0\147\0\125\0\151\0\130\0\126\0\134" +
		"\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233\0\167" +
		"\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270\0\227" +
		"\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111\0\242" +
		"\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304" +
		"\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310\0\344" +
		"\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\376\0\111\0\u0103" +
		"\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113" +
		"\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c" +
		"\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129" +
		"\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e" +
		"\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159" +
		"\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0" +
		"\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310\0\u0189\0\310" +
		"\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0" +
		"\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63" +
		"\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0" +
		"\10\0\116\0\10\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111" +
		"\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233\0\166\0\240\0\167\0\45\0\175\0\45" +
		"\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111" +
		"\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45" +
		"\0\250\0\240\0\254\0\240\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\303\0\u010c" +
		"\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310" +
		"\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\366\0\240" +
		"\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45" +
		"\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a" +
		"\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122" +
		"\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139" +
		"\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f" +
		"\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161" +
		"\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187" +
		"\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111" +
		"\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57" +
		"\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45" +
		"\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0\45" +
		"\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233\0\167\0\45" +
		"\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270\0\227\0\45" +
		"\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111\0\242\0\45" +
		"\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\302\0\u010a\0\303\0\u010c" +
		"\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310" +
		"\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\376\0\111" +
		"\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310" +
		"\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0" +
		"\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127" +
		"\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c" +
		"\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f\0\111\0\u0157" +
		"\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0" +
		"\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310" +
		"\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1" +
		"\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0" +
		"\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105" +
		"\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141" +
		"\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233\0\167\0\45\0\175" +
		"\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270\0\227\0\45\0\230" +
		"\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243" +
		"\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\302\0\u010b\0\303\0\u010c\0" +
		"\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310" +
		"\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\376\0\111" +
		"\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310" +
		"\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0" +
		"\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127" +
		"\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c" +
		"\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f\0\111\0\u0157" +
		"\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0" +
		"\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310" +
		"\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1" +
		"\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0" +
		"\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105" +
		"\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141" +
		"\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233\0\166\0\241\0\167" +
		"\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270\0\227" +
		"\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111\0\242" +
		"\0\45\0\243\0\45\0\250\0\241\0\254\0\241\0\257\0\45\0\276\0\111\0\277\0\45\0\301" +
		"\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0" +
		"\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310" +
		"\0\363\0\310\0\366\0\241\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b" +
		"\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\214\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\215\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\216\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\217\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\220\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\221\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111" +
		"\0\220\0\270\0\221\0\271\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111" +
		"\0\220\0\270\0\221\0\272\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\222\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\223\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\224\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\225\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\231\0\225\0\232\0\45\0\235\0\310" +
		"\0\236\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111" +
		"\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310" +
		"\0\321\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310" +
		"\0\360\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b" +
		"\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\164\0\226\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\231\0\226\0\232\0\45\0\235\0\310" +
		"\0\236\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111" +
		"\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310" +
		"\0\321\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310" +
		"\0\360\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b" +
		"\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111" +
		"\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344" +
		"\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\265\0\374\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111" +
		"\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344" +
		"\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0" +
		"\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310" +
		"\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310" +
		"\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45" +
		"\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a" +
		"\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122" +
		"\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139" +
		"\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u0146" +
		"\0\u018a\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0" +
		"\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0" +
		"\165\0\233\0\166\0\242\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\240\0\350\0\241\0\111\0\242\0\45\0\243\0\45\0\250\0\242\0\254\0\242" +
		"\0\257\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310" +
		"\0\306\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45" +
		"\0\355\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\366\0\242\0\376\0\111\0\u0103" +
		"\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113" +
		"\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c" +
		"\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129" +
		"\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e" +
		"\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u0146\0\u018b\0\u014f\0\111\0" +
		"\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162" +
		"\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310" +
		"\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1" +
		"\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0" +
		"\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105" +
		"\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141" +
		"\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162\0\126\0\164\0\227\0\165\0\233\0\166" +
		"\0\243\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214\0\111\0\220" +
		"\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237\0\344\0\241" +
		"\0\111\0\242\0\45\0\243\0\45\0\250\0\243\0\254\0\243\0\257\0\45\0\276\0\111\0\277" +
		"\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321" +
		"\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\366\0\243\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111" +
		"\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162" +
		"\0\u0119\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310" +
		"\0\u0121\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344" +
		"\0\u0134\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0" +
		"\310\0\u0143\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e" +
		"\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0" +
		"\u0180\0\310\0\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162" +
		"\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17" +
		"\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45" +
		"\0\53\0\75\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125" +
		"\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162" +
		"\0\126\0\165\0\233\0\166\0\244\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212" +
		"\0\126\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236" +
		"\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\250\0\244\0\254\0\244\0\257" +
		"\0\45\0\276\0\111\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306" +
		"\0\111\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355" +
		"\0\310\0\357\0\310\0\360\0\310\0\363\0\310\0\366\0\244\0\376\0\111\0\u0103\0\45\0" +
		"\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111" +
		"\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0" +
		"\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129" +
		"\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e" +
		"\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159" +
		"\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0" +
		"\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310\0\u0189\0\310" +
		"\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0" +
		"\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63" +
		"\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0" +
		"\10\0\116\0\10\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111" +
		"\0\146\0\45\0\147\0\45\0\162\0\126\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111" +
		"\0\205\0\151\0\212\0\126\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45" +
		"\0\235\0\310\0\236\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45" +
		"\0\276\0\111\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111" +
		"\0\320\0\310\0\321\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310" +
		"\0\357\0\310\0\360\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a" +
		"\0\111\0\u010b\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118" +
		"\0\u0162\0\u0119\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f" +
		"\0\310\0\u0121\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131" +
		"\0\344\0\u0134\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142" +
		"\0\310\0\u0143\0\310\0\u0146\0\u018c\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b" +
		"\0\111\0\u015e\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0" +
		"\u0176\0\111\0\u0180\0\310\0\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111" +
		"\0\u019f\0\u0162\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0" +
		"\6\0\10\0\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43" +
		"\0\45\0\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10" +
		"\0\117\0\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45" +
		"\0\147\0\45\0\162\0\126\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151" +
		"\0\212\0\126\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310" +
		"\0\236\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111" +
		"\0\277\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310" +
		"\0\321\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310" +
		"\0\360\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b" +
		"\0\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u0146\0\u018d\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e" +
		"\0\111\0\u0160\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0" +
		"\u0180\0\310\0\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162" +
		"\0\u01a1\0\u0162\0\u01a7\0\111\0\u01b1\0\310\0\0\0\2\0\1\0\10\0\2\0\17\0\6\0\10\0" +
		"\13\0\17\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0" +
		"\44\0\45\0\53\0\75\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0" +
		"\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0" +
		"\45\0\162\0\126\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0" +
		"\126\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236" +
		"\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277" +
		"\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321" +
		"\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\20\0\32" +
		"\0\26\0\45\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45" +
		"\0\53\0\75\0\72\0\111\0\73\0\45\0\101\0\32\0\105\0\126\0\106\0\10\0\116\0\10\0\117" +
		"\0\75\0\125\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147" +
		"\0\45\0\162\0\126\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212" +
		"\0\126\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236" +
		"\0\45\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277" +
		"\0\45\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321" +
		"\0\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\62\0\105\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\116\0\10\0\117\0\75\0\125" +
		"\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0\162" +
		"\0\126\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126\0\214" +
		"\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45\0\237" +
		"\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45\0\301" +
		"\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0\310\0" +
		"\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360\0\310" +
		"\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0\111\0" +
		"\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119\0\310" +
		"\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121\0\310" +
		"\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134\0\111" +
		"\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143\0" +
		"\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\1\0\10\0\2\0\17\0\6\0\10\0\13\0\17\0\26\0\45" +
		"\0\31\0\17\0\35\0\57\0\36\0\61\0\37\0\63\0\41\0\10\0\43\0\45\0\44\0\45\0\53\0\75" +
		"\0\72\0\111\0\73\0\45\0\105\0\126\0\106\0\10\0\107\0\141\0\116\0\10\0\117\0\75\0" +
		"\125\0\151\0\130\0\126\0\134\0\45\0\141\0\45\0\145\0\111\0\146\0\45\0\147\0\45\0" +
		"\162\0\126\0\165\0\233\0\167\0\45\0\175\0\45\0\177\0\111\0\205\0\151\0\212\0\126" +
		"\0\214\0\111\0\220\0\270\0\227\0\45\0\230\0\111\0\232\0\45\0\235\0\310\0\236\0\45" +
		"\0\237\0\344\0\241\0\111\0\242\0\45\0\243\0\45\0\257\0\45\0\276\0\111\0\277\0\45" +
		"\0\301\0\10\0\303\0\u010c\0\304\0\45\0\305\0\310\0\306\0\111\0\320\0\310\0\321\0" +
		"\310\0\323\0\310\0\337\0\310\0\344\0\45\0\350\0\45\0\355\0\310\0\357\0\310\0\360" +
		"\0\310\0\363\0\310\0\376\0\111\0\u0103\0\45\0\u0107\0\111\0\u010a\0\111\0\u010b\0" +
		"\111\0\u010d\0\45\0\u0112\0\310\0\u0113\0\111\0\u0117\0\310\0\u0118\0\u0162\0\u0119" +
		"\0\310\0\u011a\0\10\0\u011b\0\310\0\u011c\0\310\0\u011e\0\310\0\u011f\0\310\0\u0121" +
		"\0\310\0\u0122\0\310\0\u0127\0\310\0\u0129\0\10\0\u012f\0\45\0\u0131\0\344\0\u0134" +
		"\0\111\0\u0139\0\310\0\u013c\0\310\0\u013e\0\310\0\u0140\0\310\0\u0142\0\310\0\u0143" +
		"\0\310\0\u014f\0\111\0\u0157\0\45\0\u0159\0\45\0\u015b\0\111\0\u015e\0\111\0\u0160" +
		"\0\u0162\0\u0161\0\u0162\0\u0162\0\10\0\u016b\0\310\0\u0176\0\111\0\u0180\0\310\0" +
		"\u0184\0\310\0\u0187\0\310\0\u0189\0\310\0\u0199\0\111\0\u019f\0\u0162\0\u01a1\0" +
		"\u0162\0\u01a7\0\111\0\u01b1\0\310\0\127\0\155\0\163\0\155\0\166\0\155\0\235\0\312" +
		"\0\250\0\155\0\254\0\155\0\305\0\312\0\320\0\312\0\321\0\312\0\323\0\312\0\355\0" +
		"\312\0\357\0\312\0\360\0\312\0\363\0\312\0\366\0\155\0\u0112\0\312\0\u0117\0\312" +
		"\0\u011c\0\312\0\u011e\0\312\0\u011f\0\312\0\u0121\0\312\0\u0127\0\312\0\u0139\0" +
		"\312\0\u013c\0\312\0\u013e\0\312\0\u0140\0\312\0\u0142\0\312\0\u0143\0\312\0\u0148" +
		"\0\312\0\u016b\0\312\0\u0180\0\312\0\u0184\0\312\0\u0187\0\312\0\u0189\0\312\0\u0191" +
		"\0\312\0\u01b1\0\312\0\134\0\162\0\154\0\207\0\211\0\207\0\264\0\207\0\343\0\u012f" +
		"\0\3\0\22\0\0\0\u01c4\0\22\0\37\0\0\0\3\0\37\0\64\0\64\0\110\0\20\0\33\0\101\0\33" +
		"\0\22\0\40\0\37\0\65\0\26\0\46\0\43\0\46\0\44\0\46\0\73\0\46\0\105\0\127\0\130\0" +
		"\127\0\134\0\163\0\141\0\166\0\146\0\200\0\147\0\200\0\162\0\127\0\167\0\166\0\175" +
		"\0\254\0\212\0\127\0\227\0\273\0\232\0\300\0\235\0\313\0\236\0\343\0\237\0\46\0\242" +
		"\0\352\0\243\0\352\0\257\0\200\0\277\0\u0106\0\304\0\46\0\305\0\313\0\320\0\313\0" +
		"\321\0\313\0\323\0\313\0\337\0\313\0\344\0\u0130\0\350\0\352\0\355\0\313\0\357\0" +
		"\313\0\360\0\313\0\363\0\313\0\u0103\0\u014e\0\u010d\0\46\0\u0112\0\313\0\u0117\0" +
		"\313\0\u0119\0\u0166\0\u011b\0\u0166\0\u011c\0\313\0\u011e\0\313\0\u011f\0\313\0" +
		"\u0121\0\313\0\u0122\0\313\0\u0127\0\313\0\u012f\0\u0175\0\u0131\0\46\0\u0139\0\313" +
		"\0\u013c\0\313\0\u013e\0\313\0\u0140\0\313\0\u0142\0\313\0\u0143\0\313\0\u0157\0" +
		"\46\0\u0159\0\46\0\u016b\0\313\0\u0180\0\313\0\u0184\0\313\0\u0187\0\313\0\u0189" +
		"\0\313\0\u01b1\0\313\0\1\0\14\0\6\0\14\0\41\0\14\0\106\0\14\0\116\0\14\0\235\0\314" +
		"\0\301\0\14\0\305\0\314\0\320\0\314\0\321\0\314\0\323\0\314\0\337\0\314\0\355\0\314" +
		"\0\357\0\314\0\360\0\314\0\363\0\314\0\u0112\0\314\0\u0117\0\314\0\u0118\0\u0163" +
		"\0\u0119\0\314\0\u011a\0\314\0\u011b\0\314\0\u011c\0\314\0\u011e\0\314\0\u011f\0" +
		"\314\0\u0121\0\314\0\u0122\0\314\0\u0127\0\314\0\u0129\0\u0173\0\u0139\0\314\0\u013c" +
		"\0\314\0\u013e\0\314\0\u0140\0\314\0\u0142\0\314\0\u0143\0\314\0\u0160\0\u0163\0" +
		"\u0161\0\u0163\0\u0162\0\u019e\0\u016b\0\314\0\u0180\0\314\0\u0184\0\314\0\u0187" +
		"\0\314\0\u0189\0\314\0\u019f\0\u0163\0\u01a1\0\u0163\0\u01b1\0\314\0\72\0\112\0\145" +
		"\0\177\0\177\0\255\0\214\0\265\0\230\0\275\0\241\0\351\0\276\0\u0105\0\306\0\u0114" +
		"\0\376\0\265\0\u0107\0\112\0\u010a\0\u0152\0\u010b\0\u0153\0\u0113\0\u015c\0\u0134" +
		"\0\u0179\0\u014f\0\112\0\u015b\0\275\0\u015e\0\u0114\0\u0176\0\112\0\u0199\0\u0105" +
		"\0\u01a7\0\u01bb\0\127\0\156\0\163\0\156\0\166\0\245\0\250\0\245\0\254\0\245\0\366" +
		"\0\245\0\154\0\210\0\211\0\262\0\264\0\373\0\105\0\130\0\162\0\212\0\105\0\131\0" +
		"\130\0\160\0\162\0\131\0\212\0\160\0\105\0\132\0\130\0\132\0\162\0\132\0\212\0\132" +
		"\0\105\0\133\0\130\0\133\0\162\0\133\0\212\0\133\0\105\0\134\0\130\0\134\0\162\0" +
		"\134\0\212\0\134\0\125\0\152\0\105\0\135\0\130\0\135\0\162\0\135\0\212\0\135\0\372" +
		"\0\u0147\0\u0149\0\u0147\0\u0146\0\u018e\0\105\0\136\0\130\0\136\0\162\0\136\0\212" +
		"\0\136\0\146\0\201\0\147\0\203\0\105\0\137\0\130\0\137\0\162\0\137\0\212\0\137\0" +
		"\125\0\153\0\205\0\261\0\146\0\202\0\147\0\202\0\257\0\370\0\141\0\167\0\141\0\170" +
		"\0\167\0\251\0\141\0\171\0\167\0\171\0\166\0\246\0\250\0\361\0\254\0\364\0\366\0" +
		"\u0144\0\353\0\u0135\0\354\0\u0135\0\u0133\0\u0135\0\164\0\230\0\164\0\231\0\141" +
		"\0\172\0\167\0\172\0\141\0\173\0\167\0\173\0\227\0\274\0\214\0\266\0\214\0\267\0" +
		"\376\0\u014a\0\230\0\276\0\u015b\0\u0199\0\u0134\0\u017a\0\235\0\315\0\305\0\315" +
		"\0\355\0\315\0\357\0\315\0\360\0\315\0\363\0\315\0\u0117\0\315\0\u0139\0\315\0\u013c" +
		"\0\315\0\u013e\0\315\0\u0140\0\315\0\u0142\0\315\0\u0143\0\315\0\u0180\0\315\0\u0184" +
		"\0\315\0\u0187\0\315\0\u0189\0\315\0\u01b1\0\315\0\235\0\316\0\305\0\u0111\0\355" +
		"\0\u0138\0\357\0\u013a\0\360\0\u013b\0\363\0\u013f\0\u0117\0\u015f\0\u0139\0\u017c" +
		"\0\u013c\0\u017f\0\u013e\0\u0181\0\u0140\0\u0183\0\u0142\0\u0185\0\u0143\0\u0186" +
		"\0\u0180\0\u01aa\0\u0184\0\u01ad\0\u0187\0\u01b0\0\u0189\0\u01b2\0\u01b1\0\u01bf" +
		"\0\235\0\317\0\305\0\317\0\355\0\317\0\357\0\317\0\360\0\317\0\363\0\317\0\u0117" +
		"\0\317\0\u011c\0\u016a\0\u0139\0\317\0\u013c\0\317\0\u013e\0\317\0\u0140\0\317\0" +
		"\u0142\0\317\0\u0143\0\317\0\u0180\0\317\0\u0184\0\317\0\u0187\0\317\0\u0189\0\317" +
		"\0\u01b1\0\317\0\235\0\320\0\305\0\320\0\355\0\320\0\357\0\320\0\360\0\320\0\363" +
		"\0\320\0\u0117\0\320\0\u011c\0\320\0\u0139\0\320\0\u013c\0\320\0\u013e\0\320\0\u0140" +
		"\0\320\0\u0142\0\320\0\u0143\0\320\0\u0180\0\320\0\u0184\0\320\0\u0187\0\320\0\u0189" +
		"\0\320\0\u01b1\0\320\0\235\0\321\0\305\0\321\0\320\0\u011e\0\355\0\321\0\357\0\321" +
		"\0\360\0\321\0\363\0\321\0\u0117\0\321\0\u011c\0\321\0\u0139\0\321\0\u013c\0\321" +
		"\0\u013e\0\321\0\u0140\0\321\0\u0142\0\321\0\u0143\0\321\0\u0180\0\321\0\u0184\0" +
		"\321\0\u0187\0\321\0\u0189\0\321\0\u01b1\0\321\0\235\0\322\0\305\0\322\0\320\0\322" +
		"\0\321\0\322\0\323\0\322\0\355\0\322\0\357\0\322\0\360\0\322\0\363\0\322\0\u0112" +
		"\0\322\0\u0117\0\322\0\u011c\0\322\0\u011e\0\322\0\u011f\0\322\0\u0121\0\322\0\u0139" +
		"\0\322\0\u013c\0\322\0\u013e\0\322\0\u0140\0\322\0\u0142\0\322\0\u0143\0\322\0\u016b" +
		"\0\322\0\u0180\0\322\0\u0184\0\322\0\u0187\0\322\0\u0189\0\322\0\u01b1\0\322\0\166" +
		"\0\247\0\246\0\356\0\250\0\362\0\254\0\365\0\342\0\u012e\0\361\0\u013d\0\364\0\u0141" +
		"\0\366\0\u0145\0\u0120\0\u016e\0\u0123\0\u0170\0\u0126\0\u0171\0\u0144\0\u0188\0" +
		"\u016c\0\u01a3\0\u016d\0\u01a4\0\u016f\0\u01a5\0\u01a2\0\u01ba\0\235\0\323\0\305" +
		"\0\u0112\0\320\0\u011f\0\321\0\u0121\0\355\0\323\0\357\0\323\0\360\0\323\0\363\0" +
		"\323\0\u0117\0\323\0\u011c\0\323\0\u011e\0\u016b\0\u0139\0\323\0\u013c\0\323\0\u013e" +
		"\0\323\0\u0140\0\323\0\u0142\0\323\0\u0143\0\323\0\u0180\0\323\0\u0184\0\323\0\u0187" +
		"\0\323\0\u0189\0\323\0\u01b1\0\323\0\235\0\324\0\305\0\324\0\320\0\324\0\321\0\324" +
		"\0\323\0\u0124\0\355\0\324\0\357\0\324\0\360\0\324\0\363\0\324\0\u0112\0\u0124\0" +
		"\u0117\0\324\0\u011c\0\324\0\u011e\0\324\0\u011f\0\u0124\0\u0121\0\u0124\0\u0127" +
		"\0\u0172\0\u0139\0\324\0\u013c\0\324\0\u013e\0\324\0\u0140\0\324\0\u0142\0\324\0" +
		"\u0143\0\324\0\u016b\0\u0124\0\u0180\0\324\0\u0184\0\324\0\u0187\0\324\0\u0189\0" +
		"\324\0\u01b1\0\324\0\306\0\u0115\0\235\0\325\0\305\0\325\0\320\0\325\0\321\0\325" +
		"\0\323\0\325\0\355\0\325\0\357\0\325\0\360\0\325\0\363\0\325\0\u0112\0\325\0\u0117" +
		"\0\325\0\u011c\0\325\0\u011e\0\325\0\u011f\0\325\0\u0121\0\325\0\u0127\0\325\0\u0139" +
		"\0\325\0\u013c\0\325\0\u013e\0\325\0\u0140\0\325\0\u0142\0\325\0\u0143\0\325\0\u016b" +
		"\0\325\0\u0180\0\325\0\u0184\0\325\0\u0187\0\325\0\u0189\0\325\0\u01b1\0\325\0\306" +
		"\0\u0116\0\u015e\0\u019a\0\235\0\326\0\305\0\326\0\320\0\326\0\321\0\326\0\323\0" +
		"\326\0\355\0\326\0\357\0\326\0\360\0\326\0\363\0\326\0\u0112\0\326\0\u0117\0\326" +
		"\0\u011c\0\326\0\u011e\0\326\0\u011f\0\326\0\u0121\0\326\0\u0127\0\326\0\u0139\0" +
		"\326\0\u013c\0\326\0\u013e\0\326\0\u0140\0\326\0\u0142\0\326\0\u0143\0\326\0\u016b" +
		"\0\326\0\u0180\0\326\0\u0184\0\326\0\u0187\0\326\0\u0189\0\326\0\u01b1\0\326\0\235" +
		"\0\327\0\305\0\327\0\320\0\327\0\321\0\327\0\323\0\327\0\355\0\327\0\357\0\327\0" +
		"\360\0\327\0\363\0\327\0\u0112\0\327\0\u0117\0\327\0\u011c\0\327\0\u011e\0\327\0" +
		"\u011f\0\327\0\u0121\0\327\0\u0127\0\327\0\u0139\0\327\0\u013c\0\327\0\u013e\0\327" +
		"\0\u0140\0\327\0\u0142\0\327\0\u0143\0\327\0\u016b\0\327\0\u0180\0\327\0\u0184\0" +
		"\327\0\u0187\0\327\0\u0189\0\327\0\u01b1\0\327\0\235\0\330\0\305\0\330\0\320\0\330" +
		"\0\321\0\330\0\323\0\330\0\337\0\u012d\0\355\0\330\0\357\0\330\0\360\0\330\0\363" +
		"\0\330\0\u0112\0\330\0\u0117\0\330\0\u011c\0\330\0\u011e\0\330\0\u011f\0\330\0\u0121" +
		"\0\330\0\u0122\0\u012d\0\u0127\0\330\0\u0139\0\330\0\u013c\0\330\0\u013e\0\330\0" +
		"\u0140\0\330\0\u0142\0\330\0\u0143\0\330\0\u016b\0\330\0\u0180\0\330\0\u0184\0\330" +
		"\0\u0187\0\330\0\u0189\0\330\0\u01b1\0\330\0\235\0\331\0\305\0\331\0\320\0\331\0" +
		"\321\0\331\0\323\0\331\0\337\0\331\0\355\0\331\0\357\0\331\0\360\0\331\0\363\0\331" +
		"\0\u0112\0\331\0\u0117\0\331\0\u0119\0\u0167\0\u011b\0\u0169\0\u011c\0\331\0\u011e" +
		"\0\331\0\u011f\0\331\0\u0121\0\331\0\u0122\0\331\0\u0127\0\331\0\u0139\0\331\0\u013c" +
		"\0\331\0\u013e\0\331\0\u0140\0\331\0\u0142\0\331\0\u0143\0\331\0\u016b\0\331\0\u0180" +
		"\0\331\0\u0184\0\331\0\u0187\0\331\0\u0189\0\331\0\u01b1\0\331\0\235\0\332\0\305" +
		"\0\332\0\320\0\332\0\321\0\332\0\323\0\332\0\337\0\332\0\355\0\332\0\357\0\332\0" +
		"\360\0\332\0\363\0\332\0\u0112\0\332\0\u0117\0\332\0\u0119\0\332\0\u011b\0\332\0" +
		"\u011c\0\332\0\u011e\0\332\0\u011f\0\332\0\u0121\0\332\0\u0122\0\332\0\u0127\0\332" +
		"\0\u0139\0\332\0\u013c\0\332\0\u013e\0\332\0\u0140\0\332\0\u0142\0\332\0\u0143\0" +
		"\332\0\u016b\0\332\0\u0180\0\332\0\u0184\0\332\0\u0187\0\332\0\u0189\0\332\0\u01b1" +
		"\0\332\0\235\0\333\0\305\0\333\0\320\0\333\0\321\0\333\0\323\0\333\0\355\0\333\0" +
		"\357\0\333\0\360\0\333\0\363\0\333\0\u0112\0\333\0\u0117\0\333\0\u011c\0\333\0\u011e" +
		"\0\333\0\u011f\0\333\0\u0121\0\333\0\u0127\0\333\0\u0139\0\333\0\u013c\0\333\0\u013e" +
		"\0\333\0\u0140\0\333\0\u0142\0\333\0\u0143\0\333\0\u016b\0\333\0\u0180\0\333\0\u0184" +
		"\0\333\0\u0187\0\333\0\u0189\0\333\0\u01b1\0\333\0\235\0\334\0\305\0\334\0\320\0" +
		"\334\0\321\0\334\0\323\0\334\0\337\0\334\0\355\0\334\0\357\0\334\0\360\0\334\0\363" +
		"\0\334\0\u0112\0\334\0\u0117\0\334\0\u0119\0\334\0\u011b\0\334\0\u011c\0\334\0\u011e" +
		"\0\334\0\u011f\0\334\0\u0121\0\334\0\u0122\0\334\0\u0127\0\334\0\u0139\0\334\0\u013c" +
		"\0\334\0\u013e\0\334\0\u0140\0\334\0\u0142\0\334\0\u0143\0\334\0\u016b\0\334\0\u0180" +
		"\0\334\0\u0184\0\334\0\u0187\0\334\0\u0189\0\334\0\u01b1\0\334\0\235\0\335\0\305" +
		"\0\335\0\320\0\335\0\321\0\335\0\323\0\335\0\337\0\335\0\355\0\335\0\357\0\335\0" +
		"\360\0\335\0\363\0\335\0\u0112\0\335\0\u0117\0\335\0\u0119\0\335\0\u011a\0\u0168" +
		"\0\u011b\0\335\0\u011c\0\335\0\u011e\0\335\0\u011f\0\335\0\u0121\0\335\0\u0122\0" +
		"\335\0\u0127\0\335\0\u0139\0\335\0\u013c\0\335\0\u013e\0\335\0\u0140\0\335\0\u0142" +
		"\0\335\0\u0143\0\335\0\u016b\0\335\0\u0180\0\335\0\u0184\0\335\0\u0187\0\335\0\u0189" +
		"\0\335\0\u01b1\0\335\0\235\0\336\0\271\0\u0100\0\272\0\u0101\0\305\0\336\0\320\0" +
		"\336\0\321\0\336\0\323\0\336\0\337\0\336\0\355\0\336\0\357\0\336\0\360\0\336\0\363" +
		"\0\336\0\377\0\u014b\0\u0112\0\336\0\u0117\0\336\0\u0119\0\336\0\u011a\0\336\0\u011b" +
		"\0\336\0\u011c\0\336\0\u011e\0\336\0\u011f\0\336\0\u0121\0\336\0\u0122\0\336\0\u0127" +
		"\0\336\0\u0139\0\336\0\u013c\0\336\0\u013e\0\336\0\u0140\0\336\0\u0142\0\336\0\u0143" +
		"\0\336\0\u016b\0\336\0\u0180\0\336\0\u0184\0\336\0\u0187\0\336\0\u0189\0\336\0\u01b1" +
		"\0\336\0\u0118\0\u0164\0\u0160\0\u0164\0\u0161\0\u019d\0\u019f\0\u0164\0\u01a1\0" +
		"\u0164\0\u0118\0\u0165\0\u0160\0\u019c\0\u019f\0\u01b8\0\u01a1\0\u01b9\0\141\0\174" +
		"\0\167\0\174\0\235\0\174\0\305\0\174\0\320\0\174\0\321\0\174\0\323\0\174\0\355\0" +
		"\174\0\357\0\174\0\360\0\174\0\363\0\174\0\u0112\0\174\0\u0117\0\174\0\u011c\0\174" +
		"\0\u011e\0\174\0\u011f\0\174\0\u0121\0\174\0\u0127\0\174\0\u0139\0\174\0\u013c\0" +
		"\174\0\u013e\0\174\0\u0140\0\174\0\u0142\0\174\0\u0143\0\174\0\u016b\0\174\0\u0180" +
		"\0\174\0\u0184\0\174\0\u0187\0\174\0\u0189\0\174\0\u01b1\0\174\0\141\0\175\0\167" +
		"\0\175\0\235\0\337\0\305\0\337\0\320\0\337\0\321\0\u0122\0\323\0\u0122\0\355\0\337" +
		"\0\357\0\337\0\360\0\337\0\363\0\337\0\u0112\0\u0122\0\u0117\0\337\0\u011c\0\337" +
		"\0\u011e\0\u0122\0\u011f\0\u0122\0\u0121\0\u0122\0\u0127\0\u0122\0\u0139\0\337\0" +
		"\u013c\0\337\0\u013e\0\337\0\u0140\0\337\0\u0142\0\337\0\u0143\0\337\0\u016b\0\u0122" +
		"\0\u0180\0\337\0\u0184\0\337\0\u0187\0\337\0\u0189\0\337\0\u01b1\0\337\0\141\0\176" +
		"\0\167\0\176\0\174\0\253\0\235\0\176\0\305\0\176\0\320\0\176\0\321\0\176\0\323\0" +
		"\176\0\355\0\176\0\357\0\176\0\360\0\176\0\363\0\176\0\u0112\0\176\0\u0117\0\176" +
		"\0\u011c\0\176\0\u011e\0\176\0\u011f\0\176\0\u0121\0\176\0\u0127\0\176\0\u0139\0" +
		"\176\0\u013c\0\176\0\u013e\0\176\0\u0140\0\176\0\u0142\0\176\0\u0143\0\176\0\u016b" +
		"\0\176\0\u0180\0\176\0\u0184\0\176\0\u0187\0\176\0\u0189\0\176\0\u01b1\0\176\0\237" +
		"\0\345\0\166\0\250\0\254\0\366\0\237\0\346\0\u0131\0\u0177\0\26\0\47\0\43\0\70\0" +
		"\44\0\71\0\73\0\47\0\237\0\347\0\304\0\u010e\0\u010d\0\u0154\0\u0131\0\347\0\u0157" +
		"\0\u010e\0\u0159\0\u010e\0\26\0\50\0\26\0\51\0\10\0\27\0\310\0\27\0\u0162\0\27\0" +
		"\26\0\52\0\73\0\115\0\164\0\232\0\231\0\277\0\72\0\113\0\u0107\0\u0151\0\u014f\0" +
		"\u0193\0\u0176\0\u01a6\0\304\0\u010f\0\u0157\0\u010f\0\u0159\0\u010f\0\304\0\u0110" +
		"\0\u0157\0\u0197\0\u0159\0\u0198\0\1\0\u01c5\0\6\0\23\0\41\0\67\0\106\0\140\0\116" +
		"\0\142\0\301\0\u0109\0\6\0\24\0\6\0\25\0\53\0\76\0\53\0\77\0\53\0\100\0\117\0\143" +
		"\0\1\0\15\0\6\0\15\0\41\0\15\0\72\0\114\0\106\0\15\0\116\0\15\0\301\0\15\0\u0107" +
		"\0\114\0\u0129\0\u0174\0\u014f\0\114\0\u0155\0\u0195\0\u0156\0\u0196\0\u0176\0\114" +
		"\0\2\0\20\0\13\0\30\0\31\0\54\0\2\0\21\0\13\0\21\0\31\0\21\0\235\0\340\0\305\0\340" +
		"\0\320\0\340\0\321\0\340\0\323\0\340\0\355\0\340\0\357\0\340\0\360\0\340\0\363\0" +
		"\340\0\u0112\0\340\0\u0117\0\340\0\u011c\0\340\0\u011e\0\340\0\u011f\0\340\0\u0121" +
		"\0\340\0\u0127\0\340\0\u0139\0\340\0\u013c\0\340\0\u013e\0\340\0\u0140\0\340\0\u0142" +
		"\0\340\0\u0143\0\340\0\u0148\0\u018f\0\u016b\0\340\0\u0180\0\340\0\u0184\0\340\0" +
		"\u0187\0\340\0\u0189\0\340\0\u0191\0\u018f\0\u01b1\0\340\0\1\0\16\0\6\0\16\0\37\0" +
		"\66\0\41\0\16\0\106\0\16\0\116\0\16\0\130\0\161\0\165\0\234\0\167\0\252\0\212\0\161" +
		"\0\235\0\341\0\301\0\16\0\305\0\341\0\323\0\u0125\0\355\0\341\0\357\0\341\0\360\0" +
		"\341\0\363\0\341\0\u0112\0\u0125\0\u0117\0\341\0\u011c\0\341\0\u011f\0\u0125\0\u0121" +
		"\0\u0125\0\u0139\0\341\0\u013c\0\341\0\u013e\0\341\0\u0140\0\341\0\u0142\0\341\0" +
		"\u0143\0\341\0\u016b\0\u0125\0\u0180\0\341\0\u0184\0\341\0\u0187\0\341\0\u0189\0" +
		"\341\0\u01b1\0\341\0\20\0\34\0\101\0\121\0\127\0\157\0\163\0\213\0\262\0\372\0\373" +
		"\0\u0149\0\372\0\u0148\0\u0149\0\u0191\0\u0148\0\u0190\0\u0191\0\u01b4\0\242\0\353" +
		"\0\243\0\354\0\350\0\u0133\0\353\0\u0136\0\354\0\u0137\0\u0133\0\u0178\0\235\0\342" +
		"\0\305\0\342\0\320\0\u0120\0\321\0\u0123\0\323\0\u0126\0\355\0\342\0\357\0\342\0" +
		"\360\0\342\0\363\0\342\0\u0112\0\u0126\0\u0117\0\342\0\u011c\0\342\0\u011e\0\u016c" +
		"\0\u011f\0\u016d\0\u0121\0\u016f\0\u0139\0\342\0\u013c\0\342\0\u013e\0\342\0\u0140" +
		"\0\342\0\u0142\0\342\0\u0143\0\342\0\u016b\0\u01a2\0\u0180\0\342\0\u0184\0\342\0" +
		"\u0187\0\342\0\u0189\0\342\0\u01b1\0\342\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(247,
		"\2\0\0\0\5\0\4\0\2\0\0\0\7\0\4\0\3\0\3\0\4\0\4\0\3\0\3\0\1\0\1\0\2\0\1\0\1\0\1\0" +
		"\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\4\0\3\0\3\0\3\0\1\0\10\0\4\0\7\0\3\0\3\0" +
		"\1\0\1\0\1\0\1\0\5\0\3\0\1\0\4\0\4\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\10\0\7\0\7\0" +
		"\6\0\7\0\6\0\6\0\5\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0\4\0\2\0\4\0\3\0\3\0\1\0\1\0\2\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0\4\0\5\0\5\0\6\0\3\0\1\0\3\0\1\0" +
		"\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\5\0\4\0\4\0\3\0\4\0\3\0\3\0\2\0\4\0\3\0\3\0" +
		"\2\0\3\0\2\0\2\0\1\0\1\0\3\0\2\0\3\0\3\0\4\0\2\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0" +
		"\3\0\1\0\3\0\2\0\1\0\2\0\1\0\2\0\1\0\3\0\3\0\1\0\2\0\1\0\3\0\3\0\3\0\1\0\3\0\1\0" +
		"\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0\1\0\3\0\2\0\1\0\3\0\3\0\2\0\1\0\1\0\4\0\2\0" +
		"\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0\1\0\1\0\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0" +
		"\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0\1\0\5\0\3\0\1\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0" +
		"\0\0\3\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0" +
		"\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(247,
		"\121\0\121\0\122\0\122\0\123\0\123\0\124\0\124\0\125\0\126\0\127\0\130\0\130\0\131" +
		"\0\131\0\132\0\133\0\133\0\134\0\135\0\136\0\137\0\137\0\137\0\140\0\140\0\140\0" +
		"\140\0\140\0\141\0\142\0\143\0\143\0\144\0\144\0\145\0\145\0\145\0\145\0\146\0\147" +
		"\0\147\0\147\0\147\0\150\0\151\0\151\0\152\0\152\0\153\0\154\0\155\0\155\0\155\0" +
		"\156\0\156\0\156\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\157" +
		"\0\157\0\157\0\157\0\157\0\157\0\160\0\160\0\160\0\160\0\160\0\160\0\161\0\162\0" +
		"\162\0\162\0\163\0\163\0\163\0\164\0\164\0\164\0\164\0\165\0\165\0\165\0\165\0\165" +
		"\0\165\0\166\0\166\0\167\0\167\0\170\0\170\0\171\0\171\0\172\0\172\0\173\0\173\0" +
		"\174\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175" +
		"\0\175\0\175\0\175\0\175\0\176\0\177\0\200\0\200\0\201\0\201\0\202\0\202\0\202\0" +
		"\203\0\203\0\203\0\203\0\203\0\204\0\204\0\205\0\206\0\206\0\207\0\210\0\210\0\211" +
		"\0\211\0\211\0\212\0\212\0\213\0\213\0\213\0\214\0\215\0\215\0\216\0\216\0\216\0" +
		"\216\0\216\0\216\0\216\0\216\0\217\0\220\0\220\0\220\0\220\0\221\0\221\0\221\0\222" +
		"\0\222\0\223\0\224\0\224\0\224\0\225\0\225\0\226\0\227\0\227\0\227\0\230\0\231\0" +
		"\231\0\232\0\232\0\233\0\234\0\234\0\234\0\234\0\235\0\235\0\236\0\236\0\237\0\237" +
		"\0\237\0\237\0\240\0\240\0\240\0\241\0\241\0\241\0\241\0\241\0\242\0\242\0\243\0" +
		"\243\0\244\0\244\0\245\0\245\0\246\0\247\0\247\0\247\0\247\0\250\0\251\0\251\0\252" +
		"\0\253\0\254\0\254\0\255\0\255\0\256\0\256\0\257\0\257\0\260\0\260\0\261\0\261\0" +
		"\262\0\262\0\263\0\263\0");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"scon",
		"icon",
		"_skip",
		"_skip_comment",
		"_skip_multiline",
		"'%'",
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
		"'true'",
		"'false'",
		"'new'",
		"'separator'",
		"'as'",
		"'import'",
		"'set'",
		"'implements'",
		"'brackets'",
		"'s'",
		"'x'",
		"'inline'",
		"'prec'",
		"'shift'",
		"'returns'",
		"'input'",
		"'left'",
		"'right'",
		"'nonassoc'",
		"'generate'",
		"'assert'",
		"'empty'",
		"'nonempty'",
		"'global'",
		"'explicit'",
		"'lookahead'",
		"'param'",
		"'flag'",
		"'noeoi'",
		"'soft'",
		"'class'",
		"'interface'",
		"'void'",
		"'space'",
		"'layout'",
		"'language'",
		"'lalr'",
		"'lexer'",
		"'parser'",
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
		"start_conditions_scope",
		"start_conditions",
		"stateref_list_Comma_separated",
		"lexeme",
		"lexeme_attrs",
		"lexeme_attribute",
		"brackets_directive",
		"lexer_state_list_Comma_separated",
		"states_clause",
		"stateref",
		"lexer_state",
		"grammar_parts",
		"grammar_part",
		"nonterm",
		"nonterm_type",
		"implements_clause",
		"assoc",
		"param_modifier",
		"template_param",
		"directive",
		"identifier_list_Comma_separated",
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
		"reportClause",
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
		"implements_clauseopt",
		"rhsSuffixopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int import__optlist = 81;
		int input1 = 82;
		int option_optlist = 83;
		int header = 84;
		int lexer_section = 85;
		int parser_section = 86;
		int parsing_algorithm = 87;
		int import_ = 88;
		int option = 89;
		int identifier = 90;
		int symref = 91;
		int symref_noargs = 92;
		int rawType = 93;
		int pattern = 94;
		int lexer_parts = 95;
		int lexer_part = 96;
		int named_pattern = 97;
		int start_conditions_scope = 98;
		int start_conditions = 99;
		int stateref_list_Comma_separated = 100;
		int lexeme = 101;
		int lexeme_attrs = 102;
		int lexeme_attribute = 103;
		int brackets_directive = 104;
		int lexer_state_list_Comma_separated = 105;
		int states_clause = 106;
		int stateref = 107;
		int lexer_state = 108;
		int grammar_parts = 109;
		int grammar_part = 110;
		int nonterm = 111;
		int nonterm_type = 112;
		int implements_clause = 113;
		int assoc = 114;
		int param_modifier = 115;
		int template_param = 116;
		int directive = 117;
		int identifier_list_Comma_separated = 118;
		int inputref_list_Comma_separated = 119;
		int inputref = 120;
		int references = 121;
		int references_cs = 122;
		int rule0_list_Or_separated = 123;
		int rules = 124;
		int rule0 = 125;
		int predicate = 126;
		int rhsPrefix = 127;
		int rhsSuffix = 128;
		int reportClause = 129;
		int rhsParts = 130;
		int rhsPart = 131;
		int lookahead_predicate_list_And_separated = 132;
		int rhsLookahead = 133;
		int lookahead_predicate = 134;
		int rhsStateMarker = 135;
		int rhsAnnotated = 136;
		int rhsAssignment = 137;
		int rhsOptional = 138;
		int rhsCast = 139;
		int rhsUnordered = 140;
		int rhsClass = 141;
		int rhsPrimary = 142;
		int rhsSet = 143;
		int setPrimary = 144;
		int setExpression = 145;
		int annotation_list = 146;
		int annotations = 147;
		int annotation = 148;
		int nonterm_param_list_Comma_separated = 149;
		int nonterm_params = 150;
		int nonterm_param = 151;
		int param_ref = 152;
		int argument_list_Comma_separated = 153;
		int argument_list_Comma_separated_opt = 154;
		int symref_args = 155;
		int argument = 156;
		int param_type = 157;
		int param_value = 158;
		int predicate_primary = 159;
		int predicate_expression = 160;
		int expression = 161;
		int expression_list_Comma_separated = 162;
		int expression_list_Comma_separated_opt = 163;
		int map_entry_list_Comma_separated = 164;
		int map_entry_list_Comma_separated_opt = 165;
		int map_entry = 166;
		int literal = 167;
		int name = 168;
		int qualified_id = 169;
		int command = 170;
		int syntax_problem = 171;
		int parsing_algorithmopt = 172;
		int rawTypeopt = 173;
		int iconopt = 174;
		int lexeme_attrsopt = 175;
		int commandopt = 176;
		int identifieropt = 177;
		int implements_clauseopt = 178;
		int rhsSuffixopt = 179;
	}

	public interface Rules {
		int nonterm_type_nontermTypeAST = 73;  // nonterm_type : 'returns' symref_noargs
		int nonterm_type_nontermTypeHint = 74;  // nonterm_type : 'inline' 'class' identifieropt implements_clauseopt
		int nonterm_type_nontermTypeHint2 = 75;  // nonterm_type : 'class' identifieropt implements_clauseopt
		int nonterm_type_nontermTypeHint3 = 76;  // nonterm_type : 'interface' identifieropt implements_clauseopt
		int nonterm_type_nontermTypeHint4 = 77;  // nonterm_type : 'void'
		int directive_directivePrio = 90;  // directive : '%' assoc references ';'
		int directive_directiveInput = 91;  // directive : '%' 'input' inputref_list_Comma_separated ';'
		int directive_directiveInterface = 92;  // directive : '%' 'interface' identifier_list_Comma_separated ';'
		int directive_directiveAssert = 93;  // directive : '%' 'assert' 'empty' rhsSet ';'
		int directive_directiveAssert2 = 94;  // directive : '%' 'assert' 'nonempty' rhsSet ';'
		int directive_directiveSet = 95;  // directive : '%' 'generate' ID '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 152;  // rhsOptional : rhsCast '?'
		int rhsCast_rhsAsLiteral = 155;  // rhsCast : rhsClass 'as' literal
		int rhsPrimary_rhsSymbol = 159;  // rhsPrimary : symref
		int rhsPrimary_rhsNested = 160;  // rhsPrimary : '(' rules ')'
		int rhsPrimary_rhsList = 161;  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
		int rhsPrimary_rhsList2 = 162;  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
		int rhsPrimary_rhsQuantifier = 163;  // rhsPrimary : rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 164;  // rhsPrimary : rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 165;  // rhsPrimary : '$' '(' rules ')'
		int setPrimary_setSymbol = 168;  // setPrimary : ID symref
		int setPrimary_setSymbol2 = 169;  // setPrimary : symref
		int setPrimary_setCompound = 170;  // setPrimary : '(' setExpression ')'
		int setPrimary_setComplement = 171;  // setPrimary : '~' setPrimary
		int setExpression_setBinary = 173;  // setExpression : setExpression '|' setExpression
		int setExpression_setBinary2 = 174;  // setExpression : setExpression '&' setExpression
		int nonterm_param_inlineParameter = 185;  // nonterm_param : ID identifier '=' param_value
		int nonterm_param_inlineParameter2 = 186;  // nonterm_param : ID identifier
		int predicate_primary_boolPredicate = 201;  // predicate_primary : '!' param_ref
		int predicate_primary_boolPredicate2 = 202;  // predicate_primary : param_ref
		int predicate_primary_comparePredicate = 203;  // predicate_primary : param_ref '==' literal
		int predicate_primary_comparePredicate2 = 204;  // predicate_primary : param_ref '!=' literal
		int predicate_expression_predicateBinary = 206;  // predicate_expression : predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 207;  // predicate_expression : predicate_expression '||' predicate_expression
		int expression_instance = 210;  // expression : 'new' name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 211;  // expression : '[' expression_list_Comma_separated_opt ']'
	}

	// set(follow error)
	private static int[] afterErr = {
		6, 7, 8, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 34, 35,
		36, 37, 41, 44, 77
	};

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

	protected static int gotoState(int state, int symbol) {
		int min = tmGoto[symbol], max = tmGoto[symbol + 1];
		int i, e;

		while (min < max) {
			e = (min + max) >> 2 << 1;
			i = tmFromTo[e];
			if (i == state) {
				return tmFromTo[e+1];
			} else if (i < state) {
				min = e + 2;
			} else {
				max = e;
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
		while (tmHead >= 0 && gotoState(tmStack[tmHead].state, 36) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new Span();
			tmStack[tmHead].symbol = 36;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, 36);
			tmStack[tmHead].line = tmNext.line;
			tmStack[tmHead].offset = tmNext.offset;
			tmStack[tmHead].endoffset = tmNext.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, tmNext.symbol);
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
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
		switch (ruleIndex) {
			case 0:  // import__optlist : import__optlist import_
				((List<TmaImport>)tmLeft.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 1:  // import__optlist :
				tmLeft.value = new ArrayList();
				break;
			case 2:  // input : header import__optlist option_optlist lexer_section parser_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexer */,
						((List<ITmaGrammarPart>)tmStack[tmHead].value) /* parser */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 3:  // input : header import__optlist option_optlist lexer_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 3].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 2].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 1].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead].value) /* lexer */,
						null /* parser */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 4:  // option_optlist : option_optlist option
				((List<TmaOption>)tmLeft.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 5:  // option_optlist :
				tmLeft.value = new ArrayList();
				break;
			case 6:  // header : 'language' name '(' name ')' parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 5].value) /* name */,
						((TmaName)tmStack[tmHead - 3].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 7:  // header : 'language' name parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 2].value) /* name */,
						null /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // lexer_section : '::' 'lexer' lexer_parts
				tmLeft.value = ((List<ITmaLexerPart>)tmStack[tmHead].value);
				break;
			case 9:  // parser_section : '::' 'parser' grammar_parts
				tmLeft.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value);
				break;
			case 10:  // parsing_algorithm : 'lalr' '(' icon ')'
				tmLeft.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // import_ : 'import' ID scon ';'
				tmLeft.value = new TmaImport(
						((String)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 12:  // import_ : 'import' scon ';'
				tmLeft.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 13:  // option : ID '=' expression
				tmLeft.value = new TmaOption(
						((String)tmStack[tmHead - 2].value) /* key */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 14:  // option : syntax_problem
				tmLeft.value = new TmaOption(
						null /* key */,
						null /* value */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 15:  // identifier : ID
				tmLeft.value = new TmaIdentifier(
						((String)tmStack[tmHead].value) /* ID */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 16:  // symref : ID symref_args
				tmLeft.value = new TmaSymref(
						((String)tmStack[tmHead - 1].value) /* name */,
						((TmaSymrefArgs)tmStack[tmHead].value) /* args */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 17:  // symref : ID
				tmLeft.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // symref_noargs : ID
				tmLeft.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // rawType : code
				tmLeft.value = new TmaRawType(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 20:  // pattern : regexp
				tmLeft.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 21:  // lexer_parts : lexer_part
				tmLeft.value = new ArrayList();
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 22:  // lexer_parts : lexer_parts lexer_part
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 23:  // lexer_parts : lexer_parts syntax_problem
				((List<ITmaLexerPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 29:  // named_pattern : ID '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // start_conditions_scope : start_conditions '{' lexer_parts '}'
				tmLeft.value = new TmaStartConditionsScope(
						((TmaStartConditions)tmStack[tmHead - 3].value) /* startConditions */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexerParts */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // start_conditions : '<' '*' '>'
				tmLeft.value = new TmaStartConditions(
						null /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // start_conditions : '<' stateref_list_Comma_separated '>'
				tmLeft.value = new TmaStartConditions(
						((List<TmaStateref>)tmStack[tmHead - 1].value) /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 33:  // stateref_list_Comma_separated : stateref_list_Comma_separated ',' stateref
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 34:  // stateref_list_Comma_separated : stateref
				tmLeft.value = new ArrayList();
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 35:  // lexeme : start_conditions identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
				tmLeft.value = new TmaLexeme(
						((TmaStartConditions)tmStack[tmHead - 7].value) /* startConditions */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaRawType)tmStack[tmHead - 5].value) /* rawType */,
						((TmaPattern)tmStack[tmHead - 3].value) /* pattern */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead - 1].value) /* attrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						source, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 36:  // lexeme : start_conditions identifier rawTypeopt ':'
				tmLeft.value = new TmaLexeme(
						((TmaStartConditions)tmStack[tmHead - 3].value) /* startConditions */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaRawType)tmStack[tmHead - 1].value) /* rawType */,
						null /* pattern */,
						null /* priority */,
						null /* attrs */,
						null /* command */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // lexeme : identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
				tmLeft.value = new TmaLexeme(
						null /* startConditions */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaRawType)tmStack[tmHead - 5].value) /* rawType */,
						((TmaPattern)tmStack[tmHead - 3].value) /* pattern */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead - 1].value) /* attrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 38:  // lexeme : identifier rawTypeopt ':'
				tmLeft.value = new TmaLexeme(
						null /* startConditions */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaRawType)tmStack[tmHead - 1].value) /* rawType */,
						null /* pattern */,
						null /* priority */,
						null /* attrs */,
						null /* command */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 39:  // lexeme_attrs : '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 40:  // lexeme_attribute : 'soft'
				tmLeft.value = TmaLexemeAttribute.SOFT;
				break;
			case 41:  // lexeme_attribute : 'class'
				tmLeft.value = TmaLexemeAttribute.CLASS;
				break;
			case 42:  // lexeme_attribute : 'space'
				tmLeft.value = TmaLexemeAttribute.SPACE;
				break;
			case 43:  // lexeme_attribute : 'layout'
				tmLeft.value = TmaLexemeAttribute.LAYOUT;
				break;
			case 44:  // brackets_directive : '%' 'brackets' symref_noargs symref_noargs ';'
				tmLeft.value = new TmaBracketsDirective(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 45:  // lexer_state_list_Comma_separated : lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 46:  // lexer_state_list_Comma_separated : lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 47:  // states_clause : '%' 's' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						false /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 48:  // states_clause : '%' 'x' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						true /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // stateref : ID
				tmLeft.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // lexer_state : identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 51:  // grammar_parts : grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 52:  // grammar_parts : grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 53:  // grammar_parts : grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 57:  // nonterm : annotations identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 7].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // nonterm : annotations identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // nonterm : annotations identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // nonterm : annotations identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 61:  // nonterm : annotations identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 62:  // nonterm : annotations identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 63:  // nonterm : annotations identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 64:  // nonterm : annotations identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 65:  // nonterm : identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 66:  // nonterm : identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 67:  // nonterm : identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 68:  // nonterm : identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 69:  // nonterm : identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 70:  // nonterm : identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // nonterm : identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 72:  // nonterm : identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 73:  // nonterm_type : 'returns' symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 74:  // nonterm_type : 'inline' 'class' identifieropt implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 75:  // nonterm_type : 'class' identifieropt implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // nonterm_type : 'interface' identifieropt implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.INTERFACE /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // nonterm_type : 'void'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.VOID /* kind */,
						null /* name */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 79:  // implements_clause : 'implements' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 80:  // assoc : 'left'
				tmLeft.value = TmaAssoc.LEFT;
				break;
			case 81:  // assoc : 'right'
				tmLeft.value = TmaAssoc.RIGHT;
				break;
			case 82:  // assoc : 'nonassoc'
				tmLeft.value = TmaAssoc.NONASSOC;
				break;
			case 83:  // param_modifier : 'explicit'
				tmLeft.value = TmaParamModifier.EXPLICIT;
				break;
			case 84:  // param_modifier : 'global'
				tmLeft.value = TmaParamModifier.GLOBAL;
				break;
			case 85:  // param_modifier : 'lookahead'
				tmLeft.value = TmaParamModifier.LOOKAHEAD;
				break;
			case 86:  // template_param : '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 87:  // template_param : '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 88:  // template_param : '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // template_param : '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // directive : '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // directive : '%' 'input' inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // directive : '%' 'interface' identifier_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInterface(
						((List<TmaIdentifier>)tmStack[tmHead - 1].value) /* ids */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // directive : '%' 'assert' 'empty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.EMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // directive : '%' 'assert' 'nonempty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.NONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 95:  // directive : '%' 'generate' ID '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((String)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 96:  // identifier_list_Comma_separated : identifier_list_Comma_separated ',' identifier
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 97:  // identifier_list_Comma_separated : identifier
				tmLeft.value = new ArrayList();
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 98:  // inputref_list_Comma_separated : inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 99:  // inputref_list_Comma_separated : inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 100:  // inputref : symref_noargs 'noeoi'
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // inputref : symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // references : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 103:  // references : references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 104:  // references_cs : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 105:  // references_cs : references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 106:  // rule0_list_Or_separated : rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 107:  // rule0_list_Or_separated : rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 109:  // rule0 : predicate rhsPrefix rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 4].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // rule0 : predicate rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // rule0 : predicate rhsPrefix rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // rule0 : predicate rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rule0 : predicate rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // rule0 : predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // rule0 : predicate rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // rule0 : predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // rule0 : rhsPrefix rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // rule0 : rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 119:  // rule0 : rhsPrefix rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // rule0 : rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 121:  // rule0 : rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // rule0 : rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // rule0 : rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 124:  // rule0 : rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 125:  // rule0 : syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* suffix */,
						null /* action */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 126:  // predicate : '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 127:  // rhsPrefix : annotations ':'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // rhsSuffix : '%' 'prec' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.PREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // rhsSuffix : '%' 'shift' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.SHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // reportClause : '->' identifier '/' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((TmaIdentifier)tmStack[tmHead].value) /* kind */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 131:  // reportClause : '->' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead].value) /* action */,
						null /* kind */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // rhsParts : rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 133:  // rhsParts : rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 134:  // rhsParts : rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 140:  // lookahead_predicate_list_And_separated : lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 141:  // lookahead_predicate_list_And_separated : lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 142:  // rhsLookahead : '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // lookahead_predicate : '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // lookahead_predicate : symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // rhsStateMarker : '.' ID
				tmLeft.value = new TmaRhsStateMarker(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // rhsAnnotated : annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // rhsAssignment : identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rhsAssignment : identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rhsOptional : rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rhsCast : rhsClass 'as' symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rhsCast : rhsClass 'as' literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rhsUnordered : rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // rhsClass : identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // rhsPrimary : symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // rhsPrimary : '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // rhsPrimary : rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // rhsPrimary : rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // rhsPrimary : '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 167:  // rhsSet : 'set' '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 168:  // setPrimary : ID symref
				tmLeft.value = new TmaSetSymbol(
						((String)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 169:  // setPrimary : symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 170:  // setPrimary : '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 171:  // setPrimary : '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 173:  // setExpression : setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 174:  // setExpression : setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // annotation_list : annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 176:  // annotation_list : annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 177:  // annotations : annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 178:  // annotation : '@' ID '=' expression
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 179:  // annotation : '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 180:  // annotation : '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 181:  // nonterm_param_list_Comma_separated : nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 182:  // nonterm_param_list_Comma_separated : nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 183:  // nonterm_params : '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // nonterm_param : ID identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // nonterm_param : ID identifier
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // param_ref : identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 188:  // argument_list_Comma_separated : argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 189:  // argument_list_Comma_separated : argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 192:  // symref_args : '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // argument : param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 194:  // argument : '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // argument : '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // argument : param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 197:  // param_type : 'flag'
				tmLeft.value = TmaParamType.FLAG;
				break;
			case 198:  // param_type : 'param'
				tmLeft.value = TmaParamType.PARAM;
				break;
			case 201:  // predicate_primary : '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // predicate_primary : param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 203:  // predicate_primary : param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 204:  // predicate_primary : param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 206:  // predicate_expression : predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 207:  // predicate_expression : predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 210:  // expression : 'new' name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 211:  // expression : '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 213:  // expression_list_Comma_separated : expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 214:  // expression_list_Comma_separated : expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 217:  // map_entry_list_Comma_separated : map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 218:  // map_entry_list_Comma_separated : map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 221:  // map_entry : ID ':' expression
				tmLeft.value = new TmaMapEntry(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 222:  // literal : scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 223:  // literal : icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 224:  // literal : 'true'
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 225:  // literal : 'false'
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 226:  // name : qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 228:  // qualified_id : qualified_id '.' ID
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 229:  // command : code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 230:  // syntax_problem : error
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

	public TmaInput1 parseInput1(TMLexer lexer) throws IOException, ParseException {
		return (TmaInput1) parse(lexer, 0, 454);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 455);
	}
}
