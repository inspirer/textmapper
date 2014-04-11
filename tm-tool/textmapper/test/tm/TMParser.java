/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
	private static final int[] tmAction = TMLexer.unpack_int(283,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\240\0\241\0\uffcd\uffff\255\0\22" +
		"\0\242\0\243\0\uffff\uffff\226\0\225\0\237\0\252\0\uff93\uffff\uff8b\uffff\uff7f" +
		"\uffff\233\0\uff4f\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\6\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\0\0\uffff\uffff\uffff\uffff\236\0\uff49\uffff\uffff" +
		"\uffff\uffff\uffff\11\0\253\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uff1d" +
		"\uffff\2\0\20\0\232\0\uffff\uffff\uff17\uffff\uffff\uffff\uff11\uffff\uffff\uffff" +
		"\16\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\4\0\247\0\250\0\246\0\uffff" +
		"\uffff\uffff\uffff\231\0\uffff\uffff\14\0\15\0\uffff\uffff\uff0b\uffff\uff03\uffff" +
		"\ufefd\uffff\45\0\51\0\52\0\50\0\17\0\uffff\uffff\244\0\uffff\uffff\10\0\21\0\ufecb" +
		"\uffff\77\0\uffff\uffff\uffff\uffff\uffff\uffff\54\0\uffff\uffff\46\0\47\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\ufec3\uffff\104\0\107\0\110\0\uffff\uffff\220\0\ufe91" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\100\0\44\0\53\0\uffff\uffff\35\0\36\0" +
		"\31\0\32\0\uffff\uffff\27\0\30\0\34\0\37\0\41\0\40\0\33\0\uffff\uffff\26\0\ufe5f" +
		"\uffff\uffff\uffff\125\0\126\0\127\0\uffff\uffff\ufe2b\uffff\224\0\ufdf7\uffff\uffff" +
		"\uffff\uffff\uffff\ufdbb\uffff\ufd8f\uffff\123\0\124\0\uffff\uffff\105\0\106\0\uffff" +
		"\uffff\217\0\245\0\101\0\102\0\76\0\23\0\43\0\uffff\uffff\24\0\25\0\ufd63\uffff\ufd29" +
		"\uffff\132\0\uffff\uffff\136\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufd21" +
		"\uffff\uffff\uffff\ufce5\uffff\254\0\uffff\uffff\204\0\uffff\uffff\143\0\ufc97\uffff" +
		"\145\0\ufc5d\uffff\ufc21\uffff\164\0\167\0\171\0\ufbe1\uffff\165\0\ufb9f\uffff\ufb5b" +
		"\uffff\uffff\uffff\166\0\153\0\ufb13\uffff\152\0\ufb0b\uffff\115\0\116\0\121\0\122" +
		"\0\ufadf\uffff\ufaa3\uffff\uffff\uffff\42\0\uffff\uffff\56\0\ufa67\uffff\134\0\133" +
		"\0\uffff\uffff\130\0\137\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\ufa2f\uffff\uf9f3\uffff\uffff\uffff\uffff\uffff\uffff\uffff\114" +
		"\0\uf9b5\uffff\150\0\uf979\uffff\163\0\151\0\uffff\uffff\175\0\uffff\uffff\210\0" +
		"\211\0\170\0\uf939\uffff\120\0\uffff\uffff\uffff\uffff\uf8fb\uffff\70\0\60\0\uf8bf" +
		"\uffff\131\0\222\0\157\0\160\0\156\0\154\0\uffff\uffff\205\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\172\0\uf889\uffff\173\0\147\0\uf841\uffff\177\0\200\0\142\0\113\0\112" +
		"\0\uffff\uffff\uffff\uffff\62\0\uf801\uffff\155\0\uffff\uffff\uffff\uffff\215\0\111" +
		"\0\72\0\73\0\74\0\75\0\uffff\uffff\64\0\66\0\uffff\uffff\uffff\uffff\213\0\uffff" +
		"\uffff\71\0\207\0\206\0\uffff\uffff\uffff\uffff\214\0\216\0\212\0\uffff\uffff\uffff" +
		"\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(2096,
		"\10\1\37\1\40\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1" +
		"\63\1\64\1\65\1\66\1\67\1\70\1\71\1\uffff\ufffe\2\uffff\3\uffff\21\uffff\37\uffff" +
		"\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\41\uffff\42\uffff\43\uffff\22\235\uffff\ufffe\23\uffff\67\uffff" +
		"\14\7\uffff\ufffe\15\uffff\14\251\23\251\24\251\67\251\uffff\ufffe\46\uffff\10\3" +
		"\37\3\40\3\47\3\50\3\51\3\52\3\53\3\54\3\55\3\56\3\57\3\60\3\61\3\62\3\63\3\64\3" +
		"\65\3\66\3\67\3\70\3\71\3\uffff\ufffe\17\uffff\22\234\uffff\ufffe\40\uffff\71\uffff" +
		"\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\24\230\uffff\ufffe\10\uffff\0\5\uffff\ufffe\17\uffff\24\227\uffff\ufffe\67\uffff" +
		"\14\7\uffff\ufffe\12\uffff\20\21\23\21\uffff\ufffe\23\uffff\20\55\uffff\ufffe\21" +
		"\uffff\37\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\0\12\10\12\uffff\ufffe\13\uffff\17\103\22\103\uffff" +
		"\ufffe\6\uffff\36\uffff\37\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65" +
		"\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\0\13\uffff\ufffe\36\uffff\22" +
		"\221\23\221\35\221\40\221\47\221\50\221\51\221\52\221\53\221\54\221\55\221\56\221" +
		"\57\221\60\221\61\221\62\221\63\221\64\221\65\221\66\221\67\221\70\221\71\221\uffff" +
		"\ufffe\1\uffff\0\67\10\67\21\67\37\67\40\67\47\67\50\67\51\67\52\67\53\67\54\67\55" +
		"\67\56\67\57\67\60\67\61\67\62\67\63\67\64\67\65\67\66\67\67\67\70\67\71\67\uffff" +
		"\ufffe\74\uffff\22\223\23\223\35\223\36\223\40\223\47\223\50\223\51\223\52\223\53" +
		"\223\54\223\55\223\56\223\57\223\60\223\61\223\62\223\63\223\64\223\65\223\66\223" +
		"\67\223\70\223\71\223\uffff\ufffe\6\uffff\21\uffff\23\uffff\35\uffff\36\uffff\37" +
		"\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62" +
		"\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\73\uffff\11\146\14\146\uffff\ufffe\40\uffff\71\uffff\70" +
		"\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57" +
		"\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\7" +
		"\117\uffff\ufffe\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\7\117\uffff\ufffe\13\uffff\0\57\3\57\10\57\21\57" +
		"\23\57\37\57\40\57\47\57\50\57\51\57\52\57\53\57\54\57\55\57\56\57\57\57\60\57\61" +
		"\57\62\57\63\57\64\57\65\57\66\57\67\57\70\57\71\57\73\57\uffff\ufffe\57\uffff\14" +
		"\135\17\135\uffff\ufffe\6\uffff\21\uffff\23\uffff\35\uffff\36\uffff\37\uffff\40\uffff" +
		"\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\73\uffff\11\146\24\146\uffff\ufffe\12\21\20\21\32\21\6\22\11\22\14\22\23" +
		"\22\24\22\30\22\31\22\33\22\34\22\35\22\36\22\37\22\40\22\44\22\45\22\47\22\50\22" +
		"\51\22\52\22\53\22\54\22\55\22\56\22\57\22\60\22\61\22\62\22\63\22\64\22\65\22\66" +
		"\22\67\22\70\22\71\22\73\22\uffff\ufffe\6\uffff\23\uffff\35\uffff\36\uffff\40\uffff" +
		"\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\73\uffff\11\146\14\146\24\146\uffff\ufffe\6\uffff\23\uffff\35\uffff\36" +
		"\uffff\37\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\73\uffff\11\146\14\146\24\146\uffff\ufffe\34\uffff" +
		"\6\161\11\161\14\161\23\161\24\161\35\161\36\161\37\161\40\161\44\161\47\161\50\161" +
		"\51\161\52\161\53\161\54\161\55\161\56\161\57\161\60\161\61\161\62\161\63\161\64" +
		"\161\65\161\66\161\67\161\70\161\71\161\73\161\uffff\ufffe\33\uffff\6\174\11\174" +
		"\14\174\23\174\24\174\34\174\35\174\36\174\37\174\40\174\44\174\47\174\50\174\51" +
		"\174\52\174\53\174\54\174\55\174\56\174\57\174\60\174\61\174\62\174\63\174\64\174" +
		"\65\174\66\174\67\174\70\174\71\174\73\174\uffff\ufffe\45\uffff\6\176\11\176\14\176" +
		"\23\176\24\176\33\176\34\176\35\176\36\176\37\176\40\176\44\176\47\176\50\176\51" +
		"\176\52\176\53\176\54\176\55\176\56\176\57\176\60\176\61\176\62\176\63\176\64\176" +
		"\65\176\66\176\67\176\70\176\71\176\73\176\uffff\ufffe\30\uffff\31\uffff\6\202\11" +
		"\202\14\202\23\202\24\202\33\202\34\202\35\202\36\202\37\202\40\202\44\202\45\202" +
		"\47\202\50\202\51\202\52\202\53\202\54\202\55\202\56\202\57\202\60\202\61\202\62" +
		"\202\63\202\64\202\65\202\66\202\67\202\70\202\71\202\73\202\uffff\ufffe\11\uffff" +
		"\14\144\24\144\uffff\ufffe\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\7\117\uffff\ufffe\6\uffff\21\uffff" +
		"\23\uffff\35\uffff\36\uffff\37\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff" +
		"\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff" +
		"\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\73\uffff\11\146\14\146\uffff" +
		"\ufffe\6\uffff\21\uffff\23\uffff\35\uffff\36\uffff\37\uffff\40\uffff\71\uffff\70" +
		"\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57" +
		"\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\73" +
		"\uffff\11\146\14\146\uffff\ufffe\3\uffff\0\61\10\61\21\61\23\61\37\61\40\61\47\61" +
		"\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65" +
		"\61\66\61\67\61\70\61\71\61\73\61\uffff\ufffe\6\uffff\23\uffff\35\uffff\36\uffff" +
		"\37\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\44\uffff\73\uffff\11\146\24\146\uffff\ufffe\6\uffff\21" +
		"\uffff\23\uffff\35\uffff\36\uffff\37\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66" +
		"\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\73\uffff\11\146\14\146" +
		"\24\146\uffff\ufffe\6\uffff\23\uffff\35\uffff\36\uffff\37\uffff\40\uffff\71\uffff" +
		"\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\73\uffff\11\146\14\146\24\146\uffff\ufffe\34\uffff\6\162\11\162\14\162\23\162\24" +
		"\162\35\162\36\162\37\162\40\162\44\162\47\162\50\162\51\162\52\162\53\162\54\162" +
		"\55\162\56\162\57\162\60\162\61\162\62\162\63\162\64\162\65\162\66\162\67\162\70" +
		"\162\71\162\73\162\uffff\ufffe\6\uffff\21\uffff\23\uffff\35\uffff\36\uffff\37\uffff" +
		"\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\73\uffff\11\146\14\146\24\146\uffff\ufffe\6\uffff\21\uffff\23" +
		"\uffff\35\uffff\36\uffff\37\uffff\40\uffff\71\uffff\70\uffff\67\uffff\66\uffff\65" +
		"\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\73\uffff\11\146\14\146\uffff" +
		"\ufffe\23\uffff\0\63\10\63\21\63\37\63\40\63\47\63\50\63\51\63\52\63\53\63\54\63" +
		"\55\63\56\63\57\63\60\63\61\63\62\63\63\63\64\63\65\63\66\63\67\63\70\63\71\63\73" +
		"\63\uffff\ufffe\30\uffff\31\uffff\6\203\11\203\14\203\23\203\24\203\33\203\34\203" +
		"\35\203\36\203\37\203\40\203\44\203\45\203\47\203\50\203\51\203\52\203\53\203\54" +
		"\203\55\203\56\203\57\203\60\203\61\203\62\203\63\203\64\203\65\203\66\203\67\203" +
		"\70\203\71\203\73\203\uffff\ufffe\34\201\6\201\11\201\14\201\23\201\24\201\35\201" +
		"\36\201\37\201\40\201\44\201\47\201\50\201\51\201\52\201\53\201\54\201\55\201\56" +
		"\201\57\201\60\201\61\201\62\201\63\201\64\201\65\201\66\201\67\201\70\201\71\201" +
		"\73\201\uffff\ufffe\73\uffff\0\65\10\65\21\65\37\65\40\65\47\65\50\65\51\65\52\65" +
		"\53\65\54\65\55\65\56\65\57\65\60\65\61\65\62\65\63\65\64\65\65\65\66\65\67\65\70" +
		"\65\71\65\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(136,
		"\0\2\4\17\31\31\31\46\52\54\55\62\66\101\106\107\120\125\151\162\216\232\233\237" +
		"\243\252\255\256\263\272\312\336\363\u0132\u013a\u0142\u0149\u014a\u014b\u014c\u018d" +
		"\u01cd\u020d\u024e\u028e\u02ce\u030e\u034e\u038e\u03ce\u0411\u0452\u0493\u04d3\u0513" +
		"\u0553\u0594\u05d4\u0614\u0614\u0621\u0622\u0623\u0624\u0625\u0626\u0628\u0629\u062a" +
		"\u0645\u0669\u066c\u066e\u0672\u0674\u0675\u0677\u0679\u067b\u067c\u067d\u067e\u0680" +
		"\u0682\u0684\u0685\u0687\u0689\u068b\u068c\u068e\u0690\u0692\u0692\u0698\u069f\u06a6" +
		"\u06b1\u06b9\u06c5\u06d1\u06de\u06ed\u06fc\u0708\u0717\u0727\u0729\u0738\u0748\u074f" +
		"\u0757\u0758\u075a\u075d\u0760\u076d\u0782\u0783\u0784\u0786\u0787\u0788\u0789\u078a" +
		"\u078b\u078c\u078f\u0790\u0796\u07a1\u07a2\u07b1\u07b2\u07b3\u07b4");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1972,
		"\u0117\u0118\123\170\1\6\34\37\47\66\74\124\143\233\332\1\6\37\43\66\74\143\233\301" +
		"\332\113\134\200\236\246\250\273\274\315\316\323\336\342\133\207\212\275\36\52\264" +
		"\51\56\103\115\242\56\115\120\225\32\46\64\77\230\232\244\340\341\357\373\21\124" +
		"\156\166\222\u010c\24\57\122\124\156\166\222\230\u010e\56\115\126\242\360\1\6\37" +
		"\65\66\74\105\124\143\156\166\200\222\233\236\273\274\316\336\342\25\122\124\156" +
		"\166\222\312\313\354\20\26\30\104\124\133\156\166\200\212\222\236\237\246\250\261" +
		"\273\274\315\316\317\320\321\323\330\336\342\345\42\60\62\151\156\166\222\314\357" +
		"\u0100\u0108\u010e\307\124\156\166\222\124\156\166\222\124\156\166\222\260\362\u010b" +
		"\260\362\u010b\242\124\156\166\222\255\124\156\166\222\251\325\365\200\236\246\250" +
		"\261\273\274\315\316\317\320\321\323\330\336\342\113\124\134\142\156\166\200\222" +
		"\235\236\246\250\273\274\315\316\323\330\336\342\1\6\36\37\66\74\105\132\134\143" +
		"\200\233\236\250\273\274\315\316\323\336\342\1\2\6\13\27\33\34\36\37\41\65\66\74" +
		"\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232" +
		"\233\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316\317\320\321\323" +
		"\330\332\336\342\356\u0100\u0101\u0112\u0113\1\6\37\66\74\143\233\332\1\6\37\66\74" +
		"\143\233\332\1\6\37\66\74\143\233\315\257\22\1\2\6\13\27\33\34\36\37\41\65\66\74" +
		"\75\102\105\113\124\132\133\134\140\143\144\145\156\166\171\175\200\202\203\204\212" +
		"\222\232\233\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316\317\320" +
		"\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13\27\33\34\36\37\41\65" +
		"\66\74\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204" +
		"\222\232\233\234\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316\317" +
		"\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13\27\33\34\36\37" +
		"\41\65\66\74\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203" +
		"\204\222\232\233\234\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316" +
		"\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13\27\33\34\36" +
		"\37\41\65\66\74\75\102\105\113\124\132\133\134\140\143\144\145\156\166\171\175\200" +
		"\202\203\204\212\222\232\233\235\236\246\250\261\266\273\274\277\304\310\311\313" +
		"\315\316\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13\27" +
		"\33\34\36\37\41\65\66\74\75\102\105\113\124\131\132\134\140\143\144\145\156\166\171" +
		"\175\200\202\203\204\222\232\233\235\236\246\250\261\266\273\274\277\304\310\311" +
		"\313\315\316\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13" +
		"\27\33\34\36\37\41\65\66\74\75\102\105\113\124\131\132\134\140\143\144\145\156\166" +
		"\171\175\200\202\203\204\222\232\233\235\236\246\250\261\266\273\274\277\304\310" +
		"\311\313\315\316\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2" +
		"\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\131\132\134\140\143\144\145\156" +
		"\166\171\175\200\202\203\204\222\232\233\235\236\246\250\261\266\273\274\277\304" +
		"\310\311\313\315\316\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113" +
		"\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\131\132\134\140\143\144\145" +
		"\156\166\171\175\200\202\203\204\222\232\233\235\236\246\250\261\266\273\274\277" +
		"\304\310\311\313\315\316\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113" +
		"\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\132\134\140\143\144\145\156" +
		"\166\171\175\200\202\203\204\222\226\232\233\235\236\246\250\261\266\273\274\277" +
		"\304\310\311\313\315\316\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113" +
		"\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\132\134\140\143\144\145\156" +
		"\166\171\175\200\202\203\204\222\232\233\235\236\246\250\261\266\273\274\277\304" +
		"\310\311\313\315\316\317\320\321\323\330\332\336\342\356\374\u0100\u0101\u0112\u0113" +
		"\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\132\133\134\140\143\144\145" +
		"\156\166\171\175\200\201\202\203\204\212\222\232\233\235\236\246\250\261\266\273" +
		"\274\277\304\310\311\313\315\316\317\320\321\323\330\332\336\342\356\374\u0100\u0101" +
		"\u0112\u0113\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\132\133\134\140" +
		"\143\144\145\156\166\171\175\200\202\203\204\212\222\232\233\235\236\246\250\261" +
		"\266\273\274\277\304\310\311\313\315\316\317\320\321\323\330\332\336\342\356\u0100" +
		"\u0101\u0112\u0113\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124\132\133" +
		"\134\140\143\144\145\156\166\171\175\200\202\203\204\212\222\232\233\235\236\246" +
		"\250\261\266\273\274\277\304\310\311\313\315\316\317\320\321\323\330\332\336\342" +
		"\356\u0100\u0101\u0112\u0113\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113\124" +
		"\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236\246" +
		"\250\261\266\273\274\277\304\310\311\313\315\316\317\320\321\323\330\332\336\342" +
		"\356\374\u0100\u0101\u0112\u0113\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102\105\113" +
		"\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233\235\236" +
		"\246\250\261\266\273\274\277\304\310\311\313\315\316\317\320\321\323\330\332\336" +
		"\342\356\374\u0100\u0101\u0112\u0113\0\1\2\6\13\27\33\34\36\37\41\65\66\74\75\102" +
		"\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232\233" +
		"\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316\317\320\321\323\330" +
		"\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13\20\27\33\34\36\37\41\61\65\66\74" +
		"\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222\232" +
		"\233\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316\317\320\321\323" +
		"\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13\27\33\34\36\37\41\50\65\66" +
		"\74\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204\222" +
		"\232\233\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316\317\320\321" +
		"\323\330\332\336\342\356\u0100\u0101\u0112\u0113\1\2\6\13\27\33\34\36\37\41\65\66" +
		"\67\74\75\102\105\113\124\132\134\140\143\144\145\156\166\171\175\200\202\203\204" +
		"\222\232\233\235\236\246\250\261\266\273\274\277\304\310\311\313\315\316\317\320" +
		"\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\200\236\246\250\273\274\315" +
		"\316\323\330\336\342\376\176\0\0\36\52\20\61\22\36\65\102\105\113\134\140\145\200" +
		"\203\204\235\236\246\250\261\266\273\274\313\315\316\317\321\323\330\336\342\1\6" +
		"\37\66\74\143\171\175\200\202\232\233\236\246\250\261\273\274\304\310\311\315\316" +
		"\317\320\321\323\330\332\336\342\356\u0100\u0101\u0112\u0113\104\133\212\124\156" +
		"\124\156\166\222\123\170\65\65\105\65\105\65\105\225\345\374\65\105\144\277\102\145" +
		"\113\113\134\113\134\133\212\131\113\134\171\304\175\356\200\236\273\274\316\342" +
		"\200\236\273\274\316\336\342\200\236\273\274\316\336\342\200\236\246\250\273\274" +
		"\315\316\323\336\342\200\236\246\273\274\316\336\342\200\236\246\250\273\274\315" +
		"\316\323\330\336\342\200\236\246\250\273\274\315\316\323\330\336\342\200\236\246" +
		"\250\261\273\274\315\316\323\330\336\342\200\236\246\250\261\273\274\315\316\317" +
		"\321\323\330\336\342\200\236\246\250\261\273\274\315\316\317\321\323\330\336\342" +
		"\200\236\246\250\273\274\315\316\323\330\336\342\200\236\246\250\261\273\274\315" +
		"\316\317\321\323\330\336\342\200\236\246\250\261\273\274\315\316\317\320\321\323" +
		"\330\336\342\u0101\u0113\113\134\200\235\236\246\250\273\274\315\316\323\330\336" +
		"\342\113\134\142\200\235\236\246\250\273\274\315\316\323\330\336\342\1\6\37\66\74" +
		"\143\233\1\6\37\66\74\143\233\332\41\56\115\2\13\27\2\13\27\200\236\246\250\273\274" +
		"\315\316\323\330\336\342\376\1\6\36\37\66\74\105\132\134\143\200\233\236\250\273" +
		"\274\315\316\323\336\342\3\22\20\61\104\225\301\345\376\102\203\204\266\171\200\236" +
		"\273\274\316\342\200\236\246\250\273\274\315\316\323\336\342\u0101\113\134\200\235" +
		"\236\246\250\273\274\315\316\323\330\336\342\41\6\6");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1972,
		"\u0119\u011a\147\147\4\4\46\4\64\4\4\151\4\4\4\5\5\5\62\5\5\5\5\344\5\131\131\234" +
		"\234\234\234\234\234\234\234\234\234\234\200\273\274\342\50\67\336\66\71\123\71\317" +
		"\72\72\144\277\44\63\101\116\303\305\322\371\372\u0101\u0103\33\152\152\152\152\u0112" +
		"\37\75\145\153\153\153\153\304\u0113\73\73\170\320\320\6\6\6\102\6\6\102\154\6\154" +
		"\154\235\154\6\235\235\235\235\235\235\40\146\155\155\155\155\352\353\377\27\41\43" +
		"\124\156\124\156\156\236\124\156\236\316\236\236\236\236\236\236\236\236\236\236" +
		"\236\236\236\236\374\61\76\100\220\221\223\276\355\u0102\u010b\u010f\u0114\347\157" +
		"\157\157\157\160\160\160\160\161\161\161\161\333\333\u0110\334\334\u0111\321\162" +
		"\162\162\162\331\163\163\163\163\330\330\330\237\237\237\237\237\237\237\237\237" +
		"\237\237\237\237\237\237\237\132\164\132\132\164\164\132\164\132\132\132\132\132" +
		"\132\132\132\132\132\132\132\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\10\17\10\17" +
		"\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10" +
		"\10\240\10\117\117\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240" +
		"\240\240\10\240\240\240\10\240\240\10\10\10\10\10\11\11\11\11\11\11\11\11\12\12\12" +
		"\12\12\12\12\12\13\13\13\13\13\13\13\356\332\34\10\17\10\17\17\45\47\51\10\56\103" +
		"\10\10\115\117\103\117\165\176\201\117\117\10\215\117\165\165\10\10\240\10\117\117" +
		"\201\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10\240" +
		"\240\240\10\240\240\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117" +
		"\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\310\117" +
		"\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240\240" +
		"\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\117" +
		"\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\311\117\240\240\240\240\117" +
		"\240\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240\240\10\10\10\10\10\10" +
		"\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\202\117\117\10\215" +
		"\117\165\165\10\10\240\10\117\117\202\165\10\10\117\240\240\240\240\117\240\240\215" +
		"\10\10\10\117\240\240\240\10\240\240\240\10\240\240\10\10\10\10\10\10\17\10\17\17" +
		"\45\47\51\10\56\103\10\10\115\117\103\117\165\171\176\117\117\10\215\117\165\165" +
		"\10\10\240\10\117\117\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117" +
		"\240\240\240\10\240\240\240\10\240\240\10\10\10\10\10\10\17\10\17\17\45\47\51\10" +
		"\56\103\10\10\115\117\103\117\165\172\176\117\117\10\215\117\165\165\10\10\240\10" +
		"\117\117\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10" +
		"\240\240\240\10\240\240\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115" +
		"\117\103\117\165\173\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10" +
		"\117\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240" +
		"\240\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\174" +
		"\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117\240\240\240\240" +
		"\117\240\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240\240\10\10\10\10\10" +
		"\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117" +
		"\165\165\10\10\240\10\117\117\165\302\10\10\117\240\240\240\240\117\240\240\215\10" +
		"\10\10\117\240\240\240\10\240\240\240\10\240\240\10\10\10\10\10\10\17\10\17\17\45" +
		"\47\51\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240" +
		"\10\117\117\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240" +
		"\10\240\240\240\10\240\240\10\u0104\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103" +
		"\10\10\115\117\103\117\165\176\203\117\117\10\215\117\165\165\10\10\240\266\10\117" +
		"\117\203\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10" +
		"\240\240\240\10\240\240\10\u0105\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10" +
		"\10\115\117\103\117\165\176\204\117\117\10\215\117\165\165\10\10\240\10\117\117\204" +
		"\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10\240\240" +
		"\240\10\240\240\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103" +
		"\117\165\176\205\117\117\10\215\117\165\165\10\10\240\10\117\117\205\165\10\10\117" +
		"\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240\240" +
		"\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\117" +
		"\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117\240\240\240\240\117\240" +
		"\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240\240\10\u0106\10\10\10\10" +
		"\10\17\10\17\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117" +
		"\165\165\10\10\240\10\117\117\165\10\10\117\240\240\240\240\117\240\240\215\10\10" +
		"\10\117\240\240\240\10\240\240\240\10\240\240\10\u0107\10\10\10\10\2\10\17\10\17" +
		"\17\45\47\51\10\56\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10" +
		"\10\240\10\117\117\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240" +
		"\240\240\10\240\240\240\10\240\240\10\10\10\10\10\10\17\10\17\30\17\45\47\51\10\56" +
		"\30\103\10\10\115\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10\117" +
		"\117\165\10\10\117\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10\240" +
		"\240\240\10\240\240\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\65\103\10\10\115" +
		"\117\103\117\165\176\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117" +
		"\240\240\240\240\117\240\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240\240" +
		"\10\10\10\10\10\10\17\10\17\17\45\47\51\10\56\103\10\113\10\115\117\103\117\165\176" +
		"\117\117\10\215\117\165\165\10\10\240\10\117\117\165\10\10\117\240\240\240\240\117" +
		"\240\240\215\10\10\10\117\240\240\240\10\240\240\240\10\240\240\10\10\10\10\10\241" +
		"\241\241\241\241\241\241\241\241\241\241\241\241\233\u0117\3\52\70\31\31\35\53\104" +
		"\120\104\133\133\212\120\242\270\270\312\242\242\242\242\270\242\242\354\242\242" +
		"\360\360\242\242\242\242\14\14\14\14\14\14\226\231\243\267\306\14\243\243\243\243" +
		"\243\243\226\350\351\243\243\243\243\243\243\243\366\243\243\231\306\u010c\u0115" +
		"\u010c\125\206\206\166\222\167\167\224\224\150\225\105\106\127\107\107\110\110\300" +
		"\375\u0108\111\111\216\343\121\217\134\135\210\136\136\207\275\175\137\137\227\346" +
		"\232\u0100\244\314\340\341\357\373\245\245\245\245\245\370\245\246\246\246\246\246" +
		"\246\246\247\247\247\247\247\247\247\247\247\247\247\250\315\323\250\250\250\250" +
		"\250\251\251\251\325\251\251\325\251\325\365\251\251\252\252\252\252\252\252\252" +
		"\252\252\252\252\252\253\253\253\253\335\253\253\253\253\253\253\253\253\254\254" +
		"\254\254\254\254\254\254\254\361\363\254\254\254\254\255\255\255\255\255\255\255" +
		"\255\255\255\255\255\255\255\255\256\256\256\256\256\256\256\256\256\256\256\256" +
		"\257\257\257\257\257\257\257\257\257\257\257\257\257\257\257\260\260\260\260\260" +
		"\260\260\260\260\260\362\260\260\260\260\260\u010d\u0116\140\140\261\313\261\261" +
		"\261\261\261\261\261\261\261\261\261\141\141\213\141\141\141\141\141\141\141\141" +
		"\141\141\141\141\141\u0118\23\55\112\114\214\307\15\15\15\15\15\15\15\367\57\74\143" +
		"\20\26\42\21\21\21\262\262\262\262\262\262\262\262\262\262\262\262\u0109\16\16\54" +
		"\16\16\16\130\177\211\16\263\16\263\326\263\263\326\263\326\263\263\22\36\32\77\126" +
		"\301\345\376\u010a\122\271\272\337\230\264\264\264\264\264\264\265\265\324\327\265" +
		"\265\327\265\364\265\265\u010e\142\142\142\142\142\142\142\142\142\142\142\142\142" +
		"\142\142\60\24\25");

	private static final short[] tmRuleLen = TMLexer.unpack_short(174,
		"\2\0\2\0\5\4\1\0\7\4\3\3\4\4\3\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\3\2\1\1\2\2" +
		"\1\1\1\3\1\0\1\0\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\6\5\5\4\2\1\0" +
		"\3\2\2\1\1\1\1\1\4\3\1\4\2\1\1\2\1\3\3\1\1\1\0\3\2\2\1\1\3\4\3\3\3\1\2\2\1\1\1\1" +
		"\2\1\3\3\1\2\1\3\3\3\1\3\1\3\6\6\2\2\3\1\6\4\3\2\1\1\5\2\2\1\1\1\0\5\3\1\1\0\3\1" +
		"\1\1\1\1\3\5\1\1\1\1\1\3\1\1");

	private static final short[] tmRuleSymbol = TMLexer.unpack_short(174,
		"\165\165\166\166\75\75\167\167\76\76\77\100\101\102\102\103\103\104\105\106\106\107" +
		"\107\110\110\110\110\110\110\110\110\110\110\110\110\110\111\112\112\112\113\113" +
		"\113\114\170\170\171\171\172\172\173\173\174\174\115\115\116\117\120\120\120\120" +
		"\175\175\121\122\123\123\124\124\124\125\125\126\126\126\126\127\176\176\127\127" +
		"\127\127\127\130\130\130\131\177\177\131\132\132\133\133\134\134\200\200\135\201" +
		"\201\136\136\136\136\136\137\137\137\140\140\141\141\141\142\142\142\143\143\144" +
		"\144\144\145\145\146\146\146\147\150\150\151\151\151\151\151\151\202\202\151\151" +
		"\152\203\203\153\154\154\154\155\155\204\204\155\205\205\206\206\155\155\156\156" +
		"\156\156\157\157\160\160\160\161\162\162\163\164");

	protected static final String[] tmSymbolNames = new String[] {
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
		"'..'",
		"','",
		"':'",
		"'['",
		"']'",
		"'('",
		"')'",
		"'}'",
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'+='",
		"'?'",
		"'&'",
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
		"lexeme_transition",
		"lexeme_attrs",
		"lexeme_attribute",
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
		"rhsBracketsPair",
		"annotations",
		"annotation",
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
		"lexeme_transitionopt",
		"iconopt",
		"lexeme_attrsopt",
		"commandopt",
		"lexer_state_list_Comma_separated",
		"identifieropt",
		"inputref_list_Comma_separated",
		"rule0_list_Or_separated",
		"rhsSuffixopt",
		"rhsBracketsPair_list_Comma_separated",
		"annotation_list",
		"map_entriesopt",
		"expression_list_Comma_separated",
		"expression_list_Comma_separated_opt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int input = 61;
		static final int header = 62;
		static final int lexer_section = 63;
		static final int parser_section = 64;
		static final int parsing_algorithm = 65;
		static final int import_ = 66;
		static final int option = 67;
		static final int identifier = 68;
		static final int symref = 69;
		static final int type = 70;
		static final int type_part_list = 71;
		static final int type_part = 72;
		static final int pattern = 73;
		static final int lexer_parts = 74;
		static final int lexer_part = 75;
		static final int named_pattern = 76;
		static final int lexeme = 77;
		static final int lexeme_transition = 78;
		static final int lexeme_attrs = 79;
		static final int lexeme_attribute = 80;
		static final int state_selector = 81;
		static final int stateref = 82;
		static final int lexer_state = 83;
		static final int grammar_parts = 84;
		static final int grammar_part = 85;
		static final int nonterm = 86;
		static final int nonterm_type = 87;
		static final int assoc = 88;
		static final int directive = 89;
		static final int inputref = 90;
		static final int references = 91;
		static final int references_cs = 92;
		static final int rules = 93;
		static final int rule0 = 94;
		static final int rhsPrefix = 95;
		static final int rhsSuffix = 96;
		static final int rhsParts = 97;
		static final int rhsPart = 98;
		static final int rhsAnnotated = 99;
		static final int rhsAssignment = 100;
		static final int rhsOptional = 101;
		static final int rhsCast = 102;
		static final int rhsUnordered = 103;
		static final int rhsClass = 104;
		static final int rhsPrimary = 105;
		static final int rhsBracketsPair = 106;
		static final int annotations = 107;
		static final int annotation = 108;
		static final int expression = 109;
		static final int literal = 110;
		static final int map_entries = 111;
		static final int map_separator = 112;
		static final int name = 113;
		static final int qualified_id = 114;
		static final int command = 115;
		static final int syntax_problem = 116;
		static final int import__optlist = 117;
		static final int option_optlist = 118;
		static final int parsing_algorithmopt = 119;
		static final int typeopt = 120;
		static final int lexeme_transitionopt = 121;
		static final int iconopt = 122;
		static final int lexeme_attrsopt = 123;
		static final int commandopt = 124;
		static final int lexer_state_list_Comma_separated = 125;
		static final int identifieropt = 126;
		static final int inputref_list_Comma_separated = 127;
		static final int rule0_list_Or_separated = 128;
		static final int rhsSuffixopt = 129;
		static final int rhsBracketsPair_list_Comma_separated = 130;
		static final int annotation_list = 131;
		static final int map_entriesopt = 132;
		static final int expression_list_Comma_separated = 133;
		static final int expression_list_Comma_separated_opt = 134;
	}

	public interface Rules {
		static final int nonterm_type_nontermTypeAST = 77;  // nonterm_type ::= Lreturns symref
		static final int nonterm_type_nontermTypeHint = 80;  // nonterm_type ::= Linline Lclass identifieropt
		static final int nonterm_type_nontermTypeHint2 = 81;  // nonterm_type ::= Lclass identifieropt
		static final int nonterm_type_nontermTypeHint3 = 82;  // nonterm_type ::= Linterface identifieropt
		static final int nonterm_type_nontermTypeHint4 = 83;  // nonterm_type ::= Lvoid
		static final int nonterm_type_nontermTypeRaw = 84;  // nonterm_type ::= type
		static final int directive_directivePrio = 88;  // directive ::= '%' assoc references ';'
		static final int directive_directiveInput = 91;  // directive ::= '%' Linput inputref_list_Comma_separated ';'
		static final int rhsOptional_rhsQuantifier = 125;  // rhsOptional ::= rhsCast '?'
		static final int rhsCast_rhsAsLiteral = 128;  // rhsCast ::= rhsClass Las literal
		static final int rhsPrimary_rhsSymbol = 132;  // rhsPrimary ::= symref
		static final int rhsPrimary_rhsNested = 133;  // rhsPrimary ::= '(' rules ')'
		static final int rhsPrimary_rhsList = 134;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		static final int rhsPrimary_rhsList2 = 135;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		static final int rhsPrimary_rhsQuantifier = 136;  // rhsPrimary ::= rhsPrimary '*'
		static final int rhsPrimary_rhsQuantifier2 = 137;  // rhsPrimary ::= rhsPrimary '+'
		static final int rhsPrimary_rhsIgnored = 140;  // rhsPrimary ::= '$' '(' rules ';' rhsBracketsPair_list_Comma_separated ')'
		static final int rhsPrimary_rhsIgnored2 = 141;  // rhsPrimary ::= '$' '(' rules ')'
		static final int expression_instance = 153;  // expression ::= Lnew name '(' map_entriesopt ')'
		static final int expression_array = 158;  // expression ::= '[' expression_list_Comma_separated_opt ']'
		static final int literal_literal = 160;  // literal ::= scon
		static final int literal_literal2 = 161;  // literal ::= icon
		static final int literal_literal3 = 162;  // literal ::= Ltrue
		static final int literal_literal4 = 163;  // literal ::= Lfalse
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
		int tmShiftsAfterError = 4;

		tmStack[0] = new LapgSymbol();
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
					tmStack[0] = new LapgSymbol();
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
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 31) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new LapgSymbol();
			tmStack[tmHead].symbol = 31;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 31);
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
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol tmLeft = new LapgSymbol();
		tmLeft.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		tmLeft.symbol = tmRuleSymbol[rule];
		tmLeft.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		LapgSymbol startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		tmLeft.line = startsym.line;
		tmLeft.offset = startsym.offset;
		tmLeft.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(tmLeft, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			cleanup(tmStack[tmHead]);
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = tmLeft;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmLeft.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol tmLeft, int tmRule, int tmLength) {
		switch (tmRule) {
			case 0:  // import__optlist ::= import__optlist import_
				((List<TmaImport>)tmLeft.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 1:  // import__optlist ::=
				tmLeft.value = new ArrayList();
				break;
			case 2:  // option_optlist ::= option_optlist option
				((List<TmaOption>)tmLeft.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 3:  // option_optlist ::=
				tmLeft.value = new ArrayList();
				break;
			case 4:  // input ::= header import__optlist option_optlist lexer_section parser_section
				tmLeft.value = new TmaInput(
						((TmaHeader)tmStack[tmHead - 4].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 3].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 2].value) /* options */,
						((TmaLexerSection)tmStack[tmHead - 1].value) /* lexer */,
						((TmaParserSection)tmStack[tmHead].value) /* parser */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 5:  // input ::= header import__optlist option_optlist lexer_section
				tmLeft.value = new TmaInput(
						((TmaHeader)tmStack[tmHead - 3].value) /* header */,
						((List<TmaImport>)tmStack[tmHead - 2].value) /* imports */,
						((List<TmaOption>)tmStack[tmHead - 1].value) /* options */,
						((TmaLexerSection)tmStack[tmHead].value) /* lexer */,
						null /* parser */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // header ::= Llanguage name '(' name ')' parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 5].value) /* name */,
						((TmaName)tmStack[tmHead - 3].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						null /* input */, tmStack[tmHead - 6].line, tmStack[tmHead - 6].offset, tmStack[tmHead].endoffset);
				break;
			case 9:  // header ::= Llanguage name parsing_algorithmopt ';'
				tmLeft.value = new TmaHeader(
						((TmaName)tmStack[tmHead - 2].value) /* name */,
						null /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 1].value) /* parsingAlgorithm */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 10:  // lexer_section ::= '::' Llexer lexer_parts
				tmLeft.value = new TmaLexerSection(
						((List<TmaLexerPartsItem>)tmStack[tmHead].value) /* lexerParts */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // parser_section ::= '::' Lparser grammar_parts
				tmLeft.value = new TmaParserSection(
						((List<TmaGrammarPartsItem>)tmStack[tmHead].value) /* grammarParts */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 12:  // parsing_algorithm ::= Llalr '(' icon ')'
				tmLeft.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 13:  // import_ ::= Limport ID scon ';'
				tmLeft.value = new TmaImport(
						((String)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 14:  // import_ ::= Limport scon ';'
				tmLeft.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 15:  // option ::= ID '=' expression
				tmLeft.value = new TmaOption(
						((String)tmStack[tmHead - 2].value) /* key */,
						((ITmaExpression)tmStack[tmHead].value) /* value */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 16:  // option ::= syntax_problem
				tmLeft.value = new TmaOption(
						null /* key */,
						null /* value */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 17:  // identifier ::= ID
				tmLeft.value = new TmaIdentifier(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // symref ::= ID
				tmLeft.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // type ::= '(' scon ')'
				 tmLeft.value = ((String)tmStack[tmHead - 1].value); 
				break;
			case 20:  // type ::= '(' type_part_list ')'
				 tmLeft.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 36:  // pattern ::= regexp
				tmLeft.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // lexer_parts ::= lexer_part
				tmLeft.value = new ArrayList();
				((List<TmaLexerPartsItem>)tmLeft.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 38:  // lexer_parts ::= lexer_parts lexer_part
				((List<TmaLexerPartsItem>)tmLeft.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 39:  // lexer_parts ::= lexer_parts syntax_problem
				((List<TmaLexerPartsItem>)tmLeft.value).add(new TmaLexerPartsItem(
						null /* lexerPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 43:  // named_pattern ::= ID '=' pattern
				tmLeft.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // lexeme ::= identifier typeopt ':' pattern lexeme_transitionopt iconopt lexeme_attrsopt commandopt
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 7].value) /* name */,
						((String)tmStack[tmHead - 6].value) /* type */,
						((TmaPattern)tmStack[tmHead - 4].value) /* pattern */,
						((TmaStateref)tmStack[tmHead - 3].value) /* transition */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((TmaLexemeAttrs)tmStack[tmHead - 1].value) /* attrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						null /* input */, tmStack[tmHead - 7].line, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 55:  // lexeme ::= identifier typeopt ':'
				tmLeft.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((String)tmStack[tmHead - 1].value) /* type */,
						null /* pattern */,
						null /* transition */,
						null /* priority */,
						null /* attrs */,
						null /* command */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 56:  // lexeme_transition ::= '=>' stateref
				tmLeft.value = ((TmaStateref)tmStack[tmHead].value);
				break;
			case 57:  // lexeme_attrs ::= '(' lexeme_attribute ')'
				tmLeft.value = new TmaLexemeAttrs(
						((TmaLexemeAttribute)tmStack[tmHead - 1].value) /* kind */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 58:  // lexeme_attribute ::= Lsoft
				tmLeft.value = TmaLexemeAttribute.LSOFT;
				break;
			case 59:  // lexeme_attribute ::= Lclass
				tmLeft.value = TmaLexemeAttribute.LCLASS;
				break;
			case 60:  // lexeme_attribute ::= Lspace
				tmLeft.value = TmaLexemeAttribute.LSPACE;
				break;
			case 61:  // lexeme_attribute ::= Llayout
				tmLeft.value = TmaLexemeAttribute.LLAYOUT;
				break;
			case 62:  // lexer_state_list_Comma_separated ::= lexer_state_list_Comma_separated ',' lexer_state
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 63:  // lexer_state_list_Comma_separated ::= lexer_state
				tmLeft.value = new ArrayList();
				((List<TmaLexerState>)tmLeft.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 64:  // state_selector ::= '[' lexer_state_list_Comma_separated ']'
				tmLeft.value = new TmaStateSelector(
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 65:  // stateref ::= ID
				tmLeft.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 66:  // lexer_state ::= identifier '=>' stateref
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaStateref)tmStack[tmHead].value) /* defaultTransition */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 67:  // lexer_state ::= identifier
				tmLeft.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* defaultTransition */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 68:  // grammar_parts ::= grammar_part
				tmLeft.value = new ArrayList();
				((List<TmaGrammarPartsItem>)tmLeft.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 69:  // grammar_parts ::= grammar_parts grammar_part
				((List<TmaGrammarPartsItem>)tmLeft.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 70:  // grammar_parts ::= grammar_parts syntax_problem
				((List<TmaGrammarPartsItem>)tmLeft.value).add(new TmaGrammarPartsItem(
						null /* grammarPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 73:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 74:  // nonterm ::= annotations identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 75:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((ITmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // nonterm ::= identifier '::=' rules ';'
				tmLeft.value = new TmaNonterm(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* type */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // nonterm_type ::= Lreturns symref
				tmLeft.value = new TmaNontermTypeAST(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // nonterm_type ::= Linline Lclass identifieropt
				tmLeft.value = new TmaNontermTypeHint(
						true /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 81:  // nonterm_type ::= Lclass identifieropt
				tmLeft.value = new TmaNontermTypeHint(
						false /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LCLASS /* kind */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 82:  // nonterm_type ::= Linterface identifieropt
				tmLeft.value = new TmaNontermTypeHint(
						false /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LINTERFACE /* kind */,
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 83:  // nonterm_type ::= Lvoid
				tmLeft.value = new TmaNontermTypeHint(
						false /* isInline */,
						TmaNontermTypeHint.TmaKindKind.LVOID /* kind */,
						null /* name */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 84:  // nonterm_type ::= type
				tmLeft.value = new TmaNontermTypeRaw(
						((String)tmStack[tmHead].value) /* typeText */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 85:  // assoc ::= Lleft
				tmLeft.value = TmaAssoc.LLEFT;
				break;
			case 86:  // assoc ::= Lright
				tmLeft.value = TmaAssoc.LRIGHT;
				break;
			case 87:  // assoc ::= Lnonassoc
				tmLeft.value = TmaAssoc.LNONASSOC;
				break;
			case 88:  // directive ::= '%' assoc references ';'
				tmLeft.value = new TmaDirectivePrio(
						((TmaAssoc)tmStack[tmHead - 2].value) /* assoc */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* symbols */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // inputref_list_Comma_separated ::= inputref_list_Comma_separated ',' inputref
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 90:  // inputref_list_Comma_separated ::= inputref
				tmLeft.value = new ArrayList();
				((List<TmaInputref>)tmLeft.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 91:  // directive ::= '%' Linput inputref_list_Comma_separated ';'
				tmLeft.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputRefs */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 92:  // inputref ::= symref Lnoeoi
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead - 1].value) /* reference */,
						true /* noeoi */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 93:  // inputref ::= symref
				tmLeft.value = new TmaInputref(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						false /* noeoi */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 94:  // references ::= symref
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 95:  // references ::= references symref
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 96:  // references_cs ::= symref
				tmLeft.value = new ArrayList();
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 97:  // references_cs ::= references_cs ',' symref
				((List<TmaSymref>)tmLeft.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 98:  // rule0_list_Or_separated ::= rule0_list_Or_separated '|' rule0
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 99:  // rule0_list_Or_separated ::= rule0
				tmLeft.value = new ArrayList();
				((List<TmaRule0>)tmLeft.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 103:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // rule0 ::= rhsPrefix rhsSuffixopt
				tmLeft.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // rule0 ::= rhsParts rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // rule0 ::= rhsSuffixopt
				tmLeft.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // rule0 ::= syntax_problem
				tmLeft.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						null /* rhsSuffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rhsPrefix ::= '[' annotations ']'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						null /* alias */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 109:  // rhsPrefix ::= '[' annotations identifier ']'
				tmLeft.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 2].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 110:  // rhsPrefix ::= '[' identifier ']'
				tmLeft.value = new TmaRhsPrefix(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 111:  // rhsSuffix ::= '%' Lprio symref
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPRIO /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 112:  // rhsSuffix ::= '%' Lshift symref
				tmLeft.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 113:  // rhsParts ::= rhsPart
				tmLeft.value = new ArrayList();
				((List<TmaRhsPartsItem>)tmLeft.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 114:  // rhsParts ::= rhsParts rhsPart
				((List<TmaRhsPartsItem>)tmLeft.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 115:  // rhsParts ::= rhsParts syntax_problem
				((List<TmaRhsPartsItem>)tmLeft.value).add(new TmaRhsPartsItem(
						null /* rhsPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 120:  // rhsAnnotated ::= annotations rhsAssignment
				tmLeft.value = new TmaRhsAnnotated(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // rhsAssignment ::= identifier '=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // rhsAssignment ::= identifier '+=' rhsOptional
				tmLeft.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 125:  // rhsOptional ::= rhsCast '?'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.QUESTIONMARK /* quantifier */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // rhsCast ::= rhsClass Las symref
				tmLeft.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* rhsClass */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // rhsCast ::= rhsClass Las literal
				tmLeft.value = new TmaRhsAsLiteral(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* inner */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // rhsUnordered ::= rhsPart '&' rhsPart
				tmLeft.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 131:  // rhsClass ::= identifier ':' rhsPrimary
				tmLeft.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // rhsPrimary ::= symref
				tmLeft.value = new TmaRhsSymbol(
						((TmaSymref)tmStack[tmHead].value) /* reference */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // rhsPrimary ::= '(' rules ')'
				tmLeft.value = new TmaRhsNested(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 134:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				tmLeft.value = new TmaRhsList(
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						TmaRhsList.TmaQuantifierKind.PLUS /* quantifier */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 135:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				tmLeft.value = new TmaRhsList(
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						TmaRhsList.TmaQuantifierKind.MULT /* quantifier */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // rhsPrimary ::= rhsPrimary '*'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.MULT /* quantifier */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 137:  // rhsPrimary ::= rhsPrimary '+'
				tmLeft.value = new TmaRhsQuantifier(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* inner */,
						TmaRhsQuantifier.TmaQuantifierKind.PLUS /* quantifier */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // rhsBracketsPair_list_Comma_separated ::= rhsBracketsPair_list_Comma_separated ',' rhsBracketsPair
				((List<TmaRhsBracketsPair>)tmLeft.value).add(((TmaRhsBracketsPair)tmStack[tmHead].value));
				break;
			case 139:  // rhsBracketsPair_list_Comma_separated ::= rhsBracketsPair
				tmLeft.value = new ArrayList();
				((List<TmaRhsBracketsPair>)tmLeft.value).add(((TmaRhsBracketsPair)tmStack[tmHead].value));
				break;
			case 140:  // rhsPrimary ::= '$' '(' rules ';' rhsBracketsPair_list_Comma_separated ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 3].value) /* rules */,
						((List<TmaRhsBracketsPair>)tmStack[tmHead - 1].value) /* brackets */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 141:  // rhsPrimary ::= '$' '(' rules ')'
				tmLeft.value = new TmaRhsIgnored(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* brackets */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // rhsBracketsPair ::= symref '..' symref
				tmLeft.value = new TmaRhsBracketsPair(
						((TmaSymref)tmStack[tmHead - 2].value) /* lhs */,
						((TmaSymref)tmStack[tmHead].value) /* rhs */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 144:  // annotation_list ::= annotation
				tmLeft.value = new ArrayList();
				((List<TmaAnnotation>)tmLeft.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 145:  // annotations ::= annotation_list
				tmLeft.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 146:  // annotation ::= '@' ID '{' expression '}'
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead - 3].value) /* name */,
						((ITmaExpression)tmStack[tmHead - 1].value) /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 147:  // annotation ::= '@' ID
				tmLeft.value = new TmaAnnotation(
						((String)tmStack[tmHead].value) /* name */,
						null /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 148:  // annotation ::= '@' syntax_problem
				tmLeft.value = new TmaAnnotation(
						null /* name */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 153:  // expression ::= Lnew name '(' map_entriesopt ')'
				tmLeft.value = new TmaInstance(
						((TmaName)tmStack[tmHead - 3].value) /* className */,
						((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value) /* mapEntries */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 154:  // expression_list_Comma_separated ::= expression_list_Comma_separated ',' expression
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 155:  // expression_list_Comma_separated ::= expression
				tmLeft.value = new ArrayList();
				((List<ITmaExpression>)tmLeft.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 158:  // expression ::= '[' expression_list_Comma_separated_opt ']'
				tmLeft.value = new TmaArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* content */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // literal ::= scon
				tmLeft.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* sval */,
						null /* ival */,
						false /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // literal ::= icon
				tmLeft.value = new TmaLiteral(
						null /* sval */,
						((Integer)tmStack[tmHead].value) /* ival */,
						false /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // literal ::= Ltrue
				tmLeft.value = new TmaLiteral(
						null /* sval */,
						null /* ival */,
						true /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 163:  // literal ::= Lfalse
				tmLeft.value = new TmaLiteral(
						null /* sval */,
						null /* ival */,
						false /* val */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 164:  // map_entries ::= ID map_separator expression
				tmLeft.value = new ArrayList();
				((List<TmaMapEntriesItem>)tmLeft.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 165:  // map_entries ::= map_entries ',' ID map_separator expression
				((List<TmaMapEntriesItem>)tmLeft.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* name */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset));
				break;
			case 169:  // name ::= qualified_id
				tmLeft.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 171:  // qualified_id ::= qualified_id '.' ID
				 tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 172:  // command ::= code
				tmLeft.value = new TmaCommand(
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 173:  // syntax_problem ::= error
				tmLeft.value = new TmaSyntaxProblem(
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
		return (TmaInput) parse(lexer, 0, 281);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 282);
	}
}
