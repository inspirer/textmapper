package net.sf.lapg.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ArrayList;
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
		-3, 87, -1, -13, 3, 6, -1, -1, 4, 7, -1, -23, -1, 28, 8, 72,
		73, -1, 74, 5, 77, 40, -1, -1, 33, -1, -1, -1, -29, 29, -37, 42,
		-1, 30, -49, -1, 78, -1, 41, 31, 17, -1, 19, 20, 15, 16, -57, 13,
		14, 18, 21, 23, 22, -1, 12, -85, -1, -97, -1, 65, 69, -1, -1, -105,
		43, 44, -111, -1, -1, 75, -1, 76, 9, -117, -1, 10, 11, 27, -145, 48,
		-1, -1, -1, 64, -159, -1, -1, 70, -1, 79, 26, 36, -175, 47, 49, 68,
		66, -1, 50, 53, 58, -187, -199, -1, -215, 38, 39, -221, 45, -237, 59, -251,
		-1, 71, -261, -267, 85, -1, 51, 60, 61, -273, -283, -1, -301, 55, 57, 46,
		-1, 84, 83, 56, -1, 80, 63, 86, -317, 62, -1, -2,
	};

	private static final short lapg_lalr[] = {
		1, -1, 13, -1, 2, 0, 16, 0, -1, -2, 1, -1, 13, -1, 2, 1,
		16, 1, -1, -2, 18, -1, 15, 32, -1, -2, 18, -1, 9, 32, 15, 32,
		-1, -2, 1, -1, 2, -1, 6, -1, 16, -1, 0, 2, -1, -2, 15, -1,
		14, 8, 17, 8, -1, -2, 2, -1, 13, -1, 14, -1, 16, -1, 17, -1,
		18, -1, 21, -1, 22, -1, 23, -1, 24, -1, 25, -1, 26, -1, 19, 24,
		-1, -2, 3, -1, 1, 34, 2, 34, 6, 34, 16, 34, -1, -2, 15, -1,
		14, 67, 17, 67, -1, -2, 18, -1, 9, 32, -1, -2, 18, -1, 9, 32,
		-1, -2, 2, -1, 13, -1, 14, -1, 16, -1, 17, -1, 18, -1, 21, -1,
		22, -1, 23, -1, 24, -1, 25, -1, 26, -1, 19, 25, -1, -2, 5, -1,
		1, 35, 2, 35, 6, 35, 16, 35, 27, 35, -1, -2, 1, -1, 16, -1,
		2, 52, 10, 52, 12, 52, 20, 52, 27, 52, -1, -2, 27, -1, 1, 37,
		2, 37, 6, 37, 16, 37, -1, -2, 27, -1, 2, 37, 10, 37, 12, 37,
		20, 37, -1, -2, 1, -1, 16, -1, 2, 52, 10, 52, 12, 52, 20, 52,
		27, 52, -1, -2, 28, -1, 29, 81, -1, -2, 1, -1, 16, -1, 2, 52,
		10, 52, 12, 52, 20, 52, 27, 52, -1, -2, 1, -1, 27, -1, 2, 37,
		10, 37, 12, 37, 20, 37, -1, -2, 2, -1, 20, -1, 10, 54, 12, 54,
		-1, -2, 28, -1, 29, 81, -1, -2, 28, -1, 29, 82, -1, -2, 2, -1,
		20, -1, 10, 54, 12, 54, -1, -2, 11, -1, 1, 8, 2, 8, 10, 8,
		12, 8, 16, 8, 20, 8, 27, 8, -1, -2, 16, -1, 1, 52, 2, 52,
		10, 52, 12, 52, 20, 52, 27, 52, -1, -2, 16, -1, 1, 52, 2, 52,
		10, 52, 12, 52, 20, 52, 27, 52, -1, -2,
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 18, 44, 45, 52, 62, 64, 64, 64, 67, 69, 70, 73, 79, 86,
		91, 109, 117, 125, 128, 130, 134, 138, 142, 146, 150, 154, 157, 160, 162, 163,
		164, 166, 170, 182, 186, 188, 192, 193, 194, 196, 198, 199, 201, 202, 204, 207,
		208, 210, 217, 219, 222, 223, 229, 230, 232, 235, 237, 240, 257, 258, 259, 263,
		264, 267, 272, 274, 276,
	};

	private static final short lapg_sym_from[] = {
		138, 0, 3, 7, 12, 17, 27, 30, 62, 67, 70, 81, 82, 84, 102, 103,
		107, 109, 2, 6, 7, 12, 17, 23, 26, 27, 30, 32, 46, 53, 56, 62,
		67, 68, 70, 73, 80, 81, 82, 103, 111, 121, 123, 132, 55, 7, 17, 23,
		67, 70, 81, 103, 7, 10, 17, 22, 27, 67, 70, 78, 81, 103, 12, 30,
		61, 85, 86, 97, 112, 122, 80, 97, 112, 0, 3, 23, 46, 53, 73, 23,
		35, 37, 46, 53, 58, 73, 25, 34, 57, 61, 88, 6, 7, 12, 17, 23,
		30, 46, 53, 67, 70, 73, 81, 84, 102, 103, 107, 124, 136, 22, 23, 35,
		37, 46, 53, 58, 73, 11, 23, 28, 46, 53, 63, 66, 73, 41, 53, 74,
		111, 121, 23, 46, 53, 73, 23, 46, 53, 73, 23, 46, 53, 73, 23, 46,
		53, 73, 23, 46, 53, 73, 23, 46, 53, 73, 92, 101, 109, 104, 114, 115,
		117, 128, 0, 0, 0, 3, 6, 12, 30, 32, 7, 17, 56, 67, 70, 80,
		81, 103, 111, 121, 123, 132, 11, 28, 63, 66, 23, 46, 23, 46, 53, 73,
		55, 6, 6, 12, 10, 27, 12, 12, 30, 56, 84, 102, 84, 102, 107, 101,
		101, 109, 12, 30, 84, 102, 107, 124, 136, 27, 62, 27, 62, 82, 17, 7,
		17, 67, 70, 81, 103, 17, 111, 121, 92, 101, 109, 104, 114, 104, 114, 115,
		0, 3, 7, 12, 17, 27, 30, 62, 67, 70, 81, 82, 84, 102, 103, 107,
		109, 0, 46, 11, 28, 63, 66, 78, 92, 101, 109, 84, 102, 107, 124, 136,
		111, 121, 104, 114,
	};

	private static final short lapg_sym_to[] = {
		139, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 7, 9, 14, 9, 34, 40, 56, 57, 9, 9, 40, 40, 14, 57,
		14, 88, 14, 40, 14, 14, 57, 14, 122, 122, 14, 14, 77, 15, 15, 41,
		15, 15, 15, 15, 16, 21, 16, 38, 21, 16, 16, 91, 16, 16, 26, 26,
		84, 84, 102, 107, 107, 132, 93, 108, 127, 2, 2, 42, 42, 42, 42, 43,
		68, 70, 43, 43, 82, 43, 55, 67, 81, 55, 103, 10, 17, 27, 17, 44,
		62, 44, 44, 17, 17, 44, 17, 62, 62, 17, 62, 62, 62, 39, 45, 69,
		71, 45, 45, 83, 45, 23, 46, 23, 46, 46, 23, 23, 46, 72, 75, 90,
		123, 123, 47, 47, 47, 47, 48, 48, 48, 48, 49, 49, 49, 49, 50, 50,
		50, 50, 51, 51, 51, 51, 52, 52, 52, 52, 104, 104, 104, 114, 114, 114,
		130, 135, 138, 3, 4, 8, 11, 28, 63, 66, 18, 18, 79, 18, 18, 94,
		18, 18, 124, 124, 133, 136, 24, 24, 24, 24, 53, 73, 54, 54, 76, 76,
		78, 12, 13, 29, 22, 22, 30, 31, 64, 80, 97, 112, 98, 98, 118, 109,
		110, 119, 32, 32, 99, 99, 99, 99, 99, 58, 58, 59, 59, 96, 35, 19,
		36, 87, 89, 95, 113, 37, 125, 125, 105, 105, 105, 115, 115, 116, 116, 129,
		5, 5, 20, 33, 20, 60, 65, 60, 20, 20, 20, 60, 100, 100, 20, 100,
		120, 6, 74, 25, 61, 85, 86, 92, 106, 111, 121, 101, 101, 101, 134, 137,
		126, 131, 117, 128,
	};

	private static final short lapg_rlen[] = {
		0, 1, 3, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3,
		0, 1, 3, 0, 1, 0, 1, 6, 1, 2, 1, 2, 2, 5, 6, 4,
		1, 2, 1, 3, 0, 1, 0, 1, 4, 3, 1, 1, 2, 2, 5, 3,
		3, 1, 3, 1, 3, 1, 3, 5, 1, 1, 1, 3, 3, 1, 1, 3,
		2, 0, 1, 3, 2, 1, 3, 1,
	};

	private static final short lapg_rlex[] = {
		60, 60, 30, 31, 31, 32, 32, 33, 34, 35, 35, 36, 36, 37, 37, 37,
		37, 37, 37, 37, 37, 37, 37, 37, 61, 61, 37, 38, 39, 39, 39, 40,
		62, 62, 40, 63, 63, 64, 64, 40, 41, 41, 42, 42, 42, 43, 43, 43,
		44, 44, 45, 45, 65, 65, 66, 66, 46, 46, 46, 47, 47, 47, 48, 48,
		49, 50, 50, 51, 51, 51, 52, 52, 53, 53, 53, 53, 53, 53, 54, 54,
		55, 67, 67, 56, 57, 57, 58, 59,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"error",
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
		"annotations_decl",
		"annotations",
		"annotation",
		"map_entries",
		"expression",
		"expression_list",
		"rule_priority",
		"command",
		"command_tokens",
		"command_token",
		"syntax_problem",
		"optionsopt",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"commandopt",
		"annotations_declopt",
		"rule_priorityopt",
		"command_tokensopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 30;
		public static final int options = 31;
		public static final int option = 32;
		public static final int symbol = 33;
		public static final int reference = 34;
		public static final int type = 35;
		public static final int type_part_list = 36;
		public static final int type_part = 37;
		public static final int pattern = 38;
		public static final int lexer_parts = 39;
		public static final int lexer_part = 40;
		public static final int icon_list = 41;
		public static final int grammar_parts = 42;
		public static final int grammar_part = 43;
		public static final int references = 44;
		public static final int rules = 45;
		public static final int rule0 = 46;
		public static final int rulesyms = 47;
		public static final int rulesym = 48;
		public static final int annotations_decl = 49;
		public static final int annotations = 50;
		public static final int annotation = 51;
		public static final int map_entries = 52;
		public static final int expression = 53;
		public static final int expression_list = 54;
		public static final int rule_priority = 55;
		public static final int command = 56;
		public static final int command_tokens = 57;
		public static final int command_token = 58;
		public static final int syntax_problem = 59;
		public static final int optionsopt = 60;
		public static final int type_part_listopt = 61;
		public static final int typeopt = 62;
		public static final int iconopt = 63;
		public static final int commandopt = 64;
		public static final int annotations_declopt = 65;
		public static final int rule_priorityopt = 66;
		public static final int command_tokensopt = 67;
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
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while( lapg_m[lapg_head].state != 139 ) {
			int lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

			if( lapg_i >= 0 ) {
				reduce(lapg_i);
			} else if( lapg_i == -1 ) {
				shift(lexer);
				lapg_symbols_ok++;
			}

			if( lapg_i == -2 || lapg_m[lapg_head].state == -1 ) {
				if (lapg_n.lexem == 0) {
					break;
				}
				while (lapg_head >= 0 && lapg_state_sym( lapg_m[lapg_head].state, 1 ) == -1) {
					lapg_m[lapg_head] = null; // TODO dispose?
					lapg_head--;
				}
				if (lapg_head >= 0) {
					lapg_m[++lapg_head] = new LapgSymbol();
					lapg_m[lapg_head].lexem = 1;
					lapg_m[lapg_head].sym = null;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, 1 );
					lapg_m[lapg_head].line = lapg_n.line;
					lapg_m[lapg_head].offset = lapg_n.offset;
					lapg_m[lapg_head].endoffset = lapg_n.endoffset;
					if( lapg_symbols_ok >= 4 ) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
					}
					if( lapg_symbols_ok <= 1 ) {
						lapg_n = lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				} else {
					lapg_head = 0;
					lapg_m[0] = new LapgSymbol();
					lapg_m[0].state = 0;
				}
				break;
			}
		}

		if( lapg_m[lapg_head].state != 139 ) {
			if(lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			}
			throw new ParseException();
		}
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
				  lapg_gg.sym = new AstRoot(((List<AstOptionPart>)lapg_m[lapg_head-2].sym), ((List<AstLexerPart>)lapg_m[lapg_head-1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOptionPart>(16); ((List<AstOptionPart>)lapg_gg.sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 4:  // options ::= options option
				 ((List<AstOptionPart>)lapg_m[lapg_head-1].sym).add(((AstOptionPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 5:  // option ::= '.' identifier expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head-1].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 10:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 27:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 28:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 29:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstLexerPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 30:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<AstLexerPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 31:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 34:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 39:  // lexer_part ::= symbol typeopt ':' pattern iconopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((AstRegexp)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((AstCode)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 40:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 41:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head-1].sym).add(((Integer)lapg_m[lapg_head-0].sym)); 
				break;
			case 42:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 43:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstGrammarPart)lapg_m[lapg_head-0].sym)); 
				break;
			case 44:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head-1].sym).add(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 45:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // grammar_part ::= annotations_decl symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((List<AstRule>)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // grammar_part ::= '%' identifier references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head-2].sym), ((List<AstReference>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 49:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head-1].sym).add(((AstReference)lapg_m[lapg_head-0].sym)); 
				break;
			case 50:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 51:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head-2].sym).add(((AstRule)lapg_m[lapg_head-0].sym)); 
				break;
			case 56:  // rule0 ::= annotations_declopt rulesyms commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(((List<AstRuleSymbol>)lapg_m[lapg_head-2].sym), ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 57:  // rule0 ::= annotations_declopt commandopt rule_priorityopt
				 lapg_gg.sym = new AstRule(null, ((AstCode)lapg_m[lapg_head-1].sym), ((AstReference)lapg_m[lapg_head-0].sym), ((AstAnnotations)lapg_m[lapg_head-2].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 58:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 59:  // rulesyms ::= rulesym
				 lapg_gg.sym = new ArrayList<AstRuleSymbol>(); ((List<AstRuleSymbol>)lapg_gg.sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 60:  // rulesyms ::= rulesyms rulesym
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(((AstRuleSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 61:  // rulesyms ::= rulesyms syntax_problem
				 ((List<AstRuleSymbol>)lapg_m[lapg_head-1].sym).add(new AstRuleSymbol(((AstError)lapg_m[lapg_head-0].sym))); 
				break;
			case 62:  // rulesym ::= commandopt identifier '=' reference annotations_declopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym), ((AstReference)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 63:  // rulesym ::= commandopt reference annotations_declopt
				 lapg_gg.sym = new AstRuleSymbol(((AstCode)lapg_m[lapg_head-2].sym), null, ((AstReference)lapg_m[lapg_head-1].sym), ((AstAnnotations)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 64:  // annotations_decl ::= '[' annotations ']'
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 65:  // annotations ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 66:  // annotations ::= annotations ',' annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head-0].sym)); 
				break;
			case 67:  // annotation ::= identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-0].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 68:  // annotation ::= identifier ':' expression
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 69:  // annotation ::= syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 70:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 71:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 72:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 73:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // expression ::= '[' map_entries ']'
				 lapg_gg.sym = new AstMap(((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // expression ::= '[' expression_list ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 78:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 79:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 80:  // rule_priority ::= '<<' reference
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head-0].sym); 
				break;
			case 83:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 87:  // syntax_problem ::= error
				 lapg_gg.sym = new AstError(source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset); 
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
