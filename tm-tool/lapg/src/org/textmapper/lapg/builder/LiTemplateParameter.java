/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

import org.textmapper.lapg.api.*;

public class LiTemplateParameter extends LiNamedElement
		implements TemplateParameter, DerivedSourceElement {

	private final Type type;
	private final Name name;
	private final Object defaultValue;
	private final SourceElement origin;
	private final Modifier m;
	private final int index;

	public LiTemplateParameter(int index, Type type, Name name, Object defaultValue,
							   TemplateParameter.Modifier m, SourceElement origin) {
		this.index = index;
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.m = m;
		this.origin = origin;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Modifier getModifier() {
		return m;
	}

	@Override
	public Name getName() {
		return name;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void appendSuffix(StringBuilder sb, Object value) {
		if (type == Type.Flag && !value.equals(Boolean.TRUE)) {
			return;
		}
		sb.append("_");
		sb.append(name.camelCase(true));
		if (value instanceof Symbol) {
			sb.append("_");
			sb.append(((Symbol) value).getNameText());
		}
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return name + " (parameter)";
	}
}
