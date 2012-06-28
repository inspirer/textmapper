/**
 * Copyright 2002-2012 Evgeny Gryaznov
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

	private String name;
	private AstTypeEx typeEx;
	private List<AstConstraint> modifiersopt;
	private IAstExpression defaultvalopt;

	public AstFeatureDeclaration(String name, AstTypeEx typeEx, List<AstConstraint> modifiersopt, IAstExpression defaultvalopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.typeEx = typeEx;
		this.modifiersopt = modifiersopt;
		this.defaultvalopt = defaultvalopt;
	}

	public String getName() {
		return name;
	}
	public AstTypeEx getTypeEx() {
		return typeEx;
	}
	public List<AstConstraint> getModifiersopt() {
		return modifiersopt;
	}
	public IAstExpression getDefaultvalopt() {
		return defaultvalopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for name
		if (typeEx != null) {
			typeEx.accept(v);
		}
		if (modifiersopt != null) {
			for (AstConstraint it : modifiersopt) {
				it.accept(v);
			}
		}
		if (defaultvalopt != null) {
			defaultvalopt.accept(v);
		}
	}
}
