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
package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.AstTree.TextSource;

public class IfNode extends CompoundNode {
	private final ExpressionNode condition;
	private ElseIfNode elseClauses;

	public IfNode(ExpressionNode select, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.condition = select;
	}

	public ExpressionNode getSelect() {
		return condition;
	}

	public void applyElse(ElseIfNode elseNode) {
		this.elseClauses = elseNode;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			if(env.toBoolean(env.evaluate(condition, context, true))) {
				super.emit(sb, context, env);
				return;
			}
			if (elseClauses != null) {
				ElseIfNode eif = elseClauses;
				while(eif != null) {
					if(eif.getCondition() == null || env.toBoolean(env.evaluate(eif.getCondition(), context, true))) {
						eif.emit(sb, context, env);
						return;
					}
					eif = eif.getNext();
				}
			}
		} catch (EvaluationException ex) {
			/* ignore, skip if */
		}
	}
}
