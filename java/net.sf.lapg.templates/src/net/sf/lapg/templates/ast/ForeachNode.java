package net.sf.lapg.templates.ast;

import java.util.Collection;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ForeachNode extends CompoundNode {

	private static final String INDEX = "index";
	String var;
	ExpressionNode selectExpr, targetExpr;

	public ForeachNode(String var, ExpressionNode selectExpr) {
		this.var = var;
		this.selectExpr = selectExpr;
		this.targetExpr = null;
	}

	public ForeachNode(String var, ExpressionNode selectExpr, ExpressionNode targetExpr) {
		this.var = var;
		this.selectExpr = selectExpr;
		this.targetExpr = targetExpr;
	}

	@Override
	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		try {
			Object select = env.evaluate(selectExpr, context, false);
			Object prevVar = env.getVariable(var);
			Object prevIndex = env.getVariable(INDEX);
			int index = 0;
			try {
				if( targetExpr != null ) {
					Object to = env.evaluate(targetExpr, context, false);
					if( select instanceof Integer && to instanceof Integer ) {
						int toInt = (Integer)to;
						int delta = toInt >= (Integer)select ? 1 : -1;
						for( int i = (Integer)select; (delta > 0 ? i <= toInt : i >= toInt); i += delta ) {
							env.setVariable(var, i);
							for( Node n : instructions ) {
								n.emit(sb, context, env);
							}
						}
					} else {
						env.fireError("In for `"+selectExpr.toString()+"` and `"+targetExpr.toString()+"` should be Integers for " + context.getClass().getCanonicalName());
					}
				} else if( select instanceof Collection ) {
					for( Object o : (Collection<?>)select ) {
						env.setVariable(var, o);
						env.setVariable(INDEX, index++);
						for( Node n : instructions ) {
							n.emit(sb, context, env);
						}
					}
				} else if(select instanceof Object[]) {
					for( Object o : (Object[])select ) {
						env.setVariable(var, o);
						env.setVariable(INDEX, index++);
						for( Node n : instructions ) {
							n.emit(sb, context, env);
						}
					}
				} else {
					env.fireError("In foreach `"+selectExpr.toString()+"` should be Object[] or Collection for " + context.getClass().getCanonicalName());
				}
			} finally {
				env.setVariable(var, prevVar);
				env.setVariable(INDEX, prevIndex);
			}
		} catch( EvaluationException ex ) {
			/* ignore, skip foreach */
		}
	}
}
