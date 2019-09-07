/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
		"\67\0\ufff1\uffff\uffe9\uffff\4\0\uffff\uffff\uffff\uffff\uffff\uffff\70\0\uffe3" +
		"\uffff\uffdb\uffff\5\0\41\0\43\0\42\0\3\0\7\0\10\0\uffff\uffff\uffcd\uffff\uffc1" +
		"\uffff\uffb1\uffff\13\0\uffff\uffff\uffa9\uffff\uffff\uffff\45\0\uffff\uffff\uffff" +
		"\uffff\uff9f\uffff\uffff\uffff\75\0\uff91\uffff\uff8b\uffff\uffff\uffff\24\0\14\0" +
		"\46\0\uffff\uffff\27\0\uffff\uffff\uffff\uffff\uffff\uffff\17\0\uff81\uffff\21\0" +
		"\uffff\uffff\77\0\uffff\uffff\uffff\uffff\uffff\uffff\40\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\20\0\51\0\52\0\53\0\uff79\uffff\15\0\50\0\47\0\uffff\uffff" +
		"\11\0\36\0\35\0\23\0\12\0\32\0\33\0\uff6b\uffff\31\0\uff63\uffff\16\0\62\0\uff5b" +
		"\uffff\uffff\uffff\uff55\uffff\uffff\uffff\uffff\uffff\61\0\uffff\uffff\uff4f\uffff" +
		"\uffff\uffff\30\0\63\0\64\0\65\0\66\0\uffff\uffff\uffff\uffff\60\0\55\0\uffff\uffff" +
		"\uffff\uffff\54\0\uffff\uffff\ufffe\uffff");

	private static final int[] tmLalr = TypesLexer.unpack_int(180,
		"\24\0\uffff\uffff\0\0\0\0\uffff\uffff\ufffe\uffff\25\0\uffff\uffff\16\0\74\0\uffff" +
		"\uffff\ufffe\uffff\7\0\uffff\uffff\12\0\71\0\16\0\71\0\uffff\uffff\ufffe\uffff\12" +
		"\0\uffff\uffff\16\0\6\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\12\0\72\0\16\0\72" +
		"\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\16\0\uffff\uffff\26\0\uffff\uffff\27\0" +
		"\uffff\uffff\30\0\uffff\uffff\15\0\102\0\uffff\uffff\ufffe\uffff\22\0\uffff\uffff" +
		"\1\0\37\0\12\0\37\0\15\0\37\0\21\0\37\0\uffff\uffff\ufffe\uffff\7\0\uffff\uffff\10" +
		"\0\uffff\uffff\1\0\44\0\12\0\44\0\15\0\44\0\21\0\44\0\22\0\44\0\uffff\uffff\ufffe" +
		"\uffff\12\0\uffff\uffff\15\0\101\0\21\0\101\0\uffff\uffff\ufffe\uffff\20\0\uffff" +
		"\uffff\22\0\uffff\uffff\11\0\76\0\14\0\76\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff" +
		"\16\0\uffff\uffff\26\0\uffff\uffff\27\0\uffff\uffff\30\0\uffff\uffff\21\0\102\0\uffff" +
		"\uffff\ufffe\uffff\14\0\uffff\uffff\11\0\100\0\uffff\uffff\ufffe\uffff\6\0\uffff" +
		"\uffff\11\0\34\0\12\0\34\0\23\0\34\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\11" +
		"\0\22\0\23\0\22\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\2\0\uffff\uffff\3\0\uffff" +
		"\uffff\4\0\uffff\uffff\22\0\uffff\uffff\23\0\104\0\uffff\uffff\ufffe\uffff\12\0\uffff" +
		"\uffff\11\0\25\0\23\0\25\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\11\0\26\0\23" +
		"\0\26\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\23\0\103\0\uffff\uffff\ufffe\uffff" +
		"\1\0\uffff\uffff\21\0\57\0\uffff\uffff\ufffe\uffff\12\0\uffff\uffff\21\0\56\0\uffff" +
		"\uffff\ufffe\uffff");

	private static final int[] tmGoto = TypesLexer.unpack_int(62,
		"\0\0\2\0\54\0\74\0\120\0\132\0\132\0\134\0\144\0\150\0\156\0\176\0\206\0\214\0\222" +
		"\0\234\0\240\0\244\0\250\0\266\0\274\0\300\0\302\0\312\0\322\0\332\0\336\0\342\0" +
		"\344\0\346\0\350\0\354\0\356\0\360\0\362\0\364\0\370\0\372\0\374\0\376\0\u0102\0" +
		"\u0108\0\u010c\0\u0110\0\u0116\0\u011e\0\u0126\0\u012e\0\u0138\0\u0142\0\u0144\0" +
		"\u0146\0\u0150\0\u0152\0\u0156\0\u016c\0\u016e\0\u0170\0\u0172\0\u0174\0\u0178\0" +
		"\u017a\0");

	private static final int[] tmFromTo = TypesLexer.unpack_int(378,
		"\152\0\153\0\1\0\4\0\6\0\11\0\15\0\20\0\16\0\11\0\17\0\11\0\22\0\11\0\32\0\40\0\43" +
		"\0\11\0\45\0\11\0\46\0\57\0\66\0\11\0\75\0\116\0\76\0\116\0\77\0\57\0\104\0\11\0" +
		"\127\0\133\0\130\0\116\0\131\0\11\0\143\0\11\0\144\0\147\0\150\0\11\0\66\0\101\0" +
		"\75\0\117\0\76\0\117\0\104\0\101\0\130\0\117\0\131\0\101\0\143\0\101\0\150\0\101" +
		"\0\41\0\51\0\46\0\51\0\66\0\102\0\71\0\112\0\72\0\51\0\77\0\51\0\104\0\102\0\131" +
		"\0\102\0\143\0\102\0\150\0\102\0\66\0\103\0\104\0\103\0\131\0\103\0\143\0\103\0\150" +
		"\0\103\0\51\0\71\0\12\0\15\0\21\0\15\0\34\0\15\0\110\0\15\0\34\0\42\0\71\0\113\0" +
		"\62\0\77\0\70\0\111\0\74\0\115\0\13\0\16\0\35\0\43\0\52\0\72\0\64\0\72\0\120\0\130" +
		"\0\122\0\130\0\125\0\131\0\134\0\144\0\60\0\75\0\61\0\76\0\133\0\140\0\147\0\140" +
		"\0\50\0\66\0\133\0\141\0\147\0\141\0\37\0\44\0\133\0\142\0\147\0\142\0\10\0\14\0" +
		"\17\0\22\0\22\0\22\0\43\0\22\0\45\0\22\0\17\0\23\0\44\0\55\0\40\0\45\0\110\0\127" +
		"\0\56\0\74\0\135\0\145\0\33\0\41\0\40\0\46\0\66\0\104\0\104\0\104\0\131\0\104\0\143" +
		"\0\104\0\150\0\104\0\52\0\73\0\62\0\100\0\126\0\132\0\0\0\1\0\2\0\1\0\4\0\6\0\17" +
		"\0\24\0\22\0\24\0\43\0\24\0\45\0\24\0\17\0\25\0\22\0\25\0\43\0\25\0\45\0\25\0\17" +
		"\0\26\0\22\0\26\0\43\0\26\0\45\0\26\0\46\0\60\0\77\0\60\0\46\0\61\0\77\0\61\0\0\0" +
		"\152\0\0\0\2\0\14\0\17\0\0\0\3\0\2\0\5\0\4\0\7\0\17\0\27\0\17\0\30\0\17\0\31\0\22" +
		"\0\35\0\45\0\35\0\50\0\67\0\46\0\62\0\40\0\47\0\46\0\63\0\77\0\123\0\41\0\52\0\46" +
		"\0\64\0\77\0\64\0\46\0\65\0\77\0\65\0\75\0\120\0\76\0\122\0\75\0\121\0\76\0\121\0" +
		"\130\0\136\0\41\0\53\0\46\0\53\0\72\0\114\0\77\0\53\0\17\0\32\0\22\0\36\0\43\0\54" +
		"\0\45\0\36\0\17\0\33\0\22\0\33\0\43\0\33\0\45\0\33\0\66\0\105\0\104\0\124\0\131\0" +
		"\137\0\143\0\146\0\150\0\151\0\66\0\106\0\104\0\106\0\131\0\106\0\143\0\106\0\150" +
		"\0\106\0\127\0\134\0\127\0\135\0\66\0\107\0\104\0\107\0\131\0\107\0\143\0\107\0\150" +
		"\0\107\0\104\0\125\0\133\0\143\0\147\0\150\0\6\0\12\0\16\0\21\0\17\0\34\0\22\0\34" +
		"\0\43\0\34\0\45\0\34\0\66\0\110\0\104\0\110\0\131\0\110\0\143\0\110\0\150\0\110\0" +
		"\6\0\13\0\4\0\10\0\40\0\50\0\50\0\70\0\22\0\37\0\45\0\56\0\104\0\126\0");

	private static final int[] tmRuleLen = TypesLexer.unpack_int(69,
		"\1\0\2\0\1\0\2\0\0\0\6\0\2\0\1\0\1\0\5\0\6\0\1\0\3\0\2\0\3\0\1\0\3\0\1\0\1\0\3\0" +
		"\1\0\3\0\3\0\1\0\3\0\1\0\1\0\1\0\1\0\3\0\3\0\1\0\4\0\1\0\1\0\1\0\1\0\2\0\4\0\1\0" +
		"\1\0\1\0\1\0\1\0\5\0\3\0\1\0\0\0\4\0\3\0\1\0\3\0\1\0\1\0\1\0\1\0\3\0\1\0\3\0\1\0" +
		"\0\0\1\0\0\0\1\0\0\0\1\0\0\0\1\0\0\0");

	private static final int[] tmRuleSymbol = TypesLexer.unpack_int(69,
		"\33\0\34\0\34\0\35\0\35\0\36\0\37\0\40\0\40\0\41\0\42\0\43\0\43\0\44\0\45\0\45\0" +
		"\46\0\47\0\47\0\50\0\50\0\51\0\51\0\51\0\52\0\52\0\53\0\53\0\54\0\54\0\54\0\55\0" +
		"\55\0\56\0\56\0\56\0\56\0\56\0\56\0\57\0\57\0\60\0\60\0\60\0\61\0\61\0\62\0\62\0" +
		"\63\0\63\0\64\0\64\0\65\0\65\0\65\0\66\0\66\0\67\0\67\0\70\0\70\0\71\0\71\0\72\0" +
		"\72\0\73\0\73\0\74\0\74\0");

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

	protected static int gotoState(int state, int symbol) {
		int min = tmGoto[symbol], max = tmGoto[symbol + 1];
		int i, e;

		while (min < max) {
			e = (min + max) >> 2 << 1;
			i = tmFromTo[e];
			if (i == state) {
				return tmFromTo[e+1];
			} else if (i < state) {
				min = e + 2;
			} else {
				max = e;
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
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, tmNext.symbol);
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
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
		switch (ruleIndex) {
			case 0:  // input : declarations
				tmLeft.value = new AstInput(
						((List<AstTypeDeclaration>)tmStack[tmHead].value) /* declarations */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 1:  // declarations : declarations type_declaration
				((List<AstTypeDeclaration>)tmLeft.value).add(((AstTypeDeclaration)tmStack[tmHead].value));
				break;
			case 2:  // declarations : type_declaration
				tmLeft.value = new ArrayList();
				((List<AstTypeDeclaration>)tmLeft.value).add(((AstTypeDeclaration)tmStack[tmHead].value));
				break;
			case 3:  // member_declaration_optlist : member_declaration_optlist member_declaration
				((List<IAstMemberDeclaration>)tmLeft.value).add(((IAstMemberDeclaration)tmStack[tmHead].value));
				break;
			case 4:  // member_declaration_optlist :
				tmLeft.value = new ArrayList();
				break;
			case 5:  // type_declaration : Lclass identifier extends_clauseopt '{' member_declaration_optlist '}'
				tmLeft.value = new AstTypeDeclaration(
						((String)tmStack[tmHead - 4].value) /* name */,
						((List<List<String>>)tmStack[tmHead - 3].value) /* _super */,
						((List<IAstMemberDeclaration>)tmStack[tmHead - 1].value) /* members */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 6:  // extends_clause : Lextends name_list
				tmLeft.value = ((List<List<String>>)tmStack[tmHead].value);
				break;
			case 9:  // feature_declaration : type_ex identifier modifiersopt defaultvalopt ';'
				tmLeft.value = new AstFeatureDeclaration(
						((AstTypeEx)tmStack[tmHead - 4].value) /* typeEx */,
						((String)tmStack[tmHead - 3].value) /* name */,
						((List<AstConstraint>)tmStack[tmHead - 2].value) /* modifiers */,
						((IAstExpression)tmStack[tmHead - 1].value) /* defaultval */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset);
				break;
			case 10:  // method_declaration : type_ex identifier '(' parametersopt ')' ';'
				tmLeft.value = new AstMethodDeclaration(
						((AstTypeEx)tmStack[tmHead - 5].value) /* returnType */,
						((String)tmStack[tmHead - 4].value) /* name */,
						((List<AstTypeEx>)tmStack[tmHead - 2].value) /* parameters */,
						null /* input */, tmStack[tmHead - 5].line, tmStack[tmHead - 5].offset, tmStack[tmHead].endoffset);
				break;
			case 11:  // parameters : type_ex
				tmLeft.value = new ArrayList();
				((List<AstTypeEx>)tmLeft.value).add(((AstTypeEx)tmStack[tmHead].value));
				break;
			case 12:  // parameters : parameters ',' type_ex
				((List<AstTypeEx>)tmLeft.value).add(((AstTypeEx)tmStack[tmHead].value));
				break;
			case 13:  // defaultval : '=' expression
				tmLeft.value = ((IAstExpression)tmStack[tmHead].value);
				break;
			case 14:  // constraint_list_Semicolon_separated : constraint_list_Semicolon_separated ';' constraint
				((List<AstConstraint>)tmLeft.value).add(((AstConstraint)tmStack[tmHead].value));
				break;
			case 15:  // constraint_list_Semicolon_separated : constraint
				tmLeft.value = new ArrayList();
				((List<AstConstraint>)tmLeft.value).add(((AstConstraint)tmStack[tmHead].value));
				break;
			case 16:  // modifiers : '[' constraint_list_Semicolon_separated ']'
				tmLeft.value = ((List<AstConstraint>)tmStack[tmHead - 1].value);
				break;
			case 17:  // constraint : string_constraint
				tmLeft.value = new AstConstraint(
						((AstStringConstraint)tmStack[tmHead].value) /* stringConstraint */,
						null /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 18:  // constraint : multiplicity_list_Comma_separated
				tmLeft.value = new AstConstraint(
						null /* stringConstraint */,
						((List<AstMultiplicity>)tmStack[tmHead].value) /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 19:  // multiplicity_list_Comma_separated : multiplicity_list_Comma_separated ',' multiplicity
				((List<AstMultiplicity>)tmLeft.value).add(((AstMultiplicity)tmStack[tmHead].value));
				break;
			case 20:  // multiplicity_list_Comma_separated : multiplicity
				tmLeft.value = new ArrayList();
				((List<AstMultiplicity>)tmLeft.value).add(((AstMultiplicity)tmStack[tmHead].value));
				break;
			case 21:  // string_constraint : Lset ':' strings
				tmLeft.value = new AstStringConstraint(
						AstStringConstraint.AstKindKind.LSET /* kind */,
						((List<Ast_String>)tmStack[tmHead].value) /* strings */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 22:  // string_constraint : Lchoice ':' strings
				tmLeft.value = new AstStringConstraint(
						AstStringConstraint.AstKindKind.LCHOICE /* kind */,
						((List<Ast_String>)tmStack[tmHead].value) /* strings */,
						null /* identifier */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 23:  // string_constraint : identifier
				tmLeft.value = new AstStringConstraint(
						null /* kind */,
						null /* strings */,
						((String)tmStack[tmHead].value) /* identifier */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 24:  // strings : strings ',' string
				((List<Ast_String>)tmLeft.value).add(((Ast_String)tmStack[tmHead].value));
				break;
			case 25:  // strings : string
				tmLeft.value = new ArrayList();
				((List<Ast_String>)tmLeft.value).add(((Ast_String)tmStack[tmHead].value));
				break;
			case 26:  // string : identifier
				tmLeft.value = new Ast_String(
						((String)tmStack[tmHead].value) /* identifier */,
						null /* scon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 27:  // string : scon
				tmLeft.value = new Ast_String(
						null /* identifier */,
						((String)tmStack[tmHead].value) /* scon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 28:  // multiplicity : icon
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead].value) /* lo */,
						false /* hasNoUpperBound */,
						null /* hi */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 29:  // multiplicity : icon '..' '*'
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead - 2].value) /* lo */,
						true /* hasNoUpperBound */,
						null /* hi */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 30:  // multiplicity : icon '..' icon
				tmLeft.value = new AstMultiplicity(
						((Integer)tmStack[tmHead - 2].value) /* lo */,
						false /* hasNoUpperBound */,
						((Integer)tmStack[tmHead].value) /* hi */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 31:  // type_ex : type
				tmLeft.value = new AstTypeEx(
						((AstType)tmStack[tmHead].value) /* type */,
						null /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 32:  // type_ex : type '[' multiplicity_list_Comma_separated ']'
				tmLeft.value = new AstTypeEx(
						((AstType)tmStack[tmHead - 3].value) /* type */,
						((List<AstMultiplicity>)tmStack[tmHead - 1].value) /* multiplicityListCommaSeparated */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 33:  // type : Lint
				tmLeft.value = new AstType(
						AstType.AstKindKind.LINT /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 34:  // type : Lstring
				tmLeft.value = new AstType(
						AstType.AstKindKind.LSTRING /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 35:  // type : Lbool
				tmLeft.value = new AstType(
						AstType.AstKindKind.LBOOL /* kind */,
						null /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 36:  // type : name
				tmLeft.value = new AstType(
						null /* kind */,
						((List<String>)tmStack[tmHead].value) /* name */,
						false /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 37:  // type : name '*'
				tmLeft.value = new AstType(
						null /* kind */,
						((List<String>)tmStack[tmHead - 1].value) /* name */,
						true /* isReference */,
						false /* isClosure */,
						null /* parameters */,
						null /* input */, tmStack[tmHead - 1].line, tmStack[tmHead - 1].offset, tmStack[tmHead].endoffset);
				break;
			case 38:  // type : '{' parametersopt '=>' '}'
				tmLeft.value = new AstType(
						null /* kind */,
						null /* name */,
						false /* isReference */,
						true /* isClosure */,
						((List<AstTypeEx>)tmStack[tmHead - 2].value) /* parameters */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 41:  // literal_expression : scon
				tmLeft.value = new AstLiteralExpression(
						((String)tmStack[tmHead].value) /* scon */,
						null /* icon */,
						null /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 42:  // literal_expression : icon
				tmLeft.value = new AstLiteralExpression(
						null /* scon */,
						((Integer)tmStack[tmHead].value) /* icon */,
						null /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 43:  // literal_expression : bcon
				tmLeft.value = new AstLiteralExpression(
						null /* scon */,
						null /* icon */,
						((Boolean)tmStack[tmHead].value) /* bcon */,
						null /* input */, tmStack[tmHead].line, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
				break;
			case 44:  // list_of_identifier_and_2_elements_Comma_separated : list_of_identifier_and_2_elements_Comma_separated ',' identifier map_separator expression
				((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmLeft.value).add(new AstListOfIdentifierAnd2ElementsCommaSeparatedItem(
						((String)tmStack[tmHead - 2].value) /* identifier */,
						((AstMapSeparator)tmStack[tmHead - 1].value) /* mapSeparator */,
						((IAstExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 4].line, tmStack[tmHead - 4].offset, tmStack[tmHead].endoffset));
				break;
			case 45:  // list_of_identifier_and_2_elements_Comma_separated : identifier map_separator expression
				tmLeft.value = new ArrayList();
				((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmLeft.value).add(new AstListOfIdentifierAnd2ElementsCommaSeparatedItem(
						((String)tmStack[tmHead - 2].value) /* identifier */,
						((AstMapSeparator)tmStack[tmHead - 1].value) /* mapSeparator */,
						((IAstExpression)tmStack[tmHead].value) /* expression */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset));
				break;
			case 48:  // structural_expression : name '(' list_of_identifier_and_2_elements_Comma_separated_opt ')'
				tmLeft.value = new AstStructuralExpression(
						((List<String>)tmStack[tmHead - 3].value) /* name */,
						((List<AstListOfIdentifierAnd2ElementsCommaSeparatedItem>)tmStack[tmHead - 1].value) /* mapEntries */,
						null /* expressionList */,
						null /* input */, tmStack[tmHead - 3].line, tmStack[tmHead - 3].offset, tmStack[tmHead].endoffset);
				break;
			case 49:  // structural_expression : '[' expression_listopt ']'
				tmLeft.value = new AstStructuralExpression(
						null /* name */,
						null /* mapEntries */,
						((List<IAstExpression>)tmStack[tmHead - 1].value) /* expressionList */,
						null /* input */, tmStack[tmHead - 2].line, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
				break;
			case 50:  // expression_list : expression
				tmLeft.value = new ArrayList();
				((List<IAstExpression>)tmLeft.value).add(((IAstExpression)tmStack[tmHead].value));
				break;
			case 51:  // expression_list : expression_list ',' expression
				((List<IAstExpression>)tmLeft.value).add(((IAstExpression)tmStack[tmHead].value));
				break;
			case 52:  // map_separator : ':'
				tmLeft.value = AstMapSeparator.COLON;
				break;
			case 53:  // map_separator : '='
				tmLeft.value = AstMapSeparator.ASSIGN;
				break;
			case 54:  // map_separator : '=>'
				tmLeft.value = AstMapSeparator.ASSIGN_GT;
				break;
			case 55:  // name : identifier
				tmLeft.value = new ArrayList();
				((List<String>)tmLeft.value).add(((String)tmStack[tmHead].value));
				break;
			case 56:  // name : name '.' identifier
				((List<String>)tmLeft.value).add(((String)tmStack[tmHead].value));
				break;
			case 57:  // name_list : name
				tmLeft.value = new ArrayList();
				((List<List<String>>)tmLeft.value).add(((List<String>)tmStack[tmHead].value));
				break;
			case 58:  // name_list : name_list ',' name
				((List<List<String>>)tmLeft.value).add(((List<String>)tmStack[tmHead].value));
				break;
		}
	}
}
