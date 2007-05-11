package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class CallTemplateNode extends Node {
	
	String identifier;
	ExpressionNode[] arguments;
	ExpressionNode selectExpr;

	public CallTemplateNode(String identifier, List<ExpressionNode> args, ExpressionNode selectExpr) {
		this.identifier = identifier;
		this.arguments = args != null ? args.toArray(new ExpressionNode[args.size()]) : null;
		this.selectExpr = selectExpr;
	}
	
	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		try {
			Object callContext;
			if( selectExpr != null ) {
				callContext = env.evaluate(selectExpr, context, false);
				if( callContext == null )
					return;
			} else {
				callContext = context;
			}
	
			Object[] args = null;
			if( arguments != null ) {
				args = new Object[arguments.length];
				for( int i = 0; i < arguments.length; i++ ) {
					args[i] = env.evaluate(arguments[i], context, false);
					if( args[i] == null )
						return;
				}
			}
			sb.append(env.executeTemplate(identifier, callContext, args));
		} catch(EvaluationException ex) {
			/* some problems in expressions evaluation, skip call */
		}			
	}
}
