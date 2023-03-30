/**
 * Copyright 2002-2022 Evgeny Gryaznov
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

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.LookaheadRule.LookaheadCase;
import org.textmapper.lapg.api.rule.LookaheadPredicate;
import org.textmapper.lapg.builder.LiUtil;

import java.util.*;
import java.util.stream.Collectors;

class ExplicitLookaheadBuilder {

	private static final LookaheadCase[] EMPTY_CASES = new LookaheadCase[0];

	private static class Node {
		InputRef input;
		Set<Node> prev = new HashSet<>();
		List<Lookahead> positive = new ArrayList<>();
		List<Lookahead> negative = new ArrayList<>();

		boolean processing = false;
		boolean done = false;

		public Node(InputRef input) {
			this.input = input;
		}

		String getName() {
			return input.getTarget().getNameText();
		}

		void addPredicate(boolean negated, Lookahead la) {
			(negated ? negative : positive).add(la);
		}

		boolean serializeTo(List<Node> res) {
			if (processing) return false;
			if (done) return true;
			processing = true;
			Node[] nodes = prev.toArray(new Node[prev.size()]);
			if (nodes.length > 1) {
				Arrays.sort(nodes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
			}
			for (Node n : nodes) {
				if (!n.serializeTo(res)) return false;
			}
			processing = false;
			done = true;
			if (input != null) {
				res.add(this);
			}
			return true;
		}

		List<Node> serialize() {
			List<Node> result = new ArrayList<>();
			if (!serializeTo(result)) return null;
			return result;
		}

		Lookahead pickLookahead(LinkedHashSet<Lookahead> lookaheads, boolean negated) {
			List<Lookahead> list = (negated ? negative : positive).stream()
					.filter(lookaheads::contains)
					.collect(Collectors.toList());
			if (list.size() != 1) return null;
			return list.get(0);
		}
	}

	private static class LiLookaheadCase implements LookaheadCase {

		InputRef input;
		boolean negated;
		Nonterminal target;

		public LiLookaheadCase(InputRef input, boolean negated, Nonterminal target) {
			this.input = input;
			this.negated = negated;
			this.target = target;
		}

		@Override
		public InputRef getInput() {
			return input;
		}

		@Override
		public boolean isNegated() {
			return negated;
		}

		@Override
		public Nonterminal getTarget() {
			return target;
		}
	}

	private static class LiLookaheadRule implements LookaheadRule {
		private int index;
		private int refCount;
		private int refRule;
		private Set<Lookahead> lookaheads;
		private LookaheadCase[] cases = EMPTY_CASES;
		private Nonterminal defaultTarget = null;

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

		@Override
		public Nonterminal getDefaultTarget() {
			return defaultTarget;
		}

		Node addNode(Map<InputRef, Node> nodes, Node prev, InputRef input) {
			Node node = nodes.get(input);
			if (node == null) {
				node = new Node(input);
				nodes.put(input, node);
			}
			if (prev != null) {
				node.prev.add(prev);
			}
			return node;
		}

		void reportError(ProcessingStatus status, String message) {
			status.report(ProcessingStatus.KIND_ERROR, message
					+ lookaheads.stream().map(Lookahead::asString)
					.collect(Collectors.joining(", ")),
					lookaheads.toArray(new SourceElement[lookaheads.size()]));
		}

		void computeCases(ProcessingStatus status) {
			Node root = new Node(null);
			Map<InputRef, Node> nodes = new HashMap<>();
			for (Lookahead la : lookaheads) {
				Node prev = null;
				for (LookaheadPredicate p : la.getLookaheadPredicates()) {
					prev = addNode(nodes, prev, p.getInput());
					prev.addPredicate(p.isNegated(), la);
				}
				if (prev == null) {
					throw new IllegalStateException();
				}
				root.prev.add(prev);
			}
			List<Node> evalOrder = root.serialize();
			if (evalOrder == null) {
				reportError(status, "Conflicting lookaheads (inconsistent nonterminal order): ");
				return;
			}

			LinkedHashSet<Lookahead> lookaheads = new LinkedHashSet<>(this.lookaheads);
			List<LookaheadCase> cases = new ArrayList<>();
			for (Node node : evalOrder) {
				if (lookaheads.size() == 1) break;
				boolean negated = false;
				Lookahead target = node.pickLookahead(lookaheads, false /*negated*/);
				if (target == null) {
					target = node.pickLookahead(lookaheads, true /*negated*/);
					negated = true;
				}
				if (target == null) continue;

				lookaheads.remove(target);
				cases.add(new LiLookaheadCase(node.input, negated, target));
			}
			if (lookaheads.size() != 1) {
				reportError(status, "Conflicting lookaheads: ");
				return;
			}
			this.cases = cases.toArray(new LookaheadCase[cases.size()]);
			defaultTarget = lookaheads.iterator().next();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (LookaheadCase c : cases) {
				if (c.isNegated()) {
					sb.append("!");
				}
				sb.append(c.getInput().getTarget().getNameText());
				sb.append(" -> ");
				sb.append(LiUtil.getSymbolName(c.getTarget()));
				sb.append("; ");
			}
			sb.append("default -> ");
			if (defaultTarget == null) {
				sb.append("ERROR");
			} else {
				sb.append(LiUtil.getSymbolName(defaultTarget));
			}
			return sb.toString();
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
		Set<Lookahead> set = new LinkedHashSet<>(laRule.lookaheads);
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
