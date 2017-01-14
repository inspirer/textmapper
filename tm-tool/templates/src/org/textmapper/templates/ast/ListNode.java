/**
 * Copyright 2002-2017 Evgeny Gryaznov
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

import java.util.List;

public class ListNode extends ExpressionNode {

	private final ExpressionNode[] expressions;

	public ListNode(List<ExpressionNode> expressions, TextSource source,
					int offset, int endoffset) {
		super(source, offset, endoffset);
		this.expressions = expressions != null && expressions.size() > 0
				? expressions.toArray(new ExpressionNode[expressions.size()]) : null;
	}

	public ExpressionNode[] getExpressions() {
		return expressions;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		Object[] result;
		if (expressions != null) {
			result = new Object[expressions.length];
			for (int i = 0; i < expressions.length; i++) {
				result[i] = env.evaluate(expressions[i], context, false);
			}
		} else {
			result = new Object[0];
		}
		return result;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append('[');
		if (expressions != null) {
			for (int i = 0; i < expressions.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				expressions[i].toString(sb);
			}
		}
		sb.append(']');
	}
}
