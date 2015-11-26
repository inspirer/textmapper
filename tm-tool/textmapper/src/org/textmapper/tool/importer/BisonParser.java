/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.tool.importer;

import java.io.IOException;
import java.text.MessageFormat;
import org.textmapper.tool.importer.BisonLexer.ErrorReporter;
import org.textmapper.tool.importer.BisonLexer.Span;
import org.textmapper.tool.importer.BisonLexer.Tokens;

public class BisonParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public BisonParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] tmAction = BisonLexer.unpack_int(141,
		"\ufffd\uffff\uffff\uffff\uffff\uffff\31\0\uffff\uffff\uffff\uffff\uffff\uffff\44" +
		"\0\45\0\52\0\53\0\54\0\55\0\uffff\uffff\36\0\uffff\uffff\uffb1\uffff\10\0\uffff\uffff" +
		"\uffff\uffff\4\0\uffff\uffff\14\0\uffff\uffff\uffff\uffff\uffff\uffff\37\0\20\0\21" +
		"\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\26\0\uffff\uffff\27" +
		"\0\30\0\3\0\127\0\2\0\1\0\uffff\uffff\33\0\32\0\uff63\uffff\uff59\uffff\uffff\uffff" +
		"\132\0\100\0\uff1f\uffff\ufefd\uffff\ufea7\uffff\67\0\142\0\ufe51\uffff\ufdff\uffff" +
		"\uffff\uffff\uffff\uffff\40\0\117\0\120\0\ufdad\uffff\7\0\11\0\12\0\13\0\15\0\16" +
		"\0\17\0\22\0\136\0\ufd5b\uffff\24\0\25\0\124\0\125\0\126\0\35\0\uffff\uffff\43\0" +
		"\uffff\uffff\56\0\uffff\uffff\uffff\uffff\ufd0d\uffff\101\0\131\0\ufcd5\uffff\72" +
		"\0\ufc81\uffff\76\0\141\0\144\0\ufc2d\uffff\41\0\121\0\122\0\123\0\133\0\5\0\135" +
		"\0\42\0\65\0\66\0\64\0\140\0\63\0\62\0\ufbdb\uffff\146\0\ufb83\uffff\ufb2f\uffff" +
		"\uffff\uffff\ufadd\uffff\ufab7\uffff\70\0\74\0\143\0\137\0\60\0\145\0\115\0\ufa7f" +
		"\uffff\105\0\uffff\uffff\uffff\uffff\uffff\uffff\111\0\ufa47\uffff\110\0\147\0\ufa0d" +
		"\uffff\uf9d3\uffff\112\0\113\0\114\0\107\0\106\0\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = BisonLexer.unpack_short(1634,
		"\11\130\13\130\20\130\21\130\22\130\23\130\24\130\25\130\26\130\27\130\30\130\34" +
		"\130\35\130\36\130\37\130\41\130\42\130\43\130\44\130\45\130\46\130\47\130\50\130" +
		"\51\130\52\130\53\130\54\130\55\130\56\130\57\130\60\130\61\130\62\130\63\130\64" +
		"\130\65\130\70\130\uffff\ufffe\6\uffff\11\6\13\6\20\6\21\6\22\6\23\6\24\6\25\6\26" +
		"\6\27\6\30\6\34\6\35\6\36\6\37\6\41\6\42\6\43\6\44\6\45\6\46\6\47\6\50\6\51\6\52" +
		"\6\53\6\54\6\55\6\56\6\57\6\60\6\61\6\62\6\63\6\64\6\65\6\70\6\uffff\ufffe\73\uffff" +
		"\2\57\5\57\6\57\uffff\ufffe\14\uffff\0\116\1\116\2\116\5\116\6\116\12\116\13\116" +
		"\20\116\21\116\22\116\23\116\24\116\25\116\26\116\27\116\30\116\31\116\32\116\33" +
		"\116\34\116\35\116\40\116\52\116\61\116\63\116\66\116\67\116\uffff\ufffe\1\uffff" +
		"\20\uffff\21\uffff\22\uffff\23\uffff\24\uffff\25\uffff\26\uffff\27\uffff\30\uffff" +
		"\34\uffff\35\uffff\52\uffff\61\uffff\63\uffff\0\0\uffff\ufffe\4\uffff\6\uffff\2\73" +
		"\5\73\11\73\13\73\20\73\21\73\22\73\23\73\24\73\25\73\26\73\27\73\30\73\34\73\35" +
		"\73\36\73\37\73\41\73\42\73\43\73\44\73\45\73\46\73\47\73\50\73\51\73\52\73\53\73" +
		"\54\73\55\73\56\73\57\73\60\73\61\73\62\73\63\73\64\73\65\73\70\73\73\73\uffff\ufffe" +
		"\4\uffff\6\uffff\2\77\5\77\11\77\13\77\20\77\21\77\22\77\23\77\24\77\25\77\26\77" +
		"\27\77\30\77\34\77\35\77\36\77\37\77\41\77\42\77\43\77\44\77\45\77\46\77\47\77\50" +
		"\77\51\77\52\77\53\77\54\77\55\77\56\77\57\77\60\77\61\77\62\77\63\77\64\77\65\77" +
		"\70\77\73\77\uffff\ufffe\2\uffff\5\uffff\73\uffff\11\47\13\47\20\47\21\47\22\47\23" +
		"\47\24\47\25\47\26\47\27\47\30\47\34\47\35\47\36\47\37\47\41\47\42\47\43\47\44\47" +
		"\45\47\46\47\47\47\50\47\51\47\52\47\53\47\54\47\55\47\56\47\57\47\60\47\61\47\62" +
		"\47\63\47\64\47\65\47\70\47\uffff\ufffe\2\uffff\5\uffff\73\uffff\11\46\13\46\20\46" +
		"\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\34\46\35\46\36\46\37\46\41\46\42" +
		"\46\43\46\44\46\45\46\46\46\47\46\50\46\51\46\52\46\53\46\54\46\55\46\56\46\57\46" +
		"\60\46\61\46\62\46\63\46\64\46\65\46\70\46\uffff\ufffe\2\uffff\6\uffff\66\uffff\11" +
		"\134\13\134\20\134\21\134\22\134\23\134\24\134\25\134\26\134\27\134\30\134\34\134" +
		"\35\134\36\134\37\134\41\134\42\134\43\134\44\134\45\134\46\134\47\134\50\134\51" +
		"\134\52\134\53\134\54\134\55\134\56\134\57\134\60\134\61\134\62\134\63\134\64\134" +
		"\65\134\70\134\uffff\ufffe\66\uffff\11\23\13\23\20\23\21\23\22\23\23\23\24\23\25" +
		"\23\26\23\27\23\30\23\34\23\35\23\36\23\37\23\41\23\42\23\43\23\44\23\45\23\46\23" +
		"\47\23\50\23\51\23\52\23\53\23\54\23\55\23\56\23\57\23\60\23\61\23\62\23\63\23\64" +
		"\23\65\23\70\23\uffff\ufffe\0\150\1\150\2\150\5\150\6\150\12\150\13\150\20\150\21" +
		"\150\22\150\23\150\24\150\25\150\26\150\27\150\30\150\31\150\32\150\33\150\34\150" +
		"\35\150\40\150\52\150\61\150\63\150\66\150\67\150\uffff\ufffe\6\uffff\2\71\5\71\11" +
		"\71\13\71\20\71\21\71\22\71\23\71\24\71\25\71\26\71\27\71\30\71\34\71\35\71\36\71" +
		"\37\71\41\71\42\71\43\71\44\71\45\71\46\71\47\71\50\71\51\71\52\71\53\71\54\71\55" +
		"\71\56\71\57\71\60\71\61\71\62\71\63\71\64\71\65\71\70\71\73\71\uffff\ufffe\6\uffff" +
		"\2\75\5\75\11\75\13\75\20\75\21\75\22\75\23\75\24\75\25\75\26\75\27\75\30\75\34\75" +
		"\35\75\36\75\37\75\41\75\42\75\43\75\44\75\45\75\46\75\47\75\50\75\51\75\52\75\53" +
		"\75\54\75\55\75\56\75\57\75\60\75\61\75\62\75\63\75\64\75\65\75\70\75\73\75\uffff" +
		"\ufffe\2\uffff\5\uffff\6\uffff\11\50\13\50\20\50\21\50\22\50\23\50\24\50\25\50\26" +
		"\50\27\50\30\50\34\50\35\50\36\50\37\50\41\50\42\50\43\50\44\50\45\50\46\50\47\50" +
		"\50\50\51\50\52\50\53\50\54\50\55\50\56\50\57\50\60\50\61\50\62\50\63\50\64\50\65" +
		"\50\70\50\uffff\ufffe\2\uffff\5\uffff\6\uffff\7\uffff\10\uffff\73\uffff\11\34\13" +
		"\34\20\34\21\34\22\34\23\34\24\34\25\34\26\34\27\34\30\34\34\34\35\34\36\34\37\34" +
		"\41\34\42\34\43\34\44\34\45\34\46\34\47\34\50\34\51\34\52\34\53\34\54\34\55\34\56" +
		"\34\57\34\60\34\61\34\62\34\63\34\64\34\65\34\70\34\uffff\ufffe\4\uffff\2\61\5\61" +
		"\6\61\11\61\13\61\20\61\21\61\22\61\23\61\24\61\25\61\26\61\27\61\30\61\34\61\35" +
		"\61\36\61\37\61\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61" +
		"\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\70\61\uffff\ufffe\2" +
		"\uffff\5\uffff\6\uffff\11\51\13\51\20\51\21\51\22\51\23\51\24\51\25\51\26\51\27\51" +
		"\30\51\34\51\35\51\36\51\37\51\41\51\42\51\43\51\44\51\45\51\46\51\47\51\50\51\51" +
		"\51\52\51\53\51\54\51\55\51\56\51\57\51\60\51\61\51\62\51\63\51\64\51\65\51\70\51" +
		"\uffff\ufffe\12\uffff\13\uffff\0\102\1\102\20\102\21\102\22\102\23\102\24\102\25" +
		"\102\26\102\27\102\30\102\34\102\35\102\52\102\61\102\63\102\uffff\ufffe\2\uffff" +
		"\5\uffff\6\uffff\31\uffff\32\uffff\33\uffff\40\uffff\66\uffff\67\uffff\0\103\1\103" +
		"\12\103\13\103\20\103\21\103\22\103\23\103\24\103\25\103\26\103\27\103\30\103\34" +
		"\103\35\103\52\103\61\103\63\103\uffff\ufffe\0\150\1\150\2\150\5\150\6\150\12\150" +
		"\13\150\20\150\21\150\22\150\23\150\24\150\25\150\26\150\27\150\30\150\31\150\32" +
		"\150\33\150\34\150\35\150\40\150\52\150\61\150\63\150\66\150\67\150\uffff\ufffe\14" +
		"\uffff\0\116\1\116\2\116\5\116\6\116\12\116\13\116\20\116\21\116\22\116\23\116\24" +
		"\116\25\116\26\116\27\116\30\116\31\116\32\116\33\116\34\116\35\116\40\116\52\116" +
		"\61\116\63\116\66\116\67\116\uffff\ufffe\14\uffff\0\116\1\116\2\116\5\116\6\116\12" +
		"\116\13\116\20\116\21\116\22\116\23\116\24\116\25\116\26\116\27\116\30\116\31\116" +
		"\32\116\33\116\34\116\35\116\40\116\52\116\61\116\63\116\66\116\67\116\uffff\ufffe" +
		"\2\uffff\5\uffff\6\uffff\31\uffff\32\uffff\33\uffff\40\uffff\66\uffff\67\uffff\0" +
		"\104\1\104\12\104\13\104\20\104\21\104\22\104\23\104\24\104\25\104\26\104\27\104" +
		"\30\104\34\104\35\104\52\104\61\104\63\104\uffff\ufffe");

	private static final short[] lapg_sym_goto = BisonLexer.unpack_short(99,
		"\0\1\3\26\26\34\52\101\103\105\106\107\112\115\116\116\116\121\124\127\132\135\140" +
		"\143\146\151\153\155\157\162\165\166\167\171\172\173\174\175\176\177\200\201\202" +
		"\205\206\207\210\211\212\213\216\217\222\223\224\237\241\242\242\242\253\253\253" +
		"\253\253\253\253\253\253\254\255\256\261\264\267\272\275\276\300\302\304\310\312" +
		"\314\315\317\322\323\324\336\337\340\341\342\343\345\346\347\351");

	private static final short[] lapg_sym_from = BisonLexer.unpack_short(233,
		"\213\2\62\4\5\15\17\41\43\67\70\71\76\121\123\124\136\155\160\163\175\205\22\23\63" +
		"\64\157\176\4\5\41\67\70\71\121\123\136\155\160\163\175\205\17\20\25\30\31\35\37" +
		"\40\41\63\64\71\76\121\123\130\132\136\155\160\163\175\205\121\155\121\155\1\162" +
		"\1\57\162\56\201\204\161\1\2\62\1\2\62\1\2\62\1\2\62\1\2\62\1\2\62\1\2\62\1\2\62" +
		"\1\2\62\163\205\163\205\163\205\1\2\62\1\2\62\1\1\163\205\1\1\1\1\1\1\1\1\1\1\2\62" +
		"\1\1\1\1\1\1\1\2\62\1\1\2\62\1\1\15\27\36\43\52\72\76\110\117\163\205\163\205\1\4" +
		"\5\6\55\67\70\121\155\177\0\1\1\1\2\62\1\2\62\1\2\62\1\2\62\1\2\62\55\123\160\121" +
		"\155\121\155\4\5\67\70\2\62\2\62\125\163\205\56\201\204\17\76\41\71\121\123\136\155" +
		"\160\163\175\205\0\2\76\36\121\4\5\71\123\125\173");

	private static final short[] lapg_sym_to = BisonLexer.unpack_short(233,
		"\214\56\56\63\63\72\74\113\117\63\63\113\140\113\113\161\113\113\113\113\113\113" +
		"\100\101\130\132\170\207\64\64\114\64\64\114\114\114\114\114\114\114\114\114\75\77" +
		"\102\104\105\106\111\112\115\131\133\115\141\115\115\164\165\115\115\115\115\115" +
		"\115\147\147\150\150\2\173\3\126\174\124\124\124\172\4\4\4\5\5\5\6\6\6\7\7\7\10\10" +
		"\10\11\11\11\12\12\12\13\13\13\14\14\14\175\175\176\176\177\177\15\15\15\16\16\16" +
		"\17\20\200\200\21\22\23\24\25\26\27\30\31\32\32\32\33\34\35\36\37\40\41\41\41\42" +
		"\43\43\43\44\45\73\103\107\120\121\137\142\145\146\201\201\202\202\46\65\65\71\122" +
		"\65\65\151\151\210\213\47\50\51\57\57\52\52\52\53\53\53\54\54\54\55\55\55\123\156" +
		"\171\152\167\153\153\66\66\134\134\60\127\61\61\162\203\203\125\211\212\76\143\116" +
		"\135\154\157\166\154\157\204\206\204\1\62\144\110\155\67\70\136\160\163\205");

	private static final short[] tmRuleLen = BisonLexer.unpack_short(105,
		"\3\1\1\1\1\3\1\2\1\2\2\2\1\2\2\2\1\1\2\2\2\2\1\1\1\1\1\1\3\2\1\1\2\3\3\2\1\1\2\2" +
		"\3\3\1\1\1\1\1\0\2\1\1\1\1\1\1\1\3\2\2\1\3\2\2\1\1\2\3\1\3\2\2\2\1\1\2\2\2\3\0\1" +
		"\1\1\1\1\1\1\1\2\0\2\1\1\0\2\1\2\1\2\1\2\1\2\1\2\0");

	private static final short[] tmRuleSymbol = BisonLexer.unpack_short(105,
		"\104\105\105\105\106\106\106\106\106\106\106\106\106\106\106\106\106\106\106\106" +
		"\106\106\106\106\106\106\107\107\107\107\107\107\107\107\107\107\110\110\111\111" +
		"\111\112\113\113\113\113\114\114\115\115\116\116\117\117\117\120\120\120\120\120" +
		"\120\120\120\120\121\121\122\123\123\123\124\124\124\124\124\124\124\125\125\126" +
		"\126\127\127\127\130\130\130\131\131\132\132\133\133\134\134\135\135\136\136\137" +
		"\137\140\140\141\141");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"ID_COLON",
		"ID",
		"skip",
		"INT",
		"CHAR",
		"STRING",
		"'<*>'",
		"'<>'",
		"'%%'",
		"'|'",
		"';'",
		"'['",
		"']'",
		"skip_comment",
		"skip_ml_comment",
		"'%token'",
		"'%nterm'",
		"'%type'",
		"'%destructor'",
		"'%printer'",
		"'%left'",
		"'%right'",
		"'%nonassoc'",
		"'%precedence'",
		"'%prec'",
		"'%dprec'",
		"'%merge'",
		"'%code'",
		"'%default-prec'",
		"'%define'",
		"'%defines'",
		"'%empty'",
		"'%error-verbose'",
		"'%expect'",
		"'%expect-rr'",
		"'%<flag>'",
		"'%file-prefix'",
		"'%glr-parser'",
		"'%initial-action'",
		"'%language'",
		"'%name-prefix'",
		"'%no-default-prec'",
		"'%no-lines'",
		"'%nondeterministic-parser'",
		"'%output'",
		"'%param'",
		"'%require'",
		"'%skeleton'",
		"'%start'",
		"'%token-table'",
		"'%union'",
		"'%verbose'",
		"'%yacc'",
		"'{...}'",
		"'%?{...}'",
		"'%{...%}'",
		"tag_any",
		"tag_inc_nesting",
		"TAG",
		"code_char",
		"code_string",
		"code_comment",
		"code_ml_comment",
		"code_any",
		"code_inc_nesting",
		"code_dec_nesting",
		"code_lessless",
		"input",
		"prologue_declaration",
		"prologue_directive",
		"grammar_declaration",
		"code_props_type",
		"symbol_declaration",
		"prec_declaration",
		"prec_directive",
		"tag_op",
		"symbol_prec",
		"symbol_or_tag",
		"tag",
		"symbol_def",
		"grammar_part",
		"nonterm_rules",
		"rules",
		"rhsPart",
		"named_ref_op",
		"variable",
		"value",
		"symbol",
		"prologue_declaration_optlist",
		"grammar_part_list",
		"valueopt",
		"'{...}'_list",
		"symbol_or_tag_list",
		"symbol_def_list",
		"symbol_list",
		"symbol_prec_list",
		"rhsPart_optlist",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int input = 68;
		int prologue_declaration = 69;
		int prologue_directive = 70;
		int grammar_declaration = 71;
		int code_props_type = 72;
		int symbol_declaration = 73;
		int prec_declaration = 74;
		int prec_directive = 75;
		int tag_op = 76;
		int symbol_prec = 77;
		int symbol_or_tag = 78;
		int tag = 79;
		int symbol_def = 80;
		int grammar_part = 81;
		int nonterm_rules = 82;
		int rules = 83;
		int rhsPart = 84;
		int named_ref_op = 85;
		int variable = 86;
		int value = 87;
		int symbol = 88;
		int prologue_declaration_optlist = 89;
		int grammar_part_list = 90;
		int valueopt = 91;
		int ApostropheLcurlyDotDotDotRcurlyApostrophe_list = 92;
		int symbol_or_tag_list = 93;
		int symbol_def_list = 94;
		int symbol_list = 95;
		int symbol_prec_list = 96;
		int rhsPart_optlist = 97;
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
	protected Span[] tmStack;
	protected Span tmNext;
	protected BisonLexer tmLexer;

	public Object parse(BisonLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;

		tmStack[0] = new Span();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 140) {
			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				break;
			}
		}

		if (tmStack[tmHead].state != 140) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
			throw new ParseException();
		}
		return tmStack[tmHead - 1].value;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.tokenText()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		Span left = new Span();
		left.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		left.symbol = tmRuleSymbol[rule];
		left.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		Span startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		left.line = startsym.line;
		left.offset = startsym.offset;
		left.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(left, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = left;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
	}
}
