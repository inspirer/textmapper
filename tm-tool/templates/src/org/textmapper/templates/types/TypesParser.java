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
package org.textmapper.templates.types;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.templates.types.TypesLexer.ErrorReporter;
import org.textmapper.templates.types.TypesLexer.LapgSymbol;
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
		"\uffff\uffff\uffff\uffff\ufffd\uffff\2\0\ufff7\uffff\1\0\uffff\uffff\3\0\uffff\uffff" +
		"\101\0\ufff1\uffff\uffe9\uffff\uffe3\uffff\uffff\uffff\uffff\uffff\uffff\uffff\102" +
		"\0\uffd5\uffff\uffcd\uffff\7\0\51\0\53\0\52\0\5\0\11\0\12\0\uffff\uffff\uffbf\uffff" +
		"\uffb3\uffff\uffa3\uffff\23\0\uffff\uffff\uff9b\uffff\uffff\uffff\55\0\uffff\uffff" +
		"\uffff\uffff\uff91\uffff\uffff\uffff\13\0\uff83\uffff\uff7d\uffff\33\0\uffff\uffff" +
		"\24\0\56\0\uffff\uffff\37\0\uffff\uffff\uffff\uffff\27\0\31\0\uffff\uffff\uff73\uffff" +
		"\uffff\uffff\15\0\uffff\uffff\uffff\uffff\uffff\uffff\50\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\30\0\61\0\62\0\63\0\uff6b\uffff\25\0\60\0\57\0\uffff\uffff" +
		"\17\0\46\0\45\0\32\0\22\0\42\0\43\0\uff5d\uffff\41\0\uff55\uffff\26\0\74\0\uff4d" +
		"\uffff\uffff\uffff\uff47\uffff\uffff\uffff\uffff\uffff\73\0\uffff\uffff\uff41\uffff" +
		"\uffff\uffff\40\0\75\0\76\0\77\0\100\0\uffff\uffff\uffff\uffff\70\0\65\0\uffff\uffff" +
		"\uffff\uffff\64\0\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = TypesLexer.unpack_short(194,
		"\24\uffff\0\0\uffff\ufffe\25\uffff\16\4\uffff\ufffe\7\uffff\12\103\16\103\uffff\ufffe" +
		"\12\uffff\16\10\uffff\ufffe\1\6\16\6\17\6\26\6\27\6\30\6\uffff\ufffe\7\uffff\12\104" +
		"\16\104\uffff\ufffe\1\uffff\16\uffff\26\uffff\27\uffff\30\uffff\15\21\uffff\ufffe" +
		"\22\uffff\1\47\12\47\15\47\21\47\uffff\ufffe\7\uffff\10\uffff\1\54\12\54\15\54\21" +
		"\54\22\54\uffff\ufffe\12\uffff\15\20\21\20\uffff\ufffe\20\uffff\22\uffff\11\14\14" +
		"\14\uffff\ufffe\1\uffff\16\uffff\26\uffff\27\uffff\30\uffff\21\21\uffff\ufffe\14" +
		"\uffff\11\16\uffff\ufffe\6\uffff\11\44\12\44\23\44\uffff\ufffe\12\uffff\11\34\23" +
		"\34\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\22\uffff\23\72\uffff\ufffe\12\uffff" +
		"\11\35\23\35\uffff\ufffe\12\uffff\11\36\23\36\uffff\ufffe\12\uffff\23\71\uffff\ufffe" +
		"\1\uffff\21\67\uffff\ufffe\12\uffff\21\66\uffff\ufffe");

	private static final short[] lapg_sym_goto = TypesLexer.unpack_short(62,
		"\0\1\26\36\50\55\55\56\62\64\67\77\103\106\111\116\120\122\124\133\136\140\141\145" +
		"\151\155\157\161\162\163\165\166\167\170\171\173\174\175\177\201\203\206\212\216" +
		"\222\227\234\241\242\244\257\260\261\262\263\264\266\267\272\273\274\275");

	private static final short[] lapg_sym_from = TypesLexer.unpack_short(189,
		"\152\1\6\15\16\17\22\32\43\45\46\66\75\76\77\104\127\130\131\143\144\150\66\75\76" +
		"\104\130\131\143\150\41\46\66\71\72\77\104\131\143\150\66\104\131\143\150\51\12\21" +
		"\34\110\34\71\64\70\74\13\35\53\65\120\122\125\134\60\61\133\147\50\133\147\37\133" +
		"\147\10\17\22\43\45\17\44\40\110\56\135\33\40\66\104\131\143\150\53\64\126\0\2\4" +
		"\17\22\43\45\17\22\43\45\17\22\43\45\46\77\46\77\0\0\0\2\4\17\17\17\22\45\50\40\46" +
		"\77\46\77\75\76\75\76\130\41\46\72\77\17\22\43\45\17\22\43\45\66\104\131\143\150" +
		"\66\104\131\143\150\66\104\131\143\150\104\133\147\6\16\17\22\43\45\66\104\131\143" +
		"\150\6\4\14\40\50\22\45\46\41\46\77\127\127\104");

	private static final short[] lapg_sym_to = TypesLexer.unpack_short(189,
		"\153\4\11\20\11\11\11\40\11\11\57\11\116\116\57\11\133\116\11\11\147\11\101\117\117" +
		"\101\117\101\101\101\51\51\102\112\51\51\102\102\102\102\103\103\103\103\103\71\15" +
		"\15\15\15\42\113\77\111\115\16\43\72\72\130\130\131\144\75\76\140\140\66\141\141" +
		"\44\142\142\14\22\22\22\22\23\55\45\127\74\145\41\46\104\104\104\104\104\73\100\132" +
		"\1\1\6\24\24\24\24\25\25\25\25\26\26\26\26\60\60\61\61\152\2\3\5\7\27\30\31\35\35" +
		"\67\47\62\123\63\63\120\122\121\121\136\52\52\114\52\32\36\54\36\33\33\33\33\105" +
		"\124\137\146\151\106\106\106\106\106\107\107\107\107\107\125\143\150\12\21\34\34" +
		"\34\34\110\110\110\110\110\13\10\17\50\70\37\56\64\53\65\65\134\135\126");

	private static final short[] tmRuleLen = TypesLexer.unpack_short(69,
		"\1\2\1\1\0\2\0\6\2\1\1\1\0\1\0\5\1\0\6\1\3\2\3\1\3\1\3\1\1\3\3\1\3\1\1\1\1\3\3\1" +
		"\4\1\1\1\1\2\4\1\1\1\1\1\5\3\1\0\4\1\0\3\1\3\1\1\1\1\3\1\3");

	private static final short[] tmRuleSymbol = TypesLexer.unpack_short(69,
		"\33\34\34\63\63\64\64\35\36\37\37\65\65\66\66\40\67\67\41\42\42\43\70\70\44\45\71" +
		"\71\45\46\46\46\47\47\50\50\51\51\51\52\52\53\53\53\53\53\53\54\54\55\55\55\72\72" +
		"\73\73\56\74\74\56\57\57\60\60\60\61\61\62\62");

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
		"type_declaration",
		"extends_clause",
		"member_declaration",
		"feature_declaration",
		"method_declaration",
		"parameters",
		"defaultval",
		"modifiers",
		"constraint",
		"string_constraint",
		"strings",
		"string",
		"multiplicity",
		"type_ex",
		"type",
		"expression",
		"literal_expression",
		"structural_expression",
		"expression_list",
		"map_separator",
		"name",
		"name_list",
		"extends_clauseopt",
		"member_declaration_optlist",
		"modifiersopt",
		"defaultvalopt",
		"parametersopt",
		"constraint_list_Semicolon_separated",
		"multiplicity_list_Comma_separated",
		"list_of_identifier_and_2_elements_Comma_separated",
		"list_of_identifier_and_2_elements_Comma_separated_opt",
		"expression_listopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int input = 27;
		static final int declarations = 28;
		static final int type_declaration = 29;
		static final int extends_clause = 30;
		static final int member_declaration = 31;
		static final int feature_declaration = 32;
		static final int method_declaration = 33;
		static final int parameters = 34;
		static final int defaultval = 35;
		static final int modifiers = 36;
		static final int constraint = 37;
		static final int string_constraint = 38;
		static final int strings = 39;
		static final int string = 40;
		static final int multiplicity = 41;
		static final int type_ex = 42;
		static final int type = 43;
		static final int expression = 44;
		static final int literal_expression = 45;
		static final int structural_expression = 46;
		static final int expression_list = 47;
		static final int map_separator = 48;
		static final int name = 49;
		static final int name_list = 50;
		static final int extends_clauseopt = 51;
		static final int member_declaration_optlist = 52;
		static final int modifiersopt = 53;
		static final int defaultvalopt = 54;
		static final int parametersopt = 55;
		static final int constraint_list_Semicolon_separated = 56;
		static final int multiplicity_list_Comma_separated = 57;
		static final int list_of_identifier_and_2_elements_Comma_separated = 58;
		static final int list_of_identifier_and_2_elements_Comma_separated_opt = 59;
		static final int expression_listopt = 60;
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
	protected TypesLexer tmLexer;

	public AstInput parse(TypesLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new LapgSymbol[1024];
		tmHead = 0;

		tmStack[0] = new LapgSymbol();
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
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = tmLeft;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmLeft.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol tmLeft, int tmRule, int tmLength) {
		switch (tmRule) {
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
			case 5:  // member_declaration_optlist ::= member_declaration_optlist member_declaration
				((List<IAstMemberDeclaration>)tmLeft.value).add(((IAstMemberDeclaration)tmStack[tmHead].value));
				break;
			case 6:  // member_declaration_optlist ::=
				tmLeft.value = new ArrayList();
				break;
			case 7:  // type_declaration ::= Lclass identifier extends_clauseopt '{' member_declaration_optlist '}'
				tmLeft.value = new AstTypeDeclaration(
						((String)tmStack[tmHead - 4].value) /* name */,
						((List<List<String>>)tmStack[tmHead - 3].value) /* _super */,
						((List<IAstMemberDeclaration>)tmStack[tmHead - 1].value) /* members */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 8:  // extends_clause ::= Lextends name_list
				tmLeft.value = ((List<List<String>>)tmStack[tmHead].value);
				break;
			case 15:  // feature_declaration ::= type_ex identifier modifiersopt defaultvalopt ';'
				tmLeft.value = new AstFeatureDeclaration(
						((AstTypeEx)tmStack[tmHead - 4].value) /* typeEx */,
						((String)tmStack[tmHead - 3].value) /* name */,
						((List<AstConstraint>)tmStack[tmHead - 2].value) /* modifiers */,
						((IAstExpression)tmStack[tmHead - 1].value) /* defaultval */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // method_declaration ::= type_ex identifier '(' parametersopt ')' ';'
				tmLeft.value = new AstMethodDeclaration(
						((AstTypeEx)tmStack[tmHead - 5].value) /* returnType */,
						((String)tmStack[tmHead - 4].value) /* name */,
						((List<AstTypeEx>)tmStack[tmHead - 2].value) /* parameters */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // parameters ::= type_ex
				tmLeft.value = new ArrayList();
				((List<AstTypeEx>)tmLeft.value).add(((AstTypeEx)tmStack[tmHead].value));
				break;
			case 20:  // parameters ::= parameters ',' type_ex
				((List<AstTypeEx>)tmLeft.value).add(((AstTypeEx)tmStack[tmHead].value));
				break;
			case 21:  // defaultval ::= '=' expression
				tmLeft.value = ((IAstExpression)tmStack[tmHead].value);
				break;
			case 22:  // constraint_list_Semicolon_separated ::= constraint_list_Semicolon_separated ';' constraint
				((List<AstConstraint>)tmLeft.value).add(((AstConstraint)tmStack[tmHead].value));
				break;
			case 23:  // constraint_list_Semicolon_separated ::= constraint
				tmLeft.value = new ArrayList();
				((List<AstConstraint>)tmLeft.value).add(((AstConstraint)tmStack[tmHead].value));
				break;
			case 24:  // modifiers ::= '[' constraint_list_Semicolon_separated ']'
				tmLeft.value = ((List<AstConstraint>)tmStack[tmHead - 1].value);
				break;
			case 25:  // constraint ::= string_constraint
				tmLeft.value = new AstConstraint(
						((AstStringConstraint)tmStack[tmHead].value) /* stringConstraint */,
						null /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 26:  // multiplicity_list_Comma_separated ::= multiplicity_list_Comma_separated ',' multiplicity
				((List<AstMultiplicity>)tmLeft.value).add(((AstMultiplicity)tmStack[tmHead].value));
				break;
			case 27:  // multiplicity_list_Comma_separated ::= multiplicity
				tmLeft.value = new ArrayList();
				((List<AstMultiplicity>)tmLeft.value).add(((AstMultiplicity)tmStack[tmHead].value));
				break;
			case 28:  // constraint ::= multiplicity_list_Comma_separated
				tmLeft.value = new AstConstraint(
						null /* stringConstraint */,
						((List<AstMultiplicity>)tmStack[tmHead].value) /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 29:  // string_constraint ::= Lset ':' strings
				tmLeft.value = new AstStringConstraint(
						AstStringConstraint.AstKindKind.LSET /* kind */,
						((List<Ast_String>)tmStack[tmHead].value) /* strings */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // string_constraint ::= Lchoice ':' strings
				tmLeft.value = new AstStringConstraint(
						AstStringConstraint.AstKindKind.LCHOICE /* kind */,
						((List<Ast_String>)tmStack[tmHead].value) /* strings */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // string_constraint ::= identifier
				tmLeft.value = new AstStringConstraint(
						null /* kind */,
						null /* strings */,
						((String)tmStack[tmHead].value) /* identifier */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // strings ::= strings ',' string
				((List<Ast_String>)tmLeft.value).add(((Ast_String)tmStack[tmHead].value));
				break;
			case 33:  // strings ::= string
				tmLeft.value = new ArrayList();
				((List<Ast_String>)tmLeft.value).add(((Ast_String)tmStack[tmHead].value));
				break;
			case 34:  // string ::= identifier
				tmLeft.value = new Ast_String(
						((String)tmStack[tmHead].value) /* identifier */,
						null /* scon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 35:  // string ::= scon
				tmLeft.value = new Ast_String(
						null /* identifier */,
						((String)tmStack[tmHead].value) /* scon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 36:  // multiplicity ::= icon
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead].value) /* lo */,
						false /* hasNoUpperBound */,
						null /* hi */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // multiplicity ::= icon '..' '*'
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead - 2].value) /* lo */,
						true /* hasNoUpperBound */,
						null /* hi */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 38:  // multiplicity ::= icon '..' icon
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead - 2].value) /* lo */,
						false /* hasNoUpperBound */,
						((Integer)tmStack[tmHead].value) /* hi */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 39:  // type_ex ::= type
				tmLeft.value = new AstTypeEx(
						((AstType)tmStack[tmHead].value) /* type */,
						null /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 40:  // type_ex ::= type '[' multiplicity_list_Comma_separated ']'
				tmLeft.value = new AstTypeEx(
						((AstType)tmStack[tmHead - 3].value) /* type */,
						((List<AstMultiplicity>)tmStack[tmHead - 1].value) /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 41:  // type ::= Lint
				tmLeft.value = new AstType(
						AstType.AstKindKind.LINT /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 42:  // type ::= Lstring
				tmLeft.value = new AstType(
						AstType.AstKindKind.LSTRING /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 43:  // type ::= Lbool
				tmLeft.value = new AstType(
						AstType.AstKindKind.LBOOL /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 44:  // type ::= name
				tmLeft.value = new AstType(
						null /* kind */,
						((List<String>)tmStack[tmHead].value) /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 45:  // type ::= name '*'
				tmLeft.value = new AstType(
						null /* kind */,
						((List<String>)tmStack[tmHead - 1].value) /* name */,
						true /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 46:  // type ::= '{' parametersopt '=>' '}'
				tmLeft.value = new AstType(
						null /* kind */,
						null /* name */,
						false /* isReference */,
						true /* isClosure */,
						((List<AstTypeEx>)tmStack[tmHead - 2].value) /* parameters */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // literal_expression ::= scon
				tmLeft.value = new AstLiteralExpression(
						((String)tmStack[tmHead].value) /* scon */,
						null /* icon */,
						null /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // literal_expression ::= icon
				tmLeft.value = new AstLiteralExpression(
						null /* scon */,
						((Integer)tmStack[tmHead].value) /* icon */,
						null /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 51:  // literal_expression ::= bcon
				tmLeft.value = new AstLiteralExpression(
						null /* scon */,
						null /* icon */,
						((Boolean)tmStack[tmHead].value) /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 52:  // list_of_identifier_and_2_elements_Comma_separated ::= list_of_identifier_and_2_elements_Comma_separated ',' identifier map_separator expression
				((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmLeft.value).add(new AstListOfIdentifierAnd2ElementsCommaSeparatedItem(
						((String)tmStack[tmHead - 2].value) /* identifier */,
						((AstMapSeparator)tmStack[tmHead - 1].value) /* mapSeparator */,
						((IAstExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset));
				break;
			case 53:  // list_of_identifier_and_2_elements_Comma_separated ::= identifier map_separator expression
				tmLeft.value = new ArrayList();
				((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmLeft.value).add(new AstListOfIdentifierAnd2ElementsCommaSeparatedItem(
						((String)tmStack[tmHead - 2].value) /* identifier */,
						((AstMapSeparator)tmStack[tmHead - 1].value) /* mapSeparator */,
						((IAstExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 56:  // structural_expression ::= name '(' list_of_identifier_and_2_elements_Comma_separated_opt ')'
				tmLeft.value = new AstStructuralExpression(
						((List<String>)tmStack[tmHead - 3].value) /* name */,
						((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmStack[tmHead - 1].value) /* mapEntries */,
						null /* expressionList */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 59:  // structural_expression ::= '[' expression_listopt ']'
				tmLeft.value = new AstStructuralExpression(
						null /* name */,
						null /* mapEntries */,
						((List<IAstExpression>)tmStack[tmHead - 1].value) /* expressionList */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 60:  // expression_list ::= expression
				tmLeft.value = new ArrayList();
				((List<IAstExpression>)tmLeft.value).add(((IAstExpression)tmStack[tmHead].value));
				break;
			case 61:  // expression_list ::= expression_list ',' expression
				((List<IAstExpression>)tmLeft.value).add(((IAstExpression)tmStack[tmHead].value));
				break;
			case 62:  // map_separator ::= ':'
				tmLeft.value = AstMapSeparator.COLON;
				break;
			case 63:  // map_separator ::= '='
				tmLeft.value = AstMapSeparator.EQUAL;
				break;
			case 64:  // map_separator ::= '=>'
				tmLeft.value = AstMapSeparator.EQUAL_GREATER;
				break;
			case 65:  // name ::= identifier
				tmLeft.value = new ArrayList();
				((List<String>)tmLeft.value).add(((String)tmStack[tmHead].value));
				break;
			case 66:  // name ::= name '.' identifier
				((List<String>)tmLeft.value).add(((String)tmStack[tmHead].value));
				break;
			case 67:  // name_list ::= name
				tmLeft.value = new ArrayList();
				((List<List<String>>)tmLeft.value).add(((List<String>)tmStack[tmHead].value));
				break;
			case 68:  // name_list ::= name_list ',' name
				((List<List<String>>)tmLeft.value).add(((List<String>)tmStack[tmHead].value));
				break;
		}
	}
}
