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
	private static final int[] tmAction = TMLexer.unpack_int(444,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\40\0\41\0\uffb5\uffff\51\0\0\0\43" +
		"\0\42\0\13\0\1\0\27\0\14\0\17\0\22\0\12\0\16\0\2\0\6\0\30\0\35\0\34\0\33\0\7\0\36" +
		"\0\20\0\23\0\11\0\15\0\21\0\37\0\3\0\5\0\10\0\24\0\4\0\26\0\32\0\31\0\25\0\uff65" +
		"\uffff\355\0\360\0\356\0\45\0\ufef3\uffff\uffff\uffff\ufee9\uffff\362\0\ufea1\uffff" +
		"\uffff\uffff\ufe9b\uffff\70\0\uffff\uffff\61\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\52\0\uffff\uffff\357\0\uffff\uffff\uffff\uffff\330\0\ufe53\uffff\ufe4b\uffff\uffff" +
		"\uffff\332\0\46\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\67" +
		"\0\ufe45\uffff\56\0\361\0\337\0\340\0\uffff\uffff\uffff\uffff\335\0\uffff\uffff\65" +
		"\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\54\0\72\0\344\0\345\0\336\0\331" +
		"\0\60\0\64\0\uffff\uffff\uffff\uffff\ufe3f\uffff\ufe37\uffff\74\0\77\0\103\0\uffff" +
		"\uffff\100\0\102\0\101\0\66\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\130\0\uffff\uffff\111\0\uffff\uffff\73\0\365\0\uffff\uffff\76\0\75\0\uffff" +
		"\uffff\ufdeb\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufde5\uffff\132\0\135\0\136" +
		"\0\137\0\ufd9b\uffff\uffff\uffff\315\0\uffff\uffff\131\0\uffff\uffff\125\0\uffff" +
		"\uffff\106\0\uffff\uffff\107\0\44\0\104\0\ufd51\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\172\0\342\0\uffff\uffff\173\0\uffff\uffff\uffff\uffff\167\0\174\0\171\0\343" +
		"\0\170\0\uffff\uffff\uffff\uffff\uffff\uffff\ufd01\uffff\321\0\ufcb5\uffff\uffff" +
		"\uffff\uffff\uffff\ufc59\uffff\uffff\uffff\163\0\uffff\uffff\164\0\165\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\134\0\133\0\314\0\uffff\uffff\uffff\uffff\126\0\uffff\uffff" +
		"\127\0\110\0\ufc51\uffff\105\0\ufbfd\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufbad" +
		"\uffff\uffff\uffff\212\0\210\0\uffff\uffff\215\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufba5\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\50\0\ufb49\uffff\251\0\234\0\274\0\ufadd\uffff\uffff\uffff\222\0\ufad5" +
		"\uffff\377\0\ufa7b\uffff\245\0\253\0\252\0\250\0\262\0\264\0\ufa1f\uffff\uf9bf\uffff" +
		"\303\0\uffff\uffff\uf959\uffff\uf94f\uffff\uf941\uffff\uffff\uffff\323\0\325\0\uffff" +
		"\uffff\375\0\162\0\uf8fb\uffff\160\0\uf8f3\uffff\uffff\uffff\uf897\uffff\uf83b\uffff" +
		"\uffff\uffff\uffff\uffff\uf7df\uffff\uffff\uffff\uffff\uffff\uffff\uffff\123\0\124" +
		"\0\367\0\uf783\uffff\uf731\uffff\uffff\uffff\uffff\uffff\uffff\uffff\213\0\202\0" +
		"\uffff\uffff\203\0\uffff\uffff\201\0\216\0\uffff\uffff\uffff\uffff\200\0\317\0\uffff" +
		"\uffff\uffff\uffff\261\0\uffff\uffff\uf6dd\uffff\352\0\uffff\uffff\uffff\uffff\uf6d1" +
		"\uffff\uffff\uffff\260\0\uffff\uffff\255\0\uf675\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uf619\uffff\157\0\uf5bb\uffff\uf55f\uffff\247\0\246\0\uf555\uffff\270\0\300" +
		"\0\301\0\uffff\uffff\263\0\232\0\uffff\uffff\uffff\uffff\242\0\uf54b\uffff\uffff" +
		"\uffff\324\0\217\0\uf543\uffff\161\0\uffff\uffff\uf53b\uffff\uffff\uffff\uffff\uffff" +
		"\uf4df\uffff\uffff\uffff\uf483\uffff\uffff\uffff\uf427\uffff\uffff\uffff\uf3cb\uffff" +
		"\uf36f\uffff\uffff\uffff\uffff\uffff\uffff\uffff\371\0\uf313\uffff\uf2c3\uffff\204" +
		"\0\205\0\uffff\uffff\211\0\207\0\uffff\uffff\176\0\uffff\uffff\236\0\237\0\346\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\235\0\uffff\uffff\275\0\uffff\uffff\257\0\256" +
		"\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf271\uffff\306\0\311\0\uffff" +
		"\uffff\265\0\266\0\221\0\uf227\uffff\226\0\230\0\273\0\272\0\244\0\uf21d\uffff\uffff" +
		"\uffff\322\0\uffff\uffff\155\0\uffff\uffff\156\0\153\0\uffff\uffff\uf211\uffff\uffff" +
		"\uffff\147\0\uffff\uffff\uf1b5\uffff\uffff\uffff\uffff\uffff\uf159\uffff\uffff\uffff" +
		"\uf0fd\uffff\120\0\122\0\117\0\121\0\uffff\uffff\373\0\114\0\uf0a1\uffff\206\0\uffff" +
		"\uffff\177\0\350\0\351\0\uf051\uffff\uf049\uffff\uffff\uffff\254\0\302\0\uffff\uffff" +
		"\310\0\305\0\uffff\uffff\304\0\uffff\uffff\224\0\240\0\326\0\220\0\154\0\151\0\uffff" +
		"\uffff\152\0\145\0\uffff\uffff\146\0\143\0\uffff\uffff\uf041\uffff\uffff\uffff\116" +
		"\0\112\0\175\0\uffff\uffff\307\0\uefe5\uffff\uefdd\uffff\150\0\144\0\141\0\uffff" +
		"\uffff\142\0\277\0\276\0\140\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4136,
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
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\22\0\364\0\uffff\uffff\ufffe\uffff\30" +
		"\0\uffff\uffff\0\0\71\0\6\0\71\0\7\0\71\0\10\0\71\0\15\0\71\0\16\0\71\0\17\0\71\0" +
		"\22\0\71\0\23\0\71\0\24\0\71\0\25\0\71\0\26\0\71\0\32\0\71\0\33\0\71\0\35\0\71\0" +
		"\40\0\71\0\42\0\71\0\43\0\71\0\44\0\71\0\45\0\71\0\46\0\71\0\52\0\71\0\53\0\71\0" +
		"\55\0\71\0\56\0\71\0\57\0\71\0\60\0\71\0\61\0\71\0\62\0\71\0\63\0\71\0\64\0\71\0" +
		"\65\0\71\0\66\0\71\0\67\0\71\0\70\0\71\0\71\0\71\0\72\0\71\0\73\0\71\0\74\0\71\0" +
		"\75\0\71\0\76\0\71\0\77\0\71\0\100\0\71\0\101\0\71\0\102\0\71\0\103\0\71\0\104\0" +
		"\71\0\105\0\71\0\106\0\71\0\107\0\71\0\110\0\71\0\111\0\71\0\112\0\71\0\113\0\71" +
		"\0\114\0\71\0\uffff\uffff\ufffe\uffff\16\0\uffff\uffff\15\0\47\0\23\0\47\0\26\0\47" +
		"\0\uffff\uffff\ufffe\uffff\51\0\uffff\uffff\7\0\57\0\44\0\57\0\45\0\57\0\55\0\57" +
		"\0\56\0\57\0\57\0\57\0\60\0\57\0\61\0\57\0\62\0\57\0\63\0\57\0\64\0\57\0\65\0\57" +
		"\0\66\0\57\0\67\0\57\0\70\0\57\0\71\0\57\0\72\0\57\0\73\0\57\0\74\0\57\0\75\0\57" +
		"\0\76\0\57\0\77\0\57\0\100\0\57\0\101\0\57\0\102\0\57\0\103\0\57\0\104\0\57\0\105" +
		"\0\57\0\106\0\57\0\107\0\57\0\110\0\57\0\111\0\57\0\112\0\57\0\113\0\57\0\uffff\uffff" +
		"\ufffe\uffff\17\0\uffff\uffff\22\0\363\0\uffff\uffff\ufffe\uffff\33\0\uffff\uffff" +
		"\37\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\31\0\334\0\uffff" +
		"\uffff\ufffe\uffff\20\0\uffff\uffff\17\0\341\0\31\0\341\0\uffff\uffff\ufffe\uffff" +
		"\17\0\uffff\uffff\31\0\333\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\0\0\55\0\uffff" +
		"\uffff\ufffe\uffff\12\0\uffff\uffff\114\0\uffff\uffff\20\0\366\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff" +
		"\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff" +
		"\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff" +
		"\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff" +
		"\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0" +
		"\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff" +
		"\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\0\0\62\0\7\0\62\0\uffff\uffff\ufffe\uffff\114\0\uffff\uffff" +
		"\20\0\366\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\0\0\63\0\uffff\uffff\ufffe\uffff" +
		"\43\0\uffff\uffff\23\0\316\0\42\0\316\0\45\0\316\0\53\0\316\0\55\0\316\0\56\0\316" +
		"\0\57\0\316\0\60\0\316\0\61\0\316\0\62\0\316\0\63\0\316\0\64\0\316\0\65\0\316\0\66" +
		"\0\316\0\67\0\316\0\70\0\316\0\71\0\316\0\72\0\316\0\73\0\316\0\74\0\316\0\75\0\316" +
		"\0\76\0\316\0\77\0\316\0\100\0\316\0\101\0\316\0\102\0\316\0\103\0\316\0\104\0\316" +
		"\0\105\0\316\0\106\0\316\0\107\0\316\0\110\0\316\0\111\0\316\0\112\0\316\0\113\0" +
		"\316\0\uffff\uffff\ufffe\uffff\116\0\uffff\uffff\0\0\115\0\6\0\115\0\7\0\115\0\27" +
		"\0\115\0\30\0\115\0\44\0\115\0\45\0\115\0\55\0\115\0\56\0\115\0\57\0\115\0\60\0\115" +
		"\0\61\0\115\0\62\0\115\0\63\0\115\0\64\0\115\0\65\0\115\0\66\0\115\0\67\0\115\0\70" +
		"\0\115\0\71\0\115\0\72\0\115\0\73\0\115\0\74\0\115\0\75\0\115\0\76\0\115\0\77\0\115" +
		"\0\100\0\115\0\101\0\115\0\102\0\115\0\103\0\115\0\104\0\115\0\105\0\115\0\106\0" +
		"\115\0\107\0\115\0\110\0\115\0\111\0\115\0\112\0\115\0\113\0\115\0\uffff\uffff\ufffe" +
		"\uffff\12\0\uffff\uffff\23\0\320\0\42\0\320\0\43\0\320\0\45\0\320\0\53\0\320\0\55" +
		"\0\320\0\56\0\320\0\57\0\320\0\60\0\320\0\61\0\320\0\62\0\320\0\63\0\320\0\64\0\320" +
		"\0\65\0\320\0\66\0\320\0\67\0\320\0\70\0\320\0\71\0\320\0\72\0\320\0\73\0\320\0\74" +
		"\0\320\0\75\0\320\0\76\0\320\0\77\0\320\0\100\0\320\0\101\0\320\0\102\0\320\0\103" +
		"\0\320\0\104\0\320\0\105\0\320\0\106\0\320\0\107\0\320\0\110\0\320\0\111\0\320\0" +
		"\112\0\320\0\113\0\320\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
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
		"\0\0\113\0\6\0\113\0\7\0\113\0\27\0\113\0\30\0\113\0\44\0\113\0\45\0\113\0\55\0\113" +
		"\0\56\0\113\0\57\0\113\0\60\0\113\0\61\0\113\0\62\0\113\0\63\0\113\0\64\0\113\0\65" +
		"\0\113\0\66\0\113\0\67\0\113\0\70\0\113\0\71\0\113\0\72\0\113\0\73\0\113\0\74\0\113" +
		"\0\75\0\113\0\76\0\113\0\77\0\113\0\100\0\113\0\101\0\113\0\102\0\113\0\103\0\113" +
		"\0\104\0\113\0\105\0\113\0\106\0\113\0\107\0\113\0\110\0\113\0\111\0\113\0\112\0" +
		"\113\0\113\0\113\0\uffff\uffff\ufffe\uffff\76\0\uffff\uffff\15\0\214\0\17\0\214\0" +
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
		"\0\uffff\uffff\34\0\uffff\uffff\6\0\71\0\10\0\71\0\15\0\71\0\16\0\71\0\23\0\71\0" +
		"\24\0\71\0\25\0\71\0\26\0\71\0\32\0\71\0\33\0\71\0\35\0\71\0\42\0\71\0\43\0\71\0" +
		"\44\0\71\0\45\0\71\0\46\0\71\0\52\0\71\0\53\0\71\0\55\0\71\0\56\0\71\0\57\0\71\0" +
		"\60\0\71\0\61\0\71\0\62\0\71\0\63\0\71\0\64\0\71\0\65\0\71\0\66\0\71\0\67\0\71\0" +
		"\70\0\71\0\71\0\71\0\72\0\71\0\73\0\71\0\74\0\71\0\75\0\71\0\76\0\71\0\77\0\71\0" +
		"\100\0\71\0\101\0\71\0\102\0\71\0\103\0\71\0\104\0\71\0\105\0\71\0\106\0\71\0\107" +
		"\0\71\0\110\0\71\0\111\0\71\0\112\0\71\0\113\0\71\0\114\0\71\0\uffff\uffff\ufffe" +
		"\uffff\10\0\uffff\uffff\15\0\223\0\26\0\223\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
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
		"\26\0\u0100\0\uffff\uffff\ufffe\uffff\35\0\uffff\uffff\6\0\267\0\10\0\267\0\15\0" +
		"\267\0\16\0\267\0\23\0\267\0\24\0\267\0\25\0\267\0\26\0\267\0\42\0\267\0\43\0\267" +
		"\0\44\0\267\0\45\0\267\0\52\0\267\0\53\0\267\0\55\0\267\0\56\0\267\0\57\0\267\0\60" +
		"\0\267\0\61\0\267\0\62\0\267\0\63\0\267\0\64\0\267\0\65\0\267\0\66\0\267\0\67\0\267" +
		"\0\70\0\267\0\71\0\267\0\72\0\267\0\73\0\267\0\74\0\267\0\75\0\267\0\76\0\267\0\77" +
		"\0\267\0\100\0\267\0\101\0\267\0\102\0\267\0\103\0\267\0\104\0\267\0\105\0\267\0" +
		"\106\0\267\0\107\0\267\0\110\0\267\0\111\0\267\0\112\0\267\0\113\0\267\0\114\0\267" +
		"\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\33\0\uffff\uffff\46\0\uffff\uffff\6\0" +
		"\271\0\10\0\271\0\15\0\271\0\16\0\271\0\23\0\271\0\24\0\271\0\25\0\271\0\26\0\271" +
		"\0\35\0\271\0\42\0\271\0\43\0\271\0\44\0\271\0\45\0\271\0\52\0\271\0\53\0\271\0\55" +
		"\0\271\0\56\0\271\0\57\0\271\0\60\0\271\0\61\0\271\0\62\0\271\0\63\0\271\0\64\0\271" +
		"\0\65\0\271\0\66\0\271\0\67\0\271\0\70\0\271\0\71\0\271\0\72\0\271\0\73\0\271\0\74" +
		"\0\271\0\75\0\271\0\76\0\271\0\77\0\271\0\100\0\271\0\101\0\271\0\102\0\271\0\103" +
		"\0\271\0\104\0\271\0\105\0\271\0\106\0\271\0\107\0\271\0\110\0\271\0\111\0\271\0" +
		"\112\0\271\0\113\0\271\0\114\0\271\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10" +
		"\0\233\0\15\0\233\0\26\0\233\0\uffff\uffff\ufffe\uffff\46\0\uffff\uffff\117\0\uffff" +
		"\uffff\10\0\243\0\15\0\243\0\20\0\243\0\26\0\243\0\uffff\uffff\ufffe\uffff\45\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\17\0\330\0\31\0\330\0\uffff\uffff\ufffe" +
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
		"\0\uffff\uffff\11\0\347\0\22\0\347\0\41\0\347\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
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
		"\uffff\10\0\227\0\15\0\227\0\26\0\227\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff" +
		"\10\0\231\0\15\0\231\0\26\0\231\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\17\0\327" +
		"\0\31\0\327\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\20\0\166\0\25\0\166\0\uffff" +
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
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\10\0\71\0\26\0\71\0\40\0\71\0\uffff\uffff" +
		"\ufffe\uffff\25\0\uffff\uffff\10\0\225\0\15\0\225\0\26\0\225\0\uffff\uffff\ufffe" +
		"\uffff\46\0\uffff\uffff\10\0\241\0\15\0\241\0\20\0\241\0\26\0\241\0\uffff\uffff\ufffe" +
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
		"\111\0\374\0\112\0\374\0\113\0\374\0\uffff\uffff\ufffe\uffff\11\0\354\0\41\0\uffff" +
		"\uffff\22\0\354\0\uffff\uffff\ufffe\uffff\11\0\353\0\41\0\353\0\22\0\353\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff" +
		"\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\u0100\0" +
		"\15\0\u0100\0\25\0\u0100\0\uffff\uffff\ufffe\uffff\10\0\312\0\40\0\uffff\uffff\26" +
		"\0\312\0\uffff\uffff\ufffe\uffff\10\0\313\0\40\0\313\0\26\0\313\0\uffff\uffff\ufffe" +
		"\uffff");

	private static final int[] tmGoto = TMLexer.unpack_int(172,
		"\0\0\4\0\40\0\74\0\74\0\74\0\74\0\166\0\172\0\204\0\212\0\232\0\234\0\236\0\340\0" +
		"\u0110\0\u0122\0\u0148\0\u0178\0\u017c\0\u01c4\0\u01f2\0\u020a\0\u021a\0\u021c\0" +
		"\u022e\0\u0236\0\u023c\0\u0244\0\u0246\0\u0248\0\u0252\0\u0260\0\u026a\0\u0270\0" +
		"\u02a4\0\u02d8\0\u0318\0\u03da\0\u03e0\0\u03f8\0\u03fc\0\u03fe\0\u0400\0\u043a\0" +
		"\u0452\0\u0516\0\u05da\0\u06a8\0\u076c\0\u0830\0\u08f6\0\u09ba\0\u0a7e\0\u0b48\0" +
		"\u0c0c\0\u0cd8\0\u0d9a\0\u0e5e\0\u0f22\0\u0fe6\0\u10aa\0\u116e\0\u1232\0\u12f6\0" +
		"\u13ba\0\u1480\0\u1544\0\u1608\0\u16d2\0\u1796\0\u185a\0\u191e\0\u19e2\0\u1aa6\0" +
		"\u1b70\0\u1c34\0\u1c72\0\u1c74\0\u1c7a\0\u1c7c\0\u1d3e\0\u1d56\0\u1d5c\0\u1d60\0" +
		"\u1d64\0\u1d96\0\u1dd6\0\u1dd8\0\u1dda\0\u1ddc\0\u1dde\0\u1de0\0\u1de2\0\u1de4\0" +
		"\u1de6\0\u1e32\0\u1e5a\0\u1e66\0\u1e6a\0\u1e72\0\u1e7a\0\u1e82\0\u1e8a\0\u1e8c\0" +
		"\u1e94\0\u1e98\0\u1e9a\0\u1ea2\0\u1ea6\0\u1eae\0\u1eb2\0\u1eb8\0\u1eba\0\u1ebe\0" +
		"\u1ec2\0\u1eca\0\u1ece\0\u1ed0\0\u1ed2\0\u1ed6\0\u1eda\0\u1edc\0\u1ede\0\u1ee2\0" +
		"\u1ee6\0\u1ee8\0\u1f0c\0\u1f30\0\u1f56\0\u1f7c\0\u1faa\0\u1fc2\0\u1fc6\0\u1fee\0" +
		"\u201c\0\u201e\0\u204c\0\u2050\0\u207e\0\u20ac\0\u20dc\0\u2110\0\u2144\0\u2178\0" +
		"\u21b2\0\u21bc\0\u21c4\0\u21f6\0\u2228\0\u225c\0\u225e\0\u2262\0\u2266\0\u227a\0" +
		"\u227c\0\u227e\0\u2284\0\u2288\0\u228c\0\u2294\0\u229a\0\u22a0\0\u22aa\0\u22ac\0" +
		"\u22ae\0\u22b2\0\u22b6\0\u22ba\0\u22be\0\u22c2\0\u22f0\0");

	private static final int[] tmFromTo = TMLexer.unpack_int(8944,
		"\u01b8\0\u01ba\0\u01b9\0\u01bb\0\1\0\4\0\6\0\4\0\72\0\110\0\75\0\4\0\111\0\127\0" +
		"\122\0\4\0\131\0\4\0\314\0\4\0\u010e\0\4\0\u012c\0\4\0\u014f\0\4\0\u0155\0\4\0\u0156" +
		"\0\4\0\u0170\0\4\0\1\0\5\0\6\0\5\0\75\0\5\0\122\0\5\0\131\0\5\0\275\0\u0100\0\314" +
		"\0\5\0\u0102\0\u0100\0\u010e\0\5\0\u012c\0\5\0\u014f\0\5\0\u0155\0\5\0\u0156\0\5" +
		"\0\u0170\0\5\0\130\0\143\0\146\0\143\0\157\0\177\0\175\0\143\0\202\0\177\0\225\0" +
		"\143\0\250\0\315\0\320\0\315\0\334\0\315\0\336\0\315\0\364\0\315\0\366\0\315\0\367" +
		"\0\315\0\372\0\315\0\u0119\0\315\0\u011e\0\315\0\u0122\0\315\0\u0124\0\315\0\u0139" +
		"\0\315\0\u013c\0\315\0\u013e\0\315\0\u0140\0\315\0\u0142\0\315\0\u0143\0\315\0\u0178" +
		"\0\315\0\u017c\0\315\0\u017f\0\315\0\u0181\0\315\0\u01a7\0\315\0\73\0\112\0\115\0" +
		"\132\0\331\0\u0122\0\u0165\0\u0197\0\u0194\0\u0197\0\u01ae\0\u0197\0\u01af\0\u0197" +
		"\0\u0117\0\u0157\0\u018f\0\u0157\0\u0190\0\u0157\0\113\0\131\0\145\0\167\0\246\0" +
		"\314\0\302\0\u0105\0\313\0\u010e\0\325\0\u0120\0\u010d\0\u014f\0\u0132\0\u0170\0" +
		"\u0115\0\u0155\0\u0115\0\u0156\0\60\0\70\0\110\0\126\0\125\0\141\0\127\0\142\0\214" +
		"\0\271\0\216\0\273\0\270\0\376\0\304\0\u0107\0\307\0\u0109\0\311\0\u010b\0\313\0" +
		"\u010f\0\332\0\u0123\0\u0103\0\u014a\0\u0104\0\u014b\0\u010d\0\u0150\0\u0138\0\u0173" +
		"\0\u013a\0\u0175\0\u013b\0\u0176\0\u013f\0\u017a\0\u014c\0\u018a\0\u0151\0\u018c" +
		"\0\u0174\0\u019e\0\u0177\0\u019f\0\u0179\0\u01a1\0\u017b\0\u01a2\0\u017d\0\u01a4" +
		"\0\u017e\0\u01a5\0\u018b\0\u01ab\0\u01a0\0\u01b0\0\u01a3\0\u01b1\0\u01a6\0\u01b2" +
		"\0\u01a8\0\u01b4\0\u01b3\0\u01b7\0\57\0\67\0\250\0\316\0\320\0\316\0\334\0\316\0" +
		"\336\0\316\0\364\0\316\0\366\0\316\0\367\0\316\0\372\0\316\0\u0119\0\316\0\u011e" +
		"\0\316\0\u0122\0\316\0\u0124\0\316\0\u0139\0\316\0\u013c\0\316\0\u013e\0\316\0\u0140" +
		"\0\316\0\u0142\0\316\0\u0143\0\316\0\u0178\0\316\0\u017c\0\316\0\u017f\0\316\0\u0181" +
		"\0\316\0\u01a7\0\316\0\63\0\75\0\103\0\123\0\165\0\220\0\214\0\272\0\216\0\272\0" +
		"\304\0\u0108\0\307\0\u010a\0\354\0\u0133\0\u0136\0\u0172\0\102\0\122\0\172\0\224" +
		"\0\201\0\250\0\226\0\277\0\261\0\364\0\262\0\366\0\263\0\367\0\267\0\372\0\365\0" +
		"\u0139\0\370\0\u013c\0\371\0\u013e\0\373\0\u0140\0\374\0\u0142\0\375\0\u0143\0\u013d" +
		"\0\u0178\0\u0141\0\u017c\0\u0144\0\u017f\0\u0145\0\u0181\0\u0180\0\u01a7\0\1\0\6" +
		"\0\6\0\6\0\75\0\6\0\131\0\6\0\250\0\317\0\314\0\6\0\320\0\317\0\364\0\317\0\366\0" +
		"\317\0\367\0\317\0\372\0\317\0\u011e\0\317\0\u0122\0\317\0\u0139\0\317\0\u013c\0" +
		"\317\0\u013e\0\317\0\u0140\0\317\0\u0142\0\317\0\u0143\0\317\0\u0178\0\317\0\u017c" +
		"\0\317\0\u017f\0\317\0\u0181\0\317\0\u01a7\0\317\0\64\0\76\0\u0117\0\u0158\0\60\0" +
		"\71\0\250\0\320\0\320\0\320\0\322\0\u011e\0\323\0\u011f\0\334\0\320\0\336\0\320\0" +
		"\350\0\320\0\364\0\320\0\366\0\320\0\367\0\320\0\372\0\320\0\u0101\0\u0146\0\u0119" +
		"\0\320\0\u011e\0\320\0\u011f\0\u0160\0\u0120\0\320\0\u0121\0\320\0\u0122\0\320\0" +
		"\u0124\0\320\0\u0139\0\320\0\u013c\0\320\0\u013e\0\320\0\u0140\0\320\0\u0142\0\320" +
		"\0\u0143\0\320\0\u0149\0\u0146\0\u0160\0\u0160\0\u0161\0\u0160\0\u0178\0\320\0\u017c" +
		"\0\320\0\u017f\0\320\0\u0181\0\320\0\u0197\0\u0160\0\u0199\0\u0160\0\u01a7\0\320" +
		"\0\250\0\321\0\320\0\321\0\334\0\321\0\336\0\321\0\364\0\321\0\366\0\321\0\367\0" +
		"\321\0\372\0\321\0\u0119\0\321\0\u011e\0\321\0\u0122\0\321\0\u0124\0\321\0\u0139" +
		"\0\321\0\u013c\0\321\0\u013e\0\321\0\u0140\0\321\0\u0142\0\321\0\u0143\0\321\0\u0178" +
		"\0\321\0\u017c\0\321\0\u017f\0\321\0\u0181\0\321\0\u01a7\0\321\0\201\0\251\0\261" +
		"\0\251\0\263\0\251\0\267\0\251\0\351\0\251\0\370\0\251\0\373\0\251\0\375\0\251\0" +
		"\u0125\0\251\0\u0128\0\251\0\u0144\0\251\0\u0169\0\251\0\107\0\125\0\u0118\0\u015a" +
		"\0\u011c\0\u015d\0\u015f\0\u0193\0\u0165\0\u0198\0\u0186\0\u01a9\0\u0191\0\u01ac" +
		"\0\u0194\0\u01ad\0\225\0\276\0\52\0\65\0\130\0\144\0\146\0\144\0\175\0\144\0\201" +
		"\0\252\0\225\0\144\0\267\0\252\0\325\0\65\0\u0162\0\65\0\104\0\124\0\163\0\217\0" +
		"\165\0\221\0\354\0\u0134\0\144\0\163\0\346\0\u012a\0\u01ac\0\u01b5\0\65\0\77\0\123" +
		"\0\77\0\346\0\u012b\0\u01ac\0\u01b6\0\325\0\u0121\0\345\0\u0129\0\317\0\u0114\0\321" +
		"\0\u011a\0\u0157\0\u0114\0\u0159\0\u0114\0\u015e\0\u011a\0\65\0\100\0\123\0\100\0" +
		"\u011f\0\u0161\0\u0160\0\u0161\0\u0161\0\u0161\0\u0197\0\u0161\0\u0199\0\u0161\0" +
		"\u011c\0\u015e\0\u0165\0\u0199\0\u0194\0\u0199\0\u01ae\0\u0199\0\u01af\0\u0199\0" +
		"\u0117\0\u0159\0\u018f\0\u0159\0\u0190\0\u0159\0\250\0\322\0\320\0\322\0\334\0\322" +
		"\0\336\0\322\0\350\0\322\0\364\0\322\0\366\0\322\0\367\0\322\0\372\0\322\0\u0119" +
		"\0\322\0\u011e\0\322\0\u0120\0\322\0\u0121\0\322\0\u0122\0\322\0\u0124\0\322\0\u0139" +
		"\0\322\0\u013c\0\322\0\u013e\0\322\0\u0140\0\322\0\u0142\0\322\0\u0143\0\322\0\u0178" +
		"\0\322\0\u017c\0\322\0\u017f\0\322\0\u0181\0\322\0\u01a7\0\322\0\157\0\200\0\202" +
		"\0\200\0\207\0\200\0\250\0\200\0\320\0\200\0\334\0\200\0\336\0\200\0\364\0\200\0" +
		"\366\0\200\0\367\0\200\0\372\0\200\0\u0119\0\200\0\u011e\0\200\0\u0122\0\200\0\u0124" +
		"\0\200\0\u0139\0\200\0\u013c\0\200\0\u013e\0\200\0\u0140\0\200\0\u0142\0\200\0\u0143" +
		"\0\200\0\u0178\0\200\0\u017c\0\200\0\u017f\0\200\0\u0181\0\200\0\u01a7\0\200\0\1" +
		"\0\7\0\6\0\7\0\73\0\7\0\75\0\7\0\131\0\7\0\146\0\7\0\200\0\7\0\202\0\7\0\225\0\7" +
		"\0\250\0\7\0\314\0\7\0\320\0\7\0\336\0\7\0\364\0\7\0\366\0\7\0\367\0\7\0\372\0\7" +
		"\0\u0119\0\7\0\u011e\0\7\0\u0122\0\7\0\u0124\0\7\0\u0139\0\7\0\u013c\0\7\0\u013e" +
		"\0\7\0\u0140\0\7\0\u0142\0\7\0\u0143\0\7\0\u0178\0\7\0\u017c\0\7\0\u017f\0\7\0\u0181" +
		"\0\7\0\u01a7\0\7\0\1\0\10\0\2\0\10\0\6\0\10\0\65\0\10\0\67\0\10\0\71\0\10\0\72\0" +
		"\10\0\73\0\10\0\75\0\10\0\77\0\10\0\100\0\10\0\122\0\10\0\123\0\10\0\130\0\10\0\131" +
		"\0\10\0\144\0\10\0\146\0\10\0\152\0\10\0\157\0\10\0\160\0\10\0\161\0\10\0\162\0\10" +
		"\0\175\0\10\0\200\0\10\0\202\0\10\0\210\0\10\0\212\0\10\0\220\0\10\0\225\0\10\0\232" +
		"\0\10\0\234\0\10\0\235\0\10\0\243\0\10\0\245\0\10\0\250\0\10\0\251\0\10\0\252\0\10" +
		"\0\256\0\10\0\272\0\10\0\311\0\10\0\312\0\10\0\314\0\10\0\316\0\10\0\317\0\10\0\320" +
		"\0\10\0\321\0\10\0\334\0\10\0\336\0\10\0\350\0\10\0\353\0\10\0\357\0\10\0\364\0\10" +
		"\0\366\0\10\0\367\0\10\0\372\0\10\0\u0108\0\10\0\u010a\0\10\0\u010e\0\10\0\u0111" +
		"\0\10\0\u0112\0\10\0\u0114\0\10\0\u0119\0\10\0\u011a\0\10\0\u011e\0\10\0\u011f\0" +
		"\10\0\u0120\0\10\0\u0121\0\10\0\u0122\0\10\0\u0124\0\10\0\u012c\0\10\0\u012f\0\10" +
		"\0\u0130\0\10\0\u0133\0\10\0\u0139\0\10\0\u013c\0\10\0\u013e\0\10\0\u0140\0\10\0" +
		"\u0142\0\10\0\u0143\0\10\0\u014f\0\10\0\u0157\0\10\0\u0159\0\10\0\u015b\0\10\0\u015e" +
		"\0\10\0\u0160\0\10\0\u0161\0\10\0\u0162\0\10\0\u0170\0\10\0\u0172\0\10\0\u0178\0" +
		"\10\0\u017c\0\10\0\u017f\0\10\0\u0181\0\10\0\u0191\0\10\0\u0197\0\10\0\u0199\0\10" +
		"\0\u01a7\0\10\0\346\0\u012c\0\352\0\u012f\0\u016f\0\u012f\0\1\0\11\0\6\0\11\0\75" +
		"\0\11\0\122\0\11\0\131\0\11\0\314\0\11\0\u010e\0\11\0\u012c\0\11\0\u014f\0\11\0\u0155" +
		"\0\11\0\u0156\0\11\0\u0170\0\11\0\253\0\357\0\362\0\357\0\61\0\72\0\u0119\0\u015b" +
		"\0\250\0\323\0\300\0\323\0\301\0\323\0\320\0\323\0\334\0\323\0\336\0\323\0\350\0" +
		"\323\0\364\0\323\0\366\0\323\0\367\0\323\0\372\0\323\0\u0105\0\323\0\u0119\0\323" +
		"\0\u011e\0\323\0\u0120\0\323\0\u0121\0\323\0\u0122\0\323\0\u0124\0\323\0\u0139\0" +
		"\323\0\u013c\0\323\0\u013e\0\323\0\u0140\0\323\0\u0142\0\323\0\u0143\0\323\0\u0178" +
		"\0\323\0\u017c\0\323\0\u017f\0\323\0\u0181\0\323\0\u01a7\0\323\0\1\0\12\0\6\0\12" +
		"\0\75\0\12\0\122\0\12\0\131\0\12\0\314\0\12\0\u010e\0\12\0\u012c\0\12\0\u014f\0\12" +
		"\0\u0155\0\12\0\u0156\0\12\0\u0170\0\12\0\1\0\13\0\2\0\13\0\6\0\13\0\65\0\13\0\67" +
		"\0\13\0\71\0\13\0\72\0\13\0\73\0\13\0\75\0\13\0\77\0\13\0\100\0\13\0\122\0\13\0\123" +
		"\0\13\0\130\0\13\0\131\0\13\0\144\0\13\0\146\0\13\0\152\0\13\0\157\0\13\0\160\0\13" +
		"\0\161\0\13\0\162\0\13\0\175\0\13\0\177\0\227\0\200\0\13\0\202\0\13\0\210\0\13\0" +
		"\212\0\13\0\220\0\13\0\225\0\13\0\232\0\13\0\234\0\13\0\235\0\13\0\243\0\13\0\245" +
		"\0\13\0\250\0\13\0\251\0\13\0\252\0\13\0\256\0\13\0\272\0\13\0\311\0\13\0\312\0\13" +
		"\0\314\0\13\0\316\0\13\0\317\0\13\0\320\0\13\0\321\0\13\0\334\0\13\0\336\0\13\0\350" +
		"\0\13\0\353\0\13\0\357\0\13\0\364\0\13\0\366\0\13\0\367\0\13\0\372\0\13\0\u0108\0" +
		"\13\0\u010a\0\13\0\u010e\0\13\0\u0111\0\13\0\u0112\0\13\0\u0114\0\13\0\u0119\0\13" +
		"\0\u011a\0\13\0\u011e\0\13\0\u011f\0\13\0\u0120\0\13\0\u0121\0\13\0\u0122\0\13\0" +
		"\u0124\0\13\0\u012c\0\13\0\u012f\0\13\0\u0130\0\13\0\u0133\0\13\0\u0139\0\13\0\u013c" +
		"\0\13\0\u013e\0\13\0\u0140\0\13\0\u0142\0\13\0\u0143\0\13\0\u014f\0\13\0\u0157\0" +
		"\13\0\u0159\0\13\0\u015b\0\13\0\u015e\0\13\0\u0160\0\13\0\u0161\0\13\0\u0162\0\13" +
		"\0\u0170\0\13\0\u0172\0\13\0\u0178\0\13\0\u017c\0\13\0\u017f\0\13\0\u0181\0\13\0" +
		"\u0191\0\13\0\u0197\0\13\0\u0199\0\13\0\u01a7\0\13\0\1\0\14\0\2\0\14\0\6\0\14\0\65" +
		"\0\14\0\67\0\14\0\71\0\14\0\72\0\14\0\73\0\14\0\75\0\14\0\77\0\14\0\100\0\14\0\122" +
		"\0\14\0\123\0\14\0\130\0\14\0\131\0\14\0\143\0\160\0\144\0\14\0\146\0\14\0\152\0" +
		"\14\0\157\0\14\0\160\0\14\0\161\0\14\0\162\0\14\0\175\0\14\0\200\0\14\0\202\0\14" +
		"\0\210\0\14\0\212\0\14\0\220\0\14\0\225\0\14\0\232\0\14\0\234\0\14\0\235\0\14\0\243" +
		"\0\14\0\245\0\14\0\250\0\14\0\251\0\14\0\252\0\14\0\256\0\14\0\272\0\14\0\311\0\14" +
		"\0\312\0\14\0\314\0\14\0\316\0\14\0\317\0\14\0\320\0\14\0\321\0\14\0\334\0\14\0\336" +
		"\0\14\0\350\0\14\0\353\0\14\0\357\0\14\0\364\0\14\0\366\0\14\0\367\0\14\0\372\0\14" +
		"\0\u0108\0\14\0\u010a\0\14\0\u010e\0\14\0\u0111\0\14\0\u0112\0\14\0\u0114\0\14\0" +
		"\u0119\0\14\0\u011a\0\14\0\u011e\0\14\0\u011f\0\14\0\u0120\0\14\0\u0121\0\14\0\u0122" +
		"\0\14\0\u0124\0\14\0\u012c\0\14\0\u012f\0\14\0\u0130\0\14\0\u0133\0\14\0\u0139\0" +
		"\14\0\u013c\0\14\0\u013e\0\14\0\u0140\0\14\0\u0142\0\14\0\u0143\0\14\0\u014f\0\14" +
		"\0\u0157\0\14\0\u0159\0\14\0\u015b\0\14\0\u015e\0\14\0\u0160\0\14\0\u0161\0\14\0" +
		"\u0162\0\14\0\u0170\0\14\0\u0172\0\14\0\u0178\0\14\0\u017c\0\14\0\u017f\0\14\0\u0181" +
		"\0\14\0\u0191\0\14\0\u0197\0\14\0\u0199\0\14\0\u01a7\0\14\0\1\0\15\0\2\0\15\0\6\0" +
		"\15\0\65\0\15\0\67\0\15\0\71\0\15\0\72\0\15\0\73\0\15\0\75\0\15\0\77\0\15\0\100\0" +
		"\15\0\122\0\15\0\123\0\15\0\130\0\15\0\131\0\15\0\144\0\15\0\146\0\15\0\152\0\15" +
		"\0\157\0\15\0\160\0\15\0\161\0\15\0\162\0\15\0\175\0\15\0\200\0\15\0\201\0\253\0" +
		"\202\0\15\0\210\0\15\0\212\0\15\0\220\0\15\0\225\0\15\0\232\0\15\0\234\0\15\0\235" +
		"\0\15\0\243\0\15\0\245\0\15\0\250\0\15\0\251\0\15\0\252\0\15\0\254\0\362\0\256\0" +
		"\15\0\263\0\253\0\267\0\253\0\272\0\15\0\311\0\15\0\312\0\15\0\314\0\15\0\316\0\15" +
		"\0\317\0\15\0\320\0\15\0\321\0\15\0\334\0\15\0\336\0\15\0\350\0\15\0\353\0\15\0\357" +
		"\0\15\0\364\0\15\0\366\0\15\0\367\0\15\0\372\0\15\0\375\0\253\0\u0108\0\15\0\u010a" +
		"\0\15\0\u010e\0\15\0\u0111\0\15\0\u0112\0\15\0\u0114\0\15\0\u0119\0\15\0\u011a\0" +
		"\15\0\u011e\0\15\0\u011f\0\15\0\u0120\0\15\0\u0121\0\15\0\u0122\0\15\0\u0124\0\15" +
		"\0\u012c\0\15\0\u012f\0\15\0\u0130\0\15\0\u0133\0\15\0\u0139\0\15\0\u013c\0\15\0" +
		"\u013e\0\15\0\u0140\0\15\0\u0142\0\15\0\u0143\0\15\0\u0146\0\u0182\0\u014f\0\15\0" +
		"\u0157\0\15\0\u0159\0\15\0\u015b\0\15\0\u015e\0\15\0\u0160\0\15\0\u0161\0\15\0\u0162" +
		"\0\15\0\u0170\0\15\0\u0172\0\15\0\u0178\0\15\0\u017c\0\15\0\u017f\0\15\0\u0181\0" +
		"\15\0\u0191\0\15\0\u0197\0\15\0\u0199\0\15\0\u01a7\0\15\0\1\0\16\0\2\0\16\0\6\0\16" +
		"\0\65\0\16\0\67\0\16\0\71\0\16\0\72\0\16\0\73\0\16\0\75\0\16\0\77\0\16\0\100\0\16" +
		"\0\122\0\16\0\123\0\16\0\130\0\16\0\131\0\16\0\144\0\16\0\146\0\16\0\152\0\16\0\157" +
		"\0\16\0\160\0\16\0\161\0\16\0\162\0\16\0\175\0\16\0\200\0\16\0\202\0\16\0\210\0\16" +
		"\0\212\0\16\0\220\0\16\0\225\0\16\0\227\0\300\0\232\0\16\0\234\0\16\0\235\0\16\0" +
		"\243\0\16\0\245\0\16\0\250\0\16\0\251\0\16\0\252\0\16\0\256\0\16\0\272\0\16\0\311" +
		"\0\16\0\312\0\16\0\314\0\16\0\316\0\16\0\317\0\16\0\320\0\16\0\321\0\16\0\334\0\16" +
		"\0\336\0\16\0\350\0\16\0\353\0\16\0\357\0\16\0\364\0\16\0\366\0\16\0\367\0\16\0\372" +
		"\0\16\0\u0108\0\16\0\u010a\0\16\0\u010e\0\16\0\u0111\0\16\0\u0112\0\16\0\u0114\0" +
		"\16\0\u0119\0\16\0\u011a\0\16\0\u011e\0\16\0\u011f\0\16\0\u0120\0\16\0\u0121\0\16" +
		"\0\u0122\0\16\0\u0124\0\16\0\u012c\0\16\0\u012f\0\16\0\u0130\0\16\0\u0133\0\16\0" +
		"\u0139\0\16\0\u013c\0\16\0\u013e\0\16\0\u0140\0\16\0\u0142\0\16\0\u0143\0\16\0\u014f" +
		"\0\16\0\u0157\0\16\0\u0159\0\16\0\u015b\0\16\0\u015e\0\16\0\u0160\0\16\0\u0161\0" +
		"\16\0\u0162\0\16\0\u0170\0\16\0\u0172\0\16\0\u0178\0\16\0\u017c\0\16\0\u017f\0\16" +
		"\0\u0181\0\16\0\u0191\0\16\0\u0197\0\16\0\u0199\0\16\0\u01a7\0\16\0\1\0\17\0\2\0" +
		"\17\0\6\0\17\0\65\0\17\0\67\0\17\0\71\0\17\0\72\0\17\0\73\0\17\0\75\0\17\0\77\0\17" +
		"\0\100\0\17\0\122\0\17\0\123\0\17\0\130\0\17\0\131\0\17\0\144\0\17\0\146\0\17\0\152" +
		"\0\17\0\157\0\17\0\160\0\17\0\161\0\17\0\162\0\17\0\175\0\17\0\177\0\230\0\200\0" +
		"\17\0\202\0\17\0\210\0\17\0\212\0\17\0\220\0\17\0\225\0\17\0\232\0\17\0\234\0\17" +
		"\0\235\0\17\0\243\0\17\0\245\0\17\0\250\0\17\0\251\0\17\0\252\0\17\0\256\0\17\0\272" +
		"\0\17\0\311\0\17\0\312\0\17\0\314\0\17\0\316\0\17\0\317\0\17\0\320\0\17\0\321\0\17" +
		"\0\334\0\17\0\336\0\17\0\350\0\17\0\353\0\17\0\357\0\17\0\364\0\17\0\366\0\17\0\367" +
		"\0\17\0\372\0\17\0\u0108\0\17\0\u010a\0\17\0\u010e\0\17\0\u0111\0\17\0\u0112\0\17" +
		"\0\u0114\0\17\0\u0119\0\17\0\u011a\0\17\0\u011e\0\17\0\u011f\0\17\0\u0120\0\17\0" +
		"\u0121\0\17\0\u0122\0\17\0\u0124\0\17\0\u012c\0\17\0\u012f\0\17\0\u0130\0\17\0\u0133" +
		"\0\17\0\u0139\0\17\0\u013c\0\17\0\u013e\0\17\0\u0140\0\17\0\u0142\0\17\0\u0143\0" +
		"\17\0\u014f\0\17\0\u0157\0\17\0\u0159\0\17\0\u015b\0\17\0\u015e\0\17\0\u0160\0\17" +
		"\0\u0161\0\17\0\u0162\0\17\0\u0170\0\17\0\u0172\0\17\0\u0178\0\17\0\u017c\0\17\0" +
		"\u017f\0\17\0\u0181\0\17\0\u0191\0\17\0\u0197\0\17\0\u0199\0\17\0\u01a7\0\17\0\1" +
		"\0\20\0\2\0\20\0\6\0\20\0\65\0\20\0\67\0\20\0\71\0\20\0\72\0\20\0\73\0\20\0\75\0" +
		"\20\0\77\0\20\0\100\0\20\0\122\0\20\0\123\0\20\0\130\0\20\0\131\0\20\0\144\0\20\0" +
		"\146\0\20\0\152\0\20\0\157\0\20\0\160\0\20\0\161\0\20\0\162\0\20\0\175\0\20\0\177" +
		"\0\231\0\200\0\20\0\202\0\20\0\210\0\20\0\212\0\20\0\220\0\20\0\225\0\20\0\232\0" +
		"\20\0\234\0\20\0\235\0\20\0\243\0\20\0\244\0\231\0\245\0\20\0\250\0\20\0\251\0\20" +
		"\0\252\0\20\0\256\0\20\0\272\0\20\0\311\0\20\0\312\0\20\0\314\0\20\0\316\0\20\0\317" +
		"\0\20\0\320\0\20\0\321\0\20\0\334\0\20\0\336\0\20\0\350\0\20\0\353\0\20\0\357\0\20" +
		"\0\364\0\20\0\366\0\20\0\367\0\20\0\372\0\20\0\u0108\0\20\0\u010a\0\20\0\u010e\0" +
		"\20\0\u0111\0\20\0\u0112\0\20\0\u0114\0\20\0\u0119\0\20\0\u011a\0\20\0\u011e\0\20" +
		"\0\u011f\0\20\0\u0120\0\20\0\u0121\0\20\0\u0122\0\20\0\u0124\0\20\0\u012c\0\20\0" +
		"\u012f\0\20\0\u0130\0\20\0\u0133\0\20\0\u0139\0\20\0\u013c\0\20\0\u013e\0\20\0\u0140" +
		"\0\20\0\u0142\0\20\0\u0143\0\20\0\u014f\0\20\0\u0157\0\20\0\u0159\0\20\0\u015b\0" +
		"\20\0\u015e\0\20\0\u0160\0\20\0\u0161\0\20\0\u0162\0\20\0\u0170\0\20\0\u0172\0\20" +
		"\0\u0178\0\20\0\u017c\0\20\0\u017f\0\20\0\u0181\0\20\0\u0191\0\20\0\u0197\0\20\0" +
		"\u0199\0\20\0\u01a7\0\20\0\1\0\21\0\2\0\21\0\6\0\21\0\65\0\21\0\67\0\21\0\71\0\21" +
		"\0\72\0\21\0\73\0\21\0\75\0\21\0\77\0\21\0\100\0\21\0\122\0\21\0\123\0\21\0\130\0" +
		"\21\0\131\0\21\0\144\0\21\0\146\0\21\0\152\0\21\0\157\0\21\0\160\0\21\0\161\0\21" +
		"\0\162\0\21\0\175\0\21\0\177\0\232\0\200\0\21\0\202\0\21\0\210\0\21\0\212\0\21\0" +
		"\220\0\21\0\225\0\21\0\232\0\21\0\234\0\21\0\235\0\21\0\243\0\21\0\245\0\21\0\250" +
		"\0\21\0\251\0\21\0\252\0\21\0\256\0\21\0\272\0\21\0\311\0\21\0\312\0\21\0\314\0\21" +
		"\0\316\0\21\0\317\0\21\0\320\0\21\0\321\0\21\0\334\0\21\0\336\0\21\0\350\0\21\0\353" +
		"\0\21\0\357\0\21\0\364\0\21\0\366\0\21\0\367\0\21\0\372\0\21\0\u0108\0\21\0\u010a" +
		"\0\21\0\u010e\0\21\0\u0111\0\21\0\u0112\0\21\0\u0114\0\21\0\u0119\0\21\0\u011a\0" +
		"\21\0\u011e\0\21\0\u011f\0\21\0\u0120\0\21\0\u0121\0\21\0\u0122\0\21\0\u0124\0\21" +
		"\0\u012c\0\21\0\u012f\0\21\0\u0130\0\21\0\u0133\0\21\0\u0139\0\21\0\u013c\0\21\0" +
		"\u013e\0\21\0\u0140\0\21\0\u0142\0\21\0\u0143\0\21\0\u014f\0\21\0\u0157\0\21\0\u0159" +
		"\0\21\0\u015b\0\21\0\u015e\0\21\0\u0160\0\21\0\u0161\0\21\0\u0162\0\21\0\u0170\0" +
		"\21\0\u0172\0\21\0\u0178\0\21\0\u017c\0\21\0\u017f\0\21\0\u0181\0\21\0\u0191\0\21" +
		"\0\u0197\0\21\0\u0199\0\21\0\u01a7\0\21\0\1\0\22\0\2\0\22\0\6\0\22\0\65\0\22\0\67" +
		"\0\22\0\71\0\22\0\72\0\22\0\73\0\22\0\75\0\22\0\77\0\22\0\100\0\22\0\122\0\22\0\123" +
		"\0\22\0\130\0\22\0\131\0\22\0\144\0\22\0\146\0\22\0\152\0\22\0\157\0\22\0\160\0\22" +
		"\0\161\0\22\0\162\0\22\0\175\0\22\0\177\0\233\0\200\0\22\0\202\0\22\0\210\0\22\0" +
		"\212\0\22\0\220\0\22\0\225\0\22\0\232\0\22\0\234\0\22\0\235\0\22\0\243\0\22\0\245" +
		"\0\22\0\250\0\22\0\251\0\22\0\252\0\22\0\256\0\22\0\272\0\22\0\311\0\22\0\312\0\22" +
		"\0\314\0\22\0\316\0\22\0\317\0\22\0\320\0\22\0\321\0\22\0\334\0\22\0\336\0\22\0\350" +
		"\0\22\0\353\0\22\0\357\0\22\0\364\0\22\0\366\0\22\0\367\0\22\0\372\0\22\0\u0108\0" +
		"\22\0\u010a\0\22\0\u010e\0\22\0\u0111\0\22\0\u0112\0\22\0\u0114\0\22\0\u0119\0\22" +
		"\0\u011a\0\22\0\u011e\0\22\0\u011f\0\22\0\u0120\0\22\0\u0121\0\22\0\u0122\0\22\0" +
		"\u0124\0\22\0\u012c\0\22\0\u012f\0\22\0\u0130\0\22\0\u0133\0\22\0\u0139\0\22\0\u013c" +
		"\0\22\0\u013e\0\22\0\u0140\0\22\0\u0142\0\22\0\u0143\0\22\0\u014f\0\22\0\u0157\0" +
		"\22\0\u0159\0\22\0\u015b\0\22\0\u015e\0\22\0\u0160\0\22\0\u0161\0\22\0\u0162\0\22" +
		"\0\u0170\0\22\0\u0172\0\22\0\u0178\0\22\0\u017c\0\22\0\u017f\0\22\0\u0181\0\22\0" +
		"\u0191\0\22\0\u0197\0\22\0\u0199\0\22\0\u01a7\0\22\0\1\0\23\0\2\0\23\0\6\0\23\0\65" +
		"\0\23\0\67\0\23\0\71\0\23\0\72\0\23\0\73\0\23\0\75\0\23\0\77\0\23\0\100\0\23\0\122" +
		"\0\23\0\123\0\23\0\130\0\23\0\131\0\23\0\144\0\23\0\146\0\23\0\152\0\23\0\157\0\23" +
		"\0\160\0\23\0\161\0\23\0\162\0\23\0\175\0\23\0\200\0\23\0\201\0\254\0\202\0\23\0" +
		"\210\0\23\0\212\0\23\0\220\0\23\0\225\0\23\0\232\0\23\0\234\0\23\0\235\0\23\0\243" +
		"\0\23\0\245\0\23\0\250\0\23\0\251\0\23\0\252\0\23\0\256\0\23\0\263\0\254\0\267\0" +
		"\254\0\272\0\23\0\311\0\23\0\312\0\23\0\314\0\23\0\316\0\23\0\317\0\23\0\320\0\23" +
		"\0\321\0\23\0\334\0\23\0\336\0\23\0\350\0\23\0\353\0\23\0\357\0\23\0\364\0\23\0\366" +
		"\0\23\0\367\0\23\0\372\0\23\0\375\0\254\0\u0108\0\23\0\u010a\0\23\0\u010e\0\23\0" +
		"\u0111\0\23\0\u0112\0\23\0\u0114\0\23\0\u0119\0\23\0\u011a\0\23\0\u011e\0\23\0\u011f" +
		"\0\23\0\u0120\0\23\0\u0121\0\23\0\u0122\0\23\0\u0124\0\23\0\u012c\0\23\0\u012f\0" +
		"\23\0\u0130\0\23\0\u0133\0\23\0\u0139\0\23\0\u013c\0\23\0\u013e\0\23\0\u0140\0\23" +
		"\0\u0142\0\23\0\u0143\0\23\0\u014f\0\23\0\u0157\0\23\0\u0159\0\23\0\u015b\0\23\0" +
		"\u015e\0\23\0\u0160\0\23\0\u0161\0\23\0\u0162\0\23\0\u0170\0\23\0\u0172\0\23\0\u0178" +
		"\0\23\0\u017c\0\23\0\u017f\0\23\0\u0181\0\23\0\u0191\0\23\0\u0197\0\23\0\u0199\0" +
		"\23\0\u01a7\0\23\0\1\0\24\0\2\0\24\0\6\0\24\0\65\0\24\0\67\0\24\0\71\0\24\0\72\0" +
		"\24\0\73\0\24\0\75\0\24\0\77\0\24\0\100\0\24\0\122\0\24\0\123\0\24\0\130\0\24\0\131" +
		"\0\24\0\144\0\24\0\146\0\24\0\152\0\24\0\157\0\24\0\160\0\24\0\161\0\24\0\162\0\24" +
		"\0\175\0\24\0\177\0\234\0\200\0\24\0\202\0\24\0\210\0\24\0\212\0\24\0\220\0\24\0" +
		"\225\0\24\0\232\0\24\0\234\0\24\0\235\0\24\0\243\0\24\0\245\0\24\0\250\0\24\0\251" +
		"\0\24\0\252\0\24\0\256\0\24\0\272\0\24\0\311\0\24\0\312\0\24\0\314\0\24\0\316\0\24" +
		"\0\317\0\24\0\320\0\24\0\321\0\24\0\334\0\24\0\336\0\24\0\350\0\24\0\353\0\24\0\357" +
		"\0\24\0\364\0\24\0\366\0\24\0\367\0\24\0\372\0\24\0\u0108\0\24\0\u010a\0\24\0\u010e" +
		"\0\24\0\u0111\0\24\0\u0112\0\24\0\u0114\0\24\0\u0119\0\24\0\u011a\0\24\0\u011e\0" +
		"\24\0\u011f\0\24\0\u0120\0\24\0\u0121\0\24\0\u0122\0\24\0\u0124\0\24\0\u012c\0\24" +
		"\0\u012f\0\24\0\u0130\0\24\0\u0133\0\24\0\u0139\0\24\0\u013c\0\24\0\u013e\0\24\0" +
		"\u0140\0\24\0\u0142\0\24\0\u0143\0\24\0\u014f\0\24\0\u0157\0\24\0\u0159\0\24\0\u015b" +
		"\0\24\0\u015e\0\24\0\u0160\0\24\0\u0161\0\24\0\u0162\0\24\0\u0170\0\24\0\u0172\0" +
		"\24\0\u0178\0\24\0\u017c\0\24\0\u017f\0\24\0\u0181\0\24\0\u0191\0\24\0\u0197\0\24" +
		"\0\u0199\0\24\0\u01a7\0\24\0\1\0\25\0\2\0\25\0\6\0\25\0\65\0\25\0\67\0\25\0\71\0" +
		"\25\0\72\0\25\0\73\0\25\0\75\0\25\0\77\0\25\0\100\0\25\0\122\0\25\0\123\0\25\0\130" +
		"\0\25\0\131\0\25\0\144\0\25\0\146\0\25\0\152\0\25\0\157\0\25\0\160\0\25\0\161\0\25" +
		"\0\162\0\25\0\175\0\25\0\177\0\235\0\200\0\25\0\201\0\255\0\202\0\25\0\210\0\25\0" +
		"\212\0\25\0\220\0\25\0\225\0\25\0\232\0\25\0\234\0\25\0\235\0\25\0\243\0\25\0\245" +
		"\0\25\0\250\0\25\0\251\0\25\0\252\0\25\0\256\0\25\0\263\0\255\0\267\0\255\0\272\0" +
		"\25\0\311\0\25\0\312\0\25\0\314\0\25\0\316\0\25\0\317\0\25\0\320\0\25\0\321\0\25" +
		"\0\334\0\25\0\336\0\25\0\350\0\25\0\353\0\25\0\357\0\25\0\364\0\25\0\366\0\25\0\367" +
		"\0\25\0\372\0\25\0\375\0\255\0\u0108\0\25\0\u010a\0\25\0\u010e\0\25\0\u0111\0\25" +
		"\0\u0112\0\25\0\u0114\0\25\0\u0119\0\25\0\u011a\0\25\0\u011e\0\25\0\u011f\0\25\0" +
		"\u0120\0\25\0\u0121\0\25\0\u0122\0\25\0\u0124\0\25\0\u012c\0\25\0\u012f\0\25\0\u0130" +
		"\0\25\0\u0133\0\25\0\u0139\0\25\0\u013c\0\25\0\u013e\0\25\0\u0140\0\25\0\u0142\0" +
		"\25\0\u0143\0\25\0\u014f\0\25\0\u0157\0\25\0\u0159\0\25\0\u015b\0\25\0\u015e\0\25" +
		"\0\u0160\0\25\0\u0161\0\25\0\u0162\0\25\0\u0170\0\25\0\u0172\0\25\0\u0178\0\25\0" +
		"\u017c\0\25\0\u017f\0\25\0\u0181\0\25\0\u0191\0\25\0\u0197\0\25\0\u0199\0\25\0\u01a7" +
		"\0\25\0\1\0\26\0\2\0\26\0\6\0\26\0\65\0\26\0\67\0\26\0\71\0\26\0\72\0\26\0\73\0\26" +
		"\0\75\0\26\0\77\0\26\0\100\0\26\0\122\0\26\0\123\0\26\0\130\0\26\0\131\0\26\0\144" +
		"\0\26\0\146\0\26\0\152\0\26\0\157\0\26\0\160\0\26\0\161\0\26\0\162\0\26\0\175\0\26" +
		"\0\200\0\26\0\202\0\26\0\210\0\26\0\212\0\26\0\220\0\26\0\225\0\26\0\232\0\26\0\234" +
		"\0\26\0\235\0\26\0\243\0\26\0\245\0\26\0\250\0\26\0\251\0\26\0\252\0\26\0\256\0\26" +
		"\0\272\0\26\0\311\0\26\0\312\0\26\0\314\0\26\0\316\0\26\0\317\0\26\0\320\0\26\0\321" +
		"\0\26\0\334\0\26\0\336\0\26\0\350\0\26\0\353\0\26\0\357\0\26\0\364\0\26\0\366\0\26" +
		"\0\367\0\26\0\372\0\26\0\u0108\0\26\0\u010a\0\26\0\u010e\0\26\0\u0111\0\26\0\u0112" +
		"\0\26\0\u0114\0\26\0\u0119\0\26\0\u011a\0\26\0\u011e\0\26\0\u011f\0\26\0\u0120\0" +
		"\26\0\u0121\0\26\0\u0122\0\26\0\u0124\0\26\0\u012c\0\26\0\u012f\0\26\0\u0130\0\26" +
		"\0\u0133\0\26\0\u0139\0\26\0\u013c\0\26\0\u013e\0\26\0\u0140\0\26\0\u0142\0\26\0" +
		"\u0143\0\26\0\u014f\0\26\0\u0157\0\26\0\u0159\0\26\0\u015b\0\26\0\u015e\0\26\0\u0160" +
		"\0\26\0\u0161\0\26\0\u0162\0\26\0\u0170\0\26\0\u0172\0\26\0\u0178\0\26\0\u017c\0" +
		"\26\0\u017f\0\26\0\u0181\0\26\0\u0191\0\26\0\u0197\0\26\0\u0199\0\26\0\u01a7\0\26" +
		"\0\0\0\2\0\1\0\27\0\2\0\27\0\6\0\27\0\65\0\27\0\67\0\27\0\71\0\27\0\72\0\27\0\73" +
		"\0\27\0\75\0\27\0\77\0\27\0\100\0\27\0\122\0\27\0\123\0\27\0\130\0\27\0\131\0\27" +
		"\0\144\0\27\0\146\0\27\0\152\0\27\0\157\0\27\0\160\0\27\0\161\0\27\0\162\0\27\0\175" +
		"\0\27\0\200\0\27\0\202\0\27\0\210\0\27\0\212\0\27\0\220\0\27\0\225\0\27\0\232\0\27" +
		"\0\234\0\27\0\235\0\27\0\243\0\27\0\245\0\27\0\250\0\27\0\251\0\27\0\252\0\27\0\256" +
		"\0\27\0\272\0\27\0\311\0\27\0\312\0\27\0\314\0\27\0\316\0\27\0\317\0\27\0\320\0\27" +
		"\0\321\0\27\0\334\0\27\0\336\0\27\0\350\0\27\0\353\0\27\0\357\0\27\0\364\0\27\0\366" +
		"\0\27\0\367\0\27\0\372\0\27\0\u0108\0\27\0\u010a\0\27\0\u010e\0\27\0\u0111\0\27\0" +
		"\u0112\0\27\0\u0114\0\27\0\u0119\0\27\0\u011a\0\27\0\u011e\0\27\0\u011f\0\27\0\u0120" +
		"\0\27\0\u0121\0\27\0\u0122\0\27\0\u0124\0\27\0\u012c\0\27\0\u012f\0\27\0\u0130\0" +
		"\27\0\u0133\0\27\0\u0139\0\27\0\u013c\0\27\0\u013e\0\27\0\u0140\0\27\0\u0142\0\27" +
		"\0\u0143\0\27\0\u014f\0\27\0\u0157\0\27\0\u0159\0\27\0\u015b\0\27\0\u015e\0\27\0" +
		"\u0160\0\27\0\u0161\0\27\0\u0162\0\27\0\u0170\0\27\0\u0172\0\27\0\u0178\0\27\0\u017c" +
		"\0\27\0\u017f\0\27\0\u0181\0\27\0\u0191\0\27\0\u0197\0\27\0\u0199\0\27\0\u01a7\0" +
		"\27\0\1\0\30\0\2\0\30\0\6\0\30\0\65\0\30\0\67\0\30\0\71\0\30\0\72\0\30\0\73\0\30" +
		"\0\75\0\30\0\77\0\30\0\100\0\30\0\122\0\30\0\123\0\30\0\130\0\30\0\131\0\30\0\144" +
		"\0\30\0\146\0\30\0\152\0\30\0\157\0\30\0\160\0\30\0\161\0\30\0\162\0\30\0\175\0\30" +
		"\0\200\0\30\0\202\0\30\0\210\0\30\0\212\0\30\0\220\0\30\0\225\0\30\0\232\0\30\0\234" +
		"\0\30\0\235\0\30\0\243\0\30\0\245\0\30\0\250\0\30\0\251\0\30\0\252\0\30\0\256\0\30" +
		"\0\272\0\30\0\311\0\30\0\312\0\30\0\314\0\30\0\316\0\30\0\317\0\30\0\320\0\30\0\321" +
		"\0\30\0\334\0\30\0\336\0\30\0\350\0\30\0\353\0\30\0\357\0\30\0\364\0\30\0\366\0\30" +
		"\0\367\0\30\0\372\0\30\0\u0108\0\30\0\u010a\0\30\0\u010e\0\30\0\u0111\0\30\0\u0112" +
		"\0\30\0\u0114\0\30\0\u0119\0\30\0\u011a\0\30\0\u011e\0\30\0\u011f\0\30\0\u0120\0" +
		"\30\0\u0121\0\30\0\u0122\0\30\0\u0124\0\30\0\u012c\0\30\0\u012f\0\30\0\u0130\0\30" +
		"\0\u0133\0\30\0\u0139\0\30\0\u013c\0\30\0\u013e\0\30\0\u0140\0\30\0\u0142\0\30\0" +
		"\u0143\0\30\0\u0146\0\u0183\0\u014f\0\30\0\u0157\0\30\0\u0159\0\30\0\u015b\0\30\0" +
		"\u015e\0\30\0\u0160\0\30\0\u0161\0\30\0\u0162\0\30\0\u0170\0\30\0\u0172\0\30\0\u0178" +
		"\0\30\0\u017c\0\30\0\u017f\0\30\0\u0181\0\30\0\u0191\0\30\0\u0197\0\30\0\u0199\0" +
		"\30\0\u01a7\0\30\0\1\0\31\0\2\0\31\0\6\0\31\0\65\0\31\0\67\0\31\0\71\0\31\0\72\0" +
		"\31\0\73\0\31\0\75\0\31\0\77\0\31\0\100\0\31\0\122\0\31\0\123\0\31\0\130\0\31\0\131" +
		"\0\31\0\144\0\31\0\146\0\31\0\152\0\31\0\157\0\31\0\160\0\31\0\161\0\31\0\162\0\31" +
		"\0\175\0\31\0\177\0\236\0\200\0\31\0\202\0\31\0\210\0\31\0\212\0\31\0\220\0\31\0" +
		"\225\0\31\0\232\0\31\0\234\0\31\0\235\0\31\0\243\0\31\0\245\0\31\0\250\0\31\0\251" +
		"\0\31\0\252\0\31\0\256\0\31\0\272\0\31\0\311\0\31\0\312\0\31\0\314\0\31\0\316\0\31" +
		"\0\317\0\31\0\320\0\31\0\321\0\31\0\334\0\31\0\336\0\31\0\350\0\31\0\353\0\31\0\357" +
		"\0\31\0\364\0\31\0\366\0\31\0\367\0\31\0\372\0\31\0\u0108\0\31\0\u010a\0\31\0\u010e" +
		"\0\31\0\u0111\0\31\0\u0112\0\31\0\u0114\0\31\0\u0119\0\31\0\u011a\0\31\0\u011e\0" +
		"\31\0\u011f\0\31\0\u0120\0\31\0\u0121\0\31\0\u0122\0\31\0\u0124\0\31\0\u012c\0\31" +
		"\0\u012f\0\31\0\u0130\0\31\0\u0133\0\31\0\u0139\0\31\0\u013c\0\31\0\u013e\0\31\0" +
		"\u0140\0\31\0\u0142\0\31\0\u0143\0\31\0\u014f\0\31\0\u0157\0\31\0\u0159\0\31\0\u015b" +
		"\0\31\0\u015e\0\31\0\u0160\0\31\0\u0161\0\31\0\u0162\0\31\0\u0170\0\31\0\u0172\0" +
		"\31\0\u0178\0\31\0\u017c\0\31\0\u017f\0\31\0\u0181\0\31\0\u0191\0\31\0\u0197\0\31" +
		"\0\u0199\0\31\0\u01a7\0\31\0\1\0\32\0\2\0\32\0\6\0\32\0\65\0\32\0\67\0\32\0\71\0" +
		"\32\0\72\0\32\0\73\0\32\0\75\0\32\0\77\0\32\0\100\0\32\0\112\0\130\0\122\0\32\0\123" +
		"\0\32\0\130\0\32\0\131\0\32\0\144\0\32\0\146\0\32\0\152\0\32\0\157\0\32\0\160\0\32" +
		"\0\161\0\32\0\162\0\32\0\175\0\32\0\200\0\32\0\202\0\32\0\210\0\32\0\212\0\32\0\220" +
		"\0\32\0\225\0\32\0\232\0\32\0\234\0\32\0\235\0\32\0\243\0\32\0\245\0\32\0\250\0\32" +
		"\0\251\0\32\0\252\0\32\0\256\0\32\0\272\0\32\0\311\0\32\0\312\0\32\0\314\0\32\0\316" +
		"\0\32\0\317\0\32\0\320\0\32\0\321\0\32\0\334\0\32\0\336\0\32\0\350\0\32\0\353\0\32" +
		"\0\357\0\32\0\364\0\32\0\366\0\32\0\367\0\32\0\372\0\32\0\u0108\0\32\0\u010a\0\32" +
		"\0\u010e\0\32\0\u0111\0\32\0\u0112\0\32\0\u0114\0\32\0\u0119\0\32\0\u011a\0\32\0" +
		"\u011e\0\32\0\u011f\0\32\0\u0120\0\32\0\u0121\0\32\0\u0122\0\32\0\u0124\0\32\0\u012c" +
		"\0\32\0\u012f\0\32\0\u0130\0\32\0\u0133\0\32\0\u0139\0\32\0\u013c\0\32\0\u013e\0" +
		"\32\0\u0140\0\32\0\u0142\0\32\0\u0143\0\32\0\u014f\0\32\0\u0157\0\32\0\u0159\0\32" +
		"\0\u015b\0\32\0\u015e\0\32\0\u0160\0\32\0\u0161\0\32\0\u0162\0\32\0\u0170\0\32\0" +
		"\u0172\0\32\0\u0178\0\32\0\u017c\0\32\0\u017f\0\32\0\u0181\0\32\0\u0191\0\32\0\u0197" +
		"\0\32\0\u0199\0\32\0\u01a7\0\32\0\1\0\33\0\2\0\33\0\6\0\33\0\65\0\33\0\67\0\33\0" +
		"\71\0\33\0\72\0\33\0\73\0\33\0\75\0\33\0\77\0\33\0\100\0\33\0\122\0\33\0\123\0\33" +
		"\0\130\0\33\0\131\0\33\0\144\0\33\0\146\0\33\0\152\0\33\0\157\0\33\0\160\0\33\0\161" +
		"\0\33\0\162\0\33\0\175\0\33\0\177\0\237\0\200\0\33\0\202\0\33\0\210\0\33\0\212\0" +
		"\33\0\220\0\33\0\225\0\33\0\232\0\33\0\234\0\33\0\235\0\33\0\243\0\33\0\245\0\33" +
		"\0\250\0\33\0\251\0\33\0\252\0\33\0\256\0\33\0\272\0\33\0\311\0\33\0\312\0\33\0\314" +
		"\0\33\0\316\0\33\0\317\0\33\0\320\0\33\0\321\0\33\0\334\0\33\0\336\0\33\0\350\0\33" +
		"\0\353\0\33\0\357\0\33\0\364\0\33\0\366\0\33\0\367\0\33\0\372\0\33\0\u0108\0\33\0" +
		"\u010a\0\33\0\u010e\0\33\0\u0111\0\33\0\u0112\0\33\0\u0114\0\33\0\u0119\0\33\0\u011a" +
		"\0\33\0\u011e\0\33\0\u011f\0\33\0\u0120\0\33\0\u0121\0\33\0\u0122\0\33\0\u0124\0" +
		"\33\0\u012c\0\33\0\u012f\0\33\0\u0130\0\33\0\u0133\0\33\0\u0139\0\33\0\u013c\0\33" +
		"\0\u013e\0\33\0\u0140\0\33\0\u0142\0\33\0\u0143\0\33\0\u014f\0\33\0\u0157\0\33\0" +
		"\u0159\0\33\0\u015b\0\33\0\u015e\0\33\0\u0160\0\33\0\u0161\0\33\0\u0162\0\33\0\u0170" +
		"\0\33\0\u0172\0\33\0\u0178\0\33\0\u017c\0\33\0\u017f\0\33\0\u0181\0\33\0\u0191\0" +
		"\33\0\u0197\0\33\0\u0199\0\33\0\u01a7\0\33\0\1\0\34\0\2\0\34\0\6\0\34\0\65\0\34\0" +
		"\67\0\34\0\71\0\34\0\72\0\34\0\73\0\34\0\75\0\34\0\77\0\34\0\100\0\34\0\122\0\34" +
		"\0\123\0\34\0\130\0\34\0\131\0\34\0\144\0\34\0\146\0\34\0\152\0\34\0\157\0\34\0\160" +
		"\0\34\0\161\0\34\0\162\0\34\0\175\0\34\0\200\0\34\0\202\0\34\0\210\0\34\0\212\0\34" +
		"\0\220\0\34\0\225\0\34\0\232\0\34\0\234\0\34\0\235\0\34\0\243\0\34\0\245\0\34\0\250" +
		"\0\34\0\251\0\34\0\252\0\34\0\256\0\34\0\272\0\34\0\303\0\u0106\0\311\0\34\0\312" +
		"\0\34\0\314\0\34\0\316\0\34\0\317\0\34\0\320\0\34\0\321\0\34\0\334\0\34\0\336\0\34" +
		"\0\350\0\34\0\353\0\34\0\357\0\34\0\364\0\34\0\366\0\34\0\367\0\34\0\372\0\34\0\u0108" +
		"\0\34\0\u010a\0\34\0\u010e\0\34\0\u0111\0\34\0\u0112\0\34\0\u0114\0\34\0\u0119\0" +
		"\34\0\u011a\0\34\0\u011e\0\34\0\u011f\0\34\0\u0120\0\34\0\u0121\0\34\0\u0122\0\34" +
		"\0\u0124\0\34\0\u012c\0\34\0\u012f\0\34\0\u0130\0\34\0\u0133\0\34\0\u0139\0\34\0" +
		"\u013c\0\34\0\u013e\0\34\0\u0140\0\34\0\u0142\0\34\0\u0143\0\34\0\u014f\0\34\0\u0157" +
		"\0\34\0\u0159\0\34\0\u015b\0\34\0\u015e\0\34\0\u0160\0\34\0\u0161\0\34\0\u0162\0" +
		"\34\0\u0170\0\34\0\u0172\0\34\0\u0178\0\34\0\u017c\0\34\0\u017f\0\34\0\u0181\0\34" +
		"\0\u0191\0\34\0\u0197\0\34\0\u0199\0\34\0\u01a7\0\34\0\1\0\35\0\2\0\35\0\6\0\35\0" +
		"\65\0\35\0\67\0\35\0\71\0\35\0\72\0\35\0\73\0\35\0\75\0\35\0\77\0\35\0\100\0\35\0" +
		"\122\0\35\0\123\0\35\0\130\0\35\0\131\0\35\0\144\0\35\0\146\0\35\0\152\0\35\0\157" +
		"\0\35\0\160\0\35\0\161\0\35\0\162\0\35\0\175\0\35\0\177\0\240\0\200\0\35\0\202\0" +
		"\35\0\210\0\35\0\212\0\35\0\220\0\35\0\225\0\35\0\232\0\35\0\234\0\35\0\235\0\35" +
		"\0\243\0\35\0\245\0\35\0\250\0\35\0\251\0\35\0\252\0\35\0\256\0\35\0\272\0\35\0\311" +
		"\0\35\0\312\0\35\0\314\0\35\0\316\0\35\0\317\0\35\0\320\0\35\0\321\0\35\0\334\0\35" +
		"\0\336\0\35\0\350\0\35\0\353\0\35\0\357\0\35\0\364\0\35\0\366\0\35\0\367\0\35\0\372" +
		"\0\35\0\u0108\0\35\0\u010a\0\35\0\u010e\0\35\0\u0111\0\35\0\u0112\0\35\0\u0114\0" +
		"\35\0\u0119\0\35\0\u011a\0\35\0\u011e\0\35\0\u011f\0\35\0\u0120\0\35\0\u0121\0\35" +
		"\0\u0122\0\35\0\u0124\0\35\0\u012c\0\35\0\u012f\0\35\0\u0130\0\35\0\u0133\0\35\0" +
		"\u0139\0\35\0\u013c\0\35\0\u013e\0\35\0\u0140\0\35\0\u0142\0\35\0\u0143\0\35\0\u014f" +
		"\0\35\0\u0157\0\35\0\u0159\0\35\0\u015b\0\35\0\u015e\0\35\0\u0160\0\35\0\u0161\0" +
		"\35\0\u0162\0\35\0\u0170\0\35\0\u0172\0\35\0\u0178\0\35\0\u017c\0\35\0\u017f\0\35" +
		"\0\u0181\0\35\0\u0191\0\35\0\u0197\0\35\0\u0199\0\35\0\u01a7\0\35\0\1\0\36\0\2\0" +
		"\36\0\6\0\36\0\65\0\36\0\67\0\36\0\71\0\36\0\72\0\36\0\73\0\36\0\75\0\36\0\77\0\36" +
		"\0\100\0\36\0\122\0\36\0\123\0\36\0\130\0\36\0\131\0\36\0\144\0\36\0\146\0\36\0\152" +
		"\0\36\0\157\0\36\0\160\0\36\0\161\0\36\0\162\0\36\0\175\0\36\0\200\0\36\0\202\0\36" +
		"\0\210\0\36\0\212\0\36\0\220\0\36\0\225\0\36\0\227\0\301\0\232\0\36\0\234\0\36\0" +
		"\235\0\36\0\243\0\36\0\245\0\36\0\250\0\36\0\251\0\36\0\252\0\36\0\256\0\36\0\272" +
		"\0\36\0\311\0\36\0\312\0\36\0\314\0\36\0\316\0\36\0\317\0\36\0\320\0\36\0\321\0\36" +
		"\0\334\0\36\0\336\0\36\0\350\0\36\0\353\0\36\0\357\0\36\0\364\0\36\0\366\0\36\0\367" +
		"\0\36\0\372\0\36\0\u0108\0\36\0\u010a\0\36\0\u010e\0\36\0\u0111\0\36\0\u0112\0\36" +
		"\0\u0114\0\36\0\u0119\0\36\0\u011a\0\36\0\u011e\0\36\0\u011f\0\36\0\u0120\0\36\0" +
		"\u0121\0\36\0\u0122\0\36\0\u0124\0\36\0\u012c\0\36\0\u012f\0\36\0\u0130\0\36\0\u0133" +
		"\0\36\0\u0139\0\36\0\u013c\0\36\0\u013e\0\36\0\u0140\0\36\0\u0142\0\36\0\u0143\0" +
		"\36\0\u014f\0\36\0\u0157\0\36\0\u0159\0\36\0\u015b\0\36\0\u015e\0\36\0\u0160\0\36" +
		"\0\u0161\0\36\0\u0162\0\36\0\u0170\0\36\0\u0172\0\36\0\u0178\0\36\0\u017c\0\36\0" +
		"\u017f\0\36\0\u0181\0\36\0\u0191\0\36\0\u0197\0\36\0\u0199\0\36\0\u01a7\0\36\0\1" +
		"\0\37\0\2\0\37\0\6\0\37\0\65\0\37\0\67\0\37\0\71\0\37\0\72\0\37\0\73\0\37\0\75\0" +
		"\37\0\77\0\37\0\100\0\37\0\122\0\37\0\123\0\37\0\130\0\37\0\131\0\37\0\144\0\37\0" +
		"\146\0\37\0\152\0\37\0\157\0\37\0\160\0\37\0\161\0\37\0\162\0\37\0\175\0\37\0\177" +
		"\0\241\0\200\0\37\0\202\0\37\0\210\0\37\0\212\0\37\0\220\0\37\0\225\0\37\0\232\0" +
		"\37\0\234\0\37\0\235\0\37\0\243\0\37\0\244\0\241\0\245\0\37\0\250\0\37\0\251\0\37" +
		"\0\252\0\37\0\256\0\37\0\272\0\37\0\311\0\37\0\312\0\37\0\314\0\37\0\316\0\37\0\317" +
		"\0\37\0\320\0\37\0\321\0\37\0\334\0\37\0\336\0\37\0\350\0\37\0\353\0\37\0\357\0\37" +
		"\0\364\0\37\0\366\0\37\0\367\0\37\0\372\0\37\0\u0108\0\37\0\u010a\0\37\0\u010e\0" +
		"\37\0\u0111\0\37\0\u0112\0\37\0\u0114\0\37\0\u0119\0\37\0\u011a\0\37\0\u011e\0\37" +
		"\0\u011f\0\37\0\u0120\0\37\0\u0121\0\37\0\u0122\0\37\0\u0124\0\37\0\u012c\0\37\0" +
		"\u012f\0\37\0\u0130\0\37\0\u0133\0\37\0\u0139\0\37\0\u013c\0\37\0\u013e\0\37\0\u0140" +
		"\0\37\0\u0142\0\37\0\u0143\0\37\0\u014f\0\37\0\u0157\0\37\0\u0159\0\37\0\u015b\0" +
		"\37\0\u015e\0\37\0\u0160\0\37\0\u0161\0\37\0\u0162\0\37\0\u0170\0\37\0\u0172\0\37" +
		"\0\u0178\0\37\0\u017c\0\37\0\u017f\0\37\0\u0181\0\37\0\u0191\0\37\0\u0197\0\37\0" +
		"\u0199\0\37\0\u01a7\0\37\0\1\0\40\0\2\0\40\0\6\0\40\0\65\0\40\0\67\0\40\0\71\0\40" +
		"\0\72\0\40\0\73\0\40\0\75\0\40\0\77\0\40\0\100\0\40\0\122\0\40\0\123\0\40\0\130\0" +
		"\40\0\131\0\40\0\132\0\157\0\144\0\40\0\146\0\40\0\152\0\40\0\157\0\40\0\160\0\40" +
		"\0\161\0\40\0\162\0\40\0\175\0\40\0\200\0\40\0\202\0\40\0\210\0\40\0\212\0\40\0\220" +
		"\0\40\0\225\0\40\0\232\0\40\0\234\0\40\0\235\0\40\0\243\0\40\0\245\0\40\0\250\0\40" +
		"\0\251\0\40\0\252\0\40\0\256\0\40\0\272\0\40\0\311\0\40\0\312\0\40\0\314\0\40\0\316" +
		"\0\40\0\317\0\40\0\320\0\40\0\321\0\40\0\334\0\40\0\336\0\40\0\350\0\40\0\353\0\40" +
		"\0\357\0\40\0\364\0\40\0\366\0\40\0\367\0\40\0\372\0\40\0\u0108\0\40\0\u010a\0\40" +
		"\0\u010e\0\40\0\u0111\0\40\0\u0112\0\40\0\u0114\0\40\0\u0119\0\40\0\u011a\0\40\0" +
		"\u011e\0\40\0\u011f\0\40\0\u0120\0\40\0\u0121\0\40\0\u0122\0\40\0\u0124\0\40\0\u012c" +
		"\0\40\0\u012f\0\40\0\u0130\0\40\0\u0133\0\40\0\u0139\0\40\0\u013c\0\40\0\u013e\0" +
		"\40\0\u0140\0\40\0\u0142\0\40\0\u0143\0\40\0\u014f\0\40\0\u0157\0\40\0\u0159\0\40" +
		"\0\u015b\0\40\0\u015e\0\40\0\u0160\0\40\0\u0161\0\40\0\u0162\0\40\0\u0170\0\40\0" +
		"\u0172\0\40\0\u0178\0\40\0\u017c\0\40\0\u017f\0\40\0\u0181\0\40\0\u0191\0\40\0\u0197" +
		"\0\40\0\u0199\0\40\0\u01a7\0\40\0\1\0\41\0\2\0\41\0\6\0\41\0\65\0\41\0\67\0\41\0" +
		"\71\0\41\0\72\0\41\0\73\0\41\0\75\0\41\0\77\0\41\0\100\0\41\0\122\0\41\0\123\0\41" +
		"\0\130\0\41\0\131\0\41\0\144\0\41\0\146\0\41\0\152\0\41\0\157\0\41\0\160\0\41\0\161" +
		"\0\41\0\162\0\41\0\175\0\41\0\200\0\41\0\202\0\41\0\210\0\41\0\212\0\41\0\220\0\41" +
		"\0\225\0\41\0\232\0\41\0\234\0\41\0\235\0\41\0\243\0\41\0\245\0\41\0\250\0\41\0\251" +
		"\0\41\0\252\0\41\0\256\0\41\0\272\0\41\0\311\0\41\0\312\0\41\0\314\0\41\0\315\0\u0111" +
		"\0\316\0\41\0\317\0\41\0\320\0\41\0\321\0\41\0\334\0\41\0\336\0\41\0\350\0\41\0\353" +
		"\0\41\0\357\0\41\0\364\0\41\0\366\0\41\0\367\0\41\0\372\0\41\0\u0108\0\41\0\u010a" +
		"\0\41\0\u010e\0\41\0\u0111\0\41\0\u0112\0\41\0\u0114\0\41\0\u0119\0\41\0\u011a\0" +
		"\41\0\u011e\0\41\0\u011f\0\41\0\u0120\0\41\0\u0121\0\41\0\u0122\0\41\0\u0124\0\41" +
		"\0\u012c\0\41\0\u012f\0\41\0\u0130\0\41\0\u0133\0\41\0\u0139\0\41\0\u013c\0\41\0" +
		"\u013e\0\41\0\u0140\0\41\0\u0142\0\41\0\u0143\0\41\0\u014f\0\41\0\u0157\0\41\0\u0159" +
		"\0\41\0\u015b\0\41\0\u015e\0\41\0\u0160\0\41\0\u0161\0\41\0\u0162\0\41\0\u0170\0" +
		"\41\0\u0172\0\41\0\u0178\0\41\0\u017c\0\41\0\u017f\0\41\0\u0181\0\41\0\u0191\0\41" +
		"\0\u0197\0\41\0\u0199\0\41\0\u01a7\0\41\0\1\0\42\0\2\0\42\0\6\0\42\0\65\0\42\0\67" +
		"\0\42\0\71\0\42\0\72\0\42\0\73\0\42\0\75\0\42\0\77\0\42\0\100\0\42\0\122\0\42\0\123" +
		"\0\42\0\130\0\42\0\131\0\42\0\144\0\42\0\146\0\42\0\152\0\42\0\157\0\42\0\160\0\42" +
		"\0\161\0\42\0\162\0\42\0\175\0\42\0\200\0\42\0\201\0\256\0\202\0\42\0\210\0\42\0" +
		"\212\0\42\0\220\0\42\0\225\0\42\0\232\0\42\0\234\0\42\0\235\0\42\0\243\0\42\0\245" +
		"\0\42\0\250\0\42\0\251\0\42\0\252\0\42\0\256\0\42\0\263\0\256\0\267\0\256\0\272\0" +
		"\42\0\311\0\42\0\312\0\42\0\314\0\42\0\316\0\42\0\317\0\42\0\320\0\42\0\321\0\42" +
		"\0\334\0\42\0\336\0\42\0\350\0\42\0\353\0\42\0\357\0\42\0\364\0\42\0\366\0\42\0\367" +
		"\0\42\0\372\0\42\0\375\0\256\0\u0108\0\42\0\u010a\0\42\0\u010e\0\42\0\u0111\0\42" +
		"\0\u0112\0\42\0\u0114\0\42\0\u0119\0\42\0\u011a\0\42\0\u011e\0\42\0\u011f\0\42\0" +
		"\u0120\0\42\0\u0121\0\42\0\u0122\0\42\0\u0124\0\42\0\u012c\0\42\0\u012f\0\42\0\u0130" +
		"\0\42\0\u0133\0\42\0\u0139\0\42\0\u013c\0\42\0\u013e\0\42\0\u0140\0\42\0\u0142\0" +
		"\42\0\u0143\0\42\0\u014f\0\42\0\u0157\0\42\0\u0159\0\42\0\u015b\0\42\0\u015e\0\42" +
		"\0\u0160\0\42\0\u0161\0\42\0\u0162\0\42\0\u0170\0\42\0\u0172\0\42\0\u0178\0\42\0" +
		"\u017c\0\42\0\u017f\0\42\0\u0181\0\42\0\u0191\0\42\0\u0197\0\42\0\u0199\0\42\0\u01a7" +
		"\0\42\0\1\0\43\0\2\0\43\0\6\0\43\0\65\0\43\0\67\0\43\0\71\0\43\0\72\0\43\0\73\0\43" +
		"\0\75\0\43\0\77\0\43\0\100\0\43\0\122\0\43\0\123\0\43\0\130\0\43\0\131\0\43\0\144" +
		"\0\43\0\146\0\43\0\152\0\43\0\157\0\43\0\160\0\43\0\161\0\43\0\162\0\43\0\175\0\43" +
		"\0\177\0\242\0\200\0\43\0\202\0\43\0\210\0\43\0\212\0\43\0\220\0\43\0\225\0\43\0" +
		"\232\0\43\0\234\0\43\0\235\0\43\0\243\0\43\0\245\0\43\0\250\0\43\0\251\0\43\0\252" +
		"\0\43\0\256\0\43\0\272\0\43\0\311\0\43\0\312\0\43\0\314\0\43\0\316\0\43\0\317\0\43" +
		"\0\320\0\43\0\321\0\43\0\334\0\43\0\336\0\43\0\350\0\43\0\353\0\43\0\357\0\43\0\364" +
		"\0\43\0\366\0\43\0\367\0\43\0\372\0\43\0\u0108\0\43\0\u010a\0\43\0\u010e\0\43\0\u0111" +
		"\0\43\0\u0112\0\43\0\u0114\0\43\0\u0119\0\43\0\u011a\0\43\0\u011e\0\43\0\u011f\0" +
		"\43\0\u0120\0\43\0\u0121\0\43\0\u0122\0\43\0\u0124\0\43\0\u012c\0\43\0\u012f\0\43" +
		"\0\u0130\0\43\0\u0133\0\43\0\u0139\0\43\0\u013c\0\43\0\u013e\0\43\0\u0140\0\43\0" +
		"\u0142\0\43\0\u0143\0\43\0\u014f\0\43\0\u0157\0\43\0\u0159\0\43\0\u015b\0\43\0\u015e" +
		"\0\43\0\u0160\0\43\0\u0161\0\43\0\u0162\0\43\0\u0170\0\43\0\u0172\0\43\0\u0178\0" +
		"\43\0\u017c\0\43\0\u017f\0\43\0\u0181\0\43\0\u0191\0\43\0\u0197\0\43\0\u0199\0\43" +
		"\0\u01a7\0\43\0\1\0\44\0\2\0\44\0\6\0\44\0\65\0\44\0\67\0\44\0\71\0\44\0\72\0\44" +
		"\0\73\0\44\0\75\0\44\0\77\0\44\0\100\0\44\0\122\0\44\0\123\0\44\0\130\0\44\0\131" +
		"\0\44\0\143\0\161\0\144\0\44\0\146\0\44\0\152\0\44\0\157\0\44\0\160\0\44\0\161\0" +
		"\44\0\162\0\44\0\175\0\44\0\200\0\44\0\202\0\44\0\210\0\44\0\212\0\44\0\220\0\44" +
		"\0\225\0\44\0\232\0\44\0\234\0\44\0\235\0\44\0\243\0\44\0\245\0\44\0\250\0\44\0\251" +
		"\0\44\0\252\0\44\0\256\0\44\0\272\0\44\0\311\0\44\0\312\0\44\0\314\0\44\0\316\0\44" +
		"\0\317\0\44\0\320\0\44\0\321\0\44\0\334\0\44\0\336\0\44\0\350\0\44\0\353\0\44\0\357" +
		"\0\44\0\364\0\44\0\366\0\44\0\367\0\44\0\372\0\44\0\u0108\0\44\0\u010a\0\44\0\u010e" +
		"\0\44\0\u0111\0\44\0\u0112\0\44\0\u0114\0\44\0\u0119\0\44\0\u011a\0\44\0\u011e\0" +
		"\44\0\u011f\0\44\0\u0120\0\44\0\u0121\0\44\0\u0122\0\44\0\u0124\0\44\0\u012c\0\44" +
		"\0\u012f\0\44\0\u0130\0\44\0\u0133\0\44\0\u0139\0\44\0\u013c\0\44\0\u013e\0\44\0" +
		"\u0140\0\44\0\u0142\0\44\0\u0143\0\44\0\u014f\0\44\0\u0157\0\44\0\u0159\0\44\0\u015b" +
		"\0\44\0\u015e\0\44\0\u0160\0\44\0\u0161\0\44\0\u0162\0\44\0\u0170\0\44\0\u0172\0" +
		"\44\0\u0178\0\44\0\u017c\0\44\0\u017f\0\44\0\u0181\0\44\0\u0191\0\44\0\u0197\0\44" +
		"\0\u0199\0\44\0\u01a7\0\44\0\1\0\45\0\2\0\45\0\6\0\45\0\65\0\45\0\67\0\45\0\71\0" +
		"\45\0\72\0\45\0\73\0\45\0\75\0\45\0\77\0\45\0\100\0\45\0\122\0\45\0\123\0\45\0\130" +
		"\0\45\0\131\0\45\0\144\0\45\0\146\0\45\0\152\0\45\0\157\0\45\0\160\0\45\0\161\0\45" +
		"\0\162\0\45\0\175\0\45\0\200\0\45\0\202\0\45\0\210\0\45\0\212\0\45\0\220\0\45\0\225" +
		"\0\45\0\232\0\45\0\234\0\45\0\235\0\45\0\243\0\45\0\245\0\45\0\250\0\45\0\251\0\45" +
		"\0\252\0\45\0\256\0\45\0\272\0\45\0\311\0\45\0\312\0\45\0\314\0\45\0\315\0\u0112" +
		"\0\316\0\45\0\317\0\45\0\320\0\45\0\321\0\45\0\334\0\45\0\336\0\45\0\350\0\45\0\353" +
		"\0\45\0\357\0\45\0\364\0\45\0\366\0\45\0\367\0\45\0\372\0\45\0\u0108\0\45\0\u010a" +
		"\0\45\0\u010e\0\45\0\u0111\0\45\0\u0112\0\45\0\u0114\0\45\0\u0119\0\45\0\u011a\0" +
		"\45\0\u011e\0\45\0\u011f\0\45\0\u0120\0\45\0\u0121\0\45\0\u0122\0\45\0\u0124\0\45" +
		"\0\u012c\0\45\0\u012f\0\45\0\u0130\0\45\0\u0133\0\45\0\u0139\0\45\0\u013c\0\45\0" +
		"\u013e\0\45\0\u0140\0\45\0\u0142\0\45\0\u0143\0\45\0\u014f\0\45\0\u0157\0\45\0\u0159" +
		"\0\45\0\u015b\0\45\0\u015e\0\45\0\u0160\0\45\0\u0161\0\45\0\u0162\0\45\0\u0170\0" +
		"\45\0\u0172\0\45\0\u0178\0\45\0\u017c\0\45\0\u017f\0\45\0\u0181\0\45\0\u0191\0\45" +
		"\0\u0197\0\45\0\u0199\0\45\0\u01a7\0\45\0\1\0\46\0\2\0\46\0\6\0\46\0\65\0\46\0\67" +
		"\0\46\0\71\0\46\0\72\0\46\0\73\0\46\0\75\0\46\0\77\0\46\0\100\0\46\0\122\0\46\0\123" +
		"\0\46\0\130\0\46\0\131\0\46\0\144\0\46\0\146\0\46\0\152\0\46\0\157\0\46\0\160\0\46" +
		"\0\161\0\46\0\162\0\46\0\175\0\46\0\200\0\46\0\202\0\46\0\210\0\46\0\212\0\46\0\220" +
		"\0\46\0\225\0\46\0\232\0\46\0\234\0\46\0\235\0\46\0\243\0\46\0\245\0\46\0\250\0\46" +
		"\0\251\0\46\0\252\0\46\0\256\0\46\0\272\0\46\0\311\0\46\0\312\0\46\0\314\0\46\0\316" +
		"\0\46\0\317\0\46\0\320\0\46\0\321\0\46\0\334\0\46\0\336\0\46\0\350\0\46\0\353\0\46" +
		"\0\357\0\46\0\364\0\46\0\366\0\46\0\367\0\46\0\372\0\46\0\u0108\0\46\0\u010a\0\46" +
		"\0\u010e\0\46\0\u0111\0\46\0\u0112\0\46\0\u0114\0\46\0\u0119\0\46\0\u011a\0\46\0" +
		"\u011e\0\46\0\u011f\0\46\0\u0120\0\46\0\u0121\0\46\0\u0122\0\46\0\u0124\0\46\0\u012c" +
		"\0\46\0\u012f\0\46\0\u0130\0\46\0\u0133\0\46\0\u0139\0\46\0\u013c\0\46\0\u013e\0" +
		"\46\0\u0140\0\46\0\u0142\0\46\0\u0143\0\46\0\u0146\0\u0184\0\u014f\0\46\0\u0157\0" +
		"\46\0\u0159\0\46\0\u015b\0\46\0\u015e\0\46\0\u0160\0\46\0\u0161\0\46\0\u0162\0\46" +
		"\0\u0170\0\46\0\u0172\0\46\0\u0178\0\46\0\u017c\0\46\0\u017f\0\46\0\u0181\0\46\0" +
		"\u0191\0\46\0\u0197\0\46\0\u0199\0\46\0\u01a7\0\46\0\1\0\47\0\2\0\47\0\6\0\47\0\65" +
		"\0\47\0\67\0\47\0\71\0\47\0\72\0\47\0\73\0\47\0\75\0\47\0\77\0\47\0\100\0\47\0\122" +
		"\0\47\0\123\0\47\0\130\0\47\0\131\0\47\0\144\0\47\0\146\0\47\0\152\0\47\0\157\0\47" +
		"\0\160\0\47\0\161\0\47\0\162\0\47\0\175\0\47\0\200\0\47\0\202\0\47\0\210\0\47\0\212" +
		"\0\47\0\220\0\47\0\225\0\47\0\232\0\47\0\234\0\47\0\235\0\47\0\243\0\47\0\245\0\47" +
		"\0\250\0\47\0\251\0\47\0\252\0\47\0\256\0\47\0\272\0\47\0\311\0\47\0\312\0\47\0\314" +
		"\0\47\0\316\0\47\0\317\0\47\0\320\0\47\0\321\0\47\0\334\0\47\0\336\0\47\0\350\0\47" +
		"\0\353\0\47\0\357\0\47\0\364\0\47\0\366\0\47\0\367\0\47\0\372\0\47\0\u0108\0\47\0" +
		"\u010a\0\47\0\u010e\0\47\0\u0111\0\47\0\u0112\0\47\0\u0114\0\47\0\u0119\0\47\0\u011a" +
		"\0\47\0\u011e\0\47\0\u011f\0\47\0\u0120\0\47\0\u0121\0\47\0\u0122\0\47\0\u0124\0" +
		"\47\0\u012c\0\47\0\u012f\0\47\0\u0130\0\47\0\u0133\0\47\0\u0139\0\47\0\u013c\0\47" +
		"\0\u013e\0\47\0\u0140\0\47\0\u0142\0\47\0\u0143\0\47\0\u0146\0\u0185\0\u014f\0\47" +
		"\0\u0157\0\47\0\u0159\0\47\0\u015b\0\47\0\u015e\0\47\0\u0160\0\47\0\u0161\0\47\0" +
		"\u0162\0\47\0\u0170\0\47\0\u0172\0\47\0\u0178\0\47\0\u017c\0\47\0\u017f\0\47\0\u0181" +
		"\0\47\0\u0191\0\47\0\u0197\0\47\0\u0199\0\47\0\u01a7\0\47\0\1\0\50\0\2\0\50\0\6\0" +
		"\50\0\65\0\50\0\67\0\50\0\71\0\50\0\72\0\50\0\73\0\50\0\75\0\50\0\77\0\50\0\100\0" +
		"\50\0\122\0\50\0\123\0\50\0\130\0\50\0\131\0\50\0\144\0\50\0\146\0\50\0\152\0\50" +
		"\0\157\0\50\0\160\0\50\0\161\0\50\0\162\0\50\0\175\0\50\0\200\0\50\0\201\0\257\0" +
		"\202\0\50\0\210\0\50\0\212\0\50\0\220\0\50\0\225\0\50\0\232\0\50\0\234\0\50\0\235" +
		"\0\50\0\243\0\50\0\245\0\50\0\250\0\50\0\251\0\50\0\252\0\50\0\256\0\50\0\263\0\257" +
		"\0\267\0\257\0\272\0\50\0\311\0\50\0\312\0\50\0\314\0\50\0\316\0\50\0\317\0\50\0" +
		"\320\0\50\0\321\0\50\0\334\0\50\0\336\0\50\0\350\0\50\0\353\0\50\0\357\0\50\0\364" +
		"\0\50\0\366\0\50\0\367\0\50\0\372\0\50\0\375\0\257\0\u0108\0\50\0\u010a\0\50\0\u010e" +
		"\0\50\0\u0111\0\50\0\u0112\0\50\0\u0114\0\50\0\u0119\0\50\0\u011a\0\50\0\u011e\0" +
		"\50\0\u011f\0\50\0\u0120\0\50\0\u0121\0\50\0\u0122\0\50\0\u0124\0\50\0\u012c\0\50" +
		"\0\u012f\0\50\0\u0130\0\50\0\u0133\0\50\0\u0139\0\50\0\u013c\0\50\0\u013e\0\50\0" +
		"\u0140\0\50\0\u0142\0\50\0\u0143\0\50\0\u014f\0\50\0\u0157\0\50\0\u0159\0\50\0\u015b" +
		"\0\50\0\u015e\0\50\0\u0160\0\50\0\u0161\0\50\0\u0162\0\50\0\u0170\0\50\0\u0172\0" +
		"\50\0\u0178\0\50\0\u017c\0\50\0\u017f\0\50\0\u0181\0\50\0\u0191\0\50\0\u0197\0\50" +
		"\0\u0199\0\50\0\u01a7\0\50\0\1\0\51\0\2\0\51\0\6\0\51\0\65\0\51\0\67\0\51\0\71\0" +
		"\51\0\72\0\51\0\73\0\51\0\75\0\51\0\77\0\51\0\100\0\51\0\122\0\51\0\123\0\51\0\130" +
		"\0\51\0\131\0\51\0\143\0\162\0\144\0\51\0\146\0\51\0\152\0\51\0\157\0\51\0\160\0" +
		"\51\0\161\0\51\0\162\0\51\0\175\0\51\0\200\0\51\0\202\0\51\0\210\0\51\0\212\0\51" +
		"\0\220\0\51\0\225\0\51\0\232\0\51\0\234\0\51\0\235\0\51\0\243\0\51\0\245\0\51\0\250" +
		"\0\51\0\251\0\51\0\252\0\51\0\256\0\51\0\272\0\51\0\311\0\51\0\312\0\51\0\314\0\51" +
		"\0\316\0\51\0\317\0\51\0\320\0\51\0\321\0\51\0\334\0\51\0\336\0\51\0\350\0\51\0\353" +
		"\0\51\0\357\0\51\0\364\0\51\0\366\0\51\0\367\0\51\0\372\0\51\0\u0108\0\51\0\u010a" +
		"\0\51\0\u010e\0\51\0\u0111\0\51\0\u0112\0\51\0\u0114\0\51\0\u0119\0\51\0\u011a\0" +
		"\51\0\u011e\0\51\0\u011f\0\51\0\u0120\0\51\0\u0121\0\51\0\u0122\0\51\0\u0124\0\51" +
		"\0\u012c\0\51\0\u012f\0\51\0\u0130\0\51\0\u0133\0\51\0\u0139\0\51\0\u013c\0\51\0" +
		"\u013e\0\51\0\u0140\0\51\0\u0142\0\51\0\u0143\0\51\0\u014f\0\51\0\u0157\0\51\0\u0159" +
		"\0\51\0\u015b\0\51\0\u015e\0\51\0\u0160\0\51\0\u0161\0\51\0\u0162\0\51\0\u0170\0" +
		"\51\0\u0172\0\51\0\u0178\0\51\0\u017c\0\51\0\u017f\0\51\0\u0181\0\51\0\u0191\0\51" +
		"\0\u0197\0\51\0\u0199\0\51\0\u01a7\0\51\0\145\0\170\0\176\0\170\0\201\0\170\0\250" +
		"\0\324\0\263\0\170\0\267\0\170\0\320\0\324\0\334\0\324\0\336\0\324\0\364\0\324\0" +
		"\366\0\324\0\367\0\324\0\372\0\324\0\375\0\170\0\u0119\0\324\0\u011e\0\324\0\u0122" +
		"\0\324\0\u0124\0\324\0\u0139\0\324\0\u013c\0\324\0\u013e\0\324\0\u0140\0\324\0\u0142" +
		"\0\324\0\u0143\0\324\0\u0148\0\324\0\u0178\0\324\0\u017c\0\324\0\u017f\0\324\0\u0181" +
		"\0\324\0\u0189\0\324\0\u01a7\0\324\0\152\0\175\0\167\0\222\0\224\0\222\0\277\0\222" +
		"\0\352\0\u0130\0\1\0\52\0\2\0\56\0\6\0\52\0\65\0\101\0\67\0\106\0\71\0\56\0\72\0" +
		"\111\0\73\0\113\0\75\0\52\0\77\0\101\0\100\0\101\0\122\0\134\0\123\0\101\0\130\0" +
		"\145\0\131\0\52\0\144\0\164\0\146\0\145\0\152\0\176\0\157\0\201\0\160\0\134\0\161" +
		"\0\213\0\162\0\213\0\175\0\145\0\200\0\246\0\202\0\201\0\210\0\267\0\212\0\134\0" +
		"\220\0\164\0\225\0\145\0\232\0\302\0\234\0\134\0\235\0\306\0\243\0\134\0\245\0\313" +
		"\0\250\0\325\0\251\0\352\0\252\0\353\0\256\0\134\0\272\0\213\0\311\0\134\0\312\0" +
		"\u010d\0\314\0\52\0\316\0\u0113\0\317\0\101\0\320\0\325\0\321\0\134\0\334\0\325\0" +
		"\336\0\325\0\350\0\325\0\353\0\u0132\0\357\0\134\0\364\0\325\0\366\0\325\0\367\0" +
		"\325\0\372\0\325\0\u0108\0\134\0\u010a\0\u014e\0\u010e\0\134\0\u0111\0\134\0\u0112" +
		"\0\134\0\u0114\0\101\0\u0119\0\325\0\u011a\0\134\0\u011e\0\325\0\u011f\0\u0162\0" +
		"\u0120\0\52\0\u0121\0\52\0\u0122\0\325\0\u0124\0\325\0\u012c\0\52\0\u012f\0\u016e" +
		"\0\u0130\0\u016f\0\u0133\0\353\0\u0139\0\325\0\u013c\0\325\0\u013e\0\325\0\u0140" +
		"\0\325\0\u0142\0\325\0\u0143\0\325\0\u014f\0\134\0\u0157\0\101\0\u0159\0\101\0\u015b" +
		"\0\134\0\u015e\0\134\0\u0160\0\u0162\0\u0161\0\u0162\0\u0162\0\52\0\u0170\0\134\0" +
		"\u0172\0\134\0\u0178\0\325\0\u017c\0\325\0\u017f\0\325\0\u0181\0\325\0\u0191\0\134" +
		"\0\u0197\0\u0162\0\u0199\0\u0162\0\u01a7\0\325\0\1\0\53\0\6\0\53\0\75\0\53\0\122" +
		"\0\135\0\131\0\53\0\314\0\53\0\u010e\0\135\0\u012c\0\u016c\0\u014f\0\135\0\u0155" +
		"\0\u018d\0\u0156\0\u018e\0\u0170\0\135\0\167\0\223\0\224\0\275\0\277\0\u0102\0\2" +
		"\0\57\0\71\0\57\0\2\0\60\0\71\0\107\0\250\0\326\0\320\0\326\0\334\0\326\0\336\0\326" +
		"\0\364\0\326\0\366\0\326\0\367\0\326\0\372\0\326\0\u0119\0\326\0\u011e\0\326\0\u0122" +
		"\0\326\0\u0124\0\326\0\u0139\0\326\0\u013c\0\326\0\u013e\0\326\0\u0140\0\326\0\u0142" +
		"\0\326\0\u0143\0\326\0\u0148\0\u0187\0\u0178\0\326\0\u017c\0\326\0\u017f\0\326\0" +
		"\u0181\0\326\0\u0189\0\u0187\0\u01a7\0\326\0\1\0\54\0\6\0\54\0\73\0\114\0\75\0\54" +
		"\0\131\0\54\0\146\0\173\0\200\0\247\0\202\0\264\0\225\0\173\0\250\0\327\0\314\0\54" +
		"\0\320\0\327\0\336\0\u0126\0\364\0\327\0\366\0\327\0\367\0\327\0\372\0\327\0\u0119" +
		"\0\u0126\0\u011e\0\327\0\u0122\0\327\0\u0124\0\u0126\0\u0139\0\327\0\u013c\0\327" +
		"\0\u013e\0\327\0\u0140\0\327\0\u0142\0\327\0\u0143\0\327\0\u0178\0\327\0\u017c\0" +
		"\327\0\u017f\0\327\0\u0181\0\327\0\u01a7\0\327\0\3\0\61\0\0\0\u01b8\0\61\0\73\0\0" +
		"\0\3\0\73\0\115\0\115\0\133\0\61\0\74\0\73\0\116\0\1\0\55\0\6\0\55\0\75\0\55\0\131" +
		"\0\55\0\250\0\330\0\314\0\55\0\320\0\330\0\334\0\330\0\336\0\330\0\350\0\330\0\364" +
		"\0\330\0\366\0\330\0\367\0\330\0\372\0\330\0\u0119\0\330\0\u011e\0\330\0\u011f\0" +
		"\u0163\0\u0120\0\330\0\u0121\0\330\0\u0122\0\330\0\u0124\0\330\0\u012c\0\u016d\0" +
		"\u0139\0\330\0\u013c\0\330\0\u013e\0\330\0\u0140\0\330\0\u0142\0\330\0\u0143\0\330" +
		"\0\u0160\0\u0163\0\u0161\0\u0163\0\u0162\0\u0196\0\u0178\0\330\0\u017c\0\330\0\u017f" +
		"\0\330\0\u0181\0\330\0\u0197\0\u0163\0\u0199\0\u0163\0\u01a7\0\330\0\122\0\136\0" +
		"\160\0\212\0\212\0\270\0\234\0\303\0\243\0\310\0\256\0\363\0\311\0\u010c\0\321\0" +
		"\u011b\0\357\0\u0135\0\u0108\0\303\0\u010e\0\136\0\u0111\0\u0152\0\u0112\0\u0153" +
		"\0\u011a\0\u015c\0\u014f\0\136\0\u015b\0\310\0\u015e\0\u011b\0\u0170\0\136\0\u0172" +
		"\0\u019d\0\u0191\0\u010c\0\145\0\171\0\176\0\171\0\201\0\260\0\263\0\260\0\267\0" +
		"\260\0\375\0\260\0\130\0\146\0\175\0\225\0\130\0\147\0\146\0\174\0\175\0\147\0\225" +
		"\0\174\0\130\0\150\0\146\0\150\0\175\0\150\0\225\0\150\0\130\0\151\0\146\0\151\0" +
		"\175\0\151\0\225\0\151\0\130\0\152\0\146\0\152\0\175\0\152\0\225\0\152\0\144\0\165" +
		"\0\130\0\153\0\146\0\153\0\175\0\153\0\225\0\153\0\u0101\0\u0147\0\u0149\0\u0147" +
		"\0\u0146\0\u0186\0\130\0\154\0\146\0\154\0\175\0\154\0\225\0\154\0\161\0\214\0\162" +
		"\0\216\0\130\0\155\0\146\0\155\0\175\0\155\0\225\0\155\0\144\0\166\0\220\0\274\0" +
		"\161\0\215\0\162\0\215\0\272\0\377\0\157\0\202\0\157\0\203\0\202\0\265\0\157\0\204" +
		"\0\202\0\204\0\201\0\261\0\263\0\370\0\267\0\373\0\375\0\u0144\0\253\0\360\0\362" +
		"\0\360\0\177\0\243\0\177\0\244\0\157\0\205\0\202\0\205\0\157\0\206\0\202\0\206\0" +
		"\235\0\307\0\234\0\304\0\234\0\305\0\u0108\0\u014d\0\243\0\311\0\u015b\0\u0191\0" +
		"\357\0\u0136\0\250\0\331\0\320\0\331\0\364\0\331\0\366\0\331\0\367\0\331\0\372\0" +
		"\331\0\u011e\0\331\0\u0139\0\331\0\u013c\0\331\0\u013e\0\331\0\u0140\0\331\0\u0142" +
		"\0\331\0\u0143\0\331\0\u0178\0\331\0\u017c\0\331\0\u017f\0\331\0\u0181\0\331\0\u01a7" +
		"\0\331\0\250\0\332\0\320\0\u0118\0\364\0\u0138\0\366\0\u013a\0\367\0\u013b\0\372" +
		"\0\u013f\0\u011e\0\u015f\0\u0139\0\u0174\0\u013c\0\u0177\0\u013e\0\u0179\0\u0140" +
		"\0\u017b\0\u0142\0\u017d\0\u0143\0\u017e\0\u0178\0\u01a0\0\u017c\0\u01a3\0\u017f" +
		"\0\u01a6\0\u0181\0\u01a8\0\u01a7\0\u01b3\0\250\0\333\0\320\0\333\0\364\0\333\0\366" +
		"\0\333\0\367\0\333\0\372\0\333\0\u011e\0\333\0\u0122\0\u0168\0\u0139\0\333\0\u013c" +
		"\0\333\0\u013e\0\333\0\u0140\0\333\0\u0142\0\333\0\u0143\0\333\0\u0178\0\333\0\u017c" +
		"\0\333\0\u017f\0\333\0\u0181\0\333\0\u01a7\0\333\0\250\0\334\0\320\0\334\0\364\0" +
		"\334\0\366\0\334\0\367\0\334\0\372\0\334\0\u011e\0\334\0\u0122\0\334\0\u0139\0\334" +
		"\0\u013c\0\334\0\u013e\0\334\0\u0140\0\334\0\u0142\0\334\0\u0143\0\334\0\u0178\0" +
		"\334\0\u017c\0\334\0\u017f\0\334\0\u0181\0\334\0\u01a7\0\334\0\250\0\335\0\320\0" +
		"\335\0\334\0\335\0\336\0\335\0\364\0\335\0\366\0\335\0\367\0\335\0\372\0\335\0\u0119" +
		"\0\335\0\u011e\0\335\0\u0122\0\335\0\u0124\0\335\0\u0139\0\335\0\u013c\0\335\0\u013e" +
		"\0\335\0\u0140\0\335\0\u0142\0\335\0\u0143\0\335\0\u0178\0\335\0\u017c\0\335\0\u017f" +
		"\0\335\0\u0181\0\335\0\u01a7\0\335\0\201\0\262\0\261\0\365\0\263\0\371\0\267\0\374" +
		"\0\351\0\u012e\0\370\0\u013d\0\373\0\u0141\0\375\0\u0145\0\u0125\0\u016a\0\u0128" +
		"\0\u016b\0\u0144\0\u0180\0\u0169\0\u019a\0\352\0\u0131\0\u016f\0\u019b\0\250\0\336" +
		"\0\320\0\u0119\0\334\0\u0124\0\364\0\336\0\366\0\336\0\367\0\336\0\372\0\336\0\u011e" +
		"\0\336\0\u0122\0\336\0\u0139\0\336\0\u013c\0\336\0\u013e\0\336\0\u0140\0\336\0\u0142" +
		"\0\336\0\u0143\0\336\0\u0178\0\336\0\u017c\0\336\0\u017f\0\336\0\u0181\0\336\0\u01a7" +
		"\0\336\0\250\0\337\0\320\0\337\0\334\0\337\0\336\0\u0127\0\364\0\337\0\366\0\337" +
		"\0\367\0\337\0\372\0\337\0\u0119\0\u0127\0\u011e\0\337\0\u0122\0\337\0\u0124\0\u0127" +
		"\0\u0139\0\337\0\u013c\0\337\0\u013e\0\337\0\u0140\0\337\0\u0142\0\337\0\u0143\0" +
		"\337\0\u0178\0\337\0\u017c\0\337\0\u017f\0\337\0\u0181\0\337\0\u01a7\0\337\0\321" +
		"\0\u011c\0\250\0\340\0\320\0\340\0\334\0\340\0\336\0\340\0\364\0\340\0\366\0\340" +
		"\0\367\0\340\0\372\0\340\0\u0119\0\340\0\u011e\0\340\0\u0122\0\340\0\u0124\0\340" +
		"\0\u0139\0\340\0\u013c\0\340\0\u013e\0\340\0\u0140\0\340\0\u0142\0\340\0\u0143\0" +
		"\340\0\u0178\0\340\0\u017c\0\340\0\u017f\0\340\0\u0181\0\340\0\u01a7\0\340\0\321" +
		"\0\u011d\0\u015e\0\u0192\0\250\0\341\0\320\0\341\0\334\0\341\0\336\0\341\0\364\0" +
		"\341\0\366\0\341\0\367\0\341\0\372\0\341\0\u0119\0\341\0\u011e\0\341\0\u0122\0\341" +
		"\0\u0124\0\341\0\u0139\0\341\0\u013c\0\341\0\u013e\0\341\0\u0140\0\341\0\u0142\0" +
		"\341\0\u0143\0\341\0\u0178\0\341\0\u017c\0\341\0\u017f\0\341\0\u0181\0\341\0\u01a7" +
		"\0\341\0\250\0\342\0\320\0\342\0\334\0\342\0\336\0\342\0\364\0\342\0\366\0\342\0" +
		"\367\0\342\0\372\0\342\0\u0119\0\342\0\u011e\0\342\0\u0122\0\342\0\u0124\0\342\0" +
		"\u0139\0\342\0\u013c\0\342\0\u013e\0\342\0\u0140\0\342\0\u0142\0\342\0\u0143\0\342" +
		"\0\u0178\0\342\0\u017c\0\342\0\u017f\0\342\0\u0181\0\342\0\u01a7\0\342\0\250\0\343" +
		"\0\320\0\343\0\334\0\343\0\336\0\343\0\350\0\u012d\0\364\0\343\0\366\0\343\0\367" +
		"\0\343\0\372\0\343\0\u0119\0\343\0\u011e\0\343\0\u0122\0\343\0\u0124\0\343\0\u0139" +
		"\0\343\0\u013c\0\343\0\u013e\0\343\0\u0140\0\343\0\u0142\0\343\0\u0143\0\343\0\u0178" +
		"\0\343\0\u017c\0\343\0\u017f\0\343\0\u0181\0\343\0\u01a7\0\343\0\250\0\344\0\320" +
		"\0\344\0\334\0\344\0\336\0\344\0\350\0\344\0\364\0\344\0\366\0\344\0\367\0\344\0" +
		"\372\0\344\0\u0119\0\344\0\u011e\0\344\0\u0120\0\u0166\0\u0121\0\u0167\0\u0122\0" +
		"\344\0\u0124\0\344\0\u0139\0\344\0\u013c\0\344\0\u013e\0\344\0\u0140\0\344\0\u0142" +
		"\0\344\0\u0143\0\344\0\u0178\0\344\0\u017c\0\344\0\u017f\0\344\0\u0181\0\344\0\u01a7" +
		"\0\344\0\250\0\345\0\320\0\345\0\334\0\345\0\336\0\345\0\350\0\345\0\364\0\345\0" +
		"\366\0\345\0\367\0\345\0\372\0\345\0\u0119\0\345\0\u011e\0\345\0\u0120\0\345\0\u0121" +
		"\0\345\0\u0122\0\345\0\u0124\0\345\0\u0139\0\345\0\u013c\0\345\0\u013e\0\345\0\u0140" +
		"\0\345\0\u0142\0\345\0\u0143\0\345\0\u0178\0\345\0\u017c\0\345\0\u017f\0\345\0\u0181" +
		"\0\345\0\u01a7\0\345\0\250\0\346\0\320\0\346\0\334\0\346\0\336\0\346\0\350\0\346" +
		"\0\364\0\346\0\366\0\346\0\367\0\346\0\372\0\346\0\u0119\0\346\0\u011e\0\346\0\u0120" +
		"\0\346\0\u0121\0\346\0\u0122\0\346\0\u0124\0\346\0\u0139\0\346\0\u013c\0\346\0\u013e" +
		"\0\346\0\u0140\0\346\0\u0142\0\346\0\u0143\0\346\0\u0178\0\346\0\u017c\0\346\0\u017f" +
		"\0\346\0\u0181\0\346\0\u01a7\0\346\0\250\0\347\0\300\0\u0103\0\301\0\u0104\0\320" +
		"\0\347\0\334\0\347\0\336\0\347\0\350\0\347\0\364\0\347\0\366\0\347\0\367\0\347\0" +
		"\372\0\347\0\u0105\0\u014c\0\u0119\0\347\0\u011e\0\347\0\u0120\0\347\0\u0121\0\347" +
		"\0\u0122\0\347\0\u0124\0\347\0\u0139\0\347\0\u013c\0\347\0\u013e\0\347\0\u0140\0" +
		"\347\0\u0142\0\347\0\u0143\0\347\0\u0178\0\347\0\u017c\0\347\0\u017f\0\347\0\u0181" +
		"\0\347\0\u01a7\0\347\0\u011f\0\u0164\0\u0160\0\u0164\0\u0161\0\u0195\0\u0197\0\u0164" +
		"\0\u0199\0\u0164\0\u011f\0\u0165\0\u0160\0\u0194\0\u0197\0\u01ae\0\u0199\0\u01af" +
		"\0\157\0\207\0\202\0\207\0\250\0\207\0\320\0\207\0\334\0\207\0\336\0\207\0\364\0" +
		"\207\0\366\0\207\0\367\0\207\0\372\0\207\0\u0119\0\207\0\u011e\0\207\0\u0122\0\207" +
		"\0\u0124\0\207\0\u0139\0\207\0\u013c\0\207\0\u013e\0\207\0\u0140\0\207\0\u0142\0" +
		"\207\0\u0143\0\207\0\u0178\0\207\0\u017c\0\207\0\u017f\0\207\0\u0181\0\207\0\u01a7" +
		"\0\207\0\157\0\210\0\202\0\210\0\250\0\350\0\320\0\350\0\334\0\350\0\336\0\350\0" +
		"\364\0\350\0\366\0\350\0\367\0\350\0\372\0\350\0\u0119\0\350\0\u011e\0\350\0\u0122" +
		"\0\350\0\u0124\0\350\0\u0139\0\350\0\u013c\0\350\0\u013e\0\350\0\u0140\0\350\0\u0142" +
		"\0\350\0\u0143\0\350\0\u0178\0\350\0\u017c\0\350\0\u017f\0\350\0\u0181\0\350\0\u01a7" +
		"\0\350\0\157\0\211\0\202\0\211\0\207\0\266\0\250\0\211\0\320\0\211\0\334\0\211\0" +
		"\336\0\211\0\364\0\211\0\366\0\211\0\367\0\211\0\372\0\211\0\u0119\0\211\0\u011e" +
		"\0\211\0\u0122\0\211\0\u0124\0\211\0\u0139\0\211\0\u013c\0\211\0\u013e\0\211\0\u0140" +
		"\0\211\0\u0142\0\211\0\u0143\0\211\0\u0178\0\211\0\u017c\0\211\0\u017f\0\211\0\u0181" +
		"\0\211\0\u01a7\0\211\0\252\0\354\0\201\0\263\0\267\0\375\0\252\0\355\0\u0133\0\u0171" +
		"\0\65\0\102\0\77\0\120\0\100\0\121\0\123\0\102\0\252\0\356\0\317\0\u0115\0\u0114" +
		"\0\u0154\0\u0133\0\356\0\u0157\0\u0115\0\u0159\0\u0115\0\65\0\103\0\65\0\104\0\52" +
		"\0\66\0\325\0\66\0\u0162\0\66\0\65\0\105\0\123\0\140\0\177\0\245\0\244\0\312\0\122" +
		"\0\137\0\u010e\0\u0151\0\u014f\0\u018b\0\u0170\0\u019c\0\317\0\u0116\0\u0157\0\u0116" +
		"\0\u0159\0\u0116\0\317\0\u0117\0\u0157\0\u018f\0\u0159\0\u0190\0\1\0\u01b9\0\6\0" +
		"\62\0\75\0\117\0\131\0\156\0\314\0\u0110\0\6\0\63\0\6\0\64\0\145\0\172\0\176\0\226" +
		"\0\275\0\u0101\0\u0102\0\u0149\0\u0101\0\u0148\0\u0149\0\u0189\0\u0148\0\u0188\0" +
		"\u0189\0\u01aa\0\253\0\361\0\362\0\u0137\0\250\0\351\0\320\0\351\0\334\0\u0125\0" +
		"\336\0\u0128\0\364\0\351\0\366\0\351\0\367\0\351\0\372\0\351\0\u0119\0\u0128\0\u011e" +
		"\0\351\0\u0122\0\351\0\u0124\0\u0169\0\u0139\0\351\0\u013c\0\351\0\u013e\0\351\0" +
		"\u0140\0\351\0\u0142\0\351\0\u0143\0\351\0\u0178\0\351\0\u017c\0\351\0\u017f\0\351" +
		"\0\u0181\0\351\0\u01a7\0\351\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(257,
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0" +
		"\1\0\1\0\2\0\0\0\5\0\4\0\2\0\0\0\6\0\3\0\3\0\3\0\4\0\3\0\3\0\1\0\2\0\1\0\1\0\1\0" +
		"\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\4\0\3\0\3\0\3\0\1\0\10\0\4\0\7\0\3\0\3\0\1\0" +
		"\1\0\1\0\1\0\5\0\3\0\1\0\4\0\4\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\10\0\7\0\7\0\6\0" +
		"\7\0\6\0\6\0\5\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0\4\0\2\0\3\0\2\0\1\0\1\0\1\0\2\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0\4\0\5\0\5\0\6\0\3\0\1\0\3\0\1\0\2\0" +
		"\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\4\0\3\0\3\0\2\0\3\0\2\0\2\0\1\0\1\0\3\0\3\0\3\0" +
		"\5\0\4\0\3\0\2\0\2\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\2\0\1\0\2\0\1\0\2\0" +
		"\1\0\3\0\3\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0\1\0\3\0" +
		"\2\0\1\0\3\0\3\0\2\0\1\0\1\0\4\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0\1\0\1\0" +
		"\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0\1\0\3\0" +
		"\1\0\3\0\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(257,
		"\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120" +
		"\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0\120\0" +
		"\120\0\120\0\120\0\120\0\120\0\121\0\121\0\121\0\121\0\122\0\123\0\123\0\124\0\125" +
		"\0\126\0\127\0\127\0\130\0\130\0\131\0\131\0\132\0\132\0\133\0\134\0\135\0\135\0" +
		"\136\0\136\0\137\0\137\0\140\0\141\0\142\0\142\0\142\0\143\0\143\0\143\0\143\0\143" +
		"\0\144\0\145\0\146\0\146\0\147\0\147\0\150\0\150\0\150\0\150\0\151\0\152\0\152\0" +
		"\152\0\152\0\153\0\154\0\154\0\155\0\155\0\156\0\157\0\160\0\160\0\160\0\161\0\161" +
		"\0\161\0\162\0\162\0\162\0\162\0\162\0\162\0\162\0\162\0\162\0\162\0\162\0\162\0" +
		"\162\0\162\0\162\0\162\0\163\0\163\0\163\0\163\0\163\0\163\0\164\0\165\0\165\0\165" +
		"\0\166\0\166\0\166\0\167\0\167\0\167\0\167\0\170\0\170\0\170\0\170\0\170\0\170\0" +
		"\171\0\171\0\172\0\172\0\173\0\173\0\174\0\174\0\175\0\175\0\176\0\176\0\177\0\200" +
		"\0\200\0\200\0\200\0\200\0\200\0\200\0\200\0\200\0\201\0\202\0\202\0\203\0\203\0" +
		"\203\0\203\0\204\0\205\0\205\0\205\0\206\0\206\0\206\0\206\0\207\0\207\0\210\0\211" +
		"\0\211\0\212\0\213\0\213\0\214\0\214\0\214\0\215\0\215\0\216\0\216\0\216\0\217\0" +
		"\217\0\217\0\217\0\217\0\217\0\217\0\217\0\220\0\221\0\221\0\221\0\221\0\222\0\222" +
		"\0\222\0\223\0\223\0\224\0\225\0\225\0\225\0\226\0\226\0\227\0\230\0\230\0\230\0" +
		"\231\0\232\0\232\0\233\0\233\0\234\0\235\0\235\0\235\0\235\0\236\0\236\0\237\0\237" +
		"\0\240\0\240\0\240\0\240\0\241\0\241\0\241\0\242\0\242\0\242\0\242\0\243\0\243\0" +
		"\244\0\244\0\245\0\245\0\246\0\246\0\247\0\247\0\250\0\250\0\251\0\251\0\252\0\252" +
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
		int import_ = 93;
		int option = 94;
		int symref = 95;
		int symref_noargs = 96;
		int rawType = 97;
		int lexer_parts = 98;
		int lexer_part = 99;
		int named_pattern = 100;
		int start_conditions_scope = 101;
		int start_conditions = 102;
		int stateref_list_Comma_separated = 103;
		int lexeme = 104;
		int lexeme_attrs = 105;
		int lexeme_attribute = 106;
		int brackets_directive = 107;
		int lexer_state_list_Comma_separated = 108;
		int states_clause = 109;
		int stateref = 110;
		int lexer_state = 111;
		int grammar_parts = 112;
		int grammar_part = 113;
		int nonterm = 114;
		int nonterm_type = 115;
		int implements_clause = 116;
		int assoc = 117;
		int param_modifier = 118;
		int template_param = 119;
		int directive = 120;
		int identifier_list_Comma_separated = 121;
		int inputref_list_Comma_separated = 122;
		int inputref = 123;
		int references = 124;
		int references_cs = 125;
		int rule0_list_Or_separated = 126;
		int rules = 127;
		int rule0 = 128;
		int predicate = 129;
		int rhsSuffix = 130;
		int reportClause = 131;
		int reportAs = 132;
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
		int rawTypeopt = 165;
		int iconopt = 166;
		int lexeme_attrsopt = 167;
		int commandopt = 168;
		int implements_clauseopt = 169;
		int rhsSuffixopt = 170;
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
		int rhsOptional_rhsQuantifier = 184;  // rhsOptional : rhsCast '?'
		int rhsCast_rhsAsLiteral = 187;  // rhsCast : rhsPrimary 'as' literal
		int rhsPrimary_rhsSymbol = 188;  // rhsPrimary : symref
		int rhsPrimary_rhsNested = 189;  // rhsPrimary : '(' rules ')'
		int rhsPrimary_rhsList = 190;  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
		int rhsPrimary_rhsList2 = 191;  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
		int rhsPrimary_rhsQuantifier = 192;  // rhsPrimary : rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 193;  // rhsPrimary : rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 194;  // rhsPrimary : '$' '(' rules ')'
		int setPrimary_setSymbol = 197;  // setPrimary : identifier symref
		int setPrimary_setSymbol2 = 198;  // setPrimary : symref
		int setPrimary_setCompound = 199;  // setPrimary : '(' setExpression ')'
		int setPrimary_setComplement = 200;  // setPrimary : '~' setPrimary
		int setExpression_setBinary = 202;  // setExpression : setExpression '|' setExpression
		int setExpression_setBinary2 = 203;  // setExpression : setExpression '&' setExpression
		int nonterm_param_inlineParameter = 214;  // nonterm_param : identifier identifier '=' param_value
		int nonterm_param_inlineParameter2 = 215;  // nonterm_param : identifier identifier
		int predicate_primary_boolPredicate = 230;  // predicate_primary : '!' param_ref
		int predicate_primary_boolPredicate2 = 231;  // predicate_primary : param_ref
		int predicate_primary_comparePredicate = 232;  // predicate_primary : param_ref '==' literal
		int predicate_primary_comparePredicate2 = 233;  // predicate_primary : param_ref '!=' literal
		int predicate_expression_predicateBinary = 235;  // predicate_expression : predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 236;  // predicate_expression : predicate_expression '||' predicate_expression
		int expression_array = 239;  // expression : '[' expression_list_Comma_separated_opt ']'
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
			case 48:  // header : 'language' name '(' name ')' ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 4].value) /* name */,
						((TmaName)tmStack[tmHead - 2].value) /* target */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // header : 'language' name ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 1].value) /* name */,
						null /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // lexer_section : '::' 'lexer' lexer_parts
				tmLeft.value = ((List<ITmaLexerPart>)tmStack[tmHead].value);
				break;
			case 51:  // parser_section : '::' 'parser' grammar_parts
				tmLeft.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value);
				break;
			case 52:  // import_ : 'import' identifier scon ';'
				tmLeft.value = new TmaImport(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 53:  // import_ : 'import' scon ';'
				tmLeft.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // option : identifier '=' expression
				tmLeft.value = new TmaOption(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* key */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 55:  // option : syntax_problem
				tmLeft.value = new TmaOption(
						null /* key */,
						null /* value */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 56:  // symref : identifier symref_args
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((TmaSymrefArgs)tmStack[tmHead].value) /* args */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 57:  // symref : identifier
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // symref_noargs : identifier
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // rawType : code
				tmLeft.value = new TmaRawType(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // lexer_parts : lexer_part
				tmLeft.value = new ArrayList();
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 61:  // lexer_parts : lexer_parts lexer_part
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 62:  // lexer_parts : lexer_parts syntax_problem
				((List<ITmaLexerPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 68:  // named_pattern : identifier '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 69:  // start_conditions_scope : start_conditions '{' lexer_parts '}'
				tmLeft.value = new TmaStartConditionsScope(
						((TmaStartConditions)tmStack[tmHead - 3].value) /* startConditions */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexerParts */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 70:  // start_conditions : '<' '*' '>'
				tmLeft.value = new TmaStartConditions(
						null /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // start_conditions : '<' stateref_list_Comma_separated '>'
				tmLeft.value = new TmaStartConditions(
						((List<TmaStateref>)tmStack[tmHead - 1].value) /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 72:  // stateref_list_Comma_separated : stateref_list_Comma_separated ',' stateref
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 73:  // stateref_list_Comma_separated : stateref
				tmLeft.value = new ArrayList();
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 74:  // lexeme : start_conditions identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
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
			case 75:  // lexeme : start_conditions identifier rawTypeopt ':'
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
			case 76:  // lexeme : identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
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
			case 77:  // lexeme : identifier rawTypeopt ':'
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
			case 78:  // lexeme_attrs : '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 79:  // lexeme_attribute : 'soft'
				tmLeft.value = TmaLexemeAttribute.SOFT;
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
			case 135:  // identifier_list_Comma_separated : identifier_list_Comma_separated ',' identifier
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 136:  // identifier_list_Comma_separated : identifier
				tmLeft.value = new ArrayList();
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 137:  // inputref_list_Comma_separated : inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 138:  // inputref_list_Comma_separated : inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 139:  // inputref : symref_noargs 'no-eoi'
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 140:  // inputref : symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // references : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 142:  // references : references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 143:  // references_cs : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 144:  // references_cs : references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 145:  // rule0_list_Or_separated : rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 146:  // rule0_list_Or_separated : rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 148:  // rule0 : predicate rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // rule0 : predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rule0 : predicate rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rule0 : predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rule0 : rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rule0 : rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rule0 : rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rule0 : rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rule0 : syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						null /* suffix */,
						null /* action */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // predicate : '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 158:  // rhsSuffix : '%' 'prec' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.PREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // rhsSuffix : '%' 'shift' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.SHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // reportClause : '->' identifier '/' identifier reportAs
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* action */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* kind */,
						((TmaReportAs)tmStack[tmHead].value) /* reportAs */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // reportClause : '->' identifier '/' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((TmaIdentifier)tmStack[tmHead].value) /* kind */,
						null /* reportAs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // reportClause : '->' identifier reportAs
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* action */,
						null /* kind */,
						((TmaReportAs)tmStack[tmHead].value) /* reportAs */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // reportClause : '->' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead].value) /* action */,
						null /* kind */,
						null /* reportAs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // reportAs : 'as' identifier
				tmLeft.value = new TmaReportAs(
						((TmaIdentifier)tmStack[tmHead].value) /* identifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // rhsParts : rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 166:  // rhsParts : rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 167:  // rhsParts : rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 172:  // lookahead_predicate_list_And_separated : lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 173:  // lookahead_predicate_list_And_separated : lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 174:  // rhsLookahead : '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // lookahead_predicate : '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 176:  // lookahead_predicate : symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // rhsStateMarker : '.' identifier
				tmLeft.value = new TmaRhsStateMarker(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 179:  // rhsAnnotated : annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 181:  // rhsAssignment : identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 182:  // rhsAssignment : identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 184:  // rhsOptional : rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // rhsCast : rhsPrimary 'as' symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // rhsCast : rhsPrimary 'as' literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 188:  // rhsPrimary : symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 189:  // rhsPrimary : '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 190:  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // rhsPrimary : rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // rhsPrimary : rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 194:  // rhsPrimary : '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // rhsSet : 'set' '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 197:  // setPrimary : identifier symref
				tmLeft.value = new TmaSetSymbol(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 198:  // setPrimary : symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 199:  // setPrimary : '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 200:  // setPrimary : '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // setExpression : setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 203:  // setExpression : setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 204:  // annotation_list : annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 205:  // annotation_list : annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 206:  // annotations : annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 207:  // annotation : '@' identifier '=' expression
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 208:  // annotation : '@' identifier
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 209:  // annotation : '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 210:  // nonterm_param_list_Comma_separated : nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 211:  // nonterm_param_list_Comma_separated : nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 212:  // nonterm_params : '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 214:  // nonterm_param : identifier identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 215:  // nonterm_param : identifier identifier
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 216:  // param_ref : identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 217:  // argument_list_Comma_separated : argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 218:  // argument_list_Comma_separated : argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 221:  // symref_args : '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 222:  // argument : param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 223:  // argument : '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 224:  // argument : '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 225:  // argument : param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 226:  // param_type : 'flag'
				tmLeft.value = TmaParamType.FLAG;
				break;
			case 227:  // param_type : 'param'
				tmLeft.value = TmaParamType.PARAM;
				break;
			case 230:  // predicate_primary : '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 231:  // predicate_primary : param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 232:  // predicate_primary : param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 233:  // predicate_primary : param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 235:  // predicate_expression : predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 236:  // predicate_expression : predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 239:  // expression : '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 241:  // expression_list_Comma_separated : expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 242:  // expression_list_Comma_separated : expression
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
		return (TmaInput1) parse(lexer, 0, 442);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 443);
	}
}
