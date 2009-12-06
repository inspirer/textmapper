package net.sf.lapg.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
		13, -1, -1, -25, 9, -33, 23, 20, 11, -41, -1, -1, -51, 22, 8, -57,
		29, -1, -69, -1, 16, -81, 25, 28, -91, -1, 27, -97, 18, -109, 19, -119,
		-125, 41, -1, -131, 24, -143, -1, 35, 31, 33, -1, 40, 39, 26, 34, 32,
		36, 42, -1, -2,
	};

    private static final short lapg_lalr[] = {
		11, -1, 1, 0, 13, 0, -1, -2, 11, -1, 1, 1, 13, 1, -1, -2,
		4, -1, 12, 12, -1, -2, 4, -1, 8, 12, 12, 12, -1, -2, 1, -1,
		6, -1, 0, 2, -1, -2, 2, -1, 1, 14, 6, 14, 13, 14, -1, -2,
		4, -1, 8, 12, -1, -2, 5, -1, 1, 15, 6, 15, 13, 15, 16, 15,
		-1, -2, 16, -1, 1, 17, 9, 17, 10, 17, 15, 17, -1, -2, 16, -1,
		1, 17, 6, 17, 13, 17, -1, -2, 17, -1, 18, 37, -1, -2, 16, -1,
		1, 17, 9, 17, 10, 17, 15, 17, -1, -2, 1, -1, 15, -1, 9, 30,
		10, 30, -1, -2, 17, -1, 18, 37, -1, -2, 17, -1, 18, 38, -1, -2,
		16, -1, 1, 17, 9, 17, 10, 17, 15, 17, -1, -2, 1, -1, 15, -1,
		9, 30, 10, 30, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 11, 12, 13, 16, 20, 22, 22, 24, 25, 27, 29, 31, 33, 34,
		36, 40, 43, 45, 46, 47, 49, 56, 57, 58, 60, 61, 62, 64, 65, 66,
		68, 70, 72, 76, 78, 81, 82, 85, 86, 90, 92, 94,
	};

    private static final short lapg_sym_from[] = {
		66, 1, 4, 10, 18, 21, 26, 33, 45, 53, 54, 25, 5, 9, 19, 28,
		5, 8, 15, 31, 10, 21, 27, 35, 41, 33, 41, 0, 2, 17, 27, 4,
		10, 15, 45, 53, 34, 37, 43, 51, 40, 47, 48, 50, 58, 0, 0, 0,
		2, 4, 10, 21, 26, 33, 45, 53, 25, 4, 4, 10, 8, 10, 10, 21,
		34, 26, 34, 51, 34, 51, 45, 53, 34, 37, 43, 51, 40, 47, 40, 47,
		48, 0, 9, 19, 28, 31, 34, 37, 43, 51, 45, 53, 40, 47,
	};

    private static final short lapg_sym_to[] = {
		67, 5, 7, 7, 26, 7, 7, 7, 7, 7, 64, 30, 12, 16, 16, 16,
		13, 14, 23, 36, 18, 18, 34, 34, 51, 38, 52, 1, 1, 25, 25, 8,
		8, 24, 54, 54, 40, 40, 40, 40, 47, 47, 47, 60, 65, 66, 2, 3,
		6, 9, 19, 28, 32, 39, 55, 62, 31, 10, 11, 20, 15, 21, 22, 29,
		41, 33, 42, 61, 43, 43, 56, 56, 44, 44, 44, 44, 48, 48, 49, 49,
		59, 4, 17, 27, 35, 37, 45, 46, 53, 45, 57, 63, 50, 58,
	};

    private static final short lapg_rlen[] = {
		0, 1, 3, 2, 1, 3, 3, 1, 1, 2, 1, 3, 0, 1, 3, 0,
		1, 0, 1, 6, 2, 1, 2, 1, 5, 4, 3, 1, 2, 1, 0, 1,
		3, 2, 3, 2, 2, 0, 1, 3, 2, 1, 3,
	};

    private static final short lapg_rlex[] = {
		37, 37, 19, 20, 20, 21, 21, 22, 23, 24, 24, 25, 38, 38, 25, 39,
		39, 40, 40, 25, 26, 26, 27, 27, 28, 28, 29, 29, 30, 30, 41, 41,
		31, 31, 32, 32, 33, 42, 42, 34, 35, 35, 36,
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
		"';'",
		"'.'",
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
		"rule_list",
		"symbol_list",
		"rule_right",
		"rule_symbols",
		"rule_priority",
		"command",
		"command_tokens",
		"command_token",
		"optionsopt",
		"typeopt",
		"iconopt",
		"commandopt",
		"rule_priorityopt",
		"command_tokensopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 19;
		public static final int options = 20;
		public static final int option = 21;
		public static final int symbol = 22;
		public static final int pattern = 23;
		public static final int lexer_parts = 24;
		public static final int lexer_part = 25;
		public static final int icon_list = 26;
		public static final int grammar_parts = 27;
		public static final int grammar_part = 28;
		public static final int rule_list = 29;
		public static final int symbol_list = 30;
		public static final int rule_right = 31;
		public static final int rule_symbols = 32;
		public static final int rule_priority = 33;
		public static final int command = 34;
		public static final int command_tokens = 35;
		public static final int command_token = 36;
		public static final int optionsopt = 37;
		public static final int typeopt = 38;
		public static final int iconopt = 39;
		public static final int commandopt = 40;
		public static final int rule_priorityopt = 41;
		public static final int command_tokensopt = 42;
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

		while( lapg_m[lapg_head].state != 67 ) {
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

		if( lapg_m[lapg_head].state != 67 ) {
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
			case 24:  // grammar_part ::= symbol typeopt '::=' rule_list ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRuleRight>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 25:  // grammar_part ::= '%' identifier symbol_list ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstIdentifier>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 26:  // rule_list ::= rule_list '|' rule_right
				 ((List<AstRuleRight>)lapg_m[lapg_head-2].sym).add(((AstRuleRight)lapg_m[lapg_head-0].sym)); 
				break;
			case 27:  // rule_list ::= rule_right
				 lapg_gg.sym = new ArrayList<AstRuleRight>(); ((List<AstRuleRight>)lapg_gg.sym).add(((AstRuleRight)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // symbol_list ::= symbol_list symbol
				 ((List<AstIdentifier>)lapg_m[lapg_head-1].sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // symbol_list ::= symbol
				 lapg_gg.sym = new ArrayList<AstIdentifier>(); ((List<AstIdentifier>)lapg_gg.sym).add(((AstIdentifier)lapg_m[lapg_head-0].sym)); 
				break;
			case 32:  // rule_right ::= rule_symbols commandopt rule_priorityopt
				 lapg_gg.sym = new AstRuleRight(((List<AstRightSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // rule_right ::= commandopt rule_priorityopt
				 lapg_gg.sym = new AstRuleRight(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 34:  // rule_symbols ::= rule_symbols commandopt symbol
				 ((List<AstRightSymbol>)lapg_m[lapg_head-2].sym).add(new AstRightSymbol(((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 35:  // rule_symbols ::= commandopt symbol
				 lapg_gg.sym = new ArrayList<AstRightSymbol>(); ((List<AstRightSymbol>)lapg_gg.sym).add(new AstRightSymbol(((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 36:  // rule_priority ::= '<<' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 39:  // command ::= '{' command_tokensopt '}'
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
