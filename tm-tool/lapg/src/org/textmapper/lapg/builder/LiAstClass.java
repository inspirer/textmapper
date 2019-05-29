/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
import org.textmapper.lapg.api.ast.AstClass;
import org.textmapper.lapg.api.ast.AstClassifier;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.ast.AstType;

import java.util.*;

class LiAstClass extends LiUserDataHolder implements AstClass, DerivedSourceElement {

	private final String name;
	private final boolean isInterface;
	private final AstClass container;
	private final SourceElement origin;
	private final List<AstField> fields = new ArrayList<>();
	private final List<AstClassifier> inner = new ArrayList<>();
	private final Set<AstClass> superClasses = new LinkedHashSet<>();

	public LiAstClass(String name, boolean isInterface, AstClass container, SourceElement origin) {
		this.name = name;
		this.isInterface = isInterface;
		this.container = container;
		this.origin = origin;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public AstClass getContainingClass() {
		return container;
	}

	@Override
	public boolean isInterface() {
		return isInterface;
	}

	@Override
	public AstClass[] getSuper() {
		return superClasses.toArray(new AstClass[superClasses.size()]);
	}

	void addSuper(AstClass cl) {
		if (isInterface && !(cl.isInterface())) {
			throw new IllegalArgumentException("interfaces cannot extend classes");
		}
		superClasses.add(cl);
	}

	@Override
	public AstField[] getFields() {
		return fields.toArray(new AstField[fields.size()]);
	}

	void addField(AstField field) {
		fields.add(field);
	}

	@Override
	public AstClassifier[] getInner() {
		return inner.toArray(new AstClassifier[inner.size()]);
	}

	void addInner(AstClassifier cl) {
		inner.add(cl);
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public boolean isSubtypeOf(AstType another) {
		if (another == AstType.ANY) return true;
		if (!(another instanceof AstClass)) return false;
		AstClass cl = (AstClass) another;
		if (this == cl) return true;

		LinkedList<AstClass> queue = new LinkedList<>();
		Set<AstClass> seen = new HashSet<>();
		queue.addAll(Arrays.asList(getSuper()));
		seen.addAll(queue);
		seen.add(this);
		AstClass next;
		while ((next = queue.poll()) != null) {
			if (cl == next) {
				return true;
			}
			for (AstClass s : next.getSuper()) {
				if (seen.add(s)) {
					queue.add(s);
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
}
