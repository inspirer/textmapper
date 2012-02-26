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
package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstStructuralExpression extends AstNode implements IAstExpression {

	private List<String> name;
	private List<AstMapEntriesItem> mapEntriesopt;
	private List<IAstExpression> expressionListopt;

	public AstStructuralExpression(List<String> name, List<AstMapEntriesItem> mapEntriesopt, List<IAstExpression> expressionListopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.mapEntriesopt = mapEntriesopt;
		this.expressionListopt = expressionListopt;
	}

	public List<String> getName() {
		return name;
	}
	public List<AstMapEntriesItem> getMapEntriesopt() {
		return mapEntriesopt;
	}
	public List<IAstExpression> getExpressionListopt() {
		return expressionListopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for name
		if (mapEntriesopt != null) {
			for (AstMapEntriesItem it : mapEntriesopt) {
				it.accept(v);
			}
		}
		if (expressionListopt != null) {
			for (IAstExpression it : expressionListopt) {
				it.accept(v);
			}
		}
	}
}
