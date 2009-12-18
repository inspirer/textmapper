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
		-3, -1, -11, 4, -1, -1, 3, 7, -1, -19, -1, 28, 5, 6, 39, -1,
		-1, 31, -1, -1, -1, -25, 27, -33, 41, -1, 38, 29, 16, -1, 18, 19,
		14, 15, -43, 12, 13, 17, 20, 22, 21, -1, 11, -71, -1, -1, -1, -1,
		-1, -81, 40, -87, 8, -93, -1, 9, 10, 26, -121, 45, -1, -1, -1, 59,
		-133, -1, -1, 25, 34, -147, 44, 46, 62, 63, -1, 64, 60, -1, -1, 47,
		50, -157, -169, -183, 36, 37, -189, -1, 67, -1, -1, -197, 42, -211, 55, -223,
		-1, -233, -239, 74, -1, 65, -1, 66, 61, 48, 56, -245, -255, -1, -271, 52,
		54, 43, -1, 73, 72, 68, 53, -1, 69, 58, 75, -285, 57, -1, -2,
	};

    private static final short lapg_lalr[] = {
		11, -1, 1, 0, 14, 0, -1, -2, 11, -1, 1, 1, 14, 1, -1, -2,
		16, -1, 13, 30, -1, -2, 16, -1, 7, 30, 13, 30, -1, -2, 1, -1,
		5, -1, 14, -1, 0, 2, -1, -2, 1, -1, 11, -1, 12, -1, 14, -1,
		15, -1, 16, -1, 19, -1, 20, -1, 21, -1, 22, -1, 23, -1, 24, -1,
		17, 23, -1, -2, 2, -1, 1, 32, 5, 32, 14, 32, -1, -2, 16, -1,
		7, 30, -1, -2, 16, -1, 7, 30, -1, -2, 1, -1, 11, -1, 12, -1,
		14, -1, 15, -1, 16, -1, 19, -1, 20, -1, 21, -1, 22, -1, 23, -1,
		24, -1, 17, 24, -1, -2, 4, -1, 1, 33, 5, 33, 14, 33, 25, 33,
		-1, -2, 14, -1, 1, 49, 8, 49, 10, 49, 18, 49, 25, 49, -1, -2,
		25, -1, 1, 35, 5, 35, 14, 35, -1, -2, 25, -1, 1, 35, 8, 35,
		10, 35, 18, 35, -1, -2, 14, -1, 1, 49, 8, 49, 10, 49, 18, 49,
		25, 49, -1, -2, 26, -1, 27, 70, -1, -2, 13, -1, 12, 7, 15, 7,
		-1, -2, 14, -1, 1, 49, 8, 49, 10, 49, 18, 49, 25, 49, -1, -2,
		25, -1, 1, 35, 8, 35, 10, 35, 18, 35, -1, -2, 1, -1, 18, -1,
		8, 51, 10, 51, -1, -2, 26, -1, 27, 70, -1, -2, 26, -1, 27, 71,
		-1, -2, 1, -1, 18, -1, 8, 51, 10, 51, -1, -2, 9, -1, 1, 7,
		8, 7, 10, 7, 14, 7, 18, 7, 25, 7, -1, -2, 14, -1, 1, 49,
		8, 49, 10, 49, 18, 49, 25, 49, -1, -2, 14, -1, 1, 49, 8, 49,
		10, 49, 18, 49, 25, 49, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 24, 25, 31, 40, 42, 42, 45, 47, 48, 51, 57, 64, 69, 85,
		93, 101, 104, 106, 110, 114, 118, 122, 126, 130, 133, 136, 138, 139, 140, 142,
		156, 160, 162, 166, 167, 168, 170, 172, 173, 175, 176, 178, 181, 182, 184, 191,
		194, 198, 199, 201, 204, 206, 209, 210, 211, 215, 216, 219, 224, 226, 228,
	};

    private static final short lapg_sym_from[] = {
		125, 1, 4, 10, 16, 19, 20, 23, 25, 34, 41, 44, 48, 53, 60, 61,
		62, 74, 90, 95, 102, 107, 109, 119, 43, 5, 16, 61, 74, 90, 102, 5,
		8, 15, 20, 58, 61, 74, 90, 102, 10, 23, 47, 65, 66, 78, 96, 108,
		60, 78, 96, 0, 2, 16, 34, 41, 53, 16, 34, 41, 46, 53, 87, 89,
		18, 45, 47, 77, 86, 4, 10, 16, 23, 34, 41, 53, 61, 64, 74, 82,
		90, 91, 102, 110, 123, 15, 16, 34, 41, 46, 53, 87, 89, 9, 16, 21,
		34, 41, 49, 51, 53, 29, 41, 54, 95, 107, 16, 34, 41, 53, 16, 34,
		41, 53, 16, 34, 41, 53, 16, 34, 41, 53, 16, 34, 41, 53, 16, 34,
		41, 53, 69, 81, 93, 83, 97, 98, 100, 114, 0, 0, 0, 2, 4, 10,
		23, 25, 44, 60, 61, 74, 90, 95, 102, 107, 109, 119, 9, 21, 49, 51,
		16, 34, 16, 34, 41, 53, 43, 4, 4, 10, 8, 20, 10, 10, 23, 44,
		64, 82, 64, 82, 91, 81, 81, 93, 10, 23, 64, 82, 91, 110, 123, 20,
		48, 74, 61, 74, 90, 102, 74, 95, 107, 69, 81, 93, 83, 97, 83, 97,
		98, 0, 34, 9, 21, 49, 51, 58, 69, 81, 93, 64, 82, 91, 110, 123,
		95, 107, 83, 97,
	};

    private static final short lapg_sym_to[] = {
		126, 5, 7, 7, 28, 44, 45, 7, 7, 28, 28, 7, 45, 28, 7, 7,
		77, 86, 7, 108, 7, 108, 7, 7, 57, 12, 29, 72, 72, 72, 72, 13,
		14, 26, 14, 68, 73, 73, 73, 73, 19, 19, 64, 64, 82, 91, 91, 119,
		70, 92, 113, 1, 1, 30, 30, 30, 30, 31, 31, 31, 62, 31, 62, 102,
		43, 61, 43, 90, 61, 8, 20, 32, 48, 32, 32, 32, 74, 48, 74, 48,
		74, 48, 74, 48, 48, 27, 33, 33, 33, 63, 33, 101, 103, 16, 34, 16,
		34, 34, 16, 16, 34, 52, 55, 67, 109, 109, 35, 35, 35, 35, 36, 36,
		36, 36, 37, 37, 37, 37, 38, 38, 38, 38, 39, 39, 39, 39, 40, 40,
		40, 40, 83, 83, 83, 97, 97, 97, 116, 122, 125, 2, 3, 6, 9, 21,
		49, 51, 59, 71, 75, 75, 75, 110, 75, 110, 120, 123, 17, 17, 17, 17,
		41, 53, 42, 42, 56, 56, 58, 10, 11, 22, 15, 15, 23, 24, 50, 60,
		78, 96, 79, 79, 105, 93, 94, 106, 25, 25, 80, 80, 80, 80, 80, 46,
		46, 87, 76, 88, 104, 117, 89, 111, 111, 84, 84, 84, 98, 98, 99, 99,
		115, 4, 54, 18, 47, 65, 66, 69, 85, 95, 107, 81, 81, 81, 121, 124,
		112, 118, 100, 114,
	};

    private static final short lapg_rlen[] = {
		0, 1, 3, 2, 1, 3, 3, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 2, 1, 3, 0, 1,
		3, 0, 1, 0, 1, 6, 2, 1, 2, 1, 5, 6, 4, 1, 2, 1,
		3, 0, 1, 0, 1, 4, 3, 1, 2, 5, 3, 3, 3, 5, 1, 1,
		1, 3, 3, 1, 3, 2, 0, 1, 3, 2, 1, 3,
	};

    private static final short lapg_rlex[] = {
		54, 54, 28, 29, 29, 30, 30, 31, 32, 32, 33, 33, 34, 34, 34, 34,
		34, 34, 34, 34, 34, 34, 34, 55, 55, 34, 35, 36, 36, 37, 56, 56,
		37, 57, 57, 58, 58, 37, 38, 38, 39, 39, 40, 40, 40, 41, 41, 42,
		42, 59, 59, 60, 60, 43, 43, 44, 44, 45, 45, 46, 47, 47, 48, 48,
		48, 48, 48, 49, 49, 50, 61, 61, 51, 52, 52, 53,
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

		while( lapg_m[lapg_head].state != 126 ) {
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

		if( lapg_m[lapg_head].state != 126 ) {
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
			case 26:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 27:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 32:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 37:  // lexer_part ::= symbol typeopt ':' pattern iconopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((AstRegexp)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 38:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 39:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 40:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 41:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 42:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 43:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // grammar_part ::= '%' identifier symbols ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstIdentifier>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 45:  // symbols ::= symbol
				 lapg_gg.sym = new ArrayList<AstIdentifier>(); ((List<AstIdentifier>)lapg_gg.sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 46:  // symbols ::= symbols symbol
				 ((List<AstIdentifier>)lapg_m[lapg_head-1].sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 47:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 48:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 53:  // rule0 ::= annotationsopt rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // rule0 ::= annotationsopt commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-2].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 56:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:  // rulesym ::= commandopt identifier '=' symbol annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((AstIdentifier)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 58:  // rulesym ::= commandopt symbol annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstIdentifier)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 59:  // annotations ::= '[' map_entries ']'
				 lapg_gg.sym = ((Map<String,Object>)lapg_m[lapg_head-1].sym); 
				break;
			case 60:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new HashMap<String,Object>(); ((Map<String,Object>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-0].sym); 
				break;
			case 61:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((Map<String,Object>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-0].sym); 
				break;
			case 65:  // expression ::= '[' map_entries ']'
				 lapg_gg.sym = ((Map<String,Object>)lapg_m[lapg_head-1].sym); 
				break;
			case 66:  // expression ::= '[' expression_list ']'
				 lapg_gg.sym = ((List<Object>)lapg_m[lapg_head-1].sym); 
				break;
			case 67:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<Object>)lapg_gg.sym).add(lapg_m[lapg_head-0].sym); 
				break;
			case 68:  // expression_list ::= expression_list ',' expression
				 ((List<Object>)lapg_gg.sym).add(lapg_m[lapg_head-0].sym); 
				break;
			case 69:  // rule_priority ::= '<<' symbol
				 lapg_gg.sym = ((AstIdentifier)lapg_m[lapg_head-0].sym); 
				break;
			case 72:  // command ::= '{' command_tokensopt '}'
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
