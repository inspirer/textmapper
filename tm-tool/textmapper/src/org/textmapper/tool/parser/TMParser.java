/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
	private static final int[] tmAction = TMLexer.unpack_int(450,
		"\uffff\uffff\uffff\uffff\uffff\uffff\54\0\41\0\42\0\ufffd\uffff\52\0\0\0\44\0\43" +
		"\0\13\0\1\0\30\0\14\0\26\0\27\0\17\0\22\0\12\0\16\0\2\0\6\0\31\0\36\0\35\0\34\0\7" +
		"\0\37\0\20\0\23\0\11\0\15\0\21\0\40\0\3\0\5\0\10\0\24\0\4\0\33\0\32\0\25\0\uffab" +
		"\uffff\357\0\362\0\360\0\46\0\uff37\uffff\uffff\uffff\uff2d\uffff\364\0\ufee3\uffff" +
		"\uffff\uffff\ufedd\uffff\71\0\uffff\uffff\62\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\53\0\uffff\uffff\361\0\uffff\uffff\uffff\uffff\332\0\ufe93\uffff\ufe8b\uffff\uffff" +
		"\uffff\334\0\47\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\70" +
		"\0\ufe85\uffff\57\0\363\0\341\0\342\0\uffff\uffff\uffff\uffff\337\0\uffff\uffff\66" +
		"\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\55\0\73\0\346\0\347\0\340\0\333" +
		"\0\61\0\65\0\uffff\uffff\uffff\uffff\ufe7f\uffff\ufe77\uffff\75\0\100\0\104\0\uffff" +
		"\uffff\101\0\103\0\102\0\67\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\130\0\uffff\uffff\112\0\uffff\uffff\74\0\367\0\uffff\uffff\77\0\76\0\uffff" +
		"\uffff\ufe29\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufe23\uffff\132\0\135\0\136" +
		"\0\137\0\ufdd7\uffff\uffff\uffff\317\0\uffff\uffff\131\0\uffff\uffff\125\0\uffff" +
		"\uffff\107\0\uffff\uffff\110\0\45\0\105\0\ufd8b\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\172\0\344\0\uffff\uffff\173\0\uffff\uffff\uffff\uffff" +
		"\167\0\174\0\171\0\345\0\170\0\uffff\uffff\uffff\uffff\uffff\uffff\ufd39\uffff\323" +
		"\0\ufceb\uffff\uffff\uffff\uffff\uffff\ufc8d\uffff\uffff\uffff\163\0\uffff\uffff" +
		"\164\0\165\0\uffff\uffff\uffff\uffff\uffff\uffff\134\0\133\0\316\0\uffff\uffff\uffff" +
		"\uffff\126\0\uffff\uffff\127\0\111\0\ufc85\uffff\106\0\ufc2f\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufbdd\uffff\uffff\uffff\214\0\212\0\uffff" +
		"\uffff\217\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\ufbd5\uffff\uffff\uffff\uffff\uffff\uffff\uffff\51\0\ufb77\uffff\253" +
		"\0\236\0\276\0\ufb09\uffff\uffff\uffff\224\0\ufb01\uffff\u0101\0\ufaa5\uffff\247" +
		"\0\255\0\254\0\252\0\264\0\266\0\ufa47\uffff\uf9e5\uffff\305\0\uffff\uffff\uf97d" +
		"\uffff\uf973\uffff\uf965\uffff\uffff\uffff\325\0\327\0\uffff\uffff\377\0\162\0\uf91d" +
		"\uffff\160\0\uf915\uffff\uffff\uffff\uf8b7\uffff\uf859\uffff\uffff\uffff\uffff\uffff" +
		"\uf7fb\uffff\uffff\uffff\uffff\uffff\uffff\uffff\123\0\124\0\371\0\uf79d\uffff\uf749" +
		"\uffff\uffff\uffff\uffff\uffff\207\0\210\0\uffff\uffff\215\0\202\0\uffff\uffff\203" +
		"\0\uffff\uffff\201\0\220\0\uffff\uffff\uffff\uffff\200\0\321\0\uffff\uffff\uffff" +
		"\uffff\263\0\uffff\uffff\uf6f3\uffff\354\0\uffff\uffff\uffff\uffff\uf6e7\uffff\uffff" +
		"\uffff\262\0\uffff\uffff\257\0\uf689\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf62b" +
		"\uffff\157\0\uf5cb\uffff\uf56d\uffff\251\0\250\0\uf563\uffff\272\0\302\0\303\0\uffff" +
		"\uffff\265\0\234\0\uffff\uffff\uffff\uffff\244\0\uf559\uffff\uffff\uffff\326\0\221" +
		"\0\uf551\uffff\161\0\uffff\uffff\uf549\uffff\uffff\uffff\uffff\uffff\uf4eb\uffff" +
		"\uffff\uffff\uf48d\uffff\uffff\uffff\uf42f\uffff\uffff\uffff\uf3d1\uffff\uf373\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\373\0\uf315\uffff\uf2c3\uffff\204\0\205\0\uffff" +
		"\uffff\213\0\211\0\uffff\uffff\176\0\uffff\uffff\240\0\241\0\350\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\237\0\uffff\uffff\277\0\uffff\uffff\261\0\260\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uf26f\uffff\310\0\313\0\uffff\uffff\267\0\270\0\223" +
		"\0\uf223\uffff\230\0\232\0\275\0\274\0\246\0\uf219\uffff\uffff\uffff\324\0\uffff" +
		"\uffff\155\0\uffff\uffff\156\0\153\0\uffff\uffff\uf20b\uffff\uffff\uffff\147\0\uffff" +
		"\uffff\uf1ad\uffff\uffff\uffff\uffff\uffff\uf14f\uffff\uffff\uffff\uf0f1\uffff\120" +
		"\0\122\0\121\0\uffff\uffff\375\0\115\0\uf093\uffff\206\0\uffff\uffff\177\0\352\0" +
		"\353\0\uf041\uffff\uf039\uffff\uffff\uffff\256\0\304\0\uffff\uffff\312\0\307\0\uffff" +
		"\uffff\306\0\uffff\uffff\226\0\242\0\330\0\222\0\154\0\151\0\uffff\uffff\152\0\145" +
		"\0\uffff\uffff\146\0\143\0\uffff\uffff\uf031\uffff\uffff\uffff\117\0\113\0\175\0" +
		"\uffff\uffff\311\0\uefd3\uffff\uefcb\uffff\150\0\144\0\141\0\uffff\uffff\142\0\301" +
		"\0\300\0\140\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4154,
		"\1\0\uffff\uffff\2\0\uffff\uffff\21\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\47\0\uffff\uffff\54\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\22\0\366\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff\0\0\72\0\6\0\72\0\7\0" +
		"\72\0\10\0\72\0\15\0\72\0\16\0\72\0\17\0\72\0\22\0\72\0\23\0\72\0\24\0\72\0\25\0" +
		"\72\0\26\0\72\0\32\0\72\0\33\0\72\0\35\0\72\0\40\0\72\0\42\0\72\0\43\0\72\0\44\0" +
		"\72\0\45\0\72\0\46\0\72\0\52\0\72\0\53\0\72\0\55\0\72\0\56\0\72\0\57\0\72\0\60\0" +
		"\72\0\61\0\72\0\62\0\72\0\63\0\72\0\64\0\72\0\65\0\72\0\66\0\72\0\67\0\72\0\70\0" +
		"\72\0\71\0\72\0\72\0\72\0\73\0\72\0\74\0\72\0\75\0\72\0\76\0\72\0\77\0\72\0\100\0" +
		"\72\0\101\0\72\0\102\0\72\0\103\0\72\0\104\0\72\0\105\0\72\0\106\0\72\0\107\0\72" +
		"\0\110\0\72\0\111\0\72\0\112\0\72\0\113\0\72\0\114\0\72\0\115\0\72\0\uffff\uffff" +
		"\ufffe\uffff\16\0\uffff\uffff\15\0\50\0\23\0\50\0\26\0\50\0\uffff\uffff\ufffe\uffff" +
		"\51\0\uffff\uffff\7\0\60\0\44\0\60\0\45\0\60\0\55\0\60\0\56\0\60\0\57\0\60\0\60\0" +
		"\60\0\61\0\60\0\62\0\60\0\63\0\60\0\64\0\60\0\65\0\60\0\66\0\60\0\67\0\60\0\70\0" +
		"\60\0\71\0\60\0\72\0\60\0\73\0\60\0\74\0\60\0\75\0\60\0\76\0\60\0\77\0\60\0\100\0" +
		"\60\0\101\0\60\0\102\0\60\0\103\0\60\0\104\0\60\0\105\0\60\0\106\0\60\0\107\0\60" +
		"\0\110\0\60\0\111\0\60\0\112\0\60\0\113\0\60\0\114\0\60\0\uffff\uffff\ufffe\uffff" +
		"\17\0\uffff\uffff\22\0\365\0\uffff\uffff\ufffe\uffff\33\0\uffff\uffff\37\0\uffff" +
		"\uffff\45\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\31" +
		"\0\336\0\uffff\uffff\ufffe\uffff\20\0\uffff\uffff\17\0\343\0\31\0\343\0\uffff\uffff" +
		"\ufffe\uffff\17\0\uffff\uffff\31\0\335\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff" +
		"\0\0\56\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\115\0\uffff\uffff\20\0\370\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\0\0\63\0\7\0\63\0\uffff" +
		"\uffff\ufffe\uffff\115\0\uffff\uffff\20\0\370\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff" +
		"\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff" +
		"\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff" +
		"\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff" +
		"\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102" +
		"\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff" +
		"\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113" +
		"\0\uffff\uffff\114\0\uffff\uffff\0\0\64\0\uffff\uffff\ufffe\uffff\43\0\uffff\uffff" +
		"\23\0\320\0\42\0\320\0\45\0\320\0\53\0\320\0\55\0\320\0\56\0\320\0\57\0\320\0\60" +
		"\0\320\0\61\0\320\0\62\0\320\0\63\0\320\0\64\0\320\0\65\0\320\0\66\0\320\0\67\0\320" +
		"\0\70\0\320\0\71\0\320\0\72\0\320\0\73\0\320\0\74\0\320\0\75\0\320\0\76\0\320\0\77" +
		"\0\320\0\100\0\320\0\101\0\320\0\102\0\320\0\103\0\320\0\104\0\320\0\105\0\320\0" +
		"\106\0\320\0\107\0\320\0\110\0\320\0\111\0\320\0\112\0\320\0\113\0\320\0\114\0\320" +
		"\0\uffff\uffff\ufffe\uffff\117\0\uffff\uffff\0\0\116\0\6\0\116\0\7\0\116\0\27\0\116" +
		"\0\30\0\116\0\44\0\116\0\45\0\116\0\55\0\116\0\56\0\116\0\57\0\116\0\60\0\116\0\61" +
		"\0\116\0\62\0\116\0\63\0\116\0\64\0\116\0\65\0\116\0\66\0\116\0\67\0\116\0\70\0\116" +
		"\0\71\0\116\0\72\0\116\0\73\0\116\0\74\0\116\0\75\0\116\0\76\0\116\0\77\0\116\0\100" +
		"\0\116\0\101\0\116\0\102\0\116\0\103\0\116\0\104\0\116\0\105\0\116\0\106\0\116\0" +
		"\107\0\116\0\110\0\116\0\111\0\116\0\112\0\116\0\113\0\116\0\114\0\116\0\uffff\uffff" +
		"\ufffe\uffff\12\0\uffff\uffff\23\0\322\0\42\0\322\0\43\0\322\0\45\0\322\0\53\0\322" +
		"\0\55\0\322\0\56\0\322\0\57\0\322\0\60\0\322\0\61\0\322\0\62\0\322\0\63\0\322\0\64" +
		"\0\322\0\65\0\322\0\66\0\322\0\67\0\322\0\70\0\322\0\71\0\322\0\72\0\322\0\73\0\322" +
		"\0\74\0\322\0\75\0\322\0\76\0\322\0\77\0\322\0\100\0\322\0\101\0\322\0\102\0\322" +
		"\0\103\0\322\0\104\0\322\0\105\0\322\0\106\0\322\0\107\0\322\0\110\0\322\0\111\0" +
		"\322\0\112\0\322\0\113\0\322\0\114\0\322\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102" +
		"\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\50\0\uffff\uffff\20\0\u0100\0\25\0\u0100" +
		"\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0\0\372\0\6\0\372\0\7\0\372\0\23\0\372" +
		"\0\27\0\372\0\30\0\372\0\44\0\372\0\45\0\372\0\55\0\372\0\56\0\372\0\57\0\372\0\60" +
		"\0\372\0\61\0\372\0\62\0\372\0\63\0\372\0\64\0\372\0\65\0\372\0\66\0\372\0\67\0\372" +
		"\0\70\0\372\0\71\0\372\0\72\0\372\0\73\0\372\0\74\0\372\0\75\0\372\0\76\0\372\0\77" +
		"\0\372\0\100\0\372\0\101\0\372\0\102\0\372\0\103\0\372\0\104\0\372\0\105\0\372\0" +
		"\106\0\372\0\107\0\372\0\110\0\372\0\111\0\372\0\112\0\372\0\113\0\372\0\114\0\372" +
		"\0\115\0\372\0\uffff\uffff\ufffe\uffff\117\0\uffff\uffff\0\0\114\0\6\0\114\0\7\0" +
		"\114\0\27\0\114\0\30\0\114\0\44\0\114\0\45\0\114\0\55\0\114\0\56\0\114\0\57\0\114" +
		"\0\60\0\114\0\61\0\114\0\62\0\114\0\63\0\114\0\64\0\114\0\65\0\114\0\66\0\114\0\67" +
		"\0\114\0\70\0\114\0\71\0\114\0\72\0\114\0\73\0\114\0\74\0\114\0\75\0\114\0\76\0\114" +
		"\0\77\0\114\0\100\0\114\0\101\0\114\0\102\0\114\0\103\0\114\0\104\0\114\0\105\0\114" +
		"\0\106\0\114\0\107\0\114\0\110\0\114\0\111\0\114\0\112\0\114\0\113\0\114\0\114\0" +
		"\114\0\uffff\uffff\ufffe\uffff\100\0\uffff\uffff\15\0\216\0\17\0\216\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0102\0\25\0\u0102\0\26\0\u0102\0\uffff\uffff\ufffe\uffff\12\0\uffff" +
		"\uffff\30\0\uffff\uffff\34\0\uffff\uffff\6\0\72\0\10\0\72\0\15\0\72\0\16\0\72\0\23" +
		"\0\72\0\24\0\72\0\25\0\72\0\26\0\72\0\32\0\72\0\33\0\72\0\35\0\72\0\42\0\72\0\43" +
		"\0\72\0\44\0\72\0\45\0\72\0\46\0\72\0\52\0\72\0\53\0\72\0\55\0\72\0\56\0\72\0\57" +
		"\0\72\0\60\0\72\0\61\0\72\0\62\0\72\0\63\0\72\0\64\0\72\0\65\0\72\0\66\0\72\0\67" +
		"\0\72\0\70\0\72\0\71\0\72\0\72\0\72\0\73\0\72\0\74\0\72\0\75\0\72\0\76\0\72\0\77" +
		"\0\72\0\100\0\72\0\101\0\72\0\102\0\72\0\103\0\72\0\104\0\72\0\105\0\72\0\106\0\72" +
		"\0\107\0\72\0\110\0\72\0\111\0\72\0\112\0\72\0\113\0\72\0\114\0\72\0\115\0\72\0\uffff" +
		"\uffff\ufffe\uffff\10\0\uffff\uffff\15\0\225\0\26\0\225\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0" +
		"\26\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115" +
		"\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\26\0\u0102\0\uffff\uffff\ufffe" +
		"\uffff\35\0\uffff\uffff\6\0\271\0\10\0\271\0\15\0\271\0\16\0\271\0\23\0\271\0\24" +
		"\0\271\0\25\0\271\0\26\0\271\0\42\0\271\0\43\0\271\0\44\0\271\0\45\0\271\0\52\0\271" +
		"\0\53\0\271\0\55\0\271\0\56\0\271\0\57\0\271\0\60\0\271\0\61\0\271\0\62\0\271\0\63" +
		"\0\271\0\64\0\271\0\65\0\271\0\66\0\271\0\67\0\271\0\70\0\271\0\71\0\271\0\72\0\271" +
		"\0\73\0\271\0\74\0\271\0\75\0\271\0\76\0\271\0\77\0\271\0\100\0\271\0\101\0\271\0" +
		"\102\0\271\0\103\0\271\0\104\0\271\0\105\0\271\0\106\0\271\0\107\0\271\0\110\0\271" +
		"\0\111\0\271\0\112\0\271\0\113\0\271\0\114\0\271\0\115\0\271\0\uffff\uffff\ufffe" +
		"\uffff\32\0\uffff\uffff\33\0\uffff\uffff\46\0\uffff\uffff\6\0\273\0\10\0\273\0\15" +
		"\0\273\0\16\0\273\0\23\0\273\0\24\0\273\0\25\0\273\0\26\0\273\0\35\0\273\0\42\0\273" +
		"\0\43\0\273\0\44\0\273\0\45\0\273\0\52\0\273\0\53\0\273\0\55\0\273\0\56\0\273\0\57" +
		"\0\273\0\60\0\273\0\61\0\273\0\62\0\273\0\63\0\273\0\64\0\273\0\65\0\273\0\66\0\273" +
		"\0\67\0\273\0\70\0\273\0\71\0\273\0\72\0\273\0\73\0\273\0\74\0\273\0\75\0\273\0\76" +
		"\0\273\0\77\0\273\0\100\0\273\0\101\0\273\0\102\0\273\0\103\0\273\0\104\0\273\0\105" +
		"\0\273\0\106\0\273\0\107\0\273\0\110\0\273\0\111\0\273\0\112\0\273\0\113\0\273\0" +
		"\114\0\273\0\115\0\273\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\235\0\15\0" +
		"\235\0\26\0\235\0\uffff\uffff\ufffe\uffff\46\0\uffff\uffff\120\0\uffff\uffff\10\0" +
		"\245\0\15\0\245\0\20\0\245\0\26\0\245\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff" +
		"\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\17\0\332\0\31\0\332" +
		"\0\uffff\uffff\ufffe\uffff\50\0\uffff\uffff\20\0\u0100\0\25\0\u0100\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102" +
		"\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\23\0\uffff" +
		"\uffff\0\0\374\0\6\0\374\0\7\0\374\0\27\0\374\0\30\0\374\0\44\0\374\0\45\0\374\0" +
		"\55\0\374\0\56\0\374\0\57\0\374\0\60\0\374\0\61\0\374\0\62\0\374\0\63\0\374\0\64" +
		"\0\374\0\65\0\374\0\66\0\374\0\67\0\374\0\70\0\374\0\71\0\374\0\72\0\374\0\73\0\374" +
		"\0\74\0\374\0\75\0\374\0\76\0\374\0\77\0\374\0\100\0\374\0\101\0\374\0\102\0\374" +
		"\0\103\0\374\0\104\0\374\0\105\0\374\0\106\0\374\0\107\0\374\0\110\0\374\0\111\0" +
		"\374\0\112\0\374\0\113\0\374\0\114\0\374\0\115\0\374\0\uffff\uffff\ufffe\uffff\2" +
		"\0\uffff\uffff\0\0\372\0\6\0\372\0\7\0\372\0\23\0\372\0\27\0\372\0\30\0\372\0\44" +
		"\0\372\0\45\0\372\0\55\0\372\0\56\0\372\0\57\0\372\0\60\0\372\0\61\0\372\0\62\0\372" +
		"\0\63\0\372\0\64\0\372\0\65\0\372\0\66\0\372\0\67\0\372\0\70\0\372\0\71\0\372\0\72" +
		"\0\372\0\73\0\372\0\74\0\372\0\75\0\372\0\76\0\372\0\77\0\372\0\100\0\372\0\101\0" +
		"\372\0\102\0\372\0\103\0\372\0\104\0\372\0\105\0\372\0\106\0\372\0\107\0\372\0\110" +
		"\0\372\0\111\0\372\0\112\0\372\0\113\0\372\0\114\0\372\0\115\0\372\0\uffff\uffff" +
		"\ufffe\uffff\13\0\uffff\uffff\14\0\uffff\uffff\11\0\351\0\22\0\351\0\41\0\351\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\52\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0102\0\25\0\u0102\0\26\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\25\0\u0102" +
		"\0\26\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\26\0\u0102" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0" +
		"\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\26\0\u0102\0\uffff\uffff\ufffe\uffff" +
		"\25\0\uffff\uffff\10\0\231\0\15\0\231\0\26\0\231\0\uffff\uffff\ufffe\uffff\25\0\uffff" +
		"\uffff\10\0\233\0\15\0\233\0\26\0\233\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff" +
		"\17\0\331\0\31\0\331\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\20\0\166\0\25\0\166" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0" +
		"\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102" +
		"\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102" +
		"\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\0\0\376\0\6\0\376\0\7\0" +
		"\376\0\27\0\376\0\30\0\376\0\44\0\376\0\45\0\376\0\55\0\376\0\56\0\376\0\57\0\376" +
		"\0\60\0\376\0\61\0\376\0\62\0\376\0\63\0\376\0\64\0\376\0\65\0\376\0\66\0\376\0\67" +
		"\0\376\0\70\0\376\0\71\0\376\0\72\0\376\0\73\0\376\0\74\0\376\0\75\0\376\0\76\0\376" +
		"\0\77\0\376\0\100\0\376\0\101\0\376\0\102\0\376\0\103\0\376\0\104\0\376\0\105\0\376" +
		"\0\106\0\376\0\107\0\376\0\110\0\376\0\111\0\376\0\112\0\376\0\113\0\376\0\114\0" +
		"\376\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0\374\0\6\0\374\0\7\0\374\0\27" +
		"\0\374\0\30\0\374\0\44\0\374\0\45\0\374\0\55\0\374\0\56\0\374\0\57\0\374\0\60\0\374" +
		"\0\61\0\374\0\62\0\374\0\63\0\374\0\64\0\374\0\65\0\374\0\66\0\374\0\67\0\374\0\70" +
		"\0\374\0\71\0\374\0\72\0\374\0\73\0\374\0\74\0\374\0\75\0\374\0\76\0\374\0\77\0\374" +
		"\0\100\0\374\0\101\0\374\0\102\0\374\0\103\0\374\0\104\0\374\0\105\0\374\0\106\0" +
		"\374\0\107\0\374\0\110\0\374\0\111\0\374\0\112\0\374\0\113\0\374\0\114\0\374\0\115" +
		"\0\374\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\72\0\26\0\72\0\40\0\72\0\uffff\uffff" +
		"\ufffe\uffff\25\0\uffff\uffff\10\0\227\0\15\0\227\0\26\0\227\0\uffff\uffff\ufffe" +
		"\uffff\17\0\uffff\uffff\46\0\uffff\uffff\10\0\243\0\15\0\243\0\20\0\243\0\26\0\243" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0" +
		"\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102" +
		"\0\25\0\u0102\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0\uffff\uffff" +
		"\ufffe\uffff\115\0\uffff\uffff\0\0\376\0\6\0\376\0\7\0\376\0\27\0\376\0\30\0\376" +
		"\0\44\0\376\0\45\0\376\0\55\0\376\0\56\0\376\0\57\0\376\0\60\0\376\0\61\0\376\0\62" +
		"\0\376\0\63\0\376\0\64\0\376\0\65\0\376\0\66\0\376\0\67\0\376\0\70\0\376\0\71\0\376" +
		"\0\72\0\376\0\73\0\376\0\74\0\376\0\75\0\376\0\76\0\376\0\77\0\376\0\100\0\376\0" +
		"\101\0\376\0\102\0\376\0\103\0\376\0\104\0\376\0\105\0\376\0\106\0\376\0\107\0\376" +
		"\0\110\0\376\0\111\0\376\0\112\0\376\0\113\0\376\0\114\0\376\0\uffff\uffff\ufffe" +
		"\uffff\11\0\356\0\41\0\uffff\uffff\22\0\356\0\uffff\uffff\ufffe\uffff\11\0\355\0" +
		"\41\0\355\0\22\0\355\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0102\0\15\0\u0102\0\25\0\u0102\0" +
		"\uffff\uffff\ufffe\uffff\10\0\314\0\40\0\uffff\uffff\26\0\314\0\uffff\uffff\ufffe" +
		"\uffff\10\0\315\0\40\0\315\0\26\0\315\0\uffff\uffff\ufffe\uffff");

	private static final int[] tmGoto = TMLexer.unpack_int(173,
		"\0\0\4\0\40\0\100\0\100\0\100\0\100\0\172\0\176\0\210\0\216\0\236\0\240\0\242\0\350" +
		"\0\u0118\0\u012c\0\u0152\0\u0182\0\u0186\0\u01ce\0\u01fc\0\u0214\0\u0224\0\u0226" +
		"\0\u0238\0\u0240\0\u0246\0\u024e\0\u0250\0\u0252\0\u025c\0\u026a\0\u0274\0\u027a" +
		"\0\u02ae\0\u02e2\0\u0322\0\u03e4\0\u03ea\0\u0402\0\u0406\0\u0408\0\u040a\0\u0444" +
		"\0\u045c\0\u0520\0\u05e4\0\u06b2\0\u0776\0\u083a\0\u08fe\0\u09c2\0\u0a88\0\u0b4c" +
		"\0\u0c10\0\u0cda\0\u0d9e\0\u0e6a\0\u0f2c\0\u0ff0\0\u10b4\0\u1178\0\u123c\0\u1300" +
		"\0\u13c4\0\u1488\0\u154c\0\u1612\0\u16d6\0\u179a\0\u1864\0\u1928\0\u19ec\0\u1ab0" +
		"\0\u1b74\0\u1c3e\0\u1d02\0\u1d40\0\u1d42\0\u1d48\0\u1d4a\0\u1e0c\0\u1e24\0\u1e2a" +
		"\0\u1e2e\0\u1e32\0\u1e64\0\u1ea4\0\u1ea6\0\u1ea8\0\u1eaa\0\u1eac\0\u1eae\0\u1eb0" +
		"\0\u1eb2\0\u1eb4\0\u1f00\0\u1f28\0\u1f34\0\u1f38\0\u1f40\0\u1f48\0\u1f50\0\u1f58" +
		"\0\u1f5a\0\u1f62\0\u1f66\0\u1f68\0\u1f70\0\u1f74\0\u1f7c\0\u1f80\0\u1f86\0\u1f88" +
		"\0\u1f8c\0\u1f90\0\u1f98\0\u1f9c\0\u1f9e\0\u1fa0\0\u1fa4\0\u1fa8\0\u1fac\0\u1fae" +
		"\0\u1fb2\0\u1fb6\0\u1fb8\0\u1fdc\0\u2000\0\u2026\0\u204c\0\u207a\0\u2092\0\u2096" +
		"\0\u20be\0\u20ec\0\u20ee\0\u211c\0\u2120\0\u214e\0\u217c\0\u21ac\0\u21e0\0\u2214" +
		"\0\u2248\0\u2282\0\u228c\0\u2294\0\u22c6\0\u22f8\0\u232c\0\u232e\0\u2332\0\u2336" +
		"\0\u234a\0\u234c\0\u234e\0\u2354\0\u2358\0\u235c\0\u2364\0\u236a\0\u2370\0\u237a" +
		"\0\u237c\0\u237e\0\u2382\0\u2386\0\u238a\0\u238e\0\u2392\0\u23c0\0");

	private static final int[] tmFromTo = TMLexer.unpack_int(9152,
		"\u01be\0\u01c0\0\u01bf\0\u01c1\0\1\0\4\0\6\0\4\0\73\0\111\0\76\0\4\0\112\0\130\0" +
		"\123\0\4\0\132\0\4\0\321\0\4\0\u0115\0\4\0\u0133\0\4\0\u0156\0\4\0\u015c\0\4\0\u015d" +
		"\0\4\0\u0177\0\4\0\1\0\5\0\6\0\5\0\76\0\5\0\123\0\5\0\132\0\5\0\231\0\305\0\232\0" +
		"\306\0\300\0\u0105\0\321\0\5\0\u0107\0\u0105\0\u0115\0\5\0\u0133\0\5\0\u0156\0\5" +
		"\0\u015c\0\5\0\u015d\0\5\0\u0177\0\5\0\131\0\144\0\147\0\144\0\160\0\200\0\176\0" +
		"\144\0\203\0\200\0\226\0\144\0\253\0\322\0\325\0\322\0\341\0\322\0\343\0\322\0\371" +
		"\0\322\0\373\0\322\0\374\0\322\0\377\0\322\0\u0120\0\322\0\u0125\0\322\0\u0129\0" +
		"\322\0\u012b\0\322\0\u0140\0\322\0\u0143\0\322\0\u0145\0\322\0\u0147\0\322\0\u0149" +
		"\0\322\0\u014a\0\322\0\u017f\0\322\0\u0183\0\322\0\u0186\0\322\0\u0188\0\322\0\u01ad" +
		"\0\322\0\74\0\113\0\116\0\133\0\336\0\u0129\0\u016c\0\u019d\0\u019a\0\u019d\0\u01b4" +
		"\0\u019d\0\u01b5\0\u019d\0\u011e\0\u015e\0\u0195\0\u015e\0\u0196\0\u015e\0\114\0" +
		"\132\0\146\0\170\0\251\0\321\0\307\0\u010c\0\320\0\u0115\0\332\0\u0127\0\u0114\0" +
		"\u0156\0\u0139\0\u0177\0\u011c\0\u015c\0\u011c\0\u015d\0\61\0\71\0\111\0\127\0\126" +
		"\0\142\0\130\0\143\0\215\0\274\0\217\0\276\0\273\0\u0103\0\305\0\u010a\0\306\0\u010b" +
		"\0\311\0\u010e\0\314\0\u0110\0\316\0\u0112\0\320\0\u0116\0\337\0\u012a\0\u0108\0" +
		"\u0151\0\u0109\0\u0152\0\u0114\0\u0157\0\u013f\0\u017a\0\u0141\0\u017c\0\u0142\0" +
		"\u017d\0\u0146\0\u0181\0\u0153\0\u0190\0\u0158\0\u0192\0\u017b\0\u01a4\0\u017e\0" +
		"\u01a5\0\u0180\0\u01a7\0\u0182\0\u01a8\0\u0184\0\u01aa\0\u0185\0\u01ab\0\u0191\0" +
		"\u01b1\0\u01a6\0\u01b6\0\u01a9\0\u01b7\0\u01ac\0\u01b8\0\u01ae\0\u01ba\0\u01b9\0" +
		"\u01bd\0\60\0\70\0\253\0\323\0\325\0\323\0\341\0\323\0\343\0\323\0\371\0\323\0\373" +
		"\0\323\0\374\0\323\0\377\0\323\0\u0120\0\323\0\u0125\0\323\0\u0129\0\323\0\u012b" +
		"\0\323\0\u0140\0\323\0\u0143\0\323\0\u0145\0\323\0\u0147\0\323\0\u0149\0\323\0\u014a" +
		"\0\323\0\u017f\0\323\0\u0183\0\323\0\u0186\0\323\0\u0188\0\323\0\u01ad\0\323\0\64" +
		"\0\76\0\104\0\124\0\166\0\221\0\215\0\275\0\217\0\275\0\311\0\u010f\0\314\0\u0111" +
		"\0\361\0\u013a\0\u013d\0\u0179\0\u0176\0\u0111\0\103\0\123\0\173\0\225\0\202\0\253" +
		"\0\227\0\302\0\264\0\371\0\265\0\373\0\266\0\374\0\272\0\377\0\372\0\u0140\0\375" +
		"\0\u0143\0\376\0\u0145\0\u0100\0\u0147\0\u0101\0\u0149\0\u0102\0\u014a\0\u0144\0" +
		"\u017f\0\u0148\0\u0183\0\u014b\0\u0186\0\u014c\0\u0188\0\u0187\0\u01ad\0\1\0\6\0" +
		"\6\0\6\0\76\0\6\0\132\0\6\0\253\0\324\0\321\0\6\0\325\0\324\0\371\0\324\0\373\0\324" +
		"\0\374\0\324\0\377\0\324\0\u0125\0\324\0\u0129\0\324\0\u0140\0\324\0\u0143\0\324" +
		"\0\u0145\0\324\0\u0147\0\324\0\u0149\0\324\0\u014a\0\324\0\u017f\0\324\0\u0183\0" +
		"\324\0\u0186\0\324\0\u0188\0\324\0\u01ad\0\324\0\65\0\77\0\u011e\0\u015f\0\61\0\72" +
		"\0\253\0\325\0\325\0\325\0\327\0\u0125\0\330\0\u0126\0\341\0\325\0\343\0\325\0\355" +
		"\0\325\0\371\0\325\0\373\0\325\0\374\0\325\0\377\0\325\0\u0106\0\u014d\0\u0120\0" +
		"\325\0\u0125\0\325\0\u0126\0\u0167\0\u0127\0\325\0\u0128\0\325\0\u0129\0\325\0\u012b" +
		"\0\325\0\u0140\0\325\0\u0143\0\325\0\u0145\0\325\0\u0147\0\325\0\u0149\0\325\0\u014a" +
		"\0\325\0\u0150\0\u014d\0\u0167\0\u0167\0\u0168\0\u0167\0\u017f\0\325\0\u0183\0\325" +
		"\0\u0186\0\325\0\u0188\0\325\0\u019d\0\u0167\0\u019f\0\u0167\0\u01ad\0\325\0\253" +
		"\0\326\0\325\0\326\0\341\0\326\0\343\0\326\0\371\0\326\0\373\0\326\0\374\0\326\0" +
		"\377\0\326\0\u0120\0\326\0\u0125\0\326\0\u0129\0\326\0\u012b\0\326\0\u0140\0\326" +
		"\0\u0143\0\326\0\u0145\0\326\0\u0147\0\326\0\u0149\0\326\0\u014a\0\326\0\u017f\0" +
		"\326\0\u0183\0\326\0\u0186\0\326\0\u0188\0\326\0\u01ad\0\326\0\202\0\254\0\264\0" +
		"\254\0\266\0\254\0\272\0\254\0\356\0\254\0\375\0\254\0\u0100\0\254\0\u0102\0\254" +
		"\0\u012c\0\254\0\u012f\0\254\0\u014b\0\254\0\u0170\0\254\0\110\0\126\0\u011f\0\u0161" +
		"\0\u0123\0\u0164\0\u0166\0\u0199\0\u016c\0\u019e\0\u018c\0\u01af\0\u0197\0\u01b2" +
		"\0\u019a\0\u01b3\0\226\0\301\0\53\0\66\0\131\0\145\0\147\0\145\0\176\0\145\0\202" +
		"\0\255\0\226\0\145\0\272\0\255\0\332\0\66\0\u0169\0\66\0\105\0\125\0\164\0\220\0" +
		"\166\0\222\0\361\0\u013b\0\145\0\164\0\353\0\u0131\0\u01b2\0\u01bb\0\66\0\100\0\124" +
		"\0\100\0\353\0\u0132\0\u01b2\0\u01bc\0\332\0\u0128\0\352\0\u0130\0\324\0\u011b\0" +
		"\326\0\u0121\0\u015e\0\u011b\0\u0160\0\u011b\0\u0165\0\u0121\0\66\0\101\0\124\0\101" +
		"\0\u0126\0\u0168\0\u0167\0\u0168\0\u0168\0\u0168\0\u019d\0\u0168\0\u019f\0\u0168" +
		"\0\u0123\0\u0165\0\u016c\0\u019f\0\u019a\0\u019f\0\u01b4\0\u019f\0\u01b5\0\u019f" +
		"\0\u011e\0\u0160\0\u0195\0\u0160\0\u0196\0\u0160\0\253\0\327\0\325\0\327\0\341\0" +
		"\327\0\343\0\327\0\355\0\327\0\371\0\327\0\373\0\327\0\374\0\327\0\377\0\327\0\u0120" +
		"\0\327\0\u0125\0\327\0\u0127\0\327\0\u0128\0\327\0\u0129\0\327\0\u012b\0\327\0\u0140" +
		"\0\327\0\u0143\0\327\0\u0145\0\327\0\u0147\0\327\0\u0149\0\327\0\u014a\0\327\0\u017f" +
		"\0\327\0\u0183\0\327\0\u0186\0\327\0\u0188\0\327\0\u01ad\0\327\0\160\0\201\0\203" +
		"\0\201\0\210\0\201\0\253\0\201\0\325\0\201\0\341\0\201\0\343\0\201\0\371\0\201\0" +
		"\373\0\201\0\374\0\201\0\377\0\201\0\u0120\0\201\0\u0125\0\201\0\u0129\0\201\0\u012b" +
		"\0\201\0\u0140\0\201\0\u0143\0\201\0\u0145\0\201\0\u0147\0\201\0\u0149\0\201\0\u014a" +
		"\0\201\0\u017f\0\201\0\u0183\0\201\0\u0186\0\201\0\u0188\0\201\0\u01ad\0\201\0\1" +
		"\0\7\0\6\0\7\0\74\0\7\0\76\0\7\0\132\0\7\0\147\0\7\0\201\0\7\0\203\0\7\0\226\0\7" +
		"\0\253\0\7\0\321\0\7\0\325\0\7\0\343\0\7\0\371\0\7\0\373\0\7\0\374\0\7\0\377\0\7" +
		"\0\u0120\0\7\0\u0125\0\7\0\u0129\0\7\0\u012b\0\7\0\u0140\0\7\0\u0143\0\7\0\u0145" +
		"\0\7\0\u0147\0\7\0\u0149\0\7\0\u014a\0\7\0\u017f\0\7\0\u0183\0\7\0\u0186\0\7\0\u0188" +
		"\0\7\0\u01ad\0\7\0\1\0\10\0\2\0\10\0\6\0\10\0\66\0\10\0\70\0\10\0\72\0\10\0\73\0" +
		"\10\0\74\0\10\0\76\0\10\0\100\0\10\0\101\0\10\0\123\0\10\0\124\0\10\0\131\0\10\0" +
		"\132\0\10\0\145\0\10\0\147\0\10\0\153\0\10\0\160\0\10\0\161\0\10\0\162\0\10\0\163" +
		"\0\10\0\176\0\10\0\201\0\10\0\203\0\10\0\211\0\10\0\213\0\10\0\221\0\10\0\226\0\10" +
		"\0\235\0\10\0\237\0\10\0\240\0\10\0\246\0\10\0\250\0\10\0\253\0\10\0\254\0\10\0\255" +
		"\0\10\0\261\0\10\0\275\0\10\0\316\0\10\0\317\0\10\0\321\0\10\0\323\0\10\0\324\0\10" +
		"\0\325\0\10\0\326\0\10\0\341\0\10\0\343\0\10\0\355\0\10\0\360\0\10\0\364\0\10\0\371" +
		"\0\10\0\373\0\10\0\374\0\10\0\377\0\10\0\u010f\0\10\0\u0111\0\10\0\u0115\0\10\0\u0118" +
		"\0\10\0\u0119\0\10\0\u011b\0\10\0\u0120\0\10\0\u0121\0\10\0\u0125\0\10\0\u0126\0" +
		"\10\0\u0127\0\10\0\u0128\0\10\0\u0129\0\10\0\u012b\0\10\0\u0133\0\10\0\u0136\0\10" +
		"\0\u0137\0\10\0\u013a\0\10\0\u0140\0\10\0\u0143\0\10\0\u0145\0\10\0\u0147\0\10\0" +
		"\u0149\0\10\0\u014a\0\10\0\u0156\0\10\0\u015e\0\10\0\u0160\0\10\0\u0162\0\10\0\u0165" +
		"\0\10\0\u0167\0\10\0\u0168\0\10\0\u0169\0\10\0\u0177\0\10\0\u0179\0\10\0\u017f\0" +
		"\10\0\u0183\0\10\0\u0186\0\10\0\u0188\0\10\0\u0197\0\10\0\u019d\0\10\0\u019f\0\10" +
		"\0\u01ad\0\10\0\353\0\u0133\0\357\0\u0136\0\u0176\0\u0136\0\1\0\11\0\6\0\11\0\76" +
		"\0\11\0\123\0\11\0\132\0\11\0\321\0\11\0\u0115\0\11\0\u0133\0\11\0\u0156\0\11\0\u015c" +
		"\0\11\0\u015d\0\11\0\u0177\0\11\0\256\0\364\0\367\0\364\0\62\0\73\0\u0120\0\u0162" +
		"\0\253\0\330\0\303\0\330\0\304\0\330\0\325\0\330\0\341\0\330\0\343\0\330\0\355\0" +
		"\330\0\371\0\330\0\373\0\330\0\374\0\330\0\377\0\330\0\u010c\0\330\0\u0120\0\330" +
		"\0\u0125\0\330\0\u0127\0\330\0\u0128\0\330\0\u0129\0\330\0\u012b\0\330\0\u0140\0" +
		"\330\0\u0143\0\330\0\u0145\0\330\0\u0147\0\330\0\u0149\0\330\0\u014a\0\330\0\u017f" +
		"\0\330\0\u0183\0\330\0\u0186\0\330\0\u0188\0\330\0\u01ad\0\330\0\1\0\12\0\6\0\12" +
		"\0\76\0\12\0\123\0\12\0\132\0\12\0\321\0\12\0\u0115\0\12\0\u0133\0\12\0\u0156\0\12" +
		"\0\u015c\0\12\0\u015d\0\12\0\u0177\0\12\0\1\0\13\0\2\0\13\0\6\0\13\0\66\0\13\0\70" +
		"\0\13\0\72\0\13\0\73\0\13\0\74\0\13\0\76\0\13\0\100\0\13\0\101\0\13\0\123\0\13\0" +
		"\124\0\13\0\131\0\13\0\132\0\13\0\145\0\13\0\147\0\13\0\153\0\13\0\160\0\13\0\161" +
		"\0\13\0\162\0\13\0\163\0\13\0\176\0\13\0\200\0\230\0\201\0\13\0\203\0\13\0\211\0" +
		"\13\0\213\0\13\0\221\0\13\0\226\0\13\0\235\0\13\0\237\0\13\0\240\0\13\0\246\0\13" +
		"\0\250\0\13\0\253\0\13\0\254\0\13\0\255\0\13\0\261\0\13\0\275\0\13\0\316\0\13\0\317" +
		"\0\13\0\321\0\13\0\323\0\13\0\324\0\13\0\325\0\13\0\326\0\13\0\341\0\13\0\343\0\13" +
		"\0\355\0\13\0\360\0\13\0\364\0\13\0\371\0\13\0\373\0\13\0\374\0\13\0\377\0\13\0\u010f" +
		"\0\13\0\u0111\0\13\0\u0115\0\13\0\u0118\0\13\0\u0119\0\13\0\u011b\0\13\0\u0120\0" +
		"\13\0\u0121\0\13\0\u0125\0\13\0\u0126\0\13\0\u0127\0\13\0\u0128\0\13\0\u0129\0\13" +
		"\0\u012b\0\13\0\u0133\0\13\0\u0136\0\13\0\u0137\0\13\0\u013a\0\13\0\u0140\0\13\0" +
		"\u0143\0\13\0\u0145\0\13\0\u0147\0\13\0\u0149\0\13\0\u014a\0\13\0\u0156\0\13\0\u015e" +
		"\0\13\0\u0160\0\13\0\u0162\0\13\0\u0165\0\13\0\u0167\0\13\0\u0168\0\13\0\u0169\0" +
		"\13\0\u0177\0\13\0\u0179\0\13\0\u017f\0\13\0\u0183\0\13\0\u0186\0\13\0\u0188\0\13" +
		"\0\u0197\0\13\0\u019d\0\13\0\u019f\0\13\0\u01ad\0\13\0\1\0\14\0\2\0\14\0\6\0\14\0" +
		"\66\0\14\0\70\0\14\0\72\0\14\0\73\0\14\0\74\0\14\0\76\0\14\0\100\0\14\0\101\0\14" +
		"\0\123\0\14\0\124\0\14\0\131\0\14\0\132\0\14\0\144\0\161\0\145\0\14\0\147\0\14\0" +
		"\153\0\14\0\160\0\14\0\161\0\14\0\162\0\14\0\163\0\14\0\176\0\14\0\201\0\14\0\203" +
		"\0\14\0\211\0\14\0\213\0\14\0\221\0\14\0\226\0\14\0\235\0\14\0\237\0\14\0\240\0\14" +
		"\0\246\0\14\0\250\0\14\0\253\0\14\0\254\0\14\0\255\0\14\0\261\0\14\0\275\0\14\0\316" +
		"\0\14\0\317\0\14\0\321\0\14\0\323\0\14\0\324\0\14\0\325\0\14\0\326\0\14\0\341\0\14" +
		"\0\343\0\14\0\355\0\14\0\360\0\14\0\364\0\14\0\371\0\14\0\373\0\14\0\374\0\14\0\377" +
		"\0\14\0\u010f\0\14\0\u0111\0\14\0\u0115\0\14\0\u0118\0\14\0\u0119\0\14\0\u011b\0" +
		"\14\0\u0120\0\14\0\u0121\0\14\0\u0125\0\14\0\u0126\0\14\0\u0127\0\14\0\u0128\0\14" +
		"\0\u0129\0\14\0\u012b\0\14\0\u0133\0\14\0\u0136\0\14\0\u0137\0\14\0\u013a\0\14\0" +
		"\u0140\0\14\0\u0143\0\14\0\u0145\0\14\0\u0147\0\14\0\u0149\0\14\0\u014a\0\14\0\u0156" +
		"\0\14\0\u015e\0\14\0\u0160\0\14\0\u0162\0\14\0\u0165\0\14\0\u0167\0\14\0\u0168\0" +
		"\14\0\u0169\0\14\0\u0177\0\14\0\u0179\0\14\0\u017f\0\14\0\u0183\0\14\0\u0186\0\14" +
		"\0\u0188\0\14\0\u0197\0\14\0\u019d\0\14\0\u019f\0\14\0\u01ad\0\14\0\1\0\15\0\2\0" +
		"\15\0\6\0\15\0\66\0\15\0\70\0\15\0\72\0\15\0\73\0\15\0\74\0\15\0\76\0\15\0\100\0" +
		"\15\0\101\0\15\0\123\0\15\0\124\0\15\0\131\0\15\0\132\0\15\0\145\0\15\0\147\0\15" +
		"\0\153\0\15\0\160\0\15\0\161\0\15\0\162\0\15\0\163\0\15\0\176\0\15\0\201\0\15\0\202" +
		"\0\256\0\203\0\15\0\211\0\15\0\213\0\15\0\221\0\15\0\226\0\15\0\235\0\15\0\237\0" +
		"\15\0\240\0\15\0\246\0\15\0\250\0\15\0\253\0\15\0\254\0\15\0\255\0\15\0\257\0\367" +
		"\0\261\0\15\0\266\0\256\0\272\0\256\0\275\0\15\0\316\0\15\0\317\0\15\0\321\0\15\0" +
		"\323\0\15\0\324\0\15\0\325\0\15\0\326\0\15\0\341\0\15\0\343\0\15\0\355\0\15\0\360" +
		"\0\15\0\364\0\15\0\371\0\15\0\373\0\15\0\374\0\15\0\377\0\15\0\u0102\0\256\0\u010f" +
		"\0\15\0\u0111\0\15\0\u0115\0\15\0\u0118\0\15\0\u0119\0\15\0\u011b\0\15\0\u0120\0" +
		"\15\0\u0121\0\15\0\u0125\0\15\0\u0126\0\15\0\u0127\0\15\0\u0128\0\15\0\u0129\0\15" +
		"\0\u012b\0\15\0\u0133\0\15\0\u0136\0\15\0\u0137\0\15\0\u013a\0\15\0\u0140\0\15\0" +
		"\u0143\0\15\0\u0145\0\15\0\u0147\0\15\0\u0149\0\15\0\u014a\0\15\0\u014d\0\u0189\0" +
		"\u0156\0\15\0\u015e\0\15\0\u0160\0\15\0\u0162\0\15\0\u0165\0\15\0\u0167\0\15\0\u0168" +
		"\0\15\0\u0169\0\15\0\u0177\0\15\0\u0179\0\15\0\u017f\0\15\0\u0183\0\15\0\u0186\0" +
		"\15\0\u0188\0\15\0\u0197\0\15\0\u019d\0\15\0\u019f\0\15\0\u01ad\0\15\0\1\0\16\0\2" +
		"\0\16\0\6\0\16\0\66\0\16\0\70\0\16\0\72\0\16\0\73\0\16\0\74\0\16\0\76\0\16\0\100" +
		"\0\16\0\101\0\16\0\123\0\16\0\124\0\16\0\131\0\16\0\132\0\16\0\145\0\16\0\147\0\16" +
		"\0\153\0\16\0\160\0\16\0\161\0\16\0\162\0\16\0\163\0\16\0\176\0\16\0\201\0\16\0\203" +
		"\0\16\0\211\0\16\0\213\0\16\0\221\0\16\0\226\0\16\0\230\0\303\0\235\0\16\0\237\0" +
		"\16\0\240\0\16\0\246\0\16\0\250\0\16\0\253\0\16\0\254\0\16\0\255\0\16\0\261\0\16" +
		"\0\275\0\16\0\316\0\16\0\317\0\16\0\321\0\16\0\323\0\16\0\324\0\16\0\325\0\16\0\326" +
		"\0\16\0\341\0\16\0\343\0\16\0\355\0\16\0\360\0\16\0\364\0\16\0\371\0\16\0\373\0\16" +
		"\0\374\0\16\0\377\0\16\0\u010f\0\16\0\u0111\0\16\0\u0115\0\16\0\u0118\0\16\0\u0119" +
		"\0\16\0\u011b\0\16\0\u0120\0\16\0\u0121\0\16\0\u0125\0\16\0\u0126\0\16\0\u0127\0" +
		"\16\0\u0128\0\16\0\u0129\0\16\0\u012b\0\16\0\u0133\0\16\0\u0136\0\16\0\u0137\0\16" +
		"\0\u013a\0\16\0\u0140\0\16\0\u0143\0\16\0\u0145\0\16\0\u0147\0\16\0\u0149\0\16\0" +
		"\u014a\0\16\0\u0156\0\16\0\u015e\0\16\0\u0160\0\16\0\u0162\0\16\0\u0165\0\16\0\u0167" +
		"\0\16\0\u0168\0\16\0\u0169\0\16\0\u0177\0\16\0\u0179\0\16\0\u017f\0\16\0\u0183\0" +
		"\16\0\u0186\0\16\0\u0188\0\16\0\u0197\0\16\0\u019d\0\16\0\u019f\0\16\0\u01ad\0\16" +
		"\0\1\0\17\0\2\0\17\0\6\0\17\0\66\0\17\0\70\0\17\0\72\0\17\0\73\0\17\0\74\0\17\0\76" +
		"\0\17\0\100\0\17\0\101\0\17\0\123\0\17\0\124\0\17\0\131\0\17\0\132\0\17\0\145\0\17" +
		"\0\147\0\17\0\153\0\17\0\160\0\17\0\161\0\17\0\162\0\17\0\163\0\17\0\176\0\17\0\200" +
		"\0\231\0\201\0\17\0\203\0\17\0\211\0\17\0\213\0\17\0\221\0\17\0\226\0\17\0\235\0" +
		"\17\0\237\0\17\0\240\0\17\0\246\0\17\0\250\0\17\0\253\0\17\0\254\0\17\0\255\0\17" +
		"\0\261\0\17\0\275\0\17\0\316\0\17\0\317\0\17\0\321\0\17\0\323\0\17\0\324\0\17\0\325" +
		"\0\17\0\326\0\17\0\341\0\17\0\343\0\17\0\355\0\17\0\360\0\17\0\364\0\17\0\371\0\17" +
		"\0\373\0\17\0\374\0\17\0\377\0\17\0\u010f\0\17\0\u0111\0\17\0\u0115\0\17\0\u0118" +
		"\0\17\0\u0119\0\17\0\u011b\0\17\0\u0120\0\17\0\u0121\0\17\0\u0125\0\17\0\u0126\0" +
		"\17\0\u0127\0\17\0\u0128\0\17\0\u0129\0\17\0\u012b\0\17\0\u0133\0\17\0\u0136\0\17" +
		"\0\u0137\0\17\0\u013a\0\17\0\u0140\0\17\0\u0143\0\17\0\u0145\0\17\0\u0147\0\17\0" +
		"\u0149\0\17\0\u014a\0\17\0\u0156\0\17\0\u015e\0\17\0\u0160\0\17\0\u0162\0\17\0\u0165" +
		"\0\17\0\u0167\0\17\0\u0168\0\17\0\u0169\0\17\0\u0177\0\17\0\u0179\0\17\0\u017f\0" +
		"\17\0\u0183\0\17\0\u0186\0\17\0\u0188\0\17\0\u0197\0\17\0\u019d\0\17\0\u019f\0\17" +
		"\0\u01ad\0\17\0\1\0\20\0\2\0\20\0\6\0\20\0\66\0\20\0\70\0\20\0\72\0\20\0\73\0\20" +
		"\0\74\0\20\0\76\0\20\0\100\0\20\0\101\0\20\0\123\0\20\0\124\0\20\0\131\0\20\0\132" +
		"\0\20\0\145\0\20\0\147\0\20\0\153\0\20\0\160\0\20\0\161\0\20\0\162\0\20\0\163\0\20" +
		"\0\176\0\20\0\200\0\232\0\201\0\20\0\203\0\20\0\211\0\20\0\213\0\20\0\221\0\20\0" +
		"\226\0\20\0\235\0\20\0\237\0\20\0\240\0\20\0\246\0\20\0\250\0\20\0\253\0\20\0\254" +
		"\0\20\0\255\0\20\0\261\0\20\0\275\0\20\0\316\0\20\0\317\0\20\0\321\0\20\0\323\0\20" +
		"\0\324\0\20\0\325\0\20\0\326\0\20\0\341\0\20\0\343\0\20\0\355\0\20\0\360\0\20\0\364" +
		"\0\20\0\371\0\20\0\373\0\20\0\374\0\20\0\377\0\20\0\u010f\0\20\0\u0111\0\20\0\u0115" +
		"\0\20\0\u0118\0\20\0\u0119\0\20\0\u011b\0\20\0\u0120\0\20\0\u0121\0\20\0\u0125\0" +
		"\20\0\u0126\0\20\0\u0127\0\20\0\u0128\0\20\0\u0129\0\20\0\u012b\0\20\0\u0133\0\20" +
		"\0\u0136\0\20\0\u0137\0\20\0\u013a\0\20\0\u0140\0\20\0\u0143\0\20\0\u0145\0\20\0" +
		"\u0147\0\20\0\u0149\0\20\0\u014a\0\20\0\u0156\0\20\0\u015e\0\20\0\u0160\0\20\0\u0162" +
		"\0\20\0\u0165\0\20\0\u0167\0\20\0\u0168\0\20\0\u0169\0\20\0\u0177\0\20\0\u0179\0" +
		"\20\0\u017f\0\20\0\u0183\0\20\0\u0186\0\20\0\u0188\0\20\0\u0197\0\20\0\u019d\0\20" +
		"\0\u019f\0\20\0\u01ad\0\20\0\1\0\21\0\2\0\21\0\6\0\21\0\66\0\21\0\70\0\21\0\72\0" +
		"\21\0\73\0\21\0\74\0\21\0\76\0\21\0\100\0\21\0\101\0\21\0\123\0\21\0\124\0\21\0\131" +
		"\0\21\0\132\0\21\0\145\0\21\0\147\0\21\0\153\0\21\0\160\0\21\0\161\0\21\0\162\0\21" +
		"\0\163\0\21\0\176\0\21\0\200\0\233\0\201\0\21\0\203\0\21\0\211\0\21\0\213\0\21\0" +
		"\221\0\21\0\226\0\21\0\235\0\21\0\237\0\21\0\240\0\21\0\246\0\21\0\250\0\21\0\253" +
		"\0\21\0\254\0\21\0\255\0\21\0\261\0\21\0\275\0\21\0\316\0\21\0\317\0\21\0\321\0\21" +
		"\0\323\0\21\0\324\0\21\0\325\0\21\0\326\0\21\0\341\0\21\0\343\0\21\0\355\0\21\0\360" +
		"\0\21\0\364\0\21\0\371\0\21\0\373\0\21\0\374\0\21\0\377\0\21\0\u010f\0\21\0\u0111" +
		"\0\21\0\u0115\0\21\0\u0118\0\21\0\u0119\0\21\0\u011b\0\21\0\u0120\0\21\0\u0121\0" +
		"\21\0\u0125\0\21\0\u0126\0\21\0\u0127\0\21\0\u0128\0\21\0\u0129\0\21\0\u012b\0\21" +
		"\0\u0133\0\21\0\u0136\0\21\0\u0137\0\21\0\u013a\0\21\0\u0140\0\21\0\u0143\0\21\0" +
		"\u0145\0\21\0\u0147\0\21\0\u0149\0\21\0\u014a\0\21\0\u0156\0\21\0\u015e\0\21\0\u0160" +
		"\0\21\0\u0162\0\21\0\u0165\0\21\0\u0167\0\21\0\u0168\0\21\0\u0169\0\21\0\u0177\0" +
		"\21\0\u0179\0\21\0\u017f\0\21\0\u0183\0\21\0\u0186\0\21\0\u0188\0\21\0\u0197\0\21" +
		"\0\u019d\0\21\0\u019f\0\21\0\u01ad\0\21\0\1\0\22\0\2\0\22\0\6\0\22\0\66\0\22\0\70" +
		"\0\22\0\72\0\22\0\73\0\22\0\74\0\22\0\76\0\22\0\100\0\22\0\101\0\22\0\123\0\22\0" +
		"\124\0\22\0\131\0\22\0\132\0\22\0\145\0\22\0\147\0\22\0\153\0\22\0\160\0\22\0\161" +
		"\0\22\0\162\0\22\0\163\0\22\0\176\0\22\0\200\0\234\0\201\0\22\0\203\0\22\0\211\0" +
		"\22\0\213\0\22\0\221\0\22\0\226\0\22\0\235\0\22\0\237\0\22\0\240\0\22\0\246\0\22" +
		"\0\247\0\234\0\250\0\22\0\253\0\22\0\254\0\22\0\255\0\22\0\261\0\22\0\275\0\22\0" +
		"\316\0\22\0\317\0\22\0\321\0\22\0\323\0\22\0\324\0\22\0\325\0\22\0\326\0\22\0\341" +
		"\0\22\0\343\0\22\0\355\0\22\0\360\0\22\0\364\0\22\0\371\0\22\0\373\0\22\0\374\0\22" +
		"\0\377\0\22\0\u010f\0\22\0\u0111\0\22\0\u0115\0\22\0\u0118\0\22\0\u0119\0\22\0\u011b" +
		"\0\22\0\u0120\0\22\0\u0121\0\22\0\u0125\0\22\0\u0126\0\22\0\u0127\0\22\0\u0128\0" +
		"\22\0\u0129\0\22\0\u012b\0\22\0\u0133\0\22\0\u0136\0\22\0\u0137\0\22\0\u013a\0\22" +
		"\0\u0140\0\22\0\u0143\0\22\0\u0145\0\22\0\u0147\0\22\0\u0149\0\22\0\u014a\0\22\0" +
		"\u0156\0\22\0\u015e\0\22\0\u0160\0\22\0\u0162\0\22\0\u0165\0\22\0\u0167\0\22\0\u0168" +
		"\0\22\0\u0169\0\22\0\u0177\0\22\0\u0179\0\22\0\u017f\0\22\0\u0183\0\22\0\u0186\0" +
		"\22\0\u0188\0\22\0\u0197\0\22\0\u019d\0\22\0\u019f\0\22\0\u01ad\0\22\0\1\0\23\0\2" +
		"\0\23\0\6\0\23\0\66\0\23\0\70\0\23\0\72\0\23\0\73\0\23\0\74\0\23\0\76\0\23\0\100" +
		"\0\23\0\101\0\23\0\123\0\23\0\124\0\23\0\131\0\23\0\132\0\23\0\145\0\23\0\147\0\23" +
		"\0\153\0\23\0\160\0\23\0\161\0\23\0\162\0\23\0\163\0\23\0\176\0\23\0\200\0\235\0" +
		"\201\0\23\0\203\0\23\0\211\0\23\0\213\0\23\0\221\0\23\0\226\0\23\0\235\0\23\0\237" +
		"\0\23\0\240\0\23\0\246\0\23\0\250\0\23\0\253\0\23\0\254\0\23\0\255\0\23\0\261\0\23" +
		"\0\275\0\23\0\316\0\23\0\317\0\23\0\321\0\23\0\323\0\23\0\324\0\23\0\325\0\23\0\326" +
		"\0\23\0\341\0\23\0\343\0\23\0\355\0\23\0\360\0\23\0\364\0\23\0\371\0\23\0\373\0\23" +
		"\0\374\0\23\0\377\0\23\0\u010f\0\23\0\u0111\0\23\0\u0115\0\23\0\u0118\0\23\0\u0119" +
		"\0\23\0\u011b\0\23\0\u0120\0\23\0\u0121\0\23\0\u0125\0\23\0\u0126\0\23\0\u0127\0" +
		"\23\0\u0128\0\23\0\u0129\0\23\0\u012b\0\23\0\u0133\0\23\0\u0136\0\23\0\u0137\0\23" +
		"\0\u013a\0\23\0\u0140\0\23\0\u0143\0\23\0\u0145\0\23\0\u0147\0\23\0\u0149\0\23\0" +
		"\u014a\0\23\0\u0156\0\23\0\u015e\0\23\0\u0160\0\23\0\u0162\0\23\0\u0165\0\23\0\u0167" +
		"\0\23\0\u0168\0\23\0\u0169\0\23\0\u0177\0\23\0\u0179\0\23\0\u017f\0\23\0\u0183\0" +
		"\23\0\u0186\0\23\0\u0188\0\23\0\u0197\0\23\0\u019d\0\23\0\u019f\0\23\0\u01ad\0\23" +
		"\0\1\0\24\0\2\0\24\0\6\0\24\0\66\0\24\0\70\0\24\0\72\0\24\0\73\0\24\0\74\0\24\0\76" +
		"\0\24\0\100\0\24\0\101\0\24\0\123\0\24\0\124\0\24\0\131\0\24\0\132\0\24\0\145\0\24" +
		"\0\147\0\24\0\153\0\24\0\160\0\24\0\161\0\24\0\162\0\24\0\163\0\24\0\176\0\24\0\200" +
		"\0\236\0\201\0\24\0\203\0\24\0\211\0\24\0\213\0\24\0\221\0\24\0\226\0\24\0\235\0" +
		"\24\0\237\0\24\0\240\0\24\0\246\0\24\0\250\0\24\0\253\0\24\0\254\0\24\0\255\0\24" +
		"\0\261\0\24\0\275\0\24\0\316\0\24\0\317\0\24\0\321\0\24\0\323\0\24\0\324\0\24\0\325" +
		"\0\24\0\326\0\24\0\341\0\24\0\343\0\24\0\355\0\24\0\360\0\24\0\364\0\24\0\371\0\24" +
		"\0\373\0\24\0\374\0\24\0\377\0\24\0\u010f\0\24\0\u0111\0\24\0\u0115\0\24\0\u0118" +
		"\0\24\0\u0119\0\24\0\u011b\0\24\0\u0120\0\24\0\u0121\0\24\0\u0125\0\24\0\u0126\0" +
		"\24\0\u0127\0\24\0\u0128\0\24\0\u0129\0\24\0\u012b\0\24\0\u0133\0\24\0\u0136\0\24" +
		"\0\u0137\0\24\0\u013a\0\24\0\u0140\0\24\0\u0143\0\24\0\u0145\0\24\0\u0147\0\24\0" +
		"\u0149\0\24\0\u014a\0\24\0\u0156\0\24\0\u015e\0\24\0\u0160\0\24\0\u0162\0\24\0\u0165" +
		"\0\24\0\u0167\0\24\0\u0168\0\24\0\u0169\0\24\0\u0177\0\24\0\u0179\0\24\0\u017f\0" +
		"\24\0\u0183\0\24\0\u0186\0\24\0\u0188\0\24\0\u0197\0\24\0\u019d\0\24\0\u019f\0\24" +
		"\0\u01ad\0\24\0\1\0\25\0\2\0\25\0\6\0\25\0\66\0\25\0\70\0\25\0\72\0\25\0\73\0\25" +
		"\0\74\0\25\0\76\0\25\0\100\0\25\0\101\0\25\0\123\0\25\0\124\0\25\0\131\0\25\0\132" +
		"\0\25\0\145\0\25\0\147\0\25\0\153\0\25\0\160\0\25\0\161\0\25\0\162\0\25\0\163\0\25" +
		"\0\176\0\25\0\201\0\25\0\202\0\257\0\203\0\25\0\211\0\25\0\213\0\25\0\221\0\25\0" +
		"\226\0\25\0\235\0\25\0\237\0\25\0\240\0\25\0\246\0\25\0\250\0\25\0\253\0\25\0\254" +
		"\0\25\0\255\0\25\0\261\0\25\0\266\0\257\0\272\0\257\0\275\0\25\0\316\0\25\0\317\0" +
		"\25\0\321\0\25\0\323\0\25\0\324\0\25\0\325\0\25\0\326\0\25\0\341\0\25\0\343\0\25" +
		"\0\355\0\25\0\360\0\25\0\364\0\25\0\371\0\25\0\373\0\25\0\374\0\25\0\377\0\25\0\u0102" +
		"\0\257\0\u010f\0\25\0\u0111\0\25\0\u0115\0\25\0\u0118\0\25\0\u0119\0\25\0\u011b\0" +
		"\25\0\u0120\0\25\0\u0121\0\25\0\u0125\0\25\0\u0126\0\25\0\u0127\0\25\0\u0128\0\25" +
		"\0\u0129\0\25\0\u012b\0\25\0\u0133\0\25\0\u0136\0\25\0\u0137\0\25\0\u013a\0\25\0" +
		"\u0140\0\25\0\u0143\0\25\0\u0145\0\25\0\u0147\0\25\0\u0149\0\25\0\u014a\0\25\0\u0156" +
		"\0\25\0\u015e\0\25\0\u0160\0\25\0\u0162\0\25\0\u0165\0\25\0\u0167\0\25\0\u0168\0" +
		"\25\0\u0169\0\25\0\u0177\0\25\0\u0179\0\25\0\u017f\0\25\0\u0183\0\25\0\u0186\0\25" +
		"\0\u0188\0\25\0\u0197\0\25\0\u019d\0\25\0\u019f\0\25\0\u01ad\0\25\0\1\0\26\0\2\0" +
		"\26\0\6\0\26\0\66\0\26\0\70\0\26\0\72\0\26\0\73\0\26\0\74\0\26\0\76\0\26\0\100\0" +
		"\26\0\101\0\26\0\123\0\26\0\124\0\26\0\131\0\26\0\132\0\26\0\145\0\26\0\147\0\26" +
		"\0\153\0\26\0\160\0\26\0\161\0\26\0\162\0\26\0\163\0\26\0\176\0\26\0\200\0\237\0" +
		"\201\0\26\0\203\0\26\0\211\0\26\0\213\0\26\0\221\0\26\0\226\0\26\0\235\0\26\0\237" +
		"\0\26\0\240\0\26\0\246\0\26\0\250\0\26\0\253\0\26\0\254\0\26\0\255\0\26\0\261\0\26" +
		"\0\275\0\26\0\316\0\26\0\317\0\26\0\321\0\26\0\323\0\26\0\324\0\26\0\325\0\26\0\326" +
		"\0\26\0\341\0\26\0\343\0\26\0\355\0\26\0\360\0\26\0\364\0\26\0\371\0\26\0\373\0\26" +
		"\0\374\0\26\0\377\0\26\0\u010f\0\26\0\u0111\0\26\0\u0115\0\26\0\u0118\0\26\0\u0119" +
		"\0\26\0\u011b\0\26\0\u0120\0\26\0\u0121\0\26\0\u0125\0\26\0\u0126\0\26\0\u0127\0" +
		"\26\0\u0128\0\26\0\u0129\0\26\0\u012b\0\26\0\u0133\0\26\0\u0136\0\26\0\u0137\0\26" +
		"\0\u013a\0\26\0\u0140\0\26\0\u0143\0\26\0\u0145\0\26\0\u0147\0\26\0\u0149\0\26\0" +
		"\u014a\0\26\0\u0156\0\26\0\u015e\0\26\0\u0160\0\26\0\u0162\0\26\0\u0165\0\26\0\u0167" +
		"\0\26\0\u0168\0\26\0\u0169\0\26\0\u0177\0\26\0\u0179\0\26\0\u017f\0\26\0\u0183\0" +
		"\26\0\u0186\0\26\0\u0188\0\26\0\u0197\0\26\0\u019d\0\26\0\u019f\0\26\0\u01ad\0\26" +
		"\0\1\0\27\0\2\0\27\0\6\0\27\0\66\0\27\0\70\0\27\0\72\0\27\0\73\0\27\0\74\0\27\0\76" +
		"\0\27\0\100\0\27\0\101\0\27\0\123\0\27\0\124\0\27\0\131\0\27\0\132\0\27\0\145\0\27" +
		"\0\147\0\27\0\153\0\27\0\160\0\27\0\161\0\27\0\162\0\27\0\163\0\27\0\176\0\27\0\200" +
		"\0\240\0\201\0\27\0\202\0\260\0\203\0\27\0\211\0\27\0\213\0\27\0\221\0\27\0\226\0" +
		"\27\0\235\0\27\0\237\0\27\0\240\0\27\0\246\0\27\0\250\0\27\0\253\0\27\0\254\0\27" +
		"\0\255\0\27\0\261\0\27\0\266\0\260\0\272\0\260\0\275\0\27\0\316\0\27\0\317\0\27\0" +
		"\321\0\27\0\323\0\27\0\324\0\27\0\325\0\27\0\326\0\27\0\341\0\27\0\343\0\27\0\355" +
		"\0\27\0\360\0\27\0\364\0\27\0\371\0\27\0\373\0\27\0\374\0\27\0\377\0\27\0\u0102\0" +
		"\260\0\u010f\0\27\0\u0111\0\27\0\u0115\0\27\0\u0118\0\27\0\u0119\0\27\0\u011b\0\27" +
		"\0\u0120\0\27\0\u0121\0\27\0\u0125\0\27\0\u0126\0\27\0\u0127\0\27\0\u0128\0\27\0" +
		"\u0129\0\27\0\u012b\0\27\0\u0133\0\27\0\u0136\0\27\0\u0137\0\27\0\u013a\0\27\0\u0140" +
		"\0\27\0\u0143\0\27\0\u0145\0\27\0\u0147\0\27\0\u0149\0\27\0\u014a\0\27\0\u0156\0" +
		"\27\0\u015e\0\27\0\u0160\0\27\0\u0162\0\27\0\u0165\0\27\0\u0167\0\27\0\u0168\0\27" +
		"\0\u0169\0\27\0\u0177\0\27\0\u0179\0\27\0\u017f\0\27\0\u0183\0\27\0\u0186\0\27\0" +
		"\u0188\0\27\0\u0197\0\27\0\u019d\0\27\0\u019f\0\27\0\u01ad\0\27\0\1\0\30\0\2\0\30" +
		"\0\6\0\30\0\66\0\30\0\70\0\30\0\72\0\30\0\73\0\30\0\74\0\30\0\76\0\30\0\100\0\30" +
		"\0\101\0\30\0\123\0\30\0\124\0\30\0\131\0\30\0\132\0\30\0\145\0\30\0\147\0\30\0\153" +
		"\0\30\0\160\0\30\0\161\0\30\0\162\0\30\0\163\0\30\0\176\0\30\0\201\0\30\0\203\0\30" +
		"\0\211\0\30\0\213\0\30\0\221\0\30\0\226\0\30\0\235\0\30\0\237\0\30\0\240\0\30\0\246" +
		"\0\30\0\250\0\30\0\253\0\30\0\254\0\30\0\255\0\30\0\261\0\30\0\275\0\30\0\316\0\30" +
		"\0\317\0\30\0\321\0\30\0\323\0\30\0\324\0\30\0\325\0\30\0\326\0\30\0\341\0\30\0\343" +
		"\0\30\0\355\0\30\0\360\0\30\0\364\0\30\0\371\0\30\0\373\0\30\0\374\0\30\0\377\0\30" +
		"\0\u010f\0\30\0\u0111\0\30\0\u0115\0\30\0\u0118\0\30\0\u0119\0\30\0\u011b\0\30\0" +
		"\u0120\0\30\0\u0121\0\30\0\u0125\0\30\0\u0126\0\30\0\u0127\0\30\0\u0128\0\30\0\u0129" +
		"\0\30\0\u012b\0\30\0\u0133\0\30\0\u0136\0\30\0\u0137\0\30\0\u013a\0\30\0\u0140\0" +
		"\30\0\u0143\0\30\0\u0145\0\30\0\u0147\0\30\0\u0149\0\30\0\u014a\0\30\0\u0156\0\30" +
		"\0\u015e\0\30\0\u0160\0\30\0\u0162\0\30\0\u0165\0\30\0\u0167\0\30\0\u0168\0\30\0" +
		"\u0169\0\30\0\u0177\0\30\0\u0179\0\30\0\u017f\0\30\0\u0183\0\30\0\u0186\0\30\0\u0188" +
		"\0\30\0\u0197\0\30\0\u019d\0\30\0\u019f\0\30\0\u01ad\0\30\0\0\0\2\0\1\0\31\0\2\0" +
		"\31\0\6\0\31\0\66\0\31\0\70\0\31\0\72\0\31\0\73\0\31\0\74\0\31\0\76\0\31\0\100\0" +
		"\31\0\101\0\31\0\123\0\31\0\124\0\31\0\131\0\31\0\132\0\31\0\145\0\31\0\147\0\31" +
		"\0\153\0\31\0\160\0\31\0\161\0\31\0\162\0\31\0\163\0\31\0\176\0\31\0\201\0\31\0\203" +
		"\0\31\0\211\0\31\0\213\0\31\0\221\0\31\0\226\0\31\0\235\0\31\0\237\0\31\0\240\0\31" +
		"\0\246\0\31\0\250\0\31\0\253\0\31\0\254\0\31\0\255\0\31\0\261\0\31\0\275\0\31\0\316" +
		"\0\31\0\317\0\31\0\321\0\31\0\323\0\31\0\324\0\31\0\325\0\31\0\326\0\31\0\341\0\31" +
		"\0\343\0\31\0\355\0\31\0\360\0\31\0\364\0\31\0\371\0\31\0\373\0\31\0\374\0\31\0\377" +
		"\0\31\0\u010f\0\31\0\u0111\0\31\0\u0115\0\31\0\u0118\0\31\0\u0119\0\31\0\u011b\0" +
		"\31\0\u0120\0\31\0\u0121\0\31\0\u0125\0\31\0\u0126\0\31\0\u0127\0\31\0\u0128\0\31" +
		"\0\u0129\0\31\0\u012b\0\31\0\u0133\0\31\0\u0136\0\31\0\u0137\0\31\0\u013a\0\31\0" +
		"\u0140\0\31\0\u0143\0\31\0\u0145\0\31\0\u0147\0\31\0\u0149\0\31\0\u014a\0\31\0\u0156" +
		"\0\31\0\u015e\0\31\0\u0160\0\31\0\u0162\0\31\0\u0165\0\31\0\u0167\0\31\0\u0168\0" +
		"\31\0\u0169\0\31\0\u0177\0\31\0\u0179\0\31\0\u017f\0\31\0\u0183\0\31\0\u0186\0\31" +
		"\0\u0188\0\31\0\u0197\0\31\0\u019d\0\31\0\u019f\0\31\0\u01ad\0\31\0\1\0\32\0\2\0" +
		"\32\0\6\0\32\0\66\0\32\0\70\0\32\0\72\0\32\0\73\0\32\0\74\0\32\0\76\0\32\0\100\0" +
		"\32\0\101\0\32\0\123\0\32\0\124\0\32\0\131\0\32\0\132\0\32\0\145\0\32\0\147\0\32" +
		"\0\153\0\32\0\160\0\32\0\161\0\32\0\162\0\32\0\163\0\32\0\176\0\32\0\201\0\32\0\203" +
		"\0\32\0\211\0\32\0\213\0\32\0\221\0\32\0\226\0\32\0\235\0\32\0\237\0\32\0\240\0\32" +
		"\0\246\0\32\0\250\0\32\0\253\0\32\0\254\0\32\0\255\0\32\0\261\0\32\0\275\0\32\0\316" +
		"\0\32\0\317\0\32\0\321\0\32\0\323\0\32\0\324\0\32\0\325\0\32\0\326\0\32\0\341\0\32" +
		"\0\343\0\32\0\355\0\32\0\360\0\32\0\364\0\32\0\371\0\32\0\373\0\32\0\374\0\32\0\377" +
		"\0\32\0\u010f\0\32\0\u0111\0\32\0\u0115\0\32\0\u0118\0\32\0\u0119\0\32\0\u011b\0" +
		"\32\0\u0120\0\32\0\u0121\0\32\0\u0125\0\32\0\u0126\0\32\0\u0127\0\32\0\u0128\0\32" +
		"\0\u0129\0\32\0\u012b\0\32\0\u0133\0\32\0\u0136\0\32\0\u0137\0\32\0\u013a\0\32\0" +
		"\u0140\0\32\0\u0143\0\32\0\u0145\0\32\0\u0147\0\32\0\u0149\0\32\0\u014a\0\32\0\u014d" +
		"\0\u018a\0\u0156\0\32\0\u015e\0\32\0\u0160\0\32\0\u0162\0\32\0\u0165\0\32\0\u0167" +
		"\0\32\0\u0168\0\32\0\u0169\0\32\0\u0177\0\32\0\u0179\0\32\0\u017f\0\32\0\u0183\0" +
		"\32\0\u0186\0\32\0\u0188\0\32\0\u0197\0\32\0\u019d\0\32\0\u019f\0\32\0\u01ad\0\32" +
		"\0\1\0\33\0\2\0\33\0\6\0\33\0\66\0\33\0\70\0\33\0\72\0\33\0\73\0\33\0\74\0\33\0\76" +
		"\0\33\0\100\0\33\0\101\0\33\0\123\0\33\0\124\0\33\0\131\0\33\0\132\0\33\0\145\0\33" +
		"\0\147\0\33\0\153\0\33\0\160\0\33\0\161\0\33\0\162\0\33\0\163\0\33\0\176\0\33\0\200" +
		"\0\241\0\201\0\33\0\203\0\33\0\211\0\33\0\213\0\33\0\221\0\33\0\226\0\33\0\235\0" +
		"\33\0\237\0\33\0\240\0\33\0\246\0\33\0\250\0\33\0\253\0\33\0\254\0\33\0\255\0\33" +
		"\0\261\0\33\0\275\0\33\0\316\0\33\0\317\0\33\0\321\0\33\0\323\0\33\0\324\0\33\0\325" +
		"\0\33\0\326\0\33\0\341\0\33\0\343\0\33\0\355\0\33\0\360\0\33\0\364\0\33\0\371\0\33" +
		"\0\373\0\33\0\374\0\33\0\377\0\33\0\u010f\0\33\0\u0111\0\33\0\u0115\0\33\0\u0118" +
		"\0\33\0\u0119\0\33\0\u011b\0\33\0\u0120\0\33\0\u0121\0\33\0\u0125\0\33\0\u0126\0" +
		"\33\0\u0127\0\33\0\u0128\0\33\0\u0129\0\33\0\u012b\0\33\0\u0133\0\33\0\u0136\0\33" +
		"\0\u0137\0\33\0\u013a\0\33\0\u0140\0\33\0\u0143\0\33\0\u0145\0\33\0\u0147\0\33\0" +
		"\u0149\0\33\0\u014a\0\33\0\u0156\0\33\0\u015e\0\33\0\u0160\0\33\0\u0162\0\33\0\u0165" +
		"\0\33\0\u0167\0\33\0\u0168\0\33\0\u0169\0\33\0\u0177\0\33\0\u0179\0\33\0\u017f\0" +
		"\33\0\u0183\0\33\0\u0186\0\33\0\u0188\0\33\0\u0197\0\33\0\u019d\0\33\0\u019f\0\33" +
		"\0\u01ad\0\33\0\1\0\34\0\2\0\34\0\6\0\34\0\66\0\34\0\70\0\34\0\72\0\34\0\73\0\34" +
		"\0\74\0\34\0\76\0\34\0\100\0\34\0\101\0\34\0\113\0\131\0\123\0\34\0\124\0\34\0\131" +
		"\0\34\0\132\0\34\0\145\0\34\0\147\0\34\0\153\0\34\0\160\0\34\0\161\0\34\0\162\0\34" +
		"\0\163\0\34\0\176\0\34\0\201\0\34\0\203\0\34\0\211\0\34\0\213\0\34\0\221\0\34\0\226" +
		"\0\34\0\235\0\34\0\237\0\34\0\240\0\34\0\246\0\34\0\250\0\34\0\253\0\34\0\254\0\34" +
		"\0\255\0\34\0\261\0\34\0\275\0\34\0\316\0\34\0\317\0\34\0\321\0\34\0\323\0\34\0\324" +
		"\0\34\0\325\0\34\0\326\0\34\0\341\0\34\0\343\0\34\0\355\0\34\0\360\0\34\0\364\0\34" +
		"\0\371\0\34\0\373\0\34\0\374\0\34\0\377\0\34\0\u010f\0\34\0\u0111\0\34\0\u0115\0" +
		"\34\0\u0118\0\34\0\u0119\0\34\0\u011b\0\34\0\u0120\0\34\0\u0121\0\34\0\u0125\0\34" +
		"\0\u0126\0\34\0\u0127\0\34\0\u0128\0\34\0\u0129\0\34\0\u012b\0\34\0\u0133\0\34\0" +
		"\u0136\0\34\0\u0137\0\34\0\u013a\0\34\0\u0140\0\34\0\u0143\0\34\0\u0145\0\34\0\u0147" +
		"\0\34\0\u0149\0\34\0\u014a\0\34\0\u0156\0\34\0\u015e\0\34\0\u0160\0\34\0\u0162\0" +
		"\34\0\u0165\0\34\0\u0167\0\34\0\u0168\0\34\0\u0169\0\34\0\u0177\0\34\0\u0179\0\34" +
		"\0\u017f\0\34\0\u0183\0\34\0\u0186\0\34\0\u0188\0\34\0\u0197\0\34\0\u019d\0\34\0" +
		"\u019f\0\34\0\u01ad\0\34\0\1\0\35\0\2\0\35\0\6\0\35\0\66\0\35\0\70\0\35\0\72\0\35" +
		"\0\73\0\35\0\74\0\35\0\76\0\35\0\100\0\35\0\101\0\35\0\123\0\35\0\124\0\35\0\131" +
		"\0\35\0\132\0\35\0\145\0\35\0\147\0\35\0\153\0\35\0\160\0\35\0\161\0\35\0\162\0\35" +
		"\0\163\0\35\0\176\0\35\0\200\0\242\0\201\0\35\0\203\0\35\0\211\0\35\0\213\0\35\0" +
		"\221\0\35\0\226\0\35\0\235\0\35\0\237\0\35\0\240\0\35\0\246\0\35\0\250\0\35\0\253" +
		"\0\35\0\254\0\35\0\255\0\35\0\261\0\35\0\275\0\35\0\316\0\35\0\317\0\35\0\321\0\35" +
		"\0\323\0\35\0\324\0\35\0\325\0\35\0\326\0\35\0\341\0\35\0\343\0\35\0\355\0\35\0\360" +
		"\0\35\0\364\0\35\0\371\0\35\0\373\0\35\0\374\0\35\0\377\0\35\0\u010f\0\35\0\u0111" +
		"\0\35\0\u0115\0\35\0\u0118\0\35\0\u0119\0\35\0\u011b\0\35\0\u0120\0\35\0\u0121\0" +
		"\35\0\u0125\0\35\0\u0126\0\35\0\u0127\0\35\0\u0128\0\35\0\u0129\0\35\0\u012b\0\35" +
		"\0\u0133\0\35\0\u0136\0\35\0\u0137\0\35\0\u013a\0\35\0\u0140\0\35\0\u0143\0\35\0" +
		"\u0145\0\35\0\u0147\0\35\0\u0149\0\35\0\u014a\0\35\0\u0156\0\35\0\u015e\0\35\0\u0160" +
		"\0\35\0\u0162\0\35\0\u0165\0\35\0\u0167\0\35\0\u0168\0\35\0\u0169\0\35\0\u0177\0" +
		"\35\0\u0179\0\35\0\u017f\0\35\0\u0183\0\35\0\u0186\0\35\0\u0188\0\35\0\u0197\0\35" +
		"\0\u019d\0\35\0\u019f\0\35\0\u01ad\0\35\0\1\0\36\0\2\0\36\0\6\0\36\0\66\0\36\0\70" +
		"\0\36\0\72\0\36\0\73\0\36\0\74\0\36\0\76\0\36\0\100\0\36\0\101\0\36\0\123\0\36\0" +
		"\124\0\36\0\131\0\36\0\132\0\36\0\145\0\36\0\147\0\36\0\153\0\36\0\160\0\36\0\161" +
		"\0\36\0\162\0\36\0\163\0\36\0\176\0\36\0\201\0\36\0\203\0\36\0\211\0\36\0\213\0\36" +
		"\0\221\0\36\0\226\0\36\0\235\0\36\0\237\0\36\0\240\0\36\0\246\0\36\0\250\0\36\0\253" +
		"\0\36\0\254\0\36\0\255\0\36\0\261\0\36\0\275\0\36\0\310\0\u010d\0\316\0\36\0\317" +
		"\0\36\0\321\0\36\0\323\0\36\0\324\0\36\0\325\0\36\0\326\0\36\0\341\0\36\0\343\0\36" +
		"\0\355\0\36\0\360\0\36\0\364\0\36\0\371\0\36\0\373\0\36\0\374\0\36\0\377\0\36\0\u010f" +
		"\0\36\0\u0111\0\36\0\u0115\0\36\0\u0118\0\36\0\u0119\0\36\0\u011b\0\36\0\u0120\0" +
		"\36\0\u0121\0\36\0\u0125\0\36\0\u0126\0\36\0\u0127\0\36\0\u0128\0\36\0\u0129\0\36" +
		"\0\u012b\0\36\0\u0133\0\36\0\u0136\0\36\0\u0137\0\36\0\u013a\0\36\0\u0140\0\36\0" +
		"\u0143\0\36\0\u0145\0\36\0\u0147\0\36\0\u0149\0\36\0\u014a\0\36\0\u0156\0\36\0\u015e" +
		"\0\36\0\u0160\0\36\0\u0162\0\36\0\u0165\0\36\0\u0167\0\36\0\u0168\0\36\0\u0169\0" +
		"\36\0\u0177\0\36\0\u0179\0\36\0\u017f\0\36\0\u0183\0\36\0\u0186\0\36\0\u0188\0\36" +
		"\0\u0197\0\36\0\u019d\0\36\0\u019f\0\36\0\u01ad\0\36\0\1\0\37\0\2\0\37\0\6\0\37\0" +
		"\66\0\37\0\70\0\37\0\72\0\37\0\73\0\37\0\74\0\37\0\76\0\37\0\100\0\37\0\101\0\37" +
		"\0\123\0\37\0\124\0\37\0\131\0\37\0\132\0\37\0\145\0\37\0\147\0\37\0\153\0\37\0\160" +
		"\0\37\0\161\0\37\0\162\0\37\0\163\0\37\0\176\0\37\0\200\0\243\0\201\0\37\0\203\0" +
		"\37\0\211\0\37\0\213\0\37\0\221\0\37\0\226\0\37\0\235\0\37\0\237\0\37\0\240\0\37" +
		"\0\246\0\37\0\250\0\37\0\253\0\37\0\254\0\37\0\255\0\37\0\261\0\37\0\275\0\37\0\316" +
		"\0\37\0\317\0\37\0\321\0\37\0\323\0\37\0\324\0\37\0\325\0\37\0\326\0\37\0\341\0\37" +
		"\0\343\0\37\0\355\0\37\0\360\0\37\0\364\0\37\0\371\0\37\0\373\0\37\0\374\0\37\0\377" +
		"\0\37\0\u010f\0\37\0\u0111\0\37\0\u0115\0\37\0\u0118\0\37\0\u0119\0\37\0\u011b\0" +
		"\37\0\u0120\0\37\0\u0121\0\37\0\u0125\0\37\0\u0126\0\37\0\u0127\0\37\0\u0128\0\37" +
		"\0\u0129\0\37\0\u012b\0\37\0\u0133\0\37\0\u0136\0\37\0\u0137\0\37\0\u013a\0\37\0" +
		"\u0140\0\37\0\u0143\0\37\0\u0145\0\37\0\u0147\0\37\0\u0149\0\37\0\u014a\0\37\0\u0156" +
		"\0\37\0\u015e\0\37\0\u0160\0\37\0\u0162\0\37\0\u0165\0\37\0\u0167\0\37\0\u0168\0" +
		"\37\0\u0169\0\37\0\u0177\0\37\0\u0179\0\37\0\u017f\0\37\0\u0183\0\37\0\u0186\0\37" +
		"\0\u0188\0\37\0\u0197\0\37\0\u019d\0\37\0\u019f\0\37\0\u01ad\0\37\0\1\0\40\0\2\0" +
		"\40\0\6\0\40\0\66\0\40\0\70\0\40\0\72\0\40\0\73\0\40\0\74\0\40\0\76\0\40\0\100\0" +
		"\40\0\101\0\40\0\123\0\40\0\124\0\40\0\131\0\40\0\132\0\40\0\145\0\40\0\147\0\40" +
		"\0\153\0\40\0\160\0\40\0\161\0\40\0\162\0\40\0\163\0\40\0\176\0\40\0\201\0\40\0\203" +
		"\0\40\0\211\0\40\0\213\0\40\0\221\0\40\0\226\0\40\0\230\0\304\0\235\0\40\0\237\0" +
		"\40\0\240\0\40\0\246\0\40\0\250\0\40\0\253\0\40\0\254\0\40\0\255\0\40\0\261\0\40" +
		"\0\275\0\40\0\316\0\40\0\317\0\40\0\321\0\40\0\323\0\40\0\324\0\40\0\325\0\40\0\326" +
		"\0\40\0\341\0\40\0\343\0\40\0\355\0\40\0\360\0\40\0\364\0\40\0\371\0\40\0\373\0\40" +
		"\0\374\0\40\0\377\0\40\0\u010f\0\40\0\u0111\0\40\0\u0115\0\40\0\u0118\0\40\0\u0119" +
		"\0\40\0\u011b\0\40\0\u0120\0\40\0\u0121\0\40\0\u0125\0\40\0\u0126\0\40\0\u0127\0" +
		"\40\0\u0128\0\40\0\u0129\0\40\0\u012b\0\40\0\u0133\0\40\0\u0136\0\40\0\u0137\0\40" +
		"\0\u013a\0\40\0\u0140\0\40\0\u0143\0\40\0\u0145\0\40\0\u0147\0\40\0\u0149\0\40\0" +
		"\u014a\0\40\0\u0156\0\40\0\u015e\0\40\0\u0160\0\40\0\u0162\0\40\0\u0165\0\40\0\u0167" +
		"\0\40\0\u0168\0\40\0\u0169\0\40\0\u0177\0\40\0\u0179\0\40\0\u017f\0\40\0\u0183\0" +
		"\40\0\u0186\0\40\0\u0188\0\40\0\u0197\0\40\0\u019d\0\40\0\u019f\0\40\0\u01ad\0\40" +
		"\0\1\0\41\0\2\0\41\0\6\0\41\0\66\0\41\0\70\0\41\0\72\0\41\0\73\0\41\0\74\0\41\0\76" +
		"\0\41\0\100\0\41\0\101\0\41\0\123\0\41\0\124\0\41\0\131\0\41\0\132\0\41\0\145\0\41" +
		"\0\147\0\41\0\153\0\41\0\160\0\41\0\161\0\41\0\162\0\41\0\163\0\41\0\176\0\41\0\200" +
		"\0\244\0\201\0\41\0\203\0\41\0\211\0\41\0\213\0\41\0\221\0\41\0\226\0\41\0\235\0" +
		"\41\0\237\0\41\0\240\0\41\0\246\0\41\0\247\0\244\0\250\0\41\0\253\0\41\0\254\0\41" +
		"\0\255\0\41\0\261\0\41\0\275\0\41\0\316\0\41\0\317\0\41\0\321\0\41\0\323\0\41\0\324" +
		"\0\41\0\325\0\41\0\326\0\41\0\341\0\41\0\343\0\41\0\355\0\41\0\360\0\41\0\364\0\41" +
		"\0\371\0\41\0\373\0\41\0\374\0\41\0\377\0\41\0\u010f\0\41\0\u0111\0\41\0\u0115\0" +
		"\41\0\u0118\0\41\0\u0119\0\41\0\u011b\0\41\0\u0120\0\41\0\u0121\0\41\0\u0125\0\41" +
		"\0\u0126\0\41\0\u0127\0\41\0\u0128\0\41\0\u0129\0\41\0\u012b\0\41\0\u0133\0\41\0" +
		"\u0136\0\41\0\u0137\0\41\0\u013a\0\41\0\u0140\0\41\0\u0143\0\41\0\u0145\0\41\0\u0147" +
		"\0\41\0\u0149\0\41\0\u014a\0\41\0\u0156\0\41\0\u015e\0\41\0\u0160\0\41\0\u0162\0" +
		"\41\0\u0165\0\41\0\u0167\0\41\0\u0168\0\41\0\u0169\0\41\0\u0177\0\41\0\u0179\0\41" +
		"\0\u017f\0\41\0\u0183\0\41\0\u0186\0\41\0\u0188\0\41\0\u0197\0\41\0\u019d\0\41\0" +
		"\u019f\0\41\0\u01ad\0\41\0\1\0\42\0\2\0\42\0\6\0\42\0\66\0\42\0\70\0\42\0\72\0\42" +
		"\0\73\0\42\0\74\0\42\0\76\0\42\0\100\0\42\0\101\0\42\0\123\0\42\0\124\0\42\0\131" +
		"\0\42\0\132\0\42\0\133\0\160\0\145\0\42\0\147\0\42\0\153\0\42\0\160\0\42\0\161\0" +
		"\42\0\162\0\42\0\163\0\42\0\176\0\42\0\201\0\42\0\203\0\42\0\211\0\42\0\213\0\42" +
		"\0\221\0\42\0\226\0\42\0\235\0\42\0\237\0\42\0\240\0\42\0\246\0\42\0\250\0\42\0\253" +
		"\0\42\0\254\0\42\0\255\0\42\0\261\0\42\0\275\0\42\0\316\0\42\0\317\0\42\0\321\0\42" +
		"\0\323\0\42\0\324\0\42\0\325\0\42\0\326\0\42\0\341\0\42\0\343\0\42\0\355\0\42\0\360" +
		"\0\42\0\364\0\42\0\371\0\42\0\373\0\42\0\374\0\42\0\377\0\42\0\u010f\0\42\0\u0111" +
		"\0\42\0\u0115\0\42\0\u0118\0\42\0\u0119\0\42\0\u011b\0\42\0\u0120\0\42\0\u0121\0" +
		"\42\0\u0125\0\42\0\u0126\0\42\0\u0127\0\42\0\u0128\0\42\0\u0129\0\42\0\u012b\0\42" +
		"\0\u0133\0\42\0\u0136\0\42\0\u0137\0\42\0\u013a\0\42\0\u0140\0\42\0\u0143\0\42\0" +
		"\u0145\0\42\0\u0147\0\42\0\u0149\0\42\0\u014a\0\42\0\u0156\0\42\0\u015e\0\42\0\u0160" +
		"\0\42\0\u0162\0\42\0\u0165\0\42\0\u0167\0\42\0\u0168\0\42\0\u0169\0\42\0\u0177\0" +
		"\42\0\u0179\0\42\0\u017f\0\42\0\u0183\0\42\0\u0186\0\42\0\u0188\0\42\0\u0197\0\42" +
		"\0\u019d\0\42\0\u019f\0\42\0\u01ad\0\42\0\1\0\43\0\2\0\43\0\6\0\43\0\66\0\43\0\70" +
		"\0\43\0\72\0\43\0\73\0\43\0\74\0\43\0\76\0\43\0\100\0\43\0\101\0\43\0\123\0\43\0" +
		"\124\0\43\0\131\0\43\0\132\0\43\0\145\0\43\0\147\0\43\0\153\0\43\0\160\0\43\0\161" +
		"\0\43\0\162\0\43\0\163\0\43\0\176\0\43\0\201\0\43\0\203\0\43\0\211\0\43\0\213\0\43" +
		"\0\221\0\43\0\226\0\43\0\235\0\43\0\237\0\43\0\240\0\43\0\246\0\43\0\250\0\43\0\253" +
		"\0\43\0\254\0\43\0\255\0\43\0\261\0\43\0\275\0\43\0\316\0\43\0\317\0\43\0\321\0\43" +
		"\0\322\0\u0118\0\323\0\43\0\324\0\43\0\325\0\43\0\326\0\43\0\341\0\43\0\343\0\43" +
		"\0\355\0\43\0\360\0\43\0\364\0\43\0\371\0\43\0\373\0\43\0\374\0\43\0\377\0\43\0\u010f" +
		"\0\43\0\u0111\0\43\0\u0115\0\43\0\u0118\0\43\0\u0119\0\43\0\u011b\0\43\0\u0120\0" +
		"\43\0\u0121\0\43\0\u0125\0\43\0\u0126\0\43\0\u0127\0\43\0\u0128\0\43\0\u0129\0\43" +
		"\0\u012b\0\43\0\u0133\0\43\0\u0136\0\43\0\u0137\0\43\0\u013a\0\43\0\u0140\0\43\0" +
		"\u0143\0\43\0\u0145\0\43\0\u0147\0\43\0\u0149\0\43\0\u014a\0\43\0\u0156\0\43\0\u015e" +
		"\0\43\0\u0160\0\43\0\u0162\0\43\0\u0165\0\43\0\u0167\0\43\0\u0168\0\43\0\u0169\0" +
		"\43\0\u0177\0\43\0\u0179\0\43\0\u017f\0\43\0\u0183\0\43\0\u0186\0\43\0\u0188\0\43" +
		"\0\u0197\0\43\0\u019d\0\43\0\u019f\0\43\0\u01ad\0\43\0\1\0\44\0\2\0\44\0\6\0\44\0" +
		"\66\0\44\0\70\0\44\0\72\0\44\0\73\0\44\0\74\0\44\0\76\0\44\0\100\0\44\0\101\0\44" +
		"\0\123\0\44\0\124\0\44\0\131\0\44\0\132\0\44\0\145\0\44\0\147\0\44\0\153\0\44\0\160" +
		"\0\44\0\161\0\44\0\162\0\44\0\163\0\44\0\176\0\44\0\201\0\44\0\202\0\261\0\203\0" +
		"\44\0\211\0\44\0\213\0\44\0\221\0\44\0\226\0\44\0\235\0\44\0\237\0\44\0\240\0\44" +
		"\0\246\0\44\0\250\0\44\0\253\0\44\0\254\0\44\0\255\0\44\0\261\0\44\0\266\0\261\0" +
		"\272\0\261\0\275\0\44\0\316\0\44\0\317\0\44\0\321\0\44\0\323\0\44\0\324\0\44\0\325" +
		"\0\44\0\326\0\44\0\341\0\44\0\343\0\44\0\355\0\44\0\360\0\44\0\364\0\44\0\371\0\44" +
		"\0\373\0\44\0\374\0\44\0\377\0\44\0\u0102\0\261\0\u010f\0\44\0\u0111\0\44\0\u0115" +
		"\0\44\0\u0118\0\44\0\u0119\0\44\0\u011b\0\44\0\u0120\0\44\0\u0121\0\44\0\u0125\0" +
		"\44\0\u0126\0\44\0\u0127\0\44\0\u0128\0\44\0\u0129\0\44\0\u012b\0\44\0\u0133\0\44" +
		"\0\u0136\0\44\0\u0137\0\44\0\u013a\0\44\0\u0140\0\44\0\u0143\0\44\0\u0145\0\44\0" +
		"\u0147\0\44\0\u0149\0\44\0\u014a\0\44\0\u0156\0\44\0\u015e\0\44\0\u0160\0\44\0\u0162" +
		"\0\44\0\u0165\0\44\0\u0167\0\44\0\u0168\0\44\0\u0169\0\44\0\u0177\0\44\0\u0179\0" +
		"\44\0\u017f\0\44\0\u0183\0\44\0\u0186\0\44\0\u0188\0\44\0\u0197\0\44\0\u019d\0\44" +
		"\0\u019f\0\44\0\u01ad\0\44\0\1\0\45\0\2\0\45\0\6\0\45\0\66\0\45\0\70\0\45\0\72\0" +
		"\45\0\73\0\45\0\74\0\45\0\76\0\45\0\100\0\45\0\101\0\45\0\123\0\45\0\124\0\45\0\131" +
		"\0\45\0\132\0\45\0\145\0\45\0\147\0\45\0\153\0\45\0\160\0\45\0\161\0\45\0\162\0\45" +
		"\0\163\0\45\0\176\0\45\0\200\0\245\0\201\0\45\0\203\0\45\0\211\0\45\0\213\0\45\0" +
		"\221\0\45\0\226\0\45\0\235\0\45\0\237\0\45\0\240\0\45\0\246\0\45\0\250\0\45\0\253" +
		"\0\45\0\254\0\45\0\255\0\45\0\261\0\45\0\275\0\45\0\316\0\45\0\317\0\45\0\321\0\45" +
		"\0\323\0\45\0\324\0\45\0\325\0\45\0\326\0\45\0\341\0\45\0\343\0\45\0\355\0\45\0\360" +
		"\0\45\0\364\0\45\0\371\0\45\0\373\0\45\0\374\0\45\0\377\0\45\0\u010f\0\45\0\u0111" +
		"\0\45\0\u0115\0\45\0\u0118\0\45\0\u0119\0\45\0\u011b\0\45\0\u0120\0\45\0\u0121\0" +
		"\45\0\u0125\0\45\0\u0126\0\45\0\u0127\0\45\0\u0128\0\45\0\u0129\0\45\0\u012b\0\45" +
		"\0\u0133\0\45\0\u0136\0\45\0\u0137\0\45\0\u013a\0\45\0\u0140\0\45\0\u0143\0\45\0" +
		"\u0145\0\45\0\u0147\0\45\0\u0149\0\45\0\u014a\0\45\0\u0156\0\45\0\u015e\0\45\0\u0160" +
		"\0\45\0\u0162\0\45\0\u0165\0\45\0\u0167\0\45\0\u0168\0\45\0\u0169\0\45\0\u0177\0" +
		"\45\0\u0179\0\45\0\u017f\0\45\0\u0183\0\45\0\u0186\0\45\0\u0188\0\45\0\u0197\0\45" +
		"\0\u019d\0\45\0\u019f\0\45\0\u01ad\0\45\0\1\0\46\0\2\0\46\0\6\0\46\0\66\0\46\0\70" +
		"\0\46\0\72\0\46\0\73\0\46\0\74\0\46\0\76\0\46\0\100\0\46\0\101\0\46\0\123\0\46\0" +
		"\124\0\46\0\131\0\46\0\132\0\46\0\144\0\162\0\145\0\46\0\147\0\46\0\153\0\46\0\160" +
		"\0\46\0\161\0\46\0\162\0\46\0\163\0\46\0\176\0\46\0\201\0\46\0\203\0\46\0\211\0\46" +
		"\0\213\0\46\0\221\0\46\0\226\0\46\0\235\0\46\0\237\0\46\0\240\0\46\0\246\0\46\0\250" +
		"\0\46\0\253\0\46\0\254\0\46\0\255\0\46\0\261\0\46\0\275\0\46\0\316\0\46\0\317\0\46" +
		"\0\321\0\46\0\323\0\46\0\324\0\46\0\325\0\46\0\326\0\46\0\341\0\46\0\343\0\46\0\355" +
		"\0\46\0\360\0\46\0\364\0\46\0\371\0\46\0\373\0\46\0\374\0\46\0\377\0\46\0\u010f\0" +
		"\46\0\u0111\0\46\0\u0115\0\46\0\u0118\0\46\0\u0119\0\46\0\u011b\0\46\0\u0120\0\46" +
		"\0\u0121\0\46\0\u0125\0\46\0\u0126\0\46\0\u0127\0\46\0\u0128\0\46\0\u0129\0\46\0" +
		"\u012b\0\46\0\u0133\0\46\0\u0136\0\46\0\u0137\0\46\0\u013a\0\46\0\u0140\0\46\0\u0143" +
		"\0\46\0\u0145\0\46\0\u0147\0\46\0\u0149\0\46\0\u014a\0\46\0\u0156\0\46\0\u015e\0" +
		"\46\0\u0160\0\46\0\u0162\0\46\0\u0165\0\46\0\u0167\0\46\0\u0168\0\46\0\u0169\0\46" +
		"\0\u0177\0\46\0\u0179\0\46\0\u017f\0\46\0\u0183\0\46\0\u0186\0\46\0\u0188\0\46\0" +
		"\u0197\0\46\0\u019d\0\46\0\u019f\0\46\0\u01ad\0\46\0\1\0\47\0\2\0\47\0\6\0\47\0\66" +
		"\0\47\0\70\0\47\0\72\0\47\0\73\0\47\0\74\0\47\0\76\0\47\0\100\0\47\0\101\0\47\0\123" +
		"\0\47\0\124\0\47\0\131\0\47\0\132\0\47\0\145\0\47\0\147\0\47\0\153\0\47\0\160\0\47" +
		"\0\161\0\47\0\162\0\47\0\163\0\47\0\176\0\47\0\201\0\47\0\203\0\47\0\211\0\47\0\213" +
		"\0\47\0\221\0\47\0\226\0\47\0\235\0\47\0\237\0\47\0\240\0\47\0\246\0\47\0\250\0\47" +
		"\0\253\0\47\0\254\0\47\0\255\0\47\0\261\0\47\0\275\0\47\0\316\0\47\0\317\0\47\0\321" +
		"\0\47\0\322\0\u0119\0\323\0\47\0\324\0\47\0\325\0\47\0\326\0\47\0\341\0\47\0\343" +
		"\0\47\0\355\0\47\0\360\0\47\0\364\0\47\0\371\0\47\0\373\0\47\0\374\0\47\0\377\0\47" +
		"\0\u010f\0\47\0\u0111\0\47\0\u0115\0\47\0\u0118\0\47\0\u0119\0\47\0\u011b\0\47\0" +
		"\u0120\0\47\0\u0121\0\47\0\u0125\0\47\0\u0126\0\47\0\u0127\0\47\0\u0128\0\47\0\u0129" +
		"\0\47\0\u012b\0\47\0\u0133\0\47\0\u0136\0\47\0\u0137\0\47\0\u013a\0\47\0\u0140\0" +
		"\47\0\u0143\0\47\0\u0145\0\47\0\u0147\0\47\0\u0149\0\47\0\u014a\0\47\0\u0156\0\47" +
		"\0\u015e\0\47\0\u0160\0\47\0\u0162\0\47\0\u0165\0\47\0\u0167\0\47\0\u0168\0\47\0" +
		"\u0169\0\47\0\u0177\0\47\0\u0179\0\47\0\u017f\0\47\0\u0183\0\47\0\u0186\0\47\0\u0188" +
		"\0\47\0\u0197\0\47\0\u019d\0\47\0\u019f\0\47\0\u01ad\0\47\0\1\0\50\0\2\0\50\0\6\0" +
		"\50\0\66\0\50\0\70\0\50\0\72\0\50\0\73\0\50\0\74\0\50\0\76\0\50\0\100\0\50\0\101" +
		"\0\50\0\123\0\50\0\124\0\50\0\131\0\50\0\132\0\50\0\145\0\50\0\147\0\50\0\153\0\50" +
		"\0\160\0\50\0\161\0\50\0\162\0\50\0\163\0\50\0\176\0\50\0\201\0\50\0\203\0\50\0\211" +
		"\0\50\0\213\0\50\0\221\0\50\0\226\0\50\0\235\0\50\0\237\0\50\0\240\0\50\0\246\0\50" +
		"\0\250\0\50\0\253\0\50\0\254\0\50\0\255\0\50\0\261\0\50\0\275\0\50\0\316\0\50\0\317" +
		"\0\50\0\321\0\50\0\323\0\50\0\324\0\50\0\325\0\50\0\326\0\50\0\341\0\50\0\343\0\50" +
		"\0\355\0\50\0\360\0\50\0\364\0\50\0\371\0\50\0\373\0\50\0\374\0\50\0\377\0\50\0\u010f" +
		"\0\50\0\u0111\0\50\0\u0115\0\50\0\u0118\0\50\0\u0119\0\50\0\u011b\0\50\0\u0120\0" +
		"\50\0\u0121\0\50\0\u0125\0\50\0\u0126\0\50\0\u0127\0\50\0\u0128\0\50\0\u0129\0\50" +
		"\0\u012b\0\50\0\u0133\0\50\0\u0136\0\50\0\u0137\0\50\0\u013a\0\50\0\u0140\0\50\0" +
		"\u0143\0\50\0\u0145\0\50\0\u0147\0\50\0\u0149\0\50\0\u014a\0\50\0\u014d\0\u018b\0" +
		"\u0156\0\50\0\u015e\0\50\0\u0160\0\50\0\u0162\0\50\0\u0165\0\50\0\u0167\0\50\0\u0168" +
		"\0\50\0\u0169\0\50\0\u0177\0\50\0\u0179\0\50\0\u017f\0\50\0\u0183\0\50\0\u0186\0" +
		"\50\0\u0188\0\50\0\u0197\0\50\0\u019d\0\50\0\u019f\0\50\0\u01ad\0\50\0\1\0\51\0\2" +
		"\0\51\0\6\0\51\0\66\0\51\0\70\0\51\0\72\0\51\0\73\0\51\0\74\0\51\0\76\0\51\0\100" +
		"\0\51\0\101\0\51\0\123\0\51\0\124\0\51\0\131\0\51\0\132\0\51\0\145\0\51\0\147\0\51" +
		"\0\153\0\51\0\160\0\51\0\161\0\51\0\162\0\51\0\163\0\51\0\176\0\51\0\201\0\51\0\202" +
		"\0\262\0\203\0\51\0\211\0\51\0\213\0\51\0\221\0\51\0\226\0\51\0\235\0\51\0\237\0" +
		"\51\0\240\0\51\0\246\0\51\0\250\0\51\0\253\0\51\0\254\0\51\0\255\0\51\0\261\0\51" +
		"\0\266\0\262\0\272\0\262\0\275\0\51\0\316\0\51\0\317\0\51\0\321\0\51\0\323\0\51\0" +
		"\324\0\51\0\325\0\51\0\326\0\51\0\341\0\51\0\343\0\51\0\355\0\51\0\360\0\51\0\364" +
		"\0\51\0\371\0\51\0\373\0\51\0\374\0\51\0\377\0\51\0\u0102\0\262\0\u010f\0\51\0\u0111" +
		"\0\51\0\u0115\0\51\0\u0118\0\51\0\u0119\0\51\0\u011b\0\51\0\u0120\0\51\0\u0121\0" +
		"\51\0\u0125\0\51\0\u0126\0\51\0\u0127\0\51\0\u0128\0\51\0\u0129\0\51\0\u012b\0\51" +
		"\0\u0133\0\51\0\u0136\0\51\0\u0137\0\51\0\u013a\0\51\0\u0140\0\51\0\u0143\0\51\0" +
		"\u0145\0\51\0\u0147\0\51\0\u0149\0\51\0\u014a\0\51\0\u0156\0\51\0\u015e\0\51\0\u0160" +
		"\0\51\0\u0162\0\51\0\u0165\0\51\0\u0167\0\51\0\u0168\0\51\0\u0169\0\51\0\u0177\0" +
		"\51\0\u0179\0\51\0\u017f\0\51\0\u0183\0\51\0\u0186\0\51\0\u0188\0\51\0\u0197\0\51" +
		"\0\u019d\0\51\0\u019f\0\51\0\u01ad\0\51\0\1\0\52\0\2\0\52\0\6\0\52\0\66\0\52\0\70" +
		"\0\52\0\72\0\52\0\73\0\52\0\74\0\52\0\76\0\52\0\100\0\52\0\101\0\52\0\123\0\52\0" +
		"\124\0\52\0\131\0\52\0\132\0\52\0\144\0\163\0\145\0\52\0\147\0\52\0\153\0\52\0\160" +
		"\0\52\0\161\0\52\0\162\0\52\0\163\0\52\0\176\0\52\0\201\0\52\0\203\0\52\0\211\0\52" +
		"\0\213\0\52\0\221\0\52\0\226\0\52\0\235\0\52\0\237\0\52\0\240\0\52\0\246\0\52\0\250" +
		"\0\52\0\253\0\52\0\254\0\52\0\255\0\52\0\261\0\52\0\275\0\52\0\316\0\52\0\317\0\52" +
		"\0\321\0\52\0\323\0\52\0\324\0\52\0\325\0\52\0\326\0\52\0\341\0\52\0\343\0\52\0\355" +
		"\0\52\0\360\0\52\0\364\0\52\0\371\0\52\0\373\0\52\0\374\0\52\0\377\0\52\0\u010f\0" +
		"\52\0\u0111\0\52\0\u0115\0\52\0\u0118\0\52\0\u0119\0\52\0\u011b\0\52\0\u0120\0\52" +
		"\0\u0121\0\52\0\u0125\0\52\0\u0126\0\52\0\u0127\0\52\0\u0128\0\52\0\u0129\0\52\0" +
		"\u012b\0\52\0\u0133\0\52\0\u0136\0\52\0\u0137\0\52\0\u013a\0\52\0\u0140\0\52\0\u0143" +
		"\0\52\0\u0145\0\52\0\u0147\0\52\0\u0149\0\52\0\u014a\0\52\0\u0156\0\52\0\u015e\0" +
		"\52\0\u0160\0\52\0\u0162\0\52\0\u0165\0\52\0\u0167\0\52\0\u0168\0\52\0\u0169\0\52" +
		"\0\u0177\0\52\0\u0179\0\52\0\u017f\0\52\0\u0183\0\52\0\u0186\0\52\0\u0188\0\52\0" +
		"\u0197\0\52\0\u019d\0\52\0\u019f\0\52\0\u01ad\0\52\0\146\0\171\0\177\0\171\0\202" +
		"\0\171\0\253\0\331\0\266\0\171\0\272\0\171\0\325\0\331\0\341\0\331\0\343\0\331\0" +
		"\371\0\331\0\373\0\331\0\374\0\331\0\377\0\331\0\u0102\0\171\0\u0120\0\331\0\u0125" +
		"\0\331\0\u0129\0\331\0\u012b\0\331\0\u0140\0\331\0\u0143\0\331\0\u0145\0\331\0\u0147" +
		"\0\331\0\u0149\0\331\0\u014a\0\331\0\u014f\0\331\0\u017f\0\331\0\u0183\0\331\0\u0186" +
		"\0\331\0\u0188\0\331\0\u018f\0\331\0\u01ad\0\331\0\153\0\176\0\170\0\223\0\225\0" +
		"\223\0\302\0\223\0\357\0\u0137\0\1\0\53\0\2\0\57\0\6\0\53\0\66\0\102\0\70\0\107\0" +
		"\72\0\57\0\73\0\112\0\74\0\114\0\76\0\53\0\100\0\102\0\101\0\102\0\123\0\135\0\124" +
		"\0\102\0\131\0\146\0\132\0\53\0\145\0\165\0\147\0\146\0\153\0\177\0\160\0\202\0\161" +
		"\0\135\0\162\0\214\0\163\0\214\0\176\0\146\0\201\0\251\0\203\0\202\0\211\0\272\0" +
		"\213\0\135\0\221\0\165\0\226\0\146\0\235\0\307\0\237\0\135\0\240\0\313\0\246\0\135" +
		"\0\250\0\320\0\253\0\332\0\254\0\357\0\255\0\360\0\261\0\135\0\275\0\214\0\316\0" +
		"\135\0\317\0\u0114\0\321\0\53\0\323\0\u011a\0\324\0\102\0\325\0\332\0\326\0\135\0" +
		"\341\0\332\0\343\0\332\0\355\0\332\0\360\0\u0139\0\364\0\135\0\371\0\332\0\373\0" +
		"\332\0\374\0\332\0\377\0\332\0\u010f\0\135\0\u0111\0\u0155\0\u0115\0\135\0\u0118" +
		"\0\135\0\u0119\0\135\0\u011b\0\102\0\u0120\0\332\0\u0121\0\135\0\u0125\0\332\0\u0126" +
		"\0\u0169\0\u0127\0\53\0\u0128\0\53\0\u0129\0\332\0\u012b\0\332\0\u0133\0\53\0\u0136" +
		"\0\u0175\0\u0137\0\313\0\u013a\0\360\0\u0140\0\332\0\u0143\0\332\0\u0145\0\332\0" +
		"\u0147\0\332\0\u0149\0\332\0\u014a\0\332\0\u0156\0\135\0\u015e\0\102\0\u0160\0\102" +
		"\0\u0162\0\135\0\u0165\0\135\0\u0167\0\u0169\0\u0168\0\u0169\0\u0169\0\53\0\u0177" +
		"\0\135\0\u0179\0\135\0\u017f\0\332\0\u0183\0\332\0\u0186\0\332\0\u0188\0\332\0\u0197" +
		"\0\135\0\u019d\0\u0169\0\u019f\0\u0169\0\u01ad\0\332\0\1\0\54\0\6\0\54\0\76\0\54" +
		"\0\123\0\136\0\132\0\54\0\321\0\54\0\u0115\0\136\0\u0133\0\u0173\0\u0156\0\136\0" +
		"\u015c\0\u0193\0\u015d\0\u0194\0\u0177\0\136\0\170\0\224\0\225\0\300\0\302\0\u0107" +
		"\0\2\0\60\0\72\0\60\0\2\0\61\0\72\0\110\0\253\0\333\0\325\0\333\0\341\0\333\0\343" +
		"\0\333\0\371\0\333\0\373\0\333\0\374\0\333\0\377\0\333\0\u0120\0\333\0\u0125\0\333" +
		"\0\u0129\0\333\0\u012b\0\333\0\u0140\0\333\0\u0143\0\333\0\u0145\0\333\0\u0147\0" +
		"\333\0\u0149\0\333\0\u014a\0\333\0\u014f\0\u018d\0\u017f\0\333\0\u0183\0\333\0\u0186" +
		"\0\333\0\u0188\0\333\0\u018f\0\u018d\0\u01ad\0\333\0\1\0\55\0\6\0\55\0\74\0\115\0" +
		"\76\0\55\0\132\0\55\0\147\0\174\0\201\0\252\0\203\0\267\0\226\0\174\0\253\0\334\0" +
		"\321\0\55\0\325\0\334\0\343\0\u012d\0\371\0\334\0\373\0\334\0\374\0\334\0\377\0\334" +
		"\0\u0120\0\u012d\0\u0125\0\334\0\u0129\0\334\0\u012b\0\u012d\0\u0140\0\334\0\u0143" +
		"\0\334\0\u0145\0\334\0\u0147\0\334\0\u0149\0\334\0\u014a\0\334\0\u017f\0\334\0\u0183" +
		"\0\334\0\u0186\0\334\0\u0188\0\334\0\u01ad\0\334\0\3\0\62\0\0\0\u01be\0\62\0\74\0" +
		"\0\0\3\0\74\0\116\0\116\0\134\0\62\0\75\0\74\0\117\0\1\0\56\0\6\0\56\0\76\0\56\0" +
		"\132\0\56\0\253\0\335\0\321\0\56\0\325\0\335\0\341\0\335\0\343\0\335\0\355\0\335" +
		"\0\371\0\335\0\373\0\335\0\374\0\335\0\377\0\335\0\u0120\0\335\0\u0125\0\335\0\u0126" +
		"\0\u016a\0\u0127\0\335\0\u0128\0\335\0\u0129\0\335\0\u012b\0\335\0\u0133\0\u0174" +
		"\0\u0140\0\335\0\u0143\0\335\0\u0145\0\335\0\u0147\0\335\0\u0149\0\335\0\u014a\0" +
		"\335\0\u0167\0\u016a\0\u0168\0\u016a\0\u0169\0\u019c\0\u017f\0\335\0\u0183\0\335" +
		"\0\u0186\0\335\0\u0188\0\335\0\u019d\0\u016a\0\u019f\0\u016a\0\u01ad\0\335\0\123" +
		"\0\137\0\161\0\213\0\213\0\273\0\237\0\310\0\246\0\315\0\261\0\370\0\316\0\u0113" +
		"\0\326\0\u0122\0\364\0\u013c\0\u010f\0\310\0\u0115\0\137\0\u0118\0\u0159\0\u0119" +
		"\0\u015a\0\u0121\0\u0163\0\u0156\0\137\0\u0162\0\315\0\u0165\0\u0122\0\u0177\0\137" +
		"\0\u0179\0\u01a3\0\u0197\0\u0113\0\146\0\172\0\177\0\172\0\202\0\263\0\266\0\263" +
		"\0\272\0\263\0\u0102\0\263\0\131\0\147\0\176\0\226\0\131\0\150\0\147\0\175\0\176" +
		"\0\150\0\226\0\175\0\131\0\151\0\147\0\151\0\176\0\151\0\226\0\151\0\131\0\152\0" +
		"\147\0\152\0\176\0\152\0\226\0\152\0\131\0\153\0\147\0\153\0\176\0\153\0\226\0\153" +
		"\0\145\0\166\0\131\0\154\0\147\0\154\0\176\0\154\0\226\0\154\0\u0106\0\u014e\0\u0150" +
		"\0\u014e\0\u014d\0\u018c\0\131\0\155\0\147\0\155\0\176\0\155\0\226\0\155\0\162\0" +
		"\215\0\163\0\217\0\131\0\156\0\147\0\156\0\176\0\156\0\226\0\156\0\145\0\167\0\221" +
		"\0\277\0\162\0\216\0\163\0\216\0\275\0\u0104\0\160\0\203\0\160\0\204\0\203\0\270" +
		"\0\160\0\205\0\203\0\205\0\202\0\264\0\266\0\375\0\272\0\u0100\0\u0102\0\u014b\0" +
		"\256\0\365\0\367\0\365\0\200\0\246\0\200\0\247\0\160\0\206\0\203\0\206\0\160\0\207" +
		"\0\203\0\207\0\240\0\314\0\u0137\0\u0176\0\237\0\311\0\237\0\312\0\u010f\0\u0154" +
		"\0\246\0\316\0\u0162\0\u0197\0\364\0\u013d\0\253\0\336\0\325\0\336\0\371\0\336\0" +
		"\373\0\336\0\374\0\336\0\377\0\336\0\u0125\0\336\0\u0140\0\336\0\u0143\0\336\0\u0145" +
		"\0\336\0\u0147\0\336\0\u0149\0\336\0\u014a\0\336\0\u017f\0\336\0\u0183\0\336\0\u0186" +
		"\0\336\0\u0188\0\336\0\u01ad\0\336\0\253\0\337\0\325\0\u011f\0\371\0\u013f\0\373" +
		"\0\u0141\0\374\0\u0142\0\377\0\u0146\0\u0125\0\u0166\0\u0140\0\u017b\0\u0143\0\u017e" +
		"\0\u0145\0\u0180\0\u0147\0\u0182\0\u0149\0\u0184\0\u014a\0\u0185\0\u017f\0\u01a6" +
		"\0\u0183\0\u01a9\0\u0186\0\u01ac\0\u0188\0\u01ae\0\u01ad\0\u01b9\0\253\0\340\0\325" +
		"\0\340\0\371\0\340\0\373\0\340\0\374\0\340\0\377\0\340\0\u0125\0\340\0\u0129\0\u016f" +
		"\0\u0140\0\340\0\u0143\0\340\0\u0145\0\340\0\u0147\0\340\0\u0149\0\340\0\u014a\0" +
		"\340\0\u017f\0\340\0\u0183\0\340\0\u0186\0\340\0\u0188\0\340\0\u01ad\0\340\0\253" +
		"\0\341\0\325\0\341\0\371\0\341\0\373\0\341\0\374\0\341\0\377\0\341\0\u0125\0\341" +
		"\0\u0129\0\341\0\u0140\0\341\0\u0143\0\341\0\u0145\0\341\0\u0147\0\341\0\u0149\0" +
		"\341\0\u014a\0\341\0\u017f\0\341\0\u0183\0\341\0\u0186\0\341\0\u0188\0\341\0\u01ad" +
		"\0\341\0\253\0\342\0\325\0\342\0\341\0\342\0\343\0\342\0\371\0\342\0\373\0\342\0" +
		"\374\0\342\0\377\0\342\0\u0120\0\342\0\u0125\0\342\0\u0129\0\342\0\u012b\0\342\0" +
		"\u0140\0\342\0\u0143\0\342\0\u0145\0\342\0\u0147\0\342\0\u0149\0\342\0\u014a\0\342" +
		"\0\u017f\0\342\0\u0183\0\342\0\u0186\0\342\0\u0188\0\342\0\u01ad\0\342\0\202\0\265" +
		"\0\264\0\372\0\266\0\376\0\272\0\u0101\0\356\0\u0135\0\375\0\u0144\0\u0100\0\u0148" +
		"\0\u0102\0\u014c\0\u012c\0\u0171\0\u012f\0\u0172\0\u014b\0\u0187\0\u0170\0\u01a0" +
		"\0\357\0\u0138\0\u0176\0\u01a1\0\253\0\343\0\325\0\u0120\0\341\0\u012b\0\371\0\343" +
		"\0\373\0\343\0\374\0\343\0\377\0\343\0\u0125\0\343\0\u0129\0\343\0\u0140\0\343\0" +
		"\u0143\0\343\0\u0145\0\343\0\u0147\0\343\0\u0149\0\343\0\u014a\0\343\0\u017f\0\343" +
		"\0\u0183\0\343\0\u0186\0\343\0\u0188\0\343\0\u01ad\0\343\0\253\0\344\0\325\0\344" +
		"\0\341\0\344\0\343\0\u012e\0\371\0\344\0\373\0\344\0\374\0\344\0\377\0\344\0\u0120" +
		"\0\u012e\0\u0125\0\344\0\u0129\0\344\0\u012b\0\u012e\0\u0140\0\344\0\u0143\0\344" +
		"\0\u0145\0\344\0\u0147\0\344\0\u0149\0\344\0\u014a\0\344\0\u017f\0\344\0\u0183\0" +
		"\344\0\u0186\0\344\0\u0188\0\344\0\u01ad\0\344\0\326\0\u0123\0\253\0\345\0\325\0" +
		"\345\0\341\0\345\0\343\0\345\0\371\0\345\0\373\0\345\0\374\0\345\0\377\0\345\0\u0120" +
		"\0\345\0\u0125\0\345\0\u0129\0\345\0\u012b\0\345\0\u0140\0\345\0\u0143\0\345\0\u0145" +
		"\0\345\0\u0147\0\345\0\u0149\0\345\0\u014a\0\345\0\u017f\0\345\0\u0183\0\345\0\u0186" +
		"\0\345\0\u0188\0\345\0\u01ad\0\345\0\326\0\u0124\0\u0165\0\u0198\0\253\0\346\0\325" +
		"\0\346\0\341\0\346\0\343\0\346\0\371\0\346\0\373\0\346\0\374\0\346\0\377\0\346\0" +
		"\u0120\0\346\0\u0125\0\346\0\u0129\0\346\0\u012b\0\346\0\u0140\0\346\0\u0143\0\346" +
		"\0\u0145\0\346\0\u0147\0\346\0\u0149\0\346\0\u014a\0\346\0\u017f\0\346\0\u0183\0" +
		"\346\0\u0186\0\346\0\u0188\0\346\0\u01ad\0\346\0\253\0\347\0\325\0\347\0\341\0\347" +
		"\0\343\0\347\0\371\0\347\0\373\0\347\0\374\0\347\0\377\0\347\0\u0120\0\347\0\u0125" +
		"\0\347\0\u0129\0\347\0\u012b\0\347\0\u0140\0\347\0\u0143\0\347\0\u0145\0\347\0\u0147" +
		"\0\347\0\u0149\0\347\0\u014a\0\347\0\u017f\0\347\0\u0183\0\347\0\u0186\0\347\0\u0188" +
		"\0\347\0\u01ad\0\347\0\253\0\350\0\325\0\350\0\341\0\350\0\343\0\350\0\355\0\u0134" +
		"\0\371\0\350\0\373\0\350\0\374\0\350\0\377\0\350\0\u0120\0\350\0\u0125\0\350\0\u0129" +
		"\0\350\0\u012b\0\350\0\u0140\0\350\0\u0143\0\350\0\u0145\0\350\0\u0147\0\350\0\u0149" +
		"\0\350\0\u014a\0\350\0\u017f\0\350\0\u0183\0\350\0\u0186\0\350\0\u0188\0\350\0\u01ad" +
		"\0\350\0\253\0\351\0\325\0\351\0\341\0\351\0\343\0\351\0\355\0\351\0\371\0\351\0" +
		"\373\0\351\0\374\0\351\0\377\0\351\0\u0120\0\351\0\u0125\0\351\0\u0127\0\u016d\0" +
		"\u0128\0\u016e\0\u0129\0\351\0\u012b\0\351\0\u0140\0\351\0\u0143\0\351\0\u0145\0" +
		"\351\0\u0147\0\351\0\u0149\0\351\0\u014a\0\351\0\u017f\0\351\0\u0183\0\351\0\u0186" +
		"\0\351\0\u0188\0\351\0\u01ad\0\351\0\253\0\352\0\325\0\352\0\341\0\352\0\343\0\352" +
		"\0\355\0\352\0\371\0\352\0\373\0\352\0\374\0\352\0\377\0\352\0\u0120\0\352\0\u0125" +
		"\0\352\0\u0127\0\352\0\u0128\0\352\0\u0129\0\352\0\u012b\0\352\0\u0140\0\352\0\u0143" +
		"\0\352\0\u0145\0\352\0\u0147\0\352\0\u0149\0\352\0\u014a\0\352\0\u017f\0\352\0\u0183" +
		"\0\352\0\u0186\0\352\0\u0188\0\352\0\u01ad\0\352\0\253\0\353\0\325\0\353\0\341\0" +
		"\353\0\343\0\353\0\355\0\353\0\371\0\353\0\373\0\353\0\374\0\353\0\377\0\353\0\u0120" +
		"\0\353\0\u0125\0\353\0\u0127\0\353\0\u0128\0\353\0\u0129\0\353\0\u012b\0\353\0\u0140" +
		"\0\353\0\u0143\0\353\0\u0145\0\353\0\u0147\0\353\0\u0149\0\353\0\u014a\0\353\0\u017f" +
		"\0\353\0\u0183\0\353\0\u0186\0\353\0\u0188\0\353\0\u01ad\0\353\0\253\0\354\0\303" +
		"\0\u0108\0\304\0\u0109\0\325\0\354\0\341\0\354\0\343\0\354\0\355\0\354\0\371\0\354" +
		"\0\373\0\354\0\374\0\354\0\377\0\354\0\u010c\0\u0153\0\u0120\0\354\0\u0125\0\354" +
		"\0\u0127\0\354\0\u0128\0\354\0\u0129\0\354\0\u012b\0\354\0\u0140\0\354\0\u0143\0" +
		"\354\0\u0145\0\354\0\u0147\0\354\0\u0149\0\354\0\u014a\0\354\0\u017f\0\354\0\u0183" +
		"\0\354\0\u0186\0\354\0\u0188\0\354\0\u01ad\0\354\0\u0126\0\u016b\0\u0167\0\u016b" +
		"\0\u0168\0\u019b\0\u019d\0\u016b\0\u019f\0\u016b\0\u0126\0\u016c\0\u0167\0\u019a" +
		"\0\u019d\0\u01b4\0\u019f\0\u01b5\0\160\0\210\0\203\0\210\0\253\0\210\0\325\0\210" +
		"\0\341\0\210\0\343\0\210\0\371\0\210\0\373\0\210\0\374\0\210\0\377\0\210\0\u0120" +
		"\0\210\0\u0125\0\210\0\u0129\0\210\0\u012b\0\210\0\u0140\0\210\0\u0143\0\210\0\u0145" +
		"\0\210\0\u0147\0\210\0\u0149\0\210\0\u014a\0\210\0\u017f\0\210\0\u0183\0\210\0\u0186" +
		"\0\210\0\u0188\0\210\0\u01ad\0\210\0\160\0\211\0\203\0\211\0\253\0\355\0\325\0\355" +
		"\0\341\0\355\0\343\0\355\0\371\0\355\0\373\0\355\0\374\0\355\0\377\0\355\0\u0120" +
		"\0\355\0\u0125\0\355\0\u0129\0\355\0\u012b\0\355\0\u0140\0\355\0\u0143\0\355\0\u0145" +
		"\0\355\0\u0147\0\355\0\u0149\0\355\0\u014a\0\355\0\u017f\0\355\0\u0183\0\355\0\u0186" +
		"\0\355\0\u0188\0\355\0\u01ad\0\355\0\160\0\212\0\203\0\212\0\210\0\271\0\253\0\212" +
		"\0\325\0\212\0\341\0\212\0\343\0\212\0\371\0\212\0\373\0\212\0\374\0\212\0\377\0" +
		"\212\0\u0120\0\212\0\u0125\0\212\0\u0129\0\212\0\u012b\0\212\0\u0140\0\212\0\u0143" +
		"\0\212\0\u0145\0\212\0\u0147\0\212\0\u0149\0\212\0\u014a\0\212\0\u017f\0\212\0\u0183" +
		"\0\212\0\u0186\0\212\0\u0188\0\212\0\u01ad\0\212\0\255\0\361\0\202\0\266\0\272\0" +
		"\u0102\0\255\0\362\0\u013a\0\u0178\0\66\0\103\0\100\0\121\0\101\0\122\0\124\0\103" +
		"\0\255\0\363\0\324\0\u011c\0\u011b\0\u015b\0\u013a\0\363\0\u015e\0\u011c\0\u0160" +
		"\0\u011c\0\66\0\104\0\66\0\105\0\53\0\67\0\332\0\67\0\u0169\0\67\0\66\0\106\0\124" +
		"\0\141\0\200\0\250\0\247\0\317\0\123\0\140\0\u0115\0\u0158\0\u0156\0\u0191\0\u0177" +
		"\0\u01a2\0\324\0\u011d\0\u015e\0\u011d\0\u0160\0\u011d\0\324\0\u011e\0\u015e\0\u0195" +
		"\0\u0160\0\u0196\0\1\0\u01bf\0\6\0\63\0\76\0\120\0\132\0\157\0\321\0\u0117\0\6\0" +
		"\64\0\6\0\65\0\146\0\173\0\177\0\227\0\300\0\u0106\0\u0107\0\u0150\0\u0106\0\u014f" +
		"\0\u0150\0\u018f\0\u014f\0\u018e\0\u018f\0\u01b0\0\256\0\366\0\367\0\u013e\0\253" +
		"\0\356\0\325\0\356\0\341\0\u012c\0\343\0\u012f\0\371\0\356\0\373\0\356\0\374\0\356" +
		"\0\377\0\356\0\u0120\0\u012f\0\u0125\0\356\0\u0129\0\356\0\u012b\0\u0170\0\u0140" +
		"\0\356\0\u0143\0\356\0\u0145\0\356\0\u0147\0\356\0\u0149\0\356\0\u014a\0\356\0\u017f" +
		"\0\356\0\u0183\0\356\0\u0186\0\356\0\u0188\0\356\0\u01ad\0\356\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(259,
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0" +
		"\1\0\1\0\1\0\2\0\0\0\5\0\4\0\2\0\0\0\6\0\3\0\3\0\3\0\4\0\3\0\3\0\1\0\2\0\1\0\1\0" +
		"\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\4\0\3\0\3\0\3\0\1\0\10\0\4\0\7\0\3\0\3\0" +
		"\1\0\1\0\1\0\5\0\3\0\1\0\4\0\4\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\10\0\7\0\7\0\6\0" +
		"\7\0\6\0\6\0\5\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0\4\0\2\0\3\0\2\0\1\0\1\0\1\0\2\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0\4\0\5\0\5\0\6\0\4\0\4\0\3\0\1\0\3\0" +
		"\1\0\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\4\0\3\0\3\0\2\0\3\0\2\0\2\0\1\0\1\0\3\0" +
		"\3\0\3\0\5\0\4\0\3\0\2\0\2\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\2\0\1\0\2\0" +
		"\1\0\2\0\1\0\3\0\3\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0" +
		"\1\0\3\0\2\0\1\0\3\0\3\0\2\0\1\0\1\0\4\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0" +
		"\1\0\1\0\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0" +
		"\1\0\3\0\1\0\3\0\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(259,
		"\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121" +
		"\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0" +
		"\121\0\121\0\121\0\121\0\121\0\121\0\122\0\122\0\122\0\122\0\123\0\124\0\124\0\125" +
		"\0\126\0\127\0\130\0\130\0\131\0\131\0\132\0\132\0\133\0\133\0\134\0\135\0\136\0" +
		"\136\0\137\0\137\0\140\0\140\0\141\0\142\0\143\0\143\0\143\0\144\0\144\0\144\0\144" +
		"\0\144\0\145\0\146\0\147\0\147\0\150\0\150\0\151\0\151\0\151\0\151\0\152\0\153\0" +
		"\153\0\153\0\154\0\155\0\155\0\156\0\156\0\157\0\160\0\161\0\161\0\161\0\162\0\162" +
		"\0\162\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0" +
		"\163\0\163\0\163\0\163\0\164\0\164\0\164\0\164\0\164\0\164\0\165\0\166\0\166\0\166" +
		"\0\167\0\167\0\167\0\170\0\170\0\170\0\170\0\171\0\171\0\171\0\171\0\171\0\171\0" +
		"\171\0\171\0\172\0\172\0\173\0\173\0\174\0\174\0\175\0\175\0\176\0\176\0\177\0\177" +
		"\0\200\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\202\0\203\0\203\0" +
		"\204\0\204\0\204\0\204\0\205\0\206\0\206\0\206\0\207\0\207\0\207\0\207\0\210\0\210" +
		"\0\211\0\212\0\212\0\213\0\214\0\214\0\215\0\215\0\215\0\216\0\216\0\217\0\217\0" +
		"\217\0\220\0\220\0\220\0\220\0\220\0\220\0\220\0\220\0\221\0\222\0\222\0\222\0\222" +
		"\0\223\0\223\0\223\0\224\0\224\0\225\0\226\0\226\0\226\0\227\0\227\0\230\0\231\0" +
		"\231\0\231\0\232\0\233\0\233\0\234\0\234\0\235\0\236\0\236\0\236\0\236\0\237\0\237" +
		"\0\240\0\240\0\241\0\241\0\241\0\241\0\242\0\242\0\242\0\243\0\243\0\243\0\243\0" +
		"\244\0\244\0\245\0\245\0\246\0\246\0\247\0\247\0\250\0\250\0\251\0\251\0\252\0\252" +
		"\0\253\0\253\0");

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
		"'as'",
		"'false'",
		"'implements'",
		"'import'",
		"'separator'",
		"'set'",
		"'true'",
		"'assert'",
		"'brackets'",
		"'class'",
		"'empty'",
		"'expect'",
		"'expect-rr'",
		"'explicit'",
		"'flag'",
		"'generate'",
		"'global'",
		"'inline'",
		"'input'",
		"'interface'",
		"'lalr'",
		"'language'",
		"'layout'",
		"'left'",
		"'lexer'",
		"'lookahead'",
		"'no-eoi'",
		"'nonassoc'",
		"'nonempty'",
		"'param'",
		"'parser'",
		"'prec'",
		"'returns'",
		"'right'",
		"'s'",
		"'shift'",
		"'space'",
		"'void'",
		"'x'",
		"code",
		"'{'",
		"regexp",
		"'/'",
		"identifier",
		"literal",
		"pattern",
		"qualified_id",
		"name",
		"command",
		"syntax_problem",
		"import__optlist",
		"input",
		"option_optlist",
		"header",
		"lexer_section",
		"parser_section",
		"import_",
		"option",
		"symref",
		"symref_noargs",
		"rawType",
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
		"rhsSuffix",
		"reportClause",
		"reportAs",
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
		"argument_list_Comma_separatedopt",
		"symref_args",
		"argument",
		"param_type",
		"param_value",
		"predicate_primary",
		"predicate_expression",
		"expression",
		"expression_list_Comma_separated",
		"expression_list_Comma_separatedopt",
		"rawTypeopt",
		"iconopt",
		"lexeme_attrsopt",
		"commandopt",
		"implements_clauseopt",
		"rhsSuffixopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int identifier = 81;
		int literal = 82;
		int pattern = 83;
		int qualified_id = 84;
		int name = 85;
		int command = 86;
		int syntax_problem = 87;
		int import__optlist = 88;
		int input1 = 89;
		int option_optlist = 90;
		int header = 91;
		int lexer_section = 92;
		int parser_section = 93;
		int import_ = 94;
		int option = 95;
		int symref = 96;
		int symref_noargs = 97;
		int rawType = 98;
		int lexer_parts = 99;
		int lexer_part = 100;
		int named_pattern = 101;
		int start_conditions_scope = 102;
		int start_conditions = 103;
		int stateref_list_Comma_separated = 104;
		int lexeme = 105;
		int lexeme_attrs = 106;
		int lexeme_attribute = 107;
		int brackets_directive = 108;
		int lexer_state_list_Comma_separated = 109;
		int states_clause = 110;
		int stateref = 111;
		int lexer_state = 112;
		int grammar_parts = 113;
		int grammar_part = 114;
		int nonterm = 115;
		int nonterm_type = 116;
		int implements_clause = 117;
		int assoc = 118;
		int param_modifier = 119;
		int template_param = 120;
		int directive = 121;
		int identifier_list_Comma_separated = 122;
		int inputref_list_Comma_separated = 123;
		int inputref = 124;
		int references = 125;
		int references_cs = 126;
		int rule0_list_Or_separated = 127;
		int rules = 128;
		int rule0 = 129;
		int predicate = 130;
		int rhsSuffix = 131;
		int reportClause = 132;
		int reportAs = 133;
		int rhsParts = 134;
		int rhsPart = 135;
		int lookahead_predicate_list_And_separated = 136;
		int rhsLookahead = 137;
		int lookahead_predicate = 138;
		int rhsStateMarker = 139;
		int rhsAnnotated = 140;
		int rhsAssignment = 141;
		int rhsOptional = 142;
		int rhsCast = 143;
		int rhsPrimary = 144;
		int rhsSet = 145;
		int setPrimary = 146;
		int setExpression = 147;
		int annotation_list = 148;
		int annotations = 149;
		int annotation = 150;
		int nonterm_param_list_Comma_separated = 151;
		int nonterm_params = 152;
		int nonterm_param = 153;
		int param_ref = 154;
		int argument_list_Comma_separated = 155;
		int argument_list_Comma_separatedopt = 156;
		int symref_args = 157;
		int argument = 158;
		int param_type = 159;
		int param_value = 160;
		int predicate_primary = 161;
		int predicate_expression = 162;
		int expression = 163;
		int expression_list_Comma_separated = 164;
		int expression_list_Comma_separatedopt = 165;
		int rawTypeopt = 166;
		int iconopt = 167;
		int lexeme_attrsopt = 168;
		int commandopt = 169;
		int implements_clauseopt = 170;
		int rhsSuffixopt = 171;
	}

	public interface Rules {
		int nonterm_type_nontermTypeAST = 112;  // nonterm_type : 'returns' symref_noargs
		int nonterm_type_nontermTypeHint = 113;  // nonterm_type : 'inline' 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint2 = 114;  // nonterm_type : 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint3 = 115;  // nonterm_type : 'interface'
		int nonterm_type_nontermTypeHint4 = 116;  // nonterm_type : 'void'
		int directive_directivePrio = 129;  // directive : '%' assoc references ';'
		int directive_directiveInput = 130;  // directive : '%' 'input' inputref_list_Comma_separated ';'
		int directive_directiveInterface = 131;  // directive : '%' 'interface' identifier_list_Comma_separated ';'
		int directive_directiveAssert = 132;  // directive : '%' 'assert' 'empty' rhsSet ';'
		int directive_directiveAssert2 = 133;  // directive : '%' 'assert' 'nonempty' rhsSet ';'
		int directive_directiveSet = 134;  // directive : '%' 'generate' identifier '=' rhsSet ';'
		int directive_directiveExpect = 135;  // directive : '%' 'expect' icon ';'
		int directive_directiveExpectRR = 136;  // directive : '%' 'expect-rr' icon ';'
		int rhsOptional_rhsQuantifier = 186;  // rhsOptional : rhsCast '?'
		int rhsCast_rhsAsLiteral = 189;  // rhsCast : rhsPrimary 'as' literal
		int rhsPrimary_rhsSymbol = 190;  // rhsPrimary : symref
		int rhsPrimary_rhsNested = 191;  // rhsPrimary : '(' rules ')'
		int rhsPrimary_rhsList = 192;  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
		int rhsPrimary_rhsList2 = 193;  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
		int rhsPrimary_rhsQuantifier = 194;  // rhsPrimary : rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 195;  // rhsPrimary : rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 196;  // rhsPrimary : '$' '(' rules ')'
		int setPrimary_setSymbol = 199;  // setPrimary : identifier symref
		int setPrimary_setSymbol2 = 200;  // setPrimary : symref
		int setPrimary_setCompound = 201;  // setPrimary : '(' setExpression ')'
		int setPrimary_setComplement = 202;  // setPrimary : '~' setPrimary
		int setExpression_setBinary = 204;  // setExpression : setExpression '|' setExpression
		int setExpression_setBinary2 = 205;  // setExpression : setExpression '&' setExpression
		int nonterm_param_inlineParameter = 216;  // nonterm_param : identifier identifier '=' param_value
		int nonterm_param_inlineParameter2 = 217;  // nonterm_param : identifier identifier
		int predicate_primary_boolPredicate = 232;  // predicate_primary : '!' param_ref
		int predicate_primary_boolPredicate2 = 233;  // predicate_primary : param_ref
		int predicate_primary_comparePredicate = 234;  // predicate_primary : param_ref '==' literal
		int predicate_primary_comparePredicate2 = 235;  // predicate_primary : param_ref '!=' literal
		int predicate_expression_predicateBinary = 237;  // predicate_expression : predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 238;  // predicate_expression : predicate_expression '||' predicate_expression
		int expression_array = 241;  // expression : '[' expression_list_Comma_separatedopt ']'
	}

	// set(follow error)
	private static int[] afterErr = {
		6, 7, 8, 13, 14, 15, 18, 19, 20, 21, 22, 23, 24, 34, 35, 36,
		37, 42, 43, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
		58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73,
		74, 75, 76, 77
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
			case 0:  // identifier : ID
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 1:  // identifier : 'brackets'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 2:  // identifier : 'inline'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 3:  // identifier : 'prec'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 4:  // identifier : 'shift'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 5:  // identifier : 'returns'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 6:  // identifier : 'input'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 7:  // identifier : 'left'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // identifier : 'right'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 9:  // identifier : 'nonassoc'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 10:  // identifier : 'generate'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // identifier : 'assert'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 12:  // identifier : 'empty'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 13:  // identifier : 'nonempty'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 14:  // identifier : 'global'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 15:  // identifier : 'explicit'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 16:  // identifier : 'lookahead'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 17:  // identifier : 'param'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // identifier : 'flag'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // identifier : 'no-eoi'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 20:  // identifier : 's'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 21:  // identifier : 'x'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 22:  // identifier : 'expect'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 23:  // identifier : 'expect-rr'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 24:  // identifier : 'class'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 25:  // identifier : 'interface'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 26:  // identifier : 'void'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 27:  // identifier : 'space'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 28:  // identifier : 'layout'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 29:  // identifier : 'language'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // identifier : 'lalr'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // identifier : 'lexer'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // identifier : 'parser'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 33:  // literal : scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 34:  // literal : icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 35:  // literal : 'true'
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 36:  // literal : 'false'
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // pattern : regexp
				tmLeft.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 38:  // qualified_id : identifier
				{ tmLeft.value = ((TmaIdentifier)tmStack[tmHead].value).getText(); }
				break;
			case 39:  // qualified_id : qualified_id '.' identifier
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((TmaIdentifier)tmStack[tmHead].value).getText(); }
				break;
			case 40:  // name : qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 41:  // command : code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 42:  // syntax_problem : error
				tmLeft.value = new TmaSyntaxProblem(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 43:  // import__optlist : import__optlist import_
				((List<TmaImport>)tmLeft.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 44:  // import__optlist :
				tmLeft.value = new ArrayList();
				break;
			case 45:  // input : header import__optlist option_optlist lexer_section parser_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexer */,
						((List<ITmaGrammarPart>)tmStack[tmHead].value) /* parser */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 46:  // input : header import__optlist option_optlist lexer_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 3].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 2].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 1].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead].value) /* lexer */,
						null /* parser */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 47:  // option_optlist : option_optlist option
				((List<TmaOption>)tmLeft.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 48:  // option_optlist :
				tmLeft.value = new ArrayList();
				break;
			case 49:  // header : 'language' name '(' name ')' ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 4].value) /* name */,
						((TmaName)tmStack[tmHead - 2].value) /* target */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // header : 'language' name ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 1].value) /* name */,
						null /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 51:  // lexer_section : '::' 'lexer' lexer_parts
				tmLeft.value = ((List<ITmaLexerPart>)tmStack[tmHead].value);
				break;
			case 52:  // parser_section : '::' 'parser' grammar_parts
				tmLeft.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value);
				break;
			case 53:  // import_ : 'import' identifier scon ';'
				tmLeft.value = new TmaImport(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // import_ : 'import' scon ';'
				tmLeft.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 55:  // option : identifier '=' expression
				tmLeft.value = new TmaOption(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* key */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 56:  // option : syntax_problem
				tmLeft.value = new TmaOption(
						null /* key */,
						null /* value */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 57:  // symref : identifier symref_args
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((TmaSymrefArgs)tmStack[tmHead].value) /* args */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // symref : identifier
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // symref_noargs : identifier
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // rawType : code
				tmLeft.value = new TmaRawType(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 61:  // lexer_parts : lexer_part
				tmLeft.value = new ArrayList();
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 62:  // lexer_parts : lexer_parts lexer_part
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 63:  // lexer_parts : lexer_parts syntax_problem
				((List<ITmaLexerPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 69:  // named_pattern : identifier '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 70:  // start_conditions_scope : start_conditions '{' lexer_parts '}'
				tmLeft.value = new TmaStartConditionsScope(
						((TmaStartConditions)tmStack[tmHead - 3].value) /* startConditions */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexerParts */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // start_conditions : '<' '*' '>'
				tmLeft.value = new TmaStartConditions(
						null /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 72:  // start_conditions : '<' stateref_list_Comma_separated '>'
				tmLeft.value = new TmaStartConditions(
						((List<TmaStateref>)tmStack[tmHead - 1].value) /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 73:  // stateref_list_Comma_separated : stateref_list_Comma_separated ',' stateref
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 74:  // stateref_list_Comma_separated : stateref
				tmLeft.value = new ArrayList();
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 75:  // lexeme : start_conditions identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
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
			case 76:  // lexeme : start_conditions identifier rawTypeopt ':'
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
			case 77:  // lexeme : identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
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
			case 78:  // lexeme : identifier rawTypeopt ':'
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
			case 79:  // lexeme_attrs : '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // lexeme_attribute : 'class'
				tmLeft.value = TmaLexemeAttribute.CLASS;
				break;
			case 81:  // lexeme_attribute : 'space'
				tmLeft.value = TmaLexemeAttribute.SPACE;
				break;
			case 82:  // lexeme_attribute : 'layout'
				tmLeft.value = TmaLexemeAttribute.LAYOUT;
				break;
			case 83:  // brackets_directive : '%' 'brackets' symref_noargs symref_noargs ';'
				tmLeft.value = new TmaBracketsDirective(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 84:  // lexer_state_list_Comma_separated : lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 85:  // lexer_state_list_Comma_separated : lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 86:  // states_clause : '%' 's' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						false /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 87:  // states_clause : '%' 'x' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						true /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 88:  // stateref : identifier
				tmLeft.value = new TmaStateref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // lexer_state : identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // grammar_parts : grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 91:  // grammar_parts : grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 92:  // grammar_parts : grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 96:  // nonterm : annotations identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 7].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 97:  // nonterm : annotations identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 98:  // nonterm : annotations identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // nonterm : annotations identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // nonterm : annotations identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // nonterm : annotations identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // nonterm : annotations identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // nonterm : annotations identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // nonterm : identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // nonterm : identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // nonterm : identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // nonterm : identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // nonterm : identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // nonterm : identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // nonterm : identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // nonterm : identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // nonterm_type : 'returns' symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // nonterm_type : 'inline' 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // nonterm_type : 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // nonterm_type : 'interface'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.INTERFACE /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // nonterm_type : 'void'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.VOID /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // implements_clause : 'implements' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 119:  // assoc : 'left'
				tmLeft.value = TmaAssoc.LEFT;
				break;
			case 120:  // assoc : 'right'
				tmLeft.value = TmaAssoc.RIGHT;
				break;
			case 121:  // assoc : 'nonassoc'
				tmLeft.value = TmaAssoc.NONASSOC;
				break;
			case 122:  // param_modifier : 'explicit'
				tmLeft.value = TmaParamModifier.EXPLICIT;
				break;
			case 123:  // param_modifier : 'global'
				tmLeft.value = TmaParamModifier.GLOBAL;
				break;
			case 124:  // param_modifier : 'lookahead'
				tmLeft.value = TmaParamModifier.LOOKAHEAD;
				break;
			case 125:  // template_param : '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 126:  // template_param : '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // template_param : '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // template_param : '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // directive : '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // directive : '%' 'input' inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 131:  // directive : '%' 'interface' identifier_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInterface(
						((List<TmaIdentifier>)tmStack[tmHead - 1].value) /* ids */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // directive : '%' 'assert' 'empty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.EMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // directive : '%' 'assert' 'nonempty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.NONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // directive : '%' 'generate' identifier '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // directive : '%' 'expect' icon ';'
				tmLeft.value = new TmaDirectiveExpect(
						((Integer)tmStack[tmHead - 1].value) /* icon */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // directive : '%' 'expect-rr' icon ';'
				tmLeft.value = new TmaDirectiveExpectRR(
						((Integer)tmStack[tmHead - 1].value) /* icon */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 137:  // identifier_list_Comma_separated : identifier_list_Comma_separated ',' identifier
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 138:  // identifier_list_Comma_separated : identifier
				tmLeft.value = new ArrayList();
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 139:  // inputref_list_Comma_separated : inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 140:  // inputref_list_Comma_separated : inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 141:  // inputref : symref_noargs 'no-eoi'
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // inputref : symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // references : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 144:  // references : references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 145:  // references_cs : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 146:  // references_cs : references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 147:  // rule0_list_Or_separated : rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 148:  // rule0_list_Or_separated : rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 150:  // rule0 : predicate rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rule0 : predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rule0 : predicate rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rule0 : predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rule0 : rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rule0 : rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rule0 : rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // rule0 : rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // rule0 : syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						null /* suffix */,
						null /* action */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // predicate : '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 160:  // rhsSuffix : '%' 'prec' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.PREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // rhsSuffix : '%' 'shift' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.SHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // reportClause : '->' identifier '/' identifier_list_Comma_separated reportAs
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* action */,
						((List<TmaIdentifier>)tmStack[tmHead - 1].value) /* flags */,
						((TmaReportAs)tmStack[tmHead].value) /* reportAs */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // reportClause : '->' identifier '/' identifier_list_Comma_separated
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((List<TmaIdentifier>)tmStack[tmHead].value) /* flags */,
						null /* reportAs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // reportClause : '->' identifier reportAs
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* action */,
						null /* flags */,
						((TmaReportAs)tmStack[tmHead].value) /* reportAs */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // reportClause : '->' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead].value) /* action */,
						null /* flags */,
						null /* reportAs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 166:  // reportAs : 'as' identifier
				tmLeft.value = new TmaReportAs(
						((TmaIdentifier)tmStack[tmHead].value) /* identifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 167:  // rhsParts : rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 168:  // rhsParts : rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 169:  // rhsParts : rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 174:  // lookahead_predicate_list_And_separated : lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 175:  // lookahead_predicate_list_And_separated : lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 176:  // rhsLookahead : '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // lookahead_predicate : '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 178:  // lookahead_predicate : symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 179:  // rhsStateMarker : '.' identifier
				tmLeft.value = new TmaRhsStateMarker(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 181:  // rhsAnnotated : annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 183:  // rhsAssignment : identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 184:  // rhsAssignment : identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // rhsOptional : rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 188:  // rhsCast : rhsPrimary 'as' symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 189:  // rhsCast : rhsPrimary 'as' literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 190:  // rhsPrimary : symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // rhsPrimary : '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 194:  // rhsPrimary : rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // rhsPrimary : rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // rhsPrimary : '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 198:  // rhsSet : 'set' '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 199:  // setPrimary : identifier symref
				tmLeft.value = new TmaSetSymbol(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 200:  // setPrimary : symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 201:  // setPrimary : '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // setPrimary : '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 204:  // setExpression : setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 205:  // setExpression : setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 206:  // annotation_list : annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 207:  // annotation_list : annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 208:  // annotations : annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 209:  // annotation : '@' identifier '=' expression
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 210:  // annotation : '@' identifier
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 211:  // annotation : '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 212:  // nonterm_param_list_Comma_separated : nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 213:  // nonterm_param_list_Comma_separated : nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 214:  // nonterm_params : '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 216:  // nonterm_param : identifier identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 217:  // nonterm_param : identifier identifier
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 218:  // param_ref : identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 219:  // argument_list_Comma_separated : argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 220:  // argument_list_Comma_separated : argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 223:  // symref_args : '<' argument_list_Comma_separatedopt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 224:  // argument : param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 225:  // argument : '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 226:  // argument : '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 227:  // argument : param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 228:  // param_type : 'flag'
				tmLeft.value = TmaParamType.FLAG;
				break;
			case 229:  // param_type : 'param'
				tmLeft.value = TmaParamType.PARAM;
				break;
			case 232:  // predicate_primary : '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 233:  // predicate_primary : param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 234:  // predicate_primary : param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 235:  // predicate_primary : param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 237:  // predicate_expression : predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 238:  // predicate_expression : predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 241:  // expression : '[' expression_list_Comma_separatedopt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 243:  // expression_list_Comma_separated : expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 244:  // expression_list_Comma_separated : expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
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
		return (TmaInput1) parse(lexer, 0, 448);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 449);
	}
}
