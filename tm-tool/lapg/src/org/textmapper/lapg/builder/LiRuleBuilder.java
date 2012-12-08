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
import org.textmapper.lapg.api.rule.*;

import java.util.*;

/**
 * evgeny, 14.12.11
 */
class LiRuleBuilder implements RuleBuilder {

	private LiGrammarBuilder parent;
	private final Nonterminal left;
	private final String alias;
	private final SourceElement origin;
	private Symbol priority;
	private List<LiRhsPart> parts = new ArrayList<LiRhsPart>();
	private Set<RhsPart> mine = new HashSet<RhsPart>();

	LiRuleBuilder(LiGrammarBuilder parent, Nonterminal left, String alias, SourceElement origin) {
		this.parent = parent;
		this.left = left;
		this.alias = alias;
		this.origin = origin;
	}

	@Override
	public RhsSymbol symbol(String alias, Symbol sym, Collection<Terminal> unwanted, SourceElement origin) {
		parent.check(sym);
		NegativeLookahead nla = null;
		if (unwanted != null && unwanted.size() > 0) {
			for (Terminal u : unwanted) {
				parent.check(u);
			}
			nla = new LiNegativeLookahead(unwanted.toArray(new Terminal[unwanted.size()]));
		}
		LiRhsSymbol result = new LiRhsSymbol(sym, alias, nla, origin);
		mine.add(result);
		return result;
	}

	@Override
	public RhsChoice choice(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			check(p);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsChoice choice = new LiRhsChoice(liparts, origin);
		mine.add(choice);
		return choice;
	}

	@Override
	public RhsSequence sequence(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			check(p);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsSequence seq = new LiRhsSequence(liparts, origin);
		mine.add(seq);
		return seq;
	}

	@Override
	public RhsUnordered unordered(Collection<RhsPart> parts, SourceElement origin) {
		LiRhsPart[] liparts = new LiRhsPart[parts.size()];
		int index = 0;
		for (RhsPart p : parts) {
			check(p);
			liparts[index++] = (LiRhsPart) p;
		}
		LiRhsUnordered unordered = new LiRhsUnordered(liparts, origin);
		mine.add(unordered);
		return unordered;
	}

	@Override
	public RhsOptional optional(RhsPart inner, SourceElement origin) {
		check(inner);
		LiRhsOptional opt = new LiRhsOptional((LiRhsPart) inner, origin);
		mine.add(opt);
		return opt;
	}

	@Override
	public void addPart(RhsPart part) {
		check(part);
		parts.add((LiRhsPart) part);
	}

	private void check(RhsPart part) {
		if (part == null) {
			throw new NullPointerException();
		}
		if (!mine.contains(part)) {
			throw new IllegalArgumentException("unknown right-hand side entity passed");
		}
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
