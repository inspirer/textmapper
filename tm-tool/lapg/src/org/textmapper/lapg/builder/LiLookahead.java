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

import org.textmapper.lapg.api.Lookahead;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.LookaheadPredicate;

import java.util.Collection;

class LiLookahead extends LiNonterminal implements Lookahead {

	private LookaheadPredicate[] predicates;

	LiLookahead(Collection<LookaheadPredicate> predicates, SourceElement origin) {
		super(null /* name */, nameHint(predicates), origin);
		this.predicates = predicates.toArray(new LookaheadPredicate[predicates.size()]);
		addRule(new LiRhsSequence(null /*name*/, new LiRhsPart[0], false, origin));
		setNullable(true);
	}

	@Override
	public LookaheadPredicate[] getLookaheadPredicates() {
		return predicates;
	}

	@Override
	public String asString() {
		StringBuilder sb = new StringBuilder();
		for (LookaheadPredicate p : predicates) {
			if (sb.length() > 0) {
				sb.append(" & ");
			}
			if (p.isNegated()) {
				sb.append("!");
			}
			sb.append(p.getInput().getTarget().getNameText());
		}
		return sb.toString();
	}

	private static String nameHint(Collection<LookaheadPredicate> predicates) {
		StringBuilder sb = new StringBuilder("lookahead");
		for (LookaheadPredicate p : predicates) {
			sb.append('_');
			if (p.isNegated()) {
				sb.append("not");
			}
			sb.append(p.getInput().getTarget().getNameText());
		}
		return sb.toString();
	}
}
