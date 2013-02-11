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
package org.textmapper.tool.parser.ast;

import org.textmapper.tool.parser.TMTree.TextSource;

import java.util.List;

public class TmaRhsPrefix extends AstNode {

	private final AstAnnotations annotations;
	private final AstIdentifier name;
	private final List<AstReference> extendedNonterms;

	public TmaRhsPrefix(AstAnnotations annotations, AstIdentifier name, List<AstReference> extendedNonterms, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.annotations = annotations;
		this.name = name;
		this.extendedNonterms = extendedNonterms;
	}

	public AstAnnotations getAnnotations() {
		return annotations;
	}

	public AstIdentifier getName() {
		return name;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (annotations != null) {
			annotations.accept(v);
		}
		if (name != null) {
			name.accept(v);
		}
		if (extendedNonterms != null) {
			for (AstReference extendedNonterm : extendedNonterms) {
				extendedNonterm.accept(v);
			}
		}
	}
}
