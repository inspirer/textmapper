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
		-3, 91, -1, -13, 3, 6, -1, -1, 4, 7, -1, -23, -1, 28, 8, 74,
		75, -1, 76, 77, 78, 5, 81, 40, -1, -1, 33, -1, -1, -1, -29, 29,
		-37, 42, -1, 30, -49, -1, 82, -1, 41, 31, 17, -1, 19, 20, 15, 16,
		-57, 13, 14, 18, 21, 23, 22, -1, 12, -85, -1, -97, -1, 65, 71, -1,
		-1, -109, 43, 44, -115, -1, -1, 79, -1, 80, 9, -121, -1, 10, 11, 27,
		-149, 48, -1, -1, -1, -1, -1, 64, -163, -1, -1, 72, -1, 83, 26, 36,
		-179, 47, 49, 69, 68, -1, 66, -1, 50, 53, 58, -191, -203, -1, -219, 38,
		39, 70, -225, 45, -241, 59, -255, -1, 73, -265, -271, 89, -1, 51, 60, 61,
		-277, -287, -1, -305, 55, 57, 46, -1, 88, 87, 56, -1, 84, 63, 90, -321,
		62, -1, -2,
	};

	private static final short lapg_lalr[] = {
		1, -1, 13, -1, 2, 0, 16, 0, -1, -2, 1, -1, 13, -1, 2, 1,
		16, 1, -1, -2, 18, -1, 15, 32, -1, -2, 18, -1, 9, 32, 15, 32,
		-1, -2, 1, -1, 2, -1, 6, -1, 16, -1, 0, 2, -1, -2, 15, -1,
		14, 8, 17, 8, -1, -2, 2, -1, 13, -1, 14, -1, 16, -1, 17, -1,
		18, -1, 21, -1, 22, -1, 23, -1, 24, -1, 25, -1, 26, -1, 19, 24,
		-1, -2, 3, -1, 1, 34, 2, 34, 6, 34, 16, 34, -1, -2, 11, -1,
		15, -1, 18, -1, 14, 67, 17, 67, -1, -2, 18, -1, 9, 32, -1, -2,
		18, -1, 9, 32, -1, -2, 2, -1, 13, -1, 14, -1, 16, -1, 17, -1,
		18, -1, 21, -1, 22, -1, 23, -1, 24, -1, 25, -1, 26, -1, 19, 25,
		-1, -2, 5, -1, 1, 35, 2, 35, 6, 35, 16, 35, 29, 35, -1, -2,
		1, -1, 16, -1, 2, 52, 10, 52, 12, 52, 20, 52, 29, 52, -1, -2,
		29, -1, 1, 37, 2, 37, 6, 37, 16, 37, -1, -2, 29, -1, 2, 37,
		10, 37, 12, 37, 20, 37, -1, -2, 1, -1, 16, -1, 2, 52, 10, 52,
		12, 52, 20, 52, 29, 52, -1, -2, 30, -1, 31, 85, -1, -2, 1, -1,
		16, -1, 2, 52, 10, 52, 12, 52, 20, 52, 29, 52, -1, -2, 1, -1,
		29, -1, 2, 37, 10, 37, 12, 37, 20, 37, -1, -2, 2, -1, 20, -1,
		10, 54, 12, 54, -1, -2, 30, -1, 31, 85, -1, -2, 30, -1, 31, 86,
		-1, -2, 2, -1, 20, -1, 10, 54, 12, 54, -1, -2, 11, -1, 1, 8,
		2, 8, 10, 8, 12, 8, 16, 8, 20, 8, 29, 8, -1, -2, 16, -1,
		1, 52, 2, 52, 10, 52, 12, 52, 20, 52, 29, 52, -1, -2, 16, -1,
		1, 52, 2, 52, 10, 52, 12, 52, 20, 52, 29, 52, -1, -2,
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 20, 48, 49, 58, 70, 72, 72, 72, 75, 77, 79, 82, 88, 95,
		100, 120, 128, 137, 141, 143, 147, 151, 155, 159, 163, 167, 175, 183, 186, 189,
		191, 192, 193, 195, 199, 213, 217, 219, 223, 224, 225, 227, 229, 230, 232, 233,
		235, 238, 239, 241, 248, 250, 253, 254, 262, 263, 265, 268, 270, 273, 292, 293,
		294, 298, 299, 302, 307, 309, 311,
	};

	private static final short lapg_sym_from[] = {
		145, 0, 3, 7, 12, 17, 29, 32, 64, 69, 72, 83, 84, 85, 86, 88,
		108, 109, 114, 116, 2, 6, 7, 12, 17, 25, 28, 29, 32, 34, 48, 55,
		58, 64, 69, 70, 72, 75, 82, 83, 84, 85, 86, 109, 118, 128, 130, 139,
		57, 7, 17, 25, 69, 72, 83, 84, 85, 109, 7, 10, 17, 24, 29, 69,
		72, 80, 83, 84, 85, 109, 12, 32, 63, 89, 90, 103, 119, 59, 129, 82,
		103, 119, 0, 3, 25, 48, 55, 75, 25, 37, 39, 48, 55, 60, 75, 27,
		36, 59, 63, 92, 6, 7, 12, 17, 25, 32, 48, 55, 69, 72, 75, 83,
		84, 85, 88, 108, 109, 114, 131, 143, 24, 25, 37, 39, 48, 55, 60, 75,
		11, 25, 30, 48, 55, 59, 65, 68, 75, 43, 55, 76, 101, 118, 128, 25,
		48, 55, 75, 25, 48, 55, 75, 25, 48, 55, 75, 25, 48, 55, 75, 25,
		48, 55, 75, 25, 48, 55, 75, 7, 17, 69, 72, 83, 84, 85, 109, 7,
		17, 69, 72, 83, 84, 85, 109, 96, 107, 116, 110, 121, 122, 124, 135, 0,
		0, 0, 3, 6, 12, 32, 34, 7, 17, 58, 69, 72, 82, 83, 84, 85,
		109, 118, 128, 130, 139, 11, 30, 65, 68, 25, 48, 25, 48, 55, 75, 57,
		6, 6, 12, 10, 29, 12, 12, 32, 58, 88, 108, 88, 108, 114, 107, 107,
		116, 12, 32, 88, 108, 114, 131, 143, 29, 64, 29, 64, 86, 17, 7, 17,
		69, 72, 83, 84, 85, 109, 17, 118, 128, 96, 107, 116, 110, 121, 110, 121,
		122, 0, 3, 7, 12, 17, 29, 32, 64, 69, 72, 83, 84, 85, 86, 88,
		108, 109, 114, 116, 0, 48, 11, 30, 65, 68, 80, 96, 107, 116, 88, 108,
		114, 131, 143, 118, 128, 110, 121,
	};

	private static final short lapg_sym_to[] = {
		146, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 7, 9, 14, 9, 36, 42, 58, 59, 9, 9, 42, 42,
		14, 59, 14, 92, 14, 42, 14, 14, 14, 14, 59, 14, 129, 129, 14, 14,
		79, 15, 15, 43, 15, 15, 15, 15, 15, 15, 16, 23, 16, 40, 23, 16,
		16, 95, 16, 16, 16, 16, 28, 28, 88, 88, 108, 114, 114, 83, 139, 97,
		115, 134, 2, 2, 44, 44, 44, 44, 45, 70, 72, 45, 45, 86, 45, 57,
		69, 84, 57, 109, 10, 17, 29, 17, 46, 64, 46, 46, 17, 17, 46, 17,
		17, 17, 64, 64, 17, 64, 64, 64, 41, 47, 71, 73, 47, 47, 87, 47,
		25, 48, 25, 48, 48, 85, 25, 25, 48, 74, 77, 94, 113, 130, 130, 49,
		49, 49, 49, 50, 50, 50, 50, 51, 51, 51, 51, 52, 52, 52, 52, 53,
		53, 53, 53, 54, 54, 54, 54, 18, 18, 18, 18, 18, 18, 18, 18, 19,
		19, 19, 19, 19, 19, 19, 19, 110, 110, 110, 121, 121, 121, 137, 142, 145,
		3, 4, 8, 11, 30, 65, 68, 20, 20, 81, 20, 20, 98, 20, 20, 20,
		20, 131, 131, 140, 143, 26, 26, 26, 26, 55, 75, 56, 56, 78, 78, 80,
		12, 13, 31, 24, 24, 32, 33, 66, 82, 103, 119, 104, 104, 125, 116, 117,
		126, 34, 34, 105, 105, 105, 105, 105, 60, 60, 61, 61, 102, 37, 21, 38,
		91, 93, 99, 100, 101, 120, 39, 132, 132, 111, 111, 111, 122, 122, 123, 123,
		136, 5, 5, 22, 35, 22, 62, 67, 62, 22, 22, 22, 22, 22, 62, 106,
		106, 22, 106, 127, 6, 76, 27, 63, 89, 90, 96, 112, 118, 128, 107, 107,
		107, 141, 144, 133, 138, 124, 135,
	};

	private static final short lapg_rlen[] = {
		0, 1, 3, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2, 3,
		0, 1, 3, 0, 1, 0, 1, 6, 1, 2, 1, 2, 2, 5, 6, 4,
		1, 2, 1, 3, 0, 1, 0, 1, 4, 3, 1, 1, 2, 2, 5, 3,
		3, 1, 3, 1, 3, 3, 4, 1, 3, 5, 1, 1, 1, 1, 1, 3,
		3, 1, 1, 3, 2, 0, 1, 3, 2, 1, 3, 1,
	};

	private static final short lapg_rlex[] = {
		62, 62, 32, 33, 33, 34, 34, 35, 36, 37, 37, 38, 38, 39, 39, 39,
		39, 39, 39, 39, 39, 39, 39, 39, 63, 63, 39, 40, 41, 41, 41, 42,
		64, 64, 42, 65, 65, 66, 66, 42, 43, 43, 44, 44, 44, 45, 45, 45,
		46, 46, 47, 47, 67, 67, 68, 68, 48, 48, 48, 49, 49, 49, 50, 50,
		51, 52, 52, 53, 53, 53, 53, 53, 54, 54, 55, 55, 55, 55, 55, 55,
		55, 55, 56, 56, 57, 69, 69, 58, 59, 59, 60, 61,
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
		"Ltrue",
		"Lfalse",
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
		public static final int input = 32;
		public static final int options = 33;
		public static final int option = 34;
		public static final int symbol = 35;
		public static final int reference = 36;
		public static final int type = 37;
		public static final int type_part_list = 38;
		public static final int type_part = 39;
		public static final int pattern = 40;
		public static final int lexer_parts = 41;
		public static final int lexer_part = 42;
		public static final int icon_list = 43;
		public static final int grammar_parts = 44;
		public static final int grammar_part = 45;
		public static final int references = 46;
		public static final int rules = 47;
		public static final int rule0 = 48;
		public static final int rulesyms = 49;
		public static final int rulesym = 50;
		public static final int annotations_decl = 51;
		public static final int annotations = 52;
		public static final int annotation = 53;
		public static final int map_entries = 54;
		public static final int expression = 55;
		public static final int expression_list = 56;
		public static final int rule_priority = 57;
		public static final int command = 58;
		public static final int command_tokens = 59;
		public static final int command_token = 60;
		public static final int syntax_problem = 61;
		public static final int optionsopt = 62;
		public static final int type_part_listopt = 63;
		public static final int typeopt = 64;
		public static final int iconopt = 65;
		public static final int commandopt = 66;
		public static final int annotations_declopt = 67;
		public static final int rule_priorityopt = 68;
		public static final int command_tokensopt = 69;
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

	public AstRoot parse(LapgLexer lexer) throws IOException, ParseException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next();

		while (lapg_m[lapg_head].state != 146) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state, lapg_n.lexem);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift(lexer);
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				if (lapg_n.lexem == 0) {
					break;
				}
				while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 1) == -1) {
					lapg_m[lapg_head] = null; // TODO dispose?
					lapg_head--;
				}
				if (lapg_head >= 0) {
					lapg_m[++lapg_head] = new LapgSymbol();
					lapg_m[lapg_head].lexem = 1;
					lapg_m[lapg_head].sym = null;
					lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 1);
					lapg_m[lapg_head].line = lapg_n.line;
					lapg_m[lapg_head].offset = lapg_n.offset;
					lapg_m[lapg_head].endoffset = lapg_n.endoffset;
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
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

		if (lapg_m[lapg_head].state != 146) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lexer.getTokenLine(), MessageFormat.format("syntax error before line {0}", lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return (AstRoot)lapg_m[lapg_head - 1].sym;
	}

	private void shift(LapgLexer lexer) throws IOException {
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
			case 69:  // annotation ::= identifier '=' expression
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 70:  // annotation ::= identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head-3].sym), ((AstExpression)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 71:  // annotation ::= syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head-0].sym)); 
				break;
			case 72:  // map_entries ::= identifier ':' expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 73:  // map_entries ::= map_entries ',' identifier ':' expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head-2].sym), ((AstExpression)lapg_m[lapg_head-0].sym), source, lapg_m[lapg_head-2].offset, lapg_m[lapg_head-0].endoffset)); 
				break;
			case 74:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head-0].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 79:  // expression ::= '[' map_entries ']'
				 lapg_gg.sym = new AstMap(((List<AstNamedEntry>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 80:  // expression ::= '[' expression_list ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head-1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 83:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head-0].sym)); 
				break;
			case 84:  // rule_priority ::= '<<' reference
				 lapg_gg.sym = ((AstReference)lapg_m[lapg_head-0].sym); 
				break;
			case 87:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head-2].offset+1, lapg_m[lapg_head-0].endoffset-1); 
				break;
			case 91:  // syntax_problem ::= error
				 lapg_gg.sym = new AstError(source, lapg_m[lapg_head-0].offset, lapg_m[lapg_head-0].endoffset); 
				break;
		}
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
