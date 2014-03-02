/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
package org.textmapper.tool.gen;

import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.LexerRule;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.rule.RhsAssignment;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.lapg.common.RuleUtil;
import org.textmapper.lapg.util.RhsUtil;
import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.objects.DefaultJavaIxObject;
import org.textmapper.templates.objects.IxObject;
import org.textmapper.templates.objects.IxWrapper;
import org.textmapper.templates.objects.JavaIxFactory;
import org.textmapper.tool.compiler.TMDataUtil;
import org.textmapper.tool.compiler.TMGrammar;

import java.util.*;

public class GrammarIxFactory extends JavaIxFactory {

	private final String templatePackage;
	private final EvaluationContext rootContext;
	private IEvaluationStrategy evaluationStrategy;
	private final TMGrammar grammar;

	public GrammarIxFactory(TMGrammar g, String templatePackage, EvaluationContext context) {
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
		if (o instanceof LexerRule) {
			return new LexerRuleIxObject((LexerRule) o);
		}
		if (o instanceof Rule) {
			return new RuleIxObject((Rule) o);
		}
		if (o instanceof Symbol) {
			return new SymbolIxObject((Symbol) o);
		}
		if (o instanceof RhsSymbol) {
			return new RhsSymbolIxObject((RhsSymbol) o);
		}
		if (o instanceof Grammar) {
			return new GrammarIxObject((Grammar) o);
		}
		if (o instanceof TMGrammar) {
			return new TMGrammarIxObject((TMGrammar) o);
		}
		return super.asObject(o);
	}

	private final class LexerRuleIxObject extends DefaultJavaIxObject {
		private final LexerRule lexerRule;

		private LexerRuleIxObject(LexerRule lexerRule) {
			super(lexerRule);
			this.lexerRule = lexerRule;
		}

		@Override
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			if ("action".equals(propertyName)) {
				return TMDataUtil.getCode(lexerRule);
			}
			if ("transitions".equals(propertyName)) {
				return TMDataUtil.getTransition(lexerRule);
			}
			return super.getProperty(caller, propertyName);
		}
	}

	private final class RuleIxObject extends DefaultJavaIxObject {

		private final Rule rule;
		private RhsSymbol[] sourceSymbols;

		private RuleIxObject(Rule rule) {
			super(rule);
			this.rule = rule;
		}

		@Override
		public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
			if (args == null || args.length == 0) {
				if ("getAction".equals(methodName)) {
					return TMDataUtil.getCode(rule);
				}
				if ("left".equals(methodName)) {
					return new ActionSymbol(grammar, rule.getLeft(), null, true, 0, evaluationStrategy, rootContext,
							templatePackage, caller);
				}
				if ("sourceSymbols".equals(methodName)) {
					loadSourceSymbols();
					return sourceSymbols;
				}
				if (methodName.equals("last") || methodName.equals("first")) {
					RhsSymbol[] array = rule.getRight();
					if (array == null || array.length == 0) {
						throw new EvaluationException(methodName + "() cannot be used on empty rule");
					}
					int i = methodName.charAt(0) == 'f' ? 0 : array.length - 1;
					return new ActionSymbol(grammar, array[i].getTarget(), array[i], false, array.length - 1 - i,
							evaluationStrategy, rootContext, templatePackage, caller);
				}
			}
			if (args != null && args.length == 1) {
				if ("mappedSymbols".equals(methodName)) {
					return getMappedSymbols((RhsSequence) args[0], new HashSet<RhsSymbol>(Arrays.asList(rule.getRight())));
				}
				if ("isMatched".equals(methodName)) {
					return isMatched(((RhsSequence) args[0]), new HashSet<RhsSymbol>(Arrays.asList(rule.getRight())));
				}
			}
			return super.callMethod(caller, methodName, args);
		}

		private boolean isMatched(RhsPart part, Set<RhsSymbol> active) {
			for (RhsSymbol o : RhsUtil.getRhsSymbols(part)) {
				if (active.contains(o)) {
					return true;
				}
			}
			return false;

		}

		private void loadSourceSymbols() {
			if (sourceSymbols == null) {
				sourceSymbols = RhsUtil.getRhsSymbols(rule.getSource());
			}
		}

		private RhsPart[] getMappedSymbols(RhsSequence seq, Set<RhsSymbol> active) {
			List<RhsPart> result = new ArrayList<RhsPart>();
			for (RhsPart p : seq.getParts()) {
				collectMappedSymbols(p, result, active);
			}
			return result.toArray(new RhsPart[result.size()]);
		}

		private void collectMappedSymbols(RhsPart part, List<RhsPart> r, Set<RhsSymbol> active) {
			if (part instanceof RhsSymbol) {
				RhsSymbol sym = (RhsSymbol) part;
				RhsSymbol rewrittenTo = TMDataUtil.getRewrittenTo(sym);
				if ((rewrittenTo != null ? rewrittenTo : sym).getMapping() != null) {
					if (active.contains(part)) {
						r.add(part);
					}
					return;
				}
			}

			if (part instanceof RhsSequence && RhsUtil.hasMapping((RhsSequence) part)) {
				if (isMatched(part, active)) {
					r.add(part);
				}
				return;
			}

			Iterable<RhsPart> children = RhsUtil.getChildren(part);
			if (children == null) return;

			for (RhsPart c : children) {
				collectMappedSymbols(c, r, active);
			}
		}


		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
			if (index instanceof Integer) {
				int i = (Integer) index;
				loadSourceSymbols();
				if (i < 0 || i >= sourceSymbols.length) {
					throw new EvaluationException("index is out of range");
				}
				RhsSymbol ref = sourceSymbols[i];

				RhsSymbol[] right = rule.getRight();
				int rightOffset = -1;
				for (i = 0; i < right.length; i++) {
					if (right[i] == ref) {
						rightOffset = right.length - 1 - i;
						break;
					}
				}
				return new ActionSymbol(grammar, ref.getTarget(), ref, false, rightOffset,
						evaluationStrategy, rootContext, templatePackage, caller);
			} else if (index instanceof String) {
				return grammar.getAnnotation(rule, (String) index);
			} else {
				throw new EvaluationException(
						"index object should be integer (right part index) or string (annotation name)");
			}
		}

		@Override
		public Object getProperty(SourceElement caller, String id) throws EvaluationException {
			ActionSymbol result = null;
			Set<RhsSymbol> matching = RuleUtil.getSymbolsByName(id, rule.getSource());
			if (matching == null || matching.isEmpty()) {
				throw new EvaluationException("symbol `" + id + "' is " + (matching == null ? "undefined" : "ambiguous"));
			}

			RhsSymbol[] right = rule.getRight();
			for (int i = 0; i < right.length; i++) {
				if (!matching.contains(right[i])) {
					continue;
				}

				assert result == null : "internal error in RuleUtil.getSymbols()";
				result = new ActionSymbol(grammar, right[i].getTarget(), right[i], false, right.length - 1 - i,
						evaluationStrategy, rootContext, templatePackage, caller);
			}

			if (result != null) {
				return result;
			}

			final RhsSymbol first = matching.iterator().next();
			return new ActionSymbol(grammar, first.getTarget(), first, false, -1, evaluationStrategy,
					rootContext, templatePackage, caller);
		}
	}

	private final class SymbolIxObject extends DefaultJavaIxObject {

		private final Symbol sym;

		private SymbolIxObject(Symbol sym) {
			super(sym);
			this.sym = sym;
		}

		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
			if (index instanceof String) {
				return grammar.getAnnotation(sym, (String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}

		@Override
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			if ("id".equals(propertyName)) {
				return TMDataUtil.getId(sym);
			}
			return super.getProperty(caller, propertyName);
		}
	}

	private final class RhsSymbolIxObject extends DefaultJavaIxObject {

		private final RhsSymbol sym;

		private RhsSymbolIxObject(RhsSymbol sym) {
			super(sym);
			this.sym = sym;
		}

		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
			if (index instanceof String) {
				return grammar.getAnnotation(sym, (String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}

		@Override
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			if ("mapping".equals(propertyName)) {
				RhsSymbol rewrittenTo = TMDataUtil.getRewrittenTo(sym);
				return (rewrittenTo != null ? rewrittenTo : sym).getMapping();
			}

			return super.getProperty(caller, propertyName);
		}
	}

	private final class TMGrammarIxObject extends DefaultJavaIxObject {

		private final GrammarIxObject grammarIxObject;

		private TMGrammarIxObject(TMGrammar grammar) {
			super(grammar);
			grammarIxObject = new GrammarIxObject(grammar.getGrammar());
		}

		@Override
		public Object getProperty(SourceElement caller, String id) throws EvaluationException {
			if ("templates".equals(id) || "hasErrors".equals(id) || "options".equals(id) || "copyrightHeader".equals(id)) {
				return super.getProperty(caller, id);
			} else {
				return grammarIxObject.getProperty(caller, id);
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
		public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
			if ("rules".equals(propertyName)) {
				GrammarRules gr = rules.get(grammar);
				if (gr == null) {
					gr = new GrammarRules(grammar);
					rules.put(grammar, gr);
				}
				return gr;
			}
			if ("lexerRuleTokens".equals(propertyName)) {
				LexerRule[] lexerRules = grammar.getLexerRules();
				int[] result = new int[lexerRules.length];
				for (int i = 0; i < lexerRules.length; i++) {
					result[i] = lexerRules[i].getSymbol().getIndex();
				}
				return result;
			}
			return super.getProperty(caller, propertyName);
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
				for (RhsSymbol sref : r.getRight()) {
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
		public Object callMethod(SourceElement caller, String methodName, Object... args) throws EvaluationException {
			if (args.length == 1 && "with".equals(methodName) && args[0] instanceof Symbol) {
				List<Rule> list = getRulesContainingSymbol().get(args[0]);
				return list != null ? list : Collections.emptyList();
			}
			return asObject(myRules).callMethod(caller, methodName, args);
		}

		@Override
		public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
			if (index instanceof Symbol) {
				List<Rule> list = getRulesBySymbol().get(index);
				return list != null ? list : Collections.emptyList();
			}
			return asObject(myRules).getByIndex(caller, index);
		}

		@Override
		public Object getProperty(SourceElement caller, String id) throws EvaluationException {
			return asObject(myRules).getProperty(caller, id);
		}

		@Override
		public Iterator asSequence() {
			return iterator();
		}
	}
}
