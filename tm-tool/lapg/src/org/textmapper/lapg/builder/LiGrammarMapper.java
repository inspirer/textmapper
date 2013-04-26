/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.ast.AstEnumMember;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.ast.AstList;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class LiGrammarMapper implements GrammarMapper {

	protected final Set<Symbol> symbolsSet = new HashSet<Symbol>();

	LiGrammarMapper(Grammar grammar) {
		if (grammar != null) {
			symbolsSet.addAll(Arrays.asList(grammar.getSymbols()));
		}
	}

	void check(RhsPart part, boolean asChild) {
		if (part == null || part.getLeft() == null) {
			throw new NullPointerException();
		}
		if (!symbolsSet.contains(part.getLeft())) {
			throw new IllegalArgumentException("unknown right-hand side element passed");
		}
	}

	final void check(Symbol sym) {
		if (sym == null) {
			throw new NullPointerException();
		}
		if (!symbolsSet.contains(sym)) {
			throw new IllegalArgumentException("unknown symbol passed");
		}
	}

	@Override
	public void map(Nonterminal symbol, AstType type) {
		check(symbol);
		if (symbol.getType() != null) {
			throw new IllegalArgumentException("cannot re-map symbol");
		}
		((LiSymbol) symbol).setType(type);
	}

	@Override
	public void map(Rule rule, AstType type) {
		if (rule == null) {
			throw new NullPointerException();
		}
		final Nonterminal left = rule.getLeft();
		check(left);
		final AstType leftType = left.getType();
		if (leftType == null) {
			throw new IllegalArgumentException("map nonterminal first");
		}
		if (!type.isSubtypeOf(leftType)) {
			throw new IllegalArgumentException("rule type should be a subtype of its non-terminal");
		}
		if (rule.getMapping() != null) {
			throw new IllegalArgumentException("cannot re-map rule");
		}
		((LiRule) rule).setMapping(type);
	}

	@Override
	public void map(RhsSymbol symbol, AstField field, AstEnumMember value, boolean isAddition) {
		check(symbol, false);
		final AstType nontermType = symbol.getLeft().getType();
		if (nontermType == null) {
			throw new IllegalArgumentException("cannot map symbol, map nonterminal first");
		}
		if (field != null && nontermType != field.getContainingClass()) {
			throw new IllegalArgumentException("field should be from the nonterminal class");
		}
		AstType type = field != null ? field.getType() : nontermType;
		if (isAddition) {
			if (!(type instanceof AstList)) {
				throw new IllegalArgumentException("addition is applicable only to list types");
			}
			type = ((AstList) type).getInner();
		}
		if (value != null && type != value.getContainingEnum()) {
			throw new IllegalArgumentException(
					"enumeration value should match " + (field != null
							? "the field type"
							: "the nonterminal type"));
		}
		((LiRhsSymbol) symbol).setMapping(new LiRhsMapping(field, value, isAddition));
	}
}
