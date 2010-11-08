package org.textway.templates.types;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.templates.types.TypesLexer.ErrorReporter;
import org.textway.templates.types.TypesLexer.LapgSymbol;
import org.textway.templates.types.TypesLexer.Lexems;
import org.textway.templates.types.ast.FeatureDeclaration;
import org.textway.templates.types.ast.IConstraint;
import org.textway.templates.types.ast.IExpression;
import org.textway.templates.types.ast.Input;
import org.textway.templates.types.ast.LiteralExpression;
import org.textway.templates.types.ast.MapEntriesItem;
import org.textway.templates.types.ast.Multiplicity;
import org.textway.templates.types.ast.StringConstraint;
import org.textway.templates.types.ast.StructuralExpression;
import org.textway.templates.types.ast.Type;
import org.textway.templates.types.ast.TypeDeclaration;
import org.textway.templates.types.ast._String;

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
	private static final int lapg_action[] = {
		-1, -1, -3, 2, -9, 1, -1, 4, -1, 6, -1, -15, 30, 32, 31, -1,
		8, -1, 34, 5, 7, -21, -1, 10, -29, 22, -35, -1, -1, -1, 17, 18,
		19, -1, 12, -1, -1, -1, -1, -1, 15, 37, 38, 39, -1, -1, 14, 36,
		35, 13, 29, 28, 25, 26, -43, 24, -51, 16, 42, -1, -1, -1, -1, 41,
		-1, 23, 43, -1, -1, -1, -1, 40, 44, -1, -1, 45, -1, -2,
	};

	private static final short lapg_lalr[] = {
		18, -1, 0, 0, -1, -2, 19, -1, 12, 3, -1, -2, 7, -1, 1, 33,
		-1, -2, 16, -1, 8, 9, 11, 9, -1, -2, 11, -1, 8, 11, -1, -2,
		6, -1, 8, 27, 17, 27, -1, -2, 9, -1, 8, 20, 17, 20, -1, -2,
		9, -1, 8, 21, 17, 21, -1, -2,
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 14, 22, 30, 35, 35, 36, 38, 40, 44, 48, 49, 50, 51, 52,
		53, 59, 61, 63, 64, 69, 71, 73, 75, 77, 79, 80, 81, 83, 84, 85,
		87, 88, 89, 90, 92, 94, 96, 99, 101, 103, 108, 113, 118, 119, 120, 121,
		122, 123,
	};

	private static final short lapg_sym_from[] = {
		76, 1, 6, 10, 15, 17, 22, 37, 38, 39, 45, 61, 64, 70, 33, 37,
		38, 44, 61, 62, 69, 74, 22, 33, 36, 39, 44, 62, 69, 74, 33, 44,
		62, 69, 74, 26, 11, 36, 29, 35, 54, 56, 59, 68, 27, 28, 67, 73,
		24, 8, 15, 60, 68, 21, 33, 44, 62, 69, 74, 29, 59, 0, 2, 4,
		33, 44, 62, 69, 74, 10, 15, 10, 15, 10, 15, 22, 39, 22, 39, 0,
		0, 0, 2, 4, 10, 10, 15, 24, 21, 22, 22, 39, 22, 39, 37, 38,
		37, 38, 61, 22, 39, 10, 15, 33, 44, 62, 69, 74, 33, 44, 62, 69,
		74, 33, 44, 62, 69, 74, 44, 64, 4, 21, 24,
	};

	private static final short lapg_sym_to[] = {
		77, 4, 9, 11, 11, 21, 25, 52, 52, 25, 60, 52, 67, 73, 41, 53,
		53, 41, 53, 41, 41, 41, 26, 42, 50, 26, 42, 42, 42, 42, 43, 43,
		43, 43, 43, 36, 18, 51, 39, 49, 61, 61, 62, 70, 37, 38, 69, 74,
		33, 10, 19, 64, 71, 22, 44, 44, 44, 44, 44, 40, 63, 1, 1, 6,
		45, 45, 45, 45, 45, 12, 12, 13, 13, 14, 14, 27, 27, 28, 28, 76,
		2, 3, 5, 7, 15, 16, 20, 34, 23, 29, 30, 57, 31, 31, 54, 56,
		55, 55, 65, 32, 32, 17, 17, 46, 58, 66, 72, 75, 47, 47, 47, 47,
		47, 48, 48, 48, 48, 48, 59, 68, 8, 24, 35,
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 0, 1, 6, 2, 2, 1, 0, 1, 0, 1, 5, 2, 3,
		3, 1, 1, 1, 3, 3, 1, 3, 1, 1, 1, 1, 3, 3, 1, 1,
		1, 1, 2, 1, 1, 1, 1, 1, 5, 3, 1, 3, 3, 5,
	};

	private static final short lapg_rlex[] = {
		26, 27, 27, 46, 46, 28, 29, 30, 30, 47, 47, 48, 48, 31, 32, 33,
		34, 34, 35, 35, 36, 36, 36, 37, 37, 38, 38, 39, 39, 39, 40, 40,
		40, 40, 40, 41, 41, 42, 42, 42, 43, 43, 44, 44, 45, 45,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"scon",
		"icon",
		"bcon",
		"_skip",
		"'..'",
		"'*'",
		"';'",
		"','",
		"':'",
		"'='",
		"'{'",
		"'}'",
		"'('",
		"')'",
		"'['",
		"']'",
		"Lclass",
		"Lextends",
		"Lnew",
		"Lint",
		"Lbool",
		"Lstring",
		"Lset",
		"Lchoice",
		"input",
		"declarations",
		"type_declaration",
		"extends_clause",
		"feature_declarations",
		"feature_declaration",
		"defaultval",
		"modifiers",
		"constraints",
		"constraint",
		"string_constraint",
		"strings",
		"string",
		"multiplicity",
		"type",
		"expression",
		"literal_expression",
		"structural_expression",
		"expression_list",
		"map_entries",
		"extends_clauseopt",
		"modifiersopt",
		"defaultvalopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 26;
		public static final int declarations = 27;
		public static final int type_declaration = 28;
		public static final int extends_clause = 29;
		public static final int feature_declarations = 30;
		public static final int feature_declaration = 31;
		public static final int defaultval = 32;
		public static final int modifiers = 33;
		public static final int constraints = 34;
		public static final int constraint = 35;
		public static final int string_constraint = 36;
		public static final int strings = 37;
		public static final int string = 38;
		public static final int multiplicity = 39;
		public static final int type = 40;
		public static final int expression = 41;
		public static final int literal_expression = 42;
		public static final int structural_expression = 43;
		public static final int expression_list = 44;
		public static final int map_entries = 45;
		public static final int extends_clauseopt = 46;
		public static final int modifiersopt = 47;
		public static final int defaultvalopt = 48;
	}

	private static int lapg_next(int state, int symbol) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == symbol) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	private static int lapg_state_sym(int state, int symbol) {
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

	private int lapg_head;
	private LapgSymbol[] lapg_m;
	private LapgSymbol lapg_n;

	public Input parse(TypesLexer lexer) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 77) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state, lapg_n.lexem);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift(lexer);
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != 77) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		}
		return (Input)lapg_m[lapg_head - 1].sym;
	}

	private void shift(TypesLexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lexer.next();
		}
	}

	@SuppressWarnings("unchecked")
	private void reduce(int rule) {
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
		switch (rule) {
			case 0:  // input ::= declarations
				lapg_gg.sym = new Input(
((List<TypeDeclaration>)lapg_m[lapg_head-0].sym) /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 1:  // declarations ::= declarations type_declaration
				((List<TypeDeclaration>)lapg_m[lapg_head-1].sym).add(((TypeDeclaration)lapg_m[lapg_head-0].sym));
				break;
			case 2:  // declarations ::= type_declaration
				lapg_gg.sym = new ArrayList();
((List<TypeDeclaration>)lapg_gg.sym).add(((TypeDeclaration)lapg_m[lapg_head-0].sym));
				break;
			case 5:  // type_declaration ::= Lclass identifier extends_clauseopt '{' feature_declarations '}'
				lapg_gg.sym = new TypeDeclaration(
((String)lapg_m[lapg_head-4].sym) /* name */,
((String)lapg_m[lapg_head-3].sym) /* _extends */,
((List<FeatureDeclaration>)lapg_m[lapg_head-1].sym) /* featureDeclarations */,
null /* input */, lapg_m[lapg_head-5].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 6:  // extends_clause ::= Lextends identifier
				lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym);
				break;
			case 7:  // feature_declarations ::= feature_declarations feature_declaration
				((List<FeatureDeclaration>)lapg_m[lapg_head-1].sym).add(((FeatureDeclaration)lapg_m[lapg_head-0].sym));
				break;
			case 8:  // feature_declarations ::= feature_declaration
				lapg_gg.sym = new ArrayList();
((List<FeatureDeclaration>)lapg_gg.sym).add(((FeatureDeclaration)lapg_m[lapg_head-0].sym));
				break;
			case 13:  // feature_declaration ::= type identifier modifiersopt defaultvalopt ';'
				lapg_gg.sym = new FeatureDeclaration(
((String)lapg_m[lapg_head-3].sym) /* name */,
((Type)lapg_m[lapg_head-4].sym) /* type */,
((List<IConstraint>)lapg_m[lapg_head-2].sym) /* modifiersopt */,
((IExpression)lapg_m[lapg_head-1].sym) /* defaultvalopt */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 14:  // defaultval ::= '=' expression
				lapg_gg.sym = ((IExpression)lapg_m[lapg_head-0].sym);
				break;
			case 15:  // modifiers ::= '[' constraints ']'
				lapg_gg.sym = ((List<IConstraint>)lapg_m[lapg_head-1].sym);
				break;
			case 16:  // constraints ::= constraints ';' constraint
				((List<IConstraint>)lapg_m[lapg_head-2].sym).add(((IConstraint)lapg_m[lapg_head-0].sym));
				break;
			case 17:  // constraints ::= constraint
				lapg_gg.sym = new ArrayList();
((List<IConstraint>)lapg_gg.sym).add(((IConstraint)lapg_m[lapg_head-0].sym));
				break;
			case 20:  // string_constraint ::= Lset ':' strings
				lapg_gg.sym = new StringConstraint(
StringConstraint.LSET,
((List<_String>)lapg_m[lapg_head-0].sym) /* strings */,
null /* identifier */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 21:  // string_constraint ::= Lchoice ':' strings
				lapg_gg.sym = new StringConstraint(
StringConstraint.LCHOICE,
((List<_String>)lapg_m[lapg_head-0].sym) /* strings */,
null /* identifier */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 22:  // string_constraint ::= identifier
				lapg_gg.sym = new StringConstraint(
0,
null /* strings */,
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 23:  // strings ::= strings ',' string
				((List<_String>)lapg_m[lapg_head-2].sym).add(((_String)lapg_m[lapg_head-0].sym));
				break;
			case 24:  // strings ::= string
				lapg_gg.sym = new ArrayList();
((List<_String>)lapg_gg.sym).add(((_String)lapg_m[lapg_head-0].sym));
				break;
			case 25:  // string ::= identifier
				lapg_gg.sym = new _String(
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* scon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 26:  // string ::= scon
				lapg_gg.sym = new _String(
null /* identifier */,
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 27:  // multiplicity ::= icon
				lapg_gg.sym = new Multiplicity(
false,
((Integer)lapg_m[lapg_head-0].sym) /* icon */,
null /* icon2 */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 28:  // multiplicity ::= icon '..' '*'
				lapg_gg.sym = new Multiplicity(
true,
((Integer)lapg_m[lapg_head-2].sym) /* icon */,
null /* icon2 */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 29:  // multiplicity ::= icon '..' icon
				lapg_gg.sym = new Multiplicity(
false,
((Integer)lapg_m[lapg_head-2].sym) /* icon */,
((Integer)lapg_m[lapg_head-0].sym) /* icon2 */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 30:  // type ::= Lint
				lapg_gg.sym = new Type(
Type.LINT,
false,
null /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 31:  // type ::= Lstring
				lapg_gg.sym = new Type(
Type.LSTRING,
false,
null /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 32:  // type ::= Lbool
				lapg_gg.sym = new Type(
Type.LBOOL,
false,
null /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 33:  // type ::= identifier
				lapg_gg.sym = new Type(
0,
false,
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 34:  // type ::= identifier '*'
				lapg_gg.sym = new Type(
0,
true,
((String)lapg_m[lapg_head-1].sym) /* identifier */,
null /* input */, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 37:  // literal_expression ::= scon
				lapg_gg.sym = new LiteralExpression(
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* icon */,
null /* bcon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 38:  // literal_expression ::= icon
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
((Integer)lapg_m[lapg_head-0].sym) /* icon */,
null /* bcon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 39:  // literal_expression ::= bcon
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
null /* icon */,
((Boolean)lapg_m[lapg_head-0].sym) /* bcon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 40:  // structural_expression ::= Lnew identifier '(' map_entries ')'
				lapg_gg.sym = new StructuralExpression(
((String)lapg_m[lapg_head-3].sym) /* identifier */,
((List<MapEntriesItem>)lapg_m[lapg_head-1].sym) /* mapEntries */,
null /* expressionList */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 41:  // structural_expression ::= '[' expression_list ']'
				lapg_gg.sym = new StructuralExpression(
null /* identifier */,
null /* mapEntries */,
((List<IExpression>)lapg_m[lapg_head-1].sym) /* expressionList */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 42:  // expression_list ::= expression
				lapg_gg.sym = new ArrayList();
((List<IExpression>)lapg_gg.sym).add(((IExpression)lapg_m[lapg_head-0].sym));
				break;
			case 43:  // expression_list ::= expression_list ',' expression
				((List<IExpression>)lapg_m[lapg_head-2].sym).add(((IExpression)lapg_m[lapg_head-0].sym));
				break;
			case 44:  // map_entries ::= identifier ':' expression
				lapg_gg.sym = new ArrayList();
((List<MapEntriesItem>)lapg_gg.sym).add(new MapEntriesItem(
((String)lapg_m[lapg_head-2].sym) /* identifier */,
((IExpression)lapg_m[lapg_head-0].sym) /* expression */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 45:  // map_entries ::= map_entries ',' identifier ':' expression
				((List<MapEntriesItem>)lapg_m[lapg_head-4].sym).add(new MapEntriesItem(
((String)lapg_m[lapg_head-2].sym) /* identifier */,
((IExpression)lapg_m[lapg_head-0].sym) /* expression */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset));
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
