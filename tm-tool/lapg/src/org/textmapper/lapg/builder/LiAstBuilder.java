/**
 * Copyright 2002-2022 Evgeny Gryaznov
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

import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.ast.*;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.common.FormatUtil;

import java.util.*;

class LiAstBuilder implements AstBuilder {

	private final List<AstClassifier> classifiers = new ArrayList<>();
	private final Set<AstType> mine = new HashSet<>();

	private final Map<AstClassifier, Set<String>> usedNames = new HashMap<>();
	private final Set<String> usedGlobals = new HashSet<>();

	LiAstBuilder() {
	}

	private void check(AstType type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		if (!mine.contains(type)) {
			throw new IllegalArgumentException("unknown type");
		}
	}

	private void checkName(AstClassifier type, String childName) {
		Set<String> taken = type == null ? usedGlobals : usedNames.get(type);
		if (taken == null) {
			taken = new HashSet<>();
			usedNames.put(type, taken);
		}

		if (!taken.add(childName)) {
			throw new IllegalArgumentException("duplicate element `" + childName + "' in " + type);
		}
	}

	@Override
	public AstType rawType(String type, SourceElement origin) {
		return new LiRawAstType(type, origin);
	}

	@Override
	public AstList list(AstType inner, boolean nonEmpty, SourceElement origin) {
		if (inner == null) {
			throw new NullPointerException();
		}
		return new LiAstList(inner, nonEmpty, origin);
	}

	@Override
	public AstField addField(String name, AstType type, boolean nullable, AstClass container, SourceElement origin) {
		check(container);
		checkName(container, name);
		LiAstField result = new LiAstField(name, type, nullable, container, origin);
		((LiAstClass) container).addField(result);
		return result;
	}

	@Override
	public AstClass addClass(String name, AstClass container, SourceElement origin) {
		if (container != null) {
			check(container);
		}
		checkName(container, name);
		LiAstClass result = new LiAstClass(name, false, container, origin);
		if (container != null) {
			((LiAstClass) container).addInner(result);
		} else {
			classifiers.add(result);
		}
		mine.add(result);
		return result;
	}

	@Override
	public AstClass addInterface(String name, AstClass container, SourceElement origin) {
		if (container != null) {
			check(container);
		}
		checkName(container, name);
		LiAstClass result = new LiAstClass(name, true, container, origin);
		if (container != null) {
			((LiAstClass) container).addInner(result);
		} else {
			classifiers.add(result);
		}
		mine.add(result);
		return result;
	}

	@Override
	public void addExtends(AstClass cl, AstClass baseClass) {
		if (cl == baseClass) {
			return;
		}
		if (baseClass.isSubtypeOf(cl)) {
			throw new IllegalArgumentException("cannot add extends relation as it breaks the inheritance tree");
		}
		check(cl);
		check(baseClass);
		((LiAstClass) cl).addSuper(baseClass);
	}

	@Override
	public AstEnum addEnum(String name, AstClass container, SourceElement origin) {
		checkName(container, name);
		LiAstEnum result = new LiAstEnum(name, container, origin);
		if (container != null) {
			((LiAstClass) container).addInner(result);
		} else {
			classifiers.add(result);
		}
		mine.add(result);
		return result;
	}

	@Override
	public AstEnumMember addMember(String name, AstEnum container, SourceElement origin) {
		check(container);
		checkName(container, name);
		LiAstEnumMember member = new LiAstEnumMember(name, container, origin);
		((LiAstEnum) container).addMember(member);
		return member;
	}

	@Override
	public AstModel create() {
		return new LiAstModel(classifiers.toArray(new AstClassifier[classifiers.size()]));
	}

	@Override
	public String uniqueName(AstClassifier type, String baseName, boolean isMember) {
		assert FormatUtil.isIdentifier(baseName) : baseName;

		String name = type instanceof AstEnum
				? FormatUtil.toUpperWithUnderscores(baseName)
				: FormatUtil.toCamelCase(baseName, !isMember);
		Set<String> usedIdentifiers = type == null ? usedGlobals : usedNames.get(type);
		if (usedIdentifiers == null) {
			return name;
		}

		String result = name;
		int i = 2;
		while (usedIdentifiers.contains(result)) {
			result = name + i++;
		}
		return result;
	}
}
