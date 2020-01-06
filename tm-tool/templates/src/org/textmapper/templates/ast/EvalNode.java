/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.ast.TemplatesTree.TextSource;
import org.textmapper.templates.objects.IxWrapper;
import org.textmapper.templates.storage.Resource;

import java.io.File;
import java.net.URI;

public class EvalNode extends Node {

	private final ExpressionNode templateExpr;
	private final ExpressionNode templateLocation;

	public EvalNode(ExpressionNode expr, ExpressionNode templateId, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.templateExpr = expr;
		this.templateLocation = templateId;
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			Object toEvaluate = env.evaluate(templateExpr, context, false);
			String templateCode = env.toString(toEvaluate, templateExpr);
			Resource resource;
			if (templateLocation != null) {
				String id = env.toString(env.evaluate(templateLocation, context, false), templateLocation);
				resource = new Resource(URI.create(prepareForURI(id)), templateCode, 1, 0);
			} else {
				Object unwrapped = toEvaluate instanceof IxWrapper ? ((IxWrapper)toEvaluate).getObject() : toEvaluate;
				if (unwrapped instanceof SourceElement) {
					SourceElement elem = (SourceElement) unwrapped;
					File file = new File(elem.getResourceName());
					resource = new Resource(file.toURI(), templateCode, elem.getLine(), elem.getOffset());
				} else {
					resource = new Resource(URI.create("inline"), templateCode, 1, 0);
				}
			}
			sb.append(env.eval(resource, context));
		} catch (EvaluationException ex) {
			/* already handled, ignore */
		}
	}

	@Override
	public void toJavascript(StringBuilder sb) {
		sb.append("eval/*TODO*/(");
		templateExpr.toJavascript(sb);
		sb.append(')');
	}

	private String prepareForURI(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c >= '0' && c <= '9') {
				sb.append(c);
				continue;
			}
			if (c >= 16) {
				sb.append("%");
				sb.append(Integer.toHexString(c));
			}
		}
		return sb.toString();
	}
}
