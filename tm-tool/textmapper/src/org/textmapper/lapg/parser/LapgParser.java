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
	private static final int[] lapg_action = LapgLexer.unpack_int(205,
		"\uffff\uffff\uffff\uffff\ufffd\uffff\204\0\uffff\uffff\uffff\uffff\4\0\ufff5\uffff" +
		"\uffef\uffff\35\0\7\0\11\0\153\0\154\0\uffcb\uffff\155\0\156\0\uffff\uffff\157\0" +
		"\166\0\uffff\uffff\61\0\uffff\uffff\5\0\uffa1\uffff\uffff\uffff\43\0\uffff\uffff" +
		"\uff7d\uffff\uffff\uffff\uffff\uffff\uff73\uffff\36\0\uff6b\uffff\63\0\70\0\uffff" +
		"\uffff\uff49\uffff\143\0\37\0\3\0\167\0\uff2d\uffff\uffff\uffff\177\0\uffff\uffff" +
		"\uff27\uffff\34\0\41\0\6\0\62\0\40\0\2\0\22\0\uffff\uffff\24\0\25\0\20\0\21\0\uff21" +
		"\uffff\16\0\17\0\23\0\26\0\30\0\27\0\uffff\uffff\15\0\ufeef\uffff\uffff\uffff\uffff" +
		"\uffff\71\0\72\0\73\0\uffff\uffff\ufec9\uffff\147\0\uffff\uffff\10\0\ufea7\uffff" +
		"\64\0\65\0\ufea1\uffff\144\0\uffff\uffff\165\0\ufe9b\uffff\uffff\uffff\12\0\ufe7f" +
		"\uffff\uffff\uffff\13\0\14\0\ufe4d\uffff\ufe23\uffff\uffff\uffff\76\0\103\0\uffff" +
		"\uffff\uffff\uffff\ufe1b\uffff\uffff\uffff\uffff\uffff\170\0\uffff\uffff\ufdf1\uffff" +
		"\uffff\uffff\200\0\33\0\46\0\ufdeb\uffff\101\0\102\0\75\0\uffff\uffff\74\0\104\0" +
		"\146\0\ufdc3\uffff\uffff\uffff\ufd8b\uffff\uffff\uffff\203\0\132\0\uffff\uffff\105" +
		"\0\ufd61\uffff\ufd37\uffff\ufd0b\uffff\ufcdd\uffff\uffff\uffff\uffff\uffff\ufca9" +
		"\uffff\ufc89\uffff\110\0\130\0\115\0\114\0\ufc6b\uffff\174\0\175\0\173\0\uffff\uffff" +
		"\uffff\uffff\162\0\uffff\uffff\50\0\ufc41\uffff\77\0\uffff\uffff\120\0\uffff\uffff" +
		"\202\0\uffff\uffff\151\0\uffff\uffff\ufc1b\uffff\66\0\ufbef\uffff\ufbb9\uffff\uffff" +
		"\uffff\ufb8d\uffff\113\0\ufb6f\uffff\123\0\112\0\uffff\uffff\135\0\136\0\134\0\ufb41" +
		"\uffff\ufb09\uffff\116\0\ufad5\uffff\uffff\uffff\171\0\uffff\uffff\55\0\56\0\57\0" +
		"\60\0\uffff\uffff\52\0\53\0\ufab7\uffff\201\0\133\0\uffff\uffff\150\0\106\0\111\0" +
		"\ufa83\uffff\ufa4d\uffff\uffff\uffff\117\0\67\0\uffff\uffff\54\0\152\0\ufa1f\uffff" +
		"\172\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = LapgLexer.unpack_short(1554,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\42\uffff\ufffe\1\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40" +
		"\uffff\2\uffff\6\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\1\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff" +
		"\2\uffff\4\uffff\5\uffff\21\uffff\35\uffff\36\uffff\37\uffff\22\163\uffff\ufffe\1" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42" +
		"\uffff\41\uffff\40\uffff\2\uffff\6\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\13\uffff" +
		"\11\10\20\10\23\10\uffff\ufffe\23\uffff\11\42\20\42\uffff\ufffe\1\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff" +
		"\40\uffff\2\uffff\6\uffff\34\uffff\0\1\uffff\ufffe\34\uffff\1\142\40\142\41\142\42" +
		"\142\43\142\44\142\45\142\46\142\47\142\50\142\51\142\52\142\uffff\ufffe\17\uffff" +
		"\22\164\uffff\ufffe\16\uffff\23\176\uffff\ufffe\1\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\16\uffff" +
		"\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27\uffff\30\uffff\32\uffff\33\uffff" +
		"\34\uffff\25\31\uffff\ufffe\3\uffff\0\44\1\44\2\44\6\44\21\44\34\44\40\44\41\44\42" +
		"\44\43\44\44\44\45\44\46\44\47\44\50\44\51\44\52\44\uffff\ufffe\13\uffff\1\145\20" +
		"\145\23\145\34\145\40\145\41\145\42\145\43\145\44\145\45\145\46\145\47\145\50\145" +
		"\51\145\52\145\uffff\ufffe\23\uffff\11\42\uffff\ufffe\23\uffff\11\42\uffff\ufffe" +
		"\1\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff" +
		"\42\uffff\41\uffff\40\uffff\25\160\uffff\ufffe\1\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\16\uffff" +
		"\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27\uffff\30\uffff\32\uffff\33\uffff" +
		"\34\uffff\25\32\uffff\ufffe\5\uffff\0\45\1\45\2\45\6\45\21\45\23\45\34\45\40\45\41" +
		"\45\42\45\43\45\44\45\45\45\46\45\47\45\50\45\51\45\52\45\54\45\uffff\ufffe\46\uffff" +
		"\15\100\17\100\uffff\ufffe\1\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45" +
		"\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\2\uffff\6\uffff\23\uffff\24\uffff" +
		"\34\uffff\54\uffff\12\107\15\107\uffff\ufffe\17\uffff\25\161\uffff\ufffe\23\uffff" +
		"\0\47\1\47\2\47\6\47\21\47\34\47\40\47\41\47\42\47\43\47\44\47\45\47\46\47\47\47" +
		"\50\47\51\47\52\47\54\47\uffff\ufffe\13\uffff\20\uffff\1\11\2\11\6\11\12\11\15\11" +
		"\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42\11\43\11\44\11\45" +
		"\11\46\11\47\11\50\11\51\11\52\11\54\11\uffff\ufffe\1\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\2" +
		"\uffff\6\uffff\23\uffff\24\uffff\34\uffff\54\uffff\12\107\25\107\uffff\ufffe\1\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff" +
		"\41\uffff\40\uffff\6\uffff\23\uffff\24\uffff\34\uffff\54\uffff\12\107\15\107\25\107" +
		"\uffff\ufffe\1\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff" +
		"\43\uffff\42\uffff\41\uffff\40\uffff\2\uffff\6\uffff\23\uffff\24\uffff\34\uffff\54" +
		"\uffff\12\107\15\107\25\107\uffff\ufffe\33\uffff\1\121\2\121\6\121\12\121\15\121" +
		"\23\121\24\121\25\121\34\121\40\121\41\121\42\121\43\121\44\121\45\121\46\121\47" +
		"\121\50\121\51\121\52\121\54\121\uffff\ufffe\30\uffff\31\uffff\32\uffff\1\127\2\127" +
		"\6\127\12\127\15\127\23\127\24\127\25\127\33\127\34\127\40\127\41\127\42\127\43\127" +
		"\44\127\45\127\46\127\47\127\50\127\51\127\52\127\54\127\uffff\ufffe\34\uffff\1\137" +
		"\23\137\40\137\41\137\42\137\43\137\44\137\45\137\46\137\47\137\50\137\51\137\52" +
		"\137\20\142\uffff\ufffe\34\uffff\1\141\23\141\40\141\41\141\42\141\43\141\44\141" +
		"\45\141\46\141\47\141\50\141\51\141\52\141\uffff\ufffe\1\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff" +
		"\2\uffff\6\uffff\23\uffff\24\uffff\34\uffff\54\uffff\12\107\15\107\uffff\ufffe\54" +
		"\uffff\0\51\1\51\2\51\6\51\21\51\34\51\40\51\41\51\42\51\43\51\44\51\45\51\46\51" +
		"\47\51\50\51\51\51\52\51\uffff\ufffe\1\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\40\uffff\2\uffff\6\uffff\23" +
		"\uffff\24\uffff\34\uffff\54\uffff\12\107\15\107\25\107\uffff\ufffe\13\uffff\1\11" +
		"\2\11\6\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11" +
		"\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\54\11\uffff\ufffe\1\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff" +
		"\41\uffff\40\uffff\2\uffff\6\uffff\23\uffff\24\uffff\34\uffff\54\uffff\12\107\15" +
		"\107\25\107\uffff\ufffe\34\uffff\1\137\23\137\40\137\41\137\42\137\43\137\44\137" +
		"\45\137\46\137\47\137\50\137\51\137\52\137\uffff\ufffe\33\uffff\1\122\2\122\6\122" +
		"\12\122\15\122\23\122\24\122\25\122\34\122\40\122\41\122\42\122\43\122\44\122\45" +
		"\122\46\122\47\122\50\122\51\122\52\122\54\122\uffff\ufffe\13\uffff\20\uffff\1\11" +
		"\2\11\6\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11" +
		"\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\54\11\uffff\ufffe\30\uffff" +
		"\31\uffff\32\uffff\1\125\2\125\6\125\12\125\15\125\23\125\24\125\25\125\33\125\34" +
		"\125\40\125\41\125\42\125\43\125\44\125\45\125\46\125\47\125\50\125\51\125\52\125" +
		"\54\125\uffff\ufffe\34\uffff\1\140\23\140\40\140\41\140\42\140\43\140\44\140\45\140" +
		"\46\140\47\140\50\140\51\140\52\140\uffff\ufffe\30\uffff\31\uffff\32\uffff\1\126" +
		"\2\126\6\126\12\126\15\126\23\126\24\126\25\126\33\126\34\126\40\126\41\126\42\126" +
		"\43\126\44\126\45\126\46\126\47\126\50\126\51\126\52\126\54\126\uffff\ufffe\13\uffff" +
		"\1\11\2\11\6\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11" +
		"\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\54\11\uffff\ufffe\33" +
		"\131\1\131\2\131\6\131\12\131\15\131\23\131\24\131\25\131\34\131\40\131\41\131\42" +
		"\131\43\131\44\131\45\131\46\131\47\131\50\131\51\131\52\131\54\131\uffff\ufffe\30" +
		"\uffff\31\uffff\32\uffff\1\124\2\124\6\124\12\124\15\124\23\124\24\124\25\124\33" +
		"\124\34\124\40\124\41\124\42\124\43\124\44\124\45\124\46\124\47\124\50\124\51\124" +
		"\52\124\54\124\uffff\ufffe");

	private static final short[] lapg_sym_goto = LapgLexer.unpack_short(98,
		"\0\2\53\76\101\111\123\135\135\135\140\144\155\157\163\170\177\206\225\233\261\271" +
		"\300\304\310\320\324\334\343\367\376\u0105\u010c\u0136\u0160\u018a\u01b4\u01de\u0208" +
		"\u0232\u025c\u0286\u02b0\u02da\u02da\u02e3\u02e4\u02e5\u02e7\u02ed\u0307\u030b\u030d" +
		"\u0311\u0314\u0316\u031a\u031b\u031c\u031d\u031f\u0322\u0323\u0326\u0327\u0329\u032a" +
		"\u032d\u0331\u0335\u033a\u0342\u034e\u0356\u035d\u0369\u0379\u0381\u0382\u0389\u038a" +
		"\u038b\u038d\u038e\u038f\u0396\u039f\u03b2\u03b4\u03b5\u03b9\u03ba\u03bb\u03bc\u03bd" +
		"\u03c4\u03c5\u03c6");

	private static final short[] lapg_sym_from = LapgLexer.unpack_short(966,
		"\311\312\0\1\5\10\16\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\142\143" +
		"\144\162\170\171\176\177\202\212\216\217\225\227\234\237\240\246\273\301\304\0\1" +
		"\5\10\16\24\30\36\41\124\143\144\170\177\212\216\234\237\304\24\104\105\1\16\24\31" +
		"\124\143\216\304\1\4\16\24\26\124\135\143\216\304\10\30\41\144\170\176\177\212\234" +
		"\237\115\145\146\174\231\233\256\2\34\113\150\166\236\252\260\277\150\260\137\142" +
		"\174\256\31\56\73\102\131\31\52\73\102\131\137\151\33\115\150\166\203\252\260\0\1" +
		"\5\10\16\24\30\31\73\102\124\131\143\216\304\26\31\53\73\102\131\7\31\37\55\73\102" +
		"\117\122\131\144\156\170\176\177\202\212\225\234\237\240\246\301\144\170\176\177" +
		"\212\234\237\246\66\102\132\152\231\233\265\31\73\102\131\31\73\102\131\31\73\102" +
		"\131\201\253\270\307\201\253\270\307\31\73\102\131\201\253\270\307\31\73\102\131" +
		"\200\243\300\10\30\31\41\45\73\102\131\144\170\176\177\204\205\212\234\237\241\246" +
		"\255\1\16\24\124\143\216\304\1\16\24\124\143\216\304\1\16\24\124\143\216\304\0\1" +
		"\5\10\16\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\142\143\144\162\167" +
		"\170\171\176\177\202\212\216\217\225\227\234\237\240\246\273\301\304\0\1\5\10\16" +
		"\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\142\143\144\162\167\170\171" +
		"\176\177\202\212\216\217\225\227\234\237\240\246\273\301\304\0\1\5\10\16\21\24\30" +
		"\31\35\36\41\44\73\102\106\112\124\126\127\131\142\143\144\162\170\171\176\177\202" +
		"\212\216\217\225\227\234\237\240\246\273\301\304\0\1\5\10\16\21\24\30\31\35\36\41" +
		"\44\73\102\106\112\124\126\127\131\142\143\144\162\170\171\176\177\202\212\216\217" +
		"\225\227\234\237\240\246\273\301\304\0\1\5\10\16\21\24\30\31\35\36\41\44\73\102\106" +
		"\112\124\126\127\131\142\143\144\162\170\171\176\177\202\212\216\217\225\227\234" +
		"\237\240\246\273\301\304\0\1\5\10\16\21\24\30\31\35\36\41\44\73\102\106\112\124\126" +
		"\127\131\142\143\144\162\170\171\176\177\202\212\216\217\225\227\234\237\240\246" +
		"\273\301\304\0\1\5\10\16\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\136" +
		"\142\143\144\162\170\171\176\177\202\212\216\217\225\227\234\237\240\246\273\301" +
		"\304\0\1\5\10\16\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\142\143\144" +
		"\162\170\171\176\177\202\212\216\217\221\225\227\234\237\240\246\273\301\304\0\1" +
		"\5\10\16\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\142\143\144\162\170" +
		"\171\176\177\202\212\216\217\221\225\227\234\237\240\246\273\301\304\0\1\5\10\16" +
		"\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\142\143\144\162\170\171\176" +
		"\177\202\212\216\217\221\225\227\234\237\240\246\273\301\304\0\1\5\10\16\21\24\30" +
		"\31\36\41\44\73\102\106\112\124\126\127\131\142\143\144\162\170\171\176\177\202\212" +
		"\216\217\221\225\227\234\237\240\246\273\301\304\144\170\176\177\212\223\234\237" +
		"\246\0\0\0\5\0\5\10\30\41\44\1\16\24\106\112\124\142\143\144\162\170\171\176\177" +
		"\202\212\216\225\227\234\237\240\246\273\301\304\7\37\117\122\31\73\31\73\102\131" +
		"\24\104\105\0\5\0\5\10\30\156\221\4\10\30\10\30\41\35\10\30\41\106\106\162\112\144" +
		"\170\212\144\170\212\234\144\170\212\234\144\170\176\212\234\144\170\176\177\212" +
		"\234\237\246\144\170\176\177\202\212\225\234\237\240\246\301\144\170\176\177\212" +
		"\234\237\246\10\30\41\144\170\212\234\10\30\41\144\170\176\177\205\212\234\237\246" +
		"\10\30\41\45\144\170\176\177\204\205\212\234\237\241\246\255\144\170\176\177\212" +
		"\234\237\246\171\1\16\24\124\143\216\304\16\126\150\260\21\21\144\170\176\177\212" +
		"\234\237\144\170\176\177\212\223\234\237\246\0\1\5\10\16\24\30\36\41\124\143\144" +
		"\170\177\212\216\234\237\304\10\30\73\7\37\117\122\135\156\223\136\144\170\176\177" +
		"\212\234\237\126\16");

	private static final short[] lapg_sym_to = LapgLexer.unpack_short(966,
		"\313\314\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150\153\65\13\13\166" +
		"\13\166\13\236\236\252\166\13\260\13\13\166\236\277\236\13\13\13\3\3\3\3\3\3\3\3" +
		"\3\3\3\3\3\3\3\3\3\3\3\57\57\57\14\14\14\66\14\14\14\14\15\25\15\15\62\15\155\15" +
		"\15\15\35\35\35\167\167\167\167\167\167\167\144\144\212\234\234\273\234\24\105\143" +
		"\213\225\225\301\213\301\214\214\161\163\235\303\67\127\67\67\67\70\124\70\70\70" +
		"\162\217\104\104\215\226\254\302\215\4\16\4\4\16\16\4\71\71\71\16\71\16\16\16\63" +
		"\72\125\72\72\72\31\73\31\126\73\73\31\31\73\170\221\170\170\170\170\170\170\170" +
		"\170\170\170\170\171\171\171\171\171\171\171\171\130\133\154\220\272\274\305\74\74" +
		"\74\74\75\75\75\75\76\76\76\76\247\247\247\247\250\250\250\250\77\77\77\77\251\251" +
		"\251\251\100\100\100\100\246\246\246\36\36\101\36\36\101\101\101\36\36\36\36\36\36" +
		"\36\36\36\36\36\36\17\17\17\17\17\17\17\20\20\20\20\20\20\20\21\21\21\21\21\21\21" +
		"\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13\227" +
		"\166\13\236\236\252\166\13\260\13\13\166\236\277\236\13\13\13\2\13\2\34\13\54\13" +
		"\34\65\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13\230\166\13\236\236\252" +
		"\166\13\260\13\13\166\236\277\236\13\13\13\2\13\2\34\13\54\13\34\65\106\113\116\116" +
		"\65\65\13\13\13\150\153\65\13\13\166\13\166\13\236\236\252\166\13\260\13\13\166\236" +
		"\277\236\13\13\13\2\13\2\34\13\54\13\34\65\107\113\116\116\65\65\13\13\13\150\153" +
		"\65\13\13\166\13\166\13\236\236\252\166\13\260\13\13\166\236\277\236\13\13\13\2\13" +
		"\2\34\13\54\13\34\65\110\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13\166\13" +
		"\236\236\252\166\13\260\13\13\166\236\277\236\13\13\13\2\13\2\34\13\54\13\34\65\111" +
		"\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13\166\13\236\236\252\166\13\260" +
		"\13\13\166\236\277\236\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13" +
		"\13\150\153\65\157\13\13\166\13\166\13\236\236\252\166\13\260\13\13\166\236\277\236" +
		"\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150\153\65\13\13\166" +
		"\13\166\13\236\236\252\166\13\260\261\13\13\166\236\277\236\13\13\13\2\13\2\34\13" +
		"\54\13\34\65\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13\166\13\236\236\252" +
		"\166\13\260\262\13\13\166\236\277\236\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116" +
		"\65\65\13\13\13\150\153\65\13\13\166\13\166\13\236\236\252\166\13\260\263\13\13\166" +
		"\236\277\236\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150\153" +
		"\65\13\13\166\13\166\13\236\236\252\166\13\260\264\13\13\166\236\277\236\13\13\13" +
		"\172\172\172\172\172\172\172\172\172\311\5\6\27\7\7\37\37\117\122\22\22\22\136\141" +
		"\22\164\22\173\136\173\232\173\173\173\173\22\173\271\173\173\173\173\306\173\22" +
		"\32\32\32\32\102\131\103\103\134\134\60\135\60\10\30\11\11\40\40\222\265\26\41\41" +
		"\42\42\120\112\43\43\43\137\140\224\142\174\231\256\175\175\175\275\176\176\176\176" +
		"\177\177\237\177\177\200\200\200\243\200\200\243\300\201\201\201\201\253\201\270" +
		"\201\201\253\201\307\202\202\240\240\202\202\240\240\44\44\44\203\203\203\203\45" +
		"\45\45\204\204\241\241\255\204\204\241\241\46\46\46\123\46\46\46\46\123\46\46\46" +
		"\46\123\46\123\205\205\205\205\205\205\205\205\233\312\51\61\147\165\257\310\52\151" +
		"\216\304\55\56\206\206\206\206\206\206\206\207\207\207\207\207\266\207\207\207\12" +
		"\23\12\47\23\23\47\114\121\23\23\210\210\244\210\23\210\244\23\50\64\132\33\115\145" +
		"\146\156\223\267\160\211\211\242\245\211\211\276\152\53");

	private static final short[] lapg_rlen = LapgLexer.unpack_short(133,
		"\0\1\3\2\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\0\1\3\1\1\2\2\3\3\0\1\3\0\1\0" +
		"\1\0\1\7\3\1\1\1\1\1\2\1\2\2\5\6\1\1\1\1\4\4\1\3\0\1\2\1\2\1\3\0\1\3\2\2\1\1\2\3" +
		"\2\1\2\2\4\2\3\1\1\3\1\3\2\2\2\1\2\1\1\1\2\2\4\2\3\1\3\1\1\1\1\1\0\1\5\0\1\3\1\1" +
		"\3\3\5\1\1\1\1\1\3\3\2\1\1");

	private static final short[] lapg_rlex = LapgLexer.unpack_short(133,
		"\127\127\55\55\56\56\57\57\60\61\62\62\63\63\64\64\64\64\64\64\64\64\64\64\64\130" +
		"\130\64\65\66\66\66\67\67\131\131\67\132\132\133\133\134\134\67\70\71\71\71\71\72" +
		"\72\73\73\73\74\74\74\75\75\75\76\76\77\77\135\135\100\101\101\102\102\136\136\103" +
		"\103\103\103\103\104\104\104\105\105\105\106\106\106\106\106\106\107\107\107\107" +
		"\107\110\110\110\111\112\112\113\113\113\114\115\115\116\116\116\116\116\137\137" +
		"\116\140\140\116\116\117\117\120\120\121\121\121\122\123\123\124\124\125\126");

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
		public static final int input = 45;
		public static final int options = 46;
		public static final int option = 47;
		public static final int symbol = 48;
		public static final int reference = 49;
		public static final int type = 50;
		public static final int type_part_list = 51;
		public static final int type_part = 52;
		public static final int pattern = 53;
		public static final int lexer_parts = 54;
		public static final int lexer_part = 55;
		public static final int lexem_attrs = 56;
		public static final int lexem_attribute = 57;
		public static final int icon_list = 58;
		public static final int grammar_parts = 59;
		public static final int grammar_part = 60;
		public static final int priority_kw = 61;
		public static final int directive = 62;
		public static final int inputs = 63;
		public static final int inputref = 64;
		public static final int references = 65;
		public static final int rules = 66;
		public static final int rule0 = 67;
		public static final int ruleprefix = 68;
		public static final int ruleparts = 69;
		public static final int rulepart = 70;
		public static final int rulesymref = 71;
		public static final int ruleannotations = 72;
		public static final int annotations = 73;
		public static final int annotation_list = 74;
		public static final int annotation = 75;
		public static final int negative_la = 76;
		public static final int negative_la_clause = 77;
		public static final int expression = 78;
		public static final int expression_list = 79;
		public static final int map_entries = 80;
		public static final int map_separator = 81;
		public static final int name = 82;
		public static final int qualified_id = 83;
		public static final int rule_attrs = 84;
		public static final int command = 85;
		public static final int syntax_problem = 86;
		public static final int grammar_partsopt = 87;
		public static final int type_part_listopt = 88;
		public static final int typeopt = 89;
		public static final int iconopt = 90;
		public static final int lexem_attrsopt = 91;
		public static final int commandopt = 92;
		public static final int Lnoeoiopt = 93;
		public static final int rule_attrsopt = 94;
		public static final int map_entriesopt = 95;
		public static final int expression_listopt = 96;
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
			case 84:  // rulepart ::= ruleannotations identifier '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rulepart ::= ruleannotations rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rulepart ::= identifier '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // rulepart ::= rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // rulepart ::= rulepart '&' rulepart
				 lapg_gg.sym = new AstUnorderedRulePart(((AstRulePart)lapg_m[lapg_head - 2].sym), ((AstRulePart)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 90:  // rulesymref ::= reference
				 lapg_gg.sym = new AstRuleDefaultSymbolRef(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 91:  // rulesymref ::= '(' rules ')'
				 lapg_gg.sym = new AstRuleNestedNonTerm(((List<AstRule>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // rulesymref ::= rulesymref '?'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // rulesymref ::= rulesymref '*'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // rulesymref ::= rulesymref '+'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // ruleannotations ::= annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // ruleannotations ::= negative_la annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head - 1].sym), ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // ruleannotations ::= negative_la
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // annotations ::= annotation_list
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // annotation_list ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 100:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 101:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // annotation ::= '@' identifier '=' expression
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 104:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.sym = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // negative_la_clause ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 106:  // negative_la_clause ::= negative_la_clause '|' reference
				 ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 107:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head - 3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 120:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 121:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 122:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 126:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 2].sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 129:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 132:  // syntax_problem ::= error
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
		return (AstRoot) parse(lexer, 0, 203);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 204);
	}
}
