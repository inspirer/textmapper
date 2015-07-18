/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.rule.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A template instantiation for a particular environment.
 */
class TemplateInstance {

	private Nonterminal instance;
	private final LiNonterminal template;
	private final TemplateEnvironment environment;
	private final GrammarBuilder builder;
	private final SourceElement referrer;
	private Map<TemplatedSymbolRef, TemplateInstance> targetInstance;
	private Map<TemplatedSymbolRef, Terminal> terminals;

	TemplateInstance(LiNonterminal template, TemplateEnvironment environment,
					 GrammarBuilder builder, SourceElement referrer) {
		this.template = template;
		this.environment = environment;
		this.builder = builder;
		this.referrer = referrer;
		template.addInstance(this);
	}

	LiNonterminal getTemplate() {
		return template;
	}

	TemplateEnvironment getEnvironment() {
		return environment;
	}

	SourceElement getReferrer() {
		return referrer;
	}

	void addNonterminalTarget(TemplatedSymbolRef ref, TemplateInstance target) {
		if (targetInstance == null) {
			targetInstance = new HashMap<TemplatedSymbolRef, TemplateInstance>();
		}
		targetInstance.put(ref, target);
	}

	void addTerminalTarget(TemplatedSymbolRef ref, Terminal target) {
		if (terminals == null) {
			terminals = new HashMap<TemplatedSymbolRef, Terminal>();
		}
		terminals.put(ref, target);
	}

	Nonterminal getOrCreateNonterminal() {
		if (instance != null) return instance;

		if (template.isTemplate()) {
			String nameHint = LiUtil.getSymbolName(template) +
					(template.getNumberOfInstances() > 1 ? environment.getNonterminalSuffix() : "");
			instance = builder.addAnonymous(nameHint, template);
		} else {
			instance = template;
		}
		return instance;
	}

	private Symbol resolveSymbol(TemplatedSymbolRef ref) {
		if (targetInstance != null) {
			TemplateInstance instance = targetInstance.get(ref);
			if (instance != null) return instance.getOrCreateNonterminal();
		}
		if (terminals != null) {
			Terminal terminal = terminals.get(ref);
			if (terminal != null) return terminal;
		}
		if (ref instanceof RhsCast) return ((RhsCast) ref).getTarget();
		if (ref instanceof RhsSymbol) return ((RhsSymbol) ref).getTarget();
		if (ref instanceof RhsSet) return ((RhsSet) ref).getSymbol();
		throw new IllegalStateException("something went wrong");
	}

	private void updateExistingNonterminal() {
		if (targetInstance != null) {
			for (Entry<TemplatedSymbolRef, TemplateInstance> entry : targetInstance.entrySet()) {
				entry.getKey().setResolvedSymbol(entry.getValue().getOrCreateNonterminal());
			}
		}
		if (terminals != null) {
			for (Entry<TemplatedSymbolRef, Terminal> entry : terminals.entrySet()) {
				entry.getKey().setResolvedSymbol(entry.getValue());
			}
		}
	}

	private LiRhsPart[] clone(RhsPart[] parts) {
		LiRhsPart[] result = new LiRhsPart[parts.length];
		int target = 0;
		for (RhsPart p : parts) {
			LiRhsPart val = clone(p);
			if (val != null) {
				result[target++] = val;
			}
		}
		if (target < parts.length) result = Arrays.copyOf(result, target);
		return result;
	}

	private LiRhsSet cloneSet(LiRhsSet set) {
		LiRhsSet[] children = null;
		LiRhsSet[] nested = set.getSets();
		if (nested != null) {
			children = new LiRhsSet[nested.length];
			for (int i = 0; i < nested.length; i++) {
				children[i] = cloneSet(nested[i]);
			}
		}
		return new LiRhsSet(set.getOperation(), resolveSymbol(set), null /* args */, children, set);
	}

	private LiRhsSequence cloneSeq(RhsSequence p) {
		if (p == null) return null;
		return new LiRhsSequence(p.getName(), clone(p.getParts()), false, p);
	}

	private LiRhsPart clone(RhsPart p) {
		if (p == null) return null;
		switch (p.getKind()) {
			case Choice:
				// TODO error if evaluates to an empty list
				if (p instanceof LiRootRhsChoice) {
					LiRootRhsChoice target = new LiRootRhsChoice(instance);
					for (LiRhsPart source : ((LiRootRhsChoice) p).getParts()) {
						LiRhsPart copy = clone(source);
						if (copy == null) continue;
						target.addRule(copy);
					}
					return target;
				} else {
					return new LiRhsChoice(clone(((RhsChoice) p).getParts()), false, p);
				}
			case Optional:
				return new LiRhsOptional(clone(((RhsOptional) p).getPart()), p);
			case Sequence:
				return cloneSeq((RhsSequence) p);
			case Symbol:
				return new LiRhsSymbol(resolveSymbol((TemplatedSymbolRef) p), null /* args */, p);
			case Unordered:
				return new LiRhsUnordered(clone(((LiRhsUnordered) p).getParts()), p);
			case Assignment: {
				RhsAssignment source = (RhsAssignment) p;
				return new LiRhsAssignment(source.getName(), clone(source.getPart()), source.isAddition(), p);
			}
			case List: {
				LiRhsList list = (LiRhsList) p;
				return new LiRhsList(cloneSeq(list.getElement()), clone(list.getSeparator()), list.isNonEmpty(),
						cloneSeq(list.getCustomInitialElement()), list.isRightRecursive(), false, p);
			}
			case Cast: {
				LiRhsCast cast = (LiRhsCast) p;
				return new LiRhsCast(resolveSymbol(cast), null /* args */, clone(cast.getPart()), p);
			}
			case Ignored:
				// TODO implement
				throw new UnsupportedOperationException();
			case Conditional: {
				boolean value = ((RhsConditional) p).getPredicate().apply(environment);
				return value ? clone(((RhsConditional) p).getInner()) : null;
			}
			case Set:
				return cloneSet((LiRhsSet) p);
		}
		throw new IllegalStateException("unknown part kind");
	}

	void allocate() {
		getOrCreateNonterminal();
		if (template.isTemplate()) {
			((LiNonterminal) instance).setDefinition((LiRhsRoot) clone(template.getDefinition()));
		} else {
			updateExistingNonterminal();
		}
	}

	void updateNameHint() {
		Nonterminal instance = this.instance != null ? this.instance : this.template;
		if (instance.getName() == null) {
			String hint = instance.getDefinition().getProvisionalName();
			if (hint != null) {
				instance.putUserData(Nonterminal.UD_NAME_HINT, hint);
			}
		}
	}
}
