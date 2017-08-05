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
	private static final int[] tmAction = TMLexer.unpack_int(480,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\375\0\376\0\uffb5\uffff\u0105\0" +
		"\0\0\u0100\0\uffff\uffff\377\0\13\0\1\0\27\0\14\0\17\0\22\0\12\0\16\0\2\0\6\0\30" +
		"\0\35\0\34\0\33\0\7\0\36\0\20\0\23\0\11\0\15\0\21\0\37\0\3\0\5\0\10\0\24\0\4\0\26" +
		"\0\32\0\31\0\25\0\uff63\uffff\360\0\357\0\363\0\u0102\0\ufeef\uffff\ufee7\uffff\ufedb" +
		"\uffff\365\0\ufe93\uffff\uffff\uffff\uffff\uffff\ufe8d\uffff\57\0\uffff\uffff\uffff" +
		"\uffff\u0106\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\40\0\uffff\uffff\362" +
		"\0\ufe45\uffff\uffff\uffff\uffff\uffff\332\0\ufe01\uffff\ufdf9\uffff\uffff\uffff" +
		"\334\0\uffff\uffff\uffff\uffff\47\0\u0103\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufdf3\uffff\44\0\56\0\364\0\uffff\uffff\ufded\uffff\uffff\uffff\371\0\341" +
		"\0\342\0\uffff\uffff\uffff\uffff\337\0\ufde7\uffff\uffff\uffff\54\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\42\0\uffff\uffff\uffff\uffff\361\0\61\0\347\0\340" +
		"\0\346\0\333\0\uffff\uffff\52\0\53\0\uffff\uffff\uffff\uffff\ufde1\uffff\ufdd9\uffff" +
		"\64\0\67\0\73\0\uffff\uffff\70\0\72\0\71\0\55\0\uffff\uffff\374\0\370\0\46\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\120\0\uffff\uffff\101\0\uffff\uffff\62" +
		"\0\u0108\0\uffff\uffff\65\0\66\0\uffff\uffff\ufd8d\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\ufd87\uffff\122\0\125\0\126\0\127\0\ufd3d\uffff\uffff\uffff\317\0\uffff" +
		"\uffff\121\0\uffff\uffff\115\0\uffff\uffff\76\0\uffff\uffff\77\0\63\0\74\0\ufcf1" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\162\0\344\0\uffff\uffff\163\0\uffff\uffff" +
		"\uffff\uffff\157\0\164\0\161\0\345\0\160\0\uffff\uffff\uffff\uffff\uffff\uffff\ufca1" +
		"\uffff\323\0\ufc53\uffff\uffff\uffff\uffff\uffff\ufbf7\uffff\uffff\uffff\153\0\uffff" +
		"\uffff\154\0\155\0\uffff\uffff\uffff\uffff\uffff\uffff\123\0\124\0\316\0\uffff\uffff" +
		"\uffff\uffff\116\0\uffff\uffff\117\0\100\0\ufbef\uffff\75\0\ufb9b\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\ufb4b\uffff\uffff\uffff\202\0\200\0\uffff\uffff\205\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufb43" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\u0104\0\ufae7\uffff\276\0\ufa77\uffff" +
		"\uffff\uffff\212\0\ufa6f\uffff\ufa15\uffff\u0112\0\uf9bb\uffff\uf95f\uffff\252\0" +
		"\251\0\246\0\261\0\263\0\uf8ff\uffff\247\0\uf89d\uffff\uf839\uffff\305\0\uffff\uffff" +
		"\250\0\234\0\uf7d1\uffff\uf7c7\uffff\uf7bb\uffff\uffff\uffff\325\0\327\0\uffff\uffff" +
		"\u0110\0\152\0\uf775\uffff\150\0\uf76d\uffff\uffff\uffff\uf711\uffff\uf6b5\uffff" +
		"\uffff\uffff\uffff\uffff\uf659\uffff\uffff\uffff\uffff\uffff\uffff\uffff\113\0\114" +
		"\0\u010a\0\uf5fd\uffff\uf5ab\uffff\uffff\uffff\uffff\uffff\uffff\uffff\203\0\172" +
		"\0\uffff\uffff\173\0\uffff\uffff\171\0\206\0\uffff\uffff\uffff\uffff\170\0\321\0" +
		"\uffff\uffff\uffff\uffff\260\0\uffff\uffff\uf557\uffff\354\0\uffff\uffff\uffff\uffff" +
		"\uf54b\uffff\uffff\uffff\257\0\uffff\uffff\254\0\uf4ef\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uf493\uffff\147\0\uf435\uffff\uf3db\uffff\uf37f\uffff\uf375" +
		"\uffff\uffff\uffff\uf319\uffff\uf30f\uffff\245\0\uf2af\uffff\uffff\uffff\267\0\uffff" +
		"\uffff\302\0\303\0\236\0\262\0\232\0\uffff\uffff\uf2a5\uffff\uffff\uffff\326\0\207" +
		"\0\uf29d\uffff\151\0\uffff\uffff\uf295\uffff\uffff\uffff\uffff\uffff\uf239\uffff" +
		"\uffff\uffff\uf1dd\uffff\uffff\uffff\uf181\uffff\uffff\uffff\uf125\uffff\uf0c9\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\u010c\0\uf06d\uffff\uf01d\uffff\174\0\175\0" +
		"\uffff\uffff\201\0\177\0\uffff\uffff\166\0\uffff\uffff\237\0\240\0\350\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\235\0\uffff\uffff\277\0\uffff\uffff\256\0\255\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uefcb\uffff\310\0\313\0\uffff\uffff\uef81\uffff" +
		"\264\0\uef15\uffff\265\0\211\0\ueead\uffff\uee51\uffff\uee47\uffff\222\0\uee3d\uffff" +
		"\226\0\230\0\uee33\uffff\271\0\272\0\241\0\uffff\uffff\324\0\uffff\uffff\145\0\uffff" +
		"\uffff\146\0\143\0\uffff\uffff\uedd3\uffff\uffff\uffff\137\0\uffff\uffff\ued77\uffff" +
		"\uffff\uffff\uffff\uffff\ued1b\uffff\uffff\uffff\uecbf\uffff\110\0\112\0\107\0\111" +
		"\0\uffff\uffff\u010e\0\104\0\uec63\uffff\176\0\uffff\uffff\167\0\352\0\353\0\uec13" +
		"\uffff\uec0b\uffff\uffff\uffff\253\0\304\0\uffff\uffff\312\0\307\0\uffff\uffff\306" +
		"\0\uffff\uffff\uec03\uffff\216\0\220\0\224\0\330\0\210\0\144\0\141\0\uffff\uffff" +
		"\142\0\135\0\uffff\uffff\136\0\133\0\uffff\uffff\uebf9\uffff\uffff\uffff\106\0\102" +
		"\0\165\0\uffff\uffff\311\0\ueb9d\uffff\ueb95\uffff\214\0\140\0\134\0\131\0\uffff" +
		"\uffff\132\0\301\0\300\0\130\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(5232,
		"\7\0\41\0\44\0\41\0\45\0\41\0\51\0\41\0\56\0\41\0\57\0\41\0\60\0\41\0\61\0\41\0\62" +
		"\0\41\0\63\0\41\0\64\0\41\0\65\0\41\0\66\0\41\0\67\0\41\0\70\0\41\0\71\0\41\0\72" +
		"\0\41\0\73\0\41\0\74\0\41\0\75\0\41\0\76\0\41\0\77\0\41\0\100\0\41\0\101\0\41\0\102" +
		"\0\41\0\103\0\41\0\104\0\41\0\105\0\41\0\106\0\41\0\107\0\41\0\110\0\41\0\111\0\41" +
		"\0\112\0\41\0\113\0\41\0\114\0\41\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\2\0\uffff" +
		"\uffff\21\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\47\0\uffff\uffff\53\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\22\0\367\0\uffff\uffff" +
		"\ufffe\uffff\30\0\uffff\uffff\0\0\60\0\6\0\60\0\7\0\60\0\10\0\60\0\15\0\60\0\16\0" +
		"\60\0\17\0\60\0\20\0\60\0\22\0\60\0\23\0\60\0\24\0\60\0\25\0\60\0\26\0\60\0\32\0" +
		"\60\0\33\0\60\0\35\0\60\0\40\0\60\0\42\0\60\0\43\0\60\0\44\0\60\0\45\0\60\0\46\0" +
		"\60\0\52\0\60\0\54\0\60\0\56\0\60\0\57\0\60\0\60\0\60\0\61\0\60\0\62\0\60\0\63\0" +
		"\60\0\64\0\60\0\65\0\60\0\66\0\60\0\67\0\60\0\70\0\60\0\71\0\60\0\72\0\60\0\73\0" +
		"\60\0\74\0\60\0\75\0\60\0\76\0\60\0\77\0\60\0\100\0\60\0\101\0\60\0\102\0\60\0\103" +
		"\0\60\0\104\0\60\0\105\0\60\0\106\0\60\0\107\0\60\0\110\0\60\0\111\0\60\0\112\0\60" +
		"\0\113\0\60\0\114\0\60\0\115\0\60\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\71\0" +
		"\uffff\uffff\15\0\u0107\0\uffff\uffff\ufffe\uffff\16\0\uffff\uffff\15\0\u0101\0\23" +
		"\0\u0101\0\26\0\u0101\0\71\0\u0101\0\uffff\uffff\ufffe\uffff\51\0\uffff\uffff\7\0" +
		"\45\0\44\0\45\0\45\0\45\0\56\0\45\0\57\0\45\0\60\0\45\0\61\0\45\0\62\0\45\0\63\0" +
		"\45\0\64\0\45\0\65\0\45\0\66\0\45\0\67\0\45\0\70\0\45\0\71\0\45\0\72\0\45\0\73\0" +
		"\45\0\74\0\45\0\75\0\45\0\76\0\45\0\77\0\45\0\100\0\45\0\101\0\45\0\102\0\45\0\103" +
		"\0\45\0\104\0\45\0\105\0\45\0\106\0\45\0\107\0\45\0\110\0\45\0\111\0\45\0\112\0\45" +
		"\0\113\0\45\0\114\0\45\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\22\0\366\0\uffff" +
		"\uffff\ufffe\uffff\33\0\uffff\uffff\37\0\uffff\uffff\45\0\uffff\uffff\56\0\uffff" +
		"\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff" +
		"\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff" +
		"\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff" +
		"\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102" +
		"\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff" +
		"\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113" +
		"\0\uffff\uffff\114\0\uffff\uffff\31\0\336\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\26\0\373\0\uffff\uffff\ufffe\uffff\20" +
		"\0\uffff\uffff\17\0\343\0\31\0\343\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\31" +
		"\0\335\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\0\0\43\0\uffff\uffff\ufffe\uffff" +
		"\17\0\uffff\uffff\26\0\372\0\uffff\uffff\ufffe\uffff\71\0\uffff\uffff\15\0\u0107" +
		"\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\115\0\uffff\uffff\20\0\u0109\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\0\0\50\0\7\0\50\0\uffff\uffff\ufffe\uffff" +
		"\115\0\uffff\uffff\20\0\u0109\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\43\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\0" +
		"\0\51\0\uffff\uffff\ufffe\uffff\43\0\uffff\uffff\20\0\320\0\23\0\320\0\42\0\320\0" +
		"\45\0\320\0\54\0\320\0\56\0\320\0\57\0\320\0\60\0\320\0\61\0\320\0\62\0\320\0\63" +
		"\0\320\0\64\0\320\0\65\0\320\0\66\0\320\0\67\0\320\0\70\0\320\0\71\0\320\0\72\0\320" +
		"\0\73\0\320\0\74\0\320\0\75\0\320\0\76\0\320\0\77\0\320\0\100\0\320\0\101\0\320\0" +
		"\102\0\320\0\103\0\320\0\104\0\320\0\105\0\320\0\106\0\320\0\107\0\320\0\110\0\320" +
		"\0\111\0\320\0\112\0\320\0\113\0\320\0\114\0\320\0\uffff\uffff\ufffe\uffff\117\0" +
		"\uffff\uffff\0\0\105\0\6\0\105\0\7\0\105\0\27\0\105\0\30\0\105\0\44\0\105\0\45\0" +
		"\105\0\56\0\105\0\57\0\105\0\60\0\105\0\61\0\105\0\62\0\105\0\63\0\105\0\64\0\105" +
		"\0\65\0\105\0\66\0\105\0\67\0\105\0\70\0\105\0\71\0\105\0\72\0\105\0\73\0\105\0\74" +
		"\0\105\0\75\0\105\0\76\0\105\0\77\0\105\0\100\0\105\0\101\0\105\0\102\0\105\0\103" +
		"\0\105\0\104\0\105\0\105\0\105\0\106\0\105\0\107\0\105\0\110\0\105\0\111\0\105\0" +
		"\112\0\105\0\113\0\105\0\114\0\105\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\20" +
		"\0\322\0\23\0\322\0\42\0\322\0\43\0\322\0\45\0\322\0\54\0\322\0\56\0\322\0\57\0\322" +
		"\0\60\0\322\0\61\0\322\0\62\0\322\0\63\0\322\0\64\0\322\0\65\0\322\0\66\0\322\0\67" +
		"\0\322\0\70\0\322\0\71\0\322\0\72\0\322\0\73\0\322\0\74\0\322\0\75\0\322\0\76\0\322" +
		"\0\77\0\322\0\100\0\322\0\101\0\322\0\102\0\322\0\103\0\322\0\104\0\322\0\105\0\322" +
		"\0\106\0\322\0\107\0\322\0\110\0\322\0\111\0\322\0\112\0\322\0\113\0\322\0\114\0" +
		"\322\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\50\0\uffff" +
		"\uffff\20\0\u0111\0\25\0\u0111\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0\0\u010b" +
		"\0\6\0\u010b\0\7\0\u010b\0\23\0\u010b\0\27\0\u010b\0\30\0\u010b\0\44\0\u010b\0\45" +
		"\0\u010b\0\56\0\u010b\0\57\0\u010b\0\60\0\u010b\0\61\0\u010b\0\62\0\u010b\0\63\0" +
		"\u010b\0\64\0\u010b\0\65\0\u010b\0\66\0\u010b\0\67\0\u010b\0\70\0\u010b\0\71\0\u010b" +
		"\0\72\0\u010b\0\73\0\u010b\0\74\0\u010b\0\75\0\u010b\0\76\0\u010b\0\77\0\u010b\0" +
		"\100\0\u010b\0\101\0\u010b\0\102\0\u010b\0\103\0\u010b\0\104\0\u010b\0\105\0\u010b" +
		"\0\106\0\u010b\0\107\0\u010b\0\110\0\u010b\0\111\0\u010b\0\112\0\u010b\0\113\0\u010b" +
		"\0\114\0\u010b\0\115\0\u010b\0\uffff\uffff\ufffe\uffff\117\0\uffff\uffff\0\0\103" +
		"\0\6\0\103\0\7\0\103\0\27\0\103\0\30\0\103\0\44\0\103\0\45\0\103\0\56\0\103\0\57" +
		"\0\103\0\60\0\103\0\61\0\103\0\62\0\103\0\63\0\103\0\64\0\103\0\65\0\103\0\66\0\103" +
		"\0\67\0\103\0\70\0\103\0\71\0\103\0\72\0\103\0\73\0\103\0\74\0\103\0\75\0\103\0\76" +
		"\0\103\0\77\0\103\0\100\0\103\0\101\0\103\0\102\0\103\0\103\0\103\0\104\0\103\0\105" +
		"\0\103\0\106\0\103\0\107\0\103\0\110\0\103\0\111\0\103\0\112\0\103\0\113\0\103\0" +
		"\114\0\103\0\uffff\uffff\ufffe\uffff\77\0\uffff\uffff\15\0\204\0\17\0\204\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff" +
		"\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0113\0\25\0\u0113\0\26\0\u0113\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\20" +
		"\0\uffff\uffff\30\0\uffff\uffff\34\0\uffff\uffff\6\0\60\0\10\0\60\0\15\0\60\0\16" +
		"\0\60\0\23\0\60\0\24\0\60\0\25\0\60\0\26\0\60\0\32\0\60\0\33\0\60\0\35\0\60\0\40" +
		"\0\60\0\42\0\60\0\43\0\60\0\44\0\60\0\45\0\60\0\46\0\60\0\52\0\60\0\54\0\60\0\56" +
		"\0\60\0\57\0\60\0\60\0\60\0\61\0\60\0\62\0\60\0\63\0\60\0\64\0\60\0\65\0\60\0\66" +
		"\0\60\0\67\0\60\0\70\0\60\0\71\0\60\0\72\0\60\0\73\0\60\0\74\0\60\0\75\0\60\0\76" +
		"\0\60\0\77\0\60\0\100\0\60\0\101\0\60\0\102\0\60\0\103\0\60\0\104\0\60\0\105\0\60" +
		"\0\106\0\60\0\107\0\60\0\110\0\60\0\111\0\60\0\112\0\60\0\113\0\60\0\114\0\60\0\115" +
		"\0\60\0\uffff\uffff\ufffe\uffff\10\0\uffff\uffff\15\0\213\0\26\0\213\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42" +
		"\0\uffff\uffff\43\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0" +
		"\26\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff" +
		"\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff" +
		"\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff" +
		"\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff" +
		"\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0" +
		"\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff" +
		"\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113" +
		"\0\25\0\u0113\0\26\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\26\0\u0113\0\uffff\uffff\ufffe\uffff" +
		"\40\0\uffff\uffff\6\0\243\0\10\0\243\0\15\0\243\0\16\0\243\0\23\0\243\0\24\0\243" +
		"\0\25\0\243\0\26\0\243\0\42\0\243\0\43\0\243\0\44\0\243\0\45\0\243\0\52\0\243\0\54" +
		"\0\243\0\56\0\243\0\57\0\243\0\60\0\243\0\61\0\243\0\62\0\243\0\63\0\243\0\64\0\243" +
		"\0\65\0\243\0\66\0\243\0\67\0\243\0\70\0\243\0\71\0\243\0\72\0\243\0\73\0\243\0\74" +
		"\0\243\0\75\0\243\0\76\0\243\0\77\0\243\0\100\0\243\0\101\0\243\0\102\0\243\0\103" +
		"\0\243\0\104\0\243\0\105\0\243\0\106\0\243\0\107\0\243\0\110\0\243\0\111\0\243\0" +
		"\112\0\243\0\113\0\243\0\114\0\243\0\115\0\243\0\uffff\uffff\ufffe\uffff\35\0\uffff" +
		"\uffff\6\0\266\0\10\0\266\0\15\0\266\0\16\0\266\0\23\0\266\0\24\0\266\0\25\0\266" +
		"\0\26\0\266\0\40\0\266\0\42\0\266\0\43\0\266\0\44\0\266\0\45\0\266\0\52\0\266\0\54" +
		"\0\266\0\56\0\266\0\57\0\266\0\60\0\266\0\61\0\266\0\62\0\266\0\63\0\266\0\64\0\266" +
		"\0\65\0\266\0\66\0\266\0\67\0\266\0\70\0\266\0\71\0\266\0\72\0\266\0\73\0\266\0\74" +
		"\0\266\0\75\0\266\0\76\0\266\0\77\0\266\0\100\0\266\0\101\0\266\0\102\0\266\0\103" +
		"\0\266\0\104\0\266\0\105\0\266\0\106\0\266\0\107\0\266\0\110\0\266\0\111\0\266\0" +
		"\112\0\266\0\113\0\266\0\114\0\266\0\115\0\266\0\uffff\uffff\ufffe\uffff\46\0\uffff" +
		"\uffff\6\0\270\0\10\0\270\0\15\0\270\0\16\0\270\0\23\0\270\0\24\0\270\0\25\0\270" +
		"\0\26\0\270\0\35\0\270\0\40\0\270\0\42\0\270\0\43\0\270\0\44\0\270\0\45\0\270\0\52" +
		"\0\270\0\54\0\270\0\56\0\270\0\57\0\270\0\60\0\270\0\61\0\270\0\62\0\270\0\63\0\270" +
		"\0\64\0\270\0\65\0\270\0\66\0\270\0\67\0\270\0\70\0\270\0\71\0\270\0\72\0\270\0\73" +
		"\0\270\0\74\0\270\0\75\0\270\0\76\0\270\0\77\0\270\0\100\0\270\0\101\0\270\0\102" +
		"\0\270\0\103\0\270\0\104\0\270\0\105\0\270\0\106\0\270\0\107\0\270\0\110\0\270\0" +
		"\111\0\270\0\112\0\270\0\113\0\270\0\114\0\270\0\115\0\270\0\uffff\uffff\ufffe\uffff" +
		"\32\0\uffff\uffff\33\0\uffff\uffff\6\0\274\0\10\0\274\0\15\0\274\0\16\0\274\0\23" +
		"\0\274\0\24\0\274\0\25\0\274\0\26\0\274\0\35\0\274\0\40\0\274\0\42\0\274\0\43\0\274" +
		"\0\44\0\274\0\45\0\274\0\46\0\274\0\52\0\274\0\54\0\274\0\56\0\274\0\57\0\274\0\60" +
		"\0\274\0\61\0\274\0\62\0\274\0\63\0\274\0\64\0\274\0\65\0\274\0\66\0\274\0\67\0\274" +
		"\0\70\0\274\0\71\0\274\0\72\0\274\0\73\0\274\0\74\0\274\0\75\0\274\0\76\0\274\0\77" +
		"\0\274\0\100\0\274\0\101\0\274\0\102\0\274\0\103\0\274\0\104\0\274\0\105\0\274\0" +
		"\106\0\274\0\107\0\274\0\110\0\274\0\111\0\274\0\112\0\274\0\113\0\274\0\114\0\274" +
		"\0\115\0\274\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\233\0\15\0\233\0\26" +
		"\0\233\0\uffff\uffff\ufffe\uffff\120\0\uffff\uffff\10\0\242\0\15\0\242\0\20\0\242" +
		"\0\26\0\242\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\17\0\332\0\31\0\332\0\uffff\uffff\ufffe\uffff\50\0\uffff\uffff\20" +
		"\0\u0111\0\25\0\u0111\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff" +
		"\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff" +
		"\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff" +
		"\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff" +
		"\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0" +
		"\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff" +
		"\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113" +
		"\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115" +
		"\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\6\0" +
		"\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113" +
		"\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0\u010d\0\6\0\u010d\0" +
		"\7\0\u010d\0\27\0\u010d\0\30\0\u010d\0\44\0\u010d\0\45\0\u010d\0\56\0\u010d\0\57" +
		"\0\u010d\0\60\0\u010d\0\61\0\u010d\0\62\0\u010d\0\63\0\u010d\0\64\0\u010d\0\65\0" +
		"\u010d\0\66\0\u010d\0\67\0\u010d\0\70\0\u010d\0\71\0\u010d\0\72\0\u010d\0\73\0\u010d" +
		"\0\74\0\u010d\0\75\0\u010d\0\76\0\u010d\0\77\0\u010d\0\100\0\u010d\0\101\0\u010d" +
		"\0\102\0\u010d\0\103\0\u010d\0\104\0\u010d\0\105\0\u010d\0\106\0\u010d\0\107\0\u010d" +
		"\0\110\0\u010d\0\111\0\u010d\0\112\0\u010d\0\113\0\u010d\0\114\0\u010d\0\115\0\u010d" +
		"\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0\0\u010b\0\6\0\u010b\0\7\0\u010b\0\23" +
		"\0\u010b\0\27\0\u010b\0\30\0\u010b\0\44\0\u010b\0\45\0\u010b\0\56\0\u010b\0\57\0" +
		"\u010b\0\60\0\u010b\0\61\0\u010b\0\62\0\u010b\0\63\0\u010b\0\64\0\u010b\0\65\0\u010b" +
		"\0\66\0\u010b\0\67\0\u010b\0\70\0\u010b\0\71\0\u010b\0\72\0\u010b\0\73\0\u010b\0" +
		"\74\0\u010b\0\75\0\u010b\0\76\0\u010b\0\77\0\u010b\0\100\0\u010b\0\101\0\u010b\0" +
		"\102\0\u010b\0\103\0\u010b\0\104\0\u010b\0\105\0\u010b\0\106\0\u010b\0\107\0\u010b" +
		"\0\110\0\u010b\0\111\0\u010b\0\112\0\u010b\0\113\0\u010b\0\114\0\u010b\0\115\0\u010b" +
		"\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff\14\0\uffff\uffff\11\0\351\0\22\0\351" +
		"\0\41\0\351\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\52\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115" +
		"\0\uffff\uffff\10\0\u0113\0\25\0\u0113\0\26\0\u0113\0\uffff\uffff\ufffe\uffff\6\0" +
		"\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\25\0\u0113" +
		"\0\26\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff" +
		"\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff" +
		"\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff" +
		"\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff" +
		"\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104" +
		"\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff" +
		"\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115" +
		"\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\26\0\u0113\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff" +
		"\uffff\43\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\26\0\u0113" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0" +
		"\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0113\0\15\0\u0113\0\25\0\u0113\0\26\0\u0113\0\uffff\uffff\ufffe\uffff\25\0\uffff" +
		"\uffff\10\0\223\0\15\0\223\0\26\0\223\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16" +
		"\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\26\0\u0113\0\uffff" +
		"\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\227\0\15\0\227\0\26\0\227\0\uffff\uffff" +
		"\ufffe\uffff\40\0\uffff\uffff\6\0\244\0\10\0\244\0\15\0\244\0\16\0\244\0\23\0\244" +
		"\0\24\0\244\0\25\0\244\0\26\0\244\0\42\0\244\0\43\0\244\0\44\0\244\0\45\0\244\0\52" +
		"\0\244\0\54\0\244\0\56\0\244\0\57\0\244\0\60\0\244\0\61\0\244\0\62\0\244\0\63\0\244" +
		"\0\64\0\244\0\65\0\244\0\66\0\244\0\67\0\244\0\70\0\244\0\71\0\244\0\72\0\244\0\73" +
		"\0\244\0\74\0\244\0\75\0\244\0\76\0\244\0\77\0\244\0\100\0\244\0\101\0\244\0\102" +
		"\0\244\0\103\0\244\0\104\0\244\0\105\0\244\0\106\0\244\0\107\0\244\0\110\0\244\0" +
		"\111\0\244\0\112\0\244\0\113\0\244\0\114\0\244\0\115\0\244\0\uffff\uffff\ufffe\uffff" +
		"\25\0\uffff\uffff\10\0\231\0\15\0\231\0\26\0\231\0\uffff\uffff\ufffe\uffff\12\0\uffff" +
		"\uffff\17\0\331\0\31\0\331\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\20\0\156\0" +
		"\25\0\156\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0" +
		"\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\0\0\u010f\0\6\0\u010f\0\7\0\u010f\0\27" +
		"\0\u010f\0\30\0\u010f\0\44\0\u010f\0\45\0\u010f\0\56\0\u010f\0\57\0\u010f\0\60\0" +
		"\u010f\0\61\0\u010f\0\62\0\u010f\0\63\0\u010f\0\64\0\u010f\0\65\0\u010f\0\66\0\u010f" +
		"\0\67\0\u010f\0\70\0\u010f\0\71\0\u010f\0\72\0\u010f\0\73\0\u010f\0\74\0\u010f\0" +
		"\75\0\u010f\0\76\0\u010f\0\77\0\u010f\0\100\0\u010f\0\101\0\u010f\0\102\0\u010f\0" +
		"\103\0\u010f\0\104\0\u010f\0\105\0\u010f\0\106\0\u010f\0\107\0\u010f\0\110\0\u010f" +
		"\0\111\0\u010f\0\112\0\u010f\0\113\0\u010f\0\114\0\u010f\0\uffff\uffff\ufffe\uffff" +
		"\23\0\uffff\uffff\0\0\u010d\0\6\0\u010d\0\7\0\u010d\0\27\0\u010d\0\30\0\u010d\0\44" +
		"\0\u010d\0\45\0\u010d\0\56\0\u010d\0\57\0\u010d\0\60\0\u010d\0\61\0\u010d\0\62\0" +
		"\u010d\0\63\0\u010d\0\64\0\u010d\0\65\0\u010d\0\66\0\u010d\0\67\0\u010d\0\70\0\u010d" +
		"\0\71\0\u010d\0\72\0\u010d\0\73\0\u010d\0\74\0\u010d\0\75\0\u010d\0\76\0\u010d\0" +
		"\77\0\u010d\0\100\0\u010d\0\101\0\u010d\0\102\0\u010d\0\103\0\u010d\0\104\0\u010d" +
		"\0\105\0\u010d\0\106\0\u010d\0\107\0\u010d\0\110\0\u010d\0\111\0\u010d\0\112\0\u010d" +
		"\0\113\0\u010d\0\114\0\u010d\0\115\0\u010d\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff" +
		"\45\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\60\0\26\0\60\0" +
		"\40\0\60\0\uffff\uffff\ufffe\uffff\20\0\uffff\uffff\30\0\uffff\uffff\6\0\60\0\10" +
		"\0\60\0\15\0\60\0\16\0\60\0\23\0\60\0\24\0\60\0\25\0\60\0\26\0\60\0\32\0\60\0\33" +
		"\0\60\0\35\0\60\0\40\0\60\0\42\0\60\0\43\0\60\0\44\0\60\0\45\0\60\0\46\0\60\0\52" +
		"\0\60\0\54\0\60\0\56\0\60\0\57\0\60\0\60\0\60\0\61\0\60\0\62\0\60\0\63\0\60\0\64" +
		"\0\60\0\65\0\60\0\66\0\60\0\67\0\60\0\70\0\60\0\71\0\60\0\72\0\60\0\73\0\60\0\74" +
		"\0\60\0\75\0\60\0\76\0\60\0\77\0\60\0\100\0\60\0\101\0\60\0\102\0\60\0\103\0\60\0" +
		"\104\0\60\0\105\0\60\0\106\0\60\0\107\0\60\0\110\0\60\0\111\0\60\0\112\0\60\0\113" +
		"\0\60\0\114\0\60\0\115\0\60\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\33\0\uffff" +
		"\uffff\6\0\275\0\10\0\275\0\15\0\275\0\16\0\275\0\23\0\275\0\24\0\275\0\25\0\275" +
		"\0\26\0\275\0\35\0\275\0\40\0\275\0\42\0\275\0\43\0\275\0\44\0\275\0\45\0\275\0\46" +
		"\0\275\0\52\0\275\0\54\0\275\0\56\0\275\0\57\0\275\0\60\0\275\0\61\0\275\0\62\0\275" +
		"\0\63\0\275\0\64\0\275\0\65\0\275\0\66\0\275\0\67\0\275\0\70\0\275\0\71\0\275\0\72" +
		"\0\275\0\73\0\275\0\74\0\275\0\75\0\275\0\76\0\275\0\77\0\275\0\100\0\275\0\101\0" +
		"\275\0\102\0\275\0\103\0\275\0\104\0\275\0\105\0\275\0\106\0\275\0\107\0\275\0\110" +
		"\0\275\0\111\0\275\0\112\0\275\0\113\0\275\0\114\0\275\0\115\0\275\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42" +
		"\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113" +
		"\0\25\0\u0113\0\26\0\u0113\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\217\0" +
		"\15\0\217\0\26\0\217\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\221\0\15\0\221" +
		"\0\26\0\221\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\225\0\15\0\225\0\26\0" +
		"\225\0\uffff\uffff\ufffe\uffff\40\0\273\0\6\0\273\0\10\0\273\0\15\0\273\0\16\0\273" +
		"\0\23\0\273\0\24\0\273\0\25\0\273\0\26\0\273\0\42\0\273\0\43\0\273\0\44\0\273\0\45" +
		"\0\273\0\52\0\273\0\54\0\273\0\56\0\273\0\57\0\273\0\60\0\273\0\61\0\273\0\62\0\273" +
		"\0\63\0\273\0\64\0\273\0\65\0\273\0\66\0\273\0\67\0\273\0\70\0\273\0\71\0\273\0\72" +
		"\0\273\0\73\0\273\0\74\0\273\0\75\0\273\0\76\0\273\0\77\0\273\0\100\0\273\0\101\0" +
		"\273\0\102\0\273\0\103\0\273\0\104\0\273\0\105\0\273\0\106\0\273\0\107\0\273\0\110" +
		"\0\273\0\111\0\273\0\112\0\273\0\113\0\273\0\114\0\273\0\115\0\273\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff" +
		"\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff" +
		"\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff" +
		"\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff" +
		"\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105" +
		"\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff" +
		"\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff" +
		"\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff" +
		"\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff" +
		"\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff" +
		"\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107" +
		"\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff" +
		"\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0" +
		"\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\0\0\u010f\0\6\0\u010f\0\7\0\u010f\0\27" +
		"\0\u010f\0\30\0\u010f\0\44\0\u010f\0\45\0\u010f\0\56\0\u010f\0\57\0\u010f\0\60\0" +
		"\u010f\0\61\0\u010f\0\62\0\u010f\0\63\0\u010f\0\64\0\u010f\0\65\0\u010f\0\66\0\u010f" +
		"\0\67\0\u010f\0\70\0\u010f\0\71\0\u010f\0\72\0\u010f\0\73\0\u010f\0\74\0\u010f\0" +
		"\75\0\u010f\0\76\0\u010f\0\77\0\u010f\0\100\0\u010f\0\101\0\u010f\0\102\0\u010f\0" +
		"\103\0\u010f\0\104\0\u010f\0\105\0\u010f\0\106\0\u010f\0\107\0\u010f\0\110\0\u010f" +
		"\0\111\0\u010f\0\112\0\u010f\0\113\0\u010f\0\114\0\u010f\0\uffff\uffff\ufffe\uffff" +
		"\11\0\356\0\41\0\uffff\uffff\22\0\356\0\uffff\uffff\ufffe\uffff\11\0\355\0\41\0\355" +
		"\0\22\0\355\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10\0\215\0\15\0\215\0\26\0" +
		"\215\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\54\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0113\0\15\0\u0113\0\25\0\u0113\0\uffff\uffff\ufffe\uffff\10\0\314\0" +
		"\40\0\uffff\uffff\26\0\314\0\uffff\uffff\ufffe\uffff\10\0\315\0\40\0\315\0\26\0\315" +
		"\0\uffff\uffff\ufffe\uffff");

	private static final int[] tmGoto = TMLexer.unpack_int(180,
		"\0\0\4\0\42\0\102\0\102\0\102\0\102\0\204\0\210\0\222\0\230\0\250\0\252\0\254\0\356" +
		"\0\u0128\0\u013c\0\u016a\0\u019c\0\u01a0\0\u01fa\0\u0232\0\u0252\0\u0266\0\u0268" +
		"\0\u027c\0\u0284\0\u028c\0\u0296\0\u0298\0\u029a\0\u02a4\0\u02b2\0\u02c2\0\u02c8" +
		"\0\u030a\0\u0348\0\u038e\0\u0464\0\u0466\0\u0480\0\u0484\0\u0486\0\u0488\0\u0494" +
		"\0\u04dc\0\u04f6\0\u05ce\0\u06a6\0\u0788\0\u0860\0\u0938\0\u0a12\0\u0aea\0\u0bc2" +
		"\0\u0ca0\0\u0d78\0\u0e58\0\u0f32\0\u100a\0\u10e2\0\u11ba\0\u1292\0\u136a\0\u1442" +
		"\0\u151a\0\u15f2\0\u16cc\0\u17a4\0\u187c\0\u195a\0\u1a32\0\u1b0a\0\u1be2\0\u1cba" +
		"\0\u1d92\0\u1e70\0\u1f48\0\u1f90\0\u1f92\0\u1f98\0\u1f9a\0\u2070\0\u2072\0\u2074" +
		"\0\u2076\0\u2078\0\u207a\0\u207c\0\u2080\0\u2082\0\u2084\0\u20e0\0\u2108\0\u2114" +
		"\0\u211a\0\u211e\0\u2126\0\u212e\0\u2136\0\u213e\0\u2140\0\u2148\0\u214c\0\u214e" +
		"\0\u2156\0\u215a\0\u2162\0\u2166\0\u216c\0\u216e\0\u2172\0\u2176\0\u217e\0\u2182" +
		"\0\u2184\0\u2186\0\u218a\0\u218e\0\u2190\0\u2192\0\u2196\0\u219a\0\u219c\0\u21c0" +
		"\0\u21e4\0\u220a\0\u2230\0\u2258\0\u228e\0\u22ae\0\u22da\0\u2312\0\u2314\0\u234c" +
		"\0\u2350\0\u2388\0\u23c0\0\u23fc\0\u243c\0\u247c\0\u24b4\0\u24f4\0\u2536\0\u257e" +
		"\0\u2588\0\u2590\0\u25cc\0\u2608\0\u2646\0\u2648\0\u264c\0\u2650\0\u2664\0\u2666" +
		"\0\u2668\0\u2670\0\u2674\0\u2678\0\u2680\0\u2686\0\u268c\0\u2698\0\u269a\0\u269c" +
		"\0\u269e\0\u26a0\0\u26a4\0\u26be\0\u26c4\0\u26ca\0\u2706\0\u274c\0\u2750\0\u2754" +
		"\0\u2758\0\u275c\0\u2760\0\u2764\0\u279a\0");

	private static final int[] tmFromTo = TMLexer.unpack_int(10138,
		"\u01dc\0\u01de\0\u01dd\0\u01df\0\1\0\4\0\6\0\4\0\76\0\117\0\101\0\4\0\120\0\143\0" +
		"\135\0\4\0\145\0\4\0\150\0\4\0\337\0\4\0\u0124\0\4\0\u0146\0\4\0\u016a\0\4\0\u0170" +
		"\0\4\0\u0171\0\4\0\u0191\0\4\0\1\0\5\0\6\0\5\0\101\0\5\0\114\0\141\0\135\0\5\0\145" +
		"\0\5\0\150\0\5\0\320\0\u0116\0\337\0\5\0\u0118\0\u0116\0\u0124\0\5\0\u0146\0\5\0" +
		"\u016a\0\5\0\u0170\0\5\0\u0171\0\5\0\u0191\0\5\0\144\0\163\0\166\0\163\0\177\0\222" +
		"\0\220\0\163\0\225\0\222\0\250\0\163\0\273\0\340\0\343\0\340\0\355\0\340\0\356\0" +
		"\340\0\360\0\340\0\u010a\0\340\0\u010c\0\340\0\u010d\0\340\0\u0110\0\340\0\u012f" +
		"\0\340\0\u0134\0\340\0\u0139\0\340\0\u013b\0\340\0\u013c\0\340\0\u013e\0\340\0\u0154" +
		"\0\340\0\u0157\0\340\0\u0159\0\340\0\u015b\0\340\0\u015d\0\340\0\u015e\0\340\0\u0186" +
		"\0\340\0\u0199\0\340\0\u019d\0\340\0\u01a0\0\340\0\u01a2\0\340\0\u01ca\0\340\0\77" +
		"\0\121\0\123\0\146\0\352\0\u0139\0\u0180\0\u01b8\0\u01b5\0\u01b8\0\u01d1\0\u01b8" +
		"\0\u01d2\0\u01b8\0\u012d\0\u0172\0\u01b0\0\u0172\0\u01b1\0\u0172\0\122\0\145\0\165" +
		"\0\212\0\271\0\337\0\325\0\u011b\0\336\0\u0124\0\350\0\u0136\0\u0123\0\u016a\0\u014d" +
		"\0\u0191\0\u012b\0\u0170\0\u012b\0\u0171\0\74\0\115\0\117\0\142\0\143\0\162\0\160" +
		"\0\202\0\237\0\314\0\241\0\316\0\313\0\u0114\0\327\0\u011d\0\332\0\u011f\0\334\0" +
		"\u0121\0\336\0\u0125\0\353\0\u013a\0\u0119\0\u0165\0\u011a\0\u0166\0\u0123\0\u016b" +
		"\0\u0153\0\u0194\0\u0155\0\u0196\0\u0156\0\u0197\0\u015a\0\u019b\0\u0167\0\u01ab" +
		"\0\u016c\0\u01ad\0\u0195\0\u01c1\0\u0198\0\u01c2\0\u019a\0\u01c4\0\u019c\0\u01c5" +
		"\0\u019e\0\u01c7\0\u019f\0\u01c8\0\u01ac\0\u01ce\0\u01c3\0\u01d4\0\u01c6\0\u01d5" +
		"\0\u01c9\0\u01d6\0\u01cb\0\u01d8\0\u01d7\0\u01db\0\61\0\75\0\273\0\341\0\343\0\341" +
		"\0\355\0\341\0\356\0\341\0\360\0\341\0\u010a\0\341\0\u010c\0\341\0\u010d\0\341\0" +
		"\u0110\0\341\0\u012f\0\341\0\u0134\0\341\0\u0139\0\341\0\u013b\0\341\0\u013c\0\341" +
		"\0\u013e\0\341\0\u0144\0\341\0\u0154\0\341\0\u0157\0\341\0\u0159\0\341\0\u015b\0" +
		"\341\0\u015d\0\341\0\u015e\0\341\0\u0186\0\341\0\u0199\0\341\0\u019d\0\341\0\u01a0" +
		"\0\341\0\u01a2\0\341\0\u01ca\0\341\0\64\0\101\0\110\0\136\0\130\0\151\0\210\0\243" +
		"\0\237\0\315\0\241\0\315\0\327\0\u011e\0\332\0\u0120\0\u0102\0\u014e\0\u0151\0\u0193" +
		"\0\107\0\135\0\127\0\150\0\215\0\247\0\224\0\273\0\251\0\322\0\304\0\u010a\0\305" +
		"\0\u010c\0\306\0\u010d\0\312\0\u0110\0\350\0\u0137\0\374\0\u0149\0\u010b\0\u0154" +
		"\0\u010e\0\u0157\0\u010f\0\u0159\0\u0111\0\u015b\0\u0112\0\u015d\0\u0113\0\u015e" +
		"\0\u0158\0\u0199\0\u015c\0\u019d\0\u015f\0\u01a0\0\u0160\0\u01a2\0\u0181\0\u0137" +
		"\0\u01a1\0\u01ca\0\1\0\6\0\6\0\6\0\101\0\6\0\145\0\6\0\150\0\6\0\273\0\342\0\337" +
		"\0\6\0\343\0\342\0\u010a\0\342\0\u010c\0\342\0\u010d\0\342\0\u0110\0\342\0\u0134" +
		"\0\342\0\u0139\0\342\0\u0154\0\342\0\u0157\0\342\0\u0159\0\342\0\u015b\0\342\0\u015d" +
		"\0\342\0\u015e\0\342\0\u0199\0\342\0\u019d\0\342\0\u01a0\0\342\0\u01a2\0\342\0\u01ca" +
		"\0\342\0\65\0\102\0\u012d\0\u0173\0\60\0\71\0\66\0\103\0\72\0\114\0\273\0\343\0\343" +
		"\0\343\0\345\0\u0134\0\346\0\u0135\0\355\0\343\0\356\0\343\0\360\0\343\0\374\0\343" +
		"\0\u010a\0\343\0\u010c\0\343\0\u010d\0\343\0\u0110\0\343\0\u0117\0\u0161\0\u012f" +
		"\0\343\0\u0134\0\343\0\u0135\0\u017b\0\u0136\0\343\0\u0137\0\343\0\u0138\0\343\0" +
		"\u0139\0\343\0\u013b\0\343\0\u013c\0\343\0\u013e\0\343\0\u013f\0\343\0\u0144\0\343" +
		"\0\u0154\0\343\0\u0157\0\343\0\u0159\0\343\0\u015b\0\343\0\u015d\0\343\0\u015e\0" +
		"\343\0\u0164\0\u0161\0\u017b\0\u017b\0\u017c\0\u017b\0\u0186\0\343\0\u0199\0\343" +
		"\0\u019d\0\343\0\u01a0\0\343\0\u01a2\0\343\0\u01b8\0\u017b\0\u01ba\0\u017b\0\u01ca" +
		"\0\343\0\273\0\344\0\343\0\344\0\355\0\344\0\356\0\344\0\360\0\344\0\u010a\0\344" +
		"\0\u010c\0\344\0\u010d\0\344\0\u0110\0\344\0\u012f\0\344\0\u0134\0\344\0\u0139\0" +
		"\344\0\u013b\0\344\0\u013c\0\344\0\u013e\0\344\0\u0144\0\344\0\u0154\0\344\0\u0157" +
		"\0\344\0\u0159\0\344\0\u015b\0\344\0\u015d\0\344\0\u015e\0\344\0\u0186\0\344\0\u0199" +
		"\0\344\0\u019d\0\344\0\u01a0\0\344\0\u01a2\0\344\0\u01ca\0\344\0\224\0\274\0\304" +
		"\0\274\0\306\0\274\0\312\0\274\0\377\0\274\0\u010e\0\274\0\u0111\0\274\0\u0113\0" +
		"\274\0\u013d\0\274\0\u0140\0\274\0\u0143\0\274\0\u015f\0\274\0\u0187\0\274\0\u0188" +
		"\0\274\0\u018a\0\274\0\u01bb\0\274\0\113\0\140\0\131\0\152\0\141\0\161\0\u012e\0" +
		"\u0175\0\u0132\0\u0178\0\u017a\0\u01b4\0\u0180\0\u01b9\0\u01a7\0\u01cc\0\u01b2\0" +
		"\u01cf\0\u01b5\0\u01d0\0\250\0\321\0\53\0\67\0\144\0\164\0\166\0\164\0\220\0\164" +
		"\0\224\0\275\0\250\0\164\0\312\0\275\0\350\0\67\0\u017d\0\67\0\u0181\0\67\0\111\0" +
		"\137\0\206\0\242\0\210\0\244\0\u0102\0\u014f\0\164\0\206\0\372\0\u0147\0\u0183\0" +
		"\u0147\0\u01cf\0\u01d9\0\67\0\104\0\136\0\104\0\372\0\u0148\0\u0183\0\u0148\0\u01cf" +
		"\0\u01da\0\350\0\u0138\0\367\0\u0145\0\342\0\u012a\0\344\0\u0130\0\u0172\0\u012a" +
		"\0\u0174\0\u012a\0\u0179\0\u0130\0\67\0\105\0\136\0\105\0\u0135\0\u017c\0\u017b\0" +
		"\u017c\0\u017c\0\u017c\0\u01b8\0\u017c\0\u01ba\0\u017c\0\361\0\u0144\0\u0132\0\u0179" +
		"\0\u0141\0\u0144\0\u0180\0\u01ba\0\u018d\0\u0144\0\u01b5\0\u01ba\0\u01d1\0\u01ba" +
		"\0\u01d2\0\u01ba\0\u012d\0\u0174\0\u01b0\0\u0174\0\u01b1\0\u0174\0\273\0\345\0\343" +
		"\0\345\0\355\0\345\0\356\0\345\0\360\0\345\0\374\0\345\0\u010a\0\345\0\u010c\0\345" +
		"\0\u010d\0\345\0\u0110\0\345\0\u012f\0\345\0\u0134\0\345\0\u0136\0\345\0\u0137\0" +
		"\345\0\u0138\0\345\0\u0139\0\345\0\u013b\0\345\0\u013c\0\345\0\u013e\0\345\0\u013f" +
		"\0\345\0\u0144\0\345\0\u0154\0\345\0\u0157\0\345\0\u0159\0\345\0\u015b\0\345\0\u015d" +
		"\0\345\0\u015e\0\345\0\u0186\0\345\0\u0199\0\345\0\u019d\0\345\0\u01a0\0\345\0\u01a2" +
		"\0\345\0\u01ca\0\345\0\177\0\223\0\225\0\223\0\232\0\223\0\273\0\223\0\343\0\223" +
		"\0\355\0\223\0\356\0\223\0\360\0\223\0\u010a\0\223\0\u010c\0\223\0\u010d\0\223\0" +
		"\u0110\0\223\0\u012f\0\223\0\u0134\0\223\0\u0139\0\223\0\u013b\0\223\0\u013c\0\223" +
		"\0\u013e\0\223\0\u0144\0\223\0\u0154\0\223\0\u0157\0\223\0\u0159\0\223\0\u015b\0" +
		"\223\0\u015d\0\223\0\u015e\0\223\0\u0186\0\223\0\u0199\0\223\0\u019d\0\223\0\u01a0" +
		"\0\223\0\u01a2\0\223\0\u01ca\0\223\0\1\0\7\0\6\0\7\0\77\0\7\0\101\0\7\0\145\0\7\0" +
		"\150\0\7\0\166\0\7\0\223\0\7\0\225\0\7\0\250\0\7\0\273\0\7\0\337\0\7\0\343\0\7\0" +
		"\360\0\7\0\u010a\0\7\0\u010c\0\7\0\u010d\0\7\0\u0110\0\7\0\u012f\0\7\0\u0134\0\7" +
		"\0\u0139\0\7\0\u013c\0\7\0\u013e\0\7\0\u0154\0\7\0\u0157\0\7\0\u0159\0\7\0\u015b" +
		"\0\7\0\u015d\0\7\0\u015e\0\7\0\u0186\0\7\0\u0199\0\7\0\u019d\0\7\0\u01a0\0\7\0\u01a2" +
		"\0\7\0\u01ca\0\7\0\1\0\10\0\2\0\10\0\6\0\10\0\12\0\10\0\67\0\10\0\71\0\10\0\75\0" +
		"\10\0\76\0\10\0\77\0\10\0\101\0\10\0\103\0\10\0\104\0\10\0\105\0\10\0\135\0\10\0" +
		"\136\0\10\0\144\0\10\0\145\0\10\0\150\0\10\0\151\0\10\0\164\0\10\0\166\0\10\0\172" +
		"\0\10\0\177\0\10\0\203\0\10\0\204\0\10\0\205\0\10\0\220\0\10\0\223\0\10\0\225\0\10" +
		"\0\233\0\10\0\235\0\10\0\243\0\10\0\250\0\10\0\255\0\10\0\257\0\10\0\260\0\10\0\266" +
		"\0\10\0\270\0\10\0\273\0\10\0\274\0\10\0\275\0\10\0\301\0\10\0\315\0\10\0\334\0\10" +
		"\0\335\0\10\0\337\0\10\0\341\0\10\0\342\0\10\0\343\0\10\0\344\0\10\0\355\0\10\0\356" +
		"\0\10\0\360\0\10\0\374\0\10\0\u0101\0\10\0\u0105\0\10\0\u010a\0\10\0\u010c\0\10\0" +
		"\u010d\0\10\0\u0110\0\10\0\u011e\0\10\0\u0120\0\10\0\u0124\0\10\0\u0127\0\10\0\u0128" +
		"\0\10\0\u012a\0\10\0\u012f\0\10\0\u0130\0\10\0\u0134\0\10\0\u0135\0\10\0\u0136\0" +
		"\10\0\u0137\0\10\0\u0138\0\10\0\u0139\0\10\0\u013b\0\10\0\u013c\0\10\0\u013e\0\10" +
		"\0\u013f\0\10\0\u0144\0\10\0\u0146\0\10\0\u014c\0\10\0\u014e\0\10\0\u0154\0\10\0" +
		"\u0157\0\10\0\u0159\0\10\0\u015b\0\10\0\u015d\0\10\0\u015e\0\10\0\u016a\0\10\0\u0172" +
		"\0\10\0\u0174\0\10\0\u0176\0\10\0\u0179\0\10\0\u017b\0\10\0\u017c\0\10\0\u017d\0" +
		"\10\0\u0186\0\10\0\u0191\0\10\0\u0193\0\10\0\u0199\0\10\0\u019d\0\10\0\u01a0\0\10" +
		"\0\u01a2\0\10\0\u01b2\0\10\0\u01b8\0\10\0\u01ba\0\10\0\u01ca\0\10\0\371\0\u0146\0" +
		"\1\0\11\0\6\0\11\0\101\0\11\0\135\0\11\0\145\0\11\0\150\0\11\0\337\0\11\0\u0124\0" +
		"\11\0\u0146\0\11\0\u016a\0\11\0\u0170\0\11\0\u0171\0\11\0\u0191\0\11\0\276\0\u0105" +
		"\0\u0108\0\u0105\0\62\0\76\0\u012f\0\u0176\0\1\0\12\0\6\0\12\0\101\0\12\0\145\0\12" +
		"\0\150\0\12\0\337\0\12\0\273\0\346\0\323\0\346\0\324\0\346\0\343\0\346\0\355\0\346" +
		"\0\356\0\346\0\360\0\346\0\374\0\346\0\u010a\0\346\0\u010c\0\346\0\u010d\0\346\0" +
		"\u0110\0\346\0\u011b\0\346\0\u012f\0\346\0\u0134\0\346\0\u0136\0\346\0\u0137\0\346" +
		"\0\u0138\0\346\0\u0139\0\346\0\u013b\0\346\0\u013c\0\346\0\u013e\0\346\0\u013f\0" +
		"\346\0\u0144\0\346\0\u0154\0\346\0\u0157\0\346\0\u0159\0\346\0\u015b\0\346\0\u015d" +
		"\0\346\0\u015e\0\346\0\u0186\0\346\0\u0199\0\346\0\u019d\0\346\0\u01a0\0\346\0\u01a2" +
		"\0\346\0\u01ca\0\346\0\1\0\13\0\6\0\13\0\101\0\13\0\135\0\13\0\145\0\13\0\150\0\13" +
		"\0\337\0\13\0\u0124\0\13\0\u0146\0\13\0\u016a\0\13\0\u0170\0\13\0\u0171\0\13\0\u0191" +
		"\0\13\0\1\0\14\0\2\0\14\0\6\0\14\0\12\0\14\0\67\0\14\0\71\0\14\0\75\0\14\0\76\0\14" +
		"\0\77\0\14\0\101\0\14\0\103\0\14\0\104\0\14\0\105\0\14\0\135\0\14\0\136\0\14\0\144" +
		"\0\14\0\145\0\14\0\150\0\14\0\151\0\14\0\164\0\14\0\166\0\14\0\172\0\14\0\177\0\14" +
		"\0\203\0\14\0\204\0\14\0\205\0\14\0\220\0\14\0\222\0\252\0\223\0\14\0\225\0\14\0" +
		"\233\0\14\0\235\0\14\0\243\0\14\0\250\0\14\0\255\0\14\0\257\0\14\0\260\0\14\0\266" +
		"\0\14\0\270\0\14\0\273\0\14\0\274\0\14\0\275\0\14\0\301\0\14\0\315\0\14\0\334\0\14" +
		"\0\335\0\14\0\337\0\14\0\341\0\14\0\342\0\14\0\343\0\14\0\344\0\14\0\355\0\14\0\356" +
		"\0\14\0\360\0\14\0\374\0\14\0\u0101\0\14\0\u0105\0\14\0\u010a\0\14\0\u010c\0\14\0" +
		"\u010d\0\14\0\u0110\0\14\0\u011e\0\14\0\u0120\0\14\0\u0124\0\14\0\u0127\0\14\0\u0128" +
		"\0\14\0\u012a\0\14\0\u012f\0\14\0\u0130\0\14\0\u0134\0\14\0\u0135\0\14\0\u0136\0" +
		"\14\0\u0137\0\14\0\u0138\0\14\0\u0139\0\14\0\u013b\0\14\0\u013c\0\14\0\u013e\0\14" +
		"\0\u013f\0\14\0\u0144\0\14\0\u0146\0\14\0\u014c\0\14\0\u014e\0\14\0\u0154\0\14\0" +
		"\u0157\0\14\0\u0159\0\14\0\u015b\0\14\0\u015d\0\14\0\u015e\0\14\0\u016a\0\14\0\u0172" +
		"\0\14\0\u0174\0\14\0\u0176\0\14\0\u0179\0\14\0\u017b\0\14\0\u017c\0\14\0\u017d\0" +
		"\14\0\u0186\0\14\0\u0191\0\14\0\u0193\0\14\0\u0199\0\14\0\u019d\0\14\0\u01a0\0\14" +
		"\0\u01a2\0\14\0\u01b2\0\14\0\u01b8\0\14\0\u01ba\0\14\0\u01ca\0\14\0\1\0\15\0\2\0" +
		"\15\0\6\0\15\0\12\0\15\0\67\0\15\0\71\0\15\0\75\0\15\0\76\0\15\0\77\0\15\0\101\0" +
		"\15\0\103\0\15\0\104\0\15\0\105\0\15\0\135\0\15\0\136\0\15\0\144\0\15\0\145\0\15" +
		"\0\150\0\15\0\151\0\15\0\163\0\203\0\164\0\15\0\166\0\15\0\172\0\15\0\177\0\15\0" +
		"\203\0\15\0\204\0\15\0\205\0\15\0\220\0\15\0\223\0\15\0\225\0\15\0\233\0\15\0\235" +
		"\0\15\0\243\0\15\0\250\0\15\0\255\0\15\0\257\0\15\0\260\0\15\0\266\0\15\0\270\0\15" +
		"\0\273\0\15\0\274\0\15\0\275\0\15\0\301\0\15\0\315\0\15\0\334\0\15\0\335\0\15\0\337" +
		"\0\15\0\341\0\15\0\342\0\15\0\343\0\15\0\344\0\15\0\355\0\15\0\356\0\15\0\360\0\15" +
		"\0\374\0\15\0\u0101\0\15\0\u0105\0\15\0\u010a\0\15\0\u010c\0\15\0\u010d\0\15\0\u0110" +
		"\0\15\0\u011e\0\15\0\u0120\0\15\0\u0124\0\15\0\u0127\0\15\0\u0128\0\15\0\u012a\0" +
		"\15\0\u012f\0\15\0\u0130\0\15\0\u0134\0\15\0\u0135\0\15\0\u0136\0\15\0\u0137\0\15" +
		"\0\u0138\0\15\0\u0139\0\15\0\u013b\0\15\0\u013c\0\15\0\u013e\0\15\0\u013f\0\15\0" +
		"\u0144\0\15\0\u0146\0\15\0\u014c\0\15\0\u014e\0\15\0\u0154\0\15\0\u0157\0\15\0\u0159" +
		"\0\15\0\u015b\0\15\0\u015d\0\15\0\u015e\0\15\0\u016a\0\15\0\u0172\0\15\0\u0174\0" +
		"\15\0\u0176\0\15\0\u0179\0\15\0\u017b\0\15\0\u017c\0\15\0\u017d\0\15\0\u0186\0\15" +
		"\0\u0191\0\15\0\u0193\0\15\0\u0199\0\15\0\u019d\0\15\0\u01a0\0\15\0\u01a2\0\15\0" +
		"\u01b2\0\15\0\u01b8\0\15\0\u01ba\0\15\0\u01ca\0\15\0\1\0\16\0\2\0\16\0\6\0\16\0\12" +
		"\0\16\0\67\0\16\0\71\0\16\0\75\0\16\0\76\0\16\0\77\0\16\0\101\0\16\0\103\0\16\0\104" +
		"\0\16\0\105\0\16\0\135\0\16\0\136\0\16\0\144\0\16\0\145\0\16\0\150\0\16\0\151\0\16" +
		"\0\164\0\16\0\166\0\16\0\172\0\16\0\177\0\16\0\203\0\16\0\204\0\16\0\205\0\16\0\220" +
		"\0\16\0\223\0\16\0\224\0\276\0\225\0\16\0\233\0\16\0\235\0\16\0\243\0\16\0\250\0" +
		"\16\0\255\0\16\0\257\0\16\0\260\0\16\0\266\0\16\0\270\0\16\0\273\0\16\0\274\0\16" +
		"\0\275\0\16\0\277\0\u0108\0\301\0\16\0\306\0\276\0\312\0\276\0\315\0\16\0\334\0\16" +
		"\0\335\0\16\0\337\0\16\0\341\0\16\0\342\0\16\0\343\0\16\0\344\0\16\0\355\0\16\0\356" +
		"\0\16\0\360\0\16\0\374\0\16\0\u0101\0\16\0\u0105\0\16\0\u010a\0\16\0\u010c\0\16\0" +
		"\u010d\0\16\0\u0110\0\16\0\u0113\0\276\0\u011e\0\16\0\u0120\0\16\0\u0124\0\16\0\u0127" +
		"\0\16\0\u0128\0\16\0\u012a\0\16\0\u012f\0\16\0\u0130\0\16\0\u0134\0\16\0\u0135\0" +
		"\16\0\u0136\0\16\0\u0137\0\16\0\u0138\0\16\0\u0139\0\16\0\u013b\0\16\0\u013c\0\16" +
		"\0\u013e\0\16\0\u013f\0\16\0\u0144\0\16\0\u0146\0\16\0\u014c\0\16\0\u014e\0\16\0" +
		"\u0154\0\16\0\u0157\0\16\0\u0159\0\16\0\u015b\0\16\0\u015d\0\16\0\u015e\0\16\0\u0161" +
		"\0\u01a3\0\u016a\0\16\0\u0172\0\16\0\u0174\0\16\0\u0176\0\16\0\u0179\0\16\0\u017b" +
		"\0\16\0\u017c\0\16\0\u017d\0\16\0\u0186\0\16\0\u0191\0\16\0\u0193\0\16\0\u0199\0" +
		"\16\0\u019d\0\16\0\u01a0\0\16\0\u01a2\0\16\0\u01b2\0\16\0\u01b8\0\16\0\u01ba\0\16" +
		"\0\u01ca\0\16\0\1\0\17\0\2\0\17\0\6\0\17\0\12\0\17\0\67\0\17\0\71\0\17\0\75\0\17" +
		"\0\76\0\17\0\77\0\17\0\101\0\17\0\103\0\17\0\104\0\17\0\105\0\17\0\135\0\17\0\136" +
		"\0\17\0\144\0\17\0\145\0\17\0\150\0\17\0\151\0\17\0\164\0\17\0\166\0\17\0\172\0\17" +
		"\0\177\0\17\0\203\0\17\0\204\0\17\0\205\0\17\0\220\0\17\0\223\0\17\0\225\0\17\0\233" +
		"\0\17\0\235\0\17\0\243\0\17\0\250\0\17\0\252\0\323\0\255\0\17\0\257\0\17\0\260\0" +
		"\17\0\266\0\17\0\270\0\17\0\273\0\17\0\274\0\17\0\275\0\17\0\301\0\17\0\315\0\17" +
		"\0\334\0\17\0\335\0\17\0\337\0\17\0\341\0\17\0\342\0\17\0\343\0\17\0\344\0\17\0\355" +
		"\0\17\0\356\0\17\0\360\0\17\0\374\0\17\0\u0101\0\17\0\u0105\0\17\0\u010a\0\17\0\u010c" +
		"\0\17\0\u010d\0\17\0\u0110\0\17\0\u011e\0\17\0\u0120\0\17\0\u0124\0\17\0\u0127\0" +
		"\17\0\u0128\0\17\0\u012a\0\17\0\u012f\0\17\0\u0130\0\17\0\u0134\0\17\0\u0135\0\17" +
		"\0\u0136\0\17\0\u0137\0\17\0\u0138\0\17\0\u0139\0\17\0\u013b\0\17\0\u013c\0\17\0" +
		"\u013e\0\17\0\u013f\0\17\0\u0144\0\17\0\u0146\0\17\0\u014c\0\17\0\u014e\0\17\0\u0154" +
		"\0\17\0\u0157\0\17\0\u0159\0\17\0\u015b\0\17\0\u015d\0\17\0\u015e\0\17\0\u016a\0" +
		"\17\0\u0172\0\17\0\u0174\0\17\0\u0176\0\17\0\u0179\0\17\0\u017b\0\17\0\u017c\0\17" +
		"\0\u017d\0\17\0\u0186\0\17\0\u0191\0\17\0\u0193\0\17\0\u0199\0\17\0\u019d\0\17\0" +
		"\u01a0\0\17\0\u01a2\0\17\0\u01b2\0\17\0\u01b8\0\17\0\u01ba\0\17\0\u01ca\0\17\0\1" +
		"\0\20\0\2\0\20\0\6\0\20\0\12\0\20\0\67\0\20\0\71\0\20\0\75\0\20\0\76\0\20\0\77\0" +
		"\20\0\101\0\20\0\103\0\20\0\104\0\20\0\105\0\20\0\135\0\20\0\136\0\20\0\144\0\20" +
		"\0\145\0\20\0\150\0\20\0\151\0\20\0\164\0\20\0\166\0\20\0\172\0\20\0\177\0\20\0\203" +
		"\0\20\0\204\0\20\0\205\0\20\0\220\0\20\0\222\0\253\0\223\0\20\0\225\0\20\0\233\0" +
		"\20\0\235\0\20\0\243\0\20\0\250\0\20\0\255\0\20\0\257\0\20\0\260\0\20\0\266\0\20" +
		"\0\270\0\20\0\273\0\20\0\274\0\20\0\275\0\20\0\301\0\20\0\315\0\20\0\334\0\20\0\335" +
		"\0\20\0\337\0\20\0\341\0\20\0\342\0\20\0\343\0\20\0\344\0\20\0\355\0\20\0\356\0\20" +
		"\0\360\0\20\0\374\0\20\0\u0101\0\20\0\u0105\0\20\0\u010a\0\20\0\u010c\0\20\0\u010d" +
		"\0\20\0\u0110\0\20\0\u011e\0\20\0\u0120\0\20\0\u0124\0\20\0\u0127\0\20\0\u0128\0" +
		"\20\0\u012a\0\20\0\u012f\0\20\0\u0130\0\20\0\u0134\0\20\0\u0135\0\20\0\u0136\0\20" +
		"\0\u0137\0\20\0\u0138\0\20\0\u0139\0\20\0\u013b\0\20\0\u013c\0\20\0\u013e\0\20\0" +
		"\u013f\0\20\0\u0144\0\20\0\u0146\0\20\0\u014c\0\20\0\u014e\0\20\0\u0154\0\20\0\u0157" +
		"\0\20\0\u0159\0\20\0\u015b\0\20\0\u015d\0\20\0\u015e\0\20\0\u016a\0\20\0\u0172\0" +
		"\20\0\u0174\0\20\0\u0176\0\20\0\u0179\0\20\0\u017b\0\20\0\u017c\0\20\0\u017d\0\20" +
		"\0\u0186\0\20\0\u0191\0\20\0\u0193\0\20\0\u0199\0\20\0\u019d\0\20\0\u01a0\0\20\0" +
		"\u01a2\0\20\0\u01b2\0\20\0\u01b8\0\20\0\u01ba\0\20\0\u01ca\0\20\0\1\0\21\0\2\0\21" +
		"\0\6\0\21\0\12\0\21\0\67\0\21\0\71\0\21\0\75\0\21\0\76\0\21\0\77\0\21\0\101\0\21" +
		"\0\103\0\21\0\104\0\21\0\105\0\21\0\135\0\21\0\136\0\21\0\144\0\21\0\145\0\21\0\150" +
		"\0\21\0\151\0\21\0\164\0\21\0\166\0\21\0\172\0\21\0\177\0\21\0\203\0\21\0\204\0\21" +
		"\0\205\0\21\0\220\0\21\0\222\0\254\0\223\0\21\0\225\0\21\0\233\0\21\0\235\0\21\0" +
		"\243\0\21\0\250\0\21\0\255\0\21\0\257\0\21\0\260\0\21\0\266\0\21\0\267\0\254\0\270" +
		"\0\21\0\273\0\21\0\274\0\21\0\275\0\21\0\301\0\21\0\315\0\21\0\334\0\21\0\335\0\21" +
		"\0\337\0\21\0\341\0\21\0\342\0\21\0\343\0\21\0\344\0\21\0\355\0\21\0\356\0\21\0\360" +
		"\0\21\0\374\0\21\0\u0101\0\21\0\u0105\0\21\0\u010a\0\21\0\u010c\0\21\0\u010d\0\21" +
		"\0\u0110\0\21\0\u011e\0\21\0\u0120\0\21\0\u0124\0\21\0\u0127\0\21\0\u0128\0\21\0" +
		"\u012a\0\21\0\u012f\0\21\0\u0130\0\21\0\u0134\0\21\0\u0135\0\21\0\u0136\0\21\0\u0137" +
		"\0\21\0\u0138\0\21\0\u0139\0\21\0\u013b\0\21\0\u013c\0\21\0\u013e\0\21\0\u013f\0" +
		"\21\0\u0144\0\21\0\u0146\0\21\0\u014c\0\21\0\u014e\0\21\0\u0154\0\21\0\u0157\0\21" +
		"\0\u0159\0\21\0\u015b\0\21\0\u015d\0\21\0\u015e\0\21\0\u016a\0\21\0\u0172\0\21\0" +
		"\u0174\0\21\0\u0176\0\21\0\u0179\0\21\0\u017b\0\21\0\u017c\0\21\0\u017d\0\21\0\u0186" +
		"\0\21\0\u0191\0\21\0\u0193\0\21\0\u0199\0\21\0\u019d\0\21\0\u01a0\0\21\0\u01a2\0" +
		"\21\0\u01b2\0\21\0\u01b8\0\21\0\u01ba\0\21\0\u01ca\0\21\0\1\0\22\0\2\0\22\0\6\0\22" +
		"\0\12\0\22\0\67\0\22\0\71\0\22\0\75\0\22\0\76\0\22\0\77\0\22\0\101\0\22\0\103\0\22" +
		"\0\104\0\22\0\105\0\22\0\135\0\22\0\136\0\22\0\144\0\22\0\145\0\22\0\150\0\22\0\151" +
		"\0\22\0\164\0\22\0\166\0\22\0\172\0\22\0\177\0\22\0\203\0\22\0\204\0\22\0\205\0\22" +
		"\0\220\0\22\0\222\0\255\0\223\0\22\0\225\0\22\0\233\0\22\0\235\0\22\0\243\0\22\0" +
		"\250\0\22\0\255\0\22\0\257\0\22\0\260\0\22\0\266\0\22\0\270\0\22\0\273\0\22\0\274" +
		"\0\22\0\275\0\22\0\301\0\22\0\315\0\22\0\334\0\22\0\335\0\22\0\337\0\22\0\341\0\22" +
		"\0\342\0\22\0\343\0\22\0\344\0\22\0\355\0\22\0\356\0\22\0\360\0\22\0\374\0\22\0\u0101" +
		"\0\22\0\u0105\0\22\0\u010a\0\22\0\u010c\0\22\0\u010d\0\22\0\u0110\0\22\0\u011e\0" +
		"\22\0\u0120\0\22\0\u0124\0\22\0\u0127\0\22\0\u0128\0\22\0\u012a\0\22\0\u012f\0\22" +
		"\0\u0130\0\22\0\u0134\0\22\0\u0135\0\22\0\u0136\0\22\0\u0137\0\22\0\u0138\0\22\0" +
		"\u0139\0\22\0\u013b\0\22\0\u013c\0\22\0\u013e\0\22\0\u013f\0\22\0\u0144\0\22\0\u0146" +
		"\0\22\0\u014c\0\22\0\u014e\0\22\0\u0154\0\22\0\u0157\0\22\0\u0159\0\22\0\u015b\0" +
		"\22\0\u015d\0\22\0\u015e\0\22\0\u016a\0\22\0\u0172\0\22\0\u0174\0\22\0\u0176\0\22" +
		"\0\u0179\0\22\0\u017b\0\22\0\u017c\0\22\0\u017d\0\22\0\u0186\0\22\0\u0191\0\22\0" +
		"\u0193\0\22\0\u0199\0\22\0\u019d\0\22\0\u01a0\0\22\0\u01a2\0\22\0\u01b2\0\22\0\u01b8" +
		"\0\22\0\u01ba\0\22\0\u01ca\0\22\0\1\0\23\0\2\0\23\0\6\0\23\0\12\0\23\0\67\0\23\0" +
		"\71\0\23\0\75\0\23\0\76\0\23\0\77\0\23\0\101\0\23\0\103\0\23\0\104\0\23\0\105\0\23" +
		"\0\135\0\23\0\136\0\23\0\144\0\23\0\145\0\23\0\150\0\23\0\151\0\23\0\164\0\23\0\166" +
		"\0\23\0\172\0\23\0\177\0\23\0\203\0\23\0\204\0\23\0\205\0\23\0\220\0\23\0\222\0\256" +
		"\0\223\0\23\0\225\0\23\0\233\0\23\0\235\0\23\0\243\0\23\0\250\0\23\0\255\0\23\0\257" +
		"\0\23\0\260\0\23\0\266\0\23\0\270\0\23\0\273\0\23\0\274\0\23\0\275\0\23\0\301\0\23" +
		"\0\315\0\23\0\334\0\23\0\335\0\23\0\337\0\23\0\341\0\23\0\342\0\23\0\343\0\23\0\344" +
		"\0\23\0\355\0\23\0\356\0\23\0\360\0\23\0\374\0\23\0\u0101\0\23\0\u0105\0\23\0\u010a" +
		"\0\23\0\u010c\0\23\0\u010d\0\23\0\u0110\0\23\0\u011e\0\23\0\u0120\0\23\0\u0124\0" +
		"\23\0\u0127\0\23\0\u0128\0\23\0\u012a\0\23\0\u012f\0\23\0\u0130\0\23\0\u0134\0\23" +
		"\0\u0135\0\23\0\u0136\0\23\0\u0137\0\23\0\u0138\0\23\0\u0139\0\23\0\u013b\0\23\0" +
		"\u013c\0\23\0\u013e\0\23\0\u013f\0\23\0\u0144\0\23\0\u0146\0\23\0\u014c\0\23\0\u014e" +
		"\0\23\0\u0154\0\23\0\u0157\0\23\0\u0159\0\23\0\u015b\0\23\0\u015d\0\23\0\u015e\0" +
		"\23\0\u016a\0\23\0\u0172\0\23\0\u0174\0\23\0\u0176\0\23\0\u0179\0\23\0\u017b\0\23" +
		"\0\u017c\0\23\0\u017d\0\23\0\u0186\0\23\0\u0191\0\23\0\u0193\0\23\0\u0199\0\23\0" +
		"\u019d\0\23\0\u01a0\0\23\0\u01a2\0\23\0\u01b2\0\23\0\u01b8\0\23\0\u01ba\0\23\0\u01ca" +
		"\0\23\0\1\0\24\0\2\0\24\0\6\0\24\0\12\0\24\0\67\0\24\0\71\0\24\0\75\0\24\0\76\0\24" +
		"\0\77\0\24\0\101\0\24\0\103\0\24\0\104\0\24\0\105\0\24\0\135\0\24\0\136\0\24\0\144" +
		"\0\24\0\145\0\24\0\150\0\24\0\151\0\24\0\164\0\24\0\166\0\24\0\172\0\24\0\177\0\24" +
		"\0\203\0\24\0\204\0\24\0\205\0\24\0\220\0\24\0\223\0\24\0\224\0\277\0\225\0\24\0" +
		"\233\0\24\0\235\0\24\0\243\0\24\0\250\0\24\0\255\0\24\0\257\0\24\0\260\0\24\0\266" +
		"\0\24\0\270\0\24\0\273\0\24\0\274\0\24\0\275\0\24\0\301\0\24\0\306\0\277\0\312\0" +
		"\277\0\315\0\24\0\334\0\24\0\335\0\24\0\337\0\24\0\341\0\24\0\342\0\24\0\343\0\24" +
		"\0\344\0\24\0\355\0\24\0\356\0\24\0\360\0\24\0\374\0\24\0\u0101\0\24\0\u0105\0\24" +
		"\0\u010a\0\24\0\u010c\0\24\0\u010d\0\24\0\u0110\0\24\0\u0113\0\277\0\u011e\0\24\0" +
		"\u0120\0\24\0\u0124\0\24\0\u0127\0\24\0\u0128\0\24\0\u012a\0\24\0\u012f\0\24\0\u0130" +
		"\0\24\0\u0134\0\24\0\u0135\0\24\0\u0136\0\24\0\u0137\0\24\0\u0138\0\24\0\u0139\0" +
		"\24\0\u013b\0\24\0\u013c\0\24\0\u013e\0\24\0\u013f\0\24\0\u0144\0\24\0\u0146\0\24" +
		"\0\u014c\0\24\0\u014e\0\24\0\u0154\0\24\0\u0157\0\24\0\u0159\0\24\0\u015b\0\24\0" +
		"\u015d\0\24\0\u015e\0\24\0\u016a\0\24\0\u0172\0\24\0\u0174\0\24\0\u0176\0\24\0\u0179" +
		"\0\24\0\u017b\0\24\0\u017c\0\24\0\u017d\0\24\0\u0186\0\24\0\u0191\0\24\0\u0193\0" +
		"\24\0\u0199\0\24\0\u019d\0\24\0\u01a0\0\24\0\u01a2\0\24\0\u01b2\0\24\0\u01b8\0\24" +
		"\0\u01ba\0\24\0\u01ca\0\24\0\1\0\25\0\2\0\25\0\6\0\25\0\12\0\25\0\67\0\25\0\71\0" +
		"\25\0\75\0\25\0\76\0\25\0\77\0\25\0\101\0\25\0\103\0\25\0\104\0\25\0\105\0\25\0\135" +
		"\0\25\0\136\0\25\0\144\0\25\0\145\0\25\0\150\0\25\0\151\0\25\0\164\0\25\0\166\0\25" +
		"\0\172\0\25\0\177\0\25\0\203\0\25\0\204\0\25\0\205\0\25\0\220\0\25\0\222\0\257\0" +
		"\223\0\25\0\225\0\25\0\233\0\25\0\235\0\25\0\243\0\25\0\250\0\25\0\255\0\25\0\257" +
		"\0\25\0\260\0\25\0\266\0\25\0\270\0\25\0\273\0\25\0\274\0\25\0\275\0\25\0\301\0\25" +
		"\0\315\0\25\0\334\0\25\0\335\0\25\0\337\0\25\0\341\0\25\0\342\0\25\0\343\0\25\0\344" +
		"\0\25\0\355\0\25\0\356\0\25\0\360\0\25\0\374\0\25\0\u0101\0\25\0\u0105\0\25\0\u010a" +
		"\0\25\0\u010c\0\25\0\u010d\0\25\0\u0110\0\25\0\u011e\0\25\0\u0120\0\25\0\u0124\0" +
		"\25\0\u0127\0\25\0\u0128\0\25\0\u012a\0\25\0\u012f\0\25\0\u0130\0\25\0\u0134\0\25" +
		"\0\u0135\0\25\0\u0136\0\25\0\u0137\0\25\0\u0138\0\25\0\u0139\0\25\0\u013b\0\25\0" +
		"\u013c\0\25\0\u013e\0\25\0\u013f\0\25\0\u0144\0\25\0\u0146\0\25\0\u014c\0\25\0\u014e" +
		"\0\25\0\u0154\0\25\0\u0157\0\25\0\u0159\0\25\0\u015b\0\25\0\u015d\0\25\0\u015e\0" +
		"\25\0\u016a\0\25\0\u0172\0\25\0\u0174\0\25\0\u0176\0\25\0\u0179\0\25\0\u017b\0\25" +
		"\0\u017c\0\25\0\u017d\0\25\0\u0186\0\25\0\u0191\0\25\0\u0193\0\25\0\u0199\0\25\0" +
		"\u019d\0\25\0\u01a0\0\25\0\u01a2\0\25\0\u01b2\0\25\0\u01b8\0\25\0\u01ba\0\25\0\u01ca" +
		"\0\25\0\1\0\26\0\2\0\26\0\6\0\26\0\12\0\26\0\67\0\26\0\71\0\26\0\75\0\26\0\76\0\26" +
		"\0\77\0\26\0\101\0\26\0\103\0\26\0\104\0\26\0\105\0\26\0\135\0\26\0\136\0\26\0\144" +
		"\0\26\0\145\0\26\0\150\0\26\0\151\0\26\0\164\0\26\0\166\0\26\0\172\0\26\0\177\0\26" +
		"\0\203\0\26\0\204\0\26\0\205\0\26\0\220\0\26\0\222\0\260\0\223\0\26\0\224\0\300\0" +
		"\225\0\26\0\233\0\26\0\235\0\26\0\243\0\26\0\250\0\26\0\255\0\26\0\257\0\26\0\260" +
		"\0\26\0\266\0\26\0\270\0\26\0\273\0\26\0\274\0\26\0\275\0\26\0\301\0\26\0\306\0\300" +
		"\0\312\0\300\0\315\0\26\0\334\0\26\0\335\0\26\0\337\0\26\0\341\0\26\0\342\0\26\0" +
		"\343\0\26\0\344\0\26\0\355\0\26\0\356\0\26\0\360\0\26\0\374\0\26\0\u0101\0\26\0\u0105" +
		"\0\26\0\u010a\0\26\0\u010c\0\26\0\u010d\0\26\0\u0110\0\26\0\u0113\0\300\0\u011e\0" +
		"\26\0\u0120\0\26\0\u0124\0\26\0\u0127\0\26\0\u0128\0\26\0\u012a\0\26\0\u012f\0\26" +
		"\0\u0130\0\26\0\u0134\0\26\0\u0135\0\26\0\u0136\0\26\0\u0137\0\26\0\u0138\0\26\0" +
		"\u0139\0\26\0\u013b\0\26\0\u013c\0\26\0\u013e\0\26\0\u013f\0\26\0\u0144\0\26\0\u0146" +
		"\0\26\0\u014c\0\26\0\u014e\0\26\0\u0154\0\26\0\u0157\0\26\0\u0159\0\26\0\u015b\0" +
		"\26\0\u015d\0\26\0\u015e\0\26\0\u016a\0\26\0\u0172\0\26\0\u0174\0\26\0\u0176\0\26" +
		"\0\u0179\0\26\0\u017b\0\26\0\u017c\0\26\0\u017d\0\26\0\u0186\0\26\0\u0191\0\26\0" +
		"\u0193\0\26\0\u0199\0\26\0\u019d\0\26\0\u01a0\0\26\0\u01a2\0\26\0\u01b2\0\26\0\u01b8" +
		"\0\26\0\u01ba\0\26\0\u01ca\0\26\0\1\0\27\0\2\0\27\0\6\0\27\0\12\0\27\0\60\0\72\0" +
		"\67\0\27\0\71\0\27\0\75\0\27\0\76\0\27\0\77\0\27\0\101\0\27\0\103\0\27\0\104\0\27" +
		"\0\105\0\27\0\135\0\27\0\136\0\27\0\140\0\72\0\144\0\27\0\145\0\27\0\150\0\27\0\151" +
		"\0\27\0\164\0\27\0\166\0\27\0\172\0\27\0\177\0\27\0\203\0\27\0\204\0\27\0\205\0\27" +
		"\0\220\0\27\0\223\0\27\0\225\0\27\0\233\0\27\0\235\0\27\0\243\0\27\0\250\0\27\0\255" +
		"\0\27\0\257\0\27\0\260\0\27\0\266\0\27\0\270\0\27\0\273\0\27\0\274\0\27\0\275\0\27" +
		"\0\301\0\27\0\315\0\27\0\334\0\27\0\335\0\27\0\337\0\27\0\341\0\27\0\342\0\27\0\343" +
		"\0\27\0\344\0\27\0\355\0\27\0\356\0\27\0\360\0\27\0\374\0\27\0\u0101\0\27\0\u0105" +
		"\0\27\0\u010a\0\27\0\u010c\0\27\0\u010d\0\27\0\u0110\0\27\0\u011e\0\27\0\u0120\0" +
		"\27\0\u0124\0\27\0\u0127\0\27\0\u0128\0\27\0\u012a\0\27\0\u012f\0\27\0\u0130\0\27" +
		"\0\u0134\0\27\0\u0135\0\27\0\u0136\0\27\0\u0137\0\27\0\u0138\0\27\0\u0139\0\27\0" +
		"\u013b\0\27\0\u013c\0\27\0\u013e\0\27\0\u013f\0\27\0\u0144\0\27\0\u0146\0\27\0\u014c" +
		"\0\27\0\u014e\0\27\0\u0154\0\27\0\u0157\0\27\0\u0159\0\27\0\u015b\0\27\0\u015d\0" +
		"\27\0\u015e\0\27\0\u016a\0\27\0\u0172\0\27\0\u0174\0\27\0\u0176\0\27\0\u0179\0\27" +
		"\0\u017b\0\27\0\u017c\0\27\0\u017d\0\27\0\u0186\0\27\0\u0191\0\27\0\u0193\0\27\0" +
		"\u0199\0\27\0\u019d\0\27\0\u01a0\0\27\0\u01a2\0\27\0\u01b2\0\27\0\u01b8\0\27\0\u01ba" +
		"\0\27\0\u01ca\0\27\0\0\0\2\0\1\0\30\0\2\0\30\0\6\0\30\0\12\0\30\0\67\0\30\0\71\0" +
		"\30\0\75\0\30\0\76\0\30\0\77\0\30\0\101\0\30\0\103\0\30\0\104\0\30\0\105\0\30\0\135" +
		"\0\30\0\136\0\30\0\144\0\30\0\145\0\30\0\150\0\30\0\151\0\30\0\164\0\30\0\166\0\30" +
		"\0\172\0\30\0\177\0\30\0\203\0\30\0\204\0\30\0\205\0\30\0\220\0\30\0\223\0\30\0\225" +
		"\0\30\0\233\0\30\0\235\0\30\0\243\0\30\0\250\0\30\0\255\0\30\0\257\0\30\0\260\0\30" +
		"\0\266\0\30\0\270\0\30\0\273\0\30\0\274\0\30\0\275\0\30\0\301\0\30\0\315\0\30\0\334" +
		"\0\30\0\335\0\30\0\337\0\30\0\341\0\30\0\342\0\30\0\343\0\30\0\344\0\30\0\355\0\30" +
		"\0\356\0\30\0\360\0\30\0\374\0\30\0\u0101\0\30\0\u0105\0\30\0\u010a\0\30\0\u010c" +
		"\0\30\0\u010d\0\30\0\u0110\0\30\0\u011e\0\30\0\u0120\0\30\0\u0124\0\30\0\u0127\0" +
		"\30\0\u0128\0\30\0\u012a\0\30\0\u012f\0\30\0\u0130\0\30\0\u0134\0\30\0\u0135\0\30" +
		"\0\u0136\0\30\0\u0137\0\30\0\u0138\0\30\0\u0139\0\30\0\u013b\0\30\0\u013c\0\30\0" +
		"\u013e\0\30\0\u013f\0\30\0\u0144\0\30\0\u0146\0\30\0\u014c\0\30\0\u014e\0\30\0\u0154" +
		"\0\30\0\u0157\0\30\0\u0159\0\30\0\u015b\0\30\0\u015d\0\30\0\u015e\0\30\0\u016a\0" +
		"\30\0\u0172\0\30\0\u0174\0\30\0\u0176\0\30\0\u0179\0\30\0\u017b\0\30\0\u017c\0\30" +
		"\0\u017d\0\30\0\u0186\0\30\0\u0191\0\30\0\u0193\0\30\0\u0199\0\30\0\u019d\0\30\0" +
		"\u01a0\0\30\0\u01a2\0\30\0\u01b2\0\30\0\u01b8\0\30\0\u01ba\0\30\0\u01ca\0\30\0\1" +
		"\0\31\0\2\0\31\0\6\0\31\0\12\0\31\0\67\0\31\0\71\0\31\0\75\0\31\0\76\0\31\0\77\0" +
		"\31\0\101\0\31\0\103\0\31\0\104\0\31\0\105\0\31\0\135\0\31\0\136\0\31\0\144\0\31" +
		"\0\145\0\31\0\150\0\31\0\151\0\31\0\164\0\31\0\166\0\31\0\172\0\31\0\177\0\31\0\203" +
		"\0\31\0\204\0\31\0\205\0\31\0\220\0\31\0\223\0\31\0\225\0\31\0\233\0\31\0\235\0\31" +
		"\0\243\0\31\0\250\0\31\0\255\0\31\0\257\0\31\0\260\0\31\0\266\0\31\0\270\0\31\0\273" +
		"\0\31\0\274\0\31\0\275\0\31\0\301\0\31\0\315\0\31\0\334\0\31\0\335\0\31\0\337\0\31" +
		"\0\341\0\31\0\342\0\31\0\343\0\31\0\344\0\31\0\355\0\31\0\356\0\31\0\360\0\31\0\374" +
		"\0\31\0\u0101\0\31\0\u0105\0\31\0\u010a\0\31\0\u010c\0\31\0\u010d\0\31\0\u0110\0" +
		"\31\0\u011e\0\31\0\u0120\0\31\0\u0124\0\31\0\u0127\0\31\0\u0128\0\31\0\u012a\0\31" +
		"\0\u012f\0\31\0\u0130\0\31\0\u0134\0\31\0\u0135\0\31\0\u0136\0\31\0\u0137\0\31\0" +
		"\u0138\0\31\0\u0139\0\31\0\u013b\0\31\0\u013c\0\31\0\u013e\0\31\0\u013f\0\31\0\u0144" +
		"\0\31\0\u0146\0\31\0\u014c\0\31\0\u014e\0\31\0\u0154\0\31\0\u0157\0\31\0\u0159\0" +
		"\31\0\u015b\0\31\0\u015d\0\31\0\u015e\0\31\0\u0161\0\u01a4\0\u016a\0\31\0\u0172\0" +
		"\31\0\u0174\0\31\0\u0176\0\31\0\u0179\0\31\0\u017b\0\31\0\u017c\0\31\0\u017d\0\31" +
		"\0\u0186\0\31\0\u0191\0\31\0\u0193\0\31\0\u0199\0\31\0\u019d\0\31\0\u01a0\0\31\0" +
		"\u01a2\0\31\0\u01b2\0\31\0\u01b8\0\31\0\u01ba\0\31\0\u01ca\0\31\0\1\0\32\0\2\0\32" +
		"\0\6\0\32\0\12\0\32\0\67\0\32\0\71\0\32\0\75\0\32\0\76\0\32\0\77\0\32\0\101\0\32" +
		"\0\103\0\32\0\104\0\32\0\105\0\32\0\135\0\32\0\136\0\32\0\144\0\32\0\145\0\32\0\150" +
		"\0\32\0\151\0\32\0\164\0\32\0\166\0\32\0\172\0\32\0\177\0\32\0\203\0\32\0\204\0\32" +
		"\0\205\0\32\0\220\0\32\0\222\0\261\0\223\0\32\0\225\0\32\0\233\0\32\0\235\0\32\0" +
		"\243\0\32\0\250\0\32\0\255\0\32\0\257\0\32\0\260\0\32\0\266\0\32\0\270\0\32\0\273" +
		"\0\32\0\274\0\32\0\275\0\32\0\301\0\32\0\315\0\32\0\334\0\32\0\335\0\32\0\337\0\32" +
		"\0\341\0\32\0\342\0\32\0\343\0\32\0\344\0\32\0\355\0\32\0\356\0\32\0\360\0\32\0\374" +
		"\0\32\0\u0101\0\32\0\u0105\0\32\0\u010a\0\32\0\u010c\0\32\0\u010d\0\32\0\u0110\0" +
		"\32\0\u011e\0\32\0\u0120\0\32\0\u0124\0\32\0\u0127\0\32\0\u0128\0\32\0\u012a\0\32" +
		"\0\u012f\0\32\0\u0130\0\32\0\u0134\0\32\0\u0135\0\32\0\u0136\0\32\0\u0137\0\32\0" +
		"\u0138\0\32\0\u0139\0\32\0\u013b\0\32\0\u013c\0\32\0\u013e\0\32\0\u013f\0\32\0\u0144" +
		"\0\32\0\u0146\0\32\0\u014c\0\32\0\u014e\0\32\0\u0154\0\32\0\u0157\0\32\0\u0159\0" +
		"\32\0\u015b\0\32\0\u015d\0\32\0\u015e\0\32\0\u016a\0\32\0\u0172\0\32\0\u0174\0\32" +
		"\0\u0176\0\32\0\u0179\0\32\0\u017b\0\32\0\u017c\0\32\0\u017d\0\32\0\u0186\0\32\0" +
		"\u0191\0\32\0\u0193\0\32\0\u0199\0\32\0\u019d\0\32\0\u01a0\0\32\0\u01a2\0\32\0\u01b2" +
		"\0\32\0\u01b8\0\32\0\u01ba\0\32\0\u01ca\0\32\0\1\0\33\0\2\0\33\0\6\0\33\0\12\0\33" +
		"\0\67\0\33\0\71\0\33\0\75\0\33\0\76\0\33\0\77\0\33\0\101\0\33\0\103\0\33\0\104\0" +
		"\33\0\105\0\33\0\121\0\144\0\135\0\33\0\136\0\33\0\144\0\33\0\145\0\33\0\150\0\33" +
		"\0\151\0\33\0\164\0\33\0\166\0\33\0\172\0\33\0\177\0\33\0\203\0\33\0\204\0\33\0\205" +
		"\0\33\0\220\0\33\0\223\0\33\0\225\0\33\0\233\0\33\0\235\0\33\0\243\0\33\0\250\0\33" +
		"\0\255\0\33\0\257\0\33\0\260\0\33\0\266\0\33\0\270\0\33\0\273\0\33\0\274\0\33\0\275" +
		"\0\33\0\301\0\33\0\315\0\33\0\334\0\33\0\335\0\33\0\337\0\33\0\341\0\33\0\342\0\33" +
		"\0\343\0\33\0\344\0\33\0\355\0\33\0\356\0\33\0\360\0\33\0\374\0\33\0\u0101\0\33\0" +
		"\u0105\0\33\0\u010a\0\33\0\u010c\0\33\0\u010d\0\33\0\u0110\0\33\0\u011e\0\33\0\u0120" +
		"\0\33\0\u0124\0\33\0\u0127\0\33\0\u0128\0\33\0\u012a\0\33\0\u012f\0\33\0\u0130\0" +
		"\33\0\u0134\0\33\0\u0135\0\33\0\u0136\0\33\0\u0137\0\33\0\u0138\0\33\0\u0139\0\33" +
		"\0\u013b\0\33\0\u013c\0\33\0\u013e\0\33\0\u013f\0\33\0\u0144\0\33\0\u0146\0\33\0" +
		"\u014c\0\33\0\u014e\0\33\0\u0154\0\33\0\u0157\0\33\0\u0159\0\33\0\u015b\0\33\0\u015d" +
		"\0\33\0\u015e\0\33\0\u016a\0\33\0\u0172\0\33\0\u0174\0\33\0\u0176\0\33\0\u0179\0" +
		"\33\0\u017b\0\33\0\u017c\0\33\0\u017d\0\33\0\u0186\0\33\0\u0191\0\33\0\u0193\0\33" +
		"\0\u0199\0\33\0\u019d\0\33\0\u01a0\0\33\0\u01a2\0\33\0\u01b2\0\33\0\u01b8\0\33\0" +
		"\u01ba\0\33\0\u01ca\0\33\0\1\0\34\0\2\0\34\0\6\0\34\0\12\0\34\0\67\0\34\0\71\0\34" +
		"\0\75\0\34\0\76\0\34\0\77\0\34\0\101\0\34\0\103\0\34\0\104\0\34\0\105\0\34\0\135" +
		"\0\34\0\136\0\34\0\144\0\34\0\145\0\34\0\150\0\34\0\151\0\34\0\164\0\34\0\166\0\34" +
		"\0\172\0\34\0\177\0\34\0\203\0\34\0\204\0\34\0\205\0\34\0\220\0\34\0\222\0\262\0" +
		"\223\0\34\0\225\0\34\0\233\0\34\0\235\0\34\0\243\0\34\0\250\0\34\0\255\0\34\0\257" +
		"\0\34\0\260\0\34\0\266\0\34\0\270\0\34\0\273\0\34\0\274\0\34\0\275\0\34\0\301\0\34" +
		"\0\315\0\34\0\334\0\34\0\335\0\34\0\337\0\34\0\341\0\34\0\342\0\34\0\343\0\34\0\344" +
		"\0\34\0\355\0\34\0\356\0\34\0\360\0\34\0\374\0\34\0\u0101\0\34\0\u0105\0\34\0\u010a" +
		"\0\34\0\u010c\0\34\0\u010d\0\34\0\u0110\0\34\0\u011e\0\34\0\u0120\0\34\0\u0124\0" +
		"\34\0\u0127\0\34\0\u0128\0\34\0\u012a\0\34\0\u012f\0\34\0\u0130\0\34\0\u0134\0\34" +
		"\0\u0135\0\34\0\u0136\0\34\0\u0137\0\34\0\u0138\0\34\0\u0139\0\34\0\u013b\0\34\0" +
		"\u013c\0\34\0\u013e\0\34\0\u013f\0\34\0\u0144\0\34\0\u0146\0\34\0\u014c\0\34\0\u014e" +
		"\0\34\0\u0154\0\34\0\u0157\0\34\0\u0159\0\34\0\u015b\0\34\0\u015d\0\34\0\u015e\0" +
		"\34\0\u016a\0\34\0\u0172\0\34\0\u0174\0\34\0\u0176\0\34\0\u0179\0\34\0\u017b\0\34" +
		"\0\u017c\0\34\0\u017d\0\34\0\u0186\0\34\0\u0191\0\34\0\u0193\0\34\0\u0199\0\34\0" +
		"\u019d\0\34\0\u01a0\0\34\0\u01a2\0\34\0\u01b2\0\34\0\u01b8\0\34\0\u01ba\0\34\0\u01ca" +
		"\0\34\0\1\0\35\0\2\0\35\0\6\0\35\0\12\0\35\0\67\0\35\0\71\0\35\0\75\0\35\0\76\0\35" +
		"\0\77\0\35\0\101\0\35\0\103\0\35\0\104\0\35\0\105\0\35\0\135\0\35\0\136\0\35\0\144" +
		"\0\35\0\145\0\35\0\150\0\35\0\151\0\35\0\164\0\35\0\166\0\35\0\172\0\35\0\177\0\35" +
		"\0\203\0\35\0\204\0\35\0\205\0\35\0\220\0\35\0\223\0\35\0\225\0\35\0\233\0\35\0\235" +
		"\0\35\0\243\0\35\0\250\0\35\0\255\0\35\0\257\0\35\0\260\0\35\0\266\0\35\0\270\0\35" +
		"\0\273\0\35\0\274\0\35\0\275\0\35\0\301\0\35\0\315\0\35\0\326\0\u011c\0\334\0\35" +
		"\0\335\0\35\0\337\0\35\0\341\0\35\0\342\0\35\0\343\0\35\0\344\0\35\0\355\0\35\0\356" +
		"\0\35\0\360\0\35\0\374\0\35\0\u0101\0\35\0\u0105\0\35\0\u010a\0\35\0\u010c\0\35\0" +
		"\u010d\0\35\0\u0110\0\35\0\u011e\0\35\0\u0120\0\35\0\u0124\0\35\0\u0127\0\35\0\u0128" +
		"\0\35\0\u012a\0\35\0\u012f\0\35\0\u0130\0\35\0\u0134\0\35\0\u0135\0\35\0\u0136\0" +
		"\35\0\u0137\0\35\0\u0138\0\35\0\u0139\0\35\0\u013b\0\35\0\u013c\0\35\0\u013e\0\35" +
		"\0\u013f\0\35\0\u0144\0\35\0\u0146\0\35\0\u014c\0\35\0\u014e\0\35\0\u0154\0\35\0" +
		"\u0157\0\35\0\u0159\0\35\0\u015b\0\35\0\u015d\0\35\0\u015e\0\35\0\u016a\0\35\0\u0172" +
		"\0\35\0\u0174\0\35\0\u0176\0\35\0\u0179\0\35\0\u017b\0\35\0\u017c\0\35\0\u017d\0" +
		"\35\0\u0186\0\35\0\u0191\0\35\0\u0193\0\35\0\u0199\0\35\0\u019d\0\35\0\u01a0\0\35" +
		"\0\u01a2\0\35\0\u01b2\0\35\0\u01b8\0\35\0\u01ba\0\35\0\u01ca\0\35\0\1\0\36\0\2\0" +
		"\36\0\6\0\36\0\12\0\36\0\67\0\36\0\71\0\36\0\75\0\36\0\76\0\36\0\77\0\36\0\101\0" +
		"\36\0\103\0\36\0\104\0\36\0\105\0\36\0\135\0\36\0\136\0\36\0\144\0\36\0\145\0\36" +
		"\0\150\0\36\0\151\0\36\0\164\0\36\0\166\0\36\0\172\0\36\0\177\0\36\0\203\0\36\0\204" +
		"\0\36\0\205\0\36\0\220\0\36\0\222\0\263\0\223\0\36\0\225\0\36\0\233\0\36\0\235\0" +
		"\36\0\243\0\36\0\250\0\36\0\255\0\36\0\257\0\36\0\260\0\36\0\266\0\36\0\270\0\36" +
		"\0\273\0\36\0\274\0\36\0\275\0\36\0\301\0\36\0\315\0\36\0\334\0\36\0\335\0\36\0\337" +
		"\0\36\0\341\0\36\0\342\0\36\0\343\0\36\0\344\0\36\0\355\0\36\0\356\0\36\0\360\0\36" +
		"\0\374\0\36\0\u0101\0\36\0\u0105\0\36\0\u010a\0\36\0\u010c\0\36\0\u010d\0\36\0\u0110" +
		"\0\36\0\u011e\0\36\0\u0120\0\36\0\u0124\0\36\0\u0127\0\36\0\u0128\0\36\0\u012a\0" +
		"\36\0\u012f\0\36\0\u0130\0\36\0\u0134\0\36\0\u0135\0\36\0\u0136\0\36\0\u0137\0\36" +
		"\0\u0138\0\36\0\u0139\0\36\0\u013b\0\36\0\u013c\0\36\0\u013e\0\36\0\u013f\0\36\0" +
		"\u0144\0\36\0\u0146\0\36\0\u014c\0\36\0\u014e\0\36\0\u0154\0\36\0\u0157\0\36\0\u0159" +
		"\0\36\0\u015b\0\36\0\u015d\0\36\0\u015e\0\36\0\u016a\0\36\0\u0172\0\36\0\u0174\0" +
		"\36\0\u0176\0\36\0\u0179\0\36\0\u017b\0\36\0\u017c\0\36\0\u017d\0\36\0\u0186\0\36" +
		"\0\u0191\0\36\0\u0193\0\36\0\u0199\0\36\0\u019d\0\36\0\u01a0\0\36\0\u01a2\0\36\0" +
		"\u01b2\0\36\0\u01b8\0\36\0\u01ba\0\36\0\u01ca\0\36\0\1\0\37\0\2\0\37\0\6\0\37\0\12" +
		"\0\37\0\67\0\37\0\71\0\37\0\75\0\37\0\76\0\37\0\77\0\37\0\101\0\37\0\103\0\37\0\104" +
		"\0\37\0\105\0\37\0\135\0\37\0\136\0\37\0\144\0\37\0\145\0\37\0\150\0\37\0\151\0\37" +
		"\0\164\0\37\0\166\0\37\0\172\0\37\0\177\0\37\0\203\0\37\0\204\0\37\0\205\0\37\0\220" +
		"\0\37\0\223\0\37\0\225\0\37\0\233\0\37\0\235\0\37\0\243\0\37\0\250\0\37\0\252\0\324" +
		"\0\255\0\37\0\257\0\37\0\260\0\37\0\266\0\37\0\270\0\37\0\273\0\37\0\274\0\37\0\275" +
		"\0\37\0\301\0\37\0\315\0\37\0\334\0\37\0\335\0\37\0\337\0\37\0\341\0\37\0\342\0\37" +
		"\0\343\0\37\0\344\0\37\0\355\0\37\0\356\0\37\0\360\0\37\0\374\0\37\0\u0101\0\37\0" +
		"\u0105\0\37\0\u010a\0\37\0\u010c\0\37\0\u010d\0\37\0\u0110\0\37\0\u011e\0\37\0\u0120" +
		"\0\37\0\u0124\0\37\0\u0127\0\37\0\u0128\0\37\0\u012a\0\37\0\u012f\0\37\0\u0130\0" +
		"\37\0\u0134\0\37\0\u0135\0\37\0\u0136\0\37\0\u0137\0\37\0\u0138\0\37\0\u0139\0\37" +
		"\0\u013b\0\37\0\u013c\0\37\0\u013e\0\37\0\u013f\0\37\0\u0144\0\37\0\u0146\0\37\0" +
		"\u014c\0\37\0\u014e\0\37\0\u0154\0\37\0\u0157\0\37\0\u0159\0\37\0\u015b\0\37\0\u015d" +
		"\0\37\0\u015e\0\37\0\u016a\0\37\0\u0172\0\37\0\u0174\0\37\0\u0176\0\37\0\u0179\0" +
		"\37\0\u017b\0\37\0\u017c\0\37\0\u017d\0\37\0\u0186\0\37\0\u0191\0\37\0\u0193\0\37" +
		"\0\u0199\0\37\0\u019d\0\37\0\u01a0\0\37\0\u01a2\0\37\0\u01b2\0\37\0\u01b8\0\37\0" +
		"\u01ba\0\37\0\u01ca\0\37\0\1\0\40\0\2\0\40\0\6\0\40\0\12\0\40\0\67\0\40\0\71\0\40" +
		"\0\75\0\40\0\76\0\40\0\77\0\40\0\101\0\40\0\103\0\40\0\104\0\40\0\105\0\40\0\135" +
		"\0\40\0\136\0\40\0\144\0\40\0\145\0\40\0\150\0\40\0\151\0\40\0\164\0\40\0\166\0\40" +
		"\0\172\0\40\0\177\0\40\0\203\0\40\0\204\0\40\0\205\0\40\0\220\0\40\0\222\0\264\0" +
		"\223\0\40\0\225\0\40\0\233\0\40\0\235\0\40\0\243\0\40\0\250\0\40\0\255\0\40\0\257" +
		"\0\40\0\260\0\40\0\266\0\40\0\267\0\264\0\270\0\40\0\273\0\40\0\274\0\40\0\275\0" +
		"\40\0\301\0\40\0\315\0\40\0\334\0\40\0\335\0\40\0\337\0\40\0\341\0\40\0\342\0\40" +
		"\0\343\0\40\0\344\0\40\0\355\0\40\0\356\0\40\0\360\0\40\0\374\0\40\0\u0101\0\40\0" +
		"\u0105\0\40\0\u010a\0\40\0\u010c\0\40\0\u010d\0\40\0\u0110\0\40\0\u011e\0\40\0\u0120" +
		"\0\40\0\u0124\0\40\0\u0127\0\40\0\u0128\0\40\0\u012a\0\40\0\u012f\0\40\0\u0130\0" +
		"\40\0\u0134\0\40\0\u0135\0\40\0\u0136\0\40\0\u0137\0\40\0\u0138\0\40\0\u0139\0\40" +
		"\0\u013b\0\40\0\u013c\0\40\0\u013e\0\40\0\u013f\0\40\0\u0144\0\40\0\u0146\0\40\0" +
		"\u014c\0\40\0\u014e\0\40\0\u0154\0\40\0\u0157\0\40\0\u0159\0\40\0\u015b\0\40\0\u015d" +
		"\0\40\0\u015e\0\40\0\u016a\0\40\0\u0172\0\40\0\u0174\0\40\0\u0176\0\40\0\u0179\0" +
		"\40\0\u017b\0\40\0\u017c\0\40\0\u017d\0\40\0\u0186\0\40\0\u0191\0\40\0\u0193\0\40" +
		"\0\u0199\0\40\0\u019d\0\40\0\u01a0\0\40\0\u01a2\0\40\0\u01b2\0\40\0\u01b8\0\40\0" +
		"\u01ba\0\40\0\u01ca\0\40\0\1\0\41\0\2\0\41\0\6\0\41\0\12\0\41\0\67\0\41\0\71\0\41" +
		"\0\75\0\41\0\76\0\41\0\77\0\41\0\101\0\41\0\103\0\41\0\104\0\41\0\105\0\41\0\135" +
		"\0\41\0\136\0\41\0\144\0\41\0\145\0\41\0\146\0\177\0\150\0\41\0\151\0\41\0\164\0" +
		"\41\0\166\0\41\0\172\0\41\0\177\0\41\0\203\0\41\0\204\0\41\0\205\0\41\0\220\0\41" +
		"\0\223\0\41\0\225\0\41\0\233\0\41\0\235\0\41\0\243\0\41\0\250\0\41\0\255\0\41\0\257" +
		"\0\41\0\260\0\41\0\266\0\41\0\270\0\41\0\273\0\41\0\274\0\41\0\275\0\41\0\301\0\41" +
		"\0\315\0\41\0\334\0\41\0\335\0\41\0\337\0\41\0\341\0\41\0\342\0\41\0\343\0\41\0\344" +
		"\0\41\0\355\0\41\0\356\0\41\0\360\0\41\0\374\0\41\0\u0101\0\41\0\u0105\0\41\0\u010a" +
		"\0\41\0\u010c\0\41\0\u010d\0\41\0\u0110\0\41\0\u011e\0\41\0\u0120\0\41\0\u0124\0" +
		"\41\0\u0127\0\41\0\u0128\0\41\0\u012a\0\41\0\u012f\0\41\0\u0130\0\41\0\u0134\0\41" +
		"\0\u0135\0\41\0\u0136\0\41\0\u0137\0\41\0\u0138\0\41\0\u0139\0\41\0\u013b\0\41\0" +
		"\u013c\0\41\0\u013e\0\41\0\u013f\0\41\0\u0144\0\41\0\u0146\0\41\0\u014c\0\41\0\u014e" +
		"\0\41\0\u0154\0\41\0\u0157\0\41\0\u0159\0\41\0\u015b\0\41\0\u015d\0\41\0\u015e\0" +
		"\41\0\u016a\0\41\0\u0172\0\41\0\u0174\0\41\0\u0176\0\41\0\u0179\0\41\0\u017b\0\41" +
		"\0\u017c\0\41\0\u017d\0\41\0\u0186\0\41\0\u0191\0\41\0\u0193\0\41\0\u0199\0\41\0" +
		"\u019d\0\41\0\u01a0\0\41\0\u01a2\0\41\0\u01b2\0\41\0\u01b8\0\41\0\u01ba\0\41\0\u01ca" +
		"\0\41\0\1\0\42\0\2\0\42\0\6\0\42\0\12\0\42\0\67\0\42\0\71\0\42\0\75\0\42\0\76\0\42" +
		"\0\77\0\42\0\101\0\42\0\103\0\42\0\104\0\42\0\105\0\42\0\135\0\42\0\136\0\42\0\144" +
		"\0\42\0\145\0\42\0\150\0\42\0\151\0\42\0\164\0\42\0\166\0\42\0\172\0\42\0\177\0\42" +
		"\0\203\0\42\0\204\0\42\0\205\0\42\0\220\0\42\0\223\0\42\0\225\0\42\0\233\0\42\0\235" +
		"\0\42\0\243\0\42\0\250\0\42\0\255\0\42\0\257\0\42\0\260\0\42\0\266\0\42\0\270\0\42" +
		"\0\273\0\42\0\274\0\42\0\275\0\42\0\301\0\42\0\315\0\42\0\334\0\42\0\335\0\42\0\337" +
		"\0\42\0\340\0\u0127\0\341\0\42\0\342\0\42\0\343\0\42\0\344\0\42\0\355\0\42\0\356" +
		"\0\42\0\360\0\42\0\374\0\42\0\u0101\0\42\0\u0105\0\42\0\u010a\0\42\0\u010c\0\42\0" +
		"\u010d\0\42\0\u0110\0\42\0\u011e\0\42\0\u0120\0\42\0\u0124\0\42\0\u0127\0\42\0\u0128" +
		"\0\42\0\u012a\0\42\0\u012f\0\42\0\u0130\0\42\0\u0134\0\42\0\u0135\0\42\0\u0136\0" +
		"\42\0\u0137\0\42\0\u0138\0\42\0\u0139\0\42\0\u013b\0\42\0\u013c\0\42\0\u013e\0\42" +
		"\0\u013f\0\42\0\u0144\0\42\0\u0146\0\42\0\u014c\0\42\0\u014e\0\42\0\u0154\0\42\0" +
		"\u0157\0\42\0\u0159\0\42\0\u015b\0\42\0\u015d\0\42\0\u015e\0\42\0\u016a\0\42\0\u0172" +
		"\0\42\0\u0174\0\42\0\u0176\0\42\0\u0179\0\42\0\u017b\0\42\0\u017c\0\42\0\u017d\0" +
		"\42\0\u0186\0\42\0\u0191\0\42\0\u0193\0\42\0\u0199\0\42\0\u019d\0\42\0\u01a0\0\42" +
		"\0\u01a2\0\42\0\u01b2\0\42\0\u01b8\0\42\0\u01ba\0\42\0\u01ca\0\42\0\1\0\43\0\2\0" +
		"\43\0\6\0\43\0\12\0\43\0\67\0\43\0\71\0\43\0\75\0\43\0\76\0\43\0\77\0\43\0\101\0" +
		"\43\0\103\0\43\0\104\0\43\0\105\0\43\0\135\0\43\0\136\0\43\0\144\0\43\0\145\0\43" +
		"\0\150\0\43\0\151\0\43\0\164\0\43\0\166\0\43\0\172\0\43\0\177\0\43\0\203\0\43\0\204" +
		"\0\43\0\205\0\43\0\220\0\43\0\223\0\43\0\224\0\301\0\225\0\43\0\233\0\43\0\235\0" +
		"\43\0\243\0\43\0\250\0\43\0\255\0\43\0\257\0\43\0\260\0\43\0\266\0\43\0\270\0\43" +
		"\0\273\0\43\0\274\0\43\0\275\0\43\0\301\0\43\0\306\0\301\0\312\0\301\0\315\0\43\0" +
		"\334\0\43\0\335\0\43\0\337\0\43\0\341\0\43\0\342\0\43\0\343\0\43\0\344\0\43\0\355" +
		"\0\43\0\356\0\43\0\360\0\43\0\374\0\43\0\u0101\0\43\0\u0105\0\43\0\u010a\0\43\0\u010c" +
		"\0\43\0\u010d\0\43\0\u0110\0\43\0\u0113\0\301\0\u011e\0\43\0\u0120\0\43\0\u0124\0" +
		"\43\0\u0127\0\43\0\u0128\0\43\0\u012a\0\43\0\u012f\0\43\0\u0130\0\43\0\u0134\0\43" +
		"\0\u0135\0\43\0\u0136\0\43\0\u0137\0\43\0\u0138\0\43\0\u0139\0\43\0\u013b\0\43\0" +
		"\u013c\0\43\0\u013e\0\43\0\u013f\0\43\0\u0144\0\43\0\u0146\0\43\0\u014c\0\43\0\u014e" +
		"\0\43\0\u0154\0\43\0\u0157\0\43\0\u0159\0\43\0\u015b\0\43\0\u015d\0\43\0\u015e\0" +
		"\43\0\u016a\0\43\0\u0172\0\43\0\u0174\0\43\0\u0176\0\43\0\u0179\0\43\0\u017b\0\43" +
		"\0\u017c\0\43\0\u017d\0\43\0\u0186\0\43\0\u0191\0\43\0\u0193\0\43\0\u0199\0\43\0" +
		"\u019d\0\43\0\u01a0\0\43\0\u01a2\0\43\0\u01b2\0\43\0\u01b8\0\43\0\u01ba\0\43\0\u01ca" +
		"\0\43\0\1\0\44\0\2\0\44\0\6\0\44\0\12\0\44\0\67\0\44\0\71\0\44\0\75\0\44\0\76\0\44" +
		"\0\77\0\44\0\101\0\44\0\103\0\44\0\104\0\44\0\105\0\44\0\135\0\44\0\136\0\44\0\144" +
		"\0\44\0\145\0\44\0\150\0\44\0\151\0\44\0\164\0\44\0\166\0\44\0\172\0\44\0\177\0\44" +
		"\0\203\0\44\0\204\0\44\0\205\0\44\0\220\0\44\0\222\0\265\0\223\0\44\0\225\0\44\0" +
		"\233\0\44\0\235\0\44\0\243\0\44\0\250\0\44\0\255\0\44\0\257\0\44\0\260\0\44\0\266" +
		"\0\44\0\270\0\44\0\273\0\44\0\274\0\44\0\275\0\44\0\301\0\44\0\315\0\44\0\334\0\44" +
		"\0\335\0\44\0\337\0\44\0\341\0\44\0\342\0\44\0\343\0\44\0\344\0\44\0\355\0\44\0\356" +
		"\0\44\0\360\0\44\0\374\0\44\0\u0101\0\44\0\u0105\0\44\0\u010a\0\44\0\u010c\0\44\0" +
		"\u010d\0\44\0\u0110\0\44\0\u011e\0\44\0\u0120\0\44\0\u0124\0\44\0\u0127\0\44\0\u0128" +
		"\0\44\0\u012a\0\44\0\u012f\0\44\0\u0130\0\44\0\u0134\0\44\0\u0135\0\44\0\u0136\0" +
		"\44\0\u0137\0\44\0\u0138\0\44\0\u0139\0\44\0\u013b\0\44\0\u013c\0\44\0\u013e\0\44" +
		"\0\u013f\0\44\0\u0144\0\44\0\u0146\0\44\0\u014c\0\44\0\u014e\0\44\0\u0154\0\44\0" +
		"\u0157\0\44\0\u0159\0\44\0\u015b\0\44\0\u015d\0\44\0\u015e\0\44\0\u016a\0\44\0\u0172" +
		"\0\44\0\u0174\0\44\0\u0176\0\44\0\u0179\0\44\0\u017b\0\44\0\u017c\0\44\0\u017d\0" +
		"\44\0\u0186\0\44\0\u0191\0\44\0\u0193\0\44\0\u0199\0\44\0\u019d\0\44\0\u01a0\0\44" +
		"\0\u01a2\0\44\0\u01b2\0\44\0\u01b8\0\44\0\u01ba\0\44\0\u01ca\0\44\0\1\0\45\0\2\0" +
		"\45\0\6\0\45\0\12\0\45\0\67\0\45\0\71\0\45\0\75\0\45\0\76\0\45\0\77\0\45\0\101\0" +
		"\45\0\103\0\45\0\104\0\45\0\105\0\45\0\135\0\45\0\136\0\45\0\144\0\45\0\145\0\45" +
		"\0\150\0\45\0\151\0\45\0\163\0\204\0\164\0\45\0\166\0\45\0\172\0\45\0\177\0\45\0" +
		"\203\0\45\0\204\0\45\0\205\0\45\0\220\0\45\0\223\0\45\0\225\0\45\0\233\0\45\0\235" +
		"\0\45\0\243\0\45\0\250\0\45\0\255\0\45\0\257\0\45\0\260\0\45\0\266\0\45\0\270\0\45" +
		"\0\273\0\45\0\274\0\45\0\275\0\45\0\301\0\45\0\315\0\45\0\334\0\45\0\335\0\45\0\337" +
		"\0\45\0\341\0\45\0\342\0\45\0\343\0\45\0\344\0\45\0\355\0\45\0\356\0\45\0\360\0\45" +
		"\0\374\0\45\0\u0101\0\45\0\u0105\0\45\0\u010a\0\45\0\u010c\0\45\0\u010d\0\45\0\u0110" +
		"\0\45\0\u011e\0\45\0\u0120\0\45\0\u0124\0\45\0\u0127\0\45\0\u0128\0\45\0\u012a\0" +
		"\45\0\u012f\0\45\0\u0130\0\45\0\u0134\0\45\0\u0135\0\45\0\u0136\0\45\0\u0137\0\45" +
		"\0\u0138\0\45\0\u0139\0\45\0\u013b\0\45\0\u013c\0\45\0\u013e\0\45\0\u013f\0\45\0" +
		"\u0144\0\45\0\u0146\0\45\0\u014c\0\45\0\u014e\0\45\0\u0154\0\45\0\u0157\0\45\0\u0159" +
		"\0\45\0\u015b\0\45\0\u015d\0\45\0\u015e\0\45\0\u016a\0\45\0\u0172\0\45\0\u0174\0" +
		"\45\0\u0176\0\45\0\u0179\0\45\0\u017b\0\45\0\u017c\0\45\0\u017d\0\45\0\u0186\0\45" +
		"\0\u0191\0\45\0\u0193\0\45\0\u0199\0\45\0\u019d\0\45\0\u01a0\0\45\0\u01a2\0\45\0" +
		"\u01b2\0\45\0\u01b8\0\45\0\u01ba\0\45\0\u01ca\0\45\0\1\0\46\0\2\0\46\0\6\0\46\0\12" +
		"\0\46\0\67\0\46\0\71\0\46\0\75\0\46\0\76\0\46\0\77\0\46\0\101\0\46\0\103\0\46\0\104" +
		"\0\46\0\105\0\46\0\135\0\46\0\136\0\46\0\144\0\46\0\145\0\46\0\150\0\46\0\151\0\46" +
		"\0\164\0\46\0\166\0\46\0\172\0\46\0\177\0\46\0\203\0\46\0\204\0\46\0\205\0\46\0\220" +
		"\0\46\0\223\0\46\0\225\0\46\0\233\0\46\0\235\0\46\0\243\0\46\0\250\0\46\0\255\0\46" +
		"\0\257\0\46\0\260\0\46\0\266\0\46\0\270\0\46\0\273\0\46\0\274\0\46\0\275\0\46\0\301" +
		"\0\46\0\315\0\46\0\334\0\46\0\335\0\46\0\337\0\46\0\340\0\u0128\0\341\0\46\0\342" +
		"\0\46\0\343\0\46\0\344\0\46\0\355\0\46\0\356\0\46\0\360\0\46\0\374\0\46\0\u0101\0" +
		"\46\0\u0105\0\46\0\u010a\0\46\0\u010c\0\46\0\u010d\0\46\0\u0110\0\46\0\u011e\0\46" +
		"\0\u0120\0\46\0\u0124\0\46\0\u0127\0\46\0\u0128\0\46\0\u012a\0\46\0\u012f\0\46\0" +
		"\u0130\0\46\0\u0134\0\46\0\u0135\0\46\0\u0136\0\46\0\u0137\0\46\0\u0138\0\46\0\u0139" +
		"\0\46\0\u013b\0\46\0\u013c\0\46\0\u013e\0\46\0\u013f\0\46\0\u0144\0\46\0\u0146\0" +
		"\46\0\u014c\0\46\0\u014e\0\46\0\u0154\0\46\0\u0157\0\46\0\u0159\0\46\0\u015b\0\46" +
		"\0\u015d\0\46\0\u015e\0\46\0\u016a\0\46\0\u0172\0\46\0\u0174\0\46\0\u0176\0\46\0" +
		"\u0179\0\46\0\u017b\0\46\0\u017c\0\46\0\u017d\0\46\0\u0186\0\46\0\u0191\0\46\0\u0193" +
		"\0\46\0\u0199\0\46\0\u019d\0\46\0\u01a0\0\46\0\u01a2\0\46\0\u01b2\0\46\0\u01b8\0" +
		"\46\0\u01ba\0\46\0\u01ca\0\46\0\1\0\47\0\2\0\47\0\6\0\47\0\12\0\47\0\67\0\47\0\71" +
		"\0\47\0\75\0\47\0\76\0\47\0\77\0\47\0\101\0\47\0\103\0\47\0\104\0\47\0\105\0\47\0" +
		"\135\0\47\0\136\0\47\0\144\0\47\0\145\0\47\0\150\0\47\0\151\0\47\0\164\0\47\0\166" +
		"\0\47\0\172\0\47\0\177\0\47\0\203\0\47\0\204\0\47\0\205\0\47\0\220\0\47\0\223\0\47" +
		"\0\225\0\47\0\233\0\47\0\235\0\47\0\243\0\47\0\250\0\47\0\255\0\47\0\257\0\47\0\260" +
		"\0\47\0\266\0\47\0\270\0\47\0\273\0\47\0\274\0\47\0\275\0\47\0\301\0\47\0\315\0\47" +
		"\0\334\0\47\0\335\0\47\0\337\0\47\0\341\0\47\0\342\0\47\0\343\0\47\0\344\0\47\0\355" +
		"\0\47\0\356\0\47\0\360\0\47\0\374\0\47\0\u0101\0\47\0\u0105\0\47\0\u010a\0\47\0\u010c" +
		"\0\47\0\u010d\0\47\0\u0110\0\47\0\u011e\0\47\0\u0120\0\47\0\u0124\0\47\0\u0127\0" +
		"\47\0\u0128\0\47\0\u012a\0\47\0\u012f\0\47\0\u0130\0\47\0\u0134\0\47\0\u0135\0\47" +
		"\0\u0136\0\47\0\u0137\0\47\0\u0138\0\47\0\u0139\0\47\0\u013b\0\47\0\u013c\0\47\0" +
		"\u013e\0\47\0\u013f\0\47\0\u0144\0\47\0\u0146\0\47\0\u014c\0\47\0\u014e\0\47\0\u0154" +
		"\0\47\0\u0157\0\47\0\u0159\0\47\0\u015b\0\47\0\u015d\0\47\0\u015e\0\47\0\u0161\0" +
		"\u01a5\0\u016a\0\47\0\u0172\0\47\0\u0174\0\47\0\u0176\0\47\0\u0179\0\47\0\u017b\0" +
		"\47\0\u017c\0\47\0\u017d\0\47\0\u0186\0\47\0\u0191\0\47\0\u0193\0\47\0\u0199\0\47" +
		"\0\u019d\0\47\0\u01a0\0\47\0\u01a2\0\47\0\u01b2\0\47\0\u01b8\0\47\0\u01ba\0\47\0" +
		"\u01ca\0\47\0\1\0\50\0\2\0\50\0\6\0\50\0\12\0\50\0\67\0\50\0\71\0\50\0\75\0\50\0" +
		"\76\0\50\0\77\0\50\0\101\0\50\0\103\0\50\0\104\0\50\0\105\0\50\0\135\0\50\0\136\0" +
		"\50\0\144\0\50\0\145\0\50\0\150\0\50\0\151\0\50\0\164\0\50\0\166\0\50\0\172\0\50" +
		"\0\177\0\50\0\203\0\50\0\204\0\50\0\205\0\50\0\220\0\50\0\223\0\50\0\225\0\50\0\233" +
		"\0\50\0\235\0\50\0\243\0\50\0\250\0\50\0\255\0\50\0\257\0\50\0\260\0\50\0\266\0\50" +
		"\0\270\0\50\0\273\0\50\0\274\0\50\0\275\0\50\0\301\0\50\0\315\0\50\0\334\0\50\0\335" +
		"\0\50\0\337\0\50\0\341\0\50\0\342\0\50\0\343\0\50\0\344\0\50\0\355\0\50\0\356\0\50" +
		"\0\360\0\50\0\374\0\50\0\u0101\0\50\0\u0105\0\50\0\u010a\0\50\0\u010c\0\50\0\u010d" +
		"\0\50\0\u0110\0\50\0\u011e\0\50\0\u0120\0\50\0\u0124\0\50\0\u0127\0\50\0\u0128\0" +
		"\50\0\u012a\0\50\0\u012f\0\50\0\u0130\0\50\0\u0134\0\50\0\u0135\0\50\0\u0136\0\50" +
		"\0\u0137\0\50\0\u0138\0\50\0\u0139\0\50\0\u013b\0\50\0\u013c\0\50\0\u013e\0\50\0" +
		"\u013f\0\50\0\u0144\0\50\0\u0146\0\50\0\u014c\0\50\0\u014e\0\50\0\u0154\0\50\0\u0157" +
		"\0\50\0\u0159\0\50\0\u015b\0\50\0\u015d\0\50\0\u015e\0\50\0\u0161\0\u01a6\0\u016a" +
		"\0\50\0\u0172\0\50\0\u0174\0\50\0\u0176\0\50\0\u0179\0\50\0\u017b\0\50\0\u017c\0" +
		"\50\0\u017d\0\50\0\u0186\0\50\0\u0191\0\50\0\u0193\0\50\0\u0199\0\50\0\u019d\0\50" +
		"\0\u01a0\0\50\0\u01a2\0\50\0\u01b2\0\50\0\u01b8\0\50\0\u01ba\0\50\0\u01ca\0\50\0" +
		"\1\0\51\0\2\0\51\0\6\0\51\0\12\0\51\0\67\0\51\0\71\0\51\0\75\0\51\0\76\0\51\0\77" +
		"\0\51\0\101\0\51\0\103\0\51\0\104\0\51\0\105\0\51\0\135\0\51\0\136\0\51\0\144\0\51" +
		"\0\145\0\51\0\150\0\51\0\151\0\51\0\164\0\51\0\166\0\51\0\172\0\51\0\177\0\51\0\203" +
		"\0\51\0\204\0\51\0\205\0\51\0\220\0\51\0\223\0\51\0\224\0\302\0\225\0\51\0\233\0" +
		"\51\0\235\0\51\0\243\0\51\0\250\0\51\0\255\0\51\0\257\0\51\0\260\0\51\0\266\0\51" +
		"\0\270\0\51\0\273\0\51\0\274\0\51\0\275\0\51\0\301\0\51\0\306\0\302\0\312\0\302\0" +
		"\315\0\51\0\334\0\51\0\335\0\51\0\337\0\51\0\341\0\51\0\342\0\51\0\343\0\51\0\344" +
		"\0\51\0\355\0\51\0\356\0\51\0\360\0\51\0\374\0\51\0\u0101\0\51\0\u0105\0\51\0\u010a" +
		"\0\51\0\u010c\0\51\0\u010d\0\51\0\u0110\0\51\0\u0113\0\302\0\u011e\0\51\0\u0120\0" +
		"\51\0\u0124\0\51\0\u0127\0\51\0\u0128\0\51\0\u012a\0\51\0\u012f\0\51\0\u0130\0\51" +
		"\0\u0134\0\51\0\u0135\0\51\0\u0136\0\51\0\u0137\0\51\0\u0138\0\51\0\u0139\0\51\0" +
		"\u013b\0\51\0\u013c\0\51\0\u013e\0\51\0\u013f\0\51\0\u0144\0\51\0\u0146\0\51\0\u014c" +
		"\0\51\0\u014e\0\51\0\u0154\0\51\0\u0157\0\51\0\u0159\0\51\0\u015b\0\51\0\u015d\0" +
		"\51\0\u015e\0\51\0\u016a\0\51\0\u0172\0\51\0\u0174\0\51\0\u0176\0\51\0\u0179\0\51" +
		"\0\u017b\0\51\0\u017c\0\51\0\u017d\0\51\0\u0186\0\51\0\u0191\0\51\0\u0193\0\51\0" +
		"\u0199\0\51\0\u019d\0\51\0\u01a0\0\51\0\u01a2\0\51\0\u01b2\0\51\0\u01b8\0\51\0\u01ba" +
		"\0\51\0\u01ca\0\51\0\1\0\52\0\2\0\52\0\6\0\52\0\12\0\52\0\67\0\52\0\71\0\52\0\75" +
		"\0\52\0\76\0\52\0\77\0\52\0\101\0\52\0\103\0\52\0\104\0\52\0\105\0\52\0\135\0\52" +
		"\0\136\0\52\0\144\0\52\0\145\0\52\0\150\0\52\0\151\0\52\0\163\0\205\0\164\0\52\0" +
		"\166\0\52\0\172\0\52\0\177\0\52\0\203\0\52\0\204\0\52\0\205\0\52\0\220\0\52\0\223" +
		"\0\52\0\225\0\52\0\233\0\52\0\235\0\52\0\243\0\52\0\250\0\52\0\255\0\52\0\257\0\52" +
		"\0\260\0\52\0\266\0\52\0\270\0\52\0\273\0\52\0\274\0\52\0\275\0\52\0\301\0\52\0\315" +
		"\0\52\0\334\0\52\0\335\0\52\0\337\0\52\0\341\0\52\0\342\0\52\0\343\0\52\0\344\0\52" +
		"\0\355\0\52\0\356\0\52\0\360\0\52\0\374\0\52\0\u0101\0\52\0\u0105\0\52\0\u010a\0" +
		"\52\0\u010c\0\52\0\u010d\0\52\0\u0110\0\52\0\u011e\0\52\0\u0120\0\52\0\u0124\0\52" +
		"\0\u0127\0\52\0\u0128\0\52\0\u012a\0\52\0\u012f\0\52\0\u0130\0\52\0\u0134\0\52\0" +
		"\u0135\0\52\0\u0136\0\52\0\u0137\0\52\0\u0138\0\52\0\u0139\0\52\0\u013b\0\52\0\u013c" +
		"\0\52\0\u013e\0\52\0\u013f\0\52\0\u0144\0\52\0\u0146\0\52\0\u014c\0\52\0\u014e\0" +
		"\52\0\u0154\0\52\0\u0157\0\52\0\u0159\0\52\0\u015b\0\52\0\u015d\0\52\0\u015e\0\52" +
		"\0\u016a\0\52\0\u0172\0\52\0\u0174\0\52\0\u0176\0\52\0\u0179\0\52\0\u017b\0\52\0" +
		"\u017c\0\52\0\u017d\0\52\0\u0186\0\52\0\u0191\0\52\0\u0193\0\52\0\u0199\0\52\0\u019d" +
		"\0\52\0\u01a0\0\52\0\u01a2\0\52\0\u01b2\0\52\0\u01b8\0\52\0\u01ba\0\52\0\u01ca\0" +
		"\52\0\165\0\213\0\221\0\213\0\224\0\213\0\273\0\347\0\306\0\213\0\312\0\213\0\343" +
		"\0\347\0\355\0\347\0\356\0\347\0\360\0\347\0\u010a\0\347\0\u010c\0\347\0\u010d\0" +
		"\347\0\u0110\0\347\0\u0113\0\213\0\u012f\0\347\0\u0134\0\347\0\u0139\0\347\0\u013b" +
		"\0\347\0\u013c\0\347\0\u013e\0\347\0\u0144\0\347\0\u0154\0\347\0\u0157\0\347\0\u0159" +
		"\0\347\0\u015b\0\347\0\u015d\0\347\0\u015e\0\347\0\u0163\0\347\0\u0186\0\347\0\u0199" +
		"\0\347\0\u019d\0\347\0\u01a0\0\347\0\u01a2\0\347\0\u01aa\0\347\0\u01ca\0\347\0\172" +
		"\0\220\0\212\0\245\0\247\0\245\0\322\0\245\0\u0100\0\u014c\0\1\0\53\0\2\0\57\0\6" +
		"\0\53\0\12\0\57\0\67\0\106\0\71\0\57\0\75\0\116\0\76\0\120\0\77\0\122\0\101\0\53" +
		"\0\103\0\127\0\104\0\106\0\105\0\106\0\135\0\153\0\136\0\106\0\144\0\165\0\145\0" +
		"\53\0\150\0\53\0\151\0\127\0\164\0\207\0\166\0\165\0\172\0\221\0\177\0\224\0\203" +
		"\0\153\0\204\0\236\0\205\0\236\0\220\0\165\0\223\0\271\0\225\0\224\0\233\0\312\0" +
		"\235\0\153\0\243\0\207\0\250\0\165\0\255\0\325\0\257\0\153\0\260\0\331\0\266\0\153" +
		"\0\270\0\336\0\273\0\350\0\274\0\u0100\0\275\0\u0101\0\301\0\153\0\315\0\236\0\334" +
		"\0\153\0\335\0\u0123\0\337\0\53\0\341\0\u0129\0\342\0\106\0\343\0\350\0\344\0\153" +
		"\0\355\0\350\0\356\0\350\0\360\0\350\0\374\0\350\0\u0101\0\u014d\0\u0105\0\153\0" +
		"\u010a\0\350\0\u010c\0\350\0\u010d\0\350\0\u0110\0\350\0\u011e\0\153\0\u0120\0\u0169" +
		"\0\u0124\0\153\0\u0127\0\153\0\u0128\0\153\0\u012a\0\106\0\u012f\0\350\0\u0130\0" +
		"\153\0\u0134\0\350\0\u0135\0\u017d\0\u0136\0\u0181\0\u0137\0\53\0\u0138\0\u0181\0" +
		"\u0139\0\350\0\u013b\0\350\0\u013c\0\350\0\u013e\0\350\0\u013f\0\350\0\u0144\0\350" +
		"\0\u0146\0\53\0\u014c\0\u0190\0\u014e\0\u0101\0\u0154\0\350\0\u0157\0\350\0\u0159" +
		"\0\350\0\u015b\0\350\0\u015d\0\350\0\u015e\0\350\0\u016a\0\153\0\u0172\0\106\0\u0174" +
		"\0\106\0\u0176\0\153\0\u0179\0\153\0\u017b\0\u017d\0\u017c\0\u017d\0\u017d\0\53\0" +
		"\u0186\0\350\0\u0191\0\153\0\u0193\0\153\0\u0199\0\350\0\u019d\0\350\0\u01a0\0\350" +
		"\0\u01a2\0\350\0\u01b2\0\153\0\u01b8\0\u017d\0\u01ba\0\u017d\0\u01ca\0\350\0\3\0" +
		"\62\0\0\0\u01dc\0\62\0\77\0\0\0\3\0\77\0\123\0\123\0\147\0\60\0\73\0\140\0\73\0\62" +
		"\0\100\0\77\0\124\0\1\0\54\0\6\0\54\0\101\0\54\0\145\0\54\0\150\0\54\0\273\0\351" +
		"\0\337\0\54\0\343\0\351\0\355\0\351\0\356\0\351\0\360\0\351\0\374\0\351\0\u010a\0" +
		"\351\0\u010c\0\351\0\u010d\0\351\0\u0110\0\351\0\u012f\0\351\0\u0134\0\351\0\u0135" +
		"\0\u017e\0\u0136\0\351\0\u0137\0\351\0\u0138\0\351\0\u0139\0\351\0\u013b\0\351\0" +
		"\u013c\0\351\0\u013e\0\351\0\u013f\0\351\0\u0144\0\351\0\u0146\0\u018e\0\u0154\0" +
		"\351\0\u0157\0\351\0\u0159\0\351\0\u015b\0\351\0\u015d\0\351\0\u015e\0\351\0\u017b" +
		"\0\u017e\0\u017c\0\u017e\0\u017d\0\u01b7\0\u0186\0\351\0\u0199\0\351\0\u019d\0\351" +
		"\0\u01a0\0\351\0\u01a2\0\351\0\u01b8\0\u017e\0\u01ba\0\u017e\0\u01ca\0\351\0\135" +
		"\0\154\0\203\0\235\0\235\0\313\0\257\0\326\0\266\0\333\0\301\0\u0109\0\334\0\u0122" +
		"\0\344\0\u0131\0\u0105\0\u0150\0\u011e\0\326\0\u0124\0\154\0\u0127\0\u016d\0\u0128" +
		"\0\u016e\0\u0130\0\u0177\0\u016a\0\154\0\u0176\0\333\0\u0179\0\u0131\0\u0191\0\154" +
		"\0\u0193\0\u01c0\0\u01b2\0\u0122\0\165\0\214\0\221\0\214\0\224\0\303\0\306\0\303" +
		"\0\312\0\303\0\u0113\0\303\0\212\0\246\0\247\0\320\0\322\0\u0118\0\144\0\166\0\220" +
		"\0\250\0\144\0\167\0\166\0\216\0\220\0\167\0\250\0\216\0\144\0\170\0\166\0\170\0" +
		"\220\0\170\0\250\0\170\0\144\0\171\0\166\0\171\0\220\0\171\0\250\0\171\0\144\0\172" +
		"\0\166\0\172\0\220\0\172\0\250\0\172\0\164\0\210\0\144\0\173\0\166\0\173\0\220\0" +
		"\173\0\250\0\173\0\u0117\0\u0162\0\u0164\0\u0162\0\u0161\0\u01a7\0\144\0\174\0\166" +
		"\0\174\0\220\0\174\0\250\0\174\0\204\0\237\0\205\0\241\0\144\0\175\0\166\0\175\0" +
		"\220\0\175\0\250\0\175\0\164\0\211\0\243\0\317\0\204\0\240\0\205\0\240\0\315\0\u0115" +
		"\0\177\0\225\0\177\0\226\0\225\0\307\0\177\0\227\0\225\0\227\0\224\0\304\0\306\0" +
		"\u010e\0\312\0\u0111\0\u0113\0\u015f\0\276\0\u0106\0\u0108\0\u0106\0\222\0\266\0" +
		"\222\0\267\0\177\0\230\0\225\0\230\0\177\0\231\0\225\0\231\0\260\0\332\0\257\0\327" +
		"\0\257\0\330\0\u011e\0\u0168\0\266\0\334\0\u0176\0\u01b2\0\u0105\0\u0151\0\273\0" +
		"\352\0\343\0\352\0\u010a\0\352\0\u010c\0\352\0\u010d\0\352\0\u0110\0\352\0\u0134" +
		"\0\352\0\u0154\0\352\0\u0157\0\352\0\u0159\0\352\0\u015b\0\352\0\u015d\0\352\0\u015e" +
		"\0\352\0\u0199\0\352\0\u019d\0\352\0\u01a0\0\352\0\u01a2\0\352\0\u01ca\0\352\0\273" +
		"\0\353\0\343\0\u012e\0\u010a\0\u0153\0\u010c\0\u0155\0\u010d\0\u0156\0\u0110\0\u015a" +
		"\0\u0134\0\u017a\0\u0154\0\u0195\0\u0157\0\u0198\0\u0159\0\u019a\0\u015b\0\u019c" +
		"\0\u015d\0\u019e\0\u015e\0\u019f\0\u0199\0\u01c3\0\u019d\0\u01c6\0\u01a0\0\u01c9" +
		"\0\u01a2\0\u01cb\0\u01ca\0\u01d7\0\273\0\354\0\343\0\354\0\u010a\0\354\0\u010c\0" +
		"\354\0\u010d\0\354\0\u0110\0\354\0\u0134\0\354\0\u0139\0\u0185\0\u0154\0\354\0\u0157" +
		"\0\354\0\u0159\0\354\0\u015b\0\354\0\u015d\0\354\0\u015e\0\354\0\u0199\0\354\0\u019d" +
		"\0\354\0\u01a0\0\354\0\u01a2\0\354\0\u01ca\0\354\0\273\0\355\0\343\0\355\0\u010a" +
		"\0\355\0\u010c\0\355\0\u010d\0\355\0\u0110\0\355\0\u0134\0\355\0\u0139\0\355\0\u0154" +
		"\0\355\0\u0157\0\355\0\u0159\0\355\0\u015b\0\355\0\u015d\0\355\0\u015e\0\355\0\u0199" +
		"\0\355\0\u019d\0\355\0\u01a0\0\355\0\u01a2\0\355\0\u01ca\0\355\0\273\0\356\0\343" +
		"\0\356\0\355\0\u013b\0\u010a\0\356\0\u010c\0\356\0\u010d\0\356\0\u0110\0\356\0\u0134" +
		"\0\356\0\u0139\0\356\0\u0154\0\356\0\u0157\0\356\0\u0159\0\356\0\u015b\0\356\0\u015d" +
		"\0\356\0\u015e\0\356\0\u0199\0\356\0\u019d\0\356\0\u01a0\0\356\0\u01a2\0\356\0\u01ca" +
		"\0\356\0\273\0\357\0\343\0\357\0\355\0\357\0\356\0\357\0\360\0\357\0\u010a\0\357" +
		"\0\u010c\0\357\0\u010d\0\357\0\u0110\0\357\0\u012f\0\357\0\u0134\0\357\0\u0139\0" +
		"\357\0\u013b\0\357\0\u013c\0\357\0\u013e\0\357\0\u0154\0\357\0\u0157\0\357\0\u0159" +
		"\0\357\0\u015b\0\357\0\u015d\0\357\0\u015e\0\357\0\u0186\0\357\0\u0199\0\357\0\u019d" +
		"\0\357\0\u01a0\0\357\0\u01a2\0\357\0\u01ca\0\357\0\224\0\305\0\304\0\u010b\0\306" +
		"\0\u010f\0\312\0\u0112\0\377\0\u014b\0\u010e\0\u0158\0\u0111\0\u015c\0\u0113\0\u0160" +
		"\0\u013d\0\u0189\0\u0140\0\u018b\0\u0143\0\u018c\0\u015f\0\u01a1\0\u0187\0\u01bc" +
		"\0\u0188\0\u01bd\0\u018a\0\u01be\0\u01bb\0\u01d3\0\273\0\360\0\343\0\u012f\0\355" +
		"\0\u013c\0\356\0\u013e\0\u010a\0\360\0\u010c\0\360\0\u010d\0\360\0\u0110\0\360\0" +
		"\u0134\0\360\0\u0139\0\360\0\u013b\0\u0186\0\u0154\0\360\0\u0157\0\360\0\u0159\0" +
		"\360\0\u015b\0\360\0\u015d\0\360\0\u015e\0\360\0\u0199\0\360\0\u019d\0\360\0\u01a0" +
		"\0\360\0\u01a2\0\360\0\u01ca\0\360\0\273\0\361\0\343\0\361\0\355\0\361\0\356\0\361" +
		"\0\360\0\u0141\0\u010a\0\361\0\u010c\0\361\0\u010d\0\361\0\u0110\0\361\0\u012f\0" +
		"\u0141\0\u0134\0\361\0\u0139\0\361\0\u013b\0\361\0\u013c\0\u0141\0\u013e\0\u0141" +
		"\0\u0144\0\u018d\0\u0154\0\361\0\u0157\0\361\0\u0159\0\361\0\u015b\0\361\0\u015d" +
		"\0\361\0\u015e\0\361\0\u0186\0\u0141\0\u0199\0\361\0\u019d\0\361\0\u01a0\0\361\0" +
		"\u01a2\0\361\0\u01ca\0\361\0\344\0\u0132\0\273\0\362\0\343\0\362\0\355\0\362\0\356" +
		"\0\362\0\360\0\362\0\u010a\0\362\0\u010c\0\362\0\u010d\0\362\0\u0110\0\362\0\u012f" +
		"\0\362\0\u0134\0\362\0\u0139\0\362\0\u013b\0\362\0\u013c\0\362\0\u013e\0\362\0\u0144" +
		"\0\362\0\u0154\0\362\0\u0157\0\362\0\u0159\0\362\0\u015b\0\362\0\u015d\0\362\0\u015e" +
		"\0\362\0\u0186\0\362\0\u0199\0\362\0\u019d\0\362\0\u01a0\0\362\0\u01a2\0\362\0\u01ca" +
		"\0\362\0\344\0\u0133\0\u0179\0\u01b3\0\273\0\363\0\343\0\363\0\355\0\363\0\356\0" +
		"\363\0\360\0\363\0\u010a\0\363\0\u010c\0\363\0\u010d\0\363\0\u0110\0\363\0\u012f" +
		"\0\363\0\u0134\0\363\0\u0139\0\363\0\u013b\0\363\0\u013c\0\363\0\u013e\0\363\0\u0144" +
		"\0\363\0\u0154\0\363\0\u0157\0\363\0\u0159\0\363\0\u015b\0\363\0\u015d\0\363\0\u015e" +
		"\0\363\0\u0186\0\363\0\u0199\0\363\0\u019d\0\363\0\u01a0\0\363\0\u01a2\0\363\0\u01ca" +
		"\0\363\0\273\0\364\0\343\0\364\0\355\0\364\0\356\0\364\0\360\0\364\0\u010a\0\364" +
		"\0\u010c\0\364\0\u010d\0\364\0\u0110\0\364\0\u012f\0\364\0\u0134\0\364\0\u0139\0" +
		"\364\0\u013b\0\364\0\u013c\0\364\0\u013e\0\364\0\u0144\0\364\0\u0154\0\364\0\u0157" +
		"\0\364\0\u0159\0\364\0\u015b\0\364\0\u015d\0\364\0\u015e\0\364\0\u0186\0\364\0\u0199" +
		"\0\364\0\u019d\0\364\0\u01a0\0\364\0\u01a2\0\364\0\u01ca\0\364\0\273\0\365\0\343" +
		"\0\365\0\355\0\365\0\356\0\365\0\360\0\365\0\374\0\u014a\0\u010a\0\365\0\u010c\0" +
		"\365\0\u010d\0\365\0\u0110\0\365\0\u012f\0\365\0\u0134\0\365\0\u0139\0\365\0\u013b" +
		"\0\365\0\u013c\0\365\0\u013e\0\365\0\u013f\0\u014a\0\u0144\0\365\0\u0154\0\365\0" +
		"\u0157\0\365\0\u0159\0\365\0\u015b\0\365\0\u015d\0\365\0\u015e\0\365\0\u0186\0\365" +
		"\0\u0199\0\365\0\u019d\0\365\0\u01a0\0\365\0\u01a2\0\365\0\u01ca\0\365\0\273\0\366" +
		"\0\343\0\366\0\355\0\366\0\356\0\366\0\360\0\366\0\374\0\366\0\u010a\0\366\0\u010c" +
		"\0\366\0\u010d\0\366\0\u0110\0\366\0\u012f\0\366\0\u0134\0\366\0\u0136\0\u0182\0" +
		"\u0138\0\u0184\0\u0139\0\366\0\u013b\0\366\0\u013c\0\366\0\u013e\0\366\0\u013f\0" +
		"\366\0\u0144\0\366\0\u0154\0\366\0\u0157\0\366\0\u0159\0\366\0\u015b\0\366\0\u015d" +
		"\0\366\0\u015e\0\366\0\u0186\0\366\0\u0199\0\366\0\u019d\0\366\0\u01a0\0\366\0\u01a2" +
		"\0\366\0\u01ca\0\366\0\273\0\367\0\343\0\367\0\355\0\367\0\356\0\367\0\360\0\367" +
		"\0\374\0\367\0\u010a\0\367\0\u010c\0\367\0\u010d\0\367\0\u0110\0\367\0\u012f\0\367" +
		"\0\u0134\0\367\0\u0136\0\367\0\u0138\0\367\0\u0139\0\367\0\u013b\0\367\0\u013c\0" +
		"\367\0\u013e\0\367\0\u013f\0\367\0\u0144\0\367\0\u0154\0\367\0\u0157\0\367\0\u0159" +
		"\0\367\0\u015b\0\367\0\u015d\0\367\0\u015e\0\367\0\u0186\0\367\0\u0199\0\367\0\u019d" +
		"\0\367\0\u01a0\0\367\0\u01a2\0\367\0\u01ca\0\367\0\273\0\370\0\343\0\370\0\355\0" +
		"\370\0\356\0\370\0\360\0\370\0\u010a\0\370\0\u010c\0\370\0\u010d\0\370\0\u0110\0" +
		"\370\0\u012f\0\370\0\u0134\0\370\0\u0139\0\370\0\u013b\0\370\0\u013c\0\370\0\u013e" +
		"\0\370\0\u0144\0\370\0\u0154\0\370\0\u0157\0\370\0\u0159\0\370\0\u015b\0\370\0\u015d" +
		"\0\370\0\u015e\0\370\0\u0186\0\370\0\u0199\0\370\0\u019d\0\370\0\u01a0\0\370\0\u01a2" +
		"\0\370\0\u01ca\0\370\0\273\0\371\0\343\0\371\0\355\0\371\0\356\0\371\0\360\0\371" +
		"\0\374\0\371\0\u010a\0\371\0\u010c\0\371\0\u010d\0\371\0\u0110\0\371\0\u012f\0\371" +
		"\0\u0134\0\371\0\u0136\0\371\0\u0138\0\371\0\u0139\0\371\0\u013b\0\371\0\u013c\0" +
		"\371\0\u013e\0\371\0\u013f\0\371\0\u0144\0\371\0\u0154\0\371\0\u0157\0\371\0\u0159" +
		"\0\371\0\u015b\0\371\0\u015d\0\371\0\u015e\0\371\0\u0186\0\371\0\u0199\0\371\0\u019d" +
		"\0\371\0\u01a0\0\371\0\u01a2\0\371\0\u01ca\0\371\0\273\0\372\0\343\0\372\0\355\0" +
		"\372\0\356\0\372\0\360\0\372\0\374\0\372\0\u010a\0\372\0\u010c\0\372\0\u010d\0\372" +
		"\0\u0110\0\372\0\u012f\0\372\0\u0134\0\372\0\u0136\0\372\0\u0137\0\u0183\0\u0138" +
		"\0\372\0\u0139\0\372\0\u013b\0\372\0\u013c\0\372\0\u013e\0\372\0\u013f\0\372\0\u0144" +
		"\0\372\0\u0154\0\372\0\u0157\0\372\0\u0159\0\372\0\u015b\0\372\0\u015d\0\372\0\u015e" +
		"\0\372\0\u0186\0\372\0\u0199\0\372\0\u019d\0\372\0\u01a0\0\372\0\u01a2\0\372\0\u01ca" +
		"\0\372\0\273\0\373\0\323\0\u0119\0\324\0\u011a\0\343\0\373\0\355\0\373\0\356\0\373" +
		"\0\360\0\373\0\374\0\373\0\u010a\0\373\0\u010c\0\373\0\u010d\0\373\0\u0110\0\373" +
		"\0\u011b\0\u0167\0\u012f\0\373\0\u0134\0\373\0\u0136\0\373\0\u0137\0\373\0\u0138" +
		"\0\373\0\u0139\0\373\0\u013b\0\373\0\u013c\0\373\0\u013e\0\373\0\u013f\0\373\0\u0144" +
		"\0\373\0\u0154\0\373\0\u0157\0\373\0\u0159\0\373\0\u015b\0\373\0\u015d\0\373\0\u015e" +
		"\0\373\0\u0186\0\373\0\u0199\0\373\0\u019d\0\373\0\u01a0\0\373\0\u01a2\0\373\0\u01ca" +
		"\0\373\0\u0135\0\u017f\0\u017b\0\u017f\0\u017c\0\u01b6\0\u01b8\0\u017f\0\u01ba\0" +
		"\u017f\0\u0135\0\u0180\0\u017b\0\u01b5\0\u01b8\0\u01d1\0\u01ba\0\u01d2\0\177\0\232" +
		"\0\225\0\232\0\273\0\232\0\343\0\232\0\355\0\232\0\356\0\232\0\360\0\232\0\u010a" +
		"\0\232\0\u010c\0\232\0\u010d\0\232\0\u0110\0\232\0\u012f\0\232\0\u0134\0\232\0\u0139" +
		"\0\232\0\u013b\0\232\0\u013c\0\232\0\u013e\0\232\0\u0144\0\232\0\u0154\0\232\0\u0157" +
		"\0\232\0\u0159\0\232\0\u015b\0\232\0\u015d\0\232\0\u015e\0\232\0\u0186\0\232\0\u0199" +
		"\0\232\0\u019d\0\232\0\u01a0\0\232\0\u01a2\0\232\0\u01ca\0\232\0\177\0\233\0\225" +
		"\0\233\0\273\0\374\0\343\0\374\0\355\0\374\0\356\0\u013f\0\360\0\u013f\0\u010a\0" +
		"\374\0\u010c\0\374\0\u010d\0\374\0\u0110\0\374\0\u012f\0\u013f\0\u0134\0\374\0\u0139" +
		"\0\374\0\u013b\0\u013f\0\u013c\0\u013f\0\u013e\0\u013f\0\u0144\0\u013f\0\u0154\0" +
		"\374\0\u0157\0\374\0\u0159\0\374\0\u015b\0\374\0\u015d\0\374\0\u015e\0\374\0\u0186" +
		"\0\u013f\0\u0199\0\374\0\u019d\0\374\0\u01a0\0\374\0\u01a2\0\374\0\u01ca\0\374\0" +
		"\177\0\234\0\225\0\234\0\232\0\311\0\273\0\234\0\343\0\234\0\355\0\234\0\356\0\234" +
		"\0\360\0\234\0\u010a\0\234\0\u010c\0\234\0\u010d\0\234\0\u0110\0\234\0\u012f\0\234" +
		"\0\u0134\0\234\0\u0139\0\234\0\u013b\0\234\0\u013c\0\234\0\u013e\0\234\0\u0144\0" +
		"\234\0\u0154\0\234\0\u0157\0\234\0\u0159\0\234\0\u015b\0\234\0\u015d\0\234\0\u015e" +
		"\0\234\0\u0186\0\234\0\u0199\0\234\0\u019d\0\234\0\u01a0\0\234\0\u01a2\0\234\0\u01ca" +
		"\0\234\0\275\0\u0102\0\224\0\306\0\312\0\u0113\0\275\0\u0103\0\u014e\0\u0192\0\67" +
		"\0\107\0\104\0\133\0\105\0\134\0\136\0\107\0\275\0\u0104\0\342\0\u012b\0\u012a\0" +
		"\u016f\0\u014e\0\u0104\0\u0172\0\u012b\0\u0174\0\u012b\0\67\0\110\0\67\0\111\0\53" +
		"\0\70\0\350\0\70\0\u017d\0\70\0\u0181\0\70\0\67\0\112\0\136\0\157\0\222\0\270\0\267" +
		"\0\335\0\135\0\155\0\u0124\0\u016c\0\u016a\0\u01ac\0\u0191\0\u01bf\0\342\0\u012c" +
		"\0\u0172\0\u012c\0\u0174\0\u012c\0\342\0\u012d\0\u0172\0\u01b0\0\u0174\0\u01b1\0" +
		"\1\0\u01dd\0\6\0\63\0\101\0\126\0\145\0\176\0\150\0\200\0\337\0\u0126\0\6\0\64\0" +
		"\6\0\65\0\103\0\130\0\103\0\131\0\103\0\132\0\151\0\201\0\1\0\55\0\6\0\55\0\101\0" +
		"\55\0\135\0\156\0\145\0\55\0\150\0\55\0\337\0\55\0\u0124\0\156\0\u0146\0\u018f\0" +
		"\u016a\0\156\0\u0170\0\u01ae\0\u0171\0\u01af\0\u0191\0\156\0\2\0\60\0\12\0\66\0\71" +
		"\0\113\0\2\0\61\0\12\0\61\0\71\0\61\0\273\0\375\0\343\0\375\0\355\0\375\0\356\0\375" +
		"\0\360\0\375\0\u010a\0\375\0\u010c\0\375\0\u010d\0\375\0\u0110\0\375\0\u012f\0\375" +
		"\0\u0134\0\375\0\u0139\0\375\0\u013b\0\375\0\u013c\0\375\0\u013e\0\375\0\u0144\0" +
		"\375\0\u0154\0\375\0\u0157\0\375\0\u0159\0\375\0\u015b\0\375\0\u015d\0\375\0\u015e" +
		"\0\375\0\u0163\0\u01a8\0\u0186\0\375\0\u0199\0\375\0\u019d\0\375\0\u01a0\0\375\0" +
		"\u01a2\0\375\0\u01aa\0\u01a8\0\u01ca\0\375\0\1\0\56\0\6\0\56\0\77\0\125\0\101\0\56" +
		"\0\145\0\56\0\150\0\56\0\166\0\217\0\223\0\272\0\225\0\310\0\250\0\217\0\273\0\376" +
		"\0\337\0\56\0\343\0\376\0\360\0\u0142\0\u010a\0\376\0\u010c\0\376\0\u010d\0\376\0" +
		"\u0110\0\376\0\u012f\0\u0142\0\u0134\0\376\0\u0139\0\376\0\u013c\0\u0142\0\u013e" +
		"\0\u0142\0\u0154\0\376\0\u0157\0\376\0\u0159\0\376\0\u015b\0\376\0\u015d\0\376\0" +
		"\u015e\0\376\0\u0186\0\u0142\0\u0199\0\376\0\u019d\0\376\0\u01a0\0\376\0\u01a2\0" +
		"\376\0\u01ca\0\376\0\60\0\74\0\140\0\160\0\165\0\215\0\221\0\251\0\320\0\u0117\0" +
		"\u0118\0\u0164\0\u0117\0\u0163\0\u0164\0\u01aa\0\u0163\0\u01a9\0\u01aa\0\u01cd\0" +
		"\276\0\u0107\0\u0108\0\u0152\0\273\0\377\0\343\0\377\0\355\0\u013d\0\356\0\u0140" +
		"\0\360\0\u0143\0\u010a\0\377\0\u010c\0\377\0\u010d\0\377\0\u0110\0\377\0\u012f\0" +
		"\u0143\0\u0134\0\377\0\u0139\0\377\0\u013b\0\u0187\0\u013c\0\u0188\0\u013e\0\u018a" +
		"\0\u0154\0\377\0\u0157\0\377\0\u0159\0\377\0\u015b\0\377\0\u015d\0\377\0\u015e\0" +
		"\377\0\u0186\0\u01bb\0\u0199\0\377\0\u019d\0\377\0\u01a0\0\377\0\u01a2\0\377\0\u01ca" +
		"\0\377\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(276,
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\2\0\0\0\5\0\4\0\2\0\0\0\7\0\4\0" +
		"\3\0\3\0\4\0\4\0\3\0\3\0\1\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0" +
		"\3\0\4\0\3\0\3\0\3\0\1\0\10\0\4\0\7\0\3\0\3\0\1\0\1\0\1\0\1\0\5\0\3\0\1\0\4\0\4\0" +
		"\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\10\0\7\0\7\0\6\0\7\0\6\0\6\0\5\0\7\0\6\0\6\0\5\0" +
		"\6\0\5\0\5\0\4\0\2\0\3\0\2\0\1\0\1\0\1\0\2\0\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0" +
		"\4\0\4\0\4\0\4\0\5\0\5\0\6\0\3\0\1\0\3\0\1\0\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0" +
		"\5\0\4\0\4\0\3\0\4\0\3\0\3\0\2\0\4\0\3\0\3\0\2\0\3\0\2\0\2\0\1\0\1\0\3\0\2\0\3\0" +
		"\3\0\4\0\2\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\2\0\1\0\2\0\1\0\2\0\1\0" +
		"\3\0\3\0\1\0\2\0\1\0\3\0\3\0\3\0\1\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0" +
		"\1\0\3\0\2\0\1\0\3\0\3\0\2\0\1\0\1\0\4\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0" +
		"\1\0\1\0\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0" +
		"\1\0\5\0\3\0\1\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0" +
		"\1\0\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(276,
		"\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121" +
		"\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0" +
		"\121\0\121\0\121\0\121\0\121\0\122\0\122\0\123\0\123\0\124\0\124\0\125\0\125\0\126" +
		"\0\127\0\130\0\131\0\131\0\132\0\132\0\133\0\133\0\134\0\135\0\136\0\137\0\137\0" +
		"\137\0\140\0\140\0\140\0\140\0\140\0\141\0\142\0\143\0\143\0\144\0\144\0\145\0\145" +
		"\0\145\0\145\0\146\0\147\0\147\0\147\0\147\0\150\0\151\0\151\0\152\0\152\0\153\0" +
		"\154\0\155\0\155\0\155\0\156\0\156\0\156\0\157\0\157\0\157\0\157\0\157\0\157\0\157" +
		"\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\160\0\160\0\160\0\160\0" +
		"\160\0\160\0\161\0\162\0\162\0\162\0\163\0\163\0\163\0\164\0\164\0\164\0\164\0\165" +
		"\0\165\0\165\0\165\0\165\0\165\0\166\0\166\0\167\0\167\0\170\0\170\0\171\0\171\0" +
		"\172\0\172\0\173\0\173\0\174\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175" +
		"\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\175\0\176\0\177\0\200\0\200\0\201\0" +
		"\201\0\202\0\202\0\202\0\203\0\203\0\203\0\203\0\203\0\204\0\204\0\205\0\206\0\206" +
		"\0\207\0\210\0\210\0\211\0\211\0\211\0\212\0\212\0\213\0\213\0\213\0\214\0\215\0" +
		"\215\0\216\0\216\0\216\0\216\0\216\0\216\0\216\0\216\0\217\0\220\0\220\0\220\0\220" +
		"\0\221\0\221\0\221\0\222\0\222\0\223\0\224\0\224\0\224\0\225\0\225\0\226\0\227\0" +
		"\227\0\227\0\230\0\231\0\231\0\232\0\232\0\233\0\234\0\234\0\234\0\234\0\235\0\235" +
		"\0\236\0\236\0\237\0\237\0\237\0\237\0\240\0\240\0\240\0\241\0\241\0\241\0\241\0" +
		"\241\0\242\0\242\0\243\0\243\0\244\0\244\0\245\0\245\0\246\0\247\0\247\0\247\0\247" +
		"\0\250\0\251\0\251\0\252\0\253\0\254\0\254\0\255\0\255\0\256\0\256\0\257\0\257\0" +
		"\260\0\260\0\261\0\261\0\262\0\262\0");

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
		"'new'",
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
		"implements_clauseopt",
		"rhsSuffixopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int identifier = 81;
		int import__optlist = 82;
		int input1 = 83;
		int option_optlist = 84;
		int header = 85;
		int lexer_section = 86;
		int parser_section = 87;
		int parsing_algorithm = 88;
		int import_ = 89;
		int option = 90;
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
		int implements_clauseopt = 177;
		int rhsSuffixopt = 178;
	}

	public interface Rules {
		int nonterm_type_nontermTypeAST = 104;  // nonterm_type : 'returns' symref_noargs
		int nonterm_type_nontermTypeHint = 105;  // nonterm_type : 'inline' 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint2 = 106;  // nonterm_type : 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint3 = 107;  // nonterm_type : 'interface'
		int nonterm_type_nontermTypeHint4 = 108;  // nonterm_type : 'void'
		int directive_directivePrio = 121;  // directive : '%' assoc references ';'
		int directive_directiveInput = 122;  // directive : '%' 'input' inputref_list_Comma_separated ';'
		int directive_directiveInterface = 123;  // directive : '%' 'interface' identifier_list_Comma_separated ';'
		int directive_directiveAssert = 124;  // directive : '%' 'assert' 'empty' rhsSet ';'
		int directive_directiveAssert2 = 125;  // directive : '%' 'assert' 'nonempty' rhsSet ';'
		int directive_directiveSet = 126;  // directive : '%' 'generate' identifier '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 183;  // rhsOptional : rhsCast '?'
		int rhsCast_rhsAsLiteral = 186;  // rhsCast : rhsClass 'as' literal
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
		int expression_instance = 241;  // expression : 'new' name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 242;  // expression : '[' expression_list_Comma_separated_opt ']'
	}

	// set(follow error)
	private static int[] afterErr = {
		6, 7, 8, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 34, 35,
		36, 37, 42, 44, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
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
			case 32:  // import__optlist : import__optlist import_
				((List<TmaImport>)tmLeft.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 33:  // import__optlist :
				tmLeft.value = new ArrayList();
				break;
			case 34:  // input : header import__optlist option_optlist lexer_section parser_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexer */,
						((List<ITmaGrammarPart>)tmStack[tmHead].value) /* parser */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 35:  // input : header import__optlist option_optlist lexer_section
				tmLeft.value = new TmaInput1(
						((TmaHeader)tmStack[tmHead - 3].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 2].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 1].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead].value) /* lexer */,
						null /* parser */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 36:  // option_optlist : option_optlist option
				((List<TmaOption>)tmLeft.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 37:  // option_optlist :
				tmLeft.value = new ArrayList();
				break;
			case 38:  // header : 'language' name '(' name ')' parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 5].value) /* name */,
						((TmaName)tmStack[tmHead - 3].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 39:  // header : 'language' name parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 2].value) /* name */,
						null /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 40:  // lexer_section : '::' 'lexer' lexer_parts
				tmLeft.value = ((List<ITmaLexerPart>)tmStack[tmHead].value);
				break;
			case 41:  // parser_section : '::' 'parser' grammar_parts
				tmLeft.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value);
				break;
			case 42:  // parsing_algorithm : 'lalr' '(' icon ')'
				tmLeft.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 43:  // import_ : 'import' identifier scon ';'
				tmLeft.value = new TmaImport(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 44:  // import_ : 'import' scon ';'
				tmLeft.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 45:  // option : identifier '=' expression
				tmLeft.value = new TmaOption(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* key */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 46:  // option : syntax_problem
				tmLeft.value = new TmaOption(
						null /* key */,
						null /* value */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 47:  // symref : identifier symref_args
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((TmaSymrefArgs)tmStack[tmHead].value) /* args */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 48:  // symref : identifier
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // symref_noargs : identifier
				tmLeft.value = new TmaSymref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* args */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // rawType : code
				tmLeft.value = new TmaRawType(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 51:  // pattern : regexp
				tmLeft.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 52:  // lexer_parts : lexer_part
				tmLeft.value = new ArrayList();
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 53:  // lexer_parts : lexer_parts lexer_part
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 54:  // lexer_parts : lexer_parts syntax_problem
				((List<ITmaLexerPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 60:  // named_pattern : identifier '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 61:  // start_conditions_scope : start_conditions '{' lexer_parts '}'
				tmLeft.value = new TmaStartConditionsScope(
						((TmaStartConditions)tmStack[tmHead - 3].value) /* startConditions */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexerParts */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 62:  // start_conditions : '<' '*' '>'
				tmLeft.value = new TmaStartConditions(
						null /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 63:  // start_conditions : '<' stateref_list_Comma_separated '>'
				tmLeft.value = new TmaStartConditions(
						((List<TmaStateref>)tmStack[tmHead - 1].value) /* staterefListCommaSeparated */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 64:  // stateref_list_Comma_separated : stateref_list_Comma_separated ',' stateref
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 65:  // stateref_list_Comma_separated : stateref
				tmLeft.value = new ArrayList();
				((List<TmaStateref>)tmLeft.value).add(((TmaStateref)tmStack[tmHead].value));
				break;
			case 66:  // lexeme : start_conditions identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
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
			case 67:  // lexeme : start_conditions identifier rawTypeopt ':'
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
			case 68:  // lexeme : identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
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
			case 69:  // lexeme : identifier rawTypeopt ':'
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
			case 70:  // lexeme_attrs : '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // lexeme_attribute : 'soft'
				tmLeft.value = TmaLexemeAttribute.SOFT;
				break;
			case 72:  // lexeme_attribute : 'class'
				tmLeft.value = TmaLexemeAttribute.CLASS;
				break;
			case 73:  // lexeme_attribute : 'space'
				tmLeft.value = TmaLexemeAttribute.SPACE;
				break;
			case 74:  // lexeme_attribute : 'layout'
				tmLeft.value = TmaLexemeAttribute.LAYOUT;
				break;
			case 75:  // brackets_directive : '%' 'brackets' symref_noargs symref_noargs ';'
				tmLeft.value = new TmaBracketsDirective(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // lexer_state_list_Comma_separated : lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 77:  // lexer_state_list_Comma_separated : lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 78:  // states_clause : '%' 's' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						false /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 79:  // states_clause : '%' 'x' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						true /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // stateref : identifier
				tmLeft.value = new TmaStateref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 81:  // lexer_state : identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // grammar_parts : grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 83:  // grammar_parts : grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 84:  // grammar_parts : grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 88:  // nonterm : annotations identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 7].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // nonterm : annotations identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // nonterm : annotations identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // nonterm : annotations identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // nonterm : annotations identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // nonterm : annotations identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // nonterm : annotations identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 95:  // nonterm : annotations identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 96:  // nonterm : identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 97:  // nonterm : identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 98:  // nonterm : identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // nonterm : identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // nonterm : identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // nonterm : identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // nonterm : identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // nonterm : identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // nonterm_type : 'returns' symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // nonterm_type : 'inline' 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // nonterm_type : 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // nonterm_type : 'interface'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.INTERFACE /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // nonterm_type : 'void'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.VOID /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // implements_clause : 'implements' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 111:  // assoc : 'left'
				tmLeft.value = TmaAssoc.LEFT;
				break;
			case 112:  // assoc : 'right'
				tmLeft.value = TmaAssoc.RIGHT;
				break;
			case 113:  // assoc : 'nonassoc'
				tmLeft.value = TmaAssoc.NONASSOC;
				break;
			case 114:  // param_modifier : 'explicit'
				tmLeft.value = TmaParamModifier.EXPLICIT;
				break;
			case 115:  // param_modifier : 'global'
				tmLeft.value = TmaParamModifier.GLOBAL;
				break;
			case 116:  // param_modifier : 'lookahead'
				tmLeft.value = TmaParamModifier.LOOKAHEAD;
				break;
			case 117:  // template_param : '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // template_param : '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 119:  // template_param : '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // template_param : '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 121:  // directive : '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // directive : '%' 'input' inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // directive : '%' 'interface' identifier_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInterface(
						((List<TmaIdentifier>)tmStack[tmHead - 1].value) /* ids */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 124:  // directive : '%' 'assert' 'empty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.EMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 125:  // directive : '%' 'assert' 'nonempty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.NONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 126:  // directive : '%' 'generate' identifier '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // identifier_list_Comma_separated : identifier_list_Comma_separated ',' identifier
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 128:  // identifier_list_Comma_separated : identifier
				tmLeft.value = new ArrayList();
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 129:  // inputref_list_Comma_separated : inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 130:  // inputref_list_Comma_separated : inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 131:  // inputref : symref_noargs 'no-eoi'
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // inputref : symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // references : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 134:  // references : references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 135:  // references_cs : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 136:  // references_cs : references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 137:  // rule0_list_Or_separated : rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 138:  // rule0_list_Or_separated : rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 140:  // rule0 : predicate rhsPrefix rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 4].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rule0 : predicate rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // rule0 : predicate rhsPrefix rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // rule0 : predicate rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // rule0 : predicate rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // rule0 : predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 146:  // rule0 : predicate rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // rule0 : predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // rule0 : rhsPrefix rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // rule0 : rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rule0 : rhsPrefix rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rule0 : rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rule0 : rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rule0 : rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rule0 : rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rule0 : rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rule0 : syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* suffix */,
						null /* action */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // predicate : '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 158:  // rhsPrefix : annotations ':'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
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
			case 171:  // lookahead_predicate_list_And_separated : lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 172:  // lookahead_predicate_list_And_separated : lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 173:  // rhsLookahead : '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 174:  // lookahead_predicate : '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // lookahead_predicate : symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 176:  // rhsStateMarker : '.' identifier
				tmLeft.value = new TmaRhsStateMarker(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 178:  // rhsAnnotated : annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 180:  // rhsAssignment : identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 181:  // rhsAssignment : identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 183:  // rhsOptional : rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // rhsCast : rhsClass 'as' symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // rhsCast : rhsClass 'as' literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // rhsUnordered : rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 189:  // rhsClass : identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
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
			case 223:  // symref_args : '<' argument_list_Comma_separated_opt '>'
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
			case 241:  // expression : 'new' name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 242:  // expression : '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 244:  // expression_list_Comma_separated : expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 245:  // expression_list_Comma_separated : expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 248:  // map_entry_list_Comma_separated : map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 249:  // map_entry_list_Comma_separated : map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 252:  // map_entry : identifier ':' expression
				tmLeft.value = new TmaMapEntry(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 253:  // literal : scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 254:  // literal : icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 255:  // literal : 'true'
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 256:  // literal : 'false'
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 257:  // name : qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 258:  // qualified_id : identifier
				{ tmLeft.value = ((TmaIdentifier)tmStack[tmHead].value).getText(); }
				break;
			case 259:  // qualified_id : qualified_id '.' identifier
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((TmaIdentifier)tmStack[tmHead].value).getText(); }
				break;
			case 260:  // command : code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 261:  // syntax_problem : error
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
		return (TmaInput1) parse(lexer, 0, 478);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 479);
	}
}
