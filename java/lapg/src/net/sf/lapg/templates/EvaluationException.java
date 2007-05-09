package net.sf.lapg.templates;

import net.sf.lapg.templates.ast.ExpressionNode;

public class EvaluationException extends Exception {

	private static final long serialVersionUID = -1507473514714934980L;

	private ExpressionNode expr;
	private Object context;

	public EvaluationException(ExpressionNode expr, Object context, String message, Throwable th) {
		super("Evaluation of `"+expr.toString()+"` failed for " + context.getClass().getCanonicalName() + ": " + message, th);
		this.expr = expr;
		this.context = context;
	}
}
