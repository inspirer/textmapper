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
		-3, -1, -11, 3, -1, -1, 4, 6, -1, -19, -1, 27, 7, 62, 63, -1,
		64, 5, 38, -1, -1, 31, -1, -1, -1, -25, 28, -33, 40, -1, -43, -1,
		67, -1, 39, 29, 16, -1, 18, 19, 14, 15, -51, 12, 13, 17, 20, 22,
		21, -1, 11, -79, -1, -1, -1, -1, -1, -89, 41, -95, -1, -1, 65, -1,
		66, 8, -101, -1, 9, 10, 26, -129, 45, -1, 59, -141, -1, -1, 60, -1,
		68, 25, 34, -155, 44, 46, -1, 47, 50, -165, -177, -1, -191, 36, 37, -197,
		42, -211, 55, -223, -1, 61, -233, -239, 74, -1, 48, 56, -245, -255, -1, -271,
		52, 54, 43, -1, 73, 72, 53, -1, 69, 58, 75, -285, 57, -1, -2,
	};

    private static final short lapg_lalr[] = {
		12, -1, 1, 0, 15, 0, -1, -2, 12, -1, 1, 1, 15, 1, -1, -2,
		17, -1, 14, 30, -1, -2, 17, -1, 8, 30, 14, 30, -1, -2, 1, -1,
		5, -1, 15, -1, 0, 2, -1, -2, 14, -1, 13, 7, 16, 7, -1, -2,
		1, -1, 12, -1, 13, -1, 15, -1, 16, -1, 17, -1, 20, -1, 21, -1,
		22, -1, 23, -1, 24, -1, 25, -1, 18, 23, -1, -2, 2, -1, 1, 32,
		5, 32, 15, 32, -1, -2, 17, -1, 8, 30, -1, -2, 17, -1, 8, 30,
		-1, -2, 1, -1, 12, -1, 13, -1, 15, -1, 16, -1, 17, -1, 20, -1,
		21, -1, 22, -1, 23, -1, 24, -1, 25, -1, 18, 24, -1, -2, 4, -1,
		1, 33, 5, 33, 15, 33, 26, 33, -1, -2, 15, -1, 1, 49, 9, 49,
		11, 49, 19, 49, 26, 49, -1, -2, 26, -1, 1, 35, 5, 35, 15, 35,
		-1, -2, 26, -1, 1, 35, 9, 35, 11, 35, 19, 35, -1, -2, 15, -1,
		1, 49, 9, 49, 11, 49, 19, 49, 26, 49, -1, -2, 27, -1, 28, 70,
		-1, -2, 15, -1, 1, 49, 9, 49, 11, 49, 19, 49, 26, 49, -1, -2,
		26, -1, 1, 35, 9, 35, 11, 35, 19, 35, -1, -2, 1, -1, 19, -1,
		9, 51, 11, 51, -1, -2, 27, -1, 28, 70, -1, -2, 27, -1, 28, 71,
		-1, -2, 1, -1, 19, -1, 9, 51, 11, 51, -1, -2, 10, -1, 1, 7,
		9, 7, 11, 7, 15, 7, 19, 7, 26, 7, -1, -2, 15, -1, 1, 49,
		9, 49, 11, 49, 19, 49, 26, 49, -1, -2, 15, -1, 1, 49, 9, 49,
		11, 49, 19, 49, 26, 49, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 25, 26, 32, 41, 43, 43, 43, 46, 48, 49, 52, 58, 65, 70,
		87, 95, 103, 106, 108, 112, 116, 120, 124, 128, 132, 135, 138, 140, 141, 142,
		144, 148, 159, 163, 165, 169, 170, 171, 173, 175, 176, 178, 179, 181, 184, 185,
		187, 194, 197, 202, 203, 205, 208, 210, 213, 214, 215, 219, 220, 223, 228, 230,
		232,
	};

    private static final short lapg_sym_from[] = {
		125, 1, 4, 5, 10, 15, 20, 23, 24, 27, 29, 42, 49, 52, 56, 60,
		61, 63, 66, 73, 91, 99, 108, 110, 119, 51, 5, 15, 20, 60, 63, 91,
		5, 8, 15, 19, 24, 60, 63, 71, 91, 10, 27, 55, 76, 77, 86, 100,
		109, 73, 86, 100, 0, 2, 20, 42, 49, 66, 20, 31, 33, 42, 49, 54,
		66, 22, 30, 53, 55, 79, 4, 5, 10, 15, 20, 27, 42, 49, 60, 63,
		66, 75, 90, 91, 95, 111, 123, 19, 20, 31, 33, 42, 49, 54, 66, 9,
		20, 25, 42, 49, 57, 59, 66, 37, 49, 67, 99, 108, 20, 42, 49, 66,
		20, 42, 49, 66, 20, 42, 49, 66, 20, 42, 49, 66, 20, 42, 49, 66,
		20, 42, 49, 66, 83, 89, 97, 92, 102, 103, 105, 115, 0, 0, 0, 2,
		4, 10, 27, 29, 5, 15, 52, 60, 63, 73, 91, 99, 108, 110, 119, 9,
		25, 57, 59, 20, 42, 20, 42, 49, 66, 51, 4, 4, 10, 8, 24, 10,
		10, 27, 52, 75, 90, 75, 90, 95, 89, 89, 97, 10, 27, 75, 90, 95,
		111, 123, 15, 24, 56, 5, 15, 60, 63, 91, 15, 99, 108, 83, 89, 97,
		92, 102, 92, 102, 103, 0, 42, 9, 25, 57, 59, 71, 83, 89, 97, 75,
		90, 95, 111, 123, 99, 108, 92, 102,
	};

    private static final short lapg_sym_to[] = {
		126, 5, 7, 12, 7, 30, 36, 52, 53, 7, 7, 36, 36, 12, 53, 12,
		79, 12, 36, 12, 12, 109, 109, 12, 12, 70, 13, 13, 37, 13, 13, 13,
		14, 18, 14, 34, 18, 14, 14, 82, 14, 23, 23, 75, 75, 90, 95, 95,
		119, 84, 96, 114, 1, 1, 38, 38, 38, 38, 39, 61, 63, 39, 39, 61,
		39, 51, 60, 60, 51, 91, 8, 15, 24, 15, 40, 56, 40, 40, 15, 15,
		40, 56, 56, 15, 56, 56, 56, 35, 41, 62, 64, 41, 41, 74, 41, 20,
		42, 20, 42, 42, 20, 20, 42, 65, 68, 81, 110, 110, 43, 43, 43, 43,
		44, 44, 44, 44, 45, 45, 45, 45, 46, 46, 46, 46, 47, 47, 47, 47,
		48, 48, 48, 48, 92, 92, 92, 102, 102, 102, 117, 122, 125, 2, 3, 6,
		9, 25, 57, 59, 16, 16, 72, 16, 16, 85, 16, 111, 111, 120, 123, 21,
		21, 21, 21, 49, 66, 50, 50, 69, 69, 71, 10, 11, 26, 19, 19, 27,
		28, 58, 73, 86, 100, 87, 87, 106, 97, 98, 107, 29, 29, 88, 88, 88,
		88, 88, 31, 54, 54, 17, 32, 78, 80, 101, 33, 112, 112, 93, 93, 93,
		103, 103, 104, 104, 116, 4, 67, 22, 55, 76, 77, 83, 94, 99, 108, 89,
		89, 89, 121, 124, 113, 118, 105, 115,
	};

    private static final short lapg_rlen[] = {
		0, 1, 3, 1, 2, 3, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 3, 0, 1,
		3, 0, 1, 0, 1, 6, 1, 2, 1, 2, 5, 6, 4, 1, 2, 1,
		3, 0, 1, 0, 1, 4, 3, 1, 2, 5, 3, 3, 3, 5, 1, 1,
		1, 3, 3, 1, 3, 2, 0, 1, 3, 2, 1, 3,
	};

    private static final short lapg_rlex[] = {
		56, 56, 29, 30, 30, 31, 32, 33, 34, 34, 35, 35, 36, 36, 36, 36,
		36, 36, 36, 36, 36, 36, 36, 57, 57, 36, 37, 38, 38, 39, 58, 58,
		39, 59, 59, 60, 60, 39, 40, 40, 41, 41, 42, 42, 42, 43, 43, 44,
		44, 61, 61, 62, 62, 45, 45, 46, 46, 47, 47, 48, 49, 49, 50, 50,
		50, 50, 50, 51, 51, 52, 63, 63, 53, 54, 54, 55,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"regexp",
		"scon",
		"icon",
		"'%'",
		"_skip",
		"_skip_comment",
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
		"reference",
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"icon_list",
		"grammar_parts",
		"grammar_part",
		"references",
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
		public static final int input = 29;
		public static final int options = 30;
		public static final int option = 31;
		public static final int symbol = 32;
		public static final int reference = 33;
		public static final int type = 34;
		public static final int type_part_list = 35;
		public static final int type_part = 36;
		public static final int pattern = 37;
		public static final int lexer_parts = 38;
		public static final int lexer_part = 39;
		public static final int icon_list = 40;
		public static final int grammar_parts = 41;
		public static final int grammar_part = 42;
		public static final int references = 43;
		public static final int rules = 44;
		public static final int rule0 = 45;
		public static final int rulesyms = 46;
		public static final int rulesym = 47;
		public static final int annotations = 48;
		public static final int map_entries = 49;
		public static final int expression = 50;
		public static final int expression_list = 51;
		public static final int rule_priority = 52;
		public static final int command = 53;
		public static final int command_tokens = 54;
		public static final int command_token = 55;
		public static final int optionsopt = 56;
		public static final int type_part_listopt = 57;
		public static final int typeopt = 58;
		public static final int iconopt = 59;
		public static final int commandopt = 60;
		public static final int annotationsopt = 61;
		public static final int rule_priorityopt = 62;
		public static final int command_tokensopt = 63;
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
			case 7:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
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
			case 27:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 28:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
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
			case 38:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 39:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 40:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 41:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 42:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 43:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // grammar_part ::= '%' identifier references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 45:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 46:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 47:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 48:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 53:  // rule0 ::= annotationsopt rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 54:  // rule0 ::= annotationsopt commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((Map<String,Object>)lapg_m[lapg_head-2].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 56:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 57:  // rulesym ::= commandopt identifier '=' reference annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((AstReference)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // rulesym ::= commandopt reference annotationsopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-1].sym), ((Map<String,Object>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
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
			case 69:  // rule_priority ::= '<<' reference
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head-0].sym); 
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
