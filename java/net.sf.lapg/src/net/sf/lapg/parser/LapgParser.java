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
		-3, -1, -11, 3, -1, -1, 4, 6, -1, -19, -1, 26, 61, 62, -1, 63,
		5, 37, -1, -1, 30, -1, -1, -1, -25, 27, -33, 39, -1, -43, -1, 66,
		-1, 38, 28, 15, -1, 17, 18, 13, 14, -51, 11, 12, 16, 19, 21, 20,
		-1, 10, -79, -1, -1, -1, -1, -1, -89, 40, -95, -1, -1, 64, -1, 65,
		7, -101, -1, 8, 9, 25, -129, 44, -1, 58, -141, -1, -1, 59, -1, 67,
		24, 33, -155, 43, 45, -1, 46, 49, -165, -177, -1, -191, 35, 36, -197, 41,
		-211, 54, -223, -1, 60, -233, -239, 73, -1, 47, 55, -245, -255, -1, -271, 51,
		53, 42, -1, 72, 71, 52, -1, 68, 57, 74, -285, 56, -1, -2,
	};

    private static final short lapg_lalr[] = {
		11, -1, 1, 0, 14, 0, -1, -2, 11, -1, 1, 1, 14, 1, -1, -2,
		16, -1, 13, 29, -1, -2, 16, -1, 7, 29, 13, 29, -1, -2, 1, -1,
		5, -1, 14, -1, 0, 2, -1, -2, 13, -1, 12, 6, 15, 6, -1, -2,
		1, -1, 11, -1, 12, -1, 14, -1, 15, -1, 16, -1, 19, -1, 20, -1,
		21, -1, 22, -1, 23, -1, 24, -1, 17, 22, -1, -2, 2, -1, 1, 31,
		5, 31, 14, 31, -1, -2, 16, -1, 7, 29, -1, -2, 16, -1, 7, 29,
		-1, -2, 1, -1, 11, -1, 12, -1, 14, -1, 15, -1, 16, -1, 19, -1,
		20, -1, 21, -1, 22, -1, 23, -1, 24, -1, 17, 23, -1, -2, 4, -1,
		1, 32, 5, 32, 14, 32, 25, 32, -1, -2, 14, -1, 1, 48, 8, 48,
		10, 48, 18, 48, 25, 48, -1, -2, 25, -1, 1, 34, 5, 34, 14, 34,
		-1, -2, 25, -1, 1, 34, 8, 34, 10, 34, 18, 34, -1, -2, 14, -1,
		1, 48, 8, 48, 10, 48, 18, 48, 25, 48, -1, -2, 26, -1, 27, 69,
		-1, -2, 14, -1, 1, 48, 8, 48, 10, 48, 18, 48, 25, 48, -1, -2,
		25, -1, 1, 34, 8, 34, 10, 34, 18, 34, -1, -2, 1, -1, 18, -1,
		8, 50, 10, 50, -1, -2, 26, -1, 27, 69, -1, -2, 26, -1, 27, 70,
		-1, -2, 1, -1, 18, -1, 8, 50, 10, 50, -1, -2, 9, -1, 1, 6,
		8, 6, 10, 6, 14, 6, 18, 6, 25, 6, -1, -2, 14, -1, 1, 48,
		8, 48, 10, 48, 18, 48, 25, 48, -1, -2, 14, -1, 1, 48, 8, 48,
		10, 48, 18, 48, 25, 48, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 25, 26, 32, 41, 43, 43, 46, 48, 49, 52, 58, 65, 70, 87,
		95, 103, 106, 108, 112, 116, 120, 124, 128, 132, 135, 138, 140, 141, 142, 144,
		159, 163, 165, 169, 170, 171, 173, 175, 176, 178, 179, 181, 184, 185, 187, 194,
		197, 202, 203, 205, 208, 210, 213, 214, 215, 219, 220, 223, 228, 230, 232,
	};

    private static final short lapg_sym_from[] = {
		124, 1, 4, 5, 10, 14, 19, 22, 23, 26, 28, 41, 48, 51, 55, 59,
		60, 62, 65, 72, 90, 98, 107, 109, 118, 50, 5, 14, 19, 59, 62, 90,
		5, 8, 14, 18, 23, 59, 62, 70, 90, 10, 26, 54, 75, 76, 85, 99,
		108, 72, 85, 99, 0, 2, 19, 41, 48, 65, 19, 30, 32, 41, 48, 53,
		65, 21, 29, 52, 54, 78, 4, 5, 10, 14, 19, 26, 41, 48, 59, 62,
		65, 74, 89, 90, 94, 110, 122, 18, 19, 30, 32, 41, 48, 53, 65, 9,
		19, 24, 41, 48, 56, 58, 65, 36, 48, 66, 98, 107, 19, 41, 48, 65,
		19, 41, 48, 65, 19, 41, 48, 65, 19, 41, 48, 65, 19, 41, 48, 65,
		19, 41, 48, 65, 82, 88, 96, 91, 101, 102, 104, 114, 0, 0, 0, 2,
		4, 5, 10, 14, 26, 28, 51, 59, 62, 72, 90, 98, 107, 109, 118, 9,
		24, 56, 58, 19, 41, 19, 41, 48, 65, 50, 4, 4, 10, 8, 23, 10,
		10, 26, 51, 74, 89, 74, 89, 94, 88, 88, 96, 10, 26, 74, 89, 94,
		110, 122, 14, 23, 55, 5, 14, 59, 62, 90, 14, 98, 107, 82, 88, 96,
		91, 101, 91, 101, 102, 0, 41, 9, 24, 56, 58, 70, 82, 88, 96, 74,
		89, 94, 110, 122, 98, 107, 91, 101,
	};

    private static final short lapg_sym_to[] = {
		125, 5, 7, 7, 7, 29, 35, 51, 52, 7, 7, 35, 35, 7, 52, 7,
		78, 7, 35, 7, 7, 108, 108, 7, 7, 69, 12, 12, 36, 12, 12, 12,
		13, 17, 13, 33, 17, 13, 13, 81, 13, 22, 22, 74, 74, 89, 94, 94,
		118, 83, 95, 113, 1, 1, 37, 37, 37, 37, 38, 60, 62, 38, 38, 60,
		38, 50, 59, 59, 50, 90, 8, 14, 23, 14, 39, 55, 39, 39, 14, 14,
		39, 55, 55, 14, 55, 55, 55, 34, 40, 61, 63, 40, 40, 73, 40, 19,
		41, 19, 41, 41, 19, 19, 41, 64, 67, 80, 109, 109, 42, 42, 42, 42,
		43, 43, 43, 43, 44, 44, 44, 44, 45, 45, 45, 45, 46, 46, 46, 46,
		47, 47, 47, 47, 91, 91, 91, 101, 101, 101, 116, 121, 124, 2, 3, 6,
		9, 15, 24, 15, 56, 58, 71, 15, 15, 84, 15, 110, 110, 119, 122, 20,
		20, 20, 20, 48, 65, 49, 49, 68, 68, 70, 10, 11, 25, 18, 18, 26,
		27, 57, 72, 85, 99, 86, 86, 105, 96, 97, 106, 28, 28, 87, 87, 87,
		87, 87, 30, 53, 53, 16, 31, 77, 79, 100, 32, 111, 111, 92, 92, 92,
		102, 102, 103, 103, 115, 4, 66, 21, 54, 75, 76, 82, 93, 98, 107, 88,
		88, 88, 120, 123, 112, 117, 104, 114,
	};

    private static final short lapg_rlen[] = {
		0, 1, 3, 1, 2, 3, 1, 3, 3, 2, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 3, 0, 1, 3,
		0, 1, 0, 1, 6, 1, 2, 1, 2, 5, 6, 4, 1, 2, 1, 3,
		0, 1, 0, 1, 4, 3, 1, 2, 5, 3, 3, 3, 5, 1, 1, 1,
		3, 3, 1, 3, 2, 0, 1, 3, 2, 1, 3,
	};

    private static final short lapg_rlex[] = {
		54, 54, 28, 29, 29, 30, 31, 32, 32, 33, 33, 34, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 55, 55, 34, 35, 36, 36, 37, 56, 56, 37,
		57, 57, 58, 58, 37, 38, 38, 39, 39, 40, 40, 40, 41, 41, 42, 42,
		59, 59, 60, 60, 43, 43, 44, 44, 45, 45, 46, 47, 47, 48, 48, 48,
		48, 48, 49, 49, 50, 61, 61, 51, 52, 52, 53,
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
		"'@'",
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
		public static final int input = 28;
		public static final int options = 29;
		public static final int option = 30;
		public static final int symbol = 31;
		public static final int type = 32;
		public static final int type_part_list = 33;
		public static final int type_part = 34;
		public static final int pattern = 35;
		public static final int lexer_parts = 36;
		public static final int lexer_part = 37;
		public static final int icon_list = 38;
		public static final int grammar_parts = 39;
		public static final int grammar_part = 40;
		public static final int symbols = 41;
		public static final int rules = 42;
		public static final int rule0 = 43;
		public static final int rulesyms = 44;
		public static final int rulesym = 45;
		public static final int annotations = 46;
		public static final int map_entries = 47;
		public static final int expression = 48;
		public static final int expression_list = 49;
		public static final int rule_priority = 50;
		public static final int command = 51;
		public static final int command_tokens = 52;
		public static final int command_token = 53;
		public static final int optionsopt = 54;
		public static final int type_part_listopt = 55;
		public static final int typeopt = 56;
		public static final int iconopt = 57;
		public static final int commandopt = 58;
		public static final int annotationsopt = 59;
		public static final int rule_priorityopt = 60;
		public static final int command_tokensopt = 61;
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
			case 3:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOption>(16); ((List<AstOption>)lapg_gg.sym).add(((AstOption)lapg_m[lapg_head-0].sym)); 
				break;
			case 4:  // options ::= options option
				 ((List<AstOption>)lapg_m[lapg_head-1].sym).add(((AstOption)lapg_m[lapg_head-0].sym)); 
				break;
			case 5:  // option ::= '.' identifier expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-1].sym), lapg_m[lapg_head-0].sym, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 8:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 25:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 26:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 27:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
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
			case 37:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 38:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 39:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 40:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
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
