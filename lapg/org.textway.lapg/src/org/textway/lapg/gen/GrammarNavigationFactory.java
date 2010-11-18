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
package org.textway.lapg.gen;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;
import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.INavigationStrategy;
import org.textway.templates.api.IProxyObject;
import org.textway.templates.eval.DefaultNavigationFactory;

import java.util.*;

public class GrammarNavigationFactory extends DefaultNavigationFactory {

	private final String templatePackage;
	private final EvaluationContext rootContext;

	public GrammarNavigationFactory(String templatePackage, EvaluationContext context) {
		this.templatePackage = templatePackage;
		this.rootContext = context;
	}

	@Override
	public INavigationStrategy<?> getStrategy(Object o) {

		if (o instanceof Rule) {
			return ruleNavigation;
		}

		if (o instanceof Symbol) {
			return symbolNavigation;
		}

		if (o instanceof SymbolRef) {
			return symbolrefNavigation;
		}

		if(o instanceof Grammar) {
			return grammarNavigation;
		}

		return super.getStrategy(o);
	}

	private final INavigationStrategy<Rule> ruleNavigation = new INavigationStrategy<Rule>() {

		public Object callMethod(Rule rule, String methodName, Object[] args) throws EvaluationException {
			if(args == null || args.length == 0) {
				if(methodName.equals("left")) {
					return new ActionSymbol(rule.getLeft(), null, true, 0, evaluationStrategy, rootContext, templatePackage);
				}
				if(methodName.equals("last") || methodName.equals("first")) {
					SymbolRef[] array = rule.getRight();
					int i = methodName.charAt(0) == 'f' ? 0 : array.length-1;
					return new ActionSymbol(array[i].getTarget(), array[i], false, array.length - i - 1,
							evaluationStrategy, rootContext, templatePackage);
				}
			}
			return javaNavigation.callMethod(rule, methodName, args);
		}

		public Object getByIndex(Rule rule, Object index) throws EvaluationException {
			if (index instanceof Integer) {
				int i = (Integer) index;
				SymbolRef[] array = rule.getRight();
				return new ActionSymbol(array[i].getTarget(), array[i], false, array.length - i - 1,
						evaluationStrategy, rootContext, templatePackage);
			} else if (index instanceof String) {
				return rule.getAnnotation((String) index);
			} else {
				throw new EvaluationException(
				"index object should be integer (right part index) or string (annotation name)");
			}
		}

		public Object getProperty(Rule rule, String id) throws EvaluationException {
			ArrayList<ActionSymbol> result = new ArrayList<ActionSymbol>();
			if (rule.getLeft().getName().equals(id)) {
				result.add(new ActionSymbol(rule.getLeft(), null, true, 0, evaluationStrategy, rootContext, templatePackage));
			}

			SymbolRef[] right = rule.getRight();
			for (int i = 0; i < right.length; i++) {
				String name = right[i].getTarget().getName();
				if (right[i].getAlias() != null) {
					name = right[i].getAlias();
				}
				if (id.equals(name)) {
					result.add(new ActionSymbol(right[i].getTarget(), right[i], false, right.length - i - 1,
							evaluationStrategy, rootContext, templatePackage));
				}
			}

			if (result.size() == 1) {
				return result.get(0);
			} else if (result.size() > 1) {
				return result;
			}
			throw new EvaluationException("do not know symbol `" + id + "`");
		}
	};

	private final INavigationStrategy<Symbol> symbolNavigation = new INavigationStrategy<Symbol>() {

		public Object callMethod(Symbol obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Symbol sym, Object index) throws EvaluationException {
			if (index instanceof String) {
				return sym.getAnnotation((String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}

		public Object getProperty(Symbol obj, String propertyName) throws EvaluationException {
			return javaNavigation.getProperty(obj, propertyName);
		}
	};

	private final INavigationStrategy<SymbolRef> symbolrefNavigation = new INavigationStrategy<SymbolRef>() {

		public Object callMethod(SymbolRef obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(SymbolRef sym, Object index) throws EvaluationException {
			if (index instanceof String) {
				return sym.getAnnotation((String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}

		public Object getProperty(SymbolRef obj, String propertyName) throws EvaluationException {
			return javaNavigation.getProperty(obj, propertyName);
		}
	};

	private final INavigationStrategy<Grammar> grammarNavigation = new INavigationStrategy<Grammar>() {

		private final Map<Grammar,GrammarRules> rules = new HashMap<Grammar, GrammarRules>();

		public Object callMethod(Grammar obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Grammar obj, Object index) throws EvaluationException {
			return javaNavigation.getByIndex(obj, index);
		}

		public Object getProperty(Grammar grammar, String propertyName) throws EvaluationException {
			if("rules".equals(propertyName)) {
				GrammarRules gr = rules.get(grammar);
				if(gr == null) {
					gr = new GrammarRules(grammar);
					rules.put(grammar, gr);
				}
				return gr;
			}
			return javaNavigation.getProperty(grammar, propertyName);
		}
	};

	private class GrammarRules implements Iterable<Rule>, IProxyObject {

		private final Rule[] myRules;
		private Map<Symbol,List<Rule>> rulesBySymbol;
		private Map<Symbol,List<Rule>> rulesWithSymbol;

		public GrammarRules(Grammar grammar) {
			myRules = grammar.getRules();
		}

		public Map<Symbol,List<Rule>> getRulesBySymbol() {
			if(rulesBySymbol != null) {
				return rulesBySymbol;
			}
			rulesBySymbol = new HashMap<Symbol,List<Rule>>();
			for(Rule r : myRules) {
				List<Rule> target = rulesBySymbol.get(r.getLeft());
				if(target == null) {
					target = new ArrayList<Rule>();
					rulesBySymbol.put(r.getLeft(), target);
				}
				target.add(r);
			}
			return rulesBySymbol;
		}

		public Map<Symbol,List<Rule>> getRulesContainingSymbol() {
			if(rulesWithSymbol != null) {
				return rulesWithSymbol;
			}
			rulesWithSymbol = new HashMap<Symbol,List<Rule>>();
			Set<Symbol> seen = new HashSet<Symbol>();
			for(Rule r : myRules) {
				seen.clear();
				for(SymbolRef sref : r.getRight()) {
					Symbol s = sref.getTarget();
					if(seen.contains(s)) {
						continue;
					}
					seen.add(s);
					List<Rule> list = rulesWithSymbol.get(s);
					if(list == null) {
						list = new ArrayList<Rule>();
						rulesWithSymbol.put(s, list);
					}
					list.add(r);
				}
			}
			return rulesWithSymbol;
		}

		public Iterator<Rule> iterator() {
			return new Iterator<Rule>() {
				int index = 0;

				public boolean hasNext() {
					return index < myRules.length;
				}

				public Rule next() {
					return index < myRules.length ? myRules[index++] : null;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		public Object callMethod(String methodName, Object[] args) throws EvaluationException {
			if(args.length == 1 && "with".equals(methodName) && args[0] instanceof Symbol) {
				List<Rule> list = getRulesContainingSymbol().get(args[0]);
				return list != null ? list : Collections.emptyList();
			}
			return arrayNavigation.callMethod(myRules, methodName, args);
		}

		public Object getByIndex(Object index) throws EvaluationException {
			if(index instanceof Symbol) {
				List<Rule> list = getRulesBySymbol().get(index);
				return list != null ? list : Collections.emptyList();
			}
			return arrayNavigation.getByIndex(myRules, index);
		}

		public Object getProperty(String id) throws EvaluationException {
			return arrayNavigation.getProperty(myRules, id);
		}

	}
}
