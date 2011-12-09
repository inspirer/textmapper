/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.parser.ast;

import org.textway.lapg.parser.LapgTree.TextSource;

public class AstRuleSymbol extends AstNode implements AstRulePart {

	private final String alias;
	private final AstReference symbol;
	private final AstRuleAnnotations annotations;

	public AstRuleSymbol(String alias, AstReference symbol, AstRuleAnnotations annotations, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.alias = alias;
		this.symbol = symbol;
		this.annotations = annotations;
	}

	public AstReference getSymbol() {
		return symbol;
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
		if (symbol != null) {
			symbol.accept(v);
		}
		if (annotations != null) {
			annotations.accept(v);
		}
	}
}
