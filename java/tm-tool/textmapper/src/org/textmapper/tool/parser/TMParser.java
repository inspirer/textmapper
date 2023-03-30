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
	private static final int[] tmAction = TMLexer.unpack_int(455,
		"\uffff\uffff\uffff\uffff\uffff\uffff\54\0\41\0\42\0\uffff\uffff\52\0\0\0\44\0\43" +
		"\0\13\0\1\0\30\0\14\0\26\0\27\0\17\0\22\0\12\0\16\0\2\0\6\0\31\0\36\0\35\0\34\0\7" +
		"\0\37\0\20\0\23\0\11\0\15\0\21\0\40\0\3\0\5\0\10\0\24\0\4\0\33\0\32\0\25\0\ufffd" +
		"\uffff\361\0\367\0\362\0\46\0\uff89\uffff\uffff\uffff\uff7f\uffff\uffff\uffff\366" +
		"\0\371\0\uffff\uffff\uff35\uffff\71\0\uffff\uffff\62\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\53\0\365\0\uffff\uffff\364\0\uffff\uffff\uffff\uffff\334\0\ufeeb\uffff\ufee3" +
		"\uffff\uffff\uffff\336\0\47\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\70\0\ufedd\uffff\57\0\363\0\370\0\343\0\344\0\uffff\uffff\uffff\uffff\341" +
		"\0\uffff\uffff\66\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\55\0\73\0\350" +
		"\0\351\0\342\0\335\0\61\0\65\0\uffff\uffff\uffff\uffff\ufed7\uffff\ufecf\uffff\75" +
		"\0\100\0\104\0\uffff\uffff\101\0\103\0\102\0\67\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\132\0\uffff\uffff\112\0\uffff\uffff\74\0\372\0\uffff\uffff" +
		"\77\0\76\0\uffff\uffff\ufe81\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufe7b\uffff" +
		"\134\0\137\0\140\0\141\0\ufe2f\uffff\uffff\uffff\321\0\uffff\uffff\133\0\uffff\uffff" +
		"\127\0\uffff\uffff\107\0\uffff\uffff\110\0\45\0\105\0\ufde3\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\174\0\346\0\uffff\uffff\175\0\uffff\uffff" +
		"\uffff\uffff\171\0\176\0\173\0\347\0\172\0\uffff\uffff\uffff\uffff\uffff\uffff\ufd8f" +
		"\uffff\325\0\ufd41\uffff\uffff\uffff\uffff\uffff\ufce3\uffff\uffff\uffff\165\0\uffff" +
		"\uffff\166\0\167\0\uffff\uffff\uffff\uffff\uffff\uffff\136\0\135\0\320\0\uffff\uffff" +
		"\uffff\uffff\130\0\uffff\uffff\131\0\111\0\uffff\uffff\ufcdb\uffff\117\0\106\0\ufc85" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufc31\uffff\uffff" +
		"\uffff\216\0\214\0\uffff\uffff\221\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufc29\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\51\0\ufbcb\uffff\255\0\240\0\300\0\ufb5d\uffff\uffff\uffff\226\0\ufb55\uffff\u0104" +
		"\0\ufaf9\uffff\251\0\257\0\256\0\254\0\266\0\270\0\ufa9b\uffff\ufa39\uffff\307\0" +
		"\uffff\uffff\uf9d1\uffff\uf9c7\uffff\uf9b9\uffff\uffff\uffff\327\0\331\0\uffff\uffff" +
		"\u0102\0\164\0\uf971\uffff\162\0\uf969\uffff\uffff\uffff\uf90b\uffff\uf8ad\uffff" +
		"\uffff\uffff\uffff\uffff\uf84f\uffff\uffff\uffff\uffff\uffff\uffff\uffff\125\0\126" +
		"\0\122\0\124\0\123\0\uffff\uffff\374\0\uf7f1\uffff\uf79d\uffff\114\0\uffff\uffff" +
		"\uffff\uffff\211\0\212\0\uffff\uffff\217\0\204\0\uffff\uffff\205\0\uffff\uffff\203" +
		"\0\222\0\uffff\uffff\uffff\uffff\202\0\323\0\uffff\uffff\uffff\uffff\265\0\uffff" +
		"\uffff\uf747\uffff\356\0\uffff\uffff\uffff\uffff\uf73b\uffff\uffff\uffff\264\0\uffff" +
		"\uffff\261\0\uf6dd\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf67f\uffff\161\0\uf61f" +
		"\uffff\uf5c1\uffff\253\0\252\0\uf5b7\uffff\274\0\304\0\305\0\uffff\uffff\267\0\236" +
		"\0\uffff\uffff\uffff\uffff\246\0\uf5ad\uffff\uffff\uffff\330\0\223\0\uf5a5\uffff" +
		"\163\0\uffff\uffff\uf59d\uffff\uffff\uffff\uffff\uffff\uf53f\uffff\uffff\uffff\uf4e1" +
		"\uffff\uffff\uffff\uf483\uffff\uffff\uffff\uf425\uffff\uf3c7\uffff\uffff\uffff\uffff" +
		"\uffff\121\0\376\0\uf369\uffff\uf317\uffff\206\0\207\0\uffff\uffff\215\0\213\0\uffff" +
		"\uffff\200\0\uffff\uffff\242\0\243\0\352\0\uffff\uffff\uffff\uffff\uffff\uffff\241" +
		"\0\uffff\uffff\301\0\uffff\uffff\263\0\262\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uf2c3\uffff\312\0\315\0\uffff\uffff\271\0\272\0\225\0\uf277\uffff\232" +
		"\0\234\0\277\0\276\0\250\0\uf26d\uffff\uffff\uffff\326\0\uffff\uffff\157\0\uffff" +
		"\uffff\160\0\155\0\uffff\uffff\uf25f\uffff\uffff\uffff\151\0\uffff\uffff\uf201\uffff" +
		"\uffff\uffff\uffff\uffff\uf1a3\uffff\uffff\uffff\uf145\uffff\u0100\0\116\0\uf0e7" +
		"\uffff\210\0\uffff\uffff\201\0\354\0\355\0\uf095\uffff\uf08d\uffff\uffff\uffff\260" +
		"\0\306\0\uffff\uffff\314\0\311\0\uffff\uffff\310\0\uffff\uffff\230\0\244\0\332\0" +
		"\224\0\156\0\153\0\uffff\uffff\154\0\147\0\uffff\uffff\150\0\145\0\uffff\uffff\uf085" +
		"\uffff\uffff\uffff\113\0\177\0\uffff\uffff\313\0\uf027\uffff\uf01f\uffff\152\0\146" +
		"\0\143\0\uffff\uffff\144\0\303\0\302\0\142\0\uffff\uffff\uffff\uffff\ufffe\uffff" +
		"\ufffe\uffff");

	private static final int[] tmLalr = TMLexer.unpack_int(4070,
		"\30\0\uffff\uffff\0\0\72\0\6\0\72\0\7\0\72\0\10\0\72\0\15\0\72\0\16\0\72\0\17\0\72" +
		"\0\22\0\72\0\23\0\72\0\24\0\72\0\25\0\72\0\26\0\72\0\32\0\72\0\33\0\72\0\35\0\72" +
		"\0\40\0\72\0\42\0\72\0\43\0\72\0\44\0\72\0\45\0\72\0\46\0\72\0\52\0\72\0\53\0\72" +
		"\0\55\0\72\0\56\0\72\0\57\0\72\0\60\0\72\0\61\0\72\0\62\0\72\0\63\0\72\0\64\0\72" +
		"\0\65\0\72\0\66\0\72\0\67\0\72\0\70\0\72\0\71\0\72\0\72\0\72\0\73\0\72\0\74\0\72" +
		"\0\75\0\72\0\76\0\72\0\77\0\72\0\100\0\72\0\101\0\72\0\102\0\72\0\103\0\72\0\104" +
		"\0\72\0\105\0\72\0\106\0\72\0\107\0\72\0\110\0\72\0\111\0\72\0\112\0\72\0\113\0\72" +
		"\0\114\0\72\0\115\0\72\0\uffff\uffff\ufffe\uffff\16\0\uffff\uffff\15\0\50\0\23\0" +
		"\50\0\26\0\50\0\uffff\uffff\ufffe\uffff\51\0\uffff\uffff\7\0\60\0\44\0\60\0\45\0" +
		"\60\0\55\0\60\0\56\0\60\0\57\0\60\0\60\0\60\0\61\0\60\0\62\0\60\0\63\0\60\0\64\0" +
		"\60\0\65\0\60\0\66\0\60\0\67\0\60\0\70\0\60\0\71\0\60\0\72\0\60\0\73\0\60\0\74\0" +
		"\60\0\75\0\60\0\76\0\60\0\77\0\60\0\100\0\60\0\101\0\60\0\102\0\60\0\103\0\60\0\104" +
		"\0\60\0\105\0\60\0\106\0\60\0\107\0\60\0\110\0\60\0\111\0\60\0\112\0\60\0\113\0\60" +
		"\0\114\0\60\0\uffff\uffff\ufffe\uffff\33\0\uffff\uffff\37\0\uffff\uffff\45\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\31\0\340\0\uffff\uffff" +
		"\ufffe\uffff\20\0\uffff\uffff\17\0\345\0\31\0\345\0\uffff\uffff\ufffe\uffff\17\0" +
		"\uffff\uffff\31\0\337\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\0\0\56\0\uffff\uffff" +
		"\ufffe\uffff\12\0\uffff\uffff\115\0\uffff\uffff\20\0\373\0\uffff\uffff\ufffe\uffff" +
		"\6\0\uffff\uffff\30\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\0\0\63\0\7\0\63\0\uffff\uffff\ufffe\uffff" +
		"\115\0\uffff\uffff\20\0\373\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\43\0\uffff" +
		"\uffff\44\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\0\0\64\0\uffff\uffff\ufffe\uffff\43\0\uffff\uffff\23\0\322\0\42\0" +
		"\322\0\45\0\322\0\53\0\322\0\55\0\322\0\56\0\322\0\57\0\322\0\60\0\322\0\61\0\322" +
		"\0\62\0\322\0\63\0\322\0\64\0\322\0\65\0\322\0\66\0\322\0\67\0\322\0\70\0\322\0\71" +
		"\0\322\0\72\0\322\0\73\0\322\0\74\0\322\0\75\0\322\0\76\0\322\0\77\0\322\0\100\0" +
		"\322\0\101\0\322\0\102\0\322\0\103\0\322\0\104\0\322\0\105\0\322\0\106\0\322\0\107" +
		"\0\322\0\110\0\322\0\111\0\322\0\112\0\322\0\113\0\322\0\114\0\322\0\uffff\uffff" +
		"\ufffe\uffff\23\0\uffff\uffff\117\0\uffff\uffff\0\0\120\0\6\0\120\0\7\0\120\0\27" +
		"\0\120\0\30\0\120\0\44\0\120\0\45\0\120\0\55\0\120\0\56\0\120\0\57\0\120\0\60\0\120" +
		"\0\61\0\120\0\62\0\120\0\63\0\120\0\64\0\120\0\65\0\120\0\66\0\120\0\67\0\120\0\70" +
		"\0\120\0\71\0\120\0\72\0\120\0\73\0\120\0\74\0\120\0\75\0\120\0\76\0\120\0\77\0\120" +
		"\0\100\0\120\0\101\0\120\0\102\0\120\0\103\0\120\0\104\0\120\0\105\0\120\0\106\0" +
		"\120\0\107\0\120\0\110\0\120\0\111\0\120\0\112\0\120\0\113\0\120\0\114\0\120\0\uffff" +
		"\uffff\ufffe\uffff\12\0\uffff\uffff\23\0\324\0\42\0\324\0\43\0\324\0\45\0\324\0\53" +
		"\0\324\0\55\0\324\0\56\0\324\0\57\0\324\0\60\0\324\0\61\0\324\0\62\0\324\0\63\0\324" +
		"\0\64\0\324\0\65\0\324\0\66\0\324\0\67\0\324\0\70\0\324\0\71\0\324\0\72\0\324\0\73" +
		"\0\324\0\74\0\324\0\75\0\324\0\76\0\324\0\77\0\324\0\100\0\324\0\101\0\324\0\102" +
		"\0\324\0\103\0\324\0\104\0\324\0\105\0\324\0\106\0\324\0\107\0\324\0\110\0\324\0" +
		"\111\0\324\0\112\0\324\0\113\0\324\0\114\0\324\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff" +
		"\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff" +
		"\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff" +
		"\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff" +
		"\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff" +
		"\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0" +
		"\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff" +
		"\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105" +
		"\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\50\0\uffff\uffff\20\0\u0103\0\25\0\u0103" +
		"\0\uffff\uffff\ufffe\uffff\2\0\uffff\uffff\0\0\375\0\6\0\375\0\7\0\375\0\23\0\375" +
		"\0\27\0\375\0\30\0\375\0\44\0\375\0\45\0\375\0\55\0\375\0\56\0\375\0\57\0\375\0\60" +
		"\0\375\0\61\0\375\0\62\0\375\0\63\0\375\0\64\0\375\0\65\0\375\0\66\0\375\0\67\0\375" +
		"\0\70\0\375\0\71\0\375\0\72\0\375\0\73\0\375\0\74\0\375\0\75\0\375\0\76\0\375\0\77" +
		"\0\375\0\100\0\375\0\101\0\375\0\102\0\375\0\103\0\375\0\104\0\375\0\105\0\375\0" +
		"\106\0\375\0\107\0\375\0\110\0\375\0\111\0\375\0\112\0\375\0\113\0\375\0\114\0\375" +
		"\0\115\0\375\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\117\0\uffff\uffff\0\0\115" +
		"\0\6\0\115\0\7\0\115\0\27\0\115\0\30\0\115\0\44\0\115\0\45\0\115\0\55\0\115\0\56" +
		"\0\115\0\57\0\115\0\60\0\115\0\61\0\115\0\62\0\115\0\63\0\115\0\64\0\115\0\65\0\115" +
		"\0\66\0\115\0\67\0\115\0\70\0\115\0\71\0\115\0\72\0\115\0\73\0\115\0\74\0\115\0\75" +
		"\0\115\0\76\0\115\0\77\0\115\0\100\0\115\0\101\0\115\0\102\0\115\0\103\0\115\0\104" +
		"\0\115\0\105\0\115\0\106\0\115\0\107\0\115\0\110\0\115\0\111\0\115\0\112\0\115\0" +
		"\113\0\115\0\114\0\115\0\uffff\uffff\ufffe\uffff\100\0\uffff\uffff\15\0\220\0\17" +
		"\0\220\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0105\0\25\0\u0105\0\26\0\u0105\0\uffff\uffff\ufffe" +
		"\uffff\12\0\uffff\uffff\30\0\uffff\uffff\34\0\uffff\uffff\6\0\72\0\10\0\72\0\15\0" +
		"\72\0\16\0\72\0\23\0\72\0\24\0\72\0\25\0\72\0\26\0\72\0\32\0\72\0\33\0\72\0\35\0" +
		"\72\0\42\0\72\0\43\0\72\0\44\0\72\0\45\0\72\0\46\0\72\0\52\0\72\0\53\0\72\0\55\0" +
		"\72\0\56\0\72\0\57\0\72\0\60\0\72\0\61\0\72\0\62\0\72\0\63\0\72\0\64\0\72\0\65\0" +
		"\72\0\66\0\72\0\67\0\72\0\70\0\72\0\71\0\72\0\72\0\72\0\73\0\72\0\74\0\72\0\75\0" +
		"\72\0\76\0\72\0\77\0\72\0\100\0\72\0\101\0\72\0\102\0\72\0\103\0\72\0\104\0\72\0" +
		"\105\0\72\0\106\0\72\0\107\0\72\0\110\0\72\0\111\0\72\0\112\0\72\0\113\0\72\0\114" +
		"\0\72\0\115\0\72\0\uffff\uffff\ufffe\uffff\10\0\uffff\uffff\15\0\227\0\26\0\227\0" +
		"\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff" +
		"\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff" +
		"\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff" +
		"\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff" +
		"\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0" +
		"\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff" +
		"\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105" +
		"\0\25\0\u0105\0\26\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff" +
		"\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\26\0\u0105\0\uffff" +
		"\uffff\ufffe\uffff\35\0\uffff\uffff\6\0\273\0\10\0\273\0\15\0\273\0\16\0\273\0\23" +
		"\0\273\0\24\0\273\0\25\0\273\0\26\0\273\0\42\0\273\0\43\0\273\0\44\0\273\0\45\0\273" +
		"\0\52\0\273\0\53\0\273\0\55\0\273\0\56\0\273\0\57\0\273\0\60\0\273\0\61\0\273\0\62" +
		"\0\273\0\63\0\273\0\64\0\273\0\65\0\273\0\66\0\273\0\67\0\273\0\70\0\273\0\71\0\273" +
		"\0\72\0\273\0\73\0\273\0\74\0\273\0\75\0\273\0\76\0\273\0\77\0\273\0\100\0\273\0" +
		"\101\0\273\0\102\0\273\0\103\0\273\0\104\0\273\0\105\0\273\0\106\0\273\0\107\0\273" +
		"\0\110\0\273\0\111\0\273\0\112\0\273\0\113\0\273\0\114\0\273\0\115\0\273\0\uffff" +
		"\uffff\ufffe\uffff\32\0\uffff\uffff\33\0\uffff\uffff\46\0\uffff\uffff\6\0\275\0\10" +
		"\0\275\0\15\0\275\0\16\0\275\0\23\0\275\0\24\0\275\0\25\0\275\0\26\0\275\0\35\0\275" +
		"\0\42\0\275\0\43\0\275\0\44\0\275\0\45\0\275\0\52\0\275\0\53\0\275\0\55\0\275\0\56" +
		"\0\275\0\57\0\275\0\60\0\275\0\61\0\275\0\62\0\275\0\63\0\275\0\64\0\275\0\65\0\275" +
		"\0\66\0\275\0\67\0\275\0\70\0\275\0\71\0\275\0\72\0\275\0\73\0\275\0\74\0\275\0\75" +
		"\0\275\0\76\0\275\0\77\0\275\0\100\0\275\0\101\0\275\0\102\0\275\0\103\0\275\0\104" +
		"\0\275\0\105\0\275\0\106\0\275\0\107\0\275\0\110\0\275\0\111\0\275\0\112\0\275\0" +
		"\113\0\275\0\114\0\275\0\115\0\275\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\10" +
		"\0\237\0\15\0\237\0\26\0\237\0\uffff\uffff\ufffe\uffff\46\0\uffff\uffff\120\0\uffff" +
		"\uffff\10\0\247\0\15\0\247\0\20\0\247\0\26\0\247\0\uffff\uffff\ufffe\uffff\45\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\17\0\334\0\31\0\334" +
		"\0\uffff\uffff\ufffe\uffff\50\0\uffff\uffff\20\0\u0103\0\25\0\u0103\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105" +
		"\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\23\0\uffff" +
		"\uffff\0\0\377\0\6\0\377\0\7\0\377\0\27\0\377\0\30\0\377\0\44\0\377\0\45\0\377\0" +
		"\55\0\377\0\56\0\377\0\57\0\377\0\60\0\377\0\61\0\377\0\62\0\377\0\63\0\377\0\64" +
		"\0\377\0\65\0\377\0\66\0\377\0\67\0\377\0\70\0\377\0\71\0\377\0\72\0\377\0\73\0\377" +
		"\0\74\0\377\0\75\0\377\0\76\0\377\0\77\0\377\0\100\0\377\0\101\0\377\0\102\0\377" +
		"\0\103\0\377\0\104\0\377\0\105\0\377\0\106\0\377\0\107\0\377\0\110\0\377\0\111\0" +
		"\377\0\112\0\377\0\113\0\377\0\114\0\377\0\115\0\377\0\uffff\uffff\ufffe\uffff\2" +
		"\0\uffff\uffff\0\0\375\0\6\0\375\0\7\0\375\0\23\0\375\0\27\0\375\0\30\0\375\0\44" +
		"\0\375\0\45\0\375\0\55\0\375\0\56\0\375\0\57\0\375\0\60\0\375\0\61\0\375\0\62\0\375" +
		"\0\63\0\375\0\64\0\375\0\65\0\375\0\66\0\375\0\67\0\375\0\70\0\375\0\71\0\375\0\72" +
		"\0\375\0\73\0\375\0\74\0\375\0\75\0\375\0\76\0\375\0\77\0\375\0\100\0\375\0\101\0" +
		"\375\0\102\0\375\0\103\0\375\0\104\0\375\0\105\0\375\0\106\0\375\0\107\0\375\0\110" +
		"\0\375\0\111\0\375\0\112\0\375\0\113\0\375\0\114\0\375\0\115\0\375\0\uffff\uffff" +
		"\ufffe\uffff\13\0\uffff\uffff\14\0\uffff\uffff\11\0\353\0\22\0\353\0\41\0\353\0\uffff" +
		"\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff" +
		"\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\52\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0105\0\25\0\u0105\0\26\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\25\0\u0105" +
		"\0\26\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\26\0\u0105" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\23\0\uffff\uffff\24\0" +
		"\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\26\0\u0105\0\uffff\uffff\ufffe\uffff" +
		"\25\0\uffff\uffff\10\0\233\0\15\0\233\0\26\0\233\0\uffff\uffff\ufffe\uffff\25\0\uffff" +
		"\uffff\10\0\235\0\15\0\235\0\26\0\235\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff" +
		"\17\0\333\0\31\0\333\0\uffff\uffff\ufffe\uffff\17\0\uffff\uffff\20\0\170\0\25\0\170" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0" +
		"\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105" +
		"\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff" +
		"\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24" +
		"\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff" +
		"\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff" +
		"\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff" +
		"\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff" +
		"\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff" +
		"\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff" +
		"\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111" +
		"\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff" +
		"\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff" +
		"\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105" +
		"\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\115\0\uffff\uffff\0\0\u0101\0\6\0\u0101\0" +
		"\7\0\u0101\0\27\0\u0101\0\30\0\u0101\0\44\0\u0101\0\45\0\u0101\0\55\0\u0101\0\56" +
		"\0\u0101\0\57\0\u0101\0\60\0\u0101\0\61\0\u0101\0\62\0\u0101\0\63\0\u0101\0\64\0" +
		"\u0101\0\65\0\u0101\0\66\0\u0101\0\67\0\u0101\0\70\0\u0101\0\71\0\u0101\0\72\0\u0101" +
		"\0\73\0\u0101\0\74\0\u0101\0\75\0\u0101\0\76\0\u0101\0\77\0\u0101\0\100\0\u0101\0" +
		"\101\0\u0101\0\102\0\u0101\0\103\0\u0101\0\104\0\u0101\0\105\0\u0101\0\106\0\u0101" +
		"\0\107\0\u0101\0\110\0\u0101\0\111\0\u0101\0\112\0\u0101\0\113\0\u0101\0\114\0\u0101" +
		"\0\uffff\uffff\ufffe\uffff\23\0\uffff\uffff\0\0\377\0\6\0\377\0\7\0\377\0\27\0\377" +
		"\0\30\0\377\0\44\0\377\0\45\0\377\0\55\0\377\0\56\0\377\0\57\0\377\0\60\0\377\0\61" +
		"\0\377\0\62\0\377\0\63\0\377\0\64\0\377\0\65\0\377\0\66\0\377\0\67\0\377\0\70\0\377" +
		"\0\71\0\377\0\72\0\377\0\73\0\377\0\74\0\377\0\75\0\377\0\76\0\377\0\77\0\377\0\100" +
		"\0\377\0\101\0\377\0\102\0\377\0\103\0\377\0\104\0\377\0\105\0\377\0\106\0\377\0" +
		"\107\0\377\0\110\0\377\0\111\0\377\0\112\0\377\0\113\0\377\0\114\0\377\0\115\0\377" +
		"\0\uffff\uffff\ufffe\uffff\30\0\uffff\uffff\45\0\uffff\uffff\55\0\uffff\uffff\56" +
		"\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\10\0\72\0\26\0\72\0\40\0\72\0\uffff\uffff" +
		"\ufffe\uffff\25\0\uffff\uffff\10\0\231\0\15\0\231\0\26\0\231\0\uffff\uffff\ufffe" +
		"\uffff\17\0\uffff\uffff\46\0\uffff\uffff\10\0\245\0\15\0\245\0\20\0\245\0\26\0\245" +
		"\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0" +
		"\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff" +
		"\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff" +
		"\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff" +
		"\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff" +
		"\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff" +
		"\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff" +
		"\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110" +
		"\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff" +
		"\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff\ufffe" +
		"\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff" +
		"\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff" +
		"\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff" +
		"\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff" +
		"\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff" +
		"\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0" +
		"\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff" +
		"\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff" +
		"\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10" +
		"\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0" +
		"\uffff\uffff\21\0\uffff\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff" +
		"\43\0\uffff\uffff\44\0\uffff\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff" +
		"\56\0\uffff\uffff\57\0\uffff\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff" +
		"\63\0\uffff\uffff\64\0\uffff\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff" +
		"\70\0\uffff\uffff\71\0\uffff\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff" +
		"\75\0\uffff\uffff\76\0\uffff\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff" +
		"\uffff\102\0\uffff\uffff\103\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106" +
		"\0\uffff\uffff\107\0\uffff\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff" +
		"\uffff\113\0\uffff\uffff\114\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105" +
		"\0\25\0\u0105\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff" +
		"\ufffe\uffff\115\0\uffff\uffff\0\0\u0101\0\6\0\u0101\0\7\0\u0101\0\27\0\u0101\0\30" +
		"\0\u0101\0\44\0\u0101\0\45\0\u0101\0\55\0\u0101\0\56\0\u0101\0\57\0\u0101\0\60\0" +
		"\u0101\0\61\0\u0101\0\62\0\u0101\0\63\0\u0101\0\64\0\u0101\0\65\0\u0101\0\66\0\u0101" +
		"\0\67\0\u0101\0\70\0\u0101\0\71\0\u0101\0\72\0\u0101\0\73\0\u0101\0\74\0\u0101\0" +
		"\75\0\u0101\0\76\0\u0101\0\77\0\u0101\0\100\0\u0101\0\101\0\u0101\0\102\0\u0101\0" +
		"\103\0\u0101\0\104\0\u0101\0\105\0\u0101\0\106\0\u0101\0\107\0\u0101\0\110\0\u0101" +
		"\0\111\0\u0101\0\112\0\u0101\0\113\0\u0101\0\114\0\u0101\0\uffff\uffff\ufffe\uffff" +
		"\11\0\360\0\41\0\uffff\uffff\22\0\360\0\uffff\uffff\ufffe\uffff\11\0\357\0\41\0\357" +
		"\0\22\0\357\0\uffff\uffff\ufffe\uffff\6\0\uffff\uffff\16\0\uffff\uffff\21\0\uffff" +
		"\uffff\23\0\uffff\uffff\24\0\uffff\uffff\42\0\uffff\uffff\43\0\uffff\uffff\44\0\uffff" +
		"\uffff\45\0\uffff\uffff\53\0\uffff\uffff\55\0\uffff\uffff\56\0\uffff\uffff\57\0\uffff" +
		"\uffff\60\0\uffff\uffff\61\0\uffff\uffff\62\0\uffff\uffff\63\0\uffff\uffff\64\0\uffff" +
		"\uffff\65\0\uffff\uffff\66\0\uffff\uffff\67\0\uffff\uffff\70\0\uffff\uffff\71\0\uffff" +
		"\uffff\72\0\uffff\uffff\73\0\uffff\uffff\74\0\uffff\uffff\75\0\uffff\uffff\76\0\uffff" +
		"\uffff\77\0\uffff\uffff\100\0\uffff\uffff\101\0\uffff\uffff\102\0\uffff\uffff\103" +
		"\0\uffff\uffff\104\0\uffff\uffff\105\0\uffff\uffff\106\0\uffff\uffff\107\0\uffff" +
		"\uffff\110\0\uffff\uffff\111\0\uffff\uffff\112\0\uffff\uffff\113\0\uffff\uffff\114" +
		"\0\uffff\uffff\115\0\uffff\uffff\10\0\u0105\0\15\0\u0105\0\25\0\u0105\0\uffff\uffff" +
		"\ufffe\uffff\10\0\316\0\40\0\uffff\uffff\26\0\316\0\uffff\uffff\ufffe\uffff\10\0" +
		"\317\0\40\0\317\0\26\0\317\0\uffff\uffff\ufffe\uffff");

	private static final int[] tmGoto = TMLexer.unpack_int(172,
		"\0\0\4\0\40\0\100\0\100\0\100\0\100\0\172\0\176\0\210\0\216\0\236\0\240\0\242\0\350" +
		"\0\u0118\0\u012e\0\u0154\0\u0184\0\u018e\0\u01da\0\u0208\0\u0220\0\u0230\0\u0232" +
		"\0\u0244\0\u024c\0\u0252\0\u025a\0\u025c\0\u025e\0\u0268\0\u0276\0\u0280\0\u0286" +
		"\0\u02ba\0\u02ee\0\u032e\0\u03f0\0\u03f6\0\u040e\0\u0412\0\u0414\0\u0416\0\u0450" +
		"\0\u0468\0\u052c\0\u05f0\0\u06be\0\u0782\0\u0846\0\u090a\0\u09ce\0\u0a94\0\u0b58" +
		"\0\u0c1c\0\u0ce6\0\u0daa\0\u0e76\0\u0f38\0\u0ffc\0\u10c0\0\u1184\0\u1248\0\u130c" +
		"\0\u13d0\0\u1494\0\u1558\0\u161e\0\u16e2\0\u17a6\0\u1870\0\u1934\0\u19f8\0\u1abc" +
		"\0\u1b80\0\u1c4a\0\u1d0e\0\u1d4c\0\u1d4e\0\u1d54\0\u1d56\0\u1e18\0\u1e30\0\u1e36" +
		"\0\u1e3a\0\u1e3e\0\u1e70\0\u1eb0\0\u1eb2\0\u1eb4\0\u1eb6\0\u1eb8\0\u1eba\0\u1ebc" +
		"\0\u1ebe\0\u1ec0\0\u1f0c\0\u1f34\0\u1f40\0\u1f44\0\u1f4c\0\u1f54\0\u1f5c\0\u1f64" +
		"\0\u1f66\0\u1f6e\0\u1f76\0\u1f78\0\u1f80\0\u1f84\0\u1f8c\0\u1f90\0\u1f96\0\u1f98" +
		"\0\u1f9c\0\u1fa0\0\u1fa8\0\u1fac\0\u1fae\0\u1fb0\0\u1fb4\0\u1fb8\0\u1fbc\0\u1fbe" +
		"\0\u1fc2\0\u1fc6\0\u1fc8\0\u1fec\0\u2010\0\u2036\0\u205c\0\u208a\0\u20a2\0\u20a6" +
		"\0\u20ce\0\u20fc\0\u20fe\0\u212c\0\u2130\0\u215e\0\u218c\0\u21bc\0\u21f0\0\u2224" +
		"\0\u2258\0\u2292\0\u229c\0\u22a4\0\u22d6\0\u2308\0\u233c\0\u233e\0\u2342\0\u2346" +
		"\0\u235a\0\u235c\0\u235e\0\u2364\0\u2368\0\u236c\0\u2374\0\u237a\0\u2380\0\u238a" +
		"\0\u238c\0\u2390\0\u2394\0\u2398\0\u239c\0\u23a0\0\u23ce\0");

	private static final int[] tmFromTo = TMLexer.unpack_int(9166,
		"\u01c3\0\u01c5\0\u01c4\0\u01c6\0\1\0\4\0\6\0\4\0\74\0\113\0\100\0\4\0\114\0\133\0" +
		"\126\0\4\0\135\0\4\0\326\0\4\0\u011f\0\4\0\u013d\0\4\0\u0160\0\4\0\u0166\0\4\0\u0167" +
		"\0\4\0\u0181\0\4\0\1\0\5\0\6\0\5\0\100\0\5\0\126\0\5\0\135\0\5\0\234\0\312\0\235" +
		"\0\313\0\304\0\u010e\0\326\0\5\0\u0110\0\u010e\0\u011f\0\5\0\u013d\0\5\0\u0160\0" +
		"\5\0\u0166\0\5\0\u0167\0\5\0\u0181\0\5\0\134\0\147\0\152\0\147\0\163\0\203\0\201" +
		"\0\147\0\206\0\203\0\231\0\147\0\256\0\327\0\332\0\327\0\346\0\327\0\350\0\327\0" +
		"\376\0\327\0\u0100\0\327\0\u0101\0\327\0\u0104\0\327\0\u012a\0\327\0\u012f\0\327" +
		"\0\u0133\0\327\0\u0135\0\327\0\u014a\0\327\0\u014d\0\327\0\u014f\0\327\0\u0151\0" +
		"\327\0\u0153\0\327\0\u0154\0\327\0\u0189\0\327\0\u018d\0\327\0\u0190\0\327\0\u0192" +
		"\0\327\0\u01b3\0\327\0\75\0\115\0\120\0\136\0\343\0\u0133\0\u0176\0\u01a3\0\u01a0" +
		"\0\u01a3\0\u01b9\0\u01a3\0\u01ba\0\u01a3\0\u0128\0\u0168\0\u019b\0\u0168\0\u019c" +
		"\0\u0168\0\116\0\135\0\151\0\173\0\254\0\326\0\314\0\u0116\0\325\0\u011f\0\337\0" +
		"\u0131\0\u011e\0\u0160\0\u0143\0\u0181\0\u0126\0\u0166\0\u0126\0\u0167\0\61\0\72" +
		"\0\113\0\132\0\131\0\145\0\133\0\146\0\220\0\277\0\222\0\301\0\276\0\u0108\0\312" +
		"\0\u0114\0\313\0\u0115\0\316\0\u0118\0\321\0\u011a\0\323\0\u011c\0\325\0\u0120\0" +
		"\344\0\u0134\0\u0112\0\u015b\0\u0113\0\u015c\0\u011e\0\u0161\0\u0149\0\u0184\0\u014b" +
		"\0\u0186\0\u014c\0\u0187\0\u0150\0\u018b\0\u015d\0\u0196\0\u0162\0\u0198\0\u0185" +
		"\0\u01aa\0\u0188\0\u01ab\0\u018a\0\u01ad\0\u018c\0\u01ae\0\u018e\0\u01b0\0\u018f" +
		"\0\u01b1\0\u0197\0\u01b6\0\u01ac\0\u01bb\0\u01af\0\u01bc\0\u01b2\0\u01bd\0\u01b4" +
		"\0\u01bf\0\u01be\0\u01c2\0\60\0\71\0\256\0\330\0\332\0\330\0\346\0\330\0\350\0\330" +
		"\0\376\0\330\0\u0100\0\330\0\u0101\0\330\0\u0104\0\330\0\u012a\0\330\0\u012f\0\330" +
		"\0\u0133\0\330\0\u0135\0\330\0\u014a\0\330\0\u014d\0\330\0\u014f\0\330\0\u0151\0" +
		"\330\0\u0153\0\330\0\u0154\0\330\0\u0189\0\330\0\u018d\0\330\0\u0190\0\330\0\u0192" +
		"\0\330\0\u01b3\0\330\0\6\0\63\0\66\0\100\0\106\0\127\0\171\0\224\0\220\0\300\0\222" +
		"\0\300\0\316\0\u0119\0\321\0\u011b\0\366\0\u0144\0\u0147\0\u0183\0\u0180\0\u011b" +
		"\0\105\0\126\0\176\0\230\0\205\0\256\0\232\0\307\0\267\0\376\0\270\0\u0100\0\271" +
		"\0\u0101\0\275\0\u0104\0\377\0\u014a\0\u0102\0\u014d\0\u0103\0\u014f\0\u0105\0\u0151" +
		"\0\u0106\0\u0153\0\u0107\0\u0154\0\u014e\0\u0189\0\u0152\0\u018d\0\u0155\0\u0190" +
		"\0\u0156\0\u0192\0\u0191\0\u01b3\0\1\0\6\0\6\0\6\0\100\0\6\0\135\0\6\0\256\0\331" +
		"\0\326\0\6\0\332\0\331\0\376\0\331\0\u0100\0\331\0\u0101\0\331\0\u0104\0\331\0\u012f" +
		"\0\331\0\u0133\0\331\0\u014a\0\331\0\u014d\0\331\0\u014f\0\331\0\u0151\0\331\0\u0153" +
		"\0\331\0\u0154\0\331\0\u0189\0\331\0\u018d\0\331\0\u0190\0\331\0\u0192\0\331\0\u01b3" +
		"\0\331\0\6\0\64\0\63\0\77\0\66\0\101\0\100\0\122\0\u0128\0\u0169\0\61\0\73\0\230" +
		"\0\303\0\256\0\332\0\307\0\303\0\332\0\332\0\334\0\u012f\0\335\0\u0130\0\346\0\332" +
		"\0\350\0\332\0\362\0\332\0\376\0\332\0\u0100\0\332\0\u0101\0\332\0\u0104\0\332\0" +
		"\u010f\0\303\0\u012a\0\332\0\u012f\0\332\0\u0130\0\u0171\0\u0131\0\332\0\u0132\0" +
		"\332\0\u0133\0\332\0\u0135\0\332\0\u014a\0\332\0\u014d\0\332\0\u014f\0\332\0\u0151" +
		"\0\332\0\u0153\0\332\0\u0154\0\332\0\u015a\0\303\0\u0171\0\u0171\0\u0172\0\u0171" +
		"\0\u0189\0\332\0\u018d\0\332\0\u0190\0\332\0\u0192\0\332\0\u01a3\0\u0171\0\u01a5" +
		"\0\u0171\0\u01b3\0\332\0\256\0\333\0\332\0\333\0\346\0\333\0\350\0\333\0\376\0\333" +
		"\0\u0100\0\333\0\u0101\0\333\0\u0104\0\333\0\u012a\0\333\0\u012f\0\333\0\u0133\0" +
		"\333\0\u0135\0\333\0\u014a\0\333\0\u014d\0\333\0\u014f\0\333\0\u0151\0\333\0\u0153" +
		"\0\333\0\u0154\0\333\0\u0189\0\333\0\u018d\0\333\0\u0190\0\333\0\u0192\0\333\0\u01b3" +
		"\0\333\0\205\0\257\0\267\0\257\0\271\0\257\0\275\0\257\0\363\0\257\0\u0102\0\257" +
		"\0\u0105\0\257\0\u0107\0\257\0\u0136\0\257\0\u0139\0\257\0\u0155\0\257\0\u017a\0" +
		"\257\0\112\0\131\0\u010d\0\u0157\0\u0129\0\u016b\0\u012d\0\u016e\0\u0170\0\u019f" +
		"\0\u0176\0\u01a4\0\u019d\0\u01b7\0\u01a0\0\u01b8\0\231\0\306\0\53\0\67\0\134\0\150" +
		"\0\152\0\150\0\201\0\150\0\205\0\260\0\231\0\150\0\275\0\260\0\337\0\67\0\u0173\0" +
		"\67\0\107\0\130\0\167\0\223\0\171\0\225\0\366\0\u0145\0\150\0\167\0\360\0\u013b\0" +
		"\u01b7\0\u01c0\0\67\0\102\0\127\0\102\0\360\0\u013c\0\u01b7\0\u01c1\0\337\0\u0132" +
		"\0\357\0\u013a\0\331\0\u0125\0\333\0\u012b\0\u0168\0\u0125\0\u016a\0\u0125\0\u016f" +
		"\0\u012b\0\67\0\103\0\127\0\103\0\u0130\0\u0172\0\u0171\0\u0172\0\u0172\0\u0172\0" +
		"\u01a3\0\u0172\0\u01a5\0\u0172\0\u012d\0\u016f\0\u0176\0\u01a5\0\u01a0\0\u01a5\0" +
		"\u01b9\0\u01a5\0\u01ba\0\u01a5\0\u0128\0\u016a\0\u019b\0\u016a\0\u019c\0\u016a\0" +
		"\256\0\334\0\332\0\334\0\346\0\334\0\350\0\334\0\362\0\334\0\376\0\334\0\u0100\0" +
		"\334\0\u0101\0\334\0\u0104\0\334\0\u012a\0\334\0\u012f\0\334\0\u0131\0\334\0\u0132" +
		"\0\334\0\u0133\0\334\0\u0135\0\334\0\u014a\0\334\0\u014d\0\334\0\u014f\0\334\0\u0151" +
		"\0\334\0\u0153\0\334\0\u0154\0\334\0\u0189\0\334\0\u018d\0\334\0\u0190\0\334\0\u0192" +
		"\0\334\0\u01b3\0\334\0\163\0\204\0\206\0\204\0\213\0\204\0\256\0\204\0\332\0\204" +
		"\0\346\0\204\0\350\0\204\0\376\0\204\0\u0100\0\204\0\u0101\0\204\0\u0104\0\204\0" +
		"\u012a\0\204\0\u012f\0\204\0\u0133\0\204\0\u0135\0\204\0\u014a\0\204\0\u014d\0\204" +
		"\0\u014f\0\204\0\u0151\0\204\0\u0153\0\204\0\u0154\0\204\0\u0189\0\204\0\u018d\0" +
		"\204\0\u0190\0\204\0\u0192\0\204\0\u01b3\0\204\0\1\0\7\0\6\0\7\0\75\0\7\0\100\0\7" +
		"\0\135\0\7\0\152\0\7\0\204\0\7\0\206\0\7\0\231\0\7\0\256\0\7\0\326\0\7\0\332\0\7" +
		"\0\350\0\7\0\376\0\7\0\u0100\0\7\0\u0101\0\7\0\u0104\0\7\0\u012a\0\7\0\u012f\0\7" +
		"\0\u0133\0\7\0\u0135\0\7\0\u014a\0\7\0\u014d\0\7\0\u014f\0\7\0\u0151\0\7\0\u0153" +
		"\0\7\0\u0154\0\7\0\u0189\0\7\0\u018d\0\7\0\u0190\0\7\0\u0192\0\7\0\u01b3\0\7\0\1" +
		"\0\10\0\2\0\10\0\6\0\10\0\67\0\10\0\71\0\10\0\73\0\10\0\74\0\10\0\75\0\10\0\100\0" +
		"\10\0\102\0\10\0\103\0\10\0\126\0\10\0\127\0\10\0\134\0\10\0\135\0\10\0\150\0\10" +
		"\0\152\0\10\0\156\0\10\0\163\0\10\0\164\0\10\0\165\0\10\0\166\0\10\0\201\0\10\0\204" +
		"\0\10\0\206\0\10\0\214\0\10\0\216\0\10\0\224\0\10\0\231\0\10\0\240\0\10\0\242\0\10" +
		"\0\243\0\10\0\251\0\10\0\253\0\10\0\256\0\10\0\257\0\10\0\260\0\10\0\264\0\10\0\300" +
		"\0\10\0\323\0\10\0\324\0\10\0\326\0\10\0\330\0\10\0\331\0\10\0\332\0\10\0\333\0\10" +
		"\0\346\0\10\0\350\0\10\0\362\0\10\0\365\0\10\0\371\0\10\0\376\0\10\0\u0100\0\10\0" +
		"\u0101\0\10\0\u0104\0\10\0\u0119\0\10\0\u011b\0\10\0\u011f\0\10\0\u0122\0\10\0\u0123" +
		"\0\10\0\u0125\0\10\0\u012a\0\10\0\u012b\0\10\0\u012f\0\10\0\u0130\0\10\0\u0131\0" +
		"\10\0\u0132\0\10\0\u0133\0\10\0\u0135\0\10\0\u013d\0\10\0\u0140\0\10\0\u0141\0\10" +
		"\0\u0144\0\10\0\u014a\0\10\0\u014d\0\10\0\u014f\0\10\0\u0151\0\10\0\u0153\0\10\0" +
		"\u0154\0\10\0\u0160\0\10\0\u0168\0\10\0\u016a\0\10\0\u016c\0\10\0\u016f\0\10\0\u0171" +
		"\0\10\0\u0172\0\10\0\u0173\0\10\0\u0181\0\10\0\u0183\0\10\0\u0189\0\10\0\u018d\0" +
		"\10\0\u0190\0\10\0\u0192\0\10\0\u019d\0\10\0\u01a3\0\10\0\u01a5\0\10\0\u01b3\0\10" +
		"\0\360\0\u013d\0\364\0\u0140\0\u0180\0\u0140\0\1\0\11\0\6\0\11\0\100\0\11\0\126\0" +
		"\11\0\135\0\11\0\326\0\11\0\u011f\0\11\0\u013d\0\11\0\u0160\0\11\0\u0166\0\11\0\u0167" +
		"\0\11\0\u0181\0\11\0\261\0\371\0\374\0\371\0\62\0\74\0\u012a\0\u016c\0\256\0\335" +
		"\0\310\0\335\0\311\0\335\0\332\0\335\0\346\0\335\0\350\0\335\0\362\0\335\0\376\0" +
		"\335\0\u0100\0\335\0\u0101\0\335\0\u0104\0\335\0\u0116\0\335\0\u012a\0\335\0\u012f" +
		"\0\335\0\u0131\0\335\0\u0132\0\335\0\u0133\0\335\0\u0135\0\335\0\u014a\0\335\0\u014d" +
		"\0\335\0\u014f\0\335\0\u0151\0\335\0\u0153\0\335\0\u0154\0\335\0\u0189\0\335\0\u018d" +
		"\0\335\0\u0190\0\335\0\u0192\0\335\0\u01b3\0\335\0\1\0\12\0\6\0\12\0\100\0\12\0\126" +
		"\0\12\0\135\0\12\0\326\0\12\0\u011f\0\12\0\u013d\0\12\0\u0160\0\12\0\u0166\0\12\0" +
		"\u0167\0\12\0\u0181\0\12\0\1\0\13\0\2\0\13\0\6\0\13\0\67\0\13\0\71\0\13\0\73\0\13" +
		"\0\74\0\13\0\75\0\13\0\100\0\13\0\102\0\13\0\103\0\13\0\126\0\13\0\127\0\13\0\134" +
		"\0\13\0\135\0\13\0\150\0\13\0\152\0\13\0\156\0\13\0\163\0\13\0\164\0\13\0\165\0\13" +
		"\0\166\0\13\0\201\0\13\0\203\0\233\0\204\0\13\0\206\0\13\0\214\0\13\0\216\0\13\0" +
		"\224\0\13\0\231\0\13\0\240\0\13\0\242\0\13\0\243\0\13\0\251\0\13\0\253\0\13\0\256" +
		"\0\13\0\257\0\13\0\260\0\13\0\264\0\13\0\300\0\13\0\323\0\13\0\324\0\13\0\326\0\13" +
		"\0\330\0\13\0\331\0\13\0\332\0\13\0\333\0\13\0\346\0\13\0\350\0\13\0\362\0\13\0\365" +
		"\0\13\0\371\0\13\0\376\0\13\0\u0100\0\13\0\u0101\0\13\0\u0104\0\13\0\u0119\0\13\0" +
		"\u011b\0\13\0\u011f\0\13\0\u0122\0\13\0\u0123\0\13\0\u0125\0\13\0\u012a\0\13\0\u012b" +
		"\0\13\0\u012f\0\13\0\u0130\0\13\0\u0131\0\13\0\u0132\0\13\0\u0133\0\13\0\u0135\0" +
		"\13\0\u013d\0\13\0\u0140\0\13\0\u0141\0\13\0\u0144\0\13\0\u014a\0\13\0\u014d\0\13" +
		"\0\u014f\0\13\0\u0151\0\13\0\u0153\0\13\0\u0154\0\13\0\u0160\0\13\0\u0168\0\13\0" +
		"\u016a\0\13\0\u016c\0\13\0\u016f\0\13\0\u0171\0\13\0\u0172\0\13\0\u0173\0\13\0\u0181" +
		"\0\13\0\u0183\0\13\0\u0189\0\13\0\u018d\0\13\0\u0190\0\13\0\u0192\0\13\0\u019d\0" +
		"\13\0\u01a3\0\13\0\u01a5\0\13\0\u01b3\0\13\0\1\0\14\0\2\0\14\0\6\0\14\0\67\0\14\0" +
		"\71\0\14\0\73\0\14\0\74\0\14\0\75\0\14\0\100\0\14\0\102\0\14\0\103\0\14\0\126\0\14" +
		"\0\127\0\14\0\134\0\14\0\135\0\14\0\147\0\164\0\150\0\14\0\152\0\14\0\156\0\14\0" +
		"\163\0\14\0\164\0\14\0\165\0\14\0\166\0\14\0\201\0\14\0\204\0\14\0\206\0\14\0\214" +
		"\0\14\0\216\0\14\0\224\0\14\0\231\0\14\0\240\0\14\0\242\0\14\0\243\0\14\0\251\0\14" +
		"\0\253\0\14\0\256\0\14\0\257\0\14\0\260\0\14\0\264\0\14\0\300\0\14\0\323\0\14\0\324" +
		"\0\14\0\326\0\14\0\330\0\14\0\331\0\14\0\332\0\14\0\333\0\14\0\346\0\14\0\350\0\14" +
		"\0\362\0\14\0\365\0\14\0\371\0\14\0\376\0\14\0\u0100\0\14\0\u0101\0\14\0\u0104\0" +
		"\14\0\u0119\0\14\0\u011b\0\14\0\u011f\0\14\0\u0122\0\14\0\u0123\0\14\0\u0125\0\14" +
		"\0\u012a\0\14\0\u012b\0\14\0\u012f\0\14\0\u0130\0\14\0\u0131\0\14\0\u0132\0\14\0" +
		"\u0133\0\14\0\u0135\0\14\0\u013d\0\14\0\u0140\0\14\0\u0141\0\14\0\u0144\0\14\0\u014a" +
		"\0\14\0\u014d\0\14\0\u014f\0\14\0\u0151\0\14\0\u0153\0\14\0\u0154\0\14\0\u0160\0" +
		"\14\0\u0168\0\14\0\u016a\0\14\0\u016c\0\14\0\u016f\0\14\0\u0171\0\14\0\u0172\0\14" +
		"\0\u0173\0\14\0\u0181\0\14\0\u0183\0\14\0\u0189\0\14\0\u018d\0\14\0\u0190\0\14\0" +
		"\u0192\0\14\0\u019d\0\14\0\u01a3\0\14\0\u01a5\0\14\0\u01b3\0\14\0\1\0\15\0\2\0\15" +
		"\0\6\0\15\0\67\0\15\0\71\0\15\0\73\0\15\0\74\0\15\0\75\0\15\0\100\0\15\0\102\0\15" +
		"\0\103\0\15\0\126\0\15\0\127\0\15\0\134\0\15\0\135\0\15\0\150\0\15\0\152\0\15\0\156" +
		"\0\15\0\163\0\15\0\164\0\15\0\165\0\15\0\166\0\15\0\201\0\15\0\204\0\15\0\205\0\261" +
		"\0\206\0\15\0\214\0\15\0\216\0\15\0\224\0\15\0\231\0\15\0\240\0\15\0\242\0\15\0\243" +
		"\0\15\0\251\0\15\0\253\0\15\0\256\0\15\0\257\0\15\0\260\0\15\0\262\0\374\0\264\0" +
		"\15\0\271\0\261\0\275\0\261\0\300\0\15\0\303\0\u010a\0\323\0\15\0\324\0\15\0\326" +
		"\0\15\0\330\0\15\0\331\0\15\0\332\0\15\0\333\0\15\0\346\0\15\0\350\0\15\0\362\0\15" +
		"\0\365\0\15\0\371\0\15\0\376\0\15\0\u0100\0\15\0\u0101\0\15\0\u0104\0\15\0\u0107" +
		"\0\261\0\u0119\0\15\0\u011b\0\15\0\u011f\0\15\0\u0122\0\15\0\u0123\0\15\0\u0125\0" +
		"\15\0\u012a\0\15\0\u012b\0\15\0\u012f\0\15\0\u0130\0\15\0\u0131\0\15\0\u0132\0\15" +
		"\0\u0133\0\15\0\u0135\0\15\0\u013d\0\15\0\u0140\0\15\0\u0141\0\15\0\u0144\0\15\0" +
		"\u014a\0\15\0\u014d\0\15\0\u014f\0\15\0\u0151\0\15\0\u0153\0\15\0\u0154\0\15\0\u0160" +
		"\0\15\0\u0168\0\15\0\u016a\0\15\0\u016c\0\15\0\u016f\0\15\0\u0171\0\15\0\u0172\0" +
		"\15\0\u0173\0\15\0\u0181\0\15\0\u0183\0\15\0\u0189\0\15\0\u018d\0\15\0\u0190\0\15" +
		"\0\u0192\0\15\0\u019d\0\15\0\u01a3\0\15\0\u01a5\0\15\0\u01b3\0\15\0\1\0\16\0\2\0" +
		"\16\0\6\0\16\0\67\0\16\0\71\0\16\0\73\0\16\0\74\0\16\0\75\0\16\0\100\0\16\0\102\0" +
		"\16\0\103\0\16\0\126\0\16\0\127\0\16\0\134\0\16\0\135\0\16\0\150\0\16\0\152\0\16" +
		"\0\156\0\16\0\163\0\16\0\164\0\16\0\165\0\16\0\166\0\16\0\201\0\16\0\204\0\16\0\206" +
		"\0\16\0\214\0\16\0\216\0\16\0\224\0\16\0\231\0\16\0\233\0\310\0\240\0\16\0\242\0" +
		"\16\0\243\0\16\0\251\0\16\0\253\0\16\0\256\0\16\0\257\0\16\0\260\0\16\0\264\0\16" +
		"\0\300\0\16\0\323\0\16\0\324\0\16\0\326\0\16\0\330\0\16\0\331\0\16\0\332\0\16\0\333" +
		"\0\16\0\346\0\16\0\350\0\16\0\362\0\16\0\365\0\16\0\371\0\16\0\376\0\16\0\u0100\0" +
		"\16\0\u0101\0\16\0\u0104\0\16\0\u0119\0\16\0\u011b\0\16\0\u011f\0\16\0\u0122\0\16" +
		"\0\u0123\0\16\0\u0125\0\16\0\u012a\0\16\0\u012b\0\16\0\u012f\0\16\0\u0130\0\16\0" +
		"\u0131\0\16\0\u0132\0\16\0\u0133\0\16\0\u0135\0\16\0\u013d\0\16\0\u0140\0\16\0\u0141" +
		"\0\16\0\u0144\0\16\0\u014a\0\16\0\u014d\0\16\0\u014f\0\16\0\u0151\0\16\0\u0153\0" +
		"\16\0\u0154\0\16\0\u0160\0\16\0\u0168\0\16\0\u016a\0\16\0\u016c\0\16\0\u016f\0\16" +
		"\0\u0171\0\16\0\u0172\0\16\0\u0173\0\16\0\u0181\0\16\0\u0183\0\16\0\u0189\0\16\0" +
		"\u018d\0\16\0\u0190\0\16\0\u0192\0\16\0\u019d\0\16\0\u01a3\0\16\0\u01a5\0\16\0\u01b3" +
		"\0\16\0\1\0\17\0\2\0\17\0\6\0\17\0\67\0\17\0\71\0\17\0\73\0\17\0\74\0\17\0\75\0\17" +
		"\0\100\0\17\0\102\0\17\0\103\0\17\0\126\0\17\0\127\0\17\0\134\0\17\0\135\0\17\0\150" +
		"\0\17\0\152\0\17\0\156\0\17\0\163\0\17\0\164\0\17\0\165\0\17\0\166\0\17\0\201\0\17" +
		"\0\203\0\234\0\204\0\17\0\206\0\17\0\214\0\17\0\216\0\17\0\224\0\17\0\231\0\17\0" +
		"\240\0\17\0\242\0\17\0\243\0\17\0\251\0\17\0\253\0\17\0\256\0\17\0\257\0\17\0\260" +
		"\0\17\0\264\0\17\0\300\0\17\0\323\0\17\0\324\0\17\0\326\0\17\0\330\0\17\0\331\0\17" +
		"\0\332\0\17\0\333\0\17\0\346\0\17\0\350\0\17\0\362\0\17\0\365\0\17\0\371\0\17\0\376" +
		"\0\17\0\u0100\0\17\0\u0101\0\17\0\u0104\0\17\0\u0119\0\17\0\u011b\0\17\0\u011f\0" +
		"\17\0\u0122\0\17\0\u0123\0\17\0\u0125\0\17\0\u012a\0\17\0\u012b\0\17\0\u012f\0\17" +
		"\0\u0130\0\17\0\u0131\0\17\0\u0132\0\17\0\u0133\0\17\0\u0135\0\17\0\u013d\0\17\0" +
		"\u0140\0\17\0\u0141\0\17\0\u0144\0\17\0\u014a\0\17\0\u014d\0\17\0\u014f\0\17\0\u0151" +
		"\0\17\0\u0153\0\17\0\u0154\0\17\0\u0160\0\17\0\u0168\0\17\0\u016a\0\17\0\u016c\0" +
		"\17\0\u016f\0\17\0\u0171\0\17\0\u0172\0\17\0\u0173\0\17\0\u0181\0\17\0\u0183\0\17" +
		"\0\u0189\0\17\0\u018d\0\17\0\u0190\0\17\0\u0192\0\17\0\u019d\0\17\0\u01a3\0\17\0" +
		"\u01a5\0\17\0\u01b3\0\17\0\1\0\20\0\2\0\20\0\6\0\20\0\67\0\20\0\71\0\20\0\73\0\20" +
		"\0\74\0\20\0\75\0\20\0\100\0\20\0\102\0\20\0\103\0\20\0\126\0\20\0\127\0\20\0\134" +
		"\0\20\0\135\0\20\0\150\0\20\0\152\0\20\0\156\0\20\0\163\0\20\0\164\0\20\0\165\0\20" +
		"\0\166\0\20\0\201\0\20\0\203\0\235\0\204\0\20\0\206\0\20\0\214\0\20\0\216\0\20\0" +
		"\224\0\20\0\231\0\20\0\240\0\20\0\242\0\20\0\243\0\20\0\251\0\20\0\253\0\20\0\256" +
		"\0\20\0\257\0\20\0\260\0\20\0\264\0\20\0\300\0\20\0\323\0\20\0\324\0\20\0\326\0\20" +
		"\0\330\0\20\0\331\0\20\0\332\0\20\0\333\0\20\0\346\0\20\0\350\0\20\0\362\0\20\0\365" +
		"\0\20\0\371\0\20\0\376\0\20\0\u0100\0\20\0\u0101\0\20\0\u0104\0\20\0\u0119\0\20\0" +
		"\u011b\0\20\0\u011f\0\20\0\u0122\0\20\0\u0123\0\20\0\u0125\0\20\0\u012a\0\20\0\u012b" +
		"\0\20\0\u012f\0\20\0\u0130\0\20\0\u0131\0\20\0\u0132\0\20\0\u0133\0\20\0\u0135\0" +
		"\20\0\u013d\0\20\0\u0140\0\20\0\u0141\0\20\0\u0144\0\20\0\u014a\0\20\0\u014d\0\20" +
		"\0\u014f\0\20\0\u0151\0\20\0\u0153\0\20\0\u0154\0\20\0\u0160\0\20\0\u0168\0\20\0" +
		"\u016a\0\20\0\u016c\0\20\0\u016f\0\20\0\u0171\0\20\0\u0172\0\20\0\u0173\0\20\0\u0181" +
		"\0\20\0\u0183\0\20\0\u0189\0\20\0\u018d\0\20\0\u0190\0\20\0\u0192\0\20\0\u019d\0" +
		"\20\0\u01a3\0\20\0\u01a5\0\20\0\u01b3\0\20\0\1\0\21\0\2\0\21\0\6\0\21\0\67\0\21\0" +
		"\71\0\21\0\73\0\21\0\74\0\21\0\75\0\21\0\100\0\21\0\102\0\21\0\103\0\21\0\126\0\21" +
		"\0\127\0\21\0\134\0\21\0\135\0\21\0\150\0\21\0\152\0\21\0\156\0\21\0\163\0\21\0\164" +
		"\0\21\0\165\0\21\0\166\0\21\0\201\0\21\0\203\0\236\0\204\0\21\0\206\0\21\0\214\0" +
		"\21\0\216\0\21\0\224\0\21\0\231\0\21\0\240\0\21\0\242\0\21\0\243\0\21\0\251\0\21" +
		"\0\253\0\21\0\256\0\21\0\257\0\21\0\260\0\21\0\264\0\21\0\300\0\21\0\323\0\21\0\324" +
		"\0\21\0\326\0\21\0\330\0\21\0\331\0\21\0\332\0\21\0\333\0\21\0\346\0\21\0\350\0\21" +
		"\0\362\0\21\0\365\0\21\0\371\0\21\0\376\0\21\0\u0100\0\21\0\u0101\0\21\0\u0104\0" +
		"\21\0\u0119\0\21\0\u011b\0\21\0\u011f\0\21\0\u0122\0\21\0\u0123\0\21\0\u0125\0\21" +
		"\0\u012a\0\21\0\u012b\0\21\0\u012f\0\21\0\u0130\0\21\0\u0131\0\21\0\u0132\0\21\0" +
		"\u0133\0\21\0\u0135\0\21\0\u013d\0\21\0\u0140\0\21\0\u0141\0\21\0\u0144\0\21\0\u014a" +
		"\0\21\0\u014d\0\21\0\u014f\0\21\0\u0151\0\21\0\u0153\0\21\0\u0154\0\21\0\u0160\0" +
		"\21\0\u0168\0\21\0\u016a\0\21\0\u016c\0\21\0\u016f\0\21\0\u0171\0\21\0\u0172\0\21" +
		"\0\u0173\0\21\0\u0181\0\21\0\u0183\0\21\0\u0189\0\21\0\u018d\0\21\0\u0190\0\21\0" +
		"\u0192\0\21\0\u019d\0\21\0\u01a3\0\21\0\u01a5\0\21\0\u01b3\0\21\0\1\0\22\0\2\0\22" +
		"\0\6\0\22\0\67\0\22\0\71\0\22\0\73\0\22\0\74\0\22\0\75\0\22\0\100\0\22\0\102\0\22" +
		"\0\103\0\22\0\126\0\22\0\127\0\22\0\134\0\22\0\135\0\22\0\150\0\22\0\152\0\22\0\156" +
		"\0\22\0\163\0\22\0\164\0\22\0\165\0\22\0\166\0\22\0\201\0\22\0\203\0\237\0\204\0" +
		"\22\0\206\0\22\0\214\0\22\0\216\0\22\0\224\0\22\0\231\0\22\0\240\0\22\0\242\0\22" +
		"\0\243\0\22\0\251\0\22\0\252\0\237\0\253\0\22\0\256\0\22\0\257\0\22\0\260\0\22\0" +
		"\264\0\22\0\300\0\22\0\323\0\22\0\324\0\22\0\326\0\22\0\330\0\22\0\331\0\22\0\332" +
		"\0\22\0\333\0\22\0\346\0\22\0\350\0\22\0\362\0\22\0\365\0\22\0\371\0\22\0\376\0\22" +
		"\0\u0100\0\22\0\u0101\0\22\0\u0104\0\22\0\u0119\0\22\0\u011b\0\22\0\u011f\0\22\0" +
		"\u0122\0\22\0\u0123\0\22\0\u0125\0\22\0\u012a\0\22\0\u012b\0\22\0\u012f\0\22\0\u0130" +
		"\0\22\0\u0131\0\22\0\u0132\0\22\0\u0133\0\22\0\u0135\0\22\0\u013d\0\22\0\u0140\0" +
		"\22\0\u0141\0\22\0\u0144\0\22\0\u014a\0\22\0\u014d\0\22\0\u014f\0\22\0\u0151\0\22" +
		"\0\u0153\0\22\0\u0154\0\22\0\u0160\0\22\0\u0168\0\22\0\u016a\0\22\0\u016c\0\22\0" +
		"\u016f\0\22\0\u0171\0\22\0\u0172\0\22\0\u0173\0\22\0\u0181\0\22\0\u0183\0\22\0\u0189" +
		"\0\22\0\u018d\0\22\0\u0190\0\22\0\u0192\0\22\0\u019d\0\22\0\u01a3\0\22\0\u01a5\0" +
		"\22\0\u01b3\0\22\0\1\0\23\0\2\0\23\0\6\0\23\0\67\0\23\0\71\0\23\0\73\0\23\0\74\0" +
		"\23\0\75\0\23\0\100\0\23\0\102\0\23\0\103\0\23\0\126\0\23\0\127\0\23\0\134\0\23\0" +
		"\135\0\23\0\150\0\23\0\152\0\23\0\156\0\23\0\163\0\23\0\164\0\23\0\165\0\23\0\166" +
		"\0\23\0\201\0\23\0\203\0\240\0\204\0\23\0\206\0\23\0\214\0\23\0\216\0\23\0\224\0" +
		"\23\0\231\0\23\0\240\0\23\0\242\0\23\0\243\0\23\0\251\0\23\0\253\0\23\0\256\0\23" +
		"\0\257\0\23\0\260\0\23\0\264\0\23\0\300\0\23\0\323\0\23\0\324\0\23\0\326\0\23\0\330" +
		"\0\23\0\331\0\23\0\332\0\23\0\333\0\23\0\346\0\23\0\350\0\23\0\362\0\23\0\365\0\23" +
		"\0\371\0\23\0\376\0\23\0\u0100\0\23\0\u0101\0\23\0\u0104\0\23\0\u0119\0\23\0\u011b" +
		"\0\23\0\u011f\0\23\0\u0122\0\23\0\u0123\0\23\0\u0125\0\23\0\u012a\0\23\0\u012b\0" +
		"\23\0\u012f\0\23\0\u0130\0\23\0\u0131\0\23\0\u0132\0\23\0\u0133\0\23\0\u0135\0\23" +
		"\0\u013d\0\23\0\u0140\0\23\0\u0141\0\23\0\u0144\0\23\0\u014a\0\23\0\u014d\0\23\0" +
		"\u014f\0\23\0\u0151\0\23\0\u0153\0\23\0\u0154\0\23\0\u0160\0\23\0\u0168\0\23\0\u016a" +
		"\0\23\0\u016c\0\23\0\u016f\0\23\0\u0171\0\23\0\u0172\0\23\0\u0173\0\23\0\u0181\0" +
		"\23\0\u0183\0\23\0\u0189\0\23\0\u018d\0\23\0\u0190\0\23\0\u0192\0\23\0\u019d\0\23" +
		"\0\u01a3\0\23\0\u01a5\0\23\0\u01b3\0\23\0\1\0\24\0\2\0\24\0\6\0\24\0\67\0\24\0\71" +
		"\0\24\0\73\0\24\0\74\0\24\0\75\0\24\0\100\0\24\0\102\0\24\0\103\0\24\0\126\0\24\0" +
		"\127\0\24\0\134\0\24\0\135\0\24\0\150\0\24\0\152\0\24\0\156\0\24\0\163\0\24\0\164" +
		"\0\24\0\165\0\24\0\166\0\24\0\201\0\24\0\203\0\241\0\204\0\24\0\206\0\24\0\214\0" +
		"\24\0\216\0\24\0\224\0\24\0\231\0\24\0\240\0\24\0\242\0\24\0\243\0\24\0\251\0\24" +
		"\0\253\0\24\0\256\0\24\0\257\0\24\0\260\0\24\0\264\0\24\0\300\0\24\0\323\0\24\0\324" +
		"\0\24\0\326\0\24\0\330\0\24\0\331\0\24\0\332\0\24\0\333\0\24\0\346\0\24\0\350\0\24" +
		"\0\362\0\24\0\365\0\24\0\371\0\24\0\376\0\24\0\u0100\0\24\0\u0101\0\24\0\u0104\0" +
		"\24\0\u0119\0\24\0\u011b\0\24\0\u011f\0\24\0\u0122\0\24\0\u0123\0\24\0\u0125\0\24" +
		"\0\u012a\0\24\0\u012b\0\24\0\u012f\0\24\0\u0130\0\24\0\u0131\0\24\0\u0132\0\24\0" +
		"\u0133\0\24\0\u0135\0\24\0\u013d\0\24\0\u0140\0\24\0\u0141\0\24\0\u0144\0\24\0\u014a" +
		"\0\24\0\u014d\0\24\0\u014f\0\24\0\u0151\0\24\0\u0153\0\24\0\u0154\0\24\0\u0160\0" +
		"\24\0\u0168\0\24\0\u016a\0\24\0\u016c\0\24\0\u016f\0\24\0\u0171\0\24\0\u0172\0\24" +
		"\0\u0173\0\24\0\u0181\0\24\0\u0183\0\24\0\u0189\0\24\0\u018d\0\24\0\u0190\0\24\0" +
		"\u0192\0\24\0\u019d\0\24\0\u01a3\0\24\0\u01a5\0\24\0\u01b3\0\24\0\1\0\25\0\2\0\25" +
		"\0\6\0\25\0\67\0\25\0\71\0\25\0\73\0\25\0\74\0\25\0\75\0\25\0\100\0\25\0\102\0\25" +
		"\0\103\0\25\0\126\0\25\0\127\0\25\0\134\0\25\0\135\0\25\0\150\0\25\0\152\0\25\0\156" +
		"\0\25\0\163\0\25\0\164\0\25\0\165\0\25\0\166\0\25\0\201\0\25\0\204\0\25\0\205\0\262" +
		"\0\206\0\25\0\214\0\25\0\216\0\25\0\224\0\25\0\231\0\25\0\240\0\25\0\242\0\25\0\243" +
		"\0\25\0\251\0\25\0\253\0\25\0\256\0\25\0\257\0\25\0\260\0\25\0\264\0\25\0\271\0\262" +
		"\0\275\0\262\0\300\0\25\0\323\0\25\0\324\0\25\0\326\0\25\0\330\0\25\0\331\0\25\0" +
		"\332\0\25\0\333\0\25\0\346\0\25\0\350\0\25\0\362\0\25\0\365\0\25\0\371\0\25\0\376" +
		"\0\25\0\u0100\0\25\0\u0101\0\25\0\u0104\0\25\0\u0107\0\262\0\u0119\0\25\0\u011b\0" +
		"\25\0\u011f\0\25\0\u0122\0\25\0\u0123\0\25\0\u0125\0\25\0\u012a\0\25\0\u012b\0\25" +
		"\0\u012f\0\25\0\u0130\0\25\0\u0131\0\25\0\u0132\0\25\0\u0133\0\25\0\u0135\0\25\0" +
		"\u013d\0\25\0\u0140\0\25\0\u0141\0\25\0\u0144\0\25\0\u014a\0\25\0\u014d\0\25\0\u014f" +
		"\0\25\0\u0151\0\25\0\u0153\0\25\0\u0154\0\25\0\u0160\0\25\0\u0168\0\25\0\u016a\0" +
		"\25\0\u016c\0\25\0\u016f\0\25\0\u0171\0\25\0\u0172\0\25\0\u0173\0\25\0\u0181\0\25" +
		"\0\u0183\0\25\0\u0189\0\25\0\u018d\0\25\0\u0190\0\25\0\u0192\0\25\0\u019d\0\25\0" +
		"\u01a3\0\25\0\u01a5\0\25\0\u01b3\0\25\0\1\0\26\0\2\0\26\0\6\0\26\0\67\0\26\0\71\0" +
		"\26\0\73\0\26\0\74\0\26\0\75\0\26\0\100\0\26\0\102\0\26\0\103\0\26\0\126\0\26\0\127" +
		"\0\26\0\134\0\26\0\135\0\26\0\150\0\26\0\152\0\26\0\156\0\26\0\163\0\26\0\164\0\26" +
		"\0\165\0\26\0\166\0\26\0\201\0\26\0\203\0\242\0\204\0\26\0\206\0\26\0\214\0\26\0" +
		"\216\0\26\0\224\0\26\0\231\0\26\0\240\0\26\0\242\0\26\0\243\0\26\0\251\0\26\0\253" +
		"\0\26\0\256\0\26\0\257\0\26\0\260\0\26\0\264\0\26\0\300\0\26\0\323\0\26\0\324\0\26" +
		"\0\326\0\26\0\330\0\26\0\331\0\26\0\332\0\26\0\333\0\26\0\346\0\26\0\350\0\26\0\362" +
		"\0\26\0\365\0\26\0\371\0\26\0\376\0\26\0\u0100\0\26\0\u0101\0\26\0\u0104\0\26\0\u0119" +
		"\0\26\0\u011b\0\26\0\u011f\0\26\0\u0122\0\26\0\u0123\0\26\0\u0125\0\26\0\u012a\0" +
		"\26\0\u012b\0\26\0\u012f\0\26\0\u0130\0\26\0\u0131\0\26\0\u0132\0\26\0\u0133\0\26" +
		"\0\u0135\0\26\0\u013d\0\26\0\u0140\0\26\0\u0141\0\26\0\u0144\0\26\0\u014a\0\26\0" +
		"\u014d\0\26\0\u014f\0\26\0\u0151\0\26\0\u0153\0\26\0\u0154\0\26\0\u0160\0\26\0\u0168" +
		"\0\26\0\u016a\0\26\0\u016c\0\26\0\u016f\0\26\0\u0171\0\26\0\u0172\0\26\0\u0173\0" +
		"\26\0\u0181\0\26\0\u0183\0\26\0\u0189\0\26\0\u018d\0\26\0\u0190\0\26\0\u0192\0\26" +
		"\0\u019d\0\26\0\u01a3\0\26\0\u01a5\0\26\0\u01b3\0\26\0\1\0\27\0\2\0\27\0\6\0\27\0" +
		"\67\0\27\0\71\0\27\0\73\0\27\0\74\0\27\0\75\0\27\0\100\0\27\0\102\0\27\0\103\0\27" +
		"\0\126\0\27\0\127\0\27\0\134\0\27\0\135\0\27\0\150\0\27\0\152\0\27\0\156\0\27\0\163" +
		"\0\27\0\164\0\27\0\165\0\27\0\166\0\27\0\201\0\27\0\203\0\243\0\204\0\27\0\205\0" +
		"\263\0\206\0\27\0\214\0\27\0\216\0\27\0\224\0\27\0\231\0\27\0\240\0\27\0\242\0\27" +
		"\0\243\0\27\0\251\0\27\0\253\0\27\0\256\0\27\0\257\0\27\0\260\0\27\0\264\0\27\0\271" +
		"\0\263\0\275\0\263\0\300\0\27\0\323\0\27\0\324\0\27\0\326\0\27\0\330\0\27\0\331\0" +
		"\27\0\332\0\27\0\333\0\27\0\346\0\27\0\350\0\27\0\362\0\27\0\365\0\27\0\371\0\27" +
		"\0\376\0\27\0\u0100\0\27\0\u0101\0\27\0\u0104\0\27\0\u0107\0\263\0\u0119\0\27\0\u011b" +
		"\0\27\0\u011f\0\27\0\u0122\0\27\0\u0123\0\27\0\u0125\0\27\0\u012a\0\27\0\u012b\0" +
		"\27\0\u012f\0\27\0\u0130\0\27\0\u0131\0\27\0\u0132\0\27\0\u0133\0\27\0\u0135\0\27" +
		"\0\u013d\0\27\0\u0140\0\27\0\u0141\0\27\0\u0144\0\27\0\u014a\0\27\0\u014d\0\27\0" +
		"\u014f\0\27\0\u0151\0\27\0\u0153\0\27\0\u0154\0\27\0\u0160\0\27\0\u0168\0\27\0\u016a" +
		"\0\27\0\u016c\0\27\0\u016f\0\27\0\u0171\0\27\0\u0172\0\27\0\u0173\0\27\0\u0181\0" +
		"\27\0\u0183\0\27\0\u0189\0\27\0\u018d\0\27\0\u0190\0\27\0\u0192\0\27\0\u019d\0\27" +
		"\0\u01a3\0\27\0\u01a5\0\27\0\u01b3\0\27\0\1\0\30\0\2\0\30\0\6\0\30\0\67\0\30\0\71" +
		"\0\30\0\73\0\30\0\74\0\30\0\75\0\30\0\100\0\30\0\102\0\30\0\103\0\30\0\126\0\30\0" +
		"\127\0\30\0\134\0\30\0\135\0\30\0\150\0\30\0\152\0\30\0\156\0\30\0\163\0\30\0\164" +
		"\0\30\0\165\0\30\0\166\0\30\0\201\0\30\0\204\0\30\0\206\0\30\0\214\0\30\0\216\0\30" +
		"\0\224\0\30\0\231\0\30\0\240\0\30\0\242\0\30\0\243\0\30\0\251\0\30\0\253\0\30\0\256" +
		"\0\30\0\257\0\30\0\260\0\30\0\264\0\30\0\300\0\30\0\323\0\30\0\324\0\30\0\326\0\30" +
		"\0\330\0\30\0\331\0\30\0\332\0\30\0\333\0\30\0\346\0\30\0\350\0\30\0\362\0\30\0\365" +
		"\0\30\0\371\0\30\0\376\0\30\0\u0100\0\30\0\u0101\0\30\0\u0104\0\30\0\u0119\0\30\0" +
		"\u011b\0\30\0\u011f\0\30\0\u0122\0\30\0\u0123\0\30\0\u0125\0\30\0\u012a\0\30\0\u012b" +
		"\0\30\0\u012f\0\30\0\u0130\0\30\0\u0131\0\30\0\u0132\0\30\0\u0133\0\30\0\u0135\0" +
		"\30\0\u013d\0\30\0\u0140\0\30\0\u0141\0\30\0\u0144\0\30\0\u014a\0\30\0\u014d\0\30" +
		"\0\u014f\0\30\0\u0151\0\30\0\u0153\0\30\0\u0154\0\30\0\u0160\0\30\0\u0168\0\30\0" +
		"\u016a\0\30\0\u016c\0\30\0\u016f\0\30\0\u0171\0\30\0\u0172\0\30\0\u0173\0\30\0\u0181" +
		"\0\30\0\u0183\0\30\0\u0189\0\30\0\u018d\0\30\0\u0190\0\30\0\u0192\0\30\0\u019d\0" +
		"\30\0\u01a3\0\30\0\u01a5\0\30\0\u01b3\0\30\0\0\0\2\0\1\0\31\0\2\0\31\0\6\0\31\0\67" +
		"\0\31\0\71\0\31\0\73\0\31\0\74\0\31\0\75\0\31\0\100\0\31\0\102\0\31\0\103\0\31\0" +
		"\126\0\31\0\127\0\31\0\134\0\31\0\135\0\31\0\150\0\31\0\152\0\31\0\156\0\31\0\163" +
		"\0\31\0\164\0\31\0\165\0\31\0\166\0\31\0\201\0\31\0\204\0\31\0\206\0\31\0\214\0\31" +
		"\0\216\0\31\0\224\0\31\0\231\0\31\0\240\0\31\0\242\0\31\0\243\0\31\0\251\0\31\0\253" +
		"\0\31\0\256\0\31\0\257\0\31\0\260\0\31\0\264\0\31\0\300\0\31\0\323\0\31\0\324\0\31" +
		"\0\326\0\31\0\330\0\31\0\331\0\31\0\332\0\31\0\333\0\31\0\346\0\31\0\350\0\31\0\362" +
		"\0\31\0\365\0\31\0\371\0\31\0\376\0\31\0\u0100\0\31\0\u0101\0\31\0\u0104\0\31\0\u0119" +
		"\0\31\0\u011b\0\31\0\u011f\0\31\0\u0122\0\31\0\u0123\0\31\0\u0125\0\31\0\u012a\0" +
		"\31\0\u012b\0\31\0\u012f\0\31\0\u0130\0\31\0\u0131\0\31\0\u0132\0\31\0\u0133\0\31" +
		"\0\u0135\0\31\0\u013d\0\31\0\u0140\0\31\0\u0141\0\31\0\u0144\0\31\0\u014a\0\31\0" +
		"\u014d\0\31\0\u014f\0\31\0\u0151\0\31\0\u0153\0\31\0\u0154\0\31\0\u0160\0\31\0\u0168" +
		"\0\31\0\u016a\0\31\0\u016c\0\31\0\u016f\0\31\0\u0171\0\31\0\u0172\0\31\0\u0173\0" +
		"\31\0\u0181\0\31\0\u0183\0\31\0\u0189\0\31\0\u018d\0\31\0\u0190\0\31\0\u0192\0\31" +
		"\0\u019d\0\31\0\u01a3\0\31\0\u01a5\0\31\0\u01b3\0\31\0\1\0\32\0\2\0\32\0\6\0\32\0" +
		"\67\0\32\0\71\0\32\0\73\0\32\0\74\0\32\0\75\0\32\0\100\0\32\0\102\0\32\0\103\0\32" +
		"\0\126\0\32\0\127\0\32\0\134\0\32\0\135\0\32\0\150\0\32\0\152\0\32\0\156\0\32\0\163" +
		"\0\32\0\164\0\32\0\165\0\32\0\166\0\32\0\201\0\32\0\204\0\32\0\206\0\32\0\214\0\32" +
		"\0\216\0\32\0\224\0\32\0\231\0\32\0\240\0\32\0\242\0\32\0\243\0\32\0\251\0\32\0\253" +
		"\0\32\0\256\0\32\0\257\0\32\0\260\0\32\0\264\0\32\0\300\0\32\0\303\0\u010b\0\323" +
		"\0\32\0\324\0\32\0\326\0\32\0\330\0\32\0\331\0\32\0\332\0\32\0\333\0\32\0\346\0\32" +
		"\0\350\0\32\0\362\0\32\0\365\0\32\0\371\0\32\0\376\0\32\0\u0100\0\32\0\u0101\0\32" +
		"\0\u0104\0\32\0\u0119\0\32\0\u011b\0\32\0\u011f\0\32\0\u0122\0\32\0\u0123\0\32\0" +
		"\u0125\0\32\0\u012a\0\32\0\u012b\0\32\0\u012f\0\32\0\u0130\0\32\0\u0131\0\32\0\u0132" +
		"\0\32\0\u0133\0\32\0\u0135\0\32\0\u013d\0\32\0\u0140\0\32\0\u0141\0\32\0\u0144\0" +
		"\32\0\u014a\0\32\0\u014d\0\32\0\u014f\0\32\0\u0151\0\32\0\u0153\0\32\0\u0154\0\32" +
		"\0\u0160\0\32\0\u0168\0\32\0\u016a\0\32\0\u016c\0\32\0\u016f\0\32\0\u0171\0\32\0" +
		"\u0172\0\32\0\u0173\0\32\0\u0181\0\32\0\u0183\0\32\0\u0189\0\32\0\u018d\0\32\0\u0190" +
		"\0\32\0\u0192\0\32\0\u019d\0\32\0\u01a3\0\32\0\u01a5\0\32\0\u01b3\0\32\0\1\0\33\0" +
		"\2\0\33\0\6\0\33\0\67\0\33\0\71\0\33\0\73\0\33\0\74\0\33\0\75\0\33\0\100\0\33\0\102" +
		"\0\33\0\103\0\33\0\126\0\33\0\127\0\33\0\134\0\33\0\135\0\33\0\150\0\33\0\152\0\33" +
		"\0\156\0\33\0\163\0\33\0\164\0\33\0\165\0\33\0\166\0\33\0\201\0\33\0\203\0\244\0" +
		"\204\0\33\0\206\0\33\0\214\0\33\0\216\0\33\0\224\0\33\0\231\0\33\0\240\0\33\0\242" +
		"\0\33\0\243\0\33\0\251\0\33\0\253\0\33\0\256\0\33\0\257\0\33\0\260\0\33\0\264\0\33" +
		"\0\300\0\33\0\323\0\33\0\324\0\33\0\326\0\33\0\330\0\33\0\331\0\33\0\332\0\33\0\333" +
		"\0\33\0\346\0\33\0\350\0\33\0\362\0\33\0\365\0\33\0\371\0\33\0\376\0\33\0\u0100\0" +
		"\33\0\u0101\0\33\0\u0104\0\33\0\u0119\0\33\0\u011b\0\33\0\u011f\0\33\0\u0122\0\33" +
		"\0\u0123\0\33\0\u0125\0\33\0\u012a\0\33\0\u012b\0\33\0\u012f\0\33\0\u0130\0\33\0" +
		"\u0131\0\33\0\u0132\0\33\0\u0133\0\33\0\u0135\0\33\0\u013d\0\33\0\u0140\0\33\0\u0141" +
		"\0\33\0\u0144\0\33\0\u014a\0\33\0\u014d\0\33\0\u014f\0\33\0\u0151\0\33\0\u0153\0" +
		"\33\0\u0154\0\33\0\u0160\0\33\0\u0168\0\33\0\u016a\0\33\0\u016c\0\33\0\u016f\0\33" +
		"\0\u0171\0\33\0\u0172\0\33\0\u0173\0\33\0\u0181\0\33\0\u0183\0\33\0\u0189\0\33\0" +
		"\u018d\0\33\0\u0190\0\33\0\u0192\0\33\0\u019d\0\33\0\u01a3\0\33\0\u01a5\0\33\0\u01b3" +
		"\0\33\0\1\0\34\0\2\0\34\0\6\0\34\0\67\0\34\0\71\0\34\0\73\0\34\0\74\0\34\0\75\0\34" +
		"\0\100\0\34\0\102\0\34\0\103\0\34\0\115\0\134\0\126\0\34\0\127\0\34\0\134\0\34\0" +
		"\135\0\34\0\150\0\34\0\152\0\34\0\156\0\34\0\163\0\34\0\164\0\34\0\165\0\34\0\166" +
		"\0\34\0\201\0\34\0\204\0\34\0\206\0\34\0\214\0\34\0\216\0\34\0\224\0\34\0\231\0\34" +
		"\0\240\0\34\0\242\0\34\0\243\0\34\0\251\0\34\0\253\0\34\0\256\0\34\0\257\0\34\0\260" +
		"\0\34\0\264\0\34\0\300\0\34\0\323\0\34\0\324\0\34\0\326\0\34\0\330\0\34\0\331\0\34" +
		"\0\332\0\34\0\333\0\34\0\346\0\34\0\350\0\34\0\362\0\34\0\365\0\34\0\371\0\34\0\376" +
		"\0\34\0\u0100\0\34\0\u0101\0\34\0\u0104\0\34\0\u0119\0\34\0\u011b\0\34\0\u011f\0" +
		"\34\0\u0122\0\34\0\u0123\0\34\0\u0125\0\34\0\u012a\0\34\0\u012b\0\34\0\u012f\0\34" +
		"\0\u0130\0\34\0\u0131\0\34\0\u0132\0\34\0\u0133\0\34\0\u0135\0\34\0\u013d\0\34\0" +
		"\u0140\0\34\0\u0141\0\34\0\u0144\0\34\0\u014a\0\34\0\u014d\0\34\0\u014f\0\34\0\u0151" +
		"\0\34\0\u0153\0\34\0\u0154\0\34\0\u0160\0\34\0\u0168\0\34\0\u016a\0\34\0\u016c\0" +
		"\34\0\u016f\0\34\0\u0171\0\34\0\u0172\0\34\0\u0173\0\34\0\u0181\0\34\0\u0183\0\34" +
		"\0\u0189\0\34\0\u018d\0\34\0\u0190\0\34\0\u0192\0\34\0\u019d\0\34\0\u01a3\0\34\0" +
		"\u01a5\0\34\0\u01b3\0\34\0\1\0\35\0\2\0\35\0\6\0\35\0\67\0\35\0\71\0\35\0\73\0\35" +
		"\0\74\0\35\0\75\0\35\0\100\0\35\0\102\0\35\0\103\0\35\0\126\0\35\0\127\0\35\0\134" +
		"\0\35\0\135\0\35\0\150\0\35\0\152\0\35\0\156\0\35\0\163\0\35\0\164\0\35\0\165\0\35" +
		"\0\166\0\35\0\201\0\35\0\203\0\245\0\204\0\35\0\206\0\35\0\214\0\35\0\216\0\35\0" +
		"\224\0\35\0\231\0\35\0\240\0\35\0\242\0\35\0\243\0\35\0\251\0\35\0\253\0\35\0\256" +
		"\0\35\0\257\0\35\0\260\0\35\0\264\0\35\0\300\0\35\0\323\0\35\0\324\0\35\0\326\0\35" +
		"\0\330\0\35\0\331\0\35\0\332\0\35\0\333\0\35\0\346\0\35\0\350\0\35\0\362\0\35\0\365" +
		"\0\35\0\371\0\35\0\376\0\35\0\u0100\0\35\0\u0101\0\35\0\u0104\0\35\0\u0119\0\35\0" +
		"\u011b\0\35\0\u011f\0\35\0\u0122\0\35\0\u0123\0\35\0\u0125\0\35\0\u012a\0\35\0\u012b" +
		"\0\35\0\u012f\0\35\0\u0130\0\35\0\u0131\0\35\0\u0132\0\35\0\u0133\0\35\0\u0135\0" +
		"\35\0\u013d\0\35\0\u0140\0\35\0\u0141\0\35\0\u0144\0\35\0\u014a\0\35\0\u014d\0\35" +
		"\0\u014f\0\35\0\u0151\0\35\0\u0153\0\35\0\u0154\0\35\0\u0160\0\35\0\u0168\0\35\0" +
		"\u016a\0\35\0\u016c\0\35\0\u016f\0\35\0\u0171\0\35\0\u0172\0\35\0\u0173\0\35\0\u0181" +
		"\0\35\0\u0183\0\35\0\u0189\0\35\0\u018d\0\35\0\u0190\0\35\0\u0192\0\35\0\u019d\0" +
		"\35\0\u01a3\0\35\0\u01a5\0\35\0\u01b3\0\35\0\1\0\36\0\2\0\36\0\6\0\36\0\67\0\36\0" +
		"\71\0\36\0\73\0\36\0\74\0\36\0\75\0\36\0\100\0\36\0\102\0\36\0\103\0\36\0\126\0\36" +
		"\0\127\0\36\0\134\0\36\0\135\0\36\0\150\0\36\0\152\0\36\0\156\0\36\0\163\0\36\0\164" +
		"\0\36\0\165\0\36\0\166\0\36\0\201\0\36\0\204\0\36\0\206\0\36\0\214\0\36\0\216\0\36" +
		"\0\224\0\36\0\231\0\36\0\240\0\36\0\242\0\36\0\243\0\36\0\251\0\36\0\253\0\36\0\256" +
		"\0\36\0\257\0\36\0\260\0\36\0\264\0\36\0\300\0\36\0\315\0\u0117\0\323\0\36\0\324" +
		"\0\36\0\326\0\36\0\330\0\36\0\331\0\36\0\332\0\36\0\333\0\36\0\346\0\36\0\350\0\36" +
		"\0\362\0\36\0\365\0\36\0\371\0\36\0\376\0\36\0\u0100\0\36\0\u0101\0\36\0\u0104\0" +
		"\36\0\u0119\0\36\0\u011b\0\36\0\u011f\0\36\0\u0122\0\36\0\u0123\0\36\0\u0125\0\36" +
		"\0\u012a\0\36\0\u012b\0\36\0\u012f\0\36\0\u0130\0\36\0\u0131\0\36\0\u0132\0\36\0" +
		"\u0133\0\36\0\u0135\0\36\0\u013d\0\36\0\u0140\0\36\0\u0141\0\36\0\u0144\0\36\0\u014a" +
		"\0\36\0\u014d\0\36\0\u014f\0\36\0\u0151\0\36\0\u0153\0\36\0\u0154\0\36\0\u0160\0" +
		"\36\0\u0168\0\36\0\u016a\0\36\0\u016c\0\36\0\u016f\0\36\0\u0171\0\36\0\u0172\0\36" +
		"\0\u0173\0\36\0\u0181\0\36\0\u0183\0\36\0\u0189\0\36\0\u018d\0\36\0\u0190\0\36\0" +
		"\u0192\0\36\0\u019d\0\36\0\u01a3\0\36\0\u01a5\0\36\0\u01b3\0\36\0\1\0\37\0\2\0\37" +
		"\0\6\0\37\0\67\0\37\0\71\0\37\0\73\0\37\0\74\0\37\0\75\0\37\0\100\0\37\0\102\0\37" +
		"\0\103\0\37\0\126\0\37\0\127\0\37\0\134\0\37\0\135\0\37\0\150\0\37\0\152\0\37\0\156" +
		"\0\37\0\163\0\37\0\164\0\37\0\165\0\37\0\166\0\37\0\201\0\37\0\203\0\246\0\204\0" +
		"\37\0\206\0\37\0\214\0\37\0\216\0\37\0\224\0\37\0\231\0\37\0\240\0\37\0\242\0\37" +
		"\0\243\0\37\0\251\0\37\0\253\0\37\0\256\0\37\0\257\0\37\0\260\0\37\0\264\0\37\0\300" +
		"\0\37\0\323\0\37\0\324\0\37\0\326\0\37\0\330\0\37\0\331\0\37\0\332\0\37\0\333\0\37" +
		"\0\346\0\37\0\350\0\37\0\362\0\37\0\365\0\37\0\371\0\37\0\376\0\37\0\u0100\0\37\0" +
		"\u0101\0\37\0\u0104\0\37\0\u0119\0\37\0\u011b\0\37\0\u011f\0\37\0\u0122\0\37\0\u0123" +
		"\0\37\0\u0125\0\37\0\u012a\0\37\0\u012b\0\37\0\u012f\0\37\0\u0130\0\37\0\u0131\0" +
		"\37\0\u0132\0\37\0\u0133\0\37\0\u0135\0\37\0\u013d\0\37\0\u0140\0\37\0\u0141\0\37" +
		"\0\u0144\0\37\0\u014a\0\37\0\u014d\0\37\0\u014f\0\37\0\u0151\0\37\0\u0153\0\37\0" +
		"\u0154\0\37\0\u0160\0\37\0\u0168\0\37\0\u016a\0\37\0\u016c\0\37\0\u016f\0\37\0\u0171" +
		"\0\37\0\u0172\0\37\0\u0173\0\37\0\u0181\0\37\0\u0183\0\37\0\u0189\0\37\0\u018d\0" +
		"\37\0\u0190\0\37\0\u0192\0\37\0\u019d\0\37\0\u01a3\0\37\0\u01a5\0\37\0\u01b3\0\37" +
		"\0\1\0\40\0\2\0\40\0\6\0\40\0\67\0\40\0\71\0\40\0\73\0\40\0\74\0\40\0\75\0\40\0\100" +
		"\0\40\0\102\0\40\0\103\0\40\0\126\0\40\0\127\0\40\0\134\0\40\0\135\0\40\0\150\0\40" +
		"\0\152\0\40\0\156\0\40\0\163\0\40\0\164\0\40\0\165\0\40\0\166\0\40\0\201\0\40\0\204" +
		"\0\40\0\206\0\40\0\214\0\40\0\216\0\40\0\224\0\40\0\231\0\40\0\233\0\311\0\240\0" +
		"\40\0\242\0\40\0\243\0\40\0\251\0\40\0\253\0\40\0\256\0\40\0\257\0\40\0\260\0\40" +
		"\0\264\0\40\0\300\0\40\0\323\0\40\0\324\0\40\0\326\0\40\0\330\0\40\0\331\0\40\0\332" +
		"\0\40\0\333\0\40\0\346\0\40\0\350\0\40\0\362\0\40\0\365\0\40\0\371\0\40\0\376\0\40" +
		"\0\u0100\0\40\0\u0101\0\40\0\u0104\0\40\0\u0119\0\40\0\u011b\0\40\0\u011f\0\40\0" +
		"\u0122\0\40\0\u0123\0\40\0\u0125\0\40\0\u012a\0\40\0\u012b\0\40\0\u012f\0\40\0\u0130" +
		"\0\40\0\u0131\0\40\0\u0132\0\40\0\u0133\0\40\0\u0135\0\40\0\u013d\0\40\0\u0140\0" +
		"\40\0\u0141\0\40\0\u0144\0\40\0\u014a\0\40\0\u014d\0\40\0\u014f\0\40\0\u0151\0\40" +
		"\0\u0153\0\40\0\u0154\0\40\0\u0160\0\40\0\u0168\0\40\0\u016a\0\40\0\u016c\0\40\0" +
		"\u016f\0\40\0\u0171\0\40\0\u0172\0\40\0\u0173\0\40\0\u0181\0\40\0\u0183\0\40\0\u0189" +
		"\0\40\0\u018d\0\40\0\u0190\0\40\0\u0192\0\40\0\u019d\0\40\0\u01a3\0\40\0\u01a5\0" +
		"\40\0\u01b3\0\40\0\1\0\41\0\2\0\41\0\6\0\41\0\67\0\41\0\71\0\41\0\73\0\41\0\74\0" +
		"\41\0\75\0\41\0\100\0\41\0\102\0\41\0\103\0\41\0\126\0\41\0\127\0\41\0\134\0\41\0" +
		"\135\0\41\0\150\0\41\0\152\0\41\0\156\0\41\0\163\0\41\0\164\0\41\0\165\0\41\0\166" +
		"\0\41\0\201\0\41\0\203\0\247\0\204\0\41\0\206\0\41\0\214\0\41\0\216\0\41\0\224\0" +
		"\41\0\231\0\41\0\240\0\41\0\242\0\41\0\243\0\41\0\251\0\41\0\252\0\247\0\253\0\41" +
		"\0\256\0\41\0\257\0\41\0\260\0\41\0\264\0\41\0\300\0\41\0\323\0\41\0\324\0\41\0\326" +
		"\0\41\0\330\0\41\0\331\0\41\0\332\0\41\0\333\0\41\0\346\0\41\0\350\0\41\0\362\0\41" +
		"\0\365\0\41\0\371\0\41\0\376\0\41\0\u0100\0\41\0\u0101\0\41\0\u0104\0\41\0\u0119" +
		"\0\41\0\u011b\0\41\0\u011f\0\41\0\u0122\0\41\0\u0123\0\41\0\u0125\0\41\0\u012a\0" +
		"\41\0\u012b\0\41\0\u012f\0\41\0\u0130\0\41\0\u0131\0\41\0\u0132\0\41\0\u0133\0\41" +
		"\0\u0135\0\41\0\u013d\0\41\0\u0140\0\41\0\u0141\0\41\0\u0144\0\41\0\u014a\0\41\0" +
		"\u014d\0\41\0\u014f\0\41\0\u0151\0\41\0\u0153\0\41\0\u0154\0\41\0\u0160\0\41\0\u0168" +
		"\0\41\0\u016a\0\41\0\u016c\0\41\0\u016f\0\41\0\u0171\0\41\0\u0172\0\41\0\u0173\0" +
		"\41\0\u0181\0\41\0\u0183\0\41\0\u0189\0\41\0\u018d\0\41\0\u0190\0\41\0\u0192\0\41" +
		"\0\u019d\0\41\0\u01a3\0\41\0\u01a5\0\41\0\u01b3\0\41\0\1\0\42\0\2\0\42\0\6\0\42\0" +
		"\67\0\42\0\71\0\42\0\73\0\42\0\74\0\42\0\75\0\42\0\100\0\42\0\102\0\42\0\103\0\42" +
		"\0\126\0\42\0\127\0\42\0\134\0\42\0\135\0\42\0\136\0\163\0\150\0\42\0\152\0\42\0" +
		"\156\0\42\0\163\0\42\0\164\0\42\0\165\0\42\0\166\0\42\0\201\0\42\0\204\0\42\0\206" +
		"\0\42\0\214\0\42\0\216\0\42\0\224\0\42\0\231\0\42\0\240\0\42\0\242\0\42\0\243\0\42" +
		"\0\251\0\42\0\253\0\42\0\256\0\42\0\257\0\42\0\260\0\42\0\264\0\42\0\300\0\42\0\323" +
		"\0\42\0\324\0\42\0\326\0\42\0\330\0\42\0\331\0\42\0\332\0\42\0\333\0\42\0\346\0\42" +
		"\0\350\0\42\0\362\0\42\0\365\0\42\0\371\0\42\0\376\0\42\0\u0100\0\42\0\u0101\0\42" +
		"\0\u0104\0\42\0\u0119\0\42\0\u011b\0\42\0\u011f\0\42\0\u0122\0\42\0\u0123\0\42\0" +
		"\u0125\0\42\0\u012a\0\42\0\u012b\0\42\0\u012f\0\42\0\u0130\0\42\0\u0131\0\42\0\u0132" +
		"\0\42\0\u0133\0\42\0\u0135\0\42\0\u013d\0\42\0\u0140\0\42\0\u0141\0\42\0\u0144\0" +
		"\42\0\u014a\0\42\0\u014d\0\42\0\u014f\0\42\0\u0151\0\42\0\u0153\0\42\0\u0154\0\42" +
		"\0\u0160\0\42\0\u0168\0\42\0\u016a\0\42\0\u016c\0\42\0\u016f\0\42\0\u0171\0\42\0" +
		"\u0172\0\42\0\u0173\0\42\0\u0181\0\42\0\u0183\0\42\0\u0189\0\42\0\u018d\0\42\0\u0190" +
		"\0\42\0\u0192\0\42\0\u019d\0\42\0\u01a3\0\42\0\u01a5\0\42\0\u01b3\0\42\0\1\0\43\0" +
		"\2\0\43\0\6\0\43\0\67\0\43\0\71\0\43\0\73\0\43\0\74\0\43\0\75\0\43\0\100\0\43\0\102" +
		"\0\43\0\103\0\43\0\126\0\43\0\127\0\43\0\134\0\43\0\135\0\43\0\150\0\43\0\152\0\43" +
		"\0\156\0\43\0\163\0\43\0\164\0\43\0\165\0\43\0\166\0\43\0\201\0\43\0\204\0\43\0\206" +
		"\0\43\0\214\0\43\0\216\0\43\0\224\0\43\0\231\0\43\0\240\0\43\0\242\0\43\0\243\0\43" +
		"\0\251\0\43\0\253\0\43\0\256\0\43\0\257\0\43\0\260\0\43\0\264\0\43\0\300\0\43\0\323" +
		"\0\43\0\324\0\43\0\326\0\43\0\327\0\u0122\0\330\0\43\0\331\0\43\0\332\0\43\0\333" +
		"\0\43\0\346\0\43\0\350\0\43\0\362\0\43\0\365\0\43\0\371\0\43\0\376\0\43\0\u0100\0" +
		"\43\0\u0101\0\43\0\u0104\0\43\0\u0119\0\43\0\u011b\0\43\0\u011f\0\43\0\u0122\0\43" +
		"\0\u0123\0\43\0\u0125\0\43\0\u012a\0\43\0\u012b\0\43\0\u012f\0\43\0\u0130\0\43\0" +
		"\u0131\0\43\0\u0132\0\43\0\u0133\0\43\0\u0135\0\43\0\u013d\0\43\0\u0140\0\43\0\u0141" +
		"\0\43\0\u0144\0\43\0\u014a\0\43\0\u014d\0\43\0\u014f\0\43\0\u0151\0\43\0\u0153\0" +
		"\43\0\u0154\0\43\0\u0160\0\43\0\u0168\0\43\0\u016a\0\43\0\u016c\0\43\0\u016f\0\43" +
		"\0\u0171\0\43\0\u0172\0\43\0\u0173\0\43\0\u0181\0\43\0\u0183\0\43\0\u0189\0\43\0" +
		"\u018d\0\43\0\u0190\0\43\0\u0192\0\43\0\u019d\0\43\0\u01a3\0\43\0\u01a5\0\43\0\u01b3" +
		"\0\43\0\1\0\44\0\2\0\44\0\6\0\44\0\67\0\44\0\71\0\44\0\73\0\44\0\74\0\44\0\75\0\44" +
		"\0\100\0\44\0\102\0\44\0\103\0\44\0\126\0\44\0\127\0\44\0\134\0\44\0\135\0\44\0\150" +
		"\0\44\0\152\0\44\0\156\0\44\0\163\0\44\0\164\0\44\0\165\0\44\0\166\0\44\0\201\0\44" +
		"\0\204\0\44\0\205\0\264\0\206\0\44\0\214\0\44\0\216\0\44\0\224\0\44\0\231\0\44\0" +
		"\240\0\44\0\242\0\44\0\243\0\44\0\251\0\44\0\253\0\44\0\256\0\44\0\257\0\44\0\260" +
		"\0\44\0\264\0\44\0\271\0\264\0\275\0\264\0\300\0\44\0\323\0\44\0\324\0\44\0\326\0" +
		"\44\0\330\0\44\0\331\0\44\0\332\0\44\0\333\0\44\0\346\0\44\0\350\0\44\0\362\0\44" +
		"\0\365\0\44\0\371\0\44\0\376\0\44\0\u0100\0\44\0\u0101\0\44\0\u0104\0\44\0\u0107" +
		"\0\264\0\u0119\0\44\0\u011b\0\44\0\u011f\0\44\0\u0122\0\44\0\u0123\0\44\0\u0125\0" +
		"\44\0\u012a\0\44\0\u012b\0\44\0\u012f\0\44\0\u0130\0\44\0\u0131\0\44\0\u0132\0\44" +
		"\0\u0133\0\44\0\u0135\0\44\0\u013d\0\44\0\u0140\0\44\0\u0141\0\44\0\u0144\0\44\0" +
		"\u014a\0\44\0\u014d\0\44\0\u014f\0\44\0\u0151\0\44\0\u0153\0\44\0\u0154\0\44\0\u0160" +
		"\0\44\0\u0168\0\44\0\u016a\0\44\0\u016c\0\44\0\u016f\0\44\0\u0171\0\44\0\u0172\0" +
		"\44\0\u0173\0\44\0\u0181\0\44\0\u0183\0\44\0\u0189\0\44\0\u018d\0\44\0\u0190\0\44" +
		"\0\u0192\0\44\0\u019d\0\44\0\u01a3\0\44\0\u01a5\0\44\0\u01b3\0\44\0\1\0\45\0\2\0" +
		"\45\0\6\0\45\0\67\0\45\0\71\0\45\0\73\0\45\0\74\0\45\0\75\0\45\0\100\0\45\0\102\0" +
		"\45\0\103\0\45\0\126\0\45\0\127\0\45\0\134\0\45\0\135\0\45\0\150\0\45\0\152\0\45" +
		"\0\156\0\45\0\163\0\45\0\164\0\45\0\165\0\45\0\166\0\45\0\201\0\45\0\203\0\250\0" +
		"\204\0\45\0\206\0\45\0\214\0\45\0\216\0\45\0\224\0\45\0\231\0\45\0\240\0\45\0\242" +
		"\0\45\0\243\0\45\0\251\0\45\0\253\0\45\0\256\0\45\0\257\0\45\0\260\0\45\0\264\0\45" +
		"\0\300\0\45\0\323\0\45\0\324\0\45\0\326\0\45\0\330\0\45\0\331\0\45\0\332\0\45\0\333" +
		"\0\45\0\346\0\45\0\350\0\45\0\362\0\45\0\365\0\45\0\371\0\45\0\376\0\45\0\u0100\0" +
		"\45\0\u0101\0\45\0\u0104\0\45\0\u0119\0\45\0\u011b\0\45\0\u011f\0\45\0\u0122\0\45" +
		"\0\u0123\0\45\0\u0125\0\45\0\u012a\0\45\0\u012b\0\45\0\u012f\0\45\0\u0130\0\45\0" +
		"\u0131\0\45\0\u0132\0\45\0\u0133\0\45\0\u0135\0\45\0\u013d\0\45\0\u0140\0\45\0\u0141" +
		"\0\45\0\u0144\0\45\0\u014a\0\45\0\u014d\0\45\0\u014f\0\45\0\u0151\0\45\0\u0153\0" +
		"\45\0\u0154\0\45\0\u0160\0\45\0\u0168\0\45\0\u016a\0\45\0\u016c\0\45\0\u016f\0\45" +
		"\0\u0171\0\45\0\u0172\0\45\0\u0173\0\45\0\u0181\0\45\0\u0183\0\45\0\u0189\0\45\0" +
		"\u018d\0\45\0\u0190\0\45\0\u0192\0\45\0\u019d\0\45\0\u01a3\0\45\0\u01a5\0\45\0\u01b3" +
		"\0\45\0\1\0\46\0\2\0\46\0\6\0\46\0\67\0\46\0\71\0\46\0\73\0\46\0\74\0\46\0\75\0\46" +
		"\0\100\0\46\0\102\0\46\0\103\0\46\0\126\0\46\0\127\0\46\0\134\0\46\0\135\0\46\0\147" +
		"\0\165\0\150\0\46\0\152\0\46\0\156\0\46\0\163\0\46\0\164\0\46\0\165\0\46\0\166\0" +
		"\46\0\201\0\46\0\204\0\46\0\206\0\46\0\214\0\46\0\216\0\46\0\224\0\46\0\231\0\46" +
		"\0\240\0\46\0\242\0\46\0\243\0\46\0\251\0\46\0\253\0\46\0\256\0\46\0\257\0\46\0\260" +
		"\0\46\0\264\0\46\0\300\0\46\0\323\0\46\0\324\0\46\0\326\0\46\0\330\0\46\0\331\0\46" +
		"\0\332\0\46\0\333\0\46\0\346\0\46\0\350\0\46\0\362\0\46\0\365\0\46\0\371\0\46\0\376" +
		"\0\46\0\u0100\0\46\0\u0101\0\46\0\u0104\0\46\0\u0119\0\46\0\u011b\0\46\0\u011f\0" +
		"\46\0\u0122\0\46\0\u0123\0\46\0\u0125\0\46\0\u012a\0\46\0\u012b\0\46\0\u012f\0\46" +
		"\0\u0130\0\46\0\u0131\0\46\0\u0132\0\46\0\u0133\0\46\0\u0135\0\46\0\u013d\0\46\0" +
		"\u0140\0\46\0\u0141\0\46\0\u0144\0\46\0\u014a\0\46\0\u014d\0\46\0\u014f\0\46\0\u0151" +
		"\0\46\0\u0153\0\46\0\u0154\0\46\0\u0160\0\46\0\u0168\0\46\0\u016a\0\46\0\u016c\0" +
		"\46\0\u016f\0\46\0\u0171\0\46\0\u0172\0\46\0\u0173\0\46\0\u0181\0\46\0\u0183\0\46" +
		"\0\u0189\0\46\0\u018d\0\46\0\u0190\0\46\0\u0192\0\46\0\u019d\0\46\0\u01a3\0\46\0" +
		"\u01a5\0\46\0\u01b3\0\46\0\1\0\47\0\2\0\47\0\6\0\47\0\67\0\47\0\71\0\47\0\73\0\47" +
		"\0\74\0\47\0\75\0\47\0\100\0\47\0\102\0\47\0\103\0\47\0\126\0\47\0\127\0\47\0\134" +
		"\0\47\0\135\0\47\0\150\0\47\0\152\0\47\0\156\0\47\0\163\0\47\0\164\0\47\0\165\0\47" +
		"\0\166\0\47\0\201\0\47\0\204\0\47\0\206\0\47\0\214\0\47\0\216\0\47\0\224\0\47\0\231" +
		"\0\47\0\240\0\47\0\242\0\47\0\243\0\47\0\251\0\47\0\253\0\47\0\256\0\47\0\257\0\47" +
		"\0\260\0\47\0\264\0\47\0\300\0\47\0\323\0\47\0\324\0\47\0\326\0\47\0\327\0\u0123" +
		"\0\330\0\47\0\331\0\47\0\332\0\47\0\333\0\47\0\346\0\47\0\350\0\47\0\362\0\47\0\365" +
		"\0\47\0\371\0\47\0\376\0\47\0\u0100\0\47\0\u0101\0\47\0\u0104\0\47\0\u0119\0\47\0" +
		"\u011b\0\47\0\u011f\0\47\0\u0122\0\47\0\u0123\0\47\0\u0125\0\47\0\u012a\0\47\0\u012b" +
		"\0\47\0\u012f\0\47\0\u0130\0\47\0\u0131\0\47\0\u0132\0\47\0\u0133\0\47\0\u0135\0" +
		"\47\0\u013d\0\47\0\u0140\0\47\0\u0141\0\47\0\u0144\0\47\0\u014a\0\47\0\u014d\0\47" +
		"\0\u014f\0\47\0\u0151\0\47\0\u0153\0\47\0\u0154\0\47\0\u0160\0\47\0\u0168\0\47\0" +
		"\u016a\0\47\0\u016c\0\47\0\u016f\0\47\0\u0171\0\47\0\u0172\0\47\0\u0173\0\47\0\u0181" +
		"\0\47\0\u0183\0\47\0\u0189\0\47\0\u018d\0\47\0\u0190\0\47\0\u0192\0\47\0\u019d\0" +
		"\47\0\u01a3\0\47\0\u01a5\0\47\0\u01b3\0\47\0\1\0\50\0\2\0\50\0\6\0\50\0\67\0\50\0" +
		"\71\0\50\0\73\0\50\0\74\0\50\0\75\0\50\0\100\0\50\0\102\0\50\0\103\0\50\0\126\0\50" +
		"\0\127\0\50\0\134\0\50\0\135\0\50\0\150\0\50\0\152\0\50\0\156\0\50\0\163\0\50\0\164" +
		"\0\50\0\165\0\50\0\166\0\50\0\201\0\50\0\204\0\50\0\206\0\50\0\214\0\50\0\216\0\50" +
		"\0\224\0\50\0\231\0\50\0\240\0\50\0\242\0\50\0\243\0\50\0\251\0\50\0\253\0\50\0\256" +
		"\0\50\0\257\0\50\0\260\0\50\0\264\0\50\0\300\0\50\0\303\0\u010c\0\323\0\50\0\324" +
		"\0\50\0\326\0\50\0\330\0\50\0\331\0\50\0\332\0\50\0\333\0\50\0\346\0\50\0\350\0\50" +
		"\0\362\0\50\0\365\0\50\0\371\0\50\0\376\0\50\0\u0100\0\50\0\u0101\0\50\0\u0104\0" +
		"\50\0\u0119\0\50\0\u011b\0\50\0\u011f\0\50\0\u0122\0\50\0\u0123\0\50\0\u0125\0\50" +
		"\0\u012a\0\50\0\u012b\0\50\0\u012f\0\50\0\u0130\0\50\0\u0131\0\50\0\u0132\0\50\0" +
		"\u0133\0\50\0\u0135\0\50\0\u013d\0\50\0\u0140\0\50\0\u0141\0\50\0\u0144\0\50\0\u014a" +
		"\0\50\0\u014d\0\50\0\u014f\0\50\0\u0151\0\50\0\u0153\0\50\0\u0154\0\50\0\u0160\0" +
		"\50\0\u0168\0\50\0\u016a\0\50\0\u016c\0\50\0\u016f\0\50\0\u0171\0\50\0\u0172\0\50" +
		"\0\u0173\0\50\0\u0181\0\50\0\u0183\0\50\0\u0189\0\50\0\u018d\0\50\0\u0190\0\50\0" +
		"\u0192\0\50\0\u019d\0\50\0\u01a3\0\50\0\u01a5\0\50\0\u01b3\0\50\0\1\0\51\0\2\0\51" +
		"\0\6\0\51\0\67\0\51\0\71\0\51\0\73\0\51\0\74\0\51\0\75\0\51\0\100\0\51\0\102\0\51" +
		"\0\103\0\51\0\126\0\51\0\127\0\51\0\134\0\51\0\135\0\51\0\150\0\51\0\152\0\51\0\156" +
		"\0\51\0\163\0\51\0\164\0\51\0\165\0\51\0\166\0\51\0\201\0\51\0\204\0\51\0\205\0\265" +
		"\0\206\0\51\0\214\0\51\0\216\0\51\0\224\0\51\0\231\0\51\0\240\0\51\0\242\0\51\0\243" +
		"\0\51\0\251\0\51\0\253\0\51\0\256\0\51\0\257\0\51\0\260\0\51\0\264\0\51\0\271\0\265" +
		"\0\275\0\265\0\300\0\51\0\323\0\51\0\324\0\51\0\326\0\51\0\330\0\51\0\331\0\51\0" +
		"\332\0\51\0\333\0\51\0\346\0\51\0\350\0\51\0\362\0\51\0\365\0\51\0\371\0\51\0\376" +
		"\0\51\0\u0100\0\51\0\u0101\0\51\0\u0104\0\51\0\u0107\0\265\0\u0119\0\51\0\u011b\0" +
		"\51\0\u011f\0\51\0\u0122\0\51\0\u0123\0\51\0\u0125\0\51\0\u012a\0\51\0\u012b\0\51" +
		"\0\u012f\0\51\0\u0130\0\51\0\u0131\0\51\0\u0132\0\51\0\u0133\0\51\0\u0135\0\51\0" +
		"\u013d\0\51\0\u0140\0\51\0\u0141\0\51\0\u0144\0\51\0\u014a\0\51\0\u014d\0\51\0\u014f" +
		"\0\51\0\u0151\0\51\0\u0153\0\51\0\u0154\0\51\0\u0160\0\51\0\u0168\0\51\0\u016a\0" +
		"\51\0\u016c\0\51\0\u016f\0\51\0\u0171\0\51\0\u0172\0\51\0\u0173\0\51\0\u0181\0\51" +
		"\0\u0183\0\51\0\u0189\0\51\0\u018d\0\51\0\u0190\0\51\0\u0192\0\51\0\u019d\0\51\0" +
		"\u01a3\0\51\0\u01a5\0\51\0\u01b3\0\51\0\1\0\52\0\2\0\52\0\6\0\52\0\67\0\52\0\71\0" +
		"\52\0\73\0\52\0\74\0\52\0\75\0\52\0\100\0\52\0\102\0\52\0\103\0\52\0\126\0\52\0\127" +
		"\0\52\0\134\0\52\0\135\0\52\0\147\0\166\0\150\0\52\0\152\0\52\0\156\0\52\0\163\0" +
		"\52\0\164\0\52\0\165\0\52\0\166\0\52\0\201\0\52\0\204\0\52\0\206\0\52\0\214\0\52" +
		"\0\216\0\52\0\224\0\52\0\231\0\52\0\240\0\52\0\242\0\52\0\243\0\52\0\251\0\52\0\253" +
		"\0\52\0\256\0\52\0\257\0\52\0\260\0\52\0\264\0\52\0\300\0\52\0\323\0\52\0\324\0\52" +
		"\0\326\0\52\0\330\0\52\0\331\0\52\0\332\0\52\0\333\0\52\0\346\0\52\0\350\0\52\0\362" +
		"\0\52\0\365\0\52\0\371\0\52\0\376\0\52\0\u0100\0\52\0\u0101\0\52\0\u0104\0\52\0\u0119" +
		"\0\52\0\u011b\0\52\0\u011f\0\52\0\u0122\0\52\0\u0123\0\52\0\u0125\0\52\0\u012a\0" +
		"\52\0\u012b\0\52\0\u012f\0\52\0\u0130\0\52\0\u0131\0\52\0\u0132\0\52\0\u0133\0\52" +
		"\0\u0135\0\52\0\u013d\0\52\0\u0140\0\52\0\u0141\0\52\0\u0144\0\52\0\u014a\0\52\0" +
		"\u014d\0\52\0\u014f\0\52\0\u0151\0\52\0\u0153\0\52\0\u0154\0\52\0\u0160\0\52\0\u0168" +
		"\0\52\0\u016a\0\52\0\u016c\0\52\0\u016f\0\52\0\u0171\0\52\0\u0172\0\52\0\u0173\0" +
		"\52\0\u0181\0\52\0\u0183\0\52\0\u0189\0\52\0\u018d\0\52\0\u0190\0\52\0\u0192\0\52" +
		"\0\u019d\0\52\0\u01a3\0\52\0\u01a5\0\52\0\u01b3\0\52\0\151\0\174\0\202\0\174\0\205" +
		"\0\174\0\256\0\336\0\271\0\174\0\275\0\174\0\332\0\336\0\346\0\336\0\350\0\336\0" +
		"\376\0\336\0\u0100\0\336\0\u0101\0\336\0\u0104\0\336\0\u0107\0\174\0\u012a\0\336" +
		"\0\u012f\0\336\0\u0133\0\336\0\u0135\0\336\0\u014a\0\336\0\u014d\0\336\0\u014f\0" +
		"\336\0\u0151\0\336\0\u0153\0\336\0\u0154\0\336\0\u0159\0\336\0\u0189\0\336\0\u018d" +
		"\0\336\0\u0190\0\336\0\u0192\0\336\0\u0195\0\336\0\u01b3\0\336\0\156\0\201\0\173" +
		"\0\226\0\230\0\226\0\307\0\226\0\364\0\u0141\0\1\0\53\0\2\0\57\0\6\0\53\0\67\0\104" +
		"\0\71\0\111\0\73\0\57\0\74\0\114\0\75\0\116\0\100\0\53\0\102\0\104\0\103\0\104\0" +
		"\126\0\140\0\127\0\104\0\134\0\151\0\135\0\53\0\150\0\170\0\152\0\151\0\156\0\202" +
		"\0\163\0\205\0\164\0\140\0\165\0\217\0\166\0\217\0\201\0\151\0\204\0\254\0\206\0" +
		"\205\0\214\0\275\0\216\0\140\0\224\0\170\0\231\0\151\0\240\0\314\0\242\0\140\0\243" +
		"\0\320\0\251\0\140\0\253\0\325\0\256\0\337\0\257\0\364\0\260\0\365\0\264\0\140\0" +
		"\300\0\217\0\323\0\140\0\324\0\u011e\0\326\0\53\0\330\0\u0124\0\331\0\104\0\332\0" +
		"\337\0\333\0\140\0\346\0\337\0\350\0\337\0\362\0\337\0\365\0\u0143\0\371\0\140\0" +
		"\376\0\337\0\u0100\0\337\0\u0101\0\337\0\u0104\0\337\0\u0119\0\140\0\u011b\0\u015f" +
		"\0\u011f\0\140\0\u0122\0\140\0\u0123\0\140\0\u0125\0\104\0\u012a\0\337\0\u012b\0" +
		"\140\0\u012f\0\337\0\u0130\0\u0173\0\u0131\0\53\0\u0132\0\53\0\u0133\0\337\0\u0135" +
		"\0\337\0\u013d\0\53\0\u0140\0\u017f\0\u0141\0\320\0\u0144\0\365\0\u014a\0\337\0\u014d" +
		"\0\337\0\u014f\0\337\0\u0151\0\337\0\u0153\0\337\0\u0154\0\337\0\u0160\0\140\0\u0168" +
		"\0\104\0\u016a\0\104\0\u016c\0\140\0\u016f\0\140\0\u0171\0\u0173\0\u0172\0\u0173" +
		"\0\u0173\0\53\0\u0181\0\140\0\u0183\0\140\0\u0189\0\337\0\u018d\0\337\0\u0190\0\337" +
		"\0\u0192\0\337\0\u019d\0\140\0\u01a3\0\u0173\0\u01a5\0\u0173\0\u01b3\0\337\0\1\0" +
		"\54\0\6\0\54\0\100\0\54\0\126\0\141\0\135\0\54\0\326\0\54\0\u011f\0\141\0\u013d\0" +
		"\u017d\0\u0160\0\141\0\u0166\0\u0199\0\u0167\0\u019a\0\u0181\0\141\0\173\0\227\0" +
		"\230\0\304\0\307\0\u0110\0\2\0\60\0\73\0\60\0\2\0\61\0\73\0\112\0\256\0\340\0\332" +
		"\0\340\0\346\0\340\0\350\0\340\0\376\0\340\0\u0100\0\340\0\u0101\0\340\0\u0104\0" +
		"\340\0\u012a\0\340\0\u012f\0\340\0\u0133\0\340\0\u0135\0\340\0\u014a\0\340\0\u014d" +
		"\0\340\0\u014f\0\340\0\u0151\0\340\0\u0153\0\340\0\u0154\0\340\0\u0159\0\u0193\0" +
		"\u0189\0\340\0\u018d\0\340\0\u0190\0\340\0\u0192\0\340\0\u0195\0\u0193\0\u01b3\0" +
		"\340\0\1\0\55\0\6\0\55\0\75\0\117\0\100\0\55\0\135\0\55\0\152\0\177\0\204\0\255\0" +
		"\206\0\272\0\231\0\177\0\256\0\341\0\326\0\55\0\332\0\341\0\350\0\u0137\0\376\0\341" +
		"\0\u0100\0\341\0\u0101\0\341\0\u0104\0\341\0\u012a\0\u0137\0\u012f\0\341\0\u0133" +
		"\0\341\0\u0135\0\u0137\0\u014a\0\341\0\u014d\0\341\0\u014f\0\341\0\u0151\0\341\0" +
		"\u0153\0\341\0\u0154\0\341\0\u0189\0\341\0\u018d\0\341\0\u0190\0\341\0\u0192\0\341" +
		"\0\u01b3\0\341\0\3\0\62\0\0\0\u01c3\0\62\0\75\0\0\0\3\0\75\0\120\0\120\0\137\0\62" +
		"\0\76\0\75\0\121\0\1\0\56\0\6\0\56\0\100\0\56\0\135\0\56\0\256\0\342\0\326\0\56\0" +
		"\332\0\342\0\346\0\342\0\350\0\342\0\362\0\342\0\376\0\342\0\u0100\0\342\0\u0101" +
		"\0\342\0\u0104\0\342\0\u012a\0\342\0\u012f\0\342\0\u0130\0\u0174\0\u0131\0\342\0" +
		"\u0132\0\342\0\u0133\0\342\0\u0135\0\342\0\u013d\0\u017e\0\u014a\0\342\0\u014d\0" +
		"\342\0\u014f\0\342\0\u0151\0\342\0\u0153\0\342\0\u0154\0\342\0\u0171\0\u0174\0\u0172" +
		"\0\u0174\0\u0173\0\u01a2\0\u0189\0\342\0\u018d\0\342\0\u0190\0\342\0\u0192\0\342" +
		"\0\u01a3\0\u0174\0\u01a5\0\u0174\0\u01b3\0\342\0\126\0\142\0\164\0\216\0\216\0\276" +
		"\0\242\0\315\0\251\0\322\0\264\0\375\0\323\0\u011d\0\333\0\u012c\0\371\0\u0146\0" +
		"\u0119\0\315\0\u011f\0\142\0\u0122\0\u0163\0\u0123\0\u0164\0\u012b\0\u016d\0\u0160" +
		"\0\142\0\u016c\0\322\0\u016f\0\u012c\0\u0181\0\142\0\u0183\0\u01a9\0\u019d\0\u011d" +
		"\0\151\0\175\0\202\0\175\0\205\0\266\0\271\0\266\0\275\0\266\0\u0107\0\266\0\134" +
		"\0\152\0\201\0\231\0\134\0\153\0\152\0\200\0\201\0\153\0\231\0\200\0\134\0\154\0" +
		"\152\0\154\0\201\0\154\0\231\0\154\0\134\0\155\0\152\0\155\0\201\0\155\0\231\0\155" +
		"\0\134\0\156\0\152\0\156\0\201\0\156\0\231\0\156\0\150\0\171\0\134\0\157\0\152\0" +
		"\157\0\201\0\157\0\231\0\157\0\230\0\305\0\307\0\u0111\0\u010f\0\u0158\0\u015a\0" +
		"\u0158\0\303\0\u010d\0\134\0\160\0\152\0\160\0\201\0\160\0\231\0\160\0\165\0\220" +
		"\0\166\0\222\0\134\0\161\0\152\0\161\0\201\0\161\0\231\0\161\0\150\0\172\0\224\0" +
		"\302\0\165\0\221\0\166\0\221\0\300\0\u0109\0\163\0\206\0\163\0\207\0\206\0\273\0" +
		"\163\0\210\0\206\0\210\0\205\0\267\0\271\0\u0102\0\275\0\u0105\0\u0107\0\u0155\0" +
		"\261\0\372\0\374\0\372\0\203\0\251\0\203\0\252\0\163\0\211\0\206\0\211\0\163\0\212" +
		"\0\206\0\212\0\243\0\321\0\u0141\0\u0180\0\242\0\316\0\242\0\317\0\u0119\0\u015e" +
		"\0\251\0\323\0\u016c\0\u019d\0\371\0\u0147\0\256\0\343\0\332\0\343\0\376\0\343\0" +
		"\u0100\0\343\0\u0101\0\343\0\u0104\0\343\0\u012f\0\343\0\u014a\0\343\0\u014d\0\343" +
		"\0\u014f\0\343\0\u0151\0\343\0\u0153\0\343\0\u0154\0\343\0\u0189\0\343\0\u018d\0" +
		"\343\0\u0190\0\343\0\u0192\0\343\0\u01b3\0\343\0\256\0\344\0\332\0\u0129\0\376\0" +
		"\u0149\0\u0100\0\u014b\0\u0101\0\u014c\0\u0104\0\u0150\0\u012f\0\u0170\0\u014a\0" +
		"\u0185\0\u014d\0\u0188\0\u014f\0\u018a\0\u0151\0\u018c\0\u0153\0\u018e\0\u0154\0" +
		"\u018f\0\u0189\0\u01ac\0\u018d\0\u01af\0\u0190\0\u01b2\0\u0192\0\u01b4\0\u01b3\0" +
		"\u01be\0\256\0\345\0\332\0\345\0\376\0\345\0\u0100\0\345\0\u0101\0\345\0\u0104\0" +
		"\345\0\u012f\0\345\0\u0133\0\u0179\0\u014a\0\345\0\u014d\0\345\0\u014f\0\345\0\u0151" +
		"\0\345\0\u0153\0\345\0\u0154\0\345\0\u0189\0\345\0\u018d\0\345\0\u0190\0\345\0\u0192" +
		"\0\345\0\u01b3\0\345\0\256\0\346\0\332\0\346\0\376\0\346\0\u0100\0\346\0\u0101\0" +
		"\346\0\u0104\0\346\0\u012f\0\346\0\u0133\0\346\0\u014a\0\346\0\u014d\0\346\0\u014f" +
		"\0\346\0\u0151\0\346\0\u0153\0\346\0\u0154\0\346\0\u0189\0\346\0\u018d\0\346\0\u0190" +
		"\0\346\0\u0192\0\346\0\u01b3\0\346\0\256\0\347\0\332\0\347\0\346\0\347\0\350\0\347" +
		"\0\376\0\347\0\u0100\0\347\0\u0101\0\347\0\u0104\0\347\0\u012a\0\347\0\u012f\0\347" +
		"\0\u0133\0\347\0\u0135\0\347\0\u014a\0\347\0\u014d\0\347\0\u014f\0\347\0\u0151\0" +
		"\347\0\u0153\0\347\0\u0154\0\347\0\u0189\0\347\0\u018d\0\347\0\u0190\0\347\0\u0192" +
		"\0\347\0\u01b3\0\347\0\205\0\270\0\267\0\377\0\271\0\u0103\0\275\0\u0106\0\363\0" +
		"\u013f\0\u0102\0\u014e\0\u0105\0\u0152\0\u0107\0\u0156\0\u0136\0\u017b\0\u0139\0" +
		"\u017c\0\u0155\0\u0191\0\u017a\0\u01a6\0\364\0\u0142\0\u0180\0\u01a7\0\256\0\350" +
		"\0\332\0\u012a\0\346\0\u0135\0\376\0\350\0\u0100\0\350\0\u0101\0\350\0\u0104\0\350" +
		"\0\u012f\0\350\0\u0133\0\350\0\u014a\0\350\0\u014d\0\350\0\u014f\0\350\0\u0151\0" +
		"\350\0\u0153\0\350\0\u0154\0\350\0\u0189\0\350\0\u018d\0\350\0\u0190\0\350\0\u0192" +
		"\0\350\0\u01b3\0\350\0\256\0\351\0\332\0\351\0\346\0\351\0\350\0\u0138\0\376\0\351" +
		"\0\u0100\0\351\0\u0101\0\351\0\u0104\0\351\0\u012a\0\u0138\0\u012f\0\351\0\u0133" +
		"\0\351\0\u0135\0\u0138\0\u014a\0\351\0\u014d\0\351\0\u014f\0\351\0\u0151\0\351\0" +
		"\u0153\0\351\0\u0154\0\351\0\u0189\0\351\0\u018d\0\351\0\u0190\0\351\0\u0192\0\351" +
		"\0\u01b3\0\351\0\333\0\u012d\0\256\0\352\0\332\0\352\0\346\0\352\0\350\0\352\0\376" +
		"\0\352\0\u0100\0\352\0\u0101\0\352\0\u0104\0\352\0\u012a\0\352\0\u012f\0\352\0\u0133" +
		"\0\352\0\u0135\0\352\0\u014a\0\352\0\u014d\0\352\0\u014f\0\352\0\u0151\0\352\0\u0153" +
		"\0\352\0\u0154\0\352\0\u0189\0\352\0\u018d\0\352\0\u0190\0\352\0\u0192\0\352\0\u01b3" +
		"\0\352\0\333\0\u012e\0\u016f\0\u019e\0\256\0\353\0\332\0\353\0\346\0\353\0\350\0" +
		"\353\0\376\0\353\0\u0100\0\353\0\u0101\0\353\0\u0104\0\353\0\u012a\0\353\0\u012f" +
		"\0\353\0\u0133\0\353\0\u0135\0\353\0\u014a\0\353\0\u014d\0\353\0\u014f\0\353\0\u0151" +
		"\0\353\0\u0153\0\353\0\u0154\0\353\0\u0189\0\353\0\u018d\0\353\0\u0190\0\353\0\u0192" +
		"\0\353\0\u01b3\0\353\0\256\0\354\0\332\0\354\0\346\0\354\0\350\0\354\0\376\0\354" +
		"\0\u0100\0\354\0\u0101\0\354\0\u0104\0\354\0\u012a\0\354\0\u012f\0\354\0\u0133\0" +
		"\354\0\u0135\0\354\0\u014a\0\354\0\u014d\0\354\0\u014f\0\354\0\u0151\0\354\0\u0153" +
		"\0\354\0\u0154\0\354\0\u0189\0\354\0\u018d\0\354\0\u0190\0\354\0\u0192\0\354\0\u01b3" +
		"\0\354\0\256\0\355\0\332\0\355\0\346\0\355\0\350\0\355\0\362\0\u013e\0\376\0\355" +
		"\0\u0100\0\355\0\u0101\0\355\0\u0104\0\355\0\u012a\0\355\0\u012f\0\355\0\u0133\0" +
		"\355\0\u0135\0\355\0\u014a\0\355\0\u014d\0\355\0\u014f\0\355\0\u0151\0\355\0\u0153" +
		"\0\355\0\u0154\0\355\0\u0189\0\355\0\u018d\0\355\0\u0190\0\355\0\u0192\0\355\0\u01b3" +
		"\0\355\0\256\0\356\0\332\0\356\0\346\0\356\0\350\0\356\0\362\0\356\0\376\0\356\0" +
		"\u0100\0\356\0\u0101\0\356\0\u0104\0\356\0\u012a\0\356\0\u012f\0\356\0\u0131\0\u0177" +
		"\0\u0132\0\u0178\0\u0133\0\356\0\u0135\0\356\0\u014a\0\356\0\u014d\0\356\0\u014f" +
		"\0\356\0\u0151\0\356\0\u0153\0\356\0\u0154\0\356\0\u0189\0\356\0\u018d\0\356\0\u0190" +
		"\0\356\0\u0192\0\356\0\u01b3\0\356\0\256\0\357\0\332\0\357\0\346\0\357\0\350\0\357" +
		"\0\362\0\357\0\376\0\357\0\u0100\0\357\0\u0101\0\357\0\u0104\0\357\0\u012a\0\357" +
		"\0\u012f\0\357\0\u0131\0\357\0\u0132\0\357\0\u0133\0\357\0\u0135\0\357\0\u014a\0" +
		"\357\0\u014d\0\357\0\u014f\0\357\0\u0151\0\357\0\u0153\0\357\0\u0154\0\357\0\u0189" +
		"\0\357\0\u018d\0\357\0\u0190\0\357\0\u0192\0\357\0\u01b3\0\357\0\256\0\360\0\332" +
		"\0\360\0\346\0\360\0\350\0\360\0\362\0\360\0\376\0\360\0\u0100\0\360\0\u0101\0\360" +
		"\0\u0104\0\360\0\u012a\0\360\0\u012f\0\360\0\u0131\0\360\0\u0132\0\360\0\u0133\0" +
		"\360\0\u0135\0\360\0\u014a\0\360\0\u014d\0\360\0\u014f\0\360\0\u0151\0\360\0\u0153" +
		"\0\360\0\u0154\0\360\0\u0189\0\360\0\u018d\0\360\0\u0190\0\360\0\u0192\0\360\0\u01b3" +
		"\0\360\0\256\0\361\0\310\0\u0112\0\311\0\u0113\0\332\0\361\0\346\0\361\0\350\0\361" +
		"\0\362\0\361\0\376\0\361\0\u0100\0\361\0\u0101\0\361\0\u0104\0\361\0\u0116\0\u015d" +
		"\0\u012a\0\361\0\u012f\0\361\0\u0131\0\361\0\u0132\0\361\0\u0133\0\361\0\u0135\0" +
		"\361\0\u014a\0\361\0\u014d\0\361\0\u014f\0\361\0\u0151\0\361\0\u0153\0\361\0\u0154" +
		"\0\361\0\u0189\0\361\0\u018d\0\361\0\u0190\0\361\0\u0192\0\361\0\u01b3\0\361\0\u0130" +
		"\0\u0175\0\u0171\0\u0175\0\u0172\0\u01a1\0\u01a3\0\u0175\0\u01a5\0\u0175\0\u0130" +
		"\0\u0176\0\u0171\0\u01a0\0\u01a3\0\u01b9\0\u01a5\0\u01ba\0\163\0\213\0\206\0\213" +
		"\0\256\0\213\0\332\0\213\0\346\0\213\0\350\0\213\0\376\0\213\0\u0100\0\213\0\u0101" +
		"\0\213\0\u0104\0\213\0\u012a\0\213\0\u012f\0\213\0\u0133\0\213\0\u0135\0\213\0\u014a" +
		"\0\213\0\u014d\0\213\0\u014f\0\213\0\u0151\0\213\0\u0153\0\213\0\u0154\0\213\0\u0189" +
		"\0\213\0\u018d\0\213\0\u0190\0\213\0\u0192\0\213\0\u01b3\0\213\0\163\0\214\0\206" +
		"\0\214\0\256\0\362\0\332\0\362\0\346\0\362\0\350\0\362\0\376\0\362\0\u0100\0\362" +
		"\0\u0101\0\362\0\u0104\0\362\0\u012a\0\362\0\u012f\0\362\0\u0133\0\362\0\u0135\0" +
		"\362\0\u014a\0\362\0\u014d\0\362\0\u014f\0\362\0\u0151\0\362\0\u0153\0\362\0\u0154" +
		"\0\362\0\u0189\0\362\0\u018d\0\362\0\u0190\0\362\0\u0192\0\362\0\u01b3\0\362\0\163" +
		"\0\215\0\206\0\215\0\213\0\274\0\256\0\215\0\332\0\215\0\346\0\215\0\350\0\215\0" +
		"\376\0\215\0\u0100\0\215\0\u0101\0\215\0\u0104\0\215\0\u012a\0\215\0\u012f\0\215" +
		"\0\u0133\0\215\0\u0135\0\215\0\u014a\0\215\0\u014d\0\215\0\u014f\0\215\0\u0151\0" +
		"\215\0\u0153\0\215\0\u0154\0\215\0\u0189\0\215\0\u018d\0\215\0\u0190\0\215\0\u0192" +
		"\0\215\0\u01b3\0\215\0\260\0\366\0\205\0\271\0\275\0\u0107\0\260\0\367\0\u0144\0" +
		"\u0182\0\67\0\105\0\102\0\124\0\103\0\125\0\127\0\105\0\260\0\370\0\331\0\u0126\0" +
		"\u0125\0\u0165\0\u0144\0\370\0\u0168\0\u0126\0\u016a\0\u0126\0\67\0\106\0\67\0\107" +
		"\0\53\0\70\0\337\0\70\0\u0173\0\70\0\67\0\110\0\127\0\144\0\203\0\253\0\252\0\324" +
		"\0\126\0\143\0\u011f\0\u0162\0\u0160\0\u0197\0\u0181\0\u01a8\0\331\0\u0127\0\u0168" +
		"\0\u0127\0\u016a\0\u0127\0\331\0\u0128\0\u0168\0\u019b\0\u016a\0\u019c\0\1\0\u01c4" +
		"\0\6\0\65\0\100\0\123\0\135\0\162\0\326\0\u0121\0\6\0\66\0\151\0\176\0\202\0\232" +
		"\0\304\0\u010f\0\u0110\0\u015a\0\u010f\0\u0159\0\u015a\0\u0195\0\u0159\0\u0194\0" +
		"\u0195\0\u01b5\0\261\0\373\0\374\0\u0148\0\256\0\363\0\332\0\363\0\346\0\u0136\0" +
		"\350\0\u0139\0\376\0\363\0\u0100\0\363\0\u0101\0\363\0\u0104\0\363\0\u012a\0\u0139" +
		"\0\u012f\0\363\0\u0133\0\363\0\u0135\0\u017a\0\u014a\0\363\0\u014d\0\363\0\u014f" +
		"\0\363\0\u0151\0\363\0\u0153\0\363\0\u0154\0\363\0\u0189\0\363\0\u018d\0\363\0\u0190" +
		"\0\363\0\u0192\0\363\0\u01b3\0\363\0");

	private static final int[] tmRuleLen = TMLexer.unpack_int(262,
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\3\0" +
		"\1\0\1\0\1\0\2\0\0\0\5\0\4\0\2\0\0\0\6\0\3\0\3\0\3\0\4\0\3\0\3\0\1\0\2\0\1\0\1\0" +
		"\1\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\3\0\4\0\3\0\3\0\3\0\1\0\10\0\5\0\4\0\7\0\4\0" +
		"\3\0\3\0\1\0\1\0\1\0\5\0\3\0\1\0\4\0\4\0\1\0\1\0\1\0\2\0\2\0\1\0\1\0\1\0\10\0\7\0" +
		"\7\0\6\0\7\0\6\0\6\0\5\0\7\0\6\0\6\0\5\0\6\0\5\0\5\0\4\0\2\0\3\0\2\0\1\0\1\0\1\0" +
		"\2\0\1\0\1\0\1\0\1\0\1\0\1\0\7\0\5\0\6\0\4\0\4\0\4\0\4\0\5\0\5\0\6\0\4\0\4\0\3\0" +
		"\1\0\3\0\1\0\2\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\1\0\4\0\3\0\3\0\2\0\3\0\2\0\2\0\1\0" +
		"\1\0\3\0\3\0\3\0\5\0\4\0\3\0\2\0\2\0\1\0\2\0\2\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\2\0" +
		"\1\0\2\0\1\0\2\0\1\0\3\0\3\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0\6\0\6\0\2\0\2\0\4\0\1\0" +
		"\4\0\2\0\1\0\3\0\2\0\1\0\3\0\3\0\2\0\1\0\1\0\4\0\2\0\2\0\3\0\1\0\3\0\1\0\4\0\2\0" +
		"\1\0\3\0\1\0\1\0\0\0\3\0\3\0\2\0\2\0\1\0\1\0\1\0\1\0\1\0\2\0\1\0\3\0\3\0\1\0\3\0" +
		"\3\0\1\0\1\0\4\0\3\0\3\0\2\0\1\0\3\0\1\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0" +
		"\1\0\0\0");

	private static final int[] tmRuleSymbol = TMLexer.unpack_int(262,
		"\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121" +
		"\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0\121\0" +
		"\121\0\121\0\121\0\121\0\121\0\121\0\122\0\122\0\122\0\122\0\123\0\124\0\124\0\125" +
		"\0\126\0\127\0\130\0\130\0\131\0\131\0\132\0\132\0\133\0\133\0\134\0\135\0\136\0" +
		"\136\0\137\0\137\0\140\0\140\0\141\0\142\0\143\0\143\0\143\0\144\0\144\0\144\0\144" +
		"\0\144\0\145\0\146\0\147\0\147\0\150\0\150\0\151\0\151\0\151\0\151\0\151\0\151\0" +
		"\152\0\153\0\153\0\153\0\154\0\155\0\155\0\156\0\156\0\157\0\160\0\161\0\161\0\161" +
		"\0\162\0\162\0\162\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0\163\0" +
		"\163\0\163\0\163\0\163\0\163\0\163\0\164\0\164\0\164\0\164\0\164\0\164\0\165\0\166" +
		"\0\166\0\166\0\167\0\167\0\167\0\170\0\170\0\170\0\170\0\171\0\171\0\171\0\171\0" +
		"\171\0\171\0\171\0\171\0\172\0\172\0\173\0\173\0\174\0\174\0\175\0\175\0\176\0\176" +
		"\0\177\0\177\0\200\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\201\0\202\0" +
		"\203\0\203\0\204\0\204\0\204\0\204\0\205\0\206\0\206\0\206\0\207\0\207\0\207\0\207" +
		"\0\210\0\210\0\211\0\212\0\212\0\213\0\214\0\214\0\215\0\215\0\215\0\216\0\216\0" +
		"\217\0\217\0\217\0\220\0\220\0\220\0\220\0\220\0\220\0\220\0\220\0\221\0\222\0\222" +
		"\0\222\0\222\0\223\0\223\0\223\0\224\0\224\0\225\0\226\0\226\0\226\0\227\0\227\0" +
		"\230\0\231\0\231\0\231\0\232\0\233\0\233\0\234\0\234\0\235\0\236\0\236\0\236\0\236" +
		"\0\237\0\237\0\240\0\240\0\241\0\241\0\241\0\241\0\242\0\242\0\242\0\243\0\243\0" +
		"\243\0\243\0\243\0\243\0\243\0\244\0\244\0\245\0\245\0\246\0\246\0\247\0\247\0\250" +
		"\0\250\0\251\0\251\0\252\0\252\0");

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
		int rawTypeopt = 165;
		int iconopt = 166;
		int lexeme_attrsopt = 167;
		int commandopt = 168;
		int implements_clauseopt = 169;
		int rhsSuffixopt = 170;
	}

	public interface Rules {
		int nonterm_type_nontermTypeAST = 114;  // nonterm_type : 'returns' symref_noargs
		int nonterm_type_nontermTypeHint = 115;  // nonterm_type : 'inline' 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint2 = 116;  // nonterm_type : 'class' implements_clauseopt
		int nonterm_type_nontermTypeHint3 = 117;  // nonterm_type : 'interface'
		int nonterm_type_nontermTypeHint4 = 118;  // nonterm_type : 'void'
		int directive_directivePrio = 131;  // directive : '%' assoc references ';'
		int directive_directiveInput = 132;  // directive : '%' 'input' inputref_list_Comma_separated ';'
		int directive_directiveInterface = 133;  // directive : '%' 'interface' identifier_list_Comma_separated ';'
		int directive_directiveAssert = 134;  // directive : '%' 'assert' 'empty' rhsSet ';'
		int directive_directiveAssert2 = 135;  // directive : '%' 'assert' 'nonempty' rhsSet ';'
		int directive_directiveSet = 136;  // directive : '%' 'generate' identifier '=' rhsSet ';'
		int directive_directiveExpect = 137;  // directive : '%' 'expect' icon ';'
		int directive_directiveExpectRR = 138;  // directive : '%' 'expect-rr' icon ';'
		int rhsOptional_rhsQuantifier = 188;  // rhsOptional : rhsCast '?'
		int rhsCast_rhsAsLiteral = 191;  // rhsCast : rhsPrimary 'as' literal
		int rhsPrimary_rhsSymbol = 192;  // rhsPrimary : symref
		int rhsPrimary_rhsNested = 193;  // rhsPrimary : '(' rules ')'
		int rhsPrimary_rhsList = 194;  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
		int rhsPrimary_rhsList2 = 195;  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
		int rhsPrimary_rhsQuantifier = 196;  // rhsPrimary : rhsPrimary '*'
		int rhsPrimary_rhsQuantifier2 = 197;  // rhsPrimary : rhsPrimary '+'
		int rhsPrimary_rhsIgnored = 198;  // rhsPrimary : '$' '(' rules ')'
		int setPrimary_setSymbol = 201;  // setPrimary : identifier symref
		int setPrimary_setSymbol2 = 202;  // setPrimary : symref
		int setPrimary_setCompound = 203;  // setPrimary : '(' setExpression ')'
		int setPrimary_setComplement = 204;  // setPrimary : '~' setPrimary
		int setExpression_setBinary = 206;  // setExpression : setExpression '|' setExpression
		int setExpression_setBinary2 = 207;  // setExpression : setExpression '&' setExpression
		int nonterm_param_inlineParameter = 218;  // nonterm_param : identifier identifier '=' param_value
		int nonterm_param_inlineParameter2 = 219;  // nonterm_param : identifier identifier
		int predicate_primary_boolPredicate = 234;  // predicate_primary : '!' param_ref
		int predicate_primary_boolPredicate2 = 235;  // predicate_primary : param_ref
		int predicate_primary_comparePredicate = 236;  // predicate_primary : param_ref '==' literal
		int predicate_primary_comparePredicate2 = 237;  // predicate_primary : param_ref '!=' literal
		int predicate_expression_predicateBinary = 239;  // predicate_expression : predicate_expression '&&' predicate_expression
		int predicate_expression_predicateBinary2 = 240;  // predicate_expression : predicate_expression '||' predicate_expression
		int expression_array = 243;  // expression : '[' expression_list_Comma_separated ',' ']'
		int expression_array2 = 244;  // expression : '[' expression_list_Comma_separated ']'
		int expression_array3 = 245;  // expression : '[' ',' ']'
		int expression_array4 = 246;  // expression : '[' ']'
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
			case 76:  // lexeme : start_conditions identifier rawTypeopt ':' lexeme_attrs
				tmLeft.value = new TmaLexeme(
						((TmaStartConditions)tmStack[tmHead - 4].value) /* startConditions */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((TmaRawType)tmStack[tmHead - 2].value) /* rawType */,
						null /* pattern */,
						null /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead].value) /* attrs */,
						null /* command */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // lexeme : start_conditions identifier rawTypeopt ':'
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
			case 78:  // lexeme : identifier rawTypeopt ':' pattern iconopt lexeme_attrsopt commandopt
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
			case 79:  // lexeme : identifier rawTypeopt ':' lexeme_attrs
				tmLeft.value = new TmaLexeme(
						null /* startConditions */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((TmaRawType)tmStack[tmHead - 2].value) /* rawType */,
						null /* pattern */,
						null /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead].value) /* attrs */,
						null /* command */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // lexeme : identifier rawTypeopt ':'
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
			case 81:  // lexeme_attrs : '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // lexeme_attribute : 'class'
				tmLeft.value = TmaLexemeAttribute.CLASS;
				break;
			case 83:  // lexeme_attribute : 'space'
				tmLeft.value = TmaLexemeAttribute.SPACE;
				break;
			case 84:  // lexeme_attribute : 'layout'
				tmLeft.value = TmaLexemeAttribute.LAYOUT;
				break;
			case 85:  // brackets_directive : '%' 'brackets' symref_noargs symref_noargs ';'
				tmLeft.value = new TmaBracketsDirective(
						((TmaSymref)tmStack[tmHead - 2].value) /* opening */,
						((TmaSymref)tmStack[tmHead - 1].value) /* closing */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 86:  // lexer_state_list_Comma_separated : lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 87:  // lexer_state_list_Comma_separated : lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 88:  // states_clause : '%' 's' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						false /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // states_clause : '%' 'x' lexer_state_list_Comma_separated ';'
				tmLeft.value = new TmaStatesClause(
						true /* exclusive */,
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 90:  // stateref : identifier
				tmLeft.value = new TmaStateref(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 91:  // lexer_state : identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // grammar_parts : grammar_part
				tmLeft.value = new ArrayList();
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 93:  // grammar_parts : grammar_parts grammar_part
				((List<ITmaGrammarPart>)tmLeft.value).add(((ITmaGrammarPart)tmStack[tmHead].value));
				break;
			case 94:  // grammar_parts : grammar_parts syntax_problem
				((List<ITmaGrammarPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 98:  // nonterm : annotations identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 7].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // nonterm : annotations identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // nonterm : annotations identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // nonterm : annotations identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // nonterm : annotations identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // nonterm : annotations identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // nonterm : annotations identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // nonterm : annotations identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // nonterm : identifier nonterm_params nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 5].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // nonterm : identifier nonterm_params nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // nonterm : identifier nonterm_params reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 4].value) /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // nonterm : identifier nonterm_params ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermParams)tmStack[tmHead - 3].value) /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // nonterm : identifier nonterm_type reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 4].value) /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // nonterm : identifier nonterm_type ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // nonterm : identifier reportClause ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						null /* params */,
						null /* type */,
						((TmaReportClause)tmStack[tmHead - 3].value) /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // nonterm : identifier ':' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* params */,
						null /* type */,
						null /* defaultAction */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // nonterm_type : 'returns' symref_noargs
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 115:  // nonterm_type : 'inline' 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						true /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // nonterm_type : 'class' implements_clauseopt
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.CLASS /* kind */,
						((List<TmaSymref>)tmStack[tmHead].value) /* implementsClause */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // nonterm_type : 'interface'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.INTERFACE /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // nonterm_type : 'void'
				tmLeft.value = new TmaNontermTypeHint(
						false /* inline */,
						TmaNontermTypeHint.TmaKindKind.VOID /* kind */,
						null /* implementsClause */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // implements_clause : 'implements' references_cs
				tmLeft.value = ((List<TmaSymref>)tmStack[tmHead].value);
				break;
			case 121:  // assoc : 'left'
				tmLeft.value = TmaAssoc.LEFT;
				break;
			case 122:  // assoc : 'right'
				tmLeft.value = TmaAssoc.RIGHT;
				break;
			case 123:  // assoc : 'nonassoc'
				tmLeft.value = TmaAssoc.NONASSOC;
				break;
			case 124:  // param_modifier : 'explicit'
				tmLeft.value = TmaParamModifier.EXPLICIT;
				break;
			case 125:  // param_modifier : 'global'
				tmLeft.value = TmaParamModifier.GLOBAL;
				break;
			case 126:  // param_modifier : 'lookahead'
				tmLeft.value = TmaParamModifier.LOOKAHEAD;
				break;
			case 127:  // template_param : '%' param_modifier param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 5].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // template_param : '%' param_modifier param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						((TmaParamModifier)tmStack[tmHead - 3].value) /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // template_param : '%' param_type identifier '=' param_value ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 4].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((ITmaParamValue)tmStack[tmHead - 1].value) /* paramValue */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // template_param : '%' param_type identifier ';'
				tmLeft.value = new TmaTemplateParam(
						null /* modifier */,
						((TmaParamType)tmStack[tmHead - 2].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 131:  // directive : '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // directive : '%' 'input' inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // directive : '%' 'interface' identifier_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInterface(
						((List<TmaIdentifier>)tmStack[tmHead - 1].value) /* ids */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // directive : '%' 'assert' 'empty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.EMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // directive : '%' 'assert' 'nonempty' rhsSet ';'
				tmLeft.value = new TmaDirectiveAssert(
						TmaDirectiveAssert.TmaKindKind.NONEMPTY /* kind */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // directive : '%' 'generate' identifier '=' rhsSet ';'
				tmLeft.value = new TmaDirectiveSet(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						((TmaRhsSet)tmStack[tmHead - 1].value) /* rhsSet */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 137:  // directive : '%' 'expect' icon ';'
				tmLeft.value = new TmaDirectiveExpect(
						((Integer)tmStack[tmHead - 1].value) /* icon */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // directive : '%' 'expect-rr' icon ';'
				tmLeft.value = new TmaDirectiveExpectRR(
						((Integer)tmStack[tmHead - 1].value) /* icon */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // identifier_list_Comma_separated : identifier_list_Comma_separated ',' identifier
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 140:  // identifier_list_Comma_separated : identifier
				tmLeft.value = new ArrayList();
				((List<TmaIdentifier>)tmLeft.value).add(((TmaIdentifier)tmStack[tmHead].value));
				break;
			case 141:  // inputref_list_Comma_separated : inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 142:  // inputref_list_Comma_separated : inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 143:  // inputref : symref_noargs 'no-eoi'
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // inputref : symref_noargs
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // references : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 146:  // references : references symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 147:  // references_cs : symref_noargs
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 148:  // references_cs : references_cs ',' symref_noargs
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 149:  // rule0_list_Or_separated : rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 150:  // rule0_list_Or_separated : rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 152:  // rule0 : predicate rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 3].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // rule0 : predicate rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // rule0 : predicate rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // rule0 : predicate rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((ITmaPredicateExpression)tmStack[tmHead - 1].value) /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 156:  // rule0 : rhsParts rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 2].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 157:  // rule0 : rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						((List<ITmaRhsPart>)tmStack[tmHead - 1].value) /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // rule0 : rhsSuffixopt reportClause
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead - 1].value) /* suffix */,
						((TmaReportClause)tmStack[tmHead].value) /* action */,
						null /* error */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // rule0 : rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* suffix */,
						null /* action */,
						null /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // rule0 : syntax_problem
				tmLeft.value = new TmaRule0(
						null /* predicate */,
						null /* list */,
						null /* suffix */,
						null /* action */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* error */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // predicate : '[' predicate_expression ']'
				tmLeft.value = ((ITmaPredicateExpression)tmStack[tmHead - 1].value);
				break;
			case 162:  // rhsSuffix : '%' 'prec' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.PREC /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // rhsSuffix : '%' 'shift' symref_noargs
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.SHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // reportClause : '->' identifier '/' identifier_list_Comma_separated reportAs
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* action */,
						((List<TmaIdentifier>)tmStack[tmHead - 1].value) /* flags */,
						((TmaReportAs)tmStack[tmHead].value) /* reportAs */,
						source, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // reportClause : '->' identifier '/' identifier_list_Comma_separated
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* action */,
						((List<TmaIdentifier>)tmStack[tmHead].value) /* flags */,
						null /* reportAs */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 166:  // reportClause : '->' identifier reportAs
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* action */,
						null /* flags */,
						((TmaReportAs)tmStack[tmHead].value) /* reportAs */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 167:  // reportClause : '->' identifier
				tmLeft.value = new TmaReportClause(
						((TmaIdentifier)tmStack[tmHead].value) /* action */,
						null /* flags */,
						null /* reportAs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 168:  // reportAs : 'as' identifier
				tmLeft.value = new TmaReportAs(
						((TmaIdentifier)tmStack[tmHead].value) /* identifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 169:  // rhsParts : rhsPart
				tmLeft.value = new ArrayList();
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 170:  // rhsParts : rhsParts rhsPart
				((List<ITmaRhsPart>)tmLeft.value).add(((ITmaRhsPart)tmStack[tmHead].value));
				break;
			case 171:  // rhsParts : rhsParts syntax_problem
				((List<ITmaRhsPart>)tmLeft.value).add(((TmaSyntaxProblem)tmStack[tmHead].value));
				break;
			case 176:  // lookahead_predicate_list_And_separated : lookahead_predicate_list_And_separated '&' lookahead_predicate
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 177:  // lookahead_predicate_list_And_separated : lookahead_predicate
				tmLeft.value = new ArrayList();
				((List<TmaLookaheadPredicate>)tmLeft.value).add(((TmaLookaheadPredicate)tmStack[tmHead].value));
				break;
			case 178:  // rhsLookahead : '(?=' lookahead_predicate_list_And_separated ')'
				tmLeft.value = new TmaRhsLookahead(
						((List<TmaLookaheadPredicate>)tmStack[tmHead - 1].value) /* predicates */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 179:  // lookahead_predicate : '!' symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						true /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 180:  // lookahead_predicate : symref_noargs
				tmLeft.value = new TmaLookaheadPredicate(
						false /* negate */,
						((TmaSymref)tmStack[tmHead].value) /* symrefNoargs */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 181:  // rhsStateMarker : '.' identifier
				tmLeft.value = new TmaRhsStateMarker(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 183:  // rhsAnnotated : annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 185:  // rhsAssignment : identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 186:  // rhsAssignment : identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 188:  // rhsOptional : rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUEST /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 190:  // rhsCast : rhsPrimary 'as' symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaSymref)tmStack[tmHead].value) /* target */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 191:  // rhsCast : rhsPrimary 'as' literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 192:  // rhsPrimary : symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 193:  // rhsPrimary : '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 194:  // rhsPrimary : '(' rhsParts 'separator' references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						true /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 195:  // rhsPrimary : '(' rhsParts 'separator' references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<ITmaRhsPart>)tmStack[tmHead - 4].value) /* ruleParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* separator */,
						false /* atLeastOne */,
						source, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 196:  // rhsPrimary : rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 197:  // rhsPrimary : rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 198:  // rhsPrimary : '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 200:  // rhsSet : 'set' '(' setExpression ')'
				tmLeft.value = new TmaRhsSet(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* expr */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 201:  // setPrimary : identifier symref
				tmLeft.value = new TmaSetSymbol(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 202:  // setPrimary : symref
				tmLeft.value = new TmaSetSymbol(
						null /* operator */,
						((TmaSymref)tmStack[tmHead].value) /* symbol */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 203:  // setPrimary : '(' setExpression ')'
				tmLeft.value = new TmaSetCompound(
						((ITmaSetExpression)tmStack[tmHead - 1].value) /* inner */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 204:  // setPrimary : '~' setPrimary
				tmLeft.value = new TmaSetComplement(
						((ITmaSetExpression)tmStack[tmHead].value) /* inner */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 206:  // setExpression : setExpression '|' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.OR /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 207:  // setExpression : setExpression '&' setExpression
				tmLeft.value = new TmaSetBinary(
						((ITmaSetExpression)tmStack[tmHead - 2].value) /* left */,
						TmaSetBinary.TmaKindKind.AND /* kind */,
						((ITmaSetExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 208:  // annotation_list : annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 209:  // annotation_list : annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 210:  // annotations : annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 211:  // annotation : '@' identifier '=' expression
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 212:  // annotation : '@' identifier
				tmLeft.value = new TmaAnnotation(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 213:  // annotation : '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 214:  // nonterm_param_list_Comma_separated : nonterm_param_list_Comma_separated ',' nonterm_param
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 215:  // nonterm_param_list_Comma_separated : nonterm_param
				tmLeft.value = new ArrayList();
				((List<ITmaNontermParam>)tmLeft.value).add(((ITmaNontermParam)tmStack[tmHead].value));
				break;
			case 216:  // nonterm_params : '<' nonterm_param_list_Comma_separated '>'
				tmLeft.value = new TmaNontermParams(
						((List<ITmaNontermParam>)tmStack[tmHead - 1].value) /* list */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 218:  // nonterm_param : identifier identifier '=' param_value
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* paramValue */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 219:  // nonterm_param : identifier identifier
				tmLeft.value = new TmaInlineParameter(
						((TmaIdentifier)tmStack[tmHead - 1].value) /* paramType */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* paramValue */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 220:  // param_ref : identifier
				tmLeft.value = new TmaParamRef(
						((TmaIdentifier)tmStack[tmHead].value) /* ref */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 221:  // argument_list_Comma_separated : argument_list_Comma_separated ',' argument
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 222:  // argument_list_Comma_separated : argument
				tmLeft.value = new ArrayList();
				((List<TmaArgument>)tmLeft.value).add(((TmaArgument)tmStack[tmHead].value));
				break;
			case 225:  // symref_args : '<' argument_list_Comma_separatedopt '>'
				tmLeft.value = new TmaSymrefArgs(
						((List<TmaArgument>)tmStack[tmHead - 1].value) /* argList */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 226:  // argument : param_ref ':' param_value
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead - 2].value) /* name */,
						((ITmaParamValue)tmStack[tmHead].value) /* val */,
						null /* bool */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 227:  // argument : '+' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.PLUS /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 228:  // argument : '~' param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						TmaArgument.TmaBoolKind.TILDE /* bool */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 229:  // argument : param_ref
				tmLeft.value = new TmaArgument(
						((TmaParamRef)tmStack[tmHead].value) /* name */,
						null /* val */,
						null /* bool */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 230:  // param_type : 'flag'
				tmLeft.value = TmaParamType.FLAG;
				break;
			case 231:  // param_type : 'param'
				tmLeft.value = TmaParamType.PARAM;
				break;
			case 234:  // predicate_primary : '!' param_ref
				tmLeft.value = new TmaBoolPredicate(
						true /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 235:  // predicate_primary : param_ref
				tmLeft.value = new TmaBoolPredicate(
						false /* negated */,
						((TmaParamRef)tmStack[tmHead].value) /* paramRef */,
						source, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 236:  // predicate_primary : param_ref '==' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.ASSIGN_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 237:  // predicate_primary : param_ref '!=' literal
				tmLeft.value = new TmaComparePredicate(
						((TmaParamRef)tmStack[tmHead - 2].value) /* paramRef */,
						TmaComparePredicate.TmaKindKind.EXCL_ASSIGN /* kind */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 239:  // predicate_expression : predicate_expression '&&' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.AND_AND /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 240:  // predicate_expression : predicate_expression '||' predicate_expression
				tmLeft.value = new TmaPredicateBinary(
						((ITmaPredicateExpression)tmStack[tmHead - 2].value) /* left */,
						TmaPredicateBinary.TmaKindKind.OR_OR /* kind */,
						((ITmaPredicateExpression)tmStack[tmHead].value) /* right */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 243:  // expression : '[' expression_list_Comma_separated ',' ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 2].value) /* content */,
						source, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 244:  // expression : '[' expression_list_Comma_separated ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 245:  // expression : '[' ',' ']'
				tmLeft.value = new TmaArray(
						null /* content */,
						source, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 246:  // expression : '[' ']'
				tmLeft.value = new TmaArray(
						null /* content */,
						source, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 248:  // expression_list_Comma_separated : expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 249:  // expression_list_Comma_separated : expression
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
		return (TmaInput1) parse(lexer, 0, 453);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 454);
	}
}
