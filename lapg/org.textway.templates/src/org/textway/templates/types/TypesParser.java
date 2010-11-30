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
		-1, -1, -3, 2, -9, 1, -1, 4, -1, 52, -15, -23, -29, -1, -1, 32,
		34, 33, -41, 10, -1, -53, -1, 53, -61, 9, -69, 36, 7, -1, 12, -77,
		24, -83, -1, -1, -1, 19, 20, 21, -1, 14, -1, -1, -1, -1, -1, 17,
		39, 40, 41, -91, 16, 38, 37, -1, 15, 31, 30, 27, 28, -105, 26, -113,
		18, 48, -121, -1, -127, -1, -1, 47, -1, -133, -1, 25, 49, -1, -1, 44,
		50, -1, -1, 51, -1, -2
	};

	private static final short lapg_lalr[] = {
		19, -1, 0, 0, -1, -2, 20, -1, 13, 3, -1, -2, 7, -1, 10, 54,
		13, 54, -1, -2, 10, -1, 13, 8, -1, -2, 1, -1, 21, -1, 22, -1,
		23, -1, 14, 5, -1, -2, 1, -1, 21, -1, 22, -1, 23, -1, 14, 6,
		-1, -2, 7, -1, 8, -1, 1, 35, -1, -2, 7, -1, 10, 55, 13, 55,
		-1, -2, 17, -1, 9, 11, 12, 11, -1, -2, 12, -1, 9, 13, -1, -2,
		6, -1, 9, 29, 18, 29, -1, -2, 1, -1, 2, -1, 3, -1, 4, -1,
		17, -1, 18, 45, -1, -2, 10, -1, 9, 22, 18, 22, -1, -2, 10, -1,
		9, 23, 18, 23, -1, -2, 10, -1, 18, 46, -1, -2, 1, -1, 14, 42,
		-1, -2, 10, -1, 14, 43, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 20, 28, 36, 41, 41, 42, 46, 48, 50, 55, 59, 60, 62, 64,
		64, 64, 70, 72, 74, 75, 77, 79, 81, 83, 85, 86, 87, 89, 90, 91,
		93, 94, 95, 96, 98, 100, 102, 105, 107, 109, 114, 119, 124, 125, 126, 135,
		136, 137, 138, 139, 140, 141, 142
	};

	private static final short lapg_sym_from[] = {
		84, 1, 6, 12, 13, 14, 18, 20, 29, 40, 44, 45, 46, 51, 68, 69,
		70, 77, 78, 82, 40, 44, 45, 51, 69, 70, 77, 82, 29, 40, 43, 46,
		51, 70, 77, 82, 40, 51, 70, 77, 82, 33, 10, 21, 24, 55, 21, 43,
		36, 42, 11, 61, 63, 66, 73, 34, 35, 72, 81, 31, 8, 55, 22, 74,
		26, 40, 51, 70, 77, 82, 36, 67, 0, 2, 4, 12, 18, 12, 18, 12,
		18, 29, 46, 29, 46, 0, 0, 0, 2, 4, 12, 12, 18, 31, 26, 29,
		29, 46, 29, 46, 44, 45, 44, 45, 69, 29, 46, 12, 18, 40, 51, 70,
		77, 82, 40, 51, 70, 77, 82, 40, 51, 70, 77, 82, 51, 68, 6, 12,
		14, 18, 40, 51, 70, 77, 82, 6, 4, 12, 26, 31, 68, 51
	};

	private static final short lapg_sym_to[] = {
		85, 4, 9, 9, 23, 9, 9, 26, 32, 9, 59, 59, 32, 9, 72, 59,
		9, 9, 81, 9, 48, 60, 60, 48, 60, 48, 48, 48, 33, 49, 57, 33,
		49, 49, 49, 49, 50, 50, 50, 50, 50, 43, 13, 13, 13, 13, 27, 58,
		46, 56, 14, 69, 69, 70, 78, 44, 45, 77, 82, 40, 12, 68, 28, 79,
		29, 51, 51, 51, 51, 51, 47, 71, 1, 1, 6, 15, 15, 16, 16, 17,
		17, 34, 34, 35, 35, 84, 2, 3, 5, 7, 18, 19, 25, 41, 30, 36,
		37, 64, 38, 38, 61, 63, 62, 62, 75, 39, 39, 20, 20, 52, 65, 76,
		80, 83, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 66, 73, 10, 21,
		24, 21, 55, 55, 55, 55, 55, 11, 8, 22, 31, 42, 74, 67
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 0, 1, 0, 1, 6, 2, 2, 1, 0, 1, 0, 1, 5,
		2, 3, 3, 1, 1, 1, 3, 3, 1, 3, 1, 1, 1, 1, 3, 3,
		1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 4, 0, 1, 3,
		1, 3, 3, 5, 1, 3, 1, 3
	};

	private static final short lapg_rlex[] = {
		26, 27, 27, 48, 48, 49, 49, 28, 29, 30, 30, 50, 50, 51, 51, 31,
		32, 33, 34, 34, 35, 35, 36, 36, 36, 37, 37, 38, 38, 39, 39, 39,
		40, 40, 40, 40, 40, 41, 41, 42, 42, 42, 52, 52, 43, 53, 53, 43,
		44, 44, 45, 45, 46, 46, 47, 47
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
		"feature_declarationsopt",
		"modifiersopt",
		"defaultvalopt",
		"map_entriesopt",
		"expression_listopt",
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
		public static final int name = 46;
		public static final int name_list = 47;
		public static final int extends_clauseopt = 48;
		public static final int feature_declarationsopt = 49;
		public static final int modifiersopt = 50;
		public static final int defaultvalopt = 51;
		public static final int map_entriesopt = 52;
		public static final int expression_listopt = 53;
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

		while (lapg_m[lapg_head].state != 85) {
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

		if (lapg_m[lapg_head].state != 85) {
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
			case 7:  // type_declaration ::= Lclass identifier extends_clauseopt '{' feature_declarationsopt '}'
				lapg_gg.sym = new TypeDeclaration(
((String)lapg_m[lapg_head-4].sym) /* name */,
((List<List<String>>)lapg_m[lapg_head-3].sym) /* _extends */,
((List<FeatureDeclaration>)lapg_m[lapg_head-1].sym) /* featureDeclarationsopt */,
null /* input */, lapg_m[lapg_head-5].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 8:  // extends_clause ::= Lextends name_list
				lapg_gg.sym = ((List<List<String>>)lapg_m[lapg_head-0].sym);
				break;
			case 9:  // feature_declarations ::= feature_declarations feature_declaration
				((List<FeatureDeclaration>)lapg_m[lapg_head-1].sym).add(((FeatureDeclaration)lapg_m[lapg_head-0].sym));
				break;
			case 10:  // feature_declarations ::= feature_declaration
				lapg_gg.sym = new ArrayList();
((List<FeatureDeclaration>)lapg_gg.sym).add(((FeatureDeclaration)lapg_m[lapg_head-0].sym));
				break;
			case 15:  // feature_declaration ::= type identifier modifiersopt defaultvalopt ';'
				lapg_gg.sym = new FeatureDeclaration(
((String)lapg_m[lapg_head-3].sym) /* name */,
((Type)lapg_m[lapg_head-4].sym) /* type */,
((List<IConstraint>)lapg_m[lapg_head-2].sym) /* modifiersopt */,
((IExpression)lapg_m[lapg_head-1].sym) /* defaultvalopt */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 16:  // defaultval ::= '=' expression
				lapg_gg.sym = ((IExpression)lapg_m[lapg_head-0].sym);
				break;
			case 17:  // modifiers ::= '[' constraints ']'
				lapg_gg.sym = ((List<IConstraint>)lapg_m[lapg_head-1].sym);
				break;
			case 18:  // constraints ::= constraints ';' constraint
				((List<IConstraint>)lapg_m[lapg_head-2].sym).add(((IConstraint)lapg_m[lapg_head-0].sym));
				break;
			case 19:  // constraints ::= constraint
				lapg_gg.sym = new ArrayList();
((List<IConstraint>)lapg_gg.sym).add(((IConstraint)lapg_m[lapg_head-0].sym));
				break;
			case 22:  // string_constraint ::= Lset ':' strings
				lapg_gg.sym = new StringConstraint(
StringConstraint.LSET,
((List<_String>)lapg_m[lapg_head-0].sym) /* strings */,
null /* identifier */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 23:  // string_constraint ::= Lchoice ':' strings
				lapg_gg.sym = new StringConstraint(
StringConstraint.LCHOICE,
((List<_String>)lapg_m[lapg_head-0].sym) /* strings */,
null /* identifier */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 24:  // string_constraint ::= identifier
				lapg_gg.sym = new StringConstraint(
0,
null /* strings */,
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 25:  // strings ::= strings ',' string
				((List<_String>)lapg_m[lapg_head-2].sym).add(((_String)lapg_m[lapg_head-0].sym));
				break;
			case 26:  // strings ::= string
				lapg_gg.sym = new ArrayList();
((List<_String>)lapg_gg.sym).add(((_String)lapg_m[lapg_head-0].sym));
				break;
			case 27:  // string ::= identifier
				lapg_gg.sym = new _String(
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* scon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 28:  // string ::= scon
				lapg_gg.sym = new _String(
null /* identifier */,
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 29:  // multiplicity ::= icon
				lapg_gg.sym = new Multiplicity(
((Integer)lapg_m[lapg_head-0].sym) /* lo */,
false,
null /* hi */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 30:  // multiplicity ::= icon '..' '*'
				lapg_gg.sym = new Multiplicity(
((Integer)lapg_m[lapg_head-2].sym) /* lo */,
true,
null /* hi */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 31:  // multiplicity ::= icon '..' icon
				lapg_gg.sym = new Multiplicity(
((Integer)lapg_m[lapg_head-2].sym) /* lo */,
false,
((Integer)lapg_m[lapg_head-0].sym) /* hi */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 32:  // type ::= Lint
				lapg_gg.sym = new Type(
Type.LINT,
false,
null /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 33:  // type ::= Lstring
				lapg_gg.sym = new Type(
Type.LSTRING,
false,
null /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 34:  // type ::= Lbool
				lapg_gg.sym = new Type(
Type.LBOOL,
false,
null /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 35:  // type ::= name
				lapg_gg.sym = new Type(
0,
false,
((List<String>)lapg_m[lapg_head-0].sym) /* name */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 36:  // type ::= name '*'
				lapg_gg.sym = new Type(
0,
true,
((List<String>)lapg_m[lapg_head-1].sym) /* name */,
null /* input */, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 39:  // literal_expression ::= scon
				lapg_gg.sym = new LiteralExpression(
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* icon */,
null /* bcon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 40:  // literal_expression ::= icon
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
((Integer)lapg_m[lapg_head-0].sym) /* icon */,
null /* bcon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 41:  // literal_expression ::= bcon
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
null /* icon */,
((Boolean)lapg_m[lapg_head-0].sym) /* bcon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 44:  // structural_expression ::= name '{' map_entriesopt '}'
				lapg_gg.sym = new StructuralExpression(
((List<String>)lapg_m[lapg_head-3].sym) /* name */,
((List<MapEntriesItem>)lapg_m[lapg_head-1].sym) /* mapEntriesopt */,
null /* expressionListopt */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 47:  // structural_expression ::= '[' expression_listopt ']'
				lapg_gg.sym = new StructuralExpression(
null /* name */,
null /* mapEntriesopt */,
((List<IExpression>)lapg_m[lapg_head-1].sym) /* expressionListopt */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 48:  // expression_list ::= expression
				lapg_gg.sym = new ArrayList();
((List<IExpression>)lapg_gg.sym).add(((IExpression)lapg_m[lapg_head-0].sym));
				break;
			case 49:  // expression_list ::= expression_list ',' expression
				((List<IExpression>)lapg_m[lapg_head-2].sym).add(((IExpression)lapg_m[lapg_head-0].sym));
				break;
			case 50:  // map_entries ::= identifier ':' expression
				lapg_gg.sym = new ArrayList();
((List<MapEntriesItem>)lapg_gg.sym).add(new MapEntriesItem(
((String)lapg_m[lapg_head-2].sym) /* identifier */,
((IExpression)lapg_m[lapg_head-0].sym) /* expression */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 51:  // map_entries ::= map_entries ',' identifier ':' expression
				((List<MapEntriesItem>)lapg_m[lapg_head-4].sym).add(new MapEntriesItem(
((String)lapg_m[lapg_head-2].sym) /* identifier */,
((IExpression)lapg_m[lapg_head-0].sym) /* expression */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 52:  // name ::= identifier
				lapg_gg.sym = new ArrayList();
((List<String>)lapg_gg.sym).add(((String)lapg_m[lapg_head-0].sym));
				break;
			case 53:  // name ::= name '.' identifier
				((List<String>)lapg_m[lapg_head-2].sym).add(((String)lapg_m[lapg_head-0].sym));
				break;
			case 54:  // name_list ::= name
				lapg_gg.sym = new ArrayList();
((List<List<String>>)lapg_gg.sym).add(((List<String>)lapg_m[lapg_head-0].sym));
				break;
			case 55:  // name_list ::= name_list ',' name
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
