/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.TemplateParameter.Modifier;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsPart.Kind;
import org.textmapper.lapg.common.SetBuilder;
import org.textmapper.lapg.common.SetsClosure;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;

class TemplateInstantiator {

	private final GrammarBuilder builder;
	private TemplateParameter[] params;
	private final Symbol[] symbols;
	private final int terminals;
	private final List<Problem> problems;
	private final int nonterminals;
	private final Map<TemplateParameter, Integer> paramIndex = new HashMap<>();
	private Map<TemplateParameter, Set<Object>> paramValues;
	private Map<Nonterminal, BitSet> paramUsage;

	private final Map<InstanceKey, TemplateInstance> instances = new LinkedHashMap<>();
	private final Queue<TemplateInstance> queue = new LinkedList<>();

	public TemplateInstantiator(GrammarBuilder builder, TemplateParameter[] params,
								Symbol[] symbols, int terminals, List<Problem> problems) {
		this.builder = builder;
		this.params = params;
		this.symbols = symbols;
		this.terminals = terminals;
		this.problems = problems;
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
		paramValues = new HashMap<>();
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
				set = new HashSet<>();
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
			collectDependencies(closure, sb, paramUsage, symbol.getDefinition(), b, true);
			closure.addDependencies(paramUsage[i], b.create(false));
		}
		if (!closure.compute()) {
			throw new IllegalStateException("Internal error");
		}
		this.paramUsage = new HashMap<>();
		for (int i = 0; i < nonterminals; i++) {
			Nonterminal nonterm = (Nonterminal) symbols[i + terminals];
			BitSet set = new BitSet(params.length);
			closure.exportIntoBitset(paramUsage[i], params.length, set);
			this.paramUsage.put(nonterm, set);
		}
	}

	private void collectDirectUsage(RhsPart part, SetBuilder result) {
		part = RhsUtil.unwrapEx(part, true, true, true);
		if (part instanceof RhsSymbol) {
			TemplateParameter param = ((RhsSymbol) part).getTemplateTarget();
			if (param != null) {
				result.add(paramIndex.get(param));
			}

			RhsArgument[] args = ((RhsSymbol) part).getArgs();
			if (args == null) return;

			for (RhsArgument arg : args) {
				if (arg.getSource() != null && arg.getSource() != arg.getParameter()) {
					result.add(paramIndex.get(arg.getSource()));
				}
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
									 IntArrayBuilder result,
									 boolean leftmost) {
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
				//   List<X> : '(' ( X separator ',')+ ')';
				//   AA : List<A> ;
				//   BB : List<B> ;
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
					if (arg.getSource() != arg.getParameter()) {
						sb.add(paramIndex.get(arg.getParameter()));
					}
				}
			}
			for (TemplateParameter globalParam : params) {
				if (globalParam.getModifier() == Modifier.Lookahead && !leftmost) {
					sb.add(paramIndex.get(globalParam));
				}
			}

			int[] paramSet = sb.create();
			if (paramSet.length > 0) {
				node = closure.addIntersection(new int[]{
						node,
						closure.complement(closure.addSet(paramSet, null), null)
				}, null);
			}

			result.add(node);
		}

		final Iterable<RhsPart> children = RhsUtil.getChildren(p);
		if (children == null) return;

		switch (p.getKind()) {
			case Sequence:
			case Choice:
			case Assignment:
			case Conditional:
			case Cast:
				break;
			default:
				leftmost = false;
		}

		for (RhsPart child : children) {
			collectDependencies(closure, sb, nontermNode, child, result, leftmost);
			if (child.getKind() == Kind.StateMarker) continue;
			if (p.getKind() == Kind.Choice) continue;
			leftmost = false;
		}
	}

	private TemplateEnvironment applyArguments(TemplateEnvironment sourceEnv,
											   Nonterminal target, RhsArgument[] args,
											   boolean fwdAll, boolean leftmost) {
		final BitSet acceptedParameters = paramUsage.get(target);

		// Remove non-global & unused parameters.
		TemplateEnvironment env = sourceEnv.filter(
				parameter -> (fwdAll || parameter.getModifier() == Modifier.Global ||
						leftmost && parameter.getModifier() == Modifier.Lookahead)
						&& acceptedParameters.get(paramIndex.get(parameter)));

		if (args == null) return env;

		// Add arguments.
		for (RhsArgument arg : args) {
			int index = paramIndex.get(arg.getParameter());
			if (!acceptedParameters.get(index)) {

				problems.add(new LiProblem(arg, arg.getParameter().getNameText()
						+ " is not used in " + target.getNameText()));
				continue;
			}
			TemplateParameter source = arg.getSource();
			Object val = source != null ? sourceEnv.getValue(source) : arg.getValue();
			env = env.extend(arg.getParameter(), val);
		}
		return env;
	}

	private void instantiateRef(TemplateInstance context, TemplatedSymbolRef ref,
								Symbol target, RhsArgument[] args,
								boolean fwdAll, boolean leftmost) {
		if (!(target instanceof Nonterminal)) {
			if (!target.isTerm()) {
				throw new UnsupportedOperationException();
			}

			context.addTerminalTarget(ref, (Terminal) target);
			return;
		}

		Nonterminal nonterm = (Nonterminal) target;
		TemplateEnvironment env = applyArguments(
				context.getEnvironment(), nonterm, args, fwdAll, leftmost);
		TemplateInstance instance = instantiate(nonterm, env, ref);
		context.addNonterminalTarget(ref, instance);
	}

	private void instantiatePart(TemplateInstance context, RhsPart p, boolean leftmost) {
		p = RhsUtil.unwrapEx(p, true, false /* cast */, true);
		if (p instanceof RhsSymbol) {
			Symbol target = ((RhsSymbol) p).getTarget();
			if (target == null) {
				Object value = context.getEnvironment().getValue(
						((RhsSymbol) p).getTemplateTarget());
				if (!(value instanceof Symbol)) {
					// TODO report instantiation trace
					problems.add(new LiProblem(p, "Template parameter is unset."));
					return;
				}
				target = (Symbol) value;
			}
			instantiateRef(context, (LiRhsSymbol) p, target,
					((RhsSymbol) p).getArgs(), ((RhsSymbol) p).isFwdAll(), leftmost);
			return;
		}
		if (p instanceof RhsCast) {
			instantiateRef(context, (LiRhsCast) p, ((RhsCast) p).getTarget(),
					((RhsCast) p).getArgs(), false, leftmost);
		} else if (p instanceof RhsSet) {
			if (context.getEnvironment().hasLookahead()) {
				problems.add(new LiProblem(p,
						"Cannot instantiate sets with lookahead parameters."));
				return;
			}
			switch (((RhsSet) p).getOperation()) {
				case Any:
				case First:
				case Last:
				case Precede:
				case Follow:
					instantiateRef(context, (LiRhsSet) p, ((RhsSet) p).getSymbol(),
							((RhsSet) p).getArgs(), false /*fwdAll*/, false /*leftmost*/);
					break;
				case Complement:
				case Intersection:
				case Union:
					for (RhsPart child : ((RhsSet) p).getSets()) {
						instantiatePart(context, child, false /*leftmost*/);
					}
					break;
			}
		} else if (p instanceof RhsConditional) {
			context.getTemplate().setTemplate();
		} else if (leftmost && p instanceof RhsList && context.getEnvironment().hasLookahead()) {
			if (((RhsList) p).isRightRecursive()) {
				problems.add(new LiProblem(p, "Cannot instantiate right recursive lists with " +
						"lookahead parameters."));
				return;
			}
			// TODO
			problems.add(new LiProblem(p, "(not implemented) Cannot instantiate lists with " +
					"lookahead parameters."));
			return;

		} else if (leftmost && p instanceof RhsOptional &&
				context.getEnvironment().hasLookahead()) {
			problems.add(new LiProblem(p, "Cannot instantiate ()? with lookahead parameters."));
			return;
		}
		final Iterable<RhsPart> children = RhsUtil.getChildren(p);
		if (children == null) return;

		switch (p.getKind()) {
			case Sequence:
			case Choice:
			case Assignment:
			case Conditional:
			case Cast:
				break;
			default:
				leftmost = false;
		}

		for (RhsPart child : children) {
			instantiatePart(context, child, leftmost);
			if (child.getKind() == Kind.StateMarker) continue;
			if (p.getKind() == Kind.Choice) continue;
			leftmost = false;
		}
	}

	private TemplateInstance instantiate(Nonterminal nonterm, TemplateEnvironment env,
										 SourceElement referrer) {
		InstanceKey key = new InstanceKey(nonterm, env);
		TemplateInstance instance = instances.get(key);
		if (instance == null) {
			instance = new TemplateInstance((LiNonterminal) nonterm, env, builder, referrer);
			instances.put(key, instance);
			queue.add(instance);
		}
		return instance;
	}

	void instantiate(Collection<? extends InputRef> refs) {
		if (params.length == 0) return;

		collectParameterValues();
		computeParametersUsage();

		TemplateEnvironment root = builder.getRootEnvironment();
		for (InputRef ref : refs) {
			instantiate(ref.getTarget(), root, ref);
		}
		TemplateInstance instance;
		while ((instance = queue.poll()) != null) {
			instantiatePart(instance, instance.getTemplate().getDefinition(), true);
		}
		for (int i = 0; i < nonterminals; i++) {
			LiNonterminal nonterm = (LiNonterminal) symbols[i + terminals];
			int num = nonterm.getNumberOfInstances();
			if (num > 1) {
				nonterm.setTemplate();
			} else if (num == 0) {
				nonterm.setUnused();
			}
		}
		if (!problems.isEmpty()) return;

		instances.values().forEach(TemplateInstance::allocate);
		instances.values().forEach(TemplateInstance::updateNameHint);
	}

	public void updateSets(Collection<NamedSet> elements) {
		if (params.length == 0) return;

		elements.forEach(e -> updateSet(e.getSet()));
	}

	public void updateSet(RhsSet set) {
		switch (set.getOperation()) {
			case Any:
			case First:
			case Last:
			case Precede:
			case Follow:
				if (set.getSymbol().isTerm()) {
					break;
				}
				Nonterminal nonterm = (Nonterminal) set.getSymbol();
				TemplateEnvironment env = applyArguments(
						builder.getRootEnvironment(), nonterm, set.getArgs(), false /*fwdAll*/, false /*leftmost*/);
				InstanceKey key = new InstanceKey(nonterm, env);
				TemplateInstance instance = instances.get(key);
				if (instance == null) {
					problems.add(new LiProblem(set, "Symbol is not used in the grammar with given arguments."));
					break;
				}
				((LiRhsSet)set).setResolvedSymbol(instance.getOrCreateNonterminal());
				break;
			case Complement:
			case Intersection:
			case Union:
				for (RhsSet child : set.getSets()) {
					updateSet(child);
				}
				break;
		}
	}
}
