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
		-1, -1, -3, 2, -9, 1, -1, 4, -1, 46, -15, -23, -1, -1, -1, 30,
		32, 31, -1, 8, -1, -29, 47, -37, 5, 7, -45, 34, -1, 10, -53, 22,
		-59, -1, -1, -1, 17, 18, 19, -1, 12, -1, -1, -1, -1, -1, 15, 37,
		38, 39, -1, -1, 14, 36, 35, 13, 29, 28, 25, 26, -67, 24, -75, 16,
		42, -1, -1, -1, -1, 41, -1, 23, 43, -1, -1, -1, -1, 40, 44, -1,
		-1, 45, -1, -2,
	};

	private static final short lapg_lalr[] = {
		19, -1, 0, 0, -1, -2, 20, -1, 13, 3, -1, -2, 7, -1, 10, 48,
		13, 48, -1, -2, 10, -1, 13, 6, -1, -2, 7, -1, 8, -1, 1, 33,
		-1, -2, 7, -1, 10, 49, 13, 49, -1, -2, 17, -1, 9, 9, 12, 9,
		-1, -2, 12, -1, 9, 11, -1, -2, 6, -1, 9, 27, 18, 27, -1, -2,
		10, -1, 9, 20, 18, 20, -1, -2, 10, -1, 9, 21, 18, 21, -1, -2,
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 16, 24, 32, 37, 37, 38, 42, 44, 46, 51, 55, 56, 57, 58,
		59, 60, 66, 68, 70, 71, 76, 78, 80, 82, 84, 86, 87, 88, 90, 91,
		92, 94, 95, 96, 97, 99, 101, 103, 106, 108, 110, 115, 120, 125, 126, 127,
		132, 133, 134, 135, 136,
	};

	private static final short lapg_sym_from[] = {
		82, 1, 6, 12, 13, 14, 18, 20, 28, 43, 44, 45, 51, 67, 70, 76,
		39, 43, 44, 50, 67, 68, 75, 80, 28, 39, 42, 45, 50, 68, 75, 80,
		39, 50, 68, 75, 80, 32, 10, 21, 23, 66, 21, 42, 35, 41, 11, 60,
		62, 65, 74, 33, 34, 73, 79, 30, 8, 18, 66, 74, 26, 39, 50, 68,
		75, 80, 35, 65, 0, 2, 4, 39, 50, 68, 75, 80, 12, 18, 12, 18,
		12, 18, 28, 45, 28, 45, 0, 0, 0, 2, 4, 12, 12, 18, 30, 26,
		28, 28, 45, 28, 45, 43, 44, 43, 44, 67, 28, 45, 12, 18, 39, 50,
		68, 75, 80, 39, 50, 68, 75, 80, 39, 50, 68, 75, 80, 50, 70, 6,
		12, 14, 18, 51, 6, 4, 26, 30,
	};

	private static final short lapg_sym_to[] = {
		83, 4, 9, 9, 22, 9, 9, 26, 31, 58, 58, 31, 9, 58, 73, 79,
		47, 59, 59, 47, 59, 47, 47, 47, 32, 48, 56, 32, 48, 48, 48, 48,
		49, 49, 49, 49, 49, 42, 13, 13, 13, 13, 27, 57, 45, 55, 14, 67,
		67, 68, 76, 43, 44, 75, 80, 39, 12, 24, 70, 77, 28, 50, 50, 50,
		50, 50, 46, 69, 1, 1, 6, 51, 51, 51, 51, 51, 15, 15, 16, 16,
		17, 17, 33, 33, 34, 34, 82, 2, 3, 5, 7, 18, 19, 25, 40, 29,
		35, 36, 63, 37, 37, 60, 62, 61, 61, 71, 38, 38, 20, 20, 52, 64,
		72, 78, 81, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 65, 74, 10,
		21, 23, 21, 66, 11, 8, 30, 41,
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 0, 1, 6, 2, 2, 1, 0, 1, 0, 1, 5, 2, 3,
		3, 1, 1, 1, 3, 3, 1, 3, 1, 1, 1, 1, 3, 3, 1, 1,
		1, 1, 2, 1, 1, 1, 1, 1, 5, 3, 1, 3, 3, 5, 1, 3,
		1, 3,
	};

	private static final short lapg_rlex[] = {
		27, 28, 28, 49, 49, 29, 30, 31, 31, 50, 50, 51, 51, 32, 33, 34,
		35, 35, 36, 36, 37, 37, 37, 38, 38, 39, 39, 40, 40, 40, 41, 41,
		41, 41, 41, 42, 42, 43, 43, 43, 44, 44, 45, 45, 46, 46, 47, 47,
		48, 48,
	};

	private static final String[] lapg_syms = new String[] {
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
		"name",
		"name_list",
		"extends_clauseopt",
		"modifiersopt",
		"defaultvalopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 27;
		public static final int declarations = 28;
		public static final int type_declaration = 29;
		public static final int extends_clause = 30;
		public static final int feature_declarations = 31;
		public static final int feature_declaration = 32;
		public static final int defaultval = 33;
		public static final int modifiers = 34;
		public static final int constraints = 35;
		public static final int constraint = 36;
		public static final int string_constraint = 37;
		public static final int strings = 38;
		public static final int string = 39;
		public static final int multiplicity = 40;
		public static final int type = 41;
		public static final int expression = 42;
		public static final int literal_expression = 43;
		public static final int structural_expression = 44;
		public static final int expression_list = 45;
		public static final int map_entries = 46;
		public static final int name = 47;
		public static final int name_list = 48;
		public static final int extends_clauseopt = 49;
		public static final int modifiersopt = 50;
		public static final int defaultvalopt = 51;
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

		while (lapg_m[lapg_head].state != 83) {
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

		if (lapg_m[lapg_head].state != 83) {
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
((List<List<String>>)lapg_m[lapg_head-3].sym) /* _extends */,
((List<FeatureDeclaration>)lapg_m[lapg_head-1].sym) /* featureDeclarations */,
null /* input */, lapg_m[lapg_head-5].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 6:  // extends_clause ::= Lextends name_list
				lapg_gg.sym = ((List<List<String>>)lapg_m[lapg_head-0].sym);
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
((Integer)lapg_m[lapg_head-0].sym) /* lo */,
false,
null /* hi */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 28:  // multiplicity ::= icon '..' '*'
				lapg_gg.sym = new Multiplicity(
((Integer)lapg_m[lapg_head-2].sym) /* lo */,
true,
null /* hi */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 29:  // multiplicity ::= icon '..' icon
				lapg_gg.sym = new Multiplicity(
((Integer)lapg_m[lapg_head-2].sym) /* lo */,
false,
((Integer)lapg_m[lapg_head-0].sym) /* hi */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 30:  // type ::= Lint
				lapg_gg.sym = new Type(
Type.LINT,
false,
null /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 31:  // type ::= Lstring
				lapg_gg.sym = new Type(
Type.LSTRING,
false,
null /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 32:  // type ::= Lbool
				lapg_gg.sym = new Type(
Type.LBOOL,
false,
null /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 33:  // type ::= name
				lapg_gg.sym = new Type(
0,
false,
((List<String>)lapg_m[lapg_head-0].sym) /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 34:  // type ::= name '*'
				lapg_gg.sym = new Type(
0,
true,
((List<String>)lapg_m[lapg_head-1].sym) /* name */,
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
			case 40:  // structural_expression ::= Lnew name '(' map_entries ')'
				lapg_gg.sym = new StructuralExpression(
((List<String>)lapg_m[lapg_head-3].sym) /* name */,
((List<MapEntriesItem>)lapg_m[lapg_head-1].sym) /* mapEntries */,
null /* expressionList */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 41:  // structural_expression ::= '[' expression_list ']'
				lapg_gg.sym = new StructuralExpression(
null /* name */,
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
			case 46:  // name ::= identifier
				lapg_gg.sym = new ArrayList();
((List<String>)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym));
				break;
			case 47:  // name ::= name '.' identifier
				((List<String>)lapg_m[lapg_head-2].sym).add(((String)lapg_m[lapg_head-0].sym));
				break;
			case 48:  // name_list ::= name
				lapg_gg.sym = new ArrayList();
((List<List<String>>)lapg_gg.sym).add(((List<String>)lapg_m[lapg_head-0].sym));
				break;
			case 49:  // name_list ::= name_list ',' name
				((List<List<String>>)lapg_m[lapg_head-2].sym).add(((List<String>)lapg_m[lapg_head-0].sym));
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
