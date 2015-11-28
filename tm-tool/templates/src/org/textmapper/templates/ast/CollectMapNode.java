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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CollectMapNode extends ExpressionNode {

	private final ExpressionNode selectExpression;
	private final String varName;
	private final ExpressionNode key;
	private final ExpressionNode value;

	public CollectMapNode(ExpressionNode select, String varName,
						  ExpressionNode key, ExpressionNode value,
						  TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.selectExpression = select;
		this.varName = varName;
		this.key = key;
		this.value = value;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		Object select = env.evaluate(selectExpression, context, false);
		Iterator<?> it = env.asAdaptable(select).asSequence();
		if (it == null) {
			throw new EvaluationException("`" + selectExpression.toString()
					+ "` should be array or collection (instead of "
					+ select.getClass().getCanonicalName() + ")");
		}

		Map<Object, Object> result = new HashMap<>();
		while (it.hasNext()) {
			Object curr = it.next();
			EvaluationContext innerContext = new EvaluationContext(
					context.getThisObject(), null, context);
			innerContext.setVariable(varName, curr != null ? curr : EvaluationContext.NULL_VALUE);
			Object key_ = env.evaluate(key, innerContext, false);
			Object value_ = env.evaluate(value, innerContext, false);
			if (key_ instanceof Collection<?>) {
				for (Object k : (Collection<?>) key_) {
					result.put(k, value_);
				}
			} else if (key_ instanceof Object[]) {
				for (Object k : (Object[]) key_) {
					result.put(k, value_);
				}
			} else {
				result.put(key_, value_);
			}
		}
		return result;
	}

	@Override
	public void toString(StringBuilder sb) {
		selectExpression.toString(sb);
		sb.append(".collect").append("(").append(varName).append("|");
		key.toString(sb);
		sb.append(":");
		value.toString(sb);
		sb.append(")");
	}

}
