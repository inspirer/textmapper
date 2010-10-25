/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.parser;

import java.util.*;

import org.textway.lapg.api.Action;
import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.Prio;
import org.textway.lapg.common.FormatUtil;
import org.textway.lapg.parser.LapgTree.LapgProblem;
import org.textway.lapg.parser.ast.*;

public class LapgResolver {

	public static final String RESOLVER_SOURCE = "problem.resolver"; //$NON-NLS-1$

	private final LapgTree<AstRoot> tree;
	private final Map<String, LiSymbol> symbolsMap = new HashMap<String, LiSymbol>();

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
        for(LiSymbol s : symbolArr) {
            String name = s.getName();
            if(FormatUtil.isIdentifier(name)) {
                usedIdentifiers.add(name);
            }
        }
		for(int i = 0; i < symbolArr.length; i++) {
			symbolArr[i].setId(i, generateId(symbolArr[i].getName(), i));
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


    private final Set<String> usedIdentifiers = new HashSet<String>();

    private String generateId(String name, int i) {
        if(usedIdentifiers.contains(name)) {
            return name;
        }
        name = FormatUtil.toIdentifier(name, i);
        String result = name;
        int i1 = 2;
        while(usedIdentifiers.contains(result)) {
            result = name + i1++;
        }
        usedIdentifiers.add(result);
        return result;
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
			LiSymbol sym = new LiSymbol(name, type, isTerm, id);
			symbolsMap.put(name, sym);
			symbols.add(sym);
			return sym;
		}
	}

	private LiSymbol resolve(AstReference id) {
		String name = id.getName();
		LiSymbol sym = symbolsMap.get(name);
		if(sym == null) {
			if(name.length() > 3 && name.endsWith("opt")) {
				sym = symbolsMap.get(name.substring(0, name.length()-3));
				if(sym != null) {
					LiSymbol symopt = create(new AstIdentifier(id.getName(), id.getInput(), id.getOffset(), id.getEndOffset()), sym.getType(), false);
					rules.add(new LiRule(symopt, new LiSymbolRef[0], null, null, null, id));
					rules.add(new LiRule(symopt, new LiSymbolRef[]{new LiSymbolRef(sym,null,null,null)}, null, null, null, id));
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
		return new LiAction(code.toString(), code);
	}

	private String convert(AstRegexp regexp) {
		if(regexp == null) {
			return null;
		}
		return regexp.getRegexp();
	}

	private void collectLexems() {
		eoi = new LiSymbol("eoi", null, true, null);
		symbolsMap.put(eoi.getName(), eoi);
		symbols.add(eoi);
		int groups = 1;

		lexems = new ArrayList<LiLexem>(tree.getRoot().getLexer().size());

		for(AstLexerPart clause : tree.getRoot().getLexer()) {
			if(clause instanceof AstLexeme) {
				AstLexeme lexeme = (AstLexeme) clause;
				LiSymbol s = create(lexeme.getName(), lexeme.getType(), true);
				if(lexeme.getRegexp() != null) {
					LiLexem l = new LiLexem(s, convert(lexeme.getRegexp()), groups, lexeme.getPriority(), convert(lexeme.getCode()), lexeme);
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
				if(rs.hasSyntaxError()) {
					continue;
				}
				AstCode astCode = rs.getCode();
				if(astCode != null) {
					LiSymbol codeSym = new LiSymbol("{}", null, false, astCode);
					symbols.add(codeSym);
					rightPart.add(new LiSymbolRef(codeSym, null, null, null));
					rules.add(new LiRule(codeSym, null, convert(astCode), null, null, astCode));
				}
				LiSymbol sym = resolve(rs.getSymbol());
				if(sym != null) {
					// TODO check duplicate alias
					rightPart.add(new LiSymbolRef(sym, rs.getAlias(), convert(rs.getAnnotations()), rs.getSymbol()));
				}
			}
		}
		LiSymbol prio = right.getPriority() != null ? resolve(right.getPriority()) : null;
		rules.add(new LiRule(left, rightPart.toArray(new LiSymbolRef[rightPart.size()]), convert(right.getAction()), prio, convert(right.getAnnotations()), right));
	}

	private void collectRules() {
		rules = new ArrayList<LiRule>();
		List<LiSymbolRef> rightPart = new ArrayList<LiSymbolRef>(32);
		for(AstGrammarPart clause : tree.getRoot().getGrammar()) {
			if(clause instanceof AstNonTerm) {
				AstNonTerm nonterm = (AstNonTerm) clause;
				LiSymbol left = symbolsMap.get(nonterm.getName().getName());
				if(left == null) {
					continue; /* error is already reported */
				}
				for(AstRule right : nonterm.getRules()) {
					if(!right.hasSyntaxError()) {
						createRule(left, right, rightPart);
					}
				}
			}
		}
	}

	private List<LiSymbol> resolve(List<AstReference> input) {
		List<LiSymbol> result = new ArrayList<LiSymbol>(input.size());
		for(AstReference id : input) {
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
				List<LiSymbol> val = resolve(directive.getSymbols());
				if(key.equals("input")) {
					inputs.addAll(val);
				} else if(key.equals("left")) {
					priorities.add(new LiPrio(Prio.LEFT, val.toArray(new LiSymbol[val.size()]), directive));
				} else if( key.equals("right") ) {
					priorities.add(new LiPrio(Prio.RIGHT, val.toArray(new LiSymbol[val.size()]), directive));
				} else if( key.equals("nonassoc") ) {
					priorities.add(new LiPrio(Prio.NONASSOC, val.toArray(new LiSymbol[val.size()]), directive));
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
		for(AstOptionPart option : tree.getRoot().getOptions()) {
			if(option instanceof AstOption) {
				options.put(((AstOption)option).getKey(), convertExpression(((AstOption)option).getValue()));
			}
		}
	}

	private static boolean safeEquals(Object o1, Object o2) {
		return o1 == null || o2 == null ? o1 == o2 : o1.equals(o2);
	}

	private void error(IAstNode n, String message) {
		tree.getErrors().add(new LapgResolverProblem(LapgTree.KIND_ERROR, n.getOffset(), n.getEndOffset(), message));
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> convert(AstAnnotations astAnnotations) {
		return (Map<String, Object>) convertExpression(astAnnotations);
	}

	@SuppressWarnings("unchecked")
	private Object convertExpression(Object o) {
		if(o instanceof AstMap || o instanceof AstAnnotations) {
			List<AstNamedEntry> list = o instanceof AstMap ? ((AstMap) o).getEntries() : ((AstAnnotations) o).getAnnotations();
			Map<String,Object> result = new HashMap<String,Object>();
			for(AstNamedEntry entry : list) {
				if(entry.hasSyntaxError()) {
					continue;
				}
				AstExpression expr = entry.getExpression();
				if(expr == null && o instanceof AstAnnotations) {
					result.put(entry.getName(), Boolean.TRUE);
				} else {
					result.put(entry.getName(), convertExpression(expr));
				}
			}
			return result;
		}
		if(o instanceof AstArray) {
			List<AstExpression> list = ((AstArray) o).getExpressions();

			List<Object> result = new ArrayList<Object>(list.size());
			for(Object v : list) {
				result.add(convertExpression(v));
			}
			return result;
		}
		if(o instanceof AstReference) {
			return resolve((AstReference)o);
		}
		if(o instanceof AstLiteralExpression) {
			return ((AstLiteralExpression) o).getLiteral();
		}
		return null;
	}

	private static class LapgResolverProblem extends LapgProblem {
		private static final long serialVersionUID = 3810706800688899470L;

		public LapgResolverProblem(int kind, int offset, int endoffset, String message) {
			super(kind, offset, endoffset, message, null);
		}

		@Override
		public String getSource() {
			return RESOLVER_SOURCE;
		}
	}
}
