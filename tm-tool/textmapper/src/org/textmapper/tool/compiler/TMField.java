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
import java.util.stream.Collectors;

class TMField implements RangeField {

	private final String name;
	private final String[] types;
	private final boolean hasExplicitName;
	private final boolean isList;
	private final boolean nullable;
	private String signature;

	TMField(String type) {
		this(type, new String[]{type}, false, false, false);
	}

	private TMField(String name, String[] types, boolean hasExplicitName,
					boolean list, boolean nullable) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.types = types;
		this.hasExplicitName = hasExplicitName;
		this.isList = list;
		this.nullable = nullable;
	}

	TMField makeNullable() {
		if (nullable) return this;
		return new TMField(name, types, hasExplicitName, isList, true);
	}

	TMField makeList() {
		if (isList) return this;

		return new TMField(name, types, hasExplicitName, true /* list */, nullable);
	}

	TMField withName(String newName) {
		if (newName == null) {
			throw new NullPointerException();
		}
		return new TMField(newName, types, true /* named */, isList, nullable);
	}

	String getSignature() {
		if (signature != null) return signature;

		signature = this.name +
				(isList ? "+" : "") +
				(hasExplicitName ? "=" : "");
		return signature;
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
		return hasExplicitName;
	}

	@Override
	public boolean isList() {
		return isList;
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (types.length != 1 || !types[0].equals(name)) {
			sb.append(name);
			sb.append("=");
		}
		boolean needParentheses = types.length > 1 || isList;
		if (needParentheses) sb.append('(');
		sb.append(Arrays.stream(types).collect(Collectors.joining(" | ")));
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

	private static boolean equalNames(TMField f1, TMField f2) {
		return f1.name.equals(f2.name);
	}

	/**
	 *  Merges fields that are either simple non-list elements, or share the same signature.
	 */
	static TMField merge(String nameHint, TMField... fields) {
		if (fields.length == 0) {
			throw new IllegalArgumentException("fields is empty");
		}

		Set<String> types = new HashSet<>();
		boolean nullable = false;
		boolean isList = false;
		boolean sameName = true;
		for (TMField field : fields) {
			sameName &= equalNames(field, fields[0]);
			isList |= field.isList;
			types.addAll(Arrays.asList(field.types));
			nullable |= field.nullable;
		}
		String[] arr = types.toArray(new String[types.size()]);
		Arrays.sort(arr);
		if (!sameName && nameHint == null) {
			throw new IllegalStateException();
		}
		return new TMField(sameName ? fields[0].name : nameHint, arr,
				sameName && fields[0].hasExplicitName,
				isList, nullable);
	}
}
