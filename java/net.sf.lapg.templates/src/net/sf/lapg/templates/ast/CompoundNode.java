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
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class CompoundNode extends Node {

	protected CompoundNode(String input, int line) {
		super(input, line);
	}

	protected ArrayList<Node> instructions;

	public ArrayList<Node> getInstructions() {
		return instructions;
	}

	public void setInstructions(ArrayList<Node> instructions) {
		this.instructions = instructions;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationStrategy env) {
		if (instructions != null) {
			for (Node n : instructions) {
				n.emit(sb, context, env);
			}
		}
	}
}
