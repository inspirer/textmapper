/**
 * Copyright 2002-2012 Evgeny Gryaznov
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

public class LiteralNode extends ExpressionNode {

	private final Object literal;

	public LiteralNode(Object literal, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.literal = literal;
	}

	public Object getLiteral() {
		return literal;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		return literal;
	}

	@Override
	public void toString(StringBuilder sb) {
		if( literal == null ) {
			sb.append("null");
		} else if( literal instanceof String) {
			sb.append("'");
			sb.append(literal.toString());
			sb.append("'");
		} else {
			sb.append(literal.toString());
		}
	}
}
