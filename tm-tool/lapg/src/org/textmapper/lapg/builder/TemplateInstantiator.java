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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.InputRef;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;

class TemplateInstantiator {

	private LiTemplateParameter[] params;
	private final LiSymbol[] symbols;
	private final int terminals;
	private final int nonterminals;
	private final Map<TemplateParameter, Integer> paramIndex = new HashMap<TemplateParameter, Integer>();
	private Map<TemplateParameter, Set<Object>> paramValues;

	public TemplateInstantiator(LiTemplateParameter[] params, LiSymbol[] symbols, int terminals) {
		this.params = params;
		this.symbols = symbols;
		this.terminals = terminals;
		this.nonterminals = symbols.length - terminals;
		int index = 0;
		for (TemplateParameter p : params) {
			paramIndex.put(p, index++);
		}
	}

	/**
	 * Collects possible values for all parameters in {@link #paramValues}.
	 */
	private void collectParameterValues() {
		paramValues = new HashMap<TemplateParameter, Set<Object>>();
		for (int i = 0; i < nonterminals; i++) {
			LiNonterminal nonterm = (LiNonterminal) symbols[i + terminals];
			collectParameterValues(nonterm.getDefinition());
		}
	}

	private void collectParameterValues(RhsPart p) {
		p = RhsUtil.unwrapEx(p, true, false /* cast */, true);
		if (p instanceof RhsSymbol) {
			collectParameterValuesInArgs(((RhsSymbol) p).getArgs());
			return;
		}
		if (p instanceof RhsCast) {
			collectParameterValuesInArgs(((RhsCast) p).getArgs());
		} else if (p instanceof RhsSet) {
			collectParameterValuesInArgs(((RhsSet) p).getArgs());
		}
		final Iterable<RhsPart> children = RhsUtil.getChildren(p);
		if (children == null) return;

		for (RhsPart child : children) {
			collectParameterValues(child);
		}
	}

	private void collectParameterValuesInArgs(RhsArgument[] args) {
		if (args == null) return;

		for (RhsArgument arg : args) {
			TemplateParameter param = arg.getParameter();
			Set<Object> set = paramValues.get(param);
			if (set == null) {
				set = new HashSet<Object>();
				paramValues.put(param, set);
			}
			set.add(arg.getValue());
		}
	}

	/**
	 * For every nonterminal computes a set of template parameters it depends on.
	 */
	void computeParametersUsage() {
		SetsClosure closure = new SetsClosure();
		SetBuilder sb = new SetBuilder(Math.max(nonterminals, params.length));
		int[] paramUsage = new int[nonterminals];
		for (int i = 0; i < nonterminals; i++) {
			LiNonterminal symbol = (LiNonterminal) symbols[i + terminals];
			collectDirectUsage(symbol.getDefinition(), sb);
			paramUsage[i] = closure.addSet(sb.create(), symbol);
		}

		IntArrayBuilder b = new IntArrayBuilder(true);
		for (int i = 0; i < nonterminals; i++) {
			LiNonterminal symbol = (LiNonterminal) symbols[i + terminals];
			collectDependencies(closure, sb, paramUsage, symbol.getDefinition(), b);
			closure.addDependencies(paramUsage[i], b.create(false));
		}
		if (!closure.compute()) {
			throw new IllegalStateException("Internal error");
		}
	}

	private void collectDirectUsage(RhsPart part, SetBuilder result) {
		part = RhsUtil.unwrapEx(part, true, true, true);
		if (part instanceof RhsSymbol) {
			TemplateParameter param = ((RhsSymbol) part).getTemplateTarget();
			if (param != null) {
				int index = paramIndex.get(param);
				result.add(index);
			}
			return;
		} else if (part instanceof RhsConditional) {
			collectDirectUsage(((RhsConditional) part).getPredicate(), result);
		}
		final Iterable<RhsPart> children = RhsUtil.getChildren(part);
		if (children == null) return;

		for (RhsPart child : children) {
			collectDirectUsage(child, result);
		}
	}

	private void collectDirectUsage(RhsPredicate p, SetBuilder result) {
		TemplateParameter param = p.getParameter();
		if (param != null) {
			int index = paramIndex.get(param);
			result.add(index);
		}

		final RhsPredicate[] children = p.getChildren();
		if (children == null) return;

		for (RhsPredicate child : children) {
			collectDirectUsage(child, result);
		}
	}

	private void collectDependencies(SetsClosure closure, SetBuilder sb,
									 int[] nontermNode, RhsPart p,
									 IntArrayBuilder result) {
		p = RhsUtil.unwrapEx(p, true, false /* cast */, true);

		TemplateParameter param = null;
		Symbol symbol = null;
		RhsArgument[] args = null;

		if (p instanceof RhsSymbol) {
			RhsSymbol symref = (RhsSymbol) p;
			param = symref.getTemplateTarget();
			symbol = symref.getTarget();
			args = symref.getArgs();
		} else if (p instanceof RhsCast) {
			symbol = ((RhsCast) p).getTarget();
			args = ((RhsCast) p).getArgs();
		} else if (p instanceof RhsSet) {
			symbol = ((RhsSet) p).getSymbol();
			args = ((RhsSet) p).getArgs();
		}

		if (symbol instanceof Nonterminal || param != null && paramValues.containsKey(param)) {
			int node;
			if (param != null) {
				// Note: currently, we overestimate here to keep the logic simple. Consider:
				//   List<X> ::= '(' ( X separator ',')+ ')';
				//   AA ::= List<A> ;
				//   BB ::= List<B> ;
				// Both AA & BB will depend on a union of A & B.
				Set<Object> values = paramValues.get(param);
				for (Object o : values) {
					Symbol s = (Symbol) o;
					if (!(s instanceof Nonterminal)) continue;

					sb.add(nontermNode[s.getIndex() - terminals]);
				}
				node = closure.addSet(SetsClosure.EMPTY_ARRAY, null);
				closure.addDependencies(node, sb.create());
			} else {
				node = nontermNode[symbol.getIndex() - terminals];
			}

			if (args != null) {
				for (RhsArgument arg : args) {
					sb.add(paramIndex.get(arg.getParameter()));
				}
				int[] paramSet = sb.create();
				if (paramSet.length > 0) {
					node = closure.addIntersection(new int[]{
							node,
							closure.complement(closure.addSet(paramSet, null), null)
					}, null);
				}
			}

			result.add(node);
		}

		final Iterable<RhsPart> children = RhsUtil.getChildren(p);
		if (children == null) return;

		for (RhsPart child : children) {
			collectDependencies(closure, sb, nontermNode, child, result);
		}
	}

	void instantiate(GrammarBuilder builder, Collection<? extends InputRef> refs) {
		collectParameterValues();
		computeParametersUsage();

	}
}
