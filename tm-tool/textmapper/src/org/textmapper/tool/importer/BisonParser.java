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
		"\ufffd\uffff\uffff\uffff\uffff\uffff\37\0\uffff\uffff\uffff\uffff\uffff\uffff\54" +
		"\0\55\0\70\0\71\0\72\0\73\0\uffff\uffff\44\0\uffff\uffff\uffb1\uffff\16\0\uffff\uffff" +
		"\uffff\uffff\12\0\uffff\uffff\22\0\uffff\uffff\uffff\uffff\uffff\uffff\45\0\26\0" +
		"\27\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\34\0\uffff\uffff" +
		"\35\0\36\0\7\0\3\0\6\0\5\0\uffff\uffff\41\0\40\0\uff63\uffff\uff59\uffff\uff1f\uffff" +
		"\uffff\uffff\1\0\116\0\ufefd\uffff\ufea7\uffff\105\0\ufe51\uffff\62\0\ufdff\uffff" +
		"\uffff\uffff\uffff\uffff\46\0\137\0\140\0\ufdad\uffff\15\0\17\0\20\0\21\0\23\0\24" +
		"\0\25\0\30\0\11\0\ufd5b\uffff\32\0\33\0\144\0\145\0\146\0\43\0\uffff\uffff\51\0\uffff" +
		"\uffff\74\0\uffff\uffff\uffff\uffff\ufd0d\uffff\0\0\117\0\ufcd5\uffff\110\0\ufc81" +
		"\uffff\114\0\61\0\ufc2d\uffff\64\0\47\0\141\0\142\0\143\0\147\0\13\0\10\0\50\0\103" +
		"\0\104\0\102\0\ufbdb\uffff\53\0\101\0\100\0\ufb83\uffff\67\0\ufb31\uffff\uffff\uffff" +
		"\ufadd\uffff\ufaa5\uffff\106\0\112\0\63\0\52\0\66\0\76\0\135\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\131\0\ufa7f\uffff\130\0\121\0\ufa45\uffff\ufa0b\uffff\125\0\132" +
		"\0\133\0\134\0\127\0\126\0\uf9d3\uffff\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = BisonLexer.unpack_short(1634,
		"\11\4\13\4\20\4\21\4\22\4\23\4\24\4\25\4\26\4\27\4\30\4\34\4\35\4\36\4\37\4\41\4" +
		"\42\4\43\4\44\4\45\4\46\4\47\4\50\4\51\4\52\4\53\4\54\4\55\4\56\4\57\4\60\4\61\4" +
		"\62\4\63\4\64\4\65\4\70\4\uffff\ufffe\6\uffff\11\14\13\14\20\14\21\14\22\14\23\14" +
		"\24\14\25\14\26\14\27\14\30\14\34\14\35\14\36\14\37\14\41\14\42\14\43\14\44\14\45" +
		"\14\46\14\47\14\50\14\51\14\52\14\53\14\54\14\55\14\56\14\57\14\60\14\61\14\62\14" +
		"\63\14\64\14\65\14\70\14\uffff\ufffe\73\uffff\2\75\5\75\6\75\uffff\ufffe\14\uffff" +
		"\0\136\1\136\2\136\5\136\6\136\12\136\13\136\20\136\21\136\22\136\23\136\24\136\25" +
		"\136\26\136\27\136\30\136\31\136\32\136\33\136\34\136\35\136\40\136\52\136\61\136" +
		"\63\136\66\136\67\136\uffff\ufffe\1\uffff\20\uffff\21\uffff\22\uffff\23\uffff\24" +
		"\uffff\25\uffff\26\uffff\27\uffff\30\uffff\34\uffff\35\uffff\52\uffff\61\uffff\63" +
		"\uffff\0\2\uffff\ufffe\4\uffff\6\uffff\2\111\5\111\11\111\13\111\20\111\21\111\22" +
		"\111\23\111\24\111\25\111\26\111\27\111\30\111\34\111\35\111\36\111\37\111\41\111" +
		"\42\111\43\111\44\111\45\111\46\111\47\111\50\111\51\111\52\111\53\111\54\111\55" +
		"\111\56\111\57\111\60\111\61\111\62\111\63\111\64\111\65\111\70\111\73\111\uffff" +
		"\ufffe\4\uffff\6\uffff\2\115\5\115\11\115\13\115\20\115\21\115\22\115\23\115\24\115" +
		"\25\115\26\115\27\115\30\115\34\115\35\115\36\115\37\115\41\115\42\115\43\115\44" +
		"\115\45\115\46\115\47\115\50\115\51\115\52\115\53\115\54\115\55\115\56\115\57\115" +
		"\60\115\61\115\62\115\63\115\64\115\65\115\70\115\73\115\uffff\ufffe\2\uffff\5\uffff" +
		"\73\uffff\11\57\13\57\20\57\21\57\22\57\23\57\24\57\25\57\26\57\27\57\30\57\34\57" +
		"\35\57\36\57\37\57\41\57\42\57\43\57\44\57\45\57\46\57\47\57\50\57\51\57\52\57\53" +
		"\57\54\57\55\57\56\57\57\57\60\57\61\57\62\57\63\57\64\57\65\57\70\57\uffff\ufffe" +
		"\2\uffff\5\uffff\73\uffff\11\56\13\56\20\56\21\56\22\56\23\56\24\56\25\56\26\56\27" +
		"\56\30\56\34\56\35\56\36\56\37\56\41\56\42\56\43\56\44\56\45\56\46\56\47\56\50\56" +
		"\51\56\52\56\53\56\54\56\55\56\56\56\57\56\60\56\61\56\62\56\63\56\64\56\65\56\70" +
		"\56\uffff\ufffe\2\uffff\6\uffff\66\uffff\11\150\13\150\20\150\21\150\22\150\23\150" +
		"\24\150\25\150\26\150\27\150\30\150\34\150\35\150\36\150\37\150\41\150\42\150\43" +
		"\150\44\150\45\150\46\150\47\150\50\150\51\150\52\150\53\150\54\150\55\150\56\150" +
		"\57\150\60\150\61\150\62\150\63\150\64\150\65\150\70\150\uffff\ufffe\66\uffff\11" +
		"\31\13\31\20\31\21\31\22\31\23\31\24\31\25\31\26\31\27\31\30\31\34\31\35\31\36\31" +
		"\37\31\41\31\42\31\43\31\44\31\45\31\46\31\47\31\50\31\51\31\52\31\53\31\54\31\55" +
		"\31\56\31\57\31\60\31\61\31\62\31\63\31\64\31\65\31\70\31\uffff\ufffe\0\122\1\122" +
		"\2\122\5\122\6\122\12\122\13\122\20\122\21\122\22\122\23\122\24\122\25\122\26\122" +
		"\27\122\30\122\31\122\32\122\33\122\34\122\35\122\40\122\52\122\61\122\63\122\66" +
		"\122\67\122\uffff\ufffe\6\uffff\2\107\5\107\11\107\13\107\20\107\21\107\22\107\23" +
		"\107\24\107\25\107\26\107\27\107\30\107\34\107\35\107\36\107\37\107\41\107\42\107" +
		"\43\107\44\107\45\107\46\107\47\107\50\107\51\107\52\107\53\107\54\107\55\107\56" +
		"\107\57\107\60\107\61\107\62\107\63\107\64\107\65\107\70\107\73\107\uffff\ufffe\6" +
		"\uffff\2\113\5\113\11\113\13\113\20\113\21\113\22\113\23\113\24\113\25\113\26\113" +
		"\27\113\30\113\34\113\35\113\36\113\37\113\41\113\42\113\43\113\44\113\45\113\46" +
		"\113\47\113\50\113\51\113\52\113\53\113\54\113\55\113\56\113\57\113\60\113\61\113" +
		"\62\113\63\113\64\113\65\113\70\113\73\113\uffff\ufffe\2\uffff\5\uffff\6\uffff\11" +
		"\60\13\60\20\60\21\60\22\60\23\60\24\60\25\60\26\60\27\60\30\60\34\60\35\60\36\60" +
		"\37\60\41\60\42\60\43\60\44\60\45\60\46\60\47\60\50\60\51\60\52\60\53\60\54\60\55" +
		"\60\56\60\57\60\60\60\61\60\62\60\63\60\64\60\65\60\70\60\uffff\ufffe\2\uffff\5\uffff" +
		"\6\uffff\7\uffff\10\uffff\73\uffff\11\42\13\42\20\42\21\42\22\42\23\42\24\42\25\42" +
		"\26\42\27\42\30\42\34\42\35\42\36\42\37\42\41\42\42\42\43\42\44\42\45\42\46\42\47" +
		"\42\50\42\51\42\52\42\53\42\54\42\55\42\56\42\57\42\60\42\61\42\62\42\63\42\64\42" +
		"\65\42\70\42\uffff\ufffe\2\uffff\5\uffff\6\uffff\11\65\13\65\20\65\21\65\22\65\23" +
		"\65\24\65\25\65\26\65\27\65\30\65\34\65\35\65\36\65\37\65\41\65\42\65\43\65\44\65" +
		"\45\65\46\65\47\65\50\65\51\65\52\65\53\65\54\65\55\65\56\65\57\65\60\65\61\65\62" +
		"\65\63\65\64\65\65\65\70\65\uffff\ufffe\4\uffff\2\77\5\77\6\77\11\77\13\77\20\77" +
		"\21\77\22\77\23\77\24\77\25\77\26\77\27\77\30\77\34\77\35\77\36\77\37\77\41\77\42" +
		"\77\43\77\44\77\45\77\46\77\47\77\50\77\51\77\52\77\53\77\54\77\55\77\56\77\57\77" +
		"\60\77\61\77\62\77\63\77\64\77\65\77\70\77\uffff\ufffe\2\uffff\5\uffff\6\uffff\31" +
		"\uffff\32\uffff\33\uffff\40\uffff\66\uffff\67\uffff\0\123\1\123\12\123\13\123\20" +
		"\123\21\123\22\123\23\123\24\123\25\123\26\123\27\123\30\123\34\123\35\123\52\123" +
		"\61\123\63\123\uffff\ufffe\12\uffff\13\uffff\0\120\1\120\20\120\21\120\22\120\23" +
		"\120\24\120\25\120\26\120\27\120\30\120\34\120\35\120\52\120\61\120\63\120\uffff" +
		"\ufffe\14\uffff\0\136\1\136\2\136\5\136\6\136\12\136\13\136\20\136\21\136\22\136" +
		"\23\136\24\136\25\136\26\136\27\136\30\136\31\136\32\136\33\136\34\136\35\136\40" +
		"\136\52\136\61\136\63\136\66\136\67\136\uffff\ufffe\14\uffff\0\136\1\136\2\136\5" +
		"\136\6\136\12\136\13\136\20\136\21\136\22\136\23\136\24\136\25\136\26\136\27\136" +
		"\30\136\31\136\32\136\33\136\34\136\35\136\40\136\52\136\61\136\63\136\66\136\67" +
		"\136\uffff\ufffe\0\122\1\122\2\122\5\122\6\122\12\122\13\122\20\122\21\122\22\122" +
		"\23\122\24\122\25\122\26\122\27\122\30\122\31\122\32\122\33\122\34\122\35\122\40" +
		"\122\52\122\61\122\63\122\66\122\67\122\uffff\ufffe\2\uffff\5\uffff\6\uffff\31\uffff" +
		"\32\uffff\33\uffff\40\uffff\66\uffff\67\uffff\0\124\1\124\12\124\13\124\20\124\21" +
		"\124\22\124\23\124\24\124\25\124\26\124\27\124\30\124\34\124\35\124\52\124\61\124" +
		"\63\124\uffff\ufffe");

	private static final short[] lapg_sym_goto = BisonLexer.unpack_short(99,
		"\0\1\3\26\26\34\52\101\103\105\106\107\112\115\116\116\116\121\124\127\132\135\140" +
		"\143\146\151\153\155\157\162\165\166\167\171\172\173\174\175\176\177\200\201\202" +
		"\205\206\207\210\211\212\213\216\217\222\223\224\237\241\242\242\242\253\253\253" +
		"\253\253\253\253\253\253\254\255\256\257\260\261\264\265\270\273\275\276\301\302" +
		"\305\306\310\312\314\320\322\324\326\327\331\334\335\336\350\351");

	private static final short[] lapg_sym_from = BisonLexer.unpack_short(233,
		"\213\2\57\4\5\15\17\41\43\66\70\71\76\121\123\124\135\152\156\162\173\212\22\23\63" +
		"\64\160\174\4\5\41\66\70\71\121\123\135\152\156\162\173\212\17\20\25\30\31\35\37" +
		"\40\41\63\64\71\76\121\123\130\132\135\152\156\162\173\212\121\152\121\152\1\163" +
		"\1\60\163\56\177\202\161\1\2\57\1\2\57\1\2\57\1\2\57\1\2\57\1\2\57\1\2\57\1\2\57" +
		"\1\2\57\162\212\162\212\162\212\1\2\57\1\2\57\1\1\162\212\1\1\1\1\1\1\1\1\1\1\2\57" +
		"\1\1\1\1\1\1\1\2\57\1\1\2\57\1\1\15\27\36\43\52\72\76\110\117\162\212\162\212\1\4" +
		"\5\6\55\66\70\121\152\175\2\0\0\1\36\1\1\2\57\121\1\2\57\1\2\57\4\5\71\1\2\57\123" +
		"\1\2\57\55\123\156\121\152\121\152\4\5\66\70\2\57\2\57\125\203\125\162\212\56\177" +
		"\202\17\76\41\71\121\123\135\152\156\162\173\212\76");

	private static final short[] lapg_sym_to = BisonLexer.unpack_short(233,
		"\214\56\56\63\63\72\74\113\117\63\63\113\140\113\113\161\113\113\113\113\113\113" +
		"\100\101\130\132\171\206\64\64\114\64\64\114\114\114\114\114\114\114\114\114\75\77" +
		"\102\104\105\106\111\112\115\131\133\115\141\115\115\164\165\115\115\115\115\115" +
		"\115\147\147\150\150\2\203\3\127\204\124\124\124\172\4\4\4\5\5\5\6\6\6\7\7\7\10\10" +
		"\10\11\11\11\12\12\12\13\13\13\14\14\14\173\173\174\174\175\175\15\15\15\16\16\16" +
		"\17\20\176\176\21\22\23\24\25\26\27\30\31\32\32\32\33\34\35\36\37\40\41\41\41\42" +
		"\43\43\43\44\45\73\103\107\120\121\137\142\145\146\177\177\200\200\46\65\65\71\122" +
		"\65\65\151\151\207\57\213\1\47\110\50\51\60\60\152\52\52\52\53\53\53\66\70\135\54" +
		"\54\54\156\55\55\55\123\157\170\153\167\154\154\67\67\134\134\61\126\62\62\162\212" +
		"\163\201\201\125\210\211\76\143\116\136\155\160\166\155\160\202\205\202\144");

	private static final short[] tmRuleLen = BisonLexer.unpack_short(105,
		"\2\1\3\2\0\1\1\1\2\1\1\3\1\2\1\2\2\2\1\2\2\2\1\1\2\2\2\2\1\1\1\1\1\1\3\2\1\1\2\3" +
		"\3\2\2\1\1\1\2\2\3\2\1\2\1\3\2\1\1\1\1\1\1\0\2\1\1\1\1\1\1\1\3\2\2\1\3\2\2\1\1\2" +
		"\3\2\0\1\3\2\2\2\1\1\2\2\2\3\0\1\1\1\1\1\1\1\1\1\0");

	private static final short[] tmRuleSymbol = BisonLexer.unpack_short(105,
		"\104\104\105\106\106\107\107\107\110\110\111\111\111\111\111\111\111\111\111\111" +
		"\111\111\111\111\111\111\111\111\111\111\111\111\112\112\112\112\112\112\112\112" +
		"\112\112\113\113\114\114\115\115\115\116\116\117\117\120\121\121\122\122\122\122" +
		"\123\123\124\124\125\125\126\126\126\127\127\127\127\127\127\127\127\127\130\130" +
		"\131\132\132\133\133\133\134\134\134\134\134\134\134\135\135\136\136\137\137\137" +
		"\140\140\140\141\141");

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
		"grammar_part_list",
		"input",
		"prologue_declaration_optlist",
		"prologue_declaration",
		"'{...}'_list",
		"prologue_directive",
		"grammar_declaration",
		"symbol_or_tag_list",
		"code_props_type",
		"symbol_declaration",
		"symbol_def_list",
		"symbol_list",
		"prec_declaration",
		"symbol_prec_list",
		"prec_directive",
		"tag_op",
		"symbol_prec",
		"symbol_or_tag",
		"tag",
		"symbol_def",
		"grammar_part",
		"nonterm_rules",
		"rhsPart_optlist",
		"rules",
		"rhsPart",
		"named_ref_op",
		"variable",
		"value",
		"symbol",
		"valueopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int grammar_part_list = 68;
		int input = 69;
		int prologue_declaration_optlist = 70;
		int prologue_declaration = 71;
		int ApostropheLcurlyDotDotDotRcurlyApostrophe_list = 72;
		int prologue_directive = 73;
		int grammar_declaration = 74;
		int symbol_or_tag_list = 75;
		int code_props_type = 76;
		int symbol_declaration = 77;
		int symbol_def_list = 78;
		int symbol_list = 79;
		int prec_declaration = 80;
		int symbol_prec_list = 81;
		int prec_directive = 82;
		int tag_op = 83;
		int symbol_prec = 84;
		int symbol_or_tag = 85;
		int tag = 86;
		int symbol_def = 87;
		int grammar_part = 88;
		int nonterm_rules = 89;
		int rhsPart_optlist = 90;
		int rules = 91;
		int rhsPart = 92;
		int named_ref_op = 93;
		int variable = 94;
		int value = 95;
		int symbol = 96;
		int valueopt = 97;
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
