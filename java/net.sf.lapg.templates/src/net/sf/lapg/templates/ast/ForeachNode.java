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

import java.util.Collection;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class ForeachNode extends CompoundNode {

	private static final String INDEX = "index";
	private final String var;
	private final ExpressionNode selectExpr, targetExpr;

	public ForeachNode(String var, ExpressionNode selectExpr, String input, int line) {
		this(var, selectExpr, null, input, line);
	}

	public ForeachNode(String var, ExpressionNode selectExpr, ExpressionNode targetExpr, String input, int line) {
		super(input, line);
		this.var = var;
		this.selectExpr = selectExpr;
		this.targetExpr = targetExpr;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			Object select = env.evaluate(selectExpr, context, false);
			Object prevVar = context.getVariable(var);
			Object prevIndex = context.getVariable(INDEX);
			int index = 0;
			try {
				if( targetExpr != null ) {
					Object to = env.evaluate(targetExpr, context, false);
					if( select instanceof Integer && to instanceof Integer ) {
						int toInt = (Integer)to;
						int delta = toInt >= (Integer)select ? 1 : -1;
						for( int i = (Integer)select; (delta > 0 ? i <= toInt : i >= toInt); i += delta ) {
							context.setVariable(var, i);
							for( Node n : instructions ) {
								n.emit(sb, context, env);
							}
						}
					} else {
						env.fireError(this, "In for `"+selectExpr.toString()+"` and `"+targetExpr.toString()+"` should be Integers for " + env.getTitle(context));
					}
				} else if( select instanceof Collection ) {
					for( Object o : (Collection<?>)select ) {
						context.setVariable(var, o);
						context.setVariable(INDEX, index++);
						for( Node n : instructions ) {
							n.emit(sb, context, env);
						}
					}
				} else if(select instanceof Object[]) {
					for( Object o : (Object[])select ) {
						context.setVariable(var, o);
						context.setVariable(INDEX, index++);
						for( Node n : instructions ) {
							n.emit(sb, context, env);
						}
					}
				} else {
					env.fireError(this, "In foreach `"+selectExpr.toString()+"` should be Object[] or Collection for " + env.getTitle(context));
				}
			} finally {
				context.setVariable(var, prevVar);
				context.setVariable(INDEX, prevIndex);
			}
		} catch( EvaluationException ex ) {
			/* ignore, skip foreach */
		}
	}
}
