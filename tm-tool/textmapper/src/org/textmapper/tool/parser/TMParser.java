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
	private static final int[] tmAction = TMLexer.unpack_int(403,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\312\0\313\0\uffb9\uffff\322\0\uff6b" +
		"\uffff\314\0\315\0\uffff\uffff\275\0\274\0\300\0\317\0\ufefb\uffff\ufef3\uffff\ufee7" +
		"\uffff\302\0\ufea3\uffff\uffff\uffff\ufe9d\uffff\20\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\323\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\277" +
		"\0\uffff\uffff\uffff\uffff\17\0\247\0\ufe59\uffff\ufe51\uffff\uffff\uffff\251\0\ufe4b" +
		"\uffff\uffff\uffff\uffff\uffff\7\0\320\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufe0b\uffff\4\0\16\0\301\0\256\0\257\0\uffff\uffff\uffff\uffff\254\0\uffff" +
		"\uffff\ufe05\uffff\uffff\uffff\306\0\ufdff\uffff\uffff\uffff\14\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\2\0\22\0\264\0\255\0\263\0\250\0\uffff\uffff\uffff" +
		"\uffff\276\0\uffff\uffff\12\0\13\0\uffff\uffff\uffff\uffff\ufdf9\uffff\ufdf1\uffff" +
		"\ufdeb\uffff\25\0\31\0\32\0\33\0\30\0\15\0\uffff\uffff\311\0\305\0\6\0\uffff\uffff" +
		"\51\0\uffff\uffff\46\0\uffff\uffff\23\0\325\0\uffff\uffff\26\0\27\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\ufda3\uffff\52\0\55\0\56\0\57\0\ufd5d\uffff\uffff\uffff\234\0" +
		"\uffff\uffff\uffff\uffff\47\0\24\0\34\0\ufd15\uffff\uffff\uffff\77\0\100\0\101\0" +
		"\uffff\uffff\uffff\uffff\103\0\102\0\104\0\262\0\261\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufccb\uffff\240\0\ufc81\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufc29\uffff" +
		"\ufbe7\uffff\74\0\75\0\uffff\uffff\uffff\uffff\53\0\54\0\233\0\uffff\uffff\uffff" +
		"\uffff\45\0\ufba5\uffff\ufb57\uffff\uffff\uffff\117\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\122\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\ufb4f\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufaf7\uffff\uffff\uffff" +
		"\321\0\uffff\uffff\213\0\ufa8b\uffff\uffff\uffff\127\0\ufa83\uffff\ufa2d\uffff\341" +
		"\0\uf9d7\uffff\uf9cd\uffff\uf975\uffff\167\0\166\0\163\0\176\0\200\0\uf919\uffff" +
		"\164\0\uf8bb\uffff\uf85b\uffff\222\0\uffff\uffff\165\0\151\0\150\0\uf7f7\uffff\uffff" +
		"\uffff\242\0\244\0\uf7b5\uffff\70\0\335\0\uf773\uffff\uf76d\uffff\uf767\uffff\uf70f" +
		"\uffff\uffff\uffff\uf6b7\uffff\uffff\uffff\uffff\uffff\44\0\327\0\uf65f\uffff\120" +
		"\0\112\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\111\0\123\0\uffff\uffff" +
		"\uffff\uffff\110\0\236\0\uffff\uffff\uffff\uffff\175\0\uffff\uffff\uf613\uffff\271" +
		"\0\uffff\uffff\uffff\uffff\uf607\uffff\uffff\uffff\174\0\uffff\uffff\171\0\uffff" +
		"\uffff\uf5af\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf557\uffff\67" +
		"\0\uf4fd\uffff\uf4a7\uffff\uf49d\uffff\140\0\uf445\uffff\uf43b\uffff\uffff\uffff" +
		"\144\0\147\0\uf3e3\uffff\uf3d9\uffff\162\0\146\0\uffff\uffff\204\0\uffff\uffff\217" +
		"\0\220\0\153\0\177\0\uf37d\uffff\uffff\uffff\243\0\uf375\uffff\uffff\uffff\337\0" +
		"\72\0\73\0\uffff\uffff\uffff\uffff\uf36f\uffff\uffff\uffff\uf317\uffff\uf2bf\uffff" +
		"\uffff\uffff\uffff\uffff\331\0\uf267\uffff\116\0\uffff\uffff\113\0\114\0\uffff\uffff" +
		"\106\0\uffff\uffff\154\0\155\0\265\0\uffff\uffff\uffff\uffff\uffff\uffff\152\0\uffff" +
		"\uffff\214\0\uffff\uffff\173\0\172\0\uffff\uffff\uffff\uffff\157\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uf21d\uffff\225\0\230\0\uffff\uffff\uffff\uffff\201\0\uf1d7\uffff" +
		"\202\0\126\0\uf173\uffff\uf169\uffff\134\0\137\0\uf111\uffff\136\0\143\0\uf107\uffff" +
		"\142\0\145\0\uf0fd\uffff\206\0\207\0\uffff\uffff\241\0\71\0\124\0\uf0a1\uffff\66" +
		"\0\65\0\uffff\uffff\63\0\uffff\uffff\uffff\uffff\uf09b\uffff\40\0\41\0\42\0\43\0" +
		"\uffff\uffff\333\0\35\0\115\0\uffff\uffff\107\0\267\0\270\0\uf043\uffff\uf03b\uffff" +
		"\uffff\uffff\170\0\156\0\221\0\uffff\uffff\227\0\224\0\uffff\uffff\223\0\uffff\uffff" +
		"\133\0\uf033\uffff\132\0\135\0\141\0\245\0\uffff\uffff\64\0\62\0\61\0\uffff\uffff" +
		"\37\0\105\0\uffff\uffff\226\0\uf029\uffff\uf021\uffff\131\0\125\0\60\0\216\0\215" +
		"\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4068,
		"\10\0\1\0\46\0\1\0\47\0\1\0\55\0\1\0\57\0\1\0\60\0\1\0\61\0\1\0\62\0\1\0\63\0\1\0" +
		"\64\0\1\0\65\0\1\0\66\0\1\0\67\0\1\0\70\0\1\0\71\0\1\0\72\0\1\0\73\0\1\0\74\0\1\0" +
		"\75\0\1\0\76\0\1\0\77\0\1\0\100\0\1\0\101\0\1\0\102\0\1\0\103\0\1\0\104\0\1\0\105" +
		"\0\1\0\106\0\1\0\107\0\1\0\110\0\1\0\111\0\1\0\112\0\1\0\113\0\1\0\uffff\uffff\ufffe" +
		"\uffff\1\0\uffff\uffff\2\0\uffff\uffff\22\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\50\0\uffff\uffff\51\0\uffff\uffff\52\0\uffff\uffff\23\0\304\0\uffff" +
		"\uffff\ufffe\uffff\32\0\uffff\uffff\0\0\21\0\6\0\21\0\10\0\21\0\11\0\21\0\16\0\21" +
		"\0\17\0\21\0\20\0\21\0\21\0\21\0\23\0\21\0\24\0\21\0\25\0\21\0\27\0\21\0\30\0\21" +
		"\0\34\0\21\0\35\0\21\0\37\0\21\0\42\0\21\0\44\0\21\0\45\0\21\0\46\0\21\0\47\0\21" +
		"\0\53\0\21\0\54\0\21\0\56\0\21\0\57\0\21\0\60\0\21\0\61\0\21\0\62\0\21\0\63\0\21" +
		"\0\64\0\21\0\65\0\21\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0\72\0\21\0\73\0\21" +
		"\0\74\0\21\0\75\0\21\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21\0\102\0\21\0\103\0" +
		"\21\0\104\0\21\0\105\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111\0\21\0\112\0\21" +
		"\0\113\0\21\0\114\0\21\0\uffff\uffff\ufffe\uffff\24\0\uffff\uffff\111\0\uffff\uffff" +
		"\16\0\324\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\16\0\316\0\24\0\316\0\27\0\316" +
		"\0\111\0\316\0\uffff\uffff\ufffe\uffff\55\0\uffff\uffff\10\0\5\0\46\0\5\0\47\0\5" +
		"\0\57\0\5\0\60\0\5\0\61\0\5\0\62\0\5\0\63\0\5\0\64\0\5\0\65\0\5\0\66\0\5\0\67\0\5" +
		"\0\70\0\5\0\71\0\5\0\72\0\5\0\73\0\5\0\74\0\5\0\75\0\5\0\76\0\5\0\77\0\5\0\100\0" +
		"\5\0\101\0\5\0\102\0\5\0\103\0\5\0\104\0\5\0\105\0\5\0\106\0\5\0\107\0\5\0\110\0" +
		"\5\0\111\0\5\0\112\0\5\0\113\0\5\0\uffff\uffff\ufffe\uffff\20\0\uffff\uffff\23\0" +
		"\303\0\uffff\uffff\ufffe\uffff\35\0\uffff\uffff\41\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\33\0\253\0\uffff\uffff\ufffe\uffff\21\0\uffff\uffff\20\0\260\0\33\0\260\0\uffff" +
		"\uffff\ufffe\uffff\20\0\uffff\uffff\33\0\252\0\uffff\uffff\ufffe\uffff\47\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\27\0\310\0\uffff\uffff\ufffe\uffff\10\0\uffff\uffff\0\0\3\0\uffff" +
		"\uffff\ufffe\uffff\20\0\uffff\uffff\27\0\307\0\uffff\uffff\ufffe\uffff\111\0\uffff" +
		"\uffff\16\0\324\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff\21\0\17\0\114\0\17\0\uffff" +
		"\uffff\ufffe\uffff\114\0\uffff\uffff\21\0\326\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\22\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0" +
		"\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff" +
		"\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\0\0\10\0\10" +
		"\0\10\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\0\0\11\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\21\0\235\0\24" +
		"\0\235\0\44\0\235\0\47\0\235\0\56\0\235\0\57\0\235\0\60\0\235\0\61\0\235\0\62\0\235" +
		"\0\63\0\235\0\64\0\235\0\65\0\235\0\66\0\235\0\67\0\235\0\70\0\235\0\71\0\235\0\72" +
		"\0\235\0\73\0\235\0\74\0\235\0\75\0\235\0\76\0\235\0\77\0\235\0\100\0\235\0\101\0" +
		"\235\0\102\0\235\0\103\0\235\0\104\0\235\0\105\0\235\0\106\0\235\0\107\0\235\0\110" +
		"\0\235\0\111\0\235\0\112\0\235\0\113\0\235\0\uffff\uffff\ufffe\uffff\116\0\uffff" +
		"\uffff\0\0\36\0\6\0\36\0\10\0\36\0\22\0\36\0\46\0\36\0\47\0\36\0\57\0\36\0\60\0\36" +
		"\0\61\0\36\0\62\0\36\0\63\0\36\0\64\0\36\0\65\0\36\0\66\0\36\0\67\0\36\0\70\0\36" +
		"\0\71\0\36\0\72\0\36\0\73\0\36\0\74\0\36\0\75\0\36\0\76\0\36\0\77\0\36\0\100\0\36" +
		"\0\101\0\36\0\102\0\36\0\103\0\36\0\104\0\36\0\105\0\36\0\106\0\36\0\107\0\36\0\110" +
		"\0\36\0\111\0\36\0\112\0\36\0\113\0\36\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff" +
		"\21\0\237\0\24\0\237\0\44\0\237\0\45\0\237\0\47\0\237\0\56\0\237\0\57\0\237\0\60" +
		"\0\237\0\61\0\237\0\62\0\237\0\63\0\237\0\64\0\237\0\65\0\237\0\66\0\237\0\67\0\237" +
		"\0\70\0\237\0\71\0\237\0\72\0\237\0\73\0\237\0\74\0\237\0\75\0\237\0\76\0\237\0\77" +
		"\0\237\0\100\0\237\0\101\0\237\0\102\0\237\0\103\0\237\0\104\0\237\0\105\0\237\0" +
		"\106\0\237\0\107\0\237\0\110\0\237\0\111\0\237\0\112\0\237\0\113\0\237\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25" +
		"\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\uffff" +
		"\uffff\ufffe\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\7\0\336\0\21\0\336\0\uffff\uffff\ufffe\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\7\0\336\0\21\0\336\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0" +
		"\0\330\0\6\0\330\0\10\0\330\0\22\0\330\0\24\0\330\0\46\0\330\0\47\0\330\0\57\0\330" +
		"\0\60\0\330\0\61\0\330\0\62\0\330\0\63\0\330\0\64\0\330\0\65\0\330\0\66\0\330\0\67" +
		"\0\330\0\70\0\330\0\71\0\330\0\72\0\330\0\73\0\330\0\74\0\330\0\75\0\330\0\76\0\330" +
		"\0\77\0\330\0\100\0\330\0\101\0\330\0\102\0\330\0\103\0\330\0\104\0\330\0\105\0\330" +
		"\0\106\0\330\0\107\0\330\0\110\0\330\0\111\0\330\0\112\0\330\0\113\0\330\0\114\0" +
		"\330\0\uffff\uffff\ufffe\uffff\101\0\uffff\uffff\16\0\121\0\20\0\121\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25" +
		"\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff" +
		"\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\27\0\342\0\uffff" +
		"\uffff\ufffe\uffff\32\0\uffff\uffff\13\0\17\0\21\0\17\0\36\0\17\0\6\0\21\0\11\0\21" +
		"\0\16\0\21\0\17\0\21\0\24\0\21\0\25\0\21\0\27\0\21\0\30\0\21\0\34\0\21\0\35\0\21" +
		"\0\37\0\21\0\42\0\21\0\44\0\21\0\45\0\21\0\46\0\21\0\47\0\21\0\53\0\21\0\54\0\21" +
		"\0\56\0\21\0\57\0\21\0\60\0\21\0\61\0\21\0\62\0\21\0\63\0\21\0\64\0\21\0\65\0\21" +
		"\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0\72\0\21\0\73\0\21\0\74\0\21\0\75\0\21" +
		"\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21\0\102\0\21\0\103\0\21\0\104\0\21\0\105" +
		"\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111\0\21\0\112\0\21\0\113\0\21\0\114\0\21" +
		"\0\uffff\uffff\ufffe\uffff\11\0\uffff\uffff\16\0\130\0\27\0\130\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\17\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0" +
		"\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff" +
		"\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\114\0\uffff\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\17\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\114\0\uffff" +
		"\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\11" +
		"\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff" +
		"\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\114\0\uffff" +
		"\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\42\0\uffff\uffff" +
		"\6\0\160\0\11\0\160\0\16\0\160\0\17\0\160\0\24\0\160\0\25\0\160\0\27\0\160\0\30\0" +
		"\160\0\44\0\160\0\45\0\160\0\46\0\160\0\47\0\160\0\53\0\160\0\56\0\160\0\57\0\160" +
		"\0\60\0\160\0\61\0\160\0\62\0\160\0\63\0\160\0\64\0\160\0\65\0\160\0\66\0\160\0\67" +
		"\0\160\0\70\0\160\0\71\0\160\0\72\0\160\0\73\0\160\0\74\0\160\0\75\0\160\0\76\0\160" +
		"\0\77\0\160\0\100\0\160\0\101\0\160\0\102\0\160\0\103\0\160\0\104\0\160\0\105\0\160" +
		"\0\106\0\160\0\107\0\160\0\110\0\160\0\111\0\160\0\112\0\160\0\113\0\160\0\114\0" +
		"\160\0\uffff\uffff\ufffe\uffff\37\0\uffff\uffff\6\0\203\0\11\0\203\0\16\0\203\0\17" +
		"\0\203\0\24\0\203\0\25\0\203\0\27\0\203\0\30\0\203\0\42\0\203\0\44\0\203\0\45\0\203" +
		"\0\46\0\203\0\47\0\203\0\53\0\203\0\56\0\203\0\57\0\203\0\60\0\203\0\61\0\203\0\62" +
		"\0\203\0\63\0\203\0\64\0\203\0\65\0\203\0\66\0\203\0\67\0\203\0\70\0\203\0\71\0\203" +
		"\0\72\0\203\0\73\0\203\0\74\0\203\0\75\0\203\0\76\0\203\0\77\0\203\0\100\0\203\0" +
		"\101\0\203\0\102\0\203\0\103\0\203\0\104\0\203\0\105\0\203\0\106\0\203\0\107\0\203" +
		"\0\110\0\203\0\111\0\203\0\112\0\203\0\113\0\203\0\114\0\203\0\uffff\uffff\ufffe" +
		"\uffff\54\0\uffff\uffff\6\0\205\0\11\0\205\0\16\0\205\0\17\0\205\0\24\0\205\0\25" +
		"\0\205\0\27\0\205\0\30\0\205\0\37\0\205\0\42\0\205\0\44\0\205\0\45\0\205\0\46\0\205" +
		"\0\47\0\205\0\53\0\205\0\56\0\205\0\57\0\205\0\60\0\205\0\61\0\205\0\62\0\205\0\63" +
		"\0\205\0\64\0\205\0\65\0\205\0\66\0\205\0\67\0\205\0\70\0\205\0\71\0\205\0\72\0\205" +
		"\0\73\0\205\0\74\0\205\0\75\0\205\0\76\0\205\0\77\0\205\0\100\0\205\0\101\0\205\0" +
		"\102\0\205\0\103\0\205\0\104\0\205\0\105\0\205\0\106\0\205\0\107\0\205\0\110\0\205" +
		"\0\111\0\205\0\112\0\205\0\113\0\205\0\114\0\205\0\uffff\uffff\ufffe\uffff\34\0\uffff" +
		"\uffff\35\0\uffff\uffff\6\0\211\0\11\0\211\0\16\0\211\0\17\0\211\0\24\0\211\0\25" +
		"\0\211\0\27\0\211\0\30\0\211\0\37\0\211\0\42\0\211\0\44\0\211\0\45\0\211\0\46\0\211" +
		"\0\47\0\211\0\53\0\211\0\54\0\211\0\56\0\211\0\57\0\211\0\60\0\211\0\61\0\211\0\62" +
		"\0\211\0\63\0\211\0\64\0\211\0\65\0\211\0\66\0\211\0\67\0\211\0\70\0\211\0\71\0\211" +
		"\0\72\0\211\0\73\0\211\0\74\0\211\0\75\0\211\0\76\0\211\0\77\0\211\0\100\0\211\0" +
		"\101\0\211\0\102\0\211\0\103\0\211\0\104\0\211\0\105\0\211\0\106\0\211\0\107\0\211" +
		"\0\110\0\211\0\111\0\211\0\112\0\211\0\113\0\211\0\114\0\211\0\uffff\uffff\ufffe" +
		"\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\20\0\17\0\33\0\17\0\uffff\uffff\ufffe\uffff\47" +
		"\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\7\0\336\0\21\0\336\0\uffff\uffff\ufffe\uffff\21\0\uffff\uffff\7" +
		"\0\340\0\uffff\uffff\ufffe\uffff\21\0\uffff\uffff\7\0\340\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\uffff\uffff\ufffe\uffff" +
		"\24\0\uffff\uffff\0\0\332\0\6\0\332\0\10\0\332\0\22\0\332\0\46\0\332\0\47\0\332\0" +
		"\57\0\332\0\60\0\332\0\61\0\332\0\62\0\332\0\63\0\332\0\64\0\332\0\65\0\332\0\66" +
		"\0\332\0\67\0\332\0\70\0\332\0\71\0\332\0\72\0\332\0\73\0\332\0\74\0\332\0\75\0\332" +
		"\0\76\0\332\0\77\0\332\0\100\0\332\0\101\0\332\0\102\0\332\0\103\0\332\0\104\0\332" +
		"\0\105\0\332\0\106\0\332\0\107\0\332\0\110\0\332\0\111\0\332\0\112\0\332\0\113\0" +
		"\332\0\114\0\332\0\uffff\uffff\ufffe\uffff\14\0\uffff\uffff\15\0\uffff\uffff\12\0" +
		"\266\0\23\0\266\0\43\0\266\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff" +
		"\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\53\0\uffff\uffff\56\0\uffff\uffff\114\0" +
		"\uffff\uffff\11\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff" +
		"\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0" +
		"\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff" +
		"\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\114\0\uffff\uffff\11\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30" +
		"\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff" +
		"\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff" +
		"\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101" +
		"\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff" +
		"\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\114\0\uffff\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17" +
		"\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\114\0\uffff\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17" +
		"\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\114\0\uffff\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\42\0\uffff\uffff" +
		"\6\0\161\0\11\0\161\0\16\0\161\0\17\0\161\0\24\0\161\0\25\0\161\0\27\0\161\0\30\0" +
		"\161\0\44\0\161\0\45\0\161\0\46\0\161\0\47\0\161\0\53\0\161\0\56\0\161\0\57\0\161" +
		"\0\60\0\161\0\61\0\161\0\62\0\161\0\63\0\161\0\64\0\161\0\65\0\161\0\66\0\161\0\67" +
		"\0\161\0\70\0\161\0\71\0\161\0\72\0\161\0\73\0\161\0\74\0\161\0\75\0\161\0\76\0\161" +
		"\0\77\0\161\0\100\0\161\0\101\0\161\0\102\0\161\0\103\0\161\0\104\0\161\0\105\0\161" +
		"\0\106\0\161\0\107\0\161\0\110\0\161\0\111\0\161\0\112\0\161\0\113\0\161\0\114\0" +
		"\161\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff\20\0\246\0\33\0\246\0\uffff\uffff" +
		"\ufffe\uffff\21\0\uffff\uffff\7\0\340\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17" +
		"\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\46\0\uffff\uffff\47\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\114\0\uffff\uffff\11\0\342\0\16\0\342\0\uffff\uffff\ufffe\uffff" +
		"\114\0\uffff\uffff\0\0\334\0\6\0\334\0\10\0\334\0\22\0\334\0\46\0\334\0\47\0\334" +
		"\0\57\0\334\0\60\0\334\0\61\0\334\0\62\0\334\0\63\0\334\0\64\0\334\0\65\0\334\0\66" +
		"\0\334\0\67\0\334\0\70\0\334\0\71\0\334\0\72\0\334\0\73\0\334\0\74\0\334\0\75\0\334" +
		"\0\76\0\334\0\77\0\334\0\100\0\334\0\101\0\334\0\102\0\334\0\103\0\334\0\104\0\334" +
		"\0\105\0\334\0\106\0\334\0\107\0\334\0\110\0\334\0\111\0\334\0\112\0\334\0\113\0" +
		"\334\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff" +
		"\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff" +
		"\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101" +
		"\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff" +
		"\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\11\0\21\0\27" +
		"\0\21\0\42\0\21\0\uffff\uffff\ufffe\uffff\34\0\uffff\uffff\35\0\uffff\uffff\6\0\212" +
		"\0\11\0\212\0\16\0\212\0\17\0\212\0\24\0\212\0\25\0\212\0\27\0\212\0\30\0\212\0\37" +
		"\0\212\0\42\0\212\0\44\0\212\0\45\0\212\0\46\0\212\0\47\0\212\0\53\0\212\0\54\0\212" +
		"\0\56\0\212\0\57\0\212\0\60\0\212\0\61\0\212\0\62\0\212\0\63\0\212\0\64\0\212\0\65" +
		"\0\212\0\66\0\212\0\67\0\212\0\70\0\212\0\71\0\212\0\72\0\212\0\73\0\212\0\74\0\212" +
		"\0\75\0\212\0\76\0\212\0\77\0\212\0\100\0\212\0\101\0\212\0\102\0\212\0\103\0\212" +
		"\0\104\0\212\0\105\0\212\0\106\0\212\0\107\0\212\0\110\0\212\0\111\0\212\0\112\0" +
		"\212\0\113\0\212\0\114\0\212\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\11\0\342\0" +
		"\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\24" +
		"\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\114\0\uffff\uffff\11\0" +
		"\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\11\0\342\0\16" +
		"\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\11\0\342\0\16\0\342\0" +
		"\27\0\342\0\uffff\uffff\ufffe\uffff\42\0\210\0\6\0\210\0\11\0\210\0\16\0\210\0\17" +
		"\0\210\0\24\0\210\0\25\0\210\0\27\0\210\0\30\0\210\0\44\0\210\0\45\0\210\0\46\0\210" +
		"\0\47\0\210\0\53\0\210\0\56\0\210\0\57\0\210\0\60\0\210\0\61\0\210\0\62\0\210\0\63" +
		"\0\210\0\64\0\210\0\65\0\210\0\66\0\210\0\67\0\210\0\70\0\210\0\71\0\210\0\72\0\210" +
		"\0\73\0\210\0\74\0\210\0\75\0\210\0\76\0\210\0\77\0\210\0\100\0\210\0\101\0\210\0" +
		"\102\0\210\0\103\0\210\0\104\0\210\0\105\0\210\0\106\0\210\0\107\0\210\0\110\0\210" +
		"\0\111\0\210\0\112\0\210\0\113\0\210\0\114\0\210\0\uffff\uffff\ufffe\uffff\20\0\uffff" +
		"\uffff\7\0\76\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\17\0\uffff\uffff\22\0\uffff" +
		"\uffff\24\0\uffff\uffff\25\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\46\0\uffff\uffff\47\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\114\0\uffff" +
		"\uffff\11\0\342\0\16\0\342\0\uffff\uffff\ufffe\uffff\12\0\273\0\43\0\uffff\uffff" +
		"\23\0\273\0\uffff\uffff\ufffe\uffff\12\0\272\0\43\0\272\0\23\0\272\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\11\0\342\0\16\0\342\0\27\0\342\0\uffff\uffff\ufffe\uffff" +
		"\11\0\231\0\42\0\uffff\uffff\27\0\231\0\uffff\uffff\ufffe\uffff\11\0\232\0\42\0\232" +
		"\0\27\0\232\0\uffff\uffff\ufffe\uffff");

	private static final int[] lapg_sym_goto = TMLexer.unpack_int(176,
		"\0\0\2\0\22\0\41\0\41\0\41\0\41\0\100\0\110\0\112\0\117\0\122\0\132\0\133\0\134\0" +
		"\162\0\207\0\216\0\227\0\252\0\255\0\321\0\345\0\345\0\357\0\u0102\0\u0104\0\u0109" +
		"\0\u010b\0\u010e\0\u0113\0\u0114\0\u0115\0\u011a\0\u0121\0\u0129\0\u012c\0\u0145" +
		"\0\u015c\0\u0176\0\u01d3\0\u01e0\0\u01ed\0\u01f3\0\u01f4\0\u01f5\0\u01f6\0\u0212" +
		"\0\u0270\0\u02d1\0\u032f\0\u038d\0\u03ee\0\u044c\0\u04aa\0\u0508\0\u0566\0\u05c4" +
		"\0\u0622\0\u0680\0\u06de\0\u073c\0\u079a\0\u07f8\0\u0857\0\u08b6\0\u0914\0\u0972" +
		"\0\u09d5\0\u0a36\0\u0a97\0\u0af5\0\u0b53\0\u0bb1\0\u0c10\0\u0c6e\0\u0ccc\0\u0ce6" +
		"\0\u0ce6\0\u0ce8\0\u0ce8\0\u0ce9\0\u0cea\0\u0ceb\0\u0cec\0\u0ced\0\u0cee\0\u0cf0" +
		"\0\u0cf1\0\u0cf2\0\u0d22\0\u0d48\0\u0d5c\0\u0d61\0\u0d63\0\u0d64\0\u0d66\0\u0d68" +
		"\0\u0d6a\0\u0d6b\0\u0d6c\0\u0d6e\0\u0d6f\0\u0d71\0\u0d71\0\u0d73\0\u0d74\0\u0d76" +
		"\0\u0d78\0\u0d7c\0\u0d7f\0\u0d80\0\u0d81\0\u0d83\0\u0d85\0\u0d86\0\u0d88\0\u0d8a" +
		"\0\u0d8b\0\u0d95\0\u0d9f\0\u0daa\0\u0db5\0\u0dc1\0\u0ddc\0\u0def\0\u0dfd\0\u0e11" +
		"\0\u0e12\0\u0e26\0\u0e28\0\u0e3c\0\u0e50\0\u0e66\0\u0e7e\0\u0e96\0\u0eaa\0\u0ec2" +
		"\0\u0edb\0\u0ef7\0\u0efc\0\u0f00\0\u0f16\0\u0f2c\0\u0f43\0\u0f44\0\u0f46\0\u0f48" +
		"\0\u0f52\0\u0f53\0\u0f54\0\u0f57\0\u0f59\0\u0f5b\0\u0f5f\0\u0f62\0\u0f65\0\u0f6b" +
		"\0\u0f6c\0\u0f6d\0\u0f6e\0\u0f6f\0\u0f71\0\u0f7e\0\u0f81\0\u0f84\0\u0f99\0\u0fb3" +
		"\0\u0fb5\0\u0fb6\0\u0fb7\0\u0fb8\0\u0fb9\0\u0fbc\0\u0fbf\0\u0fda\0");

	private static final int[] lapg_sym_from = TMLexer.unpack_int(4058,
		"\u018f\0\u0190\0\1\0\6\0\36\0\41\0\61\0\72\0\106\0\116\0\251\0\350\0\370\0\u010f" +
		"\0\u012a\0\u0130\0\u0131\0\u0155\0\1\0\6\0\41\0\55\0\72\0\106\0\116\0\236\0\251\0" +
		"\350\0\u010f\0\u012a\0\u0130\0\u0131\0\u0155\0\105\0\130\0\137\0\160\0\216\0\255" +
		"\0\271\0\272\0\274\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0100\0\u0101\0\u0102" +
		"\0\u0104\0\u0105\0\u0109\0\u011e\0\u0120\0\u0121\0\u0148\0\u0149\0\u014c\0\u014f" +
		"\0\u0160\0\u017a\0\157\0\226\0\227\0\233\0\330\0\332\0\333\0\u0122\0\37\0\64\0\266" +
		"\0\u0142\0\u0173\0\u0188\0\u0189\0\361\0\u016d\0\u016e\0\63\0\126\0\214\0\242\0\250" +
		"\0\264\0\347\0\u0114\0\357\0\357\0\34\0\60\0\104\0\121\0\234\0\240\0\246\0\250\0" +
		"\267\0\343\0\344\0\347\0\u011c\0\u011d\0\u011f\0\u0127\0\u012c\0\u015c\0\u015e\0" +
		"\u015f\0\u0169\0\u0183\0\21\0\216\0\255\0\271\0\272\0\275\0\326\0\327\0\331\0\363" +
		"\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160" +
		"\0\24\0\50\0\76\0\145\0\240\0\316\0\u0159\0\47\0\75\0\152\0\264\0\311\0\324\0\325" +
		"\0\u0117\0\u0143\0\1\0\6\0\41\0\105\0\106\0\116\0\130\0\216\0\251\0\255\0\326\0\327" +
		"\0\331\0\371\0\376\0\u011e\0\u0120\0\u0121\0\u0160\0\25\0\145\0\361\0\20\0\30\0\32" +
		"\0\216\0\255\0\260\0\262\0\271\0\272\0\275\0\311\0\326\0\327\0\331\0\336\0\363\0" +
		"\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u011e" +
		"\0\u0120\0\u0121\0\u013d\0\u013e\0\u0149\0\u0160\0\u0176\0\u0178\0\216\0\255\0\271" +
		"\0\272\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d" +
		"\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\54\0\77\0\102\0\362\0\366\0\u013c\0\u0142" +
		"\0\u0165\0\u016f\0\u0173\0\216\0\255\0\271\0\272\0\275\0\326\0\327\0\331\0\363\0" +
		"\371\0\376\0\u0100\0\u0102\0\u0105\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\370" +
		"\0\u013a\0\10\0\157\0\233\0\261\0\u013f\0\51\0\316\0\307\0\u0145\0\u0186\0\26\0\73" +
		"\0\307\0\u0145\0\u0186\0\264\0\304\0\254\0\256\0\u0132\0\u0134\0\u0139\0\26\0\73" +
		"\0\372\0\u013d\0\u013e\0\u0176\0\u0178\0\276\0\366\0\u010a\0\u0142\0\u0152\0\u0173" +
		"\0\u0188\0\u0189\0\361\0\u016d\0\u016e\0\216\0\255\0\271\0\272\0\275\0\311\0\326" +
		"\0\327\0\331\0\363\0\371\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106" +
		"\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\137\0\160\0\165\0\216\0\255\0" +
		"\271\0\272\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d" +
		"\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\1\0\6\0\37\0\41\0\106\0\116\0\130\0\156" +
		"\0\160\0\216\0\251\0\255\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0102\0\u0105" +
		"\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223" +
		"\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0" +
		"\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373" +
		"\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118" +
		"\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e" +
		"\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\6\0\41\0\72" +
		"\0\106\0\116\0\251\0\350\0\u010f\0\u012a\0\u0130\0\u0131\0\u0155\0\1\0\6\0\41\0\72" +
		"\0\106\0\116\0\251\0\350\0\u010f\0\u012a\0\u0130\0\u0131\0\u0155\0\1\0\6\0\41\0\106" +
		"\0\116\0\251\0\363\0\306\0\22\0\216\0\243\0\244\0\255\0\271\0\272\0\275\0\311\0\326" +
		"\0\327\0\331\0\342\0\363\0\371\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0" +
		"\u0106\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\1\0\2\0\6\0\13\0\26\0\31" +
		"\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\124\0\125" +
		"\0\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0" +
		"\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272" +
		"\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0" +
		"\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d" +
		"\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136" +
		"\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178" +
		"\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\157\0\160\0\166\0\170\0" +
		"\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\227\0\233\0\246\0\247" +
		"\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0" +
		"\327\0\331\0\333\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374" +
		"\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e" +
		"\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f" +
		"\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217" +
		"\0\221\0\222\0\223\0\246\0\247\0\251\0\252\0\253\0\254\0\255\0\256\0\257\0\271\0" +
		"\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363" +
		"\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d" +
		"\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136" +
		"\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178" +
		"\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171\0" +
		"\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\252\0\253" +
		"\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0" +
		"\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100" +
		"\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121" +
		"\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155" +
		"\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\156\0\157\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222" +
		"\0\223\0\227\0\233\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0" +
		"\275\0\311\0\315\0\321\0\326\0\327\0\331\0\333\0\341\0\350\0\353\0\354\0\356\0\363" +
		"\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d" +
		"\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136" +
		"\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178" +
		"\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\155\0\156\0\160\0\166\0\170\0" +
		"\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253" +
		"\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0" +
		"\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100" +
		"\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121" +
		"\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155" +
		"\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\155\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222" +
		"\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0" +
		"\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372" +
		"\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115" +
		"\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d" +
		"\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\137\0\143\0\155\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0" +
		"\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256" +
		"\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0" +
		"\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105" +
		"\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132" +
		"\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f" +
		"\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\155\0\156\0\160" +
		"\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0" +
		"\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326" +
		"\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0" +
		"\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e" +
		"\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f" +
		"\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\137\0\143\0\155\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216" +
		"\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0" +
		"\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363" +
		"\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d" +
		"\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136" +
		"\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178" +
		"\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\155\0\156\0\160\0\166\0\170\0" +
		"\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253" +
		"\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0" +
		"\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100" +
		"\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121" +
		"\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155" +
		"\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\156\0\160\0\166\0\170\0\171\0\176\0\202\0\203\0\211\0\213\0\216\0\217\0\221\0\222" +
		"\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0" +
		"\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372" +
		"\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115" +
		"\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d" +
		"\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\203\0" +
		"\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256" +
		"\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0" +
		"\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105" +
		"\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132" +
		"\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f" +
		"\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\155\0\156\0\160" +
		"\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0" +
		"\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326" +
		"\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0" +
		"\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e" +
		"\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f" +
		"\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\137\0\143\0\155\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216" +
		"\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0" +
		"\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363" +
		"\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d" +
		"\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136" +
		"\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178" +
		"\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\155\0\156\0\160\0\166\0\170\0" +
		"\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253" +
		"\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0" +
		"\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100" +
		"\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121" +
		"\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155" +
		"\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\155\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\212\0\213\0\216\0\217\0\221" +
		"\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0" +
		"\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371" +
		"\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0" +
		"\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0" +
		"\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0" +
		"\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106" +
		"\0\116\0\117\0\125\0\130\0\137\0\143\0\155\0\156\0\160\0\166\0\170\0\171\0\176\0" +
		"\202\0\211\0\212\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254" +
		"\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0" +
		"\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0" +
		"\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0" +
		"\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0" +
		"\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0" +
		"\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223" +
		"\0\237\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0" +
		"\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372" +
		"\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115" +
		"\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d" +
		"\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0" +
		"\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257" +
		"\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0" +
		"\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106" +
		"\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u0123\0\u012a\0\u0132" +
		"\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f" +
		"\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\157\0\160" +
		"\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\220\0\221\0\222\0\223\0" +
		"\227\0\233\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311" +
		"\0\315\0\321\0\326\0\327\0\331\0\333\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0" +
		"\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f" +
		"\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u0123\0\u012a\0\u0132\0\u0134\0\u0136" +
		"\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178" +
		"\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\157\0\160\0\166\0\170\0" +
		"\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\227\0\233\0\246\0\247" +
		"\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0" +
		"\327\0\331\0\333\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374" +
		"\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e" +
		"\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f" +
		"\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\137\0\143\0\156\0\157\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216" +
		"\0\217\0\221\0\222\0\223\0\227\0\233\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0" +
		"\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\333\0\341\0\350\0\353" +
		"\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105" +
		"\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132" +
		"\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f" +
		"\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\160\0\166" +
		"\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0" +
		"\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327" +
		"\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0" +
		"\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120" +
		"\0\u0121\0\u0123\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f" +
		"\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217" +
		"\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0" +
		"\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364" +
		"\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f" +
		"\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u0123\0\u012a\0\u0132\0\u0134\0\u0136" +
		"\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178" +
		"\0\u017f\0\0\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0" +
		"\73\0\105\0\106\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171" +
		"\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0" +
		"\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341" +
		"\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100" +
		"\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121" +
		"\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155" +
		"\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\20\0\26\0\31\0\35\0\36" +
		"\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\101\0\105\0\106\0\116\0\117\0\125\0\130\0\137" +
		"\0\143\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0" +
		"\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311" +
		"\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0" +
		"\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115" +
		"\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d" +
		"\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\62\0\72\0\73\0\105\0\106" +
		"\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\160\0\166\0\170\0\171\0\176\0\202\0" +
		"\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0\247\0\251\0\253\0\254\0\255\0\256" +
		"\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\341\0\350\0\353\0" +
		"\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105" +
		"\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e\0\u0120\0\u0121\0\u012a\0\u0132" +
		"\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f\0\u0149\0\u0155\0\u0160\0\u016f" +
		"\0\u0176\0\u0178\0\u017f\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44" +
		"\0\53\0\72\0\73\0\105\0\106\0\107\0\116\0\117\0\125\0\130\0\137\0\143\0\156\0\160" +
		"\0\166\0\170\0\171\0\176\0\202\0\211\0\213\0\216\0\217\0\221\0\222\0\223\0\246\0" +
		"\247\0\251\0\253\0\254\0\255\0\256\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326" +
		"\0\327\0\331\0\341\0\350\0\353\0\354\0\356\0\363\0\364\0\371\0\372\0\373\0\374\0" +
		"\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u0115\0\u0118\0\u011e" +
		"\0\u0120\0\u0121\0\u012a\0\u0132\0\u0134\0\u0136\0\u0139\0\u013d\0\u013e\0\u013f" +
		"\0\u0149\0\u0155\0\u0160\0\u016f\0\u0176\0\u0178\0\u017f\0\127\0\157\0\216\0\227" +
		"\0\233\0\255\0\271\0\272\0\275\0\326\0\327\0\331\0\333\0\363\0\371\0\376\0\u0100" +
		"\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121\0\u0125\0\u0149\0\u0160\0\147\0" +
		"\175\0\3\0\0\0\22\0\0\0\37\0\64\0\20\0\101\0\22\0\37\0\26\0\43\0\44\0\73\0\105\0" +
		"\125\0\130\0\137\0\160\0\166\0\171\0\213\0\216\0\217\0\222\0\223\0\247\0\254\0\255" +
		"\0\257\0\271\0\272\0\275\0\311\0\315\0\321\0\326\0\327\0\331\0\356\0\363\0\371\0" +
		"\373\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u0115\0\u011e\0\u0120" +
		"\0\u0121\0\u0132\0\u0134\0\u0149\0\u0160\0\1\0\6\0\41\0\106\0\116\0\216\0\251\0\255" +
		"\0\271\0\272\0\275\0\311\0\326\0\327\0\331\0\363\0\371\0\372\0\373\0\374\0\375\0" +
		"\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u010f\0\u011e\0\u0120\0\u0121\0\u013d" +
		"\0\u013e\0\u013f\0\u0149\0\u0160\0\u0176\0\u0178\0\72\0\143\0\170\0\176\0\211\0\221" +
		"\0\246\0\256\0\341\0\350\0\353\0\354\0\364\0\u0118\0\u012a\0\u0136\0\u0139\0\u0155" +
		"\0\u016f\0\u017f\0\127\0\157\0\227\0\233\0\333\0\147\0\175\0\105\0\105\0\130\0\105" +
		"\0\130\0\105\0\130\0\336\0\u0123\0\105\0\130\0\125\0\105\0\130\0\125\0\171\0\137" +
		"\0\137\0\160\0\137\0\160\0\157\0\227\0\233\0\333\0\324\0\325\0\u0117\0\155\0\155" +
		"\0\137\0\160\0\137\0\160\0\176\0\176\0\341\0\211\0\u0136\0\u0118\0\216\0\255\0\326" +
		"\0\327\0\331\0\371\0\u011e\0\u0120\0\u0121\0\u0160\0\216\0\255\0\326\0\327\0\331" +
		"\0\371\0\u011e\0\u0120\0\u0121\0\u0160\0\216\0\255\0\326\0\327\0\331\0\371\0\376" +
		"\0\u011e\0\u0120\0\u0121\0\u0160\0\216\0\255\0\326\0\327\0\331\0\371\0\376\0\u011e" +
		"\0\u0120\0\u0121\0\u0160\0\216\0\255\0\271\0\326\0\327\0\331\0\371\0\376\0\u011e" +
		"\0\u0120\0\u0121\0\u0160\0\216\0\255\0\271\0\272\0\274\0\275\0\326\0\327\0\331\0" +
		"\363\0\371\0\376\0\u0100\0\u0101\0\u0102\0\u0104\0\u0105\0\u0109\0\u011e\0\u0120" +
		"\0\u0121\0\u0148\0\u0149\0\u014c\0\u014f\0\u0160\0\u017a\0\216\0\255\0\271\0\272" +
		"\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u011e\0\u0120" +
		"\0\u0121\0\u0149\0\u0160\0\216\0\255\0\271\0\272\0\326\0\327\0\331\0\371\0\376\0" +
		"\u0100\0\u011e\0\u0120\0\u0121\0\u0160\0\216\0\255\0\271\0\272\0\275\0\326\0\327" +
		"\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121" +
		"\0\u0149\0\u0160\0\256\0\216\0\255\0\271\0\272\0\275\0\326\0\327\0\331\0\363\0\371" +
		"\0\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0" +
		"\256\0\u0139\0\216\0\255\0\271\0\272\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0" +
		"\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\216\0\255" +
		"\0\271\0\272\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0" +
		"\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\216\0\255\0\271\0\272\0\275\0\311" +
		"\0\326\0\327\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u011e" +
		"\0\u0120\0\u0121\0\u0149\0\u0160\0\216\0\255\0\271\0\272\0\275\0\311\0\326\0\327" +
		"\0\331\0\363\0\371\0\373\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u011e" +
		"\0\u0120\0\u0121\0\u0149\0\u0160\0\216\0\255\0\271\0\272\0\275\0\311\0\326\0\327" +
		"\0\331\0\363\0\371\0\373\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u011e" +
		"\0\u0120\0\u0121\0\u0149\0\u0160\0\216\0\255\0\271\0\272\0\275\0\326\0\327\0\331" +
		"\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149" +
		"\0\u0160\0\216\0\255\0\271\0\272\0\275\0\311\0\326\0\327\0\331\0\363\0\371\0\373" +
		"\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149" +
		"\0\u0160\0\216\0\255\0\271\0\272\0\275\0\311\0\326\0\327\0\331\0\363\0\371\0\373" +
		"\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d\0\u011e\0\u0120\0\u0121" +
		"\0\u0149\0\u0160\0\216\0\243\0\244\0\255\0\271\0\272\0\275\0\311\0\326\0\327\0\331" +
		"\0\342\0\363\0\371\0\373\0\374\0\375\0\376\0\u0100\0\u0102\0\u0105\0\u0106\0\u010d" +
		"\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\372\0\u013d\0\u013e\0\u0176\0\u0178\0" +
		"\372\0\u013d\0\u0176\0\u0178\0\137\0\160\0\216\0\255\0\271\0\272\0\275\0\326\0\327" +
		"\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121" +
		"\0\u0149\0\u0160\0\137\0\160\0\216\0\255\0\271\0\272\0\275\0\326\0\327\0\331\0\363" +
		"\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160" +
		"\0\137\0\160\0\165\0\216\0\255\0\271\0\272\0\275\0\326\0\327\0\331\0\363\0\371\0" +
		"\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\217" +
		"\0\157\0\233\0\217\0\u0115\0\26\0\43\0\44\0\73\0\217\0\254\0\356\0\u0115\0\u0132" +
		"\0\u0134\0\26\0\26\0\10\0\261\0\u013f\0\26\0\73\0\155\0\212\0\72\0\350\0\u012a\0" +
		"\u0155\0\254\0\u0132\0\u0134\0\254\0\u0132\0\u0134\0\1\0\6\0\41\0\106\0\116\0\251" +
		"\0\6\0\6\0\53\0\53\0\53\0\117\0\1\0\6\0\41\0\72\0\106\0\116\0\251\0\350\0\u010f\0" +
		"\u012a\0\u0130\0\u0131\0\u0155\0\2\0\13\0\31\0\2\0\13\0\31\0\216\0\255\0\271\0\272" +
		"\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0100\0\u0102\0\u0105\0\u010d\0\u011e" +
		"\0\u0120\0\u0121\0\u0125\0\u0149\0\u0160\0\1\0\6\0\37\0\41\0\106\0\116\0\130\0\156" +
		"\0\160\0\216\0\251\0\255\0\275\0\326\0\327\0\331\0\363\0\371\0\376\0\u0102\0\u0105" +
		"\0\u011e\0\u0120\0\u0121\0\u0149\0\u0160\0\20\0\101\0\127\0\236\0\336\0\u0125\0\222" +
		"\0\223\0\321\0\324\0\325\0\u0117\0\216\0\255\0\271\0\272\0\274\0\275\0\326\0\327" +
		"\0\331\0\363\0\371\0\376\0\u0100\0\u0101\0\u0102\0\u0104\0\u0105\0\u0109\0\u011e" +
		"\0\u0120\0\u0121\0\u0148\0\u0149\0\u014c\0\u014f\0\u0160\0\u017a\0");

	private static final int[] lapg_sym_to = TMLexer.unpack_int(4058,
		"\u0191\0\u0192\0\4\0\4\0\60\0\4\0\104\0\4\0\4\0\4\0\4\0\4\0\u013a\0\4\0\4\0\4\0\4" +
		"\0\4\0\5\0\5\0\5\0\102\0\5\0\5\0\5\0\335\0\5\0\5\0\5\0\5\0\5\0\5\0\5\0\124\0\124" +
		"\0\155\0\155\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0" +
		"\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252\0\252" +
		"\0\252\0\252\0\216\0\326\0\327\0\331\0\u011e\0\u0120\0\u0121\0\u0160\0\62\0\107\0" +
		"\376\0\u0176\0\u0176\0\u0176\0\u0176\0\u0132\0\u0132\0\u0132\0\106\0\147\0\251\0" +
		"\342\0\350\0\373\0\u012a\0\u0155\0\u0130\0\u0131\0\56\0\103\0\123\0\142\0\334\0\340" +
		"\0\345\0\351\0\377\0\u0128\0\u0129\0\u012b\0\u015a\0\u015b\0\u015d\0\u0168\0\u016a" +
		"\0\u0180\0\u0181\0\u0182\0\u0185\0\u018c\0\35\0\253\0\253\0\253\0\253\0\253\0\253" +
		"\0\253\0\253\0\253\0\253\0\253\0\253\0\253\0\253\0\253\0\253\0\253\0\253\0\253\0" +
		"\253\0\41\0\73\0\117\0\171\0\341\0\u0115\0\u017f\0\72\0\116\0\175\0\374\0\u0112\0" +
		"\u0118\0\u0118\0\u0118\0\374\0\6\0\6\0\6\0\125\0\6\0\6\0\125\0\254\0\6\0\254\0\254" +
		"\0\254\0\254\0\254\0\254\0\254\0\254\0\254\0\254\0\42\0\172\0\u0133\0\31\0\53\0\55" +
		"\0\255\0\255\0\371\0\372\0\255\0\255\0\255\0\255\0\255\0\255\0\255\0\u0123\0\255" +
		"\0\255\0\u013d\0\255\0\255\0\255\0\255\0\255\0\255\0\255\0\255\0\255\0\255\0\255" +
		"\0\255\0\u013d\0\u013d\0\255\0\255\0\u013d\0\u013d\0\256\0\256\0\256\0\256\0\256" +
		"\0\256\0\256\0\256\0\256\0\256\0\256\0\256\0\256\0\256\0\256\0\256\0\256\0\256\0" +
		"\256\0\256\0\101\0\120\0\122\0\u0135\0\u0138\0\u0172\0\u0177\0\u0184\0\u0186\0\u0187" +
		"\0\257\0\257\0\257\0\257\0\257\0\257\0\257\0\257\0\257\0\257\0\257\0\257\0\257\0" +
		"\257\0\257\0\257\0\257\0\257\0\257\0\u013b\0\u0171\0\26\0\217\0\217\0\26\0\26\0\74" +
		"\0\u0116\0\u0110\0\u0110\0\u018d\0\43\0\43\0\u0111\0\u0111\0\u018e\0\375\0\u010e" +
		"\0\356\0\364\0\356\0\356\0\364\0\44\0\44\0\u013e\0\u013e\0\u013e\0\u013e\0\u013e" +
		"\0\u010d\0\u0139\0\u010d\0\u0178\0\u010d\0\u0178\0\u0178\0\u0178\0\u0134\0\u0134" +
		"\0\u0134\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260" +
		"\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0\260\0" +
		"\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156" +
		"\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\156\0\7\0\7\0\7\0\7\0\7\0\7\0" +
		"\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0" +
		"\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126" +
		"\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111" +
		"\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261" +
		"\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261" +
		"\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0" +
		"\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261" +
		"\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11" +
		"\0\11\0\11\0\11\0\11\0\11\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0\12" +
		"\0\12\0\12\0\13\0\13\0\13\0\13\0\13\0\13\0\u0136\0\u010f\0\36\0\262\0\262\0\262\0" +
		"\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262" +
		"\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\262\0\10\0\17\0\10" +
		"\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75" +
		"\0\143\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0" +
		"\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261" +
		"\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111" +
		"\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0" +
		"\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111" +
		"\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0" +
		"\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0" +
		"\220\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\220" +
		"\0\220\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0" +
		"\45\0\261\0\261\0\261\0\220\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f" +
		"\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261" +
		"\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0" +
		"\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111" +
		"\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\353\0" +
		"\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0" +
		"\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261" +
		"\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111" +
		"\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0" +
		"\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45" +
		"\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\354\0\355\0\45\0\261\0\111\0\45\0" +
		"\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0" +
		"\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10" +
		"\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0" +
		"\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0" +
		"\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111" +
		"\0\214\0\221\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0" +
		"\45\0\221\0\221\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261" +
		"\0\45\0\45\0\261\0\261\0\261\0\221\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261" +
		"\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0" +
		"\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261" +
		"\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0" +
		"\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\176\0\214\0" +
		"\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45" +
		"\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0" +
		"\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261" +
		"\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45" +
		"\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0" +
		"\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126" +
		"\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\177\0\214\0\45\0\45\0\111\0\45\0\111\0\242" +
		"\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0" +
		"\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0" +
		"\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261" +
		"\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0" +
		"\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0" +
		"\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45" +
		"\0\111\0\200\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111" +
		"\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0" +
		"\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f" +
		"\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261" +
		"\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0" +
		"\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\201\0\214\0\45\0\45" +
		"\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0" +
		"\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0" +
		"\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261" +
		"\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111" +
		"\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0" +
		"\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\45\0\126\0\45\0\111\0\202\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111" +
		"\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261" +
		"\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261" +
		"\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0" +
		"\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261" +
		"\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61" +
		"\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0" +
		"\203\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45" +
		"\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0" +
		"\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10" +
		"\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111" +
		"\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242" +
		"\0\243\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0" +
		"\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0" +
		"\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261" +
		"\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f" +
		"\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0" +
		"\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\244\0\111\0\45\0\261\0\315\0\111" +
		"\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0" +
		"\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f" +
		"\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261" +
		"\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0" +
		"\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\204\0\214\0\45\0\45" +
		"\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0" +
		"\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0" +
		"\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261" +
		"\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111" +
		"\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0" +
		"\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\45\0\126\0\45\0\111\0\205\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111" +
		"\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261" +
		"\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261" +
		"\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0" +
		"\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261" +
		"\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61" +
		"\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0" +
		"\206\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45" +
		"\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0" +
		"\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10" +
		"\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111" +
		"\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\207\0\214\0\45\0\45\0\111\0\45\0\111" +
		"\0\242\0\111\0\207\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0" +
		"\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0" +
		"\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261" +
		"\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f" +
		"\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45" +
		"\0\126\0\45\0\111\0\210\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\210\0\45\0" +
		"\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261" +
		"\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111" +
		"\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0" +
		"\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111" +
		"\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0" +
		"\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0" +
		"\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\337\0\111" +
		"\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0" +
		"\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261" +
		"\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45" +
		"\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0" +
		"\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242" +
		"\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0" +
		"\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0" +
		"\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261" +
		"\0\10\0\315\0\111\0\261\0\261\0\261\0\u0161\0\111\0\45\0\45\0\111\0\111\0\u013f\0" +
		"\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45" +
		"\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126" +
		"\0\45\0\111\0\214\0\222\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0" +
		"\321\0\111\0\45\0\45\0\222\0\222\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261" +
		"\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\222\0\111\0\111\0\111\0\111\0\45" +
		"\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0" +
		"\10\0\315\0\111\0\261\0\261\0\261\0\u0162\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f" +
		"\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0" +
		"\45\0\111\0\214\0\223\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111" +
		"\0\45\0\45\0\223\0\223\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0" +
		"\261\0\261\0\45\0\45\0\261\0\261\0\261\0\223\0\111\0\111\0\111\0\111\0\45\0\261\0" +
		"\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315" +
		"\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0" +
		"\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0" +
		"\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214" +
		"\0\224\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0" +
		"\224\0\224\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0" +
		"\45\0\45\0\261\0\261\0\261\0\224\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0" +
		"\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261" +
		"\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0" +
		"\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45" +
		"\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45" +
		"\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0" +
		"\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0" +
		"\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261" +
		"\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\u0163\0\111\0\45\0" +
		"\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111" +
		"\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126" +
		"\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111" +
		"\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261" +
		"\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261" +
		"\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0" +
		"\315\0\111\0\261\0\261\0\261\0\u0164\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f" +
		"\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\2\0\10\0\17\0\10\0\17\0\45" +
		"\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\45\0\126" +
		"\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0" +
		"\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45" +
		"\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0" +
		"\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261" +
		"\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0" +
		"\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\32\0\45\0\17\0\57\0\61\0\63\0\10\0\45" +
		"\0\45\0\75\0\111\0\45\0\32\0\126\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45" +
		"\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0" +
		"\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261" +
		"\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0" +
		"\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0" +
		"\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10" +
		"\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\105\0\111\0\45\0\126" +
		"\0\10\0\10\0\75\0\45\0\126\0\45\0\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111" +
		"\0\45\0\261\0\315\0\111\0\45\0\45\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261" +
		"\0\261\0\261\0\261\0\45\0\45\0\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261" +
		"\0\111\0\261\0\u013f\0\261\0\10\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0" +
		"\315\0\111\0\261\0\261\0\261\0\111\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261" +
		"\0\111\0\261\0\111\0\u013f\0\u013f\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61" +
		"\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\137\0\10\0\75\0\45\0\126\0\45\0" +
		"\111\0\214\0\45\0\45\0\111\0\45\0\111\0\242\0\111\0\45\0\261\0\315\0\111\0\45\0\45" +
		"\0\111\0\45\0\10\0\355\0\45\0\261\0\111\0\45\0\261\0\261\0\261\0\261\0\45\0\45\0" +
		"\261\0\261\0\261\0\111\0\111\0\111\0\111\0\45\0\261\0\111\0\261\0\u013f\0\261\0\10" +
		"\0\261\0\261\0\261\0\261\0\261\0\261\0\261\0\10\0\315\0\111\0\261\0\261\0\261\0\111" +
		"\0\45\0\45\0\111\0\111\0\u013f\0\u013f\0\10\0\261\0\111\0\261\0\111\0\u013f\0\u013f" +
		"\0\111\0\150\0\150\0\263\0\150\0\150\0\263\0\263\0\263\0\263\0\263\0\263\0\263\0" +
		"\150\0\263\0\263\0\263\0\263\0\263\0\263\0\263\0\263\0\263\0\263\0\263\0\263\0\263" +
		"\0\173\0\173\0\22\0\u018f\0\37\0\3\0\64\0\110\0\33\0\33\0\40\0\65\0\46\0\46\0\46" +
		"\0\46\0\127\0\144\0\127\0\157\0\157\0\233\0\144\0\250\0\264\0\46\0\323\0\323\0\347" +
		"\0\46\0\264\0\370\0\264\0\264\0\264\0\264\0\u0114\0\323\0\264\0\264\0\264\0\46\0" +
		"\264\0\264\0\u0143\0\u0143\0\264\0\264\0\264\0\264\0\264\0\264\0\46\0\264\0\264\0" +
		"\264\0\46\0\46\0\264\0\264\0\14\0\14\0\14\0\14\0\14\0\265\0\14\0\265\0\265\0\265" +
		"\0\265\0\265\0\265\0\265\0\265\0\265\0\265\0\u0140\0\265\0\265\0\265\0\265\0\265" +
		"\0\265\0\265\0\265\0\265\0\u0153\0\265\0\265\0\265\0\u0140\0\u0140\0\u0175\0\265" +
		"\0\265\0\u0140\0\u0140\0\112\0\170\0\234\0\237\0\245\0\322\0\346\0\365\0\237\0\112" +
		"\0\u012d\0\u012e\0\u0137\0\u0158\0\112\0\245\0\365\0\112\0\346\0\u018b\0\151\0\225" +
		"\0\225\0\225\0\225\0\174\0\236\0\130\0\131\0\153\0\132\0\132\0\133\0\133\0\u0124" +
		"\0\u0165\0\134\0\134\0\145\0\135\0\135\0\146\0\235\0\160\0\161\0\230\0\162\0\162" +
		"\0\226\0\330\0\332\0\u0122\0\u0119\0\u0119\0\u0119\0\211\0\212\0\163\0\163\0\164" +
		"\0\164\0\240\0\241\0\u0126\0\246\0\u016f\0\u0159\0\266\0\266\0\266\0\266\0\266\0" +
		"\266\0\266\0\266\0\266\0\266\0\267\0\362\0\u011c\0\u011d\0\u011f\0\u013c\0\u015c" +
		"\0\u015e\0\u015f\0\u0183\0\270\0\270\0\270\0\270\0\270\0\270\0\u0147\0\270\0\270" +
		"\0\270\0\270\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0\271\0" +
		"\272\0\272\0\u0100\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\272\0\273\0" +
		"\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273" +
		"\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\273\0\274\0" +
		"\274\0\u0101\0\u0104\0\u0109\0\274\0\274\0\274\0\u0109\0\274\0\274\0\u0148\0\u014c" +
		"\0\u014f\0\274\0\274\0\274\0\u017a\0\274\0\275\0\363\0\u0102\0\u0105\0\275\0\275" +
		"\0\275\0\275\0\275\0\u0149\0\275\0\275\0\275\0\275\0\276\0\276\0\276\0\276\0\u010a" +
		"\0\276\0\276\0\276\0\u010a\0\276\0\276\0\276\0\u010a\0\u010a\0\u0152\0\276\0\276" +
		"\0\276\0\u010a\0\276\0\366\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277" +
		"\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\367\0\u0170" +
		"\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0" +
		"\300\0\300\0\300\0\300\0\300\0\300\0\300\0\301\0\301\0\301\0\301\0\301\0\301\0\301" +
		"\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0" +
		"\302\0\302\0\302\0\302\0\302\0\u0113\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0" +
		"\302\0\302\0\u0113\0\302\0\302\0\302\0\302\0\302\0\302\0\303\0\303\0\303\0\303\0" +
		"\303\0\303\0\303\0\303\0\303\0\303\0\303\0\u0144\0\u0146\0\303\0\303\0\303\0\303" +
		"\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\304\0\304\0\304\0\304\0\304\0\304\0" +
		"\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\304" +
		"\0\304\0\304\0\304\0\304\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0" +
		"\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\306\0\306\0\306" +
		"\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0" +
		"\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\307\0\307\0\307\0\307\0\307\0\307" +
		"\0\307\0\307\0\307\0\307\0\307\0\307\0\u0145\0\307\0\307\0\307\0\307\0\307\0\307" +
		"\0\307\0\307\0\307\0\307\0\307\0\307\0\310\0\343\0\344\0\310\0\310\0\310\0\310\0" +
		"\310\0\310\0\310\0\310\0\u0127\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0" +
		"\310\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0\u0141\0\u0141\0\u0174\0\u0141\0" +
		"\u0141\0\u0142\0\u0173\0\u0188\0\u0189\0\165\0\165\0\165\0\165\0\165\0\165\0\165" +
		"\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0" +
		"\165\0\165\0\166\0\166\0\311\0\311\0\311\0\u0106\0\u0106\0\311\0\311\0\311\0\u0106" +
		"\0\311\0\311\0\u0106\0\u0106\0\u0106\0\u0106\0\311\0\311\0\311\0\u0106\0\311\0\167" +
		"\0\167\0\232\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0" +
		"\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\167\0\316\0\227\0\333\0\317\0\u0156" +
		"\0\47\0\70\0\71\0\47\0\320\0\357\0\u012f\0\320\0\357\0\357\0\50\0\51\0\27\0\27\0" +
		"\27\0\52\0\115\0\213\0\247\0\113\0\u012c\0\u0169\0\u017e\0\360\0\360\0\360\0\361" +
		"\0\u016d\0\u016e\0\u0190\0\23\0\67\0\136\0\140\0\352\0\24\0\25\0\76\0\77\0\100\0" +
		"\141\0\15\0\15\0\15\0\114\0\15\0\15\0\15\0\114\0\u0154\0\114\0\u016b\0\u016c\0\114" +
		"\0\20\0\30\0\54\0\21\0\21\0\21\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0" +
		"\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\312\0\u0166\0\312\0\312\0" +
		"\16\0\16\0\66\0\16\0\16\0\16\0\154\0\215\0\231\0\313\0\16\0\313\0\u010b\0\313\0\313" +
		"\0\313\0\u010b\0\313\0\313\0\u010b\0\u010b\0\313\0\313\0\313\0\u010b\0\313\0\34\0" +
		"\121\0\152\0\336\0\u0125\0\u0167\0\324\0\325\0\u0117\0\u011a\0\u011b\0\u0157\0\314" +
		"\0\314\0\u0103\0\u0107\0\u0108\0\u010c\0\314\0\314\0\314\0\u010c\0\314\0\314\0\u014a" +
		"\0\u014b\0\u014d\0\u014e\0\u0150\0\u0151\0\314\0\314\0\314\0\u0179\0\u017b\0\u017c" +
		"\0\u017d\0\314\0\u018a\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(227,
		"\2\0\0\0\5\0\4\0\2\0\0\0\7\0\4\0\3\0\3\0\4\0\4\0\3\0\3\0\1\0\1\0\2\0\1\0\1\0\1\0" +
		"\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\3\0\7\0\3\0\3\0\1\0\1\0\1\0\1\0\5\0\3\0\1\0\3\0" +
		"\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0\4\0\2\0\4\0\3\0\3\0" +
		"\1\0\1\0\2\0\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0\5\0\5\0\6\0\3\0\1\0" +
		"\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\5\0\4\0\4\0\3\0\4\0\3\0\3\0\2\0\4\0\3\0\3\0" +
		"\2\0\3\0\2\0\2\0\1\0\1\0\3\0\2\0\3\0\3\0\4\0\3\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0" +
		"\3\0\1\0\3\0\2\0\1\0\2\0\1\0\2\0\1\0\3\0\3\0\1\0\2\0\1\0\3\0\3\0\3\0\1\0\3\0\1\0" +
		"\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0\1\0\3\0\2\0\1\0\3\0\3\0\2\0\1\0\1\0\4\0\2\0" +
		"\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0\1\0\1\0\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0" +
		"\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0\1\0\5\0\3\0\1\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0" +
		"\0\0\3\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0" +
		"\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(227,
		"\120\0\120\0\121\0\121\0\122\0\122\0\123\0\123\0\124\0\125\0\126\0\127\0\127\0\130" +
		"\0\130\0\131\0\132\0\132\0\133\0\134\0\135\0\136\0\136\0\136\0\137\0\137\0\137\0" +
		"\137\0\140\0\141\0\141\0\142\0\143\0\143\0\143\0\143\0\144\0\145\0\145\0\146\0\147" +
		"\0\150\0\151\0\151\0\151\0\152\0\152\0\152\0\153\0\153\0\153\0\153\0\153\0\153\0" +
		"\153\0\153\0\154\0\154\0\154\0\154\0\154\0\154\0\155\0\156\0\156\0\156\0\157\0\157" +
		"\0\157\0\160\0\160\0\160\0\160\0\161\0\161\0\161\0\161\0\161\0\162\0\162\0\163\0" +
		"\163\0\164\0\164\0\165\0\165\0\166\0\166\0\167\0\170\0\170\0\170\0\170\0\170\0\170" +
		"\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\170\0\171\0\172\0" +
		"\173\0\173\0\174\0\174\0\175\0\175\0\175\0\176\0\176\0\176\0\176\0\176\0\177\0\177" +
		"\0\200\0\201\0\201\0\202\0\203\0\203\0\204\0\204\0\204\0\205\0\205\0\206\0\206\0" +
		"\206\0\207\0\210\0\210\0\211\0\211\0\211\0\211\0\211\0\211\0\211\0\211\0\212\0\213" +
		"\0\213\0\213\0\213\0\214\0\214\0\214\0\215\0\215\0\216\0\217\0\217\0\217\0\220\0" +
		"\220\0\221\0\222\0\222\0\222\0\223\0\224\0\224\0\225\0\225\0\226\0\227\0\227\0\227" +
		"\0\227\0\230\0\230\0\231\0\231\0\232\0\232\0\232\0\232\0\233\0\233\0\233\0\234\0" +
		"\234\0\234\0\234\0\234\0\235\0\235\0\236\0\236\0\237\0\237\0\240\0\240\0\241\0\242" +
		"\0\242\0\242\0\242\0\243\0\244\0\244\0\245\0\246\0\247\0\247\0\250\0\250\0\251\0" +
		"\251\0\252\0\252\0\253\0\253\0\254\0\254\0\255\0\255\0\256\0\256\0");

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
		int import__optlist = 80;
		int input = 81;
		int option_optlist = 82;
		int header = 83;
		int lexer_section = 84;
		int parser_section = 85;
		int parsing_algorithm = 86;
		int import_ = 87;
		int option = 88;
		int identifier = 89;
		int symref = 90;
		int symref_noargs = 91;
		int rawType = 92;
		int pattern = 93;
		int lexer_parts = 94;
		int lexer_part = 95;
		int named_pattern = 96;
		int lexeme = 97;
		int lexeme_attrs = 98;
		int lexeme_attribute = 99;
		int lexer_directive = 100;
		int lexer_state_list_Comma_separated = 101;
		int state_selector = 102;
		int stateref = 103;
		int lexer_state = 104;
		int grammar_parts = 105;
		int grammar_part = 106;
		int nonterm = 107;
		int nonterm_type = 108;
		int _implements = 109;
		int assoc = 110;
		int param_modifier = 111;
		int template_param = 112;
		int directive = 113;
		int inputref_list_Comma_separated = 114;
		int inputref = 115;
		int references = 116;
		int references_cs = 117;
		int rule0_list_Or_separated = 118;
		int rules = 119;
		int rule0 = 120;
		int predicate = 121;
		int rhsPrefix = 122;
		int rhsSuffix = 123;
		int ruleAction = 124;
		int rhsParts = 125;
		int rhsPart = 126;
		int lookahead_predicate_list_And_separated = 127;
		int rhsLookahead = 128;
		int lookahead_predicate = 129;
		int rhsStateMarker = 130;
		int rhsAnnotated = 131;
		int rhsAssignment = 132;
		int rhsOptional = 133;
		int rhsCast = 134;
		int rhsUnordered = 135;
		int rhsClass = 136;
		int rhsPrimary = 137;
		int rhsSet = 138;
		int setPrimary = 139;
		int setExpression = 140;
		int annotation_list = 141;
		int annotations = 142;
		int annotation = 143;
		int nonterm_param_list_Comma_separated = 144;
		int nonterm_params = 145;
		int nonterm_param = 146;
		int param_ref = 147;
		int argument_list_Comma_separated = 148;
		int argument_list_Comma_separated_opt = 149;
		int symref_args = 150;
		int argument = 151;
		int param_type = 152;
		int param_value = 153;
		int predicate_primary = 154;
		int predicate_expression = 155;
		int expression = 156;
		int expression_list_Comma_separated = 157;
		int expression_list_Comma_separated_opt = 158;
		int map_entry_list_Comma_separated = 159;
		int map_entry_list_Comma_separated_opt = 160;
		int map_entry = 161;
		int literal = 162;
		int name = 163;
		int qualified_id = 164;
		int command = 165;
		int syntax_problem = 166;
		int parsing_algorithmopt = 167;
		int rawTypeopt = 168;
		int iconopt = 169;
		int lexeme_attrsopt = 170;
		int commandopt = 171;
		int identifieropt = 172;
		int implementsopt = 173;
		int rhsSuffixopt = 174;
	}

	public interface Rules {
		int lexer_directive_directiveBrackets = 36;  // lexer_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
		int nonterm_type_nontermTypeAST = 56;  // nonterm_type ::= Lreturns symref_noargs
		int nonterm_type_nontermTypeHint = 57;  // nonterm_type ::= Linline Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint2 = 58;  // nonterm_type ::= Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint3 = 59;  // nonterm_type ::= Linterface identifieropt implementsopt
		int nonterm_type_nontermTypeHint4 = 60;  // nonterm_type ::= Lvoid
		int directive_directivePrio = 73;  // directive ::= '%' assoc references ';'
		int directive_directiveInput = 74;  // directive ::= '%' Linput inputref_list_Comma_separated ';'
		int directive_directiveAssert = 75;  // directive ::= '%' Lassert Lempty rhsSet ';'
		int directive_directiveAssert2 = 76;  // directive ::= '%' Lassert Lnonempty rhsSet ';'
		int directive_directiveSet = 77;  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 132;  // rhsOptional ::= rhsCast '?'
		int rhsCast_rhsAsLiteral = 135;  // rhsCast ::= rhsClass Las literal
		int rhsPrimary_rhsSymbol = 139;  // rhsPrimary ::= symref
		int rhsPrimary_rhsNested = 140;  // rhsPrimary ::= '(' rules ')'
		int rhsPrimary_rhsList = 141;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		int rhsPrimary_rhsList2 = 142;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		int rhsPrimary_rhsQuantifier = 143;  // rhsPrimary ::= rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 144;  // rhsPrimary ::= rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 145;  // rhsPrimary ::= '$' '(' rules ')'
		int setPrimary_setSymbol = 148;  // setPrimary ::= ID symref
		int setPrimary_setSymbol2 = 149;  // setPrimary ::= symref
		int setPrimary_setCompound = 150;  // setPrimary ::= '(' setExpression ')'
		int setPrimary_setComplement = 151;  // setPrimary ::= '~' setPrimary
		int setExpression_setBinary = 153;  // setExpression ::= setExpression '|' setExpression
		int setExpression_setBinary2 = 154;  // setExpression ::= setExpression '&' setExpression
		int nonterm_param_inlineParameter = 165;  // nonterm_param ::= ID identifier '=' param_value
		int nonterm_param_inlineParameter2 = 166;  // nonterm_param ::= ID identifier
		int predicate_primary_boolPredicate = 181;  // predicate_primary ::= '!' param_ref
		int predicate_primary_boolPredicate2 = 182;  // predicate_primary ::= param_ref
		int predicate_primary_comparePredicate = 183;  // predicate_primary ::= param_ref '==' literal
		int predicate_primary_comparePredicate2 = 184;  // predicate_primary ::= param_ref '!=' literal
		int predicate_expression_predicateBinary = 186;  // predicate_expression ::= predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 187;  // predicate_expression ::= predicate_expression '||' predicate_expression
		int expression_instance = 190;  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 191;  // expression ::= '[' expression_list_Comma_separated_opt ']'
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
			case 28:  // named_pattern ::= ID '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 29:  // lexeme ::= identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaRawType)tmStack[tmHead - 5].value) /* rawType */,
						((TmaPattern)tmStack[tmHead - 3].value) /* pattern */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead - 1].value) /* attrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // lexeme ::= identifier rawTypeopt ':'
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaRawType)tmStack[tmHead - 1].value) /* rawType */,
						null /* pattern */,
						null /* priority */,
						null /* attrs */,
						null /* command */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // lexeme_attrs ::= '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // lexeme_attribute ::= Lsoft
				tmLeft.value = TmaLexemeAttribute.LSOFT;
				break;
			case 33:  // lexeme_attribute ::= Lclass
				tmLeft.value = TmaLexemeAttribute.LCLASS;
				break;
			case 34:  // lexeme_attribute ::= Lspace
				tmLeft.value = TmaLexemeAttribute.LSPACE;
				break;
			case 35:  // lexeme_attribute ::= Llayout
				tmLeft.value = TmaLexemeAttribute.LLAYOUT;
				break;
			case 36:  // lexer_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
				tmLeft.value = new TmaDirectiveBrackets(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // lexer_state_list_Comma_separated ::= lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 38:  // lexer_state_list_Comma_separated ::= lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 39:  // state_selector ::= '[' lexer_state_list_Comma_separated ']'
				tmLeft.value = new TmaStateSelector(
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 40:  // stateref ::= ID
				tmLeft.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 41:  // lexer_state ::= identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 42:  // grammar_parts ::= grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 43:  // grammar_parts ::= grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 44:  // grammar_parts ::= grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 48:  // nonterm ::= annotations identifier nonterm_params nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // nonterm ::= annotations identifier nonterm_params '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 51:  // nonterm ::= annotations identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 52:  // nonterm ::= identifier nonterm_params nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 53:  // nonterm ::= identifier nonterm_params '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 55:  // nonterm ::= identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 56:  // nonterm_type ::= Lreturns symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 57:  // nonterm_type ::= Linline Lclass identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // nonterm_type ::= Lclass identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // nonterm_type ::= Linterface identifieropt implementsopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LINTERFACE /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* _implements */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // nonterm_type ::= Lvoid
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LVOID /* kind */,
						null /* name */,
						null /* _implements */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 62:  // implements ::= ':' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 63:  // assoc ::= Lleft
				tmLeft.value = TmaAssoc.LLEFT;
				break;
			case 64:  // assoc ::= Lright
				tmLeft.value = TmaAssoc.LRIGHT;
				break;
			case 65:  // assoc ::= Lnonassoc
				tmLeft.value = TmaAssoc.LNONASSOC;
				break;
			case 66:  // param_modifier ::= Lexplicit
				tmLeft.value = TmaParamModifier.LEXPLICIT;
				break;
			case 67:  // param_modifier ::= Lglobal
				tmLeft.value = TmaParamModifier.LGLOBAL;
				break;
			case 68:  // param_modifier ::= Llookahead
				tmLeft.value = TmaParamModifier.LLOOKAHEAD;
				break;
			case 69:  // template_param ::= '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 70:  // template_param ::= '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // template_param ::= '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 72:  // template_param ::= '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 73:  // directive ::= '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 74:  // directive ::= '%' Linput inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 75:  // directive ::= '%' Lassert Lempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // directive ::= '%' Lassert Lnonempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LNONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((String)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 78:  // inputref_list_Comma_separated ::= inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 79:  // inputref_list_Comma_separated ::= inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 80:  // inputref ::= symref_noargs Lnoeoi
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 81:  // inputref ::= symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // references ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 83:  // references ::= references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 84:  // references_cs ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 85:  // references_cs ::= references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 86:  // rule0_list_Or_separated ::= rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 87:  // rule0_list_Or_separated ::= rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 89:  // rule0 ::= predicate rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 4].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // rule0 ::= predicate rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // rule0 ::= predicate rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // rule0 ::= predicate rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // rule0 ::= predicate rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // rule0 ::= predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 95:  // rule0 ::= predicate ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 96:  // rule0 ::= predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 97:  // rule0 ::= rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 98:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // rule0 ::= rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // rule0 ::= rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // rule0 ::= rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // rule0 ::= rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // rule0 ::= ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // rule0 ::= rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // rule0 ::= syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						null /* suffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // predicate ::= '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 107:  // rhsPrefix ::= annotations ':'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rhsSuffix ::= '%' Lprec symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // rhsSuffix ::= '%' Lshift symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // ruleAction ::= '{~' identifier scon '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((String)tmStack[tmHead - 1].value) /* parameter */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // ruleAction ::= '{~' identifier '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* action */,
						null /* parameter */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // rhsParts ::= rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 113:  // rhsParts ::= rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 114:  // rhsParts ::= rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 120:  // lookahead_predicate_list_And_separated ::= lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 121:  // lookahead_predicate_list_And_separated ::= lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 122:  // rhsLookahead ::= '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // lookahead_predicate ::= '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 124:  // lookahead_predicate ::= symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 125:  // rhsStateMarker ::= '.' ID
				tmLeft.value = new TmaRhsStateMarker(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // rhsAnnotated ::= annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // rhsAssignment ::= identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // rhsAssignment ::= identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // rhsOptional ::= rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // rhsCast ::= rhsClass Las symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // rhsCast ::= rhsClass Las literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // rhsUnordered ::= rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // rhsClass ::= identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // rhsPrimary ::= symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 140:  // rhsPrimary ::= '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // rhsPrimary ::= rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // rhsPrimary ::= rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // rhsPrimary ::= '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // rhsSet ::= Lset '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // setPrimary ::= ID symref
				tmLeft.value = new TmaSetSymbol(
						((String)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // setPrimary ::= symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // setPrimary ::= '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // setPrimary ::= '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // setExpression ::= setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // setExpression ::= setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 156:  // annotation_list ::= annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 157:  // annotations ::= annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // annotation ::= '@' ID '=' expression
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // annotation ::= '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // annotation ::= '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // nonterm_param_list_Comma_separated ::= nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 162:  // nonterm_param_list_Comma_separated ::= nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 163:  // nonterm_params ::= '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // nonterm_param ::= ID identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 166:  // nonterm_param ::= ID identifier
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 167:  // param_ref ::= identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 168:  // argument_list_Comma_separated ::= argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 169:  // argument_list_Comma_separated ::= argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 172:  // symref_args ::= '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 173:  // argument ::= param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 174:  // argument ::= '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // argument ::= '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 176:  // argument ::= param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // param_type ::= Lflag
				tmLeft.value = TmaParamType.LFLAG;
				break;
			case 178:  // param_type ::= Lparam
				tmLeft.value = TmaParamType.LPARAM;
				break;
			case 181:  // predicate_primary ::= '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 182:  // predicate_primary ::= param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 183:  // predicate_primary ::= param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 184:  // predicate_primary ::= param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // predicate_expression ::= predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // predicate_expression ::= predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 190:  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // expression ::= '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // expression_list_Comma_separated ::= expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 194:  // expression_list_Comma_separated ::= expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 197:  // map_entry_list_Comma_separated ::= map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 198:  // map_entry_list_Comma_separated ::= map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 201:  // map_entry ::= ID ':' expression
				tmLeft.value = new TmaMapEntry(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // literal ::= scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 203:  // literal ::= icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 204:  // literal ::= Ltrue
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 205:  // literal ::= Lfalse
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 206:  // name ::= qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 208:  // qualified_id ::= qualified_id '.' ID
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 209:  // command ::= code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 210:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 401);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 402);
	}
}
