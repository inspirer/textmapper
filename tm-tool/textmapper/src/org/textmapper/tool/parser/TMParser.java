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
	private static final int[] tmAction = TMLexer.unpack_int(447,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\40\0\41\0\uffb5\uffff\51\0\0\0\43" +
		"\0\42\0\13\0\1\0\27\0\14\0\17\0\22\0\12\0\16\0\2\0\6\0\30\0\35\0\34\0\33\0\7\0\36" +
		"\0\20\0\23\0\11\0\15\0\21\0\37\0\3\0\5\0\10\0\24\0\4\0\26\0\32\0\31\0\25\0\uff65" +
		"\uffff\353\0\356\0\354\0\45\0\ufef3\uffff\ufee7\uffff\ufedf\uffff\360\0\ufe97\uffff" +
		"\uffff\uffff\ufe91\uffff\71\0\uffff\uffff\uffff\uffff\uffff\uffff\363\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\52\0\uffff\uffff\355\0\uffff\uffff\uffff\uffff\326\0\ufe49" +
		"\uffff\ufe41\uffff\uffff\uffff\330\0\46\0\uffff\uffff\uffff\uffff\61\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\70\0\ufe3b\uffff\56\0\357\0\335\0\336\0\uffff" +
		"\uffff\uffff\uffff\333\0\ufe35\uffff\uffff\uffff\66\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\54\0\73\0\342\0\343\0\334\0\327\0\uffff\uffff\64\0\65\0\uffff" +
		"\uffff\uffff\uffff\ufe2f\uffff\ufe27\uffff\75\0\100\0\104\0\uffff\uffff\101\0\103" +
		"\0\102\0\67\0\uffff\uffff\60\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\131" +
		"\0\uffff\uffff\112\0\uffff\uffff\74\0\365\0\uffff\uffff\77\0\76\0\uffff\uffff\ufddb" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufdd5\uffff\133\0\136\0\137\0\140\0\ufd8b" +
		"\uffff\uffff\uffff\313\0\uffff\uffff\132\0\uffff\uffff\126\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\44\0\105\0\ufd41\uffff\uffff\uffff\uffff\uffff\uffff\uffff\173\0\340" +
		"\0\uffff\uffff\174\0\uffff\uffff\uffff\uffff\170\0\175\0\172\0\341\0\171\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\ufcf1\uffff\317\0\ufca5\uffff\uffff\uffff\uffff\uffff" +
		"\ufc49\uffff\uffff\uffff\164\0\uffff\uffff\165\0\166\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\135\0\134\0\312\0\uffff\uffff\uffff\uffff\127\0\uffff\uffff\130\0\111\0\ufc41" +
		"\uffff\106\0\ufbed\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufb9d\uffff\uffff\uffff" +
		"\213\0\211\0\uffff\uffff\216\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\ufb95\uffff\uffff\uffff\uffff\uffff\uffff\uffff\50" +
		"\0\ufb39\uffff\247\0\235\0\272\0\ufacd\uffff\uffff\uffff\223\0\ufac5\uffff\377\0" +
		"\ufa6b\uffff\243\0\251\0\250\0\246\0\260\0\262\0\ufa0f\uffff\uf9af\uffff\301\0\uffff" +
		"\uffff\uf949\uffff\uf93f\uffff\uf933\uffff\uffff\uffff\321\0\323\0\uffff\uffff\375" +
		"\0\163\0\uf8ed\uffff\161\0\uf8e5\uffff\uffff\uffff\uf889\uffff\uf82d\uffff\uffff" +
		"\uffff\uffff\uffff\uf7d1\uffff\uffff\uffff\uffff\uffff\uffff\uffff\124\0\125\0\367" +
		"\0\uf775\uffff\uf723\uffff\uffff\uffff\uffff\uffff\uffff\uffff\214\0\203\0\uffff" +
		"\uffff\204\0\uffff\uffff\202\0\217\0\uffff\uffff\uffff\uffff\201\0\315\0\uffff\uffff" +
		"\uffff\uffff\257\0\uffff\uffff\uf6cf\uffff\350\0\uffff\uffff\uffff\uffff\uf6c3\uffff" +
		"\uffff\uffff\256\0\uffff\uffff\253\0\uf667\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uf60b\uffff\160\0\uf5ad\uffff\uf551\uffff\245\0\244\0\uf547\uffff\266\0\276\0\277" +
		"\0\uffff\uffff\261\0\233\0\uffff\uffff\uf53d\uffff\uffff\uffff\322\0\220\0\uf535" +
		"\uffff\162\0\uffff\uffff\uf52d\uffff\uffff\uffff\uffff\uffff\uf4d1\uffff\uffff\uffff" +
		"\uf475\uffff\uffff\uffff\uf419\uffff\uffff\uffff\uf3bd\uffff\uf361\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\371\0\uf305\uffff\uf2b5\uffff\205\0\206\0\uffff\uffff\212" +
		"\0\210\0\uffff\uffff\177\0\uffff\uffff\237\0\240\0\344\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\236\0\uffff\uffff\273\0\uffff\uffff\255\0\254\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uf263\uffff\304\0\307\0\uffff\uffff\263\0\264\0\222\0\uf219" +
		"\uffff\227\0\231\0\271\0\270\0\241\0\uffff\uffff\320\0\uffff\uffff\156\0\uffff\uffff" +
		"\157\0\154\0\uffff\uffff\uf20f\uffff\uffff\uffff\150\0\uffff\uffff\uf1b3\uffff\uffff" +
		"\uffff\uffff\uffff\uf157\uffff\uffff\uffff\uf0fb\uffff\121\0\123\0\120\0\122\0\uffff" +
		"\uffff\373\0\115\0\uf09f\uffff\207\0\uffff\uffff\200\0\346\0\347\0\uf04f\uffff\uf047" +
		"\uffff\uffff\uffff\252\0\300\0\uffff\uffff\306\0\303\0\uffff\uffff\302\0\uffff\uffff" +
		"\225\0\324\0\221\0\155\0\152\0\uffff\uffff\153\0\146\0\uffff\uffff\147\0\144\0\uffff" +
		"\uffff\uf03f\uffff\uffff\uffff\117\0\113\0\176\0\uffff\uffff\305\0\uefe3\uffff\uefdb" +
		"\uffff\151\0\145\0\142\0\uffff\uffff\143\0\275\0\274\0\141\0\uffff\uffff\uffff\uffff" +
		"\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4138,
		"\7\0\53\0\44\0\53\0\45\0\53\0\51\0\53\0\55\0\53\0\56\0\53\0\57\0\53\0\60\0\53\0\61" +
		"\0\53\0\62\0\53\0\63\0\53\0\64\0\53\0\65\0\53\0\66\0\53\0\67\0\53\0\70\0\53\0\71" +
		"\0\53\0\72\0\53\0\73\0\53\0\74\0\53\0\75\0\53\0\76\0\53\0\77\0\53\0\100\0\53\0\101" +
		"\0\53\0\102\0\53\0\103\0\53\0\104\0\53\0\105\0\53\0\106\0\53\0\107\0\53\0\110\0\53" +
		"\0\111\0\53\0\112\0\53\0\113\0\53\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\2\0\uffff" +
		"\uffff\21\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\47\0\uffff\uffff\54\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\22\0\362\0\uffff\uffff\ufffe\uffff\30" +
		"\0\uffff\uffff\0\0\72\0\6\0\72\0\7\0\72\0\10\0\72\0\15\0\72\0\16\0\72\0\17\0\72\0" +
		"\22\0\72\0\23\0\72\0\24\0\72\0\25\0\72\0\26\0\72\0\32\0\72\0\33\0\72\0\35\0\72\0" +
		"\40\0\72\0\42\0\72\0\43\0\72\0\44\0\72\0\45\0\72\0\46\0\72\0\52\0\72\0\53\0\72\0" +
		"\55\0\72\0\56\0\72\0\57\0\72\0\60\0\72\0\61\0\72\0\62\0\72\0\63\0\72\0\64\0\72\0" +
		"\65\0\72\0\66\0\72\0\67\0\72\0\70\0\72\0\71\0\72\0\72\0\72\0\73\0\72\0\74\0\72\0" +
		"\75\0\72\0\76\0\72\0\77\0\72\0\100\0\72\0\101\0\72\0\102\0\72\0\103\0\72\0\104\0" +
		"\72\0\105\0\72\0\106\0\72\0\107\0\72\0\110\0\72\0\111\0\72\0\112\0\72\0\113\0\72" +
		"\0\114\0\72\0\uffff\uffff\ufffe\uffff\16\0\uffff\uffff\15\0\47\0\23\0\47\0\26\0\47" +
		"\0\70\0\47\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\70\0\uffff\uffff\15\0\364\0" +
		"\uffff\uffff\ufffe\uffff\51\0\uffff\uffff\7\0\57\0\44\0\57\0\45\0\57\0\55\0\57\0" +
		"\56\0\57\0\57\0\57\0\60\0\57\0\61\0\57\0\62\0\57\0\63\0\57\0\64\0\57\0\65\0\57\0" +
		"\66\0\57\0\67\0\57\0\70\0\57\0\71\0\57\0\72\0\57\0\73\0\57\0\74\0\57\0\75\0\57\0" +
		"\76\0\57\0\77\0\57\0\100\0\57\0\101\0\57\0\102\0\57\0\103\0\57\0\104\0\57\0\105\0" +
		"\57\0\106\0\57\0\107\0\57\0\110\0\57\0\111\0\57\0\112\0\57\0\113\0\57\0\uffff\uffff" +
		"\ufffe\uffff\17\0\uffff\uffff\22\0\361\0\uffff\uffff\ufffe\uffff\33\0\uffff\uffff" +
		"\37\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\31\0\332\0\uffff" +
		"\uffff\ufffe\uffff\20\0\uffff\uffff\17\0\337\0\31\0\337\0\uffff\uffff\ufffe\uffff" +
		"\17\0\uffff\uffff\31\0\331\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\0\0\55\0\uffff" +
		"\uffff\ufffe\uffff\70\0\uffff\uffff\15\0\364\0\uffff\uffff\ufffe\uffff\12\0\uffff" +
		"\uffff\114\0\uffff\uffff\20\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\30\0" +
		"\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\0\0\62\0\7\0\62\0\uffff\uffff\ufffe\uffff\114\0\uffff\uffff\20\0\366\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\0\0\63\0\uffff\uffff\ufffe\uffff\43\0" +
		"\uffff\uffff\23\0\314\0\42\0\314\0\45\0\314\0\53\0\314\0\55\0\314\0\56\0\314\0\57" +
		"\0\314\0\60\0\314\0\61\0\314\0\62\0\314\0\63\0\314\0\64\0\314\0\65\0\314\0\66\0\314" +
		"\0\67\0\314\0\70\0\314\0\71\0\314\0\72\0\314\0\73\0\314\0\74\0\314\0\75\0\314\0\76" +
		"\0\314\0\77\0\314\0\100\0\314\0\101\0\314\0\102\0\314\0\103\0\314\0\104\0\314\0\105" +
		"\0\314\0\106\0\314\0\107\0\314\0\110\0\314\0\111\0\314\0\112\0\314\0\113\0\314\0" +
		"\uffff\uffff\ufffe\uffff\116\0\uffff\uffff\0\0\116\0\6\0\116\0\7\0\116\0\27\0\116" +
		"\0\30\0\116\0\44\0\116\0\45\0\116\0\55\0\116\0\56\0\116\0\57\0\116\0\60\0\116\0\61" +
		"\0\116\0\62\0\116\0\63\0\116\0\64\0\116\0\65\0\116\0\66\0\116\0\67\0\116\0\70\0\116" +
		"\0\71\0\116\0\72\0\116\0\73\0\116\0\74\0\116\0\75\0\116\0\76\0\116\0\77\0\116\0\100" +
		"\0\116\0\101\0\116\0\102\0\116\0\103\0\116\0\104\0\116\0\105\0\116\0\106\0\116\0" +
		"\107\0\116\0\110\0\116\0\111\0\116\0\112\0\116\0\113\0\116\0\uffff\uffff\ufffe\uffff" +
		"\12\0\uffff\uffff\23\0\316\0\42\0\316\0\43\0\316\0\45\0\316\0\53\0\316\0\55\0\316" +
		"\0\56\0\316\0\57\0\316\0\60\0\316\0\61\0\316\0\62\0\316\0\63\0\316\0\64\0\316\0\65" +
		"\0\316\0\66\0\316\0\67\0\316\0\70\0\316\0\71\0\316\0\72\0\316\0\73\0\316\0\74\0\316" +
		"\0\75\0\316\0\76\0\316\0\77\0\316\0\100\0\316\0\101\0\316\0\102\0\316\0\103\0\316" +
		"\0\104\0\316\0\105\0\316\0\106\0\316\0\107\0\316\0\110\0\316\0\111\0\316\0\112\0" +
		"\316\0\113\0\316\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0" +
		"\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe" +
		"\uffff\50\0\uffff\uffff\20\0\376\0\25\0\376\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff" +
		"\0\0\370\0\6\0\370\0\7\0\370\0\23\0\370\0\27\0\370\0\30\0\370\0\44\0\370\0\45\0\370" +
		"\0\55\0\370\0\56\0\370\0\57\0\370\0\60\0\370\0\61\0\370\0\62\0\370\0\63\0\370\0\64" +
		"\0\370\0\65\0\370\0\66\0\370\0\67\0\370\0\70\0\370\0\71\0\370\0\72\0\370\0\73\0\370" +
		"\0\74\0\370\0\75\0\370\0\76\0\370\0\77\0\370\0\100\0\370\0\101\0\370\0\102\0\370" +
		"\0\103\0\370\0\104\0\370\0\105\0\370\0\106\0\370\0\107\0\370\0\110\0\370\0\111\0" +
		"\370\0\112\0\370\0\113\0\370\0\114\0\370\0\uffff\uffff\ufffe\uffff\116\0\uffff\uffff" +
		"\0\0\114\0\6\0\114\0\7\0\114\0\27\0\114\0\30\0\114\0\44\0\114\0\45\0\114\0\55\0\114" +
		"\0\56\0\114\0\57\0\114\0\60\0\114\0\61\0\114\0\62\0\114\0\63\0\114\0\64\0\114\0\65" +
		"\0\114\0\66\0\114\0\67\0\114\0\70\0\114\0\71\0\114\0\72\0\114\0\73\0\114\0\74\0\114" +
		"\0\75\0\114\0\76\0\114\0\77\0\114\0\100\0\114\0\101\0\114\0\102\0\114\0\103\0\114" +
		"\0\104\0\114\0\105\0\114\0\106\0\114\0\107\0\114\0\110\0\114\0\111\0\114\0\112\0" +
		"\114\0\113\0\114\0\uffff\uffff\ufffe\uffff\76\0\uffff\uffff\15\0\215\0\17\0\215\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10" +
		"\0\u0100\0\25\0\u0100\0\26\0\u0100\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\30" +
		"\0\uffff\uffff\34\0\uffff\uffff\6\0\72\0\10\0\72\0\15\0\72\0\16\0\72\0\23\0\72\0" +
		"\24\0\72\0\25\0\72\0\26\0\72\0\32\0\72\0\33\0\72\0\35\0\72\0\42\0\72\0\43\0\72\0" +
		"\44\0\72\0\45\0\72\0\46\0\72\0\52\0\72\0\53\0\72\0\55\0\72\0\56\0\72\0\57\0\72\0" +
		"\60\0\72\0\61\0\72\0\62\0\72\0\63\0\72\0\64\0\72\0\65\0\72\0\66\0\72\0\67\0\72\0" +
		"\70\0\72\0\71\0\72\0\72\0\72\0\73\0\72\0\74\0\72\0\75\0\72\0\76\0\72\0\77\0\72\0" +
		"\100\0\72\0\101\0\72\0\102\0\72\0\103\0\72\0\104\0\72\0\105\0\72\0\106\0\72\0\107" +
		"\0\72\0\110\0\72\0\111\0\72\0\112\0\72\0\113\0\72\0\114\0\72\0\uffff\uffff\ufffe" +
		"\uffff\10\0\uffff\uffff\15\0\224\0\26\0\224\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0\26\0\u0100\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0" +
		"\26\0\u0100\0\uffff\uffff\ufffe\uffff\35\0\uffff\uffff\6\0\265\0\10\0\265\0\15\0" +
		"\265\0\16\0\265\0\23\0\265\0\24\0\265\0\25\0\265\0\26\0\265\0\42\0\265\0\43\0\265" +
		"\0\44\0\265\0\45\0\265\0\52\0\265\0\53\0\265\0\55\0\265\0\56\0\265\0\57\0\265\0\60" +
		"\0\265\0\61\0\265\0\62\0\265\0\63\0\265\0\64\0\265\0\65\0\265\0\66\0\265\0\67\0\265" +
		"\0\70\0\265\0\71\0\265\0\72\0\265\0\73\0\265\0\74\0\265\0\75\0\265\0\76\0\265\0\77" +
		"\0\265\0\100\0\265\0\101\0\265\0\102\0\265\0\103\0\265\0\104\0\265\0\105\0\265\0" +
		"\106\0\265\0\107\0\265\0\110\0\265\0\111\0\265\0\112\0\265\0\113\0\265\0\114\0\265" +
		"\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\33\0\uffff\uffff\46\0\uffff\uffff\6\0" +
		"\267\0\10\0\267\0\15\0\267\0\16\0\267\0\23\0\267\0\24\0\267\0\25\0\267\0\26\0\267" +
		"\0\35\0\267\0\42\0\267\0\43\0\267\0\44\0\267\0\45\0\267\0\52\0\267\0\53\0\267\0\55" +
		"\0\267\0\56\0\267\0\57\0\267\0\60\0\267\0\61\0\267\0\62\0\267\0\63\0\267\0\64\0\267" +
		"\0\65\0\267\0\66\0\267\0\67\0\267\0\70\0\267\0\71\0\267\0\72\0\267\0\73\0\267\0\74" +
		"\0\267\0\75\0\267\0\76\0\267\0\77\0\267\0\100\0\267\0\101\0\267\0\102\0\267\0\103" +
		"\0\267\0\104\0\267\0\105\0\267\0\106\0\267\0\107\0\267\0\110\0\267\0\111\0\267\0" +
		"\112\0\267\0\113\0\267\0\114\0\267\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10" +
		"\0\234\0\15\0\234\0\26\0\234\0\uffff\uffff\ufffe\uffff\117\0\uffff\uffff\10\0\242" +
		"\0\15\0\242\0\20\0\242\0\26\0\242\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\55\0" +
		"\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\17\0\326\0\31\0\326\0\uffff\uffff\ufffe" +
		"\uffff\50\0\uffff\uffff\20\0\376\0\25\0\376\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10" +
		"\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10" +
		"\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0" +
		"\372\0\6\0\372\0\7\0\372\0\27\0\372\0\30\0\372\0\44\0\372\0\45\0\372\0\55\0\372\0" +
		"\56\0\372\0\57\0\372\0\60\0\372\0\61\0\372\0\62\0\372\0\63\0\372\0\64\0\372\0\65" +
		"\0\372\0\66\0\372\0\67\0\372\0\70\0\372\0\71\0\372\0\72\0\372\0\73\0\372\0\74\0\372" +
		"\0\75\0\372\0\76\0\372\0\77\0\372\0\100\0\372\0\101\0\372\0\102\0\372\0\103\0\372" +
		"\0\104\0\372\0\105\0\372\0\106\0\372\0\107\0\372\0\110\0\372\0\111\0\372\0\112\0" +
		"\372\0\113\0\372\0\114\0\372\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0\0\370\0" +
		"\6\0\370\0\7\0\370\0\23\0\370\0\27\0\370\0\30\0\370\0\44\0\370\0\45\0\370\0\55\0" +
		"\370\0\56\0\370\0\57\0\370\0\60\0\370\0\61\0\370\0\62\0\370\0\63\0\370\0\64\0\370" +
		"\0\65\0\370\0\66\0\370\0\67\0\370\0\70\0\370\0\71\0\370\0\72\0\370\0\73\0\370\0\74" +
		"\0\370\0\75\0\370\0\76\0\370\0\77\0\370\0\100\0\370\0\101\0\370\0\102\0\370\0\103" +
		"\0\370\0\104\0\370\0\105\0\370\0\106\0\370\0\107\0\370\0\110\0\370\0\111\0\370\0" +
		"\112\0\370\0\113\0\370\0\114\0\370\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff\14" +
		"\0\uffff\uffff\11\0\345\0\22\0\345\0\41\0\345\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\52\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff" +
		"\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff" +
		"\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff" +
		"\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff" +
		"\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0" +
		"\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff" +
		"\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\25\0\u0100\0\26\0\u0100\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10" +
		"\0\u0100\0\25\0\u0100\0\26\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0" +
		"\26\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10" +
		"\0\u0100\0\15\0\u0100\0\25\0\u0100\0\26\0\u0100\0\uffff\uffff\ufffe\uffff\25\0\uffff" +
		"\uffff\10\0\230\0\15\0\230\0\26\0\230\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff" +
		"\10\0\232\0\15\0\232\0\26\0\232\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\17\0\325" +
		"\0\31\0\325\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\20\0\167\0\25\0\167\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff" +
		"\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0" +
		"\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100" +
		"\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0" +
		"\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff" +
		"\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100" +
		"\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\114" +
		"\0\uffff\uffff\0\0\374\0\6\0\374\0\7\0\374\0\27\0\374\0\30\0\374\0\44\0\374\0\45" +
		"\0\374\0\55\0\374\0\56\0\374\0\57\0\374\0\60\0\374\0\61\0\374\0\62\0\374\0\63\0\374" +
		"\0\64\0\374\0\65\0\374\0\66\0\374\0\67\0\374\0\70\0\374\0\71\0\374\0\72\0\374\0\73" +
		"\0\374\0\74\0\374\0\75\0\374\0\76\0\374\0\77\0\374\0\100\0\374\0\101\0\374\0\102" +
		"\0\374\0\103\0\374\0\104\0\374\0\105\0\374\0\106\0\374\0\107\0\374\0\110\0\374\0" +
		"\111\0\374\0\112\0\374\0\113\0\374\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0" +
		"\372\0\6\0\372\0\7\0\372\0\27\0\372\0\30\0\372\0\44\0\372\0\45\0\372\0\55\0\372\0" +
		"\56\0\372\0\57\0\372\0\60\0\372\0\61\0\372\0\62\0\372\0\63\0\372\0\64\0\372\0\65" +
		"\0\372\0\66\0\372\0\67\0\372\0\70\0\372\0\71\0\372\0\72\0\372\0\73\0\372\0\74\0\372" +
		"\0\75\0\372\0\76\0\372\0\77\0\372\0\100\0\372\0\101\0\372\0\102\0\372\0\103\0\372" +
		"\0\104\0\372\0\105\0\372\0\106\0\372\0\107\0\372\0\110\0\372\0\111\0\372\0\112\0" +
		"\372\0\113\0\372\0\114\0\372\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff\45\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\10\0\72\0\26\0\72\0\40\0\72\0\uffff\uffff" +
		"\ufffe\uffff\25\0\uffff\uffff\10\0\226\0\15\0\226\0\26\0\226\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100" +
		"\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0" +
		"\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff" +
		"\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0\15\0\u0100" +
		"\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\10\0\u0100\0\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\114" +
		"\0\uffff\uffff\0\0\374\0\6\0\374\0\7\0\374\0\27\0\374\0\30\0\374\0\44\0\374\0\45" +
		"\0\374\0\55\0\374\0\56\0\374\0\57\0\374\0\60\0\374\0\61\0\374\0\62\0\374\0\63\0\374" +
		"\0\64\0\374\0\65\0\374\0\66\0\374\0\67\0\374\0\70\0\374\0\71\0\374\0\72\0\374\0\73" +
		"\0\374\0\74\0\374\0\75\0\374\0\76\0\374\0\77\0\374\0\100\0\374\0\101\0\374\0\102" +
		"\0\374\0\103\0\374\0\104\0\374\0\105\0\374\0\106\0\374\0\107\0\374\0\110\0\374\0" +
		"\111\0\374\0\112\0\374\0\113\0\374\0\uffff\uffff\ufffe\uffff\11\0\352\0\41\0\uffff" +
		"\uffff\22\0\352\0\uffff\uffff\ufffe\uffff\11\0\351\0\41\0\351\0\22\0\351\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff" +
		"\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0" +
		"\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\10\0\310\0\40\0\uffff\uffff\26" +
		"\0\310\0\uffff\uffff\ufffe\uffff\10\0\311\0\40\0\311\0\26\0\311\0\uffff\uffff\ufffe" +
		"\uffff");

	private static final int[] tmGoto = TMLexer.unpack_int(173,
		"\0\0\4\0\40\0\76\0\76\0\76\0\76\0\170\0\174\0\206\0\214\0\234\0\236\0\240\0\342\0" +
		"\u0112\0\u0124\0\u014a\0\u017a\0\u017e\0\u01c8\0\u01f6\0\u020e\0\u0220\0\u0222\0" +
		"\u0234\0\u023c\0\u0242\0\u024a\0\u024c\0\u024e\0\u0258\0\u0266\0\u0270\0\u0276\0" +
		"\u02aa\0\u02de\0\u031e\0\u03de\0\u03e0\0\u03f8\0\u03fc\0\u03fe\0\u0400\0\u043a\0" +
		"\u0452\0\u0514\0\u05d6\0\u06a2\0\u0764\0\u0826\0\u08ea\0\u09ac\0\u0a6e\0\u0b36\0" +
		"\u0bf8\0\u0cc2\0\u0d86\0\u0e48\0\u0f0a\0\u0fcc\0\u108e\0\u1150\0\u1212\0\u12d4\0" +
		"\u1396\0\u145a\0\u151c\0\u15de\0\u16a6\0\u1768\0\u182a\0\u18ec\0\u19ae\0\u1a70\0" +
		"\u1b38\0\u1bfa\0\u1c38\0\u1c3a\0\u1c40\0\u1c42\0\u1d02\0\u1d1a\0\u1d20\0\u1d24\0" +
		"\u1d28\0\u1d5a\0\u1d9a\0\u1d9c\0\u1d9e\0\u1da0\0\u1da2\0\u1da4\0\u1da6\0\u1daa\0" +
		"\u1dac\0\u1dae\0\u1dfa\0\u1e22\0\u1e2e\0\u1e32\0\u1e3a\0\u1e42\0\u1e4a\0\u1e52\0" +
		"\u1e54\0\u1e5c\0\u1e60\0\u1e62\0\u1e6a\0\u1e6e\0\u1e76\0\u1e7a\0\u1e80\0\u1e82\0" +
		"\u1e86\0\u1e8a\0\u1e92\0\u1e96\0\u1e98\0\u1e9a\0\u1e9e\0\u1ea2\0\u1ea4\0\u1ea6\0" +
		"\u1eaa\0\u1eae\0\u1eb0\0\u1ed4\0\u1ef8\0\u1f1e\0\u1f44\0\u1f72\0\u1f8a\0\u1fb2\0" +
		"\u1fe0\0\u1fe2\0\u2010\0\u2014\0\u2042\0\u2070\0\u20a0\0\u20d4\0\u2108\0\u213c\0" +
		"\u2176\0\u2180\0\u2188\0\u21ba\0\u21ec\0\u2220\0\u2222\0\u2226\0\u222a\0\u223e\0" +
		"\u2240\0\u2242\0\u2248\0\u224c\0\u2250\0\u2258\0\u225e\0\u2264\0\u226e\0\u2270\0" +
		"\u2272\0\u2276\0\u227a\0\u227e\0\u2282\0\u2286\0\u228a\0\u22b8\0");

	private static final int[] tmFromTo = TMLexer.unpack_int(8888,
		"\u01bb\0\u01bd\0\u01bc\0\u01be\0\1\0\4\0\6\0\4\0\74\0\114\0\77\0\4\0\115\0\134\0" +
		"\126\0\4\0\136\0\4\0\323\0\4\0\u0115\0\4\0\u0133\0\4\0\u0154\0\4\0\u015a\0\4\0\u015b" +
		"\0\4\0\u0174\0\4\0\1\0\5\0\6\0\5\0\77\0\5\0\112\0\132\0\126\0\5\0\136\0\5\0\304\0" +
		"\u0107\0\323\0\5\0\u0109\0\u0107\0\u0115\0\5\0\u0133\0\5\0\u0154\0\5\0\u015a\0\5" +
		"\0\u015b\0\5\0\u0174\0\5\0\135\0\151\0\154\0\151\0\165\0\206\0\204\0\151\0\211\0" +
		"\206\0\234\0\151\0\257\0\324\0\327\0\324\0\343\0\324\0\345\0\324\0\373\0\324\0\375" +
		"\0\324\0\376\0\324\0\u0101\0\324\0\u0120\0\324\0\u0125\0\324\0\u0129\0\324\0\u012b" +
		"\0\324\0\u013e\0\324\0\u0141\0\324\0\u0143\0\324\0\u0145\0\324\0\u0147\0\324\0\u0148" +
		"\0\324\0\u017c\0\324\0\u0180\0\324\0\u0183\0\324\0\u0185\0\324\0\u01aa\0\324\0\75" +
		"\0\116\0\121\0\137\0\340\0\u0129\0\u016a\0\u019b\0\u0198\0\u019b\0\u01b1\0\u019b" +
		"\0\u01b2\0\u019b\0\u011e\0\u015c\0\u0193\0\u015c\0\u0194\0\u015c\0\117\0\136\0\153" +
		"\0\176\0\255\0\323\0\311\0\u010c\0\322\0\u0115\0\334\0\u0127\0\u0114\0\u0154\0\u0137" +
		"\0\u0174\0\u011c\0\u015a\0\u011c\0\u015b\0\73\0\113\0\114\0\133\0\134\0\150\0\146" +
		"\0\166\0\223\0\300\0\225\0\302\0\277\0\u0105\0\313\0\u010e\0\316\0\u0110\0\320\0" +
		"\u0112\0\322\0\u0116\0\341\0\u012a\0\u010a\0\u014f\0\u010b\0\u0150\0\u0114\0\u0155" +
		"\0\u013d\0\u0177\0\u013f\0\u0179\0\u0140\0\u017a\0\u0144\0\u017e\0\u0151\0\u018e" +
		"\0\u0156\0\u0190\0\u0178\0\u01a1\0\u017b\0\u01a2\0\u017d\0\u01a4\0\u017f\0\u01a5" +
		"\0\u0181\0\u01a7\0\u0182\0\u01a8\0\u018f\0\u01ae\0\u01a3\0\u01b3\0\u01a6\0\u01b4" +
		"\0\u01a9\0\u01b5\0\u01ab\0\u01b7\0\u01b6\0\u01ba\0\57\0\67\0\257\0\325\0\327\0\325" +
		"\0\343\0\325\0\345\0\325\0\373\0\325\0\375\0\325\0\376\0\325\0\u0101\0\325\0\u0120" +
		"\0\325\0\u0125\0\325\0\u0129\0\325\0\u012b\0\325\0\u013e\0\325\0\u0141\0\325\0\u0143" +
		"\0\325\0\u0145\0\325\0\u0147\0\325\0\u0148\0\325\0\u017c\0\325\0\u0180\0\325\0\u0183" +
		"\0\325\0\u0185\0\325\0\u01aa\0\325\0\63\0\77\0\105\0\127\0\174\0\227\0\223\0\301" +
		"\0\225\0\301\0\313\0\u010f\0\316\0\u0111\0\363\0\u0138\0\u013b\0\u0176\0\104\0\126" +
		"\0\201\0\233\0\210\0\257\0\235\0\306\0\270\0\373\0\271\0\375\0\272\0\376\0\276\0" +
		"\u0101\0\374\0\u013e\0\377\0\u0141\0\u0100\0\u0143\0\u0102\0\u0145\0\u0103\0\u0147" +
		"\0\u0104\0\u0148\0\u0142\0\u017c\0\u0146\0\u0180\0\u0149\0\u0183\0\u014a\0\u0185" +
		"\0\u0184\0\u01aa\0\1\0\6\0\6\0\6\0\77\0\6\0\136\0\6\0\257\0\326\0\323\0\6\0\327\0" +
		"\326\0\373\0\326\0\375\0\326\0\376\0\326\0\u0101\0\326\0\u0125\0\326\0\u0129\0\326" +
		"\0\u013e\0\326\0\u0141\0\326\0\u0143\0\326\0\u0145\0\326\0\u0147\0\326\0\u0148\0" +
		"\326\0\u017c\0\326\0\u0180\0\326\0\u0183\0\326\0\u0185\0\326\0\u01aa\0\326\0\64\0" +
		"\100\0\u011e\0\u015d\0\60\0\70\0\71\0\112\0\257\0\327\0\327\0\327\0\331\0\u0125\0" +
		"\332\0\u0126\0\343\0\327\0\345\0\327\0\357\0\327\0\373\0\327\0\375\0\327\0\376\0" +
		"\327\0\u0101\0\327\0\u0108\0\u014b\0\u0120\0\327\0\u0125\0\327\0\u0126\0\u0165\0" +
		"\u0127\0\327\0\u0128\0\327\0\u0129\0\327\0\u012b\0\327\0\u013e\0\327\0\u0141\0\327" +
		"\0\u0143\0\327\0\u0145\0\327\0\u0147\0\327\0\u0148\0\327\0\u014e\0\u014b\0\u0165" +
		"\0\u0165\0\u0166\0\u0165\0\u017c\0\327\0\u0180\0\327\0\u0183\0\327\0\u0185\0\327" +
		"\0\u019b\0\u0165\0\u019d\0\u0165\0\u01aa\0\327\0\257\0\330\0\327\0\330\0\343\0\330" +
		"\0\345\0\330\0\373\0\330\0\375\0\330\0\376\0\330\0\u0101\0\330\0\u0120\0\330\0\u0125" +
		"\0\330\0\u0129\0\330\0\u012b\0\330\0\u013e\0\330\0\u0141\0\330\0\u0143\0\330\0\u0145" +
		"\0\330\0\u0147\0\330\0\u0148\0\330\0\u017c\0\330\0\u0180\0\330\0\u0183\0\330\0\u0185" +
		"\0\330\0\u01aa\0\330\0\210\0\260\0\270\0\260\0\272\0\260\0\276\0\260\0\360\0\260" +
		"\0\377\0\260\0\u0102\0\260\0\u0104\0\260\0\u012c\0\260\0\u012f\0\260\0\u0149\0\260" +
		"\0\u016e\0\260\0\111\0\131\0\132\0\147\0\u011f\0\u015f\0\u0123\0\u0162\0\u0164\0" +
		"\u0197\0\u016a\0\u019c\0\u018a\0\u01ac\0\u0195\0\u01af\0\u0198\0\u01b0\0\234\0\305" +
		"\0\52\0\65\0\135\0\152\0\154\0\152\0\204\0\152\0\210\0\261\0\234\0\152\0\276\0\261" +
		"\0\334\0\65\0\u0167\0\65\0\106\0\130\0\172\0\226\0\174\0\230\0\363\0\u0139\0\152" +
		"\0\172\0\355\0\u0131\0\u01af\0\u01b8\0\65\0\101\0\127\0\101\0\355\0\u0132\0\u01af" +
		"\0\u01b9\0\334\0\u0128\0\354\0\u0130\0\326\0\u011b\0\330\0\u0121\0\u015c\0\u011b" +
		"\0\u015e\0\u011b\0\u0163\0\u0121\0\65\0\102\0\127\0\102\0\u0126\0\u0166\0\u0165\0" +
		"\u0166\0\u0166\0\u0166\0\u019b\0\u0166\0\u019d\0\u0166\0\u0123\0\u0163\0\u016a\0" +
		"\u019d\0\u0198\0\u019d\0\u01b1\0\u019d\0\u01b2\0\u019d\0\u011e\0\u015e\0\u0193\0" +
		"\u015e\0\u0194\0\u015e\0\257\0\331\0\327\0\331\0\343\0\331\0\345\0\331\0\357\0\331" +
		"\0\373\0\331\0\375\0\331\0\376\0\331\0\u0101\0\331\0\u0120\0\331\0\u0125\0\331\0" +
		"\u0127\0\331\0\u0128\0\331\0\u0129\0\331\0\u012b\0\331\0\u013e\0\331\0\u0141\0\331" +
		"\0\u0143\0\331\0\u0145\0\331\0\u0147\0\331\0\u0148\0\331\0\u017c\0\331\0\u0180\0" +
		"\331\0\u0183\0\331\0\u0185\0\331\0\u01aa\0\331\0\165\0\207\0\211\0\207\0\216\0\207" +
		"\0\257\0\207\0\327\0\207\0\343\0\207\0\345\0\207\0\373\0\207\0\375\0\207\0\376\0" +
		"\207\0\u0101\0\207\0\u0120\0\207\0\u0125\0\207\0\u0129\0\207\0\u012b\0\207\0\u013e" +
		"\0\207\0\u0141\0\207\0\u0143\0\207\0\u0145\0\207\0\u0147\0\207\0\u0148\0\207\0\u017c" +
		"\0\207\0\u0180\0\207\0\u0183\0\207\0\u0185\0\207\0\u01aa\0\207\0\1\0\7\0\6\0\7\0" +
		"\75\0\7\0\77\0\7\0\136\0\7\0\154\0\7\0\207\0\7\0\211\0\7\0\234\0\7\0\257\0\7\0\323" +
		"\0\7\0\327\0\7\0\345\0\7\0\373\0\7\0\375\0\7\0\376\0\7\0\u0101\0\7\0\u0120\0\7\0" +
		"\u0125\0\7\0\u0129\0\7\0\u012b\0\7\0\u013e\0\7\0\u0141\0\7\0\u0143\0\7\0\u0145\0" +
		"\7\0\u0147\0\7\0\u0148\0\7\0\u017c\0\7\0\u0180\0\7\0\u0183\0\7\0\u0185\0\7\0\u01aa" +
		"\0\7\0\1\0\10\0\2\0\10\0\6\0\10\0\65\0\10\0\67\0\10\0\70\0\10\0\74\0\10\0\75\0\10" +
		"\0\77\0\10\0\101\0\10\0\102\0\10\0\126\0\10\0\127\0\10\0\135\0\10\0\136\0\10\0\152" +
		"\0\10\0\154\0\10\0\160\0\10\0\165\0\10\0\167\0\10\0\170\0\10\0\171\0\10\0\204\0\10" +
		"\0\207\0\10\0\211\0\10\0\217\0\10\0\221\0\10\0\227\0\10\0\234\0\10\0\241\0\10\0\243" +
		"\0\10\0\244\0\10\0\252\0\10\0\254\0\10\0\257\0\10\0\260\0\10\0\261\0\10\0\265\0\10" +
		"\0\301\0\10\0\320\0\10\0\321\0\10\0\323\0\10\0\325\0\10\0\326\0\10\0\327\0\10\0\330" +
		"\0\10\0\343\0\10\0\345\0\10\0\357\0\10\0\362\0\10\0\366\0\10\0\373\0\10\0\375\0\10" +
		"\0\376\0\10\0\u0101\0\10\0\u010f\0\10\0\u0111\0\10\0\u0115\0\10\0\u0118\0\10\0\u0119" +
		"\0\10\0\u011b\0\10\0\u0120\0\10\0\u0121\0\10\0\u0125\0\10\0\u0126\0\10\0\u0127\0" +
		"\10\0\u0128\0\10\0\u0129\0\10\0\u012b\0\10\0\u0133\0\10\0\u0136\0\10\0\u0138\0\10" +
		"\0\u013e\0\10\0\u0141\0\10\0\u0143\0\10\0\u0145\0\10\0\u0147\0\10\0\u0148\0\10\0" +
		"\u0154\0\10\0\u015c\0\10\0\u015e\0\10\0\u0160\0\10\0\u0163\0\10\0\u0165\0\10\0\u0166" +
		"\0\10\0\u0167\0\10\0\u0174\0\10\0\u0176\0\10\0\u017c\0\10\0\u0180\0\10\0\u0183\0" +
		"\10\0\u0185\0\10\0\u0195\0\10\0\u019b\0\10\0\u019d\0\10\0\u01aa\0\10\0\355\0\u0133" +
		"\0\1\0\11\0\6\0\11\0\77\0\11\0\126\0\11\0\136\0\11\0\323\0\11\0\u0115\0\11\0\u0133" +
		"\0\11\0\u0154\0\11\0\u015a\0\11\0\u015b\0\11\0\u0174\0\11\0\262\0\366\0\371\0\366" +
		"\0\61\0\74\0\u0120\0\u0160\0\257\0\332\0\307\0\332\0\310\0\332\0\327\0\332\0\343" +
		"\0\332\0\345\0\332\0\357\0\332\0\373\0\332\0\375\0\332\0\376\0\332\0\u0101\0\332" +
		"\0\u010c\0\332\0\u0120\0\332\0\u0125\0\332\0\u0127\0\332\0\u0128\0\332\0\u0129\0" +
		"\332\0\u012b\0\332\0\u013e\0\332\0\u0141\0\332\0\u0143\0\332\0\u0145\0\332\0\u0147" +
		"\0\332\0\u0148\0\332\0\u017c\0\332\0\u0180\0\332\0\u0183\0\332\0\u0185\0\332\0\u01aa" +
		"\0\332\0\1\0\12\0\6\0\12\0\77\0\12\0\126\0\12\0\136\0\12\0\323\0\12\0\u0115\0\12" +
		"\0\u0133\0\12\0\u0154\0\12\0\u015a\0\12\0\u015b\0\12\0\u0174\0\12\0\1\0\13\0\2\0" +
		"\13\0\6\0\13\0\65\0\13\0\67\0\13\0\70\0\13\0\74\0\13\0\75\0\13\0\77\0\13\0\101\0" +
		"\13\0\102\0\13\0\126\0\13\0\127\0\13\0\135\0\13\0\136\0\13\0\152\0\13\0\154\0\13" +
		"\0\160\0\13\0\165\0\13\0\167\0\13\0\170\0\13\0\171\0\13\0\204\0\13\0\206\0\236\0" +
		"\207\0\13\0\211\0\13\0\217\0\13\0\221\0\13\0\227\0\13\0\234\0\13\0\241\0\13\0\243" +
		"\0\13\0\244\0\13\0\252\0\13\0\254\0\13\0\257\0\13\0\260\0\13\0\261\0\13\0\265\0\13" +
		"\0\301\0\13\0\320\0\13\0\321\0\13\0\323\0\13\0\325\0\13\0\326\0\13\0\327\0\13\0\330" +
		"\0\13\0\343\0\13\0\345\0\13\0\357\0\13\0\362\0\13\0\366\0\13\0\373\0\13\0\375\0\13" +
		"\0\376\0\13\0\u0101\0\13\0\u010f\0\13\0\u0111\0\13\0\u0115\0\13\0\u0118\0\13\0\u0119" +
		"\0\13\0\u011b\0\13\0\u0120\0\13\0\u0121\0\13\0\u0125\0\13\0\u0126\0\13\0\u0127\0" +
		"\13\0\u0128\0\13\0\u0129\0\13\0\u012b\0\13\0\u0133\0\13\0\u0136\0\13\0\u0138\0\13" +
		"\0\u013e\0\13\0\u0141\0\13\0\u0143\0\13\0\u0145\0\13\0\u0147\0\13\0\u0148\0\13\0" +
		"\u0154\0\13\0\u015c\0\13\0\u015e\0\13\0\u0160\0\13\0\u0163\0\13\0\u0165\0\13\0\u0166" +
		"\0\13\0\u0167\0\13\0\u0174\0\13\0\u0176\0\13\0\u017c\0\13\0\u0180\0\13\0\u0183\0" +
		"\13\0\u0185\0\13\0\u0195\0\13\0\u019b\0\13\0\u019d\0\13\0\u01aa\0\13\0\1\0\14\0\2" +
		"\0\14\0\6\0\14\0\65\0\14\0\67\0\14\0\70\0\14\0\74\0\14\0\75\0\14\0\77\0\14\0\101" +
		"\0\14\0\102\0\14\0\126\0\14\0\127\0\14\0\135\0\14\0\136\0\14\0\151\0\167\0\152\0" +
		"\14\0\154\0\14\0\160\0\14\0\165\0\14\0\167\0\14\0\170\0\14\0\171\0\14\0\204\0\14" +
		"\0\207\0\14\0\211\0\14\0\217\0\14\0\221\0\14\0\227\0\14\0\234\0\14\0\241\0\14\0\243" +
		"\0\14\0\244\0\14\0\252\0\14\0\254\0\14\0\257\0\14\0\260\0\14\0\261\0\14\0\265\0\14" +
		"\0\301\0\14\0\320\0\14\0\321\0\14\0\323\0\14\0\325\0\14\0\326\0\14\0\327\0\14\0\330" +
		"\0\14\0\343\0\14\0\345\0\14\0\357\0\14\0\362\0\14\0\366\0\14\0\373\0\14\0\375\0\14" +
		"\0\376\0\14\0\u0101\0\14\0\u010f\0\14\0\u0111\0\14\0\u0115\0\14\0\u0118\0\14\0\u0119" +
		"\0\14\0\u011b\0\14\0\u0120\0\14\0\u0121\0\14\0\u0125\0\14\0\u0126\0\14\0\u0127\0" +
		"\14\0\u0128\0\14\0\u0129\0\14\0\u012b\0\14\0\u0133\0\14\0\u0136\0\14\0\u0138\0\14" +
		"\0\u013e\0\14\0\u0141\0\14\0\u0143\0\14\0\u0145\0\14\0\u0147\0\14\0\u0148\0\14\0" +
		"\u0154\0\14\0\u015c\0\14\0\u015e\0\14\0\u0160\0\14\0\u0163\0\14\0\u0165\0\14\0\u0166" +
		"\0\14\0\u0167\0\14\0\u0174\0\14\0\u0176\0\14\0\u017c\0\14\0\u0180\0\14\0\u0183\0" +
		"\14\0\u0185\0\14\0\u0195\0\14\0\u019b\0\14\0\u019d\0\14\0\u01aa\0\14\0\1\0\15\0\2" +
		"\0\15\0\6\0\15\0\65\0\15\0\67\0\15\0\70\0\15\0\74\0\15\0\75\0\15\0\77\0\15\0\101" +
		"\0\15\0\102\0\15\0\126\0\15\0\127\0\15\0\135\0\15\0\136\0\15\0\152\0\15\0\154\0\15" +
		"\0\160\0\15\0\165\0\15\0\167\0\15\0\170\0\15\0\171\0\15\0\204\0\15\0\207\0\15\0\210" +
		"\0\262\0\211\0\15\0\217\0\15\0\221\0\15\0\227\0\15\0\234\0\15\0\241\0\15\0\243\0" +
		"\15\0\244\0\15\0\252\0\15\0\254\0\15\0\257\0\15\0\260\0\15\0\261\0\15\0\263\0\371" +
		"\0\265\0\15\0\272\0\262\0\276\0\262\0\301\0\15\0\320\0\15\0\321\0\15\0\323\0\15\0" +
		"\325\0\15\0\326\0\15\0\327\0\15\0\330\0\15\0\343\0\15\0\345\0\15\0\357\0\15\0\362" +
		"\0\15\0\366\0\15\0\373\0\15\0\375\0\15\0\376\0\15\0\u0101\0\15\0\u0104\0\262\0\u010f" +
		"\0\15\0\u0111\0\15\0\u0115\0\15\0\u0118\0\15\0\u0119\0\15\0\u011b\0\15\0\u0120\0" +
		"\15\0\u0121\0\15\0\u0125\0\15\0\u0126\0\15\0\u0127\0\15\0\u0128\0\15\0\u0129\0\15" +
		"\0\u012b\0\15\0\u0133\0\15\0\u0136\0\15\0\u0138\0\15\0\u013e\0\15\0\u0141\0\15\0" +
		"\u0143\0\15\0\u0145\0\15\0\u0147\0\15\0\u0148\0\15\0\u014b\0\u0186\0\u0154\0\15\0" +
		"\u015c\0\15\0\u015e\0\15\0\u0160\0\15\0\u0163\0\15\0\u0165\0\15\0\u0166\0\15\0\u0167" +
		"\0\15\0\u0174\0\15\0\u0176\0\15\0\u017c\0\15\0\u0180\0\15\0\u0183\0\15\0\u0185\0" +
		"\15\0\u0195\0\15\0\u019b\0\15\0\u019d\0\15\0\u01aa\0\15\0\1\0\16\0\2\0\16\0\6\0\16" +
		"\0\65\0\16\0\67\0\16\0\70\0\16\0\74\0\16\0\75\0\16\0\77\0\16\0\101\0\16\0\102\0\16" +
		"\0\126\0\16\0\127\0\16\0\135\0\16\0\136\0\16\0\152\0\16\0\154\0\16\0\160\0\16\0\165" +
		"\0\16\0\167\0\16\0\170\0\16\0\171\0\16\0\204\0\16\0\207\0\16\0\211\0\16\0\217\0\16" +
		"\0\221\0\16\0\227\0\16\0\234\0\16\0\236\0\307\0\241\0\16\0\243\0\16\0\244\0\16\0" +
		"\252\0\16\0\254\0\16\0\257\0\16\0\260\0\16\0\261\0\16\0\265\0\16\0\301\0\16\0\320" +
		"\0\16\0\321\0\16\0\323\0\16\0\325\0\16\0\326\0\16\0\327\0\16\0\330\0\16\0\343\0\16" +
		"\0\345\0\16\0\357\0\16\0\362\0\16\0\366\0\16\0\373\0\16\0\375\0\16\0\376\0\16\0\u0101" +
		"\0\16\0\u010f\0\16\0\u0111\0\16\0\u0115\0\16\0\u0118\0\16\0\u0119\0\16\0\u011b\0" +
		"\16\0\u0120\0\16\0\u0121\0\16\0\u0125\0\16\0\u0126\0\16\0\u0127\0\16\0\u0128\0\16" +
		"\0\u0129\0\16\0\u012b\0\16\0\u0133\0\16\0\u0136\0\16\0\u0138\0\16\0\u013e\0\16\0" +
		"\u0141\0\16\0\u0143\0\16\0\u0145\0\16\0\u0147\0\16\0\u0148\0\16\0\u0154\0\16\0\u015c" +
		"\0\16\0\u015e\0\16\0\u0160\0\16\0\u0163\0\16\0\u0165\0\16\0\u0166\0\16\0\u0167\0" +
		"\16\0\u0174\0\16\0\u0176\0\16\0\u017c\0\16\0\u0180\0\16\0\u0183\0\16\0\u0185\0\16" +
		"\0\u0195\0\16\0\u019b\0\16\0\u019d\0\16\0\u01aa\0\16\0\1\0\17\0\2\0\17\0\6\0\17\0" +
		"\65\0\17\0\67\0\17\0\70\0\17\0\74\0\17\0\75\0\17\0\77\0\17\0\101\0\17\0\102\0\17" +
		"\0\126\0\17\0\127\0\17\0\135\0\17\0\136\0\17\0\152\0\17\0\154\0\17\0\160\0\17\0\165" +
		"\0\17\0\167\0\17\0\170\0\17\0\171\0\17\0\204\0\17\0\206\0\237\0\207\0\17\0\211\0" +
		"\17\0\217\0\17\0\221\0\17\0\227\0\17\0\234\0\17\0\241\0\17\0\243\0\17\0\244\0\17" +
		"\0\252\0\17\0\254\0\17\0\257\0\17\0\260\0\17\0\261\0\17\0\265\0\17\0\301\0\17\0\320" +
		"\0\17\0\321\0\17\0\323\0\17\0\325\0\17\0\326\0\17\0\327\0\17\0\330\0\17\0\343\0\17" +
		"\0\345\0\17\0\357\0\17\0\362\0\17\0\366\0\17\0\373\0\17\0\375\0\17\0\376\0\17\0\u0101" +
		"\0\17\0\u010f\0\17\0\u0111\0\17\0\u0115\0\17\0\u0118\0\17\0\u0119\0\17\0\u011b\0" +
		"\17\0\u0120\0\17\0\u0121\0\17\0\u0125\0\17\0\u0126\0\17\0\u0127\0\17\0\u0128\0\17" +
		"\0\u0129\0\17\0\u012b\0\17\0\u0133\0\17\0\u0136\0\17\0\u0138\0\17\0\u013e\0\17\0" +
		"\u0141\0\17\0\u0143\0\17\0\u0145\0\17\0\u0147\0\17\0\u0148\0\17\0\u0154\0\17\0\u015c" +
		"\0\17\0\u015e\0\17\0\u0160\0\17\0\u0163\0\17\0\u0165\0\17\0\u0166\0\17\0\u0167\0" +
		"\17\0\u0174\0\17\0\u0176\0\17\0\u017c\0\17\0\u0180\0\17\0\u0183\0\17\0\u0185\0\17" +
		"\0\u0195\0\17\0\u019b\0\17\0\u019d\0\17\0\u01aa\0\17\0\1\0\20\0\2\0\20\0\6\0\20\0" +
		"\65\0\20\0\67\0\20\0\70\0\20\0\74\0\20\0\75\0\20\0\77\0\20\0\101\0\20\0\102\0\20" +
		"\0\126\0\20\0\127\0\20\0\135\0\20\0\136\0\20\0\152\0\20\0\154\0\20\0\160\0\20\0\165" +
		"\0\20\0\167\0\20\0\170\0\20\0\171\0\20\0\204\0\20\0\206\0\240\0\207\0\20\0\211\0" +
		"\20\0\217\0\20\0\221\0\20\0\227\0\20\0\234\0\20\0\241\0\20\0\243\0\20\0\244\0\20" +
		"\0\252\0\20\0\253\0\240\0\254\0\20\0\257\0\20\0\260\0\20\0\261\0\20\0\265\0\20\0" +
		"\301\0\20\0\320\0\20\0\321\0\20\0\323\0\20\0\325\0\20\0\326\0\20\0\327\0\20\0\330" +
		"\0\20\0\343\0\20\0\345\0\20\0\357\0\20\0\362\0\20\0\366\0\20\0\373\0\20\0\375\0\20" +
		"\0\376\0\20\0\u0101\0\20\0\u010f\0\20\0\u0111\0\20\0\u0115\0\20\0\u0118\0\20\0\u0119" +
		"\0\20\0\u011b\0\20\0\u0120\0\20\0\u0121\0\20\0\u0125\0\20\0\u0126\0\20\0\u0127\0" +
		"\20\0\u0128\0\20\0\u0129\0\20\0\u012b\0\20\0\u0133\0\20\0\u0136\0\20\0\u0138\0\20" +
		"\0\u013e\0\20\0\u0141\0\20\0\u0143\0\20\0\u0145\0\20\0\u0147\0\20\0\u0148\0\20\0" +
		"\u0154\0\20\0\u015c\0\20\0\u015e\0\20\0\u0160\0\20\0\u0163\0\20\0\u0165\0\20\0\u0166" +
		"\0\20\0\u0167\0\20\0\u0174\0\20\0\u0176\0\20\0\u017c\0\20\0\u0180\0\20\0\u0183\0" +
		"\20\0\u0185\0\20\0\u0195\0\20\0\u019b\0\20\0\u019d\0\20\0\u01aa\0\20\0\1\0\21\0\2" +
		"\0\21\0\6\0\21\0\65\0\21\0\67\0\21\0\70\0\21\0\74\0\21\0\75\0\21\0\77\0\21\0\101" +
		"\0\21\0\102\0\21\0\126\0\21\0\127\0\21\0\135\0\21\0\136\0\21\0\152\0\21\0\154\0\21" +
		"\0\160\0\21\0\165\0\21\0\167\0\21\0\170\0\21\0\171\0\21\0\204\0\21\0\206\0\241\0" +
		"\207\0\21\0\211\0\21\0\217\0\21\0\221\0\21\0\227\0\21\0\234\0\21\0\241\0\21\0\243" +
		"\0\21\0\244\0\21\0\252\0\21\0\254\0\21\0\257\0\21\0\260\0\21\0\261\0\21\0\265\0\21" +
		"\0\301\0\21\0\320\0\21\0\321\0\21\0\323\0\21\0\325\0\21\0\326\0\21\0\327\0\21\0\330" +
		"\0\21\0\343\0\21\0\345\0\21\0\357\0\21\0\362\0\21\0\366\0\21\0\373\0\21\0\375\0\21" +
		"\0\376\0\21\0\u0101\0\21\0\u010f\0\21\0\u0111\0\21\0\u0115\0\21\0\u0118\0\21\0\u0119" +
		"\0\21\0\u011b\0\21\0\u0120\0\21\0\u0121\0\21\0\u0125\0\21\0\u0126\0\21\0\u0127\0" +
		"\21\0\u0128\0\21\0\u0129\0\21\0\u012b\0\21\0\u0133\0\21\0\u0136\0\21\0\u0138\0\21" +
		"\0\u013e\0\21\0\u0141\0\21\0\u0143\0\21\0\u0145\0\21\0\u0147\0\21\0\u0148\0\21\0" +
		"\u0154\0\21\0\u015c\0\21\0\u015e\0\21\0\u0160\0\21\0\u0163\0\21\0\u0165\0\21\0\u0166" +
		"\0\21\0\u0167\0\21\0\u0174\0\21\0\u0176\0\21\0\u017c\0\21\0\u0180\0\21\0\u0183\0" +
		"\21\0\u0185\0\21\0\u0195\0\21\0\u019b\0\21\0\u019d\0\21\0\u01aa\0\21\0\1\0\22\0\2" +
		"\0\22\0\6\0\22\0\65\0\22\0\67\0\22\0\70\0\22\0\74\0\22\0\75\0\22\0\77\0\22\0\101" +
		"\0\22\0\102\0\22\0\126\0\22\0\127\0\22\0\135\0\22\0\136\0\22\0\152\0\22\0\154\0\22" +
		"\0\160\0\22\0\165\0\22\0\167\0\22\0\170\0\22\0\171\0\22\0\204\0\22\0\206\0\242\0" +
		"\207\0\22\0\211\0\22\0\217\0\22\0\221\0\22\0\227\0\22\0\234\0\22\0\241\0\22\0\243" +
		"\0\22\0\244\0\22\0\252\0\22\0\254\0\22\0\257\0\22\0\260\0\22\0\261\0\22\0\265\0\22" +
		"\0\301\0\22\0\320\0\22\0\321\0\22\0\323\0\22\0\325\0\22\0\326\0\22\0\327\0\22\0\330" +
		"\0\22\0\343\0\22\0\345\0\22\0\357\0\22\0\362\0\22\0\366\0\22\0\373\0\22\0\375\0\22" +
		"\0\376\0\22\0\u0101\0\22\0\u010f\0\22\0\u0111\0\22\0\u0115\0\22\0\u0118\0\22\0\u0119" +
		"\0\22\0\u011b\0\22\0\u0120\0\22\0\u0121\0\22\0\u0125\0\22\0\u0126\0\22\0\u0127\0" +
		"\22\0\u0128\0\22\0\u0129\0\22\0\u012b\0\22\0\u0133\0\22\0\u0136\0\22\0\u0138\0\22" +
		"\0\u013e\0\22\0\u0141\0\22\0\u0143\0\22\0\u0145\0\22\0\u0147\0\22\0\u0148\0\22\0" +
		"\u0154\0\22\0\u015c\0\22\0\u015e\0\22\0\u0160\0\22\0\u0163\0\22\0\u0165\0\22\0\u0166" +
		"\0\22\0\u0167\0\22\0\u0174\0\22\0\u0176\0\22\0\u017c\0\22\0\u0180\0\22\0\u0183\0" +
		"\22\0\u0185\0\22\0\u0195\0\22\0\u019b\0\22\0\u019d\0\22\0\u01aa\0\22\0\1\0\23\0\2" +
		"\0\23\0\6\0\23\0\65\0\23\0\67\0\23\0\70\0\23\0\74\0\23\0\75\0\23\0\77\0\23\0\101" +
		"\0\23\0\102\0\23\0\126\0\23\0\127\0\23\0\135\0\23\0\136\0\23\0\152\0\23\0\154\0\23" +
		"\0\160\0\23\0\165\0\23\0\167\0\23\0\170\0\23\0\171\0\23\0\204\0\23\0\207\0\23\0\210" +
		"\0\263\0\211\0\23\0\217\0\23\0\221\0\23\0\227\0\23\0\234\0\23\0\241\0\23\0\243\0" +
		"\23\0\244\0\23\0\252\0\23\0\254\0\23\0\257\0\23\0\260\0\23\0\261\0\23\0\265\0\23" +
		"\0\272\0\263\0\276\0\263\0\301\0\23\0\320\0\23\0\321\0\23\0\323\0\23\0\325\0\23\0" +
		"\326\0\23\0\327\0\23\0\330\0\23\0\343\0\23\0\345\0\23\0\357\0\23\0\362\0\23\0\366" +
		"\0\23\0\373\0\23\0\375\0\23\0\376\0\23\0\u0101\0\23\0\u0104\0\263\0\u010f\0\23\0" +
		"\u0111\0\23\0\u0115\0\23\0\u0118\0\23\0\u0119\0\23\0\u011b\0\23\0\u0120\0\23\0\u0121" +
		"\0\23\0\u0125\0\23\0\u0126\0\23\0\u0127\0\23\0\u0128\0\23\0\u0129\0\23\0\u012b\0" +
		"\23\0\u0133\0\23\0\u0136\0\23\0\u0138\0\23\0\u013e\0\23\0\u0141\0\23\0\u0143\0\23" +
		"\0\u0145\0\23\0\u0147\0\23\0\u0148\0\23\0\u0154\0\23\0\u015c\0\23\0\u015e\0\23\0" +
		"\u0160\0\23\0\u0163\0\23\0\u0165\0\23\0\u0166\0\23\0\u0167\0\23\0\u0174\0\23\0\u0176" +
		"\0\23\0\u017c\0\23\0\u0180\0\23\0\u0183\0\23\0\u0185\0\23\0\u0195\0\23\0\u019b\0" +
		"\23\0\u019d\0\23\0\u01aa\0\23\0\1\0\24\0\2\0\24\0\6\0\24\0\65\0\24\0\67\0\24\0\70" +
		"\0\24\0\74\0\24\0\75\0\24\0\77\0\24\0\101\0\24\0\102\0\24\0\126\0\24\0\127\0\24\0" +
		"\135\0\24\0\136\0\24\0\152\0\24\0\154\0\24\0\160\0\24\0\165\0\24\0\167\0\24\0\170" +
		"\0\24\0\171\0\24\0\204\0\24\0\206\0\243\0\207\0\24\0\211\0\24\0\217\0\24\0\221\0" +
		"\24\0\227\0\24\0\234\0\24\0\241\0\24\0\243\0\24\0\244\0\24\0\252\0\24\0\254\0\24" +
		"\0\257\0\24\0\260\0\24\0\261\0\24\0\265\0\24\0\301\0\24\0\320\0\24\0\321\0\24\0\323" +
		"\0\24\0\325\0\24\0\326\0\24\0\327\0\24\0\330\0\24\0\343\0\24\0\345\0\24\0\357\0\24" +
		"\0\362\0\24\0\366\0\24\0\373\0\24\0\375\0\24\0\376\0\24\0\u0101\0\24\0\u010f\0\24" +
		"\0\u0111\0\24\0\u0115\0\24\0\u0118\0\24\0\u0119\0\24\0\u011b\0\24\0\u0120\0\24\0" +
		"\u0121\0\24\0\u0125\0\24\0\u0126\0\24\0\u0127\0\24\0\u0128\0\24\0\u0129\0\24\0\u012b" +
		"\0\24\0\u0133\0\24\0\u0136\0\24\0\u0138\0\24\0\u013e\0\24\0\u0141\0\24\0\u0143\0" +
		"\24\0\u0145\0\24\0\u0147\0\24\0\u0148\0\24\0\u0154\0\24\0\u015c\0\24\0\u015e\0\24" +
		"\0\u0160\0\24\0\u0163\0\24\0\u0165\0\24\0\u0166\0\24\0\u0167\0\24\0\u0174\0\24\0" +
		"\u0176\0\24\0\u017c\0\24\0\u0180\0\24\0\u0183\0\24\0\u0185\0\24\0\u0195\0\24\0\u019b" +
		"\0\24\0\u019d\0\24\0\u01aa\0\24\0\1\0\25\0\2\0\25\0\6\0\25\0\65\0\25\0\67\0\25\0" +
		"\70\0\25\0\74\0\25\0\75\0\25\0\77\0\25\0\101\0\25\0\102\0\25\0\126\0\25\0\127\0\25" +
		"\0\135\0\25\0\136\0\25\0\152\0\25\0\154\0\25\0\160\0\25\0\165\0\25\0\167\0\25\0\170" +
		"\0\25\0\171\0\25\0\204\0\25\0\206\0\244\0\207\0\25\0\210\0\264\0\211\0\25\0\217\0" +
		"\25\0\221\0\25\0\227\0\25\0\234\0\25\0\241\0\25\0\243\0\25\0\244\0\25\0\252\0\25" +
		"\0\254\0\25\0\257\0\25\0\260\0\25\0\261\0\25\0\265\0\25\0\272\0\264\0\276\0\264\0" +
		"\301\0\25\0\320\0\25\0\321\0\25\0\323\0\25\0\325\0\25\0\326\0\25\0\327\0\25\0\330" +
		"\0\25\0\343\0\25\0\345\0\25\0\357\0\25\0\362\0\25\0\366\0\25\0\373\0\25\0\375\0\25" +
		"\0\376\0\25\0\u0101\0\25\0\u0104\0\264\0\u010f\0\25\0\u0111\0\25\0\u0115\0\25\0\u0118" +
		"\0\25\0\u0119\0\25\0\u011b\0\25\0\u0120\0\25\0\u0121\0\25\0\u0125\0\25\0\u0126\0" +
		"\25\0\u0127\0\25\0\u0128\0\25\0\u0129\0\25\0\u012b\0\25\0\u0133\0\25\0\u0136\0\25" +
		"\0\u0138\0\25\0\u013e\0\25\0\u0141\0\25\0\u0143\0\25\0\u0145\0\25\0\u0147\0\25\0" +
		"\u0148\0\25\0\u0154\0\25\0\u015c\0\25\0\u015e\0\25\0\u0160\0\25\0\u0163\0\25\0\u0165" +
		"\0\25\0\u0166\0\25\0\u0167\0\25\0\u0174\0\25\0\u0176\0\25\0\u017c\0\25\0\u0180\0" +
		"\25\0\u0183\0\25\0\u0185\0\25\0\u0195\0\25\0\u019b\0\25\0\u019d\0\25\0\u01aa\0\25" +
		"\0\1\0\26\0\2\0\26\0\6\0\26\0\60\0\71\0\65\0\26\0\67\0\26\0\70\0\26\0\74\0\26\0\75" +
		"\0\26\0\77\0\26\0\101\0\26\0\102\0\26\0\126\0\26\0\127\0\26\0\131\0\71\0\135\0\26" +
		"\0\136\0\26\0\152\0\26\0\154\0\26\0\160\0\26\0\165\0\26\0\167\0\26\0\170\0\26\0\171" +
		"\0\26\0\204\0\26\0\207\0\26\0\211\0\26\0\217\0\26\0\221\0\26\0\227\0\26\0\234\0\26" +
		"\0\241\0\26\0\243\0\26\0\244\0\26\0\252\0\26\0\254\0\26\0\257\0\26\0\260\0\26\0\261" +
		"\0\26\0\265\0\26\0\301\0\26\0\320\0\26\0\321\0\26\0\323\0\26\0\325\0\26\0\326\0\26" +
		"\0\327\0\26\0\330\0\26\0\343\0\26\0\345\0\26\0\357\0\26\0\362\0\26\0\366\0\26\0\373" +
		"\0\26\0\375\0\26\0\376\0\26\0\u0101\0\26\0\u010f\0\26\0\u0111\0\26\0\u0115\0\26\0" +
		"\u0118\0\26\0\u0119\0\26\0\u011b\0\26\0\u0120\0\26\0\u0121\0\26\0\u0125\0\26\0\u0126" +
		"\0\26\0\u0127\0\26\0\u0128\0\26\0\u0129\0\26\0\u012b\0\26\0\u0133\0\26\0\u0136\0" +
		"\26\0\u0138\0\26\0\u013e\0\26\0\u0141\0\26\0\u0143\0\26\0\u0145\0\26\0\u0147\0\26" +
		"\0\u0148\0\26\0\u0154\0\26\0\u015c\0\26\0\u015e\0\26\0\u0160\0\26\0\u0163\0\26\0" +
		"\u0165\0\26\0\u0166\0\26\0\u0167\0\26\0\u0174\0\26\0\u0176\0\26\0\u017c\0\26\0\u0180" +
		"\0\26\0\u0183\0\26\0\u0185\0\26\0\u0195\0\26\0\u019b\0\26\0\u019d\0\26\0\u01aa\0" +
		"\26\0\0\0\2\0\1\0\27\0\2\0\27\0\6\0\27\0\65\0\27\0\67\0\27\0\70\0\27\0\74\0\27\0" +
		"\75\0\27\0\77\0\27\0\101\0\27\0\102\0\27\0\126\0\27\0\127\0\27\0\135\0\27\0\136\0" +
		"\27\0\152\0\27\0\154\0\27\0\160\0\27\0\165\0\27\0\167\0\27\0\170\0\27\0\171\0\27" +
		"\0\204\0\27\0\207\0\27\0\211\0\27\0\217\0\27\0\221\0\27\0\227\0\27\0\234\0\27\0\241" +
		"\0\27\0\243\0\27\0\244\0\27\0\252\0\27\0\254\0\27\0\257\0\27\0\260\0\27\0\261\0\27" +
		"\0\265\0\27\0\301\0\27\0\320\0\27\0\321\0\27\0\323\0\27\0\325\0\27\0\326\0\27\0\327" +
		"\0\27\0\330\0\27\0\343\0\27\0\345\0\27\0\357\0\27\0\362\0\27\0\366\0\27\0\373\0\27" +
		"\0\375\0\27\0\376\0\27\0\u0101\0\27\0\u010f\0\27\0\u0111\0\27\0\u0115\0\27\0\u0118" +
		"\0\27\0\u0119\0\27\0\u011b\0\27\0\u0120\0\27\0\u0121\0\27\0\u0125\0\27\0\u0126\0" +
		"\27\0\u0127\0\27\0\u0128\0\27\0\u0129\0\27\0\u012b\0\27\0\u0133\0\27\0\u0136\0\27" +
		"\0\u0138\0\27\0\u013e\0\27\0\u0141\0\27\0\u0143\0\27\0\u0145\0\27\0\u0147\0\27\0" +
		"\u0148\0\27\0\u0154\0\27\0\u015c\0\27\0\u015e\0\27\0\u0160\0\27\0\u0163\0\27\0\u0165" +
		"\0\27\0\u0166\0\27\0\u0167\0\27\0\u0174\0\27\0\u0176\0\27\0\u017c\0\27\0\u0180\0" +
		"\27\0\u0183\0\27\0\u0185\0\27\0\u0195\0\27\0\u019b\0\27\0\u019d\0\27\0\u01aa\0\27" +
		"\0\1\0\30\0\2\0\30\0\6\0\30\0\65\0\30\0\67\0\30\0\70\0\30\0\74\0\30\0\75\0\30\0\77" +
		"\0\30\0\101\0\30\0\102\0\30\0\126\0\30\0\127\0\30\0\135\0\30\0\136\0\30\0\152\0\30" +
		"\0\154\0\30\0\160\0\30\0\165\0\30\0\167\0\30\0\170\0\30\0\171\0\30\0\204\0\30\0\207" +
		"\0\30\0\211\0\30\0\217\0\30\0\221\0\30\0\227\0\30\0\234\0\30\0\241\0\30\0\243\0\30" +
		"\0\244\0\30\0\252\0\30\0\254\0\30\0\257\0\30\0\260\0\30\0\261\0\30\0\265\0\30\0\301" +
		"\0\30\0\320\0\30\0\321\0\30\0\323\0\30\0\325\0\30\0\326\0\30\0\327\0\30\0\330\0\30" +
		"\0\343\0\30\0\345\0\30\0\357\0\30\0\362\0\30\0\366\0\30\0\373\0\30\0\375\0\30\0\376" +
		"\0\30\0\u0101\0\30\0\u010f\0\30\0\u0111\0\30\0\u0115\0\30\0\u0118\0\30\0\u0119\0" +
		"\30\0\u011b\0\30\0\u0120\0\30\0\u0121\0\30\0\u0125\0\30\0\u0126\0\30\0\u0127\0\30" +
		"\0\u0128\0\30\0\u0129\0\30\0\u012b\0\30\0\u0133\0\30\0\u0136\0\30\0\u0138\0\30\0" +
		"\u013e\0\30\0\u0141\0\30\0\u0143\0\30\0\u0145\0\30\0\u0147\0\30\0\u0148\0\30\0\u014b" +
		"\0\u0187\0\u0154\0\30\0\u015c\0\30\0\u015e\0\30\0\u0160\0\30\0\u0163\0\30\0\u0165" +
		"\0\30\0\u0166\0\30\0\u0167\0\30\0\u0174\0\30\0\u0176\0\30\0\u017c\0\30\0\u0180\0" +
		"\30\0\u0183\0\30\0\u0185\0\30\0\u0195\0\30\0\u019b\0\30\0\u019d\0\30\0\u01aa\0\30" +
		"\0\1\0\31\0\2\0\31\0\6\0\31\0\65\0\31\0\67\0\31\0\70\0\31\0\74\0\31\0\75\0\31\0\77" +
		"\0\31\0\101\0\31\0\102\0\31\0\126\0\31\0\127\0\31\0\135\0\31\0\136\0\31\0\152\0\31" +
		"\0\154\0\31\0\160\0\31\0\165\0\31\0\167\0\31\0\170\0\31\0\171\0\31\0\204\0\31\0\206" +
		"\0\245\0\207\0\31\0\211\0\31\0\217\0\31\0\221\0\31\0\227\0\31\0\234\0\31\0\241\0" +
		"\31\0\243\0\31\0\244\0\31\0\252\0\31\0\254\0\31\0\257\0\31\0\260\0\31\0\261\0\31" +
		"\0\265\0\31\0\301\0\31\0\320\0\31\0\321\0\31\0\323\0\31\0\325\0\31\0\326\0\31\0\327" +
		"\0\31\0\330\0\31\0\343\0\31\0\345\0\31\0\357\0\31\0\362\0\31\0\366\0\31\0\373\0\31" +
		"\0\375\0\31\0\376\0\31\0\u0101\0\31\0\u010f\0\31\0\u0111\0\31\0\u0115\0\31\0\u0118" +
		"\0\31\0\u0119\0\31\0\u011b\0\31\0\u0120\0\31\0\u0121\0\31\0\u0125\0\31\0\u0126\0" +
		"\31\0\u0127\0\31\0\u0128\0\31\0\u0129\0\31\0\u012b\0\31\0\u0133\0\31\0\u0136\0\31" +
		"\0\u0138\0\31\0\u013e\0\31\0\u0141\0\31\0\u0143\0\31\0\u0145\0\31\0\u0147\0\31\0" +
		"\u0148\0\31\0\u0154\0\31\0\u015c\0\31\0\u015e\0\31\0\u0160\0\31\0\u0163\0\31\0\u0165" +
		"\0\31\0\u0166\0\31\0\u0167\0\31\0\u0174\0\31\0\u0176\0\31\0\u017c\0\31\0\u0180\0" +
		"\31\0\u0183\0\31\0\u0185\0\31\0\u0195\0\31\0\u019b\0\31\0\u019d\0\31\0\u01aa\0\31" +
		"\0\1\0\32\0\2\0\32\0\6\0\32\0\65\0\32\0\67\0\32\0\70\0\32\0\74\0\32\0\75\0\32\0\77" +
		"\0\32\0\101\0\32\0\102\0\32\0\116\0\135\0\126\0\32\0\127\0\32\0\135\0\32\0\136\0" +
		"\32\0\152\0\32\0\154\0\32\0\160\0\32\0\165\0\32\0\167\0\32\0\170\0\32\0\171\0\32" +
		"\0\204\0\32\0\207\0\32\0\211\0\32\0\217\0\32\0\221\0\32\0\227\0\32\0\234\0\32\0\241" +
		"\0\32\0\243\0\32\0\244\0\32\0\252\0\32\0\254\0\32\0\257\0\32\0\260\0\32\0\261\0\32" +
		"\0\265\0\32\0\301\0\32\0\320\0\32\0\321\0\32\0\323\0\32\0\325\0\32\0\326\0\32\0\327" +
		"\0\32\0\330\0\32\0\343\0\32\0\345\0\32\0\357\0\32\0\362\0\32\0\366\0\32\0\373\0\32" +
		"\0\375\0\32\0\376\0\32\0\u0101\0\32\0\u010f\0\32\0\u0111\0\32\0\u0115\0\32\0\u0118" +
		"\0\32\0\u0119\0\32\0\u011b\0\32\0\u0120\0\32\0\u0121\0\32\0\u0125\0\32\0\u0126\0" +
		"\32\0\u0127\0\32\0\u0128\0\32\0\u0129\0\32\0\u012b\0\32\0\u0133\0\32\0\u0136\0\32" +
		"\0\u0138\0\32\0\u013e\0\32\0\u0141\0\32\0\u0143\0\32\0\u0145\0\32\0\u0147\0\32\0" +
		"\u0148\0\32\0\u0154\0\32\0\u015c\0\32\0\u015e\0\32\0\u0160\0\32\0\u0163\0\32\0\u0165" +
		"\0\32\0\u0166\0\32\0\u0167\0\32\0\u0174\0\32\0\u0176\0\32\0\u017c\0\32\0\u0180\0" +
		"\32\0\u0183\0\32\0\u0185\0\32\0\u0195\0\32\0\u019b\0\32\0\u019d\0\32\0\u01aa\0\32" +
		"\0\1\0\33\0\2\0\33\0\6\0\33\0\65\0\33\0\67\0\33\0\70\0\33\0\74\0\33\0\75\0\33\0\77" +
		"\0\33\0\101\0\33\0\102\0\33\0\126\0\33\0\127\0\33\0\135\0\33\0\136\0\33\0\152\0\33" +
		"\0\154\0\33\0\160\0\33\0\165\0\33\0\167\0\33\0\170\0\33\0\171\0\33\0\204\0\33\0\206" +
		"\0\246\0\207\0\33\0\211\0\33\0\217\0\33\0\221\0\33\0\227\0\33\0\234\0\33\0\241\0" +
		"\33\0\243\0\33\0\244\0\33\0\252\0\33\0\254\0\33\0\257\0\33\0\260\0\33\0\261\0\33" +
		"\0\265\0\33\0\301\0\33\0\320\0\33\0\321\0\33\0\323\0\33\0\325\0\33\0\326\0\33\0\327" +
		"\0\33\0\330\0\33\0\343\0\33\0\345\0\33\0\357\0\33\0\362\0\33\0\366\0\33\0\373\0\33" +
		"\0\375\0\33\0\376\0\33\0\u0101\0\33\0\u010f\0\33\0\u0111\0\33\0\u0115\0\33\0\u0118" +
		"\0\33\0\u0119\0\33\0\u011b\0\33\0\u0120\0\33\0\u0121\0\33\0\u0125\0\33\0\u0126\0" +
		"\33\0\u0127\0\33\0\u0128\0\33\0\u0129\0\33\0\u012b\0\33\0\u0133\0\33\0\u0136\0\33" +
		"\0\u0138\0\33\0\u013e\0\33\0\u0141\0\33\0\u0143\0\33\0\u0145\0\33\0\u0147\0\33\0" +
		"\u0148\0\33\0\u0154\0\33\0\u015c\0\33\0\u015e\0\33\0\u0160\0\33\0\u0163\0\33\0\u0165" +
		"\0\33\0\u0166\0\33\0\u0167\0\33\0\u0174\0\33\0\u0176\0\33\0\u017c\0\33\0\u0180\0" +
		"\33\0\u0183\0\33\0\u0185\0\33\0\u0195\0\33\0\u019b\0\33\0\u019d\0\33\0\u01aa\0\33" +
		"\0\1\0\34\0\2\0\34\0\6\0\34\0\65\0\34\0\67\0\34\0\70\0\34\0\74\0\34\0\75\0\34\0\77" +
		"\0\34\0\101\0\34\0\102\0\34\0\126\0\34\0\127\0\34\0\135\0\34\0\136\0\34\0\152\0\34" +
		"\0\154\0\34\0\160\0\34\0\165\0\34\0\167\0\34\0\170\0\34\0\171\0\34\0\204\0\34\0\207" +
		"\0\34\0\211\0\34\0\217\0\34\0\221\0\34\0\227\0\34\0\234\0\34\0\241\0\34\0\243\0\34" +
		"\0\244\0\34\0\252\0\34\0\254\0\34\0\257\0\34\0\260\0\34\0\261\0\34\0\265\0\34\0\301" +
		"\0\34\0\312\0\u010d\0\320\0\34\0\321\0\34\0\323\0\34\0\325\0\34\0\326\0\34\0\327" +
		"\0\34\0\330\0\34\0\343\0\34\0\345\0\34\0\357\0\34\0\362\0\34\0\366\0\34\0\373\0\34" +
		"\0\375\0\34\0\376\0\34\0\u0101\0\34\0\u010f\0\34\0\u0111\0\34\0\u0115\0\34\0\u0118" +
		"\0\34\0\u0119\0\34\0\u011b\0\34\0\u0120\0\34\0\u0121\0\34\0\u0125\0\34\0\u0126\0" +
		"\34\0\u0127\0\34\0\u0128\0\34\0\u0129\0\34\0\u012b\0\34\0\u0133\0\34\0\u0136\0\34" +
		"\0\u0138\0\34\0\u013e\0\34\0\u0141\0\34\0\u0143\0\34\0\u0145\0\34\0\u0147\0\34\0" +
		"\u0148\0\34\0\u0154\0\34\0\u015c\0\34\0\u015e\0\34\0\u0160\0\34\0\u0163\0\34\0\u0165" +
		"\0\34\0\u0166\0\34\0\u0167\0\34\0\u0174\0\34\0\u0176\0\34\0\u017c\0\34\0\u0180\0" +
		"\34\0\u0183\0\34\0\u0185\0\34\0\u0195\0\34\0\u019b\0\34\0\u019d\0\34\0\u01aa\0\34" +
		"\0\1\0\35\0\2\0\35\0\6\0\35\0\65\0\35\0\67\0\35\0\70\0\35\0\74\0\35\0\75\0\35\0\77" +
		"\0\35\0\101\0\35\0\102\0\35\0\126\0\35\0\127\0\35\0\135\0\35\0\136\0\35\0\152\0\35" +
		"\0\154\0\35\0\160\0\35\0\165\0\35\0\167\0\35\0\170\0\35\0\171\0\35\0\204\0\35\0\206" +
		"\0\247\0\207\0\35\0\211\0\35\0\217\0\35\0\221\0\35\0\227\0\35\0\234\0\35\0\241\0" +
		"\35\0\243\0\35\0\244\0\35\0\252\0\35\0\254\0\35\0\257\0\35\0\260\0\35\0\261\0\35" +
		"\0\265\0\35\0\301\0\35\0\320\0\35\0\321\0\35\0\323\0\35\0\325\0\35\0\326\0\35\0\327" +
		"\0\35\0\330\0\35\0\343\0\35\0\345\0\35\0\357\0\35\0\362\0\35\0\366\0\35\0\373\0\35" +
		"\0\375\0\35\0\376\0\35\0\u0101\0\35\0\u010f\0\35\0\u0111\0\35\0\u0115\0\35\0\u0118" +
		"\0\35\0\u0119\0\35\0\u011b\0\35\0\u0120\0\35\0\u0121\0\35\0\u0125\0\35\0\u0126\0" +
		"\35\0\u0127\0\35\0\u0128\0\35\0\u0129\0\35\0\u012b\0\35\0\u0133\0\35\0\u0136\0\35" +
		"\0\u0138\0\35\0\u013e\0\35\0\u0141\0\35\0\u0143\0\35\0\u0145\0\35\0\u0147\0\35\0" +
		"\u0148\0\35\0\u0154\0\35\0\u015c\0\35\0\u015e\0\35\0\u0160\0\35\0\u0163\0\35\0\u0165" +
		"\0\35\0\u0166\0\35\0\u0167\0\35\0\u0174\0\35\0\u0176\0\35\0\u017c\0\35\0\u0180\0" +
		"\35\0\u0183\0\35\0\u0185\0\35\0\u0195\0\35\0\u019b\0\35\0\u019d\0\35\0\u01aa\0\35" +
		"\0\1\0\36\0\2\0\36\0\6\0\36\0\65\0\36\0\67\0\36\0\70\0\36\0\74\0\36\0\75\0\36\0\77" +
		"\0\36\0\101\0\36\0\102\0\36\0\126\0\36\0\127\0\36\0\135\0\36\0\136\0\36\0\152\0\36" +
		"\0\154\0\36\0\160\0\36\0\165\0\36\0\167\0\36\0\170\0\36\0\171\0\36\0\204\0\36\0\207" +
		"\0\36\0\211\0\36\0\217\0\36\0\221\0\36\0\227\0\36\0\234\0\36\0\236\0\310\0\241\0" +
		"\36\0\243\0\36\0\244\0\36\0\252\0\36\0\254\0\36\0\257\0\36\0\260\0\36\0\261\0\36" +
		"\0\265\0\36\0\301\0\36\0\320\0\36\0\321\0\36\0\323\0\36\0\325\0\36\0\326\0\36\0\327" +
		"\0\36\0\330\0\36\0\343\0\36\0\345\0\36\0\357\0\36\0\362\0\36\0\366\0\36\0\373\0\36" +
		"\0\375\0\36\0\376\0\36\0\u0101\0\36\0\u010f\0\36\0\u0111\0\36\0\u0115\0\36\0\u0118" +
		"\0\36\0\u0119\0\36\0\u011b\0\36\0\u0120\0\36\0\u0121\0\36\0\u0125\0\36\0\u0126\0" +
		"\36\0\u0127\0\36\0\u0128\0\36\0\u0129\0\36\0\u012b\0\36\0\u0133\0\36\0\u0136\0\36" +
		"\0\u0138\0\36\0\u013e\0\36\0\u0141\0\36\0\u0143\0\36\0\u0145\0\36\0\u0147\0\36\0" +
		"\u0148\0\36\0\u0154\0\36\0\u015c\0\36\0\u015e\0\36\0\u0160\0\36\0\u0163\0\36\0\u0165" +
		"\0\36\0\u0166\0\36\0\u0167\0\36\0\u0174\0\36\0\u0176\0\36\0\u017c\0\36\0\u0180\0" +
		"\36\0\u0183\0\36\0\u0185\0\36\0\u0195\0\36\0\u019b\0\36\0\u019d\0\36\0\u01aa\0\36" +
		"\0\1\0\37\0\2\0\37\0\6\0\37\0\65\0\37\0\67\0\37\0\70\0\37\0\74\0\37\0\75\0\37\0\77" +
		"\0\37\0\101\0\37\0\102\0\37\0\126\0\37\0\127\0\37\0\135\0\37\0\136\0\37\0\152\0\37" +
		"\0\154\0\37\0\160\0\37\0\165\0\37\0\167\0\37\0\170\0\37\0\171\0\37\0\204\0\37\0\206" +
		"\0\250\0\207\0\37\0\211\0\37\0\217\0\37\0\221\0\37\0\227\0\37\0\234\0\37\0\241\0" +
		"\37\0\243\0\37\0\244\0\37\0\252\0\37\0\253\0\250\0\254\0\37\0\257\0\37\0\260\0\37" +
		"\0\261\0\37\0\265\0\37\0\301\0\37\0\320\0\37\0\321\0\37\0\323\0\37\0\325\0\37\0\326" +
		"\0\37\0\327\0\37\0\330\0\37\0\343\0\37\0\345\0\37\0\357\0\37\0\362\0\37\0\366\0\37" +
		"\0\373\0\37\0\375\0\37\0\376\0\37\0\u0101\0\37\0\u010f\0\37\0\u0111\0\37\0\u0115" +
		"\0\37\0\u0118\0\37\0\u0119\0\37\0\u011b\0\37\0\u0120\0\37\0\u0121\0\37\0\u0125\0" +
		"\37\0\u0126\0\37\0\u0127\0\37\0\u0128\0\37\0\u0129\0\37\0\u012b\0\37\0\u0133\0\37" +
		"\0\u0136\0\37\0\u0138\0\37\0\u013e\0\37\0\u0141\0\37\0\u0143\0\37\0\u0145\0\37\0" +
		"\u0147\0\37\0\u0148\0\37\0\u0154\0\37\0\u015c\0\37\0\u015e\0\37\0\u0160\0\37\0\u0163" +
		"\0\37\0\u0165\0\37\0\u0166\0\37\0\u0167\0\37\0\u0174\0\37\0\u0176\0\37\0\u017c\0" +
		"\37\0\u0180\0\37\0\u0183\0\37\0\u0185\0\37\0\u0195\0\37\0\u019b\0\37\0\u019d\0\37" +
		"\0\u01aa\0\37\0\1\0\40\0\2\0\40\0\6\0\40\0\65\0\40\0\67\0\40\0\70\0\40\0\74\0\40" +
		"\0\75\0\40\0\77\0\40\0\101\0\40\0\102\0\40\0\126\0\40\0\127\0\40\0\135\0\40\0\136" +
		"\0\40\0\137\0\165\0\152\0\40\0\154\0\40\0\160\0\40\0\165\0\40\0\167\0\40\0\170\0" +
		"\40\0\171\0\40\0\204\0\40\0\207\0\40\0\211\0\40\0\217\0\40\0\221\0\40\0\227\0\40" +
		"\0\234\0\40\0\241\0\40\0\243\0\40\0\244\0\40\0\252\0\40\0\254\0\40\0\257\0\40\0\260" +
		"\0\40\0\261\0\40\0\265\0\40\0\301\0\40\0\320\0\40\0\321\0\40\0\323\0\40\0\325\0\40" +
		"\0\326\0\40\0\327\0\40\0\330\0\40\0\343\0\40\0\345\0\40\0\357\0\40\0\362\0\40\0\366" +
		"\0\40\0\373\0\40\0\375\0\40\0\376\0\40\0\u0101\0\40\0\u010f\0\40\0\u0111\0\40\0\u0115" +
		"\0\40\0\u0118\0\40\0\u0119\0\40\0\u011b\0\40\0\u0120\0\40\0\u0121\0\40\0\u0125\0" +
		"\40\0\u0126\0\40\0\u0127\0\40\0\u0128\0\40\0\u0129\0\40\0\u012b\0\40\0\u0133\0\40" +
		"\0\u0136\0\40\0\u0138\0\40\0\u013e\0\40\0\u0141\0\40\0\u0143\0\40\0\u0145\0\40\0" +
		"\u0147\0\40\0\u0148\0\40\0\u0154\0\40\0\u015c\0\40\0\u015e\0\40\0\u0160\0\40\0\u0163" +
		"\0\40\0\u0165\0\40\0\u0166\0\40\0\u0167\0\40\0\u0174\0\40\0\u0176\0\40\0\u017c\0" +
		"\40\0\u0180\0\40\0\u0183\0\40\0\u0185\0\40\0\u0195\0\40\0\u019b\0\40\0\u019d\0\40" +
		"\0\u01aa\0\40\0\1\0\41\0\2\0\41\0\6\0\41\0\65\0\41\0\67\0\41\0\70\0\41\0\74\0\41" +
		"\0\75\0\41\0\77\0\41\0\101\0\41\0\102\0\41\0\126\0\41\0\127\0\41\0\135\0\41\0\136" +
		"\0\41\0\152\0\41\0\154\0\41\0\160\0\41\0\165\0\41\0\167\0\41\0\170\0\41\0\171\0\41" +
		"\0\204\0\41\0\207\0\41\0\211\0\41\0\217\0\41\0\221\0\41\0\227\0\41\0\234\0\41\0\241" +
		"\0\41\0\243\0\41\0\244\0\41\0\252\0\41\0\254\0\41\0\257\0\41\0\260\0\41\0\261\0\41" +
		"\0\265\0\41\0\301\0\41\0\320\0\41\0\321\0\41\0\323\0\41\0\324\0\u0118\0\325\0\41" +
		"\0\326\0\41\0\327\0\41\0\330\0\41\0\343\0\41\0\345\0\41\0\357\0\41\0\362\0\41\0\366" +
		"\0\41\0\373\0\41\0\375\0\41\0\376\0\41\0\u0101\0\41\0\u010f\0\41\0\u0111\0\41\0\u0115" +
		"\0\41\0\u0118\0\41\0\u0119\0\41\0\u011b\0\41\0\u0120\0\41\0\u0121\0\41\0\u0125\0" +
		"\41\0\u0126\0\41\0\u0127\0\41\0\u0128\0\41\0\u0129\0\41\0\u012b\0\41\0\u0133\0\41" +
		"\0\u0136\0\41\0\u0138\0\41\0\u013e\0\41\0\u0141\0\41\0\u0143\0\41\0\u0145\0\41\0" +
		"\u0147\0\41\0\u0148\0\41\0\u0154\0\41\0\u015c\0\41\0\u015e\0\41\0\u0160\0\41\0\u0163" +
		"\0\41\0\u0165\0\41\0\u0166\0\41\0\u0167\0\41\0\u0174\0\41\0\u0176\0\41\0\u017c\0" +
		"\41\0\u0180\0\41\0\u0183\0\41\0\u0185\0\41\0\u0195\0\41\0\u019b\0\41\0\u019d\0\41" +
		"\0\u01aa\0\41\0\1\0\42\0\2\0\42\0\6\0\42\0\65\0\42\0\67\0\42\0\70\0\42\0\74\0\42" +
		"\0\75\0\42\0\77\0\42\0\101\0\42\0\102\0\42\0\126\0\42\0\127\0\42\0\135\0\42\0\136" +
		"\0\42\0\152\0\42\0\154\0\42\0\160\0\42\0\165\0\42\0\167\0\42\0\170\0\42\0\171\0\42" +
		"\0\204\0\42\0\207\0\42\0\210\0\265\0\211\0\42\0\217\0\42\0\221\0\42\0\227\0\42\0" +
		"\234\0\42\0\241\0\42\0\243\0\42\0\244\0\42\0\252\0\42\0\254\0\42\0\257\0\42\0\260" +
		"\0\42\0\261\0\42\0\265\0\42\0\272\0\265\0\276\0\265\0\301\0\42\0\320\0\42\0\321\0" +
		"\42\0\323\0\42\0\325\0\42\0\326\0\42\0\327\0\42\0\330\0\42\0\343\0\42\0\345\0\42" +
		"\0\357\0\42\0\362\0\42\0\366\0\42\0\373\0\42\0\375\0\42\0\376\0\42\0\u0101\0\42\0" +
		"\u0104\0\265\0\u010f\0\42\0\u0111\0\42\0\u0115\0\42\0\u0118\0\42\0\u0119\0\42\0\u011b" +
		"\0\42\0\u0120\0\42\0\u0121\0\42\0\u0125\0\42\0\u0126\0\42\0\u0127\0\42\0\u0128\0" +
		"\42\0\u0129\0\42\0\u012b\0\42\0\u0133\0\42\0\u0136\0\42\0\u0138\0\42\0\u013e\0\42" +
		"\0\u0141\0\42\0\u0143\0\42\0\u0145\0\42\0\u0147\0\42\0\u0148\0\42\0\u0154\0\42\0" +
		"\u015c\0\42\0\u015e\0\42\0\u0160\0\42\0\u0163\0\42\0\u0165\0\42\0\u0166\0\42\0\u0167" +
		"\0\42\0\u0174\0\42\0\u0176\0\42\0\u017c\0\42\0\u0180\0\42\0\u0183\0\42\0\u0185\0" +
		"\42\0\u0195\0\42\0\u019b\0\42\0\u019d\0\42\0\u01aa\0\42\0\1\0\43\0\2\0\43\0\6\0\43" +
		"\0\65\0\43\0\67\0\43\0\70\0\43\0\74\0\43\0\75\0\43\0\77\0\43\0\101\0\43\0\102\0\43" +
		"\0\126\0\43\0\127\0\43\0\135\0\43\0\136\0\43\0\152\0\43\0\154\0\43\0\160\0\43\0\165" +
		"\0\43\0\167\0\43\0\170\0\43\0\171\0\43\0\204\0\43\0\206\0\251\0\207\0\43\0\211\0" +
		"\43\0\217\0\43\0\221\0\43\0\227\0\43\0\234\0\43\0\241\0\43\0\243\0\43\0\244\0\43" +
		"\0\252\0\43\0\254\0\43\0\257\0\43\0\260\0\43\0\261\0\43\0\265\0\43\0\301\0\43\0\320" +
		"\0\43\0\321\0\43\0\323\0\43\0\325\0\43\0\326\0\43\0\327\0\43\0\330\0\43\0\343\0\43" +
		"\0\345\0\43\0\357\0\43\0\362\0\43\0\366\0\43\0\373\0\43\0\375\0\43\0\376\0\43\0\u0101" +
		"\0\43\0\u010f\0\43\0\u0111\0\43\0\u0115\0\43\0\u0118\0\43\0\u0119\0\43\0\u011b\0" +
		"\43\0\u0120\0\43\0\u0121\0\43\0\u0125\0\43\0\u0126\0\43\0\u0127\0\43\0\u0128\0\43" +
		"\0\u0129\0\43\0\u012b\0\43\0\u0133\0\43\0\u0136\0\43\0\u0138\0\43\0\u013e\0\43\0" +
		"\u0141\0\43\0\u0143\0\43\0\u0145\0\43\0\u0147\0\43\0\u0148\0\43\0\u0154\0\43\0\u015c" +
		"\0\43\0\u015e\0\43\0\u0160\0\43\0\u0163\0\43\0\u0165\0\43\0\u0166\0\43\0\u0167\0" +
		"\43\0\u0174\0\43\0\u0176\0\43\0\u017c\0\43\0\u0180\0\43\0\u0183\0\43\0\u0185\0\43" +
		"\0\u0195\0\43\0\u019b\0\43\0\u019d\0\43\0\u01aa\0\43\0\1\0\44\0\2\0\44\0\6\0\44\0" +
		"\65\0\44\0\67\0\44\0\70\0\44\0\74\0\44\0\75\0\44\0\77\0\44\0\101\0\44\0\102\0\44" +
		"\0\126\0\44\0\127\0\44\0\135\0\44\0\136\0\44\0\151\0\170\0\152\0\44\0\154\0\44\0" +
		"\160\0\44\0\165\0\44\0\167\0\44\0\170\0\44\0\171\0\44\0\204\0\44\0\207\0\44\0\211" +
		"\0\44\0\217\0\44\0\221\0\44\0\227\0\44\0\234\0\44\0\241\0\44\0\243\0\44\0\244\0\44" +
		"\0\252\0\44\0\254\0\44\0\257\0\44\0\260\0\44\0\261\0\44\0\265\0\44\0\301\0\44\0\320" +
		"\0\44\0\321\0\44\0\323\0\44\0\325\0\44\0\326\0\44\0\327\0\44\0\330\0\44\0\343\0\44" +
		"\0\345\0\44\0\357\0\44\0\362\0\44\0\366\0\44\0\373\0\44\0\375\0\44\0\376\0\44\0\u0101" +
		"\0\44\0\u010f\0\44\0\u0111\0\44\0\u0115\0\44\0\u0118\0\44\0\u0119\0\44\0\u011b\0" +
		"\44\0\u0120\0\44\0\u0121\0\44\0\u0125\0\44\0\u0126\0\44\0\u0127\0\44\0\u0128\0\44" +
		"\0\u0129\0\44\0\u012b\0\44\0\u0133\0\44\0\u0136\0\44\0\u0138\0\44\0\u013e\0\44\0" +
		"\u0141\0\44\0\u0143\0\44\0\u0145\0\44\0\u0147\0\44\0\u0148\0\44\0\u0154\0\44\0\u015c" +
		"\0\44\0\u015e\0\44\0\u0160\0\44\0\u0163\0\44\0\u0165\0\44\0\u0166\0\44\0\u0167\0" +
		"\44\0\u0174\0\44\0\u0176\0\44\0\u017c\0\44\0\u0180\0\44\0\u0183\0\44\0\u0185\0\44" +
		"\0\u0195\0\44\0\u019b\0\44\0\u019d\0\44\0\u01aa\0\44\0\1\0\45\0\2\0\45\0\6\0\45\0" +
		"\65\0\45\0\67\0\45\0\70\0\45\0\74\0\45\0\75\0\45\0\77\0\45\0\101\0\45\0\102\0\45" +
		"\0\126\0\45\0\127\0\45\0\135\0\45\0\136\0\45\0\152\0\45\0\154\0\45\0\160\0\45\0\165" +
		"\0\45\0\167\0\45\0\170\0\45\0\171\0\45\0\204\0\45\0\207\0\45\0\211\0\45\0\217\0\45" +
		"\0\221\0\45\0\227\0\45\0\234\0\45\0\241\0\45\0\243\0\45\0\244\0\45\0\252\0\45\0\254" +
		"\0\45\0\257\0\45\0\260\0\45\0\261\0\45\0\265\0\45\0\301\0\45\0\320\0\45\0\321\0\45" +
		"\0\323\0\45\0\324\0\u0119\0\325\0\45\0\326\0\45\0\327\0\45\0\330\0\45\0\343\0\45" +
		"\0\345\0\45\0\357\0\45\0\362\0\45\0\366\0\45\0\373\0\45\0\375\0\45\0\376\0\45\0\u0101" +
		"\0\45\0\u010f\0\45\0\u0111\0\45\0\u0115\0\45\0\u0118\0\45\0\u0119\0\45\0\u011b\0" +
		"\45\0\u0120\0\45\0\u0121\0\45\0\u0125\0\45\0\u0126\0\45\0\u0127\0\45\0\u0128\0\45" +
		"\0\u0129\0\45\0\u012b\0\45\0\u0133\0\45\0\u0136\0\45\0\u0138\0\45\0\u013e\0\45\0" +
		"\u0141\0\45\0\u0143\0\45\0\u0145\0\45\0\u0147\0\45\0\u0148\0\45\0\u0154\0\45\0\u015c" +
		"\0\45\0\u015e\0\45\0\u0160\0\45\0\u0163\0\45\0\u0165\0\45\0\u0166\0\45\0\u0167\0" +
		"\45\0\u0174\0\45\0\u0176\0\45\0\u017c\0\45\0\u0180\0\45\0\u0183\0\45\0\u0185\0\45" +
		"\0\u0195\0\45\0\u019b\0\45\0\u019d\0\45\0\u01aa\0\45\0\1\0\46\0\2\0\46\0\6\0\46\0" +
		"\65\0\46\0\67\0\46\0\70\0\46\0\74\0\46\0\75\0\46\0\77\0\46\0\101\0\46\0\102\0\46" +
		"\0\126\0\46\0\127\0\46\0\135\0\46\0\136\0\46\0\152\0\46\0\154\0\46\0\160\0\46\0\165" +
		"\0\46\0\167\0\46\0\170\0\46\0\171\0\46\0\204\0\46\0\207\0\46\0\211\0\46\0\217\0\46" +
		"\0\221\0\46\0\227\0\46\0\234\0\46\0\241\0\46\0\243\0\46\0\244\0\46\0\252\0\46\0\254" +
		"\0\46\0\257\0\46\0\260\0\46\0\261\0\46\0\265\0\46\0\301\0\46\0\320\0\46\0\321\0\46" +
		"\0\323\0\46\0\325\0\46\0\326\0\46\0\327\0\46\0\330\0\46\0\343\0\46\0\345\0\46\0\357" +
		"\0\46\0\362\0\46\0\366\0\46\0\373\0\46\0\375\0\46\0\376\0\46\0\u0101\0\46\0\u010f" +
		"\0\46\0\u0111\0\46\0\u0115\0\46\0\u0118\0\46\0\u0119\0\46\0\u011b\0\46\0\u0120\0" +
		"\46\0\u0121\0\46\0\u0125\0\46\0\u0126\0\46\0\u0127\0\46\0\u0128\0\46\0\u0129\0\46" +
		"\0\u012b\0\46\0\u0133\0\46\0\u0136\0\46\0\u0138\0\46\0\u013e\0\46\0\u0141\0\46\0" +
		"\u0143\0\46\0\u0145\0\46\0\u0147\0\46\0\u0148\0\46\0\u014b\0\u0188\0\u0154\0\46\0" +
		"\u015c\0\46\0\u015e\0\46\0\u0160\0\46\0\u0163\0\46\0\u0165\0\46\0\u0166\0\46\0\u0167" +
		"\0\46\0\u0174\0\46\0\u0176\0\46\0\u017c\0\46\0\u0180\0\46\0\u0183\0\46\0\u0185\0" +
		"\46\0\u0195\0\46\0\u019b\0\46\0\u019d\0\46\0\u01aa\0\46\0\1\0\47\0\2\0\47\0\6\0\47" +
		"\0\65\0\47\0\67\0\47\0\70\0\47\0\74\0\47\0\75\0\47\0\77\0\47\0\101\0\47\0\102\0\47" +
		"\0\126\0\47\0\127\0\47\0\135\0\47\0\136\0\47\0\152\0\47\0\154\0\47\0\160\0\47\0\165" +
		"\0\47\0\167\0\47\0\170\0\47\0\171\0\47\0\204\0\47\0\207\0\47\0\211\0\47\0\217\0\47" +
		"\0\221\0\47\0\227\0\47\0\234\0\47\0\241\0\47\0\243\0\47\0\244\0\47\0\252\0\47\0\254" +
		"\0\47\0\257\0\47\0\260\0\47\0\261\0\47\0\265\0\47\0\301\0\47\0\320\0\47\0\321\0\47" +
		"\0\323\0\47\0\325\0\47\0\326\0\47\0\327\0\47\0\330\0\47\0\343\0\47\0\345\0\47\0\357" +
		"\0\47\0\362\0\47\0\366\0\47\0\373\0\47\0\375\0\47\0\376\0\47\0\u0101\0\47\0\u010f" +
		"\0\47\0\u0111\0\47\0\u0115\0\47\0\u0118\0\47\0\u0119\0\47\0\u011b\0\47\0\u0120\0" +
		"\47\0\u0121\0\47\0\u0125\0\47\0\u0126\0\47\0\u0127\0\47\0\u0128\0\47\0\u0129\0\47" +
		"\0\u012b\0\47\0\u0133\0\47\0\u0136\0\47\0\u0138\0\47\0\u013e\0\47\0\u0141\0\47\0" +
		"\u0143\0\47\0\u0145\0\47\0\u0147\0\47\0\u0148\0\47\0\u014b\0\u0189\0\u0154\0\47\0" +
		"\u015c\0\47\0\u015e\0\47\0\u0160\0\47\0\u0163\0\47\0\u0165\0\47\0\u0166\0\47\0\u0167" +
		"\0\47\0\u0174\0\47\0\u0176\0\47\0\u017c\0\47\0\u0180\0\47\0\u0183\0\47\0\u0185\0" +
		"\47\0\u0195\0\47\0\u019b\0\47\0\u019d\0\47\0\u01aa\0\47\0\1\0\50\0\2\0\50\0\6\0\50" +
		"\0\65\0\50\0\67\0\50\0\70\0\50\0\74\0\50\0\75\0\50\0\77\0\50\0\101\0\50\0\102\0\50" +
		"\0\126\0\50\0\127\0\50\0\135\0\50\0\136\0\50\0\152\0\50\0\154\0\50\0\160\0\50\0\165" +
		"\0\50\0\167\0\50\0\170\0\50\0\171\0\50\0\204\0\50\0\207\0\50\0\210\0\266\0\211\0" +
		"\50\0\217\0\50\0\221\0\50\0\227\0\50\0\234\0\50\0\241\0\50\0\243\0\50\0\244\0\50" +
		"\0\252\0\50\0\254\0\50\0\257\0\50\0\260\0\50\0\261\0\50\0\265\0\50\0\272\0\266\0" +
		"\276\0\266\0\301\0\50\0\320\0\50\0\321\0\50\0\323\0\50\0\325\0\50\0\326\0\50\0\327" +
		"\0\50\0\330\0\50\0\343\0\50\0\345\0\50\0\357\0\50\0\362\0\50\0\366\0\50\0\373\0\50" +
		"\0\375\0\50\0\376\0\50\0\u0101\0\50\0\u0104\0\266\0\u010f\0\50\0\u0111\0\50\0\u0115" +
		"\0\50\0\u0118\0\50\0\u0119\0\50\0\u011b\0\50\0\u0120\0\50\0\u0121\0\50\0\u0125\0" +
		"\50\0\u0126\0\50\0\u0127\0\50\0\u0128\0\50\0\u0129\0\50\0\u012b\0\50\0\u0133\0\50" +
		"\0\u0136\0\50\0\u0138\0\50\0\u013e\0\50\0\u0141\0\50\0\u0143\0\50\0\u0145\0\50\0" +
		"\u0147\0\50\0\u0148\0\50\0\u0154\0\50\0\u015c\0\50\0\u015e\0\50\0\u0160\0\50\0\u0163" +
		"\0\50\0\u0165\0\50\0\u0166\0\50\0\u0167\0\50\0\u0174\0\50\0\u0176\0\50\0\u017c\0" +
		"\50\0\u0180\0\50\0\u0183\0\50\0\u0185\0\50\0\u0195\0\50\0\u019b\0\50\0\u019d\0\50" +
		"\0\u01aa\0\50\0\1\0\51\0\2\0\51\0\6\0\51\0\65\0\51\0\67\0\51\0\70\0\51\0\74\0\51" +
		"\0\75\0\51\0\77\0\51\0\101\0\51\0\102\0\51\0\126\0\51\0\127\0\51\0\135\0\51\0\136" +
		"\0\51\0\151\0\171\0\152\0\51\0\154\0\51\0\160\0\51\0\165\0\51\0\167\0\51\0\170\0" +
		"\51\0\171\0\51\0\204\0\51\0\207\0\51\0\211\0\51\0\217\0\51\0\221\0\51\0\227\0\51" +
		"\0\234\0\51\0\241\0\51\0\243\0\51\0\244\0\51\0\252\0\51\0\254\0\51\0\257\0\51\0\260" +
		"\0\51\0\261\0\51\0\265\0\51\0\301\0\51\0\320\0\51\0\321\0\51\0\323\0\51\0\325\0\51" +
		"\0\326\0\51\0\327\0\51\0\330\0\51\0\343\0\51\0\345\0\51\0\357\0\51\0\362\0\51\0\366" +
		"\0\51\0\373\0\51\0\375\0\51\0\376\0\51\0\u0101\0\51\0\u010f\0\51\0\u0111\0\51\0\u0115" +
		"\0\51\0\u0118\0\51\0\u0119\0\51\0\u011b\0\51\0\u0120\0\51\0\u0121\0\51\0\u0125\0" +
		"\51\0\u0126\0\51\0\u0127\0\51\0\u0128\0\51\0\u0129\0\51\0\u012b\0\51\0\u0133\0\51" +
		"\0\u0136\0\51\0\u0138\0\51\0\u013e\0\51\0\u0141\0\51\0\u0143\0\51\0\u0145\0\51\0" +
		"\u0147\0\51\0\u0148\0\51\0\u0154\0\51\0\u015c\0\51\0\u015e\0\51\0\u0160\0\51\0\u0163" +
		"\0\51\0\u0165\0\51\0\u0166\0\51\0\u0167\0\51\0\u0174\0\51\0\u0176\0\51\0\u017c\0" +
		"\51\0\u0180\0\51\0\u0183\0\51\0\u0185\0\51\0\u0195\0\51\0\u019b\0\51\0\u019d\0\51" +
		"\0\u01aa\0\51\0\153\0\177\0\205\0\177\0\210\0\177\0\257\0\333\0\272\0\177\0\276\0" +
		"\177\0\327\0\333\0\343\0\333\0\345\0\333\0\373\0\333\0\375\0\333\0\376\0\333\0\u0101" +
		"\0\333\0\u0104\0\177\0\u0120\0\333\0\u0125\0\333\0\u0129\0\333\0\u012b\0\333\0\u013e" +
		"\0\333\0\u0141\0\333\0\u0143\0\333\0\u0145\0\333\0\u0147\0\333\0\u0148\0\333\0\u014d" +
		"\0\333\0\u017c\0\333\0\u0180\0\333\0\u0183\0\333\0\u0185\0\333\0\u018d\0\333\0\u01aa" +
		"\0\333\0\160\0\204\0\176\0\231\0\233\0\231\0\306\0\231\0\361\0\u0136\0\1\0\52\0\2" +
		"\0\56\0\6\0\52\0\65\0\103\0\67\0\110\0\70\0\56\0\74\0\115\0\75\0\117\0\77\0\52\0" +
		"\101\0\103\0\102\0\103\0\126\0\141\0\127\0\103\0\135\0\153\0\136\0\52\0\152\0\173" +
		"\0\154\0\153\0\160\0\205\0\165\0\210\0\167\0\141\0\170\0\222\0\171\0\222\0\204\0" +
		"\153\0\207\0\255\0\211\0\210\0\217\0\276\0\221\0\141\0\227\0\173\0\234\0\153\0\241" +
		"\0\311\0\243\0\141\0\244\0\315\0\252\0\141\0\254\0\322\0\257\0\334\0\260\0\361\0" +
		"\261\0\362\0\265\0\141\0\301\0\222\0\320\0\141\0\321\0\u0114\0\323\0\52\0\325\0\u011a" +
		"\0\326\0\103\0\327\0\334\0\330\0\141\0\343\0\334\0\345\0\334\0\357\0\334\0\362\0" +
		"\u0137\0\366\0\141\0\373\0\334\0\375\0\334\0\376\0\334\0\u0101\0\334\0\u010f\0\141" +
		"\0\u0111\0\u0153\0\u0115\0\141\0\u0118\0\141\0\u0119\0\141\0\u011b\0\103\0\u0120" +
		"\0\334\0\u0121\0\141\0\u0125\0\334\0\u0126\0\u0167\0\u0127\0\52\0\u0128\0\52\0\u0129" +
		"\0\334\0\u012b\0\334\0\u0133\0\52\0\u0136\0\u0173\0\u0138\0\362\0\u013e\0\334\0\u0141" +
		"\0\334\0\u0143\0\334\0\u0145\0\334\0\u0147\0\334\0\u0148\0\334\0\u0154\0\141\0\u015c" +
		"\0\103\0\u015e\0\103\0\u0160\0\141\0\u0163\0\141\0\u0165\0\u0167\0\u0166\0\u0167" +
		"\0\u0167\0\52\0\u0174\0\141\0\u0176\0\141\0\u017c\0\334\0\u0180\0\334\0\u0183\0\334" +
		"\0\u0185\0\334\0\u0195\0\141\0\u019b\0\u0167\0\u019d\0\u0167\0\u01aa\0\334\0\1\0" +
		"\53\0\6\0\53\0\77\0\53\0\126\0\142\0\136\0\53\0\323\0\53\0\u0115\0\142\0\u0133\0" +
		"\u0171\0\u0154\0\142\0\u015a\0\u0191\0\u015b\0\u0192\0\u0174\0\142\0\176\0\232\0" +
		"\233\0\304\0\306\0\u0109\0\2\0\57\0\70\0\57\0\2\0\60\0\70\0\111\0\257\0\335\0\327" +
		"\0\335\0\343\0\335\0\345\0\335\0\373\0\335\0\375\0\335\0\376\0\335\0\u0101\0\335" +
		"\0\u0120\0\335\0\u0125\0\335\0\u0129\0\335\0\u012b\0\335\0\u013e\0\335\0\u0141\0" +
		"\335\0\u0143\0\335\0\u0145\0\335\0\u0147\0\335\0\u0148\0\335\0\u014d\0\u018b\0\u017c" +
		"\0\335\0\u0180\0\335\0\u0183\0\335\0\u0185\0\335\0\u018d\0\u018b\0\u01aa\0\335\0" +
		"\1\0\54\0\6\0\54\0\75\0\120\0\77\0\54\0\136\0\54\0\154\0\202\0\207\0\256\0\211\0" +
		"\273\0\234\0\202\0\257\0\336\0\323\0\54\0\327\0\336\0\345\0\u012d\0\373\0\336\0\375" +
		"\0\336\0\376\0\336\0\u0101\0\336\0\u0120\0\u012d\0\u0125\0\336\0\u0129\0\336\0\u012b" +
		"\0\u012d\0\u013e\0\336\0\u0141\0\336\0\u0143\0\336\0\u0145\0\336\0\u0147\0\336\0" +
		"\u0148\0\336\0\u017c\0\336\0\u0180\0\336\0\u0183\0\336\0\u0185\0\336\0\u01aa\0\336" +
		"\0\3\0\61\0\0\0\u01bb\0\61\0\75\0\0\0\3\0\75\0\121\0\121\0\140\0\60\0\72\0\131\0" +
		"\72\0\61\0\76\0\75\0\122\0\1\0\55\0\6\0\55\0\77\0\55\0\136\0\55\0\257\0\337\0\323" +
		"\0\55\0\327\0\337\0\343\0\337\0\345\0\337\0\357\0\337\0\373\0\337\0\375\0\337\0\376" +
		"\0\337\0\u0101\0\337\0\u0120\0\337\0\u0125\0\337\0\u0126\0\u0168\0\u0127\0\337\0" +
		"\u0128\0\337\0\u0129\0\337\0\u012b\0\337\0\u0133\0\u0172\0\u013e\0\337\0\u0141\0" +
		"\337\0\u0143\0\337\0\u0145\0\337\0\u0147\0\337\0\u0148\0\337\0\u0165\0\u0168\0\u0166" +
		"\0\u0168\0\u0167\0\u019a\0\u017c\0\337\0\u0180\0\337\0\u0183\0\337\0\u0185\0\337" +
		"\0\u019b\0\u0168\0\u019d\0\u0168\0\u01aa\0\337\0\126\0\143\0\167\0\221\0\221\0\277" +
		"\0\243\0\312\0\252\0\317\0\265\0\372\0\320\0\u0113\0\330\0\u0122\0\366\0\u013a\0" +
		"\u010f\0\312\0\u0115\0\143\0\u0118\0\u0157\0\u0119\0\u0158\0\u0121\0\u0161\0\u0154" +
		"\0\143\0\u0160\0\317\0\u0163\0\u0122\0\u0174\0\143\0\u0176\0\u01a0\0\u0195\0\u0113" +
		"\0\153\0\200\0\205\0\200\0\210\0\267\0\272\0\267\0\276\0\267\0\u0104\0\267\0\135" +
		"\0\154\0\204\0\234\0\135\0\155\0\154\0\203\0\204\0\155\0\234\0\203\0\135\0\156\0" +
		"\154\0\156\0\204\0\156\0\234\0\156\0\135\0\157\0\154\0\157\0\204\0\157\0\234\0\157" +
		"\0\135\0\160\0\154\0\160\0\204\0\160\0\234\0\160\0\152\0\174\0\135\0\161\0\154\0" +
		"\161\0\204\0\161\0\234\0\161\0\u0108\0\u014c\0\u014e\0\u014c\0\u014b\0\u018a\0\135" +
		"\0\162\0\154\0\162\0\204\0\162\0\234\0\162\0\170\0\223\0\171\0\225\0\135\0\163\0" +
		"\154\0\163\0\204\0\163\0\234\0\163\0\152\0\175\0\227\0\303\0\170\0\224\0\171\0\224" +
		"\0\301\0\u0106\0\165\0\211\0\165\0\212\0\211\0\274\0\165\0\213\0\211\0\213\0\210" +
		"\0\270\0\272\0\377\0\276\0\u0102\0\u0104\0\u0149\0\262\0\367\0\371\0\367\0\206\0" +
		"\252\0\206\0\253\0\165\0\214\0\211\0\214\0\165\0\215\0\211\0\215\0\244\0\316\0\243" +
		"\0\313\0\243\0\314\0\u010f\0\u0152\0\252\0\320\0\u0160\0\u0195\0\366\0\u013b\0\257" +
		"\0\340\0\327\0\340\0\373\0\340\0\375\0\340\0\376\0\340\0\u0101\0\340\0\u0125\0\340" +
		"\0\u013e\0\340\0\u0141\0\340\0\u0143\0\340\0\u0145\0\340\0\u0147\0\340\0\u0148\0" +
		"\340\0\u017c\0\340\0\u0180\0\340\0\u0183\0\340\0\u0185\0\340\0\u01aa\0\340\0\257" +
		"\0\341\0\327\0\u011f\0\373\0\u013d\0\375\0\u013f\0\376\0\u0140\0\u0101\0\u0144\0" +
		"\u0125\0\u0164\0\u013e\0\u0178\0\u0141\0\u017b\0\u0143\0\u017d\0\u0145\0\u017f\0" +
		"\u0147\0\u0181\0\u0148\0\u0182\0\u017c\0\u01a3\0\u0180\0\u01a6\0\u0183\0\u01a9\0" +
		"\u0185\0\u01ab\0\u01aa\0\u01b6\0\257\0\342\0\327\0\342\0\373\0\342\0\375\0\342\0" +
		"\376\0\342\0\u0101\0\342\0\u0125\0\342\0\u0129\0\u016d\0\u013e\0\342\0\u0141\0\342" +
		"\0\u0143\0\342\0\u0145\0\342\0\u0147\0\342\0\u0148\0\342\0\u017c\0\342\0\u0180\0" +
		"\342\0\u0183\0\342\0\u0185\0\342\0\u01aa\0\342\0\257\0\343\0\327\0\343\0\373\0\343" +
		"\0\375\0\343\0\376\0\343\0\u0101\0\343\0\u0125\0\343\0\u0129\0\343\0\u013e\0\343" +
		"\0\u0141\0\343\0\u0143\0\343\0\u0145\0\343\0\u0147\0\343\0\u0148\0\343\0\u017c\0" +
		"\343\0\u0180\0\343\0\u0183\0\343\0\u0185\0\343\0\u01aa\0\343\0\257\0\344\0\327\0" +
		"\344\0\343\0\344\0\345\0\344\0\373\0\344\0\375\0\344\0\376\0\344\0\u0101\0\344\0" +
		"\u0120\0\344\0\u0125\0\344\0\u0129\0\344\0\u012b\0\344\0\u013e\0\344\0\u0141\0\344" +
		"\0\u0143\0\344\0\u0145\0\344\0\u0147\0\344\0\u0148\0\344\0\u017c\0\344\0\u0180\0" +
		"\344\0\u0183\0\344\0\u0185\0\344\0\u01aa\0\344\0\210\0\271\0\270\0\374\0\272\0\u0100" +
		"\0\276\0\u0103\0\360\0\u0135\0\377\0\u0142\0\u0102\0\u0146\0\u0104\0\u014a\0\u012c" +
		"\0\u016f\0\u012f\0\u0170\0\u0149\0\u0184\0\u016e\0\u019e\0\257\0\345\0\327\0\u0120" +
		"\0\343\0\u012b\0\373\0\345\0\375\0\345\0\376\0\345\0\u0101\0\345\0\u0125\0\345\0" +
		"\u0129\0\345\0\u013e\0\345\0\u0141\0\345\0\u0143\0\345\0\u0145\0\345\0\u0147\0\345" +
		"\0\u0148\0\345\0\u017c\0\345\0\u0180\0\345\0\u0183\0\345\0\u0185\0\345\0\u01aa\0" +
		"\345\0\257\0\346\0\327\0\346\0\343\0\346\0\345\0\u012e\0\373\0\346\0\375\0\346\0" +
		"\376\0\346\0\u0101\0\346\0\u0120\0\u012e\0\u0125\0\346\0\u0129\0\346\0\u012b\0\u012e" +
		"\0\u013e\0\346\0\u0141\0\346\0\u0143\0\346\0\u0145\0\346\0\u0147\0\346\0\u0148\0" +
		"\346\0\u017c\0\346\0\u0180\0\346\0\u0183\0\346\0\u0185\0\346\0\u01aa\0\346\0\330" +
		"\0\u0123\0\257\0\347\0\327\0\347\0\343\0\347\0\345\0\347\0\373\0\347\0\375\0\347" +
		"\0\376\0\347\0\u0101\0\347\0\u0120\0\347\0\u0125\0\347\0\u0129\0\347\0\u012b\0\347" +
		"\0\u013e\0\347\0\u0141\0\347\0\u0143\0\347\0\u0145\0\347\0\u0147\0\347\0\u0148\0" +
		"\347\0\u017c\0\347\0\u0180\0\347\0\u0183\0\347\0\u0185\0\347\0\u01aa\0\347\0\330" +
		"\0\u0124\0\u0163\0\u0196\0\257\0\350\0\327\0\350\0\343\0\350\0\345\0\350\0\373\0" +
		"\350\0\375\0\350\0\376\0\350\0\u0101\0\350\0\u0120\0\350\0\u0125\0\350\0\u0129\0" +
		"\350\0\u012b\0\350\0\u013e\0\350\0\u0141\0\350\0\u0143\0\350\0\u0145\0\350\0\u0147" +
		"\0\350\0\u0148\0\350\0\u017c\0\350\0\u0180\0\350\0\u0183\0\350\0\u0185\0\350\0\u01aa" +
		"\0\350\0\257\0\351\0\327\0\351\0\343\0\351\0\345\0\351\0\373\0\351\0\375\0\351\0" +
		"\376\0\351\0\u0101\0\351\0\u0120\0\351\0\u0125\0\351\0\u0129\0\351\0\u012b\0\351" +
		"\0\u013e\0\351\0\u0141\0\351\0\u0143\0\351\0\u0145\0\351\0\u0147\0\351\0\u0148\0" +
		"\351\0\u017c\0\351\0\u0180\0\351\0\u0183\0\351\0\u0185\0\351\0\u01aa\0\351\0\257" +
		"\0\352\0\327\0\352\0\343\0\352\0\345\0\352\0\357\0\u0134\0\373\0\352\0\375\0\352" +
		"\0\376\0\352\0\u0101\0\352\0\u0120\0\352\0\u0125\0\352\0\u0129\0\352\0\u012b\0\352" +
		"\0\u013e\0\352\0\u0141\0\352\0\u0143\0\352\0\u0145\0\352\0\u0147\0\352\0\u0148\0" +
		"\352\0\u017c\0\352\0\u0180\0\352\0\u0183\0\352\0\u0185\0\352\0\u01aa\0\352\0\257" +
		"\0\353\0\327\0\353\0\343\0\353\0\345\0\353\0\357\0\353\0\373\0\353\0\375\0\353\0" +
		"\376\0\353\0\u0101\0\353\0\u0120\0\353\0\u0125\0\353\0\u0127\0\u016b\0\u0128\0\u016c" +
		"\0\u0129\0\353\0\u012b\0\353\0\u013e\0\353\0\u0141\0\353\0\u0143\0\353\0\u0145\0" +
		"\353\0\u0147\0\353\0\u0148\0\353\0\u017c\0\353\0\u0180\0\353\0\u0183\0\353\0\u0185" +
		"\0\353\0\u01aa\0\353\0\257\0\354\0\327\0\354\0\343\0\354\0\345\0\354\0\357\0\354" +
		"\0\373\0\354\0\375\0\354\0\376\0\354\0\u0101\0\354\0\u0120\0\354\0\u0125\0\354\0" +
		"\u0127\0\354\0\u0128\0\354\0\u0129\0\354\0\u012b\0\354\0\u013e\0\354\0\u0141\0\354" +
		"\0\u0143\0\354\0\u0145\0\354\0\u0147\0\354\0\u0148\0\354\0\u017c\0\354\0\u0180\0" +
		"\354\0\u0183\0\354\0\u0185\0\354\0\u01aa\0\354\0\257\0\355\0\327\0\355\0\343\0\355" +
		"\0\345\0\355\0\357\0\355\0\373\0\355\0\375\0\355\0\376\0\355\0\u0101\0\355\0\u0120" +
		"\0\355\0\u0125\0\355\0\u0127\0\355\0\u0128\0\355\0\u0129\0\355\0\u012b\0\355\0\u013e" +
		"\0\355\0\u0141\0\355\0\u0143\0\355\0\u0145\0\355\0\u0147\0\355\0\u0148\0\355\0\u017c" +
		"\0\355\0\u0180\0\355\0\u0183\0\355\0\u0185\0\355\0\u01aa\0\355\0\257\0\356\0\307" +
		"\0\u010a\0\310\0\u010b\0\327\0\356\0\343\0\356\0\345\0\356\0\357\0\356\0\373\0\356" +
		"\0\375\0\356\0\376\0\356\0\u0101\0\356\0\u010c\0\u0151\0\u0120\0\356\0\u0125\0\356" +
		"\0\u0127\0\356\0\u0128\0\356\0\u0129\0\356\0\u012b\0\356\0\u013e\0\356\0\u0141\0" +
		"\356\0\u0143\0\356\0\u0145\0\356\0\u0147\0\356\0\u0148\0\356\0\u017c\0\356\0\u0180" +
		"\0\356\0\u0183\0\356\0\u0185\0\356\0\u01aa\0\356\0\u0126\0\u0169\0\u0165\0\u0169" +
		"\0\u0166\0\u0199\0\u019b\0\u0169\0\u019d\0\u0169\0\u0126\0\u016a\0\u0165\0\u0198" +
		"\0\u019b\0\u01b1\0\u019d\0\u01b2\0\165\0\216\0\211\0\216\0\257\0\216\0\327\0\216" +
		"\0\343\0\216\0\345\0\216\0\373\0\216\0\375\0\216\0\376\0\216\0\u0101\0\216\0\u0120" +
		"\0\216\0\u0125\0\216\0\u0129\0\216\0\u012b\0\216\0\u013e\0\216\0\u0141\0\216\0\u0143" +
		"\0\216\0\u0145\0\216\0\u0147\0\216\0\u0148\0\216\0\u017c\0\216\0\u0180\0\216\0\u0183" +
		"\0\216\0\u0185\0\216\0\u01aa\0\216\0\165\0\217\0\211\0\217\0\257\0\357\0\327\0\357" +
		"\0\343\0\357\0\345\0\357\0\373\0\357\0\375\0\357\0\376\0\357\0\u0101\0\357\0\u0120" +
		"\0\357\0\u0125\0\357\0\u0129\0\357\0\u012b\0\357\0\u013e\0\357\0\u0141\0\357\0\u0143" +
		"\0\357\0\u0145\0\357\0\u0147\0\357\0\u0148\0\357\0\u017c\0\357\0\u0180\0\357\0\u0183" +
		"\0\357\0\u0185\0\357\0\u01aa\0\357\0\165\0\220\0\211\0\220\0\216\0\275\0\257\0\220" +
		"\0\327\0\220\0\343\0\220\0\345\0\220\0\373\0\220\0\375\0\220\0\376\0\220\0\u0101" +
		"\0\220\0\u0120\0\220\0\u0125\0\220\0\u0129\0\220\0\u012b\0\220\0\u013e\0\220\0\u0141" +
		"\0\220\0\u0143\0\220\0\u0145\0\220\0\u0147\0\220\0\u0148\0\220\0\u017c\0\220\0\u0180" +
		"\0\220\0\u0183\0\220\0\u0185\0\220\0\u01aa\0\220\0\261\0\363\0\210\0\272\0\276\0" +
		"\u0104\0\261\0\364\0\u0138\0\u0175\0\65\0\104\0\101\0\124\0\102\0\125\0\127\0\104" +
		"\0\261\0\365\0\326\0\u011c\0\u011b\0\u0159\0\u0138\0\365\0\u015c\0\u011c\0\u015e" +
		"\0\u011c\0\65\0\105\0\65\0\106\0\52\0\66\0\334\0\66\0\u0167\0\66\0\65\0\107\0\127" +
		"\0\145\0\206\0\254\0\253\0\321\0\126\0\144\0\u0115\0\u0156\0\u0154\0\u018f\0\u0174" +
		"\0\u019f\0\326\0\u011d\0\u015c\0\u011d\0\u015e\0\u011d\0\326\0\u011e\0\u015c\0\u0193" +
		"\0\u015e\0\u0194\0\1\0\u01bc\0\6\0\62\0\77\0\123\0\136\0\164\0\323\0\u0117\0\6\0" +
		"\63\0\6\0\64\0\60\0\73\0\131\0\146\0\153\0\201\0\205\0\235\0\304\0\u0108\0\u0109" +
		"\0\u014e\0\u0108\0\u014d\0\u014e\0\u018d\0\u014d\0\u018c\0\u018d\0\u01ad\0\262\0" +
		"\370\0\371\0\u013c\0\257\0\360\0\327\0\360\0\343\0\u012c\0\345\0\u012f\0\373\0\360" +
		"\0\375\0\360\0\376\0\360\0\u0101\0\360\0\u0120\0\u012f\0\u0125\0\360\0\u0129\0\360" +
		"\0\u012b\0\u016e\0\u013e\0\360\0\u0141\0\360\0\u0143\0\360\0\u0145\0\360\0\u0147" +
		"\0\360\0\u0148\0\360\0\u017c\0\360\0\u0180\0\360\0\u0183\0\360\0\u0185\0\360\0\u01aa" +
		"\0\360\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(257,
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0" +
		"\1\0\1\0\2\0\0\0\5\0\4\0\2\0\0\0\7\0\4\0\3\0\3\0\4\0\4\0\3\0\3\0\1\0\2\0\1\0\1\0" +
		"\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\4\0\3\0\3\0\3\0\1\0\10\0\4\0\7\0\3\0\3\0" +
		"\1\0\1\0\1\0\1\0\5\0\3\0\1\0\4\0\4\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\10\0\7\0\7\0" +
		"\6\0\7\0\6\0\6\0\5\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0\4\0\2\0\3\0\2\0\1\0\1\0\1\0\2\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0\4\0\5\0\5\0\6\0\3\0\1\0\3\0\1\0" +
		"\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\4\0\3\0\3\0\2\0\3\0\2\0\2\0\1\0\1\0\3\0\3\0" +
		"\3\0\4\0\2\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\2\0\1\0\2\0\1\0\2\0\1\0\3\0" +
		"\3\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0\1\0\3\0\2\0\1\0" +
		"\3\0\3\0\2\0\1\0\1\0\4\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0\1\0\1\0\0\0\3\0" +
		"\3\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0\1\0\3\0\1\0\3\0" +
		"\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(257,
		"\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120" +
		"\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0" +
		"\120\0\120\0\120\0\120\0\120\0\121\0\121\0\121\0\121\0\122\0\123\0\123\0\124\0\125" +
		"\0\126\0\127\0\127\0\130\0\130\0\131\0\131\0\132\0\132\0\133\0\134\0\135\0\136\0" +
		"\136\0\137\0\137\0\140\0\140\0\141\0\142\0\143\0\143\0\143\0\144\0\144\0\144\0\144" +
		"\0\144\0\145\0\146\0\147\0\147\0\150\0\150\0\151\0\151\0\151\0\151\0\152\0\153\0" +
		"\153\0\153\0\153\0\154\0\155\0\155\0\156\0\156\0\157\0\160\0\161\0\161\0\161\0\162" +
		"\0\162\0\162\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0" +
		"\163\0\163\0\163\0\163\0\163\0\164\0\164\0\164\0\164\0\164\0\164\0\165\0\166\0\166" +
		"\0\166\0\167\0\167\0\167\0\170\0\170\0\170\0\170\0\171\0\171\0\171\0\171\0\171\0" +
		"\171\0\172\0\172\0\173\0\173\0\174\0\174\0\175\0\175\0\176\0\176\0\177\0\177\0\200" +
		"\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\202\0\203\0\203\0\204\0" +
		"\204\0\205\0\205\0\205\0\206\0\206\0\206\0\206\0\207\0\207\0\210\0\211\0\211\0\212" +
		"\0\213\0\213\0\214\0\214\0\214\0\215\0\215\0\216\0\216\0\216\0\217\0\217\0\217\0" +
		"\217\0\217\0\217\0\217\0\217\0\220\0\221\0\221\0\221\0\221\0\222\0\222\0\222\0\223" +
		"\0\223\0\224\0\225\0\225\0\225\0\226\0\226\0\227\0\230\0\230\0\230\0\231\0\232\0" +
		"\232\0\233\0\233\0\234\0\235\0\235\0\235\0\235\0\236\0\236\0\237\0\237\0\240\0\240" +
		"\0\240\0\240\0\241\0\241\0\241\0\242\0\242\0\242\0\242\0\243\0\243\0\244\0\244\0" +
		"\245\0\245\0\246\0\246\0\247\0\247\0\250\0\250\0\251\0\251\0\252\0\252\0\253\0\253" +
		"\0");

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
		"'soft'",
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
		"parsing_algorithm",
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
		"parsing_algorithmopt",
		"rawTypeopt",
		"iconopt",
		"lexeme_attrsopt",
		"commandopt",
		"implements_clauseopt",
		"rhsSuffixopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int identifier = 80;
		int literal = 81;
		int pattern = 82;
		int qualified_id = 83;
		int name = 84;
		int command = 85;
		int syntax_problem = 86;
		int import__optlist = 87;
		int input1 = 88;
		int option_optlist = 89;
		int header = 90;
		int lexer_section = 91;
		int parser_section = 92;
		int parsing_algorithm = 93;
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
		int rhsParts = 133;
		int rhsPart = 134;
		int lookahead_predicate_list_And_separated = 135;
		int rhsLookahead = 136;
		int lookahead_predicate = 137;
		int rhsStateMarker = 138;
		int rhsAnnotated = 139;
		int rhsAssignment = 140;
		int rhsOptional = 141;
		int rhsCast = 142;
		int rhsPrimary = 143;
		int rhsSet = 144;
		int setPrimary = 145;
		int setExpression = 146;
		int annotation_list = 147;
		int annotations = 148;
		int annotation = 149;
		int nonterm_param_list_Comma_separated = 150;
		int nonterm_params = 151;
		int nonterm_param = 152;
		int param_ref = 153;
		int argument_list_Comma_separated = 154;
		int argument_list_Comma_separated_opt = 155;
		int symref_args = 156;
		int argument = 157;
		int param_type = 158;
		int param_value = 159;
		int predicate_primary = 160;
		int predicate_expression = 161;
		int expression = 162;
		int expression_list_Comma_separated = 163;
		int expression_list_Comma_separated_opt = 164;
		int parsing_algorithmopt = 165;
		int rawTypeopt = 166;
		int iconopt = 167;
		int lexeme_attrsopt = 168;
		int commandopt = 169;
		int implements_clauseopt = 170;
		int rhsSuffixopt = 171;
	}

	public interface Rules {
		int nonterm_type_nontermTypeAST = 113;  // nonterm_type : 'returns' symref_noargs
		int nonterm_type_nontermTypeHint = 114;  // nonterm_type : 'inline' 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint2 = 115;  // nonterm_type : 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint3 = 116;  // nonterm_type : 'interface'
		int nonterm_type_nontermTypeHint4 = 117;  // nonterm_type : 'void'
		int directive_directivePrio = 130;  // directive : '%' assoc references ';'
		int directive_directiveInput = 131;  // directive : '%' 'input' inputref_list_Comma_separated ';'
		int directive_directiveInterface = 132;  // directive : '%' 'interface' identifier_list_Comma_separated ';'
		int directive_directiveAssert = 133;  // directive : '%' 'assert' 'empty' rhsSet ';'
		int directive_directiveAssert2 = 134;  // directive : '%' 'assert' 'nonempty' rhsSet ';'
		int directive_directiveSet = 135;  // directive : '%' 'generate' identifier '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 182;  // rhsOptional : rhsCast '?'
		int rhsCast_rhsAsLiteral = 185;  // rhsCast : rhsPrimary 'as' literal
		int rhsPrimary_rhsSymbol = 186;  // rhsPrimary : symref
		int rhsPrimary_rhsNested = 187;  // rhsPrimary : '(' rules ')'
		int rhsPrimary_rhsList = 188;  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
		int rhsPrimary_rhsList2 = 189;  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
		int rhsPrimary_rhsQuantifier = 190;  // rhsPrimary : rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 191;  // rhsPrimary : rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 192;  // rhsPrimary : '$' '(' rules ')'
		int setPrimary_setSymbol = 195;  // setPrimary : identifier symref
		int setPrimary_setSymbol2 = 196;  // setPrimary : symref
		int setPrimary_setCompound = 197;  // setPrimary : '(' setExpression ')'
		int setPrimary_setComplement = 198;  // setPrimary : '~' setPrimary
		int setExpression_setBinary = 200;  // setExpression : setExpression '|' setExpression
		int setExpression_setBinary2 = 201;  // setExpression : setExpression '&' setExpression
		int nonterm_param_inlineParameter = 212;  // nonterm_param : identifier identifier '=' param_value
		int nonterm_param_inlineParameter2 = 213;  // nonterm_param : identifier identifier
		int predicate_primary_boolPredicate = 228;  // predicate_primary : '!' param_ref
		int predicate_primary_boolPredicate2 = 229;  // predicate_primary : param_ref
		int predicate_primary_comparePredicate = 230;  // predicate_primary : param_ref '==' literal
		int predicate_primary_comparePredicate2 = 231;  // predicate_primary : param_ref '!=' literal
		int predicate_expression_predicateBinary = 233;  // predicate_expression : predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 234;  // predicate_expression : predicate_expression '||' predicate_expression
		int expression_array = 237;  // expression : '[' expression_list_Comma_separated_opt ']'
	}

	// set(follow error)
	private static int[] afterErr = {
		6, 7, 8, 13, 14, 15, 18, 19, 20, 21, 22, 23, 24, 34, 35, 36,
		37, 42, 43, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
		58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73,
		74, 75, 76
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
			case 22:  // identifier : 'soft'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 23:  // identifier : 'class'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 24:  // identifier : 'interface'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 25:  // identifier : 'void'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 26:  // identifier : 'space'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 27:  // identifier : 'layout'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 28:  // identifier : 'language'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 29:  // identifier : 'lalr'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // identifier : 'lexer'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // identifier : 'parser'
				tmLeft.value = new TmaIdentifier(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // literal : scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 33:  // literal : icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 34:  // literal : 'true'
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 35:  // literal : 'false'
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 36:  // pattern : regexp
				tmLeft.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // qualified_id : identifier
				{ tmLeft.value = ((TmaIdentifier)tmStack[tmHead].value).getText(); }
				break;
			case 38:  // qualified_id : qualified_id '.' identifier
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((TmaIdentifier)tmStack[tmHead].value).getText(); }
				break;
			case 39:  // name : qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 40:  // command : code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 41:  // syntax_problem : error
				tmLeft.value = new TmaSyntaxProblem(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 42:  // import__optlist : import__optlist import_
				((List<TmaImport>)tmLeft.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 43:  // import__optlist :
				tmLeft.value = new ArrayList();
				break;
			case 44:  // input : header import__optlist option_optlist lexer_section parser_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexer */,
						((List<ITmaGrammarPart>)tmStack[tmHead].value) /* parser */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 45:  // input : header import__optlist option_optlist lexer_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 3].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 2].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 1].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead].value) /* lexer */,
						null /* parser */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 46:  // option_optlist : option_optlist option
				((List<TmaOption>)tmLeft.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 47:  // option_optlist :
				tmLeft.value = new ArrayList();
				break;
			case 48:  // header : 'language' name '(' name ')' parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 5].value) /* name */,
						((TmaName)tmStack[tmHead - 3].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // header : 'language' name parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 2].value) /* name */,
						null /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // lexer_section : '::' 'lexer' lexer_parts
				tmLeft.value = ((List<ITmaLexerPart>)tmStack[tmHead].value);
				break;
			case 51:  // parser_section : '::' 'parser' grammar_parts
				tmLeft.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value);
				break;
			case 52:  // parsing_algorithm : 'lalr' '(' icon ')'
				tmLeft.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
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
			case 80:  // lexeme_attribute : 'soft'
				tmLeft.value = TmaLexemeAttribute.SOFT;
				break;
			case 81:  // lexeme_attribute : 'class'
				tmLeft.value = TmaLexemeAttribute.CLASS;
				break;
			case 82:  // lexeme_attribute : 'space'
				tmLeft.value = TmaLexemeAttribute.SPACE;
				break;
			case 83:  // lexeme_attribute : 'layout'
				tmLeft.value = TmaLexemeAttribute.LAYOUT;
				break;
			case 84:  // brackets_directive : '%' 'brackets' symref_noargs symref_noargs ';'
				tmLeft.value = new TmaBracketsDirective(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 85:  // lexer_state_list_Comma_separated : lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 86:  // lexer_state_list_Comma_separated : lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 87:  // states_clause : '%' 's' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						false /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 88:  // states_clause : '%' 'x' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						true /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // stateref : identifier
				tmLeft.value = new TmaStateref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // lexer_state : identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // grammar_parts : grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 92:  // grammar_parts : grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 93:  // grammar_parts : grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 97:  // nonterm : annotations identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 7].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 98:  // nonterm : annotations identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // nonterm : annotations identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // nonterm : annotations identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // nonterm : annotations identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // nonterm : annotations identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // nonterm : annotations identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // nonterm : annotations identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // nonterm : identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // nonterm : identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // nonterm : identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // nonterm : identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // nonterm : identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // nonterm : identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // nonterm : identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // nonterm : identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // nonterm_type : 'returns' symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // nonterm_type : 'inline' 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // nonterm_type : 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // nonterm_type : 'interface'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.INTERFACE /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // nonterm_type : 'void'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.VOID /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 119:  // implements_clause : 'implements' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 120:  // assoc : 'left'
				tmLeft.value = TmaAssoc.LEFT;
				break;
			case 121:  // assoc : 'right'
				tmLeft.value = TmaAssoc.RIGHT;
				break;
			case 122:  // assoc : 'nonassoc'
				tmLeft.value = TmaAssoc.NONASSOC;
				break;
			case 123:  // param_modifier : 'explicit'
				tmLeft.value = TmaParamModifier.EXPLICIT;
				break;
			case 124:  // param_modifier : 'global'
				tmLeft.value = TmaParamModifier.GLOBAL;
				break;
			case 125:  // param_modifier : 'lookahead'
				tmLeft.value = TmaParamModifier.LOOKAHEAD;
				break;
			case 126:  // template_param : '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // template_param : '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // template_param : '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // template_param : '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // directive : '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 131:  // directive : '%' 'input' inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // directive : '%' 'interface' identifier_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInterface(
						((List<TmaIdentifier>)tmStack[tmHead - 1].value) /* ids */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // directive : '%' 'assert' 'empty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.EMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // directive : '%' 'assert' 'nonempty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.NONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // directive : '%' 'generate' identifier '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // identifier_list_Comma_separated : identifier_list_Comma_separated ',' identifier
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 137:  // identifier_list_Comma_separated : identifier
				tmLeft.value = new ArrayList();
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 138:  // inputref_list_Comma_separated : inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 139:  // inputref_list_Comma_separated : inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 140:  // inputref : symref_noargs 'no-eoi'
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // inputref : symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // references : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 143:  // references : references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 144:  // references_cs : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 145:  // references_cs : references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 146:  // rule0_list_Or_separated : rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 147:  // rule0_list_Or_separated : rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 149:  // rule0 : predicate rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rule0 : predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rule0 : predicate rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rule0 : predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rule0 : rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rule0 : rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rule0 : rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rule0 : rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // rule0 : syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						null /* suffix */,
						null /* action */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // predicate : '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 159:  // rhsSuffix : '%' 'prec' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.PREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // rhsSuffix : '%' 'shift' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.SHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // reportClause : '->' identifier '/' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((TmaIdentifier)tmStack[tmHead].value) /* kind */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // reportClause : '->' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead].value) /* action */,
						null /* kind */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // rhsParts : rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 164:  // rhsParts : rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 165:  // rhsParts : rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 170:  // lookahead_predicate_list_And_separated : lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 171:  // lookahead_predicate_list_And_separated : lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 172:  // rhsLookahead : '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 173:  // lookahead_predicate : '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 174:  // lookahead_predicate : symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // rhsStateMarker : '.' identifier
				tmLeft.value = new TmaRhsStateMarker(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // rhsAnnotated : annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 179:  // rhsAssignment : identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 180:  // rhsAssignment : identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 182:  // rhsOptional : rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 184:  // rhsCast : rhsPrimary 'as' symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // rhsCast : rhsPrimary 'as' literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // rhsPrimary : symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // rhsPrimary : '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 188:  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 189:  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 190:  // rhsPrimary : rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // rhsPrimary : rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // rhsPrimary : '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 194:  // rhsSet : 'set' '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // setPrimary : identifier symref
				tmLeft.value = new TmaSetSymbol(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // setPrimary : symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 197:  // setPrimary : '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 198:  // setPrimary : '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 200:  // setExpression : setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 201:  // setExpression : setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // annotation_list : annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 203:  // annotation_list : annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 204:  // annotations : annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 205:  // annotation : '@' identifier '=' expression
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 206:  // annotation : '@' identifier
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 207:  // annotation : '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 208:  // nonterm_param_list_Comma_separated : nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 209:  // nonterm_param_list_Comma_separated : nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 210:  // nonterm_params : '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 212:  // nonterm_param : identifier identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 213:  // nonterm_param : identifier identifier
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 214:  // param_ref : identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 215:  // argument_list_Comma_separated : argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 216:  // argument_list_Comma_separated : argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 219:  // symref_args : '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 220:  // argument : param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 221:  // argument : '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 222:  // argument : '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 223:  // argument : param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 224:  // param_type : 'flag'
				tmLeft.value = TmaParamType.FLAG;
				break;
			case 225:  // param_type : 'param'
				tmLeft.value = TmaParamType.PARAM;
				break;
			case 228:  // predicate_primary : '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 229:  // predicate_primary : param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 230:  // predicate_primary : param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 231:  // predicate_primary : param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 233:  // predicate_expression : predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 234:  // predicate_expression : predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 237:  // expression : '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 239:  // expression_list_Comma_separated : expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 240:  // expression_list_Comma_separated : expression
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
		return (TmaInput1) parse(lexer, 0, 445);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 446);
	}
}
