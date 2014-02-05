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

import org.textmapper.templates.types.TypesTree.TextSource;

public class AstListOfIdentifierAnd2ElementsCommaSeparatedItem extends AstNode {

	private final String identifier;
	private final AstMapSeparator mapSeparator;
	private final IAstExpression expression;

	public AstListOfIdentifierAnd2ElementsCommaSeparatedItem(String identifier, AstMapSeparator mapSeparator, IAstExpression expression, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.identifier = identifier;
		this.mapSeparator = mapSeparator;
		this.expression = expression;
	}

	public String getIdentifier() {
		return identifier;
	}

	public AstMapSeparator getMapSeparator() {
		return mapSeparator;
	}

	public IAstExpression getExpression() {
		return expression;
	}

	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (expression != null) {
			expression.accept(v);
		}
	}
}
