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
package org.textmapper.tool.compiler;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsPart.Kind;
import org.textmapper.lapg.common.RuleUtil;
import org.textmapper.lapg.util.NonterminalUtil;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;
import java.util.Map.Entry;

public class TMEventMapper {

	private final Grammar grammar;
	private final ProcessingStatus status;


	private final Map<RhsSequence, String> sequenceTypes = new HashMap<>();
	private final Map<Nonterminal, List<RhsSequence>> index = new HashMap<>();
	private final Map<String, List<RhsSequence>> typeIndex = new HashMap<>();
	private final Set<Nonterminal> lists = new HashSet<>();
	private final Map<Nonterminal, Set<String>> categories = new HashMap<>();

	private final Set<Nonterminal> entered = new HashSet<>();
	private final Map<Symbol, TMRangePhrase> phrases = new HashMap<>();


	public TMEventMapper(Grammar grammar, ProcessingStatus status) {
		this.grammar = grammar;
		this.status = status;
	}

	public void deriveTypes() {
		computeRoles();
		computeTypes();
		computeFields();
	}

	private void computeTypes() {
		for (Rule rule : grammar.getRules()) {
			String type = assignRangeType(rule);
			String existing;
			if ((existing = sequenceTypes.putIfAbsent(rule.getSource(), type)) != null) {
				if (!existing.equals(type)) {
					throw new IllegalStateException();
				}
			}
		}
		for (Entry<RhsSequence, String> e : sequenceTypes.entrySet()) {
			RhsSequence seq = e.getKey();
			List<RhsSequence> list = index.get(seq.getLeft());
			if (list == null) {
				index.put(seq.getLeft(), list = new ArrayList<>());
			}
			list.add(seq);

			String type = e.getValue();
			if (type.isEmpty()) continue;
			list = typeIndex.get(type);
			if (list == null) {
				typeIndex.put(type, list = new ArrayList<>());
			}
			list.add(seq);
		}
	}

	private void computeRoles() {
		for (Symbol sym : grammar.getSymbols()) {
			if (!(sym instanceof Nonterminal)) continue;
			assignRoles(((Nonterminal) sym).getDefinition());
		}
	}

	private boolean isListRule(Rule rule) {
		if (TMDataUtil.getRangeType(rule) != null) return false;

		Nonterminal left = rule.getLeft();
		for (RhsCFPart r : rule.getRight()) {
			if (r instanceof RhsSymbol && r.getTarget() == left) {
				return true;
			}
		}
		return false;
	}

	private void computeFields() {
		// Detect lists.
		for (Rule rule : grammar.getRules()) {
			if (!isListRule(rule)) continue;

			Nonterminal left = rule.getLeft();

			if (!(left.getDefinition() instanceof RhsList) &&
					!TMDataUtil.hasProperty(rule.getLeft(), "noast")) {
				status.report(ProcessingStatus.KIND_ERROR,
						rule.getLeft().getName() + " have to be marked as @noast", rule);
			}
			lists.add(rule.getLeft());
		}

		// Collect categories.
		for (Symbol symbol : grammar.getSymbols()) {
			if (symbol instanceof Nonterminal &&
					TMDataUtil.hasProperty(symbol, "category")) {
				categories.put((Nonterminal) symbol, new LinkedHashSet<>());
			}
		}

		// Pre-compute phrases for all nonterminals.
		for (Symbol symbol : grammar.getSymbols()) {
			if (symbol instanceof Nonterminal) {
				computePhrase((Nonterminal) symbol, false);
			}
		}

		// TODO fill in categories
		// TODO actually compute fields
	}

	private String assignRangeType(Rule rule) {
		RhsSequence seq = rule.getSource();
		if (seq.getName() != null) {
			TMDataUtil.putRangeType(rule, seq.getName());
			return seq.getName();
		}

		if (seq.getParts().length > 0 &&
				TMDataUtil.hasProperty(seq.getParts()[0], "noast")) {
			return "";
		}

		Nonterminal n = rule.getLeft();
		if (n instanceof Lookahead) return "";
		if (!TMDataUtil.hasProperty(n, "ast")) {
			if (n.getDefinition() instanceof RhsList
					&& ((RhsList) n.getDefinition()).getCustomInitialElement() == null
					|| NonterminalUtil.isOptional(n)
					|| TMDataUtil.hasProperty(n, "_set")
					|| TMDataUtil.hasProperty(n, "category")
					|| TMDataUtil.hasProperty(n, "listof")
					|| TMDataUtil.hasProperty(n, "noast")) {
				return "";
			}
		}

		if (n.getTemplate() != null) n = n.getTemplate();
		TMDataUtil.putRangeType(rule, n.getName());
		return n.getName();
	}

	private void assignRoles(RhsPart part) {
		switch (part.getKind()) {
			case Assignment: {
				RhsAssignment a = (RhsAssignment) part;
				RhsSymbol sym = RuleUtil.getAssignmentSymbol(a);
				if (sym != null) {
					TMDataUtil.putRole(sym, a.getName());
				}
				return;
			}
			case Ignored:
			case Symbol:
			case Set:
			case StateMarker:
				// cannot contain aliases
				return;
			case Optional:
			case Cast:
			case Choice:
			case Sequence:
			case Unordered:
			case List:
				final Iterable<RhsPart> children = RhsUtil.getChildren(part);
				if (children == null) return;

				for (RhsPart child : children) {
					assignRoles(child);
				}
				return;
			case Conditional:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}

	private TMRangePhrase computePhrase(Nonterminal nt, boolean internal) {
		TMRangePhrase result;
		if (!internal) {
			result = phrases.get(nt);
			if (result != null) return result;

			if (!entered.add(nt)) {
				status.report(ProcessingStatus.KIND_ERROR,
						"`" + nt.getName() + "' recursively contain itself", nt);
				result = TMRangePhrase.empty();
			}

			if (categories.containsKey(nt)) {
				result = new TMRangePhrase(new TMRangeField(nt.getName()));
			}

			if (result != null) {
				phrases.put(nt, result);
				return result;
			}
		}

		List<TMRangePhrase> list = new ArrayList<>();
		for (RhsSequence p : index.get(nt)) {
			String type = sequenceTypes.get(p);
			if (!type.isEmpty()) {
				list.add(new TMRangePhrase(new TMRangeField(type)));
				continue;
			}
			list.add(computePhrase(nt.getName(), p));
		}
		result = TMRangePhrase.merge(nt.getName(), list, nt, status);
		if (lists.contains(nt)) {
			if (!result.isSingleElement()) {
				if (result.fields.size() > 0) {
					status.report(ProcessingStatus.KIND_ERROR,
							"Invalid list: " + result.toString(), nt);
				}
				result = TMRangePhrase.empty();
			} else {
				result = result.makeList();
			}
		}
		if (!internal) phrases.put(nt, result);
		return result;
	}

	private TMRangePhrase computePhrase(String contextName, RhsPart part) {
		switch (part.getKind()) {
			case Assignment: {
				RhsAssignment assignment = (RhsAssignment) part;
				TMRangePhrase p = computePhrase(contextName, assignment.getPart());
				if (!p.isUnnamedField()) {
					status.report(ProcessingStatus.KIND_ERROR,
							"More than one ast element behind an assignment (" +
									assignment.getName() + ")", part);
					return p;
				}
				return new TMRangePhrase(p.fields.get(0).withName(assignment.getName(), true));
			}
			case Symbol: {
				Symbol target = ((RhsSymbol) part).getTarget();
				if (target == part.getLeft() || target.isTerm()) {
					// This must be a list, ignore.
					return TMRangePhrase.empty();
				}
				TMRangePhrase p = computePhrase((Nonterminal) target, false);
				if (p.isUnnamedField()) {
					p = new TMRangePhrase(p.fields.get(0).withName(target.getName(), false));
				}
				return p;
			}
			case Optional:
				return computePhrase(contextName, ((RhsOptional) part).getPart()).makeNullable();
			case Choice:
			case Sequence: {
				List<TMRangePhrase> list = new ArrayList<>();
				for (RhsPart p : ((RhsSequence) part).getParts()) {
					list.add(computePhrase(null, p));
				}
				if (part.getKind() == Kind.Choice) {
					return TMRangePhrase.merge(contextName, list, part, status);
				} else {
					return TMRangePhrase.concat(list, part, status);
				}
			}

			case StateMarker:
			case Set:
			case Ignored:
				return TMRangePhrase.empty();
			case Cast:
			case Unordered:
			case Conditional:
			case List:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}
}
