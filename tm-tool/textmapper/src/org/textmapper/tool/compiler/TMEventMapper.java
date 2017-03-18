/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
import org.textmapper.lapg.common.SetBuilder;
import org.textmapper.lapg.common.SetsClosure;
import org.textmapper.lapg.util.NonterminalUtil;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class TMEventMapper {

	static final String TOKEN_CATEGORY = "TokenSet";

	private final Grammar grammar;
	private final Map<String, Object> opts;
	private final ProcessingStatus status;

	private final Set<Symbol> reportedTokens = new HashSet<>();
	private final Map<Nonterminal, List<RhsSequence>> index = new HashMap<>();
	private final Map<String, List<RhsSequence>> typeIndex = new HashMap<>();
	private final Set<Nonterminal> lists = new HashSet<>();
	private final Map<String, Set<String>> categories = new HashMap<>();

	private final Map<String, List<Nonterminal>> categoryNonterms = new HashMap<>();
	private final Set<Nonterminal> entered = new HashSet<>();
	private final Map<Symbol, TMPhrase> phrases = new HashMap<>();


	public TMEventMapper(Grammar grammar, Map<String, Object> opts, ProcessingStatus status) {
		this.grammar = grammar;
		this.opts = opts;
		this.status = status;

		Object rt = opts.get("reportTokens");
		if (rt instanceof Collection && ((Collection<?>) rt).stream()
				.allMatch(p -> p instanceof Symbol)) {
			reportedTokens.addAll((Collection<? extends Symbol>) rt);
		}
	}

	public void deriveTypes() {
		boolean withFields = Boolean.TRUE.equals(opts.get("eventFields"));
		computeTypes();
		if (withFields) {
			computeFields();
		}
	}

	private void computeTypes() {
		Map<RhsSequence, String> sequenceTypes = new HashMap<>();
		List<String> allTypes = new ArrayList<>();

		for (Rule rule : grammar.getRules()) {
			RhsSequence seq = rule.getSource();
			RangeType rangeType = TMDataUtil.getRangeType(seq);
			String type = rangeType != null ? rangeType.getName() : "";
			String existing;
			if ((existing = sequenceTypes.putIfAbsent(seq, type)) != null) {
				if (!existing.equals(type)) {
					throw new IllegalStateException();
				}
			} else {
				List<RhsSequence> list = index.get(seq.getLeft());
				if (list == null) {
					index.put(seq.getLeft(), list = new ArrayList<>());
				}
				list.add(seq);
			}

			RuleIndex ri = new RuleIndex(rule);
			RhsUtil.traverse(seq, rhsPart -> {
				if (!(rhsPart instanceof RhsSequence)) return;

				RangeType rt = TMDataUtil.getRangeType(rhsPart);
				if (rt == null) return;

				List<RhsSequence> typeSeqs = typeIndex.get(rt.getName());
				if (typeSeqs == null) {
					typeIndex.put(rt.getName(), typeSeqs = new ArrayList<>());
					allTypes.add(rt.getName());
				}
				typeSeqs.add((RhsSequence) rhsPart);

				if (!sequenceTypes.containsKey(rhsPart)) {
					TMDataUtil.putCustomRange(rule, ri.compute((RhsSequence) rhsPart, rt));
				}
			});
		}

		TMDataUtil.putTypes(grammar, allTypes);
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
			lists.add(rule.getLeft());
		}

		// Collect categories.
		for (Symbol symbol : grammar.getSymbols()) {
			if (!(symbol instanceof Nonterminal)) continue;
			Nonterminal n = (Nonterminal) symbol;

			RangeType rangeType = TMDataUtil.getRangeType(n);
			if (rangeType == null || !rangeType.isInterface()) continue;

			String category = rangeType.getName();
			if (!categories.containsKey(category)) {
				categories.put(category, new LinkedHashSet<>());
				categoryNonterms.put(category, new ArrayList<>());
			}
			categoryNonterms.get(category).add(n);
		}

		// A special category of tokens.
		categories.put(TOKEN_CATEGORY, new LinkedHashSet<>());
		categoryNonterms.put(TOKEN_CATEGORY, Collections.emptyList());

		// Pre-compute phrases for all nonterminals.
		for (Symbol symbol : grammar.getSymbols()) {
			if (!(symbol instanceof Nonterminal)) continue;

			computePhrase((Nonterminal) symbol, false);
		}

		// Build a set of types behind each interface.
		collectCategoryTypes();

		// Export fields.
		for (Entry<String, List<RhsSequence>> e : typeIndex.entrySet()) {
			String type = e.getKey();
			List<TMPhrase> list = new ArrayList<>();
			for (RhsSequence p : e.getValue()) {
				list.add(computePhrase(p, true));
			}
			TMPhrase phrase = TMPhrase.merge(list, e.getValue().get(0), status);
			phrase = phrase.resolve(categories);
			TMPhrase.verify(phrase, status);
			TMDataUtil.putRangeFields(grammar, type, extractFields(phrase));
		}

		// Export categories.
		for (Entry<String, Set<String>> e : categories.entrySet()) {
			List<String> types = new ArrayList<>(e.getValue());
			Collections.sort(types);
			TMDataUtil.putCategory(grammar, e.getKey(), types);
		}
	}

	private List<RangeField> extractFields(TMPhrase phrase) {
		List<RangeField> result = new ArrayList<>();
		Map<String, Integer> namedTypes = new HashMap<>();
		for (TMField field : phrase.getFields()) {
			if (!field.hasExplicitName()) {
				result.add(field);
				continue;
			}
			int comesAfter = -1;
			for (String type : field.getTypes()) {
				Integer prev = namedTypes.get(type);
				if (prev != null) {
					comesAfter = Math.max(comesAfter, prev);
				}
				namedTypes.put(type, result.size());
			}
			if (comesAfter >= 0) {
				RangeField prev = result.get(comesAfter);
				if (prev.isNullable() || prev.isList()) {
					String adj = prev.isNullable() ? "nullable" : "a list";
					status.report(ProcessingStatus.KIND_ERROR,
							"`" + prev.getName() + "` cannot be " + adj + ", since it precedes " +
									field.getName(), phrase);
				}
				field = field.withComesAfter(prev);
			}
			result.add(field);
		}
		return result;
	}

	private void collectCategoryTypes() {
		Map<String, Integer> catIndex = new HashMap<>();
		Map<String, Integer> typeIndex = new HashMap<>();
		List<String> allTypes = new ArrayList<>(this.typeIndex.keySet());
		Collections.sort(allTypes);
		List<String> allCategories = new ArrayList<>(this.categories.keySet());
		Collections.sort(allCategories);
		class Category {
			private Category(String name) {
				this.name = name;
			}

			final String name;
			int node;
			int[] deps;
		}
		List<Category> catList = new ArrayList<>();
		for (String name : allCategories) {
			catIndex.put(name, catIndex.size());
			catList.add(new Category(name));
		}
		for (String name : allTypes) {
			typeIndex.put(name, typeIndex.size());
		}

		// Fill in categories.
		SetsClosure closure = new SetsClosure();
		SetBuilder typeSet = new SetBuilder(typeIndex.size());
		SetBuilder categorySet = new SetBuilder(catIndex.size());

		for (String catName : allCategories) {
			List<Nonterminal> nonterminals = this.categoryNonterms.get(catName);
			Category cat = catList.get(catIndex.get(catName));
			for (Nonterminal nt : nonterminals) {
				TMPhrase phrase = computePhrase(nt, true);
				if (!phrase.isUnnamedField() || phrase.first().isList()) {
					status.report(ProcessingStatus.KIND_ERROR,
							nt.getNameText() + " cannot be used as an interface: "
									+ phrase.toString(), nt);
					continue;
				}
				for (String catOrType : phrase.first().getTypes()) {
					Integer category = catIndex.get(catOrType);
					if (category != null) {
						categorySet.add(category);
						continue;
					}

					Integer type = typeIndex.get(catOrType);
					if (type != null) {
						typeSet.add(type);
						continue;
					}

					throw new IllegalStateException();
				}
			}
			cat.node = closure.addSet(typeSet.create(), null);
			cat.deps = categorySet.create();
		}
		for (Category cat : catList) {
			for (int i = 0; i < cat.deps.length; i++) {
				cat.deps[i] = catList.get(cat.deps[i]).node;
			}
			closure.addDependencies(cat.node, cat.deps);
		}
		if (!closure.compute()) throw new IllegalStateException();
		for (Category cat : catList) {
			Set<String> catTypes = this.categories.get(cat.name);
			for (int typeId : closure.getSet(cat.node)) {
				catTypes.add(allTypes.get(typeId));
			}
		}
	}

	private boolean isListSelfReference(RhsSymbol ref) {
		Symbol target = ref.getTarget();
		if (ref.getLeft() != target || !(target instanceof Nonterminal)) return false;

		Nonterminal n = (Nonterminal) target;
		return NonterminalUtil.isList(n) || lists.contains(n);
	}

	private TMPhrase computePhrase(Nonterminal nt, boolean internal) {
		RangeType rangeType = TMDataUtil.getRangeType(nt);
		if (rangeType != null && rangeType.getName().equals("void")) {
			return TMPhrase.empty(nt);
		}

		TMPhrase result;
		if (!internal) {
			result = phrases.get(nt);
			if (result != null) return result;

			if (!entered.add(nt)) {
				status.report(ProcessingStatus.KIND_ERROR,
						"`" + nt.getNameText() + "' recursively contain itself", nt);
				result = TMPhrase.empty(nt);
			}

			// -> something
			if (result == null && rangeType != null && rangeType.isInterface()) {
				result = TMPhrase.type(rangeType.getName(), nt);
			}

			if (result != null) {
				phrases.put(nt, result);
				return result;
			}
		}

		List<TMPhrase> list = new ArrayList<>();
		for (RhsSequence p : index.get(nt)) {
			list.add(computePhrase(p, false));
		}
		if (rangeType != null) {
			result = TMPhrase.mergeSet(rangeType.getName(), list, nt, status);
		} else {
			result = TMPhrase.merge(list, nt, status);
		}
		if (lists.contains(nt)) {
			if (result.getFields().size() == 1 && !result.first().hasExplicitName()) {
				result = result.makeList(nt);
			} else if (result.getFields().stream()
					.allMatch(f -> f.hasExplicitName() && f.isList())) {
				result = result.makeList(nt);
			} else if (!result.isEmpty()) {
				status.report(ProcessingStatus.KIND_ERROR,
						"Cannot make a list out of: " + result.toString(), nt);

				result = TMPhrase.empty(nt);
			}
		}
		if (!internal) phrases.put(nt, result);
		return result;
	}

	private TMPhrase computePhrase(RhsPart part) {
		return computePhrase(part, false);
	}

	private TMPhrase computePhrase(RhsPart part, boolean internal) {
		switch (part.getKind()) {
			case Assignment: {
				RhsAssignment assignment = (RhsAssignment) part;
				RhsPart unwrapped = RhsUtil.unwrap(part);
				TMPhrase p;
				if (unwrapped instanceof RhsChoice) {
					p = TMPhrase.mergeSet(
							assignment.getName(),
							Arrays.stream(((RhsChoice) unwrapped).getParts())
									.map(this::computePhrase)
									.collect(Collectors.toList()),
							part, status);
				} else {
					p = computePhrase(assignment.getPart());
				}
				if (p.isEmpty()) {
					status.report(ProcessingStatus.KIND_ERROR,
							"No ast nodes behind an assignment `" + assignment.getName() + "'",
							part);
					return p;
				}
				if (!p.isUnnamedField()) {
					status.report(ProcessingStatus.KIND_ERROR,
							"Exactly one ast element behind " +
									assignment.getName() + " is expected: " + p.toString(), part);
					return p;
				}
				if (assignment.isAddition()) {
					p = p.makeList(part);
				}
				return p.withName(assignment.getName(), part);
			}
			case Symbol: {
				Symbol target = ((RhsSymbol) part).getTarget();
				if (target.isTerm() && reportedTokens.contains(target)) {
					categories.get(TOKEN_CATEGORY).add(target.getNameText());
					return TMPhrase.type(target.getNameText(), part);
				}

				TMPhrase p = phrases.get(target);
				if (p != null) return p;

				if (isListSelfReference((RhsSymbol) part) || target.isTerm()) {
					return TMPhrase.empty(part);
				}
				return computePhrase((Nonterminal) target, false);
			}
			case Optional:
				return computePhrase(((RhsOptional) part).getPart(), false).makeNullable(part);
			case Sequence:
				if (!internal) {
					RangeType type = TMDataUtil.getRangeType(part);
					if (type != null) {
						return TMPhrase.type(type.getName(), part);
					}
				}
				/* fallthrough */
			case Choice: {
				List<RhsPart> children = RhsUtil.getChildren(part);
				if (children == null) return TMPhrase.empty(part);

				if (children.size() == 1) {
					return computePhrase(children.get(0));
				}
				List<TMPhrase> list = children.stream()
						.map(this::computePhrase)
						.collect(Collectors.toList());

				if (part.getKind() == Kind.Choice) {
					return TMPhrase.merge(list, part, status);
				} else {
					return TMPhrase.concat(list, part, status);
				}
			}

			case StateMarker:
			case Set:
			case Ignored:
				return TMPhrase.empty(part);
			case Cast:
			case Unordered:
			case Conditional:
			case List:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}

	private class RuleIndex {
		private Rule rule;
		private Map<RhsSymbol, Integer> index;

		public RuleIndex(Rule rule) {
			this.rule = rule;
		}

		public CustomRange compute(RhsSequence rhsPart, RangeType rt) {
			if (index == null) {
				index = new HashMap<>();
				int i = 0;
				for (RhsCFPart p : rule.getRight()) {
					if (!(p instanceof RhsSymbol)) continue;
					index.put((RhsSymbol) p, i);
					i++;
				}
			}

			int[] result = new int[]{-1, -1};
			RhsUtil.traverse(rhsPart, p -> {
				if (!(p instanceof RhsSymbol)) return;
				Integer m = index.get(p);
				if (m == null) return;
				if (result[0] == -1) {
					result[0] = result[1] = m;
				} else if (m < result[0]) {
					result[0] = m;
				} else if (m > result[1]) {
					result[1] = m;
				}
			});
			if (result[0] != -1) {
				return new CustomRange(rule, result[0], result[1], rt);
			}
			return null;
		}
	}
}
