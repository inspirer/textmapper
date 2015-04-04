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
import org.textmapper.lapg.common.FormatUtil;

public class LiTemplateParameter extends LiUserDataHolder implements TemplateParameter, DerivedSourceElement {

	private final Type type;
	private final String name;
	private final Object defaultValue;
	private final SourceElement origin;

	public LiTemplateParameter(Type type, String name, Object defaultValue, SourceElement origin) {
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.origin = origin;
	}

	@Override
	public Type getType() {
		return type;
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
		sb.append("_");
		if (value == Boolean.FALSE) {
			sb.append("non");
		}
		sb.append(getName());
		if (value instanceof Integer) {
			sb.append(value.toString());
		} else if (value instanceof String) {
			String s = (String) value;
			sb.append("-");
			for (int i = 0; i < s.length(); i++) {
				int c = s.charAt(i);
				if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '-' || c >= '0' && c <= '9') {
					sb.append((char) c);
				} else {
					sb.append(FormatUtil.getCharacterName((char) c));
				}
			}
		} else if (value instanceof Symbol) {
			sb.append("-");
			sb.append(((Symbol) value).getName());
		}
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		return name + ":" + type;
	}
}
