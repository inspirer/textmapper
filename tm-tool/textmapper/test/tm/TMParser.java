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
	private static final int[] tmAction = TMLexer.unpack_int(275,
		"\uffff\uffff\uffff\uffff\uffff\uffff\236\0\237\0\ufffd\uffff\253\0\16\0\240\0\241" +
		"\0\uffff\uffff\224\0\223\0\235\0\uffff\uffff\231\0\uffc5\uffff\uffff\uffff\250\0" +
		"\uffff\uffff\uffbf\uffff\uffff\uffff\uffff\uffff\234\0\uffb9\uffff\uffff\uffff\uffff" +
		"\uffff\230\0\uffff\uffff\uff8f\uffff\uffff\uffff\251\0\uff89\uffff\245\0\246\0\244" +
		"\0\uffff\uffff\uffff\uffff\227\0\uffff\uffff\0\0\uffff\uffff\242\0\uffff\uffff\uffff" +
		"\uffff\uff83\uffff\uffff\uffff\uffff\uffff\uff55\uffff\243\0\10\0\uffff\uffff\2\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\4\0\14\0\12\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\11\0\uffff\uffff\uff27\uffff\uff1f\uffff\uff19\uffff" +
		"\41\0\45\0\46\0\44\0\13\0\15\0\ufee9\uffff\73\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\50\0\uffff\uffff\uffff\uffff\42\0\43\0\uffff\uffff\uffff\uffff\74\0\40\0\47\0\uffff" +
		"\uffff\31\0\32\0\25\0\26\0\uffff\uffff\23\0\24\0\30\0\33\0\35\0\34\0\27\0\uffff\uffff" +
		"\22\0\ufee1\uffff\uffff\uffff\75\0\76\0\72\0\17\0\37\0\uffff\uffff\20\0\21\0\ufeaf" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufe77\uffff\100\0\103\0\104\0\uffff\uffff" +
		"\206\0\ufe47\uffff\36\0\uffff\uffff\52\0\ufe1b\uffff\uffff\uffff\120\0\121\0\122" +
		"\0\uffff\uffff\ufde5\uffff\217\0\ufdb3\uffff\uffff\uffff\uffff\uffff\ufd79\uffff" +
		"\ufd4f\uffff\117\0\uffff\uffff\101\0\102\0\uffff\uffff\205\0\64\0\54\0\ufd25\uffff" +
		"\ufcf1\uffff\125\0\uffff\uffff\131\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\ufce9\uffff\uffff\uffff\ufcaf\uffff\252\0\uffff\uffff\177\0\uffff\uffff\136\0\ufc63" +
		"\uffff\140\0\ufc2b\uffff\ufbf1\uffff\157\0\162\0\164\0\ufbb3\uffff\160\0\ufb73\uffff" +
		"\ufb31\uffff\uffff\uffff\ufaeb\uffff\161\0\146\0\ufabf\uffff\145\0\ufab7\uffff\ufa8b" +
		"\uffff\111\0\112\0\115\0\116\0\ufa61\uffff\ufa27\uffff\uffff\uffff\uffff\uffff\56" +
		"\0\uf9ed\uffff\127\0\126\0\uffff\uffff\123\0\132\0\214\0\uffff\uffff\uffff\uffff" +
		"\153\0\uffff\uffff\uffff\uffff\uffff\uffff\uf9bb\uffff\221\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\110\0\uf981\uffff\143\0\uf947\uffff\156\0\144\0\uffff\uffff" +
		"\170\0\uffff\uffff\203\0\204\0\163\0\uf909\uffff\uf8dd\uffff\114\0\uffff\uffff\uffff" +
		"\uffff\uf8a1\uffff\66\0\67\0\70\0\71\0\uffff\uffff\60\0\62\0\124\0\uffff\uffff\215" +
		"\0\152\0\151\0\147\0\uffff\uffff\200\0\uffff\uffff\uffff\uffff\222\0\uffff\uffff" +
		"\165\0\uf867\uffff\166\0\142\0\uf821\uffff\172\0\173\0\135\0\107\0\106\0\uffff\uffff" +
		"\65\0\213\0\150\0\uffff\uffff\220\0\105\0\uffff\uffff\202\0\201\0\uffff\uffff\uffff" +
		"\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(2074,
		"\2\uffff\3\uffff\20\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\40\uffff\41\uffff\42\uffff\21\233\uffff" +
		"\ufffe\16\uffff\21\232\uffff\ufffe\15\uffff\22\247\uffff\ufffe\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\24\226\uffff" +
		"\ufffe\16\uffff\24\225\uffff\ufffe\65\uffff\14\1\uffff\ufffe\10\3\36\3\37\3\45\3" +
		"\46\3\47\3\50\3\51\3\52\3\53\3\54\3\55\3\56\3\57\3\60\3\61\3\62\3\63\3\64\3\65\3" +
		"\66\3\67\3\uffff\ufffe\45\uffff\10\5\36\5\37\5\46\5\47\5\50\5\51\5\52\5\53\5\54\5" +
		"\55\5\56\5\57\5\60\5\61\5\62\5\63\5\64\5\65\5\66\5\67\5\uffff\ufffe\12\uffff\17\15" +
		"\22\15\uffff\ufffe\22\uffff\17\51\uffff\ufffe\10\uffff\20\uffff\36\uffff\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\0\7\uffff\ufffe\13\uffff\16\77\21\77\uffff\ufffe\1\uffff\0\63\10\63\20\63\36\63" +
		"\37\63\46\63\47\63\50\63\51\63\52\63\53\63\54\63\55\63\56\63\57\63\60\63\61\63\62" +
		"\63\63\63\64\63\65\63\66\63\67\63\uffff\ufffe\13\uffff\0\53\3\53\10\53\20\53\22\53" +
		"\36\53\37\53\46\53\47\53\50\53\51\53\52\53\53\53\54\53\55\53\56\53\57\53\60\53\61" +
		"\53\62\53\63\53\64\53\65\53\66\53\67\53\71\53\uffff\ufffe\6\uffff\35\uffff\36\uffff" +
		"\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\0\6\uffff\ufffe\35\uffff\21\212\37\212\46\212\47\212\50\212\51\212\52\212" +
		"\53\212\54\212\55\212\56\212\57\212\60\212\61\212\62\212\63\212\64\212\65\212\66" +
		"\212\67\212\uffff\ufffe\3\uffff\0\55\10\55\20\55\22\55\36\55\37\55\46\55\47\55\50" +
		"\55\51\55\52\55\53\55\54\55\55\55\56\55\57\55\60\55\61\55\62\55\63\55\64\55\65\55" +
		"\66\55\67\55\71\55\uffff\ufffe\15\uffff\72\uffff\21\216\22\216\35\216\37\216\46\216" +
		"\47\216\50\216\51\216\52\216\53\216\54\216\55\216\56\216\57\216\60\216\61\216\62" +
		"\216\63\216\64\216\65\216\66\216\67\216\uffff\ufffe\6\uffff\20\uffff\22\uffff\23" +
		"\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62" +
		"\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\141\14\141\uffff\ufffe\37\uffff\67" +
		"\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56" +
		"\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\7" +
		"\113\uffff\ufffe\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61" +
		"\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\7\113\uffff\ufffe\22\uffff\0\57\10\57\20\57\36\57\37\57" +
		"\46\57\47\57\50\57\51\57\52\57\53\57\54\57\55\57\56\57\57\57\60\57\61\57\62\57\63" +
		"\57\64\57\65\57\66\57\67\57\71\57\uffff\ufffe\56\uffff\14\130\16\130\uffff\ufffe" +
		"\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff" +
		"\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff" +
		"\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\141\24" +
		"\141\uffff\ufffe\12\15\17\15\32\15\6\16\11\16\14\16\22\16\23\16\24\16\30\16\31\16" +
		"\33\16\34\16\35\16\36\16\37\16\43\16\44\16\46\16\47\16\50\16\51\16\52\16\53\16\54" +
		"\16\55\16\56\16\57\16\60\16\61\16\62\16\63\16\64\16\65\16\66\16\67\16\71\16\uffff" +
		"\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\141\14\141\24\141" +
		"\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff" +
		"\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff" +
		"\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\141\14" +
		"\141\24\141\uffff\ufffe\34\uffff\6\154\11\154\14\154\22\154\23\154\24\154\35\154" +
		"\36\154\37\154\43\154\46\154\47\154\50\154\51\154\52\154\53\154\54\154\55\154\56" +
		"\154\57\154\60\154\61\154\62\154\63\154\64\154\65\154\66\154\67\154\71\154\uffff" +
		"\ufffe\33\uffff\6\167\11\167\14\167\22\167\23\167\24\167\34\167\35\167\36\167\37" +
		"\167\43\167\46\167\47\167\50\167\51\167\52\167\53\167\54\167\55\167\56\167\57\167" +
		"\60\167\61\167\62\167\63\167\64\167\65\167\66\167\67\167\71\167\uffff\ufffe\44\uffff" +
		"\6\171\11\171\14\171\22\171\23\171\24\171\33\171\34\171\35\171\36\171\37\171\43\171" +
		"\46\171\47\171\50\171\51\171\52\171\53\171\54\171\55\171\56\171\57\171\60\171\61" +
		"\171\62\171\63\171\64\171\65\171\66\171\67\171\71\171\uffff\ufffe\30\uffff\31\uffff" +
		"\6\175\11\175\14\175\22\175\23\175\24\175\33\175\34\175\35\175\36\175\37\175\43\175" +
		"\44\175\46\175\47\175\50\175\51\175\52\175\53\175\54\175\55\175\56\175\57\175\60" +
		"\175\61\175\62\175\63\175\64\175\65\175\66\175\67\175\71\175\uffff\ufffe\35\uffff" +
		"\22\211\37\211\46\211\47\211\50\211\51\211\52\211\53\211\54\211\55\211\56\211\57" +
		"\211\60\211\61\211\62\211\63\211\64\211\65\211\66\211\67\211\uffff\ufffe\11\uffff" +
		"\14\137\24\137\uffff\ufffe\35\uffff\22\207\37\207\46\207\47\207\50\207\51\207\52" +
		"\207\53\207\54\207\55\207\56\207\57\207\60\207\61\207\62\207\63\207\64\207\65\207" +
		"\66\207\67\207\uffff\ufffe\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\7\113\uffff\ufffe\6\uffff\20\uffff\22\uffff" +
		"\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\141\14\141\uffff\ufffe\6\uffff\20" +
		"\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\141\14\141\uffff" +
		"\ufffe\71\uffff\0\61\10\61\20\61\36\61\37\61\46\61\47\61\50\61\51\61\52\61\53\61" +
		"\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\uffff\ufffe" +
		"\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff" +
		"\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff" +
		"\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\43\uffff\71\uffff\11\141\24" +
		"\141\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66" +
		"\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11" +
		"\141\14\141\24\141\uffff\ufffe\34\uffff\6\155\11\155\14\155\22\155\23\155\24\155" +
		"\35\155\36\155\37\155\43\155\46\155\47\155\50\155\51\155\52\155\53\155\54\155\55" +
		"\155\56\155\57\155\60\155\61\155\62\155\63\155\64\155\65\155\66\155\67\155\71\155" +
		"\uffff\ufffe\35\uffff\22\210\37\210\46\210\47\210\50\210\51\210\52\210\53\210\54" +
		"\210\55\210\56\210\57\210\60\210\61\210\62\210\63\210\64\210\65\210\66\210\67\210" +
		"\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff" +
		"\11\141\14\141\24\141\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36" +
		"\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\71\uffff\11\141\14\141\uffff\ufffe\30\uffff\31\uffff\6\176\11\176" +
		"\14\176\22\176\23\176\24\176\33\176\34\176\35\176\36\176\37\176\43\176\44\176\46" +
		"\176\47\176\50\176\51\176\52\176\53\176\54\176\55\176\56\176\57\176\60\176\61\176" +
		"\62\176\63\176\64\176\65\176\66\176\67\176\71\176\uffff\ufffe\34\174\6\174\11\174" +
		"\14\174\22\174\23\174\24\174\35\174\36\174\37\174\43\174\46\174\47\174\50\174\51" +
		"\174\52\174\53\174\54\174\55\174\56\174\57\174\60\174\61\174\62\174\63\174\64\174" +
		"\65\174\66\174\67\174\71\174\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(133,
		"\0\2\4\20\33\33\33\47\53\55\57\64\70\101\107\120\125\151\162\214\227\242\243\247" +
		"\253\262\265\266\273\302\330\355\u012a\u0133\u013c\u0144\u0145\u0146\u0147\u0186" +
		"\u01c4\u0202\u0241\u027f\u02bd\u02fb\u0339\u0377\u03b5\u03f6\u0435\u0473\u04b1\u04ef" +
		"\u052d\u056b\u05a9\u05a9\u05b5\u05b6\u05b7\u05b8\u05b9\u05ba\u05d4\u05f6\u05f9\u05fb" +
		"\u05ff\u0601\u0602\u0604\u0606\u0608\u0609\u060a\u060b\u060d\u060f\u0611\u0612\u0614" +
		"\u0616\u0618\u0619\u061b\u061d\u061f\u061f\u0624\u062a\u0630\u063a\u0641\u064c\u0657" +
		"\u0663\u0671\u067f\u068a\u0698\u06a7\u06b2\u06b5\u06c7\u06d2\u06da\u06e3\u06e4\u06e6" +
		"\u06e7\u06e9\u06f5\u070a\u070b\u070c\u070d\u070e\u070f\u0710\u0711\u0712\u0713\u0716" +
		"\u0717\u071c\u0726\u0735\u0736\u0737\u0738\u0739\u073a");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1850,
		"\u010f\u0110\116\151\1\5\26\44\56\63\67\77\117\234\336\360\1\5\26\44\54\56\77\201" +
		"\234\336\360\152\167\211\237\247\251\276\277\320\327\343\347\166\217\222\300\65\104" +
		"\266\322\34\53\71\102\243\34\53\113\163\51\66\75\231\233\245\345\346\u0105\24\117" +
		"\137\147\160\207\20\35\115\117\137\147\160\231\312\34\53\121\243\372\1\5\26\44\56" +
		"\76\77\104\117\137\147\160\211\234\237\276\277\343\347\360\21\115\117\137\147\160" +
		"\315\316\365\16\23\47\103\117\137\147\160\166\211\222\226\237\247\251\262\276\277" +
		"\320\323\324\325\327\334\343\347\211\237\247\251\276\277\320\327\334\343\347\32\36" +
		"\57\132\137\147\160\317\322\354\u0109\312\117\137\147\160\117\137\147\160\117\137" +
		"\147\160\261\374\u010c\261\374\u010c\243\117\137\147\160\256\117\137\147\160\252" +
		"\331\377\117\137\147\152\160\167\175\211\236\237\247\251\263\270\276\277\320\327" +
		"\334\342\343\347\1\5\26\44\56\65\77\104\165\167\211\234\237\251\276\277\320\327\343" +
		"\347\360\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152" +
		"\160\165\167\173\177\202\206\211\213\214\215\233\234\236\237\240\247\251\262\271" +
		"\276\277\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109\1\5\26" +
		"\44\56\77\234\336\360\1\5\26\44\56\77\234\336\360\1\5\26\44\56\77\234\360\320\260" +
		"\60\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160" +
		"\165\166\167\173\177\202\206\211\213\214\215\222\233\234\236\237\240\247\251\262" +
		"\271\276\277\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109\1" +
		"\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165" +
		"\167\173\177\202\206\211\213\214\215\233\234\235\236\237\240\247\251\262\271\276" +
		"\277\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109\1\2\5\12\25" +
		"\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165\167\173\177" +
		"\202\206\211\213\214\215\233\234\235\236\237\240\247\251\262\271\276\277\306\313" +
		"\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109\1\2\5\12\25\26\30\31\44" +
		"\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165\166\167\173\177\202\206" +
		"\211\213\214\215\222\233\234\236\237\240\247\251\262\271\276\277\306\313\316\320" +
		"\323\324\325\327\334\336\343\347\360\367\370\u0109\1\2\5\12\25\26\30\31\44\45\56" +
		"\63\65\76\77\101\104\117\125\126\137\147\152\160\164\165\167\173\177\202\206\211" +
		"\213\214\215\233\234\236\237\240\247\251\262\271\276\277\306\313\316\320\323\324" +
		"\325\327\334\336\343\347\360\367\370\u0109\1\2\5\12\25\26\30\31\44\45\56\63\65\76" +
		"\77\101\104\117\125\126\137\147\152\160\164\165\167\173\177\202\206\211\213\214\215" +
		"\233\234\236\237\240\247\251\262\271\276\277\306\313\316\320\323\324\325\327\334" +
		"\336\343\347\360\367\370\u0109\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104" +
		"\117\125\126\137\147\152\160\164\165\167\173\177\202\206\211\213\214\215\233\234" +
		"\236\237\240\247\251\262\271\276\277\306\313\316\320\323\324\325\327\334\336\343" +
		"\347\360\367\370\u0109\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125" +
		"\126\137\147\152\160\164\165\167\173\177\202\206\211\213\214\215\233\234\236\237" +
		"\240\247\251\262\271\276\277\306\313\316\320\323\324\325\327\334\336\343\347\360" +
		"\367\370\u0109\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137" +
		"\147\152\160\165\167\173\177\202\206\211\213\214\215\227\233\234\236\237\240\247" +
		"\251\262\271\276\277\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370" +
		"\u0109\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152" +
		"\160\165\167\173\177\202\206\211\213\214\215\233\234\236\237\240\247\251\262\271" +
		"\276\277\301\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109\1" +
		"\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165" +
		"\166\167\173\177\202\206\211\212\213\214\215\222\233\234\236\237\240\247\251\262" +
		"\271\276\277\301\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109" +
		"\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165" +
		"\166\167\173\177\202\206\211\213\214\215\222\233\234\236\237\240\247\251\262\271" +
		"\276\277\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109\1\2\5" +
		"\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165\167" +
		"\173\177\202\206\211\213\214\215\233\234\236\237\240\247\251\262\271\276\277\301" +
		"\306\313\316\320\323\324\325\327\334\336\343\347\360\367\370\u0109\1\2\5\12\25\26" +
		"\30\31\44\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165\167\173\177\202" +
		"\206\211\213\214\215\233\234\236\237\240\247\251\262\271\276\277\301\306\313\316" +
		"\320\323\324\325\327\334\336\343\347\360\367\370\u0109\0\1\2\5\12\25\26\30\31\44" +
		"\45\56\63\65\76\77\101\104\117\125\126\137\147\152\160\165\167\173\177\202\206\211" +
		"\213\214\215\233\234\236\237\240\247\251\262\271\276\277\306\313\316\320\323\324" +
		"\325\327\334\336\343\347\360\367\370\u0109\1\2\5\12\25\26\30\31\40\44\45\56\63\65" +
		"\76\77\101\104\117\125\126\137\147\152\160\165\167\173\177\202\206\211\213\214\215" +
		"\233\234\236\237\240\247\251\262\271\276\277\306\313\316\320\323\324\325\327\334" +
		"\336\343\347\360\367\370\u0109\1\2\5\12\25\26\30\31\44\45\56\63\65\70\76\77\101\104" +
		"\117\125\126\137\147\152\160\165\167\173\177\202\206\211\213\214\215\233\234\236" +
		"\237\240\247\251\262\271\276\277\306\313\316\320\323\324\325\327\334\336\343\347" +
		"\360\367\370\u0109\1\2\5\12\25\26\30\31\44\45\56\63\65\76\77\101\104\117\122\125" +
		"\126\137\147\152\160\165\167\173\177\202\206\211\213\214\215\233\234\236\237\240" +
		"\247\251\262\271\276\277\306\313\316\320\323\324\325\327\334\336\343\347\360\367" +
		"\370\u0109\211\237\247\251\276\277\303\320\327\334\343\347\207\0\40\60\65\76\101" +
		"\104\126\152\167\173\211\214\215\236\237\247\251\262\271\276\277\316\320\323\325" +
		"\327\334\343\347\1\5\26\44\56\77\202\206\211\213\233\234\237\240\247\251\262\276" +
		"\277\306\313\320\323\324\325\327\334\336\343\347\360\367\370\u0109\103\166\222\117" +
		"\137\117\137\147\160\116\151\76\76\104\76\104\76\104\163\226\301\76\104\125\177\101" +
		"\126\152\152\167\152\167\166\222\164\152\167\202\306\206\367\211\237\276\277\347" +
		"\211\237\276\277\343\347\211\237\276\277\343\347\211\237\247\251\276\277\320\327" +
		"\343\347\211\237\247\276\277\343\347\211\237\247\251\276\277\320\327\334\343\347" +
		"\211\237\247\251\276\277\320\327\334\343\347\211\237\247\251\262\276\277\320\327" +
		"\334\343\347\211\237\247\251\262\276\277\320\323\325\327\334\343\347\211\237\247" +
		"\251\262\276\277\320\323\325\327\334\343\347\211\237\247\251\276\277\320\327\334" +
		"\343\347\211\237\247\251\262\276\277\320\323\325\327\334\343\347\211\237\247\251" +
		"\262\276\277\320\323\324\325\327\334\343\347\211\237\247\251\276\277\320\327\334" +
		"\343\347\152\167\236\152\167\175\211\236\237\247\251\263\270\276\277\320\327\334" +
		"\342\343\347\211\237\247\251\276\277\320\327\334\343\347\1\5\26\44\56\77\234\360" +
		"\1\5\26\44\56\77\234\336\360\30\34\53\12\12\165\211\237\247\251\276\277\303\320\327" +
		"\334\343\347\1\5\26\44\56\65\77\104\165\167\211\234\237\251\276\277\320\327\343\347" +
		"\360\40\55\60\103\163\201\226\303\101\214\215\271\202\211\237\276\277\347\211\237" +
		"\247\251\276\277\320\327\343\347\152\167\211\236\237\247\251\263\276\277\320\327" +
		"\334\343\347\234\240\30\5\5");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1850,
		"\u0111\u0112\130\130\3\3\3\3\3\66\75\3\132\3\3\3\4\4\4\4\57\4\4\225\4\4\4\164\164" +
		"\235\235\235\235\235\235\235\235\235\235\211\276\277\347\70\122\343\370\41\41\77" +
		"\116\323\42\42\125\177\55\74\100\305\307\326\u0103\u0104\u010b\31\133\133\133\133" +
		"\31\26\45\126\134\134\134\134\306\360\43\43\151\324\324\5\5\5\5\5\101\5\101\135\135" +
		"\135\135\236\5\236\236\236\236\236\5\27\127\136\136\136\136\363\364\u0108\25\30\54" +
		"\117\137\137\137\137\117\237\117\301\237\237\237\237\237\237\237\237\237\237\237" +
		"\237\237\237\240\240\240\240\240\240\240\240\240\240\240\40\46\62\156\157\161\176" +
		"\366\371\u0106\u010c\361\140\140\140\140\141\141\141\141\142\142\142\142\337\337" +
		"\u010d\340\340\u010e\325\143\143\143\143\335\144\144\144\144\334\334\334\145\145" +
		"\145\165\145\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165\165" +
		"\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\7\16\7\22\32\7\34\37\7\53\7\67\71\102" +
		"\7\112\102\146\153\112\146\146\112\146\22\112\112\153\7\7\241\7\112\112\7\7\112\241" +
		"\7\241\241\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\10\10" +
		"\10\10\10\10\10\10\10\11\11\11\11\11\11\11\11\11\12\12\12\12\12\12\12\12\367\336" +
		"\63\7\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112\146\22" +
		"\212\112\112\153\7\7\241\7\112\112\212\7\7\112\241\7\241\241\241\112\241\241\7\7" +
		"\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\102" +
		"\7\112\102\146\153\112\146\146\112\146\22\112\112\153\7\7\241\7\112\112\7\7\313\112" +
		"\241\7\241\241\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7" +
		"\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112\146\22\112" +
		"\112\153\7\7\241\7\112\112\7\7\314\112\241\7\241\241\241\112\241\241\7\7\112\241" +
		"\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112" +
		"\102\146\153\112\146\146\112\146\22\213\112\112\153\7\7\241\7\112\112\213\7\7\112" +
		"\241\7\241\241\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7" +
		"\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112\146\202\22" +
		"\112\112\153\7\7\241\7\112\112\7\7\112\241\7\241\241\241\112\241\241\7\7\112\241" +
		"\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112" +
		"\102\146\153\112\146\146\112\146\203\22\112\112\153\7\7\241\7\112\112\7\7\112\241" +
		"\7\241\241\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7" +
		"\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112\146\204\22\112" +
		"\112\153\7\7\241\7\112\112\7\7\112\241\7\241\241\241\112\241\241\7\7\112\241\241" +
		"\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112\102" +
		"\146\153\112\146\146\112\146\205\22\112\112\153\7\7\241\7\112\112\7\7\112\241\7\241" +
		"\241\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32" +
		"\7\34\37\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112\146\22\112\112\153\7" +
		"\7\241\7\112\112\304\7\7\112\241\7\241\241\241\112\241\241\7\7\112\241\241\7\241" +
		"\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\153" +
		"\112\146\146\112\146\22\112\112\153\7\7\241\7\112\112\7\7\112\241\7\241\241\241\112" +
		"\241\241\350\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37" +
		"\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112\146\22\214\112\112\153\7\7\241" +
		"\271\7\112\112\214\7\7\112\241\7\241\241\241\112\241\241\351\7\7\112\241\241\7\241" +
		"\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\153" +
		"\112\146\146\112\146\22\215\112\112\153\7\7\241\7\112\112\215\7\7\112\241\7\241\241" +
		"\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34" +
		"\37\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112\146\22\112\112\153\7\7\241" +
		"\7\112\112\7\7\112\241\7\241\241\241\112\241\241\352\7\7\112\241\241\7\241\241\241" +
		"\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\153\112\146" +
		"\146\112\146\22\112\112\153\7\7\241\7\112\112\7\7\112\241\7\241\241\241\112\241\241" +
		"\353\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\2\7\16\7\22\32\7\34\37\7\53" +
		"\7\67\71\102\7\112\102\146\153\112\146\146\112\146\22\112\112\153\7\7\241\7\112\112" +
		"\7\7\112\241\7\241\241\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7" +
		"\7\7\7\7\16\7\22\32\7\34\37\47\7\53\7\67\71\102\7\112\102\146\153\112\146\146\112" +
		"\146\22\112\112\153\7\7\241\7\112\112\7\7\112\241\7\241\241\241\112\241\241\7\7\112" +
		"\241\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7\22\32\7\34\37\7\53\7\67\71\76\102" +
		"\7\112\102\146\153\112\146\146\112\146\22\112\112\153\7\7\241\7\112\112\7\7\112\241" +
		"\7\241\241\241\112\241\241\7\7\112\241\241\7\241\241\241\7\241\241\7\7\7\7\7\16\7" +
		"\22\32\7\34\37\7\53\7\67\71\102\7\112\102\146\152\153\112\146\146\112\146\22\112" +
		"\112\153\7\7\241\7\112\112\7\7\112\241\7\241\241\241\112\241\241\7\7\112\241\241" +
		"\7\241\241\241\7\241\241\7\7\7\7\242\242\242\242\242\242\242\242\242\242\242\242" +
		"\234\u010f\50\64\72\103\113\103\113\166\166\222\243\273\273\315\243\243\243\243\273" +
		"\243\243\365\243\372\372\243\243\243\243\13\13\13\13\13\13\227\232\244\272\310\13" +
		"\244\321\244\244\244\244\244\227\362\244\244\244\244\244\244\u0100\244\244\13\232" +
		"\u010a\310\120\216\216\147\160\150\150\162\162\131\163\104\105\123\106\106\107\107" +
		"\200\302\354\110\110\154\224\114\155\167\170\220\171\171\217\300\206\172\172\230" +
		"\357\233\u0109\245\317\345\346\u0105\246\246\246\246\u0102\246\247\247\247\247\247" +
		"\247\250\250\250\250\250\250\250\250\250\250\251\320\327\251\251\251\251\252\252" +
		"\252\331\252\252\331\331\377\252\252\253\253\253\253\253\253\253\253\253\253\253" +
		"\254\254\254\254\341\254\254\254\254\254\254\254\255\255\255\255\255\255\255\255" +
		"\373\375\255\255\255\255\256\256\256\256\256\256\256\256\256\256\256\256\256\256" +
		"\257\257\257\257\257\257\257\257\257\257\257\260\260\260\260\260\260\260\260\260" +
		"\260\260\260\260\260\261\261\261\261\261\261\261\261\261\374\261\261\261\261\261" +
		"\262\262\262\262\262\262\262\262\262\262\262\173\173\316\174\174\223\174\174\174" +
		"\174\174\174\223\174\174\174\174\174\223\174\174\263\263\263\263\263\263\263\263" +
		"\263\263\263\u0110\17\33\52\61\111\311\u0107\14\14\14\14\14\14\14\u0101\14\35\44" +
		"\56\23\24\207\264\264\264\264\264\264\355\264\264\264\264\264\15\15\15\15\15\73\15" +
		"\124\210\221\265\15\265\332\265\265\332\332\265\265\15\51\60\65\121\201\226\303\356" +
		"\115\274\275\344\231\266\266\266\266\266\267\267\330\333\267\267\333\376\267\267" +
		"\175\175\270\175\270\270\270\342\270\270\270\270\270\270\270\312\322\36\20\21");

	private static final short[] lapg_rlen = TMLexer.unpack_short(172,
		"\1\0\2\0\2\0\17\14\4\4\3\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\3\2\1\1\2\2\1\1\1" +
		"\3\1\0\1\0\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\6\5\5\4\2\1\0\3\2\2" +
		"\1\1\1\1\4\3\1\4\2\1\1\2\1\3\3\1\1\1\0\3\2\2\1\1\3\4\3\3\2\1\2\2\1\1\1\1\2\1\3\3" +
		"\1\2\1\3\3\3\1\3\1\3\6\6\2\2\2\1\1\2\1\1\3\1\5\2\2\3\1\3\1\1\1\0\5\3\1\1\0\3\1\1" +
		"\1\1\1\3\5\1\1\1\1\1\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(172,
		"\161\161\162\162\163\163\73\73\74\75\75\76\76\77\100\101\101\102\102\103\103\103" +
		"\103\103\103\103\103\103\103\103\103\103\104\105\105\105\106\106\106\107\164\164" +
		"\165\165\166\166\167\167\170\170\110\110\111\112\113\113\113\113\171\171\114\115" +
		"\116\116\117\117\117\120\120\121\121\121\121\122\172\172\122\122\122\122\123\123" +
		"\123\124\173\173\124\125\125\126\126\127\127\174\174\130\175\175\131\131\131\131" +
		"\131\132\132\132\133\133\134\134\134\135\135\135\136\136\137\137\137\140\140\141" +
		"\141\141\142\143\143\144\144\144\144\144\144\176\176\145\145\145\146\177\177\147" +
		"\147\147\200\200\150\151\151\201\201\151\202\202\203\203\151\151\152\152\152\152" +
		"\153\153\154\154\154\155\156\156\157\160");

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
		"parsing_algorithmopt",
		"import__optlist",
		"option_optlist",
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
		"expression_list",
		"symref_list",
		"map_entriesopt",
		"expression_list1",
		"expression_list1_opt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 59;
		public static final int parsing_algorithm = 60;
		public static final int import_ = 61;
		public static final int option = 62;
		public static final int identifier = 63;
		public static final int symref = 64;
		public static final int type = 65;
		public static final int type_part_list = 66;
		public static final int type_part = 67;
		public static final int pattern = 68;
		public static final int lexer_parts = 69;
		public static final int lexer_part = 70;
		public static final int named_pattern = 71;
		public static final int lexeme = 72;
		public static final int lexem_transition = 73;
		public static final int lexem_attrs = 74;
		public static final int lexem_attribute = 75;
		public static final int state_selector = 76;
		public static final int stateref = 77;
		public static final int lexer_state = 78;
		public static final int grammar_parts = 79;
		public static final int grammar_part = 80;
		public static final int nonterm = 81;
		public static final int nonterm_type = 82;
		public static final int priority_kw = 83;
		public static final int directive = 84;
		public static final int inputref = 85;
		public static final int references = 86;
		public static final int references_cs = 87;
		public static final int rules = 88;
		public static final int rule0 = 89;
		public static final int rhsPrefix = 90;
		public static final int rhsSuffix = 91;
		public static final int rhsParts = 92;
		public static final int rhsPart = 93;
		public static final int rhsAnnotated = 94;
		public static final int rhsAssignment = 95;
		public static final int rhsOptional = 96;
		public static final int rhsCast = 97;
		public static final int rhsUnordered = 98;
		public static final int rhsClass = 99;
		public static final int rhsPrimary = 100;
		public static final int rhsAnnotations = 101;
		public static final int annotations = 102;
		public static final int annotation = 103;
		public static final int negative_la = 104;
		public static final int expression = 105;
		public static final int literal = 106;
		public static final int map_entries = 107;
		public static final int map_separator = 108;
		public static final int name = 109;
		public static final int qualified_id = 110;
		public static final int command = 111;
		public static final int syntax_problem = 112;
		public static final int parsing_algorithmopt = 113;
		public static final int import__optlist = 114;
		public static final int option_optlist = 115;
		public static final int typeopt = 116;
		public static final int lexem_transitionopt = 117;
		public static final int iconopt = 118;
		public static final int lexem_attrsopt = 119;
		public static final int commandopt = 120;
		public static final int lexer_state_list = 121;
		public static final int identifieropt = 122;
		public static final int inputref_list = 123;
		public static final int rule0_list = 124;
		public static final int rhsSuffixopt = 125;
		public static final int annotation_list = 126;
		public static final int expression_list = 127;
		public static final int symref_list = 128;
		public static final int map_entriesopt = 129;
		public static final int expression_list1 = 130;
		public static final int expression_list1_opt = 131;
	}

	public interface Rules {
		public static final int directive_input = 86;  // directive ::= '%' Linput inputref_list ';'
		public static final int rhsPrimary_symbol = 127;  // rhsPrimary ::= symref
		public static final int rhsPrimary_group = 128;  // rhsPrimary ::= '(' rules ')'
		public static final int rhsPrimary_list = 129;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		public static final int rhsPrimary_list2 = 130;  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		public static final int rhsPrimary_list3 = 131;  // rhsPrimary ::= rhsPrimary '*'
		public static final int rhsPrimary_list4 = 132;  // rhsPrimary ::= rhsPrimary '+'
		public static final int expression_instance = 151;  // expression ::= Lnew name '(' map_entriesopt ')'
		public static final int expression_array = 156;  // expression ::= '[' expression_list1_opt ']'
		public static final int literal_literal = 158;  // literal ::= scon
		public static final int literal_literal2 = 159;  // literal ::= icon
		public static final int literal_literal3 = 160;  // literal ::= Ltrue
		public static final int literal_literal4 = 161;  // literal ::= Lfalse
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
			case 2:  // import__optlist ::= import__optlist import_
				((List<TmaImport>)lapg_gg.value).add(((TmaImport)tmStack[tmHead].value));
				break;
			case 3:  // import__optlist ::=
				lapg_gg.value = new ArrayList();
				break;
			case 4:  // option_optlist ::= option_optlist option
				((List<TmaOption>)lapg_gg.value).add(((TmaOption)tmStack[tmHead].value));
				break;
			case 5:  // option_optlist ::=
				lapg_gg.value = new ArrayList();
				break;
			case 6:  // input ::= Llanguage ID '(' ID ')' parsing_algorithmopt ';' import__optlist option_optlist '::' Llexer lexer_parts '::' Lparser grammar_parts
				lapg_gg.value = new TmaInput(
						((String)tmStack[tmHead - 13].value) /* name */,
						((String)tmStack[tmHead - 11].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 9].value) /* parsingAlgorithm */,
						((List<TmaImport>)tmStack[tmHead - 7].value) /* importOptlist */,
						((List<TmaOption>)tmStack[tmHead - 6].value) /* optionOptlist */,
						((List<TmaLexerPartsItem>)tmStack[tmHead - 3].value) /* lexerParts */,
						((List<TmaGrammarPartsItem>)tmStack[tmHead].value) /* grammarParts */,
						null /* input */, tmStack[tmHead - 14].offset, tmStack[tmHead].endoffset);
				break;
			case 7:  // input ::= Llanguage ID '(' ID ')' parsing_algorithmopt ';' import__optlist option_optlist '::' Llexer lexer_parts
				lapg_gg.value = new TmaInput(
						((String)tmStack[tmHead - 10].value) /* name */,
						((String)tmStack[tmHead - 8].value) /* target */,
						((TmaParsingAlgorithm)tmStack[tmHead - 6].value) /* parsingAlgorithm */,
						((List<TmaImport>)tmStack[tmHead - 4].value) /* importOptlist */,
						((List<TmaOption>)tmStack[tmHead - 3].value) /* optionOptlist */,
						((List<TmaLexerPartsItem>)tmStack[tmHead].value) /* lexerParts */,
						null /* grammarParts */,
						null /* input */, tmStack[tmHead - 11].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // parsing_algorithm ::= Llalr '(' icon ')'
				lapg_gg.value = new TmaParsingAlgorithm(
						((Integer)tmStack[tmHead - 1].value) /* la */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 9:  // import_ ::= Limport ID scon ';'
				lapg_gg.value = new TmaImport(
						((String)tmStack[tmHead - 2].value) /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 10:  // import_ ::= Limport scon ';'
				lapg_gg.value = new TmaImport(
						null /* alias */,
						((String)tmStack[tmHead - 1].value) /* file */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // option ::= ID '=' expression
				lapg_gg.value = new TmaOption(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 12:  // option ::= syntax_problem
				lapg_gg.value = new TmaOption(
						null /* ID */,
						null /* expression */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 13:  // identifier ::= ID
				lapg_gg.value = new TmaIdentifier(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 14:  // symref ::= ID
				lapg_gg.value = new TmaSymref(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 15:  // type ::= '(' scon ')'
				 lapg_gg.value = ((String)tmStack[tmHead - 1].value); 
				break;
			case 16:  // type ::= '(' type_part_list ')'
				 lapg_gg.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 32:  // pattern ::= regexp
				lapg_gg.value = new TmaPattern(
						((String)tmStack[tmHead].value) /* regexp */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 33:  // lexer_parts ::= lexer_part
				lapg_gg.value = new ArrayList();
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 34:  // lexer_parts ::= lexer_parts lexer_part
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						((ITmaLexerPart)tmStack[tmHead].value) /* lexerPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 35:  // lexer_parts ::= lexer_parts syntax_problem
				((List<TmaLexerPartsItem>)lapg_gg.value).add(new TmaLexerPartsItem(
						null /* lexerPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 39:  // named_pattern ::= ID '=' pattern
				lapg_gg.value = new TmaNamedPattern(
						((String)tmStack[tmHead - 2].value) /* name */,
						((TmaPattern)tmStack[tmHead].value) /* pattern */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				lapg_gg.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 7].value) /* name */,
						((Integer)tmStack[tmHead - 2].value) /* priority */,
						((String)tmStack[tmHead - 6].value) /* type */,
						((TmaPattern)tmStack[tmHead - 4].value) /* pattern */,
						((TmaStateref)tmStack[tmHead - 3].value) /* lexemTransition */,
						((TmaLexemAttrs)tmStack[tmHead - 1].value) /* lexemAttrs */,
						((TmaCommand)tmStack[tmHead].value) /* command */,
						null /* input */, tmStack[tmHead - 7].offset, tmStack[tmHead].endoffset);
				break;
			case 51:  // lexeme ::= identifier typeopt ':'
				lapg_gg.value = new TmaLexeme(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						null /* priority */,
						((String)tmStack[tmHead - 1].value) /* type */,
						null /* pattern */,
						null /* lexemTransition */,
						null /* lexemAttrs */,
						null /* command */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 52:  // lexem_transition ::= '=>' stateref
				lapg_gg.value = ((TmaStateref)tmStack[tmHead].value);
				break;
			case 53:  // lexem_attrs ::= '(' lexem_attribute ')'
				lapg_gg.value = new TmaLexemAttrs(
						((TmaLexemAttribute)tmStack[tmHead - 1].value) /* lexemAttribute */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 54:  // lexem_attribute ::= Lsoft
				lapg_gg.value = TmaLexemAttribute.LSOFT;
				break;
			case 55:  // lexem_attribute ::= Lclass
				lapg_gg.value = TmaLexemAttribute.LCLASS;
				break;
			case 56:  // lexem_attribute ::= Lspace
				lapg_gg.value = TmaLexemAttribute.LSPACE;
				break;
			case 57:  // lexem_attribute ::= Llayout
				lapg_gg.value = TmaLexemAttribute.LLAYOUT;
				break;
			case 58:  // lexer_state_list ::= lexer_state_list ',' lexer_state
				((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 59:  // lexer_state_list ::= lexer_state
				lapg_gg.value = new ArrayList();
				((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value));
				break;
			case 60:  // state_selector ::= '[' lexer_state_list ']'
				lapg_gg.value = new TmaStateSelector(
						((List<TmaLexerState>)tmStack[tmHead - 1].value) /* states */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 61:  // stateref ::= ID
				lapg_gg.value = new TmaStateref(
						((String)tmStack[tmHead].value) /* ID */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 62:  // lexer_state ::= identifier '=>' stateref
				lapg_gg.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* name */,
						((TmaStateref)tmStack[tmHead].value) /* defaultTransition */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 63:  // lexer_state ::= identifier
				lapg_gg.value = new TmaLexerState(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* defaultTransition */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 64:  // grammar_parts ::= grammar_part
				lapg_gg.value = new ArrayList();
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 65:  // grammar_parts ::= grammar_parts grammar_part
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						((ITmaGrammarPart)tmStack[tmHead].value) /* grammarPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 66:  // grammar_parts ::= grammar_parts syntax_problem
				((List<TmaGrammarPartsItem>)lapg_gg.value).add(new TmaGrammarPartsItem(
						null /* grammarPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 69:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermType)tmStack[tmHead - 3].value) /* type */,
						((TmaAnnotations)tmStack[tmHead - 5].value) /* annotations */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 70:  // nonterm ::= annotations identifier '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* type */,
						((TmaAnnotations)tmStack[tmHead - 4].value) /* annotations */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 71:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((TmaIdentifier)tmStack[tmHead - 4].value) /* name */,
						((TmaNontermType)tmStack[tmHead - 3].value) /* type */,
						null /* annotations */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 72:  // nonterm ::= identifier '::=' rules ';'
				lapg_gg.value = new TmaNonterm(
						((TmaIdentifier)tmStack[tmHead - 3].value) /* name */,
						null /* type */,
						null /* annotations */,
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 73:  // nonterm_type ::= Lreturns symref
				lapg_gg.value = new TmaNontermType(
						null /* name */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* type */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 76:  // nonterm_type ::= Linline Lclass identifieropt
				lapg_gg.value = new TmaNontermType(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* symref */,
						null /* type */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 77:  // nonterm_type ::= Lclass identifieropt
				lapg_gg.value = new TmaNontermType(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* symref */,
						null /* type */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 78:  // nonterm_type ::= Linterface identifieropt
				lapg_gg.value = new TmaNontermType(
						((TmaIdentifier)tmStack[tmHead].value) /* name */,
						null /* symref */,
						null /* type */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 79:  // nonterm_type ::= type
				lapg_gg.value = new TmaNontermType(
						null /* name */,
						null /* symref */,
						((String)tmStack[tmHead].value) /* type */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 80:  // priority_kw ::= Lleft
				lapg_gg.value = TmaPriorityKw.LLEFT;
				break;
			case 81:  // priority_kw ::= Lright
				lapg_gg.value = TmaPriorityKw.LRIGHT;
				break;
			case 82:  // priority_kw ::= Lnonassoc
				lapg_gg.value = TmaPriorityKw.LNONASSOC;
				break;
			case 83:  // directive ::= '%' priority_kw references ';'
				lapg_gg.value = new TmaDirectiveImpl(
						((TmaPriorityKw)tmStack[tmHead - 2].value) /* priorityKw */,
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* references */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 84:  // inputref_list ::= inputref_list ',' inputref
				((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 85:  // inputref_list ::= inputref
				lapg_gg.value = new ArrayList();
				((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value));
				break;
			case 86:  // directive ::= '%' Linput inputref_list ';'
				lapg_gg.value = new TmaDirectiveInput(
						((List<TmaInputref>)tmStack[tmHead - 1].value) /* inputrefList */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 87:  // inputref ::= symref Lnoeoi
				lapg_gg.value = new TmaInputref(
						true /* noeoi */,
						((TmaSymref)tmStack[tmHead - 1].value) /* symref */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 88:  // inputref ::= symref
				lapg_gg.value = new TmaInputref(
						false /* noeoi */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 89:  // references ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 90:  // references ::= references symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 91:  // references_cs ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 92:  // references_cs ::= references_cs ',' symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 93:  // rule0_list ::= rule0_list '|' rule0
				((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 94:  // rule0_list ::= rule0
				lapg_gg.value = new ArrayList();
				((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value));
				break;
			case 98:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 2].value) /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 99:  // rule0 ::= rhsPrefix rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						((TmaRhsPrefix)tmStack[tmHead - 1].value) /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 100:  // rule0 ::= rhsParts rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 1].value) /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 101:  // rule0 ::= rhsSuffixopt
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						((TmaRhsSuffix)tmStack[tmHead].value) /* rhsSuffix */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 102:  // rule0 ::= syntax_problem
				lapg_gg.value = new TmaRule0(
						null /* rhsPrefix */,
						null /* rhsParts */,
						null /* rhsSuffix */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 103:  // rhsPrefix ::= '[' annotations ']'
				lapg_gg.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 1].value) /* annotations */,
						null /* alias */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 104:  // rhsPrefix ::= '[' annotations identifier ']'
				lapg_gg.value = new TmaRhsPrefix(
						((TmaAnnotations)tmStack[tmHead - 2].value) /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* input */, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 105:  // rhsPrefix ::= '[' identifier ']'
				lapg_gg.value = new TmaRhsPrefix(
						null /* annotations */,
						((TmaIdentifier)tmStack[tmHead - 1].value) /* alias */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 106:  // rhsSuffix ::= '%' Lprio symref
				lapg_gg.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LPRIO /* kind */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 107:  // rhsSuffix ::= '%' Lshift
				lapg_gg.value = new TmaRhsSuffix(
						TmaRhsSuffix.TmaKindKind.LSHIFT /* kind */,
						null /* symref */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 108:  // rhsParts ::= rhsPart
				lapg_gg.value = new ArrayList();
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset));
				break;
			case 109:  // rhsParts ::= rhsParts rhsPart
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPart */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 110:  // rhsParts ::= rhsParts syntax_problem
				((List<TmaRhsPartsItem>)lapg_gg.value).add(new TmaRhsPartsItem(
						null /* rhsPart */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset));
				break;
			case 115:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				lapg_gg.value = new TmaRhsAnnotated(
						((TmaRhsAnnotations)tmStack[tmHead - 1].value) /* rhsAnnotations */,
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsAssignment */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 117:  // rhsAssignment ::= identifier '=' rhsOptional
				lapg_gg.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						false /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 118:  // rhsAssignment ::= identifier '+=' rhsOptional
				lapg_gg.value = new TmaRhsAssignment(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* id */,
						true /* addition */,
						((ITmaRhsPart)tmStack[tmHead].value) /* inner */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 120:  // rhsOptional ::= rhsCast '?'
				lapg_gg.value = new TmaRhsOptional(
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* rhsCast */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 122:  // rhsCast ::= rhsClass Las symref
				lapg_gg.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* rhsClass */,
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* literal */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 123:  // rhsCast ::= rhsClass Las literal
				lapg_gg.value = new TmaRhsCast(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* rhsClass */,
						null /* symref */,
						((TmaLiteral)tmStack[tmHead].value) /* literal */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 124:  // rhsUnordered ::= rhsPart '&' rhsPart
				lapg_gg.value = new TmaRhsUnordered(
						((ITmaRhsPart)tmStack[tmHead - 2].value) /* left */,
						((ITmaRhsPart)tmStack[tmHead].value) /* right */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 126:  // rhsClass ::= identifier ':' rhsPrimary
				lapg_gg.value = new TmaRhsClass(
						((TmaIdentifier)tmStack[tmHead - 2].value) /* identifier */,
						((ITmaRhsPart)tmStack[tmHead].value) /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 127:  // rhsPrimary ::= symref
				lapg_gg.value = new TmaRhsPrimarySymbol(
						((TmaSymref)tmStack[tmHead].value) /* symref */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 128:  // rhsPrimary ::= '(' rules ')'
				lapg_gg.value = new TmaRhsPrimaryGroup(
						((List<TmaRule0>)tmStack[tmHead - 1].value) /* rules */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 129:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.PLUS /* quantifier */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						null /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 130:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.MULT /* quantifier */,
						((List<TmaRhsPartsItem>)tmStack[tmHead - 4].value) /* rhsParts */,
						((List<TmaSymref>)tmStack[tmHead - 2].value) /* references */,
						null /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 131:  // rhsPrimary ::= rhsPrimary '*'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.MULT /* quantifier */,
						null /* rhsParts */,
						null /* references */,
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 132:  // rhsPrimary ::= rhsPrimary '+'
				lapg_gg.value = new TmaRhsPrimaryList(
						TmaRhsPrimaryList.TmaQuantifierKind.PLUS /* quantifier */,
						null /* rhsParts */,
						null /* references */,
						((ITmaRhsPart)tmStack[tmHead - 1].value) /* rhsPrimary */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 133:  // annotation_list ::= annotation_list annotation
				((List<TmaAnnotation>)lapg_gg.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 134:  // annotation_list ::= annotation
				lapg_gg.value = new ArrayList();
				((List<TmaAnnotation>)lapg_gg.value).add(((TmaAnnotation)tmStack[tmHead].value));
				break;
			case 135:  // rhsAnnotations ::= annotation_list
				lapg_gg.value = new TmaRhsAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotationList */,
						null /* negativeLa */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 136:  // rhsAnnotations ::= negative_la annotation_list
				lapg_gg.value = new TmaRhsAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotationList */,
						((TmaNegativeLa)tmStack[tmHead - 1].value) /* negativeLa */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 137:  // rhsAnnotations ::= negative_la
				lapg_gg.value = new TmaRhsAnnotations(
						null /* annotationList */,
						((TmaNegativeLa)tmStack[tmHead].value) /* negativeLa */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 138:  // annotations ::= annotation_list
				lapg_gg.value = new TmaAnnotations(
						((List<TmaAnnotation>)tmStack[tmHead].value) /* annotations */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 139:  // expression_list ::= expression_list ',' expression
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 140:  // expression_list ::= expression
				lapg_gg.value = new ArrayList();
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 141:  // annotation ::= '@' qualified_id '{' expression_list '}'
				lapg_gg.value = new TmaAnnotation(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* arguments */,
						((String)tmStack[tmHead - 3].value) /* qualifiedId */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 142:  // annotation ::= '@' qualified_id
				lapg_gg.value = new TmaAnnotation(
						null /* arguments */,
						((String)tmStack[tmHead].value) /* qualifiedId */,
						null /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 143:  // annotation ::= '@' syntax_problem
				lapg_gg.value = new TmaAnnotation(
						null /* arguments */,
						null /* qualifiedId */,
						((TmaSyntaxProblem)tmStack[tmHead].value) /* syntaxProblem */,
						null /* input */, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 144:  // symref_list ::= symref_list '|' symref
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 145:  // symref_list ::= symref
				lapg_gg.value = new ArrayList();
				((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value));
				break;
			case 146:  // negative_la ::= '(?!' symref_list ')'
				lapg_gg.value = new TmaNegativeLa(
						((List<TmaSymref>)tmStack[tmHead - 1].value) /* unwantedSymbols */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 151:  // expression ::= Lnew name '(' map_entriesopt ')'
				lapg_gg.value = new TmaExpressionInstance(
						((TmaName)tmStack[tmHead - 3].value) /* name */,
						((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value) /* mapEntries */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 152:  // expression_list1 ::= expression_list1 ',' expression
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 153:  // expression_list1 ::= expression
				lapg_gg.value = new ArrayList();
				((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value));
				break;
			case 156:  // expression ::= '[' expression_list1_opt ']'
				lapg_gg.value = new TmaExpressionArray(
						((List<ITmaExpression>)tmStack[tmHead - 1].value) /* expressionList1 */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 158:  // literal ::= scon
				lapg_gg.value = new TmaLiteral(
						((String)tmStack[tmHead].value) /* sval */,
						null /* ival */,
						false /* isTrue */,
						false /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 159:  // literal ::= icon
				lapg_gg.value = new TmaLiteral(
						null /* sval */,
						((Integer)tmStack[tmHead].value) /* ival */,
						false /* isTrue */,
						false /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 160:  // literal ::= Ltrue
				lapg_gg.value = new TmaLiteral(
						null /* sval */,
						null /* ival */,
						true /* isTrue */,
						false /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 161:  // literal ::= Lfalse
				lapg_gg.value = new TmaLiteral(
						null /* sval */,
						null /* ival */,
						false /* isTrue */,
						true /* isFalse */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 162:  // map_entries ::= ID map_separator expression
				lapg_gg.value = new ArrayList();
				((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 163:  // map_entries ::= map_entries ',' ID map_separator expression
				((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(
						((String)tmStack[tmHead - 2].value) /* ID */,
						((ITmaExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset));
				break;
			case 167:  // name ::= qualified_id
				lapg_gg.value = new TmaName(
						((String)tmStack[tmHead].value) /* qualifiedId */,
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 169:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 170:  // command ::= code
				lapg_gg.value = new TmaCommand(
						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 171:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 273);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 274);
	}
}
