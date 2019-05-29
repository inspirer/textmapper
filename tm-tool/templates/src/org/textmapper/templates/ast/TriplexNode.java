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


public class TriplexNode extends ExpressionNode {

	private final ExpressionNode elsenode;
	private final ExpressionNode thennode;
	private final ExpressionNode condition;

	protected TriplexNode(ExpressionNode condition, ExpressionNode thennode, ExpressionNode elsenode, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.condition = condition;
		this.thennode = thennode;
		this.elsenode = elsenode;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object cond = env.evaluate(condition, context, true);
		if (env.asAdaptable(cond).asBoolean()) {
			return env.evaluate(thennode, context, true);
		} else {
			return env.evaluate(elsenode, context, true);
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		condition.toString(sb);
		sb.append(" ? ");
		thennode.toString(sb);
		sb.append(" : ");
		elsenode.toString(sb);
	}

}
