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
	private static final int[] tmAction = TMLexer.unpack_int(279,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\241\0\242\0\uffcd\uffff\256\0\22" +
		"\0\243\0\244\0\uffff\uffff\227\0\226\0\240\0\253\0\uff93\uffff\uff8b\uffff\uff7f" +
		"\uffff\234\0\uff4f\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\6\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\uffff\uffff\237\0\uff49\uffff\uffff" +
		"\uffff\uffff\uffff\11\0\254\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uff1d" +
		"\uffff\2\0\20\0\233\0\uffff\uffff\uff17\uffff\uffff\uffff\uff11\uffff\uffff\uffff" +
		"\16\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\4\0\250\0\251\0\247\0\uffff" +
		"\uffff\uffff\uffff\232\0\uffff\uffff\14\0\15\0\uffff\uffff\uff0b\uffff\uff03\uffff" +
		"\ufefd\uffff\45\0\51\0\52\0\50\0\17\0\uffff\uffff\245\0\uffff\uffff\10\0\21\0\ufecb" +
		"\uffff\77\0\uffff\uffff\uffff\uffff\uffff\uffff\54\0\uffff\uffff\46\0\47\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\ufec3\uffff\104\0\107\0\110\0\uffff\uffff\213\0\ufe91" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\100\0\44\0\53\0\uffff\uffff\35\0\36\0" +
		"\31\0\32\0\uffff\uffff\27\0\30\0\34\0\37\0\41\0\40\0\33\0\uffff\uffff\26\0\ufe63" +
		"\uffff\uffff\uffff\125\0\126\0\127\0\uffff\uffff\ufe2f\uffff\222\0\ufdfd\uffff\uffff" +
		"\uffff\uffff\uffff\ufdc1\uffff\ufd95\uffff\123\0\124\0\uffff\uffff\105\0\106\0\uffff" +
		"\uffff\212\0\246\0\101\0\102\0\76\0\23\0\43\0\uffff\uffff\24\0\25\0\ufd69\uffff\ufd2f" +
		"\uffff\132\0\uffff\uffff\136\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufd27" +
		"\uffff\uffff\uffff\ufceb\uffff\255\0\uffff\uffff\204\0\uffff\uffff\143\0\ufc9d\uffff" +
		"\145\0\ufc63\uffff\ufc27\uffff\164\0\167\0\171\0\ufbe7\uffff\165\0\ufba5\uffff\ufb61" +
		"\uffff\uffff\uffff\ufb19\uffff\166\0\153\0\ufaeb\uffff\152\0\ufae3\uffff\ufab5\uffff" +
		"\115\0\116\0\121\0\122\0\ufa89\uffff\ufa4d\uffff\uffff\uffff\42\0\uffff\uffff\56" +
		"\0\ufa11\uffff\134\0\133\0\uffff\uffff\130\0\137\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uf9d9\uffff\224\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\114\0\uf99d\uffff\150\0\uf961\uffff\163\0\151\0\uffff\uffff" +
		"\175\0\uffff\uffff\210\0\211\0\170\0\uf921\uffff\uf8f3\uffff\120\0\uffff\uffff\uffff" +
		"\uffff\uf8b5\uffff\70\0\60\0\uf879\uffff\131\0\220\0\157\0\160\0\156\0\154\0\uffff" +
		"\uffff\205\0\uffff\uffff\uffff\uffff\225\0\uffff\uffff\172\0\uf843\uffff\173\0\147" +
		"\0\uf7fb\uffff\177\0\200\0\142\0\113\0\112\0\uffff\uffff\uffff\uffff\62\0\uf7bb\uffff" +
		"\155\0\uffff\uffff\223\0\111\0\72\0\73\0\74\0\75\0\uffff\uffff\64\0\66\0\uffff\uffff" +
		"\71\0\207\0\206\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(2166,
		"\10\1\36\1\37\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1" +
		"\62\1\63\1\64\1\65\1\66\1\67\1\70\1\uffff\ufffe\2\uffff\3\uffff\20\uffff\36\uffff" +
		"\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\40\uffff\41\uffff\42\uffff\21\236\uffff\ufffe\22\uffff\66\uffff" +
		"\14\7\uffff\ufffe\15\uffff\14\252\22\252\24\252\66\252\uffff\ufffe\45\uffff\10\3" +
		"\36\3\37\3\46\3\47\3\50\3\51\3\52\3\53\3\54\3\55\3\56\3\57\3\60\3\61\3\62\3\63\3" +
		"\64\3\65\3\66\3\67\3\70\3\uffff\ufffe\16\uffff\21\235\uffff\ufffe\37\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\24\231\uffff\ufffe\10\uffff\0\5\uffff\ufffe\16\uffff\24\230\uffff\ufffe\66\uffff" +
		"\14\7\uffff\ufffe\12\uffff\17\21\22\21\uffff\ufffe\22\uffff\17\55\uffff\ufffe\20" +
		"\uffff\36\uffff\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62" +
		"\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\0\12\10\12\uffff\ufffe\13\uffff\16\103\21\103\uffff" +
		"\ufffe\6\uffff\35\uffff\36\uffff\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\0\13\uffff\ufffe\35\uffff\21" +
		"\217\37\217\46\217\47\217\50\217\51\217\52\217\53\217\54\217\55\217\56\217\57\217" +
		"\60\217\61\217\62\217\63\217\64\217\65\217\66\217\67\217\70\217\uffff\ufffe\1\uffff" +
		"\0\67\10\67\20\67\36\67\37\67\46\67\47\67\50\67\51\67\52\67\53\67\54\67\55\67\56" +
		"\67\57\67\60\67\61\67\62\67\63\67\64\67\65\67\66\67\67\67\70\67\uffff\ufffe\73\uffff" +
		"\21\221\22\221\35\221\37\221\46\221\47\221\50\221\51\221\52\221\53\221\54\221\55" +
		"\221\56\221\57\221\60\221\61\221\62\221\63\221\64\221\65\221\66\221\67\221\70\221" +
		"\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\70\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\72\uffff\11\146\14\146\uffff\ufffe\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\7\117\uffff\ufffe\37\uffff" +
		"\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\7\117\uffff\ufffe\13\uffff\0\57\3\57\10\57\20\57\22\57\36\57\37\57\46\57" +
		"\47\57\50\57\51\57\52\57\53\57\54\57\55\57\56\57\57\57\60\57\61\57\62\57\63\57\64" +
		"\57\65\57\66\57\67\57\70\57\72\57\uffff\ufffe\56\uffff\14\135\16\135\uffff\ufffe" +
		"\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\70\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\72\uffff" +
		"\11\146\24\146\uffff\ufffe\12\21\17\21\32\21\6\22\11\22\14\22\22\22\23\22\24\22\30" +
		"\22\31\22\33\22\34\22\35\22\36\22\37\22\43\22\44\22\46\22\47\22\50\22\51\22\52\22" +
		"\53\22\54\22\55\22\56\22\57\22\60\22\61\22\62\22\63\22\64\22\65\22\66\22\67\22\70" +
		"\22\72\22\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\37\uffff\70\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\72\uffff" +
		"\11\146\14\146\24\146\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37" +
		"\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\72\uffff\11\146\14\146\24\146\uffff\ufffe\34\uffff\6\161\11\161\14" +
		"\161\22\161\23\161\24\161\35\161\36\161\37\161\43\161\46\161\47\161\50\161\51\161" +
		"\52\161\53\161\54\161\55\161\56\161\57\161\60\161\61\161\62\161\63\161\64\161\65" +
		"\161\66\161\67\161\70\161\72\161\uffff\ufffe\33\uffff\6\174\11\174\14\174\22\174" +
		"\23\174\24\174\34\174\35\174\36\174\37\174\43\174\46\174\47\174\50\174\51\174\52" +
		"\174\53\174\54\174\55\174\56\174\57\174\60\174\61\174\62\174\63\174\64\174\65\174" +
		"\66\174\67\174\70\174\72\174\uffff\ufffe\44\uffff\6\176\11\176\14\176\22\176\23\176" +
		"\24\176\33\176\34\176\35\176\36\176\37\176\43\176\46\176\47\176\50\176\51\176\52" +
		"\176\53\176\54\176\55\176\56\176\57\176\60\176\61\176\62\176\63\176\64\176\65\176" +
		"\66\176\67\176\70\176\72\176\uffff\ufffe\30\uffff\31\uffff\6\202\11\202\14\202\22" +
		"\202\23\202\24\202\33\202\34\202\35\202\36\202\37\202\43\202\44\202\46\202\47\202" +
		"\50\202\51\202\52\202\53\202\54\202\55\202\56\202\57\202\60\202\61\202\62\202\63" +
		"\202\64\202\65\202\66\202\67\202\70\202\72\202\uffff\ufffe\35\uffff\22\216\37\216" +
		"\46\216\47\216\50\216\51\216\52\216\53\216\54\216\55\216\56\216\57\216\60\216\61" +
		"\216\62\216\63\216\64\216\65\216\66\216\67\216\70\216\uffff\ufffe\11\uffff\14\144" +
		"\24\144\uffff\ufffe\35\uffff\22\214\37\214\46\214\47\214\50\214\51\214\52\214\53" +
		"\214\54\214\55\214\56\214\57\214\60\214\61\214\62\214\63\214\64\214\65\214\66\214" +
		"\67\214\70\214\uffff\ufffe\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\7\117\uffff\ufffe\6\uffff\20\uffff" +
		"\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\72\uffff\11\146\14\146\uffff" +
		"\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\70\uffff\67" +
		"\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56" +
		"\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\72" +
		"\uffff\11\146\14\146\uffff\ufffe\3\uffff\0\61\10\61\20\61\22\61\36\61\37\61\46\61" +
		"\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64" +
		"\61\65\61\66\61\67\61\70\61\72\61\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff" +
		"\36\uffff\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\43\uffff\72\uffff\11\146\24\146\uffff\ufffe\6\uffff\22" +
		"\uffff\23\uffff\35\uffff\36\uffff\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\72\uffff\11\146\14\146\24\146" +
		"\uffff\ufffe\34\uffff\6\162\11\162\14\162\22\162\23\162\24\162\35\162\36\162\37\162" +
		"\43\162\46\162\47\162\50\162\51\162\52\162\53\162\54\162\55\162\56\162\57\162\60" +
		"\162\61\162\62\162\63\162\64\162\65\162\66\162\67\162\70\162\72\162\uffff\ufffe\35" +
		"\uffff\22\215\37\215\46\215\47\215\50\215\51\215\52\215\53\215\54\215\55\215\56\215" +
		"\57\215\60\215\61\215\62\215\63\215\64\215\65\215\66\215\67\215\70\215\uffff\ufffe" +
		"\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\70\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\72\uffff" +
		"\11\146\14\146\24\146\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36" +
		"\uffff\37\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61" +
		"\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\72\uffff\11\146\14\146\uffff\ufffe\22\uffff\0\63\10\63\20" +
		"\63\36\63\37\63\46\63\47\63\50\63\51\63\52\63\53\63\54\63\55\63\56\63\57\63\60\63" +
		"\61\63\62\63\63\63\64\63\65\63\66\63\67\63\70\63\72\63\uffff\ufffe\30\uffff\31\uffff" +
		"\6\203\11\203\14\203\22\203\23\203\24\203\33\203\34\203\35\203\36\203\37\203\43\203" +
		"\44\203\46\203\47\203\50\203\51\203\52\203\53\203\54\203\55\203\56\203\57\203\60" +
		"\203\61\203\62\203\63\203\64\203\65\203\66\203\67\203\70\203\72\203\uffff\ufffe\34" +
		"\201\6\201\11\201\14\201\22\201\23\201\24\201\35\201\36\201\37\201\43\201\46\201" +
		"\47\201\50\201\51\201\52\201\53\201\54\201\55\201\56\201\57\201\60\201\61\201\62" +
		"\201\63\201\64\201\65\201\66\201\67\201\70\201\72\201\uffff\ufffe\72\uffff\0\65\10" +
		"\65\20\65\36\65\37\65\46\65\47\65\50\65\51\65\52\65\53\65\54\65\55\65\56\65\57\65" +
		"\60\65\61\65\62\65\63\65\64\65\65\65\66\65\67\65\70\65\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(136,
		"\0\2\4\17\31\31\31\45\51\53\55\62\66\100\105\115\122\145\156\210\223\236\237\243" +
		"\247\256\261\262\267\276\324\350\u0125\u012d\u0135\u013c\u013d\u013e\u013f\u017e" +
		"\u01bc\u01fa\u0239\u0277\u02b5\u02f3\u0331\u036f\u03ad\u03ee\u042d\u046c\u04aa\u04e8" +
		"\u0526\u0565\u05a3\u05e1\u05e1\u05ed\u05ee\u05ef\u05f0\u05f1\u05f2\u05f4\u05f5\u05f6" +
		"\u0610\u0632\u0635\u0637\u063b\u063d\u063e\u0640\u0642\u0644\u0645\u0646\u0647\u0649" +
		"\u064b\u064d\u064e\u0650\u0652\u0654\u0655\u0657\u0659\u065b\u065b\u0660\u0666\u066c" +
		"\u0676\u067d\u0688\u0693\u069f\u06ad\u06bb\u06c6\u06d4\u06e3\u06ee\u06f1\u0703\u070e" +
		"\u0715\u071d\u071e\u0720\u0723\u0726\u0732\u0746\u0747\u0748\u074a\u074b\u074c\u074d" +
		"\u074e\u074f\u0750\u0753\u0754\u0759\u0763\u0772\u0773\u0774\u0775\u0776");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1910,
		"\u0113\u0114\123\170\1\6\34\37\47\66\74\124\143\233\335\1\6\37\43\66\74\143\233\303" +
		"\335\113\134\200\236\246\250\275\276\317\326\342\346\133\207\212\277\36\52\265\321" +
		"\51\56\103\115\242\56\115\120\225\32\46\64\77\230\232\244\344\345\u0100\21\124\156" +
		"\166\222\24\57\122\124\156\166\222\230\56\115\126\242\365\1\6\37\65\66\74\105\124" +
		"\143\156\166\200\222\233\236\275\276\342\346\25\122\124\156\166\222\314\315\360\20" +
		"\26\30\104\124\133\156\166\200\212\222\236\246\250\261\275\276\317\322\323\324\326" +
		"\333\342\346\351\200\236\246\250\275\276\317\326\333\342\346\42\60\62\151\156\166" +
		"\222\316\321\u0105\u010c\311\124\156\166\222\124\156\166\222\124\156\166\222\260" +
		"\367\u010f\260\367\u010f\242\124\156\166\222\255\124\156\166\222\251\330\372\113" +
		"\124\134\142\156\166\200\222\235\236\246\250\262\267\275\276\317\326\333\341\342" +
		"\346\1\6\36\37\66\74\105\132\134\143\200\233\236\250\275\276\317\326\342\346\1\2" +
		"\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\132\134\140\143\144\145\156\166" +
		"\171\175\200\202\203\204\222\232\233\235\236\237\246\250\261\270\275\276\301\306" +
		"\312\313\315\317\322\323\324\326\333\335\342\346\362\363\u0105\1\6\37\66\74\143\233" +
		"\335\1\6\37\66\74\143\233\335\1\6\37\66\74\143\233\317\257\22\1\2\6\13\27\33\34\36" +
		"\37\41\65\66\74\75\102\105\113\124\132\133\134\140\143\144\145\156\166\171\175\200" +
		"\202\203\204\212\222\232\233\235\236\237\246\250\261\270\275\276\301\306\312\313" +
		"\315\317\322\323\324\326\333\335\342\346\362\363\u0105\1\2\6\13\27\33\34\36\37\41" +
		"\65\66\74\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203" +
		"\204\222\232\233\234\235\236\237\246\250\261\270\275\276\301\306\312\313\315\317" +
		"\322\323\324\326\333\335\342\346\362\363\u0105\1\2\6\13\27\33\34\36\37\41\65\66\74" +
		"\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232" +
		"\233\234\235\236\237\246\250\261\270\275\276\301\306\312\313\315\317\322\323\324" +
		"\326\333\335\342\346\362\363\u0105\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105" +
		"\113\124\132\133\134\140\143\144\145\156\166\171\175\200\202\203\204\212\222\232" +
		"\233\235\236\237\246\250\261\270\275\276\301\306\312\313\315\317\322\323\324\326" +
		"\333\335\342\346\362\363\u0105\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113" +
		"\124\131\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235" +
		"\236\237\246\250\261\270\275\276\301\306\312\313\315\317\322\323\324\326\333\335" +
		"\342\346\362\363\u0105\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\131" +
		"\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236\237" +
		"\246\250\261\270\275\276\301\306\312\313\315\317\322\323\324\326\333\335\342\346" +
		"\362\363\u0105\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\131\132\134" +
		"\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236\237\246\250" +
		"\261\270\275\276\301\306\312\313\315\317\322\323\324\326\333\335\342\346\362\363" +
		"\u0105\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\131\132\134\140\143" +
		"\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236\237\246\250\261\270" +
		"\275\276\301\306\312\313\315\317\322\323\324\326\333\335\342\346\362\363\u0105\1" +
		"\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\132\134\140\143\144\145\156" +
		"\166\171\175\200\202\203\204\222\226\232\233\235\236\237\246\250\261\270\275\276" +
		"\301\306\312\313\315\317\322\323\324\326\333\335\342\346\362\363\u0105\1\2\6\13\27" +
		"\33\34\36\37\41\65\66\74\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175" +
		"\200\202\203\204\222\232\233\235\236\237\246\250\261\270\275\276\301\306\312\313" +
		"\315\317\322\323\324\326\333\335\342\346\362\363\u0101\u0105\1\2\6\13\27\33\34\36" +
		"\37\41\65\66\74\75\102\105\113\124\132\133\134\140\143\144\145\156\166\171\175\200" +
		"\201\202\203\204\212\222\232\233\235\236\237\246\250\261\270\275\276\301\306\312" +
		"\313\315\317\322\323\324\326\333\335\342\346\362\363\u0101\u0105\1\2\6\13\27\33\34" +
		"\36\37\41\65\66\74\75\102\105\113\124\132\133\134\140\143\144\145\156\166\171\175" +
		"\200\202\203\204\212\222\232\233\235\236\237\246\250\261\270\275\276\301\306\312" +
		"\313\315\317\322\323\324\326\333\335\342\346\362\363\u0105\1\2\6\13\27\33\34\36\37" +
		"\41\65\66\74\75\102\105\113\124\132\133\134\140\143\144\145\156\166\171\175\200\202" +
		"\203\204\212\222\232\233\235\236\237\246\250\261\270\275\276\301\306\312\313\315" +
		"\317\322\323\324\326\333\335\342\346\362\363\u0105\1\2\6\13\27\33\34\36\37\41\65" +
		"\66\74\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204" +
		"\222\232\233\235\236\237\246\250\261\270\275\276\301\306\312\313\315\317\322\323" +
		"\324\326\333\335\342\346\362\363\u0101\u0105\1\2\6\13\27\33\34\36\37\41\65\66\74" +
		"\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232" +
		"\233\235\236\237\246\250\261\270\275\276\301\306\312\313\315\317\322\323\324\326" +
		"\333\335\342\346\362\363\u0101\u0105\0\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102" +
		"\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233" +
		"\235\236\237\246\250\261\270\275\276\301\306\312\313\315\317\322\323\324\326\333" +
		"\335\342\346\362\363\u0105\1\2\6\13\20\27\33\34\36\37\41\61\65\66\74\75\102\105\113" +
		"\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236" +
		"\237\246\250\261\270\275\276\301\306\312\313\315\317\322\323\324\326\333\335\342" +
		"\346\362\363\u0105\1\2\6\13\27\33\34\36\37\41\50\65\66\74\75\102\105\113\124\132" +
		"\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236\237\246" +
		"\250\261\270\275\276\301\306\312\313\315\317\322\323\324\326\333\335\342\346\362" +
		"\363\u0105\1\2\6\13\27\33\34\36\37\41\65\66\67\74\75\102\105\113\124\132\134\140" +
		"\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236\237\246\250\261" +
		"\270\275\276\301\306\312\313\315\317\322\323\324\326\333\335\342\346\362\363\u0105" +
		"\200\236\246\250\275\276\317\326\333\342\346\u0103\176\0\0\36\52\20\61\22\36\65\102" +
		"\105\113\134\140\145\200\203\204\235\236\246\250\261\270\275\276\315\317\322\324" +
		"\326\333\342\346\1\6\37\66\74\143\171\175\200\202\232\233\236\237\246\250\261\275" +
		"\276\306\312\313\317\322\323\324\326\333\335\342\346\362\363\u0105\104\133\212\124" +
		"\156\124\156\166\222\123\170\65\65\105\65\105\65\105\225\351\u0101\65\105\144\301" +
		"\102\145\113\113\134\113\134\133\212\131\113\134\171\306\175\362\200\236\275\276" +
		"\346\200\236\275\276\342\346\200\236\275\276\342\346\200\236\246\250\275\276\317" +
		"\326\342\346\200\236\246\275\276\342\346\200\236\246\250\275\276\317\326\333\342" +
		"\346\200\236\246\250\275\276\317\326\333\342\346\200\236\246\250\261\275\276\317" +
		"\326\333\342\346\200\236\246\250\261\275\276\317\322\324\326\333\342\346\200\236" +
		"\246\250\261\275\276\317\322\324\326\333\342\346\200\236\246\250\275\276\317\326" +
		"\333\342\346\200\236\246\250\261\275\276\317\322\324\326\333\342\346\200\236\246" +
		"\250\261\275\276\317\322\323\324\326\333\342\346\200\236\246\250\275\276\317\326" +
		"\333\342\346\113\134\235\113\134\142\200\235\236\246\250\262\267\275\276\317\326" +
		"\333\341\342\346\200\236\246\250\275\276\317\326\333\342\346\1\6\37\66\74\143\233" +
		"\1\6\37\66\74\143\233\335\41\56\115\2\13\27\2\13\27\200\236\246\250\275\276\317\326" +
		"\333\342\346\u0103\1\6\36\37\66\74\105\132\134\143\200\233\236\250\275\276\317\326" +
		"\342\346\3\22\20\61\104\225\303\351\u0103\102\203\204\270\171\200\236\275\276\346" +
		"\200\236\246\250\275\276\317\326\342\346\113\134\200\235\236\246\250\262\275\276" +
		"\317\326\333\342\346\237\41\6\6");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1910,
		"\u0115\u0116\147\147\4\4\46\4\64\4\4\151\4\4\4\5\5\5\62\5\5\5\5\350\5\131\131\234" +
		"\234\234\234\234\234\234\234\234\234\200\275\276\346\50\67\342\363\66\71\123\71\322" +
		"\72\72\144\301\44\63\101\116\305\307\325\376\377\u0107\33\152\152\152\152\37\75\145" +
		"\153\153\153\153\306\73\73\170\323\323\6\6\6\102\6\6\102\154\6\154\154\235\154\6" +
		"\235\235\235\235\235\40\146\155\155\155\155\356\357\u0104\27\41\43\124\156\124\156" +
		"\156\236\124\156\236\236\236\236\236\236\236\236\236\236\236\236\236\236\u0101\237" +
		"\237\237\237\237\237\237\237\237\237\237\61\76\100\220\221\223\300\361\364\u010f" +
		"\u0110\353\157\157\157\157\160\160\160\160\161\161\161\161\336\336\u0111\337\337" +
		"\u0112\324\162\162\162\162\334\163\163\163\163\333\333\333\132\164\132\132\164\164" +
		"\132\164\132\132\132\132\132\132\132\132\132\132\132\132\132\132\7\7\7\7\7\7\7\7" +
		"\7\7\7\7\7\7\7\7\7\7\7\7\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117" +
		"\165\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117\240\10\240" +
		"\240\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10" +
		"\11\11\11\11\11\11\11\11\12\12\12\12\12\12\12\12\13\13\13\13\13\13\13\362\335\34" +
		"\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\201\117\117\10\215" +
		"\117\165\165\10\10\240\10\117\117\201\165\10\10\117\240\10\240\240\240\117\240\240" +
		"\215\10\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47" +
		"\51\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240" +
		"\10\117\117\165\10\10\312\117\240\10\240\240\240\117\240\240\215\10\10\10\117\240" +
		"\240\10\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115" +
		"\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\313" +
		"\117\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240" +
		"\240\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\202" +
		"\117\117\10\215\117\165\165\10\10\240\10\117\117\202\165\10\10\117\240\10\240\240" +
		"\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17" +
		"\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\171\176\117\117\10\215\117" +
		"\165\165\10\10\240\10\117\117\165\10\10\117\240\10\240\240\240\117\240\240\215\10" +
		"\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47\51\10" +
		"\56\103\10\10\115\117\103\117\165\172\176\117\117\10\215\117\165\165\10\10\240\10" +
		"\117\117\165\10\10\117\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10" +
		"\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117" +
		"\103\117\165\173\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117" +
		"\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240" +
		"\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\174\176\117" +
		"\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117\240\10\240\240\240\117" +
		"\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17\10\17" +
		"\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10" +
		"\10\240\10\117\117\165\304\10\10\117\240\10\240\240\240\117\240\240\215\10\10\10" +
		"\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47\51\10\56\103" +
		"\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165" +
		"\10\10\117\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240" +
		"\10\240\240\10\10\u0108\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117" +
		"\165\176\203\117\117\10\215\117\165\165\10\10\240\270\10\117\117\203\165\10\10\117" +
		"\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240" +
		"\10\10\u0109\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\204" +
		"\117\117\10\215\117\165\165\10\10\240\10\117\117\204\165\10\10\117\240\10\240\240" +
		"\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17" +
		"\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\205\117\117\10\215\117" +
		"\165\165\10\10\240\10\117\117\205\165\10\10\117\240\10\240\240\240\117\240\240\215" +
		"\10\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47\51" +
		"\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10" +
		"\117\117\165\10\10\117\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10" +
		"\240\240\240\10\240\240\10\10\u010a\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115" +
		"\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117" +
		"\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240" +
		"\10\10\u010b\10\2\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176" +
		"\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117\240\10\240\240\240" +
		"\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17\10" +
		"\17\30\17\45\47\51\10\56\30\103\10\10\115\117\103\117\165\176\117\117\10\215\117" +
		"\165\165\10\10\240\10\117\117\165\10\10\117\240\10\240\240\240\117\240\240\215\10" +
		"\10\10\117\240\240\10\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47\51\10" +
		"\56\65\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10" +
		"\117\117\165\10\10\117\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10" +
		"\240\240\240\10\240\240\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\113\10\115" +
		"\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117" +
		"\240\10\240\240\240\117\240\240\215\10\10\10\117\240\240\10\240\240\240\10\240\240" +
		"\10\10\10\241\241\241\241\241\241\241\241\241\241\241\241\233\u0113\3\52\70\31\31" +
		"\35\53\104\120\104\133\133\212\120\242\272\272\314\242\242\242\242\272\242\242\360" +
		"\242\365\365\242\242\242\242\14\14\14\14\14\14\226\231\243\271\310\14\243\320\243" +
		"\243\243\243\243\226\354\355\243\243\243\243\243\243\373\243\243\231\u0106\310\125" +
		"\206\206\166\222\167\167\224\224\150\225\105\106\127\107\107\110\110\302\u0102\u010c" +
		"\111\111\216\347\121\217\134\135\210\136\136\207\277\175\137\137\227\352\232\u0105" +
		"\244\316\344\345\u0100\245\245\245\245\375\245\246\246\246\246\246\246\247\247\247" +
		"\247\247\247\247\247\247\247\250\317\326\250\250\250\250\251\251\251\330\251\251" +
		"\330\330\372\251\251\252\252\252\252\252\252\252\252\252\252\252\253\253\253\253" +
		"\340\253\253\253\253\253\253\253\254\254\254\254\254\254\254\254\366\370\254\254" +
		"\254\254\255\255\255\255\255\255\255\255\255\255\255\255\255\255\256\256\256\256" +
		"\256\256\256\256\256\256\256\257\257\257\257\257\257\257\257\257\257\257\257\257" +
		"\257\260\260\260\260\260\260\260\260\260\367\260\260\260\260\260\261\261\261\261" +
		"\261\261\261\261\261\261\261\140\140\315\141\141\213\141\141\141\141\141\141\213" +
		"\141\141\141\141\141\213\141\141\262\262\262\262\262\262\262\262\262\262\262\u0114" +
		"\23\55\112\114\214\311\15\15\15\15\15\15\15\374\57\74\143\20\26\42\21\21\21\263\263" +
		"\263\263\263\263\263\263\263\263\263\u010d\16\16\54\16\16\16\130\177\211\16\264\16" +
		"\264\331\264\264\331\331\264\264\22\36\32\77\126\303\351\u0103\u010e\122\273\274" +
		"\343\230\265\265\265\265\265\266\266\327\332\266\266\332\371\266\266\142\142\267" +
		"\142\267\267\267\341\267\267\267\267\267\267\267\321\60\24\25");

	private static final short[] lapg_rlen = TMLexer.unpack_short(175,
		"\2\0\2\0\5\4\1\0\7\4\3\3\4\4\3\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\3\2\1\1\2\2" +
		"\1\1\1\3\1\0\1\0\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\6\5\5\4\2\1\0" +
		"\3\2\2\1\1\1\1\1\4\3\1\4\2\1\1\2\1\3\3\1\1\1\0\3\2\2\1\1\3\4\3\3\3\1\2\2\1\1\1\1" +
		"\2\1\3\3\1\2\1\3\3\3\1\3\1\3\6\6\2\2\2\1\1\2\1\1\5\2\2\3\1\3\1\1\1\0\5\3\1\1\0\3" +
		"\1\1\1\1\1\3\5\1\1\1\1\1\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(175,
		"\165\165\166\166\74\74\167\167\75\75\76\77\100\101\101\102\102\103\104\105\105\106" +
		"\106\107\107\107\107\107\107\107\107\107\107\107\107\107\110\111\111\111\112\112" +
		"\112\113\170\170\171\171\172\172\173\173\174\174\114\114\115\116\117\117\117\117" +
		"\175\175\120\121\122\122\123\123\123\124\124\125\125\125\125\126\176\176\126\126" +
		"\126\126\126\127\127\127\130\177\177\130\131\131\132\132\133\133\200\200\134\201" +
		"\201\135\135\135\135\135\136\136\136\137\137\140\140\140\141\141\141\142\142\143" +
		"\143\143\144\144\145\145\145\146\147\147\150\150\150\150\150\150\202\202\151\151" +
		"\151\152\153\153\153\203\203\154\155\155\204\204\155\205\205\206\206\155\155\156" +
		"\156\156\156\157\157\160\160\160\161\162\162\163\164");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"regexp",
		"scon",
		"icon",
		"_skip",
		"_skip_comment",
		"'%'",
		"'::='",
		"'::'",
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
		"'}'",
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'+='",
		"'?'",
		"'&'",
		"'@'",
		"error",
		"ID",
		"Ltrue",
		"Lfalse",
		"Lnew",
		"Lseparator",
		"Las",
		"Limport",
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
		"input",
		"header",
		"lexer_section",
		"parser_section",
		"parsing_algorithm",
		"import_",
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
		"nonterm_type",
		"assoc",
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
		"rhsClass",
		"rhsPrimary",
		"rhsAnnotations",
		"annotations",
		"annotation",
		"negative_la",
		"expression",
		"literal",
		"map_entries",
		"map_separator",
		"name",
		"qualified_id",
		"command",
		"syntax_problem",
		"import__optlist",
		"option_optlist",
		"parsing_algorithmopt",
		"typeopt",
		"lexem_transitionopt",
		"iconopt",
		"lexem_attrsopt",
		"commandopt",
		"lexer_state_list",
		"identifieropt",
		"inputref_list",
		"rule0_list",
		"rhsSuffixopt",
		"annotation_list",
		"symref_list",
		"map_entriesopt",
		"expression_list",
		"expression_list_opt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 60;
		public static final int header = 61;
		public static final int lexer_section = 62;
		public static final int parser_section = 63;
		public static final int parsing_algorithm = 64;
		public static final int import_ = 65;
		public static final int option = 66;
		public static final int identifier = 67;
		public static final int symref = 68;
		public static final int type = 69;
		public static final int type_part_list = 70;
		public static final int type_part = 71;
		public static final int pattern = 72;
		public static final int lexer_parts = 73;
		public static final int lexer_part = 74;
		public static final int named_pattern = 75;
		public static final int lexeme = 76;
		public static final int lexem_transition = 77;
		public static final int lexem_attrs = 78;
		public static final int lexem_attribute = 79;
		public static final int state_selector = 80;
		public static final int stateref = 81;
		public static final int lexer_state = 82;
		public static final int grammar_parts = 83;
		public static final int grammar_part = 84;
		public static final int nonterm = 85;
		public static final int nonterm_type = 86;
		public static final int assoc = 87;
		public static final int directive = 88;
		public static final int inputref = 89;
		public static final int references = 90;
		public static final int references_cs = 91;
		public static final int rules = 92;
		public static final int rule0 = 93;
		public static final int rhsPrefix = 94;
		public static final int rhsSuffix = 95;
		public static final int rhsParts = 96;
		public static final int rhsPart = 97;
		public static final int rhsAnnotated = 98;
		public static final int rhsAssignment = 99;
		public static final int rhsOptional = 100;
		public static final int rhsCast = 101;
		public static final int rhsUnordered = 102;
		public static final int rhsClass = 103;
		public static final int rhsPrimary = 104;
		public static final int rhsAnnotations = 105;
		public static final int annotations = 106;
		public static final int annotation = 107;
		public static final int negative_la = 108;
		public static final int expression = 109;
		public static final int literal = 110;
		public static final int map_entries = 111;
		public static final int map_separator = 112;
		public static final int name = 113;
		public static final int qualified_id = 114;
		public static final int command = 115;
		public static final int syntax_problem = 116;
		public static final int import__optlist = 117;
		public static final int option_optlist = 118;
		public static final int parsing_algorithmopt = 119;
		public static final int typeopt = 120;
		public static final int lexem_transitionopt = 121;
		public static final int iconopt = 122;
		public static final int lexem_attrsopt = 123;
		public static final int commandopt = 124;
		public static final int lexer_state_list = 125;
		public static final int identifieropt = 126;
		public static final int inputref_list = 127;
		public static final int rule0_list = 128;
		public static final int rhsSuffixopt = 129;
		public static final int annotation_list = 130;
		public static final int symref_list = 131;
		public static final int map_entriesopt = 132;
		public static final int expression_list = 133;
		public static final int expression_list_opt = 134;
	}

	public interface Rules {
		public static final int nonterm_type_nontermTypeAST = 77;  // nonterm_type ::= Lreturns symref
		public static final int nonterm_type_nontermTypeHint = 80;  // nonterm_type ::= Linline Lclass identifieropt
		public static final int nonterm_type_nontermTypeHint2 = 81;  // nonterm_type ::= Lclass identifieropt
		public static final int nonterm_type_nontermTypeHint3 = 82;  // nonterm_type ::= Linterface identifieropt
		public static final int nonterm_type_nontermTypeHint4 = 83;  // nonterm_type ::= Lvoid
		public static final int nonterm_type_nontermTypeRaw = 84;  // nonterm_type ::= type
		public static final int directive_directivePrio = 88;  // directive ::= '%' assoc references ';'
		public static final int directive_directiveInput = 91;  // directive ::= '%' Linput inputref_list ';'
		public static final int rhsOptional_rhsQuantifier = 125;  // rhsOptional ::= rhsCast '?'
		public static final int rhsCast_rhsAsLiteral = 128;  // rhsCast ::= rhsClass Las literal
		public static final int rhsPrimary_rhsSymbol = 132;  // rhsPrimary ::= symref
		public static final int rhsPrimary_rhsNested = 133;  // rhsPrimary ::= '(' rules ')'
		public static final int rhsPrimary_rhsList = 134;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		public static final int rhsPrimary_rhsList2 = 135;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		public static final int rhsPrimary_rhsQuantifier = 136;  // rhsPrimary ::= rhsPrimary '*'
		public static final int rhsPrimary_rhsQuantifier2 = 137;  // rhsPrimary ::= rhsPrimary '+'
		public static final int expression_instance = 154;  // expression ::= Lnew name '(' map_entriesopt ')'
		public static final int expression_array = 159;  // expression ::= '[' expression_list_opt ']'
		public static final int literal_literal = 161;  // literal ::= scon
		public static final int literal_literal2 = 162;  // literal ::= icon
		public static final int literal_literal3 = 163;  // literal ::= Ltrue
		public static final int literal_literal4 = 164;  // literal ::= Lfalse
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
						reporter.error(MessageFormat.format("syntax error before line {0}", tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
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
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 30) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new LapgSymbol();
			tmStack[tmHead].symbol = 30;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 30);
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
			case 0:  // import__optlist ::= import__optlist import_
				((List<TmaImport>)lapg_gg.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 1:  // import__optlist ::=
				lapg_gg.value = new ArrayList();
				break;
			case 2:  // option_optlist ::= option_optlist option
				((List<TmaOption>)lapg_gg.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 3:  // option_optlist ::=
				lapg_gg.value = new ArrayList();
				break;
			case 4:  // input ::= header import__optlist option_optlist lexer_section parser_section
				lapg_gg.value = new TmaInput(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* importOptlist */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* optionOptlist */,
						((TmaLexerSection)tmStack[tmHead - 1].value) /* lexerSection */,
						((TmaParserSection)tmStack[tmHead].value) /* parserSection */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 5:  // input ::= header import__optlist option_optlist lexer_section
				lapg_gg.value = new TmaInput(
						((TmaHeader)tmStack[tmHead - 3].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 2].value) /* importOptlist */,
						((List<TmaOption>)tmStack[tmHead - 1].value) /* optionOptlist */,
						((TmaLexerSection)tmStack[tmHead].value) /* lexerSection */,
						null /* parserSection */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // header ::= Llanguage name '(' name ')' parsing_algorithmopt ';'
				lapg_gg.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 5].value) /* name */,
						((TmaName)tmStack[tmHead - 3].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						null /* input */, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 9:  // header ::= Llanguage name parsing_algorithmopt ';'
				lapg_gg.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 2].value) /* name */,
						null /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 10:  // lexer_section ::= '::' Llexer lexer_parts
				lapg_gg.value = new TmaLexerSection(
						((List<TmaLexerPartsItem>)tmStack[tmHead].value) /* lexerParts */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // parser_section ::= '::' Lparser grammar_parts
				lapg_gg.value = new TmaParserSection(
						((List<TmaGrammarPartsItem>)tmStack[tmHead].value) /* grammarParts */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 12:  // parsing_algorithm ::= Llalr '(' icon ')'
				lapg_gg.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 13:  // import_ ::= Limport ID scon ';'
				lapg_gg.value = new TmaImport(
						((String)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 14:  // import_ ::= Limport scon ';'
				lapg_gg.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 15:  // option ::= ID '=' expression
				lapg_gg.value = new TmaOption(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 16:  // option ::= syntax_problem
				lapg_gg.value = new TmaOption(
						null /* ID */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 17:  // identifier ::= ID
				lapg_gg.value = new TmaIdentifier(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // symref ::= ID
				lapg_gg.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // type ::= '(' scon ')'
				 lapg_gg.value = ((String)tmStack[tmHead - 1].value); 
				break;
			case 20:  // type ::= '(' type_part_list ')'
				 lapg_gg.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 36:  // pattern ::= regexp
				lapg_gg.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // lexer_parts ::= lexer_part
				lapg_gg.value = new ArrayList();
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 38:  // lexer_parts ::= lexer_parts lexer_part
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 39:  // lexer_parts ::= lexer_parts syntax_problem
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						null /* lexerPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 43:  // named_pattern ::= ID '=' pattern
				lapg_gg.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				lapg_gg.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 7].value) /* name */,
						((String)tmStack[tmHead - 6].value) /* type */,
						((TmaPattern)tmStack[tmHead - 4].value) /* pattern */,
						((TmaStateref)tmStack[tmHead - 3].value) /* lexemTransition */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaLexemAttrs)tmStack[tmHead - 1].value) /* lexemAttrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						null /* input */, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 55:  // lexeme ::= identifier typeopt ':'
				lapg_gg.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((String)tmStack[tmHead - 1].value) /* type */,
						null /* pattern */,
						null /* lexemTransition */,
						null /* priority */,
						null /* lexemAttrs */,
						null /* command */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 56:  // lexem_transition ::= '=>' stateref
				lapg_gg.value = ((TmaStateref)tmStack[tmHead].value);
				break;
			case 57:  // lexem_attrs ::= '(' lexem_attribute ')'
				lapg_gg.value = new TmaLexemAttrs(
						((TmaLexemAttribute)tmStack[tmHead - 1].value) /* lexemAttribute */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // lexem_attribute ::= Lsoft
				lapg_gg.value = TmaLexemAttribute.LSOFT;
				break;
			case 59:  // lexem_attribute ::= Lclass
				lapg_gg.value = TmaLexemAttribute.LCLASS;
				break;
			case 60:  // lexem_attribute ::= Lspace
				lapg_gg.value = TmaLexemAttribute.LSPACE;
				break;
			case 61:  // lexem_attribute ::= Llayout
				lapg_gg.value = TmaLexemAttribute.LLAYOUT;
				break;
			case 62:  // lexer_state_list ::= lexer_state_list ',' lexer_state
				((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 63:  // lexer_state_list ::= lexer_state
				lapg_gg.value = new ArrayList();
				((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 64:  // state_selector ::= '[' lexer_state_list ']'
				lapg_gg.value = new TmaStateSelector(
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 65:  // stateref ::= ID
				lapg_gg.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 66:  // lexer_state ::= identifier '=>' stateref
				lapg_gg.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaStateref)tmStack[tmHead].value) /* defaultTransition */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 67:  // lexer_state ::= identifier
				lapg_gg.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* defaultTransition */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 68:  // grammar_parts ::= grammar_part
				lapg_gg.value = new ArrayList();
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 69:  // grammar_parts ::= grammar_parts grammar_part
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 70:  // grammar_parts ::= grammar_parts syntax_problem
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						null /* grammarPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 73:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 74:  // nonterm ::= annotations identifier '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 75:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // nonterm ::= identifier '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // nonterm_type ::= Lreturns symref
				lapg_gg.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // nonterm_type ::= Linline Lclass identifieropt
				lapg_gg.value = new TmaNontermTypeHint(
						true /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 81:  // nonterm_type ::= Lclass identifieropt
				lapg_gg.value = new TmaNontermTypeHint(
						false /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // nonterm_type ::= Linterface identifieropt
				lapg_gg.value = new TmaNontermTypeHint(
						false /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LINTERFACE /* kind */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 83:  // nonterm_type ::= Lvoid
				lapg_gg.value = new TmaNontermTypeHint(
						false /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LVOID /* kind */,
						null /* name */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 84:  // nonterm_type ::= type
				lapg_gg.value = new TmaNontermTypeRaw(
						((String)tmStack[tmHead].value) /* type */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 85:  // assoc ::= Lleft
				lapg_gg.value = TmaAssoc.LLEFT;
				break;
			case 86:  // assoc ::= Lright
				lapg_gg.value = TmaAssoc.LRIGHT;
				break;
			case 87:  // assoc ::= Lnonassoc
				lapg_gg.value = TmaAssoc.LNONASSOC;
				break;
			case 88:  // directive ::= '%' assoc references ';'
				lapg_gg.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // inputref_list ::= inputref_list ',' inputref
				((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 90:  // inputref_list ::= inputref
				lapg_gg.value = new ArrayList();
				((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 91:  // directive ::= '%' Linput inputref_list ';'
				lapg_gg.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // inputref ::= symref Lnoeoi
				lapg_gg.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* symref */,
						true /* noeoi */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // inputref ::= symref
				lapg_gg.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						false /* noeoi */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // references ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 95:  // references ::= references symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 96:  // references_cs ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 97:  // references_cs ::= references_cs ',' symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 98:  // rule0_list ::= rule0_list '|' rule0
				((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 99:  // rule0_list ::= rule0
				lapg_gg.value = new ArrayList();
				((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 103:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // rule0 ::= rhsPrefix rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // rule0 ::= rhsParts rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // rule0 ::= rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // rule0 ::= syntax_problem
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						null /* rhsSuffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rhsPrefix ::= '[' annotations ']'
				lapg_gg.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						null /* alias */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // rhsPrefix ::= '[' annotations identifier ']'
				lapg_gg.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 2].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // rhsPrefix ::= '[' identifier ']'
				lapg_gg.value = new TmaRhsPrefix(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // rhsSuffix ::= '%' Lprio symref
				lapg_gg.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPRIO /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // rhsSuffix ::= '%' Lshift symref
				lapg_gg.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rhsParts ::= rhsPart
				lapg_gg.value = new ArrayList();
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 114:  // rhsParts ::= rhsParts rhsPart
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 115:  // rhsParts ::= rhsParts syntax_problem
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						null /* rhsPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 120:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				lapg_gg.value = new TmaRhsAnnotated(
						((TmaRhsAnnotations)tmStack[tmHead - 1].value) /* rhsAnnotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsAssignment */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // rhsAssignment ::= identifier '=' rhsOptional
				lapg_gg.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // rhsAssignment ::= identifier '+=' rhsOptional
				lapg_gg.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 125:  // rhsOptional ::= rhsCast '?'
				lapg_gg.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUESTIONMARK /* quantifier */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // rhsCast ::= rhsClass Las symref
				lapg_gg.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* rhsClass */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // rhsCast ::= rhsClass Las literal
				lapg_gg.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* rhsClass */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // rhsUnordered ::= rhsPart '&' rhsPart
				lapg_gg.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 131:  // rhsClass ::= identifier ':' rhsPrimary
				lapg_gg.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // rhsPrimary ::= symref
				lapg_gg.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // rhsPrimary ::= '(' rules ')'
				lapg_gg.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				lapg_gg.value = new TmaRhsList(
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						TmaRhsList.TmaQuantifierKind.PLUS /* quantifier */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				lapg_gg.value = new TmaRhsList(
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						TmaRhsList.TmaQuantifierKind.MULT /* quantifier */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // rhsPrimary ::= rhsPrimary '*'
				lapg_gg.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 137:  // rhsPrimary ::= rhsPrimary '+'
				lapg_gg.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)lapg_gg.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 139:  // annotation_list ::= annotation
				lapg_gg.value = new ArrayList();
				((List<TmaAnnotation>)lapg_gg.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 140:  // rhsAnnotations ::= annotation_list
				lapg_gg.value = new TmaRhsAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotationList */,
						null /* negativeLa */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rhsAnnotations ::= negative_la annotation_list
				lapg_gg.value = new TmaRhsAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotationList */,
						((TmaNegativeLa)tmStack[tmHead - 1].value) /* negativeLa */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // rhsAnnotations ::= negative_la
				lapg_gg.value = new TmaRhsAnnotations(
						null /* annotationList */,
						((TmaNegativeLa)tmStack[tmHead].value) /* negativeLa */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // annotations ::= annotation_list
				lapg_gg.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // annotation ::= '@' ID '{' expression '}'
				lapg_gg.value = new TmaAnnotation(
						((String)tmStack[tmHead - 3].value) /* name */,
						((ITmaExpression)tmStack[tmHead - 1].value) /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 145:  // annotation ::= '@' ID
				lapg_gg.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 146:  // annotation ::= '@' syntax_problem
				lapg_gg.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // symref_list ::= symref_list '|' symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 148:  // symref_list ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 149:  // negative_la ::= '(?!' symref_list ')'
				lapg_gg.value = new TmaNegativeLa(
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* unwantedSymbols */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // expression ::= Lnew name '(' map_entriesopt ')'
				lapg_gg.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value) /* mapEntries */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 155:  // expression_list ::= expression_list ',' expression
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 156:  // expression_list ::= expression
				lapg_gg.value = new ArrayList();
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 159:  // expression ::= '[' expression_list_opt ']'
				lapg_gg.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // literal ::= scon
				lapg_gg.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* sval */,
						null /* ival */,
						false /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // literal ::= icon
				lapg_gg.value = new TmaLiteral(
						null /* sval */,
						((Integer)tmStack[tmHead].value) /* ival */,
						false /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // literal ::= Ltrue
				lapg_gg.value = new TmaLiteral(
						null /* sval */,
						null /* ival */,
						true /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // literal ::= Lfalse
				lapg_gg.value = new TmaLiteral(
						null /* sval */,
						null /* ival */,
						false /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 165:  // map_entries ::= ID map_separator expression
				lapg_gg.value = new ArrayList();
				((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 166:  // map_entries ::= map_entries ',' ID map_separator expression
				((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset));
				break;
			case 170:  // name ::= qualified_id
				lapg_gg.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 172:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 173:  // command ::= code
				lapg_gg.value = new TmaCommand(
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 174:  // syntax_problem ::= error
				lapg_gg.value = new TmaSyntaxProblem(
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
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
		return (TmaInput) parse(lexer, 0, 277);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 278);
	}
}
