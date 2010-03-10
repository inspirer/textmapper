/*************************************************************
 * Copyright (c) 2002-2009 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 *************************************************************/
package net.sf.lapg.gen;

import java.util.ArrayList;

import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.api.SymbolRef;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.impl.DefaultNavigationFactory;

public class GrammarNavigationFactory extends DefaultNavigationFactory {

	private final String templatePackage;

	public GrammarNavigationFactory(String templatePackage) {
		this.templatePackage = templatePackage;
	}

	@Override
	public INavigationStrategy<?> getStrategy(Object o) {

		if (o instanceof Rule) {
			return ruleNavigation;
		}

		if (o instanceof ActionSymbol) {
			return actSymbolNavigation;
		}

		if (o instanceof Symbol) {
			return symbolNavigation;
		}

		return super.getStrategy(o);
	}

	private final INavigationStrategy<ActionSymbol> actSymbolNavigation = new INavigationStrategy<ActionSymbol>() {

		public Object callMethod(ActionSymbol obj, String methodName, Object[] args) throws EvaluationException {
			throw new EvaluationException("do not know method");
		}

		public Object getByIndex(ActionSymbol obj, Object index) throws EvaluationException {
			if (index instanceof String && obj.ref != null) {
				return obj.ref.getAnnotation((String) index);
			}
			throw new EvaluationException("do not know index");
		}

		public Object getProperty(ActionSymbol obj, String id) throws EvaluationException {
			if (id.equals("symbol")) {
				return obj.symbol;
			}
			if (id.equals("isLeft")) {
				return obj.isLeft;
			}
			if (id.equals("rightOffset")) {
				return obj.rightOffset;
			}
			ITemplate templ = (ITemplate) evaluationStrategy.loadEntity(templatePackage + ".sym_" + id,
					IBundleEntity.KIND_TEMPLATE, null);
			return evaluationStrategy.evaluate(templ, new EvaluationContext(obj), null, null);
		}
	};

	private final INavigationStrategy<Rule> ruleNavigation = new INavigationStrategy<Rule>() {

		public Object callMethod(Rule rule, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(rule, methodName, args);
		}

		public Object getByIndex(Rule rule, Object index) throws EvaluationException {
			if (index instanceof Integer) {
				int i = (Integer) index;
				SymbolRef[] array = rule.getRight();
				return new ActionSymbol(array[i].getTarget(), array[i], false, array.length - i - 1,
						evaluationStrategy, templatePackage);
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
				result.add(new ActionSymbol(rule.getLeft(), null, true, 0, evaluationStrategy, templatePackage));
			}

			SymbolRef[] right = rule.getRight();
			for (int i = 0; i < right.length; i++) {
				String name = right[i].getTarget().getName();
				if (right[i].getAlias() != null) {
					name = right[i].getAlias();
				}
				if (id.equals(name)) {
					result.add(new ActionSymbol(right[i].getTarget(), right[i], false, right.length - i - 1,
							evaluationStrategy, templatePackage));
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

		@Override
		public Object callMethod(Symbol obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		@Override
		public Object getByIndex(Symbol sym, Object index) throws EvaluationException {
			if (index instanceof String) {
				return sym.getAnnotation((String) index);
			} else {
				throw new EvaluationException("index object should be string (annotation name)");
			}
		}

		@Override
		public Object getProperty(Symbol obj, String propertyName) throws EvaluationException {
			return javaNavigation.getProperty(obj, propertyName);
		}
	};
}
