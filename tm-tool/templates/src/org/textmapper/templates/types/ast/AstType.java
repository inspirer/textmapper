/**
 * Copyright 2002-2017 Evgeny Gryaznov
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

public class AstType extends AstNode {

	private final AstType.AstKindKind kind;
	private final List<String> name;
	private final boolean isReference;
	private final boolean isClosure;
	private final List<AstTypeEx> parameters;

	public AstType(AstType.AstKindKind kind, List<String> name, boolean isReference, boolean isClosure, List<AstTypeEx> parameters, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.kind = kind;
		this.name = name;
		this.isReference = isReference;
		this.isClosure = isClosure;
		this.parameters = parameters;
	}

	public AstType.AstKindKind getKind() {
		return kind;
	}

	public List<String> getName() {
		return name;
	}

	public boolean isReference() {
		return isReference;
	}

	public boolean isClosure() {
		return isClosure;
	}

	public List<AstTypeEx> getParameters() {
		return parameters;
	}

	@Override
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (parameters != null) {
			for (AstTypeEx it : parameters) {
				it.accept(v);
			}
		}
	}

	public enum AstKindKind {
		LINT,
		LBOOL,
		LSTRING,
	}
}
