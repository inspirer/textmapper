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
package org.textmapper.templates.types.ast;

import java.util.List;
import org.textmapper.templates.types.TypesTree.TextSource;

public class AstStringConstraint extends AstNode {

	private AstStringConstraint.AstKindKind kind;
	private List<Ast_String> strings;
	private String identifier;

	public AstStringConstraint(AstStringConstraint.AstKindKind kind, List<Ast_String> strings, String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.strings = strings;
		this.identifier = identifier;
	}

	public AstStringConstraint.AstKindKind getKind() {
		return kind;
	}
	public List<Ast_String> getStrings() {
		return strings;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for kind
		if (strings != null) {
			for (Ast_String it : strings) {
				it.accept(v);
			}
		}
		// TODO for identifier
	}

	public enum AstKindKind {
		LCHOICE,
		LSET,
	}
}
