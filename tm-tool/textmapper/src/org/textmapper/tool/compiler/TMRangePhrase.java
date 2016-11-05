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

class TMRangePhrase {
	final List<TMRangeField> fields;

	TMRangePhrase(TMRangeField... fields) {
		this.fields = Arrays.asList(fields);
	}

	private TMRangePhrase(List<TMRangeField> fields) {
		this.fields = fields;
	}

	TMRangeField first() {
		return fields.get(0);
	}

	static TMRangePhrase empty() {
		return new TMRangePhrase();
	}

	static TMRangePhrase type(String rangeType) {
		return new TMRangePhrase(new TMRangeField(rangeType));
	}

	boolean isSingleElement() {
		return fields.size() == 1 && first().isMergeable();
	}

	boolean isUnnamedField() {
		return fields.size() == 1 && !first().hasExplicitName();
	}

	TMRangePhrase makeNullable() {
		if (fields.isEmpty()) return this;

		TMRangeField[] result = new TMRangeField[fields.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = fields.get(i).makeNullable();
		}
		return new TMRangePhrase(result);
	}

	TMRangePhrase makeList() {
		if (fields.size() != 1 || first().isList()) throw new IllegalStateException();

		return new TMRangePhrase(first().makeList());
	}

	TMRangePhrase withName(String name) {
		if (!isUnnamedField()) throw new IllegalStateException();

		return new TMRangePhrase(first().withName(name));
	}

	static TMRangePhrase merge(List<TMRangePhrase> phrases,
							   SourceElement anchor,
							   ProcessingStatus status) {
		if (phrases.stream().allMatch((r) -> r.isEmpty() || r.isSingleElement())) {
			List<TMRangeField> fields = new ArrayList<>();
			boolean nullable = false;
			for (TMRangePhrase phrase : phrases) {
				if (phrase.isEmpty()) {
					nullable = true;
				} else {
					fields.add(phrase.first());
				}
			}
			if (fields.isEmpty()) {
				return TMRangePhrase.empty();
			}
			TMRangeField field = TMRangeField.merge(
					fields.toArray(new TMRangeField[fields.size()]));
			if (nullable) {
				field = field.makeNullable();
			}
			return new TMRangePhrase(field);
		}

		Map<String, List<TMRangeField>> bySignature = new LinkedHashMap<>();
		UniqueOrder<String> nameOrder = new UniqueOrder<>();

		Set<String> seen = new HashSet<>();
		for (TMRangePhrase p : phrases) {
			seen.clear();
			for (TMRangeField f : p.fields) {
				String signature = f.getSignature();
				if (!seen.add(signature)) {
					status.report(ProcessingStatus.KIND_ERROR,
							"two fields with the same signature `" + signature + "`", anchor);
					continue;
				}
				if (f.hasExplicitName() && !f.isListElement()) {
					nameOrder.add(f.getName());
				}
				List<TMRangeField> list = bySignature.get(signature);
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
		TMRangeField[] result = new TMRangeField[bySignature.size()];
		for (Entry<String, List<TMRangeField>> entry : bySignature.entrySet()) {
			TMRangeField composite;
			List<TMRangeField> fields = entry.getValue();
			if (fields.size() == 1) {
				composite = fields.get(0);
			} else {
				composite = TMRangeField.merge(fields.toArray(new TMRangeField[fields.size()]));
				if (composite == null) {
					status.report(ProcessingStatus.KIND_ERROR,
							"Cannot merge " + new TMRangePhrase(fields).toString()
									+ " on " + entry.getKey(), anchor);
					continue;
				}
			}
			if (fields.size() < phrases.size()) {
				composite = composite.makeNullable();
			}
			result[ind++] = composite;
		}
		return ind == result.length ? new TMRangePhrase(result) : TMRangePhrase.empty();
	}

	static TMRangePhrase concat(List<TMRangePhrase> phrases,
								SourceElement anchor,
								ProcessingStatus status) {
		Map<String, TMRangeField> seen = new LinkedHashMap<>();
		for (TMRangePhrase p : phrases) {
			for (TMRangeField f : p.fields) {
				String signature = f.getSignature();
				TMRangeField existing = seen.putIfAbsent(signature, f);
				if (existing != null) {
					if (!(existing.isListElement() && f.isListElement())) {
						status.report(ProcessingStatus.KIND_ERROR,
								"two fields with the same signature `" + signature + "`", anchor);
						continue;
					}
					f = TMRangeField.merge(existing, f);
					if (!f.isList()) {
						f = f.makeList();
					}
					seen.put(signature, f);
				}
			}
		}

		return new TMRangePhrase(new ArrayList<>(seen.values()));
	}

	static void verify(TMRangePhrase phrase,
					   SourceElement anchor,
					   ProcessingStatus status) {
		Set<String> namedTypes = new HashSet<>();
		for (TMRangeField field : phrase.fields) {
			if (field.hasExplicitName() && !field.isListElement()) {
				namedTypes.addAll(Arrays.asList(field.getTypes()));
			}
		}
		Map<String, String> unnamedTypes = new HashMap<>();
		for (TMRangeField field : phrase.fields) {
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
				.map(TMRangeField::toString)
				.collect(Collectors.joining(" "));
	}

	public boolean isEmpty() {
		return fields.isEmpty();
	}
}
