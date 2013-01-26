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

import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.ast.*;
import org.textmapper.lapg.api.builder.AstBuilder;

import java.util.ArrayList;
import java.util.List;

public class LiAstBuilder implements AstBuilder {

	private final List<LiAstClass> classes = new ArrayList<LiAstClass>();
	private final List<LiAstEnum> enums = new ArrayList<LiAstEnum>();

	void check(AstType type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		// TODO
	}

	@Override
	public AstType rawType(String type, SourceElement origin) {
		return new LiRawAstType(type, origin);
	}

	@Override
	public AstList list(AstType inner, boolean nonEmpty, SourceElement origin) {
		return new LiAstList(inner, nonEmpty, origin);
	}

	@Override
	public AstField addField(String name, AstType type, boolean nullable, AstClass container, SourceElement origin) {
		check(container);
		LiAstField result = new LiAstField(name, type, nullable, container, origin);
		((LiAstClass) container).addField(result);
		return result;
	}

	@Override
	public AstClass addClass(String name, AstClass container, SourceElement origin) {
		if (container != null) {
			check(container);
		}
		LiAstClass result = new LiAstClass(name, container, origin);
		if (container != null) {
			((LiAstClass) container).addInner(result);
		} else {
			classes.add(result);
		}
		return result;
	}

	@Override
	public void addExtends(AstClass cl, AstClass baseClass) {
		check(cl);
		check(baseClass);
		((LiAstClass) cl).addSuper(baseClass);
	}

	@Override
	public AstEnum addEnum(String name, SourceElement origin) {
		// TODO
		return null;
	}

	@Override
	public AstEnumMember addMember(String name, AstEnum container, SourceElement origin) {
		// TODO
		return null;
	}

	@Override
	public AstModel create(SourceElement origin) {
		return new LiAstModel(classes.toArray(new AstClass[classes.size()]), enums.toArray(new AstEnum[enums.size()]), origin);
	}
}
