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

public class CommaNode extends ExpressionNode {

	final ExpressionNode leftExpr;
	final ExpressionNode rightExpr;

	public CommaNode(ExpressionNode leftExpr, ExpressionNode rightExpr,
					 TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		env.evaluate(leftExpr, context, true);
		return env.evaluate(rightExpr, context, true);
	}

	@Override
	public void toString(StringBuilder sb) {
		leftExpr.toString(sb);
		sb.append(", ");
		rightExpr.toString(sb);
	}

	@Override
	public void toJavascript(StringBuilder sb) {
		sb.append("## /* TODO conversion error */");
	}
}
