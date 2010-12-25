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
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.objects.DefaultJavaIxObject;
import org.textway.templates.objects.IxObject;
import org.textway.templates.objects.JavaIxFactory;

import java.util.*;

public class GrammarIxFactory extends JavaIxFactory {

	private final String templatePackage;
	private final EvaluationContext rootContext;
	private IEvaluationStrategy evaluationStrategy;

	public GrammarIxFactory(String templatePackage, EvaluationContext context) {
		this.templatePackage = templatePackage;
		this.rootContext = context;
	}

	public void setStrategy(IEvaluationStrategy strategy) {
		this.evaluationStrategy = strategy;
	}

	@Override
	public IxObject asObject(Object o) {
		if (o instanceof Rule) {
			return new RuleIxObject((Rule) o);
		}

		if (o instanceof Symbol) {
			return new SymbolIxObject((Symbol) o);
		}

		if (o instanceof SymbolRef) {
			return new SymbolRefIxObject((SymbolRef) o);
		}

		if (o instanceof Grammar) {
			return new GrammarIxObject((Grammar) o);
		}

		return super.asObject(o);
	}

	private final class RuleIxObject extends DefaultJavaIxObject {

		private final Rule rule;

		private RuleIxObject(Rule rule) {
			super(rule);
			this.rule = rule;
		}

		public Object callMethod(String methodName, Object[] args) throws EvaluationException {
			if (args == null || args.length == 0) {
				if (methodName.equals("left")) {
					return new ActionSymbol(rule.getLeft(), null, true, 0, evaluationStrategy, rootContext, templatePackage);
				}
				if (methodName.equals("last") || methodName.equals("first")) {
					SymbolRef[] array = rule.getRight();
					int i = methodName.charAt(0) == 'f' ? 0 : array.length - 1;
					return new ActionSymbol(array[i].getTarget(), array[i], false, array.length - i - 1,
							evaluationStrategy, rootContext, templatePackage);
				}
			}
			return super.callMethod(methodName, args);
		}

		public Object getByIndex(Object index) throws EvaluationException {
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

		public Object getProperty(String id) throws EvaluationException {
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
			throw new EvaluationException("symbol `" + id + "` is undefined");
		}
	}

	private final class SymbolIxObject extends DefaultJavaIxObject {

		private final Symbol sym;

		private SymbolIxObject(Symbol sym) {
			super(sym);
			this.sym = sym;
		}

		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof String) {
				return sym.getAnnotation((String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}
	}

	private final class SymbolRefIxObject extends DefaultJavaIxObject {

		private final SymbolRef sym;

		private SymbolRefIxObject(SymbolRef sym) {
			super(sym);
			this.sym = sym;
		}

		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof String) {
				return sym.getAnnotation((String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}
	}

	private final Map<Grammar, GrammarRules> rules = new HashMap<Grammar, GrammarRules>();

	private final class GrammarIxObject extends DefaultJavaIxObject {

		private final Grammar grammar;

		private GrammarIxObject(Grammar grammar) {
			super(grammar);
			this.grammar = grammar;
		}

		public Object getProperty(String propertyName) throws EvaluationException {
			if ("rules".equals(propertyName)) {
				GrammarRules gr = rules.get(grammar);
				if (gr == null) {
					gr = new GrammarRules(grammar);
					rules.put(grammar, gr);
				}
				return gr;
			}
			return super.getProperty(propertyName);
		}
	}

	private class GrammarRules extends DefaultJavaIxObject implements Iterable<Rule> {

		private final Rule[] myRules;
		private Map<Symbol, List<Rule>> rulesBySymbol;
		private Map<Symbol, List<Rule>> rulesWithSymbol;

		public GrammarRules(Grammar grammar) {
			super(grammar);
			myRules = grammar.getRules();
		}

		public Map<Symbol, List<Rule>> getRulesBySymbol() {
			if (rulesBySymbol != null) {
				return rulesBySymbol;
			}
			rulesBySymbol = new HashMap<Symbol, List<Rule>>();
			for (Rule r : myRules) {
				List<Rule> target = rulesBySymbol.get(r.getLeft());
				if (target == null) {
					target = new ArrayList<Rule>();
					rulesBySymbol.put(r.getLeft(), target);
				}
				target.add(r);
			}
			return rulesBySymbol;
		}

		public Map<Symbol, List<Rule>> getRulesContainingSymbol() {
			if (rulesWithSymbol != null) {
				return rulesWithSymbol;
			}
			rulesWithSymbol = new HashMap<Symbol, List<Rule>>();
			Set<Symbol> seen = new HashSet<Symbol>();
			for (Rule r : myRules) {
				seen.clear();
				for (SymbolRef sref : r.getRight()) {
					Symbol s = sref.getTarget();
					if (seen.contains(s)) {
						continue;
					}
					seen.add(s);
					List<Rule> list = rulesWithSymbol.get(s);
					if (list == null) {
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
			if (args.length == 1 && "with".equals(methodName) && args[0] instanceof Symbol) {
				List<Rule> list = getRulesContainingSymbol().get(args[0]);
				return list != null ? list : Collections.emptyList();
			}
			return asObject(myRules).callMethod(methodName, args);
		}

		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof Symbol) {
				List<Rule> list = getRulesBySymbol().get(index);
				return list != null ? list : Collections.emptyList();
			}
			return asObject(myRules).getByIndex(index);
		}

		public Object getProperty(String id) throws EvaluationException {
			return asObject(myRules).getProperty(id);
		}

		@Override
		public Iterator asSequence() {
			return iterator();
		}
	}
}
