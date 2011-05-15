/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
import org.textway.templates.ast.TemplatesTree.TextSource;


public class FileNode extends CompoundNode {

	// TODO encoding option (utf-8 by default)
	private final ExpressionNode targetNameExpr;

	public FileNode(ExpressionNode expressionNode, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.targetNameExpr = expressionNode;
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		StringBuilder file = new StringBuilder();
		try {
			String fileName = env.toString(env.evaluate(targetNameExpr, context, false), targetNameExpr);
			super.emit(file, context, env);

			env.createStream(fileName, file.toString());
		} catch (EvaluationException ex) {
			/* ignore, skip if */
		}
	}
}
