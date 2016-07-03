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
package org.textmapper.lapg.lalr;

import org.textmapper.lapg.api.Lookahead;
import org.textmapper.lapg.api.LookaheadRule;
import org.textmapper.lapg.api.LookaheadRule.LookaheadCase;
import org.textmapper.lapg.api.ProcessingStatus;

import java.util.*;

class ExplicitLookaheadBuilder {

	private static final LookaheadCase[] EMPTY_CASES = new LookaheadCase[0];

	private static class LiLookaheadRule implements LookaheadRule {
		private int index;
		private int refCount;
		private int refRule;
		private Set<Lookahead> lookaheads;
		private LookaheadCase[] cases = EMPTY_CASES;

		private LiLookaheadRule(int index, Set<Lookahead> lookaheads, int refRule) {
			this.index = index;
			this.lookaheads = lookaheads;
			this.refCount = 1;
			this.refRule = refRule;
		}

		void incRef() {
			refCount++;
		}

		void decRef() {
			refCount--;
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public LookaheadCase[] getCases() {
			return cases;
		}

		void computeCases(ProcessingStatus status) {
			// TODO
//			Set<Nonterminal> set = new HashSet<>();
//			for (Lookahead la : lookaheads) {
//				for (LookaheadPredicate p : la.getLookaheadPredicates()) {
//					set.add(p.getPrefix());
//				}
//			}
//			List<Nonterminal>
		}
	}

	private final int rulesCount;
	private final ProcessingStatus status;
	private final List<LiLookaheadRule> rules = new ArrayList<>();
	private final Map<Set<Lookahead>, LiLookaheadRule> resolutionMap = new HashMap<>();

	ExplicitLookaheadBuilder(int rulesCount, ProcessingStatus status) {
		this.rulesCount = rulesCount;
		this.status = status;
	}

	/**
	 * Creates or returns an existing resolution rule for the given set of lookahead rules.
	 */
	int addResolutionRule(Set<Lookahead> set, int refRule) {
		LiLookaheadRule rule = resolutionMap.get(set);
		if (rule != null) {
			rule.incRef();
			return rule.getIndex();
		}
		rule = new LiLookaheadRule(rulesCount + rules.size(), set, refRule);
		rules.add(rule);
		resolutionMap.put(set, rule);
		return rule.getIndex();
	}

	int addResolutionRule(int resolutionRule, Lookahead la) {
		LiLookaheadRule laRule = rules.get(resolutionRule - rulesCount);
		laRule.decRef();
		Set<Lookahead> set = new HashSet<>(laRule.lookaheads);
		set.add(la);
		return addResolutionRule(set, laRule.refRule);
	}

	int getRefRule(int resolutionRule) {
		return rules.get(resolutionRule - rulesCount).refRule;
	}

	void assignIndices() {
		int i = rulesCount;
		for (LiLookaheadRule r : rules) {
			if (r.refCount == 0) {
				r.index = -1;
			} else {
				r.index = i++;
			}
		}
	}

	void compact() {
		rules.removeIf(rule -> rule.getIndex() == -1);
	}

	int getRuleIndex(int resolutionRule) {
		int index = rules.get(resolutionRule - rulesCount).getIndex();
		if (index == -1) {
			throw new IllegalStateException();
		}
		return index;
	}

	boolean isResolutionRule(int rule) {
		return rule >= this.rulesCount;
	}

	LookaheadRule[] extractRules() {
		for (LiLookaheadRule rule : rules) {
			rule.computeCases(status);
		}
		return rules.toArray(new LookaheadRule[rules.size()]);
	}
}
