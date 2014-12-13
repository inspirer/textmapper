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

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.rule.RhsArgument;
import org.textmapper.lapg.common.FormatUtil;

public class LiRhsArgument implements RhsArgument, DerivedSourceElement {

	private final TemplateParameter parameter;
	private final Object value;
	private final SourceElement origin;

	public LiRhsArgument(TemplateParameter parameter, Object value, SourceElement origin) {
		this.parameter = parameter;
		this.value = value;
		this.origin = origin;
	}

	@Override
	public TemplateParameter getParameter() {
		return parameter;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsArgument that = (LiRhsArgument) o;

		if (!parameter.equals(that.parameter)) return false;
		if (!value.equals(that.value)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = parameter.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	public void toString(StringBuilder sb) {
		sb.append(parameter.getName());
		sb.append(":");
		if (value instanceof Symbol) {
			sb.append(((Symbol) value).getName());
		} else if (value instanceof Integer || value instanceof Boolean) {
			sb.append(value);
		} else if (value instanceof String) {
			sb.append('"');
			sb.append(FormatUtil.escape((String) value));
			sb.append('"');
		} else {
			throw new IllegalStateException();
		}
	}
}
