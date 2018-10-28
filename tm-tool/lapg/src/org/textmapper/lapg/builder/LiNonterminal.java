/**
 * Copyright 2002-2018 Evgeny Gryaznov
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

import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.RhsRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 10/27/12
 */
public class LiNonterminal extends LiSymbol implements Nonterminal {

	private RhsRoot definition;
	private boolean isNullable;
	private final List<Rule> rules = new ArrayList<>();
	private List<TemplateInstance> instances;
	private boolean isTemplate;

	public LiNonterminal(Name name, String nameHint, SourceElement origin) {
		super(name, nameHint, origin);
	}

	@Override
	public RhsRoot getDefinition() {
		return definition;
	}

	@Override
	public Iterable<Rule> getRules() {
		return rules;
	}

	@Override
	public boolean isTemplate() {
		return isTemplate;
	}

	@Override
	public Nonterminal getTemplate() {
		return origin instanceof LiNonterminal ? (Nonterminal) origin : null;
	}

	@Override
	public boolean isNullable() {
		return isNullable;
	}

	void setDefinition(LiRhsRoot part) {
		if (definition != null) {
			throw new IllegalStateException("non-terminal is sealed");
		}
		this.definition = part;
		part.setLeft(this);
	}

	void addRule(LiRhsPart part) {
		if (definition == null) {
			definition = new LiRootRhsChoice(this);
		} else if (!(definition instanceof LiRootRhsChoice)) {
			throw new IllegalStateException("non-terminal is sealed");
		}
		((LiRootRhsChoice) definition).addRule(part);
	}

	void rewriteDefinition(RhsRoot old, RhsRoot new_) {
		if (old == definition) {
			definition = new_;
			((LiRhsRoot) new_).setLeft(this);
		}
	}

	void addRule(LiRule rule) {
		rules.add(rule);
	}

	void setNullable(boolean nullable) {
		isNullable = nullable;
	}

	int getNumberOfInstances() {
		return instances == null ? 0 : instances.size();
	}

	void addInstance(TemplateInstance instance) {
		if (instances == null) {
			instances = new ArrayList<>();
		}
		instances.add(instance);
	}

	void setTemplate() {
		this.isTemplate = true;
	}
}
