/**
 * Copyright 2002-2015 Evgeny Gryaznov
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

public class AssignNode extends ExpressionNode {

	private final String identifier;
	private final ExpressionNode valueExpr;

	public AssignNode(String identifier, ExpressionNode value, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.identifier = identifier;
		this.valueExpr = value;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object value = env.evaluate(valueExpr, context, true);
		context.setVariable(identifier, value != null ? value : EvaluationContext.NULL_VALUE);
		return value;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(identifier);
		sb.append(" = ");
		valueExpr.toString(sb);
	}
}
