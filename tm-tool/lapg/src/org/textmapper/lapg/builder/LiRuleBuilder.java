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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * evgeny, 14.12.11
 */
class LiRuleBuilder implements RuleBuilder {

	private LiGrammarBuilder parent;
	private final Symbol left;
	private final String alias;
	private final SourceElement origin;
	private Symbol priority;
	private List<SymbolRef> right = new ArrayList<SymbolRef>();

	LiRuleBuilder(LiGrammarBuilder parent, Symbol left, String alias, SourceElement origin) {
		this.parent = parent;
		this.left = left;
		this.alias = alias;
		this.origin = origin;
	}

	@Override
	public SymbolRef addPart(String alias, Symbol sym, Collection<Symbol> unwanted, SourceElement origin) {
		parent.check(sym);
		NegativeLookahead nla = null;
		if (unwanted != null && unwanted.size() > 0) {
			for (Symbol u : unwanted) {
				parent.check(u);
				if (!u.isTerm()) {
					throw new IllegalArgumentException("negative lookahead should contain terminals only");
				}
			}
			nla = new LiNegativeLookahead(unwanted.toArray(new Symbol[unwanted.size()]));
		}
		LiSymbolRef ref = new LiSymbolRef(sym, alias, nla, origin);
		right.add(ref);
		return ref;
	}

	@Override
	public Rule create() {
		return parent.addRule(alias, left, right.toArray(new SymbolRef[right.size()]), priority, origin);
	}

	@Override
	public RuleBuilder copy() {
		LiRuleBuilder res = new LiRuleBuilder(parent, left, alias, origin);
		res.priority = priority;
		for (SymbolRef r : right) {
			res.right.add(r);
		}
		return res;
	}

	@Override
	public void setPriority(Symbol sym) {
		if (priority != null) {
			throw new IllegalStateException("redeclaring rule priority");
		}
		parent.check(sym);
		if (!sym.isTerm()) {
			throw new IllegalArgumentException("symbol `" + sym.getName() + "' is not a terminal");
		}
		priority = sym;
	}
}
