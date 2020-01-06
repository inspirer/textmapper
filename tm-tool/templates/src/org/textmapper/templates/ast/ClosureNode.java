/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
import org.textmapper.templates.types.TiClosure;

import java.util.List;

/**
 * Gryaznov Evgeny, 3/11/11
 */
public class ClosureNode extends ExpressionNode {

	private final ParameterNode[] parameters;
	private final ExpressionNode expression;
	private final boolean isCached;

	public ClosureNode(boolean cached, List<ParameterNode> parameters, ExpressionNode expression,
					   TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.parameters = parameters != null
				? parameters.toArray(new ParameterNode[parameters.size()]) : null;
		this.expression = expression;
		this.isCached = cached;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		return new TiClosure(isCached, parameters, expression, context, env);
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("{ ");
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				parameters[i].toString(sb);
			}
			sb.append(" ");
		}
		sb.append("=>");
		expression.toString(sb);
		sb.append(" }");
	}
}
