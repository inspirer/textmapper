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
	private static final int[] lapg_action = LapgLexer.unpack_int(212,
		"\uffff\uffff\uffff\uffff\ufffd\uffff\207\0\uffff\uffff\uffff\uffff\4\0\ufff5\uffff" +
		"\uffef\uffff\35\0\7\0\11\0\156\0\157\0\uffcb\uffff\160\0\161\0\uffff\uffff\162\0" +
		"\171\0\uffff\uffff\61\0\uffff\uffff\5\0\uffa1\uffff\uffff\uffff\43\0\uffff\uffff" +
		"\uff7d\uffff\uffff\uffff\uffff\uffff\uff73\uffff\36\0\uff6b\uffff\63\0\70\0\uffff" +
		"\uffff\uff49\uffff\146\0\37\0\3\0\172\0\uff2d\uffff\uffff\uffff\202\0\uffff\uffff" +
		"\uff27\uffff\34\0\41\0\6\0\62\0\40\0\2\0\22\0\uffff\uffff\24\0\25\0\20\0\21\0\uff21" +
		"\uffff\16\0\17\0\23\0\26\0\30\0\27\0\uffff\uffff\15\0\ufeef\uffff\uffff\uffff\uffff" +
		"\uffff\71\0\72\0\73\0\uffff\uffff\ufec9\uffff\152\0\uffff\uffff\10\0\ufea7\uffff" +
		"\64\0\65\0\ufea1\uffff\147\0\uffff\uffff\170\0\ufe9b\uffff\uffff\uffff\12\0\ufe7f" +
		"\uffff\uffff\uffff\13\0\14\0\ufe4d\uffff\ufe23\uffff\uffff\uffff\76\0\103\0\uffff" +
		"\uffff\uffff\uffff\ufe1b\uffff\uffff\uffff\uffff\uffff\173\0\uffff\uffff\ufdf1\uffff" +
		"\uffff\uffff\203\0\33\0\46\0\ufdeb\uffff\101\0\102\0\75\0\uffff\uffff\74\0\104\0" +
		"\151\0\ufdc3\uffff\uffff\uffff\ufd89\uffff\uffff\uffff\206\0\133\0\uffff\uffff\ufd5f" +
		"\uffff\106\0\ufd57\uffff\ufd2d\uffff\ufd01\uffff\ufcd1\uffff\uffff\uffff\uffff\uffff" +
		"\ufc9b\uffff\ufc7b\uffff\111\0\131\0\116\0\115\0\ufc5d\uffff\177\0\200\0\176\0\uffff" +
		"\uffff\uffff\uffff\165\0\uffff\uffff\50\0\ufc33\uffff\77\0\uffff\uffff\121\0\uffff" +
		"\uffff\205\0\uffff\uffff\ufc0d\uffff\154\0\uffff\uffff\66\0\ufbe1\uffff\ufbb5\uffff" +
		"\ufb7d\uffff\uffff\uffff\ufb51\uffff\114\0\ufb33\uffff\124\0\113\0\uffff\uffff\140" +
		"\0\141\0\137\0\ufb03\uffff\ufac9\uffff\117\0\ufa93\uffff\uffff\uffff\174\0\uffff" +
		"\uffff\55\0\56\0\57\0\60\0\uffff\uffff\52\0\53\0\ufa75\uffff\204\0\134\0\uffff\uffff" +
		"\uffff\uffff\153\0\107\0\112\0\ufa3f\uffff\ufa07\uffff\uffff\uffff\120\0\67\0\uffff" +
		"\uffff\54\0\uffff\uffff\155\0\uf9d7\uffff\175\0\uffff\uffff\136\0\135\0\uffff\uffff" +
		"\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = LapgLexer.unpack_short(1628,
		"\13\uffff\20\10\23\10\uffff\ufffe\23\uffff\20\42\uffff\ufffe\1\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41" +
		"\uffff\2\uffff\10\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\1\uffff\53\uffff\52\uffff" +
		"\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff" +
		"\2\uffff\4\uffff\5\uffff\21\uffff\35\uffff\36\uffff\37\uffff\22\166\uffff\ufffe\1" +
		"\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43" +
		"\uffff\42\uffff\41\uffff\2\uffff\10\uffff\21\uffff\34\uffff\0\0\uffff\ufffe\13\uffff" +
		"\11\10\20\10\23\10\uffff\ufffe\23\uffff\11\42\20\42\uffff\ufffe\1\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff" +
		"\41\uffff\2\uffff\10\uffff\34\uffff\0\1\uffff\ufffe\34\uffff\1\145\41\145\42\145" +
		"\43\145\44\145\45\145\46\145\47\145\50\145\51\145\52\145\53\145\uffff\ufffe\17\uffff" +
		"\22\167\uffff\ufffe\16\uffff\23\201\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\16\uffff" +
		"\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27\uffff\30\uffff\32\uffff\33\uffff" +
		"\34\uffff\25\31\uffff\ufffe\3\uffff\0\44\1\44\2\44\10\44\21\44\34\44\41\44\42\44" +
		"\43\44\44\44\45\44\46\44\47\44\50\44\51\44\52\44\53\44\uffff\ufffe\13\uffff\1\150" +
		"\20\150\23\150\34\150\41\150\42\150\43\150\44\150\45\150\46\150\47\150\50\150\51" +
		"\150\52\150\53\150\uffff\ufffe\23\uffff\11\42\uffff\ufffe\23\uffff\11\42\uffff\ufffe" +
		"\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff" +
		"\43\uffff\42\uffff\41\uffff\25\163\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\16\uffff" +
		"\17\uffff\21\uffff\22\uffff\23\uffff\26\uffff\27\uffff\30\uffff\32\uffff\33\uffff" +
		"\34\uffff\25\32\uffff\ufffe\5\uffff\0\45\1\45\2\45\10\45\21\45\23\45\34\45\41\45" +
		"\42\45\43\45\44\45\45\45\46\45\47\45\50\45\51\45\52\45\53\45\55\45\uffff\ufffe\47" +
		"\uffff\15\100\17\100\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\110\15\110\uffff\ufffe\17\uffff\25\164\uffff\ufffe" +
		"\23\uffff\0\47\1\47\2\47\10\47\21\47\34\47\41\47\42\47\43\47\44\47\45\47\46\47\47" +
		"\47\50\47\51\47\52\47\53\47\55\47\uffff\ufffe\13\uffff\20\uffff\1\11\2\11\10\11\12" +
		"\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42\11\43\11" +
		"\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe\1\uffff\53\uffff" +
		"\52\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff" +
		"\41\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\110\25\110\uffff" +
		"\ufffe\12\uffff\15\105\25\105\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff" +
		"\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\55\uffff\12\110\15\110\25\110\uffff\ufffe\1\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41" +
		"\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\110\15\110\25\110" +
		"\uffff\ufffe\33\uffff\1\122\2\122\10\122\12\122\15\122\23\122\24\122\25\122\34\122" +
		"\40\122\41\122\42\122\43\122\44\122\45\122\46\122\47\122\50\122\51\122\52\122\53" +
		"\122\55\122\uffff\ufffe\30\uffff\31\uffff\32\uffff\1\130\2\130\10\130\12\130\15\130" +
		"\23\130\24\130\25\130\33\130\34\130\40\130\41\130\42\130\43\130\44\130\45\130\46" +
		"\130\47\130\50\130\51\130\52\130\53\130\55\130\uffff\ufffe\34\uffff\1\142\23\142" +
		"\41\142\42\142\43\142\44\142\45\142\46\142\47\142\50\142\51\142\52\142\53\142\20" +
		"\145\uffff\ufffe\34\uffff\1\144\23\144\41\144\42\144\43\144\44\144\45\144\46\144" +
		"\47\144\50\144\51\144\52\144\53\144\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff" +
		"\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff" +
		"\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\110\15\110\uffff\ufffe\55\uffff" +
		"\0\51\1\51\2\51\10\51\21\51\34\51\41\51\42\51\43\51\44\51\45\51\46\51\47\51\50\51" +
		"\51\51\52\51\53\51\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff" +
		"\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff" +
		"\24\uffff\34\uffff\40\uffff\55\uffff\12\110\25\110\uffff\ufffe\1\uffff\53\uffff\52" +
		"\uffff\51\uffff\50\uffff\47\uffff\46\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41" +
		"\uffff\2\uffff\10\uffff\23\uffff\24\uffff\34\uffff\55\uffff\12\110\15\110\25\110" +
		"\uffff\ufffe\13\uffff\1\11\2\11\10\11\12\11\15\11\23\11\24\11\25\11\30\11\31\11\32" +
		"\11\33\11\34\11\40\11\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50\11\51\11\52\11" +
		"\53\11\55\11\uffff\ufffe\1\uffff\53\uffff\52\uffff\51\uffff\50\uffff\47\uffff\46" +
		"\uffff\45\uffff\44\uffff\43\uffff\42\uffff\41\uffff\2\uffff\10\uffff\23\uffff\24" +
		"\uffff\34\uffff\55\uffff\12\110\15\110\25\110\uffff\ufffe\34\uffff\1\142\23\142\41" +
		"\142\42\142\43\142\44\142\45\142\46\142\47\142\50\142\51\142\52\142\53\142\uffff" +
		"\ufffe\33\uffff\1\123\2\123\10\123\12\123\15\123\23\123\24\123\25\123\34\123\40\123" +
		"\41\123\42\123\43\123\44\123\45\123\46\123\47\123\50\123\51\123\52\123\53\123\55" +
		"\123\uffff\ufffe\13\uffff\20\uffff\1\11\2\11\10\11\12\11\15\11\23\11\24\11\25\11" +
		"\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42\11\43\11\44\11\45\11\46\11\47\11\50" +
		"\11\51\11\52\11\53\11\55\11\uffff\ufffe\30\uffff\31\uffff\32\uffff\1\126\2\126\10" +
		"\126\12\126\15\126\23\126\24\126\25\126\33\126\34\126\40\126\41\126\42\126\43\126" +
		"\44\126\45\126\46\126\47\126\50\126\51\126\52\126\53\126\55\126\uffff\ufffe\34\uffff" +
		"\1\143\23\143\41\143\42\143\43\143\44\143\45\143\46\143\47\143\50\143\51\143\52\143" +
		"\53\143\uffff\ufffe\30\uffff\31\uffff\32\uffff\1\127\2\127\10\127\12\127\15\127\23" +
		"\127\24\127\25\127\33\127\34\127\40\127\41\127\42\127\43\127\44\127\45\127\46\127" +
		"\47\127\50\127\51\127\52\127\53\127\55\127\uffff\ufffe\13\uffff\1\11\2\11\10\11\12" +
		"\11\15\11\23\11\24\11\25\11\30\11\31\11\32\11\33\11\34\11\40\11\41\11\42\11\43\11" +
		"\44\11\45\11\46\11\47\11\50\11\51\11\52\11\53\11\55\11\uffff\ufffe\33\132\1\132\2" +
		"\132\10\132\12\132\15\132\23\132\24\132\25\132\34\132\40\132\41\132\42\132\43\132" +
		"\44\132\45\132\46\132\47\132\50\132\51\132\52\132\53\132\55\132\uffff\ufffe\30\uffff" +
		"\31\uffff\32\uffff\1\125\2\125\10\125\12\125\15\125\23\125\24\125\25\125\33\125\34" +
		"\125\40\125\41\125\42\125\43\125\44\125\45\125\46\125\47\125\50\125\51\125\52\125" +
		"\53\125\55\125\uffff\ufffe");

	private static final short[] lapg_sym_goto = LapgLexer.unpack_short(100,
		"\0\2\56\102\105\115\127\127\127\142\145\147\160\162\166\173\202\211\230\236\265\276" +
		"\306\312\316\327\334\344\353\u0100\u0107\u010e\u0115\u0116\u0143\u0170\u019d\u01ca" +
		"\u01f7\u0224\u0251\u027e\u02ab\u02d8\u0305\u0305\u030f\u0310\u0311\u0313\u0319\u0336" +
		"\u033a\u033c\u0340\u0343\u0345\u0349\u034a\u034b\u034c\u034e\u0351\u0352\u0355\u0356" +
		"\u0358\u035a\u035d\u0360\u0364\u0368\u036d\u0376\u0383\u038c\u0393\u03a0\u03b1\u03ba" +
		"\u03bb\u03c2\u03c3\u03c4\u03c6\u03c7\u03c8\u03d0\u03da\u03ee\u03f0\u03f1\u03f5\u03f6" +
		"\u03f7\u03f8\u03f9\u0401\u0402\u0403");

	private static final short[] lapg_sym_from = LapgLexer.unpack_short(1027,
		"\320\321\0\1\5\10\16\21\24\30\31\36\41\44\73\102\106\112\124\126\127\131\142\143" +
		"\144\162\170\171\177\200\203\213\217\220\226\230\233\237\241\242\250\275\276\304" +
		"\307\311\0\1\5\10\16\24\30\36\41\124\143\144\170\200\213\217\233\237\241\307\24\104" +
		"\105\1\16\24\31\124\143\217\307\1\4\16\24\26\124\135\143\217\307\10\30\41\144\170" +
		"\177\200\213\233\237\241\115\145\146\175\235\2\34\113\150\166\240\254\262\302\150" +
		"\262\137\142\174\260\31\56\73\102\131\31\52\73\102\131\137\151\33\115\150\166\204" +
		"\254\262\0\1\5\10\16\24\30\31\73\102\124\131\143\217\307\26\31\53\73\102\131\7\31" +
		"\37\55\73\102\117\122\131\144\156\170\177\200\203\213\226\233\237\241\242\250\304" +
		"\144\170\177\200\213\233\237\241\250\66\102\132\152\232\235\267\311\31\73\102\131" +
		"\31\73\102\131\31\73\102\131\202\255\272\313\315\202\255\272\313\315\31\73\102\131" +
		"\202\255\272\313\31\73\102\131\201\245\303\10\30\31\41\45\73\102\131\144\170\177" +
		"\200\205\206\213\233\237\241\243\250\257\1\16\24\124\143\217\307\1\16\24\124\143" +
		"\217\307\1\16\24\124\143\217\307\233\0\1\5\10\16\21\24\30\31\36\41\44\73\102\106" +
		"\112\124\126\127\131\142\143\144\162\167\170\171\177\200\203\213\217\220\226\230" +
		"\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\36\41\44\73\102" +
		"\106\112\124\126\127\131\142\143\144\162\167\170\171\177\200\203\213\217\220\226" +
		"\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\35\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\226" +
		"\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\35\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\226" +
		"\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\35\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\226" +
		"\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\35\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\226" +
		"\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\36\41\44\73" +
		"\102\106\112\124\126\127\131\136\142\143\144\162\170\171\177\200\203\213\217\220" +
		"\226\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\222" +
		"\226\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\222" +
		"\226\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\222" +
		"\226\230\233\237\241\242\250\275\276\304\307\311\0\1\5\10\16\21\24\30\31\36\41\44" +
		"\73\102\106\112\124\126\127\131\142\143\144\162\170\171\177\200\203\213\217\220\222" +
		"\226\230\233\237\241\242\250\275\276\304\307\311\144\170\177\200\213\224\233\237" +
		"\241\250\0\0\0\5\0\5\10\30\41\44\1\16\24\106\112\124\142\143\144\162\170\171\177" +
		"\200\203\213\217\226\230\233\237\241\242\250\275\276\304\307\311\7\37\117\122\31" +
		"\73\31\73\102\131\24\104\105\0\5\0\5\10\30\156\222\4\10\30\10\30\41\35\10\30\41\106" +
		"\106\162\112\275\144\170\213\144\170\213\144\170\213\237\144\170\213\237\144\170" +
		"\177\213\237\144\170\177\200\213\233\237\241\250\144\170\177\200\203\213\226\233" +
		"\237\241\242\250\304\144\170\177\200\213\233\237\241\250\10\30\41\144\170\213\237" +
		"\10\30\41\144\170\177\200\206\213\233\237\241\250\10\30\41\45\144\170\177\200\205" +
		"\206\213\233\237\241\243\250\257\144\170\177\200\213\233\237\241\250\171\1\16\24" +
		"\124\143\217\307\16\126\150\262\21\21\144\170\177\200\213\233\237\241\144\170\177" +
		"\200\213\224\233\237\241\250\0\1\5\10\16\24\30\36\41\124\143\144\170\200\213\217" +
		"\233\237\241\307\10\30\73\7\37\117\122\135\156\224\136\144\170\177\200\213\233\237" +
		"\241\126\16");

	private static final short[] lapg_sym_to = LapgLexer.unpack_short(1027,
		"\322\323\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150\153\65\13\13\166" +
		"\13\166\13\240\240\254\166\13\262\13\13\240\166\240\302\240\13\13\13\13\13\3\3\3" +
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\57\57\57\14\14\14\66\14\14\14\14\15\25\15\15\62" +
		"\15\155\15\15\15\35\35\35\167\167\167\167\167\167\167\167\144\144\213\237\276\24" +
		"\105\143\214\226\226\304\214\304\215\215\161\163\236\306\67\127\67\67\67\70\124\70" +
		"\70\70\162\220\104\104\216\227\256\305\216\4\16\4\4\16\16\4\71\71\71\16\71\16\16" +
		"\16\63\72\125\72\72\72\31\73\31\126\73\73\31\31\73\170\222\170\170\170\170\170\170" +
		"\170\170\170\170\170\170\171\171\171\171\171\171\171\171\171\130\133\154\221\274" +
		"\277\310\315\74\74\74\74\75\75\75\75\76\76\76\76\251\251\251\251\316\252\252\252" +
		"\252\317\77\77\77\77\253\253\253\253\100\100\100\100\250\250\250\36\36\101\36\36" +
		"\101\101\101\36\36\36\36\36\36\36\36\36\36\36\36\36\17\17\17\17\17\17\17\20\20\20" +
		"\20\20\20\20\21\21\21\21\21\21\21\275\2\13\2\34\13\54\13\34\65\113\116\116\65\65" +
		"\13\13\13\150\153\65\13\13\166\13\230\166\13\240\240\254\166\13\262\13\13\240\166" +
		"\240\302\240\13\13\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150" +
		"\153\65\13\13\166\13\231\166\13\240\240\254\166\13\262\13\13\240\166\240\302\240" +
		"\13\13\13\13\13\2\13\2\34\13\54\13\34\65\106\113\116\116\65\65\13\13\13\150\153\65" +
		"\13\13\166\13\166\13\240\240\254\166\13\262\13\13\240\166\240\302\240\13\13\13\13" +
		"\13\2\13\2\34\13\54\13\34\65\107\113\116\116\65\65\13\13\13\150\153\65\13\13\166" +
		"\13\166\13\240\240\254\166\13\262\13\13\240\166\240\302\240\13\13\13\13\13\2\13\2" +
		"\34\13\54\13\34\65\110\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13\166\13" +
		"\240\240\254\166\13\262\13\13\240\166\240\302\240\13\13\13\13\13\2\13\2\34\13\54" +
		"\13\34\65\111\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13\166\13\240\240\254" +
		"\166\13\262\13\13\240\166\240\302\240\13\13\13\13\13\2\13\2\34\13\54\13\34\65\113" +
		"\116\116\65\65\13\13\13\150\153\65\157\13\13\166\13\166\13\240\240\254\166\13\262" +
		"\13\13\240\166\240\302\240\13\13\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65" +
		"\65\13\13\13\150\153\65\13\13\166\13\166\13\240\240\254\166\13\262\263\13\13\240" +
		"\166\240\302\240\13\13\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13" +
		"\13\150\153\65\13\13\166\13\166\13\240\240\254\166\13\262\264\13\13\240\166\240\302" +
		"\240\13\13\13\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150\153\65" +
		"\13\13\166\13\166\13\240\240\254\166\13\262\265\13\13\240\166\240\302\240\13\13\13" +
		"\13\13\2\13\2\34\13\54\13\34\65\113\116\116\65\65\13\13\13\150\153\65\13\13\166\13" +
		"\166\13\240\240\254\166\13\262\266\13\13\240\166\240\302\240\13\13\13\13\13\172\172" +
		"\172\172\172\172\172\172\172\172\320\5\6\27\7\7\37\37\117\122\22\22\22\136\141\22" +
		"\164\22\173\136\173\234\173\173\173\173\22\173\273\173\173\173\173\173\141\312\173" +
		"\22\164\32\32\32\32\102\131\103\103\134\134\60\135\60\10\30\11\11\40\40\223\267\26" +
		"\41\41\42\42\120\112\43\43\43\137\140\225\142\311\174\232\260\175\175\175\176\176" +
		"\176\300\177\177\177\177\200\233\241\200\200\201\201\201\245\201\245\201\245\303" +
		"\202\202\202\202\255\202\272\202\202\202\255\202\313\203\203\242\242\203\242\203" +
		"\242\242\44\44\44\204\204\204\204\45\45\45\205\205\243\243\257\205\243\205\243\243" +
		"\46\46\46\123\46\46\46\46\123\46\46\46\46\46\123\46\123\206\206\206\206\206\206\206" +
		"\206\206\235\321\51\61\147\165\261\314\52\151\217\307\55\56\207\207\207\207\207\207" +
		"\207\207\210\210\210\210\210\270\210\210\210\210\12\23\12\47\23\23\47\114\121\23" +
		"\23\211\211\246\211\23\246\211\246\23\50\64\132\33\115\145\146\156\224\271\160\212" +
		"\212\244\247\212\247\212\301\152\53");

	private static final short[] lapg_rlen = LapgLexer.unpack_short(136,
		"\0\1\3\2\1\2\3\1\1\1\3\3\2\1\1\1\1\1\1\1\1\1\1\1\1\0\1\3\1\1\2\2\3\3\0\1\3\0\1\0" +
		"\1\0\1\7\3\1\1\1\1\1\2\1\2\2\5\6\1\1\1\1\4\4\1\3\0\1\2\1\2\1\1\3\0\1\3\2\2\1\1\2" +
		"\3\2\1\2\2\4\2\3\1\1\3\1\3\6\6\2\2\2\1\2\1\1\1\2\2\4\2\3\1\3\1\1\1\1\1\0\1\5\0\1" +
		"\3\1\1\3\3\5\1\1\1\1\1\3\3\2\1\1");

	private static final short[] lapg_rlex = LapgLexer.unpack_short(136,
		"\131\131\56\56\57\57\60\60\61\62\63\63\64\64\65\65\65\65\65\65\65\65\65\65\65\132" +
		"\132\65\66\67\67\67\70\70\133\133\70\134\134\135\135\136\136\70\71\72\72\72\72\73" +
		"\73\74\74\74\75\75\75\76\76\76\77\77\100\100\137\137\101\102\102\103\104\104\140" +
		"\140\105\105\105\105\105\106\106\106\107\107\107\110\110\110\110\110\110\111\111" +
		"\111\111\111\111\111\112\112\112\113\114\114\115\115\115\116\117\117\120\120\120" +
		"\120\120\141\141\120\142\142\120\120\121\121\122\122\123\123\123\124\125\125\126" +
		"\126\127\130");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
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
		"symbol",
		"reference",
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"lexem_attrs",
		"lexem_attribute",
		"icon_list",
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
		public static final int symbol = 49;
		public static final int reference = 50;
		public static final int type = 51;
		public static final int type_part_list = 52;
		public static final int type_part = 53;
		public static final int pattern = 54;
		public static final int lexer_parts = 55;
		public static final int lexer_part = 56;
		public static final int lexem_attrs = 57;
		public static final int lexem_attribute = 58;
		public static final int icon_list = 59;
		public static final int grammar_parts = 60;
		public static final int grammar_part = 61;
		public static final int priority_kw = 62;
		public static final int directive = 63;
		public static final int inputs = 64;
		public static final int inputref = 65;
		public static final int references = 66;
		public static final int rules = 67;
		public static final int rule_list = 68;
		public static final int rule0 = 69;
		public static final int ruleprefix = 70;
		public static final int ruleparts = 71;
		public static final int rulepart = 72;
		public static final int rulesymref = 73;
		public static final int ruleannotations = 74;
		public static final int annotations = 75;
		public static final int annotation_list = 76;
		public static final int annotation = 77;
		public static final int negative_la = 78;
		public static final int negative_la_clause = 79;
		public static final int expression = 80;
		public static final int expression_list = 81;
		public static final int map_entries = 82;
		public static final int map_separator = 83;
		public static final int name = 84;
		public static final int qualified_id = 85;
		public static final int rule_attrs = 86;
		public static final int command = 87;
		public static final int syntax_problem = 88;
		public static final int grammar_partsopt = 89;
		public static final int type_part_listopt = 90;
		public static final int typeopt = 91;
		public static final int iconopt = 92;
		public static final int lexem_attrsopt = 93;
		public static final int commandopt = 94;
		public static final int Lnoeoiopt = 95;
		public static final int rule_attrsopt = 96;
		public static final int map_entriesopt = 97;
		public static final int expression_listopt = 98;
	}

	public interface Rules {
		public static final int lexer_part_group_selector = 32;  // lexer_part ::= '[' icon_list ']'
		public static final int lexer_part_alias = 33;  // lexer_part ::= identifier '=' pattern
		public static final int grammar_part_directive = 56;  // grammar_part ::= directive
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
			case 6:  // option ::= identifier '=' expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
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
			case 32:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // lexer_part ::= identifier '=' pattern
				 lapg_gg.sym = new AstNamedPattern(((String)lapg_m[lapg_head - 2].sym), ((AstRegexp)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym), null, null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 43:  // lexer_part ::= symbol typeopt ':' pattern iconopt lexem_attrsopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 6].sym), ((String)lapg_m[lapg_head - 5].sym), ((AstRegexp)lapg_m[lapg_head - 3].sym), ((Integer)lapg_m[lapg_head - 2].sym), ((AstLexemAttrs)lapg_m[lapg_head - 1].sym), ((AstCode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.sym = ((AstLexemAttrs)lapg_m[lapg_head - 1].sym); 
				break;
			case 45:  // lexem_attribute ::= Lsoft
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // lexem_attribute ::= Lclass
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // lexem_attribute ::= Lspace
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // lexem_attribute ::= Llayout
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 49:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head].sym)); 
				break;
			case 50:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head - 1].sym).add(((Integer)lapg_m[lapg_head].sym)); 
				break;
			case 51:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 52:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 53:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 54:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), ((AstAnnotations)lapg_m[lapg_head - 5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 56:  // grammar_part ::= directive
				 lapg_gg.sym = lapg_m[lapg_head].sym; 
				break;
			case 60:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head - 2].sym), ((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 62:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 63:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head - 2].sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 66:  // inputref ::= reference Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 68:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head - 1].sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 70:  // rule_list ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 71:  // rule_list ::= rule_list '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head - 2].sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 74:  // rule0 ::= ruleprefix ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 2].sym), ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // rule0 ::= ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // rule0 ::= ruleprefix rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 1].sym), null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // rule0 ::= rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 79:  // ruleprefix ::= annotations ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head - 1].sym), null); 
				break;
			case 80:  // ruleprefix ::= ruleannotations identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstRuleAnnotations)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 81:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 82:  // ruleparts ::= rulepart
				 lapg_gg.sym = new ArrayList<AstRulePart>(); ((List<AstRulePart>)lapg_gg.sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 83:  // ruleparts ::= ruleparts rulepart
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 84:  // ruleparts ::= ruleparts syntax_problem
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 85:  // rulepart ::= ruleannotations identifier '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rulepart ::= ruleannotations rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // rulepart ::= identifier '=' rulesymref
				 lapg_gg.sym = new AstRefRulePart(((String)lapg_m[lapg_head - 2].sym), ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // rulepart ::= rulesymref
				 lapg_gg.sym = new AstRefRulePart(null, ((AstRuleSymbolRef)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 90:  // rulepart ::= rulepart '&' rulepart
				 lapg_gg.sym = new AstUnorderedRulePart(((AstRulePart)lapg_m[lapg_head - 2].sym), ((AstRulePart)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 91:  // rulesymref ::= reference
				 lapg_gg.sym = new AstRuleDefaultSymbolRef(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 92:  // rulesymref ::= '(' rules ')'
				 lapg_gg.sym = new AstRuleNestedNonTerm(((List<AstRule>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // rulesymref ::= '(' ruleparts Lseparator references ')' '+'
				 lapg_gg.sym = new AstRuleNestedListWithSeparator(((List<AstRulePart>)lapg_m[lapg_head - 4].sym), ((List<AstReference>)lapg_m[lapg_head - 2].sym), true, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // rulesymref ::= '(' ruleparts Lseparator references ')' '*'
				 lapg_gg.sym = new AstRuleNestedListWithSeparator(((List<AstRulePart>)lapg_m[lapg_head - 4].sym), ((List<AstReference>)lapg_m[lapg_head - 2].sym), false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 95:  // rulesymref ::= rulesymref '?'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_OPTIONAL, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 96:  // rulesymref ::= rulesymref '*'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ZEROORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // rulesymref ::= rulesymref '+'
				 lapg_gg.sym = new AstRuleNestedQuantifier(((AstRuleSymbolRef)lapg_m[lapg_head - 1].sym), AstRuleNestedQuantifier.KIND_ONEORMORE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // ruleannotations ::= annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // ruleannotations ::= negative_la annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head - 1].sym), ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // ruleannotations ::= negative_la
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 101:  // annotations ::= annotation_list
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // annotation_list ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 103:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 104:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 105:  // annotation ::= '@' identifier '=' expression
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 107:  // negative_la ::= '(?!' negative_la_clause ')'
				 lapg_gg.sym = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 108:  // negative_la_clause ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 109:  // negative_la_clause ::= negative_la_clause '|' reference
				 ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 110:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 113:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // expression ::= Lnew name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head - 3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 123:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 124:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 125:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 129:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 2].sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 132:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 133:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 134:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 135:  // syntax_problem ::= error
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
		return (AstRoot) parse(lexer, 0, 210);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 211);
	}
}
