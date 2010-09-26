package net.sf.lapg.gen.options;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import net.sf.lapg.gen.options.OptdefLexer.ErrorReporter;
import net.sf.lapg.gen.options.OptdefLexer.LapgSymbol;
import net.sf.lapg.gen.options.OptdefLexer.Lexems;
import net.sf.lapg.gen.options.ast.AnnoKind;
import net.sf.lapg.gen.options.ast.Declaration;
import net.sf.lapg.gen.options.ast.FeatureDeclaration;
import net.sf.lapg.gen.options.ast.IConstraint;
import net.sf.lapg.gen.options.ast.IDefaultval;
import net.sf.lapg.gen.options.ast.IExpression;
import net.sf.lapg.gen.options.ast.Input;
import net.sf.lapg.gen.options.ast.LiteralExpression;
import net.sf.lapg.gen.options.ast.MapEntriesItem;
import net.sf.lapg.gen.options.ast.Modifiers;
import net.sf.lapg.gen.options.ast.Multiplicity;
import net.sf.lapg.gen.options.ast.StringConstraint;
import net.sf.lapg.gen.options.ast.StructuralExpression;
import net.sf.lapg.gen.options.ast.Type;
import net.sf.lapg.gen.options.ast._String;

public class OptdefParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public OptdefParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int lapg_action[] = {
		-1, -1, -1, -3, 2, -1, 3, 4, 5, 6, -1, 1, -1, -1, -11, 32,
		34, 33, -1, 10, -1, -1, 36, 8, 9, -17, 7, -1, 12, -25, 24, -31,
		-1, -1, -1, 19, 20, 21, -1, 14, -1, -1, -1, -1, -1, 17, 39, 40,
		-1, 41, 42, 16, 38, 37, 15, 31, 30, 27, 28, -39, 26, -47, 18, -1,
		45, -1, -1, -1, -1, -1, 44, -1, 43, 25, 47, 46, -1, -1, 48, -1,
		-2,
	};

	private static final short lapg_lalr[] = {
		17, -1, 18, -1, 0, 0, -1, -2, 6, -1, 1, 35, -1, -2, 15, -1,
		7, 11, 10, 11, -1, -2, 10, -1, 7, 13, -1, -2, 5, -1, 7, 29,
		16, 29, -1, -2, 8, -1, 7, 22, 16, 22, -1, -2, 8, -1, 7, 23,
		16, 23, -1, -2,
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 14, 22, 30, 30, 31, 33, 35, 39, 43, 44, 46, 48, 48, 48,
		54, 57, 59, 61, 65, 69, 73, 75, 77, 78, 79, 80, 81, 86, 91, 92,
		93, 94, 96, 98, 102, 103, 104, 105, 107, 109, 111, 114, 116, 120, 125, 130,
		135, 136, 137, 138, 139,
	};

	private static final short lapg_sym_from[] = {
		79, 1, 12, 13, 18, 20, 21, 27, 42, 43, 44, 48, 67, 71, 38, 42,
		43, 48, 67, 68, 69, 77, 27, 38, 41, 44, 48, 68, 69, 77, 31, 14,
		41, 34, 40, 59, 61, 65, 66, 32, 33, 63, 76, 29, 5, 10, 18, 21,
		25, 38, 48, 68, 69, 77, 34, 65, 66, 0, 3, 0, 3, 12, 13, 18,
		21, 12, 13, 18, 21, 12, 13, 18, 21, 27, 44, 27, 44, 2, 2, 2,
		2, 38, 48, 68, 69, 77, 38, 48, 68, 69, 77, 0, 0, 2, 0, 3,
		12, 13, 12, 13, 18, 21, 29, 25, 27, 27, 44, 27, 44, 42, 43, 42,
		43, 67, 27, 44, 12, 13, 18, 21, 38, 48, 68, 69, 77, 38, 48, 68,
		69, 77, 38, 48, 68, 69, 77, 48, 48, 25, 29,
	};

	private static final short lapg_sym_to[] = {
		80, 5, 14, 14, 14, 25, 14, 30, 57, 57, 30, 63, 57, 76, 46, 58,
		58, 46, 58, 46, 46, 46, 31, 47, 55, 31, 47, 47, 47, 47, 41, 22,
		56, 44, 54, 67, 67, 69, 71, 42, 43, 68, 77, 38, 12, 13, 23, 26,
		27, 48, 48, 48, 48, 48, 45, 70, 72, 1, 1, 2, 2, 15, 15, 15,
		15, 16, 16, 16, 16, 17, 17, 17, 17, 32, 32, 33, 33, 6, 7, 8,
		9, 49, 49, 49, 49, 49, 50, 50, 50, 50, 50, 79, 3, 10, 4, 11,
		18, 21, 19, 19, 24, 24, 39, 28, 34, 35, 62, 36, 36, 59, 61, 60,
		60, 73, 37, 37, 20, 20, 20, 20, 51, 64, 74, 75, 78, 52, 52, 52,
		52, 52, 53, 53, 53, 53, 53, 65, 66, 29, 40,
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 1, 1, 1, 1, 5, 5, 2, 1, 0, 1, 0, 1, 5,
		2, 3, 3, 1, 1, 1, 3, 3, 1, 3, 1, 1, 1, 1, 3, 3,
		1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 3, 3, 1, 3, 3,
		5,
	};

	private static final short lapg_rlex[] = {
		30, 31, 31, 32, 32, 32, 32, 33, 33, 34, 34, 50, 50, 51, 51, 35,
		36, 37, 38, 38, 39, 39, 40, 40, 40, 41, 41, 42, 42, 43, 43, 43,
		44, 44, 44, 44, 44, 45, 45, 46, 46, 46, 46, 47, 47, 48, 48, 49,
		49,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"scon",
		"icon",
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
		"Lconstraints",
		"Lint",
		"Lbool",
		"Lstring",
		"Lset",
		"Lchoice",
		"Lsymbol",
		"Lrule",
		"Lref",
		"Loption",
		"Ltrue",
		"Lfalse",
		"input",
		"declarations",
		"anno_kind",
		"declaration",
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
		"modifiersopt",
		"defaultvalopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 30;
		public static final int declarations = 31;
		public static final int anno_kind = 32;
		public static final int declaration = 33;
		public static final int feature_declarations = 34;
		public static final int feature_declaration = 35;
		public static final int defaultval = 36;
		public static final int modifiers = 37;
		public static final int constraints = 38;
		public static final int constraint = 39;
		public static final int string_constraint = 40;
		public static final int strings = 41;
		public static final int string = 42;
		public static final int multiplicity = 43;
		public static final int type = 44;
		public static final int expression = 45;
		public static final int literal_expression = 46;
		public static final int structural_expression = 47;
		public static final int expression_list = 48;
		public static final int map_entries = 49;
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

	public Input parse(OptdefLexer lexer) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 80) {
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

		if (lapg_m[lapg_head].state != 80) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		}
		return (Input)lapg_m[lapg_head - 1].sym;
	}

	private void shift(OptdefLexer lexer) throws IOException {
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
((List<Declaration>)lapg_m[lapg_head-0].sym) /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 1:  // declarations ::= declarations declaration
				((List<Declaration>)lapg_m[lapg_head-1].sym).add(((Declaration)lapg_m[lapg_head-0].sym));
				break;
			case 2:  // declarations ::= declaration
				lapg_gg.sym = new ArrayList();
((List<Declaration>)lapg_gg.sym).add(((Declaration)lapg_m[lapg_head-0].sym));
				break;
			case 3:  // anno_kind ::= Lsymbol
				lapg_gg.sym = AnnoKind.LSYMBOL;
				break;
			case 4:  // anno_kind ::= Lrule
				lapg_gg.sym = AnnoKind.LRULE;
				break;
			case 5:  // anno_kind ::= Lref
				lapg_gg.sym = AnnoKind.LREF;
				break;
			case 6:  // anno_kind ::= Loption
				lapg_gg.sym = AnnoKind.LOPTION;
				break;
			case 7:  // declaration ::= Lconstraints anno_kind '{' feature_declarations '}'
				lapg_gg.sym = new Declaration(
((AnnoKind)lapg_m[lapg_head-3].sym) /* kind */,
null /* name */,
((List<FeatureDeclaration>)lapg_m[lapg_head-1].sym) /* featureDeclarations */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 8:  // declaration ::= Lclass identifier '{' feature_declarations '}'
				lapg_gg.sym = new Declaration(
null /* kind */,
((String)lapg_m[lapg_head-3].sym) /* name */,
((List<FeatureDeclaration>)lapg_m[lapg_head-1].sym) /* featureDeclarations */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
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
((Modifiers)lapg_m[lapg_head-2].sym) /* modifiersopt */,
((IDefaultval)lapg_m[lapg_head-1].sym) /* defaultvalopt */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 16:  // defaultval ::= '=' expression
				lapg_gg.sym = ((IExpression)lapg_m[lapg_head-0].sym);
				break;
			case 17:  // modifiers ::= '[' constraints ']'
				lapg_gg.sym = new Modifiers(
((List<IConstraint>)lapg_m[lapg_head-1].sym) /* constraints */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
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
false,
((Integer)lapg_m[lapg_head-0].sym) /* icon */,
null /* icon2 */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 30:  // multiplicity ::= icon '..' '*'
				lapg_gg.sym = new Multiplicity(
true,
((Integer)lapg_m[lapg_head-2].sym) /* icon */,
null /* icon2 */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 31:  // multiplicity ::= icon '..' icon
				lapg_gg.sym = new Multiplicity(
false,
((Integer)lapg_m[lapg_head-2].sym) /* icon */,
((Integer)lapg_m[lapg_head-0].sym) /* icon2 */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 32:  // type ::= Lint
				lapg_gg.sym = new Type(
null /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 33:  // type ::= Lstring
				lapg_gg.sym = new Type(
null /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 34:  // type ::= Lbool
				lapg_gg.sym = new Type(
null /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 35:  // type ::= identifier
				lapg_gg.sym = new Type(
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 36:  // type ::= identifier '*'
				lapg_gg.sym = new Type(
((String)lapg_m[lapg_head-1].sym) /* identifier */,
null /* input */, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 39:  // literal_expression ::= scon
				lapg_gg.sym = new LiteralExpression(
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* icon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 40:  // literal_expression ::= icon
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
((Integer)lapg_m[lapg_head-0].sym) /* icon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 41:  // literal_expression ::= Ltrue
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
null /* icon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 42:  // literal_expression ::= Lfalse
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
null /* icon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 43:  // structural_expression ::= '[' map_entries ']'
				lapg_gg.sym = new StructuralExpression(
((List<MapEntriesItem>)lapg_m[lapg_head-1].sym) /* mapEntries */,
null /* expressionList */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 44:  // structural_expression ::= '[' expression_list ']'
				lapg_gg.sym = new StructuralExpression(
null /* mapEntries */,
((List<IExpression>)lapg_m[lapg_head-1].sym) /* expressionList */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 45:  // expression_list ::= expression
				lapg_gg.sym = new ArrayList();
((List<IExpression>)lapg_gg.sym).add(((IExpression)lapg_m[lapg_head-0].sym));
				break;
			case 46:  // expression_list ::= expression_list ',' expression
				((List<IExpression>)lapg_m[lapg_head-2].sym).add(((IExpression)lapg_m[lapg_head-0].sym));
				break;
			case 47:  // map_entries ::= identifier ':' expression
				lapg_gg.sym = new ArrayList();
((List<MapEntriesItem>)lapg_gg.sym).add(new MapEntriesItem(
((String)lapg_m[lapg_head-2].sym) /* identifier */,
((IExpression)lapg_m[lapg_head-0].sym) /* expression */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset));
				break;
			case 48:  // map_entries ::= map_entries ',' identifier ':' expression
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
