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
package org.textmapper.templates.types;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.templates.types.TypesLexer.ErrorReporter;
import org.textmapper.templates.types.TypesLexer.Span;
import org.textmapper.templates.types.TypesLexer.Tokens;
import org.textmapper.templates.types.ast.AstConstraint;
import org.textmapper.templates.types.ast.AstFeatureDeclaration;
import org.textmapper.templates.types.ast.AstInput;
import org.textmapper.templates.types.ast.AstListOfIdentifierAnd2ElementsCommaSeparatedItem;
import org.textmapper.templates.types.ast.AstLiteralExpression;
import org.textmapper.templates.types.ast.AstMapSeparator;
import org.textmapper.templates.types.ast.AstMethodDeclaration;
import org.textmapper.templates.types.ast.AstMultiplicity;
import org.textmapper.templates.types.ast.AstStringConstraint;
import org.textmapper.templates.types.ast.AstStructuralExpression;
import org.textmapper.templates.types.ast.AstType;
import org.textmapper.templates.types.ast.AstTypeDeclaration;
import org.textmapper.templates.types.ast.AstTypeEx;
import org.textmapper.templates.types.ast.Ast_String;
import org.textmapper.templates.types.ast.IAstExpression;
import org.textmapper.templates.types.ast.IAstMemberDeclaration;

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
	private static final int[] tmAction = TypesLexer.unpack_int(108,
		"\uffff\uffff\uffff\uffff\ufffd\uffff\2\0\ufff7\uffff\1\0\uffff\uffff\73\0\uffff\uffff" +
		"\67\0\ufff1\uffff\uffe9\uffff\uffe3\uffff\uffff\uffff\uffff\uffff\uffff\uffff\70" +
		"\0\uffd5\uffff\uffcd\uffff\5\0\41\0\43\0\42\0\3\0\7\0\10\0\uffff\uffff\uffbf\uffff" +
		"\uffb3\uffff\uffa3\uffff\13\0\uffff\uffff\uff9b\uffff\uffff\uffff\45\0\uffff\uffff" +
		"\uffff\uffff\uff91\uffff\uffff\uffff\75\0\uff83\uffff\uff7d\uffff\uffff\uffff\24" +
		"\0\14\0\46\0\uffff\uffff\27\0\uffff\uffff\uffff\uffff\uffff\uffff\17\0\uff73\uffff" +
		"\21\0\uffff\uffff\77\0\uffff\uffff\uffff\uffff\uffff\uffff\40\0\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\20\0\51\0\52\0\53\0\uff6b\uffff\15\0\50\0\47\0\uffff" +
		"\uffff\11\0\36\0\35\0\23\0\12\0\32\0\33\0\uff5d\uffff\31\0\uff55\uffff\16\0\62\0" +
		"\uff4d\uffff\uffff\uffff\uff47\uffff\uffff\uffff\uffff\uffff\61\0\uffff\uffff\uff41" +
		"\uffff\uffff\uffff\30\0\63\0\64\0\65\0\66\0\uffff\uffff\uffff\uffff\60\0\55\0\uffff" +
		"\uffff\uffff\uffff\54\0\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = TypesLexer.unpack_short(194,
		"\24\uffff\0\0\uffff\ufffe\25\uffff\16\74\uffff\ufffe\7\uffff\12\71\16\71\uffff\ufffe" +
		"\12\uffff\16\6\uffff\ufffe\1\4\16\4\17\4\26\4\27\4\30\4\uffff\ufffe\7\uffff\12\72" +
		"\16\72\uffff\ufffe\1\uffff\16\uffff\26\uffff\27\uffff\30\uffff\15\102\uffff\ufffe" +
		"\22\uffff\1\37\12\37\15\37\21\37\uffff\ufffe\7\uffff\10\uffff\1\44\12\44\15\44\21" +
		"\44\22\44\uffff\ufffe\12\uffff\15\101\21\101\uffff\ufffe\20\uffff\22\uffff\11\76" +
		"\14\76\uffff\ufffe\1\uffff\16\uffff\26\uffff\27\uffff\30\uffff\21\102\uffff\ufffe" +
		"\14\uffff\11\100\uffff\ufffe\6\uffff\11\34\12\34\23\34\uffff\ufffe\12\uffff\11\22" +
		"\23\22\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\22\uffff\23\104\uffff\ufffe\12" +
		"\uffff\11\25\23\25\uffff\ufffe\12\uffff\11\26\23\26\uffff\ufffe\12\uffff\23\103\uffff" +
		"\ufffe\1\uffff\21\57\uffff\ufffe\12\uffff\21\56\uffff\ufffe");

	private static final short[] lapg_sym_goto = TypesLexer.unpack_short(62,
		"\0\1\26\36\50\55\55\56\62\64\67\77\103\106\111\116\120\122\124\133\136\140\141\145" +
		"\151\155\157\161\162\163\164\166\167\170\171\172\174\175\176\177\201\204\206\210" +
		"\213\217\223\227\234\241\242\243\250\251\253\266\267\270\271\272\274\275");

	private static final short[] lapg_sym_from = TypesLexer.unpack_short(189,
		"\152\1\6\15\16\17\22\32\43\45\46\66\75\76\77\104\127\130\131\143\144\150\66\75\76" +
		"\104\130\131\143\150\41\46\66\71\72\77\104\131\143\150\66\104\131\143\150\51\12\21" +
		"\34\110\34\71\62\70\74\13\35\52\64\120\122\125\134\60\61\133\147\50\133\147\37\133" +
		"\147\10\17\22\43\45\17\44\40\110\56\135\33\40\66\104\131\143\150\52\62\126\0\2\4" +
		"\17\22\43\45\17\22\43\45\17\22\43\45\46\77\46\77\0\0\14\0\2\4\17\17\17\22\45\50\46" +
		"\40\46\77\41\46\77\46\77\75\76\75\76\130\41\46\72\77\17\22\43\45\17\22\43\45\66\104" +
		"\131\143\150\66\104\131\143\150\127\127\66\104\131\143\150\104\133\147\6\16\17\22" +
		"\43\45\66\104\131\143\150\6\4\40\50\22\45\104");

	private static final short[] lapg_sym_to = TypesLexer.unpack_short(189,
		"\153\4\11\20\11\11\11\40\11\11\57\11\116\116\57\11\133\116\11\11\147\11\101\117\117" +
		"\101\117\101\101\101\51\51\102\112\51\51\102\102\102\102\103\103\103\103\103\71\15" +
		"\15\15\15\42\113\77\111\115\16\43\72\72\130\130\131\144\75\76\140\140\66\141\141" +
		"\44\142\142\14\22\22\22\22\23\55\45\127\74\145\41\46\104\104\104\104\104\73\100\132" +
		"\1\1\6\24\24\24\24\25\25\25\25\26\26\26\26\60\60\61\61\152\2\17\3\5\7\27\30\31\35" +
		"\35\67\62\47\63\123\52\64\64\65\65\120\122\121\121\136\53\53\114\53\32\36\54\36\33" +
		"\33\33\33\105\124\137\146\151\106\106\106\106\106\134\135\107\107\107\107\107\125" +
		"\143\150\12\21\34\34\34\34\110\110\110\110\110\13\10\50\70\37\56\126");

	private static final short[] tmRuleLen = TypesLexer.unpack_short(69,
		"\1\2\1\2\0\6\2\1\1\5\6\1\3\2\3\1\3\1\1\3\1\3\3\1\3\1\1\1\1\3\3\1\4\1\1\1\1\2\4\1" +
		"\1\1\1\1\5\3\1\0\4\3\1\3\1\1\1\1\3\1\3\1\0\1\0\1\0\1\0\1\0");

	private static final short[] tmRuleSymbol = TypesLexer.unpack_short(69,
		"\33\34\34\35\35\36\37\40\40\41\42\43\43\44\45\45\46\47\47\50\50\51\51\51\52\52\53" +
		"\53\54\54\54\55\55\56\56\56\56\56\56\57\57\60\60\60\61\61\62\62\63\63\64\64\65\65" +
		"\65\66\66\67\67\70\70\71\71\72\72\73\73\74\74");

	protected static final String[] tmSymbolNames = new String[] {
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
		"member_declaration_optlist",
		"type_declaration",
		"extends_clause",
		"member_declaration",
		"feature_declaration",
		"method_declaration",
		"parameters",
		"defaultval",
		"constraint_list_Semicolon_separated",
		"modifiers",
		"constraint",
		"multiplicity_list_Comma_separated",
		"string_constraint",
		"strings",
		"string",
		"multiplicity",
		"type_ex",
		"type",
		"expression",
		"literal_expression",
		"list_of_identifier_and_2_elements_Comma_separated",
		"list_of_identifier_and_2_elements_Comma_separated_opt",
		"structural_expression",
		"expression_list",
		"map_separator",
		"name",
		"name_list",
		"extends_clauseopt",
		"modifiersopt",
		"defaultvalopt",
		"parametersopt",
		"expression_listopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int input = 27;
		int declarations = 28;
		int member_declaration_optlist = 29;
		int type_declaration = 30;
		int extends_clause = 31;
		int member_declaration = 32;
		int feature_declaration = 33;
		int method_declaration = 34;
		int parameters = 35;
		int defaultval = 36;
		int constraint_list_Semicolon_separated = 37;
		int modifiers = 38;
		int constraint = 39;
		int multiplicity_list_Comma_separated = 40;
		int string_constraint = 41;
		int strings = 42;
		int string = 43;
		int multiplicity = 44;
		int type_ex = 45;
		int type = 46;
		int expression = 47;
		int literal_expression = 48;
		int list_of_identifier_and_2_elements_Comma_separated = 49;
		int list_of_identifier_and_2_elements_Comma_separated_opt = 50;
		int structural_expression = 51;
		int expression_list = 52;
		int map_separator = 53;
		int name = 54;
		int name_list = 55;
		int extends_clauseopt = 56;
		int modifiersopt = 57;
		int defaultvalopt = 58;
		int parametersopt = 59;
		int expression_listopt = 60;
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
	protected TypesLexer tmLexer;

	public AstInput parse(TypesLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;

		tmStack[0] = new Span();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 107) {
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

		if (tmStack[tmHead].state != 107) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
			throw new ParseException();
		}
		return (AstInput)tmStack[tmHead - 1].value;
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
		switch (ruleIndex) {
			case 0:  // input ::= declarations
				tmLeft.value = new AstInput(
						((List<AstTypeDeclaration>)tmStack[tmHead].value) /* declarations */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 1:  // declarations ::= declarations type_declaration
				((List<AstTypeDeclaration>)tmLeft.value).add(((AstTypeDeclaration)tmStack[tmHead].value));
				break;
			case 2:  // declarations ::= type_declaration
				tmLeft.value = new ArrayList();
				((List<AstTypeDeclaration>)tmLeft.value).add(((AstTypeDeclaration)tmStack[tmHead].value));
				break;
			case 3:  // member_declaration_optlist ::= member_declaration_optlist member_declaration
				((List<IAstMemberDeclaration>)tmLeft.value).add(((IAstMemberDeclaration)tmStack[tmHead].value));
				break;
			case 4:  // member_declaration_optlist ::=
				tmLeft.value = new ArrayList();
				break;
			case 5:  // type_declaration ::= Lclass identifier extends_clauseopt '{' member_declaration_optlist '}'
				tmLeft.value = new AstTypeDeclaration(
						((String)tmStack[tmHead - 4].value) /* name */,
						((List<List<String>>)tmStack[tmHead - 3].value) /* _super */,
						((List<IAstMemberDeclaration>)tmStack[tmHead - 1].value) /* members */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 6:  // extends_clause ::= Lextends name_list
				tmLeft.value = ((List<List<String>>)tmStack[tmHead].value);
				break;
			case 9:  // feature_declaration ::= type_ex identifier modifiersopt defaultvalopt ';'
				tmLeft.value = new AstFeatureDeclaration(
						((AstTypeEx)tmStack[tmHead - 4].value) /* typeEx */,
						((String)tmStack[tmHead - 3].value) /* name */,
						((List<AstConstraint>)tmStack[tmHead - 2].value) /* modifiers */,
						((IAstExpression)tmStack[tmHead - 1].value) /* defaultval */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 10:  // method_declaration ::= type_ex identifier '(' parametersopt ')' ';'
				tmLeft.value = new AstMethodDeclaration(
						((AstTypeEx)tmStack[tmHead - 5].value) /* returnType */,
						((String)tmStack[tmHead - 4].value) /* name */,
						((List<AstTypeEx>)tmStack[tmHead - 2].value) /* parameters */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // parameters ::= type_ex
				tmLeft.value = new ArrayList();
				((List<AstTypeEx>)tmLeft.value).add(((AstTypeEx)tmStack[tmHead].value));
				break;
			case 12:  // parameters ::= parameters ',' type_ex
				((List<AstTypeEx>)tmLeft.value).add(((AstTypeEx)tmStack[tmHead].value));
				break;
			case 13:  // defaultval ::= '=' expression
				tmLeft.value = ((IAstExpression)tmStack[tmHead].value);
				break;
			case 14:  // constraint_list_Semicolon_separated ::= constraint_list_Semicolon_separated ';' constraint
				((List<AstConstraint>)tmLeft.value).add(((AstConstraint)tmStack[tmHead].value));
				break;
			case 15:  // constraint_list_Semicolon_separated ::= constraint
				tmLeft.value = new ArrayList();
				((List<AstConstraint>)tmLeft.value).add(((AstConstraint)tmStack[tmHead].value));
				break;
			case 16:  // modifiers ::= '[' constraint_list_Semicolon_separated ']'
				tmLeft.value = ((List<AstConstraint>)tmStack[tmHead - 1].value);
				break;
			case 17:  // constraint ::= string_constraint
				tmLeft.value = new AstConstraint(
						((AstStringConstraint)tmStack[tmHead].value) /* stringConstraint */,
						null /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // constraint ::= multiplicity_list_Comma_separated
				tmLeft.value = new AstConstraint(
						null /* stringConstraint */,
						((List<AstMultiplicity>)tmStack[tmHead].value) /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // multiplicity_list_Comma_separated ::= multiplicity_list_Comma_separated ',' multiplicity
				((List<AstMultiplicity>)tmLeft.value).add(((AstMultiplicity)tmStack[tmHead].value));
				break;
			case 20:  // multiplicity_list_Comma_separated ::= multiplicity
				tmLeft.value = new ArrayList();
				((List<AstMultiplicity>)tmLeft.value).add(((AstMultiplicity)tmStack[tmHead].value));
				break;
			case 21:  // string_constraint ::= Lset ':' strings
				tmLeft.value = new AstStringConstraint(
						AstStringConstraint.AstKindKind.LSET /* kind */,
						((List<Ast_String>)tmStack[tmHead].value) /* strings */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 22:  // string_constraint ::= Lchoice ':' strings
				tmLeft.value = new AstStringConstraint(
						AstStringConstraint.AstKindKind.LCHOICE /* kind */,
						((List<Ast_String>)tmStack[tmHead].value) /* strings */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 23:  // string_constraint ::= identifier
				tmLeft.value = new AstStringConstraint(
						null /* kind */,
						null /* strings */,
						((String)tmStack[tmHead].value) /* identifier */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 24:  // strings ::= strings ',' string
				((List<Ast_String>)tmLeft.value).add(((Ast_String)tmStack[tmHead].value));
				break;
			case 25:  // strings ::= string
				tmLeft.value = new ArrayList();
				((List<Ast_String>)tmLeft.value).add(((Ast_String)tmStack[tmHead].value));
				break;
			case 26:  // string ::= identifier
				tmLeft.value = new Ast_String(
						((String)tmStack[tmHead].value) /* identifier */,
						null /* scon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 27:  // string ::= scon
				tmLeft.value = new Ast_String(
						null /* identifier */,
						((String)tmStack[tmHead].value) /* scon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 28:  // multiplicity ::= icon
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead].value) /* lo */,
						false /* hasNoUpperBound */,
						null /* hi */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 29:  // multiplicity ::= icon '..' '*'
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead - 2].value) /* lo */,
						true /* hasNoUpperBound */,
						null /* hi */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // multiplicity ::= icon '..' icon
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead - 2].value) /* lo */,
						false /* hasNoUpperBound */,
						((Integer)tmStack[tmHead].value) /* hi */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // type_ex ::= type
				tmLeft.value = new AstTypeEx(
						((AstType)tmStack[tmHead].value) /* type */,
						null /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // type_ex ::= type '[' multiplicity_list_Comma_separated ']'
				tmLeft.value = new AstTypeEx(
						((AstType)tmStack[tmHead - 3].value) /* type */,
						((List<AstMultiplicity>)tmStack[tmHead - 1].value) /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 33:  // type ::= Lint
				tmLeft.value = new AstType(
						AstType.AstKindKind.LINT /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 34:  // type ::= Lstring
				tmLeft.value = new AstType(
						AstType.AstKindKind.LSTRING /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 35:  // type ::= Lbool
				tmLeft.value = new AstType(
						AstType.AstKindKind.LBOOL /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 36:  // type ::= name
				tmLeft.value = new AstType(
						null /* kind */,
						((List<String>)tmStack[tmHead].value) /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // type ::= name '*'
				tmLeft.value = new AstType(
						null /* kind */,
						((List<String>)tmStack[tmHead - 1].value) /* name */,
						true /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 38:  // type ::= '{' parametersopt '=>' '}'
				tmLeft.value = new AstType(
						null /* kind */,
						null /* name */,
						false /* isReference */,
						true /* isClosure */,
						((List<AstTypeEx>)tmStack[tmHead - 2].value) /* parameters */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 41:  // literal_expression ::= scon
				tmLeft.value = new AstLiteralExpression(
						((String)tmStack[tmHead].value) /* scon */,
						null /* icon */,
						null /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 42:  // literal_expression ::= icon
				tmLeft.value = new AstLiteralExpression(
						null /* scon */,
						((Integer)tmStack[tmHead].value) /* icon */,
						null /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 43:  // literal_expression ::= bcon
				tmLeft.value = new AstLiteralExpression(
						null /* scon */,
						null /* icon */,
						((Boolean)tmStack[tmHead].value) /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 44:  // list_of_identifier_and_2_elements_Comma_separated ::= list_of_identifier_and_2_elements_Comma_separated ',' identifier map_separator expression
				((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmLeft.value).add(new AstListOfIdentifierAnd2ElementsCommaSeparatedItem(
						((String)tmStack[tmHead - 2].value) /* identifier */,
						((AstMapSeparator)tmStack[tmHead - 1].value) /* mapSeparator */,
						((IAstExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset));
				break;
			case 45:  // list_of_identifier_and_2_elements_Comma_separated ::= identifier map_separator expression
				tmLeft.value = new ArrayList();
				((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmLeft.value).add(new AstListOfIdentifierAnd2ElementsCommaSeparatedItem(
						((String)tmStack[tmHead - 2].value) /* identifier */,
						((AstMapSeparator)tmStack[tmHead - 1].value) /* mapSeparator */,
						((IAstExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 48:  // structural_expression ::= name '(' list_of_identifier_and_2_elements_Comma_separated_opt ')'
				tmLeft.value = new AstStructuralExpression(
						((List<String>)tmStack[tmHead - 3].value) /* name */,
						((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmStack[tmHead - 1].value) /* mapEntries */,
						null /* expressionList */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // structural_expression ::= '[' expression_listopt ']'
				tmLeft.value = new AstStructuralExpression(
						null /* name */,
						null /* mapEntries */,
						((List<IAstExpression>)tmStack[tmHead - 1].value) /* expressionList */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // expression_list ::= expression
				tmLeft.value = new ArrayList();
				((List<IAstExpression>)tmLeft.value).add(((IAstExpression)tmStack[tmHead].value));
				break;
			case 51:  // expression_list ::= expression_list ',' expression
				((List<IAstExpression>)tmLeft.value).add(((IAstExpression)tmStack[tmHead].value));
				break;
			case 52:  // map_separator ::= ':'
				tmLeft.value = AstMapSeparator.COLON;
				break;
			case 53:  // map_separator ::= '='
				tmLeft.value = AstMapSeparator.EQUAL;
				break;
			case 54:  // map_separator ::= '=>'
				tmLeft.value = AstMapSeparator.EQUAL_GREATER;
				break;
			case 55:  // name ::= identifier
				tmLeft.value = new ArrayList();
				((List<String>)tmLeft.value).add(((String)tmStack[tmHead].value));
				break;
			case 56:  // name ::= name '.' identifier
				((List<String>)tmLeft.value).add(((String)tmStack[tmHead].value));
				break;
			case 57:  // name_list ::= name
				tmLeft.value = new ArrayList();
				((List<List<String>>)tmLeft.value).add(((List<String>)tmStack[tmHead].value));
				break;
			case 58:  // name_list ::= name_list ',' name
				((List<List<String>>)tmLeft.value).add(((List<String>)tmStack[tmHead].value));
				break;
		}
	}
}
