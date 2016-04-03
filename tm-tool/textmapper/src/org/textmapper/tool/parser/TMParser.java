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
	private static final int[] tmAction = TMLexer.unpack_int(423,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\323\0\324\0\uffbb\uffff\333\0\uff6f" +
		"\uffff\325\0\326\0\uffff\uffff\306\0\305\0\311\0\330\0\uff05\uffff\ufefd\uffff\ufef1" +
		"\uffff\313\0\ufeaf\uffff\uffff\uffff\ufea9\uffff\20\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\334\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\310" +
		"\0\uffff\uffff\uffff\uffff\17\0\260\0\ufe67\uffff\ufe5f\uffff\uffff\uffff\262\0\ufe59" +
		"\uffff\uffff\uffff\uffff\uffff\7\0\331\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\ufe1b\uffff\4\0\16\0\312\0\267\0\270\0\uffff\uffff\uffff\uffff\265\0\uffff" +
		"\uffff\ufe15\uffff\uffff\uffff\317\0\ufe0f\uffff\uffff\uffff\14\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\2\0\22\0\275\0\266\0\274\0\261\0\uffff\uffff\uffff" +
		"\uffff\307\0\uffff\uffff\12\0\13\0\uffff\uffff\uffff\uffff\ufe09\uffff\ufe01\uffff" +
		"\ufdfb\uffff\45\0\51\0\52\0\53\0\50\0\15\0\uffff\uffff\322\0\316\0\6\0\uffff\uffff" +
		"\ufdb5\uffff\uffff\uffff\67\0\uffff\uffff\uffff\uffff\336\0\uffff\uffff\46\0\47\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufdad\uffff\74\0\77\0\100\0\101\0\ufd69\uffff" +
		"\uffff\uffff\245\0\uffff\uffff\uffff\uffff\uffff\uffff\70\0\44\0\54\0\uffff\uffff" +
		"\35\0\36\0\31\0\32\0\uffff\uffff\27\0\30\0\34\0\37\0\41\0\40\0\33\0\uffff\uffff\26" +
		"\0\ufd23\uffff\uffff\uffff\121\0\122\0\123\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\273\0\272\0\uffff\uffff\uffff\uffff\ufcdb\uffff\251\0\ufc93\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\ufc41\uffff\ufc01\uffff\116\0\117\0\uffff\uffff\uffff" +
		"\uffff\75\0\76\0\244\0\uffff\uffff\uffff\uffff\71\0\72\0\66\0\23\0\43\0\uffff\uffff" +
		"\24\0\25\0\ufbc1\uffff\ufb73\uffff\uffff\uffff\140\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\143\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\ufb6b\uffff\uffff\uffff\uffff\uffff\ufb19\uffff\uffff\uffff\332\0\uffff" +
		"\uffff\224\0\ufab3\uffff\uffff\uffff\150\0\ufaab\uffff\ufa5b\uffff\354\0\ufa0b\uffff" +
		"\ufa01\uffff\uf9af\uffff\204\0\207\0\211\0\uf959\uffff\205\0\uf901\uffff\uf8a7\uffff" +
		"\233\0\uffff\uffff\206\0\172\0\171\0\uf849\uffff\uffff\uffff\253\0\255\0\uf809\uffff" +
		"\112\0\350\0\uf7c9\uffff\uf7c3\uffff\uf7bd\uffff\uf76b\uffff\uffff\uffff\uf719\uffff" +
		"\uffff\uffff\uffff\uffff\65\0\42\0\uffff\uffff\340\0\uf6c7\uffff\141\0\133\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\132\0\144\0\uffff" +
		"\uffff\131\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf67b\uffff\302\0\uffff" +
		"\uffff\uffff\uffff\uf66f\uffff\uffff\uffff\uf61d\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uf5cb\uffff\111\0\uf577\uffff\uf527\uffff\uf51d\uffff\161\0\uf4cb" +
		"\uffff\uf4c1\uffff\uffff\uffff\165\0\170\0\uf46f\uffff\uf465\uffff\203\0\167\0\uffff" +
		"\uffff\215\0\uffff\uffff\230\0\231\0\174\0\210\0\uf40f\uffff\uffff\uffff\254\0\uf407" +
		"\uffff\uffff\uffff\352\0\114\0\115\0\uffff\uffff\uffff\uffff\uf401\uffff\uffff\uffff" +
		"\uf3af\uffff\uf35d\uffff\uffff\uffff\57\0\342\0\uf30b\uffff\137\0\uffff\uffff\134" +
		"\0\135\0\uffff\uffff\127\0\uffff\uffff\125\0\uffff\uffff\247\0\175\0\176\0\276\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\173\0\uffff\uffff\225\0\uffff\uffff\uffff\uffff" +
		"\200\0\uffff\uffff\uffff\uffff\uffff\uffff\uf2c1\uffff\236\0\241\0\uffff\uffff\uffff" +
		"\uffff\212\0\uf27d\uffff\213\0\147\0\uf21f\uffff\uf215\uffff\155\0\160\0\uf1c3\uffff" +
		"\157\0\164\0\uf1b9\uffff\163\0\166\0\uf1af\uffff\217\0\220\0\uffff\uffff\252\0\113" +
		"\0\145\0\uf159\uffff\110\0\107\0\uffff\uffff\105\0\uffff\uffff\uffff\uffff\uf153" +
		"\uffff\uffff\uffff\344\0\uf101\uffff\136\0\uffff\uffff\uffff\uffff\130\0\300\0\301" +
		"\0\uf0b9\uffff\uf0b1\uffff\uffff\uffff\177\0\232\0\uffff\uffff\240\0\235\0\uffff" +
		"\uffff\234\0\uffff\uffff\154\0\uf0a9\uffff\153\0\156\0\162\0\256\0\uffff\uffff\106" +
		"\0\104\0\103\0\uffff\uffff\61\0\62\0\63\0\64\0\uffff\uffff\346\0\55\0\126\0\124\0" +
		"\uffff\uffff\237\0\uf09f\uffff\uf097\uffff\152\0\146\0\102\0\60\0\227\0\226\0\uffff" +
		"\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(3950,
		"\11\1\46\1\47\1\55\1\57\1\60\1\61\1\62\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1" +
		"\73\1\74\1\75\1\76\1\77\1\100\1\101\1\102\1\103\1\104\1\105\1\106\1\107\1\110\1\111" +
		"\1\112\1\uffff\ufffe\2\uffff\3\uffff\24\uffff\46\uffff\47\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\50\uffff\51\uffff\52\uffff\25\315\uffff\ufffe\32\uffff\0\21\7\21\11\21\12\21\20" +
		"\21\22\21\25\21\26\21\27\21\30\21\31\21\34\21\35\21\37\21\42\21\44\21\45\21\46\21" +
		"\47\21\53\21\54\21\56\21\57\21\60\21\61\21\62\21\63\21\64\21\65\21\66\21\67\21\70" +
		"\21\71\21\72\21\73\21\74\21\75\21\76\21\77\21\100\21\101\21\102\21\103\21\104\21" +
		"\105\21\106\21\107\21\110\21\111\21\112\21\114\21\uffff\ufffe\26\uffff\110\uffff" +
		"\20\335\uffff\ufffe\21\uffff\20\327\26\327\27\327\110\327\uffff\ufffe\55\uffff\11" +
		"\5\46\5\47\5\57\5\60\5\61\5\62\5\63\5\64\5\65\5\66\5\67\5\70\5\71\5\72\5\73\5\74" +
		"\5\75\5\76\5\77\5\100\5\101\5\102\5\103\5\104\5\105\5\106\5\107\5\110\5\111\5\112" +
		"\5\uffff\ufffe\22\uffff\25\314\uffff\ufffe\35\uffff\41\uffff\47\uffff\112\uffff\111" +
		"\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101" +
		"\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70" +
		"\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57" +
		"\uffff\33\264\uffff\ufffe\23\uffff\22\271\33\271\uffff\ufffe\22\uffff\33\263\uffff" +
		"\ufffe\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff" +
		"\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\27\321\uffff\ufffe\11\uffff\0\3\uffff\ufffe\22\uffff" +
		"\27\320\uffff\ufffe\110\uffff\20\335\uffff\ufffe\14\uffff\23\17\26\17\uffff\ufffe" +
		"\26\uffff\23\337\uffff\ufffe\7\uffff\24\uffff\46\uffff\47\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\0\10\11\10\uffff\ufffe\17\uffff\22\73\25\73\uffff\ufffe\7\uffff\45\uffff\46\uffff" +
		"\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103" +
		"\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\0\11\uffff\ufffe\45\uffff\23\246\26\246\44\246\47\246" +
		"\56\246\57\246\60\246\61\246\62\246\63\246\64\246\65\246\66\246\67\246\70\246\71" +
		"\246\72\246\73\246\74\246\75\246\76\246\77\246\100\246\101\246\102\246\103\246\104" +
		"\246\105\246\106\246\107\246\110\246\111\246\112\246\uffff\ufffe\1\uffff\0\56\7\56" +
		"\11\56\24\56\46\56\47\56\57\56\60\56\61\56\62\56\63\56\64\56\65\56\66\56\67\56\70" +
		"\56\71\56\72\56\73\56\74\56\75\56\76\56\77\56\100\56\101\56\102\56\103\56\104\56" +
		"\105\56\106\56\107\56\110\56\111\56\112\56\uffff\ufffe\115\uffff\23\250\26\250\44" +
		"\250\45\250\47\250\56\250\57\250\60\250\61\250\62\250\63\250\64\250\65\250\66\250" +
		"\67\250\70\250\71\250\72\250\73\250\74\250\75\250\76\250\77\250\100\250\101\250\102" +
		"\250\103\250\104\250\105\250\106\250\107\250\110\250\111\250\112\250\uffff\ufffe" +
		"\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff" +
		"\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff" +
		"\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff" +
		"\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\114\uffff\12\355\20\355\uffff\ufffe\47\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\10\351\23\351\uffff\ufffe\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff" +
		"\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75" +
		"\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\10\351\23\351\uffff\ufffe\17" +
		"\uffff\0\341\3\341\7\341\11\341\24\341\26\341\46\341\47\341\57\341\60\341\61\341" +
		"\62\341\63\341\64\341\65\341\66\341\67\341\70\341\71\341\72\341\73\341\74\341\75" +
		"\341\76\341\77\341\100\341\101\341\102\341\103\341\104\341\105\341\106\341\107\341" +
		"\110\341\111\341\112\341\114\341\uffff\ufffe\100\uffff\20\142\22\142\uffff\ufffe" +
		"\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff" +
		"\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff" +
		"\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff" +
		"\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\114\uffff\12\355\27\355\uffff\ufffe\32\uffff\14\17\23\17\36\17" +
		"\7\21\12\21\20\21\26\21\27\21\30\21\34\21\35\21\37\21\42\21\44\21\45\21\46\21\47" +
		"\21\53\21\54\21\56\21\57\21\60\21\61\21\62\21\63\21\64\21\65\21\66\21\67\21\70\21" +
		"\71\21\72\21\73\21\74\21\75\21\76\21\77\21\100\21\101\21\102\21\103\21\104\21\105" +
		"\21\106\21\107\21\110\21\111\21\112\21\114\21\uffff\ufffe\12\uffff\20\151\27\151" +
		"\uffff\ufffe\7\uffff\26\uffff\30\uffff\44\uffff\45\uffff\47\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\114\uffff\12\355\20\355\27\355\uffff\ufffe\7\uffff\26\uffff\30\uffff\44" +
		"\uffff\45\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff" +
		"\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74" +
		"\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20\355\27\355" +
		"\uffff\ufffe\7\uffff\12\355\20\355\27\355\uffff\ufffe\7\uffff\26\uffff\30\uffff\44" +
		"\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff" +
		"\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75" +
		"\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20" +
		"\355\27\355\uffff\ufffe\42\uffff\7\201\12\201\20\201\26\201\27\201\30\201\44\201" +
		"\45\201\46\201\47\201\53\201\56\201\57\201\60\201\61\201\62\201\63\201\64\201\65" +
		"\201\66\201\67\201\70\201\71\201\72\201\73\201\74\201\75\201\76\201\77\201\100\201" +
		"\101\201\102\201\103\201\104\201\105\201\106\201\107\201\110\201\111\201\112\201" +
		"\114\201\uffff\ufffe\37\uffff\7\214\12\214\20\214\26\214\27\214\30\214\42\214\44" +
		"\214\45\214\46\214\47\214\53\214\56\214\57\214\60\214\61\214\62\214\63\214\64\214" +
		"\65\214\66\214\67\214\70\214\71\214\72\214\73\214\74\214\75\214\76\214\77\214\100" +
		"\214\101\214\102\214\103\214\104\214\105\214\106\214\107\214\110\214\111\214\112" +
		"\214\114\214\uffff\ufffe\54\uffff\7\216\12\216\20\216\26\216\27\216\30\216\37\216" +
		"\42\216\44\216\45\216\46\216\47\216\53\216\56\216\57\216\60\216\61\216\62\216\63" +
		"\216\64\216\65\216\66\216\67\216\70\216\71\216\72\216\73\216\74\216\75\216\76\216" +
		"\77\216\100\216\101\216\102\216\103\216\104\216\105\216\106\216\107\216\110\216\111" +
		"\216\112\216\114\216\uffff\ufffe\34\uffff\35\uffff\7\222\12\222\20\222\26\222\27" +
		"\222\30\222\37\222\42\222\44\222\45\222\46\222\47\222\53\222\54\222\56\222\57\222" +
		"\60\222\61\222\62\222\63\222\64\222\65\222\66\222\67\222\70\222\71\222\72\222\73" +
		"\222\74\222\75\222\76\222\77\222\100\222\101\222\102\222\103\222\104\222\105\222" +
		"\106\222\107\222\110\222\111\222\112\222\114\222\uffff\ufffe\47\uffff\112\uffff\111" +
		"\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101" +
		"\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70" +
		"\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57" +
		"\uffff\22\17\33\17\uffff\ufffe\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106" +
		"\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff" +
		"\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\10\351\23\351\uffff\ufffe" +
		"\23\uffff\10\353\uffff\ufffe\23\uffff\10\353\uffff\ufffe\7\uffff\24\uffff\26\uffff" +
		"\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff" +
		"\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76" +
		"\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65" +
		"\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12" +
		"\355\20\355\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff" +
		"\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103" +
		"\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20\355\uffff\ufffe\7\uffff" +
		"\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\114\uffff\12\355\20\355\uffff\ufffe\3\uffff\0\343\7\343\11\343\24\343\26" +
		"\343\46\343\47\343\57\343\60\343\61\343\62\343\63\343\64\343\65\343\66\343\67\343" +
		"\70\343\71\343\72\343\73\343\74\343\75\343\76\343\77\343\100\343\101\343\102\343" +
		"\103\343\104\343\105\343\106\343\107\343\110\343\111\343\112\343\114\343\uffff\ufffe" +
		"\15\uffff\16\uffff\13\277\25\277\43\277\uffff\ufffe\7\uffff\26\uffff\30\uffff\44" +
		"\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff" +
		"\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75" +
		"\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\53\uffff\56\uffff\114\uffff\12" +
		"\355\27\355\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff" +
		"\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103" +
		"\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\27\355\uffff\ufffe\7\uffff" +
		"\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\114\uffff\12\355\20\355\27\355\uffff\ufffe\7\uffff\26\uffff\30\uffff\44" +
		"\uffff\45\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff" +
		"\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74" +
		"\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20\355\27\355" +
		"\uffff\ufffe\7\uffff\12\355\20\355\27\355\uffff\ufffe\7\uffff\26\uffff\30\uffff\44" +
		"\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff" +
		"\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75" +
		"\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20" +
		"\355\27\355\uffff\ufffe\7\uffff\12\355\20\355\27\355\uffff\ufffe\7\uffff\26\uffff" +
		"\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff" +
		"\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76" +
		"\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65" +
		"\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12" +
		"\355\20\355\27\355\uffff\ufffe\7\uffff\12\355\20\355\27\355\uffff\ufffe\42\uffff" +
		"\7\202\12\202\20\202\26\202\27\202\30\202\44\202\45\202\46\202\47\202\53\202\56\202" +
		"\57\202\60\202\61\202\62\202\63\202\64\202\65\202\66\202\67\202\70\202\71\202\72" +
		"\202\73\202\74\202\75\202\76\202\77\202\100\202\101\202\102\202\103\202\104\202\105" +
		"\202\106\202\107\202\110\202\111\202\112\202\114\202\uffff\ufffe\14\uffff\22\257" +
		"\33\257\uffff\ufffe\23\uffff\10\353\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff" +
		"\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106" +
		"\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff" +
		"\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355" +
		"\20\355\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff" +
		"\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103" +
		"\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff" +
		"\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20\355\uffff\ufffe\7\uffff" +
		"\24\uffff\26\uffff\30\uffff\44\uffff\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff" +
		"\110\uffff\107\uffff\106\uffff\105\uffff\104\uffff\103\uffff\102\uffff\101\uffff" +
		"\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\114\uffff\12\355\20\355\uffff\ufffe\26\uffff\0\345\7\345\11\345\24\345" +
		"\46\345\47\345\57\345\60\345\61\345\62\345\63\345\64\345\65\345\66\345\67\345\70" +
		"\345\71\345\72\345\73\345\74\345\75\345\76\345\77\345\100\345\101\345\102\345\103" +
		"\345\104\345\105\345\106\345\107\345\110\345\111\345\112\345\114\345\uffff\ufffe" +
		"\32\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff\104" +
		"\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74\uffff" +
		"\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\12\21\27\21\42\21\uffff\ufffe\34\uffff\35\uffff" +
		"\7\223\12\223\20\223\26\223\27\223\30\223\37\223\42\223\44\223\45\223\46\223\47\223" +
		"\53\223\54\223\56\223\57\223\60\223\61\223\62\223\63\223\64\223\65\223\66\223\67" +
		"\223\70\223\71\223\72\223\73\223\74\223\75\223\76\223\77\223\100\223\101\223\102" +
		"\223\103\223\104\223\105\223\106\223\107\223\110\223\111\223\112\223\114\223\uffff" +
		"\ufffe\7\uffff\12\355\20\355\27\355\uffff\ufffe\7\uffff\26\uffff\30\uffff\44\uffff" +
		"\45\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105" +
		"\uffff\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff" +
		"\74\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20\355\27" +
		"\355\uffff\ufffe\7\uffff\12\355\20\355\27\355\uffff\ufffe\7\uffff\12\355\20\355\27" +
		"\355\uffff\ufffe\42\221\7\221\12\221\20\221\26\221\27\221\30\221\44\221\45\221\46" +
		"\221\47\221\53\221\56\221\57\221\60\221\61\221\62\221\63\221\64\221\65\221\66\221" +
		"\67\221\70\221\71\221\72\221\73\221\74\221\75\221\76\221\77\221\100\221\101\221\102" +
		"\221\103\221\104\221\105\221\106\221\107\221\110\221\111\221\112\221\114\221\uffff" +
		"\ufffe\22\uffff\10\120\uffff\ufffe\7\uffff\24\uffff\26\uffff\30\uffff\44\uffff\45" +
		"\uffff\46\uffff\47\uffff\112\uffff\111\uffff\110\uffff\107\uffff\106\uffff\105\uffff" +
		"\104\uffff\103\uffff\102\uffff\101\uffff\100\uffff\77\uffff\76\uffff\75\uffff\74" +
		"\uffff\73\uffff\72\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\114\uffff\12\355\20\355\uffff" +
		"\ufffe\114\uffff\0\347\7\347\11\347\24\347\46\347\47\347\57\347\60\347\61\347\62" +
		"\347\63\347\64\347\65\347\66\347\67\347\70\347\71\347\72\347\73\347\74\347\75\347" +
		"\76\347\77\347\100\347\101\347\102\347\103\347\104\347\105\347\106\347\107\347\110" +
		"\347\111\347\112\347\uffff\ufffe\13\304\43\uffff\25\304\uffff\ufffe\13\303\43\303" +
		"\25\303\uffff\ufffe\7\uffff\12\355\20\355\27\355\uffff\ufffe\12\242\42\uffff\27\242" +
		"\uffff\ufffe\12\243\42\243\27\243\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(173,
		"\0\2\4\26\46\46\46\46\105\115\117\124\127\137\140\141\143\173\200\213\224\253\262" +
		"\337\354\377\u0102\u010b\u0111\u0118\u011d\u011e\u0123\u0126\u012d\u0138\u013b\u0154" +
		"\u016f\u0189\u01ea\u01f8\u0206\u020c\u020d\u020e\u020f\u022b\u028d\u02f2\u0354\u03b6" +
		"\u041b\u047d\u04df\u0541\u05a3\u0605\u0667\u06c9\u072b\u078d\u07ef\u0853\u08b7\u0919" +
		"\u097b\u09e2\u0a47\u0aac\u0b0e\u0b70\u0bd2\u0c35\u0c97\u0cf9\u0cf9\u0d0e\u0d0f\u0d10" +
		"\u0d11\u0d12\u0d13\u0d14\u0d15\u0d17\u0d18\u0d19\u0d4a\u0d70\u0d82\u0d87\u0d89\u0d8d" +
		"\u0d8f\u0d90\u0d92\u0d94\u0d96\u0d97\u0d98\u0d99\u0d9b\u0d9c\u0d9e\u0da0\u0da2\u0da3" +
		"\u0da5\u0da7\u0dab\u0dae\u0daf\u0db1\u0db3\u0db4\u0db6\u0db8\u0db9\u0dc3\u0dcd\u0dd8" +
		"\u0de3\u0def\u0e0a\u0e1d\u0e2b\u0e3f\u0e53\u0e69\u0e81\u0e99\u0ead\u0ec5\u0ede\u0efa" +
		"\u0eff\u0f03\u0f19\u0f2f\u0f46\u0f47\u0f49\u0f4b\u0f55\u0f56\u0f57\u0f5a\u0f5c\u0f5f" +
		"\u0f64\u0f67\u0f6a\u0f70\u0f71\u0f72\u0f73\u0f74\u0f76\u0f84\u0f87\u0f8a\u0f9f\u0fb9" +
		"\u0fbb\u0fbc\u0fbd\u0fbe\u0fbf\u0fc0\u0fc3\u0fc6\u0fe1");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(4065,
		"\u01a3\u01a4\147\215\1\6\36\41\61\72\106\116\150\277\375\u0108\u011f\u013a\u013c" +
		"\u0143\u0144\u0165\1\6\41\55\72\106\116\277\362\375\u011f\u013a\u013c\u0143\u0144" +
		"\u0165\105\130\137\160\234\302\315\316\320\321\350\351\353\u0107\u0109\u010e\u0110" +
		"\u0111\u0112\u0114\u0115\u0119\u012e\u0130\u0131\u0158\u0159\u015c\u015f\u0170\u0186" +
		"\157\244\245\251\352\354\355\u0132\37\64\312\u0152\u017f\u019b\u019c\u0105\u017a" +
		"\u017b\63\126\267\276\310\371\372\u0124\u0103\u0103\144\263\34\60\104\121\252\265" +
		"\275\276\313\367\370\371\372\u012c\u012d\u012f\u0137\u013e\u016c\u016e\u016f\u0175" +
		"\u0176\u018f\21\150\203\213\260\24\50\76\145\150\203\213\260\265\340\u0169\47\75" +
		"\152\310\333\346\347\u0127\u0153\1\6\41\105\106\116\130\150\203\213\234\260\277\302" +
		"\350\351\353\u0109\u010e\u012e\u0130\u0131\u0170\25\145\150\203\213\260\u0105\20" +
		"\30\32\127\150\157\203\213\234\245\251\260\302\304\306\315\316\321\333\350\351\353" +
		"\355\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e" +
		"\u0130\u0131\u0135\u014d\u014e\u0159\u0170\u0182\u0184\54\77\102\176\203\213\260" +
		"\u0106\u014c\u0152\u017c\u017f\u0194\234\302\315\316\321\350\351\353\u0107\u0109" +
		"\u010e\u0110\u0112\u0115\u012e\u0130\u0131\u0159\u0170\377\u0108\u014a\10\150\157" +
		"\203\213\251\260\305\u014f\51\150\203\213\260\340\150\203\213\260\331\u0155\u0199" +
		"\26\73\331\u0155\u0199\310\150\203\213\260\326\301\u0145\u0147\26\73\u010a\u014d" +
		"\u014e\u0182\u0184\150\203\213\260\322\u011a\u0152\u0162\u017f\u019b\u019c\u0105" +
		"\u017a\u017b\234\302\315\316\321\333\350\351\353\u0107\u0109\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0159\u0170\137\150\160\165\203" +
		"\213\234\260\302\315\316\321\350\351\353\u0107\u0109\u010e\u0110\u0112\u0115\u011d" +
		"\u012e\u0130\u0131\u0159\u0170\1\6\37\41\106\116\130\156\160\234\277\302\321\350" +
		"\351\353\u0107\u0109\u010e\u0112\u0115\u012e\u0130\u0131\u0159\u0170\1\2\6\13\26" +
		"\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\160\166\170" +
		"\171\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275\277\301\302" +
		"\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109" +
		"\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e" +
		"\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c" +
		"\u0182\u0184\u018b\1\6\41\72\106\116\277\375\u011f\u013a\u013c\u0143\u0144\u0165" +
		"\1\6\41\72\106\116\277\375\u011f\u013a\u013c\u0143\u0144\u0165\1\6\41\106\116\277" +
		"\u0107\330\22\234\270\271\302\315\316\321\333\350\351\353\366\u0107\u0109\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0159\u0170\1\2\6\13" +
		"\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\124\125\130\137\143\150\156\160" +
		"\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275\277" +
		"\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107" +
		"\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128" +
		"\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170" +
		"\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\156\157\160\166\170\171\172\203\213\216\222\230\231\234\235" +
		"\237\240\241\245\251\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350" +
		"\351\353\355\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145" +
		"\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13" +
		"\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\160\166" +
		"\170\171\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275\277\300" +
		"\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107" +
		"\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128" +
		"\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170" +
		"\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\230\231\234\235\237" +
		"\240\241\260\272\273\275\277\300\301\302\303\315\316\321\333\337\343\350\351\353" +
		"\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149" +
		"\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36" +
		"\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\157\160\166\170\171" +
		"\172\203\213\216\222\230\231\234\235\237\240\241\245\251\260\272\273\275\277\301" +
		"\302\303\315\316\321\333\337\343\350\351\353\355\360\365\375\u0100\u0101\u0102\u0107" +
		"\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128" +
		"\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170" +
		"\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\155\156\160\166\170\171\172\203\213\216\222\230\231\234\235" +
		"\237\240\241\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350\351\353" +
		"\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149" +
		"\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36" +
		"\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155\156\160\166\170\171" +
		"\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275\277\301\302\303" +
		"\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a" +
		"\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130" +
		"\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182" +
		"\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137" +
		"\143\150\155\156\160\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241" +
		"\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375" +
		"\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116" +
		"\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e" +
		"\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44" +
		"\53\72\73\105\106\116\117\125\130\137\143\150\155\156\160\166\170\171\172\203\213" +
		"\216\222\230\231\234\235\237\240\241\260\272\273\275\277\301\302\303\315\316\321" +
		"\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c" +
		"\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a" +
		"\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b" +
		"\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155" +
		"\156\160\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273" +
		"\275\277\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101" +
		"\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f" +
		"\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159" +
		"\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105" +
		"\106\116\117\125\130\137\143\150\155\156\160\166\170\171\172\203\213\216\222\230" +
		"\231\234\235\237\240\241\260\272\273\275\277\301\302\303\315\316\321\333\337\343" +
		"\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145" +
		"\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13" +
		"\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\160\166" +
		"\170\171\172\203\213\216\222\223\230\231\234\235\237\240\241\260\272\273\275\277" +
		"\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107" +
		"\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128" +
		"\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170" +
		"\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\223\230\231\234\235" +
		"\237\240\241\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350\351\353" +
		"\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149" +
		"\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36" +
		"\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\155\156\160\166\170\171" +
		"\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275\277\301\302\303" +
		"\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a" +
		"\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130" +
		"\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182" +
		"\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137" +
		"\143\150\155\156\160\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241" +
		"\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375" +
		"\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116" +
		"\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e" +
		"\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44" +
		"\53\72\73\105\106\116\117\125\130\137\143\150\155\156\160\166\170\171\172\203\213" +
		"\216\222\224\225\230\231\234\235\237\240\241\260\272\273\275\277\301\302\303\315" +
		"\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a" +
		"\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130" +
		"\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182" +
		"\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137" +
		"\143\150\155\156\160\166\170\171\172\203\213\216\222\224\225\230\231\234\235\237" +
		"\240\241\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350\351\353\360" +
		"\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149" +
		"\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36" +
		"\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\160\166\170\171\172" +
		"\203\213\216\222\230\231\234\235\237\240\241\260\264\272\273\275\277\301\302\303" +
		"\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a" +
		"\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130" +
		"\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182" +
		"\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137" +
		"\143\150\156\160\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241\260" +
		"\272\273\275\277\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100" +
		"\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d" +
		"\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f" +
		"\u0159\u0165\u0170\u0171\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44" +
		"\53\72\73\105\106\116\117\125\130\137\143\150\156\157\160\166\170\171\172\203\213" +
		"\216\222\230\231\234\235\236\237\240\241\245\251\260\272\273\275\277\301\302\303" +
		"\315\316\321\333\337\343\350\351\353\355\360\365\375\u0100\u0101\u0102\u0107\u0109" +
		"\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e" +
		"\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u0171" +
		"\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117" +
		"\125\130\137\143\150\156\157\160\166\170\171\172\203\213\216\222\230\231\234\235" +
		"\237\240\241\245\251\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350" +
		"\351\353\355\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145" +
		"\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13" +
		"\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156\157\160" +
		"\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241\245\251\260\272\273" +
		"\275\277\301\302\303\315\316\321\333\337\343\350\351\353\355\360\365\375\u0100\u0101" +
		"\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f" +
		"\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159" +
		"\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105" +
		"\106\116\117\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\230\231" +
		"\234\235\237\240\241\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350" +
		"\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145" +
		"\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u0171\u017c\u0182\u0184\u018b\1" +
		"\2\6\13\26\31\35\36\37\41\43\44\53\72\73\105\106\116\117\125\130\137\143\150\156" +
		"\160\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275" +
		"\277\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102" +
		"\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125" +
		"\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165" +
		"\u0170\u0171\u017c\u0182\u0184\u018b\0\1\2\6\13\26\31\35\36\37\41\43\44\53\72\73" +
		"\105\106\116\117\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\230" +
		"\231\234\235\237\240\241\260\272\273\275\277\301\302\303\315\316\321\333\337\343" +
		"\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145" +
		"\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13" +
		"\20\26\31\35\36\37\41\43\44\53\72\73\101\105\106\116\117\125\130\137\143\150\156" +
		"\160\166\170\171\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275" +
		"\277\301\302\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102" +
		"\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125" +
		"\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165" +
		"\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31\35\36\37\41\43\44\53\62\72\73\105\106" +
		"\116\117\125\130\137\143\150\156\160\166\170\171\172\203\213\216\222\230\231\234" +
		"\235\237\240\241\260\272\273\275\277\301\302\303\315\316\321\333\337\343\350\351" +
		"\353\360\365\375\u0100\u0101\u0102\u0107\u0109\u010a\u010b\u010c\u010d\u010e\u0110" +
		"\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e\u0130\u0131\u013a\u013c\u0145\u0147" +
		"\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c\u0182\u0184\u018b\1\2\6\13\26\31" +
		"\35\36\37\41\43\44\53\72\73\105\106\107\116\117\125\130\137\143\150\156\160\166\170" +
		"\171\172\203\213\216\222\230\231\234\235\237\240\241\260\272\273\275\277\301\302" +
		"\303\315\316\321\333\337\343\350\351\353\360\365\375\u0100\u0101\u0102\u0107\u0109" +
		"\u010a\u010b\u010c\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u011f\u0125\u0128\u012e" +
		"\u0130\u0131\u013a\u013c\u0145\u0147\u0149\u014d\u014e\u014f\u0159\u0165\u0170\u017c" +
		"\u0182\u0184\u018b\234\302\315\316\321\350\351\353\u0107\u0109\u010e\u0110\u0112" +
		"\u0115\u011d\u012e\u0130\u0131\u0159\u0170\u0173\232\3\0\22\0\37\64\20\101\22\37" +
		"\26\43\44\73\105\125\130\137\160\166\172\231\234\235\240\241\272\273\301\302\303" +
		"\315\316\321\333\337\343\350\351\353\u0102\u0107\u0109\u010b\u010d\u010e\u0110\u0112" +
		"\u0115\u0116\u011d\u0125\u012e\u0130\u0131\u0145\u0147\u0159\u0170\1\6\41\106\116" +
		"\234\277\302\315\316\321\333\350\351\353\u0107\u0109\u010a\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u011f\u012e\u0130\u0131\u014d\u014e\u014f\u0159\u0170" +
		"\u0182\u0184\72\143\170\216\230\237\275\365\375\u0100\u0101\u0128\u013a\u013c\u0149" +
		"\u0165\u017c\u018b\127\157\245\251\355\150\203\150\203\213\260\147\215\105\105\130" +
		"\105\130\105\130\263\u0135\u0171\105\130\125\105\130\171\360\125\172\137\137\160" +
		"\137\160\157\245\251\355\346\347\u0127\155\137\160\137\160\216\216\365\230\u0149" +
		"\u0128\234\302\350\351\353\u0109\u012e\u0130\u0131\u0170\234\302\350\351\353\u0109" +
		"\u012e\u0130\u0131\u0170\234\302\350\351\353\u0109\u010e\u012e\u0130\u0131\u0170" +
		"\234\302\350\351\353\u0109\u010e\u012e\u0130\u0131\u0170\234\302\315\350\351\353" +
		"\u0109\u010e\u012e\u0130\u0131\u0170\234\302\315\316\320\321\350\351\353\u0107\u0109" +
		"\u010e\u0110\u0111\u0112\u0114\u0115\u0119\u012e\u0130\u0131\u0158\u0159\u015c\u015f" +
		"\u0170\u0186\234\302\315\316\321\350\351\353\u0107\u0109\u010e\u0110\u0112\u0115" +
		"\u012e\u0130\u0131\u0159\u0170\234\302\315\316\350\351\353\u0109\u010e\u0110\u012e" +
		"\u0130\u0131\u0170\234\302\315\316\321\350\351\353\u0107\u0109\u010e\u0110\u0112" +
		"\u0115\u011d\u012e\u0130\u0131\u0159\u0170\234\302\315\316\321\350\351\353\u0107" +
		"\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131\u0159\u0170\234\302\315\316" +
		"\321\333\350\351\353\u0107\u0109\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130" +
		"\u0131\u0159\u0170\234\302\315\316\321\333\350\351\353\u0107\u0109\u010b\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0159\u0170\234\302\315\316\321" +
		"\333\350\351\353\u0107\u0109\u010b\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e" +
		"\u0130\u0131\u0159\u0170\234\302\315\316\321\350\351\353\u0107\u0109\u010e\u0110" +
		"\u0112\u0115\u011d\u012e\u0130\u0131\u0159\u0170\234\302\315\316\321\333\350\351" +
		"\353\u0107\u0109\u010b\u010d\u010e\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131" +
		"\u0159\u0170\234\302\315\316\321\333\350\351\353\u0107\u0109\u010b\u010c\u010d\u010e" +
		"\u0110\u0112\u0115\u0116\u011d\u012e\u0130\u0131\u0159\u0170\234\270\271\302\315" +
		"\316\321\333\350\351\353\366\u0107\u0109\u010b\u010c\u010d\u010e\u0110\u0112\u0115" +
		"\u0116\u011d\u012e\u0130\u0131\u0159\u0170\u010a\u014d\u014e\u0182\u0184\u010a\u014d" +
		"\u0182\u0184\137\160\234\302\315\316\321\350\351\353\u0107\u0109\u010e\u0110\u0112" +
		"\u0115\u011d\u012e\u0130\u0131\u0159\u0170\137\160\234\302\315\316\321\350\351\353" +
		"\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131\u0159\u0170\137\160" +
		"\165\234\302\315\316\321\350\351\353\u0107\u0109\u010e\u0110\u0112\u0115\u011d\u012e" +
		"\u0130\u0131\u0159\u0170\235\157\251\235\u0125\26\43\44\73\235\301\u0102\u0125\u0145" +
		"\u0147\26\26\10\305\u014f\26\73\155\224\225\72\375\u013a\u013c\u0165\301\u0145\u0147" +
		"\301\u0145\u0147\1\6\41\106\116\277\6\6\53\53\53\117\1\6\41\72\106\116\277\375\u011f" +
		"\u013a\u013c\u0143\u0144\u0165\2\13\31\2\13\31\234\302\315\316\321\350\351\353\u0107" +
		"\u0109\u010e\u0110\u0112\u0115\u011d\u012e\u0130\u0131\u0159\u0170\u0173\1\6\37\41" +
		"\106\116\130\156\160\234\277\302\321\350\351\353\u0107\u0109\u010e\u0112\u0115\u012e" +
		"\u0130\u0131\u0159\u0170\20\101\127\263\362\u0135\u0173\240\241\343\346\347\u0127" +
		"\234\302\315\316\320\321\350\351\353\u0107\u0109\u010e\u0110\u0111\u0112\u0114\u0115" +
		"\u0119\u012e\u0130\u0131\u0158\u0159\u015c\u015f\u0170\u0186");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(4065,
		"\u01a5\u01a6\174\174\4\4\60\4\104\4\4\4\176\4\4\u014a\4\4\4\4\4\4\5\5\5\102\5\5\5" +
		"\5\u0134\5\5\5\5\5\5\5\124\124\155\155\300\300\300\300\300\300\300\300\300\300\300" +
		"\300\300\300\300\300\300\300\300\300\300\300\300\300\300\300\300\234\350\351\353" +
		"\u012e\u0130\u0131\u0170\62\107\u010e\u0182\u0182\u0182\u0182\u0145\u0145\u0145\106" +
		"\147\366\375\u010b\u013a\u013c\u0165\u0143\u0144\171\360\56\103\123\142\356\364\373" +
		"\376\u010f\u0138\u0139\u013b\u013d\u016a\u016b\u016d\u0174\u0177\u018c\u018d\u018e" +
		"\u0197\u0198\u019f\35\177\177\177\177\41\73\117\172\200\200\200\200\365\u0125\u018b" +
		"\72\116\215\u010c\u0122\u0128\u0128\u0128\u010c\6\6\6\125\6\6\125\201\201\201\301" +
		"\201\6\301\301\301\301\301\301\301\301\301\301\42\173\202\202\202\202\u0146\31\53" +
		"\55\150\203\150\203\203\302\150\150\203\302\u0109\u010a\302\302\302\302\302\302\302" +
		"\150\302\302\u014d\302\302\302\302\302\302\302\302\302\302\302\302\u0171\u014d\u014d" +
		"\302\302\u014d\u014d\101\120\122\256\257\261\357\u0148\u017e\u0183\u0199\u019a\u01a0" +
		"\303\303\303\303\303\303\303\303\303\303\303\303\303\303\303\303\303\303\303\u013f" +
		"\u014b\u017d\26\204\235\204\204\235\204\26\26\74\205\205\205\205\u0126\206\206\206" +
		"\206\u0120\u0120\u01a1\43\43\u0121\u0121\u01a2\u010d\207\207\207\207\u011e\u0102" +
		"\u0102\u0102\44\44\u014e\u014e\u014e\u014e\u014e\210\210\210\210\u011d\u011d\u0184" +
		"\u011d\u0184\u0184\u0184\u0147\u0147\u0147\304\304\304\304\304\304\304\304\304\304" +
		"\304\304\304\304\304\304\304\304\304\304\304\304\304\304\304\156\211\156\156\211" +
		"\211\156\211\156\156\156\156\156\156\156\156\156\156\156\156\156\156\156\156\156" +
		"\156\156\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\10\17\10\17\45\17\57" +
		"\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232\45\45\111\253\45\212" +
		"\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305\305" +
		"\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305" +
		"\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305\111" +
		"\u014f\u014f\111\11\11\11\11\11\11\11\11\11\11\11\11\11\11\12\12\12\12\12\12\12\12" +
		"\12\12\12\12\12\12\13\13\13\13\13\13\u0149\u011f\36\306\306\306\306\306\306\306\306" +
		"\306\306\306\306\306\306\306\306\306\306\306\306\306\306\306\306\306\306\306\306" +
		"\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\143\45\126\45\111\212" +
		"\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45" +
		"\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305" +
		"\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f" +
		"\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45" +
		"\126\10\10\75\45\126\45\111\212\232\236\45\45\111\253\45\212\212\111\267\111\45\305" +
		"\337\111\45\45\236\236\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305" +
		"\236\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337" +
		"\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111" +
		"\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232" +
		"\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\u0100" +
		"\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f" +
		"\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f" +
		"\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75" +
		"\111\45\126\10\10\75\45\126\45\111\212\232\45\45\111\253\45\212\212\111\267\111\45" +
		"\305\337\111\45\45\212\45\45\111\10\u0101\45\305\45\305\305\305\305\45\45\305\305" +
		"\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337" +
		"\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111" +
		"\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232" +
		"\237\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45\45\237\237\212\45\45" +
		"\111\10\45\305\45\305\305\305\305\45\45\305\305\305\237\253\111\111\111\111\45\305" +
		"\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45" +
		"\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10" +
		"\45\45\75\111\45\126\10\10\75\45\126\45\111\212\216\232\45\45\111\253\45\212\212" +
		"\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45" +
		"\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305" +
		"\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f" +
		"\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111" +
		"\212\217\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45" +
		"\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111\111\45\305\305" +
		"\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111" +
		"\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45" +
		"\45\75\111\45\126\10\10\75\45\126\45\111\212\220\232\45\45\111\253\45\212\212\111" +
		"\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305" +
		"\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305" +
		"\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f" +
		"\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212" +
		"\221\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111" +
		"\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f" +
		"\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f" +
		"\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75" +
		"\111\45\126\10\10\75\45\126\45\111\212\222\232\45\45\111\253\45\212\212\111\267\111" +
		"\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305" +
		"\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111" +
		"\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10" +
		"\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\223\232" +
		"\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305" +
		"\45\305\305\305\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10" +
		"\305\305\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f" +
		"\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45" +
		"\126\10\10\75\45\126\45\111\212\232\45\45\111\253\45\212\212\111\267\270\111\45\305" +
		"\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111" +
		"\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305" +
		"\305\111\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17" +
		"\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232\45\45\111\253" +
		"\45\212\212\111\267\271\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305" +
		"\305\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305" +
		"\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111" +
		"\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10" +
		"\75\45\126\45\111\212\224\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111" +
		"\45\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111" +
		"\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111" +
		"\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57" +
		"\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\225\232\45\45\111\253\45" +
		"\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305" +
		"\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305" +
		"\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305" +
		"\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45" +
		"\126\45\111\212\226\232\45\45\111\253\45\212\212\111\267\226\226\111\45\305\337\111" +
		"\45\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111" +
		"\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111" +
		"\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57" +
		"\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\227\232\45\45\111\253\45" +
		"\212\212\111\267\227\227\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305" +
		"\305\305\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305" +
		"\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305" +
		"\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10" +
		"\10\75\45\126\45\111\212\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111" +
		"\45\45\212\363\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111" +
		"\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305" +
		"\111\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45" +
		"\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232\45\45\111\253" +
		"\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305" +
		"\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305" +
		"\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305" +
		"\u0190\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10" +
		"\75\45\126\45\111\212\232\240\45\45\111\253\45\212\212\111\267\111\45\305\337\343" +
		"\111\45\45\240\240\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\240" +
		"\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111" +
		"\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305\u0191\111\u014f\u014f\111" +
		"\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232" +
		"\241\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45\45\241\241\212\45\45" +
		"\111\10\45\305\45\305\305\305\305\45\45\305\305\305\241\253\111\111\111\111\45\305" +
		"\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111\111\45\45" +
		"\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10" +
		"\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232\242\45\45\111\253\45\212\212" +
		"\111\267\111\45\305\337\111\45\45\242\242\212\45\45\111\10\45\305\45\305\305\305" +
		"\305\45\45\305\305\305\242\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305" +
		"\305\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111" +
		"\305\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10\10" +
		"\75\45\126\45\111\212\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45" +
		"\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111" +
		"\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111" +
		"\111\45\45\111\u014f\u014f\10\305\111\305\u0192\111\u014f\u014f\111\10\17\10\17\45" +
		"\17\57\61\63\10\45\45\75\111\45\126\10\10\75\45\126\45\111\212\232\45\45\111\253" +
		"\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305" +
		"\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305" +
		"\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305" +
		"\u0193\111\u014f\u014f\111\2\10\17\10\17\45\17\57\61\63\10\45\45\75\111\45\126\10" +
		"\10\75\45\126\45\111\212\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111" +
		"\45\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111" +
		"\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111" +
		"\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\32\45\17" +
		"\57\61\63\10\45\45\75\111\45\32\126\10\10\75\45\126\45\111\212\232\45\45\111\253" +
		"\45\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305" +
		"\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305" +
		"\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305" +
		"\111\u014f\u014f\111\10\17\10\17\45\17\57\61\63\10\45\45\75\105\111\45\126\10\10" +
		"\75\45\126\45\111\212\232\45\45\111\253\45\212\212\111\267\111\45\305\337\111\45" +
		"\45\212\45\45\111\10\45\305\45\305\305\305\305\45\45\305\305\305\253\111\111\111" +
		"\111\45\305\305\u014f\305\10\305\305\305\305\305\305\305\10\337\111\305\305\305\111" +
		"\111\45\45\111\u014f\u014f\10\305\111\305\111\u014f\u014f\111\10\17\10\17\45\17\57" +
		"\61\63\10\45\45\75\111\45\126\10\137\10\75\45\126\45\111\212\232\45\45\111\253\45" +
		"\212\212\111\267\111\45\305\337\111\45\45\212\45\45\111\10\45\305\45\305\305\305" +
		"\305\45\45\305\305\305\253\111\111\111\111\45\305\305\u014f\305\10\305\305\305\305" +
		"\305\305\305\10\337\111\305\305\305\111\111\45\45\111\u014f\u014f\10\305\111\305" +
		"\111\u014f\u014f\111\307\307\307\307\307\307\307\307\307\307\307\307\307\307\307" +
		"\307\307\307\307\307\307\277\22\u01a3\37\3\64\110\33\33\40\65\46\46\46\46\127\144" +
		"\127\157\157\251\144\276\310\46\345\345\371\372\46\310\u0108\310\310\310\310\u0124" +
		"\345\310\310\310\46\310\310\u0153\u0153\310\310\310\310\310\310\46\310\310\310\46" +
		"\46\310\310\14\14\14\14\14\311\14\311\311\311\311\311\311\311\311\311\311\u0150\311" +
		"\311\311\311\311\311\311\311\311\u0163\311\311\311\u0150\u0150\u0181\311\311\u0150" +
		"\u0150\112\170\252\264\274\344\374\264\112\u0140\u0141\u0168\112\112\274\112\374" +
		"\u019e\151\243\243\243\243\213\260\214\214\262\262\175\263\130\131\153\132\132\133" +
		"\133\361\u0172\u0194\134\134\145\135\135\254\u0133\146\255\160\161\246\162\162\244" +
		"\352\354\u0132\u0129\u0129\u0129\230\163\163\164\164\265\266\u0136\275\u017c\u0169" +
		"\312\312\312\312\312\312\312\312\312\312\313\u0106\u012c\u012d\u012f\u014c\u016c" +
		"\u016e\u016f\u018f\314\314\314\314\314\314\u0157\314\314\314\314\315\315\315\315" +
		"\315\315\315\315\315\315\315\316\316\u0110\316\316\316\316\316\316\316\316\316\317" +
		"\317\317\317\317\317\317\317\317\317\317\317\317\317\317\317\317\317\317\317\317" +
		"\317\317\317\317\317\317\320\320\u0111\u0114\u0119\320\320\320\u0119\320\320\u0158" +
		"\u015c\u015f\320\320\320\u0186\320\321\u0107\u0112\u0115\321\321\321\321\321\u0159" +
		"\321\321\321\321\322\322\322\322\u011a\322\322\322\u011a\322\322\322\u011a\u011a" +
		"\u0162\322\322\322\u011a\322\323\323\323\323\323\323\323\323\323\323\323\323\323" +
		"\323\323\323\323\323\323\323\324\324\324\324\324\u0123\324\324\324\324\324\324\324" +
		"\324\324\u0123\324\324\324\324\324\324\325\325\325\325\325\325\325\325\325\325\325" +
		"\u0154\u0156\325\325\325\325\325\325\325\325\325\325\325\326\326\326\326\326\326" +
		"\326\326\326\326\326\326\326\326\326\326\326\326\326\326\326\326\326\326\327\327" +
		"\327\327\327\327\327\327\327\327\327\327\327\327\327\327\327\327\327\327\330\330" +
		"\330\330\330\330\330\330\330\330\330\330\330\330\330\330\330\330\330\330\330\330" +
		"\330\330\331\331\331\331\331\331\331\331\331\331\331\331\u0155\331\331\331\331\331" +
		"\331\331\331\331\331\331\331\332\367\370\332\332\332\332\332\332\332\332\u0137\332" +
		"\332\332\332\332\332\332\332\332\332\332\332\332\332\332\332\u0151\u0151\u0180\u0151" +
		"\u0151\u0152\u017f\u019b\u019c\165\165\165\165\165\165\165\165\165\165\165\165\165" +
		"\165\165\165\165\165\165\165\165\165\166\166\333\333\333\u0116\u0116\333\333\333" +
		"\u0116\333\333\u0116\u0116\u0116\u0116\333\333\333\u0116\333\167\167\250\167\167" +
		"\167\167\167\167\167\167\167\167\167\167\167\167\167\167\167\167\167\167\340\245" +
		"\355\341\u0166\47\70\71\47\342\u0103\u0142\342\u0103\u0103\50\51\27\27\27\52\115" +
		"\231\272\273\113\u013e\u0175\u0176\u018a\u0104\u0104\u0104\u0105\u017a\u017b\u01a4" +
		"\23\67\136\140\377\24\25\76\77\100\141\15\15\15\114\15\15\15\114\u0164\114\114\u0178" +
		"\u0179\114\20\30\54\21\21\21\334\334\334\334\334\334\334\334\334\334\334\334\334" +
		"\334\334\334\334\334\334\334\u0195\16\16\66\16\16\16\154\233\247\335\16\335\u011b" +
		"\335\335\335\u011b\335\335\u011b\u011b\335\335\335\u011b\335\34\121\152\362\u0135" +
		"\u0173\u0196\346\347\u0127\u012a\u012b\u0167\336\336\u0113\u0117\u0118\u011c\336" +
		"\336\336\u011c\336\336\u015a\u015b\u015d\u015e\u0160\u0161\336\336\336\u0185\u0187" +
		"\u0188\u0189\336\u019d");

	private static final short[] tmRuleLen = TMLexer.unpack_short(238,
		"\2\0\5\4\2\0\7\4\3\3\4\4\3\3\1\1\2\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\3\2\1\1\2\2" +
		"\1\1\1\1\3\10\3\2\3\1\1\1\1\5\3\1\3\1\3\1\1\2\2\1\1\1\7\6\6\5\6\5\5\4\2\4\3\3\1\1" +
		"\2\1\1\1\7\5\7\5\6\4\4\4\5\5\6\3\1\2\1\1\2\1\3\3\1\1\5\4\4\3\4\3\3\2\4\3\3\2\3\2" +
		"\2\1\1\3\2\3\3\4\3\1\2\2\1\1\1\1\2\1\3\3\1\2\1\3\3\3\1\3\1\3\6\6\2\2\4\1\4\2\1\3" +
		"\2\1\3\3\2\1\1\5\2\2\3\1\3\1\4\2\1\3\1\1\0\3\3\2\2\1\1\1\1\1\2\1\3\3\1\3\3\1\1\5" +
		"\3\1\3\1\1\0\3\1\1\0\3\1\1\1\1\1\1\3\1\1\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0");

	private static final short[] tmRuleSymbol = TMLexer.unpack_short(238,
		"\116\116\117\117\120\120\121\121\122\123\124\125\125\126\126\127\130\130\131\132" +
		"\132\133\133\134\134\134\134\134\134\134\134\134\134\134\134\134\135\136\136\136" +
		"\137\137\137\137\140\141\141\142\143\144\144\144\144\145\146\146\147\150\151\151" +
		"\152\152\152\153\153\153\154\154\154\154\154\154\154\154\155\155\155\155\155\155" +
		"\156\157\157\157\160\160\160\160\160\160\161\161\161\161\161\162\162\163\163\164" +
		"\164\165\165\166\166\167\170\170\170\170\170\170\170\170\170\170\170\170\170\170" +
		"\170\170\170\171\172\173\173\174\174\175\175\175\176\176\176\177\177\200\200\200" +
		"\201\201\202\202\202\203\204\204\205\205\205\205\205\205\205\205\206\207\207\207" +
		"\207\210\210\210\211\211\212\213\213\213\214\214\215\216\216\216\217\220\220\221" +
		"\221\222\223\223\223\223\224\224\225\225\226\226\226\226\227\227\227\230\230\230" +
		"\230\230\231\231\232\232\233\233\234\234\235\236\236\236\236\237\240\240\241\242" +
		"\243\243\244\244\245\245\246\246\247\247\250\250\251\251\252\252\253\253");

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
		int import__optlist = 78;
		int input = 79;
		int option_optlist = 80;
		int header = 81;
		int lexer_section = 82;
		int parser_section = 83;
		int parsing_algorithm = 84;
		int import_ = 85;
		int option = 86;
		int identifier = 87;
		int symref = 88;
		int symref_noargs = 89;
		int type = 90;
		int type_part_list = 91;
		int type_part = 92;
		int pattern = 93;
		int lexer_parts = 94;
		int lexer_part = 95;
		int named_pattern = 96;
		int lexeme = 97;
		int lexeme_transition = 98;
		int lexeme_attrs = 99;
		int lexeme_attribute = 100;
		int lexer_directive = 101;
		int lexer_state_list_Comma_separated = 102;
		int state_selector = 103;
		int stateref = 104;
		int lexer_state = 105;
		int grammar_parts = 106;
		int grammar_part = 107;
		int nonterm = 108;
		int nonterm_type = 109;
		int _implements = 110;
		int assoc = 111;
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
		int rhsAnnotated = 127;
		int rhsAssignment = 128;
		int rhsOptional = 129;
		int rhsCast = 130;
		int rhsUnordered = 131;
		int rhsClass = 132;
		int rhsPrimary = 133;
		int rhsSet = 134;
		int setPrimary = 135;
		int setExpression = 136;
		int annotation_list = 137;
		int annotations = 138;
		int annotation = 139;
		int nonterm_param_list_Comma_separated = 140;
		int nonterm_params = 141;
		int nonterm_param = 142;
		int param_ref = 143;
		int argument_list_Comma_separated = 144;
		int argument_list_Comma_separated_opt = 145;
		int symref_args = 146;
		int argument = 147;
		int param_type = 148;
		int param_value = 149;
		int predicate_primary = 150;
		int predicate_expression = 151;
		int expression = 152;
		int expression_list_Comma_separated = 153;
		int expression_list_Comma_separated_opt = 154;
		int map_entry_list_Comma_separated = 155;
		int map_entry_list_Comma_separated_opt = 156;
		int map_entry = 157;
		int literal = 158;
		int name = 159;
		int qualified_id = 160;
		int command = 161;
		int syntax_problem = 162;
		int parsing_algorithmopt = 163;
		int typeopt = 164;
		int lexeme_transitionopt = 165;
		int iconopt = 166;
		int lexeme_attrsopt = 167;
		int commandopt = 168;
		int identifieropt = 169;
		int implementsopt = 170;
		int rhsSuffixopt = 171;
	}

	public interface Rules {
		int lexer_directive_directiveBrackets = 53;  // lexer_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
		int nonterm_type_nontermTypeAST = 74;  // nonterm_type ::= Lreturns symref_noargs
		int nonterm_type_nontermTypeHint = 75;  // nonterm_type ::= Linline Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint2 = 76;  // nonterm_type ::= Lclass identifieropt implementsopt
		int nonterm_type_nontermTypeHint3 = 77;  // nonterm_type ::= Linterface identifieropt implementsopt
		int nonterm_type_nontermTypeHint4 = 78;  // nonterm_type ::= Lvoid
		int nonterm_type_nontermTypeRaw = 79;  // nonterm_type ::= type
		int directive_directivePrio = 90;  // directive ::= '%' assoc references ';'
		int directive_directiveInput = 91;  // directive ::= '%' Linput inputref_list_Comma_separated ';'
		int directive_directiveAssert = 92;  // directive ::= '%' Lassert Lempty rhsSet ';'
		int directive_directiveAssert2 = 93;  // directive ::= '%' Lassert Lnonempty rhsSet ';'
		int directive_directiveSet = 94;  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
		int rhsOptional_rhsQuantifier = 141;  // rhsOptional ::= rhsCast '?'
		int rhsCast_rhsAsLiteral = 144;  // rhsCast ::= rhsClass Las literal
		int rhsPrimary_rhsSymbol = 148;  // rhsPrimary ::= symref
		int rhsPrimary_rhsNested = 149;  // rhsPrimary ::= '(' rules ')'
		int rhsPrimary_rhsList = 150;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		int rhsPrimary_rhsList2 = 151;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		int rhsPrimary_rhsQuantifier = 152;  // rhsPrimary ::= rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 153;  // rhsPrimary ::= rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 154;  // rhsPrimary ::= '$' '(' rules ')'
		int setPrimary_setSymbol = 157;  // setPrimary ::= ID symref
		int setPrimary_setSymbol2 = 158;  // setPrimary ::= symref
		int setPrimary_setCompound = 159;  // setPrimary ::= '(' setExpression ')'
		int setPrimary_setComplement = 160;  // setPrimary ::= '~' setPrimary
		int setExpression_setBinary = 162;  // setExpression ::= setExpression '|' setExpression
		int setExpression_setBinary2 = 163;  // setExpression ::= setExpression '&' setExpression
		int nonterm_param_inlineParameter = 174;  // nonterm_param ::= ID identifier '=' param_value
		int nonterm_param_inlineParameter2 = 175;  // nonterm_param ::= ID identifier
		int predicate_primary_boolPredicate = 190;  // predicate_primary ::= '!' param_ref
		int predicate_primary_boolPredicate2 = 191;  // predicate_primary ::= param_ref
		int predicate_primary_comparePredicate = 192;  // predicate_primary ::= param_ref '==' literal
		int predicate_primary_comparePredicate2 = 193;  // predicate_primary ::= param_ref '!=' literal
		int predicate_expression_predicateBinary = 195;  // predicate_expression ::= predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 196;  // predicate_expression ::= predicate_expression '||' predicate_expression
		int expression_instance = 199;  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
		int expression_array = 200;  // expression ::= '[' expression_list_Comma_separated_opt ']'
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
			case 84:  // template_param ::= '%' Lexplicit param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						true /* explicit */,
						false /* global */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 85:  // template_param ::= '%' Lexplicit param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						true /* explicit */,
						false /* global */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 86:  // template_param ::= '%' Lglobal param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						false /* explicit */,
						true /* global */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 87:  // template_param ::= '%' Lglobal param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						false /* explicit */,
						true /* global */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 88:  // template_param ::= '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						false /* explicit */,
						false /* global */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // template_param ::= '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						false /* explicit */,
						false /* global */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // directive ::= '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // directive ::= '%' Linput inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // directive ::= '%' Lassert Lempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // directive ::= '%' Lassert Lnonempty rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.LNONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((String)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 95:  // inputref_list_Comma_separated ::= inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 96:  // inputref_list_Comma_separated ::= inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 97:  // inputref ::= symref_noargs Lnoeoi
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 98:  // inputref ::= symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // references ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 100:  // references ::= references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 101:  // references_cs ::= symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 102:  // references_cs ::= references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 103:  // rule0_list_Or_separated ::= rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 104:  // rule0_list_Or_separated ::= rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 106:  // rule0 ::= predicate rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 4].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // rule0 ::= predicate rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rule0 ::= predicate rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // rule0 ::= predicate rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // rule0 ::= predicate rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // rule0 ::= predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // rule0 ::= predicate ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rule0 ::= predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // rule0 ::= rhsPrefix rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 3].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // rule0 ::= rhsPrefix ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // rule0 ::= rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // rule0 ::= rhsParts ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 119:  // rule0 ::= rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // rule0 ::= ruleAction rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						((TmaRuleAction)tmStack[tmHead - 1].value) /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 121:  // rule0 ::= rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // rule0 ::= syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* prefix */,
						null /* list */,
						null /* action */,
						null /* suffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // predicate ::= '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 124:  // rhsPrefix ::= annotations ':'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 125:  // rhsSuffix ::= '%' Lprec symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 126:  // rhsSuffix ::= '%' Lshift symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // ruleAction ::= '{~' identifier scon '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((String)tmStack[tmHead - 1].value) /* parameter */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // ruleAction ::= '{~' identifier '}'
				tmLeft.value = new TmaRuleAction(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* action */,
						null /* parameter */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // rhsParts ::= rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 130:  // rhsParts ::= rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 131:  // rhsParts ::= rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 136:  // rhsAnnotated ::= annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // rhsAssignment ::= identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // rhsAssignment ::= identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rhsOptional ::= rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUESTIONMARK /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // rhsCast ::= rhsClass Las symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // rhsCast ::= rhsClass Las literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // rhsUnordered ::= rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // rhsClass ::= identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // rhsPrimary ::= symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 149:  // rhsPrimary ::= '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 150:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // rhsPrimary ::= rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rhsPrimary ::= rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rhsPrimary ::= '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rhsSet ::= Lset '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // setPrimary ::= ID symref
				tmLeft.value = new TmaSetSymbol(
						((String)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // setPrimary ::= symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // setPrimary ::= '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // setPrimary ::= '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // setExpression ::= setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // setExpression ::= setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AMPERSAND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 165:  // annotation_list ::= annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 166:  // annotations ::= annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 167:  // annotation ::= '@' ID '{' expression '}'
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 3].value) /* name */,
						((ITmaExpression)tmStack[tmHead - 1].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 168:  // annotation ::= '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 169:  // annotation ::= '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 170:  // nonterm_param_list_Comma_separated ::= nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 171:  // nonterm_param_list_Comma_separated ::= nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 172:  // nonterm_params ::= '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 174:  // nonterm_param ::= ID identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 175:  // nonterm_param ::= ID identifier
				tmLeft.value = new TmaInlineParameter(
						((String)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 176:  // param_ref ::= identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 177:  // argument_list_Comma_separated ::= argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 178:  // argument_list_Comma_separated ::= argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 181:  // symref_args ::= '<' argument_list_Comma_separated_opt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 182:  // argument ::= param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 183:  // argument ::= '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 184:  // argument ::= '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // argument ::= param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // param_type ::= Lflag
				tmLeft.value = TmaParamType.LFLAG;
				break;
			case 187:  // param_type ::= Lparam
				tmLeft.value = TmaParamType.LPARAM;
				break;
			case 190:  // predicate_primary ::= '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // predicate_primary ::= param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // predicate_primary ::= param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EQUAL_EQUAL /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // predicate_primary ::= param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCLAMATION_EQUAL /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // predicate_expression ::= predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AMPERSAND_AMPERSAND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // predicate_expression ::= predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 199:  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntry>)tmStack[tmHead - 1].value) /* entries */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 200:  // expression ::= '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // expression_list_Comma_separated ::= expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 203:  // expression_list_Comma_separated ::= expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 206:  // map_entry_list_Comma_separated ::= map_entry_list_Comma_separated ',' map_entry
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 207:  // map_entry_list_Comma_separated ::= map_entry
				tmLeft.value = new ArrayList();
				((List<TmaMapEntry>)tmLeft.value).add(((TmaMapEntry)tmStack[tmHead].value));
				break;
			case 210:  // map_entry ::= ID ':' expression
				tmLeft.value = new TmaMapEntry(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 211:  // literal ::= scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 212:  // literal ::= icon
				tmLeft.value = new TmaLiteral(
						((Integer)tmStack[tmHead].value) /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 213:  // literal ::= Ltrue
				tmLeft.value = new TmaLiteral(
						true /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 214:  // literal ::= Lfalse
				tmLeft.value = new TmaLiteral(
						false /* value */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 215:  // name ::= qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 217:  // qualified_id ::= qualified_id '.' ID
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 218:  // command ::= code
				tmLeft.value = new TmaCommand(
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 219:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 421);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 422);
	}
}
