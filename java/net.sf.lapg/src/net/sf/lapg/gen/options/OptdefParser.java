package net.sf.lapg.gen.options;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import net.sf.lapg.gen.options.OptdefLexer.ErrorReporter;
import net.sf.lapg.gen.options.OptdefLexer.LapgSymbol;
import net.sf.lapg.gen.options.OptdefLexer.Lexems;
import net.sf.lapg.gen.options.ast.AnnoKind;
import net.sf.lapg.gen.options.ast.Declaration;
import net.sf.lapg.gen.options.ast.Defaultval;
import net.sf.lapg.gen.options.ast.Group;
import net.sf.lapg.gen.options.ast.IExpression;
import net.sf.lapg.gen.options.ast.Input;
import net.sf.lapg.gen.options.ast.LiteralExpression;
import net.sf.lapg.gen.options.ast.MapEntriesItem;
import net.sf.lapg.gen.options.ast.Modifier;
import net.sf.lapg.gen.options.ast.Option;
import net.sf.lapg.gen.options.ast.SomeA;
import net.sf.lapg.gen.options.ast.StructuralExpression;
import net.sf.lapg.gen.options.ast.Type;
import net.sf.lapg.gen.options.ast.Typedef;
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
		-1, -1, -1, 6, 7, 8, -3, 2, -1, -1, -1, 1, -1, -1, -1, -1,
		24, -1, -1, 10, -1, -1, 5, 23, -1, 4, 9, 3, 26, -1, -1, 27,
		29, 30, -17, 28, 31, -1, -1, -1, -29, -1, -1, -1, -1, -1, 25, 22,
		-35, 21, -1, 40, 41, -1, 39, -1, -1, -1, -1, 20, -1, -41, -1, 34,
		35, -1, 36, 37, 44, 45, -1, 19, 43, 42, -1, -49, 15, 38, -1, -1,
		48, -1, -1, -1, 17, -1, 33, -1, -1, 47, -1, 46, 18, 16, 50, 49,
		-1, -1, 51, -1, -2,
	};

    private static final short lapg_lalr[] = {
		21, -1, 25, -1, 27, -1, 28, -1, 29, -1, 0, 0, -1, -2, 11, -1,
		5, 32, 12, 32, 23, 32, 24, 32, -1, -2, 24, -1, 23, 11, -1, -2,
		24, -1, 23, 12, -1, -2, 5, -1, 1, 13, 10, 13, -1, -2, 6, -1,
		1, 14, 10, 14, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 19, 31, 36, 36, 38, 44, 47, 48, 52, 56, 60, 64, 69, 71,
		74, 77, 80, 83, 86, 89, 91, 93, 94, 96, 98, 101, 106, 108, 110, 113,
		116, 117, 118, 120, 122, 125, 131, 132, 134, 135, 136, 138, 139, 141, 144, 146,
		151, 156, 161, 166, 167, 168, 168, 168, 169, 170, 170,
	};

    private static final short lapg_sym_from[] = {
		99, 10, 12, 13, 15, 18, 20, 21, 24, 41, 42, 43, 44, 45, 58, 62,
		65, 70, 90, 1, 41, 42, 43, 60, 62, 65, 70, 83, 87, 88, 97, 60,
		70, 87, 88, 97, 39, 61, 53, 55, 56, 75, 81, 82, 17, 79, 96, 14,
		2, 8, 9, 38, 15, 18, 20, 58, 29, 30, 34, 37, 53, 55, 57, 78,
		60, 70, 87, 88, 97, 81, 82, 21, 24, 44, 21, 24, 44, 21, 24, 44,
		21, 24, 44, 21, 24, 44, 21, 24, 44, 0, 6, 74, 85, 50, 40, 48,
		0, 6, 21, 24, 44, 0, 6, 21, 24, 44, 0, 6, 0, 6, 21, 24,
		44, 21, 24, 44, 0, 0, 0, 6, 0, 6, 12, 13, 45, 12, 13, 18,
		20, 45, 58, 61, 74, 85, 50, 40, 40, 48, 10, 10, 15, 21, 24, 44,
		41, 42, 41, 42, 43, 62, 65, 60, 70, 87, 88, 97, 60, 70, 87, 88,
		97, 60, 70, 87, 88, 97, 70, 70, 40, 61,
	};

    private static final short lapg_sym_to[] = {
		100, 14, 17, 17, 14, 17, 17, 28, 28, 51, 51, 51, 28, 17, 17, 51,
		51, 79, 96, 9, 52, 52, 52, 68, 52, 52, 68, 92, 68, 68, 68, 69,
		69, 69, 69, 69, 46, 74, 62, 62, 65, 85, 88, 90, 24, 87, 97, 21,
		10, 12, 13, 45, 22, 25, 27, 67, 41, 42, 43, 44, 63, 64, 66, 86,
		70, 70, 70, 70, 70, 89, 91, 29, 29, 29, 30, 30, 30, 31, 31, 31,
		32, 32, 32, 33, 33, 33, 34, 34, 34, 1, 1, 83, 83, 60, 47, 47,
		2, 2, 35, 35, 35, 3, 3, 36, 36, 36, 4, 4, 5, 5, 37, 37,
		37, 38, 38, 38, 99, 6, 7, 11, 8, 8, 18, 20, 58, 19, 19, 26,
		26, 19, 26, 75, 84, 93, 61, 48, 49, 59, 15, 16, 23, 39, 40, 57,
		53, 55, 54, 54, 56, 77, 78, 71, 80, 94, 95, 98, 72, 72, 72, 72,
		72, 73, 73, 73, 73, 73, 81, 82, 50, 76,
	};

    private static final short lapg_rlen[] = {
		1, 2, 1, 5, 4, 4, 1, 1, 1, 2, 1, 0, 1, 0, 1, 6,
		3, 2, 2, 2, 2, 1, 1, 2, 1, 4, 1, 1, 1, 1, 1, 1,
		1, 6, 4, 4, 4, 4, 3, 1, 1, 1, 1, 1, 1, 1, 3, 3,
		1, 3, 3, 5, 1, 1, 0, 1, 1,
	};

    private static final short lapg_rlex[] = {
		32, 33, 33, 34, 34, 34, 35, 35, 35, 36, 36, 55, 55, 56, 56, 37,
		38, 38, 39, 40, 41, 41, 42, 43, 43, 44, 45, 45, 45, 45, 45, 45,
		45, 45, 45, 45, 45, 45, 46, 46, 47, 47, 48, 48, 49, 49, 50, 50,
		51, 51, 52, 52, 53, 53, 57, 57, 54,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"scon",
		"icon",
		"_skip",
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
		"Lset",
		"Lchoice",
		"Luint",
		"Lidentifier",
		"Lqualified",
		"Lbool",
		"Lglobal",
		"Ltitle",
		"Ldefault",
		"Lnotempty",
		"Ltypes",
		"Lstring",
		"Lsymbol",
		"Lrule",
		"Lref",
		"Larray",
		"Lstruct",
		"input",
		"groups",
		"group",
		"anno_kind",
		"declarations",
		"declaration",
		"optionslist",
		"option",
		"defaultval",
		"modifiers",
		"modifier",
		"typedefs",
		"typedef",
		"type",
		"strings",
		"string",
		"expression",
		"literal_expression",
		"structural_expression",
		"expression_list",
		"map_entries",
		"someA",
		"someB",
		"modifiersopt",
		"optionslistopt",
		"structural_expressionopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 32;
		public static final int groups = 33;
		public static final int group = 34;
		public static final int anno_kind = 35;
		public static final int declarations = 36;
		public static final int declaration = 37;
		public static final int optionslist = 38;
		public static final int option = 39;
		public static final int defaultval = 40;
		public static final int modifiers = 41;
		public static final int modifier = 42;
		public static final int typedefs = 43;
		public static final int typedef = 44;
		public static final int type = 45;
		public static final int strings = 46;
		public static final int string = 47;
		public static final int expression = 48;
		public static final int literal_expression = 49;
		public static final int structural_expression = 50;
		public static final int expression_list = 51;
		public static final int map_entries = 52;
		public static final int someA = 53;
		public static final int someB = 54;
		public static final int modifiersopt = 55;
		public static final int optionslistopt = 56;
		public static final int structural_expressionopt = 57;
	}

	private static int lapg_next( int state, int symbol ) {
		int p;
		if( lapg_action[state] < -2 ) {
			for( p = - lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2 )
				if( lapg_lalr[p] == symbol ) break;
			return lapg_lalr[p+1];
		}
		return lapg_action[state];
	}

	private static int lapg_state_sym( int state, int symbol ) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol+1]-1;
		int i, e;

		while( min <= max ) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if( i == state )
				return lapg_sym_to[e];
			else if( i < state )
				min = e + 1;
			else
				max = e - 1;
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

		while( lapg_m[lapg_head].state != 100 ) {
			int lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

			if( lapg_i >= 0 ) {
				reduce(lapg_i);
			} else if( lapg_i == -1 ) {
				shift(lexer);
			}

			if( lapg_i == -2 || lapg_m[lapg_head].state == -1 ) {
				break;
			}
		}

		if( lapg_m[lapg_head].state != 100 ) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		};
		return (Input)lapg_m[lapg_head-1].sym;
	}

	private void shift(OptdefLexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
		if( DEBUG_SYNTAX ) {
			System.out.println(MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current()));
		}
		if( lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0 ) {
			lapg_n = lexer.next();
		}
	}

	@SuppressWarnings("unchecked")
	private void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]].sym:null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if( DEBUG_SYNTAX ) {
			System.out.println( "reduce to " + lapg_syms[lapg_rlex[rule]] );
		}
		LapgSymbol startsym = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]]:lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule]!=0)?lapg_m[lapg_head].endoffset:lapg_n.offset;
		switch( rule ) {
			case 0:  // input ::= groups
				lapg_gg.sym = new Input(
((List<Group>)lapg_m[lapg_head-0].sym) /* groups */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 3:  // group ::= Lglobal scon '{' declarations '}'
				lapg_gg.sym = new Group(
((String)lapg_m[lapg_head-3].sym) /* title */,
null /* kind */,
((List<Declaration>)lapg_m[lapg_head-1].sym) /* declarations */,
null /* typedefs */,
null /* input */, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 4:  // group ::= anno_kind '{' declarations '}'
				lapg_gg.sym = new Group(
null /* title */,
((AnnoKind)lapg_m[lapg_head-3].sym) /* kind */,
((List<Declaration>)lapg_m[lapg_head-1].sym) /* declarations */,
null /* typedefs */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 5:  // group ::= Ltypes '{' typedefs '}'
				lapg_gg.sym = new Group(
null /* title */,
null /* kind */,
null /* declarations */,
((List<Typedef>)lapg_m[lapg_head-1].sym) /* typedefs */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 6:  // anno_kind ::= Lsymbol
				lapg_gg.sym = AnnoKind.LSYMBOL;
				break;
			case 7:  // anno_kind ::= Lrule
				lapg_gg.sym = AnnoKind.LRULE;
				break;
			case 8:  // anno_kind ::= Lref
				lapg_gg.sym = AnnoKind.LREF;
				break;
			case 15:  // declaration ::= identifier ':' type modifiersopt defaultval optionslistopt
				lapg_gg.sym = new Declaration(
((String)lapg_m[lapg_head-5].sym) /* identifier */,
((Type)lapg_m[lapg_head-3].sym) /* type */,
((List<Modifier>)lapg_m[lapg_head-2].sym) /* modifiersopt */,
((Defaultval)lapg_m[lapg_head-1].sym) /* defaultval */,
((List<Option>)lapg_m[lapg_head-0].sym) /* optionslistopt */,
null /* input */, lapg_m[lapg_head-5].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 18:  // option ::= Ltitle scon
				lapg_gg.sym = new Option(
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* input */, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 19:  // defaultval ::= Ldefault expression
				lapg_gg.sym = new Defaultval(
((IExpression)lapg_m[lapg_head-0].sym) /* expression */,
null /* input */, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 22:  // modifier ::= Lnotempty
				lapg_gg.sym = Modifier.LNOTEMPTY;
				break;
			case 25:  // typedef ::= identifier '=' type ';'
				lapg_gg.sym = new Typedef(
((String)lapg_m[lapg_head-3].sym) /* identifier */,
((Type)lapg_m[lapg_head-1].sym) /* type */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 26:  // type ::= identifier
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 27:  // type ::= Luint
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 28:  // type ::= Lstring
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 29:  // type ::= Lidentifier
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 30:  // type ::= Lqualified
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 31:  // type ::= Lsymbol
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 32:  // type ::= Lbool
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 33:  // type ::= Lbool '(' string ',' string ')'
				lapg_gg.sym = new Type(
((_String)lapg_m[lapg_head-3].sym) /* trueVal */,
((_String)lapg_m[lapg_head-1].sym) /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-5].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 34:  // type ::= Lset '(' strings ')'
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
((List<_String>)lapg_m[lapg_head-1].sym) /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 35:  // type ::= Lchoice '(' strings ')'
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
((List<_String>)lapg_m[lapg_head-1].sym) /* strings */,
null /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 36:  // type ::= Larray '(' type ')'
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
((Type)lapg_m[lapg_head-1].sym) /* type */,
null /* declarations */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 37:  // type ::= Lstruct '{' declarations '}'
				lapg_gg.sym = new Type(
null /* trueVal */,
null /* falseVal */,
null /* identifier */,
null /* strings */,
null /* type */,
((List<Declaration>)lapg_m[lapg_head-1].sym) /* declarations */,
null /* input */, lapg_m[lapg_head-3].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 40:  // string ::= identifier
				lapg_gg.sym = new _String(
((String)lapg_m[lapg_head-0].sym) /* identifier */,
null /* scon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 41:  // string ::= scon
				lapg_gg.sym = new _String(
null /* identifier */,
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 44:  // literal_expression ::= scon
				lapg_gg.sym = new LiteralExpression(
((String)lapg_m[lapg_head-0].sym) /* scon */,
null /* icon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 45:  // literal_expression ::= icon
				lapg_gg.sym = new LiteralExpression(
null /* scon */,
((Integer)lapg_m[lapg_head-0].sym) /* icon */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 46:  // structural_expression ::= '[' map_entries ']'
				lapg_gg.sym = new StructuralExpression(
((List<MapEntriesItem>)lapg_m[lapg_head-1].sym) /* mapEntries */,
null /* expressionList */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 47:  // structural_expression ::= '[' expression_list ']'
				lapg_gg.sym = new StructuralExpression(
null /* mapEntries */,
((List<IExpression>)lapg_m[lapg_head-1].sym) /* expressionList */,
null /* input */, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 52:  // someA ::= map_entries
				lapg_gg.sym = new SomeA(
((List<MapEntriesItem>)lapg_m[lapg_head-0].sym) /* mapEntries */,
null /* structuralExpression */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
			case 53:  // someA ::= structural_expression
				lapg_gg.sym = new SomeA(
null /* mapEntries */,
((StructuralExpression)lapg_m[lapg_head-0].sym) /* structuralExpression */,
null /* input */, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset);
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
