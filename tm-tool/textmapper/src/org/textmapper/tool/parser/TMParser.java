/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
	private static final int[] tmAction = TMLexer.unpack_int(419,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\324\0\325\0\uffb9\uffff\334\0\uff6b" +
		"\uffff\326\0\327\0\uffff\uffff\307\0\306\0\312\0\331\0\ufeff\uffff\ufef7\uffff\ufeeb" +
		"\uffff\314\0\ufea7\uffff\uffff\uffff\ufea1\uffff\20\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\335\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\311" +
		"\0\uffff\uffff\uffff\uffff\17\0\261\0\ufe5d\uffff\ufe55\uffff\uffff\uffff\263\0\ufe4f" +
		"\uffff\uffff\uffff\uffff\uffff\7\0\332\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufe0f\uffff\4\0\16\0\313\0\270\0\271\0\uffff\uffff\uffff\uffff\266\0\uffff" +
		"\uffff\ufe09\uffff\uffff\uffff\320\0\ufe03\uffff\uffff\uffff\14\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\2\0\22\0\276\0\267\0\275\0\262\0\uffff\uffff\uffff" +
		"\uffff\310\0\uffff\uffff\12\0\13\0\uffff\uffff\uffff\uffff\ufdfd\uffff\ufdf5\uffff" +
		"\ufdef\uffff\45\0\51\0\52\0\53\0\50\0\15\0\uffff\uffff\323\0\317\0\6\0\uffff\uffff" +
		"\ufda7\uffff\uffff\uffff\67\0\uffff\uffff\uffff\uffff\337\0\uffff\uffff\46\0\47\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufd9f\uffff\74\0\77\0\100\0\101\0\ufd59\uffff" +
		"\uffff\uffff\246\0\uffff\uffff\uffff\uffff\uffff\uffff\70\0\44\0\54\0\uffff\uffff" +
		"\35\0\36\0\31\0\32\0\uffff\uffff\27\0\30\0\34\0\37\0\41\0\40\0\33\0\uffff\uffff\26" +
		"\0\ufd11\uffff\uffff\uffff\121\0\122\0\123\0\uffff\uffff\uffff\uffff\125\0\124\0" +
		"\126\0\274\0\273\0\uffff\uffff\uffff\uffff\uffff\uffff\ufcc7\uffff\252\0\ufc7d\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufc29\uffff\ufbe7\uffff\116\0\117\0\uffff\uffff" +
		"\uffff\uffff\75\0\76\0\245\0\uffff\uffff\uffff\uffff\71\0\72\0\66\0\23\0\43\0\uffff" +
		"\uffff\24\0\25\0\ufba5\uffff\ufb55\uffff\uffff\uffff\141\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\144\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufb4d\uffff\uffff\uffff\uffff\uffff\ufaf9\uffff\uffff\uffff\333\0\uffff\uffff" +
		"\225\0\ufa91\uffff\uffff\uffff\151\0\ufa89\uffff\ufa37\uffff\355\0\uf9e5\uffff\uf9db" +
		"\uffff\uf987\uffff\205\0\210\0\212\0\uf92f\uffff\206\0\uf8d5\uffff\uf879\uffff\234" +
		"\0\uffff\uffff\207\0\173\0\172\0\uf819\uffff\uffff\uffff\254\0\256\0\uf7d7\uffff" +
		"\112\0\351\0\uf795\uffff\uf78f\uffff\uf789\uffff\uf735\uffff\uffff\uffff\uf6e1\uffff" +
		"\uffff\uffff\uffff\uffff\65\0\42\0\uffff\uffff\341\0\uf68d\uffff\142\0\134\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\133\0\145\0\uffff\uffff\uffff\uffff\132" +
		"\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf63f\uffff\303\0\uffff\uffff" +
		"\uffff\uffff\uf633\uffff\uffff\uffff\uf5df\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uf58b\uffff\111\0\uf535\uffff\uf4e3\uffff\uf4d9\uffff\162\0\uf485\uffff" +
		"\uf47b\uffff\uffff\uffff\166\0\171\0\uf427\uffff\uf41d\uffff\204\0\170\0\uffff\uffff" +
		"\216\0\uffff\uffff\231\0\232\0\175\0\211\0\uf3c5\uffff\uffff\uffff\255\0\uf3bd\uffff" +
		"\uffff\uffff\353\0\114\0\115\0\uffff\uffff\uffff\uffff\uf3b7\uffff\uffff\uffff\uf363" +
		"\uffff\uf30f\uffff\uffff\uffff\57\0\343\0\uf2bb\uffff\140\0\uffff\uffff\135\0\136" +
		"\0\uffff\uffff\130\0\uffff\uffff\250\0\176\0\177\0\277\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\174\0\uffff\uffff\226\0\uffff\uffff\uffff\uffff\201\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uf26f\uffff\237\0\242\0\uffff\uffff\uffff\uffff\213\0\uf229\uffff" +
		"\214\0\150\0\uf1c9\uffff\uf1bf\uffff\156\0\161\0\uf16b\uffff\160\0\165\0\uf161\uffff" +
		"\164\0\167\0\uf157\uffff\220\0\221\0\uffff\uffff\253\0\113\0\146\0\uf0ff\uffff\110" +
		"\0\107\0\uffff\uffff\105\0\uffff\uffff\uffff\uffff\uf0f9\uffff\uffff\uffff\345\0" +
		"\uf0a5\uffff\137\0\uffff\uffff\131\0\301\0\302\0\uf05b\uffff\uf053\uffff\uffff\uffff" +
		"\200\0\233\0\uffff\uffff\241\0\236\0\uffff\uffff\235\0\uffff\uffff\155\0\uf04b\uffff" +
		"\154\0\157\0\163\0\257\0\uffff\uffff\106\0\104\0\103\0\uffff\uffff\61\0\62\0\63\0" +
		"\64\0\uffff\uffff\347\0\55\0\127\0\uffff\uffff\240\0\uf041\uffff\uf039\uffff\153" +
		"\0\147\0\102\0\60\0\230\0\227\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(4044,
		"\11\1\46\1\47\1\55\1\57\1\60\1\61\1\62\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1" +
		"\73\1\74\1\75\1\76\1\77\1\100\1\101\1\102\1\103\1\104\1\105\1\106\1\107\1\110\1\111" +
		"\1\112\1\113\1\uffff\ufffe\2\uffff\3\uffff\24\uffff\46\uffff\47\uffff\113\uffff\112" +
		"\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102" +
		"\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff" +
		"\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\50\uffff\51\uffff\52\uffff\25\316\uffff\ufffe\32\uffff\0\21\7" +
		"\21\11\21\12\21\20\21\22\21\25\21\26\21\27\21\30\21\31\21\34\21\35\21\37\21\42\21" +
		"\44\21\45\21\46\21\47\21\53\21\54\21\56\21\57\21\60\21\61\21\62\21\63\21\64\21\65" +
		"\21\66\21\67\21\70\21\71\21\72\21\73\21\74\21\75\21\76\21\77\21\100\21\101\21\102" +
		"\21\103\21\104\21\105\21\106\21\107\21\110\21\111\21\112\21\113\21\115\21\uffff\ufffe" +
		"\26\uffff\111\uffff\20\336\uffff\ufffe\21\uffff\20\330\26\330\27\330\111\330\uffff" +
		"\ufffe\55\uffff\11\5\46\5\47\5\57\5\60\5\61\5\62\5\63\5\64\5\65\5\66\5\67\5\70\5" +
		"\71\5\72\5\73\5\74\5\75\5\76\5\77\5\100\5\101\5\102\5\103\5\104\5\105\5\106\5\107" +
		"\5\110\5\111\5\112\5\113\5\uffff\ufffe\22\uffff\25\315\uffff\ufffe\35\uffff\41\uffff" +
		"\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104" +
		"\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff" +
		"\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\33\265\uffff\ufffe\23\uffff\22\272\33\272\uffff" +
		"\ufffe\22\uffff\33\264\uffff\ufffe\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff" +
		"\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff" +
		"\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\27\322\uffff" +
		"\ufffe\11\uffff\0\3\uffff\ufffe\22\uffff\27\321\uffff\ufffe\111\uffff\20\336\uffff" +
		"\ufffe\14\uffff\23\17\26\17\uffff\ufffe\26\uffff\23\340\uffff\ufffe\7\uffff\24\uffff" +
		"\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105" +
		"\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff" +
		"\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\0\10\11\10\uffff\ufffe\17\uffff\22" +
		"\73\25\73\uffff\ufffe\7\uffff\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111" +
		"\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101" +
		"\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70" +
		"\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57" +
		"\uffff\0\11\uffff\ufffe\45\uffff\23\247\26\247\44\247\47\247\56\247\57\247\60\247" +
		"\61\247\62\247\63\247\64\247\65\247\66\247\67\247\70\247\71\247\72\247\73\247\74" +
		"\247\75\247\76\247\77\247\100\247\101\247\102\247\103\247\104\247\105\247\106\247" +
		"\107\247\110\247\111\247\112\247\113\247\uffff\ufffe\1\uffff\0\56\7\56\11\56\24\56" +
		"\46\56\47\56\57\56\60\56\61\56\62\56\63\56\64\56\65\56\66\56\67\56\70\56\71\56\72" +
		"\56\73\56\74\56\75\56\76\56\77\56\100\56\101\56\102\56\103\56\104\56\105\56\106\56" +
		"\107\56\110\56\111\56\112\56\113\56\uffff\ufffe\116\uffff\23\251\26\251\44\251\45" +
		"\251\47\251\56\251\57\251\60\251\61\251\62\251\63\251\64\251\65\251\66\251\67\251" +
		"\70\251\71\251\72\251\73\251\74\251\75\251\76\251\77\251\100\251\101\251\102\251" +
		"\103\251\104\251\105\251\106\251\107\251\110\251\111\251\112\251\113\251\uffff\ufffe" +
		"\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\113\uffff" +
		"\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff" +
		"\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff" +
		"\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\uffff\ufffe\47\uffff\113\uffff" +
		"\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff" +
		"\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff" +
		"\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\10\352\23\352\uffff\ufffe\47\uffff\113\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\10\352\23\352\uffff\ufffe\17\uffff\0\342\3\342\7\342\11\342\24\342\26\342\46\342" +
		"\47\342\57\342\60\342\61\342\62\342\63\342\64\342\65\342\66\342\67\342\70\342\71" +
		"\342\72\342\73\342\74\342\75\342\76\342\77\342\100\342\101\342\102\342\103\342\104" +
		"\342\105\342\106\342\107\342\110\342\111\342\112\342\113\342\115\342\uffff\ufffe" +
		"\101\uffff\20\143\22\143\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff" +
		"\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106" +
		"\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff" +
		"\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356" +
		"\27\356\uffff\ufffe\32\uffff\14\17\23\17\36\17\7\21\12\21\20\21\26\21\27\21\30\21" +
		"\34\21\35\21\37\21\42\21\44\21\45\21\46\21\47\21\53\21\54\21\56\21\57\21\60\21\61" +
		"\21\62\21\63\21\64\21\65\21\66\21\67\21\70\21\71\21\72\21\73\21\74\21\75\21\76\21" +
		"\77\21\100\21\101\21\102\21\103\21\104\21\105\21\106\21\107\21\110\21\111\21\112" +
		"\21\113\21\115\21\uffff\ufffe\12\uffff\20\152\27\152\uffff\ufffe\7\uffff\26\uffff" +
		"\30\uffff\44\uffff\45\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107" +
		"\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff" +
		"\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff" +
		"\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff" +
		"\12\356\20\356\27\356\uffff\ufffe\7\uffff\26\uffff\30\uffff\44\uffff\45\uffff\47" +
		"\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104" +
		"\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff" +
		"\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\27\356\uffff" +
		"\ufffe\7\uffff\12\356\20\356\27\356\uffff\ufffe\7\uffff\26\uffff\30\uffff\44\uffff" +
		"\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106" +
		"\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff" +
		"\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356" +
		"\20\356\27\356\uffff\ufffe\42\uffff\7\202\12\202\20\202\26\202\27\202\30\202\44\202" +
		"\45\202\46\202\47\202\53\202\56\202\57\202\60\202\61\202\62\202\63\202\64\202\65" +
		"\202\66\202\67\202\70\202\71\202\72\202\73\202\74\202\75\202\76\202\77\202\100\202" +
		"\101\202\102\202\103\202\104\202\105\202\106\202\107\202\110\202\111\202\112\202" +
		"\113\202\115\202\uffff\ufffe\37\uffff\7\215\12\215\20\215\26\215\27\215\30\215\42" +
		"\215\44\215\45\215\46\215\47\215\53\215\56\215\57\215\60\215\61\215\62\215\63\215" +
		"\64\215\65\215\66\215\67\215\70\215\71\215\72\215\73\215\74\215\75\215\76\215\77" +
		"\215\100\215\101\215\102\215\103\215\104\215\105\215\106\215\107\215\110\215\111" +
		"\215\112\215\113\215\115\215\uffff\ufffe\54\uffff\7\217\12\217\20\217\26\217\27\217" +
		"\30\217\37\217\42\217\44\217\45\217\46\217\47\217\53\217\56\217\57\217\60\217\61" +
		"\217\62\217\63\217\64\217\65\217\66\217\67\217\70\217\71\217\72\217\73\217\74\217" +
		"\75\217\76\217\77\217\100\217\101\217\102\217\103\217\104\217\105\217\106\217\107" +
		"\217\110\217\111\217\112\217\113\217\115\217\uffff\ufffe\34\uffff\35\uffff\7\223" +
		"\12\223\20\223\26\223\27\223\30\223\37\223\42\223\44\223\45\223\46\223\47\223\53" +
		"\223\54\223\56\223\57\223\60\223\61\223\62\223\63\223\64\223\65\223\66\223\67\223" +
		"\70\223\71\223\72\223\73\223\74\223\75\223\76\223\77\223\100\223\101\223\102\223" +
		"\103\223\104\223\105\223\106\223\107\223\110\223\111\223\112\223\113\223\115\223" +
		"\uffff\ufffe\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff" +
		"\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75" +
		"\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\22\17\33\17\uffff\ufffe\47\uffff" +
		"\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff" +
		"\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\10\352\23\352\uffff\ufffe\23\uffff\10\354\uffff\ufffe" +
		"\23\uffff\10\354\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff" +
		"\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105" +
		"\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff" +
		"\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\uffff" +
		"\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\113" +
		"\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103" +
		"\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\uffff\ufffe\7\uffff" +
		"\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff" +
		"\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff" +
		"\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff" +
		"\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\115\uffff\12\356\20\356\uffff\ufffe\3\uffff\0\344\7\344\11\344" +
		"\24\344\26\344\46\344\47\344\57\344\60\344\61\344\62\344\63\344\64\344\65\344\66" +
		"\344\67\344\70\344\71\344\72\344\73\344\74\344\75\344\76\344\77\344\100\344\101\344" +
		"\102\344\103\344\104\344\105\344\106\344\107\344\110\344\111\344\112\344\113\344" +
		"\115\344\uffff\ufffe\15\uffff\16\uffff\13\300\25\300\43\300\uffff\ufffe\7\uffff\26" +
		"\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\53\uffff\56\uffff\115\uffff\12\356\27\356\uffff\ufffe\7\uffff\24\uffff\26\uffff" +
		"\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff" +
		"\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff" +
		"\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\115\uffff\12\356\27\356\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff" +
		"\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106" +
		"\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff" +
		"\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356" +
		"\20\356\27\356\uffff\ufffe\7\uffff\26\uffff\30\uffff\44\uffff\45\uffff\47\uffff\113" +
		"\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103" +
		"\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\27\356\uffff\ufffe\7" +
		"\uffff\12\356\20\356\27\356\uffff\ufffe\7\uffff\26\uffff\30\uffff\44\uffff\45\uffff" +
		"\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105" +
		"\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff" +
		"\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\27" +
		"\356\uffff\ufffe\7\uffff\12\356\20\356\27\356\uffff\ufffe\7\uffff\26\uffff\30\uffff" +
		"\44\uffff\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107" +
		"\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff" +
		"\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff" +
		"\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff" +
		"\12\356\20\356\27\356\uffff\ufffe\7\uffff\12\356\20\356\27\356\uffff\ufffe\42\uffff" +
		"\7\203\12\203\20\203\26\203\27\203\30\203\44\203\45\203\46\203\47\203\53\203\56\203" +
		"\57\203\60\203\61\203\62\203\63\203\64\203\65\203\66\203\67\203\70\203\71\203\72" +
		"\203\73\203\74\203\75\203\76\203\77\203\100\203\101\203\102\203\103\203\104\203\105" +
		"\203\106\203\107\203\110\203\111\203\112\203\113\203\115\203\uffff\ufffe\14\uffff" +
		"\22\260\33\260\uffff\ufffe\23\uffff\10\354\uffff\ufffe\7\uffff\24\uffff\26\uffff" +
		"\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff" +
		"\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff" +
		"\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\115\uffff\12\356\20\356\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff" +
		"\45\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106" +
		"\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff" +
		"\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356" +
		"\20\356\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff" +
		"\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104" +
		"\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff" +
		"\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\uffff\ufffe" +
		"\26\uffff\0\346\7\346\11\346\24\346\46\346\47\346\57\346\60\346\61\346\62\346\63" +
		"\346\64\346\65\346\66\346\67\346\70\346\71\346\72\346\73\346\74\346\75\346\76\346" +
		"\77\346\100\346\101\346\102\346\103\346\104\346\105\346\106\346\107\346\110\346\111" +
		"\346\112\346\113\346\115\346\uffff\ufffe\32\uffff\47\uffff\113\uffff\112\uffff\111" +
		"\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101" +
		"\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70" +
		"\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57" +
		"\uffff\12\21\27\21\42\21\uffff\ufffe\34\uffff\35\uffff\7\224\12\224\20\224\26\224" +
		"\27\224\30\224\37\224\42\224\44\224\45\224\46\224\47\224\53\224\54\224\56\224\57" +
		"\224\60\224\61\224\62\224\63\224\64\224\65\224\66\224\67\224\70\224\71\224\72\224" +
		"\73\224\74\224\75\224\76\224\77\224\100\224\101\224\102\224\103\224\104\224\105\224" +
		"\106\224\107\224\110\224\111\224\112\224\113\224\115\224\uffff\ufffe\7\uffff\12\356" +
		"\20\356\27\356\uffff\ufffe\7\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47" +
		"\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104" +
		"\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff" +
		"\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20\356\27\356\uffff" +
		"\ufffe\7\uffff\12\356\20\356\27\356\uffff\ufffe\7\uffff\12\356\20\356\27\356\uffff" +
		"\ufffe\42\222\7\222\12\222\20\222\26\222\27\222\30\222\44\222\45\222\46\222\47\222" +
		"\53\222\56\222\57\222\60\222\61\222\62\222\63\222\64\222\65\222\66\222\67\222\70" +
		"\222\71\222\72\222\73\222\74\222\75\222\76\222\77\222\100\222\101\222\102\222\103" +
		"\222\104\222\105\222\106\222\107\222\110\222\111\222\112\222\113\222\115\222\uffff" +
		"\ufffe\22\uffff\10\120\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45" +
		"\uffff\46\uffff\47\uffff\113\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff" +
		"\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75" +
		"\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\115\uffff\12\356\20" +
		"\356\uffff\ufffe\115\uffff\0\350\7\350\11\350\24\350\46\350\47\350\57\350\60\350" +
		"\61\350\62\350\63\350\64\350\65\350\66\350\67\350\70\350\71\350\72\350\73\350\74" +
		"\350\75\350\76\350\77\350\100\350\101\350\102\350\103\350\104\350\105\350\106\350" +
		"\107\350\110\350\111\350\112\350\113\350\uffff\ufffe\13\305\43\uffff\25\305\uffff" +
		"\ufffe\13\304\43\304\25\304\uffff\ufffe\7\uffff\12\356\20\356\27\356\uffff\ufffe" +
		"\12\243\42\uffff\27\243\uffff\ufffe\12\244\42\244\27\244\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(175,
		"\0\2\4\25\44\44\44\44\103\113\115\122\125\134\135\136\140\166\173\206\217\246\255" +
		"\332\347\372\375\u0106\u010c\u0113\u0118\u0119\u011e\u0121\u0128\u0133\u0136\u014f" +
		"\u016a\u0184\u01e3\u01f0\u01fd\u0203\u0204\u0205\u0206\u0222\u0282\u02e5\u0345\u03a5" +
		"\u0408\u0468\u04c8\u0528\u0588\u05e8\u0648\u06a8\u0708\u0768\u07c8\u0828\u0889\u08ea" +
		"\u094a\u09aa\u0a0f\u0a72\u0ad5\u0b35\u0b95\u0bf5\u0c56\u0cb6\u0d16\u0d16\u0d2b\u0d2c" +
		"\u0d2d\u0d2e\u0d2f\u0d30\u0d31\u0d32\u0d34\u0d35\u0d36\u0d66\u0d8c\u0d9d\u0da2\u0da4" +
		"\u0da8\u0daa\u0dab\u0dad\u0daf\u0db1\u0db2\u0db3\u0db4\u0db6\u0db7\u0db9\u0dbb\u0dbd" +
		"\u0dbe\u0dc0\u0dc2\u0dc6\u0dc9\u0dca\u0dcb\u0dcd\u0dcf\u0dd0\u0dd2\u0dd4\u0dd5\u0ddf" +
		"\u0de9\u0df4\u0dff\u0e0b\u0e26\u0e39\u0e47\u0e5b\u0e6f\u0e85\u0e9d\u0eb5\u0ec9\u0ee1" +
		"\u0efa\u0f16\u0f1b\u0f1f\u0f35\u0f4b\u0f62\u0f63\u0f65\u0f67\u0f71\u0f72\u0f73\u0f76" +
		"\u0f78\u0f7a\u0f7e\u0f81\u0f84\u0f8a\u0f8b\u0f8c\u0f8d\u0f8e\u0f90\u0f9d\u0fa0\u0fa3" +
		"\u0fb8\u0fd2\u0fd4\u0fd5\u0fd6\u0fd7\u0fd8\u0fd9\u0fdc\u0fdf\u0ffa");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(4090,
		"\u019f\u01a0\147\215\1\6\36\41\61\72\106\116\150\300\375\u0108\u011f\u013a\u0141" +
		"\u0142\u0163\1\6\41\55\72\106\116\300\363\375\u011f\u013a\u0141\u0142\u0163\105\130" +
		"\137\160\236\303\316\317\321\322\351\352\354\u0107\u0109\u010e\u0110\u0111\u0112" +
		"\u0114\u0115\u0119\u012e\u0130\u0131\u0156\u0157\u015a\u015d\u016e\u0183\157\246" +
		"\247\253\353\355\356\u0132\37\64\313\u0150\u017c\u0197\u0198\u0105\u0177\u0178\63" +
		"\126\271\277\311\374\u0124\u0103\u0103\144\265\34\60\104\121\254\267\275\277\314" +
		"\370\371\374\u012c\u012d\u012f\u0137\u013c\u016a\u016c\u016d\u0173\u018c\21\150\203" +
		"\213\262\24\50\76\145\150\203\213\262\267\341\u0167\47\75\152\311\334\347\350\u0127" +
		"\u0151\1\6\41\105\106\116\130\150\203\213\236\262\300\303\351\352\354\u0109\u010e" +
		"\u012e\u0130\u0131\u016e\25\145\150\203\213\262\u0105\20\30\32\127\150\157\203\213" +
		"\236\247\253\262\303\305\307\316\317\322\334\351\352\354\356\u0107\u0109\u010a\u010b" +
		"\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0135\u014b\u014c" +
		"\u0157\u016e\u017f\u0181\54\77\102\176\203\213\262\u0106\u014a\u0150\u0179\u017c" +
		"\u0191\236\303\316\317\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u012e" +
		"\u0130\u0131\u0157\u016e\377\u0108\u0148\10\150\157\203\213\253\262\306\u014d\51" +
		"\150\203\213\262\341\150\203\213\262\332\u0153\u0195\26\73\332\u0153\u0195\311\150" +
		"\203\213\262\327\302\u0143\u0145\26\73\u010a\u014b\u014c\u017f\u0181\150\203\213" +
		"\262\323\u011a\u0150\u0160\u017c\u0197\u0198\u0105\u0177\u0178\236\303\316\317\322" +
		"\334\351\352\354\u0107\u0109\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d" +
		"\u012e\u0130\u0131\u0157\u016e\137\150\160\165\203\213\236\262\303\316\317\322\351" +
		"\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131\u0157\u016e" +
		"\1\6\37\41\106\116\130\156\160\236\300\303\322\351\352\354\u0107\u0109\u010e\u0112" +
		"\u0115\u012e\u0130\u0131\u0157\u016e\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105" +
		"\106\116\117\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\231\233" +
		"\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352" +
		"\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110" +
		"\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147" +
		"\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\6\41\72\106\116\300" +
		"\375\u011f\u013a\u0141\u0142\u0163\1\6\41\72\106\116\300\375\u011f\u013a\u0141\u0142" +
		"\u0163\1\6\41\106\116\300\u0107\331\22\236\272\273\303\316\317\322\334\351\352\354" +
		"\367\u0107\u0109\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130" +
		"\u0131\u0157\u016e\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\124" +
		"\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241" +
		"\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366" +
		"\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115" +
		"\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c" +
		"\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44" +
		"\53\72\73\105\106\116\117\125\130\137\143\150\156\157\160\166\170\171\172\203\213" +
		"\216\222\231\233\236\237\241\242\243\247\253\262\275\276\300\302\303\304\316\317" +
		"\322\334\340\344\351\352\354\356\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a" +
		"\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130" +
		"\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181" +
		"\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143" +
		"\150\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\262\275" +
		"\276\300\301\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101" +
		"\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f" +
		"\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163" +
		"\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106" +
		"\116\117\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\231\233\236" +
		"\237\241\242\243\262\275\276\300\301\302\303\304\316\317\322\334\340\344\351\352" +
		"\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110" +
		"\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147" +
		"\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36" +
		"\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\157\160\166\170\171" +
		"\172\203\213\216\222\231\233\236\237\241\242\243\247\253\262\275\276\300\302\303" +
		"\304\316\317\322\334\340\344\351\352\354\356\361\366\375\u0100\u0101\u0102\u0107" +
		"\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128" +
		"\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179" +
		"\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125" +
		"\130\137\143\150\155\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241" +
		"\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366" +
		"\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115" +
		"\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c" +
		"\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44" +
		"\53\72\73\105\106\116\117\125\130\137\143\150\155\156\160\166\170\171\172\203\213" +
		"\216\222\231\233\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322\334" +
		"\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1" +
		"\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155" +
		"\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\262\275\276" +
		"\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102" +
		"\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125" +
		"\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e" +
		"\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\155\156\160\166\170\171\172\203\213\216\222\231\233\236\237" +
		"\241\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361" +
		"\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b" +
		"\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41" +
		"\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155\156\160\166\170\171\172\203" +
		"\213\216\222\231\233\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322" +
		"\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1" +
		"\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155" +
		"\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\262\275\276" +
		"\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102" +
		"\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125" +
		"\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e" +
		"\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\223\231\233\236\237" +
		"\241\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361" +
		"\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b" +
		"\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41" +
		"\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\160\166\170\171\172\203\213" +
		"\216\222\223\231\233\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322" +
		"\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1" +
		"\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155" +
		"\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\262\275\276" +
		"\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102" +
		"\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125" +
		"\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e" +
		"\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\155\156\160\166\170\171\172\203\213\216\222\231\233\236\237" +
		"\241\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361" +
		"\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b" +
		"\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41" +
		"\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155\156\160\166\170\171\172\203" +
		"\213\216\222\231\233\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322" +
		"\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1" +
		"\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155" +
		"\156\160\166\170\171\172\203\213\216\222\231\232\233\236\237\241\242\243\262\275" +
		"\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101" +
		"\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f" +
		"\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163" +
		"\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106" +
		"\116\117\125\130\137\143\150\155\156\160\166\170\171\172\203\213\216\222\231\232" +
		"\233\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351" +
		"\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145" +
		"\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31" +
		"\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\160\166\170\171" +
		"\172\203\213\216\222\231\233\236\237\241\242\243\262\266\275\276\300\302\303\304" +
		"\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a" +
		"\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130" +
		"\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181" +
		"\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143" +
		"\150\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\262\275" +
		"\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101" +
		"\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f" +
		"\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163" +
		"\u016e\u016f\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105" +
		"\106\116\117\125\130\137\143\150\156\157\160\166\170\171\172\203\213\216\222\231" +
		"\233\236\237\240\241\242\243\247\253\262\275\276\300\302\303\304\316\317\322\334" +
		"\340\344\351\352\354\356\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u016f\u0179\u017f\u0181\u0188" +
		"\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156" +
		"\157\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\247\253\262" +
		"\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\356\361\366\375\u0100" +
		"\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d" +
		"\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157" +
		"\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105" +
		"\106\116\117\125\130\137\143\150\156\157\160\166\170\171\172\203\213\216\222\231" +
		"\233\236\237\241\242\243\247\253\262\275\276\300\302\303\304\316\317\322\334\340" +
		"\344\351\352\354\356\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1" +
		"\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156" +
		"\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\262\275\276\300" +
		"\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107" +
		"\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128" +
		"\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u016f" +
		"\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241" +
		"\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366" +
		"\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115" +
		"\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c" +
		"\u014d\u0157\u0163\u016e\u016f\u0179\u017f\u0181\u0188\0\1\2\6\13\26\31\35\36\37" +
		"\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\160\166\170\171\172\203" +
		"\213\216\222\231\233\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322" +
		"\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1" +
		"\2\6\13\20\26\31\35\36\37\41\43\44\53\72\73\101\105\106\116\117\125\130\137\143\150" +
		"\156\160\166\170\171\172\203\213\216\222\231\233\236\237\241\242\243\262\275\276" +
		"\300\302\303\304\316\317\322\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102" +
		"\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125" +
		"\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e" +
		"\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41\43\44\53\62\72\73\105\106\116" +
		"\117\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\231\233\236\237" +
		"\241\242\243\262\275\276\300\302\303\304\316\317\322\334\340\344\351\352\354\361" +
		"\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u0143\u0145\u0147\u014b" +
		"\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\1\2\6\13\26\31\35\36\37\41" +
		"\43\44\53\72\73\105\106\107\116\117\125\130\137\143\150\156\160\166\170\171\172\203" +
		"\213\216\222\231\233\236\237\241\242\243\262\275\276\300\302\303\304\316\317\322" +
		"\334\340\344\351\352\354\361\366\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u0143\u0145\u0147\u014b\u014c\u014d\u0157\u0163\u016e\u0179\u017f\u0181\u0188\236" +
		"\303\316\317\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130" +
		"\u0131\u0157\u016e\u0171\234\3\0\22\0\37\64\20\101\22\37\26\43\44\73\105\125\130" +
		"\137\160\166\172\233\236\237\242\243\276\302\303\304\316\317\322\334\340\344\351" +
		"\352\354\u0102\u0107\u0109\u010b\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u0125" +
		"\u012e\u0130\u0131\u0143\u0145\u0157\u016e\1\6\41\106\116\236\300\303\316\317\322" +
		"\334\351\352\354\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116" +
		"\u011d\u011f\u012e\u0130\u0131\u014b\u014c\u014d\u0157\u016e\u017f\u0181\72\143\170" +
		"\216\231\241\275\366\375\u0100\u0101\u0128\u013a\u0147\u0163\u0179\u0188\127\157" +
		"\247\253\356\150\203\150\203\213\262\147\215\105\105\130\105\130\105\130\265\u0135" +
		"\u016f\105\130\125\105\130\171\361\125\172\137\137\160\137\160\157\247\253\356\347" +
		"\350\u0127\155\155\137\160\137\160\216\216\366\231\u0147\u0128\236\303\351\352\354" +
		"\u0109\u012e\u0130\u0131\u016e\236\303\351\352\354\u0109\u012e\u0130\u0131\u016e" +
		"\236\303\351\352\354\u0109\u010e\u012e\u0130\u0131\u016e\236\303\351\352\354\u0109" +
		"\u010e\u012e\u0130\u0131\u016e\236\303\316\351\352\354\u0109\u010e\u012e\u0130\u0131" +
		"\u016e\236\303\316\317\321\322\351\352\354\u0107\u0109\u010e\u0110\u0111\u0112\u0114" +
		"\u0115\u0119\u012e\u0130\u0131\u0156\u0157\u015a\u015d\u016e\u0183\236\303\316\317" +
		"\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u012e\u0130\u0131\u0157\u016e" +
		"\236\303\316\317\351\352\354\u0109\u010e\u0110\u012e\u0130\u0131\u016e\236\303\316" +
		"\317\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131" +
		"\u0157\u016e\236\303\316\317\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115" +
		"\u011d\u012e\u0130\u0131\u0157\u016e\236\303\316\317\322\334\351\352\354\u0107\u0109" +
		"\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0157\u016e\236\303\316\317" +
		"\322\334\351\352\354\u0107\u0109\u010b\u010d\u010e\u0110\u0112\u0115\u0116\u011d" +
		"\u012e\u0130\u0131\u0157\u016e\236\303\316\317\322\334\351\352\354\u0107\u0109\u010b" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0157\u016e\236\303" +
		"\316\317\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131" +
		"\u0157\u016e\236\303\316\317\322\334\351\352\354\u0107\u0109\u010b\u010d\u010e\u0110" +
		"\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0157\u016e\236\303\316\317\322\334\351" +
		"\352\354\u0107\u0109\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e" +
		"\u0130\u0131\u0157\u016e\236\272\273\303\316\317\322\334\351\352\354\367\u0107\u0109" +
		"\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0157\u016e" +
		"\u010a\u014b\u014c\u017f\u0181\u010a\u014b\u017f\u0181\137\160\236\303\316\317\322" +
		"\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131\u0157\u016e" +
		"\137\160\236\303\316\317\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u011d" +
		"\u012e\u0130\u0131\u0157\u016e\137\160\165\236\303\316\317\322\351\352\354\u0107" +
		"\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131\u0157\u016e\237\157\253\237" +
		"\u0125\26\43\44\73\237\302\u0102\u0125\u0143\u0145\26\26\10\306\u014d\26\73\155\232" +
		"\72\375\u013a\u0163\302\u0143\u0145\302\u0143\u0145\1\6\41\106\116\300\6\6\53\53" +
		"\53\117\1\6\41\72\106\116\300\375\u011f\u013a\u0141\u0142\u0163\2\13\31\2\13\31\236" +
		"\303\316\317\322\351\352\354\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130" +
		"\u0131\u0157\u016e\u0171\1\6\37\41\106\116\130\156\160\236\300\303\322\351\352\354" +
		"\u0107\u0109\u010e\u0112\u0115\u012e\u0130\u0131\u0157\u016e\20\101\127\265\363\u0135" +
		"\u0171\242\243\344\347\350\u0127\236\303\316\317\321\322\351\352\354\u0107\u0109" +
		"\u010e\u0110\u0111\u0112\u0114\u0115\u0119\u012e\u0130\u0131\u0156\u0157\u015a\u015d" +
		"\u016e\u0183");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(4090,
		"\u01a1\u01a2\174\174\4\4\60\4\104\4\4\4\176\4\4\u0148\4\4\4\4\4\5\5\5\102\5\5\5\5" +
		"\u0134\5\5\5\5\5\5\124\124\155\155\301\301\301\301\301\301\301\301\301\301\301\301" +
		"\301\301\301\301\301\301\301\301\301\301\301\301\301\301\301\236\351\352\354\u012e" +
		"\u0130\u0131\u016e\62\107\u010e\u017f\u017f\u017f\u017f\u0143\u0143\u0143\106\147" +
		"\367\375\u010b\u013a\u0163\u0141\u0142\171\361\56\103\123\142\357\365\372\376\u010f" +
		"\u0138\u0139\u013b\u0168\u0169\u016b\u0172\u0174\u0189\u018a\u018b\u0194\u019b\35" +
		"\177\177\177\177\41\73\117\172\200\200\200\200\366\u0125\u0188\72\116\215\u010c\u0122" +
		"\u0128\u0128\u0128\u010c\6\6\6\125\6\6\125\201\201\201\302\201\6\302\302\302\302" +
		"\302\302\302\302\302\302\42\173\202\202\202\202\u0144\31\53\55\150\203\150\203\203" +
		"\303\150\150\203\303\u0109\u010a\303\303\303\303\303\303\303\150\303\303\u014b\303" +
		"\303\303\303\303\303\303\303\303\303\303\303\u016f\u014b\u014b\303\303\u014b\u014b" +
		"\101\120\122\260\261\263\360\u0146\u017b\u0180\u0195\u0196\u019c\304\304\304\304" +
		"\304\304\304\304\304\304\304\304\304\304\304\304\304\304\304\u013d\u0149\u017a\26" +
		"\204\237\204\204\237\204\26\26\74\205\205\205\205\u0126\206\206\206\206\u0120\u0120" +
		"\u019d\43\43\u0121\u0121\u019e\u010d\207\207\207\207\u011e\u0102\u0102\u0102\44\44" +
		"\u014c\u014c\u014c\u014c\u014c\210\210\210\210\u011d\u011d\u0181\u011d\u0181\u0181" +
		"\u0181\u0145\u0145\u0145\305\305\305\305\305\305\305\305\305\305\305\305\305\305" +
		"\305\305\305\305\305\305\305\305\305\305\305\156\211\156\156\211\211\156\211\156" +
		"\156\156\156\156\156\156\156\156\156\156\156\156\156\156\156\156\156\156\7\7\7\7" +
		"\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\10\17\10\17\45\17\57\61\63\10\45\45" +
		"\75\111\45\126\10\10\75\45\126\45\111\212\234\45\45\111\255\45\212\212\111\271\111" +
		"\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255" +
		"\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306" +
		"\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\11\11\11\11" +
		"\11\11\11\11\11\11\11\11\11\12\12\12\12\12\12\12\12\12\12\12\12\12\13\13\13\13\13" +
		"\13\u0147\u011f\36\307\307\307\307\307\307\307\307\307\307\307\307\307\307\307\307" +
		"\307\307\307\307\307\307\307\307\307\307\307\307\10\17\10\17\45\17\57\61\63\10\45" +
		"\45\75\111\45\126\10\10\75\143\45\126\45\111\212\234\45\45\111\255\45\212\212\111" +
		"\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306" +
		"\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340" +
		"\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10" +
		"\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\234\240" +
		"\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45\240\240\212\111\45\10" +
		"\45\306\45\306\306\306\306\45\45\306\306\306\240\255\111\111\111\111\45\306\306\u014d" +
		"\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d" +
		"\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45" +
		"\126\10\10\75\45\126\45\111\212\234\45\45\111\255\45\212\212\111\271\111\45\306\340" +
		"\111\45\45\212\111\45\10\u0100\45\306\45\306\306\306\306\45\45\306\306\306\255\111" +
		"\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306" +
		"\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45" +
		"\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\234\45\45\111\255" +
		"\45\212\212\111\271\111\45\306\340\111\45\45\212\111\45\10\u0101\45\306\45\306\306" +
		"\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306" +
		"\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306" +
		"\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45" +
		"\126\45\111\212\234\241\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45" +
		"\241\241\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\241\255\111\111" +
		"\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306" +
		"\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57" +
		"\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\216\234\45\45\111\255\45" +
		"\212\212\111\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306" +
		"\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306" +
		"\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d" +
		"\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111" +
		"\212\217\234\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45\212\111\45" +
		"\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d" +
		"\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d" +
		"\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45" +
		"\126\10\10\75\45\126\45\111\212\220\234\45\45\111\255\45\212\212\111\271\111\45\306" +
		"\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111" +
		"\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306" +
		"\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45" +
		"\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\221\234\45\45\111" +
		"\255\45\212\212\111\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306" +
		"\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306" +
		"\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306" +
		"\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45" +
		"\126\45\111\212\222\234\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45" +
		"\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45" +
		"\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45" +
		"\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10" +
		"\45\45\75\111\45\126\10\10\75\45\126\45\111\212\223\234\45\45\111\255\45\212\212" +
		"\111\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306" +
		"\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306" +
		"\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d" +
		"\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212" +
		"\234\45\45\111\255\45\212\212\111\271\272\111\45\306\340\111\45\45\212\111\45\10" +
		"\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d" +
		"\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d" +
		"\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45" +
		"\126\10\10\75\45\126\45\111\212\234\45\45\111\255\45\212\212\111\271\273\111\45\306" +
		"\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111" +
		"\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306" +
		"\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45" +
		"\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\224\234\45\45\111" +
		"\255\45\212\212\111\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306" +
		"\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306" +
		"\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306" +
		"\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45" +
		"\126\45\111\212\225\234\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45" +
		"\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45" +
		"\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45" +
		"\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10" +
		"\45\45\75\111\45\126\10\10\75\45\126\45\111\212\226\234\45\45\111\255\45\212\212" +
		"\111\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306" +
		"\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306" +
		"\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d" +
		"\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212" +
		"\227\234\45\45\111\255\45\212\212\111\271\111\227\45\306\340\111\45\45\212\111\45" +
		"\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d" +
		"\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d" +
		"\10\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45" +
		"\126\10\10\75\45\126\45\111\212\230\234\45\45\111\255\45\212\212\111\271\111\230" +
		"\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255" +
		"\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306" +
		"\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17" +
		"\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\234\45\45\111\255" +
		"\45\212\212\111\271\111\45\306\340\111\45\45\212\364\111\45\10\45\306\45\306\306" +
		"\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306" +
		"\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306" +
		"\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45" +
		"\126\45\111\212\234\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45\212" +
		"\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45\306" +
		"\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111" +
		"\u014d\u014d\10\306\111\306\u018d\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63" +
		"\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\234\242\45\45\111\255\45\212\212" +
		"\111\271\111\45\306\340\344\111\45\45\242\242\212\111\45\10\45\306\45\306\306\306" +
		"\306\45\45\306\306\306\242\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306" +
		"\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306" +
		"\u018e\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10" +
		"\75\45\126\45\111\212\234\243\45\45\111\255\45\212\212\111\271\111\45\306\340\111" +
		"\45\45\243\243\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\243\255" +
		"\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306" +
		"\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17" +
		"\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\234\244\45\45\111" +
		"\255\45\212\212\111\271\111\45\306\340\111\45\45\244\244\212\111\45\10\45\306\45" +
		"\306\306\306\306\45\45\306\306\306\244\255\111\111\111\111\45\306\306\u014d\306\10" +
		"\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10" +
		"\306\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126" +
		"\10\10\75\45\126\45\111\212\234\45\45\111\255\45\212\212\111\271\111\45\306\340\111" +
		"\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111" +
		"\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111" +
		"\45\45\111\u014d\u014d\10\306\111\306\u018f\111\u014d\u014d\111\10\17\10\17\45\17" +
		"\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\234\45\45\111\255\45" +
		"\212\212\111\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306" +
		"\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306" +
		"\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306\u0190\111" +
		"\u014d\u014d\111\2\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45" +
		"\126\45\111\212\234\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45\212" +
		"\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45\306" +
		"\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111" +
		"\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10\17\10\17\32\45\17\57\61\63\10" +
		"\45\45\75\111\45\32\126\10\10\75\45\126\45\111\212\234\45\45\111\255\45\212\212\111" +
		"\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306" +
		"\306\255\111\111\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340" +
		"\111\306\306\306\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\10" +
		"\17\10\17\45\17\57\61\63\10\45\45\75\105\111\45\126\10\10\75\45\126\45\111\212\234" +
		"\45\45\111\255\45\212\212\111\271\111\45\306\340\111\45\45\212\111\45\10\45\306\45" +
		"\306\306\306\306\45\45\306\306\306\255\111\111\111\111\45\306\306\u014d\306\10\306" +
		"\306\306\306\306\306\306\10\340\111\306\306\306\111\45\45\111\u014d\u014d\10\306" +
		"\111\306\111\u014d\u014d\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10" +
		"\137\10\75\45\126\45\111\212\234\45\45\111\255\45\212\212\111\271\111\45\306\340" +
		"\111\45\45\212\111\45\10\45\306\45\306\306\306\306\45\45\306\306\306\255\111\111" +
		"\111\111\45\306\306\u014d\306\10\306\306\306\306\306\306\306\10\340\111\306\306\306" +
		"\111\45\45\111\u014d\u014d\10\306\111\306\111\u014d\u014d\111\310\310\310\310\310" +
		"\310\310\310\310\310\310\310\310\310\310\310\310\310\310\310\310\300\22\u019f\37" +
		"\3\64\110\33\33\40\65\46\46\46\46\127\144\127\157\157\253\144\277\311\46\346\346" +
		"\374\46\311\u0108\311\311\311\311\u0124\346\311\311\311\46\311\311\u0151\u0151\311" +
		"\311\311\311\311\311\46\311\311\311\46\46\311\311\14\14\14\14\14\312\14\312\312\312" +
		"\312\312\312\312\312\312\312\u014e\312\312\312\312\312\312\312\312\312\u0161\312" +
		"\312\312\u014e\u014e\u017e\312\312\u014e\u014e\112\170\254\266\274\345\373\266\112" +
		"\u013e\u013f\u0166\112\274\112\373\u019a\151\245\245\245\245\213\262\214\214\264" +
		"\264\175\265\130\131\153\132\132\133\133\362\u0170\u0191\134\134\145\135\135\256" +
		"\u0133\146\257\160\161\250\162\162\246\353\355\u0132\u0129\u0129\u0129\231\232\163" +
		"\163\164\164\267\270\u0136\275\u0179\u0167\313\313\313\313\313\313\313\313\313\313" +
		"\314\u0106\u012c\u012d\u012f\u014a\u016a\u016c\u016d\u018c\315\315\315\315\315\315" +
		"\u0155\315\315\315\315\316\316\316\316\316\316\316\316\316\316\316\317\317\u0110" +
		"\317\317\317\317\317\317\317\317\317\320\320\320\320\320\320\320\320\320\320\320" +
		"\320\320\320\320\320\320\320\320\320\320\320\320\320\320\320\320\321\321\u0111\u0114" +
		"\u0119\321\321\321\u0119\321\321\u0156\u015a\u015d\321\321\321\u0183\321\322\u0107" +
		"\u0112\u0115\322\322\322\322\322\u0157\322\322\322\322\323\323\323\323\u011a\323" +
		"\323\323\u011a\323\323\323\u011a\u011a\u0160\323\323\323\u011a\323\324\324\324\324" +
		"\324\324\324\324\324\324\324\324\324\324\324\324\324\324\324\324\325\325\325\325" +
		"\325\u0123\325\325\325\325\325\325\325\325\325\u0123\325\325\325\325\325\325\326" +
		"\326\326\326\326\326\326\326\326\326\326\u0152\u0154\326\326\326\326\326\326\326" +
		"\326\326\326\326\327\327\327\327\327\327\327\327\327\327\327\327\327\327\327\327" +
		"\327\327\327\327\327\327\327\327\330\330\330\330\330\330\330\330\330\330\330\330" +
		"\330\330\330\330\330\330\330\330\331\331\331\331\331\331\331\331\331\331\331\331" +
		"\331\331\331\331\331\331\331\331\331\331\331\331\332\332\332\332\332\332\332\332" +
		"\332\332\332\332\u0153\332\332\332\332\332\332\332\332\332\332\332\332\333\370\371" +
		"\333\333\333\333\333\333\333\333\u0137\333\333\333\333\333\333\333\333\333\333\333" +
		"\333\333\333\333\333\u014f\u014f\u017d\u014f\u014f\u0150\u017c\u0197\u0198\165\165" +
		"\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165" +
		"\166\166\334\334\334\u0116\u0116\334\334\334\u0116\334\334\u0116\u0116\u0116\u0116" +
		"\334\334\334\u0116\334\167\167\252\167\167\167\167\167\167\167\167\167\167\167\167" +
		"\167\167\167\167\167\167\167\167\341\247\356\342\u0164\47\70\71\47\343\u0103\u0140" +
		"\343\u0103\u0103\50\51\27\27\27\52\115\233\276\113\u013c\u0173\u0187\u0104\u0104" +
		"\u0104\u0105\u0177\u0178\u01a0\23\67\136\140\377\24\25\76\77\100\141\15\15\15\114" +
		"\15\15\15\114\u0162\114\u0175\u0176\114\20\30\54\21\21\21\335\335\335\335\335\335" +
		"\335\335\335\335\335\335\335\335\335\335\335\335\335\335\u0192\16\16\66\16\16\16" +
		"\154\235\251\336\16\336\u011b\336\336\336\u011b\336\336\u011b\u011b\336\336\336\u011b" +
		"\336\34\121\152\363\u0135\u0171\u0193\347\350\u0127\u012a\u012b\u0165\337\337\u0113" +
		"\u0117\u0118\u011c\337\337\337\u011c\337\337\u0158\u0159\u015b\u015c\u015e\u015f" +
		"\337\337\337\u0182\u0184\u0185\u0186\337\u0199");

	private static final short[] tmRuleLen = TMLexer.unpack_short(239,
		"\2\0\5\4\2\0\7\4\3\3\4\4\3\3\1\1\2\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\3\2\1\1\2\2" +
		"\1\1\1\1\3\10\3\2\3\1\1\1\1\5\3\1\3\1\3\1\1\2\2\1\1\1\7\6\6\5\6\5\5\4\2\4\3\3\1\1" +
		"\2\1\1\1\1\1\1\7\5\6\4\4\4\5\5\6\3\1\2\1\1\2\1\3\3\1\1\5\4\4\3\4\3\3\2\4\3\3\2\3" +
		"\2\2\1\1\3\2\3\3\4\3\1\2\2\1\1\1\1\2\1\3\3\1\2\1\3\3\3\1\3\1\3\6\6\2\2\4\1\4\2\1" +
		"\3\2\1\3\3\2\1\1\5\2\2\3\1\3\1\4\2\1\3\1\1\0\3\3\2\2\1\1\1\1\1\2\1\3\3\1\3\3\1\1" +
		"\5\3\1\3\1\1\0\3\1\1\0\3\1\1\1\1\1\1\3\1\1\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0");

	private static final short[] tmRuleSymbol = TMLexer.unpack_short(239,
		"\117\117\120\120\121\121\122\122\123\124\125\126\126\127\127\130\131\131\132\133" +
		"\133\134\134\135\135\135\135\135\135\135\135\135\135\135\135\135\136\137\137\137" +
		"\140\140\140\140\141\142\142\143\144\145\145\145\145\146\147\147\150\151\152\152" +
		"\153\153\153\154\154\154\155\155\155\155\155\155\155\155\156\156\156\156\156\156" +
		"\157\160\160\160\161\161\161\162\162\162\162\163\163\163\163\163\164\164\165\165" +
		"\166\166\167\167\170\170\171\172\172\172\172\172\172\172\172\172\172\172\172\172" +
		"\172\172\172\172\173\174\175\175\176\176\177\177\177\200\200\200\201\201\202\202" +
		"\202\203\203\204\204\204\205\206\206\207\207\207\207\207\207\207\207\210\211\211" +
		"\211\211\212\212\212\213\213\214\215\215\215\216\216\217\220\220\220\221\222\222" +
		"\223\223\224\225\225\225\225\226\226\227\227\230\230\230\230\231\231\231\232\232" +
		"\232\232\232\233\233\234\234\235\235\236\236\237\240\240\240\240\241\242\242\243" +
		"\244\245\245\246\246\247\247\250\250\251\251\252\252\253\253\254\254\255\255");

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
		int rhsAnnotated = 129;
		int rhsAssignment = 130;
		int rhsOptional = 131;
		int rhsCast = 132;
		int rhsUnordered = 133;
		int rhsClass = 134;
		int rhsPrimary = 135;
		int rhsSet = 136;
		int setPrimary = 137;
		int setExpression = 138;
		int annotation_list = 139;
		int annotations = 140;
		int annotation = 141;
		int nonterm_param_list_Comma_separated = 142;
		int nonterm_params = 143;
		int nonterm_param = 144;
		int param_ref = 145;
		int argument_list_Comma_separated = 146;
		int argument_list_Comma_separated_opt = 147;
		int symref_args = 148;
		int argument = 149;
		int param_type = 150;
		int param_value = 151;
		int predicate_primary = 152;
		int predicate_expression = 153;
		int expression = 154;
		int expression_list_Comma_separated = 155;
		int expression_list_Comma_separated_opt = 156;
		int map_entry_list_Comma_separated = 157;
		int map_entry_list_Comma_separated_opt = 158;
		int map_entry = 159;
		int literal = 160;
		int name = 161;
		int qualified_id = 162;
		int command = 163;
		int syntax_problem = 164;
		int parsing_algorithmopt = 165;
		int typeopt = 166;
		int lexeme_transitionopt = 167;
		int iconopt = 168;
		int lexeme_attrsopt = 169;
		int commandopt = 170;
		int identifieropt = 171;
		int implementsopt = 172;
		int rhsSuffixopt = 173;
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
		int rhsOptional_rhsQuantifier = 142;  // rhsOptional ::= rhsCast '?'
		int rhsCast_rhsAsLiteral = 145;  // rhsCast ::= rhsClass Las literal
		int rhsPrimary_rhsSymbol = 149;  // rhsPrimary ::= symref
		int rhsPrimary_rhsNested = 150;  // rhsPrimary ::= '(' rules ')'
		int rhsPrimary_rhsList = 151;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		int rhsPrimary_rhsList2 = 152;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		int rhsPrimary_rhsQuantifier = 153;  // rhsPrimary ::= rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 154;  // rhsPrimary ::= rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 155;  // rhsPrimary ::= '$' '(' rules ')'
		int setPrimary_setSymbol = 158;  // setPrimary ::= ID symref
		int setPrimary_setSymbol2 = 159;  // setPrimary ::= symref
		int setPrimary_setCompound = 160;  // setPrimary ::= '(' setExpression ')'
		int setPrimary_setComplement = 161;  // setPrimary ::= '~' setPrimary
		int setExpression_setBinary = 163;  // setExpression ::= setExpression '|' setExpression
		int setExpression_setBinary2 = 164;  // setExpression ::= setExpression '&' setExpression
		int nonterm_param_inlineParameter = 175;  // nonterm_param ::= ID identifier '=' param_value
		int nonterm_param_inlineParameter2 = 176;  // nonterm_param ::= ID identifier
		int predicate_primary_boolPredicate = 191;  // predicate_primary ::= '!' param_ref
		int predicate_primary_boolPredicate2 = 192;  // predicate_primary ::= param_ref
		int predicate_primary_comparePredicate = 193;  // predicate_primary ::= param_ref '==' literal
		int predicate_primary_comparePredicate2 = 194;  // predicate_primary ::= param_ref '!=' literal
		int predicate_expression_predicateBinary = 196;  // predicate_expression ::= predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 197;  // predicate_expression ::= predicate_expression '||' predicate_expression
		int expression_instance = 200;  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 201;  // expression ::= '[' expression_list_Comma_separated_opt ']'
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
			case 137:  // rhsAnnotated ::= annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // rhsAssignment ::= identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 140:  // rhsAssignment ::= identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // rhsOptional ::= rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUESTIONMARK /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // rhsCast ::= rhsClass Las symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // rhsCast ::= rhsClass Las literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 146:  // rhsUnordered ::= rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // rhsClass ::= identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // rhsPrimary ::= symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rhsPrimary ::= '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rhsPrimary ::= rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rhsPrimary ::= rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rhsPrimary ::= '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // rhsSet ::= Lset '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // setPrimary ::= ID symref
				tmLeft.value = new TmaSetSymbol(
						((String)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // setPrimary ::= symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // setPrimary ::= '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // setPrimary ::= '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // setExpression ::= setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // setExpression ::= setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AMPERSAND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 166:  // annotation_list ::= annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 167:  // annotations ::= annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 168:  // annotation ::= '@' ID '{' expression '}'
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 3].value) /* name */,
						((ITmaExpression)tmStack[tmHead - 1].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 169:  // annotation ::= '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 170:  // annotation ::= '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 171:  // nonterm_param_list_Comma_separated ::= nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 172:  // nonterm_param_list_Comma_separated ::= nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 173:  // nonterm_params ::= '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // nonterm_param ::= ID identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 176:  // nonterm_param ::= ID identifier
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // param_ref ::= identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 178:  // argument_list_Comma_separated ::= argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 179:  // argument_list_Comma_separated ::= argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 182:  // symref_args ::= '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 183:  // argument ::= param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 184:  // argument ::= '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // argument ::= '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // argument ::= param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 187:  // param_type ::= Lflag
				tmLeft.value = TmaParamType.LFLAG;
				break;
			case 188:  // param_type ::= Lparam
				tmLeft.value = TmaParamType.LPARAM;
				break;
			case 191:  // predicate_primary ::= '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // predicate_primary ::= param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // predicate_primary ::= param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EQUAL_EQUAL /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 194:  // predicate_primary ::= param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCLAMATION_EQUAL /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // predicate_expression ::= predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AMPERSAND_AMPERSAND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 197:  // predicate_expression ::= predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 200:  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 201:  // expression ::= '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 203:  // expression_list_Comma_separated ::= expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 204:  // expression_list_Comma_separated ::= expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 207:  // map_entry_list_Comma_separated ::= map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 208:  // map_entry_list_Comma_separated ::= map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 211:  // map_entry ::= ID ':' expression
				tmLeft.value = new TmaMapEntry(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 212:  // literal ::= scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 213:  // literal ::= icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 214:  // literal ::= Ltrue
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 215:  // literal ::= Lfalse
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 216:  // name ::= qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 218:  // qualified_id ::= qualified_id '.' ID
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 219:  // command ::= code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 220:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 417);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 418);
	}
}
