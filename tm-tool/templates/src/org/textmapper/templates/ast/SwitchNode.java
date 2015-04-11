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

import java.util.List;

import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.ast.TemplatesTree.TextSource;

public class SwitchNode extends Node {

	private final List<CaseNode> cases;
	private final ExpressionNode expression;
	private final List<Node> elseInstructions;

	public SwitchNode(ExpressionNode expression, List<CaseNode> cases, List<Node> elseInstructions, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.expression = expression;
		this.cases = cases;
		this.elseInstructions = elseInstructions;
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			Object value = env.evaluate(expression, context, false);
			for( CaseNode n : cases ) {
				Object caseValue = env.evaluate(n.getExpression(), context, false);
				if( ConditionalNode.safeEqual(value, caseValue)) {
					n.emit(sb, context, env);
					return;
				}
			}
			for (Node n : elseInstructions) {
				n.emit(sb, context, env);
			}
		} catch( EvaluationException ex ) {
			/* ignore, statement will be skipped */
		}
	}
}
