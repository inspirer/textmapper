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
		-3, -1, -11, 4, -1, -1, 3, 7, -1, -19, -1, 10, 5, 6, 21, -1,
		13, -1, -1, -1, -25, 9, -33, 23, -1, 20, 11, -43, -1, -1, -1, -1,
		-1, -53, 22, -59, 8, -65, 27, -1, -1, -1, 41, -77, -1, -1, 16, -91,
		26, 28, 44, 45, 46, 42, -1, -1, 29, 32, -101, -113, -127, 18, 19, -1,
		-133, 24, -147, 37, -159, -1, -169, -175, 52, -1, 43, 30, 38, -181, -191, -1,
		-207, 34, 36, 25, -1, 51, 50, 35, -1, 47, 40, 53, -221, 39, -1, -2,
	};

    private static final short lapg_lalr[] = {
		12, -1, 1, 0, 15, 0, -1, -2, 12, -1, 1, 1, 15, 1, -1, -2,
		4, -1, 14, 12, -1, -2, 4, -1, 8, 12, 14, 12, -1, -2, 1, -1,
		6, -1, 15, -1, 0, 2, -1, -2, 2, -1, 1, 14, 6, 14, 15, 14,
		-1, -2, 4, -1, 8, 12, -1, -2, 4, -1, 8, 12, -1, -2, 5, -1,
		1, 15, 6, 15, 15, 15, 18, 15, -1, -2, 15, -1, 1, 31, 9, 31,
		11, 31, 17, 31, 18, 31, -1, -2, 18, -1, 1, 17, 6, 17, 15, 17,
		-1, -2, 18, -1, 1, 17, 9, 17, 11, 17, 17, 17, -1, -2, 15, -1,
		1, 31, 9, 31, 11, 31, 17, 31, 18, 31, -1, -2, 19, -1, 20, 48,
		-1, -2, 15, -1, 1, 31, 9, 31, 11, 31, 17, 31, 18, 31, -1, -2,
		18, -1, 1, 17, 9, 17, 11, 17, 17, 17, -1, -2, 1, -1, 17, -1,
		9, 33, 11, 33, -1, -2, 19, -1, 20, 48, -1, -2, 19, -1, 20, 49,
		-1, -2, 1, -1, 17, -1, 9, 33, 11, 33, -1, -2, 10, -1, 1, 7,
		9, 7, 11, 7, 15, 7, 17, 7, 18, 7, -1, -2, 15, -1, 1, 31,
		9, 31, 11, 31, 17, 31, 18, 31, -1, -2, 15, -1, 1, 31, 9, 31,
		11, 31, 17, 31, 18, 31, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 18, 19, 22, 26, 33, 35, 35, 38, 40, 41, 44, 46, 47, 51,
		59, 61, 63, 66, 69, 71, 72, 73, 75, 87, 88, 89, 91, 93, 94, 96,
		97, 99, 102, 103, 105, 112, 114, 116, 118, 121, 123, 126, 127, 131, 132, 135,
		140, 142, 144,
	};

    private static final short lapg_sym_from[] = {
		94, 1, 4, 10, 18, 19, 22, 24, 28, 32, 39, 40, 41, 63, 68, 77,
		79, 88, 27, 5, 40, 63, 9, 20, 33, 35, 5, 8, 15, 19, 37, 40,
		63, 10, 22, 31, 44, 45, 55, 69, 78, 39, 55, 69, 0, 2, 30, 17,
		29, 31, 54, 4, 10, 22, 43, 59, 64, 80, 92, 15, 30, 68, 77, 47,
		58, 66, 60, 70, 71, 73, 84, 0, 0, 0, 2, 4, 10, 22, 24, 28,
		39, 40, 63, 68, 77, 79, 88, 27, 4, 4, 10, 8, 19, 10, 10, 22,
		28, 43, 59, 43, 59, 64, 58, 58, 66, 10, 22, 43, 59, 64, 80, 92,
		19, 32, 40, 63, 68, 77, 47, 58, 66, 60, 70, 60, 70, 71, 0, 9,
		20, 33, 35, 37, 47, 58, 66, 43, 59, 64, 80, 92, 68, 77, 60, 70,
	};

    private static final short lapg_sym_to[] = {
		95, 5, 7, 7, 28, 29, 7, 7, 7, 29, 7, 7, 54, 7, 78, 78,
		7, 7, 36, 12, 50, 50, 16, 16, 16, 16, 13, 14, 25, 14, 46, 51,
		51, 18, 18, 43, 43, 59, 64, 64, 88, 48, 65, 83, 1, 1, 41, 27,
		40, 27, 63, 8, 19, 32, 32, 32, 32, 32, 32, 26, 42, 79, 79, 60,
		60, 60, 70, 70, 70, 86, 91, 94, 2, 3, 6, 9, 20, 33, 35, 38,
		49, 52, 52, 80, 80, 89, 92, 37, 10, 11, 21, 15, 15, 22, 23, 34,
		39, 55, 69, 56, 56, 75, 66, 67, 76, 24, 24, 57, 57, 57, 57, 57,
		30, 30, 53, 74, 81, 81, 61, 61, 61, 71, 71, 72, 72, 85, 4, 17,
		31, 44, 45, 47, 62, 68, 77, 58, 58, 58, 90, 93, 82, 87, 73, 84,
	};

    private static final short lapg_rlen[] = {
		0, 1, 3, 2, 1, 3, 3, 1, 1, 2, 1, 3, 0, 1, 3, 0,
		1, 0, 1, 6, 2, 1, 2, 1, 5, 6, 4, 1, 2, 1, 3, 0,
		1, 0, 1, 4, 3, 1, 2, 5, 3, 3, 3, 5, 1, 1, 1, 2,
		0, 1, 3, 2, 1, 3,
	};

    private static final short lapg_rlex[] = {
		43, 43, 21, 22, 22, 23, 23, 24, 25, 26, 26, 27, 44, 44, 27, 45,
		45, 46, 46, 27, 28, 28, 29, 29, 30, 30, 30, 31, 31, 32, 32, 47,
		47, 48, 48, 33, 33, 34, 34, 35, 35, 36, 37, 37, 38, 38, 38, 39,
		49, 49, 40, 41, 41, 42,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"regexp",
		"scon",
		"type",
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
		"'<<'",
		"'{'",
		"'i{'",
		"'}'",
		"input",
		"options",
		"option",
		"symbol",
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
		"rule_priority",
		"command",
		"command_tokens",
		"command_token",
		"optionsopt",
		"typeopt",
		"iconopt",
		"commandopt",
		"annotationsopt",
		"rule_priorityopt",
		"command_tokensopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 21;
		public static final int options = 22;
		public static final int option = 23;
		public static final int symbol = 24;
		public static final int pattern = 25;
		public static final int lexer_parts = 26;
		public static final int lexer_part = 27;
		public static final int icon_list = 28;
		public static final int grammar_parts = 29;
		public static final int grammar_part = 30;
		public static final int symbols = 31;
		public static final int rules = 32;
		public static final int rule0 = 33;
		public static final int rulesyms = 34;
		public static final int rulesym = 35;
		public static final int annotations = 36;
		public static final int map_entries = 37;
		public static final int expression = 38;
		public static final int rule_priority = 39;
		public static final int command = 40;
		public static final int command_tokens = 41;
		public static final int command_token = 42;
		public static final int optionsopt = 43;
		public static final int typeopt = 44;
		public static final int iconopt = 45;
		public static final int commandopt = 46;
		public static final int annotationsopt = 47;
		public static final int rule_priorityopt = 48;
		public static final int command_tokensopt = 49;
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

		while( lapg_m[lapg_head].state != 95 ) {
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

		if( lapg_m[lapg_head].state != 95 ) {
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
			case 8:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 10:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 11:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 14:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 19:  // lexer_part ::= symbol typeopt ':' pattern iconopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((AstRegexp)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 20:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 21:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 22:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 23:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 24:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 25:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 26:  // grammar_part ::= '%' identifier symbols ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstIdentifier>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 27:  // symbols ::= symbol
				 lapg_gg.sym = new ArrayList<AstIdentifier>(); ((List<AstIdentifier>)lapg_gg.sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // symbols ::= symbols symbol
				 ((List<AstIdentifier>)lapg_m[lapg_head-1].sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 30:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 35:  // rule0 ::= annotationsopt rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // rule0 ::= annotationsopt commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-2].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 37:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 38:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 39:  // rulesym ::= commandopt identifier '=' symbol annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((AstIdentifier)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-4].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 40:  // rulesym ::= commandopt symbol annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstIdentifier)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-1].endoffset); 
				break;
			case 41:  // annotations ::= '[' map_entries ']'
				 lapg_gg.sym = ((Map<String,Object>)lapg_m[lapg_head-1].sym); 
				break;
			case 42:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new HashMap<String,Object>(); ((Map<String,Object>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-0].sym); 
				break;
			case 43:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((Map<String,Object>)lapg_gg.sym).put(((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-0].sym); 
				break;
			case 47:  // rule_priority ::= '<<' symbol
				 lapg_gg.sym = ((AstIdentifier)lapg_m[lapg_head-0].sym); 
				break;
			case 50:  // command ::= '{' command_tokensopt '}'
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
