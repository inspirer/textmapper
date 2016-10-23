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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class TMRangeField implements RangeField {

	private final String name;
	private final String[] types;
	private final boolean isNamed;
	private final boolean isList;
	private final boolean nullable;
	private String signature;

	TMRangeField(String type) {
		this(type, new String[]{type}, false, false, false);
	}

	private TMRangeField(String name, String[] types, boolean isNamed,
						 boolean list, boolean nullable) {
		this.name = name;
		this.types = types;
		this.isNamed = isNamed;
		this.isList = list;
		this.nullable = nullable;
	}

	TMRangeField makeNullable() {
		if (isNullable()) return this;
		return new TMRangeField(name, types, isNamed, isList, true);
	}

	TMRangeField makeList() {
		if (isList()) throw new IllegalStateException();
		return new TMRangeField(name, types, isNamed, true, nullable);
	}

	TMRangeField withName(String newName, boolean explicit) {
		return new TMRangeField(newName, types, explicit, isList, nullable);
	}

	boolean isMergeable() {
		return !isNamed && !isList;
	}

	/**
	 *  Merges fields that are either {@code isMergeable()}, or share the same signature.
	 *
	 *  Returns null if nameHint is not provided and fields have different names.
	 */
	static TMRangeField merge(String nameHint, TMRangeField... fields) {
		if (fields.length == 0) {
			throw new IllegalArgumentException("fields is empty");
		}
		boolean mergeable = true;
		boolean sameSignature = true;
		for (TMRangeField field : fields) {
			if (fields[0].isList != field.isList || fields[0].isNamed != field.isNamed) {
				throw new IllegalArgumentException("inconsistent properties");
			}
			if (field.isNamed && !field.getName().equals(fields[0].name)) {
				throw new IllegalArgumentException("different names");
			}
			mergeable &= field.isMergeable();
			sameSignature &= fields[0].getSignature().equals(field.getSignature());
		}
		if (!mergeable && !sameSignature) throw new IllegalArgumentException();

		Set<String> types = new HashSet<>();
		boolean nullable = false;
		for (TMRangeField field : fields) {
			if (nameHint == null && !field.getName().equals(fields[0].name)) {
				return null;
			}
			types.addAll(Arrays.asList(field.types));
			nullable |= field.nullable;
		}
		String[] arr = types.toArray(new String[types.size()]);
		Arrays.sort(arr);
		return new TMRangeField(fields[0].isNamed || nameHint == null ? fields[0].name : nameHint,
				arr, fields[0].isNamed, fields[0].isList, nullable);
	}

	String getSignature() {
		if (signature != null) return signature;
		if (isNamed) {
			signature = name + (isList ? "+=" : "=");
		} else {
			signature = asString(false);
		}
		return signature;
	}

	@Override
	public String toString() {
		boolean hasName = types.length != 1 || !types[0].equals(name);
		return asString(hasName);
	}

	private String asString(boolean named) {
		StringBuilder sb = new StringBuilder();
		if (named) {
			sb.append(name);
			sb.append('=');
		}
		boolean needParentheses = types.length > 1 || isList;
		if (needParentheses) sb.append('(');
		boolean first = true;
		for (String type : types) {
			if (first) {
				first = false;
			} else {
				sb.append(" | ");
			}
			sb.append(type);
		}
		if (needParentheses) sb.append(')');
		if (isList && nullable) {
			sb.append('*');
		} else if (isList) {
			sb.append('+');
		} else if (nullable) {
			sb.append('?');
		}
		return sb.toString();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] getTypes() {
		return types;
	}

	@Override
	public boolean hasExplicitName() {
		return isNamed;
	}

	@Override
	public boolean isList() {
		return isList;
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}
}
