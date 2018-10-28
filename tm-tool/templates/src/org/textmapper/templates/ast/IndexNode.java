/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.templates.ast;

import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.ast.TemplatesTree.TextSource;


public class IndexNode extends ExpressionNode {

	private final ExpressionNode objectExpr;
	private final ExpressionNode indexExpr;

	public IndexNode(ExpressionNode objectExpr, ExpressionNode index, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.objectExpr = objectExpr;
		this.indexExpr = index;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object object = objectExpr != null ? env.evaluate(objectExpr, context, false) : context.getThisObject();
		Object index = env.evaluate(indexExpr, context, false);

		return env.asObject(object).getByIndex(this, index);
	}

	@Override
	public void toString(StringBuilder sb) {
		if( objectExpr == null ) {
			sb.append("self");
		} else {
			objectExpr.toString(sb);
		}
		sb.append("[");
		indexExpr.toString(sb);
		sb.append("]");
	}
}
