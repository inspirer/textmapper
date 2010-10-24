/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.AstTree.TextSource;

public class SelectNode extends ExpressionNode {

	private final ExpressionNode objectExpr;
	private final String identifier;

	public SelectNode(ExpressionNode objectExpr, String identifier, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.objectExpr = objectExpr;
		this.identifier = identifier;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object object;
		if( objectExpr != null ) {
			object = env.evaluate(objectExpr, context, false);
		} else {
			Object value = context.getVariable(identifier);
			if( value != null ) {
				return value;
			}

			object = context.getThisObject();
		}

		return env.getProperty(object, identifier);
	}

	@Override
	public void toString(StringBuffer sb) {
		if( objectExpr != null ) {
			objectExpr.toString(sb);
			sb.append(".");
		}
		sb.append(identifier);
	}
}
