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
package org.textway.templates.types;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.templates.types.TypesLexer.ErrorReporter;
import org.textway.templates.types.TypesLexer.LapgSymbol;
import org.textway.templates.types.TypesLexer.Lexems;
import org.textway.templates.types.ast.AstConstraint;
import org.textway.templates.types.ast.AstFeatureDeclaration;
import org.textway.templates.types.ast.AstInput;
import org.textway.templates.types.ast.AstLiteralExpression;
import org.textway.templates.types.ast.AstMapEntriesItem;
import org.textway.templates.types.ast.AstMapSeparator;
import org.textway.templates.types.ast.AstMethodDeclaration;
import org.textway.templates.types.ast.AstMultiplicity;
import org.textway.templates.types.ast.AstStringConstraint;
import org.textway.templates.types.ast.AstStructuralExpression;
import org.textway.templates.types.ast.AstType;
import org.textway.templates.types.ast.AstTypeDeclaration;
import org.textway.templates.types.ast.AstTypeEx;
import org.textway.templates.types.ast.Ast_String;
import org.textway.templates.types.ast.IAstExpression;
import org.textway.templates.types.ast.IAstMemberDeclaration;

public class TypesParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public TypesParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] lapg_action = TypesLexer.unpack_int(110,
		"\uffff\uffff\uffff\uffff\ufffd\uffff\2\0\ufff7\uffff\1\0\uffff\uffff\4\0\uffff\uffff" +
		"\103\0\ufff1\uffff\uffe9\uffff\uffe3\uffff\uffff\uffff\uffff\uffff\uffd5\uffff\53" +
		"\0\55\0\54\0\uffc7\uffff\12\0\13\0\14\0\uffff\uffff\uffb9\uffff\uffad\uffff\uffff" +
		"\uffff\104\0\uff9d\uffff\uff95\uffff\25\0\uffff\uffff\11\0\uff8d\uffff\uffff\uffff" +
		"\57\0\7\0\uffff\uffff\uffff\uffff\uff83\uffff\uffff\uffff\16\0\uff75\uffff\uff6f" +
		"\uffff\uffff\uffff\44\0\26\0\60\0\uffff\uffff\37\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\32\0\33\0\uff65\uffff\uffff\uffff\20\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\52\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\30\0\63\0\64\0\65\0\uff5d\uffff" +
		"\27\0\62\0\61\0\uffff\uffff\21\0\50\0\47\0\45\0\24\0\42\0\43\0\uff4f\uffff\41\0\uff47" +
		"\uffff\31\0\74\0\uff3f\uffff\uffff\uffff\uff39\uffff\uffff\uffff\uffff\uffff\73\0" +
		"\uffff\uffff\uff33\uffff\uffff\uffff\40\0\75\0\100\0\101\0\102\0\uffff\uffff\uffff" +
		"\uffff\70\0\76\0\uffff\uffff\uffff\uffff\77\0\uffff\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = TypesLexer.unpack_short(208,
		"\24\uffff\0\0\uffff\ufffe\25\uffff\16\3\uffff\ufffe\7\uffff\12\105\16\105\uffff\ufffe" +
		"\12\uffff\16\10\uffff\ufffe\1\uffff\16\uffff\26\uffff\27\uffff\30\uffff\17\5\uffff" +
		"\ufffe\1\uffff\16\uffff\26\uffff\27\uffff\30\uffff\15\22\uffff\ufffe\1\uffff\16\uffff" +
		"\26\uffff\27\uffff\30\uffff\17\6\uffff\ufffe\22\uffff\1\51\12\51\15\51\21\51\uffff" +
		"\ufffe\7\uffff\10\uffff\1\56\12\56\15\56\21\56\22\56\uffff\ufffe\7\uffff\12\106\16" +
		"\106\uffff\ufffe\12\uffff\15\23\21\23\uffff\ufffe\20\uffff\22\uffff\11\15\14\15\uffff" +
		"\ufffe\1\uffff\16\uffff\26\uffff\27\uffff\30\uffff\21\22\uffff\ufffe\14\uffff\11" +
		"\17\uffff\ufffe\6\uffff\11\46\12\46\23\46\uffff\ufffe\12\uffff\11\34\23\34\uffff" +
		"\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\22\uffff\23\71\uffff\ufffe\12\uffff\11\35" +
		"\23\35\uffff\ufffe\12\uffff\11\36\23\36\uffff\ufffe\12\uffff\23\72\uffff\ufffe\1" +
		"\uffff\21\66\uffff\ufffe\12\uffff\21\67\uffff\ufffe");

	private static final short[] lapg_sym_goto = TypesLexer.unpack_short(63,
		"\0\1\27\37\51\56\56\57\63\65\70\100\104\107\112\120\122\124\126\135\140\142\143\150" +
		"\155\162\164\166\167\170\172\173\174\176\200\202\204\205\206\207\211\213\215\220" +
		"\223\227\234\241\246\253\260\261\262\264\300\301\302\303\304\305\307\310\311");

	private static final short[] lapg_sym_from = TypesLexer.unpack_short(201,
		"\154\1\6\14\15\16\17\23\27\45\47\50\70\77\100\101\106\131\132\133\145\146\152\70" +
		"\77\100\106\132\133\145\152\42\50\70\73\74\101\106\133\145\152\70\106\133\145\152" +
		"\53\12\31\34\112\31\73\64\72\76\13\35\54\67\122\124\127\136\62\63\135\151\52\135" +
		"\151\37\135\151\10\14\17\23\45\47\32\46\41\112\60\137\30\41\70\106\133\145\152\54" +
		"\64\130\0\2\4\14\17\23\45\47\14\17\23\45\47\14\17\23\45\47\50\101\50\101\0\0\0\2" +
		"\4\14\14\23\14\23\14\23\17\47\52\41\50\50\101\50\101\77\100\77\100\132\42\50\101" +
		"\42\50\74\101\14\17\23\45\47\14\17\23\45\47\70\106\133\145\152\70\106\133\145\152" +
		"\70\106\133\145\152\106\131\135\151\6\14\16\17\23\45\47\70\106\133\145\152\6\4\14" +
		"\41\52\17\47\131\106");

	private static final short[] lapg_sym_to = TypesLexer.unpack_short(201,
		"\155\4\11\11\33\11\11\11\41\11\11\61\11\120\120\61\11\135\120\11\11\151\11\103\121" +
		"\121\103\121\103\103\103\53\53\104\114\53\53\104\104\104\104\105\105\105\105\105" +
		"\73\15\15\15\15\43\115\101\113\117\16\45\74\74\132\132\133\146\77\100\142\142\70" +
		"\143\143\46\144\144\14\17\17\17\17\17\44\57\47\131\76\147\42\50\106\106\106\106\106" +
		"\75\102\134\1\1\6\20\20\20\20\20\21\21\21\21\21\22\22\22\22\22\62\62\63\63\154\2" +
		"\3\5\7\23\24\40\25\25\26\26\35\35\71\51\64\65\125\66\66\122\124\123\123\140\54\67" +
		"\67\55\55\116\55\27\36\27\56\36\30\30\30\30\30\107\126\141\150\153\110\110\110\110" +
		"\110\111\111\111\111\111\127\136\145\152\12\31\34\31\31\31\31\112\112\112\112\112" +
		"\13\10\32\52\72\37\60\137\130");

	private static final short[] lapg_rlen = TypesLexer.unpack_short(71,
		"\1\2\1\0\1\0\1\6\2\2\1\1\1\0\1\0\1\5\0\1\6\1\3\2\3\3\1\1\1\3\3\1\3\1\1\1\1\3\1\3" +
		"\3\1\4\1\1\1\1\2\4\1\1\1\1\1\0\1\4\0\1\3\1\3\3\5\1\1\1\1\3\1\3");

	private static final short[] lapg_rlex = TypesLexer.unpack_short(71,
		"\33\34\34\67\67\70\70\35\36\37\37\40\40\71\71\72\72\41\73\73\42\43\43\44\45\46\46" +
		"\47\47\50\50\50\51\51\52\52\53\53\54\54\54\55\55\56\56\56\56\56\56\57\57\60\60\60" +
		"\74\74\61\75\75\61\62\62\63\63\64\64\64\65\65\66\66");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"scon",
		"icon",
		"bcon",
		"_skip",
		"'..'",
		"'.'",
		"'*'",
		"';'",
		"','",
		"':'",
		"'='",
		"'=>'",
		"'{'",
		"'}'",
		"'('",
		"')'",
		"'['",
		"']'",
		"Lclass",
		"Lextends",
		"Lint",
		"Lbool",
		"Lstring",
		"Lset",
		"Lchoice",
		"input",
		"declarations",
		"type_declaration",
		"extends_clause",
		"member_declarations",
		"member_declaration",
		"feature_declaration",
		"method_declaration",
		"parameters",
		"defaultval",
		"modifiers",
		"constraints",
		"constraint",
		"string_constraint",
		"strings",
		"string",
		"multiplicity_list",
		"multiplicity",
		"type_ex",
		"type",
		"expression",
		"literal_expression",
		"structural_expression",
		"expression_list",
		"map_entries",
		"map_separator",
		"name",
		"name_list",
		"extends_clauseopt",
		"member_declarationsopt",
		"modifiersopt",
		"defaultvalopt",
		"parametersopt",
		"map_entriesopt",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 27;
		public static final int declarations = 28;
		public static final int type_declaration = 29;
		public static final int extends_clause = 30;
		public static final int member_declarations = 31;
		public static final int member_declaration = 32;
		public static final int feature_declaration = 33;
		public static final int method_declaration = 34;
		public static final int parameters = 35;
		public static final int defaultval = 36;
		public static final int modifiers = 37;
		public static final int constraints = 38;
		public static final int constraint = 39;
		public static final int string_constraint = 40;
		public static final int strings = 41;
		public static final int string = 42;
		public static final int multiplicity_list = 43;
		public static final int multiplicity = 44;
		public static final int type_ex = 45;
		public static final int type = 46;
		public static final int expression = 47;
		public static final int literal_expression = 48;
		public static final int structural_expression = 49;
		public static final int expression_list = 50;
		public static final int map_entries = 51;
		public static final int map_separator = 52;
		public static final int name = 53;
		public static final int name_list = 54;
		public static final int extends_clauseopt = 55;
		public static final int member_declarationsopt = 56;
		public static final int modifiersopt = 57;
		public static final int defaultvalopt = 58;
		public static final int parametersopt = 59;
		public static final int map_entriesopt = 60;
		public static final int expression_listopt = 61;
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
	protected TypesLexer lapg_lexer;

	public AstInput parse(TypesLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 109) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != 109) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			throw new ParseException();
		}
		return (AstInput)lapg_m[lapg_head - 1].sym;
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
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 0:  // input ::= declarations
				lapg_gg.sym = new AstInput(
						((List<AstTypeDeclaration>)lapg_m[lapg_head].sym) /* declarations */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 1:  // declarations ::= declarations type_declaration
				((List<AstTypeDeclaration>)lapg_m[lapg_head - 1].sym).add(((AstTypeDeclaration)lapg_m[lapg_head].sym));
				break;
			case 2:  // declarations ::= type_declaration
				lapg_gg.sym = new ArrayList();
				((List<AstTypeDeclaration>)lapg_gg.sym).add(((AstTypeDeclaration)lapg_m[lapg_head].sym));
				break;
			case 7:  // type_declaration ::= Lclass identifier extends_clauseopt '{' member_declarationsopt '}'
				lapg_gg.sym = new AstTypeDeclaration(
						((String)lapg_m[lapg_head - 4].sym) /* name */,
						((List<List<String>>)lapg_m[lapg_head - 3].sym) /* _extends */,
						((List<IAstMemberDeclaration>)lapg_m[lapg_head - 1].sym) /* memberDeclarationsopt */,
						null /* input */, lapg_m[lapg_head - 5].offset, lapg_m[lapg_head].endoffset);
				break;
			case 8:  // extends_clause ::= Lextends name_list
				lapg_gg.sym = ((List<List<String>>)lapg_m[lapg_head].sym);
				break;
			case 9:  // member_declarations ::= member_declarations member_declaration
				((List<IAstMemberDeclaration>)lapg_m[lapg_head - 1].sym).add(((IAstMemberDeclaration)lapg_m[lapg_head].sym));
				break;
			case 10:  // member_declarations ::= member_declaration
				lapg_gg.sym = new ArrayList();
				((List<IAstMemberDeclaration>)lapg_gg.sym).add(((IAstMemberDeclaration)lapg_m[lapg_head].sym));
				break;
			case 17:  // feature_declaration ::= type_ex identifier modifiersopt defaultvalopt ';'
				lapg_gg.sym = new AstFeatureDeclaration(
						((String)lapg_m[lapg_head - 3].sym) /* name */,
						((AstTypeEx)lapg_m[lapg_head - 4].sym) /* typeEx */,
						((List<AstConstraint>)lapg_m[lapg_head - 2].sym) /* modifiersopt */,
						((IAstExpression)lapg_m[lapg_head - 1].sym) /* defaultvalopt */,
						null /* input */, lapg_m[lapg_head - 4].offset, lapg_m[lapg_head].endoffset);
				break;
			case 20:  // method_declaration ::= type_ex identifier '(' parametersopt ')' ';'
				lapg_gg.sym = new AstMethodDeclaration(
						((AstTypeEx)lapg_m[lapg_head - 5].sym) /* returnType */,
						((String)lapg_m[lapg_head - 4].sym) /* name */,
						((List<AstTypeEx>)lapg_m[lapg_head - 2].sym) /* parametersopt */,
						null /* input */, lapg_m[lapg_head - 5].offset, lapg_m[lapg_head].endoffset);
				break;
			case 21:  // parameters ::= type_ex
				lapg_gg.sym = new ArrayList();
				((List<AstTypeEx>)lapg_gg.sym).add(((AstTypeEx)lapg_m[lapg_head].sym));
				break;
			case 22:  // parameters ::= parameters ',' type_ex
				((List<AstTypeEx>)lapg_m[lapg_head - 2].sym).add(((AstTypeEx)lapg_m[lapg_head].sym));
				break;
			case 23:  // defaultval ::= '=' expression
				lapg_gg.sym = ((IAstExpression)lapg_m[lapg_head].sym);
				break;
			case 24:  // modifiers ::= '[' constraints ']'
				lapg_gg.sym = ((List<AstConstraint>)lapg_m[lapg_head - 1].sym);
				break;
			case 25:  // constraints ::= constraints ';' constraint
				((List<AstConstraint>)lapg_m[lapg_head - 2].sym).add(((AstConstraint)lapg_m[lapg_head].sym));
				break;
			case 26:  // constraints ::= constraint
				lapg_gg.sym = new ArrayList();
				((List<AstConstraint>)lapg_gg.sym).add(((AstConstraint)lapg_m[lapg_head].sym));
				break;
			case 27:  // constraint ::= string_constraint
				lapg_gg.sym = new AstConstraint(
						((AstStringConstraint)lapg_m[lapg_head].sym) /* stringConstraint */,
						null /* multiplicityList */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 28:  // constraint ::= multiplicity_list
				lapg_gg.sym = new AstConstraint(
						null /* stringConstraint */,
						((List<AstMultiplicity>)lapg_m[lapg_head].sym) /* multiplicityList */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 29:  // string_constraint ::= Lset ':' strings
				lapg_gg.sym = new AstStringConstraint(
						AstStringConstraint.LSET,
						((List<Ast_String>)lapg_m[lapg_head].sym) /* strings */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset);
				break;
			case 30:  // string_constraint ::= Lchoice ':' strings
				lapg_gg.sym = new AstStringConstraint(
						AstStringConstraint.LCHOICE,
						((List<Ast_String>)lapg_m[lapg_head].sym) /* strings */,
						null /* identifier */,
						null /* input */, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset);
				break;
			case 31:  // string_constraint ::= identifier
				lapg_gg.sym = new AstStringConstraint(
						0,
						null /* strings */,
						((String)lapg_m[lapg_head].sym) /* identifier */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 32:  // strings ::= strings ',' string
				((List<Ast_String>)lapg_m[lapg_head - 2].sym).add(((Ast_String)lapg_m[lapg_head].sym));
				break;
			case 33:  // strings ::= string
				lapg_gg.sym = new ArrayList();
				((List<Ast_String>)lapg_gg.sym).add(((Ast_String)lapg_m[lapg_head].sym));
				break;
			case 34:  // string ::= identifier
				lapg_gg.sym = new Ast_String(
						((String)lapg_m[lapg_head].sym) /* identifier */,
						null /* scon */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 35:  // string ::= scon
				lapg_gg.sym = new Ast_String(
						null /* identifier */,
						((String)lapg_m[lapg_head].sym) /* scon */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 36:  // multiplicity_list ::= multiplicity
				lapg_gg.sym = new ArrayList();
				((List<AstMultiplicity>)lapg_gg.sym).add(((AstMultiplicity)lapg_m[lapg_head].sym));
				break;
			case 37:  // multiplicity_list ::= multiplicity_list ',' multiplicity
				((List<AstMultiplicity>)lapg_m[lapg_head - 2].sym).add(((AstMultiplicity)lapg_m[lapg_head].sym));
				break;
			case 38:  // multiplicity ::= icon
				lapg_gg.sym = new AstMultiplicity(
						((Integer)lapg_m[lapg_head].sym) /* lo */,
						false,
						null /* hi */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 39:  // multiplicity ::= icon '..' '*'
				lapg_gg.sym = new AstMultiplicity(
						((Integer)lapg_m[lapg_head - 2].sym) /* lo */,
						true,
						null /* hi */,
						null /* input */, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset);
				break;
			case 40:  // multiplicity ::= icon '..' icon
				lapg_gg.sym = new AstMultiplicity(
						((Integer)lapg_m[lapg_head - 2].sym) /* lo */,
						false,
						((Integer)lapg_m[lapg_head].sym) /* hi */,
						null /* input */, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset);
				break;
			case 41:  // type_ex ::= type
				lapg_gg.sym = new AstTypeEx(
						((AstType)lapg_m[lapg_head].sym) /* type */,
						null /* multiplicityList */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 42:  // type_ex ::= type '[' multiplicity_list ']'
				lapg_gg.sym = new AstTypeEx(
						((AstType)lapg_m[lapg_head - 3].sym) /* type */,
						((List<AstMultiplicity>)lapg_m[lapg_head - 1].sym) /* multiplicityList */,
						null /* input */, lapg_m[lapg_head - 3].offset, lapg_m[lapg_head].endoffset);
				break;
			case 43:  // type ::= Lint
				lapg_gg.sym = new AstType(
						AstType.LINT,
						false,
						false,
						null /* name */,
						null /* parametersopt */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 44:  // type ::= Lstring
				lapg_gg.sym = new AstType(
						AstType.LSTRING,
						false,
						false,
						null /* name */,
						null /* parametersopt */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 45:  // type ::= Lbool
				lapg_gg.sym = new AstType(
						AstType.LBOOL,
						false,
						false,
						null /* name */,
						null /* parametersopt */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 46:  // type ::= name
				lapg_gg.sym = new AstType(
						0,
						false,
						false,
						((List<String>)lapg_m[lapg_head].sym) /* name */,
						null /* parametersopt */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 47:  // type ::= name '*'
				lapg_gg.sym = new AstType(
						0,
						true,
						false,
						((List<String>)lapg_m[lapg_head - 1].sym) /* name */,
						null /* parametersopt */,
						null /* input */, lapg_m[lapg_head - 1].offset, lapg_m[lapg_head].endoffset);
				break;
			case 48:  // type ::= '{' parametersopt '=>' '}'
				lapg_gg.sym = new AstType(
						0,
						false,
						true,
						null /* name */,
						((List<AstTypeEx>)lapg_m[lapg_head - 2].sym) /* parametersopt */,
						null /* input */, lapg_m[lapg_head - 3].offset, lapg_m[lapg_head].endoffset);
				break;
			case 51:  // literal_expression ::= scon
				lapg_gg.sym = new AstLiteralExpression(
						((String)lapg_m[lapg_head].sym) /* scon */,
						null /* icon */,
						null /* bcon */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 52:  // literal_expression ::= icon
				lapg_gg.sym = new AstLiteralExpression(
						null /* scon */,
						((Integer)lapg_m[lapg_head].sym) /* icon */,
						null /* bcon */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 53:  // literal_expression ::= bcon
				lapg_gg.sym = new AstLiteralExpression(
						null /* scon */,
						null /* icon */,
						((Boolean)lapg_m[lapg_head].sym) /* bcon */,
						null /* input */, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset);
				break;
			case 56:  // structural_expression ::= name '(' map_entriesopt ')'
				lapg_gg.sym = new AstStructuralExpression(
						((List<String>)lapg_m[lapg_head - 3].sym) /* name */,
						((List<AstMapEntriesItem>)lapg_m[lapg_head - 1].sym) /* mapEntriesopt */,
						null /* expressionListopt */,
						null /* input */, lapg_m[lapg_head - 3].offset, lapg_m[lapg_head].endoffset);
				break;
			case 59:  // structural_expression ::= '[' expression_listopt ']'
				lapg_gg.sym = new AstStructuralExpression(
						null /* name */,
						null /* mapEntriesopt */,
						((List<IAstExpression>)lapg_m[lapg_head - 1].sym) /* expressionListopt */,
						null /* input */, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset);
				break;
			case 60:  // expression_list ::= expression
				lapg_gg.sym = new ArrayList();
				((List<IAstExpression>)lapg_gg.sym).add(((IAstExpression)lapg_m[lapg_head].sym));
				break;
			case 61:  // expression_list ::= expression_list ',' expression
				((List<IAstExpression>)lapg_m[lapg_head - 2].sym).add(((IAstExpression)lapg_m[lapg_head].sym));
				break;
			case 62:  // map_entries ::= identifier map_separator expression
				lapg_gg.sym = new ArrayList();
				((List<AstMapEntriesItem>)lapg_gg.sym).add(new AstMapEntriesItem(
						((String)lapg_m[lapg_head - 2].sym) /* identifier */,
						((AstMapSeparator)lapg_m[lapg_head - 1].sym) /* mapSeparator */,
						((IAstExpression)lapg_m[lapg_head].sym) /* expression */,
						null /* input */, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset));
				break;
			case 63:  // map_entries ::= map_entries ',' identifier map_separator expression
				((List<AstMapEntriesItem>)lapg_m[lapg_head - 4].sym).add(new AstMapEntriesItem(
						((String)lapg_m[lapg_head - 2].sym) /* identifier */,
						((AstMapSeparator)lapg_m[lapg_head - 1].sym) /* mapSeparator */,
						((IAstExpression)lapg_m[lapg_head].sym) /* expression */,
						null /* input */, lapg_m[lapg_head - 4].offset, lapg_m[lapg_head].endoffset));
				break;
			case 64:  // map_separator ::= ':'
				lapg_gg.sym = AstMapSeparator.COLON;
				break;
			case 65:  // map_separator ::= '='
				lapg_gg.sym = AstMapSeparator.EQUAL;
				break;
			case 66:  // map_separator ::= '=>'
				lapg_gg.sym = AstMapSeparator.EQUALGREATER;
				break;
			case 67:  // name ::= identifier
				lapg_gg.sym = new ArrayList();
				((List<String>)lapg_gg.sym).add(((String)lapg_m[lapg_head].sym));
				break;
			case 68:  // name ::= name '.' identifier
				((List<String>)lapg_m[lapg_head - 2].sym).add(((String)lapg_m[lapg_head].sym));
				break;
			case 69:  // name_list ::= name
				lapg_gg.sym = new ArrayList();
				((List<List<String>>)lapg_gg.sym).add(((List<String>)lapg_m[lapg_head].sym));
				break;
			case 70:  // name_list ::= name_list ',' name
				((List<List<String>>)lapg_m[lapg_head - 2].sym).add(((List<String>)lapg_m[lapg_head].sym));
				break;
		}
	}
}
