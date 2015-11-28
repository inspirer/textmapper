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

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.rule.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * evgeny, 12/25/12
 */
class LiRootRhsChoice extends LiRhsRoot implements RhsChoice {

	private List<LiRhsPart> rulesList;
	private LiRhsPart[] rules;

	LiRootRhsChoice(Nonterminal left) {
		super(left, null);
		rulesList = new ArrayList<>();
	}

	void addRule(LiRhsPart rule) {
		if (!(rule instanceof RhsRule)) {
			throw new IllegalArgumentException("rule");
		}
		toList();
		rulesList.add(rule);
		register(false, rule);
	}

	@Override
	public LiRhsPart[] getParts() {
		toArr();
		return rules;
	}

	@Override
	List<RhsSymbol[]> expand(ExpansionContext context) {
		List<RhsSymbol[]> result = new ArrayList<>();
		for (LiRhsPart part : rules) {
			result.addAll(part.expand(context));
		}
		return result;
	}

	private void toArr() {
		if (rules == null) {
			rules = rulesList.toArray(new LiRhsPart[rulesList.size()]);
			rulesList = null;
		}
	}

	private void toList() {
		if (rulesList == null) {
			rulesList = new ArrayList<>(rules.length);
			rulesList.addAll(Arrays.asList(rules));
			rules = null;
		}
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LiRootRhsChoice that = (LiRootRhsChoice) o;
		return structurallyEquals(getParts(), that.getParts());
	}

	@Override
	public int structuralHashCode() {
		return structuralHashCode(getParts());
	}

	@Override
	public Kind getKind() {
		return Kind.Choice;
	}

	@Override
	protected RhsSequence[] preprocess() {
		toArr();
		for (RhsPart p : rules) {
			if (!(p instanceof RhsSequence)) throw new IllegalStateException();
		}
		RhsSequence[] result = new RhsSequence[rules.length];
		System.arraycopy(rules, 0, result, 0, rules.length);
		return result;
	}

	@Override
	protected void toString(StringBuilder sb) {
		toArr();
		if (rules.length >= 1) {
			sb.append("  ");
			toString(sb, rules, "\n| ");
			sb.append("\n");
		} else {
			sb.append("<broken choice>");
		}
	}
}
