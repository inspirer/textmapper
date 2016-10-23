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

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class TMRangePhrase {
	final List<TMRangeField> fields;

	TMRangePhrase(TMRangeField... fields) {
		this.fields = Arrays.asList(fields);
	}

	TMRangePhrase(List<TMRangeField> fields) {
		this.fields = fields;
	}

	static TMRangePhrase empty() {
		return new TMRangePhrase();
	}

	static TMRangePhrase type(String rangeType) {
		return new TMRangePhrase(new TMRangeField(rangeType));
	}

	boolean isSingleElement() {
		return fields.size() == 1 && fields.get(0).isMergeable();
	}

	TMRangePhrase makeNullable() {
		if (fields.isEmpty()) return this;

		TMRangeField[] result = new TMRangeField[fields.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = fields.get(i).makeNullable();
		}
		return new TMRangePhrase(result);
	}

	static TMRangePhrase merge(String newName, List<TMRangePhrase> phrases,
							   SourceElement anchor,
							   ProcessingStatus status) {
		if (phrases.stream().allMatch(TMRangePhrase::isSingleElement)) {
			return new TMRangePhrase(TMRangeField.merge(newName,
					phrases.stream()
							.map(p -> p.fields.get(0))
							.toArray(TMRangeField[]::new)));
		}

		Map<String, List<TMRangeField>> bySignature = new LinkedHashMap<>();

		Set<String> seen = new HashSet<>();
		for (TMRangePhrase p : phrases) {
			seen.clear();
			for (TMRangeField f : p.fields) {
				String signature = f.getSignature();
				if (!seen.add(signature)) {
					status.report(ProcessingStatus.KIND_ERROR,
							"two fields with the same signature `" + signature + "` in " +
									newName, anchor);
					continue;
				}
				List<TMRangeField> list = bySignature.get(signature);
				if (list == null) {
					bySignature.put(signature, list = new ArrayList<>());
				}
				list.add(f);
			}
		}

		int ind = 0;
		TMRangeField[] result = new TMRangeField[bySignature.size()];
		for (Entry<String, List<TMRangeField>> entry : bySignature.entrySet()) {
			TMRangeField composite;
			List<TMRangeField> fields = entry.getValue();
			if (fields.size() == 1) {
				composite = fields.get(0);
			} else {
				composite = TMRangeField.merge(null,
						fields.toArray(new TMRangeField[fields.size()]));
				if (composite == null) {
					status.report(ProcessingStatus.KIND_ERROR,
							"Cannot merge " + fields.size() + " fields in " + newName + ": " +
									entry.getKey(), anchor);
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

	@Override
	public String toString() {
		return fields.stream()
				.map(TMRangeField::toString)
				.collect(Collectors.joining(" "));
	}
}
