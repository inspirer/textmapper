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
package org.textmapper.lapg.parser.ast;

import org.textmapper.lapg.parser.LapgTree.TextSource;

public class AstRefRulePart extends AstNode implements AstRulePart {

	private final String alias;
	private final AstRuleSymbolRef ref;
	private final AstRuleAnnotations annotations;

	public AstRefRulePart(String alias, AstRuleSymbolRef ref, AstRuleAnnotations annotations, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.alias = alias;
		this.ref = ref;
		this.annotations = annotations;
	}

	public AstRuleSymbolRef getReference() {
		return ref;
	}

	public String getAlias() {
		return alias;
	}

	public AstRuleAnnotations getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (ref != null) {
			ref.accept(v);
		}
		if (annotations != null) {
			annotations.accept(v);
		}
	}
}
