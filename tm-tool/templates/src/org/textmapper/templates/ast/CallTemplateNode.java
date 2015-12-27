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

import org.textmapper.templates.api.*;
import org.textmapper.templates.ast.TemplatesTree.TextSource;
import org.textmapper.templates.bundle.IBundleEntity;

import java.util.List;

public class CallTemplateNode extends ExpressionNode {

	private final String templateId;
	private ExpressionNode templateIdExpr;
	private final ExpressionNode[] arguments;
	private final ExpressionNode selectExpr;
	private final boolean isStatement;

	public CallTemplateNode(String identifier, List<ExpressionNode> args,
							ExpressionNode selectExpr, String currentPackage, boolean isStatement,
							TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.isStatement = isStatement;
		this.arguments = args != null ? args.toArray(new ExpressionNode[args.size()]) : null;
		this.selectExpr = selectExpr;
		this.templateId = identifier;
	}

	public CallTemplateNode(ExpressionNode identifier, List<ExpressionNode> args,
							ExpressionNode selectExpr, String currentPackage,
							TextSource source, int offset, int endoffset) {
		this(null, args, selectExpr, currentPackage, false, source, offset, endoffset);
		this.templateIdExpr = identifier;
	}

	private static String getTemplateId(EvaluationContext context, String templateId) {
		return templateId != null && templateId.indexOf('.') == -1 && !templateId.equals("base")
				? context.getCurrent().getPackage() + "." + templateId
				: templateId;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env)
			throws EvaluationException {
		EvaluationContext callContext = selectExpr != null
				? new EvaluationContext(env.evaluate(selectExpr, context, false), this, context)
				: context;
		String tid = getTemplateId(context, templateId != null
				? templateId
				: (String/* TODO */) env.evaluate(templateIdExpr, context, false));

		Object[] args = null;
		if (arguments != null) {
			args = new Object[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				args[i] = env.evaluate(arguments[i], context, false);
			}
		}

		IBundleEntity t = null;
		boolean isBase = false;
		IBundleEntity current = callContext.getCurrent();
		if (tid.equals("base")) {
			if (current != null) {
				isBase = true;
				t = current.getBase();
				if (t == null) {
					env.report(TemplatesStatus.KIND_ERROR,
							"Cannot find base template for `" + current.getName() + "`", this);
				}
			}
		}
		if (!isBase) {
			t = env.loadEntity(tid, IBundleEntity.KIND_ANY, this);
		}
		if (t instanceof ITemplate) {
			return env.evaluate((ITemplate) t, callContext, args, this);
		} else if (t instanceof IQuery) {
			return env.evaluate((IQuery) t, callContext, args, this);
		} else {
			return "";
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		if (!isStatement) {
			if (selectExpr != null) {
				selectExpr.toString(sb);
			} else {
				sb.append("self");
			}
			sb.append("->");
			if (templateId != null) {
				sb.append(templateId);
			} else {
				sb.append("(");
				templateIdExpr.toString(sb);
				sb.append(")");
			}
			sb.append("(");
			if (arguments != null) {
				for (int i = 0; i < arguments.length; i++) {
					if (i > 0) {
						sb.append(",");
					}
					arguments[i].toString(sb);
				}
			}
			sb.append(")");
		} else {
			// statement
			sb.append("call ");
			sb.append(templateId);
			if (arguments != null && arguments.length > 0) {
				sb.append("(");
				for (int i = 0; i < arguments.length; i++) {
					if (i > 0) {
						sb.append(",");
					}
					arguments[i].toString(sb);
				}
				sb.append(")");
			}
			if (selectExpr != null) {
				sb.append(" for ");
				selectExpr.toString(sb);
			}
		}
	}

	@Override
	public void toJavascript(StringBuilder sb) {
		if (templateId != null) {
			sb.append(templateId);
		} else {
			sb.append("(/*TODO*/");
			templateIdExpr.toString(sb);
			sb.append(")");
		}
		if (selectExpr != null && !(selectExpr instanceof ThisNode)) {
			sb.append(".call(");
			selectExpr.toString(sb);
			if (arguments != null && arguments.length > 0) {
				sb.append(",");
			}
		} else {
			sb.append("(");
		}
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				arguments[i].toString(sb);
			}
		}
		sb.append(")");
	}
}
