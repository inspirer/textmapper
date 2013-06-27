/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.tool.parser.TMLexer.Lexems;
import org.textmapper.tool.parser.TMTree.TextSource;
import org.textmapper.tool.parser.ast.*;
import org.textmapper.tool.parser.TMLexer.LapgSymbol;

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
	private static final int[] tmAction = TMLexer.unpack_int(270,
		"\uffff\uffff\uffff\uffff\241\0\ufffd\uffff\uffff\uffff\1\0\ufff5\uffff\uffef\uffff" +
		"\34\0\40\0\41\0\37\0\7\0\uffff\uffff\11\0\212\0\213\0\uffc9\uffff\214\0\215\0\uffff" +
		"\uffff\216\0\227\0\uffff\uffff\10\0\uff9d\uffff\66\0\uffff\uffff\uffff\uffff\43\0" +
		"\uffff\uffff\uff95\uffff\uffff\uffff\uffff\uffff\uff85\uffff\35\0\uff77\uffff\73" +
		"\0\76\0\77\0\uffff\uffff\uff53\uffff\202\0\36\0\5\0\0\0\uff35\uffff\223\0\uff0f\uffff" +
		"\uffff\uffff\236\0\uffff\uffff\uff09\uffff\33\0\42\0\6\0\uffff\uffff\uffff\uffff" +
		"\67\0\22\0\uffff\uffff\24\0\25\0\20\0\21\0\uffff\uffff\16\0\17\0\23\0\26\0\30\0\27" +
		"\0\uffff\uffff\15\0\uff03\uffff\uffff\uffff\uffff\uffff\112\0\113\0\114\0\uffff\uffff" +
		"\ufedb\uffff\206\0\uffff\uffff\uffff\uffff\ufeb7\uffff\uffff\uffff\ufeaf\uffff\74" +
		"\0\75\0\ufea3\uffff\203\0\4\0\uffff\uffff\226\0\ufe97\uffff\uffff\uffff\70\0\71\0" +
		"\65\0\12\0\32\0\uffff\uffff\13\0\14\0\ufe79\uffff\ufe4b\uffff\117\0\uffff\uffff\123" +
		"\0\uffff\uffff\uffff\uffff\125\0\ufe43\uffff\111\0\uffff\uffff\ufe39\uffff\uffff" +
		"\uffff\uffff\uffff\ufe0d\uffff\uffff\uffff\222\0\uffff\uffff\ufe05\uffff\uffff\uffff" +
		"\237\0\31\0\uffff\uffff\45\0\ufdff\uffff\121\0\120\0\uffff\uffff\115\0\124\0\204" +
		"\0\uffff\uffff\ufdd3\uffff\uffff\uffff\ufda7\uffff\uffff\uffff\ufd65\uffff\uffff" +
		"\uffff\240\0\uffff\uffff\170\0\uffff\uffff\130\0\ufd39\uffff\132\0\ufd0d\uffff\ufcdf" +
		"\uffff\153\0\156\0\160\0\ufcad\uffff\154\0\ufc79\uffff\uffff\uffff\uffff\uffff\ufc3f" +
		"\uffff\ufc1d\uffff\155\0\140\0\ufbfd\uffff\137\0\ufbf5\uffff\uffff\uffff\ufbc9\uffff" +
		"\uffff\uffff\233\0\234\0\232\0\uffff\uffff\uffff\uffff\221\0\57\0\47\0\ufb9d\uffff" +
		"\116\0\126\0\uffff\uffff\ufb73\uffff\uffff\uffff\147\0\uffff\uffff\ufb47\uffff\210" +
		"\0\uffff\uffff\uffff\uffff\145\0\uffff\uffff\uffff\uffff\107\0\uffff\uffff\ufb19" +
		"\uffff\uffff\uffff\ufaeb\uffff\135\0\ufacb\uffff\152\0\136\0\uffff\uffff\164\0\174" +
		"\0\175\0\uffff\uffff\uffff\uffff\157\0\141\0\ufa99\uffff\ufa79\uffff\uffff\uffff" +
		"\ufa4b\uffff\uffff\uffff\uffff\uffff\ufa1f\uffff\230\0\uffff\uffff\uffff\uffff\51" +
		"\0\uf9f3\uffff\105\0\uffff\uffff\146\0\171\0\uffff\uffff\uffff\uffff\211\0\161\0" +
		"\162\0\uffff\uffff\134\0\uf9cb\uffff\166\0\143\0\uffff\uffff\127\0\106\0\uffff\uffff" +
		"\uf999\uffff\103\0\uffff\uffff\uffff\uffff\61\0\62\0\63\0\64\0\uffff\uffff\53\0\55" +
		"\0\104\0\uffff\uffff\207\0\144\0\uffff\uffff\101\0\uffff\uffff\102\0\231\0\60\0\uffff" +
		"\uffff\142\0\100\0\173\0\172\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(1680,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\44\uffff\ufffe\1\uffff\2\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\10\uffff\21\uffff\35\uffff\0\3\uffff\ufffe\1\uffff\2\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\45\uffff\4\uffff\5\uffff\21\uffff\36\uffff\37\uffff\40\uffff\22\225" +
		"\uffff\ufffe\14\uffff\17\72\22\72\uffff\ufffe\13\uffff\11\10\20\10\23\10\43\10\44" +
		"\10\47\10\uffff\ufffe\23\uffff\43\uffff\47\uffff\11\44\20\44\44\44\uffff\ufffe\1" +
		"\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\35\uffff\0\2\uffff\ufffe\35\uffff" +
		"\2\201\45\201\46\201\47\201\50\201\51\201\52\201\53\201\54\201\55\201\56\201\57\201" +
		"\60\201\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\21\uffff" +
		"\35\uffff\0\3\uffff\ufffe\17\uffff\22\224\uffff\ufffe\16\uffff\23\235\uffff\ufffe" +
		"\3\uffff\0\56\1\56\2\56\10\56\21\56\35\56\45\56\46\56\47\56\50\56\51\56\52\56\53" +
		"\56\54\56\55\56\56\56\57\56\60\56\uffff\ufffe\13\uffff\2\205\20\205\23\205\35\205" +
		"\45\205\46\205\47\205\50\205\51\205\52\205\53\205\54\205\55\205\56\205\57\205\60" +
		"\205\uffff\ufffe\23\uffff\11\44\44\44\uffff\ufffe\23\uffff\43\uffff\47\uffff\11\44" +
		"\44\44\uffff\ufffe\23\uffff\43\uffff\47\uffff\11\44\44\44\uffff\ufffe\2\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\45\uffff\25\220\uffff\ufffe\14\uffff\0\46\1\46\2\46\5\46\10\46\21" +
		"\46\23\46\35\46\45\46\46\46\47\46\50\46\51\46\52\46\53\46\54\46\55\46\56\46\57\46" +
		"\60\46\62\46\uffff\ufffe\54\uffff\15\122\17\122\uffff\ufffe\17\uffff\11\110\23\110" +
		"\44\110\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff" +
		"\24\uffff\35\uffff\62\uffff\12\133\15\133\uffff\ufffe\23\uffff\11\44\44\44\uffff" +
		"\ufffe\17\uffff\25\217\uffff\ufffe\5\uffff\0\50\1\50\2\50\10\50\21\50\23\50\35\50" +
		"\45\50\46\50\47\50\50\50\51\50\52\50\53\50\54\50\55\50\56\50\57\50\60\50\62\50\uffff" +
		"\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff" +
		"\62\uffff\12\133\15\133\uffff\ufffe\13\10\20\10\32\10\43\10\1\11\2\11\10\11\12\11" +
		"\15\11\23\11\24\11\25\11\30\11\31\11\33\11\34\11\35\11\41\11\42\11\45\11\46\11\47" +
		"\11\50\11\51\11\52\11\53\11\54\11\55\11\56\11\57\11\60\11\62\11\uffff\ufffe\1\uffff" +
		"\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff" +
		"\12\133\25\133\uffff\ufffe\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24" +
		"\uffff\35\uffff\62\uffff\12\133\15\133\25\133\uffff\ufffe\1\uffff\2\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\133\15\133\25" +
		"\133\uffff\ufffe\34\uffff\1\150\2\150\10\150\12\150\15\150\23\150\24\150\25\150\35" +
		"\150\41\150\45\150\46\150\47\150\50\150\51\150\52\150\53\150\54\150\55\150\56\150" +
		"\57\150\60\150\62\150\uffff\ufffe\33\uffff\1\163\2\163\10\163\12\163\15\163\23\163" +
		"\24\163\25\163\34\163\35\163\41\163\45\163\46\163\47\163\50\163\51\163\52\163\53" +
		"\163\54\163\55\163\56\163\57\163\60\163\62\163\uffff\ufffe\30\uffff\31\uffff\42\uffff" +
		"\1\165\2\165\10\165\12\165\15\165\23\165\24\165\25\165\33\165\34\165\35\165\41\165" +
		"\45\165\46\165\47\165\50\165\51\165\52\165\53\165\54\165\55\165\56\165\57\165\60" +
		"\165\62\165\uffff\ufffe\35\uffff\2\176\23\176\45\176\46\176\47\176\50\176\51\176" +
		"\52\176\53\176\54\176\55\176\56\176\57\176\60\176\20\201\uffff\ufffe\35\uffff\2\200" +
		"\23\200\45\200\46\200\47\200\50\200\51\200\52\200\53\200\54\200\55\200\56\200\57" +
		"\200\60\200\uffff\ufffe\12\uffff\15\131\25\131\uffff\ufffe\1\uffff\2\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\133\15\133\uffff" +
		"\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff" +
		"\62\uffff\12\133\15\133\uffff\ufffe\23\uffff\0\52\1\52\2\52\10\52\21\52\35\52\45" +
		"\52\46\52\47\52\50\52\51\52\52\52\53\52\54\52\55\52\56\52\57\52\60\52\62\52\uffff" +
		"\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff" +
		"\62\uffff\12\133\15\133\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10" +
		"\uffff\23\uffff\24\uffff\35\uffff\41\uffff\62\uffff\12\133\25\133\uffff\ufffe\1\uffff" +
		"\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff" +
		"\12\133\15\133\25\133\uffff\ufffe\35\uffff\2\176\23\176\45\176\46\176\47\176\50\176" +
		"\51\176\52\176\53\176\54\176\55\176\56\176\57\176\60\176\uffff\ufffe\34\uffff\1\151" +
		"\2\151\10\151\12\151\15\151\23\151\24\151\25\151\35\151\41\151\45\151\46\151\47\151" +
		"\50\151\51\151\52\151\53\151\54\151\55\151\56\151\57\151\60\151\62\151\uffff\ufffe" +
		"\35\uffff\2\177\23\177\45\177\46\177\47\177\50\177\51\177\52\177\53\177\54\177\55" +
		"\177\56\177\57\177\60\177\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff" +
		"\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\133\15\133\25\133\uffff\ufffe\1" +
		"\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62" +
		"\uffff\12\133\15\133\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff" +
		"\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff" +
		"\23\uffff\24\uffff\35\uffff\62\uffff\12\133\15\133\uffff\ufffe\62\uffff\0\54\1\54" +
		"\2\54\10\54\21\54\35\54\45\54\46\54\47\54\50\54\51\54\52\54\53\54\54\54\55\54\56" +
		"\54\57\54\60\54\uffff\ufffe\34\167\1\167\2\167\10\167\12\167\15\167\23\167\24\167" +
		"\25\167\35\167\41\167\45\167\46\167\47\167\50\167\51\167\52\167\53\167\54\167\55" +
		"\167\56\167\57\167\60\167\62\167\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56" +
		"\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45" +
		"\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\133\15\133\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(118,
		"\0\2\34\130\133\143\153\153\153\174\205\207\217\223\235\242\255\266\305\313\352\371" +
		"\u0102\u0106\u010a\u0110\u0112\u0115\u011a\u0121\u013c\u0143\u014a\u0151\u0152\u0153" +
		"\u0158\u015d\u019a\u01d7\u0216\u0253\u0290\u02cd\u030a\u0347\u0384\u03c1\u03fe\u043b" +
		"\u043b\u044b\u044c\u044e\u0467\u0490\u0496\u0498\u049c\u049f\u04a1\u04a5\u04a9\u04ad" +
		"\u04ae\u04af\u04b0\u04b4\u04b6\u04b8\u04ba\u04bd\u04c0\u04c3\u04c4\u04c7\u04c9\u04cb" +
		"\u04ce\u04d7\u04e1\u04eb\u04f9\u0504\u0513\u0522\u0533\u0546\u0559\u0568\u057b\u058a" +
		"\u0597\u05aa\u05c1\u05d0\u05d7\u05d8\u05da\u05db\u05dc\u05ec\u0606\u0607\u0609\u060f" +
		"\u0610\u0611\u0612\u0613\u0614\u0615\u061e\u062c\u062d\u062e\u062f\u0630");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1584,
		"\u010a\u010b\0\1\7\15\21\27\41\44\56\135\157\164\211\215\226\246\250\255\266\272" +
		"\303\323\325\330\360\363\0\1\4\7\15\21\24\27\34\41\44\50\56\70\71\101\110\114\120" +
		"\123\124\135\137\140\146\156\157\164\177\204\210\211\215\216\224\226\236\246\250" +
		"\255\256\266\267\272\275\277\300\303\304\312\316\323\325\330\342\343\354\360\363" +
		"\374\27\112\113\1\21\27\34\135\157\255\363\1\21\27\135\157\201\255\363\7\44\56\164" +
		"\211\215\224\226\246\250\266\272\303\323\325\330\360\126\163\165\166\170\212\247" +
		"\251\326\244\274\3\37\121\172\220\302\317\332\31\151\172\332\154\156\222\265\324" +
		"\327\337\357\362\u0101\34\64\101\110\146\33\34\60\101\110\146\154\161\173\347\377" +
		"\36\126\172\220\237\317\332\347\377\0\1\7\15\21\27\34\56\101\110\135\146\157\255" +
		"\363\33\34\61\101\110\146\6\34\42\63\101\110\125\127\132\146\164\167\211\215\224" +
		"\226\236\246\250\262\266\272\275\277\303\304\312\323\325\330\360\164\211\215\224" +
		"\226\246\250\266\272\303\312\323\325\330\360\74\101\110\146\174\271\274\370\374\34" +
		"\101\110\146\34\101\110\146\34\101\110\146\235\u0105\235\u0105\220\302\317\34\101" +
		"\110\146\233\34\101\110\146\227\307\351\7\34\44\51\56\101\110\146\164\211\215\224" +
		"\226\240\241\246\250\266\272\303\305\312\322\323\325\330\360\1\21\27\135\157\255" +
		"\363\1\21\27\135\157\255\363\1\21\27\135\157\255\363\272\235\42\127\132\220\317\126" +
		"\163\166\170\247\0\1\4\7\15\21\24\27\34\41\44\50\56\70\71\101\110\114\120\123\124" +
		"\135\137\140\146\156\157\164\177\204\210\211\214\215\216\224\226\236\246\250\255" +
		"\256\266\267\272\275\277\300\303\304\312\316\323\325\330\342\343\354\360\363\374" +
		"\0\1\4\7\15\21\24\27\34\41\44\50\56\70\71\101\110\114\120\123\124\135\137\140\146" +
		"\156\157\164\177\204\210\211\214\215\216\224\226\236\246\250\255\256\266\267\272" +
		"\275\277\300\303\304\312\316\323\325\330\342\343\354\360\363\374\0\1\4\7\15\21\24" +
		"\27\34\41\42\44\50\56\70\71\101\110\114\120\123\124\127\132\135\137\140\146\156\157" +
		"\164\177\204\210\211\215\216\224\226\236\246\250\255\256\266\267\272\275\277\300" +
		"\303\304\312\316\323\325\330\342\343\354\360\363\374\0\1\4\7\15\21\24\27\34\40\41" +
		"\44\50\56\70\71\101\110\114\120\123\124\135\137\140\146\156\157\164\177\204\210\211" +
		"\215\216\224\226\236\246\250\255\256\266\267\272\275\277\300\303\304\312\316\323" +
		"\325\330\342\343\354\360\363\374\0\1\4\7\15\21\24\27\34\40\41\44\50\56\70\71\101" +
		"\110\114\120\123\124\135\137\140\146\156\157\164\177\204\210\211\215\216\224\226" +
		"\236\246\250\255\256\266\267\272\275\277\300\303\304\312\316\323\325\330\342\343" +
		"\354\360\363\374\0\1\4\7\15\21\24\27\34\40\41\44\50\56\70\71\101\110\114\120\123" +
		"\124\135\137\140\146\156\157\164\177\204\210\211\215\216\224\226\236\246\250\255" +
		"\256\266\267\272\275\277\300\303\304\312\316\323\325\330\342\343\354\360\363\374" +
		"\0\1\4\7\15\21\24\27\34\40\41\44\50\56\70\71\101\110\114\120\123\124\135\137\140" +
		"\146\156\157\164\177\204\210\211\215\216\224\226\236\246\250\255\256\266\267\272" +
		"\275\277\300\303\304\312\316\323\325\330\342\343\354\360\363\374\0\1\4\7\15\21\24" +
		"\27\34\41\44\50\56\70\71\101\110\114\120\123\124\135\137\140\146\152\156\157\164" +
		"\177\204\210\211\215\216\224\226\236\246\250\255\256\266\267\272\275\277\300\303" +
		"\304\312\316\323\325\330\342\343\354\360\363\374\0\1\4\7\15\21\24\27\34\41\44\50" +
		"\56\70\71\101\110\114\120\123\124\135\137\140\146\156\157\164\177\204\210\211\215" +
		"\216\224\226\236\246\250\255\256\266\267\272\275\277\300\303\304\312\316\323\325" +
		"\330\333\342\343\354\360\363\374\0\1\4\7\15\21\24\27\34\41\44\50\56\70\71\101\110" +
		"\114\120\123\124\135\137\140\146\156\157\164\177\204\210\211\215\216\224\226\236" +
		"\246\250\255\256\266\267\272\275\277\300\303\304\312\316\323\325\330\333\342\343" +
		"\354\360\363\374\0\1\4\7\15\21\24\27\34\41\44\50\56\70\71\101\110\114\120\123\124" +
		"\135\137\140\146\156\157\164\177\204\210\211\215\216\224\226\236\246\250\255\256" +
		"\266\267\272\275\277\300\303\304\312\316\323\325\330\333\342\343\354\360\363\374" +
		"\0\1\4\7\15\21\24\27\34\41\44\50\56\70\71\101\110\114\120\123\124\135\137\140\146" +
		"\156\157\164\177\204\210\211\215\216\224\226\236\246\250\255\256\266\267\272\275" +
		"\277\300\303\304\312\316\323\325\330\333\342\343\354\360\363\374\164\211\215\224" +
		"\226\246\250\266\272\303\312\323\325\330\335\360\0\0\15\0\4\7\15\44\50\56\71\164" +
		"\211\215\224\226\236\246\250\266\272\303\304\312\323\325\330\360\1\21\27\114\120" +
		"\123\124\135\156\157\164\204\210\211\215\216\224\226\236\246\250\255\266\267\272" +
		"\275\277\300\303\304\312\316\323\325\330\342\343\354\360\363\374\6\42\125\127\132" +
		"\167\34\101\34\101\110\146\27\112\113\0\15\0\7\15\56\0\7\15\56\0\7\15\56\151\262" +
		"\333\0\7\15\56\70\177\4\71\7\56\7\44\56\7\44\56\42\127\132\40\7\44\56\114\204\120" +
		"\342\123\300\354\164\211\215\246\250\266\325\330\360\164\211\215\246\250\266\323" +
		"\325\330\360\164\211\215\246\250\266\323\325\330\360\164\211\215\224\226\246\250" +
		"\266\272\303\323\325\330\360\164\211\215\224\246\250\266\323\325\330\360\164\211" +
		"\215\224\226\246\250\266\272\303\312\323\325\330\360\164\211\215\224\226\246\250" +
		"\266\272\303\312\323\325\330\360\164\211\215\224\226\236\246\250\266\272\303\304" +
		"\312\323\325\330\360\164\211\215\224\226\236\246\250\266\272\275\277\303\304\312" +
		"\323\325\330\360\164\211\215\224\226\236\246\250\266\272\275\277\303\304\312\323" +
		"\325\330\360\164\211\215\224\226\246\250\266\272\303\312\323\325\330\360\164\211" +
		"\215\224\226\236\246\250\266\272\275\277\303\304\312\323\325\330\360\164\211\215" +
		"\224\226\246\250\266\272\303\312\323\325\330\360\7\44\56\164\211\215\246\250\266" +
		"\323\325\330\360\7\44\56\164\211\215\224\226\241\246\250\266\272\303\312\323\325" +
		"\330\360\7\44\51\56\164\211\215\224\226\240\241\246\250\266\272\303\305\312\322\323" +
		"\325\330\360\164\211\215\224\226\246\250\266\272\303\312\323\325\330\360\1\21\27" +
		"\135\157\255\363\137\172\332\24\24\164\211\215\224\226\246\250\266\272\303\312\323" +
		"\325\330\335\360\0\1\7\15\21\27\41\44\56\135\157\164\211\215\226\246\250\255\266" +
		"\272\303\323\325\330\360\363\0\7\56\6\42\125\127\132\167\151\201\262\335\4\114\164" +
		"\211\215\246\250\266\325\330\360\164\211\215\224\226\246\250\266\272\303\323\325" +
		"\330\360\216\137\21\21");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1584,
		"\u010c\u010d\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\3\16\30\37\3\16" +
		"\62\16\73\121\30\30\37\141\30\73\73\16\16\16\16\16\172\175\73\16\16\213\141\16\16" +
		"\213\213\16\213\213\213\213\213\16\332\213\16\213\16\16\16\213\213\213\16\213\213" +
		"\213\16\16\16\213\16\16\65\65\65\17\17\17\74\17\17\17\17\20\20\20\20\20\261\20\20" +
		"\40\40\40\214\214\214\214\214\214\214\214\214\214\214\214\214\214\164\211\246\164" +
		"\250\266\325\330\360\323\343\27\113\157\252\275\275\275\252\70\177\253\253\203\205" +
		"\301\336\356\361\373\u0100\u0102\u0107\75\140\75\75\75\71\76\135\76\76\76\204\210" +
		"\256\210\210\112\112\254\276\321\353\254\376\u0106\4\21\4\4\21\21\77\4\77\77\21\77" +
		"\21\21\21\72\100\136\100\100\100\34\101\34\137\101\101\34\34\34\101\215\34\215\215" +
		"\215\215\215\215\215\333\215\215\215\215\215\215\215\215\215\215\215\216\216\216" +
		"\216\216\216\216\216\216\216\216\216\216\216\216\144\145\147\176\257\341\344\u0104" +
		"\u0105\102\102\102\102\103\103\103\103\104\104\104\104\314\u0108\315\u0109\277\277" +
		"\277\105\105\105\105\313\106\106\106\106\312\312\312\41\107\41\41\41\107\107\107" +
		"\41\41\41\41\41\41\41\41\41\41\41\41\41\41\41\41\41\41\41\22\22\22\22\22\22\22\23" +
		"\23\23\23\23\23\23\24\24\24\24\24\24\24\342\316\123\123\123\300\354\165\212\165\251" +
		"\326\3\16\30\37\3\16\62\16\73\121\30\30\37\141\30\73\73\16\16\16\16\16\172\175\73" +
		"\16\16\213\141\16\16\213\267\213\16\213\213\213\213\213\16\332\213\16\213\16\16\16" +
		"\213\213\213\16\213\213\213\16\16\16\213\16\16\3\16\30\37\3\16\62\16\73\121\30\30" +
		"\37\141\30\73\73\16\16\16\16\16\172\175\73\16\16\213\141\16\16\213\270\213\16\213" +
		"\213\213\213\213\16\332\213\16\213\16\16\16\213\213\213\16\213\213\213\16\16\16\213" +
		"\16\16\3\16\30\37\3\16\62\16\73\121\124\30\30\37\141\30\73\73\16\16\16\16\124\124" +
		"\16\172\175\73\16\16\213\141\16\16\213\213\16\213\213\213\213\213\16\332\213\16\213" +
		"\16\16\16\213\213\213\16\213\213\213\16\16\16\213\16\16\3\16\30\37\3\16\62\16\73" +
		"\114\121\30\30\37\141\30\73\73\16\16\16\16\16\172\175\73\16\16\213\141\16\16\213" +
		"\213\16\213\213\213\213\213\16\332\213\16\213\16\16\16\213\213\213\16\213\213\213" +
		"\16\16\16\213\16\16\3\16\30\37\3\16\62\16\73\115\121\30\30\37\141\30\73\73\16\16" +
		"\16\16\16\172\175\73\16\16\213\141\16\16\213\213\16\213\213\213\213\213\16\332\213" +
		"\16\213\16\16\16\213\213\213\16\213\213\213\16\16\16\213\16\16\3\16\30\37\3\16\62" +
		"\16\73\116\121\30\30\37\141\30\73\73\16\16\16\16\16\172\175\73\16\16\213\141\16\16" +
		"\213\213\16\213\213\213\213\213\16\332\213\16\213\16\16\16\213\213\213\16\213\213" +
		"\213\16\16\16\213\16\16\3\16\30\37\3\16\62\16\73\117\121\30\30\37\141\30\73\73\16" +
		"\16\16\16\16\172\175\73\16\16\213\141\16\16\213\213\16\213\213\213\213\213\16\332" +
		"\213\16\213\16\16\16\213\213\213\16\213\213\213\16\16\16\213\16\16\3\16\30\37\3\16" +
		"\62\16\73\121\30\30\37\141\30\73\73\16\16\16\16\16\172\175\73\202\16\16\213\141\16" +
		"\16\213\213\16\213\213\213\213\213\16\332\213\16\213\16\16\16\213\213\213\16\213" +
		"\213\213\16\16\16\213\16\16\3\16\30\37\3\16\62\16\73\121\30\30\37\141\30\73\73\16" +
		"\16\16\16\16\172\175\73\16\16\213\141\16\16\213\213\16\213\213\213\213\213\16\332" +
		"\213\16\213\16\16\16\213\213\213\16\213\213\213\364\16\16\16\213\16\16\3\16\30\37" +
		"\3\16\62\16\73\121\30\30\37\141\30\73\73\16\16\16\16\16\172\175\73\16\16\213\141" +
		"\16\16\213\213\16\213\213\213\213\213\16\332\213\16\213\16\16\16\213\213\213\16\213" +
		"\213\213\365\16\16\16\213\16\16\3\16\30\37\3\16\62\16\73\121\30\30\37\141\30\73\73" +
		"\16\16\16\16\16\172\175\73\16\16\213\141\16\16\213\213\16\213\213\213\213\213\16" +
		"\332\213\16\213\16\16\16\213\213\213\16\213\213\213\366\16\16\16\213\16\16\3\16\30" +
		"\37\3\16\62\16\73\121\30\30\37\141\30\73\73\16\16\16\16\16\172\175\73\16\16\213\141" +
		"\16\16\213\213\16\213\213\213\213\213\16\332\213\16\213\16\16\16\213\213\213\16\213" +
		"\213\213\367\16\16\16\213\16\16\217\217\217\217\217\217\217\217\217\217\217\217\217" +
		"\217\217\217\u010a\5\55\6\31\42\6\127\132\42\31\220\220\220\302\302\317\220\220\220" +
		"\302\302\302\302\220\220\220\220\25\25\25\152\155\160\162\25\206\25\221\152\264\221" +
		"\221\273\221\221\221\221\221\25\221\340\221\221\221\160\221\221\221\352\221\221\221" +
		"\155\375\160\221\25\206\35\35\35\35\35\35\110\146\111\111\150\150\66\151\66\7\56" +
		"\10\43\10\43\11\11\11\11\12\12\12\12\200\334\370\13\13\13\13\142\260\32\143\44\44" +
		"\45\130\45\46\46\46\125\125\167\120\47\47\47\153\263\156\374\161\347\377\222\265" +
		"\271\324\327\337\357\362\u0101\223\223\223\223\223\223\355\223\223\223\224\224\224" +
		"\224\224\224\224\224\224\224\225\225\225\225\225\225\225\225\225\225\225\225\225" +
		"\225\226\226\272\303\226\226\226\226\226\226\226\227\227\227\227\307\227\227\227" +
		"\307\307\351\227\227\227\227\230\230\230\230\230\230\230\230\230\230\230\230\230" +
		"\230\230\231\231\231\231\231\320\231\231\231\231\231\320\231\231\231\231\231\232" +
		"\232\232\232\232\232\232\232\232\232\345\346\232\232\232\232\232\232\232\233\233" +
		"\233\233\233\233\233\233\233\233\233\233\233\233\233\233\233\233\233\234\234\234" +
		"\234\234\234\234\234\234\234\234\234\234\234\234\235\235\235\235\235\235\235\235" +
		"\235\235\235\235\235\235\235\235\235\235\235\236\236\236\304\304\236\236\236\304" +
		"\304\304\236\236\236\236\50\50\50\237\237\237\237\237\237\237\237\237\237\51\51\51" +
		"\240\240\240\305\305\322\240\240\240\305\305\305\240\240\240\240\52\52\133\52\52" +
		"\52\52\52\52\133\52\52\52\52\52\52\133\52\133\52\52\52\52\241\241\241\241\241\241" +
		"\241\241\241\241\241\241\241\241\241\u010b\57\67\171\207\331\u0103\173\255\363\63" +
		"\64\242\242\242\242\242\242\242\242\242\242\242\242\242\242\371\242\14\26\53\14\26" +
		"\26\122\131\53\26\26\243\243\243\310\243\243\26\243\310\310\243\243\243\243\26\15" +
		"\54\134\36\126\163\166\170\247\201\262\335\372\33\154\244\244\244\244\244\244\244" +
		"\244\244\245\245\245\306\311\245\245\245\311\350\245\245\245\245\274\174\60\61");

	private static final short[] lapg_rlen = TMLexer.unpack_short(162,
		"\2\1\1\0\3\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\3\2\1\1\2\2\1\1\1\3\1\0\1\0\1" +
		"\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\10\7\7\6\7\6\6\5\2\2\1\1\1\4\3" +
		"\1\4\2\1\1\2\1\3\3\1\1\1\0\3\2\2\1\1\2\5\3\4\2\3\2\1\2\2\1\1\1\1\2\1\3\3\1\2\1\3" +
		"\3\1\3\6\6\2\2\1\2\1\1\1\2\4\2\2\3\1\3\1\1\1\1\1\1\0\5\3\1\1\0\3\1\3\5\1\1\1\1\1" +
		"\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(162,
		"\146\146\147\147\63\63\64\64\65\66\67\67\70\70\71\71\71\71\71\71\71\71\71\71\71\71" +
		"\71\72\73\73\73\74\74\74\75\150\150\151\151\152\152\153\153\154\154\76\76\77\100" +
		"\101\101\101\101\155\155\102\103\104\104\105\105\105\106\106\107\107\107\107\107" +
		"\107\107\107\110\110\111\111\111\112\156\156\112\113\113\114\114\115\115\157\157" +
		"\116\160\160\117\117\117\117\117\120\120\120\120\120\121\121\122\122\122\123\123" +
		"\123\124\124\125\125\125\126\126\127\127\130\131\131\131\131\131\131\132\132\132" +
		"\133\134\134\135\135\135\161\161\136\137\137\137\137\137\162\162\137\163\163\164" +
		"\164\137\137\140\140\141\141\141\142\143\143\144\145");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"error",
		"ID",
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
		"'+='",
		"'?'",
		"'&'",
		"'@'",
		"Ltrue",
		"Lfalse",
		"Lnew",
		"Lseparator",
		"Las",
		"Lextends",
		"Linline",
		"Lprio",
		"Lshift",
		"Lreturns",
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
		"stateref",
		"lexer_state",
		"grammar_parts",
		"grammar_part",
		"nonterm",
		"nonterm_ast",
		"priority_kw",
		"directive",
		"inputref",
		"references",
		"references_cs",
		"rules",
		"rule0",
		"rhsPrefix",
		"rhsSuffix",
		"rhsParts",
		"rhsPart",
		"rhsAnnotated",
		"rhsAssignment",
		"rhsOptional",
		"rhsCast",
		"rhsUnordered",
		"rhsPrimary",
		"rhsAnnotations",
		"annotations",
		"annotation_list",
		"annotation",
		"negative_la",
		"expression",
		"map_entries",
		"map_separator",
		"name",
		"qualified_id",
		"command",
		"syntax_problem",
		"option_list",
		"grammar_partsopt",
		"typeopt",
		"lexem_transitionopt",
		"iconopt",
		"lexem_attrsopt",
		"commandopt",
		"lexer_state_list",
		"inputref_list",
		"rule0_list",
		"rhsSuffixopt",
		"symref_list",
		"map_entriesopt",
		"expression_list",
		"expression_list_opt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 51;
		public static final int option = 52;
		public static final int identifier = 53;
		public static final int symref = 54;
		public static final int type = 55;
		public static final int type_part_list = 56;
		public static final int type_part = 57;
		public static final int pattern = 58;
		public static final int lexer_parts = 59;
		public static final int lexer_part = 60;
		public static final int named_pattern = 61;
		public static final int lexeme = 62;
		public static final int lexem_transition = 63;
		public static final int lexem_attrs = 64;
		public static final int lexem_attribute = 65;
		public static final int state_selector = 66;
		public static final int stateref = 67;
		public static final int lexer_state = 68;
		public static final int grammar_parts = 69;
		public static final int grammar_part = 70;
		public static final int nonterm = 71;
		public static final int nonterm_ast = 72;
		public static final int priority_kw = 73;
		public static final int directive = 74;
		public static final int inputref = 75;
		public static final int references = 76;
		public static final int references_cs = 77;
		public static final int rules = 78;
		public static final int rule0 = 79;
		public static final int rhsPrefix = 80;
		public static final int rhsSuffix = 81;
		public static final int rhsParts = 82;
		public static final int rhsPart = 83;
		public static final int rhsAnnotated = 84;
		public static final int rhsAssignment = 85;
		public static final int rhsOptional = 86;
		public static final int rhsCast = 87;
		public static final int rhsUnordered = 88;
		public static final int rhsPrimary = 89;
		public static final int rhsAnnotations = 90;
		public static final int annotations = 91;
		public static final int annotation_list = 92;
		public static final int annotation = 93;
		public static final int negative_la = 94;
		public static final int expression = 95;
		public static final int map_entries = 96;
		public static final int map_separator = 97;
		public static final int name = 98;
		public static final int qualified_id = 99;
		public static final int command = 100;
		public static final int syntax_problem = 101;
		public static final int option_list = 102;
		public static final int grammar_partsopt = 103;
		public static final int typeopt = 104;
		public static final int lexem_transitionopt = 105;
		public static final int iconopt = 106;
		public static final int lexem_attrsopt = 107;
		public static final int commandopt = 108;
		public static final int lexer_state_list = 109;
		public static final int inputref_list = 110;
		public static final int rule0_list = 111;
		public static final int rhsSuffixopt = 112;
		public static final int symref_list = 113;
		public static final int map_entriesopt = 114;
		public static final int expression_list = 115;
		public static final int expression_list_opt = 116;
	}

	public interface Rules {
		public static final int directive_input = 80;  // directive ::= '%' Linput inputref_list ';'
		public static final int rhsPrimary_symbol = 120;  // rhsPrimary ::= symref
		public static final int rhsPrimary_group = 121;  // rhsPrimary ::= '(' rules ')'
		public static final int rhsPrimary_list = 122;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		public static final int rhsPrimary_list2 = 123;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		public static final int rhsPrimary_list3 = 124;  // rhsPrimary ::= rhsPrimary '*'
		public static final int rhsPrimary_list4 = 125;  // rhsPrimary ::= rhsPrimary '+'
		public static final int expression_literal = 138;  // expression ::= scon
		public static final int expression_literal2 = 139;  // expression ::= icon
		public static final int expression_literal3 = 140;  // expression ::= Ltrue
		public static final int expression_literal4 = 141;  // expression ::= Lfalse
		public static final int expression_instance = 145;  // expression ::= Lnew name '(' map_entriesopt ')'
		public static final int expression_array = 150;  // expression ::= '[' expression_list_opt ']'
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
	protected LapgSymbol[] tmStack;
	protected LapgSymbol tmNext;
	protected TMLexer tmLexer;

	private Object parse(TMLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new LapgSymbol[1024];
		tmHead = 0;
		int lapg_symbols_ok = 4;

		tmStack[0] = new LapgSymbol();
		tmStack[0].state = initialState;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != finalState) {
			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
				lapg_symbols_ok++;
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(tmNext.offset, tmNext.endoffset, tmNext.line,
								MessageFormat.format("syntax error before line {0}", tmLexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
						tmNext = tmLexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (tmHead < 0) {
					tmHead = 0;
					tmStack[0] = new LapgSymbol();
					tmStack[0].state = initialState;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(tmNext.offset, tmNext.endoffset, tmNext.line,
						MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return tmStack[tmHead - 1].value;
	}

	protected boolean restore() {
		if (tmNext.symbol == 0) {
			return false;
		}
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 1) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new LapgSymbol();
			tmStack[tmHead].symbol = 1;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 1);
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
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[tmNext.symbol], tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.value = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]].value : null;
		lapg_gg.symbol = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]] : tmNext;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			cleanup(tmStack[tmHead]);
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = lapg_gg;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, lapg_gg.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 0:  // option_list ::= option_list option
				((List<TmaOption>)lapg_gg.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 1:  // option_list ::= option
				lapg_gg.value = new ArrayList();
				((List<TmaOption>)lapg_gg.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 4:  // input ::= option_list lexer_parts grammar_partsopt
				lapg_gg.value = new TmaInput(
						((List<TmaOption>)tmStack[tmHead - 2].value) /* optionList */,
						((List<TmaLexerPartsItem>)tmStack[tmHead - 1].value) /* lexerParts */,
						((List<TmaGrammarPartsItem>)tmStack[tmHead].value) /* grammarParts */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 5:  // input ::= lexer_parts grammar_partsopt
				lapg_gg.value = new TmaInput(
						null /* optionList */,
						((List<TmaLexerPartsItem>)tmStack[tmHead - 1].value) /* lexerParts */,
						((List<TmaGrammarPartsItem>)tmStack[tmHead].value) /* grammarParts */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 6:  // option ::= ID '=' expression
				lapg_gg.value = new TmaOption(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 7:  // option ::= syntax_problem
				lapg_gg.value = new TmaOption(
						null /* ID */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // identifier ::= ID
				lapg_gg.value = new TmaIdentifier(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 9:  // symref ::= ID
				lapg_gg.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 10:  // type ::= '(' scon ')'
				 lapg_gg.value = ((String)tmStack[tmHead - 1].value); 
				break;
			case 11:  // type ::= '(' type_part_list ')'
				 lapg_gg.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 28:  // lexer_parts ::= lexer_part
				lapg_gg.value = new ArrayList();
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 29:  // lexer_parts ::= lexer_parts lexer_part
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 30:  // lexer_parts ::= lexer_parts syntax_problem
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						null /* lexerPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 34:  // named_pattern ::= ID '=' pattern
				lapg_gg.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((String)tmStack[tmHead].value) /* pattern */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 45:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				lapg_gg.value = new TmaLexeme(
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaIdentifier)tmStack[tmHead - 7].value) /* identifier */,
						((String)tmStack[tmHead - 6].value) /* type */,
						((String)tmStack[tmHead - 4].value) /* pattern */,
						((TmaStateref)tmStack[tmHead - 3].value) /* lexemTransition */,
						((TmaLexemAttrs)tmStack[tmHead - 1].value) /* lexemAttrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						null /* input */, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 46:  // lexeme ::= identifier typeopt ':'
				lapg_gg.value = new TmaLexeme(
						null /* priority */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((String)tmStack[tmHead - 1].value) /* type */,
						null /* pattern */,
						null /* lexemTransition */,
						null /* lexemAttrs */,
						null /* command */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 47:  // lexem_transition ::= '=>' stateref
				lapg_gg.value = ((TmaStateref)tmStack[tmHead].value);
				break;
			case 48:  // lexem_attrs ::= '(' lexem_attribute ')'
				lapg_gg.value = new TmaLexemAttrs(
						((TmaLexemAttribute)tmStack[tmHead - 1].value) /* lexemAttribute */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // lexem_attribute ::= Lsoft
				lapg_gg.value = TmaLexemAttribute.LSOFT;
				break;
			case 50:  // lexem_attribute ::= Lclass
				lapg_gg.value = TmaLexemAttribute.LCLASS;
				break;
			case 51:  // lexem_attribute ::= Lspace
				lapg_gg.value = TmaLexemAttribute.LSPACE;
				break;
			case 52:  // lexem_attribute ::= Llayout
				lapg_gg.value = TmaLexemAttribute.LLAYOUT;
				break;
			case 53:  // lexer_state_list ::= lexer_state_list ',' lexer_state
				((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 54:  // lexer_state_list ::= lexer_state
				lapg_gg.value = new ArrayList();
				((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 55:  // state_selector ::= '[' lexer_state_list ']'
				lapg_gg.value = new TmaStateSelector(
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 56:  // stateref ::= ID
				lapg_gg.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 57:  // lexer_state ::= identifier '=>' stateref
				lapg_gg.value = new TmaLexerState(
						((TmaStateref)tmStack[tmHead].value) /* defaultTransition */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // lexer_state ::= identifier
				lapg_gg.value = new TmaLexerState(
						null /* defaultTransition */,
						((TmaIdentifier)tmStack[tmHead].value) /* identifier */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // grammar_parts ::= grammar_part
				lapg_gg.value = new ArrayList();
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 60:  // grammar_parts ::= grammar_parts grammar_part
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 61:  // grammar_parts ::= grammar_parts syntax_problem
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						null /* grammarPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 64:  // nonterm ::= annotations identifier nonterm_ast typeopt Linline '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((List<TmaAnnotation>)tmStack[tmHead - 7].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* identifier */,
						((TmaNontermAst)tmStack[tmHead - 5].value) /* nontermAst */,
						((String)tmStack[tmHead - 4].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 65:  // nonterm ::= annotations identifier nonterm_ast typeopt '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((List<TmaAnnotation>)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* identifier */,
						((TmaNontermAst)tmStack[tmHead - 4].value) /* nontermAst */,
						((String)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 66:  // nonterm ::= annotations identifier typeopt Linline '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((List<TmaAnnotation>)tmStack[tmHead - 6].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* identifier */,
						null /* nontermAst */,
						((String)tmStack[tmHead - 4].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 67:  // nonterm ::= annotations identifier typeopt '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((List<TmaAnnotation>)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* identifier */,
						null /* nontermAst */,
						((String)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 68:  // nonterm ::= identifier nonterm_ast typeopt Linline '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 6].value) /* identifier */,
						((TmaNontermAst)tmStack[tmHead - 5].value) /* nontermAst */,
						((String)tmStack[tmHead - 4].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 69:  // nonterm ::= identifier nonterm_ast typeopt '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* identifier */,
						((TmaNontermAst)tmStack[tmHead - 4].value) /* nontermAst */,
						((String)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 70:  // nonterm ::= identifier typeopt Linline '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 5].value) /* identifier */,
						null /* nontermAst */,
						((String)tmStack[tmHead - 4].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // nonterm ::= identifier typeopt '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* identifier */,
						null /* nontermAst */,
						((String)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 72:  // nonterm_ast ::= Lextends references_cs
				lapg_gg.value = new TmaNontermAst(
						((List<TmaSymref>)tmStack[tmHead].value) /* referencesCs */,
						null /* symref */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 73:  // nonterm_ast ::= Lreturns symref
				lapg_gg.value = new TmaNontermAst(
						null /* referencesCs */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 74:  // priority_kw ::= Lleft
				lapg_gg.value = TmaPriorityKw.LLEFT;
				break;
			case 75:  // priority_kw ::= Lright
				lapg_gg.value = TmaPriorityKw.LRIGHT;
				break;
			case 76:  // priority_kw ::= Lnonassoc
				lapg_gg.value = TmaPriorityKw.LNONASSOC;
				break;
			case 77:  // directive ::= '%' priority_kw references ';'
				lapg_gg.value = new TmaDirectiveImpl(
						((TmaPriorityKw)tmStack[tmHead - 2].value) /* priorityKw */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* references */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 78:  // inputref_list ::= inputref_list ',' inputref
				((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 79:  // inputref_list ::= inputref
				lapg_gg.value = new ArrayList();
				((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 80:  // directive ::= '%' Linput inputref_list ';'
				lapg_gg.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputrefList */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 81:  // inputref ::= symref Lnoeoi
				lapg_gg.value = new TmaInputref(
						true /* noeoi */,
						((TmaSymref)tmStack[tmHead - 1].value) /* symref */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // inputref ::= symref
				lapg_gg.value = new TmaInputref(
						false /* noeoi */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 83:  // references ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 84:  // references ::= references symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 85:  // references_cs ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 86:  // references_cs ::= references_cs ',' symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 87:  // rule0_list ::= rule0_list '|' rule0
				((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 88:  // rule0_list ::= rule0
				lapg_gg.value = new ArrayList();
				((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 92:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // rule0 ::= rhsPrefix rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // rule0 ::= rhsParts rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 95:  // rule0 ::= rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 96:  // rule0 ::= syntax_problem
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						null /* rhsSuffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 97:  // rhsPrefix ::= annotations ':'
				lapg_gg.value = new TmaRhsPrefix(
						((List<TmaAnnotation>)tmStack[tmHead - 1].value) /* annotations */,
						null /* alias */,
						null /* _extends */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 98:  // rhsPrefix ::= rhsAnnotations identifier Lextends references_cs ':'
				lapg_gg.value = new TmaRhsPrefix(
						((TmaRhsAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* alias */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* _extends */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // rhsPrefix ::= rhsAnnotations identifier ':'
				lapg_gg.value = new TmaRhsPrefix(
						((TmaRhsAnnotations)tmStack[tmHead - 2].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* _extends */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // rhsPrefix ::= identifier Lextends references_cs ':'
				lapg_gg.value = new TmaRhsPrefix(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* alias */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* _extends */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // rhsPrefix ::= identifier ':'
				lapg_gg.value = new TmaRhsPrefix(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* _extends */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // rhsSuffix ::= '%' Lprio symref
				lapg_gg.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPRIO /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // rhsSuffix ::= '%' Lshift
				lapg_gg.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						null /* symref */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // rhsParts ::= rhsPart
				lapg_gg.value = new ArrayList();
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 105:  // rhsParts ::= rhsParts rhsPart
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 106:  // rhsParts ::= rhsParts syntax_problem
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						null /* rhsPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 111:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				lapg_gg.value = new TmaRhsAnnotated(
						((TmaRhsAnnotations)tmStack[tmHead - 1].value) /* rhsAnnotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsAssignment */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rhsAssignment ::= identifier '=' rhsOptional
				lapg_gg.value = new TmaRhsAssignment(
						false /* addition */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsOptional */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 114:  // rhsAssignment ::= identifier '+=' rhsOptional
				lapg_gg.value = new TmaRhsAssignment(
						true /* addition */,
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsOptional */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 116:  // rhsOptional ::= rhsCast '?'
				lapg_gg.value = new TmaRhsOptional(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* rhsCast */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // rhsCast ::= rhsPrimary Las symref
				lapg_gg.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* rhsPrimary */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 119:  // rhsUnordered ::= rhsPart '&' rhsPart
				lapg_gg.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // rhsPrimary ::= symref
				lapg_gg.value = new TmaRhsPrimarySymbol(
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 121:  // rhsPrimary ::= '(' rules ')'
				lapg_gg.value = new TmaRhsPrimaryGroup(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.PLUS /* quantifier */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						null /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.MULT /* quantifier */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						null /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 124:  // rhsPrimary ::= rhsPrimary '*'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.MULT /* quantifier */,
						null /* rhsParts */,
						null /* references */,
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 125:  // rhsPrimary ::= rhsPrimary '+'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.PLUS /* quantifier */,
						null /* rhsParts */,
						null /* references */,
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 126:  // rhsAnnotations ::= annotation_list
				lapg_gg.value = new TmaRhsAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotationList */,
						null /* negativeLa */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // rhsAnnotations ::= negative_la annotation_list
				lapg_gg.value = new TmaRhsAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotationList */,
						((TmaNegativeLa)tmStack[tmHead - 1].value) /* negativeLa */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // rhsAnnotations ::= negative_la
				lapg_gg.value = new TmaRhsAnnotations(
						null /* annotationList */,
						((TmaNegativeLa)tmStack[tmHead].value) /* negativeLa */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // annotation_list ::= annotation
				lapg_gg.value = new ArrayList();
				((List<TmaAnnotation>)lapg_gg.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 131:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)lapg_gg.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 132:  // annotation ::= '@' ID '=' expression
				lapg_gg.value = new TmaAnnotation(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // annotation ::= '@' ID
				lapg_gg.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* ID */,
						null /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // annotation ::= '@' syntax_problem
				lapg_gg.value = new TmaAnnotation(
						null /* ID */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // symref_list ::= symref_list '|' symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 136:  // symref_list ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 137:  // negative_la ::= '(?!' symref_list ')'
				lapg_gg.value = new TmaNegativeLa(
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* unwantedSymbols */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // expression ::= scon
				lapg_gg.value = new TmaExpressionLiteral(
						((String)tmStack[tmHead].value) /* sval */,
						null /* ival */,
						false /* isTrue */,
						false /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // expression ::= icon
				lapg_gg.value = new TmaExpressionLiteral(
						null /* sval */,
						((Integer)tmStack[tmHead].value) /* ival */,
						false /* isTrue */,
						false /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 140:  // expression ::= Ltrue
				lapg_gg.value = new TmaExpressionLiteral(
						null /* sval */,
						null /* ival */,
						true /* isTrue */,
						false /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // expression ::= Lfalse
				lapg_gg.value = new TmaExpressionLiteral(
						null /* sval */,
						null /* ival */,
						false /* isTrue */,
						true /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // expression ::= Lnew name '(' map_entriesopt ')'
				lapg_gg.value = new TmaExpressionInstance(
						((TmaName)tmStack[tmHead - 3].value) /* name */,
						((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value) /* mapEntries */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 146:  // expression_list ::= expression_list ',' expression
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 147:  // expression_list ::= expression
				lapg_gg.value = new ArrayList();
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 150:  // expression ::= '[' expression_list_opt ']'
				lapg_gg.value = new TmaExpressionArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* expressionList */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // map_entries ::= ID map_separator expression
				lapg_gg.value = new ArrayList();
				((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 153:  // map_entries ::= map_entries ',' ID map_separator expression
				((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset));
				break;
			case 157:  // name ::= qualified_id
				lapg_gg.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 160:  // command ::= code
				lapg_gg.value = new TmaCommand(
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // syntax_problem ::= error
				lapg_gg.value = new TmaSyntaxProblem(
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol value) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(LapgSymbol value) {
	}

	public TmaInput parseInput(TMLexer lexer) throws IOException, ParseException {
		return (TmaInput) parse(lexer, 0, 268);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 269);
	}
}
