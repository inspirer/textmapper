/**
 * Copyright 2002-2011 Evgeny Gryaznov
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

import org.textway.lapg.api.*;
import org.textway.lapg.parser.LapgGrammar;
import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.objects.DefaultJavaIxObject;
import org.textway.templates.objects.IxObject;
import org.textway.templates.objects.IxWrapper;
import org.textway.templates.objects.JavaIxFactory;

import java.util.*;

public class GrammarIxFactory extends JavaIxFactory {

	private final String templatePackage;
	private final EvaluationContext rootContext;
	private IEvaluationStrategy evaluationStrategy;
	private final LapgGrammar grammar;

	public GrammarIxFactory(LapgGrammar g, String templatePackage, EvaluationContext context) {
		grammar = g;
		this.templatePackage = templatePackage;
		rootContext = context;
	}

	@Override
	public void setStrategy(IEvaluationStrategy strategy) {
		evaluationStrategy = strategy;
	}

	@Override
	public IxObject asObject(Object o) {
		if (o instanceof IxObject) {
			return (IxObject) o;
		}
		if (o instanceof IxWrapper) {
			o = ((IxWrapper) o).getObject();
		}
		if (o instanceof Lexem) {
			return new LexemIxObject((Lexem) o);
		}
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
		if (o instanceof LapgGrammar) {
			return new LapgGrammarIxObject((LapgGrammar) o);
		}
		return super.asObject(o);
	}

	private final class LexemIxObject extends DefaultJavaIxObject {
		private final Lexem lexem;

		private LexemIxObject(Lexem lexem) {
			super(lexem);
			this.lexem = lexem;
		}

		@Override
		public Object getProperty(String propertyName) throws EvaluationException {
			if ("action".equals(propertyName)) {
				return grammar.getCode(lexem);
			}
			return super.getProperty(propertyName);
		}
	}

	private final class RuleIxObject extends DefaultJavaIxObject {

		private final Rule rule;

		private RuleIxObject(Rule rule) {
			super(rule);
			this.rule = rule;
		}

		@Override
		public Object callMethod(String methodName, Object... args) throws EvaluationException {
			if (args == null || args.length == 0) {
				if ("getAction".equals(methodName)) {
					return grammar.getCode(rule);
				}
				if ("left".equals(methodName)) {
					return new ActionSymbol(grammar, rule.getLeft(), null, true, 0, evaluationStrategy, rootContext, templatePackage);
				}
				if (methodName.equals("last") || methodName.equals("first")) {
					SymbolRef[] array = rule.getRight();
					if (array == null || array.length == 0) {
						throw new EvaluationException(methodName + "() cannot be used on empty rule");
					}
					int i;
					if (methodName.charAt(0) == 'f') {
						i = 0;
						while (i < array.length && !isRulePart(array[i])) {
							i++;
						}
					} else {
						i = array.length - 1;
						while (i >= 0 && !isRulePart(array[i])) {
							i--;
						}
					}
					if (i < 0 || i >= array.length) {
						throw new EvaluationException(methodName + "() cannot be used in rules where all symbols are optionals");
					}
					return new ActionSymbol(grammar, array[i].getTarget(), array[i], false, getRightOffset(i, array),
							evaluationStrategy, rootContext, templatePackage);
				}
			}
			return super.callMethod(methodName, args);
		}

		private boolean isRulePart(SymbolRef ref) {
			return !ref.isHidden() && ref.getTarget() != null && ref.getTarget().getKind() != Symbol.KIND_LAYOUT;
		}

		private int getRightOffset(int index, SymbolRef[] right) {
			assert index >= 0 && index < right.length;
			int rightOffset = 0;
			for (int e = right.length - 1; e > index; e--) {
				if (isRulePart(right[e])) {
					rightOffset++;
				}
			}
			return rightOffset;
		}

		@Override
		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof Integer) {
				int i = (Integer) index;
				SymbolRef[] array = rule.getRight();
				return new ActionSymbol(grammar, array[i].getTarget(), array[i], false, getRightOffset(i, array),
						evaluationStrategy, rootContext, templatePackage);
			} else if (index instanceof String) {
				return grammar.getAnnotation(rule, (String) index);
			} else {
				throw new EvaluationException(
						"index object should be integer (right part index) or string (annotation name)");
			}
		}

		@Override
		public Object getProperty(String id) throws EvaluationException {
			ArrayList<ActionSymbol> result = new ArrayList<ActionSymbol>();

			int rightOffset = 0;
			SymbolRef[] right = rule.getRight();
			for (int i = right.length - 1; i >= 0; i--) {
				Symbol sym = right[i].getTarget();
				if (sym == null || sym.getKind() == Symbol.KIND_LAYOUT) {
					continue;
				}
				String name = sym.getName();
				if (right[i].getAlias() != null) {
					name = right[i].getAlias();
				}
				if (id.equals(name)) {
					result.add(new ActionSymbol(grammar, sym, right[i], false, rightOffset,
							evaluationStrategy, rootContext, templatePackage));
				}
				if (isRulePart(right[i])) {
					rightOffset++;
				}
			}

			if (rule.getLeft().getName().equals(id)) {
				result.add(new ActionSymbol(grammar, rule.getLeft(), null, true, 0, evaluationStrategy, rootContext, templatePackage));
			}

			if (result.size() == 1) {
				return result.get(0);
			} else if (result.size() > 1) {
				Collections.reverse(result);
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

		@Override
		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof String) {
				return grammar.getAnnotation(sym, (String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}

		@Override
		public Object getProperty(String propertyName) throws EvaluationException {
			if ("id".equals(propertyName)) {
				return grammar.getId(sym);
			}
			return super.getProperty(propertyName);
		}
	}

	private final class SymbolRefIxObject extends DefaultJavaIxObject {

		private final SymbolRef sym;

		private SymbolRefIxObject(SymbolRef sym) {
			super(sym);
			this.sym = sym;
		}

		@Override
		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof String) {
				return grammar.getAnnotation(sym, (String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}
	}

	private final class LapgGrammarIxObject extends DefaultJavaIxObject {

		private final GrammarIxObject grammarIxObject;

		private LapgGrammarIxObject(LapgGrammar grammar) {
			super(grammar);
			grammarIxObject = new GrammarIxObject(grammar.getGrammar());
		}

		@Override
		public Object getProperty(String id) throws EvaluationException {
			if ("templates".equals(id) || "hasErrors".equals(id) || "options".equals(id) || "copyrightHeader".equals(id)) {
				return super.getProperty(id);
			} else {
				return grammarIxObject.getProperty(id);
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

		@Override
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

		@Override
		public Iterator<Rule> iterator() {
			return new Iterator<Rule>() {
				int index = 0;

				@Override
				public boolean hasNext() {
					return index < myRules.length;
				}

				@Override
				public Rule next() {
					return index < myRules.length ? myRules[index++] : null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public Object callMethod(String methodName, Object... args) throws EvaluationException {
			if (args.length == 1 && "with".equals(methodName) && args[0] instanceof Symbol) {
				List<Rule> list = getRulesContainingSymbol().get(args[0]);
				return list != null ? list : Collections.emptyList();
			}
			return asObject(myRules).callMethod(methodName, args);
		}

		@Override
		public Object getByIndex(Object index) throws EvaluationException {
			if (index instanceof Symbol) {
				List<Rule> list = getRulesBySymbol().get(index);
				return list != null ? list : Collections.emptyList();
			}
			return asObject(myRules).getByIndex(index);
		}

		@Override
		public Object getProperty(String id) throws EvaluationException {
			return asObject(myRules).getProperty(id);
		}

		@Override
		public Iterator asSequence() {
			return iterator();
		}
	}
}
