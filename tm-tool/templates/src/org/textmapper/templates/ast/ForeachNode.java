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
package org.textmapper.templates.ast;

import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.ast.TemplatesTree.TextSource;

public class ForeachNode extends CompoundNode {

	private static final String INDEX = "index";
	private final String var;
	private final ExpressionNode selectExpr;
    private final ExpressionNode targetExpr;
    private final ExpressionNode separatorExpr;

	public ForeachNode(String var, ExpressionNode selectExpr, ExpressionNode targetExpr, ExpressionNode separator, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.var = var;
		this.selectExpr = selectExpr;
		this.targetExpr = targetExpr;
        this.separatorExpr = separator;
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			Object select = env.evaluate(selectExpr, context, false);
            String separator = separatorExpr == null ? null : env.toString(env.evaluate(separatorExpr, context, false), separatorExpr);
			int index = 0;
            int lastSeparator = sb.length();
			if( targetExpr != null ) {
				Object to = env.evaluate(targetExpr, context, false);
				if( select instanceof Integer && to instanceof Integer ) {
					int toInt = (Integer)to;
					int delta = toInt >= (Integer)select ? 1 : -1;
					for( int i = (Integer)select; (delta > 0 ? i <= toInt : i >= toInt); i += delta ) {
						EvaluationContext innerContext = new EvaluationContext(context.getThisObject(), context);
						innerContext.setVariable(var, i);
						if(separator != null && lastSeparator < sb.length()) {
							sb.append(separator);
							lastSeparator = sb.length();
						}
						for( Node n : instructions ) {
							n.emit(sb, innerContext, env);
						}
					}
				} else {
					env.report(TemplatesStatus.KIND_ERROR, "In for `" + selectExpr.toString() + "` and `" + targetExpr.toString() + "` should be Integers for " + env.getTitle(context), this);
				}
			} else if( select instanceof Iterable<?> ) {
				for( Object o : (Iterable<?>)select ) {
					EvaluationContext innerContext = new EvaluationContext(context.getThisObject(), context);
					innerContext.setVariable(var, o);
					innerContext.setVariable(INDEX, index++);
					if(separator != null && lastSeparator < sb.length()) {
						sb.append(separator);
						lastSeparator = sb.length();
					}
					for( Node n : instructions ) {
						n.emit(sb, innerContext, env);
					}
				}
			} else if(select instanceof Object[]) {
				for( Object o : (Object[])select ) {
					EvaluationContext innerContext = new EvaluationContext(context.getThisObject(), context);
					innerContext.setVariable(var, o);
					innerContext.setVariable(INDEX, index++);
					if(separator != null && lastSeparator < sb.length()) {
						sb.append(separator);
						lastSeparator = sb.length();
					}
					for( Node n : instructions ) {
						n.emit(sb, innerContext, env);
					}
				}
			} else {
				env.report(TemplatesStatus.KIND_ERROR, "In foreach `" + selectExpr.toString() + "` should be array or iterable for " + env.getTitle(context), this);
			}
		} catch( EvaluationException ex ) {
			/* ignore, skip foreach */
		}
	}
}
