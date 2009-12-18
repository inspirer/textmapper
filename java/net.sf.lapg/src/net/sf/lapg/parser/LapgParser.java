package net.sf.lapg.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.lapg.parser.LapgLexer.ErrorReporter;
import net.sf.lapg.parser.LapgLexer.Lexems;
import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.parser.ast.*;

import net.sf.lapg.parser.LapgLexer.LapgSymbol;

public class LapgParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public LapgParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	
	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
    private static final int lapg_action[] = {
		-3, -1, -11, 4, -1, -1, 3, 7, -1, -19, -1, 27, 5, 6, 38, -1,
		-1, 30, -1, -1, -1, -25, 26, -33, 40, -1, 37, 28, 16, -1, 18, 19,
		14, 15, -43, 12, 13, 17, 20, 21, -1, 11, -69, -1, -1, -1, -1, -1,
		-79, 39, -85, 8, -91, -1, 9, 10, 25, -117, 44, -1, -1, -1, 58, -129,
		-1, -1, 24, 33, -143, 43, 45, 61, 62, -1, 63, 59, -1, -1, 46, 49,
		-153, -165, -179, 35, 36, -185, -1, 66, -1, -1, -193, 41, -207, 54, -219, -1,
		-229, -235, 73, -1, 64, -1, 65, 60, 47, 55, -241, -251, -1, -267, 51, 53,
		42, -1, 72, 71, 67, 52, -1, 68, 57, 74, -281, 56, -1, -2,
	};

    private static final short lapg_lalr[] = {
		11, -1, 1, 0, 14, 0, -1, -2, 11, -1, 1, 1, 14, 1, -1, -2,
		16, -1, 13, 29, -1, -2, 16, -1, 7, 29, 13, 29, -1, -2, 1, -1,
		5, -1, 14, -1, 0, 2, -1, -2, 1, -1, 11, -1, 12, -1, 14, -1,
		15, -1, 16, -1, 19, -1, 20, -1, 21, -1, 22, -1, 23, -1, 17, 22,
		-1, -2, 2, -1, 1, 31, 5, 31, 14, 31, -1, -2, 16, -1, 7, 29,
		-1, -2, 16, -1, 7, 29, -1, -2, 1, -1, 11, -1, 12, -1, 14, -1,
		15, -1, 16, -1, 19, -1, 20, -1, 21, -1, 22, -1, 23, -1, 17, 23,
		-1, -2, 4, -1, 1, 32, 5, 32, 14, 32, 24, 32, -1, -2, 14, -1,
		1, 48, 8, 48, 10, 48, 18, 48, 24, 48, -1, -2, 24, -1, 1, 34,
		5, 34, 14, 34, -1, -2, 24, -1, 1, 34, 8, 34, 10, 34, 18, 34,
		-1, -2, 14, -1, 1, 48, 8, 48, 10, 48, 18, 48, 24, 48, -1, -2,
		25, -1, 26, 69, -1, -2, 13, -1, 12, 7, 15, 7, -1, -2, 14, -1,
		1, 48, 8, 48, 10, 48, 18, 48, 24, 48, -1, -2, 24, -1, 1, 34,
		8, 34, 10, 34, 18, 34, -1, -2, 1, -1, 18, -1, 8, 50, 10, 50,
		-1, -2, 25, -1, 26, 69, -1, -2, 25, -1, 26, 70, -1, -2, 1, -1,
		18, -1, 8, 50, 10, 50, -1, -2, 9, -1, 1, 7, 8, 7, 10, 7,
		14, 7, 18, 7, 24, 7, -1, -2, 14, -1, 1, 48, 8, 48, 10, 48,
		18, 48, 24, 48, -1, -2, 14, -1, 1, 48, 8, 48, 10, 48, 18, 48,
		24, 48, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 24, 25, 31, 40, 42, 42, 45, 47, 48, 51, 57, 64, 69, 85,
		93, 101, 104, 106, 110, 114, 118, 122, 126, 129, 132, 134, 135, 136, 138, 152,
		156, 158, 162, 163, 164, 166, 168, 169, 171, 172, 174, 177, 178, 180, 187, 190,
		194, 195, 197, 200, 202, 205, 206, 207, 211, 212, 215, 220, 222, 224,
	};

    private static final short lapg_sym_from[] = {
		124, 1, 4, 10, 16, 19, 20, 23, 25, 34, 40, 43, 47, 52, 59, 60,
		61, 73, 89, 94, 101, 106, 108, 118, 42, 5, 16, 60, 73, 89, 101, 5,
		8, 15, 20, 57, 60, 73, 89, 101, 10, 23, 46, 64, 65, 77, 95, 107,
		59, 77, 95, 0, 2, 16, 34, 40, 52, 16, 34, 40, 45, 52, 86, 88,
		18, 44, 46, 76, 85, 4, 10, 16, 23, 34, 40, 52, 60, 63, 73, 81,
		89, 90, 101, 109, 122, 15, 16, 34, 40, 45, 52, 86, 88, 9, 16, 21,
		34, 40, 48, 50, 52, 29, 40, 53, 94, 106, 16, 34, 40, 52, 16, 34,
		40, 52, 16, 34, 40, 52, 16, 34, 40, 52, 16, 34, 40, 52, 68, 80,
		92, 82, 96, 97, 99, 113, 0, 0, 0, 2, 4, 10, 23, 25, 43, 59,
		60, 73, 89, 94, 101, 106, 108, 118, 9, 21, 48, 50, 16, 34, 16, 34,
		40, 52, 42, 4, 4, 10, 8, 20, 10, 10, 23, 43, 63, 81, 63, 81,
		90, 80, 80, 92, 10, 23, 63, 81, 90, 109, 122, 20, 47, 73, 60, 73,
		89, 101, 73, 94, 106, 68, 80, 92, 82, 96, 82, 96, 97, 0, 34, 9,
		21, 48, 50, 57, 68, 80, 92, 63, 81, 90, 109, 122, 94, 106, 82, 96,
	};

    private static final short lapg_sym_to[] = {
		125, 5, 7, 7, 28, 43, 44, 7, 7, 28, 28, 7, 44, 28, 7, 7,
		76, 85, 7, 107, 7, 107, 7, 7, 56, 12, 29, 71, 71, 71, 71, 13,
		14, 26, 14, 67, 72, 72, 72, 72, 19, 19, 63, 63, 81, 90, 90, 118,
		69, 91, 112, 1, 1, 30, 30, 30, 30, 31, 31, 31, 61, 31, 61, 101,
		42, 60, 42, 89, 60, 8, 20, 32, 47, 32, 32, 32, 73, 47, 73, 47,
		73, 47, 73, 47, 47, 27, 33, 33, 33, 62, 33, 100, 102, 16, 34, 16,
		34, 34, 16, 16, 34, 51, 54, 66, 108, 108, 35, 35, 35, 35, 36, 36,
		36, 36, 37, 37, 37, 37, 38, 38, 38, 38, 39, 39, 39, 39, 82, 82,
		82, 96, 96, 96, 115, 121, 124, 2, 3, 6, 9, 21, 48, 50, 58, 70,
		74, 74, 74, 109, 74, 109, 119, 122, 17, 17, 17, 17, 40, 52, 41, 41,
		55, 55, 57, 10, 11, 22, 15, 15, 23, 24, 49, 59, 77, 95, 78, 78,
		104, 92, 93, 105, 25, 25, 79, 79, 79, 79, 79, 45, 45, 86, 75, 87,
		103, 116, 88, 110, 110, 83, 83, 83, 97, 97, 98, 98, 114, 4, 53, 18,
		46, 64, 65, 68, 84, 94, 106, 80, 80, 80, 120, 123, 111, 117, 99, 113,
	};

    private static final short lapg_rlen[] = {
		0, 1, 3, 2, 1, 3, 3, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 2, 1, 3, 0, 1, 3,
		0, 1, 0, 1, 6, 2, 1, 2, 1, 5, 6, 4, 1, 2, 1, 3,
		0, 1, 0, 1, 4, 3, 1, 2, 5, 3, 3, 3, 5, 1, 1, 1,
		3, 3, 1, 3, 2, 0, 1, 3, 2, 1, 3,
	};

    private static final short lapg_rlex[] = {
		53, 53, 27, 28, 28, 29, 29, 30, 31, 31, 32, 32, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 54, 54, 33, 34, 35, 35, 36, 55, 55, 36,
		56, 56, 57, 57, 36, 37, 37, 38, 38, 39, 39, 39, 40, 40, 41, 41,
		58, 58, 59, 59, 42, 42, 43, 43, 44, 44, 45, 46, 46, 47, 47, 47,
		47, 47, 48, 48, 49, 60, 60, 50, 51, 51, 52,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"regexp",
		"scon",
		"icon",
		"'%'",
		"_skip",
		"'::='",
		"'|'",
		"'='",
		"';'",
		"'.'",
		"','",
		"':'",
		"'['",
		"']'",
		"'('",
		"')'",
		"'<<'",
		"'<'",
		"'>'",
		"'*'",
		"'?'",
		"'&'",
		"'{'",
		"'i{'",
		"'}'",
		"input",
		"options",
		"option",
		"symbol",
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"icon_list",
		"grammar_parts",
		"grammar_part",
		"symbols",
		"rules",
		"rule0",
		"rulesyms",
		"rulesym",
		"annotations",
		"map_entries",
		"expression",
		"expression_list",
		"rule_priority",
		"command",
		"command_tokens",
		"command_token",
		"optionsopt",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"commandopt",
		"annotationsopt",
		"rule_priorityopt",
		"command_tokensopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 27;
		public static final int options = 28;
		public static final int option = 29;
		public static final int symbol = 30;
		public static final int type = 31;
		public static final int type_part_list = 32;
		public static final int type_part = 33;
		public static final int pattern = 34;
		public static final int lexer_parts = 35;
		public static final int lexer_part = 36;
		public static final int icon_list = 37;
		public static final int grammar_parts = 38;
		public static final int grammar_part = 39;
		public static final int symbols = 40;
		public static final int rules = 41;
		public static final int rule0 = 42;
		public static final int rulesyms = 43;
		public static final int rulesym = 44;
		public static final int annotations = 45;
		public static final int map_entries = 46;
		public static final int expression = 47;
		public static final int expression_list = 48;
		public static final int rule_priority = 49;
		public static final int command = 50;
		public static final int command_tokens = 51;
		public static final int command_token = 52;
		public static final int optionsopt = 53;
		public static final int type_part_listopt = 54;
		public static final int typeopt = 55;
		public static final int iconopt = 56;
		public static final int commandopt = 57;
		public static final int annotationsopt = 58;
		public static final int rule_priorityopt = 59;
		public static final int command_tokensopt = 60;
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

	public AstRoot parse(LapgLexer lexer) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while( lapg_m[lapg_head].state != 125 ) {
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

		if( lapg_m[lapg_head].state != 125 ) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			throw new ParseException();
		};
		return (AstRoot)lapg_m[lapg_head-1].sym;
	}

	private void shift(LapgLexer lexer) throws IOException {
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
			case 2:  // input ::= optionsopt lexer_parts grammar_parts
				  lapg_gg.sym = new AstRoot(((List<AstOption>)lapg_m[lapg_head-2].sym), ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // options ::= options option
				 ((List<AstOption>)lapg_m[lapg_head-1].sym).add(((AstOption)lapg_m[lapg_head-0].sym)); 
				break;
			case 4:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOption>(16); ((List<AstOption>)lapg_gg.sym).add(((AstOption)lapg_m[lapg_head-0].sym)); 
				break;
			case 5:  // option ::= '.' identifier scon
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // option ::= '.' identifier icon
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-1].sym), ((Integer)lapg_m[lapg_head-0].sym).toString(), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 9:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 25:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 26:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 27:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 31:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // lexer_part ::= symbol typeopt ':' pattern iconopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((AstRegexp)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 37:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 38:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 39:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 40:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 41:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 42:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 43:  // grammar_part ::= '%' identifier symbols ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstIdentifier>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // symbols ::= symbol
				 lapg_gg.sym = new ArrayList<AstIdentifier>(); ((List<AstIdentifier>)lapg_gg.sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 45:  // symbols ::= symbols symbol
				 ((List<AstIdentifier>)lapg_m[lapg_head-1].sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 46:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 47:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 52:  // rule0 ::= annotationsopt rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 53:  // rule0 ::= annotationsopt commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-2].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 55:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 56:  // rulesym ::= commandopt identifier '=' symbol annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((AstIdentifier)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 57:  // rulesym ::= commandopt symbol annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstIdentifier)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 58:  // annotations ::= '[' map_entries ']'
				 lapg_gg.sym = ((Map<String,Object>)lapg_m[lapg_head-1].sym); 
				break;
			case 59:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new HashMap<String,Object>(); ((Map<String,Object>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-0].sym); 
				break;
			case 60:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((Map<String,Object>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-0].sym); 
				break;
			case 64:  // expression ::= '[' map_entries ']'
				 lapg_gg.sym = ((Map<String,Object>)lapg_m[lapg_head-1].sym); 
				break;
			case 65:  // expression ::= '[' expression_list ']'
				 lapg_gg.sym = ((List<Object>)lapg_m[lapg_head-1].sym); 
				break;
			case 66:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<Object>)lapg_gg.sym).add(lapg_m[lapg_head-0].sym); 
				break;
			case 67:  // expression_list ::= expression_list ',' expression
				 ((List<Object>)lapg_gg.sym).add(lapg_m[lapg_head-0].sym); 
				break;
			case 68:  // rule_priority ::= '<<' symbol
				 lapg_gg.sym = ((AstIdentifier)lapg_m[lapg_head-0].sym); 
				break;
			case 71:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
