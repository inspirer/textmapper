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
package org.textmapper.templates.types.ast;

import java.util.List;
import org.textmapper.templates.types.TypesTree.TextSource;

public class AstFeatureDeclaration extends AstNode implements IAstMemberDeclaration {

	private final AstTypeEx typeEx;
	private final String name;
	private final List<AstConstraint> modifiers;
	private final IAstExpression defaultval;

	public AstFeatureDeclaration(AstTypeEx typeEx, String name, List<AstConstraint> modifiers, IAstExpression defaultval, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.typeEx = typeEx;
		this.name = name;
		this.modifiers = modifiers;
		this.defaultval = defaultval;
	}

	public AstTypeEx getTypeEx() {
		return typeEx;
	}

	public String getName() {
		return name;
	}

	public List<AstConstraint> getModifiers() {
		return modifiers;
	}

	public IAstExpression getDefaultval() {
		return defaultval;
	}

	@Override
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (typeEx != null) {
			typeEx.accept(v);
		}
		if (modifiers != null) {
			for (AstConstraint it : modifiers) {
				it.accept(v);
			}
		}
		if (defaultval != null) {
			defaultval.accept(v);
		}
	}
}
