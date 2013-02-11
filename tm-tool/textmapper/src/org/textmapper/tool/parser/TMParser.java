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
import org.textmapper.lapg.api.LexerRule;
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
	private static final int[] lapg_action = TMLexer.unpack_int(269,
		"\uffff\uffff\uffff\uffff\242\0\ufffd\uffff\uffff\uffff\uffff\uffff\4\0\ufff5\uffff" +
		"\uffef\uffff\35\0\41\0\42\0\40\0\7\0\11\0\213\0\214\0\uffc9\uffff\215\0\216\0\uffff" +
		"\uffff\217\0\226\0\uffff\uffff\10\0\uff9d\uffff\uffff\uffff\67\0\5\0\uff95\uffff" +
		"\uffff\uffff\44\0\uffff\uffff\uff6f\uffff\uffff\uffff\uffff\uffff\uff5f\uffff\36" +
		"\0\uff51\uffff\74\0\77\0\100\0\uffff\uffff\uff2d\uffff\203\0\37\0\3\0\227\0\uff0f" +
		"\uffff\uffff\uffff\237\0\uffff\uffff\uff09\uffff\34\0\43\0\6\0\uffff\uffff\uffff" +
		"\uffff\66\0\2\0\22\0\uffff\uffff\24\0\25\0\20\0\21\0\uff03\uffff\16\0\17\0\23\0\26" +
		"\0\30\0\27\0\uffff\uffff\15\0\ufecf\uffff\uffff\uffff\uffff\uffff\113\0\114\0\115" +
		"\0\uffff\uffff\ufea7\uffff\207\0\uffff\uffff\uffff\uffff\ufe83\uffff\uffff\uffff" +
		"\ufe7b\uffff\75\0\76\0\ufe6f\uffff\204\0\uffff\uffff\225\0\ufe63\uffff\uffff\uffff" +
		"\71\0\72\0\70\0\12\0\ufe45\uffff\uffff\uffff\13\0\14\0\ufe11\uffff\ufde3\uffff\uffff" +
		"\uffff\120\0\125\0\uffff\uffff\uffff\uffff\127\0\ufddb\uffff\112\0\uffff\uffff\ufdd1" +
		"\uffff\uffff\uffff\uffff\uffff\ufda5\uffff\uffff\uffff\230\0\uffff\uffff\ufd9d\uffff" +
		"\uffff\uffff\240\0\33\0\uffff\uffff\46\0\ufd97\uffff\122\0\124\0\117\0\uffff\uffff" +
		"\116\0\126\0\205\0\uffff\uffff\ufd6b\uffff\uffff\uffff\ufd3f\uffff\uffff\uffff\ufcff" +
		"\uffff\uffff\uffff\241\0\uffff\uffff\171\0\uffff\uffff\ufcd3\uffff\132\0\ufccb\uffff" +
		"\134\0\ufc9f\uffff\ufc71\uffff\155\0\160\0\162\0\ufc3f\uffff\156\0\ufc0b\uffff\uffff" +
		"\uffff\uffff\uffff\ufbd1\uffff\ufbaf\uffff\157\0\142\0\141\0\ufb8f\uffff\uffff\uffff" +
		"\ufb63\uffff\uffff\uffff\234\0\235\0\233\0\uffff\uffff\uffff\uffff\222\0\60\0\50" +
		"\0\ufb37\uffff\121\0\130\0\uffff\uffff\ufb0d\uffff\uffff\uffff\151\0\uffff\uffff" +
		"\ufae1\uffff\211\0\uffff\uffff\uffff\uffff\147\0\uffff\uffff\110\0\ufab3\uffff\uffff" +
		"\uffff\ufa85\uffff\uffff\uffff\ufa57\uffff\137\0\ufa37\uffff\154\0\140\0\uffff\uffff" +
		"\165\0\175\0\176\0\uffff\uffff\uffff\uffff\161\0\143\0\ufa05\uffff\uffff\uffff\uf9e5" +
		"\uffff\uffff\uffff\uffff\uffff\uf9b9\uffff\231\0\uffff\uffff\uffff\uffff\52\0\uf98d" +
		"\uffff\106\0\uffff\uffff\150\0\172\0\uffff\uffff\uffff\uffff\210\0\163\0\uffff\uffff" +
		"\133\0\136\0\uf965\uffff\167\0\145\0\uffff\uffff\107\0\uffff\uffff\uf933\uffff\104" +
		"\0\uffff\uffff\uffff\uffff\62\0\63\0\64\0\65\0\uffff\uffff\54\0\56\0\105\0\uffff" +
		"\uffff\212\0\146\0\uffff\uffff\102\0\uffff\uffff\103\0\232\0\61\0\uffff\uffff\144" +
		"\0\101\0\174\0\173\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = TMLexer.unpack_short(1782,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\45\uffff\ufffe\1\uffff\2\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\45\uffff\44\uffff\10\uffff\21\uffff\34\uffff\0\1\uffff\ufffe\1\uffff\2\uffff\57" +
		"\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46" +
		"\uffff\45\uffff\44\uffff\4\uffff\5\uffff\21\uffff\35\uffff\36\uffff\37\uffff\22\224" +
		"\uffff\ufffe\14\uffff\17\73\22\73\uffff\ufffe\1\uffff\2\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\10\uffff\21\uffff\34\uffff\0\1\uffff\ufffe\13\uffff\11\10\20\10\23\10\42\10" +
		"\43\10\46\10\uffff\ufffe\23\uffff\42\uffff\46\uffff\11\45\20\45\43\45\uffff\ufffe" +
		"\1\uffff\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\34\uffff\0\0\uffff\ufffe\34\uffff" +
		"\2\202\44\202\45\202\46\202\47\202\50\202\51\202\52\202\53\202\54\202\55\202\56\202" +
		"\57\202\uffff\ufffe\17\uffff\22\223\uffff\ufffe\16\uffff\23\236\uffff\ufffe\2\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\16\uffff\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff" +
		"\27\uffff\30\uffff\32\uffff\33\uffff\34\uffff\25\32\uffff\ufffe\3\uffff\0\57\1\57" +
		"\2\57\10\57\21\57\34\57\44\57\45\57\46\57\47\57\50\57\51\57\52\57\53\57\54\57\55" +
		"\57\56\57\57\57\uffff\ufffe\13\uffff\2\206\20\206\23\206\34\206\44\206\45\206\46" +
		"\206\47\206\50\206\51\206\52\206\53\206\54\206\55\206\56\206\57\206\uffff\ufffe\23" +
		"\uffff\11\45\43\45\uffff\ufffe\23\uffff\42\uffff\46\uffff\11\45\43\45\uffff\ufffe" +
		"\23\uffff\42\uffff\46\uffff\11\45\43\45\uffff\ufffe\2\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\25\221\uffff\ufffe\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\16\uffff\17\uffff\21" +
		"\uffff\22\uffff\23\uffff\26\uffff\27\uffff\30\uffff\32\uffff\33\uffff\34\uffff\25" +
		"\31\uffff\ufffe\14\uffff\0\47\1\47\2\47\5\47\10\47\21\47\23\47\34\47\44\47\45\47" +
		"\46\47\47\47\50\47\51\47\52\47\53\47\54\47\55\47\56\47\57\47\61\47\uffff\ufffe\53" +
		"\uffff\15\123\17\123\uffff\ufffe\17\uffff\11\111\23\111\43\111\uffff\ufffe\1\uffff" +
		"\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff" +
		"\12\135\15\135\uffff\ufffe\23\uffff\11\45\43\45\uffff\ufffe\17\uffff\25\220\uffff" +
		"\ufffe\5\uffff\0\51\1\51\2\51\10\51\21\51\23\51\34\51\44\51\45\51\46\51\47\51\50" +
		"\51\51\51\52\51\53\51\54\51\55\51\56\51\57\51\61\51\uffff\ufffe\1\uffff\2\uffff\57" +
		"\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46" +
		"\uffff\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff\12\135\15\135" +
		"\uffff\ufffe\13\10\20\10\42\10\1\11\2\11\10\11\12\11\15\11\23\11\24\11\25\11\30\11" +
		"\31\11\32\11\33\11\34\11\40\11\41\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53" +
		"\11\54\11\55\11\56\11\57\11\61\11\uffff\ufffe\1\uffff\2\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff\12\135\25\135\uffff\ufffe\12" +
		"\uffff\15\131\25\131\uffff\ufffe\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\61\uffff\12\135\15\135\25\135\uffff\ufffe\1\uffff\2\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff\12\135\15\135\25" +
		"\135\uffff\ufffe\33\uffff\1\152\2\152\10\152\12\152\15\152\23\152\24\152\25\152\34" +
		"\152\40\152\44\152\45\152\46\152\47\152\50\152\51\152\52\152\53\152\54\152\55\152" +
		"\56\152\57\152\61\152\uffff\ufffe\32\uffff\1\164\2\164\10\164\12\164\15\164\23\164" +
		"\24\164\25\164\33\164\34\164\40\164\44\164\45\164\46\164\47\164\50\164\51\164\52" +
		"\164\53\164\54\164\55\164\56\164\57\164\61\164\uffff\ufffe\30\uffff\31\uffff\41\uffff" +
		"\1\166\2\166\10\166\12\166\15\166\23\166\24\166\25\166\32\166\33\166\34\166\40\166" +
		"\44\166\45\166\46\166\47\166\50\166\51\166\52\166\53\166\54\166\55\166\56\166\57" +
		"\166\61\166\uffff\ufffe\34\uffff\2\177\23\177\44\177\45\177\46\177\47\177\50\177" +
		"\51\177\52\177\53\177\54\177\55\177\56\177\57\177\20\202\uffff\ufffe\34\uffff\2\201" +
		"\23\201\44\201\45\201\46\201\47\201\50\201\51\201\52\201\53\201\54\201\55\201\56" +
		"\201\57\201\uffff\ufffe\1\uffff\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\61\uffff\12\135\15\135\uffff\ufffe\1\uffff\2\uffff\57\uffff\56" +
		"\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45" +
		"\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff\12\135\15\135\uffff" +
		"\ufffe\23\uffff\0\53\1\53\2\53\10\53\21\53\34\53\44\53\45\53\46\53\47\53\50\53\51" +
		"\53\52\53\53\53\54\53\55\53\56\53\57\53\61\53\uffff\ufffe\1\uffff\2\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff\12\135\15\135\uffff" +
		"\ufffe\1\uffff\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff" +
		"\40\uffff\61\uffff\12\135\25\135\uffff\ufffe\1\uffff\2\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff\12\135\15\135\25\135\uffff\ufffe" +
		"\1\uffff\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61" +
		"\uffff\12\135\15\135\25\135\uffff\ufffe\34\uffff\2\177\23\177\44\177\45\177\46\177" +
		"\47\177\50\177\51\177\52\177\53\177\54\177\55\177\56\177\57\177\uffff\ufffe\33\uffff" +
		"\1\153\2\153\10\153\12\153\15\153\23\153\24\153\25\153\34\153\40\153\44\153\45\153" +
		"\46\153\47\153\50\153\51\153\52\153\53\153\54\153\55\153\56\153\57\153\61\153\uffff" +
		"\ufffe\34\uffff\2\200\23\200\44\200\45\200\46\200\47\200\50\200\51\200\52\200\53" +
		"\200\54\200\55\200\56\200\57\200\uffff\ufffe\1\uffff\2\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff\12\135\15\135\uffff\ufffe\1\uffff" +
		"\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff\61\uffff" +
		"\12\135\15\135\uffff\ufffe\61\uffff\0\55\1\55\2\55\10\55\21\55\34\55\44\55\45\55" +
		"\46\55\47\55\50\55\51\55\52\55\53\55\54\55\55\55\56\55\57\55\uffff\ufffe\33\170\1" +
		"\170\2\170\10\170\12\170\15\170\23\170\24\170\25\170\34\170\40\170\44\170\45\170" +
		"\46\170\47\170\50\170\51\170\52\170\53\170\54\170\55\170\56\170\57\170\61\170\uffff" +
		"\ufffe\1\uffff\2\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\10\uffff\23\uffff\24\uffff\34\uffff" +
		"\61\uffff\12\135\15\135\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(119,
		"\0\2\34\127\132\142\152\152\152\173\204\206\216\222\234\241\254\265\304\312\350\367" +
		"\377\u0103\u0107\u010d\u010f\u0114\u011b\u0136\u013d\u0144\u014b\u014c\u014d\u0152" +
		"\u0157\u0193\u01cf\u020d\u0249\u0285\u02c1\u02fd\u0339\u0375\u03b1\u03ed\u0429\u0429" +
		"\u0439\u043a\u043b\u043d\u0456\u047e\u0484\u0486\u048a\u048d\u048f\u0493\u0497\u049b" +
		"\u049c\u049d\u049e\u04a2\u04a3\u04a5\u04a7\u04a9\u04ac\u04af\u04b2\u04b3\u04b6\u04b7" +
		"\u04b9\u04bb\u04be\u04c7\u04d0\u04da\u04e4\u04f2\u04fd\u050c\u051b\u052c\u053e\u0550" +
		"\u055f\u0571\u0580\u058d\u05a0\u05b7\u05c6\u05c7\u05ce\u05cf\u05d0\u05d2\u05d3\u05d4" +
		"\u05e4\u05fe\u0600\u0601\u0607\u0608\u0609\u060a\u060b\u060c\u061a\u061b\u061c");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1564,
		"\u0109\u010a\0\1\5\10\21\27\35\43\46\135\157\164\212\216\230\247\251\256\267\273" +
		"\302\304\325\330\357\362\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121" +
		"\124\125\135\137\140\145\156\157\164\177\205\211\212\216\217\226\230\240\247\251" +
		"\256\257\267\270\273\276\300\302\304\305\313\317\325\330\342\343\354\357\362\373" +
		"\27\113\114\1\21\27\36\135\157\256\362\1\21\27\135\157\201\256\362\10\35\46\164\212" +
		"\216\226\230\247\251\267\273\302\304\325\330\357\127\163\165\166\170\213\250\252" +
		"\326\224\275\3\41\122\172\221\303\320\332\31\151\172\332\153\156\223\266\324\327" +
		"\337\356\361\u0100\36\64\102\111\145\32\36\60\102\111\145\153\161\173\346\376\40" +
		"\127\172\221\241\320\332\346\376\0\1\5\10\21\27\35\36\102\111\135\145\157\256\362" +
		"\32\36\61\102\111\145\7\36\44\63\102\111\126\130\133\145\164\167\212\216\226\230" +
		"\240\247\251\263\267\273\276\302\304\305\313\325\330\357\164\212\216\226\230\247" +
		"\251\267\273\302\304\313\325\330\357\75\111\146\174\272\275\367\373\36\102\111\145" +
		"\36\102\111\145\36\102\111\145\237\u0104\237\u0104\36\102\111\145\235\36\102\111" +
		"\145\231\310\351\10\35\36\46\53\102\111\145\164\212\216\226\230\242\243\247\251\267" +
		"\273\302\304\306\313\323\325\330\357\1\21\27\135\157\256\362\1\21\27\135\157\256" +
		"\362\1\21\27\135\157\256\362\273\237\44\130\133\221\320\127\163\166\170\250\0\1\4" +
		"\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\124\125\135\137\140\145\156\157" +
		"\164\177\205\211\212\215\216\217\226\230\240\247\251\256\257\267\270\273\276\300" +
		"\302\304\305\313\317\325\330\342\343\354\357\362\373\0\1\4\5\10\21\24\27\35\36\43" +
		"\46\52\70\71\102\111\115\121\124\125\135\137\140\145\156\157\164\177\205\211\212" +
		"\215\216\217\226\230\240\247\251\256\257\267\270\273\276\300\302\304\305\313\317" +
		"\325\330\342\343\354\357\362\373\0\1\4\5\10\21\24\27\35\36\43\44\46\52\70\71\102" +
		"\111\115\121\124\125\130\133\135\137\140\145\156\157\164\177\205\211\212\216\217" +
		"\226\230\240\247\251\256\257\267\270\273\276\300\302\304\305\313\317\325\330\342" +
		"\343\354\357\362\373\0\1\4\5\10\21\24\27\35\36\42\43\46\52\70\71\102\111\115\121" +
		"\124\125\135\137\140\145\156\157\164\177\205\211\212\216\217\226\230\240\247\251" +
		"\256\257\267\270\273\276\300\302\304\305\313\317\325\330\342\343\354\357\362\373" +
		"\0\1\4\5\10\21\24\27\35\36\42\43\46\52\70\71\102\111\115\121\124\125\135\137\140" +
		"\145\156\157\164\177\205\211\212\216\217\226\230\240\247\251\256\257\267\270\273" +
		"\276\300\302\304\305\313\317\325\330\342\343\354\357\362\373\0\1\4\5\10\21\24\27" +
		"\35\36\42\43\46\52\70\71\102\111\115\121\124\125\135\137\140\145\156\157\164\177" +
		"\205\211\212\216\217\226\230\240\247\251\256\257\267\270\273\276\300\302\304\305" +
		"\313\317\325\330\342\343\354\357\362\373\0\1\4\5\10\21\24\27\35\36\42\43\46\52\70" +
		"\71\102\111\115\121\124\125\135\137\140\145\156\157\164\177\205\211\212\216\217\226" +
		"\230\240\247\251\256\257\267\270\273\276\300\302\304\305\313\317\325\330\342\343" +
		"\354\357\362\373\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\124\125" +
		"\135\137\140\145\152\156\157\164\177\205\211\212\216\217\226\230\240\247\251\256" +
		"\257\267\270\273\276\300\302\304\305\313\317\325\330\342\343\354\357\362\373\0\1" +
		"\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\124\125\135\137\140\145\156" +
		"\157\164\177\205\211\212\216\217\226\230\240\247\251\256\257\267\270\273\276\300" +
		"\302\304\305\313\317\325\330\333\342\343\354\357\362\373\0\1\4\5\10\21\24\27\35\36" +
		"\43\46\52\70\71\102\111\115\121\124\125\135\137\140\145\156\157\164\177\205\211\212" +
		"\216\217\226\230\240\247\251\256\257\267\270\273\276\300\302\304\305\313\317\325" +
		"\330\333\342\343\354\357\362\373\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111" +
		"\115\121\124\125\135\137\140\145\156\157\164\177\205\211\212\216\217\226\230\240" +
		"\247\251\256\257\267\270\273\276\300\302\304\305\313\317\325\330\333\342\343\354" +
		"\357\362\373\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\124\125\135" +
		"\137\140\145\156\157\164\177\205\211\212\216\217\226\230\240\247\251\256\257\267" +
		"\270\273\276\300\302\304\305\313\317\325\330\333\342\343\354\357\362\373\164\212" +
		"\216\226\230\247\251\267\273\302\304\313\325\330\335\357\0\0\0\5\0\4\5\10\35\46\52" +
		"\71\164\212\216\226\230\240\247\251\267\273\302\304\305\313\325\330\357\1\21\27\115" +
		"\121\124\125\135\156\157\164\205\211\212\216\217\226\230\240\247\251\256\267\270" +
		"\273\276\300\302\304\305\313\317\325\330\342\343\354\357\362\373\7\44\126\130\133" +
		"\167\36\102\36\102\111\145\27\113\114\0\5\0\5\10\35\0\5\10\35\0\5\10\35\151\263\333" +
		"\0\5\10\35\4\70\177\4\71\10\35\10\35\46\10\35\46\44\130\133\42\10\35\46\115\115\205" +
		"\121\342\124\300\354\164\212\216\247\251\267\325\330\357\164\212\216\247\251\267" +
		"\325\330\357\164\212\216\247\251\267\302\325\330\357\164\212\216\247\251\267\302" +
		"\325\330\357\164\212\216\226\230\247\251\267\273\302\304\325\330\357\164\212\216" +
		"\226\247\251\267\302\325\330\357\164\212\216\226\230\247\251\267\273\302\304\313" +
		"\325\330\357\164\212\216\226\230\247\251\267\273\302\304\313\325\330\357\164\212" +
		"\216\226\230\240\247\251\267\273\302\304\305\313\325\330\357\164\212\216\226\230" +
		"\240\247\251\267\273\276\302\304\305\313\325\330\357\164\212\216\226\230\240\247" +
		"\251\267\273\276\302\304\305\313\325\330\357\164\212\216\226\230\247\251\267\273" +
		"\302\304\313\325\330\357\164\212\216\226\230\240\247\251\267\273\276\302\304\305" +
		"\313\325\330\357\164\212\216\226\230\247\251\267\273\302\304\313\325\330\357\10\35" +
		"\46\164\212\216\247\251\267\302\325\330\357\10\35\46\164\212\216\226\230\243\247" +
		"\251\267\273\302\304\313\325\330\357\10\35\46\53\164\212\216\226\230\242\243\247" +
		"\251\267\273\302\304\306\313\323\325\330\357\164\212\216\226\230\247\251\267\273" +
		"\302\304\313\325\330\357\217\1\21\27\135\157\256\362\21\137\172\332\24\24\164\212" +
		"\216\226\230\247\251\267\273\302\304\313\325\330\335\357\0\1\5\10\21\27\35\43\46" +
		"\135\157\164\212\216\230\247\251\256\267\273\302\304\325\330\357\362\10\35\102\7" +
		"\44\126\130\133\167\151\201\263\335\152\164\212\216\226\230\247\251\267\273\302\304" +
		"\325\330\357\137\21");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1564,
		"\u010b\u010c\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\3\16\30\3\41\16" +
		"\62\16\41\74\122\30\30\141\30\74\74\16\16\16\16\16\172\175\74\16\16\214\141\16\16" +
		"\214\214\16\214\214\214\214\214\16\332\214\16\214\16\16\214\214\214\214\16\214\214" +
		"\16\16\16\214\16\16\65\65\65\17\17\17\75\17\17\17\17\20\20\20\20\20\262\20\20\42" +
		"\42\42\215\215\215\215\215\215\215\215\215\215\215\215\215\215\164\212\247\164\251" +
		"\267\325\330\357\302\343\27\114\157\253\276\276\276\253\70\177\254\254\204\206\301" +
		"\336\355\360\372\377\u0101\u0106\76\140\76\76\76\71\77\135\77\77\77\205\211\257\211" +
		"\211\113\113\255\277\322\353\255\375\u0105\4\21\4\4\21\21\4\100\100\100\21\100\21" +
		"\21\21\72\101\136\101\101\101\36\102\36\137\102\102\36\36\36\102\216\36\216\216\216" +
		"\216\216\216\216\333\216\216\216\216\216\216\216\216\216\216\217\217\217\217\217" +
		"\217\217\217\217\217\217\217\217\217\217\144\147\176\260\341\344\u0103\u0104\103" +
		"\103\103\103\104\104\104\104\105\105\105\105\315\u0107\316\u0108\106\106\106\106" +
		"\314\107\107\107\107\313\313\313\43\43\110\43\43\110\110\110\43\43\43\43\43\43\43" +
		"\43\43\43\43\43\43\43\43\43\43\43\43\22\22\22\22\22\22\22\23\23\23\23\23\23\23\24" +
		"\24\24\24\24\24\24\342\317\124\124\124\300\354\165\213\165\252\326\3\16\30\3\41\16" +
		"\62\16\41\74\122\30\30\141\30\74\74\16\16\16\16\16\172\175\74\16\16\214\141\16\16" +
		"\214\270\214\16\214\214\214\214\214\16\332\214\16\214\16\16\214\214\214\214\16\214" +
		"\214\16\16\16\214\16\16\3\16\30\3\41\16\62\16\41\74\122\30\30\141\30\74\74\16\16" +
		"\16\16\16\172\175\74\16\16\214\141\16\16\214\271\214\16\214\214\214\214\214\16\332" +
		"\214\16\214\16\16\214\214\214\214\16\214\214\16\16\16\214\16\16\3\16\30\3\41\16\62" +
		"\16\41\74\122\125\30\30\141\30\74\74\16\16\16\16\125\125\16\172\175\74\16\16\214" +
		"\141\16\16\214\214\16\214\214\214\214\214\16\332\214\16\214\16\16\214\214\214\214" +
		"\16\214\214\16\16\16\214\16\16\3\16\30\3\41\16\62\16\41\74\115\122\30\30\141\30\74" +
		"\74\16\16\16\16\16\172\175\74\16\16\214\141\16\16\214\214\16\214\214\214\214\214" +
		"\16\332\214\16\214\16\16\214\214\214\214\16\214\214\16\16\16\214\16\16\3\16\30\3" +
		"\41\16\62\16\41\74\116\122\30\30\141\30\74\74\16\16\16\16\16\172\175\74\16\16\214" +
		"\141\16\16\214\214\16\214\214\214\214\214\16\332\214\16\214\16\16\214\214\214\214" +
		"\16\214\214\16\16\16\214\16\16\3\16\30\3\41\16\62\16\41\74\117\122\30\30\141\30\74" +
		"\74\16\16\16\16\16\172\175\74\16\16\214\141\16\16\214\214\16\214\214\214\214\214" +
		"\16\332\214\16\214\16\16\214\214\214\214\16\214\214\16\16\16\214\16\16\3\16\30\3" +
		"\41\16\62\16\41\74\120\122\30\30\141\30\74\74\16\16\16\16\16\172\175\74\16\16\214" +
		"\141\16\16\214\214\16\214\214\214\214\214\16\332\214\16\214\16\16\214\214\214\214" +
		"\16\214\214\16\16\16\214\16\16\3\16\30\3\41\16\62\16\41\74\122\30\30\141\30\74\74" +
		"\16\16\16\16\16\172\175\74\202\16\16\214\141\16\16\214\214\16\214\214\214\214\214" +
		"\16\332\214\16\214\16\16\214\214\214\214\16\214\214\16\16\16\214\16\16\3\16\30\3" +
		"\41\16\62\16\41\74\122\30\30\141\30\74\74\16\16\16\16\16\172\175\74\16\16\214\141" +
		"\16\16\214\214\16\214\214\214\214\214\16\332\214\16\214\16\16\214\214\214\214\16" +
		"\214\214\363\16\16\16\214\16\16\3\16\30\3\41\16\62\16\41\74\122\30\30\141\30\74\74" +
		"\16\16\16\16\16\172\175\74\16\16\214\141\16\16\214\214\16\214\214\214\214\214\16" +
		"\332\214\16\214\16\16\214\214\214\214\16\214\214\364\16\16\16\214\16\16\3\16\30\3" +
		"\41\16\62\16\41\74\122\30\30\141\30\74\74\16\16\16\16\16\172\175\74\16\16\214\141" +
		"\16\16\214\214\16\214\214\214\214\214\16\332\214\16\214\16\16\214\214\214\214\16" +
		"\214\214\365\16\16\16\214\16\16\3\16\30\3\41\16\62\16\41\74\122\30\30\141\30\74\74" +
		"\16\16\16\16\16\172\175\74\16\16\214\141\16\16\214\214\16\214\214\214\214\214\16" +
		"\332\214\16\214\16\16\214\214\214\214\16\214\214\366\16\16\16\214\16\16\220\220\220" +
		"\220\220\220\220\220\220\220\220\220\220\220\220\220\u0109\5\6\34\7\31\7\44\44\130" +
		"\133\31\221\221\221\303\303\320\221\221\221\303\221\303\303\303\221\221\221\25\25" +
		"\25\152\155\160\162\25\207\25\222\152\265\222\222\274\222\222\222\222\222\25\222" +
		"\340\222\222\160\222\222\222\222\352\222\222\155\374\160\222\25\207\37\37\37\37\37" +
		"\37\111\145\112\112\150\150\66\151\66\10\35\11\11\45\45\12\12\12\12\13\13\13\13\200" +
		"\334\367\14\14\14\14\32\142\261\33\143\46\46\47\47\131\50\50\50\126\126\167\121\51" +
		"\51\51\153\154\264\156\373\161\346\376\223\266\272\324\327\337\356\361\u0100\224" +
		"\224\224\224\224\224\224\224\224\225\225\225\225\225\225\347\225\225\225\226\226" +
		"\226\226\226\226\226\226\226\226\227\227\227\227\227\227\227\227\227\227\227\227" +
		"\227\227\230\230\273\304\230\230\230\230\230\230\230\231\231\231\231\310\231\231" +
		"\231\310\231\310\351\231\231\231\232\232\232\232\232\232\232\232\232\232\232\232" +
		"\232\232\232\233\233\233\233\233\321\233\233\233\233\233\233\321\233\233\233\233" +
		"\234\234\234\234\234\234\234\234\234\234\345\234\234\234\234\234\234\234\235\235" +
		"\235\235\235\235\235\235\235\235\235\235\235\235\235\235\235\235\236\236\236\236" +
		"\236\236\236\236\236\236\236\236\236\236\236\237\237\237\237\237\237\237\237\237" +
		"\237\237\237\237\237\237\237\237\237\240\240\240\305\305\240\240\240\305\240\305" +
		"\305\240\240\240\52\52\52\241\241\241\241\241\241\241\241\241\241\53\53\53\242\242" +
		"\242\306\306\323\242\242\242\306\242\306\306\242\242\242\54\54\54\134\54\54\54\54" +
		"\54\134\54\54\54\54\54\54\54\134\54\134\54\54\54\243\243\243\243\243\243\243\243" +
		"\243\243\243\243\243\243\243\275\u010a\57\67\171\210\331\u0102\60\173\256\362\63" +
		"\64\244\244\244\244\244\244\244\244\244\244\244\244\244\244\370\244\15\26\15\55\26" +
		"\26\55\123\132\26\26\245\245\245\311\245\245\26\245\311\245\311\245\245\245\26\56" +
		"\73\146\40\127\163\166\170\250\201\263\335\371\203\246\246\246\307\312\246\246\246" +
		"\312\246\350\246\246\246\174\61");

	private static final short[] lapg_rlen = TMLexer.unpack_short(163,
		"\1\0\3\2\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\1\0\3\1\1\2\2\1\1\1\3\1\0\1\0" +
		"\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\10\7\7\6\7\6\6\5\2\2\1\1\1\4" +
		"\4\1\3\1\0\2\1\2\1\3\1\1\3\1\0\3\2\2\1\1\2\5\3\4\2\3\2\1\2\2\1\1\1\1\2\1\3\1\2\1" +
		"\3\3\1\3\6\6\2\2\1\2\1\1\1\2\4\2\2\3\1\3\1\1\1\1\1\1\0\5\1\0\3\1\1\3\3\5\1\1\1\1" +
		"\1\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(163,
		"\153\153\62\62\63\63\64\64\65\66\67\67\70\70\71\71\71\71\71\71\71\71\71\71\71\154" +
		"\154\71\72\73\73\73\74\74\74\75\155\155\156\156\157\157\160\160\161\161\76\76\77" +
		"\100\101\101\101\101\102\103\103\104\105\105\106\106\106\107\107\110\110\110\110" +
		"\110\110\110\110\111\111\112\112\112\113\113\114\114\162\162\115\116\116\117\117" +
		"\120\121\121\163\163\122\122\122\122\122\123\123\123\123\123\124\124\125\125\125" +
		"\126\126\126\127\127\130\130\131\131\132\132\133\134\134\134\134\134\134\135\135" +
		"\135\136\137\137\140\140\140\141\142\142\143\143\143\143\143\164\164\143\165\165" +
		"\143\143\144\144\145\145\146\146\146\147\150\150\151\152");

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
		"nonterm",
		"nonterm_ast",
		"priority_kw",
		"directive",
		"inputs",
		"inputref",
		"references",
		"references_cs",
		"rules",
		"rule_list",
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
		"negative_la_clause",
		"expression",
		"expression_list",
		"map_entries",
		"map_separator",
		"name",
		"qualified_id",
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
		"rhsSuffixopt",
		"map_entriesopt",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 50;
		public static final int options = 51;
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
		public static final int state_list = 67;
		public static final int stateref = 68;
		public static final int lexer_state = 69;
		public static final int grammar_parts = 70;
		public static final int grammar_part = 71;
		public static final int nonterm = 72;
		public static final int nonterm_ast = 73;
		public static final int priority_kw = 74;
		public static final int directive = 75;
		public static final int inputs = 76;
		public static final int inputref = 77;
		public static final int references = 78;
		public static final int references_cs = 79;
		public static final int rules = 80;
		public static final int rule_list = 81;
		public static final int rule0 = 82;
		public static final int rhsPrefix = 83;
		public static final int rhsSuffix = 84;
		public static final int rhsParts = 85;
		public static final int rhsPart = 86;
		public static final int rhsAnnotated = 87;
		public static final int rhsAssignment = 88;
		public static final int rhsOptional = 89;
		public static final int rhsCast = 90;
		public static final int rhsUnordered = 91;
		public static final int rhsPrimary = 92;
		public static final int rhsAnnotations = 93;
		public static final int annotations = 94;
		public static final int annotation_list = 95;
		public static final int annotation = 96;
		public static final int negative_la = 97;
		public static final int negative_la_clause = 98;
		public static final int expression = 99;
		public static final int expression_list = 100;
		public static final int map_entries = 101;
		public static final int map_separator = 102;
		public static final int name = 103;
		public static final int qualified_id = 104;
		public static final int command = 105;
		public static final int syntax_problem = 106;
		public static final int grammar_partsopt = 107;
		public static final int type_part_listopt = 108;
		public static final int typeopt = 109;
		public static final int lexem_transitionopt = 110;
		public static final int iconopt = 111;
		public static final int lexem_attrsopt = 112;
		public static final int commandopt = 113;
		public static final int Lnoeoiopt = 114;
		public static final int rhsSuffixopt = 115;
		public static final int map_entriesopt = 116;
		public static final int expression_listopt = 117;
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.symbol) {
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
	protected TMLexer lapg_lexer;

	private Object parse(TMLexer lexer, int initialState, int finalState) throws IOException, ParseException {

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
		return lapg_m[lapg_head - 1].value;
	}

	protected boolean restore() {
		if (lapg_n.symbol == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 1) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].symbol = 1;
			lapg_m[lapg_head].value = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 1);
			lapg_m[lapg_head].line = lapg_n.line;
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endoffset = lapg_n.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.symbol], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.symbol != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.value = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].value : null;
		lapg_gg.symbol = lapg_rlex[rule];
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
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 2:  // input ::= options lexer_parts grammar_partsopt
				  lapg_gg.value = new AstRoot(((List<AstOptionPart>)lapg_m[lapg_head - 2].value), ((List<AstLexerPart>)lapg_m[lapg_head - 1].value), ((List<AstGrammarPart>)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // input ::= lexer_parts grammar_partsopt
				  lapg_gg.value = new AstRoot(((List<AstOptionPart>)null), ((List<AstLexerPart>)lapg_m[lapg_head - 1].value), ((List<AstGrammarPart>)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 4:  // options ::= option
				 lapg_gg.value = new ArrayList<AstOptionPart>(16); ((List<AstOptionPart>)lapg_gg.value).add(((AstOptionPart)lapg_m[lapg_head].value)); 
				break;
			case 5:  // options ::= options option
				 ((List<AstOptionPart>)lapg_m[lapg_head - 1].value).add(((AstOptionPart)lapg_m[lapg_head].value)); 
				break;
			case 6:  // option ::= ID '=' expression
				 lapg_gg.value = new AstOption(((String)lapg_m[lapg_head - 2].value), ((AstExpression)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // identifier ::= ID
				 lapg_gg.value = new AstIdentifier(((String)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // symref ::= ID
				 lapg_gg.value = new AstReference(((String)lapg_m[lapg_head].value), AstReference.DEFAULT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // type ::= '(' scon ')'
				 lapg_gg.value = ((String)lapg_m[lapg_head - 1].value); 
				break;
			case 11:  // type ::= '(' type_part_list ')'
				 lapg_gg.value = source.getText(lapg_m[lapg_head - 2].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 28:  // pattern ::= regexp
				 lapg_gg.value = new AstRegexp(((String)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 29:  // lexer_parts ::= lexer_part
				 lapg_gg.value = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.value).add(((AstLexerPart)lapg_m[lapg_head].value)); 
				break;
			case 30:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head - 1].value).add(((AstLexerPart)lapg_m[lapg_head].value)); 
				break;
			case 31:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<AstLexerPart>)lapg_m[lapg_head - 1].value).add(((AstError)lapg_m[lapg_head].value)); 
				break;
			case 35:  // named_pattern ::= ID '=' pattern
				 lapg_gg.value = new AstNamedPattern(((String)lapg_m[lapg_head - 2].value), ((AstRegexp)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				 lapg_gg.value = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 7].value), ((String)lapg_m[lapg_head - 6].value), ((AstRegexp)lapg_m[lapg_head - 4].value), ((AstReference)lapg_m[lapg_head - 3].value), ((Integer)lapg_m[lapg_head - 2].value), ((AstLexemAttrs)lapg_m[lapg_head - 1].value), ((AstCode)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // lexeme ::= identifier typeopt ':'
				 lapg_gg.value = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 2].value), ((String)lapg_m[lapg_head - 1].value), ((AstRegexp)null), ((AstReference)null), ((Integer)null), ((AstLexemAttrs)null), ((AstCode)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // lexem_transition ::= '=>' stateref
				 lapg_gg.value = ((AstReference)lapg_m[lapg_head].value); 
				break;
			case 49:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.value = ((AstLexemAttrs)lapg_m[lapg_head - 1].value); 
				break;
			case 50:  // lexem_attribute ::= Lsoft
				 lapg_gg.value = new AstLexemAttrs(LexerRule.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 51:  // lexem_attribute ::= Lclass
				 lapg_gg.value = new AstLexemAttrs(LexerRule.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 52:  // lexem_attribute ::= Lspace
				 lapg_gg.value = new AstLexemAttrs(LexerRule.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 53:  // lexem_attribute ::= Llayout
				 lapg_gg.value = new AstLexemAttrs(LexerRule.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // state_selector ::= '[' state_list ']'
				 lapg_gg.value = new AstStateSelector(((List<AstLexerState>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // state_list ::= lexer_state
				 lapg_gg.value = new ArrayList<Integer>(4); ((List<AstLexerState>)lapg_gg.value).add(((AstLexerState)lapg_m[lapg_head].value)); 
				break;
			case 56:  // state_list ::= state_list ',' lexer_state
				 ((List<AstLexerState>)lapg_m[lapg_head - 2].value).add(((AstLexerState)lapg_m[lapg_head].value)); 
				break;
			case 57:  // stateref ::= ID
				 lapg_gg.value = new AstReference(((String)lapg_m[lapg_head].value), AstReference.STATE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // lexer_state ::= identifier '=>' stateref
				 lapg_gg.value = new AstLexerState(((AstIdentifier)lapg_m[lapg_head - 2].value), ((AstReference)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 59:  // lexer_state ::= identifier
				 lapg_gg.value = new AstLexerState(((AstIdentifier)lapg_m[lapg_head].value), ((AstReference)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 60:  // grammar_parts ::= grammar_part
				 lapg_gg.value = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.value).add(((AstGrammarPart)lapg_m[lapg_head].value)); 
				break;
			case 61:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].value).add(((AstGrammarPart)lapg_m[lapg_head].value)); 
				break;
			case 62:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].value).add(((AstError)lapg_m[lapg_head].value)); 
				break;
			case 65:  // nonterm ::= annotations identifier nonterm_ast typeopt Linline '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 6].value), ((String)lapg_m[lapg_head - 4].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)lapg_m[lapg_head - 7].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // nonterm ::= annotations identifier nonterm_ast typeopt '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 5].value), ((String)lapg_m[lapg_head - 3].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)lapg_m[lapg_head - 6].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // nonterm ::= annotations identifier typeopt Linline '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 5].value), ((String)lapg_m[lapg_head - 4].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)lapg_m[lapg_head - 6].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // nonterm ::= annotations identifier typeopt '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].value), ((String)lapg_m[lapg_head - 3].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)lapg_m[lapg_head - 5].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // nonterm ::= identifier nonterm_ast typeopt Linline '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 6].value), ((String)lapg_m[lapg_head - 4].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // nonterm ::= identifier nonterm_ast typeopt '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 5].value), ((String)lapg_m[lapg_head - 3].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // nonterm ::= identifier typeopt Linline '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 5].value), ((String)lapg_m[lapg_head - 4].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // nonterm ::= identifier typeopt '::=' rules ';'
				 lapg_gg.value = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].value), ((String)lapg_m[lapg_head - 3].value), ((List<AstRule>)lapg_m[lapg_head - 1].value), ((AstAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.value = new AstDirective(((String)lapg_m[lapg_head - 2].value), ((List<AstReference>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.value = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // inputs ::= inputref
				 lapg_gg.value = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.value).add(((AstInputRef)lapg_m[lapg_head].value)); 
				break;
			case 81:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head - 2].value).add(((AstInputRef)lapg_m[lapg_head].value)); 
				break;
			case 84:  // inputref ::= symref Lnoeoiopt
				 lapg_gg.value = new AstInputRef(((AstReference)lapg_m[lapg_head - 1].value), ((String)lapg_m[lapg_head].value) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // references ::= symref
				 lapg_gg.value = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.value).add(((AstReference)lapg_m[lapg_head].value)); 
				break;
			case 86:  // references ::= references symref
				 ((List<AstReference>)lapg_m[lapg_head - 1].value).add(((AstReference)lapg_m[lapg_head].value)); 
				break;
			case 87:  // references_cs ::= symref
				 lapg_gg.value = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.value).add(((AstReference)lapg_m[lapg_head].value)); 
				break;
			case 88:  // references_cs ::= references_cs ',' symref
				 ((List<AstReference>)lapg_m[lapg_head - 2].value).add(((AstReference)lapg_m[lapg_head].value)); 
				break;
			case 90:  // rule_list ::= rule0
				 lapg_gg.value = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.value).add(((AstRule)lapg_m[lapg_head].value)); 
				break;
			case 91:  // rule_list ::= rule_list '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head - 2].value).add(((AstRule)lapg_m[lapg_head].value)); 
				break;
			case 94:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				 lapg_gg.value = new AstRule(((TmaRhsPrefix)lapg_m[lapg_head - 2].value), ((List<TmaRhsPart>)lapg_m[lapg_head - 1].value), ((TmaRhsSuffix)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // rule0 ::= rhsPrefix rhsSuffixopt
				 lapg_gg.value = new AstRule(((TmaRhsPrefix)lapg_m[lapg_head - 1].value), ((List<TmaRhsPart>)null), ((TmaRhsSuffix)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // rule0 ::= rhsParts rhsSuffixopt
				 lapg_gg.value = new AstRule(((TmaRhsPrefix)null), ((List<TmaRhsPart>)lapg_m[lapg_head - 1].value), ((TmaRhsSuffix)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // rule0 ::= rhsSuffixopt
				 lapg_gg.value = new AstRule(((TmaRhsPrefix)null), ((List<TmaRhsPart>)null), ((TmaRhsSuffix)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // rule0 ::= syntax_problem
				 lapg_gg.value = new AstRule(((AstError)lapg_m[lapg_head].value)); 
				break;
			case 99:  // rhsPrefix ::= annotations ':'
				 lapg_gg.value = new TmaRhsPrefix(((AstAnnotations)lapg_m[lapg_head - 1].value), null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // rhsPrefix ::= rhsAnnotations identifier Lextends references_cs ':'
				 lapg_gg.value = new TmaRhsPrefix(((AstRuleAnnotations)lapg_m[lapg_head - 4].value), ((AstIdentifier)lapg_m[lapg_head - 3].value), ((List<AstReference>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // rhsPrefix ::= rhsAnnotations identifier ':'
				 lapg_gg.value = new TmaRhsPrefix(((AstRuleAnnotations)lapg_m[lapg_head - 2].value), ((AstIdentifier)lapg_m[lapg_head - 1].value), ((List<AstReference>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // rhsPrefix ::= identifier Lextends references_cs ':'
				 lapg_gg.value = new TmaRhsPrefix(((AstRuleAnnotations)null), ((AstIdentifier)lapg_m[lapg_head - 3].value), ((List<AstReference>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // rhsPrefix ::= identifier ':'
				 lapg_gg.value = new TmaRhsPrefix(((AstRuleAnnotations)null), ((AstIdentifier)lapg_m[lapg_head - 1].value), ((List<AstReference>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // rhsSuffix ::= '%' Lprio symref
				 lapg_gg.value = new TmaRhsPrio(((AstReference)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // rhsSuffix ::= '%' Lshift
				 lapg_gg.value = new TmaRhsShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // rhsParts ::= rhsPart
				 lapg_gg.value = new ArrayList<TmaRhsPart>(); ((List<TmaRhsPart>)lapg_gg.value).add(((TmaRhsPart)lapg_m[lapg_head].value)); 
				break;
			case 107:  // rhsParts ::= rhsParts rhsPart
				 ((List<TmaRhsPart>)lapg_m[lapg_head - 1].value).add(((TmaRhsPart)lapg_m[lapg_head].value)); 
				break;
			case 108:  // rhsParts ::= rhsParts syntax_problem
				 ((List<TmaRhsPart>)lapg_m[lapg_head - 1].value).add(((AstError)lapg_m[lapg_head].value)); 
				break;
			case 113:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				 lapg_gg.value = new TmaRhsAnnotated(((AstRuleAnnotations)lapg_m[lapg_head - 1].value), ((TmaRhsPart)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // rhsAssignment ::= identifier '=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((AstIdentifier)lapg_m[lapg_head - 2].value), ((TmaRhsPart)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // rhsOptional ::= rhsCast '?'
				 lapg_gg.value = new TmaRhsQuantifier(((TmaRhsPart)lapg_m[lapg_head - 1].value), TmaRhsQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // rhsCast ::= rhsPrimary Las symref
				 lapg_gg.value = new TmaRhsCast(((TmaRhsPart)lapg_m[lapg_head - 2].value), ((AstReference)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // rhsUnordered ::= rhsPart '&' rhsPart
				 lapg_gg.value = new TmaRhsUnordered(((TmaRhsPart)lapg_m[lapg_head - 2].value), ((TmaRhsPart)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // rhsPrimary ::= symref
				 lapg_gg.value = new TmaRhsSymbol(((AstReference)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // rhsPrimary ::= '(' rules ')'
				 lapg_gg.value = new TmaRhsNested(((List<AstRule>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				 lapg_gg.value = new TmaRhsList(((List<TmaRhsPart>)lapg_m[lapg_head - 4].value), ((List<AstReference>)lapg_m[lapg_head - 2].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				 lapg_gg.value = new TmaRhsList(((List<TmaRhsPart>)lapg_m[lapg_head - 4].value), ((List<AstReference>)lapg_m[lapg_head - 2].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // rhsPrimary ::= rhsPrimary '*'
				 lapg_gg.value = new TmaRhsQuantifier(((TmaRhsPart)lapg_m[lapg_head - 1].value), TmaRhsQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // rhsPrimary ::= rhsPrimary '+'
				 lapg_gg.value = new TmaRhsQuantifier(((TmaRhsPart)lapg_m[lapg_head - 1].value), TmaRhsQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // rhsAnnotations ::= annotation_list
				 lapg_gg.value = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // rhsAnnotations ::= negative_la annotation_list
				 lapg_gg.value = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head - 1].value), ((List<AstNamedEntry>)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // rhsAnnotations ::= negative_la
				 lapg_gg.value = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // annotations ::= annotation_list
				 lapg_gg.value = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // annotation_list ::= annotation
				 lapg_gg.value = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.value).add(((AstNamedEntry)lapg_m[lapg_head].value)); 
				break;
			case 132:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_m[lapg_head - 1].value).add(((AstNamedEntry)lapg_m[lapg_head].value)); 
				break;
			case 133:  // annotation ::= '@' ID '=' expression
				 lapg_gg.value = new AstNamedEntry(((String)lapg_m[lapg_head - 2].value), ((AstExpression)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 134:  // annotation ::= '@' ID
				 lapg_gg.value = new AstNamedEntry(((String)lapg_m[lapg_head].value), ((AstExpression)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 135:  // annotation ::= '@' syntax_problem
				 lapg_gg.value = new AstNamedEntry(((AstError)lapg_m[lapg_head].value)); 
				break;
			case 136:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.value = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 137:  // negative_la_clause ::= symref
				 lapg_gg.value = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.value).add(((AstReference)lapg_m[lapg_head].value)); 
				break;
			case 138:  // negative_la_clause ::= negative_la_clause '|' symref
				 ((List<AstReference>)lapg_m[lapg_head - 2].value).add(((AstReference)lapg_m[lapg_head].value)); 
				break;
			case 139:  // expression ::= scon
				 lapg_gg.value = new AstLiteralExpression(((String)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 140:  // expression ::= icon
				 lapg_gg.value = new AstLiteralExpression(((Integer)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 141:  // expression ::= Ltrue
				 lapg_gg.value = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 142:  // expression ::= Lfalse
				 lapg_gg.value = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 146:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.value = new AstInstance(((AstName)lapg_m[lapg_head - 3].value), ((List<AstNamedEntry>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 149:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.value = new AstArray(((List<AstExpression>)lapg_m[lapg_head - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 151:  // expression_list ::= expression
				 lapg_gg.value = new ArrayList(); ((List<AstExpression>)lapg_gg.value).add(((AstExpression)lapg_m[lapg_head].value)); 
				break;
			case 152:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_m[lapg_head - 2].value).add(((AstExpression)lapg_m[lapg_head].value)); 
				break;
			case 153:  // map_entries ::= ID map_separator expression
				 lapg_gg.value = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.value).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].value), ((AstExpression)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 154:  // map_entries ::= map_entries ',' ID map_separator expression
				 ((List<AstNamedEntry>)lapg_m[lapg_head - 4].value).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].value), ((AstExpression)lapg_m[lapg_head].value), source, lapg_m[lapg_head - 2].offset, lapg_gg.endoffset)); 
				break;
			case 158:  // name ::= qualified_id
				 lapg_gg.value = new AstName(((String)lapg_m[lapg_head].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 160:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)lapg_m[lapg_head - 2].value) + "." + ((String)lapg_m[lapg_head].value); 
				break;
			case 161:  // command ::= code
				 lapg_gg.value = new AstCode(source, lapg_m[lapg_head].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 162:  // syntax_problem ::= error
				 lapg_gg.value = new AstError(source, lapg_gg.offset, lapg_gg.endoffset); 
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

	public AstRoot parseInput(TMLexer lexer) throws IOException, ParseException {
		return (AstRoot) parse(lexer, 0, 267);
	}

	public AstExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 268);
	}
}
