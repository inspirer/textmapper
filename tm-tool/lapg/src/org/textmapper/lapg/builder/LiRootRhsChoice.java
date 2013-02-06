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

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.rule.RhsChoice;
import org.textmapper.lapg.api.rule.RhsSwitch;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * evgeny, 12/25/12
 */
class LiRootRhsChoice extends LiRhsRoot implements RhsChoice {

	private List<LiRhsPart> rulesList = new ArrayList<LiRhsPart>();
	private LiRhsPart[] rules;

	LiRootRhsChoice(Nonterminal left) {
		super(left, null);
	}

	void addRule(LiRhsPart rule) {
		toList();
		rulesList.add(rule);
		rule.setParent(this);
	}


	@Override
	public LiRhsPart[] getParts() {
		toArr();
		return rules;
	}

	@Override
	List<RhsSymbol[]> expand() {
		List<RhsSymbol[]> result = new ArrayList<RhsSymbol[]>();
		for (LiRhsPart part : rules) {
			result.addAll(part.expand());
		}
		return result;
	}

	@Override
	protected boolean replaceChild(LiRhsPart child, LiRhsPart newChild) {
		toArr();
		return replaceInArray(rules, child, newChild);
	}

	@Override
	public boolean structuralEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LiRootRhsChoice that = (LiRootRhsChoice) o;
		return structuralEquals(getParts(), that.getParts());
	}

	@Override
	public int structuralHashCode() {
		return structuralHashCode(getParts());
	}

	@Override
	public <T> T accept(RhsSwitch<T> switch_) {
		return switch_.caseChoice(this);
	}

	private void toArr() {
		if (rules == null) {
			rules = rulesList.toArray(new LiRhsPart[rulesList.size()]);
			rulesList = null;
		}
	}

	private void toList() {
		if (rulesList == null) {
			rulesList = new ArrayList<LiRhsPart>(rules.length);
			rulesList.addAll(Arrays.asList(rules));
			rules = null;
		}
	}
}
