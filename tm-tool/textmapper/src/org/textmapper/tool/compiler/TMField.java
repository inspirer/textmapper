/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TMField implements RangeField {

	private final String name;
	private final String[] types;
	private final String interfaceType;
	private final RangeField comesAfterField;
	private final boolean hasExplicitName;
	private final boolean isList;
	private final boolean nullable;
	private String signature;
	private String text;

	TMField(String type) {
		this(type, new String[]{type}, false, false, false, null, null);
	}

	private TMField(String name, String[] types, boolean hasExplicitName,
					boolean list, boolean nullable, String interfaceType, RangeField comesAfterField) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.types = types;
		this.hasExplicitName = hasExplicitName;
		this.isList = list;
		this.nullable = nullable;
		this.interfaceType = interfaceType;
		this.comesAfterField = comesAfterField;
	}

	TMField resolve(Map<String, Set<String>> categories) {
		if (Arrays.stream(types).noneMatch(categories::containsKey)) return this;

		String newInterfaceType =
				(types.length == 1 && categories.containsKey(types[0])) ? types[0] : null;
		Set<String> newTypes = new HashSet<>();
		for (String type : types) {
			if (categories.containsKey(type)) {
				newTypes.addAll(categories.get(type));
			} else {
				newTypes.add(type);
			}
		}
		String[] newTypesArr = newTypes.toArray(new String[0]);
		Arrays.sort(newTypesArr);

		TMField result = new TMField(name, newTypesArr, hasExplicitName, isList, nullable,
				newInterfaceType, comesAfterField);
		result.text = toString();
		return result;
	}

	TMField withComesAfter(RangeField comesAfter) {
		TMField field = new TMField(name, types, hasExplicitName, isList, nullable,
				interfaceType,
				comesAfter);
		field.text = text;
		return field;
	}

	TMField makeNullable() {
		if (nullable) return this;
		return new TMField(name, types, hasExplicitName, isList, true, interfaceType,
				comesAfterField);
	}

	TMField makeList() {
		if (isList) return this;

		return new TMField(name, types, hasExplicitName, true /* list */, nullable, interfaceType,
				comesAfterField);
	}

	TMField withName(String newName) {
		if (newName == null) {
			throw new NullPointerException();
		}
		return new TMField(newName, types, true /* named */, isList, nullable, interfaceType,
				comesAfterField);
	}

	String getSignature() {
		if (signature != null) return signature;

		signature = this.name +
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
	public String getInterfaceType() {
		return interfaceType;
	}

	@Override
	public boolean hasExplicitName() {
		return hasExplicitName;
	}

	@Override
	public RangeField comesAfterField() {
		return comesAfterField;
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
		if (text != null) return text;
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
		return text = sb.toString();
	}

	private static boolean equalNames(TMField f1, TMField f2) {
		return f1.name.equals(f2.name);
	}

	/**
	 *  Merges fields that share either the type or the name.
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
		String[] arr = types.toArray(new String[0]);
		Arrays.sort(arr);
		if (!sameName && nameHint == null) {
			throw new IllegalStateException();
		}
		return new TMField(sameName ? fields[0].name : nameHint, arr,
				sameName && fields[0].hasExplicitName,
				isList, nullable, null, null);
	}
}
