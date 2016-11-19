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
import org.textmapper.lapg.common.SetBuilder;
import org.textmapper.lapg.common.SetsClosure;
import org.textmapper.lapg.util.NonterminalUtil;

import java.util.*;
import java.util.Map.Entry;

public class TMEventMapper {

	private final Grammar grammar;
	private final ProcessingStatus status;


	private final Map<RhsSequence, String> sequenceTypes = new HashMap<>();
	private final Map<Nonterminal, List<RhsSequence>> index = new HashMap<>();
	private final Map<String, List<RhsSequence>> typeIndex = new HashMap<>();
	private final Set<Nonterminal> lists = new HashSet<>();
	private final Map<String, Set<String>> categories = new HashMap<>();
	private final Map<String, List<Nonterminal>> categoryNonterms = new HashMap<>();

	private final Set<Nonterminal> entered = new HashSet<>();
	private final Map<Symbol, TMPhrase> phrases = new HashMap<>();


	public TMEventMapper(Grammar grammar, ProcessingStatus status) {
		this.grammar = grammar;
		this.status = status;
	}

	public void deriveTypes(boolean withFields) {
		computeTypes();
		if (withFields) {
			computeFields();
		}
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

			if (!NonterminalUtil.isList(left) &&
					!TMDataUtil.hasProperty(rule.getLeft(), "noast")) {
				status.report(ProcessingStatus.KIND_ERROR,
						rule.getLeft().getName() + " have to be marked as @noast", rule);
			}
			lists.add(rule.getLeft());
		}

		// Collect categories.
		for (Symbol symbol : grammar.getSymbols()) {
			if (!(symbol instanceof Nonterminal)) continue;
			Nonterminal n = (Nonterminal) symbol;

			if (isInterface(n)) {
				String category = getVariableName(n);
				if (categories.containsKey(category)) continue;

				if (typeIndex.containsKey(category)) {
					status.report(ProcessingStatus.KIND_ERROR,
							"the name `" + category + "' is used for both - " +
									"an interface and a type", symbol);
					continue;
				}
				categories.put(category, new LinkedHashSet<>());
				categoryNonterms.put(category, new ArrayList<>(Collections.singletonList(n)));
			}
		}

		// Category extensions.
		for (Symbol symbol : grammar.getSymbols()) {
			if (!(symbol instanceof Nonterminal)) continue;
			Nonterminal n = (Nonterminal) symbol;

			Nonterminal type = TMDataUtil.getCustomType(n);
			if (type == null) continue;

			String category = getVariableName(type);
			if (!categoryNonterms.containsKey(category)) {
				status.report(ProcessingStatus.KIND_ERROR,
						category + " must be an interface", symbol);
				continue;
			}
			categoryNonterms.get(category).add(n);
		}

		// Pre-compute phrases for all nonterminals.
		for (Symbol symbol : grammar.getSymbols()) {
			if (symbol instanceof Nonterminal) {
				computePhrase((Nonterminal) symbol, false);
			}
		}

		// Build a set of types behind each interface.
		collectCategoryTypes();

		for (Entry<String, List<RhsSequence>> e : typeIndex.entrySet()) {
			String type = e.getKey();
			List<TMPhrase> list = new ArrayList<>();
			for (RhsSequence p : e.getValue()) {
				list.add(computePhrase(p));
			}
			TMPhrase phrase = TMPhrase.merge(list, e.getValue().get(0), status);
			TMPhrase.verify(phrase, e.getValue().get(0), status);
//			System.out.println(type + ": " + phrase.toString());
			TMDataUtil.putRangeFields(grammar, type, phrase.fields);
		}
	}

	private void collectCategoryTypes() {
		Map<String,Integer> catIndex = new HashMap<>();
		Map<String,Integer> typeIndex = new HashMap<>();
		List<String> allTypes = new ArrayList<>(this.typeIndex.keySet());
		Collections.sort(allTypes);
		List<String> allCategories = new ArrayList<>(this.categories.keySet());
		Collections.sort(allCategories);
		class Category {
			private Category(String name) {
				this.name = name;
			}
			String name;
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
							getVariableName(nt) + " cannot be used as an interface: "
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
			if (NonterminalUtil.isList(n)
					|| NonterminalUtil.isOptional(n)
					|| TMDataUtil.hasProperty(n, "_set")
					|| isInterface(n)
					|| TMDataUtil.hasProperty(n, "noast")) {
				return "";
			}
		}

		if (n.getTemplate() != null) n = n.getTemplate();
		TMDataUtil.putRangeType(rule, n.getName());
		return n.getName();
	}

	private static boolean isListSelfReference(RhsSymbol ref) {
		Symbol target = ref.getTarget();
		if (ref.getLeft() != target || !(target instanceof Nonterminal)) return false;

		Nonterminal n = (Nonterminal) target;
		return NonterminalUtil.isList(n) || TMDataUtil.hasProperty(n, "noast");
	}

	private TMPhrase computePhrase(Nonterminal nt, boolean internal) {
		TMPhrase result;
		if (!internal) {
			result = phrases.get(nt);
			if (result != null) return result;

			if (!entered.add(nt)) {
				status.report(ProcessingStatus.KIND_ERROR,
						"`" + nt.getName() + "' recursively contain itself", nt);
				result = TMPhrase.empty();
			}

			Nonterminal category = TMDataUtil.getCustomType(nt);
			if (result == null && category != null) {
				result = new TMPhrase(new TMField(getVariableName(category)));
			}

			String ntName = getVariableName(nt);
			if (result == null && categories.containsKey(ntName)) {
				result = new TMPhrase(new TMField(ntName));
			}

			if (result != null) {
				phrases.put(nt, result);
				return result;
			}
		}

		List<TMPhrase> list = new ArrayList<>();
		for (RhsSequence p : index.get(nt)) {
			String type = sequenceTypes.get(p);
			if (!type.isEmpty()) {
				list.add(new TMPhrase(new TMField(type)));
				continue;
			}
			list.add(computePhrase(p));
		}
		result = TMPhrase.merge(list, nt, status);
		if (result.isUnnamedField() && !NonterminalUtil.isOptional(nt)
				&& !NonterminalUtil.isList(nt) && !TMDataUtil.hasProperty(nt, "noname")) {
			result = result.withName(getVariableName(nt));
		}
		if (lists.contains(nt)) {
			if (result.fields.size() == 1 && !result.first().isList()) {
				result = result.makeList();
			} else if (!result.isEmpty()) {
				status.report(ProcessingStatus.KIND_ERROR,
						"Cannot make a list out of: " + result.toString(), nt);

				result = TMPhrase.empty();
			}
		}
		if (!internal) phrases.put(nt, result);
		return result;
	}

	private TMPhrase computePhrase(RhsPart part) {
		switch (part.getKind()) {
			case Assignment: {
				RhsAssignment assignment = (RhsAssignment) part;
				TMPhrase p = computePhrase(assignment.getPart());
				if (p.isEmpty()) {
					status.report(ProcessingStatus.KIND_ERROR,
							"No ast nodes behind an assignment `" + assignment.getName() + "'",
							part);
					return p;
				}
				if (!p.isUnnamedField()) {
					status.report(ProcessingStatus.KIND_ERROR,
							"More than one ast element behind an assignment (" +
									assignment.getName() + "): " + p.toString(), part);
					return p;
				}
				return new TMPhrase(p.first().withExplicitName(assignment.getName(),
						assignment.isAddition()));
			}
			case Symbol: {
				Symbol target = ((RhsSymbol) part).getTarget();
				TMPhrase p = phrases.get(target);
				if (p != null) return p;

				if (isListSelfReference((RhsSymbol) part) || target.isTerm()) {
					return TMPhrase.empty();
				}
				return computePhrase((Nonterminal) target, false);
			}
			case Optional:
				return computePhrase(((RhsOptional) part).getPart()).makeNullable();
			case Choice:
			case Sequence: {
				RhsPart[] parts = ((RhsSequence) part).getParts();
				if (parts.length == 1) {
					return computePhrase(parts[0]);
				}

				List<TMPhrase> list = new ArrayList<>();
				for (RhsPart p : parts) {
					list.add(computePhrase(p));
				}
				if (part.getKind() == Kind.Choice) {
					return TMPhrase.merge(list, part, status);
				} else {
					return TMPhrase.concat(list, part, status);
				}
			}

			case StateMarker:
			case Set:
			case Ignored:
				return TMPhrase.empty();
			case Cast:
			case Unordered:
			case Conditional:
			case List:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}

	private String getVariableName(Symbol s) {
		if (s instanceof Nonterminal) {
			Nonterminal template = ((Nonterminal) s).getTemplate();
			if (template != null) {
				return template.getName();
			}
		}
		return s.getName();
	}

	private boolean isInterface(Nonterminal n) {
		TMTypeHint hint = TMDataUtil.getTypeHint(n);
		return hint != null && hint.getKind() == TMTypeHint.Kind.INTERFACE;
	}
}
