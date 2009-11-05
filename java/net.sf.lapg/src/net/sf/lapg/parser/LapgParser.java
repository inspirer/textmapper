package net.sf.lapg.parser;

import java.io.IOException;
import net.sf.lapg.parser.LapgLexer.Lexems;
import net.sf.lapg.parser.LapgLexer.LapgSymbol;
import java.io.CharArrayReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.lapg.parser.ast.*;


public class LapgParser implements LapgLexer.ErrorReporter {
	
	private static final boolean DEBUG_SYNTAX = false;
	
	TextSource source;
	AstRoot result;
	public void error(LapgLexer.LapgPlace start, LapgLexer.LapgPlace end, String s) {
		System.err.println(s);
	}
    private static final int lapg_action[] = {
		-3, -1, -1, -11, 4, -1, 7, -1, -1, -19, 10, 3, 5, 6, 21, -1,
		-1, -25, -33, 9, 23, 12, -1, 20, 11, -1, -41, 22, -1, -47, 29, -1,
		-1, -57, 8, -69, 25, 28, -81, -87, 17, -1, 27, -97, 15, -109, -119, -1,
		-125, 41, -1, 35, 33, 30, -131, 24, -143, 19, -1, 39, 40, 36, 26, 34,
		32, 42, -1, -2,
	};

    private static final short lapg_lalr[] = {
		11, -1, 1, 1, 13, 1, -1, -2, 11, -1, 1, 0, 13, 0, -1, -2,
		4, -1, 12, 13, -1, -2, 1, -1, 6, -1, 0, 2, -1, -2, 4, -1,
		8, 13, 12, 13, -1, -2, 4, -1, 8, 13, -1, -2, 2, -1, 1, 14,
		6, 14, 13, 14, -1, -2, 16, -1, 1, 18, 9, 18, 10, 18, 15, 18,
		-1, -2, 5, -1, 1, 16, 6, 16, 13, 16, 16, 16, -1, -2, 17, -1,
		18, 38, -1, -2, 1, -1, 15, -1, 9, 31, 10, 31, -1, -2, 16, -1,
		1, 18, 9, 18, 10, 18, 15, 18, -1, -2, 16, -1, 1, 18, 6, 18,
		13, 18, -1, -2, 17, -1, 18, 38, -1, -2, 17, -1, 18, 37, -1, -2,
		16, -1, 1, 18, 9, 18, 10, 18, 15, 18, -1, -2, 1, -1, 15, -1,
		9, 31, 10, 31, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 11, 12, 13, 16, 20, 22, 22, 24, 25, 27, 29, 31, 33, 34,
		36, 40, 43, 45, 46, 47, 48, 49, 50, 52, 59, 60, 62, 63, 66, 67,
		71, 75, 77, 78, 79, 81, 83, 85, 87, 89, 91, 94,
	};

    private static final short lapg_sym_from[] = {
		66, 1, 2, 8, 16, 17, 25, 31, 39, 50, 56, 29, 5, 9, 18, 26,
		5, 7, 15, 35, 8, 17, 28, 32, 41, 31, 41, 0, 3, 22, 28, 2,
		8, 15, 39, 56, 33, 43, 45, 54, 38, 46, 48, 47, 58, 0, 0, 0,
		2, 8, 0, 3, 2, 8, 17, 25, 31, 39, 56, 29, 2, 8, 7, 9,
		18, 26, 35, 33, 43, 45, 54, 33, 43, 45, 54, 8, 17, 33, 25, 33,
		54, 33, 54, 39, 56, 39, 56, 38, 46, 38, 46, 38, 46, 48,
	};

    private static final short lapg_sym_to[] = {
		67, 5, 6, 6, 25, 6, 6, 6, 6, 61, 6, 34, 12, 21, 21, 21,
		13, 14, 23, 44, 16, 16, 33, 33, 54, 36, 55, 1, 1, 29, 29, 7,
		7, 24, 50, 50, 38, 38, 38, 38, 46, 46, 46, 59, 65, 66, 2, 3,
		8, 17, 4, 11, 9, 18, 26, 30, 37, 51, 63, 35, 10, 19, 15, 22,
		28, 32, 45, 39, 56, 57, 39, 40, 40, 40, 40, 20, 27, 41, 31, 42,
		62, 43, 43, 52, 64, 53, 53, 47, 58, 48, 48, 49, 49, 60,
	};

    private static final short lapg_rlen[] = {
		1, 0, 3, 2, 1, 3, 3, 1, 1, 2, 1, 3, 1, 0, 3, 1,
		0, 1, 0, 6, 2, 1, 2, 1, 5, 4, 3, 1, 2, 1, 1, 0,
		3, 2, 3, 2, 2, 1, 0, 3, 2, 1, 3,
	};

    private static final short lapg_rlex[] = {
		20, 20, 19, 21, 21, 24, 24, 25, 26, 22, 22, 27, 29, 29, 27, 30,
		30, 31, 31, 27, 28, 28, 23, 23, 33, 33, 34, 34, 35, 35, 38, 38,
		36, 36, 37, 37, 39, 40, 40, 32, 41, 41, 42,
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
		"optionsopt",
		"options",
		"lexer_parts",
		"grammar_parts",
		"option",
		"symbol",
		"pattern",
		"lexer_part",
		"icon_list",
		"typeopt",
		"iconopt",
		"commandopt",
		"command",
		"grammar_part",
		"rule_list",
		"symbol_list",
		"rule_right",
		"rule_symbols",
		"rule_priorityopt",
		"rule_priority",
		"command_tokensopt",
		"command_tokens",
		"command_token",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 19;
		public static final int optionsopt = 20;
		public static final int options = 21;
		public static final int lexer_parts = 22;
		public static final int grammar_parts = 23;
		public static final int option = 24;
		public static final int symbol = 25;
		public static final int pattern = 26;
		public static final int lexer_part = 27;
		public static final int icon_list = 28;
		public static final int typeopt = 29;
		public static final int iconopt = 30;
		public static final int commandopt = 31;
		public static final int command = 32;
		public static final int grammar_part = 33;
		public static final int rule_list = 34;
		public static final int symbol_list = 35;
		public static final int rule_right = 36;
		public static final int rule_symbols = 37;
		public static final int rule_priorityopt = 38;
		public static final int rule_priority = 39;
		public static final int command_tokensopt = 40;
		public static final int command_tokens = 41;
		public static final int command_token = 42;
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

	private boolean parse(LapgLexer lexer) throws IOException {

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
			error(lapg_n.pos, lapg_n.endpos, MessageFormat.format( "syntax error before line {0}", lapg_n.pos.line ) );
			return false;
		};
		return true;
	}

	private void shift(LapgLexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
		if( DEBUG_SYNTAX ) {
			System.out.println( MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current() ) );
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
		lapg_gg.pos = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]].pos:lapg_n.pos;
		lapg_gg.endpos = (lapg_rlen[rule]!=0)?lapg_m[lapg_head].endpos:lapg_n.pos;
		switch( rule ) {
			case 2:  // input ::= optionsopt lexer_parts grammar_parts
				  lapg_gg.sym = result = new AstRoot(((List<AstOption>)lapg_m[lapg_head-2].sym), ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 3:  // options ::= options option
				 ((List<AstOption>)lapg_m[lapg_head-1].sym).add(((AstOption)lapg_m[lapg_head-0].sym)); 
				break;
			case 4:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOption>(16); ((List<AstOption>)lapg_gg.sym).add(((AstOption)lapg_m[lapg_head-0].sym)); 
				break;
			case 5:  // option ::= '.' identifier scon
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 6:  // option ::= '.' identifier icon
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-1].sym), ((Integer)lapg_m[lapg_head-0].sym).toString(), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 7:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 8:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 9:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 10:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 11:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 14:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 19:  // lexer_part ::= symbol typeopt ':' pattern iconopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((AstRegexp)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
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
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRuleRight>)lapg_m[lapg_head-1].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 25:  // grammar_part ::= '%' identifier symbol_list ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstIdentifier>)lapg_m[lapg_head-1].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
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
				 lapg_gg.sym = new AstRuleRight(((List<AstRightSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 33:  // rule_right ::= commandopt rule_priorityopt
				 lapg_gg.sym = new AstRuleRight(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_gg.pos.offset, lapg_gg.endpos.offset); 
				break;
			case 34:  // rule_symbols ::= rule_symbols commandopt symbol
				 ((List<AstRightSymbol>)lapg_m[lapg_head-2].sym).add(new AstRightSymbol(((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].pos.offset, lapg_m[lapg_head-0].endpos.offset)); 
				break;
			case 35:  // rule_symbols ::= commandopt symbol
				 lapg_gg.sym = new ArrayList<AstRightSymbol>(); ((List<AstRightSymbol>)lapg_gg.sym).add(new AstRightSymbol(((AstCode)lapg_m[lapg_head-1].sym), ((AstIdentifier)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-1].pos.offset, lapg_m[lapg_head-0].endpos.offset)); 
				break;
			case 36:  // rule_priority ::= '<<' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 39:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].pos.offset+1, lapg_m[lapg_head-0].endpos.offset-1); 
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
