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
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSwitch;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 12/25/12
 */
class LiRootRhsChoice extends LiUserDataHolder implements RhsChoice {

	private List<RhsPart> rules = new ArrayList<RhsPart>();
	private final Nonterminal left;

	LiRootRhsChoice(Nonterminal left) {
		this.left = left;
	}

	void addRule(RhsPart rule) {
		rules.add(rule);
	}

	@Override
	public RhsPart[] getParts() {
		return rules.toArray(new RhsPart[rules.size()]);
	}

	@Override
	public Nonterminal getLeft() {
		return left;
	}

	@Override
	public Object structuralNode() {
		return this;
	}

	@Override
	public <T> T accept(RhsSwitch<T> switch_) {
		return switch_.caseChoice(this);
	}
}
