/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

import org.textmapper.lapg.api.TemplateEnvironment;
import org.textmapper.lapg.api.TemplateParameter;

import java.util.HashMap;
import java.util.Map;

public class LiTemplateEnvironment implements TemplateEnvironment {

	private final Map<TemplateParameter, Object> values;

	LiTemplateEnvironment() {
		values = new HashMap<TemplateParameter, Object>();
	}

	private LiTemplateEnvironment(LiTemplateEnvironment env, TemplateParameter param, Object value) {
		values = new HashMap<TemplateParameter, Object>(env.values);
		if (value == null) {
			values.remove(param);
		} else {
			values.put(param, value);
		}
	}

	@Override
	public Object getValue(TemplateParameter param) {
		Object val = values.get(param);
		return val != null ? val : param.getDefaultValue();
	}

	@Override
	public TemplateEnvironment extend(TemplateParameter param, Object value) {
		return new LiTemplateEnvironment(this, param, value);
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
