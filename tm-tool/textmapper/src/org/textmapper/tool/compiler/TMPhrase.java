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

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.tool.common.ArrayMerger;

import java.util.*;
import java.util.stream.Collectors;

class TMPhrase implements DerivedSourceElement {
	private final List<TMField> fields;
	private final SourceElement origin;

	TMPhrase(List<TMField> fields, SourceElement origin) {
		if (origin == null) {
			throw new NullPointerException("origin");
		}
		this.fields = fields;
		this.origin = origin;
	}

	static TMPhrase empty(SourceElement origin) {
		return new TMPhrase(Collections.emptyList(), origin);
	}

	static TMPhrase type(String rangeType, SourceElement origin) {
		return new TMPhrase(Collections.singletonList(new TMField(rangeType)), origin);
	}

	TMField first() {
		return fields.get(0);
	}

	public boolean isEmpty() {
		return fields.isEmpty();
	}

	boolean isMergeable() {
		return isEmpty() || isUnnamedField() && !first().isList();
	}

	boolean isUnnamedField() {
		return fields.size() == 1 && !first().hasExplicitName();
	}

	TMPhrase resolve(Map<String, Set<String>> categories) {
		return new TMPhrase(fields.stream()
				.map(f -> f.resolve(categories))
				.collect(Collectors.toList()), origin);
	}

	TMPhrase makeNullable(SourceElement origin) {
		if (fields.isEmpty()) return this;

		return new TMPhrase(fields.stream()
				.map(TMField::makeNullable)
				.collect(Collectors.toList()), origin);
	}

	TMPhrase makeList(SourceElement origin) {
		return new TMPhrase(fields.stream()
				.map(TMField::makeList)
				.collect(Collectors.toList()), origin);
	}

	TMPhrase withName(String newName, SourceElement origin) {
		if (!isUnnamedField()) throw new IllegalStateException();

		return new TMPhrase(Collections.singletonList(
				first().withName(newName)), origin);
	}

	static boolean allMergeable(List<TMPhrase> phrases) {
		return phrases.stream().allMatch(TMPhrase::isMergeable);
	}

	static TMPhrase mergeSet(String name,
							 List<TMPhrase> phrases,
							 SourceElement anchor,
							 ProcessingStatus status) {
		List<TMPhrase> nonMergable = phrases.stream()
				.filter(p -> !p.isMergeable())
				.collect(Collectors.toList());
		if (!nonMergable.isEmpty()) {
			for (TMPhrase p : nonMergable) {
				status.report(ProcessingStatus.KIND_ERROR,
						"Exactly one ast element behind " + name +
								" is expected: " + p.toString(), p);
			}
			return TMPhrase.empty(anchor);
		}

		List<TMField> fields = new ArrayList<>();
		boolean nullable = false;
		for (TMPhrase phrase : phrases) {
			if (phrase.isEmpty()) {
				nullable = true;
			} else {
				fields.add(phrase.first());
			}
		}
		if (fields.isEmpty()) {
			return TMPhrase.empty(anchor);
		}
		TMField field = TMField.merge(name, fields.toArray(new TMField[fields.size()]));
		if (nullable) {
			field = field.makeNullable();
		}
		return new TMPhrase(Collections.singletonList(field), anchor);
	}

	static TMPhrase merge(List<TMPhrase> phrases,
						  SourceElement anchor,
						  ProcessingStatus status) {
		Map<String, List<TMField>> bySignature = new LinkedHashMap<>();
		ArrayMerger<String> namedFields = new ArrayMerger<>();
		ArrayMerger<String> allFields = new ArrayMerger<>();

		Map<String, TMField> seen = new HashMap<>();
		for (TMPhrase p : phrases) {
			seen.clear();
			for (TMField f : p.fields) {
				String signature = f.getSignature();
				TMField existing = seen.putIfAbsent(signature, f);
				if (existing != null) {
					throw new IllegalStateException();
				}
				if (f.hasExplicitName()) {
					namedFields.add(signature);
				}
				allFields.add(signature);
				List<TMField> list = bySignature.get(signature);
				if (list == null) {
					bySignature.put(signature, list = new ArrayList<>());
				}
				list.add(f);
			}
			namedFields.flush();
			allFields.flush();
		}

		String[] order = namedFields.uniqueSort(String[]::new);
		if (order == null) {
			status.report(ProcessingStatus.KIND_ERROR,
					"named elements must occur in the same order in all productions", anchor);
			order = new String[0];
		}

		String[] sortedSignatures = allFields.topoSort(String[]::new);
		if (sortedSignatures == null) {
			Set<String> unnamed = new HashSet<>(bySignature.keySet());
			unnamed.removeAll(Arrays.asList(order));
			String[] unnamedArr = unnamed.toArray(new String[unnamed.size()]);
			Arrays.sort(unnamedArr);
			assert unnamedArr.length + order.length == bySignature.size();
			sortedSignatures = new String[bySignature.size()];
			System.arraycopy(order, 0, sortedSignatures, 0, order.length);
			System.arraycopy(unnamedArr, 0, sortedSignatures, order.length, unnamedArr.length);
		}

		List<TMField> result = new ArrayList<>(bySignature.size());
		for (String signature : sortedSignatures) {
			TMField composite;
			List<TMField> fields = bySignature.get(signature);
			if (fields.size() == 1) {
				composite = fields.get(0);
			} else {
				composite = TMField.merge(null, fields.toArray(new TMField[fields.size()]));
			}
			if (fields.size() < phrases.size()) {
				composite = composite.makeNullable();
			}
			result.add(composite);
		}
		return new TMPhrase(result, anchor);
	}

	static TMPhrase concat(List<TMPhrase> phrases,
						   SourceElement anchor,
						   ProcessingStatus status) {
		Map<String, TMField> seen = new LinkedHashMap<>();
		for (TMPhrase p : phrases) {
			for (TMField f : p.fields) {
				String signature = f.getSignature();
				TMField existing = seen.putIfAbsent(signature, f);
				if (existing != null) {
					if (!existing.isList() || !f.isList()) {
						status.report(ProcessingStatus.KIND_ERROR,
								"two fields with the same signature: " + existing.toString() +
										" -vs- " + f.toString(), anchor);
						continue;
					}
					f = TMField.merge(null, existing, f);
					seen.put(signature, f);
				}
			}
		}

		return new TMPhrase(new ArrayList<>(seen.values()), anchor);
	}

	static void verify(TMPhrase phrase, ProcessingStatus status) {
		Set<String> namedTypes = new HashSet<>();
		for (TMField field : phrase.getFields()) {
			if (field.hasExplicitName()) {
				namedTypes.addAll(Arrays.asList(field.getTypes()));
			}
		}
		Map<String, String> unnamedTypes = new HashMap<>();
		for (TMField field : phrase.fields) {
			if (field.hasExplicitName()) continue;
			String signature = field.getSignature();

			for (String type : field.getTypes()) {
				if (namedTypes.contains(type)) {
					status.report(ProcessingStatus.KIND_ERROR,
							"`" + type + "` occurs in both named and unnamed fields", phrase);
					return;
				}

				String prev = unnamedTypes.putIfAbsent(type, signature);
				if (prev != null && !prev.equals(signature)) {
					status.report(ProcessingStatus.KIND_ERROR,
							"two unnamed fields share the same type `" + type + "`: " +
									prev + " -vs- " + signature, phrase);
					return;
				}
			}
		}
		for (TMField field : phrase.getFields()) {
			if (field.getInterfaceType() != null) continue;

			if (field.getTypes().length > 8) {
				status.report(ProcessingStatus.KIND_WARN,
						"two many range types behind `" + field.getName() + "`, consider " +
								"introducing an interface",
						phrase);
			}
		}
	}

	public List<TMField> getFields() {
		return fields;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return fields.stream()
				.map(TMField::toString)
				.collect(Collectors.joining(" "));
	}
}
