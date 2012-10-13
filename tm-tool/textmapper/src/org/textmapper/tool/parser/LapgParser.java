/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
import org.textmapper.lapg.api.Lexem;
import org.textmapper.tool.parser.LapgLexer.ErrorReporter;
import org.textmapper.tool.parser.LapgLexer.Lexems;
import org.textmapper.tool.parser.LapgTree.TextSource;
import org.textmapper.tool.parser.LapgLexer.LapgSymbol;
import org.textmapper.tool.parser.ast.*;

public class LapgParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public LapgParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	private static final int[] lapg_action = LapgLexer.unpack_int(227,
		"\uffff\uffff\uffff\uffff\ufffd\uffff\223\0\uffff\uffff\uffff\uffff\4\0\ufff5\uffff" +
		"\uffef\uffff\35\0\41\0\42\0\40\0\7\0\11\0\172\0\173\0\uffcb\uffff\174\0\175\0\uffff" +
		"\uffff\176\0\205\0\uffff\uffff\10\0\uffa1\uffff\uffff\uffff\67\0\5\0\uff99\uffff" +
		"\uffff\uffff\45\0\uffff\uffff\uff75\uffff\uffff\uffff\uffff\uffff\uff6b\uffff\36" +
		"\0\uff63\uffff\74\0\77\0\100\0\uffff\uffff\uff41\uffff\162\0\37\0\3\0\206\0\uff25" +
		"\uffff\uffff\uffff\216\0\uffff\uffff\uff1f\uffff\34\0\43\0\6\0\uffff\uffff\uffff" +
		"\uffff\66\0\2\0\22\0\uffff\uffff\24\0\25\0\20\0\21\0\uff19\uffff\16\0\17\0\23\0\26" +
		"\0\30\0\27\0\uffff\uffff\15\0\ufee7\uffff\uffff\uffff\uffff\uffff\103\0\104\0\105" +
		"\0\uffff\uffff\ufec1\uffff\166\0\uffff\uffff\ufe9f\uffff\75\0\76\0\ufe99\uffff\163" +
		"\0\uffff\uffff\204\0\ufe93\uffff\uffff\uffff\71\0\73\0\70\0\12\0\ufe77\uffff\uffff" +
		"\uffff\13\0\14\0\ufe45\uffff\ufe19\uffff\uffff\uffff\110\0\115\0\uffff\uffff\uffff" +
		"\uffff\ufe11\uffff\uffff\uffff\uffff\uffff\207\0\uffff\uffff\ufde7\uffff\uffff\uffff" +
		"\217\0\33\0\uffff\uffff\50\0\ufde1\uffff\113\0\114\0\107\0\uffff\uffff\106\0\116" +
		"\0\165\0\ufdb7\uffff\uffff\uffff\ufd7d\uffff\uffff\uffff\222\0\147\0\uffff\uffff" +
		"\ufd53\uffff\120\0\ufd4b\uffff\ufd21\uffff\ufcf5\uffff\137\0\141\0\ufcc5\uffff\uffff" +
		"\uffff\uffff\uffff\ufc8f\uffff\ufc6f\uffff\123\0\140\0\130\0\127\0\ufc51\uffff\213" +
		"\0\214\0\212\0\uffff\uffff\uffff\uffff\201\0\60\0\52\0\ufc27\uffff\111\0\uffff\uffff" +
		"\133\0\uffff\uffff\221\0\uffff\uffff\ufbff\uffff\170\0\uffff\uffff\101\0\ufbd3\uffff" +
		"\ufba7\uffff\ufb6f\uffff\uffff\uffff\ufb43\uffff\126\0\ufb25\uffff\136\0\125\0\uffff" +
		"\uffff\154\0\155\0\153\0\ufaf5\uffff\ufabb\uffff\131\0\ufa85\uffff\uffff\uffff\210" +
		"\0\uffff\uffff\uffff\uffff\54\0\ufa67\uffff\ufa41\uffff\220\0\150\0\uffff\uffff\uffff" +
		"\uffff\167\0\121\0\124\0\ufa0b\uffff\uf9d3\uffff\uffff\uffff\132\0\102\0\uffff\uffff" +
		"\62\0\63\0\64\0\65\0\uffff\uffff\56\0\57\0\uffff\uffff\171\0\uf9a3\uffff\211\0\61" +
		"\0\uffff\uffff\152\0\151\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = LapgLexer.unpack_short(1680,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\44\uffff\ufffe\1\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41" +
		"\uffff\2\uffff\10\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\1\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff" +
		"\2\uffff\4\uffff\5\uffff\21\uffff\35\uffff\36\uffff\37\uffff\22\202\uffff\ufffe\14" +
		"\uffff\17\72\22\72\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\21\uffff" +
		"\34\uffff\0\0\uffff\ufffe\13\uffff\11\10\20\10\23\10\uffff\ufffe\23\uffff\11\44\20" +
		"\44\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45" +
		"\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\34\uffff\0\1\uffff\ufffe" +
		"\34\uffff\1\161\41\161\42\161\43\161\44\161\45\161\46\161\47\161\50\161\51\161\52" +
		"\161\53\161\uffff\ufffe\17\uffff\22\203\uffff\ufffe\16\uffff\23\215\uffff\ufffe\1" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43" +
		"\uffff\42\uffff\41\uffff\16\uffff\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27" +
		"\uffff\30\uffff\32\uffff\33\uffff\34\uffff\25\31\uffff\ufffe\3\uffff\0\46\1\46\2" +
		"\46\10\46\21\46\34\46\41\46\42\46\43\46\44\46\45\46\46\46\47\46\50\46\51\46\52\46" +
		"\53\46\uffff\ufffe\13\uffff\1\164\20\164\23\164\34\164\41\164\42\164\43\164\44\164" +
		"\45\164\46\164\47\164\50\164\51\164\52\164\53\164\uffff\ufffe\23\uffff\11\44\uffff" +
		"\ufffe\23\uffff\11\44\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\25\177\uffff\ufffe\1" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43" +
		"\uffff\42\uffff\41\uffff\16\uffff\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27" +
		"\uffff\30\uffff\32\uffff\33\uffff\34\uffff\25\32\uffff\ufffe\14\uffff\0\47\1\47\2" +
		"\47\5\47\10\47\21\47\23\47\34\47\41\47\42\47\43\47\44\47\45\47\46\47\47\47\50\47" +
		"\51\47\52\47\53\47\55\47\uffff\ufffe\47\uffff\15\112\17\112\uffff\ufffe\1\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42" +
		"\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\122\15\122" +
		"\uffff\ufffe\17\uffff\25\200\uffff\ufffe\5\uffff\0\51\1\51\2\51\10\51\21\51\23\51" +
		"\34\51\41\51\42\51\43\51\44\51\45\51\46\51\47\51\50\51\51\51\52\51\53\51\55\51\uffff" +
		"\ufffe\13\uffff\20\uffff\1\11\2\11\10\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11" +
		"\32\11\33\11\34\11\40\11\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52" +
		"\11\53\11\55\11\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\122\25\122\uffff\ufffe\12\uffff\15\117\25\117\uffff" +
		"\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\43\uffff\42\uffff\41\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12" +
		"\122\15\122\25\122\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\122\15\122\25\122\uffff\ufffe\33\uffff\1\134\2\134" +
		"\10\134\12\134\15\134\23\134\24\134\25\134\34\134\40\134\41\134\42\134\43\134\44" +
		"\134\45\134\46\134\47\134\50\134\51\134\52\134\53\134\55\134\uffff\ufffe\30\uffff" +
		"\31\uffff\32\uffff\1\145\2\145\10\145\12\145\15\145\23\145\24\145\25\145\33\145\34" +
		"\145\40\145\41\145\42\145\43\145\44\145\45\145\46\145\47\145\50\145\51\145\52\145" +
		"\53\145\55\145\uffff\ufffe\34\uffff\1\156\23\156\41\156\42\156\43\156\44\156\45\156" +
		"\46\156\47\156\50\156\51\156\52\156\53\156\20\161\uffff\ufffe\34\uffff\1\160\23\160" +
		"\41\160\42\160\43\160\44\160\45\160\46\160\47\160\50\160\51\160\52\160\53\160\uffff" +
		"\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55" +
		"\uffff\12\122\15\122\uffff\ufffe\23\uffff\0\53\1\53\2\53\10\53\21\53\34\53\41\53" +
		"\42\53\43\53\44\53\45\53\46\53\47\53\50\53\51\53\52\53\53\53\55\53\uffff\ufffe\1" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43" +
		"\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\40\uffff\55" +
		"\uffff\12\122\25\122\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\122\15\122\25\122\uffff\ufffe\13\uffff\1\11\2\11\10" +
		"\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42\11" +
		"\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe\1\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff" +
		"\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\122\15" +
		"\122\25\122\uffff\ufffe\34\uffff\1\156\23\156\41\156\42\156\43\156\44\156\45\156" +
		"\46\156\47\156\50\156\51\156\52\156\53\156\uffff\ufffe\33\uffff\1\135\2\135\10\135" +
		"\12\135\15\135\23\135\24\135\25\135\34\135\40\135\41\135\42\135\43\135\44\135\45" +
		"\135\46\135\47\135\50\135\51\135\52\135\53\135\55\135\uffff\ufffe\13\uffff\20\uffff" +
		"\1\11\2\11\10\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11" +
		"\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe" +
		"\30\uffff\31\uffff\32\uffff\1\143\2\143\10\143\12\143\15\143\23\143\24\143\25\143" +
		"\33\143\34\143\40\143\41\143\42\143\43\143\44\143\45\143\46\143\47\143\50\143\51" +
		"\143\52\143\53\143\55\143\uffff\ufffe\34\uffff\1\157\23\157\41\157\42\157\43\157" +
		"\44\157\45\157\46\157\47\157\50\157\51\157\52\157\53\157\uffff\ufffe\55\uffff\0\55" +
		"\1\55\2\55\10\55\21\55\34\55\41\55\42\55\43\55\44\55\45\55\46\55\47\55\50\55\51\55" +
		"\52\55\53\55\uffff\ufffe\30\uffff\31\uffff\32\uffff\1\144\2\144\10\144\12\144\15" +
		"\144\23\144\24\144\25\144\33\144\34\144\40\144\41\144\42\144\43\144\44\144\45\144" +
		"\46\144\47\144\50\144\51\144\52\144\53\144\55\144\uffff\ufffe\13\uffff\1\11\2\11" +
		"\10\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42" +
		"\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe\33\146" +
		"\1\146\2\146\10\146\12\146\15\146\23\146\24\146\25\146\34\146\40\146\41\146\42\146" +
		"\43\146\44\146\45\146\46\146\47\146\50\146\51\146\52\146\53\146\55\146\uffff\ufffe" +
		"\30\uffff\31\uffff\32\uffff\1\142\2\142\10\142\12\142\15\142\23\142\24\142\25\142" +
		"\33\142\34\142\40\142\41\142\42\142\43\142\44\142\45\142\46\142\47\142\50\142\51" +
		"\142\52\142\53\142\55\142\uffff\ufffe");

	private static final short[] lapg_sym_goto = LapgLexer.unpack_short(110,
		"\0\2\62\106\111\121\131\131\131\144\147\151\162\166\172\177\207\216\235\243\272\303" +
		"\313\317\323\334\341\351\360\u0105\u010c\u0113\u011a\u011b\u014c\u017d\u01ae\u01df" +
		"\u0210\u0241\u0272\u02a3\u02d4\u0305\u0336\u0336\u0340\u0341\u0342\u0344\u034c\u0369" +
		"\u036d\u036f\u0373\u0376\u0378\u037c\u0380\u0384\u0385\u0386\u0387\u038b\u038c\u038e" +
		"\u0390\u0392\u0395\u0398\u0399\u039c\u039d\u039f\u03a1\u03a4\u03a7\u03ab\u03af\u03b4" +
		"\u03bd\u03c6\u03cf\u03dc\u03e5\u03ec\u03f9\u040a\u0413\u0414\u041b\u041c\u041d\u041f" +
		"\u0420\u0421\u0429\u0433\u0447\u0449\u044a\u044e\u044f\u0450\u0451\u0452\u0453\u045b" +
		"\u045c\u045d");

	private static final short[] lapg_sym_from = LapgLexer.unpack_short(1117,
		"\337\340\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\132\134\135\142" +
		"\153\154\155\166\174\202\203\211\212\217\227\233\234\242\244\247\253\255\256\264" +
		"\305\306\314\317\327\0\1\5\10\21\27\35\43\46\132\154\155\202\212\227\233\247\253" +
		"\255\317\27\113\114\1\21\27\36\132\154\233\317\1\21\27\132\154\170\233\317\10\35" +
		"\46\155\202\211\212\227\247\253\255\124\156\157\207\251\2\41\122\161\200\254\270" +
		"\276\312\31\146\161\276\150\153\206\274\36\64\102\111\142\32\36\60\102\111\142\150" +
		"\162\40\124\161\200\220\270\276\0\1\5\10\21\27\35\36\102\111\132\142\154\233\317" +
		"\32\36\61\102\111\142\7\36\44\63\102\111\125\130\142\155\202\211\212\217\227\240" +
		"\242\247\253\255\256\264\314\155\202\211\212\227\247\253\255\264\75\111\143\163\246" +
		"\251\324\327\36\102\111\142\36\102\111\142\36\102\111\142\216\271\302\331\334\216" +
		"\271\302\331\334\36\102\111\142\216\271\302\331\36\102\111\142\213\261\313\10\35" +
		"\36\46\53\102\111\142\155\202\211\212\221\222\227\247\253\255\257\264\273\1\21\27" +
		"\132\154\233\317\1\21\27\132\154\233\317\1\21\27\132\154\233\317\247\0\1\4\5\10\21" +
		"\24\27\35\36\43\46\52\70\71\102\111\115\121\132\134\135\142\153\154\155\166\174\201" +
		"\202\203\211\212\217\227\233\234\242\244\247\253\255\256\264\305\306\314\317\327" +
		"\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\132\134\135\142\153\154" +
		"\155\166\174\201\202\203\211\212\217\227\233\234\242\244\247\253\255\256\264\305" +
		"\306\314\317\327\0\1\4\5\10\21\24\27\35\36\42\43\46\52\70\71\102\111\115\121\132" +
		"\134\135\142\153\154\155\166\174\202\203\211\212\217\227\233\234\242\244\247\253" +
		"\255\256\264\305\306\314\317\327\0\1\4\5\10\21\24\27\35\36\42\43\46\52\70\71\102" +
		"\111\115\121\132\134\135\142\153\154\155\166\174\202\203\211\212\217\227\233\234" +
		"\242\244\247\253\255\256\264\305\306\314\317\327\0\1\4\5\10\21\24\27\35\36\42\43" +
		"\46\52\70\71\102\111\115\121\132\134\135\142\153\154\155\166\174\202\203\211\212" +
		"\217\227\233\234\242\244\247\253\255\256\264\305\306\314\317\327\0\1\4\5\10\21\24" +
		"\27\35\36\42\43\46\52\70\71\102\111\115\121\132\134\135\142\153\154\155\166\174\202" +
		"\203\211\212\217\227\233\234\242\244\247\253\255\256\264\305\306\314\317\327\0\1" +
		"\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\132\134\135\142\147\153\154" +
		"\155\166\174\202\203\211\212\217\227\233\234\242\244\247\253\255\256\264\305\306" +
		"\314\317\327\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\132\134\135" +
		"\142\153\154\155\166\174\202\203\211\212\217\227\233\234\242\244\247\253\255\256" +
		"\264\277\305\306\314\317\327\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115" +
		"\121\132\134\135\142\153\154\155\166\174\202\203\211\212\217\227\233\234\242\244" +
		"\247\253\255\256\264\277\305\306\314\317\327\0\1\4\5\10\21\24\27\35\36\43\46\52\70" +
		"\71\102\111\115\121\132\134\135\142\153\154\155\166\174\202\203\211\212\217\227\233" +
		"\234\242\244\247\253\255\256\264\277\305\306\314\317\327\0\1\4\5\10\21\24\27\35\36" +
		"\43\46\52\70\71\102\111\115\121\132\134\135\142\153\154\155\166\174\202\203\211\212" +
		"\217\227\233\234\242\244\247\253\255\256\264\277\305\306\314\317\327\155\202\211" +
		"\212\227\247\253\255\264\301\0\0\0\5\0\4\5\10\35\46\52\71\1\21\27\115\121\132\153" +
		"\154\155\174\202\203\211\212\217\227\233\242\244\247\253\255\256\264\305\306\314" +
		"\317\327\7\44\125\130\36\102\36\102\111\142\27\113\114\0\5\0\5\10\35\0\5\10\35\0" +
		"\5\10\35\146\240\277\0\5\10\35\4\70\166\4\71\10\35\10\35\46\10\35\46\42\10\35\46" +
		"\115\115\174\121\305\155\202\227\155\202\227\155\202\227\253\155\202\227\253\155" +
		"\202\211\227\253\155\202\211\212\227\247\253\255\264\155\202\211\212\227\247\253" +
		"\255\264\155\202\211\212\227\247\253\255\264\155\202\211\212\217\227\242\247\253" +
		"\255\256\264\314\155\202\211\212\227\247\253\255\264\10\35\46\155\202\227\253\10" +
		"\35\46\155\202\211\212\222\227\247\253\255\264\10\35\46\53\155\202\211\212\221\222" +
		"\227\247\253\255\257\264\273\155\202\211\212\227\247\253\255\264\203\1\21\27\132" +
		"\154\233\317\21\134\161\276\24\24\155\202\211\212\227\247\253\255\155\202\211\212" +
		"\227\247\253\255\264\301\0\1\5\10\21\27\35\43\46\132\154\155\202\212\227\233\247" +
		"\253\255\317\10\35\102\7\44\125\130\146\170\240\301\147\155\202\211\212\227\247\253" +
		"\255\134\21");

	private static final short[] lapg_sym_to = LapgLexer.unpack_short(1117,
		"\341\342\2\16\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\16" +
		"\16\200\136\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16" +
		"\16\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\65\65\65\17\17\17\75\17\17\17\17\20\20" +
		"\20\20\20\237\20\20\42\42\42\201\201\201\201\201\201\201\201\155\155\227\253\306" +
		"\27\114\154\230\242\242\314\230\314\70\166\231\231\173\175\252\316\76\135\76\76\76" +
		"\71\77\132\77\77\77\174\234\113\113\232\243\272\315\232\4\21\4\4\21\21\4\100\100" +
		"\100\21\100\21\21\21\72\101\133\101\101\101\36\102\36\134\102\102\36\36\102\202\202" +
		"\202\202\202\202\277\202\202\202\202\202\202\202\203\203\203\203\203\203\203\203" +
		"\203\141\144\165\235\304\307\333\334\103\103\103\103\104\104\104\104\105\105\105" +
		"\105\265\265\265\265\335\266\266\266\266\336\106\106\106\106\267\267\267\267\107" +
		"\107\107\107\264\264\264\43\43\110\43\43\110\110\110\43\43\43\43\43\43\43\43\43\43" +
		"\43\43\43\22\22\22\22\22\22\22\23\23\23\23\23\23\23\24\24\24\24\24\24\24\305\2\16" +
		"\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200\136" +
		"\16\244\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200\136" +
		"\16\245\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\115\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200" +
		"\136\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\116\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200" +
		"\136\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\117\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200" +
		"\136\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\120\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200" +
		"\136\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\171\16\16\200" +
		"\136\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200\136" +
		"\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\320\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200\136" +
		"\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\321\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200\136" +
		"\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\322\16\16\16\16\16\2" +
		"\16\30\2\41\16\62\16\41\74\122\30\30\136\30\74\74\16\16\16\161\164\74\16\16\200\136" +
		"\16\200\16\254\254\270\200\16\276\16\16\254\200\254\312\254\323\16\16\16\16\16\204" +
		"\204\204\204\204\204\204\204\204\204\337\5\6\34\7\31\7\44\44\125\130\31\25\25\25" +
		"\147\152\25\176\25\205\147\205\250\205\205\205\205\25\205\303\205\205\205\205\205" +
		"\152\330\205\25\176\37\37\37\37\111\142\112\112\145\145\66\146\66\10\35\11\11\45" +
		"\45\12\12\12\12\13\13\13\13\167\300\324\14\14\14\14\32\137\236\33\140\46\46\47\47" +
		"\126\50\50\50\121\51\51\51\150\151\241\153\327\206\246\274\207\207\207\210\210\210" +
		"\310\211\211\211\211\212\247\255\212\212\213\213\213\261\213\261\213\261\313\214" +
		"\214\214\214\214\214\214\214\214\215\215\215\215\215\215\215\215\215\216\216\216" +
		"\216\271\216\302\216\216\216\271\216\331\217\217\256\256\217\256\217\256\256\52\52" +
		"\52\220\220\220\220\53\53\53\221\221\257\257\273\221\257\221\257\257\54\54\54\131" +
		"\54\54\54\54\131\54\54\54\54\54\131\54\131\222\222\222\222\222\222\222\222\222\251" +
		"\340\57\67\160\177\275\332\60\162\233\317\63\64\223\223\223\223\223\223\223\223\224" +
		"\224\224\224\224\224\224\224\224\325\15\26\15\55\26\26\55\123\127\26\26\225\225\262" +
		"\225\26\262\225\262\26\56\73\143\40\124\156\157\170\240\301\326\172\226\226\260\263" +
		"\226\263\226\311\163\61");

	private static final short[] lapg_rlen = LapgLexer.unpack_short(148,
		"\0\1\3\2\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\0\1\3\1\1\2\2\1\1\1\3\0\1\3\0" +
		"\1\0\1\0\1\0\1\10\2\3\1\1\1\1\3\1\3\1\1\3\1\2\2\1\1\5\6\1\1\1\4\4\1\3\0\1\2\1\2\1" +
		"\1\3\0\1\3\2\2\1\1\2\3\2\1\2\2\1\1\1\4\2\3\1\3\1\3\6\6\2\2\2\1\2\1\1\1\2\2\4\2\3" +
		"\1\3\1\1\1\1\1\0\1\5\0\1\3\1\1\3\3\5\1\1\1\1\1\3\3\2\1\1");

	private static final short[] lapg_rlex = LapgLexer.unpack_short(148,
		"\142\142\56\56\57\57\60\60\61\62\63\63\64\64\65\65\65\65\65\65\65\65\65\65\65\143" +
		"\143\65\66\67\67\67\70\70\70\71\144\144\72\145\145\146\146\147\147\150\150\72\73" +
		"\74\75\75\75\75\76\77\77\100\101\101\102\102\102\103\103\104\104\105\105\105\106" +
		"\106\107\107\151\151\110\111\111\112\113\113\152\152\114\114\114\114\114\115\115" +
		"\115\116\116\116\117\117\117\120\120\120\120\121\122\122\122\122\122\122\122\123" +
		"\123\123\124\125\125\126\126\126\127\130\130\131\131\131\131\131\153\153\131\154" +
		"\154\131\131\132\132\133\133\134\134\134\135\136\136\137\137\140\141");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"ID",
		"error",
		"regexp",
		"scon",
		"icon",
		"_skip",
		"_skip_comment",
		"'%'",
		"'::='",
		"'|'",
		"'='",
		"'=>'",
		"';'",
		"'.'",
		"','",
		"':'",
		"'['",
		"']'",
		"'('",
		"'(?!'",
		"')'",
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'?'",
		"'&'",
		"'@'",
		"Ltrue",
		"Lfalse",
		"Lnew",
		"Lseparator",
		"Lprio",
		"Lshift",
		"Linput",
		"Lleft",
		"Lright",
		"Lnonassoc",
		"Lnoeoi",
		"Lsoft",
		"Lclass",
		"Lspace",
		"Llayout",
		"Lreduce",
		"code",
		"input",
		"options",
		"option",
		"identifier",
		"symref",
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"named_pattern",
		"lexeme",
		"lexem_transition",
		"lexem_attrs",
		"lexem_attribute",
		"state_selector",
		"state_list",
		"stateref",
		"lexer_state",
		"grammar_parts",
		"grammar_part",
		"non_term",
		"priority_kw",
		"directive",
		"inputs",
		"inputref",
		"references",
		"rules",
		"rule_list",
		"rule0",
		"ruleprefix",
		"ruleparts",
		"rulepart",
		"refrulepart",
		"unorderedrulepart",
		"rulesymref",
		"ruleannotations",
		"annotations",
		"annotation_list",
		"annotation",
		"negative_la",
		"negative_la_clause",
		"expression",
		"expression_list",
		"map_entries",
		"map_separator",
		"name",
		"qualified_id",
		"rule_attrs",
		"command",
		"syntax_problem",
		"grammar_partsopt",
		"type_part_listopt",
		"typeopt",
		"lexem_transitionopt",
		"iconopt",
		"lexem_attrsopt",
		"commandopt",
		"Lnoeoiopt",
		"rule_attrsopt",
		"map_entriesopt",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 46;
		public static final int options = 47;
		public static final int option = 48;
		public static final int identifier = 49;
		public static final int symref = 50;
		public static final int type = 51;
		public static final int type_part_list = 52;
		public static final int type_part = 53;
		public static final int pattern = 54;
		public static final int lexer_parts = 55;
		public static final int lexer_part = 56;
		public static final int named_pattern = 57;
		public static final int lexeme = 58;
		public static final int lexem_transition = 59;
		public static final int lexem_attrs = 60;
		public static final int lexem_attribute = 61;
		public static final int state_selector = 62;
		public static final int state_list = 63;
		public static final int stateref = 64;
		public static final int lexer_state = 65;
		public static final int grammar_parts = 66;
		public static final int grammar_part = 67;
		public static final int non_term = 68;
		public static final int priority_kw = 69;
		public static final int directive = 70;
		public static final int inputs = 71;
		public static final int inputref = 72;
		public static final int references = 73;
		public static final int rules = 74;
		public static final int rule_list = 75;
		public static final int rule0 = 76;
		public static final int ruleprefix = 77;
		public static final int ruleparts = 78;
		public static final int rulepart = 79;
		public static final int refrulepart = 80;
		public static final int unorderedrulepart = 81;
		public static final int rulesymref = 82;
		public static final int ruleannotations = 83;
		public static final int annotations = 84;
		public static final int annotation_list = 85;
		public static final int annotation = 86;
		public static final int negative_la = 87;
		public static final int negative_la_clause = 88;
		public static final int expression = 89;
		public static final int expression_list = 90;
		public static final int map_entries = 91;
		public static final int map_separator = 92;
		public static final int name = 93;
		public static final int qualified_id = 94;
		public static final int rule_attrs = 95;
		public static final int command = 96;
		public static final int syntax_problem = 97;
		public static final int grammar_partsopt = 98;
		public static final int type_part_listopt = 99;
		public static final int typeopt = 100;
		public static final int lexem_transitionopt = 101;
		public static final int iconopt = 102;
		public static final int lexem_attrsopt = 103;
		public static final int commandopt = 104;
		public static final int Lnoeoiopt = 105;
		public static final int rule_attrsopt = 106;
		public static final int map_entriesopt = 107;
		public static final int expression_listopt = 108;
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final int lapg_state_sym(int state, int symbol) {
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

	protected int lapg_head;
	protected LapgSymbol[] lapg_m;
	protected LapgSymbol lapg_n;
	protected LapgLexer lapg_lexer;

	private Object parse(LapgLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = initialState;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != finalState) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
								MessageFormat.format("syntax error before line {0}", lapg_lexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
						lapg_n = lapg_lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (lapg_head < 0) {
					lapg_head = 0;
					lapg_m[0] = new LapgSymbol();
					lapg_m[0].state = initialState;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
	}

	protected boolean restore() {
		if (lapg_n.lexem == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 2) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].lexem = 2;
			lapg_m[lapg_head].sym = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 2);
			lapg_m[lapg_head].line = lapg_n.line;
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endoffset = lapg_n.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			cleanup(lapg_m[lapg_head]);
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 2:  // input ::= options lexer_parts grammar_partsopt
				  lapg_gg.sym = new AstRoot(((List<AstOptionPart>)lapg_m[lapg_head - 2].sym), ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset);
				break;
			case 3:  // input ::= lexer_parts grammar_partsopt
				  lapg_gg.sym = new AstRoot(null, ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 4:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOptionPart>(16); ((List<AstOptionPart>)lapg_gg.sym).add(((AstOptionPart)lapg_m[lapg_head].sym)); 
				break;
			case 5:  // options ::= options option
				 ((List<AstOptionPart>)lapg_m[lapg_head - 1].sym).add(((AstOptionPart)lapg_m[lapg_head].sym)); 
				break;
			case 6:  // option ::= ID '=' expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // identifier ::= ID
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // symref ::= ID
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head].sym), AstReference.DEFAULT, source, lapg_gg.offset, lapg_gg.endoffset);
				break;
			case 10:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 1].sym); 
				break;
			case 11:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head - 2].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 28:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset);
				break;
			case 29:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head].sym)); 
				break;
			case 30:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym).add(((AstLexerPart)lapg_m[lapg_head].sym)); 
				break;
			case 31:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 35:  // named_pattern ::= ID '=' pattern
				 lapg_gg.sym = new AstNamedPattern(((String)lapg_m[lapg_head - 2].sym), ((AstRegexp)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 38:  // lexeme ::= identifier typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym), null, null, null, null, null, source, lapg_gg.offset, lapg_gg.endoffset);
				break;
			case 47:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 7].sym), ((String)lapg_m[lapg_head - 6].sym), ((AstRegexp)lapg_m[lapg_head - 4].sym), ((AstReference)lapg_m[lapg_head - 3].sym), ((Integer)lapg_m[lapg_head - 2].sym), ((AstLexemAttrs)lapg_m[lapg_head - 1].sym), ((AstCode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // lexem_transition ::= '=>' stateref
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head].sym); 
				break;
			case 49:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.sym = ((AstLexemAttrs)lapg_m[lapg_head - 1].sym); 
				break;
			case 50:  // lexem_attribute ::= Lsoft
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 51:  // lexem_attribute ::= Lclass
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 52:  // lexem_attribute ::= Lspace
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 53:  // lexem_attribute ::= Llayout
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // state_selector ::= '[' state_list ']'
				 lapg_gg.sym = new AstStateSelector(((List<AstLexerState>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // state_list ::= lexer_state
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<AstLexerState>)lapg_gg.sym).add(((AstLexerState)lapg_m[lapg_head].sym)); 
				break;
			case 56:  // state_list ::= state_list ',' lexer_state
				 ((List<AstLexerState>)lapg_m[lapg_head - 2].sym).add(((AstLexerState)lapg_m[lapg_head].sym)); 
				break;
			case 57:  // stateref ::= ID
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head].sym), AstReference.STATE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // lexer_state ::= identifier
				 lapg_gg.sym = new AstLexerState(((AstIdentifier)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 59:  // lexer_state ::= identifier '=>' stateref
				 lapg_gg.sym = new AstLexerState(((AstIdentifier)lapg_m[lapg_head - 2].sym), ((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 60:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 61:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 62:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 65:  // non_term ::= identifier typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // non_term ::= annotations identifier typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), ((AstAnnotations)lapg_m[lapg_head - 5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head - 2].sym), ((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 73:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head - 2].sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 76:  // inputref ::= symref Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // references ::= symref
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 78:  // references ::= references symref
				 ((List<AstReference>)lapg_m[lapg_head - 1].sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 80:  // rule_list ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 81:  // rule_list ::= rule_list '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head - 2].sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 84:  // rule0 ::= ruleprefix ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 2].sym), ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rule0 ::= ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rule0 ::= ruleprefix rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 1].sym), null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // rule0 ::= rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 89:  // ruleprefix ::= annotations ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head - 1].sym), null); 
				break;
			case 90:  // ruleprefix ::= ruleannotations ID ':'
				 lapg_gg.sym = new AstRulePrefix(((AstRuleAnnotations)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 91:  // ruleprefix ::= ID ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 92:  // ruleparts ::= rulepart
				 lapg_gg.sym = new ArrayList<AstRulePart>(); ((List<AstRulePart>)lapg_gg.sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 93:  // ruleparts ::= ruleparts rulepart
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 94:  // ruleparts ::= ruleparts syntax_problem
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 98:  // refrulepart ::= ruleannotations ID '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // refrulepart ::= ruleannotations rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // refrulepart ::= ID '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // refrulepart ::= rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // unorderedrulepart ::= rulepart '&' rulepart
				 lapg_gg.sym = new AstUnorderedRulePart(((AstRulePart)lapg_m[lapg_head - 2].sym), ((AstRulePart)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // rulesymref ::= symref
				 lapg_gg.sym = new AstRuleDefaultSymbolRef(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // rulesymref ::= '(' rules ')'
				 lapg_gg.sym = new AstRuleNestedNonTerm(((List<AstRule>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // rulesymref ::= '(' ruleparts Lseparator references ')' '+'
				 lapg_gg.sym = new AstRuleNestedListWithSeparator(((List<AstRulePart>)lapg_m[lapg_head - 4].sym), ((List<AstReference>)lapg_m[lapg_head - 2].sym), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // rulesymref ::= '(' ruleparts Lseparator references ')' '*'
				 lapg_gg.sym = new AstRuleNestedListWithSeparator(((List<AstRulePart>)lapg_m[lapg_head - 4].sym), ((List<AstReference>)lapg_m[lapg_head - 2].sym), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 107:  // rulesymref ::= rulesymref '?'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // rulesymref ::= rulesymref '*'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // rulesymref ::= rulesymref '+'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // ruleannotations ::= annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // ruleannotations ::= negative_la annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head - 1].sym), ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // ruleannotations ::= negative_la
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 113:  // annotations ::= annotation_list
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // annotation_list ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 115:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 116:  // annotation ::= '@' ID
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // annotation ::= '@' ID '=' expression
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 119:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.sym = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // negative_la_clause ::= symref
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 121:  // negative_la_clause ::= negative_la_clause '|' symref
				 ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 122:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head - 3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 132:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 134:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 135:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 136:  // map_entries ::= ID map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 137:  // map_entries ::= map_entries ',' ID map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 2].offset, lapg_gg.endoffset)); 
				break;
			case 141:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 143:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 2].sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 144:  // rule_attrs ::= '%' Lprio symref
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 145:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 146:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 147:  // syntax_problem ::= error
				 lapg_gg.sym = new AstError(source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset); 
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol sym) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(LapgSymbol sym) {
	}

	public AstRoot parseInput(LapgLexer lexer) throws IOException, ParseException {
		return (AstRoot) parse(lexer, 0, 225);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 226);
	}
}
