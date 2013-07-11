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
	private static final int[] tmAction = TMLexer.unpack_int(268,
		"\uffff\uffff\uffff\uffff\244\0\ufffd\uffff\uffff\uffff\uffff\uffff\4\0\ufff5\uffff" +
		"\uffef\uffff\35\0\41\0\42\0\40\0\7\0\11\0\215\0\216\0\uffc9\uffff\217\0\220\0\uffff" +
		"\uffff\221\0\230\0\uffff\uffff\10\0\uff9d\uffff\uffff\uffff\67\0\5\0\uff95\uffff" +
		"\uffff\uffff\44\0\uffff\uffff\uff6f\uffff\uffff\uffff\uffff\uffff\uff5f\uffff\36" +
		"\0\uff51\uffff\74\0\77\0\100\0\uffff\uffff\uff2d\uffff\205\0\37\0\3\0\231\0\uff0f" +
		"\uffff\uffff\uffff\241\0\uffff\uffff\uff09\uffff\34\0\43\0\6\0\uffff\uffff\uffff" +
		"\uffff\66\0\2\0\22\0\uffff\uffff\24\0\25\0\20\0\21\0\uff03\uffff\16\0\17\0\23\0\26" +
		"\0\30\0\27\0\uffff\uffff\15\0\ufecf\uffff\uffff\uffff\uffff\uffff\114\0\115\0\116" +
		"\0\uffff\uffff\ufea7\uffff\211\0\ufe83\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\ufe57\uffff\uffff\uffff\uffff\uffff\75\0\76\0\uffff\uffff\206\0\uffff\uffff\227" +
		"\0\ufe4f\uffff\uffff\uffff\71\0\72\0\70\0\12\0\ufe31\uffff\uffff\uffff\13\0\14\0" +
		"\ufdfd\uffff\ufdcf\uffff\uffff\uffff\121\0\126\0\uffff\uffff\uffff\uffff\ufdc7\uffff" +
		"\uffff\uffff\ufd85\uffff\uffff\uffff\243\0\uffff\uffff\173\0\uffff\uffff\ufd59\uffff" +
		"\133\0\ufd51\uffff\135\0\ufd25\uffff\ufcf7\uffff\156\0\161\0\163\0\ufcc5\uffff\157" +
		"\0\ufc91\uffff\uffff\uffff\uffff\uffff\ufc57\uffff\ufc35\uffff\160\0\143\0\142\0" +
		"\130\0\ufc15\uffff\ufc0d\uffff\112\0\ufbe1\uffff\uffff\uffff\113\0\ufbb5\uffff\uffff" +
		"\uffff\uffff\uffff\232\0\uffff\uffff\ufb89\uffff\uffff\uffff\242\0\33\0\uffff\uffff" +
		"\46\0\ufb83\uffff\123\0\125\0\120\0\uffff\uffff\117\0\127\0\207\0\uffff\uffff\152" +
		"\0\uffff\uffff\ufb57\uffff\213\0\uffff\uffff\uffff\uffff\150\0\uffff\uffff\uffff" +
		"\uffff\110\0\ufb29\uffff\uffff\uffff\ufafb\uffff\uffff\uffff\ufacd\uffff\140\0\ufaad" +
		"\uffff\155\0\141\0\uffff\uffff\167\0\177\0\200\0\uffff\uffff\uffff\uffff\162\0\144" +
		"\0\ufa7b\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufa5b\uffff\uffff\uffff\ufa2f" +
		"\uffff\ufa03\uffff\uffff\uffff\236\0\237\0\235\0\uffff\uffff\uffff\uffff\224\0\60" +
		"\0\50\0\uf9d7\uffff\122\0\151\0\174\0\uffff\uffff\uffff\uffff\212\0\164\0\165\0\uffff" +
		"\uffff\134\0\137\0\uf9ad\uffff\171\0\146\0\uffff\uffff\131\0\107\0\106\0\uffff\uffff" +
		"\104\0\uffff\uffff\uffff\uffff\uf97b\uffff\233\0\uffff\uffff\uffff\uffff\52\0\uf94f" +
		"\uffff\uffff\uffff\214\0\147\0\uffff\uffff\105\0\103\0\102\0\uffff\uffff\uffff\uffff" +
		"\62\0\63\0\64\0\65\0\uffff\uffff\54\0\56\0\uffff\uffff\145\0\101\0\234\0\61\0\176" +
		"\0\175\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(1750,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\45\uffff\ufffe\1\uffff\2\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\10\uffff\21\uffff\35\uffff\0\1\uffff\ufffe\1\uffff\2\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\45\uffff\4\uffff\5\uffff\21\uffff\36\uffff\37\uffff\40\uffff\22\226" +
		"\uffff\ufffe\14\uffff\17\73\22\73\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56" +
		"\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45" +
		"\uffff\10\uffff\21\uffff\35\uffff\0\1\uffff\ufffe\13\uffff\11\10\20\10\23\10\43\10" +
		"\44\10\47\10\uffff\ufffe\11\uffff\23\uffff\43\uffff\44\uffff\47\uffff\20\45\uffff" +
		"\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\35\uffff\0\0\uffff\ufffe\35" +
		"\uffff\2\204\45\204\46\204\47\204\50\204\51\204\52\204\53\204\54\204\55\204\56\204" +
		"\57\204\60\204\uffff\ufffe\17\uffff\22\225\uffff\ufffe\16\uffff\23\240\uffff\ufffe" +
		"\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\16\uffff\17\uffff\21\uffff\22\uffff\23\uffff" +
		"\26\uffff\27\uffff\30\uffff\33\uffff\34\uffff\35\uffff\25\32\uffff\ufffe\3\uffff" +
		"\0\57\1\57\2\57\10\57\21\57\35\57\45\57\46\57\47\57\50\57\51\57\52\57\53\57\54\57" +
		"\55\57\56\57\57\57\60\57\uffff\ufffe\13\uffff\2\210\20\210\23\210\35\210\45\210\46" +
		"\210\47\210\50\210\51\210\52\210\53\210\54\210\55\210\56\210\57\210\60\210\uffff" +
		"\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff" +
		"\62\uffff\12\136\15\136\uffff\ufffe\20\44\11\113\44\113\uffff\ufffe\2\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\25\223\uffff\ufffe\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff" +
		"\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\16\uffff" +
		"\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27\uffff\30\uffff\33\uffff\34\uffff" +
		"\35\uffff\25\31\uffff\ufffe\14\uffff\0\47\1\47\2\47\5\47\10\47\21\47\23\47\35\47" +
		"\45\47\46\47\47\47\50\47\51\47\52\47\53\47\54\47\55\47\56\47\57\47\60\47\62\47\uffff" +
		"\ufffe\54\uffff\15\124\17\124\uffff\ufffe\13\10\20\10\32\10\43\10\1\11\2\11\10\11" +
		"\12\11\15\11\23\11\24\11\25\11\30\11\31\11\33\11\34\11\35\11\41\11\42\11\45\11\46" +
		"\11\47\11\50\11\51\11\52\11\53\11\54\11\55\11\56\11\57\11\60\11\62\11\uffff\ufffe" +
		"\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62" +
		"\uffff\12\136\25\136\uffff\ufffe\12\uffff\15\132\25\132\uffff\ufffe\2\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\136\15\136\25" +
		"\136\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24" +
		"\uffff\35\uffff\62\uffff\12\136\15\136\25\136\uffff\ufffe\34\uffff\1\153\2\153\10" +
		"\153\12\153\15\153\23\153\24\153\25\153\35\153\41\153\45\153\46\153\47\153\50\153" +
		"\51\153\52\153\53\153\54\153\55\153\56\153\57\153\60\153\62\153\uffff\ufffe\33\uffff" +
		"\1\166\2\166\10\166\12\166\15\166\23\166\24\166\25\166\34\166\35\166\41\166\45\166" +
		"\46\166\47\166\50\166\51\166\52\166\53\166\54\166\55\166\56\166\57\166\60\166\62" +
		"\166\uffff\ufffe\30\uffff\31\uffff\42\uffff\1\170\2\170\10\170\12\170\15\170\23\170" +
		"\24\170\25\170\33\170\34\170\35\170\41\170\45\170\46\170\47\170\50\170\51\170\52" +
		"\170\53\170\54\170\55\170\56\170\57\170\60\170\62\170\uffff\ufffe\35\uffff\2\201" +
		"\23\201\45\201\46\201\47\201\50\201\51\201\52\201\53\201\54\201\55\201\56\201\57" +
		"\201\60\201\20\204\uffff\ufffe\35\uffff\2\203\23\203\45\203\46\203\47\203\50\203" +
		"\51\203\52\203\53\203\54\203\55\203\56\203\57\203\60\203\uffff\ufffe\17\uffff\11" +
		"\111\44\111\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff" +
		"\24\uffff\35\uffff\62\uffff\12\136\15\136\uffff\ufffe\1\uffff\2\uffff\60\uffff\57" +
		"\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46" +
		"\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\136\15\136\uffff" +
		"\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff" +
		"\62\uffff\12\136\15\136\uffff\ufffe\17\uffff\25\222\uffff\ufffe\5\uffff\0\51\1\51" +
		"\2\51\10\51\21\51\23\51\35\51\45\51\46\51\47\51\50\51\51\51\52\51\53\51\54\51\55" +
		"\51\56\51\57\51\60\51\62\51\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff" +
		"\10\uffff\23\uffff\24\uffff\35\uffff\41\uffff\62\uffff\12\136\25\136\uffff\ufffe" +
		"\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62" +
		"\uffff\12\136\15\136\25\136\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff" +
		"\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\136\15\136\25\136\uffff\ufffe\35" +
		"\uffff\2\201\23\201\45\201\46\201\47\201\50\201\51\201\52\201\53\201\54\201\55\201" +
		"\56\201\57\201\60\201\uffff\ufffe\34\uffff\1\154\2\154\10\154\12\154\15\154\23\154" +
		"\24\154\25\154\35\154\41\154\45\154\46\154\47\154\50\154\51\154\52\154\53\154\54" +
		"\154\55\154\56\154\57\154\60\154\62\154\uffff\ufffe\35\uffff\2\202\23\202\45\202" +
		"\46\202\47\202\50\202\51\202\52\202\53\202\54\202\55\202\56\202\57\202\60\202\uffff" +
		"\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff" +
		"\62\uffff\12\136\15\136\uffff\ufffe\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10" +
		"\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\136\15\136\uffff\ufffe\1\uffff\2\uffff" +
		"\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62\uffff\12\136\15" +
		"\136\uffff\ufffe\23\uffff\0\53\1\53\2\53\10\53\21\53\35\53\45\53\46\53\47\53\50\53" +
		"\51\53\52\53\53\53\54\53\55\53\56\53\57\53\60\53\62\53\uffff\ufffe\34\172\1\172\2" +
		"\172\10\172\12\172\15\172\23\172\24\172\25\172\35\172\41\172\45\172\46\172\47\172" +
		"\50\172\51\172\52\172\53\172\54\172\55\172\56\172\57\172\60\172\62\172\uffff\ufffe" +
		"\1\uffff\2\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\45\uffff\10\uffff\23\uffff\24\uffff\35\uffff\62" +
		"\uffff\12\136\15\136\uffff\ufffe\62\uffff\0\55\1\55\2\55\10\55\21\55\35\55\45\55" +
		"\46\55\47\55\50\55\51\55\52\55\53\55\54\55\55\55\56\55\57\55\60\55\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(120,
		"\0\2\34\130\133\143\153\153\153\174\205\207\217\223\235\242\255\265\304\312\347\366" +
		"\376\u0102\u0106\u010c\u010e\u0111\u0116\u011d\u0138\u013f\u0146\u014d\u014e\u014f" +
		"\u0154\u0159\u0196\u01d3\u0212\u024f\u028c\u02c9\u0306\u0343\u0380\u03bd\u03fa\u0437" +
		"\u0437\u0447\u0448\u0449\u044b\u0464\u048d\u0491\u0493\u0497\u049a\u049c\u04a0\u04a4" +
		"\u04a8\u04a9\u04aa\u04ab\u04af\u04b0\u04b2\u04b4\u04b6\u04b9\u04bc\u04bf\u04c0\u04c3" +
		"\u04c4\u04c6\u04c8\u04cb\u04d4\u04dd\u04e7\u04f1\u04ff\u050a\u0519\u0528\u0539\u054c" +
		"\u055f\u056e\u0581\u0590\u059d\u05b0\u05c7\u05d6\u05d7\u05de\u05df\u05e0\u05e2\u05e3" +
		"\u05e4\u05f4\u060e\u0610\u0611\u0613\u0614\u0615\u0616\u0617\u0618\u0626\u0627\u0628");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1576,
		"\u0108\u0109\0\1\5\10\21\27\35\43\46\124\137\161\164\176\217\221\224\252\262\264" +
		"\307\311\312\317\353\371\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121" +
		"\124\125\127\137\141\142\147\160\161\164\165\174\176\206\217\221\224\235\243\247" +
		"\252\255\257\260\262\264\265\273\277\304\307\311\312\317\320\330\331\343\353\361" +
		"\371\27\113\114\1\21\27\36\137\161\317\371\1\21\27\137\161\237\317\371\10\35\46\124" +
		"\164\174\176\217\221\224\252\262\264\307\311\312\353\44\126\131\132\135\222\225\226" +
		"\313\172\254\3\41\122\167\230\263\300\355\31\153\230\355\155\160\171\305\306\310" +
		"\347\351\352\370\36\64\102\111\147\32\36\60\102\111\147\155\216\231\335\364\40\167" +
		"\207\230\300\335\355\364\0\1\5\10\21\27\35\36\102\111\137\147\161\317\371\32\36\61" +
		"\102\111\147\7\36\44\63\102\111\124\132\135\147\164\174\176\206\217\221\224\252\255" +
		"\257\262\264\265\273\307\311\312\324\353\124\164\174\176\217\221\224\252\262\264" +
		"\273\307\311\312\353\75\111\150\232\251\254\361\376\36\102\111\147\36\102\111\147" +
		"\36\102\111\147\205\u0101\205\u0101\167\263\300\36\102\111\147\203\36\102\111\147" +
		"\177\270\340\10\35\36\46\53\102\111\124\147\164\174\176\210\211\217\221\224\252\262" +
		"\264\266\273\303\307\311\312\353\1\21\27\137\161\317\371\1\21\27\137\161\317\371" +
		"\1\21\27\137\161\317\371\252\205\44\132\135\167\300\44\131\132\135\226\0\1\4\5\10" +
		"\21\24\27\35\36\43\46\52\70\71\102\111\115\121\124\125\127\137\141\142\147\160\161" +
		"\163\164\165\174\176\206\217\221\224\235\243\247\252\255\257\260\262\264\265\273" +
		"\277\304\307\311\312\317\320\330\331\343\353\361\371\0\1\4\5\10\21\24\27\35\36\43" +
		"\46\52\70\71\102\111\115\121\124\125\127\137\141\142\147\160\161\163\164\165\174" +
		"\176\206\217\221\224\235\243\247\252\255\257\260\262\264\265\273\277\304\307\311" +
		"\312\317\320\330\331\343\353\361\371\0\1\4\5\10\21\24\27\35\36\43\44\46\52\70\71" +
		"\102\111\115\121\124\125\127\132\135\137\141\142\147\160\161\164\165\174\176\206" +
		"\217\221\224\235\243\247\252\255\257\260\262\264\265\273\277\304\307\311\312\317" +
		"\320\330\331\343\353\361\371\0\1\4\5\10\21\24\27\35\36\42\43\46\52\70\71\102\111" +
		"\115\121\124\125\127\137\141\142\147\160\161\164\165\174\176\206\217\221\224\235" +
		"\243\247\252\255\257\260\262\264\265\273\277\304\307\311\312\317\320\330\331\343" +
		"\353\361\371\0\1\4\5\10\21\24\27\35\36\42\43\46\52\70\71\102\111\115\121\124\125" +
		"\127\137\141\142\147\160\161\164\165\174\176\206\217\221\224\235\243\247\252\255" +
		"\257\260\262\264\265\273\277\304\307\311\312\317\320\330\331\343\353\361\371\0\1" +
		"\4\5\10\21\24\27\35\36\42\43\46\52\70\71\102\111\115\121\124\125\127\137\141\142" +
		"\147\160\161\164\165\174\176\206\217\221\224\235\243\247\252\255\257\260\262\264" +
		"\265\273\277\304\307\311\312\317\320\330\331\343\353\361\371\0\1\4\5\10\21\24\27" +
		"\35\36\42\43\46\52\70\71\102\111\115\121\124\125\127\137\141\142\147\160\161\164" +
		"\165\174\176\206\217\221\224\235\243\247\252\255\257\260\262\264\265\273\277\304" +
		"\307\311\312\317\320\330\331\343\353\361\371\0\1\4\5\10\21\24\27\35\36\43\46\52\70" +
		"\71\102\111\115\121\124\125\127\137\141\142\147\154\160\161\164\165\174\176\206\217" +
		"\221\224\235\243\247\252\255\257\260\262\264\265\273\277\304\307\311\312\317\320" +
		"\330\331\343\353\361\371\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121" +
		"\124\125\127\137\141\142\147\160\161\164\165\174\176\206\217\221\224\235\243\247" +
		"\252\255\257\260\262\264\265\273\277\304\307\311\312\317\320\330\331\343\353\356" +
		"\361\371\0\1\4\5\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\124\125\127\137" +
		"\141\142\147\160\161\164\165\174\176\206\217\221\224\235\243\247\252\255\257\260" +
		"\262\264\265\273\277\304\307\311\312\317\320\330\331\343\353\356\361\371\0\1\4\5" +
		"\10\21\24\27\35\36\43\46\52\70\71\102\111\115\121\124\125\127\137\141\142\147\160" +
		"\161\164\165\174\176\206\217\221\224\235\243\247\252\255\257\260\262\264\265\273" +
		"\277\304\307\311\312\317\320\330\331\343\353\356\361\371\0\1\4\5\10\21\24\27\35\36" +
		"\43\46\52\70\71\102\111\115\121\124\125\127\137\141\142\147\160\161\164\165\174\176" +
		"\206\217\221\224\235\243\247\252\255\257\260\262\264\265\273\277\304\307\311\312" +
		"\317\320\330\331\343\353\356\361\371\124\164\174\176\217\221\224\252\262\264\273" +
		"\307\311\312\353\360\0\0\0\5\0\4\5\10\35\46\52\71\124\164\174\176\206\217\221\224" +
		"\252\262\264\265\273\307\311\312\353\1\21\27\115\121\124\125\127\137\160\161\164" +
		"\165\174\176\206\217\221\224\243\247\252\255\257\260\262\264\265\273\277\304\307" +
		"\311\312\317\330\331\343\353\361\371\7\44\132\135\36\102\36\102\111\147\27\113\114" +
		"\0\5\0\5\10\35\0\5\10\35\0\5\10\35\153\324\356\0\5\10\35\4\70\235\4\71\10\35\10\35" +
		"\46\10\35\46\44\132\135\42\10\35\46\115\115\243\121\330\125\260\343\124\164\217\221" +
		"\224\307\311\312\353\124\164\217\221\224\307\311\312\353\124\164\217\221\224\262" +
		"\307\311\312\353\124\164\217\221\224\262\307\311\312\353\124\164\174\176\217\221" +
		"\224\252\262\264\307\311\312\353\124\164\174\217\221\224\262\307\311\312\353\124" +
		"\164\174\176\217\221\224\252\262\264\273\307\311\312\353\124\164\174\176\217\221" +
		"\224\252\262\264\273\307\311\312\353\124\164\174\176\206\217\221\224\252\262\264" +
		"\265\273\307\311\312\353\124\164\174\176\206\217\221\224\252\255\257\262\264\265" +
		"\273\307\311\312\353\124\164\174\176\206\217\221\224\252\255\257\262\264\265\273" +
		"\307\311\312\353\124\164\174\176\217\221\224\252\262\264\273\307\311\312\353\124" +
		"\164\174\176\206\217\221\224\252\255\257\262\264\265\273\307\311\312\353\124\164" +
		"\174\176\217\221\224\252\262\264\273\307\311\312\353\10\35\46\124\164\217\221\224" +
		"\262\307\311\312\353\10\35\46\124\164\174\176\211\217\221\224\252\262\264\273\307" +
		"\311\312\353\10\35\46\53\124\164\174\176\210\211\217\221\224\252\262\264\266\273" +
		"\303\307\311\312\353\124\164\174\176\217\221\224\252\262\264\273\307\311\312\353" +
		"\165\1\21\27\137\161\317\371\21\141\230\355\24\24\124\164\174\176\217\221\224\252" +
		"\262\264\273\307\311\312\353\360\0\1\5\10\21\27\35\43\46\124\137\161\164\176\217" +
		"\221\224\252\262\264\307\311\312\317\353\371\10\35\102\7\44\153\237\324\360\154\124" +
		"\164\174\176\217\221\224\252\262\264\307\311\312\353\141\21");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1576,
		"\u010a\u010b\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\2\3\16\30\3\41\16" +
		"\62\16\41\74\122\30\30\143\30\74\74\16\16\162\16\16\16\230\233\74\16\16\162\16\162" +
		"\162\162\162\162\162\143\16\16\162\16\16\16\162\162\162\162\16\16\162\162\162\16" +
		"\355\16\16\16\162\16\16\65\65\65\17\17\17\75\17\17\17\17\20\20\20\20\20\323\20\20" +
		"\42\42\42\163\163\163\163\163\163\163\163\163\163\163\163\163\163\124\217\221\124" +
		"\224\307\311\312\353\262\331\27\114\161\255\314\255\255\314\70\235\315\315\242\244" +
		"\261\345\346\350\365\366\367\u0103\76\142\76\76\76\71\77\137\77\77\77\243\304\320" +
		"\304\304\113\256\302\316\342\363\316\u0102\4\21\4\4\21\21\4\100\100\100\21\100\21" +
		"\21\21\72\101\140\101\101\101\36\102\36\141\102\102\164\36\36\102\164\164\164\164" +
		"\164\164\164\164\164\164\164\164\164\164\164\164\164\356\164\165\165\165\165\165" +
		"\165\165\165\165\165\165\165\165\165\165\146\151\234\321\327\332\u0101\u0105\103" +
		"\103\103\103\104\104\104\104\105\105\105\105\275\u0106\276\u0107\257\257\257\106" +
		"\106\106\106\274\107\107\107\107\273\273\273\43\43\110\43\43\110\110\43\110\43\43" +
		"\43\43\43\43\43\43\43\43\43\43\43\43\43\43\43\43\22\22\22\22\22\22\22\23\23\23\23" +
		"\23\23\23\24\24\24\24\24\24\24\330\277\125\125\125\260\343\126\222\126\225\313\3" +
		"\16\30\3\41\16\62\16\41\74\122\30\30\143\30\74\74\16\16\162\16\16\16\230\233\74\16" +
		"\16\247\162\16\162\162\162\162\162\162\143\16\16\162\16\16\16\162\162\162\162\16" +
		"\16\162\162\162\16\355\16\16\16\162\16\16\3\16\30\3\41\16\62\16\41\74\122\30\30\143" +
		"\30\74\74\16\16\162\16\16\16\230\233\74\16\16\250\162\16\162\162\162\162\162\162" +
		"\143\16\16\162\16\16\16\162\162\162\162\16\16\162\162\162\16\355\16\16\16\162\16" +
		"\16\3\16\30\3\41\16\62\16\41\74\122\127\30\30\143\30\74\74\16\16\162\16\16\127\127" +
		"\16\230\233\74\16\16\162\16\162\162\162\162\162\162\143\16\16\162\16\16\16\162\162" +
		"\162\162\16\16\162\162\162\16\355\16\16\16\162\16\16\3\16\30\3\41\16\62\16\41\74" +
		"\115\122\30\30\143\30\74\74\16\16\162\16\16\16\230\233\74\16\16\162\16\162\162\162" +
		"\162\162\162\143\16\16\162\16\16\16\162\162\162\162\16\16\162\162\162\16\355\16\16" +
		"\16\162\16\16\3\16\30\3\41\16\62\16\41\74\116\122\30\30\143\30\74\74\16\16\162\16" +
		"\16\16\230\233\74\16\16\162\16\162\162\162\162\162\162\143\16\16\162\16\16\16\162" +
		"\162\162\162\16\16\162\162\162\16\355\16\16\16\162\16\16\3\16\30\3\41\16\62\16\41" +
		"\74\117\122\30\30\143\30\74\74\16\16\162\16\16\16\230\233\74\16\16\162\16\162\162" +
		"\162\162\162\162\143\16\16\162\16\16\16\162\162\162\162\16\16\162\162\162\16\355" +
		"\16\16\16\162\16\16\3\16\30\3\41\16\62\16\41\74\120\122\30\30\143\30\74\74\16\16" +
		"\162\16\16\16\230\233\74\16\16\162\16\162\162\162\162\162\162\143\16\16\162\16\16" +
		"\16\162\162\162\162\16\16\162\162\162\16\355\16\16\16\162\16\16\3\16\30\3\41\16\62" +
		"\16\41\74\122\30\30\143\30\74\74\16\16\162\16\16\16\230\233\74\240\16\16\162\16\162" +
		"\162\162\162\162\162\143\16\16\162\16\16\16\162\162\162\162\16\16\162\162\162\16" +
		"\355\16\16\16\162\16\16\3\16\30\3\41\16\62\16\41\74\122\30\30\143\30\74\74\16\16" +
		"\162\16\16\16\230\233\74\16\16\162\16\162\162\162\162\162\162\143\16\16\162\16\16" +
		"\16\162\162\162\162\16\16\162\162\162\16\355\16\16\16\162\372\16\16\3\16\30\3\41" +
		"\16\62\16\41\74\122\30\30\143\30\74\74\16\16\162\16\16\16\230\233\74\16\16\162\16" +
		"\162\162\162\162\162\162\143\16\16\162\16\16\16\162\162\162\162\16\16\162\162\162" +
		"\16\355\16\16\16\162\373\16\16\3\16\30\3\41\16\62\16\41\74\122\30\30\143\30\74\74" +
		"\16\16\162\16\16\16\230\233\74\16\16\162\16\162\162\162\162\162\162\143\16\16\162" +
		"\16\16\16\162\162\162\162\16\16\162\162\162\16\355\16\16\16\162\374\16\16\3\16\30" +
		"\3\41\16\62\16\41\74\122\30\30\143\30\74\74\16\16\162\16\16\16\230\233\74\16\16\162" +
		"\16\162\162\162\162\162\162\143\16\16\162\16\16\16\162\162\162\162\16\16\162\162" +
		"\162\16\355\16\16\16\162\375\16\16\166\166\166\166\166\166\166\166\166\166\166\166" +
		"\166\166\166\166\u0108\5\6\34\7\31\7\44\44\132\135\31\167\167\263\263\300\167\167" +
		"\167\263\167\263\263\263\167\167\167\167\25\25\25\154\157\170\215\220\25\245\25\170" +
		"\253\170\170\170\170\170\170\154\326\170\170\170\215\170\170\170\170\341\344\170" +
		"\170\170\25\157\362\215\170\245\25\37\130\223\223\111\147\112\112\152\152\66\153" +
		"\66\10\35\11\11\45\45\12\12\12\12\13\13\13\13\236\357\376\14\14\14\14\32\144\322" +
		"\33\145\46\46\47\47\133\50\50\50\131\131\226\121\51\51\51\155\156\325\160\361\216" +
		"\335\364\171\251\305\306\310\347\351\352\370\172\172\172\172\172\172\172\172\172" +
		"\173\173\173\173\173\336\173\173\173\173\174\174\174\174\174\174\174\174\174\174" +
		"\175\175\175\175\175\175\175\175\175\175\175\175\175\175\176\252\264\176\176\176" +
		"\176\176\176\176\176\177\177\177\270\177\177\177\270\177\270\340\177\177\177\177" +
		"\200\200\200\200\200\200\200\200\200\200\200\200\200\200\200\201\201\201\201\301" +
		"\201\201\201\201\201\201\301\201\201\201\201\201\202\202\202\202\202\202\202\202" +
		"\202\333\334\202\202\202\202\202\202\202\202\203\203\203\203\203\203\203\203\203" +
		"\203\203\203\203\203\203\203\203\203\203\204\204\204\204\204\204\204\204\204\204" +
		"\204\204\204\204\204\205\205\205\205\205\205\205\205\205\205\205\205\205\205\205" +
		"\205\205\205\205\206\206\265\265\206\206\206\265\206\265\265\206\206\206\206\52\52" +
		"\52\207\207\207\207\207\207\207\207\207\207\53\53\53\210\210\266\266\303\210\210" +
		"\210\266\210\266\266\210\210\210\210\54\54\54\136\54\54\54\54\136\54\54\54\54\54" +
		"\54\54\136\54\136\54\54\54\54\211\211\211\211\211\211\211\211\211\211\211\211\211" +
		"\211\211\254\u0109\57\67\227\246\354\u0104\60\231\317\371\63\64\212\212\212\212\212" +
		"\212\212\212\212\212\212\212\212\212\212\377\15\26\15\55\26\26\55\123\134\213\26" +
		"\26\213\271\213\213\213\271\213\271\213\213\213\26\213\26\56\73\150\40\40\237\324" +
		"\360\u0100\241\214\214\267\272\214\214\214\272\214\337\214\214\214\214\232\61");

	private static final short[] lapg_rlen = TMLexer.unpack_short(165,
		"\1\0\3\2\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\1\0\3\1\1\2\2\1\1\1\3\1\0\1\0" +
		"\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\7\6\6\5\6\5\5\4\2\2\1\1\1\1\4" +
		"\4\1\3\1\0\2\1\2\1\3\1\1\3\1\0\3\2\2\1\1\2\5\3\4\2\3\2\1\2\2\1\1\1\1\2\1\3\3\1\2" +
		"\1\3\3\1\3\6\6\2\2\1\2\1\1\1\2\4\2\2\3\1\3\1\1\1\1\1\1\0\5\1\0\3\1\1\3\3\5\1\1\1" +
		"\1\1\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(165,
		"\154\154\63\63\64\64\65\65\66\67\70\70\71\71\72\72\72\72\72\72\72\72\72\72\72\155" +
		"\155\72\73\74\74\74\75\75\75\76\156\156\157\157\160\160\161\161\162\162\77\77\100" +
		"\101\102\102\102\102\103\104\104\105\106\106\107\107\107\110\110\111\111\111\111" +
		"\111\111\111\111\112\112\112\113\113\113\114\114\115\115\163\163\116\117\117\120" +
		"\120\121\122\122\164\164\123\123\123\123\123\124\124\124\124\124\125\125\126\126" +
		"\126\127\127\127\130\130\131\131\131\132\132\133\133\134\135\135\135\135\135\135" +
		"\136\136\136\137\140\140\141\141\141\142\143\143\144\144\144\144\144\165\165\144" +
		"\166\166\144\144\145\145\146\146\147\147\147\150\151\151\152\153");

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
		"nonterm_type",
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
		public static final int input = 51;
		public static final int options = 52;
		public static final int option = 53;
		public static final int identifier = 54;
		public static final int symref = 55;
		public static final int type = 56;
		public static final int type_part_list = 57;
		public static final int type_part = 58;
		public static final int pattern = 59;
		public static final int lexer_parts = 60;
		public static final int lexer_part = 61;
		public static final int named_pattern = 62;
		public static final int lexeme = 63;
		public static final int lexem_transition = 64;
		public static final int lexem_attrs = 65;
		public static final int lexem_attribute = 66;
		public static final int state_selector = 67;
		public static final int state_list = 68;
		public static final int stateref = 69;
		public static final int lexer_state = 70;
		public static final int grammar_parts = 71;
		public static final int grammar_part = 72;
		public static final int nonterm = 73;
		public static final int nonterm_type = 74;
		public static final int priority_kw = 75;
		public static final int directive = 76;
		public static final int inputs = 77;
		public static final int inputref = 78;
		public static final int references = 79;
		public static final int references_cs = 80;
		public static final int rules = 81;
		public static final int rule_list = 82;
		public static final int rule0 = 83;
		public static final int rhsPrefix = 84;
		public static final int rhsSuffix = 85;
		public static final int rhsParts = 86;
		public static final int rhsPart = 87;
		public static final int rhsAnnotated = 88;
		public static final int rhsAssignment = 89;
		public static final int rhsOptional = 90;
		public static final int rhsCast = 91;
		public static final int rhsUnordered = 92;
		public static final int rhsPrimary = 93;
		public static final int rhsAnnotations = 94;
		public static final int annotations = 95;
		public static final int annotation_list = 96;
		public static final int annotation = 97;
		public static final int negative_la = 98;
		public static final int negative_la_clause = 99;
		public static final int expression = 100;
		public static final int expression_list = 101;
		public static final int map_entries = 102;
		public static final int map_separator = 103;
		public static final int name = 104;
		public static final int qualified_id = 105;
		public static final int command = 106;
		public static final int syntax_problem = 107;
		public static final int grammar_partsopt = 108;
		public static final int type_part_listopt = 109;
		public static final int typeopt = 110;
		public static final int lexem_transitionopt = 111;
		public static final int iconopt = 112;
		public static final int lexem_attrsopt = 113;
		public static final int commandopt = 114;
		public static final int Lnoeoiopt = 115;
		public static final int rhsSuffixopt = 116;
		public static final int map_entriesopt = 117;
		public static final int expression_listopt = 118;
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
			case 2:  // input ::= options lexer_parts grammar_partsopt
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)tmStack[tmHead - 2].value), ((List<ITmaLexerPart>)tmStack[tmHead - 1].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // input ::= lexer_parts grammar_partsopt
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)null), ((List<ITmaLexerPart>)tmStack[tmHead - 1].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 4:  // options ::= option
				 lapg_gg.value = new ArrayList<TmaOptionPart>(16); ((List<TmaOptionPart>)lapg_gg.value).add(((TmaOptionPart)tmStack[tmHead].value)); 
				break;
			case 5:  // options ::= options option
				 ((List<TmaOptionPart>)tmStack[tmHead - 1].value).add(((TmaOptionPart)tmStack[tmHead].value)); 
				break;
			case 6:  // option ::= ID '=' expression
				 lapg_gg.value = new TmaOption(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // identifier ::= ID
				 lapg_gg.value = new TmaIdentifier(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // symref ::= ID
				 lapg_gg.value = new TmaSymref(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // type ::= '(' scon ')'
				 lapg_gg.value = ((String)tmStack[tmHead - 1].value); 
				break;
			case 11:  // type ::= '(' type_part_list ')'
				 lapg_gg.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 28:  // pattern ::= regexp
				 lapg_gg.value = new TmaPattern(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 29:  // lexer_parts ::= lexer_part
				 lapg_gg.value = new ArrayList<ITmaLexerPart>(64); ((List<ITmaLexerPart>)lapg_gg.value).add(((ITmaLexerPart)tmStack[tmHead].value)); 
				break;
			case 30:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<ITmaLexerPart>)tmStack[tmHead - 1].value).add(((ITmaLexerPart)tmStack[tmHead].value)); 
				break;
			case 31:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<ITmaLexerPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 35:  // named_pattern ::= ID '=' pattern
				 lapg_gg.value = new TmaNamedPattern(((String)tmStack[tmHead - 2].value), ((TmaPattern)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				 lapg_gg.value = new TmaLexeme(((TmaIdentifier)tmStack[tmHead - 7].value), ((String)tmStack[tmHead - 6].value), ((TmaPattern)tmStack[tmHead - 4].value), ((TmaStateref)tmStack[tmHead - 3].value), ((Integer)tmStack[tmHead - 2].value), ((TmaLexemAttrs)tmStack[tmHead - 1].value), ((TmaCommand)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // lexeme ::= identifier typeopt ':'
				 lapg_gg.value = new TmaLexeme(((TmaIdentifier)tmStack[tmHead - 2].value), ((String)tmStack[tmHead - 1].value), ((TmaPattern)null), ((TmaStateref)null), ((Integer)null), ((TmaLexemAttrs)null), ((TmaCommand)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // lexem_transition ::= '=>' stateref
				 lapg_gg.value = ((TmaStateref)tmStack[tmHead].value); 
				break;
			case 49:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.value = ((TmaLexemAttrs)tmStack[tmHead - 1].value); 
				break;
			case 50:  // lexem_attribute ::= Lsoft
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 51:  // lexem_attribute ::= Lclass
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 52:  // lexem_attribute ::= Lspace
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 53:  // lexem_attribute ::= Llayout
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // state_selector ::= '[' state_list ']'
				 lapg_gg.value = new TmaStateSelector(((List<TmaLexerState>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // state_list ::= lexer_state
				 lapg_gg.value = new ArrayList<Integer>(4); ((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value)); 
				break;
			case 56:  // state_list ::= state_list ',' lexer_state
				 ((List<TmaLexerState>)tmStack[tmHead - 2].value).add(((TmaLexerState)tmStack[tmHead].value)); 
				break;
			case 57:  // stateref ::= ID
				 lapg_gg.value = new TmaStateref(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // lexer_state ::= identifier '=>' stateref
				 lapg_gg.value = new TmaLexerState(((TmaIdentifier)tmStack[tmHead - 2].value), ((TmaStateref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 59:  // lexer_state ::= identifier
				 lapg_gg.value = new TmaLexerState(((TmaIdentifier)tmStack[tmHead].value), ((TmaStateref)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 60:  // grammar_parts ::= grammar_part
				 lapg_gg.value = new ArrayList<ITmaGrammarPart>(64); ((List<ITmaGrammarPart>)lapg_gg.value).add(((ITmaGrammarPart)tmStack[tmHead].value)); 
				break;
			case 61:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<ITmaGrammarPart>)tmStack[tmHead - 1].value).add(((ITmaGrammarPart)tmStack[tmHead].value)); 
				break;
			case 62:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<ITmaGrammarPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 65:  // nonterm ::= annotations identifier nonterm_type Linline '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 5].value), ((TmaNontermType)tmStack[tmHead - 4].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 6].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 5].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // nonterm ::= annotations identifier Linline '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 5].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // nonterm ::= annotations identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 4].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // nonterm ::= identifier nonterm_type Linline '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 5].value), ((TmaNontermType)tmStack[tmHead - 4].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // nonterm ::= identifier Linline '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // nonterm ::= identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // nonterm_type ::= Lextends references_cs
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 74:  // nonterm_type ::= Lreturns symref
				 lapg_gg.value = new TmaNontermTypeAST(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // nonterm_type ::= type
				 lapg_gg.value = new TmaNontermTypeRaw(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.value = new TmaDirectivePrio(((String)tmStack[tmHead - 2].value), ((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.value = new TmaDirectiveInput(((List<TmaInputref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // inputs ::= inputref
				 lapg_gg.value = new ArrayList<TmaInputref>(); ((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value)); 
				break;
			case 82:  // inputs ::= inputs ',' inputref
				 ((List<TmaInputref>)tmStack[tmHead - 2].value).add(((TmaInputref)tmStack[tmHead].value)); 
				break;
			case 85:  // inputref ::= symref Lnoeoiopt
				 lapg_gg.value = new TmaInputref(((TmaSymref)tmStack[tmHead - 1].value), ((String)tmStack[tmHead].value) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // references ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 87:  // references ::= references symref
				 ((List<TmaSymref>)tmStack[tmHead - 1].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 88:  // references_cs ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 89:  // references_cs ::= references_cs ',' symref
				 ((List<TmaSymref>)tmStack[tmHead - 2].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 91:  // rule_list ::= rule0
				 lapg_gg.value = new ArrayList<TmaRule0>(); ((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value)); 
				break;
			case 92:  // rule_list ::= rule_list '|' rule0
				 ((List<TmaRule0>)tmStack[tmHead - 2].value).add(((TmaRule0)tmStack[tmHead].value)); 
				break;
			case 95:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)tmStack[tmHead - 2].value), ((List<ITmaRhsPart>)tmStack[tmHead - 1].value), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // rule0 ::= rhsPrefix rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)tmStack[tmHead - 1].value), ((List<ITmaRhsPart>)null), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // rule0 ::= rhsParts rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)null), ((List<ITmaRhsPart>)tmStack[tmHead - 1].value), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // rule0 ::= rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)null), ((List<ITmaRhsPart>)null), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // rule0 ::= syntax_problem
				 lapg_gg.value = new TmaRule0(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 100:  // rhsPrefix ::= annotations ':'
				 lapg_gg.value = new TmaRhsPrefix(((TmaAnnotations)tmStack[tmHead - 1].value), null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // rhsPrefix ::= rhsAnnotations identifier Lextends references_cs ':'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)tmStack[tmHead - 4].value), ((TmaIdentifier)tmStack[tmHead - 3].value), ((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // rhsPrefix ::= rhsAnnotations identifier ':'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)tmStack[tmHead - 2].value), ((TmaIdentifier)tmStack[tmHead - 1].value), ((List<TmaSymref>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // rhsPrefix ::= identifier Lextends references_cs ':'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)null), ((TmaIdentifier)tmStack[tmHead - 3].value), ((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // rhsPrefix ::= identifier ':'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)null), ((TmaIdentifier)tmStack[tmHead - 1].value), ((List<TmaSymref>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // rhsSuffix ::= '%' Lprio symref
				 lapg_gg.value = new TmaRhsPrio(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // rhsSuffix ::= '%' Lshift
				 lapg_gg.value = new TmaRhsShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 107:  // rhsParts ::= rhsPart
				 lapg_gg.value = new ArrayList<ITmaRhsPart>(); ((List<ITmaRhsPart>)lapg_gg.value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 108:  // rhsParts ::= rhsParts rhsPart
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 109:  // rhsParts ::= rhsParts syntax_problem
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 114:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				 lapg_gg.value = new TmaRhsAnnotated(((TmaRuleAnnotations)tmStack[tmHead - 1].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // rhsAssignment ::= identifier '=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // rhsAssignment ::= identifier '+=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // rhsOptional ::= rhsCast '?'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // rhsCast ::= rhsPrimary Las symref
				 lapg_gg.value = new TmaRhsCast(((ITmaRhsPart)tmStack[tmHead - 2].value), ((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // rhsUnordered ::= rhsPart '&' rhsPart
				 lapg_gg.value = new TmaRhsUnordered(((ITmaRhsPart)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // rhsPrimary ::= symref
				 lapg_gg.value = new TmaRhsSymbol(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // rhsPrimary ::= '(' rules ')'
				 lapg_gg.value = new TmaRhsNested(((List<TmaRule0>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // rhsPrimary ::= rhsPrimary '*'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // rhsPrimary ::= rhsPrimary '+'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // rhsAnnotations ::= annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(null, ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // rhsAnnotations ::= negative_la annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead - 1].value), ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // rhsAnnotations ::= negative_la
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 132:  // annotations ::= annotation_list
				 lapg_gg.value = new TmaAnnotations(((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 133:  // annotation_list ::= annotation
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 134:  // annotation_list ::= annotation_list annotation
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 135:  // annotation ::= '@' ID '=' expression
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 136:  // annotation ::= '@' ID
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead].value), ((ITmaExpression)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 137:  // annotation ::= '@' syntax_problem
				 lapg_gg.value = new TmaMapEntriesItem(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 138:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.value = new TmaNegativeLa(((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 139:  // negative_la_clause ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 140:  // negative_la_clause ::= negative_la_clause '|' symref
				 ((List<TmaSymref>)tmStack[tmHead - 2].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 141:  // expression ::= scon
				 lapg_gg.value = new TmaExpressionLiteral(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 142:  // expression ::= icon
				 lapg_gg.value = new TmaExpressionLiteral(((Integer)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 143:  // expression ::= Ltrue
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 144:  // expression ::= Lfalse
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 148:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.value = new TmaExpressionInstance(((TmaName)tmStack[tmHead - 3].value), ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 151:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.value = new TmaExpressionArray(((List<ITmaExpression>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 153:  // expression_list ::= expression
				 lapg_gg.value = new ArrayList(); ((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 154:  // expression_list ::= expression_list ',' expression
				 ((List<ITmaExpression>)tmStack[tmHead - 2].value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 155:  // map_entries ::= ID map_separator expression
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 156:  // map_entries ::= map_entries ',' ID map_separator expression
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 4].value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, tmStack[tmHead - 2].offset, lapg_gg.endoffset)); 
				break;
			case 160:  // name ::= qualified_id
				 lapg_gg.value = new TmaName(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 162:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 163:  // command ::= code
				 lapg_gg.value = new TmaCommand(source, tmStack[tmHead].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 164:  // syntax_problem ::= error
				 lapg_gg.value = new TmaSyntaxProblem(source, lapg_gg.offset, lapg_gg.endoffset); 
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
		return (TmaInput) parse(lexer, 0, 266);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 267);
	}
}
