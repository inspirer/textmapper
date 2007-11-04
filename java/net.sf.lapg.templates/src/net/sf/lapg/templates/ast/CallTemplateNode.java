package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class CallTemplateNode extends Node {

	private String identifier;
	private ExpressionNode[] arguments;
	private ExpressionNode selectExpr;
	private String currentPackage;

	public CallTemplateNode(String identifier, List<ExpressionNode> args, ExpressionNode selectExpr, String currentPackage) {
		this.identifier = identifier;
		this.arguments = args != null ? args.toArray(new ExpressionNode[args.size()]) : null;
		this.selectExpr = selectExpr;
		this.currentPackage = currentPackage;
	}

	@Override
	protected void emit(StringBuffer sb, Object context, IEvaluationEnvironment env) {
		try {
			Object callContext = selectExpr != null ? env.evaluate(selectExpr, context, false) : context;

			Object[] args = null;
			if( arguments != null ) {
				args = new Object[arguments.length];
				for( int i = 0; i < arguments.length; i++ ) {
					args[i] = env.evaluate(arguments[i], context, false);
					if( args[i] == null ) {
						return;
					}
				}
			}
			String qualifiedName = identifier.indexOf('.') == -1 ? currentPackage + "." + identifier : identifier;
			sb.append(env.executeTemplate(qualifiedName, callContext, args));
		} catch(EvaluationException ex) {
			/* some problems in expressions evaluation, skip call */
		}
	}
}
