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

import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.tool.common.UniqueOrder;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class TMPhrase {
	final List<TMField> fields;

	TMPhrase(TMField... fields) {
		this.fields = Arrays.asList(fields);
	}

	static TMPhrase empty() {
		return new TMPhrase();
	}

	static TMPhrase type(String rangeType) {
		return new TMPhrase(new TMField(rangeType));
	}

	private TMPhrase(List<TMField> fields) {
		this.fields = fields;
	}

	TMField first() {
		return fields.get(0);
	}

	public boolean isEmpty() {
		return fields.isEmpty();
	}

	boolean isMergeable() {
		return isEmpty() || isUnnamedField() && !first().isList() && !first().isListElement();
	}

	boolean isUnnamedField() {
		return fields.size() == 1 && !first().hasExplicitName();
	}

	TMPhrase makeNullable() {
		if (fields.isEmpty()) return this;

		TMField[] result = new TMField[fields.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = fields.get(i).makeNullable();
		}
		return new TMPhrase(result);
	}

	TMPhrase makeList() {
		if (fields.size() != 1 || first().isList()) throw new IllegalStateException();

		return new TMPhrase(first().makeList());
	}

	TMPhrase withName(String name) {
		if (!isUnnamedField()) throw new IllegalStateException();

		return new TMPhrase(first().withName(name));
	}

	static TMPhrase merge(List<TMPhrase> phrases,
						  SourceElement anchor,
						  ProcessingStatus status) {
		if (phrases.stream().allMatch(TMPhrase::isMergeable)) {
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
				return TMPhrase.empty();
			}
			TMField field = TMField.merge(
					fields.toArray(new TMField[fields.size()]));
			if (nullable) {
				field = field.makeNullable();
			}
			return new TMPhrase(field);
		}

		Map<String, List<TMField>> bySignature = new LinkedHashMap<>();
		UniqueOrder<String> nameOrder = new UniqueOrder<>();

		Map<String, TMField> seen = new HashMap<>();
		for (TMPhrase p : phrases) {
			seen.clear();
			for (TMField f : p.fields) {
				String signature = f.getSignature();
				TMField existing = seen.putIfAbsent(signature, f);
				if (existing != null) {
					status.report(ProcessingStatus.KIND_ERROR,
							"two fields with the same signature: " + existing.toString() +
									" -vs- " + f.toString(), anchor);
					continue;
				}
				if (f.hasExplicitName() && !f.isListElement()) {
					nameOrder.add(f.getName());
				}
				List<TMField> list = bySignature.get(signature);
				if (list == null) {
					bySignature.put(signature, list = new ArrayList<>());
				}
				list.add(f);
			}
			nameOrder.flush();
		}

		if (nameOrder.getResult(String[]::new) == null) {
			status.report(ProcessingStatus.KIND_ERROR,
					"named elements must occur in the same order in all productions", anchor);
		}

		int ind = 0;
		TMField[] result = new TMField[bySignature.size()];
		for (Entry<String, List<TMField>> entry : bySignature.entrySet()) {
			TMField composite;
			List<TMField> fields = entry.getValue();
			if (fields.size() == 1) {
				composite = fields.get(0);
			} else {
				composite = TMField.merge(fields.toArray(new TMField[fields.size()]));
				if (composite == null) {
					status.report(ProcessingStatus.KIND_ERROR,
							"Cannot merge " + new TMPhrase(fields).toString()
									+ " on " + entry.getKey(), anchor);
					continue;
				}
			}
			if (fields.size() < phrases.size()) {
				composite = composite.makeNullable();
			}
			result[ind++] = composite;
		}
		return ind == result.length ? new TMPhrase(result) : TMPhrase.empty();
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
					if (!(existing.isListElement() && f.isListElement())) {
						status.report(ProcessingStatus.KIND_ERROR,
								"two fields with the same signature: " + existing.toString() +
										" -vs- " + f.toString(), anchor);
						continue;
					}
					f = TMField.merge(existing, f);
					if (!f.isList()) {
						f = f.makeList();
					}
					seen.put(signature, f);
				}
			}
		}

		return new TMPhrase(new ArrayList<>(seen.values()));
	}

	static void verify(TMPhrase phrase,
					   SourceElement anchor,
					   ProcessingStatus status) {
		Set<String> namedTypes = new HashSet<>();
		for (TMField field : phrase.fields) {
			if (field.hasExplicitName() && !field.isListElement()) {
				namedTypes.addAll(Arrays.asList(field.getTypes()));
			}
		}
		Map<String, String> unnamedTypes = new HashMap<>();
		for (TMField field : phrase.fields) {
			if (field.hasExplicitName() && !field.isListElement()) continue;
			String signature = field.getSignature();

			for (String type : field.getTypes()) {
				if (namedTypes.contains(type)) {
					status.report(ProcessingStatus.KIND_ERROR,
							"`" + type + "` occurs in both named and unnamed fields", anchor);
					return;
				}

				String prev = unnamedTypes.putIfAbsent(type, signature);
				if (prev != null && !prev.equals(signature)) {
					status.report(ProcessingStatus.KIND_ERROR,
							"two unnamed fields share the same type `" + type + "`: " +
									prev + " -vs- " + signature, anchor);
					return;
				}
			}
		}
	}

	@Override
	public String toString() {
		return fields.stream()
				.map(TMField::toString)
				.collect(Collectors.joining(" "));
	}
}
