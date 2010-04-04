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
package net.sf.lapg.templates.ast;

import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class ConcreteMapNode extends ExpressionNode {

	private final HashMap<String,ExpressionNode> fields;

	public ConcreteMapNode(HashMap<String,ExpressionNode> fields, String input, int line) {
		super(input, line);
		this.fields = fields;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		HashMap<Object,Object> result = new HashMap<Object,Object>();
		for( Map.Entry<String,ExpressionNode> entry : fields.entrySet() ) {
			result.put(entry.getKey(), env.evaluate(entry.getValue(), context, false) );
		}
		return result;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append('[');
		boolean notFirst = false;
		for( Map.Entry<String,ExpressionNode> entry : fields.entrySet() ) {
			if(notFirst) {
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
