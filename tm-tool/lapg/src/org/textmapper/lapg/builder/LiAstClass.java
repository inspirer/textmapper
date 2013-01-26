/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.lapg.api.ast.AstField;

import java.util.ArrayList;
import java.util.List;

class LiAstClass extends LiUserDataHolder implements AstClass, DerivedSourceElement {

	private final String name;
	private final AstClass container;
	private final SourceElement origin;
	private final List<AstField> fields = new ArrayList<AstField>();
	private final List<AstClass> inner = new ArrayList<AstClass>();
	private final List<AstClass> superClasses = new ArrayList<AstClass>();

	public LiAstClass(String name, AstClass container, SourceElement origin) {
		this.name = name;
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
	public AstClass[] getSuper() {
		return superClasses.toArray(new AstClass[superClasses.size()]);
	}

	void addSuper(AstClass cl) {
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
	public AstClass[] getInner() {
		return inner.toArray(new AstClass[inner.size()]);
	}

	void addInner(AstClass cl) {
		inner.add(cl);
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
