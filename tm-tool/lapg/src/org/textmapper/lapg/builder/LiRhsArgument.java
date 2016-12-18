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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.rule.RhsArgument;

public class LiRhsArgument implements RhsArgument, DerivedSourceElement {

	private final TemplateParameter parameter;
	private final TemplateParameter source;
	private final Object value;
	private final SourceElement origin;

	public LiRhsArgument(TemplateParameter parameter, TemplateParameter source,
						 Object value, SourceElement origin) {
		this.parameter = parameter;
		this.source = source;
		this.value = value;
		this.origin = origin;
	}

	@Override
	public TemplateParameter getParameter() {
		return parameter;
	}

	@Override
	public TemplateParameter getSource() {
		return source;
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
		if (source != null ? !source.equals(that.source) : that.source != null) return false;
		return value != null ? value.equals(that.value) : that.value == null;

	}

	@Override
	public int hashCode() {
		int result = parameter.hashCode();
		result = 31 * result + (source != null ? source.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	public void toString(StringBuilder sb) {
		if (source != null) {
			sb.append(parameter.getNameText());
			if (source != parameter) {
				sb.append(":").append(source.getNameText());
			}
		} else if (value instanceof Boolean) {
			sb.append((boolean) value ? "+" : "~");
			sb.append(parameter.getNameText());
		} else if (value instanceof Symbol || value == null) {
			sb.append(parameter.getNameText());
			sb.append(":");
			sb.append(value == null ? "null" : ((Symbol) value).getNameText());
		} else {
			throw new IllegalStateException();
		}
	}
}
