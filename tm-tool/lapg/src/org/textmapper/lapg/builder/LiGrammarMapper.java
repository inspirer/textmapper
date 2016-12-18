/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.ast.AstEnumMember;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.ast.AstList;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.rule.RhsMapping;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

class LiGrammarMapper implements GrammarMapper {

	protected Scope<Symbol> symScope = new LiScope<>();

	LiGrammarMapper(Grammar grammar) {
		if (grammar != null) {
			for (Symbol s : grammar.getSymbols()) {
				if (s.getName() == null) {
					throw new IllegalArgumentException("grammar contains symbols without a name");
				}
				symScope.insert(s, null);
			}
		}
	}

	void check(RhsPart part) {
		if (part == null || part.getLeft() == null) {
			throw new NullPointerException();
		}
		if (!symScope.contains(part.getLeft())) {
			throw new IllegalArgumentException("unknown right-hand side element passed");
		}
	}

	final void check(Symbol sym) {
		if (sym == null) {
			throw new NullPointerException();
		}
		if (!symScope.contains(sym)) {
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
	public void map(RhsSequence seq, AstField field, AstType subType, boolean isAddition) {
		check(seq);

		final AstType contextType = getEnclosingType(seq);
		if (contextType == null) {
			throw new IllegalArgumentException("cannot map sequence, map its nonterminal first");
		}

		if (field != null && contextType != field.getContainingClass()) {
			throw new IllegalArgumentException("field must belong to the context class (" +
					contextType.toString() + ")");
		}

		AstType type = field != null ? field.getType() : contextType;
		if (isAddition) {
			if (!(type instanceof AstList)) {
				throw new IllegalArgumentException("addition is applicable only to list types");
			}
			type = ((AstList) type).getInner();
		}

		if (subType != null && !subType.isSubtypeOf(type)) {
			throw new IllegalArgumentException(
					"sequence type should be a subtype of its context type (" +
							type.toString() + ")");
		}
		if (seq.getMapping() != null || seq.getType() != null) {
			throw new IllegalArgumentException("cannot re-map sequences");
		}
		((LiRhsSequence) seq).map(subType, new LiRhsMapping(field, null, isAddition));
	}


	private AstType getEnclosingType(RhsPart part) {
		RhsSequence context = part.getContext();
		while (context != null) {
			AstType type = context.getType();
			if (type != null) {
				return type;
			}

			RhsMapping mapping = context.getMapping();
			if (mapping == null) {
				// seal sequence
				((LiRhsSequence) context).map(null, LiRhsMapping.EMPTY_MAPPING);
			} else if (mapping.getField() != null) {
				return mapping.getField().getType();
			}

			context = context.getContext();
		}

		return part.getLeft().getType();
	}

	@Override
	public void map(RhsSymbol symbol, AstField field, Object value, boolean isAddition) {
		check(symbol);

		final AstType contextType = getEnclosingType(symbol);
		if (contextType == null) {
			throw new IllegalArgumentException("cannot map symbol, map its nonterminal first");
		}

		if (field != null && contextType != field.getContainingClass()) {
			throw new IllegalArgumentException("field must belong to the context class (" +
					contextType.toString() + ")");
		}

		AstType type = field != null ? field.getType() : contextType;
		if (isAddition) {
			if (!(type instanceof AstList)) {
				throw new IllegalArgumentException("addition is applicable only to list types");
			}
			type = ((AstList) type).getInner();
		}

		if (value != null && !(value instanceof AstEnumMember) && !(value instanceof Boolean)
				&& !(value instanceof Integer) && !(value instanceof String)) {
			throw new IllegalArgumentException(
					"value must be AstEnumMember, Integer, Boolean or String");
		}

		if (value instanceof AstEnumMember
				&& type != ((AstEnumMember) value).getContainingEnum()) {
			throw new IllegalArgumentException(
					"enumeration value should match " + (field != null
							? "the field type"
							: "the nonterminal type"));

		} else if (type != AstType.ANY &&
				(value instanceof Boolean && type != AstType.BOOL
						|| value instanceof String && type != AstType.STRING
						|| value instanceof Integer && type != AstType.INT)) {
			throw new IllegalArgumentException(
					"value should match " + (field != null
							? "the field type"
							: "the nonterminal type") + ", " + type + " is expected");
		}
		if (symbol.getMapping() != null) {
			throw new IllegalArgumentException("cannot re-map symbols");
		}
		((LiRhsSymbol) symbol).setMapping(new LiRhsMapping(field, value, isAddition));
	}
}
