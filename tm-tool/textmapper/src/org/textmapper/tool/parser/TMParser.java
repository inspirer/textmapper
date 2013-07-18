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
	private static final int[] tmAction = TMLexer.unpack_int(285,
		"\uffff\uffff\uffff\uffff\uffff\uffff\ufffd\uffff\244\0\245\0\uffcf\uffff\263\0\26" +
		"\0\246\0\247\0\uffff\uffff\234\0\233\0\243\0\260\0\uff97\uffff\uff8f\uffff\uffff" +
		"\uffff\uff83\uffff\15\0\uffff\uffff\250\0\uff55\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\6\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\16\0\uffff" +
		"\uffff\uffff\uffff\uff4f\uffff\uffff\uffff\21\0\24\0\uffff\uffff\242\0\uff49\uffff" +
		"\uffff\uffff\uffff\uffff\11\0\261\0\20\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\4\0\uff1f\uffff\22\0\251\0\uffff\uffff\uff19\uffff\uffff\uffff\uff13\uffff" +
		"\uffff\uffff\17\0\uffff\uffff\uff0d\uffff\uff05\uffff\ufeff\uffff\52\0\56\0\57\0" +
		"\55\0\23\0\uffff\uffff\2\0\255\0\256\0\254\0\uffff\uffff\uffff\uffff\237\0\uffff" +
		"\uffff\14\0\25\0\ufecf\uffff\uffff\uffff\104\0\uffff\uffff\uffff\uffff\61\0\uffff" +
		"\uffff\53\0\54\0\uffff\uffff\uffff\uffff\uffff\uffff\ufec7\uffff\111\0\114\0\115" +
		"\0\uffff\uffff\ufe97\uffff\223\0\252\0\uffff\uffff\10\0\uffff\uffff\uffff\uffff\103" +
		"\0\51\0\60\0\uffff\uffff\41\0\42\0\35\0\36\0\ufe6d\uffff\33\0\34\0\40\0\43\0\45\0" +
		"\44\0\37\0\uffff\uffff\32\0\ufe2d\uffff\uffff\uffff\131\0\132\0\133\0\uffff\uffff" +
		"\ufdfb\uffff\227\0\ufdcb\uffff\uffff\uffff\uffff\uffff\ufd91\uffff\ufd67\uffff\130" +
		"\0\uffff\uffff\112\0\113\0\uffff\uffff\224\0\uffff\uffff\106\0\107\0\105\0\27\0\ufd3d" +
		"\uffff\uffff\uffff\30\0\31\0\ufcfd\uffff\ufcc5\uffff\uffff\uffff\136\0\143\0\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufcbd\uffff\uffff\uffff\ufc83\uffff\262" +
		"\0\uffff\uffff\211\0\uffff\uffff\ufc37\uffff\150\0\ufc2f\uffff\152\0\ufbf7\uffff" +
		"\ufbbd\uffff\171\0\174\0\176\0\ufb7f\uffff\172\0\ufb3f\uffff\ufafd\uffff\uffff\uffff" +
		"\ufab7\uffff\ufa8b\uffff\173\0\160\0\157\0\ufa5f\uffff\122\0\123\0\126\0\127\0\ufa35" +
		"\uffff\uf9fb\uffff\uffff\uffff\253\0\50\0\uffff\uffff\63\0\uf9c1\uffff\140\0\142" +
		"\0\135\0\uffff\uffff\134\0\144\0\uffff\uffff\uffff\uffff\165\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uf98b\uffff\uffff\uffff\uf95f\uffff\231\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\121\0\uf925\uffff\uf8e9\uffff\155\0\uf8af\uffff\170\0\156" +
		"\0\uffff\uffff\202\0\uffff\uffff\215\0\216\0\175\0\uf871\uffff\125\0\uffff\uffff" +
		"\uffff\uffff\uf845\uffff\75\0\65\0\uf80b\uffff\137\0\225\0\164\0\163\0\uffff\uffff" +
		"\161\0\212\0\uffff\uffff\uffff\uffff\230\0\uffff\uffff\177\0\uf7d7\uffff\200\0\151" +
		"\0\154\0\uf791\uffff\204\0\205\0\120\0\117\0\uffff\uffff\uffff\uffff\67\0\uf753\uffff" +
		"\162\0\uffff\uffff\232\0\116\0\77\0\100\0\101\0\102\0\uffff\uffff\71\0\73\0\uffff" +
		"\uffff\76\0\214\0\213\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TMLexer.unpack_short(2268,
		"\45\uffff\10\1\36\1\37\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61" +
		"\1\62\1\63\1\64\1\65\1\66\1\67\1\uffff\ufffe\2\uffff\3\uffff\20\uffff\36\uffff\37" +
		"\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57" +
		"\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46" +
		"\uffff\40\uffff\41\uffff\42\uffff\21\241\uffff\ufffe\22\uffff\65\uffff\14\7\uffff" +
		"\ufffe\15\uffff\14\257\22\257\24\257\65\257\uffff\ufffe\45\uffff\10\0\36\0\37\0\46" +
		"\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57\0\60\0\61\0\62\0\63\0\64\0\65\0\66" +
		"\0\67\0\uffff\ufffe\16\uffff\21\240\uffff\ufffe\10\uffff\0\5\uffff\ufffe\37\uffff" +
		"\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff" +
		"\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff" +
		"\24\236\uffff\ufffe\10\uffff\0\3\uffff\ufffe\16\uffff\24\235\uffff\ufffe\65\uffff" +
		"\14\7\uffff\ufffe\12\uffff\17\25\22\25\uffff\ufffe\22\uffff\17\62\uffff\ufffe\20" +
		"\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61" +
		"\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\0\12\10\12\uffff\ufffe\13\uffff\16\110\21\110\uffff\ufffe" +
		"\6\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\0\13\uffff\ufffe\35\uffff\37\222\46\222\47\222" +
		"\50\222\51\222\52\222\53\222\54\222\55\222\56\222\57\222\60\222\61\222\62\222\63" +
		"\222\64\222\65\222\66\222\67\222\uffff\ufffe\15\uffff\16\uffff\20\uffff\21\uffff" +
		"\22\uffff\26\uffff\27\uffff\30\uffff\33\uffff\34\uffff\35\uffff\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\24\47\uffff" +
		"\ufffe\1\uffff\0\74\10\74\20\74\36\74\37\74\46\74\47\74\50\74\51\74\52\74\53\74\54" +
		"\74\55\74\56\74\57\74\60\74\61\74\62\74\63\74\64\74\65\74\66\74\67\74\uffff\ufffe" +
		"\72\uffff\21\226\22\226\35\226\37\226\46\226\47\226\50\226\51\226\52\226\53\226\54" +
		"\226\55\226\56\226\57\226\60\226\61\226\62\226\63\226\64\226\65\226\66\226\67\226" +
		"\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff" +
		"\11\153\14\153\uffff\ufffe\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff" +
		"\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\7\124\uffff\ufffe\37\uffff\67\uffff\66\uffff" +
		"\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff" +
		"\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\7\124\uffff\ufffe" +
		"\15\uffff\16\uffff\20\uffff\21\uffff\22\uffff\26\uffff\27\uffff\30\uffff\33\uffff" +
		"\34\uffff\35\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\24\46\uffff\ufffe\13\uffff\0\64\3\64\10\64\20\64\22\64" +
		"\36\64\37\64\46\64\47\64\50\64\51\64\52\64\53\64\54\64\55\64\56\64\57\64\60\64\61" +
		"\64\62\64\63\64\64\64\65\64\66\64\67\64\71\64\uffff\ufffe\56\uffff\14\141\16\141" +
		"\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff" +
		"\11\153\24\153\uffff\ufffe\12\25\17\25\32\25\6\26\11\26\14\26\22\26\23\26\24\26\30" +
		"\26\31\26\33\26\34\26\35\26\36\26\37\26\43\26\44\26\46\26\47\26\50\26\51\26\52\26" +
		"\53\26\54\26\55\26\56\26\57\26\60\26\61\26\62\26\63\26\64\26\65\26\66\26\67\26\71" +
		"\26\uffff\ufffe\11\uffff\14\147\24\147\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff" +
		"\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff" +
		"\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\71\uffff\11\153\14\153\24\153\uffff\ufffe\6\uffff\22\uffff\23\uffff\35" +
		"\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61" +
		"\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50" +
		"\uffff\47\uffff\46\uffff\71\uffff\11\153\14\153\24\153\uffff\ufffe\34\uffff\6\166" +
		"\11\166\14\166\22\166\23\166\24\166\35\166\36\166\37\166\43\166\46\166\47\166\50" +
		"\166\51\166\52\166\53\166\54\166\55\166\56\166\57\166\60\166\61\166\62\166\63\166" +
		"\64\166\65\166\66\166\67\166\71\166\uffff\ufffe\33\uffff\6\201\11\201\14\201\22\201" +
		"\23\201\24\201\34\201\35\201\36\201\37\201\43\201\46\201\47\201\50\201\51\201\52" +
		"\201\53\201\54\201\55\201\56\201\57\201\60\201\61\201\62\201\63\201\64\201\65\201" +
		"\66\201\67\201\71\201\uffff\ufffe\44\uffff\6\203\11\203\14\203\22\203\23\203\24\203" +
		"\33\203\34\203\35\203\36\203\37\203\43\203\46\203\47\203\50\203\51\203\52\203\53" +
		"\203\54\203\55\203\56\203\57\203\60\203\61\203\62\203\63\203\64\203\65\203\66\203" +
		"\67\203\71\203\uffff\ufffe\30\uffff\31\uffff\6\207\11\207\14\207\22\207\23\207\24" +
		"\207\33\207\34\207\35\207\36\207\37\207\43\207\44\207\46\207\47\207\50\207\51\207" +
		"\52\207\53\207\54\207\55\207\56\207\57\207\60\207\61\207\62\207\63\207\64\207\65" +
		"\207\66\207\67\207\71\207\uffff\ufffe\35\uffff\22\217\37\217\46\217\47\217\50\217" +
		"\51\217\52\217\53\217\54\217\55\217\56\217\57\217\60\217\61\217\62\217\63\217\64" +
		"\217\65\217\66\217\67\217\uffff\ufffe\35\uffff\22\221\37\221\46\221\47\221\50\221" +
		"\51\221\52\221\53\221\54\221\55\221\56\221\57\221\60\221\61\221\62\221\63\221\64" +
		"\221\65\221\66\221\67\221\uffff\ufffe\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\7\124\uffff\ufffe\6\uffff\20\uffff" +
		"\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff" +
		"\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\153\14\153\uffff\ufffe" +
		"\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff" +
		"\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff" +
		"\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\153\14" +
		"\153\uffff\ufffe\3\uffff\0\66\10\66\20\66\22\66\36\66\37\66\46\66\47\66\50\66\51" +
		"\66\52\66\53\66\54\66\55\66\56\66\57\66\60\66\61\66\62\66\63\66\64\66\65\66\66\66" +
		"\67\66\71\66\uffff\ufffe\35\uffff\37\217\46\217\47\217\50\217\51\217\52\217\53\217" +
		"\54\217\55\217\56\217\57\217\60\217\61\217\62\217\63\217\64\217\65\217\66\217\67" +
		"\217\21\222\uffff\ufffe\6\uffff\22\uffff\23\uffff\35\uffff\36\uffff\37\uffff\67\uffff" +
		"\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60\uffff\57\uffff\56\uffff" +
		"\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\43\uffff" +
		"\71\uffff\11\153\24\153\uffff\ufffe\6\uffff\20\uffff\22\uffff\23\uffff\35\uffff\36" +
		"\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff\61\uffff\60" +
		"\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47" +
		"\uffff\46\uffff\71\uffff\11\153\14\153\24\153\uffff\ufffe\6\uffff\22\uffff\23\uffff" +
		"\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62\uffff" +
		"\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\71\uffff\11\153\14\153\24\153\uffff\ufffe\34\uffff\6" +
		"\167\11\167\14\167\22\167\23\167\24\167\35\167\36\167\37\167\43\167\46\167\47\167" +
		"\50\167\51\167\52\167\53\167\54\167\55\167\56\167\57\167\60\167\61\167\62\167\63" +
		"\167\64\167\65\167\66\167\67\167\71\167\uffff\ufffe\35\uffff\22\220\37\220\46\220" +
		"\47\220\50\220\51\220\52\220\53\220\54\220\55\220\56\220\57\220\60\220\61\220\62" +
		"\220\63\220\64\220\65\220\66\220\67\220\uffff\ufffe\6\uffff\20\uffff\22\uffff\23" +
		"\uffff\35\uffff\36\uffff\37\uffff\67\uffff\66\uffff\65\uffff\64\uffff\63\uffff\62" +
		"\uffff\61\uffff\60\uffff\57\uffff\56\uffff\55\uffff\54\uffff\53\uffff\52\uffff\51" +
		"\uffff\50\uffff\47\uffff\46\uffff\71\uffff\11\153\14\153\uffff\ufffe\22\uffff\0\70" +
		"\10\70\20\70\36\70\37\70\46\70\47\70\50\70\51\70\52\70\53\70\54\70\55\70\56\70\57" +
		"\70\60\70\61\70\62\70\63\70\64\70\65\70\66\70\67\70\71\70\uffff\ufffe\30\uffff\31" +
		"\uffff\6\210\11\210\14\210\22\210\23\210\24\210\33\210\34\210\35\210\36\210\37\210" +
		"\43\210\44\210\46\210\47\210\50\210\51\210\52\210\53\210\54\210\55\210\56\210\57" +
		"\210\60\210\61\210\62\210\63\210\64\210\65\210\66\210\67\210\71\210\uffff\ufffe\34" +
		"\206\6\206\11\206\14\206\22\206\23\206\24\206\35\206\36\206\37\206\43\206\46\206" +
		"\47\206\50\206\51\206\52\206\53\206\54\206\55\206\56\206\57\206\60\206\61\206\62" +
		"\206\63\206\64\206\65\206\66\206\67\206\71\206\uffff\ufffe\71\uffff\0\72\10\72\20" +
		"\72\36\72\37\72\46\72\47\72\50\72\51\72\52\72\53\72\54\72\55\72\56\72\57\72\60\72" +
		"\61\72\62\72\63\72\64\72\65\72\66\72\67\72\uffff\ufffe");

	private static final short[] lapg_sym_goto = TMLexer.unpack_short(138,
		"\0\2\4\17\31\31\31\45\51\55\57\64\70\102\107\117\124\147\160\212\226\240\241\245" +
		"\251\260\263\264\271\300\327\354\u0129\u0131\u0139\u0140\u0141\u0142\u0144\u0183" +
		"\u01c1\u01ff\u023e\u027c\u02ba\u02f8\u0336\u0374\u03b2\u03f3\u0432\u0470\u04ae\u04ec" +
		"\u052b\u0569\u05a7\u05a7\u05b3\u05b4\u05b5\u05b6\u05b8\u05ba\u05bc\u05bd\u05bf\u05c0" +
		"\u05c2\u05dc\u05fd\u0600\u0602\u0606\u0608\u0609\u060b\u060d\u060f\u0610\u0611\u0612" +
		"\u0614\u0615\u0617\u0619\u061a\u061c\u061e\u0620\u0621\u0623\u0624\u0626\u0628\u0628" +
		"\u062d\u0632\u0638\u063e\u0648\u064f\u065a\u0665\u0671\u067f\u068d\u0698\u06a6\u06b5" +
		"\u06c1\u06c4\u06d3\u06e6\u06f2\u06f3\u06fa\u0702\u0703\u0704\u0706\u0709\u070c\u0718" +
		"\u072d\u072e\u0730\u0731\u0732\u0733\u0734\u0735\u0736\u0739\u073a\u0744\u0745\u0746");

	private static final short[] lapg_sym_from = TMLexer.unpack_short(1862,
		"\u0119\u011a\125\174\1\6\22\40\50\62\114\126\217\236\345\1\6\50\54\62\114\217\236" +
		"\307\345\107\136\204\241\252\254\300\301\326\335\336\355\135\212\215\302\25\44\45" +
		"\65\250\330\43\70\77\146\245\70\122\146\230\35\37\60\117\232\235\247\353\354\u0106" +
		"\21\126\162\172\224\27\71\123\126\162\172\224\232\70\130\146\245\373\1\6\50\61\62" +
		"\101\114\126\162\172\204\217\224\236\241\300\301\335\355\30\123\126\162\172\224\321" +
		"\323\365\20\31\33\100\126\135\162\172\204\215\224\241\252\254\265\300\301\326\331" +
		"\332\333\335\336\343\355\360\204\240\241\252\254\300\301\326\335\336\343\355\53\72" +
		"\74\155\172\225\325\330\u010b\u0112\316\126\162\172\224\126\162\172\224\126\162\172" +
		"\224\264\375\u0115\264\375\u0115\245\126\162\172\224\261\126\162\172\224\255\340" +
		"\u0101\107\126\136\143\162\172\204\224\240\241\252\254\266\267\300\301\324\326\335" +
		"\336\343\351\355\1\6\25\45\50\62\101\114\134\136\204\217\236\241\254\300\301\326" +
		"\335\336\355\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\136\142" +
		"\150\151\162\172\175\201\204\206\207\210\217\224\235\236\240\241\242\252\254\265" +
		"\273\300\301\305\313\317\322\326\331\332\333\335\336\343\345\355\370\371\u010b\1" +
		"\6\50\62\114\217\236\345\1\6\50\62\114\217\236\345\1\6\50\62\114\217\236\326\263" +
		"\3\23\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\135\136\142" +
		"\150\151\162\172\175\201\204\206\207\210\215\217\224\235\236\240\241\242\252\254" +
		"\265\273\300\301\305\313\317\322\326\331\332\333\335\336\343\345\355\370\371\u010b" +
		"\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\136\142\150\151\162" +
		"\172\175\201\204\206\207\210\217\224\235\236\237\240\241\242\252\254\265\273\300" +
		"\301\305\313\317\322\326\331\332\333\335\336\343\345\355\370\371\u010b\1\2\6\13\22" +
		"\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\136\142\150\151\162\172\175\201" +
		"\204\206\207\210\217\224\235\236\237\240\241\242\252\254\265\273\300\301\305\313" +
		"\317\322\326\331\332\333\335\336\343\345\355\370\371\u010b\1\2\6\13\22\25\32\36\45" +
		"\50\52\61\62\76\101\107\114\115\126\134\135\136\142\150\151\162\172\175\201\204\206" +
		"\207\210\215\217\224\235\236\240\241\242\252\254\265\273\300\301\305\313\317\322" +
		"\326\331\332\333\335\336\343\345\355\370\371\u010b\1\2\6\13\22\25\32\36\45\50\52" +
		"\61\62\76\101\107\114\115\126\133\134\136\142\150\151\162\172\175\201\204\206\207" +
		"\210\217\224\235\236\240\241\242\252\254\265\273\300\301\305\313\317\322\326\331" +
		"\332\333\335\336\343\345\355\370\371\u010b\1\2\6\13\22\25\32\36\45\50\52\61\62\76" +
		"\101\107\114\115\126\133\134\136\142\150\151\162\172\175\201\204\206\207\210\217" +
		"\224\235\236\240\241\242\252\254\265\273\300\301\305\313\317\322\326\331\332\333" +
		"\335\336\343\345\355\370\371\u010b\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107" +
		"\114\115\126\133\134\136\142\150\151\162\172\175\201\204\206\207\210\217\224\235" +
		"\236\240\241\242\252\254\265\273\300\301\305\313\317\322\326\331\332\333\335\336" +
		"\343\345\355\370\371\u010b\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115" +
		"\126\133\134\136\142\150\151\162\172\175\201\204\206\207\210\217\224\235\236\240" +
		"\241\242\252\254\265\273\300\301\305\313\317\322\326\331\332\333\335\336\343\345" +
		"\355\370\371\u010b\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134" +
		"\136\142\150\151\162\172\175\201\204\206\207\210\217\224\231\235\236\240\241\242" +
		"\252\254\265\273\300\301\305\313\317\322\326\331\332\333\335\336\343\345\355\370" +
		"\371\u010b\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\136\142" +
		"\150\151\162\172\175\201\204\206\207\210\217\224\235\236\240\241\242\252\254\265" +
		"\273\300\301\305\313\317\322\326\331\332\333\335\336\343\345\355\370\371\u0107\u010b" +
		"\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\135\136\142\150\151" +
		"\162\172\175\201\204\205\206\207\210\215\217\224\235\236\240\241\242\252\254\265" +
		"\273\300\301\305\313\317\322\326\331\332\333\335\336\343\345\355\370\371\u0107\u010b" +
		"\1\2\6\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\135\136\142\150\151" +
		"\162\172\175\201\204\206\207\210\215\217\224\235\236\240\241\242\252\254\265\273" +
		"\300\301\305\313\317\322\326\331\332\333\335\336\343\345\355\370\371\u010b\1\2\6" +
		"\13\22\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\136\142\150\151\162\172" +
		"\175\201\204\206\207\210\217\224\235\236\240\241\242\252\254\265\273\300\301\305" +
		"\313\317\322\326\331\332\333\335\336\343\345\355\370\371\u0107\u010b\1\2\6\13\22" +
		"\25\32\36\45\50\52\61\62\76\101\107\114\115\126\134\136\142\150\151\162\172\175\201" +
		"\204\206\207\210\217\224\235\236\240\241\242\252\254\265\273\300\301\305\313\317" +
		"\322\326\331\332\333\335\336\343\345\355\370\371\u0107\u010b\0\1\2\6\13\22\25\32" +
		"\36\45\50\52\61\62\76\101\107\114\115\126\134\136\142\150\151\162\172\175\201\204" +
		"\206\207\210\217\224\235\236\240\241\242\252\254\265\273\300\301\305\313\317\322" +
		"\326\331\332\333\335\336\343\345\355\370\371\u010b\1\2\6\13\20\22\25\32\36\45\50" +
		"\52\61\62\73\76\101\107\114\115\126\134\136\142\150\151\162\172\175\201\204\206\207" +
		"\210\217\224\235\236\240\241\242\252\254\265\273\300\301\305\313\317\322\326\331" +
		"\332\333\335\336\343\345\355\370\371\u010b\1\2\6\13\22\25\32\36\42\45\50\52\61\62" +
		"\76\101\107\114\115\126\134\136\142\150\151\162\172\175\201\204\206\207\210\217\224" +
		"\235\236\240\241\242\252\254\265\273\300\301\305\313\317\322\326\331\332\333\335" +
		"\336\343\345\355\370\371\u010b\1\2\6\13\22\25\32\36\45\50\52\61\62\63\76\101\107" +
		"\114\115\126\134\136\142\150\151\162\172\175\201\204\206\207\210\217\224\235\236" +
		"\240\241\242\252\254\265\273\300\301\305\313\317\322\326\331\332\333\335\336\343" +
		"\345\355\370\371\u010b\204\241\252\254\300\301\326\335\336\343\355\u0109\202\0\0" +
		"\25\45\44\65\20\73\3\3\23\25\25\45\61\76\101\107\136\142\151\204\207\210\240\241" +
		"\252\254\265\273\300\301\322\326\331\333\335\336\343\355\1\6\50\62\114\175\201\204" +
		"\206\217\235\236\241\242\252\254\265\300\301\313\317\326\331\332\333\335\336\343" +
		"\345\355\370\371\u010b\100\135\215\126\162\126\162\172\224\125\174\61\61\101\61\101" +
		"\61\101\230\360\u0107\61\101\76\150\305\76\151\107\107\136\107\136\135\215\133\107" +
		"\136\175\175\313\201\370\204\241\300\301\355\204\241\300\301\355\204\241\300\301" +
		"\335\355\204\241\300\301\335\355\204\241\252\254\300\301\326\335\336\355\204\241" +
		"\252\300\301\335\355\204\241\252\254\300\301\326\335\336\343\355\204\241\252\254" +
		"\300\301\326\335\336\343\355\204\241\252\254\265\300\301\326\335\336\343\355\204" +
		"\241\252\254\265\300\301\326\331\333\335\336\343\355\204\241\252\254\265\300\301" +
		"\326\331\333\335\336\343\355\204\241\252\254\300\301\326\335\336\343\355\204\241" +
		"\252\254\265\300\301\326\331\333\335\336\343\355\204\241\252\254\265\300\301\326" +
		"\331\332\333\335\336\343\355\204\240\241\252\254\300\301\326\335\336\343\355\107" +
		"\136\240\107\136\204\240\241\252\254\267\300\301\326\335\336\343\355\107\136\143" +
		"\204\240\241\252\254\266\267\300\301\324\326\335\336\343\351\355\204\240\241\252" +
		"\254\300\301\326\335\336\343\355\242\1\6\50\62\114\217\236\1\6\50\62\114\217\236" +
		"\345\6\52\70\146\2\13\32\2\13\32\204\241\252\254\300\301\326\335\336\343\355\u0109" +
		"\1\6\25\45\50\62\101\114\134\136\204\217\236\241\254\300\301\326\335\336\355\3\20" +
		"\73\162\100\230\307\360\u0109\207\210\273\231\204\241\252\254\300\301\326\335\336" +
		"\355\52\6");

	private static final short[] lapg_sym_to = TMLexer.unpack_short(1862,
		"\u011b\u011c\153\153\4\4\37\60\4\4\4\155\4\4\4\5\5\5\74\5\5\5\5\357\5\133\133\237" +
		"\237\237\237\237\237\237\237\237\237\204\300\301\355\42\63\42\63\335\371\62\111\125" +
		"\111\331\112\150\112\305\55\57\75\147\312\314\334\u0104\u0105\u010d\36\156\156\156" +
		"\156\50\115\151\157\157\157\157\313\113\174\113\332\332\6\6\6\76\6\76\6\160\160\160" +
		"\240\6\160\6\240\240\240\240\240\51\152\161\161\161\161\364\366\u010a\32\52\54\126" +
		"\162\126\162\162\241\126\162\241\241\241\241\241\241\241\241\241\241\241\241\241" +
		"\241\u0107\242\242\242\242\242\242\242\242\242\242\242\242\73\116\120\223\226\304" +
		"\367\372\u0115\u0116\362\163\163\163\163\164\164\164\164\165\165\165\165\346\346" +
		"\u0117\347\347\u0118\333\166\166\166\166\344\167\167\167\167\343\343\343\134\170" +
		"\134\134\170\170\134\170\134\134\134\134\134\134\134\134\134\134\134\134\134\134" +
		"\134\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\7\10\17\10\17\40\43\17\56\43\10\70\77" +
		"\10\121\77\121\10\146\171\202\121\121\220\121\171\171\10\10\243\10\121\121\10\171" +
		"\10\10\121\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243" +
		"\10\243\10\10\10\11\11\11\11\11\11\11\11\12\12\12\12\12\12\12\12\13\13\13\13\13\13" +
		"\13\370\345\22\22\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10\146\171\202" +
		"\205\121\121\220\121\171\171\10\10\243\10\121\121\205\10\171\10\10\121\243\10\243" +
		"\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10\10\10\10" +
		"\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10\146\171\202\121\121\220\121\171" +
		"\171\10\10\243\10\121\121\10\171\10\10\317\121\243\10\243\243\243\121\243\243\220" +
		"\10\10\121\243\243\10\243\243\243\243\10\243\10\10\10\10\17\10\17\40\43\17\56\43" +
		"\10\70\77\10\121\77\121\10\146\171\202\121\121\220\121\171\171\10\10\243\10\121\121" +
		"\10\171\10\10\320\121\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243" +
		"\243\243\243\10\243\10\10\10\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10" +
		"\146\171\202\206\121\121\220\121\171\171\10\10\243\10\121\121\206\10\171\10\10\121" +
		"\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10" +
		"\10\10\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10\146\171\175\202\121\121" +
		"\220\121\171\171\10\10\243\10\121\121\10\171\10\10\121\243\10\243\243\243\121\243" +
		"\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10\10\10\10\17\10\17\40\43\17" +
		"\56\43\10\70\77\10\121\77\121\10\146\171\176\202\121\121\220\121\171\171\10\10\243" +
		"\10\121\121\10\171\10\10\121\243\10\243\243\243\121\243\243\220\10\10\121\243\243" +
		"\10\243\243\243\243\10\243\10\10\10\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77" +
		"\121\10\146\171\177\202\121\121\220\121\171\171\10\10\243\10\121\121\10\171\10\10" +
		"\121\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243" +
		"\10\10\10\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10\146\171\200\202\121" +
		"\121\220\121\171\171\10\10\243\10\121\121\10\171\10\10\121\243\10\243\243\243\121" +
		"\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10\10\10\10\17\10\17\40" +
		"\43\17\56\43\10\70\77\10\121\77\121\10\146\171\202\121\121\220\121\171\171\10\10" +
		"\243\10\121\121\10\171\310\10\10\121\243\10\243\243\243\121\243\243\220\10\10\121" +
		"\243\243\10\243\243\243\243\10\243\10\10\10\10\17\10\17\40\43\17\56\43\10\70\77\10" +
		"\121\77\121\10\146\171\202\121\121\220\121\171\171\10\10\243\10\121\121\10\171\10" +
		"\10\121\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10" +
		"\243\10\10\u010e\10\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10\146\171" +
		"\202\207\121\121\220\121\171\171\10\10\243\273\10\121\121\207\10\171\10\10\121\243" +
		"\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10\10" +
		"\u010f\10\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10\146\171\202\210\121" +
		"\121\220\121\171\171\10\10\243\10\121\121\210\10\171\10\10\121\243\10\243\243\243" +
		"\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10\10\10\10\17\10\17" +
		"\40\43\17\56\43\10\70\77\10\121\77\121\10\146\171\202\121\121\220\121\171\171\10" +
		"\10\243\10\121\121\10\171\10\10\121\243\10\243\243\243\121\243\243\220\10\10\121" +
		"\243\243\10\243\243\243\243\10\243\10\10\u0110\10\10\17\10\17\40\43\17\56\43\10\70" +
		"\77\10\121\77\121\10\146\171\202\121\121\220\121\171\171\10\10\243\10\121\121\10" +
		"\171\10\10\121\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243" +
		"\243\10\243\10\10\u0111\10\2\10\17\10\17\40\43\17\56\43\10\70\77\10\121\77\121\10" +
		"\146\171\202\121\121\220\121\171\171\10\10\243\10\121\121\10\171\10\10\121\243\10" +
		"\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10\10\10" +
		"\10\17\10\17\33\40\43\17\56\43\10\70\77\10\33\121\77\121\10\146\171\202\121\121\220" +
		"\121\171\171\10\10\243\10\121\121\10\171\10\10\121\243\10\243\243\243\121\243\243" +
		"\220\10\10\121\243\243\10\243\243\243\243\10\243\10\10\10\10\17\10\17\40\43\17\56" +
		"\61\43\10\70\77\10\121\77\121\10\146\171\202\121\121\220\121\171\171\10\10\243\10" +
		"\121\121\10\171\10\10\121\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10" +
		"\243\243\243\243\10\243\10\10\10\10\17\10\17\40\43\17\56\43\10\70\77\10\107\121\77" +
		"\121\10\146\171\202\121\121\220\121\171\171\10\10\243\10\121\121\10\171\10\10\121" +
		"\243\10\243\243\243\121\243\243\220\10\10\121\243\243\10\243\243\243\243\10\243\10" +
		"\10\10\244\244\244\244\244\244\244\244\244\244\244\244\236\u0119\3\44\65\64\110\34" +
		"\34\23\24\41\45\46\66\100\122\100\135\135\215\122\245\275\275\321\245\245\245\245" +
		"\275\245\245\365\245\373\373\245\245\245\245\14\14\14\14\14\231\234\246\274\14\315" +
		"\14\246\327\246\246\246\246\246\231\363\246\246\246\246\246\246\246\u0102\246\234" +
		"\u010c\315\127\211\211\172\224\173\173\227\227\154\230\101\102\131\103\103\104\104" +
		"\306\u0108\u0112\105\105\123\221\356\124\222\136\137\213\140\140\212\302\201\141" +
		"\141\232\233\361\235\u010b\247\325\353\354\u0106\250\250\250\250\250\251\251\251" +
		"\251\377\251\252\252\252\252\252\252\253\253\253\253\253\253\253\253\253\253\254" +
		"\326\336\254\254\254\254\255\255\255\340\255\255\340\255\340\u0101\255\256\256\256" +
		"\256\256\256\256\256\256\256\256\257\257\257\257\350\257\257\257\257\257\257\257" +
		"\260\260\260\260\260\260\260\260\374\376\260\260\260\260\261\261\261\261\261\261" +
		"\261\261\261\261\261\261\261\261\262\262\262\262\262\262\262\262\262\262\262\263" +
		"\263\263\263\263\263\263\263\263\263\263\263\263\263\264\264\264\264\264\264\264" +
		"\264\264\375\264\264\264\264\264\265\322\265\265\265\265\265\265\265\265\265\265" +
		"\142\142\323\143\143\266\324\266\266\266\351\266\266\266\266\266\266\266\144\144" +
		"\216\144\144\144\144\144\216\144\144\144\216\144\144\144\144\216\144\267\267\267" +
		"\267\267\267\267\267\267\267\267\267\330\u011a\26\67\106\145\303\316\15\15\15\15" +
		"\15\15\15\u0103\27\71\114\217\20\31\53\21\21\21\270\270\270\270\270\270\270\270\270" +
		"\270\270\u0113\16\16\47\47\16\16\132\16\203\214\271\16\16\271\341\271\271\341\271" +
		"\341\271\25\35\117\225\130\307\360\u0109\u0114\276\277\352\311\272\272\337\342\272" +
		"\272\342\272\u0100\272\72\30");

	private static final short[] lapg_rlen = TMLexer.unpack_short(180,
		"\1\0\5\4\4\3\1\0\7\4\3\3\4\1\2\4\3\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\1\0" +
		"\3\1\1\2\2\1\1\1\3\1\0\1\0\1\0\1\0\1\0\10\3\2\3\1\1\1\1\3\1\3\1\3\1\1\2\2\1\1\6\5" +
		"\5\4\2\1\0\3\2\2\1\1\1\1\4\4\1\3\1\0\2\1\2\1\3\1\1\3\1\0\3\2\2\1\1\3\4\3\3\2\1\2" +
		"\2\1\1\1\1\2\1\3\3\1\2\1\3\3\3\1\3\1\3\6\6\2\2\1\2\1\1\1\2\5\2\2\3\1\3\1\1\1\0\5" +
		"\1\0\3\1\1\1\1\1\1\3\3\5\1\1\1\1\1\3\1\1");

	private static final short[] lapg_rlex = TMLexer.unpack_short(180,
		"\174\174\73\73\73\73\175\175\74\74\75\76\77\100\100\101\101\102\102\103\103\104\105" +
		"\106\106\107\107\110\110\110\110\110\110\110\110\110\110\110\176\176\110\111\112" +
		"\112\112\113\113\113\114\177\177\200\200\201\201\202\202\203\203\115\115\116\117" +
		"\120\120\120\120\121\122\122\123\124\124\125\125\125\126\126\127\127\127\127\130" +
		"\204\204\130\130\130\130\131\131\131\132\132\133\133\205\205\134\135\135\136\136" +
		"\137\140\140\206\206\141\141\141\141\141\142\142\142\143\143\144\144\144\145\145" +
		"\145\146\146\147\147\147\150\150\151\151\151\152\153\153\154\154\154\154\154\154" +
		"\155\155\155\156\157\157\160\160\160\161\162\162\163\163\207\207\163\210\210\163" +
		"\163\164\164\164\164\165\165\166\166\167\167\167\170\171\171\172\173");

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
		"header",
		"lexer_section",
		"parser_section",
		"parsing_algorithm",
		"imports",
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
		"importsopt",
		"parsing_algorithmopt",
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
		public static final int header = 60;
		public static final int lexer_section = 61;
		public static final int parser_section = 62;
		public static final int parsing_algorithm = 63;
		public static final int imports = 64;
		public static final int import_ = 65;
		public static final int options = 66;
		public static final int option = 67;
		public static final int identifier = 68;
		public static final int symref = 69;
		public static final int type = 70;
		public static final int type_part_list = 71;
		public static final int type_part = 72;
		public static final int pattern = 73;
		public static final int lexer_parts = 74;
		public static final int lexer_part = 75;
		public static final int named_pattern = 76;
		public static final int lexeme = 77;
		public static final int lexem_transition = 78;
		public static final int lexem_attrs = 79;
		public static final int lexem_attribute = 80;
		public static final int state_selector = 81;
		public static final int state_list = 82;
		public static final int stateref = 83;
		public static final int lexer_state = 84;
		public static final int grammar_parts = 85;
		public static final int grammar_part = 86;
		public static final int nonterm = 87;
		public static final int nonterm_type = 88;
		public static final int priority_kw = 89;
		public static final int directive = 90;
		public static final int inputs = 91;
		public static final int inputref = 92;
		public static final int references = 93;
		public static final int references_cs = 94;
		public static final int rules = 95;
		public static final int rule_list = 96;
		public static final int rule0 = 97;
		public static final int rhsPrefix = 98;
		public static final int rhsSuffix = 99;
		public static final int rhsParts = 100;
		public static final int rhsPart = 101;
		public static final int rhsAnnotated = 102;
		public static final int rhsAssignment = 103;
		public static final int rhsOptional = 104;
		public static final int rhsCast = 105;
		public static final int rhsUnordered = 106;
		public static final int rhsClass = 107;
		public static final int rhsPrimary = 108;
		public static final int rhsAnnotations = 109;
		public static final int annotations = 110;
		public static final int annotation_list = 111;
		public static final int annotation = 112;
		public static final int negative_la = 113;
		public static final int negative_la_clause = 114;
		public static final int expression = 115;
		public static final int literal = 116;
		public static final int expression_list = 117;
		public static final int map_entries = 118;
		public static final int map_separator = 119;
		public static final int name = 120;
		public static final int qualified_id = 121;
		public static final int command = 122;
		public static final int syntax_problem = 123;
		public static final int importsopt = 124;
		public static final int parsing_algorithmopt = 125;
		public static final int type_part_listopt = 126;
		public static final int typeopt = 127;
		public static final int lexem_transitionopt = 128;
		public static final int iconopt = 129;
		public static final int lexem_attrsopt = 130;
		public static final int commandopt = 131;
		public static final int identifieropt = 132;
		public static final int Lnoeoiopt = 133;
		public static final int rhsSuffixopt = 134;
		public static final int map_entriesopt = 135;
		public static final int expression_listopt = 136;
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
			case 2:  // input ::= header importsopt options lexer_section parser_section
				 lapg_gg.value = new TmaInput(((TmaHeader)tmStack[tmHead - 4].value), ((List<TmaImport>)tmStack[tmHead - 3].value), ((List<TmaOptionPart>)tmStack[tmHead - 2].value), ((List<ITmaLexerPart>)tmStack[tmHead - 1].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // input ::= header importsopt options lexer_section
				 lapg_gg.value = new TmaInput(((TmaHeader)tmStack[tmHead - 3].value), ((List<TmaImport>)tmStack[tmHead - 2].value), ((List<TmaOptionPart>)tmStack[tmHead - 1].value), ((List<ITmaLexerPart>)tmStack[tmHead].value), ((List<ITmaGrammarPart>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 4:  // input ::= header importsopt lexer_section parser_section
				 lapg_gg.value = new TmaInput(((TmaHeader)tmStack[tmHead - 3].value), ((List<TmaImport>)tmStack[tmHead - 2].value), ((List<TmaOptionPart>)null), ((List<ITmaLexerPart>)tmStack[tmHead - 1].value), ((List<ITmaGrammarPart>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 5:  // input ::= header importsopt lexer_section
				 lapg_gg.value = new TmaInput(((TmaHeader)tmStack[tmHead - 2].value), ((List<TmaImport>)tmStack[tmHead - 1].value), ((List<TmaOptionPart>)null), ((List<ITmaLexerPart>)tmStack[tmHead].value), ((List<ITmaGrammarPart>)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // header ::= Llanguage name '(' name ')' parsing_algorithmopt ';'
				 lapg_gg.value = new TmaHeader(((TmaName)tmStack[tmHead - 5].value), ((TmaName)tmStack[tmHead - 3].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // header ::= Llanguage name parsing_algorithmopt ';'
				 lapg_gg.value = new TmaHeader(((TmaName)tmStack[tmHead - 2].value), ((TmaName)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // lexer_section ::= '::' Llexer lexer_parts
				 lapg_gg.value = ((List<ITmaLexerPart>)tmStack[tmHead].value); 
				break;
			case 11:  // parser_section ::= '::' Lparser grammar_parts
				 lapg_gg.value = ((List<ITmaGrammarPart>)tmStack[tmHead].value); 
				break;
			case 13:  // imports ::= import_
				 lapg_gg.value = new ArrayList<TmaImport>(16); ((List<TmaImport>)lapg_gg.value).add(((TmaImport)tmStack[tmHead].value)); 
				break;
			case 14:  // imports ::= imports import_
				 ((List<TmaImport>)tmStack[tmHead - 1].value).add(((TmaImport)tmStack[tmHead].value)); 
				break;
			case 15:  // import_ ::= Limport ID scon ';'
				 lapg_gg.value = new TmaImport(((String)tmStack[tmHead - 2].value), ((String)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 16:  // import_ ::= Limport scon ';'
				 lapg_gg.value = new TmaImport(((String)null), ((String)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 17:  // options ::= option
				 lapg_gg.value = new ArrayList<TmaOptionPart>(16); ((List<TmaOptionPart>)lapg_gg.value).add(((TmaOptionPart)tmStack[tmHead].value)); 
				break;
			case 18:  // options ::= options option
				 ((List<TmaOptionPart>)tmStack[tmHead - 1].value).add(((TmaOptionPart)tmStack[tmHead].value)); 
				break;
			case 19:  // option ::= ID '=' expression
				 lapg_gg.value = new TmaOption(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 21:  // identifier ::= ID
				 lapg_gg.value = new TmaIdentifier(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 22:  // symref ::= ID
				 lapg_gg.value = new TmaSymref(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 23:  // type ::= '(' scon ')'
				 lapg_gg.value = ((String)tmStack[tmHead - 1].value); 
				break;
			case 24:  // type ::= '(' type_part_list ')'
				 lapg_gg.value = source.getText(tmStack[tmHead - 2].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 41:  // pattern ::= regexp
				 lapg_gg.value = new TmaPattern(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 42:  // lexer_parts ::= lexer_part
				 lapg_gg.value = new ArrayList<ITmaLexerPart>(64); ((List<ITmaLexerPart>)lapg_gg.value).add(((ITmaLexerPart)tmStack[tmHead].value)); 
				break;
			case 43:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<ITmaLexerPart>)tmStack[tmHead - 1].value).add(((ITmaLexerPart)tmStack[tmHead].value)); 
				break;
			case 44:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<ITmaLexerPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 48:  // named_pattern ::= ID '=' pattern
				 lapg_gg.value = new TmaNamedPattern(((String)tmStack[tmHead - 2].value), ((TmaPattern)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 59:  // lexeme ::= identifier typeopt ':' pattern lexem_transitionopt iconopt lexem_attrsopt commandopt
				 lapg_gg.value = new TmaLexeme(((TmaIdentifier)tmStack[tmHead - 7].value), ((String)tmStack[tmHead - 6].value), ((TmaPattern)tmStack[tmHead - 4].value), ((TmaStateref)tmStack[tmHead - 3].value), ((Integer)tmStack[tmHead - 2].value), ((TmaLexemAttrs)tmStack[tmHead - 1].value), ((TmaCommand)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 60:  // lexeme ::= identifier typeopt ':'
				 lapg_gg.value = new TmaLexeme(((TmaIdentifier)tmStack[tmHead - 2].value), ((String)tmStack[tmHead - 1].value), ((TmaPattern)null), ((TmaStateref)null), ((Integer)null), ((TmaLexemAttrs)null), ((TmaCommand)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // lexem_transition ::= '=>' stateref
				 lapg_gg.value = ((TmaStateref)tmStack[tmHead].value); 
				break;
			case 62:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.value = ((TmaLexemAttrs)tmStack[tmHead - 1].value); 
				break;
			case 63:  // lexem_attribute ::= Lsoft
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 64:  // lexem_attribute ::= Lclass
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 65:  // lexem_attribute ::= Lspace
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 66:  // lexem_attribute ::= Llayout
				 lapg_gg.value = new TmaLexemAttrs(LexerRule.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // state_selector ::= '[' state_list ']'
				 lapg_gg.value = new TmaStateSelector(((List<TmaLexerState>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // state_list ::= lexer_state
				 lapg_gg.value = new ArrayList<Integer>(4); ((List<TmaLexerState>)lapg_gg.value).add(((TmaLexerState)tmStack[tmHead].value)); 
				break;
			case 69:  // state_list ::= state_list ',' lexer_state
				 ((List<TmaLexerState>)tmStack[tmHead - 2].value).add(((TmaLexerState)tmStack[tmHead].value)); 
				break;
			case 70:  // stateref ::= ID
				 lapg_gg.value = new TmaStateref(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // lexer_state ::= identifier '=>' stateref
				 lapg_gg.value = new TmaLexerState(((TmaIdentifier)tmStack[tmHead - 2].value), ((TmaStateref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 72:  // lexer_state ::= identifier
				 lapg_gg.value = new TmaLexerState(((TmaIdentifier)tmStack[tmHead].value), ((TmaStateref)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // grammar_parts ::= grammar_part
				 lapg_gg.value = new ArrayList<ITmaGrammarPart>(64); ((List<ITmaGrammarPart>)lapg_gg.value).add(((ITmaGrammarPart)tmStack[tmHead].value)); 
				break;
			case 74:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<ITmaGrammarPart>)tmStack[tmHead - 1].value).add(((ITmaGrammarPart)tmStack[tmHead].value)); 
				break;
			case 75:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<ITmaGrammarPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 78:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 5].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // nonterm ::= annotations identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)tmStack[tmHead - 4].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // nonterm ::= identifier nonterm_type '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 4].value), ((TmaNontermType)tmStack[tmHead - 3].value), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // nonterm ::= identifier '::=' rules ';'
				 lapg_gg.value = new TmaNonterm(((TmaIdentifier)tmStack[tmHead - 3].value), ((TmaNontermType)null), ((List<TmaRule0>)tmStack[tmHead - 1].value), ((TmaAnnotations)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // nonterm_type ::= Lreturns symref
				 lapg_gg.value = new TmaNontermTypeAST(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // nonterm_type ::= Linline Lclass identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 86:  // nonterm_type ::= Lclass identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 87:  // nonterm_type ::= Linterface identifieropt
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 88:  // nonterm_type ::= type
				 lapg_gg.value = new TmaNontermTypeRaw(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.value = new TmaDirectivePrio(((String)tmStack[tmHead - 2].value), ((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.value = new TmaDirectiveInput(((List<TmaInputref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // inputs ::= inputref
				 lapg_gg.value = new ArrayList<TmaInputref>(); ((List<TmaInputref>)lapg_gg.value).add(((TmaInputref)tmStack[tmHead].value)); 
				break;
			case 95:  // inputs ::= inputs ',' inputref
				 ((List<TmaInputref>)tmStack[tmHead - 2].value).add(((TmaInputref)tmStack[tmHead].value)); 
				break;
			case 98:  // inputref ::= symref Lnoeoiopt
				 lapg_gg.value = new TmaInputref(((TmaSymref)tmStack[tmHead - 1].value), ((String)tmStack[tmHead].value) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // references ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 100:  // references ::= references symref
				 ((List<TmaSymref>)tmStack[tmHead - 1].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 101:  // references_cs ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 102:  // references_cs ::= references_cs ',' symref
				 ((List<TmaSymref>)tmStack[tmHead - 2].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 104:  // rule_list ::= rule0
				 lapg_gg.value = new ArrayList<TmaRule0>(); ((List<TmaRule0>)lapg_gg.value).add(((TmaRule0)tmStack[tmHead].value)); 
				break;
			case 105:  // rule_list ::= rule_list '|' rule0
				 ((List<TmaRule0>)tmStack[tmHead - 2].value).add(((TmaRule0)tmStack[tmHead].value)); 
				break;
			case 108:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)tmStack[tmHead - 2].value), ((List<ITmaRhsPart>)tmStack[tmHead - 1].value), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // rule0 ::= rhsPrefix rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)tmStack[tmHead - 1].value), ((List<ITmaRhsPart>)null), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // rule0 ::= rhsParts rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)null), ((List<ITmaRhsPart>)tmStack[tmHead - 1].value), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // rule0 ::= rhsSuffixopt
				 lapg_gg.value = new TmaRule0(((TmaRhsPrefix)null), ((List<ITmaRhsPart>)null), ((TmaRhsSuffix)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // rule0 ::= syntax_problem
				 lapg_gg.value = new TmaRule0(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 113:  // rhsPrefix ::= '[' annotations ']'
				 lapg_gg.value = new TmaRhsPrefix(((TmaAnnotations)tmStack[tmHead - 1].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // rhsPrefix ::= '[' rhsAnnotations identifier ']'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)tmStack[tmHead - 2].value), ((TmaIdentifier)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // rhsPrefix ::= '[' identifier ']'
				 lapg_gg.value = new TmaRhsPrefix(((TmaRuleAnnotations)null), ((TmaIdentifier)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // rhsSuffix ::= '%' Lprio symref
				 lapg_gg.value = new TmaRhsPrio(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // rhsSuffix ::= '%' Lshift
				 lapg_gg.value = new TmaRhsShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // rhsParts ::= rhsPart
				 lapg_gg.value = new ArrayList<ITmaRhsPart>(); ((List<ITmaRhsPart>)lapg_gg.value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 119:  // rhsParts ::= rhsParts rhsPart
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((ITmaRhsPart)tmStack[tmHead].value)); 
				break;
			case 120:  // rhsParts ::= rhsParts syntax_problem
				 ((List<ITmaRhsPart>)tmStack[tmHead - 1].value).add(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 125:  // rhsAnnotated ::= rhsAnnotations rhsAssignment
				 lapg_gg.value = new TmaRhsAnnotated(((TmaRuleAnnotations)tmStack[tmHead - 1].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // rhsAssignment ::= identifier '=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // rhsAssignment ::= identifier '+=' rhsOptional
				 lapg_gg.value = new TmaRhsAssignment(((TmaIdentifier)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // rhsOptional ::= rhsCast '?'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 132:  // rhsCast ::= rhsClass Las symref
				 lapg_gg.value = new TmaRhsCast(((ITmaRhsPart)tmStack[tmHead - 2].value), ((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 133:  // rhsCast ::= rhsClass Las literal
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 134:  // rhsUnordered ::= rhsPart '&' rhsPart
				 lapg_gg.value = new TmaRhsUnordered(((ITmaRhsPart)tmStack[tmHead - 2].value), ((ITmaRhsPart)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 136:  // rhsClass ::= identifier ':' rhsPrimary
				 lapg_gg.value = ((ITmaRhsPart)tmStack[tmHead].value); reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 137:  // rhsPrimary ::= symref
				 lapg_gg.value = new TmaRhsSymbol(((TmaSymref)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 138:  // rhsPrimary ::= '(' rules ')'
				 lapg_gg.value = new TmaRhsNested(((List<TmaRule0>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 139:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 140:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
				 lapg_gg.value = new TmaRhsList(((List<ITmaRhsPart>)tmStack[tmHead - 4].value), ((List<TmaSymref>)tmStack[tmHead - 2].value), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 141:  // rhsPrimary ::= rhsPrimary '*'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 142:  // rhsPrimary ::= rhsPrimary '+'
				 lapg_gg.value = new TmaRhsQuantifier(((ITmaRhsPart)tmStack[tmHead - 1].value), TmaRhsQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 143:  // rhsAnnotations ::= annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(null, ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 144:  // rhsAnnotations ::= negative_la annotation_list
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead - 1].value), ((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 145:  // rhsAnnotations ::= negative_la
				 lapg_gg.value = new TmaRuleAnnotations(((TmaNegativeLa)tmStack[tmHead].value), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 146:  // annotations ::= annotation_list
				 lapg_gg.value = new TmaAnnotations(((List<TmaMapEntriesItem>)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 147:  // annotation_list ::= annotation
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 148:  // annotation_list ::= annotation_list annotation
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value).add(((TmaMapEntriesItem)tmStack[tmHead].value)); 
				break;
			case 149:  // annotation ::= '@' ID '{' expression '}'
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead - 3].value), ((ITmaExpression)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 150:  // annotation ::= '@' ID
				 lapg_gg.value = new TmaMapEntriesItem(((String)tmStack[tmHead].value), ((ITmaExpression)null), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 151:  // annotation ::= '@' syntax_problem
				 lapg_gg.value = new TmaMapEntriesItem(((TmaSyntaxProblem)tmStack[tmHead].value)); 
				break;
			case 152:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.value = new TmaNegativeLa(((List<TmaSymref>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 153:  // negative_la_clause ::= symref
				 lapg_gg.value = new ArrayList<TmaSymref>(); ((List<TmaSymref>)lapg_gg.value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 154:  // negative_la_clause ::= negative_la_clause '|' symref
				 ((List<TmaSymref>)tmStack[tmHead - 2].value).add(((TmaSymref)tmStack[tmHead].value)); 
				break;
			case 159:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.value = new TmaExpressionInstance(((TmaName)tmStack[tmHead - 3].value), ((List<TmaMapEntriesItem>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 162:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.value = new TmaExpressionArray(((List<ITmaExpression>)tmStack[tmHead - 1].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 164:  // literal ::= scon
				 lapg_gg.value = new TmaExpressionLiteral(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 165:  // literal ::= icon
				 lapg_gg.value = new TmaExpressionLiteral(((Integer)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 166:  // literal ::= Ltrue
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 167:  // literal ::= Lfalse
				 lapg_gg.value = new TmaExpressionLiteral(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 168:  // expression_list ::= expression
				 lapg_gg.value = new ArrayList(); ((List<ITmaExpression>)lapg_gg.value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 169:  // expression_list ::= expression_list ',' expression
				 ((List<ITmaExpression>)tmStack[tmHead - 2].value).add(((ITmaExpression)tmStack[tmHead].value)); 
				break;
			case 170:  // map_entries ::= ID map_separator expression
				 lapg_gg.value = new ArrayList<TmaMapEntriesItem>(); ((List<TmaMapEntriesItem>)lapg_gg.value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 171:  // map_entries ::= map_entries ',' ID map_separator expression
				 ((List<TmaMapEntriesItem>)tmStack[tmHead - 4].value).add(new TmaMapEntriesItem(((String)tmStack[tmHead - 2].value), ((ITmaExpression)tmStack[tmHead].value), source, tmStack[tmHead - 2].offset, lapg_gg.endoffset)); 
				break;
			case 175:  // name ::= qualified_id
				 lapg_gg.value = new TmaName(((String)tmStack[tmHead].value), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 177:  // qualified_id ::= qualified_id '.' ID
				 lapg_gg.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); 
				break;
			case 178:  // command ::= code
				 lapg_gg.value = new TmaCommand(source, tmStack[tmHead].offset+1, tmStack[tmHead].endoffset-1); 
				break;
			case 179:  // syntax_problem ::= error
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
		return (TmaInput) parse(lexer, 0, 283);
	}

	public ITmaExpression parseExpression(TMLexer lexer) throws IOException, ParseException {
		return (ITmaExpression) parse(lexer, 1, 284);
	}
}
