/**
 * Copyright 2002-2015 Evgeny Gryaznov
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

public class LiTemplateParameter extends LiUserDataHolder
		implements TemplateParameter, DerivedSourceElement {

	private final Type type;
	private final String name;
	private final Object defaultValue;
	private final SourceElement origin;
	private final Forward p;

	public LiTemplateParameter(Type type, String name, Object defaultValue,
							   Forward p, SourceElement origin) {
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.p = p;
		this.origin = origin;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Forward getFwdStrategy() {
		return p;
	}

	@Override
	public String getName() {
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
		sb.append(name.substring(name.indexOf('$') + 1));
		if (value instanceof Symbol) {
			sb.append("_");
			sb.append(((Symbol) value).getName());
		}
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return name + " " + type;
	}
}
