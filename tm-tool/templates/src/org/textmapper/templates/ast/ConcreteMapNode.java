/**
 * Copyright 2002-2019 Evgeny Gryaznov
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

import java.util.HashMap;
import java.util.Map;

public class ConcreteMapNode extends ExpressionNode {

	private final Map<String, ExpressionNode> fields;

	public ConcreteMapNode(Map<String, ExpressionNode> fields,
						   TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.fields = fields;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		HashMap<Object, Object> result = new HashMap<>();
		for (Map.Entry<String, ExpressionNode> entry : fields.entrySet()) {
			result.put(entry.getKey(), env.evaluate(entry.getValue(), context, false));
		}
		return result;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append('[');
		boolean notFirst = false;
		for (Map.Entry<String, ExpressionNode> entry : fields.entrySet()) {
			if (notFirst) {
				sb.append(",");
			} else {
				notFirst = true;
			}
			sb.append(entry.getKey());
			sb.append(":");
			entry.getValue().toString(sb);
		}
		sb.append(']');
	}
}
