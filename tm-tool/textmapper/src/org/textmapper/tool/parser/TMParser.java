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
	private static final int[] tmAction = TMLexer.unpack_int(422,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\326\0\327\0\uffb9\uffff\336\0\uff6b" +
		"\uffff\330\0\331\0\uffff\uffff\311\0\310\0\314\0\333\0\ufefd\uffff\ufef5\uffff\ufee9" +
		"\uffff\316\0\ufea5\uffff\uffff\uffff\ufe9f\uffff\20\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\337\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\313" +
		"\0\uffff\uffff\uffff\uffff\17\0\263\0\ufe5b\uffff\ufe53\uffff\uffff\uffff\265\0\ufe4d" +
		"\uffff\uffff\uffff\uffff\uffff\7\0\334\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufe0d\uffff\4\0\16\0\315\0\272\0\273\0\uffff\uffff\uffff\uffff\270\0\uffff" +
		"\uffff\ufe07\uffff\uffff\uffff\322\0\ufe01\uffff\uffff\uffff\14\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\2\0\22\0\300\0\271\0\277\0\264\0\uffff\uffff\uffff" +
		"\uffff\312\0\uffff\uffff\12\0\13\0\uffff\uffff\uffff\uffff\ufdfb\uffff\ufdf3\uffff" +
		"\ufded\uffff\45\0\51\0\52\0\53\0\50\0\15\0\uffff\uffff\325\0\321\0\6\0\uffff\uffff" +
		"\ufda5\uffff\uffff\uffff\67\0\uffff\uffff\uffff\uffff\341\0\uffff\uffff\46\0\47\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufd9d\uffff\74\0\77\0\100\0\101\0\ufd57\uffff" +
		"\uffff\uffff\250\0\uffff\uffff\uffff\uffff\uffff\uffff\70\0\44\0\54\0\uffff\uffff" +
		"\35\0\36\0\31\0\32\0\uffff\uffff\27\0\30\0\34\0\37\0\41\0\40\0\33\0\uffff\uffff\26" +
		"\0\ufd0f\uffff\uffff\uffff\121\0\122\0\123\0\uffff\uffff\uffff\uffff\125\0\124\0" +
		"\126\0\276\0\275\0\uffff\uffff\uffff\uffff\uffff\uffff\ufcc5\uffff\254\0\ufc7b\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufc25\uffff\ufbe3\uffff\116\0\117\0\uffff\uffff" +
		"\uffff\uffff\75\0\76\0\247\0\uffff\uffff\uffff\uffff\71\0\72\0\66\0\23\0\43\0\uffff" +
		"\uffff\24\0\25\0\ufba1\uffff\ufb51\uffff\uffff\uffff\141\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\144\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\ufb49\uffff\uffff\uffff\uffff\uffff\ufaf3\uffff\uffff\uffff\335" +
		"\0\uffff\uffff\227\0\ufa89\uffff\uffff\uffff\151\0\ufa81\uffff\ufa2d\uffff\357\0" +
		"\uf9d9\uffff\uf9cf\uffff\uf979\uffff\210\0\205\0\212\0\214\0\uf91f\uffff\206\0\uf8c3" +
		"\uffff\uf865\uffff\236\0\uffff\uffff\207\0\173\0\172\0\uf803\uffff\uffff\uffff\256" +
		"\0\260\0\uf7c1\uffff\112\0\353\0\uf77f\uffff\uf779\uffff\uf773\uffff\uf71d\uffff" +
		"\uffff\uffff\uf6c7\uffff\uffff\uffff\uffff\uffff\65\0\42\0\uffff\uffff\343\0\uf671" +
		"\uffff\142\0\134\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\133\0\145\0\uffff" +
		"\uffff\uffff\uffff\132\0\uffff\uffff\uffff\uffff\uffff\uffff\211\0\uffff\uffff\uf623" +
		"\uffff\305\0\uffff\uffff\uffff\uffff\uf617\uffff\uffff\uffff\uf5c1\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uf56b\uffff\111\0\uf513\uffff\uf4bf\uffff\uf4b5" +
		"\uffff\162\0\uf45f\uffff\uf455\uffff\uffff\uffff\166\0\171\0\uf3ff\uffff\uf3f5\uffff" +
		"\204\0\170\0\uffff\uffff\220\0\uffff\uffff\233\0\234\0\175\0\213\0\uf39b\uffff\uffff" +
		"\uffff\257\0\uf393\uffff\uffff\uffff\355\0\114\0\115\0\uffff\uffff\uffff\uffff\uf38d" +
		"\uffff\uffff\uffff\uf337\uffff\uf2e1\uffff\uffff\uffff\57\0\345\0\uf28b\uffff\140" +
		"\0\uffff\uffff\135\0\136\0\uffff\uffff\130\0\uffff\uffff\252\0\176\0\177\0\301\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\174\0\uffff\uffff\230\0\uffff\uffff\uffff\uffff" +
		"\201\0\uffff\uffff\uffff\uffff\uffff\uffff\uf23f\uffff\241\0\244\0\uffff\uffff\uffff" +
		"\uffff\215\0\uf1f9\uffff\216\0\150\0\uf197\uffff\uf18d\uffff\156\0\161\0\uf137\uffff" +
		"\160\0\165\0\uf12d\uffff\164\0\167\0\uf123\uffff\222\0\223\0\uffff\uffff\255\0\113" +
		"\0\146\0\uf0c9\uffff\110\0\107\0\uffff\uffff\105\0\uffff\uffff\uffff\uffff\uf0c3" +
		"\uffff\uffff\uffff\347\0\uf06d\uffff\137\0\uffff\uffff\131\0\303\0\304\0\uf023\uffff" +
		"\uf01b\uffff\uffff\uffff\200\0\235\0\uffff\uffff\243\0\240\0\uffff\uffff\237\0\uffff" +
		"\uffff\155\0\uf013\uffff\154\0\157\0\163\0\261\0\uffff\uffff\106\0\104\0\103\0\uffff" +
		"\uffff\61\0\62\0\63\0\64\0\uffff\uffff\351\0\55\0\127\0\uffff\uffff\242\0\uf009\uffff" +
		"\uf001\uffff\153\0\147\0\102\0\60\0\232\0\231\0\uffff\uffff\uffff\uffff\ufffe\uffff" +
		"\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4100,
		"\11\0\1\0\46\0\1\0\47\0\1\0\55\0\1\0\57\0\1\0\60\0\1\0\61\0\1\0\62\0\1\0\63\0\1\0" +
		"\64\0\1\0\65\0\1\0\66\0\1\0\67\0\1\0\70\0\1\0\71\0\1\0\72\0\1\0\73\0\1\0\74\0\1\0" +
		"\75\0\1\0\76\0\1\0\77\0\1\0\100\0\1\0\101\0\1\0\102\0\1\0\103\0\1\0\104\0\1\0\105" +
		"\0\1\0\106\0\1\0\107\0\1\0\110\0\1\0\111\0\1\0\112\0\1\0\113\0\1\0\uffff\uffff\ufffe" +
		"\uffff\2\0\uffff\uffff\3\0\uffff\uffff\24\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\50\0\uffff\uffff\51\0\uffff\uffff\52\0\uffff\uffff\25\0\320\0\uffff" +
		"\uffff\ufffe\uffff\32\0\uffff\uffff\0\0\21\0\7\0\21\0\11\0\21\0\12\0\21\0\20\0\21" +
		"\0\21\0\21\0\22\0\21\0\25\0\21\0\26\0\21\0\27\0\21\0\30\0\21\0\31\0\21\0\34\0\21" +
		"\0\35\0\21\0\37\0\21\0\42\0\21\0\44\0\21\0\45\0\21\0\46\0\21\0\47\0\21\0\53\0\21" +
		"\0\54\0\21\0\56\0\21\0\57\0\21\0\60\0\21\0\61\0\21\0\62\0\21\0\63\0\21\0\64\0\21" +
		"\0\65\0\21\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0\72\0\21\0\73\0\21\0\74\0\21" +
		"\0\75\0\21\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21\0\102\0\21\0\103\0\21\0\104" +
		"\0\21\0\105\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111\0\21\0\112\0\21\0\113\0\21" +
		"\0\115\0\21\0\uffff\uffff\ufffe\uffff\26\0\uffff\uffff\111\0\uffff\uffff\20\0\340" +
		"\0\uffff\uffff\ufffe\uffff\21\0\uffff\uffff\20\0\332\0\26\0\332\0\27\0\332\0\111" +
		"\0\332\0\uffff\uffff\ufffe\uffff\55\0\uffff\uffff\11\0\5\0\46\0\5\0\47\0\5\0\57\0" +
		"\5\0\60\0\5\0\61\0\5\0\62\0\5\0\63\0\5\0\64\0\5\0\65\0\5\0\66\0\5\0\67\0\5\0\70\0" +
		"\5\0\71\0\5\0\72\0\5\0\73\0\5\0\74\0\5\0\75\0\5\0\76\0\5\0\77\0\5\0\100\0\5\0\101" +
		"\0\5\0\102\0\5\0\103\0\5\0\104\0\5\0\105\0\5\0\106\0\5\0\107\0\5\0\110\0\5\0\111" +
		"\0\5\0\112\0\5\0\113\0\5\0\uffff\uffff\ufffe\uffff\22\0\uffff\uffff\25\0\317\0\uffff" +
		"\uffff\ufffe\uffff\35\0\uffff\uffff\41\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\33\0\267\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\22\0\274\0\33\0\274\0\uffff" +
		"\uffff\ufffe\uffff\22\0\uffff\uffff\33\0\266\0\uffff\uffff\ufffe\uffff\47\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\27\0\324\0\uffff\uffff\ufffe\uffff\11\0\uffff\uffff\0\0\3\0\uffff" +
		"\uffff\ufffe\uffff\22\0\uffff\uffff\27\0\323\0\uffff\uffff\ufffe\uffff\111\0\uffff" +
		"\uffff\20\0\340\0\uffff\uffff\ufffe\uffff\14\0\uffff\uffff\23\0\17\0\26\0\17\0\uffff" +
		"\uffff\ufffe\uffff\26\0\uffff\uffff\23\0\342\0\uffff\uffff\ufffe\uffff\7\0\uffff" +
		"\uffff\24\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0" +
		"\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff" +
		"\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\0\0\10\0\11" +
		"\0\10\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\22\0\73\0\25\0\73\0\uffff\uffff" +
		"\ufffe\uffff\7\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\0\0\11\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\23\0\251\0\26\0\251\0\44\0\251" +
		"\0\47\0\251\0\56\0\251\0\57\0\251\0\60\0\251\0\61\0\251\0\62\0\251\0\63\0\251\0\64" +
		"\0\251\0\65\0\251\0\66\0\251\0\67\0\251\0\70\0\251\0\71\0\251\0\72\0\251\0\73\0\251" +
		"\0\74\0\251\0\75\0\251\0\76\0\251\0\77\0\251\0\100\0\251\0\101\0\251\0\102\0\251" +
		"\0\103\0\251\0\104\0\251\0\105\0\251\0\106\0\251\0\107\0\251\0\110\0\251\0\111\0" +
		"\251\0\112\0\251\0\113\0\251\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\0\0\56\0\7" +
		"\0\56\0\11\0\56\0\24\0\56\0\46\0\56\0\47\0\56\0\57\0\56\0\60\0\56\0\61\0\56\0\62" +
		"\0\56\0\63\0\56\0\64\0\56\0\65\0\56\0\66\0\56\0\67\0\56\0\70\0\56\0\71\0\56\0\72" +
		"\0\56\0\73\0\56\0\74\0\56\0\75\0\56\0\76\0\56\0\77\0\56\0\100\0\56\0\101\0\56\0\102" +
		"\0\56\0\103\0\56\0\104\0\56\0\105\0\56\0\106\0\56\0\107\0\56\0\110\0\56\0\111\0\56" +
		"\0\112\0\56\0\113\0\56\0\uffff\uffff\ufffe\uffff\116\0\uffff\uffff\23\0\253\0\26" +
		"\0\253\0\44\0\253\0\45\0\253\0\47\0\253\0\56\0\253\0\57\0\253\0\60\0\253\0\61\0\253" +
		"\0\62\0\253\0\63\0\253\0\64\0\253\0\65\0\253\0\66\0\253\0\67\0\253\0\70\0\253\0\71" +
		"\0\253\0\72\0\253\0\73\0\253\0\74\0\253\0\75\0\253\0\76\0\253\0\77\0\253\0\100\0" +
		"\253\0\101\0\253\0\102\0\253\0\103\0\253\0\104\0\253\0\105\0\253\0\106\0\253\0\107" +
		"\0\253\0\110\0\253\0\111\0\253\0\112\0\253\0\113\0\253\0\uffff\uffff\ufffe\uffff" +
		"\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff\uffff\ufffe\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\10\0\354\0\23\0\354\0\uffff\uffff\ufffe\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\10\0\354\0\23\0\354\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\0\0\344\0\3\0\344" +
		"\0\7\0\344\0\11\0\344\0\24\0\344\0\26\0\344\0\46\0\344\0\47\0\344\0\57\0\344\0\60" +
		"\0\344\0\61\0\344\0\62\0\344\0\63\0\344\0\64\0\344\0\65\0\344\0\66\0\344\0\67\0\344" +
		"\0\70\0\344\0\71\0\344\0\72\0\344\0\73\0\344\0\74\0\344\0\75\0\344\0\76\0\344\0\77" +
		"\0\344\0\100\0\344\0\101\0\344\0\102\0\344\0\103\0\344\0\104\0\344\0\105\0\344\0" +
		"\106\0\344\0\107\0\344\0\110\0\344\0\111\0\344\0\112\0\344\0\113\0\344\0\115\0\344" +
		"\0\uffff\uffff\ufffe\uffff\101\0\uffff\uffff\20\0\143\0\22\0\143\0\uffff\uffff\ufffe" +
		"\uffff\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0" +
		"\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff" +
		"\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff" +
		"\32\0\uffff\uffff\14\0\17\0\23\0\17\0\36\0\17\0\7\0\21\0\12\0\21\0\20\0\21\0\21\0" +
		"\21\0\26\0\21\0\27\0\21\0\30\0\21\0\34\0\21\0\35\0\21\0\37\0\21\0\42\0\21\0\44\0" +
		"\21\0\45\0\21\0\46\0\21\0\47\0\21\0\53\0\21\0\54\0\21\0\56\0\21\0\57\0\21\0\60\0" +
		"\21\0\61\0\21\0\62\0\21\0\63\0\21\0\64\0\21\0\65\0\21\0\66\0\21\0\67\0\21\0\70\0" +
		"\21\0\71\0\21\0\72\0\21\0\73\0\21\0\74\0\21\0\75\0\21\0\76\0\21\0\77\0\21\0\100\0" +
		"\21\0\101\0\21\0\102\0\21\0\103\0\21\0\104\0\21\0\105\0\21\0\106\0\21\0\107\0\21" +
		"\0\110\0\21\0\111\0\21\0\112\0\21\0\113\0\21\0\115\0\21\0\uffff\uffff\ufffe\uffff" +
		"\12\0\uffff\uffff\20\0\152\0\27\0\152\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21" +
		"\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360" +
		"\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff\26\0\uffff\uffff\30\0" +
		"\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff" +
		"\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff" +
		"\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101" +
		"\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff" +
		"\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\115\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff" +
		"\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21" +
		"\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff\uffff\12\0" +
		"\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\42\0\uffff\uffff\7\0\202\0\12" +
		"\0\202\0\20\0\202\0\21\0\202\0\26\0\202\0\27\0\202\0\30\0\202\0\44\0\202\0\45\0\202" +
		"\0\46\0\202\0\47\0\202\0\53\0\202\0\56\0\202\0\57\0\202\0\60\0\202\0\61\0\202\0\62" +
		"\0\202\0\63\0\202\0\64\0\202\0\65\0\202\0\66\0\202\0\67\0\202\0\70\0\202\0\71\0\202" +
		"\0\72\0\202\0\73\0\202\0\74\0\202\0\75\0\202\0\76\0\202\0\77\0\202\0\100\0\202\0" +
		"\101\0\202\0\102\0\202\0\103\0\202\0\104\0\202\0\105\0\202\0\106\0\202\0\107\0\202" +
		"\0\110\0\202\0\111\0\202\0\112\0\202\0\113\0\202\0\115\0\202\0\uffff\uffff\ufffe" +
		"\uffff\37\0\uffff\uffff\7\0\217\0\12\0\217\0\20\0\217\0\21\0\217\0\26\0\217\0\27" +
		"\0\217\0\30\0\217\0\42\0\217\0\44\0\217\0\45\0\217\0\46\0\217\0\47\0\217\0\53\0\217" +
		"\0\56\0\217\0\57\0\217\0\60\0\217\0\61\0\217\0\62\0\217\0\63\0\217\0\64\0\217\0\65" +
		"\0\217\0\66\0\217\0\67\0\217\0\70\0\217\0\71\0\217\0\72\0\217\0\73\0\217\0\74\0\217" +
		"\0\75\0\217\0\76\0\217\0\77\0\217\0\100\0\217\0\101\0\217\0\102\0\217\0\103\0\217" +
		"\0\104\0\217\0\105\0\217\0\106\0\217\0\107\0\217\0\110\0\217\0\111\0\217\0\112\0" +
		"\217\0\113\0\217\0\115\0\217\0\uffff\uffff\ufffe\uffff\54\0\uffff\uffff\7\0\221\0" +
		"\12\0\221\0\20\0\221\0\21\0\221\0\26\0\221\0\27\0\221\0\30\0\221\0\37\0\221\0\42" +
		"\0\221\0\44\0\221\0\45\0\221\0\46\0\221\0\47\0\221\0\53\0\221\0\56\0\221\0\57\0\221" +
		"\0\60\0\221\0\61\0\221\0\62\0\221\0\63\0\221\0\64\0\221\0\65\0\221\0\66\0\221\0\67" +
		"\0\221\0\70\0\221\0\71\0\221\0\72\0\221\0\73\0\221\0\74\0\221\0\75\0\221\0\76\0\221" +
		"\0\77\0\221\0\100\0\221\0\101\0\221\0\102\0\221\0\103\0\221\0\104\0\221\0\105\0\221" +
		"\0\106\0\221\0\107\0\221\0\110\0\221\0\111\0\221\0\112\0\221\0\113\0\221\0\115\0" +
		"\221\0\uffff\uffff\ufffe\uffff\34\0\uffff\uffff\35\0\uffff\uffff\7\0\225\0\12\0\225" +
		"\0\20\0\225\0\21\0\225\0\26\0\225\0\27\0\225\0\30\0\225\0\37\0\225\0\42\0\225\0\44" +
		"\0\225\0\45\0\225\0\46\0\225\0\47\0\225\0\53\0\225\0\54\0\225\0\56\0\225\0\57\0\225" +
		"\0\60\0\225\0\61\0\225\0\62\0\225\0\63\0\225\0\64\0\225\0\65\0\225\0\66\0\225\0\67" +
		"\0\225\0\70\0\225\0\71\0\225\0\72\0\225\0\73\0\225\0\74\0\225\0\75\0\225\0\76\0\225" +
		"\0\77\0\225\0\100\0\225\0\101\0\225\0\102\0\225\0\103\0\225\0\104\0\225\0\105\0\225" +
		"\0\106\0\225\0\107\0\225\0\110\0\225\0\111\0\225\0\112\0\225\0\113\0\225\0\115\0" +
		"\225\0\uffff\uffff\ufffe\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff" +
		"\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\22\0\17\0\33\0\17\0\uffff\uffff" +
		"\ufffe\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff" +
		"\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\10\0\354\0\23\0\354\0\uffff\uffff\ufffe\uffff" +
		"\23\0\uffff\uffff\10\0\356\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\10\0\356\0" +
		"\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff" +
		"\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff" +
		"\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff\uffff\ufffe\uffff" +
		"\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff\uffff\ufffe\uffff" +
		"\3\0\uffff\uffff\0\0\346\0\7\0\346\0\11\0\346\0\24\0\346\0\26\0\346\0\46\0\346\0" +
		"\47\0\346\0\57\0\346\0\60\0\346\0\61\0\346\0\62\0\346\0\63\0\346\0\64\0\346\0\65" +
		"\0\346\0\66\0\346\0\67\0\346\0\70\0\346\0\71\0\346\0\72\0\346\0\73\0\346\0\74\0\346" +
		"\0\75\0\346\0\76\0\346\0\77\0\346\0\100\0\346\0\101\0\346\0\102\0\346\0\103\0\346" +
		"\0\104\0\346\0\105\0\346\0\106\0\346\0\107\0\346\0\110\0\346\0\111\0\346\0\112\0" +
		"\346\0\113\0\346\0\115\0\346\0\uffff\uffff\ufffe\uffff\15\0\uffff\uffff\16\0\uffff" +
		"\uffff\13\0\302\0\25\0\302\0\43\0\302\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21" +
		"\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\53\0\uffff\uffff\56\0\uffff\uffff\115\0" +
		"\uffff\uffff\12\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff" +
		"\uffff\24\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff" +
		"\uffff\12\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff" +
		"\24\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff\uffff\12\0" +
		"\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff" +
		"\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff" +
		"\ufffe\uffff\7\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff" +
		"\7\0\uffff\uffff\21\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\115\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff" +
		"\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21" +
		"\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff\uffff\12\0" +
		"\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\12\0\360\0\20" +
		"\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\42\0\uffff\uffff\7\0\203\0\12\0\203\0" +
		"\20\0\203\0\21\0\203\0\26\0\203\0\27\0\203\0\30\0\203\0\44\0\203\0\45\0\203\0\46" +
		"\0\203\0\47\0\203\0\53\0\203\0\56\0\203\0\57\0\203\0\60\0\203\0\61\0\203\0\62\0\203" +
		"\0\63\0\203\0\64\0\203\0\65\0\203\0\66\0\203\0\67\0\203\0\70\0\203\0\71\0\203\0\72" +
		"\0\203\0\73\0\203\0\74\0\203\0\75\0\203\0\76\0\203\0\77\0\203\0\100\0\203\0\101\0" +
		"\203\0\102\0\203\0\103\0\203\0\104\0\203\0\105\0\203\0\106\0\203\0\107\0\203\0\110" +
		"\0\203\0\111\0\203\0\112\0\203\0\113\0\203\0\115\0\203\0\uffff\uffff\ufffe\uffff" +
		"\14\0\uffff\uffff\22\0\262\0\33\0\262\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff" +
		"\10\0\356\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff" +
		"\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff" +
		"\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff\uffff\ufffe\uffff" +
		"\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff\uffff\ufffe\uffff" +
		"\26\0\uffff\uffff\0\0\350\0\7\0\350\0\11\0\350\0\24\0\350\0\46\0\350\0\47\0\350\0" +
		"\57\0\350\0\60\0\350\0\61\0\350\0\62\0\350\0\63\0\350\0\64\0\350\0\65\0\350\0\66" +
		"\0\350\0\67\0\350\0\70\0\350\0\71\0\350\0\72\0\350\0\73\0\350\0\74\0\350\0\75\0\350" +
		"\0\76\0\350\0\77\0\350\0\100\0\350\0\101\0\350\0\102\0\350\0\103\0\350\0\104\0\350" +
		"\0\105\0\350\0\106\0\350\0\107\0\350\0\110\0\350\0\111\0\350\0\112\0\350\0\113\0" +
		"\350\0\115\0\350\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\47\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\12\0\21\0\27\0\21\0\42\0\21\0\uffff\uffff\ufffe\uffff\34\0\uffff\uffff\35\0\uffff" +
		"\uffff\7\0\226\0\12\0\226\0\20\0\226\0\21\0\226\0\26\0\226\0\27\0\226\0\30\0\226" +
		"\0\37\0\226\0\42\0\226\0\44\0\226\0\45\0\226\0\46\0\226\0\47\0\226\0\53\0\226\0\54" +
		"\0\226\0\56\0\226\0\57\0\226\0\60\0\226\0\61\0\226\0\62\0\226\0\63\0\226\0\64\0\226" +
		"\0\65\0\226\0\66\0\226\0\67\0\226\0\70\0\226\0\71\0\226\0\72\0\226\0\73\0\226\0\74" +
		"\0\226\0\75\0\226\0\76\0\226\0\77\0\226\0\100\0\226\0\101\0\226\0\102\0\226\0\103" +
		"\0\226\0\104\0\226\0\105\0\226\0\106\0\226\0\107\0\226\0\110\0\226\0\111\0\226\0" +
		"\112\0\226\0\113\0\226\0\115\0\226\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\12\0" +
		"\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\21\0\uffff\uffff" +
		"\26\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360" +
		"\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff" +
		"\uffff\ufffe\uffff\7\0\uffff\uffff\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe" +
		"\uffff\42\0\224\0\7\0\224\0\12\0\224\0\20\0\224\0\21\0\224\0\26\0\224\0\27\0\224" +
		"\0\30\0\224\0\44\0\224\0\45\0\224\0\46\0\224\0\47\0\224\0\53\0\224\0\56\0\224\0\57" +
		"\0\224\0\60\0\224\0\61\0\224\0\62\0\224\0\63\0\224\0\64\0\224\0\65\0\224\0\66\0\224" +
		"\0\67\0\224\0\70\0\224\0\71\0\224\0\72\0\224\0\73\0\224\0\74\0\224\0\75\0\224\0\76" +
		"\0\224\0\77\0\224\0\100\0\224\0\101\0\224\0\102\0\224\0\103\0\224\0\104\0\224\0\105" +
		"\0\224\0\106\0\224\0\107\0\224\0\110\0\224\0\111\0\224\0\112\0\224\0\113\0\224\0" +
		"\115\0\224\0\uffff\uffff\ufffe\uffff\22\0\uffff\uffff\10\0\120\0\uffff\uffff\ufffe" +
		"\uffff\7\0\uffff\uffff\21\0\uffff\uffff\24\0\uffff\uffff\26\0\uffff\uffff\30\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0" +
		"\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff" +
		"\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\115\0\uffff\uffff\12\0\360\0\20\0\360\0\uffff\uffff\ufffe\uffff" +
		"\115\0\uffff\uffff\0\0\352\0\7\0\352\0\11\0\352\0\24\0\352\0\46\0\352\0\47\0\352" +
		"\0\57\0\352\0\60\0\352\0\61\0\352\0\62\0\352\0\63\0\352\0\64\0\352\0\65\0\352\0\66" +
		"\0\352\0\67\0\352\0\70\0\352\0\71\0\352\0\72\0\352\0\73\0\352\0\74\0\352\0\75\0\352" +
		"\0\76\0\352\0\77\0\352\0\100\0\352\0\101\0\352\0\102\0\352\0\103\0\352\0\104\0\352" +
		"\0\105\0\352\0\106\0\352\0\107\0\352\0\110\0\352\0\111\0\352\0\112\0\352\0\113\0" +
		"\352\0\uffff\uffff\ufffe\uffff\13\0\307\0\43\0\uffff\uffff\25\0\307\0\uffff\uffff" +
		"\ufffe\uffff\13\0\306\0\43\0\306\0\25\0\306\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff" +
		"\12\0\360\0\20\0\360\0\27\0\360\0\uffff\uffff\ufffe\uffff\12\0\245\0\42\0\uffff\uffff" +
		"\27\0\245\0\uffff\uffff\ufffe\uffff\12\0\246\0\42\0\246\0\27\0\246\0\uffff\uffff" +
		"\ufffe\uffff");

	private static final int[] lapg_sym_goto = TMLexer.unpack_int(176,
		"\0\0\2\0\4\0\25\0\44\0\44\0\44\0\44\0\103\0\113\0\115\0\122\0\125\0\134\0\135\0\136" +
		"\0\140\0\166\0\217\0\232\0\243\0\272\0\301\0\356\0\373\0\u010e\0\u0111\0\u011a\0" +
		"\u0120\0\u0127\0\u012c\0\u012d\0\u0132\0\u0135\0\u013c\0\u0147\0\u014a\0\u0163\0" +
		"\u017e\0\u0198\0\u01f8\0\u0205\0\u0212\0\u0218\0\u0219\0\u021a\0\u021b\0\u0237\0" +
		"\u0298\0\u02fc\0\u035d\0\u03be\0\u0422\0\u0483\0\u04e4\0\u0545\0\u05a6\0\u0607\0" +
		"\u0668\0\u06c9\0\u072a\0\u078b\0\u07ec\0\u084d\0\u08af\0\u0911\0\u0972\0\u09d3\0" +
		"\u0a39\0\u0a9d\0\u0b01\0\u0b62\0\u0bc3\0\u0c24\0\u0c86\0\u0ce7\0\u0d48\0\u0d48\0" +
		"\u0d5d\0\u0d5e\0\u0d5f\0\u0d60\0\u0d61\0\u0d62\0\u0d63\0\u0d64\0\u0d66\0\u0d67\0" +
		"\u0d68\0\u0d98\0\u0dbe\0\u0dcf\0\u0dd4\0\u0dd6\0\u0dda\0\u0ddc\0\u0ddd\0\u0ddf\0" +
		"\u0de1\0\u0de3\0\u0de4\0\u0de5\0\u0de6\0\u0de8\0\u0de9\0\u0deb\0\u0ded\0\u0def\0" +
		"\u0df0\0\u0df2\0\u0df4\0\u0df8\0\u0dfb\0\u0dfc\0\u0dfd\0\u0dff\0\u0e01\0\u0e02\0" +
		"\u0e04\0\u0e06\0\u0e07\0\u0e11\0\u0e1b\0\u0e26\0\u0e31\0\u0e3d\0\u0e58\0\u0e6b\0" +
		"\u0e79\0\u0e8d\0\u0ea1\0\u0eb5\0\u0ecb\0\u0ee3\0\u0efb\0\u0f0f\0\u0f27\0\u0f40\0" +
		"\u0f5c\0\u0f61\0\u0f65\0\u0f7b\0\u0f91\0\u0fa8\0\u0fa9\0\u0fab\0\u0fad\0\u0fb7\0" +
		"\u0fb8\0\u0fb9\0\u0fbc\0\u0fbe\0\u0fc0\0\u0fc4\0\u0fc7\0\u0fca\0\u0fd0\0\u0fd1\0" +
		"\u0fd2\0\u0fd3\0\u0fd4\0\u0fd6\0\u0fe3\0\u0fe6\0\u0fe9\0\u0ffe\0\u1018\0\u101a\0" +
		"\u101b\0\u101c\0\u101d\0\u101e\0\u101f\0\u1022\0\u1025\0\u1040\0");

	private static final int[] lapg_sym_from = TMLexer.unpack_int(4160,
		"\u01a2\0\u01a3\0\147\0\215\0\1\0\6\0\36\0\41\0\61\0\72\0\106\0\116\0\150\0\300\0" +
		"\377\0\u010b\0\u0122\0\u013d\0\u0144\0\u0145\0\u0166\0\1\0\6\0\41\0\55\0\72\0\106" +
		"\0\116\0\300\0\365\0\377\0\u0122\0\u013d\0\u0144\0\u0145\0\u0166\0\105\0\130\0\137" +
		"\0\160\0\236\0\304\0\317\0\320\0\322\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111" +
		"\0\u0113\0\u0114\0\u0115\0\u0117\0\u0118\0\u011c\0\u0131\0\u0133\0\u0134\0\u0159" +
		"\0\u015a\0\u015d\0\u0160\0\u0171\0\u0186\0\157\0\246\0\247\0\253\0\355\0\357\0\360" +
		"\0\u0135\0\37\0\64\0\314\0\u0153\0\u017f\0\u019a\0\u019b\0\u0108\0\u017a\0\u017b" +
		"\0\63\0\126\0\271\0\277\0\312\0\376\0\u0127\0\u0106\0\u0106\0\144\0\265\0\34\0\60" +
		"\0\104\0\121\0\254\0\267\0\275\0\277\0\315\0\372\0\373\0\376\0\u012f\0\u0130\0\u0132" +
		"\0\u013a\0\u013f\0\u016d\0\u016f\0\u0170\0\u0176\0\u018f\0\21\0\150\0\203\0\213\0" +
		"\236\0\262\0\304\0\317\0\320\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113" +
		"\0\u0115\0\u0118\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\24\0\50\0\76\0" +
		"\145\0\150\0\203\0\213\0\262\0\267\0\343\0\u016a\0\47\0\75\0\152\0\312\0\336\0\351" +
		"\0\352\0\u012a\0\u0154\0\1\0\6\0\41\0\105\0\106\0\116\0\130\0\150\0\203\0\213\0\236" +
		"\0\262\0\300\0\304\0\353\0\354\0\356\0\u010c\0\u0111\0\u0131\0\u0133\0\u0134\0\u0171" +
		"\0\25\0\145\0\150\0\203\0\213\0\262\0\u0108\0\20\0\30\0\32\0\127\0\150\0\157\0\203" +
		"\0\213\0\236\0\247\0\253\0\262\0\304\0\306\0\310\0\317\0\320\0\323\0\336\0\353\0" +
		"\354\0\356\0\360\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113" +
		"\0\u0115\0\u0118\0\u0119\0\u0120\0\u0131\0\u0133\0\u0134\0\u0138\0\u014e\0\u014f" +
		"\0\u015a\0\u0171\0\u0182\0\u0184\0\54\0\77\0\102\0\176\0\203\0\213\0\262\0\u0109" +
		"\0\u014d\0\u0153\0\u017c\0\u017f\0\u0194\0\236\0\304\0\317\0\320\0\323\0\353\0\354" +
		"\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115\0\u0118\0\u0131\0\u0133\0\u0134\0" +
		"\u015a\0\u0171\0\u0101\0\u010b\0\u014b\0\10\0\150\0\157\0\203\0\213\0\253\0\262\0" +
		"\307\0\u0150\0\51\0\150\0\203\0\213\0\262\0\343\0\150\0\203\0\213\0\262\0\334\0\u0156" +
		"\0\u0198\0\26\0\73\0\334\0\u0156\0\u0198\0\312\0\150\0\203\0\213\0\262\0\331\0\303" +
		"\0\u0146\0\u0148\0\26\0\73\0\u010d\0\u014e\0\u014f\0\u0182\0\u0184\0\150\0\203\0" +
		"\213\0\262\0\324\0\u011d\0\u0153\0\u0163\0\u017f\0\u019a\0\u019b\0\u0108\0\u017a" +
		"\0\u017b\0\236\0\304\0\317\0\320\0\323\0\336\0\353\0\354\0\356\0\u010a\0\u010c\0" +
		"\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0131\0" +
		"\u0133\0\u0134\0\u015a\0\u0171\0\137\0\150\0\160\0\165\0\203\0\213\0\236\0\262\0" +
		"\304\0\317\0\320\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115" +
		"\0\u0118\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\1\0\6\0\37\0\41\0\106" +
		"\0\116\0\130\0\156\0\160\0\236\0\300\0\304\0\323\0\353\0\354\0\356\0\u010a\0\u010c" +
		"\0\u0111\0\u0115\0\u0118\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\1\0\2\0\6\0\13" +
		"\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0" +
		"\125\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216" +
		"\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0" +
		"\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370" +
		"\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0" +
		"\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0" +
		"\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0" +
		"\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\6\0\41\0\72\0\106\0\116\0\300" +
		"\0\377\0\u0122\0\u013d\0\u0144\0\u0145\0\u0166\0\1\0\6\0\41\0\72\0\106\0\116\0\300" +
		"\0\377\0\u0122\0\u013d\0\u0144\0\u0145\0\u0166\0\1\0\6\0\41\0\106\0\116\0\300\0\u010a" +
		"\0\333\0\22\0\236\0\272\0\273\0\304\0\317\0\320\0\323\0\336\0\353\0\354\0\356\0\371" +
		"\0\u010a\0\u010c\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119" +
		"\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\1\0\2\0\6\0\13\0\26\0\31\0\35" +
		"\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\124\0\125\0\130" +
		"\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0" +
		"\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304" +
		"\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0" +
		"\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0" +
		"\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0" +
		"\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0" +
		"\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\150\0\156\0\157\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233" +
		"\0\236\0\237\0\241\0\242\0\243\0\247\0\253\0\262\0\275\0\276\0\300\0\302\0\303\0" +
		"\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\360\0\363\0\370" +
		"\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0" +
		"\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0" +
		"\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0" +
		"\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0" +
		"\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137" +
		"\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0" +
		"\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\301\0\302\0\303\0\304" +
		"\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0" +
		"\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0" +
		"\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0" +
		"\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0" +
		"\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236" +
		"\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\301\0\302\0\303\0\304\0\305\0" +
		"\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0" +
		"\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0" +
		"\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0" +
		"\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0" +
		"\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43" +
		"\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156" +
		"\0\157\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0" +
		"\237\0\241\0\242\0\243\0\247\0\253\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305" +
		"\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\360\0\363\0\370\0\377\0" +
		"\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0" +
		"\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0" +
		"\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0" +
		"\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\150\0\155\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233" +
		"\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0" +
		"\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0" +
		"\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0" +
		"\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0" +
		"\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0" +
		"\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43" +
		"\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155" +
		"\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0" +
		"\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320" +
		"\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105" +
		"\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118" +
		"\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146" +
		"\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182" +
		"\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155\0\156\0\160" +
		"\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0" +
		"\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336" +
		"\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a" +
		"\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119" +
		"\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148" +
		"\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184" +
		"\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155\0\156\0\160\0\166\0" +
		"\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243" +
		"\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0" +
		"\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c" +
		"\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120" +
		"\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a" +
		"\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155\0\156\0\160\0\166\0\170\0\171" +
		"\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0" +
		"\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353" +
		"\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0" +
		"\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0" +
		"\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0" +
		"\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0" +
		"\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\137\0\143\0\150\0\155\0\156\0\160\0\166\0\170\0\171\0\172\0" +
		"\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276" +
		"\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0" +
		"\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0" +
		"\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0" +
		"\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0" +
		"\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0" +
		"\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125" +
		"\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0" +
		"\222\0\223\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302" +
		"\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0" +
		"\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110" +
		"\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131" +
		"\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a" +
		"\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35" +
		"\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137" +
		"\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\223\0" +
		"\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304" +
		"\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0" +
		"\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0" +
		"\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0" +
		"\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0" +
		"\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\150\0\155\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233" +
		"\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0" +
		"\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0" +
		"\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0" +
		"\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0" +
		"\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0" +
		"\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43" +
		"\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155" +
		"\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0" +
		"\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320" +
		"\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105" +
		"\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118" +
		"\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146" +
		"\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182" +
		"\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155\0\156\0\160" +
		"\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0" +
		"\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336" +
		"\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a" +
		"\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119" +
		"\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148" +
		"\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184" +
		"\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155\0\156\0\160\0\166\0" +
		"\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\232\0\233\0\236\0\237\0\241\0\242" +
		"\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0" +
		"\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0" +
		"\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0" +
		"\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0" +
		"\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0" +
		"\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0" +
		"\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\155\0\156\0\160\0\166\0\170" +
		"\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\232\0\233\0\236\0\237\0\241\0\242\0" +
		"\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342" +
		"\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c" +
		"\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120" +
		"\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a" +
		"\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172" +
		"\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\266\0" +
		"\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353" +
		"\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0" +
		"\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0" +
		"\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0" +
		"\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0" +
		"\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0" +
		"\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300" +
		"\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0" +
		"\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f" +
		"\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b" +
		"\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150" +
		"\0\u015a\0\u0166\0\u0171\0\u0172\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13" +
		"\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0" +
		"\125\0\130\0\137\0\143\0\150\0\156\0\157\0\160\0\166\0\170\0\171\0\172\0\203\0\213" +
		"\0\216\0\222\0\231\0\233\0\236\0\237\0\240\0\241\0\242\0\243\0\247\0\253\0\262\0" +
		"\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353" +
		"\0\354\0\356\0\360\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d" +
		"\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122" +
		"\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e" +
		"\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u0172\0\u017c\0\u0182\0\u0184\0\u018b" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\157\0\160\0\166\0\170\0\171" +
		"\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\247\0" +
		"\253\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342" +
		"\0\346\0\353\0\354\0\356\0\360\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a" +
		"\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119" +
		"\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148" +
		"\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184" +
		"\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\157\0\160\0\166\0" +
		"\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243" +
		"\0\247\0\253\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0" +
		"\336\0\342\0\346\0\353\0\354\0\356\0\360\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105" +
		"\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118" +
		"\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146" +
		"\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182" +
		"\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\160\0\166" +
		"\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0" +
		"\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342" +
		"\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c" +
		"\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120" +
		"\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a" +
		"\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u0172\0\u017c\0\u0182\0\u0184" +
		"\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0" +
		"\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262" +
		"\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0" +
		"\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d" +
		"\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122" +
		"\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e" +
		"\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0\u0172\0\u017c\0\u0182\0\u0184\0\u018b" +
		"\0\0\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105" +
		"\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0" +
		"\172\0\203\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275" +
		"\0\276\0\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0" +
		"\354\0\356\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e" +
		"\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128" +
		"\0\u012b\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f" +
		"\0\u0150\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13" +
		"\0\20\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\101\0\105\0\106\0" +
		"\116\0\117\0\125\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203" +
		"\0\213\0\216\0\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0" +
		"\300\0\302\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356" +
		"\0\363\0\370\0\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f" +
		"\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b" +
		"\0\u0131\0\u0133\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150" +
		"\0\u015a\0\u0166\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\62\0\72\0\73\0\105\0\106\0\116\0\117\0\125" +
		"\0\130\0\137\0\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0" +
		"\222\0\231\0\233\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303" +
		"\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0" +
		"\377\0\u0102\0\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111" +
		"\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133" +
		"\0\u0134\0\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166" +
		"\0\u0171\0\u017c\0\u0182\0\u0184\0\u018b\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\107\0\116\0\117\0\125\0\130\0\137\0" +
		"\143\0\150\0\156\0\160\0\166\0\170\0\171\0\172\0\203\0\213\0\216\0\222\0\231\0\233" +
		"\0\236\0\237\0\241\0\242\0\243\0\262\0\275\0\276\0\300\0\302\0\303\0\304\0\305\0" +
		"\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0\356\0\363\0\370\0\377\0\u0102\0" +
		"\u0103\0\u0105\0\u010a\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0" +
		"\u0115\0\u0118\0\u0119\0\u0120\0\u0122\0\u0128\0\u012b\0\u0131\0\u0133\0\u0134\0" +
		"\u013d\0\u0146\0\u0148\0\u014a\0\u014e\0\u014f\0\u0150\0\u015a\0\u0166\0\u0171\0" +
		"\u017c\0\u0182\0\u0184\0\u018b\0\236\0\304\0\317\0\320\0\323\0\353\0\354\0\356\0" +
		"\u010a\0\u010c\0\u0111\0\u0113\0\u0115\0\u0118\0\u0120\0\u0131\0\u0133\0\u0134\0" +
		"\u015a\0\u0171\0\u0174\0\234\0\3\0\0\0\22\0\0\0\37\0\64\0\20\0\101\0\22\0\37\0\26" +
		"\0\43\0\44\0\73\0\105\0\125\0\130\0\137\0\160\0\166\0\172\0\233\0\236\0\237\0\242" +
		"\0\243\0\276\0\303\0\304\0\305\0\317\0\320\0\323\0\336\0\342\0\346\0\353\0\354\0" +
		"\356\0\u0105\0\u010a\0\u010c\0\u010e\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119" +
		"\0\u0120\0\u0128\0\u0131\0\u0133\0\u0134\0\u0146\0\u0148\0\u015a\0\u0171\0\1\0\6" +
		"\0\41\0\106\0\116\0\236\0\300\0\304\0\317\0\320\0\323\0\336\0\353\0\354\0\356\0\u010a" +
		"\0\u010c\0\u010d\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119" +
		"\0\u0120\0\u0122\0\u0131\0\u0133\0\u0134\0\u014e\0\u014f\0\u0150\0\u015a\0\u0171" +
		"\0\u0182\0\u0184\0\72\0\143\0\170\0\216\0\231\0\241\0\275\0\370\0\377\0\u0102\0\u0103" +
		"\0\u012b\0\u013d\0\u014a\0\u0166\0\u017c\0\u018b\0\127\0\157\0\247\0\253\0\360\0" +
		"\150\0\203\0\150\0\203\0\213\0\262\0\147\0\215\0\105\0\105\0\130\0\105\0\130\0\105" +
		"\0\130\0\265\0\u0138\0\u0172\0\105\0\130\0\125\0\105\0\130\0\171\0\363\0\125\0\172" +
		"\0\137\0\137\0\160\0\137\0\160\0\157\0\247\0\253\0\360\0\351\0\352\0\u012a\0\155" +
		"\0\155\0\137\0\160\0\137\0\160\0\216\0\216\0\370\0\231\0\u014a\0\u012b\0\236\0\304" +
		"\0\353\0\354\0\356\0\u010c\0\u0131\0\u0133\0\u0134\0\u0171\0\236\0\304\0\353\0\354" +
		"\0\356\0\u010c\0\u0131\0\u0133\0\u0134\0\u0171\0\236\0\304\0\353\0\354\0\356\0\u010c" +
		"\0\u0111\0\u0131\0\u0133\0\u0134\0\u0171\0\236\0\304\0\353\0\354\0\356\0\u010c\0" +
		"\u0111\0\u0131\0\u0133\0\u0134\0\u0171\0\236\0\304\0\317\0\353\0\354\0\356\0\u010c" +
		"\0\u0111\0\u0131\0\u0133\0\u0134\0\u0171\0\236\0\304\0\317\0\320\0\322\0\323\0\353" +
		"\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0114\0\u0115\0\u0117\0\u0118\0\u011c" +
		"\0\u0131\0\u0133\0\u0134\0\u0159\0\u015a\0\u015d\0\u0160\0\u0171\0\u0186\0\236\0" +
		"\304\0\317\0\320\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115" +
		"\0\u0118\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\353\0" +
		"\354\0\356\0\u010c\0\u0111\0\u0113\0\u0131\0\u0133\0\u0134\0\u0171\0\236\0\304\0" +
		"\317\0\320\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115\0\u0118" +
		"\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0" +
		"\353\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115\0\u0118\0\u0120\0\u0131" +
		"\0\u0133\0\u0134\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0\353\0\354\0\356" +
		"\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115\0\u0118\0\u0120\0\u0131\0\u0133\0\u0134" +
		"\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0\336\0\353\0\354\0\356\0\u010a\0" +
		"\u010c\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0131\0\u0133\0\u0134\0" +
		"\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0\336\0\353\0\354\0\356\0\u010a\0\u010c" +
		"\0\u010e\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0131\0\u0133" +
		"\0\u0134\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0\336\0\353\0\354\0\356\0" +
		"\u010a\0\u010c\0\u010e\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0" +
		"\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0\353\0\354" +
		"\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115\0\u0118\0\u0120\0\u0131\0\u0133\0" +
		"\u0134\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0\336\0\353\0\354\0\356\0\u010a" +
		"\0\u010c\0\u010e\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0131" +
		"\0\u0133\0\u0134\0\u015a\0\u0171\0\236\0\304\0\317\0\320\0\323\0\336\0\353\0\354" +
		"\0\356\0\u010a\0\u010c\0\u010e\0\u010f\0\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0" +
		"\u0119\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\236\0\272\0\273\0\304\0" +
		"\317\0\320\0\323\0\336\0\353\0\354\0\356\0\371\0\u010a\0\u010c\0\u010e\0\u010f\0" +
		"\u0110\0\u0111\0\u0113\0\u0115\0\u0118\0\u0119\0\u0120\0\u0131\0\u0133\0\u0134\0" +
		"\u015a\0\u0171\0\u010d\0\u014e\0\u014f\0\u0182\0\u0184\0\u010d\0\u014e\0\u0182\0" +
		"\u0184\0\137\0\160\0\236\0\304\0\317\0\320\0\323\0\353\0\354\0\356\0\u010a\0\u010c" +
		"\0\u0111\0\u0113\0\u0115\0\u0118\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171" +
		"\0\137\0\160\0\236\0\304\0\317\0\320\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111" +
		"\0\u0113\0\u0115\0\u0118\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\137\0" +
		"\160\0\165\0\236\0\304\0\317\0\320\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111" +
		"\0\u0113\0\u0115\0\u0118\0\u0120\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\237\0" +
		"\157\0\253\0\237\0\u0128\0\26\0\43\0\44\0\73\0\237\0\303\0\u0105\0\u0128\0\u0146" +
		"\0\u0148\0\26\0\26\0\10\0\307\0\u0150\0\26\0\73\0\155\0\232\0\72\0\377\0\u013d\0" +
		"\u0166\0\303\0\u0146\0\u0148\0\303\0\u0146\0\u0148\0\1\0\6\0\41\0\106\0\116\0\300" +
		"\0\6\0\6\0\53\0\53\0\53\0\117\0\1\0\6\0\41\0\72\0\106\0\116\0\300\0\377\0\u0122\0" +
		"\u013d\0\u0144\0\u0145\0\u0166\0\2\0\13\0\31\0\2\0\13\0\31\0\236\0\304\0\317\0\320" +
		"\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0115\0\u0118\0\u0120" +
		"\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\u0174\0\1\0\6\0\37\0\41\0\106\0\116\0" +
		"\130\0\156\0\160\0\236\0\300\0\304\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111" +
		"\0\u0115\0\u0118\0\u0131\0\u0133\0\u0134\0\u015a\0\u0171\0\20\0\101\0\127\0\265\0" +
		"\365\0\u0138\0\u0174\0\242\0\243\0\346\0\351\0\352\0\u012a\0\236\0\304\0\317\0\320" +
		"\0\322\0\323\0\353\0\354\0\356\0\u010a\0\u010c\0\u0111\0\u0113\0\u0114\0\u0115\0" +
		"\u0117\0\u0118\0\u011c\0\u0131\0\u0133\0\u0134\0\u0159\0\u015a\0\u015d\0\u0160\0" +
		"\u0171\0\u0186\0");

	private static final int[] lapg_sym_to = TMLexer.unpack_int(4160,
		"\u01a4\0\u01a5\0\174\0\174\0\4\0\4\0\60\0\4\0\104\0\4\0\4\0\4\0\176\0\4\0\4\0\u014b" +
		"\0\4\0\4\0\4\0\4\0\4\0\5\0\5\0\5\0\102\0\5\0\5\0\5\0\5\0\u0137\0\5\0\5\0\5\0\5\0" +
		"\5\0\5\0\124\0\124\0\155\0\155\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0" +
		"\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301" +
		"\0\301\0\301\0\301\0\301\0\301\0\236\0\353\0\354\0\356\0\u0131\0\u0133\0\u0134\0" +
		"\u0171\0\62\0\107\0\u0111\0\u0182\0\u0182\0\u0182\0\u0182\0\u0146\0\u0146\0\u0146" +
		"\0\106\0\147\0\371\0\377\0\u010e\0\u013d\0\u0166\0\u0144\0\u0145\0\171\0\363\0\56" +
		"\0\103\0\123\0\142\0\361\0\367\0\374\0\u0100\0\u0112\0\u013b\0\u013c\0\u013e\0\u016b" +
		"\0\u016c\0\u016e\0\u0175\0\u0177\0\u018c\0\u018d\0\u018e\0\u0197\0\u019e\0\35\0\177" +
		"\0\177\0\177\0\302\0\177\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0" +
		"\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\41\0\73\0\117\0\172" +
		"\0\200\0\200\0\200\0\200\0\370\0\u0128\0\u018b\0\72\0\116\0\215\0\u010f\0\u0125\0" +
		"\u012b\0\u012b\0\u012b\0\u010f\0\6\0\6\0\6\0\125\0\6\0\6\0\125\0\201\0\201\0\201" +
		"\0\303\0\201\0\6\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\42" +
		"\0\173\0\202\0\202\0\202\0\202\0\u0147\0\31\0\53\0\55\0\150\0\203\0\150\0\203\0\203" +
		"\0\304\0\150\0\150\0\203\0\304\0\u010c\0\u010d\0\304\0\304\0\304\0\304\0\304\0\304" +
		"\0\304\0\150\0\304\0\304\0\u014e\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304" +
		"\0\304\0\304\0\304\0\304\0\u0172\0\u014e\0\u014e\0\304\0\304\0\u014e\0\u014e\0\101" +
		"\0\120\0\122\0\260\0\261\0\263\0\362\0\u0149\0\u017e\0\u0183\0\u0198\0\u0199\0\u019f" +
		"\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0" +
		"\305\0\305\0\305\0\305\0\305\0\305\0\u0140\0\u014c\0\u017d\0\26\0\204\0\237\0\204" +
		"\0\204\0\237\0\204\0\26\0\26\0\74\0\205\0\205\0\205\0\205\0\u0129\0\206\0\206\0\206" +
		"\0\206\0\u0123\0\u0123\0\u01a0\0\43\0\43\0\u0124\0\u0124\0\u01a1\0\u0110\0\207\0" +
		"\207\0\207\0\207\0\u0121\0\u0105\0\u0105\0\u0105\0\44\0\44\0\u014f\0\u014f\0\u014f" +
		"\0\u014f\0\u014f\0\210\0\210\0\210\0\210\0\u0120\0\u0120\0\u0184\0\u0120\0\u0184" +
		"\0\u0184\0\u0184\0\u0148\0\u0148\0\u0148\0\306\0\306\0\306\0\306\0\306\0\306\0\306" +
		"\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0" +
		"\306\0\306\0\306\0\306\0\306\0\156\0\211\0\156\0\156\0\211\0\211\0\156\0\211\0\156" +
		"\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0" +
		"\156\0\156\0\156\0\156\0\156\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7" +
		"\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0" +
		"\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0" +
		"\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307" +
		"\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45" +
		"\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0" +
		"\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111" +
		"\0\307\0\111\0\u0150\0\u0150\0\111\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0" +
		"\11\0\11\0\11\0\11\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0" +
		"\12\0\13\0\13\0\13\0\13\0\13\0\13\0\u014a\0\u0122\0\36\0\310\0\310\0\310\0\310\0" +
		"\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310" +
		"\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\10\0\17\0\10\0\17" +
		"\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\143" +
		"\0\45\0\126\0\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0" +
		"\271\0\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0" +
		"\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0" +
		"\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307" +
		"\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0" +
		"\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0" +
		"\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45" +
		"\0\111\0\212\0\234\0\240\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111" +
		"\0\45\0\307\0\342\0\111\0\45\0\45\0\240\0\240\0\212\0\111\0\45\0\10\0\u0104\0\45" +
		"\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\240\0\255\0\111" +
		"\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0" +
		"\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150" +
		"\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45" +
		"\0\126\0\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271" +
		"\0\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0102\0\u0104\0\45" +
		"\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111" +
		"\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0" +
		"\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150" +
		"\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0" +
		"\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0" +
		"\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0103\0\u0104\0\45\0\307" +
		"\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111" +
		"\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0" +
		"\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10" +
		"\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0" +
		"\111\0\212\0\234\0\241\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0" +
		"\45\0\307\0\342\0\111\0\45\0\45\0\241\0\241\0\212\0\111\0\45\0\10\0\u0104\0\45\0" +
		"\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\241\0\255\0\111\0" +
		"\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307" +
		"\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150" +
		"\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0" +
		"\45\0\111\0\212\0\216\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0" +
		"\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0" +
		"\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0" +
		"\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307" +
		"\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0" +
		"\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0" +
		"\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111" +
		"\0\212\0\217\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45" +
		"\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307" +
		"\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45" +
		"\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0" +
		"\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111" +
		"\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0" +
		"\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0" +
		"\220\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0" +
		"\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0" +
		"\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0" +
		"\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111" +
		"\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0" +
		"\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45" +
		"\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\221\0\234" +
		"\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111" +
		"\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307" +
		"\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150" +
		"\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307" +
		"\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150" +
		"\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\222\0\234\0\45\0\45" +
		"\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111\0\45\0\45" +
		"\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45" +
		"\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0" +
		"\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307" +
		"\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\223\0\234\0\45\0\45\0\111\0\255" +
		"\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111" +
		"\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307" +
		"\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0" +
		"\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45" +
		"\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0" +
		"\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126" +
		"\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212" +
		"\0\212\0\111\0\271\0\272\0\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45" +
		"\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307" +
		"\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0" +
		"\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0" +
		"\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17" +
		"\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10" +
		"\0\75\0\45\0\126\0\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0" +
		"\111\0\271\0\273\0\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104" +
		"\0\45\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111" +
		"\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0" +
		"\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150" +
		"\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45" +
		"\0\126\0\45\0\111\0\212\0\224\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111" +
		"\0\271\0\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45" +
		"\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111" +
		"\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0" +
		"\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150" +
		"\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0" +
		"\45\0\111\0\212\0\225\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0" +
		"\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0" +
		"\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0" +
		"\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307" +
		"\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0" +
		"\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0" +
		"\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111" +
		"\0\212\0\226\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45" +
		"\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307" +
		"\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45" +
		"\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0" +
		"\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111" +
		"\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0" +
		"\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0" +
		"\227\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\227\0\45\0" +
		"\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0" +
		"\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0" +
		"\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342" +
		"\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0" +
		"\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10" +
		"\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\230" +
		"\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\230\0\45\0\307" +
		"\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307" +
		"\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307" +
		"\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0" +
		"\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307" +
		"\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0" +
		"\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\234\0" +
		"\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111\0" +
		"\45\0\45\0\212\0\366\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0" +
		"\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0" +
		"\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307" +
		"\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0" +
		"\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\234\0\45\0\45" +
		"\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111\0\45\0\45" +
		"\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45" +
		"\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0" +
		"\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307" +
		"\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\u0190\0\111\0\u0150" +
		"\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\234\0\242\0\45\0\45" +
		"\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\346\0\111\0\45" +
		"\0\45\0\242\0\242\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307" +
		"\0\307\0\45\0\45\0\307\0\307\0\307\0\242\0\255\0\111\0\111\0\111\0\111\0\45\0\307" +
		"\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0" +
		"\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307" +
		"\0\u0191\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63" +
		"\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212" +
		"\0\234\0\243\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307" +
		"\0\342\0\111\0\45\0\45\0\243\0\243\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45" +
		"\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\243\0\255\0\111\0\111\0\111" +
		"\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0" +
		"\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10" +
		"\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0" +
		"\111\0\212\0\234\0\244\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0" +
		"\45\0\307\0\342\0\111\0\45\0\45\0\244\0\244\0\212\0\111\0\45\0\10\0\u0104\0\45\0" +
		"\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\244\0\255\0\111\0" +
		"\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307" +
		"\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150" +
		"\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0" +
		"\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0" +
		"\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307" +
		"\0\307\0\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45" +
		"\0\307\0\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0" +
		"\342\0\111\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111" +
		"\0\307\0\u0192\0\111\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61" +
		"\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0" +
		"\212\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0" +
		"\342\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0" +
		"\307\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0" +
		"\307\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111" +
		"\0\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0" +
		"\u0193\0\111\0\u0150\0\u0150\0\111\0\2\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0" +
		"\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212" +
		"\0\234\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342" +
		"\0\111\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307" +
		"\0\307\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307" +
		"\0\u0150\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0" +
		"\307\0\307\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111" +
		"\0\u0150\0\u0150\0\111\0\10\0\17\0\10\0\17\0\32\0\45\0\17\0\57\0\61\0\63\0\10\0\45" +
		"\0\45\0\75\0\111\0\45\0\32\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\234" +
		"\0\45\0\45\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111" +
		"\0\45\0\45\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307" +
		"\0\45\0\45\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150" +
		"\0\307\0\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307" +
		"\0\307\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150" +
		"\0\u0150\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\105\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\234\0\45\0\45" +
		"\0\111\0\255\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111\0\45\0\45" +
		"\0\212\0\111\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45" +
		"\0\307\0\307\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0" +
		"\10\0\307\0\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307" +
		"\0\111\0\45\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\137\0\10\0\75\0\45\0\126\0\45\0\111\0\212\0\234\0\45\0\45\0\111\0\255" +
		"\0\45\0\212\0\212\0\111\0\271\0\111\0\45\0\307\0\342\0\111\0\45\0\45\0\212\0\111" +
		"\0\45\0\10\0\u0104\0\45\0\307\0\45\0\307\0\307\0\307\0\307\0\45\0\45\0\307\0\307" +
		"\0\307\0\255\0\111\0\111\0\111\0\111\0\45\0\307\0\307\0\u0150\0\307\0\10\0\307\0" +
		"\307\0\307\0\307\0\307\0\307\0\307\0\10\0\342\0\111\0\307\0\307\0\307\0\111\0\45" +
		"\0\45\0\111\0\u0150\0\u0150\0\10\0\307\0\111\0\307\0\111\0\u0150\0\u0150\0\111\0" +
		"\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\311" +
		"\0\311\0\311\0\311\0\311\0\311\0\311\0\311\0\300\0\22\0\u01a2\0\37\0\3\0\64\0\110" +
		"\0\33\0\33\0\40\0\65\0\46\0\46\0\46\0\46\0\127\0\144\0\127\0\157\0\157\0\253\0\144" +
		"\0\277\0\312\0\46\0\350\0\350\0\376\0\46\0\312\0\u010b\0\312\0\312\0\312\0\312\0" +
		"\u0127\0\350\0\312\0\312\0\312\0\46\0\312\0\312\0\u0154\0\u0154\0\312\0\312\0\312" +
		"\0\312\0\312\0\312\0\46\0\312\0\312\0\312\0\46\0\46\0\312\0\312\0\14\0\14\0\14\0" +
		"\14\0\14\0\313\0\14\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0" +
		"\u0151\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\313\0\u0164\0\313\0\313" +
		"\0\313\0\u0151\0\u0151\0\u0181\0\313\0\313\0\u0151\0\u0151\0\112\0\170\0\254\0\266" +
		"\0\274\0\347\0\375\0\266\0\112\0\u0141\0\u0142\0\u0169\0\112\0\274\0\112\0\375\0" +
		"\u019d\0\151\0\245\0\245\0\245\0\245\0\213\0\262\0\214\0\214\0\264\0\264\0\175\0" +
		"\265\0\130\0\131\0\153\0\132\0\132\0\133\0\133\0\364\0\u0173\0\u0194\0\134\0\134" +
		"\0\145\0\135\0\135\0\256\0\u0136\0\146\0\257\0\160\0\161\0\250\0\162\0\162\0\246" +
		"\0\355\0\357\0\u0135\0\u012c\0\u012c\0\u012c\0\231\0\232\0\163\0\163\0\164\0\164" +
		"\0\267\0\270\0\u0139\0\275\0\u017c\0\u016a\0\314\0\314\0\314\0\314\0\314\0\314\0" +
		"\314\0\314\0\314\0\314\0\315\0\u0109\0\u012f\0\u0130\0\u0132\0\u014d\0\u016d\0\u016f" +
		"\0\u0170\0\u018f\0\316\0\316\0\316\0\316\0\316\0\316\0\u0158\0\316\0\316\0\316\0" +
		"\316\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\317\0\320\0\320" +
		"\0\u0113\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\320\0\321\0\321\0\321" +
		"\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0" +
		"\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\321\0\322\0\322\0\u0114" +
		"\0\u0117\0\u011c\0\322\0\322\0\322\0\u011c\0\322\0\322\0\u0159\0\u015d\0\u0160\0" +
		"\322\0\322\0\322\0\u0186\0\322\0\323\0\u010a\0\u0115\0\u0118\0\323\0\323\0\323\0" +
		"\323\0\323\0\u015a\0\323\0\323\0\323\0\323\0\324\0\324\0\324\0\324\0\u011d\0\324" +
		"\0\324\0\324\0\u011d\0\324\0\324\0\324\0\u011d\0\u011d\0\u0163\0\324\0\324\0\324" +
		"\0\u011d\0\324\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\325" +
		"\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\326\0\326\0\326\0\326\0" +
		"\326\0\326\0\326\0\326\0\326\0\326\0\326\0\326\0\326\0\326\0\326\0\326\0\326\0\326" +
		"\0\326\0\326\0\327\0\327\0\327\0\327\0\327\0\u0126\0\327\0\327\0\327\0\327\0\327" +
		"\0\327\0\327\0\327\0\327\0\u0126\0\327\0\327\0\327\0\327\0\327\0\327\0\330\0\330" +
		"\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\u0155\0\u0157\0\330\0\330" +
		"\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\331\0\331\0\331\0\331\0" +
		"\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331" +
		"\0\331\0\331\0\331\0\331\0\331\0\331\0\332\0\332\0\332\0\332\0\332\0\332\0\332\0" +
		"\332\0\332\0\332\0\332\0\332\0\332\0\332\0\332\0\332\0\332\0\332\0\332\0\332\0\333" +
		"\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0" +
		"\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\334\0\334\0\334\0\334" +
		"\0\334\0\334\0\334\0\334\0\334\0\334\0\334\0\334\0\u0156\0\334\0\334\0\334\0\334" +
		"\0\334\0\334\0\334\0\334\0\334\0\334\0\334\0\334\0\335\0\372\0\373\0\335\0\335\0" +
		"\335\0\335\0\335\0\335\0\335\0\335\0\u013a\0\335\0\335\0\335\0\335\0\335\0\335\0" +
		"\335\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0\u0152\0\u0152\0\u0180" +
		"\0\u0152\0\u0152\0\u0153\0\u017f\0\u019a\0\u019b\0\165\0\165\0\165\0\165\0\165\0" +
		"\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165" +
		"\0\165\0\165\0\165\0\166\0\166\0\336\0\336\0\336\0\u0119\0\u0119\0\336\0\336\0\336" +
		"\0\u0119\0\336\0\336\0\u0119\0\u0119\0\u0119\0\u0119\0\336\0\336\0\336\0\u0119\0" +
		"\336\0\167\0\167\0\252\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167" +
		"\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\343\0\247\0\360\0" +
		"\344\0\u0167\0\47\0\70\0\71\0\47\0\345\0\u0106\0\u0143\0\345\0\u0106\0\u0106\0\50" +
		"\0\51\0\27\0\27\0\27\0\52\0\115\0\233\0\276\0\113\0\u013f\0\u0176\0\u018a\0\u0107" +
		"\0\u0107\0\u0107\0\u0108\0\u017a\0\u017b\0\u01a3\0\23\0\67\0\136\0\140\0\u0101\0" +
		"\24\0\25\0\76\0\77\0\100\0\141\0\15\0\15\0\15\0\114\0\15\0\15\0\15\0\114\0\u0165" +
		"\0\114\0\u0178\0\u0179\0\114\0\20\0\30\0\54\0\21\0\21\0\21\0\337\0\337\0\337\0\337" +
		"\0\337\0\337\0\337\0\337\0\337\0\337\0\337\0\337\0\337\0\337\0\337\0\337\0\337\0" +
		"\337\0\337\0\337\0\u0195\0\16\0\16\0\66\0\16\0\16\0\16\0\154\0\235\0\251\0\340\0" +
		"\16\0\340\0\u011e\0\340\0\340\0\340\0\u011e\0\340\0\340\0\u011e\0\u011e\0\340\0\340" +
		"\0\340\0\u011e\0\340\0\34\0\121\0\152\0\365\0\u0138\0\u0174\0\u0196\0\351\0\352\0" +
		"\u012a\0\u012d\0\u012e\0\u0168\0\341\0\341\0\u0116\0\u011a\0\u011b\0\u011f\0\341" +
		"\0\341\0\341\0\u011f\0\341\0\341\0\u015b\0\u015c\0\u015e\0\u015f\0\u0161\0\u0162" +
		"\0\341\0\341\0\341\0\u0185\0\u0187\0\u0188\0\u0189\0\341\0\u019c\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(241,
		"\2\0\0\0\5\0\4\0\2\0\0\0\7\0\4\0\3\0\3\0\4\0\4\0\3\0\3\0\1\0\1\0\2\0\1\0\1\0\3\0" +
		"\3\0\2\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0\2\0\1\0\1\0\2\0\2\0" +
		"\1\0\1\0\1\0\1\0\3\0\10\0\3\0\2\0\3\0\1\0\1\0\1\0\1\0\5\0\3\0\1\0\3\0\1\0\3\0\1\0" +
		"\1\0\2\0\2\0\1\0\1\0\1\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0\4\0\2\0\4\0\3\0\3\0\1\0\1\0" +
		"\2\0\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0\5\0\5\0\6\0\3\0\1\0\2\0\1\0" +
		"\1\0\2\0\1\0\3\0\3\0\1\0\1\0\5\0\4\0\4\0\3\0\4\0\3\0\3\0\2\0\4\0\3\0\3\0\2\0\3\0" +
		"\2\0\2\0\1\0\1\0\3\0\2\0\3\0\3\0\4\0\3\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\2\0\1\0\2\0" +
		"\1\0\3\0\3\0\1\0\2\0\1\0\3\0\3\0\3\0\1\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0" +
		"\2\0\1\0\3\0\2\0\1\0\3\0\3\0\2\0\1\0\1\0\5\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0" +
		"\3\0\1\0\1\0\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0" +
		"\1\0\1\0\5\0\3\0\1\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0\1\0\1\0\1\0\1\0" +
		"\3\0\1\0\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0" +
		"\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(241,
		"\117\0\117\0\120\0\120\0\121\0\121\0\122\0\122\0\123\0\124\0\125\0\126\0\126\0\127" +
		"\0\127\0\130\0\131\0\131\0\132\0\133\0\133\0\134\0\134\0\135\0\135\0\135\0\135\0" +
		"\135\0\135\0\135\0\135\0\135\0\135\0\135\0\135\0\135\0\136\0\137\0\137\0\137\0\140" +
		"\0\140\0\140\0\140\0\141\0\142\0\142\0\143\0\144\0\145\0\145\0\145\0\145\0\146\0" +
		"\147\0\147\0\150\0\151\0\152\0\152\0\153\0\153\0\153\0\154\0\154\0\154\0\155\0\155" +
		"\0\155\0\155\0\155\0\155\0\155\0\155\0\156\0\156\0\156\0\156\0\156\0\156\0\157\0" +
		"\160\0\160\0\160\0\161\0\161\0\161\0\162\0\162\0\162\0\162\0\163\0\163\0\163\0\163" +
		"\0\163\0\164\0\164\0\165\0\165\0\166\0\166\0\167\0\167\0\170\0\170\0\171\0\172\0" +
		"\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172\0\172" +
		"\0\172\0\172\0\173\0\174\0\175\0\175\0\176\0\176\0\177\0\177\0\177\0\200\0\200\0" +
		"\200\0\200\0\201\0\202\0\202\0\203\0\203\0\203\0\204\0\204\0\205\0\205\0\205\0\206" +
		"\0\207\0\207\0\210\0\210\0\210\0\210\0\210\0\210\0\210\0\210\0\211\0\212\0\212\0" +
		"\212\0\212\0\213\0\213\0\213\0\214\0\214\0\215\0\216\0\216\0\216\0\217\0\217\0\220" +
		"\0\221\0\221\0\221\0\222\0\223\0\223\0\224\0\224\0\225\0\226\0\226\0\226\0\226\0" +
		"\227\0\227\0\230\0\230\0\231\0\231\0\231\0\231\0\232\0\232\0\232\0\233\0\233\0\233" +
		"\0\233\0\233\0\234\0\234\0\235\0\235\0\236\0\236\0\237\0\237\0\240\0\241\0\241\0" +
		"\241\0\241\0\242\0\243\0\243\0\244\0\245\0\246\0\246\0\247\0\247\0\250\0\250\0\251" +
		"\0\251\0\252\0\252\0\253\0\253\0\254\0\254\0\255\0\255\0\256\0\256\0");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"regexp",
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
		"'=>'",
		"';'",
		"'.'",
		"','",
		"':'",
		"'['",
		"']'",
		"'('",
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
		"Lreduce",
		"code",
		"'{'",
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
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"named_pattern",
		"lexeme",
		"lexeme_transition",
		"lexeme_attrs",
		"lexeme_attribute",
		"lexer_directive",
		"lexer_state_list_Comma_separated",
		"state_selector",
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
		"typeopt",
		"lexeme_transitionopt",
		"iconopt",
		"lexeme_attrsopt",
		"commandopt",
		"identifieropt",
		"implementsopt",
		"rhsSuffixopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int import__optlist = 79;
		int input = 80;
		int option_optlist = 81;
		int header = 82;
		int lexer_section = 83;
		int parser_section = 84;
		int parsing_algorithm = 85;
		int import_ = 86;
		int option = 87;
		int identifier = 88;
		int symref = 89;
		int symref_noargs = 90;
		int type = 91;
		int type_part_list = 92;
		int type_part = 93;
		int pattern = 94;
		int lexer_parts = 95;
		int lexer_part = 96;
		int named_pattern = 97;
		int lexeme = 98;
		int lexeme_transition = 99;
		int lexeme_attrs = 100;
		int lexeme_attribute = 101;
		int lexer_directive = 102;
		int lexer_state_list_Comma_separated = 103;
		int state_selector = 104;
		int stateref = 105;
		int lexer_state = 106;
		int grammar_parts = 107;
		int grammar_part = 108;
		int nonterm = 109;
		int nonterm_type = 110;
		int _implements = 111;
		int assoc = 112;
		int param_modifier = 113;
		int template_param = 114;
		int directive = 115;
		int inputref_list_Comma_separated = 116;
		int inputref = 117;
		int references = 118;
		int references_cs = 119;
		int rule0_list_Or_separated = 120;
		int rules = 121;
		int rule0 = 122;
		int predicate = 123;
		int rhsPrefix = 124;
		int rhsSuffix = 125;
		int ruleAction = 126;
		int rhsParts = 127;
		int rhsPart = 128;
		int rhsStateMarker = 129;
		int rhsAnnotated = 130;
		int rhsAssignment = 131;
		int rhsOptional = 132;
		int rhsCast = 133;
		int rhsUnordered = 134;
		int rhsClass = 135;
		int rhsPrimary = 136;
		int rhsSet = 137;
		int setPrimary = 138;
		int setExpression = 139;
		int annotation_list = 140;
		int annotations = 141;
		int annotation = 142;
		int nonterm_param_list_Comma_separated = 143;
		int nonterm_params = 144;
		int nonterm_param = 145;
		int param_ref = 146;
		int argument_list_Comma_separated = 147;
		int argument_list_Comma_separated_opt = 148;
		int symref_args = 149;
		int argument = 150;
		int param_type = 151;
		int param_value = 152;
		int predicate_primary = 153;
		int predicate_expression = 154;
		int expression = 155;
		int expression_list_Comma_separated = 156;
		int expression_list_Comma_separated_opt = 157;
		int map_entry_list_Comma_separated = 158;
		int map_entry_list_Comma_separated_opt = 159;
		int map_entry = 160;
		int literal = 161;
		int name = 162;
		int qualified_id = 163;
		int command = 164;
		int syntax_problem = 165;
		int parsing_algorithmopt = 166;
		int typeopt = 167;
		int lexeme_transitionopt = 168;
		int iconopt = 169;
		int lexeme_attrsopt = 170;
		int commandopt = 171;
		int identifieropt = 172;
		int implementsopt = 173;
		int rhsSuffixopt = 174;
	}

	public interface Rules {
		int lexer_directive_directiveBrackets = 53;  // lexer_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
		int nonterm_type_nontermTypeAST = 74;  // nonterm_type ::= Lreturns symref_noargs
		int nonterm_type_nontermTypeHint = 75;  // nonterm_type ::= Linline Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint2 = 76;  // nonterm_type ::= Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint3 = 77;  // nonterm_type ::= Linterface identifieropt implementsopt
		int nonterm_type_nontermTypeHint4 = 78;  // nonterm_type ::= Lvoid
		int nonterm_type_nontermTypeRaw = 79;  // nonterm_type ::= type
		int directive_directivePrio = 91;  // directive ::= '%' assoc references ';'
		int directive_directiveInput = 92;  // directive ::= '%' Linput inputref_list_Comma_separated ';'
		int directive_directiveAssert = 93;  // directive ::= '%' Lassert Lempty rhsSet ';'
		int directive_directiveAssert2 = 94;  // directive ::= '%' Lassert Lnonempty rhsSet ';'
		int directive_directiveSet = 95;  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 144;  // rhsOptional ::= rhsCast '?'
		int rhsCast_rhsAsLiteral = 147;  // rhsCast ::= rhsClass Las literal
		int rhsPrimary_rhsSymbol = 151;  // rhsPrimary ::= symref
		int rhsPrimary_rhsNested = 152;  // rhsPrimary ::= '(' rules ')'
		int rhsPrimary_rhsList = 153;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		int rhsPrimary_rhsList2 = 154;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		int rhsPrimary_rhsQuantifier = 155;  // rhsPrimary ::= rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 156;  // rhsPrimary ::= rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 157;  // rhsPrimary ::= '$' '(' rules ')'
		int setPrimary_setSymbol = 160;  // setPrimary ::= ID symref
		int setPrimary_setSymbol2 = 161;  // setPrimary ::= symref
		int setPrimary_setCompound = 162;  // setPrimary ::= '(' setExpression ')'
		int setPrimary_setComplement = 163;  // setPrimary ::= '~' setPrimary
		int setExpression_setBinary = 165;  // setExpression ::= setExpression '|' setExpression
		int setExpression_setBinary2 = 166;  // setExpression ::= setExpression '&' setExpression
		int nonterm_param_inlineParameter = 177;  // nonterm_param ::= ID identifier '=' param_value
		int nonterm_param_inlineParameter2 = 178;  // nonterm_param ::= ID identifier
		int predicate_primary_boolPredicate = 193;  // predicate_primary ::= '!' param_ref
		int predicate_primary_boolPredicate2 = 194;  // predicate_primary ::= param_ref
		int predicate_primary_comparePredicate = 195;  // predicate_primary ::= param_ref '==' literal
		int predicate_primary_comparePredicate2 = 196;  // predicate_primary ::= param_ref '!=' literal
		int predicate_expression_predicateBinary = 198;  // predicate_expression ::= predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 199;  // predicate_expression ::= predicate_expression '||' predicate_expression
		int expression_instance = 202;  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 203;  // expression ::= '[' expression_list_Comma_separated_opt ']'
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
			case 19:  // type ::= '(' scon ')'
				{ tmLeft.value = ((String)tmStack[tmHead - 1].value); }
				break;
			case 20:  // type ::= '(' type_part_list ')'
				{ tmLeft.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); }
				break;
			case 36:  // pattern ::= regexp
				tmLeft.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // lexer_parts ::= lexer_part
				tmLeft.value = new ArrayList();
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 38:  // lexer_parts ::= lexer_parts lexer_part
				((List<ITmaLexerPart>)tmLeft.value).add(((ITmaLexerPart)tmStack[tmHead].value));
				break;
			case 39:  // lexer_parts ::= lexer_parts syntax_problem
				((List<ITmaLexerPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 44:  // named_pattern ::= ID '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 45:  // lexeme ::= identifier typeopt ':' pattern lexeme_transitionopt iconopt lexeme_attrsopt commandopt
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 7].value) /* name */,
						((String)tmStack[tmHead - 6].value) /* type */,
						((TmaPattern)tmStack[tmHead - 4].value) /* pattern */,
						((TmaStateref)tmStack[tmHead - 3].value) /* transition */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead - 1].value) /* attrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						source, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 46:  // lexeme ::= identifier typeopt ':'
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((String)tmStack[tmHead - 1].value) /* type */,
						null /* pattern */,
						null /* transition */,
						null /* priority */,
						null /* attrs */,
						null /* command */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 47:  // lexeme_transition ::= '=>' stateref
				tmLeft.value = ((TmaStateref)tmStack[tmHead].value);
				break;
			case 48:  // lexeme_attrs ::= '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // lexeme_attribute ::= Lsoft
				tmLeft.value = TmaLexemeAttribute.LSOFT;
				break;
			case 50:  // lexeme_attribute ::= Lclass
				tmLeft.value = TmaLexemeAttribute.LCLASS;
				break;
			case 51:  // lexeme_attribute ::= Lspace
				tmLeft.value = TmaLexemeAttribute.LSPACE;
				break;
			case 52:  // lexeme_attribute ::= Llayout
				tmLeft.value = TmaLexemeAttribute.LLAYOUT;
				break;
			case 53:  // lexer_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
				tmLeft.value = new TmaDirectiveBrackets(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // lexer_state_list_Comma_separated ::= lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 55:  // lexer_state_list_Comma_separated ::= lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 56:  // state_selector ::= '[' lexer_state_list_Comma_separated ']'
				tmLeft.value = new TmaStateSelector(
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 57:  // stateref ::= ID
				tmLeft.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // lexer_state ::= identifier '=>' stateref
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaStateref)tmStack[tmHead].value) /* defaultTransition */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // lexer_state ::= identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* defaultTransition */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // grammar_parts ::= grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 61:  // grammar_parts ::= grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 62:  // grammar_parts ::= grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 66:  // nonterm ::= annotations identifier nonterm_params nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 67:  // nonterm ::= annotations identifier nonterm_params '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 68:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 69:  // nonterm ::= annotations identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 70:  // nonterm ::= identifier nonterm_params nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // nonterm ::= identifier nonterm_params '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 72:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 73:  // nonterm ::= identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 74:  // nonterm_type ::= Lreturns symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 75:  // nonterm_type ::= Linline Lclass identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // nonterm_type ::= Lclass identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // nonterm_type ::= Linterface identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LINTERFACE /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 78:  // nonterm_type ::= Lvoid
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LVOID /* kind */,
						null /* name */,
						null /* _implements */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 79:  // nonterm_type ::= type
				tmLeft.value = new TmaNontermTypeRaw(
						((String)tmStack[tmHead].value) /* typeText */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // implements ::= ':' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 81:  // assoc ::= Lleft
				tmLeft.value = TmaAssoc.LLEFT;
				break;
			case 82:  // assoc ::= Lright
				tmLeft.value = TmaAssoc.LRIGHT;
				break;
			case 83:  // assoc ::= Lnonassoc
				tmLeft.value = TmaAssoc.LNONASSOC;
				break;
			case 84:  // param_modifier ::= Lexplicit
				tmLeft.value = TmaParamModifier.LEXPLICIT;
				break;
			case 85:  // param_modifier ::= Lglobal
				tmLeft.value = TmaParamModifier.LGLOBAL;
				break;
			case 86:  // param_modifier ::= Llookahead
				tmLeft.value = TmaParamModifier.LLOOKAHEAD;
				break;
			case 87:  // template_param ::= '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 88:  // template_param ::= '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // template_param ::= '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // template_param ::= '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // directive ::= '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // directive ::= '%' Linput inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // directive ::= '%' Lassert Lempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // directive ::= '%' Lassert Lnonempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LNONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 95:  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((String)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 96:  // inputref_list_Comma_separated ::= inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 97:  // inputref_list_Comma_separated ::= inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 98:  // inputref ::= symref_noargs Lnoeoi
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // inputref ::= symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // references ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 101:  // references ::= references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 102:  // references_cs ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 103:  // references_cs ::= references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 104:  // rule0_list_Or_separated ::= rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 105:  // rule0_list_Or_separated ::= rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 107:  // rule0 ::= predicate rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 4].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rule0 ::= predicate rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // rule0 ::= predicate rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // rule0 ::= predicate rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // rule0 ::= predicate rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // rule0 ::= predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rule0 ::= predicate ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // rule0 ::= predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // rule0 ::= rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // rule0 ::= rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // rule0 ::= rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 119:  // rule0 ::= rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // rule0 ::= rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 121:  // rule0 ::= ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // rule0 ::= rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // rule0 ::= syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						null /* suffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 124:  // predicate ::= '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 125:  // rhsPrefix ::= annotations ':'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 126:  // rhsSuffix ::= '%' Lprec symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // rhsSuffix ::= '%' Lshift symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // ruleAction ::= '{~' identifier scon '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((String)tmStack[tmHead - 1].value) /* parameter */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // ruleAction ::= '{~' identifier '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* action */,
						null /* parameter */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // rhsParts ::= rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 131:  // rhsParts ::= rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 132:  // rhsParts ::= rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 137:  // rhsStateMarker ::= '.' ID
				tmLeft.value = new TmaRhsStateMarker(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // rhsAnnotated ::= annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rhsAssignment ::= identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // rhsAssignment ::= identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // rhsOptional ::= rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 146:  // rhsCast ::= rhsClass Las symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // rhsCast ::= rhsClass Las literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // rhsUnordered ::= rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rhsClass ::= identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rhsPrimary ::= symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rhsPrimary ::= '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rhsPrimary ::= rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rhsPrimary ::= rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // rhsPrimary ::= '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // rhsSet ::= Lset '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // setPrimary ::= ID symref
				tmLeft.value = new TmaSetSymbol(
						((String)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // setPrimary ::= symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // setPrimary ::= '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // setPrimary ::= '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // setExpression ::= setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 166:  // setExpression ::= setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 167:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 168:  // annotation_list ::= annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 169:  // annotations ::= annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 170:  // annotation ::= '@' ID '{' expression '}'
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 3].value) /* name */,
						((ITmaExpression)tmStack[tmHead - 1].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 171:  // annotation ::= '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 172:  // annotation ::= '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 173:  // nonterm_param_list_Comma_separated ::= nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 174:  // nonterm_param_list_Comma_separated ::= nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 175:  // nonterm_params ::= '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // nonterm_param ::= ID identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 178:  // nonterm_param ::= ID identifier
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 179:  // param_ref ::= identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 180:  // argument_list_Comma_separated ::= argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 181:  // argument_list_Comma_separated ::= argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 184:  // symref_args ::= '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // argument ::= param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // argument ::= '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // argument ::= '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 188:  // argument ::= param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 189:  // param_type ::= Lflag
				tmLeft.value = TmaParamType.LFLAG;
				break;
			case 190:  // param_type ::= Lparam
				tmLeft.value = TmaParamType.LPARAM;
				break;
			case 193:  // predicate_primary ::= '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 194:  // predicate_primary ::= param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // predicate_primary ::= param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // predicate_primary ::= param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 198:  // predicate_expression ::= predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 199:  // predicate_expression ::= predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 203:  // expression ::= '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 205:  // expression_list_Comma_separated ::= expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 206:  // expression_list_Comma_separated ::= expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 209:  // map_entry_list_Comma_separated ::= map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 210:  // map_entry_list_Comma_separated ::= map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 213:  // map_entry ::= ID ':' expression
				tmLeft.value = new TmaMapEntry(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 214:  // literal ::= scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 215:  // literal ::= icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 216:  // literal ::= Ltrue
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 217:  // literal ::= Lfalse
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 218:  // name ::= qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 220:  // qualified_id ::= qualified_id '.' ID
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 221:  // command ::= code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 222:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 420);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 421);
	}
}
