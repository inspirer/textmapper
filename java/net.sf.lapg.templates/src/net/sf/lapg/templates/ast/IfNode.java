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

import java.util.ArrayList;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class IfNode extends CompoundNode {
	private final ExpressionNode select;
	private ArrayList<Node> elseInstructions;

	public IfNode(ExpressionNode select, String input, int line) {
		super(input, line);
		this.select = select;
	}

	public ExpressionNode getSelect() {
		return select;
	}

	public void setElseInstructions(ArrayList<Node> elseInstructions) {
		this.elseInstructions = elseInstructions;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			ArrayList<Node> execute = env.toBoolean(env.evaluate(select, context, true)) ? instructions
					: elseInstructions;
			if (execute != null) {
				for (Node n : execute) {
					n.emit(sb, context, env);
				}
			}
		} catch (EvaluationException ex) {
			/* ignore, skip if */
		}
	}
}
