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
	private static final int[] lapg_action = LapgLexer.unpack_int(224,
		"\uffff\uffff\uffff\uffff\ufffd\uffff\220\0\uffff\uffff\uffff\uffff\4\0\ufff5\uffff" +
		"\uffef\uffff\35\0\41\0\42\0\40\0\7\0\11\0\167\0\170\0\uffcb\uffff\171\0\172\0\uffff" +
		"\uffff\173\0\202\0\uffff\uffff\10\0\uffa1\uffff\uffff\uffff\67\0\5\0\uff99\uffff" +
		"\uffff\uffff\45\0\uffff\uffff\uff75\uffff\uffff\uffff\uffff\uffff\uff6b\uffff\36" +
		"\0\uff63\uffff\74\0\101\0\uffff\uffff\uff41\uffff\157\0\37\0\3\0\203\0\uff25\uffff" +
		"\uffff\uffff\213\0\uffff\uffff\uff1f\uffff\34\0\43\0\6\0\uffff\uffff\uffff\uffff" +
		"\66\0\2\0\22\0\uffff\uffff\24\0\25\0\20\0\21\0\uff19\uffff\16\0\17\0\23\0\26\0\30" +
		"\0\27\0\uffff\uffff\15\0\ufee7\uffff\uffff\uffff\uffff\uffff\102\0\103\0\104\0\uffff" +
		"\uffff\ufec1\uffff\163\0\uffff\uffff\ufe9f\uffff\75\0\76\0\ufe99\uffff\160\0\uffff" +
		"\uffff\201\0\ufe93\uffff\uffff\uffff\71\0\73\0\70\0\12\0\ufe77\uffff\uffff\uffff" +
		"\13\0\14\0\ufe45\uffff\ufe19\uffff\uffff\uffff\107\0\114\0\uffff\uffff\uffff\uffff" +
		"\ufe11\uffff\uffff\uffff\uffff\uffff\204\0\uffff\uffff\ufde7\uffff\uffff\uffff\214" +
		"\0\33\0\uffff\uffff\50\0\ufde1\uffff\112\0\113\0\106\0\uffff\uffff\105\0\115\0\162" +
		"\0\ufdb7\uffff\uffff\uffff\ufd7d\uffff\uffff\uffff\217\0\144\0\uffff\uffff\ufd53" +
		"\uffff\117\0\ufd4b\uffff\ufd21\uffff\ufcf5\uffff\ufcc5\uffff\uffff\uffff\uffff\uffff" +
		"\ufc8f\uffff\ufc6f\uffff\122\0\142\0\127\0\126\0\ufc51\uffff\210\0\211\0\207\0\uffff" +
		"\uffff\uffff\uffff\176\0\60\0\52\0\ufc27\uffff\110\0\uffff\uffff\132\0\uffff\uffff" +
		"\216\0\uffff\uffff\ufbff\uffff\165\0\uffff\uffff\77\0\ufbd3\uffff\ufba7\uffff\ufb6f" +
		"\uffff\uffff\uffff\ufb43\uffff\125\0\ufb25\uffff\135\0\124\0\uffff\uffff\151\0\152" +
		"\0\150\0\ufaf5\uffff\ufabb\uffff\130\0\ufa85\uffff\uffff\uffff\205\0\uffff\uffff" +
		"\uffff\uffff\54\0\ufa67\uffff\ufa41\uffff\215\0\145\0\uffff\uffff\uffff\uffff\164" +
		"\0\120\0\123\0\ufa0b\uffff\uf9d3\uffff\uffff\uffff\131\0\100\0\uffff\uffff\62\0\63" +
		"\0\64\0\65\0\uffff\uffff\56\0\57\0\uffff\uffff\166\0\uf9a3\uffff\206\0\61\0\uffff" +
		"\uffff\147\0\146\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = LapgLexer.unpack_short(1680,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\44\uffff\ufffe\1\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41" +
		"\uffff\2\uffff\10\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\1\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff" +
		"\2\uffff\4\uffff\5\uffff\21\uffff\35\uffff\36\uffff\37\uffff\22\177\uffff\ufffe\14" +
		"\uffff\17\72\22\72\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\21\uffff" +
		"\34\uffff\0\0\uffff\ufffe\13\uffff\11\10\20\10\23\10\uffff\ufffe\23\uffff\11\44\20" +
		"\44\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45" +
		"\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\34\uffff\0\1\uffff\ufffe" +
		"\34\uffff\1\156\41\156\42\156\43\156\44\156\45\156\46\156\47\156\50\156\51\156\52" +
		"\156\53\156\uffff\ufffe\17\uffff\22\200\uffff\ufffe\16\uffff\23\212\uffff\ufffe\1" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43" +
		"\uffff\42\uffff\41\uffff\16\uffff\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27" +
		"\uffff\30\uffff\32\uffff\33\uffff\34\uffff\25\31\uffff\ufffe\3\uffff\0\46\1\46\2" +
		"\46\10\46\21\46\34\46\41\46\42\46\43\46\44\46\45\46\46\46\47\46\50\46\51\46\52\46" +
		"\53\46\uffff\ufffe\13\uffff\1\161\20\161\23\161\34\161\41\161\42\161\43\161\44\161" +
		"\45\161\46\161\47\161\50\161\51\161\52\161\53\161\uffff\ufffe\23\uffff\11\44\uffff" +
		"\ufffe\23\uffff\11\44\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\25\174\uffff\ufffe\1" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43" +
		"\uffff\42\uffff\41\uffff\16\uffff\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27" +
		"\uffff\30\uffff\32\uffff\33\uffff\34\uffff\25\32\uffff\ufffe\14\uffff\0\47\1\47\2" +
		"\47\5\47\10\47\21\47\23\47\34\47\41\47\42\47\43\47\44\47\45\47\46\47\47\47\50\47" +
		"\51\47\52\47\53\47\55\47\uffff\ufffe\47\uffff\15\111\17\111\uffff\ufffe\1\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42" +
		"\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\121\15\121" +
		"\uffff\ufffe\17\uffff\25\175\uffff\ufffe\5\uffff\0\51\1\51\2\51\10\51\21\51\23\51" +
		"\34\51\41\51\42\51\43\51\44\51\45\51\46\51\47\51\50\51\51\51\52\51\53\51\55\51\uffff" +
		"\ufffe\13\uffff\20\uffff\1\11\2\11\10\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11" +
		"\32\11\33\11\34\11\40\11\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52" +
		"\11\53\11\55\11\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\121\25\121\uffff\ufffe\12\uffff\15\116\25\116\uffff" +
		"\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\43\uffff\42\uffff\41\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12" +
		"\121\15\121\25\121\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\121\15\121\25\121\uffff\ufffe\33\uffff\1\133\2\133" +
		"\10\133\12\133\15\133\23\133\24\133\25\133\34\133\40\133\41\133\42\133\43\133\44" +
		"\133\45\133\46\133\47\133\50\133\51\133\52\133\53\133\55\133\uffff\ufffe\30\uffff" +
		"\31\uffff\32\uffff\1\141\2\141\10\141\12\141\15\141\23\141\24\141\25\141\33\141\34" +
		"\141\40\141\41\141\42\141\43\141\44\141\45\141\46\141\47\141\50\141\51\141\52\141" +
		"\53\141\55\141\uffff\ufffe\34\uffff\1\153\23\153\41\153\42\153\43\153\44\153\45\153" +
		"\46\153\47\153\50\153\51\153\52\153\53\153\20\156\uffff\ufffe\34\uffff\1\155\23\155" +
		"\41\155\42\155\43\155\44\155\45\155\46\155\47\155\50\155\51\155\52\155\53\155\uffff" +
		"\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44" +
		"\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55" +
		"\uffff\12\121\15\121\uffff\ufffe\23\uffff\0\53\1\53\2\53\10\53\21\53\34\53\41\53" +
		"\42\53\43\53\44\53\45\53\46\53\47\53\50\53\51\53\52\53\53\53\55\53\uffff\ufffe\1" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43" +
		"\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\40\uffff\55" +
		"\uffff\12\121\25\121\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\121\15\121\25\121\uffff\ufffe\13\uffff\1\11\2\11\10" +
		"\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42\11" +
		"\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe\1\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff" +
		"\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\121\15" +
		"\121\25\121\uffff\ufffe\34\uffff\1\153\23\153\41\153\42\153\43\153\44\153\45\153" +
		"\46\153\47\153\50\153\51\153\52\153\53\153\uffff\ufffe\33\uffff\1\134\2\134\10\134" +
		"\12\134\15\134\23\134\24\134\25\134\34\134\40\134\41\134\42\134\43\134\44\134\45" +
		"\134\46\134\47\134\50\134\51\134\52\134\53\134\55\134\uffff\ufffe\13\uffff\20\uffff" +
		"\1\11\2\11\10\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11" +
		"\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe" +
		"\30\uffff\31\uffff\32\uffff\1\137\2\137\10\137\12\137\15\137\23\137\24\137\25\137" +
		"\33\137\34\137\40\137\41\137\42\137\43\137\44\137\45\137\46\137\47\137\50\137\51" +
		"\137\52\137\53\137\55\137\uffff\ufffe\34\uffff\1\154\23\154\41\154\42\154\43\154" +
		"\44\154\45\154\46\154\47\154\50\154\51\154\52\154\53\154\uffff\ufffe\55\uffff\0\55" +
		"\1\55\2\55\10\55\21\55\34\55\41\55\42\55\43\55\44\55\45\55\46\55\47\55\50\55\51\55" +
		"\52\55\53\55\uffff\ufffe\30\uffff\31\uffff\32\uffff\1\140\2\140\10\140\12\140\15" +
		"\140\23\140\24\140\25\140\33\140\34\140\40\140\41\140\42\140\43\140\44\140\45\140" +
		"\46\140\47\140\50\140\51\140\52\140\53\140\55\140\uffff\ufffe\13\uffff\1\11\2\11" +
		"\10\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42" +
		"\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe\33\143" +
		"\1\143\2\143\10\143\12\143\15\143\23\143\24\143\25\143\34\143\40\143\41\143\42\143" +
		"\43\143\44\143\45\143\46\143\47\143\50\143\51\143\52\143\53\143\55\143\uffff\ufffe" +
		"\30\uffff\31\uffff\32\uffff\1\136\2\136\10\136\12\136\15\136\23\136\24\136\25\136" +
		"\33\136\34\136\40\136\41\136\42\136\43\136\44\136\45\136\46\136\47\136\50\136\51" +
		"\136\52\136\53\136\55\136\uffff\ufffe");

	private static final short[] lapg_sym_goto = LapgLexer.unpack_short(107,
		"\0\2\62\106\111\121\131\131\131\144\147\151\162\166\172\177\207\216\235\243\272\303" +
		"\313\317\323\334\341\351\360\u0105\u010c\u0113\u011a\u011b\u014c\u017d\u01ae\u01df" +
		"\u0210\u0241\u0272\u02a3\u02d4\u0305\u0336\u0336\u0340\u0341\u0342\u0344\u034c\u0369" +
		"\u036d\u036f\u0373\u0376\u0378\u037c\u0380\u0384\u0385\u0386\u0387\u038b\u038c\u038e" +
		"\u0390\u0392\u0395\u0396\u0399\u039a\u039c\u039e\u03a1\u03a4\u03a8\u03ac\u03b1\u03ba" +
		"\u03c7\u03d0\u03d7\u03e4\u03f5\u03fe\u03ff\u0406\u0407\u0408\u040a\u040b\u040c\u0414" +
		"\u041e\u0432\u0434\u0435\u0439\u043a\u043b\u043c\u043d\u043e\u0446\u0447\u0448");

	private static final short[] lapg_sym_from = LapgLexer.unpack_short(1096,
		"\334\335\0\1\4\5\10\21\24\27\35\36\43\46\51\67\70\101\110\114\120\131\133\134\141" +
		"\152\153\154\165\173\201\202\210\211\214\224\230\231\237\241\244\250\252\253\261" +
		"\302\303\311\314\324\0\1\5\10\21\27\35\43\46\131\153\154\201\211\224\230\244\250" +
		"\252\314\27\112\113\1\21\27\36\131\153\230\314\1\21\27\131\153\167\230\314\10\35" +
		"\46\154\201\210\211\224\244\250\252\123\155\156\206\246\2\41\121\160\177\251\265" +
		"\273\307\31\145\160\273\147\152\205\271\36\63\101\110\141\32\36\57\101\110\141\147" +
		"\161\40\123\160\177\215\265\273\0\1\5\10\21\27\35\36\101\110\131\141\153\230\314" +
		"\32\36\60\101\110\141\7\36\44\62\101\110\124\127\141\154\201\210\211\214\224\235" +
		"\237\244\250\252\253\261\311\154\201\210\211\224\244\250\252\261\74\110\142\162\243" +
		"\246\321\324\36\101\110\141\36\101\110\141\36\101\110\141\213\266\277\326\331\213" +
		"\266\277\326\331\36\101\110\141\213\266\277\326\36\101\110\141\212\256\310\10\35" +
		"\36\46\52\101\110\141\154\201\210\211\216\217\224\244\250\252\254\261\270\1\21\27" +
		"\131\153\230\314\1\21\27\131\153\230\314\1\21\27\131\153\230\314\244\0\1\4\5\10\21" +
		"\24\27\35\36\43\46\51\67\70\101\110\114\120\131\133\134\141\152\153\154\165\173\200" +
		"\201\202\210\211\214\224\230\231\237\241\244\250\252\253\261\302\303\311\314\324" +
		"\0\1\4\5\10\21\24\27\35\36\43\46\51\67\70\101\110\114\120\131\133\134\141\152\153" +
		"\154\165\173\200\201\202\210\211\214\224\230\231\237\241\244\250\252\253\261\302" +
		"\303\311\314\324\0\1\4\5\10\21\24\27\35\36\42\43\46\51\67\70\101\110\114\120\131" +
		"\133\134\141\152\153\154\165\173\201\202\210\211\214\224\230\231\237\241\244\250" +
		"\252\253\261\302\303\311\314\324\0\1\4\5\10\21\24\27\35\36\42\43\46\51\67\70\101" +
		"\110\114\120\131\133\134\141\152\153\154\165\173\201\202\210\211\214\224\230\231" +
		"\237\241\244\250\252\253\261\302\303\311\314\324\0\1\4\5\10\21\24\27\35\36\42\43" +
		"\46\51\67\70\101\110\114\120\131\133\134\141\152\153\154\165\173\201\202\210\211" +
		"\214\224\230\231\237\241\244\250\252\253\261\302\303\311\314\324\0\1\4\5\10\21\24" +
		"\27\35\36\42\43\46\51\67\70\101\110\114\120\131\133\134\141\152\153\154\165\173\201" +
		"\202\210\211\214\224\230\231\237\241\244\250\252\253\261\302\303\311\314\324\0\1" +
		"\4\5\10\21\24\27\35\36\43\46\51\67\70\101\110\114\120\131\133\134\141\146\152\153" +
		"\154\165\173\201\202\210\211\214\224\230\231\237\241\244\250\252\253\261\302\303" +
		"\311\314\324\0\1\4\5\10\21\24\27\35\36\43\46\51\67\70\101\110\114\120\131\133\134" +
		"\141\152\153\154\165\173\201\202\210\211\214\224\230\231\237\241\244\250\252\253" +
		"\261\274\302\303\311\314\324\0\1\4\5\10\21\24\27\35\36\43\46\51\67\70\101\110\114" +
		"\120\131\133\134\141\152\153\154\165\173\201\202\210\211\214\224\230\231\237\241" +
		"\244\250\252\253\261\274\302\303\311\314\324\0\1\4\5\10\21\24\27\35\36\43\46\51\67" +
		"\70\101\110\114\120\131\133\134\141\152\153\154\165\173\201\202\210\211\214\224\230" +
		"\231\237\241\244\250\252\253\261\274\302\303\311\314\324\0\1\4\5\10\21\24\27\35\36" +
		"\43\46\51\67\70\101\110\114\120\131\133\134\141\152\153\154\165\173\201\202\210\211" +
		"\214\224\230\231\237\241\244\250\252\253\261\274\302\303\311\314\324\154\201\210" +
		"\211\224\244\250\252\261\276\0\0\0\5\0\4\5\10\35\46\51\70\1\21\27\114\120\131\152" +
		"\153\154\173\201\202\210\211\214\224\230\237\241\244\250\252\253\261\302\303\311" +
		"\314\324\7\44\124\127\36\101\36\101\110\141\27\112\113\0\5\0\5\10\35\0\5\10\35\0" +
		"\5\10\35\145\235\274\0\5\10\35\4\67\165\4\70\10\35\10\35\46\42\10\35\46\114\114\173" +
		"\120\302\154\201\224\154\201\224\154\201\224\250\154\201\224\250\154\201\210\224" +
		"\250\154\201\210\211\224\244\250\252\261\154\201\210\211\214\224\237\244\250\252" +
		"\253\261\311\154\201\210\211\224\244\250\252\261\10\35\46\154\201\224\250\10\35\46" +
		"\154\201\210\211\217\224\244\250\252\261\10\35\46\52\154\201\210\211\216\217\224" +
		"\244\250\252\254\261\270\154\201\210\211\224\244\250\252\261\202\1\21\27\131\153" +
		"\230\314\21\133\160\273\24\24\154\201\210\211\224\244\250\252\154\201\210\211\224" +
		"\244\250\252\261\276\0\1\5\10\21\27\35\43\46\131\153\154\201\211\224\230\244\250" +
		"\252\314\10\35\101\7\44\124\127\145\167\235\276\146\154\201\210\211\224\244\250\252" +
		"\133\21");

	private static final short[] lapg_sym_to = LapgLexer.unpack_short(1096,
		"\336\337\2\16\30\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\16" +
		"\16\177\135\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16" +
		"\16\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\64\64\64\17\17\17\74\17\17\17\17\20\20" +
		"\20\20\20\234\20\20\42\42\42\200\200\200\200\200\200\200\200\154\154\224\250\303" +
		"\27\113\153\225\237\237\311\225\311\67\165\226\226\172\174\247\313\75\134\75\75\75" +
		"\70\76\131\76\76\76\173\231\112\112\227\240\267\312\227\4\21\4\4\21\21\4\77\77\77" +
		"\21\77\21\21\21\71\100\132\100\100\100\36\101\36\133\101\101\36\36\101\201\201\201" +
		"\201\201\201\274\201\201\201\201\201\201\201\202\202\202\202\202\202\202\202\202" +
		"\140\143\164\232\301\304\330\331\102\102\102\102\103\103\103\103\104\104\104\104" +
		"\262\262\262\262\332\263\263\263\263\333\105\105\105\105\264\264\264\264\106\106" +
		"\106\106\261\261\261\43\43\107\43\43\107\107\107\43\43\43\43\43\43\43\43\43\43\43" +
		"\43\43\22\22\22\22\22\22\22\23\23\23\23\23\23\23\24\24\24\24\24\24\24\302\2\16\30" +
		"\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177\135\16" +
		"\241\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16\16\2\16" +
		"\30\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177\135" +
		"\16\242\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\114\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177" +
		"\135\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\115\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177" +
		"\135\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\116\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177" +
		"\135\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\117\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177" +
		"\135\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\170\16\16\177" +
		"\135\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177\135" +
		"\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\315\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177\135" +
		"\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\316\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177\135" +
		"\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\317\16\16\16\16\16\2" +
		"\16\30\2\41\16\61\16\41\73\121\30\30\135\30\73\73\16\16\16\160\163\73\16\16\177\135" +
		"\16\177\16\251\251\265\177\16\273\16\16\251\177\251\307\251\320\16\16\16\16\16\203" +
		"\203\203\203\203\203\203\203\203\203\334\5\6\34\7\31\7\44\44\124\127\31\25\25\25" +
		"\146\151\25\175\25\204\146\204\245\204\204\204\204\25\204\300\204\204\204\204\204" +
		"\151\325\204\25\175\37\37\37\37\110\141\111\111\144\144\65\145\65\10\35\11\11\45" +
		"\45\12\12\12\12\13\13\13\13\166\275\321\14\14\14\14\32\136\233\33\137\46\46\47\47" +
		"\125\120\50\50\50\147\150\236\152\324\205\243\271\206\206\206\207\207\207\305\210" +
		"\210\210\210\211\244\252\211\211\212\212\212\256\212\256\212\256\310\213\213\213" +
		"\213\266\213\277\213\213\213\266\213\326\214\214\253\253\214\253\214\253\253\51\51" +
		"\51\215\215\215\215\52\52\52\216\216\254\254\270\216\254\216\254\254\53\53\53\130" +
		"\53\53\53\53\130\53\53\53\53\53\130\53\130\217\217\217\217\217\217\217\217\217\246" +
		"\335\56\66\157\176\272\327\57\161\230\314\62\63\220\220\220\220\220\220\220\220\221" +
		"\221\221\221\221\221\221\221\221\322\15\26\15\54\26\26\54\122\126\26\26\222\222\257" +
		"\222\26\257\222\257\26\55\72\142\40\123\155\156\167\235\276\323\171\223\223\255\260" +
		"\223\260\223\306\162\60");

	private static final short[] lapg_rlen = LapgLexer.unpack_short(145,
		"\0\1\3\2\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\0\1\3\1\1\2\2\1\1\1\3\0\1\3\0" +
		"\1\0\1\0\1\0\1\10\2\3\1\1\1\1\3\1\3\1\1\3\1\2\2\5\6\1\1\1\1\4\4\1\3\0\1\2\1\2\1\1" +
		"\3\0\1\3\2\2\1\1\2\3\2\1\2\2\4\2\3\1\1\3\1\3\6\6\2\2\2\1\2\1\1\1\2\2\4\2\3\1\3\1" +
		"\1\1\1\1\0\1\5\0\1\3\1\1\3\3\5\1\1\1\1\1\3\3\2\1\1");

	private static final short[] lapg_rlex = LapgLexer.unpack_short(145,
		"\137\137\56\56\57\57\60\60\61\62\63\63\64\64\65\65\65\65\65\65\65\65\65\65\65\140" +
		"\140\65\66\67\67\67\70\70\70\71\141\141\72\142\142\143\143\144\144\145\145\72\73" +
		"\74\75\75\75\75\76\77\77\100\101\101\102\102\102\103\103\103\104\104\104\105\105" +
		"\106\106\146\146\107\110\110\111\112\112\147\147\113\113\113\113\113\114\114\114" +
		"\115\115\115\116\116\116\116\116\116\117\117\117\117\117\117\117\120\120\120\121" +
		"\122\122\123\123\123\124\125\125\126\126\126\126\126\150\150\126\151\151\126\126" +
		"\127\127\130\130\131\131\131\132\133\133\134\134\135\136");

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
		public static final int priority_kw = 68;
		public static final int directive = 69;
		public static final int inputs = 70;
		public static final int inputref = 71;
		public static final int references = 72;
		public static final int rules = 73;
		public static final int rule_list = 74;
		public static final int rule0 = 75;
		public static final int ruleprefix = 76;
		public static final int ruleparts = 77;
		public static final int rulepart = 78;
		public static final int rulesymref = 79;
		public static final int ruleannotations = 80;
		public static final int annotations = 81;
		public static final int annotation_list = 82;
		public static final int annotation = 83;
		public static final int negative_la = 84;
		public static final int negative_la_clause = 85;
		public static final int expression = 86;
		public static final int expression_list = 87;
		public static final int map_entries = 88;
		public static final int map_separator = 89;
		public static final int name = 90;
		public static final int qualified_id = 91;
		public static final int rule_attrs = 92;
		public static final int command = 93;
		public static final int syntax_problem = 94;
		public static final int grammar_partsopt = 95;
		public static final int type_part_listopt = 96;
		public static final int typeopt = 97;
		public static final int lexem_transitionopt = 98;
		public static final int iconopt = 99;
		public static final int lexem_attrsopt = 100;
		public static final int commandopt = 101;
		public static final int Lnoeoiopt = 102;
		public static final int rule_attrsopt = 103;
		public static final int map_entriesopt = 104;
		public static final int expression_listopt = 105;
	}

	public interface Rules {
		public static final int grammar_part_directive = 65;  // grammar_part ::= directive
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
			case 63:  // grammar_part ::= identifier typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 64:  // grammar_part ::= annotations identifier typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), ((AstAnnotations)lapg_m[lapg_head - 5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 65:  // grammar_part ::= directive
				 lapg_gg.sym = lapg_m[lapg_head].sym; 
				break;
			case 69:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head - 2].sym), ((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 72:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head - 2].sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 75:  // inputref ::= symref Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // references ::= symref
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 77:  // references ::= references symref
				 ((List<AstReference>)lapg_m[lapg_head - 1].sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 79:  // rule_list ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 80:  // rule_list ::= rule_list '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head - 2].sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 83:  // rule0 ::= ruleprefix ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 2].sym), ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // rule0 ::= ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rule0 ::= ruleprefix rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 1].sym), null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rule0 ::= rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 88:  // ruleprefix ::= annotations ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head - 1].sym), null); 
				break;
			case 89:  // ruleprefix ::= ruleannotations ID ':'
				 lapg_gg.sym = new AstRulePrefix(((AstRuleAnnotations)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 90:  // ruleprefix ::= ID ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 91:  // ruleparts ::= rulepart
				 lapg_gg.sym = new ArrayList<AstRulePart>(); ((List<AstRulePart>)lapg_gg.sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 92:  // ruleparts ::= ruleparts rulepart
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 93:  // ruleparts ::= ruleparts syntax_problem
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 94:  // rulepart ::= ruleannotations ID '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // rulepart ::= ruleannotations rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // rulepart ::= ID '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // rulepart ::= rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // rulepart ::= rulepart '&' rulepart
				 lapg_gg.sym = new AstUnorderedRulePart(((AstRulePart)lapg_m[lapg_head - 2].sym), ((AstRulePart)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // rulesymref ::= symref
				 lapg_gg.sym = new AstRuleDefaultSymbolRef(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // rulesymref ::= '(' rules ')'
				 lapg_gg.sym = new AstRuleNestedNonTerm(((List<AstRule>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // rulesymref ::= '(' ruleparts Lseparator references ')' '+'
				 lapg_gg.sym = new AstRuleNestedListWithSeparator(((List<AstRulePart>)lapg_m[lapg_head - 4].sym), ((List<AstReference>)lapg_m[lapg_head - 2].sym), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // rulesymref ::= '(' ruleparts Lseparator references ')' '*'
				 lapg_gg.sym = new AstRuleNestedListWithSeparator(((List<AstRulePart>)lapg_m[lapg_head - 4].sym), ((List<AstReference>)lapg_m[lapg_head - 2].sym), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // rulesymref ::= rulesymref '?'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // rulesymref ::= rulesymref '*'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // rulesymref ::= rulesymref '+'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 107:  // ruleannotations ::= annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // ruleannotations ::= negative_la annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head - 1].sym), ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // ruleannotations ::= negative_la
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // annotations ::= annotation_list
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // annotation_list ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 112:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 113:  // annotation ::= '@' ID
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // annotation ::= '@' ID '=' expression
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 116:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.sym = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // negative_la_clause ::= symref
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 118:  // negative_la_clause ::= negative_la_clause '|' symref
				 ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 119:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head - 3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 132:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 133:  // map_entries ::= ID map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 134:  // map_entries ::= map_entries ',' ID map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 2].offset, lapg_gg.endoffset)); 
				break;
			case 138:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 140:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 2].sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 141:  // rule_attrs ::= '%' Lprio symref
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 142:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 143:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 144:  // syntax_problem ::= error
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
		return (AstRoot) parse(lexer, 0, 222);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 223);
	}
}
