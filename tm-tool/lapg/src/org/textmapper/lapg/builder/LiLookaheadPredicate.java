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

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.rule.LookaheadPredicate;

class LiLookaheadPredicate implements LookaheadPredicate {

	private final Nonterminal rule;
	private final boolean negated;

	LiLookaheadPredicate(Nonterminal rule, boolean negated) {
		this.rule = rule;
		this.negated = negated;
	}

	@Override
	public Nonterminal getPrefix() {
		return rule;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiLookaheadPredicate that = (LiLookaheadPredicate) o;

		return negated == that.negated && rule.equals(that.rule);
	}

	@Override
	public int hashCode() {
		int result = rule.hashCode();
		result = 31 * result + (negated ? 1 : 0);
		return result;
	}
}
