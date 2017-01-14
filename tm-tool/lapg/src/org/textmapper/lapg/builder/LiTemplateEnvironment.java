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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.NamedElement;
import org.textmapper.lapg.api.TemplateEnvironment;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.TemplateParameter.Modifier;

import java.util.*;

public class LiTemplateEnvironment implements TemplateEnvironment {

	public static final Comparator<TemplateParameter> TEMPLATE_PARAMETER_COMPARATOR =
			Comparator.comparing(NamedElement::getNameText);

	private final Map<TemplateParameter, Object> values;
	private String nonterminalSuffix;

	LiTemplateEnvironment() {
		values = new HashMap<>();
	}

	private LiTemplateEnvironment(Map<TemplateParameter, Object> map) {
		values = map;
	}

	@Override
	public Object getValue(TemplateParameter param) {
		Object val = values.get(param);
		return val != null ? val : param.getDefaultValue();
	}

	@Override
	public TemplateEnvironment extend(TemplateParameter param, Object value) {
		Map<TemplateParameter, Object> result;
		if (value == null || value.equals(param.getDefaultValue())) {
			if (!values.containsKey(param)) {
				return this;
			}
			result = new HashMap<>(values);
			result.remove(param);
		} else {
			if (value.equals(values.get(param))) {
				return this;
			}
			result = new HashMap<>(values);
			result.put(param, value);
		}

		return new LiTemplateEnvironment(result);
	}

	@Override
	public String getNonterminalSuffix() {
		if (nonterminalSuffix != null) return nonterminalSuffix;

		List<TemplateParameter> params = new ArrayList<>(values.keySet());
		Collections.sort(params, TEMPLATE_PARAMETER_COMPARATOR);
		StringBuilder sb = new StringBuilder();

		for (TemplateParameter param : params) {
			param.appendSuffix(sb, values.get(param));
		}
		return nonterminalSuffix = sb.toString();
	}

	@Override
	public TemplateEnvironment filter(ParameterPredicate predicate) {
		TemplateEnvironment env = this;
		for (TemplateParameter param : values.keySet()) {
			if (!predicate.include(param)) env = env.extend(param, null);
		}
		return env;
	}

	@Override
	public boolean hasLookahead() {
		for (TemplateParameter param : values.keySet()) {
			if (param.getModifier() == Modifier.Lookahead) return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiTemplateEnvironment that = (LiTemplateEnvironment) o;
		return values.equals(that.values);
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}
}
