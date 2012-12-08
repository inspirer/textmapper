/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
import org.textmapper.lapg.api.builder.RuleBuilder;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * evgeny, 14.12.11
 */
class LiRuleBuilder implements RuleBuilder {

	private final LiGrammarBuilder parent;
	private final Object token = new Object();
	private final Nonterminal left;
	private final String alias;
	private final SourceElement origin;
	private Symbol priority;
	private List<LiRhsPart> parts = new ArrayList<LiRhsPart>();

	LiRuleBuilder(LiGrammarBuilder parent, Nonterminal left, String alias, SourceElement origin) {
		this.parent = parent;
		this.left = left;
		this.alias = alias;
		this.origin = origin;
	}

	@Override
	public void addPart(RhsPart part) {
		parent.check(part);
		((LiRhsPart) part).attach(token);
		parts.add((LiRhsPart) part);
	}

	@Override
	public void setPriority(Terminal sym) {
		if (priority != null) {
			throw new IllegalStateException("re-declaring rule priority");
		}
		parent.check(sym);
		priority = sym;
	}

	@Override
	public Collection<Rule> create() {
		List<RhsSymbol[]> rules = LiRhsSequence.expandList(parts.toArray(new LiRhsPart[parts.size()]));
		List<Rule> result = new ArrayList<Rule>(rules.size());
		for (RhsSymbol[] rhs : rules) {
			result.add(parent.addRule(alias, left, rhs, priority, origin));
		}
		return result;
	}
}
