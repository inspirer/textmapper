package net.sf.lapg.test.oldparser;

import java.io.IOException;

import net.sf.lapg.test.oldparser.LapgLexer.LapgSymbol;
import net.sf.lapg.test.oldparser.LapgLexer.Lexems;

import java.io.CharArrayReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LapgParser implements LapgLexer.ErrorReporter {
	
	private LapgParser(String inputId, char[] data, Map<String,String> defaultOptions) {
		this.inputId = inputId;
		this.buff = data;
		
		addLexem(getSymbol(CSyntax.EOI, 1), null, null, null, null, 1);
		options.putAll(defaultOptions);
	}
	
	private static final boolean DEBUG_SYNTAX = false;
	private static final int BITS = 32;
	
	private Map<String,CSymbol> symCash = new HashMap<String,CSymbol>();
	private List<String> errors = new ArrayList<String>();
	
	private List<CSymbol> symbols = new ArrayList<CSymbol>();
	private List<CRule> rules = new ArrayList<CRule>();
	private List<CPrio> prios = new ArrayList<CPrio>();
	private List<CInputDef> inputs = new ArrayList<CInputDef>();
	private Map<String,String> options = new HashMap<String, String>();
	private List<CLexem> lexems = new ArrayList<CLexem>();
	
	private String inputId;
	private char[] buff;
	
	private int currentgroups = 1;
	
	private String rawData(int start, int end) {
		return new String(buff, start, end-start);
	}
	
	private CSymbol getSymbol(String name, int line) {
		CSymbol res = symCash.get(name);
		if( res == null ) {
			res = new CSymbol(name, inputId, line);
			symbols.add(res);
			symCash.put(name,res);
	
			if( name.endsWith(CSyntax.OPTSUFFIX) && name.length() > CSyntax.OPTSUFFIX.length() ) {
				try {
					CSymbol original = getSymbol(name.substring(0, name.length()-CSyntax.OPTSUFFIX.length()), line);
					res.setNonTerminal(null, null, 0);
					addRule(new CRule(Collections.singletonList(original), null, null, inputId, line), res);
					addRule(new CRule(null, null, null, inputId, line), res);
				} catch(ParseException ex) {
					/* should never happen */
				}
			}
		}
		return res;
	}
	
	private void addLexem(CSymbol sym, String type, String regexp, Integer lexprio, CAction command, int line) {
		try {
			sym.setTerminal(type, regexp != null, inputId, line);
			if( regexp != null ) {
				lexems.add(new CLexem(sym,regexp,command,lexprio!=null?lexprio.intValue():0,currentgroups,inputId,line));
			}
		} catch( ParseException ex ) {
			error(null, null, ex.getMessage());
		}
	}
	
	private void addNonterm(CSymbol sym, String type, int line ) {
		try {
			sym.setNonTerminal(type, inputId, line);
		} catch( ParseException ex ) {
			error(null, null, ex.getMessage());
		}
	}
	
	private void addRule( CRule rule, CSymbol left ) {
		rule.setLeft(left);
		rules.add(rule);
	}
	
	private void addGrammarDirective( String id, List<CSymbol> list, int line ) {
		if( id.equals("left") ) {
			prios.add(new CPrio(CPrio.LEFT, list,inputId,line));
		} else if( id.equals("right") ) {
			prios.add(new CPrio(CPrio.RIGHT, list,inputId,line));
		} else if( id.equals("nonassoc") ) {
			prios.add(new CPrio(CPrio.NONASSOC, list,inputId,line));
		} else if( id.equals("input") ) {
			inputs.add(new CInputDef(list,inputId,line));
		} else {
			error(null, null, "unknown directive identifier used: `"+id+"` at " + line);
		}
	}
	
	private void addRuleSymbol(List<CSymbol> list, CAction cmdopt, CSymbol symbol) {
		if( cmdopt != null ) {
			try {
				CSymbol sym = new CSymbol("{}", inputId, 0);
				sym.setNonTerminal(null, inputId, cmdopt.getLine());
				symbols.add(sym);
				addRule(new CRule(null, cmdopt, null, inputId, cmdopt.getLine()), sym);
				list.add(sym);
			} catch( ParseException ex ) {
				error(null, null, ex.getMessage());
			}
		}
		list.add(symbol);
	}
	
	private void propagateTypes() {
		for( CSymbol s : symbols) {
			String name = s.getName();
			if( name.endsWith(CSyntax.OPTSUFFIX) && name.length() > CSyntax.OPTSUFFIX.length() ) {
				CSymbol original = getSymbol(name.substring(0, name.length()-CSyntax.OPTSUFFIX.length()), -1);
				if( original != null && s.getType() == null && original.getType() != null ) {
					s.setType(original.getType());
				}
			}
		}
	}
	
	public static CSyntax process(String inputId, String contents, Map<String,String> defaultOptions) {
		try {
			char[] buff = contents.toCharArray();
			LapgParser p = new LapgParser(inputId, buff, defaultOptions);
			LapgLexer lexer = new LapgLexer(new CharArrayReader(buff), p);
			if( !p.parse(lexer) || !p.errors.isEmpty() ) {
				return new CSyntax(p.errors);
			}
			p.propagateTypes();
	
			int offset = lexer.getTemplatesStart();
			String templates = offset < buff.length && offset != -1 ? new String(buff,offset,buff.length-offset) : null;
			return new CSyntax(p.symbols,p.rules,p.prios,p.inputs,p.options,p.lexems,templates);
		} catch( UnsupportedEncodingException ex ) {
		} catch( IOException ex ) {
		}
		return null;
	}
	
	static class ParseException extends Exception {
		private static final long serialVersionUID = 2811939050284758826L;
	
		public ParseException(String arg0) {
			super(arg0);
		}
	}
	public void error(LapgLexer.LapgPlace start, LapgLexer.LapgPlace end, String s) {
		errors.add(s);
	}
    private static final int lapg_action[] = {
		-3, -1, -1, -11, 4, -1, 41, -1, -1, 8, -19, 3, 5, 6, 18, -1,
		-1, -25, 7, -33, 21, -1, 10, -1, 19, 9, -1, -41, 20, -1, -47, 22,
		-59, 27, -1, -1, -69, -81, -87, 15, 25, -97, -109, 23, 26, 24, -121, -1,
		-127, 39, -1, 33, 31, 28, -133, 13, -143, -1, 37, 38, 34, 32, 30, 17,
		40, -1, -2,
	};

    private static final short lapg_lalr[] = {
		11, -1, 1, 1, 13, 1, -1, -2, 11, -1, 1, 0, 13, 0, -1, -2,
		4, -1, 12, 11, -1, -2, 1, -1, 6, -1, 0, 2, -1, -2, 4, -1,
		8, 11, 12, 11, -1, -2, 4, -1, 8, 11, -1, -2, 16, -1, 1, 16,
		9, 16, 10, 16, 15, 16, -1, -2, 2, -1, 1, 12, 6, 12, 13, 12,
		-1, -2, 16, -1, 1, 16, 9, 16, 10, 16, 15, 16, -1, -2, 17, -1,
		18, 36, -1, -2, 1, -1, 15, -1, 9, 29, 10, 29, -1, -2, 16, -1,
		1, 16, 9, 16, 10, 16, 15, 16, -1, -2, 5, -1, 1, 14, 6, 14,
		13, 14, 16, 14, -1, -2, 17, -1, 18, 36, -1, -2, 17, -1, 18, 35,
		-1, -2, 1, -1, 15, -1, 9, 29, 10, 29, -1, -2, 16, -1, 1, 16,
		6, 16, 13, 16, -1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 11, 12, 13, 16, 20, 22, 22, 24, 25, 27, 29, 31, 33, 34,
		36, 40, 43, 45, 46, 47, 48, 49, 50, 52, 54, 55, 63, 66, 67, 71,
		75, 77, 79, 80, 82, 84, 86, 88, 90, 92, 95,
	};

    private static final short lapg_sym_from[] = {
		65, 1, 2, 8, 16, 17, 26, 34, 38, 50, 54, 32, 5, 10, 19, 27,
		5, 7, 15, 42, 8, 17, 29, 35, 21, 21, 34, 0, 3, 23, 29, 2,
		8, 15, 38, 54, 30, 36, 41, 56, 37, 46, 48, 47, 57, 0, 0, 0,
		2, 8, 0, 3, 2, 8, 7, 2, 8, 17, 26, 34, 38, 50, 54, 10,
		19, 27, 42, 30, 36, 41, 56, 30, 36, 41, 56, 8, 17, 8, 17, 26,
		30, 36, 30, 36, 38, 54, 38, 54, 37, 46, 37, 46, 37, 46, 48,
	};

    private static final short lapg_sym_to[] = {
		66, 5, 6, 6, 26, 6, 6, 6, 6, 6, 6, 42, 12, 22, 22, 22,
		13, 14, 24, 55, 16, 16, 36, 36, 30, 31, 43, 1, 1, 32, 32, 7,
		7, 25, 50, 50, 37, 37, 37, 37, 46, 46, 46, 58, 64, 65, 2, 3,
		8, 17, 4, 11, 9, 18, 15, 10, 19, 27, 33, 44, 51, 60, 61, 23,
		29, 35, 56, 38, 38, 54, 63, 39, 39, 39, 39, 20, 28, 21, 21, 34,
		40, 45, 41, 41, 52, 62, 53, 53, 47, 57, 48, 48, 49, 49, 59,
	};

    private static final short lapg_rlen[] = {
		1, 0, 3, 2, 1, 3, 3, 2, 1, 3, 1, 0, 3, 1, 0, 1,
		0, 6, 1, 2, 2, 1, 2, 4, 4, 3, 2, 1, 1, 0, 3, 2,
		3, 2, 2, 1, 0, 3, 2, 1, 3, 1,
	};

    private static final short lapg_rlex[] = {
		20, 20, 19, 21, 21, 24, 24, 22, 22, 25, 28, 28, 25, 29, 29, 30,
		30, 25, 26, 26, 23, 23, 32, 32, 33, 33, 34, 34, 37, 37, 35, 35,
		36, 36, 38, 39, 39, 31, 40, 40, 41, 27,
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
		"directivesopt",
		"directives",
		"lexical_definitions",
		"grammar_definitions",
		"directive",
		"lexical_definition",
		"iconlist_in_bits",
		"symbol",
		"typeopt",
		"iconopt",
		"commandopt",
		"command",
		"grammar_definition",
		"symbol_definition",
		"symbol_list",
		"rule_def",
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
		public static final int directivesopt = 20;
		public static final int directives = 21;
		public static final int lexical_definitions = 22;
		public static final int grammar_definitions = 23;
		public static final int directive = 24;
		public static final int lexical_definition = 25;
		public static final int iconlist_in_bits = 26;
		public static final int symbol = 27;
		public static final int typeopt = 28;
		public static final int iconopt = 29;
		public static final int commandopt = 30;
		public static final int command = 31;
		public static final int grammar_definition = 32;
		public static final int symbol_definition = 33;
		public static final int symbol_list = 34;
		public static final int rule_def = 35;
		public static final int rule_symbols = 36;
		public static final int rule_priorityopt = 37;
		public static final int rule_priority = 38;
		public static final int command_tokensopt = 39;
		public static final int command_tokens = 40;
		public static final int command_token = 41;
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

		while( lapg_m[lapg_head].state != 66 ) {
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

		if( lapg_m[lapg_head].state != 66 ) {
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
			case 5:  // directive ::= '.' identifier scon
				 options.put(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym)); 
				break;
			case 6:  // directive ::= '.' identifier icon
				 options.put(((String)lapg_m[lapg_head-1].sym), ((Integer)lapg_m[lapg_head-0].sym).toString()); 
				break;
			case 9:  // lexical_definition ::= '[' iconlist_in_bits ']'
				 currentgroups = ((Integer)lapg_m[lapg_head-1].sym); 
				break;
			case 12:  // lexical_definition ::= symbol typeopt ':'
				 addLexem(((CSymbol)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, lapg_m[lapg_head-2].pos.line); 
				break;
			case 17:  // lexical_definition ::= symbol typeopt ':' regexp iconopt commandopt
				 addLexem(((CSymbol)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((CAction)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-5].pos.line); 
				break;
			case 18:  // iconlist_in_bits ::= icon
				 if( ((Integer)lapg_m[lapg_head-0].sym) < 0 || ((Integer)lapg_m[lapg_head-0].sym) >= BITS ) lapg_gg.sym = 0; else lapg_gg.sym = 1 << ((Integer)lapg_m[lapg_head-0].sym); 
				break;
			case 19:  // iconlist_in_bits ::= iconlist_in_bits icon
				 lapg_gg.sym = ((Integer)lapg_gg.sym) | ((Integer)lapg_m[lapg_head-0].sym); 
				break;
			case 23:  // grammar_definition ::= '%' identifier symbol_list ';'
				 addGrammarDirective(((String)lapg_m[lapg_head-2].sym), ((List<CSymbol>)lapg_m[lapg_head-1].sym), lapg_m[lapg_head-2].pos.line); 
				break;
			case 24:  // symbol_definition ::= symbol typeopt '::=' rule_def
				 addNonterm(((CSymbol)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-3].pos.line); addRule(((CRule)lapg_m[lapg_head-0].sym),((CSymbol)lapg_m[lapg_head-3].sym)); 
				break;
			case 25:  // symbol_definition ::= symbol_definition '|' rule_def
				 addRule(((CRule)lapg_m[lapg_head-0].sym),((CSymbol)lapg_gg.sym)); 
				break;
			case 26:  // symbol_list ::= symbol_list symbol
				 ((List<CSymbol>)lapg_gg.sym).add(((CSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 27:  // symbol_list ::= symbol
				 lapg_gg.sym = new ArrayList<CSymbol>(); ((List<CSymbol>)lapg_gg.sym).add(((CSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 30:  // rule_def ::= rule_symbols commandopt rule_priorityopt
				 lapg_gg.sym = new CRule(((List<CSymbol>)lapg_m[lapg_head-2].sym), ((CAction)lapg_m[lapg_head-1].sym), ((CSymbol)lapg_m[lapg_head-0].sym), inputId, lapg_m[lapg_head-2].pos.line); 
				break;
			case 31:  // rule_def ::= commandopt rule_priorityopt
				 lapg_gg.sym = new CRule(null, ((CAction)lapg_m[lapg_head-1].sym), ((CSymbol)lapg_m[lapg_head-0].sym), inputId, lapg_m[lapg_head-1].pos.line); 
				break;
			case 32:  // rule_symbols ::= rule_symbols commandopt symbol
				 addRuleSymbol(((List<CSymbol>)lapg_gg.sym),((CAction)lapg_m[lapg_head-1].sym),((CSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 33:  // rule_symbols ::= commandopt symbol
				 lapg_gg.sym = new ArrayList<CSymbol>(); addRuleSymbol(((List<CSymbol>)lapg_gg.sym),((CAction)lapg_m[lapg_head-1].sym),((CSymbol)lapg_m[lapg_head-0].sym)); 
				break;
			case 34:  // rule_priority ::= '<<' symbol
				 lapg_gg.sym = ((CSymbol)lapg_m[lapg_head-0].sym); 
				break;
			case 37:  // command ::= '{' command_tokensopt '}'
				 lapg_gg.sym = new CAction(rawData(lapg_m[lapg_head-2].pos.offset+1,lapg_m[lapg_head-0].pos.offset), inputId, lapg_m[lapg_head-2].pos.line); 
				break;
			case 41:  // symbol ::= identifier
				 lapg_gg.sym = getSymbol(((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-0].pos.line); 
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
