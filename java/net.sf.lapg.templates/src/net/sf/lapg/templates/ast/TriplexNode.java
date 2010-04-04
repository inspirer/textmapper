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

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;


public class TriplexNode extends ExpressionNode {

	private final ExpressionNode elsenode;
	private final ExpressionNode thennode;
	private final ExpressionNode condition;

	protected TriplexNode(ExpressionNode condition, ExpressionNode thennode, ExpressionNode elsenode, String input, int line) {
		super(input, line);
		this.condition = condition;
		this.thennode = thennode;
		this.elsenode = elsenode;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object cond = env.evaluate(condition, context, true);
		if( env.toBoolean(cond) ) {
			return env.evaluate(thennode, context, true);
		} else {
			return env.evaluate(elsenode, context, true);
		}
	}

	@Override
	public void toString(StringBuffer sb) {
		condition.toString(sb);
		sb.append(" ? ");
		thennode.toString(sb);
		sb.append(" : ");
		elsenode.toString(sb);
	}

}
