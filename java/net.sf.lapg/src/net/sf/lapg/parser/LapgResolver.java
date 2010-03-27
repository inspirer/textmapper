package net.sf.lapg.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.lapg.api.Action;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Prio;
import net.sf.lapg.parser.LapgTree.ParseProblem;
import net.sf.lapg.parser.ast.AstCode;
import net.sf.lapg.parser.ast.AstDirective;
import net.sf.lapg.parser.ast.AstGrammarPart;
import net.sf.lapg.parser.ast.AstGroupsSelector;
import net.sf.lapg.parser.ast.AstIdentifier;
import net.sf.lapg.parser.ast.AstLexeme;
import net.sf.lapg.parser.ast.AstLexerPart;
import net.sf.lapg.parser.ast.AstNonTerm;
import net.sf.lapg.parser.ast.AstOption;
import net.sf.lapg.parser.ast.AstRegexp;
import net.sf.lapg.parser.ast.AstRuleSymbol;
import net.sf.lapg.parser.ast.AstRoot;
import net.sf.lapg.parser.ast.AstRule;
import net.sf.lapg.parser.ast.AstNode;

public class LapgResolver {

	private final LapgTree<AstRoot> tree;
	private final Map<String, LiSymbol> symbolsMap = new HashMap<String, LiSymbol>();;

	private final List<LiSymbol> symbols = new ArrayList<LiSymbol>();
	private List<LiLexem> lexems;
	private List<LiRule> rules;
	private List<LiPrio> priorities;

	private List<LiSymbol> inputs;
	private LiSymbol eoi;
	private LiSymbol error;

	private final Map<String,Object> options;

	public LapgResolver(LapgTree<AstRoot> tree, Map<String, Object> options) {
		this.tree = tree;
		this.options = options;
	}

	public Grammar resolve() {
		if(tree.getRoot() == null) {
			return null;
		}
		collectLexems();
		int terminals = symbols.size();

		collectNonTerminals();
		collectRules();
		collectDirectives();
		collectOptions();
		String templates = getTemplates();

		if(inputs.size() == 0) {
			LiSymbol input = symbolsMap.get("input");
			if(input == null) {
				error(tree.getRoot(), "no input non-terminal");
			} else if(input.isTerm()) {
				error(tree.getRoot(), "input should be non-terminal");
			} else {
				inputs.add(input);
			}
		}

		LiRule[] ruleArr = rules.toArray(new LiRule[rules.size()]);
		for(int i = 0; i < ruleArr.length; i++) {
			ruleArr[i].setIndex(i);
		}
		LiSymbol[] symbolArr = symbols.toArray(new LiSymbol[symbols.size()]);
		for(int i = 0; i < symbolArr.length; i++) {
			symbolArr[i].setIndex(i);
		}
		LiLexem[] lexemArr = lexems.toArray(new LiLexem[lexems.size()]);
		LiPrio[] prioArr = priorities.toArray(new LiPrio[priorities.size()]);
		LiSymbol[] inputArr = inputs.toArray(new LiSymbol[inputs.size()]);

		error = symbolsMap.get("error");

		return new LiGrammar(symbolArr, ruleArr, prioArr, lexemArr,
				inputArr, eoi, error, options,
				templates, terminals,
				!tree.getErrors().isEmpty());
	}

	private String getTemplates() {
		int offset = tree.getRoot() != null ? tree.getRoot().getTemplatesStart() : -1;
		char[] text = tree.getSource().getContents();
		return offset < text.length && offset != -1 ? new String(text,offset,text.length-offset) : "";
	}

	private LiSymbol create(AstIdentifier id, String type, boolean isTerm) {
		String name = id.getName();
		if(symbolsMap.containsKey(name)) {
			LiSymbol sym = symbolsMap.get(name);
			if(sym.isTerm() != isTerm) {
				error(id, "redeclaration of " + (isTerm ? "non-terminal" : "terminal") + ": " + name);
			} else if(!safeEquals(sym.getType(), type)) {
				error(id, "redeclaration of type: " + (type == null ? "<empty>" : type) + " instead of " + (sym.getType() == null ? "<empty>" : sym.getType()));
			}
			return sym;
		} else {
			LiSymbol sym = new LiSymbol(name, type, isTerm);
			symbolsMap.put(name, sym);
			symbols.add(sym);
			return sym;
		}
	}

	private LiSymbol resolve(AstIdentifier id) {
		String name = id.getName();
		LiSymbol sym = symbolsMap.get(name);
		if(sym == null) {
			if(name.length() > 3 && name.endsWith("opt")) {
				sym = symbolsMap.get(name.substring(0, name.length()-3));
				if(sym != null) {
					LiSymbol symopt = create(id, sym.getType(), false);
					rules.add(new LiRule(symopt, new LiSymbolRef[0], null, null, id, null));
					rules.add(new LiRule(symopt, new LiSymbolRef[]{new LiSymbolRef(sym,null,null)}, null, null, id, null));
					return symopt;
				}
			}
			error(id, name + " cannot be resolved");
		}
		return sym;
	}

	private int convert(AstGroupsSelector selector) {
		int result = 0;
		for(Integer i : selector.getGroups()) {
			if(i == null || i < 0 || i > 31) {
				error(selector, "group id should be in range 0..31");
				return 1;
			} else if((result & (1 << i)) != 0) {
				error(selector, "duplicate group id: " + i);
				return 1;
			} else {
				result |= (1<<i);
			}
		}
		if(result == 0) {
			error(selector, "empty group set");
			return 1;
		}
		return result;
	}

	private Action convert(final AstCode code) {
		if(code == null) {
			return null;
		}
		return new LiAction(code.toString(), tree.getSource().getFile(), code.getLine());
	}

	private String convert(AstRegexp regexp) {
		if(regexp == null) {
			return null;
		}
		return regexp.getRegexp();
	}

	private void collectLexems() {
		eoi = new LiSymbol("eoi", null, true);
		symbolsMap.put(eoi.getName(), eoi);
		symbols.add(eoi);
		int groups = 1;

		lexems = new ArrayList<LiLexem>(tree.getRoot().getLexer().size());

		for(AstLexerPart clause : tree.getRoot().getLexer()) {
			if(clause instanceof AstLexeme) {
				AstLexeme lexeme = (AstLexeme) clause;
				LiSymbol s = create(lexeme.getName(), lexeme.getType(), true);
				if(lexeme.getRegexp() != null) {
					LiLexem l = new LiLexem(s, convert(lexeme.getRegexp()), groups, lexeme.getPriority(), convert(lexeme.getCode()));
					lexems.add(l);
				}
			} else if(clause instanceof AstGroupsSelector) {
				groups = convert((AstGroupsSelector) clause);
			}
		}
	}

	private void addSymbolAnnotations(AstIdentifier id, Map<String,Object> annotations) {
		if(annotations != null){
			LiSymbol sym = symbolsMap.get(id.getName());
			for(Map.Entry<String, Object> ann : annotations.entrySet()) {
				if(sym.getAnnotation(ann.getKey()) != null) {
					error(id, "redeclaration of annotation `" + ann.getKey() + "' for non-terminal: " + id.getName() + ", skipped");
				} else {
					sym.addAnnotation(ann.getKey(), ann.getValue());
				}
			}
		}
	}

	private void collectNonTerminals() {
		for(AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if(clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				create(nonterm.getName(), nonterm.getType(), false);
			}
		}
		for(AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if(clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				addSymbolAnnotations(nonterm.getName(), convert(nonterm.getAnnotations()));
			}
		}
	}

	private void createRule(LiSymbol left, AstRule right, List<LiSymbolRef> rightPart) {
		List<AstRuleSymbol> list = right.getList();
		rightPart.clear();
		if(list != null) {
			for(AstRuleSymbol rs : list) {
				AstCode astCode = rs.getCode();
				if(astCode != null) {
					LiSymbol codeSym = new LiSymbol("{}", null, false);
					symbols.add(codeSym);
					rightPart.add(new LiSymbolRef(codeSym, null, null));
					rules.add(new LiRule(codeSym, null, convert(astCode), null, astCode, null));
				}
				LiSymbol sym = resolve(rs.getSymbol());
				if(sym != null) {
					rightPart.add(new LiSymbolRef(sym, rs.getAlias(), convert(rs.getAnnotations())));
				}
			}
		}
		LiSymbol prio = right.getPriority() != null ? resolve(right.getPriority()) : null;
		rules.add(new LiRule(left, rightPart.toArray(new LiSymbolRef[rightPart.size()]), convert(right.getAction()), prio, right, convert(right.getAnnotations())));
	}

	private void collectRules() {
		rules = new ArrayList<LiRule>();
		ArrayList<LiSymbolRef> rightPart = new ArrayList<LiSymbolRef>(32);
		for(AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if(clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				LiSymbol left = resolve(nonterm.getName());
				for(AstRule right : nonterm.getRules()) {
					createRule(left, right, rightPart);
				}
			}
		}
	}

	private List<LiSymbol> convert(List<AstIdentifier> input) {
		ArrayList<LiSymbol> result = new ArrayList<LiSymbol>(input.size());
		for(AstIdentifier id : input) {
			LiSymbol sym = resolve(id);
			if(sym != null) {
				result.add(sym);
			}
		}
		return result;
	}

	private void collectDirectives() {
		priorities = new ArrayList<LiPrio>();
		inputs = new ArrayList<LiSymbol>();

		for(AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if(clause instanceof AstDirective) {
				AstDirective directive = (AstDirective) clause;
				String key = directive.getKey();
				List<LiSymbol> val = convert(directive.getSymbols());
				if(key.equals("input")) {
					inputs.addAll(val);
				} else if(key.equals("left")) {
					priorities.add(new LiPrio(Prio.LEFT, val.toArray(new LiSymbol[val.size()])));
				} else if( key.equals("right") ) {
					priorities.add(new LiPrio(Prio.RIGHT, val.toArray(new LiSymbol[val.size()])));
				} else if( key.equals("nonassoc") ) {
					priorities.add(new LiPrio(Prio.NONASSOC, val.toArray(new LiSymbol[val.size()])));
				} else {
					error(directive, "unknown directive identifier used: `"+key+"`");
				}
			}
		}
	}

	private void collectOptions() {
		if(tree.getRoot().getOptions() == null) {
			return;
		}
		for(AstOption option : tree.getRoot().getOptions()) {
			options.put(option.getKey(), convertExpression(option.getValue()));
		}
	}

	private static boolean safeEquals(Object o1, Object o2) {
		return o1 == null || o2 == null ? o1 == o2 : o1.equals(o2);
	}

	private void error(AstNode n, String message) {
		tree.getErrors().add(new ParseProblem(2, n.getOffset(), n.getEndOffset(), message, null));
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> convert(Map<String,Object> astAnnotations) {
		return (Map<String, Object>) convertExpression(astAnnotations);
	}

	@SuppressWarnings("unchecked")
	private Object convertExpression(Object o) {
		if(o instanceof Map) {
			Map<String,Object> result = new HashMap<String,Object>();
			for(Map.Entry<String, Object> entry : ((Map<String,Object>)o).entrySet()) {
				result.put(entry.getKey(), convertExpression(entry.getValue()));
			}
			return result;
		}
		if(o instanceof List) {
			ArrayList<Object> result = new ArrayList<Object>(((List)o).size());
			for(Object v : ((List)o)) {
				result.add(convertExpression(v));
			}
			return result;
		}
		if(o instanceof AstIdentifier) {
			return resolve((AstIdentifier)o);
		}
		return o;
	}
}
