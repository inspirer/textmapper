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
package org.textmapper.lapg.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ArrayList;
import org.textmapper.lapg.api.Lexem;
import org.textmapper.lapg.parser.LapgLexer.ErrorReporter;
import org.textmapper.lapg.parser.LapgLexer.Lexems;
import org.textmapper.lapg.parser.LapgTree.TextSource;
import org.textmapper.lapg.parser.ast.*;
import org.textmapper.lapg.parser.LapgLexer.LapgSymbol;

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
	private static final int[] lapg_action = LapgLexer.unpack_int(207,
		"\uffff\uffff\uffff\uffff\ufffd\uffff\205\0\uffff\uffff\uffff\uffff\4\0\ufff5\uffff" +
		"\uffef\uffff\35\0\7\0\uffcb\uffff\154\0\155\0\uffa1\uffff\156\0\157\0\160\0\uffff" +
		"\uffff\uff79\uffff\167\0\uffff\uffff\61\0\uffff\uffff\5\0\uff73\uffff\uffff\uffff" +
		"\43\0\uffff\uffff\uff4f\uffff\uffff\uffff\uffff\uffff\uff45\uffff\36\0\uff3d\uffff" +
		"\63\0\70\0\uffff\uffff\uff1b\uffff\144\0\37\0\3\0\170\0\ufeff\uffff\uffff\uffff\ufef9" +
		"\uffff\uffff\uffff\34\0\41\0\6\0\62\0\40\0\2\0\22\0\uffff\uffff\24\0\25\0\20\0\21" +
		"\0\ufedd\uffff\16\0\17\0\23\0\26\0\30\0\27\0\uffff\uffff\15\0\ufeab\uffff\uffff\uffff" +
		"\uffff\uffff\71\0\72\0\73\0\uffff\uffff\ufe85\uffff\150\0\uffff\uffff\10\0\ufe65" +
		"\uffff\64\0\65\0\ufe5f\uffff\145\0\uffff\uffff\166\0\uffff\uffff\ufe59\uffff\uffff" +
		"\uffff\201\0\12\0\ufe53\uffff\uffff\uffff\13\0\14\0\ufe21\uffff\11\0\ufdf7\uffff" +
		"\uffff\uffff\76\0\103\0\uffff\uffff\uffff\uffff\ufdef\uffff\uffff\uffff\uffff\uffff" +
		"\171\0\175\0\176\0\174\0\uffff\uffff\uffff\uffff\163\0\33\0\46\0\ufdc7\uffff\101" +
		"\0\102\0\75\0\uffff\uffff\74\0\104\0\uffff\uffff\ufd9f\uffff\uffff\uffff\uffff\uffff" +
		"\204\0\127\0\uffff\uffff\105\0\ufd6b\uffff\ufd45\uffff\ufd1d\uffff\uffff\uffff\uffff" +
		"\uffff\ufceb\uffff\ufccd\uffff\110\0\130\0\115\0\114\0\ufcb1\uffff\172\0\uffff\uffff" +
		"\uffff\uffff\50\0\ufc89\uffff\77\0\147\0\uffff\uffff\120\0\uffff\uffff\203\0\ufc63" +
		"\uffff\uffff\uffff\ufc2f\uffff\uffff\uffff\uffff\uffff\ufc09\uffff\ufbed\uffff\66" +
		"\0\ufbc5\uffff\113\0\ufb9d\uffff\123\0\112\0\134\0\135\0\133\0\uffff\uffff\ufb6b" +
		"\uffff\125\0\116\0\ufb37\uffff\uffff\uffff\uffff\uffff\55\0\56\0\57\0\60\0\uffff" +
		"\uffff\52\0\53\0\126\0\202\0\152\0\uffff\uffff\uffff\uffff\131\0\ufb1b\uffff\106" +
		"\0\111\0\ufae7\uffff\uffff\uffff\117\0\67\0\173\0\54\0\uffff\uffff\151\0\ufab5\uffff" +
		"\124\0\153\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = LapgLexer.unpack_short(1390,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\42\uffff\ufffe\1\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\37" +
		"\uffff\2\uffff\6\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\0\11\1\11\2\11\17\11\21" +
		"\11\22\11\24\11\37\11\40\11\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11" +
		"\16\200\23\200\uffff\ufffe\1\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\43\uffff\42\uffff\41\uffff\40\uffff\37\uffff\2\uffff\4\uffff\5\uffff\21\uffff" +
		"\35\uffff\36\uffff\22\164\uffff\ufffe\16\uffff\23\177\uffff\ufffe\1\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff" +
		"\37\uffff\2\uffff\6\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\13\uffff\11\10\20\10" +
		"\23\10\uffff\ufffe\23\uffff\11\42\20\42\uffff\ufffe\1\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\37\uffff\2" +
		"\uffff\6\uffff\34\uffff\0\1\uffff\ufffe\34\uffff\1\143\37\143\40\143\41\143\42\143" +
		"\43\143\44\143\45\143\46\143\47\143\50\143\51\143\uffff\ufffe\17\uffff\22\165\uffff" +
		"\ufffe\1\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42" +
		"\uffff\41\uffff\40\uffff\37\uffff\24\161\uffff\ufffe\1\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\37\uffff\16" +
		"\uffff\17\uffff\21\uffff\22\uffff\23\uffff\25\uffff\26\uffff\27\uffff\31\uffff\33" +
		"\uffff\34\uffff\24\31\uffff\ufffe\3\uffff\0\44\1\44\2\44\6\44\21\44\34\44\37\44\40" +
		"\44\41\44\42\44\43\44\44\44\45\44\46\44\47\44\50\44\51\44\uffff\ufffe\23\uffff\1" +
		"\146\20\146\34\146\37\146\40\146\41\146\42\146\43\146\44\146\45\146\46\146\47\146" +
		"\50\146\51\146\uffff\ufffe\23\uffff\11\42\uffff\ufffe\23\uffff\11\42\uffff\ufffe" +
		"\17\uffff\24\162\uffff\ufffe\1\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff" +
		"\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\37\uffff\16\uffff\17\uffff\21\uffff" +
		"\22\uffff\23\uffff\25\uffff\26\uffff\27\uffff\31\uffff\33\uffff\34\uffff\24\32\uffff" +
		"\ufffe\5\uffff\0\45\1\45\2\45\6\45\21\45\23\45\34\45\37\45\40\45\41\45\42\45\43\45" +
		"\44\45\45\45\46\45\47\45\50\45\51\45\53\45\uffff\ufffe\45\uffff\15\100\17\100\uffff" +
		"\ufffe\1\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42" +
		"\uffff\41\uffff\40\uffff\37\uffff\2\uffff\6\uffff\23\uffff\34\uffff\53\uffff\12\107" +
		"\15\107\uffff\ufffe\23\uffff\0\47\1\47\2\47\6\47\21\47\34\47\37\47\40\47\41\47\42" +
		"\47\43\47\44\47\45\47\46\47\47\47\50\47\51\47\53\47\uffff\ufffe\13\uffff\20\uffff" +
		"\1\11\2\11\6\11\12\11\15\11\23\11\27\11\30\11\31\11\33\11\34\11\37\11\40\11\41\11" +
		"\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\53\11\uffff\ufffe\1\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff" +
		"\37\uffff\6\uffff\23\uffff\34\uffff\53\uffff\12\107\15\107\uffff\ufffe\1\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40" +
		"\uffff\37\uffff\2\uffff\6\uffff\23\uffff\34\uffff\53\uffff\12\107\15\107\uffff\ufffe" +
		"\27\uffff\30\uffff\31\uffff\33\uffff\1\121\2\121\6\121\12\121\15\121\23\121\24\121" +
		"\34\121\37\121\40\121\41\121\42\121\43\121\44\121\45\121\46\121\47\121\50\121\51" +
		"\121\53\121\uffff\ufffe\34\uffff\1\140\37\140\40\140\41\140\42\140\43\140\44\140" +
		"\45\140\46\140\47\140\50\140\51\140\20\143\uffff\ufffe\34\uffff\1\142\37\142\40\142" +
		"\41\142\42\142\43\142\44\142\45\142\46\142\47\142\50\142\51\142\uffff\ufffe\1\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff" +
		"\40\uffff\37\uffff\2\uffff\6\uffff\23\uffff\34\uffff\53\uffff\12\107\15\107\uffff" +
		"\ufffe\53\uffff\0\51\1\51\2\51\6\51\21\51\34\51\37\51\40\51\41\51\42\51\43\51\44" +
		"\51\45\51\46\51\47\51\50\51\51\51\uffff\ufffe\13\uffff\1\11\2\11\6\11\12\11\15\11" +
		"\23\11\24\11\27\11\30\11\31\11\33\11\34\11\37\11\40\11\41\11\42\11\43\11\44\11\45" +
		"\11\46\11\47\11\50\11\51\11\53\11\uffff\ufffe\1\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\37\uffff\2\uffff" +
		"\23\uffff\34\uffff\53\uffff\12\136\24\136\uffff\ufffe\34\uffff\1\140\37\140\40\140" +
		"\41\140\42\140\43\140\44\140\45\140\46\140\47\140\50\140\51\140\uffff\ufffe\1\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff" +
		"\40\uffff\37\uffff\2\uffff\6\uffff\23\uffff\34\uffff\53\uffff\12\107\15\107\uffff" +
		"\ufffe\1\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42" +
		"\uffff\41\uffff\40\uffff\37\uffff\2\uffff\6\uffff\23\uffff\34\uffff\53\uffff\12\107" +
		"\15\107\uffff\ufffe\27\uffff\30\uffff\31\uffff\33\uffff\1\122\2\122\6\122\12\122" +
		"\15\122\23\122\24\122\34\122\37\122\40\122\41\122\42\122\43\122\44\122\45\122\46" +
		"\122\47\122\50\122\51\122\53\122\uffff\ufffe\13\uffff\20\uffff\1\11\2\11\6\11\12" +
		"\11\15\11\23\11\27\11\30\11\31\11\33\11\34\11\37\11\40\11\41\11\42\11\43\11\44\11" +
		"\45\11\46\11\47\11\50\11\51\11\53\11\uffff\ufffe\34\uffff\1\141\37\141\40\141\41" +
		"\141\42\141\43\141\44\141\45\141\46\141\47\141\50\141\51\141\uffff\ufffe\13\uffff" +
		"\1\11\2\11\6\11\12\11\15\11\23\11\24\11\27\11\30\11\31\11\33\11\34\11\37\11\40\11" +
		"\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\53\11\uffff\ufffe\27\uffff" +
		"\30\uffff\31\uffff\33\132\1\132\2\132\6\132\12\132\15\132\23\132\24\132\34\132\37" +
		"\132\40\132\41\132\42\132\43\132\44\132\45\132\46\132\47\132\50\132\51\132\53\132" +
		"\uffff\ufffe\1\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff" +
		"\42\uffff\41\uffff\40\uffff\37\uffff\2\uffff\23\uffff\34\uffff\53\uffff\12\137\24" +
		"\137\uffff\ufffe");

	private static final short[] lapg_sym_goto = LapgLexer.unpack_short(97,
		"\0\2\55\101\104\114\126\137\137\137\142\146\156\160\164\171\200\207\226\234\262\272" +
		"\276\302\311\314\323\324\333\362\371\u0100\u012c\u0158\u0184\u01b0\u01dc\u0208\u0234" +
		"\u0260\u028c\u02b8\u02e4\u02e4\u02f0\u02f1\u02f2\u02f4\u02fa\u0317\u031b\u031d\u0321" +
		"\u0324\u0326\u032a\u032b\u032c\u032d\u032f\u0332\u0333\u0336\u0337\u0339\u033a\u033c" +
		"\u033f\u0342\u0348\u0353\u0354\u035f\u0365\u0374\u0387\u0392\u0393\u039a\u039b\u039c" +
		"\u039e\u03a5\u03ac\u03b2\u03be\u03d2\u03d4\u03d5\u03d9\u03da\u03db\u03dc\u03dd\u03e3" +
		"\u03e4\u03e5");

	private static final short[] lapg_sym_from = LapgLexer.unpack_short(997,
		"\313\314\0\1\5\10\16\25\31\32\37\42\45\55\56\73\102\106\112\124\133\145\146\147\156" +
		"\157\167\175\202\203\205\215\225\227\232\233\235\237\241\251\257\273\301\306\310" +
		"\0\1\5\10\16\25\31\37\42\124\146\147\156\203\215\233\237\241\257\310\25\104\105\1" +
		"\16\25\32\124\146\156\257\1\4\16\25\27\124\137\146\156\257\10\31\42\147\202\203\215" +
		"\237\241\115\150\151\200\234\256\272\2\35\126\173\217\231\252\275\126\217\142\145" +
		"\200\256\23\32\73\102\133\32\53\73\102\127\133\142\34\115\126\173\206\217\252\0\1" +
		"\5\10\16\25\31\32\73\102\124\133\146\156\257\27\32\54\73\102\133\7\22\32\40\73\102" +
		"\113\117\122\133\147\163\175\202\203\215\233\237\241\251\273\310\66\102\130\134\172" +
		"\234\264\272\32\73\102\133\32\73\102\133\32\73\102\133\204\243\300\204\243\300\32" +
		"\73\102\133\204\243\300\175\32\73\102\133\204\243\300\10\31\32\42\46\73\102\133\147" +
		"\175\202\203\207\210\215\233\236\237\241\251\255\273\310\1\16\25\124\146\156\257" +
		"\1\16\25\124\146\156\257\0\1\5\10\16\25\31\32\37\42\45\55\56\73\102\106\112\124\133" +
		"\145\146\147\156\157\167\174\175\202\203\205\215\225\227\232\233\235\237\241\251" +
		"\257\273\301\306\310\0\1\5\10\16\25\31\32\37\42\45\55\56\73\102\106\112\124\133\145" +
		"\146\147\156\157\167\174\175\202\203\205\215\225\227\232\233\235\237\241\251\257" +
		"\273\301\306\310\0\1\5\10\16\25\31\32\36\37\42\45\55\56\73\102\106\112\124\133\145" +
		"\146\147\156\157\167\175\202\203\205\215\225\227\232\233\235\237\241\251\257\273" +
		"\301\306\310\0\1\5\10\16\25\31\32\36\37\42\45\55\56\73\102\106\112\124\133\145\146" +
		"\147\156\157\167\175\202\203\205\215\225\227\232\233\235\237\241\251\257\273\301" +
		"\306\310\0\1\5\10\16\25\31\32\36\37\42\45\55\56\73\102\106\112\124\133\145\146\147" +
		"\156\157\167\175\202\203\205\215\225\227\232\233\235\237\241\251\257\273\301\306" +
		"\310\0\1\5\10\16\25\31\32\36\37\42\45\55\56\73\102\106\112\124\133\145\146\147\156" +
		"\157\167\175\202\203\205\215\225\227\232\233\235\237\241\251\257\273\301\306\310" +
		"\0\1\5\10\16\25\31\32\37\42\45\55\56\73\102\106\112\124\133\141\145\146\147\156\157" +
		"\167\175\202\203\205\215\225\227\232\233\235\237\241\251\257\273\301\306\310\0\1" +
		"\5\10\16\25\31\32\37\42\45\55\56\73\102\106\112\124\133\145\146\147\156\157\167\175" +
		"\202\203\205\215\220\225\227\232\233\235\237\241\251\257\273\301\306\310\0\1\5\10" +
		"\16\25\31\32\37\42\45\55\56\73\102\106\112\124\133\145\146\147\156\157\167\175\202" +
		"\203\205\215\220\225\227\232\233\235\237\241\251\257\273\301\306\310\0\1\5\10\16" +
		"\25\31\32\37\42\45\55\56\73\102\106\112\124\133\145\146\147\156\157\167\175\202\203" +
		"\205\215\220\225\227\232\233\235\237\241\251\257\273\301\306\310\0\1\5\10\16\25\31" +
		"\32\37\42\45\55\56\73\102\106\112\124\133\145\146\147\156\157\167\175\202\203\205" +
		"\215\220\225\227\232\233\235\237\241\251\257\273\301\306\310\147\175\202\203\215" +
		"\222\233\237\241\251\273\310\0\0\0\5\0\5\10\31\42\45\1\16\25\106\112\124\145\146" +
		"\147\156\167\175\202\203\205\215\225\227\232\233\235\237\241\251\257\273\301\306" +
		"\310\7\40\117\122\32\73\32\73\102\133\25\104\105\0\5\0\5\10\31\163\220\4\10\31\10" +
		"\31\42\36\10\31\42\106\106\167\112\147\215\147\215\237\147\215\237\147\175\202\215" +
		"\237\273\147\175\202\203\215\233\237\241\251\273\310\175\147\175\202\203\215\233" +
		"\237\241\251\273\310\10\31\42\147\215\237\10\31\42\147\175\202\203\210\215\233\237" +
		"\241\251\273\310\10\31\42\46\147\175\202\203\207\210\215\233\236\237\241\251\255" +
		"\273\310\147\175\202\203\215\233\237\241\251\273\310\232\1\16\25\124\146\156\257" +
		"\16\55\126\217\1\16\25\124\146\156\257\1\16\25\124\146\156\257\147\202\203\215\237" +
		"\241\147\175\202\203\215\222\233\237\241\251\273\310\0\1\5\10\16\25\31\37\42\124" +
		"\146\147\156\203\215\233\237\241\257\310\10\31\73\7\40\117\122\137\163\222\141\147" +
		"\202\203\215\237\241\55\16");

	private static final short[] lapg_sym_to = LapgLexer.unpack_short(997,
		"\315\316\2\13\2\35\13\13\35\65\113\116\116\126\131\65\65\140\140\13\65\140\13\173" +
		"\13\217\140\231\231\231\252\173\140\140\140\231\275\173\231\231\13\231\140\140\231" +
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\57\57\57\14\14\14\66\14\14\14\14\15\26\15" +
		"\15\62\15\162\15\15\15\36\36\36\174\174\174\174\174\174\147\147\215\237\273\237\306" +
		"\25\105\153\225\153\225\301\301\154\154\166\170\240\303\56\67\67\67\67\70\124\70" +
		"\70\157\70\167\104\104\155\226\254\155\302\4\16\4\4\16\16\4\71\71\71\16\71\16\16" +
		"\16\63\72\125\72\72\72\32\55\73\32\73\73\146\32\32\73\175\220\175\175\175\175\175" +
		"\175\175\175\175\175\132\135\160\161\224\274\305\307\74\74\74\74\75\75\75\75\76\76" +
		"\76\76\246\246\246\247\247\247\77\77\77\77\250\250\250\232\100\100\100\100\251\251" +
		"\251\37\37\101\37\37\101\101\101\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\17" +
		"\17\17\17\17\17\17\20\20\20\20\20\20\20\2\13\2\35\13\13\35\65\113\116\116\126\131" +
		"\65\65\140\140\13\65\140\13\173\13\217\140\227\231\231\231\252\173\140\140\140\231" +
		"\275\173\231\231\13\231\140\140\231\2\13\2\35\13\13\35\65\113\116\116\126\131\65" +
		"\65\140\140\13\65\140\13\173\13\217\140\230\231\231\231\252\173\140\140\140\231\275" +
		"\173\231\231\13\231\140\140\231\2\13\2\35\13\13\35\65\106\113\116\116\126\131\65" +
		"\65\140\140\13\65\140\13\173\13\217\140\231\231\231\252\173\140\140\140\231\275\173" +
		"\231\231\13\231\140\140\231\2\13\2\35\13\13\35\65\107\113\116\116\126\131\65\65\140" +
		"\140\13\65\140\13\173\13\217\140\231\231\231\252\173\140\140\140\231\275\173\231" +
		"\231\13\231\140\140\231\2\13\2\35\13\13\35\65\110\113\116\116\126\131\65\65\140\140" +
		"\13\65\140\13\173\13\217\140\231\231\231\252\173\140\140\140\231\275\173\231\231" +
		"\13\231\140\140\231\2\13\2\35\13\13\35\65\111\113\116\116\126\131\65\65\140\140\13" +
		"\65\140\13\173\13\217\140\231\231\231\252\173\140\140\140\231\275\173\231\231\13" +
		"\231\140\140\231\2\13\2\35\13\13\35\65\113\116\116\126\131\65\65\140\140\13\65\164" +
		"\140\13\173\13\217\140\231\231\231\252\173\140\140\140\231\275\173\231\231\13\231" +
		"\140\140\231\2\13\2\35\13\13\35\65\113\116\116\126\131\65\65\140\140\13\65\140\13" +
		"\173\13\217\140\231\231\231\252\173\260\140\140\140\231\275\173\231\231\13\231\140" +
		"\140\231\2\13\2\35\13\13\35\65\113\116\116\126\131\65\65\140\140\13\65\140\13\173" +
		"\13\217\140\231\231\231\252\173\261\140\140\140\231\275\173\231\231\13\231\140\140" +
		"\231\2\13\2\35\13\13\35\65\113\116\116\126\131\65\65\140\140\13\65\140\13\173\13" +
		"\217\140\231\231\231\252\173\262\140\140\140\231\275\173\231\231\13\231\140\140\231" +
		"\2\13\2\35\13\13\35\65\113\116\116\126\131\65\65\140\140\13\65\140\13\173\13\217" +
		"\140\231\231\231\252\173\263\140\140\140\231\275\173\231\231\13\231\140\140\231\176" +
		"\176\176\176\176\176\176\176\176\176\176\176\313\5\6\30\7\7\40\40\117\122\21\21\21" +
		"\141\144\21\171\21\177\21\141\177\177\177\253\177\267\270\271\177\253\177\177\177" +
		"\21\177\311\312\177\33\33\33\33\102\133\103\103\136\136\60\137\60\10\31\11\11\41" +
		"\41\221\264\27\42\42\43\43\120\112\44\44\44\142\143\223\145\200\256\201\201\276\202" +
		"\202\202\203\233\241\203\203\310\204\204\204\243\204\243\204\243\300\204\243\234" +
		"\205\235\235\235\205\235\205\235\235\235\235\45\45\45\206\206\206\46\46\46\207\236" +
		"\236\236\255\207\236\207\236\236\236\236\47\47\47\123\47\47\47\47\123\47\47\47\123" +
		"\47\47\47\123\47\47\210\210\210\210\210\210\210\210\210\210\210\272\314\52\61\152" +
		"\172\216\304\53\127\156\257\22\22\22\22\22\22\22\23\23\23\23\23\23\23\211\211\211" +
		"\211\211\211\212\212\212\212\212\265\212\212\212\212\212\212\12\24\12\50\24\24\50" +
		"\114\121\24\24\213\24\244\213\244\213\244\24\244\51\64\134\34\115\150\151\163\222" +
		"\266\165\214\242\245\214\214\277\130\54");

	private static final short[] lapg_rlen = LapgLexer.unpack_short(134,
		"\0\1\3\2\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\0\1\3\1\1\2\2\3\3\0\1\3\0\1\0" +
		"\1\0\1\7\3\1\1\1\1\1\2\1\2\2\5\6\1\1\1\1\4\4\1\3\0\1\2\1\2\1\3\0\1\3\2\2\1\1\2\3" +
		"\2\1\2\2\4\2\3\1\1\3\3\2\2\2\1\3\1\2\1\1\1\2\2\5\2\4\1\3\1\1\1\1\1\0\1\4\0\1\3\1" +
		"\1\3\3\5\1\1\1\1\1\3\3\2\1\1");

	private static final short[] lapg_rlex = LapgLexer.unpack_short(134,
		"\126\126\54\54\55\55\56\56\57\60\61\61\62\62\63\63\63\63\63\63\63\63\63\63\63\127" +
		"\127\63\64\65\65\65\66\66\130\130\66\131\131\132\132\133\133\66\67\70\70\70\70\71" +
		"\71\72\72\72\73\73\73\74\74\74\75\75\76\76\134\134\77\100\100\101\101\135\135\102" +
		"\102\102\102\102\103\103\103\104\104\104\105\105\105\105\105\105\105\105\105\105" +
		"\106\106\107\107\107\110\111\111\112\112\112\113\114\114\115\115\115\115\115\136" +
		"\136\115\137\137\115\115\116\116\117\117\120\120\120\121\122\122\123\123\124\125");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"error",
		"regexp",
		"scon",
		"icon",
		"'%'",
		"_skip",
		"_skip_comment",
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
		"')'",
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'?'",
		"'?!'",
		"'&'",
		"'@'",
		"Ltrue",
		"Lfalse",
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
		"symbol",
		"reference",
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"lexem_attrs",
		"lexem_attribute",
		"icon_list",
		"grammar_parts",
		"grammar_part",
		"priority_kw",
		"directive",
		"inputs",
		"inputref",
		"references",
		"rules",
		"rule0",
		"ruleprefix",
		"ruleparts",
		"rulepart",
		"ruleparts_choice",
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
		public static final int input = 44;
		public static final int options = 45;
		public static final int option = 46;
		public static final int symbol = 47;
		public static final int reference = 48;
		public static final int type = 49;
		public static final int type_part_list = 50;
		public static final int type_part = 51;
		public static final int pattern = 52;
		public static final int lexer_parts = 53;
		public static final int lexer_part = 54;
		public static final int lexem_attrs = 55;
		public static final int lexem_attribute = 56;
		public static final int icon_list = 57;
		public static final int grammar_parts = 58;
		public static final int grammar_part = 59;
		public static final int priority_kw = 60;
		public static final int directive = 61;
		public static final int inputs = 62;
		public static final int inputref = 63;
		public static final int references = 64;
		public static final int rules = 65;
		public static final int rule0 = 66;
		public static final int ruleprefix = 67;
		public static final int ruleparts = 68;
		public static final int rulepart = 69;
		public static final int ruleparts_choice = 70;
		public static final int ruleannotations = 71;
		public static final int annotations = 72;
		public static final int annotation_list = 73;
		public static final int annotation = 74;
		public static final int negative_la = 75;
		public static final int negative_la_clause = 76;
		public static final int expression = 77;
		public static final int expression_list = 78;
		public static final int map_entries = 79;
		public static final int map_separator = 80;
		public static final int name = 81;
		public static final int qualified_id = 82;
		public static final int rule_attrs = 83;
		public static final int command = 84;
		public static final int syntax_problem = 85;
		public static final int grammar_partsopt = 86;
		public static final int type_part_listopt = 87;
		public static final int typeopt = 88;
		public static final int iconopt = 89;
		public static final int lexem_attrsopt = 90;
		public static final int commandopt = 91;
		public static final int Lnoeoiopt = 92;
		public static final int rule_attrsopt = 93;
		public static final int map_entriesopt = 94;
		public static final int expression_listopt = 95;
	}

	public interface Rules {
		public static final int lexer_part_group_selector = 32;  // lexer_part ::= '[' icon_list ']'
		public static final int lexer_part_alias = 33;  // lexer_part ::= identifier '=' pattern
		public static final int grammar_part_directive = 56;  // grammar_part ::= directive
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
			case 6:  // option ::= identifier '=' expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
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
			case 32:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // lexer_part ::= identifier '=' pattern
				 lapg_gg.sym = new AstNamedPattern(((String)lapg_m[lapg_head - 2].sym), ((AstRegexp)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym), null, null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 43:  // lexer_part ::= symbol typeopt ':' pattern iconopt lexem_attrsopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 6].sym), ((String)lapg_m[lapg_head - 5].sym), ((AstRegexp)lapg_m[lapg_head - 3].sym), ((Integer)lapg_m[lapg_head - 2].sym), ((AstLexemAttrs)lapg_m[lapg_head - 1].sym), ((AstCode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.sym = ((AstLexemAttrs)lapg_m[lapg_head - 1].sym); 
				break;
			case 45:  // lexem_attribute ::= Lsoft
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // lexem_attribute ::= Lclass
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // lexem_attribute ::= Lspace
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // lexem_attribute ::= Llayout
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 49:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head].sym)); 
				break;
			case 50:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head - 1].sym).add(((Integer)lapg_m[lapg_head].sym)); 
				break;
			case 51:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 52:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 53:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 54:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), ((AstAnnotations)lapg_m[lapg_head - 5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 56:  // grammar_part ::= directive
				 lapg_gg.sym = lapg_m[lapg_head].sym; 
				break;
			case 60:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head - 2].sym), ((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 62:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 63:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head - 2].sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 66:  // inputref ::= reference Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 68:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head - 1].sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 69:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 70:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head - 2].sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 73:  // rule0 ::= ruleprefix ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 2].sym), ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // rule0 ::= ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // rule0 ::= ruleprefix rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 1].sym), null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // rule0 ::= rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 78:  // ruleprefix ::= annotations ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head - 1].sym), null); 
				break;
			case 79:  // ruleprefix ::= ruleannotations identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstRuleAnnotations)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 80:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 81:  // ruleparts ::= rulepart
				 lapg_gg.sym = new ArrayList<AstRulePart>(); ((List<AstRulePart>)lapg_gg.sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 82:  // ruleparts ::= ruleparts rulepart
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 83:  // ruleparts ::= ruleparts syntax_problem
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 84:  // rulepart ::= ruleannotations identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((String)lapg_m[lapg_head - 2].sym), ((AstReference)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rulepart ::= ruleannotations reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((AstReference)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rulepart ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((String)lapg_m[lapg_head - 2].sym), ((AstReference)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // rulepart ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((AstReference)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // rulepart ::= '(' ruleparts_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 90:  // rulepart ::= rulepart '&' rulepart
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 91:  // rulepart ::= rulepart '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 92:  // rulepart ::= rulepart '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 93:  // rulepart ::= rulepart '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 96:  // ruleannotations ::= annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // ruleannotations ::= negative_la annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head - 1].sym), ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // ruleannotations ::= negative_la
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // annotations ::= annotation_list
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // annotation_list ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 101:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 102:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head - 3].sym), ((AstExpression)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 105:  // negative_la ::= '(' '?!' negative_la_clause ')'
				 lapg_gg.sym = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // negative_la_clause ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 107:  // negative_la_clause ::= negative_la_clause '|' reference
				 ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 108:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head - 3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 121:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 122:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 123:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 127:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 2].sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 130:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 132:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 133:  // syntax_problem ::= error
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
		return (AstRoot) parse(lexer, 0, 205);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 206);
	}
}
