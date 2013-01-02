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

public class AstStructuralExpression extends AstNode implements IAstExpression {

	private List<AstStructuralExpressionDOLLAR1Item> mapEntries;
	private List<String> name;
	private List<IAstExpression> expressionListopt;

	public AstStructuralExpression(List<AstStructuralExpressionDOLLAR1Item> mapEntries, List<String> name, List<IAstExpression> expressionListopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.mapEntries = mapEntries;
		this.name = name;
		this.expressionListopt = expressionListopt;
	}

	public List<AstStructuralExpressionDOLLAR1Item> getMapEntries() {
		return mapEntries;
	}
	public List<String> getName() {
		return name;
	}
	public List<IAstExpression> getExpressionListopt() {
		return expressionListopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		if (mapEntries != null) {
			for (AstStructuralExpressionDOLLAR1Item it : mapEntries) {
				it.accept(v);
			}
		}
		// TODO for name
		if (expressionListopt != null) {
			for (IAstExpression it : expressionListopt) {
				it.accept(v);
			}
		}
	}
}
