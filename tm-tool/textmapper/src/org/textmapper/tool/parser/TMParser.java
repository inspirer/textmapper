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
	private static final int[] tmAction = TMLexer.unpack_int(426,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\323\0\324\0\uffb5\uffff\333\0\uff63" +
		"\uffff\325\0\326\0\uffff\uffff\306\0\305\0\311\0\330\0\ufeef\uffff\ufee7\uffff\ufedb" +
		"\uffff\313\0\ufe93\uffff\uffff\uffff\ufe8d\uffff\20\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\334\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\310" +
		"\0\uffff\uffff\uffff\uffff\17\0\260\0\ufe45\uffff\ufe3d\uffff\uffff\uffff\262\0\ufe37" +
		"\uffff\uffff\uffff\uffff\uffff\7\0\331\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufdf3\uffff\4\0\16\0\312\0\267\0\270\0\uffff\uffff\uffff\uffff\265\0\uffff" +
		"\uffff\ufded\uffff\uffff\uffff\317\0\ufde7\uffff\uffff\uffff\14\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\2\0\22\0\275\0\266\0\274\0\261\0\uffff\uffff\uffff" +
		"\uffff\307\0\uffff\uffff\12\0\13\0\uffff\uffff\uffff\uffff\ufde1\uffff\ufdd9\uffff" +
		"\ufdd3\uffff\25\0\31\0\34\0\uffff\uffff\32\0\33\0\30\0\15\0\uffff\uffff\322\0\316" +
		"\0\6\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\61\0\uffff\uffff\42\0\uffff" +
		"\uffff\23\0\336\0\uffff\uffff\26\0\27\0\uffff\uffff\ufd87\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\ufd81\uffff\63\0\66\0\67\0\70\0\ufd37\uffff\uffff\uffff\245\0" +
		"\uffff\uffff\62\0\uffff\uffff\56\0\uffff\uffff\37\0\uffff\uffff\40\0\24\0\35\0\ufceb" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\110\0\111\0\112\0\uffff\uffff\uffff\uffff" +
		"\114\0\113\0\115\0\273\0\272\0\uffff\uffff\uffff\uffff\uffff\uffff\ufc9b\uffff\251" +
		"\0\ufc4d\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufbf1\uffff\ufbab\uffff\105\0" +
		"\106\0\uffff\uffff\uffff\uffff\64\0\65\0\244\0\uffff\uffff\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\41\0\ufb65\uffff\36\0\ufb11\uffff\ufac1\uffff\uffff\uffff\130\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\133\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufab9\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\ufa5d\uffff\uffff\uffff\332\0\uffff\uffff\224\0\uf9ed\uffff\uffff\uffff\140\0\uf9e5" +
		"\uffff\uf98b\uffff\352\0\uf931\uffff\uf927\uffff\uf8cb\uffff\200\0\177\0\174\0\207" +
		"\0\211\0\uf86b\uffff\175\0\uf809\uffff\uf7a5\uffff\233\0\uffff\uffff\176\0\162\0" +
		"\161\0\uf73d\uffff\uffff\uffff\253\0\255\0\uf6f7\uffff\101\0\346\0\uf6b1\uffff\uf6ab" +
		"\uffff\uf6a5\uffff\uf649\uffff\uffff\uffff\uf5ed\uffff\uffff\uffff\uffff\uffff\54" +
		"\0\55\0\340\0\uf591\uffff\uf53f\uffff\131\0\123\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\122\0\134\0\uffff\uffff\uffff\uffff\121\0\247\0\uffff\uffff\uffff\uffff" +
		"\206\0\uffff\uffff\uf4eb\uffff\302\0\uffff\uffff\uffff\uffff\uf4df\uffff\uffff\uffff" +
		"\205\0\uffff\uffff\202\0\uf483\uffff\uf477\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uf41b\uffff\100\0\uf3bd\uffff\uf363\uffff\uf359\uffff\151\0\uf2fd\uffff" +
		"\uf2f3\uffff\uffff\uffff\155\0\160\0\uf297\uffff\uf28d\uffff\173\0\157\0\uffff\uffff" +
		"\215\0\uffff\uffff\230\0\231\0\164\0\210\0\uf22d\uffff\uffff\uffff\254\0\uf225\uffff" +
		"\uffff\uffff\350\0\103\0\104\0\uffff\uffff\uffff\uffff\uf21f\uffff\uffff\uffff\uf1c3" +
		"\uffff\uf167\uffff\uffff\uffff\uffff\uffff\342\0\uf10b\uffff\uf0bb\uffff\127\0\uffff" +
		"\uffff\124\0\125\0\uffff\uffff\117\0\uffff\uffff\165\0\166\0\276\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\163\0\uffff\uffff\225\0\uffff\uffff\204\0\203\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf069\uffff\236\0\241\0\uffff\uffff\uffff" +
		"\uffff\212\0\uf01f\uffff\213\0\137\0\uefb7\uffff\uefad\uffff\145\0\150\0\uef51\uffff" +
		"\147\0\154\0\uef47\uffff\153\0\156\0\uef3d\uffff\217\0\220\0\uffff\uffff\252\0\102" +
		"\0\135\0\ueedd\uffff\77\0\76\0\uffff\uffff\74\0\uffff\uffff\uffff\uffff\ueed7\uffff" +
		"\50\0\51\0\52\0\53\0\uffff\uffff\344\0\45\0\uee7b\uffff\126\0\uffff\uffff\120\0\300" +
		"\0\301\0\uee2b\uffff\uee23\uffff\uffff\uffff\201\0\167\0\232\0\uffff\uffff\240\0" +
		"\235\0\uffff\uffff\234\0\uffff\uffff\144\0\uee1b\uffff\143\0\146\0\152\0\256\0\uffff" +
		"\uffff\75\0\73\0\72\0\uffff\uffff\47\0\43\0\116\0\uffff\uffff\237\0\uee11\uffff\uee09" +
		"\uffff\142\0\136\0\71\0\227\0\226\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4604,
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
		"\47\0\uffff\uffff\50\0\uffff\uffff\22\0\315\0\uffff\uffff\ufffe\uffff\30\0\uffff" +
		"\uffff\0\0\21\0\6\0\21\0\7\0\21\0\10\0\21\0\15\0\21\0\16\0\21\0\17\0\21\0\20\0\21" +
		"\0\22\0\21\0\23\0\21\0\24\0\21\0\25\0\21\0\26\0\21\0\32\0\21\0\33\0\21\0\35\0\21" +
		"\0\40\0\21\0\42\0\21\0\43\0\21\0\44\0\21\0\45\0\21\0\51\0\21\0\52\0\21\0\54\0\21" +
		"\0\56\0\21\0\57\0\21\0\60\0\21\0\61\0\21\0\62\0\21\0\63\0\21\0\64\0\21\0\65\0\21" +
		"\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0\72\0\21\0\73\0\21\0\74\0\21\0\75\0\21" +
		"\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21\0\102\0\21\0\103\0\21\0\104\0\21\0\105" +
		"\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111\0\21\0\112\0\21\0\113\0\21\0\114\0\21" +
		"\0\115\0\21\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\112\0\uffff\uffff\15\0\335" +
		"\0\uffff\uffff\ufffe\uffff\16\0\uffff\uffff\15\0\327\0\23\0\327\0\26\0\327\0\112" +
		"\0\327\0\uffff\uffff\ufffe\uffff\53\0\uffff\uffff\7\0\5\0\44\0\5\0\45\0\5\0\56\0" +
		"\5\0\57\0\5\0\60\0\5\0\61\0\5\0\62\0\5\0\63\0\5\0\64\0\5\0\65\0\5\0\66\0\5\0\67\0" +
		"\5\0\70\0\5\0\71\0\5\0\72\0\5\0\73\0\5\0\74\0\5\0\75\0\5\0\76\0\5\0\77\0\5\0\100" +
		"\0\5\0\101\0\5\0\102\0\5\0\103\0\5\0\104\0\5\0\105\0\5\0\106\0\5\0\107\0\5\0\110" +
		"\0\5\0\111\0\5\0\112\0\5\0\113\0\5\0\114\0\5\0\uffff\uffff\ufffe\uffff\17\0\uffff" +
		"\uffff\22\0\314\0\uffff\uffff\ufffe\uffff\33\0\uffff\uffff\37\0\uffff\uffff\45\0" +
		"\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff" +
		"\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\31\0\264\0\uffff\uffff\ufffe" +
		"\uffff\20\0\uffff\uffff\17\0\271\0\31\0\271\0\uffff\uffff\ufffe\uffff\17\0\uffff" +
		"\uffff\31\0\263\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113" +
		"\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\26\0\321\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\0\0\3\0\uffff" +
		"\uffff\ufffe\uffff\17\0\uffff\uffff\26\0\320\0\uffff\uffff\ufffe\uffff\112\0\uffff" +
		"\uffff\15\0\335\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\20\0\17\0\115\0\17\0\uffff" +
		"\uffff\ufffe\uffff\115\0\uffff\uffff\20\0\337\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0" +
		"\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff" +
		"\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\0\0\10\0\7\0\10\0\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\20" +
		"\0\337\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\0\0\11\0\uffff\uffff\ufffe" +
		"\uffff\43\0\uffff\uffff\20\0\246\0\23\0\246\0\42\0\246\0\45\0\246\0\54\0\246\0\56" +
		"\0\246\0\57\0\246\0\60\0\246\0\61\0\246\0\62\0\246\0\63\0\246\0\64\0\246\0\65\0\246" +
		"\0\66\0\246\0\67\0\246\0\70\0\246\0\71\0\246\0\72\0\246\0\73\0\246\0\74\0\246\0\75" +
		"\0\246\0\76\0\246\0\77\0\246\0\100\0\246\0\101\0\246\0\102\0\246\0\103\0\246\0\104" +
		"\0\246\0\105\0\246\0\106\0\246\0\107\0\246\0\110\0\246\0\111\0\246\0\112\0\246\0" +
		"\113\0\246\0\114\0\246\0\uffff\uffff\ufffe\uffff\117\0\uffff\uffff\0\0\46\0\6\0\46" +
		"\0\7\0\46\0\27\0\46\0\30\0\46\0\44\0\46\0\45\0\46\0\56\0\46\0\57\0\46\0\60\0\46\0" +
		"\61\0\46\0\62\0\46\0\63\0\46\0\64\0\46\0\65\0\46\0\66\0\46\0\67\0\46\0\70\0\46\0" +
		"\71\0\46\0\72\0\46\0\73\0\46\0\74\0\46\0\75\0\46\0\76\0\46\0\77\0\46\0\100\0\46\0" +
		"\101\0\46\0\102\0\46\0\103\0\46\0\104\0\46\0\105\0\46\0\106\0\46\0\107\0\46\0\110" +
		"\0\46\0\111\0\46\0\112\0\46\0\113\0\46\0\114\0\46\0\uffff\uffff\ufffe\uffff\12\0" +
		"\uffff\uffff\20\0\250\0\23\0\250\0\42\0\250\0\43\0\250\0\45\0\250\0\54\0\250\0\56" +
		"\0\250\0\57\0\250\0\60\0\250\0\61\0\250\0\62\0\250\0\63\0\250\0\64\0\250\0\65\0\250" +
		"\0\66\0\250\0\67\0\250\0\70\0\250\0\71\0\250\0\72\0\250\0\73\0\250\0\74\0\250\0\75" +
		"\0\250\0\76\0\250\0\77\0\250\0\100\0\250\0\101\0\250\0\102\0\250\0\103\0\250\0\104" +
		"\0\250\0\105\0\250\0\106\0\250\0\107\0\250\0\110\0\250\0\111\0\250\0\112\0\250\0" +
		"\113\0\250\0\114\0\250\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0\15\0\353\0\uffff" +
		"\uffff\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\20\0\347\0\55\0\347\0\uffff\uffff\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\20\0\347\0\55\0\347\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0" +
		"\0\341\0\6\0\341\0\7\0\341\0\23\0\341\0\27\0\341\0\30\0\341\0\44\0\341\0\45\0\341" +
		"\0\56\0\341\0\57\0\341\0\60\0\341\0\61\0\341\0\62\0\341\0\63\0\341\0\64\0\341\0\65" +
		"\0\341\0\66\0\341\0\67\0\341\0\70\0\341\0\71\0\341\0\72\0\341\0\73\0\341\0\74\0\341" +
		"\0\75\0\341\0\76\0\341\0\77\0\341\0\100\0\341\0\101\0\341\0\102\0\341\0\103\0\341" +
		"\0\104\0\341\0\105\0\341\0\106\0\341\0\107\0\341\0\110\0\341\0\111\0\341\0\112\0" +
		"\341\0\113\0\341\0\114\0\341\0\115\0\341\0\uffff\uffff\ufffe\uffff\117\0\uffff\uffff" +
		"\0\0\44\0\6\0\44\0\7\0\44\0\27\0\44\0\30\0\44\0\44\0\44\0\45\0\44\0\56\0\44\0\57" +
		"\0\44\0\60\0\44\0\61\0\44\0\62\0\44\0\63\0\44\0\64\0\44\0\65\0\44\0\66\0\44\0\67" +
		"\0\44\0\70\0\44\0\71\0\44\0\72\0\44\0\73\0\44\0\74\0\44\0\75\0\44\0\76\0\44\0\77" +
		"\0\44\0\100\0\44\0\101\0\44\0\102\0\44\0\103\0\44\0\104\0\44\0\105\0\44\0\106\0\44" +
		"\0\107\0\44\0\110\0\44\0\111\0\44\0\112\0\44\0\113\0\44\0\114\0\44\0\uffff\uffff" +
		"\ufffe\uffff\102\0\uffff\uffff\15\0\132\0\17\0\132\0\uffff\uffff\ufffe\uffff\6\0" +
		"\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\26\0\353\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff\12\0\17\0\20\0\17\0\34\0\17" +
		"\0\6\0\21\0\10\0\21\0\15\0\21\0\16\0\21\0\23\0\21\0\24\0\21\0\25\0\21\0\26\0\21\0" +
		"\32\0\21\0\33\0\21\0\35\0\21\0\40\0\21\0\42\0\21\0\43\0\21\0\44\0\21\0\45\0\21\0" +
		"\51\0\21\0\52\0\21\0\54\0\21\0\56\0\21\0\57\0\21\0\60\0\21\0\61\0\21\0\62\0\21\0" +
		"\63\0\21\0\64\0\21\0\65\0\21\0\66\0\21\0\67\0\21\0\70\0\21\0\71\0\21\0\72\0\21\0" +
		"\73\0\21\0\74\0\21\0\75\0\21\0\76\0\21\0\77\0\21\0\100\0\21\0\101\0\21\0\102\0\21" +
		"\0\103\0\21\0\104\0\21\0\105\0\21\0\106\0\21\0\107\0\21\0\110\0\21\0\111\0\21\0\112" +
		"\0\21\0\113\0\21\0\114\0\21\0\115\0\21\0\uffff\uffff\ufffe\uffff\10\0\uffff\uffff" +
		"\15\0\141\0\26\0\141\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23" +
		"\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0" +
		"\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\15\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff\40\0\uffff\uffff\6\0\171\0\10\0\171" +
		"\0\15\0\171\0\16\0\171\0\23\0\171\0\24\0\171\0\25\0\171\0\26\0\171\0\42\0\171\0\43" +
		"\0\171\0\44\0\171\0\45\0\171\0\51\0\171\0\54\0\171\0\56\0\171\0\57\0\171\0\60\0\171" +
		"\0\61\0\171\0\62\0\171\0\63\0\171\0\64\0\171\0\65\0\171\0\66\0\171\0\67\0\171\0\70" +
		"\0\171\0\71\0\171\0\72\0\171\0\73\0\171\0\74\0\171\0\75\0\171\0\76\0\171\0\77\0\171" +
		"\0\100\0\171\0\101\0\171\0\102\0\171\0\103\0\171\0\104\0\171\0\105\0\171\0\106\0" +
		"\171\0\107\0\171\0\110\0\171\0\111\0\171\0\112\0\171\0\113\0\171\0\114\0\171\0\115" +
		"\0\171\0\uffff\uffff\ufffe\uffff\35\0\uffff\uffff\6\0\214\0\10\0\214\0\15\0\214\0" +
		"\16\0\214\0\23\0\214\0\24\0\214\0\25\0\214\0\26\0\214\0\40\0\214\0\42\0\214\0\43" +
		"\0\214\0\44\0\214\0\45\0\214\0\51\0\214\0\54\0\214\0\56\0\214\0\57\0\214\0\60\0\214" +
		"\0\61\0\214\0\62\0\214\0\63\0\214\0\64\0\214\0\65\0\214\0\66\0\214\0\67\0\214\0\70" +
		"\0\214\0\71\0\214\0\72\0\214\0\73\0\214\0\74\0\214\0\75\0\214\0\76\0\214\0\77\0\214" +
		"\0\100\0\214\0\101\0\214\0\102\0\214\0\103\0\214\0\104\0\214\0\105\0\214\0\106\0" +
		"\214\0\107\0\214\0\110\0\214\0\111\0\214\0\112\0\214\0\113\0\214\0\114\0\214\0\115" +
		"\0\214\0\uffff\uffff\ufffe\uffff\52\0\uffff\uffff\6\0\216\0\10\0\216\0\15\0\216\0" +
		"\16\0\216\0\23\0\216\0\24\0\216\0\25\0\216\0\26\0\216\0\35\0\216\0\40\0\216\0\42" +
		"\0\216\0\43\0\216\0\44\0\216\0\45\0\216\0\51\0\216\0\54\0\216\0\56\0\216\0\57\0\216" +
		"\0\60\0\216\0\61\0\216\0\62\0\216\0\63\0\216\0\64\0\216\0\65\0\216\0\66\0\216\0\67" +
		"\0\216\0\70\0\216\0\71\0\216\0\72\0\216\0\73\0\216\0\74\0\216\0\75\0\216\0\76\0\216" +
		"\0\77\0\216\0\100\0\216\0\101\0\216\0\102\0\216\0\103\0\216\0\104\0\216\0\105\0\216" +
		"\0\106\0\216\0\107\0\216\0\110\0\216\0\111\0\216\0\112\0\216\0\113\0\216\0\114\0" +
		"\216\0\115\0\216\0\uffff\uffff\ufffe\uffff\32\0\uffff\uffff\33\0\uffff\uffff\6\0" +
		"\222\0\10\0\222\0\15\0\222\0\16\0\222\0\23\0\222\0\24\0\222\0\25\0\222\0\26\0\222" +
		"\0\35\0\222\0\40\0\222\0\42\0\222\0\43\0\222\0\44\0\222\0\45\0\222\0\51\0\222\0\52" +
		"\0\222\0\54\0\222\0\56\0\222\0\57\0\222\0\60\0\222\0\61\0\222\0\62\0\222\0\63\0\222" +
		"\0\64\0\222\0\65\0\222\0\66\0\222\0\67\0\222\0\70\0\222\0\71\0\222\0\72\0\222\0\73" +
		"\0\222\0\74\0\222\0\75\0\222\0\76\0\222\0\77\0\222\0\100\0\222\0\101\0\222\0\102" +
		"\0\222\0\103\0\222\0\104\0\222\0\105\0\222\0\106\0\222\0\107\0\222\0\110\0\222\0" +
		"\111\0\222\0\112\0\222\0\113\0\222\0\114\0\222\0\115\0\222\0\uffff\uffff\ufffe\uffff" +
		"\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\17\0\17\0\31\0\17\0\uffff" +
		"\uffff\ufffe\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\20\0\347\0\55\0\347\0\uffff\uffff\ufffe\uffff\55\0\uffff\uffff\20\0\351\0\uffff" +
		"\uffff\ufffe\uffff\55\0\uffff\uffff\20\0\351\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0" +
		"\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff" +
		"\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\15\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0\15\0\353\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\15\0\353\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0\343\0\6\0\343\0\7\0\343" +
		"\0\27\0\343\0\30\0\343\0\44\0\343\0\45\0\343\0\56\0\343\0\57\0\343\0\60\0\343\0\61" +
		"\0\343\0\62\0\343\0\63\0\343\0\64\0\343\0\65\0\343\0\66\0\343\0\67\0\343\0\70\0\343" +
		"\0\71\0\343\0\72\0\343\0\73\0\343\0\74\0\343\0\75\0\343\0\76\0\343\0\77\0\343\0\100" +
		"\0\343\0\101\0\343\0\102\0\343\0\103\0\343\0\104\0\343\0\105\0\343\0\106\0\343\0" +
		"\107\0\343\0\110\0\343\0\111\0\343\0\112\0\343\0\113\0\343\0\114\0\343\0\115\0\343" +
		"\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0\0\341\0\6\0\341\0\7\0\341\0\23\0\341" +
		"\0\27\0\341\0\30\0\341\0\44\0\341\0\45\0\341\0\56\0\341\0\57\0\341\0\60\0\341\0\61" +
		"\0\341\0\62\0\341\0\63\0\341\0\64\0\341\0\65\0\341\0\66\0\341\0\67\0\341\0\70\0\341" +
		"\0\71\0\341\0\72\0\341\0\73\0\341\0\74\0\341\0\75\0\341\0\76\0\341\0\77\0\341\0\100" +
		"\0\341\0\101\0\341\0\102\0\341\0\103\0\341\0\104\0\341\0\105\0\341\0\106\0\341\0" +
		"\107\0\341\0\110\0\341\0\111\0\341\0\112\0\341\0\113\0\341\0\114\0\341\0\115\0\341" +
		"\0\uffff\uffff\ufffe\uffff\13\0\uffff\uffff\14\0\uffff\uffff\11\0\277\0\22\0\277" +
		"\0\41\0\277\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\51\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff" +
		"\120\0\uffff\uffff\6\0\170\0\10\0\170\0\15\0\170\0\26\0\170\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\26\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff" +
		"\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff" +
		"\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105" +
		"\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff" +
		"\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0" +
		"\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff" +
		"\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff" +
		"\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff" +
		"\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\25" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\15\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\10\0\353\0\15\0\353" +
		"\0\26\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff" +
		"\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111" +
		"\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff" +
		"\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100" +
		"\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff" +
		"\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff" +
		"\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff" +
		"\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff" +
		"\115\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff\40\0\uffff\uffff" +
		"\6\0\172\0\10\0\172\0\15\0\172\0\16\0\172\0\23\0\172\0\24\0\172\0\25\0\172\0\26\0" +
		"\172\0\42\0\172\0\43\0\172\0\44\0\172\0\45\0\172\0\51\0\172\0\54\0\172\0\56\0\172" +
		"\0\57\0\172\0\60\0\172\0\61\0\172\0\62\0\172\0\63\0\172\0\64\0\172\0\65\0\172\0\66" +
		"\0\172\0\67\0\172\0\70\0\172\0\71\0\172\0\72\0\172\0\73\0\172\0\74\0\172\0\75\0\172" +
		"\0\76\0\172\0\77\0\172\0\100\0\172\0\101\0\172\0\102\0\172\0\103\0\172\0\104\0\172" +
		"\0\105\0\172\0\106\0\172\0\107\0\172\0\110\0\172\0\111\0\172\0\112\0\172\0\113\0" +
		"\172\0\114\0\172\0\115\0\172\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\17\0\257" +
		"\0\31\0\257\0\uffff\uffff\ufffe\uffff\55\0\uffff\uffff\20\0\351\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff" +
		"\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110" +
		"\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff" +
		"\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77" +
		"\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff" +
		"\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff" +
		"\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff" +
		"\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\353\0\15\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff" +
		"\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106" +
		"\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff" +
		"\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0" +
		"\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0\15\0\353\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff" +
		"\24\0\uffff\uffff\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff" +
		"\uffff\110\0\uffff\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104" +
		"\0\uffff\uffff\103\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff" +
		"\uffff\77\0\uffff\uffff\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff" +
		"\uffff\72\0\uffff\uffff\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff" +
		"\uffff\65\0\uffff\uffff\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff" +
		"\uffff\60\0\uffff\uffff\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0" +
		"\uffff\uffff\10\0\353\0\15\0\353\0\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\0\0" +
		"\345\0\6\0\345\0\7\0\345\0\27\0\345\0\30\0\345\0\44\0\345\0\45\0\345\0\56\0\345\0" +
		"\57\0\345\0\60\0\345\0\61\0\345\0\62\0\345\0\63\0\345\0\64\0\345\0\65\0\345\0\66" +
		"\0\345\0\67\0\345\0\70\0\345\0\71\0\345\0\72\0\345\0\73\0\345\0\74\0\345\0\75\0\345" +
		"\0\76\0\345\0\77\0\345\0\100\0\345\0\101\0\345\0\102\0\345\0\103\0\345\0\104\0\345" +
		"\0\105\0\345\0\106\0\345\0\107\0\345\0\110\0\345\0\111\0\345\0\112\0\345\0\113\0" +
		"\345\0\114\0\345\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0\343\0\6\0\343\0\7" +
		"\0\343\0\27\0\343\0\30\0\343\0\44\0\343\0\45\0\343\0\56\0\343\0\57\0\343\0\60\0\343" +
		"\0\61\0\343\0\62\0\343\0\63\0\343\0\64\0\343\0\65\0\343\0\66\0\343\0\67\0\343\0\70" +
		"\0\343\0\71\0\343\0\72\0\343\0\73\0\343\0\74\0\343\0\75\0\343\0\76\0\343\0\77\0\343" +
		"\0\100\0\343\0\101\0\343\0\102\0\343\0\103\0\343\0\104\0\343\0\105\0\343\0\106\0" +
		"\343\0\107\0\343\0\110\0\343\0\111\0\343\0\112\0\343\0\113\0\343\0\114\0\343\0\115" +
		"\0\343\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff\uffff" +
		"\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107\0\uffff" +
		"\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff\uffff\102" +
		"\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76\0\uffff\uffff" +
		"\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff\71\0\uffff\uffff" +
		"\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff\64\0\uffff\uffff" +
		"\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff\57\0\uffff\uffff" +
		"\56\0\uffff\uffff\10\0\21\0\26\0\21\0\40\0\21\0\uffff\uffff\ufffe\uffff\32\0\uffff" +
		"\uffff\33\0\uffff\uffff\6\0\223\0\10\0\223\0\15\0\223\0\16\0\223\0\23\0\223\0\24" +
		"\0\223\0\25\0\223\0\26\0\223\0\35\0\223\0\40\0\223\0\42\0\223\0\43\0\223\0\44\0\223" +
		"\0\45\0\223\0\51\0\223\0\52\0\223\0\54\0\223\0\56\0\223\0\57\0\223\0\60\0\223\0\61" +
		"\0\223\0\62\0\223\0\63\0\223\0\64\0\223\0\65\0\223\0\66\0\223\0\67\0\223\0\70\0\223" +
		"\0\71\0\223\0\72\0\223\0\73\0\223\0\74\0\223\0\75\0\223\0\76\0\223\0\77\0\223\0\100" +
		"\0\223\0\101\0\223\0\102\0\223\0\103\0\223\0\104\0\223\0\105\0\223\0\106\0\223\0" +
		"\107\0\223\0\110\0\223\0\111\0\223\0\112\0\223\0\113\0\223\0\114\0\223\0\115\0\223" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0\353\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\25\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\114\0\uffff\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff" +
		"\uffff\107\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103" +
		"\0\uffff\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff" +
		"\76\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\15\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\10\0\353\0\15\0\353" +
		"\0\26\0\353\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\10\0\353\0\15\0\353\0\26\0" +
		"\353\0\uffff\uffff\ufffe\uffff\40\0\221\0\6\0\221\0\10\0\221\0\15\0\221\0\16\0\221" +
		"\0\23\0\221\0\24\0\221\0\25\0\221\0\26\0\221\0\42\0\221\0\43\0\221\0\44\0\221\0\45" +
		"\0\221\0\51\0\221\0\54\0\221\0\56\0\221\0\57\0\221\0\60\0\221\0\61\0\221\0\62\0\221" +
		"\0\63\0\221\0\64\0\221\0\65\0\221\0\66\0\221\0\67\0\221\0\70\0\221\0\71\0\221\0\72" +
		"\0\221\0\73\0\221\0\74\0\221\0\75\0\221\0\76\0\221\0\77\0\221\0\100\0\221\0\101\0" +
		"\221\0\102\0\221\0\103\0\221\0\104\0\221\0\105\0\221\0\106\0\221\0\107\0\221\0\110" +
		"\0\221\0\111\0\221\0\112\0\221\0\113\0\221\0\114\0\221\0\115\0\221\0\uffff\uffff" +
		"\ufffe\uffff\17\0\uffff\uffff\20\0\107\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\25\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\114\0\uffff" +
		"\uffff\113\0\uffff\uffff\112\0\uffff\uffff\111\0\uffff\uffff\110\0\uffff\uffff\107" +
		"\0\uffff\uffff\106\0\uffff\uffff\105\0\uffff\uffff\104\0\uffff\uffff\103\0\uffff" +
		"\uffff\102\0\uffff\uffff\101\0\uffff\uffff\100\0\uffff\uffff\77\0\uffff\uffff\76" +
		"\0\uffff\uffff\75\0\uffff\uffff\74\0\uffff\uffff\73\0\uffff\uffff\72\0\uffff\uffff" +
		"\71\0\uffff\uffff\70\0\uffff\uffff\67\0\uffff\uffff\66\0\uffff\uffff\65\0\uffff\uffff" +
		"\64\0\uffff\uffff\63\0\uffff\uffff\62\0\uffff\uffff\61\0\uffff\uffff\60\0\uffff\uffff" +
		"\57\0\uffff\uffff\56\0\uffff\uffff\54\0\uffff\uffff\115\0\uffff\uffff\10\0\353\0" +
		"\15\0\353\0\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\0\0\345\0\6\0\345\0\7\0\345" +
		"\0\27\0\345\0\30\0\345\0\44\0\345\0\45\0\345\0\56\0\345\0\57\0\345\0\60\0\345\0\61" +
		"\0\345\0\62\0\345\0\63\0\345\0\64\0\345\0\65\0\345\0\66\0\345\0\67\0\345\0\70\0\345" +
		"\0\71\0\345\0\72\0\345\0\73\0\345\0\74\0\345\0\75\0\345\0\76\0\345\0\77\0\345\0\100" +
		"\0\345\0\101\0\345\0\102\0\345\0\103\0\345\0\104\0\345\0\105\0\345\0\106\0\345\0" +
		"\107\0\345\0\110\0\345\0\111\0\345\0\112\0\345\0\113\0\345\0\114\0\345\0\uffff\uffff" +
		"\ufffe\uffff\11\0\304\0\41\0\uffff\uffff\22\0\304\0\uffff\uffff\ufffe\uffff\11\0" +
		"\303\0\41\0\303\0\22\0\303\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\10\0\353\0\15" +
		"\0\353\0\26\0\353\0\uffff\uffff\ufffe\uffff\10\0\242\0\40\0\uffff\uffff\26\0\242" +
		"\0\uffff\uffff\ufffe\uffff\10\0\243\0\40\0\243\0\26\0\243\0\uffff\uffff\ufffe\uffff");

	private static final int[] lapg_sym_goto = TMLexer.unpack_int(180,
		"\0\0\2\0\21\0\41\0\41\0\41\0\41\0\102\0\104\0\111\0\114\0\124\0\125\0\126\0\156\0" +
		"\203\0\214\0\233\0\254\0\256\0\323\0\347\0\372\0\u0104\0\u0105\0\u010e\0\u0112\0" +
		"\u0116\0\u011b\0\u011c\0\u011d\0\u0122\0\u0129\0\u0131\0\u0134\0\u014d\0\u0164\0" +
		"\u017f\0\u01e3\0\u01f0\0\u01fd\0\u0203\0\u0204\0\u0205\0\u0206\0\u0222\0\u0225\0" +
		"\u028a\0\u02ef\0\u0354\0\u03bc\0\u0421\0\u0486\0\u04ee\0\u0553\0\u05b8\0\u061d\0" +
		"\u0682\0\u06e7\0\u074c\0\u07b1\0\u0816\0\u087b\0\u08e0\0\u0945\0\u09ab\0\u0a11\0" +
		"\u0a76\0\u0adb\0\u0b45\0\u0bad\0\u0c15\0\u0c7a\0\u0cdf\0\u0d44\0\u0daa\0\u0e0f\0" +
		"\u0e74\0\u0e90\0\u0e91\0\u0e94\0\u0e95\0\u0e96\0\u0e97\0\u0e98\0\u0e99\0\u0e9a\0" +
		"\u0e9b\0\u0e9d\0\u0e9e\0\u0e9f\0\u0ed4\0\u0efa\0\u0f0e\0\u0f14\0\u0f17\0\u0f19\0" +
		"\u0f1d\0\u0f21\0\u0f25\0\u0f29\0\u0f2a\0\u0f2e\0\u0f30\0\u0f31\0\u0f35\0\u0f37\0" +
		"\u0f3b\0\u0f3d\0\u0f40\0\u0f41\0\u0f43\0\u0f45\0\u0f49\0\u0f4c\0\u0f4d\0\u0f4e\0" +
		"\u0f50\0\u0f52\0\u0f53\0\u0f55\0\u0f57\0\u0f58\0\u0f62\0\u0f6c\0\u0f77\0\u0f82\0" +
		"\u0f8e\0\u0fa9\0\u0fbc\0\u0fca\0\u0fde\0\u0fdf\0\u0ff3\0\u0ff5\0\u1009\0\u101d\0" +
		"\u1033\0\u104b\0\u1063\0\u1077\0\u108f\0\u10a8\0\u10c4\0\u10c9\0\u10cd\0\u10e3\0" +
		"\u10f9\0\u1110\0\u1111\0\u1113\0\u1115\0\u111f\0\u1120\0\u1121\0\u1124\0\u1126\0" +
		"\u1128\0\u112c\0\u112f\0\u1132\0\u1138\0\u1139\0\u113a\0\u113b\0\u113c\0\u113e\0" +
		"\u114b\0\u114e\0\u1151\0\u1167\0\u1182\0\u1184\0\u1186\0\u1188\0\u118a\0\u118c\0" +
		"\u118f\0\u1192\0\u11ad\0");

	private static final int[] lapg_sym_from = TMLexer.unpack_int(4525,
		"\u01a6\0\u01a7\0\1\0\6\0\36\0\41\0\61\0\72\0\106\0\116\0\274\0\375\0\u0124\0\u0140" +
		"\0\u0146\0\u0147\0\u016a\0\1\0\6\0\41\0\55\0\72\0\106\0\116\0\257\0\274\0\363\0\375" +
		"\0\u0124\0\u0140\0\u0146\0\u0147\0\u016a\0\105\0\130\0\141\0\162\0\167\0\212\0\234" +
		"\0\300\0\314\0\315\0\317\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115" +
		"\0\u0116\0\u0117\0\u0119\0\u011a\0\u011e\0\u0133\0\u0135\0\u0136\0\u015d\0\u015e" +
		"\0\u0161\0\u0164\0\u0175\0\u0190\0\37\0\64\0\311\0\u0157\0\u0189\0\u019f\0\u01a0" +
		"\0\u0106\0\u0183\0\u0184\0\63\0\126\0\232\0\265\0\273\0\307\0\374\0\u0129\0\u0104" +
		"\0\u0104\0\34\0\60\0\104\0\121\0\201\0\203\0\252\0\263\0\271\0\273\0\312\0\370\0" +
		"\371\0\374\0\u0131\0\u0132\0\u0134\0\u013d\0\u0142\0\u0171\0\u0173\0\u0174\0\u017f" +
		"\0\u0199\0\21\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113" +
		"\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\24\0\50" +
		"\0\76\0\152\0\201\0\203\0\263\0\341\0\u016e\0\47\0\75\0\157\0\166\0\213\0\244\0\245" +
		"\0\251\0\307\0\334\0\353\0\355\0\356\0\u0137\0\u0158\0\1\0\6\0\41\0\106\0\116\0\234" +
		"\0\274\0\300\0\351\0\352\0\354\0\u010e\0\u0113\0\u0133\0\u0135\0\u0136\0\u0175\0" +
		"\25\0\u0106\0\20\0\30\0\32\0\234\0\300\0\303\0\305\0\314\0\315\0\320\0\334\0\351" +
		"\0\352\0\354\0\362\0\u0108\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115" +
		"\0\u0117\0\u011a\0\u011b\0\u0122\0\u0133\0\u0135\0\u0136\0\u013b\0\u0152\0\u0153" +
		"\0\u015e\0\u0175\0\u018c\0\u018e\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0\354" +
		"\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136" +
		"\0\u015e\0\u0175\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108\0\u010e" +
		"\0\u0113\0\u0115\0\u0117\0\u011a\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\54\0\77" +
		"\0\102\0\u0107\0\u010b\0\u0151\0\u0157\0\u017a\0\u0185\0\u0189\0\212\0\10\0\105\0" +
		"\130\0\162\0\166\0\212\0\251\0\304\0\u0154\0\51\0\150\0\152\0\341\0\125\0\332\0\u015a" +
		"\0\u019d\0\26\0\73\0\332\0\u015a\0\u019d\0\307\0\327\0\277\0\301\0\u0148\0\u014a" +
		"\0\u014f\0\26\0\73\0\u010f\0\u0152\0\u0153\0\u018c\0\u018e\0\321\0\u010b\0\u011f" +
		"\0\u0157\0\u0167\0\u0189\0\u019f\0\u01a0\0\u0106\0\u0183\0\u0184\0\234\0\300\0\314" +
		"\0\315\0\320\0\334\0\351\0\352\0\354\0\u0108\0\u010e\0\u0110\0\u0111\0\u0112\0\u0113" +
		"\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175" +
		"\0\141\0\167\0\174\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108\0\u010e" +
		"\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175" +
		"\0\1\0\6\0\37\0\41\0\106\0\116\0\130\0\165\0\167\0\212\0\234\0\274\0\300\0\320\0" +
		"\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0117\0\u011a\0\u0133\0\u0135\0\u0136" +
		"\0\u015e\0\u0175\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162" +
		"\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0" +
		"\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320" +
		"\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108" +
		"\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a" +
		"\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148" +
		"\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175" +
		"\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\6\0\41\0\72\0\106\0\116\0\274\0\375\0\u0124" +
		"\0\u0140\0\u0146\0\u0147\0\u016a\0\1\0\6\0\41\0\72\0\106\0\116\0\274\0\375\0\u0124" +
		"\0\u0140\0\u0146\0\u0147\0\u016a\0\1\0\6\0\41\0\106\0\116\0\274\0\u0108\0\331\0\22" +
		"\0\234\0\266\0\267\0\300\0\314\0\315\0\320\0\334\0\351\0\352\0\354\0\367\0\u0108" +
		"\0\u010e\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122" +
		"\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\347\0\350\0\u012c\0\1\0\2\0\6\0\13\0\26" +
		"\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\124" +
		"\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175\0\177\0\205\0" +
		"\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274" +
		"\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0" +
		"\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110" +
		"\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a" +
		"\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150" +
		"\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\124\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167" +
		"\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0" +
		"\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340" +
		"\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e" +
		"\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122" +
		"\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c" +
		"\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c" +
		"\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\116\0\117\0\124\0\125\0\130\0\134\0\141\0\145\0\146\0\147" +
		"\0\162\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0" +
		"\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315" +
		"\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0" +
		"\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0" +
		"\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0" +
		"\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0" +
		"\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0" +
		"\145\0\146\0\147\0\162\0\165\0\166\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227" +
		"\0\231\0\234\0\235\0\237\0\240\0\241\0\245\0\251\0\254\0\271\0\272\0\274\0\276\0" +
		"\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\356" +
		"\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111" +
		"\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d" +
		"\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152" +
		"\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175\0\177\0" +
		"\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272" +
		"\0\274\0\275\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0" +
		"\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f" +
		"\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124" +
		"\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f" +
		"\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e" +
		"\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0" +
		"\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241" +
		"\0\254\0\271\0\272\0\274\0\275\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0" +
		"\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0" +
		"\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0" +
		"\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0" +
		"\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0" +
		"\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43" +
		"\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146" +
		"\0\147\0\162\0\165\0\166\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0" +
		"\234\0\235\0\237\0\240\0\241\0\245\0\251\0\254\0\271\0\272\0\274\0\276\0\277\0\300" +
		"\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\356\0\366\0" +
		"\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112" +
		"\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133" +
		"\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153" +
		"\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13" +
		"\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0" +
		"\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\164\0\165\0\167\0\175\0\177\0\205" +
		"\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0" +
		"\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352" +
		"\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110" +
		"\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a" +
		"\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150" +
		"\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\164\0\165\0\167" +
		"\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0" +
		"\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340" +
		"\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e" +
		"\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122" +
		"\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c" +
		"\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c" +
		"\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162" +
		"\0\164\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0" +
		"\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315" +
		"\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0" +
		"\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0" +
		"\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0" +
		"\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0" +
		"\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0" +
		"\145\0\146\0\147\0\162\0\164\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227" +
		"\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0" +
		"\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100" +
		"\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113" +
		"\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135" +
		"\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154" +
		"\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0" +
		"\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0" +
		"\130\0\134\0\141\0\145\0\146\0\147\0\162\0\164\0\165\0\167\0\175\0\177\0\205\0\212" +
		"\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0" +
		"\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354" +
		"\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111" +
		"\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d" +
		"\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152" +
		"\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\164\0\165\0\167\0\175\0" +
		"\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271" +
		"\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0" +
		"\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f" +
		"\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124" +
		"\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f" +
		"\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e" +
		"\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73" +
		"\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0" +
		"\167\0\175\0\177\0\205\0\212\0\214\0\220\0\221\0\227\0\231\0\234\0\235\0\237\0\240" +
		"\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0" +
		"\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0" +
		"\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0" +
		"\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0" +
		"\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0" +
		"\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43" +
		"\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146" +
		"\0\147\0\162\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\221\0\227\0\231\0" +
		"\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302" +
		"\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101" +
		"\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115" +
		"\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136" +
		"\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e" +
		"\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35" +
		"\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134" +
		"\0\141\0\145\0\146\0\147\0\162\0\164\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0" +
		"\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277" +
		"\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0" +
		"\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112" +
		"\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133" +
		"\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153" +
		"\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13" +
		"\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0" +
		"\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\164\0\165\0\167\0\175\0\177\0\205" +
		"\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0" +
		"\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352" +
		"\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110" +
		"\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a" +
		"\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150" +
		"\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\164\0\165\0\167" +
		"\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0" +
		"\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340" +
		"\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e" +
		"\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122" +
		"\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c" +
		"\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c" +
		"\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0" +
		"\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162" +
		"\0\164\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\230\0\231\0\234\0" +
		"\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314" +
		"\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103" +
		"\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117" +
		"\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140" +
		"\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a" +
		"\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37" +
		"\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0" +
		"\145\0\146\0\147\0\162\0\164\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227" +
		"\0\230\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0" +
		"\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375" +
		"\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112" +
		"\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133" +
		"\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153" +
		"\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13" +
		"\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0" +
		"\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175\0\177\0\205\0\212" +
		"\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\262\0\271\0\272\0" +
		"\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352" +
		"\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110" +
		"\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a" +
		"\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150" +
		"\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175" +
		"\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0" +
		"\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344" +
		"\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0" +
		"\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0" +
		"\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0138\0\u0140\0\u0148\0\u014a\0" +
		"\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0" +
		"\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0" +
		"\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0" +
		"\162\0\165\0\166\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235" +
		"\0\236\0\237\0\240\0\241\0\245\0\251\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0" +
		"\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\356\0\366\0\375" +
		"\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112" +
		"\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133" +
		"\0\u0135\0\u0136\0\u0138\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152" +
		"\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2" +
		"\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116" +
		"\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\166\0\167\0\175\0" +
		"\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\245\0\251" +
		"\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0" +
		"\340\0\344\0\351\0\352\0\354\0\356\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0" +
		"\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0" +
		"\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0" +
		"\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0" +
		"\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43" +
		"\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146" +
		"\0\147\0\162\0\165\0\166\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0" +
		"\234\0\235\0\237\0\240\0\241\0\245\0\251\0\254\0\271\0\272\0\274\0\276\0\277\0\300" +
		"\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\356\0\366\0" +
		"\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112" +
		"\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133" +
		"\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153" +
		"\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13" +
		"\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0" +
		"\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175\0\177\0\205\0\212" +
		"\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0" +
		"\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354" +
		"\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111" +
		"\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d" +
		"\0\u0133\0\u0135\0\u0136\0\u0138\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150" +
		"\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175" +
		"\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0" +
		"\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344" +
		"\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0" +
		"\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0" +
		"\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0138\0\u0140\0\u0148\0\u014a\0" +
		"\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0" +
		"\u018c\0\u018e\0\u0195\0\0\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0" +
		"\44\0\53\0\72\0\73\0\105\0\106\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0" +
		"\147\0\162\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235" +
		"\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0" +
		"\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103" +
		"\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117" +
		"\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140" +
		"\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a" +
		"\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0\20\0\26\0\31\0\35\0\36" +
		"\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\101\0\105\0\106\0\116\0\117\0\125\0\130\0\134" +
		"\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175\0\177\0\205\0\212\0\214\0\220\0" +
		"\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274\0\276\0\277\0\300" +
		"\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354\0\366\0\375\0" +
		"\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0" +
		"\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a\0\u012d\0\u0133\0" +
		"\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150\0\u0152\0\u0153\0" +
		"\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195\0\1\0\2\0\6\0\13\0" +
		"\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\62\0\72\0\73\0\105\0\106\0\116\0\117" +
		"\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167\0\175\0\177\0\205\0" +
		"\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0\254\0\271\0\272\0\274" +
		"\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0" +
		"\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e\0\u010f\0\u0110" +
		"\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0124\0\u012a" +
		"\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c\0\u014f\0\u0150" +
		"\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c\0\u018e\0\u0195" +
		"\0\1\0\2\0\6\0\13\0\26\0\31\0\35\0\36\0\37\0\41\0\43\0\44\0\53\0\72\0\73\0\105\0" +
		"\106\0\107\0\116\0\117\0\125\0\130\0\134\0\141\0\145\0\146\0\147\0\162\0\165\0\167" +
		"\0\175\0\177\0\205\0\212\0\214\0\220\0\227\0\231\0\234\0\235\0\237\0\240\0\241\0" +
		"\254\0\271\0\272\0\274\0\276\0\277\0\300\0\301\0\302\0\314\0\315\0\320\0\334\0\340" +
		"\0\344\0\351\0\352\0\354\0\366\0\375\0\u0100\0\u0101\0\u0103\0\u0108\0\u0109\0\u010e" +
		"\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122" +
		"\0\u0124\0\u012a\0\u012d\0\u0133\0\u0135\0\u0136\0\u0140\0\u0148\0\u014a\0\u014c" +
		"\0\u014f\0\u0150\0\u0152\0\u0153\0\u0154\0\u015e\0\u016a\0\u0175\0\u0185\0\u018c" +
		"\0\u018e\0\u0195\0\127\0\163\0\166\0\234\0\245\0\251\0\300\0\314\0\315\0\320\0\351" +
		"\0\352\0\354\0\356\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133" +
		"\0\u0135\0\u0136\0\u013a\0\u015e\0\u0175\0\u017d\0\134\0\154\0\211\0\261\0\u010d" +
		"\0\3\0\0\0\22\0\0\0\37\0\64\0\20\0\101\0\22\0\37\0\26\0\43\0\44\0\73\0\105\0\130" +
		"\0\134\0\141\0\146\0\147\0\162\0\167\0\175\0\212\0\231\0\234\0\235\0\240\0\241\0" +
		"\254\0\272\0\277\0\300\0\302\0\314\0\315\0\320\0\334\0\340\0\344\0\351\0\352\0\354" +
		"\0\u0103\0\u0108\0\u010e\0\u0110\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b" +
		"\0\u0122\0\u012a\0\u0133\0\u0135\0\u0136\0\u0148\0\u014a\0\u0150\0\u015e\0\u0175" +
		"\0\1\0\6\0\41\0\106\0\116\0\234\0\274\0\300\0\314\0\315\0\320\0\334\0\351\0\352\0" +
		"\354\0\u0108\0\u010e\0\u010f\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a" +
		"\0\u011b\0\u0122\0\u0124\0\u0133\0\u0135\0\u0136\0\u0152\0\u0153\0\u0154\0\u015e" +
		"\0\u0175\0\u018c\0\u018e\0\72\0\145\0\177\0\214\0\227\0\237\0\271\0\301\0\366\0\375" +
		"\0\u0100\0\u0101\0\u0109\0\u012d\0\u0140\0\u014c\0\u014f\0\u016a\0\u0185\0\u0195" +
		"\0\127\0\163\0\166\0\245\0\251\0\356\0\154\0\211\0\261\0\105\0\162\0\105\0\130\0" +
		"\162\0\212\0\105\0\130\0\162\0\212\0\105\0\130\0\162\0\212\0\105\0\130\0\162\0\212" +
		"\0\125\0\105\0\130\0\162\0\212\0\362\0\u013b\0\u0138\0\105\0\130\0\162\0\212\0\146" +
		"\0\147\0\105\0\130\0\162\0\212\0\125\0\205\0\146\0\147\0\254\0\141\0\141\0\167\0" +
		"\141\0\167\0\166\0\245\0\251\0\356\0\347\0\350\0\u012c\0\164\0\164\0\141\0\167\0" +
		"\141\0\167\0\214\0\214\0\366\0\227\0\u014c\0\u012d\0\234\0\300\0\351\0\352\0\354" +
		"\0\u010e\0\u0133\0\u0135\0\u0136\0\u0175\0\234\0\300\0\351\0\352\0\354\0\u010e\0" +
		"\u0133\0\u0135\0\u0136\0\u0175\0\234\0\300\0\351\0\352\0\354\0\u010e\0\u0113\0\u0133" +
		"\0\u0135\0\u0136\0\u0175\0\234\0\300\0\351\0\352\0\354\0\u010e\0\u0113\0\u0133\0" +
		"\u0135\0\u0136\0\u0175\0\234\0\300\0\314\0\351\0\352\0\354\0\u010e\0\u0113\0\u0133" +
		"\0\u0135\0\u0136\0\u0175\0\234\0\300\0\314\0\315\0\317\0\320\0\351\0\352\0\354\0" +
		"\u0108\0\u010e\0\u0113\0\u0115\0\u0116\0\u0117\0\u0119\0\u011a\0\u011e\0\u0133\0" +
		"\u0135\0\u0136\0\u015d\0\u015e\0\u0161\0\u0164\0\u0175\0\u0190\0\234\0\300\0\314" +
		"\0\315\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0" +
		"\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\234\0\300\0\314\0\315\0\351\0\352\0\354" +
		"\0\u010e\0\u0113\0\u0115\0\u0133\0\u0135\0\u0136\0\u0175\0\234\0\300\0\314\0\315" +
		"\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122" +
		"\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\301\0\234\0\300\0\314\0\315\0\320\0\351" +
		"\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135" +
		"\0\u0136\0\u015e\0\u0175\0\301\0\u014f\0\234\0\300\0\314\0\315\0\320\0\351\0\352" +
		"\0\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0" +
		"\u0136\0\u015e\0\u0175\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108\0" +
		"\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0" +
		"\u0175\0\234\0\300\0\314\0\315\0\320\0\334\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113" +
		"\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175" +
		"\0\234\0\300\0\314\0\315\0\320\0\334\0\351\0\352\0\354\0\u0108\0\u010e\0\u0110\0" +
		"\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0133\0\u0135\0\u0136\0" +
		"\u015e\0\u0175\0\234\0\300\0\314\0\315\0\320\0\334\0\351\0\352\0\354\0\u0108\0\u010e" +
		"\0\u0110\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0133\0\u0135" +
		"\0\u0136\0\u015e\0\u0175\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108" +
		"\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e" +
		"\0\u0175\0\234\0\300\0\314\0\315\0\320\0\334\0\351\0\352\0\354\0\u0108\0\u010e\0" +
		"\u0110\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0133\0\u0135\0" +
		"\u0136\0\u015e\0\u0175\0\234\0\300\0\314\0\315\0\320\0\334\0\351\0\352\0\354\0\u0108" +
		"\0\u010e\0\u0110\0\u0111\0\u0112\0\u0113\0\u0115\0\u0117\0\u011a\0\u011b\0\u0122" +
		"\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\234\0\266\0\267\0\300\0\314\0\315\0\320" +
		"\0\334\0\351\0\352\0\354\0\367\0\u0108\0\u010e\0\u0110\0\u0111\0\u0112\0\u0113\0" +
		"\u0115\0\u0117\0\u011a\0\u011b\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0" +
		"\u010f\0\u0152\0\u0153\0\u018c\0\u018e\0\u010f\0\u0152\0\u018c\0\u018e\0\141\0\167" +
		"\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115" +
		"\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\141\0\167\0\234" +
		"\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117" +
		"\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\141\0\167\0\174\0\234" +
		"\0\300\0\314\0\315\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117" +
		"\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\235\0\166\0\251\0\235" +
		"\0\u012a\0\26\0\43\0\44\0\73\0\235\0\277\0\u0103\0\u012a\0\u0148\0\u014a\0\26\0\26" +
		"\0\10\0\304\0\u0154\0\26\0\73\0\164\0\230\0\72\0\375\0\u0140\0\u016a\0\277\0\u0148" +
		"\0\u014a\0\277\0\u0148\0\u014a\0\1\0\6\0\41\0\106\0\116\0\274\0\6\0\6\0\53\0\53\0" +
		"\53\0\117\0\1\0\6\0\41\0\72\0\106\0\116\0\274\0\375\0\u0124\0\u0140\0\u0146\0\u0147" +
		"\0\u016a\0\2\0\13\0\31\0\2\0\13\0\31\0\234\0\300\0\314\0\315\0\320\0\351\0\352\0" +
		"\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0117\0\u011a\0\u0122\0\u0133\0\u0135\0\u0136" +
		"\0\u013a\0\u015e\0\u0175\0\u017d\0\1\0\6\0\37\0\41\0\106\0\116\0\130\0\165\0\167" +
		"\0\212\0\234\0\274\0\300\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0117" +
		"\0\u011a\0\u0133\0\u0135\0\u0136\0\u015e\0\u0175\0\20\0\101\0\127\0\163\0\257\0\363" +
		"\0\362\0\u013b\0\u013a\0\u017d\0\240\0\241\0\344\0\347\0\350\0\u012c\0\234\0\300" +
		"\0\314\0\315\0\317\0\320\0\351\0\352\0\354\0\u0108\0\u010e\0\u0113\0\u0115\0\u0116" +
		"\0\u0117\0\u0119\0\u011a\0\u011e\0\u0133\0\u0135\0\u0136\0\u015d\0\u015e\0\u0161" +
		"\0\u0164\0\u0175\0\u0190\0");

	private static final int[] lapg_sym_to = TMLexer.unpack_int(4525,
		"\u01a8\0\u01a9\0\4\0\4\0\60\0\4\0\104\0\4\0\4\0\4\0\4\0\4\0\4\0\4\0\4\0\4\0\4\0\5" +
		"\0\5\0\5\0\102\0\5\0\5\0\5\0\361\0\5\0\361\0\5\0\5\0\5\0\5\0\5\0\5\0\124\0\124\0" +
		"\164\0\124\0\164\0\124\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275" +
		"\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0\275\0" +
		"\275\0\275\0\275\0\275\0\62\0\107\0\u0113\0\u018c\0\u018c\0\u018c\0\u018c\0\u0148" +
		"\0\u0148\0\u0148\0\106\0\154\0\274\0\367\0\375\0\u0110\0\u0140\0\u016a\0\u0146\0" +
		"\u0147\0\56\0\103\0\123\0\144\0\253\0\255\0\357\0\365\0\372\0\376\0\u0114\0\u013e" +
		"\0\u013f\0\u0141\0\u016f\0\u0170\0\u0172\0\u017e\0\u0180\0\u0196\0\u0197\0\u0198" +
		"\0\u019c\0\u01a3\0\35\0\276\0\276\0\276\0\276\0\276\0\276\0\276\0\276\0\276\0\276" +
		"\0\276\0\276\0\276\0\276\0\276\0\276\0\276\0\276\0\276\0\276\0\41\0\73\0\117\0\205" +
		"\0\254\0\254\0\366\0\u012a\0\u0195\0\72\0\116\0\211\0\234\0\261\0\351\0\352\0\354" +
		"\0\u0111\0\u0127\0\u0133\0\u0135\0\u0136\0\u0175\0\u0111\0\6\0\6\0\6\0\6\0\6\0\277" +
		"\0\6\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\277\0\42\0\u0149\0\31" +
		"\0\53\0\55\0\300\0\300\0\u010e\0\u010f\0\300\0\300\0\300\0\300\0\300\0\300\0\300" +
		"\0\u0138\0\300\0\300\0\u0152\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300\0\300" +
		"\0\300\0\300\0\300\0\u0138\0\u0152\0\u0152\0\300\0\300\0\u0152\0\u0152\0\301\0\301" +
		"\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0\301\0" +
		"\301\0\301\0\301\0\301\0\301\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302" +
		"\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\302\0\101\0\120\0\122\0" +
		"\u014b\0\u014e\0\u0188\0\u018d\0\u019a\0\u019d\0\u019e\0\260\0\26\0\125\0\125\0\125" +
		"\0\235\0\125\0\235\0\26\0\26\0\74\0\204\0\206\0\u012b\0\150\0\u0125\0\u0125\0\u01a4" +
		"\0\43\0\43\0\u0126\0\u0126\0\u01a5\0\u0112\0\u0123\0\u0103\0\u0109\0\u0103\0\u0103" +
		"\0\u0109\0\44\0\44\0\u0153\0\u0153\0\u0153\0\u0153\0\u0153\0\u0122\0\u014f\0\u0122" +
		"\0\u018e\0\u0122\0\u018e\0\u018e\0\u018e\0\u014a\0\u014a\0\u014a\0\303\0\303\0\303" +
		"\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0" +
		"\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\303\0\165\0\165\0\165\0\165\0\165" +
		"\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0\165\0" +
		"\165\0\165\0\165\0\165\0\165\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7" +
		"\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\7\0\10\0\17\0\10\0\17\0\45" +
		"\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0" +
		"\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265" +
		"\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0" +
		"\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0" +
		"\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304" +
		"\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154" +
		"\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\11\0\11\0\11\0\11\0" +
		"\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\11\0\12\0\12\0\12\0\12\0\12\0\12\0\12\0" +
		"\12\0\12\0\12\0\12\0\12\0\12\0\13\0\13\0\13\0\13\0\13\0\13\0\u014c\0\u0124\0\36\0" +
		"\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305" +
		"\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0\305\0" +
		"\305\0\u012d\0\u012d\0\u012d\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0" +
		"\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\145\0\151\0\126\0\45\0\45\0\111\0" +
		"\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340" +
		"\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0" +
		"\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0" +
		"\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111" +
		"\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0" +
		"\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0" +
		"\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\146\0\151\0\126\0\45\0" +
		"\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45" +
		"\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0" +
		"\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0" +
		"\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10" +
		"\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0" +
		"\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0" +
		"\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\147\0\151\0\126" +
		"\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0" +
		"\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111" +
		"\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111" +
		"\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0" +
		"\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154" +
		"\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151" +
		"\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\236\0\45\0\45\0\111\0\151\0\126\0" +
		"\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\236\0\236\0\45\0\111\0\45\0\10" +
		"\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304" +
		"\0\236\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0" +
		"\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45" +
		"\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154" +
		"\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45" +
		"\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111" +
		"\0\45\0\10\0\u0100\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45" +
		"\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0" +
		"\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304" +
		"\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0" +
		"\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45" +
		"\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126" +
		"\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45" +
		"\0\45\0\45\0\111\0\45\0\10\0\u0101\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304" +
		"\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304" +
		"\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0" +
		"\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111" +
		"\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0" +
		"\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0" +
		"\45\0\45\0\126\0\232\0\237\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304" +
		"\0\340\0\111\0\45\0\45\0\237\0\237\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111" +
		"\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\237\0\111\0\111\0\111" +
		"\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0" +
		"\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0" +
		"\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10" +
		"\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75" +
		"\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\214\0\232\0\45\0\45\0\111\0\151\0" +
		"\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102" +
		"\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111" +
		"\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0" +
		"\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0" +
		"\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10" +
		"\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10" +
		"\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\215\0\232\0\45\0\45\0\111" +
		"\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0" +
		"\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0" +
		"\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304" +
		"\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45" +
		"\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0" +
		"\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45" +
		"\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\216\0\232\0\45" +
		"\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0" +
		"\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0" +
		"\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10" +
		"\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111" +
		"\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154" +
		"\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\217" +
		"\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45" +
		"\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304" +
		"\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154" +
		"\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304" +
		"\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0" +
		"\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45" +
		"\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45" +
		"\0\126\0\220\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340" +
		"\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0" +
		"\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0" +
		"\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111" +
		"\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0" +
		"\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0" +
		"\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111" +
		"\0\45\0\45\0\126\0\221\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0" +
		"\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304" +
		"\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304" +
		"\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0" +
		"\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10" +
		"\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57" +
		"\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0" +
		"\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\266\0\111" +
		"\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0" +
		"\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0" +
		"\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304" +
		"\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154" +
		"\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17" +
		"\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0" +
		"\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\267" +
		"\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0" +
		"\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0" +
		"\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304" +
		"\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154" +
		"\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151" +
		"\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\222\0\232\0\45\0\45\0\111\0\151\0\126\0" +
		"\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0" +
		"\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0" +
		"\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304" +
		"\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111" +
		"\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17" +
		"\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10" +
		"\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\223\0\232\0\45\0\45\0\111\0\151" +
		"\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0" +
		"\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304" +
		"\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0" +
		"\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0" +
		"\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111" +
		"\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126" +
		"\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\224\0\232\0\45\0\45" +
		"\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111" +
		"\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304" +
		"\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0" +
		"\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111" +
		"\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154" +
		"\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\225" +
		"\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\225\0\45\0\304\0\340\0\111" +
		"\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0" +
		"\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0" +
		"\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304" +
		"\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0" +
		"\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10" +
		"\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45" +
		"\0\45\0\126\0\226\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\226\0\45" +
		"\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0" +
		"\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0" +
		"\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10" +
		"\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0" +
		"\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0" +
		"\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45" +
		"\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0" +
		"\45\0\304\0\340\0\111\0\45\0\45\0\45\0\364\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111" +
		"\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111" +
		"\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0" +
		"\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154" +
		"\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0" +
		"\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151" +
		"\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0" +
		"\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304" +
		"\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111" +
		"\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0" +
		"\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\u0176\0\111\0\45\0\45\0\111\0\111" +
		"\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17" +
		"\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10" +
		"\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\240\0\45\0\45\0\111\0\151" +
		"\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\344\0\111\0\45\0\45\0\240\0\240\0\45" +
		"\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45" +
		"\0\304\0\304\0\304\0\240\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0" +
		"\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304" +
		"\0\304\0\u0177\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111" +
		"\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0" +
		"\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0" +
		"\45\0\45\0\126\0\232\0\241\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304" +
		"\0\340\0\111\0\45\0\45\0\241\0\241\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111" +
		"\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\241\0\111\0\111\0\111" +
		"\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0" +
		"\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0" +
		"\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10" +
		"\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75" +
		"\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\242\0\45\0\45\0\111\0\151\0" +
		"\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\242\0\242\0\45\0\111\0" +
		"\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0" +
		"\304\0\304\0\242\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10" +
		"\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111" +
		"\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154" +
		"\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0" +
		"\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232" +
		"\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0" +
		"\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0" +
		"\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304" +
		"\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304" +
		"\0\u0178\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304" +
		"\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0" +
		"\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0" +
		"\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0" +
		"\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304" +
		"\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304" +
		"\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0" +
		"\304\0\304\0\304\0\u0179\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0" +
		"\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\2\0\10\0\17\0\10\0\17\0\45\0\17\0" +
		"\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126\0\10\0\10\0\75\0\151\0\126\0\45" +
		"\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0\265\0\111\0" +
		"\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45" +
		"\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111\0\111\0\45" +
		"\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0\304\0\304\0" +
		"\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154" +
		"\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10\0\17\0\32\0\45" +
		"\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\32\0\126\0\10\0\10\0\75\0\151" +
		"\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151\0\126\0\111\0" +
		"\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0\u0102\0\45\0\304" +
		"\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304\0\111\0\111\0\111" +
		"\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0\304\0\304\0\304\0" +
		"\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0\111\0\111\0\45\0" +
		"\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111\0\10\0\17\0\10" +
		"\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\105\0\111\0\45\0\126\0\10\0" +
		"\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45\0\111\0\151" +
		"\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111\0\45\0\10\0" +
		"\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304\0\304\0\304" +
		"\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0\304\0\304\0" +
		"\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111\0\45\0\45\0" +
		"\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154\0\u0154\0\111" +
		"\0\10\0\17\0\10\0\17\0\45\0\17\0\57\0\61\0\63\0\10\0\45\0\45\0\75\0\111\0\45\0\126" +
		"\0\10\0\141\0\10\0\75\0\151\0\126\0\45\0\45\0\111\0\45\0\45\0\126\0\232\0\45\0\45" +
		"\0\111\0\151\0\126\0\111\0\265\0\111\0\45\0\304\0\340\0\111\0\45\0\45\0\45\0\111" +
		"\0\45\0\10\0\u0102\0\45\0\304\0\111\0\45\0\304\0\304\0\304\0\304\0\45\0\45\0\304" +
		"\0\304\0\304\0\111\0\111\0\111\0\111\0\45\0\304\0\111\0\304\0\u0154\0\304\0\10\0" +
		"\304\0\304\0\304\0\304\0\304\0\304\0\304\0\10\0\340\0\111\0\304\0\304\0\304\0\111" +
		"\0\45\0\45\0\111\0\111\0\45\0\u0154\0\u0154\0\10\0\304\0\111\0\304\0\111\0\u0154" +
		"\0\u0154\0\111\0\155\0\155\0\155\0\306\0\155\0\155\0\306\0\306\0\306\0\306\0\306" +
		"\0\306\0\306\0\155\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0\306\0" +
		"\306\0\306\0\306\0\306\0\162\0\207\0\207\0\207\0\u0150\0\22\0\u01a6\0\37\0\3\0\64" +
		"\0\110\0\33\0\33\0\40\0\65\0\46\0\46\0\46\0\46\0\127\0\127\0\163\0\166\0\200\0\200" +
		"\0\127\0\166\0\251\0\127\0\273\0\307\0\46\0\346\0\346\0\200\0\374\0\46\0\307\0\u010d" +
		"\0\307\0\307\0\307\0\307\0\u0129\0\346\0\307\0\307\0\307\0\46\0\307\0\307\0\u0158" +
		"\0\u0158\0\307\0\307\0\307\0\307\0\307\0\307\0\46\0\307\0\307\0\307\0\46\0\46\0\u0187" +
		"\0\307\0\307\0\14\0\14\0\14\0\14\0\14\0\310\0\14\0\310\0\310\0\310\0\310\0\310\0" +
		"\310\0\310\0\310\0\310\0\310\0\u0155\0\310\0\310\0\310\0\310\0\310\0\310\0\310\0" +
		"\310\0\310\0\u0168\0\310\0\310\0\310\0\u0155\0\u0155\0\u018b\0\310\0\310\0\u0155" +
		"\0\u0155\0\112\0\177\0\252\0\262\0\270\0\345\0\373\0\u010a\0\262\0\112\0\u0143\0" +
		"\u0144\0\u014d\0\u016d\0\112\0\270\0\u010a\0\112\0\373\0\u01a2\0\156\0\156\0\243" +
		"\0\243\0\243\0\243\0\210\0\257\0\363\0\130\0\212\0\131\0\160\0\131\0\160\0\132\0" +
		"\132\0\132\0\132\0\133\0\133\0\133\0\133\0\134\0\134\0\134\0\134\0\152\0\135\0\135" +
		"\0\135\0\135\0\u0139\0\u0139\0\u017a\0\136\0\136\0\136\0\136\0\201\0\203\0\137\0" +
		"\137\0\137\0\137\0\153\0\256\0\202\0\202\0\360\0\167\0\170\0\246\0\171\0\171\0\244" +
		"\0\353\0\355\0\u0137\0\u012e\0\u012e\0\u012e\0\227\0\230\0\172\0\172\0\173\0\173" +
		"\0\263\0\264\0\u013c\0\271\0\u0185\0\u016e\0\311\0\311\0\311\0\311\0\311\0\311\0" +
		"\311\0\311\0\311\0\311\0\312\0\u0107\0\u0131\0\u0132\0\u0134\0\u0151\0\u0171\0\u0173" +
		"\0\u0174\0\u0199\0\313\0\313\0\313\0\313\0\313\0\313\0\u015c\0\313\0\313\0\313\0" +
		"\313\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\314\0\315\0\315" +
		"\0\u0115\0\315\0\315\0\315\0\315\0\315\0\315\0\315\0\315\0\315\0\316\0\316\0\316" +
		"\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0" +
		"\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\316\0\317\0\317\0\u0116" +
		"\0\u0119\0\u011e\0\317\0\317\0\317\0\u011e\0\317\0\317\0\u015d\0\u0161\0\u0164\0" +
		"\317\0\317\0\317\0\u0190\0\317\0\320\0\u0108\0\u0117\0\u011a\0\320\0\320\0\320\0" +
		"\320\0\320\0\u015e\0\320\0\320\0\320\0\320\0\321\0\321\0\321\0\321\0\u011f\0\321" +
		"\0\321\0\321\0\u011f\0\321\0\321\0\321\0\u011f\0\u011f\0\u0167\0\321\0\321\0\321" +
		"\0\u011f\0\321\0\u010b\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322" +
		"\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\322\0\u010c\0\u0186\0\323" +
		"\0\323\0\323\0\323\0\323\0\323\0\323\0\323\0\323\0\323\0\323\0\323\0\323\0\323\0" +
		"\323\0\323\0\323\0\323\0\323\0\323\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324" +
		"\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\324\0\325\0" +
		"\325\0\325\0\325\0\325\0\u0128\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0\325\0" +
		"\325\0\u0128\0\325\0\325\0\325\0\325\0\325\0\325\0\326\0\326\0\326\0\326\0\326\0" +
		"\326\0\326\0\326\0\326\0\326\0\326\0\u0159\0\u015b\0\326\0\326\0\326\0\326\0\326" +
		"\0\326\0\326\0\326\0\326\0\326\0\326\0\327\0\327\0\327\0\327\0\327\0\327\0\327\0" +
		"\327\0\327\0\327\0\327\0\327\0\327\0\327\0\327\0\327\0\327\0\327\0\327\0\327\0\327" +
		"\0\327\0\327\0\327\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0" +
		"\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\330\0\331\0\331\0\331\0\331" +
		"\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0\331\0" +
		"\331\0\331\0\331\0\331\0\331\0\331\0\331\0\332\0\332\0\332\0\332\0\332\0\332\0\332" +
		"\0\332\0\332\0\332\0\332\0\332\0\u015a\0\332\0\332\0\332\0\332\0\332\0\332\0\332" +
		"\0\332\0\332\0\332\0\332\0\332\0\333\0\370\0\371\0\333\0\333\0\333\0\333\0\333\0" +
		"\333\0\333\0\333\0\u013d\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0\333\0" +
		"\333\0\333\0\333\0\333\0\333\0\333\0\333\0\u0156\0\u0156\0\u018a\0\u0156\0\u0156" +
		"\0\u0157\0\u0189\0\u019f\0\u01a0\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174" +
		"\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0" +
		"\174\0\175\0\175\0\334\0\334\0\334\0\u011b\0\u011b\0\334\0\334\0\334\0\u011b\0\334" +
		"\0\334\0\u011b\0\u011b\0\u011b\0\u011b\0\334\0\334\0\334\0\u011b\0\334\0\176\0\176" +
		"\0\250\0\176\0\176\0\176\0\176\0\176\0\176\0\176\0\176\0\176\0\176\0\176\0\176\0" +
		"\176\0\176\0\176\0\176\0\176\0\176\0\176\0\176\0\341\0\245\0\356\0\342\0\u016b\0" +
		"\47\0\70\0\71\0\47\0\343\0\u0104\0\u0145\0\343\0\u0104\0\u0104\0\50\0\51\0\27\0\27" +
		"\0\27\0\52\0\115\0\231\0\272\0\113\0\u0142\0\u017f\0\u0194\0\u0105\0\u0105\0\u0105" +
		"\0\u0106\0\u0183\0\u0184\0\u01a7\0\23\0\67\0\140\0\142\0\377\0\24\0\25\0\76\0\77" +
		"\0\100\0\143\0\15\0\15\0\15\0\114\0\15\0\15\0\15\0\114\0\u0169\0\114\0\u0181\0\u0182" +
		"\0\114\0\20\0\30\0\54\0\21\0\21\0\21\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0" +
		"\335\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0\335\0\u017b\0\335\0" +
		"\335\0\u017b\0\16\0\16\0\66\0\16\0\16\0\16\0\161\0\233\0\247\0\161\0\336\0\16\0\336" +
		"\0\u0120\0\336\0\336\0\336\0\u0120\0\336\0\336\0\u0120\0\u0120\0\336\0\336\0\336" +
		"\0\u0120\0\336\0\34\0\121\0\157\0\213\0\362\0\u013b\0\u013a\0\u017d\0\u017c\0\u019b" +
		"\0\347\0\350\0\u012c\0\u012f\0\u0130\0\u016c\0\337\0\337\0\u0118\0\u011c\0\u011d" +
		"\0\u0121\0\337\0\337\0\337\0\u0121\0\337\0\337\0\u015f\0\u0160\0\u0162\0\u0163\0" +
		"\u0165\0\u0166\0\337\0\337\0\337\0\u018f\0\u0191\0\u0192\0\u0193\0\337\0\u01a1\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(236,
		"\2\0\0\0\5\0\4\0\2\0\0\0\7\0\4\0\3\0\3\0\4\0\4\0\3\0\3\0\1\0\1\0\2\0\1\0\1\0\1\0" +
		"\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\4\0\3\0\3\0\3\0\1\0\10\0\4\0\7\0\3\0\3\0" +
		"\1\0\1\0\1\0\1\0\5\0\3\0\1\0\4\0\4\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\7\0\6\0\6\0" +
		"\5\0\6\0\5\0\5\0\4\0\2\0\4\0\3\0\3\0\1\0\1\0\2\0\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0" +
		"\6\0\4\0\4\0\4\0\5\0\5\0\6\0\3\0\1\0\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\5\0\4\0" +
		"\4\0\3\0\4\0\3\0\3\0\2\0\4\0\3\0\3\0\2\0\3\0\2\0\2\0\1\0\1\0\3\0\2\0\3\0\3\0\4\0" +
		"\2\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\2\0\1\0\2\0\1\0\2\0\1\0\3\0\3\0" +
		"\1\0\2\0\1\0\3\0\3\0\3\0\1\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0\4\0\2\0\1\0\3\0" +
		"\2\0\1\0\3\0\3\0\2\0\1\0\1\0\4\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0\1\0\3\0\1\0\1\0" +
		"\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\3\0\1\0\1\0\5\0" +
		"\3\0\1\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0\0\0\3\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0\1\0\1\0" +
		"\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(236,
		"\121\0\121\0\122\0\122\0\123\0\123\0\124\0\124\0\125\0\126\0\127\0\130\0\130\0\131" +
		"\0\131\0\132\0\133\0\133\0\134\0\135\0\136\0\137\0\137\0\137\0\140\0\140\0\140\0" +
		"\140\0\140\0\141\0\142\0\143\0\143\0\144\0\144\0\145\0\145\0\145\0\145\0\146\0\147" +
		"\0\147\0\147\0\147\0\150\0\151\0\151\0\152\0\152\0\153\0\154\0\155\0\155\0\155\0" +
		"\156\0\156\0\156\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\157\0\160\0\160\0\160" +
		"\0\160\0\160\0\160\0\161\0\162\0\162\0\162\0\163\0\163\0\163\0\164\0\164\0\164\0" +
		"\164\0\165\0\165\0\165\0\165\0\165\0\166\0\166\0\167\0\167\0\170\0\170\0\171\0\171" +
		"\0\172\0\172\0\173\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0\174\0" +
		"\174\0\174\0\174\0\174\0\174\0\174\0\174\0\175\0\176\0\177\0\177\0\200\0\200\0\201" +
		"\0\201\0\201\0\202\0\202\0\202\0\202\0\202\0\203\0\203\0\204\0\205\0\205\0\206\0" +
		"\207\0\207\0\210\0\210\0\210\0\211\0\211\0\212\0\212\0\212\0\213\0\214\0\214\0\215" +
		"\0\215\0\215\0\215\0\215\0\215\0\215\0\215\0\216\0\217\0\217\0\217\0\217\0\220\0" +
		"\220\0\220\0\221\0\221\0\222\0\223\0\223\0\223\0\224\0\224\0\225\0\226\0\226\0\226" +
		"\0\227\0\230\0\230\0\231\0\231\0\232\0\233\0\233\0\233\0\233\0\234\0\234\0\235\0" +
		"\235\0\236\0\236\0\236\0\236\0\237\0\237\0\237\0\240\0\240\0\240\0\240\0\240\0\241" +
		"\0\241\0\242\0\242\0\243\0\243\0\244\0\244\0\245\0\246\0\246\0\246\0\246\0\247\0" +
		"\250\0\250\0\251\0\252\0\253\0\253\0\254\0\254\0\255\0\255\0\256\0\256\0\257\0\257" +
		"\0\260\0\260\0\261\0\261\0\262\0\262\0");

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
		"Ltrue",
		"Lfalse",
		"Lnew",
		"Lseparator",
		"Las",
		"Limport",
		"Lset",
		"Limplements",
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
		"implements_clauseopt",
		"rhsSuffixopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int import__optlist = 81;
		int input = 82;
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
		int implements_clauseopt = 177;
		int rhsSuffixopt = 178;
	}

	public interface Rules {
		int nonterm_type_nontermTypeAST = 65;  // nonterm_type : Lreturns symref_noargs
		int nonterm_type_nontermTypeHint = 66;  // nonterm_type : Linline Lclass identifieropt implements_clauseopt
		int nonterm_type_nontermTypeHint2 = 67;  // nonterm_type : Lclass identifieropt implements_clauseopt
		int nonterm_type_nontermTypeHint3 = 68;  // nonterm_type : Linterface identifieropt implements_clauseopt
		int nonterm_type_nontermTypeHint4 = 69;  // nonterm_type : Lvoid
		int directive_directivePrio = 82;  // directive : '%' assoc references ';'
		int directive_directiveInput = 83;  // directive : '%' Linput inputref_list_Comma_separated ';'
		int directive_directiveAssert = 84;  // directive : '%' Lassert Lempty rhsSet ';'
		int directive_directiveAssert2 = 85;  // directive : '%' Lassert Lnonempty rhsSet ';'
		int directive_directiveSet = 86;  // directive : '%' Lgenerate ID '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 141;  // rhsOptional : rhsCast '?'
		int rhsCast_rhsAsLiteral = 144;  // rhsCast : rhsClass Las literal
		int rhsPrimary_rhsSymbol = 148;  // rhsPrimary : symref
		int rhsPrimary_rhsNested = 149;  // rhsPrimary : '(' rules ')'
		int rhsPrimary_rhsList = 150;  // rhsPrimary : '(' rhsParts Lseparator references ')' '+'
		int rhsPrimary_rhsList2 = 151;  // rhsPrimary : '(' rhsParts Lseparator references ')' '*'
		int rhsPrimary_rhsQuantifier = 152;  // rhsPrimary : rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 153;  // rhsPrimary : rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 154;  // rhsPrimary : '$' '(' rules ')'
		int setPrimary_setSymbol = 157;  // setPrimary : ID symref
		int setPrimary_setSymbol2 = 158;  // setPrimary : symref
		int setPrimary_setCompound = 159;  // setPrimary : '(' setExpression ')'
		int setPrimary_setComplement = 160;  // setPrimary : '~' setPrimary
		int setExpression_setBinary = 162;  // setExpression : setExpression '|' setExpression
		int setExpression_setBinary2 = 163;  // setExpression : setExpression '&' setExpression
		int nonterm_param_inlineParameter = 174;  // nonterm_param : ID identifier '=' param_value
		int nonterm_param_inlineParameter2 = 175;  // nonterm_param : ID identifier
		int predicate_primary_boolPredicate = 190;  // predicate_primary : '!' param_ref
		int predicate_primary_boolPredicate2 = 191;  // predicate_primary : param_ref
		int predicate_primary_comparePredicate = 192;  // predicate_primary : param_ref '==' literal
		int predicate_primary_comparePredicate2 = 193;  // predicate_primary : param_ref '!=' literal
		int predicate_expression_predicateBinary = 195;  // predicate_expression : predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 196;  // predicate_expression : predicate_expression '||' predicate_expression
		int expression_instance = 199;  // expression : Lnew name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 200;  // expression : '[' expression_list_Comma_separated_opt ']'
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
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 36) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new Span();
			tmStack[tmHead].symbol = 36;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 36);
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
			case 0:  // import__optlist : import__optlist import_
				((List<TmaImport>)tmLeft.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 1:  // import__optlist :
				tmLeft.value = new ArrayList();
				break;
			case 2:  // input : header import__optlist option_optlist lexer_section parser_section
				tmLeft.value = new TmaInput(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* options */,
						((List<ITmaLexerPart>)tmStack[tmHead - 1].value) /* lexer */,
						((List<ITmaGrammarPart>)tmStack[tmHead].value) /* parser */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 3:  // input : header import__optlist option_optlist lexer_section
				tmLeft.value = new TmaInput(
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
			case 6:  // header : Llanguage name '(' name ')' parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 5].value) /* name */,
						((TmaName)tmStack[tmHead - 3].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 7:  // header : Llanguage name parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 2].value) /* name */,
						null /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // lexer_section : '::' Llexer lexer_parts
				tmLeft.value = ((List<ITmaLexerPart>)tmStack[tmHead].value);
				break;
			case 9:  // parser_section : '::' Lparser grammar_parts
				tmLeft.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value);
				break;
			case 10:  // parsing_algorithm : Llalr '(' icon ')'
				tmLeft.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // import_ : Limport ID scon ';'
				tmLeft.value = new TmaImport(
						((String)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 12:  // import_ : Limport scon ';'
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
			case 40:  // lexeme_attribute : Lsoft
				tmLeft.value = TmaLexemeAttribute.LSOFT;
				break;
			case 41:  // lexeme_attribute : Lclass
				tmLeft.value = TmaLexemeAttribute.LCLASS;
				break;
			case 42:  // lexeme_attribute : Lspace
				tmLeft.value = TmaLexemeAttribute.LSPACE;
				break;
			case 43:  // lexeme_attribute : Llayout
				tmLeft.value = TmaLexemeAttribute.LLAYOUT;
				break;
			case 44:  // brackets_directive : '%' Lbrackets symref_noargs symref_noargs ';'
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
			case 47:  // states_clause : '%' Ls lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						false /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 48:  // states_clause : '%' Lx lexer_state_list_Comma_separated ';'
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
			case 57:  // nonterm : annotations identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // nonterm : annotations identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // nonterm : annotations identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // nonterm : annotations identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 61:  // nonterm : identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 62:  // nonterm : identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 63:  // nonterm : identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 64:  // nonterm : identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 65:  // nonterm_type : Lreturns symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 66:  // nonterm_type : Linline Lclass identifieropt implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 67:  // nonterm_type : Lclass identifieropt implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 68:  // nonterm_type : Linterface identifieropt implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LINTERFACE /* kind */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 69:  // nonterm_type : Lvoid
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.LVOID /* kind */,
						null /* name */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // implements_clause : Limplements references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 72:  // assoc : Lleft
				tmLeft.value = TmaAssoc.LLEFT;
				break;
			case 73:  // assoc : Lright
				tmLeft.value = TmaAssoc.LRIGHT;
				break;
			case 74:  // assoc : Lnonassoc
				tmLeft.value = TmaAssoc.LNONASSOC;
				break;
			case 75:  // param_modifier : Lexplicit
				tmLeft.value = TmaParamModifier.LEXPLICIT;
				break;
			case 76:  // param_modifier : Lglobal
				tmLeft.value = TmaParamModifier.LGLOBAL;
				break;
			case 77:  // param_modifier : Llookahead
				tmLeft.value = TmaParamModifier.LLOOKAHEAD;
				break;
			case 78:  // template_param : '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 79:  // template_param : '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // template_param : '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 81:  // template_param : '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // directive : '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 83:  // directive : '%' Linput inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 84:  // directive : '%' Lassert Lempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 85:  // directive : '%' Lassert Lnonempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LNONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 86:  // directive : '%' Lgenerate ID '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((String)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 87:  // inputref_list_Comma_separated : inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 88:  // inputref_list_Comma_separated : inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 89:  // inputref : symref_noargs Lnoeoi
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // inputref : symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // references : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 92:  // references : references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 93:  // references_cs : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 94:  // references_cs : references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 95:  // rule0_list_Or_separated : rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 96:  // rule0_list_Or_separated : rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 98:  // rule0 : predicate rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 4].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // rule0 : predicate rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // rule0 : predicate rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // rule0 : predicate rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // rule0 : predicate rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // rule0 : predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // rule0 : predicate ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // rule0 : predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // rule0 : rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // rule0 : rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rule0 : rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // rule0 : rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // rule0 : rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // rule0 : rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // rule0 : ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rule0 : rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // rule0 : syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						null /* suffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // predicate : '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 116:  // rhsPrefix : annotations ':'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // rhsSuffix : '%' Lprec symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // rhsSuffix : '%' Lshift symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 119:  // ruleAction : '->' identifier '/' identifier
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((TmaIdentifier)tmStack[tmHead].value) /* kind */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // ruleAction : '->' identifier
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead].value) /* action */,
						null /* kind */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 121:  // rhsParts : rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 122:  // rhsParts : rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 123:  // rhsParts : rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 129:  // lookahead_predicate_list_And_separated : lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 130:  // lookahead_predicate_list_And_separated : lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 131:  // rhsLookahead : '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // lookahead_predicate : '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // lookahead_predicate : symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // rhsStateMarker : '.' ID
				tmLeft.value = new TmaRhsStateMarker(
						((String)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // rhsAnnotated : annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // rhsAssignment : identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // rhsAssignment : identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rhsOptional : rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // rhsCast : rhsClass Las symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // rhsCast : rhsClass Las literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // rhsUnordered : rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // rhsClass : identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // rhsPrimary : symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // rhsPrimary : '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rhsPrimary : '(' rhsParts Lseparator references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rhsPrimary : '(' rhsParts Lseparator references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rhsPrimary : rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rhsPrimary : rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rhsPrimary : '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rhsSet : Lset '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // setPrimary : ID symref
				tmLeft.value = new TmaSetSymbol(
						((String)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // setPrimary : symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // setPrimary : '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // setPrimary : '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // setExpression : setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // setExpression : setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // annotation_list : annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 165:  // annotation_list : annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 166:  // annotations : annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 167:  // annotation : '@' ID '=' expression
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 168:  // annotation : '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 169:  // annotation : '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 170:  // nonterm_param_list_Comma_separated : nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 171:  // nonterm_param_list_Comma_separated : nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 172:  // nonterm_params : '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 174:  // nonterm_param : ID identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // nonterm_param : ID identifier
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 176:  // param_ref : identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // argument_list_Comma_separated : argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 178:  // argument_list_Comma_separated : argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 181:  // symref_args : '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 182:  // argument : param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 183:  // argument : '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 184:  // argument : '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // argument : param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // param_type : Lflag
				tmLeft.value = TmaParamType.LFLAG;
				break;
			case 187:  // param_type : Lparam
				tmLeft.value = TmaParamType.LPARAM;
				break;
			case 190:  // predicate_primary : '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // predicate_primary : param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // predicate_primary : param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // predicate_primary : param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // predicate_expression : predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // predicate_expression : predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 199:  // expression : Lnew name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 200:  // expression : '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // expression_list_Comma_separated : expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 203:  // expression_list_Comma_separated : expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 206:  // map_entry_list_Comma_separated : map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 207:  // map_entry_list_Comma_separated : map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 210:  // map_entry : ID ':' expression
				tmLeft.value = new TmaMapEntry(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 211:  // literal : scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 212:  // literal : icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 213:  // literal : Ltrue
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 214:  // literal : Lfalse
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 215:  // name : qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 217:  // qualified_id : qualified_id '.' ID
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 218:  // command : code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 219:  // syntax_problem : error
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
		return (TmaInput) parse(lexer, 0, 424);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 425);
	}
}
