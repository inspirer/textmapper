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

	private final List<String> name;
	private final List<AstStructuralExpressionDOLLAR1Item> mapEntries;
	private final List<IAstExpression> expressionList;

	public AstStructuralExpression(List<String> name, List<AstStructuralExpressionDOLLAR1Item> mapEntries, List<IAstExpression> expressionList, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.name = name;
		this.mapEntries = mapEntries;
		this.expressionList = expressionList;
	}

	public List<String> getName() {
		return name;
	}

	public List<AstStructuralExpressionDOLLAR1Item> getMapEntries() {
		return mapEntries;
	}

	public List<IAstExpression> getExpressionList() {
		return expressionList;
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
		if (expressionList != null) {
			for (IAstExpression it : expressionList) {
				it.accept(v);
			}
		}
	}
}
