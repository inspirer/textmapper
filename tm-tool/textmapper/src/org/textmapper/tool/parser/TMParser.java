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
	private static final int[] tmAction = TMLexer.unpack_int(255,
		"\uffff\uffff\uffff\uffff\uffff\uffff\242\0\uffff\uffff\uffff\uffff\4\0\7\0\213\0" +
		"\214\0\ufffd\uffff\11\0\215\0\216\0\uffff\uffff\217\0\226\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\5\0\227\0\uffc5\uffff\uffff\uffff\237\0\uffff\uffff\uffbf\uffff\uffff" +
		"\uffff\uffb9\uffff\uffb1\uffff\uffab\uffff\35\0\41\0\42\0\40\0\6\0\uffff\uffff\uffff" +
		"\uffff\225\0\uff7b\uffff\uffff\uffff\10\0\uff51\uffff\uffff\uffff\67\0\uffff\uffff" +
		"\uffff\uffff\44\0\uffff\uffff\uffff\uffff\36\0\37\0\uff49\uffff\230\0\uffff\uffff" +
		"\uff19\uffff\uffff\uffff\240\0\uffff\uffff\uffff\uffff\66\0\34\0\43\0\uffff\uffff" +
		"\24\0\25\0\20\0\21\0\uff13\uffff\16\0\17\0\23\0\26\0\30\0\27\0\22\0\uffff\uffff\15" +
		"\0\ufed3\uffff\uffff\uffff\uffff\uffff\234\0\235\0\233\0\uffff\uffff\uffff\uffff" +
		"\222\0\71\0\72\0\70\0\12\0\ufea1\uffff\uffff\uffff\13\0\14\0\ufe61\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\ufe29\uffff\74\0\77\0\100\0\uffff\uffff\ufdf9\uffff\203" +
		"\0\uffff\uffff\231\0\uffff\uffff\33\0\uffff\uffff\46\0\ufdcf\uffff\uffff\uffff\114" +
		"\0\115\0\116\0\uffff\uffff\ufd99\uffff\207\0\ufd69\uffff\uffff\uffff\uffff\uffff" +
		"\ufd31\uffff\ufd07\uffff\113\0\uffff\uffff\75\0\76\0\uffff\uffff\204\0\ufcdd\uffff" +
		"\uffff\uffff\60\0\50\0\ufcad\uffff\ufc79\uffff\uffff\uffff\121\0\126\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\ufc71\uffff\uffff\uffff\ufc39\uffff\241\0\uffff\uffff\171" +
		"\0\uffff\uffff\ufbed\uffff\133\0\ufbe5\uffff\135\0\ufbad\uffff\ufb73\uffff\154\0" +
		"\157\0\161\0\ufb35\uffff\155\0\ufaf5\uffff\uffff\uffff\uffff\uffff\ufaaf\uffff\ufa81" +
		"\uffff\156\0\143\0\142\0\ufa55\uffff\105\0\106\0\111\0\112\0\ufa2b\uffff\uf9f3\uffff" +
		"\uffff\uffff\232\0\uffff\uffff\52\0\uf9bb\uffff\123\0\125\0\120\0\uffff\uffff\117" +
		"\0\127\0\uffff\uffff\uffff\uffff\150\0\uffff\uffff\uf989\uffff\211\0\uffff\uffff" +
		"\uffff\uffff\146\0\uffff\uffff\104\0\uf94f\uffff\uffff\uffff\uf915\uffff\uffff\uffff" +
		"\uf8db\uffff\140\0\uf8af\uffff\153\0\141\0\uffff\uffff\165\0\175\0\176\0\uffff\uffff" +
		"\uffff\uffff\160\0\144\0\uf871\uffff\110\0\uffff\uffff\uffff\uffff\uf845\uffff\62" +
		"\0\63\0\64\0\65\0\uffff\uffff\54\0\56\0\122\0\205\0\147\0\172\0\uffff\uffff\uffff" +
		"\uffff\210\0\162\0\163\0\134\0\137\0\uf80d\uffff\167\0\145\0\103\0\102\0\uffff\uffff" +
		"\61\0\uffff\uffff\212\0\101\0\uffff\uffff\174\0\173\0\uffff\uffff\uffff\uffff\ufffe" +
		"\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(2094,
		"\2\uffff\3\uffff\20\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\40\uffff\41\uffff\42\uffff\21\224\uffff" +
		"\ufffe\16\uffff\21\223\uffff\ufffe\15\uffff\22\236\uffff\ufffe\12\uffff\17\10\22" +
		"\10\uffff\ufffe\22\uffff\17\45\uffff\ufffe\10\uffff\20\uffff\36\uffff\37\uffff\67" +
		"\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56" +
		"\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\0" +
		"\3\uffff\ufffe\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61" +
		"\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\24\221\uffff\ufffe\13\uffff\16\73\21\73\uffff\ufffe\10\uffff" +
		"\20\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\0\1\uffff\ufffe\16\uffff\24\220\uffff\ufffe\15\uffff" +
		"\16\uffff\20\uffff\21\uffff\22\uffff\26\uffff\27\uffff\30\uffff\33\uffff\34\uffff" +
		"\35\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\24\32\uffff\ufffe\1\uffff\0\57\10\57\20\57\36\57\37\57\46\57\47" +
		"\57\50\57\51\57\52\57\53\57\54\57\55\57\56\57\57\57\60\57\61\57\62\57\63\57\64\57" +
		"\65\57\66\57\67\57\uffff\ufffe\15\uffff\16\uffff\20\uffff\21\uffff\22\uffff\26\uffff" +
		"\27\uffff\30\uffff\33\uffff\34\uffff\35\uffff\37\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\24\31\uffff\ufffe\13\uffff" +
		"\0\47\3\47\10\47\20\47\22\47\36\47\37\47\46\47\47\47\50\47\51\47\52\47\53\47\54\47" +
		"\55\47\56\47\57\47\60\47\61\47\62\47\63\47\64\47\65\47\66\47\67\47\71\47\uffff\ufffe" +
		"\6\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\0\2\uffff\ufffe\35\uffff\37\202\46\202\47\202" +
		"\50\202\51\202\52\202\53\202\54\202\55\202\56\202\57\202\60\202\61\202\62\202\63" +
		"\202\64\202\65\202\66\202\67\202\uffff\ufffe\3\uffff\0\51\10\51\20\51\22\51\36\51" +
		"\37\51\46\51\47\51\50\51\51\51\52\51\53\51\54\51\55\51\56\51\57\51\60\51\61\51\62" +
		"\51\63\51\64\51\65\51\66\51\67\51\71\51\uffff\ufffe\72\uffff\17\206\22\206\35\206" +
		"\37\206\46\206\47\206\50\206\51\206\52\206\53\206\54\206\55\206\56\206\57\206\60" +
		"\206\61\206\62\206\63\206\64\206\65\206\66\206\67\206\uffff\ufffe\6\uffff\22\uffff" +
		"\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\136\14\136\uffff\ufffe\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\7\107\uffff\ufffe\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\7\107\uffff\ufffe\6\uffff\35\uffff\36\uffff\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\0\0\uffff\ufffe\22\uffff\0\53\10\53\20\53\36\53\37\53\46\53\47\53\50\53\51\53\52" +
		"\53\53\53\54\53\55\53\56\53\57\53\60\53\61\53\62\53\63\53\64\53\65\53\66\53\67\53" +
		"\71\53\uffff\ufffe\56\uffff\14\124\16\124\uffff\ufffe\6\uffff\22\uffff\23\uffff\35" +
		"\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61" +
		"\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\71\uffff\11\136\24\136\uffff\ufffe\12\10\17\10\32\10\6\11" +
		"\11\11\14\11\22\11\23\11\24\11\30\11\31\11\33\11\34\11\35\11\36\11\37\11\43\11\44" +
		"\11\46\11\47\11\50\11\51\11\52\11\53\11\54\11\55\11\56\11\57\11\60\11\61\11\62\11" +
		"\63\11\64\11\65\11\66\11\67\11\71\11\uffff\ufffe\11\uffff\14\132\24\132\uffff\ufffe" +
		"\6\uffff\22\uffff\23\uffff\35\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\136\14\136\24\136\uffff" +
		"\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65" +
		"\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\136\14\136" +
		"\24\136\uffff\ufffe\34\uffff\6\151\11\151\14\151\22\151\23\151\24\151\35\151\36\151" +
		"\37\151\43\151\46\151\47\151\50\151\51\151\52\151\53\151\54\151\55\151\56\151\57" +
		"\151\60\151\61\151\62\151\63\151\64\151\65\151\66\151\67\151\71\151\uffff\ufffe\33" +
		"\uffff\6\164\11\164\14\164\22\164\23\164\24\164\34\164\35\164\36\164\37\164\43\164" +
		"\46\164\47\164\50\164\51\164\52\164\53\164\54\164\55\164\56\164\57\164\60\164\61" +
		"\164\62\164\63\164\64\164\65\164\66\164\67\164\71\164\uffff\ufffe\30\uffff\31\uffff" +
		"\44\uffff\6\166\11\166\14\166\22\166\23\166\24\166\33\166\34\166\35\166\36\166\37" +
		"\166\43\166\46\166\47\166\50\166\51\166\52\166\53\166\54\166\55\166\56\166\57\166" +
		"\60\166\61\166\62\166\63\166\64\166\65\166\66\166\67\166\71\166\uffff\ufffe\35\uffff" +
		"\22\177\37\177\46\177\47\177\50\177\51\177\52\177\53\177\54\177\55\177\56\177\57" +
		"\177\60\177\61\177\62\177\63\177\64\177\65\177\66\177\67\177\17\202\uffff\ufffe\35" +
		"\uffff\22\201\37\201\46\201\47\201\50\201\51\201\52\201\53\201\54\201\55\201\56\201" +
		"\57\201\60\201\61\201\62\201\63\201\64\201\65\201\66\201\67\201\uffff\ufffe\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\7\107\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff" +
		"\11\136\14\136\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67" +
		"\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56" +
		"\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71" +
		"\uffff\11\136\14\136\uffff\ufffe\71\uffff\0\55\10\55\20\55\36\55\37\55\46\55\47\55" +
		"\50\55\51\55\52\55\53\55\54\55\55\55\56\55\57\55\60\55\61\55\62\55\63\55\64\55\65" +
		"\55\66\55\67\55\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\43\uffff\71\uffff\11\136\24\136\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36" +
		"\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\71\uffff\11\136\14\136\24\136\uffff\ufffe\6\uffff\22\uffff\23\uffff" +
		"\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\71\uffff\11\136\14\136\24\136\uffff\ufffe\35\uffff\22" +
		"\177\37\177\46\177\47\177\50\177\51\177\52\177\53\177\54\177\55\177\56\177\57\177" +
		"\60\177\61\177\62\177\63\177\64\177\65\177\66\177\67\177\uffff\ufffe\34\uffff\6\152" +
		"\11\152\14\152\22\152\23\152\24\152\35\152\36\152\37\152\43\152\46\152\47\152\50" +
		"\152\51\152\52\152\53\152\54\152\55\152\56\152\57\152\60\152\61\152\62\152\63\152" +
		"\64\152\65\152\66\152\67\152\71\152\uffff\ufffe\35\uffff\22\200\37\200\46\200\47" +
		"\200\50\200\51\200\52\200\53\200\54\200\55\200\56\200\57\200\60\200\61\200\62\200" +
		"\63\200\64\200\65\200\66\200\67\200\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff" +
		"\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\71\uffff\11\136\14\136\uffff\ufffe\34\170\6\170\11\170\14\170" +
		"\22\170\23\170\24\170\35\170\36\170\37\170\43\170\46\170\47\170\50\170\51\170\52" +
		"\170\53\170\54\170\55\170\56\170\57\170\60\170\61\170\62\170\63\170\64\170\65\170" +
		"\66\170\67\170\71\170\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(128,
		"\0\2\4\14\24\24\24\42\46\52\54\63\67\75\102\112\120\137\145\175\210\220\221\225\231" +
		"\237\241\244\251\260\310\337\u011b\u0122\u0129\u0130\u0131\u0132\u0132\u0170\u01ad" +
		"\u01ea\u0228\u0265\u02a2\u02df\u031c\u0359\u0396\u03d6\u0414\u0451\u048e\u04ca\u0506" +
		"\u0544\u0582\u0582\u058e\u058f\u0590\u0591\u0593\u05ae\u05cf\u05d2\u05d4\u05d8\u05da" +
		"\u05dc\u05e0\u05e4\u05e8\u05e9\u05ea\u05eb\u05ef\u05f0\u05f2\u05f4\u05f6\u05fa\u05fe" +
		"\u0600\u0601\u0605\u0606\u0608\u060a\u060a\u060f\u0614\u061a\u0620\u062a\u0631\u063c" +
		"\u0647\u0654\u0663\u0672\u067d\u068c\u0697\u06a1\u06b1\u06c5\u06d0\u06d1\u06d8\u06d9" +
		"\u06da\u06dc\u06dd\u06de\u06ea\u0701\u0702\u0703\u0704\u0705\u0706\u0707\u070a\u070b" +
		"\u0715\u0716\u0717");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1815,
		"\373\374\55\116\1\12\22\45\56\124\204\215\1\12\22\45\124\160\204\215\117\143\152" +
		"\170\203\217\230\232\256\257\277\306\310\333\142\176\201\260\0\5\36\64\226\301\4" +
		"\34\66\154\223\307\324\52\66\137\154\211\214\225\331\332\363\32\56\104\114\133\26" +
		"\53\56\67\104\114\133\211\60\66\154\223\243\324\1\12\21\22\36\44\45\56\64\104\114" +
		"\124\133\204\215\27\53\56\104\114\133\31\35\56\104\114\133\142\170\201\207\217\230" +
		"\232\242\256\257\277\302\304\306\310\311\317\333\170\217\230\232\256\257\277\306" +
		"\310\317\333\70\77\114\134\276\301\340\365\273\56\104\114\133\56\104\114\133\56\104" +
		"\114\133\241\370\241\370\223\307\324\56\104\114\133\237\56\104\114\133\233\314\356" +
		"\56\104\114\117\133\143\150\152\170\203\217\230\232\244\245\256\257\277\306\310\312" +
		"\317\327\333\0\1\5\12\22\36\45\64\124\141\143\170\203\204\215\217\232\256\257\277" +
		"\306\310\333\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125" +
		"\133\141\143\147\152\156\161\165\170\172\173\174\203\204\214\215\217\220\230\232" +
		"\242\251\256\257\270\274\277\302\304\306\310\311\317\323\333\347\350\365\1\12\22" +
		"\45\124\204\215\1\12\22\45\124\204\215\1\12\22\45\124\204\215\277\241\0\1\5\12\16" +
		"\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125\133\141\142\143\147\152" +
		"\156\161\165\170\172\173\174\201\203\204\214\215\217\220\230\232\242\251\256\257" +
		"\270\274\277\302\304\306\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36" +
		"\44\45\47\50\56\64\72\73\104\114\117\124\125\133\141\143\147\152\156\161\165\170" +
		"\172\173\174\203\204\214\215\216\217\220\230\232\242\251\256\257\270\274\277\302" +
		"\304\306\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64" +
		"\72\73\104\114\117\124\125\133\141\143\147\152\156\161\165\170\172\173\174\203\204" +
		"\214\215\216\217\220\230\232\242\251\256\257\270\274\277\302\304\306\310\311\317" +
		"\323\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117" +
		"\124\125\133\141\142\143\147\152\156\161\165\170\172\173\174\201\203\204\214\215" +
		"\217\220\230\232\242\251\256\257\270\274\277\302\304\306\310\311\317\323\333\347" +
		"\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125\133" +
		"\140\141\143\147\152\156\161\165\170\172\173\174\203\204\214\215\217\220\230\232" +
		"\242\251\256\257\270\274\277\302\304\306\310\311\317\323\333\347\350\365\0\1\5\12" +
		"\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125\133\140\141\143\147\152" +
		"\156\161\165\170\172\173\174\203\204\214\215\217\220\230\232\242\251\256\257\270" +
		"\274\277\302\304\306\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45" +
		"\47\50\56\64\72\73\104\114\117\124\125\133\140\141\143\147\152\156\161\165\170\172" +
		"\173\174\203\204\214\215\217\220\230\232\242\251\256\257\270\274\277\302\304\306" +
		"\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73" +
		"\104\114\117\124\125\133\140\141\143\147\152\156\161\165\170\172\173\174\203\204" +
		"\214\215\217\220\230\232\242\251\256\257\270\274\277\302\304\306\310\311\317\323" +
		"\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124" +
		"\125\133\141\143\147\152\156\161\165\170\172\173\174\203\204\210\214\215\217\220" +
		"\230\232\242\251\256\257\270\274\277\302\304\306\310\311\317\323\333\347\350\365" +
		"\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125\133\141\143" +
		"\147\152\156\161\165\170\172\173\174\203\204\214\215\217\220\230\232\242\251\256" +
		"\257\262\270\274\277\302\304\306\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22" +
		"\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125\133\141\142\143\147\152\156\161" +
		"\165\170\171\172\173\174\201\203\204\214\215\217\220\230\232\242\251\256\257\262" +
		"\270\274\277\302\304\306\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36" +
		"\44\45\47\50\56\64\72\73\104\114\117\124\125\133\141\142\143\147\152\156\161\165" +
		"\170\172\173\174\201\203\204\214\215\217\220\230\232\242\251\256\257\270\274\277" +
		"\302\304\306\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50" +
		"\56\64\72\73\104\114\117\124\125\133\141\143\147\152\156\161\165\170\172\173\174" +
		"\203\204\214\215\217\220\230\232\242\251\256\257\262\270\274\277\302\304\306\310" +
		"\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73\104" +
		"\114\117\124\125\133\141\143\147\152\156\161\165\170\172\173\174\203\204\214\215" +
		"\217\220\230\232\242\251\256\257\262\270\274\277\302\304\306\310\311\317\323\333" +
		"\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125" +
		"\133\141\143\147\152\156\161\165\170\172\173\174\203\204\214\215\217\220\230\232" +
		"\242\251\256\257\270\274\277\302\304\306\310\311\317\323\333\347\350\365\0\1\5\12" +
		"\16\21\22\33\36\44\45\47\50\56\64\72\73\104\114\117\124\125\133\141\143\147\152\156" +
		"\161\165\170\172\173\174\203\204\214\215\217\220\230\232\242\251\256\257\270\274" +
		"\277\302\304\306\310\311\317\323\333\347\350\365\0\1\2\5\12\16\21\22\23\33\36\44" +
		"\45\47\50\56\64\72\73\104\114\117\124\125\133\141\143\147\152\156\161\165\170\172" +
		"\173\174\203\204\214\215\217\220\230\232\242\251\256\257\270\274\277\302\304\306" +
		"\310\311\317\323\333\347\350\365\0\1\5\12\16\21\22\33\36\44\45\47\50\56\61\64\72" +
		"\73\104\114\117\120\124\125\133\141\143\147\152\156\161\165\170\172\173\174\203\204" +
		"\214\215\217\220\230\232\242\251\256\257\270\274\277\302\304\306\310\311\317\323" +
		"\333\347\350\365\170\217\230\232\256\257\264\277\306\310\317\333\166\0\0\0\5\21\33" +
		"\36\44\64\73\117\143\147\152\170\173\174\203\217\230\232\242\251\256\257\277\306" +
		"\310\311\317\333\1\12\22\45\124\161\165\170\172\204\214\215\217\220\230\232\242\256" +
		"\257\270\274\277\302\304\306\310\311\317\323\333\347\350\365\35\142\201\56\104\56" +
		"\104\114\133\55\116\21\44\21\36\44\64\21\36\44\64\21\36\44\64\137\207\262\21\36\44" +
		"\64\33\72\156\33\73\117\152\117\143\152\203\117\143\152\203\142\201\140\117\143\152" +
		"\203\161\161\270\165\347\170\217\256\257\333\170\217\256\257\333\170\217\256\257" +
		"\306\333\170\217\256\257\306\333\170\217\230\232\256\257\277\306\310\333\170\217" +
		"\230\256\257\306\333\170\217\230\232\256\257\277\306\310\317\333\170\217\230\232" +
		"\256\257\277\306\310\317\333\170\217\230\232\242\256\257\277\306\310\311\317\333" +
		"\170\217\230\232\242\256\257\277\302\304\306\310\311\317\333\170\217\230\232\242" +
		"\256\257\277\302\304\306\310\311\317\333\170\217\230\232\256\257\277\306\310\317" +
		"\333\170\217\230\232\242\256\257\277\302\304\306\310\311\317\333\170\217\230\232" +
		"\256\257\277\306\310\317\333\117\143\152\170\203\217\256\257\306\333\117\143\152" +
		"\170\203\217\230\232\245\256\257\277\306\310\317\333\117\143\150\152\170\203\217" +
		"\230\232\244\245\256\257\277\306\310\312\317\327\333\170\217\230\232\256\257\277" +
		"\306\310\317\333\220\1\12\22\45\124\204\215\12\47\66\154\16\16\170\217\230\232\256" +
		"\257\264\277\306\310\317\333\0\1\5\12\22\36\45\64\124\141\143\170\203\204\215\217" +
		"\232\256\257\277\306\310\333\104\35\137\160\207\264\173\174\251\210\170\217\230\232" +
		"\256\257\277\306\310\333\47\12");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1815,
		"\375\376\75\75\10\10\10\10\77\10\10\10\11\11\11\11\11\206\11\11\140\140\140\216\140" +
		"\216\216\216\216\216\216\216\216\216\170\256\257\333\2\23\61\120\306\350\22\55\121" +
		"\121\302\302\302\72\122\156\122\267\271\305\361\362\367\50\100\100\100\100\45\73" +
		"\101\125\101\101\101\270\116\123\123\303\326\360\12\12\33\12\33\33\12\102\33\102" +
		"\102\12\102\12\12\46\74\103\103\103\103\47\56\104\104\104\104\56\217\56\262\217\217" +
		"\217\217\217\217\217\217\217\217\217\217\217\217\220\220\220\220\220\220\220\220" +
		"\220\220\220\126\132\135\155\346\351\364\370\344\105\105\105\105\106\106\106\106" +
		"\107\107\107\107\321\371\322\372\304\304\304\110\110\110\110\320\111\111\111\111" +
		"\317\317\317\112\112\112\141\112\141\141\141\141\141\141\141\141\141\141\141\141" +
		"\141\141\141\141\141\141\141\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\13\4" +
		"\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13\154\113\166\51\51\51\127" +
		"\13\13\221\13\51\51\51\13\13\13\221\13\221\221\221\51\221\221\13\13\221\13\13\221" +
		"\221\221\221\13\221\13\13\13\14\14\14\14\14\14\14\15\15\15\15\15\15\15\16\16\16\16" +
		"\16\16\16\347\323\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13" +
		"\154\113\166\171\51\51\51\127\13\13\221\13\51\51\171\51\13\13\13\221\13\221\221\221" +
		"\51\221\221\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13\30\34\13\51" +
		"\34\34\13\66\71\113\34\127\51\113\113\51\13\154\113\166\51\51\51\127\13\13\221\13" +
		"\51\51\51\13\13\13\274\221\13\221\221\221\51\221\221\13\13\221\13\13\221\221\221" +
		"\221\13\221\13\13\13\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51" +
		"\13\154\113\166\51\51\51\127\13\13\221\13\51\51\51\13\13\13\275\221\13\221\221\221" +
		"\51\221\221\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13\30\34\13\51" +
		"\34\34\13\66\71\113\34\127\51\113\113\51\13\154\113\166\172\51\51\51\127\13\13\221" +
		"\13\51\51\172\51\13\13\13\221\13\221\221\221\51\221\221\13\13\221\13\13\221\221\221" +
		"\221\13\221\13\13\13\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51" +
		"\13\154\113\161\166\51\51\51\127\13\13\221\13\51\51\51\13\13\13\221\13\221\221\221" +
		"\51\221\221\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13\30\34\13\51" +
		"\34\34\13\66\71\113\34\127\51\113\113\51\13\154\113\162\166\51\51\51\127\13\13\221" +
		"\13\51\51\51\13\13\13\221\13\221\221\221\51\221\221\13\13\221\13\13\221\221\221\221" +
		"\13\221\13\13\13\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13" +
		"\154\113\163\166\51\51\51\127\13\13\221\13\51\51\51\13\13\13\221\13\221\221\221\51" +
		"\221\221\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13\30\34\13\51\34" +
		"\34\13\66\71\113\34\127\51\113\113\51\13\154\113\164\166\51\51\51\127\13\13\221\13" +
		"\51\51\51\13\13\13\221\13\221\221\221\51\221\221\13\13\221\13\13\221\221\221\221" +
		"\13\221\13\13\13\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13" +
		"\154\113\166\51\51\51\127\13\13\221\13\51\51\51\13\265\13\13\221\13\221\221\221\51" +
		"\221\221\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13\30\34\13\51\34" +
		"\34\13\66\71\113\34\127\51\113\113\51\13\154\113\166\51\51\51\127\13\13\221\13\51" +
		"\51\51\13\13\13\221\13\221\221\221\51\221\221\334\13\13\221\13\13\221\221\221\221" +
		"\13\221\13\13\13\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13" +
		"\154\113\166\173\51\51\51\127\13\13\221\251\13\51\51\173\51\13\13\13\221\13\221\221" +
		"\221\51\221\221\335\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13\30" +
		"\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13\154\113\166\174\51\51\51\127" +
		"\13\13\221\13\51\51\174\51\13\13\13\221\13\221\221\221\51\221\221\13\13\221\13\13" +
		"\221\221\221\221\13\221\13\13\13\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127" +
		"\51\113\113\51\13\154\113\166\51\51\51\127\13\13\221\13\51\51\51\13\13\13\221\13" +
		"\221\221\221\51\221\221\336\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13" +
		"\4\13\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13\154\113\166\51\51\51" +
		"\127\13\13\221\13\51\51\51\13\13\13\221\13\221\221\221\51\221\221\337\13\13\221\13" +
		"\13\221\221\221\221\13\221\13\13\13\4\13\4\13\30\34\13\51\34\34\13\66\71\113\34\127" +
		"\51\113\113\51\13\154\113\166\51\51\51\127\13\13\221\13\51\51\51\13\13\13\221\13" +
		"\221\221\221\51\221\221\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13" +
		"\30\34\13\51\34\34\13\66\71\113\34\127\51\113\113\51\13\154\113\166\51\51\51\127" +
		"\13\13\221\13\51\51\51\13\13\13\221\13\221\221\221\51\221\221\13\13\221\13\13\221" +
		"\221\221\221\13\221\13\13\13\4\13\21\4\13\30\34\13\44\51\34\34\13\66\71\113\34\127" +
		"\51\113\113\51\13\154\113\166\51\51\51\127\13\13\221\13\51\51\51\13\13\13\221\13" +
		"\221\221\221\51\221\221\13\13\221\13\13\221\221\221\221\13\221\13\13\13\4\13\4\13" +
		"\30\34\13\51\34\34\13\66\71\113\117\34\127\51\113\113\51\152\13\154\113\166\51\51" +
		"\51\127\13\13\221\13\51\51\51\13\13\13\221\13\221\221\221\51\221\221\13\13\221\13" +
		"\13\221\221\221\221\13\221\13\13\13\222\222\222\222\222\222\222\222\222\222\222\222" +
		"\215\373\5\6\24\35\52\35\35\35\52\142\142\201\142\223\253\253\142\223\307\307\324" +
		"\253\223\223\307\223\307\307\307\223\17\17\17\17\17\210\213\224\252\17\272\17\224" +
		"\300\224\224\224\224\224\210\345\224\224\224\224\224\224\224\357\224\213\366\272" +
		"\57\175\175\114\133\115\115\136\136\76\137\36\64\37\62\37\62\40\40\40\40\41\41\41" +
		"\41\157\263\340\42\42\42\42\53\130\205\54\131\143\203\144\177\144\177\145\145\145" +
		"\145\176\260\165\146\146\146\146\211\212\343\214\365\225\276\331\332\363\226\226" +
		"\226\226\226\227\227\227\227\354\227\230\230\230\230\230\230\231\231\231\231\231" +
		"\231\231\231\231\231\232\277\310\232\232\232\232\233\233\233\314\233\233\314\233" +
		"\314\356\233\234\234\234\234\234\234\234\234\234\234\234\235\235\235\235\325\235" +
		"\235\235\235\235\325\235\235\236\236\236\236\236\236\236\236\352\353\236\236\236" +
		"\236\236\237\237\237\237\237\237\237\237\237\237\237\237\237\237\237\240\240\240" +
		"\240\240\240\240\240\240\240\240\241\241\241\241\241\241\241\241\241\241\241\241" +
		"\241\241\241\242\242\311\311\242\242\311\242\311\311\242\147\147\147\243\147\243" +
		"\243\243\243\243\150\150\150\244\150\244\312\312\327\244\244\312\244\312\312\244" +
		"\151\151\202\151\151\151\151\151\151\202\151\151\151\151\151\151\202\151\202\151" +
		"\245\245\245\245\245\245\245\245\245\245\245\301\374\25\43\65\153\261\273\26\67\124" +
		"\204\31\32\246\246\246\246\246\246\341\246\246\246\246\246\7\20\7\20\20\63\20\63" +
		"\20\167\200\247\200\20\20\247\315\247\247\315\247\315\247\134\60\160\207\264\342" +
		"\254\255\330\266\250\250\313\316\250\250\316\250\355\250\70\27");

	private static final short[] lapg_rlen = TMLexer.unpack_short(163,
		"\7\4\6\3\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\1\0\3\1\1\2\2\1\1\1\3\1\0\1\0" +
		"\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\6\5\5\4\2\1\0\3\2\2\1\1\1\1\4" +
		"\4\1\3\1\0\2\1\2\1\3\1\1\3\1\0\3\2\2\1\1\2\3\2\3\2\1\2\2\1\1\1\1\2\1\3\3\1\2\1\3" +
		"\3\1\3\6\6\2\2\1\2\1\1\1\2\5\2\2\3\1\3\1\1\1\1\1\1\0\5\1\0\3\1\1\3\3\5\1\1\1\1\1" +
		"\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(163,
		"\73\73\73\73\74\74\75\75\76\77\100\100\101\101\102\102\102\102\102\102\102\102\102" +
		"\102\102\164\164\102\103\104\104\104\105\105\105\106\165\165\166\166\167\167\170" +
		"\170\171\171\107\107\110\111\112\112\112\112\113\114\114\115\116\116\117\117\117" +
		"\120\120\121\121\121\121\122\172\172\122\122\122\122\123\123\123\124\124\125\125" +
		"\173\173\126\127\127\130\130\131\132\132\174\174\133\133\133\133\133\134\134\134" +
		"\135\135\136\136\136\137\137\137\140\140\141\141\141\142\142\143\143\144\145\145" +
		"\145\145\145\145\146\146\146\147\150\150\151\151\151\152\153\153\154\154\154\154" +
		"\154\175\175\154\176\176\154\154\155\155\156\156\157\157\157\160\161\161\162\163");

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
		"type_part_listopt",
		"typeopt",
		"lexem_transitionopt",
		"iconopt",
		"lexem_attrsopt",
		"commandopt",
		"identifieropt",
		"Lnoeoiopt",
		"rhsSuffixopt",
		"map_entriesopt",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 59;
		public static final int options = 60;
		public static final int option = 61;
		public static final int identifier = 62;
		public static final int symref = 63;
		public static final int type = 64;
		public static final int type_part_list = 65;
		public static final int type_part = 66;
		public static final int pattern = 67;
		public static final int lexer_parts = 68;
		public static final int lexer_part = 69;
		public static final int named_pattern = 70;
		public static final int lexeme = 71;
		public static final int lexem_transition = 72;
		public static final int lexem_attrs = 73;
		public static final int lexem_attribute = 74;
		public static final int state_selector = 75;
		public static final int state_list = 76;
		public static final int stateref = 77;
		public static final int lexer_state = 78;
		public static final int grammar_parts = 79;
		public static final int grammar_part = 80;
		public static final int nonterm = 81;
		public static final int nonterm_type = 82;
		public static final int priority_kw = 83;
		public static final int directive = 84;
		public static final int inputs = 85;
		public static final int inputref = 86;
		public static final int references = 87;
		public static final int references_cs = 88;
		public static final int rules = 89;
		public static final int rule_list = 90;
		public static final int rule0 = 91;
		public static final int rhsPrefix = 92;
		public static final int rhsSuffix = 93;
		public static final int rhsParts = 94;
		public static final int rhsPart = 95;
		public static final int rhsAnnotated = 96;
		public static final int rhsAssignment = 97;
		public static final int rhsOptional = 98;
		public static final int rhsCast = 99;
		public static final int rhsUnordered = 100;
		public static final int rhsPrimary = 101;
		public static final int rhsAnnotations = 102;
		public static final int annotations = 103;
		public static final int annotation_list = 104;
		public static final int annotation = 105;
		public static final int negative_la = 106;
		public static final int negative_la_clause = 107;
		public static final int expression = 108;
		public static final int expression_list = 109;
		public static final int map_entries = 110;
		public static final int map_separator = 111;
		public static final int name = 112;
		public static final int qualified_id = 113;
		public static final int command = 114;
		public static final int syntax_problem = 115;
		public static final int type_part_listopt = 116;
		public static final int typeopt = 117;
		public static final int lexem_transitionopt = 118;
		public static final int iconopt = 119;
		public static final int lexem_attrsopt = 120;
		public static final int commandopt = 121;
		public static final int identifieropt = 122;
		public static final int Lnoeoiopt = 123;
		public static final int rhsSuffixopt = 124;
		public static final int map_entriesopt = 125;
		public static final int expression_listopt = 126;
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
			case 0:  // input ::= options '::' Llexer lexer_parts '::' Lparser grammar_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)tmStack[tmHead - 6].value), ((List<ITmaLexerPart>)tmStack[tmHead - 3].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 1:  // input ::= options '::' Llexer lexer_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)tmStack[tmHead - 3].value), ((List<ITmaLexerPart>)tmStack[tmHead].value), ((List<ITmaGrammarPart>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 2:  // input ::= '::' Llexer lexer_parts '::' Lparser grammar_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)null), ((List<ITmaLexerPart>)tmStack[tmHead - 3].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // input ::= '::' Llexer lexer_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)null), ((List<ITmaLexerPart>)tmStack[tmHead].value), ((List<ITmaGrammarPart>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
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
			case 65:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 5].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // nonterm ::= annotations identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 4].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // nonterm ::= identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // nonterm_type ::= Lreturns symref
				 lapg_gg.value = new TmaNontermTypeAST(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // nonterm_type ::= Linline Lclass identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 73:  // nonterm_type ::= Lclass identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 74:  // nonterm_type ::= Linterface identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
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
				 lapg_gg.value = new TmaRhsPrefix(((TmaAnnotations)tmStack[tmHead - 1].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // rhsPrefix ::= rhsAnnotations identifier ':'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)tmStack[tmHead - 2].value), ((TmaIdentifier)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // rhsPrefix ::= identifier ':'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)null), ((TmaIdentifier)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // rhsSuffix ::= '%' Lprio symref
				 lapg_gg.value = new TmaRhsPrio(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // rhsSuffix ::= '%' Lshift
				 lapg_gg.value = new TmaRhsShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // rhsParts ::= rhsPart
				 lapg_gg.value = new ArrayList<ITmaRhsPart>(); ((List<ITmaRhsPart>)lapg_gg.value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 106:  // rhsParts ::= rhsParts rhsPart
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 107:  // rhsParts ::= rhsParts syntax_problem
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 112:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				 lapg_gg.value = new TmaRhsAnnotated(((TmaRuleAnnotations)tmStack[tmHead - 1].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // rhsAssignment ::= identifier '=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // rhsAssignment ::= identifier '+=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // rhsOptional ::= rhsCast '?'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // rhsCast ::= rhsPrimary Las symref
				 lapg_gg.value = new TmaRhsCast(((ITmaRhsPart)tmStack[tmHead - 2].value), ((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // rhsUnordered ::= rhsPart '&' rhsPart
				 lapg_gg.value = new TmaRhsUnordered(((ITmaRhsPart)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // rhsPrimary ::= symref
				 lapg_gg.value = new TmaRhsSymbol(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // rhsPrimary ::= '(' rules ')'
				 lapg_gg.value = new TmaRhsNested(((List<TmaRule0>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 123:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // rhsPrimary ::= rhsPrimary '*'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // rhsPrimary ::= rhsPrimary '+'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // rhsAnnotations ::= annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(null, ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // rhsAnnotations ::= negative_la annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead - 1].value), ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // rhsAnnotations ::= negative_la
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // annotations ::= annotation_list
				 lapg_gg.value = new TmaAnnotations(((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // annotation_list ::= annotation
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 132:  // annotation_list ::= annotation_list annotation
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 133:  // annotation ::= '@' ID '{' expression '}'
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead - 3].value), ((ITmaExpression)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 134:  // annotation ::= '@' ID
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead].value), ((ITmaExpression)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 135:  // annotation ::= '@' syntax_problem
				 lapg_gg.value = new TmaMapEntriesItem(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 136:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.value = new TmaNegativeLa(((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 137:  // negative_la_clause ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 138:  // negative_la_clause ::= negative_la_clause '|' symref
				 ((List<TmaSymref>)tmStack[tmHead - 2].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 139:  // expression ::= scon
				 lapg_gg.value = new TmaExpressionLiteral(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 140:  // expression ::= icon
				 lapg_gg.value = new TmaExpressionLiteral(((Integer)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 141:  // expression ::= Ltrue
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 142:  // expression ::= Lfalse
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 146:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.value = new TmaExpressionInstance(((TmaName)tmStack[tmHead - 3].value), ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 149:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.value = new TmaExpressionArray(((List<ITmaExpression>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 151:  // expression_list ::= expression
				 lapg_gg.value = new ArrayList(); ((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 152:  // expression_list ::= expression_list ',' expression
				 ((List<ITmaExpression>)tmStack[tmHead - 2].value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 153:  // map_entries ::= ID map_separator expression
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 154:  // map_entries ::= map_entries ',' ID map_separator expression
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 4].value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, tmStack[tmHead - 2].offset, lapg_gg.endoffset)); 
				break;
			case 158:  // name ::= qualified_id
				 lapg_gg.value = new TmaName(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 160:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 161:  // command ::= code
				 lapg_gg.value = new TmaCommand(source, tmStack[tmHead].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 162:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 253);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 254);
	}
}
