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
	private static final int[] tmAction = TMLexer.unpack_int(282,
		"\uffff\uffff\uffff\uffff\uffff\uffff\236\0\237\0\ufffd\uffff\255\0\20\0\240\0\241" +
		"\0\uffff\uffff\226\0\225\0\235\0\uffff\uffff\242\0\uffc5\uffff\uffff\uffff\252\0" +
		"\uffff\uffff\uffbf\uffff\uffff\uffff\uffff\uffff\234\0\uffb9\uffff\uffff\uffff\uffff" +
		"\uffff\243\0\uffff\uffff\uff8f\uffff\uffff\uffff\253\0\uff89\uffff\247\0\250\0\246" +
		"\0\uffff\uffff\uffff\uffff\231\0\uffff\uffff\0\0\uffff\uffff\244\0\uffff\uffff\uffff" +
		"\uffff\uff83\uffff\uffff\uffff\uffff\uffff\uffff\uffff\245\0\10\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\2\0\uffff\uffff\13\0\16\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\14\0\uffff\uffff\uff55\uffff\uff4d\uffff\uff47\uffff\44" +
		"\0\50\0\51\0\47\0\15\0\12\0\uffff\uffff\uffff\uffff\17\0\uff17\uffff\uffff\uffff" +
		"\76\0\uffff\uffff\uffff\uffff\53\0\uffff\uffff\uffff\uffff\45\0\46\0\11\0\uff0f\uffff" +
		"\uffff\uffff\uffff\uffff\75\0\43\0\52\0\uffff\uffff\33\0\34\0\27\0\30\0\ufedf\uffff" +
		"\25\0\26\0\32\0\35\0\37\0\36\0\31\0\uffff\uffff\24\0\ufe9f\uffff\uffff\uffff\uffff" +
		"\uffff\100\0\101\0\77\0\21\0\ufe6d\uffff\uffff\uffff\22\0\23\0\ufe2d\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\ufdf5\uffff\103\0\106\0\107\0\uffff\uffff\ufdc5\uffff" +
		"\215\0\uffff\uffff\42\0\uffff\uffff\55\0\ufd9b\uffff\uffff\uffff\123\0\124\0\125" +
		"\0\uffff\uffff\ufd65\uffff\221\0\ufd35\uffff\uffff\uffff\uffff\uffff\ufcfb\uffff" +
		"\ufcd1\uffff\122\0\uffff\uffff\104\0\105\0\uffff\uffff\216\0\ufca7\uffff\67\0\57" +
		"\0\ufc77\uffff\ufc43\uffff\uffff\uffff\130\0\135\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\ufc3b\uffff\uffff\uffff\ufc01\uffff\254\0\uffff\uffff\203\0\uffff" +
		"\uffff\ufbb5\uffff\142\0\ufbad\uffff\144\0\ufb75\uffff\ufb3b\uffff\163\0\166\0\170" +
		"\0\ufafd\uffff\164\0\ufabd\uffff\ufa7b\uffff\uffff\uffff\ufa35\uffff\ufa09\uffff" +
		"\165\0\152\0\151\0\uf9dd\uffff\114\0\115\0\120\0\121\0\uf9b3\uffff\uf979\uffff\uffff" +
		"\uffff\uffff\uffff\61\0\uf93f\uffff\132\0\134\0\127\0\uffff\uffff\126\0\136\0\uffff" +
		"\uffff\uffff\uffff\157\0\uffff\uffff\uffff\uffff\uffff\uffff\uf90d\uffff\uffff\uffff" +
		"\uf8e1\uffff\223\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\113\0\uf8a7\uffff" +
		"\uf86b\uffff\147\0\uf831\uffff\162\0\150\0\uffff\uffff\174\0\uffff\uffff\207\0\210" +
		"\0\167\0\uf7f3\uffff\117\0\uffff\uffff\uffff\uffff\uf7c7\uffff\71\0\72\0\73\0\74" +
		"\0\uffff\uffff\63\0\65\0\131\0\217\0\156\0\155\0\uffff\uffff\153\0\204\0\uffff\uffff" +
		"\uffff\uffff\222\0\uffff\uffff\171\0\uf78d\uffff\172\0\143\0\146\0\uf747\uffff\176" +
		"\0\177\0\112\0\111\0\uffff\uffff\70\0\154\0\uffff\uffff\224\0\110\0\uffff\uffff\206" +
		"\0\205\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(2292,
		"\2\uffff\3\uffff\20\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\40\uffff\41\uffff\42\uffff\21\233\uffff" +
		"\ufffe\16\uffff\21\232\uffff\ufffe\15\uffff\22\251\uffff\ufffe\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\24\230\uffff" +
		"\ufffe\16\uffff\24\227\uffff\ufffe\65\uffff\14\1\uffff\ufffe\10\3\36\3\37\3\45\3" +
		"\46\3\47\3\50\3\51\3\52\3\53\3\54\3\55\3\56\3\57\3\60\3\61\3\62\3\63\3\64\3\65\3" +
		"\66\3\67\3\uffff\ufffe\12\uffff\17\17\22\17\uffff\ufffe\22\uffff\17\54\uffff\ufffe" +
		"\10\uffff\20\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\0\7\uffff\ufffe\13\uffff\16\102\21\102\uffff" +
		"\ufffe\10\uffff\20\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63" +
		"\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\0\5\uffff\ufffe\15\uffff\16\uffff\20\uffff" +
		"\21\uffff\22\uffff\26\uffff\27\uffff\30\uffff\33\uffff\34\uffff\35\uffff\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\24\41\uffff\ufffe\1\uffff\0\66\10\66\20\66\36\66\37\66\46\66\47\66\50\66\51\66\52" +
		"\66\53\66\54\66\55\66\56\66\57\66\60\66\61\66\62\66\63\66\64\66\65\66\66\66\67\66" +
		"\uffff\ufffe\15\uffff\16\uffff\20\uffff\21\uffff\22\uffff\26\uffff\27\uffff\30\uffff" +
		"\33\uffff\34\uffff\35\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\24\40\uffff\ufffe\13\uffff\0\56\3\56\10\56\20" +
		"\56\22\56\36\56\37\56\46\56\47\56\50\56\51\56\52\56\53\56\54\56\55\56\56\56\57\56" +
		"\60\56\61\56\62\56\63\56\64\56\65\56\66\56\67\56\71\56\uffff\ufffe\6\uffff\35\uffff" +
		"\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff" +
		"\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\0\6\uffff\ufffe\35\uffff\37\214\46\214\47\214\50\214\51\214\52" +
		"\214\53\214\54\214\55\214\56\214\57\214\60\214\61\214\62\214\63\214\64\214\65\214" +
		"\66\214\67\214\uffff\ufffe\3\uffff\0\60\10\60\20\60\22\60\36\60\37\60\46\60\47\60" +
		"\50\60\51\60\52\60\53\60\54\60\55\60\56\60\57\60\60\60\61\60\62\60\63\60\64\60\65" +
		"\60\66\60\67\60\71\60\uffff\ufffe\72\uffff\21\220\22\220\35\220\37\220\46\220\47" +
		"\220\50\220\51\220\52\220\53\220\54\220\55\220\56\220\57\220\60\220\61\220\62\220" +
		"\63\220\64\220\65\220\66\220\67\220\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff" +
		"\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\71\uffff\11\145\14\145\uffff\ufffe\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\7\116\uffff" +
		"\ufffe\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\7\116\uffff\ufffe\6\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66" +
		"\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\0\4\uffff\ufffe" +
		"\22\uffff\0\62\10\62\20\62\36\62\37\62\46\62\47\62\50\62\51\62\52\62\53\62\54\62" +
		"\55\62\56\62\57\62\60\62\61\62\62\62\63\62\64\62\65\62\66\62\67\62\71\62\uffff\ufffe" +
		"\56\uffff\14\133\16\133\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36" +
		"\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\71\uffff\11\145\24\145\uffff\ufffe\12\17\17\17\32\17\6\20\11\20\14" +
		"\20\22\20\23\20\24\20\30\20\31\20\33\20\34\20\35\20\36\20\37\20\43\20\44\20\46\20" +
		"\47\20\50\20\51\20\52\20\53\20\54\20\55\20\56\20\57\20\60\20\61\20\62\20\63\20\64" +
		"\20\65\20\66\20\67\20\71\20\uffff\ufffe\11\uffff\14\141\24\141\uffff\ufffe\6\uffff" +
		"\22\uffff\23\uffff\35\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\145\14\145\24\145\uffff\ufffe\6" +
		"\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64" +
		"\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53" +
		"\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\145\14\145\24\145" +
		"\uffff\ufffe\34\uffff\6\160\11\160\14\160\22\160\23\160\24\160\35\160\36\160\37\160" +
		"\43\160\46\160\47\160\50\160\51\160\52\160\53\160\54\160\55\160\56\160\57\160\60" +
		"\160\61\160\62\160\63\160\64\160\65\160\66\160\67\160\71\160\uffff\ufffe\33\uffff" +
		"\6\173\11\173\14\173\22\173\23\173\24\173\34\173\35\173\36\173\37\173\43\173\46\173" +
		"\47\173\50\173\51\173\52\173\53\173\54\173\55\173\56\173\57\173\60\173\61\173\62" +
		"\173\63\173\64\173\65\173\66\173\67\173\71\173\uffff\ufffe\44\uffff\6\175\11\175" +
		"\14\175\22\175\23\175\24\175\33\175\34\175\35\175\36\175\37\175\43\175\46\175\47" +
		"\175\50\175\51\175\52\175\53\175\54\175\55\175\56\175\57\175\60\175\61\175\62\175" +
		"\63\175\64\175\65\175\66\175\67\175\71\175\uffff\ufffe\30\uffff\31\uffff\6\201\11" +
		"\201\14\201\22\201\23\201\24\201\33\201\34\201\35\201\36\201\37\201\43\201\44\201" +
		"\46\201\47\201\50\201\51\201\52\201\53\201\54\201\55\201\56\201\57\201\60\201\61" +
		"\201\62\201\63\201\64\201\65\201\66\201\67\201\71\201\uffff\ufffe\35\uffff\22\211" +
		"\37\211\46\211\47\211\50\211\51\211\52\211\53\211\54\211\55\211\56\211\57\211\60" +
		"\211\61\211\62\211\63\211\64\211\65\211\66\211\67\211\uffff\ufffe\35\uffff\22\213" +
		"\37\213\46\213\47\213\50\213\51\213\52\213\53\213\54\213\55\213\56\213\57\213\60" +
		"\213\61\213\62\213\63\213\64\213\65\213\66\213\67\213\uffff\ufffe\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\7\116\uffff" +
		"\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66" +
		"\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55" +
		"\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11" +
		"\145\14\145\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\71\uffff\11\145\14\145\uffff\ufffe\71\uffff\0\64\10\64\20\64\36\64\37\64\46\64\47" +
		"\64\50\64\51\64\52\64\53\64\54\64\55\64\56\64\57\64\60\64\61\64\62\64\63\64\64\64" +
		"\65\64\66\64\67\64\uffff\ufffe\35\uffff\37\211\46\211\47\211\50\211\51\211\52\211" +
		"\53\211\54\211\55\211\56\211\57\211\60\211\61\211\62\211\63\211\64\211\65\211\66" +
		"\211\67\211\21\214\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\43\uffff\71\uffff\11\145\24\145\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35" +
		"\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61" +
		"\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\71\uffff\11\145\14\145\24\145\uffff\ufffe\6\uffff\22\uffff" +
		"\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\145\14\145\24\145\uffff\ufffe\34" +
		"\uffff\6\161\11\161\14\161\22\161\23\161\24\161\35\161\36\161\37\161\43\161\46\161" +
		"\47\161\50\161\51\161\52\161\53\161\54\161\55\161\56\161\57\161\60\161\61\161\62" +
		"\161\63\161\64\161\65\161\66\161\67\161\71\161\uffff\ufffe\35\uffff\22\212\37\212" +
		"\46\212\47\212\50\212\51\212\52\212\53\212\54\212\55\212\56\212\57\212\60\212\61" +
		"\212\62\212\63\212\64\212\65\212\66\212\67\212\uffff\ufffe\6\uffff\20\uffff\22\uffff" +
		"\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\145\14\145\uffff\ufffe\30\uffff" +
		"\31\uffff\6\202\11\202\14\202\22\202\23\202\24\202\33\202\34\202\35\202\36\202\37" +
		"\202\43\202\44\202\46\202\47\202\50\202\51\202\52\202\53\202\54\202\55\202\56\202" +
		"\57\202\60\202\61\202\62\202\63\202\64\202\65\202\66\202\67\202\71\202\uffff\ufffe" +
		"\34\200\6\200\11\200\14\200\22\200\23\200\24\200\35\200\36\200\37\200\43\200\46\200" +
		"\47\200\50\200\51\200\52\200\53\200\54\200\55\200\56\200\57\200\60\200\61\200\62" +
		"\200\63\200\64\200\65\200\66\200\67\200\71\200\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(134,
		"\0\2\4\17\31\31\31\47\53\57\61\66\72\103\110\120\125\152\163\215\231\243\244\250" +
		"\254\263\266\267\274\303\334\363\u0134\u013c\u0144\u014b\u014c\u014d\u014e\u0191" +
		"\u01d3\u0215\u0258\u029a\u02dc\u031e\u0360\u03a2\u03e4\u0429\u046c\u04ae\u04f0\u0532" +
		"\u0574\u05b7\u05fa\u05fa\u0606\u0607\u0608\u0609\u060a\u060b\u060d\u062b\u064c\u064f" +
		"\u0651\u0655\u0657\u0659\u065d\u0661\u0665\u0666\u0667\u0668\u066c\u066d\u066f\u0671" +
		"\u0673\u0677\u067b\u067d\u067e\u0682\u0683\u0685\u0687\u0687\u068c\u0691\u0697\u069d" +
		"\u06a7\u06ae\u06b9\u06c4\u06d0\u06de\u06ec\u06f7\u0705\u0714\u0720\u0725\u0736\u074b" +
		"\u0757\u0758\u075f\u0767\u0768\u0769\u076b\u076c\u076d\u0779\u0790\u0791\u0792\u0793" +
		"\u0794\u0795\u0796\u0797\u0798\u079b\u079c\u07a6\u07a7\u07a8");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1960,
		"\u0116\u0117\120\155\1\5\26\44\56\65\73\75\121\243\350\1\5\26\44\54\56\73\207\243" +
		"\350\156\174\203\217\232\246\257\261\305\306\331\340\341\360\173\225\230\307\60\67" +
		"\103\130\255\333\34\53\64\101\252\34\53\115\170\51\74\112\237\242\254\356\357\u010d" +
		"\24\121\143\153\164\20\35\116\121\143\153\164\237\34\53\123\252\u0102\1\5\26\44\56" +
		"\72\73\103\113\121\130\143\153\164\217\243\246\305\306\340\360\21\116\121\143\153" +
		"\164\324\326\374\16\23\47\102\121\143\153\164\173\217\230\235\246\257\261\272\305" +
		"\306\331\334\335\336\340\341\346\360\217\245\246\257\261\305\306\331\340\341\346" +
		"\360\32\36\57\136\153\165\330\333\365\u0110\321\121\143\153\164\121\143\153\164\121" +
		"\143\153\164\271\u0104\u0113\271\u0104\u0113\252\121\143\153\164\266\121\143\153" +
		"\164\262\343\u0108\121\143\153\156\164\174\201\203\217\232\245\246\257\261\273\274" +
		"\305\306\327\331\340\341\346\354\360\1\5\26\44\56\60\67\73\103\130\172\174\217\232" +
		"\243\246\261\305\306\331\340\341\360\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73" +
		"\100\103\113\121\130\131\132\143\153\156\164\172\174\200\203\205\210\214\217\221" +
		"\222\223\232\242\243\245\246\247\257\261\272\300\305\306\316\322\325\331\334\335" +
		"\336\340\341\346\350\360\377\u0100\u0110\1\5\26\44\56\73\243\350\1\5\26\44\56\73" +
		"\243\350\1\5\26\44\56\73\243\331\270\60\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72" +
		"\73\100\103\113\121\130\131\132\143\153\156\164\172\173\174\200\203\205\210\214\217" +
		"\221\222\223\230\232\242\243\245\246\247\257\261\272\300\305\306\316\322\325\331" +
		"\334\335\336\340\341\346\350\360\377\u0100\u0110\1\2\5\12\25\26\30\31\44\45\56\60" +
		"\65\67\72\73\100\103\113\121\130\131\132\143\153\156\164\172\174\200\203\205\210" +
		"\214\217\221\222\223\232\242\243\244\245\246\247\257\261\272\300\305\306\316\322" +
		"\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110\1\2\5\12\25\26\30\31\44" +
		"\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153\156\164\172\174\200\203" +
		"\205\210\214\217\221\222\223\232\242\243\244\245\246\247\257\261\272\300\305\306" +
		"\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110\1\2\5\12\25\26\30" +
		"\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153\156\164\172\173\174" +
		"\200\203\205\210\214\217\221\222\223\230\232\242\243\245\246\247\257\261\272\300" +
		"\305\306\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110\1\2\5\12" +
		"\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153\156\164" +
		"\171\172\174\200\203\205\210\214\217\221\222\223\232\242\243\245\246\247\257\261" +
		"\272\300\305\306\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110" +
		"\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153" +
		"\156\164\171\172\174\200\203\205\210\214\217\221\222\223\232\242\243\245\246\247" +
		"\257\261\272\300\305\306\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100" +
		"\u0110\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143" +
		"\153\156\164\171\172\174\200\203\205\210\214\217\221\222\223\232\242\243\245\246" +
		"\247\257\261\272\300\305\306\316\322\325\331\334\335\336\340\341\346\350\360\377" +
		"\u0100\u0110\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131" +
		"\132\143\153\156\164\171\172\174\200\203\205\210\214\217\221\222\223\232\242\243" +
		"\245\246\247\257\261\272\300\305\306\316\322\325\331\334\335\336\340\341\346\350" +
		"\360\377\u0100\u0110\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121" +
		"\130\131\132\143\153\156\164\172\174\200\203\205\210\214\217\221\222\223\232\236" +
		"\242\243\245\246\247\257\261\272\300\305\306\316\322\325\331\334\335\336\340\341" +
		"\346\350\360\377\u0100\u0110\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103" +
		"\113\121\130\131\132\143\153\156\164\172\174\200\203\205\210\214\217\221\222\223" +
		"\232\242\243\245\246\247\257\261\272\300\305\306\310\316\322\325\331\334\335\336" +
		"\340\341\346\350\360\377\u0100\u0110\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73" +
		"\100\103\113\121\130\131\132\143\153\156\164\172\173\174\200\203\205\210\214\217" +
		"\220\221\222\223\230\232\242\243\245\246\247\257\261\272\300\305\306\310\316\322" +
		"\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110\1\2\5\12\25\26\30\31\44" +
		"\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153\156\164\172\173\174\200" +
		"\203\205\210\214\217\221\222\223\230\232\242\243\245\246\247\257\261\272\300\305" +
		"\306\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110\1\2\5\12\25" +
		"\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153\156\164\172" +
		"\174\200\203\205\210\214\217\221\222\223\232\242\243\245\246\247\257\261\272\300" +
		"\305\306\310\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110\1\2" +
		"\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153\156" +
		"\164\172\174\200\203\205\210\214\217\221\222\223\232\242\243\245\246\247\257\261" +
		"\272\300\305\306\310\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110" +
		"\0\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143\153" +
		"\156\164\172\174\200\203\205\210\214\217\221\222\223\232\242\243\245\246\247\257" +
		"\261\272\300\305\306\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100\u0110" +
		"\1\2\5\12\25\26\30\31\40\44\45\56\60\65\67\72\73\100\103\113\121\130\131\132\143" +
		"\153\156\164\172\174\200\203\205\210\214\217\221\222\223\232\242\243\245\246\247" +
		"\257\261\272\300\305\306\316\322\325\331\334\335\336\340\341\346\350\360\377\u0100" +
		"\u0110\1\2\5\12\25\26\30\31\44\45\56\60\63\65\67\72\73\76\100\103\113\121\130\131" +
		"\132\143\153\156\164\172\174\200\203\205\210\214\217\221\222\223\232\242\243\245" +
		"\246\247\257\261\272\300\305\306\316\322\325\331\334\335\336\340\341\346\350\360" +
		"\377\u0100\u0110\1\2\5\12\25\26\30\31\44\45\56\60\65\67\72\73\100\103\113\121\124" +
		"\130\131\132\143\153\156\157\164\172\174\200\203\205\210\214\217\221\222\223\232" +
		"\242\243\245\246\247\257\261\272\300\305\306\316\322\325\331\334\335\336\340\341" +
		"\346\350\360\377\u0100\u0110\217\246\257\261\305\306\312\331\340\341\346\360\215" +
		"\0\40\60\60\60\67\72\100\103\113\130\132\156\174\200\203\217\222\223\232\245\246" +
		"\257\261\272\300\305\306\325\331\334\336\340\341\346\360\1\5\26\44\56\73\210\214" +
		"\217\221\242\243\246\247\257\261\272\305\306\316\322\331\334\335\336\340\341\346" +
		"\350\360\377\u0100\u0110\102\173\230\121\143\121\143\153\164\120\155\72\113\72\103" +
		"\113\130\72\103\113\130\72\103\113\130\170\235\310\72\103\113\130\100\131\205\100" +
		"\132\156\203\156\174\203\232\156\174\203\232\173\230\171\156\174\203\232\210\210" +
		"\316\214\377\217\246\305\306\360\217\246\305\306\360\217\246\305\306\340\360\217" +
		"\246\305\306\340\360\217\246\257\261\305\306\331\340\341\360\217\246\257\305\306" +
		"\340\360\217\246\257\261\305\306\331\340\341\346\360\217\246\257\261\305\306\331" +
		"\340\341\346\360\217\246\257\261\272\305\306\331\340\341\346\360\217\246\257\261" +
		"\272\305\306\331\334\336\340\341\346\360\217\246\257\261\272\305\306\331\334\336" +
		"\340\341\346\360\217\246\257\261\305\306\331\340\341\346\360\217\246\257\261\272" +
		"\305\306\331\334\336\340\341\346\360\217\246\257\261\272\305\306\331\334\335\336" +
		"\340\341\346\360\217\245\246\257\261\305\306\331\340\341\346\360\156\174\203\232" +
		"\245\156\174\203\217\232\245\246\257\261\274\305\306\331\340\341\346\360\156\174" +
		"\201\203\217\232\245\246\257\261\273\274\305\306\327\331\340\341\346\354\360\217" +
		"\245\246\257\261\305\306\331\340\341\346\360\247\1\5\26\44\56\73\243\1\5\26\44\56" +
		"\73\243\350\5\30\34\53\12\12\217\246\257\261\305\306\312\331\340\341\346\360\1\5" +
		"\26\44\56\60\67\73\103\130\172\174\217\232\243\246\261\305\306\331\340\341\360\40" +
		"\55\143\102\170\207\235\312\222\223\300\236\217\246\257\261\305\306\331\340\341\360" +
		"\30\5");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1960,
		"\u0118\u0119\134\134\3\3\3\3\3\74\3\112\136\3\3\4\4\4\4\57\4\4\234\4\4\171\171\171" +
		"\244\171\244\244\244\244\244\244\244\244\244\217\305\306\360\63\76\124\157\340\u0100" +
		"\41\41\73\120\334\42\42\131\205\55\111\127\315\317\337\u010b\u010c\u0112\31\137\137" +
		"\137\137\26\45\132\140\140\140\140\316\43\43\155\335\335\5\5\5\5\5\100\5\100\100" +
		"\141\100\141\141\141\245\5\245\245\245\245\245\27\133\142\142\142\142\373\375\u010f" +
		"\25\30\54\121\143\143\143\143\121\246\121\310\246\246\246\246\246\246\246\246\246" +
		"\246\246\246\246\246\247\247\247\247\247\247\247\247\247\247\247\247\40\46\62\163" +
		"\166\204\376\u0101\u010e\u0113\371\144\144\144\144\145\145\145\145\146\146\146\146" +
		"\351\351\u0114\352\352\u0115\336\147\147\147\147\347\150\150\150\150\346\346\346" +
		"\151\151\151\172\151\172\172\172\172\172\172\172\172\172\172\172\172\172\172\172" +
		"\172\172\172\172\172\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\6\7\16\7\22\32\7" +
		"\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152\215\114" +
		"\114\114\160\7\7\250\7\114\114\114\7\7\114\250\7\250\250\250\114\250\250\7\7\114" +
		"\250\250\7\250\250\250\250\7\250\7\7\7\10\10\10\10\10\10\10\10\11\11\11\11\11\11" +
		"\11\11\12\12\12\12\12\12\12\377\350\65\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7" +
		"\114\101\101\152\101\160\114\152\152\114\152\215\220\114\114\114\160\7\7\250\7\114" +
		"\114\220\114\7\7\114\250\7\250\250\250\114\250\250\7\7\114\250\250\7\250\250\250" +
		"\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101\160" +
		"\114\152\152\114\152\215\114\114\114\160\7\7\250\7\114\114\114\7\7\322\114\250\7" +
		"\250\250\250\114\250\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22" +
		"\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152\215" +
		"\114\114\114\160\7\7\250\7\114\114\114\7\7\323\114\250\7\250\250\250\114\250\250" +
		"\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75" +
		"\64\101\7\114\101\101\152\101\160\114\152\152\114\152\215\221\114\114\114\160\7\7" +
		"\250\7\114\114\221\114\7\7\114\250\7\250\250\250\114\250\250\7\7\114\250\250\7\250" +
		"\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152" +
		"\101\160\114\152\152\114\152\210\215\114\114\114\160\7\7\250\7\114\114\114\7\7\114" +
		"\250\7\250\250\250\114\250\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16" +
		"\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152" +
		"\211\215\114\114\114\160\7\7\250\7\114\114\114\7\7\114\250\7\250\250\250\114\250" +
		"\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64" +
		"\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152\212\215\114\114\114\160" +
		"\7\7\250\7\114\114\114\7\7\114\250\7\250\250\250\114\250\250\7\7\114\250\250\7\250" +
		"\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152" +
		"\101\160\114\152\152\114\152\213\215\114\114\114\160\7\7\250\7\114\114\114\7\7\114" +
		"\250\7\250\250\250\114\250\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16" +
		"\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152" +
		"\215\114\114\114\160\7\7\250\7\114\114\114\313\7\7\114\250\7\250\250\250\114\250" +
		"\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64" +
		"\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152\215\114\114\114\160\7\7" +
		"\250\7\114\114\114\7\7\114\250\7\250\250\250\114\250\250\361\7\7\114\250\250\7\250" +
		"\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152" +
		"\101\160\114\152\152\114\152\215\222\114\114\114\160\7\7\250\300\7\114\114\222\114" +
		"\7\7\114\250\7\250\250\250\114\250\250\362\7\7\114\250\250\7\250\250\250\250\7\250" +
		"\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101\160\114\152" +
		"\152\114\152\215\223\114\114\114\160\7\7\250\7\114\114\223\114\7\7\114\250\7\250" +
		"\250\250\114\250\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22\32\7" +
		"\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152\215\114" +
		"\114\114\160\7\7\250\7\114\114\114\7\7\114\250\7\250\250\250\114\250\250\363\7\7" +
		"\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101" +
		"\7\114\101\101\152\101\160\114\152\152\114\152\215\114\114\114\160\7\7\250\7\114" +
		"\114\114\7\7\114\250\7\250\250\250\114\250\250\364\7\7\114\250\250\7\250\250\250" +
		"\250\7\250\7\7\7\2\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152\101" +
		"\160\114\152\152\114\152\215\114\114\114\160\7\7\250\7\114\114\114\7\7\114\250\7" +
		"\250\250\250\114\250\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22" +
		"\32\7\34\37\47\7\53\7\64\75\64\101\7\114\101\101\152\101\160\114\152\152\114\152" +
		"\215\114\114\114\160\7\7\250\7\114\114\114\7\7\114\250\7\250\250\250\114\250\250" +
		"\7\7\114\250\250\7\250\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\72" +
		"\75\64\101\7\113\114\101\101\152\101\160\114\152\152\114\152\215\114\114\114\160" +
		"\7\7\250\7\114\114\114\7\7\114\250\7\250\250\250\114\250\250\7\7\114\250\250\7\250" +
		"\250\250\250\7\250\7\7\7\7\16\7\22\32\7\34\37\7\53\7\64\75\64\101\7\114\101\101\152" +
		"\156\101\160\114\152\152\114\203\152\215\114\114\114\160\7\7\250\7\114\114\114\7" +
		"\7\114\250\7\250\250\250\114\250\250\7\7\114\250\250\7\250\250\250\250\7\250\7\7" +
		"\7\251\251\251\251\251\251\251\251\251\251\251\251\243\u0116\50\66\67\70\77\102\115" +
		"\102\102\102\115\173\173\230\173\252\302\302\173\324\252\252\252\252\302\252\252" +
		"\374\252\u0102\u0102\252\252\252\252\13\13\13\13\13\13\236\241\253\301\320\13\253" +
		"\332\253\253\253\253\253\236\372\253\253\253\253\253\253\253\u0109\253\241\u0111" +
		"\320\122\224\224\153\164\154\154\167\167\135\170\103\130\104\125\104\125\105\105" +
		"\105\105\106\106\106\106\206\311\365\107\107\107\107\116\161\233\117\162\174\232" +
		"\175\226\175\226\176\176\176\176\225\307\214\177\177\177\177\237\240\370\242\u0110" +
		"\254\330\356\357\u010d\255\255\255\255\255\256\256\256\256\u0106\256\257\257\257" +
		"\257\257\257\260\260\260\260\260\260\260\260\260\260\261\331\341\261\261\261\261" +
		"\262\262\262\343\262\262\343\262\343\u0108\262\263\263\263\263\263\263\263\263\263" +
		"\263\263\264\264\264\264\353\264\264\264\264\264\264\264\265\265\265\265\265\265" +
		"\265\265\u0103\u0105\265\265\265\265\266\266\266\266\266\266\266\266\266\266\266" +
		"\266\266\266\267\267\267\267\267\267\267\267\267\267\267\270\270\270\270\270\270" +
		"\270\270\270\270\270\270\270\270\271\271\271\271\271\271\271\271\271\u0104\271\271" +
		"\271\271\271\272\325\272\272\272\272\272\272\272\272\272\272\200\200\200\200\326" +
		"\201\201\201\273\201\327\273\273\273\354\273\273\273\273\273\273\273\202\202\231" +
		"\202\202\202\202\202\202\202\231\202\202\202\231\202\202\202\202\231\202\274\274" +
		"\274\274\274\274\274\274\274\274\274\274\333\u0117\17\33\52\61\110\321\14\14\14\14" +
		"\14\14\14\u010a\20\35\44\56\23\24\275\275\275\275\275\275\366\275\275\275\275\275" +
		"\15\15\15\15\15\71\71\15\126\126\216\227\276\227\15\276\344\276\276\344\276\344\276" +
		"\51\60\165\123\207\235\312\367\303\304\355\314\277\277\342\345\277\277\345\277\u0107" +
		"\277\36\21");

	private static final short[] lapg_rlen = TMLexer.unpack_short(174,
		"\1\0\2\0\17\14\16\13\4\4\3\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\1\0\3\1\1\2" +
		"\2\1\1\1\3\1\0\1\0\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\6\5\5\4\2\1" +
		"\0\3\2\2\1\1\1\1\4\4\1\3\1\0\2\1\2\1\3\1\1\3\1\0\3\2\2\1\1\3\4\3\3\2\1\2\2\1\1\1" +
		"\1\2\1\3\3\1\2\1\3\3\3\1\3\1\3\6\6\2\2\1\2\1\1\1\2\5\2\2\3\1\3\1\1\1\0\5\1\0\3\1" +
		"\1\1\1\1\1\3\3\5\1\1\1\1\1\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(174,
		"\170\170\171\171\73\73\73\73\74\75\75\76\76\77\77\100\101\102\102\103\103\104\104" +
		"\104\104\104\104\104\104\104\104\104\172\172\104\105\106\106\106\107\107\107\110" +
		"\173\173\174\174\175\175\176\176\177\177\111\111\112\113\114\114\114\114\115\116" +
		"\116\117\120\120\121\121\121\122\122\123\123\123\123\124\200\200\124\124\124\124" +
		"\125\125\125\126\126\127\127\201\201\130\131\131\132\132\133\134\134\202\202\135" +
		"\135\135\135\135\136\136\136\137\137\140\140\140\141\141\141\142\142\143\143\143" +
		"\144\144\145\145\145\146\147\147\150\150\150\150\150\150\151\151\151\152\153\153" +
		"\154\154\154\155\156\156\157\157\203\203\157\204\204\157\157\160\160\160\160\161" +
		"\161\162\162\163\163\163\164\165\165\166\167");

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
		"rhsClass",
		"rhsPrimary",
		"rhsAnnotations",
		"annotations",
		"annotation_list",
		"annotation",
		"negative_la",
		"negative_la_clause",
		"expression",
		"literal",
		"expression_list",
		"map_entries",
		"map_separator",
		"name",
		"qualified_id",
		"command",
		"syntax_problem",
		"parsing_algorithmopt",
		"import__optlist",
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
		public static final int parsing_algorithm = 60;
		public static final int import_ = 61;
		public static final int options = 62;
		public static final int option = 63;
		public static final int identifier = 64;
		public static final int symref = 65;
		public static final int type = 66;
		public static final int type_part_list = 67;
		public static final int type_part = 68;
		public static final int pattern = 69;
		public static final int lexer_parts = 70;
		public static final int lexer_part = 71;
		public static final int named_pattern = 72;
		public static final int lexeme = 73;
		public static final int lexem_transition = 74;
		public static final int lexem_attrs = 75;
		public static final int lexem_attribute = 76;
		public static final int state_selector = 77;
		public static final int state_list = 78;
		public static final int stateref = 79;
		public static final int lexer_state = 80;
		public static final int grammar_parts = 81;
		public static final int grammar_part = 82;
		public static final int nonterm = 83;
		public static final int nonterm_type = 84;
		public static final int priority_kw = 85;
		public static final int directive = 86;
		public static final int inputs = 87;
		public static final int inputref = 88;
		public static final int references = 89;
		public static final int references_cs = 90;
		public static final int rules = 91;
		public static final int rule_list = 92;
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
		public static final int annotation_list = 107;
		public static final int annotation = 108;
		public static final int negative_la = 109;
		public static final int negative_la_clause = 110;
		public static final int expression = 111;
		public static final int literal = 112;
		public static final int expression_list = 113;
		public static final int map_entries = 114;
		public static final int map_separator = 115;
		public static final int name = 116;
		public static final int qualified_id = 117;
		public static final int command = 118;
		public static final int syntax_problem = 119;
		public static final int parsing_algorithmopt = 120;
		public static final int import__optlist = 121;
		public static final int type_part_listopt = 122;
		public static final int typeopt = 123;
		public static final int lexem_transitionopt = 124;
		public static final int iconopt = 125;
		public static final int lexem_attrsopt = 126;
		public static final int commandopt = 127;
		public static final int identifieropt = 128;
		public static final int Lnoeoiopt = 129;
		public static final int rhsSuffixopt = 130;
		public static final int map_entriesopt = 131;
		public static final int expression_listopt = 132;
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
			case 4:  // input ::= Llanguage ID '(' ID ')' parsing_algorithmopt ';' import__optlist options '::' Llexer lexer_parts '::' Lparser grammar_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)tmStack[tmHead - 6].value), ((List<ITmaLexerPart>)tmStack[tmHead - 3].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 5:  // input ::= Llanguage ID '(' ID ')' parsing_algorithmopt ';' import__optlist options '::' Llexer lexer_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)tmStack[tmHead - 3].value), ((List<ITmaLexerPart>)tmStack[tmHead].value), ((List<ITmaGrammarPart>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // input ::= Llanguage ID '(' ID ')' parsing_algorithmopt ';' import__optlist '::' Llexer lexer_parts '::' Lparser grammar_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)null), ((List<ITmaLexerPart>)tmStack[tmHead - 3].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // input ::= Llanguage ID '(' ID ')' parsing_algorithmopt ';' import__optlist '::' Llexer lexer_parts
				  lapg_gg.value = new TmaInput(((List<TmaOptionPart>)null), ((List<ITmaLexerPart>)tmStack[tmHead].value), ((List<ITmaGrammarPart>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 11:  // options ::= option
				 lapg_gg.value = new ArrayList<TmaOptionPart>(16); ((List<TmaOptionPart>)lapg_gg.value).add(((TmaOptionPart)tmStack[tmHead].value)); 
				break;
			case 12:  // options ::= options option
				 ((List<TmaOptionPart>)tmStack[tmHead - 1].value).add(((TmaOptionPart)tmStack[tmHead].value)); 
				break;
			case 13:  // option ::= ID '=' expression
				 lapg_gg.value = new TmaOption(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 15:  // identifier ::= ID
				 lapg_gg.value = new TmaIdentifier(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 16:  // symref ::= ID
				 lapg_gg.value = new TmaSymref(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 17:  // type ::= '(' scon ')'
				 lapg_gg.value = ((String)tmStack[tmHead - 1].value); 
				break;
			case 18:  // type ::= '(' type_part_list ')'
				 lapg_gg.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 35:  // pattern ::= regexp
				 lapg_gg.value = new TmaPattern(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // lexer_parts ::= lexer_part
				 lapg_gg.value = new ArrayList<ITmaLexerPart>(64); ((List<ITmaLexerPart>)lapg_gg.value).add(((ITmaLexerPart)tmStack[tmHead].value)); 
				break;
			case 37:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<ITmaLexerPart>)tmStack[tmHead - 1].value).add(((ITmaLexerPart)tmStack[tmHead].value)); 
				break;
			case 38:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<ITmaLexerPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 42:  // named_pattern ::= ID '=' pattern
				 lapg_gg.value = new TmaNamedPattern(((String)tmStack[tmHead - 2].value), ((TmaPattern)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 53:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				 lapg_gg.value = new TmaLexeme(((TmaIdentifier)tmStack[tmHead - 7].value), ((String)tmStack[tmHead - 6].value), ((TmaPattern)tmStack[tmHead - 4].value), ((TmaStateref)tmStack[tmHead - 3].value), ((Integer)tmStack[tmHead - 2].value), ((TmaLexemAttrs)tmStack[tmHead - 1].value), ((TmaCommand)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // lexeme ::= identifier typeopt ':'
				 lapg_gg.value = new TmaLexeme(((TmaIdentifier)tmStack[tmHead - 2].value), ((String)tmStack[tmHead - 1].value), ((TmaPattern)null), ((TmaStateref)null), ((Integer)null), ((TmaLexemAttrs)null), ((TmaCommand)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // lexem_transition ::= '=>' stateref
				 lapg_gg.value = ((TmaStateref)tmStack[tmHead].value); 
				break;
			case 56:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.value = ((TmaLexemAttrs)tmStack[tmHead - 1].value); 
				break;
			case 57:  // lexem_attribute ::= Lsoft
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // lexem_attribute ::= Lclass
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 59:  // lexem_attribute ::= Lspace
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 60:  // lexem_attribute ::= Llayout
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // state_selector ::= '[' state_list ']'
				 lapg_gg.value = new TmaStateSelector(((List<TmaLexerState>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 62:  // state_list ::= lexer_state
				 lapg_gg.value = new ArrayList<Integer>(4); ((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value)); 
				break;
			case 63:  // state_list ::= state_list ',' lexer_state
				 ((List<TmaLexerState>)tmStack[tmHead - 2].value).add(((TmaLexerState)tmStack[tmHead].value)); 
				break;
			case 64:  // stateref ::= ID
				 lapg_gg.value = new TmaStateref(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 65:  // lexer_state ::= identifier '=>' stateref
				 lapg_gg.value = new TmaLexerState(((TmaIdentifier)tmStack[tmHead - 2].value), ((TmaStateref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // lexer_state ::= identifier
				 lapg_gg.value = new TmaLexerState(((TmaIdentifier)tmStack[tmHead].value), ((TmaStateref)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // grammar_parts ::= grammar_part
				 lapg_gg.value = new ArrayList<ITmaGrammarPart>(64); ((List<ITmaGrammarPart>)lapg_gg.value).add(((ITmaGrammarPart)tmStack[tmHead].value)); 
				break;
			case 68:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<ITmaGrammarPart>)tmStack[tmHead - 1].value).add(((ITmaGrammarPart)tmStack[tmHead].value)); 
				break;
			case 69:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<ITmaGrammarPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 72:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 5].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // nonterm ::= annotations identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 4].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // nonterm ::= identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // nonterm_type ::= Lreturns symref
				 lapg_gg.value = new TmaNontermTypeAST(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // nonterm_type ::= Linline Lclass identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 80:  // nonterm_type ::= Lclass identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 81:  // nonterm_type ::= Linterface identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 82:  // nonterm_type ::= type
				 lapg_gg.value = new TmaNontermTypeRaw(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.value = new TmaDirectivePrio(((String)tmStack[tmHead - 2].value), ((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.value = new TmaDirectiveInput(((List<TmaInputref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // inputs ::= inputref
				 lapg_gg.value = new ArrayList<TmaInputref>(); ((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value)); 
				break;
			case 89:  // inputs ::= inputs ',' inputref
				 ((List<TmaInputref>)tmStack[tmHead - 2].value).add(((TmaInputref)tmStack[tmHead].value)); 
				break;
			case 92:  // inputref ::= symref Lnoeoiopt
				 lapg_gg.value = new TmaInputref(((TmaSymref)tmStack[tmHead - 1].value), ((String)tmStack[tmHead].value) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // references ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 94:  // references ::= references symref
				 ((List<TmaSymref>)tmStack[tmHead - 1].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 95:  // references_cs ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 96:  // references_cs ::= references_cs ',' symref
				 ((List<TmaSymref>)tmStack[tmHead - 2].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 98:  // rule_list ::= rule0
				 lapg_gg.value = new ArrayList<TmaRule0>(); ((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value)); 
				break;
			case 99:  // rule_list ::= rule_list '|' rule0
				 ((List<TmaRule0>)tmStack[tmHead - 2].value).add(((TmaRule0)tmStack[tmHead].value)); 
				break;
			case 102:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)tmStack[tmHead - 2].value), ((List<ITmaRhsPart>)tmStack[tmHead - 1].value), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // rule0 ::= rhsPrefix rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)tmStack[tmHead - 1].value), ((List<ITmaRhsPart>)null), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // rule0 ::= rhsParts rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)null), ((List<ITmaRhsPart>)tmStack[tmHead - 1].value), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // rule0 ::= rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)null), ((List<ITmaRhsPart>)null), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // rule0 ::= syntax_problem
				 lapg_gg.value = new TmaRule0(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 107:  // rhsPrefix ::= '[' annotations ']'
				 lapg_gg.value = new TmaRhsPrefix(((TmaAnnotations)tmStack[tmHead - 1].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // rhsPrefix ::= '[' rhsAnnotations identifier ']'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)tmStack[tmHead - 2].value), ((TmaIdentifier)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // rhsPrefix ::= '[' identifier ']'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)null), ((TmaIdentifier)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // rhsSuffix ::= '%' Lprio symref
				 lapg_gg.value = new TmaRhsPrio(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // rhsSuffix ::= '%' Lshift
				 lapg_gg.value = new TmaRhsShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // rhsParts ::= rhsPart
				 lapg_gg.value = new ArrayList<ITmaRhsPart>(); ((List<ITmaRhsPart>)lapg_gg.value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 113:  // rhsParts ::= rhsParts rhsPart
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 114:  // rhsParts ::= rhsParts syntax_problem
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 119:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				 lapg_gg.value = new TmaRhsAnnotated(((TmaRuleAnnotations)tmStack[tmHead - 1].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // rhsAssignment ::= identifier '=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // rhsAssignment ::= identifier '+=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // rhsOptional ::= rhsCast '?'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 126:  // rhsCast ::= rhsClass Las symref
				 lapg_gg.value = new TmaRhsCast(((ITmaRhsPart)tmStack[tmHead - 2].value), ((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // rhsCast ::= rhsClass Las literal
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 128:  // rhsUnordered ::= rhsPart '&' rhsPart
				 lapg_gg.value = new TmaRhsUnordered(((ITmaRhsPart)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // rhsClass ::= identifier ':' rhsPrimary
				 lapg_gg.value = ((ITmaRhsPart)tmStack[tmHead].value); reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 131:  // rhsPrimary ::= symref
				 lapg_gg.value = new TmaRhsSymbol(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 132:  // rhsPrimary ::= '(' rules ')'
				 lapg_gg.value = new TmaRhsNested(((List<TmaRule0>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 133:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 134:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 135:  // rhsPrimary ::= rhsPrimary '*'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 136:  // rhsPrimary ::= rhsPrimary '+'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 137:  // rhsAnnotations ::= annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(null, ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 138:  // rhsAnnotations ::= negative_la annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead - 1].value), ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 139:  // rhsAnnotations ::= negative_la
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 140:  // annotations ::= annotation_list
				 lapg_gg.value = new TmaAnnotations(((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 141:  // annotation_list ::= annotation
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 142:  // annotation_list ::= annotation_list annotation
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 143:  // annotation ::= '@' ID '{' expression '}'
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead - 3].value), ((ITmaExpression)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 144:  // annotation ::= '@' ID
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead].value), ((ITmaExpression)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 145:  // annotation ::= '@' syntax_problem
				 lapg_gg.value = new TmaMapEntriesItem(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 146:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.value = new TmaNegativeLa(((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 147:  // negative_la_clause ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 148:  // negative_la_clause ::= negative_la_clause '|' symref
				 ((List<TmaSymref>)tmStack[tmHead - 2].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 153:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.value = new TmaExpressionInstance(((TmaName)tmStack[tmHead - 3].value), ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 156:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.value = new TmaExpressionArray(((List<ITmaExpression>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 158:  // literal ::= scon
				 lapg_gg.value = new TmaExpressionLiteral(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 159:  // literal ::= icon
				 lapg_gg.value = new TmaExpressionLiteral(((Integer)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 160:  // literal ::= Ltrue
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 161:  // literal ::= Lfalse
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 162:  // expression_list ::= expression
				 lapg_gg.value = new ArrayList(); ((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 163:  // expression_list ::= expression_list ',' expression
				 ((List<ITmaExpression>)tmStack[tmHead - 2].value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 164:  // map_entries ::= ID map_separator expression
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 165:  // map_entries ::= map_entries ',' ID map_separator expression
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 4].value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, tmStack[tmHead - 2].offset, lapg_gg.endoffset)); 
				break;
			case 169:  // name ::= qualified_id
				 lapg_gg.value = new TmaName(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 171:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 172:  // command ::= code
				 lapg_gg.value = new TmaCommand(source, tmStack[tmHead].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 173:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 280);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 281);
	}
}
